package android.support.v4.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import java.util.Arrays;
/* loaded from: a.zip:android/support/v4/widget/ViewDragHelper.class */
public class ViewDragHelper {
    private static final Interpolator sInterpolator = new Interpolator() { // from class: android.support.v4.widget.ViewDragHelper.1
        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float f2 = f - 1.0f;
            return (f2 * f2 * f2 * f2 * f2) + 1.0f;
        }
    };
    private final Callback mCallback;
    private View mCapturedView;
    private int mDragState;
    private int[] mEdgeDragsInProgress;
    private int[] mEdgeDragsLocked;
    private int mEdgeSize;
    private int[] mInitialEdgesTouched;
    private float[] mInitialMotionX;
    private float[] mInitialMotionY;
    private float[] mLastMotionX;
    private float[] mLastMotionY;
    private float mMaxVelocity;
    private float mMinVelocity;
    private final ViewGroup mParentView;
    private int mPointersDown;
    private boolean mReleaseInProgress;
    private ScrollerCompat mScroller;
    private int mTouchSlop;
    private int mTrackingEdges;
    private VelocityTracker mVelocityTracker;
    private int mActivePointerId = -1;
    private final Runnable mSetIdleRunnable = new Runnable(this) { // from class: android.support.v4.widget.ViewDragHelper.2
        final ViewDragHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.setDragState(0);
        }
    };

    /* loaded from: a.zip:android/support/v4/widget/ViewDragHelper$Callback.class */
    public static abstract class Callback {
        public int clampViewPositionHorizontal(View view, int i, int i2) {
            return 0;
        }

        public int clampViewPositionVertical(View view, int i, int i2) {
            return 0;
        }

        public int getOrderedChildIndex(int i) {
            return i;
        }

        public int getViewHorizontalDragRange(View view) {
            return 0;
        }

        public int getViewVerticalDragRange(View view) {
            return 0;
        }

        public void onEdgeDragStarted(int i, int i2) {
        }

        public boolean onEdgeLock(int i) {
            return false;
        }

        public void onEdgeTouched(int i, int i2) {
        }

        public void onViewCaptured(View view, int i) {
        }

        public void onViewDragStateChanged(int i) {
        }

        public void onViewPositionChanged(View view, int i, int i2, int i3, int i4) {
        }

        public void onViewReleased(View view, float f, float f2) {
        }

        public abstract boolean tryCaptureView(View view, int i);
    }

    private ViewDragHelper(Context context, ViewGroup viewGroup, Callback callback) {
        if (viewGroup == null) {
            throw new IllegalArgumentException("Parent view may not be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback may not be null");
        }
        this.mParentView = viewGroup;
        this.mCallback = callback;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mEdgeSize = (int) ((20.0f * context.getResources().getDisplayMetrics().density) + 0.5f);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMaxVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mMinVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mScroller = ScrollerCompat.create(context, sInterpolator);
    }

    private boolean checkNewEdgeDrag(float f, float f2, int i, int i2) {
        float abs = Math.abs(f);
        float abs2 = Math.abs(f2);
        if ((this.mInitialEdgesTouched[i] & i2) != i2 || (this.mTrackingEdges & i2) == 0 || (this.mEdgeDragsLocked[i] & i2) == i2 || (this.mEdgeDragsInProgress[i] & i2) == i2) {
            return false;
        }
        if (abs > this.mTouchSlop || abs2 > this.mTouchSlop) {
            if (abs < 0.5f * abs2 && this.mCallback.onEdgeLock(i2)) {
                int[] iArr = this.mEdgeDragsLocked;
                iArr[i] = iArr[i] | i2;
                return false;
            }
            boolean z = false;
            if ((this.mEdgeDragsInProgress[i] & i2) == 0) {
                z = false;
                if (abs > this.mTouchSlop) {
                    z = true;
                }
            }
            return z;
        }
        return false;
    }

    private boolean checkTouchSlop(View view, float f, float f2) {
        boolean z = true;
        if (view == null) {
            return false;
        }
        boolean z2 = this.mCallback.getViewHorizontalDragRange(view) > 0;
        boolean z3 = this.mCallback.getViewVerticalDragRange(view) > 0;
        if (z2 && z3) {
            if ((f * f) + (f2 * f2) <= this.mTouchSlop * this.mTouchSlop) {
                z = false;
            }
            return z;
        } else if (z2) {
            return Math.abs(f) > ((float) this.mTouchSlop);
        } else if (z3) {
            return Math.abs(f2) > ((float) this.mTouchSlop);
        } else {
            return false;
        }
    }

    private float clampMag(float f, float f2, float f3) {
        float abs = Math.abs(f);
        if (abs < f2) {
            return 0.0f;
        }
        if (abs > f3) {
            if (f <= 0.0f) {
                f3 = -f3;
            }
            return f3;
        }
        return f;
    }

    private int clampMag(int i, int i2, int i3) {
        int abs = Math.abs(i);
        if (abs < i2) {
            return 0;
        }
        if (abs > i3) {
            if (i <= 0) {
                i3 = -i3;
            }
            return i3;
        }
        return i;
    }

    private void clearMotionHistory() {
        if (this.mInitialMotionX == null) {
            return;
        }
        Arrays.fill(this.mInitialMotionX, 0.0f);
        Arrays.fill(this.mInitialMotionY, 0.0f);
        Arrays.fill(this.mLastMotionX, 0.0f);
        Arrays.fill(this.mLastMotionY, 0.0f);
        Arrays.fill(this.mInitialEdgesTouched, 0);
        Arrays.fill(this.mEdgeDragsInProgress, 0);
        Arrays.fill(this.mEdgeDragsLocked, 0);
        this.mPointersDown = 0;
    }

    private void clearMotionHistory(int i) {
        if (this.mInitialMotionX == null || !isPointerDown(i)) {
            return;
        }
        this.mInitialMotionX[i] = 0.0f;
        this.mInitialMotionY[i] = 0.0f;
        this.mLastMotionX[i] = 0.0f;
        this.mLastMotionY[i] = 0.0f;
        this.mInitialEdgesTouched[i] = 0;
        this.mEdgeDragsInProgress[i] = 0;
        this.mEdgeDragsLocked[i] = 0;
        this.mPointersDown &= (1 << i) ^ (-1);
    }

    private int computeAxisDuration(int i, int i2, int i3) {
        if (i == 0) {
            return 0;
        }
        int width = this.mParentView.getWidth();
        int i4 = width / 2;
        float f = i4;
        float f2 = i4;
        float distanceInfluenceForSnapDuration = distanceInfluenceForSnapDuration(Math.min(1.0f, Math.abs(i) / width));
        int abs = Math.abs(i2);
        return Math.min(abs > 0 ? Math.round(Math.abs((f + (f2 * distanceInfluenceForSnapDuration)) / abs) * 1000.0f) * 4 : (int) (((Math.abs(i) / i3) + 1.0f) * 256.0f), 600);
    }

    private int computeSettleDuration(View view, int i, int i2, int i3, int i4) {
        int clampMag = clampMag(i3, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        int clampMag2 = clampMag(i4, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        int abs = Math.abs(i);
        int abs2 = Math.abs(i2);
        int abs3 = Math.abs(clampMag);
        int abs4 = Math.abs(clampMag2);
        int i5 = abs3 + abs4;
        int i6 = abs + abs2;
        return (int) ((computeAxisDuration(i, clampMag, this.mCallback.getViewHorizontalDragRange(view)) * (clampMag != 0 ? abs3 / i5 : abs / i6)) + (computeAxisDuration(i2, clampMag2, this.mCallback.getViewVerticalDragRange(view)) * (clampMag2 != 0 ? abs4 / i5 : abs2 / i6)));
    }

    public static ViewDragHelper create(ViewGroup viewGroup, float f, Callback callback) {
        ViewDragHelper create = create(viewGroup, callback);
        create.mTouchSlop = (int) (create.mTouchSlop * (1.0f / f));
        return create;
    }

    public static ViewDragHelper create(ViewGroup viewGroup, Callback callback) {
        return new ViewDragHelper(viewGroup.getContext(), viewGroup, callback);
    }

    private void dispatchViewReleased(float f, float f2) {
        this.mReleaseInProgress = true;
        this.mCallback.onViewReleased(this.mCapturedView, f, f2);
        this.mReleaseInProgress = false;
        if (this.mDragState == 1) {
            setDragState(0);
        }
    }

    private float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((float) ((f - 0.5f) * 0.4712389167638204d));
    }

    private void dragTo(int i, int i2, int i3, int i4) {
        int i5 = i;
        int i6 = i2;
        int left = this.mCapturedView.getLeft();
        int top = this.mCapturedView.getTop();
        if (i3 != 0) {
            i5 = this.mCallback.clampViewPositionHorizontal(this.mCapturedView, i, i3);
            ViewCompat.offsetLeftAndRight(this.mCapturedView, i5 - left);
        }
        if (i4 != 0) {
            i6 = this.mCallback.clampViewPositionVertical(this.mCapturedView, i2, i4);
            ViewCompat.offsetTopAndBottom(this.mCapturedView, i6 - top);
        }
        if (i3 == 0 && i4 == 0) {
            return;
        }
        this.mCallback.onViewPositionChanged(this.mCapturedView, i5, i6, i5 - left, i6 - top);
    }

    private void ensureMotionHistorySizeForId(int i) {
        if (this.mInitialMotionX == null || this.mInitialMotionX.length <= i) {
            float[] fArr = new float[i + 1];
            float[] fArr2 = new float[i + 1];
            float[] fArr3 = new float[i + 1];
            float[] fArr4 = new float[i + 1];
            int[] iArr = new int[i + 1];
            int[] iArr2 = new int[i + 1];
            int[] iArr3 = new int[i + 1];
            if (this.mInitialMotionX != null) {
                System.arraycopy(this.mInitialMotionX, 0, fArr, 0, this.mInitialMotionX.length);
                System.arraycopy(this.mInitialMotionY, 0, fArr2, 0, this.mInitialMotionY.length);
                System.arraycopy(this.mLastMotionX, 0, fArr3, 0, this.mLastMotionX.length);
                System.arraycopy(this.mLastMotionY, 0, fArr4, 0, this.mLastMotionY.length);
                System.arraycopy(this.mInitialEdgesTouched, 0, iArr, 0, this.mInitialEdgesTouched.length);
                System.arraycopy(this.mEdgeDragsInProgress, 0, iArr2, 0, this.mEdgeDragsInProgress.length);
                System.arraycopy(this.mEdgeDragsLocked, 0, iArr3, 0, this.mEdgeDragsLocked.length);
            }
            this.mInitialMotionX = fArr;
            this.mInitialMotionY = fArr2;
            this.mLastMotionX = fArr3;
            this.mLastMotionY = fArr4;
            this.mInitialEdgesTouched = iArr;
            this.mEdgeDragsInProgress = iArr2;
            this.mEdgeDragsLocked = iArr3;
        }
    }

    private boolean forceSettleCapturedViewAt(int i, int i2, int i3, int i4) {
        int left = this.mCapturedView.getLeft();
        int top = this.mCapturedView.getTop();
        int i5 = i - left;
        int i6 = i2 - top;
        if (i5 == 0 && i6 == 0) {
            this.mScroller.abortAnimation();
            setDragState(0);
            return false;
        }
        this.mScroller.startScroll(left, top, i5, i6, computeSettleDuration(this.mCapturedView, i5, i6, i3, i4));
        setDragState(2);
        return true;
    }

    private int getEdgesTouched(int i, int i2) {
        int i3 = 0;
        if (i < this.mParentView.getLeft() + this.mEdgeSize) {
            i3 = 1;
        }
        int i4 = i3;
        if (i2 < this.mParentView.getTop() + this.mEdgeSize) {
            i4 = i3 | 4;
        }
        int i5 = i4;
        if (i > this.mParentView.getRight() - this.mEdgeSize) {
            i5 = i4 | 2;
        }
        int i6 = i5;
        if (i2 > this.mParentView.getBottom() - this.mEdgeSize) {
            i6 = i5 | 8;
        }
        return i6;
    }

    private boolean isValidPointerForActionMove(int i) {
        if (isPointerDown(i)) {
            return true;
        }
        Log.e("ViewDragHelper", "Ignoring pointerId=" + i + " because ACTION_DOWN was not received for this pointer before ACTION_MOVE. It likely happened because  ViewDragHelper did not receive all the events in the event stream.");
        return false;
    }

    private void releaseViewForPointerUp() {
        this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaxVelocity);
        dispatchViewReleased(clampMag(VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity), clampMag(VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity));
    }

    private void reportNewEdgeDrags(float f, float f2, int i) {
        int i2 = 0;
        if (checkNewEdgeDrag(f, f2, i, 1)) {
            i2 = 1;
        }
        int i3 = i2;
        if (checkNewEdgeDrag(f2, f, i, 4)) {
            i3 = i2 | 4;
        }
        int i4 = i3;
        if (checkNewEdgeDrag(f, f2, i, 2)) {
            i4 = i3 | 2;
        }
        int i5 = i4;
        if (checkNewEdgeDrag(f2, f, i, 8)) {
            i5 = i4 | 8;
        }
        if (i5 != 0) {
            int[] iArr = this.mEdgeDragsInProgress;
            iArr[i] = iArr[i] | i5;
            this.mCallback.onEdgeDragStarted(i5, i);
        }
    }

    private void saveInitialMotion(float f, float f2, int i) {
        ensureMotionHistorySizeForId(i);
        float[] fArr = this.mInitialMotionX;
        this.mLastMotionX[i] = f;
        fArr[i] = f;
        float[] fArr2 = this.mInitialMotionY;
        this.mLastMotionY[i] = f2;
        fArr2[i] = f2;
        this.mInitialEdgesTouched[i] = getEdgesTouched((int) f, (int) f2);
        this.mPointersDown |= 1 << i;
    }

    private void saveLastMotion(MotionEvent motionEvent) {
        int pointerCount = MotionEventCompat.getPointerCount(motionEvent);
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = MotionEventCompat.getPointerId(motionEvent, i);
            if (isValidPointerForActionMove(pointerId)) {
                float x = MotionEventCompat.getX(motionEvent, i);
                float y = MotionEventCompat.getY(motionEvent, i);
                this.mLastMotionX[pointerId] = x;
                this.mLastMotionY[pointerId] = y;
            }
        }
    }

    public void abort() {
        cancel();
        if (this.mDragState == 2) {
            int currX = this.mScroller.getCurrX();
            int currY = this.mScroller.getCurrY();
            this.mScroller.abortAnimation();
            int currX2 = this.mScroller.getCurrX();
            int currY2 = this.mScroller.getCurrY();
            this.mCallback.onViewPositionChanged(this.mCapturedView, currX2, currY2, currX2 - currX, currY2 - currY);
        }
        setDragState(0);
    }

    public void cancel() {
        this.mActivePointerId = -1;
        clearMotionHistory();
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void captureChildView(View view, int i) {
        if (view.getParent() != this.mParentView) {
            throw new IllegalArgumentException("captureChildView: parameter must be a descendant of the ViewDragHelper's tracked parent view (" + this.mParentView + ")");
        }
        this.mCapturedView = view;
        this.mActivePointerId = i;
        this.mCallback.onViewCaptured(view, i);
        setDragState(1);
    }

    public boolean checkTouchSlop(int i) {
        int length = this.mInitialMotionX.length;
        for (int i2 = 0; i2 < length; i2++) {
            if (checkTouchSlop(i, i2)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTouchSlop(int i, int i2) {
        boolean z = true;
        if (isPointerDown(i2)) {
            boolean z2 = (i & 1) == 1;
            boolean z3 = (i & 2) == 2;
            float f = this.mLastMotionX[i2] - this.mInitialMotionX[i2];
            float f2 = this.mLastMotionY[i2] - this.mInitialMotionY[i2];
            if (z2 && z3) {
                if ((f * f) + (f2 * f2) <= this.mTouchSlop * this.mTouchSlop) {
                    z = false;
                }
                return z;
            } else if (z2) {
                return Math.abs(f) > ((float) this.mTouchSlop);
            } else if (z3) {
                return Math.abs(f2) > ((float) this.mTouchSlop);
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean continueSettling(boolean z) {
        if (this.mDragState == 2) {
            boolean computeScrollOffset = this.mScroller.computeScrollOffset();
            int currX = this.mScroller.getCurrX();
            int currY = this.mScroller.getCurrY();
            int left = currX - this.mCapturedView.getLeft();
            int top = currY - this.mCapturedView.getTop();
            if (left != 0) {
                ViewCompat.offsetLeftAndRight(this.mCapturedView, left);
            }
            if (top != 0) {
                ViewCompat.offsetTopAndBottom(this.mCapturedView, top);
            }
            if (left != 0 || top != 0) {
                this.mCallback.onViewPositionChanged(this.mCapturedView, currX, currY, left, top);
            }
            boolean z2 = computeScrollOffset;
            if (computeScrollOffset) {
                z2 = computeScrollOffset;
                if (currX == this.mScroller.getFinalX()) {
                    z2 = computeScrollOffset;
                    if (currY == this.mScroller.getFinalY()) {
                        this.mScroller.abortAnimation();
                        z2 = false;
                    }
                }
            }
            if (!z2) {
                if (z) {
                    this.mParentView.post(this.mSetIdleRunnable);
                } else {
                    setDragState(0);
                }
            }
        }
        return this.mDragState == 2;
    }

    public View findTopChildUnder(int i, int i2) {
        for (int childCount = this.mParentView.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = this.mParentView.getChildAt(this.mCallback.getOrderedChildIndex(childCount));
            if (i >= childAt.getLeft() && i < childAt.getRight() && i2 >= childAt.getTop() && i2 < childAt.getBottom()) {
                return childAt;
            }
        }
        return null;
    }

    public View getCapturedView() {
        return this.mCapturedView;
    }

    public int getEdgeSize() {
        return this.mEdgeSize;
    }

    public int getTouchSlop() {
        return this.mTouchSlop;
    }

    public int getViewDragState() {
        return this.mDragState;
    }

    public boolean isCapturedViewUnder(int i, int i2) {
        return isViewUnder(this.mCapturedView, i, i2);
    }

    public boolean isEdgeTouched(int i) {
        int length = this.mInitialEdgesTouched.length;
        for (int i2 = 0; i2 < length; i2++) {
            if (isEdgeTouched(i, i2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEdgeTouched(int i, int i2) {
        boolean z = false;
        if (isPointerDown(i2)) {
            z = false;
            if ((this.mInitialEdgesTouched[i2] & i) != 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean isPointerDown(int i) {
        boolean z = true;
        if ((this.mPointersDown & (1 << i)) == 0) {
            z = false;
        }
        return z;
    }

    public boolean isViewUnder(View view, int i, int i2) {
        if (view == null) {
            return false;
        }
        boolean z = false;
        if (i >= view.getLeft()) {
            z = false;
            if (i < view.getRight()) {
                z = false;
                if (i2 >= view.getTop()) {
                    z = false;
                    if (i2 < view.getBottom()) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public void processTouchEvent(MotionEvent motionEvent) {
        int i;
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
        if (actionMasked == 0) {
            cancel();
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        switch (actionMasked) {
            case 0:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                int pointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                View findTopChildUnder = findTopChildUnder((int) x, (int) y);
                saveInitialMotion(x, y, pointerId);
                tryCaptureViewForDrag(findTopChildUnder, pointerId);
                int i2 = this.mInitialEdgesTouched[pointerId];
                if ((this.mTrackingEdges & i2) != 0) {
                    this.mCallback.onEdgeTouched(this.mTrackingEdges & i2, pointerId);
                    return;
                }
                return;
            case 1:
                if (this.mDragState == 1) {
                    releaseViewForPointerUp();
                }
                cancel();
                return;
            case 2:
                if (this.mDragState == 1) {
                    if (isValidPointerForActionMove(this.mActivePointerId)) {
                        int findPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.mActivePointerId);
                        float x2 = MotionEventCompat.getX(motionEvent, findPointerIndex);
                        float y2 = MotionEventCompat.getY(motionEvent, findPointerIndex);
                        int i3 = (int) (x2 - this.mLastMotionX[this.mActivePointerId]);
                        int i4 = (int) (y2 - this.mLastMotionY[this.mActivePointerId]);
                        dragTo(this.mCapturedView.getLeft() + i3, this.mCapturedView.getTop() + i4, i3, i4);
                        saveLastMotion(motionEvent);
                        return;
                    }
                    return;
                }
                int pointerCount = MotionEventCompat.getPointerCount(motionEvent);
                for (int i5 = 0; i5 < pointerCount; i5++) {
                    int pointerId2 = MotionEventCompat.getPointerId(motionEvent, i5);
                    if (isValidPointerForActionMove(pointerId2)) {
                        float x3 = MotionEventCompat.getX(motionEvent, i5);
                        float y3 = MotionEventCompat.getY(motionEvent, i5);
                        float f = x3 - this.mInitialMotionX[pointerId2];
                        float f2 = y3 - this.mInitialMotionY[pointerId2];
                        reportNewEdgeDrags(f, f2, pointerId2);
                        if (this.mDragState != 1) {
                            View findTopChildUnder2 = findTopChildUnder((int) x3, (int) y3);
                            if (checkTouchSlop(findTopChildUnder2, f, f2) && tryCaptureViewForDrag(findTopChildUnder2, pointerId2)) {
                            }
                        }
                        saveLastMotion(motionEvent);
                        return;
                    }
                }
                saveLastMotion(motionEvent);
                return;
            case 3:
                if (this.mDragState == 1) {
                    dispatchViewReleased(0.0f, 0.0f);
                }
                cancel();
                return;
            case 4:
            default:
                return;
            case 5:
                int pointerId3 = MotionEventCompat.getPointerId(motionEvent, actionIndex);
                float x4 = MotionEventCompat.getX(motionEvent, actionIndex);
                float y4 = MotionEventCompat.getY(motionEvent, actionIndex);
                saveInitialMotion(x4, y4, pointerId3);
                if (this.mDragState != 0) {
                    if (isCapturedViewUnder((int) x4, (int) y4)) {
                        tryCaptureViewForDrag(this.mCapturedView, pointerId3);
                        return;
                    }
                    return;
                }
                tryCaptureViewForDrag(findTopChildUnder((int) x4, (int) y4), pointerId3);
                int i6 = this.mInitialEdgesTouched[pointerId3];
                if ((this.mTrackingEdges & i6) != 0) {
                    this.mCallback.onEdgeTouched(this.mTrackingEdges & i6, pointerId3);
                    return;
                }
                return;
            case 6:
                int pointerId4 = MotionEventCompat.getPointerId(motionEvent, actionIndex);
                if (this.mDragState == 1 && pointerId4 == this.mActivePointerId) {
                    int pointerCount2 = MotionEventCompat.getPointerCount(motionEvent);
                    int i7 = 0;
                    while (true) {
                        if (i7 < pointerCount2) {
                            int pointerId5 = MotionEventCompat.getPointerId(motionEvent, i7);
                            if (pointerId5 != this.mActivePointerId) {
                                i = (findTopChildUnder((int) MotionEventCompat.getX(motionEvent, i7), (int) MotionEventCompat.getY(motionEvent, i7)) == this.mCapturedView && tryCaptureViewForDrag(this.mCapturedView, pointerId5)) ? this.mActivePointerId : -1;
                            }
                            i7++;
                        }
                    }
                    if (i == -1) {
                        releaseViewForPointerUp();
                    }
                }
                clearMotionHistory(pointerId4);
                return;
        }
    }

    void setDragState(int i) {
        this.mParentView.removeCallbacks(this.mSetIdleRunnable);
        if (this.mDragState != i) {
            this.mDragState = i;
            this.mCallback.onViewDragStateChanged(i);
            if (this.mDragState == 0) {
                this.mCapturedView = null;
            }
        }
    }

    public void setEdgeTrackingEnabled(int i) {
        this.mTrackingEdges = i;
    }

    public void setMinVelocity(float f) {
        this.mMinVelocity = f;
    }

    public boolean settleCapturedViewAt(int i, int i2) {
        if (this.mReleaseInProgress) {
            return forceSettleCapturedViewAt(i, i2, (int) VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), (int) VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId));
        }
        throw new IllegalStateException("Cannot settleCapturedViewAt outside of a call to Callback#onViewReleased");
    }

    /* JADX WARN: Code restructure failed: missing block: B:53:0x0204, code lost:
        if (r0 != r0) goto L63;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean shouldInterceptTouchEvent(MotionEvent motionEvent) {
        View findTopChildUnder;
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
        if (actionMasked == 0) {
            cancel();
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        switch (actionMasked) {
            case 0:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                int pointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                saveInitialMotion(x, y, pointerId);
                View findTopChildUnder2 = findTopChildUnder((int) x, (int) y);
                if (findTopChildUnder2 == this.mCapturedView && this.mDragState == 2) {
                    tryCaptureViewForDrag(findTopChildUnder2, pointerId);
                }
                int i = this.mInitialEdgesTouched[pointerId];
                if ((this.mTrackingEdges & i) != 0) {
                    this.mCallback.onEdgeTouched(this.mTrackingEdges & i, pointerId);
                    break;
                }
                break;
            case 1:
            case 3:
                cancel();
                break;
            case 2:
                if (this.mInitialMotionX != null && this.mInitialMotionY != null) {
                    int pointerCount = MotionEventCompat.getPointerCount(motionEvent);
                    for (int i2 = 0; i2 < pointerCount; i2++) {
                        int pointerId2 = MotionEventCompat.getPointerId(motionEvent, i2);
                        if (isValidPointerForActionMove(pointerId2)) {
                            float x2 = MotionEventCompat.getX(motionEvent, i2);
                            float y2 = MotionEventCompat.getY(motionEvent, i2);
                            float f = x2 - this.mInitialMotionX[pointerId2];
                            float f2 = y2 - this.mInitialMotionY[pointerId2];
                            View findTopChildUnder3 = findTopChildUnder((int) x2, (int) y2);
                            boolean checkTouchSlop = findTopChildUnder3 != null ? checkTouchSlop(findTopChildUnder3, f, f2) : false;
                            if (checkTouchSlop) {
                                int left = findTopChildUnder3.getLeft();
                                int clampViewPositionHorizontal = this.mCallback.clampViewPositionHorizontal(findTopChildUnder3, left + ((int) f), (int) f);
                                int top = findTopChildUnder3.getTop();
                                int clampViewPositionVertical = this.mCallback.clampViewPositionVertical(findTopChildUnder3, top + ((int) f2), (int) f2);
                                int viewHorizontalDragRange = this.mCallback.getViewHorizontalDragRange(findTopChildUnder3);
                                int viewVerticalDragRange = this.mCallback.getViewVerticalDragRange(findTopChildUnder3);
                                if (viewHorizontalDragRange != 0) {
                                    if (viewHorizontalDragRange > 0) {
                                        break;
                                    }
                                }
                                if (viewVerticalDragRange != 0) {
                                    if (viewVerticalDragRange > 0 && clampViewPositionVertical == top) {
                                    }
                                }
                                saveLastMotion(motionEvent);
                                break;
                            }
                            reportNewEdgeDrags(f, f2, pointerId2);
                            if (this.mDragState != 1) {
                                if (checkTouchSlop && tryCaptureViewForDrag(findTopChildUnder3, pointerId2)) {
                                }
                            }
                            saveLastMotion(motionEvent);
                        }
                    }
                    saveLastMotion(motionEvent);
                }
                break;
            case 5:
                int pointerId3 = MotionEventCompat.getPointerId(motionEvent, actionIndex);
                float x3 = MotionEventCompat.getX(motionEvent, actionIndex);
                float y3 = MotionEventCompat.getY(motionEvent, actionIndex);
                saveInitialMotion(x3, y3, pointerId3);
                if (this.mDragState == 0) {
                    int i3 = this.mInitialEdgesTouched[pointerId3];
                    if ((this.mTrackingEdges & i3) != 0) {
                        this.mCallback.onEdgeTouched(this.mTrackingEdges & i3, pointerId3);
                        break;
                    }
                } else if (this.mDragState == 2 && (findTopChildUnder = findTopChildUnder((int) x3, (int) y3)) == this.mCapturedView) {
                    tryCaptureViewForDrag(findTopChildUnder, pointerId3);
                    break;
                }
                break;
            case 6:
                clearMotionHistory(MotionEventCompat.getPointerId(motionEvent, actionIndex));
                break;
        }
        return this.mDragState == 1;
    }

    public boolean smoothSlideViewTo(View view, int i, int i2) {
        this.mCapturedView = view;
        this.mActivePointerId = -1;
        boolean forceSettleCapturedViewAt = forceSettleCapturedViewAt(i, i2, 0, 0);
        if (!forceSettleCapturedViewAt && this.mDragState == 0 && this.mCapturedView != null) {
            this.mCapturedView = null;
        }
        return forceSettleCapturedViewAt;
    }

    boolean tryCaptureViewForDrag(View view, int i) {
        if (view == this.mCapturedView && this.mActivePointerId == i) {
            return true;
        }
        if (view == null || !this.mCallback.tryCaptureView(view, i)) {
            return false;
        }
        this.mActivePointerId = i;
        captureChildView(view, i);
        return true;
    }
}
