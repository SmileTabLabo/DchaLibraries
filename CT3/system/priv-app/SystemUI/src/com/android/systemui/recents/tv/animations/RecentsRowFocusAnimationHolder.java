package com.android.systemui.recents.tv.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.view.View;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/recents/tv/animations/RecentsRowFocusAnimationHolder.class */
public class RecentsRowFocusAnimationHolder {
    private AnimatorSet mFocusGainAnimatorSet;
    private AnimatorSet mFocusLossAnimatorSet;
    private final View mTitleView;
    private final View mView;

    public RecentsRowFocusAnimationHolder(View view, View view2) {
        this.mView = view;
        this.mTitleView = view2;
        Resources resources = view.getResources();
        int integer = resources.getInteger(2131755097);
        float f = resources.getFloat(2131690027);
        this.mFocusGainAnimatorSet = new AnimatorSet();
        this.mFocusGainAnimatorSet.playTogether(ObjectAnimator.ofFloat(this.mView, "alpha", 1.0f), ObjectAnimator.ofFloat(this.mTitleView, "alpha", 1.0f));
        this.mFocusGainAnimatorSet.setDuration(integer);
        this.mFocusGainAnimatorSet.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mFocusLossAnimatorSet = new AnimatorSet();
        this.mFocusLossAnimatorSet.playTogether(ObjectAnimator.ofFloat(this.mView, "alpha", 1.0f, f), ObjectAnimator.ofFloat(this.mTitleView, "alpha", 0.0f));
        this.mFocusLossAnimatorSet.setDuration(integer);
        this.mFocusLossAnimatorSet.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
    }

    private static void cancelAnimator(Animator animator) {
        if (animator.isStarted()) {
            animator.cancel();
        }
    }

    public void reset() {
        cancelAnimator(this.mFocusLossAnimatorSet);
        cancelAnimator(this.mFocusGainAnimatorSet);
        this.mView.setAlpha(1.0f);
        this.mTitleView.setAlpha(1.0f);
    }

    public void startFocusGainAnimation() {
        cancelAnimator(this.mFocusLossAnimatorSet);
        this.mFocusGainAnimatorSet.start();
    }

    public void startFocusLossAnimation() {
        cancelAnimator(this.mFocusGainAnimatorSet);
        this.mFocusLossAnimatorSet.start();
    }
}
