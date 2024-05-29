package android.support.v4.view.accessibility;

import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import java.util.List;
/* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityNodeProviderCompatKitKat.class */
class AccessibilityNodeProviderCompatKitKat {

    /* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityNodeProviderCompatKitKat$AccessibilityNodeInfoBridge.class */
    interface AccessibilityNodeInfoBridge {
        Object createAccessibilityNodeInfo(int i);

        List<Object> findAccessibilityNodeInfosByText(String str, int i);

        Object findFocus(int i);

        boolean performAction(int i, int i2, Bundle bundle);
    }

    AccessibilityNodeProviderCompatKitKat() {
    }

    public static Object newAccessibilityNodeProviderBridge(AccessibilityNodeInfoBridge accessibilityNodeInfoBridge) {
        return new AccessibilityNodeProvider(accessibilityNodeInfoBridge) { // from class: android.support.v4.view.accessibility.AccessibilityNodeProviderCompatKitKat.1
            final AccessibilityNodeInfoBridge val$bridge;

            {
                this.val$bridge = accessibilityNodeInfoBridge;
            }

            @Override // android.view.accessibility.AccessibilityNodeProvider
            public AccessibilityNodeInfo createAccessibilityNodeInfo(int i) {
                return (AccessibilityNodeInfo) this.val$bridge.createAccessibilityNodeInfo(i);
            }

            @Override // android.view.accessibility.AccessibilityNodeProvider
            public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String str, int i) {
                return this.val$bridge.findAccessibilityNodeInfosByText(str, i);
            }

            @Override // android.view.accessibility.AccessibilityNodeProvider
            public AccessibilityNodeInfo findFocus(int i) {
                return (AccessibilityNodeInfo) this.val$bridge.findFocus(i);
            }

            @Override // android.view.accessibility.AccessibilityNodeProvider
            public boolean performAction(int i, int i2, Bundle bundle) {
                return this.val$bridge.performAction(i, i2, bundle);
            }
        };
    }
}
