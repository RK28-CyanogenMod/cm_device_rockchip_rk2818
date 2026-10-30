/* Android 4.0 media service adapter for the RK2818 OpenCORE hardware player. */
#define LOG_TAG "RK28PVPlayer"
#include <media/MediaPlayerInterface.h>
#include <binder/Parcel.h>
#include <gui/SurfaceTextureClient.h>
#include <ui/GraphicBufferMapper.h>
#include <ui/Rect.h>
#include <utils/Log.h>
#include <utils/Timers.h>
#include <string.h>
#include "rk28_pv_bridge.h"
namespace android {
class RK28PVPlayer:public MediaPlayerInterface {
    void*mLegacy;
    Mutex mSurfaceLock;
    sp<ANativeWindow>mWindow;
    int mWidth,mHeight;
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
        return p->mAudioSink->open(r,ch,f,count,pull?audioFill:NULL,p);
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
    static void frame(void*c,const void*b,int w,int h,int stride,int format){
        ((RK28PVPlayer*)c)->render(b,w,h,stride,format);
    }
    void render(const void*data,int w,int h,int stride,int format){
        nsecs_t t0=systemTime();
        Mutex::Autolock lock(mSurfaceLock);
        if(!mWindow.get())return;
        if(w!=mWidth||h!=mHeight){
            int err=native_window_set_usage(mWindow.get(),GRALLOC_USAGE_SW_WRITE_OFTEN|GRALLOC_USAGE_HW_TEXTURE);
            if(!err)err=native_window_set_scaling_mode(mWindow.get(),NATIVE_WINDOW_SCALING_MODE_SCALE_TO_WINDOW);
            if(!err)err=native_window_set_buffers_geometry(mWindow.get(),w,h,format);
            if(err){LOGE("Configure surface: %d",err);return;}
            mWidth=w;mHeight=h;
        }
        ANativeWindowBuffer*buf=NULL;
        int err=mWindow->dequeueBuffer(mWindow.get(),&buf);
        nsecs_t t1=systemTime();
        if(err){LOGW("dequeue: %d",err);return;}
        err=mWindow->lockBuffer(mWindow.get(),buf);
        void*dst=NULL;
        GraphicBufferMapper&mapper=GraphicBufferMapper::get();
        if(!err)err=mapper.lock(buf->handle,GRALLOC_USAGE_SW_WRITE_OFTEN,Rect(w,h),&dst);
        if(err){mWindow->cancelBuffer(mWindow.get(),buf);LOGW("lock: %d",err);return;}
        for(int y=0;y<h;++y)
            memcpy((char*)dst+y*buf->stride*2,(const char*)data+y*stride*2,w*2);
        mapper.unlock(buf->handle);
        nsecs_t t2=systemTime();
        err=mWindow->queueBuffer(mWindow.get(),buf);
        if(err)LOGW("queue: %d",err);
        else if(++mFrames==1||mFrames%120==0)LOGI("Rendered DSP frame %u (%dx%d) dequeue %lld copy %lld queue %lld us",mFrames,w,h,(t1-t0)/1000,(t2-t1)/1000,(systemTime()-t2)/1000);
    }
    int command(int c,int*v=NULL){return mLegacy?rk28_pv_command(mLegacy,c,v):NO_INIT;}
public:
    RK28PVPlayer():mLegacy(NULL),mWidth(0),mHeight(0),mFrames(0),mPullCookie(NULL),mPull(NULL){
        RK28PVCallbacks cb={event,audioOpen,audioWrite,audioCommand,audioPosition,audioInfo,frame};
        mLegacy=rk28_pv_create(&cb,this);
        LOGI("OpenCORE hardware backend %s",mLegacy?"created":"failed");
    }
    virtual ~RK28PVPlayer(){rk28_pv_destroy(mLegacy);}
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
        mWindow=s.get()?new SurfaceTextureClient(s):NULL;
        mWidth=mHeight=0;return OK;
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
