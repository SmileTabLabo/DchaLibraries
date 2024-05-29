package com.android.launcher3.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.android.launcher3.Utilities;
/* loaded from: a.zip:com/android/launcher3/util/PackageManagerHelper.class */
public class PackageManagerHelper {
    public static boolean hasPermissionForActivity(Context context, Intent intent, String str) {
        boolean z = true;
        PackageManager packageManager = context.getPackageManager();
        ResolveInfo resolveActivity = packageManager.resolveActivity(intent, 0);
        if (resolveActivity == null) {
            return false;
        }
        if (TextUtils.isEmpty(resolveActivity.activityInfo.permission)) {
            return true;
        }
        if (!TextUtils.isEmpty(str) && packageManager.checkPermission(resolveActivity.activityInfo.permission, str) == 0) {
            if (Utilities.ATLEAST_MARSHMALLOW && !TextUtils.isEmpty(AppOpsManager.permissionToOp(resolveActivity.activityInfo.permission))) {
                try {
                    if (packageManager.getApplicationInfo(str, 0).targetSdkVersion < 23) {
                        z = false;
                    }
                    return z;
                } catch (PackageManager.NameNotFoundException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isAppEnabled(PackageManager packageManager, String str) {
        return isAppEnabled(packageManager, str, 0);
    }

    public static boolean isAppEnabled(PackageManager packageManager, String str, int i) {
        boolean z = false;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, i);
            if (applicationInfo != null) {
                z = applicationInfo.enabled;
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isAppOnSdcard(PackageManager packageManager, String str) {
        return isAppEnabled(packageManager, str, 8192);
    }

    public static boolean isAppSuspended(ApplicationInfo applicationInfo) {
        boolean z = false;
        if (Utilities.ATLEAST_N) {
            if ((applicationInfo.flags & 1073741824) != 0) {
                z = true;
            }
            return z;
        }
        return false;
    }

    public static boolean isAppSuspended(PackageManager packageManager, String str) {
        boolean z = false;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 0);
            if (applicationInfo != null) {
                z = isAppSuspended(applicationInfo);
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
