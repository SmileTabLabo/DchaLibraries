package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.systemui.RecentsComponent;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarGestureHelper.class */
public class NavigationBarGestureHelper extends GestureDetector.SimpleOnGestureListener implements TunerService.Tunable {
    private Context mContext;
    private Divider mDivider;
    private boolean mDockWindowEnabled;
    private boolean mDockWindowTouchSlopExceeded;
    private boolean mDownOnRecents;
    private int mDragMode;
    private boolean mIsRTL;
    private boolean mIsVertical;
    private final int mMinFlingVelocity;
    private NavigationBarView mNavigationBarView;
    private RecentsComponent mRecentsComponent;
    private final int mScrollTouchSlop;
    private final GestureDetector mTaskSwitcherDetector;
    private int mTouchDownX;
    private int mTouchDownY;
    private VelocityTracker mVelocityTracker;

    public NavigationBarGestureHelper(Context context) {
        this.mContext = context;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mScrollTouchSlop = context.getResources().getDimensionPixelSize(2131689773);
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mTaskSwitcherDetector = new GestureDetector(context, this);
        TunerService.get(context).addTunable(this, "overview_nav_bar_gesture");
    }

    private int calculateDragMode() {
        if (!this.mIsVertical || this.mDivider.getView().isHorizontalDivision()) {
            return (this.mIsVertical || !this.mDivider.getView().isHorizontalDivision()) ? 0 : 1;
        }
        return 1;
    }

    private boolean handleDockWindowEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case 0:
                handleDragActionDownEvent(motionEvent);
                return true;
            case 1:
            case 3:
                handleDragActionUpEvent(motionEvent);
                return true;
            case 2:
                handleDragActionMoveEvent(motionEvent);
                return true;
            default:
                return true;
        }
    }

    private void handleDragActionDownEvent(MotionEvent motionEvent) {
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mVelocityTracker.addMovement(motionEvent);
        this.mDockWindowTouchSlopExceeded = false;
        this.mTouchDownX = (int) motionEvent.getX();
        this.mTouchDownY = (int) motionEvent.getY();
        if (this.mNavigationBarView != null) {
            View currentView = this.mNavigationBarView.getRecentsButton().getCurrentView();
            if (currentView == null) {
                this.mDownOnRecents = false;
                return;
            }
            boolean z = false;
            if (this.mTouchDownX >= currentView.getLeft()) {
                z = false;
                if (this.mTouchDownX <= currentView.getRight()) {
                    z = false;
                    if (this.mTouchDownY >= currentView.getTop()) {
                        z = false;
                        if (this.mTouchDownY <= currentView.getBottom()) {
                            z = true;
                        }
                    }
                }
            }
            this.mDownOnRecents = z;
        }
    }

    private boolean handleDragActionMoveEvent(MotionEvent motionEvent) {
        int i;
        Rect rect;
        this.mVelocityTracker.addMovement(motionEvent);
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int abs = Math.abs(x - this.mTouchDownX);
        int abs2 = Math.abs(y - this.mTouchDownY);
        if (this.mDivider == null || this.mRecentsComponent == null) {
            return false;
        }
        if (this.mDockWindowTouchSlopExceeded) {
            if (this.mDragMode == 1) {
                int rawY = (int) (!this.mIsVertical ? motionEvent.getRawY() : motionEvent.getRawX());
                DividerSnapAlgorithm.SnapTarget calculateSnapTarget = this.mDivider.getView().getSnapAlgorithm().calculateSnapTarget(rawY, 0.0f, false);
                this.mDivider.getView().resizeStack(rawY, calculateSnapTarget.position, calculateSnapTarget);
                return false;
            } else if (this.mDragMode == 0) {
                this.mRecentsComponent.onDraggingInRecents(motionEvent.getRawY());
                return false;
            } else {
                return false;
            }
        }
        boolean z = !this.mIsVertical ? abs2 > this.mScrollTouchSlop && abs2 > abs : abs > this.mScrollTouchSlop && abs > abs2;
        if (this.mDownOnRecents && z && this.mDivider.getView().getWindowManagerProxy().getDockSide() == -1) {
            int calculateDragMode = calculateDragMode();
            if (calculateDragMode == 1) {
                Rect rect2 = new Rect();
                this.mDivider.getView().calculateBoundsForPosition(this.mIsVertical ? (int) motionEvent.getRawX() : (int) motionEvent.getRawY(), this.mDivider.getView().isHorizontalDivision() ? 2 : 1, rect2);
                rect = rect2;
                i = 0;
            } else {
                i = 0;
                rect = null;
                if (calculateDragMode == 0) {
                    i = 0;
                    rect = null;
                    if (this.mTouchDownX < this.mContext.getResources().getDisplayMetrics().widthPixels / 2) {
                        i = 1;
                        rect = null;
                    }
                }
            }
            if (this.mRecentsComponent.dockTopTask(calculateDragMode, i, rect, 272)) {
                this.mDragMode = calculateDragMode;
                if (this.mDragMode == 1) {
                    this.mDivider.getView().startDragging(false, true);
                }
                this.mDockWindowTouchSlopExceeded = true;
                return true;
            }
            return false;
        }
        return false;
    }

    private void handleDragActionUpEvent(MotionEvent motionEvent) {
        this.mVelocityTracker.addMovement(motionEvent);
        this.mVelocityTracker.computeCurrentVelocity(1000);
        if (this.mDockWindowTouchSlopExceeded && this.mDivider != null && this.mRecentsComponent != null) {
            if (this.mDragMode == 1) {
                this.mDivider.getView().stopDragging(this.mIsVertical ? (int) motionEvent.getRawX() : (int) motionEvent.getRawY(), this.mIsVertical ? this.mVelocityTracker.getXVelocity() : this.mVelocityTracker.getYVelocity(), true, false);
            } else if (this.mDragMode == 0) {
                this.mRecentsComponent.onDraggingInRecentsEnded(this.mVelocityTracker.getYVelocity());
            }
        }
        this.mVelocityTracker.recycle();
        this.mVelocityTracker = null;
    }

    private boolean interceptDockWindowEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case 0:
                handleDragActionDownEvent(motionEvent);
                return false;
            case 1:
            case 3:
                handleDragActionUpEvent(motionEvent);
                return false;
            case 2:
                return handleDragActionMoveEvent(motionEvent);
            default:
                return false;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:19:0x004e, code lost:
        r8 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:36:0x0089, code lost:
        r8 = true;
     */
    @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        boolean z;
        boolean z2 = false;
        float abs = Math.abs(f);
        float abs2 = Math.abs(f2);
        if (abs <= this.mMinFlingVelocity || !this.mIsVertical ? abs > abs2 : abs2 > abs) {
            z2 = true;
        }
        if (!z2 || this.mRecentsComponent == null) {
            return true;
        }
        if (this.mIsRTL) {
            z = this.mIsVertical ? false : false;
        } else {
            z = this.mIsVertical ? false : false;
        }
        if (z) {
            this.mRecentsComponent.showNextAffiliatedTask();
            return true;
        }
        this.mRecentsComponent.showPrevAffiliatedTask();
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        this.mTaskSwitcherDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction() & 255) {
            case 0:
                this.mTouchDownX = (int) motionEvent.getX();
                this.mTouchDownY = (int) motionEvent.getY();
                break;
            case 2:
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                int abs = Math.abs(x - this.mTouchDownX);
                int abs2 = Math.abs(y - this.mTouchDownY);
                if (!this.mIsVertical ? abs > this.mScrollTouchSlop && abs > abs2 : abs2 > this.mScrollTouchSlop && abs2 > abs) {
                    return true;
                }
                break;
        }
        if (this.mDockWindowEnabled) {
            z = interceptDockWindowEvent(motionEvent);
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = this.mTaskSwitcherDetector.onTouchEvent(motionEvent);
        boolean z = onTouchEvent;
        if (this.mDockWindowEnabled) {
            z = onTouchEvent | handleDockWindowEvent(motionEvent);
        }
        return z;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if (str.equals("overview_nav_bar_gesture")) {
            boolean z = false;
            if (str2 != null) {
                z = false;
                if (Integer.parseInt(str2) != 0) {
                    z = true;
                }
            }
            this.mDockWindowEnabled = z;
        }
    }

    public void setBarState(boolean z, boolean z2) {
        this.mIsVertical = z;
        this.mIsRTL = z2;
    }

    public void setComponents(RecentsComponent recentsComponent, Divider divider, NavigationBarView navigationBarView) {
        this.mRecentsComponent = recentsComponent;
        this.mDivider = divider;
        this.mNavigationBarView = navigationBarView;
    }
}
