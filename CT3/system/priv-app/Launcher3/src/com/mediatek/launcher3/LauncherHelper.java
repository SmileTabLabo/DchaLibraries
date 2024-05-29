package com.mediatek.launcher3;

import android.os.Trace;
import android.util.Log;
/* loaded from: a.zip:com/mediatek/launcher3/LauncherHelper.class */
public class LauncherHelper {
    private static Boolean DEBUG = Boolean.valueOf(LauncherLog.DEBUG_PERFORMANCE);

    public static void beginSection(String str) {
        if (DEBUG.booleanValue()) {
            Trace.beginSection(str);
        }
    }

    public static void endSection() {
        if (DEBUG.booleanValue()) {
            Trace.endSection();
        }
    }

    public static void traceCounter(long j, String str, int i) {
        try {
            Trace.class.getMethod("traceCounter", new Class[0]).invoke(Long.valueOf(j), str, Integer.valueOf(i));
        } catch (Exception e) {
            Log.e("LauncherHelper", "traceCounter() reflect fail");
        }
    }
}
