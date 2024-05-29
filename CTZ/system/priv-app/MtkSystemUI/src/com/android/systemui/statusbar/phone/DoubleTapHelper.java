package com.android.systemui.statusbar.phone;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.R;
/* loaded from: classes.dex */
public class DoubleTapHelper {
    private boolean mActivated;
    private final ActivationListener mActivationListener;
    private float mActivationX;
    private float mActivationY;
    private final DoubleTapListener mDoubleTapListener;
    private final DoubleTapLogListener mDoubleTapLogListener;
    private float mDoubleTapSlop;
    private float mDownX;
    private float mDownY;
    private final SlideBackListener mSlideBackListener;
    private Runnable mTapTimeoutRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$DoubleTapHelper$GFsC9BR8swazZioXO_-_Yt7_6kU
        @Override // java.lang.Runnable
        public final void run() {
            DoubleTapHelper.this.makeInactive();
        }
    };
    private float mTouchSlop;
    private boolean mTrackTouch;
    private final View mView;

    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface ActivationListener {
        void onActiveChanged(boolean z);
    }

    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface DoubleTapListener {
        boolean onDoubleTap();
    }

    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface DoubleTapLogListener {
        void onDoubleTapLog(boolean z, float f, float f2);
    }

    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface SlideBackListener {
        boolean onSlideBack();
    }

    public DoubleTapHelper(View view, ActivationListener activationListener, DoubleTapListener doubleTapListener, SlideBackListener slideBackListener, DoubleTapLogListener doubleTapLogListener) {
        this.mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
        this.mDoubleTapSlop = view.getResources().getDimension(R.dimen.double_tap_slop);
        this.mView = view;
        this.mActivationListener = activationListener;
        this.mDoubleTapListener = doubleTapListener;
        this.mSlideBackListener = slideBackListener;
        this.mDoubleTapLogListener = doubleTapLogListener;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return onTouchEvent(motionEvent, Integer.MAX_VALUE);
    }

    public boolean onTouchEvent(MotionEvent motionEvent, int i) {
        switch (motionEvent.getActionMasked()) {
            case 0:
                this.mDownX = motionEvent.getX();
                this.mDownY = motionEvent.getY();
                this.mTrackTouch = true;
                if (this.mDownY > i) {
                    this.mTrackTouch = false;
                    break;
                }
                break;
            case 1:
                if (isWithinTouchSlop(motionEvent)) {
                    if (this.mSlideBackListener != null && this.mSlideBackListener.onSlideBack()) {
                        return true;
                    }
                    if (!this.mActivated) {
                        makeActive();
                        this.mView.postDelayed(this.mTapTimeoutRunnable, 1200L);
                        this.mActivationX = motionEvent.getX();
                        this.mActivationY = motionEvent.getY();
                        break;
                    } else {
                        boolean isWithinDoubleTapSlop = isWithinDoubleTapSlop(motionEvent);
                        if (this.mDoubleTapLogListener != null) {
                            this.mDoubleTapLogListener.onDoubleTapLog(isWithinDoubleTapSlop, motionEvent.getX() - this.mActivationX, motionEvent.getY() - this.mActivationY);
                        }
                        if (isWithinDoubleTapSlop) {
                            if (!this.mDoubleTapListener.onDoubleTap()) {
                                return false;
                            }
                        } else {
                            makeInactive();
                            this.mTrackTouch = false;
                            break;
                        }
                    }
                } else {
                    makeInactive();
                    this.mTrackTouch = false;
                    break;
                }
                break;
            case 2:
                if (!isWithinTouchSlop(motionEvent)) {
                    makeInactive();
                    this.mTrackTouch = false;
                    break;
                }
                break;
            case 3:
                makeInactive();
                this.mTrackTouch = false;
                break;
        }
        return this.mTrackTouch;
    }

    private void makeActive() {
        if (!this.mActivated) {
            this.mActivated = true;
            this.mActivationListener.onActiveChanged(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void makeInactive() {
        if (this.mActivated) {
            this.mActivated = false;
            this.mActivationListener.onActiveChanged(false);
        }
    }

    private boolean isWithinTouchSlop(MotionEvent motionEvent) {
        return Math.abs(motionEvent.getX() - this.mDownX) < this.mTouchSlop && Math.abs(motionEvent.getY() - this.mDownY) < this.mTouchSlop;
    }

    public boolean isWithinDoubleTapSlop(MotionEvent motionEvent) {
        if (this.mActivated) {
            return Math.abs(motionEvent.getX() - this.mActivationX) < this.mDoubleTapSlop && Math.abs(motionEvent.getY() - this.mActivationY) < this.mDoubleTapSlop;
        }
        return true;
    }
}
