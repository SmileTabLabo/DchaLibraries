package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/* loaded from: a.zip:com/android/launcher3/WallpaperChangedReceiver.class */
public class WallpaperChangedReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        LauncherAppState.getInstance().onWallpaperChanged();
    }
}
