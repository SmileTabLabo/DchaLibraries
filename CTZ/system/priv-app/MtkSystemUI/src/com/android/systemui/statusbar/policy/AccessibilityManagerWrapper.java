package com.android.systemui.statusbar.policy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.view.accessibility.AccessibilityManager;
import java.util.List;
/* loaded from: classes.dex */
public class AccessibilityManagerWrapper implements CallbackController<AccessibilityManager.AccessibilityServicesStateChangeListener> {
    private final AccessibilityManager mAccessibilityManager;

    public AccessibilityManagerWrapper(Context context) {
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(AccessibilityManager.AccessibilityServicesStateChangeListener accessibilityServicesStateChangeListener) {
        this.mAccessibilityManager.addAccessibilityServicesStateChangeListener(accessibilityServicesStateChangeListener, null);
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(AccessibilityManager.AccessibilityServicesStateChangeListener accessibilityServicesStateChangeListener) {
        this.mAccessibilityManager.removeAccessibilityServicesStateChangeListener(accessibilityServicesStateChangeListener);
    }

    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int i) {
        return this.mAccessibilityManager.getEnabledAccessibilityServiceList(i);
    }
}
