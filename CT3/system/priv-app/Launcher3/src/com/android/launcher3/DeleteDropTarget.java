package com.android.launcher3;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import com.android.launcher3.DropTarget;
import com.android.launcher3.util.FlingAnimation;
/* loaded from: a.zip:com/android/launcher3/DeleteDropTarget.class */
public class DeleteDropTarget extends ButtonDropTarget {
    public DeleteDropTarget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DeleteDropTarget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public static void removeWorkspaceOrFolderItem(Launcher launcher, ItemInfo itemInfo, View view) {
        launcher.removeItem(view, itemInfo, true);
        launcher.getWorkspace().stripEmptyScreens();
        launcher.getDragLayer().announceForAccessibility(launcher.getString(2131558468));
    }

    public static boolean supportsDrop(Object obj) {
        return ((obj instanceof ShortcutInfo) || (obj instanceof LauncherAppWidgetInfo)) ? true : obj instanceof FolderInfo;
    }

    @Override // com.android.launcher3.ButtonDropTarget
    void completeDrop(DropTarget.DragObject dragObject) {
        ItemInfo itemInfo = (ItemInfo) dragObject.dragInfo;
        if ((dragObject.dragSource instanceof Workspace) || (dragObject.dragSource instanceof Folder)) {
            removeWorkspaceOrFolderItem(this.mLauncher, itemInfo, null);
        }
    }

    @Override // com.android.launcher3.ButtonDropTarget, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHoverColor = getResources().getColor(2131361792);
        setDrawable(2130837530);
    }

    @Override // com.android.launcher3.ButtonDropTarget, com.android.launcher3.DropTarget
    public void onFlingToDelete(DropTarget.DragObject dragObject, PointF pointF) {
        dragObject.dragView.setColor(0);
        dragObject.dragView.updateInitialScaleToCurrentScale();
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        FlingAnimation flingAnimation = new FlingAnimation(dragObject, pointF, getIconRect(dragObject.dragView.getMeasuredWidth(), dragObject.dragView.getMeasuredHeight(), this.mDrawable.getIntrinsicWidth(), this.mDrawable.getIntrinsicHeight()), dragLayer);
        int duration = flingAnimation.getDuration();
        dragLayer.animateView(dragObject.dragView, flingAnimation, duration, new TimeInterpolator(this, AnimationUtils.currentAnimationTimeMillis(), duration) { // from class: com.android.launcher3.DeleteDropTarget.1
            private int mCount = -1;
            private float mOffset = 0.0f;
            final DeleteDropTarget this$0;
            final int val$duration;
            final long val$startTime;

            {
                this.this$0 = this;
                this.val$startTime = r6;
                this.val$duration = duration;
            }

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                if (this.mCount < 0) {
                    this.mCount++;
                } else if (this.mCount == 0) {
                    this.mOffset = Math.min(0.5f, ((float) (AnimationUtils.currentAnimationTimeMillis() - this.val$startTime)) / this.val$duration);
                    this.mCount++;
                }
                return Math.min(1.0f, this.mOffset + f);
            }
        }, new Runnable(this, dragObject) { // from class: com.android.launcher3.DeleteDropTarget.2
            final DeleteDropTarget this$0;
            final DropTarget.DragObject val$d;

            {
                this.this$0 = this;
                this.val$d = dragObject;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mLauncher.exitSpringLoadedDragMode();
                this.this$0.completeDrop(this.val$d);
                this.this$0.mLauncher.getDragController().onDeferredEndFling(this.val$d);
            }
        }, 0, null);
    }

    @Override // com.android.launcher3.ButtonDropTarget
    protected boolean supportsDrop(DragSource dragSource, Object obj) {
        return dragSource.supportsDeleteDropTarget() ? supportsDrop(obj) : false;
    }
}
