package android.support.v17.leanback.widget;

import android.support.v4.util.CircularIntArray;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/Grid.class */
public abstract class Grid {
    protected int mMargin;
    protected int mNumRows;
    protected Provider mProvider;
    protected boolean mReversedFlow;
    protected CircularIntArray[] mTmpItemPositionsInRows;
    protected int mFirstVisibleIndex = -1;
    protected int mLastVisibleIndex = -1;
    protected int mStartIndex = -1;

    /* loaded from: a.zip:android/support/v17/leanback/widget/Grid$Location.class */
    public static class Location {
        public int row;

        public Location(int i) {
            this.row = i;
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/Grid$Provider.class */
    public interface Provider {
        void addItem(Object obj, int i, int i2, int i3, int i4);

        int createItem(int i, boolean z, Object[] objArr);

        int getCount();

        int getEdge(int i);

        int getSize(int i);

        void removeItem(int i);
    }

    public static Grid createGrid(int i) {
        Grid staggeredGridDefault;
        if (i == 1) {
            staggeredGridDefault = new SingleRow();
        } else {
            staggeredGridDefault = new StaggeredGridDefault();
            staggeredGridDefault.setNumRows(i);
        }
        return staggeredGridDefault;
    }

    private void resetVisbileIndexIfEmpty() {
        if (this.mLastVisibleIndex < this.mFirstVisibleIndex) {
            resetVisibleIndex();
        }
    }

    public boolean appendOneColumnVisibleItems() {
        return appendVisibleItems(this.mReversedFlow ? Integer.MAX_VALUE : Integer.MIN_VALUE, true);
    }

    public final void appendVisibleItems(int i) {
        appendVisibleItems(i, false);
    }

    protected abstract boolean appendVisibleItems(int i, boolean z);

    /* JADX INFO: Access modifiers changed from: protected */
    public final boolean checkAppendOverLimit(int i) {
        boolean z = true;
        if (this.mLastVisibleIndex < 0) {
            return false;
        }
        if (this.mReversedFlow) {
            if (findRowMin(true, null) > this.mMargin + i) {
                z = false;
            }
        } else if (findRowMax(false, null) < i - this.mMargin) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final boolean checkPrependOverLimit(int i) {
        boolean z = true;
        if (this.mLastVisibleIndex < 0) {
            return false;
        }
        if (this.mReversedFlow) {
            if (findRowMax(false, null) < this.mMargin + i) {
                z = false;
            }
        } else if (findRowMin(true, null) > i - this.mMargin) {
            z = false;
        }
        return z;
    }

    protected abstract int findRowMax(boolean z, int i, int[] iArr);

    public final int findRowMax(boolean z, int[] iArr) {
        return findRowMax(z, this.mReversedFlow ? this.mFirstVisibleIndex : this.mLastVisibleIndex, iArr);
    }

    protected abstract int findRowMin(boolean z, int i, int[] iArr);

    public final int findRowMin(boolean z, int[] iArr) {
        return findRowMin(z, this.mReversedFlow ? this.mLastVisibleIndex : this.mFirstVisibleIndex, iArr);
    }

    public final int getFirstVisibleIndex() {
        return this.mFirstVisibleIndex;
    }

    public final CircularIntArray[] getItemPositionsInRows() {
        return getItemPositionsInRows(getFirstVisibleIndex(), getLastVisibleIndex());
    }

    public abstract CircularIntArray[] getItemPositionsInRows(int i, int i2);

    public final int getLastVisibleIndex() {
        return this.mLastVisibleIndex;
    }

    public abstract Location getLocation(int i);

    public int getNumRows() {
        return this.mNumRows;
    }

    public final int getRowIndex(int i) {
        return getLocation(i).row;
    }

    public void invalidateItemsAfter(int i) {
        if (i >= 0 && this.mLastVisibleIndex >= 0) {
            while (this.mLastVisibleIndex >= i) {
                this.mProvider.removeItem(this.mLastVisibleIndex);
                this.mLastVisibleIndex--;
            }
            resetVisbileIndexIfEmpty();
            if (getFirstVisibleIndex() < 0) {
                setStart(i);
            }
        }
    }

    public boolean isReversedFlow() {
        return this.mReversedFlow;
    }

    public final boolean prependOneColumnVisibleItems() {
        return prependVisibleItems(this.mReversedFlow ? Integer.MIN_VALUE : Integer.MAX_VALUE, true);
    }

    public final void prependVisibleItems(int i) {
        prependVisibleItems(i, false);
    }

    protected abstract boolean prependVisibleItems(int i, boolean z);

    public void removeInvisibleItemsAtEnd(int i, int i2) {
        while (this.mLastVisibleIndex >= this.mFirstVisibleIndex && this.mLastVisibleIndex > i) {
            if (!(!this.mReversedFlow ? this.mProvider.getEdge(this.mLastVisibleIndex) >= i2 : this.mProvider.getEdge(this.mLastVisibleIndex) <= i2)) {
                break;
            }
            this.mProvider.removeItem(this.mLastVisibleIndex);
            this.mLastVisibleIndex--;
        }
        resetVisbileIndexIfEmpty();
    }

    public void removeInvisibleItemsAtFront(int i, int i2) {
        while (this.mLastVisibleIndex >= this.mFirstVisibleIndex && this.mFirstVisibleIndex < i) {
            if (!(!this.mReversedFlow ? this.mProvider.getEdge(this.mFirstVisibleIndex) + this.mProvider.getSize(this.mFirstVisibleIndex) <= i2 : this.mProvider.getEdge(this.mFirstVisibleIndex) - this.mProvider.getSize(this.mFirstVisibleIndex) >= i2)) {
                break;
            }
            this.mProvider.removeItem(this.mFirstVisibleIndex);
            this.mFirstVisibleIndex++;
        }
        resetVisbileIndexIfEmpty();
    }

    public void resetVisibleIndex() {
        this.mLastVisibleIndex = -1;
        this.mFirstVisibleIndex = -1;
    }

    public final void setMargin(int i) {
        this.mMargin = i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setNumRows(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException();
        }
        if (this.mNumRows == i) {
            return;
        }
        this.mNumRows = i;
        this.mTmpItemPositionsInRows = new CircularIntArray[this.mNumRows];
        for (int i2 = 0; i2 < this.mNumRows; i2++) {
            this.mTmpItemPositionsInRows[i2] = new CircularIntArray();
        }
    }

    public void setProvider(Provider provider) {
        this.mProvider = provider;
    }

    public final void setReversedFlow(boolean z) {
        this.mReversedFlow = z;
    }

    public void setStart(int i) {
        this.mStartIndex = i;
    }
}
