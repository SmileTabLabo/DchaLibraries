package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
/* loaded from: a.zip:com/android/launcher3/AlphaUpdateListener.class */
class AlphaUpdateListener extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private boolean mAccessibilityEnabled;
    private View mView;

    public AlphaUpdateListener(View view, boolean z) {
        this.mView = view;
        this.mAccessibilityEnabled = z;
    }

    public static void updateVisibility(View view, boolean z) {
        int i = z ? 8 : 4;
        if (view.getAlpha() < 0.01f && view.getVisibility() != i) {
            view.setVisibility(i);
        } else if (view.getAlpha() <= 0.01f || view.getVisibility() == 0) {
        } else {
            view.setVisibility(0);
        }
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animator) {
        updateVisibility(this.mView, this.mAccessibilityEnabled);
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animator) {
        this.mView.setVisibility(0);
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        updateVisibility(this.mView, this.mAccessibilityEnabled);
    }
}
