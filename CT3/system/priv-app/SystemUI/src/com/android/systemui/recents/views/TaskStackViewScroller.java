package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.FloatProperty;
import android.util.Property;
import android.view.ViewDebug;
import android.widget.OverScroller;
import com.android.systemui.Interpolators;
import com.android.systemui.recents.misc.Utilities;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/recents/views/TaskStackViewScroller.class */
public class TaskStackViewScroller {
    private static final Property<TaskStackViewScroller, Float> STACK_SCROLL = new FloatProperty<TaskStackViewScroller>("stackScroll") { // from class: com.android.systemui.recents.views.TaskStackViewScroller.1
        @Override // android.util.Property
        public Float get(TaskStackViewScroller taskStackViewScroller) {
            return Float.valueOf(taskStackViewScroller.getStackScroll());
        }

        @Override // android.util.FloatProperty
        public void setValue(TaskStackViewScroller taskStackViewScroller, float f) {
            taskStackViewScroller.setStackScroll(f);
        }
    };
    TaskStackViewScrollerCallbacks mCb;
    Context mContext;
    float mFinalAnimatedScroll;
    float mFlingDownScrollP;
    int mFlingDownY;
    @ViewDebug.ExportedProperty(category = "recents")
    float mLastDeltaP = 0.0f;
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    ObjectAnimator mScrollAnimator;
    OverScroller mScroller;
    @ViewDebug.ExportedProperty(category = "recents")
    float mStackScrollP;

    /* loaded from: a.zip:com/android/systemui/recents/views/TaskStackViewScroller$TaskStackViewScrollerCallbacks.class */
    public interface TaskStackViewScrollerCallbacks {
        void onStackScrollChanged(float f, float f2, AnimationProps animationProps);
    }

    public TaskStackViewScroller(Context context, TaskStackViewScrollerCallbacks taskStackViewScrollerCallbacks, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        this.mContext = context;
        this.mCb = taskStackViewScrollerCallbacks;
        this.mScroller = new OverScroller(context);
        this.mLayoutAlgorithm = taskStackLayoutAlgorithm;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ObjectAnimator animateBoundScroll() {
        float stackScroll = getStackScroll();
        float boundedStackScroll = getBoundedStackScroll(stackScroll);
        if (Float.compare(boundedStackScroll, stackScroll) != 0) {
            animateScroll(boundedStackScroll, null);
        }
        return this.mScrollAnimator;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void animateScroll(float f, int i, Runnable runnable) {
        if (this.mScrollAnimator != null && this.mScrollAnimator.isRunning()) {
            setStackScroll(this.mFinalAnimatedScroll);
            this.mScroller.forceFinished(true);
        }
        stopScroller();
        stopBoundScrollAnimation();
        if (Float.compare(this.mStackScrollP, f) == 0) {
            if (runnable != null) {
                runnable.run();
                return;
            }
            return;
        }
        this.mFinalAnimatedScroll = f;
        this.mScrollAnimator = ObjectAnimator.ofFloat(this, STACK_SCROLL, getStackScroll(), f);
        this.mScrollAnimator.setDuration(i);
        this.mScrollAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        this.mScrollAnimator.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.recents.views.TaskStackViewScroller.2
            final TaskStackViewScroller this$0;
            final Runnable val$postRunnable;

            {
                this.this$0 = this;
                this.val$postRunnable = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$postRunnable != null) {
                    this.val$postRunnable.run();
                }
                this.this$0.mScrollAnimator.removeAllListeners();
            }
        });
        this.mScrollAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void animateScroll(float f, Runnable runnable) {
        animateScroll(f, this.mContext.getResources().getInteger(2131755066), runnable);
    }

    public boolean boundScroll() {
        float stackScroll = getStackScroll();
        float boundedStackScroll = getBoundedStackScroll(stackScroll);
        if (Float.compare(boundedStackScroll, stackScroll) != 0) {
            setStackScroll(boundedStackScroll);
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            this.mFlingDownScrollP += setDeltaStackScroll(this.mFlingDownScrollP, this.mLayoutAlgorithm.getDeltaPForY(this.mFlingDownY, this.mScroller.getCurrY()));
            return true;
        }
        return false;
    }

    public void dump(String str, PrintWriter printWriter) {
        printWriter.print(str);
        printWriter.print("TaskStackViewScroller");
        printWriter.print(" stackScroll:");
        printWriter.print(this.mStackScrollP);
        printWriter.println();
    }

    public void fling(float f, int i, int i2, int i3, int i4, int i5, int i6) {
        this.mFlingDownScrollP = f;
        this.mFlingDownY = i;
        this.mScroller.fling(0, i2, 0, i3, 0, 0, i4, i5, 0, i6);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getBoundedStackScroll(float f) {
        return Utilities.clamp(f, this.mLayoutAlgorithm.mMinScrollP, this.mLayoutAlgorithm.mMaxScrollP);
    }

    float getScrollAmountOutOfBounds(float f) {
        if (f < this.mLayoutAlgorithm.mMinScrollP) {
            return Math.abs(f - this.mLayoutAlgorithm.mMinScrollP);
        }
        if (f > this.mLayoutAlgorithm.mMaxScrollP) {
            return Math.abs(f - this.mLayoutAlgorithm.mMaxScrollP);
        }
        return 0.0f;
    }

    public float getStackScroll() {
        return this.mStackScrollP;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isScrollOutOfBounds() {
        boolean z = false;
        if (Float.compare(getScrollAmountOutOfBounds(this.mStackScrollP), 0.0f) != 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void reset() {
        this.mStackScrollP = 0.0f;
        this.mLastDeltaP = 0.0f;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetDeltaScroll() {
        this.mLastDeltaP = 0.0f;
    }

    public float setDeltaStackScroll(float f, float f2) {
        float f3 = f + f2;
        float updateFocusStateOnScroll = this.mLayoutAlgorithm.updateFocusStateOnScroll(this.mLastDeltaP + f, f3, this.mStackScrollP);
        setStackScroll(updateFocusStateOnScroll, AnimationProps.IMMEDIATE);
        this.mLastDeltaP = f2;
        return updateFocusStateOnScroll - f3;
    }

    public void setStackScroll(float f) {
        setStackScroll(f, AnimationProps.IMMEDIATE);
    }

    public void setStackScroll(float f, AnimationProps animationProps) {
        float f2 = this.mStackScrollP;
        this.mStackScrollP = f;
        if (this.mCb != null) {
            this.mCb.onStackScrollChanged(f2, this.mStackScrollP, animationProps);
        }
    }

    public boolean setStackScrollToInitialState() {
        boolean z = false;
        float f = this.mStackScrollP;
        setStackScroll(this.mLayoutAlgorithm.mInitialScrollP);
        if (Float.compare(f, this.mStackScrollP) != 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopBoundScrollAnimation() {
        Utilities.cancelAnimationWithoutCallbacks(this.mScrollAnimator);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopScroller() {
        if (this.mScroller.isFinished()) {
            return;
        }
        this.mScroller.abortAnimation();
    }
}
