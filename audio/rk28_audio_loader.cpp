/*
 * Jelly Bean audio HAL module for RK2818.
 *
 * The RK2818 audio driver code lives in Rockchip's prebuilt libaudio.so, which
 * implements Gingerbread's AudioHardwareInterface (createAudioHardware()).
 * Android 4.0 no longer loads that library itself, so this module dlopen()s it
 * and lets libaudiohw_legacy's audio_hw_hal.cpp bridge expose it as an Android 4.2
 * audio_hw_device.
 */
#define LOG_TAG "rk28_audio"
#include <utils/Log.h>
#include <dlfcn.h>

// Keep the GB vendor vtable layout independent of platform include ordering.
#include "legacy/hardware_legacy/AudioHardwareInterface.h"

namespace android_audio_legacy {

typedef void *(*CreateAudioHardwareFn)(void);

static AudioHardwareInterface *loadVendorHal()
{
    static const char *kShimPaths[] = {
        "/system/lib/librk28audioshim.so",
    };

    for (size_t i = 0; i < sizeof(kShimPaths) / sizeof(kShimPaths[0]); i++) {
        void *shim = dlopen(kShimPaths[i], RTLD_NOW | RTLD_GLOBAL);
        if (shim != NULL) {
            LOGI("preloaded %s", kShimPaths[i]);
            break;
        }
        LOGE("cannot preload %s: %s", kShimPaths[i], dlerror());
    }

    static const char *kLibAudioPaths[] = {
        "/system/lib/libaudio.so",
    };

    void *handle = NULL;
    for (size_t i = 0; i < sizeof(kLibAudioPaths) / sizeof(kLibAudioPaths[0]); i++) {
        handle = dlopen(kLibAudioPaths[i], RTLD_NOW | RTLD_GLOBAL);
        if (handle != NULL) {
            LOGI("loaded vendor audio HAL from %s", kLibAudioPaths[i]);
            break;
        }
        LOGE("dlopen %s failed: %s", kLibAudioPaths[i], dlerror());
    }
    if (handle == NULL)
        return NULL;

    CreateAudioHardwareFn create =
            (CreateAudioHardwareFn)dlsym(handle, "createAudioHardware");
    if (create == NULL) {
        LOGE("dlsym createAudioHardware failed: %s", dlerror());
        return NULL;
    }

    AudioHardwareInterface *hw =
            reinterpret_cast<AudioHardwareInterface *>(create());
    LOGI("vendor audio HAL %s", hw ? "created" : "returned NULL");
    return hw;
}

}  // namespace android_audio_legacy

extern "C" android_audio_legacy::AudioHardwareInterface *createAudioHardware(void)
{
    return android_audio_legacy::loadVendorHal();
}
