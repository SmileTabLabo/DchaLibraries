package android.support.v4.os;

import android.os.Build;
/* loaded from: a.zip:android/support/v4/os/TraceCompat.class */
public final class TraceCompat {
    private TraceCompat() {
    }

    public static void beginSection(String str) {
        if (Build.VERSION.SDK_INT >= 18) {
            TraceJellybeanMR2.beginSection(str);
        }
    }

    public static void endSection() {
        if (Build.VERSION.SDK_INT >= 18) {
            TraceJellybeanMR2.endSection();
        }
    }
}
