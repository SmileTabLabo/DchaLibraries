package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
/* loaded from: classes.dex */
public class FirstFrameAnimatorHelper extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private static final boolean DEBUG = false;
    private static final int MAX_DELAY = 1000;
    private static final String TAG = "FirstFrameAnimatorHlpr";
    private static ViewTreeObserver.OnDrawListener sGlobalDrawListener;
    static long sGlobalFrameCounter;
    private static boolean sVisible;
    private boolean mAdjustedSecondFrameTime;
    private boolean mHandlingOnAnimationUpdate;
    private long mStartFrame;
    private long mStartTime = -1;
    private final View mTarget;

    public FirstFrameAnimatorHelper(ValueAnimator valueAnimator, View view) {
        this.mTarget = view;
        valueAnimator.addUpdateListener(this);
    }

    public FirstFrameAnimatorHelper(ViewPropertyAnimator viewPropertyAnimator, View view) {
        this.mTarget = view;
        viewPropertyAnimator.setListener(this);
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animator) {
        ValueAnimator valueAnimator = (ValueAnimator) animator;
        valueAnimator.addUpdateListener(this);
        onAnimationUpdate(valueAnimator);
    }

    public static void setIsVisible(boolean z) {
        sVisible = z;
    }

    public static void initializeDrawListener(View view) {
        if (sGlobalDrawListener != null) {
            view.getViewTreeObserver().removeOnDrawListener(sGlobalDrawListener);
        }
        sGlobalDrawListener = new ViewTreeObserver.OnDrawListener() { // from class: com.android.launcher3.-$$Lambda$FirstFrameAnimatorHelper$Rt9GS5WQ2aT33ZHWNuz2uhuk63s
            @Override // android.view.ViewTreeObserver.OnDrawListener
            public final void onDraw() {
                FirstFrameAnimatorHelper.lambda$initializeDrawListener$0();
            }
        };
        view.getViewTreeObserver().addOnDrawListener(sGlobalDrawListener);
        sVisible = true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$initializeDrawListener$0() {
        sGlobalFrameCounter++;
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(final ValueAnimator valueAnimator) {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.mStartTime == -1) {
            this.mStartFrame = sGlobalFrameCounter;
            this.mStartTime = currentTimeMillis;
        }
        long currentPlayTime = valueAnimator.getCurrentPlayTime();
        boolean z = Float.compare(1.0f, valueAnimator.getAnimatedFraction()) == 0;
        if (!this.mHandlingOnAnimationUpdate && sVisible && currentPlayTime < valueAnimator.getDuration() && !z) {
            this.mHandlingOnAnimationUpdate = true;
            long j = sGlobalFrameCounter - this.mStartFrame;
            if (j == 0 && currentTimeMillis < this.mStartTime + 1000 && currentPlayTime > 0) {
                this.mTarget.getRootView().invalidate();
                valueAnimator.setCurrentPlayTime(0L);
            } else {
                int i = (j > 1L ? 1 : (j == 1L ? 0 : -1));
                if (i == 0 && currentTimeMillis < this.mStartTime + 1000 && !this.mAdjustedSecondFrameTime && currentTimeMillis > this.mStartTime + 16 && currentPlayTime > 16) {
                    valueAnimator.setCurrentPlayTime(16L);
                    this.mAdjustedSecondFrameTime = true;
                } else if (i > 0) {
                    this.mTarget.post(new Runnable() { // from class: com.android.launcher3.FirstFrameAnimatorHelper.1
                        @Override // java.lang.Runnable
                        public void run() {
                            valueAnimator.removeUpdateListener(FirstFrameAnimatorHelper.this);
                        }
                    });
                }
            }
            this.mHandlingOnAnimationUpdate = false;
        }
    }

    public void print(ValueAnimator valueAnimator) {
        Log.d(TAG, sGlobalFrameCounter + "(" + (sGlobalFrameCounter - this.mStartFrame) + ") " + this.mTarget + " dirty? " + this.mTarget.isDirty() + " " + (((float) valueAnimator.getCurrentPlayTime()) / ((float) valueAnimator.getDuration())) + " " + this + " " + valueAnimator);
    }
}
