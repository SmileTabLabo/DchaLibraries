package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
/* loaded from: a.zip:com/android/systemui/statusbar/ScrimView.class */
public class ScrimView extends View {
    private ValueAnimator mAlphaAnimator;
    private ValueAnimator.AnimatorUpdateListener mAlphaUpdateListener;
    private Runnable mChangeRunnable;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mDrawAsSrc;
    private Rect mExcludedRect;
    private boolean mHasExcludedArea;
    private boolean mIsEmpty;
    private final Paint mPaint;
    private int mScrimColor;
    private float mViewAlpha;

    public ScrimView(Context context) {
        this(context, null);
    }

    public ScrimView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScrimView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ScrimView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPaint = new Paint();
        this.mIsEmpty = true;
        this.mViewAlpha = 1.0f;
        this.mExcludedRect = new Rect();
        this.mAlphaUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.ScrimView.1
            final ScrimView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mViewAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.this$0.invalidate();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.ScrimView.2
            final ScrimView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mAlphaAnimator = null;
            }
        };
    }

    public void animateViewAlpha(float f, long j, Interpolator interpolator) {
        if (this.mAlphaAnimator != null) {
            this.mAlphaAnimator.cancel();
        }
        this.mAlphaAnimator = ValueAnimator.ofFloat(this.mViewAlpha, f);
        this.mAlphaAnimator.addUpdateListener(this.mAlphaUpdateListener);
        this.mAlphaAnimator.addListener(this.mClearAnimatorListener);
        this.mAlphaAnimator.setInterpolator(interpolator);
        this.mAlphaAnimator.setDuration(j);
        this.mAlphaAnimator.start();
    }

    public int getScrimColorWithAlpha() {
        int i = this.mScrimColor;
        return Color.argb((int) (Color.alpha(i) * this.mViewAlpha), Color.red(i), Color.green(i), Color.blue(i));
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mDrawAsSrc || (!this.mIsEmpty && this.mViewAlpha > 0.0f)) {
            PorterDuff.Mode mode = this.mDrawAsSrc ? PorterDuff.Mode.SRC : PorterDuff.Mode.SRC_OVER;
            int scrimColorWithAlpha = getScrimColorWithAlpha();
            if (!this.mHasExcludedArea) {
                canvas.drawColor(scrimColorWithAlpha, mode);
                return;
            }
            this.mPaint.setColor(scrimColorWithAlpha);
            if (this.mExcludedRect.top > 0) {
                canvas.drawRect(0.0f, 0.0f, getWidth(), this.mExcludedRect.top, this.mPaint);
            }
            if (this.mExcludedRect.left > 0) {
                canvas.drawRect(0.0f, this.mExcludedRect.top, this.mExcludedRect.left, this.mExcludedRect.bottom, this.mPaint);
            }
            if (this.mExcludedRect.right < getWidth()) {
                canvas.drawRect(this.mExcludedRect.right, this.mExcludedRect.top, getWidth(), this.mExcludedRect.bottom, this.mPaint);
            }
            if (this.mExcludedRect.bottom < getHeight()) {
                canvas.drawRect(0.0f, this.mExcludedRect.bottom, getWidth(), getHeight(), this.mPaint);
            }
        }
    }

    public void setChangeRunnable(Runnable runnable) {
        this.mChangeRunnable = runnable;
    }

    public void setDrawAsSrc(boolean z) {
        this.mDrawAsSrc = z;
        this.mPaint.setXfermode(new PorterDuffXfermode(this.mDrawAsSrc ? PorterDuff.Mode.SRC : PorterDuff.Mode.SRC_OVER));
        invalidate();
    }

    public void setExcludedArea(Rect rect) {
        if (rect == null) {
            this.mHasExcludedArea = false;
            invalidate();
            return;
        }
        int max = Math.max(rect.left, 0);
        int max2 = Math.max(rect.top, 0);
        int min = Math.min(rect.right, getWidth());
        int min2 = Math.min(rect.bottom, getHeight());
        this.mExcludedRect.set(max, max2, min, min2);
        boolean z = false;
        if (max < min) {
            z = false;
            if (max2 < min2) {
                z = true;
            }
        }
        this.mHasExcludedArea = z;
        invalidate();
    }

    public void setScrimColor(int i) {
        boolean z = false;
        if (i != this.mScrimColor) {
            if (Color.alpha(i) == 0) {
                z = true;
            }
            this.mIsEmpty = z;
            this.mScrimColor = i;
            invalidate();
            if (this.mChangeRunnable != null) {
                this.mChangeRunnable.run();
            }
        }
    }
}
