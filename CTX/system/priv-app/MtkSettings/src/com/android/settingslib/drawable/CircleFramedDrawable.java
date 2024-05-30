package com.android.settingslib.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.android.settingslib.R;
/* loaded from: classes.dex */
public class CircleFramedDrawable extends Drawable {
    private final Bitmap mBitmap;
    private RectF mDstRect;
    private final Paint mPaint;
    private float mScale;
    private final int mSize;
    private Rect mSrcRect;

    public static CircleFramedDrawable getInstance(Context context, Bitmap bitmap) {
        return new CircleFramedDrawable(bitmap, (int) context.getResources().getDimension(R.dimen.circle_avatar_size));
    }

    public CircleFramedDrawable(Bitmap bitmap, int i) {
        this.mSize = i;
        this.mBitmap = Bitmap.createBitmap(this.mSize, this.mSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.mBitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int min = Math.min(width, height);
        Rect rect = new Rect((width - min) / 2, (height - min) / 2, min, min);
        RectF rectF = new RectF(0.0f, 0.0f, this.mSize, this.mSize);
        Path path = new Path();
        path.addArc(rectF, 0.0f, 360.0f);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(-16777216);
        this.mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, this.mPaint);
        this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rectF, this.mPaint);
        this.mPaint.setXfermode(null);
        this.mScale = 1.0f;
        this.mSrcRect = new Rect(0, 0, this.mSize, this.mSize);
        this.mDstRect = new RectF(0.0f, 0.0f, this.mSize, this.mSize);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        float f = (this.mSize - (this.mScale * this.mSize)) / 2.0f;
        this.mDstRect.set(f, f, this.mSize - f, this.mSize - f);
        canvas.drawBitmap(this.mBitmap, this.mSrcRect, this.mDstRect, (Paint) null);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mSize;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mSize;
    }
}
