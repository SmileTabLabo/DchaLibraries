package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/DozeScrimController.class */
public class DozeScrimController {
    private static final boolean DEBUG = Log.isLoggable("DozeScrimController", 3);
    private Animator mBehindAnimator;
    private float mBehindTarget;
    private final DozeParameters mDozeParameters;
    private boolean mDozing;
    private Animator mInFrontAnimator;
    private float mInFrontTarget;
    private DozeHost.PulseCallback mPulseCallback;
    private int mPulseReason;
    private final ScrimController mScrimController;
    private final Handler mHandler = new Handler();
    private final Runnable mPulseIn = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.DozeScrimController.1
        final DozeScrimController this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse in, mDozing=" + this.this$0.mDozing + " mPulseReason=" + DozeLog.pulseReasonToString(this.this$0.mPulseReason));
            }
            if (this.this$0.mDozing) {
                DozeLog.tracePulseStart(this.this$0.mPulseReason);
                this.this$0.pulseStarted();
            }
        }
    };
    private final Runnable mPulseInFinished = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.DozeScrimController.2
        final DozeScrimController this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse in finished, mDozing=" + this.this$0.mDozing);
            }
            if (this.this$0.mDozing) {
                this.this$0.mHandler.postDelayed(this.this$0.mPulseOut, this.this$0.mDozeParameters.getPulseVisibleDuration());
            }
        }
    };
    private final Runnable mPulseOut = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.DozeScrimController.3
        final DozeScrimController this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse out, mDozing=" + this.this$0.mDozing);
            }
            if (this.this$0.mDozing) {
                this.this$0.startScrimAnimation(true, 1.0f, this.this$0.mDozeParameters.getPulseOutDuration(), Interpolators.ALPHA_IN, this.this$0.mPulseOutFinished);
            }
        }
    };
    private final Runnable mPulseOutFinished = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.DozeScrimController.4
        final DozeScrimController this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse out finished");
            }
            DozeLog.tracePulseFinish();
            this.this$0.pulseFinished();
        }
    };

    public DozeScrimController(ScrimController scrimController, Context context) {
        this.mScrimController = scrimController;
        this.mDozeParameters = new DozeParameters(context);
    }

    private void abortAnimations() {
        if (this.mInFrontAnimator != null) {
            this.mInFrontAnimator.cancel();
        }
        if (this.mBehindAnimator != null) {
            this.mBehindAnimator.cancel();
        }
    }

    private void cancelPulsing() {
        if (DEBUG) {
            Log.d("DozeScrimController", "Cancel pulsing");
        }
        if (this.mPulseCallback != null) {
            this.mHandler.removeCallbacks(this.mPulseIn);
            this.mHandler.removeCallbacks(this.mPulseOut);
            pulseFinished();
        }
    }

    private Animator getCurrentAnimator(boolean z) {
        return z ? this.mInFrontAnimator : this.mBehindAnimator;
    }

    private float getCurrentTarget(boolean z) {
        return z ? this.mInFrontTarget : this.mBehindTarget;
    }

    private float getDozeAlpha(boolean z) {
        return z ? this.mScrimController.getDozeInFrontAlpha() : this.mScrimController.getDozeBehindAlpha();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pulseFinished() {
        if (this.mPulseCallback != null) {
            this.mPulseCallback.onPulseFinished();
            this.mPulseCallback = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pulseStarted() {
        if (this.mPulseCallback != null) {
            this.mPulseCallback.onPulseStarted();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCurrentAnimator(boolean z, Animator animator) {
        if (z) {
            this.mInFrontAnimator = animator;
        } else {
            this.mBehindAnimator = animator;
        }
    }

    private void setCurrentTarget(boolean z, float f) {
        if (z) {
            this.mInFrontTarget = f;
        } else {
            this.mBehindTarget = f;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDozeAlpha(boolean z, float f) {
        if (z) {
            this.mScrimController.setDozeInFrontAlpha(f);
        } else {
            this.mScrimController.setDozeBehindAlpha(f);
        }
    }

    private void startScrimAnimation(boolean z, float f, long j, Interpolator interpolator) {
        startScrimAnimation(z, f, j, interpolator, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startScrimAnimation(boolean z, float f, long j, Interpolator interpolator, Runnable runnable) {
        Animator currentAnimator = getCurrentAnimator(z);
        if (currentAnimator != null) {
            if (getCurrentTarget(z) == f) {
                return;
            }
            currentAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(getDozeAlpha(z), f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, z) { // from class: com.android.systemui.statusbar.phone.DozeScrimController.5
            final DozeScrimController this$0;
            final boolean val$inFront;

            {
                this.this$0 = this;
                this.val$inFront = z;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setDozeAlpha(this.val$inFront, ((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(j);
        ofFloat.addListener(new AnimatorListenerAdapter(this, z, runnable) { // from class: com.android.systemui.statusbar.phone.DozeScrimController.6
            final DozeScrimController this$0;
            final Runnable val$endRunnable;
            final boolean val$inFront;

            {
                this.this$0 = this;
                this.val$inFront = z;
                this.val$endRunnable = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.setCurrentAnimator(this.val$inFront, null);
                if (this.val$endRunnable != null) {
                    this.val$endRunnable.run();
                }
            }
        });
        ofFloat.start();
        setCurrentAnimator(z, ofFloat);
        setCurrentTarget(z, f);
    }

    public void abortPulsing() {
        cancelPulsing();
        if (this.mDozing) {
            this.mScrimController.setDozeBehindAlpha(1.0f);
            this.mScrimController.setDozeInFrontAlpha(1.0f);
        }
    }

    public boolean isPulsing() {
        return this.mPulseCallback != null;
    }

    public void onScreenTurnedOn() {
        if (isPulsing()) {
            boolean z = this.mPulseReason == 3;
            startScrimAnimation(true, 0.0f, this.mDozeParameters.getPulseInDuration(z), z ? Interpolators.LINEAR_OUT_SLOW_IN : Interpolators.ALPHA_OUT, this.mPulseInFinished);
        }
    }

    public void pulse(DozeHost.PulseCallback pulseCallback, int i) {
        if (pulseCallback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        if (!this.mDozing || this.mPulseCallback != null) {
            pulseCallback.onPulseFinished();
            return;
        }
        this.mPulseCallback = pulseCallback;
        this.mPulseReason = i;
        this.mHandler.post(this.mPulseIn);
    }

    public void setDozing(boolean z, boolean z2) {
        if (this.mDozing == z) {
            return;
        }
        this.mDozing = z;
        if (this.mDozing) {
            abortAnimations();
            this.mScrimController.setDozeBehindAlpha(1.0f);
            this.mScrimController.setDozeInFrontAlpha(1.0f);
            return;
        }
        cancelPulsing();
        if (z2) {
            startScrimAnimation(false, 0.0f, 700L, Interpolators.LINEAR_OUT_SLOW_IN);
            startScrimAnimation(true, 0.0f, 700L, Interpolators.LINEAR_OUT_SLOW_IN);
            return;
        }
        abortAnimations();
        this.mScrimController.setDozeBehindAlpha(0.0f);
        this.mScrimController.setDozeInFrontAlpha(0.0f);
    }
}
