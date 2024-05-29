package android.support.v4.view.accessibility;

import android.os.Build;
import android.view.View;
import java.util.Collections;
import java.util.List;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat.class */
public class AccessibilityRecordCompat {
    private static final AccessibilityRecordImpl IMPL;
    private final Object mRecord;

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordIcsImpl.class */
    static class AccessibilityRecordIcsImpl extends AccessibilityRecordStubImpl {
        AccessibilityRecordIcsImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public List<CharSequence> getText(Object obj) {
            return AccessibilityRecordCompatIcs.getText(obj);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setChecked(Object obj, boolean z) {
            AccessibilityRecordCompatIcs.setChecked(obj, z);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setClassName(Object obj, CharSequence charSequence) {
            AccessibilityRecordCompatIcs.setClassName(obj, charSequence);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setContentDescription(Object obj, CharSequence charSequence) {
            AccessibilityRecordCompatIcs.setContentDescription(obj, charSequence);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setEnabled(Object obj, boolean z) {
            AccessibilityRecordCompatIcs.setEnabled(obj, z);
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
        public void setPassword(Object obj, boolean z) {
            AccessibilityRecordCompatIcs.setPassword(obj, z);
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
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordImpl.class */
    interface AccessibilityRecordImpl {
        List<CharSequence> getText(Object obj);

        void setChecked(Object obj, boolean z);

        void setClassName(Object obj, CharSequence charSequence);

        void setContentDescription(Object obj, CharSequence charSequence);

        void setEnabled(Object obj, boolean z);

        void setFromIndex(Object obj, int i);

        void setItemCount(Object obj, int i);

        void setPassword(Object obj, boolean z);

        void setScrollable(Object obj, boolean z);

        void setSource(Object obj, View view, int i);

        void setToIndex(Object obj, int i);
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordJellyBeanImpl.class */
    static class AccessibilityRecordJellyBeanImpl extends AccessibilityRecordIcsMr1Impl {
        AccessibilityRecordJellyBeanImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setSource(Object obj, View view, int i) {
            AccessibilityRecordCompatJellyBean.setSource(obj, view, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompat$AccessibilityRecordStubImpl.class */
    static class AccessibilityRecordStubImpl implements AccessibilityRecordImpl {
        AccessibilityRecordStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public List<CharSequence> getText(Object obj) {
            return Collections.emptyList();
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setChecked(Object obj, boolean z) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setClassName(Object obj, CharSequence charSequence) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setContentDescription(Object obj, CharSequence charSequence) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setEnabled(Object obj, boolean z) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setFromIndex(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setItemCount(Object obj, int i) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setPassword(Object obj, boolean z) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollable(Object obj, boolean z) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setSource(Object obj, View view, int i) {
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

    public List<CharSequence> getText() {
        return IMPL.getText(this.mRecord);
    }

    public int hashCode() {
        return this.mRecord == null ? 0 : this.mRecord.hashCode();
    }

    public void setChecked(boolean z) {
        IMPL.setChecked(this.mRecord, z);
    }

    public void setClassName(CharSequence charSequence) {
        IMPL.setClassName(this.mRecord, charSequence);
    }

    public void setContentDescription(CharSequence charSequence) {
        IMPL.setContentDescription(this.mRecord, charSequence);
    }

    public void setEnabled(boolean z) {
        IMPL.setEnabled(this.mRecord, z);
    }

    public void setFromIndex(int i) {
        IMPL.setFromIndex(this.mRecord, i);
    }

    public void setItemCount(int i) {
        IMPL.setItemCount(this.mRecord, i);
    }

    public void setPassword(boolean z) {
        IMPL.setPassword(this.mRecord, z);
    }

    public void setScrollable(boolean z) {
        IMPL.setScrollable(this.mRecord, z);
    }

    public void setSource(View view, int i) {
        IMPL.setSource(this.mRecord, view, i);
    }

    public void setToIndex(int i) {
        IMPL.setToIndex(this.mRecord, i);
    }
}
