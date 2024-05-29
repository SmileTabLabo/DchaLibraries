package com.android.launcher3.allapps;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
/* loaded from: a.zip:com/android/launcher3/allapps/TransformedImageDrawable.class */
class TransformedImageDrawable {
    private int mAlpha;
    private int mGravity;
    private Drawable mImage;
    private float mXPercent;
    private float mYPercent;

    public TransformedImageDrawable(Resources resources, int i, float f, float f2, int i2) {
        this.mImage = resources.getDrawable(i);
        this.mXPercent = f;
        this.mYPercent = f2;
        this.mGravity = i2;
    }

    public void draw(Canvas canvas) {
        int save = canvas.save(1);
        this.mImage.draw(canvas);
        canvas.restoreToCount(save);
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void setAlpha(int i) {
        this.mImage.setAlpha(i);
        this.mAlpha = i;
    }

    public void updateBounds(Rect rect) {
        int intrinsicWidth = this.mImage.getIntrinsicWidth();
        int intrinsicHeight = this.mImage.getIntrinsicHeight();
        int width = rect.left + ((int) (this.mXPercent * rect.width()));
        int height = rect.top + ((int) (this.mYPercent * rect.height()));
        int i = width;
        if ((this.mGravity & 1) == 1) {
            i = width - (intrinsicWidth / 2);
        }
        int i2 = height;
        if ((this.mGravity & 16) == 16) {
            i2 = height - (intrinsicHeight / 2);
        }
        this.mImage.setBounds(i, i2, i + intrinsicWidth, i2 + intrinsicHeight);
    }
}
