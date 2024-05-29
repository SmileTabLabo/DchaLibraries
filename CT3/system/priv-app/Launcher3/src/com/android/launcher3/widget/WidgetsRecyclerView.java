package com.android.launcher3.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.model.WidgetsModel;
/* loaded from: a.zip:com/android/launcher3/widget/WidgetsRecyclerView.class */
public class WidgetsRecyclerView extends BaseRecyclerView {
    private BaseRecyclerView.ScrollPositionState mScrollPosState;
    private WidgetsModel mWidgets;

    public WidgetsRecyclerView(Context context) {
        this(context, null);
    }

    public WidgetsRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidgetsRecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mScrollPosState = new BaseRecyclerView.ScrollPositionState();
    }

    public WidgetsRecyclerView(Context context, AttributeSet attributeSet, int i, int i2) {
        this(context, attributeSet, i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseRecyclerView, android.view.ViewGroup, android.view.View
    public void dispatchDraw(Canvas canvas) {
        canvas.clipRect(this.mBackgroundPadding.left, this.mBackgroundPadding.top, getWidth() - this.mBackgroundPadding.right, getHeight() - this.mBackgroundPadding.bottom);
        super.dispatchDraw(canvas);
    }

    protected void getCurScrollState(BaseRecyclerView.ScrollPositionState scrollPositionState, int i) {
        View childAt;
        scrollPositionState.rowIndex = -1;
        scrollPositionState.rowTopOffset = -1;
        scrollPositionState.itemPos = -1;
        if (this.mWidgets == null || this.mWidgets.getPackageSize() == 0 || (childAt = getChildAt(0)) == null) {
            return;
        }
        int childPosition = getChildPosition(childAt);
        scrollPositionState.rowIndex = childPosition;
        scrollPositionState.rowTopOffset = getLayoutManager().getDecoratedTop(childAt);
        scrollPositionState.itemPos = childPosition;
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public int getFastScrollerTrackColor(int i) {
        return -1;
    }

    @Override // com.android.launcher3.BaseRecyclerView
    protected int getTop(int i) {
        if (getChildCount() == 0) {
            return 0;
        }
        return getChildAt(0).getMeasuredHeight() * i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseRecyclerView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        addOnItemTouchListener(this);
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public void onUpdateScrollbar(int i) {
        if (this.mWidgets == null) {
            return;
        }
        int packageSize = this.mWidgets.getPackageSize();
        if (packageSize == 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        getCurScrollState(this.mScrollPosState, -1);
        if (this.mScrollPosState.rowIndex < 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
        } else {
            synchronizeScrollBarThumbOffsetToViewScroll(this.mScrollPosState, packageSize);
        }
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public String scrollToPositionAtProgress(float f) {
        int packageSize;
        if (this.mWidgets == null || (packageSize = this.mWidgets.getPackageSize()) == 0) {
            return "";
        }
        stopScroll();
        getCurScrollState(this.mScrollPosState, -1);
        float f2 = packageSize * f;
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(0, (int) (-(getAvailableScrollHeight(packageSize) * f)));
        float f3 = f2;
        if (f == 1.0f) {
            f3 = f2 - 1.0f;
        }
        return this.mWidgets.getPackageItemInfo((int) f3).titleSectionName;
    }

    public void setWidgets(WidgetsModel widgetsModel) {
        this.mWidgets = widgetsModel;
    }
}
