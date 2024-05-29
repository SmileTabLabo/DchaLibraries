package android.support.v4.view.accessibility;

import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
/* loaded from: classes.dex */
public final class AccessibilityEventCompat {
    private static final AccessibilityEventVersionImpl IMPL;

    /* loaded from: classes.dex */
    interface AccessibilityEventVersionImpl {
        int getContentChangeTypes(AccessibilityEvent accessibilityEvent);

        void setContentChangeTypes(AccessibilityEvent accessibilityEvent, int i);
    }

    /* loaded from: classes.dex */
    static class AccessibilityEventStubImpl implements AccessibilityEventVersionImpl {
        AccessibilityEventStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public void setContentChangeTypes(AccessibilityEvent event, int types) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public int getContentChangeTypes(AccessibilityEvent event) {
            return 0;
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityEventIcsImpl extends AccessibilityEventStubImpl {
        AccessibilityEventIcsImpl() {
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityEventKitKatImpl extends AccessibilityEventIcsImpl {
        AccessibilityEventKitKatImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventStubImpl, android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public void setContentChangeTypes(AccessibilityEvent event, int types) {
            AccessibilityEventCompatKitKat.setContentChangeTypes(event, types);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventStubImpl, android.support.v4.view.accessibility.AccessibilityEventCompat.AccessibilityEventVersionImpl
        public int getContentChangeTypes(AccessibilityEvent event) {
            return AccessibilityEventCompatKitKat.getContentChangeTypes(event);
        }
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

    public static AccessibilityRecordCompat asRecord(AccessibilityEvent event) {
        return new AccessibilityRecordCompat(event);
    }

    public static void setContentChangeTypes(AccessibilityEvent event, int changeTypes) {
        IMPL.setContentChangeTypes(event, changeTypes);
    }

    public static int getContentChangeTypes(AccessibilityEvent event) {
        return IMPL.getContentChangeTypes(event);
    }
}
