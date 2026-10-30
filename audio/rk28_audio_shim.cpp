/*
 * Gingerbread-era libmedia symbols required by the prebuilt RK2818 libaudio.so.
 *
 * libaudio.so was built against GB's libmedia, which exported
 * android::AudioSystem::popCount / isInputDevice and android::AudioParameter.
 * ICS dropped both from libmedia.so, so the vendor HAL fails to load unless
 * they are provided.  This library is preloaded with RTLD_GLOBAL before the
 * vendor blob is dlopen()ed, which makes the symbols resolvable.
 */
#include <stdint.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>

namespace android {

class AudioSystem {
public:
    enum audio_devices {
        DEVICE_NONE = 0,
        AUDIO_DEVICE_BIT_IN = (int)0x80000000
    };

    static uint32_t popCount(uint32_t u);
    static bool isInputDevice(audio_devices device);
};

uint32_t AudioSystem::popCount(uint32_t u)
{
    return (uint32_t)__builtin_popcount(u);
}

bool AudioSystem::isInputDevice(audio_devices device)
{
    return ((uint32_t)device & 0x80000000u) != 0;
}

}  // namespace android

/*
 * libhardware_legacy used to provide these; the vendored libaudio.so now takes
 * this library as its dependency instead, so it has to carry them too.
 */
extern "C" int acquire_wake_lock(int lock, const char *id)
{
    (void)lock;
    int fd = open("/sys/power/wake_lock", O_WRONLY);
    if (fd < 0) return -1;
    ssize_t n = write(fd, id, strlen(id));
    close(fd);
    return n < 0 ? -1 : 0;
}

extern "C" int release_wake_lock(const char *id)
{
    int fd = open("/sys/power/wake_unlock", O_WRONLY);
    if (fd < 0) return -1;
    ssize_t n = write(fd, id, strlen(id));
    close(fd);
    return n < 0 ? -1 : 0;
}
