package com.cyanogenmod.settings.device;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import libcore.io.ErrnoException;
import libcore.io.Libcore;
import libcore.io.OsConstants;

/** One sysfs write per change, shared by the activity and boot receiver. */
final class HardwareSettings {
    private static final String TAG = "RockParts";
    static final String USB_HOST_NODE =
            "/sys/bus/platform/drivers/dwc_otg/force_usb_mode";
    private static final String[] VIBRATION_NODES = {
        "/sys/bus/spi/drivers/xpt2046_ts/MOTOenable"
    };

    static boolean isAvailable(String node) {
        return node != null && new File(node).exists() && new File(node).canWrite();
    }

    static String findVibrationNode() {
        for (String node : VIBRATION_NODES) {
            if (isAvailable(node)) return node;
        }
        return null;
    }

    private static Boolean readBoolean(String node) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(node));
        String value;
        try {
            value = reader.readLine();
        } finally {
            reader.close();
        }
        if (value == null) return null;
        value = value.trim();
        if (node.endsWith("/MOTOenable")) {
            // The vendor driver's labels describe its active-low output:
            // Motoenble == 0 reports High; nonzero reports Low.
            if ("Ledlevel is High".equals(value)) return Boolean.FALSE;
            if ("Ledlevel is Low".equals(value)) return Boolean.TRUE;
        } else {
            if ("0".equals(value)) return Boolean.FALSE;
            if ("1".equals(value)) return Boolean.TRUE;
        }
        return null;
    }

    static boolean writeBoolean(String node, boolean enabled) {
        if (!isAvailable(node)) {
            Log.w(TAG, "hardware node unavailable: " + node);
            return false;
        }
        FileDescriptor fd = null;
        try {
            Boolean current = readBoolean(node);
            if (current != null && current.booleanValue() == enabled) return true;
            byte value = (byte) (enabled ? '1' : '0');
            if (node.endsWith("/MOTOenable") && !enabled) {
                // Moto_control uses strncpy(local, buf, 1) then simple_strtol,
                // without terminating local. NUL makes zero deterministic.
                value = 0;
            }
            fd = Libcore.os.open(node, OsConstants.O_WRONLY, 0);
            // Vendor MOTOenable returns the parsed value (0/1), not a byte
            // count. FileOutputStream/IoBridge would retry a zero result
            // forever, burning CPU and repeatedly changing the driver state.
            // Issue exactly ONE syscall and validate hardware readback instead.
            int result = Libcore.os.write(fd, new byte[] { value }, 0, 1);
            Libcore.os.close(fd);
            fd = null;
            Boolean actual = readBoolean(node);
            boolean success = actual != null && actual.booleanValue() == enabled;
            Log.i(TAG, "write " + node + "=" + enabled + " result=" + result
                    + " readback=" + actual + " success=" + success);
            return success;
        } catch (ErrnoException e) {
            Log.e(TAG, "cannot write " + node, e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "cannot read " + node, e);
            return false;
        } finally {
            if (fd != null) {
                try { Libcore.os.close(fd); } catch (ErrnoException ignored) { }
            }
        }
    }
}
