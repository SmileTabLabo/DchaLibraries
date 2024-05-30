package android.support.v4.content;

import android.content.Context;
import android.os.Build;
import java.io.File;
/* loaded from: classes.dex */
public class ContextCompat {
    private static final Object sLock = new Object();

    public static File[] getExternalFilesDirs(Context context, String type) {
        return Build.VERSION.SDK_INT >= 19 ? context.getExternalFilesDirs(type) : new File[]{context.getExternalFilesDir(type)};
    }

    public static File[] getExternalCacheDirs(Context context) {
        return Build.VERSION.SDK_INT >= 19 ? context.getExternalCacheDirs() : new File[]{context.getExternalCacheDir()};
    }
}
