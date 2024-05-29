package android.support.v7.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import java.util.Arrays;
/* loaded from: a.zip:android/support/v7/widget/GridLayoutManager.class */
public class GridLayoutManager extends LinearLayoutManager {
    int[] mCachedBorders;
    final Rect mDecorInsets;
    boolean mPendingSpanCountChange;
    final SparseIntArray mPreLayoutSpanIndexCache;
    final SparseIntArray mPreLayoutSpanSizeCache;
    View[] mSet;
    int mSpanCount;
    SpanSizeLookup mSpanSizeLookup;

    /* loaded from: a.zip:android/support/v7/widget/GridLayoutManager$DefaultSpanSizeLookup.class */
    public static final class DefaultSpanSizeLookup extends SpanSizeLookup {
        @Override // android.support.v7.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanIndex(int i, int i2) {
            return i % i2;
        }

        @Override // android.support.v7.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanSize(int i) {
            return 1;
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/GridLayoutManager$LayoutParams.class */
    public static class LayoutParams extends RecyclerView.LayoutParams {
        private int mSpanIndex;
        private int mSpanSize;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public int getSpanIndex() {
            return this.mSpanIndex;
        }

        public int getSpanSize() {
            return this.mSpanSize;
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/GridLayoutManager$SpanSizeLookup.class */
    public static abstract class SpanSizeLookup {
        final SparseIntArray mSpanIndexCache = new SparseIntArray();
        private boolean mCacheSpanIndices = false;

        int findReferenceIndexFromCache(int i) {
            int i2 = 0;
            int size = this.mSpanIndexCache.size() - 1;
            while (i2 <= size) {
                int i3 = (i2 + size) >>> 1;
                if (this.mSpanIndexCache.keyAt(i3) < i) {
                    i2 = i3 + 1;
                } else {
                    size = i3 - 1;
                }
            }
            int i4 = i2 - 1;
            if (i4 < 0 || i4 >= this.mSpanIndexCache.size()) {
                return -1;
            }
            return this.mSpanIndexCache.keyAt(i4);
        }

        int getCachedSpanIndex(int i, int i2) {
            if (this.mCacheSpanIndices) {
                int i3 = this.mSpanIndexCache.get(i, -1);
                if (i3 != -1) {
                    return i3;
                }
                int spanIndex = getSpanIndex(i, i2);
                this.mSpanIndexCache.put(i, spanIndex);
                return spanIndex;
            }
            return getSpanIndex(i, i2);
        }

        public int getSpanGroupIndex(int i, int i2) {
            int i3;
            int i4 = 0;
            int i5 = 0;
            int spanSize = getSpanSize(i);
            int i6 = 0;
            while (i6 < i) {
                int spanSize2 = getSpanSize(i6);
                int i7 = i4 + spanSize2;
                if (i7 == i2) {
                    i4 = 0;
                    i3 = i5 + 1;
                } else {
                    i3 = i5;
                    i4 = i7;
                    if (i7 > i2) {
                        i4 = spanSize2;
                        i3 = i5 + 1;
                    }
                }
                i6++;
                i5 = i3;
            }
            int i8 = i5;
            if (i4 + spanSize > i2) {
                i8 = i5 + 1;
            }
            return i8;
        }

        public int getSpanIndex(int i, int i2) {
            int spanSize = getSpanSize(i);
            if (spanSize == i2) {
                return 0;
            }
            int i3 = 0;
            int i4 = 0;
            if (this.mCacheSpanIndices) {
                i3 = 0;
                i4 = 0;
                if (this.mSpanIndexCache.size() > 0) {
                    int findReferenceIndexFromCache = findReferenceIndexFromCache(i);
                    i3 = 0;
                    i4 = 0;
                    if (findReferenceIndexFromCache >= 0) {
                        i3 = this.mSpanIndexCache.get(findReferenceIndexFromCache) + getSpanSize(findReferenceIndexFromCache);
                        i4 = findReferenceIndexFromCache + 1;
                    }
                }
            }
            while (i4 < i) {
                int spanSize2 = getSpanSize(i4);
                int i5 = i3 + spanSize2;
                if (i5 == i2) {
                    i3 = 0;
                } else {
                    i3 = i5;
                    if (i5 > i2) {
                        i3 = spanSize2;
                    }
                }
                i4++;
            }
            if (i3 + spanSize <= i2) {
                return i3;
            }
            return 0;
        }

        public abstract int getSpanSize(int i);

        public void invalidateSpanIndexCache() {
            this.mSpanIndexCache.clear();
        }
    }

    public GridLayoutManager(Context context, int i) {
        super(context);
        this.mPendingSpanCountChange = false;
        this.mSpanCount = -1;
        this.mPreLayoutSpanSizeCache = new SparseIntArray();
        this.mPreLayoutSpanIndexCache = new SparseIntArray();
        this.mSpanSizeLookup = new DefaultSpanSizeLookup();
        this.mDecorInsets = new Rect();
        setSpanCount(i);
    }

    private void assignSpans(RecyclerView.Recycler recycler, RecyclerView.State state, int i, int i2, boolean z) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        if (z) {
            i4 = i;
            i5 = 1;
            i3 = 0;
        } else {
            i3 = i - 1;
            i4 = -1;
            i5 = -1;
        }
        if (this.mOrientation == 1 && isLayoutRTL()) {
            i6 = this.mSpanCount - 1;
            i7 = -1;
        } else {
            i6 = 0;
            i7 = 1;
        }
        while (i3 != i4) {
            View view = this.mSet[i3];
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.mSpanSize = getSpanSize(recycler, state, getPosition(view));
            if (i7 != -1 || layoutParams.mSpanSize <= 1) {
                layoutParams.mSpanIndex = i6;
            } else {
                layoutParams.mSpanIndex = i6 - (layoutParams.mSpanSize - 1);
            }
            i6 += layoutParams.mSpanSize * i7;
            i3 += i5;
        }
    }

    private void cachePreLayoutSpanMapping() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            int viewLayoutPosition = layoutParams.getViewLayoutPosition();
            this.mPreLayoutSpanSizeCache.put(viewLayoutPosition, layoutParams.getSpanSize());
            this.mPreLayoutSpanIndexCache.put(viewLayoutPosition, layoutParams.getSpanIndex());
        }
    }

    private void calculateItemBorders(int i) {
        this.mCachedBorders = calculateItemBorders(this.mCachedBorders, this.mSpanCount, i);
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x007b, code lost:
        if (r4[r4.length - 1] != r6) goto L23;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    static int[] calculateItemBorders(int[] iArr, int i, int i2) {
        int[] iArr2;
        if (iArr != null && iArr.length == i + 1) {
            iArr2 = iArr;
        }
        iArr2 = new int[i + 1];
        iArr2[0] = 0;
        int i3 = i2 / i;
        int i4 = i2 % i;
        int i5 = 0;
        int i6 = 0;
        for (int i7 = 1; i7 <= i; i7++) {
            int i8 = i6 + i4;
            i6 = i8;
            int i9 = i3;
            if (i8 > 0) {
                i6 = i8;
                i9 = i3;
                if (i - i8 < i4) {
                    i9 = i3 + 1;
                    i6 = i8 - i;
                }
            }
            i5 += i9;
            iArr2[i7] = i5;
        }
        return iArr2;
    }

    private void clearPreLayoutSpanMappingCache() {
        this.mPreLayoutSpanSizeCache.clear();
        this.mPreLayoutSpanIndexCache.clear();
    }

    private void ensureAnchorIsInCorrectSpan(RecyclerView.Recycler recycler, RecyclerView.State state, LinearLayoutManager.AnchorInfo anchorInfo, int i) {
        int spanIndex;
        boolean z = i == 1;
        int spanIndex2 = getSpanIndex(recycler, state, anchorInfo.mPosition);
        if (z) {
            while (spanIndex2 > 0 && anchorInfo.mPosition > 0) {
                anchorInfo.mPosition--;
                spanIndex2 = getSpanIndex(recycler, state, anchorInfo.mPosition);
            }
            return;
        }
        int itemCount = state.getItemCount();
        int i2 = anchorInfo.mPosition;
        while (i2 < itemCount - 1 && (spanIndex = getSpanIndex(recycler, state, i2 + 1)) > spanIndex2) {
            i2++;
            spanIndex2 = spanIndex;
        }
        anchorInfo.mPosition = i2;
    }

    private void ensureViewSet() {
        if (this.mSet == null || this.mSet.length != this.mSpanCount) {
            this.mSet = new View[this.mSpanCount];
        }
    }

    private int getSpanGroupIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (state.isPreLayout()) {
            int convertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
            if (convertPreLayoutPositionToPostLayout == -1) {
                Log.w("GridLayoutManager", "Cannot find span size for pre layout position. " + i);
                return 0;
            }
            return this.mSpanSizeLookup.getSpanGroupIndex(convertPreLayoutPositionToPostLayout, this.mSpanCount);
        }
        return this.mSpanSizeLookup.getSpanGroupIndex(i, this.mSpanCount);
    }

    private int getSpanIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (state.isPreLayout()) {
            int i2 = this.mPreLayoutSpanIndexCache.get(i, -1);
            if (i2 != -1) {
                return i2;
            }
            int convertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
            if (convertPreLayoutPositionToPostLayout == -1) {
                Log.w("GridLayoutManager", "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + i);
                return 0;
            }
            return this.mSpanSizeLookup.getCachedSpanIndex(convertPreLayoutPositionToPostLayout, this.mSpanCount);
        }
        return this.mSpanSizeLookup.getCachedSpanIndex(i, this.mSpanCount);
    }

    private int getSpanSize(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (state.isPreLayout()) {
            int i2 = this.mPreLayoutSpanSizeCache.get(i, -1);
            if (i2 != -1) {
                return i2;
            }
            int convertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
            if (convertPreLayoutPositionToPostLayout == -1) {
                Log.w("GridLayoutManager", "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + i);
                return 1;
            }
            return this.mSpanSizeLookup.getSpanSize(convertPreLayoutPositionToPostLayout);
        }
        return this.mSpanSizeLookup.getSpanSize(i);
    }

    private void guessMeasurement(float f, int i) {
        calculateItemBorders(Math.max(Math.round(this.mSpanCount * f), i));
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x004e, code lost:
        if (r6.mOrientation == 0) goto L19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:5:0x001f, code lost:
        if (r6.mOrientation == 1) goto L20;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void measureChildWithDecorationsAndMargin(View view, int i, int i2, boolean z, boolean z2) {
        int updateSpecWithExtra;
        int updateSpecWithExtra2;
        calculateItemDecorationsForChild(view, this.mDecorInsets);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        if (!z) {
            updateSpecWithExtra = i;
        }
        updateSpecWithExtra = updateSpecWithExtra(i, layoutParams.leftMargin + this.mDecorInsets.left, layoutParams.rightMargin + this.mDecorInsets.right);
        if (!z) {
            updateSpecWithExtra2 = i2;
        }
        updateSpecWithExtra2 = updateSpecWithExtra(i2, layoutParams.topMargin + this.mDecorInsets.top, layoutParams.bottomMargin + this.mDecorInsets.bottom);
        if (z2 ? shouldReMeasureChild(view, updateSpecWithExtra, updateSpecWithExtra2, layoutParams) : shouldMeasureChild(view, updateSpecWithExtra, updateSpecWithExtra2, layoutParams)) {
            view.measure(updateSpecWithExtra, updateSpecWithExtra2);
        }
    }

    private void updateMeasurements() {
        calculateItemBorders(getOrientation() == 1 ? (getWidth() - getPaddingRight()) - getPaddingLeft() : (getHeight() - getPaddingBottom()) - getPaddingTop());
    }

    private int updateSpecWithExtra(int i, int i2, int i3) {
        if (i2 == 0 && i3 == 0) {
            return i;
        }
        int mode = View.MeasureSpec.getMode(i);
        return (mode == Integer.MIN_VALUE || mode == 1073741824) ? View.MeasureSpec.makeMeasureSpec(Math.max(0, (View.MeasureSpec.getSize(i) - i2) - i3), mode) : i;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean checkLayoutParams(RecyclerView.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // android.support.v7.widget.LinearLayoutManager
    View findReferenceChild(RecyclerView.Recycler recycler, RecyclerView.State state, int i, int i2, int i3) {
        ensureLayoutState();
        View view = null;
        View view2 = null;
        int startAfterPadding = this.mOrientationHelper.getStartAfterPadding();
        int endAfterPadding = this.mOrientationHelper.getEndAfterPadding();
        int i4 = i2 > i ? 1 : -1;
        while (i != i2) {
            View childAt = getChildAt(i);
            int position = getPosition(childAt);
            View view3 = view;
            View view4 = view2;
            if (position >= 0) {
                view3 = view;
                view4 = view2;
                if (position >= i3) {
                    continue;
                } else if (getSpanIndex(recycler, state, position) != 0) {
                    view4 = view2;
                    view3 = view;
                } else if (((RecyclerView.LayoutParams) childAt.getLayoutParams()).isItemRemoved()) {
                    view3 = view;
                    view4 = view2;
                    if (view == null) {
                        view3 = childAt;
                        view4 = view2;
                    }
                } else if (this.mOrientationHelper.getDecoratedStart(childAt) < endAfterPadding && this.mOrientationHelper.getDecoratedEnd(childAt) >= startAfterPadding) {
                    return childAt;
                } else {
                    view3 = view;
                    view4 = view2;
                    if (view2 == null) {
                        view4 = childAt;
                        view3 = view;
                    }
                }
            }
            i += i4;
            view = view3;
            view2 = view4;
        }
        if (view2 == null) {
            view2 = view;
        }
        return view2;
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return this.mOrientation == 0 ? new LayoutParams(-2, -1) : new LayoutParams(-1, -2);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attributeSet) {
        return new LayoutParams(context, attributeSet);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 1) {
            return this.mSpanCount;
        }
        if (state.getItemCount() < 1) {
            return 0;
        }
        return getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 0) {
            return this.mSpanCount;
        }
        if (state.getItemCount() < 1) {
            return 0;
        }
        return getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
    }

    @Override // android.support.v7.widget.LinearLayoutManager
    void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state, LinearLayoutManager.LayoutState layoutState, LinearLayoutManager.LayoutChunkResult layoutChunkResult) {
        int i;
        int i2;
        View next;
        int modeInOther = this.mOrientationHelper.getModeInOther();
        boolean z = modeInOther != 1073741824;
        int i3 = getChildCount() > 0 ? this.mCachedBorders[this.mSpanCount] : 0;
        if (z) {
            updateMeasurements();
        }
        boolean z2 = layoutState.mItemDirection == 1;
        int i4 = this.mSpanCount;
        int i5 = 0;
        int i6 = 0;
        if (!z2) {
            i4 = getSpanIndex(recycler, state, layoutState.mCurrentPosition) + getSpanSize(recycler, state, layoutState.mCurrentPosition);
            i6 = 0;
            i5 = 0;
        }
        while (i5 < this.mSpanCount && layoutState.hasMore(state) && i4 > 0) {
            int i7 = layoutState.mCurrentPosition;
            int spanSize = getSpanSize(recycler, state, i7);
            if (spanSize > this.mSpanCount) {
                throw new IllegalArgumentException("Item at position " + i7 + " requires " + spanSize + " spans but GridLayoutManager has only " + this.mSpanCount + " spans.");
            }
            i4 -= spanSize;
            if (i4 < 0 || (next = layoutState.next(recycler)) == null) {
                break;
            }
            i6 += spanSize;
            this.mSet[i5] = next;
            i5++;
        }
        if (i5 == 0) {
            layoutChunkResult.mFinished = true;
            return;
        }
        int i8 = 0;
        float f = 0.0f;
        assignSpans(recycler, state, i5, i6, z2);
        int i9 = 0;
        while (i9 < i5) {
            View view = this.mSet[i9];
            if (layoutState.mScrapList == null) {
                if (z2) {
                    addView(view);
                } else {
                    addView(view, 0);
                }
            } else if (z2) {
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            int childMeasureSpec = getChildMeasureSpec(this.mCachedBorders[layoutParams.mSpanIndex + layoutParams.mSpanSize] - this.mCachedBorders[layoutParams.mSpanIndex], modeInOther, 0, this.mOrientation == 0 ? layoutParams.height : layoutParams.width, false);
            int childMeasureSpec2 = getChildMeasureSpec(this.mOrientationHelper.getTotalSpace(), this.mOrientationHelper.getMode(), 0, this.mOrientation == 1 ? layoutParams.height : layoutParams.width, true);
            if (this.mOrientation == 1) {
                measureChildWithDecorationsAndMargin(view, childMeasureSpec, childMeasureSpec2, layoutParams.height == -1, false);
            } else {
                measureChildWithDecorationsAndMargin(view, childMeasureSpec2, childMeasureSpec, layoutParams.width == -1, false);
            }
            int decoratedMeasurement = this.mOrientationHelper.getDecoratedMeasurement(view);
            int i10 = i8;
            if (decoratedMeasurement > i8) {
                i10 = decoratedMeasurement;
            }
            float decoratedMeasurementInOther = (this.mOrientationHelper.getDecoratedMeasurementInOther(view) * 1.0f) / layoutParams.mSpanSize;
            float f2 = f;
            if (decoratedMeasurementInOther > f) {
                f2 = decoratedMeasurementInOther;
            }
            i9++;
            i8 = i10;
            f = f2;
        }
        int i11 = i8;
        if (z) {
            guessMeasurement(f, i3);
            int i12 = 0;
            int i13 = 0;
            while (true) {
                i11 = i12;
                if (i13 >= i5) {
                    break;
                }
                View view2 = this.mSet[i13];
                LayoutParams layoutParams2 = (LayoutParams) view2.getLayoutParams();
                int childMeasureSpec3 = getChildMeasureSpec(this.mCachedBorders[layoutParams2.mSpanIndex + layoutParams2.mSpanSize] - this.mCachedBorders[layoutParams2.mSpanIndex], 1073741824, 0, this.mOrientation == 0 ? layoutParams2.height : layoutParams2.width, false);
                int childMeasureSpec4 = getChildMeasureSpec(this.mOrientationHelper.getTotalSpace(), this.mOrientationHelper.getMode(), 0, this.mOrientation == 1 ? layoutParams2.height : layoutParams2.width, true);
                if (this.mOrientation == 1) {
                    measureChildWithDecorationsAndMargin(view2, childMeasureSpec3, childMeasureSpec4, false, true);
                } else {
                    measureChildWithDecorationsAndMargin(view2, childMeasureSpec4, childMeasureSpec3, false, true);
                }
                int decoratedMeasurement2 = this.mOrientationHelper.getDecoratedMeasurement(view2);
                int i14 = i12;
                if (decoratedMeasurement2 > i12) {
                    i14 = decoratedMeasurement2;
                }
                i13++;
                i12 = i14;
            }
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i11, 1073741824);
        for (int i15 = 0; i15 < i5; i15++) {
            View view3 = this.mSet[i15];
            if (this.mOrientationHelper.getDecoratedMeasurement(view3) != i11) {
                LayoutParams layoutParams3 = (LayoutParams) view3.getLayoutParams();
                int childMeasureSpec5 = getChildMeasureSpec(this.mCachedBorders[layoutParams3.mSpanIndex + layoutParams3.mSpanSize] - this.mCachedBorders[layoutParams3.mSpanIndex], 1073741824, 0, this.mOrientation == 0 ? layoutParams3.height : layoutParams3.width, false);
                if (this.mOrientation == 1) {
                    measureChildWithDecorationsAndMargin(view3, childMeasureSpec5, makeMeasureSpec, true, true);
                } else {
                    measureChildWithDecorationsAndMargin(view3, makeMeasureSpec, childMeasureSpec5, true, true);
                }
            }
        }
        layoutChunkResult.mConsumed = i11;
        int i16 = 0;
        int i17 = 0;
        int i18 = 0;
        if (this.mOrientation == 1) {
            if (layoutState.mLayoutDirection == -1) {
                int i19 = layoutState.mOffset;
                i18 = i19 - i11;
                i = i19;
            } else {
                i18 = layoutState.mOffset;
                i = i18 + i11;
            }
        } else if (layoutState.mLayoutDirection == -1) {
            i17 = layoutState.mOffset;
            i16 = i17 - i11;
            i = 0;
        } else {
            i16 = layoutState.mOffset;
            i17 = i16 + i11;
            i = 0;
        }
        int i20 = 0;
        int i21 = i17;
        int i22 = i18;
        while (i20 < i5) {
            View view4 = this.mSet[i20];
            LayoutParams layoutParams4 = (LayoutParams) view4.getLayoutParams();
            if (this.mOrientation != 1) {
                i22 = getPaddingTop() + this.mCachedBorders[layoutParams4.mSpanIndex];
                i = i22 + this.mOrientationHelper.getDecoratedMeasurementInOther(view4);
                i2 = i21;
            } else if (isLayoutRTL()) {
                i2 = getPaddingLeft() + this.mCachedBorders[layoutParams4.mSpanIndex + layoutParams4.mSpanSize];
                i16 = i2 - this.mOrientationHelper.getDecoratedMeasurementInOther(view4);
            } else {
                i16 = getPaddingLeft() + this.mCachedBorders[layoutParams4.mSpanIndex];
                i2 = i16 + this.mOrientationHelper.getDecoratedMeasurementInOther(view4);
            }
            layoutDecoratedWithMargins(view4, i16, i22, i2, i);
            if (layoutParams4.isItemRemoved() || layoutParams4.isItemChanged()) {
                layoutChunkResult.mIgnoreConsumed = true;
            }
            layoutChunkResult.mFocusable |= view4.isFocusable();
            i20++;
            i21 = i2;
        }
        Arrays.fill(this.mSet, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.widget.LinearLayoutManager
    public void onAnchorReady(RecyclerView.Recycler recycler, RecyclerView.State state, LinearLayoutManager.AnchorInfo anchorInfo, int i) {
        super.onAnchorReady(recycler, state, anchorInfo, i);
        updateMeasurements();
        if (state.getItemCount() > 0 && !state.isPreLayout()) {
            ensureAnchorIsInCorrectSpan(recycler, state, anchorInfo, i);
        }
        ensureViewSet();
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public View onFocusSearchFailed(View view, int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int i2;
        int i3;
        int childCount;
        View childAt;
        boolean z;
        View findContainingItemView = findContainingItemView(view);
        if (findContainingItemView == null) {
            return null;
        }
        LayoutParams layoutParams = (LayoutParams) findContainingItemView.getLayoutParams();
        int i4 = layoutParams.mSpanIndex;
        int i5 = layoutParams.mSpanIndex + layoutParams.mSpanSize;
        if (super.onFocusSearchFailed(view, i, recycler, state) == null) {
            return null;
        }
        if ((convertFocusDirectionToLayoutDirection(i) == 1) != this.mShouldReverseLayout) {
            i2 = getChildCount() - 1;
            i3 = -1;
            childCount = -1;
        } else {
            i2 = 0;
            i3 = 1;
            childCount = getChildCount();
        }
        boolean isLayoutRTL = this.mOrientation == 1 ? isLayoutRTL() : false;
        View view2 = null;
        int i6 = -1;
        int i7 = 0;
        int i8 = i2;
        while (true) {
            int i9 = i8;
            if (i9 == childCount || (childAt = getChildAt(i9)) == findContainingItemView) {
                break;
            }
            if (childAt.isFocusable()) {
                LayoutParams layoutParams2 = (LayoutParams) childAt.getLayoutParams();
                int i10 = layoutParams2.mSpanIndex;
                int i11 = layoutParams2.mSpanIndex + layoutParams2.mSpanSize;
                if (i10 == i4 && i11 == i5) {
                    return childAt;
                }
                if (view2 == null) {
                    z = true;
                } else {
                    int min = Math.min(i11, i5) - Math.max(i10, i4);
                    if (min > i7) {
                        z = true;
                    } else {
                        z = false;
                        if (min == i7) {
                            z = false;
                            if (isLayoutRTL == (i10 > i6)) {
                                z = true;
                            }
                        }
                    }
                }
                if (z) {
                    view2 = childAt;
                    i6 = layoutParams2.mSpanIndex;
                    i7 = Math.min(i11, i5) - Math.max(i10, i4);
                }
            }
            i8 = i9 + i3;
        }
        return view2;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(view, accessibilityNodeInfoCompat);
            return;
        }
        LayoutParams layoutParams2 = (LayoutParams) layoutParams;
        int spanGroupIndex = getSpanGroupIndex(recycler, state, layoutParams2.getViewLayoutPosition());
        if (this.mOrientation == 0) {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(layoutParams2.getSpanIndex(), layoutParams2.getSpanSize(), spanGroupIndex, 1, this.mSpanCount > 1 && layoutParams2.getSpanSize() == this.mSpanCount, false));
        } else {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(spanGroupIndex, 1, layoutParams2.getSpanIndex(), layoutParams2.getSpanSize(), this.mSpanCount > 1 && layoutParams2.getSpanSize() == this.mSpanCount, false));
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsAdded(RecyclerView recyclerView, int i, int i2) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsChanged(RecyclerView recyclerView) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsMoved(RecyclerView recyclerView, int i, int i2, int i3) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsRemoved(RecyclerView recyclerView, int i, int i2) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsUpdated(RecyclerView recyclerView, int i, int i2, Object obj) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            cachePreLayoutSpanMapping();
        }
        super.onLayoutChildren(recycler, state);
        clearPreLayoutSpanMappingCache();
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        this.mPendingSpanCountChange = false;
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollHorizontallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        updateMeasurements();
        ensureViewSet();
        return super.scrollHorizontallyBy(i, recycler, state);
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollVerticallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        updateMeasurements();
        ensureViewSet();
        return super.scrollVerticallyBy(i, recycler, state);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void setMeasuredDimension(Rect rect, int i, int i2) {
        int chooseSize;
        int chooseSize2;
        if (this.mCachedBorders == null) {
            super.setMeasuredDimension(rect, i, i2);
        }
        int paddingLeft = getPaddingLeft() + getPaddingRight();
        int paddingTop = getPaddingTop() + getPaddingBottom();
        if (this.mOrientation == 1) {
            chooseSize2 = chooseSize(i2, rect.height() + paddingTop, getMinimumHeight());
            chooseSize = chooseSize(i, this.mCachedBorders[this.mCachedBorders.length - 1] + paddingLeft, getMinimumWidth());
        } else {
            chooseSize = chooseSize(i, rect.width() + paddingLeft, getMinimumWidth());
            chooseSize2 = chooseSize(i2, this.mCachedBorders[this.mCachedBorders.length - 1] + paddingTop, getMinimumHeight());
        }
        setMeasuredDimension(chooseSize, chooseSize2);
    }

    public void setSpanCount(int i) {
        if (i == this.mSpanCount) {
            return;
        }
        this.mPendingSpanCountChange = true;
        if (i < 1) {
            throw new IllegalArgumentException("Span count should be at least 1. Provided " + i);
        }
        this.mSpanCount = i;
        this.mSpanSizeLookup.invalidateSpanIndexCache();
        requestLayout();
    }

    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        this.mSpanSizeLookup = spanSizeLookup;
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public boolean supportsPredictiveItemAnimations() {
        boolean z = false;
        if (this.mPendingSavedState == null) {
            z = !this.mPendingSpanCountChange;
        }
        return z;
    }
}
