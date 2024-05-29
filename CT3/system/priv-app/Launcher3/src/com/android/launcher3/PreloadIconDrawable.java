package com.android.launcher3;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.android.launcher3.FastBitmapDrawable;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/launcher3/PreloadIconDrawable.class */
public class PreloadIconDrawable extends Drawable {
    private static final Rect sTempRect = new Rect();
    private ObjectAnimator mAnimator;
    private Drawable mBgDrawable;
    final Drawable mIcon;
    private boolean mIndicatorRectDirty;
    private int mRingOutset;
    private final RectF mIndicatorRect = new RectF();
    private int mIndicatorColor = 0;
    private int mProgress = 0;
    private float mAnimationProgress = -1.0f;
    private final Paint mPaint = new Paint(1);

    public PreloadIconDrawable(Drawable drawable, Resources.Theme theme) {
        this.mIcon = drawable;
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        setBounds(drawable.getBounds());
        applyPreloaderTheme(theme);
        onLevelChange(0);
    }

    private int getIndicatorColor() {
        if (this.mIndicatorColor != 0) {
            return this.mIndicatorColor;
        }
        if (!(this.mIcon instanceof FastBitmapDrawable)) {
            this.mIndicatorColor = -16738680;
            return this.mIndicatorColor;
        }
        this.mIndicatorColor = Utilities.findDominantColorByHue(((FastBitmapDrawable) this.mIcon).getBitmap(), 20);
        float[] fArr = new float[3];
        Color.colorToHSV(this.mIndicatorColor, fArr);
        if (fArr[1] < 0.2f) {
            this.mIndicatorColor = -16738680;
            return this.mIndicatorColor;
        }
        fArr[2] = Math.max(0.6f, fArr[2]);
        this.mIndicatorColor = Color.HSVToColor(fArr);
        return this.mIndicatorColor;
    }

    private void initIndicatorRect() {
        Drawable drawable = this.mBgDrawable;
        Rect bounds = drawable.getBounds();
        drawable.getPadding(sTempRect);
        float width = bounds.width() / drawable.getIntrinsicWidth();
        float height = bounds.height() / drawable.getIntrinsicHeight();
        this.mIndicatorRect.set(bounds.left + (sTempRect.left * width), bounds.top + (sTempRect.top * height), bounds.right - (sTempRect.right * width), bounds.bottom - (sTempRect.bottom * height));
        float strokeWidth = this.mPaint.getStrokeWidth() / 2.0f;
        this.mIndicatorRect.inset(strokeWidth, strokeWidth);
        this.mIndicatorRectDirty = false;
    }

    public void applyPreloaderTheme(Resources.Theme theme) {
        TypedArray obtainStyledAttributes = theme.obtainStyledAttributes(R$styleable.PreloadIconDrawable);
        this.mBgDrawable = obtainStyledAttributes.getDrawable(0);
        this.mBgDrawable.setFilterBitmap(true);
        this.mPaint.setStrokeWidth(obtainStyledAttributes.getDimension(2, 0.0f));
        this.mRingOutset = obtainStyledAttributes.getDimensionPixelSize(1, 0);
        obtainStyledAttributes.recycle();
        onBoundsChange(getBounds());
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        float f;
        Rect rect = new Rect(getBounds());
        if (!canvas.getClipBounds(sTempRect) || Rect.intersects(sTempRect, rect)) {
            if (this.mIndicatorRectDirty) {
                initIndicatorRect();
            }
            if (this.mAnimationProgress >= 0.0f && this.mAnimationProgress < 1.0f) {
                this.mPaint.setAlpha((int) ((1.0f - this.mAnimationProgress) * 255.0f));
                this.mBgDrawable.setAlpha(this.mPaint.getAlpha());
                this.mBgDrawable.draw(canvas);
                canvas.drawOval(this.mIndicatorRect, this.mPaint);
                f = 0.5f + (this.mAnimationProgress * 0.5f);
            } else if (this.mAnimationProgress == -1.0f) {
                this.mPaint.setAlpha(255);
                this.mBgDrawable.setAlpha(255);
                this.mBgDrawable.draw(canvas);
                if (this.mProgress >= 100) {
                    canvas.drawOval(this.mIndicatorRect, this.mPaint);
                    f = 0.5f;
                } else {
                    f = 0.5f;
                    if (this.mProgress > 0) {
                        canvas.drawArc(this.mIndicatorRect, -90.0f, 3.6f * this.mProgress, false, this.mPaint);
                        f = 0.5f;
                    }
                }
            } else {
                f = 1.0f;
            }
            canvas.save();
            canvas.scale(f, f, rect.exactCenterX(), rect.exactCenterY());
            this.mIcon.draw(canvas);
            canvas.restore();
        }
    }

    public float getAnimationProgress() {
        return this.mAnimationProgress;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mIcon.getIntrinsicHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mIcon.getIntrinsicWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    public int getOutset() {
        return this.mRingOutset;
    }

    public boolean hasNotCompleted() {
        return this.mAnimationProgress < 1.0f;
    }

    public void maybePerformFinishedAnimation() {
        if (this.mAnimationProgress > -1.0f) {
            return;
        }
        if (this.mAnimator != null) {
            this.mAnimator.cancel();
        }
        setAnimationProgress(0.0f);
        this.mAnimator = ObjectAnimator.ofFloat(this, "animationProgress", 0.0f, 1.0f);
        this.mAnimator.start();
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        this.mIcon.setBounds(rect);
        if (this.mBgDrawable != null) {
            sTempRect.set(rect);
            sTempRect.inset(-this.mRingOutset, -this.mRingOutset);
            this.mBgDrawable.setBounds(sTempRect);
        }
        this.mIndicatorRectDirty = true;
    }

    @Override // android.graphics.drawable.Drawable
    protected boolean onLevelChange(int i) {
        this.mProgress = i;
        if (this.mAnimator != null) {
            this.mAnimator.cancel();
            this.mAnimator = null;
        }
        this.mAnimationProgress = -1.0f;
        if (i > 0) {
            this.mPaint.setColor(getIndicatorColor());
        }
        if (this.mIcon instanceof FastBitmapDrawable) {
            ((FastBitmapDrawable) this.mIcon).setState(i <= 0 ? FastBitmapDrawable.State.DISABLED : FastBitmapDrawable.State.NORMAL);
        }
        invalidateSelf();
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mIcon.setAlpha(i);
    }

    public void setAnimationProgress(float f) {
        if (f != this.mAnimationProgress) {
            this.mAnimationProgress = f;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mIcon.setColorFilter(colorFilter);
    }
}
