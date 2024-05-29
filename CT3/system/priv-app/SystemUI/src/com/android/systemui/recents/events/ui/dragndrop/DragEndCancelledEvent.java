package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskView;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/dragndrop/DragEndCancelledEvent.class */
public class DragEndCancelledEvent extends EventBus.AnimatedEvent {
    public final TaskStack stack;
    public final Task task;
    public final TaskView taskView;

    public DragEndCancelledEvent(TaskStack taskStack, Task task, TaskView taskView) {
        this.stack = taskStack;
        this.task = task;
        this.taskView = taskView;
    }
}
