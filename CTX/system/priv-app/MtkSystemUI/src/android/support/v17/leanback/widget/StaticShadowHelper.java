package android.support.v17.leanback.widget;

import android.os.Build;
/* loaded from: classes.dex */
final class StaticShadowHelper {
    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean supportsShadow() {
        return Build.VERSION.SDK_INT >= 21;
    }
}
