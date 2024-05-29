package com.android.launcher3.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
/* loaded from: classes.dex */
public class FastScrollThumbDrawable extends Drawable {
    private static final Matrix sMatrix = new Matrix();
    private final boolean mIsRtl;
    private final Paint mPaint;
    private final Path mPath = new Path();

    public FastScrollThumbDrawable(Paint paint, boolean z) {
        this.mPaint = paint;
        this.mIsRtl = z;
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        if (this.mPath.isConvex()) {
            outline.setConvexPath(this.mPath);
        }
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        this.mPath.reset();
        float height = rect.height() * 0.5f;
        float f = 2.0f * height;
        float f2 = height / 5.0f;
        this.mPath.addRoundRect(rect.left, rect.top, rect.left + f, rect.top + f, new float[]{height, height, height, height, f2, f2, height, height}, Path.Direction.CCW);
        sMatrix.setRotate(-45.0f, rect.left + height, rect.top + height);
        if (this.mIsRtl) {
            sMatrix.postTranslate(rect.width(), 0.0f);
            sMatrix.postScale(-1.0f, 1.0f, rect.width(), 0.0f);
        }
        this.mPath.transform(sMatrix);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        canvas.drawPath(this.mPath, this.mPaint);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }
}
