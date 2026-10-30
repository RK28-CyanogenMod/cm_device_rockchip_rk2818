/* RK2818 OpenCORE/ICS ABI boundary. Only C types cross this interface. */
#ifndef RK28_PV_BRIDGE_H
#define RK28_PV_BRIDGE_H
#include <stdint.h>
#include <stddef.h>
#include <sys/types.h>
#ifdef __cplusplus
extern "C" {
#endif
typedef size_t (*RK28AudioPull)(void*,void*,size_t);
typedef struct {
    void (*notify)(void*,int,int,int);
    int (*audio_open)(void*,uint32_t,int,int,int,void*,RK28AudioPull);
    ssize_t (*audio_write)(void*,const void*,size_t);
    void (*audio_command)(void*,int);
    int (*audio_position)(void*,uint32_t*);
    int (*audio_info)(void*,int);
    void (*video_frame)(void*,const void*,int,int,int,int);
} RK28PVCallbacks;
enum { RK28_AUDIO_START, RK28_AUDIO_STOP, RK28_AUDIO_FLUSH, RK28_AUDIO_PAUSE, RK28_AUDIO_CLOSE };
enum { RK28_AUDIO_READY, RK28_AUDIO_BUFFER_SIZE, RK28_AUDIO_FRAME_COUNT, RK28_AUDIO_CHANNELS,
       RK28_AUDIO_FRAME_SIZE, RK28_AUDIO_LATENCY, RK28_AUDIO_SESSION_ID };
enum { RK28_PV_PREPARE, RK28_PV_PREPARE_ASYNC, RK28_PV_START, RK28_PV_STOP,
       RK28_PV_PAUSE, RK28_PV_PLAYING, RK28_PV_SEEK, RK28_PV_POSITION,
       RK28_PV_DURATION, RK28_PV_RESET, RK28_PV_LOOP };
void* rk28_pv_create(const RK28PVCallbacks*,void*);
void rk28_pv_destroy(void*);
int rk28_pv_source_url(void*,const char*,const void*);
int rk28_pv_source_fd(void*,int,int64_t,int64_t);
int rk28_pv_command(void*,int,int*);
int rk28_pv_invoke(void*,const void*,void*);
int rk28_pv_metadata(void*,const void*,void*);
#ifdef __cplusplus
}
#endif
#endif
