package com.android.browser;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import com.android.browser.TabBar;
/* loaded from: b.zip:com/android/browser/TabScrollView.class */
public class TabScrollView extends HorizontalScrollView {
    private int mAnimationDuration;
    private LinearLayout mContentView;
    private int mSelected;
    private int mTabOverlap;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/TabScrollView$TabLayout.class */
    public class TabLayout extends LinearLayout {
        final TabScrollView this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public TabLayout(TabScrollView tabScrollView, Context context) {
            super(context);
            this.this$0 = tabScrollView;
            setChildrenDrawingOrderEnabled(true);
        }

        @Override // android.view.ViewGroup
        protected int getChildDrawingOrder(int i, int i2) {
            int i3;
            if (i2 != i - 1 || this.this$0.mSelected < 0 || this.this$0.mSelected >= i) {
                int i4 = (i - i2) - 1;
                i3 = i4;
                if (i4 <= this.this$0.mSelected) {
                    i3 = i4;
                    if (i4 > 0) {
                        i3 = i4 - 1;
                    }
                }
            } else {
                i3 = this.this$0.mSelected;
            }
            return i3;
        }

        @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            int i5;
            super.onLayout(z, i, i2, i3, i4);
            if (getChildCount() > 1) {
                int right = getChildAt(0).getRight() - this.this$0.mTabOverlap;
                int layoutDirection = getResources().getConfiguration().getLayoutDirection();
                if (layoutDirection == 1) {
                    right = getChildAt(0).getLeft() + this.this$0.mTabOverlap;
                }
                for (int i6 = 1; i6 < getChildCount(); i6++) {
                    View childAt = getChildAt(i6);
                    int right2 = childAt.getRight() - childAt.getLeft();
                    if (layoutDirection == 1) {
                        childAt.layout(right - right2, childAt.getTop(), right, childAt.getBottom());
                        i5 = (right - right2) + this.this$0.mTabOverlap;
                    } else {
                        childAt.layout(right, childAt.getTop(), right + right2, childAt.getBottom());
                        i5 = (right + right2) - this.this$0.mTabOverlap;
                    }
                    right = i5;
                }
            }
        }

        @Override // android.widget.LinearLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            setMeasuredDimension(getMeasuredWidth() - (Math.max(0, this.this$0.mContentView.getChildCount() - 1) * this.this$0.mTabOverlap), getMeasuredHeight());
        }
    }

    public TabScrollView(Context context) {
        super(context);
        init(context);
    }

    public TabScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public TabScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void animateScroll(int i) {
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "scroll", getScrollX(), i);
        ofInt.setDuration(this.mAnimationDuration);
        ofInt.start();
    }

    private void ensureChildVisible(View view) {
        if (view != null) {
            int left = view.getLeft();
            int width = left + view.getWidth();
            int scrollX = getScrollX();
            int width2 = scrollX + getWidth();
            if (left < scrollX) {
                animateScroll(left);
            } else if (width > width2) {
                animateScroll((width - width2) + scrollX);
            }
        }
    }

    private void init(Context context) {
        this.mAnimationDuration = context.getResources().getInteger(2131623939);
        this.mTabOverlap = (int) context.getResources().getDimension(2131427330);
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(2);
        this.mContentView = new TabLayout(this, context);
        this.mContentView.setOrientation(0);
        this.mContentView.setLayoutParams(new FrameLayout.LayoutParams(-2, -1));
        this.mContentView.setPadding((int) context.getResources().getDimension(2131427358), 0, 0, 0);
        addView(this.mContentView);
        this.mSelected = -1;
        setScroll(getScroll());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addTab(View view) {
        this.mContentView.addView(view);
        view.setActivated(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearTabs() {
        this.mContentView.removeAllViews();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getChildIndex(View view) {
        return this.mContentView.indexOfChild(view);
    }

    public int getScroll() {
        return getScrollX();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public View getSelectedTab() {
        if (this.mSelected < 0 || this.mSelected >= this.mContentView.getChildCount()) {
            return null;
        }
        return this.mContentView.getChildAt(this.mSelected);
    }

    @Override // android.widget.HorizontalScrollView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        ensureChildVisible(getSelectedTab());
    }

    @Override // android.view.View
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        if (isHardwareAccelerated()) {
            int childCount = this.mContentView.getChildCount();
            for (int i5 = 0; i5 < childCount; i5++) {
                this.mContentView.getChildAt(i5).invalidate();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeTab(View view) {
        int indexOfChild = this.mContentView.indexOfChild(view);
        if (indexOfChild == this.mSelected) {
            this.mSelected = -1;
        } else if (indexOfChild < this.mSelected) {
            this.mSelected--;
        }
        this.mContentView.removeView(view);
    }

    public void setScroll(int i) {
        scrollTo(i, getScrollY());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSelectedTab(int i) {
        View selectedTab = getSelectedTab();
        if (selectedTab != null) {
            selectedTab.setActivated(false);
        }
        this.mSelected = i;
        View selectedTab2 = getSelectedTab();
        if (selectedTab2 != null) {
            selectedTab2.setActivated(true);
        }
        requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateLayout() {
        int childCount = this.mContentView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((TabBar.TabView) this.mContentView.getChildAt(i)).updateLayoutParams();
        }
        ensureChildVisible(getSelectedTab());
    }
}
