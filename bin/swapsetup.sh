#!/system/bin/sh
#
# RK2818 has a dedicated "swap" MTD partition (mtdblock10, 64 MB).  Enable it,
# formatting it first when it has no swap signature yet (fresh flash).
#
# No "-p 100": /system/xbin/swapon is busybox, which does not implement the
# priority option.  With a single swap device the kernel's default is used and
# the priority has no effect anyway.
#
DEV=/dev/block/mtdblock10

if swapon "$DEV" 2>/dev/null; then
    exit 0
fi

/system/xbin/mkswap "$DEV" >/dev/null 2>&1
swapon "$DEV"
