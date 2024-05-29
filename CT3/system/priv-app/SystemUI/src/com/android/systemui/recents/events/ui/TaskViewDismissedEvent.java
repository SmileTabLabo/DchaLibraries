package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.AnimationProps;
import com.android.systemui.recents.views.TaskView;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/TaskViewDismissedEvent.class */
public class TaskViewDismissedEvent extends EventBus.Event {
    public final AnimationProps animation;
    public final Task task;
    public final TaskView taskView;

    public TaskViewDismissedEvent(Task task, TaskView taskView, AnimationProps animationProps) {
        this.task = task;
        this.taskView = taskView;
        this.animation = animationProps;
    }
}
