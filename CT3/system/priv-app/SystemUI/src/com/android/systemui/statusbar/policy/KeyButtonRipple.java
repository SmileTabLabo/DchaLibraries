package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import java.util.ArrayList;
import java.util.HashSet;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyButtonRipple.class */
public class KeyButtonRipple extends Drawable {
    private CanvasProperty<Float> mBottomProp;
    private boolean mDrawingHardwareGlow;
    private CanvasProperty<Float> mLeftProp;
    private int mMaxWidth;
    private CanvasProperty<Paint> mPaintProp;
    private boolean mPressed;
    private CanvasProperty<Float> mRightProp;
    private Paint mRipplePaint;
    private CanvasProperty<Float> mRxProp;
    private CanvasProperty<Float> mRyProp;
    private boolean mSupportHardware;
    private final View mTargetView;
    private CanvasProperty<Float> mTopProp;
    private float mGlowAlpha = 0.0f;
    private float mGlowScale = 1.0f;
    private final Interpolator mInterpolator = new LogInterpolator(null);
    private final HashSet<Animator> mRunningAnimations = new HashSet<>();
    private final ArrayList<Animator> mTmpArray = new ArrayList<>();
    private final AnimatorListenerAdapter mAnimatorListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.policy.KeyButtonRipple.1
        final KeyButtonRipple this$0;

        {
            this.this$0 = this;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            this.this$0.mRunningAnimations.remove(animator);
            if (!this.this$0.mRunningAnimations.isEmpty() || this.this$0.mPressed) {
                return;
            }
            this.this$0.mDrawingHardwareGlow = false;
            this.this$0.invalidateSelf();
        }
    };

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyButtonRipple$LogInterpolator.class */
    private static final class LogInterpolator implements Interpolator {
        private LogInterpolator() {
        }

        /* synthetic */ LogInterpolator(LogInterpolator logInterpolator) {
            this();
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return 1.0f - ((float) Math.pow(400.0d, (-f) * 1.4d));
        }
    }

    public KeyButtonRipple(Context context, View view) {
        this.mMaxWidth = context.getResources().getDimensionPixelSize(2131689950);
        this.mTargetView = view;
    }

    private void cancelAnimations() {
        this.mTmpArray.addAll(this.mRunningAnimations);
        int size = this.mTmpArray.size();
        for (int i = 0; i < size; i++) {
            this.mTmpArray.get(i).cancel();
        }
        this.mTmpArray.clear();
        this.mRunningAnimations.clear();
    }

    private void drawHardware(DisplayListCanvas displayListCanvas) {
        if (this.mDrawingHardwareGlow) {
            displayListCanvas.drawRoundRect(this.mLeftProp, this.mTopProp, this.mRightProp, this.mBottomProp, this.mRxProp, this.mRyProp, this.mPaintProp);
        }
    }

    private void drawSoftware(Canvas canvas) {
        if (this.mGlowAlpha > 0.0f) {
            Paint ripplePaint = getRipplePaint();
            ripplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
            float width = getBounds().width();
            float height = getBounds().height();
            boolean z = width > height;
            float rippleSize = getRippleSize() * this.mGlowScale * 0.5f;
            float f = width * 0.5f;
            float f2 = height * 0.5f;
            float f3 = z ? rippleSize : f;
            if (z) {
                rippleSize = f2;
            }
            float f4 = z ? f2 : f;
            canvas.drawRoundRect(f - f3, f2 - rippleSize, f + f3, f2 + rippleSize, f4, f4, ripplePaint);
        }
    }

    private void enterHardware() {
        cancelAnimations();
        this.mDrawingHardwareGlow = true;
        setExtendStart(CanvasProperty.createFloat(getExtendSize() / 2));
        Animator renderNodeAnimator = new RenderNodeAnimator(getExtendStart(), (getExtendSize() / 2) - ((getRippleSize() * 1.35f) / 2.0f));
        renderNodeAnimator.setDuration(350L);
        renderNodeAnimator.setInterpolator(this.mInterpolator);
        renderNodeAnimator.addListener(this.mAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        setExtendEnd(CanvasProperty.createFloat(getExtendSize() / 2));
        Animator renderNodeAnimator2 = new RenderNodeAnimator(getExtendEnd(), (getExtendSize() / 2) + ((getRippleSize() * 1.35f) / 2.0f));
        renderNodeAnimator2.setDuration(350L);
        renderNodeAnimator2.setInterpolator(this.mInterpolator);
        renderNodeAnimator2.addListener(this.mAnimatorListener);
        renderNodeAnimator2.setTarget(this.mTargetView);
        if (isHorizontal()) {
            this.mTopProp = CanvasProperty.createFloat(0.0f);
            this.mBottomProp = CanvasProperty.createFloat(getBounds().height());
            this.mRxProp = CanvasProperty.createFloat(getBounds().height() / 2);
            this.mRyProp = CanvasProperty.createFloat(getBounds().height() / 2);
        } else {
            this.mLeftProp = CanvasProperty.createFloat(0.0f);
            this.mRightProp = CanvasProperty.createFloat(getBounds().width());
            this.mRxProp = CanvasProperty.createFloat(getBounds().width() / 2);
            this.mRyProp = CanvasProperty.createFloat(getBounds().width() / 2);
        }
        this.mGlowScale = 1.35f;
        this.mGlowAlpha = 0.2f;
        this.mRipplePaint = getRipplePaint();
        this.mRipplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        this.mPaintProp = CanvasProperty.createPaint(this.mRipplePaint);
        renderNodeAnimator.start();
        renderNodeAnimator2.start();
        this.mRunningAnimations.add(renderNodeAnimator);
        this.mRunningAnimations.add(renderNodeAnimator2);
        invalidateSelf();
    }

    private void enterSoftware() {
        cancelAnimations();
        this.mGlowAlpha = 0.2f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "glowScale", 0.0f, 1.35f);
        ofFloat.setInterpolator(this.mInterpolator);
        ofFloat.setDuration(350L);
        ofFloat.addListener(this.mAnimatorListener);
        ofFloat.start();
        this.mRunningAnimations.add(ofFloat);
    }

    private void exitHardware() {
        this.mPaintProp = CanvasProperty.createPaint(getRipplePaint());
        Animator renderNodeAnimator = new RenderNodeAnimator(this.mPaintProp, 1, 0.0f);
        renderNodeAnimator.setDuration(450L);
        renderNodeAnimator.setInterpolator(Interpolators.ALPHA_OUT);
        renderNodeAnimator.addListener(this.mAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        renderNodeAnimator.start();
        this.mRunningAnimations.add(renderNodeAnimator);
        invalidateSelf();
    }

    private void exitSoftware() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "glowAlpha", this.mGlowAlpha, 0.0f);
        ofFloat.setInterpolator(Interpolators.ALPHA_OUT);
        ofFloat.setDuration(450L);
        ofFloat.addListener(this.mAnimatorListener);
        ofFloat.start();
        this.mRunningAnimations.add(ofFloat);
    }

    private CanvasProperty<Float> getExtendEnd() {
        return isHorizontal() ? this.mRightProp : this.mBottomProp;
    }

    private int getExtendSize() {
        return isHorizontal() ? getBounds().width() : getBounds().height();
    }

    private CanvasProperty<Float> getExtendStart() {
        return isHorizontal() ? this.mLeftProp : this.mTopProp;
    }

    private Paint getRipplePaint() {
        if (this.mRipplePaint == null) {
            this.mRipplePaint = new Paint();
            this.mRipplePaint.setAntiAlias(true);
            this.mRipplePaint.setColor(-1);
        }
        return this.mRipplePaint;
    }

    private int getRippleSize() {
        return Math.min(isHorizontal() ? getBounds().width() : getBounds().height(), this.mMaxWidth);
    }

    private boolean isHorizontal() {
        return getBounds().width() > getBounds().height();
    }

    private void setExtendEnd(CanvasProperty<Float> canvasProperty) {
        if (isHorizontal()) {
            this.mRightProp = canvasProperty;
        } else {
            this.mBottomProp = canvasProperty;
        }
    }

    private void setExtendStart(CanvasProperty<Float> canvasProperty) {
        if (isHorizontal()) {
            this.mLeftProp = canvasProperty;
        } else {
            this.mTopProp = canvasProperty;
        }
    }

    private void setPressedHardware(boolean z) {
        if (z) {
            enterHardware();
        } else {
            exitHardware();
        }
    }

    private void setPressedSoftware(boolean z) {
        if (z) {
            enterSoftware();
        } else {
            exitSoftware();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        this.mSupportHardware = canvas.isHardwareAccelerated();
        if (this.mSupportHardware) {
            drawHardware((DisplayListCanvas) canvas);
        } else {
            drawSoftware(canvas);
        }
    }

    public float getGlowAlpha() {
        return this.mGlowAlpha;
    }

    public float getGlowScale() {
        return this.mGlowScale;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public void jumpToCurrentState() {
        cancelAnimations();
    }

    @Override // android.graphics.drawable.Drawable
    protected boolean onStateChange(int[] iArr) {
        boolean z;
        int i = 0;
        while (true) {
            z = false;
            if (i >= iArr.length) {
                break;
            } else if (iArr[i] == 16842919) {
                z = true;
                break;
            } else {
                i++;
            }
        }
        if (z != this.mPressed) {
            setPressed(z);
            this.mPressed = z;
            return true;
        }
        return false;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    public void setGlowAlpha(float f) {
        this.mGlowAlpha = f;
        invalidateSelf();
    }

    public void setGlowScale(float f) {
        this.mGlowScale = f;
        invalidateSelf();
    }

    public void setPressed(boolean z) {
        if (this.mSupportHardware) {
            setPressedHardware(z);
        } else {
            setPressedSoftware(z);
        }
    }
}
