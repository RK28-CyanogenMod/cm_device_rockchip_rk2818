# RK2818 physical storage on Android 4.2

- NAND is primary (`/mnt/nand`); TF is secondary (`/mnt/sdcard`).
- `/sdcard` and `/storage/sdcard0` point to NAND; `/storage/sdcard1` points to TF.
- `BOARD_VOLD_RK28_STORAGE` enables the vold compatibility code: direct-volume
  owner access and the bind mount from TF to `/mnt/nand/external_sd`.
- The nested alias is detached before either physical volume is unmounted or
  exported through UMS. An existing non-empty external_sd directory is preserved,
  and the alias is not mounted over it.
- Native MediaScanner's existing skip-list property avoids duplicate indexing
  through the alias; Settings excludes the alias from NAND miscellaneous usage.
- `mount none none / shared rec` must execute before Zygote. Without shared
  propagation, freshly booted applications see the cramfs directories underneath
  the later vold mounts, and report the ramdisk's 3.61 MiB for both devices.
- This board boots straight from read-only cramfs. The project packaging script
  must create `/storage` in the ramdisk and replace the reference `/sdcard` link
  before mkcramfs. init.rc cannot create those paths on a read-only root.

The private tmpfs at /mnt/secure is required alongside the shared root.
Linux rejects MS_MOVE from a shared parent; vold stages volumes there before
moving them to /mnt/nand or /mnt/sdcard. Keeping the staging parent private
allows mounting, unmounting and UMS reconnects while the public mounts still
propagate to Zygote/app namespaces. Do not mark the public /mnt volumes private.

The on-boot propagation refresh must be non-recursive, followed by marking
/mnt/secure recursively private. Recursively sharing / again re-shares staging
and causes MS_MOVE to fail with EINVAL; neither physical volume then mounts.
