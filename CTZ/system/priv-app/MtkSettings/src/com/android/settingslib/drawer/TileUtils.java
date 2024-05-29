package com.android.settingslib.drawer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public class TileUtils {
    private static final Comparator<DashboardCategory> CATEGORY_COMPARATOR = new Comparator<DashboardCategory>() { // from class: com.android.settingslib.drawer.TileUtils.1
        @Override // java.util.Comparator
        public int compare(DashboardCategory dashboardCategory, DashboardCategory dashboardCategory2) {
            return dashboardCategory2.priority - dashboardCategory.priority;
        }
    };

    public static List<DashboardCategory> getCategories(Context context, Map<Pair<String, String>, Tile> map, boolean z, String str, String str2) {
        System.currentTimeMillis();
        boolean z2 = Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
        ArrayList arrayList = new ArrayList();
        for (UserHandle userHandle : ((UserManager) context.getSystemService("user")).getUserProfiles()) {
            if (userHandle.getIdentifier() == ActivityManager.getCurrentUser()) {
                getTilesForAction(context, userHandle, "com.android.settings.action.SETTINGS", map, null, arrayList, true, str2);
                getTilesForAction(context, userHandle, "com.android.settings.OPERATOR_APPLICATION_SETTING", map, "com.android.settings.category.wireless", arrayList, false, true, str2);
                getTilesForAction(context, userHandle, "com.android.settings.MANUFACTURER_APPLICATION_SETTING", map, "com.android.settings.category.device", arrayList, false, true, str2);
            }
            if (z2) {
                getTilesForAction(context, userHandle, "com.android.settings.action.EXTRA_SETTINGS", map, null, arrayList, false, str2);
                if (!z) {
                    getTilesForAction(context, userHandle, "com.android.settings.action.IA_SETTINGS", map, null, arrayList, false, str2);
                    if (str != null) {
                        getTilesForAction(context, userHandle, str, map, null, arrayList, false, str2);
                    }
                }
            }
        }
        HashMap hashMap = new HashMap();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Tile tile = (Tile) it.next();
            DashboardCategory dashboardCategory = (DashboardCategory) hashMap.get(tile.category);
            if (dashboardCategory == null) {
                dashboardCategory = createCategory(context, tile.category, z);
                if (dashboardCategory == null) {
                    Log.w("TileUtils", "Couldn't find category " + tile.category);
                } else {
                    hashMap.put(dashboardCategory.key, dashboardCategory);
                }
            }
            dashboardCategory.addTile(tile);
        }
        ArrayList arrayList2 = new ArrayList(hashMap.values());
        Iterator it2 = arrayList2.iterator();
        while (it2.hasNext()) {
            ((DashboardCategory) it2.next()).sortTiles();
        }
        Collections.sort(arrayList2, CATEGORY_COMPARATOR);
        return arrayList2;
    }

    private static DashboardCategory createCategory(Context context, String str, boolean z) {
        DashboardCategory dashboardCategory = new DashboardCategory();
        dashboardCategory.key = str;
        if (!z) {
            return dashboardCategory;
        }
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(new Intent(str), 0);
        if (queryIntentActivities.size() == 0) {
            return null;
        }
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            if (resolveInfo.system) {
                dashboardCategory.title = resolveInfo.activityInfo.loadLabel(packageManager);
                dashboardCategory.priority = "com.android.settings".equals(resolveInfo.activityInfo.applicationInfo.packageName) ? resolveInfo.priority : 0;
            }
        }
        return dashboardCategory;
    }

    private static void getTilesForAction(Context context, UserHandle userHandle, String str, Map<Pair<String, String>, Tile> map, String str2, ArrayList<Tile> arrayList, boolean z, String str3) {
        getTilesForAction(context, userHandle, str, map, str2, arrayList, z, z, str3);
    }

    private static void getTilesForAction(Context context, UserHandle userHandle, String str, Map<Pair<String, String>, Tile> map, String str2, ArrayList<Tile> arrayList, boolean z, boolean z2, String str3) {
        Intent intent = new Intent(str);
        if (z) {
            intent.setPackage(str3);
        }
        getTilesForIntent(context, userHandle, intent, map, str2, arrayList, z2, true, true);
    }

    public static void getTilesForIntent(Context context, UserHandle userHandle, Intent intent, Map<Pair<String, String>, Tile> map, String str, List<Tile> list, boolean z, boolean z2, boolean z3) {
        getTilesForIntent(context, userHandle, intent, map, str, list, z, z2, z3, false);
    }

    public static void getTilesForIntent(Context context, UserHandle userHandle, Intent intent, Map<Pair<String, String>, Tile> map, String str, List<Tile> list, boolean z, boolean z2, boolean z3, boolean z4) {
        PackageManager packageManager;
        Intent intent2 = intent;
        PackageManager packageManager2 = context.getPackageManager();
        List<ResolveInfo> queryIntentActivitiesAsUser = packageManager2.queryIntentActivitiesAsUser(intent2, 128, userHandle.getIdentifier());
        HashMap hashMap = new HashMap();
        for (ResolveInfo resolveInfo : queryIntentActivitiesAsUser) {
            if (resolveInfo.system) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                Bundle bundle = activityInfo.metaData;
                if (z2 && ((bundle == null || !bundle.containsKey("com.android.settings.category")) && str == null)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Found ");
                    sb.append(resolveInfo.activityInfo.name);
                    sb.append(" for intent ");
                    sb.append(intent2);
                    sb.append(" missing metadata ");
                    sb.append(bundle == null ? "" : "com.android.settings.category");
                    Log.w("TileUtils", sb.toString());
                } else {
                    String string = bundle.getString("com.android.settings.category");
                    Pair<String, String> pair = new Pair<>(activityInfo.packageName, activityInfo.name);
                    Tile tile = map.get(pair);
                    if (tile == null) {
                        Tile tile2 = new Tile();
                        tile2.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
                        tile2.category = string;
                        tile2.priority = z ? resolveInfo.priority : 0;
                        tile2.metaData = activityInfo.metaData;
                        packageManager = packageManager2;
                        updateTileData(context, tile2, activityInfo, activityInfo.applicationInfo, packageManager2, hashMap, z3);
                        map.put(pair, tile2);
                        tile = tile2;
                    } else {
                        packageManager = packageManager2;
                        if (z4) {
                            updateSummaryAndTitle(context, hashMap, tile);
                        }
                    }
                    if (!tile.userHandle.contains(userHandle)) {
                        tile.userHandle.add(userHandle);
                    }
                    if (!list.contains(tile)) {
                        list.add(tile);
                    }
                    packageManager2 = packageManager;
                    intent2 = intent;
                }
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x0070 A[Catch: NameNotFoundException | NotFoundException -> 0x00fb, TryCatch #4 {NameNotFoundException | NotFoundException -> 0x00fb, blocks: (B:24:0x0068, B:26:0x0070, B:28:0x007a, B:29:0x0085), top: B:83:0x0068 }] */
    /* JADX WARN: Removed duplicated region for block: B:31:0x008c  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0095 A[Catch: NameNotFoundException | NotFoundException -> 0x00f6, TryCatch #1 {NameNotFoundException | NotFoundException -> 0x00f6, blocks: (B:32:0x008d, B:34:0x0095, B:36:0x009f, B:37:0x00aa), top: B:79:0x008d }] */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00b1  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x00ba A[Catch: NameNotFoundException | NotFoundException -> 0x00f3, TryCatch #0 {NameNotFoundException | NotFoundException -> 0x00f3, blocks: (B:40:0x00b2, B:42:0x00ba, B:44:0x00c4, B:45:0x00cf, B:47:0x00d6, B:49:0x00de), top: B:77:0x00b2 }] */
    /* JADX WARN: Removed duplicated region for block: B:49:0x00de A[Catch: NameNotFoundException | NotFoundException -> 0x00f3, TRY_LEAVE, TryCatch #0 {NameNotFoundException | NotFoundException -> 0x00f3, blocks: (B:40:0x00b2, B:42:0x00ba, B:44:0x00c4, B:45:0x00cf, B:47:0x00d6, B:49:0x00de), top: B:77:0x00b2 }] */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0110  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x011a  */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0128  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static boolean updateTileData(Context context, Tile tile, ActivityInfo activityInfo, ApplicationInfo applicationInfo, PackageManager packageManager, Map<String, IContentProvider> map, boolean z) {
        boolean z2;
        String str;
        String str2;
        Resources resourcesForApplication;
        Bundle bundle;
        boolean z3;
        boolean z4;
        String str3;
        String string;
        if (applicationInfo.isSystemApp()) {
            String str4 = null;
            try {
                resourcesForApplication = packageManager.getResourcesForApplication(applicationInfo.packageName);
                bundle = activityInfo.metaData;
            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                z2 = false;
            }
            if (z) {
                if (!context.getPackageName().equals(applicationInfo.packageName)) {
                    z3 = true;
                    z4 = z3;
                    if (resourcesForApplication == null && bundle != null) {
                        try {
                            r1 = bundle.containsKey("com.android.settings.icon") ? bundle.getInt("com.android.settings.icon") : 0;
                        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e2) {
                            str = null;
                            str2 = null;
                            z2 = z4;
                        }
                        try {
                            try {
                                try {
                                    if (bundle.containsKey("com.android.settings.icon_tintable")) {
                                        if (z3) {
                                            Log.w("TileUtils", "Ignoring icon tintable for " + activityInfo);
                                        } else {
                                            z2 = bundle.getBoolean("com.android.settings.icon_tintable");
                                            if (!bundle.containsKey("com.android.settings.title")) {
                                                if (bundle.get("com.android.settings.title") instanceof Integer) {
                                                    str3 = resourcesForApplication.getString(bundle.getInt("com.android.settings.title"));
                                                } else {
                                                    str3 = bundle.getString("com.android.settings.title");
                                                }
                                            } else {
                                                str3 = null;
                                            }
                                            if (!bundle.containsKey("com.android.settings.summary")) {
                                                if (bundle.get("com.android.settings.summary") instanceof Integer) {
                                                    str2 = resourcesForApplication.getString(bundle.getInt("com.android.settings.summary"));
                                                } else {
                                                    str2 = bundle.getString("com.android.settings.summary");
                                                }
                                            } else {
                                                str2 = null;
                                            }
                                            if (bundle.containsKey("com.android.settings.keyhint")) {
                                                if (bundle.get("com.android.settings.keyhint") instanceof Integer) {
                                                    string = resourcesForApplication.getString(bundle.getInt("com.android.settings.keyhint"));
                                                } else {
                                                    string = bundle.getString("com.android.settings.keyhint");
                                                }
                                                str4 = string;
                                            }
                                            if (bundle.containsKey("com.android.settings.custom_view")) {
                                                tile.remoteViews = new RemoteViews(applicationInfo.packageName, bundle.getInt("com.android.settings.custom_view"));
                                                updateSummaryAndTitle(context, map, tile);
                                            }
                                            str = str4;
                                            str4 = str3;
                                        }
                                    }
                                    if (bundle.containsKey("com.android.settings.keyhint")) {
                                    }
                                    if (bundle.containsKey("com.android.settings.custom_view")) {
                                    }
                                    str = str4;
                                    str4 = str3;
                                } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e3) {
                                    str = str4;
                                    str4 = str3;
                                    if (TextUtils.isEmpty(str4)) {
                                    }
                                    if (r1 == 0) {
                                    }
                                    if (r1 != 0) {
                                    }
                                    tile.title = str4;
                                    tile.summary = str2;
                                    tile.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
                                    tile.key = str;
                                    tile.isIconTintable = z2;
                                    return true;
                                }
                                if (!bundle.containsKey("com.android.settings.summary")) {
                                }
                            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e4) {
                                str = null;
                                str2 = null;
                            }
                            if (!bundle.containsKey("com.android.settings.title")) {
                            }
                        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e5) {
                            str = null;
                            str2 = null;
                            if (TextUtils.isEmpty(str4)) {
                            }
                            if (r1 == 0) {
                            }
                            if (r1 != 0) {
                            }
                            tile.title = str4;
                            tile.summary = str2;
                            tile.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
                            tile.key = str;
                            tile.isIconTintable = z2;
                            return true;
                        }
                        z2 = z4;
                    } else {
                        str = null;
                        str2 = null;
                        z2 = z4;
                    }
                    if (TextUtils.isEmpty(str4)) {
                        str4 = activityInfo.loadLabel(packageManager).toString();
                    }
                    if (r1 == 0 && !tile.metaData.containsKey("com.android.settings.icon_uri")) {
                        r1 = activityInfo.icon;
                    }
                    if (r1 != 0) {
                        tile.icon = Icon.createWithResource(activityInfo.packageName, r1);
                    }
                    tile.title = str4;
                    tile.summary = str2;
                    tile.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
                    tile.key = str;
                    tile.isIconTintable = z2;
                    return true;
                }
            }
            z3 = false;
            z4 = z3;
            if (resourcesForApplication == null) {
            }
            str = null;
            str2 = null;
            z2 = z4;
            if (TextUtils.isEmpty(str4)) {
            }
            if (r1 == 0) {
                r1 = activityInfo.icon;
            }
            if (r1 != 0) {
            }
            tile.title = str4;
            tile.summary = str2;
            tile.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
            tile.key = str;
            tile.isIconTintable = z2;
            return true;
        }
        return false;
    }

    private static void updateSummaryAndTitle(Context context, Map<String, IContentProvider> map, Tile tile) {
        if (tile == null || tile.metaData == null || !tile.metaData.containsKey("com.android.settings.summary_uri")) {
            return;
        }
        Bundle bundleFromUri = getBundleFromUri(context, tile.metaData.getString("com.android.settings.summary_uri"), map);
        String string = getString(bundleFromUri, "com.android.settings.summary");
        String string2 = getString(bundleFromUri, "com.android.settings.title");
        if (string != null) {
            tile.remoteViews.setTextViewText(16908304, string);
        }
        if (string2 != null) {
            tile.remoteViews.setTextViewText(16908310, string2);
        }
    }

    public static Pair<String, Integer> getIconFromUri(Context context, String str, String str2, Map<String, IContentProvider> map) {
        Bundle bundleFromUri = getBundleFromUri(context, str2, map);
        if (bundleFromUri == null) {
            return null;
        }
        String string = bundleFromUri.getString("com.android.settings.icon_package");
        if (TextUtils.isEmpty(string) || bundleFromUri.getInt("com.android.settings.icon", 0) == 0) {
            return null;
        }
        if (!string.equals(str) && !string.equals(context.getPackageName())) {
            return null;
        }
        return Pair.create(string, Integer.valueOf(bundleFromUri.getInt("com.android.settings.icon", 0)));
    }

    public static String getTextFromUri(Context context, String str, Map<String, IContentProvider> map, String str2) {
        Bundle bundleFromUri = getBundleFromUri(context, str, map);
        if (bundleFromUri != null) {
            return bundleFromUri.getString(str2);
        }
        return null;
    }

    private static Bundle getBundleFromUri(Context context, String str, Map<String, IContentProvider> map) {
        IContentProvider providerFromUri;
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Uri parse = Uri.parse(str);
        String methodFromUri = getMethodFromUri(parse);
        if (TextUtils.isEmpty(methodFromUri) || (providerFromUri = getProviderFromUri(context, parse, map)) == null) {
            return null;
        }
        try {
            return providerFromUri.call(context.getPackageName(), methodFromUri, str, (Bundle) null);
        } catch (RemoteException e) {
            return null;
        }
    }

    private static String getString(Bundle bundle, String str) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(str);
    }

    private static IContentProvider getProviderFromUri(Context context, Uri uri, Map<String, IContentProvider> map) {
        if (uri == null) {
            return null;
        }
        String authority = uri.getAuthority();
        if (TextUtils.isEmpty(authority)) {
            return null;
        }
        if (!map.containsKey(authority)) {
            map.put(authority, context.getContentResolver().acquireUnstableProvider(uri));
        }
        return map.get(authority);
    }

    static String getMethodFromUri(Uri uri) {
        List<String> pathSegments;
        if (uri == null || (pathSegments = uri.getPathSegments()) == null || pathSegments.isEmpty()) {
            return null;
        }
        return pathSegments.get(0);
    }
}
