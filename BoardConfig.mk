# Camera Setup
USE_CAMERA_STUB := true
BOARD_USE_FROYO_LIBCAMERA := false

# inherit from the proprietary version
-include vendor/rockchip/rk2818/BoardConfigVendor.mk

TARGET_BOARD_PLATFORM := rk2818
TARGET_CPU_ABI := armeabi
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
BOARD_WPA_SUPPLICANT_DRIVER := AWEXT
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
