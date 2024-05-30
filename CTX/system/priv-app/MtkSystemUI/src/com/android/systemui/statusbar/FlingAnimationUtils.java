package com.android.systemui.statusbar;

import android.animation.Animator;
import android.content.Context;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.NotificationUtils;
/* loaded from: classes.dex */
public class FlingAnimationUtils {
    private AnimatorProperties mAnimatorProperties;
    private float mCachedStartGradient;
    private float mCachedVelocityFactor;
    private float mHighVelocityPxPerSecond;
    private PathInterpolator mInterpolator;
    private float mLinearOutSlowInX2;
    private float mMaxLengthSeconds;
    private float mMinVelocityPxPerSecond;
    private final float mSpeedUpFactor;
    private final float mY2;

    public FlingAnimationUtils(Context context, float f) {
        this(context, f, 0.0f);
    }

    public FlingAnimationUtils(Context context, float f, float f2) {
        this(context, f, f2, -1.0f, 1.0f);
    }

    public FlingAnimationUtils(Context context, float f, float f2, float f3, float f4) {
        this.mAnimatorProperties = new AnimatorProperties();
        this.mCachedStartGradient = -1.0f;
        this.mCachedVelocityFactor = -1.0f;
        this.mMaxLengthSeconds = f;
        this.mSpeedUpFactor = f2;
        if (f3 < 0.0f) {
            this.mLinearOutSlowInX2 = NotificationUtils.interpolate(0.35f, 0.68f, this.mSpeedUpFactor);
        } else {
            this.mLinearOutSlowInX2 = f3;
        }
        this.mY2 = f4;
        this.mMinVelocityPxPerSecond = 250.0f * context.getResources().getDisplayMetrics().density;
        this.mHighVelocityPxPerSecond = 3000.0f * context.getResources().getDisplayMetrics().density;
    }

    public void apply(Animator animator, float f, float f2, float f3) {
        apply(animator, f, f2, f3, Math.abs(f2 - f));
    }

    public void apply(ViewPropertyAnimator viewPropertyAnimator, float f, float f2, float f3) {
        apply(viewPropertyAnimator, f, f2, f3, Math.abs(f2 - f));
    }

    public void apply(Animator animator, float f, float f2, float f3, float f4) {
        AnimatorProperties properties = getProperties(f, f2, f3, f4);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    public void apply(ViewPropertyAnimator viewPropertyAnimator, float f, float f2, float f3, float f4) {
        AnimatorProperties properties = getProperties(f, f2, f3, f4);
        viewPropertyAnimator.setDuration(properties.duration);
        viewPropertyAnimator.setInterpolator(properties.interpolator);
    }

    private AnimatorProperties getProperties(float f, float f2, float f3, float f4) {
        float f5 = f2 - f;
        float sqrt = (float) (this.mMaxLengthSeconds * Math.sqrt(Math.abs(f5) / f4));
        float abs = Math.abs(f5);
        float abs2 = Math.abs(f3);
        float min = this.mSpeedUpFactor != 0.0f ? Math.min(abs2 / 3000.0f, 1.0f) : 1.0f;
        float interpolate = NotificationUtils.interpolate(0.75f, this.mY2 / this.mLinearOutSlowInX2, min);
        float f6 = (interpolate * abs) / abs2;
        Interpolator interpolator = getInterpolator(interpolate, min);
        if (f6 <= sqrt) {
            this.mAnimatorProperties.interpolator = interpolator;
            sqrt = f6;
        } else if (abs2 >= this.mMinVelocityPxPerSecond) {
            this.mAnimatorProperties.interpolator = new InterpolatorInterpolator(new VelocityInterpolator(sqrt, abs2, abs), interpolator, Interpolators.LINEAR_OUT_SLOW_IN);
        } else {
            this.mAnimatorProperties.interpolator = Interpolators.FAST_OUT_SLOW_IN;
        }
        this.mAnimatorProperties.duration = sqrt * 1000.0f;
        return this.mAnimatorProperties;
    }

    private Interpolator getInterpolator(float f, float f2) {
        if (f != this.mCachedStartGradient || f2 != this.mCachedVelocityFactor) {
            float f3 = this.mSpeedUpFactor * (1.0f - f2);
            this.mInterpolator = new PathInterpolator(f3, f3 * f, this.mLinearOutSlowInX2, this.mY2);
            this.mCachedStartGradient = f;
            this.mCachedVelocityFactor = f2;
        }
        return this.mInterpolator;
    }

    public void applyDismissing(Animator animator, float f, float f2, float f3, float f4) {
        AnimatorProperties dismissingProperties = getDismissingProperties(f, f2, f3, f4);
        animator.setDuration(dismissingProperties.duration);
        animator.setInterpolator(dismissingProperties.interpolator);
    }

    private AnimatorProperties getDismissingProperties(float f, float f2, float f3, float f4) {
        float f5 = f2 - f;
        float pow = (float) (this.mMaxLengthSeconds * Math.pow(Math.abs(f5) / f4, 0.5d));
        float abs = Math.abs(f5);
        float abs2 = Math.abs(f3);
        float calculateLinearOutFasterInY2 = calculateLinearOutFasterInY2(abs2);
        PathInterpolator pathInterpolator = new PathInterpolator(0.0f, 0.0f, 0.5f, calculateLinearOutFasterInY2);
        float f6 = ((calculateLinearOutFasterInY2 / 0.5f) * abs) / abs2;
        if (f6 <= pow) {
            this.mAnimatorProperties.interpolator = pathInterpolator;
            pow = f6;
        } else if (abs2 >= this.mMinVelocityPxPerSecond) {
            this.mAnimatorProperties.interpolator = new InterpolatorInterpolator(new VelocityInterpolator(pow, abs2, abs), pathInterpolator, Interpolators.LINEAR_OUT_SLOW_IN);
        } else {
            this.mAnimatorProperties.interpolator = Interpolators.FAST_OUT_LINEAR_IN;
        }
        this.mAnimatorProperties.duration = pow * 1000.0f;
        return this.mAnimatorProperties;
    }

    private float calculateLinearOutFasterInY2(float f) {
        float max = Math.max(0.0f, Math.min(1.0f, (f - this.mMinVelocityPxPerSecond) / (this.mHighVelocityPxPerSecond - this.mMinVelocityPxPerSecond)));
        return ((1.0f - max) * 0.4f) + (max * 0.5f);
    }

    public float getMinVelocityPxPerSecond() {
        return this.mMinVelocityPxPerSecond;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
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
            return ((1.0f - interpolation) * this.mInterpolator1.getInterpolation(f)) + (interpolation * this.mInterpolator2.getInterpolation(f));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class VelocityInterpolator implements Interpolator {
        private float mDiff;
        private float mDurationSeconds;
        private float mVelocity;

        private VelocityInterpolator(float f, float f2, float f3) {
            this.mDurationSeconds = f;
            this.mVelocity = f2;
            this.mDiff = f3;
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return ((f * this.mDurationSeconds) * this.mVelocity) / this.mDiff;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class AnimatorProperties {
        long duration;
        Interpolator interpolator;

        private AnimatorProperties() {
        }
    }
}
