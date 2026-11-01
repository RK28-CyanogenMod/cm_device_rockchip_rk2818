/* Android 4.2 media service adapter for the RK2818 OpenCORE hardware player. */
#define LOG_TAG "RK28PVPlayer"
#include <media/MediaPlayerInterface.h>
#include <binder/Parcel.h>
#include <gui/SurfaceTextureClient.h>
#include <ui/GraphicBufferMapper.h>
#include <ui/Rect.h>
#include <ui/Fence.h>
#include <utils/Log.h>
#include <utils/Timers.h>
#include <string.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <linux/fb.h>
#include <pthread.h>
#include <utils/threads.h>
#include <gui/RK28Overlay.h>
#include <dlfcn.h>
#include <cutils/properties.h>
#include "legacy/hardware/overlay.h"
#include "rk28_pv_bridge.h"
namespace android {
static Mutex gOverlayLeaseLock;
static void*gOverlayOwner;
class RK28PVPlayer:public MediaPlayerInterface {
    void*mLegacy;
    Mutex mSurfaceLock;
    sp<ANativeWindow>mWindow;
    int mWidth,mHeight;
    overlay_control_device_t*mControl;
    overlay_data_device_t*mData;
    overlay_t*mOverlay;
    int mOverlayWidth,mOverlayHeight;
    unsigned mOverlayFrames;
    nsecs_t mOverlayStart;
    int mOverlayX,mOverlayY,mOverlayW,mOverlayH;
    bool mOverlayVisible,mMonitorStarted,mMonitorExit;
    pthread_t mMonitor;
    Condition mMonitorWake;
    static void*monitor(void*c){((RK28PVPlayer*)c)->monitorLoop();return NULL;}
    void monitorLoop(){
        Mutex::Autolock lock(mSurfaceLock);
        while(!mMonitorExit){
            if(mOverlay){
                if(!syncOverlay())releaseOverlay();
                mMonitorWake.waitRelative(mSurfaceLock,100000000LL);
            }else mMonitorWake.wait(mSurfaceLock);
        }
    }
    int overlayGeometry(int&x,int&y,int&w,int&h,int&state){
        int pos=0,size=0;
        int err=mWindow->query(mWindow.get(),RK28_OVERLAY_POSITION,&pos);
        if(!err)err=mWindow->query(mWindow.get(),RK28_OVERLAY_SIZE,&size);
        if(!err)err=mWindow->query(mWindow.get(),RK28_OVERLAY_STATE,&state);
        x=(int16_t)(pos&0xffff);y=(int16_t)((uint32_t)pos>>16);
        w=size&0xffff;h=(uint32_t)size>>16;
        return err;
    }
    bool syncOverlay(){
        int x,y,w,h,state=0;
        if(overlayGeometry(x,y,w,h,state))return false;
        // Arbitrary transforms and offscreen cropping use the RGB fallback.
        if((state&0xff)||x<0||y<0)return false;
        bool visible=(state&RK28_OVERLAY_VISIBLE)&&w>0&&h>0;
        if(visible&&(x!=mOverlayX||y!=mOverlayY||w!=mOverlayW||h!=mOverlayH)){
            int err=mControl->setPosition(mControl,mOverlay,x,y,w,h);
            if(!err)err=mControl->commit(mControl,mOverlay);
            if(err){ALOGW("Position overlay: %d",err);return false;}
            mOverlayX=x;mOverlayY=y;mOverlayW=w;mOverlayH=h;
            ALOGI("Overlay window %d,%d %dx%d",x,y,w,h);
        }
        if(visible!=mOverlayVisible){
            int fd=mOverlay->getHandleRef(mOverlay)->data[0];
            if(ioctl(fd,FBIOBLANK,visible?FB_BLANK_UNBLANK:FB_BLANK_NORMAL)<0){
                ALOGW("Blank overlay: %s",strerror(errno));return false;
            }
            mOverlayVisible=visible;
            ALOGI("Overlay visibility %d",visible);
        }
        return true;
    }
    unsigned mFrames;
    void*mPullCookie;RK28AudioPull mPull;
    static void event(void*c,int m,int a,int b){((RK28PVPlayer*)c)->sendEvent(m,a,b);}
    static size_t audioFill(AudioSink*,void*b,size_t n,void*c){
        RK28PVPlayer*p=(RK28PVPlayer*)c;return p->mPull?p->mPull(p->mPullCookie,b,n):0;
    }
    static int audioOpen(void*c,uint32_t r,int ch,int f,int count,void*pc,RK28AudioPull pull){
        RK28PVPlayer*p=(RK28PVPlayer*)c;
        if(!p->mAudioSink.get())return NO_INIT;
        p->mPullCookie=pc;p->mPull=pull;
        return p->mAudioSink->open(r,ch,CHANNEL_MASK_USE_CHANNEL_ORDER,
                (audio_format_t)f,count,pull?audioFill:NULL,p);
    }
    static ssize_t audioWrite(void*c,const void*b,size_t n){
        RK28PVPlayer*p=(RK28PVPlayer*)c;return p->mAudioSink.get()?p->mAudioSink->write(b,n):NO_INIT;
    }
    static void audioCommand(void*c,int cmd){
        RK28PVPlayer*p=(RK28PVPlayer*)c;if(!p->mAudioSink.get())return;
        switch(cmd){
        case RK28_AUDIO_START:p->mAudioSink->start();break;
        case RK28_AUDIO_STOP:p->mAudioSink->stop();break;
        case RK28_AUDIO_FLUSH:p->mAudioSink->flush();break;
        case RK28_AUDIO_PAUSE:p->mAudioSink->pause();break;
        case RK28_AUDIO_CLOSE:p->mAudioSink->close();break;
        }
    }
    static int audioPosition(void*c,uint32_t*v){
        RK28PVPlayer*p=(RK28PVPlayer*)c;return p->mAudioSink.get()?p->mAudioSink->getPosition(v):NO_INIT;
    }
    static int audioInfo(void*c,int key){
        RK28PVPlayer*p=(RK28PVPlayer*)c;
        if(!p->mAudioSink.get())return 0;
        switch(key){
        case RK28_AUDIO_READY:return p->mAudioSink->ready();
        case RK28_AUDIO_BUFFER_SIZE:return p->mAudioSink->bufferSize();
        case RK28_AUDIO_FRAME_COUNT:return p->mAudioSink->frameCount();
        case RK28_AUDIO_CHANNELS:return p->mAudioSink->channelCount();
        case RK28_AUDIO_FRAME_SIZE:return p->mAudioSink->frameSize();
        case RK28_AUDIO_LATENCY:return p->mAudioSink->latency();
        case RK28_AUDIO_SESSION_ID:return p->mAudioSink->getSessionId();
        }
        return 0;
    }
    static int dspFrame(void*c,uint32_t phys,int w,int h){return ((RK28PVPlayer*)c)->directFrame(phys,w,h);}
    static void closeOverlay(void*c){RK28PVPlayer*p=(RK28PVPlayer*)c;Mutex::Autolock l(p->mSurfaceLock);p->releaseOverlay();}
    void releaseOverlay(){
        if(mData){overlay_data_close(mData);mData=NULL;}
        if(mOverlay){mControl->destroyOverlay(mControl,mOverlay);mOverlay=NULL;}
        if(mControl){overlay_control_close(mControl);mControl=NULL;}
        {Mutex::Autolock lease(gOverlayLeaseLock);if(gOverlayOwner==this)gOverlayOwner=NULL;}
        mOverlayFrames=0;mOverlayX=mOverlayY=mOverlayW=mOverlayH=-1;
    }
    int directFrame(uint32_t phys,int w,int h){
        char mode[PROPERTY_VALUE_MAX];property_get("media.rk28.overlay",mode,"1");
        Mutex::Autolock l(mSurfaceLock);
        if(strcmp(mode,"1")){releaseOverlay();return 0;}
        if(!mWindow.get()||!mMonitorStarted)return 0;
        if(mOverlay&&(w!=mOverlayWidth||h!=mOverlayHeight))releaseOverlay();
        if(!mOverlay){
            int x,y,ow,oh,state=0;
            if(overlayGeometry(x,y,ow,oh,state)||(state&0xff)||x<0||y<0)return 0;
            if(!(state&RK28_OVERLAY_VISIBLE)||ow<=0||oh<=0){
                // 4.2 considers a BufferQueue layer invisible until its first
                // buffer. Seed the color-key surface so geometry can appear.
                if(mWidth!=w||mHeight!=h){
                    unsigned char*black=(unsigned char*)calloc(w*h,2);
                    if(!black)return 0;
                    int err=renderLocked(black,w,h,w,HAL_PIXEL_FORMAT_RGB_565);
                    free(black);if(err)return 0;
                }
                return 1;
            }
            // The original HAL spins forever when createOverlay is called
            // while another overlay exists. Reserve its single plane first.
            {Mutex::Autolock lease(gOverlayLeaseLock);
             if(gOverlayOwner&&gOverlayOwner!=this)return 0;
             gOverlayOwner=this;}
            static void*hal=dlopen("/system/lib/hw/overlay.rk28board.so",RTLD_NOW);
            const hw_module_t*module=hal?(const hw_module_t*)dlsym(hal,"HMI"):NULL;
            if(!module){ALOGE("Overlay HAL: %s",dlerror());releaseOverlay();return 0;}
            int err=overlay_control_open(module,&mControl);
            if(!err)err=overlay_data_open(module,&mData);
            if(!err)mOverlay=mControl->createOverlay(mControl,w,h,21);
            if(err||!mOverlay){ALOGE("Create overlay %dx%d: %d",w,h,err);releaseOverlay();return 0;}
            err=mData->initialize(mData,mOverlay->getHandleRef(mOverlay));
            mOverlayVisible=true;
            if(err||!syncOverlay()){releaseOverlay();return 0;}
            mOverlayWidth=w;mOverlayHeight=h;mOverlayStart=systemTime();
            ALOGI("Direct DSP overlay %dx%d",w,h);
            // The original display controller treats black UI pixels as its
            // video color key. Submit one hole buffer, not an RGB video stream.
            unsigned char*black=(unsigned char*)calloc(w*h,2);
            if(!black){releaseOverlay();return 0;}
            err=renderLocked(black,w,h,w,HAL_PIXEL_FORMAT_RGB_565);free(black);
            if(err){releaseOverlay();return 0;}
            mMonitorWake.signal();
        }
        int err=mData->queueBuffer(mData,&phys);
        if(err){ALOGE("Queue overlay: %d",err);releaseOverlay();return 0;}
        if(++mOverlayFrames==1||mOverlayFrames%250==0)
            ALOGI("Overlay frame %u: %08x, average %.2f fps",mOverlayFrames,phys,
                 mOverlayFrames*1e9/(systemTime()-mOverlayStart));
        return 1;
    }
    static void frame(void*c,const void*b,int w,int h,int stride,int format){
        ((RK28PVPlayer*)c)->render(b,w,h,stride,format);
    }
    void render(const void*data,int w,int h,int stride,int format){
        Mutex::Autolock lock(mSurfaceLock);
        if(!mOverlay)renderLocked(data,w,h,stride,format);
    }
    int renderLocked(const void*data,int w,int h,int stride,int format){
        nsecs_t t0=systemTime();
        if(!mWindow.get())return NO_INIT;
        if(w!=mWidth||h!=mHeight){
            int err=native_window_set_usage(mWindow.get(),GRALLOC_USAGE_SW_WRITE_OFTEN|GRALLOC_USAGE_HW_TEXTURE);
            if(!err)err=native_window_set_scaling_mode(mWindow.get(),NATIVE_WINDOW_SCALING_MODE_SCALE_TO_WINDOW);
            if(!err)err=native_window_set_buffers_geometry(mWindow.get(),w,h,format);
            if(err){ALOGE("Configure surface: %d",err);return err;}
            mWidth=w;mHeight=h;
        }
        ANativeWindowBuffer*buf=NULL;
        int fenceFd=-1;
        int err=mWindow->dequeueBuffer(mWindow.get(),&buf,&fenceFd);
        nsecs_t t1=systemTime();
        if(err){ALOGW("dequeue: %d",err);return err;}
        sp<Fence> fence(new Fence(fenceFd));
        err=fence->waitForever(1000,"RK28PVPlayer");
        void*dst=NULL;
        GraphicBufferMapper&mapper=GraphicBufferMapper::get();
        if(!err)err=mapper.lock(buf->handle,GRALLOC_USAGE_SW_WRITE_OFTEN,Rect(w,h),&dst);
        if(err){mWindow->cancelBuffer(mWindow.get(),buf,-1);ALOGW("lock: %d",err);return err;}
        for(int y=0;y<h;++y)
            memcpy((char*)dst+y*buf->stride*2,(const char*)data+y*stride*2,w*2);
        mapper.unlock(buf->handle);
        nsecs_t t2=systemTime();
        err=mWindow->queueBuffer(mWindow.get(),buf,-1);
        if(err)ALOGW("queue: %d",err);
        else if(++mFrames==1||mFrames%120==0)ALOGI("Rendered DSP frame %u (%dx%d) dequeue %lld copy %lld queue %lld us",mFrames,w,h,(t1-t0)/1000,(t2-t1)/1000,(systemTime()-t2)/1000);
        return err;
    }
    int command(int c,int*v=NULL){return mLegacy?rk28_pv_command(mLegacy,c,v):NO_INIT;}
public:
    RK28PVPlayer():mLegacy(NULL),mWidth(0),mHeight(0),mControl(NULL),mData(NULL),mOverlay(NULL),mOverlayWidth(0),mOverlayHeight(0),mOverlayFrames(0),mOverlayStart(0),mOverlayX(-1),mOverlayY(-1),mOverlayW(-1),mOverlayH(-1),mOverlayVisible(false),mMonitorStarted(false),mMonitorExit(false),mFrames(0),mPullCookie(NULL),mPull(NULL){
        RK28PVCallbacks cb={event,audioOpen,audioWrite,audioCommand,audioPosition,audioInfo,frame,dspFrame,closeOverlay};
        mLegacy=rk28_pv_create(&cb,this);
        mMonitorStarted=pthread_create(&mMonitor,NULL,monitor,this)==0;
        ALOGI("OpenCORE hardware backend %s",mLegacy?"created":"failed");
    }
    virtual ~RK28PVPlayer(){
        rk28_pv_destroy(mLegacy);
        {Mutex::Autolock l(mSurfaceLock);mMonitorExit=true;mMonitorWake.signal();}
        if(mMonitorStarted)pthread_join(mMonitor,NULL);
        releaseOverlay();
    }
    virtual status_t initCheck(){return mLegacy?OK:NO_INIT;}
    virtual status_t setUID(uid_t){return OK;}
    virtual status_t setDataSource(const char*u,const KeyedVector<String8,String8>*h){
        return mLegacy?rk28_pv_source_url(mLegacy,u,h):NO_INIT;
    }
    virtual status_t setDataSource(int fd,int64_t off,int64_t len){
        return mLegacy?rk28_pv_source_fd(mLegacy,fd,off,len):NO_INIT;
    }
    virtual status_t setVideoSurfaceTexture(const sp<ISurfaceTexture>&s){
        Mutex::Autolock lock(mSurfaceLock);
        releaseOverlay();
        mWindow=s.get()?new SurfaceTextureClient(s):NULL;
        mWidth=mHeight=0;
        // MediaPlayerService::Client owns the MEDIA connection in Android 4.2.
        // The backend only queues buffers; connecting here a second time fails.
        return OK;
    }
    virtual status_t prepare(){return command(RK28_PV_PREPARE);}
    virtual status_t prepareAsync(){return command(RK28_PV_PREPARE_ASYNC);}
    virtual status_t start(){return command(RK28_PV_START);}
    virtual status_t stop(){return command(RK28_PV_STOP);}
    virtual status_t pause(){return command(RK28_PV_PAUSE);}
    virtual bool isPlaying(){return command(RK28_PV_PLAYING)>0;}
    virtual status_t seekTo(int n){return command(RK28_PV_SEEK,&n);}
    virtual status_t getCurrentPosition(int*n){return command(RK28_PV_POSITION,n);}
    virtual status_t getDuration(int*n){return command(RK28_PV_DURATION,n);}
    virtual status_t reset(){return command(RK28_PV_RESET);}
    virtual status_t setLooping(int n){return command(RK28_PV_LOOP,&n);}
    virtual player_type playerType(){return PV_PLAYER;}
    virtual status_t setParameter(int,const Parcel&){return INVALID_OPERATION;}
    virtual status_t getParameter(int,Parcel*){return INVALID_OPERATION;}
    virtual status_t invoke(const Parcel&q,Parcel*r){return mLegacy?rk28_pv_invoke(mLegacy,&q,r):NO_INIT;}
    virtual status_t getMetadata(const media::Metadata::Filter&i,Parcel*r){
        return mLegacy?rk28_pv_metadata(mLegacy,&i,r):NO_INIT;
    }
};
MediaPlayerBase*createRK28PVPlayer(){return new RK28PVPlayer;}
}
