package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
/* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityNodeInfoCompatApi21.class */
class AccessibilityNodeInfoCompatApi21 {

    /* loaded from: a.zip:android/support/v4/view/accessibility/AccessibilityNodeInfoCompatApi21$CollectionItemInfo.class */
    static class CollectionItemInfo {
        CollectionItemInfo() {
        }

        public static boolean isSelected(Object obj) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) obj).isSelected();
        }
    }

    AccessibilityNodeInfoCompatApi21() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object newAccessibilityAction(int i, CharSequence charSequence) {
        return new AccessibilityNodeInfo.AccessibilityAction(i, charSequence);
    }

    public static Object obtainCollectionInfo(int i, int i2, boolean z, int i3) {
        return AccessibilityNodeInfo.CollectionInfo.obtain(i, i2, z, i3);
    }

    public static Object obtainCollectionItemInfo(int i, int i2, int i3, int i4, boolean z, boolean z2) {
        return AccessibilityNodeInfo.CollectionItemInfo.obtain(i, i2, i3, i4, z, z2);
    }

    public static boolean removeAction(Object obj, Object obj2) {
        return ((AccessibilityNodeInfo) obj).removeAction((AccessibilityNodeInfo.AccessibilityAction) obj2);
    }
}
