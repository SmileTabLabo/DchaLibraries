package com.android.launcher3.util;

import android.os.Looper;
/* loaded from: classes.dex */
public class Preconditions {
    public static void assertNotNull(Object obj) {
    }

    public static void assertWorkerThread() {
    }

    public static void assertUIThread() {
    }

    public static void assertNonUiThread() {
    }

    private static boolean isSameLooper(Looper looper) {
        return Looper.myLooper() == looper;
    }
}
