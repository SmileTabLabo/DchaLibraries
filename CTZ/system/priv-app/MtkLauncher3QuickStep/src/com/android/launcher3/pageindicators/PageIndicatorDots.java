package com.android.launcher3.pageindicators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.OvershootInterpolator;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Themes;
/* loaded from: classes.dex */
public class PageIndicatorDots extends View implements PageIndicator {
    private static final long ANIMATION_DURATION = 150;
    private static final int ENTER_ANIMATION_DURATION = 400;
    private static final float ENTER_ANIMATION_OVERSHOOT_TENSION = 4.9f;
    private static final int ENTER_ANIMATION_STAGGERED_DELAY = 150;
    private static final int ENTER_ANIMATION_START_DELAY = 300;
    private static final float SHIFT_PER_ANIMATION = 0.5f;
    private static final float SHIFT_THRESHOLD = 0.1f;
    private final int mActiveColor;
    private int mActivePage;
    private ObjectAnimator mAnimator;
    private final Paint mCirclePaint;
    private float mCurrentPosition;
    private final float mDotRadius;
    private float[] mEntryAnimationRadiusFactors;
    private float mFinalPosition;
    private final int mInActiveColor;
    private final boolean mIsRtl;
    private int mNumPages;
    private static final RectF sTempRect = new RectF();
    private static final Property<PageIndicatorDots, Float> CURRENT_POSITION = new Property<PageIndicatorDots, Float>(Float.TYPE, "current_position") { // from class: com.android.launcher3.pageindicators.PageIndicatorDots.1
        @Override // android.util.Property
        public Float get(PageIndicatorDots pageIndicatorDots) {
            return Float.valueOf(pageIndicatorDots.mCurrentPosition);
        }

        @Override // android.util.Property
        public void set(PageIndicatorDots pageIndicatorDots, Float f) {
            pageIndicatorDots.mCurrentPosition = f.floatValue();
            pageIndicatorDots.invalidate();
            pageIndicatorDots.invalidateOutline();
        }
    };

    public PageIndicatorDots(Context context) {
        this(context, null);
    }

    public PageIndicatorDots(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PageIndicatorDots(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCirclePaint = new Paint(1);
        this.mCirclePaint.setStyle(Paint.Style.FILL);
        this.mDotRadius = getResources().getDimension(R.dimen.page_indicator_dot_size) / 2.0f;
        setOutlineProvider(new MyOutlineProver());
        this.mActiveColor = Themes.getColorAccent(context);
        this.mInActiveColor = Themes.getAttrColor(context, 16843820);
        this.mIsRtl = Utilities.isRtl(getResources());
    }

    @Override // com.android.launcher3.pageindicators.PageIndicator
    public void setScroll(int i, int i2) {
        if (this.mNumPages > 1) {
            if (this.mIsRtl) {
                i = i2 - i;
            }
            int i3 = i2 / (this.mNumPages - 1);
            int i4 = i / i3;
            int i5 = i4 * i3;
            int i6 = i5 + i3;
            float f = 0.1f * i3;
            float f2 = i;
            if (f2 < i5 + f) {
                animateToPosition(i4);
            } else if (f2 > i6 - f) {
                animateToPosition(i4 + 1);
            } else {
                animateToPosition(i4 + 0.5f);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateToPosition(float f) {
        this.mFinalPosition = f;
        if (Math.abs(this.mCurrentPosition - this.mFinalPosition) < 0.1f) {
            this.mCurrentPosition = this.mFinalPosition;
        }
        if (this.mAnimator == null && Float.compare(this.mCurrentPosition, this.mFinalPosition) != 0) {
            this.mAnimator = ObjectAnimator.ofFloat(this, CURRENT_POSITION, this.mCurrentPosition > this.mFinalPosition ? this.mCurrentPosition - 0.5f : this.mCurrentPosition + 0.5f);
            this.mAnimator.addListener(new AnimationCycleListener());
            this.mAnimator.setDuration(ANIMATION_DURATION);
            this.mAnimator.start();
        }
    }

    public void stopAllAnimations() {
        if (this.mAnimator != null) {
            this.mAnimator.cancel();
            this.mAnimator = null;
        }
        this.mFinalPosition = this.mActivePage;
        CURRENT_POSITION.set(this, Float.valueOf(this.mFinalPosition));
    }

    public void prepareEntryAnimation() {
        this.mEntryAnimationRadiusFactors = new float[this.mNumPages];
        invalidate();
    }

    public void playEntryAnimation() {
        int length = this.mEntryAnimationRadiusFactors.length;
        if (length == 0) {
            this.mEntryAnimationRadiusFactors = null;
            invalidate();
            return;
        }
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(ENTER_ANIMATION_OVERSHOOT_TENSION);
        AnimatorSet animatorSet = new AnimatorSet();
        for (final int i = 0; i < length; i++) {
            ValueAnimator duration = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(400L);
            duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.launcher3.pageindicators.PageIndicatorDots.2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    PageIndicatorDots.this.mEntryAnimationRadiusFactors[i] = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    PageIndicatorDots.this.invalidate();
                }
            });
            duration.setInterpolator(overshootInterpolator);
            duration.setStartDelay(300 + (150 * i));
            animatorSet.play(duration);
        }
        animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.pageindicators.PageIndicatorDots.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PageIndicatorDots.this.mEntryAnimationRadiusFactors = null;
                PageIndicatorDots.this.invalidateOutline();
                PageIndicatorDots.this.invalidate();
            }
        });
        animatorSet.start();
    }

    @Override // com.android.launcher3.pageindicators.PageIndicator
    public void setActiveMarker(int i) {
        if (this.mActivePage != i) {
            this.mActivePage = i;
        }
    }

    @Override // com.android.launcher3.pageindicators.PageIndicator
    public void setMarkersCount(int i) {
        this.mNumPages = i;
        requestLayout();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(View.MeasureSpec.getMode(i) == 1073741824 ? View.MeasureSpec.getSize(i) : (int) (((this.mNumPages * 3) + 2) * this.mDotRadius), View.MeasureSpec.getMode(i2) == 1073741824 ? View.MeasureSpec.getSize(i2) : (int) (4.0f * this.mDotRadius));
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        float f = 3.0f * this.mDotRadius;
        float width = (((getWidth() - (this.mNumPages * f)) + this.mDotRadius) / 2.0f) + this.mDotRadius;
        float height = canvas.getHeight() / 2;
        int i = 0;
        if (this.mEntryAnimationRadiusFactors != null) {
            if (this.mIsRtl) {
                width = getWidth() - width;
                f = -f;
            }
            while (i < this.mEntryAnimationRadiusFactors.length) {
                this.mCirclePaint.setColor(i == this.mActivePage ? this.mActiveColor : this.mInActiveColor);
                canvas.drawCircle(width, height, this.mDotRadius * this.mEntryAnimationRadiusFactors[i], this.mCirclePaint);
                width += f;
                i++;
            }
            return;
        }
        this.mCirclePaint.setColor(this.mInActiveColor);
        while (i < this.mNumPages) {
            canvas.drawCircle(width, height, this.mDotRadius, this.mCirclePaint);
            width += f;
            i++;
        }
        this.mCirclePaint.setColor(this.mActiveColor);
        canvas.drawRoundRect(getActiveRect(), this.mDotRadius, this.mDotRadius, this.mCirclePaint);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public RectF getActiveRect() {
        float f = (int) this.mCurrentPosition;
        float f2 = this.mCurrentPosition - f;
        float f3 = 3.0f * this.mDotRadius;
        sTempRect.top = (getHeight() * 0.5f) - this.mDotRadius;
        sTempRect.bottom = (getHeight() * 0.5f) + this.mDotRadius;
        sTempRect.left = (((getWidth() - (this.mNumPages * f3)) + this.mDotRadius) / 2.0f) + (f * f3);
        sTempRect.right = sTempRect.left + (this.mDotRadius * 2.0f);
        if (f2 < 0.5f) {
            sTempRect.right += f2 * f3 * 2.0f;
        } else {
            sTempRect.right += f3;
            sTempRect.left += (f2 - 0.5f) * f3 * 2.0f;
        }
        if (this.mIsRtl) {
            float width = sTempRect.width();
            sTempRect.right = getWidth() - sTempRect.left;
            sTempRect.left = sTempRect.right - width;
        }
        return sTempRect;
    }

    /* loaded from: classes.dex */
    private class MyOutlineProver extends ViewOutlineProvider {
        private MyOutlineProver() {
        }

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            if (PageIndicatorDots.this.mEntryAnimationRadiusFactors == null) {
                RectF activeRect = PageIndicatorDots.this.getActiveRect();
                outline.setRoundRect((int) activeRect.left, (int) activeRect.top, (int) activeRect.right, (int) activeRect.bottom, PageIndicatorDots.this.mDotRadius);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AnimationCycleListener extends AnimatorListenerAdapter {
        private boolean mCancelled;

        private AnimationCycleListener() {
            this.mCancelled = false;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            this.mCancelled = true;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (!this.mCancelled) {
                PageIndicatorDots.this.mAnimator = null;
                PageIndicatorDots.this.animateToPosition(PageIndicatorDots.this.mFinalPosition);
            }
        }
    }
}
