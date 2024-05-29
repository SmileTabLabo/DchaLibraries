package android.support.v4.view.accessibility;

import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityEventCompat.class */
public final class AccessibilityEventCompat {
    private static final AccessibilityEventVersionImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventIcsImpl.class */
    static class AccessibilityEventIcsImpl extends AccessibilityEventStubImpl {
        AccessibilityEventIcsImpl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventKitKatImpl.class */
    static class AccessibilityEventKitKatImpl extends AccessibilityEventIcsImpl {
        AccessibilityEventKitKatImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventStubImpl, android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public int getContentChangeTypes(AccessibilityEvent accessibilityEvent) {
            return AccessibilityEventCompatKitKat.getContentChangeTypes(accessibilityEvent);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventStubImpl, android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public void setContentChangeTypes(AccessibilityEvent accessibilityEvent, int i) {
            AccessibilityEventCompatKitKat.setContentChangeTypes(accessibilityEvent, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventStubImpl.class */
    static class AccessibilityEventStubImpl implements AccessibilityEventVersionImpl {
        AccessibilityEventStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public int getContentChangeTypes(AccessibilityEvent accessibilityEvent) {
            return 0;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public void setContentChangeTypes(AccessibilityEvent accessibilityEvent, int i) {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityEventCompat$AccessibilityEventVersionImpl.class */
    interface AccessibilityEventVersionImpl {
        int getContentChangeTypes(AccessibilityEvent accessibilityEvent);

        void setContentChangeTypes(AccessibilityEvent accessibilityEvent, int i);
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

    public static int getContentChangeTypes(AccessibilityEvent accessibilityEvent) {
        return IMPL.getContentChangeTypes(accessibilityEvent);
    }

    public static void setContentChangeTypes(AccessibilityEvent accessibilityEvent, int i) {
        IMPL.setContentChangeTypes(accessibilityEvent, i);
    }
}
