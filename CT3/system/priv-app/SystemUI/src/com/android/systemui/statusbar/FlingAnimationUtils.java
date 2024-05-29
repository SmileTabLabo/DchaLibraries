package com.android.systemui.statusbar;

import android.animation.Animator;
import android.content.Context;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/statusbar/FlingAnimationUtils.class */
public class FlingAnimationUtils {
    private float mHighVelocityPxPerSecond;
    private float mMaxLengthSeconds;
    private float mMinVelocityPxPerSecond;
    private AnimatorProperties mAnimatorProperties = new AnimatorProperties(null);
    private Interpolator mLinearOutSlowIn = new PathInterpolator(0.0f, 0.0f, 0.35f, 1.0f);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/FlingAnimationUtils$AnimatorProperties.class */
    public static class AnimatorProperties {
        long duration;
        Interpolator interpolator;

        private AnimatorProperties() {
        }

        /* synthetic */ AnimatorProperties(AnimatorProperties animatorProperties) {
            this();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/FlingAnimationUtils$InterpolatorInterpolator.class */
    public static final class InterpolatorInterpolator implements Interpolator {
        private Interpolator mCrossfader;
        private Interpolator mInterpolator1;
        private Interpolator mInterpolator2;

        InterpolatorInterpolator(Interpolator interpolator, Interpolator interpolator2, Interpolator interpolator3) {
            this.mInterpolator1 = interpolator;
            this.mInterpolator2 = interpolator2;
            this.mCrossfader = interpolator3;
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float interpolation = this.mCrossfader.getInterpolation(f);
            return ((1.0f - interpolation) * this.mInterpolator1.getInterpolation(f)) + (this.mInterpolator2.getInterpolation(f) * interpolation);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/FlingAnimationUtils$VelocityInterpolator.class */
    public static final class VelocityInterpolator implements Interpolator {
        private float mDiff;
        private float mDurationSeconds;
        private float mVelocity;

        private VelocityInterpolator(float f, float f2, float f3) {
            this.mDurationSeconds = f;
            this.mVelocity = f2;
            this.mDiff = f3;
        }

        /* synthetic */ VelocityInterpolator(float f, float f2, float f3, VelocityInterpolator velocityInterpolator) {
            this(f, f2, f3);
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return (this.mVelocity * (f * this.mDurationSeconds)) / this.mDiff;
        }
    }

    public FlingAnimationUtils(Context context, float f) {
        this.mMaxLengthSeconds = f;
        this.mMinVelocityPxPerSecond = context.getResources().getDisplayMetrics().density * 250.0f;
        this.mHighVelocityPxPerSecond = context.getResources().getDisplayMetrics().density * 3000.0f;
    }

    private float calculateLinearOutFasterInY2(float f) {
        float max = Math.max(0.0f, Math.min(1.0f, (f - this.mMinVelocityPxPerSecond) / (this.mHighVelocityPxPerSecond - this.mMinVelocityPxPerSecond)));
        return ((1.0f - max) * 0.4f) + (0.5f * max);
    }

    private AnimatorProperties getDismissingProperties(float f, float f2, float f3, float f4) {
        float pow = (float) (this.mMaxLengthSeconds * Math.pow(Math.abs(f2 - f) / f4, 0.5d));
        float abs = Math.abs(f2 - f);
        float abs2 = Math.abs(f3);
        float calculateLinearOutFasterInY2 = calculateLinearOutFasterInY2(abs2);
        float f5 = calculateLinearOutFasterInY2 / 0.5f;
        PathInterpolator pathInterpolator = new PathInterpolator(0.0f, 0.0f, 0.5f, calculateLinearOutFasterInY2);
        float f6 = (f5 * abs) / abs2;
        if (f6 <= pow) {
            this.mAnimatorProperties.interpolator = pathInterpolator;
        } else if (abs2 >= this.mMinVelocityPxPerSecond) {
            f6 = pow;
            this.mAnimatorProperties.interpolator = new InterpolatorInterpolator(new VelocityInterpolator(pow, abs2, abs, null), pathInterpolator, this.mLinearOutSlowIn);
        } else {
            f6 = pow;
            this.mAnimatorProperties.interpolator = Interpolators.FAST_OUT_LINEAR_IN;
        }
        this.mAnimatorProperties.duration = 1000.0f * f6;
        return this.mAnimatorProperties;
    }

    private AnimatorProperties getProperties(float f, float f2, float f3, float f4) {
        float sqrt = (float) (this.mMaxLengthSeconds * Math.sqrt(Math.abs(f2 - f) / f4));
        float abs = Math.abs(f2 - f);
        float abs2 = Math.abs(f3);
        float f5 = (2.857143f * abs) / abs2;
        if (f5 <= sqrt) {
            this.mAnimatorProperties.interpolator = this.mLinearOutSlowIn;
        } else if (abs2 >= this.mMinVelocityPxPerSecond) {
            f5 = sqrt;
            this.mAnimatorProperties.interpolator = new InterpolatorInterpolator(new VelocityInterpolator(sqrt, abs2, abs, null), this.mLinearOutSlowIn, this.mLinearOutSlowIn);
        } else {
            f5 = sqrt;
            this.mAnimatorProperties.interpolator = Interpolators.FAST_OUT_SLOW_IN;
        }
        this.mAnimatorProperties.duration = 1000.0f * f5;
        return this.mAnimatorProperties;
    }

    public void apply(Animator animator, float f, float f2, float f3) {
        apply(animator, f, f2, f3, Math.abs(f2 - f));
    }

    public void apply(Animator animator, float f, float f2, float f3, float f4) {
        AnimatorProperties properties = getProperties(f, f2, f3, f4);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    public void apply(ViewPropertyAnimator viewPropertyAnimator, float f, float f2, float f3) {
        apply(viewPropertyAnimator, f, f2, f3, Math.abs(f2 - f));
    }

    public void apply(ViewPropertyAnimator viewPropertyAnimator, float f, float f2, float f3, float f4) {
        AnimatorProperties properties = getProperties(f, f2, f3, f4);
        viewPropertyAnimator.setDuration(properties.duration);
        viewPropertyAnimator.setInterpolator(properties.interpolator);
    }

    public void applyDismissing(Animator animator, float f, float f2, float f3, float f4) {
        AnimatorProperties dismissingProperties = getDismissingProperties(f, f2, f3, f4);
        animator.setDuration(dismissingProperties.duration);
        animator.setInterpolator(dismissingProperties.interpolator);
    }

    public float getMinVelocityPxPerSecond() {
        return this.mMinVelocityPxPerSecond;
    }
}
