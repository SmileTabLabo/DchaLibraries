package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/LaunchTaskSucceededEvent.class */
public class LaunchTaskSucceededEvent extends EventBus.Event {
    public final int taskIndexFromStackFront;

    public LaunchTaskSucceededEvent(int i) {
        this.taskIndexFromStackFront = i;
    }
}
