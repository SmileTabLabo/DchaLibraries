package android.support.v4.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
/* loaded from: a.zip:android/support/v4/view/ViewPropertyAnimatorCompatJB.class */
class ViewPropertyAnimatorCompatJB {
    ViewPropertyAnimatorCompatJB() {
    }

    public static void setListener(View view, ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        if (viewPropertyAnimatorListener != null) {
            view.animate().setListener(new AnimatorListenerAdapter(viewPropertyAnimatorListener, view) { // from class: android.support.v4.view.ViewPropertyAnimatorCompatJB.1
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
}
