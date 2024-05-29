package com.android.systemui.recents.events.ui.focus;

import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/ui/focus/FocusNextTaskViewEvent.class */
public class FocusNextTaskViewEvent extends EventBus.Event {
    public final int timerIndicatorDuration;

    public FocusNextTaskViewEvent(int i) {
        this.timerIndicatorDuration = i;
    }
}
