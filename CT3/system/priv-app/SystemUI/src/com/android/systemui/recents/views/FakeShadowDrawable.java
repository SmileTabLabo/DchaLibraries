package com.android.systemui.recents.views;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.android.systemui.recents.RecentsConfiguration;
/* loaded from: a.zip:com/android/systemui/recents/views/FakeShadowDrawable.class */
class FakeShadowDrawable extends Drawable {
    static final double COS_45 = Math.cos(Math.toRadians(45.0d));
    final RectF mCardBounds;
    float mCornerRadius;
    Paint mCornerShadowPaint;
    Path mCornerShadowPath;
    Paint mEdgeShadowPaint;
    final float mInsetShadow;
    float mMaxShadowSize;
    float mRawMaxShadowSize;
    float mRawShadowSize;
    private final int mShadowEndColor;
    float mShadowSize;
    private final int mShadowStartColor;
    private boolean mDirty = true;
    private boolean mAddPaddingForCorners = true;
    private boolean mPrintedShadowClipWarning = false;

    public FakeShadowDrawable(Resources resources, RecentsConfiguration recentsConfiguration) {
        this.mShadowStartColor = resources.getColor(2131558567);
        this.mShadowEndColor = resources.getColor(2131558568);
        this.mInsetShadow = resources.getDimension(2131689951);
        setShadowSize(resources.getDimensionPixelSize(2131689952), resources.getDimensionPixelSize(2131689952));
        this.mCornerShadowPaint = new Paint(5);
        this.mCornerShadowPaint.setStyle(Paint.Style.FILL);
        this.mCornerShadowPaint.setDither(true);
        this.mCornerRadius = resources.getDimensionPixelSize(2131690016);
        this.mCardBounds = new RectF();
        this.mEdgeShadowPaint = new Paint(this.mCornerShadowPaint);
    }

    private void buildComponents(Rect rect) {
        float f = this.mMaxShadowSize * 1.5f;
        this.mCardBounds.set(rect.left + this.mMaxShadowSize, rect.top + f, rect.right - this.mMaxShadowSize, rect.bottom - f);
        buildShadowCorners();
    }

    private void buildShadowCorners() {
        RectF rectF = new RectF(-this.mCornerRadius, -this.mCornerRadius, this.mCornerRadius, this.mCornerRadius);
        RectF rectF2 = new RectF(rectF);
        rectF2.inset(-this.mShadowSize, -this.mShadowSize);
        if (this.mCornerShadowPath == null) {
            this.mCornerShadowPath = new Path();
        } else {
            this.mCornerShadowPath.reset();
        }
        this.mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
        this.mCornerShadowPath.moveTo(-this.mCornerRadius, 0.0f);
        this.mCornerShadowPath.rLineTo(-this.mShadowSize, 0.0f);
        this.mCornerShadowPath.arcTo(rectF2, 180.0f, 90.0f, false);
        this.mCornerShadowPath.arcTo(rectF, 270.0f, -90.0f, false);
        this.mCornerShadowPath.close();
        float f = this.mCornerRadius / (this.mCornerRadius + this.mShadowSize);
        Paint paint = this.mCornerShadowPaint;
        float f2 = this.mCornerRadius;
        float f3 = this.mShadowSize;
        paint.setShader(new RadialGradient(0.0f, 0.0f, f2 + f3, new int[]{this.mShadowStartColor, this.mShadowStartColor, this.mShadowEndColor}, new float[]{0.0f, f, 1.0f}, Shader.TileMode.CLAMP));
        Paint paint2 = this.mEdgeShadowPaint;
        float f4 = -this.mCornerRadius;
        float f5 = this.mShadowSize;
        float f6 = -this.mCornerRadius;
        float f7 = this.mShadowSize;
        paint2.setShader(new LinearGradient(0.0f, f4 + f5, 0.0f, f6 - f7, new int[]{this.mShadowStartColor, this.mShadowStartColor, this.mShadowEndColor}, new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP));
    }

    static float calculateHorizontalPadding(float f, float f2, boolean z) {
        return z ? (float) (f + ((1.0d - COS_45) * f2)) : f;
    }

    static float calculateVerticalPadding(float f, float f2, boolean z) {
        return z ? (float) ((1.5f * f) + ((1.0d - COS_45) * f2)) : 1.5f * f;
    }

    private void drawShadow(Canvas canvas) {
        float f = (-this.mCornerRadius) - this.mShadowSize;
        float f2 = this.mCornerRadius + this.mInsetShadow + (this.mRawShadowSize / 2.0f);
        boolean z = this.mCardBounds.width() - (2.0f * f2) > 0.0f;
        boolean z2 = this.mCardBounds.height() - (2.0f * f2) > 0.0f;
        int save = canvas.save();
        canvas.translate(this.mCardBounds.left + f2, this.mCardBounds.top + f2);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (z) {
            canvas.drawRect(0.0f, f, this.mCardBounds.width() - (2.0f * f2), -this.mCornerRadius, this.mEdgeShadowPaint);
        }
        canvas.restoreToCount(save);
        int save2 = canvas.save();
        canvas.translate(this.mCardBounds.right - f2, this.mCardBounds.bottom - f2);
        canvas.rotate(180.0f);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (z) {
            canvas.drawRect(0.0f, f, this.mCardBounds.width() - (2.0f * f2), this.mShadowSize + (-this.mCornerRadius), this.mEdgeShadowPaint);
        }
        canvas.restoreToCount(save2);
        int save3 = canvas.save();
        canvas.translate(this.mCardBounds.left + f2, this.mCardBounds.bottom - f2);
        canvas.rotate(270.0f);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (z2) {
            canvas.drawRect(0.0f, f, this.mCardBounds.height() - (2.0f * f2), -this.mCornerRadius, this.mEdgeShadowPaint);
        }
        canvas.restoreToCount(save3);
        int save4 = canvas.save();
        canvas.translate(this.mCardBounds.right - f2, this.mCardBounds.top + f2);
        canvas.rotate(90.0f);
        canvas.drawPath(this.mCornerShadowPath, this.mCornerShadowPaint);
        if (z2) {
            canvas.drawRect(0.0f, f, this.mCardBounds.height() - (2.0f * f2), -this.mCornerRadius, this.mEdgeShadowPaint);
        }
        canvas.restoreToCount(save4);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.mDirty) {
            buildComponents(getBounds());
            this.mDirty = false;
        }
        canvas.translate(0.0f, this.mRawShadowSize / 4.0f);
        drawShadow(canvas);
        canvas.translate(0.0f, (-this.mRawShadowSize) / 4.0f);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -1;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect rect) {
        int ceil = (int) Math.ceil(calculateVerticalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners));
        int ceil2 = (int) Math.ceil(calculateHorizontalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners));
        rect.set(ceil2, ceil, ceil2, ceil);
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        this.mDirty = true;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mCornerShadowPaint.setAlpha(i);
        this.mEdgeShadowPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mCornerShadowPaint.setColorFilter(colorFilter);
        this.mEdgeShadowPaint.setColorFilter(colorFilter);
    }

    void setShadowSize(float f, float f2) {
        if (f < 0.0f || f2 < 0.0f) {
            throw new IllegalArgumentException("invalid shadow size");
        }
        float f3 = f;
        if (f > f2) {
            f3 = f2;
            if (!this.mPrintedShadowClipWarning) {
                Log.w("CardView", "Shadow size is being clipped by the max shadow size. See {CardView#setMaxCardElevation}.");
                this.mPrintedShadowClipWarning = true;
                f3 = f2;
            }
        }
        if (this.mRawShadowSize == f3 && this.mRawMaxShadowSize == f2) {
            return;
        }
        this.mRawShadowSize = f3;
        this.mRawMaxShadowSize = f2;
        this.mShadowSize = (1.5f * f3) + this.mInsetShadow;
        this.mMaxShadowSize = this.mInsetShadow + f2;
        this.mDirty = true;
        invalidateSelf();
    }
}
