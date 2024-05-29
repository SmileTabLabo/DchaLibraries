package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
/* loaded from: a.zip:android/support/v17/leanback/widget/ControlBar.class */
class ControlBar extends LinearLayout {
    private int mChildMarginFromCenter;
    private OnChildFocusedListener mOnChildFocusedListener;

    /* loaded from: a.zip:android/support/v17/leanback/widget/ControlBar$OnChildFocusedListener.class */
    public interface OnChildFocusedListener {
        void onChildFocusedListener(View view, View view2);
    }

    public ControlBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ControlBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mChildMarginFromCenter <= 0) {
            return;
        }
        int i3 = 0;
        for (int i4 = 0; i4 < getChildCount() - 1; i4++) {
            View childAt = getChildAt(i4);
            View childAt2 = getChildAt(i4 + 1);
            int measuredWidth = this.mChildMarginFromCenter - ((childAt.getMeasuredWidth() + childAt2.getMeasuredWidth()) / 2);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) childAt2.getLayoutParams();
            int marginStart = layoutParams.getMarginStart();
            layoutParams.setMarginStart(measuredWidth);
            childAt2.setLayoutParams(layoutParams);
            i3 += measuredWidth - marginStart;
        }
        setMeasuredDimension(getMeasuredWidth() + i3, getMeasuredHeight());
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        if (this.mOnChildFocusedListener != null) {
            this.mOnChildFocusedListener.onChildFocusedListener(view, view2);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean requestFocus(int i, Rect rect) {
        if (getChildCount() <= 0 || !getChildAt(getChildCount() / 2).requestFocus(i, rect)) {
            return super.requestFocus(i, rect);
        }
        return true;
    }
}
