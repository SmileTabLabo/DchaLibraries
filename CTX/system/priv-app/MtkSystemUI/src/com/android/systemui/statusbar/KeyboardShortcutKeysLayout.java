package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: classes.dex */
public final class KeyboardShortcutKeysLayout extends ViewGroup {
    private final Context mContext;
    private int mLineHeight;

    public KeyboardShortcutKeysLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    public KeyboardShortcutKeysLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int makeMeasureSpec;
        int i3;
        int size = (View.MeasureSpec.getSize(i) - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        int size2 = (View.MeasureSpec.getSize(i2) - getPaddingTop()) - getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (View.MeasureSpec.getMode(i2) == Integer.MIN_VALUE) {
            makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE);
        } else {
            makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        }
        int i4 = paddingTop;
        int i5 = paddingLeft;
        int i6 = 0;
        for (int i7 = 0; i7 < childCount; i7++) {
            View childAt = getChildAt(i7);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                childAt.measure(View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE), makeMeasureSpec);
                int measuredWidth = childAt.getMeasuredWidth();
                i6 = Math.max(i6, childAt.getMeasuredHeight() + layoutParams.mVerticalSpacing);
                if (i5 + measuredWidth > size) {
                    i5 = getPaddingLeft();
                    i4 += i6;
                }
                i5 += measuredWidth + layoutParams.mHorizontalSpacing;
            }
        }
        this.mLineHeight = i6;
        if (View.MeasureSpec.getMode(i2) == 0) {
            size2 = i4 + i6;
        } else if (View.MeasureSpec.getMode(i2) == Integer.MIN_VALUE && (i3 = i4 + i6) < size2) {
            size2 = i3;
        }
        setMeasuredDimension(size, size2);
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

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int paddingLeft;
        int paddingLeft2;
        int childCount = getChildCount();
        int i5 = i3 - i;
        if (isRTL()) {
            paddingLeft = i5 - getPaddingRight();
        } else {
            paddingLeft = getPaddingLeft();
        }
        int paddingTop = getPaddingTop();
        int i6 = paddingLeft;
        int i7 = 0;
        int i8 = 0;
        for (int i9 = 0; i9 < childCount; i9++) {
            View childAt = getChildAt(i9);
            if (childAt.getVisibility() != 8) {
                int measuredWidth = childAt.getMeasuredWidth();
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                boolean z2 = true;
                if (!isRTL() ? i6 + measuredWidth <= i5 : (i6 - getPaddingLeft()) - measuredWidth >= 0) {
                    z2 = false;
                }
                if (z2) {
                    layoutChildrenOnRow(i7, i9, i5, i6, paddingTop, i8);
                    if (isRTL()) {
                        paddingLeft2 = i5 - getPaddingRight();
                    } else {
                        paddingLeft2 = getPaddingLeft();
                    }
                    i6 = paddingLeft2;
                    paddingTop += this.mLineHeight;
                    i7 = i9;
                }
                if (isRTL()) {
                    i6 = (i6 - measuredWidth) - layoutParams.mHorizontalSpacing;
                } else {
                    i6 = i6 + measuredWidth + layoutParams.mHorizontalSpacing;
                }
                i8 = layoutParams.mHorizontalSpacing;
            }
        }
        if (i7 < childCount) {
            layoutChildrenOnRow(i7, childCount, i5, i6, paddingTop, i8);
        }
    }

    private int getHorizontalVerticalSpacing() {
        return (int) TypedValue.applyDimension(1, 4.0f, getResources().getDisplayMetrics());
    }

    private void layoutChildrenOnRow(int i, int i2, int i3, int i4, int i5, int i6) {
        int i7;
        if (!isRTL()) {
            i4 = ((getPaddingLeft() + i3) - i4) + i6;
        }
        int i8 = i4;
        for (int i9 = i; i9 < i2; i9++) {
            View childAt = getChildAt(i9);
            int measuredWidth = childAt.getMeasuredWidth();
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (isRTL() && i9 == i) {
                i8 = (((i3 - i8) - getPaddingRight()) - measuredWidth) - layoutParams.mHorizontalSpacing;
            }
            childAt.layout(i8, i5, i8 + measuredWidth, childAt.getMeasuredHeight() + i5);
            if (isRTL()) {
                if (i9 < i2 - 1) {
                    i7 = getChildAt(i9 + 1).getMeasuredWidth();
                } else {
                    i7 = 0;
                }
                i8 -= i7 + layoutParams.mHorizontalSpacing;
            } else {
                i8 += measuredWidth + layoutParams.mHorizontalSpacing;
            }
        }
    }

    private boolean isRTL() {
        return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
    }

    /* loaded from: classes.dex */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public final int mHorizontalSpacing;
        public final int mVerticalSpacing;

        public LayoutParams(int i, int i2, ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mHorizontalSpacing = i;
            this.mVerticalSpacing = i2;
        }

        public LayoutParams(int i, int i2) {
            super(0, 0);
            this.mHorizontalSpacing = i;
            this.mVerticalSpacing = i2;
        }
    }
}
