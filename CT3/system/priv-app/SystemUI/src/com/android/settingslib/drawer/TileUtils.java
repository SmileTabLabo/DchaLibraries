package com.android.settingslib.drawer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.ArrayUtils;
import com.mediatek.settingslib.UtilsExt;
import com.mediatek.settingslib.ext.IDrawerExt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/* loaded from: a.zip:com/android/settingslib/drawer/TileUtils.class */
public class TileUtils {
    private static IDrawerExt sDrawerExt;
    private static final String[] EXTRA_PACKAGE_WHITE_LIST = {"com.mediatek.duraspeed"};
    private static final Comparator<Tile> TILE_COMPARATOR = new Comparator<Tile>() { // from class: com.android.settingslib.drawer.TileUtils.1
        @Override // java.util.Comparator
        public int compare(Tile tile, Tile tile2) {
            return tile2.priority - tile.priority;
        }
    };
    private static final Comparator<DashboardCategory> CATEGORY_COMPARATOR = new Comparator<DashboardCategory>() { // from class: com.android.settingslib.drawer.TileUtils.2
        @Override // java.util.Comparator
        public int compare(DashboardCategory dashboardCategory, DashboardCategory dashboardCategory2) {
            return dashboardCategory2.priority - dashboardCategory.priority;
        }
    };

    private static DashboardCategory createCategory(Context context, String str) {
        DashboardCategory dashboardCategory = new DashboardCategory();
        dashboardCategory.key = str;
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(new Intent(str), 0);
        if (queryIntentActivities.size() == 0) {
            return null;
        }
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            if (resolveInfo.system || ArrayUtils.contains(EXTRA_PACKAGE_WHITE_LIST, resolveInfo.activityInfo.packageName)) {
                dashboardCategory.title = resolveInfo.activityInfo.loadLabel(packageManager);
                dashboardCategory.priority = "com.android.settings".equals(resolveInfo.activityInfo.applicationInfo.packageName) ? resolveInfo.priority : 0;
            }
        }
        return dashboardCategory;
    }

    public static List<DashboardCategory> getCategories(Context context, HashMap<Pair<String, String>, Tile> hashMap) {
        System.currentTimeMillis();
        boolean z = Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
        ArrayList<Tile> arrayList = new ArrayList();
        for (UserHandle userHandle : UserManager.get(context).getUserProfiles()) {
            if (userHandle.getIdentifier() == ActivityManager.getCurrentUser()) {
                getTilesForAction(context, userHandle, "com.android.settings.action.SETTINGS", hashMap, null, arrayList, true);
                getTilesForAction(context, userHandle, "com.android.settings.OPERATOR_APPLICATION_SETTING", hashMap, "com.android.settings.category.wireless", arrayList, false);
                getTilesForAction(context, userHandle, "com.android.settings.MANUFACTURER_APPLICATION_SETTING", hashMap, "com.android.settings.category.device", arrayList, false);
            }
            if (z) {
                getTilesForAction(context, userHandle, "com.android.settings.action.EXTRA_SETTINGS", hashMap, null, arrayList, false);
            }
        }
        HashMap hashMap2 = new HashMap();
        for (Tile tile : arrayList) {
            DashboardCategory dashboardCategory = (DashboardCategory) hashMap2.get(tile.category);
            DashboardCategory dashboardCategory2 = dashboardCategory;
            if (dashboardCategory == null) {
                dashboardCategory2 = createCategory(context, tile.category);
                if (dashboardCategory2 == null) {
                    Log.w("TileUtils", "Couldn't find category " + tile.category);
                } else {
                    hashMap2.put(dashboardCategory2.key, dashboardCategory2);
                }
            }
            dashboardCategory2.addTile(tile);
        }
        ArrayList<DashboardCategory> arrayList2 = new ArrayList(hashMap2.values());
        for (DashboardCategory dashboardCategory3 : arrayList2) {
            Collections.sort(dashboardCategory3.tiles, TILE_COMPARATOR);
        }
        Collections.sort(arrayList2, CATEGORY_COMPARATOR);
        return arrayList2;
    }

    private static void getTilesForAction(Context context, UserHandle userHandle, String str, Map<Pair<String, String>, Tile> map, String str2, ArrayList<Tile> arrayList, boolean z) {
        Intent intent = new Intent(str);
        if (z) {
            intent.setPackage("com.android.settings");
        }
        getTilesForIntent(context, userHandle, intent, map, str2, arrayList, z, true);
    }

    public static void getTilesForIntent(Context context, UserHandle userHandle, Intent intent, Map<Pair<String, String>, Tile> map, String str, List<Tile> list, boolean z, boolean z2) {
        PackageManager packageManager = context.getPackageManager();
        for (ResolveInfo resolveInfo : packageManager.queryIntentActivitiesAsUser(intent, 128, userHandle.getIdentifier())) {
            if (resolveInfo.system || ArrayUtils.contains(EXTRA_PACKAGE_WHITE_LIST, resolveInfo.activityInfo.packageName)) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                Bundle bundle = activityInfo.metaData;
                if (!z2 || ((bundle != null && bundle.containsKey("com.android.settings.category")) || str != null)) {
                    String string = bundle.getString("com.android.settings.category");
                    String str2 = string;
                    if (string == null) {
                        str2 = str;
                    }
                    Pair<String, String> pair = new Pair<>(activityInfo.packageName, activityInfo.name);
                    Tile tile = map.get(pair);
                    Tile tile2 = tile;
                    if (tile == null) {
                        tile2 = new Tile();
                        tile2.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
                        tile2.category = str2;
                        tile2.priority = z ? resolveInfo.priority : 0;
                        tile2.metaData = activityInfo.metaData;
                        updateTileData(context, tile2, activityInfo, activityInfo.applicationInfo, packageManager);
                        if (sDrawerExt == null) {
                            sDrawerExt = UtilsExt.getDrawerPlugin(context);
                        }
                        if (activityInfo.name.endsWith("PrivacySettingsActivity")) {
                            sDrawerExt.setFactoryResetTitle(tile2);
                        } else if (activityInfo.name.endsWith("SimSettingsActivity")) {
                            tile2.title = sDrawerExt.customizeSimDisplayString(tile2.title.toString(), -1);
                        }
                        map.put(pair, tile2);
                    }
                    if (!tile2.userHandle.contains(userHandle)) {
                        tile2.userHandle.add(userHandle);
                    }
                    if (!list.contains(tile2)) {
                        list.add(tile2);
                    }
                } else {
                    Log.w("TileUtils", "Found " + resolveInfo.activityInfo.name + " for intent " + intent + " missing metadata " + (bundle == null ? "" : "com.android.settings.category"));
                }
            }
        }
    }

    private static boolean updateTileData(Context context, Tile tile, ActivityInfo activityInfo, ApplicationInfo applicationInfo, PackageManager packageManager) {
        int i;
        String str;
        String str2;
        if (applicationInfo.isSystemApp() || ArrayUtils.contains(EXTRA_PACKAGE_WHITE_LIST, activityInfo.packageName)) {
            int i2 = 0;
            String str3 = null;
            try {
                Resources resourcesForApplication = packageManager.getResourcesForApplication(applicationInfo.packageName);
                Bundle bundle = activityInfo.metaData;
                i = 0;
                str = null;
                str2 = null;
                if (resourcesForApplication != null) {
                    i = 0;
                    str = null;
                    str2 = null;
                    if (bundle != null) {
                        if (bundle.containsKey("com.android.settings.icon")) {
                            i2 = bundle.getInt("com.android.settings.icon");
                        }
                        int i3 = i2;
                        if (bundle.containsKey("com.android.settings.title")) {
                            int i4 = i2;
                            if (bundle.get("com.android.settings.title") instanceof Integer) {
                                int i5 = i2;
                                str3 = resourcesForApplication.getString(bundle.getInt("com.android.settings.title"));
                            } else {
                                str3 = bundle.getString("com.android.settings.title");
                            }
                        }
                        i = i2;
                        str = null;
                        str2 = str3;
                        if (bundle.containsKey("com.android.settings.summary")) {
                            int i6 = i2;
                            if (bundle.get("com.android.settings.summary") instanceof Integer) {
                                int i7 = i2;
                                str = resourcesForApplication.getString(bundle.getInt("com.android.settings.summary"));
                                str2 = str3;
                                i = i2;
                            } else {
                                str = bundle.getString("com.android.settings.summary");
                                i = i2;
                                str2 = str3;
                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                i = 0;
                str = null;
                str2 = null;
            }
            String str4 = str2;
            if (TextUtils.isEmpty(str2)) {
                str4 = activityInfo.loadLabel(packageManager).toString();
            }
            int i8 = i;
            if (i == 0) {
                i8 = activityInfo.icon;
            }
            tile.icon = Icon.createWithResource(activityInfo.packageName, i8);
            tile.title = str4;
            tile.summary = str;
            tile.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
            return true;
        }
        return false;
    }
}
