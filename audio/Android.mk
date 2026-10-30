LOCAL_PATH := $(call my-dir)

# GB-era libmedia symbols (AudioSystem helpers + AudioParameter) required by the
# prebuilt vendor libaudio.so.  Preloaded RTLD_GLOBAL by the HAL module below.
include $(CLEAR_VARS)
LOCAL_MODULE := librk28audioshim
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := \
    rk28_audio_shim.cpp \
    ../../../../frameworks/base/media/libmedia/AudioParameter.cpp
LOCAL_SHARED_LIBRARIES := liblog libcutils libutils
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
include $(BUILD_SHARED_LIBRARY)

# ICS audio HAL wrapping the RK2818 vendor libaudio.so
include $(CLEAR_VARS)
LOCAL_MODULE := audio.primary.rk28board
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/hw
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := \
    audio_hw_hal.cpp \
    rk28_audio_loader.cpp
# The vendored HAL speaks Gingerbread channel masks (stereo = 0xc); ICS uses
# 0x3.  This makes the legacy bridge translate masks in both directions.
LOCAL_CFLAGS += -DUSES_AUDIO_LEGACY
LOCAL_C_INCLUDES := hardware/libhardware_legacy/include
LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libmedia \
    libhardware \
    libhardware_legacy \
    liblog \
    libdl
include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))
