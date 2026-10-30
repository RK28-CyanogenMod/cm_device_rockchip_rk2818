/*
 * RK2818 vendor OpenCORE compatibility layer.
 * Legacy player classes have distinct names to keep their vtables separate
 * from the ICS MediaPlayerBase classes loaded in the same process.
 */
#define LOG_TAG "RK28PVLegacy"
#define MediaPlayerBase RKMediaPlayBase
#define MediaPlayerInterface RKMediaPlayInterface
#define MediaPlayerHWInterface RKMediaPlayHWInterface
#include <media/PVPlayer.h>
#include <surfaceflinger/ISurface.h>
#include <binder/IMemory.h>
#include <binder/Parcel.h>
#include <ui/GraphicBuffer.h>
#include <cutils/ashmem.h>
#include <utils/Log.h>
#include <utils/Timers.h>
#include <dlfcn.h>
#include <sys/mman.h>
#include <unistd.h>
#include <new>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <pthread.h>
#include <utils/threads.h>
#include "rk28_pv_bridge.h"
using namespace android;
namespace android {
// The factory always declines overlay creation; frames use register/postBuffer.
class OverlayRef:public LightRefBase<OverlayRef>{public:~OverlayRef(){}};

// The vendor allocates 52 bytes for GB MemoryHeapBase. ICS adds mOffset and
// changes virtual-base layout. Use a local GB-layout heap, never sent over IPC.
class RK28MemoryHeap:public virtual BnMemoryHeap {
    int mFD;size_t mSize;void*mBase;uint32_t mFlags;const char*mDevice;bool mNeedUnmap;
public:
    RK28MemoryHeap(size_t size,uint32_t flags,const char*name);
    virtual ~RK28MemoryHeap(){if(mBase!=MAP_FAILED)munmap(mBase,mSize);if(mFD>=0)::close(mFD);}
    virtual int getHeapID()const{return mFD;}
    virtual void*getBase()const{return mBase;}
    virtual size_t getSize()const{return mSize;}
    virtual uint32_t getFlags()const{return mFlags;}
};
RK28MemoryHeap::RK28MemoryHeap(size_t size,uint32_t flags,const char*name):
mFD(-1),mSize((size+4095)&~4095),mBase(MAP_FAILED),mFlags(flags),mDevice(NULL),mNeedUnmap(true){
    mFD=ashmem_create_region(name?name:"rk28-video",mSize);
    if(mFD>=0)mBase=mmap(NULL,mSize,PROT_READ|PROT_WRITE,MAP_SHARED,mFD,0);
}
ISurface::BufferHeap::BufferHeap():w(0),h(0),hor_stride(0),ver_stride(0),format(0),transform(0),flags(0){}
ISurface::BufferHeap::BufferHeap(uint32_t W,uint32_t H,int32_t X,int32_t Y,PixelFormat F,const sp<IMemoryHeap>&M):
w(W),h(H),hor_stride(X),ver_stride(Y),format(F),transform(0),flags(0),heap(M){}
ISurface::BufferHeap::~BufferHeap(){}
ISurface::ISurface(){}
ISurface::~ISurface(){}
const String16 ISurface::descriptor("android.ui.ISurface");
const String16&ISurface::getInterfaceDescriptor()const{return descriptor;}
}
// Playback never uses PV's obsolete directory scanner. Reject that entry.
extern "C" int rk28_old_scan(void*,const char*,const char*,void*,void*,void*)
 asm("_ZN7android12MediaScanner16processDirectoryEPKcS2_RNS_18MediaScannerClientEPFbPvES5_");
extern "C" int rk28_old_scan(void*,const char*,const char*,void*,void*,void*){return INVALID_OPERATION;}

struct LegacyPlayer;
static pthread_key_t surfaceKey;
static pthread_once_t surfaceKeyOnce=PTHREAD_ONCE_INIT;
static void makeSurfaceKey(){pthread_key_create(&surfaceKey,NULL);}
class LegacySurface:public ISurface {
    RK28PVCallbacks cb;void*cookie;
    sp<IMemoryHeap> heap;int width,height,stride,format;unsigned frames;bool asynchronous;
public:
    LegacySurface(const RK28PVCallbacks&C,void*c):cb(C),cookie(c),width(0),height(0),stride(0),format(0),frames(0),asynchronous(false){}
    virtual IBinder*onAsBinder(){return NULL;}
    virtual sp<GraphicBuffer>requestBuffer(int,uint32_t,uint32_t,uint32_t,uint32_t){return NULL;}
    virtual status_t setBufferCount(int){return INVALID_OPERATION;}
    virtual status_t registerBuffers(const BufferHeap&b){
        if(b.format!=HAL_PIXEL_FORMAT_RGB_565||!b.heap.get()||b.w==0||b.h==0||b.hor_stride<(int)b.w)
            return BAD_VALUE;
        // Vendor ColorConvert16 uses displayWidth as RGB destination pitch.
        // hor_stride is the coded YUV width and only sizes the frame slots;
        // using it for RGB rows scrambles videos whose width is not 16-aligned.
        heap=b.heap;width=b.w;height=b.h;stride=b.w;format=b.format;
        pthread_once(&surfaceKeyOnce,makeSurfaceKey);pthread_setspecific(surfaceKey,this);
        LOGI("Video buffer %dx%d stride %d RGB565",width,height,stride);return OK;
    }
    void setAsynchronous(){asynchronous=true;}
    void deliver(const void*data,int w,int h,int pitch){cb.video_frame(cookie,data,w,h,pitch,HAL_PIXEL_FORMAT_RGB_565);}
    virtual void postBuffer(ssize_t off){
        if(asynchronous)return;
        size_t bytes=(size_t)stride*height*2;
        if(!heap.get()||off<0||(size_t)off>heap->getSize()||bytes>heap->getSize()-(size_t)off)return;
        void*base=heap->getBase();
        if(!base||base==MAP_FAILED)return;
        cb.video_frame(cookie,(char*)base+off,width,height,stride,format);
        if(++frames==1||frames%120==0)LOGI("DSP frame delivered %u",frames);
    }
    virtual void unregisterBuffers(){heap.clear();width=height=stride=0;}
    virtual sp<OverlayRef>createOverlay(uint32_t,uint32_t,int32_t,int32_t){return NULL;}
};
class LegacyAudio:public RKMediaPlayBase::AudioSink {
    RK28PVCallbacks cb;void*cookie;uint32_t rate;AudioCallback pull;void*pullCookie;
    static size_t fill(void*c,void*b,size_t n){LegacyAudio*s=(LegacyAudio*)c;return s->pull?s->pull(s,b,n,s->pullCookie):0;}
public:
    LegacyAudio(const RK28PVCallbacks&C,void*c):cb(C),cookie(c),rate(44100),pull(NULL),pullCookie(NULL){}
    virtual bool ready()const{return cb.audio_info(cookie,RK28_AUDIO_READY);}
    virtual bool realtime()const{return true;}
    virtual ssize_t bufferSize()const{return cb.audio_info(cookie,RK28_AUDIO_BUFFER_SIZE);}
    virtual ssize_t frameCount()const{return cb.audio_info(cookie,RK28_AUDIO_FRAME_COUNT);}
    virtual ssize_t channelCount()const{return cb.audio_info(cookie,RK28_AUDIO_CHANNELS);}
    virtual ssize_t frameSize()const{return cb.audio_info(cookie,RK28_AUDIO_FRAME_SIZE);}
    virtual uint32_t latency()const{return cb.audio_info(cookie,RK28_AUDIO_LATENCY);}
    virtual float msecsPerFrame()const{return 1000.0f/rate;}
    virtual status_t getPosition(uint32_t*p){return cb.audio_position(cookie,p);}
    virtual int getSessionId(){return cb.audio_info(cookie,RK28_AUDIO_SESSION_ID);}
    virtual status_t open(uint32_t r,int ch,int f,int n,AudioCallback p,void*c){
        rate=r;pull=p;pullCookie=c;return cb.audio_open(cookie,r,ch,f,n,this,p?fill:NULL);
    }
    virtual void start(){cb.audio_command(cookie,RK28_AUDIO_START);}
    virtual ssize_t write(const void*b,size_t n){return cb.audio_write(cookie,b,n);}
    virtual void stop(){cb.audio_command(cookie,RK28_AUDIO_STOP);}
    virtual void flush(){cb.audio_command(cookie,RK28_AUDIO_FLUSH);}
    virtual void pause(){cb.audio_command(cookie,RK28_AUDIO_PAUSE);}
    virtual void close(){cb.audio_command(cookie,RK28_AUDIO_CLOSE);}
};
struct LegacyPlayer {
    PVPlayer*player;RK28PVCallbacks cb;void*cookie;
    sp<LegacySurface> surface;sp<LegacyAudio> audio;
};
static void legacyNotify(void*c,int m,int a,int b){LegacyPlayer*p=(LegacyPlayer*)c;p->cb.notify(p->cookie,m,a,b);}
extern "C" void* rk28_pv_create(const RK28PVCallbacks*cb,void*cookie){
    static void*h=dlopen("libopencore_player.so",RTLD_NOW);
    if(!h){LOGE("Load OpenCORE: %s",dlerror());return NULL;}
    typedef void(*Ctor)(void*);
    Ctor ctor=(Ctor)dlsym(h,"_ZN7android8PVPlayerC1Ev");
    if(!ctor)return NULL;
    LegacyPlayer*p=new LegacyPlayer;
    p->cb=*cb;p->cookie=cookie;
    p->player=(PVPlayer*)::operator new(sizeof(PVPlayer));
    ctor(p->player);
    if(p->player->initCheck()!=OK){delete p->player;delete p;return NULL;}
    p->player->setNotifyCallback(p,legacyNotify);
    p->audio=new LegacyAudio(*cb,cookie);
    p->surface=new LegacySurface(*cb,cookie);
    p->player->setAudioSink(p->audio);
    p->player->setVideoSurface(p->surface);
    return p;
}
extern "C" void rk28_pv_destroy(void*h){
    if(!h)return;
    LegacyPlayer*p=(LegacyPlayer*)h;
    p->player->setNotifyCallback(NULL,NULL);
    p->player->reset();delete p->player;delete p;
}
extern "C" int rk28_pv_source_url(void*h,const char*u,const void*headers){
    return ((LegacyPlayer*)h)->player->setDataSource(u,(const KeyedVector<String8,String8>*)headers);
}
extern "C" int rk28_pv_source_fd(void*h,int f,int64_t o,int64_t l){return ((LegacyPlayer*)h)->player->setDataSource(f,o,l);}
extern "C" int rk28_pv_command(void*h,int cmd,int*v){
    PVPlayer*p=((LegacyPlayer*)h)->player;
    switch(cmd){
    case RK28_PV_PREPARE:return p->prepare();
    case RK28_PV_PREPARE_ASYNC:return p->prepareAsync();
    case RK28_PV_START:return p->start();
    case RK28_PV_STOP:return p->stop();
    case RK28_PV_PAUSE:return p->pause();
    case RK28_PV_PLAYING:return p->isPlaying();
    case RK28_PV_SEEK:return p->seekTo(*v);
    case RK28_PV_POSITION:return p->getCurrentPosition(v);
    case RK28_PV_DURATION:return p->getDuration(v);
    case RK28_PV_RESET:return p->reset();
    case RK28_PV_LOOP:return p->setLooping(*v);
    }
    return BAD_VALUE;
}
extern "C" int rk28_pv_invoke(void*h,const void*q,void*r){return ((LegacyPlayer*)h)->player->invoke(*(const Parcel*)q,(Parcel*)r);}
extern "C" int rk28_pv_metadata(void*h,const void*i,void*r){return ((LegacyPlayer*)h)->player->getMetadata(*(const media::Metadata::Filter*)i,(Parcel*)r);}

// Old DSP codecs return a physical frame address in the first word of each
// OMX output buffer. Only their first two buffers contain preview pixels.
// Resolve that address in the decoder's existing PMEM mapping and consume the
// RK line-paired luma/interleaved chroma layout before the DSP recycles it.
class PVColor {
public:
    virtual ~PVColor(){}
    virtual short capability()=0;
    virtual int init(int,int,int,int,int,int,int)=0;
    virtual void memheight(int)=0;
    virtual int mode(int)=0;
    virtual int convert(unsigned char*,unsigned char*)=0;
    virtual int size()=0;
    virtual int range(bool)=0;
};
class RK28Convert565:public PVColor {
    PVColor*real;int pitch,height,dstWidth,dstHeight;unsigned frames,displayed;
    uintptr_t mapStart,mapPhys;size_t mapSize,bufferSize;
    unsigned char*pendingPacked;unsigned char*renderPacked;unsigned char*planar;unsigned char*rgb;
    sp<LegacySurface>surface;
    Mutex lock;Condition ready;pthread_t worker;bool started,exiting,pending;
    static void*run(void*c){((RK28Convert565*)c)->renderLoop();return NULL;}
    void renderLoop(){
        for(;;){
            lock.lock();
            while(!pending&&!exiting)ready.wait(lock);
            if(exiting){lock.unlock();break;}
            unsigned char*t=renderPacked;renderPacked=pendingPacked;pendingPacked=t;
            pending=false;lock.unlock();
            size_t n=(size_t)pitch*height;
            for(int y=0;y<height;y+=2){
                const unsigned char*row=renderPacked+(size_t)y*pitch;
                unsigned char*a=planar+(size_t)y*pitch;unsigned char*b=a+pitch;
                for(int x=0;x<pitch;++x){a[x]=row[2*x];b[x]=row[2*x+1];}
            }
            for(size_t i=0;i<n/4;++i){planar[n+i]=renderPacked[n+2*i];planar[n+n/4+i]=renderPacked[n+2*i+1];}
            real->convert(planar,rgb);
            surface->deliver(rgb,dstWidth,dstHeight,dstWidth);
            if(++displayed==1||displayed%120==0)LOGI("Presented DSP frame %u (received %u)",displayed,frames);
        }
    }
public:
    static PVColor*NewL();
    RK28Convert565(PVColor*p,LegacySurface*s):real(p),pitch(0),height(0),dstWidth(0),dstHeight(0),frames(0),displayed(0),
        mapStart(0),mapPhys(0),mapSize(0),bufferSize(0),pendingPacked(NULL),renderPacked(NULL),planar(NULL),rgb(NULL),
        surface(s),started(false),exiting(false),pending(false){}
    virtual ~RK28Convert565(){
        if(started){{Mutex::Autolock l(lock);exiting=true;ready.signal();}pthread_join(worker,NULL);}
        free(pendingPacked);free(renderPacked);free(planar);free(rgb);delete real;
    }
    virtual short capability(){return real->capability();}
    virtual int init(int w,int h,int p,int dw,int dh,int dp,int r){
        pitch=p;height=h;dstWidth=dw;dstHeight=dh;
        return real->init(w,h,p,dw,dh,dp,r);
    }
    virtual void memheight(int h){height=h;real->memheight(h);}
    virtual int mode(int m){return real->mode(m);}
    virtual int convert(unsigned char*s,unsigned char*d){
        uint32_t phys;memcpy(&phys,s,sizeof(phys));size_t n=(size_t)pitch*height;
        if(surface.get()&&phys>=0x60000000&&phys<0x80000000){
            if(!mapStart||phys<mapPhys||(size_t)(phys-mapPhys)+n*3/2>mapSize){
                FILE*f=fopen("/proc/self/maps","r");char line[256];mapStart=0;
                while(f&&fgets(line,sizeof(line),f)){
                    unsigned start,end,offset;char perms[8];
                    if(strstr(line,"/dev/pmem-dsp")&&sscanf(line,"%x-%x %7s %x",&start,&end,perms,&offset)==4
                       &&phys>=offset&&(size_t)(phys-offset)+n*3/2<=end-start){
                        mapStart=start;mapPhys=offset;mapSize=end-start;break;
                    }
                }
                if(f)fclose(f);
            }
            if(!mapStart){LOGE("Unmapped DSP frame %08x",phys);return 0;}
            if(!started){
                bufferSize=n*3/2;pendingPacked=(unsigned char*)malloc(bufferSize);renderPacked=(unsigned char*)malloc(bufferSize);
                planar=(unsigned char*)malloc(bufferSize);rgb=(unsigned char*)malloc((size_t)pitch*height*2);
                if(!pendingPacked||!renderPacked||!planar||!rgb)return 0;
                if(pthread_create(&worker,NULL,run,this)!=0)return 0;
                started=true;surface->setAsynchronous();
            }
            // Copy while the decoder owns this frame. The rendering worker
            // keeps only the newest pending frame, so slow CPU composition
            // cannot block the PV scheduler or stretch the playback timeline.
            Mutex::Autolock l(lock);
            memcpy(pendingPacked,(const void*)(mapStart+phys-mapPhys),bufferSize);
            pending=true;++frames;ready.signal();return 1;
        }
        return real->convert(s,d);
    }
    virtual int size(){return real->size();}
    virtual int range(bool r){return real->range(r);}
};
PVColor*RK28Convert565::NewL(){
    typedef PVColor*(*Factory)();
    Factory f=(Factory)dlsym(RTLD_DEFAULT,"_ZN14ColorConvert164NewLEv");
    pthread_once(&surfaceKeyOnce,makeSurfaceKey);
    return f?new RK28Convert565(f(),(LegacySurface*)pthread_getspecific(surfaceKey)):NULL;
}
