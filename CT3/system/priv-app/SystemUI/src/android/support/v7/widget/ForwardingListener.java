package android.support.v7.widget;

import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.view.menu.ShowableListMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
/* loaded from: a.zip:android/support/v7/widget/ForwardingListener.class */
public abstract class ForwardingListener implements View.OnTouchListener {
    private int mActivePointerId;
    private Runnable mDisallowIntercept;
    private boolean mForwarding;
    private final float mScaledTouchSlop;
    private final View mSrc;
    private Runnable mTriggerLongPress;
    private final int[] mTmpLocation = new int[2];
    private final int mTapTimeout = ViewConfiguration.getTapTimeout();
    private final int mLongPressTimeout = (this.mTapTimeout + ViewConfiguration.getLongPressTimeout()) / 2;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ForwardingListener$DisallowIntercept.class */
    public class DisallowIntercept implements Runnable {
        final ForwardingListener this$0;

        private DisallowIntercept(ForwardingListener forwardingListener) {
            this.this$0 = forwardingListener;
        }

        /* synthetic */ DisallowIntercept(ForwardingListener forwardingListener, DisallowIntercept disallowIntercept) {
            this(forwardingListener);
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mSrc.getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/ForwardingListener$TriggerLongPress.class */
    public class TriggerLongPress implements Runnable {
        final ForwardingListener this$0;

        private TriggerLongPress(ForwardingListener forwardingListener) {
            this.this$0 = forwardingListener;
        }

        /* synthetic */ TriggerLongPress(ForwardingListener forwardingListener, TriggerLongPress triggerLongPress) {
            this(forwardingListener);
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.onLongPress();
        }
    }

    public ForwardingListener(View view) {
        this.mSrc = view;
        this.mScaledTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
    }

    private void clearCallbacks() {
        if (this.mTriggerLongPress != null) {
            this.mSrc.removeCallbacks(this.mTriggerLongPress);
        }
        if (this.mDisallowIntercept != null) {
            this.mSrc.removeCallbacks(this.mDisallowIntercept);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLongPress() {
        clearCallbacks();
        View view = this.mSrc;
        if (view.isEnabled() && !view.isLongClickable() && onForwardingStarted()) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            long uptimeMillis = SystemClock.uptimeMillis();
            MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0);
            view.onTouchEvent(obtain);
            obtain.recycle();
            this.mForwarding = true;
        }
    }

    private boolean onTouchForwarded(MotionEvent motionEvent) {
        DropDownListView dropDownListView;
        View view = this.mSrc;
        ShowableListMenu popup = getPopup();
        if (popup == null || !popup.isShowing() || (dropDownListView = (DropDownListView) popup.getListView()) == null || !dropDownListView.isShown()) {
            return false;
        }
        MotionEvent obtainNoHistory = MotionEvent.obtainNoHistory(motionEvent);
        toGlobalMotionEvent(view, obtainNoHistory);
        toLocalMotionEvent(dropDownListView, obtainNoHistory);
        boolean onForwardedEvent = dropDownListView.onForwardedEvent(obtainNoHistory, this.mActivePointerId);
        obtainNoHistory.recycle();
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        boolean z = actionMasked != 1 ? actionMasked != 3 : false;
        if (!onForwardedEvent) {
            z = false;
        }
        return z;
    }

    private boolean onTouchObserved(MotionEvent motionEvent) {
        View view = this.mSrc;
        if (view.isEnabled()) {
            switch (MotionEventCompat.getActionMasked(motionEvent)) {
                case 0:
                    this.mActivePointerId = motionEvent.getPointerId(0);
                    if (this.mDisallowIntercept == null) {
                        this.mDisallowIntercept = new DisallowIntercept(this, null);
                    }
                    view.postDelayed(this.mDisallowIntercept, this.mTapTimeout);
                    if (this.mTriggerLongPress == null) {
                        this.mTriggerLongPress = new TriggerLongPress(this, null);
                    }
                    view.postDelayed(this.mTriggerLongPress, this.mLongPressTimeout);
                    return false;
                case 1:
                case 3:
                    clearCallbacks();
                    return false;
                case 2:
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex < 0 || pointInView(view, motionEvent.getX(findPointerIndex), motionEvent.getY(findPointerIndex), this.mScaledTouchSlop)) {
                        return false;
                    }
                    clearCallbacks();
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private static boolean pointInView(View view, float f, float f2, float f3) {
        boolean z = false;
        if (f >= (-f3)) {
            z = false;
            if (f2 >= (-f3)) {
                z = false;
                if (f < (view.getRight() - view.getLeft()) + f3) {
                    z = false;
                    if (f2 < (view.getBottom() - view.getTop()) + f3) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    private boolean toGlobalMotionEvent(View view, MotionEvent motionEvent) {
        int[] iArr = this.mTmpLocation;
        view.getLocationOnScreen(iArr);
        motionEvent.offsetLocation(iArr[0], iArr[1]);
        return true;
    }

    private boolean toLocalMotionEvent(View view, MotionEvent motionEvent) {
        int[] iArr = this.mTmpLocation;
        view.getLocationOnScreen(iArr);
        motionEvent.offsetLocation(-iArr[0], -iArr[1]);
        return true;
    }

    public abstract ShowableListMenu getPopup();

    protected boolean onForwardingStarted() {
        ShowableListMenu popup = getPopup();
        if (popup == null || popup.isShowing()) {
            return true;
        }
        popup.show();
        return true;
    }

    protected boolean onForwardingStopped() {
        ShowableListMenu popup = getPopup();
        if (popup == null || !popup.isShowing()) {
            return true;
        }
        popup.dismiss();
        return true;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        boolean z;
        boolean z2 = this.mForwarding;
        if (z2) {
            z = onTouchForwarded(motionEvent) || !onForwardingStopped();
        } else {
            boolean onForwardingStarted = onTouchObserved(motionEvent) ? onForwardingStarted() : false;
            z = onForwardingStarted;
            if (onForwardingStarted) {
                long uptimeMillis = SystemClock.uptimeMillis();
                MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0);
                this.mSrc.onTouchEvent(obtain);
                obtain.recycle();
                z = onForwardingStarted;
            }
        }
        this.mForwarding = z;
        return !z ? z2 : true;
    }
}
