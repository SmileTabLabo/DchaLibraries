package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityManager;
/* loaded from: classes.dex */
class AccessibilityManagerCompatIcs {
    AccessibilityManagerCompatIcs() {
    }

    public static boolean isTouchExplorationEnabled(AccessibilityManager manager) {
        return manager.isTouchExplorationEnabled();
    }
}
