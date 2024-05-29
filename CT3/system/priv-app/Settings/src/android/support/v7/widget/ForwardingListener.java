package android.support.v7.widget;

import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.view.menu.ShowableListMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
/* loaded from: classes.dex */
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

    public abstract ShowableListMenu getPopup();

    public ForwardingListener(View src) {
        this.mSrc = src;
        this.mScaledTouchSlop = ViewConfiguration.get(src.getContext()).getScaledTouchSlop();
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View v, MotionEvent event) {
        boolean forwarding;
        boolean wasForwarding = this.mForwarding;
        if (wasForwarding) {
            forwarding = onTouchForwarded(event) || !onForwardingStopped();
        } else {
            forwarding = onTouchObserved(event) ? onForwardingStarted() : false;
            if (forwarding) {
                long now = SystemClock.uptimeMillis();
                MotionEvent e = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                this.mSrc.onTouchEvent(e);
                e.recycle();
            }
        }
        this.mForwarding = forwarding;
        if (forwarding) {
            return true;
        }
        return wasForwarding;
    }

    protected boolean onForwardingStarted() {
        ShowableListMenu popup = getPopup();
        if (popup != null && !popup.isShowing()) {
            popup.show();
            return true;
        }
        return true;
    }

    protected boolean onForwardingStopped() {
        ShowableListMenu popup = getPopup();
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
            return true;
        }
        return true;
    }

    private boolean onTouchObserved(MotionEvent srcEvent) {
        View src = this.mSrc;
        if (src.isEnabled()) {
            int actionMasked = MotionEventCompat.getActionMasked(srcEvent);
            switch (actionMasked) {
                case 0:
                    this.mActivePointerId = srcEvent.getPointerId(0);
                    if (this.mDisallowIntercept == null) {
                        this.mDisallowIntercept = new DisallowIntercept(this, null);
                    }
                    src.postDelayed(this.mDisallowIntercept, this.mTapTimeout);
                    if (this.mTriggerLongPress == null) {
                        this.mTriggerLongPress = new TriggerLongPress(this, null);
                    }
                    src.postDelayed(this.mTriggerLongPress, this.mLongPressTimeout);
                    break;
                case 1:
                case 3:
                    clearCallbacks();
                    break;
                case 2:
                    int activePointerIndex = srcEvent.findPointerIndex(this.mActivePointerId);
                    if (activePointerIndex >= 0) {
                        float x = srcEvent.getX(activePointerIndex);
                        float y = srcEvent.getY(activePointerIndex);
                        if (!pointInView(src, x, y, this.mScaledTouchSlop)) {
                            clearCallbacks();
                            src.getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }
        return false;
    }

    private void clearCallbacks() {
        if (this.mTriggerLongPress != null) {
            this.mSrc.removeCallbacks(this.mTriggerLongPress);
        }
        if (this.mDisallowIntercept == null) {
            return;
        }
        this.mSrc.removeCallbacks(this.mDisallowIntercept);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLongPress() {
        clearCallbacks();
        View src = this.mSrc;
        if (!src.isEnabled() || src.isLongClickable() || !onForwardingStarted()) {
            return;
        }
        src.getParent().requestDisallowInterceptTouchEvent(true);
        long now = SystemClock.uptimeMillis();
        MotionEvent e = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
        src.onTouchEvent(e);
        e.recycle();
        this.mForwarding = true;
    }

    private boolean onTouchForwarded(MotionEvent srcEvent) {
        DropDownListView dst;
        boolean keepForwarding;
        View src = this.mSrc;
        ShowableListMenu popup = getPopup();
        if (popup == null || !popup.isShowing() || (dst = (DropDownListView) popup.getListView()) == null || !dst.isShown()) {
            return false;
        }
        MotionEvent dstEvent = MotionEvent.obtainNoHistory(srcEvent);
        toGlobalMotionEvent(src, dstEvent);
        toLocalMotionEvent(dst, dstEvent);
        boolean handled = dst.onForwardedEvent(dstEvent, this.mActivePointerId);
        dstEvent.recycle();
        int action = MotionEventCompat.getActionMasked(srcEvent);
        if (action == 1) {
            keepForwarding = false;
        } else {
            keepForwarding = action != 3;
        }
        if (handled) {
            return keepForwarding;
        }
        return false;
    }

    private static boolean pointInView(View view, float localX, float localY, float slop) {
        return localX >= (-slop) && localY >= (-slop) && localX < ((float) (view.getRight() - view.getLeft())) + slop && localY < ((float) (view.getBottom() - view.getTop())) + slop;
    }

    private boolean toLocalMotionEvent(View view, MotionEvent event) {
        int[] loc = this.mTmpLocation;
        view.getLocationOnScreen(loc);
        event.offsetLocation(-loc[0], -loc[1]);
        return true;
    }

    private boolean toGlobalMotionEvent(View view, MotionEvent event) {
        int[] loc = this.mTmpLocation;
        view.getLocationOnScreen(loc);
        event.offsetLocation(loc[0], loc[1]);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DisallowIntercept implements Runnable {
        /* synthetic */ DisallowIntercept(ForwardingListener this$0, DisallowIntercept disallowIntercept) {
            this();
        }

        private DisallowIntercept() {
        }

        @Override // java.lang.Runnable
        public void run() {
            ViewParent parent = ForwardingListener.this.mSrc.getParent();
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class TriggerLongPress implements Runnable {
        /* synthetic */ TriggerLongPress(ForwardingListener this$0, TriggerLongPress triggerLongPress) {
            this();
        }

        private TriggerLongPress() {
        }

        @Override // java.lang.Runnable
        public void run() {
            ForwardingListener.this.onLongPress();
        }
    }
}
