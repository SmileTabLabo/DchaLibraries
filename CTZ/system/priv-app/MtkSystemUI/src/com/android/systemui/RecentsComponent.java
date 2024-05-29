package com.android.systemui;

import android.graphics.Rect;
/* loaded from: classes.dex */
public interface RecentsComponent {
    void onDraggingInRecents(float f);

    void onDraggingInRecentsEnded(float f);

    boolean splitPrimaryTask(int i, int i2, Rect rect, int i3);
}
