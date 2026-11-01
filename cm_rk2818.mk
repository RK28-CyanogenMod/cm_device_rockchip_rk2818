# Inherit AOSP device configuration for RK2818.
$(call inherit-product, device/rockchip/rk2818/full_rk2818.mk)

# Use the RK2818 tablet navigation/status bar resources.
PRODUCT_PACKAGE_OVERLAYS += device/rockchip/rk2818/overlay

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cm/config/common.mk)

# Include GSM stuff


# Include extra dictionaries for LatinIME

#
# Setup device specific product configuration.
#
PRODUCT_NAME := cm_rk2818
PRODUCT_BRAND := Rockchip
PRODUCT_DEVICE := rk2818
PRODUCT_MANUFACTURER := Rockchip
PRODUCT_MODEL := RK2818
PRODUCT_CHARACTERISTICS := tablet,nosdcard
# Dalvik heap is configured in system.prop instead: the tablet defaults
# (48m growth limit, 256m max) are far too generous for 200 MB of RAM.
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=rk2818 BUILD_ID=JDQ39E BUILD_FINGERPRINT=rockchip/rk2818/rk2818:4.2.2/JDQ39E/20261030:eng/test-keys PRIVATE_BUILD_DESC="rk2818-eng 4.2.2 JDQ39E 20261030 test-keys" BUILD_NUMBER=20261030

# Extra RK2818 overlay

#
# Set ro.modversion
#
ifdef CYANOGEN_NIGHTLY
    PRODUCT_PROPERTY_OVERRIDES += \
            ro.modversion=CyanogenMod-10.1-$(shell date +%m%d%Y)-NIGHTLY-RK2818
else
    ifdef CYANOGEN_RELEASE
        PRODUCT_PROPERTY_OVERRIDES += \
            ro.modversion=CyanogenMod-10.1.3-RK2818
    else
        PRODUCT_PROPERTY_OVERRIDES += \
            ro.modversion=CyanogenMod-10.1.3-RK2818-KANG
    endif
endif
    
#
# Copy RK2818 specific prebuilt files
#
PRODUCT_COPY_FILES +=  \

# System sounds.  full_base.mk still inherits frameworks/base/data/sounds/
# AllAudio.mk, but CM9 renamed that file to NewAudio.mk, so the inherits
# silently packaged nothing and /system/media/audio was missing entirely.
$(call inherit-product, frameworks/base/data/sounds/NewAudio.mk)

# CyanogenMod's own alarm/notification/ringtone set.  common.mk does not pull
# it in (only the common_full_* products do), so add it explicitly.
$(call inherit-product, vendor/cm/config/cm_audio.mk)


# CM9's Trebuchet is the intended default launcher; the AOSP Home sample is removed above.
PRODUCT_PACKAGES += Trebuchet

# MediaScanner uses these filenames to initialize sounds on a clean install.
PRODUCT_PROPERTY_OVERRIDES += \
    ro.config.ringtone=CyanTone.ogg \
    ro.config.notification_sound=CyanMessage.ogg \
    ro.config.alarm_alert=CyanAlarm.ogg
