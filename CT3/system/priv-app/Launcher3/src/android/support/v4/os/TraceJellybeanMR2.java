package android.support.v4.os;

import android.os.Trace;
/* loaded from: a.zip:android/support/v4/os/TraceJellybeanMR2.class */
class TraceJellybeanMR2 {
    TraceJellybeanMR2() {
    }

    public static void beginSection(String str) {
        Trace.beginSection(str);
    }

    public static void endSection() {
        Trace.endSection();
    }
}
