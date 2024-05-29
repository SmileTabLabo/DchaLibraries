package android.support.v4.view.accessibility;

import android.os.Build;
import android.view.accessibility.AccessibilityManager;
/* loaded from: classes.dex */
public final class AccessibilityManagerCompat {
    private static final AccessibilityManagerVersionImpl IMPL;

    /* loaded from: classes.dex */
    interface AccessibilityManagerVersionImpl {
        boolean isTouchExplorationEnabled(AccessibilityManager accessibilityManager);
    }

    /* loaded from: classes.dex */
    static class AccessibilityManagerStubImpl implements AccessibilityManagerVersionImpl {
        AccessibilityManagerStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityManagerCompat.AccessibilityManagerVersionImpl
        public boolean isTouchExplorationEnabled(AccessibilityManager manager) {
            return false;
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityManagerIcsImpl extends AccessibilityManagerStubImpl {
        AccessibilityManagerIcsImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityManagerCompat.AccessibilityManagerStubImpl, android.support.v4.view.accessibility.AccessibilityManagerCompat.AccessibilityManagerVersionImpl
        public boolean isTouchExplorationEnabled(AccessibilityManager manager) {
            return AccessibilityManagerCompatIcs.isTouchExplorationEnabled(manager);
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new AccessibilityManagerIcsImpl();
        } else {
            IMPL = new AccessibilityManagerStubImpl();
        }
    }

    public static boolean isTouchExplorationEnabled(AccessibilityManager manager) {
        return IMPL.isTouchExplorationEnabled(manager);
    }

    private AccessibilityManagerCompat() {
    }
}
