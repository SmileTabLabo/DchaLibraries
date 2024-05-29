package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/* loaded from: a.zip:com/android/launcher3/StartupReceiver.class */
public class StartupReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        context.sendStickyBroadcast(new Intent("com.android.launcher3.SYSTEM_READY"));
    }
}
