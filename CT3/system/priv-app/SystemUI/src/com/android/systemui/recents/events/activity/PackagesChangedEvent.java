package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.RecentsPackageMonitor;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/PackagesChangedEvent.class */
public class PackagesChangedEvent extends EventBus.Event {
    public final RecentsPackageMonitor monitor;
    public final String packageName;
    public final int userId;

    public PackagesChangedEvent(RecentsPackageMonitor recentsPackageMonitor, String str, int i) {
        this.monitor = recentsPackageMonitor;
        this.packageName = str;
        this.userId = i;
    }
}
