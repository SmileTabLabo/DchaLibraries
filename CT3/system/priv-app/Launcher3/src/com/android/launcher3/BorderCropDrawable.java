package com.android.launcher3;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
/* loaded from: a.zip:com/android/launcher3/BorderCropDrawable.class */
public class BorderCropDrawable extends Drawable {
    private final Drawable mChild;
    private final Rect mBoundsShift = new Rect();
    private final Rect mPadding = new Rect();

    /* JADX INFO: Access modifiers changed from: package-private */
    public BorderCropDrawable(Drawable drawable, boolean z, boolean z2, boolean z3, boolean z4) {
        this.mChild = drawable;
        this.mChild.getPadding(this.mPadding);
        if (z) {
            this.mBoundsShift.left = -this.mPadding.left;
            this.mPadding.left = 0;
        }
        if (z2) {
            this.mBoundsShift.top = -this.mPadding.top;
            this.mPadding.top = 0;
        }
        if (z3) {
            this.mBoundsShift.right = this.mPadding.right;
            this.mPadding.right = 0;
        }
        if (z4) {
            this.mBoundsShift.bottom = this.mPadding.bottom;
            this.mPadding.bottom = 0;
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        this.mChild.draw(canvas);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return this.mChild.getOpacity();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect rect) {
        boolean z = false;
        rect.set(this.mPadding);
        if ((rect.left | rect.top | rect.right | rect.bottom) != 0) {
            z = true;
        }
        return z;
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        this.mChild.setBounds(rect.left + this.mBoundsShift.left, rect.top + this.mBoundsShift.top, rect.right + this.mBoundsShift.right, rect.bottom + this.mBoundsShift.bottom);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mChild.setAlpha(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mChild.setColorFilter(colorFilter);
    }
}
