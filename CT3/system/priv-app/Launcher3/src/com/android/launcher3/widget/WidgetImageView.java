package com.android.launcher3.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
/* loaded from: a.zip:com/android/launcher3/widget/WidgetImageView.class */
public class WidgetImageView extends View {
    private Bitmap mBitmap;
    private final RectF mDstRectF;
    private final Paint mPaint;

    public WidgetImageView(Context context) {
        super(context);
        this.mPaint = new Paint(3);
        this.mDstRectF = new RectF();
    }

    public WidgetImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint = new Paint(3);
        this.mDstRectF = new RectF();
    }

    public WidgetImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mPaint = new Paint(3);
        this.mDstRectF = new RectF();
    }

    private void updateDstRectF() {
        if (this.mBitmap.getWidth() <= getWidth()) {
            this.mDstRectF.set((getWidth() - this.mBitmap.getWidth()) * 0.5f, 0.0f, (getWidth() + this.mBitmap.getWidth()) * 0.5f, this.mBitmap.getHeight());
            return;
        }
        this.mDstRectF.set(0.0f, 0.0f, getWidth(), this.mBitmap.getHeight() * (getWidth() / this.mBitmap.getWidth()));
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public Rect getBitmapBounds() {
        updateDstRectF();
        Rect rect = new Rect();
        this.mDstRectF.round(rect);
        return rect;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mBitmap != null) {
            updateDstRectF();
            canvas.drawBitmap(this.mBitmap, (Rect) null, this.mDstRectF, this.mPaint);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        invalidate();
    }
}
