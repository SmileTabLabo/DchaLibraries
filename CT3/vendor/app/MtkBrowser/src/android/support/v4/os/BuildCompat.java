package android.support.v4.os;

import android.os.Build;
/* loaded from: b.zip:android/support/v4/os/BuildCompat.class */
public class BuildCompat {
    private BuildCompat() {
    }

    public static boolean isAtLeastN() {
        return Build.VERSION.SDK_INT >= 24;
    }
}
