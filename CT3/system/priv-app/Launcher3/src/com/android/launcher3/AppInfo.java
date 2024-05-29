package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.PackageManagerHelper;
import java.util.ArrayList;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/AppInfo.class */
public class AppInfo extends ItemInfo {
    public ComponentName componentName;
    int flags;
    public Bitmap iconBitmap;
    public Intent intent;
    int isDisabled;
    boolean usingLowResIcon;

    AppInfo() {
        this.flags = 0;
        this.isDisabled = 0;
        this.itemType = 1;
    }

    public AppInfo(Context context, LauncherActivityInfoCompat launcherActivityInfoCompat, UserHandleCompat userHandleCompat, IconCache iconCache) {
        this(context, launcherActivityInfoCompat, userHandleCompat, iconCache, UserManagerCompat.getInstance(context).isQuietModeEnabled(userHandleCompat));
    }

    public AppInfo(Context context, LauncherActivityInfoCompat launcherActivityInfoCompat, UserHandleCompat userHandleCompat, IconCache iconCache, boolean z) {
        this.flags = 0;
        this.isDisabled = 0;
        this.componentName = launcherActivityInfoCompat.getComponentName();
        this.container = -1L;
        this.flags = initFlags(launcherActivityInfoCompat);
        if (PackageManagerHelper.isAppSuspended(launcherActivityInfoCompat.getApplicationInfo())) {
            this.isDisabled |= 4;
        }
        if (z) {
            this.isDisabled |= 8;
        }
        iconCache.getTitleAndIcon(this, launcherActivityInfoCompat, true);
        this.intent = makeLaunchIntent(context, launcherActivityInfoCompat, userHandleCompat);
        this.user = userHandleCompat;
    }

    public static void dumpApplicationInfoList(String str, String str2, ArrayList<AppInfo> arrayList) {
        Log.d(str, str2 + " size=" + arrayList.size());
        for (AppInfo appInfo : arrayList) {
            Log.d(str, "   title=\"" + appInfo.title + "\" iconBitmap=" + appInfo.iconBitmap + " componentName=" + appInfo.componentName.getPackageName());
        }
    }

    public static int initFlags(LauncherActivityInfoCompat launcherActivityInfoCompat) {
        int i = launcherActivityInfoCompat.getApplicationInfo().flags;
        int i2 = 0;
        if ((i & 1) == 0) {
            i2 = 1;
            if ((i & 128) != 0) {
                i2 = 1 | 2;
            }
        }
        return i2;
    }

    public static Intent makeLaunchIntent(Context context, LauncherActivityInfoCompat launcherActivityInfoCompat, UserHandleCompat userHandleCompat) {
        return new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER").setComponent(launcherActivityInfoCompat.getComponentName()).setFlags(270532608).putExtra("profile", UserManagerCompat.getInstance(context).getSerialNumberForUser(userHandleCompat));
    }

    @Override // com.android.launcher3.ItemInfo
    public Intent getIntent() {
        return this.intent;
    }

    @Override // com.android.launcher3.ItemInfo
    public boolean isDisabled() {
        boolean z = false;
        if (this.isDisabled != 0) {
            z = true;
        }
        return z;
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    public ComponentKey toComponentKey() {
        return new ComponentKey(this.componentName, this.user);
    }

    @Override // com.android.launcher3.ItemInfo
    public String toString() {
        return "ApplicationInfo(title=" + this.title + " id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + " user=" + this.user + ")";
    }
}
