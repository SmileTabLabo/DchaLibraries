package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/HideRecentsEvent.class */
public class HideRecentsEvent extends EventBus.Event {
    public final boolean triggeredFromAltTab;
    public final boolean triggeredFromHomeKey;

    public HideRecentsEvent(boolean z, boolean z2) {
        this.triggeredFromAltTab = z;
        this.triggeredFromHomeKey = z2;
    }
}
