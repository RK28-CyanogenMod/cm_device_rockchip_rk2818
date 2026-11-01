# Camera Setup
USE_CAMERA_STUB := true
BOARD_USE_FROYO_LIBCAMERA := false

# inherit from the proprietary version
-include vendor/rockchip/rk2818/BoardConfigVendor.mk

TARGET_BOARD_PLATFORM := rk2818
TARGET_ARCH := arm
TARGET_CPU_ABI := armeabi
TARGET_CPU_VARIANT := arm926ej-s
TARGET_ARCH_VARIANT := armv5te
TARGET_ARCH_VARIANT_CPU := arm926ej-s
TARGET_PROVIDES_INIT_RC := true

TARGET_NO_BOOTLOADER := true
TARGET_BOOTLOADER_BOARD_NAME := rk28board

#TARGET_NO_KERNEL := true
TARGET_NO_RECOVERY := true

# Use Old Style USB Mounting Untill we get kernel source
BOARD_USE_USB_MASS_STORAGE_SWITCH := true
BOARD_USE_LEGACY_USB_MASS_STORAGE_SWITCH := true
TARGET_USE_CUSTOM_LUN_FILE_PATH := "/sys/devices/platform/dwc_otg/gadget/lun%d/file"

# Wifi related defines
BOARD_WPA_SUPPLICANT_DRIVER := WEXT
WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_rk28
WIFI_DRIVER_MODULE_PATH     := "/system/lib/modules/wlan.ko"
WIFI_DRIVER_MODULE_ARG      := ""
WIFI_DRIVER_MODULE_NAME     := "wlan"

#BOARD_HAVE_BLUETOOTH := true

#Audio Stuff
#BOARD_USES_GENERIC_AUDIO := true

#EGL Config
BOARD_AVOID_DRAW_TEXTURE_EXTENSION := true
BOARD_HAS_LIMITED_EGL := true
TARGET_LIBAGL_USE_GRALLOC_COPYBITS := true


# fix this up by examining /proc/mtd on a running device
BOARD_BOOTIMAGE_PARTITION_SIZE := 0x00c00000
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 0x01000000
BOARD_SYSTEMIMAGE_PARTITION_SIZE := 0x08000000
BOARD_USERDATAIMAGE_PARTITION_SIZE := 0x10000000
BOARD_FLASH_BLOCK_SIZE := 16384

# The devices' prebuilt kernel
TARGET_PREBUILT_KERNEL := device/rockchip/rk2818/kernel/kernel.img

# Rockchip DSP video decoding through the legacy PVPlayer ABI bridge.
BOARD_USES_RK28_OPENCORE := true

# ARM926 has no TLS system register, VFP or NEON; use portable implementations.
TARGET_ARCH_VARIANT_CPU := arm926ej-s
ARCH_ARM_HAVE_TLS_REGISTER := false
ARCH_ARM_HAVE_NEON := false
ARCH_ARM_HAVE_VFP := false
TARGET_CPU_SMP := false
# The RK2818 software EGL driver needs the framebuffer-native window ABI.
BOARD_EGL_NEEDS_LEGACY_FB := true
TARGET_DISABLE_TRIPLE_BUFFERING := true
BOARD_HAVE_PRE_ANDROID_4_2_PMEM := true

