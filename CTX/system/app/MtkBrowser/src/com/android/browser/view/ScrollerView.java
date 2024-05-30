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
import java.util.ArrayList;
/* loaded from: classes.dex */
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

    public void setOrientation(int i) {
        this.mHorizontal = i == 0;
        Log.d("ScrollerView", "ScrollerView.setOrientation(): mHorizontal = " + this.mHorizontal);
        requestLayout();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
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
        return (int) (0.5f * (i - i2));
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

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(view, layoutParams);
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(view, i, layoutParams);
    }

    private boolean canScroll() {
        View childAt = getChildAt(0);
        if (childAt != null) {
            return this.mHorizontal ? getWidth() < (childAt.getWidth() + this.mPaddingLeft) + this.mPaddingRight : getHeight() < (childAt.getHeight() + this.mPaddingTop) + this.mPaddingBottom;
        }
        return false;
    }

    public void setFillViewport(boolean z) {
        if (z != this.mFillViewport) {
            this.mFillViewport = z;
            requestLayout();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mFillViewport && View.MeasureSpec.getMode(i2) != 0 && getChildCount() > 0) {
            View childAt = getChildAt(0);
            if (this.mHorizontal) {
                int measuredWidth = getMeasuredWidth();
                if (childAt.getMeasuredWidth() < measuredWidth) {
                    childAt.measure(View.MeasureSpec.makeMeasureSpec((measuredWidth - this.mPaddingLeft) - this.mPaddingRight, 1073741824), getChildMeasureSpec(i2, this.mPaddingTop + this.mPaddingBottom, ((FrameLayout.LayoutParams) childAt.getLayoutParams()).height));
                    return;
                }
                return;
            }
            int measuredHeight = getMeasuredHeight();
            if (childAt.getMeasuredHeight() < measuredHeight) {
                childAt.measure(getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight, ((FrameLayout.LayoutParams) childAt.getLayoutParams()).width), View.MeasureSpec.makeMeasureSpec((measuredHeight - this.mPaddingTop) - this.mPaddingBottom, 1073741824));
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return super.dispatchKeyEvent(keyEvent) || executeKeyEvent(keyEvent);
    }

    public boolean executeKeyEvent(KeyEvent keyEvent) {
        this.mTempRect.setEmpty();
        if (!canScroll()) {
            if (!isFocused() || keyEvent.getKeyCode() == 4) {
                return false;
            }
            View findFocus = findFocus();
            if (findFocus == this) {
                findFocus = null;
            }
            View findNextFocus = FocusFinder.getInstance().findNextFocus(this, findFocus, 130);
            return (findNextFocus == null || findNextFocus == this || !findNextFocus.requestFocus(130)) ? false : true;
        } else if (keyEvent.getAction() == 0) {
            int keyCode = keyEvent.getKeyCode();
            if (keyCode != 62) {
                switch (keyCode) {
                    case 19:
                        if (!keyEvent.isAltPressed()) {
                            return arrowScroll(33);
                        }
                        return fullScroll(33);
                    case 20:
                        if (!keyEvent.isAltPressed()) {
                            return arrowScroll(130);
                        }
                        return fullScroll(130);
                    default:
                        return false;
                }
            }
            pageScroll(keyEvent.isShiftPressed() ? 33 : 130);
            return false;
        } else {
            return false;
        }
    }

    private boolean inChild(int i, int i2) {
        if (getChildCount() > 0) {
            int i3 = this.mScrollY;
            View childAt = getChildAt(0);
            return i2 >= childAt.getTop() - i3 && i2 < childAt.getBottom() - i3 && i >= childAt.getLeft() && i < childAt.getRight();
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

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        if (z) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(z);
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
        int i = action & 255;
        if (i != 6) {
            switch (i) {
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
                case 1:
                case 3:
                    this.mIsBeingDragged = false;
                    this.mIsOrthoDragged = false;
                    this.mActivePointerId = -1;
                    recycleVelocityTracker();
                    if (this.mHorizontal) {
                        if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, getScrollRange(), 0, 0)) {
                            invalidate();
                            break;
                        }
                    } else if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        invalidate();
                        break;
                    }
                    break;
                case 2:
                    int i2 = this.mActivePointerId;
                    if (i2 != -1) {
                        int findPointerIndex = motionEvent.findPointerIndex(i2);
                        if (findPointerIndex == -1) {
                            Log.e("ScrollerView", "Invalid active pointer index = " + i2 + " at onInterceptTouchEvent ACTION_MOVE");
                            break;
                        } else {
                            float x2 = this.mHorizontal ? motionEvent.getX(findPointerIndex) : motionEvent.getY(findPointerIndex);
                            if (((int) Math.abs(x2 - this.mLastMotionY)) > this.mTouchSlop) {
                                this.mIsBeingDragged = true;
                                this.mLastMotionY = x2;
                                initVelocityTrackerIfNotExists();
                                this.mVelocityTracker.addMovement(motionEvent);
                                if (this.mScrollStrictSpan == null) {
                                    this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                                    break;
                                }
                            } else {
                                float y = this.mHorizontal ? motionEvent.getY(findPointerIndex) : motionEvent.getX(findPointerIndex);
                                if (Math.abs(y - this.mLastOrthoCoord) > this.mTouchSlop) {
                                    this.mIsOrthoDragged = true;
                                    this.mLastOrthoCoord = y;
                                    initVelocityTrackerIfNotExists();
                                    this.mVelocityTracker.addMovement(motionEvent);
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
        } else {
            onSecondaryPointerUp(motionEvent);
        }
        return this.mIsBeingDragged || this.mIsOrthoDragged;
    }

    /* JADX WARN: Code restructure failed: missing block: B:72:0x01bf, code lost:
        if (r0 > 0) goto L66;
     */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int yVelocity;
        float y;
        int i;
        int i2;
        float f;
        float y2;
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
                    break;
                } else {
                    return false;
                }
            case 1:
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                if (isOrthoMove(velocityTracker.getXVelocity(this.mActivePointerId), velocityTracker.getYVelocity(this.mActivePointerId))) {
                    if (this.mMinimumVelocity < Math.abs(this.mHorizontal ? velocityTracker.getYVelocity() : velocityTracker.getXVelocity())) {
                        onOrthoFling(this.mDownView, this.mHorizontal ? velocityTracker.getYVelocity() : velocityTracker.getXVelocity());
                        break;
                    }
                }
                if (this.mIsOrthoDragged) {
                    onOrthoDragFinished(this.mDownView);
                    this.mActivePointerId = -1;
                    endDrag();
                    break;
                } else if (this.mIsBeingDragged) {
                    VelocityTracker velocityTracker2 = this.mVelocityTracker;
                    velocityTracker2.computeCurrentVelocity(1000, this.mMaximumVelocity);
                    if (this.mHorizontal) {
                        yVelocity = (int) velocityTracker2.getXVelocity(this.mActivePointerId);
                    } else {
                        yVelocity = (int) velocityTracker2.getYVelocity(this.mActivePointerId);
                    }
                    if (getChildCount() > 0) {
                        if (Math.abs(yVelocity) > this.mMinimumVelocity) {
                            fling(-yVelocity);
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
                    break;
                }
                break;
            case 2:
                if (!this.mIsOrthoDragged) {
                    if (this.mIsBeingDragged) {
                        int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex == -1) {
                            Log.e("ScrollerView", "Invalid active pointer index = " + this.mActivePointerId + " at onTouchEvent ACTION_MOVE begin dragged");
                            break;
                        } else {
                            if (this.mHorizontal) {
                                y = motionEvent.getX(findPointerIndex);
                            } else {
                                y = motionEvent.getY(findPointerIndex);
                            }
                            int i3 = (int) (this.mLastMotionY - y);
                            this.mLastMotionY = y;
                            int i4 = this.mScrollX;
                            int i5 = this.mScrollY;
                            int scrollRange2 = getScrollRange();
                            if (this.mHorizontal) {
                                i = scrollRange2;
                                if (overScrollBy(i3, 0, this.mScrollX, 0, scrollRange2, 0, this.mOverscrollDistance, 0, true)) {
                                    this.mVelocityTracker.clear();
                                }
                            } else {
                                i = scrollRange2;
                                if (overScrollBy(0, i3, 0, this.mScrollY, 0, i, 0, this.mOverscrollDistance, true)) {
                                    this.mVelocityTracker.clear();
                                }
                            }
                            onScrollChanged(this.mScrollX, this.mScrollY, i4, i5);
                            int overScrollMode = getOverScrollMode();
                            if (overScrollMode == 0) {
                                i2 = i;
                            } else if (overScrollMode == 1) {
                                i2 = i;
                                break;
                            }
                            int i6 = this.mHorizontal ? i4 + i3 : i5 + i3;
                            if (i6 < 0) {
                                onPull(i6);
                                break;
                            } else if (i6 > i2) {
                                onPull(i6 - i2);
                                break;
                            } else {
                                onPull(0);
                                break;
                            }
                        }
                    }
                } else {
                    int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex2 == -1) {
                        Log.e("ScrollerView", "Invalid active pointer index = " + this.mActivePointerId + " at onTouchEvent ACTION_MOVE");
                        break;
                    } else {
                        float x = motionEvent.getX(findPointerIndex2);
                        float y3 = motionEvent.getY(findPointerIndex2);
                        if (isOrthoMove(x - this.mDownCoords.x, y3 - this.mDownCoords.y)) {
                            View view = this.mDownView;
                            if (this.mHorizontal) {
                                f = y3 - this.mDownCoords.y;
                            } else {
                                f = x - this.mDownCoords.x;
                            }
                            onOrthoDrag(view, f);
                            break;
                        }
                    }
                }
                break;
            case 3:
                if (this.mIsOrthoDragged) {
                    onOrthoDragFinished(this.mDownView);
                    this.mActivePointerId = -1;
                    endDrag();
                    break;
                } else if (this.mIsBeingDragged && getChildCount() > 0) {
                    if (this.mHorizontal) {
                        if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, getScrollRange(), 0, 0)) {
                            invalidate();
                        }
                    } else if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        invalidate();
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                    break;
                }
                break;
            case 5:
                int actionIndex = motionEvent.getActionIndex();
                this.mLastMotionY = this.mHorizontal ? motionEvent.getX(actionIndex) : motionEvent.getY(actionIndex);
                this.mLastOrthoCoord = this.mHorizontal ? motionEvent.getY(actionIndex) : motionEvent.getX(actionIndex);
                this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                break;
            case 6:
                onSecondaryPointerUp(motionEvent);
                int findPointerIndex3 = motionEvent.findPointerIndex(this.mActivePointerId);
                if (findPointerIndex3 == -1) {
                    Log.e("ScrollerView", "Invalid active pointer index = " + this.mActivePointerId + " at onTouchEvent ACTION_POINTER_UP");
                    break;
                } else {
                    if (this.mHorizontal) {
                        y2 = motionEvent.getX(findPointerIndex3);
                    } else {
                        y2 = motionEvent.getY(findPointerIndex3);
                    }
                    this.mLastMotionY = y2;
                    break;
                }
        }
        return true;
    }

    protected View findViewAt(int i, int i2) {
        return null;
    }

    protected void onPull(int i) {
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

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if ((motionEvent.getSource() & 2) != 0 && motionEvent.getAction() == 8 && !this.mIsBeingDragged) {
            if (this.mHorizontal) {
                float axisValue = motionEvent.getAxisValue(10);
                if (axisValue != 0.0f) {
                    int scrollRange = getScrollRange();
                    int i = this.mScrollX;
                    int horizontalScrollFactor = i - ((int) (axisValue * getHorizontalScrollFactor()));
                    if (horizontalScrollFactor >= 0) {
                        if (horizontalScrollFactor > scrollRange) {
                            horizontalScrollFactor = scrollRange;
                        }
                    } else {
                        horizontalScrollFactor = 0;
                    }
                    if (horizontalScrollFactor != i) {
                        super.scrollTo(horizontalScrollFactor, this.mScrollY);
                        return true;
                    }
                }
            } else {
                float axisValue2 = motionEvent.getAxisValue(9);
                if (axisValue2 != 0.0f) {
                    int scrollRange2 = getScrollRange();
                    int i2 = this.mScrollY;
                    int verticalScrollFactor = i2 - ((int) (axisValue2 * getVerticalScrollFactor()));
                    if (verticalScrollFactor >= 0) {
                        if (verticalScrollFactor > scrollRange2) {
                            verticalScrollFactor = scrollRange2;
                        }
                    } else {
                        verticalScrollFactor = 0;
                    }
                    if (verticalScrollFactor != i2) {
                        super.scrollTo(this.mScrollX, verticalScrollFactor);
                        return true;
                    }
                }
            }
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    protected void onOrthoDrag(View view, float f) {
    }

    protected void onOrthoDragFinished(View view) {
    }

    protected void onOrthoFling(View view, float f) {
    }

    @Override // android.view.View
    protected void onOverScrolled(int i, int i2, boolean z, boolean z2) {
        if (!this.mScroller.isFinished()) {
            this.mScrollX = i;
            this.mScrollY = i2;
            invalidateParentIfNeeded();
            if (this.mHorizontal && z) {
                this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, getScrollRange(), 0, 0);
            } else if (!this.mHorizontal && z2) {
                this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange());
            }
        } else {
            super.scrollTo(i, i2);
        }
        awakenScrollBars();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setScrollable(true);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setScrollable(true);
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() != 4096) {
            super.dispatchPopulateAccessibilityEvent(accessibilityEvent);
            return false;
        }
        return false;
    }

    private int getScrollRange() {
        if (getChildCount() > 0) {
            View childAt = getChildAt(0);
            if (this.mHorizontal) {
                return Math.max(0, childAt.getWidth() - ((getWidth() - this.mPaddingRight) - this.mPaddingLeft));
            }
            return Math.max(0, childAt.getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
        }
        return 0;
    }

    private View findFocusableViewInBounds(boolean z, int i, int i2) {
        ArrayList focusables = getFocusables(2);
        int size = focusables.size();
        View view = null;
        boolean z2 = false;
        for (int i3 = 0; i3 < size; i3++) {
            View view2 = (View) focusables.get(i3);
            int left = this.mHorizontal ? view2.getLeft() : view2.getTop();
            int right = this.mHorizontal ? view2.getRight() : view2.getBottom();
            if (i < right && left < i2) {
                boolean z3 = i < left && right < i2;
                if (view == null) {
                    view = view2;
                    z2 = z3;
                } else {
                    boolean z4 = (z && left < (this.mHorizontal ? view.getLeft() : view.getTop())) || (!z && right > (this.mHorizontal ? view.getRight() : view.getBottom()));
                    if (z2) {
                        if (z3) {
                            if (!z4) {
                            }
                            view = view2;
                        }
                    } else if (z3) {
                        view = view2;
                        z2 = true;
                    } else {
                        if (!z4) {
                        }
                        view = view2;
                    }
                }
            }
        }
        return view;
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

    private boolean scrollAndFocus(int i, int i2, int i3) {
        int height = getHeight();
        int scrollY = getScrollY();
        int i4 = height + scrollY;
        boolean z = false;
        boolean z2 = i == 33;
        View findFocusableViewInBounds = findFocusableViewInBounds(z2, i2, i3);
        if (findFocusableViewInBounds == null) {
            findFocusableViewInBounds = this;
        }
        if (i2 < scrollY || i3 > i4) {
            doScrollY(z2 ? i2 - scrollY : i3 - i4);
            z = true;
        }
        if (findFocusableViewInBounds != findFocus()) {
            findFocusableViewInBounds.requestFocus(i);
        }
        return z;
    }

    public boolean arrowScroll(int i) {
        int bottom;
        View findFocus = findFocus();
        if (findFocus == this) {
            findFocus = null;
        }
        View findNextFocus = FocusFinder.getInstance().findNextFocus(this, findFocus, i);
        int maxScrollAmount = getMaxScrollAmount();
        if (findNextFocus != null && isWithinDeltaOfScreen(findNextFocus, maxScrollAmount, getHeight())) {
            findNextFocus.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(findNextFocus, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
            findNextFocus.requestFocus(i);
        } else {
            if (i == 33 && getScrollY() < maxScrollAmount) {
                maxScrollAmount = getScrollY();
            } else if (i == 130 && getChildCount() > 0 && (bottom = getChildAt(0).getBottom() - ((getScrollY() + getHeight()) - this.mPaddingBottom)) < maxScrollAmount) {
                maxScrollAmount = bottom;
            }
            if (maxScrollAmount == 0) {
                return false;
            }
            if (i != 130) {
                maxScrollAmount = -maxScrollAmount;
            }
            doScrollY(maxScrollAmount);
        }
        if (findFocus != null && findFocus.isFocused() && isOffScreen(findFocus)) {
            int descendantFocusability = getDescendantFocusability();
            setDescendantFocusability(131072);
            requestFocus();
            setDescendantFocusability(descendantFocusability);
            return true;
        }
        return true;
    }

    private boolean isOrthoMove(float f, float f2) {
        return (this.mHorizontal && Math.abs(f2) > Math.abs(f)) || (!this.mHorizontal && Math.abs(f) > Math.abs(f2));
    }

    private boolean isOffScreen(View view) {
        if (this.mHorizontal) {
            return !isWithinDeltaOfScreen(view, getWidth(), 0);
        }
        return !isWithinDeltaOfScreen(view, 0, getHeight());
    }

    private boolean isWithinDeltaOfScreen(View view, int i, int i2) {
        view.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(view, this.mTempRect);
        return this.mHorizontal ? this.mTempRect.right + i >= getScrollX() && this.mTempRect.left - i <= getScrollX() + i2 : this.mTempRect.bottom + i >= getScrollY() && this.mTempRect.top - i <= getScrollY() + i2;
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

    public final void smoothScrollBy(int i, int i2) {
        if (getChildCount() == 0) {
            return;
        }
        if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250) {
            if (this.mHorizontal) {
                int max = Math.max(0, getChildAt(0).getWidth() - ((getWidth() - this.mPaddingRight) - this.mPaddingLeft));
                int i3 = this.mScrollX;
                this.mScroller.startScroll(i3, this.mScrollY, Math.max(0, Math.min(i + i3, max)) - i3, 0);
            } else {
                int max2 = Math.max(0, getChildAt(0).getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
                int i4 = this.mScrollY;
                this.mScroller.startScroll(this.mScrollX, i4, 0, Math.max(0, Math.min(i2 + i4, max2)) - i4);
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

    @Override // android.view.View
    protected int computeVerticalScrollRange() {
        if (this.mHorizontal) {
            return super.computeVerticalScrollRange();
        }
        int childCount = getChildCount();
        int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (childCount == 0) {
            return height;
        }
        int bottom = getChildAt(0).getBottom();
        int i = this.mScrollY;
        int max = Math.max(0, bottom - height);
        if (i < 0) {
            return bottom - i;
        }
        if (i > max) {
            return bottom + (i - max);
        }
        return bottom;
    }

    @Override // android.view.View
    protected int computeHorizontalScrollRange() {
        if (!this.mHorizontal) {
            return super.computeHorizontalScrollRange();
        }
        int childCount = getChildCount();
        int width = (getWidth() - this.mPaddingRight) - this.mPaddingLeft;
        if (childCount == 0) {
            return width;
        }
        int right = getChildAt(0).getRight();
        int i = this.mScrollX;
        int max = Math.max(0, right - width);
        if (i < 0) {
            return right - i;
        }
        if (i > max) {
            return right + (i - max);
        }
        return right;
    }

    @Override // android.view.View
    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override // android.view.View
    protected int computeHorizontalScrollOffset() {
        return Math.max(0, super.computeHorizontalScrollOffset());
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

    @Override // android.view.View
    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
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
        } else if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
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

    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        if (this.mHorizontal) {
            return computeScrollDeltaToGetChildRectOnScreenHorizontal(rect);
        }
        return computeScrollDeltaToGetChildRectOnScreenVertical(rect);
    }

    private int computeScrollDeltaToGetChildRectOnScreenVertical(Rect rect) {
        int i;
        int i2;
        if (getChildCount() == 0) {
            return 0;
        }
        int height = getHeight();
        int scrollY = getScrollY();
        int i3 = scrollY + height;
        int verticalFadingEdgeLength = getVerticalFadingEdgeLength();
        if (rect.top > 0) {
            scrollY += verticalFadingEdgeLength;
        }
        if (rect.bottom < getChildAt(0).getHeight()) {
            i3 -= verticalFadingEdgeLength;
        }
        if (rect.bottom > i3 && rect.top > scrollY) {
            if (rect.height() > height) {
                i2 = (rect.top - scrollY) + 0;
            } else {
                i2 = (rect.bottom - i3) + 0;
            }
            return Math.min(i2, getChildAt(0).getBottom() - i3);
        } else if (rect.top >= scrollY || rect.bottom >= i3) {
            return 0;
        } else {
            if (rect.height() > height) {
                i = 0 - (i3 - rect.bottom);
            } else {
                i = 0 - (scrollY - rect.top);
            }
            return Math.max(i, -getScrollY());
        }
    }

    private int computeScrollDeltaToGetChildRectOnScreenHorizontal(Rect rect) {
        int i;
        int i2;
        if (getChildCount() == 0) {
            return 0;
        }
        int width = getWidth();
        int scrollX = getScrollX();
        int i3 = scrollX + width;
        int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();
        if (rect.left > 0) {
            scrollX += horizontalFadingEdgeLength;
        }
        if (rect.right < getChildAt(0).getWidth()) {
            i3 -= horizontalFadingEdgeLength;
        }
        if (rect.right > i3 && rect.left > scrollX) {
            if (rect.width() > width) {
                i2 = (rect.left - scrollX) + 0;
            } else {
                i2 = (rect.right - i3) + 0;
            }
            return Math.min(i2, getChildAt(0).getRight() - i3);
        } else if (rect.left >= scrollX || rect.right >= i3) {
            return 0;
        } else {
            if (rect.width() > width) {
                i = 0 - (i3 - rect.right);
            } else {
                i = 0 - (scrollX - rect.left);
            }
            return Math.max(i, -getScrollX());
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        if (!this.mIsLayoutDirty) {
            scrollToChild(view2);
        } else {
            this.mChildToScrollTo = view2;
        }
        super.requestChildFocus(view, view2);
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        View findNextFocusFromRect;
        if (this.mHorizontal) {
            if (i == 2) {
                i = 66;
            } else if (i == 1) {
                i = 17;
            }
        } else if (i == 2) {
            i = 130;
        } else if (i == 1) {
            i = 33;
        }
        if (rect == null) {
            findNextFocusFromRect = FocusFinder.getInstance().findNextFocus(this, null, i);
        } else {
            findNextFocusFromRect = FocusFinder.getInstance().findNextFocusFromRect(this, rect, i);
        }
        if (findNextFocusFromRect == null || isOffScreen(findNextFocusFromRect)) {
            return false;
        }
        return findNextFocusFromRect.requestFocus(i, rect);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        rect.offset(view.getLeft() - view.getScrollX(), view.getTop() - view.getScrollY());
        return scrollToChildRect(rect, z);
    }

    @Override // android.view.View, android.view.ViewParent
    public void requestLayout() {
        this.mIsLayoutDirty = true;
        super.requestLayout();
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

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        View findFocus = findFocus();
        if (findFocus != null && this != findFocus && isWithinDeltaOfScreen(findFocus, 0, i4)) {
            findFocus.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(findFocus, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
        }
    }

    private boolean isViewDescendantOf(View view, View view2) {
        if (view == view2) {
            return true;
        }
        ViewParent parent = view.getParent();
        return (parent instanceof ViewGroup) && isViewDescendantOf((View) parent, view2);
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

    @Override // android.view.View
    public void scrollTo(int i, int i2) {
        if (getChildCount() > 0) {
            View childAt = getChildAt(0);
            int clamp = clamp(i, (getWidth() - this.mPaddingRight) - this.mPaddingLeft, childAt.getWidth());
            int clamp2 = clamp(i2, (getHeight() - this.mPaddingBottom) - this.mPaddingTop, childAt.getHeight());
            if (clamp != this.mScrollX || clamp2 != this.mScrollY) {
                super.scrollTo(clamp, clamp2);
            }
        }
    }

    private int clamp(int i, int i2, int i3) {
        if (i2 >= i3 || i < 0) {
            return 0;
        }
        if (i2 + i > i3) {
            return i3 - i2;
        }
        return i;
    }
}
