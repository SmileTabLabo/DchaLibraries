package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.os.TraceCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.recyclerview.R$styleable;
import android.support.v7.widget.AdapterHelper;
import android.support.v7.widget.ChildHelper;
import android.support.v7.widget.ViewInfoStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.FocusFinder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import com.android.launcher3.compat.PackageInstallerCompat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: a.zip:android/support/v7/widget/RecyclerView.class */
public class RecyclerView extends ViewGroup implements ScrollingView, NestedScrollingChild {
    static final boolean ALLOW_SIZE_IN_UNSPECIFIED_SPEC;
    private static final boolean FORCE_INVALIDATE_DISPLAY_LIST;
    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE;
    private static final Interpolator sQuinticInterpolator;
    private RecyclerViewAccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityManager mAccessibilityManager;
    private OnItemTouchListener mActiveOnItemTouchListener;
    private Adapter mAdapter;
    AdapterHelper mAdapterHelper;
    private boolean mAdapterUpdateDuringMeasure;
    private EdgeEffectCompat mBottomGlow;
    private ChildDrawingOrderCallback mChildDrawingOrderCallback;
    ChildHelper mChildHelper;
    private boolean mClipToPadding;
    private boolean mDataSetHasChangedAfterLayout;
    private int mEatRequestLayout;
    private int mEatenAccessibilityChangeFlags;
    @VisibleForTesting
    boolean mFirstLayoutComplete;
    private boolean mHasFixedSize;
    private boolean mIgnoreMotionEventTillDown;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private boolean mIsAttached;
    ItemAnimator mItemAnimator;
    private ItemAnimator.ItemAnimatorListener mItemAnimatorListener;
    private Runnable mItemAnimatorRunner;
    private final ArrayList<ItemDecoration> mItemDecorations;
    boolean mItemsAddedOrRemoved;
    boolean mItemsChanged;
    private int mLastTouchX;
    private int mLastTouchY;
    @VisibleForTesting
    LayoutManager mLayout;
    private boolean mLayoutFrozen;
    private int mLayoutOrScrollCounter;
    private boolean mLayoutRequestEaten;
    private EdgeEffectCompat mLeftGlow;
    private final int mMaxFlingVelocity;
    private final int mMinFlingVelocity;
    private final int[] mMinMaxLayoutPositions;
    private final int[] mNestedOffsets;
    private final RecyclerViewDataObserver mObserver;
    private List<OnChildAttachStateChangeListener> mOnChildAttachStateListeners;
    private final ArrayList<OnItemTouchListener> mOnItemTouchListeners;
    private SavedState mPendingSavedState;
    private final boolean mPostUpdatesOnAnimation;
    private boolean mPostedAnimatorRunner;
    private boolean mPreserveFocusAfterLayout;
    final Recycler mRecycler;
    private RecyclerListener mRecyclerListener;
    private EdgeEffectCompat mRightGlow;
    private final int[] mScrollConsumed;
    private float mScrollFactor;
    private OnScrollListener mScrollListener;
    private List<OnScrollListener> mScrollListeners;
    private final int[] mScrollOffset;
    private int mScrollPointerId;
    private int mScrollState;
    private NestedScrollingChildHelper mScrollingChildHelper;
    final State mState;
    private final Rect mTempRect;
    private final Rect mTempRect2;
    private final RectF mTempRectF;
    private EdgeEffectCompat mTopGlow;
    private int mTouchSlop;
    private final Runnable mUpdateChildViewsRunnable;
    private VelocityTracker mVelocityTracker;
    private final ViewFlinger mViewFlinger;
    private final ViewInfoStore.ProcessCallback mViewInfoProcessCallback;
    final ViewInfoStore mViewInfoStore;
    private static final int[] NESTED_SCROLLING_ATTRS = {16843830};
    private static final int[] CLIP_TO_PADDING_ATTR = {16842987};

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$Adapter.class */
    public static abstract class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable mObservable = new AdapterDataObservable();
        private boolean mHasStableIds = false;

        public final void bindViewHolder(VH vh, int i) {
            vh.mPosition = i;
            if (hasStableIds()) {
                vh.mItemId = getItemId(i);
            }
            vh.setFlags(1, 519);
            TraceCompat.beginSection("RV OnBindView");
            onBindViewHolder(vh, i, vh.getUnmodifiedPayloads());
            vh.clearPayload();
            TraceCompat.endSection();
        }

        public final VH createViewHolder(ViewGroup viewGroup, int i) {
            TraceCompat.beginSection("RV CreateView");
            VH onCreateViewHolder = onCreateViewHolder(viewGroup, i);
            onCreateViewHolder.mItemViewType = i;
            TraceCompat.endSection();
            return onCreateViewHolder;
        }

        public abstract int getItemCount();

        public long getItemId(int i) {
            return -1L;
        }

        public int getItemViewType(int i) {
            return 0;
        }

        public final boolean hasStableIds() {
            return this.mHasStableIds;
        }

        public final void notifyDataSetChanged() {
            this.mObservable.notifyChanged();
        }

        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        }

        public abstract void onBindViewHolder(VH vh, int i);

        public void onBindViewHolder(VH vh, int i, List<Object> list) {
            onBindViewHolder(vh, i);
        }

        public abstract VH onCreateViewHolder(ViewGroup viewGroup, int i);

        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        }

        public boolean onFailedToRecycleView(VH vh) {
            return false;
        }

        public void onViewAttachedToWindow(VH vh) {
        }

        public void onViewDetachedFromWindow(VH vh) {
        }

        public void onViewRecycled(VH vh) {
        }

        public void registerAdapterDataObserver(AdapterDataObserver adapterDataObserver) {
            this.mObservable.registerObserver(adapterDataObserver);
        }

        public void unregisterAdapterDataObserver(AdapterDataObserver adapterDataObserver) {
            this.mObservable.unregisterObserver(adapterDataObserver);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$AdapterDataObservable.class */
    public static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        AdapterDataObservable() {
        }

        public void notifyChanged() {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onChanged();
            }
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$AdapterDataObserver.class */
    public static abstract class AdapterDataObserver {
        public void onChanged() {
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ChildDrawingOrderCallback.class */
    public interface ChildDrawingOrderCallback {
        int onGetChildDrawingOrder(int i, int i2);
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ItemAnimator.class */
    public static abstract class ItemAnimator {
        private ItemAnimatorListener mListener = null;
        private ArrayList<ItemAnimatorFinishedListener> mFinishedListeners = new ArrayList<>();
        private long mAddDuration = 120;
        private long mRemoveDuration = 120;
        private long mMoveDuration = 250;
        private long mChangeDuration = 250;

        @Retention(RetentionPolicy.SOURCE)
        /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ItemAnimator$AdapterChanges.class */
        public @interface AdapterChanges {
        }

        /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ItemAnimator$ItemAnimatorFinishedListener.class */
        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ItemAnimator$ItemAnimatorListener.class */
        public interface ItemAnimatorListener {
            void onAnimationFinished(ViewHolder viewHolder);
        }

        /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ItemAnimator$ItemHolderInfo.class */
        public static class ItemHolderInfo {
            public int bottom;
            public int left;
            public int right;
            public int top;

            public ItemHolderInfo setFrom(ViewHolder viewHolder) {
                return setFrom(viewHolder, 0);
            }

            public ItemHolderInfo setFrom(ViewHolder viewHolder, int i) {
                View view = viewHolder.itemView;
                this.left = view.getLeft();
                this.top = view.getTop();
                this.right = view.getRight();
                this.bottom = view.getBottom();
                return this;
            }
        }

        static int buildAdapterChangeFlagsForAnimations(ViewHolder viewHolder) {
            int i = viewHolder.mFlags & 14;
            if (viewHolder.isInvalid()) {
                return 4;
            }
            int i2 = i;
            if ((i & 4) == 0) {
                int oldPosition = viewHolder.getOldPosition();
                int adapterPosition = viewHolder.getAdapterPosition();
                i2 = i;
                if (oldPosition != -1) {
                    i2 = i;
                    if (adapterPosition != -1) {
                        i2 = i;
                        if (oldPosition != adapterPosition) {
                            i2 = i | 2048;
                        }
                    }
                }
            }
            return i2;
        }

        public abstract boolean animateAppearance(@NonNull ViewHolder viewHolder, @Nullable ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2);

        public abstract boolean animateChange(@NonNull ViewHolder viewHolder, @NonNull ViewHolder viewHolder2, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2);

        public abstract boolean animateDisappearance(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @Nullable ItemHolderInfo itemHolderInfo2);

        public abstract boolean animatePersistence(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo2);

        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder) {
            return true;
        }

        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder, @NonNull List<Object> list) {
            return canReuseUpdatedViewHolder(viewHolder);
        }

        public final void dispatchAnimationFinished(ViewHolder viewHolder) {
            onAnimationFinished(viewHolder);
            if (this.mListener != null) {
                this.mListener.onAnimationFinished(viewHolder);
            }
        }

        public final void dispatchAnimationsFinished() {
            int size = this.mFinishedListeners.size();
            for (int i = 0; i < size; i++) {
                this.mFinishedListeners.get(i).onAnimationsFinished();
            }
            this.mFinishedListeners.clear();
        }

        public abstract void endAnimation(ViewHolder viewHolder);

        public abstract void endAnimations();

        public long getAddDuration() {
            return this.mAddDuration;
        }

        public long getChangeDuration() {
            return this.mChangeDuration;
        }

        public long getMoveDuration() {
            return this.mMoveDuration;
        }

        public long getRemoveDuration() {
            return this.mRemoveDuration;
        }

        public abstract boolean isRunning();

        public ItemHolderInfo obtainHolderInfo() {
            return new ItemHolderInfo();
        }

        public void onAnimationFinished(ViewHolder viewHolder) {
        }

        @NonNull
        public ItemHolderInfo recordPostLayoutInformation(@NonNull State state, @NonNull ViewHolder viewHolder) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        @NonNull
        public ItemHolderInfo recordPreLayoutInformation(@NonNull State state, @NonNull ViewHolder viewHolder, int i, @NonNull List<Object> list) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        public abstract void runPendingAnimations();

        void setListener(ItemAnimatorListener itemAnimatorListener) {
            this.mListener = itemAnimatorListener;
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ItemAnimatorRestoreListener.class */
    private class ItemAnimatorRestoreListener implements ItemAnimator.ItemAnimatorListener {
        final RecyclerView this$0;

        private ItemAnimatorRestoreListener(RecyclerView recyclerView) {
            this.this$0 = recyclerView;
        }

        /* synthetic */ ItemAnimatorRestoreListener(RecyclerView recyclerView, ItemAnimatorRestoreListener itemAnimatorRestoreListener) {
            this(recyclerView);
        }

        @Override // android.support.v7.widget.RecyclerView.ItemAnimator.ItemAnimatorListener
        public void onAnimationFinished(ViewHolder viewHolder) {
            viewHolder.setIsRecyclable(true);
            if (viewHolder.mShadowedHolder != null && viewHolder.mShadowingHolder == null) {
                viewHolder.mShadowedHolder = null;
            }
            viewHolder.mShadowingHolder = null;
            if (viewHolder.shouldBeKeptAsChild() || this.this$0.removeAnimatingView(viewHolder.itemView) || !viewHolder.isTmpDetached()) {
                return;
            }
            this.this$0.removeDetachedView(viewHolder.itemView, false);
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ItemDecoration.class */
    public static abstract class ItemDecoration {
        @Deprecated
        public void getItemOffsets(Rect rect, int i, RecyclerView recyclerView) {
            rect.set(0, 0, 0, 0);
        }

        public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, State state) {
            getItemOffsets(rect, ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition(), recyclerView);
        }

        @Deprecated
        public void onDraw(Canvas canvas, RecyclerView recyclerView) {
        }

        public void onDraw(Canvas canvas, RecyclerView recyclerView, State state) {
            onDraw(canvas, recyclerView);
        }

        @Deprecated
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView) {
        }

        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, State state) {
            onDrawOver(canvas, recyclerView);
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$LayoutManager.class */
    public static abstract class LayoutManager {
        ChildHelper mChildHelper;
        private int mHeight;
        private int mHeightMode;
        RecyclerView mRecyclerView;
        @Nullable
        SmoothScroller mSmoothScroller;
        private int mWidth;
        private int mWidthMode;
        private boolean mRequestedSimpleAnimations = false;
        boolean mIsAttachedToWindow = false;
        private boolean mAutoMeasure = false;
        private boolean mMeasurementCacheEnabled = true;

        private void addViewInt(View view, int i, boolean z) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (z || childViewHolderInt.isRemoved()) {
                this.mRecyclerView.mViewInfoStore.addToDisappearedInLayout(childViewHolderInt);
            } else {
                this.mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(childViewHolderInt);
            }
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (childViewHolderInt.wasReturnedFromScrap() || childViewHolderInt.isScrap()) {
                if (childViewHolderInt.isScrap()) {
                    childViewHolderInt.unScrap();
                } else {
                    childViewHolderInt.clearReturnedFromScrapFlag();
                }
                this.mChildHelper.attachViewToParent(view, i, view.getLayoutParams(), false);
            } else if (view.getParent() == this.mRecyclerView) {
                int indexOfChild = this.mChildHelper.indexOfChild(view);
                int i2 = i;
                if (i == -1) {
                    i2 = this.mChildHelper.getChildCount();
                }
                if (indexOfChild == -1) {
                    throw new IllegalStateException("Added View has RecyclerView as parent but view is not a real child. Unfiltered index:" + this.mRecyclerView.indexOfChild(view));
                }
                if (indexOfChild != i2) {
                    this.mRecyclerView.mLayout.moveView(indexOfChild, i2);
                }
            } else {
                this.mChildHelper.addView(view, i, false);
                layoutParams.mInsetsDirty = true;
                if (this.mSmoothScroller != null && this.mSmoothScroller.isRunning()) {
                    this.mSmoothScroller.onChildAttachedToWindow(view);
                }
            }
            if (layoutParams.mPendingInvalidate) {
                childViewHolderInt.itemView.invalidate();
                layoutParams.mPendingInvalidate = false;
            }
        }

        public static int chooseSize(int i, int i2, int i3) {
            int mode = View.MeasureSpec.getMode(i);
            int size = View.MeasureSpec.getSize(i);
            switch (mode) {
                case Integer.MIN_VALUE:
                    return Math.min(size, Math.max(i2, i3));
                case 1073741824:
                    return size;
                default:
                    return Math.max(i2, i3);
            }
        }

        private void detachViewInternal(int i, View view) {
            this.mChildHelper.detachViewFromParent(i);
        }

        public static int getChildMeasureSpec(int i, int i2, int i3, int i4, boolean z) {
            int max = Math.max(0, i - i3);
            int i5 = 0;
            int i6 = 0;
            if (z) {
                if (i4 >= 0) {
                    i5 = i4;
                    i6 = 1073741824;
                } else if (i4 == -1) {
                    switch (i2) {
                        case Integer.MIN_VALUE:
                        case 1073741824:
                            i5 = max;
                            i6 = i2;
                            break;
                        case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                            i5 = 0;
                            i6 = 0;
                            break;
                    }
                } else if (i4 == -2) {
                    i5 = 0;
                    i6 = 0;
                }
            } else if (i4 >= 0) {
                i5 = i4;
                i6 = 1073741824;
            } else if (i4 == -1) {
                i5 = max;
                i6 = i2;
            } else if (i4 == -2) {
                i5 = max;
                i6 = (i2 == Integer.MIN_VALUE || i2 == 1073741824) ? Integer.MIN_VALUE : 0;
            }
            return View.MeasureSpec.makeMeasureSpec(i5, i6);
        }

        private static boolean isMeasurementUpToDate(int i, int i2, int i3) {
            boolean z = true;
            int mode = View.MeasureSpec.getMode(i2);
            int size = View.MeasureSpec.getSize(i2);
            if (i3 <= 0 || i == i3) {
                switch (mode) {
                    case Integer.MIN_VALUE:
                        if (size < i) {
                            z = false;
                        }
                        return z;
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        return true;
                    case 1073741824:
                        return size == i;
                    default:
                        return false;
                }
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onSmoothScrollerStopped(SmoothScroller smoothScroller) {
            if (this.mSmoothScroller == smoothScroller) {
                this.mSmoothScroller = null;
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int i, View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.shouldIgnore()) {
                return;
            }
            if (childViewHolderInt.isInvalid() && !childViewHolderInt.isRemoved() && !this.mRecyclerView.mAdapter.hasStableIds()) {
                removeViewAt(i);
                recycler.recycleViewHolderInternal(childViewHolderInt);
                return;
            }
            detachViewAt(i);
            recycler.scrapView(view);
            this.mRecyclerView.mViewInfoStore.onViewDetached(childViewHolderInt);
        }

        public void addDisappearingView(View view) {
            addDisappearingView(view, -1);
        }

        public void addDisappearingView(View view, int i) {
            addViewInt(view, i, true);
        }

        public void addView(View view) {
            addView(view, -1);
        }

        public void addView(View view, int i) {
            addViewInt(view, i, false);
        }

        public void assertNotInLayoutOrScroll(String str) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.assertNotInLayoutOrScroll(str);
            }
        }

        public void attachView(View view, int i) {
            attachView(view, i, (LayoutParams) view.getLayoutParams());
        }

        public void attachView(View view, int i, LayoutParams layoutParams) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.isRemoved()) {
                this.mRecyclerView.mViewInfoStore.addToDisappearedInLayout(childViewHolderInt);
            } else {
                this.mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(childViewHolderInt);
            }
            this.mChildHelper.attachViewToParent(view, i, layoutParams, childViewHolderInt.isRemoved());
        }

        public void calculateItemDecorationsForChild(View view, Rect rect) {
            if (this.mRecyclerView == null) {
                rect.set(0, 0, 0, 0);
            } else {
                rect.set(this.mRecyclerView.getItemDecorInsetsForChild(view));
            }
        }

        public boolean canScrollHorizontally() {
            return false;
        }

        public boolean canScrollVertically() {
            return false;
        }

        public boolean checkLayoutParams(LayoutParams layoutParams) {
            return layoutParams != null;
        }

        public int computeHorizontalScrollExtent(State state) {
            return 0;
        }

        public int computeHorizontalScrollOffset(State state) {
            return 0;
        }

        public int computeHorizontalScrollRange(State state) {
            return 0;
        }

        public int computeVerticalScrollExtent(State state) {
            return 0;
        }

        public int computeVerticalScrollOffset(State state) {
            return 0;
        }

        public int computeVerticalScrollRange(State state) {
            return 0;
        }

        public void detachAndScrapAttachedViews(Recycler recycler) {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                scrapOrRecycleView(recycler, childCount, getChildAt(childCount));
            }
        }

        public void detachViewAt(int i) {
            detachViewInternal(i, getChildAt(i));
        }

        void dispatchAttachedToWindow(RecyclerView recyclerView) {
            this.mIsAttachedToWindow = true;
            onAttachedToWindow(recyclerView);
        }

        void dispatchDetachedFromWindow(RecyclerView recyclerView, Recycler recycler) {
            this.mIsAttachedToWindow = false;
            onDetachedFromWindow(recyclerView, recycler);
        }

        @Nullable
        public View findContainingItemView(View view) {
            View findContainingItemView;
            if (this.mRecyclerView == null || (findContainingItemView = this.mRecyclerView.findContainingItemView(view)) == null || this.mChildHelper.isHidden(findContainingItemView)) {
                return null;
            }
            return findContainingItemView;
        }

        public View findViewByPosition(int i) {
            int childCount = getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = getChildAt(i2);
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(childAt);
                if (childViewHolderInt != null && childViewHolderInt.getLayoutPosition() == i && !childViewHolderInt.shouldIgnore() && (this.mRecyclerView.mState.isPreLayout() || !childViewHolderInt.isRemoved())) {
                    return childAt;
                }
            }
            return null;
        }

        public abstract LayoutParams generateDefaultLayoutParams();

        public LayoutParams generateLayoutParams(Context context, AttributeSet attributeSet) {
            return new LayoutParams(context, attributeSet);
        }

        public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
            return layoutParams instanceof LayoutParams ? new LayoutParams((LayoutParams) layoutParams) : layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
        }

        public int getBaseline() {
            return -1;
        }

        public int getBottomDecorationHeight(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.bottom;
        }

        public View getChildAt(int i) {
            View view = null;
            if (this.mChildHelper != null) {
                view = this.mChildHelper.getChildAt(i);
            }
            return view;
        }

        public int getChildCount() {
            return this.mChildHelper != null ? this.mChildHelper.getChildCount() : 0;
        }

        public int getColumnCountForAccessibility(Recycler recycler, State state) {
            int i = 1;
            if (this.mRecyclerView == null || this.mRecyclerView.mAdapter == null) {
                return 1;
            }
            if (canScrollHorizontally()) {
                i = this.mRecyclerView.mAdapter.getItemCount();
            }
            return i;
        }

        public int getDecoratedBottom(View view) {
            return view.getBottom() + getBottomDecorationHeight(view);
        }

        public void getDecoratedBoundsWithMargins(View view, Rect rect) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Rect rect2 = layoutParams.mDecorInsets;
            rect.set((view.getLeft() - rect2.left) - layoutParams.leftMargin, (view.getTop() - rect2.top) - layoutParams.topMargin, view.getRight() + rect2.right + layoutParams.rightMargin, view.getBottom() + rect2.bottom + layoutParams.bottomMargin);
        }

        public int getDecoratedLeft(View view) {
            return view.getLeft() - getLeftDecorationWidth(view);
        }

        public int getDecoratedMeasuredHeight(View view) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            return view.getMeasuredHeight() + rect.top + rect.bottom;
        }

        public int getDecoratedMeasuredWidth(View view) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            return view.getMeasuredWidth() + rect.left + rect.right;
        }

        public int getDecoratedRight(View view) {
            return view.getRight() + getRightDecorationWidth(view);
        }

        public int getDecoratedTop(View view) {
            return view.getTop() - getTopDecorationHeight(view);
        }

        public View getFocusedChild() {
            View focusedChild;
            if (this.mRecyclerView == null || (focusedChild = this.mRecyclerView.getFocusedChild()) == null || this.mChildHelper.isHidden(focusedChild)) {
                return null;
            }
            return focusedChild;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public int getHeightMode() {
            return this.mHeightMode;
        }

        public int getItemViewType(View view) {
            return RecyclerView.getChildViewHolderInt(view).getItemViewType();
        }

        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(this.mRecyclerView);
        }

        public int getLeftDecorationWidth(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.left;
        }

        public int getMinimumHeight() {
            return ViewCompat.getMinimumHeight(this.mRecyclerView);
        }

        public int getMinimumWidth() {
            return ViewCompat.getMinimumWidth(this.mRecyclerView);
        }

        public int getPaddingBottom() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingBottom() : 0;
        }

        public int getPaddingLeft() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingLeft() : 0;
        }

        public int getPaddingRight() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingRight() : 0;
        }

        public int getPaddingTop() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingTop() : 0;
        }

        public int getPosition(View view) {
            return ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        }

        public int getRightDecorationWidth(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.right;
        }

        public int getRowCountForAccessibility(Recycler recycler, State state) {
            int i = 1;
            if (this.mRecyclerView == null || this.mRecyclerView.mAdapter == null) {
                return 1;
            }
            if (canScrollVertically()) {
                i = this.mRecyclerView.mAdapter.getItemCount();
            }
            return i;
        }

        public int getSelectionModeForAccessibility(Recycler recycler, State state) {
            return 0;
        }

        public int getTopDecorationHeight(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.top;
        }

        public void getTransformedBoundingBox(View view, boolean z, Rect rect) {
            Matrix matrix;
            if (z) {
                Rect rect2 = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
                rect.set(-rect2.left, -rect2.top, view.getWidth() + rect2.right, view.getHeight() + rect2.bottom);
            } else {
                rect.set(0, 0, view.getWidth(), view.getHeight());
            }
            if (this.mRecyclerView != null && (matrix = ViewCompat.getMatrix(view)) != null && !matrix.isIdentity()) {
                RectF rectF = this.mRecyclerView.mTempRectF;
                rectF.set(rect);
                matrix.mapRect(rectF);
                rect.set((int) Math.floor(rectF.left), (int) Math.floor(rectF.top), (int) Math.ceil(rectF.right), (int) Math.ceil(rectF.bottom));
            }
            rect.offset(view.getLeft(), view.getTop());
        }

        public int getWidth() {
            return this.mWidth;
        }

        public int getWidthMode() {
            return this.mWidthMode;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean hasFlexibleChildInBothOrientations() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ViewGroup.LayoutParams layoutParams = getChildAt(i).getLayoutParams();
                if (layoutParams.width < 0 && layoutParams.height < 0) {
                    return true;
                }
            }
            return false;
        }

        public boolean isLayoutHierarchical(Recycler recycler, State state) {
            return false;
        }

        public boolean isSmoothScrolling() {
            return this.mSmoothScroller != null ? this.mSmoothScroller.isRunning() : false;
        }

        public void layoutDecoratedWithMargins(View view, int i, int i2, int i3, int i4) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Rect rect = layoutParams.mDecorInsets;
            view.layout(rect.left + i + layoutParams.leftMargin, rect.top + i2 + layoutParams.topMargin, (i3 - rect.right) - layoutParams.rightMargin, (i4 - rect.bottom) - layoutParams.bottomMargin);
        }

        public void measureChildWithMargins(View view, int i, int i2) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Rect itemDecorInsetsForChild = this.mRecyclerView.getItemDecorInsetsForChild(view);
            int i3 = itemDecorInsetsForChild.left;
            int i4 = itemDecorInsetsForChild.right;
            int i5 = itemDecorInsetsForChild.top;
            int i6 = itemDecorInsetsForChild.bottom;
            int childMeasureSpec = getChildMeasureSpec(getWidth(), getWidthMode(), getPaddingLeft() + getPaddingRight() + layoutParams.leftMargin + layoutParams.rightMargin + i + i3 + i4, layoutParams.width, canScrollHorizontally());
            int childMeasureSpec2 = getChildMeasureSpec(getHeight(), getHeightMode(), getPaddingTop() + getPaddingBottom() + layoutParams.topMargin + layoutParams.bottomMargin + i2 + i5 + i6, layoutParams.height, canScrollVertically());
            if (shouldMeasureChild(view, childMeasureSpec, childMeasureSpec2, layoutParams)) {
                view.measure(childMeasureSpec, childMeasureSpec2);
            }
        }

        public void moveView(int i, int i2) {
            View childAt = getChildAt(i);
            if (childAt == null) {
                throw new IllegalArgumentException("Cannot move a child from non-existing index:" + i);
            }
            detachViewAt(i);
            attachView(childAt, i2);
        }

        public void offsetChildrenHorizontal(int i) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.offsetChildrenHorizontal(i);
            }
        }

        public void offsetChildrenVertical(int i) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.offsetChildrenVertical(i);
            }
        }

        public void onAdapterChanged(Adapter adapter, Adapter adapter2) {
        }

        public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> arrayList, int i, int i2) {
            return false;
        }

        @CallSuper
        public void onAttachedToWindow(RecyclerView recyclerView) {
        }

        @Deprecated
        public void onDetachedFromWindow(RecyclerView recyclerView) {
        }

        @CallSuper
        public void onDetachedFromWindow(RecyclerView recyclerView, Recycler recycler) {
            onDetachedFromWindow(recyclerView);
        }

        @Nullable
        public View onFocusSearchFailed(View view, int i, Recycler recycler, State state) {
            return null;
        }

        public void onInitializeAccessibilityEvent(Recycler recycler, State state, AccessibilityEvent accessibilityEvent) {
            AccessibilityRecordCompat asRecord = AccessibilityEventCompat.asRecord(accessibilityEvent);
            if (this.mRecyclerView == null || asRecord == null) {
                return;
            }
            boolean z = true;
            if (!ViewCompat.canScrollVertically(this.mRecyclerView, 1)) {
                z = true;
                if (!ViewCompat.canScrollVertically(this.mRecyclerView, -1)) {
                    z = true;
                    if (!ViewCompat.canScrollHorizontally(this.mRecyclerView, -1)) {
                        z = ViewCompat.canScrollHorizontally(this.mRecyclerView, 1);
                    }
                }
            }
            asRecord.setScrollable(z);
            if (this.mRecyclerView.mAdapter != null) {
                asRecord.setItemCount(this.mRecyclerView.mAdapter.getItemCount());
            }
        }

        public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            onInitializeAccessibilityEvent(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, accessibilityEvent);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            onInitializeAccessibilityNodeInfo(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, accessibilityNodeInfoCompat);
        }

        public void onInitializeAccessibilityNodeInfo(Recycler recycler, State state, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            if (ViewCompat.canScrollVertically(this.mRecyclerView, -1) || ViewCompat.canScrollHorizontally(this.mRecyclerView, -1)) {
                accessibilityNodeInfoCompat.addAction(8192);
                accessibilityNodeInfoCompat.setScrollable(true);
            }
            if (ViewCompat.canScrollVertically(this.mRecyclerView, 1) || ViewCompat.canScrollHorizontally(this.mRecyclerView, 1)) {
                accessibilityNodeInfoCompat.addAction(4096);
                accessibilityNodeInfoCompat.setScrollable(true);
            }
            accessibilityNodeInfoCompat.setCollectionInfo(AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state)));
        }

        public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(canScrollVertically() ? getPosition(view) : 0, 1, canScrollHorizontally() ? getPosition(view) : 0, 1, false, false));
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void onInitializeAccessibilityNodeInfoForItem(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt == null || childViewHolderInt.isRemoved() || this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                return;
            }
            onInitializeAccessibilityNodeInfoForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, view, accessibilityNodeInfoCompat);
        }

        public View onInterceptFocusSearch(View view, int i) {
            return null;
        }

        public void onItemsAdded(RecyclerView recyclerView, int i, int i2) {
        }

        public void onItemsChanged(RecyclerView recyclerView) {
        }

        public void onItemsMoved(RecyclerView recyclerView, int i, int i2, int i3) {
        }

        public void onItemsRemoved(RecyclerView recyclerView, int i, int i2) {
        }

        public void onItemsUpdated(RecyclerView recyclerView, int i, int i2) {
        }

        public void onItemsUpdated(RecyclerView recyclerView, int i, int i2, Object obj) {
            onItemsUpdated(recyclerView, i, i2);
        }

        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e("RecyclerView", "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        public void onLayoutCompleted(State state) {
        }

        public void onMeasure(Recycler recycler, State state, int i, int i2) {
            this.mRecyclerView.defaultOnMeasure(i, i2);
        }

        public boolean onRequestChildFocus(RecyclerView recyclerView, State state, View view, View view2) {
            return onRequestChildFocus(recyclerView, view, view2);
        }

        @Deprecated
        public boolean onRequestChildFocus(RecyclerView recyclerView, View view, View view2) {
            return !isSmoothScrolling() ? recyclerView.isComputingLayout() : true;
        }

        public void onRestoreInstanceState(Parcelable parcelable) {
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onScrollStateChanged(int i) {
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean performAccessibilityAction(int i, Bundle bundle) {
            return performAccessibilityAction(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, i, bundle);
        }

        public boolean performAccessibilityAction(Recycler recycler, State state, int i, Bundle bundle) {
            if (this.mRecyclerView == null) {
                return false;
            }
            int i2 = 0;
            int i3 = 0;
            switch (i) {
                case 4096:
                    int i4 = 0;
                    if (ViewCompat.canScrollVertically(this.mRecyclerView, 1)) {
                        i4 = (getHeight() - getPaddingTop()) - getPaddingBottom();
                    }
                    i2 = i4;
                    if (ViewCompat.canScrollHorizontally(this.mRecyclerView, 1)) {
                        i3 = (getWidth() - getPaddingLeft()) - getPaddingRight();
                        i2 = i4;
                        break;
                    }
                    break;
                case 8192:
                    int i5 = 0;
                    if (ViewCompat.canScrollVertically(this.mRecyclerView, -1)) {
                        i5 = -((getHeight() - getPaddingTop()) - getPaddingBottom());
                    }
                    i2 = i5;
                    if (ViewCompat.canScrollHorizontally(this.mRecyclerView, -1)) {
                        i3 = -((getWidth() - getPaddingLeft()) - getPaddingRight());
                        i2 = i5;
                        break;
                    }
                    break;
            }
            if (i2 == 0 && i3 == 0) {
                return false;
            }
            this.mRecyclerView.scrollBy(i3, i2);
            return true;
        }

        public boolean performAccessibilityActionForItem(Recycler recycler, State state, View view, int i, Bundle bundle) {
            return false;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean performAccessibilityActionForItem(View view, int i, Bundle bundle) {
            return performAccessibilityActionForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, view, i, bundle);
        }

        public void removeAndRecycleAllViews(Recycler recycler) {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                if (!RecyclerView.getChildViewHolderInt(getChildAt(childCount)).shouldIgnore()) {
                    removeAndRecycleViewAt(childCount, recycler);
                }
            }
        }

        void removeAndRecycleScrapInt(Recycler recycler) {
            int scrapCount = recycler.getScrapCount();
            for (int i = scrapCount - 1; i >= 0; i--) {
                View scrapViewAt = recycler.getScrapViewAt(i);
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(scrapViewAt);
                if (!childViewHolderInt.shouldIgnore()) {
                    childViewHolderInt.setIsRecyclable(false);
                    if (childViewHolderInt.isTmpDetached()) {
                        this.mRecyclerView.removeDetachedView(scrapViewAt, false);
                    }
                    if (this.mRecyclerView.mItemAnimator != null) {
                        this.mRecyclerView.mItemAnimator.endAnimation(childViewHolderInt);
                    }
                    childViewHolderInt.setIsRecyclable(true);
                    recycler.quickRecycleScrapView(scrapViewAt);
                }
            }
            recycler.clearScrap();
            if (scrapCount > 0) {
                this.mRecyclerView.invalidate();
            }
        }

        public void removeAndRecycleView(View view, Recycler recycler) {
            removeView(view);
            recycler.recycleView(view);
        }

        public void removeAndRecycleViewAt(int i, Recycler recycler) {
            View childAt = getChildAt(i);
            removeViewAt(i);
            recycler.recycleView(childAt);
        }

        public void removeView(View view) {
            this.mChildHelper.removeView(view);
        }

        public void removeViewAt(int i) {
            if (getChildAt(i) != null) {
                this.mChildHelper.removeViewAt(i);
            }
        }

        public boolean requestChildRectangleOnScreen(RecyclerView recyclerView, View view, Rect rect, boolean z) {
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int width = getWidth() - getPaddingRight();
            int height = getHeight();
            int paddingBottom = getPaddingBottom();
            int left = (view.getLeft() + rect.left) - view.getScrollX();
            int top = (view.getTop() + rect.top) - view.getScrollY();
            int width2 = left + rect.width();
            int height2 = rect.height();
            int min = Math.min(0, left - paddingLeft);
            int min2 = Math.min(0, top - paddingTop);
            int max = Math.max(0, width2 - width);
            int max2 = Math.max(0, (top + height2) - (height - paddingBottom));
            if (getLayoutDirection() != 1) {
                max = min != 0 ? min : Math.min(left - paddingLeft, max);
            } else if (max == 0) {
                max = Math.max(min, width2 - width);
            }
            if (min2 == 0) {
                min2 = Math.min(top - paddingTop, max2);
            }
            if (max == 0 && min2 == 0) {
                return false;
            }
            if (z) {
                recyclerView.scrollBy(max, min2);
                return true;
            }
            recyclerView.smoothScrollBy(max, min2);
            return true;
        }

        public void requestLayout() {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.requestLayout();
            }
        }

        public int scrollHorizontallyBy(int i, Recycler recycler, State state) {
            return 0;
        }

        public void scrollToPosition(int i) {
        }

        public int scrollVerticallyBy(int i, Recycler recycler, State state) {
            return 0;
        }

        public void setAutoMeasureEnabled(boolean z) {
            this.mAutoMeasure = z;
        }

        void setExactMeasureSpecsFrom(RecyclerView recyclerView) {
            setMeasureSpecs(View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), 1073741824));
        }

        void setMeasureSpecs(int i, int i2) {
            this.mWidth = View.MeasureSpec.getSize(i);
            this.mWidthMode = View.MeasureSpec.getMode(i);
            if (this.mWidthMode == 0 && !RecyclerView.ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                this.mWidth = 0;
            }
            this.mHeight = View.MeasureSpec.getSize(i2);
            this.mHeightMode = View.MeasureSpec.getMode(i2);
            if (this.mHeightMode != 0 || RecyclerView.ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                return;
            }
            this.mHeight = 0;
        }

        public void setMeasuredDimension(int i, int i2) {
            this.mRecyclerView.setMeasuredDimension(i, i2);
        }

        public void setMeasuredDimension(Rect rect, int i, int i2) {
            int width = rect.width();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int height = rect.height();
            int paddingTop = getPaddingTop();
            setMeasuredDimension(chooseSize(i, width + paddingLeft + paddingRight, getMinimumWidth()), chooseSize(i2, height + paddingTop + getPaddingBottom(), getMinimumHeight()));
        }

        void setMeasuredDimensionFromChildren(int i, int i2) {
            int childCount = getChildCount();
            if (childCount == 0) {
                this.mRecyclerView.defaultOnMeasure(i, i2);
                return;
            }
            int i3 = Integer.MAX_VALUE;
            int i4 = Integer.MAX_VALUE;
            int i5 = Integer.MIN_VALUE;
            int i6 = Integer.MIN_VALUE;
            int i7 = 0;
            while (i7 < childCount) {
                View childAt = getChildAt(i7);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                Rect rect = this.mRecyclerView.mTempRect;
                getDecoratedBoundsWithMargins(childAt, rect);
                int i8 = i3;
                if (rect.left < i3) {
                    i8 = rect.left;
                }
                int i9 = i5;
                if (rect.right > i5) {
                    i9 = rect.right;
                }
                int i10 = i4;
                if (rect.top < i4) {
                    i10 = rect.top;
                }
                int i11 = i6;
                if (rect.bottom > i6) {
                    i11 = rect.bottom;
                }
                i7++;
                i5 = i9;
                i6 = i11;
                i3 = i8;
                i4 = i10;
            }
            this.mRecyclerView.mTempRect.set(i3, i4, i5, i6);
            setMeasuredDimension(this.mRecyclerView.mTempRect, i, i2);
        }

        void setRecyclerView(RecyclerView recyclerView) {
            if (recyclerView == null) {
                this.mRecyclerView = null;
                this.mChildHelper = null;
                this.mWidth = 0;
                this.mHeight = 0;
            } else {
                this.mRecyclerView = recyclerView;
                this.mChildHelper = recyclerView.mChildHelper;
                this.mWidth = recyclerView.getWidth();
                this.mHeight = recyclerView.getHeight();
            }
            this.mWidthMode = 1073741824;
            this.mHeightMode = 1073741824;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean shouldMeasureChild(View view, int i, int i2, LayoutParams layoutParams) {
            boolean z = true;
            if (!view.isLayoutRequested()) {
                z = true;
                if (this.mMeasurementCacheEnabled) {
                    z = true;
                    if (isMeasurementUpToDate(view.getWidth(), i, layoutParams.width)) {
                        z = true;
                        if (isMeasurementUpToDate(view.getHeight(), i2, layoutParams.height)) {
                            z = false;
                        }
                    }
                }
            }
            return z;
        }

        boolean shouldMeasureTwice() {
            return false;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean shouldReMeasureChild(View view, int i, int i2, LayoutParams layoutParams) {
            boolean z = true;
            if (this.mMeasurementCacheEnabled) {
                z = true;
                if (isMeasurementUpToDate(view.getMeasuredWidth(), i, layoutParams.width)) {
                    z = true;
                    if (isMeasurementUpToDate(view.getMeasuredHeight(), i2, layoutParams.height)) {
                        z = false;
                    }
                }
            }
            return z;
        }

        void stopSmoothScroller() {
            if (this.mSmoothScroller != null) {
                this.mSmoothScroller.stop();
            }
        }

        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$LayoutParams.class */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        final Rect mDecorInsets;
        boolean mInsetsDirty;
        boolean mPendingInvalidate;
        ViewHolder mViewHolder;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((ViewGroup.LayoutParams) layoutParams);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public int getViewLayoutPosition() {
            return this.mViewHolder.getLayoutPosition();
        }

        public boolean isItemChanged() {
            return this.mViewHolder.isUpdated();
        }

        public boolean isItemRemoved() {
            return this.mViewHolder.isRemoved();
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$OnChildAttachStateChangeListener.class */
    public interface OnChildAttachStateChangeListener {
        void onChildViewAttachedToWindow(View view);

        void onChildViewDetachedFromWindow(View view);
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$OnItemTouchListener.class */
    public interface OnItemTouchListener {
        boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent);

        void onRequestDisallowInterceptTouchEvent(boolean z);

        void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent);
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$OnScrollListener.class */
    public static abstract class OnScrollListener {
        public void onScrollStateChanged(RecyclerView recyclerView, int i) {
        }

        public void onScrolled(RecyclerView recyclerView, int i, int i2) {
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$RecycledViewPool.class */
    public static class RecycledViewPool {
        private SparseArray<ArrayList<ViewHolder>> mScrap = new SparseArray<>();
        private SparseIntArray mMaxScrap = new SparseIntArray();
        private int mAttachCount = 0;

        private ArrayList<ViewHolder> getScrapHeapForType(int i) {
            ArrayList<ViewHolder> arrayList = this.mScrap.get(i);
            ArrayList<ViewHolder> arrayList2 = arrayList;
            if (arrayList == null) {
                ArrayList<ViewHolder> arrayList3 = new ArrayList<>();
                this.mScrap.put(i, arrayList3);
                arrayList2 = arrayList3;
                if (this.mMaxScrap.indexOfKey(i) < 0) {
                    this.mMaxScrap.put(i, 5);
                    arrayList2 = arrayList3;
                }
            }
            return arrayList2;
        }

        void attach(Adapter adapter) {
            this.mAttachCount++;
        }

        public void clear() {
            this.mScrap.clear();
        }

        void detach() {
            this.mAttachCount--;
        }

        public ViewHolder getRecycledView(int i) {
            ArrayList<ViewHolder> arrayList = this.mScrap.get(i);
            if (arrayList == null || arrayList.isEmpty()) {
                return null;
            }
            int size = arrayList.size() - 1;
            ViewHolder viewHolder = arrayList.get(size);
            arrayList.remove(size);
            return viewHolder;
        }

        void onAdapterChanged(Adapter adapter, Adapter adapter2, boolean z) {
            if (adapter != null) {
                detach();
            }
            if (!z && this.mAttachCount == 0) {
                clear();
            }
            if (adapter2 != null) {
                attach(adapter2);
            }
        }

        public void putRecycledView(ViewHolder viewHolder) {
            int itemViewType = viewHolder.getItemViewType();
            ArrayList<ViewHolder> scrapHeapForType = getScrapHeapForType(itemViewType);
            if (this.mMaxScrap.get(itemViewType) <= scrapHeapForType.size()) {
                return;
            }
            viewHolder.resetInternal();
            scrapHeapForType.add(viewHolder);
        }

        public void setMaxRecycledViews(int i, int i2) {
            this.mMaxScrap.put(i, i2);
            ArrayList<ViewHolder> arrayList = this.mScrap.get(i);
            if (arrayList != null) {
                while (arrayList.size() > i2) {
                    arrayList.remove(arrayList.size() - 1);
                }
            }
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$Recycler.class */
    public final class Recycler {
        private RecycledViewPool mRecyclerPool;
        private ViewCacheExtension mViewCacheExtension;
        final RecyclerView this$0;
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        private ArrayList<ViewHolder> mChangedScrap = null;
        final ArrayList<ViewHolder> mCachedViews = new ArrayList<>();
        private final List<ViewHolder> mUnmodifiableAttachedScrap = Collections.unmodifiableList(this.mAttachedScrap);
        private int mViewCacheMax = 2;

        public Recycler(RecyclerView recyclerView) {
            this.this$0 = recyclerView;
        }

        private void attachAccessibilityDelegate(View view) {
            if (this.this$0.isAccessibilityEnabled()) {
                if (ViewCompat.getImportantForAccessibility(view) == 0) {
                    ViewCompat.setImportantForAccessibility(view, 1);
                }
                if (ViewCompat.hasAccessibilityDelegate(view)) {
                    return;
                }
                ViewCompat.setAccessibilityDelegate(view, this.this$0.mAccessibilityDelegate.getItemDelegate());
            }
        }

        private void invalidateDisplayListInt(ViewHolder viewHolder) {
            if (viewHolder.itemView instanceof ViewGroup) {
                invalidateDisplayListInt((ViewGroup) viewHolder.itemView, false);
            }
        }

        private void invalidateDisplayListInt(ViewGroup viewGroup, boolean z) {
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                View childAt = viewGroup.getChildAt(childCount);
                if (childAt instanceof ViewGroup) {
                    invalidateDisplayListInt((ViewGroup) childAt, true);
                }
            }
            if (z) {
                if (viewGroup.getVisibility() == 4) {
                    viewGroup.setVisibility(0);
                    viewGroup.setVisibility(4);
                    return;
                }
                int visibility = viewGroup.getVisibility();
                viewGroup.setVisibility(4);
                viewGroup.setVisibility(visibility);
            }
        }

        void addViewHolderToRecycledViewPool(ViewHolder viewHolder) {
            ViewCompat.setAccessibilityDelegate(viewHolder.itemView, null);
            dispatchViewRecycled(viewHolder);
            viewHolder.mOwnerRecyclerView = null;
            getRecycledViewPool().putRecycledView(viewHolder);
        }

        public void clear() {
            this.mAttachedScrap.clear();
            recycleAndClearCachedViews();
        }

        void clearOldPositions() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                this.mCachedViews.get(i).clearOldPosition();
            }
            int size2 = this.mAttachedScrap.size();
            for (int i2 = 0; i2 < size2; i2++) {
                this.mAttachedScrap.get(i2).clearOldPosition();
            }
            if (this.mChangedScrap != null) {
                int size3 = this.mChangedScrap.size();
                for (int i3 = 0; i3 < size3; i3++) {
                    this.mChangedScrap.get(i3).clearOldPosition();
                }
            }
        }

        void clearScrap() {
            this.mAttachedScrap.clear();
            if (this.mChangedScrap != null) {
                this.mChangedScrap.clear();
            }
        }

        public int convertPreLayoutPositionToPostLayout(int i) {
            if (i < 0 || i >= this.this$0.mState.getItemCount()) {
                throw new IndexOutOfBoundsException("invalid position " + i + ". State item count is " + this.this$0.mState.getItemCount());
            }
            return !this.this$0.mState.isPreLayout() ? i : this.this$0.mAdapterHelper.findPositionOffset(i);
        }

        void dispatchViewRecycled(ViewHolder viewHolder) {
            if (this.this$0.mRecyclerListener != null) {
                this.this$0.mRecyclerListener.onViewRecycled(viewHolder);
            }
            if (this.this$0.mAdapter != null) {
                this.this$0.mAdapter.onViewRecycled(viewHolder);
            }
            if (this.this$0.mState != null) {
                this.this$0.mViewInfoStore.removeViewHolder(viewHolder);
            }
        }

        ViewHolder getChangedScrapViewForPosition(int i) {
            int size;
            int findPositionOffset;
            if (this.mChangedScrap == null || (size = this.mChangedScrap.size()) == 0) {
                return null;
            }
            for (int i2 = 0; i2 < size; i2++) {
                ViewHolder viewHolder = this.mChangedScrap.get(i2);
                if (!viewHolder.wasReturnedFromScrap() && viewHolder.getLayoutPosition() == i) {
                    viewHolder.addFlags(32);
                    return viewHolder;
                }
            }
            if (!this.this$0.mAdapter.hasStableIds() || (findPositionOffset = this.this$0.mAdapterHelper.findPositionOffset(i)) <= 0 || findPositionOffset >= this.this$0.mAdapter.getItemCount()) {
                return null;
            }
            long itemId = this.this$0.mAdapter.getItemId(findPositionOffset);
            for (int i3 = 0; i3 < size; i3++) {
                ViewHolder viewHolder2 = this.mChangedScrap.get(i3);
                if (!viewHolder2.wasReturnedFromScrap() && viewHolder2.getItemId() == itemId) {
                    viewHolder2.addFlags(32);
                    return viewHolder2;
                }
            }
            return null;
        }

        RecycledViewPool getRecycledViewPool() {
            if (this.mRecyclerPool == null) {
                this.mRecyclerPool = new RecycledViewPool();
            }
            return this.mRecyclerPool;
        }

        int getScrapCount() {
            return this.mAttachedScrap.size();
        }

        public List<ViewHolder> getScrapList() {
            return this.mUnmodifiableAttachedScrap;
        }

        View getScrapViewAt(int i) {
            return this.mAttachedScrap.get(i).itemView;
        }

        ViewHolder getScrapViewForId(long j, int i, boolean z) {
            for (int size = this.mAttachedScrap.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = this.mAttachedScrap.get(size);
                if (viewHolder.getItemId() == j && !viewHolder.wasReturnedFromScrap()) {
                    if (i == viewHolder.getItemViewType()) {
                        viewHolder.addFlags(32);
                        if (viewHolder.isRemoved() && !this.this$0.mState.isPreLayout()) {
                            viewHolder.setFlags(2, 14);
                        }
                        return viewHolder;
                    } else if (!z) {
                        this.mAttachedScrap.remove(size);
                        this.this$0.removeDetachedView(viewHolder.itemView, false);
                        quickRecycleScrapView(viewHolder.itemView);
                    }
                }
            }
            for (int size2 = this.mCachedViews.size() - 1; size2 >= 0; size2--) {
                ViewHolder viewHolder2 = this.mCachedViews.get(size2);
                if (viewHolder2.getItemId() == j) {
                    if (i == viewHolder2.getItemViewType()) {
                        if (!z) {
                            this.mCachedViews.remove(size2);
                        }
                        return viewHolder2;
                    } else if (!z) {
                        recycleCachedViewAt(size2);
                    }
                }
            }
            return null;
        }

        /* JADX WARN: Removed duplicated region for block: B:36:0x0132  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        ViewHolder getScrapViewForPosition(int i, int i2, boolean z) {
            int size;
            int i3;
            View findHiddenNonRemovedView;
            int size2 = this.mAttachedScrap.size();
            for (int i4 = 0; i4 < size2; i4++) {
                ViewHolder viewHolder = this.mAttachedScrap.get(i4);
                if (!viewHolder.wasReturnedFromScrap() && viewHolder.getLayoutPosition() == i && !viewHolder.isInvalid() && (this.this$0.mState.mInPreLayout || !viewHolder.isRemoved())) {
                    if (i2 == -1 || viewHolder.getItemViewType() == i2) {
                        viewHolder.addFlags(32);
                        return viewHolder;
                    }
                    Log.e("RecyclerView", "Scrap view for position " + i + " isn't dirty but has wrong view type! (found " + viewHolder.getItemViewType() + " but expected " + i2 + ")");
                    if (z && (findHiddenNonRemovedView = this.this$0.mChildHelper.findHiddenNonRemovedView(i, i2)) != null) {
                        ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(findHiddenNonRemovedView);
                        this.this$0.mChildHelper.unhide(findHiddenNonRemovedView);
                        int indexOfChild = this.this$0.mChildHelper.indexOfChild(findHiddenNonRemovedView);
                        if (indexOfChild == -1) {
                            throw new IllegalStateException("layout index should not be -1 after unhiding a view:" + childViewHolderInt);
                        }
                        this.this$0.mChildHelper.detachViewFromParent(indexOfChild);
                        scrapView(findHiddenNonRemovedView);
                        childViewHolderInt.addFlags(8224);
                        return childViewHolderInt;
                    }
                    size = this.mCachedViews.size();
                    for (i3 = 0; i3 < size; i3++) {
                        ViewHolder viewHolder2 = this.mCachedViews.get(i3);
                        if (!viewHolder2.isInvalid() && viewHolder2.getLayoutPosition() == i) {
                            if (!z) {
                                this.mCachedViews.remove(i3);
                            }
                            return viewHolder2;
                        }
                    }
                    return null;
                }
            }
            if (z) {
            }
            size = this.mCachedViews.size();
            while (i3 < size) {
            }
            return null;
        }

        public View getViewForPosition(int i) {
            return getViewForPosition(i, false);
        }

        View getViewForPosition(int i, boolean z) {
            LayoutParams layoutParams;
            if (i < 0 || i >= this.this$0.mState.getItemCount()) {
                throw new IndexOutOfBoundsException("Invalid item position " + i + "(" + i + "). Item count:" + this.this$0.mState.getItemCount());
            }
            boolean z2 = false;
            ViewHolder viewHolder = null;
            if (this.this$0.mState.isPreLayout()) {
                viewHolder = getChangedScrapViewForPosition(i);
                z2 = viewHolder != null;
            }
            boolean z3 = z2;
            ViewHolder viewHolder2 = viewHolder;
            if (viewHolder == null) {
                ViewHolder scrapViewForPosition = getScrapViewForPosition(i, -1, z);
                z3 = z2;
                viewHolder2 = scrapViewForPosition;
                if (scrapViewForPosition != null) {
                    if (validateViewHolderForOffsetPosition(scrapViewForPosition)) {
                        z3 = true;
                        viewHolder2 = scrapViewForPosition;
                    } else {
                        if (!z) {
                            scrapViewForPosition.addFlags(4);
                            if (scrapViewForPosition.isScrap()) {
                                this.this$0.removeDetachedView(scrapViewForPosition.itemView, false);
                                scrapViewForPosition.unScrap();
                            } else if (scrapViewForPosition.wasReturnedFromScrap()) {
                                scrapViewForPosition.clearReturnedFromScrapFlag();
                            }
                            recycleViewHolderInternal(scrapViewForPosition);
                        }
                        viewHolder2 = null;
                        z3 = z2;
                    }
                }
            }
            boolean z4 = z3;
            ViewHolder viewHolder3 = viewHolder2;
            if (viewHolder2 == null) {
                int findPositionOffset = this.this$0.mAdapterHelper.findPositionOffset(i);
                if (findPositionOffset < 0 || findPositionOffset >= this.this$0.mAdapter.getItemCount()) {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item position " + i + "(offset:" + findPositionOffset + ").state:" + this.this$0.mState.getItemCount());
                }
                int itemViewType = this.this$0.mAdapter.getItemViewType(findPositionOffset);
                boolean z5 = z3;
                ViewHolder viewHolder4 = viewHolder2;
                if (this.this$0.mAdapter.hasStableIds()) {
                    ViewHolder scrapViewForId = getScrapViewForId(this.this$0.mAdapter.getItemId(findPositionOffset), itemViewType, z);
                    z5 = z3;
                    viewHolder4 = scrapViewForId;
                    if (scrapViewForId != null) {
                        scrapViewForId.mPosition = findPositionOffset;
                        z5 = true;
                        viewHolder4 = scrapViewForId;
                    }
                }
                ViewHolder viewHolder5 = viewHolder4;
                if (viewHolder4 == null) {
                    viewHolder5 = viewHolder4;
                    if (this.mViewCacheExtension != null) {
                        View viewForPositionAndType = this.mViewCacheExtension.getViewForPositionAndType(this, i, itemViewType);
                        viewHolder5 = viewHolder4;
                        if (viewForPositionAndType != null) {
                            ViewHolder childViewHolder = this.this$0.getChildViewHolder(viewForPositionAndType);
                            if (childViewHolder == null) {
                                throw new IllegalArgumentException("getViewForPositionAndType returned a view which does not have a ViewHolder");
                            }
                            viewHolder5 = childViewHolder;
                            if (childViewHolder.shouldIgnore()) {
                                throw new IllegalArgumentException("getViewForPositionAndType returned a view that is ignored. You must call stopIgnoring before returning this view.");
                            }
                        }
                    }
                }
                ViewHolder viewHolder6 = viewHolder5;
                if (viewHolder5 == null) {
                    ViewHolder recycledView = getRecycledViewPool().getRecycledView(itemViewType);
                    viewHolder6 = recycledView;
                    if (recycledView != null) {
                        recycledView.resetInternal();
                        viewHolder6 = recycledView;
                        if (RecyclerView.FORCE_INVALIDATE_DISPLAY_LIST) {
                            invalidateDisplayListInt(recycledView);
                            viewHolder6 = recycledView;
                        }
                    }
                }
                z4 = z5;
                viewHolder3 = viewHolder6;
                if (viewHolder6 == null) {
                    viewHolder3 = this.this$0.mAdapter.createViewHolder(this.this$0, itemViewType);
                    z4 = z5;
                }
            }
            if (z4 && !this.this$0.mState.isPreLayout() && viewHolder3.hasAnyOfTheFlags(8192)) {
                viewHolder3.setFlags(0, 8192);
                if (this.this$0.mState.mRunSimpleAnimations) {
                    this.this$0.recordAnimationInfoIfBouncedHiddenView(viewHolder3, this.this$0.mItemAnimator.recordPreLayoutInformation(this.this$0.mState, viewHolder3, ItemAnimator.buildAdapterChangeFlagsForAnimations(viewHolder3) | 4096, viewHolder3.getUnmodifiedPayloads()));
                }
            }
            boolean z6 = false;
            if (this.this$0.mState.isPreLayout() && viewHolder3.isBound()) {
                viewHolder3.mPreLayoutPosition = i;
            } else if (!viewHolder3.isBound() || viewHolder3.needsUpdate() || viewHolder3.isInvalid()) {
                int findPositionOffset2 = this.this$0.mAdapterHelper.findPositionOffset(i);
                viewHolder3.mOwnerRecyclerView = this.this$0;
                this.this$0.mAdapter.bindViewHolder(viewHolder3, findPositionOffset2);
                attachAccessibilityDelegate(viewHolder3.itemView);
                z6 = true;
                if (this.this$0.mState.isPreLayout()) {
                    viewHolder3.mPreLayoutPosition = i;
                    z6 = true;
                }
            }
            ViewGroup.LayoutParams layoutParams2 = viewHolder3.itemView.getLayoutParams();
            if (layoutParams2 == null) {
                layoutParams = (LayoutParams) this.this$0.generateDefaultLayoutParams();
                viewHolder3.itemView.setLayoutParams(layoutParams);
            } else if (this.this$0.checkLayoutParams(layoutParams2)) {
                layoutParams = (LayoutParams) layoutParams2;
            } else {
                layoutParams = (LayoutParams) this.this$0.generateLayoutParams(layoutParams2);
                viewHolder3.itemView.setLayoutParams(layoutParams);
            }
            layoutParams.mViewHolder = viewHolder3;
            if (!z4) {
                z6 = false;
            }
            layoutParams.mPendingInvalidate = z6;
            return viewHolder3.itemView;
        }

        void markItemDecorInsetsDirty() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                LayoutParams layoutParams = (LayoutParams) this.mCachedViews.get(i).itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.mInsetsDirty = true;
                }
            }
        }

        void markKnownViewsInvalid() {
            if (this.this$0.mAdapter == null || !this.this$0.mAdapter.hasStableIds()) {
                recycleAndClearCachedViews();
                return;
            }
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                ViewHolder viewHolder = this.mCachedViews.get(i);
                if (viewHolder != null) {
                    viewHolder.addFlags(6);
                    viewHolder.addChangePayload(null);
                }
            }
        }

        void offsetPositionRecordsForInsert(int i, int i2) {
            int size = this.mCachedViews.size();
            for (int i3 = 0; i3 < size; i3++) {
                ViewHolder viewHolder = this.mCachedViews.get(i3);
                if (viewHolder != null && viewHolder.mPosition >= i) {
                    viewHolder.offsetPosition(i2, true);
                }
            }
        }

        void offsetPositionRecordsForMove(int i, int i2) {
            int i3;
            int i4;
            int i5;
            if (i < i2) {
                i3 = i;
                i4 = i2;
                i5 = -1;
            } else {
                i3 = i2;
                i4 = i;
                i5 = 1;
            }
            int size = this.mCachedViews.size();
            for (int i6 = 0; i6 < size; i6++) {
                ViewHolder viewHolder = this.mCachedViews.get(i6);
                if (viewHolder != null && viewHolder.mPosition >= i3 && viewHolder.mPosition <= i4) {
                    if (viewHolder.mPosition == i) {
                        viewHolder.offsetPosition(i2 - i, false);
                    } else {
                        viewHolder.offsetPosition(i5, false);
                    }
                }
            }
        }

        void offsetPositionRecordsForRemove(int i, int i2, boolean z) {
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = this.mCachedViews.get(size);
                if (viewHolder != null) {
                    if (viewHolder.mPosition >= i + i2) {
                        viewHolder.offsetPosition(-i2, z);
                    } else if (viewHolder.mPosition >= i) {
                        viewHolder.addFlags(8);
                        recycleCachedViewAt(size);
                    }
                }
            }
        }

        void onAdapterChanged(Adapter adapter, Adapter adapter2, boolean z) {
            clear();
            getRecycledViewPool().onAdapterChanged(adapter, adapter2, z);
        }

        void quickRecycleScrapView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            childViewHolderInt.mScrapContainer = null;
            childViewHolderInt.mInChangeScrap = false;
            childViewHolderInt.clearReturnedFromScrapFlag();
            recycleViewHolderInternal(childViewHolderInt);
        }

        void recycleAndClearCachedViews() {
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                recycleCachedViewAt(size);
            }
            this.mCachedViews.clear();
        }

        void recycleCachedViewAt(int i) {
            addViewHolderToRecycledViewPool(this.mCachedViews.get(i));
            this.mCachedViews.remove(i);
        }

        public void recycleView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.isTmpDetached()) {
                this.this$0.removeDetachedView(view, false);
            }
            if (childViewHolderInt.isScrap()) {
                childViewHolderInt.unScrap();
            } else if (childViewHolderInt.wasReturnedFromScrap()) {
                childViewHolderInt.clearReturnedFromScrapFlag();
            }
            recycleViewHolderInternal(childViewHolderInt);
        }

        /* JADX WARN: Code restructure failed: missing block: B:27:0x00b5, code lost:
            if (r6.isRecyclable() != false) goto L34;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        void recycleViewHolderInternal(ViewHolder viewHolder) {
            boolean z;
            boolean z2 = false;
            if (viewHolder.isScrap() || viewHolder.itemView.getParent() != null) {
                StringBuilder append = new StringBuilder().append("Scrapped or attached views may not be recycled. isScrap:").append(viewHolder.isScrap()).append(" isAttached:");
                if (viewHolder.itemView.getParent() != null) {
                    z2 = true;
                }
                throw new IllegalArgumentException(append.append(z2).toString());
            } else if (viewHolder.isTmpDetached()) {
                throw new IllegalArgumentException("Tmp detached view should be removed from RecyclerView before it can be recycled: " + viewHolder);
            } else {
                if (viewHolder.shouldIgnore()) {
                    throw new IllegalArgumentException("Trying to recycle an ignored view holder. You should first call stopIgnoringView(view) before calling recycle.");
                }
                boolean doesTransientStatePreventRecycling = viewHolder.doesTransientStatePreventRecycling();
                boolean z3 = false;
                if (!((this.this$0.mAdapter == null || !doesTransientStatePreventRecycling) ? false : this.this$0.mAdapter.onFailedToRecycleView(viewHolder))) {
                    z = false;
                }
                boolean z4 = false;
                if (!viewHolder.hasAnyOfTheFlags(14)) {
                    int size = this.mCachedViews.size();
                    int i = size;
                    if (size >= this.mViewCacheMax) {
                        i = size;
                        if (size > 0) {
                            recycleCachedViewAt(0);
                            i = size - 1;
                        }
                    }
                    z4 = false;
                    if (i < this.mViewCacheMax) {
                        this.mCachedViews.add(viewHolder);
                        z4 = true;
                    }
                }
                z3 = z4;
                z = false;
                if (!z4) {
                    addViewHolderToRecycledViewPool(viewHolder);
                    z = true;
                    z3 = z4;
                }
                this.this$0.mViewInfoStore.removeViewHolder(viewHolder);
                if (z3 || z || !doesTransientStatePreventRecycling) {
                    return;
                }
                viewHolder.mOwnerRecyclerView = null;
            }
        }

        void scrapView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (!childViewHolderInt.hasAnyOfTheFlags(12) && childViewHolderInt.isUpdated() && !this.this$0.canReuseUpdatedViewHolder(childViewHolderInt)) {
                if (this.mChangedScrap == null) {
                    this.mChangedScrap = new ArrayList<>();
                }
                childViewHolderInt.setScrapContainer(this, true);
                this.mChangedScrap.add(childViewHolderInt);
            } else if (childViewHolderInt.isInvalid() && !childViewHolderInt.isRemoved() && !this.this$0.mAdapter.hasStableIds()) {
                throw new IllegalArgumentException("Called scrap view with an invalid view. Invalid views cannot be reused from scrap, they should rebound from recycler pool.");
            } else {
                childViewHolderInt.setScrapContainer(this, false);
                this.mAttachedScrap.add(childViewHolderInt);
            }
        }

        void setAdapterPositionsAsUnknown() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                ViewHolder viewHolder = this.mCachedViews.get(i);
                if (viewHolder != null) {
                    viewHolder.addFlags(512);
                }
            }
        }

        void unscrapView(ViewHolder viewHolder) {
            if (viewHolder.mInChangeScrap) {
                this.mChangedScrap.remove(viewHolder);
            } else {
                this.mAttachedScrap.remove(viewHolder);
            }
            viewHolder.mScrapContainer = null;
            viewHolder.mInChangeScrap = false;
            viewHolder.clearReturnedFromScrapFlag();
        }

        boolean validateViewHolderForOffsetPosition(ViewHolder viewHolder) {
            boolean z = true;
            if (viewHolder.isRemoved()) {
                return this.this$0.mState.isPreLayout();
            }
            if (viewHolder.mPosition < 0 || viewHolder.mPosition >= this.this$0.mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid view holder adapter position" + viewHolder);
            }
            if (this.this$0.mState.isPreLayout() || this.this$0.mAdapter.getItemViewType(viewHolder.mPosition) == viewHolder.getItemViewType()) {
                if (this.this$0.mAdapter.hasStableIds()) {
                    if (viewHolder.getItemId() != this.this$0.mAdapter.getItemId(viewHolder.mPosition)) {
                        z = false;
                    }
                    return z;
                }
                return true;
            }
            return false;
        }

        void viewRangeUpdate(int i, int i2) {
            int layoutPosition;
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = this.mCachedViews.get(size);
                if (viewHolder != null && (layoutPosition = viewHolder.getLayoutPosition()) >= i && layoutPosition < i + i2) {
                    viewHolder.addFlags(2);
                    recycleCachedViewAt(size);
                }
            }
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$RecyclerListener.class */
    public interface RecyclerListener {
        void onViewRecycled(ViewHolder viewHolder);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$RecyclerViewDataObserver.class */
    public class RecyclerViewDataObserver extends AdapterDataObserver {
        final RecyclerView this$0;

        private RecyclerViewDataObserver(RecyclerView recyclerView) {
            this.this$0 = recyclerView;
        }

        /* synthetic */ RecyclerViewDataObserver(RecyclerView recyclerView, RecyclerViewDataObserver recyclerViewDataObserver) {
            this(recyclerView);
        }

        @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
        public void onChanged() {
            this.this$0.assertNotInLayoutOrScroll(null);
            if (this.this$0.mAdapter.hasStableIds()) {
                this.this$0.mState.mStructureChanged = true;
                this.this$0.setDataSetChangedAfterLayout();
            } else {
                this.this$0.mState.mStructureChanged = true;
                this.this$0.setDataSetChangedAfterLayout();
            }
            if (this.this$0.mAdapterHelper.hasPendingUpdates()) {
                return;
            }
            this.this$0.requestLayout();
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$SavedState.class */
    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() { // from class: android.support.v7.widget.RecyclerView.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.support.v4.os.ParcelableCompatCreatorCallbacks
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        });
        Parcelable mLayoutState;

        SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            this.mLayoutState = parcel.readParcelable(classLoader == null ? LayoutManager.class.getClassLoader() : classLoader);
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void copyFrom(SavedState savedState) {
            this.mLayoutState = savedState.mLayoutState;
        }

        @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeParcelable(this.mLayoutState, 0);
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$SmoothScroller.class */
    public static abstract class SmoothScroller {
        private LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private RecyclerView mRecyclerView;
        private boolean mRunning;
        private View mTargetView;
        private int mTargetPosition = -1;
        private final Action mRecyclingAction = new Action(0, 0);

        /* loaded from: a.zip:android/support/v7/widget/RecyclerView$SmoothScroller$Action.class */
        public static class Action {
            private boolean changed;
            private int consecutiveUpdates;
            private int mDuration;
            private int mDx;
            private int mDy;
            private Interpolator mInterpolator;
            private int mJumpToPosition;

            public Action(int i, int i2) {
                this(i, i2, Integer.MIN_VALUE, null);
            }

            public Action(int i, int i2, int i3, Interpolator interpolator) {
                this.mJumpToPosition = -1;
                this.changed = false;
                this.consecutiveUpdates = 0;
                this.mDx = i;
                this.mDy = i2;
                this.mDuration = i3;
                this.mInterpolator = interpolator;
            }

            /* JADX INFO: Access modifiers changed from: private */
            public void runIfNecessary(RecyclerView recyclerView) {
                if (this.mJumpToPosition >= 0) {
                    int i = this.mJumpToPosition;
                    this.mJumpToPosition = -1;
                    recyclerView.jumpToPositionForSmoothScroller(i);
                    this.changed = false;
                } else if (!this.changed) {
                    this.consecutiveUpdates = 0;
                } else {
                    validate();
                    if (this.mInterpolator != null) {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration, this.mInterpolator);
                    } else if (this.mDuration == Integer.MIN_VALUE) {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy);
                    } else {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration);
                    }
                    this.consecutiveUpdates++;
                    if (this.consecutiveUpdates > 10) {
                        Log.e("RecyclerView", "Smooth Scroll action is being updated too frequently. Make sure you are not changing it unless necessary");
                    }
                    this.changed = false;
                }
            }

            private void validate() {
                if (this.mInterpolator != null && this.mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must set a positive duration");
                }
                if (this.mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }

            boolean hasJumpTarget() {
                boolean z = false;
                if (this.mJumpToPosition >= 0) {
                    z = true;
                }
                return z;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onAnimation(int i, int i2) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (!this.mRunning || this.mTargetPosition == -1 || recyclerView == null) {
                stop();
            }
            this.mPendingInitialRun = false;
            if (this.mTargetView != null) {
                if (getChildPosition(this.mTargetView) == this.mTargetPosition) {
                    onTargetFound(this.mTargetView, recyclerView.mState, this.mRecyclingAction);
                    this.mRecyclingAction.runIfNecessary(recyclerView);
                    stop();
                } else {
                    Log.e("RecyclerView", "Passed over target position while smooth scrolling.");
                    this.mTargetView = null;
                }
            }
            if (this.mRunning) {
                onSeekTargetStep(i, i2, recyclerView.mState, this.mRecyclingAction);
                boolean hasJumpTarget = this.mRecyclingAction.hasJumpTarget();
                this.mRecyclingAction.runIfNecessary(recyclerView);
                if (hasJumpTarget) {
                    if (!this.mRunning) {
                        stop();
                        return;
                    }
                    this.mPendingInitialRun = true;
                    recyclerView.mViewFlinger.postOnAnimation();
                }
            }
        }

        public int getChildPosition(View view) {
            return this.mRecyclerView.getChildLayoutPosition(view);
        }

        public int getTargetPosition() {
            return this.mTargetPosition;
        }

        public boolean isPendingInitialRun() {
            return this.mPendingInitialRun;
        }

        public boolean isRunning() {
            return this.mRunning;
        }

        protected void onChildAttachedToWindow(View view) {
            if (getChildPosition(view) == getTargetPosition()) {
                this.mTargetView = view;
            }
        }

        protected abstract void onSeekTargetStep(int i, int i2, State state, Action action);

        protected abstract void onStop();

        protected abstract void onTargetFound(View view, State state, Action action);

        public void setTargetPosition(int i) {
            this.mTargetPosition = i;
        }

        protected final void stop() {
            if (this.mRunning) {
                onStop();
                this.mRecyclerView.mState.mTargetPosition = -1;
                this.mTargetView = null;
                this.mTargetPosition = -1;
                this.mPendingInitialRun = false;
                this.mRunning = false;
                this.mLayoutManager.onSmoothScrollerStopped(this);
                this.mLayoutManager = null;
                this.mRecyclerView = null;
            }
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$State.class */
    public static class State {
        private SparseArray<Object> mData;
        long mFocusedItemId;
        int mFocusedItemPosition;
        int mFocusedSubChildId;
        private int mTargetPosition = -1;
        private int mLayoutStep = 1;
        int mItemCount = 0;
        private int mPreviousLayoutItemCount = 0;
        private int mDeletedInvisibleItemCountSincePreviousLayout = 0;
        private boolean mStructureChanged = false;
        private boolean mInPreLayout = false;
        private boolean mRunSimpleAnimations = false;
        private boolean mRunPredictiveAnimations = false;
        private boolean mTrackOldChangeHolders = false;
        private boolean mIsMeasuring = false;

        @Retention(RetentionPolicy.SOURCE)
        /* loaded from: a.zip:android/support/v7/widget/RecyclerView$State$LayoutState.class */
        @interface LayoutState {
        }

        void assertLayoutStep(int i) {
            if ((this.mLayoutStep & i) == 0) {
                throw new IllegalStateException("Layout state should be one of " + Integer.toBinaryString(i) + " but it is " + Integer.toBinaryString(this.mLayoutStep));
            }
        }

        public int getItemCount() {
            return this.mInPreLayout ? this.mPreviousLayoutItemCount - this.mDeletedInvisibleItemCountSincePreviousLayout : this.mItemCount;
        }

        public boolean hasTargetScrollPosition() {
            return this.mTargetPosition != -1;
        }

        public boolean isPreLayout() {
            return this.mInPreLayout;
        }

        public String toString() {
            return "State{mTargetPosition=" + this.mTargetPosition + ", mData=" + this.mData + ", mItemCount=" + this.mItemCount + ", mPreviousLayoutItemCount=" + this.mPreviousLayoutItemCount + ", mDeletedInvisibleItemCountSincePreviousLayout=" + this.mDeletedInvisibleItemCountSincePreviousLayout + ", mStructureChanged=" + this.mStructureChanged + ", mInPreLayout=" + this.mInPreLayout + ", mRunSimpleAnimations=" + this.mRunSimpleAnimations + ", mRunPredictiveAnimations=" + this.mRunPredictiveAnimations + '}';
        }

        public boolean willRunPredictiveAnimations() {
            return this.mRunPredictiveAnimations;
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ViewCacheExtension.class */
    public static abstract class ViewCacheExtension {
        public abstract View getViewForPositionAndType(Recycler recycler, int i, int i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ViewFlinger.class */
    public class ViewFlinger implements Runnable {
        private int mLastFlingX;
        private int mLastFlingY;
        private ScrollerCompat mScroller;
        final RecyclerView this$0;
        private Interpolator mInterpolator = RecyclerView.sQuinticInterpolator;
        private boolean mEatRunOnAnimationRequest = false;
        private boolean mReSchedulePostAnimationCallback = false;

        public ViewFlinger(RecyclerView recyclerView) {
            this.this$0 = recyclerView;
            this.mScroller = ScrollerCompat.create(recyclerView.getContext(), RecyclerView.sQuinticInterpolator);
        }

        private int computeScrollDuration(int i, int i2, int i3, int i4) {
            int abs;
            int abs2;
            int i5;
            boolean z = Math.abs(i) > Math.abs(i2);
            int sqrt = (int) Math.sqrt((i3 * i3) + (i4 * i4));
            int sqrt2 = (int) Math.sqrt((i * i) + (i2 * i2));
            int width = z ? this.this$0.getWidth() : this.this$0.getHeight();
            int i6 = width / 2;
            float f = i6;
            float f2 = i6;
            float distanceInfluenceForSnapDuration = distanceInfluenceForSnapDuration(Math.min(1.0f, (sqrt2 * 1.0f) / width));
            if (sqrt > 0) {
                i5 = Math.round(Math.abs((f + (f2 * distanceInfluenceForSnapDuration)) / sqrt) * 1000.0f) * 4;
            } else {
                i5 = (int) ((((z ? abs : abs2) / width) + 1.0f) * 300.0f);
            }
            return Math.min(i5, 2000);
        }

        private void disableRunOnAnimationRequests() {
            this.mReSchedulePostAnimationCallback = false;
            this.mEatRunOnAnimationRequest = true;
        }

        private float distanceInfluenceForSnapDuration(float f) {
            return (float) Math.sin((float) ((f - 0.5f) * 0.4712389167638204d));
        }

        private void enableRunOnAnimationRequests() {
            this.mEatRunOnAnimationRequest = false;
            if (this.mReSchedulePostAnimationCallback) {
                postOnAnimation();
            }
        }

        public void fling(int i, int i2) {
            this.this$0.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.fling(0, 0, i, i2, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }

        void postOnAnimation() {
            if (this.mEatRunOnAnimationRequest) {
                this.mReSchedulePostAnimationCallback = true;
                return;
            }
            this.this$0.removeCallbacks(this);
            ViewCompat.postOnAnimation(this.this$0, this);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mLayout == null) {
                stop();
                return;
            }
            disableRunOnAnimationRequests();
            this.this$0.consumePendingUpdateOperations();
            ScrollerCompat scrollerCompat = this.mScroller;
            SmoothScroller smoothScroller = this.this$0.mLayout.mSmoothScroller;
            if (scrollerCompat.computeScrollOffset()) {
                int currX = scrollerCompat.getCurrX();
                int currY = scrollerCompat.getCurrY();
                int i = currX - this.mLastFlingX;
                int i2 = currY - this.mLastFlingY;
                int i3 = 0;
                int i4 = 0;
                int i5 = 0;
                int i6 = 0;
                this.mLastFlingX = currX;
                this.mLastFlingY = currY;
                int i7 = 0;
                int i8 = 0;
                int i9 = 0;
                int i10 = 0;
                if (this.this$0.mAdapter != null) {
                    this.this$0.eatRequestLayout();
                    this.this$0.onEnterLayoutOrScroll();
                    TraceCompat.beginSection("RV Scroll");
                    if (i != 0) {
                        i4 = this.this$0.mLayout.scrollHorizontallyBy(i, this.this$0.mRecycler, this.this$0.mState);
                        i8 = i - i4;
                    }
                    if (i2 != 0) {
                        i6 = this.this$0.mLayout.scrollVerticallyBy(i2, this.this$0.mRecycler, this.this$0.mState);
                        i10 = i2 - i6;
                    }
                    TraceCompat.endSection();
                    this.this$0.repositionShadowingViews();
                    this.this$0.onExitLayoutOrScroll();
                    this.this$0.resumeRequestLayout(false);
                    i3 = i4;
                    i7 = i8;
                    i9 = i10;
                    i5 = i6;
                    if (smoothScroller != null) {
                        if (smoothScroller.isPendingInitialRun()) {
                            i5 = i6;
                            i9 = i10;
                            i7 = i8;
                            i3 = i4;
                        } else {
                            i3 = i4;
                            i7 = i8;
                            i9 = i10;
                            i5 = i6;
                            if (smoothScroller.isRunning()) {
                                int itemCount = this.this$0.mState.getItemCount();
                                if (itemCount == 0) {
                                    smoothScroller.stop();
                                    i3 = i4;
                                    i7 = i8;
                                    i9 = i10;
                                    i5 = i6;
                                } else if (smoothScroller.getTargetPosition() >= itemCount) {
                                    smoothScroller.setTargetPosition(itemCount - 1);
                                    smoothScroller.onAnimation(i - i8, i2 - i10);
                                    i3 = i4;
                                    i7 = i8;
                                    i9 = i10;
                                    i5 = i6;
                                } else {
                                    smoothScroller.onAnimation(i - i8, i2 - i10);
                                    i3 = i4;
                                    i7 = i8;
                                    i9 = i10;
                                    i5 = i6;
                                }
                            }
                        }
                    }
                }
                if (!this.this$0.mItemDecorations.isEmpty()) {
                    this.this$0.invalidate();
                }
                if (ViewCompat.getOverScrollMode(this.this$0) != 2) {
                    this.this$0.considerReleasingGlowsOnScroll(i, i2);
                }
                if (i7 != 0 || i9 != 0) {
                    int currVelocity = (int) scrollerCompat.getCurrVelocity();
                    int i11 = 0;
                    if (i7 != currX) {
                        i11 = i7 < 0 ? -currVelocity : i7 > 0 ? currVelocity : 0;
                    }
                    int i12 = 0;
                    if (i9 != currY) {
                        i12 = i9 < 0 ? -currVelocity : i9 > 0 ? currVelocity : 0;
                    }
                    if (ViewCompat.getOverScrollMode(this.this$0) != 2) {
                        this.this$0.absorbGlows(i11, i12);
                    }
                    if ((i11 != 0 || i7 == currX || scrollerCompat.getFinalX() == 0) && (i12 != 0 || i9 == currY || scrollerCompat.getFinalY() == 0)) {
                        scrollerCompat.abortAnimation();
                    }
                }
                if (i3 != 0 || i5 != 0) {
                    this.this$0.dispatchOnScrolled(i3, i5);
                }
                if (!this.this$0.awakenScrollBars()) {
                    this.this$0.invalidate();
                }
                boolean z = (i2 == 0 || !this.this$0.mLayout.canScrollVertically()) ? false : i5 == i2;
                boolean z2 = (i == 0 || !this.this$0.mLayout.canScrollHorizontally()) ? false : i3 == i;
                if ((i == 0 && i2 == 0) || z2) {
                    z = true;
                }
                if (scrollerCompat.isFinished() || !z) {
                    this.this$0.setScrollState(0);
                } else {
                    postOnAnimation();
                }
            }
            if (smoothScroller != null) {
                if (smoothScroller.isPendingInitialRun()) {
                    smoothScroller.onAnimation(0, 0);
                }
                if (!this.mReSchedulePostAnimationCallback) {
                    smoothScroller.stop();
                }
            }
            enableRunOnAnimationRequests();
        }

        public void smoothScrollBy(int i, int i2) {
            smoothScrollBy(i, i2, 0, 0);
        }

        public void smoothScrollBy(int i, int i2, int i3) {
            smoothScrollBy(i, i2, i3, RecyclerView.sQuinticInterpolator);
        }

        public void smoothScrollBy(int i, int i2, int i3, int i4) {
            smoothScrollBy(i, i2, computeScrollDuration(i, i2, i3, i4));
        }

        public void smoothScrollBy(int i, int i2, int i3, Interpolator interpolator) {
            if (this.mInterpolator != interpolator) {
                this.mInterpolator = interpolator;
                this.mScroller = ScrollerCompat.create(this.this$0.getContext(), interpolator);
            }
            this.this$0.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.startScroll(0, 0, i, i2, i3);
            postOnAnimation();
        }

        public void stop() {
            this.this$0.removeCallbacks(this);
            this.mScroller.abortAnimation();
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/RecyclerView$ViewHolder.class */
    public static abstract class ViewHolder {
        private static final List<Object> FULLUPDATE_PAYLOADS = Collections.EMPTY_LIST;
        public final View itemView;
        private int mFlags;
        RecyclerView mOwnerRecyclerView;
        int mPosition = -1;
        int mOldPosition = -1;
        long mItemId = -1;
        int mItemViewType = -1;
        int mPreLayoutPosition = -1;
        ViewHolder mShadowedHolder = null;
        ViewHolder mShadowingHolder = null;
        List<Object> mPayloads = null;
        List<Object> mUnmodifiedPayloads = null;
        private int mIsRecyclableCount = 0;
        private Recycler mScrapContainer = null;
        private boolean mInChangeScrap = false;
        private int mWasImportantForAccessibilityBeforeHidden = 0;

        public ViewHolder(View view) {
            if (view == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = view;
        }

        private void createPayloadsIfNeeded() {
            if (this.mPayloads == null) {
                this.mPayloads = new ArrayList();
                this.mUnmodifiedPayloads = Collections.unmodifiableList(this.mPayloads);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean doesTransientStatePreventRecycling() {
            boolean z = false;
            if ((this.mFlags & 16) == 0) {
                z = ViewCompat.hasTransientState(this.itemView);
            }
            return z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onEnteredHiddenState() {
            this.mWasImportantForAccessibilityBeforeHidden = ViewCompat.getImportantForAccessibility(this.itemView);
            ViewCompat.setImportantForAccessibility(this.itemView, 4);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onLeftHiddenState() {
            ViewCompat.setImportantForAccessibility(this.itemView, this.mWasImportantForAccessibilityBeforeHidden);
            this.mWasImportantForAccessibilityBeforeHidden = 0;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean shouldBeKeptAsChild() {
            boolean z = false;
            if ((this.mFlags & 16) != 0) {
                z = true;
            }
            return z;
        }

        void addChangePayload(Object obj) {
            if (obj == null) {
                addFlags(1024);
            } else if ((this.mFlags & 1024) == 0) {
                createPayloadsIfNeeded();
                this.mPayloads.add(obj);
            }
        }

        void addFlags(int i) {
            this.mFlags |= i;
        }

        void clearOldPosition() {
            this.mOldPosition = -1;
            this.mPreLayoutPosition = -1;
        }

        void clearPayload() {
            if (this.mPayloads != null) {
                this.mPayloads.clear();
            }
            this.mFlags &= -1025;
        }

        void clearReturnedFromScrapFlag() {
            this.mFlags &= -33;
        }

        void clearTmpDetachFlag() {
            this.mFlags &= -257;
        }

        void flagRemovedAndOffsetPosition(int i, int i2, boolean z) {
            addFlags(8);
            offsetPosition(i2, z);
            this.mPosition = i;
        }

        public final int getAdapterPosition() {
            if (this.mOwnerRecyclerView == null) {
                return -1;
            }
            return this.mOwnerRecyclerView.getAdapterPositionFor(this);
        }

        public final long getItemId() {
            return this.mItemId;
        }

        public final int getItemViewType() {
            return this.mItemViewType;
        }

        public final int getLayoutPosition() {
            return this.mPreLayoutPosition == -1 ? this.mPosition : this.mPreLayoutPosition;
        }

        public final int getOldPosition() {
            return this.mOldPosition;
        }

        @Deprecated
        public final int getPosition() {
            return this.mPreLayoutPosition == -1 ? this.mPosition : this.mPreLayoutPosition;
        }

        List<Object> getUnmodifiedPayloads() {
            return (this.mFlags & 1024) == 0 ? (this.mPayloads == null || this.mPayloads.size() == 0) ? FULLUPDATE_PAYLOADS : this.mUnmodifiedPayloads : FULLUPDATE_PAYLOADS;
        }

        boolean hasAnyOfTheFlags(int i) {
            boolean z = false;
            if ((this.mFlags & i) != 0) {
                z = true;
            }
            return z;
        }

        boolean isAdapterPositionUnknown() {
            return (this.mFlags & 512) == 0 ? isInvalid() : true;
        }

        boolean isBound() {
            boolean z = false;
            if ((this.mFlags & 1) != 0) {
                z = true;
            }
            return z;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean isInvalid() {
            boolean z = false;
            if ((this.mFlags & 4) != 0) {
                z = true;
            }
            return z;
        }

        public final boolean isRecyclable() {
            boolean z = false;
            if ((this.mFlags & 16) == 0) {
                z = !ViewCompat.hasTransientState(this.itemView);
            }
            return z;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean isRemoved() {
            boolean z = false;
            if ((this.mFlags & 8) != 0) {
                z = true;
            }
            return z;
        }

        boolean isScrap() {
            return this.mScrapContainer != null;
        }

        boolean isTmpDetached() {
            boolean z = false;
            if ((this.mFlags & 256) != 0) {
                z = true;
            }
            return z;
        }

        boolean isUpdated() {
            boolean z = false;
            if ((this.mFlags & 2) != 0) {
                z = true;
            }
            return z;
        }

        boolean needsUpdate() {
            boolean z = false;
            if ((this.mFlags & 2) != 0) {
                z = true;
            }
            return z;
        }

        void offsetPosition(int i, boolean z) {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
            if (this.mPreLayoutPosition == -1) {
                this.mPreLayoutPosition = this.mPosition;
            }
            if (z) {
                this.mPreLayoutPosition += i;
            }
            this.mPosition += i;
            if (this.itemView.getLayoutParams() != null) {
                ((LayoutParams) this.itemView.getLayoutParams()).mInsetsDirty = true;
            }
        }

        void resetInternal() {
            this.mFlags = 0;
            this.mPosition = -1;
            this.mOldPosition = -1;
            this.mItemId = -1L;
            this.mPreLayoutPosition = -1;
            this.mIsRecyclableCount = 0;
            this.mShadowedHolder = null;
            this.mShadowingHolder = null;
            clearPayload();
            this.mWasImportantForAccessibilityBeforeHidden = 0;
        }

        void saveOldPosition() {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
        }

        void setFlags(int i, int i2) {
            this.mFlags = (this.mFlags & (i2 ^ (-1))) | (i & i2);
        }

        public final void setIsRecyclable(boolean z) {
            this.mIsRecyclableCount = z ? this.mIsRecyclableCount - 1 : this.mIsRecyclableCount + 1;
            if (this.mIsRecyclableCount < 0) {
                this.mIsRecyclableCount = 0;
                Log.e("View", "isRecyclable decremented below 0: unmatched pair of setIsRecyable() calls for " + this);
            } else if (!z && this.mIsRecyclableCount == 1) {
                this.mFlags |= 16;
            } else if (z && this.mIsRecyclableCount == 0) {
                this.mFlags &= -17;
            }
        }

        void setScrapContainer(Recycler recycler, boolean z) {
            this.mScrapContainer = recycler;
            this.mInChangeScrap = z;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean shouldIgnore() {
            boolean z = false;
            if ((this.mFlags & 128) != 0) {
                z = true;
            }
            return z;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("ViewHolder{" + Integer.toHexString(hashCode()) + " position=" + this.mPosition + " id=" + this.mItemId + ", oldPos=" + this.mOldPosition + ", pLpos:" + this.mPreLayoutPosition);
            if (isScrap()) {
                sb.append(" scrap ").append(this.mInChangeScrap ? "[changeScrap]" : "[attachedScrap]");
            }
            if (isInvalid()) {
                sb.append(" invalid");
            }
            if (!isBound()) {
                sb.append(" unbound");
            }
            if (needsUpdate()) {
                sb.append(" update");
            }
            if (isRemoved()) {
                sb.append(" removed");
            }
            if (shouldIgnore()) {
                sb.append(" ignored");
            }
            if (isTmpDetached()) {
                sb.append(" tmpDetached");
            }
            if (!isRecyclable()) {
                sb.append(" not recyclable(").append(this.mIsRecyclableCount).append(")");
            }
            if (isAdapterPositionUnknown()) {
                sb.append(" undefined adapter position");
            }
            if (this.itemView.getParent() == null) {
                sb.append(" no parent");
            }
            sb.append("}");
            return sb.toString();
        }

        void unScrap() {
            this.mScrapContainer.unscrapView(this);
        }

        boolean wasReturnedFromScrap() {
            boolean z = false;
            if ((this.mFlags & 32) != 0) {
                z = true;
            }
            return z;
        }
    }

    static {
        FORCE_INVALIDATE_DISPLAY_LIST = (Build.VERSION.SDK_INT == 18 || Build.VERSION.SDK_INT == 19) ? true : Build.VERSION.SDK_INT == 20;
        ALLOW_SIZE_IN_UNSPECIFIED_SPEC = Build.VERSION.SDK_INT >= 23;
        LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE = new Class[]{Context.class, AttributeSet.class, Integer.TYPE, Integer.TYPE};
        sQuinticInterpolator = new Interpolator() { // from class: android.support.v7.widget.RecyclerView.3
            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                float f2 = f - 1.0f;
                return (f2 * f2 * f2 * f2 * f2) + 1.0f;
            }
        };
    }

    public RecyclerView(Context context) {
        this(context, null);
    }

    public RecyclerView(Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecyclerView(Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mObserver = new RecyclerViewDataObserver(this, null);
        this.mRecycler = new Recycler(this);
        this.mViewInfoStore = new ViewInfoStore();
        this.mUpdateChildViewsRunnable = new Runnable(this) { // from class: android.support.v7.widget.RecyclerView.1
            final RecyclerView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (!this.this$0.mFirstLayoutComplete || this.this$0.isLayoutRequested()) {
                    return;
                }
                if (!this.this$0.mIsAttached) {
                    this.this$0.requestLayout();
                } else if (this.this$0.mLayoutFrozen) {
                    this.this$0.mLayoutRequestEaten = true;
                } else {
                    this.this$0.consumePendingUpdateOperations();
                }
            }
        };
        this.mTempRect = new Rect();
        this.mTempRect2 = new Rect();
        this.mTempRectF = new RectF();
        this.mItemDecorations = new ArrayList<>();
        this.mOnItemTouchListeners = new ArrayList<>();
        this.mEatRequestLayout = 0;
        this.mDataSetHasChangedAfterLayout = false;
        this.mLayoutOrScrollCounter = 0;
        this.mItemAnimator = new DefaultItemAnimator();
        this.mScrollState = 0;
        this.mScrollPointerId = -1;
        this.mScrollFactor = Float.MIN_VALUE;
        this.mPreserveFocusAfterLayout = true;
        this.mViewFlinger = new ViewFlinger(this);
        this.mState = new State();
        this.mItemsAddedOrRemoved = false;
        this.mItemsChanged = false;
        this.mItemAnimatorListener = new ItemAnimatorRestoreListener(this, null);
        this.mPostedAnimatorRunner = false;
        this.mMinMaxLayoutPositions = new int[2];
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mNestedOffsets = new int[2];
        this.mItemAnimatorRunner = new Runnable(this) { // from class: android.support.v7.widget.RecyclerView.2
            final RecyclerView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mItemAnimator != null) {
                    this.this$0.mItemAnimator.runPendingAnimations();
                }
                this.this$0.mPostedAnimatorRunner = false;
            }
        };
        this.mViewInfoProcessCallback = new ViewInfoStore.ProcessCallback(this) { // from class: android.support.v7.widget.RecyclerView.4
            final RecyclerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v7.widget.ViewInfoStore.ProcessCallback
            public void processAppeared(ViewHolder viewHolder, ItemAnimator.ItemHolderInfo itemHolderInfo, ItemAnimator.ItemHolderInfo itemHolderInfo2) {
                this.this$0.animateAppearance(viewHolder, itemHolderInfo, itemHolderInfo2);
            }

            @Override // android.support.v7.widget.ViewInfoStore.ProcessCallback
            public void processDisappeared(ViewHolder viewHolder, @NonNull ItemAnimator.ItemHolderInfo itemHolderInfo, @Nullable ItemAnimator.ItemHolderInfo itemHolderInfo2) {
                this.this$0.mRecycler.unscrapView(viewHolder);
                this.this$0.animateDisappearance(viewHolder, itemHolderInfo, itemHolderInfo2);
            }

            @Override // android.support.v7.widget.ViewInfoStore.ProcessCallback
            public void processPersistent(ViewHolder viewHolder, @NonNull ItemAnimator.ItemHolderInfo itemHolderInfo, @NonNull ItemAnimator.ItemHolderInfo itemHolderInfo2) {
                viewHolder.setIsRecyclable(false);
                if (this.this$0.mDataSetHasChangedAfterLayout) {
                    if (this.this$0.mItemAnimator.animateChange(viewHolder, viewHolder, itemHolderInfo, itemHolderInfo2)) {
                        this.this$0.postAnimationRunner();
                    }
                } else if (this.this$0.mItemAnimator.animatePersistence(viewHolder, itemHolderInfo, itemHolderInfo2)) {
                    this.this$0.postAnimationRunner();
                }
            }

            @Override // android.support.v7.widget.ViewInfoStore.ProcessCallback
            public void unused(ViewHolder viewHolder) {
                this.this$0.mLayout.removeAndRecycleView(viewHolder.itemView, this.this$0.mRecycler);
            }
        };
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, CLIP_TO_PADDING_ATTR, i, 0);
            this.mClipToPadding = obtainStyledAttributes.getBoolean(0, true);
            obtainStyledAttributes.recycle();
        } else {
            this.mClipToPadding = true;
        }
        setScrollContainer(true);
        setFocusableInTouchMode(true);
        this.mPostUpdatesOnAnimation = Build.VERSION.SDK_INT >= 16;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        setWillNotDraw(ViewCompat.getOverScrollMode(this) == 2);
        this.mItemAnimator.setListener(this.mItemAnimatorListener);
        initAdapterManager();
        initChildrenHelper();
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(this));
        boolean z = true;
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R$styleable.RecyclerView, i, 0);
            String string = obtainStyledAttributes2.getString(R$styleable.RecyclerView_layoutManager);
            if (obtainStyledAttributes2.getInt(R$styleable.RecyclerView_android_descendantFocusability, -1) == -1) {
                setDescendantFocusability(262144);
            }
            obtainStyledAttributes2.recycle();
            createLayoutManager(context, string, attributeSet, i, 0);
            if (Build.VERSION.SDK_INT >= 21) {
                TypedArray obtainStyledAttributes3 = context.obtainStyledAttributes(attributeSet, NESTED_SCROLLING_ATTRS, i, 0);
                z = obtainStyledAttributes3.getBoolean(0, true);
                obtainStyledAttributes3.recycle();
            }
        } else {
            setDescendantFocusability(262144);
        }
        setNestedScrollingEnabled(z);
    }

    private void addAnimatingView(ViewHolder viewHolder) {
        View view = viewHolder.itemView;
        boolean z = view.getParent() == this;
        this.mRecycler.unscrapView(getChildViewHolder(view));
        if (viewHolder.isTmpDetached()) {
            this.mChildHelper.attachViewToParent(view, -1, view.getLayoutParams(), true);
        } else if (z) {
            this.mChildHelper.hide(view);
        } else {
            this.mChildHelper.addView(view, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateAppearance(@NonNull ViewHolder viewHolder, @Nullable ItemAnimator.ItemHolderInfo itemHolderInfo, @NonNull ItemAnimator.ItemHolderInfo itemHolderInfo2) {
        viewHolder.setIsRecyclable(false);
        if (this.mItemAnimator.animateAppearance(viewHolder, itemHolderInfo, itemHolderInfo2)) {
            postAnimationRunner();
        }
    }

    private void animateChange(@NonNull ViewHolder viewHolder, @NonNull ViewHolder viewHolder2, @NonNull ItemAnimator.ItemHolderInfo itemHolderInfo, @NonNull ItemAnimator.ItemHolderInfo itemHolderInfo2, boolean z, boolean z2) {
        viewHolder.setIsRecyclable(false);
        if (z) {
            addAnimatingView(viewHolder);
        }
        if (viewHolder != viewHolder2) {
            if (z2) {
                addAnimatingView(viewHolder2);
            }
            viewHolder.mShadowedHolder = viewHolder2;
            addAnimatingView(viewHolder);
            this.mRecycler.unscrapView(viewHolder);
            viewHolder2.setIsRecyclable(false);
            viewHolder2.mShadowingHolder = viewHolder;
        }
        if (this.mItemAnimator.animateChange(viewHolder, viewHolder2, itemHolderInfo, itemHolderInfo2)) {
            postAnimationRunner();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateDisappearance(@NonNull ViewHolder viewHolder, @NonNull ItemAnimator.ItemHolderInfo itemHolderInfo, @Nullable ItemAnimator.ItemHolderInfo itemHolderInfo2) {
        addAnimatingView(viewHolder);
        viewHolder.setIsRecyclable(false);
        if (this.mItemAnimator.animateDisappearance(viewHolder, itemHolderInfo, itemHolderInfo2)) {
            postAnimationRunner();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean canReuseUpdatedViewHolder(ViewHolder viewHolder) {
        return this.mItemAnimator != null ? this.mItemAnimator.canReuseUpdatedViewHolder(viewHolder, viewHolder.getUnmodifiedPayloads()) : true;
    }

    private void cancelTouch() {
        resetTouch();
        setScrollState(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void considerReleasingGlowsOnScroll(int i, int i2) {
        boolean z = false;
        if (this.mLeftGlow != null) {
            if (this.mLeftGlow.isFinished()) {
                z = false;
            } else {
                z = false;
                if (i > 0) {
                    z = this.mLeftGlow.onRelease();
                }
            }
        }
        boolean z2 = z;
        if (this.mRightGlow != null) {
            if (this.mRightGlow.isFinished()) {
                z2 = z;
            } else {
                z2 = z;
                if (i < 0) {
                    z2 = z | this.mRightGlow.onRelease();
                }
            }
        }
        boolean z3 = z2;
        if (this.mTopGlow != null) {
            if (this.mTopGlow.isFinished()) {
                z3 = z2;
            } else {
                z3 = z2;
                if (i2 > 0) {
                    z3 = z2 | this.mTopGlow.onRelease();
                }
            }
        }
        boolean z4 = z3;
        if (this.mBottomGlow != null) {
            if (this.mBottomGlow.isFinished()) {
                z4 = z3;
            } else {
                z4 = z3;
                if (i2 < 0) {
                    z4 = z3 | this.mBottomGlow.onRelease();
                }
            }
        }
        if (z4) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void consumePendingUpdateOperations() {
        if (!this.mFirstLayoutComplete || this.mDataSetHasChangedAfterLayout) {
            TraceCompat.beginSection("RV FullInvalidate");
            dispatchLayout();
            TraceCompat.endSection();
        } else if (this.mAdapterHelper.hasPendingUpdates()) {
            if (!this.mAdapterHelper.hasAnyUpdateTypes(4) || this.mAdapterHelper.hasAnyUpdateTypes(11)) {
                if (this.mAdapterHelper.hasPendingUpdates()) {
                    TraceCompat.beginSection("RV FullInvalidate");
                    dispatchLayout();
                    TraceCompat.endSection();
                    return;
                }
                return;
            }
            TraceCompat.beginSection("RV PartialInvalidate");
            eatRequestLayout();
            this.mAdapterHelper.preProcess();
            if (!this.mLayoutRequestEaten) {
                if (hasUpdatedView()) {
                    dispatchLayout();
                } else {
                    this.mAdapterHelper.consumePostponedUpdates();
                }
            }
            resumeRequestLayout(true);
            TraceCompat.endSection();
        }
    }

    private void createLayoutManager(Context context, String str, AttributeSet attributeSet, int i, int i2) {
        Constructor constructor;
        Object[] objArr;
        if (str != null) {
            String trim = str.trim();
            if (trim.length() != 0) {
                String fullClassName = getFullClassName(context, trim);
                try {
                    Class<? extends U> asSubclass = (isInEditMode() ? getClass().getClassLoader() : context.getClassLoader()).loadClass(fullClassName).asSubclass(LayoutManager.class);
                    try {
                        constructor = asSubclass.getConstructor(LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE);
                        objArr = new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)};
                    } catch (NoSuchMethodException e) {
                        try {
                            constructor = asSubclass.getConstructor(new Class[0]);
                            objArr = null;
                        } catch (NoSuchMethodException e2) {
                            e2.initCause(e);
                            throw new IllegalStateException(attributeSet.getPositionDescription() + ": Error creating LayoutManager " + fullClassName, e2);
                        }
                    }
                    constructor.setAccessible(true);
                    setLayoutManager((LayoutManager) constructor.newInstance(objArr));
                } catch (ClassCastException e3) {
                    throw new IllegalStateException(attributeSet.getPositionDescription() + ": Class is not a LayoutManager " + fullClassName, e3);
                } catch (ClassNotFoundException e4) {
                    throw new IllegalStateException(attributeSet.getPositionDescription() + ": Unable to find LayoutManager " + fullClassName, e4);
                } catch (IllegalAccessException e5) {
                    throw new IllegalStateException(attributeSet.getPositionDescription() + ": Cannot access non-public constructor " + fullClassName, e5);
                } catch (InstantiationException e6) {
                    throw new IllegalStateException(attributeSet.getPositionDescription() + ": Could not instantiate the LayoutManager: " + fullClassName, e6);
                } catch (InvocationTargetException e7) {
                    throw new IllegalStateException(attributeSet.getPositionDescription() + ": Could not instantiate the LayoutManager: " + fullClassName, e7);
                }
            }
        }
    }

    private boolean didChildRangeChange(int i, int i2) {
        if (this.mChildHelper.getChildCount() == 0) {
            boolean z = true;
            if (i == 0) {
                z = i2 != 0;
            }
            return z;
        }
        findMinMaxChildLayoutPositions(this.mMinMaxLayoutPositions);
        boolean z2 = true;
        if (this.mMinMaxLayoutPositions[0] == i) {
            z2 = this.mMinMaxLayoutPositions[1] != i2;
        }
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChildAttached(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        onChildAttachedToWindow(view);
        if (this.mAdapter != null && childViewHolderInt != null) {
            this.mAdapter.onViewAttachedToWindow(childViewHolderInt);
        }
        if (this.mOnChildAttachStateListeners != null) {
            for (int size = this.mOnChildAttachStateListeners.size() - 1; size >= 0; size--) {
                this.mOnChildAttachStateListeners.get(size).onChildViewAttachedToWindow(view);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChildDetached(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        onChildDetachedFromWindow(view);
        if (this.mAdapter != null && childViewHolderInt != null) {
            this.mAdapter.onViewDetachedFromWindow(childViewHolderInt);
        }
        if (this.mOnChildAttachStateListeners != null) {
            for (int size = this.mOnChildAttachStateListeners.size() - 1; size >= 0; size--) {
                this.mOnChildAttachStateListeners.get(size).onChildViewDetachedFromWindow(view);
            }
        }
    }

    private void dispatchContentChangedIfNecessary() {
        int i = this.mEatenAccessibilityChangeFlags;
        this.mEatenAccessibilityChangeFlags = 0;
        if (i == 0 || !isAccessibilityEnabled()) {
            return;
        }
        AccessibilityEvent obtain = AccessibilityEvent.obtain();
        obtain.setEventType(2048);
        AccessibilityEventCompat.setContentChangeTypes(obtain, i);
        sendAccessibilityEventUnchecked(obtain);
    }

    private void dispatchLayoutStep1() {
        this.mState.assertLayoutStep(1);
        this.mState.mIsMeasuring = false;
        eatRequestLayout();
        this.mViewInfoStore.clear();
        onEnterLayoutOrScroll();
        saveFocusInfo();
        processAdapterUpdatesAndSetAnimationFlags();
        this.mState.mTrackOldChangeHolders = this.mState.mRunSimpleAnimations ? this.mItemsChanged : false;
        this.mItemsChanged = false;
        this.mItemsAddedOrRemoved = false;
        this.mState.mInPreLayout = this.mState.mRunPredictiveAnimations;
        this.mState.mItemCount = this.mAdapter.getItemCount();
        findMinMaxChildLayoutPositions(this.mMinMaxLayoutPositions);
        if (this.mState.mRunSimpleAnimations) {
            int childCount = this.mChildHelper.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                if (!childViewHolderInt.shouldIgnore() && (!childViewHolderInt.isInvalid() || this.mAdapter.hasStableIds())) {
                    this.mViewInfoStore.addToPreLayout(childViewHolderInt, this.mItemAnimator.recordPreLayoutInformation(this.mState, childViewHolderInt, ItemAnimator.buildAdapterChangeFlagsForAnimations(childViewHolderInt), childViewHolderInt.getUnmodifiedPayloads()));
                    if (this.mState.mTrackOldChangeHolders && childViewHolderInt.isUpdated() && !childViewHolderInt.isRemoved() && !childViewHolderInt.shouldIgnore() && !childViewHolderInt.isInvalid()) {
                        this.mViewInfoStore.addToOldChangeHolders(getChangedHolderKey(childViewHolderInt), childViewHolderInt);
                    }
                }
            }
        }
        if (this.mState.mRunPredictiveAnimations) {
            saveOldPositions();
            boolean z = this.mState.mStructureChanged;
            this.mState.mStructureChanged = false;
            this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
            this.mState.mStructureChanged = z;
            for (int i2 = 0; i2 < this.mChildHelper.getChildCount(); i2++) {
                ViewHolder childViewHolderInt2 = getChildViewHolderInt(this.mChildHelper.getChildAt(i2));
                if (!childViewHolderInt2.shouldIgnore() && !this.mViewInfoStore.isInPreLayout(childViewHolderInt2)) {
                    int buildAdapterChangeFlagsForAnimations = ItemAnimator.buildAdapterChangeFlagsForAnimations(childViewHolderInt2);
                    boolean hasAnyOfTheFlags = childViewHolderInt2.hasAnyOfTheFlags(8192);
                    int i3 = buildAdapterChangeFlagsForAnimations;
                    if (!hasAnyOfTheFlags) {
                        i3 = buildAdapterChangeFlagsForAnimations | 4096;
                    }
                    ItemAnimator.ItemHolderInfo recordPreLayoutInformation = this.mItemAnimator.recordPreLayoutInformation(this.mState, childViewHolderInt2, i3, childViewHolderInt2.getUnmodifiedPayloads());
                    if (hasAnyOfTheFlags) {
                        recordAnimationInfoIfBouncedHiddenView(childViewHolderInt2, recordPreLayoutInformation);
                    } else {
                        this.mViewInfoStore.addToAppearedInPreLayoutHolders(childViewHolderInt2, recordPreLayoutInformation);
                    }
                }
            }
            clearOldPositions();
        } else {
            clearOldPositions();
        }
        onExitLayoutOrScroll();
        resumeRequestLayout(false);
        this.mState.mLayoutStep = 2;
    }

    private void dispatchLayoutStep2() {
        eatRequestLayout();
        onEnterLayoutOrScroll();
        this.mState.assertLayoutStep(6);
        this.mAdapterHelper.consumeUpdatesInOnePass();
        this.mState.mItemCount = this.mAdapter.getItemCount();
        this.mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
        this.mState.mInPreLayout = false;
        this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
        this.mState.mStructureChanged = false;
        this.mPendingSavedState = null;
        this.mState.mRunSimpleAnimations = this.mState.mRunSimpleAnimations && this.mItemAnimator != null;
        this.mState.mLayoutStep = 4;
        onExitLayoutOrScroll();
        resumeRequestLayout(false);
    }

    private void dispatchLayoutStep3() {
        this.mState.assertLayoutStep(4);
        eatRequestLayout();
        onEnterLayoutOrScroll();
        this.mState.mLayoutStep = 1;
        if (this.mState.mRunSimpleAnimations) {
            for (int childCount = this.mChildHelper.getChildCount() - 1; childCount >= 0; childCount--) {
                ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(childCount));
                if (!childViewHolderInt.shouldIgnore()) {
                    long changedHolderKey = getChangedHolderKey(childViewHolderInt);
                    ItemAnimator.ItemHolderInfo recordPostLayoutInformation = this.mItemAnimator.recordPostLayoutInformation(this.mState, childViewHolderInt);
                    ViewHolder fromOldChangeHolders = this.mViewInfoStore.getFromOldChangeHolders(changedHolderKey);
                    if (fromOldChangeHolders == null || fromOldChangeHolders.shouldIgnore()) {
                        this.mViewInfoStore.addToPostLayout(childViewHolderInt, recordPostLayoutInformation);
                    } else {
                        boolean isDisappearing = this.mViewInfoStore.isDisappearing(fromOldChangeHolders);
                        boolean isDisappearing2 = this.mViewInfoStore.isDisappearing(childViewHolderInt);
                        if (isDisappearing && fromOldChangeHolders == childViewHolderInt) {
                            this.mViewInfoStore.addToPostLayout(childViewHolderInt, recordPostLayoutInformation);
                        } else {
                            ItemAnimator.ItemHolderInfo popFromPreLayout = this.mViewInfoStore.popFromPreLayout(fromOldChangeHolders);
                            this.mViewInfoStore.addToPostLayout(childViewHolderInt, recordPostLayoutInformation);
                            ItemAnimator.ItemHolderInfo popFromPostLayout = this.mViewInfoStore.popFromPostLayout(childViewHolderInt);
                            if (popFromPreLayout == null) {
                                handleMissingPreInfoForChangeError(changedHolderKey, childViewHolderInt, fromOldChangeHolders);
                            } else {
                                animateChange(fromOldChangeHolders, childViewHolderInt, popFromPreLayout, popFromPostLayout, isDisappearing, isDisappearing2);
                            }
                        }
                    }
                }
            }
            this.mViewInfoStore.process(this.mViewInfoProcessCallback);
        }
        this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
        this.mState.mPreviousLayoutItemCount = this.mState.mItemCount;
        this.mDataSetHasChangedAfterLayout = false;
        this.mState.mRunSimpleAnimations = false;
        this.mState.mRunPredictiveAnimations = false;
        this.mLayout.mRequestedSimpleAnimations = false;
        if (this.mRecycler.mChangedScrap != null) {
            this.mRecycler.mChangedScrap.clear();
        }
        this.mLayout.onLayoutCompleted(this.mState);
        onExitLayoutOrScroll();
        resumeRequestLayout(false);
        this.mViewInfoStore.clear();
        if (didChildRangeChange(this.mMinMaxLayoutPositions[0], this.mMinMaxLayoutPositions[1])) {
            dispatchOnScrolled(0, 0);
        }
        recoverFocusFromState();
        resetFocusInfo();
    }

    private boolean dispatchOnItemTouch(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (this.mActiveOnItemTouchListener != null) {
            if (action != 0) {
                this.mActiveOnItemTouchListener.onTouchEvent(this, motionEvent);
                if (action == 3 || action == 1) {
                    this.mActiveOnItemTouchListener = null;
                    return true;
                }
                return true;
            }
            this.mActiveOnItemTouchListener = null;
        }
        if (action != 0) {
            int size = this.mOnItemTouchListeners.size();
            for (int i = 0; i < size; i++) {
                OnItemTouchListener onItemTouchListener = this.mOnItemTouchListeners.get(i);
                if (onItemTouchListener.onInterceptTouchEvent(this, motionEvent)) {
                    this.mActiveOnItemTouchListener = onItemTouchListener;
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean dispatchOnItemTouchIntercept(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 3 || action == 0) {
            this.mActiveOnItemTouchListener = null;
        }
        int size = this.mOnItemTouchListeners.size();
        for (int i = 0; i < size; i++) {
            OnItemTouchListener onItemTouchListener = this.mOnItemTouchListeners.get(i);
            if (onItemTouchListener.onInterceptTouchEvent(this, motionEvent) && action != 3) {
                this.mActiveOnItemTouchListener = onItemTouchListener;
                return true;
            }
        }
        return false;
    }

    private void findMinMaxChildLayoutPositions(int[] iArr) {
        int i;
        int childCount = this.mChildHelper.getChildCount();
        if (childCount == 0) {
            iArr[0] = 0;
            iArr[1] = 0;
            return;
        }
        int i2 = Integer.MAX_VALUE;
        int i3 = Integer.MIN_VALUE;
        int i4 = 0;
        while (i4 < childCount) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i4));
            if (childViewHolderInt.shouldIgnore()) {
                i = i3;
            } else {
                int layoutPosition = childViewHolderInt.getLayoutPosition();
                int i5 = i2;
                if (layoutPosition < i2) {
                    i5 = layoutPosition;
                }
                i = i3;
                i2 = i5;
                if (layoutPosition > i3) {
                    i = layoutPosition;
                    i2 = i5;
                }
            }
            i4++;
            i3 = i;
        }
        iArr[0] = i2;
        iArr[1] = i3;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getAdapterPositionFor(ViewHolder viewHolder) {
        if (viewHolder.hasAnyOfTheFlags(524) || !viewHolder.isBound()) {
            return -1;
        }
        return this.mAdapterHelper.applyPendingUpdatesToPosition(viewHolder.mPosition);
    }

    static ViewHolder getChildViewHolderInt(View view) {
        if (view == null) {
            return null;
        }
        return ((LayoutParams) view.getLayoutParams()).mViewHolder;
    }

    private int getDeepestFocusedViewWithId(View view) {
        int id = view.getId();
        while (!view.isFocused() && (view instanceof ViewGroup) && view.hasFocus()) {
            View focusedChild = ((ViewGroup) view).getFocusedChild();
            view = focusedChild;
            if (focusedChild.getId() != -1) {
                id = focusedChild.getId();
                view = focusedChild;
            }
        }
        return id;
    }

    private String getFullClassName(Context context, String str) {
        return str.charAt(0) == '.' ? context.getPackageName() + str : str.contains(".") ? str : RecyclerView.class.getPackage().getName() + '.' + str;
    }

    private float getScrollFactor() {
        if (this.mScrollFactor == Float.MIN_VALUE) {
            TypedValue typedValue = new TypedValue();
            if (!getContext().getTheme().resolveAttribute(16842829, typedValue, true)) {
                return 0.0f;
            }
            this.mScrollFactor = typedValue.getDimension(getContext().getResources().getDisplayMetrics());
        }
        return this.mScrollFactor;
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (this.mScrollingChildHelper == null) {
            this.mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return this.mScrollingChildHelper;
    }

    private void handleMissingPreInfoForChangeError(long j, ViewHolder viewHolder, ViewHolder viewHolder2) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
            if (childViewHolderInt != viewHolder && getChangedHolderKey(childViewHolderInt) == j) {
                if (this.mAdapter != null && this.mAdapter.hasStableIds()) {
                    throw new IllegalStateException("Two different ViewHolders have the same stable ID. Stable IDs in your adapter MUST BE unique and SHOULD NOT change.\n ViewHolder 1:" + childViewHolderInt + " \n View Holder 2:" + viewHolder);
                }
                throw new IllegalStateException("Two different ViewHolders have the same change ID. This might happen due to inconsistent Adapter update events or if the LayoutManager lays out the same View multiple times.\n ViewHolder 1:" + childViewHolderInt + " \n View Holder 2:" + viewHolder);
            }
        }
        Log.e("RecyclerView", "Problem while matching changed view holders with the newones. The pre-layout information for the change holder " + viewHolder2 + " cannot be found but it is necessary for " + viewHolder);
    }

    private boolean hasUpdatedView() {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.isUpdated()) {
                return true;
            }
        }
        return false;
    }

    private void initChildrenHelper() {
        this.mChildHelper = new ChildHelper(new ChildHelper.Callback(this) { // from class: android.support.v7.widget.RecyclerView.5
            final RecyclerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public void addView(View view, int i) {
                this.this$0.addView(view, i);
                this.this$0.dispatchChildAttached(view);
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public void attachViewToParent(View view, int i, ViewGroup.LayoutParams layoutParams) {
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
                if (childViewHolderInt != null) {
                    if (!childViewHolderInt.isTmpDetached() && !childViewHolderInt.shouldIgnore()) {
                        throw new IllegalArgumentException("Called attach on a child which is not detached: " + childViewHolderInt);
                    }
                    childViewHolderInt.clearTmpDetachFlag();
                }
                this.this$0.attachViewToParent(view, i, layoutParams);
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public void detachViewFromParent(int i) {
                ViewHolder childViewHolderInt;
                View childAt = getChildAt(i);
                if (childAt != null && (childViewHolderInt = RecyclerView.getChildViewHolderInt(childAt)) != null) {
                    if (childViewHolderInt.isTmpDetached() && !childViewHolderInt.shouldIgnore()) {
                        throw new IllegalArgumentException("called detach on an already detached child " + childViewHolderInt);
                    }
                    childViewHolderInt.addFlags(256);
                }
                this.this$0.detachViewFromParent(i);
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public View getChildAt(int i) {
                return this.this$0.getChildAt(i);
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public int getChildCount() {
                return this.this$0.getChildCount();
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public ViewHolder getChildViewHolder(View view) {
                return RecyclerView.getChildViewHolderInt(view);
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public int indexOfChild(View view) {
                return this.this$0.indexOfChild(view);
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public void onEnteredHiddenState(View view) {
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
                if (childViewHolderInt != null) {
                    childViewHolderInt.onEnteredHiddenState();
                }
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public void onLeftHiddenState(View view) {
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
                if (childViewHolderInt != null) {
                    childViewHolderInt.onLeftHiddenState();
                }
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public void removeAllViews() {
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    this.this$0.dispatchChildDetached(getChildAt(i));
                }
                this.this$0.removeAllViews();
            }

            @Override // android.support.v7.widget.ChildHelper.Callback
            public void removeViewAt(int i) {
                View childAt = this.this$0.getChildAt(i);
                if (childAt != null) {
                    this.this$0.dispatchChildDetached(childAt);
                }
                this.this$0.removeViewAt(i);
            }
        });
    }

    private boolean isPreferredNextFocus(View view, View view2, int i) {
        boolean z = false;
        if (view2 == null || view2 == this) {
            return false;
        }
        if (view == null) {
            return true;
        }
        if (i == 2 || i == 1) {
            boolean z2 = this.mLayout.getLayoutDirection() == 1;
            if (i == 2) {
                z = true;
            }
            if (isPreferredNextFocusAbsolute(view, view2, z ^ z2 ? 66 : 17)) {
                return true;
            }
            return i == 2 ? isPreferredNextFocusAbsolute(view, view2, 130) : isPreferredNextFocusAbsolute(view, view2, 33);
        }
        return isPreferredNextFocusAbsolute(view, view2, i);
    }

    /* JADX WARN: Code restructure failed: missing block: B:19:0x00f7, code lost:
        if (r6.mTempRect.right <= r6.mTempRect2.left) goto L22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x013b, code lost:
        if (r6.mTempRect.top >= r6.mTempRect2.bottom) goto L32;
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:0x017f, code lost:
        if (r6.mTempRect.bottom <= r6.mTempRect2.top) goto L42;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean isPreferredNextFocusAbsolute(View view, View view2, int i) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4 = false;
        this.mTempRect.set(0, 0, view.getWidth(), view.getHeight());
        this.mTempRect2.set(0, 0, view2.getWidth(), view2.getHeight());
        offsetDescendantRectToMyCoords(view, this.mTempRect);
        offsetDescendantRectToMyCoords(view2, this.mTempRect2);
        switch (i) {
            case 17:
                if (this.mTempRect.right > this.mTempRect2.right || this.mTempRect.left >= this.mTempRect2.right) {
                    z4 = this.mTempRect.left > this.mTempRect2.left;
                }
                return z4;
            case 33:
                if (this.mTempRect.bottom <= this.mTempRect2.bottom) {
                    z2 = false;
                    break;
                }
                z2 = this.mTempRect.top > this.mTempRect2.top;
                return z2;
            case 66:
                if (this.mTempRect.left >= this.mTempRect2.left) {
                    z3 = false;
                    break;
                }
                z3 = this.mTempRect.right < this.mTempRect2.right;
                return z3;
            case 130:
                if (this.mTempRect.top >= this.mTempRect2.top) {
                    z = false;
                    break;
                }
                z = this.mTempRect.bottom < this.mTempRect2.bottom;
                return z;
            default:
                throw new IllegalArgumentException("direction must be absolute. received:" + i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void jumpToPositionForSmoothScroller(int i) {
        if (this.mLayout == null) {
            return;
        }
        this.mLayout.scrollToPosition(i);
        awakenScrollBars();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEnterLayoutOrScroll() {
        this.mLayoutOrScrollCounter++;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onExitLayoutOrScroll() {
        this.mLayoutOrScrollCounter--;
        if (this.mLayoutOrScrollCounter < 1) {
            this.mLayoutOrScrollCounter = 0;
            dispatchContentChangedIfNecessary();
        }
    }

    private void onPointerUp(MotionEvent motionEvent) {
        int i = 0;
        int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
        if (MotionEventCompat.getPointerId(motionEvent, actionIndex) == this.mScrollPointerId) {
            if (actionIndex == 0) {
                i = 1;
            }
            this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, i);
            int x = (int) (MotionEventCompat.getX(motionEvent, i) + 0.5f);
            this.mLastTouchX = x;
            this.mInitialTouchX = x;
            int y = (int) (MotionEventCompat.getY(motionEvent, i) + 0.5f);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void postAnimationRunner() {
        if (this.mPostedAnimatorRunner || !this.mIsAttached) {
            return;
        }
        ViewCompat.postOnAnimation(this, this.mItemAnimatorRunner);
        this.mPostedAnimatorRunner = true;
    }

    private boolean predictiveItemAnimationsEnabled() {
        return this.mItemAnimator != null ? this.mLayout.supportsPredictiveItemAnimations() : false;
    }

    private void processAdapterUpdatesAndSetAnimationFlags() {
        if (this.mDataSetHasChangedAfterLayout) {
            this.mAdapterHelper.reset();
            markKnownViewsInvalid();
            this.mLayout.onItemsChanged(this);
        }
        if (predictiveItemAnimationsEnabled()) {
            this.mAdapterHelper.preProcess();
        } else {
            this.mAdapterHelper.consumeUpdatesInOnePass();
        }
        boolean z = !this.mItemsAddedOrRemoved ? this.mItemsChanged : true;
        this.mState.mRunSimpleAnimations = (this.mFirstLayoutComplete && this.mItemAnimator != null && (this.mDataSetHasChangedAfterLayout || z || this.mLayout.mRequestedSimpleAnimations)) ? this.mDataSetHasChangedAfterLayout ? this.mAdapter.hasStableIds() : true : false;
        State state = this.mState;
        boolean z2 = false;
        if (this.mState.mRunSimpleAnimations) {
            z2 = false;
            if (z) {
                z2 = this.mDataSetHasChangedAfterLayout ? false : predictiveItemAnimationsEnabled();
            }
        }
        state.mRunPredictiveAnimations = z2;
    }

    private void pullGlows(float f, float f2, float f3, float f4) {
        boolean z;
        boolean z2;
        if (f2 < 0.0f) {
            ensureLeftGlow();
            z = false;
            if (this.mLeftGlow.onPull((-f2) / getWidth(), 1.0f - (f3 / getHeight()))) {
                z = true;
            }
        } else {
            z = false;
            if (f2 > 0.0f) {
                ensureRightGlow();
                z = false;
                if (this.mRightGlow.onPull(f2 / getWidth(), f3 / getHeight())) {
                    z = true;
                }
            }
        }
        if (f4 < 0.0f) {
            ensureTopGlow();
            z2 = z;
            if (this.mTopGlow.onPull((-f4) / getHeight(), f / getWidth())) {
                z2 = true;
            }
        } else {
            z2 = z;
            if (f4 > 0.0f) {
                ensureBottomGlow();
                z2 = z;
                if (this.mBottomGlow.onPull(f4 / getHeight(), 1.0f - (f / getWidth()))) {
                    z2 = true;
                }
            }
        }
        if (!z2 && f2 == 0.0f && f4 == 0.0f) {
            return;
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void recordAnimationInfoIfBouncedHiddenView(ViewHolder viewHolder, ItemAnimator.ItemHolderInfo itemHolderInfo) {
        viewHolder.setFlags(0, 8192);
        if (this.mState.mTrackOldChangeHolders && viewHolder.isUpdated() && !viewHolder.isRemoved() && !viewHolder.shouldIgnore()) {
            this.mViewInfoStore.addToOldChangeHolders(getChangedHolderKey(viewHolder), viewHolder);
        }
        this.mViewInfoStore.addToPreLayout(viewHolder, itemHolderInfo);
    }

    private void recoverFocusFromState() {
        View focusedChild;
        if (this.mPreserveFocusAfterLayout && this.mAdapter != null && hasFocus()) {
            if (isFocused() || ((focusedChild = getFocusedChild()) != null && this.mChildHelper.isHidden(focusedChild))) {
                ViewHolder viewHolder = null;
                if (this.mState.mFocusedItemPosition != -1) {
                    viewHolder = findViewHolderForAdapterPosition(this.mState.mFocusedItemPosition);
                }
                ViewHolder viewHolder2 = viewHolder;
                if (viewHolder == null) {
                    viewHolder2 = viewHolder;
                    if (this.mState.mFocusedItemId != -1) {
                        viewHolder2 = viewHolder;
                        if (this.mAdapter.hasStableIds()) {
                            viewHolder2 = findViewHolderForItemId(this.mState.mFocusedItemId);
                        }
                    }
                }
                if (viewHolder2 == null || viewHolder2.itemView.hasFocus() || !viewHolder2.itemView.hasFocusable()) {
                    return;
                }
                View view = viewHolder2.itemView;
                View view2 = view;
                if (this.mState.mFocusedSubChildId != -1) {
                    View findViewById = viewHolder2.itemView.findViewById(this.mState.mFocusedSubChildId);
                    view2 = view;
                    if (findViewById != null) {
                        view2 = view;
                        if (findViewById.isFocusable()) {
                            view2 = findViewById;
                        }
                    }
                }
                view2.requestFocus();
            }
        }
    }

    private void releaseGlows() {
        boolean z = false;
        if (this.mLeftGlow != null) {
            z = this.mLeftGlow.onRelease();
        }
        boolean z2 = z;
        if (this.mTopGlow != null) {
            z2 = z | this.mTopGlow.onRelease();
        }
        boolean z3 = z2;
        if (this.mRightGlow != null) {
            z3 = z2 | this.mRightGlow.onRelease();
        }
        boolean z4 = z3;
        if (this.mBottomGlow != null) {
            z4 = z3 | this.mBottomGlow.onRelease();
        }
        if (z4) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean removeAnimatingView(View view) {
        eatRequestLayout();
        boolean removeViewIfHidden = this.mChildHelper.removeViewIfHidden(view);
        if (removeViewIfHidden) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(view);
            this.mRecycler.unscrapView(childViewHolderInt);
            this.mRecycler.recycleViewHolderInternal(childViewHolderInt);
        }
        resumeRequestLayout(!removeViewIfHidden);
        return removeViewIfHidden;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void repositionShadowingViews() {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mChildHelper.getChildAt(i);
            ViewHolder childViewHolder = getChildViewHolder(childAt);
            if (childViewHolder != null && childViewHolder.mShadowingHolder != null) {
                View view = childViewHolder.mShadowingHolder.itemView;
                int left = childAt.getLeft();
                int top = childAt.getTop();
                if (left != view.getLeft() || top != view.getTop()) {
                    view.layout(left, top, view.getWidth() + left, view.getHeight() + top);
                }
            }
        }
    }

    private void resetFocusInfo() {
        this.mState.mFocusedItemId = -1L;
        this.mState.mFocusedItemPosition = -1;
        this.mState.mFocusedSubChildId = -1;
    }

    private void resetTouch() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.clear();
        }
        stopNestedScroll();
        releaseGlows();
    }

    private void saveFocusInfo() {
        View view = null;
        if (this.mPreserveFocusAfterLayout) {
            view = null;
            if (hasFocus()) {
                view = null;
                if (this.mAdapter != null) {
                    view = getFocusedChild();
                }
            }
        }
        ViewHolder findContainingViewHolder = view == null ? null : findContainingViewHolder(view);
        if (findContainingViewHolder == null) {
            resetFocusInfo();
            return;
        }
        this.mState.mFocusedItemId = this.mAdapter.hasStableIds() ? findContainingViewHolder.getItemId() : -1L;
        this.mState.mFocusedItemPosition = this.mDataSetHasChangedAfterLayout ? -1 : findContainingViewHolder.getAdapterPosition();
        this.mState.mFocusedSubChildId = getDeepestFocusedViewWithId(findContainingViewHolder.itemView);
    }

    private void setAdapterInternal(Adapter adapter, boolean z, boolean z2) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterAdapterDataObserver(this.mObserver);
            this.mAdapter.onDetachedFromRecyclerView(this);
        }
        if (!z || z2) {
            if (this.mItemAnimator != null) {
                this.mItemAnimator.endAnimations();
            }
            if (this.mLayout != null) {
                this.mLayout.removeAndRecycleAllViews(this.mRecycler);
                this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
            }
            this.mRecycler.clear();
        }
        this.mAdapterHelper.reset();
        Adapter adapter2 = this.mAdapter;
        this.mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(this.mObserver);
            adapter.onAttachedToRecyclerView(this);
        }
        if (this.mLayout != null) {
            this.mLayout.onAdapterChanged(adapter2, this.mAdapter);
        }
        this.mRecycler.onAdapterChanged(adapter2, this.mAdapter, z);
        this.mState.mStructureChanged = true;
        markKnownViewsInvalid();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDataSetChangedAfterLayout() {
        if (this.mDataSetHasChangedAfterLayout) {
            return;
        }
        this.mDataSetHasChangedAfterLayout = true;
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.addFlags(512);
            }
        }
        this.mRecycler.setAdapterPositionsAsUnknown();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setScrollState(int i) {
        if (i == this.mScrollState) {
            return;
        }
        this.mScrollState = i;
        if (i != 2) {
            stopScrollersInternal();
        }
        dispatchOnScrollStateChanged(i);
    }

    private void stopScrollersInternal() {
        this.mViewFlinger.stop();
        if (this.mLayout != null) {
            this.mLayout.stopSmoothScroller();
        }
    }

    void absorbGlows(int i, int i2) {
        if (i < 0) {
            ensureLeftGlow();
            this.mLeftGlow.onAbsorb(-i);
        } else if (i > 0) {
            ensureRightGlow();
            this.mRightGlow.onAbsorb(i);
        }
        if (i2 < 0) {
            ensureTopGlow();
            this.mTopGlow.onAbsorb(-i2);
        } else if (i2 > 0) {
            ensureBottomGlow();
            this.mBottomGlow.onAbsorb(i2);
        }
        if (i == 0 && i2 == 0) {
            return;
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        if (this.mLayout == null || !this.mLayout.onAddFocusables(this, arrayList, i, i2)) {
            super.addFocusables(arrayList, i, i2);
        }
    }

    public void addItemDecoration(ItemDecoration itemDecoration) {
        addItemDecoration(itemDecoration, -1);
    }

    public void addItemDecoration(ItemDecoration itemDecoration, int i) {
        if (this.mLayout != null) {
            this.mLayout.assertNotInLayoutOrScroll("Cannot add item decoration during a scroll  or layout");
        }
        if (this.mItemDecorations.isEmpty()) {
            setWillNotDraw(false);
        }
        if (i < 0) {
            this.mItemDecorations.add(itemDecoration);
        } else {
            this.mItemDecorations.add(i, itemDecoration);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public void addOnItemTouchListener(OnItemTouchListener onItemTouchListener) {
        this.mOnItemTouchListeners.add(onItemTouchListener);
    }

    public void addOnScrollListener(OnScrollListener onScrollListener) {
        if (this.mScrollListeners == null) {
            this.mScrollListeners = new ArrayList();
        }
        this.mScrollListeners.add(onScrollListener);
    }

    void assertNotInLayoutOrScroll(String str) {
        if (isComputingLayout()) {
            if (str != null) {
                throw new IllegalStateException(str);
            }
            throw new IllegalStateException("Cannot call this method while RecyclerView is computing a layout or scrolling");
        }
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams ? this.mLayout.checkLayoutParams((LayoutParams) layoutParams) : false;
    }

    void clearOldPositions() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.clearOldPosition();
            }
        }
        this.mRecycler.clearOldPositions();
    }

    @Override // android.view.View, android.support.v4.view.ScrollingView
    public int computeHorizontalScrollExtent() {
        int i = 0;
        if (this.mLayout == null) {
            return 0;
        }
        if (this.mLayout.canScrollHorizontally()) {
            i = this.mLayout.computeHorizontalScrollExtent(this.mState);
        }
        return i;
    }

    @Override // android.view.View, android.support.v4.view.ScrollingView
    public int computeHorizontalScrollOffset() {
        int i = 0;
        if (this.mLayout == null) {
            return 0;
        }
        if (this.mLayout.canScrollHorizontally()) {
            i = this.mLayout.computeHorizontalScrollOffset(this.mState);
        }
        return i;
    }

    @Override // android.view.View, android.support.v4.view.ScrollingView
    public int computeHorizontalScrollRange() {
        int i = 0;
        if (this.mLayout == null) {
            return 0;
        }
        if (this.mLayout.canScrollHorizontally()) {
            i = this.mLayout.computeHorizontalScrollRange(this.mState);
        }
        return i;
    }

    @Override // android.view.View, android.support.v4.view.ScrollingView
    public int computeVerticalScrollExtent() {
        int i = 0;
        if (this.mLayout == null) {
            return 0;
        }
        if (this.mLayout.canScrollVertically()) {
            i = this.mLayout.computeVerticalScrollExtent(this.mState);
        }
        return i;
    }

    @Override // android.view.View, android.support.v4.view.ScrollingView
    public int computeVerticalScrollOffset() {
        int i = 0;
        if (this.mLayout == null) {
            return 0;
        }
        if (this.mLayout.canScrollVertically()) {
            i = this.mLayout.computeVerticalScrollOffset(this.mState);
        }
        return i;
    }

    @Override // android.view.View, android.support.v4.view.ScrollingView
    public int computeVerticalScrollRange() {
        int i = 0;
        if (this.mLayout == null) {
            return 0;
        }
        if (this.mLayout.canScrollVertically()) {
            i = this.mLayout.computeVerticalScrollRange(this.mState);
        }
        return i;
    }

    void defaultOnMeasure(int i, int i2) {
        setMeasuredDimension(LayoutManager.chooseSize(i, getPaddingLeft() + getPaddingRight(), ViewCompat.getMinimumWidth(this)), LayoutManager.chooseSize(i2, getPaddingTop() + getPaddingBottom(), ViewCompat.getMinimumHeight(this)));
    }

    void dispatchLayout() {
        if (this.mAdapter == null) {
            Log.e("RecyclerView", "No adapter attached; skipping layout");
        } else if (this.mLayout == null) {
            Log.e("RecyclerView", "No layout manager attached; skipping layout");
        } else {
            this.mState.mIsMeasuring = false;
            if (this.mState.mLayoutStep == 1) {
                dispatchLayoutStep1();
                this.mLayout.setExactMeasureSpecsFrom(this);
                dispatchLayoutStep2();
            } else if (!this.mAdapterHelper.hasUpdates() && this.mLayout.getWidth() == getWidth() && this.mLayout.getHeight() == getHeight()) {
                this.mLayout.setExactMeasureSpecsFrom(this);
            } else {
                this.mLayout.setExactMeasureSpecsFrom(this);
                dispatchLayoutStep2();
            }
            dispatchLayoutStep3();
        }
    }

    @Override // android.view.View
    public boolean dispatchNestedFling(float f, float f2, boolean z) {
        return getScrollingChildHelper().dispatchNestedFling(f, f2, z);
    }

    @Override // android.view.View
    public boolean dispatchNestedPreFling(float f, float f2) {
        return getScrollingChildHelper().dispatchNestedPreFling(f, f2);
    }

    @Override // android.view.View
    public boolean dispatchNestedPreScroll(int i, int i2, int[] iArr, int[] iArr2) {
        return getScrollingChildHelper().dispatchNestedPreScroll(i, i2, iArr, iArr2);
    }

    @Override // android.view.View
    public boolean dispatchNestedScroll(int i, int i2, int i3, int i4, int[] iArr) {
        return getScrollingChildHelper().dispatchNestedScroll(i, i2, i3, i4, iArr);
    }

    void dispatchOnScrollStateChanged(int i) {
        if (this.mLayout != null) {
            this.mLayout.onScrollStateChanged(i);
        }
        onScrollStateChanged(i);
        if (this.mScrollListener != null) {
            this.mScrollListener.onScrollStateChanged(this, i);
        }
        if (this.mScrollListeners != null) {
            for (int size = this.mScrollListeners.size() - 1; size >= 0; size--) {
                this.mScrollListeners.get(size).onScrollStateChanged(this, i);
            }
        }
    }

    void dispatchOnScrolled(int i, int i2) {
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        onScrollChanged(scrollX, scrollY, scrollX, scrollY);
        onScrolled(i, i2);
        if (this.mScrollListener != null) {
            this.mScrollListener.onScrolled(this, i, i2);
        }
        if (this.mScrollListeners != null) {
            for (int size = this.mScrollListeners.size() - 1; size >= 0; size--) {
                this.mScrollListeners.get(size).onScrolled(this, i, i2);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        dispatchThawSelfOnly(sparseArray);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> sparseArray) {
        dispatchFreezeSelfOnly(sparseArray);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int size = this.mItemDecorations.size();
        for (int i = 0; i < size; i++) {
            this.mItemDecorations.get(i).onDrawOver(canvas, this, this.mState);
        }
        boolean z = false;
        if (this.mLeftGlow != null) {
            if (this.mLeftGlow.isFinished()) {
                z = false;
            } else {
                int save = canvas.save();
                int paddingBottom = this.mClipToPadding ? getPaddingBottom() : 0;
                canvas.rotate(270.0f);
                canvas.translate((-getHeight()) + paddingBottom, 0.0f);
                z = this.mLeftGlow != null ? this.mLeftGlow.draw(canvas) : false;
                canvas.restoreToCount(save);
            }
        }
        boolean z2 = z;
        if (this.mTopGlow != null) {
            if (this.mTopGlow.isFinished()) {
                z2 = z;
            } else {
                int save2 = canvas.save();
                if (this.mClipToPadding) {
                    canvas.translate(getPaddingLeft(), getPaddingTop());
                }
                z2 = z | (this.mTopGlow != null ? this.mTopGlow.draw(canvas) : false);
                canvas.restoreToCount(save2);
            }
        }
        boolean z3 = z2;
        if (this.mRightGlow != null) {
            if (this.mRightGlow.isFinished()) {
                z3 = z2;
            } else {
                int save3 = canvas.save();
                int width = getWidth();
                int paddingTop = this.mClipToPadding ? getPaddingTop() : 0;
                canvas.rotate(90.0f);
                canvas.translate(-paddingTop, -width);
                z3 = z2 | (this.mRightGlow != null ? this.mRightGlow.draw(canvas) : false);
                canvas.restoreToCount(save3);
            }
        }
        boolean z4 = z3;
        if (this.mBottomGlow != null) {
            if (this.mBottomGlow.isFinished()) {
                z4 = z3;
            } else {
                int save4 = canvas.save();
                canvas.rotate(180.0f);
                if (this.mClipToPadding) {
                    canvas.translate((-getWidth()) + getPaddingRight(), (-getHeight()) + getPaddingBottom());
                } else {
                    canvas.translate(-getWidth(), -getHeight());
                }
                z4 = z3 | (this.mBottomGlow != null ? this.mBottomGlow.draw(canvas) : false);
                canvas.restoreToCount(save4);
            }
        }
        boolean z5 = z4;
        if (!z4) {
            z5 = z4;
            if (this.mItemAnimator != null) {
                z5 = z4;
                if (this.mItemDecorations.size() > 0) {
                    z5 = z4;
                    if (this.mItemAnimator.isRunning()) {
                        z5 = true;
                    }
                }
            }
        }
        if (z5) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View view, long j) {
        return super.drawChild(canvas, view, j);
    }

    void eatRequestLayout() {
        this.mEatRequestLayout++;
        if (this.mEatRequestLayout != 1 || this.mLayoutFrozen) {
            return;
        }
        this.mLayoutRequestEaten = false;
    }

    void ensureBottomGlow() {
        if (this.mBottomGlow != null) {
            return;
        }
        this.mBottomGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mBottomGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
        } else {
            this.mBottomGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    void ensureLeftGlow() {
        if (this.mLeftGlow != null) {
            return;
        }
        this.mLeftGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mLeftGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
        } else {
            this.mLeftGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
        }
    }

    void ensureRightGlow() {
        if (this.mRightGlow != null) {
            return;
        }
        this.mRightGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mRightGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
        } else {
            this.mRightGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
        }
    }

    void ensureTopGlow() {
        if (this.mTopGlow != null) {
            return;
        }
        this.mTopGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mTopGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
        } else {
            this.mTopGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    @Nullable
    public View findContainingItemView(View view) {
        ViewParent viewParent;
        ViewParent parent = view.getParent();
        while (true) {
            viewParent = parent;
            if (viewParent == null || viewParent == this || !(viewParent instanceof View)) {
                break;
            }
            view = (View) viewParent;
            parent = view.getParent();
        }
        if (viewParent != this) {
            view = null;
        }
        return view;
    }

    @Nullable
    public ViewHolder findContainingViewHolder(View view) {
        View findContainingItemView = findContainingItemView(view);
        return findContainingItemView == null ? null : getChildViewHolder(findContainingItemView);
    }

    public ViewHolder findViewHolderForAdapterPosition(int i) {
        if (this.mDataSetHasChangedAfterLayout) {
            return null;
        }
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        ViewHolder viewHolder = null;
        int i2 = 0;
        while (i2 < unfilteredChildCount) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i2));
            ViewHolder viewHolder2 = viewHolder;
            if (childViewHolderInt != null) {
                if (childViewHolderInt.isRemoved()) {
                    viewHolder2 = viewHolder;
                } else {
                    viewHolder2 = viewHolder;
                    if (getAdapterPositionFor(childViewHolderInt) != i) {
                        continue;
                    } else if (!this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                        return childViewHolderInt;
                    } else {
                        viewHolder2 = childViewHolderInt;
                    }
                }
            }
            i2++;
            viewHolder = viewHolder2;
        }
        return viewHolder;
    }

    public ViewHolder findViewHolderForItemId(long j) {
        if (this.mAdapter == null || !this.mAdapter.hasStableIds()) {
            return null;
        }
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        ViewHolder viewHolder = null;
        int i = 0;
        while (i < unfilteredChildCount) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            ViewHolder viewHolder2 = viewHolder;
            if (childViewHolderInt != null) {
                if (childViewHolderInt.isRemoved()) {
                    viewHolder2 = viewHolder;
                } else {
                    viewHolder2 = viewHolder;
                    if (childViewHolderInt.getItemId() != j) {
                        continue;
                    } else if (!this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                        return childViewHolderInt;
                    } else {
                        viewHolder2 = childViewHolderInt;
                    }
                }
            }
            i++;
            viewHolder = viewHolder2;
        }
        return viewHolder;
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x0061  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0078 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    ViewHolder findViewHolderForPosition(int i, boolean z) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        ViewHolder viewHolder = null;
        int i2 = 0;
        while (i2 < unfilteredChildCount) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i2));
            ViewHolder viewHolder2 = viewHolder;
            if (childViewHolderInt != null) {
                if (childViewHolderInt.isRemoved()) {
                    viewHolder2 = viewHolder;
                } else if (z) {
                    viewHolder2 = viewHolder;
                    if (childViewHolderInt.mPosition != i) {
                        continue;
                    }
                    if (this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                        return childViewHolderInt;
                    }
                    viewHolder2 = childViewHolderInt;
                } else {
                    if (childViewHolderInt.getLayoutPosition() != i) {
                        viewHolder2 = viewHolder;
                    }
                    if (this.mChildHelper.isHidden(childViewHolderInt.itemView)) {
                    }
                }
            }
            i2++;
            viewHolder = viewHolder2;
        }
        return viewHolder;
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x003c, code lost:
        if (java.lang.Math.abs(r6) < r5.mMinFlingVelocity) goto L34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x0051, code lost:
        if (java.lang.Math.abs(r7) < r5.mMinFlingVelocity) goto L33;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean fling(int i, int i2) {
        int i3;
        int i4;
        if (this.mLayout == null) {
            Log.e("RecyclerView", "Cannot fling without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return false;
        } else if (this.mLayoutFrozen) {
            return false;
        } else {
            boolean canScrollHorizontally = this.mLayout.canScrollHorizontally();
            boolean canScrollVertically = this.mLayout.canScrollVertically();
            if (canScrollHorizontally) {
                i3 = i;
            }
            i3 = 0;
            if (canScrollVertically) {
                i4 = i2;
            }
            i4 = 0;
            if ((i3 == 0 && i4 == 0) || dispatchNestedPreFling(i3, i4)) {
                return false;
            }
            if (canScrollHorizontally) {
                canScrollVertically = true;
            }
            dispatchNestedFling(i3, i4, canScrollVertically);
            if (canScrollVertically) {
                this.mViewFlinger.fling(Math.max(-this.mMaxFlingVelocity, Math.min(i3, this.mMaxFlingVelocity)), Math.max(-this.mMaxFlingVelocity, Math.min(i4, this.mMaxFlingVelocity)));
                return true;
            }
            return false;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public View focusSearch(View view, int i) {
        View view2;
        View onInterceptFocusSearch = this.mLayout.onInterceptFocusSearch(view, i);
        if (onInterceptFocusSearch != null) {
            return onInterceptFocusSearch;
        }
        boolean z = (this.mAdapter == null || this.mLayout == null || isComputingLayout()) ? false : !this.mLayoutFrozen;
        FocusFinder focusFinder = FocusFinder.getInstance();
        if (z && (i == 2 || i == 1)) {
            boolean z2 = false;
            if (this.mLayout.canScrollVertically()) {
                z2 = focusFinder.findNextFocus(this, view, i == 2 ? 130 : 33) == null;
            }
            boolean z3 = z2;
            if (!z2) {
                z3 = z2;
                if (this.mLayout.canScrollHorizontally()) {
                    z3 = focusFinder.findNextFocus(this, view, (i == 2) ^ (this.mLayout.getLayoutDirection() == 1) ? 66 : 17) == null;
                }
            }
            if (z3) {
                consumePendingUpdateOperations();
                if (findContainingItemView(view) == null) {
                    return null;
                }
                eatRequestLayout();
                this.mLayout.onFocusSearchFailed(view, i, this.mRecycler, this.mState);
                resumeRequestLayout(false);
            }
            view2 = focusFinder.findNextFocus(this, view, i);
        } else {
            View findNextFocus = focusFinder.findNextFocus(this, view, i);
            view2 = findNextFocus;
            if (findNextFocus == null) {
                view2 = findNextFocus;
                if (z) {
                    consumePendingUpdateOperations();
                    if (findContainingItemView(view) == null) {
                        return null;
                    }
                    eatRequestLayout();
                    view2 = this.mLayout.onFocusSearchFailed(view, i, this.mRecycler, this.mState);
                    resumeRequestLayout(false);
                }
            }
        }
        if (!isPreferredNextFocus(view, view2, i)) {
            view2 = super.focusSearch(view, i);
        }
        return view2;
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        if (this.mLayout == null) {
            throw new IllegalStateException("RecyclerView has no LayoutManager");
        }
        return this.mLayout.generateDefaultLayoutParams();
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        if (this.mLayout == null) {
            throw new IllegalStateException("RecyclerView has no LayoutManager");
        }
        return this.mLayout.generateLayoutParams(getContext(), attributeSet);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (this.mLayout == null) {
            throw new IllegalStateException("RecyclerView has no LayoutManager");
        }
        return this.mLayout.generateLayoutParams(layoutParams);
    }

    @Override // android.view.View
    public int getBaseline() {
        return this.mLayout != null ? this.mLayout.getBaseline() : super.getBaseline();
    }

    long getChangedHolderKey(ViewHolder viewHolder) {
        return this.mAdapter.hasStableIds() ? viewHolder.getItemId() : viewHolder.mPosition;
    }

    public int getChildAdapterPosition(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        return childViewHolderInt != null ? childViewHolderInt.getAdapterPosition() : -1;
    }

    @Override // android.view.ViewGroup
    protected int getChildDrawingOrder(int i, int i2) {
        return this.mChildDrawingOrderCallback == null ? super.getChildDrawingOrder(i, i2) : this.mChildDrawingOrderCallback.onGetChildDrawingOrder(i, i2);
    }

    public int getChildLayoutPosition(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        return childViewHolderInt != null ? childViewHolderInt.getLayoutPosition() : -1;
    }

    @Deprecated
    public int getChildPosition(View view) {
        return getChildAdapterPosition(view);
    }

    public ViewHolder getChildViewHolder(View view) {
        ViewParent parent = view.getParent();
        if (parent == null || parent == this) {
            return getChildViewHolderInt(view);
        }
        throw new IllegalArgumentException("View " + view + " is not a direct child of " + this);
    }

    Rect getItemDecorInsetsForChild(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams.mInsetsDirty) {
            Rect rect = layoutParams.mDecorInsets;
            rect.set(0, 0, 0, 0);
            int size = this.mItemDecorations.size();
            for (int i = 0; i < size; i++) {
                this.mTempRect.set(0, 0, 0, 0);
                this.mItemDecorations.get(i).getItemOffsets(this.mTempRect, view, this, this.mState);
                rect.left += this.mTempRect.left;
                rect.top += this.mTempRect.top;
                rect.right += this.mTempRect.right;
                rect.bottom += this.mTempRect.bottom;
            }
            layoutParams.mInsetsDirty = false;
            return rect;
        }
        return layoutParams.mDecorInsets;
    }

    public LayoutManager getLayoutManager() {
        return this.mLayout;
    }

    public RecycledViewPool getRecycledViewPool() {
        return this.mRecycler.getRecycledViewPool();
    }

    public int getScrollState() {
        return this.mScrollState;
    }

    @Override // android.view.View
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    public boolean hasPendingAdapterUpdates() {
        return (!this.mFirstLayoutComplete || this.mDataSetHasChangedAfterLayout) ? true : this.mAdapterHelper.hasPendingUpdates();
    }

    void initAdapterManager() {
        this.mAdapterHelper = new AdapterHelper(new AdapterHelper.Callback(this) { // from class: android.support.v7.widget.RecyclerView.6
            final RecyclerView this$0;

            {
                this.this$0 = this;
            }

            void dispatchUpdate(AdapterHelper.UpdateOp updateOp) {
                switch (updateOp.cmd) {
                    case 1:
                        this.this$0.mLayout.onItemsAdded(this.this$0, updateOp.positionStart, updateOp.itemCount);
                        return;
                    case 2:
                        this.this$0.mLayout.onItemsRemoved(this.this$0, updateOp.positionStart, updateOp.itemCount);
                        return;
                    case 3:
                    case 5:
                    case 6:
                    case 7:
                    default:
                        return;
                    case 4:
                        this.this$0.mLayout.onItemsUpdated(this.this$0, updateOp.positionStart, updateOp.itemCount, updateOp.payload);
                        return;
                    case 8:
                        this.this$0.mLayout.onItemsMoved(this.this$0, updateOp.positionStart, updateOp.itemCount, 1);
                        return;
                }
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public ViewHolder findViewHolder(int i) {
                ViewHolder findViewHolderForPosition = this.this$0.findViewHolderForPosition(i, true);
                if (findViewHolderForPosition == null || this.this$0.mChildHelper.isHidden(findViewHolderForPosition.itemView)) {
                    return null;
                }
                return findViewHolderForPosition;
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public void markViewHoldersUpdated(int i, int i2, Object obj) {
                this.this$0.viewRangeUpdate(i, i2, obj);
                this.this$0.mItemsChanged = true;
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public void offsetPositionsForAdd(int i, int i2) {
                this.this$0.offsetPositionRecordsForInsert(i, i2);
                this.this$0.mItemsAddedOrRemoved = true;
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public void offsetPositionsForMove(int i, int i2) {
                this.this$0.offsetPositionRecordsForMove(i, i2);
                this.this$0.mItemsAddedOrRemoved = true;
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public void offsetPositionsForRemovingInvisible(int i, int i2) {
                this.this$0.offsetPositionRecordsForRemove(i, i2, true);
                this.this$0.mItemsAddedOrRemoved = true;
                this.this$0.mState.mDeletedInvisibleItemCountSincePreviousLayout += i2;
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public void offsetPositionsForRemovingLaidOutOrNewView(int i, int i2) {
                this.this$0.offsetPositionRecordsForRemove(i, i2, false);
                this.this$0.mItemsAddedOrRemoved = true;
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public void onDispatchFirstPass(AdapterHelper.UpdateOp updateOp) {
                dispatchUpdate(updateOp);
            }

            @Override // android.support.v7.widget.AdapterHelper.Callback
            public void onDispatchSecondPass(AdapterHelper.UpdateOp updateOp) {
                dispatchUpdate(updateOp);
            }
        });
    }

    void invalidateGlows() {
        this.mBottomGlow = null;
        this.mTopGlow = null;
        this.mRightGlow = null;
        this.mLeftGlow = null;
    }

    boolean isAccessibilityEnabled() {
        return this.mAccessibilityManager != null ? this.mAccessibilityManager.isEnabled() : false;
    }

    @Override // android.view.View
    public boolean isAttachedToWindow() {
        return this.mIsAttached;
    }

    public boolean isComputingLayout() {
        boolean z = false;
        if (this.mLayoutOrScrollCounter > 0) {
            z = true;
        }
        return z;
    }

    @Override // android.view.View
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    void markItemDecorInsetsDirty() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ((LayoutParams) this.mChildHelper.getUnfilteredChildAt(i).getLayoutParams()).mInsetsDirty = true;
        }
        this.mRecycler.markItemDecorInsetsDirty();
    }

    void markKnownViewsInvalid() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.addFlags(6);
            }
        }
        markItemDecorInsetsDirty();
        this.mRecycler.markKnownViewsInvalid();
    }

    public void offsetChildrenHorizontal(int i) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            this.mChildHelper.getChildAt(i2).offsetLeftAndRight(i);
        }
    }

    public void offsetChildrenVertical(int i) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            this.mChildHelper.getChildAt(i2).offsetTopAndBottom(i);
        }
    }

    void offsetPositionRecordsForInsert(int i, int i2) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i3 = 0; i3 < unfilteredChildCount; i3++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i3));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.mPosition >= i) {
                childViewHolderInt.offsetPosition(i2, false);
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForInsert(i, i2);
        requestLayout();
    }

    void offsetPositionRecordsForMove(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        if (i < i2) {
            i3 = i;
            i4 = i2;
            i5 = -1;
        } else {
            i3 = i2;
            i4 = i;
            i5 = 1;
        }
        for (int i6 = 0; i6 < unfilteredChildCount; i6++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i6));
            if (childViewHolderInt != null && childViewHolderInt.mPosition >= i3 && childViewHolderInt.mPosition <= i4) {
                if (childViewHolderInt.mPosition == i) {
                    childViewHolderInt.offsetPosition(i2 - i, false);
                } else {
                    childViewHolderInt.offsetPosition(i5, false);
                }
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForMove(i, i2);
        requestLayout();
    }

    void offsetPositionRecordsForRemove(int i, int i2, boolean z) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i3 = 0; i3 < unfilteredChildCount; i3++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i3));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                if (childViewHolderInt.mPosition >= i + i2) {
                    childViewHolderInt.offsetPosition(-i2, z);
                    this.mState.mStructureChanged = true;
                } else if (childViewHolderInt.mPosition >= i) {
                    childViewHolderInt.flagRemovedAndOffsetPosition(i - 1, -i2, z);
                    this.mState.mStructureChanged = true;
                }
            }
        }
        this.mRecycler.offsetPositionRecordsForRemove(i, i2, z);
        requestLayout();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        boolean z = true;
        super.onAttachedToWindow();
        this.mLayoutOrScrollCounter = 0;
        this.mIsAttached = true;
        if (!this.mFirstLayoutComplete || isLayoutRequested()) {
            z = false;
        }
        this.mFirstLayoutComplete = z;
        if (this.mLayout != null) {
            this.mLayout.dispatchAttachedToWindow(this);
        }
        this.mPostedAnimatorRunner = false;
    }

    public void onChildAttachedToWindow(View view) {
    }

    public void onChildDetachedFromWindow(View view) {
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mItemAnimator != null) {
            this.mItemAnimator.endAnimations();
        }
        stopScroll();
        this.mIsAttached = false;
        if (this.mLayout != null) {
            this.mLayout.dispatchDetachedFromWindow(this, this.mRecycler);
        }
        removeCallbacks(this.mItemAnimatorRunner);
        this.mViewInfoStore.onDetach();
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = this.mItemDecorations.size();
        for (int i = 0; i < size; i++) {
            this.mItemDecorations.get(i).onDraw(canvas, this, this.mState);
        }
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if (this.mLayout == null || this.mLayoutFrozen || (MotionEventCompat.getSource(motionEvent) & 2) == 0 || motionEvent.getAction() != 8) {
            return false;
        }
        float f = this.mLayout.canScrollVertically() ? -MotionEventCompat.getAxisValue(motionEvent, 9) : 0.0f;
        float axisValue = this.mLayout.canScrollHorizontally() ? MotionEventCompat.getAxisValue(motionEvent, 10) : 0.0f;
        if (f == 0.0f && axisValue == 0.0f) {
            return false;
        }
        float scrollFactor = getScrollFactor();
        scrollByInternal((int) (axisValue * scrollFactor), (int) (f * scrollFactor), motionEvent);
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mLayoutFrozen) {
            return false;
        }
        if (dispatchOnItemTouchIntercept(motionEvent)) {
            cancelTouch();
            return true;
        } else if (this.mLayout == null) {
            return false;
        } else {
            boolean canScrollHorizontally = this.mLayout.canScrollHorizontally();
            boolean canScrollVertically = this.mLayout.canScrollVertically();
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            this.mVelocityTracker.addMovement(motionEvent);
            int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
            int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
            switch (actionMasked) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    if (this.mIgnoreMotionEventTillDown) {
                        this.mIgnoreMotionEventTillDown = false;
                    }
                    this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                    int x = (int) (motionEvent.getX() + 0.5f);
                    this.mLastTouchX = x;
                    this.mInitialTouchX = x;
                    int y = (int) (motionEvent.getY() + 0.5f);
                    this.mLastTouchY = y;
                    this.mInitialTouchY = y;
                    if (this.mScrollState == 2) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        setScrollState(1);
                    }
                    int[] iArr = this.mNestedOffsets;
                    this.mNestedOffsets[1] = 0;
                    iArr[0] = 0;
                    int i = 0;
                    if (canScrollHorizontally) {
                        i = 1;
                    }
                    int i2 = i;
                    if (canScrollVertically) {
                        i2 = i | 2;
                    }
                    startNestedScroll(i2);
                    break;
                case 1:
                    this.mVelocityTracker.clear();
                    stopNestedScroll();
                    break;
                case 2:
                    int findPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.mScrollPointerId);
                    if (findPointerIndex >= 0) {
                        int x2 = (int) (MotionEventCompat.getX(motionEvent, findPointerIndex) + 0.5f);
                        int y2 = (int) (MotionEventCompat.getY(motionEvent, findPointerIndex) + 0.5f);
                        if (this.mScrollState != 1) {
                            int i3 = x2 - this.mInitialTouchX;
                            int i4 = y2 - this.mInitialTouchY;
                            boolean z = false;
                            if (canScrollHorizontally) {
                                z = false;
                                if (Math.abs(i3) > this.mTouchSlop) {
                                    this.mLastTouchX = ((i3 < 0 ? -1 : 1) * this.mTouchSlop) + this.mInitialTouchX;
                                    z = true;
                                }
                            }
                            boolean z2 = z;
                            if (canScrollVertically) {
                                z2 = z;
                                if (Math.abs(i4) > this.mTouchSlop) {
                                    this.mLastTouchY = ((i4 < 0 ? -1 : 1) * this.mTouchSlop) + this.mInitialTouchY;
                                    z2 = true;
                                }
                            }
                            if (z2) {
                                setScrollState(1);
                                break;
                            }
                        }
                    } else {
                        Log.e("RecyclerView", "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                        return false;
                    }
                    break;
                case 3:
                    cancelTouch();
                    break;
                case 5:
                    this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, actionIndex);
                    int x3 = (int) (MotionEventCompat.getX(motionEvent, actionIndex) + 0.5f);
                    this.mLastTouchX = x3;
                    this.mInitialTouchX = x3;
                    int y3 = (int) (MotionEventCompat.getY(motionEvent, actionIndex) + 0.5f);
                    this.mLastTouchY = y3;
                    this.mInitialTouchY = y3;
                    break;
                case 6:
                    onPointerUp(motionEvent);
                    break;
            }
            return this.mScrollState == 1;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        TraceCompat.beginSection("RV OnLayout");
        dispatchLayout();
        TraceCompat.endSection();
        this.mFirstLayoutComplete = true;
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (this.mLayout == null) {
            defaultOnMeasure(i, i2);
        } else if (this.mLayout.mAutoMeasure) {
            boolean z = View.MeasureSpec.getMode(i) == 1073741824 ? View.MeasureSpec.getMode(i2) == 1073741824 : false;
            this.mLayout.onMeasure(this.mRecycler, this.mState, i, i2);
            if (z || this.mAdapter == null) {
                return;
            }
            if (this.mState.mLayoutStep == 1) {
                dispatchLayoutStep1();
            }
            this.mLayout.setMeasureSpecs(i, i2);
            this.mState.mIsMeasuring = true;
            dispatchLayoutStep2();
            this.mLayout.setMeasuredDimensionFromChildren(i, i2);
            if (this.mLayout.shouldMeasureTwice()) {
                this.mLayout.setMeasureSpecs(View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
                this.mState.mIsMeasuring = true;
                dispatchLayoutStep2();
                this.mLayout.setMeasuredDimensionFromChildren(i, i2);
            }
        } else if (this.mHasFixedSize) {
            this.mLayout.onMeasure(this.mRecycler, this.mState, i, i2);
        } else {
            if (this.mAdapterUpdateDuringMeasure) {
                eatRequestLayout();
                processAdapterUpdatesAndSetAnimationFlags();
                if (this.mState.mRunPredictiveAnimations) {
                    this.mState.mInPreLayout = true;
                } else {
                    this.mAdapterHelper.consumeUpdatesInOnePass();
                    this.mState.mInPreLayout = false;
                }
                this.mAdapterUpdateDuringMeasure = false;
                resumeRequestLayout(false);
            }
            if (this.mAdapter != null) {
                this.mState.mItemCount = this.mAdapter.getItemCount();
            } else {
                this.mState.mItemCount = 0;
            }
            eatRequestLayout();
            this.mLayout.onMeasure(this.mRecycler, this.mState, i, i2);
            resumeRequestLayout(false);
            this.mState.mInPreLayout = false;
        }
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        if (isComputingLayout()) {
            return false;
        }
        return super.onRequestFocusInDescendants(i, rect);
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        this.mPendingSavedState = (SavedState) parcelable;
        super.onRestoreInstanceState(this.mPendingSavedState.getSuperState());
        if (this.mLayout == null || this.mPendingSavedState.mLayoutState == null) {
            return;
        }
        this.mLayout.onRestoreInstanceState(this.mPendingSavedState.mLayoutState);
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        if (this.mPendingSavedState != null) {
            savedState.copyFrom(this.mPendingSavedState);
        } else if (this.mLayout != null) {
            savedState.mLayoutState = this.mLayout.onSaveInstanceState();
        } else {
            savedState.mLayoutState = null;
        }
        return savedState;
    }

    public void onScrollStateChanged(int i) {
    }

    public void onScrolled(int i, int i2) {
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i == i3 && i2 == i4) {
            return;
        }
        invalidateGlows();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mLayoutFrozen || this.mIgnoreMotionEventTillDown) {
            return false;
        }
        if (dispatchOnItemTouch(motionEvent)) {
            cancelTouch();
            return true;
        } else if (this.mLayout == null) {
            return false;
        } else {
            boolean canScrollHorizontally = this.mLayout.canScrollHorizontally();
            boolean canScrollVertically = this.mLayout.canScrollVertically();
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            MotionEvent obtain = MotionEvent.obtain(motionEvent);
            int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
            int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
            if (actionMasked == 0) {
                int[] iArr = this.mNestedOffsets;
                this.mNestedOffsets[1] = 0;
                iArr[0] = 0;
            }
            obtain.offsetLocation(this.mNestedOffsets[0], this.mNestedOffsets[1]);
            boolean z = false;
            switch (actionMasked) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                    int x = (int) (motionEvent.getX() + 0.5f);
                    this.mLastTouchX = x;
                    this.mInitialTouchX = x;
                    int y = (int) (motionEvent.getY() + 0.5f);
                    this.mLastTouchY = y;
                    this.mInitialTouchY = y;
                    int i = 0;
                    if (canScrollHorizontally) {
                        i = 1;
                    }
                    int i2 = i;
                    if (canScrollVertically) {
                        i2 = i | 2;
                    }
                    startNestedScroll(i2);
                    z = false;
                    break;
                case 1:
                    this.mVelocityTracker.addMovement(obtain);
                    z = true;
                    this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaxFlingVelocity);
                    float f = canScrollHorizontally ? -VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mScrollPointerId) : 0.0f;
                    float f2 = canScrollVertically ? -VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mScrollPointerId) : 0.0f;
                    if (!((f == 0.0f && f2 == 0.0f) ? false : fling((int) f, (int) f2))) {
                        setScrollState(0);
                    }
                    resetTouch();
                    break;
                case 2:
                    int findPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.mScrollPointerId);
                    if (findPointerIndex >= 0) {
                        int x2 = (int) (MotionEventCompat.getX(motionEvent, findPointerIndex) + 0.5f);
                        int y2 = (int) (MotionEventCompat.getY(motionEvent, findPointerIndex) + 0.5f);
                        int i3 = this.mLastTouchX - x2;
                        int i4 = this.mLastTouchY - y2;
                        int i5 = i3;
                        int i6 = i4;
                        if (dispatchNestedPreScroll(i3, i4, this.mScrollConsumed, this.mScrollOffset)) {
                            i5 = i3 - this.mScrollConsumed[0];
                            i6 = i4 - this.mScrollConsumed[1];
                            obtain.offsetLocation(this.mScrollOffset[0], this.mScrollOffset[1]);
                            int[] iArr2 = this.mNestedOffsets;
                            iArr2[0] = iArr2[0] + this.mScrollOffset[0];
                            int[] iArr3 = this.mNestedOffsets;
                            iArr3[1] = iArr3[1] + this.mScrollOffset[1];
                        }
                        int i7 = i5;
                        int i8 = i6;
                        if (this.mScrollState != 1) {
                            int i9 = i5;
                            boolean z2 = false;
                            if (canScrollHorizontally) {
                                i9 = i5;
                                z2 = false;
                                if (Math.abs(i5) > this.mTouchSlop) {
                                    i9 = i5 > 0 ? i5 - this.mTouchSlop : i5 + this.mTouchSlop;
                                    z2 = true;
                                }
                            }
                            int i10 = i6;
                            boolean z3 = z2;
                            if (canScrollVertically) {
                                i10 = i6;
                                z3 = z2;
                                if (Math.abs(i6) > this.mTouchSlop) {
                                    i10 = i6 > 0 ? i6 - this.mTouchSlop : i6 + this.mTouchSlop;
                                    z3 = true;
                                }
                            }
                            i7 = i9;
                            i8 = i10;
                            if (z3) {
                                setScrollState(1);
                                i8 = i10;
                                i7 = i9;
                            }
                        }
                        z = false;
                        if (this.mScrollState == 1) {
                            this.mLastTouchX = x2 - this.mScrollOffset[0];
                            this.mLastTouchY = y2 - this.mScrollOffset[1];
                            if (!canScrollHorizontally) {
                                i7 = 0;
                            }
                            if (!canScrollVertically) {
                                i8 = 0;
                            }
                            z = false;
                            if (scrollByInternal(i7, i8, obtain)) {
                                getParent().requestDisallowInterceptTouchEvent(true);
                                z = false;
                                break;
                            }
                        }
                    } else {
                        Log.e("RecyclerView", "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                        return false;
                    }
                    break;
                case 3:
                    cancelTouch();
                    z = false;
                    break;
                case 4:
                    break;
                case 5:
                    this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, actionIndex);
                    int x3 = (int) (MotionEventCompat.getX(motionEvent, actionIndex) + 0.5f);
                    this.mLastTouchX = x3;
                    this.mInitialTouchX = x3;
                    int y3 = (int) (MotionEventCompat.getY(motionEvent, actionIndex) + 0.5f);
                    this.mLastTouchY = y3;
                    this.mInitialTouchY = y3;
                    z = false;
                    break;
                case 6:
                    onPointerUp(motionEvent);
                    z = false;
                    break;
                default:
                    z = false;
                    break;
            }
            if (!z) {
                this.mVelocityTracker.addMovement(obtain);
            }
            obtain.recycle();
            return true;
        }
    }

    @Override // android.view.ViewGroup
    protected void removeDetachedView(View view, boolean z) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            if (childViewHolderInt.isTmpDetached()) {
                childViewHolderInt.clearTmpDetachFlag();
            } else if (!childViewHolderInt.shouldIgnore()) {
                throw new IllegalArgumentException("Called removeDetachedView with a view which is not flagged as tmp detached." + childViewHolderInt);
            }
        }
        dispatchChildDetached(view);
        super.removeDetachedView(view, z);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        boolean z = false;
        if (!this.mLayout.onRequestChildFocus(this, this.mState, view, view2) && view2 != null) {
            this.mTempRect.set(0, 0, view2.getWidth(), view2.getHeight());
            ViewGroup.LayoutParams layoutParams = view2.getLayoutParams();
            if (layoutParams instanceof LayoutParams) {
                LayoutParams layoutParams2 = (LayoutParams) layoutParams;
                if (!layoutParams2.mInsetsDirty) {
                    Rect rect = layoutParams2.mDecorInsets;
                    this.mTempRect.left -= rect.left;
                    this.mTempRect.right += rect.right;
                    this.mTempRect.top -= rect.top;
                    this.mTempRect.bottom += rect.bottom;
                }
            }
            offsetDescendantRectToMyCoords(view2, this.mTempRect);
            offsetRectIntoDescendantCoords(view, this.mTempRect);
            Rect rect2 = this.mTempRect;
            if (!this.mFirstLayoutComplete) {
                z = true;
            }
            requestChildRectangleOnScreen(view, rect2, z);
        }
        super.requestChildFocus(view, view2);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        return this.mLayout.requestChildRectangleOnScreen(this, view, rect, z);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        int size = this.mOnItemTouchListeners.size();
        for (int i = 0; i < size; i++) {
            this.mOnItemTouchListeners.get(i).onRequestDisallowInterceptTouchEvent(z);
        }
        super.requestDisallowInterceptTouchEvent(z);
    }

    @Override // android.view.View, android.view.ViewParent
    public void requestLayout() {
        if (this.mEatRequestLayout != 0 || this.mLayoutFrozen) {
            this.mLayoutRequestEaten = true;
        } else {
            super.requestLayout();
        }
    }

    void resumeRequestLayout(boolean z) {
        if (this.mEatRequestLayout < 1) {
            this.mEatRequestLayout = 1;
        }
        if (!z) {
            this.mLayoutRequestEaten = false;
        }
        if (this.mEatRequestLayout == 1) {
            if (z && this.mLayoutRequestEaten && !this.mLayoutFrozen && this.mLayout != null && this.mAdapter != null) {
                dispatchLayout();
            }
            if (!this.mLayoutFrozen) {
                this.mLayoutRequestEaten = false;
            }
        }
        this.mEatRequestLayout--;
    }

    void saveOldPositions() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.saveOldPosition();
            }
        }
    }

    @Override // android.view.View
    public void scrollBy(int i, int i2) {
        if (this.mLayout == null) {
            Log.e("RecyclerView", "Cannot scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
        } else if (this.mLayoutFrozen) {
        } else {
            boolean canScrollHorizontally = this.mLayout.canScrollHorizontally();
            boolean canScrollVertically = this.mLayout.canScrollVertically();
            if (canScrollHorizontally || canScrollVertically) {
                if (!canScrollHorizontally) {
                    i = 0;
                }
                if (!canScrollVertically) {
                    i2 = 0;
                }
                scrollByInternal(i, i2, null);
            }
        }
    }

    boolean scrollByInternal(int i, int i2, MotionEvent motionEvent) {
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        consumePendingUpdateOperations();
        if (this.mAdapter != null) {
            eatRequestLayout();
            onEnterLayoutOrScroll();
            TraceCompat.beginSection("RV Scroll");
            i5 = 0;
            i3 = 0;
            if (i != 0) {
                i5 = this.mLayout.scrollHorizontallyBy(i, this.mRecycler, this.mState);
                i3 = i - i5;
            }
            i6 = 0;
            i4 = 0;
            if (i2 != 0) {
                i6 = this.mLayout.scrollVerticallyBy(i2, this.mRecycler, this.mState);
                i4 = i2 - i6;
            }
            TraceCompat.endSection();
            repositionShadowingViews();
            onExitLayoutOrScroll();
            resumeRequestLayout(false);
        }
        if (!this.mItemDecorations.isEmpty()) {
            invalidate();
        }
        if (dispatchNestedScroll(i5, i6, i3, i4, this.mScrollOffset)) {
            this.mLastTouchX -= this.mScrollOffset[0];
            this.mLastTouchY -= this.mScrollOffset[1];
            if (motionEvent != null) {
                motionEvent.offsetLocation(this.mScrollOffset[0], this.mScrollOffset[1]);
            }
            int[] iArr = this.mNestedOffsets;
            iArr[0] = iArr[0] + this.mScrollOffset[0];
            int[] iArr2 = this.mNestedOffsets;
            iArr2[1] = iArr2[1] + this.mScrollOffset[1];
        } else if (ViewCompat.getOverScrollMode(this) != 2) {
            if (motionEvent != null) {
                pullGlows(motionEvent.getX(), i3, motionEvent.getY(), i4);
            }
            considerReleasingGlowsOnScroll(i, i2);
        }
        if (i5 != 0 || i6 != 0) {
            dispatchOnScrolled(i5, i6);
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        return (i5 == 0 && i6 == 0) ? false : true;
    }

    @Override // android.view.View
    public void scrollTo(int i, int i2) {
        Log.w("RecyclerView", "RecyclerView does not support scrolling to an absolute position. Use scrollToPosition instead");
    }

    public void scrollToPosition(int i) {
        if (this.mLayoutFrozen) {
            return;
        }
        stopScroll();
        if (this.mLayout == null) {
            Log.e("RecyclerView", "Cannot scroll to position a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        this.mLayout.scrollToPosition(i);
        awakenScrollBars();
    }

    @Override // android.view.View, android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEventUnchecked(AccessibilityEvent accessibilityEvent) {
        if (shouldDeferAccessibilityEvent(accessibilityEvent)) {
            return;
        }
        super.sendAccessibilityEventUnchecked(accessibilityEvent);
    }

    public void setAccessibilityDelegateCompat(RecyclerViewAccessibilityDelegate recyclerViewAccessibilityDelegate) {
        this.mAccessibilityDelegate = recyclerViewAccessibilityDelegate;
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityDelegate);
    }

    public void setAdapter(Adapter adapter) {
        setLayoutFrozen(false);
        setAdapterInternal(adapter, false, true);
        requestLayout();
    }

    @Override // android.view.ViewGroup
    public void setClipToPadding(boolean z) {
        if (z != this.mClipToPadding) {
            invalidateGlows();
        }
        this.mClipToPadding = z;
        super.setClipToPadding(z);
        if (this.mFirstLayoutComplete) {
            requestLayout();
        }
    }

    public void setHasFixedSize(boolean z) {
        this.mHasFixedSize = z;
    }

    public void setLayoutFrozen(boolean z) {
        if (z != this.mLayoutFrozen) {
            assertNotInLayoutOrScroll("Do not setLayoutFrozen in layout or scroll");
            if (z) {
                long uptimeMillis = SystemClock.uptimeMillis();
                onTouchEvent(MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0));
                this.mLayoutFrozen = true;
                this.mIgnoreMotionEventTillDown = true;
                stopScroll();
                return;
            }
            this.mLayoutFrozen = false;
            if (this.mLayoutRequestEaten && this.mLayout != null && this.mAdapter != null) {
                requestLayout();
            }
            this.mLayoutRequestEaten = false;
        }
    }

    public void setLayoutManager(LayoutManager layoutManager) {
        if (layoutManager == this.mLayout) {
            return;
        }
        stopScroll();
        if (this.mLayout != null) {
            if (this.mIsAttached) {
                this.mLayout.dispatchDetachedFromWindow(this, this.mRecycler);
            }
            this.mLayout.setRecyclerView(null);
        }
        this.mRecycler.clear();
        this.mChildHelper.removeAllViewsUnfiltered();
        this.mLayout = layoutManager;
        if (layoutManager != null) {
            if (layoutManager.mRecyclerView != null) {
                throw new IllegalArgumentException("LayoutManager " + layoutManager + " is already attached to a RecyclerView: " + layoutManager.mRecyclerView);
            }
            this.mLayout.setRecyclerView(this);
            if (this.mIsAttached) {
                this.mLayout.dispatchAttachedToWindow(this);
            }
        }
        requestLayout();
    }

    @Override // android.view.View
    public void setNestedScrollingEnabled(boolean z) {
        getScrollingChildHelper().setNestedScrollingEnabled(z);
    }

    @Deprecated
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mScrollListener = onScrollListener;
    }

    boolean shouldDeferAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (isComputingLayout()) {
            int i = 0;
            if (accessibilityEvent != null) {
                i = AccessibilityEventCompat.getContentChangeTypes(accessibilityEvent);
            }
            int i2 = i;
            if (i == 0) {
                i2 = 0;
            }
            this.mEatenAccessibilityChangeFlags |= i2;
            return true;
        }
        return false;
    }

    public void smoothScrollBy(int i, int i2) {
        if (this.mLayout == null) {
            Log.e("RecyclerView", "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
        } else if (this.mLayoutFrozen) {
        } else {
            if (!this.mLayout.canScrollHorizontally()) {
                i = 0;
            }
            if (!this.mLayout.canScrollVertically()) {
                i2 = 0;
            }
            if (i == 0 && i2 == 0) {
                return;
            }
            this.mViewFlinger.smoothScrollBy(i, i2);
        }
    }

    @Override // android.view.View
    public boolean startNestedScroll(int i) {
        return getScrollingChildHelper().startNestedScroll(i);
    }

    @Override // android.view.View, android.support.v4.view.NestedScrollingChild
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    public void stopScroll() {
        setScrollState(0);
        stopScrollersInternal();
    }

    void viewRangeUpdate(int i, int i2, Object obj) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i3 = 0; i3 < unfilteredChildCount; i3++) {
            View unfilteredChildAt = this.mChildHelper.getUnfilteredChildAt(i3);
            ViewHolder childViewHolderInt = getChildViewHolderInt(unfilteredChildAt);
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.mPosition >= i && childViewHolderInt.mPosition < i + i2) {
                childViewHolderInt.addFlags(2);
                childViewHolderInt.addChangePayload(obj);
                ((LayoutParams) unfilteredChildAt.getLayoutParams()).mInsetsDirty = true;
            }
        }
        this.mRecycler.viewRangeUpdate(i, i2);
    }
}
