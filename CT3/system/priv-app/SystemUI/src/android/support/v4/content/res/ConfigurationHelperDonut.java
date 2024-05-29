package android.support.v4.content.res;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
/* loaded from: a.zip:android/support/v4/content/res/ConfigurationHelperDonut.class */
class ConfigurationHelperDonut {
    ConfigurationHelperDonut() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getScreenHeightDp(@NonNull Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return (int) (displayMetrics.heightPixels / displayMetrics.density);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getScreenWidthDp(@NonNull Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getSmallestScreenWidthDp(@NonNull Resources resources) {
        return Math.min(getScreenWidthDp(resources), getScreenHeightDp(resources));
    }
}
