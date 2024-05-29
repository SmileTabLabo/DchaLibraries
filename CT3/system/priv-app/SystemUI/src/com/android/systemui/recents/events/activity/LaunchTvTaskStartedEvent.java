package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.tv.views.TaskCardView;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/LaunchTvTaskStartedEvent.class */
public class LaunchTvTaskStartedEvent extends EventBus.AnimatedEvent {
    public final TaskCardView taskView;

    public LaunchTvTaskStartedEvent(TaskCardView taskCardView) {
        this.taskView = taskCardView;
    }
}
