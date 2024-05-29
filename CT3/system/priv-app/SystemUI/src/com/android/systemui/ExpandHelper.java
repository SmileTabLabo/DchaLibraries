package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.policy.ScrollAdapter;
/* loaded from: a.zip:com/android/systemui/ExpandHelper.class */
public class ExpandHelper {
    private Callback mCallback;
    private Context mContext;
    private float mCurrentHeight;
    private View mEventSource;
    private boolean mExpanding;
    private FlingAnimationUtils mFlingAnimationUtils;
    private boolean mHasPopped;
    private float mInitialTouchFocusY;
    private float mInitialTouchSpan;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private int mLargeSize;
    private float mLastFocusY;
    private float mLastMotionY;
    private float mLastSpanY;
    private float mMaximumStretch;
    private float mNaturalHeight;
    private float mOldHeight;
    private boolean mOnlyMovements;
    private float mPullGestureMinXSpan;
    private ExpandableView mResizedView;
    private ScaleGestureDetector mSGD;
    private ScrollAdapter mScrollAdapter;
    private int mSmallSize;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private boolean mWatchingForPull;
    private int mExpansionStyle = 0;
    private boolean mEnabled = true;
    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener(this) { // from class: com.android.systemui.ExpandHelper.1
        final ExpandHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            if (!this.this$0.mOnlyMovements) {
                this.this$0.startExpanding(this.this$0.mResizedView, 4);
            }
            return this.this$0.mExpanding;
        }

        @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }
    };
    private ViewScaler mScaler = new ViewScaler(this);
    private int mGravity = 48;
    private ObjectAnimator mScaleAnimation = ObjectAnimator.ofFloat(this.mScaler, "height", 0.0f);

    /* loaded from: a.zip:com/android/systemui/ExpandHelper$Callback.class */
    public interface Callback {
        boolean canChildBeExpanded(View view);

        void expansionStateChanged(boolean z);

        ExpandableView getChildAtPosition(float f, float f2);

        ExpandableView getChildAtRawPosition(float f, float f2);

        int getMaxExpandHeight(ExpandableView expandableView);

        void setExpansionCancelled(View view);

        void setUserExpandedChild(View view, boolean z);

        void setUserLockedChild(View view, boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/ExpandHelper$ViewScaler.class */
    public class ViewScaler {
        ExpandableView mView;
        final ExpandHelper this$0;

        public ViewScaler(ExpandHelper expandHelper) {
            this.this$0 = expandHelper;
        }

        public float getHeight() {
            return this.mView.getActualHeight();
        }

        public int getNaturalHeight() {
            return this.this$0.mCallback.getMaxExpandHeight(this.mView);
        }

        public void setHeight(float f) {
            this.mView.setActualHeight((int) f);
            this.this$0.mCurrentHeight = f;
        }

        public void setView(ExpandableView expandableView) {
            this.mView = expandableView;
        }
    }

    public ExpandHelper(Context context, Callback callback, int i, int i2) {
        this.mSmallSize = i;
        this.mMaximumStretch = this.mSmallSize * 2.0f;
        this.mLargeSize = i2;
        this.mContext = context;
        this.mCallback = callback;
        this.mPullGestureMinXSpan = this.mContext.getResources().getDimension(2131689822);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        this.mSGD = new ScaleGestureDetector(context, this.mScaleGestureListener);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.3f);
    }

    private float clamp(float f) {
        if (f < this.mSmallSize) {
            f = this.mSmallSize;
        }
        float f2 = f;
        if (f > this.mNaturalHeight) {
            f2 = this.mNaturalHeight;
        }
        return f2;
    }

    private void clearView() {
        this.mResizedView = null;
    }

    private ExpandableView findView(float f, float f2) {
        ExpandableView childAtPosition;
        if (this.mEventSource != null) {
            int[] iArr = new int[2];
            this.mEventSource.getLocationOnScreen(iArr);
            childAtPosition = this.mCallback.getChildAtRawPosition(f + iArr[0], f2 + iArr[1]);
        } else {
            childAtPosition = this.mCallback.getChildAtPosition(f, f2);
        }
        return childAtPosition;
    }

    private void finishExpanding(boolean z, float f) {
        if (this.mExpanding) {
            float height = this.mScaler.getHeight();
            this.mScaler.getHeight();
            boolean z2 = this.mOldHeight == ((float) this.mSmallSize);
            int naturalHeight = this.mScaler.getNaturalHeight();
            boolean z3 = (z2 ? z || (height > this.mOldHeight && f >= 0.0f) : !z && (height >= this.mOldHeight || f > 0.0f)) | (this.mNaturalHeight == ((float) this.mSmallSize));
            if (this.mScaleAnimation.isRunning()) {
                this.mScaleAnimation.cancel();
            }
            this.mCallback.expansionStateChanged(false);
            float f2 = z3 ? naturalHeight : this.mSmallSize;
            if (f2 != height) {
                this.mScaleAnimation.setFloatValues(f2);
                this.mScaleAnimation.setupStartValues();
                this.mScaleAnimation.addListener(new AnimatorListenerAdapter(this, this.mResizedView, z3) { // from class: com.android.systemui.ExpandHelper.2
                    public boolean mCancelled;
                    final ExpandHelper this$0;
                    final boolean val$expand;
                    final View val$scaledView;

                    {
                        this.this$0 = this;
                        this.val$scaledView = r5;
                        this.val$expand = z3;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationCancel(Animator animator) {
                        this.mCancelled = true;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        if (this.mCancelled) {
                            this.this$0.mCallback.setExpansionCancelled(this.val$scaledView);
                        } else {
                            this.this$0.mCallback.setUserExpandedChild(this.val$scaledView, this.val$expand);
                        }
                        this.this$0.mCallback.setUserLockedChild(this.val$scaledView, false);
                        this.this$0.mScaleAnimation.removeListener(this);
                    }
                });
                if (z3 != (f >= 0.0f)) {
                    f = 0.0f;
                }
                this.mFlingAnimationUtils.apply(this.mScaleAnimation, height, f2, f);
                this.mScaleAnimation.start();
            } else {
                this.mCallback.setUserExpandedChild(this.mResizedView, z3);
                this.mCallback.setUserLockedChild(this.mResizedView, false);
            }
            this.mExpanding = false;
            this.mExpansionStyle = 0;
        }
    }

    private float getCurrentVelocity() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            return this.mVelocityTracker.getYVelocity();
        }
        return 0.0f;
    }

    private boolean isEnabled() {
        return this.mEnabled;
    }

    private boolean isFullyExpanded(ExpandableView expandableView) {
        return expandableView.getIntrinsicHeight() == expandableView.getMaxContentHeight() ? expandableView.isSummaryWithChildren() ? expandableView.areChildrenExpanded() : true : false;
    }

    private boolean isInside(View view, float f, float f2) {
        int[] iArr;
        boolean z;
        int[] iArr2;
        boolean z2 = true;
        if (view == null) {
            return false;
        }
        float f3 = f;
        float f4 = f2;
        if (this.mEventSource != null) {
            this.mEventSource.getLocationOnScreen(new int[2]);
            f3 = f + iArr2[0];
            f4 = f2 + iArr2[1];
        }
        view.getLocationOnScreen(new int[2]);
        float f5 = f3 - iArr[0];
        float f6 = f4 - iArr[1];
        if (f5 <= 0.0f || f6 <= 0.0f) {
            z = false;
        } else {
            boolean z3 = f5 < ((float) view.getWidth());
            if (f6 >= view.getHeight()) {
                z2 = false;
            }
            z = z3 & z2;
        }
        return z;
    }

    private void maybeRecycleVelocityTracker(MotionEvent motionEvent) {
        if (this.mVelocityTracker != null) {
            if (motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1) {
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean startExpanding(ExpandableView expandableView, int i) {
        if (expandableView instanceof ExpandableNotificationRow) {
            this.mExpansionStyle = i;
            if (this.mExpanding && expandableView == this.mResizedView) {
                return true;
            }
            this.mExpanding = true;
            this.mCallback.expansionStateChanged(true);
            this.mCallback.setUserLockedChild(expandableView, true);
            this.mScaler.setView(expandableView);
            this.mOldHeight = this.mScaler.getHeight();
            this.mCurrentHeight = this.mOldHeight;
            if (!this.mCallback.canChildBeExpanded(expandableView)) {
                this.mNaturalHeight = this.mOldHeight;
                return true;
            }
            this.mNaturalHeight = this.mScaler.getNaturalHeight();
            this.mSmallSize = expandableView.getCollapsedHeight();
            return true;
        }
        return false;
    }

    private void trackVelocity(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case 0:
                if (this.mVelocityTracker == null) {
                    this.mVelocityTracker = VelocityTracker.obtain();
                } else {
                    this.mVelocityTracker.clear();
                }
                this.mVelocityTracker.addMovement(motionEvent);
                return;
            case 1:
            default:
                return;
            case 2:
                if (this.mVelocityTracker == null) {
                    this.mVelocityTracker = VelocityTracker.obtain();
                }
                this.mVelocityTracker.addMovement(motionEvent);
                return;
        }
    }

    private void updateExpansion() {
        float currentSpan = (this.mSGD.getCurrentSpan() - this.mInitialTouchSpan) * 1.0f;
        float focusY = (this.mSGD.getFocusY() - this.mInitialTouchFocusY) * 1.0f * (this.mGravity == 80 ? -1.0f : 1.0f);
        float abs = Math.abs(focusY) + Math.abs(currentSpan) + 1.0f;
        this.mScaler.setHeight(clamp(((Math.abs(focusY) * focusY) / abs) + ((Math.abs(currentSpan) * currentSpan) / abs) + this.mOldHeight));
        this.mLastFocusY = this.mSGD.getFocusY();
        this.mLastSpanY = this.mSGD.getCurrentSpan();
    }

    public void cancel() {
        finishExpanding(true, 0.0f);
        clearView();
        this.mSGD = new ScaleGestureDetector(this.mContext, this.mScaleGestureListener);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (isEnabled()) {
            trackVelocity(motionEvent);
            int action = motionEvent.getAction();
            this.mSGD.onTouchEvent(motionEvent);
            int focusX = (int) this.mSGD.getFocusX();
            int focusY = (int) this.mSGD.getFocusY();
            this.mInitialTouchFocusY = focusY;
            this.mInitialTouchSpan = this.mSGD.getCurrentSpan();
            this.mLastFocusY = this.mInitialTouchFocusY;
            this.mLastSpanY = this.mInitialTouchSpan;
            if (this.mExpanding) {
                this.mLastMotionY = motionEvent.getRawY();
                maybeRecycleVelocityTracker(motionEvent);
                return true;
            } else if (action != 2 || (this.mExpansionStyle & 1) == 0) {
                switch (action & 255) {
                    case 0:
                        this.mWatchingForPull = (this.mScrollAdapter == null || !isInside(this.mScrollAdapter.getHostView(), (float) focusX, (float) focusY)) ? false : this.mScrollAdapter.isScrolledToTop();
                        this.mResizedView = findView(focusX, focusY);
                        if (this.mResizedView != null && !this.mCallback.canChildBeExpanded(this.mResizedView)) {
                            this.mResizedView = null;
                            this.mWatchingForPull = false;
                        }
                        this.mInitialTouchY = motionEvent.getRawY();
                        this.mInitialTouchX = motionEvent.getRawX();
                        break;
                    case 1:
                    case 3:
                        finishExpanding(false, getCurrentVelocity());
                        clearView();
                        break;
                    case 2:
                        float currentSpanX = this.mSGD.getCurrentSpanX();
                        if (currentSpanX > this.mPullGestureMinXSpan && currentSpanX > this.mSGD.getCurrentSpanY() && !this.mExpanding) {
                            startExpanding(this.mResizedView, 2);
                            this.mWatchingForPull = false;
                        }
                        if (this.mWatchingForPull) {
                            float rawY = motionEvent.getRawY() - this.mInitialTouchY;
                            float rawX = motionEvent.getRawX();
                            float f = this.mInitialTouchX;
                            if (rawY > this.mTouchSlop && rawY > Math.abs(rawX - f)) {
                                this.mWatchingForPull = false;
                                if (this.mResizedView != null && !isFullyExpanded(this.mResizedView) && startExpanding(this.mResizedView, 1)) {
                                    this.mLastMotionY = motionEvent.getRawY();
                                    this.mInitialTouchY = motionEvent.getRawY();
                                    this.mHasPopped = false;
                                    break;
                                }
                            }
                        }
                        break;
                }
                this.mLastMotionY = motionEvent.getRawY();
                maybeRecycleVelocityTracker(motionEvent);
                return this.mExpanding;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isEnabled()) {
            trackVelocity(motionEvent);
            int actionMasked = motionEvent.getActionMasked();
            this.mSGD.onTouchEvent(motionEvent);
            int focusX = (int) this.mSGD.getFocusX();
            int focusY = (int) this.mSGD.getFocusY();
            if (this.mOnlyMovements) {
                this.mLastMotionY = motionEvent.getRawY();
                return false;
            }
            switch (actionMasked) {
                case 0:
                    this.mWatchingForPull = this.mScrollAdapter != null ? isInside(this.mScrollAdapter.getHostView(), focusX, focusY) : false;
                    this.mResizedView = findView(focusX, focusY);
                    this.mInitialTouchX = motionEvent.getRawX();
                    this.mInitialTouchY = motionEvent.getRawY();
                    break;
                case 1:
                case 3:
                    finishExpanding(false, getCurrentVelocity());
                    clearView();
                    break;
                case 2:
                    if (this.mWatchingForPull) {
                        float rawY = motionEvent.getRawY() - this.mInitialTouchY;
                        float rawX = motionEvent.getRawX();
                        float f = this.mInitialTouchX;
                        if (rawY > this.mTouchSlop && rawY > Math.abs(rawX - f)) {
                            this.mWatchingForPull = false;
                            if (this.mResizedView != null && !isFullyExpanded(this.mResizedView) && startExpanding(this.mResizedView, 1)) {
                                this.mInitialTouchY = motionEvent.getRawY();
                                this.mLastMotionY = motionEvent.getRawY();
                                this.mHasPopped = false;
                            }
                        }
                    }
                    if (this.mExpanding && (this.mExpansionStyle & 1) != 0) {
                        float rawY2 = (motionEvent.getRawY() - this.mLastMotionY) + this.mCurrentHeight;
                        float clamp = clamp(rawY2);
                        boolean z = false;
                        if (rawY2 > this.mNaturalHeight) {
                            z = true;
                        }
                        if (rawY2 < this.mSmallSize) {
                            z = true;
                        }
                        if (!this.mHasPopped) {
                            if (this.mEventSource != null) {
                                this.mEventSource.performHapticFeedback(1);
                            }
                            this.mHasPopped = true;
                        }
                        this.mScaler.setHeight(clamp);
                        this.mLastMotionY = motionEvent.getRawY();
                        if (z) {
                            this.mCallback.expansionStateChanged(false);
                            return true;
                        }
                        this.mCallback.expansionStateChanged(true);
                        return true;
                    } else if (this.mExpanding) {
                        updateExpansion();
                        this.mLastMotionY = motionEvent.getRawY();
                        return true;
                    }
                    break;
                case 5:
                case 6:
                    this.mInitialTouchY += this.mSGD.getFocusY() - this.mLastFocusY;
                    this.mInitialTouchSpan += this.mSGD.getCurrentSpan() - this.mLastSpanY;
                    break;
            }
            this.mLastMotionY = motionEvent.getRawY();
            maybeRecycleVelocityTracker(motionEvent);
            boolean z2 = false;
            if (this.mResizedView != null) {
                z2 = true;
            }
            return z2;
        }
        return false;
    }

    public void onlyObserveMovements(boolean z) {
        this.mOnlyMovements = z;
    }

    public void setEnabled(boolean z) {
        this.mEnabled = z;
    }

    public void setEventSource(View view) {
        this.mEventSource = view;
    }

    public void setScrollAdapter(ScrollAdapter scrollAdapter) {
        this.mScrollAdapter = scrollAdapter;
    }
}
