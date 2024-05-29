package com.android.launcher3.folder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Region;
import android.graphics.Shader;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.view.View;
import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.util.Themes;
/* loaded from: classes.dex */
public class PreviewBackground {
    private static final float ACCEPT_COLOR_MULTIPLIER = 1.5f;
    private static final float ACCEPT_SCALE_FACTOR = 1.2f;
    private static final int BG_OPACITY = 160;
    private static final int CONSUMPTION_ANIMATION_DURATION = 100;
    private static final int MAX_BG_OPACITY = 225;
    private static final int SHADOW_OPACITY = 40;
    int basePreviewOffsetX;
    int basePreviewOffsetY;
    public int delegateCellX;
    public int delegateCellY;
    private int mBgColor;
    private CellLayout mDrawingDelegate;
    private View mInvalidateDelegate;
    private ValueAnimator mScaleAnimator;
    private ObjectAnimator mShadowAnimator;
    private ObjectAnimator mStrokeAlphaAnimator;
    private float mStrokeWidth;
    int previewSize;
    private static final Property<PreviewBackground, Integer> STROKE_ALPHA = new Property<PreviewBackground, Integer>(Integer.class, "strokeAlpha") { // from class: com.android.launcher3.folder.PreviewBackground.1
        @Override // android.util.Property
        public Integer get(PreviewBackground previewBackground) {
            return Integer.valueOf(previewBackground.mStrokeAlpha);
        }

        @Override // android.util.Property
        public void set(PreviewBackground previewBackground, Integer num) {
            previewBackground.mStrokeAlpha = num.intValue();
            previewBackground.invalidate();
        }
    };
    private static final Property<PreviewBackground, Integer> SHADOW_ALPHA = new Property<PreviewBackground, Integer>(Integer.class, "shadowAlpha") { // from class: com.android.launcher3.folder.PreviewBackground.2
        @Override // android.util.Property
        public Integer get(PreviewBackground previewBackground) {
            return Integer.valueOf(previewBackground.mShadowAlpha);
        }

        @Override // android.util.Property
        public void set(PreviewBackground previewBackground, Integer num) {
            previewBackground.mShadowAlpha = num.intValue();
            previewBackground.invalidate();
        }
    };
    private final PorterDuffXfermode mClipPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    private final RadialGradient mClipShader = new RadialGradient(0.0f, 0.0f, 1.0f, new int[]{ViewCompat.MEASURED_STATE_MASK, ViewCompat.MEASURED_STATE_MASK, 0}, new float[]{0.0f, 0.999f, 1.0f}, Shader.TileMode.CLAMP);
    private final PorterDuffXfermode mShadowPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    private RadialGradient mShadowShader = null;
    private final Matrix mShaderMatrix = new Matrix();
    private final Path mPath = new Path();
    private final Paint mPaint = new Paint(1);
    float mScale = 1.0f;
    private float mColorMultiplier = 1.0f;
    private int mStrokeAlpha = MAX_BG_OPACITY;
    private int mShadowAlpha = 255;
    public boolean isClipping = true;

    public void setup(Launcher launcher, View view, int i, int i2) {
        this.mInvalidateDelegate = view;
        this.mBgColor = Themes.getAttrColor(launcher, 16843827);
        DeviceProfile deviceProfile = launcher.getDeviceProfile();
        this.previewSize = deviceProfile.folderIconSizePx;
        this.basePreviewOffsetX = (i - this.previewSize) / 2;
        this.basePreviewOffsetY = i2 + deviceProfile.folderIconOffsetYPx;
        this.mStrokeWidth = launcher.getResources().getDisplayMetrics().density;
        float scaledRadius = getScaledRadius();
        this.mShadowShader = new RadialGradient(0.0f, 0.0f, 1.0f, new int[]{Color.argb(40, 0, 0, 0), 0}, new float[]{scaledRadius / (this.mStrokeWidth + scaledRadius), 1.0f}, Shader.TileMode.CLAMP);
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getRadius() {
        return this.previewSize / 2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getScaledRadius() {
        return (int) (this.mScale * getRadius());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getOffsetX() {
        return this.basePreviewOffsetX - (getScaledRadius() - getRadius());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getOffsetY() {
        return this.basePreviewOffsetY - (getScaledRadius() - getRadius());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getScaleProgress() {
        return (this.mScale - 1.0f) / 0.20000005f;
    }

    void invalidate() {
        if (this.mInvalidateDelegate != null) {
            this.mInvalidateDelegate.invalidate();
        }
        if (this.mDrawingDelegate != null) {
            this.mDrawingDelegate.invalidate();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setInvalidateDelegate(View view) {
        this.mInvalidateDelegate = view;
        invalidate();
    }

    public int getBgColor() {
        return ColorUtils.setAlphaComponent(this.mBgColor, (int) Math.min(225.0f, 160.0f * this.mColorMultiplier));
    }

    public int getBadgeColor() {
        return this.mBgColor;
    }

    public void drawBackground(Canvas canvas) {
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(getBgColor());
        drawCircle(canvas, 0.0f);
        drawShadow(canvas);
    }

    public void drawShadow(Canvas canvas) {
        int save;
        if (this.mShadowShader == null) {
            return;
        }
        float scaledRadius = getScaledRadius();
        float f = this.mStrokeWidth + scaledRadius;
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        int offsetX = getOffsetX();
        int offsetY = getOffsetY();
        if (canvas.isHardwareAccelerated()) {
            float f2 = offsetX;
            float f3 = offsetY;
            save = canvas.saveLayer(f2 - this.mStrokeWidth, f3, f2 + scaledRadius + f, f3 + f + f, null);
        } else {
            save = canvas.save();
            canvas.clipPath(getClipPath(), Region.Op.DIFFERENCE);
        }
        this.mShaderMatrix.setScale(f, f);
        float f4 = offsetX + scaledRadius;
        float f5 = offsetY;
        this.mShaderMatrix.postTranslate(f4, f + f5);
        this.mShadowShader.setLocalMatrix(this.mShaderMatrix);
        this.mPaint.setAlpha(this.mShadowAlpha);
        this.mPaint.setShader(this.mShadowShader);
        canvas.drawPaint(this.mPaint);
        this.mPaint.setAlpha(255);
        this.mPaint.setShader(null);
        if (canvas.isHardwareAccelerated()) {
            this.mPaint.setXfermode(this.mShadowPorterDuffXfermode);
            canvas.drawCircle(f4, f5 + scaledRadius, scaledRadius, this.mPaint);
            this.mPaint.setXfermode(null);
        }
        canvas.restoreToCount(save);
    }

    public void fadeInBackgroundShadow() {
        if (this.mShadowAnimator != null) {
            this.mShadowAnimator.cancel();
        }
        this.mShadowAnimator = ObjectAnimator.ofInt(this, SHADOW_ALPHA, 0, 255).setDuration(100L);
        this.mShadowAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.folder.PreviewBackground.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PreviewBackground.this.mShadowAnimator = null;
            }
        });
        this.mShadowAnimator.start();
    }

    public void animateBackgroundStroke() {
        if (this.mStrokeAlphaAnimator != null) {
            this.mStrokeAlphaAnimator.cancel();
        }
        this.mStrokeAlphaAnimator = ObjectAnimator.ofInt(this, STROKE_ALPHA, AbstractFloatingView.TYPE_REBIND_SAFE, MAX_BG_OPACITY).setDuration(100L);
        this.mStrokeAlphaAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.folder.PreviewBackground.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PreviewBackground.this.mStrokeAlphaAnimator = null;
            }
        });
        this.mStrokeAlphaAnimator.start();
    }

    public void drawBackgroundStroke(Canvas canvas) {
        this.mPaint.setColor(ColorUtils.setAlphaComponent(this.mBgColor, this.mStrokeAlpha));
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeWidth(this.mStrokeWidth);
        drawCircle(canvas, 1.0f);
    }

    public void drawLeaveBehind(Canvas canvas) {
        float f = this.mScale;
        this.mScale = 0.5f;
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(Color.argb(160, 245, 245, 245));
        drawCircle(canvas, 0.0f);
        this.mScale = f;
    }

    private void drawCircle(Canvas canvas, float f) {
        float scaledRadius = getScaledRadius();
        canvas.drawCircle(getOffsetX() + scaledRadius, getOffsetY() + scaledRadius, scaledRadius - f, this.mPaint);
    }

    public Path getClipPath() {
        this.mPath.reset();
        float scaledRadius = getScaledRadius();
        this.mPath.addCircle(getOffsetX() + scaledRadius, getOffsetY() + scaledRadius, scaledRadius, Path.Direction.CW);
        return this.mPath;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clipCanvasHardware(Canvas canvas) {
        this.mPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setXfermode(this.mClipPorterDuffXfermode);
        float scaledRadius = getScaledRadius();
        this.mShaderMatrix.setScale(scaledRadius, scaledRadius);
        this.mShaderMatrix.postTranslate(getOffsetX() + scaledRadius, scaledRadius + getOffsetY());
        this.mClipShader.setLocalMatrix(this.mShaderMatrix);
        this.mPaint.setShader(this.mClipShader);
        canvas.drawPaint(this.mPaint);
        this.mPaint.setXfermode(null);
        this.mPaint.setShader(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void delegateDrawing(CellLayout cellLayout, int i, int i2) {
        if (this.mDrawingDelegate != cellLayout) {
            cellLayout.addFolderBackground(this);
        }
        this.mDrawingDelegate = cellLayout;
        this.delegateCellX = i;
        this.delegateCellY = i2;
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearDrawingDelegate() {
        if (this.mDrawingDelegate != null) {
            this.mDrawingDelegate.removeFolderBackground(this);
        }
        this.mDrawingDelegate = null;
        this.isClipping = true;
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean drawingDelegated() {
        return this.mDrawingDelegate != null;
    }

    private void animateScale(final float f, final float f2, final Runnable runnable, final Runnable runnable2) {
        final float f3 = this.mScale;
        final float f4 = this.mColorMultiplier;
        if (this.mScaleAnimator != null) {
            this.mScaleAnimator.cancel();
        }
        this.mScaleAnimator = LauncherAnimUtils.ofFloat(0.0f, 1.0f);
        this.mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.launcher3.folder.PreviewBackground.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                float f5 = 1.0f - animatedFraction;
                PreviewBackground.this.mScale = (f * animatedFraction) + (f3 * f5);
                PreviewBackground.this.mColorMultiplier = (animatedFraction * f2) + (f5 * f4);
                PreviewBackground.this.invalidate();
            }
        });
        this.mScaleAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.launcher3.folder.PreviewBackground.6
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (runnable != null) {
                    runnable.run();
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (runnable2 != null) {
                    runnable2.run();
                }
                PreviewBackground.this.mScaleAnimator = null;
            }
        });
        this.mScaleAnimator.setDuration(100L);
        this.mScaleAnimator.start();
    }

    public void animateToAccept(final CellLayout cellLayout, final int i, final int i2) {
        animateScale(ACCEPT_SCALE_FACTOR, ACCEPT_COLOR_MULTIPLIER, new Runnable() { // from class: com.android.launcher3.folder.PreviewBackground.7
            @Override // java.lang.Runnable
            public void run() {
                PreviewBackground.this.delegateDrawing(cellLayout, i, i2);
            }
        }, null);
    }

    public void animateToRest() {
        final CellLayout cellLayout = this.mDrawingDelegate;
        final int i = this.delegateCellX;
        final int i2 = this.delegateCellY;
        animateScale(1.0f, 1.0f, new Runnable() { // from class: com.android.launcher3.folder.PreviewBackground.8
            @Override // java.lang.Runnable
            public void run() {
                PreviewBackground.this.delegateDrawing(cellLayout, i, i2);
            }
        }, new Runnable() { // from class: com.android.launcher3.folder.PreviewBackground.9
            @Override // java.lang.Runnable
            public void run() {
                PreviewBackground.this.clearDrawingDelegate();
            }
        });
    }

    public int getBackgroundAlpha() {
        return (int) Math.min(225.0f, 160.0f * this.mColorMultiplier);
    }

    public float getStrokeWidth() {
        return this.mStrokeWidth;
    }
}
