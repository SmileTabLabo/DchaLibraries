package android.support.v4.view.accessibility;

import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
/* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityEventCompat.class */
public final class AccessibilityEventCompat {
    private static final AccessibilityEventVersionImpl IMPL;

    /* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventIcsImpl.class */
    static class AccessibilityEventIcsImpl extends AccessibilityEventStubImpl {
        AccessibilityEventIcsImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventKitKatImpl.class */
    static class AccessibilityEventKitKatImpl extends AccessibilityEventIcsImpl {
        AccessibilityEventKitKatImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventStubImpl.class */
    static class AccessibilityEventStubImpl implements AccessibilityEventVersionImpl {
        AccessibilityEventStubImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventVersionImpl.class */
    interface AccessibilityEventVersionImpl {
    }

    static {
        if (Build.VERSION.SDK_INT >= 19) {
            IMPL = new AccessibilityEventKitKatImpl();
        } else if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new AccessibilityEventIcsImpl();
        } else {
            IMPL = new AccessibilityEventStubImpl();
        }
    }

    private AccessibilityEventCompat() {
    }

    public static AccessibilityRecordCompat asRecord(AccessibilityEvent accessibilityEvent) {
        return new AccessibilityRecordCompat(accessibilityEvent);
    }
}
