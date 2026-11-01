package android.rk.RockVideoPlayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

/* loaded from: classes.dex */
public class DBUtils {
    private static final String PREFS_NAME = "android.rk.RockVideoPlayer";
    private static final String TAG = "DBUtils";
    public static boolean DEBUG = true;
    private static String[] mCols = {"_display_name", "duration", "mime_type", "_size", "_id", "_data", "bookmark"};

    /* loaded from: classes.dex */
    public interface Def {
        public static final int MODE_SYSTEM = 1;
        public static final int MODE_USER = 2;
        public static final int VOLUMEMINUS = -1;
        public static final int VOLUMEPLUS = 1;
    }

    public static void LOG(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        } else if (DebugUtil.isDebugFileExist()) {
            Log.d(TAG, msg);
        }
    }

    public static ContentResolver getResolver(Context context) {
        return context.getContentResolver();
    }

    public static Cursor getCurrentCursor(Context context, Uri currenturi) {
        LOG("Enter getCurrentCursor() currenturi:" + currenturi.toString());
        Uri uri = MediaStore.Video.Media.getContentUri("external");
        LOG("Enter getCurrentCursor() uri:" + uri.toString());
        Cursor cur = getResolver(context).query(uri, mCols, null, null, null);
        LOG("Enter getCurrentCursor() cur:" + cur);
        if (cur != null && cur.moveToFirst()) {
            while (!cur.isAfterLast()) {
                if (currenturi.equals(ContentUris.withAppendedId(uri, cur.getInt(cur.getColumnIndexOrThrow("_id"))))) {
                    return cur;
                }
                cur.moveToNext();
            }
        }
        return null;
    }

    public static Cursor getCurrentCursor(Context context, int position) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.getContentUri("external");
        Cursor cur = resolver.query(uri, mCols, null, null, null);
        if (cur == null) {
            return null;
        }
        if (cur.moveToFirst()) {
            while (!cur.isAfterLast()) {
                cur.moveToPosition(position + 1);
            }
        }
        return cur;
    }

    public static Cursor getNextCursor(Context context, Uri currenturi) {
        LOG("Enter getNextCursor()");
        Cursor cur = getCurrentCursor(context, currenturi);
        LOG("Current cur = " + cur);
        if (cur == null) {
            return null;
        }
        if (cur.isLast()) {
            cur.moveToFirst();
            return cur;
        }
        cur.moveToNext();
        return cur;
    }

    public static Cursor getPrevCursor(Context context, Uri currenturi) {
        LOG("Enter getPrevCursor()");
        Cursor cur = getCurrentCursor(context, currenturi);
        LOG("Current cur = " + cur);
        if (cur == null) {
            return null;
        }
        if (cur.isFirst()) {
            cur.moveToLast();
            return cur;
        }
        cur.moveToPrevious();
        return cur;
    }

    public static boolean setBookmark(Context context, Cursor cur, int bookmark) {
        if (cur == null) {
            return false;
        }
        Uri uri = MediaStore.Video.Media.getContentUri("external");
        Uri videouri = ContentUris.withAppendedId(uri, cur.getInt(cur.getColumnIndexOrThrow("_id")));
        LOG("videouri = " + videouri);
        ContentValues value = new ContentValues();
        value.put("bookmark", Integer.valueOf(bookmark));
        LOG("value = " + value);
        context.getContentResolver().update(videouri, value, null, null);
        LOG(getResolver(context).update(videouri, value, null, null) + "rows changed");
        cur.close();
        return true;
    }

    public static boolean checkVideoAvailable(Context context, Cursor cur) {
        String videofile = cur.getString(cur.getColumnIndexOrThrow("_data"));
        LOG("videofile = " + videofile);
        File file = new File(videofile);
        if (file.exists()) {
            cur.close();
            return true;
        }
        cur.close();
        return false;
    }

    public static String getLocalPath(Context context, Uri uri) {
        String path = null;
        String scheme = uri.getScheme();
        if ("content".equalsIgnoreCase(scheme)) {
            Cursor cur = getResolver(context).query(uri, mCols, null, null, null);
            if (cur == null) {
                Log.e(TAG, "getLocalPath() : Failed to get Cursor for uri : " + uri);
                return null;
            }
            if (cur.moveToFirst()) {
                path = cur.getString(cur.getColumnIndexOrThrow("_data"));
            }
            cur.close();
        } else if ("file".equalsIgnoreCase(scheme)) {
            path = uri.toString().substring("file://".length());
        }
        return path;
    }

    private static boolean uriSupportsBookmarks(Uri uri) {
        LOG("Enter uriSupportsBookmarks()");
        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        LOG("scheme = " + scheme + "AND authority = " + authority);
        return "content".equalsIgnoreCase(scheme) && "media".equalsIgnoreCase(authority);
    }

    public static Integer getBookmark(Context context, Uri uri) {
        Cursor cur;
        LOG("Enter getBookmark()");
        if (uriSupportsBookmarks(uri) && (cur = getCurrentCursor(context, uri)) != null) {
            Integer bookmark = Integer.valueOf(cur.getInt(cur.getColumnIndexOrThrow("bookmark")));
            cur.close();
            return bookmark;
        }
        return null;
    }

    public static boolean isBookmarkNull(Context context, Uri uri) {
        Cursor cur = getCurrentCursor(context, uri);
        if (cur == null || cur.getInt(cur.getColumnIndexOrThrow("bookmark")) == 0) {
            return true;
        }
        cur.close();
        return false;
    }

    public static void deleteBookmark(Context context, Uri uri) {
        if (!isBookmarkNull(context, uri)) {
            ContentValues value = new ContentValues();
            value.put("bookmark", (Integer) 0);
            LOG("value = " + value);
            context.getContentResolver().update(uri, value, null, null);
            Toast.makeText(context, (int) R.string.deletebookmark_sucess, 0).show();
            return;
        }
        Toast.makeText(context, (int) R.string.deletebookmark_fail, 0).show();
    }

    public static void deleteCurrentVideo(Context context, Uri uri) {
        try {
            getResolver(context).delete(uri, null, null);
        } catch (Exception e) {
        }
    }

    public static void deleteViedoFile(Context context, String fileName) {
        if (fileName.indexOf("sdcard") != -1) {
            new File(fileName).delete();
            Toast.makeText(context, (int) R.string.alert_deletesuccess, 0).show();
            return;
        }
        File f = new File(fileName);
        if (!f.exists() || !f.isFile()) {
            LOG(fileName + " not exist!!!");
            Toast.makeText(context, (int) R.string.alert_deletesuccess, 0).show();
            return;
        }
        LOG(fileName + " is exist and is a file");
        boolean result = f.delete();
        if (!result) {
            LOG("Could not delete " + fileName);
            Toast.makeText(context, (int) R.string.alert_cantdelete, 0).show();
            return;
        }
        Toast.makeText(context, (int) R.string.alert_deletesuccess, 0).show();
    }

    public static boolean isMediaScannerScanning(Context context) {
        boolean result = false;
        Cursor cursor = getResolver(context).query(MediaStore.getMediaScannerUri(), new String[]{"volume"}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                result = "external".equals(cursor.getString(0));
            }
            cursor.close();
        }
        LOG(">>>>>>>>>>>>>>>>>>>>>>>>> isMediaScannerScanning returning " + result);
        return result;
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LOG("Enter query()");
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    public static int FindPosition(Context context, Uri mUri) {
        if (mUri == null) {
            return -1;
        }
        Uri uri = MediaStore.Video.Media.getContentUri("external");
        Cursor cur = query(context, uri, mCols, null, null, null);
        if (cur != null) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                Uri tempUri = Uri.withAppendedPath(uri, cur.getString(cur.getColumnIndex("_id")));
                LOG("mUri/tempUri = " + mUri + "/" + tempUri);
                if (mUri.equals(tempUri)) {
                    return cur.getPosition();
                }
                cur.moveToNext();
            }
        }
        cur.close();
        return -1;
    }

    public static void setBacklight(Context context, int mode) {
        setBacklight(context, mode, getBacklight(context, mode));
    }

    public static void setOriBacklight(Context context, int mode) {
        storeOriBacklight(context, mode);
        setOriBacklight(context, mode, getOrigBacklight(context, mode));
    }

    public static void storeOriBacklight(Context context, int mode) {
        switch (mode) {
            case 1:
                storeOriSysBacklight(context, getOriSysBacklight(context));
                return;
            case Def.MODE_USER /* 2 */:
                storeOriSysBacklight(context, getOriSysBacklight(context));
                return;
            default:
                return;
        }
    }

    public static void storeOriSysBacklight(Context context, int value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("sysBacklight", value);
        editor.commit();
        LOG("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%SET SCREEN_BRIGHTNESS3:" + value);
        Settings.System.putInt(context.getContentResolver(), "screen_brightness", value);
    }

    public static int getOriSysBacklight(Context context) {
        int value = 0;
        try {
            value = Settings.System.getInt(context.getContentResolver(), "screen_brightness");
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            LOG("Get System info error ");
        }
        if (value != 0) {
            return value;
        }
        int value2 = getBacklightValue(context, 1);
        return value2;
    }

    public static void setOriBacklight(Context context, int mode, int value) {
        setBacklightValue(context, mode, value);
        storeBacklightValue(context, mode, value);
    }

    public static void setBacklight(Context context, int mode, int value) {
        setBacklightValue(context, mode, value);
        storeBacklightValue(context, mode, value);
    }

    public static int getBacklight(Context context, int mode) {
        return getBacklightValue(context, mode);
    }

    public static void setBacklightValue(Context context, int mode, int value) {
        switch (mode) {
            case 1:
                setBrightness(context, value);
                storeBacklightValue(context, 1, value);
                LOG("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%SET SCREEN_BRIGHTNESS2:" + value);
                Settings.System.putInt(context.getContentResolver(), "screen_brightness", value);
                return;
            case Def.MODE_USER /* 2 */:
                setBrightness(context, value);
                return;
            default:
                return;
        }
    }

    public static void setBrightness(Context context, int brightness) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                power.setTemporaryScreenBrightnessSettingOverride(brightness);
            }
        } catch (RemoteException e) {
        }
    }

    private static void storeBacklightValue(Context context, int mode, int lightness) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (mode) {
            case 1:
                editor.putInt("sysBacklight", lightness);
                editor.commit();
                LOG("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%SET SCREEN_BRIGHTNESS:" + lightness);
                Settings.System.putInt(context.getContentResolver(), "screen_brightness", lightness);
                return;
            case Def.MODE_USER /* 2 */:
                editor.putInt("userBacklight", lightness);
                editor.commit();
                return;
            default:
                return;
        }
    }

    private static int getBacklightValue(Context context, int mode) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        switch (mode) {
            case 1:
                int lightness = settings.getInt("sysBacklight", 0);
                if (lightness == 0) {
                    return getOrigBacklight(context, 1);
                }
                return lightness;
            case Def.MODE_USER /* 2 */:
                int lightness2 = settings.getInt("userBacklight", 0);
                if (lightness2 == 0) {
                    return getOrigBacklight(context, 1);
                }
                return lightness2;
            default:
                return 0;
        }
    }

    private static int getOrigBacklight(Context context, int mode) {
        switch (mode) {
            case 1:
                try {
                    int value = Settings.System.getInt(context.getContentResolver(), "screen_brightness");
                    return value;
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                    LOG("Get System info error ");
                    return 0;
                }
            case Def.MODE_USER /* 2 */:
                int value2 = getBacklightValue(context, 2);
                return value2;
            default:
                return 0;
        }
    }

    public static void setbackBacklight(Context context, int mode) {
        switch (mode) {
            case 1:
                setBacklightValue(context, 1, getBacklightValue(context, 1));
                return;
            case Def.MODE_USER /* 2 */:
                setBacklightValue(context, 1, getBacklightValue(context, 1));
                return;
            default:
                return;
        }
    }

    public static void setOriVolume(Context context, int mode) {
        setOriVolume(context, mode, getOrigVolume(context, mode));
    }

    public static void setVolume(Context context, int mode) {
        setVolume(context, mode, getVolumeValue(context, mode));
    }

    public static void setVolume(Context context, int mode, int value) {
        setVolumeValue(context, mode, value);
        storeVolumeValue(context, mode, value);
    }

    public static void setOriVolume(Context context, int mode, int value) {
        if (mode == 1) {
            setVolume(context, mode, value);
            return;
        }
        storeVolumeValue(context, 1, getOrigVolume(context, 1));
        setVolume(context, mode, value);
    }

    public static int getVolume(Context context, int mode) {
        return getVolumeValue(context, mode);
    }

    public static void setVolumeValue(Context context, int mode, int value) {
        switch (mode) {
            case 1:
                setVolumes(context, value);
                return;
            case Def.MODE_USER /* 2 */:
                setVolumes(context, value);
                return;
            default:
                return;
        }
    }

    public static void setVolumes(Context context, int value) {
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (value >= audioManager.getStreamMaxVolume(3)) {
            value = audioManager.getStreamMaxVolume(3);
        }
        audioManager.setStreamVolume(3, value, 0);
    }

    public static void volumeAdjust(Context context, int mode, int option) {
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (audioManager != null) {
            if (option == 1) {
                audioManager.adjustSuggestedStreamVolume(1, 3, 9);
                int volume = audioManager.getStreamVolume(3);
                storeVolumeValue(context, mode, volume);
            }
            if (option == -1) {
                audioManager.adjustSuggestedStreamVolume(-1, 3, 9);
                int volume2 = audioManager.getStreamVolume(3);
                storeVolumeValue(context, mode, volume2);
            }
        }
    }

    public static void storeVolumeValue(Context context, int mode, int value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (mode) {
            case 1:
                editor.putInt("sysVolumeValue", value);
                editor.commit();
                return;
            case Def.MODE_USER /* 2 */:
                editor.putInt("userVolumeValue", value);
                editor.commit();
                return;
            default:
                return;
        }
    }

    public static void setScreenValue(Context context, int mode) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("VideoscreenSize", mode);
        editor.commit();
    }

    public static int getScreenValue(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("VideoscreenSize", 0);
    }

    public static int getVolumeValue(Context context, int mode) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        switch (mode) {
            case 1:
                int volume = settings.getInt("sysVolumeValue", 0);
                if (volume == 0) {
                    return getOrigVolume(context, 1);
                }
                return volume;
            case Def.MODE_USER /* 2 */:
                int volume2 = settings.getInt("userVolumeValue", 0);
                if (volume2 == 0) {
                    return getOrigVolume(context, 1);
                }
                return volume2;
            default:
                return 0;
        }
    }

    public static int getOrigVolume(Context context, int mode) {
        switch (mode) {
            case 1:
                AudioManager audioManager = (AudioManager) context.getSystemService("audio");
                int volume = audioManager.getStreamVolume(3);
                return volume;
            case Def.MODE_USER /* 2 */:
                int volume2 = getVolumeValue(context, mode);
                return volume2;
            default:
                return 0;
        }
    }

    public static void setbackVolume(Context context, int mode) {
        switch (mode) {
            case 1:
            default:
                return;
            case Def.MODE_USER /* 2 */:
                setVolumeValue(context, 1, getVolumeValue(context, 1));
                return;
        }
    }

    public static int getVideoCount(Context context) {
        Uri uri = MediaStore.Video.Media.getContentUri("external");
        Cursor cur = query(context, uri, mCols, null, null, null);
        if (cur != null) {
            cur.moveToFirst();
            int count = cur.getCount();
            cur.close();
            return count;
        }
        Log.w(TAG, "getVideoCount() : Failed to get Cursor instance for '" + uri + "'.");
        return 0;
    }
}
