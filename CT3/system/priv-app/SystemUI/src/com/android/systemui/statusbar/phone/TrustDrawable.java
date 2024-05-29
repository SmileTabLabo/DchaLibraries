package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/TrustDrawable.class */
public class TrustDrawable extends Drawable {
    private int mAlpha;
    private boolean mAnimating;
    private int mCurAlpha;
    private Animator mCurAnimator;
    private float mCurInnerRadius;
    private final float mInnerRadiusEnter;
    private final float mInnerRadiusExit;
    private final float mInnerRadiusVisibleMax;
    private final float mInnerRadiusVisibleMin;
    private Paint mPaint;
    private final float mThickness;
    private boolean mTrustManaged;
    private final Animator mVisibleAnimator;
    private int mState = -1;
    private final ValueAnimator.AnimatorUpdateListener mAlphaUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.TrustDrawable.1
        final TrustDrawable this$0;

        {
            this.this$0 = this;
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            this.this$0.mCurAlpha = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            this.this$0.invalidateSelf();
        }
    };
    private final ValueAnimator.AnimatorUpdateListener mRadiusUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.TrustDrawable.2
        final TrustDrawable this$0;

        {
            this.this$0 = this;
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            this.this$0.mCurInnerRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            this.this$0.invalidateSelf();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/TrustDrawable$StateUpdateAnimatorListener.class */
    public class StateUpdateAnimatorListener extends AnimatorListenerAdapter {
        boolean mCancelled;
        final TrustDrawable this$0;

        private StateUpdateAnimatorListener(TrustDrawable trustDrawable) {
            this.this$0 = trustDrawable;
        }

        /* synthetic */ StateUpdateAnimatorListener(TrustDrawable trustDrawable, StateUpdateAnimatorListener stateUpdateAnimatorListener) {
            this(trustDrawable);
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
            this.this$0.updateState(false);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            this.mCancelled = false;
        }
    }

    public TrustDrawable(Context context) {
        Resources resources = context.getResources();
        this.mInnerRadiusVisibleMin = resources.getDimension(2131689939);
        this.mInnerRadiusVisibleMax = resources.getDimension(2131689940);
        this.mInnerRadiusExit = resources.getDimension(2131689941);
        this.mInnerRadiusEnter = resources.getDimension(2131689942);
        this.mThickness = resources.getDimension(2131689943);
        this.mCurInnerRadius = this.mInnerRadiusEnter;
        this.mVisibleAnimator = makeVisibleAnimator();
        this.mPaint = new Paint();
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(-1);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(this.mThickness);
    }

    private ValueAnimator configureAnimator(ValueAnimator valueAnimator, long j, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Interpolator interpolator, boolean z) {
        valueAnimator.setDuration(j);
        valueAnimator.addUpdateListener(animatorUpdateListener);
        valueAnimator.setInterpolator(interpolator);
        if (z) {
            valueAnimator.setRepeatCount(-1);
            valueAnimator.setRepeatMode(2);
        }
        return valueAnimator;
    }

    private Animator makeAnimators(float f, float f2, int i, int i2, long j, Interpolator interpolator, boolean z, boolean z2) {
        ValueAnimator configureAnimator = configureAnimator(ValueAnimator.ofInt(i, i2), j, this.mAlphaUpdateListener, interpolator, z);
        ValueAnimator configureAnimator2 = configureAnimator(ValueAnimator.ofFloat(f, f2), j, this.mRadiusUpdateListener, interpolator, z);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(configureAnimator, configureAnimator2);
        if (z2) {
            animatorSet.addListener(new StateUpdateAnimatorListener(this, null));
        }
        return animatorSet;
    }

    private Animator makeEnterAnimator(float f, int i) {
        return makeAnimators(f, this.mInnerRadiusVisibleMax, i, 76, 500L, Interpolators.LINEAR_OUT_SLOW_IN, false, true);
    }

    private Animator makeExitAnimator(float f, int i) {
        return makeAnimators(f, this.mInnerRadiusExit, i, 0, 500L, Interpolators.FAST_OUT_SLOW_IN, false, true);
    }

    private Animator makeVisibleAnimator() {
        return makeAnimators(this.mInnerRadiusVisibleMax, this.mInnerRadiusVisibleMin, 76, 38, 1000L, Interpolators.ACCELERATE_DECELERATE, true, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateState(boolean z) {
        int i;
        if (this.mAnimating) {
            int i2 = this.mState;
            if (this.mState == -1) {
                i = this.mTrustManaged ? 1 : 0;
            } else if (this.mState == 0) {
                i = i2;
                if (this.mTrustManaged) {
                    i = 1;
                }
            } else if (this.mState == 1) {
                i = i2;
                if (!this.mTrustManaged) {
                    i = 3;
                }
            } else if (this.mState == 2) {
                i = i2;
                if (!this.mTrustManaged) {
                    i = 3;
                }
            } else {
                i = i2;
                if (this.mState == 3) {
                    i = i2;
                    if (this.mTrustManaged) {
                        i = 1;
                    }
                }
            }
            int i3 = i;
            if (!z) {
                int i4 = i;
                if (i == 1) {
                    i4 = 2;
                }
                i3 = i4;
                if (i4 == 3) {
                    i3 = 0;
                }
            }
            if (i3 != this.mState) {
                if (this.mCurAnimator != null) {
                    this.mCurAnimator.cancel();
                    this.mCurAnimator = null;
                }
                if (i3 == 0) {
                    this.mCurAlpha = 0;
                    this.mCurInnerRadius = this.mInnerRadiusEnter;
                } else if (i3 == 1) {
                    this.mCurAnimator = makeEnterAnimator(this.mCurInnerRadius, this.mCurAlpha);
                    if (this.mState == -1) {
                        this.mCurAnimator.setStartDelay(200L);
                    }
                } else if (i3 == 2) {
                    this.mCurAlpha = 76;
                    this.mCurInnerRadius = this.mInnerRadiusVisibleMax;
                    this.mCurAnimator = this.mVisibleAnimator;
                } else if (i3 == 3) {
                    this.mCurAnimator = makeExitAnimator(this.mCurInnerRadius, this.mCurAlpha);
                }
                this.mState = i3;
                if (this.mCurAnimator != null) {
                    this.mCurAnimator.start();
                }
                invalidateSelf();
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        int i = (this.mCurAlpha * this.mAlpha) / 256;
        if (i == 0) {
            return;
        }
        Rect bounds = getBounds();
        this.mPaint.setAlpha(i);
        canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), this.mCurInnerRadius, this.mPaint);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mAlpha;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mAlpha = i;
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        throw new UnsupportedOperationException("not implemented");
    }

    public void setTrustManaged(boolean z) {
        if (z != this.mTrustManaged || this.mState == -1) {
            this.mTrustManaged = z;
            updateState(true);
        }
    }

    public void start() {
        if (this.mAnimating) {
            return;
        }
        this.mAnimating = true;
        updateState(true);
        invalidateSelf();
    }

    public void stop() {
        if (this.mAnimating) {
            this.mAnimating = false;
            if (this.mCurAnimator != null) {
                this.mCurAnimator.cancel();
                this.mCurAnimator = null;
            }
            this.mState = -1;
            this.mCurAlpha = 0;
            this.mCurInnerRadius = this.mInnerRadiusEnter;
            invalidateSelf();
        }
    }
}
