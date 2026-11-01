LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := lib_driver_cmd_rk28
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := driver_cmd_wext.c
LOCAL_CFLAGS := -DANDROID
LOCAL_C_INCLUDES := external/wpa_supplicant_8/src external/wpa_supplicant_8/src/utils external/wpa_supplicant_8/src/drivers
include $(BUILD_STATIC_LIBRARY)
