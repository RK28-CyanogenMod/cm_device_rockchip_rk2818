LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := librk28pvlegacy
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := pv_legacy.cpp
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -DBINDER_COMPAT
LOCAL_C_INCLUDES := $(LOCAL_PATH)/legacy frameworks/av/include frameworks/native/include system/core/include hardware/libhardware/include
LOCAL_SHARED_LIBRARIES := libmedia libutils libbinder libcutils liblog libdl libui
LOCAL_REQUIRED_MODULES := libopencore_player_rk28 libsurfaceflinger_client
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := librk28pvplayer
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := pv_ics.cpp rk28_factory.cpp
LOCAL_C_INCLUDES := hardware/libhardware/include frameworks/av/include frameworks/native/include system/core/include frameworks/av/media/libmediaplayerservice
LOCAL_SHARED_LIBRARIES := librk28pvlegacy libstagefright libmedia libutils libbinder libcutils liblog libgui libui libdl
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libopencore_player_rk28
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_STEM := libopencore_player
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
include $(BUILD_SYSTEM)/base_rules.mk
$(LOCAL_BUILT_MODULE): PRIVATE_PATCHER := $(LOCAL_PATH)/patch_vendor.py
$(LOCAL_BUILT_MODULE): vendor/rockchip/rk2818/proprietary/libopencore_player.so $(LOCAL_PATH)/patch_vendor.py
	@mkdir -p $(dir $@)
	$(hide) python $(PRIVATE_PATCHER) $< $@

# Old PV auxiliary libraries list this obsolete dependency but import no symbols
# from it. Surface access is implemented by librk28pvlegacy and the 4.2 bridge.
include $(CLEAR_VARS)
LOCAL_MODULE := libsurfaceflinger_client
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := surfaceflinger_client_stub.cpp
include $(BUILD_SHARED_LIBRARY)
