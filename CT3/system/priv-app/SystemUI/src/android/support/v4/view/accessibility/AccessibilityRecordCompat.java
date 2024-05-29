package android.support.v4.view.accessibility;

import android.os.Build;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat.class */
public class AccessibilityRecordCompat {
    private static final AccessibilityRecordImpl IMPL;
    private final Object mRecord;

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordIcsImpl.class */
    static class AccessibilityRecordIcsImpl extends AccessibilityRecordStubImpl {
        AccessibilityRecordIcsImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setFromIndex(Object obj, int i) {
            AccessibilityRecordCompatIcs.setFromIndex(obj, i);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setItemCount(Object obj, int i) {
            AccessibilityRecordCompatIcs.setItemCount(obj, i);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollX(Object obj, int i) {
            AccessibilityRecordCompatIcs.setScrollX(obj, i);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollY(Object obj, int i) {
            AccessibilityRecordCompatIcs.setScrollY(obj, i);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollable(Object obj, boolean z) {
            AccessibilityRecordCompatIcs.setScrollable(obj, z);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setToIndex(Object obj, int i) {
            AccessibilityRecordCompatIcs.setToIndex(obj, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordIcsMr1Impl.class */
    static class AccessibilityRecordIcsMr1Impl extends AccessibilityRecordIcsImpl {
        AccessibilityRecordIcsMr1Impl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollX(Object obj, int i) {
            AccessibilityRecordCompatIcsMr1.setMaxScrollX(obj, i);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollY(Object obj, int i) {
            AccessibilityRecordCompatIcsMr1.setMaxScrollY(obj, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordImpl.class */
    interface AccessibilityRecordImpl {
        void setFromIndex(Object obj, int i);

        void setItemCount(Object obj, int i);

        void setMaxScrollX(Object obj, int i);

        void setMaxScrollY(Object obj, int i);

        void setScrollX(Object obj, int i);

        void setScrollY(Object obj, int i);

        void setScrollable(Object obj, boolean z);

        void setToIndex(Object obj, int i);
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordJellyBeanImpl.class */
    static class AccessibilityRecordJellyBeanImpl extends AccessibilityRecordIcsMr1Impl {
        AccessibilityRecordJellyBeanImpl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordStubImpl.class */
    static class AccessibilityRecordStubImpl implements AccessibilityRecordImpl {
        AccessibilityRecordStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setFromIndex(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setItemCount(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollX(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollY(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollX(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollY(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollable(Object obj, boolean z) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setToIndex(Object obj, int i) {
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 16) {
            IMPL = new AccessibilityRecordJellyBeanImpl();
        } else if (Build.VERSION.SDK_INT >= 15) {
            IMPL = new AccessibilityRecordIcsMr1Impl();
        } else if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new AccessibilityRecordIcsImpl();
        } else {
            IMPL = new AccessibilityRecordStubImpl();
        }
    }

    @Deprecated
    public AccessibilityRecordCompat(Object obj) {
        this.mRecord = obj;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            AccessibilityRecordCompat accessibilityRecordCompat = (AccessibilityRecordCompat) obj;
            return this.mRecord == null ? accessibilityRecordCompat.mRecord == null : this.mRecord.equals(accessibilityRecordCompat.mRecord);
        }
        return false;
    }

    public int hashCode() {
        return this.mRecord == null ? 0 : this.mRecord.hashCode();
    }

    public void setFromIndex(int i) {
        IMPL.setFromIndex(this.mRecord, i);
    }

    public void setItemCount(int i) {
        IMPL.setItemCount(this.mRecord, i);
    }

    public void setMaxScrollX(int i) {
        IMPL.setMaxScrollX(this.mRecord, i);
    }

    public void setMaxScrollY(int i) {
        IMPL.setMaxScrollY(this.mRecord, i);
    }

    public void setScrollX(int i) {
        IMPL.setScrollX(this.mRecord, i);
    }

    public void setScrollY(int i) {
        IMPL.setScrollY(this.mRecord, i);
    }

    public void setScrollable(boolean z) {
        IMPL.setScrollable(this.mRecord, z);
    }

    public void setToIndex(int i) {
        IMPL.setToIndex(this.mRecord, i);
    }
}
