package com.android.systemui.statusbar.phone;

import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/HeadsUpTouchHelper.class */
public class HeadsUpTouchHelper {
    private boolean mCollapseSnoozes;
    private HeadsUpManager mHeadsUpManager;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private NotificationPanelView mPanel;
    private ExpandableNotificationRow mPickedChild;
    private NotificationStackScrollLayout mStackScroller;
    private float mTouchSlop;
    private boolean mTouchingHeadsUpView;
    private boolean mTrackingHeadsUp;
    private int mTrackingPointer;

    public HeadsUpTouchHelper(HeadsUpManager headsUpManager, NotificationStackScrollLayout notificationStackScrollLayout, NotificationPanelView notificationPanelView) {
        this.mHeadsUpManager = headsUpManager;
        this.mStackScroller = notificationStackScrollLayout;
        this.mPanel = notificationPanelView;
        this.mTouchSlop = ViewConfiguration.get(notificationStackScrollLayout.getContext()).getScaledTouchSlop();
    }

    private void endMotion() {
        this.mTrackingPointer = -1;
        this.mPickedChild = null;
        this.mTouchingHeadsUpView = false;
    }

    private void setTrackingHeadsUp(boolean z) {
        this.mTrackingHeadsUp = z;
        this.mHeadsUpManager.setTrackingHeadsUp(z);
        this.mPanel.setTrackingHeadsUp(z);
    }

    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    public void notifyFling(boolean z) {
        if (z && this.mCollapseSnoozes) {
            this.mHeadsUpManager.snooze();
        }
        this.mCollapseSnoozes = false;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.mTouchingHeadsUpView || motionEvent.getActionMasked() == 0) {
            int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
            int i = findPointerIndex;
            if (findPointerIndex < 0) {
                i = 0;
                this.mTrackingPointer = motionEvent.getPointerId(0);
            }
            float x = motionEvent.getX(i);
            float y = motionEvent.getY(i);
            switch (motionEvent.getActionMasked()) {
                case 0:
                    this.mInitialTouchY = y;
                    this.mInitialTouchX = x;
                    setTrackingHeadsUp(false);
                    ExpandableView childAtRawPosition = this.mStackScroller.getChildAtRawPosition(x, y);
                    this.mTouchingHeadsUpView = false;
                    if (childAtRawPosition instanceof ExpandableNotificationRow) {
                        this.mPickedChild = (ExpandableNotificationRow) childAtRawPosition;
                        this.mTouchingHeadsUpView = (this.mStackScroller.isExpanded() || !this.mPickedChild.isHeadsUp()) ? false : this.mPickedChild.isPinned();
                        return false;
                    }
                    return false;
                case 1:
                case 3:
                    if (this.mPickedChild != null && this.mTouchingHeadsUpView && this.mHeadsUpManager.shouldSwallowClick(this.mPickedChild.getStatusBarNotification().getKey())) {
                        endMotion();
                        return true;
                    }
                    endMotion();
                    return false;
                case 2:
                    float f = y - this.mInitialTouchY;
                    if (!this.mTouchingHeadsUpView || Math.abs(f) <= this.mTouchSlop || Math.abs(f) <= Math.abs(x - this.mInitialTouchX)) {
                        return false;
                    }
                    setTrackingHeadsUp(true);
                    if (f < 0.0f) {
                        z = true;
                    }
                    this.mCollapseSnoozes = z;
                    this.mInitialTouchX = x;
                    this.mInitialTouchY = y;
                    int actualHeight = this.mPickedChild.getActualHeight();
                    this.mPanel.setPanelScrimMinFraction(actualHeight / this.mPanel.getMaxPanelHeight());
                    this.mPanel.startExpandMotion(x, y, true, actualHeight);
                    this.mHeadsUpManager.unpinAll();
                    this.mPanel.clearNotificationEffects();
                    return true;
                case 4:
                case 5:
                default:
                    return false;
                case 6:
                    int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    if (this.mTrackingPointer == pointerId) {
                        int i2 = 1;
                        if (motionEvent.getPointerId(0) != pointerId) {
                            i2 = 0;
                        }
                        this.mTrackingPointer = motionEvent.getPointerId(i2);
                        this.mInitialTouchX = motionEvent.getX(i2);
                        this.mInitialTouchY = motionEvent.getY(i2);
                        return false;
                    }
                    return false;
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mTrackingHeadsUp) {
            switch (motionEvent.getActionMasked()) {
                case 1:
                case 3:
                    endMotion();
                    setTrackingHeadsUp(false);
                    return true;
                case 2:
                default:
                    return true;
            }
        }
        return false;
    }
}
