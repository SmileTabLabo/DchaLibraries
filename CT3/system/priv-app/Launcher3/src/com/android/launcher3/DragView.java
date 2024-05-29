package com.android.launcher3;

import android.animation.FloatArrayEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.android.launcher3.DragLayer;
import com.mediatek.launcher3.LauncherHelper;
import com.mediatek.launcher3.LauncherLog;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/DragView.class */
public class DragView extends View {
    public static int COLOR_CHANGE_DURATION = 120;
    static float sDragAlpha = 1.0f;
    ValueAnimator mAnim;
    private Bitmap mBitmap;
    private Bitmap mCrossFadeBitmap;
    float mCrossFadeProgress;
    float[] mCurrentFilter;
    private DragLayer mDragLayer;
    private Rect mDragRegion;
    private Point mDragVisualizeOffset;
    private ValueAnimator mFilterAnimator;
    private boolean mHasDrawn;
    private float mInitialScale;
    private float mIntrinsicIconScale;
    float mOffsetX;
    float mOffsetY;
    Paint mPaint;
    private int mRegistrationX;
    private int mRegistrationY;

    @TargetApi(21)
    public DragView(Launcher launcher, Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, float f) {
        super(launcher);
        this.mDragVisualizeOffset = null;
        this.mDragRegion = null;
        this.mDragLayer = null;
        this.mHasDrawn = false;
        this.mCrossFadeProgress = 0.0f;
        this.mOffsetX = 0.0f;
        this.mOffsetY = 0.0f;
        this.mInitialScale = 1.0f;
        this.mIntrinsicIconScale = 1.0f;
        this.mDragLayer = launcher.getDragLayer();
        this.mInitialScale = f;
        setScaleX(f);
        setScaleY(f);
        this.mAnim = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        this.mAnim.setDuration(150L);
        this.mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, f, (i5 + getResources().getDimensionPixelSize(2131230799)) / i5) { // from class: com.android.launcher3.DragView.1
            final DragView this$0;
            final float val$initialScale;
            final float val$scale;

            {
                this.this$0 = this;
                this.val$initialScale = f;
                this.val$scale = r6;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                int i7 = (int) (-this.this$0.mOffsetX);
                int i8 = (int) (-this.this$0.mOffsetY);
                this.this$0.mOffsetX += i7;
                this.this$0.mOffsetY += i8;
                this.this$0.setScaleX(this.val$initialScale + ((this.val$scale - this.val$initialScale) * floatValue));
                this.this$0.setScaleY(this.val$initialScale + ((this.val$scale - this.val$initialScale) * floatValue));
                if (DragView.sDragAlpha != 1.0f) {
                    this.this$0.setAlpha((DragView.sDragAlpha * floatValue) + (1.0f - floatValue));
                }
                if (this.this$0.getParent() == null) {
                    valueAnimator.cancel();
                    return;
                }
                this.this$0.setTranslationX(this.this$0.getTranslationX() + i7);
                this.this$0.setTranslationY(this.this$0.getTranslationY() + i8);
            }
        });
        this.mBitmap = Bitmap.createBitmap(bitmap, i3, i4, i5, i6);
        setDragRegion(new Rect(0, 0, i5, i6));
        this.mRegistrationX = i;
        this.mRegistrationY = i2;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("DragView", "DragView constructor: mRegistrationX = " + this.mRegistrationX + ", mRegistrationY = " + this.mRegistrationY + ", this = " + this);
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        measure(makeMeasureSpec, makeMeasureSpec);
        this.mPaint = new Paint(2);
        if (Utilities.ATLEAST_LOLLIPOP) {
            setElevation(getResources().getDimension(2131230800));
        }
    }

    @TargetApi(21)
    private void animateFilterTo(float[] fArr) {
        float[] array = this.mCurrentFilter == null ? new ColorMatrix().getArray() : this.mCurrentFilter;
        this.mCurrentFilter = Arrays.copyOf(array, array.length);
        if (this.mFilterAnimator != null) {
            this.mFilterAnimator.cancel();
        }
        this.mFilterAnimator = ValueAnimator.ofObject(new FloatArrayEvaluator(this.mCurrentFilter), array, fArr);
        this.mFilterAnimator.setDuration(COLOR_CHANGE_DURATION);
        this.mFilterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.launcher3.DragView.3
            final DragView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mPaint.setColorFilter(new ColorMatrixColorFilter(this.this$0.mCurrentFilter));
                this.this$0.invalidate();
            }
        });
        this.mFilterAnimator.start();
    }

    public static void setColorScale(int i, ColorMatrix colorMatrix) {
        colorMatrix.setScale(Color.red(i) / 255.0f, Color.green(i) / 255.0f, Color.blue(i) / 255.0f, Color.alpha(i) / 255.0f);
    }

    public void cancelAnimation() {
        if (this.mAnim == null || !this.mAnim.isRunning()) {
            return;
        }
        this.mAnim.cancel();
    }

    public void crossFade(int i) {
        ValueAnimator ofFloat = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        ofFloat.setDuration(i);
        ofFloat.setInterpolator(new DecelerateInterpolator(1.5f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.launcher3.DragView.2
            final DragView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mCrossFadeProgress = valueAnimator.getAnimatedFraction();
            }
        });
        ofFloat.start();
    }

    public Rect getDragRegion() {
        return this.mDragRegion;
    }

    public int getDragRegionTop() {
        return this.mDragRegion.top;
    }

    public int getDragRegionWidth() {
        return this.mDragRegion.width();
    }

    public Point getDragVisualizeOffset() {
        return this.mDragVisualizeOffset;
    }

    public float getIntrinsicIconScaleFactor() {
        return this.mIntrinsicIconScale;
    }

    public boolean hasDrawn() {
        return this.mHasDrawn;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void move(int i, int i2) {
        LauncherHelper.traceCounter(4L, "posX", i);
        LauncherHelper.traceCounter(4L, "posY", i2);
        LauncherHelper.beginSection("DragView.move");
        setTranslationX((i - this.mRegistrationX) + ((int) this.mOffsetX));
        setTranslationY((i2 - this.mRegistrationY) + ((int) this.mOffsetY));
        LauncherHelper.endSection();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        boolean z = true;
        this.mHasDrawn = true;
        if (this.mCrossFadeProgress <= 0.0f || this.mCrossFadeBitmap == null) {
            z = false;
        }
        if (z) {
            this.mPaint.setAlpha(z ? (int) ((1.0f - this.mCrossFadeProgress) * 255.0f) : 255);
        }
        canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, this.mPaint);
        if (z) {
            this.mPaint.setAlpha((int) (this.mCrossFadeProgress * 255.0f));
            canvas.save();
            canvas.scale((this.mBitmap.getWidth() * 1.0f) / this.mCrossFadeBitmap.getWidth(), (this.mBitmap.getHeight() * 1.0f) / this.mCrossFadeBitmap.getHeight());
            canvas.drawBitmap(this.mCrossFadeBitmap, 0.0f, 0.0f, this.mPaint);
            canvas.restore();
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(this.mBitmap.getWidth(), this.mBitmap.getHeight());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void remove() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("DragView", "remove DragView: this = " + this);
        }
        if (getParent() != null) {
            this.mDragLayer.removeView(this);
        }
    }

    public void resetLayoutParams() {
        this.mOffsetY = 0.0f;
        this.mOffsetX = 0.0f;
        requestLayout();
    }

    @Override // android.view.View
    public void setAlpha(float f) {
        super.setAlpha(f);
        this.mPaint.setAlpha((int) (255.0f * f));
        invalidate();
    }

    public void setColor(int i) {
        if (this.mPaint == null) {
            this.mPaint = new Paint(2);
        }
        if (i == 0) {
            if (Utilities.ATLEAST_LOLLIPOP && this.mCurrentFilter != null) {
                animateFilterTo(new ColorMatrix().getArray());
                return;
            }
            this.mPaint.setColorFilter(null);
            invalidate();
            return;
        }
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        ColorMatrix colorMatrix2 = new ColorMatrix();
        setColorScale(i, colorMatrix2);
        colorMatrix.postConcat(colorMatrix2);
        if (Utilities.ATLEAST_LOLLIPOP) {
            animateFilterTo(colorMatrix.getArray());
            return;
        }
        this.mPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        invalidate();
    }

    public void setCrossFadeBitmap(Bitmap bitmap) {
        this.mCrossFadeBitmap = bitmap;
    }

    public void setDragRegion(Rect rect) {
        this.mDragRegion = rect;
    }

    public void setDragVisualizeOffset(Point point) {
        this.mDragVisualizeOffset = point;
    }

    public void setIntrinsicIconScaleFactor(float f) {
        this.mIntrinsicIconScale = f;
    }

    public void show(int i, int i2) {
        this.mDragLayer.addView(this);
        DragLayer.LayoutParams layoutParams = new DragLayer.LayoutParams(0, 0);
        layoutParams.width = this.mBitmap.getWidth();
        layoutParams.height = this.mBitmap.getHeight();
        layoutParams.customPosition = true;
        setLayoutParams(layoutParams);
        setTranslationX(i - this.mRegistrationX);
        setTranslationY(i2 - this.mRegistrationY);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("DragView", "show DragView: x = " + layoutParams.x + ", y = " + layoutParams.y + ", width = " + layoutParams.width + ", height = " + layoutParams.height + ", this = " + this);
        }
        post(new Runnable(this) { // from class: com.android.launcher3.DragView.4
            final DragView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mAnim.start();
            }
        });
    }

    public void updateInitialScaleToCurrentScale() {
        this.mInitialScale = getScaleX();
    }
}
