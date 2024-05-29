package com.android.settingslib.applications;

import android.content.pm.ApplicationInfo;
import android.os.SystemProperties;
import com.android.settingslib.applications.instantapps.InstantAppDataProvider;
/* loaded from: classes.dex */
public class AppUtils {
    private static InstantAppDataProvider sInstantAppDataProvider = null;

    public static boolean isInstant(ApplicationInfo applicationInfo) {
        String[] split;
        if (sInstantAppDataProvider != null) {
            if (sInstantAppDataProvider.isInstantApp(applicationInfo)) {
                return true;
            }
        } else if (applicationInfo.isInstantApp()) {
            return true;
        }
        String str = SystemProperties.get("settingsdebug.instant.packages");
        if (str != null && !str.isEmpty() && applicationInfo.packageName != null && (split = str.split(",")) != null) {
            for (String str2 : split) {
                if (applicationInfo.packageName.contains(str2)) {
                    return true;
                }
            }
        }
        return false;
    }
}
