package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/UpdateFreeformTaskViewVisibilityEvent.class */
public class UpdateFreeformTaskViewVisibilityEvent extends EventBus.Event {
    public final boolean visible;

    public UpdateFreeformTaskViewVisibilityEvent(boolean z) {
        this.visible = z;
    }
}
