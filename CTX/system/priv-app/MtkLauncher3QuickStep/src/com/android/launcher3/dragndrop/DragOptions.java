package com.android.launcher3.dragndrop;

import android.graphics.Point;
import com.android.launcher3.DropTarget;
/* loaded from: classes.dex */
public class DragOptions {
    public boolean isAccessibleDrag = false;
    public Point systemDndStartPoint = null;
    public PreDragCondition preDragCondition = null;
    public float intrinsicIconScaleFactor = 1.0f;

    /* loaded from: classes.dex */
    public interface PreDragCondition {
        void onPreDragEnd(DropTarget.DragObject dragObject, boolean z);

        void onPreDragStart(DropTarget.DragObject dragObject);

        boolean shouldStartDrag(double d);
    }
}
