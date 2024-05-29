package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.util.FlagOp;
import com.android.launcher3.util.StringFilter;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/AllAppsList.class */
public class AllAppsList {
    private AppFilter mAppFilter;
    private IconCache mIconCache;
    public ArrayList<AppInfo> data = new ArrayList<>(42);
    public ArrayList<AppInfo> added = new ArrayList<>(42);
    public ArrayList<AppInfo> removed = new ArrayList<>();
    public ArrayList<AppInfo> modified = new ArrayList<>();

    public AllAppsList(IconCache iconCache, AppFilter appFilter) {
        this.mIconCache = iconCache;
        this.mAppFilter = appFilter;
    }

    private static boolean findActivity(ArrayList<AppInfo> arrayList, ComponentName componentName, UserHandleCompat userHandleCompat) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            AppInfo appInfo = arrayList.get(i);
            if (appInfo.user.equals(userHandleCompat) && appInfo.componentName.equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    static boolean findActivity(List<LauncherActivityInfoCompat> list, ComponentName componentName) {
        for (LauncherActivityInfoCompat launcherActivityInfoCompat : list) {
            if (launcherActivityInfoCompat.getComponentName().equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    private AppInfo findApplicationInfoLocked(String str, UserHandleCompat userHandleCompat, String str2) {
        for (AppInfo appInfo : this.data) {
            ComponentName component = appInfo.intent.getComponent();
            if (userHandleCompat.equals(appInfo.user) && str.equals(component.getPackageName()) && str2.equals(component.getClassName())) {
                return appInfo;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean packageHasActivities(Context context, String str, UserHandleCompat userHandleCompat) {
        boolean z = false;
        if (LauncherAppsCompat.getInstance(context).getActivityList(str, userHandleCompat).size() > 0) {
            z = true;
        }
        return z;
    }

    public void add(AppInfo appInfo) {
        if (this.mAppFilter == null || this.mAppFilter.shouldShowApp(appInfo.componentName)) {
            if (findActivity(this.data, appInfo.componentName, appInfo.user)) {
                LauncherLog.d("AllAppsList", "Application " + appInfo + " already exists in app list, app = " + appInfo);
                return;
            }
            this.data.add(appInfo);
            this.added.add(appInfo);
        }
    }

    public void addPackage(Context context, String str, UserHandleCompat userHandleCompat) {
        for (LauncherActivityInfoCompat launcherActivityInfoCompat : LauncherAppsCompat.getInstance(context).getActivityList(str, userHandleCompat)) {
            add(new AppInfo(context, launcherActivityInfoCompat, userHandleCompat, this.mIconCache));
        }
    }

    public void clear() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("AllAppsList", "clear all data in app list: app size = " + this.data.size());
        }
        this.data.clear();
        this.added.clear();
        this.removed.clear();
        this.modified.clear();
    }

    public void removePackage(String str, UserHandleCompat userHandleCompat) {
        ArrayList<AppInfo> arrayList = this.data;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("AllAppsList", "removePackage: packageName = " + str + ", data size = " + arrayList.size());
        }
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            AppInfo appInfo = arrayList.get(size);
            ComponentName component = appInfo.intent.getComponent();
            if (appInfo.user.equals(userHandleCompat) && str.equals(component.getPackageName())) {
                this.removed.add(appInfo);
                arrayList.remove(size);
            }
        }
    }

    public void updateIconsAndLabels(HashSet<String> hashSet, UserHandleCompat userHandleCompat, ArrayList<AppInfo> arrayList) {
        for (AppInfo appInfo : this.data) {
            if (appInfo.user.equals(userHandleCompat) && hashSet.contains(appInfo.componentName.getPackageName())) {
                this.mIconCache.updateTitleAndIcon(appInfo);
                arrayList.add(appInfo);
            }
        }
    }

    public void updatePackage(Context context, String str, UserHandleCompat userHandleCompat) {
        List<LauncherActivityInfoCompat> activityList = LauncherAppsCompat.getInstance(context).getActivityList(str, userHandleCompat);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("AllAppsList", "updatePackage: packageName = " + str + ", matches = " + activityList.size());
        }
        if (activityList.size() <= 0) {
            for (int size = this.data.size() - 1; size >= 0; size--) {
                AppInfo appInfo = this.data.get(size);
                ComponentName component = appInfo.intent.getComponent();
                if (userHandleCompat.equals(appInfo.user) && str.equals(component.getPackageName())) {
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("AllAppsList", "Remove application from launcher: component = " + component);
                    }
                    this.removed.add(appInfo);
                    this.mIconCache.remove(component, userHandleCompat);
                    this.data.remove(size);
                }
            }
            return;
        }
        for (int size2 = this.data.size() - 1; size2 >= 0; size2--) {
            AppInfo appInfo2 = this.data.get(size2);
            ComponentName component2 = appInfo2.intent.getComponent();
            if (userHandleCompat.equals(appInfo2.user) && str.equals(component2.getPackageName()) && !findActivity(activityList, component2)) {
                this.removed.add(appInfo2);
                this.data.remove(size2);
            }
        }
        for (LauncherActivityInfoCompat launcherActivityInfoCompat : activityList) {
            AppInfo findApplicationInfoLocked = findApplicationInfoLocked(launcherActivityInfoCompat.getComponentName().getPackageName(), userHandleCompat, launcherActivityInfoCompat.getComponentName().getClassName());
            if (findApplicationInfoLocked == null) {
                add(new AppInfo(context, launcherActivityInfoCompat, userHandleCompat, this.mIconCache));
            } else {
                this.mIconCache.getTitleAndIcon(findApplicationInfoLocked, launcherActivityInfoCompat, true);
                this.modified.add(findApplicationInfoLocked);
            }
        }
    }

    public void updatePackageFlags(StringFilter stringFilter, UserHandleCompat userHandleCompat, FlagOp flagOp) {
        ArrayList<AppInfo> arrayList = this.data;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            AppInfo appInfo = arrayList.get(size);
            ComponentName component = appInfo.intent.getComponent();
            if (appInfo.user.equals(userHandleCompat) && stringFilter.matches(component.getPackageName())) {
                appInfo.isDisabled = flagOp.apply(appInfo.isDisabled);
                this.modified.add(appInfo);
            }
        }
    }
}
