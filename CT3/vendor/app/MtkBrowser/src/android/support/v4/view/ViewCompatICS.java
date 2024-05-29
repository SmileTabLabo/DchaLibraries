package android.support.v4.view;

import android.support.annotation.Nullable;
import android.view.View;
/* loaded from: b.zip:android/support/v4/view/ViewCompatICS.class */
class ViewCompatICS {
    ViewCompatICS() {
    }

    public static boolean canScrollHorizontally(View view, int i) {
        return view.canScrollHorizontally(i);
    }

    public static void setAccessibilityDelegate(View view, @Nullable Object obj) {
        view.setAccessibilityDelegate((View.AccessibilityDelegate) obj);
    }
}
