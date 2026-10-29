package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RockPartsStartup extends BroadcastReceiver
{
   private void writeValue(String parameter, int value) {
      File file = new File(parameter);
      if (!file.exists() || !file.canWrite()) {
         return;
      }
      try {
          FileOutputStream fos = new FileOutputStream(file);
          fos.write(String.valueOf(value).getBytes());
          fos.flush();
          fos.close();
      } catch (FileNotFoundException e) {
         // Optional RK2818 sysfs node is not present.
      } catch (IOException e) {
         // Sysfs does not support all regular-file operations.
      }
   }

   @Override
   public void onReceive(final Context context, final Intent bootintent) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

       // USB Host Mode
       if(prefs.getBoolean("usb_host_mode", false))
        writeValue("/sys/bus/platform/drivers/dwc_otg/force_usb_mode", 1);
       else
        writeValue("/sys/bus/platform/drivers/dwc_otg/force_usb_mode", 0);

       // Touch Vibration
       if(prefs.getBoolean("touchscreen_vibration", false))
           writeValue("/sys/bus/spi/drivers/xpt2046_ts/MOTOenable", 1);
       else
           writeValue("/sys/bus/spi/drivers/xpt2046_ts/MOTOenable", 0);
   }
}
