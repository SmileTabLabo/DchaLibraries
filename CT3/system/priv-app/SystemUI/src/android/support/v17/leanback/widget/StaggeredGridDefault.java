package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.StaggeredGrid;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/StaggeredGridDefault.class */
public final class StaggeredGridDefault extends StaggeredGrid {
    private int findRowEdgeLimitSearchIndex(boolean z) {
        boolean z2;
        boolean z3;
        boolean z4 = false;
        boolean z5 = false;
        if (z) {
            int i = this.mLastVisibleIndex;
            while (i >= this.mFirstVisibleIndex) {
                int i2 = getLocation(i).row;
                if (i2 == 0) {
                    z3 = true;
                } else {
                    z3 = z5;
                    if (z5) {
                        z3 = z5;
                        if (i2 == this.mNumRows - 1) {
                            return i;
                        }
                    } else {
                        continue;
                    }
                }
                i--;
                z5 = z3;
            }
            return -1;
        }
        int i3 = this.mFirstVisibleIndex;
        while (i3 <= this.mLastVisibleIndex) {
            int i4 = getLocation(i3).row;
            if (i4 == this.mNumRows - 1) {
                z2 = true;
            } else {
                z2 = z4;
                if (z4) {
                    z2 = z4;
                    if (i4 == 0) {
                        return i3;
                    }
                } else {
                    continue;
                }
            }
            i3++;
            z4 = z2;
        }
        return -1;
    }

    /* JADX WARN: Code restructure failed: missing block: B:21:0x007c, code lost:
        if (getRowMin(r0) <= r11) goto L23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x007f, code lost:
        r10 = r0 + 1;
        r12 = r11;
        r13 = r10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x0090, code lost:
        if (r10 != r5.mNumRows) goto L29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x0093, code lost:
        r13 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x009a, code lost:
        if (r5.mReversedFlow == false) goto L28;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x009d, code lost:
        r12 = findRowMin(false, null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x00a5, code lost:
        r10 = r13;
        r11 = r0;
        r9 = true;
        r13 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x0127, code lost:
        if (getRowMax(r0) >= r11) goto L23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x012d, code lost:
        r12 = findRowMax(true, null);
     */
    /* JADX WARN: Removed duplicated region for block: B:129:0x022f A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:87:0x0223  */
    @Override // android.support.v17.leanback.widget.StaggeredGrid
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected boolean appendVisibleItemsWithoutCache(int i, boolean z) {
        int i2;
        int i3;
        int i4;
        boolean z2;
        int rowMax;
        boolean z3;
        int i5;
        int rowMax2;
        int findRowMin;
        int i6;
        int i7;
        int count = this.mProvider.getCount();
        if (this.mLastVisibleIndex < 0) {
            i2 = this.mStartIndex != -1 ? this.mStartIndex : 0;
            i3 = (this.mLocations.size() > 0 ? getLocation(getLastIndex()).row + 1 : i2) % this.mNumRows;
            i4 = 0;
            z2 = false;
        } else if (this.mLastVisibleIndex < getLastIndex()) {
            return false;
        } else {
            int i8 = this.mLastVisibleIndex + 1;
            int i9 = getLocation(this.mLastVisibleIndex).row;
            int findRowEdgeLimitSearchIndex = findRowEdgeLimitSearchIndex(true);
            if (findRowEdgeLimitSearchIndex < 0) {
                findRowMin = Integer.MIN_VALUE;
                for (int i10 = 0; i10 < this.mNumRows; i10++) {
                    findRowMin = this.mReversedFlow ? getRowMin(i10) : getRowMax(i10);
                    if (findRowMin != Integer.MIN_VALUE) {
                        break;
                    }
                }
            } else {
                findRowMin = this.mReversedFlow ? findRowMin(false, findRowEdgeLimitSearchIndex, null) : findRowMax(true, findRowEdgeLimitSearchIndex, null);
            }
            if (this.mReversedFlow) {
                i6 = findRowMin;
                i7 = i9;
            } else {
                i6 = findRowMin;
                i7 = i9;
            }
        }
        boolean z4 = false;
        int i11 = i3;
        int i12 = i2;
        boolean z5 = z2;
        while (true) {
            if (i11 < this.mNumRows) {
                if (i12 == count || (!z && checkAppendOverLimit(i))) {
                    break;
                }
                int rowMin = this.mReversedFlow ? getRowMin(i11) : getRowMax(i11);
                if (rowMin != Integer.MAX_VALUE && rowMin != Integer.MIN_VALUE) {
                    rowMax = rowMin + (this.mReversedFlow ? -this.mMargin : this.mMargin);
                } else if (i11 == 0) {
                    int rowMin2 = this.mReversedFlow ? getRowMin(this.mNumRows - 1) : getRowMax(this.mNumRows - 1);
                    rowMax = rowMin2;
                    if (rowMin2 != Integer.MAX_VALUE) {
                        rowMax = rowMin2;
                        if (rowMin2 != Integer.MIN_VALUE) {
                            rowMax = rowMin2 + (this.mReversedFlow ? -this.mMargin : this.mMargin);
                        }
                    }
                } else {
                    rowMax = this.mReversedFlow ? getRowMax(i11 - 1) : getRowMin(i11 - 1);
                }
                int i13 = i12 + 1;
                int appendVisibleItemToRow = appendVisibleItemToRow(i12, i11, rowMax);
                z4 = true;
                if (z5) {
                    int i14 = rowMax;
                    while (true) {
                        int i15 = i13;
                        if (this.mReversedFlow) {
                            rowMax2 = i4;
                            z3 = z5;
                            i5 = i15;
                            if (i14 - appendVisibleItemToRow <= i4) {
                                break;
                            }
                            if (i15 != count) {
                                return true;
                            }
                            if (!z && checkAppendOverLimit(i)) {
                                return true;
                            }
                            i14 += this.mReversedFlow ? (-appendVisibleItemToRow) - this.mMargin : this.mMargin + appendVisibleItemToRow;
                            i13 = i15 + 1;
                            appendVisibleItemToRow = appendVisibleItemToRow(i15, i11, i14);
                        } else {
                            if (i14 + appendVisibleItemToRow >= i4) {
                                i5 = i15;
                                z3 = z5;
                                rowMax2 = i4;
                                break;
                            }
                            if (i15 != count) {
                            }
                        }
                    }
                } else {
                    z3 = true;
                    if (this.mReversedFlow) {
                        i5 = i13;
                        rowMax2 = getRowMin(i11);
                    } else {
                        i5 = i13;
                        rowMax2 = getRowMax(i11);
                    }
                }
                i11++;
                i4 = rowMax2;
                z5 = z3;
                i12 = i5;
            } else if (z) {
                return z4;
            } else {
                i4 = this.mReversedFlow ? findRowMin(false, null) : findRowMax(true, null);
                i11 = 0;
            }
        }
        return z4;
    }

    @Override // android.support.v17.leanback.widget.Grid
    public int findRowMax(boolean z, int i, int[] iArr) {
        int i2;
        int i3;
        int i4;
        int edge = this.mProvider.getEdge(i);
        StaggeredGrid.Location location = getLocation(i);
        int i5 = location.row;
        int i6 = i;
        int i7 = 1;
        if (!this.mReversedFlow) {
            int size = edge + this.mProvider.getSize(i);
            int i8 = i - 1;
            int i9 = 1;
            int i10 = i5;
            int i11 = size;
            int i12 = i8;
            int i13 = edge;
            while (true) {
                i2 = i6;
                i3 = i5;
                i4 = i11;
                if (i9 >= this.mNumRows) {
                    break;
                }
                i2 = i6;
                i3 = i5;
                i4 = i11;
                if (i12 < this.mFirstVisibleIndex) {
                    break;
                }
                i13 -= location.offset;
                location = getLocation(i12);
                int i14 = i6;
                int i15 = i5;
                int i16 = i11;
                int i17 = i10;
                int i18 = i9;
                if (location.row != i10) {
                    int i19 = location.row;
                    int i20 = i9 + 1;
                    int size2 = i13 + this.mProvider.getSize(i12);
                    if (z) {
                        i14 = i6;
                        i15 = i5;
                        i16 = i11;
                        i17 = i19;
                        i18 = i20;
                        if (size2 <= i11) {
                        }
                        i15 = i19;
                        i16 = size2;
                        i14 = i12;
                        i18 = i20;
                        i17 = i19;
                    } else {
                        i14 = i6;
                        i15 = i5;
                        i16 = i11;
                        i17 = i19;
                        i18 = i20;
                        if (size2 >= i11) {
                        }
                        i15 = i19;
                        i16 = size2;
                        i14 = i12;
                        i18 = i20;
                        i17 = i19;
                    }
                }
                i12--;
                i6 = i14;
                i5 = i15;
                i11 = i16;
                i10 = i17;
                i9 = i18;
            }
        } else {
            int i21 = i + 1;
            int i22 = i5;
            int i23 = edge;
            int i24 = i21;
            int i25 = edge;
            while (true) {
                i2 = i6;
                i3 = i5;
                i4 = i23;
                if (i7 >= this.mNumRows) {
                    break;
                }
                i2 = i6;
                i3 = i5;
                i4 = i23;
                if (i24 > this.mLastVisibleIndex) {
                    break;
                }
                StaggeredGrid.Location location2 = getLocation(i24);
                i25 += location2.offset;
                int i26 = i6;
                int i27 = i5;
                int i28 = i23;
                int i29 = i22;
                int i30 = i7;
                if (location2.row != i22) {
                    int i31 = location2.row;
                    int i32 = i7 + 1;
                    if (z) {
                        i26 = i6;
                        i27 = i5;
                        i28 = i23;
                        i29 = i31;
                        i30 = i32;
                        if (i25 <= i23) {
                        }
                        i27 = i31;
                        i28 = i25;
                        i26 = i24;
                        i30 = i32;
                        i29 = i31;
                    } else {
                        i26 = i6;
                        i27 = i5;
                        i28 = i23;
                        i29 = i31;
                        i30 = i32;
                        if (i25 >= i23) {
                        }
                        i27 = i31;
                        i28 = i25;
                        i26 = i24;
                        i30 = i32;
                        i29 = i31;
                    }
                }
                i24++;
                i6 = i26;
                i5 = i27;
                i23 = i28;
                i22 = i29;
                i7 = i30;
            }
        }
        if (iArr != null) {
            iArr[0] = i3;
            iArr[1] = i2;
        }
        return i4;
    }

    @Override // android.support.v17.leanback.widget.Grid
    public int findRowMin(boolean z, int i, int[] iArr) {
        int i2;
        int i3;
        int i4;
        int edge = this.mProvider.getEdge(i);
        StaggeredGrid.Location location = getLocation(i);
        int i5 = location.row;
        int i6 = i;
        int i7 = 1;
        if (!this.mReversedFlow) {
            int i8 = i + 1;
            int i9 = 1;
            int i10 = i5;
            int i11 = edge;
            int i12 = edge;
            while (true) {
                i2 = i6;
                i3 = i5;
                i4 = i11;
                if (i9 >= this.mNumRows) {
                    break;
                }
                i2 = i6;
                i3 = i5;
                i4 = i11;
                if (i8 > this.mLastVisibleIndex) {
                    break;
                }
                StaggeredGrid.Location location2 = getLocation(i8);
                int i13 = i12 + location2.offset;
                int i14 = i6;
                int i15 = i5;
                int i16 = i11;
                int i17 = i10;
                int i18 = i9;
                if (location2.row != i10) {
                    int i19 = location2.row;
                    int i20 = i9 + 1;
                    if (z) {
                        i14 = i6;
                        i15 = i5;
                        i16 = i11;
                        i17 = i19;
                        i18 = i20;
                        if (i13 <= i11) {
                        }
                        i16 = i13;
                        i15 = i19;
                        i14 = i8;
                        i18 = i20;
                        i17 = i19;
                    } else {
                        i14 = i6;
                        i15 = i5;
                        i16 = i11;
                        i17 = i19;
                        i18 = i20;
                        if (i13 >= i11) {
                        }
                        i16 = i13;
                        i15 = i19;
                        i14 = i8;
                        i18 = i20;
                        i17 = i19;
                    }
                }
                i8++;
                i12 = i13;
                i6 = i14;
                i5 = i15;
                i11 = i16;
                i10 = i17;
                i9 = i18;
            }
        } else {
            int i21 = i - 1;
            int i22 = i5;
            int size = edge - this.mProvider.getSize(i);
            int i23 = edge;
            while (true) {
                i2 = i6;
                i3 = i5;
                i4 = size;
                if (i7 >= this.mNumRows) {
                    break;
                }
                i2 = i6;
                i3 = i5;
                i4 = size;
                if (i21 < this.mFirstVisibleIndex) {
                    break;
                }
                i23 -= location.offset;
                location = getLocation(i21);
                int i24 = i6;
                int i25 = i5;
                int i26 = size;
                int i27 = i22;
                int i28 = i7;
                if (location.row != i22) {
                    int i29 = location.row;
                    int i30 = i7 + 1;
                    int size2 = i23 - this.mProvider.getSize(i21);
                    if (z) {
                        i24 = i6;
                        i25 = i5;
                        i26 = size;
                        i27 = i29;
                        i28 = i30;
                        if (size2 <= size) {
                        }
                        i26 = size2;
                        i25 = i29;
                        i24 = i21;
                        i28 = i30;
                        i27 = i29;
                    } else {
                        i24 = i6;
                        i25 = i5;
                        i26 = size;
                        i27 = i29;
                        i28 = i30;
                        if (size2 >= size) {
                        }
                        i26 = size2;
                        i25 = i29;
                        i24 = i21;
                        i28 = i30;
                        i27 = i29;
                    }
                }
                i21--;
                i6 = i24;
                i5 = i25;
                size = i26;
                i22 = i27;
                i7 = i28;
            }
        }
        if (iArr != null) {
            iArr[0] = i3;
            iArr[1] = i2;
        }
        return i4;
    }

    int getRowMax(int i) {
        if (this.mFirstVisibleIndex < 0) {
            return Integer.MIN_VALUE;
        }
        if (this.mReversedFlow) {
            int edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
            if (getLocation(this.mFirstVisibleIndex).row == i) {
                return edge;
            }
            for (int i2 = this.mFirstVisibleIndex + 1; i2 <= getLastIndex(); i2++) {
                StaggeredGrid.Location location = getLocation(i2);
                edge += location.offset;
                if (location.row == i) {
                    return edge;
                }
            }
            return Integer.MIN_VALUE;
        }
        int edge2 = this.mProvider.getEdge(this.mLastVisibleIndex);
        StaggeredGrid.Location location2 = getLocation(this.mLastVisibleIndex);
        if (location2.row == i) {
            return location2.size + edge2;
        }
        for (int i3 = this.mLastVisibleIndex - 1; i3 >= getFirstIndex(); i3--) {
            edge2 -= location2.offset;
            location2 = getLocation(i3);
            if (location2.row == i) {
                return location2.size + edge2;
            }
        }
        return Integer.MIN_VALUE;
    }

    int getRowMin(int i) {
        if (this.mFirstVisibleIndex < 0) {
            return Integer.MAX_VALUE;
        }
        if (!this.mReversedFlow) {
            int edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
            if (getLocation(this.mFirstVisibleIndex).row == i) {
                return edge;
            }
            for (int i2 = this.mFirstVisibleIndex + 1; i2 <= getLastIndex(); i2++) {
                StaggeredGrid.Location location = getLocation(i2);
                edge += location.offset;
                if (location.row == i) {
                    return edge;
                }
            }
            return Integer.MAX_VALUE;
        }
        int edge2 = this.mProvider.getEdge(this.mLastVisibleIndex);
        StaggeredGrid.Location location2 = getLocation(this.mLastVisibleIndex);
        if (location2.row == i) {
            return edge2 - location2.size;
        }
        for (int i3 = this.mLastVisibleIndex - 1; i3 >= getFirstIndex(); i3--) {
            edge2 -= location2.offset;
            location2 = getLocation(i3);
            if (location2.row == i) {
                return edge2 - location2.size;
            }
        }
        return Integer.MAX_VALUE;
    }

    /* JADX WARN: Code restructure failed: missing block: B:22:0x0080, code lost:
        if (getRowMax(r9) >= r10) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x0083, code lost:
        r9 = r9 - 1;
        r12 = r10;
        r11 = r9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x0090, code lost:
        if (r9 >= 0) goto L30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x0093, code lost:
        r11 = r5.mNumRows - 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x009f, code lost:
        if (r5.mReversedFlow == false) goto L29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x00a2, code lost:
        r12 = findRowMax(true, null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x00aa, code lost:
        r9 = true;
        r10 = r11;
        r11 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x011a, code lost:
        if (getRowMin(r9) <= r10) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:49:0x0120, code lost:
        r12 = findRowMin(false, null);
     */
    /* JADX WARN: Removed duplicated region for block: B:130:0x022a A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:88:0x021e  */
    @Override // android.support.v17.leanback.widget.StaggeredGrid
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected boolean prependVisibleItemsWithoutCache(int i, boolean z) {
        int i2;
        boolean z2;
        int i3;
        int i4;
        int rowMin;
        boolean z3;
        int rowMin2;
        int findRowMax;
        int i5;
        int i6;
        if (this.mFirstVisibleIndex < 0) {
            int i7 = this.mStartIndex != -1 ? this.mStartIndex : 0;
            i2 = 0;
            z2 = false;
            i3 = i7;
            i4 = (this.mLocations.size() >= 0 ? (getLocation(getFirstIndex()).row + this.mNumRows) - 1 : i7) % this.mNumRows;
        } else if (this.mFirstVisibleIndex > getFirstIndex()) {
            return false;
        } else {
            i3 = this.mFirstVisibleIndex - 1;
            int i8 = getLocation(this.mFirstVisibleIndex).row;
            int findRowEdgeLimitSearchIndex = findRowEdgeLimitSearchIndex(false);
            if (findRowEdgeLimitSearchIndex < 0) {
                int i9 = i8 - 1;
                findRowMax = Integer.MAX_VALUE;
                int i10 = this.mNumRows - 1;
                while (true) {
                    i8 = i9;
                    if (i10 < 0) {
                        break;
                    }
                    findRowMax = this.mReversedFlow ? getRowMax(i10) : getRowMin(i10);
                    if (findRowMax != Integer.MAX_VALUE) {
                        i8 = i9;
                        break;
                    }
                    i10--;
                }
            } else {
                findRowMax = this.mReversedFlow ? findRowMax(true, findRowEdgeLimitSearchIndex, null) : findRowMin(false, findRowEdgeLimitSearchIndex, null);
            }
            if (this.mReversedFlow) {
                i5 = findRowMax;
                i6 = i8;
            } else {
                i5 = findRowMax;
                i6 = i8;
            }
        }
        boolean z4 = false;
        int i11 = i4;
        boolean z5 = z2;
        while (true) {
            if (i11 >= 0) {
                if (i3 < 0 || (!z && checkPrependOverLimit(i))) {
                    break;
                }
                int rowMax = this.mReversedFlow ? getRowMax(i11) : getRowMin(i11);
                if (rowMax != Integer.MAX_VALUE && rowMax != Integer.MIN_VALUE) {
                    rowMin = rowMax + (this.mReversedFlow ? this.mMargin : -this.mMargin);
                } else if (i11 == this.mNumRows - 1) {
                    int rowMax2 = this.mReversedFlow ? getRowMax(0) : getRowMin(0);
                    rowMin = rowMax2;
                    if (rowMax2 != Integer.MAX_VALUE) {
                        rowMin = rowMax2;
                        if (rowMax2 != Integer.MIN_VALUE) {
                            rowMin = rowMax2 + (this.mReversedFlow ? this.mMargin : -this.mMargin);
                        }
                    }
                } else {
                    rowMin = this.mReversedFlow ? getRowMin(i11 + 1) : getRowMax(i11 + 1);
                }
                int i12 = i3 - 1;
                int prependVisibleItemToRow = prependVisibleItemToRow(i3, i11, rowMin);
                z4 = true;
                if (z5) {
                    int i13 = rowMin;
                    int i14 = i12;
                    while (true) {
                        if (this.mReversedFlow) {
                            rowMin2 = i2;
                            z3 = z5;
                            i3 = i14;
                            if (i13 + prependVisibleItemToRow >= i2) {
                                break;
                            }
                            if (i14 >= 0) {
                                return true;
                            }
                            if (!z && checkPrependOverLimit(i)) {
                                return true;
                            }
                            i13 += this.mReversedFlow ? this.mMargin + prependVisibleItemToRow : (-prependVisibleItemToRow) - this.mMargin;
                            prependVisibleItemToRow = prependVisibleItemToRow(i14, i11, i13);
                            i14--;
                        } else {
                            if (i13 - prependVisibleItemToRow <= i2) {
                                i3 = i14;
                                z3 = z5;
                                rowMin2 = i2;
                                break;
                            }
                            if (i14 >= 0) {
                            }
                        }
                    }
                } else {
                    z3 = true;
                    if (this.mReversedFlow) {
                        rowMin2 = getRowMax(i11);
                        i3 = i12;
                    } else {
                        rowMin2 = getRowMin(i11);
                        i3 = i12;
                    }
                }
                i11--;
                i2 = rowMin2;
                z5 = z3;
            } else if (z) {
                return z4;
            } else {
                i2 = this.mReversedFlow ? findRowMax(true, null) : findRowMin(false, null);
                i11 = this.mNumRows - 1;
            }
        }
        return z4;
    }
}
