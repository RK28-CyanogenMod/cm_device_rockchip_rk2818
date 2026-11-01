package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/** Restores saved hardware state once at boot; starts no services or workers. */
public class RockPartsStartup extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        HardwareSettings.writeBoolean(HardwareSettings.USB_HOST_NODE,
                prefs.getBoolean("usb_host_mode", false));
        String vibrationNode = HardwareSettings.findVibrationNode();
        if (vibrationNode != null) {
            HardwareSettings.writeBoolean(vibrationNode,
                    prefs.getBoolean("touchscreen_vibration", false));
        }
        Log.i("RockParts", "boot restore finished");
    }
}
