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

A rendering worker burst-copies frames before the DSP recycles them, converts
colors, then writes RGB565 to an ICS SurfaceTexture. Only the newest pending
frame is retained. This keeps audio/video time synchronized when CPU display
composition is slower than DSP decoding. The baseline is intentionally a
fallback: it still uses CPU conversion/scaling and does not reproduce the
original hardware overlay display path.

## Verified on 2026-10-30

- N5PRO2, stock 2.6.25 kernel, ARMv5TE, local USB ADB.
- Actual Gallery MovieActivity playback, with working audio and changing video.
- H.264 at 426x240 and 640x360; corrected diagonal corruption and frozen frame.
- 640x360 received 502 frames in about 20 seconds, displayed about 6 fps.
- User confirmed synchronized audio/video with visible frame dropping.

Other vendor codecs, long playback, seek, suspend, and hardware overlay still
need separate device validation. No kernel, recovery, or boot changes required.
