package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v17.leanback.widget.Grid;
import android.support.v17.leanback.widget.ItemAlignmentFacet;
import android.support.v4.util.CircularIntArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/GridLayoutManager.class */
public final class GridLayoutManager extends RecyclerView.LayoutManager {
    private static final Rect sTempRect = new Rect();
    private static int[] sTwoInts = new int[2];
    private final BaseGridView mBaseGridView;
    private int mExtraLayoutSpace;
    private FacetProviderAdapter mFacetProviderAdapter;
    private int mFixedRowSizeSecondary;
    private boolean mFocusOutEnd;
    private boolean mFocusOutFront;
    private boolean mFocusSearchDisabled;
    private boolean mForceFullLayout;
    Grid mGrid;
    private int mHorizontalMargin;
    private boolean mInFastRelayout;
    private boolean mInLayout;
    private boolean mInLayoutSearchFocus;
    private boolean mInScroll;
    private int mMarginPrimary;
    private int mMarginSecondary;
    private int mMaxSizeSecondary;
    private int mNumRows;
    private PendingMoveSmoothScroller mPendingMoveSmoothScroller;
    private int mPrimaryScrollExtra;
    private RecyclerView.Recycler mRecycler;
    private boolean mRowSecondarySizeRefresh;
    private int[] mRowSizeSecondary;
    private int mRowSizeSecondaryRequested;
    private int mScrollOffsetPrimary;
    private int mScrollOffsetSecondary;
    private int mSizePrimary;
    private RecyclerView.State mState;
    private int mVerticalMargin;
    private int mOrientation = 0;
    private OrientationHelper mOrientationHelper = OrientationHelper.createHorizontalHelper(this);
    private boolean mInSelection = false;
    private OnChildSelectedListener mChildSelectedListener = null;
    private ArrayList<OnChildViewHolderSelectedListener> mChildViewHolderSelectedListeners = null;
    private OnChildLaidOutListener mChildLaidOutListener = null;
    private int mFocusPosition = -1;
    private int mSubFocusPosition = 0;
    private int mFocusPositionOffset = 0;
    private boolean mLayoutEnabled = true;
    private int mChildVisibility = -1;
    private int mGravity = 8388659;
    private int mNumRowsRequested = 1;
    private int mFocusScrollStrategy = 0;
    private final WindowAlignment mWindowAlignment = new WindowAlignment();
    private final ItemAlignment mItemAlignment = new ItemAlignment();
    private boolean mFocusOutSideStart = true;
    private boolean mFocusOutSideEnd = true;
    private boolean mPruneChild = true;
    private boolean mScrollEnabled = true;
    private boolean mReverseFlowPrimary = false;
    private boolean mReverseFlowSecondary = false;
    private int[] mMeasuredDimension = new int[2];
    final ViewsStateBundle mChildrenStates = new ViewsStateBundle();
    private final Runnable mRequestLayoutRunnable = new Runnable(this) { // from class: android.support.v17.leanback.widget.GridLayoutManager.1
        final GridLayoutManager this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.requestLayout();
        }
    };
    private final Runnable mAskFocusRunnable = new Runnable(this) { // from class: android.support.v17.leanback.widget.GridLayoutManager.2
        final GridLayoutManager this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.hasFocus()) {
                return;
            }
            View findViewByPosition = this.this$0.findViewByPosition(this.this$0.mFocusPosition);
            if (findViewByPosition != null && findViewByPosition.hasFocusable()) {
                this.this$0.mBaseGridView.focusableViewAvailable(findViewByPosition);
                return;
            }
            int childCount = this.this$0.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.this$0.getChildAt(i);
                if (childAt != null && childAt.hasFocusable()) {
                    this.this$0.mBaseGridView.focusableViewAvailable(childAt);
                    return;
                }
            }
        }
    };
    private Grid.Provider mGridProvider = new Grid.Provider(this) { // from class: android.support.v17.leanback.widget.GridLayoutManager.3
        final GridLayoutManager this$0;

        {
            this.this$0 = this;
        }

        /* JADX WARN: Code restructure failed: missing block: B:5:0x0015, code lost:
            if (r13 == Integer.MAX_VALUE) goto L40;
         */
        @Override // android.support.v17.leanback.widget.Grid.Provider
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public void addItem(Object obj, int i, int i2, int i3, int i4) {
            int paddingLow;
            int i5;
            int i6;
            View view = (View) obj;
            if (i4 != Integer.MIN_VALUE) {
                paddingLow = i4;
            }
            paddingLow = !this.this$0.mGrid.isReversedFlow() ? this.this$0.mWindowAlignment.mainAxis().getPaddingLow() : this.this$0.mWindowAlignment.mainAxis().getSize() - this.this$0.mWindowAlignment.mainAxis().getPaddingHigh();
            if (!this.this$0.mGrid.isReversedFlow()) {
                i5 = paddingLow;
                i6 = paddingLow + i2;
            } else {
                i5 = paddingLow - i2;
                i6 = paddingLow;
            }
            int rowStartSecondary = this.this$0.getRowStartSecondary(i3);
            int i7 = this.this$0.mScrollOffsetSecondary;
            this.this$0.mChildrenStates.loadView(view, i);
            this.this$0.layoutChild(i3, view, i5, i6, rowStartSecondary - i7);
            if (i == this.this$0.mGrid.getFirstVisibleIndex()) {
                if (this.this$0.mGrid.isReversedFlow()) {
                    this.this$0.updateScrollMax();
                } else {
                    this.this$0.updateScrollMin();
                }
            }
            if (i == this.this$0.mGrid.getLastVisibleIndex()) {
                if (this.this$0.mGrid.isReversedFlow()) {
                    this.this$0.updateScrollMin();
                } else {
                    this.this$0.updateScrollMax();
                }
            }
            if (!this.this$0.mInLayout && this.this$0.mPendingMoveSmoothScroller != null) {
                this.this$0.mPendingMoveSmoothScroller.consumePendingMovesAfterLayout();
            }
            if (this.this$0.mChildLaidOutListener != null) {
                RecyclerView.ViewHolder childViewHolder = this.this$0.mBaseGridView.getChildViewHolder(view);
                this.this$0.mChildLaidOutListener.onChildLaidOut(this.this$0.mBaseGridView, view, i, childViewHolder == null ? -1L : childViewHolder.getItemId());
            }
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int createItem(int i, boolean z, Object[] objArr) {
            View viewForPosition = this.this$0.getViewForPosition(i);
            LayoutParams layoutParams = (LayoutParams) viewForPosition.getLayoutParams();
            layoutParams.setItemAlignmentFacet((ItemAlignmentFacet) this.this$0.getFacet(this.this$0.mBaseGridView.getChildViewHolder(viewForPosition), ItemAlignmentFacet.class));
            if (!layoutParams.isItemRemoved()) {
                if (z) {
                    this.this$0.addView(viewForPosition);
                } else {
                    this.this$0.addView(viewForPosition, 0);
                }
                if (this.this$0.mChildVisibility != -1) {
                    viewForPosition.setVisibility(this.this$0.mChildVisibility);
                }
                if (this.this$0.mPendingMoveSmoothScroller != null) {
                    this.this$0.mPendingMoveSmoothScroller.consumePendingMovesBeforeLayout();
                }
                int subPositionByView = this.this$0.getSubPositionByView(viewForPosition, viewForPosition.findFocus());
                if (this.this$0.mInLayout) {
                    if (!this.this$0.mInFastRelayout) {
                        if (!this.this$0.mInLayoutSearchFocus && i == this.this$0.mFocusPosition && subPositionByView == this.this$0.mSubFocusPosition) {
                            this.this$0.dispatchChildSelected();
                        } else if (this.this$0.mInLayoutSearchFocus && i >= this.this$0.mFocusPosition && viewForPosition.hasFocusable()) {
                            this.this$0.mFocusPosition = i;
                            this.this$0.mSubFocusPosition = subPositionByView;
                            this.this$0.mInLayoutSearchFocus = false;
                            this.this$0.dispatchChildSelected();
                        }
                    }
                } else if (i == this.this$0.mFocusPosition && subPositionByView == this.this$0.mSubFocusPosition && this.this$0.mPendingMoveSmoothScroller == null) {
                    this.this$0.dispatchChildSelected();
                }
                this.this$0.measureChild(viewForPosition);
            }
            objArr[0] = viewForPosition;
            return this.this$0.mOrientation == 0 ? this.this$0.getDecoratedMeasuredWidthWithMargin(viewForPosition) : this.this$0.getDecoratedMeasuredHeightWithMargin(viewForPosition);
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int getCount() {
            return this.this$0.mState.getItemCount();
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int getEdge(int i) {
            return this.this$0.mReverseFlowPrimary ? this.this$0.getViewMax(this.this$0.findViewByPosition(i)) : this.this$0.getViewMin(this.this$0.findViewByPosition(i));
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public int getSize(int i) {
            return this.this$0.getViewPrimarySize(this.this$0.findViewByPosition(i));
        }

        @Override // android.support.v17.leanback.widget.Grid.Provider
        public void removeItem(int i) {
            View findViewByPosition = this.this$0.findViewByPosition(i);
            if (this.this$0.mInLayout) {
                this.this$0.detachAndScrapView(findViewByPosition, this.this$0.mRecycler);
            } else {
                this.this$0.removeAndRecycleView(findViewByPosition, this.this$0.mRecycler);
            }
        }
    };

    /* loaded from: a.zip:android/support/v17/leanback/widget/GridLayoutManager$GridLinearSmoothScroller.class */
    abstract class GridLinearSmoothScroller extends LinearSmoothScroller {
        final GridLayoutManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        GridLinearSmoothScroller(GridLayoutManager gridLayoutManager) {
            super(gridLayoutManager.mBaseGridView.getContext());
            this.this$0 = gridLayoutManager;
        }

        @Override // android.support.v7.widget.LinearSmoothScroller
        protected int calculateTimeForScrolling(int i) {
            int calculateTimeForScrolling = super.calculateTimeForScrolling(i);
            int i2 = calculateTimeForScrolling;
            if (this.this$0.mWindowAlignment.mainAxis().getSize() > 0) {
                float size = (30.0f / this.this$0.mWindowAlignment.mainAxis().getSize()) * i;
                i2 = calculateTimeForScrolling;
                if (calculateTimeForScrolling < size) {
                    i2 = (int) size;
                }
            }
            return i2;
        }

        @Override // android.support.v7.widget.LinearSmoothScroller, android.support.v7.widget.RecyclerView.SmoothScroller
        protected void onStop() {
            View findViewByPosition = findViewByPosition(getTargetPosition());
            if (findViewByPosition == null) {
                if (getTargetPosition() >= 0) {
                    this.this$0.scrollToSelection(getTargetPosition(), 0, false, 0);
                }
                super.onStop();
                return;
            }
            if (this.this$0.hasFocus()) {
                this.this$0.mInSelection = true;
                findViewByPosition.requestFocus();
                this.this$0.mInSelection = false;
            }
            this.this$0.dispatchChildSelected();
            super.onStop();
        }

        @Override // android.support.v7.widget.LinearSmoothScroller, android.support.v7.widget.RecyclerView.SmoothScroller
        protected void onTargetFound(View view, RecyclerView.State state, RecyclerView.SmoothScroller.Action action) {
            int i;
            int i2;
            if (this.this$0.getScrollPosition(view, null, GridLayoutManager.sTwoInts)) {
                if (this.this$0.mOrientation == 0) {
                    i = GridLayoutManager.sTwoInts[0];
                    i2 = GridLayoutManager.sTwoInts[1];
                } else {
                    i = GridLayoutManager.sTwoInts[1];
                    i2 = GridLayoutManager.sTwoInts[0];
                }
                action.update(i, i2, calculateTimeForDeceleration((int) Math.sqrt((i * i) + (i2 * i2))), this.mDecelerateInterpolator);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/GridLayoutManager$LayoutParams.class */
    public static final class LayoutParams extends RecyclerView.LayoutParams {
        private int[] mAlignMultiple;
        private int mAlignX;
        private int mAlignY;
        private ItemAlignmentFacet mAlignmentFacet;
        private int mBottomInset;
        private int mLeftInset;
        private int mRightInset;
        private int mTopInset;

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((RecyclerView.LayoutParams) layoutParams);
        }

        public LayoutParams(RecyclerView.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
        }

        void calculateItemAlignments(int i, View view) {
            ItemAlignmentFacet.ItemAlignmentDef[] alignmentDefs = this.mAlignmentFacet.getAlignmentDefs();
            if (this.mAlignMultiple == null || this.mAlignMultiple.length != alignmentDefs.length) {
                this.mAlignMultiple = new int[alignmentDefs.length];
            }
            for (int i2 = 0; i2 < alignmentDefs.length; i2++) {
                this.mAlignMultiple[i2] = ItemAlignmentFacetHelper.getAlignmentPosition(view, alignmentDefs[i2], i);
            }
            if (i == 0) {
                this.mAlignX = this.mAlignMultiple[0];
            } else {
                this.mAlignY = this.mAlignMultiple[0];
            }
        }

        int[] getAlignMultiple() {
            return this.mAlignMultiple;
        }

        int getAlignX() {
            return this.mAlignX;
        }

        int getAlignY() {
            return this.mAlignY;
        }

        ItemAlignmentFacet getItemAlignmentFacet() {
            return this.mAlignmentFacet;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalHeight(View view) {
            return (view.getHeight() - this.mTopInset) - this.mBottomInset;
        }

        int getOpticalLeft(View view) {
            return view.getLeft() + this.mLeftInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalLeftInset() {
            return this.mLeftInset;
        }

        int getOpticalRight(View view) {
            return view.getRight() - this.mRightInset;
        }

        int getOpticalTop(View view) {
            return view.getTop() + this.mTopInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalTopInset() {
            return this.mTopInset;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public int getOpticalWidth(View view) {
            return (view.getWidth() - this.mLeftInset) - this.mRightInset;
        }

        void setAlignX(int i) {
            this.mAlignX = i;
        }

        void setAlignY(int i) {
            this.mAlignY = i;
        }

        void setItemAlignmentFacet(ItemAlignmentFacet itemAlignmentFacet) {
            this.mAlignmentFacet = itemAlignmentFacet;
        }

        void setOpticalInsets(int i, int i2, int i3, int i4) {
            this.mLeftInset = i;
            this.mTopInset = i2;
            this.mRightInset = i3;
            this.mBottomInset = i4;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/GridLayoutManager$PendingMoveSmoothScroller.class */
    public final class PendingMoveSmoothScroller extends GridLinearSmoothScroller {
        private int mPendingMoves;
        private final boolean mStaggeredGrid;
        final GridLayoutManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        PendingMoveSmoothScroller(GridLayoutManager gridLayoutManager, int i, boolean z) {
            super(gridLayoutManager);
            this.this$0 = gridLayoutManager;
            this.mPendingMoves = i;
            this.mStaggeredGrid = z;
            setTargetPosition(-2);
        }

        @Override // android.support.v7.widget.LinearSmoothScroller
        public PointF computeScrollVectorForPosition(int i) {
            if (this.mPendingMoves == 0) {
                return null;
            }
            int i2 = (!this.this$0.mReverseFlowPrimary ? this.mPendingMoves >= 0 : this.mPendingMoves <= 0) ? -1 : 1;
            return this.this$0.mOrientation == 0 ? new PointF(i2, 0.0f) : new PointF(0.0f, i2);
        }

        void consumePendingMovesAfterLayout() {
            if (this.mStaggeredGrid && this.mPendingMoves != 0) {
                this.mPendingMoves = this.this$0.processSelectionMoves(true, this.mPendingMoves);
            }
            if (this.mPendingMoves == 0 || ((this.mPendingMoves > 0 && this.this$0.hasCreatedLastItem()) || (this.mPendingMoves < 0 && this.this$0.hasCreatedFirstItem()))) {
                setTargetPosition(this.this$0.mFocusPosition);
                stop();
            }
        }

        void consumePendingMovesBeforeLayout() {
            View findViewByPosition;
            if (this.mStaggeredGrid || this.mPendingMoves == 0) {
                return;
            }
            View view = null;
            int i = this.mPendingMoves > 0 ? this.this$0.mFocusPosition + this.this$0.mNumRows : this.this$0.mFocusPosition - this.this$0.mNumRows;
            while (true) {
                int i2 = i;
                if (this.mPendingMoves == 0 || (findViewByPosition = findViewByPosition(i2)) == null) {
                    break;
                }
                if (this.this$0.canScrollTo(findViewByPosition)) {
                    view = findViewByPosition;
                    this.this$0.mFocusPosition = i2;
                    this.this$0.mSubFocusPosition = 0;
                    if (this.mPendingMoves > 0) {
                        this.mPendingMoves--;
                    } else {
                        this.mPendingMoves++;
                    }
                }
                i = this.mPendingMoves > 0 ? i2 + this.this$0.mNumRows : i2 - this.this$0.mNumRows;
            }
            if (view == null || !this.this$0.hasFocus()) {
                return;
            }
            this.this$0.mInSelection = true;
            view.requestFocus();
            this.this$0.mInSelection = false;
        }

        void decreasePendingMoves() {
            if (this.mPendingMoves > -10) {
                this.mPendingMoves--;
            }
        }

        void increasePendingMoves() {
            if (this.mPendingMoves < 10) {
                this.mPendingMoves++;
            }
        }

        @Override // android.support.v17.leanback.widget.GridLayoutManager.GridLinearSmoothScroller, android.support.v7.widget.LinearSmoothScroller, android.support.v7.widget.RecyclerView.SmoothScroller
        protected void onStop() {
            super.onStop();
            this.mPendingMoves = 0;
            this.this$0.mPendingMoveSmoothScroller = null;
            View findViewByPosition = findViewByPosition(getTargetPosition());
            if (findViewByPosition != null) {
                this.this$0.scrollToView(findViewByPosition, true);
            }
        }

        @Override // android.support.v7.widget.LinearSmoothScroller
        protected void updateActionForInterimTarget(RecyclerView.SmoothScroller.Action action) {
            if (this.mPendingMoves == 0) {
                return;
            }
            super.updateActionForInterimTarget(action);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/GridLayoutManager$SavedState.class */
    public static final class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: android.support.v17.leanback.widget.GridLayoutManager.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        Bundle childStates;
        int index;

        SavedState() {
            this.childStates = Bundle.EMPTY;
        }

        SavedState(Parcel parcel) {
            this.childStates = Bundle.EMPTY;
            this.index = parcel.readInt();
            this.childStates = parcel.readBundle(GridLayoutManager.class.getClassLoader());
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(this.index);
            parcel.writeBundle(this.childStates);
        }
    }

    public GridLayoutManager(BaseGridView baseGridView) {
        this.mBaseGridView = baseGridView;
    }

    private boolean appendOneColumnVisibleItems() {
        return this.mGrid.appendOneColumnVisibleItems();
    }

    private void appendVisibleItems() {
        this.mGrid.appendVisibleItems(this.mReverseFlowPrimary ? -this.mExtraLayoutSpace : this.mSizePrimary + this.mExtraLayoutSpace);
    }

    private void discardLayoutInfo() {
        this.mGrid = null;
        this.mRowSizeSecondary = null;
        this.mRowSecondarySizeRefresh = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChildSelected() {
        if (this.mChildSelectedListener != null || hasOnChildViewHolderSelectedListener()) {
            View findViewByPosition = this.mFocusPosition == -1 ? null : findViewByPosition(this.mFocusPosition);
            if (findViewByPosition != null) {
                RecyclerView.ViewHolder childViewHolder = this.mBaseGridView.getChildViewHolder(findViewByPosition);
                if (this.mChildSelectedListener != null) {
                    this.mChildSelectedListener.onChildSelected(this.mBaseGridView, findViewByPosition, this.mFocusPosition, childViewHolder == null ? -1L : childViewHolder.getItemId());
                }
                fireOnChildViewHolderSelected(this.mBaseGridView, childViewHolder, this.mFocusPosition, this.mSubFocusPosition);
            } else {
                if (this.mChildSelectedListener != null) {
                    this.mChildSelectedListener.onChildSelected(this.mBaseGridView, null, -1, -1L);
                }
                fireOnChildViewHolderSelected(this.mBaseGridView, null, -1, 0);
            }
            if (this.mInLayout || this.mBaseGridView.isLayoutRequested()) {
                return;
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i).isLayoutRequested()) {
                    forceRequestLayout();
                    return;
                }
            }
        }
    }

    private void fastRelayout() {
        boolean z;
        int decoratedMeasuredHeightWithMargin;
        int i;
        int childCount = getChildCount();
        int i2 = -1;
        int i3 = 0;
        while (true) {
            z = false;
            if (i3 >= childCount) {
                break;
            }
            View childAt = getChildAt(i3);
            i2 = getPositionByIndex(i3);
            Grid.Location location = this.mGrid.getLocation(i2);
            if (location == null) {
                z = true;
                break;
            }
            int rowStartSecondary = getRowStartSecondary(location.row);
            int i4 = this.mScrollOffsetSecondary;
            int viewMin = getViewMin(childAt);
            int viewPrimarySize = getViewPrimarySize(childAt);
            View view = childAt;
            if (((LayoutParams) childAt.getLayoutParams()).viewNeedsUpdate()) {
                int indexOfChild = this.mBaseGridView.indexOfChild(childAt);
                detachAndScrapView(childAt, this.mRecycler);
                view = getViewForPosition(i2);
                addView(view, indexOfChild);
            }
            measureChild(view);
            if (this.mOrientation == 0) {
                decoratedMeasuredHeightWithMargin = getDecoratedMeasuredWidthWithMargin(view);
                i = viewMin + decoratedMeasuredHeightWithMargin;
            } else {
                decoratedMeasuredHeightWithMargin = getDecoratedMeasuredHeightWithMargin(view);
                i = viewMin + decoratedMeasuredHeightWithMargin;
            }
            layoutChild(location.row, view, viewMin, i, rowStartSecondary - i4);
            if (viewPrimarySize != decoratedMeasuredHeightWithMargin) {
                z = true;
                break;
            }
            i3++;
        }
        if (z) {
            int lastVisibleIndex = this.mGrid.getLastVisibleIndex();
            this.mGrid.invalidateItemsAfter(i2);
            if (this.mPruneChild) {
                appendVisibleItems();
                if (this.mFocusPosition >= 0 && this.mFocusPosition <= lastVisibleIndex) {
                    while (this.mGrid.getLastVisibleIndex() < this.mFocusPosition) {
                        this.mGrid.appendOneColumnVisibleItems();
                    }
                }
            } else {
                while (this.mGrid.appendOneColumnVisibleItems() && this.mGrid.getLastVisibleIndex() < lastVisibleIndex) {
                }
            }
        }
        updateScrollMin();
        updateScrollMax();
        updateScrollSecondAxis();
    }

    private int findImmediateChildIndex(View view) {
        View findContainingItemView;
        if (this.mBaseGridView == null || view == this.mBaseGridView || (findContainingItemView = findContainingItemView(view)) == null) {
            return -1;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) == findContainingItemView) {
                return i;
            }
        }
        return -1;
    }

    private void forceRequestLayout() {
        ViewCompat.postOnAnimation(this.mBaseGridView, this.mRequestLayoutRunnable);
    }

    private int getAdjustedPrimaryScrollPosition(int i, View view, View view2) {
        int subPositionByView = getSubPositionByView(view, view2);
        int i2 = i;
        if (subPositionByView != 0) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            i2 = i + (layoutParams.getAlignMultiple()[subPositionByView] - layoutParams.getAlignMultiple()[0]);
        }
        return i2;
    }

    private boolean getAlignedPosition(View view, View view2, int[] iArr) {
        int primarySystemScrollPosition = getPrimarySystemScrollPosition(view);
        int i = primarySystemScrollPosition;
        if (view2 != null) {
            i = getAdjustedPrimaryScrollPosition(primarySystemScrollPosition, view, view2);
        }
        int secondarySystemScrollPosition = getSecondarySystemScrollPosition(view);
        int i2 = this.mScrollOffsetPrimary;
        int i3 = secondarySystemScrollPosition - this.mScrollOffsetSecondary;
        int i4 = (i - i2) + this.mPrimaryScrollExtra;
        if (i4 == 0 && i3 == 0) {
            return false;
        }
        iArr[0] = i4;
        iArr[1] = i3;
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public <E> E getFacet(RecyclerView.ViewHolder viewHolder, Class<? extends E> cls) {
        E e = null;
        if (viewHolder instanceof FacetProvider) {
            e = ((FacetProvider) viewHolder).getFacet(cls);
        }
        E e2 = e;
        if (e == null) {
            e2 = e;
            if (this.mFacetProviderAdapter != null) {
                FacetProvider facetProvider = this.mFacetProviderAdapter.getFacetProvider(viewHolder.getItemViewType());
                e2 = e;
                if (facetProvider != null) {
                    e2 = facetProvider.getFacet(cls);
                }
            }
        }
        return e2;
    }

    private int getMovement(int i) {
        int i2;
        if (this.mOrientation != 0) {
            i2 = 17;
            if (this.mOrientation == 1) {
                switch (i) {
                    case 17:
                        if (!this.mReverseFlowSecondary) {
                            i2 = 2;
                            break;
                        } else {
                            i2 = 3;
                            break;
                        }
                    case 33:
                        i2 = 0;
                        break;
                    case 66:
                        if (!this.mReverseFlowSecondary) {
                            i2 = 3;
                            break;
                        } else {
                            i2 = 2;
                            break;
                        }
                    case 130:
                        i2 = 1;
                        break;
                    default:
                        i2 = 17;
                        break;
                }
            }
        } else {
            switch (i) {
                case 17:
                    if (!this.mReverseFlowPrimary) {
                        i2 = 0;
                        break;
                    } else {
                        i2 = 1;
                        break;
                    }
                case 33:
                    i2 = 2;
                    break;
                case 66:
                    if (!this.mReverseFlowPrimary) {
                        i2 = 1;
                        break;
                    } else {
                        i2 = 0;
                        break;
                    }
                case 130:
                    i2 = 3;
                    break;
                default:
                    i2 = 17;
                    break;
            }
        }
        return i2;
    }

    private boolean getNoneAlignedPosition(View view, int[] iArr) {
        View view2;
        View view3;
        View findViewByPosition;
        int positionByView = getPositionByView(view);
        int viewMin = getViewMin(view);
        int viewMax = getViewMax(view);
        int paddingLow = this.mWindowAlignment.mainAxis().getPaddingLow();
        int clientSize = this.mWindowAlignment.mainAxis().getClientSize();
        int rowIndex = this.mGrid.getRowIndex(positionByView);
        if (viewMin < paddingLow) {
            view2 = view;
            view3 = null;
            if (this.mFocusScrollStrategy == 2) {
                view2 = view;
                while (true) {
                    view3 = null;
                    if (!prependOneColumnVisibleItems()) {
                        break;
                    }
                    CircularIntArray circularIntArray = this.mGrid.getItemPositionsInRows(this.mGrid.getFirstVisibleIndex(), positionByView)[rowIndex];
                    View findViewByPosition2 = findViewByPosition(circularIntArray.get(0));
                    view2 = findViewByPosition2;
                    if (viewMax - getViewMin(findViewByPosition2) > clientSize) {
                        view2 = findViewByPosition2;
                        view3 = null;
                        if (circularIntArray.size() > 2) {
                            view2 = findViewByPosition(circularIntArray.get(2));
                            view3 = null;
                        }
                    }
                }
            }
        } else {
            view2 = null;
            view3 = null;
            if (viewMax > clientSize + paddingLow) {
                if (this.mFocusScrollStrategy == 2) {
                    view2 = view;
                    while (true) {
                        CircularIntArray circularIntArray2 = this.mGrid.getItemPositionsInRows(positionByView, this.mGrid.getLastVisibleIndex())[rowIndex];
                        findViewByPosition = findViewByPosition(circularIntArray2.get(circularIntArray2.size() - 1));
                        if (getViewMax(findViewByPosition) - viewMin <= clientSize) {
                            if (!appendOneColumnVisibleItems()) {
                                break;
                            }
                        } else {
                            findViewByPosition = null;
                            break;
                        }
                    }
                    view3 = findViewByPosition;
                    if (findViewByPosition != null) {
                        view2 = null;
                        view3 = findViewByPosition;
                    }
                } else {
                    view3 = view;
                    view2 = null;
                }
            }
        }
        int i = 0;
        if (view2 != null) {
            i = getViewMin(view2) - paddingLow;
        } else if (view3 != null) {
            i = getViewMax(view3) - (paddingLow + clientSize);
        }
        if (view2 != null) {
            view = view2;
        } else if (view3 != null) {
            view = view3;
        }
        int secondarySystemScrollPosition = getSecondarySystemScrollPosition(view) - this.mScrollOffsetSecondary;
        if (i == 0 && secondarySystemScrollPosition == 0) {
            return false;
        }
        iArr[0] = i;
        iArr[1] = secondarySystemScrollPosition;
        return true;
    }

    private int getPositionByIndex(int i) {
        return getPositionByView(getChildAt(i));
    }

    private int getPositionByView(View view) {
        LayoutParams layoutParams;
        if (view == null || (layoutParams = (LayoutParams) view.getLayoutParams()) == null || layoutParams.isItemRemoved()) {
            return -1;
        }
        return layoutParams.getViewPosition();
    }

    private int getPrimarySystemScrollPosition(View view) {
        boolean z;
        boolean z2;
        int i = this.mScrollOffsetPrimary;
        int viewCenter = getViewCenter(view);
        int viewMin = getViewMin(view);
        int viewMax = getViewMax(view);
        if (this.mReverseFlowPrimary) {
            z = this.mGrid.getFirstVisibleIndex() == 0;
            z2 = this.mGrid.getLastVisibleIndex() == (this.mState == null ? getItemCount() : this.mState.getItemCount()) - 1;
        } else {
            z2 = this.mGrid.getFirstVisibleIndex() == 0;
            z = this.mGrid.getLastVisibleIndex() == (this.mState == null ? getItemCount() : this.mState.getItemCount()) - 1;
        }
        int childCount = getChildCount() - 1;
        boolean z3 = z;
        while (true) {
            if ((z2 || z3) && childCount >= 0) {
                View childAt = getChildAt(childCount);
                boolean z4 = z3;
                boolean z5 = z2;
                if (childAt != view) {
                    if (childAt == null) {
                        z5 = z2;
                        z4 = z3;
                    } else {
                        boolean z6 = z2;
                        if (z2) {
                            z6 = z2;
                            if (getViewMin(childAt) < viewMin) {
                                z6 = false;
                            }
                        }
                        z4 = z3;
                        z5 = z6;
                        if (z3) {
                            z4 = z3;
                            z5 = z6;
                            if (getViewMax(childAt) > viewMax) {
                                z4 = false;
                                z5 = z6;
                            }
                        }
                    }
                }
                childCount--;
                z3 = z4;
                z2 = z5;
            }
        }
        return this.mWindowAlignment.mainAxis().getSystemScrollPos(i + viewCenter, z2, z3);
    }

    private int getPrimarySystemScrollPositionOfChildMax(View view) {
        int primarySystemScrollPosition = getPrimarySystemScrollPosition(view);
        int[] alignMultiple = ((LayoutParams) view.getLayoutParams()).getAlignMultiple();
        int i = primarySystemScrollPosition;
        if (alignMultiple != null) {
            i = primarySystemScrollPosition;
            if (alignMultiple.length > 0) {
                i = primarySystemScrollPosition + (alignMultiple[alignMultiple.length - 1] - alignMultiple[0]);
            }
        }
        return i;
    }

    private int getRowSizeSecondary(int i) {
        if (this.mFixedRowSizeSecondary != 0) {
            return this.mFixedRowSizeSecondary;
        }
        if (this.mRowSizeSecondary == null) {
            return 0;
        }
        return this.mRowSizeSecondary[i];
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getRowStartSecondary(int i) {
        int i2;
        int i3 = 0;
        if (!this.mReverseFlowSecondary) {
            int i4 = 0;
            int i5 = 0;
            while (true) {
                i2 = i5;
                if (i4 >= i) {
                    break;
                }
                i5 += getRowSizeSecondary(i4) + this.mMarginSecondary;
                i4++;
            }
        } else {
            int i6 = this.mNumRows - 1;
            while (true) {
                i2 = i3;
                if (i6 <= i) {
                    break;
                }
                i3 += getRowSizeSecondary(i6) + this.mMarginSecondary;
                i6--;
            }
        }
        return i2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean getScrollPosition(View view, View view2, int[] iArr) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return getNoneAlignedPosition(view, iArr);
            default:
                return getAlignedPosition(view, view2, iArr);
        }
    }

    private int getSecondarySystemScrollPosition(View view) {
        boolean z;
        boolean z2;
        int i = this.mScrollOffsetSecondary;
        int viewCenterSecondary = getViewCenterSecondary(view);
        int i2 = this.mGrid.getLocation(getPositionByView(view)).row;
        if (this.mReverseFlowSecondary) {
            z = i2 == 0;
            z2 = i2 == this.mGrid.getNumRows() - 1;
        } else {
            z2 = i2 == 0;
            z = i2 == this.mGrid.getNumRows() - 1;
        }
        return this.mWindowAlignment.secondAxis().getSystemScrollPos(i + viewCenterSecondary, z2, z);
    }

    private int getSizeSecondary() {
        int i = this.mReverseFlowSecondary ? 0 : this.mNumRows - 1;
        return getRowStartSecondary(i) + getRowSizeSecondary(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getSubPositionByView(View view, View view2) {
        ItemAlignmentFacet itemAlignmentFacet;
        if (view == null || view2 == null || (itemAlignmentFacet = ((LayoutParams) view.getLayoutParams()).getItemAlignmentFacet()) == null) {
            return 0;
        }
        ItemAlignmentFacet.ItemAlignmentDef[] alignmentDefs = itemAlignmentFacet.getAlignmentDefs();
        if (alignmentDefs.length > 1) {
            while (view2 != view) {
                int id = view2.getId();
                if (id != -1) {
                    for (int i = 1; i < alignmentDefs.length; i++) {
                        if (alignmentDefs[i].getItemAlignmentFocusViewId() == id) {
                            return i;
                        }
                    }
                    continue;
                }
                view2 = (View) view2.getParent();
            }
            return 0;
        }
        return 0;
    }

    private String getTag() {
        return "GridLayoutManager:" + this.mBaseGridView.getId();
    }

    private int getViewCenter(View view) {
        return this.mOrientation == 0 ? getViewCenterX(view) : getViewCenterY(view);
    }

    private int getViewCenterSecondary(View view) {
        return this.mOrientation == 0 ? getViewCenterY(view) : getViewCenterX(view);
    }

    private int getViewCenterX(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        return layoutParams.getOpticalLeft(view) + layoutParams.getAlignX();
    }

    private int getViewCenterY(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        return layoutParams.getOpticalTop(view) + layoutParams.getAlignY();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getViewMax(View view) {
        return this.mOrientationHelper.getDecoratedEnd(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getViewMin(View view) {
        return this.mOrientationHelper.getDecoratedStart(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getViewPrimarySize(View view) {
        getDecoratedBoundsWithMargins(view, sTempRect);
        return this.mOrientation == 0 ? sTempRect.width() : sTempRect.height();
    }

    private boolean gridOnRequestFocusInDescendantsAligned(RecyclerView recyclerView, int i, Rect rect) {
        View findViewByPosition = findViewByPosition(this.mFocusPosition);
        if (findViewByPosition != null) {
            boolean requestFocus = findViewByPosition.requestFocus(i, rect);
            if (!requestFocus) {
            }
            return requestFocus;
        }
        return false;
    }

    private boolean gridOnRequestFocusInDescendantsUnaligned(RecyclerView recyclerView, int i, Rect rect) {
        int i2;
        int i3;
        int childCount = getChildCount();
        if ((i & 2) != 0) {
            i2 = 0;
            i3 = 1;
        } else {
            i2 = childCount - 1;
            i3 = -1;
            childCount = -1;
        }
        int paddingLow = this.mWindowAlignment.mainAxis().getPaddingLow();
        int clientSize = this.mWindowAlignment.mainAxis().getClientSize();
        while (i2 != childCount) {
            View childAt = getChildAt(i2);
            if (childAt.getVisibility() == 0 && getViewMin(childAt) >= paddingLow && getViewMax(childAt) <= clientSize + paddingLow && childAt.requestFocus(i, rect)) {
                return true;
            }
            i2 += i3;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasCreatedFirstItem() {
        boolean z = true;
        if (getItemCount() != 0) {
            z = this.mBaseGridView.findViewHolderForAdapterPosition(0) != null;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasCreatedLastItem() {
        int itemCount = getItemCount();
        boolean z = true;
        if (itemCount != 0) {
            z = this.mBaseGridView.findViewHolderForAdapterPosition(itemCount - 1) != null;
        }
        return z;
    }

    private void initScrollController() {
        this.mWindowAlignment.reset();
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
        this.mScrollOffsetPrimary = -this.mWindowAlignment.mainAxis().getPaddingLow();
        this.mScrollOffsetSecondary = -this.mWindowAlignment.secondAxis().getPaddingLow();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x00e3, code lost:
        if (r16 != 3) goto L28;
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x0145, code lost:
        if (r16 == 1) goto L41;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void layoutChild(int i, View view, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int decoratedMeasuredHeightWithMargin = this.mOrientation == 0 ? getDecoratedMeasuredHeightWithMargin(view) : getDecoratedMeasuredWidthWithMargin(view);
        int i9 = decoratedMeasuredHeightWithMargin;
        if (this.mFixedRowSizeSecondary > 0) {
            i9 = Math.min(decoratedMeasuredHeightWithMargin, this.mFixedRowSizeSecondary);
        }
        int i10 = this.mGravity & 112;
        int absoluteGravity = (this.mReverseFlowPrimary || this.mReverseFlowSecondary) ? Gravity.getAbsoluteGravity(this.mGravity & 8388615, 1) : this.mGravity & 7;
        if (this.mOrientation == 0 && i10 == 48) {
            i5 = i4;
        } else {
            if (this.mOrientation == 1) {
                i5 = i4;
            }
            if ((this.mOrientation == 0 && i10 == 80) || (this.mOrientation == 1 && absoluteGravity == 5)) {
                i5 = i4 + (getRowSizeSecondary(i) - i9);
            } else {
                if (this.mOrientation != 0 || i10 != 16) {
                    i5 = i4;
                    if (this.mOrientation == 1) {
                        i5 = i4;
                    }
                }
                i5 = i4 + ((getRowSizeSecondary(i) - i9) / 2);
            }
        }
        if (this.mOrientation == 0) {
            i8 = i5;
            i6 = i3;
            i3 = i5 + i9;
            i7 = i2;
        } else {
            i6 = i5 + i9;
            i7 = i5;
            i8 = i2;
        }
        layoutDecoratedWithMargins(view, i7, i8, i6, i3);
        super.getDecoratedBoundsWithMargins(view, sTempRect);
        ((LayoutParams) view.getLayoutParams()).setOpticalInsets(i7 - sTempRect.left, i8 - sTempRect.top, sTempRect.right - i6, sTempRect.bottom - i3);
        updateChildAlignments(view);
    }

    private boolean layoutInit() {
        boolean z = (this.mGrid == null || this.mFocusPosition < 0 || this.mFocusPosition < this.mGrid.getFirstVisibleIndex()) ? false : this.mFocusPosition <= this.mGrid.getLastVisibleIndex();
        int itemCount = this.mState.getItemCount();
        if (itemCount == 0) {
            this.mFocusPosition = -1;
            this.mSubFocusPosition = 0;
        } else if (this.mFocusPosition >= itemCount) {
            this.mFocusPosition = itemCount - 1;
            this.mSubFocusPosition = 0;
        } else if (this.mFocusPosition == -1 && itemCount > 0) {
            this.mFocusPosition = 0;
            this.mSubFocusPosition = 0;
        }
        if (!this.mState.didStructureChange() && this.mGrid.getFirstVisibleIndex() >= 0 && !this.mForceFullLayout && this.mGrid != null && this.mGrid.getNumRows() == this.mNumRows) {
            updateScrollController();
            updateScrollSecondAxis();
            this.mGrid.setMargin(this.mMarginPrimary);
            if (z || this.mFocusPosition == -1) {
                return true;
            }
            this.mGrid.setStart(this.mFocusPosition);
            return true;
        }
        this.mForceFullLayout = false;
        int firstVisibleIndex = z ? this.mGrid.getFirstVisibleIndex() : 0;
        if (this.mGrid == null || this.mNumRows != this.mGrid.getNumRows() || this.mReverseFlowPrimary != this.mGrid.isReversedFlow()) {
            this.mGrid = Grid.createGrid(this.mNumRows);
            this.mGrid.setProvider(this.mGridProvider);
            this.mGrid.setReversedFlow(this.mReverseFlowPrimary);
        }
        initScrollController();
        updateScrollSecondAxis();
        this.mGrid.setMargin(this.mMarginPrimary);
        detachAndScrapAttachedViews(this.mRecycler);
        this.mGrid.resetVisibleIndex();
        if (this.mFocusPosition == -1) {
            this.mBaseGridView.clearFocus();
        }
        this.mWindowAlignment.mainAxis().invalidateScrollMin();
        this.mWindowAlignment.mainAxis().invalidateScrollMax();
        if (!z || firstVisibleIndex > this.mFocusPosition) {
            this.mGrid.setStart(this.mFocusPosition);
            return false;
        }
        this.mGrid.setStart(firstVisibleIndex);
        return false;
    }

    private void leaveContext() {
        this.mRecycler = null;
        this.mState = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void measureChild(View view) {
        int childMeasureSpec;
        int childMeasureSpec2;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        calculateItemDecorationsForChild(view, sTempRect);
        int i = layoutParams.leftMargin + layoutParams.rightMargin + sTempRect.left + sTempRect.right;
        int i2 = layoutParams.topMargin + layoutParams.bottomMargin + sTempRect.top + sTempRect.bottom;
        int makeMeasureSpec = this.mRowSizeSecondaryRequested == -2 ? View.MeasureSpec.makeMeasureSpec(0, 0) : View.MeasureSpec.makeMeasureSpec(this.mFixedRowSizeSecondary, 1073741824);
        if (this.mOrientation == 0) {
            childMeasureSpec2 = ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(0, 0), i, layoutParams.width);
            childMeasureSpec = ViewGroup.getChildMeasureSpec(makeMeasureSpec, i2, layoutParams.height);
        } else {
            childMeasureSpec = ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(0, 0), i2, layoutParams.height);
            childMeasureSpec2 = ViewGroup.getChildMeasureSpec(makeMeasureSpec, i, layoutParams.width);
        }
        view.measure(childMeasureSpec2, childMeasureSpec);
    }

    private void measureScrapChild(int i, int i2, int i3, int[] iArr) {
        View viewForPosition = this.mRecycler.getViewForPosition(i);
        if (viewForPosition != null) {
            LayoutParams layoutParams = (LayoutParams) viewForPosition.getLayoutParams();
            calculateItemDecorationsForChild(viewForPosition, sTempRect);
            int i4 = layoutParams.leftMargin;
            int i5 = layoutParams.rightMargin;
            int i6 = sTempRect.left;
            int i7 = sTempRect.right;
            int i8 = layoutParams.topMargin;
            int i9 = layoutParams.bottomMargin;
            int i10 = sTempRect.top;
            viewForPosition.measure(ViewGroup.getChildMeasureSpec(i2, getPaddingLeft() + getPaddingRight() + i4 + i5 + i6 + i7, layoutParams.width), ViewGroup.getChildMeasureSpec(i3, getPaddingTop() + getPaddingBottom() + i8 + i9 + i10 + sTempRect.bottom, layoutParams.height));
            iArr[0] = getDecoratedMeasuredWidthWithMargin(viewForPosition);
            iArr[1] = getDecoratedMeasuredHeightWithMargin(viewForPosition);
            this.mRecycler.recycleView(viewForPosition);
        }
    }

    private void offsetChildrenPrimary(int i) {
        int childCount = getChildCount();
        if (this.mOrientation == 1) {
            for (int i2 = 0; i2 < childCount; i2++) {
                getChildAt(i2).offsetTopAndBottom(i);
            }
            return;
        }
        for (int i3 = 0; i3 < childCount; i3++) {
            getChildAt(i3).offsetLeftAndRight(i);
        }
    }

    private void offsetChildrenSecondary(int i) {
        int childCount = getChildCount();
        if (this.mOrientation == 0) {
            for (int i2 = 0; i2 < childCount; i2++) {
                getChildAt(i2).offsetTopAndBottom(i);
            }
            return;
        }
        for (int i3 = 0; i3 < childCount; i3++) {
            getChildAt(i3).offsetLeftAndRight(i);
        }
    }

    private boolean prependOneColumnVisibleItems() {
        return this.mGrid.prependOneColumnVisibleItems();
    }

    private void prependVisibleItems() {
        this.mGrid.prependVisibleItems(this.mReverseFlowPrimary ? this.mSizePrimary + this.mExtraLayoutSpace : -this.mExtraLayoutSpace);
    }

    private void processPendingMovement(boolean z) {
        if (z ? hasCreatedLastItem() : hasCreatedFirstItem()) {
            return;
        }
        if (this.mPendingMoveSmoothScroller != null) {
            if (z) {
                this.mPendingMoveSmoothScroller.increasePendingMoves();
                return;
            } else {
                this.mPendingMoveSmoothScroller.decreasePendingMoves();
                return;
            }
        }
        this.mBaseGridView.stopScroll();
        PendingMoveSmoothScroller pendingMoveSmoothScroller = new PendingMoveSmoothScroller(this, z ? 1 : -1, this.mNumRows > 1);
        this.mFocusPositionOffset = 0;
        startSmoothScroll(pendingMoveSmoothScroller);
        if (pendingMoveSmoothScroller.isRunning()) {
            this.mPendingMoveSmoothScroller = pendingMoveSmoothScroller;
        }
    }

    private boolean processRowSizeSecondary(boolean z) {
        int i;
        int i2;
        if (this.mFixedRowSizeSecondary != 0 || this.mRowSizeSecondary == null) {
            return false;
        }
        CircularIntArray[] itemPositionsInRows = this.mGrid == null ? null : this.mGrid.getItemPositionsInRows();
        boolean z2 = false;
        int i3 = -1;
        int i4 = -1;
        int i5 = 0;
        while (i5 < this.mNumRows) {
            CircularIntArray circularIntArray = itemPositionsInRows == null ? null : itemPositionsInRows[i5];
            int size = circularIntArray == null ? 0 : circularIntArray.size();
            int i6 = -1;
            int i7 = 0;
            while (i7 < size) {
                int i8 = circularIntArray.get(i7);
                int i9 = circularIntArray.get(i7 + 1);
                int i10 = i6;
                while (true) {
                    i = i10;
                    if (i8 <= i9) {
                        View findViewByPosition = findViewByPosition(i8);
                        if (findViewByPosition == null) {
                            i2 = i;
                        } else {
                            if (z) {
                                measureChild(findViewByPosition);
                            }
                            int decoratedMeasuredHeightWithMargin = this.mOrientation == 0 ? getDecoratedMeasuredHeightWithMargin(findViewByPosition) : getDecoratedMeasuredWidthWithMargin(findViewByPosition);
                            i2 = i;
                            if (decoratedMeasuredHeightWithMargin > i) {
                                i2 = decoratedMeasuredHeightWithMargin;
                            }
                        }
                        i8++;
                        i10 = i2;
                    }
                }
                i7 += 2;
                i6 = i;
            }
            int itemCount = this.mState.getItemCount();
            int i11 = i6;
            int i12 = i4;
            int i13 = i3;
            if (!this.mBaseGridView.hasFixedSize()) {
                i11 = i6;
                i12 = i4;
                i13 = i3;
                if (z) {
                    i11 = i6;
                    i12 = i4;
                    i13 = i3;
                    if (i6 < 0) {
                        i11 = i6;
                        i12 = i4;
                        i13 = i3;
                        if (itemCount > 0) {
                            i12 = i4;
                            int i14 = i3;
                            if (i3 < 0) {
                                i12 = i4;
                                i14 = i3;
                                if (i4 < 0) {
                                    measureScrapChild(this.mFocusPosition == -1 ? 0 : this.mFocusPosition >= itemCount ? itemCount - 1 : this.mFocusPosition, View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0), this.mMeasuredDimension);
                                    i14 = this.mMeasuredDimension[0];
                                    i12 = this.mMeasuredDimension[1];
                                }
                            }
                            if (this.mOrientation == 0) {
                                i11 = i12;
                                i13 = i14;
                            } else {
                                i11 = i14;
                                i13 = i14;
                            }
                        }
                    }
                }
            }
            int i15 = i11;
            if (i11 < 0) {
                i15 = 0;
            }
            if (this.mRowSizeSecondary[i5] != i15) {
                this.mRowSizeSecondary[i5] = i15;
                z2 = true;
            }
            i5++;
            i4 = i12;
            i3 = i13;
        }
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int processSelectionMoves(boolean z, int i) {
        int i2;
        int i3;
        View view;
        int i4;
        if (this.mGrid == null) {
            return i;
        }
        int i5 = this.mFocusPosition;
        int rowIndex = i5 != -1 ? this.mGrid.getRowIndex(i5) : -1;
        View view2 = null;
        int childCount = getChildCount();
        int i6 = i;
        int i7 = 0;
        int i8 = rowIndex;
        while (i7 < childCount && i6 != 0) {
            int i9 = i6 > 0 ? i7 : (childCount - 1) - i7;
            View childAt = getChildAt(i9);
            if (canScrollTo(childAt)) {
                int positionByIndex = getPositionByIndex(i9);
                int rowIndex2 = this.mGrid.getRowIndex(positionByIndex);
                if (i8 == -1) {
                    i2 = positionByIndex;
                    view = childAt;
                    i3 = rowIndex2;
                    i4 = i6;
                } else {
                    i2 = i5;
                    i3 = i8;
                    view = view2;
                    i4 = i6;
                    if (rowIndex2 == i8) {
                        if (i6 <= 0 || positionByIndex <= i5) {
                            i2 = i5;
                            i3 = i8;
                            view = view2;
                            i4 = i6;
                            if (i6 < 0) {
                                i2 = i5;
                                i3 = i8;
                                view = view2;
                                i4 = i6;
                                if (positionByIndex >= i5) {
                                }
                            }
                        }
                        i2 = positionByIndex;
                        view = childAt;
                        if (i6 > 0) {
                            i4 = i6 - 1;
                            i3 = i8;
                        } else {
                            i4 = i6 + 1;
                            i3 = i8;
                        }
                    }
                }
            } else {
                i4 = i6;
                view = view2;
                i3 = i8;
                i2 = i5;
            }
            i7++;
            i5 = i2;
            i8 = i3;
            view2 = view;
            i6 = i4;
        }
        if (view2 != null) {
            if (z) {
                if (hasFocus()) {
                    this.mInSelection = true;
                    view2.requestFocus();
                    this.mInSelection = false;
                }
                this.mFocusPosition = i5;
                this.mSubFocusPosition = 0;
            } else {
                scrollToView(view2, true);
            }
        }
        return i6;
    }

    private void removeInvisibleViewsAtEnd() {
        if (this.mPruneChild) {
            this.mGrid.removeInvisibleItemsAtEnd(this.mFocusPosition, this.mReverseFlowPrimary ? -this.mExtraLayoutSpace : this.mSizePrimary + this.mExtraLayoutSpace);
        }
    }

    private void removeInvisibleViewsAtFront() {
        if (this.mPruneChild) {
            this.mGrid.removeInvisibleItemsAtFront(this.mFocusPosition, this.mReverseFlowPrimary ? this.mSizePrimary + this.mExtraLayoutSpace : -this.mExtraLayoutSpace);
        }
    }

    private void saveContext(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mRecycler != null || this.mState != null) {
            Log.e("GridLayoutManager", "Recycler information was not released, bug!");
        }
        this.mRecycler = recycler;
        this.mState = state;
    }

    private int scrollDirectionPrimary(int i) {
        int i2;
        boolean z = false;
        if (i > 0) {
            i2 = i;
            if (!this.mWindowAlignment.mainAxis().isMaxUnknown()) {
                int maxScroll = this.mWindowAlignment.mainAxis().getMaxScroll();
                i2 = i;
                if (this.mScrollOffsetPrimary + i > maxScroll) {
                    i2 = maxScroll - this.mScrollOffsetPrimary;
                }
            }
        } else {
            i2 = i;
            if (i < 0) {
                i2 = i;
                if (!this.mWindowAlignment.mainAxis().isMinUnknown()) {
                    int minScroll = this.mWindowAlignment.mainAxis().getMinScroll();
                    i2 = i;
                    if (this.mScrollOffsetPrimary + i < minScroll) {
                        i2 = minScroll - this.mScrollOffsetPrimary;
                    }
                }
            }
        }
        if (i2 == 0) {
            return 0;
        }
        offsetChildrenPrimary(-i2);
        this.mScrollOffsetPrimary += i2;
        if (this.mInLayout) {
            return i2;
        }
        int childCount = getChildCount();
        if (!this.mReverseFlowPrimary ? i2 >= 0 : i2 <= 0) {
            prependVisibleItems();
        } else {
            appendVisibleItems();
        }
        boolean z2 = getChildCount() > childCount;
        int childCount2 = getChildCount();
        if (!this.mReverseFlowPrimary ? i2 >= 0 : i2 <= 0) {
            removeInvisibleViewsAtEnd();
        } else {
            removeInvisibleViewsAtFront();
        }
        if (getChildCount() < childCount2) {
            z = true;
        }
        if (z2 | z) {
            updateRowSecondarySizeRefresh();
        }
        this.mBaseGridView.invalidate();
        return i2;
    }

    private int scrollDirectionSecondary(int i) {
        if (i == 0) {
            return 0;
        }
        offsetChildrenSecondary(-i);
        this.mScrollOffsetSecondary += i;
        this.mBaseGridView.invalidate();
        return i;
    }

    private void scrollGrid(int i, int i2, boolean z) {
        int i3;
        if (this.mInLayout) {
            scrollDirectionPrimary(i);
            scrollDirectionSecondary(i2);
            return;
        }
        if (this.mOrientation == 0) {
            i3 = i;
        } else {
            i3 = i2;
            i2 = i;
        }
        if (z) {
            this.mBaseGridView.smoothScrollBy(i3, i2);
        } else {
            this.mBaseGridView.scrollBy(i3, i2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scrollToSelection(int i, int i2, boolean z, int i3) {
        this.mPrimaryScrollExtra = i3;
        View findViewByPosition = findViewByPosition(i);
        if (findViewByPosition != null) {
            this.mInSelection = true;
            scrollToView(findViewByPosition, z);
            this.mInSelection = false;
            return;
        }
        this.mFocusPosition = i;
        this.mSubFocusPosition = i2;
        this.mFocusPositionOffset = Integer.MIN_VALUE;
        if (this.mLayoutEnabled) {
            if (!z) {
                this.mForceFullLayout = true;
                requestLayout();
            } else if (hasDoneFirstLayout()) {
                startPositionSmoothScroller(i);
            } else {
                Log.w(getTag(), "setSelectionSmooth should not be called before first layout pass");
            }
        }
    }

    private void scrollToView(View view, View view2, boolean z) {
        int positionByView = getPositionByView(view);
        int subPositionByView = getSubPositionByView(view, view2);
        if (positionByView != this.mFocusPosition || subPositionByView != this.mSubFocusPosition) {
            this.mFocusPosition = positionByView;
            this.mSubFocusPosition = subPositionByView;
            this.mFocusPositionOffset = 0;
            if (!this.mInLayout) {
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
        if ((this.mScrollEnabled || !z) && getScrollPosition(view, view2, sTwoInts)) {
            scrollGrid(sTwoInts[0], sTwoInts[1], z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scrollToView(View view, boolean z) {
        View view2 = null;
        if (view != null) {
            view2 = view.findFocus();
        }
        scrollToView(view, view2, z);
    }

    private void updateChildAlignments(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams.getItemAlignmentFacet() == null) {
            layoutParams.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(view));
            layoutParams.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(view));
            return;
        }
        layoutParams.calculateItemAlignments(this.mOrientation, view);
        if (this.mOrientation == 0) {
            layoutParams.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(view));
        } else {
            layoutParams.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(view));
        }
    }

    private void updateRowSecondarySizeRefresh() {
        this.mRowSecondarySizeRefresh = processRowSizeSecondary(false);
        if (this.mRowSecondarySizeRefresh) {
            forceRequestLayout();
        }
    }

    private void updateScrollController() {
        int paddingTop;
        int paddingLeft;
        if (this.mOrientation == 0) {
            paddingTop = getPaddingLeft() - this.mWindowAlignment.horizontal.getPaddingLow();
            paddingLeft = getPaddingTop() - this.mWindowAlignment.vertical.getPaddingLow();
        } else {
            paddingTop = getPaddingTop() - this.mWindowAlignment.vertical.getPaddingLow();
            paddingLeft = getPaddingLeft() - this.mWindowAlignment.horizontal.getPaddingLow();
        }
        this.mScrollOffsetPrimary -= paddingTop;
        this.mScrollOffsetSecondary -= paddingLeft;
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScrollMax() {
        int lastVisibleIndex = !this.mReverseFlowPrimary ? this.mGrid.getLastVisibleIndex() : this.mGrid.getFirstVisibleIndex();
        int itemCount = !this.mReverseFlowPrimary ? this.mState.getItemCount() - 1 : 0;
        if (lastVisibleIndex < 0) {
            return;
        }
        boolean z = lastVisibleIndex == itemCount;
        boolean isMaxUnknown = this.mWindowAlignment.mainAxis().isMaxUnknown();
        if (z || !isMaxUnknown) {
            int findRowMax = this.mGrid.findRowMax(true, sTwoInts) + this.mScrollOffsetPrimary;
            int i = sTwoInts[0];
            int i2 = sTwoInts[1];
            int maxEdge = this.mWindowAlignment.mainAxis().getMaxEdge();
            this.mWindowAlignment.mainAxis().setMaxEdge(findRowMax);
            int primarySystemScrollPositionOfChildMax = getPrimarySystemScrollPositionOfChildMax(findViewByPosition(i2));
            this.mWindowAlignment.mainAxis().setMaxEdge(maxEdge);
            if (!z) {
                this.mWindowAlignment.mainAxis().invalidateScrollMax();
                return;
            }
            this.mWindowAlignment.mainAxis().setMaxEdge(findRowMax);
            this.mWindowAlignment.mainAxis().setMaxScroll(primarySystemScrollPositionOfChildMax);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScrollMin() {
        int firstVisibleIndex = !this.mReverseFlowPrimary ? this.mGrid.getFirstVisibleIndex() : this.mGrid.getLastVisibleIndex();
        int itemCount = !this.mReverseFlowPrimary ? 0 : this.mState.getItemCount() - 1;
        if (firstVisibleIndex < 0) {
            return;
        }
        boolean z = firstVisibleIndex == itemCount;
        boolean isMinUnknown = this.mWindowAlignment.mainAxis().isMinUnknown();
        if (z || !isMinUnknown) {
            int findRowMin = this.mGrid.findRowMin(false, sTwoInts) + this.mScrollOffsetPrimary;
            int i = sTwoInts[0];
            int i2 = sTwoInts[1];
            int minEdge = this.mWindowAlignment.mainAxis().getMinEdge();
            this.mWindowAlignment.mainAxis().setMinEdge(findRowMin);
            int primarySystemScrollPosition = getPrimarySystemScrollPosition(findViewByPosition(i2));
            this.mWindowAlignment.mainAxis().setMinEdge(minEdge);
            if (!z) {
                this.mWindowAlignment.mainAxis().invalidateScrollMin();
                return;
            }
            this.mWindowAlignment.mainAxis().setMinEdge(findRowMin);
            this.mWindowAlignment.mainAxis().setMinScroll(primarySystemScrollPosition);
        }
    }

    private void updateScrollSecondAxis() {
        this.mWindowAlignment.secondAxis().setMinEdge(0);
        this.mWindowAlignment.secondAxis().setMaxEdge(getSizeSecondary());
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean canScrollHorizontally() {
        boolean z = true;
        if (this.mOrientation != 0) {
            z = this.mNumRows > 1;
        }
        return z;
    }

    boolean canScrollTo(View view) {
        boolean z = false;
        if (view.getVisibility() == 0) {
            z = hasFocus() ? view.hasFocusable() : true;
        }
        return z;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean canScrollVertically() {
        boolean z = true;
        if (this.mOrientation != 1) {
            z = this.mNumRows > 1;
        }
        return z;
    }

    void fireOnChildViewHolderSelected(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int i, int i2) {
        if (this.mChildViewHolderSelectedListeners == null) {
            return;
        }
        for (int size = this.mChildViewHolderSelectedListeners.size() - 1; size >= 0; size--) {
            this.mChildViewHolderSelectedListeners.get(size).onChildViewHolderSelected(recyclerView, viewHolder, i, i2);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attributeSet) {
        return new LayoutParams(context, attributeSet);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams ? new LayoutParams((LayoutParams) layoutParams) : layoutParams instanceof RecyclerView.LayoutParams ? new LayoutParams((RecyclerView.LayoutParams) layoutParams) : layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getChildDrawingOrder(RecyclerView recyclerView, int i, int i2) {
        int indexOfChild;
        View findViewByPosition = findViewByPosition(this.mFocusPosition);
        if (findViewByPosition != null && i2 >= (indexOfChild = recyclerView.indexOfChild(findViewByPosition))) {
            return i2 < i - 1 ? ((indexOfChild + i) - 1) - i2 : indexOfChild;
        }
        return i2;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        return (this.mOrientation != 1 || this.mGrid == null) ? super.getColumnCountForAccessibility(recycler, state) : this.mGrid.getNumRows();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedBottom(View view) {
        return super.getDecoratedBottom(view) - ((LayoutParams) view.getLayoutParams()).mBottomInset;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void getDecoratedBoundsWithMargins(View view, Rect rect) {
        super.getDecoratedBoundsWithMargins(view, rect);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        rect.left += layoutParams.mLeftInset;
        rect.top += layoutParams.mTopInset;
        rect.right -= layoutParams.mRightInset;
        rect.bottom -= layoutParams.mBottomInset;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedLeft(View view) {
        return ((LayoutParams) view.getLayoutParams()).mLeftInset + super.getDecoratedLeft(view);
    }

    int getDecoratedMeasuredHeightWithMargin(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + layoutParams.topMargin + layoutParams.bottomMargin;
    }

    int getDecoratedMeasuredWidthWithMargin(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + layoutParams.leftMargin + layoutParams.rightMargin;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedRight(View view) {
        return super.getDecoratedRight(view) - ((LayoutParams) view.getLayoutParams()).mRightInset;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getDecoratedTop(View view) {
        return ((LayoutParams) view.getLayoutParams()).mTopInset + super.getDecoratedTop(view);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int getOpticalLeft(View view) {
        return ((LayoutParams) view.getLayoutParams()).getOpticalLeft(view);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int getOpticalRight(View view) {
        return ((LayoutParams) view.getLayoutParams()).getOpticalRight(view);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        return (this.mOrientation != 0 || this.mGrid == null) ? super.getRowCountForAccessibility(recycler, state) : this.mGrid.getNumRows();
    }

    public int getSelection() {
        return this.mFocusPosition;
    }

    protected View getViewForPosition(int i) {
        return this.mRecycler.getViewForPosition(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean gridOnRequestFocusInDescendants(RecyclerView recyclerView, int i, Rect rect) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return gridOnRequestFocusInDescendantsUnaligned(recyclerView, i, rect);
            default:
                return gridOnRequestFocusInDescendantsAligned(recyclerView, i, rect);
        }
    }

    protected boolean hasDoneFirstLayout() {
        return this.mGrid != null;
    }

    boolean hasOnChildViewHolderSelectedListener() {
        boolean z = false;
        if (this.mChildViewHolderSelectedListeners != null) {
            z = false;
            if (this.mChildViewHolderSelectedListeners.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onAdapterChanged(RecyclerView.Adapter adapter, RecyclerView.Adapter adapter2) {
        if (adapter != null) {
            discardLayoutInfo();
            this.mFocusPosition = -1;
            this.mFocusPositionOffset = 0;
            this.mChildrenStates.clear();
        }
        if (adapter2 instanceof FacetProviderAdapter) {
            this.mFacetProviderAdapter = (FacetProviderAdapter) adapter2;
        } else {
            this.mFacetProviderAdapter = null;
        }
        super.onAdapterChanged(adapter, adapter2);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> arrayList, int i, int i2) {
        if (this.mFocusSearchDisabled) {
            return true;
        }
        if (!recyclerView.hasFocus()) {
            int size = arrayList.size();
            if (this.mFocusScrollStrategy != 0) {
                int paddingLow = this.mWindowAlignment.mainAxis().getPaddingLow();
                int clientSize = this.mWindowAlignment.mainAxis().getClientSize();
                int childCount = getChildCount();
                for (int i3 = 0; i3 < childCount; i3++) {
                    View childAt = getChildAt(i3);
                    if (childAt.getVisibility() == 0 && getViewMin(childAt) >= paddingLow && getViewMax(childAt) <= clientSize + paddingLow) {
                        childAt.addFocusables(arrayList, i, i2);
                    }
                }
                if (arrayList.size() == size) {
                    int childCount2 = getChildCount();
                    for (int i4 = 0; i4 < childCount2; i4++) {
                        View childAt2 = getChildAt(i4);
                        if (childAt2.getVisibility() == 0) {
                            childAt2.addFocusables(arrayList, i, i2);
                        }
                    }
                }
            } else {
                View findViewByPosition = findViewByPosition(this.mFocusPosition);
                if (findViewByPosition != null) {
                    findViewByPosition.addFocusables(arrayList, i, i2);
                }
            }
            if (arrayList.size() == size && recyclerView.isFocusable()) {
                arrayList.add(recyclerView);
                return true;
            }
            return true;
        } else if (this.mPendingMoveSmoothScroller != null) {
            return true;
        } else {
            int movement = getMovement(i);
            int findImmediateChildIndex = findImmediateChildIndex(recyclerView.findFocus());
            int positionByIndex = getPositionByIndex(findImmediateChildIndex);
            if (positionByIndex != -1) {
                findViewByPosition(positionByIndex).addFocusables(arrayList, i, i2);
            }
            if (this.mGrid == null || getChildCount() == 0) {
                return true;
            }
            if ((movement == 3 || movement == 2) && this.mGrid.getNumRows() <= 1) {
                return true;
            }
            int i5 = (this.mGrid == null || positionByIndex == -1) ? -1 : this.mGrid.getLocation(positionByIndex).row;
            int size2 = arrayList.size();
            int i6 = (movement == 1 || movement == 3) ? 1 : -1;
            int childCount3 = i6 > 0 ? getChildCount() - 1 : 0;
            int childCount4 = findImmediateChildIndex == -1 ? i6 > 0 ? 0 : getChildCount() - 1 : findImmediateChildIndex + i6;
            while (true) {
                int i7 = childCount4;
                if (i6 > 0) {
                    if (i7 > childCount3) {
                        return true;
                    }
                } else if (i7 < childCount3) {
                    return true;
                }
                View childAt3 = getChildAt(i7);
                if (childAt3.getVisibility() == 0 && childAt3.hasFocusable()) {
                    if (positionByIndex == -1) {
                        childAt3.addFocusables(arrayList, i, i2);
                        if (arrayList.size() > size2) {
                            return true;
                        }
                    } else {
                        int positionByIndex2 = getPositionByIndex(i7);
                        Grid.Location location = this.mGrid.getLocation(positionByIndex2);
                        if (location == null) {
                            continue;
                        } else if (movement == 1) {
                            if (location.row == i5 && positionByIndex2 > positionByIndex) {
                                childAt3.addFocusables(arrayList, i, i2);
                                if (arrayList.size() > size2) {
                                    return true;
                                }
                            }
                        } else if (movement == 0) {
                            if (location.row == i5 && positionByIndex2 < positionByIndex) {
                                childAt3.addFocusables(arrayList, i, i2);
                                if (arrayList.size() > size2) {
                                    return true;
                                }
                            }
                        } else if (movement == 3) {
                            if (location.row == i5) {
                                continue;
                            } else if (location.row < i5) {
                                return true;
                            } else {
                                childAt3.addFocusables(arrayList, i, i2);
                            }
                        } else if (movement == 2 && location.row != i5) {
                            if (location.row > i5) {
                                return true;
                            }
                            childAt3.addFocusables(arrayList, i, i2);
                        }
                    }
                }
                childCount4 = i7 + i6;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onChildRecycled(RecyclerView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        if (adapterPosition != -1) {
            this.mChildrenStates.saveOffscreenView(viewHolder.itemView, adapterPosition);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onFocusChanged(boolean z, int i, Rect rect) {
        if (!z) {
            return;
        }
        int i2 = this.mFocusPosition;
        while (true) {
            View findViewByPosition = findViewByPosition(i2);
            if (findViewByPosition == null) {
                return;
            }
            if (findViewByPosition.getVisibility() == 0 && findViewByPosition.hasFocusable()) {
                findViewByPosition.requestFocus();
                return;
            }
            i2++;
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfo(RecyclerView.Recycler recycler, RecyclerView.State state, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        saveContext(recycler, state);
        if (this.mScrollEnabled && !hasCreatedFirstItem()) {
            accessibilityNodeInfoCompat.addAction(8192);
            accessibilityNodeInfoCompat.setScrollable(true);
        }
        if (this.mScrollEnabled && !hasCreatedLastItem()) {
            accessibilityNodeInfoCompat.addAction(4096);
            accessibilityNodeInfoCompat.setScrollable(true);
        }
        accessibilityNodeInfoCompat.setCollectionInfo(AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state)));
        leaveContext();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (this.mGrid == null || !(layoutParams instanceof LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(recycler, state, view, accessibilityNodeInfoCompat);
            return;
        }
        int viewLayoutPosition = ((LayoutParams) layoutParams).getViewLayoutPosition();
        int rowIndex = this.mGrid.getRowIndex(viewLayoutPosition);
        int numRows = viewLayoutPosition / this.mGrid.getNumRows();
        if (this.mOrientation == 0) {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(rowIndex, 1, numRows, 1, false, false));
        } else {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(numRows, 1, rowIndex, 1, false, false));
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:75:0x0139, code lost:
        if (r5.mFocusOutSideEnd == false) goto L62;
     */
    /* JADX WARN: Code restructure failed: missing block: B:82:0x0156, code lost:
        if (r5.mFocusOutSideStart == false) goto L69;
     */
    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public View onInterceptFocusSearch(View view, int i) {
        View view2;
        if (this.mFocusSearchDisabled) {
            return view;
        }
        FocusFinder focusFinder = FocusFinder.getInstance();
        View view3 = null;
        if (i == 2 || i == 1) {
            if (canScrollVertically()) {
                view3 = focusFinder.findNextFocus(this.mBaseGridView, view, i == 2 ? 130 : 33);
            }
            if (canScrollHorizontally()) {
                view3 = focusFinder.findNextFocus(this.mBaseGridView, view, (i == 2) ^ (getLayoutDirection() == 1) ? 66 : 17);
            }
        } else {
            view3 = focusFinder.findNextFocus(this.mBaseGridView, view, i);
        }
        if (view3 != null) {
            return view3;
        }
        int movement = getMovement(i);
        boolean z = this.mBaseGridView.getScrollState() != 0;
        if (movement == 1) {
            if (z || !this.mFocusOutEnd) {
                view3 = view;
            }
            view2 = view3;
            if (this.mScrollEnabled) {
                if (hasCreatedLastItem()) {
                    view2 = view3;
                } else {
                    processPendingMovement(true);
                    view2 = view;
                }
            }
        } else if (movement == 0) {
            if (z || !this.mFocusOutFront) {
                view3 = view;
            }
            view2 = view3;
            if (this.mScrollEnabled) {
                view2 = view3;
                if (!hasCreatedFirstItem()) {
                    processPendingMovement(false);
                    view2 = view;
                }
            }
        } else if (movement == 3) {
            if (!z) {
                view2 = view3;
            }
            view2 = view;
        } else {
            view2 = view3;
            if (movement == 2) {
                if (!z) {
                    view2 = view3;
                }
                view2 = view;
            }
        }
        if (view2 != null) {
            return view2;
        }
        View focusSearch = this.mBaseGridView.getParent().focusSearch(view, i);
        if (focusSearch != null) {
            return focusSearch;
        }
        if (view == null) {
            view = this.mBaseGridView;
        }
        return view;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsAdded(RecyclerView recyclerView, int i, int i2) {
        if (this.mFocusPosition != -1 && this.mGrid != null && this.mGrid.getFirstVisibleIndex() >= 0 && this.mFocusPositionOffset != Integer.MIN_VALUE && i <= this.mFocusPosition + this.mFocusPositionOffset) {
            this.mFocusPositionOffset += i2;
        }
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsChanged(RecyclerView recyclerView) {
        this.mFocusPositionOffset = 0;
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsMoved(RecyclerView recyclerView, int i, int i2, int i3) {
        if (this.mFocusPosition != -1 && this.mFocusPositionOffset != Integer.MIN_VALUE) {
            int i4 = this.mFocusPosition + this.mFocusPositionOffset;
            if (i <= i4 && i4 < i + i3) {
                this.mFocusPositionOffset += i2 - i;
            } else if (i < i4 && i2 > i4 - i3) {
                this.mFocusPositionOffset -= i3;
            } else if (i > i4 && i2 < i4) {
                this.mFocusPositionOffset += i3;
            }
        }
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsRemoved(RecyclerView recyclerView, int i, int i2) {
        int i3;
        if (this.mFocusPosition != -1 && this.mGrid != null && this.mGrid.getFirstVisibleIndex() >= 0 && this.mFocusPositionOffset != Integer.MIN_VALUE && i <= (i3 = this.mFocusPosition + this.mFocusPositionOffset)) {
            if (i + i2 > i3) {
                this.mFocusPositionOffset = Integer.MIN_VALUE;
            } else {
                this.mFocusPositionOffset -= i2;
            }
        }
        this.mChildrenStates.clear();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsUpdated(RecyclerView recyclerView, int i, int i2) {
        for (int i3 = i; i3 < i + i2; i3++) {
            this.mChildrenStates.remove(i3);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        View findViewByPosition;
        if (this.mNumRows != 0 && state.getItemCount() >= 0) {
            if (!this.mLayoutEnabled) {
                discardLayoutInfo();
                removeAndRecycleAllViews(recycler);
                return;
            }
            this.mInLayout = true;
            if (state.didStructureChange()) {
                this.mBaseGridView.stopScroll();
            }
            boolean z = !isSmoothScrolling() ? this.mFocusScrollStrategy == 0 : false;
            if (this.mFocusPosition != -1 && this.mFocusPositionOffset != Integer.MIN_VALUE) {
                this.mFocusPosition += this.mFocusPositionOffset;
                this.mSubFocusPosition = 0;
            }
            this.mFocusPositionOffset = 0;
            saveContext(recycler, state);
            View findViewByPosition2 = findViewByPosition(this.mFocusPosition);
            int i = this.mFocusPosition;
            int i2 = this.mSubFocusPosition;
            boolean hasFocus = this.mBaseGridView.hasFocus();
            int i3 = 0;
            int i4 = 0;
            if (this.mFocusPosition != -1) {
                i3 = 0;
                i4 = 0;
                if (z) {
                    i3 = 0;
                    i4 = 0;
                    if (this.mBaseGridView.getScrollState() != 0) {
                        i3 = 0;
                        i4 = 0;
                        if (findViewByPosition2 != null) {
                            i3 = 0;
                            i4 = 0;
                            if (getScrollPosition(findViewByPosition2, findViewByPosition2.findFocus(), sTwoInts)) {
                                i3 = sTwoInts[0];
                                i4 = sTwoInts[1];
                            }
                        }
                    }
                }
            }
            boolean layoutInit = layoutInit();
            this.mInFastRelayout = layoutInit;
            if (!layoutInit) {
                this.mInLayoutSearchFocus = hasFocus;
                if (this.mFocusPosition != -1) {
                    while (appendOneColumnVisibleItems() && findViewByPosition(this.mFocusPosition) == null) {
                    }
                }
                while (true) {
                    updateScrollMin();
                    updateScrollMax();
                    int firstVisibleIndex = this.mGrid.getFirstVisibleIndex();
                    int lastVisibleIndex = this.mGrid.getLastVisibleIndex();
                    View findViewByPosition3 = findViewByPosition(this.mFocusPosition);
                    scrollToView(findViewByPosition3, false);
                    if (findViewByPosition3 != null && hasFocus && !findViewByPosition3.hasFocus()) {
                        findViewByPosition3.requestFocus();
                    }
                    appendVisibleItems();
                    prependVisibleItems();
                    removeInvisibleViewsAtFront();
                    removeInvisibleViewsAtEnd();
                    if (this.mGrid.getFirstVisibleIndex() == firstVisibleIndex && this.mGrid.getLastVisibleIndex() == lastVisibleIndex) {
                        break;
                    }
                }
            } else {
                fastRelayout();
                if (this.mFocusPosition != -1 && (findViewByPosition = findViewByPosition(this.mFocusPosition)) != null) {
                    if (z) {
                        scrollToView(findViewByPosition, false);
                    }
                    if (hasFocus && !findViewByPosition.hasFocus()) {
                        findViewByPosition.requestFocus();
                    }
                }
            }
            if (z) {
                scrollDirectionPrimary(-i3);
                scrollDirectionSecondary(-i4);
            }
            appendVisibleItems();
            prependVisibleItems();
            removeInvisibleViewsAtFront();
            removeInvisibleViewsAtEnd();
            if (this.mRowSecondarySizeRefresh) {
                this.mRowSecondarySizeRefresh = false;
            } else {
                updateRowSecondarySizeRefresh();
            }
            if (this.mInFastRelayout && (this.mFocusPosition != i || this.mSubFocusPosition != i2 || findViewByPosition(this.mFocusPosition) != findViewByPosition2)) {
                dispatchChildSelected();
            } else if (!this.mInFastRelayout && this.mInLayoutSearchFocus) {
                dispatchChildSelected();
            }
            this.mInLayout = false;
            leaveContext();
            if (hasFocus || this.mInFastRelayout || !this.mBaseGridView.hasFocusable()) {
                return;
            }
            ViewCompat.postOnAnimation(this.mBaseGridView, this.mAskFocusRunnable);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int i, int i2) {
        int size;
        int mode;
        int paddingLeft;
        int i3;
        int i4;
        saveContext(recycler, state);
        if (this.mOrientation == 0) {
            int size2 = View.MeasureSpec.getSize(i);
            i3 = View.MeasureSpec.getSize(i2);
            mode = View.MeasureSpec.getMode(i2);
            paddingLeft = getPaddingTop() + getPaddingBottom();
            size = size2;
        } else {
            int size3 = View.MeasureSpec.getSize(i);
            size = View.MeasureSpec.getSize(i2);
            mode = View.MeasureSpec.getMode(i);
            paddingLeft = getPaddingLeft() + getPaddingRight();
            i3 = size3;
        }
        this.mMaxSizeSecondary = i3;
        if (this.mRowSizeSecondaryRequested == -2) {
            this.mNumRows = this.mNumRowsRequested == 0 ? 1 : this.mNumRowsRequested;
            this.mFixedRowSizeSecondary = 0;
            if (this.mRowSizeSecondary == null || this.mRowSizeSecondary.length != this.mNumRows) {
                this.mRowSizeSecondary = new int[this.mNumRows];
            }
            processRowSizeSecondary(true);
            switch (mode) {
                case Integer.MIN_VALUE:
                    i4 = Math.min(getSizeSecondary() + paddingLeft, this.mMaxSizeSecondary);
                    break;
                case 0:
                    i4 = getSizeSecondary() + paddingLeft;
                    break;
                case 1073741824:
                    i4 = this.mMaxSizeSecondary;
                    break;
                default:
                    throw new IllegalStateException("wrong spec");
            }
        } else {
            switch (mode) {
                case Integer.MIN_VALUE:
                case 1073741824:
                    if (this.mNumRowsRequested == 0 && this.mRowSizeSecondaryRequested == 0) {
                        this.mNumRows = 1;
                        this.mFixedRowSizeSecondary = i3 - paddingLeft;
                    } else if (this.mNumRowsRequested == 0) {
                        this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
                        this.mNumRows = (this.mMarginSecondary + i3) / (this.mRowSizeSecondaryRequested + this.mMarginSecondary);
                    } else if (this.mRowSizeSecondaryRequested == 0) {
                        this.mNumRows = this.mNumRowsRequested;
                        this.mFixedRowSizeSecondary = ((i3 - paddingLeft) - (this.mMarginSecondary * (this.mNumRows - 1))) / this.mNumRows;
                    } else {
                        this.mNumRows = this.mNumRowsRequested;
                        this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
                    }
                    int i5 = i3;
                    i4 = i5;
                    if (mode == Integer.MIN_VALUE) {
                        int i6 = (this.mFixedRowSizeSecondary * this.mNumRows) + (this.mMarginSecondary * (this.mNumRows - 1)) + paddingLeft;
                        i4 = i5;
                        if (i6 < i5) {
                            i4 = i6;
                            break;
                        }
                    }
                    break;
                case 0:
                    this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested == 0 ? i3 - paddingLeft : this.mRowSizeSecondaryRequested;
                    this.mNumRows = this.mNumRowsRequested == 0 ? 1 : this.mNumRowsRequested;
                    i4 = (this.mFixedRowSizeSecondary * this.mNumRows) + (this.mMarginSecondary * (this.mNumRows - 1)) + paddingLeft;
                    break;
                default:
                    throw new IllegalStateException("wrong spec");
            }
        }
        if (this.mOrientation == 0) {
            setMeasuredDimension(size, i4);
        } else {
            setMeasuredDimension(i4, size);
        }
        leaveContext();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean onRequestChildFocus(RecyclerView recyclerView, View view, View view2) {
        if (this.mFocusSearchDisabled || getPositionByView(view) == -1 || this.mInLayout || this.mInSelection || this.mInScroll) {
            return true;
        }
        scrollToView(view, view2, true);
        return true;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof SavedState) {
            SavedState savedState = (SavedState) parcelable;
            this.mFocusPosition = savedState.index;
            this.mFocusPositionOffset = 0;
            this.mChildrenStates.loadFromBundle(savedState.childStates);
            this.mForceFullLayout = true;
            requestLayout();
        }
    }

    public void onRtlPropertiesChanged(int i) {
        if (this.mOrientation == 0) {
            this.mReverseFlowPrimary = i == 1;
            this.mReverseFlowSecondary = false;
        } else {
            this.mReverseFlowSecondary = i == 1;
            this.mReverseFlowPrimary = false;
        }
        this.mWindowAlignment.horizontal.setReversedFlow(i == 1);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState();
        savedState.index = getSelection();
        Bundle saveAsBundle = this.mChildrenStates.saveAsBundle();
        int i = 0;
        int childCount = getChildCount();
        while (i < childCount) {
            View childAt = getChildAt(i);
            int positionByView = getPositionByView(childAt);
            Bundle bundle = saveAsBundle;
            if (positionByView != -1) {
                bundle = this.mChildrenStates.saveOnScreenView(saveAsBundle, childAt, positionByView);
            }
            i++;
            saveAsBundle = bundle;
        }
        savedState.childStates = saveAsBundle;
        return savedState;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean performAccessibilityAction(RecyclerView.Recycler recycler, RecyclerView.State state, int i, Bundle bundle) {
        saveContext(recycler, state);
        switch (i) {
            case 4096:
                processSelectionMoves(false, this.mState.getItemCount());
                break;
            case 8192:
                processSelectionMoves(false, -this.mState.getItemCount());
                break;
        }
        leaveContext();
        return true;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            removeAndRecycleViewAt(childCount, recycler);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean requestChildRectangleOnScreen(RecyclerView recyclerView, View view, Rect rect, boolean z) {
        return false;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollHorizontallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mLayoutEnabled && hasDoneFirstLayout()) {
            saveContext(recycler, state);
            this.mInScroll = true;
            int scrollDirectionPrimary = this.mOrientation == 0 ? scrollDirectionPrimary(i) : scrollDirectionSecondary(i);
            leaveContext();
            this.mInScroll = false;
            return scrollDirectionPrimary;
        }
        return 0;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void scrollToPosition(int i) {
        setSelection(i, 0, false, 0);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollVerticallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mLayoutEnabled && hasDoneFirstLayout()) {
            this.mInScroll = true;
            saveContext(recycler, state);
            int scrollDirectionPrimary = this.mOrientation == 1 ? scrollDirectionPrimary(i) : scrollDirectionSecondary(i);
            leaveContext();
            this.mInScroll = false;
            return scrollDirectionPrimary;
        }
        return 0;
    }

    public void setFocusOutAllowed(boolean z, boolean z2) {
        this.mFocusOutFront = z;
        this.mFocusOutEnd = z2;
    }

    public void setFocusOutSideAllowed(boolean z, boolean z2) {
        this.mFocusOutSideStart = z;
        this.mFocusOutSideEnd = z2;
    }

    public void setGravity(int i) {
        this.mGravity = i;
    }

    public void setHorizontalMargin(int i) {
        if (this.mOrientation == 0) {
            this.mHorizontalMargin = i;
            this.mMarginPrimary = i;
            return;
        }
        this.mHorizontalMargin = i;
        this.mMarginSecondary = i;
    }

    public void setNumRows(int i) {
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        this.mNumRowsRequested = i;
    }

    public void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener onChildViewHolderSelectedListener) {
        if (onChildViewHolderSelectedListener == null) {
            this.mChildViewHolderSelectedListeners = null;
            return;
        }
        if (this.mChildViewHolderSelectedListeners == null) {
            this.mChildViewHolderSelectedListeners = new ArrayList<>();
        } else {
            this.mChildViewHolderSelectedListeners.clear();
        }
        this.mChildViewHolderSelectedListeners.add(onChildViewHolderSelectedListener);
    }

    public void setOrientation(int i) {
        if (i == 0 || i == 1) {
            this.mOrientation = i;
            this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, this.mOrientation);
            this.mWindowAlignment.setOrientation(i);
            this.mItemAlignment.setOrientation(i);
            this.mForceFullLayout = true;
        }
    }

    public void setRowHeight(int i) {
        if (i < 0 && i != -2) {
            throw new IllegalArgumentException("Invalid row height: " + i);
        }
        this.mRowSizeSecondaryRequested = i;
    }

    public void setSelection(int i, int i2) {
        setSelection(i, 0, false, i2);
    }

    public void setSelection(int i, int i2, boolean z, int i3) {
        if ((this.mFocusPosition == i || i == -1) && i2 == this.mSubFocusPosition && i3 == this.mPrimaryScrollExtra) {
            return;
        }
        scrollToSelection(i, i2, z, i3);
    }

    public void setSelectionSmooth(int i) {
        setSelection(i, 0, true, 0);
    }

    public void setVerticalMargin(int i) {
        if (this.mOrientation == 0) {
            this.mVerticalMargin = i;
            this.mMarginSecondary = i;
            return;
        }
        this.mVerticalMargin = i;
        this.mMarginPrimary = i;
    }

    public void setWindowAlignment(int i) {
        this.mWindowAlignment.mainAxis().setWindowAlignment(i);
    }

    void startPositionSmoothScroller(int i) {
        GridLinearSmoothScroller gridLinearSmoothScroller = new GridLinearSmoothScroller(this, this) { // from class: android.support.v17.leanback.widget.GridLayoutManager.4
            final GridLayoutManager this$0;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(this);
                this.this$0 = this;
            }

            @Override // android.support.v7.widget.LinearSmoothScroller
            public PointF computeScrollVectorForPosition(int i2) {
                boolean z = true;
                if (getChildCount() == 0) {
                    return null;
                }
                int position = this.this$0.getPosition(this.this$0.getChildAt(0));
                if (this.this$0.mReverseFlowPrimary) {
                    if (i2 <= position) {
                        z = false;
                    }
                } else if (i2 >= position) {
                    z = false;
                }
                int i3 = z ? -1 : 1;
                return this.this$0.mOrientation == 0 ? new PointF(i3, 0.0f) : new PointF(0.0f, i3);
            }
        };
        gridLinearSmoothScroller.setTargetPosition(i);
        startSmoothScroll(gridLinearSmoothScroller);
    }
}
