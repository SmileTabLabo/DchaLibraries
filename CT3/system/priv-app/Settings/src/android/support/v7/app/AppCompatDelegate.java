package android.support.v7.app;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/* loaded from: classes.dex */
public abstract class AppCompatDelegate {
    private static int sDefaultNightMode = -1;
    private static boolean sCompatVectorFromResourcesEnabled = false;

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface NightMode {
    }

    AppCompatDelegate() {
    }

    public static boolean isCompatVectorFromResourcesEnabled() {
        return sCompatVectorFromResourcesEnabled;
    }
}
