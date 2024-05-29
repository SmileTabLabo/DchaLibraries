package com.android.systemui.recents.events.ui;

import android.util.MutableInt;
import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/StackViewScrolledEvent.class */
public class StackViewScrolledEvent extends EventBus.ReusableEvent {
    public final MutableInt yMovement = new MutableInt(0);

    public void updateY(int i) {
        this.yMovement.value = i;
    }
}
