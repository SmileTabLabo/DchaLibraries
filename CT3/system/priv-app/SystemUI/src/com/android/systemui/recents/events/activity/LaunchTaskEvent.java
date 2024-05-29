package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskView;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/LaunchTaskEvent.class */
public class LaunchTaskEvent extends EventBus.Event {
    public final boolean screenPinningRequested;
    public final Rect targetTaskBounds;
    public final int targetTaskStack;
    public final Task task;
    public final TaskView taskView;

    public LaunchTaskEvent(TaskView taskView, Task task, Rect rect, int i, boolean z) {
        this.taskView = taskView;
        this.task = task;
        this.targetTaskBounds = rect;
        this.targetTaskStack = i;
        this.screenPinningRequested = z;
    }
}
