package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/assist/AssistOrbView.class */
public class AssistOrbView extends FrameLayout {
    private final Paint mBackgroundPaint;
    private final int mBaseMargin;
    private float mCircleAnimationEndValue;
    private ValueAnimator mCircleAnimator;
    private final int mCircleMinSize;
    private final Rect mCircleRect;
    private float mCircleSize;
    private ValueAnimator.AnimatorUpdateListener mCircleUpdateListener;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mClipToOutline;
    private ImageView mLogo;
    private final int mMaxElevation;
    private float mOffset;
    private ValueAnimator mOffsetAnimator;
    private ValueAnimator.AnimatorUpdateListener mOffsetUpdateListener;
    private float mOutlineAlpha;
    private final Interpolator mOvershootInterpolator;
    private final int mStaticOffset;
    private final Rect mStaticRect;

    public AssistOrbView(Context context) {
        this(context, null);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mBackgroundPaint = new Paint();
        this.mCircleRect = new Rect();
        this.mStaticRect = new Rect();
        this.mOvershootInterpolator = new OvershootInterpolator();
        this.mCircleUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.assist.AssistOrbView.1
            final AssistOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.applyCircleSize(((Float) valueAnimator.getAnimatedValue()).floatValue());
                this.this$0.updateElevation();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.assist.AssistOrbView.2
            final AssistOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mCircleAnimator = null;
            }
        };
        this.mOffsetUpdateListener = new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.assist.AssistOrbView.3
            final AssistOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mOffset = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                this.this$0.updateLayout();
            }
        };
        setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.assist.AssistOrbView.4
            final AssistOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                if (this.this$0.mCircleSize > 0.0f) {
                    outline.setOval(this.this$0.mCircleRect);
                } else {
                    outline.setEmpty();
                }
                outline.setAlpha(this.this$0.mOutlineAlpha);
            }
        });
        setWillNotDraw(false);
        this.mCircleMinSize = context.getResources().getDimensionPixelSize(2131689927);
        this.mBaseMargin = context.getResources().getDimensionPixelSize(2131689928);
        this.mStaticOffset = context.getResources().getDimensionPixelSize(2131689929);
        this.mMaxElevation = context.getResources().getDimensionPixelSize(2131689930);
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setColor(getResources().getColor(2131558564));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateOffset(float f, long j, long j2, Interpolator interpolator) {
        if (this.mOffsetAnimator != null) {
            this.mOffsetAnimator.removeAllListeners();
            this.mOffsetAnimator.cancel();
        }
        this.mOffsetAnimator = ValueAnimator.ofFloat(this.mOffset, f);
        this.mOffsetAnimator.addUpdateListener(this.mOffsetUpdateListener);
        this.mOffsetAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.assist.AssistOrbView.5
            final AssistOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mOffsetAnimator = null;
            }
        });
        this.mOffsetAnimator.setInterpolator(interpolator);
        this.mOffsetAnimator.setStartDelay(j2);
        this.mOffsetAnimator.setDuration(j);
        this.mOffsetAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyCircleSize(float f) {
        this.mCircleSize = f;
        updateLayout();
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawCircle(this.mCircleRect.centerX(), this.mCircleRect.centerY(), this.mCircleSize / 2.0f, this.mBackgroundPaint);
    }

    private void updateCircleRect() {
        updateCircleRect(this.mCircleRect, this.mOffset, false);
    }

    private void updateCircleRect(Rect rect, float f, boolean z) {
        float f2 = z ? this.mCircleMinSize : this.mCircleSize;
        int width = ((int) (getWidth() - f2)) / 2;
        int height = (int) (((getHeight() - (f2 / 2.0f)) - this.mBaseMargin) - f);
        rect.set(width, height, (int) (width + f2), (int) (height + f2));
    }

    private void updateClipping() {
        boolean z = this.mCircleSize < ((float) this.mCircleMinSize);
        if (z != this.mClipToOutline) {
            setClipToOutline(z);
            this.mClipToOutline = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateElevation() {
        setElevation((1.0f - Math.max((this.mStaticOffset - this.mOffset) / this.mStaticOffset, 0.0f)) * this.mMaxElevation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLayout() {
        updateCircleRect();
        updateLogo();
        invalidateOutline();
        invalidate();
        updateClipping();
    }

    private void updateLogo() {
        float f = this.mCircleMinSize / 7.0f;
        float f2 = (this.mStaticOffset - this.mOffset) / this.mStaticOffset;
        float f3 = this.mStaticOffset;
        this.mLogo.setImageAlpha((int) (255.0f * Math.max(((1.0f - f2) - 0.5f) * 2.0f, 0.0f)));
        this.mLogo.setTranslationX(((this.mCircleRect.left + this.mCircleRect.right) / 2.0f) - (this.mLogo.getWidth() / 2.0f));
        this.mLogo.setTranslationY(((((this.mCircleRect.top + this.mCircleRect.bottom) / 2.0f) - (this.mLogo.getHeight() / 2.0f)) - f) + (f3 * f2 * 0.1f));
    }

    public void animateCircleSize(float f, long j, long j2, Interpolator interpolator) {
        if (f == this.mCircleAnimationEndValue) {
            return;
        }
        if (this.mCircleAnimator != null) {
            this.mCircleAnimator.cancel();
        }
        this.mCircleAnimator = ValueAnimator.ofFloat(this.mCircleSize, f);
        this.mCircleAnimator.addUpdateListener(this.mCircleUpdateListener);
        this.mCircleAnimator.addListener(this.mClearAnimatorListener);
        this.mCircleAnimator.setInterpolator(interpolator);
        this.mCircleAnimator.setDuration(j);
        this.mCircleAnimator.setStartDelay(j2);
        this.mCircleAnimator.start();
        this.mCircleAnimationEndValue = f;
    }

    public ImageView getLogo() {
        return this.mLogo;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLogo = (ImageView) findViewById(2131886253);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mLogo.layout(0, 0, this.mLogo.getMeasuredWidth(), this.mLogo.getMeasuredHeight());
        if (z) {
            updateCircleRect(this.mStaticRect, this.mStaticOffset, true);
        }
    }

    public void reset() {
        this.mClipToOutline = false;
        this.mBackgroundPaint.setAlpha(255);
        this.mOutlineAlpha = 1.0f;
    }

    public void startEnterAnimation() {
        applyCircleSize(0.0f);
        post(new Runnable(this) { // from class: com.android.systemui.assist.AssistOrbView.6
            final AssistOrbView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.animateCircleSize(this.this$0.mCircleMinSize, 300L, 0L, this.this$0.mOvershootInterpolator);
                this.this$0.animateOffset(this.this$0.mStaticOffset, 400L, 0L, Interpolators.LINEAR_OUT_SLOW_IN);
            }
        });
    }

    public void startExitAnimation(long j) {
        animateCircleSize(0.0f, 200L, j, Interpolators.FAST_OUT_LINEAR_IN);
        animateOffset(0.0f, 200L, j, Interpolators.FAST_OUT_LINEAR_IN);
    }
}
