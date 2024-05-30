package com.android.systemui.qs;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.FloatProperty;
/* loaded from: classes.dex */
public class SlashDrawable extends Drawable {
    private float mCurrentSlashLength;
    private Drawable mDrawable;
    private float mRotation;
    private boolean mSlashed;
    private ColorStateList mTintList;
    private PorterDuff.Mode mTintMode;
    private final Path mPath = new Path();
    private final Paint mPaint = new Paint(1);
    private final RectF mSlashRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private boolean mAnimationEnabled = true;
    private final FloatProperty mSlashLengthProp = new FloatProperty<SlashDrawable>("slashLength") { // from class: com.android.systemui.qs.SlashDrawable.1
        @Override // android.util.FloatProperty
        public void setValue(SlashDrawable slashDrawable, float f) {
            slashDrawable.mCurrentSlashLength = f;
        }

        @Override // android.util.Property
        public Float get(SlashDrawable slashDrawable) {
            return Float.valueOf(slashDrawable.mCurrentSlashLength);
        }
    };

    public SlashDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        if (this.mDrawable != null) {
            return this.mDrawable.getIntrinsicHeight();
        }
        return 0;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        if (this.mDrawable != null) {
            return this.mDrawable.getIntrinsicWidth();
        }
        return 0;
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        this.mDrawable.setBounds(rect);
    }

    public void setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
        this.mDrawable.setCallback(getCallback());
        this.mDrawable.setBounds(getBounds());
        if (this.mTintMode != null) {
            this.mDrawable.setTintMode(this.mTintMode);
        }
        if (this.mTintList != null) {
            this.mDrawable.setTintList(this.mTintList);
        }
        invalidateSelf();
    }

    public void setRotation(float f) {
        if (this.mRotation == f) {
            return;
        }
        this.mRotation = f;
        invalidateSelf();
    }

    public void setAnimationEnabled(boolean z) {
        this.mAnimationEnabled = z;
    }

    public void setSlashed(boolean z) {
        if (this.mSlashed == z) {
            return;
        }
        this.mSlashed = z;
        float f = this.mSlashed ? 1.1666666f : 0.0f;
        float f2 = this.mSlashed ? 0.0f : 1.1666666f;
        if (this.mAnimationEnabled) {
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, this.mSlashLengthProp, f2, f);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.qs.-$$Lambda$SlashDrawable$d6ImpYshN38WeANK1PRMKepeaRo
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SlashDrawable.this.invalidateSelf();
                }
            });
            ofFloat.setDuration(350L);
            ofFloat.start();
            return;
        }
        this.mCurrentSlashLength = f;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        canvas.save();
        Matrix matrix = new Matrix();
        int width = getBounds().width();
        int height = getBounds().height();
        float scale = scale(1.0f, width);
        float scale2 = scale(1.0f, height);
        updateRect(scale(0.40544835f, width), scale(-0.088781714f, height), scale(0.4820516f, width), scale((-0.088781714f) + this.mCurrentSlashLength, height));
        this.mPath.reset();
        this.mPath.addRoundRect(this.mSlashRect, scale, scale2, Path.Direction.CW);
        float f = width / 2;
        float f2 = height / 2;
        matrix.setRotate(this.mRotation - 45.0f, f, f2);
        this.mPath.transform(matrix);
        canvas.drawPath(this.mPath, this.mPaint);
        matrix.setRotate((-this.mRotation) - (-45.0f), f, f2);
        this.mPath.transform(matrix);
        matrix.setTranslate(this.mSlashRect.width(), 0.0f);
        this.mPath.transform(matrix);
        this.mPath.addRoundRect(this.mSlashRect, width * 1.0f, 1.0f * height, Path.Direction.CW);
        matrix.setRotate(this.mRotation - 45.0f, f, f2);
        this.mPath.transform(matrix);
        canvas.clipOutPath(this.mPath);
        this.mDrawable.draw(canvas);
        canvas.restore();
    }

    private float scale(float f, int i) {
        return f * i;
    }

    private void updateRect(float f, float f2, float f3, float f4) {
        this.mSlashRect.left = f;
        this.mSlashRect.top = f2;
        this.mSlashRect.right = f3;
        this.mSlashRect.bottom = f4;
    }

    @Override // android.graphics.drawable.Drawable
    public void setTint(int i) {
        super.setTint(i);
        this.mDrawable.setTint(i);
        this.mPaint.setColor(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList colorStateList) {
        this.mTintList = colorStateList;
        super.setTintList(colorStateList);
        setDrawableTintList(colorStateList);
        this.mPaint.setColor(colorStateList.getDefaultColor());
        invalidateSelf();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setDrawableTintList(ColorStateList colorStateList) {
        this.mDrawable.setTintList(colorStateList);
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintMode(PorterDuff.Mode mode) {
        this.mTintMode = mode;
        super.setTintMode(mode);
        this.mDrawable.setTintMode(mode);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mDrawable.setAlpha(i);
        this.mPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mDrawable.setColorFilter(colorFilter);
        this.mPaint.setColorFilter(colorFilter);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return 255;
    }
}
