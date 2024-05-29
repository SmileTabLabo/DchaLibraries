package com.android.settingslib;

import android.content.Context;
import android.provider.Settings;
/* loaded from: a.zip:com/android/settingslib/WirelessUtils.class */
public class WirelessUtils {
    public static boolean isAirplaneModeOn(Context context) {
        boolean z = false;
        if (Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z = true;
        }
        return z;
    }
}
