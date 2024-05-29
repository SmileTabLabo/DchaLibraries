package android.support.v4.animation;

import android.view.View;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:android/support/v4/animation/DonutAnimatorCompatProvider.class */
class DonutAnimatorCompatProvider implements AnimatorProvider {

    /* loaded from: a.zip:android/support/v4/animation/DonutAnimatorCompatProvider$DonutFloatValueAnimator.class */
    private static class DonutFloatValueAnimator implements ValueAnimatorCompat {
        private long mStartTime;
        View mTarget;
        List<AnimatorListenerCompat> mListeners = new ArrayList();
        List<AnimatorUpdateListenerCompat> mUpdateListeners = new ArrayList();
        private long mDuration = 200;
        private float mFraction = 0.0f;
        private boolean mStarted = false;
        private boolean mEnded = false;
        private Runnable mLoopRunnable = new Runnable(this) { // from class: android.support.v4.animation.DonutAnimatorCompatProvider.DonutFloatValueAnimator.1
            final DonutFloatValueAnimator this$1;

            {
                this.this$1 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                float time = (((float) (this.this$1.getTime() - this.this$1.mStartTime)) * 1.0f) / ((float) this.this$1.mDuration);
                if (time > 1.0f || this.this$1.mTarget.getParent() == null) {
                    time = 1.0f;
                }
                this.this$1.mFraction = time;
                this.this$1.notifyUpdateListeners();
                if (this.this$1.mFraction >= 1.0f) {
                    this.this$1.dispatchEnd();
                } else {
                    this.this$1.mTarget.postDelayed(this.this$1.mLoopRunnable, 16L);
                }
            }
        };

        private void dispatchCancel() {
            for (int size = this.mListeners.size() - 1; size >= 0; size--) {
                this.mListeners.get(size).onAnimationCancel(this);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void dispatchEnd() {
            for (int size = this.mListeners.size() - 1; size >= 0; size--) {
                this.mListeners.get(size).onAnimationEnd(this);
            }
        }

        private void dispatchStart() {
            for (int size = this.mListeners.size() - 1; size >= 0; size--) {
                this.mListeners.get(size).onAnimationStart(this);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public long getTime() {
            return this.mTarget.getDrawingTime();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void notifyUpdateListeners() {
            for (int size = this.mUpdateListeners.size() - 1; size >= 0; size--) {
                this.mUpdateListeners.get(size).onAnimationUpdate(this);
            }
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void addListener(AnimatorListenerCompat animatorListenerCompat) {
            this.mListeners.add(animatorListenerCompat);
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void addUpdateListener(AnimatorUpdateListenerCompat animatorUpdateListenerCompat) {
            this.mUpdateListeners.add(animatorUpdateListenerCompat);
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void cancel() {
            if (this.mEnded) {
                return;
            }
            this.mEnded = true;
            if (this.mStarted) {
                dispatchCancel();
            }
            dispatchEnd();
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public float getAnimatedFraction() {
            return this.mFraction;
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void setDuration(long j) {
            if (this.mStarted) {
                return;
            }
            this.mDuration = j;
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void setTarget(View view) {
            this.mTarget = view;
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void start() {
            if (this.mStarted) {
                return;
            }
            this.mStarted = true;
            dispatchStart();
            this.mFraction = 0.0f;
            this.mStartTime = getTime();
            this.mTarget.postDelayed(this.mLoopRunnable, 16L);
        }
    }

    @Override // android.support.v4.animation.AnimatorProvider
    public void clearInterpolator(View view) {
    }

    @Override // android.support.v4.animation.AnimatorProvider
    public ValueAnimatorCompat emptyValueAnimator() {
        return new DonutFloatValueAnimator();
    }
}
