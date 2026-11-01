# Build the same AOSP ICS Gallery2 source used by this project's CM9 ROM.
# Use a board-local module name so inherited JB Gallery2 overrides do not drop it.
RK28_ICS_GALLERY_PATH := $(call my-dir)/ics
LOCAL_PATH := $(RK28_ICS_GALLERY_PATH)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-java-files-under, src_pd)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_MANIFEST_FILE := AndroidManifest.xml
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 rk28.gallery.ics.common
LOCAL_PACKAGE_NAME := RK28Gallery
LOCAL_CERTIFICATE := media
LOCAL_OVERRIDES_PACKAGES := Gallery Gallery2 Gallery3D GalleryNew3D
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
include $(RK28_ICS_GALLERY_PATH)/gallerycommon/Android.mk
