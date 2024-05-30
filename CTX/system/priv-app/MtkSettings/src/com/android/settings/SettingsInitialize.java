package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.shortcut.CreateShortcut;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class SettingsInitialize extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(UserHandle.myUserId());
        PackageManager packageManager = context.getPackageManager();
        managedProfileSetup(context, packageManager, intent, userInfo);
        webviewSettingSetup(context, packageManager, userInfo);
        refreshExistingShortcuts(context);
        if (Settings.System.getInt(context.getContentResolver(), "wifi_hotspot_max_client_num", -1) == -1) {
            Settings.System.putInt(context.getContentResolver(), "wifi_hotspot_max_client_num", 6);
        }
    }

    private void managedProfileSetup(Context context, PackageManager packageManager, Intent intent, UserInfo userInfo) {
        if (userInfo == null || !userInfo.isManagedProfile()) {
            return;
        }
        Log.i("Settings", "Received broadcast: " + intent.getAction() + ". Setting up intent forwarding for managed profile.");
        packageManager.clearCrossProfileIntentFilters(userInfo.id);
        Intent intent2 = new Intent();
        intent2.addCategory("android.intent.category.DEFAULT");
        intent2.setPackage(context.getPackageName());
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent2, 705);
        int size = queryIntentActivities.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = queryIntentActivities.get(i);
            if (resolveInfo.filter != null && resolveInfo.activityInfo != null && resolveInfo.activityInfo.metaData != null && resolveInfo.activityInfo.metaData.getBoolean("com.android.settings.PRIMARY_PROFILE_CONTROLLED")) {
                packageManager.addCrossProfileIntentFilter(resolveInfo.filter, userInfo.id, userInfo.profileGroupId, 2);
            }
        }
        packageManager.setComponentEnabledSetting(new ComponentName(context, Settings.class), 2, 1);
        packageManager.setComponentEnabledSetting(new ComponentName(context, CreateShortcut.class), 2, 1);
    }

    private void webviewSettingSetup(Context context, PackageManager packageManager, UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        packageManager.setComponentEnabledSetting(new ComponentName("com.android.settings", "com.android.settings.WebViewImplementation"), userInfo.isAdmin() ? 1 : 2, 1);
    }

    void refreshExistingShortcuts(Context context) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(ShortcutManager.class);
        List<ShortcutInfo> pinnedShortcuts = shortcutManager.getPinnedShortcuts();
        ArrayList arrayList = new ArrayList();
        for (ShortcutInfo shortcutInfo : pinnedShortcuts) {
            Intent intent = shortcutInfo.getIntent();
            intent.setFlags(335544320);
            arrayList.add(new ShortcutInfo.Builder(context, shortcutInfo.getId()).setIntent(intent).build());
        }
        shortcutManager.updateShortcuts(arrayList);
    }
}
