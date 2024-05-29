package android.support.v4.view.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityRecord;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityRecordCompatJellyBean.class */
class AccessibilityRecordCompatJellyBean {
    AccessibilityRecordCompatJellyBean() {
    }

    public static void setSource(Object obj, View view, int i) {
        ((AccessibilityRecord) obj).setSource(view, i);
    }
}
