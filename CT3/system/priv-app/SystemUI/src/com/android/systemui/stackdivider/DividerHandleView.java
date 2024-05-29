package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/stackdivider/DividerHandleView.class */
public class DividerHandleView extends View {
    private AnimatorSet mAnimator;
    private final int mCircleDiameter;
    private int mCurrentHeight;
    private int mCurrentWidth;
    private final int mHeight;
    private final Paint mPaint;
    private boolean mTouching;
    private final int mWidth;
    private static final Property<DividerHandleView, Integer> WIDTH_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "width") { // from class: com.android.systemui.stackdivider.DividerHandleView.1
        @Override // android.util.Property
        public Integer get(DividerHandleView dividerHandleView) {
            return Integer.valueOf(dividerHandleView.mCurrentWidth);
        }

        @Override // android.util.Property
        public void set(DividerHandleView dividerHandleView, Integer num) {
            dividerHandleView.mCurrentWidth = num.intValue();
            dividerHandleView.invalidate();
        }
    };
    private static final Property<DividerHandleView, Integer> HEIGHT_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "height") { // from class: com.android.systemui.stackdivider.DividerHandleView.2
        @Override // android.util.Property
        public Integer get(DividerHandleView dividerHandleView) {
            return Integer.valueOf(dividerHandleView.mCurrentHeight);
        }

        @Override // android.util.Property
        public void set(DividerHandleView dividerHandleView, Integer num) {
            dividerHandleView.mCurrentHeight = num.intValue();
            dividerHandleView.invalidate();
        }
    };

    public DividerHandleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint = new Paint();
        this.mPaint.setColor(getResources().getColor(2131558588, null));
        this.mPaint.setAntiAlias(true);
        this.mWidth = getResources().getDimensionPixelSize(2131689980);
        this.mHeight = getResources().getDimensionPixelSize(2131689981);
        this.mCurrentWidth = this.mWidth;
        this.mCurrentHeight = this.mHeight;
        this.mCircleDiameter = (this.mWidth + this.mHeight) / 3;
    }

    private void animateToTarget(int i, int i2, boolean z) {
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this, WIDTH_PROPERTY, this.mCurrentWidth, i);
        ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this, HEIGHT_PROPERTY, this.mCurrentHeight, i2);
        this.mAnimator = new AnimatorSet();
        this.mAnimator.playTogether(ofInt, ofInt2);
        this.mAnimator.setDuration(z ? 150L : 200L);
        this.mAnimator.setInterpolator(z ? Interpolators.TOUCH_RESPONSE : Interpolators.FAST_OUT_SLOW_IN);
        this.mAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.stackdivider.DividerHandleView.3
            final DividerHandleView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mAnimator = null;
            }
        });
        this.mAnimator.start();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int width = (getWidth() / 2) - (this.mCurrentWidth / 2);
        int height = (getHeight() / 2) - (this.mCurrentHeight / 2);
        int min = Math.min(this.mCurrentWidth, this.mCurrentHeight) / 2;
        canvas.drawRoundRect(width, height, this.mCurrentWidth + width, this.mCurrentHeight + height, min, min, this.mPaint);
    }

    public void setTouching(boolean z, boolean z2) {
        if (z == this.mTouching) {
            return;
        }
        if (this.mAnimator != null) {
            this.mAnimator.cancel();
            this.mAnimator = null;
        }
        if (z2) {
            animateToTarget(z ? this.mCircleDiameter : this.mWidth, z ? this.mCircleDiameter : this.mHeight, z);
        } else {
            if (z) {
                this.mCurrentWidth = this.mCircleDiameter;
                this.mCurrentHeight = this.mCircleDiameter;
            } else {
                this.mCurrentWidth = this.mWidth;
                this.mCurrentHeight = this.mHeight;
            }
            invalidate();
        }
        this.mTouching = z;
    }
}
