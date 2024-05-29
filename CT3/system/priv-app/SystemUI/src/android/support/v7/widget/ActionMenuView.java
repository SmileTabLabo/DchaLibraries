package android.support.v7.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
/* loaded from: a.zip:android/support/v7/widget/ActionMenuView.class */
public class ActionMenuView extends LinearLayoutCompat implements MenuBuilder.ItemInvoker, MenuView {
    private boolean mFormatItems;
    private int mFormatItemsWidth;
    private int mGeneratedItemPadding;
    private MenuBuilder mMenu;
    private int mMinCellSize;
    private Context mPopupContext;
    private int mPopupTheme;
    private ActionMenuPresenter mPresenter;
    private boolean mReserveOverflow;

    /* loaded from: a.zip:android/support/v7/widget/ActionMenuView$ActionMenuChildView.class */
    public interface ActionMenuChildView {
        boolean needsDividerAfter();

        boolean needsDividerBefore();
    }

    /* loaded from: a.zip:android/support/v7/widget/ActionMenuView$LayoutParams.class */
    public static class LayoutParams extends LinearLayoutCompat.LayoutParams {
        @ViewDebug.ExportedProperty
        public int cellsUsed;
        @ViewDebug.ExportedProperty
        public boolean expandable;
        boolean expanded;
        @ViewDebug.ExportedProperty
        public int extraPixels;
        @ViewDebug.ExportedProperty
        public boolean isOverflowButton;
        @ViewDebug.ExportedProperty
        public boolean preventEdgeOffset;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.isOverflowButton = false;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
            this.isOverflowButton = layoutParams.isOverflowButton;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/ActionMenuView$OnMenuItemClickListener.class */
    public interface OnMenuItemClickListener {
    }

    public ActionMenuView(Context context) {
        this(context, null);
    }

    public ActionMenuView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBaselineAligned(false);
        float f = context.getResources().getDisplayMetrics().density;
        this.mMinCellSize = (int) (56.0f * f);
        this.mGeneratedItemPadding = (int) (4.0f * f);
        this.mPopupContext = context;
        this.mPopupTheme = 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0046, code lost:
        if (r6 >= 2) goto L13;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static int measureChildForCells(View view, int i, int i2, int i3, int i4) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i3) - i4, View.MeasureSpec.getMode(i3));
        ActionMenuItemView actionMenuItemView = view instanceof ActionMenuItemView ? (ActionMenuItemView) view : null;
        boolean hasText = actionMenuItemView != null ? actionMenuItemView.hasText() : false;
        int i5 = 0;
        if (i2 > 0) {
            if (hasText) {
                i5 = 0;
            }
            view.measure(View.MeasureSpec.makeMeasureSpec(i * i2, Integer.MIN_VALUE), makeMeasureSpec);
            int measuredWidth = view.getMeasuredWidth();
            int i6 = measuredWidth / i;
            int i7 = i6;
            if (measuredWidth % i != 0) {
                i7 = i6 + 1;
            }
            i5 = i7;
            if (hasText) {
                i5 = i7;
                if (i7 < 2) {
                    i5 = 2;
                }
            }
        }
        if (layoutParams.isOverflowButton) {
            hasText = false;
        }
        layoutParams.expandable = hasText;
        layoutParams.cellsUsed = i5;
        view.measure(View.MeasureSpec.makeMeasureSpec(i5 * i, 1073741824), makeMeasureSpec);
        return i5;
    }

    /* JADX WARN: Code restructure failed: missing block: B:74:0x02a8, code lost:
        if (r17 > 1) goto L106;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void onMeasureExactFormat(int i, int i2) {
        long j;
        boolean z;
        int i3;
        long j2;
        int i4;
        int i5;
        long j3;
        boolean z2;
        long j4;
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int childMeasureSpec = getChildMeasureSpec(i2, paddingTop, -2);
        int i6 = size - (paddingLeft + paddingRight);
        int i7 = i6 / this.mMinCellSize;
        int i8 = this.mMinCellSize;
        if (i7 == 0) {
            setMeasuredDimension(i6, 0);
            return;
        }
        int i9 = this.mMinCellSize + ((i6 % i8) / i7);
        int i10 = 0;
        int i11 = 0;
        int i12 = 0;
        int i13 = 0;
        boolean z3 = false;
        long j5 = 0;
        int childCount = getChildCount();
        int i14 = 0;
        while (i14 < childCount) {
            View childAt = getChildAt(i14);
            if (childAt.getVisibility() == 8) {
                j4 = j5;
                z2 = z3;
            } else {
                boolean z4 = childAt instanceof ActionMenuItemView;
                int i15 = i13 + 1;
                if (z4) {
                    childAt.setPadding(this.mGeneratedItemPadding, 0, this.mGeneratedItemPadding, 0);
                }
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                layoutParams.expanded = false;
                layoutParams.extraPixels = 0;
                layoutParams.cellsUsed = 0;
                layoutParams.expandable = false;
                layoutParams.leftMargin = 0;
                layoutParams.rightMargin = 0;
                layoutParams.preventEdgeOffset = z4 ? ((ActionMenuItemView) childAt).hasText() : false;
                int measureChildForCells = measureChildForCells(childAt, i9, layoutParams.isOverflowButton ? 1 : i7, childMeasureSpec, paddingTop);
                int max = Math.max(i11, measureChildForCells);
                int i16 = i12;
                if (layoutParams.expandable) {
                    i16 = i12 + 1;
                }
                if (layoutParams.isOverflowButton) {
                    z3 = true;
                }
                int i17 = i7 - measureChildForCells;
                int max2 = Math.max(i10, childAt.getMeasuredHeight());
                i7 = i17;
                i12 = i16;
                z2 = z3;
                i11 = max;
                i10 = max2;
                j4 = j5;
                i13 = i15;
                if (measureChildForCells == 1) {
                    j4 = j5 | (1 << i14);
                    i7 = i17;
                    i12 = i16;
                    z2 = z3;
                    i11 = max;
                    i10 = max2;
                    i13 = i15;
                }
            }
            i14++;
            z3 = z2;
            j5 = j4;
        }
        boolean z5 = z3 && i13 == 2;
        boolean z6 = false;
        int i18 = i7;
        while (true) {
            j = j5;
            if (i12 <= 0) {
                break;
            }
            j = j5;
            if (i18 <= 0) {
                break;
            }
            int i19 = Integer.MAX_VALUE;
            long j6 = 0;
            int i20 = 0;
            int i21 = 0;
            while (i21 < childCount) {
                LayoutParams layoutParams2 = (LayoutParams) getChildAt(i21).getLayoutParams();
                if (!layoutParams2.expandable) {
                    j3 = j6;
                    i5 = i20;
                    i4 = i19;
                } else if (layoutParams2.cellsUsed < i19) {
                    i4 = layoutParams2.cellsUsed;
                    j3 = 1 << i21;
                    i5 = 1;
                } else {
                    i4 = i19;
                    i5 = i20;
                    j3 = j6;
                    if (layoutParams2.cellsUsed == i19) {
                        j3 = j6 | (1 << i21);
                        i5 = i20 + 1;
                        i4 = i19;
                    }
                }
                i21++;
                i19 = i4;
                i20 = i5;
                j6 = j3;
            }
            j5 |= j6;
            if (i20 > i18) {
                j = j5;
                break;
            }
            int i22 = 0;
            while (i22 < childCount) {
                View childAt2 = getChildAt(i22);
                LayoutParams layoutParams3 = (LayoutParams) childAt2.getLayoutParams();
                if (((1 << i22) & j6) == 0) {
                    i3 = i18;
                    j2 = j5;
                    if (layoutParams3.cellsUsed == i19 + 1) {
                        j2 = j5 | (1 << i22);
                        i3 = i18;
                    }
                } else {
                    if (z5 && layoutParams3.preventEdgeOffset && i18 == 1) {
                        childAt2.setPadding(this.mGeneratedItemPadding + i9, 0, this.mGeneratedItemPadding, 0);
                    }
                    layoutParams3.cellsUsed++;
                    layoutParams3.expanded = true;
                    i3 = i18 - 1;
                    j2 = j5;
                }
                i22++;
                i18 = i3;
                j5 = j2;
            }
            z6 = true;
        }
        boolean z7 = !z3 && i13 == 1;
        boolean z8 = z6;
        if (i18 > 0) {
            z8 = z6;
            if (j != 0) {
                if (i18 >= i13 - 1 && !z7) {
                    z8 = z6;
                }
                float bitCount = Long.bitCount(j);
                float f = bitCount;
                if (!z7) {
                    float f2 = bitCount;
                    if ((1 & j) != 0) {
                        f2 = bitCount;
                        if (!((LayoutParams) getChildAt(0).getLayoutParams()).preventEdgeOffset) {
                            f2 = bitCount - 0.5f;
                        }
                    }
                    f = f2;
                    if (((1 << (childCount - 1)) & j) != 0) {
                        f = f2;
                        if (!((LayoutParams) getChildAt(childCount - 1).getLayoutParams()).preventEdgeOffset) {
                            f = f2 - 0.5f;
                        }
                    }
                }
                int i23 = f > 0.0f ? (int) ((i18 * i9) / f) : 0;
                int i24 = 0;
                while (i24 < childCount) {
                    if (((1 << i24) & j) == 0) {
                        z = z6;
                    } else {
                        View childAt3 = getChildAt(i24);
                        LayoutParams layoutParams4 = (LayoutParams) childAt3.getLayoutParams();
                        if (childAt3 instanceof ActionMenuItemView) {
                            layoutParams4.extraPixels = i23;
                            layoutParams4.expanded = true;
                            if (i24 == 0 && !layoutParams4.preventEdgeOffset) {
                                layoutParams4.leftMargin = (-i23) / 2;
                            }
                            z = true;
                        } else if (layoutParams4.isOverflowButton) {
                            layoutParams4.extraPixels = i23;
                            layoutParams4.expanded = true;
                            layoutParams4.rightMargin = (-i23) / 2;
                            z = true;
                        } else {
                            if (i24 != 0) {
                                layoutParams4.leftMargin = i23 / 2;
                            }
                            z = z6;
                            if (i24 != childCount - 1) {
                                layoutParams4.rightMargin = i23 / 2;
                                z = z6;
                            }
                        }
                    }
                    i24++;
                    z6 = z;
                }
                z8 = z6;
            }
        }
        if (z8) {
            for (int i25 = 0; i25 < childCount; i25++) {
                View childAt4 = getChildAt(i25);
                LayoutParams layoutParams5 = (LayoutParams) childAt4.getLayoutParams();
                if (layoutParams5.expanded) {
                    childAt4.measure(View.MeasureSpec.makeMeasureSpec((layoutParams5.cellsUsed * i9) + layoutParams5.extraPixels, 1073741824), childMeasureSpec);
                }
            }
        }
        int i26 = size2;
        if (mode != 1073741824) {
            i26 = i10;
        }
        setMeasuredDimension(i6, i26);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams != null ? layoutParams instanceof LayoutParams : false;
    }

    public void dismissPopupMenus() {
        if (this.mPresenter != null) {
            this.mPresenter.dismissPopupMenus();
        }
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.gravity = 16;
        return layoutParams;
    }

    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (layoutParams != null) {
            LayoutParams layoutParams2 = layoutParams instanceof LayoutParams ? new LayoutParams((LayoutParams) layoutParams) : new LayoutParams(layoutParams);
            if (layoutParams2.gravity <= 0) {
                layoutParams2.gravity = 16;
            }
            return layoutParams2;
        }
        return generateDefaultLayoutParams();
    }

    public LayoutParams generateOverflowButtonLayoutParams() {
        LayoutParams generateDefaultLayoutParams = generateDefaultLayoutParams();
        generateDefaultLayoutParams.isOverflowButton = true;
        return generateDefaultLayoutParams;
    }

    protected boolean hasSupportDividerBeforeChildAt(int i) {
        if (i == 0) {
            return false;
        }
        View childAt = getChildAt(i - 1);
        View childAt2 = getChildAt(i);
        boolean z = false;
        if (i < getChildCount()) {
            z = false;
            if (childAt instanceof ActionMenuChildView) {
                z = ((ActionMenuChildView) childAt).needsDividerAfter();
            }
        }
        boolean z2 = z;
        if (i > 0) {
            z2 = z;
            if (childAt2 instanceof ActionMenuChildView) {
                z2 = z | ((ActionMenuChildView) childAt2).needsDividerBefore();
            }
        }
        return z2;
    }

    @Override // android.support.v7.view.menu.MenuBuilder.ItemInvoker
    public boolean invokeItem(MenuItemImpl menuItemImpl) {
        return this.mMenu.performItemAction(menuItemImpl, 0);
    }

    public boolean isOverflowMenuShowing() {
        return this.mPresenter != null ? this.mPresenter.isOverflowMenuShowing() : false;
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        if (Build.VERSION.SDK_INT >= 8) {
            super.onConfigurationChanged(configuration);
        }
        if (this.mPresenter != null) {
            this.mPresenter.updateMenuView(false);
            if (this.mPresenter.isOverflowMenuShowing()) {
                this.mPresenter.hideOverflowMenu();
                this.mPresenter.showOverflowMenu();
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dismissPopupMenus();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int width;
        int i5;
        if (!this.mFormatItems) {
            super.onLayout(z, i, i2, i3, i4);
            return;
        }
        int childCount = getChildCount();
        int i6 = (i4 - i2) / 2;
        int dividerWidth = getDividerWidth();
        int i7 = 0;
        int i8 = 0;
        int paddingRight = ((i3 - i) - getPaddingRight()) - getPaddingLeft();
        boolean z2 = false;
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        for (int i9 = 0; i9 < childCount; i9++) {
            View childAt = getChildAt(i9);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.isOverflowButton) {
                    int measuredWidth = childAt.getMeasuredWidth();
                    int i10 = measuredWidth;
                    if (hasSupportDividerBeforeChildAt(i9)) {
                        i10 = measuredWidth + dividerWidth;
                    }
                    int measuredHeight = childAt.getMeasuredHeight();
                    if (isLayoutRtl) {
                        i5 = getPaddingLeft() + layoutParams.leftMargin;
                        width = i5 + i10;
                    } else {
                        width = (getWidth() - getPaddingRight()) - layoutParams.rightMargin;
                        i5 = width - i10;
                    }
                    int i11 = i6 - (measuredHeight / 2);
                    childAt.layout(i5, i11, width, i11 + measuredHeight);
                    paddingRight -= i10;
                    z2 = true;
                } else {
                    int measuredWidth2 = childAt.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                    int i12 = i7 + measuredWidth2;
                    int i13 = paddingRight - measuredWidth2;
                    int i14 = i12;
                    if (hasSupportDividerBeforeChildAt(i9)) {
                        i14 = i12 + dividerWidth;
                    }
                    i8++;
                    i7 = i14;
                    paddingRight = i13;
                }
            }
        }
        if (childCount == 1 && !z2) {
            View childAt2 = getChildAt(0);
            int measuredWidth3 = childAt2.getMeasuredWidth();
            int measuredHeight2 = childAt2.getMeasuredHeight();
            int i15 = ((i3 - i) / 2) - (measuredWidth3 / 2);
            int i16 = i6 - (measuredHeight2 / 2);
            childAt2.layout(i15, i16, i15 + measuredWidth3, i16 + measuredHeight2);
            return;
        }
        int i17 = i8 - (z2 ? 0 : 1);
        int max = Math.max(0, i17 > 0 ? paddingRight / i17 : 0);
        if (isLayoutRtl) {
            int width2 = getWidth() - getPaddingRight();
            int i18 = 0;
            while (i18 < childCount) {
                View childAt3 = getChildAt(i18);
                LayoutParams layoutParams2 = (LayoutParams) childAt3.getLayoutParams();
                int i19 = width2;
                if (childAt3.getVisibility() != 8) {
                    if (layoutParams2.isOverflowButton) {
                        i19 = width2;
                    } else {
                        int i20 = width2 - layoutParams2.rightMargin;
                        int measuredWidth4 = childAt3.getMeasuredWidth();
                        int measuredHeight3 = childAt3.getMeasuredHeight();
                        int i21 = i6 - (measuredHeight3 / 2);
                        childAt3.layout(i20 - measuredWidth4, i21, i20, i21 + measuredHeight3);
                        i19 = i20 - ((layoutParams2.leftMargin + measuredWidth4) + max);
                    }
                }
                i18++;
                width2 = i19;
            }
            return;
        }
        int paddingLeft = getPaddingLeft();
        int i22 = 0;
        while (i22 < childCount) {
            View childAt4 = getChildAt(i22);
            LayoutParams layoutParams3 = (LayoutParams) childAt4.getLayoutParams();
            int i23 = paddingLeft;
            if (childAt4.getVisibility() != 8) {
                if (layoutParams3.isOverflowButton) {
                    i23 = paddingLeft;
                } else {
                    int i24 = paddingLeft + layoutParams3.leftMargin;
                    int measuredWidth5 = childAt4.getMeasuredWidth();
                    int measuredHeight4 = childAt4.getMeasuredHeight();
                    int i25 = i6 - (measuredHeight4 / 2);
                    childAt4.layout(i24, i25, i24 + measuredWidth5, i25 + measuredHeight4);
                    i23 = i24 + layoutParams3.rightMargin + measuredWidth5 + max;
                }
            }
            i22++;
            paddingLeft = i23;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.View
    public void onMeasure(int i, int i2) {
        boolean z = this.mFormatItems;
        this.mFormatItems = View.MeasureSpec.getMode(i) == 1073741824;
        if (z != this.mFormatItems) {
            this.mFormatItemsWidth = 0;
        }
        int size = View.MeasureSpec.getSize(i);
        if (this.mFormatItems && this.mMenu != null && size != this.mFormatItemsWidth) {
            this.mFormatItemsWidth = size;
            this.mMenu.onItemsChanged(true);
        }
        int childCount = getChildCount();
        if (this.mFormatItems && childCount > 0) {
            onMeasureExactFormat(i, i2);
            return;
        }
        for (int i3 = 0; i3 < childCount; i3++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i3).getLayoutParams();
            layoutParams.rightMargin = 0;
            layoutParams.leftMargin = 0;
        }
        super.onMeasure(i, i2);
    }

    public MenuBuilder peekMenu() {
        return this.mMenu;
    }

    public void setOverflowReserved(boolean z) {
        this.mReserveOverflow = z;
    }

    public boolean showOverflowMenu() {
        return this.mPresenter != null ? this.mPresenter.showOverflowMenu() : false;
    }
}
