package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/AllTaskViewsDismissedEvent.class */
public class AllTaskViewsDismissedEvent extends EventBus.Event {
    public final int msgResId;

    public AllTaskViewsDismissedEvent(int i) {
        this.msgResId = i;
    }
}
