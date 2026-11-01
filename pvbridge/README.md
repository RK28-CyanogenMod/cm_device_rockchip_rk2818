# RK2818 OpenCORE compatibility player (ICS)

This board-specific backend adapts the original PVPlayer and DSP codecs to the
ICS MediaPlayerService. It does not implement a standard Stagefright OMX codec.
Local video containers opt into PVPlayer; `media.rk28.pvplayer=0` disables it.

The vendor `libopencore_player.so` is kept unchanged in the vendor repository.
`patch_vendor.py` generates its installed version, replacing equal-length ABI
symbols and a direct library dependency, then rebuilding the ELF SysV hash.
The vendor copy rule for system/lib must be excluded to avoid overwriting it.

`pv_legacy.cpp` uses GB class declarations and a 52-byte heap implementation.
Its C++ player class names differ from the ICS classes to avoid symbol/vtable
collisions. Only the C callbacks in rk28_pv_bridge.h cross the ABI boundary.

DSP output buffers contain the current frame's physical address in their first
word. Only the first two contain preview pixels. The converter resolves that
address inside the decoder's existing PMEM mappings; it never allocates or
remaps DSP memory. The hardware layout interleaves pairs of luma rows and UV.
The RGB converter's destination pitch is display width, not coded width.

The default display path reuses `overlay.rk28board.so` and submits the DSP
physical address directly in format 21. The HAL sends Y/UV addresses to fb1
(win0fb) with ioctl 0x5002; fb0 (win1fb) remains the UI plane. One black
RGB565 SurfaceTexture buffer provides the original display color-key hole.
No CPU conversion, copying or per-frame SurfaceFlinger composition is needed.
Only one player may own this hardware plane at a time.

The RK2818-only SurfaceFlinger queries in `gui/RK28Overlay.h` publish a locked
snapshot of the video layer bounds, transform and visibility. The adapter
polls at 10 Hz, including while paused, to move/blank the overlay without
recomposing video frames. Rotated/offscreen surfaces and unsupported producers
use the RGB fallback. `media.rk28.overlay=0` forces that fallback.

A fallback rendering worker burst-copies frames before the DSP recycles them, converts
colors, then writes RGB565 to an ICS SurfaceTexture. Only the newest pending
frame is retained. This keeps audio/video time synchronized when CPU display
composition is slower than DSP decoding. The committed baseline used only this
fallback; the overlay work is layered on top of that recoverable checkpoint.

## Verified on 2026-10-30

- N5PRO2, stock 2.6.25 kernel, ARMv5TE, local USB ADB.
- Actual Gallery MovieActivity playback, with working audio and changing video.
- H.264 at 426x240 and 640x360; corrected diagonal corruption and frozen frame.
- 640x360 received 502 frames in about 20 seconds, displayed about 6 fps.
- User confirmed synchronized audio/video with visible frame dropping.

- Direct overlay with the same 640x360 H.264 clip submitted about 24 fps.
- User confirmed normal colors and visibly smoother motion with hardware overlay.
- Stop/back and reopening released/recreated the video plane successfully.
- Native integration check passed: play, pause (7375 ms remained 7375 ms),
  move/resize while paused (80,40 640x360 -> 200,100 400x225), hide/show via
  FBIOBLANK, resume, seek to 30000 ms (reported 32153 ms after playback), reset.

Other vendor codecs, long playback, seek, suspend, rotations and partially
occluded windows still need separate device validation. The display controller's
original black color key retains its limits with translucent/dark UI; video
pixels also do not appear in ordinary framebuffer screenshots.
No kernel, recovery, or boot changes required.

Local baseline commits (ics, not pushed): device `91ebcd4`, frameworks/base
`594d12ebff`, vendor `112d681`. Overlay changes remain separate working changes.

## Android 4.2 / CM10.1 port (2026-10-31)

The 4.2 build registers a MediaPlayerFactory backend and uses the fence-aware
ANativeWindow queue APIs. SurfaceTextureLayer publishes the same private
geometry queries through its BufferQueue. Audio-only MP4/M4A stays on the
Stagefright path. Auxiliary stock PV libraries retain a DT_NEEDED entry for
libsurfaceflinger_client.so without importing client symbols, so the device
build supplies an empty dependency library; all actual display access goes
through the rebuilt bridge and SurfaceFlinger.

The ICS tests above describe CM9; CM10.1 runtime results are recorded separately
in the project media validation report. A successful build alone does not verify
video playback on the device.

### CM10.1 device verification, 2026-10-31

The factory probes MP4 tracks with a duplicated fd. FileSource owns and closes
that duplicate; SavedFilePosition restores the shared file offset after probing.
Passing the service-owned fd directly used to close the real source before the
vendor player duplicated it (sharedfd://-1), causing PVMFErrNotSupported.

- Standard MediaPlayerService fd playback of H.264 426x240 passed prepare/start,
  pause with unchanged position, move/resize while paused, hide/show, resume,
  seek and reset after restoring the original uninstrumented vendor common lib.
- RockVideo used the normal Java MediaPlayer API with a MediaStore content URI
  for the 640x360 H.264 clip. DSP overlay output averaged about 23.5-24 fps.
- The user confirmed normal, smooth video with synchronized audio.
- This is a system MediaPlayer backend, not a public MediaCodec decoder.
  Apps that bundle their own engines must select their system-player backend.
