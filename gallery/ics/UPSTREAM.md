# ICS Gallery2 for RK2818

Source: platform/packages/apps/Gallery2, AOSP android-4.0.4_r2.1.
Upstream commit: fd695590504c1eb612b4002952ec1e23ee63d91c (the existing CM9 checkout in this workspace).
Imported src/, src_pd/, res/, gallerycommon/, AndroidManifest.xml and proguard.flags.
The parent Android.mk builds this source against the current CM10.1 framework.
The static common library module name is private to this import to avoid collisions.
Upstream license headers are retained. This is built from source, not a prebuilt APK.
