package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.model.TaskStack;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/MultiWindowStateChangedEvent.class */
public class MultiWindowStateChangedEvent extends EventBus.AnimatedEvent {
    public final boolean inMultiWindow;
    public final boolean showDeferredAnimation;
    public final TaskStack stack;

    public MultiWindowStateChangedEvent(boolean z, boolean z2, TaskStack taskStack) {
        this.inMultiWindow = z;
        this.showDeferredAnimation = z2;
        this.stack = taskStack;
    }
}
