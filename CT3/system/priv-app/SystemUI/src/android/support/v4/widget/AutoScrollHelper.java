package android.support.v4.widget;

import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
/* loaded from: a.zip:android/support/v4/widget/AutoScrollHelper.class */
public abstract class AutoScrollHelper implements View.OnTouchListener {
    private static final int DEFAULT_ACTIVATION_DELAY = ViewConfiguration.getTapTimeout();
    private int mActivationDelay;
    private boolean mAlreadyDelayed;
    private boolean mAnimating;
    private int mEdgeType;
    private boolean mEnabled;
    private boolean mExclusive;
    private boolean mNeedsCancel;
    private boolean mNeedsReset;
    private Runnable mRunnable;
    private final View mTarget;
    private final ClampedScroller mScroller = new ClampedScroller();
    private final Interpolator mEdgeInterpolator = new AccelerateInterpolator();
    private float[] mRelativeEdges = {0.0f, 0.0f};
    private float[] mMaximumEdges = {Float.MAX_VALUE, Float.MAX_VALUE};
    private float[] mRelativeVelocity = {0.0f, 0.0f};
    private float[] mMinimumVelocity = {0.0f, 0.0f};
    private float[] mMaximumVelocity = {Float.MAX_VALUE, Float.MAX_VALUE};

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v4/widget/AutoScrollHelper$ClampedScroller.class */
    public static class ClampedScroller {
        private int mEffectiveRampDown;
        private int mRampDownDuration;
        private int mRampUpDuration;
        private float mStopValue;
        private float mTargetVelocityX;
        private float mTargetVelocityY;
        private long mStartTime = Long.MIN_VALUE;
        private long mStopTime = -1;
        private long mDeltaTime = 0;
        private int mDeltaX = 0;
        private int mDeltaY = 0;

        private float getValueAt(long j) {
            if (j < this.mStartTime) {
                return 0.0f;
            }
            if (this.mStopTime < 0 || j < this.mStopTime) {
                return AutoScrollHelper.constrain(((float) (j - this.mStartTime)) / this.mRampUpDuration, 0.0f, 1.0f) * 0.5f;
            }
            return (1.0f - this.mStopValue) + (this.mStopValue * AutoScrollHelper.constrain(((float) (j - this.mStopTime)) / this.mEffectiveRampDown, 0.0f, 1.0f));
        }

        private float interpolateValue(float f) {
            return ((-4.0f) * f * f) + (4.0f * f);
        }

        public void computeScrollDelta() {
            if (this.mDeltaTime == 0) {
                throw new RuntimeException("Cannot compute scroll delta before calling start()");
            }
            long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
            float interpolateValue = interpolateValue(getValueAt(currentAnimationTimeMillis));
            long j = currentAnimationTimeMillis - this.mDeltaTime;
            this.mDeltaTime = currentAnimationTimeMillis;
            this.mDeltaX = (int) (((float) j) * interpolateValue * this.mTargetVelocityX);
            this.mDeltaY = (int) (((float) j) * interpolateValue * this.mTargetVelocityY);
        }

        public int getDeltaX() {
            return this.mDeltaX;
        }

        public int getDeltaY() {
            return this.mDeltaY;
        }

        public int getHorizontalDirection() {
            return (int) (this.mTargetVelocityX / Math.abs(this.mTargetVelocityX));
        }

        public int getVerticalDirection() {
            return (int) (this.mTargetVelocityY / Math.abs(this.mTargetVelocityY));
        }

        public boolean isFinished() {
            boolean z = false;
            if (this.mStopTime > 0) {
                z = false;
                if (AnimationUtils.currentAnimationTimeMillis() > this.mStopTime + this.mEffectiveRampDown) {
                    z = true;
                }
            }
            return z;
        }

        public void requestStop() {
            long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
            this.mEffectiveRampDown = AutoScrollHelper.constrain((int) (currentAnimationTimeMillis - this.mStartTime), 0, this.mRampDownDuration);
            this.mStopValue = getValueAt(currentAnimationTimeMillis);
            this.mStopTime = currentAnimationTimeMillis;
        }

        public void setRampDownDuration(int i) {
            this.mRampDownDuration = i;
        }

        public void setRampUpDuration(int i) {
            this.mRampUpDuration = i;
        }

        public void setTargetVelocity(float f, float f2) {
            this.mTargetVelocityX = f;
            this.mTargetVelocityY = f2;
        }

        public void start() {
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStopTime = -1L;
            this.mDeltaTime = this.mStartTime;
            this.mStopValue = 0.5f;
            this.mDeltaX = 0;
            this.mDeltaY = 0;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v4/widget/AutoScrollHelper$ScrollAnimationRunnable.class */
    public class ScrollAnimationRunnable implements Runnable {
        final AutoScrollHelper this$0;

        private ScrollAnimationRunnable(AutoScrollHelper autoScrollHelper) {
            this.this$0 = autoScrollHelper;
        }

        /* synthetic */ ScrollAnimationRunnable(AutoScrollHelper autoScrollHelper, ScrollAnimationRunnable scrollAnimationRunnable) {
            this(autoScrollHelper);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mAnimating) {
                if (this.this$0.mNeedsReset) {
                    this.this$0.mNeedsReset = false;
                    this.this$0.mScroller.start();
                }
                ClampedScroller clampedScroller = this.this$0.mScroller;
                if (clampedScroller.isFinished() || !this.this$0.shouldAnimate()) {
                    this.this$0.mAnimating = false;
                    return;
                }
                if (this.this$0.mNeedsCancel) {
                    this.this$0.mNeedsCancel = false;
                    this.this$0.cancelTargetTouch();
                }
                clampedScroller.computeScrollDelta();
                this.this$0.scrollTargetBy(clampedScroller.getDeltaX(), clampedScroller.getDeltaY());
                ViewCompat.postOnAnimation(this.this$0.mTarget, this);
            }
        }
    }

    public AutoScrollHelper(View view) {
        this.mTarget = view;
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int i = (int) ((displayMetrics.density * 1575.0f) + 0.5f);
        int i2 = (int) ((displayMetrics.density * 315.0f) + 0.5f);
        setMaximumVelocity(i, i);
        setMinimumVelocity(i2, i2);
        setEdgeType(1);
        setMaximumEdges(Float.MAX_VALUE, Float.MAX_VALUE);
        setRelativeEdges(0.2f, 0.2f);
        setRelativeVelocity(1.0f, 1.0f);
        setActivationDelay(DEFAULT_ACTIVATION_DELAY);
        setRampUpDuration(500);
        setRampDownDuration(500);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelTargetTouch() {
        long uptimeMillis = SystemClock.uptimeMillis();
        MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0);
        this.mTarget.onTouchEvent(obtain);
        obtain.recycle();
    }

    private float computeTargetVelocity(int i, float f, float f2, float f3) {
        float edgeValue = getEdgeValue(this.mRelativeEdges[i], f2, this.mMaximumEdges[i], f);
        if (edgeValue == 0.0f) {
            return 0.0f;
        }
        float f4 = this.mRelativeVelocity[i];
        float f5 = this.mMinimumVelocity[i];
        float f6 = this.mMaximumVelocity[i];
        float f7 = f4 * f3;
        return edgeValue > 0.0f ? constrain(edgeValue * f7, f5, f6) : -constrain((-edgeValue) * f7, f5, f6);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static float constrain(float f, float f2, float f3) {
        return f > f3 ? f3 : f < f2 ? f2 : f;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int constrain(int i, int i2, int i3) {
        return i > i3 ? i3 : i < i2 ? i2 : i;
    }

    private float constrainEdgeValue(float f, float f2) {
        if (f2 == 0.0f) {
            return 0.0f;
        }
        switch (this.mEdgeType) {
            case 0:
            case 1:
                if (f < f2) {
                    return f >= 0.0f ? 1.0f - (f / f2) : (this.mAnimating && this.mEdgeType == 1) ? 1.0f : 0.0f;
                }
                return 0.0f;
            case 2:
                if (f < 0.0f) {
                    return f / (-f2);
                }
                return 0.0f;
            default:
                return 0.0f;
        }
    }

    private float getEdgeValue(float f, float f2, float f3, float f4) {
        float interpolation;
        float constrain = constrain(f * f2, 0.0f, f3);
        float constrainEdgeValue = constrainEdgeValue(f2 - f4, constrain) - constrainEdgeValue(f4, constrain);
        if (constrainEdgeValue < 0.0f) {
            interpolation = -this.mEdgeInterpolator.getInterpolation(-constrainEdgeValue);
        } else if (constrainEdgeValue <= 0.0f) {
            return 0.0f;
        } else {
            interpolation = this.mEdgeInterpolator.getInterpolation(constrainEdgeValue);
        }
        return constrain(interpolation, -1.0f, 1.0f);
    }

    private void requestStop() {
        if (this.mNeedsReset) {
            this.mAnimating = false;
        } else {
            this.mScroller.requestStop();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldAnimate() {
        boolean z = false;
        ClampedScroller clampedScroller = this.mScroller;
        int verticalDirection = clampedScroller.getVerticalDirection();
        int horizontalDirection = clampedScroller.getHorizontalDirection();
        if (verticalDirection != 0 && canTargetScrollVertically(verticalDirection)) {
            z = true;
        } else if (horizontalDirection != 0) {
            z = canTargetScrollHorizontally(horizontalDirection);
        }
        return z;
    }

    private void startAnimating() {
        if (this.mRunnable == null) {
            this.mRunnable = new ScrollAnimationRunnable(this, null);
        }
        this.mAnimating = true;
        this.mNeedsReset = true;
        if (this.mAlreadyDelayed || this.mActivationDelay <= 0) {
            this.mRunnable.run();
        } else {
            ViewCompat.postOnAnimationDelayed(this.mTarget, this.mRunnable, this.mActivationDelay);
        }
        this.mAlreadyDelayed = true;
    }

    public abstract boolean canTargetScrollHorizontally(int i);

    public abstract boolean canTargetScrollVertically(int i);

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        boolean z = false;
        if (this.mEnabled) {
            switch (MotionEventCompat.getActionMasked(motionEvent)) {
                case 0:
                    this.mNeedsCancel = true;
                    this.mAlreadyDelayed = false;
                    this.mScroller.setTargetVelocity(computeTargetVelocity(0, motionEvent.getX(), view.getWidth(), this.mTarget.getWidth()), computeTargetVelocity(1, motionEvent.getY(), view.getHeight(), this.mTarget.getHeight()));
                    if (!this.mAnimating && shouldAnimate()) {
                        startAnimating();
                        break;
                    }
                    break;
                case 1:
                case 3:
                    requestStop();
                    break;
                case 2:
                    this.mScroller.setTargetVelocity(computeTargetVelocity(0, motionEvent.getX(), view.getWidth(), this.mTarget.getWidth()), computeTargetVelocity(1, motionEvent.getY(), view.getHeight(), this.mTarget.getHeight()));
                    if (!this.mAnimating) {
                        startAnimating();
                        break;
                    }
                    break;
            }
            if (this.mExclusive) {
                z = this.mAnimating;
            }
            return z;
        }
        return false;
    }

    public abstract void scrollTargetBy(int i, int i2);

    public AutoScrollHelper setActivationDelay(int i) {
        this.mActivationDelay = i;
        return this;
    }

    public AutoScrollHelper setEdgeType(int i) {
        this.mEdgeType = i;
        return this;
    }

    public AutoScrollHelper setEnabled(boolean z) {
        if (this.mEnabled && !z) {
            requestStop();
        }
        this.mEnabled = z;
        return this;
    }

    public AutoScrollHelper setMaximumEdges(float f, float f2) {
        this.mMaximumEdges[0] = f;
        this.mMaximumEdges[1] = f2;
        return this;
    }

    public AutoScrollHelper setMaximumVelocity(float f, float f2) {
        this.mMaximumVelocity[0] = f / 1000.0f;
        this.mMaximumVelocity[1] = f2 / 1000.0f;
        return this;
    }

    public AutoScrollHelper setMinimumVelocity(float f, float f2) {
        this.mMinimumVelocity[0] = f / 1000.0f;
        this.mMinimumVelocity[1] = f2 / 1000.0f;
        return this;
    }

    public AutoScrollHelper setRampDownDuration(int i) {
        this.mScroller.setRampDownDuration(i);
        return this;
    }

    public AutoScrollHelper setRampUpDuration(int i) {
        this.mScroller.setRampUpDuration(i);
        return this;
    }

    public AutoScrollHelper setRelativeEdges(float f, float f2) {
        this.mRelativeEdges[0] = f;
        this.mRelativeEdges[1] = f2;
        return this;
    }

    public AutoScrollHelper setRelativeVelocity(float f, float f2) {
        this.mRelativeVelocity[0] = f / 1000.0f;
        this.mRelativeVelocity[1] = f2 / 1000.0f;
        return this;
    }
}
