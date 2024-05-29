package com.android.settingslib;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import java.text.NumberFormat;
/* loaded from: a.zip:com/android/settingslib/Utils.class */
public class Utils {
    private static String sPermissionControllerPackageName;
    private static String sServicesSystemSharedLibPackageName;
    private static String sSharedSystemSharedLibPackageName;
    private static Signature[] sSystemSignature;

    private static String formatPercentage(double d) {
        return NumberFormat.getPercentInstance().format(d);
    }

    public static String formatPercentage(int i) {
        return formatPercentage(i / 100.0d);
    }

    public static int getBatteryLevel(Intent intent) {
        return (intent.getIntExtra("level", 0) * 100) / intent.getIntExtra("scale", 100);
    }

    public static String getBatteryStatus(Resources resources, Intent intent, boolean z) {
        String string;
        int intExtra = intent.getIntExtra("plugged", 0);
        int intExtra2 = intent.getIntExtra("status", 1);
        if (intExtra2 == 2) {
            string = resources.getString(intExtra == 1 ? z ? R$string.battery_info_status_charging_ac_short : R$string.battery_info_status_charging_ac : intExtra == 2 ? z ? R$string.battery_info_status_charging_usb_short : R$string.battery_info_status_charging_usb : intExtra == 4 ? z ? R$string.battery_info_status_charging_wireless_short : R$string.battery_info_status_charging_wireless : R$string.battery_info_status_charging);
        } else {
            string = intExtra2 == 3 ? resources.getString(R$string.battery_info_status_discharging) : intExtra2 == 4 ? resources.getString(R$string.battery_info_status_not_charging) : intExtra2 == 5 ? resources.getString(R$string.battery_info_status_full) : resources.getString(R$string.battery_info_status_unknown);
        }
        return string;
    }

    private static Signature getFirstSignature(PackageInfo packageInfo) {
        if (packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length <= 0) {
            return null;
        }
        return packageInfo.signatures[0];
    }

    private static Signature getSystemSignature(PackageManager packageManager) {
        try {
            return getFirstSignature(packageManager.getPackageInfo("android", 64));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x0053, code lost:
        if (com.android.settingslib.Utils.sSystemSignature[0].equals(getFirstSignature(r6)) == false) goto L20;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean isSystemPackage(PackageManager packageManager, PackageInfo packageInfo) {
        boolean z;
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(packageManager)};
        }
        if (sPermissionControllerPackageName == null) {
            sPermissionControllerPackageName = packageManager.getPermissionControllerPackageName();
        }
        if (sServicesSystemSharedLibPackageName == null) {
            sServicesSystemSharedLibPackageName = packageManager.getServicesSystemSharedLibraryPackageName();
        }
        if (sSharedSystemSharedLibPackageName == null) {
            sSharedSystemSharedLibPackageName = packageManager.getSharedSystemSharedLibraryPackageName();
        }
        if (sSystemSignature[0] != null) {
            z = true;
        }
        z = true;
        if (!packageInfo.packageName.equals(sPermissionControllerPackageName)) {
            z = true;
            if (!packageInfo.packageName.equals(sServicesSystemSharedLibPackageName)) {
                z = packageInfo.packageName.equals(sSharedSystemSharedLibPackageName);
            }
        }
        return z;
    }
}
