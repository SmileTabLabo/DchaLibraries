package com.android.systemui.recents.events.ui.dragndrop;

import android.graphics.Point;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskView;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/dragndrop/DragStartEvent.class */
public class DragStartEvent extends EventBus.Event {
    public final Task task;
    public final TaskView taskView;
    public final Point tlOffset;

    public DragStartEvent(Task task, TaskView taskView, Point point) {
        this.task = task;
        this.taskView = taskView;
        this.tlOffset = point;
    }
}
