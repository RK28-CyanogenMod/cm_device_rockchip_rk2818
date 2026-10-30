$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base.mk)

# The gps config appropriate for this device
$(call inherit-product, device/common/gps/gps_us_supl.mk)

$(call inherit-product-if-exists, vendor/rockchip/rk2818/device-vendor.mk)


# Place inital files
PRODUCT_COPY_FILES += \
    device/rockchip/rk2818/ramdisk/init.rc:root/init.rc \
    device/rockchip/rk2818/ramdisk/initlogo.rle:root/initlogo.rle \
    device/rockchip/rk2818/ramdisk/ueventd.rk28board.rc:root/ueventd.rk28board.rc \
    device/rockchip/rk2818/kernel/modules/aufs.ko:root/aufs.ko \
    device/rockchip/rk2818/ramdisk/xmode/clean_cache.sh:root/xmode/clean_cache.sh \
    device/rockchip/rk2818/ramdisk/xmode/readonly.sh:root/xmode/readonly.sh \
    device/rockchip/rk2818/ramdisk/xmode/writeable.sh:root/xmode/writeable.sh 

# Place etc files
PRODUCT_COPY_FILES += \
    device/rockchip/rk2818/init.d/10pointercal:system/etc/init.d/10pointercal \
    device/rockchip/rk2818/etc/dhcpcd.conf:system/etc/dhcpcd/dhcpcd.conf \
    device/rockchip/rk2818/etc/vold.fstab:system/etc/vold.fstab \
    device/rockchip/rk2818/etc/wpa_supplicant.conf:system/etc/wifi/wpa_supplicant.conf

# Place modules files
PRODUCT_COPY_FILES += \
    device/rockchip/rk2818/kernel/modules/cifs.ko:system/lib/modules/cifs.ko
	
# Place permission files
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/android.hardware.camera.front.xml:system/etc/permissions/android.hardware.camera.front.xml \
    frameworks/base/data/etc/android.hardware.location.gps.xml:system/etc/permissions/android.hardware.location.gps.xml \
    frameworks/base/data/etc/android.hardware.sensor.accelerometer.xml:system/etc/permissions/android.hardware.sensor.accelerometer.xml \
    frameworks/base/data/etc/android.hardware.telephony.gsm.xml:system/etc/permissions/android.hardware.telephony.gsm.xml \
    frameworks/base/data/etc/android.hardware.touchscreen.multitouch.jazzhand.xml:system/etc/permissions/android.hardware.touchscreen.multitouch.jazzhand.xml \
    frameworks/base/data/etc/android.hardware.wifi.xml:system/etc/permissions/android.hardware.wifi.xml \
    frameworks/base/data/etc/android.software.sip.voip.xml:system/etc/permissions/android.software.sip.voip.xml \
    frameworks/base/data/etc/handheld_core_hardware.xml:system/etc/permissions/handheld_core_hardware.xml

# Keylayout setup
PRODUCT_COPY_FILES += \
    device/rockchip/rk2818/keylayout/qwerty.kcm.bin:system/usr/keychars/qwerty.kcm.bin \
    device/rockchip/rk2818/keylayout/qwerty2.kcm.bin:system/usr/keychars/qwerty2.kcm.bin \
    device/rockchip/rk2818/keylayout/AVRCP.kl:system/usr/keylayout/AVRCP.kl \
    device/rockchip/rk2818/keylayout/qwerty.kl:system/usr/keylayout/qwerty.kl \
    device/rockchip/rk2818/keylayout/rk28_AD_button.kl:system/usr/keylayout/rk28_AD_button.kl

# Input device configuration for the legacy XPT2046 touchscreen.
PRODUCT_COPY_FILES += \
    device/rockchip/rk2818/idc/xpt2046_touchscreen.idc:system/usr/idc/xpt2046_touchscreen.idc

# Enables the dedicated swap partition at boot (called from init.rc).
PRODUCT_COPY_FILES += \
    device/rockchip/rk2818/bin/swapsetup.sh:system/bin/swapsetup.sh

PRODUCT_PACKAGES += \
    Gallery \
    RockParts \
    RKTSCalibration \
    audio.primary.rk28board \
    librk28audioshim

ifeq ($(TARGET_PREBUILT_KERNEL),)
	LOCAL_KERNEL := device/rockchip/rk2818/kernel/kernel.img
else
	LOCAL_KERNEL := $(TARGET_PREBUILT_KERNEL)
endif

PRODUCT_COPY_FILES += \
    $(LOCAL_KERNEL):kernel

PRODUCT_BUILD_PROP_OVERRIDES += BUILD_UTC_DATE=8
PRODUCT_NAME := full_rk2818
PRODUCT_DEVICE := rk2818

# Vendor OpenCORE hardware video playback, adapted to the ICS media service.
PRODUCT_PACKAGES += librk28pvplayer librk28pvlegacy libopencore_player_rk28
