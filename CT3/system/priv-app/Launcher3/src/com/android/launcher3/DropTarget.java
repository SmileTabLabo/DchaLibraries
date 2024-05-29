package com.android.launcher3;

import android.graphics.PointF;
import android.graphics.Rect;
import com.android.launcher3.accessibility.DragViewStateAnnouncer;
/* loaded from: a.zip:com/android/launcher3/DropTarget.class */
public interface DropTarget {

    /* loaded from: a.zip:com/android/launcher3/DropTarget$DragObject.class */
    public static class DragObject {
        public boolean accessibleDrag;
        public DragViewStateAnnouncer stateAnnouncer;
        public int x = -1;
        public int y = -1;
        public int xOffset = -1;
        public int yOffset = -1;
        public boolean dragComplete = false;
        public DragView dragView = null;
        public Object dragInfo = null;
        public DragSource dragSource = null;
        public Runnable postAnimationRunnable = null;
        public boolean cancelled = false;
        public boolean deferDragViewCleanupPostAnimation = true;

        public final float[] getVisualCenter(float[] fArr) {
            if (fArr == null) {
                fArr = new float[2];
            }
            int i = this.x;
            int i2 = this.xOffset;
            int i3 = this.y;
            int i4 = this.yOffset;
            fArr[0] = (this.dragView.getDragRegion().width() / 2) + (i - i2);
            fArr[1] = (this.dragView.getDragRegion().height() / 2) + (i3 - i4);
            return fArr;
        }

        public String toString() {
            return "DragObject{x = " + this.x + ",y = " + this.y + ",xOffset = " + this.xOffset + ",yOffset = " + this.yOffset + ",dragComplete = " + this.dragComplete + ",dragInfo = " + this.dragInfo + ",dragSource = " + this.dragSource + "}";
        }
    }

    boolean acceptDrop(DragObject dragObject);

    void getHitRectRelativeToDragLayer(Rect rect);

    boolean isDropEnabled();

    void onDragEnter(DragObject dragObject);

    void onDragExit(DragObject dragObject);

    void onDragOver(DragObject dragObject);

    void onDrop(DragObject dragObject);

    void onFlingToDelete(DragObject dragObject, PointF pointF);

    void prepareAccessibilityDrop();
}
