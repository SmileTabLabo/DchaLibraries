package com.android.launcher3.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
/* loaded from: a.zip:com/android/launcher3/util/LauncherEdgeEffect.class */
public class LauncherEdgeEffect {
    private float mBaseGlowScale;
    private float mDuration;
    private float mGlowAlpha;
    private float mGlowAlphaFinish;
    private float mGlowAlphaStart;
    private float mGlowScaleY;
    private float mGlowScaleYFinish;
    private float mGlowScaleYStart;
    private final Interpolator mInterpolator;
    private float mPullDistance;
    private float mRadius;
    private long mStartTime;
    private static final float SIN = (float) Math.sin(0.5235987755982988d);
    private static final float COS = (float) Math.cos(0.5235987755982988d);
    private int mState = 0;
    private final Rect mBounds = new Rect();
    private final Paint mPaint = new Paint();
    private float mDisplacement = 0.5f;
    private float mTargetDisplacement = 0.5f;

    public LauncherEdgeEffect() {
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mInterpolator = new DecelerateInterpolator();
    }

    private void update() {
        float min = Math.min(((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / this.mDuration, 1.0f);
        float interpolation = this.mInterpolator.getInterpolation(min);
        this.mGlowAlpha = this.mGlowAlphaStart + ((this.mGlowAlphaFinish - this.mGlowAlphaStart) * interpolation);
        this.mGlowScaleY = this.mGlowScaleYStart + ((this.mGlowScaleYFinish - this.mGlowScaleYStart) * interpolation);
        this.mDisplacement = (this.mDisplacement + this.mTargetDisplacement) / 2.0f;
        if (min >= 0.999f) {
            switch (this.mState) {
                case 1:
                    this.mState = 4;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 2000.0f;
                    this.mGlowAlphaStart = this.mGlowAlpha;
                    this.mGlowScaleYStart = this.mGlowScaleY;
                    this.mGlowAlphaFinish = 0.0f;
                    this.mGlowScaleYFinish = 0.0f;
                    return;
                case 2:
                    this.mState = 3;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 600.0f;
                    this.mGlowAlphaStart = this.mGlowAlpha;
                    this.mGlowScaleYStart = this.mGlowScaleY;
                    this.mGlowAlphaFinish = 0.0f;
                    this.mGlowScaleYFinish = 0.0f;
                    return;
                case 3:
                    this.mState = 0;
                    return;
                case 4:
                    this.mState = 3;
                    return;
                default:
                    return;
            }
        }
    }

    public boolean draw(Canvas canvas) {
        update();
        float centerX = this.mBounds.centerX();
        float height = this.mBounds.height();
        float f = this.mRadius;
        canvas.scale(1.0f, Math.min(this.mGlowScaleY, 1.0f) * this.mBaseGlowScale, centerX, 0.0f);
        float width = (this.mBounds.width() * (Math.max(0.0f, Math.min(this.mDisplacement, 1.0f)) - 0.5f)) / 2.0f;
        this.mPaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        canvas.drawCircle(centerX + width, height - f, this.mRadius, this.mPaint);
        boolean z = false;
        if (this.mState == 3) {
            z = false;
            if (this.mGlowScaleY == 0.0f) {
                this.mState = 0;
                z = true;
            }
        }
        if (this.mState != 0) {
            z = true;
        }
        return z;
    }

    public boolean isFinished() {
        boolean z = false;
        if (this.mState == 0) {
            z = true;
        }
        return z;
    }

    public void onPull(float f) {
        onPull(f, 0.5f);
    }

    public void onPull(float f, float f2) {
        long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        this.mTargetDisplacement = f2;
        if (this.mState != 4 || ((float) (currentAnimationTimeMillis - this.mStartTime)) >= this.mDuration) {
            if (this.mState != 1) {
                this.mGlowScaleY = Math.max(0.0f, this.mGlowScaleY);
            }
            this.mState = 1;
            this.mStartTime = currentAnimationTimeMillis;
            this.mDuration = 167.0f;
            this.mPullDistance += f;
            float min = Math.min(0.5f, this.mGlowAlpha + (0.8f * Math.abs(f)));
            this.mGlowAlphaStart = min;
            this.mGlowAlpha = min;
            if (this.mPullDistance == 0.0f) {
                this.mGlowScaleYStart = 0.0f;
                this.mGlowScaleY = 0.0f;
            } else {
                float max = (float) (Math.max(0.0d, (1.0d - (1.0d / Math.sqrt(Math.abs(this.mPullDistance) * this.mBounds.height()))) - 0.3d) / 0.7d);
                this.mGlowScaleYStart = max;
                this.mGlowScaleY = max;
            }
            this.mGlowAlphaFinish = this.mGlowAlpha;
            this.mGlowScaleYFinish = this.mGlowScaleY;
        }
    }

    public void onRelease() {
        this.mPullDistance = 0.0f;
        if (this.mState == 1 || this.mState == 4) {
            this.mState = 3;
            this.mGlowAlphaStart = this.mGlowAlpha;
            this.mGlowScaleYStart = this.mGlowScaleY;
            this.mGlowAlphaFinish = 0.0f;
            this.mGlowScaleYFinish = 0.0f;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 600.0f;
        }
    }

    public void setColor(int i) {
        this.mPaint.setColor(i);
    }

    public void setSize(int i, int i2) {
        float f = 1.0f;
        float f2 = (i * 0.5f) / SIN;
        float f3 = f2 - (COS * f2);
        float f4 = (i2 * 0.75f) / SIN;
        float f5 = COS;
        this.mRadius = f2;
        if (f3 > 0.0f) {
            f = Math.min((f4 - (f5 * f4)) / f3, 1.0f);
        }
        this.mBaseGlowScale = f;
        this.mBounds.set(this.mBounds.left, this.mBounds.top, i, (int) Math.min(i2, f3));
    }
}
