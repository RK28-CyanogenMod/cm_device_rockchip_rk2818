#!/system/bin/sh
#
# RK2818 has a dedicated "swap" MTD partition (mtdblock10, 64 MB).  Enable it,
# formatting it first when it has no swap signature yet (fresh flash).
#
# init runs this from "on boot", which can happen before ueventd has created
# the block device node, so wait for it first.
#
# No "-p": /system/xbin/swapon is busybox and has no priority option; with a
# single swap device the kernel default applies anyway.
#
export PATH=/system/bin:/system/xbin:/sbin

DEV=/dev/block/mtdblock10

i=0
while [ ! -b "$DEV" ] && [ $i -lt 120 ]; do
    sleep 1
    i=$((i + 1))
done

if swapon "$DEV" 2>/dev/null; then
    exit 0
fi

/system/xbin/mkswap "$DEV" >/dev/null 2>&1
swapon "$DEV"
