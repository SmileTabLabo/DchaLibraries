package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
/* loaded from: a.zip:com/android/systemui/BootReceiver.class */
public class BootReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        try {
            if (Settings.Global.getInt(context.getContentResolver(), "show_processes", 0) != 0) {
                context.startService(new Intent(context, LoadAverageService.class));
            }
        } catch (Exception e) {
            Log.e("SystemUIBootReceiver", "Can't start load average service", e);
        }
    }
}
