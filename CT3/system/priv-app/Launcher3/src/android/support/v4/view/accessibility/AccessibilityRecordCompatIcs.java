package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityRecord;
import java.util.List;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompatIcs.class */
class AccessibilityRecordCompatIcs {
    AccessibilityRecordCompatIcs() {
    }

    public static List<CharSequence> getText(Object obj) {
        return ((AccessibilityRecord) obj).getText();
    }

    public static void setChecked(Object obj, boolean z) {
        ((AccessibilityRecord) obj).setChecked(z);
    }

    public static void setClassName(Object obj, CharSequence charSequence) {
        ((AccessibilityRecord) obj).setClassName(charSequence);
    }

    public static void setContentDescription(Object obj, CharSequence charSequence) {
        ((AccessibilityRecord) obj).setContentDescription(charSequence);
    }

    public static void setEnabled(Object obj, boolean z) {
        ((AccessibilityRecord) obj).setEnabled(z);
    }

    public static void setFromIndex(Object obj, int i) {
        ((AccessibilityRecord) obj).setFromIndex(i);
    }

    public static void setItemCount(Object obj, int i) {
        ((AccessibilityRecord) obj).setItemCount(i);
    }

    public static void setPassword(Object obj, boolean z) {
        ((AccessibilityRecord) obj).setPassword(z);
    }

    public static void setScrollable(Object obj, boolean z) {
        ((AccessibilityRecord) obj).setScrollable(z);
    }

    public static void setToIndex(Object obj, int i) {
        ((AccessibilityRecord) obj).setToIndex(i);
    }
}
