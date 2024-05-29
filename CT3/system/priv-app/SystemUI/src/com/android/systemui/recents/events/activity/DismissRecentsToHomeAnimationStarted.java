package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/DismissRecentsToHomeAnimationStarted.class */
public class DismissRecentsToHomeAnimationStarted extends EventBus.AnimatedEvent {
    public final boolean animated;

    public DismissRecentsToHomeAnimationStarted(boolean z) {
        this.animated = z;
    }
}
