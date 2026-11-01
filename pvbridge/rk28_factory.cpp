/* RK2818 hardware video backend for the Android 4.2 player factory. */
#define LOG_TAG "RK28PVFactory"
#include "MediaPlayerFactory.h"
#include <cutils/properties.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/FileSource.h>
#include <media/stagefright/MediaExtractor.h>
#include <media/stagefright/MetaData.h>
#include <utils/Log.h>
#include <unistd.h>
#include <string.h>
namespace android {
MediaPlayerBase* createRK28PVPlayer();
static bool enabled() {
    char value[PROPERTY_VALUE_MAX];
    property_get("media.rk28.pvplayer", value, "1");
    return strcmp(value, "0") != 0;
}
// FileSource owns its descriptor; probing uses a dup and shares the file offset.
// Restore it after probing so the stock PV parser sees the original position.
class SavedFilePosition {
    int mFd;
    off64_t mPosition;
public:
    explicit SavedFilePosition(int fd): mFd(fd), mPosition(lseek64(fd, 0, SEEK_CUR)) {}
    ~SavedFilePosition() { if (mPosition >= 0) lseek64(mFd, mPosition, SEEK_SET); }
};
class RK28PVFactory : public MediaPlayerFactory::IFactory {
public:
    virtual float scoreFactory(const sp<IMediaPlayer>&, int fd,
            int64_t offset, int64_t length, float currentScore) {
        if (!enabled() || currentScore >= 0.9f) return 0;
        unsigned char header[16] = {0};
        ssize_t n = pread(fd, header, sizeof(header), offset);
        bool iso = n >= 8 && !memcmp(header + 4, "ftyp", 4);
        bool videoContainer = iso ||
            (n >= 12 && !memcmp(header, "RIFF", 4) && !memcmp(header + 8, "AVI ", 4)) ||
            (n >= 4 && (!memcmp(header, "\x1a\x45\xdf\xa3", 4) ||
                       !memcmp(header, "FLV", 3) || !memcmp(header, ".RMF", 4) ||
                       !memcmp(header, "\x30\x26\xb2\x75", 4)));
        if (!videoContainer) return 0;
        // M4A and video MP4 share ftyp. Keep audio-only tracks on Stagefright.
        if (iso) {
            SavedFilePosition savedPosition(fd);
            DataSource::RegisterDefaultSniffers();
            // FileSource closes its fd on destruction. Keep the descriptor
            // supplied by MediaPlayerService alive for the selected player.
            int probeFd = dup(fd);
            if (probeFd < 0) return 0;
            sp<DataSource> source = new FileSource(probeFd, offset, length);
            sp<MediaExtractor> extractor = MediaExtractor::Create(source);
            if (extractor != NULL) {
                for (size_t i=0; i<extractor->countTracks(); ++i) {
                    const char* mime = NULL;
                    sp<MetaData> meta = extractor->getTrackMetaData(i);
                    if (meta != NULL && meta->findCString(kKeyMIMEType, &mime) &&
                            !strncasecmp(mime, "video/", 6)) return 0.9f;
                }
                return 0;
            }
        }
        return 0.9f;
    }
    virtual float scoreFactory(const sp<IMediaPlayer>&, const char* url,
            float currentScore) {
        if (!enabled() || currentScore >= 0.9f ||
                (url[0] != '/' && strncmp(url, "file://", 7))) return 0;
        const char* dot = strrchr(url, '.');
        static const char* extensions[] = {".mp4", ".m4v", ".3gp", ".avi",
            ".mkv", ".flv", ".rm", ".rmvb", ".wmv", ".asf"};
        for (size_t i=0; dot && i<sizeof(extensions)/sizeof(extensions[0]); ++i)
            if (!strcasecmp(dot, extensions[i])) return 0.9f;
        return 0;
    }
    virtual sp<MediaPlayerBase> createPlayer() {
        ALOGI("Create RK2818 OpenCORE DSP player");
        return createRK28PVPlayer();
    }
};
MediaPlayerFactory::IFactory* createRK28PVFactory() {
    return new RK28PVFactory();
}
}
