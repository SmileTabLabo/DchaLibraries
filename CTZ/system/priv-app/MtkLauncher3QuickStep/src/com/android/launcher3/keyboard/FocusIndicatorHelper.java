package com.android.launcher3.keyboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.view.View;
import com.android.launcher3.R;
/* loaded from: classes.dex */
public abstract class FocusIndicatorHelper implements View.OnFocusChangeListener, ValueAnimator.AnimatorUpdateListener {
    private static final long ANIM_DURATION = 150;
    private static final float MIN_VISIBLE_ALPHA = 0.2f;
    private float mAlpha;
    private final View mContainer;
    private ObjectAnimator mCurrentAnimation;
    private View mCurrentView;
    private View mLastFocusedView;
    private final int mMaxAlpha;
    private float mShift;
    private View mTargetView;
    public static final Property<FocusIndicatorHelper, Float> ALPHA = new Property<FocusIndicatorHelper, Float>(Float.TYPE, "alpha") { // from class: com.android.launcher3.keyboard.FocusIndicatorHelper.1
        @Override // android.util.Property
        public void set(FocusIndicatorHelper focusIndicatorHelper, Float f) {
            focusIndicatorHelper.setAlpha(f.floatValue());
        }

        @Override // android.util.Property
        public Float get(FocusIndicatorHelper focusIndicatorHelper) {
            return Float.valueOf(focusIndicatorHelper.mAlpha);
        }
    };
    public static final Property<FocusIndicatorHelper, Float> SHIFT = new Property<FocusIndicatorHelper, Float>(Float.TYPE, "shift") { // from class: com.android.launcher3.keyboard.FocusIndicatorHelper.2
        @Override // android.util.Property
        public void set(FocusIndicatorHelper focusIndicatorHelper, Float f) {
            focusIndicatorHelper.mShift = f.floatValue();
        }

        @Override // android.util.Property
        public Float get(FocusIndicatorHelper focusIndicatorHelper) {
            return Float.valueOf(focusIndicatorHelper.mShift);
        }
    };
    private static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());
    private static final Rect sTempRect1 = new Rect();
    private static final Rect sTempRect2 = new Rect();
    private final Rect mDirtyRect = new Rect();
    private boolean mIsDirty = false;
    private final Paint mPaint = new Paint(1);

    public abstract void viewToRect(View view, Rect rect);

    public FocusIndicatorHelper(View view) {
        this.mContainer = view;
        int color = view.getResources().getColor(R.color.focused_background);
        this.mMaxAlpha = Color.alpha(color);
        this.mPaint.setColor(color | ViewCompat.MEASURED_STATE_MASK);
        setAlpha(0.0f);
        this.mShift = 0.0f;
    }

    protected void setAlpha(float f) {
        this.mAlpha = f;
        this.mPaint.setAlpha((int) (this.mAlpha * this.mMaxAlpha));
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        invalidateDirty();
    }

    protected void invalidateDirty() {
        if (this.mIsDirty) {
            this.mContainer.invalidate(this.mDirtyRect);
            this.mIsDirty = false;
        }
        Rect drawRect = getDrawRect();
        if (drawRect != null) {
            this.mContainer.invalidate(drawRect);
        }
    }

    public void draw(Canvas canvas) {
        Rect drawRect;
        if (this.mAlpha > 0.0f && (drawRect = getDrawRect()) != null) {
            this.mDirtyRect.set(drawRect);
            canvas.drawRect(this.mDirtyRect, this.mPaint);
            this.mIsDirty = true;
        }
    }

    private Rect getDrawRect() {
        if (this.mCurrentView != null && this.mCurrentView.isAttachedToWindow()) {
            viewToRect(this.mCurrentView, sTempRect1);
            if (this.mShift > 0.0f && this.mTargetView != null) {
                viewToRect(this.mTargetView, sTempRect2);
                return RECT_EVALUATOR.evaluate(this.mShift, sTempRect1, sTempRect2);
            }
            return sTempRect1;
        }
        return null;
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        if (z) {
            endCurrentAnimation();
            if (this.mAlpha > 0.2f) {
                this.mTargetView = view;
                this.mCurrentAnimation = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(ALPHA, 1.0f), PropertyValuesHolder.ofFloat(SHIFT, 1.0f));
                this.mCurrentAnimation.addListener(new ViewSetListener(view, true));
            } else {
                setCurrentView(view);
                this.mCurrentAnimation = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(ALPHA, 1.0f));
            }
            this.mLastFocusedView = view;
        } else if (this.mLastFocusedView == view) {
            this.mLastFocusedView = null;
            endCurrentAnimation();
            this.mCurrentAnimation = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(ALPHA, 0.0f));
            this.mCurrentAnimation.addListener(new ViewSetListener(null, false));
        }
        invalidateDirty();
        if (!z) {
            view = null;
        }
        this.mLastFocusedView = view;
        if (this.mCurrentAnimation != null) {
            this.mCurrentAnimation.addUpdateListener(this);
            this.mCurrentAnimation.setDuration(ANIM_DURATION).start();
        }
    }

    protected void endCurrentAnimation() {
        if (this.mCurrentAnimation != null) {
            this.mCurrentAnimation.cancel();
            this.mCurrentAnimation = null;
        }
    }

    protected void setCurrentView(View view) {
        this.mCurrentView = view;
        this.mShift = 0.0f;
        this.mTargetView = null;
    }

    /* loaded from: classes.dex */
    private class ViewSetListener extends AnimatorListenerAdapter {
        private final boolean mCallOnCancel;
        private boolean mCalled = false;
        private final View mViewToSet;

        public ViewSetListener(View view, boolean z) {
            this.mViewToSet = view;
            this.mCallOnCancel = z;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            if (!this.mCallOnCancel) {
                this.mCalled = true;
            }
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (!this.mCalled) {
                FocusIndicatorHelper.this.setCurrentView(this.mViewToSet);
                this.mCalled = true;
            }
        }
    }

    /* loaded from: classes.dex */
    public static class SimpleFocusIndicatorHelper extends FocusIndicatorHelper {
        public SimpleFocusIndicatorHelper(View view) {
            super(view);
        }

        @Override // com.android.launcher3.keyboard.FocusIndicatorHelper
        public void viewToRect(View view, Rect rect) {
            rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
    }
}
