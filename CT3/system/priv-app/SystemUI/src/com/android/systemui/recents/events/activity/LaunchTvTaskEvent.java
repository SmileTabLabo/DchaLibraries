package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.tv.views.TaskCardView;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/LaunchTvTaskEvent.class */
public class LaunchTvTaskEvent extends EventBus.Event {
    public final Rect targetTaskBounds;
    public final int targetTaskStack;
    public final Task task;
    public final TaskCardView taskView;

    public LaunchTvTaskEvent(TaskCardView taskCardView, Task task, Rect rect, int i) {
        this.taskView = taskCardView;
        this.task = task;
        this.targetTaskBounds = rect;
        this.targetTaskStack = i;
    }
}
