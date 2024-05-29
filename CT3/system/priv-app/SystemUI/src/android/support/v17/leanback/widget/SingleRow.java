package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.Grid;
import android.support.v4.util.CircularIntArray;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/SingleRow.class */
public class SingleRow extends Grid {
    private final Grid.Location mTmpLocation = new Grid.Location(0);
    private Object[] mTmpItem = new Object[1];

    /* JADX INFO: Access modifiers changed from: package-private */
    public SingleRow() {
        setNumRows(1);
    }

    @Override // android.support.v17.leanback.widget.Grid
    protected final boolean appendVisibleItems(int i, boolean z) {
        int i2;
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (z || !checkAppendOverLimit(i)) {
            boolean z2 = false;
            int startIndexForAppend = getStartIndexForAppend();
            while (true) {
                if (startIndexForAppend >= this.mProvider.getCount()) {
                    break;
                }
                int createItem = this.mProvider.createItem(startIndexForAppend, true, this.mTmpItem);
                if (this.mFirstVisibleIndex < 0 || this.mLastVisibleIndex < 0) {
                    i2 = this.mReversedFlow ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                    this.mFirstVisibleIndex = startIndexForAppend;
                    this.mLastVisibleIndex = startIndexForAppend;
                } else {
                    i2 = this.mReversedFlow ? (this.mProvider.getEdge(startIndexForAppend - 1) - this.mProvider.getSize(startIndexForAppend - 1)) - this.mMargin : this.mProvider.getEdge(startIndexForAppend - 1) + this.mProvider.getSize(startIndexForAppend - 1) + this.mMargin;
                    this.mLastVisibleIndex = startIndexForAppend;
                }
                this.mProvider.addItem(this.mTmpItem[0], startIndexForAppend, createItem, 0, i2);
                z2 = true;
                if (z) {
                    break;
                } else if (checkAppendOverLimit(i)) {
                    z2 = true;
                    break;
                } else {
                    startIndexForAppend++;
                    z2 = true;
                }
            }
            return z2;
        }
        return false;
    }

    @Override // android.support.v17.leanback.widget.Grid
    protected final int findRowMax(boolean z, int i, int[] iArr) {
        if (iArr != null) {
            iArr[0] = 0;
            iArr[1] = i;
        }
        return this.mReversedFlow ? this.mProvider.getEdge(i) : this.mProvider.getEdge(i) + this.mProvider.getSize(i);
    }

    @Override // android.support.v17.leanback.widget.Grid
    protected final int findRowMin(boolean z, int i, int[] iArr) {
        if (iArr != null) {
            iArr[0] = 0;
            iArr[1] = i;
        }
        return this.mReversedFlow ? this.mProvider.getEdge(i) - this.mProvider.getSize(i) : this.mProvider.getEdge(i);
    }

    @Override // android.support.v17.leanback.widget.Grid
    public final CircularIntArray[] getItemPositionsInRows(int i, int i2) {
        this.mTmpItemPositionsInRows[0].clear();
        this.mTmpItemPositionsInRows[0].addLast(i);
        this.mTmpItemPositionsInRows[0].addLast(i2);
        return this.mTmpItemPositionsInRows;
    }

    @Override // android.support.v17.leanback.widget.Grid
    public final Grid.Location getLocation(int i) {
        return this.mTmpLocation;
    }

    int getStartIndexForAppend() {
        if (this.mLastVisibleIndex >= 0) {
            return this.mLastVisibleIndex + 1;
        }
        if (this.mStartIndex != -1) {
            return Math.min(this.mStartIndex, this.mProvider.getCount() - 1);
        }
        return 0;
    }

    int getStartIndexForPrepend() {
        return this.mFirstVisibleIndex >= 0 ? this.mFirstVisibleIndex - 1 : this.mStartIndex != -1 ? Math.min(this.mStartIndex, this.mProvider.getCount() - 1) : this.mProvider.getCount() - 1;
    }

    @Override // android.support.v17.leanback.widget.Grid
    protected final boolean prependVisibleItems(int i, boolean z) {
        int i2;
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (z || !checkPrependOverLimit(i)) {
            boolean z2 = false;
            int startIndexForPrepend = getStartIndexForPrepend();
            while (true) {
                if (startIndexForPrepend < 0) {
                    break;
                }
                int createItem = this.mProvider.createItem(startIndexForPrepend, false, this.mTmpItem);
                if (this.mFirstVisibleIndex < 0 || this.mLastVisibleIndex < 0) {
                    i2 = this.mReversedFlow ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                    this.mFirstVisibleIndex = startIndexForPrepend;
                    this.mLastVisibleIndex = startIndexForPrepend;
                } else {
                    i2 = this.mReversedFlow ? this.mProvider.getEdge(startIndexForPrepend + 1) + this.mMargin + createItem : (this.mProvider.getEdge(startIndexForPrepend + 1) - this.mMargin) - createItem;
                    this.mFirstVisibleIndex = startIndexForPrepend;
                }
                this.mProvider.addItem(this.mTmpItem[0], startIndexForPrepend, createItem, 0, i2);
                z2 = true;
                if (z) {
                    break;
                } else if (checkPrependOverLimit(i)) {
                    z2 = true;
                    break;
                } else {
                    startIndexForPrepend--;
                    z2 = true;
                }
            }
            return z2;
        }
        return false;
    }
}
