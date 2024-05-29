package com.android.systemui.screenshot;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/* loaded from: a.zip:com/android/systemui/screenshot/ScreenshotServiceErrorReceiver.class */
public class ScreenshotServiceErrorReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        GlobalScreenshot.notifyScreenshotError(context, (NotificationManager) context.getSystemService("notification"), 2131493340);
    }
}
