package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.Grid;
import android.support.v4.util.CircularArray;
import android.support.v4.util.CircularIntArray;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/StaggeredGrid.class */
public abstract class StaggeredGrid extends Grid {
    protected Object mPendingItem;
    protected int mPendingItemSize;
    protected CircularArray<Location> mLocations = new CircularArray<>(64);
    protected int mFirstIndex = -1;
    private Object[] mTmpItem = new Object[1];

    /* loaded from: a.zip:android/support/v17/leanback/widget/StaggeredGrid$Location.class */
    public static class Location extends Grid.Location {
        public int offset;
        public int size;

        public Location(int i, int i2, int i3) {
            super(i);
            this.offset = i2;
            this.size = i3;
        }
    }

    private int calculateOffsetAfterLastItem(int i) {
        boolean z;
        int lastIndex = getLastIndex();
        while (true) {
            z = false;
            if (lastIndex < this.mFirstIndex) {
                break;
            } else if (getLocation(lastIndex).row == i) {
                z = true;
                break;
            } else {
                lastIndex--;
            }
        }
        if (!z) {
            lastIndex = getLastIndex();
        }
        int i2 = lastIndex + 1;
        int i3 = isReversedFlow() ? (-getLocation(lastIndex).size) - this.mMargin : getLocation(lastIndex).size + this.mMargin;
        for (int i4 = i2; i4 <= getLastIndex(); i4++) {
            i3 -= getLocation(i4).offset;
        }
        return i3;
    }

    protected final boolean appendVisbleItemsWithCache(int i, boolean z) {
        int i2;
        int i3;
        if (this.mLocations.size() == 0) {
            return false;
        }
        int count = this.mProvider.getCount();
        if (this.mLastVisibleIndex >= 0) {
            i3 = this.mLastVisibleIndex + 1;
            i2 = this.mProvider.getEdge(this.mLastVisibleIndex);
        } else {
            i2 = Integer.MAX_VALUE;
            int i4 = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (i4 > getLastIndex() + 1 || i4 < getFirstIndex()) {
                this.mLocations.clear();
                return false;
            }
            i3 = i4;
            if (i4 > getLastIndex()) {
                return false;
            }
        }
        int lastIndex = getLastIndex();
        while (true) {
            int i5 = lastIndex;
            if (i3 >= count || i3 > i5) {
                return false;
            }
            Location location = getLocation(i3);
            int i6 = i2;
            if (i2 != Integer.MAX_VALUE) {
                i6 = i2 + location.offset;
            }
            int i7 = location.row;
            int createItem = this.mProvider.createItem(i3, true, this.mTmpItem);
            int i8 = i5;
            if (createItem != location.size) {
                location.size = createItem;
                this.mLocations.removeFromEnd(i5 - i3);
                i8 = i3;
            }
            this.mLastVisibleIndex = i3;
            if (this.mFirstVisibleIndex < 0) {
                this.mFirstVisibleIndex = i3;
            }
            this.mProvider.addItem(this.mTmpItem[0], i3, createItem, i7, i6);
            if (!z && checkAppendOverLimit(i)) {
                return true;
            }
            i2 = i6;
            if (i6 == Integer.MAX_VALUE) {
                i2 = this.mProvider.getEdge(i3);
            }
            if (i7 == this.mNumRows - 1 && z) {
                return true;
            }
            i3++;
            lastIndex = i8;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final int appendVisibleItemToRow(int i, int i2, int i3) {
        Object obj;
        if (this.mLastVisibleIndex < 0 || (this.mLastVisibleIndex == getLastIndex() && this.mLastVisibleIndex == i - 1)) {
            Location location = new Location(i2, this.mLastVisibleIndex < 0 ? (this.mLocations.size() <= 0 || i != getLastIndex() + 1) ? 0 : calculateOffsetAfterLastItem(i2) : i3 - this.mProvider.getEdge(this.mLastVisibleIndex), 0);
            this.mLocations.addLast(location);
            if (this.mPendingItem != null) {
                location.size = this.mPendingItemSize;
                obj = this.mPendingItem;
                this.mPendingItem = null;
            } else {
                location.size = this.mProvider.createItem(i, true, this.mTmpItem);
                obj = this.mTmpItem[0];
            }
            if (this.mLocations.size() == 1) {
                this.mLastVisibleIndex = i;
                this.mFirstVisibleIndex = i;
                this.mFirstIndex = i;
            } else if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = i;
                this.mFirstVisibleIndex = i;
            } else {
                this.mLastVisibleIndex++;
            }
            this.mProvider.addItem(obj, i, location.size, i2, i3);
            return location.size;
        }
        throw new IllegalStateException();
    }

    @Override // android.support.v17.leanback.widget.Grid
    protected final boolean appendVisibleItems(int i, boolean z) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (z || !checkAppendOverLimit(i)) {
            try {
                if (appendVisbleItemsWithCache(i, z)) {
                    this.mTmpItem[0] = null;
                    this.mPendingItem = null;
                    return true;
                }
                return appendVisibleItemsWithoutCache(i, z);
            } finally {
                this.mTmpItem[0] = null;
                this.mPendingItem = null;
            }
        }
        return false;
    }

    protected abstract boolean appendVisibleItemsWithoutCache(int i, boolean z);

    public final int getFirstIndex() {
        return this.mFirstIndex;
    }

    @Override // android.support.v17.leanback.widget.Grid
    public final CircularIntArray[] getItemPositionsInRows(int i, int i2) {
        for (int i3 = 0; i3 < this.mNumRows; i3++) {
            this.mTmpItemPositionsInRows[i3].clear();
        }
        if (i >= 0) {
            while (i <= i2) {
                CircularIntArray circularIntArray = this.mTmpItemPositionsInRows[getLocation(i).row];
                if (circularIntArray.size() <= 0 || circularIntArray.getLast() != i - 1) {
                    circularIntArray.addLast(i);
                    circularIntArray.addLast(i);
                } else {
                    circularIntArray.popLast();
                    circularIntArray.addLast(i);
                }
                i++;
            }
        }
        return this.mTmpItemPositionsInRows;
    }

    public final int getLastIndex() {
        return (this.mFirstIndex + this.mLocations.size()) - 1;
    }

    @Override // android.support.v17.leanback.widget.Grid
    public final Location getLocation(int i) {
        if (this.mLocations.size() == 0) {
            return null;
        }
        return this.mLocations.get(i - this.mFirstIndex);
    }

    @Override // android.support.v17.leanback.widget.Grid
    public void invalidateItemsAfter(int i) {
        super.invalidateItemsAfter(i);
        this.mLocations.removeFromEnd((getLastIndex() - i) + 1);
        if (this.mLocations.size() == 0) {
            this.mFirstIndex = -1;
        }
    }

    protected final boolean prependVisbleItemsWithCache(int i, boolean z) {
        int i2;
        int i3;
        int i4;
        if (this.mLocations.size() == 0) {
            return false;
        }
        this.mProvider.getCount();
        getFirstIndex();
        if (this.mFirstVisibleIndex >= 0) {
            i2 = this.mProvider.getEdge(this.mFirstVisibleIndex);
            i3 = getLocation(this.mFirstVisibleIndex).offset;
            i4 = this.mFirstVisibleIndex - 1;
        } else {
            i2 = Integer.MAX_VALUE;
            i3 = 0;
            int i5 = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (i5 > getLastIndex() || i5 < getFirstIndex() - 1) {
                this.mLocations.clear();
                return false;
            }
            i4 = i5;
            if (i5 < getFirstIndex()) {
                return false;
            }
        }
        while (i4 >= this.mFirstIndex) {
            Location location = getLocation(i4);
            int i6 = location.row;
            int createItem = this.mProvider.createItem(i4, false, this.mTmpItem);
            if (createItem != location.size) {
                this.mLocations.removeFromStart((i4 + 1) - this.mFirstIndex);
                this.mFirstIndex = this.mFirstVisibleIndex;
                this.mPendingItem = this.mTmpItem[0];
                this.mPendingItemSize = createItem;
                return false;
            }
            this.mFirstVisibleIndex = i4;
            if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = i4;
            }
            this.mProvider.addItem(this.mTmpItem[0], i4, createItem, i6, i2 - i3);
            if (!z && checkPrependOverLimit(i)) {
                return true;
            }
            i2 = this.mProvider.getEdge(i4);
            i3 = location.offset;
            if (i6 == 0 && z) {
                return true;
            }
            i4--;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final int prependVisibleItemToRow(int i, int i2, int i3) {
        Object obj;
        if (this.mFirstVisibleIndex < 0 || (this.mFirstVisibleIndex == getFirstIndex() && this.mFirstVisibleIndex == i + 1)) {
            Location location = this.mFirstIndex >= 0 ? getLocation(this.mFirstIndex) : null;
            int edge = this.mProvider.getEdge(this.mFirstIndex);
            Location location2 = new Location(i2, 0, 0);
            this.mLocations.addFirst(location2);
            if (this.mPendingItem != null) {
                location2.size = this.mPendingItemSize;
                obj = this.mPendingItem;
                this.mPendingItem = null;
            } else {
                location2.size = this.mProvider.createItem(i, false, this.mTmpItem);
                obj = this.mTmpItem[0];
            }
            this.mFirstVisibleIndex = i;
            this.mFirstIndex = i;
            if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = i;
            }
            int i4 = !this.mReversedFlow ? i3 - location2.size : i3 + location2.size;
            if (location != null) {
                location.offset = edge - i4;
            }
            this.mProvider.addItem(obj, i, location2.size, i2, i4);
            return location2.size;
        }
        throw new IllegalStateException();
    }

    @Override // android.support.v17.leanback.widget.Grid
    protected final boolean prependVisibleItems(int i, boolean z) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (z || !checkPrependOverLimit(i)) {
            try {
                if (prependVisbleItemsWithCache(i, z)) {
                    this.mTmpItem[0] = null;
                    this.mPendingItem = null;
                    return true;
                }
                return prependVisibleItemsWithoutCache(i, z);
            } finally {
                this.mTmpItem[0] = null;
                this.mPendingItem = null;
            }
        }
        return false;
    }

    protected abstract boolean prependVisibleItemsWithoutCache(int i, boolean z);
}
