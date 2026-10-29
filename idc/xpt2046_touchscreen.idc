# Force the RK2818 XPT2046 device to use direct touchscreen mapping.
# The old 2.6.25 driver does not advertise INPUT_PROP_DIRECT, so Android
# otherwise classifies this device as a touchpad/pointer.
touch.deviceType = touchScreen
touch.orientationAware = 1
