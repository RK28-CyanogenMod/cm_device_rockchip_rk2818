LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := audio_track_test
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := audio_track_test.cpp
LOCAL_C_INCLUDES := \
    $(call include-path-for, frameworks-base) \
    system/core/include
LOCAL_SHARED_LIBRARIES := libmedia libutils libbinder libcutils liblog libdl
LOCAL_MODULE_PATH := $(TARGET_OUT_EXECUTABLES)
include $(BUILD_EXECUTABLE)
