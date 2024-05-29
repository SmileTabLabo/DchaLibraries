package android.support.v4.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
/* loaded from: a.zip:android/support/v4/view/ViewPropertyAnimatorCompatICS.class */
class ViewPropertyAnimatorCompatICS {
    ViewPropertyAnimatorCompatICS() {
    }

    public static void alpha(View view, float f) {
        view.animate().alpha(f);
    }

    public static void cancel(View view) {
        view.animate().cancel();
    }

    public static void setDuration(View view, long j) {
        view.animate().setDuration(j);
    }

    public static void setListener(View view, ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        if (viewPropertyAnimatorListener != null) {
            view.animate().setListener(new AnimatorListenerAdapter(viewPropertyAnimatorListener, view) { // from class: android.support.v4.view.ViewPropertyAnimatorCompatICS.1
                final ViewPropertyAnimatorListener val$listener;
                final View val$view;

                {
                    this.val$listener = viewPropertyAnimatorListener;
                    this.val$view = view;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.val$listener.onAnimationCancel(this.val$view);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$listener.onAnimationEnd(this.val$view);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.val$listener.onAnimationStart(this.val$view);
                }
            });
        } else {
            view.animate().setListener(null);
        }
    }

    public static void start(View view) {
        view.animate().start();
    }

    public static void translationX(View view, float f) {
        view.animate().translationX(f);
    }

    public static void translationY(View view, float f) {
        view.animate().translationY(f);
    }
}
