package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityEvent;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityEventCompatKitKat.class */
class AccessibilityEventCompatKitKat {
    AccessibilityEventCompatKitKat() {
    }

    public static int getContentChangeTypes(AccessibilityEvent accessibilityEvent) {
        return accessibilityEvent.getContentChangeTypes();
    }

    public static void setContentChangeTypes(AccessibilityEvent accessibilityEvent, int i) {
        accessibilityEvent.setContentChangeTypes(i);
    }
}
