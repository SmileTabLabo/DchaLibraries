package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/DeleteTaskDataEvent.class */
public class DeleteTaskDataEvent extends EventBus.Event {
    public final Task task;

    public DeleteTaskDataEvent(Task task) {
        this.task = task;
    }
}
