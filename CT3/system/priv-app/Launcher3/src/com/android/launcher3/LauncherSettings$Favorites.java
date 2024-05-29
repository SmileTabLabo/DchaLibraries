package com.android.launcher3;

import android.net.Uri;
import android.provider.BaseColumns;
import com.android.launcher3.config.ProviderConfig;
/* loaded from: a.zip:com/android/launcher3/LauncherSettings$Favorites.class */
public final class LauncherSettings$Favorites implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://" + ProviderConfig.AUTHORITY + "/favorites");

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final String containerToString(int i) {
        switch (i) {
            case -101:
                return "hotseat";
            case -100:
                return "desktop";
            default:
                return String.valueOf(i);
        }
    }

    public static Uri getContentUri(long j) {
        return Uri.parse("content://" + ProviderConfig.AUTHORITY + "/favorites/" + j);
    }
}
