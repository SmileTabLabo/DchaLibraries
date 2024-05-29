package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import com.android.launcher3.PageIndicator;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.util.LauncherEdgeEffect;
import com.mediatek.launcher3.LauncherHelper;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/PagedView.class */
public abstract class PagedView extends ViewGroup implements ViewGroup.OnHierarchyChangeListener {
    private int NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT;
    protected int mActivePointerId;
    protected boolean mAllowOverScroll;
    private boolean mCancelTap;
    protected boolean mCenterPagesVertically;
    protected int mChildCountOnLastLayout;
    protected int mCurrentPage;
    private Interpolator mDefaultInterpolator;
    protected float mDensity;
    private float mDownMotionX;
    private float mDownMotionY;
    private float mDownScrollX;
    protected View mDragView;
    private float mDragViewBaselineLeft;
    private final LauncherEdgeEffect mEdgeGlowLeft;
    private final LauncherEdgeEffect mEdgeGlowRight;
    protected boolean mFadeInAdjacentScreens;
    protected boolean mFirstLayout;
    protected int mFlingThresholdVelocity;
    protected boolean mForceScreenScrolled;
    private boolean mFreeScroll;
    private int mFreeScrollMaxScrollX;
    private int mFreeScrollMinScrollX;
    private boolean mInOverviewMode;
    protected final Rect mInsets;
    protected boolean mIsPageMoving;
    private boolean mIsReordering;
    protected final boolean mIsRtl;
    protected float mLastMotionX;
    protected float mLastMotionXRemainder;
    protected float mLastMotionY;
    private int mLastScreenCenter;
    private float mLastX;
    protected View.OnLongClickListener mLongClickListener;
    protected int mMaxScrollX;
    private int mMaximumVelocity;
    protected int mMinFlingVelocity;
    private float mMinScale;
    protected int mMinSnapVelocity;
    protected int mNextPage;
    private int mNormalChildHeight;
    PageIndicator mPageIndicator;
    int mPageIndicatorViewId;
    protected int mPageLayoutHeightGap;
    protected int mPageLayoutWidthGap;
    private int[] mPageScrolls;
    int mPageSpacing;
    private PageSwitchListener mPageSwitchListener;
    private float mParentDownMotionX;
    private float mParentDownMotionY;
    private int mPostReorderingPreZoomInRemainingAnimationCount;
    private Runnable mPostReorderingPreZoomInRunnable;
    private boolean mReorderingStarted;
    protected int mRestorePage;
    protected LauncherScroller mScroller;
    int mSidePageHoverIndex;
    private Runnable mSidePageHoverRunnable;
    protected float mSmoothingTime;
    protected int[] mTempVisiblePagesRange;
    protected float mTotalMotionX;
    protected int mTouchSlop;
    protected int mTouchState;
    protected float mTouchX;
    private boolean mUseMinScale;
    private VelocityTracker mVelocityTracker;
    private Rect mViewport;
    protected boolean mWasInOverscroll;
    private static int REORDERING_DROP_REPOSITION_DURATION = 200;
    static int REORDERING_REORDER_REPOSITION_DURATION = 300;
    private static int REORDERING_SIDE_PAGE_HOVER_TIMEOUT = 80;
    private static final Matrix sTmpInvMatrix = new Matrix();
    private static final float[] sTmpPoint = new float[2];
    private static final int[] sTmpIntPoint = new int[2];
    private static final Rect sTmpRect = new Rect();
    private static final RectF sTmpRectF = new RectF();

    /* loaded from: a.zip:com/android/launcher3/PagedView$LayoutParams.class */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public boolean isFullScreenPage;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.isFullScreenPage = false;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.isFullScreenPage = false;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.isFullScreenPage = false;
        }
    }

    /* loaded from: a.zip:com/android/launcher3/PagedView$PageSwitchListener.class */
    public interface PageSwitchListener {
        void onPageSwitch(View view, int i);
    }

    /* loaded from: a.zip:com/android/launcher3/PagedView$SavedState.class */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.android.launcher3.PagedView.SavedState.1
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
        int currentPage;

        SavedState(Parcel parcel) {
            super(parcel);
            this.currentPage = -1;
            this.currentPage = parcel.readInt();
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.currentPage);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/PagedView$ScrollInterpolator.class */
    public static class ScrollInterpolator implements Interpolator {
        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float f2 = f - 1.0f;
            return (f2 * f2 * f2 * f2 * f2) + 1.0f;
        }
    }

    public PagedView(Context context) {
        this(context, null);
    }

    public PagedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PagedView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFreeScroll = false;
        this.mFreeScrollMinScrollX = -1;
        this.mFreeScrollMaxScrollX = -1;
        this.mFirstLayout = true;
        this.mRestorePage = -1001;
        this.mNextPage = -1;
        this.mPageSpacing = 0;
        this.mLastScreenCenter = -1;
        this.mTouchState = 0;
        this.mForceScreenScrolled = false;
        this.mAllowOverScroll = true;
        this.mTempVisiblePagesRange = new int[2];
        this.mActivePointerId = -1;
        this.mFadeInAdjacentScreens = false;
        this.mIsPageMoving = false;
        this.mWasInOverscroll = false;
        this.mViewport = new Rect();
        this.mMinScale = 1.0f;
        this.mUseMinScale = false;
        this.mSidePageHoverIndex = -1;
        this.mReorderingStarted = false;
        this.NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT = 2;
        this.mInsets = new Rect();
        this.mEdgeGlowLeft = new LauncherEdgeEffect();
        this.mEdgeGlowRight = new LauncherEdgeEffect();
        this.mInOverviewMode = false;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PagedView, i, 0);
        this.mPageLayoutWidthGap = obtainStyledAttributes.getDimensionPixelSize(0, 0);
        this.mPageLayoutHeightGap = obtainStyledAttributes.getDimensionPixelSize(1, 0);
        this.mPageIndicatorViewId = obtainStyledAttributes.getResourceId(2, -1);
        obtainStyledAttributes.recycle();
        setHapticFeedbackEnabled(false);
        this.mIsRtl = Utilities.isRtl(getResources());
        init();
    }

    private void abortScrollerAnimation(boolean z) {
        this.mScroller.abortAnimation();
        if (z) {
            this.mNextPage = -1;
        }
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
    }

    private void animateDragViewToOriginalPosition() {
        if (this.mDragView != null) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(REORDERING_DROP_REPOSITION_DURATION);
            animatorSet.playTogether(ObjectAnimator.ofFloat(this.mDragView, "translationX", 0.0f), ObjectAnimator.ofFloat(this.mDragView, "translationY", 0.0f), ObjectAnimator.ofFloat(this.mDragView, "scaleX", 1.0f), ObjectAnimator.ofFloat(this.mDragView, "scaleY", 1.0f));
            animatorSet.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.launcher3.PagedView.3
                final PagedView this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.this$0.onPostReorderingAnimationCompleted();
                }
            });
            animatorSet.start();
        }
    }

    private float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((float) ((f - 0.5f) * 0.4712389167638204d));
    }

    private void forceFinishScroller() {
        this.mScroller.forceFinished(true);
        this.mNextPage = -1;
    }

    private int getNearestHoverOverPageIndex() {
        if (this.mDragView != null) {
            int left = (int) (this.mDragView.getLeft() + (this.mDragView.getMeasuredWidth() / 2) + this.mDragView.getTranslationX());
            getFreeScrollPageRange(this.mTempVisiblePagesRange);
            int i = Integer.MAX_VALUE;
            int indexOfChild = indexOfChild(this.mDragView);
            int i2 = this.mTempVisiblePagesRange[0];
            while (i2 <= this.mTempVisiblePagesRange[1]) {
                View pageAt = getPageAt(i2);
                int abs = Math.abs(left - (pageAt.getLeft() + (pageAt.getMeasuredWidth() / 2)));
                int i3 = i;
                if (abs < i) {
                    indexOfChild = i2;
                    i3 = abs;
                }
                i2++;
                i = i3;
            }
            return indexOfChild;
        }
        return -1;
    }

    private boolean isTouchPointInViewportWithBuffer(int i, int i2) {
        sTmpRect.set(this.mViewport.left - (this.mViewport.width() / 2), this.mViewport.top, this.mViewport.right + (this.mViewport.width() / 2), this.mViewport.bottom);
        return sTmpRect.contains(i, i2);
    }

    private float[] mapPointFromParentToView(View view, float f, float f2) {
        sTmpPoint[0] = f - view.getLeft();
        sTmpPoint[1] = f2 - view.getTop();
        view.getMatrix().invert(sTmpInvMatrix);
        sTmpInvMatrix.mapPoints(sTmpPoint);
        return sTmpPoint;
    }

    private float[] mapPointFromViewToParent(View view, float f, float f2) {
        sTmpPoint[0] = f;
        sTmpPoint[1] = f2;
        view.getMatrix().mapPoints(sTmpPoint);
        float[] fArr = sTmpPoint;
        fArr[0] = fArr[0] + view.getLeft();
        float[] fArr2 = sTmpPoint;
        fArr2[1] = fArr2[1] + view.getTop();
        return sTmpPoint;
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int i = 0;
        int action = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(action) == this.mActivePointerId) {
            if (action == 0) {
                i = 1;
            }
            float x = motionEvent.getX(i);
            this.mDownMotionX = x;
            this.mLastMotionX = x;
            this.mLastMotionY = motionEvent.getY(i);
            this.mLastMotionXRemainder = 0.0f;
            this.mActivePointerId = motionEvent.getPointerId(i);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    private void releaseVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void removeMarkerForView(int i) {
        if (this.mPageIndicator == null || isReordering(false)) {
            return;
        }
        this.mPageIndicator.removeMarker(i, true);
    }

    private void resetTouchState() {
        releaseVelocityTracker();
        endReordering();
        this.mCancelTap = false;
        this.mTouchState = 0;
        this.mActivePointerId = -1;
        this.mEdgeGlowLeft.onRelease();
        this.mEdgeGlowRight.onRelease();
    }

    private void sendScrollAccessibilityEvent() {
        if (!((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled() || this.mCurrentPage == getNextPage()) {
            return;
        }
        AccessibilityEvent obtain = AccessibilityEvent.obtain(4096);
        obtain.setScrollable(true);
        obtain.setScrollX(getScrollX());
        obtain.setScrollY(getScrollY());
        obtain.setMaxScrollX(this.mMaxScrollX);
        obtain.setMaxScrollY(0);
        sendAccessibilityEventUnchecked(obtain);
    }

    private void setEnableFreeScroll(boolean z) {
        this.mFreeScroll = z;
        if (this.mFreeScroll) {
            updateFreescrollBounds();
            getFreeScrollPageRange(this.mTempVisiblePagesRange);
            if (getCurrentPage() < this.mTempVisiblePagesRange[0]) {
                setCurrentPage(this.mTempVisiblePagesRange[0]);
            } else if (getCurrentPage() > this.mTempVisiblePagesRange[1]) {
                setCurrentPage(this.mTempVisiblePagesRange[1]);
            }
        }
        setEnableOverscroll(!z);
    }

    private void updateDragViewTranslationDuringDrag() {
        if (this.mDragView != null) {
            float f = this.mLastMotionX;
            float f2 = this.mDownMotionX;
            float scrollX = getScrollX();
            float f3 = this.mDownScrollX;
            float f4 = this.mDragViewBaselineLeft;
            float left = this.mDragView.getLeft();
            float f5 = this.mLastMotionY;
            float f6 = this.mDownMotionY;
            this.mDragView.setTranslationX((f - f2) + (scrollX - f3) + (f4 - left));
            this.mDragView.setTranslationY(f5 - f6);
        }
    }

    private void updatePageIndicator() {
        if (this.mPageIndicator != null) {
            this.mPageIndicator.setContentDescription(getPageIndicatorDescription());
            if (isReordering(false) && this.mInOverviewMode) {
                return;
            }
            this.mPageIndicator.setActiveMarker(getNextPage());
        }
    }

    private int validateNewPage(int i) {
        int i2 = i;
        if (this.mFreeScroll) {
            getFreeScrollPageRange(this.mTempVisiblePagesRange);
            i2 = Math.max(this.mTempVisiblePagesRange[0], Math.min(i, this.mTempVisiblePagesRange[1]));
        }
        return Math.max(0, Math.min(i2, getPageCount() - 1));
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        if (this.mCurrentPage >= 0 && this.mCurrentPage < getPageCount()) {
            getPageAt(this.mCurrentPage).addFocusables(arrayList, i, i2);
        }
        if (i == 17) {
            if (this.mCurrentPage > 0) {
                getPageAt(this.mCurrentPage - 1).addFocusables(arrayList, i, i2);
            }
        } else if (i != 66 || this.mCurrentPage >= getPageCount() - 1) {
        } else {
            getPageAt(this.mCurrentPage + 1).addFocusables(arrayList, i, i2);
        }
    }

    public void addFullScreenPage(View view) {
        LayoutParams generateDefaultLayoutParams = generateDefaultLayoutParams();
        generateDefaultLayoutParams.isFullScreenPage = true;
        super.addView(view, 0, generateDefaultLayoutParams);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void cancelCurrentPageLongPress() {
        View pageAt = getPageAt(this.mCurrentPage);
        if (pageAt != null) {
            pageAt.cancelLongPress();
        }
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // android.view.View
    public void computeScroll() {
        computeScrollHelper();
    }

    protected boolean computeScrollHelper() {
        if (this.mScroller.computeScrollOffset()) {
            if (getScrollX() != this.mScroller.getCurrX() || getScrollY() != this.mScroller.getCurrY()) {
                scrollTo((int) (this.mScroller.getCurrX() * (1.0f / (this.mFreeScroll ? getScaleX() : 1.0f))), this.mScroller.getCurrY());
            }
            invalidate();
            return true;
        } else if (this.mNextPage != -1) {
            sendScrollAccessibilityEvent();
            this.mCurrentPage = validateNewPage(this.mNextPage);
            this.mNextPage = -1;
            notifyPageSwitchListener();
            if (this.mTouchState == 0) {
                pageEndMoving();
            }
            onPostReorderingAnimationCompleted();
            if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
                announceForAccessibility(getCurrentPageDescription());
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void dampedOverScroll(float f) {
        float viewportWidth = f / getViewportWidth();
        if (viewportWidth < 0.0f) {
            this.mEdgeGlowLeft.onPull(-viewportWidth);
        } else if (viewportWidth <= 0.0f) {
            return;
        } else {
            this.mEdgeGlowRight.onPull(viewportWidth);
        }
        invalidate();
    }

    protected void determineScrollingStart(MotionEvent motionEvent) {
        determineScrollingStart(motionEvent, 1.0f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void determineScrollingStart(MotionEvent motionEvent, float f) {
        int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
        if (findPointerIndex == -1) {
            LauncherLog.d("PagedView", "determineScrollingStart pointerIndex == -1.");
        } else if (isReordering(true)) {
            LauncherLog.d("PagedView", "determineScrollingStart isReordering == true.");
        } else {
            float x = motionEvent.getX(findPointerIndex);
            if (isTouchPointInViewportWithBuffer((int) x, (int) motionEvent.getY(findPointerIndex))) {
                if (((int) Math.abs(x - this.mLastMotionX)) > Math.round(((float) this.mTouchSlop) * f)) {
                    this.mTouchState = 1;
                    this.mTotalMotionX += Math.abs(this.mLastMotionX - x);
                    this.mLastMotionX = x;
                    this.mLastMotionXRemainder = 0.0f;
                    this.mTouchX = getViewportOffsetX() + getScrollX();
                    this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
                    onScrollInteractionBegin();
                    pageBeginMoving();
                }
            }
        }
    }

    public void disableFreeScroll() {
        setEnableFreeScroll(false);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int scrollX = getScrollX() + (getViewportWidth() / 2);
            LauncherHelper.beginSection("PagedView.dispatchDraw: mScrollX = " + getScrollX());
            LauncherHelper.endSection();
            if (LauncherLog.DEBUG_DRAW) {
                LauncherLog.d("PagedView", "dispatchDraw: mScrollX = " + getScrollX() + ", screenCenter = " + scrollX + ", mMaxScrollX = " + this.mMaxScrollX + ", mLastScreenCenter = " + this.mLastScreenCenter + ", mLeft = " + getLeft() + ", mRight = " + getRight() + ",mForceScreenScrolled = " + this.mForceScreenScrolled + ",getWidth() = " + getWidth() + ", pageCount = " + getChildCount() + ", this = " + this);
            }
            if (scrollX != this.mLastScreenCenter || this.mForceScreenScrolled) {
                this.mForceScreenScrolled = false;
                screenScrolled(scrollX);
                this.mLastScreenCenter = scrollX;
            }
            getVisiblePages(this.mTempVisiblePagesRange);
            int i = this.mTempVisiblePagesRange[0];
            int i2 = this.mTempVisiblePagesRange[1];
            if (i == -1 || i2 == -1) {
                return;
            }
            long drawingTime = getDrawingTime();
            canvas.save();
            canvas.clipRect(getScrollX(), getScrollY(), (getScrollX() + getRight()) - getLeft(), (getScrollY() + getBottom()) - getTop());
            while (true) {
                childCount--;
                if (childCount < 0) {
                    break;
                }
                View pageAt = getPageAt(childCount);
                if (pageAt != this.mDragView && i <= childCount && childCount <= i2 && shouldDrawChild(pageAt)) {
                    drawChild(canvas, pageAt, drawingTime);
                }
            }
            if (this.mDragView != null) {
                drawChild(canvas, this.mDragView, drawingTime);
            }
            canvas.restore();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        if (super.dispatchUnhandledMove(view, i)) {
            return true;
        }
        int i2 = i;
        if (this.mIsRtl) {
            if (i == 17) {
                i2 = 66;
            } else {
                i2 = i;
                if (i == 66) {
                    i2 = 17;
                }
            }
        }
        if (i2 == 17) {
            if (getCurrentPage() > 0) {
                snapToPage(getCurrentPage() - 1);
                return true;
            }
            return false;
        } else if (i2 != 66 || getCurrentPage() >= getPageCount() - 1) {
            return false;
        } else {
            snapToPage(getCurrentPage() + 1);
            return true;
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (getPageCount() > 0) {
            if (!this.mEdgeGlowLeft.isFinished()) {
                int save = canvas.save();
                Rect rect = this.mViewport;
                canvas.translate(rect.left, rect.top);
                canvas.rotate(270.0f);
                getEdgeVerticalPostion(sTmpIntPoint);
                canvas.translate(rect.top - sTmpIntPoint[1], 0.0f);
                this.mEdgeGlowLeft.setSize(sTmpIntPoint[1] - sTmpIntPoint[0], rect.width());
                if (this.mEdgeGlowLeft.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(save);
            }
            if (this.mEdgeGlowRight.isFinished()) {
                return;
            }
            int save2 = canvas.save();
            Rect rect2 = this.mViewport;
            canvas.translate(this.mPageScrolls[this.mIsRtl ? 0 : getPageCount() - 1] + rect2.left, rect2.top);
            canvas.rotate(90.0f);
            getEdgeVerticalPostion(sTmpIntPoint);
            canvas.translate(sTmpIntPoint[0] - rect2.top, -rect2.width());
            this.mEdgeGlowRight.setSize(sTmpIntPoint[1] - sTmpIntPoint[0], rect2.width());
            if (this.mEdgeGlowRight.draw(canvas)) {
                postInvalidateOnAnimation();
            }
            canvas.restoreToCount(save2);
        }
    }

    public void enableFreeScroll() {
        setEnableFreeScroll(true);
    }

    void endReordering() {
        if (this.mReorderingStarted) {
            this.mReorderingStarted = false;
            this.mPostReorderingPreZoomInRunnable = new Runnable(this, new Runnable(this) { // from class: com.android.launcher3.PagedView.4
                final PagedView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.onEndReordering();
                }
            }) { // from class: com.android.launcher3.PagedView.5
                final PagedView this$0;
                final Runnable val$onCompleteRunnable;

                {
                    this.this$0 = this;
                    this.val$onCompleteRunnable = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$onCompleteRunnable.run();
                    if (this.this$0.mInOverviewMode) {
                        this.this$0.enableFreeScroll();
                    } else {
                        this.this$0.disableFreeScroll();
                    }
                }
            };
            this.mPostReorderingPreZoomInRemainingAnimationCount = this.NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT;
            snapToPage(indexOfChild(this.mDragView), 0);
            animateDragViewToOriginalPosition();
            this.mDragView = null;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void focusableViewAvailable(View view) {
        View pageAt = getPageAt(this.mCurrentPage);
        View view2 = view;
        while (true) {
            View view3 = view2;
            if (view3 == pageAt) {
                super.focusableViewAvailable(view);
                return;
            } else if (view3 == this || !(view3.getParent() instanceof View)) {
                return;
            } else {
                view2 = (View) view3.getParent();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    protected int getChildGap() {
        return 0;
    }

    protected int getChildOffset(int i) {
        if (i < 0 || i > getChildCount() - 1) {
            return 0;
        }
        return getPageAt(i).getLeft() - getViewportOffsetX();
    }

    public int getCurrentPage() {
        return this.mCurrentPage;
    }

    protected String getCurrentPageDescription() {
        return String.format(getContext().getString(2131558436), Integer.valueOf(getNextPage() + 1), Integer.valueOf(getChildCount()));
    }

    protected abstract void getEdgeVerticalPostion(int[] iArr);

    protected void getFreeScrollPageRange(int[] iArr) {
        iArr[0] = 0;
        iArr[1] = Math.max(0, getChildCount() - 1);
    }

    public int getLayoutTransitionOffsetForPage(int i) {
        if (this.mPageScrolls == null || i >= this.mPageScrolls.length || i < 0) {
            return 0;
        }
        View childAt = getChildAt(i);
        int i2 = 0;
        if (!((LayoutParams) childAt.getLayoutParams()).isFullScreenPage) {
            i2 = this.mIsRtl ? getPaddingRight() : getPaddingLeft();
        }
        return (int) (childAt.getX() - ((this.mPageScrolls[i] + i2) + getViewportOffsetX()));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getNextPage() {
        return this.mNextPage != -1 ? this.mNextPage : this.mCurrentPage;
    }

    public int getNormalChildHeight() {
        return this.mNormalChildHeight;
    }

    public View getPageAt(int i) {
        return getChildAt(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getPageCount() {
        return getChildCount();
    }

    public int getPageForView(View view) {
        if (view != null) {
            ViewParent parent = view.getParent();
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (parent == getPageAt(i)) {
                    return i;
                }
            }
            return -1;
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PageIndicator getPageIndicator() {
        return this.mPageIndicator;
    }

    protected View.OnClickListener getPageIndicatorClickListener() {
        return null;
    }

    protected String getPageIndicatorDescription() {
        return getCurrentPageDescription();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public PageIndicator.PageMarkerResources getPageIndicatorMarker(int i) {
        return new PageIndicator.PageMarkerResources();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getPageNearestToCenterOfScreen() {
        int i = Integer.MAX_VALUE;
        int i2 = -1;
        int viewportOffsetX = getViewportOffsetX();
        int scrollX = getScrollX();
        int viewportWidth = getViewportWidth() / 2;
        int childCount = getChildCount();
        int i3 = 0;
        while (i3 < childCount) {
            int abs = Math.abs(((getViewportOffsetX() + getChildOffset(i3)) + (getPageAt(i3).getMeasuredWidth() / 2)) - ((viewportOffsetX + scrollX) + viewportWidth));
            int i4 = i;
            if (abs < i) {
                i4 = abs;
                i2 = i3;
            }
            i3++;
            i = i4;
        }
        return i2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Matrix getPageShiftMatrix() {
        return getMatrix();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getRestorePage() {
        return this.mRestorePage;
    }

    public int getScrollForPage(int i) {
        if (this.mPageScrolls == null || i >= this.mPageScrolls.length || i < 0) {
            return 0;
        }
        return this.mPageScrolls[i];
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x003c, code lost:
        if (r4.mIsRtl != false) goto L5;
     */
    /* JADX WARN: Removed duplicated region for block: B:18:0x006e  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public float getScrollProgress(int i, View view, int i2) {
        int i3;
        int viewportWidth = getViewportWidth() / 2;
        int scrollForPage = i - (getScrollForPage(i2) + viewportWidth);
        int childCount = getChildCount();
        int i4 = i2 + 1;
        if (scrollForPage >= 0 || this.mIsRtl) {
            i3 = i4;
            if (scrollForPage > 0) {
                i3 = i4;
            }
            int measuredWidth = (i3 >= 0 || i3 > childCount - 1) ? view.getMeasuredWidth() + this.mPageSpacing : Math.abs(getScrollForPage(i3) - getScrollForPage(i2));
            float f = scrollForPage / (measuredWidth * 1.0f);
            if (LauncherLog.DEBUG_DRAW) {
                LauncherLog.d("PagedView", "getScrollProgress: screenCenter = " + i + ", page = " + i2 + ", v = " + view + ",totalDistance = " + measuredWidth + ", mPageSpacing = " + this.mPageSpacing + ", delta = " + scrollForPage + ", halfScreenSize = " + viewportWidth + ", scrollProgress = " + f);
            }
            return Math.max(Math.min(f, 1.0f), -1.0f);
        }
        i3 = i2 - 1;
        if (i3 >= 0) {
        }
        float f2 = scrollForPage / (measuredWidth * 1.0f);
        if (LauncherLog.DEBUG_DRAW) {
        }
        return Math.max(Math.min(f2, 1.0f), -1.0f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getUnboundedScrollX() {
        return getScrollX();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getViewportHeight() {
        return this.mViewport.height();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getViewportOffsetX() {
        return (getMeasuredWidth() - getViewportWidth()) / 2;
    }

    int getViewportOffsetY() {
        return (getMeasuredHeight() - getViewportHeight()) / 2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getViewportWidth() {
        return this.mViewport.width();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void getVisiblePages(int[] iArr) {
        int childCount = getChildCount();
        iArr[0] = -1;
        iArr[1] = -1;
        if (childCount <= 0) {
            iArr[0] = -1;
            iArr[1] = -1;
            return;
        }
        int i = -getLeft();
        int viewportWidth = getViewportWidth();
        Matrix pageShiftMatrix = getPageShiftMatrix();
        int i2 = 0;
        for (int i3 = 0; i3 < childCount; i3++) {
            View pageAt = getPageAt(i3);
            sTmpRectF.left = 0.0f;
            sTmpRectF.right = pageAt.getMeasuredWidth();
            pageAt.getMatrix().mapRect(sTmpRectF);
            sTmpRectF.offset(pageAt.getLeft() - getScrollX(), 0.0f);
            pageShiftMatrix.mapRect(sTmpRectF);
            if (sTmpRectF.left <= i + viewportWidth && sTmpRectF.right >= i) {
                int i4 = i3;
                i2 = i4;
                if (iArr[0] < 0) {
                    iArr[0] = i4;
                    i2 = i4;
                }
            } else if (iArr[0] != -1) {
                break;
            }
        }
        iArr[1] = i2;
    }

    protected int indexToPage(int i) {
        return i;
    }

    protected void init() {
        this.mScroller = new LauncherScroller(getContext());
        setDefaultInterpolator(new ScrollInterpolator());
        this.mCurrentPage = 0;
        this.mCenterPagesVertically = true;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        this.mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mFlingThresholdVelocity = (int) (this.mDensity * 500.0f);
        this.mMinFlingVelocity = (int) (this.mDensity * 250.0f);
        this.mMinSnapVelocity = (int) (this.mDensity * 1500.0f);
        setOnHierarchyChangeListener(this);
        setWillNotDraw(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isPageMoving() {
        return this.mIsPageMoving;
    }

    boolean isReordering(boolean z) {
        boolean z2 = this.mIsReordering;
        boolean z3 = z2;
        if (z) {
            z3 = z2 & (this.mTouchState == 4);
        }
        return z3;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyPageSwitchListener() {
        if (this.mPageSwitchListener != null) {
            this.mPageSwitchListener.onPageSwitch(getPageAt(getNextPage()), getNextPage());
        }
        updatePageIndicator();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getParent()).getParent();
        if (this.mPageIndicator != null || this.mPageIndicatorViewId <= -1) {
            return;
        }
        this.mPageIndicator = (PageIndicator) viewGroup.findViewById(this.mPageIndicatorViewId);
        this.mPageIndicator.removeAllMarkers(true);
        ArrayList<PageIndicator.PageMarkerResources> arrayList = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            arrayList.add(getPageIndicatorMarker(i));
        }
        this.mPageIndicator.addMarkers(arrayList, true);
        View.OnClickListener pageIndicatorClickListener = getPageIndicatorClickListener();
        if (pageIndicatorClickListener != null) {
            this.mPageIndicator.setOnClickListener(pageIndicatorClickListener);
        }
        this.mPageIndicator.setContentDescription(getPageIndicatorDescription());
    }

    @Override // android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewAdded(View view, View view2) {
        if (this.mPageIndicator != null && !isReordering(false)) {
            int indexOfChild = indexOfChild(view2);
            this.mPageIndicator.addMarker(indexOfChild, getPageIndicatorMarker(indexOfChild), true);
        }
        this.mForceScreenScrolled = true;
        updateFreescrollBounds();
        invalidate();
    }

    @Override // android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewRemoved(View view, View view2) {
        this.mForceScreenScrolled = true;
        updateFreescrollBounds();
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPageIndicator = null;
    }

    public void onEndReordering() {
        this.mIsReordering = false;
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        float f;
        float axisValue;
        boolean z = false;
        if ((motionEvent.getSource() & 2) != 0) {
            switch (motionEvent.getAction()) {
                case 8:
                    if ((motionEvent.getMetaState() & 1) != 0) {
                        f = 0.0f;
                        axisValue = motionEvent.getAxisValue(9);
                    } else {
                        f = -motionEvent.getAxisValue(9);
                        axisValue = motionEvent.getAxisValue(10);
                    }
                    if (axisValue != 0.0f || f != 0.0f) {
                        if (this.mIsRtl) {
                            if (axisValue < 0.0f || f < 0.0f) {
                                z = true;
                            }
                        } else if (axisValue > 0.0f || f > 0.0f) {
                            z = true;
                        }
                        if (z) {
                            scrollRight();
                            return true;
                        }
                        scrollLeft();
                        return true;
                    }
                    break;
            }
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        return true;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        boolean z = true;
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (getPageCount() <= 1) {
            z = false;
        }
        accessibilityEvent.setScrollable(z);
    }

    @Override // android.view.View
    @TargetApi(21)
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        boolean z = true;
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (getPageCount() <= 1) {
            z = false;
        }
        accessibilityNodeInfo.setScrollable(z);
        if (getCurrentPage() < getPageCount() - 1) {
            accessibilityNodeInfo.addAction(4096);
        }
        if (getCurrentPage() > 0) {
            accessibilityNodeInfo.addAction(8192);
        }
        accessibilityNodeInfo.setClassName(getClass().getName());
        accessibilityNodeInfo.setLongClickable(false);
        if (Utilities.ATLEAST_LOLLIPOP) {
            accessibilityNodeInfo.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK);
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("PagedView", "onInterceptTouchEvent: ev = " + motionEvent + ", mScrollX = " + getScrollX() + ", this = " + this);
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        if (getChildCount() <= 0) {
            LauncherLog.d("PagedView", "There are no pages to swipe, page count = " + getChildCount());
            return super.onInterceptTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action == 2 && this.mTouchState == 1) {
            LauncherLog.d("PagedView", "onInterceptTouchEvent: touch move during scrolling.");
            return true;
        }
        switch (action & 255) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                this.mDownMotionX = x;
                this.mDownMotionY = y;
                this.mDownScrollX = getScrollX();
                this.mLastMotionX = x;
                this.mLastX = this.mLastMotionX;
                this.mLastMotionY = y;
                float[] mapPointFromViewToParent = mapPointFromViewToParent(this, x, y);
                this.mParentDownMotionX = mapPointFromViewToParent[0];
                this.mParentDownMotionY = mapPointFromViewToParent[1];
                this.mLastMotionXRemainder = 0.0f;
                this.mTotalMotionX = 0.0f;
                this.mActivePointerId = motionEvent.getPointerId(0);
                int abs = Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX());
                boolean z = this.mScroller.isFinished() || abs < this.mTouchSlop / 3;
                if (z) {
                    this.mTouchState = 0;
                    if (!this.mScroller.isFinished() && !this.mFreeScroll) {
                        setCurrentPage(getNextPage());
                        pageEndMoving();
                    }
                } else if (isTouchPointInViewportWithBuffer((int) this.mDownMotionX, (int) this.mDownMotionY)) {
                    this.mTouchState = 1;
                } else {
                    this.mTouchState = 0;
                }
                if (LauncherLog.DEBUG) {
                    LauncherLog.d("PagedView", "onInterceptTouchEvent touch down: finishedScrolling = " + z + ", mScrollX = " + getScrollX() + ", xDist = " + abs + ", mTouchState = " + this.mTouchState + ", this = " + this);
                    break;
                }
                break;
            case 1:
            case 3:
                resetTouchState();
                break;
            case 2:
                if (this.mActivePointerId != -1) {
                    determineScrollingStart(motionEvent);
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex != -1) {
                        float x2 = motionEvent.getX(findPointerIndex);
                        float f = (this.mLastX + this.mLastMotionXRemainder) - x2;
                        this.mLastX = x2;
                        if (this.mTouchState == 1) {
                            this.mTotalMotionX += Math.abs(f);
                            if (Math.abs(f) >= 1.0f) {
                                this.mTouchX += f;
                                this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
                                scrollBy((int) f, 0);
                                this.mLastMotionX = x2;
                                this.mLastMotionXRemainder = f - ((int) f);
                            } else {
                                awakenScrollBars();
                            }
                            if (LauncherLog.DEBUG) {
                                LauncherLog.d("PagedView", "onInterceptTouchEvent, Touch move scroll: x = " + x2 + ", deltaX = " + f + ", mTotalMotionX = " + this.mTotalMotionX + ", mLastMotionX = " + this.mLastMotionX + ", mCurrentPage = " + this.mCurrentPage + ",mTouchX = " + this.mTouchX + " ,mLastMotionX = " + this.mLastMotionX + ", mScrollX = " + getScrollX());
                                break;
                            }
                        }
                    } else {
                        return true;
                    }
                }
                break;
            case 6:
                onSecondaryPointerUp(motionEvent);
                releaseVelocityTracker();
                break;
        }
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("PagedView", "onInterceptTouchEvent: return = " + (this.mTouchState != 0));
        }
        return this.mTouchState != 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        if (getChildCount() == 0) {
            return;
        }
        int childCount = getChildCount();
        int viewportOffsetX = getViewportOffsetX();
        int viewportOffsetY = getViewportOffsetY();
        this.mViewport.offset(viewportOffsetX, viewportOffsetY);
        int i7 = this.mIsRtl ? childCount - 1 : 0;
        int i8 = this.mIsRtl ? -1 : childCount;
        int i9 = this.mIsRtl ? -1 : 1;
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = viewportOffsetX + (((LayoutParams) getChildAt(i7).getLayoutParams()).isFullScreenPage ? 0 : getPaddingLeft());
        if (this.mPageScrolls == null || childCount != this.mChildCountOnLastLayout) {
            this.mPageScrolls = new int[childCount];
        }
        int i10 = i7;
        while (i10 != i8) {
            View pageAt = getPageAt(i10);
            int i11 = paddingLeft;
            if (pageAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) pageAt.getLayoutParams();
                if (layoutParams.isFullScreenPage) {
                    i5 = viewportOffsetY;
                } else {
                    int paddingTop2 = getPaddingTop() + viewportOffsetY + this.mInsets.top;
                    i5 = paddingTop2;
                    if (this.mCenterPagesVertically) {
                        i5 = paddingTop2 + (((((getViewportHeight() - this.mInsets.top) - this.mInsets.bottom) - (paddingTop + paddingBottom)) - pageAt.getMeasuredHeight()) / 2);
                    }
                }
                int measuredWidth = pageAt.getMeasuredWidth();
                pageAt.layout(paddingLeft, i5, pageAt.getMeasuredWidth() + paddingLeft, i5 + pageAt.getMeasuredHeight());
                this.mPageScrolls[i10] = (paddingLeft - (layoutParams.isFullScreenPage ? 0 : getPaddingLeft())) - viewportOffsetX;
                int i12 = this.mPageSpacing;
                int i13 = i10 + i9;
                LayoutParams layoutParams2 = i13 != i8 ? (LayoutParams) getPageAt(i13).getLayoutParams() : null;
                if (layoutParams.isFullScreenPage) {
                    i6 = getPaddingLeft();
                } else {
                    i6 = i12;
                    if (layoutParams2 != null) {
                        i6 = i12;
                        if (layoutParams2.isFullScreenPage) {
                            i6 = getPaddingRight();
                        }
                    }
                }
                i11 = paddingLeft + measuredWidth + i6 + getChildGap();
            }
            i10 += i9;
            paddingLeft = i11;
        }
        LayoutTransition layoutTransition = getLayoutTransition();
        if (layoutTransition == null || !layoutTransition.isRunning()) {
            updateMaxScrollX();
        } else {
            layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener(this) { // from class: com.android.launcher3.PagedView.1
                final PagedView this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.LayoutTransition.TransitionListener
                public void endTransition(LayoutTransition layoutTransition2, ViewGroup viewGroup, View view, int i14) {
                    if (layoutTransition2.isRunning()) {
                        return;
                    }
                    layoutTransition2.removeTransitionListener(this);
                    this.this$0.updateMaxScrollX();
                }

                @Override // android.animation.LayoutTransition.TransitionListener
                public void startTransition(LayoutTransition layoutTransition2, ViewGroup viewGroup, View view, int i14) {
                }
            });
        }
        if (this.mFirstLayout && this.mCurrentPage >= 0 && this.mCurrentPage < childCount) {
            updateCurrentPageScroll();
            this.mFirstLayout = false;
        }
        if (this.mScroller.isFinished() && this.mChildCountOnLastLayout != childCount) {
            if (this.mRestorePage != -1001) {
                setCurrentPage(this.mRestorePage);
                this.mRestorePage = -1001;
            } else {
                setCurrentPage(getNextPage());
            }
        }
        this.mChildCountOnLastLayout = childCount;
        if (isReordering(true)) {
            updateDragViewTranslationDuringDrag();
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        int viewportWidth;
        int viewportHeight;
        if (getChildCount() == 0) {
            super.onMeasure(i, i2);
            return;
        }
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size2 = View.MeasureSpec.getSize(i2);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int max = Math.max(displayMetrics.widthPixels + this.mInsets.left + this.mInsets.right, displayMetrics.heightPixels + this.mInsets.top + this.mInsets.bottom);
        int i7 = (int) (max * 2.0f);
        int i8 = (int) (max * 2.0f);
        if (this.mUseMinScale) {
            i3 = (int) (i7 / this.mMinScale);
            i4 = (int) (i8 / this.mMinScale);
        } else {
            i3 = size;
            i4 = size2;
        }
        this.mViewport.set(0, 0, size, size2);
        if (mode == 0 || mode2 == 0) {
            super.onMeasure(i, i2);
        } else if (size <= 0 || size2 <= 0) {
            super.onMeasure(i, i2);
        } else {
            int paddingTop = getPaddingTop();
            int paddingBottom = getPaddingBottom();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int i9 = 0;
            int childCount = getChildCount();
            int i10 = 0;
            while (i10 < childCount) {
                View pageAt = getPageAt(i10);
                int i11 = i9;
                if (pageAt.getVisibility() != 8) {
                    LayoutParams layoutParams = (LayoutParams) pageAt.getLayoutParams();
                    if (layoutParams.isFullScreenPage) {
                        i5 = 1073741824;
                        i6 = 1073741824;
                        viewportWidth = getViewportWidth();
                        viewportHeight = getViewportHeight();
                    } else {
                        int i12 = layoutParams.width == -2 ? Integer.MIN_VALUE : 1073741824;
                        int i13 = layoutParams.height == -2 ? Integer.MIN_VALUE : 1073741824;
                        int viewportWidth2 = ((getViewportWidth() - (paddingLeft + paddingRight)) - this.mInsets.left) - this.mInsets.right;
                        viewportHeight = ((getViewportHeight() - (paddingTop + paddingBottom)) - this.mInsets.top) - this.mInsets.bottom;
                        this.mNormalChildHeight = viewportHeight;
                        i5 = i12;
                        viewportWidth = viewportWidth2;
                        i6 = i13;
                    }
                    i11 = i9;
                    if (i9 == 0) {
                        i11 = viewportWidth;
                    }
                    int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(viewportWidth, i5);
                    int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(viewportHeight, i6);
                    if (LauncherLog.DEBUG_LAYOUT) {
                        LauncherLog.d("PagedView", "measure-child " + i10 + ": child = " + pageAt + ", childWidthMode = " + i5 + ", childHeightMode = " + i6 + ", this = " + this);
                    }
                    pageAt.measure(makeMeasureSpec, makeMeasureSpec2);
                }
                i10++;
                i9 = i11;
            }
            setMeasuredDimension(i3, i4);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onPageBeginMoving() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onPageEndMoving() {
        this.mWasInOverscroll = false;
    }

    void onPostReorderingAnimationCompleted() {
        this.mPostReorderingPreZoomInRemainingAnimationCount--;
        if (this.mPostReorderingPreZoomInRunnable == null || this.mPostReorderingPreZoomInRemainingAnimationCount != 0) {
            return;
        }
        this.mPostReorderingPreZoomInRunnable.run();
        this.mPostReorderingPreZoomInRunnable = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        View pageAt = getPageAt(this.mNextPage != -1 ? this.mNextPage : this.mCurrentPage);
        if (pageAt != null) {
            return pageAt.requestFocus(i, rect);
        }
        return false;
    }

    protected void onScrollInteractionBegin() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onScrollInteractionEnd() {
    }

    public void onStartReordering() {
        this.mTouchState = 4;
        this.mIsReordering = true;
        invalidate();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("PagedView", "onTouchEvent: ev = " + motionEvent + ", mScrollX = " + getScrollX() + ", this = " + this);
        }
        super.onTouchEvent(motionEvent);
        if (getChildCount() <= 0) {
            return super.onTouchEvent(motionEvent);
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        switch (motionEvent.getAction() & 255) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                if (!this.mScroller.isFinished()) {
                    abortScrollerAnimation(false);
                }
                float x = motionEvent.getX();
                this.mLastMotionX = x;
                this.mDownMotionX = x;
                this.mLastX = this.mLastMotionX;
                float y = motionEvent.getY();
                this.mLastMotionY = y;
                this.mDownMotionY = y;
                this.mDownScrollX = getScrollX();
                float[] mapPointFromViewToParent = mapPointFromViewToParent(this, this.mLastMotionX, this.mLastMotionY);
                this.mParentDownMotionX = mapPointFromViewToParent[0];
                this.mParentDownMotionY = mapPointFromViewToParent[1];
                this.mLastMotionXRemainder = 0.0f;
                this.mTotalMotionX = 0.0f;
                this.mActivePointerId = motionEvent.getPointerId(0);
                if (LauncherLog.DEBUG) {
                    LauncherLog.d("PagedView", "Touch down: mDownMotionX = " + this.mDownMotionX + ", mTouchState = " + this.mTouchState + ", mCurrentPage = " + this.mCurrentPage + ", mScrollX = " + getScrollX() + ", this = " + this);
                }
                if (this.mTouchState == 1) {
                    onScrollInteractionBegin();
                    pageBeginMoving();
                    return true;
                }
                return true;
            case 1:
                if (this.mTouchState == 1) {
                    if (this.mActivePointerId == -1) {
                        if (LauncherLog.DEBUG) {
                            LauncherLog.w("PagedView", "Touch up scroll: mActivePointerId = " + this.mActivePointerId);
                            return true;
                        }
                        return true;
                    }
                    int i = this.mActivePointerId;
                    float x2 = motionEvent.getX(motionEvent.findPointerIndex(i));
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                    int xVelocity = (int) velocityTracker.getXVelocity(i);
                    int i2 = (int) (x2 - this.mDownMotionX);
                    int measuredWidth = getPageAt(this.mCurrentPage).getMeasuredWidth();
                    boolean z = ((float) Math.abs(i2)) > ((float) measuredWidth) * 0.4f;
                    this.mTotalMotionX += Math.abs((this.mLastMotionX + this.mLastMotionXRemainder) - x2);
                    boolean z2 = this.mTotalMotionX > 25.0f ? Math.abs(xVelocity) > this.mFlingThresholdVelocity : false;
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("PagedView", "Touch up scroll: x = " + x2 + ", deltaX = " + i2 + ", mTotalMotionX = " + this.mTotalMotionX + ", mLastMotionX = " + this.mLastMotionX + ", velocityX = " + xVelocity + ", mCurrentPage = " + this.mCurrentPage + ", pageWidth = " + measuredWidth + ", isFling = " + z2 + ", isSignificantMove = " + z + ", mScrollX = " + getScrollX());
                    }
                    if (this.mFreeScroll) {
                        if (!this.mScroller.isFinished()) {
                            abortScrollerAnimation(true);
                        }
                        float scaleX = getScaleX();
                        int i3 = (int) ((-xVelocity) * scaleX);
                        this.mScroller.setInterpolator(this.mDefaultInterpolator);
                        this.mScroller.fling((int) (getScrollX() * scaleX), getScrollY(), i3, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                        invalidate();
                    } else {
                        boolean z3 = false;
                        if (Math.abs(i2) > measuredWidth * 0.33f) {
                            z3 = false;
                            if (Math.signum(xVelocity) != Math.signum(i2)) {
                                z3 = false;
                                if (z2) {
                                    if (LauncherLog.DEBUG) {
                                        LauncherLog.d("PagedView", "Return to origin page: deltaX = " + i2 + ", velocityX = " + xVelocity + ", isFling = " + z2);
                                    }
                                    z3 = true;
                                }
                            }
                        }
                        boolean z4 = !this.mIsRtl ? i2 >= 0 : i2 <= 0;
                        boolean z5 = !this.mIsRtl ? xVelocity >= 0 : xVelocity <= 0;
                        if (((z && !z4 && !z2) || (z2 && !z5)) && this.mCurrentPage > 0) {
                            snapToPageWithVelocity(z3 ? this.mCurrentPage : this.mCurrentPage - 1, xVelocity);
                        } else if (!((z && z4 && !z2) || (z2 && z5)) || this.mCurrentPage >= getChildCount() - 1) {
                            snapToDestination();
                        } else {
                            snapToPageWithVelocity(z3 ? this.mCurrentPage : this.mCurrentPage + 1, xVelocity);
                        }
                    }
                    onScrollInteractionEnd();
                } else if (this.mTouchState == 2) {
                    int max = Math.max(0, this.mCurrentPage - 1);
                    if (max != this.mCurrentPage) {
                        snapToPage(max);
                    } else {
                        snapToDestination();
                    }
                } else if (this.mTouchState == 3) {
                    int min = Math.min(getChildCount() - 1, this.mCurrentPage + 1);
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("PagedView", "TOUCH_STATE_NEXT_PAGE: mCurrentPage = " + this.mCurrentPage + ", nextPage = " + min + ", this = " + this);
                    }
                    if (min != this.mCurrentPage) {
                        snapToPage(min);
                    } else {
                        snapToDestination();
                    }
                } else if (this.mTouchState == 4) {
                    this.mLastMotionX = motionEvent.getX();
                    this.mLastMotionY = motionEvent.getY();
                    float[] mapPointFromViewToParent2 = mapPointFromViewToParent(this, this.mLastMotionX, this.mLastMotionY);
                    this.mParentDownMotionX = mapPointFromViewToParent2[0];
                    this.mParentDownMotionY = mapPointFromViewToParent2[1];
                    updateDragViewTranslationDuringDrag();
                } else {
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("PagedView", "[--Case Watcher--]Touch up unhandled: mCurrentPage = " + this.mCurrentPage + ", mTouchState = " + this.mTouchState + ", mScrollX = " + getScrollX() + ", this = " + this);
                    }
                    if (!this.mCancelTap) {
                        onUnhandledTap(motionEvent);
                    }
                }
                removeCallbacks(this.mSidePageHoverRunnable);
                resetTouchState();
                return true;
            case 2:
                if (this.mTouchState == 1) {
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex == -1) {
                        return true;
                    }
                    float x3 = motionEvent.getX(findPointerIndex);
                    float f = (this.mLastMotionX + this.mLastMotionXRemainder) - x3;
                    this.mTotalMotionX += Math.abs(f);
                    if (Math.abs(f) >= 1.0f) {
                        this.mTouchX += f;
                        this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
                        scrollBy((int) f, 0);
                        this.mLastMotionX = x3;
                        this.mLastMotionXRemainder = f - ((int) f);
                    } else {
                        awakenScrollBars();
                    }
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("PagedView", "Touch move scroll: x = " + x3 + ", deltaX = " + f + ", mTotalMotionX = " + this.mTotalMotionX + ", mLastMotionX = " + this.mLastMotionX + ", mCurrentPage = " + this.mCurrentPage + ",mTouchX = " + this.mTouchX + " ,mLastMotionX = " + this.mLastMotionX + ", mScrollX = " + getScrollX());
                        return true;
                    }
                    return true;
                } else if (this.mTouchState != 4) {
                    determineScrollingStart(motionEvent);
                    return true;
                } else {
                    this.mLastMotionX = motionEvent.getX();
                    this.mLastMotionY = motionEvent.getY();
                    float[] mapPointFromViewToParent3 = mapPointFromViewToParent(this, this.mLastMotionX, this.mLastMotionY);
                    this.mParentDownMotionX = mapPointFromViewToParent3[0];
                    this.mParentDownMotionY = mapPointFromViewToParent3[1];
                    updateDragViewTranslationDuringDrag();
                    int indexOfChild = indexOfChild(this.mDragView);
                    int nearestHoverOverPageIndex = getNearestHoverOverPageIndex();
                    if (nearestHoverOverPageIndex <= -1 || nearestHoverOverPageIndex == indexOfChild(this.mDragView)) {
                        removeCallbacks(this.mSidePageHoverRunnable);
                        this.mSidePageHoverIndex = -1;
                        return true;
                    }
                    this.mTempVisiblePagesRange[0] = 0;
                    this.mTempVisiblePagesRange[1] = getPageCount() - 1;
                    getFreeScrollPageRange(this.mTempVisiblePagesRange);
                    if (this.mTempVisiblePagesRange[0] > nearestHoverOverPageIndex || nearestHoverOverPageIndex > this.mTempVisiblePagesRange[1] || nearestHoverOverPageIndex == this.mSidePageHoverIndex || !this.mScroller.isFinished()) {
                        return true;
                    }
                    this.mSidePageHoverIndex = nearestHoverOverPageIndex;
                    this.mSidePageHoverRunnable = new Runnable(this, nearestHoverOverPageIndex, indexOfChild) { // from class: com.android.launcher3.PagedView.2
                        final PagedView this$0;
                        final int val$dragViewIndex;
                        final int val$pageUnderPointIndex;

                        {
                            this.this$0 = this;
                            this.val$pageUnderPointIndex = nearestHoverOverPageIndex;
                            this.val$dragViewIndex = indexOfChild;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            this.this$0.snapToPage(this.val$pageUnderPointIndex);
                            int i4 = this.val$dragViewIndex < this.val$pageUnderPointIndex ? -1 : 1;
                            int i5 = this.val$dragViewIndex > this.val$pageUnderPointIndex ? this.val$dragViewIndex - 1 : this.val$pageUnderPointIndex;
                            for (int i6 = this.val$dragViewIndex < this.val$pageUnderPointIndex ? this.val$dragViewIndex + 1 : this.val$pageUnderPointIndex; i6 <= i5; i6++) {
                                View childAt = this.this$0.getChildAt(i6);
                                int viewportOffsetX = this.this$0.getViewportOffsetX();
                                int childOffset = this.this$0.getChildOffset(i6);
                                int viewportOffsetX2 = this.this$0.getViewportOffsetX();
                                int childOffset2 = this.this$0.getChildOffset(i6 + i4);
                                AnimatorSet animatorSet = (AnimatorSet) childAt.getTag(100);
                                if (animatorSet != null) {
                                    animatorSet.cancel();
                                }
                                childAt.setTranslationX((viewportOffsetX + childOffset) - (viewportOffsetX2 + childOffset2));
                                AnimatorSet animatorSet2 = new AnimatorSet();
                                animatorSet2.setDuration(PagedView.REORDERING_REORDER_REPOSITION_DURATION);
                                animatorSet2.playTogether(ObjectAnimator.ofFloat(childAt, "translationX", 0.0f));
                                animatorSet2.start();
                                childAt.setTag(animatorSet2);
                            }
                            this.this$0.removeView(this.this$0.mDragView);
                            this.this$0.addView(this.this$0.mDragView, this.val$pageUnderPointIndex);
                            this.this$0.mSidePageHoverIndex = -1;
                            if (this.this$0.mPageIndicator != null) {
                                this.this$0.mPageIndicator.setActiveMarker(this.this$0.getNextPage());
                            }
                        }
                    };
                    postDelayed(this.mSidePageHoverRunnable, REORDERING_SIDE_PAGE_HOVER_TIMEOUT);
                    return true;
                }
            case 3:
                if (LauncherLog.DEBUG) {
                    LauncherLog.d("PagedView", "Touch cancel: mCurrentPage = " + this.mCurrentPage + ", mTouchState = " + this.mTouchState + ", mScrollX = , this = " + this);
                }
                if (this.mTouchState == 1) {
                    snapToDestination();
                }
                resetTouchState();
                return true;
            case 4:
            case 5:
            default:
                return true;
            case 6:
                if (LauncherLog.DEBUG) {
                    LauncherLog.d("PagedView", "Touch ACTION_POINTER_UP: mCurrentPage = " + this.mCurrentPage + ", mTouchState = " + this.mTouchState + ", mActivePointerId = " + this.mActivePointerId + ", this = " + this);
                }
                onSecondaryPointerUp(motionEvent);
                releaseVelocityTracker();
                return true;
        }
    }

    protected void onUnhandledTap(MotionEvent motionEvent) {
        ((Launcher) getContext()).onClick(this);
    }

    protected void overScroll(float f) {
        dampedOverScroll(f);
    }

    protected void pageBeginMoving() {
        if (this.mIsPageMoving) {
            return;
        }
        LauncherHelper.beginSection("PagedView.pageBeginMoving");
        this.mIsPageMoving = true;
        onPageBeginMoving();
        LauncherHelper.endSection();
    }

    protected void pageEndMoving() {
        if (this.mIsPageMoving) {
            this.mIsPageMoving = false;
            onPageEndMoving();
        }
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (super.performAccessibilityAction(i, bundle)) {
            return true;
        }
        switch (i) {
            case 4096:
                if (getCurrentPage() < getPageCount() - 1) {
                    scrollRight();
                    return true;
                }
                return false;
            case 8192:
                if (getCurrentPage() > 0) {
                    scrollLeft();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override // android.view.View
    public boolean performLongClick() {
        this.mCancelTap = true;
        return super.performLongClick();
    }

    @Override // android.view.ViewGroup
    public void removeAllViewsInLayout() {
        if (this.mPageIndicator != null) {
            this.mPageIndicator.removeAllMarkers(true);
        }
        super.removeAllViewsInLayout();
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void removeView(View view) {
        removeMarkerForView(indexOfChild(view));
        super.removeView(view);
    }

    @Override // android.view.ViewGroup
    public void removeViewAt(int i) {
        removeMarkerForView(i);
        super.removeViewAt(i);
    }

    @Override // android.view.ViewGroup
    public void removeViewInLayout(View view) {
        removeMarkerForView(indexOfChild(view));
        super.removeViewInLayout(view);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        int indexToPage = indexToPage(indexOfChild(view));
        if (indexToPage < 0 || indexToPage == getCurrentPage() || isInTouchMode()) {
            return;
        }
        snapToPage(indexToPage);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        int indexToPage = indexToPage(indexOfChild(view));
        if (indexToPage == this.mCurrentPage && this.mScroller.isFinished()) {
            return false;
        }
        snapToPage(indexToPage);
        return true;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        if (z) {
            getPageAt(this.mCurrentPage).cancelLongPress();
        }
        super.requestDisallowInterceptTouchEvent(z);
    }

    protected void screenScrolled(int i) {
    }

    @Override // android.view.View
    public void scrollBy(int i, int i2) {
        scrollTo(getUnboundedScrollX() + i, getScrollY() + i2);
    }

    public void scrollLeft() {
        if (getNextPage() > 0) {
            snapToPage(getNextPage() - 1);
        }
    }

    public void scrollRight() {
        if (getNextPage() < getChildCount() - 1) {
            snapToPage(getNextPage() + 1);
        }
    }

    @Override // android.view.View
    public void scrollTo(int i, int i2) {
        int i3 = i;
        if (this.mFreeScroll) {
            if (!this.mScroller.isFinished() && (i > this.mFreeScrollMaxScrollX || i < this.mFreeScrollMinScrollX)) {
                forceFinishScroller();
            }
            i3 = Math.max(Math.min(i, this.mFreeScrollMaxScrollX), this.mFreeScrollMinScrollX);
        }
        if (LauncherLog.DEBUG_DRAW) {
            LauncherLog.d("PagedView", "scrollTo: x = " + i3 + ", y = " + i2 + ", mOverScrollX = " + this.mMaxScrollX + ", mScrollX = " + getScrollX() + ", this = " + this);
        }
        boolean z = !this.mIsRtl ? i3 >= 0 : i3 <= this.mMaxScrollX;
        boolean z2 = !this.mIsRtl ? i3 <= this.mMaxScrollX : i3 >= 0;
        if (z) {
            super.scrollTo(this.mIsRtl ? this.mMaxScrollX : 0, i2);
            if (this.mAllowOverScroll) {
                this.mWasInOverscroll = true;
                if (this.mIsRtl) {
                    overScroll(i3 - this.mMaxScrollX);
                } else {
                    overScroll(i3);
                }
            }
        } else if (z2) {
            super.scrollTo(this.mIsRtl ? 0 : this.mMaxScrollX, i2);
            if (this.mAllowOverScroll) {
                this.mWasInOverscroll = true;
                if (this.mIsRtl) {
                    overScroll(i3);
                } else {
                    overScroll(i3 - this.mMaxScrollX);
                }
            }
        } else {
            if (this.mWasInOverscroll) {
                overScroll(0.0f);
                this.mWasInOverscroll = false;
            }
            super.scrollTo(i3, i2);
        }
        this.mTouchX = i3;
        this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
        if (isReordering(true)) {
            float[] mapPointFromParentToView = mapPointFromParentToView(this, this.mParentDownMotionX, this.mParentDownMotionY);
            this.mLastMotionX = mapPointFromParentToView[0];
            this.mLastMotionY = mapPointFromParentToView[1];
            updateDragViewTranslationDuringDrag();
        }
    }

    @Override // android.view.View, android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEvent(int i) {
        if (i != 4096) {
            super.sendAccessibilityEvent(i);
        }
    }

    public void setCurrentPage(int i) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("PagedView", "setCurrentPage: currentPage = " + i + ", mCurrentPage = " + this.mCurrentPage + ", this = " + this);
        }
        if (!this.mScroller.isFinished()) {
            abortScrollerAnimation(true);
        }
        if (getChildCount() == 0) {
            return;
        }
        this.mForceScreenScrolled = true;
        this.mCurrentPage = validateNewPage(i);
        updateCurrentPageScroll();
        notifyPageSwitchListener();
        invalidate();
    }

    protected void setDefaultInterpolator(Interpolator interpolator) {
        this.mDefaultInterpolator = interpolator;
        this.mScroller.setInterpolator(this.mDefaultInterpolator);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setEdgeGlowColor(int i) {
        this.mEdgeGlowLeft.setColor(i);
        this.mEdgeGlowRight.setColor(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setEnableOverscroll(boolean z) {
        this.mAllowOverScroll = z;
    }

    public void setMinScale(float f) {
        this.mMinScale = f;
        this.mUseMinScale = true;
        requestLayout();
    }

    @Override // android.view.View
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.mLongClickListener = onLongClickListener;
        int pageCount = getPageCount();
        for (int i = 0; i < pageCount; i++) {
            getPageAt(i).setOnLongClickListener(onLongClickListener);
        }
        super.setOnLongClickListener(onLongClickListener);
    }

    public void setPageSpacing(int i) {
        this.mPageSpacing = i;
        requestLayout();
    }

    public void setPageSwitchListener(PageSwitchListener pageSwitchListener) {
        this.mPageSwitchListener = pageSwitchListener;
        if (this.mPageSwitchListener != null) {
            this.mPageSwitchListener.onPageSwitch(getPageAt(this.mCurrentPage), this.mCurrentPage);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setRestorePage(int i) {
        this.mRestorePage = i;
    }

    @Override // android.view.View
    public void setScaleX(float f) {
        super.setScaleX(f);
        if (isReordering(true)) {
            float[] mapPointFromParentToView = mapPointFromParentToView(this, this.mParentDownMotionX, this.mParentDownMotionY);
            this.mLastMotionX = mapPointFromParentToView[0];
            this.mLastMotionY = mapPointFromParentToView[1];
            updateDragViewTranslationDuringDrag();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean shouldDrawChild(View view) {
        boolean z = false;
        if (view.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void snapToDestination() {
        snapToPage(getPageNearestToCenterOfScreen(), 750);
    }

    public void snapToPage(int i) {
        snapToPage(i, 750);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void snapToPage(int i, int i2) {
        snapToPage(i, i2, false, null);
    }

    protected void snapToPage(int i, int i2, int i3) {
        snapToPage(i, i2, i3, false, null);
    }

    protected void snapToPage(int i, int i2, int i3, boolean z, TimeInterpolator timeInterpolator) {
        int i4;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("PagedView", "(PagedView)snapToPage whichPage = " + i + ", delta = " + i2 + ", duration = " + i3 + ", mNextPage = " + this.mNextPage + ", mScrollX = , this = " + this);
        }
        this.mNextPage = validateNewPage(i);
        pageBeginMoving();
        awakenScrollBars(i3);
        if (z) {
            i4 = 0;
        } else {
            i4 = i3;
            if (i3 == 0) {
                i4 = Math.abs(i2);
            }
        }
        if (!this.mScroller.isFinished()) {
            abortScrollerAnimation(false);
        }
        if (timeInterpolator != null) {
            this.mScroller.setInterpolator(timeInterpolator);
        } else {
            this.mScroller.setInterpolator(this.mDefaultInterpolator);
        }
        this.mScroller.startScroll(getUnboundedScrollX(), 0, i2, 0, i4);
        updatePageIndicator();
        if (z) {
            computeScroll();
        }
        this.mForceScreenScrolled = true;
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void snapToPage(int i, int i2, TimeInterpolator timeInterpolator) {
        snapToPage(i, i2, false, timeInterpolator);
    }

    protected void snapToPage(int i, int i2, boolean z, TimeInterpolator timeInterpolator) {
        int validateNewPage = validateNewPage(i);
        snapToPage(validateNewPage, getScrollForPage(validateNewPage) - getUnboundedScrollX(), i2, z, timeInterpolator);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void snapToPageImmediately(int i) {
        snapToPage(i, 750, true, null);
    }

    protected void snapToPageWithVelocity(int i, int i2) {
        int validateNewPage = validateNewPage(i);
        int viewportWidth = getViewportWidth() / 2;
        int scrollForPage = getScrollForPage(validateNewPage) - getUnboundedScrollX();
        if (Math.abs(i2) < this.mMinFlingVelocity) {
            snapToPage(validateNewPage, 750);
            return;
        }
        float f = viewportWidth;
        float f2 = viewportWidth;
        float distanceInfluenceForSnapDuration = distanceInfluenceForSnapDuration(Math.min(1.0f, (Math.abs(scrollForPage) * 1.0f) / (viewportWidth * 2)));
        int max = Math.max(this.mMinSnapVelocity, Math.abs(i2));
        int round = Math.round(Math.abs((f + (f2 * distanceInfluenceForSnapDuration)) / max) * 1000.0f) * 4;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("PagedView", "snapToPageWithVelocity: velocity = " + max + ", whichPage = " + validateNewPage + ", duration = " + round + ", delta = " + scrollForPage + ", mScrollX = " + getScrollX() + ", this = " + this);
        }
        snapToPage(validateNewPage, scrollForPage, round);
    }

    public boolean startReordering(View view) {
        int indexOfChild = indexOfChild(view);
        if (this.mTouchState != 0 || indexOfChild == -1) {
            return false;
        }
        this.mTempVisiblePagesRange[0] = 0;
        this.mTempVisiblePagesRange[1] = getPageCount() - 1;
        getFreeScrollPageRange(this.mTempVisiblePagesRange);
        this.mReorderingStarted = true;
        if (this.mTempVisiblePagesRange[0] > indexOfChild || indexOfChild > this.mTempVisiblePagesRange[1]) {
            return false;
        }
        this.mDragView = getChildAt(indexOfChild);
        this.mDragView.animate().scaleX(1.15f).scaleY(1.15f).setDuration(100L).start();
        this.mDragViewBaselineLeft = this.mDragView.getLeft();
        snapToPage(getPageNearestToCenterOfScreen());
        disableFreeScroll();
        onStartReordering();
        return true;
    }

    protected void updateCurrentPageScroll() {
        int i = 0;
        if (this.mCurrentPage >= 0) {
            i = 0;
            if (this.mCurrentPage < getPageCount()) {
                i = getScrollForPage(this.mCurrentPage);
            }
        }
        scrollTo(i, 0);
        this.mScroller.setFinalX(i);
        forceFinishScroller();
    }

    void updateFreescrollBounds() {
        getFreeScrollPageRange(this.mTempVisiblePagesRange);
        if (this.mIsRtl) {
            this.mFreeScrollMinScrollX = getScrollForPage(this.mTempVisiblePagesRange[1]);
            this.mFreeScrollMaxScrollX = getScrollForPage(this.mTempVisiblePagesRange[0]);
            return;
        }
        this.mFreeScrollMinScrollX = getScrollForPage(this.mTempVisiblePagesRange[0]);
        this.mFreeScrollMaxScrollX = getScrollForPage(this.mTempVisiblePagesRange[1]);
    }

    void updateMaxScrollX() {
        int i = 0;
        int childCount = getChildCount();
        if (childCount <= 0) {
            this.mMaxScrollX = 0;
            return;
        }
        if (!this.mIsRtl) {
            i = childCount - 1;
        }
        this.mMaxScrollX = getScrollForPage(i);
    }
}
