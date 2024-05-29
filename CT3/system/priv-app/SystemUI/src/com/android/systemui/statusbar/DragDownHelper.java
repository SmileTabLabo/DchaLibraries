package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
/* loaded from: a.zip:com/android/systemui/statusbar/DragDownHelper.class */
public class DragDownHelper {
    private ExpandHelper.Callback mCallback;
    private DragDownCallback mDragDownCallback;
    private boolean mDraggedFarEnough;
    private boolean mDraggingDown;
    private FalsingManager mFalsingManager;
    private View mHost;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastHeight;
    private int mMinDragDistance;
    private ExpandableView mStartingChild;
    private final int[] mTemp2 = new int[2];
    private float mTouchSlop;

    /* loaded from: a.zip:com/android/systemui/statusbar/DragDownHelper$DragDownCallback.class */
    public interface DragDownCallback {
        void onCrossedThreshold(boolean z);

        void onDragDownReset();

        boolean onDraggedDown(View view, int i);

        void onTouchSlopExceeded();

        void setEmptyDragAmount(float f);
    }

    public DragDownHelper(Context context, View view, ExpandHelper.Callback callback, DragDownCallback dragDownCallback) {
        this.mMinDragDistance = context.getResources().getDimensionPixelSize(2131689884);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mCallback = callback;
        this.mDragDownCallback = dragDownCallback;
        this.mHost = view;
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    private void cancelExpansion() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mLastHeight, 0.0f);
        ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofFloat.setDuration(375L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.DragDownHelper.2
            final DragDownHelper this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mDragDownCallback.setEmptyDragAmount(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.start();
    }

    private void cancelExpansion(ExpandableView expandableView) {
        if (expandableView.getActualHeight() == expandableView.getCollapsedHeight()) {
            this.mCallback.setUserLockedChild(expandableView, false);
            return;
        }
        ObjectAnimator ofInt = ObjectAnimator.ofInt(expandableView, "actualHeight", expandableView.getActualHeight(), expandableView.getCollapsedHeight());
        ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofInt.setDuration(375L);
        ofInt.addListener(new AnimatorListenerAdapter(this, expandableView) { // from class: com.android.systemui.statusbar.DragDownHelper.1
            final DragDownHelper this$0;
            final ExpandableView val$child;

            {
                this.this$0 = this;
                this.val$child = expandableView;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mCallback.setUserLockedChild(this.val$child, false);
            }
        });
        ofInt.start();
    }

    private void captureStartingChild(float f, float f2) {
        if (this.mStartingChild == null) {
            this.mStartingChild = findView(f, f2);
            if (this.mStartingChild != null) {
                this.mCallback.setUserLockedChild(this.mStartingChild, true);
            }
        }
    }

    private ExpandableView findView(float f, float f2) {
        this.mHost.getLocationOnScreen(this.mTemp2);
        return this.mCallback.getChildAtRawPosition(f + this.mTemp2[0], f2 + this.mTemp2[1]);
    }

    private void handleExpansion(float f, ExpandableView expandableView) {
        float f2 = f;
        if (f < 0.0f) {
            f2 = 0.0f;
        }
        boolean isContentExpandable = expandableView.isContentExpandable();
        float f3 = f2 * (isContentExpandable ? 0.5f : 0.15f);
        float f4 = f3;
        if (isContentExpandable) {
            f4 = f3;
            if (expandableView.getCollapsedHeight() + f3 > expandableView.getMaxContentHeight()) {
                f4 = f3 - (((expandableView.getCollapsedHeight() + f3) - expandableView.getMaxContentHeight()) * 0.85f);
            }
        }
        expandableView.setActualHeight((int) (expandableView.getCollapsedHeight() + f4));
    }

    private boolean isFalseTouch() {
        boolean z = true;
        if (!this.mFalsingManager.isFalseTouch()) {
            z = true;
            if (this.mDraggedFarEnough) {
                z = false;
            }
        }
        return z;
    }

    private void stopDragging() {
        this.mFalsingManager.onNotificatonStopDraggingDown();
        if (this.mStartingChild != null) {
            cancelExpansion(this.mStartingChild);
        } else {
            cancelExpansion();
        }
        this.mDraggingDown = false;
        this.mDragDownCallback.onDragDownReset();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        switch (motionEvent.getActionMasked()) {
            case 0:
                this.mDraggedFarEnough = false;
                this.mDraggingDown = false;
                this.mStartingChild = null;
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                return false;
            case 1:
            default:
                return false;
            case 2:
                float f = y - this.mInitialTouchY;
                if (f <= this.mTouchSlop || f <= Math.abs(x - this.mInitialTouchX)) {
                    return false;
                }
                this.mFalsingManager.onNotificatonStartDraggingDown();
                this.mDraggingDown = true;
                captureStartingChild(this.mInitialTouchX, this.mInitialTouchY);
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                this.mDragDownCallback.onTouchSlopExceeded();
                return true;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mDraggingDown) {
            motionEvent.getX();
            float y = motionEvent.getY();
            switch (motionEvent.getActionMasked()) {
                case 1:
                    if (isFalseTouch() || !this.mDragDownCallback.onDraggedDown(this.mStartingChild, (int) (y - this.mInitialTouchY))) {
                        stopDragging();
                        return false;
                    }
                    if (this.mStartingChild == null) {
                        this.mDragDownCallback.setEmptyDragAmount(0.0f);
                    } else {
                        this.mCallback.setUserLockedChild(this.mStartingChild, false);
                    }
                    this.mDraggingDown = false;
                    return false;
                case 2:
                    this.mLastHeight = y - this.mInitialTouchY;
                    captureStartingChild(this.mInitialTouchX, this.mInitialTouchY);
                    if (this.mStartingChild != null) {
                        handleExpansion(this.mLastHeight, this.mStartingChild);
                    } else {
                        this.mDragDownCallback.setEmptyDragAmount(this.mLastHeight);
                    }
                    if (this.mLastHeight > this.mMinDragDistance) {
                        if (this.mDraggedFarEnough) {
                            return true;
                        }
                        this.mDraggedFarEnough = true;
                        this.mDragDownCallback.onCrossedThreshold(true);
                        return true;
                    } else if (this.mDraggedFarEnough) {
                        this.mDraggedFarEnough = false;
                        this.mDragDownCallback.onCrossedThreshold(false);
                        return true;
                    } else {
                        return true;
                    }
                case 3:
                    stopDragging();
                    return false;
                default:
                    return false;
            }
        }
        return false;
    }
}
