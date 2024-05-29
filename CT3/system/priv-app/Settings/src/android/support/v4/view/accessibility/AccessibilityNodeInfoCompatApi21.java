package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
/* loaded from: classes.dex */
class AccessibilityNodeInfoCompatApi21 {
    AccessibilityNodeInfoCompatApi21() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void addAction(Object info, Object action) {
        ((AccessibilityNodeInfo) info).addAction((AccessibilityNodeInfo.AccessibilityAction) action);
    }

    public static boolean removeAction(Object info, Object action) {
        return ((AccessibilityNodeInfo) info).removeAction((AccessibilityNodeInfo.AccessibilityAction) action);
    }

    public static Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
        return AccessibilityNodeInfo.CollectionInfo.obtain(rowCount, columnCount, hierarchical, selectionMode);
    }

    public static Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
        return AccessibilityNodeInfo.CollectionItemInfo.obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
    }

    /* loaded from: classes.dex */
    static class CollectionItemInfo {
        CollectionItemInfo() {
        }

        public static boolean isSelected(Object info) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) info).isSelected();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object newAccessibilityAction(int actionId, CharSequence label) {
        return new AccessibilityNodeInfo.AccessibilityAction(actionId, label);
    }
}
