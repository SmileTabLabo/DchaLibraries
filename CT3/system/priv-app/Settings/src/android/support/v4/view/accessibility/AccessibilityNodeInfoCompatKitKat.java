package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
/* loaded from: classes.dex */
class AccessibilityNodeInfoCompatKitKat {
    AccessibilityNodeInfoCompatKitKat() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Object getCollectionItemInfo(Object info) {
        return ((AccessibilityNodeInfo) info).getCollectionItemInfo();
    }

    public static void setCollectionInfo(Object info, Object collectionInfo) {
        ((AccessibilityNodeInfo) info).setCollectionInfo((AccessibilityNodeInfo.CollectionInfo) collectionInfo);
    }

    public static void setCollectionItemInfo(Object info, Object collectionItemInfo) {
        ((AccessibilityNodeInfo) info).setCollectionItemInfo((AccessibilityNodeInfo.CollectionItemInfo) collectionItemInfo);
    }

    public static Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
        return AccessibilityNodeInfo.CollectionInfo.obtain(rowCount, columnCount, hierarchical);
    }

    public static Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading) {
        return AccessibilityNodeInfo.CollectionItemInfo.obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading);
    }

    /* loaded from: classes.dex */
    static class CollectionItemInfo {
        CollectionItemInfo() {
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static int getColumnIndex(Object info) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) info).getColumnIndex();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static int getColumnSpan(Object info) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) info).getColumnSpan();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static int getRowIndex(Object info) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) info).getRowIndex();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static int getRowSpan(Object info) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) info).getRowSpan();
        }
    }
}
