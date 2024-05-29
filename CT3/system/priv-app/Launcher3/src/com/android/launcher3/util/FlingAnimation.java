package com.android.launcher3.util;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.animation.DecelerateInterpolator;
import com.android.launcher3.DragLayer;
import com.android.launcher3.DragView;
import com.android.launcher3.DropTarget;
/* loaded from: a.zip:com/android/launcher3/util/FlingAnimation.class */
public class FlingAnimation implements ValueAnimator.AnimatorUpdateListener {
    protected float mAX;
    protected float mAY;
    protected final float mAnimationTimeFraction;
    protected final DragLayer mDragLayer;
    protected final DropTarget.DragObject mDragObject;
    protected final int mDuration;
    protected final Rect mIconRect;
    protected final float mUX;
    protected final float mUY;
    protected final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.75f);
    protected final Rect mFrom = new Rect();

    public FlingAnimation(DropTarget.DragObject dragObject, PointF pointF, Rect rect, DragLayer dragLayer) {
        this.mDragObject = dragObject;
        this.mUX = pointF.x / 1000.0f;
        this.mUY = pointF.y / 1000.0f;
        this.mIconRect = rect;
        this.mDragLayer = dragLayer;
        dragLayer.getViewRectRelativeToSelf(dragObject.dragView, this.mFrom);
        float scaleX = dragObject.dragView.getScaleX();
        float measuredWidth = ((scaleX - 1.0f) * dragObject.dragView.getMeasuredWidth()) / 2.0f;
        float measuredHeight = ((scaleX - 1.0f) * dragObject.dragView.getMeasuredHeight()) / 2.0f;
        Rect rect2 = this.mFrom;
        rect2.left = (int) (rect2.left + measuredWidth);
        Rect rect3 = this.mFrom;
        rect3.right = (int) (rect3.right - measuredWidth);
        Rect rect4 = this.mFrom;
        rect4.top = (int) (rect4.top + measuredHeight);
        Rect rect5 = this.mFrom;
        rect5.bottom = (int) (rect5.bottom - measuredHeight);
        this.mDuration = initDuration();
        this.mAnimationTimeFraction = this.mDuration / (this.mDuration + 300);
    }

    public final int getDuration() {
        return this.mDuration + 300;
    }

    protected int initDuration() {
        float f = -this.mFrom.bottom;
        float f2 = (this.mUY * this.mUY) + (2.0f * f * 0.5f);
        if (f2 >= 0.0f) {
            this.mAY = 0.5f;
        } else {
            f2 = 0.0f;
            this.mAY = (this.mUY * this.mUY) / ((-f) * 2.0f);
        }
        double sqrt = ((-this.mUY) - Math.sqrt(f2)) / this.mAY;
        this.mAX = (float) (((((-this.mFrom.exactCenterX()) + this.mIconRect.exactCenterX()) - (this.mUX * sqrt)) * 2.0d) / (sqrt * sqrt));
        return (int) Math.round(sqrt);
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        float f = animatedFraction > this.mAnimationTimeFraction ? 1.0f : animatedFraction / this.mAnimationTimeFraction;
        DragView dragView = (DragView) this.mDragLayer.getAnimatedView();
        float f2 = f * this.mDuration;
        dragView.setTranslationX((this.mUX * f2) + this.mFrom.left + (((this.mAX * f2) * f2) / 2.0f));
        dragView.setTranslationY((this.mUY * f2) + this.mFrom.top + (((this.mAY * f2) * f2) / 2.0f));
        dragView.setAlpha(1.0f - this.mAlphaInterpolator.getInterpolation(f));
    }
}
