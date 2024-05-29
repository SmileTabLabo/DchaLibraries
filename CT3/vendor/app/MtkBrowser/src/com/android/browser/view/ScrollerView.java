package com.android.browser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import com.android.internal.R;
import com.mediatek.browser.ext.IBrowserFeatureIndexExt;
import java.util.ArrayList;
/* loaded from: b.zip:com/android/browser/view/ScrollerView.class */
public class ScrollerView extends FrameLayout {
    private int mActivePointerId;
    protected View mChildToScrollTo;
    private PointF mDownCoords;
    private View mDownView;
    @ViewDebug.ExportedProperty(category = "layout")
    private boolean mFillViewport;
    private StrictMode.Span mFlingStrictSpan;
    protected boolean mHorizontal;
    protected boolean mIsBeingDragged;
    private boolean mIsLayoutDirty;
    protected boolean mIsOrthoDragged;
    private float mLastMotionY;
    private float mLastOrthoCoord;
    private long mLastScroll;
    private int mMaximumVelocity;
    protected int mMinimumVelocity;
    private int mOverflingDistance;
    private int mOverscrollDistance;
    private StrictMode.Span mScrollStrictSpan;
    protected OverScroller mScroller;
    private boolean mSmoothScrollingEnabled;
    private final Rect mTempRect;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    public ScrollerView(Context context) {
        this(context, null);
    }

    public ScrollerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842880);
    }

    public ScrollerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTempRect = new Rect();
        this.mIsLayoutDirty = true;
        this.mChildToScrollTo = null;
        this.mIsBeingDragged = false;
        this.mSmoothScrollingEnabled = true;
        this.mActivePointerId = -1;
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        initScrollView();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ScrollView, i, 0);
        setFillViewport(obtainStyledAttributes.getBoolean(0, false));
        obtainStyledAttributes.recycle();
    }

    private boolean canScroll() {
        boolean z = true;
        View childAt = getChildAt(0);
        if (childAt != null) {
            if (!this.mHorizontal) {
                return getHeight() < (childAt.getHeight() + this.mPaddingTop) + this.mPaddingBottom;
            }
            if (getWidth() >= childAt.getWidth() + this.mPaddingLeft + this.mPaddingRight) {
                z = false;
            }
            return z;
        }
        return false;
    }

    private int clamp(int i, int i2, int i3) {
        if (i2 >= i3 || i < 0) {
            return 0;
        }
        return i2 + i > i3 ? i3 - i2 : i;
    }

    private int computeScrollDeltaToGetChildRectOnScreenHorizontal(Rect rect) {
        int i;
        if (getChildCount() == 0) {
            return 0;
        }
        int width = getWidth();
        int scrollX = getScrollX();
        int i2 = scrollX + width;
        int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();
        int i3 = scrollX;
        if (rect.left > 0) {
            i3 = scrollX + horizontalFadingEdgeLength;
        }
        int i4 = i2;
        if (rect.right < getChildAt(0).getWidth()) {
            i4 = i2 - horizontalFadingEdgeLength;
        }
        if (rect.right <= i4 || rect.left <= i3) {
            i = 0;
            if (rect.left < i3) {
                i = 0;
                if (rect.right < i4) {
                    i = Math.max(rect.width() > width ? 0 - (i4 - rect.right) : 0 - (i3 - rect.left), -getScrollX());
                }
            }
        } else {
            i = Math.min(rect.width() > width ? (rect.left - i3) + 0 : (rect.right - i4) + 0, getChildAt(0).getRight() - i4);
        }
        return i;
    }

    private int computeScrollDeltaToGetChildRectOnScreenVertical(Rect rect) {
        int i;
        if (getChildCount() == 0) {
            return 0;
        }
        int height = getHeight();
        int scrollY = getScrollY();
        int i2 = scrollY + height;
        int verticalFadingEdgeLength = getVerticalFadingEdgeLength();
        int i3 = scrollY;
        if (rect.top > 0) {
            i3 = scrollY + verticalFadingEdgeLength;
        }
        int i4 = i2;
        if (rect.bottom < getChildAt(0).getHeight()) {
            i4 = i2 - verticalFadingEdgeLength;
        }
        if (rect.bottom <= i4 || rect.top <= i3) {
            i = 0;
            if (rect.top < i3) {
                i = 0;
                if (rect.bottom < i4) {
                    i = Math.max(rect.height() > height ? 0 - (i4 - rect.bottom) : 0 - (i3 - rect.top), -getScrollY());
                }
            }
        } else {
            i = Math.min(rect.height() > height ? (rect.top - i3) + 0 : (rect.bottom - i4) + 0, getChildAt(0).getBottom() - i4);
        }
        return i;
    }

    private void doScrollY(int i) {
        if (i != 0) {
            if (this.mSmoothScrollingEnabled) {
                if (this.mHorizontal) {
                    smoothScrollBy(0, i);
                } else {
                    smoothScrollBy(i, 0);
                }
            } else if (this.mHorizontal) {
                scrollBy(0, i);
            } else {
                scrollBy(i, 0);
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        this.mIsOrthoDragged = false;
        this.mDownView = null;
        recycleVelocityTracker();
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
    }

    private View findFocusableViewInBounds(boolean z, int i, int i2) {
        ArrayList focusables = getFocusables(2);
        View view = null;
        boolean z2 = false;
        int size = focusables.size();
        int i3 = 0;
        while (i3 < size) {
            View view2 = (View) focusables.get(i3);
            int left = this.mHorizontal ? view2.getLeft() : view2.getTop();
            int right = this.mHorizontal ? view2.getRight() : view2.getBottom();
            View view3 = view;
            boolean z3 = z2;
            if (i < right) {
                view3 = view;
                z3 = z2;
                if (left < i2) {
                    boolean z4 = i < left ? right < i2 : false;
                    if (view == null) {
                        view3 = view2;
                        z3 = z4;
                    } else {
                        boolean z5 = (!z || left >= (this.mHorizontal ? view.getLeft() : view.getTop())) ? !z && right > (this.mHorizontal ? view.getRight() : view.getBottom()) : true;
                        if (z2) {
                            view3 = view;
                            z3 = z2;
                            if (z4) {
                                view3 = view;
                                z3 = z2;
                                if (z5) {
                                    view3 = view2;
                                    z3 = z2;
                                }
                            }
                        } else if (z4) {
                            view3 = view2;
                            z3 = true;
                        } else {
                            view3 = view;
                            z3 = z2;
                            if (z5) {
                                view3 = view2;
                                z3 = z2;
                            }
                        }
                    }
                }
            }
            i3++;
            view = view3;
            z2 = z3;
        }
        return view;
    }

    private int getScrollRange() {
        int i = 0;
        if (getChildCount() > 0) {
            View childAt = getChildAt(0);
            i = this.mHorizontal ? Math.max(0, childAt.getWidth() - ((getWidth() - this.mPaddingRight) - this.mPaddingLeft)) : Math.max(0, childAt.getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
        }
        return i;
    }

    private boolean inChild(int i, int i2) {
        if (getChildCount() > 0) {
            int i3 = this.mScrollY;
            View childAt = getChildAt(0);
            boolean z = false;
            if (i2 >= childAt.getTop() - i3) {
                z = false;
                if (i2 < childAt.getBottom() - i3) {
                    z = false;
                    if (i >= childAt.getLeft()) {
                        z = false;
                        if (i < childAt.getRight()) {
                            z = true;
                        }
                    }
                }
            }
            return z;
        }
        return false;
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    private void initScrollView() {
        this.mScroller = new OverScroller(getContext());
        setFocusable(true);
        setDescendantFocusability(262144);
        setWillNotDraw(false);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mOverscrollDistance = viewConfiguration.getScaledOverscrollDistance();
        this.mOverflingDistance = viewConfiguration.getScaledOverflingDistance();
        this.mDownCoords = new PointF();
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private boolean isOffScreen(View view) {
        boolean z = false;
        if (!this.mHorizontal) {
            return !isWithinDeltaOfScreen(view, 0, getHeight());
        }
        if (!isWithinDeltaOfScreen(view, getWidth(), 0)) {
            z = true;
        }
        return z;
    }

    private boolean isOrthoMove(float f, float f2) {
        boolean z = true;
        if ((!this.mHorizontal || Math.abs(f2) <= Math.abs(f)) && (this.mHorizontal || Math.abs(f) <= Math.abs(f2))) {
            z = false;
        }
        return z;
    }

    private boolean isViewDescendantOf(View view, View view2) {
        if (view == view2) {
            return true;
        }
        ViewParent parent = view.getParent();
        return parent instanceof ViewGroup ? isViewDescendantOf((View) parent, view2) : false;
    }

    private boolean isWithinDeltaOfScreen(View view, int i, int i2) {
        boolean z = true;
        view.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(view, this.mTempRect);
        if (this.mHorizontal) {
            if (this.mTempRect.right + i < getScrollX()) {
                z = false;
            } else if (this.mTempRect.left - i > getScrollX() + i2) {
                z = false;
            }
            return z;
        }
        boolean z2 = false;
        if (this.mTempRect.bottom + i >= getScrollY()) {
            z2 = false;
            if (this.mTempRect.top - i <= getScrollY() + i2) {
                z2 = true;
            }
        }
        return z2;
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int action = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(action) == this.mActivePointerId) {
            int i = action == 0 ? 1 : 0;
            this.mLastMotionY = this.mHorizontal ? motionEvent.getX(i) : motionEvent.getY(i);
            this.mActivePointerId = motionEvent.getPointerId(i);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
            this.mLastOrthoCoord = this.mHorizontal ? motionEvent.getY(i) : motionEvent.getX(i);
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private boolean scrollAndFocus(int i, int i2, int i3) {
        boolean z;
        int height = getHeight();
        int scrollY = getScrollY();
        int i4 = scrollY + height;
        boolean z2 = i == 33;
        View findFocusableViewInBounds = findFocusableViewInBounds(z2, i2, i3);
        View view = findFocusableViewInBounds;
        if (findFocusableViewInBounds == null) {
            view = this;
        }
        if (i2 < scrollY || i3 > i4) {
            doScrollY(z2 ? i2 - scrollY : i3 - i4);
            z = true;
        } else {
            z = false;
        }
        if (view != findFocus()) {
            view.requestFocus(i);
        }
        return z;
    }

    private void scrollToChild(View view) {
        view.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(view, this.mTempRect);
        scrollToChildRect(this.mTempRect, true);
    }

    private boolean scrollToChildRect(Rect rect, boolean z) {
        int computeScrollDeltaToGetChildRectOnScreen = computeScrollDeltaToGetChildRectOnScreen(rect);
        boolean z2 = computeScrollDeltaToGetChildRectOnScreen != 0;
        if (z2) {
            if (z) {
                if (this.mHorizontal) {
                    scrollBy(computeScrollDeltaToGetChildRectOnScreen, 0);
                } else {
                    scrollBy(0, computeScrollDeltaToGetChildRectOnScreen);
                }
            } else if (this.mHorizontal) {
                smoothScrollBy(computeScrollDeltaToGetChildRectOnScreen, 0);
            } else {
                smoothScrollBy(0, computeScrollDeltaToGetChildRectOnScreen);
            }
        }
        return z2;
    }

    @Override // android.view.ViewGroup
    public void addView(View view) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(view);
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(view, i);
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(view, i, layoutParams);
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(view, layoutParams);
    }

    public boolean arrowScroll(int i) {
        int i2;
        View findFocus = findFocus();
        View view = findFocus;
        if (findFocus == this) {
            view = null;
        }
        View findNextFocus = FocusFinder.getInstance().findNextFocus(this, view, i);
        int maxScrollAmount = getMaxScrollAmount();
        if (findNextFocus == null || !isWithinDeltaOfScreen(findNextFocus, maxScrollAmount, getHeight())) {
            if (i != 33 || getScrollY() >= maxScrollAmount) {
                i2 = maxScrollAmount;
                if (i == 130) {
                    i2 = maxScrollAmount;
                    if (getChildCount() > 0) {
                        int bottom = getChildAt(0).getBottom();
                        int scrollY = (getScrollY() + getHeight()) - this.mPaddingBottom;
                        i2 = maxScrollAmount;
                        if (bottom - scrollY < maxScrollAmount) {
                            i2 = bottom - scrollY;
                        }
                    }
                }
            } else {
                i2 = getScrollY();
            }
            if (i2 == 0) {
                return false;
            }
            if (i != 130) {
                i2 = -i2;
            }
            doScrollY(i2);
        } else {
            findNextFocus.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(findNextFocus, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
            findNextFocus.requestFocus(i);
        }
        if (view != null && view.isFocused() && isOffScreen(view)) {
            int descendantFocusability = getDescendantFocusability();
            setDescendantFocusability(131072);
            requestFocus();
            setDescendantFocusability(descendantFocusability);
            return true;
        }
        return true;
    }

    @Override // android.view.View
    protected int computeHorizontalScrollOffset() {
        return Math.max(0, super.computeHorizontalScrollOffset());
    }

    @Override // android.view.View
    protected int computeHorizontalScrollRange() {
        int i;
        if (this.mHorizontal) {
            int childCount = getChildCount();
            int width = (getWidth() - this.mPaddingRight) - this.mPaddingLeft;
            if (childCount == 0) {
                return width;
            }
            int right = getChildAt(0).getRight();
            int i2 = this.mScrollX;
            int max = Math.max(0, right - width);
            if (i2 < 0) {
                i = right - i2;
            } else {
                i = right;
                if (i2 > max) {
                    i = right + (i2 - max);
                }
            }
            return i;
        }
        return super.computeHorizontalScrollRange();
    }

    @Override // android.view.View
    public void computeScroll() {
        if (!this.mScroller.computeScrollOffset()) {
            if (this.mFlingStrictSpan != null) {
                this.mFlingStrictSpan.finish();
                this.mFlingStrictSpan = null;
                return;
            }
            return;
        }
        int i = this.mScrollX;
        int i2 = this.mScrollY;
        int currX = this.mScroller.getCurrX();
        int currY = this.mScroller.getCurrY();
        if (i != currX || i2 != currY) {
            if (this.mHorizontal) {
                overScrollBy(currX - i, currY - i2, i, i2, getScrollRange(), 0, this.mOverflingDistance, 0, false);
            } else {
                overScrollBy(currX - i, currY - i2, i, i2, 0, getScrollRange(), 0, this.mOverflingDistance, false);
            }
            onScrollChanged(this.mScrollX, this.mScrollY, i, i2);
        }
        awakenScrollBars();
        invalidate();
    }

    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        return this.mHorizontal ? computeScrollDeltaToGetChildRectOnScreenHorizontal(rect) : computeScrollDeltaToGetChildRectOnScreenVertical(rect);
    }

    @Override // android.view.View
    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override // android.view.View
    protected int computeVerticalScrollRange() {
        int i;
        if (this.mHorizontal) {
            return super.computeVerticalScrollRange();
        }
        int childCount = getChildCount();
        int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (childCount == 0) {
            return height;
        }
        int bottom = getChildAt(0).getBottom();
        int i2 = this.mScrollY;
        int max = Math.max(0, bottom - height);
        if (i2 < 0) {
            i = bottom - i2;
        } else {
            i = bottom;
            if (i2 > max) {
                i = bottom + (i2 - max);
            }
        }
        return i;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return !super.dispatchKeyEvent(keyEvent) ? executeKeyEvent(keyEvent) : true;
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() != 4096) {
            super.dispatchPopulateAccessibilityEvent(accessibilityEvent);
            return false;
        }
        return false;
    }

    public boolean executeKeyEvent(KeyEvent keyEvent) {
        this.mTempRect.setEmpty();
        if (!canScroll()) {
            if (!isFocused() || keyEvent.getKeyCode() == 4) {
                return false;
            }
            View findFocus = findFocus();
            View view = findFocus;
            if (findFocus == this) {
                view = null;
            }
            View findNextFocus = FocusFinder.getInstance().findNextFocus(this, view, IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_ACCESSIBILITY);
            boolean z = false;
            if (findNextFocus != null) {
                z = false;
                if (findNextFocus != this) {
                    z = findNextFocus.requestFocus(IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_ACCESSIBILITY);
                }
            }
            return z;
        }
        boolean z2 = false;
        if (keyEvent.getAction() == 0) {
            switch (keyEvent.getKeyCode()) {
                case 19:
                    if (!keyEvent.isAltPressed()) {
                        z2 = arrowScroll(33);
                        break;
                    } else {
                        z2 = fullScroll(33);
                        break;
                    }
                case 20:
                    if (!keyEvent.isAltPressed()) {
                        z2 = arrowScroll(IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_ACCESSIBILITY);
                        break;
                    } else {
                        z2 = fullScroll(IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_ACCESSIBILITY);
                        break;
                    }
                case 62:
                    pageScroll(keyEvent.isShiftPressed() ? 33 : 130);
                    z2 = false;
                    break;
                default:
                    z2 = false;
                    break;
            }
        }
        return z2;
    }

    protected View findViewAt(int i, int i2) {
        return null;
    }

    public void fling(int i) {
        if (getChildCount() > 0) {
            if (this.mHorizontal) {
                int width = (getWidth() - this.mPaddingRight) - this.mPaddingLeft;
                this.mScroller.fling(this.mScrollX, this.mScrollY, i, 0, 0, Math.max(0, getChildAt(0).getWidth() - width), 0, 0, width / 2, 0);
            } else {
                int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
                this.mScroller.fling(this.mScrollX, this.mScrollY, 0, i, 0, 0, 0, Math.max(0, getChildAt(0).getHeight() - height), 0, height / 2);
            }
            if (this.mFlingStrictSpan == null) {
                this.mFlingStrictSpan = StrictMode.enterCriticalSpan("ScrollView-fling");
            }
            invalidate();
        }
    }

    public boolean fullScroll(int i) {
        int childCount;
        boolean z = i == 130;
        int height = getHeight();
        this.mTempRect.top = 0;
        this.mTempRect.bottom = height;
        if (z && (childCount = getChildCount()) > 0) {
            this.mTempRect.bottom = getChildAt(childCount - 1).getBottom() + this.mPaddingBottom;
            this.mTempRect.top = this.mTempRect.bottom - height;
        }
        return scrollAndFocus(i, this.mTempRect.top, this.mTempRect.bottom);
    }

    @Override // android.view.View
    protected float getBottomFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        if (this.mHorizontal) {
            int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();
            int right = (getChildAt(0).getRight() - this.mScrollX) - (getWidth() - this.mPaddingRight);
            if (right < horizontalFadingEdgeLength) {
                return right / horizontalFadingEdgeLength;
            }
            return 1.0f;
        }
        int verticalFadingEdgeLength = getVerticalFadingEdgeLength();
        int bottom = (getChildAt(0).getBottom() - this.mScrollY) - (getHeight() - this.mPaddingBottom);
        if (bottom < verticalFadingEdgeLength) {
            return bottom / verticalFadingEdgeLength;
        }
        return 1.0f;
    }

    public int getMaxScrollAmount() {
        int i;
        int i2;
        if (this.mHorizontal) {
            i = this.mRight;
            i2 = this.mLeft;
        } else {
            i = this.mBottom;
            i2 = this.mTop;
        }
        return (int) ((i - i2) * 0.5f);
    }

    @Override // android.view.View
    protected float getTopFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        if (this.mHorizontal) {
            int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();
            if (this.mScrollX < horizontalFadingEdgeLength) {
                return this.mScrollX / horizontalFadingEdgeLength;
            }
            return 1.0f;
        }
        int verticalFadingEdgeLength = getVerticalFadingEdgeLength();
        if (this.mScrollY < verticalFadingEdgeLength) {
            return this.mScrollY / verticalFadingEdgeLength;
        }
        return 1.0f;
    }

    @Override // android.view.ViewGroup
    protected void measureChild(View view, int i, int i2) {
        int childMeasureSpec;
        int makeMeasureSpec;
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (this.mHorizontal) {
            makeMeasureSpec = getChildMeasureSpec(i2, this.mPaddingTop + this.mPaddingBottom, layoutParams.height);
            childMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        } else {
            childMeasureSpec = getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight, layoutParams.width);
            makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        }
        view.measure(childMeasureSpec, makeMeasureSpec);
    }

    @Override // android.view.ViewGroup
    protected void measureChildWithMargins(View view, int i, int i2, int i3, int i4) {
        int childMeasureSpec;
        int makeMeasureSpec;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (this.mHorizontal) {
            makeMeasureSpec = getChildMeasureSpec(i3, this.mPaddingTop + this.mPaddingBottom + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + i4, marginLayoutParams.height);
            childMeasureSpec = View.MeasureSpec.makeMeasureSpec(marginLayoutParams.leftMargin + marginLayoutParams.rightMargin, 0);
        } else {
            childMeasureSpec = getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin + i2, marginLayoutParams.width);
            makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(marginLayoutParams.topMargin + marginLayoutParams.bottomMargin, 0);
        }
        view.measure(childMeasureSpec, makeMeasureSpec);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
        if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        int i;
        int i2;
        if ((motionEvent.getSource() & 2) != 0) {
            switch (motionEvent.getAction()) {
                case 8:
                    if (!this.mIsBeingDragged) {
                        if (this.mHorizontal) {
                            float axisValue = motionEvent.getAxisValue(10);
                            if (axisValue != 0.0f) {
                                int horizontalScrollFactor = (int) (getHorizontalScrollFactor() * axisValue);
                                int scrollRange = getScrollRange();
                                int i3 = this.mScrollX;
                                int i4 = i3 - horizontalScrollFactor;
                                if (i4 < 0) {
                                    i2 = 0;
                                } else {
                                    i2 = i4;
                                    if (i4 > scrollRange) {
                                        i2 = scrollRange;
                                    }
                                }
                                if (i2 != i3) {
                                    super.scrollTo(i2, this.mScrollY);
                                    return true;
                                }
                            }
                        } else {
                            float axisValue2 = motionEvent.getAxisValue(9);
                            if (axisValue2 != 0.0f) {
                                int verticalScrollFactor = (int) (getVerticalScrollFactor() * axisValue2);
                                int scrollRange2 = getScrollRange();
                                int i5 = this.mScrollY;
                                int i6 = i5 - verticalScrollFactor;
                                if (i6 < 0) {
                                    i = 0;
                                } else {
                                    i = i6;
                                    if (i6 > scrollRange2) {
                                        i = scrollRange2;
                                    }
                                }
                                if (i != i5) {
                                    super.scrollTo(this.mScrollX, i);
                                    return true;
                                }
                            }
                        }
                    }
                    break;
            }
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setScrollable(true);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setScrollable(true);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        if (action == 2 && this.mIsOrthoDragged) {
            return true;
        }
        switch (action & 255) {
            case 0:
                float x = this.mHorizontal ? motionEvent.getX() : motionEvent.getY();
                this.mDownCoords.x = motionEvent.getX();
                this.mDownCoords.y = motionEvent.getY();
                if (!inChild((int) motionEvent.getX(), (int) motionEvent.getY())) {
                    this.mIsBeingDragged = false;
                    recycleVelocityTracker();
                    break;
                } else {
                    this.mLastMotionY = x;
                    this.mActivePointerId = motionEvent.getPointerId(0);
                    initOrResetVelocityTracker();
                    this.mVelocityTracker.addMovement(motionEvent);
                    this.mIsBeingDragged = !this.mScroller.isFinished();
                    if (this.mIsBeingDragged && this.mScrollStrictSpan == null) {
                        this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                    }
                    this.mIsOrthoDragged = false;
                    this.mLastOrthoCoord = this.mHorizontal ? motionEvent.getY() : motionEvent.getX();
                    this.mDownView = findViewAt((int) motionEvent.getX(), (int) motionEvent.getY());
                    break;
                }
                break;
            case 1:
            case 3:
                this.mIsBeingDragged = false;
                this.mIsOrthoDragged = false;
                this.mActivePointerId = -1;
                recycleVelocityTracker();
                if (!this.mHorizontal) {
                    if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        invalidate();
                        break;
                    }
                } else if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, getScrollRange(), 0, 0)) {
                    invalidate();
                    break;
                }
                break;
            case 2:
                int i = this.mActivePointerId;
                if (i != -1) {
                    int findPointerIndex = motionEvent.findPointerIndex(i);
                    if (findPointerIndex != -1) {
                        float x2 = this.mHorizontal ? motionEvent.getX(findPointerIndex) : motionEvent.getY(findPointerIndex);
                        if (((int) Math.abs(x2 - this.mLastMotionY)) <= this.mTouchSlop) {
                            float y = this.mHorizontal ? motionEvent.getY(findPointerIndex) : motionEvent.getX(findPointerIndex);
                            if (Math.abs(y - this.mLastOrthoCoord) > this.mTouchSlop) {
                                this.mIsOrthoDragged = true;
                                this.mLastOrthoCoord = y;
                                initVelocityTrackerIfNotExists();
                                this.mVelocityTracker.addMovement(motionEvent);
                                break;
                            }
                        } else {
                            this.mIsBeingDragged = true;
                            this.mLastMotionY = x2;
                            initVelocityTrackerIfNotExists();
                            this.mVelocityTracker.addMovement(motionEvent);
                            if (this.mScrollStrictSpan == null) {
                                this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                                break;
                            }
                        }
                    } else {
                        Log.e("ScrollerView", "Invalid active pointer index = " + i + " at onInterceptTouchEvent ACTION_MOVE");
                        break;
                    }
                }
                break;
            case 6:
                onSecondaryPointerUp(motionEvent);
                break;
        }
        boolean z = true;
        if (!this.mIsBeingDragged) {
            z = this.mIsOrthoDragged;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mIsLayoutDirty = false;
        if (this.mChildToScrollTo != null && isViewDescendantOf(this.mChildToScrollTo, this)) {
            scrollToChild(this.mChildToScrollTo);
        }
        this.mChildToScrollTo = null;
        scrollTo(this.mScrollX, this.mScrollY);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mFillViewport && View.MeasureSpec.getMode(i2) != 0 && getChildCount() > 0) {
            View childAt = getChildAt(0);
            if (!this.mHorizontal) {
                int measuredHeight = getMeasuredHeight();
                if (childAt.getMeasuredHeight() < measuredHeight) {
                    childAt.measure(getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight, ((FrameLayout.LayoutParams) childAt.getLayoutParams()).width), View.MeasureSpec.makeMeasureSpec((measuredHeight - this.mPaddingTop) - this.mPaddingBottom, 1073741824));
                    return;
                }
                return;
            }
            int measuredWidth = getMeasuredWidth();
            if (childAt.getMeasuredWidth() < measuredWidth) {
                childAt.measure(View.MeasureSpec.makeMeasureSpec((measuredWidth - this.mPaddingLeft) - this.mPaddingRight, 1073741824), getChildMeasureSpec(i2, this.mPaddingTop + this.mPaddingBottom, ((FrameLayout.LayoutParams) childAt.getLayoutParams()).height));
            }
        }
    }

    protected void onOrthoDrag(View view, float f) {
    }

    protected void onOrthoDragFinished(View view) {
    }

    protected void onOrthoFling(View view, float f) {
    }

    @Override // android.view.View
    protected void onOverScrolled(int i, int i2, boolean z, boolean z2) {
        if (this.mScroller.isFinished()) {
            super.scrollTo(i, i2);
        } else {
            this.mScrollX = i;
            this.mScrollY = i2;
            invalidateParentIfNeeded();
            if (this.mHorizontal && z) {
                this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, getScrollRange(), 0, 0);
            } else if (!this.mHorizontal && z2) {
                this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange());
            }
        }
        awakenScrollBars();
    }

    protected void onPull(int i) {
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        int i2;
        if (this.mHorizontal) {
            if (i == 2) {
                i2 = 66;
            } else {
                i2 = i;
                if (i == 1) {
                    i2 = 17;
                }
            }
        } else if (i == 2) {
            i2 = 130;
        } else {
            i2 = i;
            if (i == 1) {
                i2 = 33;
            }
        }
        View findNextFocus = rect == null ? FocusFinder.getInstance().findNextFocus(this, null, i2) : FocusFinder.getInstance().findNextFocusFromRect(this, rect, i2);
        if (findNextFocus == null || isOffScreen(findNextFocus)) {
            return false;
        }
        return findNextFocus.requestFocus(i2, rect);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        View findFocus = findFocus();
        if (findFocus == null || this == findFocus || !isWithinDeltaOfScreen(findFocus, 0, i4)) {
            return;
        }
        findFocus.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(findFocus, this.mTempRect);
        doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        initVelocityTrackerIfNotExists();
        this.mVelocityTracker.addMovement(motionEvent);
        switch (motionEvent.getAction() & 255) {
            case 0:
                this.mIsBeingDragged = getChildCount() != 0;
                if (this.mIsBeingDragged) {
                    if (!this.mScroller.isFinished()) {
                        this.mScroller.abortAnimation();
                        if (this.mFlingStrictSpan != null) {
                            this.mFlingStrictSpan.finish();
                            this.mFlingStrictSpan = null;
                        }
                    }
                    this.mLastMotionY = this.mHorizontal ? motionEvent.getX() : motionEvent.getY();
                    this.mActivePointerId = motionEvent.getPointerId(0);
                    return true;
                }
                return false;
            case 1:
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                if (isOrthoMove(velocityTracker.getXVelocity(this.mActivePointerId), velocityTracker.getYVelocity(this.mActivePointerId))) {
                    if (this.mMinimumVelocity < Math.abs(this.mHorizontal ? velocityTracker.getYVelocity() : velocityTracker.getXVelocity())) {
                        onOrthoFling(this.mDownView, this.mHorizontal ? velocityTracker.getYVelocity() : velocityTracker.getXVelocity());
                        return true;
                    }
                }
                if (this.mIsOrthoDragged) {
                    onOrthoDragFinished(this.mDownView);
                    this.mActivePointerId = -1;
                    endDrag();
                    return true;
                } else if (this.mIsBeingDragged) {
                    VelocityTracker velocityTracker2 = this.mVelocityTracker;
                    velocityTracker2.computeCurrentVelocity(1000, this.mMaximumVelocity);
                    int xVelocity = this.mHorizontal ? (int) velocityTracker2.getXVelocity(this.mActivePointerId) : (int) velocityTracker2.getYVelocity(this.mActivePointerId);
                    if (getChildCount() > 0) {
                        if (Math.abs(xVelocity) > this.mMinimumVelocity) {
                            fling(-xVelocity);
                        } else {
                            int scrollRange = getScrollRange();
                            if (this.mHorizontal) {
                                if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, scrollRange, 0, 0)) {
                                    invalidate();
                                }
                            } else if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, scrollRange)) {
                                invalidate();
                            }
                        }
                        onPull(0);
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                    return true;
                } else {
                    return true;
                }
            case 2:
                if (this.mIsOrthoDragged) {
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex == -1) {
                        Log.e("ScrollerView", "Invalid active pointer index = " + this.mActivePointerId + " at onTouchEvent ACTION_MOVE");
                        return true;
                    }
                    float x = motionEvent.getX(findPointerIndex);
                    float y = motionEvent.getY(findPointerIndex);
                    if (isOrthoMove(x - this.mDownCoords.x, y - this.mDownCoords.y)) {
                        onOrthoDrag(this.mDownView, this.mHorizontal ? y - this.mDownCoords.y : x - this.mDownCoords.x);
                        return true;
                    }
                    return true;
                } else if (this.mIsBeingDragged) {
                    int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex2 == -1) {
                        Log.e("ScrollerView", "Invalid active pointer index = " + this.mActivePointerId + " at onTouchEvent ACTION_MOVE begin dragged");
                        return true;
                    }
                    float x2 = this.mHorizontal ? motionEvent.getX(findPointerIndex2) : motionEvent.getY(findPointerIndex2);
                    int i = (int) (this.mLastMotionY - x2);
                    this.mLastMotionY = x2;
                    int i2 = this.mScrollX;
                    int i3 = this.mScrollY;
                    int scrollRange2 = getScrollRange();
                    if (this.mHorizontal) {
                        if (overScrollBy(i, 0, this.mScrollX, 0, scrollRange2, 0, this.mOverscrollDistance, 0, true)) {
                            this.mVelocityTracker.clear();
                        }
                    } else if (overScrollBy(0, i, 0, this.mScrollY, 0, scrollRange2, 0, this.mOverscrollDistance, true)) {
                        this.mVelocityTracker.clear();
                    }
                    onScrollChanged(this.mScrollX, this.mScrollY, i2, i3);
                    int overScrollMode = getOverScrollMode();
                    if (overScrollMode == 0 || (overScrollMode == 1 && scrollRange2 > 0)) {
                        int i4 = this.mHorizontal ? i2 + i : i3 + i;
                        if (i4 < 0) {
                            onPull(i4);
                            return true;
                        } else if (i4 > scrollRange2) {
                            onPull(i4 - scrollRange2);
                            return true;
                        } else {
                            onPull(0);
                            return true;
                        }
                    }
                    return true;
                } else {
                    return true;
                }
            case 3:
                if (this.mIsOrthoDragged) {
                    onOrthoDragFinished(this.mDownView);
                    this.mActivePointerId = -1;
                    endDrag();
                    return true;
                } else if (!this.mIsBeingDragged || getChildCount() <= 0) {
                    return true;
                } else {
                    if (this.mHorizontal) {
                        if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, getScrollRange(), 0, 0)) {
                            invalidate();
                        }
                    } else if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        invalidate();
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                    return true;
                }
            case 4:
            default:
                return true;
            case 5:
                int actionIndex = motionEvent.getActionIndex();
                this.mLastMotionY = this.mHorizontal ? motionEvent.getX(actionIndex) : motionEvent.getY(actionIndex);
                this.mLastOrthoCoord = this.mHorizontal ? motionEvent.getY(actionIndex) : motionEvent.getX(actionIndex);
                this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                return true;
            case 6:
                onSecondaryPointerUp(motionEvent);
                int findPointerIndex3 = motionEvent.findPointerIndex(this.mActivePointerId);
                if (findPointerIndex3 == -1) {
                    Log.e("ScrollerView", "Invalid active pointer index = " + this.mActivePointerId + " at onTouchEvent ACTION_POINTER_UP");
                    return true;
                }
                this.mLastMotionY = this.mHorizontal ? motionEvent.getX(findPointerIndex3) : motionEvent.getY(findPointerIndex3);
                return true;
        }
    }

    public boolean pageScroll(int i) {
        boolean z = i == 130;
        int height = getHeight();
        if (z) {
            this.mTempRect.top = getScrollY() + height;
            int childCount = getChildCount();
            if (childCount > 0) {
                View childAt = getChildAt(childCount - 1);
                if (this.mTempRect.top + height > childAt.getBottom()) {
                    this.mTempRect.top = childAt.getBottom() - height;
                }
            }
        } else {
            this.mTempRect.top = getScrollY() - height;
            if (this.mTempRect.top < 0) {
                this.mTempRect.top = 0;
            }
        }
        this.mTempRect.bottom = this.mTempRect.top + height;
        return scrollAndFocus(i, this.mTempRect.top, this.mTempRect.bottom);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        if (this.mIsLayoutDirty) {
            this.mChildToScrollTo = view2;
        } else {
            scrollToChild(view2);
        }
        super.requestChildFocus(view, view2);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        rect.offset(view.getLeft() - view.getScrollX(), view.getTop() - view.getScrollY());
        return scrollToChildRect(rect, z);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        if (z) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(z);
    }

    @Override // android.view.View, android.view.ViewParent
    public void requestLayout() {
        this.mIsLayoutDirty = true;
        super.requestLayout();
    }

    @Override // android.view.View
    public void scrollTo(int i, int i2) {
        if (getChildCount() > 0) {
            View childAt = getChildAt(0);
            int clamp = clamp(i, (getWidth() - this.mPaddingRight) - this.mPaddingLeft, childAt.getWidth());
            int clamp2 = clamp(i2, (getHeight() - this.mPaddingBottom) - this.mPaddingTop, childAt.getHeight());
            if (clamp == this.mScrollX && clamp2 == this.mScrollY) {
                return;
            }
            super.scrollTo(clamp, clamp2);
        }
    }

    public void setFillViewport(boolean z) {
        if (z != this.mFillViewport) {
            this.mFillViewport = z;
            requestLayout();
        }
    }

    public void setOrientation(int i) {
        boolean z = false;
        if (i == 0) {
            z = true;
        }
        this.mHorizontal = z;
        Log.d("ScrollerView", "ScrollerView.setOrientation(): mHorizontal = " + this.mHorizontal);
        requestLayout();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public final void smoothScrollBy(int i, int i2) {
        if (getChildCount() == 0) {
            return;
        }
        if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250) {
            if (this.mHorizontal) {
                int width = getWidth();
                int i3 = this.mPaddingRight;
                int max = Math.max(0, getChildAt(0).getWidth() - ((width - i3) - this.mPaddingLeft));
                int i4 = this.mScrollX;
                this.mScroller.startScroll(i4, this.mScrollY, Math.max(0, Math.min(i4 + i, max)) - i4, 0);
            } else {
                int height = getHeight();
                int i5 = this.mPaddingBottom;
                int max2 = Math.max(0, getChildAt(0).getHeight() - ((height - i5) - this.mPaddingTop));
                int i6 = this.mScrollY;
                this.mScroller.startScroll(this.mScrollX, i6, 0, Math.max(0, Math.min(i6 + i2, max2)) - i6);
            }
            invalidate();
        } else {
            if (!this.mScroller.isFinished()) {
                this.mScroller.abortAnimation();
                if (this.mFlingStrictSpan != null) {
                    this.mFlingStrictSpan.finish();
                    this.mFlingStrictSpan = null;
                }
            }
            scrollBy(i, i2);
        }
        this.mLastScroll = AnimationUtils.currentAnimationTimeMillis();
    }

    public final void smoothScrollTo(int i, int i2) {
        smoothScrollBy(i - this.mScrollX, i2 - this.mScrollY);
    }
}
