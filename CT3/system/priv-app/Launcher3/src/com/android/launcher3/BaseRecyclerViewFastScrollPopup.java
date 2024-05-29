package com.android.launcher3;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
/* loaded from: a.zip:com/android/launcher3/BaseRecyclerViewFastScrollPopup.class */
public class BaseRecyclerViewFastScrollPopup {
    private float mAlpha;
    private Animator mAlphaAnimator;
    private Drawable mBg;
    private int mBgOriginalSize;
    private Resources mRes;
    private BaseRecyclerView mRv;
    private String mSectionName;
    private Paint mTextPaint;
    private boolean mVisible;
    private Rect mBgBounds = new Rect();
    private Rect mInvalidateRect = new Rect();
    private Rect mTmpRect = new Rect();
    private Rect mTextBounds = new Rect();

    public BaseRecyclerViewFastScrollPopup(BaseRecyclerView baseRecyclerView, Resources resources) {
        this.mRes = resources;
        this.mRv = baseRecyclerView;
        this.mBgOriginalSize = resources.getDimensionPixelSize(2131230761);
        this.mBg = resources.getDrawable(2130837510);
        this.mBg.setBounds(0, 0, this.mBgOriginalSize, this.mBgOriginalSize);
        this.mTextPaint = new Paint();
        this.mTextPaint.setColor(-1);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(resources.getDimensionPixelSize(2131230762));
    }

    public void animateVisibility(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            if (this.mAlphaAnimator != null) {
                this.mAlphaAnimator.cancel();
            }
            this.mAlphaAnimator = ObjectAnimator.ofFloat(this, "alpha", z ? 1.0f : 0.0f);
            this.mAlphaAnimator.setDuration(z ? 200 : 150);
            this.mAlphaAnimator.start();
        }
    }

    public void draw(Canvas canvas) {
        if (isVisible()) {
            int save = canvas.save(1);
            canvas.translate(this.mBgBounds.left, this.mBgBounds.top);
            this.mTmpRect.set(this.mBgBounds);
            this.mTmpRect.offsetTo(0, 0);
            this.mBg.setBounds(this.mTmpRect);
            this.mBg.setAlpha((int) (this.mAlpha * 255.0f));
            this.mBg.draw(canvas);
            this.mTextPaint.setAlpha((int) (this.mAlpha * 255.0f));
            canvas.drawText(this.mSectionName, (this.mBgBounds.width() - this.mTextBounds.width()) / 2, this.mBgBounds.height() - ((this.mBgBounds.height() - this.mTextBounds.height()) / 2), this.mTextPaint);
            canvas.restoreToCount(save);
        }
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public boolean isVisible() {
        boolean z = false;
        if (this.mAlpha > 0.0f) {
            z = false;
            if (this.mSectionName != null) {
                z = true;
            }
        }
        return z;
    }

    public void setAlpha(float f) {
        this.mAlpha = f;
        this.mRv.invalidate(this.mBgBounds);
    }

    public void setSectionName(String str) {
        if (str.equals(this.mSectionName)) {
            return;
        }
        this.mSectionName = str;
        this.mTextPaint.getTextBounds(str, 0, str.length(), this.mTextBounds);
        this.mTextBounds.right = (int) (this.mTextBounds.left + this.mTextPaint.measureText(str));
    }

    public Rect updateFastScrollerBounds(int i) {
        this.mInvalidateRect.set(this.mBgBounds);
        if (isVisible()) {
            int maxScrollbarWidth = this.mRv.getMaxScrollbarWidth();
            int height = (this.mBgOriginalSize - this.mTextBounds.height()) / 2;
            int i2 = this.mBgOriginalSize;
            int max = Math.max(this.mBgOriginalSize, this.mTextBounds.width() + (height * 2));
            if (Utilities.isRtl(this.mRes)) {
                this.mBgBounds.left = this.mRv.getBackgroundPadding().left + (this.mRv.getMaxScrollbarWidth() * 2);
                this.mBgBounds.right = this.mBgBounds.left + max;
            } else {
                this.mBgBounds.right = (this.mRv.getWidth() - this.mRv.getBackgroundPadding().right) - (this.mRv.getMaxScrollbarWidth() * 2);
                this.mBgBounds.left = this.mBgBounds.right - max;
            }
            this.mBgBounds.top = i - ((int) (i2 * 1.5f));
            this.mBgBounds.top = Math.max(maxScrollbarWidth, Math.min(this.mBgBounds.top, (this.mRv.getHeight() - maxScrollbarWidth) - i2));
            this.mBgBounds.bottom = this.mBgBounds.top + i2;
        } else {
            this.mBgBounds.setEmpty();
        }
        this.mInvalidateRect.union(this.mBgBounds);
        return this.mInvalidateRect;
    }
}
