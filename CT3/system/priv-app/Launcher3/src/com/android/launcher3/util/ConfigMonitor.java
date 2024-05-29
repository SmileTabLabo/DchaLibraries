package com.android.launcher3.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Process;
import android.util.Log;
import com.android.launcher3.Utilities;
/* loaded from: a.zip:com/android/launcher3/util/ConfigMonitor.class */
public class ConfigMonitor extends BroadcastReceiver {
    private final Context mContext;
    private final int mDensity;
    private final float mFontScale;

    public ConfigMonitor(Context context) {
        this.mContext = context;
        Configuration configuration = context.getResources().getConfiguration();
        this.mFontScale = configuration.fontScale;
        this.mDensity = getDensity(configuration);
    }

    private static int getDensity(Configuration configuration) {
        return Utilities.ATLEAST_JB_MR1 ? configuration.densityDpi : 0;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Configuration configuration = context.getResources().getConfiguration();
        if (this.mFontScale == configuration.fontScale && this.mDensity == getDensity(configuration)) {
            return;
        }
        Log.d("ConfigMonitor", "Configuration changed, restarting launcher");
        this.mContext.unregisterReceiver(this);
        Process.killProcess(Process.myPid());
    }

    public void register() {
        this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.CONFIGURATION_CHANGED"));
    }
}
