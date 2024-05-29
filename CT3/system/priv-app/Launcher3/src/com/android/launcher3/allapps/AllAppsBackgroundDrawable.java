package com.android.launcher3.allapps;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
/* loaded from: a.zip:com/android/launcher3/allapps/AllAppsBackgroundDrawable.class */
public class AllAppsBackgroundDrawable extends Drawable {
    private ObjectAnimator mBackgroundAnim;
    private final TransformedImageDrawable mHand;
    private final int mHeight;
    private final TransformedImageDrawable[] mIcons;
    private final int mWidth;

    public AllAppsBackgroundDrawable(Context context) {
        Resources resources = context.getResources();
        this.mHand = new TransformedImageDrawable(resources, 2130837514, 0.575f, 0.1f, 1);
        this.mIcons = new TransformedImageDrawable[4];
        this.mIcons[0] = new TransformedImageDrawable(resources, 2130837515, 0.375f, 0.0f, 1);
        this.mIcons[1] = new TransformedImageDrawable(resources, 2130837516, 0.3125f, 0.25f, 1);
        this.mIcons[2] = new TransformedImageDrawable(resources, 2130837517, 0.475f, 0.4f, 1);
        this.mIcons[3] = new TransformedImageDrawable(resources, 2130837518, 0.7f, 0.125f, 1);
        this.mWidth = resources.getDimensionPixelSize(2131230776);
        this.mHeight = resources.getDimensionPixelSize(2131230777);
    }

    private ObjectAnimator cancelAnimator(ObjectAnimator objectAnimator) {
        if (objectAnimator != null) {
            objectAnimator.removeAllListeners();
            objectAnimator.cancel();
            return null;
        }
        return null;
    }

    public void animateBgAlpha(float f, int i) {
        int i2 = (int) (255.0f * f);
        if (getAlpha() != i2) {
            this.mBackgroundAnim = cancelAnimator(this.mBackgroundAnim);
            this.mBackgroundAnim = ObjectAnimator.ofInt(this, "alpha", i2);
            this.mBackgroundAnim.setDuration(i);
            this.mBackgroundAnim.start();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        this.mHand.draw(canvas);
        for (int i = 0; i < this.mIcons.length; i++) {
            this.mIcons[i].draw(canvas);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mHand.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        this.mHand.updateBounds(rect);
        for (int i = 0; i < this.mIcons.length; i++) {
            this.mIcons[i].updateBounds(rect);
        }
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mHand.setAlpha(i);
        for (int i2 = 0; i2 < this.mIcons.length; i2++) {
            this.mIcons[i2].setAlpha(i);
        }
        invalidateSelf();
    }

    public void setBgAlpha(float f) {
        int i = (int) (255.0f * f);
        if (getAlpha() != i) {
            this.mBackgroundAnim = cancelAnimator(this.mBackgroundAnim);
            setAlpha(i);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }
}
