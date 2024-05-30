package com.android.settingslib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.wrapper.LocationManagerWrapper;
import java.text.NumberFormat;
/* loaded from: classes.dex */
public class Utils {
    @VisibleForTesting
    static final String STORAGE_MANAGER_SHOW_OPT_IN_PROPERTY = "ro.storage_manager.show_opt_in";
    static final int[] WIFI_PIE = {17302780, 17302781, 17302782, 17302783, 17302784};
    private static String sPermissionControllerPackageName;
    private static String sServicesSystemSharedLibPackageName;
    private static String sSharedSystemSharedLibPackageName;
    private static Signature[] sSystemSignature;

    public static void updateLocationEnabled(Context context, boolean z, int i, int i2) {
        Settings.Secure.putIntForUser(context.getContentResolver(), "location_changer", i2, i);
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        int intForUser = Settings.Secure.getIntForUser(context.getContentResolver(), "location_mode", 0, i);
        int i3 = z ? 3 : 0;
        intent.putExtra("CURRENT_MODE", intForUser);
        intent.putExtra("NEW_MODE", i3);
        context.sendBroadcastAsUser(intent, UserHandle.of(i), "android.permission.WRITE_SECURE_SETTINGS");
        new LocationManagerWrapper((LocationManager) context.getSystemService("location")).setLocationEnabledForUser(z, UserHandle.of(i));
    }

    public static String formatPercentage(long j, long j2) {
        return formatPercentage(j / j2);
    }

    public static String formatPercentage(int i) {
        return formatPercentage(i / 100.0d);
    }

    public static String formatPercentage(double d) {
        return NumberFormat.getPercentInstance().format(d);
    }

    public static int getColorAccent(Context context) {
        return getColorAttr(context, 16843829);
    }

    public static int getColorError(Context context) {
        return getColorAttr(context, 16844099);
    }

    public static int getDefaultColor(Context context, int i) {
        return context.getResources().getColorStateList(i, context.getTheme()).getDefaultColor();
    }

    public static int getDisabled(Context context, int i) {
        return applyAlphaAttr(context, 16842803, i);
    }

    public static int applyAlphaAttr(Context context, int i, int i2) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        float f = obtainStyledAttributes.getFloat(0, 0.0f);
        obtainStyledAttributes.recycle();
        return applyAlpha(f, i2);
    }

    public static int applyAlpha(float f, int i) {
        return Color.argb((int) (f * Color.alpha(i)), Color.red(i), Color.green(i), Color.blue(i));
    }

    public static int getColorAttr(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        int color = obtainStyledAttributes.getColor(0, 0);
        obtainStyledAttributes.recycle();
        return color;
    }

    public static int getThemeAttr(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        int resourceId = obtainStyledAttributes.getResourceId(0, 0);
        obtainStyledAttributes.recycle();
        return resourceId;
    }

    public static Drawable getDrawable(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        Drawable drawable = obtainStyledAttributes.getDrawable(0);
        obtainStyledAttributes.recycle();
        return drawable;
    }

    public static boolean isSystemPackage(Resources resources, PackageManager packageManager, PackageInfo packageInfo) {
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
        return (sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(packageInfo))) || packageInfo.packageName.equals(sPermissionControllerPackageName) || packageInfo.packageName.equals(sServicesSystemSharedLibPackageName) || packageInfo.packageName.equals(sSharedSystemSharedLibPackageName) || packageInfo.packageName.equals("com.android.printspooler") || isDeviceProvisioningPackage(resources, packageInfo.packageName);
    }

    private static Signature getFirstSignature(PackageInfo packageInfo) {
        if (packageInfo != null && packageInfo.signatures != null && packageInfo.signatures.length > 0) {
            return packageInfo.signatures[0];
        }
        return null;
    }

    private static Signature getSystemSignature(PackageManager packageManager) {
        try {
            return getFirstSignature(packageManager.getPackageInfo("android", 64));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isDeviceProvisioningPackage(Resources resources, String str) {
        String string = resources.getString(17039662);
        return string != null && string.equals(str);
    }
}
