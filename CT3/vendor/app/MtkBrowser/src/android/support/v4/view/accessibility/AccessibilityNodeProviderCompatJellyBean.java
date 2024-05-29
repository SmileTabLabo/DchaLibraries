package android.support.v4.view.accessibility;

import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import java.util.List;
/* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityNodeProviderCompatJellyBean.class */
class AccessibilityNodeProviderCompatJellyBean {

    /* loaded from: b.zip:android/support/v4/view/accessibility/AccessibilityNodeProviderCompatJellyBean$AccessibilityNodeInfoBridge.class */
    interface AccessibilityNodeInfoBridge {
        Object createAccessibilityNodeInfo(int i);

        List<Object> findAccessibilityNodeInfosByText(String str, int i);

        boolean performAction(int i, int i2, Bundle bundle);
    }

    AccessibilityNodeProviderCompatJellyBean() {
    }

    public static Object newAccessibilityNodeProviderBridge(AccessibilityNodeInfoBridge accessibilityNodeInfoBridge) {
        return new AccessibilityNodeProvider(accessibilityNodeInfoBridge) { // from class: android.support.v4.view.accessibility.AccessibilityNodeProviderCompatJellyBean.1
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
            public boolean performAction(int i, int i2, Bundle bundle) {
                return this.val$bridge.performAction(i, i2, bundle);
            }
        };
    }
}
