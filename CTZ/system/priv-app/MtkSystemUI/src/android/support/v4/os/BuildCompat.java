package android.support.v4.os;

import android.os.Build;
/* loaded from: classes.dex */
public class BuildCompat {
    public static boolean isAtLeastP() {
        return Build.VERSION.SDK_INT >= 28;
    }
}
