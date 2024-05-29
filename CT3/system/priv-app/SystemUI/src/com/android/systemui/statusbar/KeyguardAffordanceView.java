package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/statusbar/KeyguardAffordanceView.class */
public class KeyguardAffordanceView extends ImageView {
    private ValueAnimator mAlphaAnimator;
    private AnimatorListenerAdapter mAlphaEndListener;
    private int mCenterX;
    private int mCenterY;
    private ValueAnimator mCircleAnimator;
    private int mCircleColor;
    private AnimatorListenerAdapter mCircleEndListener;
    private final Paint mCirclePaint;
    private float mCircleRadius;
    private float mCircleStartRadius;
    private float mCircleStartValue;
    private boolean mCircleWillBeHidden;
    private AnimatorListenerAdapter mClipEndListener;
    private final ArgbEvaluator mColorInterpolator;
    private boolean mFinishing;
    private final FlingAnimationUtils mFlingAnimationUtils;
    private CanvasProperty<Float> mHwCenterX;
    private CanvasProperty<Float> mHwCenterY;
    private CanvasProperty<Paint> mHwCirclePaint;
    private CanvasProperty<Float> mHwCircleRadius;
    private float mImageScale;
    private final int mInverseColor;
    private boolean mLaunchingAffordance;
    private float mMaxCircleSize;
    private final int mMinBackgroundRadius;
    private final int mNormalColor;
    private Animator mPreviewClipper;
    private View mPreviewView;
    private float mRestingAlpha;
    private ValueAnimator mScaleAnimator;
    private AnimatorListenerAdapter mScaleEndListener;
    private boolean mSupportHardware;
    private int[] mTempPoint;

    public KeyguardAffordanceView(Context context) {
        this(context, null);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTempPoint = new int[2];
        this.mImageScale = 1.0f;
        this.mRestingAlpha = 0.5f;
        this.mClipEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.1
            final KeyguardAffordanceView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mPreviewClipper = null;
            }
        };
        this.mCircleEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.2
            final KeyguardAffordanceView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mCircleAnimator = null;
            }
        };
        this.mScaleEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.3
            final KeyguardAffordanceView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mScaleAnimator = null;
            }
        };
        this.mAlphaEndListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.4
            final KeyguardAffordanceView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mAlphaAnimator = null;
            }
        };
        this.mCirclePaint = new Paint();
        this.mCirclePaint.setAntiAlias(true);
        this.mCircleColor = -1;
        this.mCirclePaint.setColor(this.mCircleColor);
        this.mNormalColor = -1;
        this.mInverseColor = -16777216;
        this.mMinBackgroundRadius = this.mContext.getResources().getDimensionPixelSize(2131689889);
        this.mColorInterpolator = new ArgbEvaluator();
        this.mFlingAnimationUtils = new FlingAnimationUtils(this.mContext, 0.3f);
    }

    private void cancelAnimator(Animator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }

    private void drawBackgroundCircle(Canvas canvas) {
        if (this.mCircleRadius > 0.0f || this.mFinishing) {
            if (this.mFinishing && this.mSupportHardware) {
                ((DisplayListCanvas) canvas).drawCircle(this.mHwCenterX, this.mHwCenterY, this.mHwCircleRadius, this.mHwCirclePaint);
                return;
            }
            updateCircleColor();
            canvas.drawCircle(this.mCenterX, this.mCenterY, this.mCircleRadius, this.mCirclePaint);
        }
    }

    private ValueAnimator getAnimatorToRadius(float f) {
        boolean z = true;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mCircleRadius, f);
        this.mCircleAnimator = ofFloat;
        this.mCircleStartValue = this.mCircleRadius;
        if (f != 0.0f) {
            z = false;
        }
        this.mCircleWillBeHidden = z;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.7
            final KeyguardAffordanceView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mCircleRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.this$0.updateIconColor();
                this.this$0.invalidate();
            }
        });
        ofFloat.addListener(this.mCircleEndListener);
        return ofFloat;
    }

    private Animator.AnimatorListener getEndListener(Runnable runnable) {
        return new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.10
            boolean mCancelled;
            final KeyguardAffordanceView this$0;
            final Runnable val$runnable;

            {
                this.this$0 = this;
                this.val$runnable = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mCancelled) {
                    return;
                }
                this.val$runnable.run();
            }
        };
    }

    private float getMaxCircleSize() {
        getLocationInWindow(this.mTempPoint);
        float width = getRootView().getWidth();
        float f = this.mTempPoint[0] + this.mCenterX;
        return (float) Math.hypot(Math.max(width - f, f), this.mTempPoint[1] + this.mCenterY);
    }

    private Animator getRtAnimatorToRadius(float f) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(this.mHwCircleRadius, f);
        renderNodeAnimator.setTarget(this);
        return renderNodeAnimator;
    }

    private void initHwProperties() {
        this.mHwCenterX = CanvasProperty.createFloat(this.mCenterX);
        this.mHwCenterY = CanvasProperty.createFloat(this.mCenterY);
        this.mHwCirclePaint = CanvasProperty.createPaint(this.mCirclePaint);
        this.mHwCircleRadius = CanvasProperty.createFloat(this.mCircleRadius);
    }

    private void setCircleRadius(float f, boolean z, boolean z2) {
        boolean z3 = (this.mCircleAnimator == null || !this.mCircleWillBeHidden) ? this.mCircleAnimator == null && this.mCircleRadius == 0.0f : true;
        boolean z4 = f == 0.0f;
        if (!((z3 == z4 || z2) ? false : true)) {
            if (this.mCircleAnimator != null) {
                if (this.mCircleWillBeHidden) {
                    return;
                }
                this.mCircleAnimator.getValues()[0].setFloatValues(this.mCircleStartValue + (f - this.mMinBackgroundRadius), f);
                this.mCircleAnimator.setCurrentPlayTime(this.mCircleAnimator.getCurrentPlayTime());
                return;
            }
            this.mCircleRadius = f;
            updateIconColor();
            invalidate();
            if (!z4 || this.mPreviewView == null) {
                return;
            }
            this.mPreviewView.setVisibility(4);
            return;
        }
        cancelAnimator(this.mCircleAnimator);
        cancelAnimator(this.mPreviewClipper);
        ValueAnimator animatorToRadius = getAnimatorToRadius(f);
        Interpolator interpolator = f == 0.0f ? Interpolators.FAST_OUT_LINEAR_IN : Interpolators.LINEAR_OUT_SLOW_IN;
        animatorToRadius.setInterpolator(interpolator);
        long j = 250;
        if (!z) {
            j = Math.min(80.0f * (Math.abs(this.mCircleRadius - f) / this.mMinBackgroundRadius), 200L);
        }
        animatorToRadius.setDuration(j);
        animatorToRadius.start();
        if (this.mPreviewView == null || this.mPreviewView.getVisibility() != 0) {
            return;
        }
        this.mPreviewView.setVisibility(0);
        this.mPreviewClipper = ViewAnimationUtils.createCircularReveal(this.mPreviewView, getLeft() + this.mCenterX, getTop() + this.mCenterY, this.mCircleRadius, f);
        this.mPreviewClipper.setInterpolator(interpolator);
        this.mPreviewClipper.setDuration(j);
        this.mPreviewClipper.addListener(this.mClipEndListener);
        this.mPreviewClipper.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.6
            final KeyguardAffordanceView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mPreviewView.setVisibility(4);
            }
        });
        this.mPreviewClipper.start();
    }

    private void startRtAlphaFadeIn() {
        if (this.mCircleRadius == 0.0f && this.mPreviewView == null) {
            Paint paint = new Paint(this.mCirclePaint);
            paint.setColor(this.mCircleColor);
            paint.setAlpha(0);
            this.mHwCirclePaint = CanvasProperty.createPaint(paint);
            RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(this.mHwCirclePaint, 1, 255.0f);
            renderNodeAnimator.setTarget(this);
            renderNodeAnimator.setInterpolator(Interpolators.ALPHA_IN);
            renderNodeAnimator.setDuration(250L);
            renderNodeAnimator.start();
        }
    }

    private void startRtCircleFadeOut(long j) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(this.mHwCirclePaint, 1, 0.0f);
        renderNodeAnimator.setDuration(j);
        renderNodeAnimator.setInterpolator(Interpolators.ALPHA_OUT);
        renderNodeAnimator.setTarget(this);
        renderNodeAnimator.start();
    }

    private void updateCircleColor() {
        float max = 0.5f + (Math.max(0.0f, Math.min(1.0f, (this.mCircleRadius - this.mMinBackgroundRadius) / (this.mMinBackgroundRadius * 0.5f))) * 0.5f);
        float f = max;
        if (this.mPreviewView != null) {
            f = max;
            if (this.mPreviewView.getVisibility() == 0) {
                f = max * (1.0f - (Math.max(0.0f, this.mCircleRadius - this.mCircleStartRadius) / (this.mMaxCircleSize - this.mCircleStartRadius)));
            }
        }
        this.mCirclePaint.setColor(Color.argb((int) (Color.alpha(this.mCircleColor) * f), Color.red(this.mCircleColor), Color.green(this.mCircleColor), Color.blue(this.mCircleColor)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIconColor() {
        getDrawable().mutate().setColorFilter(((Integer) this.mColorInterpolator.evaluate(Math.min(1.0f, this.mCircleRadius / this.mMinBackgroundRadius), Integer.valueOf(this.mNormalColor), Integer.valueOf(this.mInverseColor))).intValue(), PorterDuff.Mode.SRC_ATOP);
    }

    public void finishAnimation(float f, Runnable runnable) {
        ValueAnimator animatorToRadius;
        cancelAnimator(this.mCircleAnimator);
        cancelAnimator(this.mPreviewClipper);
        this.mFinishing = true;
        this.mCircleStartRadius = this.mCircleRadius;
        float maxCircleSize = getMaxCircleSize();
        if (this.mSupportHardware) {
            initHwProperties();
            animatorToRadius = getRtAnimatorToRadius(maxCircleSize);
            startRtAlphaFadeIn();
        } else {
            animatorToRadius = getAnimatorToRadius(maxCircleSize);
        }
        this.mFlingAnimationUtils.applyDismissing(animatorToRadius, this.mCircleRadius, maxCircleSize, f, maxCircleSize);
        animatorToRadius.addListener(new AnimatorListenerAdapter(this, runnable, maxCircleSize) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.5
            final KeyguardAffordanceView this$0;
            final Runnable val$mAnimationEndRunnable;
            final float val$maxCircleSize;

            {
                this.this$0 = this;
                this.val$mAnimationEndRunnable = runnable;
                this.val$maxCircleSize = maxCircleSize;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.val$mAnimationEndRunnable.run();
                this.this$0.mFinishing = false;
                this.this$0.mCircleRadius = this.val$maxCircleSize;
                this.this$0.invalidate();
            }
        });
        animatorToRadius.start();
        setImageAlpha(0.0f, true);
        if (this.mPreviewView != null) {
            this.mPreviewView.setVisibility(0);
            this.mPreviewClipper = ViewAnimationUtils.createCircularReveal(this.mPreviewView, getLeft() + this.mCenterX, getTop() + this.mCenterY, this.mCircleRadius, maxCircleSize);
            this.mFlingAnimationUtils.applyDismissing(this.mPreviewClipper, this.mCircleRadius, maxCircleSize, f, maxCircleSize);
            this.mPreviewClipper.addListener(this.mClipEndListener);
            this.mPreviewClipper.start();
            if (this.mSupportHardware) {
                startRtCircleFadeOut(animatorToRadius.getDuration());
            }
        }
    }

    public float getCircleRadius() {
        return this.mCircleRadius;
    }

    public float getRestingAlpha() {
        return this.mRestingAlpha;
    }

    public void instantFinishAnimation() {
        cancelAnimator(this.mPreviewClipper);
        if (this.mPreviewView != null) {
            this.mPreviewView.setClipBounds(null);
            this.mPreviewView.setVisibility(0);
        }
        this.mCircleRadius = getMaxCircleSize();
        setImageAlpha(0.0f, false);
        invalidate();
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDraw(Canvas canvas) {
        this.mSupportHardware = false;
        drawBackgroundCircle(canvas);
        canvas.save();
        canvas.scale(this.mImageScale, this.mImageScale, getWidth() / 2, getHeight() / 2);
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override // android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mCenterX = getWidth() / 2;
        this.mCenterY = getHeight() / 2;
        this.mMaxCircleSize = getMaxCircleSize();
    }

    @Override // android.view.View
    public boolean performClick() {
        if (isClickable()) {
            return super.performClick();
        }
        return false;
    }

    public void setCircleRadius(float f, boolean z) {
        setCircleRadius(f, z, false);
    }

    public void setCircleRadiusWithoutAnimation(float f) {
        cancelAnimator(this.mCircleAnimator);
        setCircleRadius(f, false, true);
    }

    public void setImageAlpha(float f, boolean z) {
        setImageAlpha(f, z, -1L, null, null);
    }

    public void setImageAlpha(float f, boolean z, long j, Interpolator interpolator, Runnable runnable) {
        int imageAlpha;
        cancelAnimator(this.mAlphaAnimator);
        if (this.mLaunchingAffordance) {
            f = 0.0f;
        }
        int i = (int) (f * 255.0f);
        Drawable background = getBackground();
        if (!z) {
            if (background != null) {
                background.mutate().setAlpha(i);
            }
            setImageAlpha(i);
            return;
        }
        ValueAnimator ofInt = ValueAnimator.ofInt(getImageAlpha(), i);
        this.mAlphaAnimator = ofInt;
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, background) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.9
            final KeyguardAffordanceView this$0;
            final Drawable val$background;

            {
                this.this$0 = this;
                this.val$background = background;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                if (this.val$background != null) {
                    this.val$background.mutate().setAlpha(intValue);
                }
                this.this$0.setImageAlpha(intValue);
            }
        });
        ofInt.addListener(this.mAlphaEndListener);
        Interpolator interpolator2 = interpolator;
        if (interpolator == null) {
            interpolator2 = f == 0.0f ? Interpolators.FAST_OUT_LINEAR_IN : Interpolators.LINEAR_OUT_SLOW_IN;
        }
        ofInt.setInterpolator(interpolator2);
        long j2 = j;
        if (j == -1) {
            j2 = 200.0f * Math.min(1.0f, Math.abs(imageAlpha - i) / 255.0f);
        }
        ofInt.setDuration(j2);
        if (runnable != null) {
            ofInt.addListener(getEndListener(runnable));
        }
        ofInt.start();
    }

    public void setImageScale(float f, boolean z) {
        setImageScale(f, z, -1L, null);
    }

    public void setImageScale(float f, boolean z, long j, Interpolator interpolator) {
        cancelAnimator(this.mScaleAnimator);
        if (!z) {
            this.mImageScale = f;
            invalidate();
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mImageScale, f);
        this.mScaleAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.KeyguardAffordanceView.8
            final KeyguardAffordanceView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mImageScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.this$0.invalidate();
            }
        });
        ofFloat.addListener(this.mScaleEndListener);
        Interpolator interpolator2 = interpolator;
        if (interpolator == null) {
            interpolator2 = f == 0.0f ? Interpolators.FAST_OUT_LINEAR_IN : Interpolators.LINEAR_OUT_SLOW_IN;
        }
        ofFloat.setInterpolator(interpolator2);
        long j2 = j;
        if (j == -1) {
            j2 = 200.0f * Math.min(1.0f, Math.abs(this.mImageScale - f) / 0.19999999f);
        }
        ofFloat.setDuration(j2);
        ofFloat.start();
    }

    public void setLaunchingAffordance(boolean z) {
        this.mLaunchingAffordance = z;
    }

    public void setPreviewView(View view) {
        View view2 = this.mPreviewView;
        this.mPreviewView = view;
        if (this.mPreviewView != null) {
            this.mPreviewView.setVisibility(this.mLaunchingAffordance ? view2.getVisibility() : 4);
        }
    }

    public void setRestingAlpha(float f) {
        this.mRestingAlpha = f;
        setImageAlpha(f, false);
    }
}
