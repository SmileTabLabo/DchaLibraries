package android.support.v4.view;

import android.view.View;
/* loaded from: b.zip:android/support/v4/view/ViewCompatJB.class */
class ViewCompatJB {
    ViewCompatJB() {
    }

    public static int getImportantForAccessibility(View view) {
        return view.getImportantForAccessibility();
    }

    public static void postInvalidateOnAnimation(View view) {
        view.postInvalidateOnAnimation();
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        view.postOnAnimation(runnable);
    }

    public static void setImportantForAccessibility(View view, int i) {
        view.setImportantForAccessibility(i);
    }
}
