# Inherit AOSP device configuration for RK2818.
$(call inherit-product, device/rockchip/rk2818/full_rk2818.mk)

# Use the RK2818 tablet navigation/status bar resources.
PRODUCT_PACKAGE_OVERLAYS += device/rockchip/rk2818/overlay

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cm/config/common.mk)

# Include GSM stuff
$(call inherit-product, vendor/cm/config/gsm.mk)

# Include extra dictionaries for LatinIME

#
# Setup device specific product configuration.
#
PRODUCT_NAME := cyanogen_rk2818
PRODUCT_BRAND := Rockchip
PRODUCT_DEVICE := rk2818
PRODUCT_MANUFACTURER := Rockchip
PRODUCT_MODEL := RK2818
PRODUCT_CHARACTERISTICS := tablet,nosdcard
$(call inherit-product, frameworks/base/build/tablet-dalvik-heap.mk)
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=rk2818 BUILD_ID=IMM76L BUILD_DISPLAY_ID=CM9-RK2818-Skyler1n BUILD_FINGERPRINT=rockchip/rk2818/rk2818:4.0.4/IMM76L/20261029:eng/test-keys PRIVATE_BUILD_DESC="rk2818-eng 4.0.4 IMM76L 20261029 test-keys" BUILD_NUMBER=20261029

# Extra RK2818 overlay

#
# Set ro.modversion
#
ifdef CYANOGEN_NIGHTLY
    PRODUCT_PROPERTY_OVERRIDES += \
            ro.modversion=CyanogenMod-9-$(shell date +%m%d%Y)-NIGHTLY-RK2818
else
    ifdef CYANOGEN_RELEASE
        PRODUCT_PROPERTY_OVERRIDES += \
            ro.modversion=CyanogenMod-9.1.0-RK2818
    else
        PRODUCT_PROPERTY_OVERRIDES += \
            ro.modversion=CyanogenMod-9.1.0-RK2818-KANG
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
