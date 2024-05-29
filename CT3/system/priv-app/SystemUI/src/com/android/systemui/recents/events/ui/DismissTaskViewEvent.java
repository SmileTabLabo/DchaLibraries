package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.views.TaskView;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/DismissTaskViewEvent.class */
public class DismissTaskViewEvent extends EventBus.AnimatedEvent {
    public final TaskView taskView;

    public DismissTaskViewEvent(TaskView taskView) {
        this.taskView = taskView;
    }
}
