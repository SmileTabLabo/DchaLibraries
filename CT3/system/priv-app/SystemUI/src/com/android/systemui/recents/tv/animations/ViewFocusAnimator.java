package com.android.systemui.recents.tv.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.systemui.recents.tv.views.TaskCardView;
/* loaded from: a.zip:com/android/systemui/recents/tv/animations/ViewFocusAnimator.class */
public class ViewFocusAnimator implements View.OnFocusChangeListener {
    private final int mAnimDuration;
    private final float mDismissIconAlpha;
    ObjectAnimator mFocusAnimation;
    private final Interpolator mFocusInterpolator;
    private float mFocusProgress;
    private final float mSelectedScale;
    private final float mSelectedScaleDelta;
    private final float mSelectedSpacingDelta;
    private final float mSelectedZDelta;
    protected TaskCardView mTargetView;
    private final float mUnselectedScale;
    private final float mUnselectedSpacing;
    private final float mUnselectedZ;

    public ViewFocusAnimator(TaskCardView taskCardView) {
        this.mTargetView = taskCardView;
        Resources resources = taskCardView.getResources();
        this.mTargetView.setOnFocusChangeListener(this);
        TypedValue typedValue = new TypedValue();
        resources.getValue(2131755102, typedValue, true);
        this.mUnselectedScale = typedValue.getFloat();
        resources.getValue(2131755103, typedValue, true);
        this.mSelectedScale = typedValue.getFloat();
        this.mSelectedScaleDelta = this.mSelectedScale - this.mUnselectedScale;
        this.mUnselectedZ = resources.getDimensionPixelOffset(2131690060);
        this.mSelectedZDelta = resources.getDimensionPixelOffset(2131690061);
        this.mUnselectedSpacing = resources.getDimensionPixelOffset(2131690058);
        this.mSelectedSpacingDelta = resources.getDimensionPixelOffset(2131690059);
        this.mAnimDuration = resources.getInteger(2131755094);
        this.mFocusInterpolator = new AccelerateDecelerateInterpolator();
        this.mFocusAnimation = ObjectAnimator.ofFloat(this, "focusProgress", 0.0f);
        this.mFocusAnimation.setDuration(this.mAnimDuration);
        this.mFocusAnimation.setInterpolator(this.mFocusInterpolator);
        this.mDismissIconAlpha = resources.getFloat(2131755104);
        setFocusProgress(0.0f);
        this.mFocusAnimation.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.recents.tv.animations.ViewFocusAnimator.1
            final ViewFocusAnimator this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mTargetView.setHasTransientState(false);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.this$0.mTargetView.setHasTransientState(true);
            }
        });
    }

    private void animateFocus(boolean z) {
        if (this.mFocusAnimation.isStarted()) {
            this.mFocusAnimation.cancel();
        }
        float f = z ? 1.0f : 0.0f;
        if (this.mFocusProgress != f) {
            this.mFocusAnimation.setFloatValues(this.mFocusProgress, f);
            this.mFocusAnimation.start();
        }
    }

    private void setFocusProgress(float f) {
        this.mFocusProgress = f;
        float f2 = this.mUnselectedScale + (this.mSelectedScaleDelta * f);
        float f3 = this.mUnselectedZ + (this.mSelectedZDelta * f);
        float f4 = this.mUnselectedSpacing + (this.mSelectedSpacingDelta * f);
        this.mTargetView.setScaleX(f2);
        this.mTargetView.setScaleY(f2);
        this.mTargetView.setPadding((int) f4, this.mTargetView.getPaddingTop(), (int) f4, this.mTargetView.getPaddingBottom());
        this.mTargetView.getDismissIconView().setAlpha(this.mDismissIconAlpha * f);
        this.mTargetView.getThumbnailView().setZ(f3);
        this.mTargetView.getDismissIconView().setZ(f3);
    }

    public void changeSize(boolean z) {
        ViewGroup.LayoutParams layoutParams = this.mTargetView.getLayoutParams();
        int i = layoutParams.width;
        int i2 = layoutParams.height;
        if (i < 0 && i2 < 0) {
            this.mTargetView.measure(0, 0);
        }
        if (this.mTargetView.isAttachedToWindow() && this.mTargetView.hasWindowFocus() && this.mTargetView.getVisibility() == 0) {
            animateFocus(z);
            return;
        }
        if (this.mFocusAnimation.isStarted()) {
            this.mFocusAnimation.cancel();
        }
        setFocusProgress(z ? 1.0f : 0.0f);
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        if (view != this.mTargetView) {
            return;
        }
        changeSize(z);
    }
}
