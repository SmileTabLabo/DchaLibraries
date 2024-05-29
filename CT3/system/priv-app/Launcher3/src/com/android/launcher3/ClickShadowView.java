package com.android.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: a.zip:com/android/launcher3/ClickShadowView.class */
public class ClickShadowView extends View {
    private Bitmap mBitmap;
    private final Paint mPaint;
    private final float mShadowOffset;
    private final float mShadowPadding;

    public ClickShadowView(Context context) {
        super(context);
        this.mPaint = new Paint(2);
        this.mPaint.setColor(-16777216);
        this.mShadowPadding = getResources().getDimension(2131230808);
        this.mShadowOffset = getResources().getDimension(2131230809);
    }

    public void alignWithIconView(BubbleTextView bubbleTextView, ViewGroup viewGroup) {
        float left = (bubbleTextView.getLeft() + viewGroup.getLeft()) - getLeft();
        float top = (bubbleTextView.getTop() + viewGroup.getTop()) - getTop();
        int right = bubbleTextView.getRight() - bubbleTextView.getLeft();
        int compoundPaddingRight = bubbleTextView.getCompoundPaddingRight();
        int compoundPaddingLeft = bubbleTextView.getCompoundPaddingLeft();
        setTranslationX(((((viewGroup.getTranslationX() + left) + (bubbleTextView.getCompoundPaddingLeft() * bubbleTextView.getScaleX())) + (((((right - compoundPaddingRight) - compoundPaddingLeft) - bubbleTextView.getIcon().getBounds().width()) * bubbleTextView.getScaleX()) / 2.0f)) + ((right * (1.0f - bubbleTextView.getScaleX())) / 2.0f)) - this.mShadowPadding);
        setTranslationY((((viewGroup.getTranslationY() + top) + (bubbleTextView.getPaddingTop() * bubbleTextView.getScaleY())) + ((bubbleTextView.getHeight() * (1.0f - bubbleTextView.getScaleY())) / 2.0f)) - this.mShadowPadding);
    }

    public void animateShadow() {
        setAlpha(0.0f);
        animate().alpha(1.0f).setDuration(2000L).setInterpolator(FastBitmapDrawable.CLICK_FEEDBACK_INTERPOLATOR).start();
    }

    public int getExtraSize() {
        return (int) (this.mShadowPadding * 3.0f);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mBitmap != null) {
            this.mPaint.setAlpha(30);
            canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, this.mPaint);
            this.mPaint.setAlpha(60);
            canvas.drawBitmap(this.mBitmap, 0.0f, this.mShadowOffset, this.mPaint);
        }
    }

    public boolean setBitmap(Bitmap bitmap) {
        if (bitmap != this.mBitmap) {
            this.mBitmap = bitmap;
            invalidate();
            return true;
        }
        return false;
    }
}
