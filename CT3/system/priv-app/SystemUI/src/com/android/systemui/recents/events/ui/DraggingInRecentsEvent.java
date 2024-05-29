package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/DraggingInRecentsEvent.class */
public class DraggingInRecentsEvent extends EventBus.Event {
    public final float distanceFromTop;

    public DraggingInRecentsEvent(float f) {
        this.distanceFromTop = f;
    }
}
