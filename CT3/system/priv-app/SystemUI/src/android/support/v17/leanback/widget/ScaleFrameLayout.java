package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
/* loaded from: a.zip:android/support/v17/leanback/widget/ScaleFrameLayout.class */
public class ScaleFrameLayout extends FrameLayout {
    private float mChildScale;
    private float mLayoutScaleX;
    private float mLayoutScaleY;

    public ScaleFrameLayout(Context context) {
        this(context, null);
    }

    public ScaleFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScaleFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLayoutScaleX = 1.0f;
        this.mLayoutScaleY = 1.0f;
        this.mChildScale = 1.0f;
    }

    private static int getScaledMeasureSpec(int i, float f) {
        if (f != 1.0f) {
            i = View.MeasureSpec.makeMeasureSpec((int) ((View.MeasureSpec.getSize(i) / f) + 0.5f), View.MeasureSpec.getMode(i));
        }
        return i;
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
        view.setScaleX(this.mChildScale);
        view.setScaleY(this.mChildScale);
    }

    @Override // android.view.ViewGroup
    protected boolean addViewInLayout(View view, int i, ViewGroup.LayoutParams layoutParams, boolean z) {
        boolean addViewInLayout = super.addViewInLayout(view, i, layoutParams, z);
        if (addViewInLayout) {
            view.setScaleX(this.mChildScale);
            view.setScaleY(this.mChildScale);
        }
        return addViewInLayout;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int paddingLeft;
        int paddingRight;
        int paddingTop;
        int paddingBottom;
        int i5;
        int i6;
        int childCount = getChildCount();
        int layoutDirection = getLayoutDirection();
        float width = layoutDirection == 1 ? getWidth() - getPivotX() : getPivotX();
        if (this.mLayoutScaleX != 1.0f) {
            int paddingLeft2 = getPaddingLeft() + ((int) ((width - (width / this.mLayoutScaleX)) + 0.5f));
            paddingRight = ((int) (((((i3 - i) - width) / this.mLayoutScaleX) + width) + 0.5f)) - getPaddingRight();
            paddingLeft = paddingLeft2;
        } else {
            paddingLeft = getPaddingLeft();
            paddingRight = (i3 - i) - getPaddingRight();
        }
        float pivotY = getPivotY();
        if (this.mLayoutScaleY != 1.0f) {
            paddingTop = getPaddingTop() + ((int) ((pivotY - (pivotY / this.mLayoutScaleY)) + 0.5f));
            paddingBottom = ((int) (((((i4 - i2) - pivotY) / this.mLayoutScaleY) + pivotY) + 0.5f)) - getPaddingBottom();
        } else {
            paddingTop = getPaddingTop();
            paddingBottom = (i4 - i2) - getPaddingBottom();
        }
        for (int i7 = 0; i7 < childCount; i7++) {
            View childAt = getChildAt(i7);
            if (childAt.getVisibility() != 8) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i8 = layoutParams.gravity;
                int i9 = i8;
                if (i8 == -1) {
                    i9 = 8388659;
                }
                switch (Gravity.getAbsoluteGravity(i9, layoutDirection) & 7) {
                    case 1:
                        i5 = (((((paddingRight - paddingLeft) - measuredWidth) / 2) + paddingLeft) + layoutParams.leftMargin) - layoutParams.rightMargin;
                        break;
                    case 5:
                        i5 = (paddingRight - measuredWidth) - layoutParams.rightMargin;
                        break;
                    default:
                        i5 = paddingLeft + layoutParams.leftMargin;
                        break;
                }
                switch (i9 & 112) {
                    case 16:
                        i6 = (((((paddingBottom - paddingTop) - measuredHeight) / 2) + paddingTop) + layoutParams.topMargin) - layoutParams.bottomMargin;
                        break;
                    case 48:
                        i6 = paddingTop + layoutParams.topMargin;
                        break;
                    case 80:
                        i6 = (paddingBottom - measuredHeight) - layoutParams.bottomMargin;
                        break;
                    default:
                        i6 = paddingTop + layoutParams.topMargin;
                        break;
                }
                childAt.layout(i5, i6, i5 + measuredWidth, i6 + measuredHeight);
                childAt.setPivotX(width - i5);
                childAt.setPivotY(pivotY - i6);
            }
        }
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        if (this.mLayoutScaleX == 1.0f && this.mLayoutScaleY == 1.0f) {
            super.onMeasure(i, i2);
            return;
        }
        super.onMeasure(getScaledMeasureSpec(i, this.mLayoutScaleX), getScaledMeasureSpec(i2, this.mLayoutScaleY));
        setMeasuredDimension((int) ((getMeasuredWidth() * this.mLayoutScaleX) + 0.5f), (int) ((getMeasuredHeight() * this.mLayoutScaleY) + 0.5f));
    }

    @Override // android.view.View
    public void setForeground(Drawable drawable) {
        throw new UnsupportedOperationException();
    }
}
