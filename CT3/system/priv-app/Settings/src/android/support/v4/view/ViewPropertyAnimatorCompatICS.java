package android.support.v4.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
/* loaded from: classes.dex */
class ViewPropertyAnimatorCompatICS {
    ViewPropertyAnimatorCompatICS() {
    }

    public static void setDuration(View view, long value) {
        view.animate().setDuration(value);
    }

    public static void alpha(View view, float value) {
        view.animate().alpha(value);
    }

    public static void translationX(View view, float value) {
        view.animate().translationX(value);
    }

    public static void translationY(View view, float value) {
        view.animate().translationY(value);
    }

    public static void cancel(View view) {
        view.animate().cancel();
    }

    public static void start(View view) {
        view.animate().start();
    }

    public static void setListener(final View view, final ViewPropertyAnimatorListener listener) {
        if (listener != null) {
            view.animate().setListener(new AnimatorListenerAdapter() { // from class: android.support.v4.view.ViewPropertyAnimatorCompatICS.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    ViewPropertyAnimatorListener.this.onAnimationCancel(view);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    ViewPropertyAnimatorListener.this.onAnimationEnd(view);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    ViewPropertyAnimatorListener.this.onAnimationStart(view);
                }
            });
        } else {
            view.animate().setListener(null);
        }
    }
}
