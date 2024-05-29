package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.EventBus;
/* loaded from: a.zip:com/android/systemui/recents/events/activity/DockedTopTaskEvent.class */
public class DockedTopTaskEvent extends EventBus.Event {
    public int dragMode;
    public Rect initialRect;

    public DockedTopTaskEvent(int i, Rect rect) {
        this.dragMode = i;
        this.initialRect = rect;
    }
}
