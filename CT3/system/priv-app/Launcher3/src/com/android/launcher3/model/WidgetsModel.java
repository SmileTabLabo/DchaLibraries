package com.android.launcher3.model;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.BenesseExtension;
import android.os.DeadObjectException;
import android.os.TransactionTooLargeException;
import android.util.Log;
import com.android.launcher3.AppFilter;
import com.android.launcher3.IconCache;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AlphabeticIndexCompat;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.UserHandleCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/model/WidgetsModel.class */
public class WidgetsModel {
    private final AppFilter mAppFilter;
    private final Comparator<ItemInfo> mAppNameComparator;
    private final AppWidgetManagerCompat mAppWidgetMgr;
    private final IconCache mIconCache;
    private final AlphabeticIndexCompat mIndexer;
    private final ArrayList<PackageItemInfo> mPackageItemInfos;
    private ArrayList<Object> mRawList;
    private final WidgetsAndShortcutNameComparator mWidgetAndShortcutNameComparator;
    private final HashMap<PackageItemInfo, ArrayList<Object>> mWidgetsList;

    public WidgetsModel(Context context, IconCache iconCache, AppFilter appFilter) {
        this.mAppWidgetMgr = AppWidgetManagerCompat.getInstance(context);
        this.mWidgetAndShortcutNameComparator = new WidgetsAndShortcutNameComparator(context);
        this.mAppNameComparator = new AppNameComparator(context).getAppInfoComparator();
        this.mIconCache = iconCache;
        this.mAppFilter = appFilter;
        this.mIndexer = new AlphabeticIndexCompat(context);
        this.mPackageItemInfos = new ArrayList<>();
        this.mWidgetsList = new HashMap<>();
        this.mRawList = new ArrayList<>();
    }

    private WidgetsModel(WidgetsModel widgetsModel) {
        this.mAppWidgetMgr = widgetsModel.mAppWidgetMgr;
        this.mPackageItemInfos = (ArrayList) widgetsModel.mPackageItemInfos.clone();
        this.mWidgetsList = (HashMap) widgetsModel.mWidgetsList.clone();
        this.mWidgetAndShortcutNameComparator = widgetsModel.mWidgetAndShortcutNameComparator;
        this.mAppNameComparator = widgetsModel.mAppNameComparator;
        this.mIconCache = widgetsModel.mIconCache;
        this.mAppFilter = widgetsModel.mAppFilter;
        this.mIndexer = widgetsModel.mIndexer;
        this.mRawList = (ArrayList) widgetsModel.mRawList.clone();
    }

    private void setWidgetsAndShortcuts(ArrayList<Object> arrayList) {
        this.mRawList = arrayList;
        HashMap hashMap = new HashMap();
        this.mWidgetsList.clear();
        this.mPackageItemInfos.clear();
        this.mWidgetAndShortcutNameComparator.reset();
        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
        for (Object obj : arrayList) {
            String str = "";
            UserHandleCompat userHandleCompat = null;
            ComponentName componentName = null;
            if (obj instanceof LauncherAppWidgetProviderInfo) {
                LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) obj;
                int min = Math.min(launcherAppWidgetProviderInfo.spanX, launcherAppWidgetProviderInfo.minSpanX);
                int min2 = Math.min(launcherAppWidgetProviderInfo.spanY, launcherAppWidgetProviderInfo.minSpanY);
                if (min <= invariantDeviceProfile.numColumns && min2 <= invariantDeviceProfile.numRows) {
                    componentName = launcherAppWidgetProviderInfo.provider;
                    str = launcherAppWidgetProviderInfo.provider.getPackageName();
                    userHandleCompat = this.mAppWidgetMgr.getUser(launcherAppWidgetProviderInfo);
                }
            } else if (obj instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) obj;
                componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                str = resolveInfo.activityInfo.packageName;
                userHandleCompat = UserHandleCompat.myUserHandle();
            }
            if (!str.startsWith("com.android.deskclock") && !str.startsWith("com.android.email") && !str.startsWith("com.android.calendar") && !str.startsWith("com.android.gallery3d") && !str.startsWith("com.android.browser") && !str.startsWith("com.android.music") && !str.startsWith("com.android.quicksearchbox") && !str.startsWith("com.android.settings") && !str.startsWith("com.android.contacts")) {
                if (componentName == null || userHandleCompat == null) {
                    Log.e("WidgetsModel", String.format("Widget cannot be set for %s.", obj.getClass().toString()));
                } else if (this.mAppFilter == null || this.mAppFilter.shouldShowApp(componentName)) {
                    ArrayList<Object> arrayList2 = this.mWidgetsList.get((PackageItemInfo) hashMap.get(str));
                    if (arrayList2 != null) {
                        arrayList2.add(obj);
                    } else {
                        ArrayList<Object> arrayList3 = new ArrayList<>();
                        arrayList3.add(obj);
                        PackageItemInfo packageItemInfo = new PackageItemInfo(str);
                        this.mIconCache.getTitleAndIconForApp(str, userHandleCompat, true, packageItemInfo);
                        packageItemInfo.titleSectionName = this.mIndexer.computeSectionName(packageItemInfo.title);
                        this.mWidgetsList.put(packageItemInfo, arrayList3);
                        hashMap.put(str, packageItemInfo);
                        this.mPackageItemInfos.add(packageItemInfo);
                    }
                }
            }
        }
        Collections.sort(this.mPackageItemInfos, this.mAppNameComparator);
        for (PackageItemInfo packageItemInfo2 : this.mPackageItemInfos) {
            Collections.sort(this.mWidgetsList.get(packageItemInfo2), this.mWidgetAndShortcutNameComparator);
        }
    }

    /* renamed from: clone */
    public WidgetsModel m241clone() {
        return new WidgetsModel(this);
    }

    public PackageItemInfo getPackageItemInfo(int i) {
        if (i >= this.mPackageItemInfos.size() || i < 0) {
            return null;
        }
        return this.mPackageItemInfos.get(i);
    }

    public int getPackageSize() {
        return this.mPackageItemInfos.size();
    }

    public ArrayList<Object> getRawList() {
        return this.mRawList;
    }

    public List<Object> getSortedWidgets(int i) {
        return this.mWidgetsList.get(this.mPackageItemInfos.get(i));
    }

    public boolean isEmpty() {
        return this.mRawList.isEmpty();
    }

    public WidgetsModel updateAndClone(Context context) {
        Utilities.assertWorkerThread();
        try {
            ArrayList<Object> arrayList = new ArrayList<>();
            for (AppWidgetProviderInfo appWidgetProviderInfo : AppWidgetManagerCompat.getInstance(context).getAllProviders()) {
                arrayList.add(LauncherAppWidgetProviderInfo.fromProviderInfo(context, appWidgetProviderInfo));
            }
            if (BenesseExtension.getDchaState() == 0) {
                arrayList.addAll(context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.CREATE_SHORTCUT"), 0));
            }
            setWidgetsAndShortcuts(arrayList);
        } catch (Exception e) {
            if (LauncherAppState.isDogfoodBuild() || (!(e.getCause() instanceof TransactionTooLargeException) && !(e.getCause() instanceof DeadObjectException))) {
                throw e;
            }
        }
        return m241clone();
    }
}
