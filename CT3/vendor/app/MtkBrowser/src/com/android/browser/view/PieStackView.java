package com.android.browser.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
/* loaded from: b.zip:com/android/browser/view/PieStackView.class */
public class PieStackView extends BasePieView {
    private OnCurrentListener mCurrentListener;
    private int mMinHeight;

    /* loaded from: b.zip:com/android/browser/view/PieStackView$OnCurrentListener.class */
    public interface OnCurrentListener {
        void onSetCurrent(int i);
    }

    public PieStackView(Context context) {
        this.mMinHeight = (int) context.getResources().getDimension(2131427347);
    }

    private void layoutChildrenLinear() {
        int size = this.mViews.size();
        int i = this.mTop;
        int i2 = size == 1 ? 0 : (this.mHeight - this.mChildHeight) / (size - 1);
        for (View view : this.mViews) {
            int i3 = this.mLeft;
            view.layout(i3, i, this.mChildWidth + i3, this.mChildHeight + i);
            i += i2;
        }
    }

    @Override // com.android.browser.view.BasePieView, com.android.browser.view.PieMenu.PieView
    public void draw(Canvas canvas) {
        if (this.mViews == null || this.mCurrent <= -1) {
            return;
        }
        int size = this.mViews.size();
        for (int i = 0; i < this.mCurrent; i++) {
            drawView(this.mViews.get(i), canvas);
        }
        for (int i2 = size - 1; i2 > this.mCurrent; i2--) {
            drawView(this.mViews.get(i2), canvas);
        }
        drawView(this.mViews.get(this.mCurrent), canvas);
    }

    @Override // com.android.browser.view.BasePieView
    protected int findChildAt(int i) {
        return ((i - this.mTop) * this.mViews.size()) / this.mHeight;
    }

    @Override // com.android.browser.view.BasePieView, com.android.browser.view.PieMenu.PieView
    public void layout(int i, int i2, boolean z, float f, int i3) {
        super.layout(i, i2, z, f, i3);
        buildViews();
        this.mWidth = this.mChildWidth;
        this.mHeight = this.mChildHeight + ((this.mViews.size() - 1) * this.mMinHeight);
        this.mLeft = (z ? 5 : -(this.mChildWidth + 5)) + i;
        this.mTop = i2 - (this.mHeight / 2);
        if (this.mViews != null) {
            layoutChildrenLinear();
        }
    }

    @Override // com.android.browser.view.BasePieView
    public void setCurrent(int i) {
        super.setCurrent(i);
        if (this.mCurrentListener != null) {
            this.mCurrentListener.onSetCurrent(i);
        }
    }

    public void setOnCurrentListener(OnCurrentListener onCurrentListener) {
        this.mCurrentListener = onCurrentListener;
    }
}
