package android.support.v4.view;

import android.os.Bundle;
import android.view.View;
/* loaded from: a.zip:android/support/v4/view/ViewCompatJB.class */
class ViewCompatJB {
    ViewCompatJB() {
    }

    public static int getImportantForAccessibility(View view) {
        return view.getImportantForAccessibility();
    }

    public static int getMinimumHeight(View view) {
        return view.getMinimumHeight();
    }

    public static int getMinimumWidth(View view) {
        return view.getMinimumWidth();
    }

    public static boolean hasTransientState(View view) {
        return view.hasTransientState();
    }

    public static boolean performAccessibilityAction(View view, int i, Bundle bundle) {
        return view.performAccessibilityAction(i, bundle);
    }

    public static void postInvalidateOnAnimation(View view) {
        view.postInvalidateOnAnimation();
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        view.postOnAnimation(runnable);
    }

    public static void postOnAnimationDelayed(View view, Runnable runnable, long j) {
        view.postOnAnimationDelayed(runnable, j);
    }

    public static void setImportantForAccessibility(View view, int i) {
        view.setImportantForAccessibility(i);
    }
}
