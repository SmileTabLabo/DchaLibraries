package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.DropTarget;
import com.android.systemui.recents.views.TaskView;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/dragndrop/DragEndEvent.class */
public class DragEndEvent extends EventBus.AnimatedEvent {
    public final DropTarget dropTarget;
    public final Task task;
    public final TaskView taskView;

    public DragEndEvent(Task task, TaskView taskView, DropTarget dropTarget) {
        this.task = task;
        this.taskView = taskView;
        this.dropTarget = dropTarget;
    }
}
