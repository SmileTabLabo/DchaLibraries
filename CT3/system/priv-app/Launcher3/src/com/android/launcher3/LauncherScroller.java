package com.android.launcher3;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.launcher3.compat.PackageInstallerCompat;
/* loaded from: a.zip:com/android/launcher3/LauncherScroller.class */
public class LauncherScroller {
    private static float DECELERATION_RATE = (float) (Math.log(0.78d) / Math.log(0.9d));
    private static final float[] SPLINE_POSITION = new float[101];
    private static final float[] SPLINE_TIME = new float[101];
    private static float sViscousFluidNormalize;
    private static float sViscousFluidScale;
    private float mCurrVelocity;
    private int mCurrX;
    private int mCurrY;
    private float mDeceleration;
    private float mDeltaX;
    private float mDeltaY;
    private int mDistance;
    private int mDuration;
    private float mDurationReciprocal;
    private int mFinalX;
    private int mFinalY;
    private boolean mFinished;
    private float mFlingFriction;
    private boolean mFlywheel;
    private TimeInterpolator mInterpolator;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    private int mMode;
    private float mPhysicalCoeff;
    private final float mPpi;
    private long mStartTime;
    private int mStartX;
    private int mStartY;
    private float mVelocity;

    static {
        float f;
        float f2;
        float f3;
        float f4;
        float f5 = 0.0f;
        float f6 = 0.0f;
        for (int i = 0; i < 100; i++) {
            float f7 = i / 100.0f;
            float f8 = 1.0f;
            while (true) {
                f = f5 + ((f8 - f5) / 2.0f);
                f2 = 3.0f * f * (1.0f - f);
                float f9 = ((((1.0f - f) * 0.175f) + (0.35000002f * f)) * f2) + (f * f * f);
                if (Math.abs(f9 - f7) < 1.0E-5d) {
                    break;
                } else if (f9 > f7) {
                    f8 = f;
                } else {
                    f5 = f;
                }
            }
            SPLINE_POSITION[i] = ((((1.0f - f) * 0.5f) + f) * f2) + (f * f * f);
            float f10 = 1.0f;
            while (true) {
                f3 = f6 + ((f10 - f6) / 2.0f);
                f4 = 3.0f * f3 * (1.0f - f3);
                float f11 = ((((1.0f - f3) * 0.5f) + f3) * f4) + (f3 * f3 * f3);
                if (Math.abs(f11 - f7) < 1.0E-5d) {
                    break;
                } else if (f11 > f7) {
                    f10 = f3;
                } else {
                    f6 = f3;
                }
            }
            SPLINE_TIME[i] = ((((1.0f - f3) * 0.175f) + (0.35000002f * f3)) * f4) + (f3 * f3 * f3);
        }
        float[] fArr = SPLINE_POSITION;
        SPLINE_TIME[100] = 1.0f;
        fArr[100] = 1.0f;
        sViscousFluidScale = 8.0f;
        sViscousFluidNormalize = 1.0f;
        sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
    }

    public LauncherScroller(Context context) {
        this(context, null);
    }

    public LauncherScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, context.getApplicationInfo().targetSdkVersion >= 11);
    }

    public LauncherScroller(Context context, Interpolator interpolator, boolean z) {
        this.mFlingFriction = ViewConfiguration.getScrollFriction();
        this.mFinished = true;
        this.mInterpolator = interpolator;
        this.mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        this.mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        this.mFlywheel = z;
        this.mPhysicalCoeff = computeDeceleration(0.84f);
    }

    private float computeDeceleration(float f) {
        return this.mPpi * 386.0878f * f;
    }

    private double getSplineDeceleration(float f) {
        return Math.log((Math.abs(f) * 0.35f) / (this.mFlingFriction * this.mPhysicalCoeff));
    }

    private double getSplineFlingDistance(float f) {
        return this.mFlingFriction * this.mPhysicalCoeff * Math.exp((DECELERATION_RATE / (DECELERATION_RATE - 1.0d)) * getSplineDeceleration(f));
    }

    private int getSplineFlingDuration(float f) {
        return (int) (Math.exp(getSplineDeceleration(f) / (DECELERATION_RATE - 1.0d)) * 1000.0d);
    }

    static float viscousFluid(float f) {
        float f2 = f * sViscousFluidScale;
        return (f2 < 1.0f ? f2 - (1.0f - ((float) Math.exp(-f2))) : 0.36787945f + (0.63212055f * (1.0f - ((float) Math.exp(1.0f - f2))))) * sViscousFluidNormalize;
    }

    public void abortAnimation() {
        this.mCurrX = this.mFinalX;
        this.mCurrY = this.mFinalY;
        this.mFinished = true;
    }

    public boolean computeScrollOffset() {
        if (this.mFinished) {
            return false;
        }
        int currentAnimationTimeMillis = (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
        if (currentAnimationTimeMillis >= this.mDuration) {
            this.mCurrX = this.mFinalX;
            this.mCurrY = this.mFinalY;
            this.mFinished = true;
            return true;
        }
        switch (this.mMode) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                float f = currentAnimationTimeMillis * this.mDurationReciprocal;
                float viscousFluid = this.mInterpolator == null ? viscousFluid(f) : this.mInterpolator.getInterpolation(f);
                this.mCurrX = this.mStartX + Math.round(this.mDeltaX * viscousFluid);
                this.mCurrY = this.mStartY + Math.round(this.mDeltaY * viscousFluid);
                return true;
            case 1:
                float f2 = currentAnimationTimeMillis / this.mDuration;
                int i = (int) (100.0f * f2);
                float f3 = 1.0f;
                float f4 = 0.0f;
                if (i < 100) {
                    float f5 = i / 100.0f;
                    float f6 = (i + 1) / 100.0f;
                    float f7 = SPLINE_POSITION[i];
                    f4 = (SPLINE_POSITION[i + 1] - f7) / (f6 - f5);
                    f3 = f7 + ((f2 - f5) * f4);
                }
                this.mCurrVelocity = ((this.mDistance * f4) / this.mDuration) * 1000.0f;
                this.mCurrX = this.mStartX + Math.round((this.mFinalX - this.mStartX) * f3);
                this.mCurrX = Math.min(this.mCurrX, this.mMaxX);
                this.mCurrX = Math.max(this.mCurrX, this.mMinX);
                this.mCurrY = this.mStartY + Math.round((this.mFinalY - this.mStartY) * f3);
                this.mCurrY = Math.min(this.mCurrY, this.mMaxY);
                this.mCurrY = Math.max(this.mCurrY, this.mMinY);
                if (this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) {
                    this.mFinished = true;
                    return true;
                }
                return true;
            default:
                return true;
        }
    }

    public void fling(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9 = i3;
        int i10 = i4;
        if (this.mFlywheel) {
            if (this.mFinished) {
                i10 = i4;
                i9 = i3;
            } else {
                float currVelocity = getCurrVelocity();
                float f = this.mFinalX - this.mStartX;
                float f2 = this.mFinalY - this.mStartY;
                float hypot = (float) Math.hypot(f, f2);
                float f3 = f / hypot;
                float f4 = f2 / hypot;
                float f5 = f3 * currVelocity;
                float f6 = f4 * currVelocity;
                i9 = i3;
                i10 = i4;
                if (Math.signum(i3) == Math.signum(f5)) {
                    i9 = i3;
                    i10 = i4;
                    if (Math.signum(i4) == Math.signum(f6)) {
                        i9 = (int) (i3 + f5);
                        i10 = (int) (i4 + f6);
                    }
                }
            }
        }
        this.mMode = 1;
        this.mFinished = false;
        float hypot2 = (float) Math.hypot(i9, i10);
        this.mVelocity = hypot2;
        this.mDuration = getSplineFlingDuration(hypot2);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = i;
        this.mStartY = i2;
        float f7 = hypot2 == 0.0f ? 1.0f : i9 / hypot2;
        float f8 = hypot2 == 0.0f ? 1.0f : i10 / hypot2;
        double splineFlingDistance = getSplineFlingDistance(hypot2);
        this.mDistance = (int) (Math.signum(hypot2) * splineFlingDistance);
        this.mMinX = i5;
        this.mMaxX = i6;
        this.mMinY = i7;
        this.mMaxY = i8;
        this.mFinalX = ((int) Math.round(f7 * splineFlingDistance)) + i;
        this.mFinalX = Math.min(this.mFinalX, this.mMaxX);
        this.mFinalX = Math.max(this.mFinalX, this.mMinX);
        this.mFinalY = ((int) Math.round(f8 * splineFlingDistance)) + i2;
        this.mFinalY = Math.min(this.mFinalY, this.mMaxY);
        this.mFinalY = Math.max(this.mFinalY, this.mMinY);
    }

    public final void forceFinished(boolean z) {
        this.mFinished = z;
    }

    public float getCurrVelocity() {
        return this.mMode == 1 ? this.mCurrVelocity : this.mVelocity - ((this.mDeceleration * timePassed()) / 2000.0f);
    }

    public final int getCurrX() {
        return this.mCurrX;
    }

    public final int getCurrY() {
        return this.mCurrY;
    }

    public final int getFinalX() {
        return this.mFinalX;
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public void setFinalX(int i) {
        this.mFinalX = i;
        this.mDeltaX = this.mFinalX - this.mStartX;
        this.mFinished = false;
    }

    public void setInterpolator(TimeInterpolator timeInterpolator) {
        this.mInterpolator = timeInterpolator;
    }

    public void startScroll(int i, int i2, int i3, int i4, int i5) {
        this.mMode = 0;
        this.mFinished = false;
        this.mDuration = i5;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = i;
        this.mStartY = i2;
        this.mFinalX = i + i3;
        this.mFinalY = i2 + i4;
        this.mDeltaX = i3;
        this.mDeltaY = i4;
        this.mDurationReciprocal = 1.0f / this.mDuration;
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
    }
}
