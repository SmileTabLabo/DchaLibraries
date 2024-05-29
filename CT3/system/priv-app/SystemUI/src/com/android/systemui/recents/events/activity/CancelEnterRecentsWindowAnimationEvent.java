package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/CancelEnterRecentsWindowAnimationEvent.class */
public class CancelEnterRecentsWindowAnimationEvent extends EventBus.Event {
    public final Task launchTask;

    public CancelEnterRecentsWindowAnimationEvent(Task task) {
        this.launchTask = task;
    }
}
