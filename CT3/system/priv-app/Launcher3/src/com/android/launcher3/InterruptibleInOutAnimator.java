package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
/* loaded from: a.zip:com/android/launcher3/InterruptibleInOutAnimator.class */
public class InterruptibleInOutAnimator {
    private ValueAnimator mAnimator;
    private long mOriginalDuration;
    private float mOriginalFromValue;
    private float mOriginalToValue;
    private boolean mFirstRun = true;
    private Object mTag = null;
    int mDirection = 0;

    public InterruptibleInOutAnimator(View view, long j, float f, float f2) {
        this.mAnimator = LauncherAnimUtils.ofFloat(view, f, f2).setDuration(j);
        this.mOriginalDuration = j;
        this.mOriginalFromValue = f;
        this.mOriginalToValue = f2;
        this.mAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.launcher3.InterruptibleInOutAnimator.1
            final InterruptibleInOutAnimator this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mDirection = 0;
            }
        });
    }

    private void animate(int i) {
        long currentPlayTime = this.mAnimator.getCurrentPlayTime();
        float f = i == 1 ? this.mOriginalToValue : this.mOriginalFromValue;
        float floatValue = this.mFirstRun ? this.mOriginalFromValue : ((Float) this.mAnimator.getAnimatedValue()).floatValue();
        cancel();
        this.mDirection = i;
        this.mAnimator.setDuration(Math.max(0L, Math.min(this.mOriginalDuration - currentPlayTime, this.mOriginalDuration)));
        this.mAnimator.setFloatValues(floatValue, f);
        this.mAnimator.start();
        this.mFirstRun = false;
    }

    public void animateIn() {
        animate(1);
    }

    public void animateOut() {
        animate(2);
    }

    public void cancel() {
        this.mAnimator.cancel();
        this.mDirection = 0;
    }

    public ValueAnimator getAnimator() {
        return this.mAnimator;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object obj) {
        this.mTag = obj;
    }
}
