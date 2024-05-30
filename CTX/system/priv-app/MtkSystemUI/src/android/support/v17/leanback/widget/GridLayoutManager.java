package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v17.leanback.widget.Grid;
import android.support.v17.leanback.widget.ItemAlignmentFacet;
import android.support.v17.leanback.widget.WindowAlignment;
import android.support.v4.util.CircularIntArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class GridLayoutManager extends RecyclerView.LayoutManager {
    private static final Rect sTempRect = new Rect();
    static int[] sTwoInts = new int[2];
    final BaseGridView mBaseGridView;
    GridLinearSmoothScroller mCurrentSmoothScroller;
    int[] mDisappearingPositions;
    private int mExtraLayoutSpace;
    int mExtraLayoutSpaceInPreLayout;
    private FacetProviderAdapter mFacetProviderAdapter;
    private int mFixedRowSizeSecondary;
    Grid mGrid;
    private int mHorizontalSpacing;
    OnLayoutCompleteListener mLayoutCompleteListener;
    private int mMaxSizeSecondary;
    int mNumRows;
    PendingMoveSmoothScroller mPendingMoveSmoothScroller;
    int mPositionDeltaInPreLayout;
    private int mPrimaryScrollExtra;
    RecyclerView.Recycler mRecycler;
    private int[] mRowSizeSecondary;
    private int mRowSizeSecondaryRequested;
    int mScrollOffsetSecondary;
    private int mSizePrimary;
    private int mSpacingPrimary;
    private int mSpacingSecondary;
    RecyclerView.State mState;
    private int mVerticalSpacing;
    int mMaxPendingMoves = 10;
    int mOrientation = 0;
    private OrientationHelper mOrientationHelper = OrientationHelper.createHorizontalHelper(this);
    final SparseIntArray mPositionToRowInPostLayout = new SparseIntArray();
    int mFlag = 221696;
    private OnChildSelectedListener mChildSelectedListener = null;
    private ArrayList<OnChildViewHolderSelectedListener> mChildViewHolderSelectedListeners = null;
    OnChildLaidOutListener mChildLaidOutListener = null;
    int mFocusPosition = -1;
    int mSubFocusPosition = 0;
    private int mFocusPositionOffset = 0;
    private int mGravity = 8388659;
    private int mNumRowsRequested = 1;
    private int mFocusScrollStrategy = 0;
    final WindowAlignment mWindowAlignment = new WindowAlignment();
    private final ItemAlignment mItemAlignment = new ItemAlignment();
    private int[] mMeasuredDimension = new int[2];
    final ViewsStateBundle mChildrenStates = new ViewsStateBundle();
    private final Runnable mRequestLayoutRunnable = new Runnable() { // from class: android.support.v17.leanback.widget.GridLayoutManager.1
        @Override // java.lang.Runnable
        public void run() {
            GridLayoutManager.this.requestLayout();
        }
    };
    private Grid.Provider mGridProvider = new Grid.Provider() { // from class: android.support.v17.leanback.widget.GridLayoutManager.2
        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int getMinIndex() {
            return GridLayoutManager.this.mPositionDeltaInPreLayout;
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int getCount() {
            return GridLayoutManager.this.mState.getItemCount() + GridLayoutManager.this.mPositionDeltaInPreLayout;
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int createItem(int index, boolean append, Object[] item, boolean disappearingItem) {
            View v = GridLayoutManager.this.getViewForPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            RecyclerView.ViewHolder vh = GridLayoutManager.this.mBaseGridView.getChildViewHolder(v);
            lp.setItemAlignmentFacet((ItemAlignmentFacet) GridLayoutManager.this.getFacet(vh, ItemAlignmentFacet.class));
            if (!lp.isItemRemoved()) {
                if (disappearingItem) {
                    if (append) {
                        GridLayoutManager.this.addDisappearingView(v);
                    } else {
                        GridLayoutManager.this.addDisappearingView(v, 0);
                    }
                } else if (append) {
                    GridLayoutManager.this.addView(v);
                } else {
                    GridLayoutManager.this.addView(v, 0);
                }
                if (GridLayoutManager.this.mChildVisibility != -1) {
                    v.setVisibility(GridLayoutManager.this.mChildVisibility);
                }
                if (GridLayoutManager.this.mPendingMoveSmoothScroller != null) {
                    GridLayoutManager.this.mPendingMoveSmoothScroller.consumePendingMovesBeforeLayout();
                }
                int subindex = GridLayoutManager.this.getSubPositionByView(v, v.findFocus());
                if ((GridLayoutManager.this.mFlag & 3) != 1) {
                    if (index == GridLayoutManager.this.mFocusPosition && subindex == GridLayoutManager.this.mSubFocusPosition && GridLayoutManager.this.mPendingMoveSmoothScroller == null) {
                        GridLayoutManager.this.dispatchChildSelected();
                    }
                } else if ((GridLayoutManager.this.mFlag & 4) == 0) {
                    if ((GridLayoutManager.this.mFlag & 16) == 0 && index == GridLayoutManager.this.mFocusPosition && subindex == GridLayoutManager.this.mSubFocusPosition) {
                        GridLayoutManager.this.dispatchChildSelected();
                    } else if ((GridLayoutManager.this.mFlag & 16) != 0 && index >= GridLayoutManager.this.mFocusPosition && v.hasFocusable()) {
                        GridLayoutManager.this.mFocusPosition = index;
                        GridLayoutManager.this.mSubFocusPosition = subindex;
                        GridLayoutManager.this.mFlag &= -17;
                        GridLayoutManager.this.dispatchChildSelected();
                    }
                }
                GridLayoutManager.this.measureChild(v);
            }
            item[0] = v;
            return GridLayoutManager.this.mOrientation == 0 ? GridLayoutManager.this.getDecoratedMeasuredWidthWithMargin(v) : GridLayoutManager.this.getDecoratedMeasuredHeightWithMargin(v);
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public void addItem(Object item, int index, int length, int rowIndex, int edge) {
            int end;
            int start;
            int edge2 = edge;
            View v = (View) item;
            if (edge2 == Integer.MIN_VALUE || edge2 == Integer.MAX_VALUE) {
                edge2 = !GridLayoutManager.this.mGrid.isReversedFlow() ? GridLayoutManager.this.mWindowAlignment.mainAxis().getPaddingMin() : GridLayoutManager.this.mWindowAlignment.mainAxis().getSize() - GridLayoutManager.this.mWindowAlignment.mainAxis().getPaddingMax();
            }
            boolean edgeIsMin = !GridLayoutManager.this.mGrid.isReversedFlow();
            if (edgeIsMin) {
                int start2 = edge2;
                int end2 = edge2 + length;
                start = start2;
                end = end2;
            } else {
                int start3 = edge2 - length;
                end = edge2;
                start = start3;
            }
            int startSecondary = (GridLayoutManager.this.getRowStartSecondary(rowIndex) + GridLayoutManager.this.mWindowAlignment.secondAxis().getPaddingMin()) - GridLayoutManager.this.mScrollOffsetSecondary;
            GridLayoutManager.this.mChildrenStates.loadView(v, index);
            GridLayoutManager.this.layoutChild(rowIndex, v, start, end, startSecondary);
            if (!GridLayoutManager.this.mState.isPreLayout()) {
                GridLayoutManager.this.updateScrollLimits();
            }
            if ((GridLayoutManager.this.mFlag & 3) != 1 && GridLayoutManager.this.mPendingMoveSmoothScroller != null) {
                GridLayoutManager.this.mPendingMoveSmoothScroller.consumePendingMovesAfterLayout();
            }
            if (GridLayoutManager.this.mChildLaidOutListener != null) {
                RecyclerView.ViewHolder vh = GridLayoutManager.this.mBaseGridView.getChildViewHolder(v);
                GridLayoutManager.this.mChildLaidOutListener.onChildLaidOut(GridLayoutManager.this.mBaseGridView, v, index, vh == null ? -1L : vh.getItemId());
            }
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public void removeItem(int index) {
            View v = GridLayoutManager.this.findViewByPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout);
            if ((GridLayoutManager.this.mFlag & 3) == 1) {
                GridLayoutManager.this.detachAndScrapView(v, GridLayoutManager.this.mRecycler);
            } else {
                GridLayoutManager.this.removeAndRecycleView(v, GridLayoutManager.this.mRecycler);
            }
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int getEdge(int index) {
            View v = GridLayoutManager.this.findViewByPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout);
            return (GridLayoutManager.this.mFlag & 262144) != 0 ? GridLayoutManager.this.getViewMax(v) : GridLayoutManager.this.getViewMin(v);
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int getSize(int index) {
            return GridLayoutManager.this.getViewPrimarySize(GridLayoutManager.this.findViewByPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout));
        }
    };
    int mChildVisibility = -1;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class LayoutParams extends RecyclerView.LayoutParams {
        private int[] mAlignMultiple;
        private int mAlignX;
        private int mAlignY;
        private ItemAlignmentFacet mAlignmentFacet;
        int mBottomInset;
        int mLeftInset;
        int mRightInset;
        int mTopInset;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((RecyclerView.LayoutParams) source);
        }

        int getAlignX() {
            return this.mAlignX;
        }

        int getAlignY() {
            return this.mAlignY;
        }

        int getOpticalLeft(View view) {
            return view.getLeft() + this.mLeftInset;
        }

        int getOpticalTop(View view) {
            return view.getTop() + this.mTopInset;
        }

        int getOpticalRight(View view) {
            return view.getRight() - this.mRightInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalWidth(View view) {
            return (view.getWidth() - this.mLeftInset) - this.mRightInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalHeight(View view) {
            return (view.getHeight() - this.mTopInset) - this.mBottomInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalLeftInset() {
            return this.mLeftInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalRightInset() {
            return this.mRightInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalTopInset() {
            return this.mTopInset;
        }

        void setAlignX(int alignX) {
            this.mAlignX = alignX;
        }

        void setAlignY(int alignY) {
            this.mAlignY = alignY;
        }

        void setItemAlignmentFacet(ItemAlignmentFacet facet) {
            this.mAlignmentFacet = facet;
        }

        ItemAlignmentFacet getItemAlignmentFacet() {
            return this.mAlignmentFacet;
        }

        void calculateItemAlignments(int orientation, View view) {
            ItemAlignmentFacet.ItemAlignmentDef[] defs = this.mAlignmentFacet.getAlignmentDefs();
            if (this.mAlignMultiple == null || this.mAlignMultiple.length != defs.length) {
                this.mAlignMultiple = new int[defs.length];
            }
            for (int i = 0; i < defs.length; i++) {
                this.mAlignMultiple[i] = ItemAlignmentFacetHelper.getAlignmentPosition(view, defs[i], orientation);
            }
            if (orientation == 0) {
                this.mAlignX = this.mAlignMultiple[0];
            } else {
                this.mAlignY = this.mAlignMultiple[0];
            }
        }

        int[] getAlignMultiple() {
            return this.mAlignMultiple;
        }

        void setOpticalInsets(int leftInset, int topInset, int rightInset, int bottomInset) {
            this.mLeftInset = leftInset;
            this.mTopInset = topInset;
            this.mRightInset = rightInset;
            this.mBottomInset = bottomInset;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public abstract class GridLinearSmoothScroller extends LinearSmoothScroller {
        boolean mSkipOnStopInternal;

        GridLinearSmoothScroller() {
            super(GridLayoutManager.this.mBaseGridView.getContext());
        }

        @Override // android.support.v7.widget.LinearSmoothScroller, android.support.v7.widget.RecyclerView.SmoothScroller
        protected void onStop() {
            super.onStop();
            if (!this.mSkipOnStopInternal) {
                onStopInternal();
            }
            if (GridLayoutManager.this.mCurrentSmoothScroller == this) {
                GridLayoutManager.this.mCurrentSmoothScroller = null;
            }
            if (GridLayoutManager.this.mPendingMoveSmoothScroller == this) {
                GridLayoutManager.this.mPendingMoveSmoothScroller = null;
            }
        }

        protected void onStopInternal() {
            View targetView = findViewByPosition(getTargetPosition());
            if (targetView == null) {
                if (getTargetPosition() >= 0) {
                    GridLayoutManager.this.scrollToSelection(getTargetPosition(), 0, false, 0);
                    return;
                }
                return;
            }
            if (GridLayoutManager.this.mFocusPosition != getTargetPosition()) {
                GridLayoutManager.this.mFocusPosition = getTargetPosition();
            }
            if (GridLayoutManager.this.hasFocus()) {
                GridLayoutManager.this.mFlag |= 32;
                targetView.requestFocus();
                GridLayoutManager.this.mFlag &= -33;
            }
            GridLayoutManager.this.dispatchChildSelected();
            GridLayoutManager.this.dispatchChildSelectedAndPositioned();
        }

        @Override // android.support.v7.widget.LinearSmoothScroller
        protected int calculateTimeForScrolling(int dx) {
            int ms = super.calculateTimeForScrolling(dx);
            if (GridLayoutManager.this.mWindowAlignment.mainAxis().getSize() > 0) {
                float minMs = (30.0f / GridLayoutManager.this.mWindowAlignment.mainAxis().getSize()) * dx;
                if (ms < minMs) {
                    return (int) minMs;
                }
                return ms;
            }
            return ms;
        }

        @Override // android.support.v7.widget.LinearSmoothScroller, android.support.v7.widget.RecyclerView.SmoothScroller
        protected void onTargetFound(View targetView, RecyclerView.State state, RecyclerView.SmoothScroller.Action action) {
            int dx;
            int dy;
            if (GridLayoutManager.this.getScrollPosition(targetView, null, GridLayoutManager.sTwoInts)) {
                if (GridLayoutManager.this.mOrientation == 0) {
                    dx = GridLayoutManager.sTwoInts[0];
                    dy = GridLayoutManager.sTwoInts[1];
                } else {
                    dx = GridLayoutManager.sTwoInts[1];
                    dy = GridLayoutManager.sTwoInts[0];
                }
                int distance = (int) Math.sqrt((dx * dx) + (dy * dy));
                int time = calculateTimeForDeceleration(distance);
                action.update(dx, dy, time, this.mDecelerateInterpolator);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public final class PendingMoveSmoothScroller extends GridLinearSmoothScroller {
        private int mPendingMoves;
        private final boolean mStaggeredGrid;

        PendingMoveSmoothScroller(int initialPendingMoves, boolean staggeredGrid) {
            super();
            this.mPendingMoves = initialPendingMoves;
            this.mStaggeredGrid = staggeredGrid;
            setTargetPosition(-2);
        }

        void increasePendingMoves() {
            if (this.mPendingMoves < GridLayoutManager.this.mMaxPendingMoves) {
                this.mPendingMoves++;
            }
        }

        void decreasePendingMoves() {
            if (this.mPendingMoves > (-GridLayoutManager.this.mMaxPendingMoves)) {
                this.mPendingMoves--;
            }
        }

        void consumePendingMovesBeforeLayout() {
            View v;
            if (this.mStaggeredGrid || this.mPendingMoves == 0) {
                return;
            }
            int startPos = this.mPendingMoves > 0 ? GridLayoutManager.this.mFocusPosition + GridLayoutManager.this.mNumRows : GridLayoutManager.this.mFocusPosition - GridLayoutManager.this.mNumRows;
            View newSelected = null;
            int pos = startPos;
            while (this.mPendingMoves != 0 && (v = findViewByPosition(pos)) != null) {
                if (GridLayoutManager.this.canScrollTo(v)) {
                    newSelected = v;
                    GridLayoutManager.this.mFocusPosition = pos;
                    GridLayoutManager.this.mSubFocusPosition = 0;
                    if (this.mPendingMoves > 0) {
                        this.mPendingMoves--;
                    } else {
                        this.mPendingMoves++;
                    }
                }
                pos = this.mPendingMoves > 0 ? GridLayoutManager.this.mNumRows + pos : pos - GridLayoutManager.this.mNumRows;
            }
            if (newSelected != null && GridLayoutManager.this.hasFocus()) {
                GridLayoutManager.this.mFlag |= 32;
                newSelected.requestFocus();
                GridLayoutManager.this.mFlag &= -33;
            }
        }

        void consumePendingMovesAfterLayout() {
            if (this.mStaggeredGrid && this.mPendingMoves != 0) {
                this.mPendingMoves = GridLayoutManager.this.processSelectionMoves(true, this.mPendingMoves);
            }
            if (this.mPendingMoves == 0 || ((this.mPendingMoves > 0 && GridLayoutManager.this.hasCreatedLastItem()) || (this.mPendingMoves < 0 && GridLayoutManager.this.hasCreatedFirstItem()))) {
                setTargetPosition(GridLayoutManager.this.mFocusPosition);
                stop();
            }
        }

        @Override // android.support.v7.widget.LinearSmoothScroller
        protected void updateActionForInterimTarget(RecyclerView.SmoothScroller.Action action) {
            if (this.mPendingMoves == 0) {
                return;
            }
            super.updateActionForInterimTarget(action);
        }

        @Override // android.support.v7.widget.RecyclerView.SmoothScroller
        public PointF computeScrollVectorForPosition(int targetPosition) {
            if (this.mPendingMoves == 0) {
                return null;
            }
            int direction = ((GridLayoutManager.this.mFlag & 262144) == 0 ? this.mPendingMoves >= 0 : this.mPendingMoves <= 0) ? 1 : -1;
            if (GridLayoutManager.this.mOrientation == 0) {
                return new PointF(direction, 0.0f);
            }
            return new PointF(0.0f, direction);
        }

        @Override // android.support.v17.leanback.widget.GridLayoutManager.GridLinearSmoothScroller
        protected void onStopInternal() {
            super.onStopInternal();
            this.mPendingMoves = 0;
            View v = findViewByPosition(getTargetPosition());
            if (v != null) {
                GridLayoutManager.this.scrollToView(v, true);
            }
        }
    }

    String getTag() {
        return "GridLayoutManager:" + this.mBaseGridView.getId();
    }

    public GridLayoutManager(BaseGridView baseGridView) {
        this.mBaseGridView = baseGridView;
        setItemPrefetchEnabled(false);
    }

    public void setOrientation(int orientation) {
        if (orientation != 0 && orientation != 1) {
            return;
        }
        this.mOrientation = orientation;
        this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, this.mOrientation);
        this.mWindowAlignment.setOrientation(orientation);
        this.mItemAlignment.setOrientation(orientation);
        this.mFlag |= 256;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        int flags;
        if (this.mOrientation == 0) {
            flags = layoutDirection == 1 ? 262144 : 0;
        } else {
            flags = layoutDirection == 1 ? 524288 : 0;
        }
        if ((this.mFlag & 786432) == flags) {
            return;
        }
        this.mFlag = (this.mFlag & (-786433)) | flags;
        this.mFlag |= 256;
        this.mWindowAlignment.horizontal.setReversedFlow(layoutDirection == 1);
    }

    public void setWindowAlignment(int windowAlignment) {
        this.mWindowAlignment.mainAxis().setWindowAlignment(windowAlignment);
    }

    public void setFocusOutAllowed(boolean throughFront, boolean throughEnd) {
        this.mFlag = (this.mFlag & (-6145)) | (throughFront ? 2048 : 0) | (throughEnd ? 4096 : 0);
    }

    public void setFocusOutSideAllowed(boolean throughStart, boolean throughEnd) {
        this.mFlag = (this.mFlag & (-24577)) | (throughStart ? 8192 : 0) | (throughEnd ? 16384 : 0);
    }

    public void setNumRows(int numRows) {
        if (numRows < 0) {
            throw new IllegalArgumentException();
        }
        this.mNumRowsRequested = numRows;
    }

    public void setRowHeight(int height) {
        if (height >= 0 || height == -2) {
            this.mRowSizeSecondaryRequested = height;
            return;
        }
        throw new IllegalArgumentException("Invalid row height: " + height);
    }

    public void setVerticalSpacing(int space) {
        if (this.mOrientation == 1) {
            this.mVerticalSpacing = space;
            this.mSpacingPrimary = space;
            return;
        }
        this.mVerticalSpacing = space;
        this.mSpacingSecondary = space;
    }

    public void setHorizontalSpacing(int space) {
        if (this.mOrientation == 0) {
            this.mHorizontalSpacing = space;
            this.mSpacingPrimary = space;
            return;
        }
        this.mHorizontalSpacing = space;
        this.mSpacingSecondary = space;
    }

    public int getVerticalSpacing() {
        return this.mVerticalSpacing;
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    protected boolean hasDoneFirstLayout() {
        return this.mGrid != null;
    }

    public void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        if (listener == null) {
            this.mChildViewHolderSelectedListeners = null;
            return;
        }
        if (this.mChildViewHolderSelectedListeners == null) {
            this.mChildViewHolderSelectedListeners = new ArrayList<>();
        } else {
            this.mChildViewHolderSelectedListeners.clear();
        }
        this.mChildViewHolderSelectedListeners.add(listener);
    }

    boolean hasOnChildViewHolderSelectedListener() {
        return this.mChildViewHolderSelectedListeners != null && this.mChildViewHolderSelectedListeners.size() > 0;
    }

    void fireOnChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder child, int position, int subposition) {
        if (this.mChildViewHolderSelectedListeners == null) {
            return;
        }
        for (int i = this.mChildViewHolderSelectedListeners.size() - 1; i >= 0; i--) {
            this.mChildViewHolderSelectedListeners.get(i).onChildViewHolderSelected(parent, child, position, subposition);
        }
    }

    void fireOnChildViewHolderSelectedAndPositioned(RecyclerView parent, RecyclerView.ViewHolder child, int position, int subposition) {
        if (this.mChildViewHolderSelectedListeners == null) {
            return;
        }
        for (int i = this.mChildViewHolderSelectedListeners.size() - 1; i >= 0; i--) {
            this.mChildViewHolderSelectedListeners.get(i).onChildViewHolderSelectedAndPositioned(parent, child, position, subposition);
        }
    }

    private int getAdapterPositionByView(View view) {
        LayoutParams params;
        if (view == null || (params = (LayoutParams) view.getLayoutParams()) == null || params.isItemRemoved()) {
            return -1;
        }
        return params.getViewAdapterPosition();
    }

    int getSubPositionByView(View view, View childView) {
        if (view == null || childView == null) {
            return 0;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        ItemAlignmentFacet facet = lp.getItemAlignmentFacet();
        if (facet != null) {
            ItemAlignmentFacet.ItemAlignmentDef[] defs = facet.getAlignmentDefs();
            if (defs.length > 1) {
                while (childView != view) {
                    int id = childView.getId();
                    if (id != -1) {
                        for (int i = 1; i < defs.length; i++) {
                            if (defs[i].getItemAlignmentFocusViewId() == id) {
                                return i;
                            }
                        }
                        continue;
                    }
                    childView = (View) childView.getParent();
                }
            }
        }
        return 0;
    }

    private int getAdapterPositionByIndex(int index) {
        return getAdapterPositionByView(getChildAt(index));
    }

    void dispatchChildSelected() {
        if (this.mChildSelectedListener == null && !hasOnChildViewHolderSelectedListener()) {
            return;
        }
        View view = this.mFocusPosition == -1 ? null : findViewByPosition(this.mFocusPosition);
        int i = 0;
        if (view != null) {
            RecyclerView.ViewHolder vh = this.mBaseGridView.getChildViewHolder(view);
            if (this.mChildSelectedListener != null) {
                this.mChildSelectedListener.onChildSelected(this.mBaseGridView, view, this.mFocusPosition, vh == null ? -1L : vh.getItemId());
            }
            fireOnChildViewHolderSelected(this.mBaseGridView, vh, this.mFocusPosition, this.mSubFocusPosition);
        } else {
            if (this.mChildSelectedListener != null) {
                this.mChildSelectedListener.onChildSelected(this.mBaseGridView, null, -1, -1L);
            }
            fireOnChildViewHolderSelected(this.mBaseGridView, null, -1, 0);
        }
        if ((this.mFlag & 3) != 1 && !this.mBaseGridView.isLayoutRequested()) {
            int childCount = getChildCount();
            while (true) {
                int i2 = i;
                if (i2 < childCount) {
                    if (!getChildAt(i2).isLayoutRequested()) {
                        i = i2 + 1;
                    } else {
                        forceRequestLayout();
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChildSelectedAndPositioned() {
        if (!hasOnChildViewHolderSelectedListener()) {
            return;
        }
        View view = this.mFocusPosition == -1 ? null : findViewByPosition(this.mFocusPosition);
        if (view != null) {
            RecyclerView.ViewHolder vh = this.mBaseGridView.getChildViewHolder(view);
            fireOnChildViewHolderSelectedAndPositioned(this.mBaseGridView, vh, this.mFocusPosition, this.mSubFocusPosition);
            return;
        }
        if (this.mChildSelectedListener != null) {
            this.mChildSelectedListener.onChildSelected(this.mBaseGridView, null, -1, -1L);
        }
        fireOnChildViewHolderSelectedAndPositioned(this.mBaseGridView, null, -1, 0);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean canScrollHorizontally() {
        return this.mOrientation == 0 || this.mNumRows > 1;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean canScrollVertically() {
        return this.mOrientation == 1 || this.mNumRows > 1;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attrs) {
        return new LayoutParams(context, attrs);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        if (lp instanceof RecyclerView.LayoutParams) {
            return new LayoutParams((RecyclerView.LayoutParams) lp);
        }
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    protected View getViewForPosition(int position) {
        return this.mRecycler.getViewForPosition(position);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int getOpticalLeft(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalLeft(v);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int getOpticalRight(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalRight(v);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedLeft(View child) {
        return super.getDecoratedLeft(child) + ((LayoutParams) child.getLayoutParams()).mLeftInset;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedTop(View child) {
        return super.getDecoratedTop(child) + ((LayoutParams) child.getLayoutParams()).mTopInset;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedRight(View child) {
        return super.getDecoratedRight(child) - ((LayoutParams) child.getLayoutParams()).mRightInset;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedBottom(View child) {
        return super.getDecoratedBottom(child) - ((LayoutParams) child.getLayoutParams()).mBottomInset;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void getDecoratedBoundsWithMargins(View view, Rect outBounds) {
        super.getDecoratedBoundsWithMargins(view, outBounds);
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        outBounds.left += params.mLeftInset;
        outBounds.top += params.mTopInset;
        outBounds.right -= params.mRightInset;
        outBounds.bottom -= params.mBottomInset;
    }

    int getViewMin(View v) {
        return this.mOrientationHelper.getDecoratedStart(v);
    }

    int getViewMax(View v) {
        return this.mOrientationHelper.getDecoratedEnd(v);
    }

    int getViewPrimarySize(View view) {
        getDecoratedBoundsWithMargins(view, sTempRect);
        return this.mOrientation == 0 ? sTempRect.width() : sTempRect.height();
    }

    private int getViewCenter(View view) {
        return this.mOrientation == 0 ? getViewCenterX(view) : getViewCenterY(view);
    }

    private int getViewCenterSecondary(View view) {
        return this.mOrientation == 0 ? getViewCenterY(view) : getViewCenterX(view);
    }

    private int getViewCenterX(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        return p.getOpticalLeft(v) + p.getAlignX();
    }

    private int getViewCenterY(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        return p.getOpticalTop(v) + p.getAlignY();
    }

    private void saveContext(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mRecycler != null || this.mState != null) {
            Log.e("GridLayoutManager", "Recycler information was not released, bug!");
        }
        this.mRecycler = recycler;
        this.mState = state;
        this.mPositionDeltaInPreLayout = 0;
        this.mExtraLayoutSpaceInPreLayout = 0;
    }

    private void leaveContext() {
        this.mRecycler = null;
        this.mState = null;
        this.mPositionDeltaInPreLayout = 0;
        this.mExtraLayoutSpaceInPreLayout = 0;
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x007b, code lost:
        if (((r6.mFlag & 262144) != 0) != r6.mGrid.isReversedFlow()) goto L29;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean layoutInit() {
        int newItemCount = this.mState.getItemCount();
        if (newItemCount == 0) {
            this.mFocusPosition = -1;
            this.mSubFocusPosition = 0;
        } else if (this.mFocusPosition < newItemCount) {
            if (this.mFocusPosition == -1 && newItemCount > 0) {
                this.mFocusPosition = 0;
                this.mSubFocusPosition = 0;
            }
        } else {
            this.mFocusPosition = newItemCount - 1;
            this.mSubFocusPosition = 0;
        }
        if (!this.mState.didStructureChange() && this.mGrid != null && this.mGrid.getFirstVisibleIndex() >= 0 && (this.mFlag & 256) == 0 && this.mGrid.getNumRows() == this.mNumRows) {
            updateScrollController();
            updateSecondaryScrollLimits();
            this.mGrid.setSpacing(this.mSpacingPrimary);
            return true;
        }
        this.mFlag &= -257;
        if (this.mGrid != null && this.mNumRows == this.mGrid.getNumRows()) {
        }
        this.mGrid = Grid.createGrid(this.mNumRows);
        this.mGrid.setProvider(this.mGridProvider);
        this.mGrid.setReversedFlow((262144 & this.mFlag) != 0);
        initScrollController();
        updateSecondaryScrollLimits();
        this.mGrid.setSpacing(this.mSpacingPrimary);
        detachAndScrapAttachedViews(this.mRecycler);
        this.mGrid.resetVisibleIndex();
        this.mWindowAlignment.mainAxis().invalidateScrollMin();
        this.mWindowAlignment.mainAxis().invalidateScrollMax();
        return false;
    }

    private int getRowSizeSecondary(int rowIndex) {
        if (this.mFixedRowSizeSecondary != 0) {
            return this.mFixedRowSizeSecondary;
        }
        if (this.mRowSizeSecondary == null) {
            return 0;
        }
        return this.mRowSizeSecondary[rowIndex];
    }

    int getRowStartSecondary(int rowIndex) {
        int start = 0;
        if ((this.mFlag & 524288) != 0) {
            for (int i = this.mNumRows - 1; i > rowIndex; i--) {
                start += getRowSizeSecondary(i) + this.mSpacingSecondary;
            }
        } else {
            for (int i2 = 0; i2 < rowIndex; i2++) {
                start += getRowSizeSecondary(i2) + this.mSpacingSecondary;
            }
        }
        return start;
    }

    private int getSizeSecondary() {
        int rightmostIndex = (this.mFlag & 524288) != 0 ? 0 : this.mNumRows - 1;
        return getRowStartSecondary(rightmostIndex) + getRowSizeSecondary(rightmostIndex);
    }

    int getDecoratedMeasuredWidthWithMargin(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        return getDecoratedMeasuredWidth(v) + lp.leftMargin + lp.rightMargin;
    }

    int getDecoratedMeasuredHeightWithMargin(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        return getDecoratedMeasuredHeight(v) + lp.topMargin + lp.bottomMargin;
    }

    private void measureScrapChild(int position, int widthSpec, int heightSpec, int[] measuredDimension) {
        View view = this.mRecycler.getViewForPosition(position);
        if (view != null) {
            LayoutParams p = (LayoutParams) view.getLayoutParams();
            calculateItemDecorationsForChild(view, sTempRect);
            int widthUsed = p.leftMargin + p.rightMargin + sTempRect.left + sTempRect.right;
            int heightUsed = p.topMargin + p.bottomMargin + sTempRect.top + sTempRect.bottom;
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, getPaddingLeft() + getPaddingRight() + widthUsed, p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, getPaddingTop() + getPaddingBottom() + heightUsed, p.height);
            view.measure(childWidthSpec, childHeightSpec);
            measuredDimension[0] = getDecoratedMeasuredWidthWithMargin(view);
            measuredDimension[1] = getDecoratedMeasuredHeightWithMargin(view);
            this.mRecycler.recycleView(view);
        }
    }

    private boolean processRowSizeSecondary(boolean measure) {
        int secondarySize;
        if (this.mFixedRowSizeSecondary != 0 || this.mRowSizeSecondary == null) {
            return false;
        }
        CircularIntArray[] rows = this.mGrid == null ? null : this.mGrid.getItemPositionsInRows();
        int scrapeChildSize = -1;
        boolean changed = false;
        for (int rowIndex = 0; rowIndex < this.mNumRows; rowIndex++) {
            CircularIntArray row = rows == null ? null : rows[rowIndex];
            int rowItemsPairCount = row == null ? 0 : row.size();
            int rowSize = -1;
            int rowSize2 = 0;
            while (rowSize2 < rowItemsPairCount) {
                int rowIndexStart = row.get(rowSize2);
                int rowIndexEnd = row.get(rowSize2 + 1);
                int rowSize3 = rowSize;
                for (int rowSize4 = rowIndexStart; rowSize4 <= rowIndexEnd; rowSize4++) {
                    View view = findViewByPosition(rowSize4 - this.mPositionDeltaInPreLayout);
                    if (view != null) {
                        if (measure) {
                            measureChild(view);
                        }
                        if (this.mOrientation == 0) {
                            secondarySize = getDecoratedMeasuredHeightWithMargin(view);
                        } else {
                            secondarySize = getDecoratedMeasuredWidthWithMargin(view);
                        }
                        if (secondarySize > rowSize3) {
                            rowSize3 = secondarySize;
                        }
                    }
                }
                rowSize2 += 2;
                rowSize = rowSize3;
            }
            int itemCount = this.mState.getItemCount();
            if (!this.mBaseGridView.hasFixedSize() && measure && rowSize < 0 && itemCount > 0) {
                if (scrapeChildSize < 0) {
                    int position = this.mFocusPosition;
                    if (position < 0) {
                        position = 0;
                    } else if (position >= itemCount) {
                        position = itemCount - 1;
                    }
                    if (getChildCount() > 0) {
                        int firstPos = this.mBaseGridView.getChildViewHolder(getChildAt(0)).getLayoutPosition();
                        int lastPos = this.mBaseGridView.getChildViewHolder(getChildAt(getChildCount() - 1)).getLayoutPosition();
                        if (position >= firstPos && position <= lastPos) {
                            position = position - firstPos <= lastPos - position ? firstPos - 1 : lastPos + 1;
                            if (position < 0 && lastPos < itemCount - 1) {
                                position = lastPos + 1;
                            } else if (position >= itemCount && firstPos > 0) {
                                position = firstPos - 1;
                            }
                        }
                    }
                    if (position >= 0 && position < itemCount) {
                        measureScrapChild(position, View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0), this.mMeasuredDimension);
                        scrapeChildSize = this.mOrientation == 0 ? this.mMeasuredDimension[1] : this.mMeasuredDimension[0];
                    }
                }
                if (scrapeChildSize >= 0) {
                    rowSize = scrapeChildSize;
                }
            }
            if (rowSize < 0) {
                rowSize = 0;
            }
            if (this.mRowSizeSecondary[rowIndex] != rowSize) {
                this.mRowSizeSecondary[rowIndex] = rowSize;
                changed = true;
            }
        }
        return changed;
    }

    private void updateRowSecondarySizeRefresh() {
        this.mFlag = (this.mFlag & (-1025)) | (processRowSizeSecondary(false) ? 1024 : 0);
        if ((this.mFlag & 1024) != 0) {
            forceRequestLayout();
        }
    }

    private void forceRequestLayout() {
        ViewCompat.postOnAnimation(this.mBaseGridView, this.mRequestLayoutRunnable);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int sizeSecondary;
        int sizePrimary;
        int modeSecondary;
        int paddingSecondary;
        int measuredSizeSecondary;
        int childrenSize;
        saveContext(recycler, state);
        if (this.mOrientation == 0) {
            sizePrimary = View.MeasureSpec.getSize(widthSpec);
            sizeSecondary = View.MeasureSpec.getSize(heightSpec);
            modeSecondary = View.MeasureSpec.getMode(heightSpec);
            paddingSecondary = getPaddingTop() + getPaddingBottom();
        } else {
            sizeSecondary = View.MeasureSpec.getSize(widthSpec);
            sizePrimary = View.MeasureSpec.getSize(heightSpec);
            modeSecondary = View.MeasureSpec.getMode(widthSpec);
            paddingSecondary = getPaddingLeft() + getPaddingRight();
        }
        this.mMaxSizeSecondary = sizeSecondary;
        if (this.mRowSizeSecondaryRequested == -2) {
            this.mNumRows = this.mNumRowsRequested == 0 ? 1 : this.mNumRowsRequested;
            this.mFixedRowSizeSecondary = 0;
            if (this.mRowSizeSecondary == null || this.mRowSizeSecondary.length != this.mNumRows) {
                this.mRowSizeSecondary = new int[this.mNumRows];
            }
            if (this.mState.isPreLayout()) {
                updatePositionDeltaInPreLayout();
            }
            processRowSizeSecondary(true);
            if (modeSecondary == Integer.MIN_VALUE) {
                measuredSizeSecondary = Math.min(getSizeSecondary() + paddingSecondary, this.mMaxSizeSecondary);
            } else if (modeSecondary == 0) {
                measuredSizeSecondary = getSizeSecondary() + paddingSecondary;
            } else if (modeSecondary == 1073741824) {
                measuredSizeSecondary = this.mMaxSizeSecondary;
            } else {
                throw new IllegalStateException("wrong spec");
            }
        } else {
            if (modeSecondary != Integer.MIN_VALUE) {
                if (modeSecondary == 0) {
                    this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested == 0 ? sizeSecondary - paddingSecondary : this.mRowSizeSecondaryRequested;
                    this.mNumRows = this.mNumRowsRequested == 0 ? 1 : this.mNumRowsRequested;
                    measuredSizeSecondary = (this.mFixedRowSizeSecondary * this.mNumRows) + (this.mSpacingSecondary * (this.mNumRows - 1)) + paddingSecondary;
                } else if (modeSecondary != 1073741824) {
                    throw new IllegalStateException("wrong spec");
                }
            }
            if (this.mNumRowsRequested == 0 && this.mRowSizeSecondaryRequested == 0) {
                this.mNumRows = 1;
                this.mFixedRowSizeSecondary = sizeSecondary - paddingSecondary;
            } else if (this.mNumRowsRequested == 0) {
                this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
                this.mNumRows = (this.mSpacingSecondary + sizeSecondary) / (this.mRowSizeSecondaryRequested + this.mSpacingSecondary);
            } else if (this.mRowSizeSecondaryRequested == 0) {
                this.mNumRows = this.mNumRowsRequested;
                this.mFixedRowSizeSecondary = ((sizeSecondary - paddingSecondary) - (this.mSpacingSecondary * (this.mNumRows - 1))) / this.mNumRows;
            } else {
                this.mNumRows = this.mNumRowsRequested;
                this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
            }
            measuredSizeSecondary = sizeSecondary;
            if (modeSecondary == Integer.MIN_VALUE && (childrenSize = (this.mFixedRowSizeSecondary * this.mNumRows) + (this.mSpacingSecondary * (this.mNumRows - 1)) + paddingSecondary) < measuredSizeSecondary) {
                measuredSizeSecondary = childrenSize;
            }
        }
        if (this.mOrientation == 0) {
            setMeasuredDimension(sizePrimary, measuredSizeSecondary);
        } else {
            setMeasuredDimension(measuredSizeSecondary, sizePrimary);
        }
        leaveContext();
    }

    void measureChild(View child) {
        int secondarySpec;
        int heightSpec;
        int widthSpec;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        calculateItemDecorationsForChild(child, sTempRect);
        int widthUsed = lp.leftMargin + lp.rightMargin + sTempRect.left + sTempRect.right;
        int heightUsed = lp.topMargin + lp.bottomMargin + sTempRect.top + sTempRect.bottom;
        if (this.mRowSizeSecondaryRequested == -2) {
            secondarySpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        } else {
            secondarySpec = View.MeasureSpec.makeMeasureSpec(this.mFixedRowSizeSecondary, 1073741824);
        }
        if (this.mOrientation == 0) {
            widthSpec = ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(0, 0), widthUsed, lp.width);
            heightSpec = ViewGroup.getChildMeasureSpec(secondarySpec, heightUsed, lp.height);
        } else {
            heightSpec = ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(0, 0), heightUsed, lp.height);
            widthSpec = ViewGroup.getChildMeasureSpec(secondarySpec, widthUsed, lp.width);
        }
        child.measure(widthSpec, heightSpec);
    }

    <E> E getFacet(RecyclerView.ViewHolder vh, Class<? extends E> facetClass) {
        FacetProvider p;
        E facet = null;
        if (vh instanceof FacetProvider) {
            facet = (E) ((FacetProvider) vh).getFacet(facetClass);
        }
        if (facet != null || this.mFacetProviderAdapter == null || (p = this.mFacetProviderAdapter.getFacetProvider(vh.getItemViewType())) == null) {
            return facet;
        }
        E facet2 = (E) p.getFacet(facetClass);
        return facet2;
    }

    /* JADX WARN: Removed duplicated region for block: B:43:0x007e  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x0089  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    void layoutChild(int rowIndex, View v, int start, int end, int startSecondary) {
        int startSecondary2;
        int startSecondary3;
        int top;
        int left;
        int bottom;
        int bottom2;
        int sizeSecondary = this.mOrientation == 0 ? getDecoratedMeasuredHeightWithMargin(v) : getDecoratedMeasuredWidthWithMargin(v);
        if (this.mFixedRowSizeSecondary > 0) {
            sizeSecondary = Math.min(sizeSecondary, this.mFixedRowSizeSecondary);
        }
        int sizeSecondary2 = sizeSecondary;
        int verticalGravity = this.mGravity & 112;
        int horizontalGravity = (this.mFlag & 786432) != 0 ? Gravity.getAbsoluteGravity(this.mGravity & 8388615, 1) : this.mGravity & 7;
        if ((this.mOrientation != 0 || verticalGravity != 48) && (this.mOrientation != 1 || horizontalGravity != 3)) {
            if ((this.mOrientation == 0 && verticalGravity == 80) || (this.mOrientation == 1 && horizontalGravity == 5)) {
                startSecondary2 = startSecondary + (getRowSizeSecondary(rowIndex) - sizeSecondary2);
            } else if ((this.mOrientation == 0 && verticalGravity == 16) || (this.mOrientation == 1 && horizontalGravity == 1)) {
                startSecondary2 = startSecondary + ((getRowSizeSecondary(rowIndex) - sizeSecondary2) / 2);
            }
            startSecondary3 = startSecondary2;
            if (this.mOrientation != 0) {
                int top2 = startSecondary3;
                bottom2 = end;
                int bottom3 = startSecondary3 + sizeSecondary2;
                left = start;
                top = top2;
                bottom = bottom3;
            } else {
                int left2 = startSecondary3;
                top = start;
                left = left2;
                bottom = end;
                bottom2 = startSecondary3 + sizeSecondary2;
            }
            int right = bottom2;
            LayoutParams params = (LayoutParams) v.getLayoutParams();
            layoutDecoratedWithMargins(v, left, top, right, bottom);
            super.getDecoratedBoundsWithMargins(v, sTempRect);
            params.setOpticalInsets(left - sTempRect.left, top - sTempRect.top, sTempRect.right - right, sTempRect.bottom - bottom);
            updateChildAlignments(v);
        }
        startSecondary3 = startSecondary;
        if (this.mOrientation != 0) {
        }
        int right2 = bottom2;
        LayoutParams params2 = (LayoutParams) v.getLayoutParams();
        layoutDecoratedWithMargins(v, left, top, right2, bottom);
        super.getDecoratedBoundsWithMargins(v, sTempRect);
        params2.setOpticalInsets(left - sTempRect.left, top - sTempRect.top, sTempRect.right - right2, sTempRect.bottom - bottom);
        updateChildAlignments(v);
    }

    private void updateChildAlignments(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        if (p.getItemAlignmentFacet() == null) {
            p.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(v));
            p.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(v));
            return;
        }
        p.calculateItemAlignments(this.mOrientation, v);
        if (this.mOrientation == 0) {
            p.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(v));
        } else {
            p.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(v));
        }
    }

    private void removeInvisibleViewsAtEnd() {
        if ((this.mFlag & 65600) == 65536) {
            this.mGrid.removeInvisibleItemsAtEnd(this.mFocusPosition, (this.mFlag & 262144) != 0 ? -this.mExtraLayoutSpace : this.mSizePrimary + this.mExtraLayoutSpace);
        }
    }

    private void removeInvisibleViewsAtFront() {
        if ((this.mFlag & 65600) == 65536) {
            this.mGrid.removeInvisibleItemsAtFront(this.mFocusPosition, (this.mFlag & 262144) != 0 ? this.mSizePrimary + this.mExtraLayoutSpace : -this.mExtraLayoutSpace);
        }
    }

    private boolean appendOneColumnVisibleItems() {
        return this.mGrid.appendOneColumnVisibleItems();
    }

    int getSlideOutDistance() {
        int start;
        int start2;
        int top;
        if (this.mOrientation == 1) {
            int distance = -getHeight();
            if (getChildCount() > 0 && (top = getChildAt(0).getTop()) < 0) {
                return distance + top;
            }
            return distance;
        }
        int distance2 = this.mFlag;
        if ((distance2 & 262144) != 0) {
            int distance3 = getWidth();
            if (getChildCount() > 0 && (start2 = getChildAt(0).getRight()) > distance3) {
                return start2;
            }
            return distance3;
        }
        int distance4 = getWidth();
        int distance5 = -distance4;
        if (getChildCount() > 0 && (start = getChildAt(0).getLeft()) < 0) {
            return distance5 + start;
        }
        return distance5;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isSlidingChildViews() {
        return (this.mFlag & 64) != 0;
    }

    private boolean prependOneColumnVisibleItems() {
        return this.mGrid.prependOneColumnVisibleItems();
    }

    private void appendVisibleItems() {
        this.mGrid.appendVisibleItems((this.mFlag & 262144) != 0 ? (-this.mExtraLayoutSpace) - this.mExtraLayoutSpaceInPreLayout : this.mSizePrimary + this.mExtraLayoutSpace + this.mExtraLayoutSpaceInPreLayout);
    }

    private void prependVisibleItems() {
        this.mGrid.prependVisibleItems((this.mFlag & 262144) != 0 ? this.mSizePrimary + this.mExtraLayoutSpace + this.mExtraLayoutSpaceInPreLayout : (-this.mExtraLayoutSpace) - this.mExtraLayoutSpaceInPreLayout);
    }

    private void fastRelayout() {
        int primarySize;
        int i;
        boolean invalidateAfter = false;
        int childCount = getChildCount();
        int position = this.mGrid.getFirstVisibleIndex();
        this.mFlag &= -9;
        int position2 = position;
        int index = 0;
        while (true) {
            if (index >= childCount) {
                break;
            }
            View view = getChildAt(index);
            if (position2 != getAdapterPositionByView(view)) {
                invalidateAfter = true;
                break;
            }
            Grid.Location location = this.mGrid.getLocation(position2);
            if (location != null) {
                int startSecondary = (getRowStartSecondary(location.row) + this.mWindowAlignment.secondAxis().getPaddingMin()) - this.mScrollOffsetSecondary;
                int start = getViewMin(view);
                int oldPrimarySize = getViewPrimarySize(view);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (lp.viewNeedsUpdate()) {
                    this.mFlag |= 8;
                    detachAndScrapView(view, this.mRecycler);
                    view = getViewForPosition(position2);
                    addView(view, index);
                }
                View view2 = view;
                measureChild(view2);
                if (this.mOrientation == 0) {
                    primarySize = getDecoratedMeasuredWidthWithMargin(view2);
                    i = start + primarySize;
                } else {
                    primarySize = getDecoratedMeasuredHeightWithMargin(view2);
                    i = start + primarySize;
                }
                int primarySize2 = primarySize;
                int end = i;
                boolean invalidateAfter2 = invalidateAfter;
                layoutChild(location.row, view2, start, end, startSecondary);
                if (oldPrimarySize == primarySize2) {
                    index++;
                    position2++;
                    invalidateAfter = invalidateAfter2;
                } else {
                    invalidateAfter = true;
                    break;
                }
            } else {
                invalidateAfter = true;
                break;
            }
        }
        if (invalidateAfter) {
            int savedLastPos = this.mGrid.getLastVisibleIndex();
            for (int i2 = childCount - 1; i2 >= index; i2--) {
                View v = getChildAt(i2);
                detachAndScrapView(v, this.mRecycler);
            }
            this.mGrid.invalidateItemsAfter(position2);
            if ((this.mFlag & 65536) != 0) {
                appendVisibleItems();
                if (this.mFocusPosition >= 0 && this.mFocusPosition <= savedLastPos) {
                    while (this.mGrid.getLastVisibleIndex() < this.mFocusPosition) {
                        this.mGrid.appendOneColumnVisibleItems();
                    }
                }
            } else {
                while (this.mGrid.appendOneColumnVisibleItems() && this.mGrid.getLastVisibleIndex() < savedLastPos) {
                }
            }
        }
        updateScrollLimits();
        updateSecondaryScrollLimits();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
    }

    private void focusToViewInLayout(boolean hadFocus, boolean alignToView, int extraDelta, int extraDeltaSecondary) {
        View focusView = findViewByPosition(this.mFocusPosition);
        if (focusView != null && alignToView) {
            scrollToView(focusView, false, extraDelta, extraDeltaSecondary);
        }
        if (focusView != null && hadFocus && !focusView.hasFocus()) {
            focusView.requestFocus();
        } else if (!hadFocus && !this.mBaseGridView.hasFocus()) {
            if (focusView != null && focusView.hasFocusable()) {
                this.mBaseGridView.focusableViewAvailable(focusView);
            } else {
                int i = 0;
                int count = getChildCount();
                while (true) {
                    if (i < count) {
                        focusView = getChildAt(i);
                        if (focusView == null || !focusView.hasFocusable()) {
                            i++;
                        } else {
                            this.mBaseGridView.focusableViewAvailable(focusView);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (alignToView && focusView != null && focusView.hasFocus()) {
                scrollToView(focusView, false, extraDelta, extraDeltaSecondary);
            }
        }
    }

    /* loaded from: classes.dex */
    public static class OnLayoutCompleteListener {
        public void onLayoutCompleted(RecyclerView.State state) {
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onLayoutCompleted(RecyclerView.State state) {
        if (this.mLayoutCompleteListener != null) {
            this.mLayoutCompleteListener.onLayoutCompleted(state);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    void updatePositionToRowMapInPostLayout() {
        Grid.Location loc;
        this.mPositionToRowInPostLayout.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            int position = this.mBaseGridView.getChildViewHolder(getChildAt(i)).getOldPosition();
            if (position >= 0 && (loc = this.mGrid.getLocation(position)) != null) {
                this.mPositionToRowInPostLayout.put(position, loc.row);
            }
        }
    }

    void fillScrapViewsInPostLayout() {
        List<RecyclerView.ViewHolder> scrapList = this.mRecycler.getScrapList();
        int scrapSize = scrapList.size();
        if (scrapSize == 0) {
            return;
        }
        if (this.mDisappearingPositions == null || scrapSize > this.mDisappearingPositions.length) {
            int length = this.mDisappearingPositions == null ? 16 : this.mDisappearingPositions.length;
            while (length < scrapSize) {
                length <<= 1;
            }
            this.mDisappearingPositions = new int[length];
        }
        int totalItems = 0;
        for (int totalItems2 = 0; totalItems2 < scrapSize; totalItems2++) {
            int pos = scrapList.get(totalItems2).getAdapterPosition();
            if (pos >= 0) {
                this.mDisappearingPositions[totalItems] = pos;
                totalItems++;
            }
        }
        if (totalItems > 0) {
            Arrays.sort(this.mDisappearingPositions, 0, totalItems);
            this.mGrid.fillDisappearingItems(this.mDisappearingPositions, totalItems, this.mPositionToRowInPostLayout);
        }
        this.mPositionToRowInPostLayout.clear();
    }

    void updatePositionDeltaInPreLayout() {
        if (getChildCount() > 0) {
            View view = getChildAt(0);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            this.mPositionDeltaInPreLayout = this.mGrid.getFirstVisibleIndex() - lp.getViewLayoutPosition();
            return;
        }
        this.mPositionDeltaInPreLayout = 0;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: CFG modification limit reached, blocks count: 228
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:59)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onLayoutChildren(android.support.v7.widget.RecyclerView.Recycler r17, android.support.v7.widget.RecyclerView.State r18) {
        /*
            Method dump skipped, instructions count: 547
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.widget.GridLayoutManager.onLayoutChildren(android.support.v7.widget.RecyclerView$Recycler, android.support.v7.widget.RecyclerView$State):void");
    }

    private void offsetChildrenSecondary(int increment) {
        int childCount = getChildCount();
        int i = 0;
        if (this.mOrientation == 0) {
            while (true) {
                int i2 = i;
                if (i2 < childCount) {
                    getChildAt(i2).offsetTopAndBottom(increment);
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        } else {
            while (true) {
                int i3 = i;
                if (i3 < childCount) {
                    getChildAt(i3).offsetLeftAndRight(increment);
                    i = i3 + 1;
                } else {
                    return;
                }
            }
        }
    }

    private void offsetChildrenPrimary(int increment) {
        int childCount = getChildCount();
        int i = 0;
        if (this.mOrientation == 1) {
            while (true) {
                int i2 = i;
                if (i2 < childCount) {
                    getChildAt(i2).offsetTopAndBottom(increment);
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        } else {
            while (true) {
                int i3 = i;
                if (i3 < childCount) {
                    getChildAt(i3).offsetLeftAndRight(increment);
                    i = i3 + 1;
                } else {
                    return;
                }
            }
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int result;
        if ((this.mFlag & 512) == 0 || !hasDoneFirstLayout()) {
            return 0;
        }
        saveContext(recycler, state);
        this.mFlag = (this.mFlag & (-4)) | 2;
        if (this.mOrientation == 0) {
            result = scrollDirectionPrimary(dx);
        } else {
            result = scrollDirectionSecondary(dx);
        }
        leaveContext();
        this.mFlag &= -4;
        return result;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int result;
        if ((this.mFlag & 512) == 0 || !hasDoneFirstLayout()) {
            return 0;
        }
        this.mFlag = (this.mFlag & (-4)) | 2;
        saveContext(recycler, state);
        if (this.mOrientation == 1) {
            result = scrollDirectionPrimary(dy);
        } else {
            result = scrollDirectionSecondary(dy);
        }
        leaveContext();
        this.mFlag &= -4;
        return result;
    }

    private int scrollDirectionPrimary(int da) {
        int minScroll;
        int maxScroll;
        if ((this.mFlag & 64) == 0 && (this.mFlag & 3) != 1) {
            if (da > 0) {
                if (!this.mWindowAlignment.mainAxis().isMaxUnknown() && da > (maxScroll = this.mWindowAlignment.mainAxis().getMaxScroll())) {
                    da = maxScroll;
                }
            } else if (da < 0 && !this.mWindowAlignment.mainAxis().isMinUnknown() && da < (minScroll = this.mWindowAlignment.mainAxis().getMinScroll())) {
                da = minScroll;
            }
        }
        if (da == 0) {
            return 0;
        }
        offsetChildrenPrimary(-da);
        if ((this.mFlag & 3) == 1) {
            updateScrollLimits();
            return da;
        }
        int childCount = getChildCount();
        if ((this.mFlag & 262144) == 0 ? da < 0 : da > 0) {
            prependVisibleItems();
        } else {
            appendVisibleItems();
        }
        boolean updated = getChildCount() > childCount;
        int childCount2 = getChildCount();
        if ((262144 & this.mFlag) == 0 ? da < 0 : da > 0) {
            removeInvisibleViewsAtEnd();
        } else {
            removeInvisibleViewsAtFront();
        }
        boolean minScroll2 = getChildCount() < childCount2;
        if (minScroll2 | updated) {
            updateRowSecondarySizeRefresh();
        }
        this.mBaseGridView.invalidate();
        updateScrollLimits();
        return da;
    }

    private int scrollDirectionSecondary(int dy) {
        if (dy == 0) {
            return 0;
        }
        offsetChildrenSecondary(-dy);
        this.mScrollOffsetSecondary += dy;
        updateSecondaryScrollLimits();
        this.mBaseGridView.invalidate();
        return dy;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state, RecyclerView.LayoutManager.LayoutPrefetchRegistry layoutPrefetchRegistry) {
        try {
            saveContext(null, state);
            int da = this.mOrientation == 0 ? dx : dy;
            if (getChildCount() != 0 && da != 0) {
                int fromLimit = da < 0 ? -this.mExtraLayoutSpace : this.mSizePrimary + this.mExtraLayoutSpace;
                this.mGrid.collectAdjacentPrefetchPositions(fromLimit, da, layoutPrefetchRegistry);
            }
        } finally {
            leaveContext();
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void collectInitialPrefetchPositions(int adapterItemCount, RecyclerView.LayoutManager.LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int numToPrefetch = this.mBaseGridView.mInitialPrefetchItemCount;
        if (adapterItemCount != 0 && numToPrefetch != 0) {
            int initialPos = Math.max(0, Math.min(this.mFocusPosition - ((numToPrefetch - 1) / 2), adapterItemCount - numToPrefetch));
            for (int i = initialPos; i < adapterItemCount && i < initialPos + numToPrefetch; i++) {
                layoutPrefetchRegistry.addPosition(i, 0);
            }
        }
    }

    void updateScrollLimits() {
        int highVisiblePos;
        int highMaxPos;
        int lowVisiblePos;
        int lowMinPos;
        int maxEdge;
        int maxViewCenter;
        int minEdge;
        int minViewCenter;
        if (this.mState.getItemCount() == 0) {
            return;
        }
        if ((this.mFlag & 262144) == 0) {
            highVisiblePos = this.mGrid.getLastVisibleIndex();
            highMaxPos = this.mState.getItemCount() - 1;
            lowVisiblePos = this.mGrid.getFirstVisibleIndex();
            lowMinPos = 0;
        } else {
            highVisiblePos = this.mGrid.getFirstVisibleIndex();
            highMaxPos = 0;
            lowVisiblePos = this.mGrid.getLastVisibleIndex();
            lowMinPos = this.mState.getItemCount() - 1;
        }
        if (highVisiblePos < 0 || lowVisiblePos < 0) {
            return;
        }
        boolean highAvailable = highVisiblePos == highMaxPos;
        boolean lowAvailable = lowVisiblePos == lowMinPos;
        if (!highAvailable && this.mWindowAlignment.mainAxis().isMaxUnknown() && !lowAvailable && this.mWindowAlignment.mainAxis().isMinUnknown()) {
            return;
        }
        if (highAvailable) {
            maxEdge = this.mGrid.findRowMax(true, sTwoInts);
            View maxChild = findViewByPosition(sTwoInts[1]);
            maxViewCenter = getViewCenter(maxChild);
            LayoutParams lp = (LayoutParams) maxChild.getLayoutParams();
            int[] multipleAligns = lp.getAlignMultiple();
            if (multipleAligns != null && multipleAligns.length > 0) {
                maxViewCenter += multipleAligns[multipleAligns.length - 1] - multipleAligns[0];
            }
        } else {
            maxEdge = Integer.MAX_VALUE;
            maxViewCenter = Integer.MAX_VALUE;
        }
        int maxViewCenter2 = maxViewCenter;
        if (lowAvailable) {
            minEdge = this.mGrid.findRowMin(false, sTwoInts);
            View minChild = findViewByPosition(sTwoInts[1]);
            minViewCenter = getViewCenter(minChild);
        } else {
            minEdge = Integer.MIN_VALUE;
            minViewCenter = Integer.MIN_VALUE;
        }
        this.mWindowAlignment.mainAxis().updateMinMax(minEdge, maxEdge, minViewCenter, maxViewCenter2);
    }

    private void updateSecondaryScrollLimits() {
        WindowAlignment.Axis secondAxis = this.mWindowAlignment.secondAxis();
        int minEdge = secondAxis.getPaddingMin() - this.mScrollOffsetSecondary;
        int maxEdge = getSizeSecondary() + minEdge;
        secondAxis.updateMinMax(minEdge, maxEdge, minEdge, maxEdge);
    }

    private void initScrollController() {
        this.mWindowAlignment.reset();
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
        this.mScrollOffsetSecondary = 0;
    }

    private void updateScrollController() {
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void scrollToPosition(int position) {
        setSelection(position, 0, false, 0);
    }

    public void setSelection(int position, int primaryScrollExtra) {
        setSelection(position, 0, false, primaryScrollExtra);
    }

    public void setSelectionSmooth(int position) {
        setSelection(position, 0, true, 0);
    }

    public void setSelectionWithSub(int position, int subposition, int primaryScrollExtra) {
        setSelection(position, subposition, false, primaryScrollExtra);
    }

    public int getSelection() {
        return this.mFocusPosition;
    }

    public void setSelection(int position, int subposition, boolean smooth, int primaryScrollExtra) {
        if ((this.mFocusPosition != position && position != -1) || subposition != this.mSubFocusPosition || primaryScrollExtra != this.mPrimaryScrollExtra) {
            scrollToSelection(position, subposition, smooth, primaryScrollExtra);
        }
    }

    void scrollToSelection(int position, int subposition, boolean smooth, int primaryScrollExtra) {
        this.mPrimaryScrollExtra = primaryScrollExtra;
        View view = findViewByPosition(position);
        boolean notSmoothScrolling = !isSmoothScrolling();
        if (notSmoothScrolling && !this.mBaseGridView.isLayoutRequested() && view != null && getAdapterPositionByView(view) == position) {
            this.mFlag |= 32;
            scrollToView(view, smooth);
            this.mFlag &= -33;
        } else if ((this.mFlag & 512) == 0 || (this.mFlag & 64) != 0) {
            this.mFocusPosition = position;
            this.mSubFocusPosition = subposition;
            this.mFocusPositionOffset = Integer.MIN_VALUE;
        } else if (smooth && !this.mBaseGridView.isLayoutRequested()) {
            this.mFocusPosition = position;
            this.mSubFocusPosition = subposition;
            this.mFocusPositionOffset = Integer.MIN_VALUE;
            if (!hasDoneFirstLayout()) {
                Log.w(getTag(), "setSelectionSmooth should not be called before first layout pass");
                return;
            }
            int position2 = startPositionSmoothScroller(position);
            if (position2 != this.mFocusPosition) {
                this.mFocusPosition = position2;
                this.mSubFocusPosition = 0;
            }
        } else {
            if (!notSmoothScrolling) {
                skipSmoothScrollerOnStopInternal();
                this.mBaseGridView.stopScroll();
            }
            if (!this.mBaseGridView.isLayoutRequested() && view != null && getAdapterPositionByView(view) == position) {
                this.mFlag |= 32;
                scrollToView(view, smooth);
                this.mFlag &= -33;
                return;
            }
            this.mFocusPosition = position;
            this.mSubFocusPosition = subposition;
            this.mFocusPositionOffset = Integer.MIN_VALUE;
            this.mFlag |= 256;
            requestLayout();
        }
    }

    int startPositionSmoothScroller(int position) {
        LinearSmoothScroller linearSmoothScroller = new GridLinearSmoothScroller() { // from class: android.support.v17.leanback.widget.GridLayoutManager.4
            @Override // android.support.v7.widget.RecyclerView.SmoothScroller
            public PointF computeScrollVectorForPosition(int targetPosition) {
                if (getChildCount() == 0) {
                    return null;
                }
                boolean z = false;
                int firstChildPos = GridLayoutManager.this.getPosition(GridLayoutManager.this.getChildAt(0));
                if ((GridLayoutManager.this.mFlag & 262144) == 0 ? targetPosition < firstChildPos : targetPosition > firstChildPos) {
                    z = true;
                }
                boolean isStart = z;
                int direction = isStart ? -1 : 1;
                if (GridLayoutManager.this.mOrientation == 0) {
                    return new PointF(direction, 0.0f);
                }
                return new PointF(0.0f, direction);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
        return linearSmoothScroller.getTargetPosition();
    }

    void skipSmoothScrollerOnStopInternal() {
        if (this.mCurrentSmoothScroller != null) {
            this.mCurrentSmoothScroller.mSkipOnStopInternal = true;
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void startSmoothScroll(RecyclerView.SmoothScroller smoothScroller) {
        skipSmoothScrollerOnStopInternal();
        super.startSmoothScroll(smoothScroller);
        if (smoothScroller.isRunning() && (smoothScroller instanceof GridLinearSmoothScroller)) {
            this.mCurrentSmoothScroller = (GridLinearSmoothScroller) smoothScroller;
            if (this.mCurrentSmoothScroller instanceof PendingMoveSmoothScroller) {
                this.mPendingMoveSmoothScroller = (PendingMoveSmoothScroller) this.mCurrentSmoothScroller;
                return;
            } else {
                this.mPendingMoveSmoothScroller = null;
                return;
            }
        }
        this.mCurrentSmoothScroller = null;
        this.mPendingMoveSmoothScroller = null;
    }

    private void processPendingMovement(boolean forward) {
        if (forward) {
            if (hasCreatedLastItem()) {
                return;
            }
        } else if (hasCreatedFirstItem()) {
            return;
        }
        if (this.mPendingMoveSmoothScroller == null) {
            this.mBaseGridView.stopScroll();
            PendingMoveSmoothScroller linearSmoothScroller = new PendingMoveSmoothScroller(forward ? 1 : -1, this.mNumRows > 1);
            this.mFocusPositionOffset = 0;
            startSmoothScroll(linearSmoothScroller);
        } else if (forward) {
            this.mPendingMoveSmoothScroller.increasePendingMoves();
        } else {
            this.mPendingMoveSmoothScroller.decreasePendingMoves();
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        if (this.mFocusPosition != -1 && this.mGrid != null && this.mGrid.getFirstVisibleIndex() >= 0 && this.mFocusPositionOffset != Integer.MIN_VALUE) {
            int pos = this.mFocusPosition + this.mFocusPositionOffset;
            if (positionStart <= pos) {
                this.mFocusPositionOffset += itemCount;
            }
        }
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsChanged(RecyclerView recyclerView) {
        this.mFocusPositionOffset = 0;
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        int pos;
        if (this.mFocusPosition != -1 && this.mGrid != null && this.mGrid.getFirstVisibleIndex() >= 0 && this.mFocusPositionOffset != Integer.MIN_VALUE && positionStart <= (pos = this.mFocusPosition + this.mFocusPositionOffset)) {
            if (positionStart + itemCount > pos) {
                this.mFocusPositionOffset += positionStart - pos;
                this.mFocusPosition += this.mFocusPositionOffset;
                this.mFocusPositionOffset = Integer.MIN_VALUE;
            } else {
                this.mFocusPositionOffset -= itemCount;
            }
        }
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsMoved(RecyclerView recyclerView, int fromPosition, int toPosition, int itemCount) {
        if (this.mFocusPosition != -1 && this.mFocusPositionOffset != Integer.MIN_VALUE) {
            int pos = this.mFocusPosition + this.mFocusPositionOffset;
            if (fromPosition <= pos && pos < fromPosition + itemCount) {
                this.mFocusPositionOffset += toPosition - fromPosition;
            } else if (fromPosition < pos && toPosition > pos - itemCount) {
                this.mFocusPositionOffset -= itemCount;
            } else if (fromPosition > pos && toPosition < pos) {
                this.mFocusPositionOffset += itemCount;
            }
        }
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        int end = positionStart + itemCount;
        for (int i = positionStart; i < end; i++) {
            this.mChildrenStates.remove(i);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean onRequestChildFocus(RecyclerView parent, View child, View focused) {
        if ((this.mFlag & 32768) == 0 && getAdapterPositionByView(child) != -1 && (this.mFlag & 35) == 0) {
            scrollToView(child, focused, true);
        }
        return true;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View view, Rect rect, boolean immediate) {
        return false;
    }

    private int getPrimaryAlignedScrollDistance(View view) {
        return this.mWindowAlignment.mainAxis().getScroll(getViewCenter(view));
    }

    private int getAdjustedPrimaryAlignedScrollDistance(int scrollPrimary, View view, View childView) {
        int subindex = getSubPositionByView(view, childView);
        if (subindex != 0) {
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            return scrollPrimary + (lp.getAlignMultiple()[subindex] - lp.getAlignMultiple()[0]);
        }
        return scrollPrimary;
    }

    private int getSecondaryScrollDistance(View view) {
        int viewCenterSecondary = getViewCenterSecondary(view);
        return this.mWindowAlignment.secondAxis().getScroll(viewCenterSecondary);
    }

    void scrollToView(View view, boolean smooth) {
        scrollToView(view, view == null ? null : view.findFocus(), smooth);
    }

    void scrollToView(View view, boolean smooth, int extraDelta, int extraDeltaSecondary) {
        scrollToView(view, view == null ? null : view.findFocus(), smooth, extraDelta, extraDeltaSecondary);
    }

    private void scrollToView(View view, View childView, boolean smooth) {
        scrollToView(view, childView, smooth, 0, 0);
    }

    private void scrollToView(View view, View childView, boolean smooth, int extraDelta, int extraDeltaSecondary) {
        if ((this.mFlag & 64) != 0) {
            return;
        }
        int newFocusPosition = getAdapterPositionByView(view);
        int newSubFocusPosition = getSubPositionByView(view, childView);
        if (newFocusPosition != this.mFocusPosition || newSubFocusPosition != this.mSubFocusPosition) {
            this.mFocusPosition = newFocusPosition;
            this.mSubFocusPosition = newSubFocusPosition;
            this.mFocusPositionOffset = 0;
            if ((this.mFlag & 3) != 1) {
                dispatchChildSelected();
            }
            if (this.mBaseGridView.isChildrenDrawingOrderEnabledInternal()) {
                this.mBaseGridView.invalidate();
            }
        }
        if (view == null) {
            return;
        }
        if (!view.hasFocus() && this.mBaseGridView.hasFocus()) {
            view.requestFocus();
        }
        if ((this.mFlag & 131072) == 0 && smooth) {
            return;
        }
        if (getScrollPosition(view, childView, sTwoInts) || extraDelta != 0 || extraDeltaSecondary != 0) {
            scrollGrid(sTwoInts[0] + extraDelta, sTwoInts[1] + extraDeltaSecondary, smooth);
        }
    }

    boolean getScrollPosition(View view, View childView, int[] deltas) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return getNoneAlignedPosition(view, deltas);
            default:
                return getAlignedPosition(view, childView, deltas);
        }
    }

    private boolean getNoneAlignedPosition(View view, int[] deltas) {
        View secondaryAlignedView;
        int pos = getAdapterPositionByView(view);
        int viewMin = getViewMin(view);
        int viewMax = getViewMax(view);
        View firstView = null;
        View lastView = null;
        int paddingMin = this.mWindowAlignment.mainAxis().getPaddingMin();
        int clientSize = this.mWindowAlignment.mainAxis().getClientSize();
        int row = this.mGrid.getRowIndex(pos);
        if (viewMin < paddingMin) {
            firstView = view;
            if (this.mFocusScrollStrategy == 2) {
                while (true) {
                    if (!prependOneColumnVisibleItems()) {
                        break;
                    }
                    CircularIntArray positions = this.mGrid.getItemPositionsInRows(this.mGrid.getFirstVisibleIndex(), pos)[row];
                    firstView = findViewByPosition(positions.get(0));
                    if (viewMax - getViewMin(firstView) > clientSize) {
                        if (positions.size() > 2) {
                            firstView = findViewByPosition(positions.get(2));
                        }
                    }
                }
            }
        } else if (viewMax > clientSize + paddingMin) {
            if (this.mFocusScrollStrategy != 2) {
                lastView = view;
            } else {
                while (true) {
                    CircularIntArray positions2 = this.mGrid.getItemPositionsInRows(pos, this.mGrid.getLastVisibleIndex())[row];
                    lastView = findViewByPosition(positions2.get(positions2.size() - 1));
                    if (getViewMax(lastView) - viewMin > clientSize) {
                        lastView = null;
                        break;
                    } else if (!appendOneColumnVisibleItems()) {
                        break;
                    }
                }
                firstView = lastView != null ? null : view;
            }
        }
        int scrollPrimary = 0;
        if (firstView != null) {
            scrollPrimary = getViewMin(firstView) - paddingMin;
        } else if (lastView != null) {
            scrollPrimary = getViewMax(lastView) - (paddingMin + clientSize);
        }
        if (firstView != null) {
            secondaryAlignedView = firstView;
        } else if (lastView != null) {
            secondaryAlignedView = lastView;
        } else {
            secondaryAlignedView = view;
        }
        int scrollSecondary = getSecondaryScrollDistance(secondaryAlignedView);
        if (scrollPrimary == 0 && scrollSecondary == 0) {
            return false;
        }
        deltas[0] = scrollPrimary;
        deltas[1] = scrollSecondary;
        return true;
    }

    private boolean getAlignedPosition(View view, View childView, int[] deltas) {
        int scrollPrimary = getPrimaryAlignedScrollDistance(view);
        if (childView != null) {
            scrollPrimary = getAdjustedPrimaryAlignedScrollDistance(scrollPrimary, view, childView);
        }
        int scrollSecondary = getSecondaryScrollDistance(view);
        int scrollPrimary2 = scrollPrimary + this.mPrimaryScrollExtra;
        if (scrollPrimary2 != 0 || scrollSecondary != 0) {
            deltas[0] = scrollPrimary2;
            deltas[1] = scrollSecondary;
            return true;
        }
        deltas[0] = 0;
        deltas[1] = 0;
        return false;
    }

    private void scrollGrid(int scrollPrimary, int scrollSecondary, boolean smooth) {
        int scrollX;
        int scrollY;
        if ((this.mFlag & 3) == 1) {
            scrollDirectionPrimary(scrollPrimary);
            scrollDirectionSecondary(scrollSecondary);
            return;
        }
        if (this.mOrientation == 0) {
            scrollX = scrollPrimary;
            scrollY = scrollSecondary;
        } else {
            scrollX = scrollSecondary;
            scrollY = scrollPrimary;
        }
        if (smooth) {
            this.mBaseGridView.smoothScrollBy(scrollX, scrollY);
            return;
        }
        this.mBaseGridView.scrollBy(scrollX, scrollY);
        dispatchChildSelectedAndPositioned();
    }

    public boolean isScrollEnabled() {
        return (this.mFlag & 131072) != 0;
    }

    private int findImmediateChildIndex(View view) {
        View view2;
        if (this.mBaseGridView != null && view != this.mBaseGridView && (view2 = findContainingItemView(view)) != null) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (getChildAt(i) == view2) {
                    return i;
                }
            }
            return -1;
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            int i = this.mFocusPosition;
            while (true) {
                View view = findViewByPosition(i);
                if (view != null) {
                    if (view.getVisibility() != 0 || !view.hasFocusable()) {
                        i++;
                    } else {
                        view.requestFocus();
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public View onInterceptFocusSearch(View focused, int direction) {
        if ((this.mFlag & 32768) != 0) {
            return focused;
        }
        FocusFinder ff = FocusFinder.getInstance();
        View result = null;
        if (direction == 2 || direction == 1) {
            if (canScrollVertically()) {
                int absDir = direction == 2 ? 130 : 33;
                result = ff.findNextFocus(this.mBaseGridView, focused, absDir);
            }
            if (canScrollHorizontally()) {
                boolean rtl = getLayoutDirection() == 1;
                int absDir2 = (direction == 2) ^ rtl ? 66 : 17;
                result = ff.findNextFocus(this.mBaseGridView, focused, absDir2);
            }
        } else {
            result = ff.findNextFocus(this.mBaseGridView, focused, direction);
        }
        if (result != null) {
            return result;
        }
        if (this.mBaseGridView.getDescendantFocusability() == 393216) {
            return this.mBaseGridView.getParent().focusSearch(focused, direction);
        }
        int movement = getMovement(direction);
        boolean isScroll = this.mBaseGridView.getScrollState() != 0;
        if (movement == 1) {
            if (isScroll || (this.mFlag & 4096) == 0) {
                result = focused;
            }
            if ((this.mFlag & 131072) != 0 && !hasCreatedLastItem()) {
                processPendingMovement(true);
                result = focused;
            }
        } else if (movement == 0) {
            if (isScroll || (this.mFlag & 2048) == 0) {
                result = focused;
            }
            if ((this.mFlag & 131072) != 0 && !hasCreatedFirstItem()) {
                processPendingMovement(false);
                result = focused;
            }
        } else if (movement == 3) {
            if (isScroll || (this.mFlag & 16384) == 0) {
                result = focused;
            }
        } else if (movement == 2 && (isScroll || (this.mFlag & 8192) == 0)) {
            result = focused;
        }
        if (result != null) {
            return result;
        }
        View result2 = this.mBaseGridView.getParent().focusSearch(focused, direction);
        if (result2 != null) {
            return result2;
        }
        return focused != null ? focused : this.mBaseGridView;
    }

    /* JADX WARN: Removed duplicated region for block: B:104:0x012a  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x00b2  */
    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        int focusedRow;
        int loop_start;
        int loop_end;
        int i;
        View child;
        View focused;
        int focusedIndex;
        int loop_start2;
        if ((this.mFlag & 32768) != 0) {
            return true;
        }
        if (recyclerView.hasFocus()) {
            if (this.mPendingMoveSmoothScroller != null) {
                return true;
            }
            int movement = getMovement(direction);
            View focused2 = recyclerView.findFocus();
            int focusedIndex2 = findImmediateChildIndex(focused2);
            int focusedPos = getAdapterPositionByIndex(focusedIndex2);
            View immediateFocusedChild = focusedPos == -1 ? null : findViewByPosition(focusedPos);
            if (immediateFocusedChild != null) {
                immediateFocusedChild.addFocusables(views, direction, focusableMode);
            }
            if (this.mGrid != null && getChildCount() != 0) {
                if ((movement == 3 || movement == 2) && this.mGrid.getNumRows() <= 1) {
                    return true;
                }
                if (this.mGrid == null || immediateFocusedChild == null) {
                    focusedRow = -1;
                } else {
                    focusedRow = this.mGrid.getLocation(focusedPos).row;
                }
                int focusableCount = views.size();
                int inc = (movement == 1 || movement == 3) ? 1 : -1;
                int loop_end2 = inc > 0 ? getChildCount() - 1 : 0;
                if (focusedIndex2 == -1) {
                    loop_start = inc <= 0 ? getChildCount() - 1 : 0;
                } else {
                    loop_start = focusedIndex2 + inc;
                }
                int i2 = loop_start;
                while (true) {
                    int i3 = i2;
                    if (inc > 0) {
                        loop_end = loop_end2;
                        i = i3;
                        if (i > loop_end) {
                            break;
                        }
                        child = getChildAt(i);
                        if (child.getVisibility() != 0) {
                            if (!child.hasFocusable()) {
                                focused = focused2;
                            } else if (immediateFocusedChild == null) {
                                child.addFocusables(views, direction, focusableMode);
                                focused = focused2;
                                if (views.size() > focusableCount) {
                                    break;
                                }
                            } else {
                                focused = focused2;
                                int position = getAdapterPositionByIndex(i);
                                focusedIndex = focusedIndex2;
                                Grid.Location loc = this.mGrid.getLocation(position);
                                if (loc != null) {
                                    loop_start2 = loop_start;
                                    if (movement == 1) {
                                        int loop_start3 = loc.row;
                                        if (loop_start3 == focusedRow && position > focusedPos) {
                                            child.addFocusables(views, direction, focusableMode);
                                            if (views.size() > focusableCount) {
                                                break;
                                            }
                                        }
                                    } else if (movement == 0) {
                                        if (loc.row == focusedRow && position < focusedPos) {
                                            child.addFocusables(views, direction, focusableMode);
                                            if (views.size() > focusableCount) {
                                                break;
                                            }
                                        }
                                    } else if (movement == 3) {
                                        if (loc.row == focusedRow) {
                                            continue;
                                        } else if (loc.row < focusedRow) {
                                            break;
                                        } else {
                                            child.addFocusables(views, direction, focusableMode);
                                        }
                                    } else if (movement == 2 && loc.row != focusedRow) {
                                        if (loc.row > focusedRow) {
                                            break;
                                        }
                                        child.addFocusables(views, direction, focusableMode);
                                    }
                                }
                                loop_start2 = loop_start;
                            }
                            focusedIndex = focusedIndex2;
                            loop_start2 = loop_start;
                        } else {
                            focused = focused2;
                            focusedIndex = focusedIndex2;
                            loop_start2 = loop_start;
                        }
                        i2 = i + inc;
                        loop_end2 = loop_end;
                        focused2 = focused;
                        focusedIndex2 = focusedIndex;
                        loop_start = loop_start2;
                    } else {
                        loop_end = loop_end2;
                        i = i3;
                        if (i < loop_end) {
                            break;
                        }
                        child = getChildAt(i);
                        if (child.getVisibility() != 0) {
                        }
                        i2 = i + inc;
                        loop_end2 = loop_end;
                        focused2 = focused;
                        focusedIndex2 = focusedIndex;
                        loop_start = loop_start2;
                    }
                }
                return true;
            }
            return true;
        }
        int focusableCount2 = views.size();
        if (this.mFocusScrollStrategy != 0) {
            int left = this.mWindowAlignment.mainAxis().getPaddingMin();
            int right = this.mWindowAlignment.mainAxis().getClientSize() + left;
            int count = getChildCount();
            for (int i4 = 0; i4 < count; i4++) {
                View child2 = getChildAt(i4);
                if (child2.getVisibility() == 0 && getViewMin(child2) >= left && getViewMax(child2) <= right) {
                    child2.addFocusables(views, direction, focusableMode);
                }
            }
            int i5 = views.size();
            if (i5 == focusableCount2) {
                int count2 = getChildCount();
                for (int i6 = 0; i6 < count2; i6++) {
                    View child3 = getChildAt(i6);
                    if (child3.getVisibility() == 0) {
                        child3.addFocusables(views, direction, focusableMode);
                    }
                }
            }
        } else {
            View view = findViewByPosition(this.mFocusPosition);
            if (view != null) {
                view.addFocusables(views, direction, focusableMode);
            }
        }
        if (views.size() == focusableCount2 && recyclerView.isFocusable()) {
            views.add(recyclerView);
            return true;
        }
        return true;
    }

    boolean hasCreatedLastItem() {
        int count = getItemCount();
        return count == 0 || this.mBaseGridView.findViewHolderForAdapterPosition(count + (-1)) != null;
    }

    boolean hasCreatedFirstItem() {
        int count = getItemCount();
        return count == 0 || this.mBaseGridView.findViewHolderForAdapterPosition(0) != null;
    }

    boolean isItemFullyVisible(int pos) {
        RecyclerView.ViewHolder vh = this.mBaseGridView.findViewHolderForAdapterPosition(pos);
        return vh != null && vh.itemView.getLeft() >= 0 && vh.itemView.getRight() <= this.mBaseGridView.getWidth() && vh.itemView.getTop() >= 0 && vh.itemView.getBottom() <= this.mBaseGridView.getHeight();
    }

    boolean canScrollTo(View view) {
        return view.getVisibility() == 0 && (!hasFocus() || view.hasFocusable());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean gridOnRequestFocusInDescendants(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return gridOnRequestFocusInDescendantsUnaligned(recyclerView, direction, previouslyFocusedRect);
            default:
                return gridOnRequestFocusInDescendantsAligned(recyclerView, direction, previouslyFocusedRect);
        }
    }

    private boolean gridOnRequestFocusInDescendantsAligned(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        View view = findViewByPosition(this.mFocusPosition);
        if (view != null) {
            boolean result = view.requestFocus(direction, previouslyFocusedRect);
            return result;
        }
        return false;
    }

    private boolean gridOnRequestFocusInDescendantsUnaligned(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        int index;
        int increment;
        int end;
        int count = getChildCount();
        if ((direction & 2) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }
        int left = this.mWindowAlignment.mainAxis().getPaddingMin();
        int right = this.mWindowAlignment.mainAxis().getClientSize() + left;
        for (int i = index; i != end; i += increment) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0 && getViewMin(child) >= left && getViewMax(child) <= right && child.requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return false;
    }

    private int getMovement(int direction) {
        if (this.mOrientation == 0) {
            if (direction == 17) {
                int movement = (this.mFlag & 262144) != 0 ? 1 : 0;
                return movement;
            } else if (direction != 33) {
                if (direction == 66) {
                    int movement2 = (this.mFlag & 262144) == 0 ? 1 : 0;
                    return movement2;
                } else if (direction != 130) {
                    return 17;
                } else {
                    return 3;
                }
            } else {
                return 2;
            }
        } else if (this.mOrientation == 1) {
            if (direction == 17) {
                int movement3 = (this.mFlag & 524288) != 0 ? 3 : 2;
                return movement3;
            } else if (direction != 33) {
                if (direction == 66) {
                    int movement4 = (this.mFlag & 524288) == 0 ? 3 : 2;
                    return movement4;
                } else if (direction != 130) {
                    return 17;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        } else {
            return 17;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getChildDrawingOrder(RecyclerView recyclerView, int childCount, int i) {
        View view = findViewByPosition(this.mFocusPosition);
        if (view == null) {
            return i;
        }
        int focusIndex = recyclerView.indexOfChild(view);
        if (i < focusIndex) {
            return i;
        }
        if (i < childCount - 1) {
            return ((focusIndex + childCount) - 1) - i;
        }
        return focusIndex;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        if (oldAdapter != null) {
            discardLayoutInfo();
            this.mFocusPosition = -1;
            this.mFocusPositionOffset = 0;
            this.mChildrenStates.clear();
        }
        if (newAdapter instanceof FacetProviderAdapter) {
            this.mFacetProviderAdapter = (FacetProviderAdapter) newAdapter;
        } else {
            this.mFacetProviderAdapter = null;
        }
        super.onAdapterChanged(oldAdapter, newAdapter);
    }

    private void discardLayoutInfo() {
        this.mGrid = null;
        this.mRowSizeSecondary = null;
        this.mFlag &= -1025;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: android.support.v17.leanback.widget.GridLayoutManager.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle childStates;
        int index;

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.index);
            out.writeBundle(this.childStates);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        SavedState(Parcel in) {
            this.childStates = Bundle.EMPTY;
            this.index = in.readInt();
            this.childStates = in.readBundle(GridLayoutManager.class.getClassLoader());
        }

        SavedState() {
            this.childStates = Bundle.EMPTY;
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState();
        ss.index = getSelection();
        Bundle bundle = this.mChildrenStates.saveAsBundle();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            int position = getAdapterPositionByView(view);
            if (position != -1) {
                bundle = this.mChildrenStates.saveOnScreenView(bundle, view, position);
            }
        }
        ss.childStates = bundle;
        return ss;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onChildRecycled(RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (position != -1) {
            this.mChildrenStates.saveOffscreenView(holder.itemView, position);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            return;
        }
        SavedState loadingState = (SavedState) state;
        this.mFocusPosition = loadingState.index;
        this.mFocusPositionOffset = 0;
        this.mChildrenStates.loadFromBundle(loadingState.childStates);
        this.mFlag |= 256;
        requestLayout();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 0 && this.mGrid != null) {
            return this.mGrid.getNumRows();
        }
        return super.getRowCountForAccessibility(recycler, state);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 1 && this.mGrid != null) {
            return this.mGrid.getNumRows();
        }
        return super.getColumnCountForAccessibility(recycler, state);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View host, AccessibilityNodeInfoCompat info) {
        ViewGroup.LayoutParams lp = host.getLayoutParams();
        if (this.mGrid == null || !(lp instanceof LayoutParams)) {
            return;
        }
        LayoutParams glp = (LayoutParams) lp;
        int position = glp.getViewAdapterPosition();
        int rowIndex = position >= 0 ? this.mGrid.getRowIndex(position) : -1;
        if (rowIndex < 0) {
            return;
        }
        int guessSpanIndex = position / this.mGrid.getNumRows();
        if (this.mOrientation == 0) {
            info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(rowIndex, 1, guessSpanIndex, 1, false, false));
        } else {
            info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(guessSpanIndex, 1, rowIndex, 1, false, false));
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean performAccessibilityAction(RecyclerView.Recycler recycler, RecyclerView.State state, int action, Bundle args) {
        if (isScrollEnabled()) {
            saveContext(recycler, state);
            int translatedAction = action;
            boolean reverseFlowPrimary = (this.mFlag & 262144) != 0;
            if (Build.VERSION.SDK_INT >= 23) {
                if (this.mOrientation == 0) {
                    if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_LEFT.getId()) {
                        translatedAction = reverseFlowPrimary ? 4096 : 8192;
                    } else if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT.getId()) {
                        translatedAction = reverseFlowPrimary ? 8192 : 4096;
                    }
                } else if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_UP.getId()) {
                    translatedAction = 8192;
                } else if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_DOWN.getId()) {
                    translatedAction = 4096;
                }
            }
            if (translatedAction == 4096) {
                processPendingMovement(true);
                processSelectionMoves(false, 1);
            } else if (translatedAction == 8192) {
                processPendingMovement(false);
                processSelectionMoves(false, -1);
            }
            leaveContext();
            return true;
        }
        return true;
    }

    int processSelectionMoves(boolean preventScroll, int moves) {
        int focusedRow;
        if (this.mGrid == null) {
            return moves;
        }
        int focusPosition = this.mFocusPosition;
        if (focusPosition == -1) {
            focusedRow = -1;
        } else {
            focusedRow = this.mGrid.getRowIndex(focusPosition);
        }
        View newSelected = null;
        int count = getChildCount();
        for (int i = 0; i < count && moves != 0; i++) {
            int index = moves > 0 ? i : (count - 1) - i;
            View child = getChildAt(index);
            if (canScrollTo(child)) {
                int position = getAdapterPositionByIndex(index);
                int rowIndex = this.mGrid.getRowIndex(position);
                if (focusedRow == -1) {
                    focusPosition = position;
                    newSelected = child;
                    focusedRow = rowIndex;
                } else if (rowIndex == focusedRow && ((moves > 0 && position > focusPosition) || (moves < 0 && position < focusPosition))) {
                    focusPosition = position;
                    newSelected = child;
                    moves = moves > 0 ? moves - 1 : moves + 1;
                }
            }
        }
        if (newSelected != null) {
            if (preventScroll) {
                if (hasFocus()) {
                    this.mFlag |= 32;
                    newSelected.requestFocus();
                    this.mFlag &= -33;
                }
                this.mFocusPosition = focusPosition;
                this.mSubFocusPosition = 0;
            } else {
                scrollToView(newSelected, true);
            }
        }
        return moves;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfo(RecyclerView.Recycler recycler, RecyclerView.State state, AccessibilityNodeInfoCompat info) {
        saveContext(recycler, state);
        int count = state.getItemCount();
        boolean reverseFlowPrimary = (this.mFlag & 262144) != 0;
        if (count > 1 && !isItemFullyVisible(0)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (this.mOrientation == 0) {
                    info.addAction(reverseFlowPrimary ? AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT : AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_LEFT);
                } else {
                    info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_UP);
                }
            } else {
                info.addAction(8192);
            }
            info.setScrollable(true);
        }
        if (count > 1 && !isItemFullyVisible(count - 1)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (this.mOrientation == 0) {
                    info.addAction(reverseFlowPrimary ? AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_LEFT : AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT);
                } else {
                    info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_DOWN);
                }
            } else {
                info.addAction(4096);
            }
            info.setScrollable(true);
        }
        AccessibilityNodeInfoCompat.CollectionInfoCompat collectionInfo = AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state));
        info.setCollectionInfo(collectionInfo);
        leaveContext();
    }
}
