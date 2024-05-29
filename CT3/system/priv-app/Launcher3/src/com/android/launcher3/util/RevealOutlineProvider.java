package com.android.launcher3.util;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/util/RevealOutlineProvider.class */
public class RevealOutlineProvider extends ViewOutlineProvider {
    private int mCenterX;
    private int mCenterY;
    private int mCurrentRadius;
    private final Rect mOval = new Rect();
    private float mRadius0;
    private float mRadius1;

    public RevealOutlineProvider(int i, int i2, float f, float f2) {
        this.mCenterX = i;
        this.mCenterY = i2;
        this.mRadius0 = f;
        this.mRadius1 = f2;
    }

    @Override // android.view.ViewOutlineProvider
    public void getOutline(View view, Outline outline) {
        outline.setRoundRect(this.mOval, this.mCurrentRadius);
    }

    public void setProgress(float f) {
        this.mCurrentRadius = (int) (((1.0f - f) * this.mRadius0) + (this.mRadius1 * f));
        this.mOval.left = this.mCenterX - this.mCurrentRadius;
        this.mOval.top = this.mCenterY - this.mCurrentRadius;
        this.mOval.right = this.mCenterX + this.mCurrentRadius;
        this.mOval.bottom = this.mCenterY + this.mCurrentRadius;
    }
}
