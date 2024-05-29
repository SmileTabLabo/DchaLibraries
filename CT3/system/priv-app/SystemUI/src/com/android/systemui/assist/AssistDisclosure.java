package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import com.android.systemui.Interpolators;
/* loaded from: a.zip:com/android/systemui/assist/AssistDisclosure.class */
public class AssistDisclosure {
    private final Context mContext;
    private final Handler mHandler;
    private Runnable mShowRunnable = new Runnable(this) { // from class: com.android.systemui.assist.AssistDisclosure.1
        final AssistDisclosure this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.show();
        }
    };
    private AssistDisclosureView mView;
    private boolean mViewAdded;
    private final WindowManager mWm;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/assist/AssistDisclosure$AssistDisclosureView.class */
    public class AssistDisclosureView extends View implements ValueAnimator.AnimatorUpdateListener {
        private int mAlpha;
        private final ValueAnimator mAlphaInAnimator;
        private final ValueAnimator mAlphaOutAnimator;
        private final AnimatorSet mAnimator;
        private final Paint mPaint;
        private final Paint mShadowPaint;
        private float mShadowThickness;
        private float mThickness;
        private final ValueAnimator mTracingAnimator;
        private float mTracingProgress;
        final AssistDisclosure this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public AssistDisclosureView(AssistDisclosure assistDisclosure, Context context) {
            super(context);
            this.this$0 = assistDisclosure;
            this.mPaint = new Paint();
            this.mShadowPaint = new Paint();
            this.mTracingProgress = 0.0f;
            this.mAlpha = 0;
            this.mTracingAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(600L);
            this.mTracingAnimator.addUpdateListener(this);
            this.mTracingAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 2131230720));
            this.mAlphaInAnimator = ValueAnimator.ofInt(0, 255).setDuration(450L);
            this.mAlphaInAnimator.addUpdateListener(this);
            this.mAlphaInAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mAlphaOutAnimator = ValueAnimator.ofInt(255, 0).setDuration(400L);
            this.mAlphaOutAnimator.addUpdateListener(this);
            this.mAlphaOutAnimator.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            this.mAnimator = new AnimatorSet();
            this.mAnimator.play(this.mAlphaInAnimator).with(this.mTracingAnimator);
            this.mAnimator.play(this.mAlphaInAnimator).before(this.mAlphaOutAnimator);
            this.mAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.assist.AssistDisclosure.AssistDisclosureView.1
                boolean mCancelled;
                final AssistDisclosureView this$1;

                {
                    this.this$1 = this;
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
                    this.this$1.this$0.hide();
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.mCancelled = false;
                }
            });
            PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
            this.mPaint.setColor(-1);
            this.mPaint.setXfermode(porterDuffXfermode);
            this.mShadowPaint.setColor(-12303292);
            this.mShadowPaint.setXfermode(porterDuffXfermode);
            this.mThickness = getResources().getDimension(2131689973);
            this.mShadowThickness = getResources().getDimension(2131689974);
        }

        private void drawBeam(Canvas canvas, float f, float f2, float f3, float f4, Paint paint, float f5) {
            canvas.drawRect(f - f5, f2 - f5, f3 + f5, f4 + f5, paint);
        }

        private void drawGeometry(Canvas canvas, Paint paint, float f) {
            int width = getWidth();
            int height = getHeight();
            float f2 = this.mThickness;
            float f3 = this.mTracingProgress * ((width + height) - (2.0f * f2));
            float min = Math.min(f3, width / 2.0f);
            if (min > 0.0f) {
                drawBeam(canvas, (width / 2.0f) - min, height - f2, (width / 2.0f) + min, height, paint, f);
            }
            float min2 = Math.min(f3 - min, height - f2);
            if (min2 > 0.0f) {
                drawBeam(canvas, 0.0f, (height - f2) - min2, f2, height - f2, paint, f);
                drawBeam(canvas, width - f2, (height - f2) - min2, width, height - f2, paint, f);
            }
            float min3 = Math.min((f3 - min) - min2, (width / 2) - f2);
            if (min2 <= 0.0f || min3 <= 0.0f) {
                return;
            }
            drawBeam(canvas, f2, 0.0f, f2 + min3, f2, paint, f);
            drawBeam(canvas, (width - f2) - min3, 0.0f, width - f2, f2, paint, f);
        }

        private void startAnimation() {
            this.mAnimator.cancel();
            this.mAnimator.start();
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            if (valueAnimator == this.mAlphaOutAnimator) {
                this.mAlpha = ((Integer) this.mAlphaOutAnimator.getAnimatedValue()).intValue();
            } else if (valueAnimator == this.mAlphaInAnimator) {
                this.mAlpha = ((Integer) this.mAlphaInAnimator.getAnimatedValue()).intValue();
            } else if (valueAnimator == this.mTracingAnimator) {
                this.mTracingProgress = ((Float) this.mTracingAnimator.getAnimatedValue()).floatValue();
            }
            invalidate();
        }

        @Override // android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            startAnimation();
            sendAccessibilityEvent(16777216);
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mAnimator.cancel();
            this.mTracingProgress = 0.0f;
            this.mAlpha = 0;
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            this.mPaint.setAlpha(this.mAlpha);
            this.mShadowPaint.setAlpha(this.mAlpha / 4);
            drawGeometry(canvas, this.mShadowPaint, this.mShadowThickness);
            drawGeometry(canvas, this.mPaint, 0.0f);
        }
    }

    public AssistDisclosure(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWm = (WindowManager) this.mContext.getSystemService(WindowManager.class);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hide() {
        if (this.mViewAdded) {
            this.mWm.removeView(this.mView);
            this.mViewAdded = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void show() {
        if (this.mView == null) {
            this.mView = new AssistDisclosureView(this, this.mContext);
        }
        if (this.mViewAdded) {
            return;
        }
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(2015, 17302792, -3);
        layoutParams.setTitle("AssistDisclosure");
        this.mWm.addView(this.mView, layoutParams);
        this.mViewAdded = true;
    }

    public void postShow() {
        this.mHandler.removeCallbacks(this.mShowRunnable);
        this.mHandler.post(this.mShowRunnable);
    }
}
