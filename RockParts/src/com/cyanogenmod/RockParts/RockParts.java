package com.cyanogenmod.settings.device;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class RockParts extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rockparts);
        bindSwitch("usb_host_mode", HardwareSettings.USB_HOST_NODE);
        bindSwitch("touchscreen_vibration", HardwareSettings.findVibrationNode());

        Preference calibration = findPreference("touchscreen_calibration");
        try {
            getPackageManager().getPackageInfo("com.rockchip.tscalibration", 0);
            calibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent();
                    intent.setClassName("com.rockchip.tscalibration",
                            "com.rockchip.tscalibration.TouchScreenMonitor");
                    startActivity(intent);
                    return true;
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            getPreferenceScreen().removePreference(calibration);
        }
    }

    private void bindSwitch(String key, final String node) {
        Preference preference = findPreference(key);
        if (!HardwareSettings.isAvailable(node)) {
            preference.setEnabled(false);
            preference.setSummary(R.string.hardware_unavailable);
            return;
        }
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean success = HardwareSettings.writeBoolean(node, (Boolean) value);
                if (!success) {
                    Toast.makeText(RockParts.this, R.string.hardware_write_failed,
                            Toast.LENGTH_SHORT).show();
                }
                // CheckBoxPreference persists the value only when this returns true.
                return success;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("RockParts", "settings activity destroyed");
    }
}
