package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: a.zip:com/android/systemui/statusbar/KeyboardShortcutKeysLayout.class */
public final class KeyboardShortcutKeysLayout extends ViewGroup {
    private final Context mContext;
    private int mLineHeight;

    /* loaded from: a.zip:com/android/systemui/statusbar/KeyboardShortcutKeysLayout$LayoutParams.class */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public final int mHorizontalSpacing;
        public final int mVerticalSpacing;

        public LayoutParams(int i, int i2) {
            super(0, 0);
            this.mHorizontalSpacing = i;
            this.mVerticalSpacing = i2;
        }

        public LayoutParams(int i, int i2, ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mHorizontalSpacing = i;
            this.mVerticalSpacing = i2;
        }
    }

    public KeyboardShortcutKeysLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    public KeyboardShortcutKeysLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    private int getHorizontalVerticalSpacing() {
        return (int) TypedValue.applyDimension(1, 4.0f, getResources().getDisplayMetrics());
    }

    private boolean isRTL() {
        boolean z = true;
        if (this.mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }

    private void layoutChildrenOnRow(int i, int i2, int i3, int i4, int i5, int i6) {
        int i7;
        int i8 = i4;
        if (!isRTL()) {
            i8 = ((getPaddingLeft() + i3) - i4) + i6;
        }
        int i9 = i;
        int i10 = i8;
        while (i9 < i2) {
            View childAt = getChildAt(i9);
            int measuredWidth = childAt.getMeasuredWidth();
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            int i11 = i10;
            if (isRTL()) {
                i11 = i10;
                if (i9 == i) {
                    i11 = (((i3 - i10) - getPaddingRight()) - measuredWidth) - layoutParams.mHorizontalSpacing;
                }
            }
            childAt.layout(i11, i5, i11 + measuredWidth, childAt.getMeasuredHeight() + i5);
            if (isRTL()) {
                i7 = i11 - (layoutParams.mHorizontalSpacing + (i9 < i2 - 1 ? getChildAt(i9 + 1).getMeasuredWidth() : 0));
            } else {
                i7 = i11 + layoutParams.mHorizontalSpacing + measuredWidth;
            }
            i10 = i7;
            i9++;
        }
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        int horizontalVerticalSpacing = getHorizontalVerticalSpacing();
        return new LayoutParams(horizontalVerticalSpacing, horizontalVerticalSpacing);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        int horizontalVerticalSpacing = getHorizontalVerticalSpacing();
        return new LayoutParams(horizontalVerticalSpacing, horizontalVerticalSpacing, layoutParams);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        int i5 = i3 - i;
        int paddingRight = isRTL() ? i5 - getPaddingRight() : getPaddingLeft();
        int paddingTop = getPaddingTop();
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        while (i8 < childCount) {
            View childAt = getChildAt(i8);
            int i9 = i7;
            int i10 = paddingRight;
            int i11 = paddingTop;
            int i12 = i6;
            if (childAt.getVisibility() != 8) {
                int measuredWidth = childAt.getMeasuredWidth();
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                i9 = i7;
                int i13 = paddingRight;
                i11 = paddingTop;
                if (isRTL() ? (paddingRight - getPaddingLeft()) - measuredWidth < 0 : paddingRight + measuredWidth > i5) {
                    layoutChildrenOnRow(i7, i8, i5, paddingRight, paddingTop, i6);
                    int paddingRight2 = isRTL() ? i5 - getPaddingRight() : getPaddingLeft();
                    i11 = paddingTop + this.mLineHeight;
                    i9 = i8;
                    i13 = paddingRight2;
                }
                i10 = isRTL() ? (i13 - measuredWidth) - layoutParams.mHorizontalSpacing : i13 + measuredWidth + layoutParams.mHorizontalSpacing;
                i12 = layoutParams.mHorizontalSpacing;
            }
            i8++;
            i7 = i9;
            paddingRight = i10;
            paddingTop = i11;
            i6 = i12;
        }
        if (i7 < childCount) {
            layoutChildrenOnRow(i7, childCount, i5, paddingRight, paddingTop, i6);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int size = (View.MeasureSpec.getSize(i) - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        int size2 = (View.MeasureSpec.getSize(i2) - getPaddingTop()) - getPaddingBottom();
        int i4 = 0;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int makeMeasureSpec = View.MeasureSpec.getMode(i2) == Integer.MIN_VALUE ? View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE) : View.MeasureSpec.makeMeasureSpec(0, 0);
        int i5 = 0;
        while (i5 < childCount) {
            View childAt = getChildAt(i5);
            int i6 = i4;
            int i7 = paddingLeft;
            int i8 = paddingTop;
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                childAt.measure(View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE), makeMeasureSpec);
                int measuredWidth = childAt.getMeasuredWidth();
                i6 = Math.max(i4, childAt.getMeasuredHeight() + layoutParams.mVerticalSpacing);
                int i9 = paddingLeft;
                i8 = paddingTop;
                if (paddingLeft + measuredWidth > size) {
                    i9 = getPaddingLeft();
                    i8 = paddingTop + i6;
                }
                i7 = i9 + layoutParams.mHorizontalSpacing + measuredWidth;
            }
            i5++;
            i4 = i6;
            paddingLeft = i7;
            paddingTop = i8;
        }
        this.mLineHeight = i4;
        if (View.MeasureSpec.getMode(i2) == 0) {
            i3 = paddingTop + i4;
        } else {
            i3 = size2;
            if (View.MeasureSpec.getMode(i2) == Integer.MIN_VALUE) {
                i3 = size2;
                if (paddingTop + i4 < size2) {
                    i3 = paddingTop + i4;
                }
            }
        }
        setMeasuredDimension(size, i3);
    }
}
