package android.support.v4.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
/* loaded from: a.zip:android/support/v4/view/PagerTabStrip.class */
public class PagerTabStrip extends PagerTitleStrip {
    private boolean mDrawFullUnderline;
    private boolean mDrawFullUnderlineSet;
    private int mFullUnderlineHeight;
    private boolean mIgnoreTap;
    private int mIndicatorColor;
    private int mIndicatorHeight;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mMinPaddingBottom;
    private int mMinStripHeight;
    private int mMinTextSpacing;
    private int mTabAlpha;
    private int mTabPadding;
    private final Paint mTabPaint;
    private final Rect mTempRect;
    private int mTouchSlop;

    public PagerTabStrip(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTabPaint = new Paint();
        this.mTempRect = new Rect();
        this.mTabAlpha = 255;
        this.mDrawFullUnderline = false;
        this.mDrawFullUnderlineSet = false;
        this.mIndicatorColor = this.mTextColor;
        this.mTabPaint.setColor(this.mIndicatorColor);
        float f = context.getResources().getDisplayMetrics().density;
        this.mIndicatorHeight = (int) ((3.0f * f) + 0.5f);
        this.mMinPaddingBottom = (int) ((6.0f * f) + 0.5f);
        this.mMinTextSpacing = (int) (64.0f * f);
        this.mTabPadding = (int) ((16.0f * f) + 0.5f);
        this.mFullUnderlineHeight = (int) ((1.0f * f) + 0.5f);
        this.mMinStripHeight = (int) ((32.0f * f) + 0.5f);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        setTextSpacing(getTextSpacing());
        setWillNotDraw(false);
        this.mPrevText.setFocusable(true);
        this.mPrevText.setOnClickListener(new View.OnClickListener(this) { // from class: android.support.v4.view.PagerTabStrip.1
            final PagerTabStrip this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.mPager.setCurrentItem(this.this$0.mPager.getCurrentItem() - 1);
            }
        });
        this.mNextText.setFocusable(true);
        this.mNextText.setOnClickListener(new View.OnClickListener(this) { // from class: android.support.v4.view.PagerTabStrip.2
            final PagerTabStrip this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.mPager.setCurrentItem(this.this$0.mPager.getCurrentItem() + 1);
            }
        });
        if (getBackground() == null) {
            this.mDrawFullUnderline = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v4.view.PagerTitleStrip
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), this.mMinStripHeight);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int left = this.mCurrText.getLeft();
        int i = this.mTabPadding;
        int right = this.mCurrText.getRight();
        int i2 = this.mTabPadding;
        int i3 = this.mIndicatorHeight;
        this.mTabPaint.setColor((this.mTabAlpha << 24) | (this.mIndicatorColor & 16777215));
        canvas.drawRect(left - i, height - i3, right + i2, height, this.mTabPaint);
        if (this.mDrawFullUnderline) {
            this.mTabPaint.setColor((this.mIndicatorColor & 16777215) | (-16777216));
            canvas.drawRect(getPaddingLeft(), height - this.mFullUnderlineHeight, getWidth() - getPaddingRight(), height, this.mTabPaint);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0 || !this.mIgnoreTap) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            switch (action) {
                case 0:
                    this.mInitialMotionX = x;
                    this.mInitialMotionY = y;
                    this.mIgnoreTap = false;
                    return true;
                case 1:
                    if (x < this.mCurrText.getLeft() - this.mTabPadding) {
                        this.mPager.setCurrentItem(this.mPager.getCurrentItem() - 1);
                        return true;
                    } else if (x > this.mCurrText.getRight() + this.mTabPadding) {
                        this.mPager.setCurrentItem(this.mPager.getCurrentItem() + 1);
                        return true;
                    } else {
                        return true;
                    }
                case 2:
                    if (Math.abs(x - this.mInitialMotionX) > this.mTouchSlop || Math.abs(y - this.mInitialMotionY) > this.mTouchSlop) {
                        this.mIgnoreTap = true;
                        return true;
                    }
                    return true;
                default:
                    return true;
            }
        }
        return false;
    }

    @Override // android.view.View
    public void setBackgroundColor(@ColorInt int i) {
        boolean z = false;
        super.setBackgroundColor(i);
        if (this.mDrawFullUnderlineSet) {
            return;
        }
        if (((-16777216) & i) == 0) {
            z = true;
        }
        this.mDrawFullUnderline = z;
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable drawable) {
        super.setBackgroundDrawable(drawable);
        if (this.mDrawFullUnderlineSet) {
            return;
        }
        this.mDrawFullUnderline = drawable == null;
    }

    @Override // android.view.View
    public void setBackgroundResource(@DrawableRes int i) {
        boolean z = false;
        super.setBackgroundResource(i);
        if (this.mDrawFullUnderlineSet) {
            return;
        }
        if (i == 0) {
            z = true;
        }
        this.mDrawFullUnderline = z;
    }

    @Override // android.view.View
    public void setPadding(int i, int i2, int i3, int i4) {
        int i5 = i4;
        if (i4 < this.mMinPaddingBottom) {
            i5 = this.mMinPaddingBottom;
        }
        super.setPadding(i, i2, i3, i5);
    }

    @Override // android.support.v4.view.PagerTitleStrip
    public void setTextSpacing(int i) {
        int i2 = i;
        if (i < this.mMinTextSpacing) {
            i2 = this.mMinTextSpacing;
        }
        super.setTextSpacing(i2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v4.view.PagerTitleStrip
    public void updateTextPositions(int i, float f, boolean z) {
        Rect rect = this.mTempRect;
        int height = getHeight();
        int left = this.mCurrText.getLeft();
        int i2 = this.mTabPadding;
        int right = this.mCurrText.getRight();
        int i3 = this.mTabPadding;
        int i4 = height - this.mIndicatorHeight;
        rect.set(left - i2, i4, right + i3, height);
        super.updateTextPositions(i, f, z);
        this.mTabAlpha = (int) (Math.abs(f - 0.5f) * 2.0f * 255.0f);
        rect.union(this.mCurrText.getLeft() - this.mTabPadding, i4, this.mCurrText.getRight() + this.mTabPadding, height);
        invalidate(rect);
    }
}
