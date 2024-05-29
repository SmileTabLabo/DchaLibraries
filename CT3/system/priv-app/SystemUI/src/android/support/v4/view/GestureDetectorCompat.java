package android.support.v4.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
/* loaded from: a.zip:android/support/v4/view/GestureDetectorCompat.class */
public final class GestureDetectorCompat {
    private final GestureDetectorCompatImpl mImpl;

    /* loaded from: a.zip:android/support/v4/view/GestureDetectorCompat$GestureDetectorCompatImpl.class */
    interface GestureDetectorCompatImpl {
        boolean onTouchEvent(MotionEvent motionEvent);
    }

    /* loaded from: a.zip:android/support/v4/view/GestureDetectorCompat$GestureDetectorCompatImplBase.class */
    static class GestureDetectorCompatImplBase implements GestureDetectorCompatImpl {
        private boolean mAlwaysInBiggerTapRegion;
        private boolean mAlwaysInTapRegion;
        private MotionEvent mCurrentDownEvent;
        private boolean mDeferConfirmSingleTap;
        private GestureDetector.OnDoubleTapListener mDoubleTapListener;
        private int mDoubleTapSlopSquare;
        private float mDownFocusX;
        private float mDownFocusY;
        private final Handler mHandler;
        private boolean mInLongPress;
        private boolean mIsDoubleTapping;
        private boolean mIsLongpressEnabled;
        private float mLastFocusX;
        private float mLastFocusY;
        private final GestureDetector.OnGestureListener mListener;
        private int mMaximumFlingVelocity;
        private int mMinimumFlingVelocity;
        private MotionEvent mPreviousUpEvent;
        private boolean mStillDown;
        private int mTouchSlopSquare;
        private VelocityTracker mVelocityTracker;
        private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
        private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
        private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

        /* loaded from: a.zip:android/support/v4/view/GestureDetectorCompat$GestureDetectorCompatImplBase$GestureHandler.class */
        private class GestureHandler extends Handler {
            final GestureDetectorCompatImplBase this$1;

            GestureHandler(GestureDetectorCompatImplBase gestureDetectorCompatImplBase) {
                this.this$1 = gestureDetectorCompatImplBase;
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            GestureHandler(GestureDetectorCompatImplBase gestureDetectorCompatImplBase, Handler handler) {
                super(handler.getLooper());
                this.this$1 = gestureDetectorCompatImplBase;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        this.this$1.mListener.onShowPress(this.this$1.mCurrentDownEvent);
                        return;
                    case 2:
                        this.this$1.dispatchLongPress();
                        return;
                    case 3:
                        if (this.this$1.mDoubleTapListener != null) {
                            if (this.this$1.mStillDown) {
                                this.this$1.mDeferConfirmSingleTap = true;
                                return;
                            } else {
                                this.this$1.mDoubleTapListener.onSingleTapConfirmed(this.this$1.mCurrentDownEvent);
                                return;
                            }
                        }
                        return;
                    default:
                        throw new RuntimeException("Unknown message " + message);
                }
            }
        }

        public GestureDetectorCompatImplBase(Context context, GestureDetector.OnGestureListener onGestureListener, Handler handler) {
            if (handler != null) {
                this.mHandler = new GestureHandler(this, handler);
            } else {
                this.mHandler = new GestureHandler(this);
            }
            this.mListener = onGestureListener;
            if (onGestureListener instanceof GestureDetector.OnDoubleTapListener) {
                setOnDoubleTapListener((GestureDetector.OnDoubleTapListener) onGestureListener);
            }
            init(context);
        }

        private void cancel() {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(3);
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
            this.mIsDoubleTapping = false;
            this.mStillDown = false;
            this.mAlwaysInTapRegion = false;
            this.mAlwaysInBiggerTapRegion = false;
            this.mDeferConfirmSingleTap = false;
            if (this.mInLongPress) {
                this.mInLongPress = false;
            }
        }

        private void cancelTaps() {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(3);
            this.mIsDoubleTapping = false;
            this.mAlwaysInTapRegion = false;
            this.mAlwaysInBiggerTapRegion = false;
            this.mDeferConfirmSingleTap = false;
            if (this.mInLongPress) {
                this.mInLongPress = false;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void dispatchLongPress() {
            this.mHandler.removeMessages(3);
            this.mDeferConfirmSingleTap = false;
            this.mInLongPress = true;
            this.mListener.onLongPress(this.mCurrentDownEvent);
        }

        private void init(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null");
            }
            if (this.mListener == null) {
                throw new IllegalArgumentException("OnGestureListener must not be null");
            }
            this.mIsLongpressEnabled = true;
            ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
            int scaledTouchSlop = viewConfiguration.getScaledTouchSlop();
            int scaledDoubleTapSlop = viewConfiguration.getScaledDoubleTapSlop();
            this.mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
            this.mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
            this.mTouchSlopSquare = scaledTouchSlop * scaledTouchSlop;
            this.mDoubleTapSlopSquare = scaledDoubleTapSlop * scaledDoubleTapSlop;
        }

        private boolean isConsideredDoubleTap(MotionEvent motionEvent, MotionEvent motionEvent2, MotionEvent motionEvent3) {
            boolean z = false;
            if (this.mAlwaysInBiggerTapRegion && motionEvent3.getEventTime() - motionEvent2.getEventTime() <= DOUBLE_TAP_TIMEOUT) {
                int x = ((int) motionEvent.getX()) - ((int) motionEvent3.getX());
                int y = ((int) motionEvent.getY()) - ((int) motionEvent3.getY());
                if ((x * x) + (y * y) < this.mDoubleTapSlopSquare) {
                    z = true;
                }
                return z;
            }
            return false;
        }

        /* JADX WARN: Code restructure failed: missing block: B:102:0x048f, code lost:
            if (java.lang.Math.abs(r0) > r7.mMinimumFlingVelocity) goto L107;
         */
        /* JADX WARN: Code restructure failed: missing block: B:77:0x036b, code lost:
            if (java.lang.Math.abs(r0) >= 1.0f) goto L82;
         */
        @Override // android.support.v4.view.GestureDetectorCompat.GestureDetectorCompatImpl
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            this.mVelocityTracker.addMovement(motionEvent);
            boolean z = (action & 255) == 6;
            int actionIndex = z ? MotionEventCompat.getActionIndex(motionEvent) : -1;
            float f = 0.0f;
            float f2 = 0.0f;
            int pointerCount = MotionEventCompat.getPointerCount(motionEvent);
            for (int i = 0; i < pointerCount; i++) {
                if (actionIndex != i) {
                    f += MotionEventCompat.getX(motionEvent, i);
                    f2 += MotionEventCompat.getY(motionEvent, i);
                }
            }
            int i2 = z ? pointerCount - 1 : pointerCount;
            float f3 = f / i2;
            float f4 = f2 / i2;
            boolean z2 = false;
            switch (action & 255) {
                case 0:
                    boolean z3 = false;
                    if (this.mDoubleTapListener != null) {
                        boolean hasMessages = this.mHandler.hasMessages(3);
                        if (hasMessages) {
                            this.mHandler.removeMessages(3);
                        }
                        if (this.mCurrentDownEvent == null || this.mPreviousUpEvent == null || !hasMessages || !isConsideredDoubleTap(this.mCurrentDownEvent, this.mPreviousUpEvent, motionEvent)) {
                            this.mHandler.sendEmptyMessageDelayed(3, DOUBLE_TAP_TIMEOUT);
                            z3 = false;
                        } else {
                            this.mIsDoubleTapping = true;
                            z3 = this.mDoubleTapListener.onDoubleTap(this.mCurrentDownEvent) | this.mDoubleTapListener.onDoubleTapEvent(motionEvent);
                        }
                    }
                    this.mLastFocusX = f3;
                    this.mDownFocusX = f3;
                    this.mLastFocusY = f4;
                    this.mDownFocusY = f4;
                    if (this.mCurrentDownEvent != null) {
                        this.mCurrentDownEvent.recycle();
                    }
                    this.mCurrentDownEvent = MotionEvent.obtain(motionEvent);
                    this.mAlwaysInTapRegion = true;
                    this.mAlwaysInBiggerTapRegion = true;
                    this.mStillDown = true;
                    this.mInLongPress = false;
                    this.mDeferConfirmSingleTap = false;
                    if (this.mIsLongpressEnabled) {
                        this.mHandler.removeMessages(2);
                        this.mHandler.sendEmptyMessageAtTime(2, this.mCurrentDownEvent.getDownTime() + TAP_TIMEOUT + LONGPRESS_TIMEOUT);
                    }
                    this.mHandler.sendEmptyMessageAtTime(1, this.mCurrentDownEvent.getDownTime() + TAP_TIMEOUT);
                    z2 = z3 | this.mListener.onDown(motionEvent);
                    break;
                case 1:
                    this.mStillDown = false;
                    MotionEvent obtain = MotionEvent.obtain(motionEvent);
                    if (this.mIsDoubleTapping) {
                        z2 = this.mDoubleTapListener.onDoubleTapEvent(motionEvent);
                    } else if (this.mInLongPress) {
                        this.mHandler.removeMessages(3);
                        this.mInLongPress = false;
                        z2 = false;
                    } else if (this.mAlwaysInTapRegion) {
                        boolean onSingleTapUp = this.mListener.onSingleTapUp(motionEvent);
                        z2 = onSingleTapUp;
                        if (this.mDeferConfirmSingleTap) {
                            z2 = onSingleTapUp;
                            if (this.mDoubleTapListener != null) {
                                this.mDoubleTapListener.onSingleTapConfirmed(motionEvent);
                                z2 = onSingleTapUp;
                            }
                        }
                    } else {
                        VelocityTracker velocityTracker = this.mVelocityTracker;
                        int pointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                        velocityTracker.computeCurrentVelocity(1000, this.mMaximumFlingVelocity);
                        float yVelocity = VelocityTrackerCompat.getYVelocity(velocityTracker, pointerId);
                        float xVelocity = VelocityTrackerCompat.getXVelocity(velocityTracker, pointerId);
                        if (Math.abs(yVelocity) <= this.mMinimumFlingVelocity) {
                            z2 = false;
                            break;
                        }
                        z2 = this.mListener.onFling(this.mCurrentDownEvent, motionEvent, xVelocity, yVelocity);
                    }
                    if (this.mPreviousUpEvent != null) {
                        this.mPreviousUpEvent.recycle();
                    }
                    this.mPreviousUpEvent = obtain;
                    if (this.mVelocityTracker != null) {
                        this.mVelocityTracker.recycle();
                        this.mVelocityTracker = null;
                    }
                    this.mIsDoubleTapping = false;
                    this.mDeferConfirmSingleTap = false;
                    this.mHandler.removeMessages(1);
                    this.mHandler.removeMessages(2);
                    break;
                case 2:
                    z2 = false;
                    if (!this.mInLongPress) {
                        float f5 = this.mLastFocusX - f3;
                        float f6 = this.mLastFocusY - f4;
                        if (this.mIsDoubleTapping) {
                            z2 = this.mDoubleTapListener.onDoubleTapEvent(motionEvent);
                            break;
                        } else if (this.mAlwaysInTapRegion) {
                            int i3 = (int) (f3 - this.mDownFocusX);
                            int i4 = (int) (f4 - this.mDownFocusY);
                            int i5 = (i3 * i3) + (i4 * i4);
                            boolean z4 = false;
                            if (i5 > this.mTouchSlopSquare) {
                                z4 = this.mListener.onScroll(this.mCurrentDownEvent, motionEvent, f5, f6);
                                this.mLastFocusX = f3;
                                this.mLastFocusY = f4;
                                this.mAlwaysInTapRegion = false;
                                this.mHandler.removeMessages(3);
                                this.mHandler.removeMessages(1);
                                this.mHandler.removeMessages(2);
                            }
                            z2 = z4;
                            if (i5 > this.mTouchSlopSquare) {
                                this.mAlwaysInBiggerTapRegion = false;
                                z2 = z4;
                                break;
                            }
                        } else {
                            if (Math.abs(f5) < 1.0f) {
                                z2 = false;
                                break;
                            }
                            z2 = this.mListener.onScroll(this.mCurrentDownEvent, motionEvent, f5, f6);
                            this.mLastFocusX = f3;
                            this.mLastFocusY = f4;
                            break;
                        }
                    }
                    break;
                case 3:
                    cancel();
                    z2 = false;
                    break;
                case 4:
                    break;
                case 5:
                    this.mLastFocusX = f3;
                    this.mDownFocusX = f3;
                    this.mLastFocusY = f4;
                    this.mDownFocusY = f4;
                    cancelTaps();
                    z2 = false;
                    break;
                case 6:
                    this.mLastFocusX = f3;
                    this.mDownFocusX = f3;
                    this.mLastFocusY = f4;
                    this.mDownFocusY = f4;
                    this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaximumFlingVelocity);
                    int actionIndex2 = MotionEventCompat.getActionIndex(motionEvent);
                    int pointerId2 = MotionEventCompat.getPointerId(motionEvent, actionIndex2);
                    float xVelocity2 = VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, pointerId2);
                    float yVelocity2 = VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, pointerId2);
                    int i6 = 0;
                    while (true) {
                        z2 = false;
                        if (i6 >= pointerCount) {
                            break;
                        } else {
                            if (i6 != actionIndex2) {
                                int pointerId3 = MotionEventCompat.getPointerId(motionEvent, i6);
                                if ((xVelocity2 * VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, pointerId3)) + (yVelocity2 * VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, pointerId3)) < 0.0f) {
                                    this.mVelocityTracker.clear();
                                    z2 = false;
                                    break;
                                }
                            }
                            i6++;
                        }
                    }
                default:
                    z2 = false;
                    break;
            }
            return z2;
        }

        public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
            this.mDoubleTapListener = onDoubleTapListener;
        }
    }

    /* loaded from: a.zip:android/support/v4/view/GestureDetectorCompat$GestureDetectorCompatImplJellybeanMr2.class */
    static class GestureDetectorCompatImplJellybeanMr2 implements GestureDetectorCompatImpl {
        private final GestureDetector mDetector;

        public GestureDetectorCompatImplJellybeanMr2(Context context, GestureDetector.OnGestureListener onGestureListener, Handler handler) {
            this.mDetector = new GestureDetector(context, onGestureListener, handler);
        }

        @Override // android.support.v4.view.GestureDetectorCompat.GestureDetectorCompatImpl
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return this.mDetector.onTouchEvent(motionEvent);
        }
    }

    public GestureDetectorCompat(Context context, GestureDetector.OnGestureListener onGestureListener) {
        this(context, onGestureListener, null);
    }

    public GestureDetectorCompat(Context context, GestureDetector.OnGestureListener onGestureListener, Handler handler) {
        if (Build.VERSION.SDK_INT > 17) {
            this.mImpl = new GestureDetectorCompatImplJellybeanMr2(context, onGestureListener, handler);
        } else {
            this.mImpl = new GestureDetectorCompatImplBase(context, onGestureListener, handler);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mImpl.onTouchEvent(motionEvent);
    }
}
