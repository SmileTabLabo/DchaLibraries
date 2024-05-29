package com.android.launcher3.compat;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
/* loaded from: classes.dex */
public class AccessibilityManagerCompat {
    public static boolean isAccessibilityEnabled(Context context) {
        return getManager(context).isEnabled();
    }

    public static boolean isObservedEventType(Context context, int i) {
        return isAccessibilityEnabled(context);
    }

    public static void sendCustomAccessibilityEvent(View view, int i, String str) {
        if (isObservedEventType(view.getContext(), i)) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain(i);
            view.onInitializeAccessibilityEvent(obtain);
            obtain.getText().add(str);
            getManager(view.getContext()).sendAccessibilityEvent(obtain);
        }
    }

    private static AccessibilityManager getManager(Context context) {
        return (AccessibilityManager) context.getSystemService("accessibility");
    }
}
