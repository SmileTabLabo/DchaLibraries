package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
/* loaded from: a.zip:com/android/launcher3/FirstFrameAnimatorHelper.class */
public class FirstFrameAnimatorHelper extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private static ViewTreeObserver.OnDrawListener sGlobalDrawListener;
    static long sGlobalFrameCounter;
    private static boolean sVisible;
    private boolean mAdjustedSecondFrameTime;
    private boolean mHandlingOnAnimationUpdate;
    private long mStartFrame;
    private long mStartTime = -1;
    private View mTarget;

    public FirstFrameAnimatorHelper(ValueAnimator valueAnimator, View view) {
        this.mTarget = view;
        valueAnimator.addUpdateListener(this);
    }

    public FirstFrameAnimatorHelper(ViewPropertyAnimator viewPropertyAnimator, View view) {
        this.mTarget = view;
        viewPropertyAnimator.setListener(this);
    }

    public static void initializeDrawListener(View view) {
        if (sGlobalDrawListener != null) {
            view.getViewTreeObserver().removeOnDrawListener(sGlobalDrawListener);
        }
        sGlobalDrawListener = new ViewTreeObserver.OnDrawListener() { // from class: com.android.launcher3.FirstFrameAnimatorHelper.1
            private long mTime = System.currentTimeMillis();

            @Override // android.view.ViewTreeObserver.OnDrawListener
            public void onDraw() {
                FirstFrameAnimatorHelper.sGlobalFrameCounter++;
            }
        };
        view.getViewTreeObserver().addOnDrawListener(sGlobalDrawListener);
        sVisible = true;
    }

    public static void setIsVisible(boolean z) {
        sVisible = z;
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animator) {
        ValueAnimator valueAnimator = (ValueAnimator) animator;
        valueAnimator.addUpdateListener(this);
        onAnimationUpdate(valueAnimator);
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.mStartTime == -1) {
            this.mStartFrame = sGlobalFrameCounter;
            this.mStartTime = currentTimeMillis;
        }
        long currentPlayTime = valueAnimator.getCurrentPlayTime();
        boolean z = Float.compare(1.0f, valueAnimator.getAnimatedFraction()) == 0;
        if (this.mHandlingOnAnimationUpdate || !sVisible || currentPlayTime >= valueAnimator.getDuration() || z) {
            return;
        }
        this.mHandlingOnAnimationUpdate = true;
        long j = sGlobalFrameCounter - this.mStartFrame;
        if (j == 0 && currentTimeMillis < this.mStartTime + 1000 && currentPlayTime > 0) {
            this.mTarget.getRootView().invalidate();
            valueAnimator.setCurrentPlayTime(0L);
        } else if (j == 1 && currentTimeMillis < this.mStartTime + 1000 && !this.mAdjustedSecondFrameTime && currentTimeMillis > this.mStartTime + 16 && currentPlayTime > 16) {
            valueAnimator.setCurrentPlayTime(16L);
            this.mAdjustedSecondFrameTime = true;
        } else if (j > 1) {
            this.mTarget.post(new Runnable(this, valueAnimator) { // from class: com.android.launcher3.FirstFrameAnimatorHelper.2
                final FirstFrameAnimatorHelper this$0;
                final ValueAnimator val$animation;

                {
                    this.this$0 = this;
                    this.val$animation = valueAnimator;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$animation.removeUpdateListener(this.this$0);
                }
            });
        }
        this.mHandlingOnAnimationUpdate = false;
    }
}
