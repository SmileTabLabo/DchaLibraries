package com.android.systemui.settings;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
/* loaded from: a.zip:com/android/systemui/settings/CurrentUserTracker.class */
public abstract class CurrentUserTracker extends BroadcastReceiver {
    private Context mContext;
    private int mCurrentUserId;

    public CurrentUserTracker(Context context) {
        this.mContext = context;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
            int i = this.mCurrentUserId;
            this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
            if (i != this.mCurrentUserId) {
                onUserSwitched(this.mCurrentUserId);
            }
        }
    }

    public abstract void onUserSwitched(int i);

    public void startTracking() {
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.USER_SWITCHED"));
    }

    public void stopTracking() {
        this.mContext.unregisterReceiver(this);
    }
}
