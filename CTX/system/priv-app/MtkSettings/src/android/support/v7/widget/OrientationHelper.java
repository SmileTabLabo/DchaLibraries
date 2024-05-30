package android.support.v7.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.android.settingslib.wifi.AccessPoint;
/* loaded from: classes.dex */
public abstract class OrientationHelper {
    private int mLastTotalSpace;
    protected final RecyclerView.LayoutManager mLayoutManager;
    final Rect mTmpRect;

    public abstract int getDecoratedEnd(View view);

    public abstract int getDecoratedMeasurement(View view);

    public abstract int getDecoratedMeasurementInOther(View view);

    public abstract int getDecoratedStart(View view);

    public abstract int getEnd();

    public abstract int getEndAfterPadding();

    public abstract int getEndPadding();

    public abstract int getMode();

    public abstract int getModeInOther();

    public abstract int getStartAfterPadding();

    public abstract int getTotalSpace();

    public abstract int getTransformedEndWithDecoration(View view);

    public abstract int getTransformedStartWithDecoration(View view);

    public abstract void offsetChildren(int i);

    private OrientationHelper(RecyclerView.LayoutManager layoutManager) {
        this.mLastTotalSpace = AccessPoint.UNREACHABLE_RSSI;
        this.mTmpRect = new Rect();
        this.mLayoutManager = layoutManager;
    }

    public void onLayoutComplete() {
        this.mLastTotalSpace = getTotalSpace();
    }

    public int getTotalSpaceChange() {
        if (Integer.MIN_VALUE == this.mLastTotalSpace) {
            return 0;
        }
        return getTotalSpace() - this.mLastTotalSpace;
    }

    public static OrientationHelper createOrientationHelper(RecyclerView.LayoutManager layoutManager, int orientation) {
        switch (orientation) {
            case 0:
                return createHorizontalHelper(layoutManager);
            case 1:
                return createVerticalHelper(layoutManager);
            default:
                throw new IllegalArgumentException("invalid orientation");
        }
    }

    public static OrientationHelper createHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) { // from class: android.support.v7.widget.OrientationHelper.1
            @Override // android.support.v7.widget.OrientationHelper
            public int getEndAfterPadding() {
                return this.mLayoutManager.getWidth() - this.mLayoutManager.getPaddingRight();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getEnd() {
                return this.mLayoutManager.getWidth();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public void offsetChildren(int amount) {
                this.mLayoutManager.offsetChildrenHorizontal(amount);
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getStartAfterPadding() {
                return this.mLayoutManager.getPaddingLeft();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedMeasurement(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedMeasurementInOther(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedEnd(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedRight(view) + params.rightMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedStart(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedLeft(view) - params.leftMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getTransformedEndWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.right;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getTransformedStartWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.left;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getTotalSpace() {
                return (this.mLayoutManager.getWidth() - this.mLayoutManager.getPaddingLeft()) - this.mLayoutManager.getPaddingRight();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getEndPadding() {
                return this.mLayoutManager.getPaddingRight();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getMode() {
                return this.mLayoutManager.getWidthMode();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getModeInOther() {
                return this.mLayoutManager.getHeightMode();
            }
        };
    }

    public static OrientationHelper createVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) { // from class: android.support.v7.widget.OrientationHelper.2
            @Override // android.support.v7.widget.OrientationHelper
            public int getEndAfterPadding() {
                return this.mLayoutManager.getHeight() - this.mLayoutManager.getPaddingBottom();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getEnd() {
                return this.mLayoutManager.getHeight();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public void offsetChildren(int amount) {
                this.mLayoutManager.offsetChildrenVertical(amount);
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getStartAfterPadding() {
                return this.mLayoutManager.getPaddingTop();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedMeasurement(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedMeasurementInOther(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedEnd(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedBottom(view) + params.bottomMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getDecoratedStart(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return this.mLayoutManager.getDecoratedTop(view) - params.topMargin;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getTransformedEndWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.bottom;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getTransformedStartWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.top;
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getTotalSpace() {
                return (this.mLayoutManager.getHeight() - this.mLayoutManager.getPaddingTop()) - this.mLayoutManager.getPaddingBottom();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getEndPadding() {
                return this.mLayoutManager.getPaddingBottom();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getMode() {
                return this.mLayoutManager.getHeightMode();
            }

            @Override // android.support.v7.widget.OrientationHelper
            public int getModeInOther() {
                return this.mLayoutManager.getWidthMode();
            }
        };
    }
}
