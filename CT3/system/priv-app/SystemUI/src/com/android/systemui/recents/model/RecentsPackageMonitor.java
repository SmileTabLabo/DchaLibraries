package com.android.systemui.recents.model;

import android.content.Context;
import android.os.UserHandle;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
/* loaded from: a.zip:com/android/systemui/recents/model/RecentsPackageMonitor.class */
public class RecentsPackageMonitor extends PackageMonitor {
    public boolean onPackageChanged(String str, int i, String[] strArr) {
        onPackageModified(str);
        return true;
    }

    public void onPackageModified(String str) {
        EventBus.getDefault().post(new PackagesChangedEvent(this, str, getChangingUserId()));
    }

    public void onPackageRemoved(String str, int i) {
        EventBus.getDefault().post(new PackagesChangedEvent(this, str, getChangingUserId()));
    }

    public void register(Context context) {
        try {
            register(context, BackgroundThread.get().getLooper(), UserHandle.ALL, true);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void unregister() {
        try {
            super.unregister();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
