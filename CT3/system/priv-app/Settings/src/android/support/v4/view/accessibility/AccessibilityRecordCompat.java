package android.support.v4.view.accessibility;

import android.os.Build;
import android.view.View;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class AccessibilityRecordCompat {
    private static final AccessibilityRecordImpl IMPL;
    private final Object mRecord;

    /* loaded from: classes.dex */
    interface AccessibilityRecordImpl {
        List<CharSequence> getText(Object obj);

        void setChecked(Object obj, boolean z);

        void setClassName(Object obj, CharSequence charSequence);

        void setContentDescription(Object obj, CharSequence charSequence);

        void setEnabled(Object obj, boolean z);

        void setFromIndex(Object obj, int i);

        void setItemCount(Object obj, int i);

        void setMaxScrollX(Object obj, int i);

        void setMaxScrollY(Object obj, int i);

        void setPassword(Object obj, boolean z);

        void setScrollX(Object obj, int i);

        void setScrollY(Object obj, int i);

        void setScrollable(Object obj, boolean z);

        void setSource(Object obj, View view, int i);

        void setToIndex(Object obj, int i);
    }

    /* loaded from: classes.dex */
    static class AccessibilityRecordStubImpl implements AccessibilityRecordImpl {
        AccessibilityRecordStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public List<CharSequence> getText(Object record) {
            return Collections.emptyList();
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setChecked(Object record, boolean isChecked) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setClassName(Object record, CharSequence className) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setContentDescription(Object record, CharSequence contentDescription) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setEnabled(Object record, boolean isEnabled) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setFromIndex(Object record, int fromIndex) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setItemCount(Object record, int itemCount) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollX(Object record, int maxScrollX) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollY(Object record, int maxScrollY) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setPassword(Object record, boolean isPassword) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollX(Object record, int scrollX) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollY(Object record, int scrollY) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollable(Object record, boolean scrollable) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setSource(Object record, View root, int virtualDescendantId) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setToIndex(Object record, int toIndex) {
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityRecordIcsImpl extends AccessibilityRecordStubImpl {
        AccessibilityRecordIcsImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public List<CharSequence> getText(Object record) {
            return AccessibilityRecordCompatIcs.getText(record);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setChecked(Object record, boolean isChecked) {
            AccessibilityRecordCompatIcs.setChecked(record, isChecked);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setClassName(Object record, CharSequence className) {
            AccessibilityRecordCompatIcs.setClassName(record, className);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setContentDescription(Object record, CharSequence contentDescription) {
            AccessibilityRecordCompatIcs.setContentDescription(record, contentDescription);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setEnabled(Object record, boolean isEnabled) {
            AccessibilityRecordCompatIcs.setEnabled(record, isEnabled);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setFromIndex(Object record, int fromIndex) {
            AccessibilityRecordCompatIcs.setFromIndex(record, fromIndex);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setItemCount(Object record, int itemCount) {
            AccessibilityRecordCompatIcs.setItemCount(record, itemCount);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setPassword(Object record, boolean isPassword) {
            AccessibilityRecordCompatIcs.setPassword(record, isPassword);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollX(Object record, int scrollX) {
            AccessibilityRecordCompatIcs.setScrollX(record, scrollX);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollY(Object record, int scrollY) {
            AccessibilityRecordCompatIcs.setScrollY(record, scrollY);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setScrollable(Object record, boolean scrollable) {
            AccessibilityRecordCompatIcs.setScrollable(record, scrollable);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setToIndex(Object record, int toIndex) {
            AccessibilityRecordCompatIcs.setToIndex(record, toIndex);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityRecordIcsMr1Impl extends AccessibilityRecordIcsImpl {
        AccessibilityRecordIcsMr1Impl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollX(Object record, int maxScrollX) {
            AccessibilityRecordCompatIcsMr1.setMaxScrollX(record, maxScrollX);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setMaxScrollY(Object record, int maxScrollY) {
            AccessibilityRecordCompatIcsMr1.setMaxScrollY(record, maxScrollY);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityRecordJellyBeanImpl extends AccessibilityRecordIcsMr1Impl {
        AccessibilityRecordJellyBeanImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordStubImpl, android.support.v4.view.accessibility.AccessibilityRecordCompat.AccessibilityRecordImpl
        public void setSource(Object record, View root, int virtualDescendantId) {
            AccessibilityRecordCompatJellyBean.setSource(record, root, virtualDescendantId);
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
    public AccessibilityRecordCompat(Object record) {
        this.mRecord = record;
    }

    public void setSource(View root, int virtualDescendantId) {
        IMPL.setSource(this.mRecord, root, virtualDescendantId);
    }

    public void setChecked(boolean isChecked) {
        IMPL.setChecked(this.mRecord, isChecked);
    }

    public void setEnabled(boolean isEnabled) {
        IMPL.setEnabled(this.mRecord, isEnabled);
    }

    public void setPassword(boolean isPassword) {
        IMPL.setPassword(this.mRecord, isPassword);
    }

    public void setScrollable(boolean scrollable) {
        IMPL.setScrollable(this.mRecord, scrollable);
    }

    public void setItemCount(int itemCount) {
        IMPL.setItemCount(this.mRecord, itemCount);
    }

    public void setFromIndex(int fromIndex) {
        IMPL.setFromIndex(this.mRecord, fromIndex);
    }

    public void setToIndex(int toIndex) {
        IMPL.setToIndex(this.mRecord, toIndex);
    }

    public void setScrollX(int scrollX) {
        IMPL.setScrollX(this.mRecord, scrollX);
    }

    public void setScrollY(int scrollY) {
        IMPL.setScrollY(this.mRecord, scrollY);
    }

    public void setMaxScrollX(int maxScrollX) {
        IMPL.setMaxScrollX(this.mRecord, maxScrollX);
    }

    public void setMaxScrollY(int maxScrollY) {
        IMPL.setMaxScrollY(this.mRecord, maxScrollY);
    }

    public void setClassName(CharSequence className) {
        IMPL.setClassName(this.mRecord, className);
    }

    public List<CharSequence> getText() {
        return IMPL.getText(this.mRecord);
    }

    public void setContentDescription(CharSequence contentDescription) {
        IMPL.setContentDescription(this.mRecord, contentDescription);
    }

    public int hashCode() {
        if (this.mRecord == null) {
            return 0;
        }
        return this.mRecord.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            AccessibilityRecordCompat other = (AccessibilityRecordCompat) obj;
            if (this.mRecord == null) {
                if (other.mRecord != null) {
                    return false;
                }
            } else if (!this.mRecord.equals(other.mRecord)) {
                return false;
            }
            return true;
        }
        return false;
    }
}
