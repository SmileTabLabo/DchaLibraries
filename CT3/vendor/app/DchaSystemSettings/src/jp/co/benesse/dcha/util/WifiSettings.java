package jp.co.benesse.dcha.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.provider.Settings;
/* loaded from: s.zip:jp/co/benesse/dcha/util/WifiSettings.class */
public class WifiSettings {
    private static boolean canModifyNetwork(Context context, WifiConfiguration wifiConfiguration) {
        boolean z = false;
        if (wifiConfiguration == null) {
            return true;
        }
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        PackageManager packageManager = context.getPackageManager();
        if (packageManager.hasSystemFeature("android.software.device_admin") && devicePolicyManager == null) {
            return false;
        }
        boolean z2 = false;
        if (devicePolicyManager != null) {
            ComponentName deviceOwnerComponentOnAnyUser = devicePolicyManager.getDeviceOwnerComponentOnAnyUser();
            z2 = false;
            if (deviceOwnerComponentOnAnyUser != null) {
                try {
                    z2 = packageManager.getPackageUidAsUser(deviceOwnerComponentOnAnyUser.getPackageName(), devicePolicyManager.getDeviceOwnerUserId()) == wifiConfiguration.creatorUid;
                } catch (PackageManager.NameNotFoundException e) {
                    z2 = false;
                }
            }
        }
        if (z2) {
            if (!(Settings.Global.getInt(context.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0)) {
                z = true;
            }
            return z;
        }
        return true;
    }

    public static boolean isEditabilityLockedDown(Context context, WifiConfiguration wifiConfiguration) {
        return !canModifyNetwork(context, wifiConfiguration);
    }
}
