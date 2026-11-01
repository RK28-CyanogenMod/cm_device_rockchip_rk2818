LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# Match CM9/ICS device-parts applications (as used by Blade ICS).
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := RockParts

# Apps this product does not ship.
#
# build/core/main.mk removes every module named in PACKAGES.<module>.OVERRIDES
# from modules_to_install, and _get-package-overrides() only considers real
# .apk packages, so the list has to hang off a package the device tree always
# installs.  A PRODUCT_PACKAGES filter-out cannot be used here: inherit-product
# only records a tag, and the product variables are merged after every product
# fragment has been parsed, leaving PRODUCT_PACKAGES empty at that point.
LOCAL_OVERRIDES_PACKAGES := \
    Gallery2 \
    Development \
    SpareParts \
    VideoEditor \
    VoiceDialer \
    HoloSpiralWallpaper \
    MagicSmokeWallpapers \
    NoiseField \
    Galaxy4 \
    LiveWallpapers \
    LiveWallpapersPicker \
    VisualizationWallpapers \
    PhaseBeam \
    DSPManager \
    CMUpdater \
    Home \
    RockAudioPlayer \
    Music \
    Contacts \
    SpeechRecorder \
    Email \
    Exchange \
    Stk
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
