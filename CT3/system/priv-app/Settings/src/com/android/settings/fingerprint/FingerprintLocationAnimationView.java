package com.android.settings.fingerprint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.settings.R;
/* loaded from: classes.dex */
public class FingerprintLocationAnimationView extends View implements FingerprintFindSensorAnimation {
    private ValueAnimator mAlphaAnimator;
    private final Paint mDotPaint;
    private final int mDotRadius;
    private final Interpolator mFastOutSlowInInterpolator;
    private final float mFractionCenterX;
    private final float mFractionCenterY;
    private final Interpolator mLinearOutSlowInInterpolator;
    private final int mMaxPulseRadius;
    private final Paint mPulsePaint;
    private float mPulseRadius;
    private ValueAnimator mRadiusAnimator;
    private final Runnable mStartPhaseRunnable;

    public FingerprintLocationAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDotPaint = new Paint();
        this.mPulsePaint = new Paint();
        this.mStartPhaseRunnable = new Runnable() { // from class: com.android.settings.fingerprint.FingerprintLocationAnimationView.1
            @Override // java.lang.Runnable
            public void run() {
                FingerprintLocationAnimationView.this.startPhase();
            }
        };
        this.mDotRadius = getResources().getDimensionPixelSize(R.dimen.fingerprint_dot_radius);
        this.mMaxPulseRadius = getResources().getDimensionPixelSize(R.dimen.fingerprint_pulse_radius);
        this.mFractionCenterX = getResources().getFraction(R.fraction.fingerprint_sensor_location_fraction_x, 1, 1);
        this.mFractionCenterY = getResources().getFraction(R.fraction.fingerprint_sensor_location_fraction_y, 1, 1);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(16843829, typedValue, true);
        int color = getResources().getColor(typedValue.resourceId, null);
        this.mDotPaint.setAntiAlias(true);
        this.mPulsePaint.setAntiAlias(true);
        this.mDotPaint.setColor(color);
        this.mPulsePaint.setColor(color);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        drawPulse(canvas);
        drawDot(canvas);
    }

    private void drawDot(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), this.mDotRadius, this.mDotPaint);
    }

    private void drawPulse(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), this.mPulseRadius, this.mPulsePaint);
    }

    private float getCenterX() {
        return getWidth() * this.mFractionCenterX;
    }

    private float getCenterY() {
        return getHeight() * this.mFractionCenterY;
    }

    @Override // com.android.settings.fingerprint.FingerprintFindSensorAnimation
    public void startAnimation() {
        startPhase();
    }

    @Override // com.android.settings.fingerprint.FingerprintFindSensorAnimation
    public void stopAnimation() {
        removeCallbacks(this.mStartPhaseRunnable);
        if (this.mRadiusAnimator != null) {
            this.mRadiusAnimator.cancel();
        }
        if (this.mAlphaAnimator == null) {
            return;
        }
        this.mAlphaAnimator.cancel();
    }

    @Override // com.android.settings.fingerprint.FingerprintFindSensorAnimation
    public void pauseAnimation() {
        stopAnimation();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startPhase() {
        startRadiusAnimation();
        startAlphaAnimation();
    }

    private void startRadiusAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, this.mMaxPulseRadius);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.settings.fingerprint.FingerprintLocationAnimationView.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                FingerprintLocationAnimationView.this.mPulseRadius = ((Float) animation.getAnimatedValue()).floatValue();
                FingerprintLocationAnimationView.this.invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() { // from class: com.android.settings.fingerprint.FingerprintLocationAnimationView.3
            boolean mCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                FingerprintLocationAnimationView.this.mRadiusAnimator = null;
                if (this.mCancelled) {
                    return;
                }
                FingerprintLocationAnimationView.this.postDelayed(FingerprintLocationAnimationView.this.mStartPhaseRunnable, 1000L);
            }
        });
        animator.setDuration(1000L);
        animator.setInterpolator(this.mLinearOutSlowInInterpolator);
        animator.start();
        this.mRadiusAnimator = animator;
    }

    private void startAlphaAnimation() {
        this.mPulsePaint.setAlpha(38);
        ValueAnimator animator = ValueAnimator.ofFloat(0.15f, 0.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.settings.fingerprint.FingerprintLocationAnimationView.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                FingerprintLocationAnimationView.this.mPulsePaint.setAlpha((int) (((Float) animation.getAnimatedValue()).floatValue() * 255.0f));
                FingerprintLocationAnimationView.this.invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() { // from class: com.android.settings.fingerprint.FingerprintLocationAnimationView.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                FingerprintLocationAnimationView.this.mAlphaAnimator = null;
            }
        });
        animator.setDuration(750L);
        animator.setInterpolator(this.mFastOutSlowInInterpolator);
        animator.setStartDelay(250L);
        animator.start();
        this.mAlphaAnimator = animator;
    }
}
