package android.support.v4.view.accessibility;

import android.os.Build;
import android.view.accessibility.AccessibilityManager;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityManagerCompat.class */
public final class AccessibilityManagerCompat {
    private static final AccessibilityManagerVersionImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityManagerCompat$AccessibilityManagerIcsImpl.class */
    static class AccessibilityManagerIcsImpl extends AccessibilityManagerStubImpl {
        AccessibilityManagerIcsImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityManagerCompat.AccessibilityManagerStubImpl, android.support.v4.view.accessibility.AccessibilityManagerCompat.AccessibilityManagerVersionImpl
        public boolean isTouchExplorationEnabled(AccessibilityManager accessibilityManager) {
            return AccessibilityManagerCompatIcs.isTouchExplorationEnabled(accessibilityManager);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityManagerCompat$AccessibilityManagerStubImpl.class */
    static class AccessibilityManagerStubImpl implements AccessibilityManagerVersionImpl {
        AccessibilityManagerStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityManagerCompat.AccessibilityManagerVersionImpl
        public boolean isTouchExplorationEnabled(AccessibilityManager accessibilityManager) {
            return false;
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityManagerCompat$AccessibilityManagerVersionImpl.class */
    interface AccessibilityManagerVersionImpl {
        boolean isTouchExplorationEnabled(AccessibilityManager accessibilityManager);
    }

    static {
        if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new AccessibilityManagerIcsImpl();
        } else {
            IMPL = new AccessibilityManagerStubImpl();
        }
    }

    private AccessibilityManagerCompat() {
    }

    public static boolean isTouchExplorationEnabled(AccessibilityManager accessibilityManager) {
        return IMPL.isTouchExplorationEnabled(accessibilityManager);
    }
}
