package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.DropTarget;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/dragndrop/DragDropTargetChangedEvent.class */
public class DragDropTargetChangedEvent extends EventBus.AnimatedEvent {
    public final DropTarget dropTarget;
    public final Task task;

    public DragDropTargetChangedEvent(Task task, DropTarget dropTarget) {
        this.task = task;
        this.dropTarget = dropTarget;
    }
}
