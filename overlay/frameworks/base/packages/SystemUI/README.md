# RK2818 tablet SystemUI resources

The board uses 120 dpi on its 800x480 panel, selecting sw600dp tablet layouts.
The notification header and system bar use upstream tablet status artwork from
frameworks/base/packages/SystemUI/res/drawable-sw720dp-mdpi, copied unchanged
into drawable-sw600dp-nodpi under the stat_sys_* names used by the controllers.

These 24-pixel-tall images fit the 32dp containers at 120 dpi without scaling.
ImageViews use center for native pixels; spacing is outside the drawable bounds.
The *_in/out arrows retain the same canvas as their signal icon for alignment.
The normal battery states, charging states, signal and Wi-Fi states are all
included, so changing state never falls back to enlarged phone artwork.

The ic_sysbar_* names alone are not sufficient: Jelly Bean BatteryController and
NetworkController request stat_sys_* resources. Merely copying the former left
12x18 phone battery images on the live path and magnified their blurred edges.
## Panel transitions

On this board ro.config.disable_hw_accel=true selects the immediate panel
open/close and notifications/settings switch in NotificationPanel.java. This
is the version the user confirmed no longer flashes the entire display.
The usual animated path remains available on hardware-accelerated devices.
Do not replace the tested APK with the temporary animation-restored build.
