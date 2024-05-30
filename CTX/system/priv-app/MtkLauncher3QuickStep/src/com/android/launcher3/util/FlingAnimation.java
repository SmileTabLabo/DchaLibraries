package com.android.launcher3.util;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import com.android.launcher3.ButtonDropTarget;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.dragndrop.DragView;
/* loaded from: classes.dex */
public class FlingAnimation implements ValueAnimator.AnimatorUpdateListener, Runnable {
    private static final int DRAG_END_DELAY = 300;
    private static final float MAX_ACCELERATION = 0.5f;
    protected float mAX;
    protected float mAY;
    protected final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.75f);
    protected float mAnimationTimeFraction;
    protected final DragLayer mDragLayer;
    protected final DropTarget.DragObject mDragObject;
    private final ButtonDropTarget mDropTarget;
    protected int mDuration;
    protected Rect mFrom;
    protected Rect mIconRect;
    private final Launcher mLauncher;
    protected final float mUX;
    protected final float mUY;

    public FlingAnimation(DropTarget.DragObject dragObject, PointF pointF, ButtonDropTarget buttonDropTarget, Launcher launcher) {
        this.mDropTarget = buttonDropTarget;
        this.mLauncher = launcher;
        this.mDragObject = dragObject;
        this.mUX = pointF.x / 1000.0f;
        this.mUY = pointF.y / 1000.0f;
        this.mDragLayer = this.mLauncher.getDragLayer();
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mIconRect = this.mDropTarget.getIconRect(this.mDragObject);
        this.mFrom = new Rect();
        this.mDragLayer.getViewRectRelativeToSelf(this.mDragObject.dragView, this.mFrom);
        float scaleX = this.mDragObject.dragView.getScaleX() - 1.0f;
        float measuredWidth = (this.mDragObject.dragView.getMeasuredWidth() * scaleX) / 2.0f;
        float measuredHeight = (scaleX * this.mDragObject.dragView.getMeasuredHeight()) / 2.0f;
        Rect rect = this.mFrom;
        rect.left = (int) (rect.left + measuredWidth);
        Rect rect2 = this.mFrom;
        rect2.right = (int) (rect2.right - measuredWidth);
        Rect rect3 = this.mFrom;
        rect3.top = (int) (rect3.top + measuredHeight);
        Rect rect4 = this.mFrom;
        rect4.bottom = (int) (rect4.bottom - measuredHeight);
        this.mDuration = Math.abs(this.mUY) > Math.abs(this.mUX) ? initFlingUpDuration() : initFlingLeftDuration();
        this.mAnimationTimeFraction = this.mDuration / (this.mDuration + 300);
        this.mDragObject.dragView.setColor(0);
        final int i = this.mDuration + 300;
        final long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        this.mDragLayer.animateView(this.mDragObject.dragView, this, i, new TimeInterpolator() { // from class: com.android.launcher3.util.FlingAnimation.1
            private int mCount = -1;
            private float mOffset = 0.0f;

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                if (this.mCount < 0) {
                    this.mCount++;
                } else if (this.mCount == 0) {
                    this.mOffset = Math.min(0.5f, ((float) (AnimationUtils.currentAnimationTimeMillis() - currentAnimationTimeMillis)) / i);
                    this.mCount++;
                }
                return Math.min(1.0f, this.mOffset + f);
            }
        }, new Runnable() { // from class: com.android.launcher3.util.FlingAnimation.2
            @Override // java.lang.Runnable
            public void run() {
                FlingAnimation.this.mLauncher.getStateManager().goToState(LauncherState.NORMAL);
                FlingAnimation.this.mDropTarget.completeDrop(FlingAnimation.this.mDragObject);
            }
        }, 0, null);
    }

    protected int initFlingUpDuration() {
        float f = -this.mFrom.bottom;
        float f2 = (this.mUY * this.mUY) + (2.0f * f * 0.5f);
        if (f2 >= 0.0f) {
            this.mAY = 0.5f;
        } else {
            this.mAY = (this.mUY * this.mUY) / (2.0f * (-f));
            f2 = 0.0f;
        }
        double sqrt = ((-this.mUY) - Math.sqrt(f2)) / this.mAY;
        this.mAX = (float) (((((-this.mFrom.exactCenterX()) + this.mIconRect.exactCenterX()) - (this.mUX * sqrt)) * 2.0d) / (sqrt * sqrt));
        return (int) Math.round(sqrt);
    }

    protected int initFlingLeftDuration() {
        float f = -this.mFrom.right;
        float f2 = (this.mUX * this.mUX) + (2.0f * f * 0.5f);
        if (f2 >= 0.0f) {
            this.mAX = 0.5f;
        } else {
            this.mAX = (this.mUX * this.mUX) / (2.0f * (-f));
            f2 = 0.0f;
        }
        double sqrt = ((-this.mUX) - Math.sqrt(f2)) / this.mAX;
        this.mAY = (float) (((((-this.mFrom.exactCenterY()) + this.mIconRect.exactCenterY()) - (this.mUY * sqrt)) * 2.0d) / (sqrt * sqrt));
        return (int) Math.round(sqrt);
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float f;
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (animatedFraction <= this.mAnimationTimeFraction) {
            f = animatedFraction / this.mAnimationTimeFraction;
        } else {
            f = 1.0f;
        }
        DragView dragView = (DragView) this.mDragLayer.getAnimatedView();
        float f2 = this.mDuration * f;
        dragView.setTranslationX((this.mUX * f2) + this.mFrom.left + (((this.mAX * f2) * f2) / 2.0f));
        dragView.setTranslationY((this.mUY * f2) + this.mFrom.top + (((this.mAY * f2) * f2) / 2.0f));
        dragView.setAlpha(1.0f - this.mAlphaInterpolator.getInterpolation(f));
    }
}
