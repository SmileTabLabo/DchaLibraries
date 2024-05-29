package android.support.v4.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
/* loaded from: a.zip:android/support/v4/animation/HoneycombMr1AnimatorCompatProvider.class */
class HoneycombMr1AnimatorCompatProvider implements AnimatorProvider {
    private TimeInterpolator mDefaultInterpolator;

    /* loaded from: a.zip:android/support/v4/animation/HoneycombMr1AnimatorCompatProvider$AnimatorListenerCompatWrapper.class */
    static class AnimatorListenerCompatWrapper implements Animator.AnimatorListener {
        final ValueAnimatorCompat mValueAnimatorCompat;
        final AnimatorListenerCompat mWrapped;

        public AnimatorListenerCompatWrapper(AnimatorListenerCompat animatorListenerCompat, ValueAnimatorCompat valueAnimatorCompat) {
            this.mWrapped = animatorListenerCompat;
            this.mValueAnimatorCompat = valueAnimatorCompat;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            this.mWrapped.onAnimationCancel(this.mValueAnimatorCompat);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            this.mWrapped.onAnimationEnd(this.mValueAnimatorCompat);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
            this.mWrapped.onAnimationRepeat(this.mValueAnimatorCompat);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            this.mWrapped.onAnimationStart(this.mValueAnimatorCompat);
        }
    }

    /* loaded from: a.zip:android/support/v4/animation/HoneycombMr1AnimatorCompatProvider$HoneycombValueAnimatorCompat.class */
    static class HoneycombValueAnimatorCompat implements ValueAnimatorCompat {
        final Animator mWrapped;

        public HoneycombValueAnimatorCompat(Animator animator) {
            this.mWrapped = animator;
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void addListener(AnimatorListenerCompat animatorListenerCompat) {
            this.mWrapped.addListener(new AnimatorListenerCompatWrapper(animatorListenerCompat, this));
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void addUpdateListener(AnimatorUpdateListenerCompat animatorUpdateListenerCompat) {
            if (this.mWrapped instanceof ValueAnimator) {
                ((ValueAnimator) this.mWrapped).addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, animatorUpdateListenerCompat) { // from class: android.support.v4.animation.HoneycombMr1AnimatorCompatProvider.HoneycombValueAnimatorCompat.1
                    final HoneycombValueAnimatorCompat this$1;
                    final AnimatorUpdateListenerCompat val$animatorUpdateListener;

                    {
                        this.this$1 = this;
                        this.val$animatorUpdateListener = animatorUpdateListenerCompat;
                    }

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        this.val$animatorUpdateListener.onAnimationUpdate(this.this$1);
                    }
                });
            }
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void cancel() {
            this.mWrapped.cancel();
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public float getAnimatedFraction() {
            return ((ValueAnimator) this.mWrapped).getAnimatedFraction();
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void setDuration(long j) {
            this.mWrapped.setDuration(j);
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void setTarget(View view) {
            this.mWrapped.setTarget(view);
        }

        @Override // android.support.v4.animation.ValueAnimatorCompat
        public void start() {
            this.mWrapped.start();
        }
    }

    @Override // android.support.v4.animation.AnimatorProvider
    public void clearInterpolator(View view) {
        if (this.mDefaultInterpolator == null) {
            this.mDefaultInterpolator = new ValueAnimator().getInterpolator();
        }
        view.animate().setInterpolator(this.mDefaultInterpolator);
    }

    @Override // android.support.v4.animation.AnimatorProvider
    public ValueAnimatorCompat emptyValueAnimator() {
        return new HoneycombValueAnimatorCompat(ValueAnimator.ofFloat(0.0f, 1.0f));
    }
}
