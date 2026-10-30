/*
 * Minimal playback check: pushes a sine through AudioTrack so the audio HAL,
 * the mixer and the codec can be verified without any UI player.
 *
 *   audio_track_test [seconds] [frequency]
 */
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <unistd.h>

#include <media/AudioTrack.h>
#include <system/audio.h>
#include <utils/Log.h>

using namespace android;

int main(int argc, char **argv)
{
    int seconds = (argc > 1) ? atoi(argv[1]) : 3;
    double freq = (argc > 2) ? atof(argv[2]) : 440.0;

    if (seconds <= 0) seconds = 3;

    // 0 lets AudioTrack pick the HAL's native sample rate/format/mask.
    AudioTrack track((int)AUDIO_STREAM_MUSIC, 0, 0, 0, (int)0);

    status_t status = track.initCheck();
    if (status != NO_ERROR) {
        fprintf(stderr, "AudioTrack init failed: %d\n", status);
        return 1;
    }

    track.setVolume(1.0f, 1.0f);

    track.start();

    const int rate = track.getSampleRate() > 0 ? track.getSampleRate() : 48000;
    const int framesPerBuffer = 1024;
    short buffer[framesPerBuffer * 2];
    long totalFrames = (long)seconds * rate;
    long written = 0;
    long phase = 0;

    while (written < totalFrames) {
        int frames = framesPerBuffer;
        if (totalFrames - written < frames) frames = (int)(totalFrames - written);
        for (int i = 0; i < frames; i++) {
            short v = (short)(12000.0 * sin(2.0 * M_PI * freq * (double)(phase + i) / (double)rate));
            buffer[i * 2] = v;
            buffer[i * 2 + 1] = v;
        }
        ssize_t n = track.write(buffer, frames * 4);
        if (n < 0) {
            fprintf(stderr, "write failed: %zd\n", n);
            break;
        }
        phase += frames;
        written += frames;
    }

    track.stop();
    printf("played %ld frames (%d s, %.0f Hz)\n", written, seconds, freq);
    return 0;
}
