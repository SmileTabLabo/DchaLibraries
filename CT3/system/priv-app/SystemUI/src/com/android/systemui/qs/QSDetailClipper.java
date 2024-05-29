package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.ViewAnimationUtils;
/* loaded from: a.zip:com/android/systemui/qs/QSDetailClipper.class */
public class QSDetailClipper {
    private Animator mAnimator;
    private final TransitionDrawable mBackground;
    private final View mDetail;
    private final Runnable mReverseBackground = new Runnable(this) { // from class: com.android.systemui.qs.QSDetailClipper.1
        final QSDetailClipper this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mAnimator != null) {
                this.this$0.mBackground.reverseTransition((int) (this.this$0.mAnimator.getDuration() * 0.35d));
            }
        }
    };
    private final AnimatorListenerAdapter mVisibleOnStart = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.QSDetailClipper.2
        final QSDetailClipper this$0;

        {
            this.this$0 = this;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            this.this$0.mAnimator = null;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            this.this$0.mDetail.setVisibility(0);
        }
    };
    private final AnimatorListenerAdapter mGoneOnEnd = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.QSDetailClipper.3
        final QSDetailClipper this$0;

        {
            this.this$0 = this;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            this.this$0.mDetail.setVisibility(8);
            this.this$0.mBackground.resetTransition();
            this.this$0.mAnimator = null;
        }
    };

    public QSDetailClipper(View view) {
        this.mDetail = view;
        this.mBackground = (TransitionDrawable) view.getBackground();
    }

    public void animateCircularClip(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        if (this.mAnimator != null) {
            this.mAnimator.cancel();
        }
        int width = this.mDetail.getWidth() - i;
        int height = this.mDetail.getHeight() - i2;
        int i3 = 0;
        if (i < 0 || width < 0 || i2 < 0 || height < 0) {
            i3 = Math.min(Math.min(Math.min(Math.abs(i), Math.abs(i2)), Math.abs(width)), Math.abs(height));
        }
        int max = (int) Math.max((int) Math.max((int) Math.max((int) Math.ceil(Math.sqrt((i * i) + (i2 * i2))), Math.ceil(Math.sqrt((width * width) + (i2 * i2)))), Math.ceil(Math.sqrt((width * width) + (height * height)))), Math.ceil(Math.sqrt((i * i) + (height * height))));
        if (z) {
            this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, i3, max);
        } else {
            this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, max, i3);
        }
        this.mAnimator.setDuration((long) (this.mAnimator.getDuration() * 1.5d));
        if (animatorListener != null) {
            this.mAnimator.addListener(animatorListener);
        }
        if (z) {
            this.mBackground.startTransition((int) (this.mAnimator.getDuration() * 0.6d));
            this.mAnimator.addListener(this.mVisibleOnStart);
        } else {
            this.mDetail.postDelayed(this.mReverseBackground, (long) (this.mAnimator.getDuration() * 0.65d));
            this.mAnimator.addListener(this.mGoneOnEnd);
        }
        this.mAnimator.start();
    }
}
