package com.android.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
/* loaded from: a.zip:com/android/launcher3/compat/LauncherActivityInfoCompatV16.class */
public class LauncherActivityInfoCompatV16 extends LauncherActivityInfoCompat {
    private final ActivityInfo mActivityInfo;
    private final ComponentName mComponentName;
    private final PackageManager mPm;
    private final ResolveInfo mResolveInfo;

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherActivityInfoCompatV16(Context context, ResolveInfo resolveInfo) {
        this.mResolveInfo = resolveInfo;
        this.mActivityInfo = resolveInfo.activityInfo;
        this.mComponentName = new ComponentName(this.mActivityInfo.packageName, this.mActivityInfo.name);
        this.mPm = context.getPackageManager();
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public ApplicationInfo getApplicationInfo() {
        return this.mActivityInfo.applicationInfo;
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public long getFirstInstallTime() {
        long j = 0;
        try {
            PackageInfo packageInfo = this.mPm.getPackageInfo(this.mActivityInfo.packageName, 0);
            if (packageInfo != null) {
                j = packageInfo.firstInstallTime;
            }
            return j;
        } catch (PackageManager.NameNotFoundException e) {
            return 0L;
        }
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getIcon(int i) {
        int iconResource = this.mResolveInfo.getIconResource();
        Drawable drawable = null;
        if (i != 0) {
            drawable = null;
            if (iconResource != 0) {
                try {
                    drawable = this.mPm.getResourcesForApplication(this.mActivityInfo.applicationInfo).getDrawableForDensity(iconResource, i);
                } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                    drawable = null;
                }
            }
        }
        Drawable drawable2 = drawable;
        if (drawable == null) {
            drawable2 = this.mResolveInfo.loadIcon(this.mPm);
        }
        Drawable drawable3 = drawable2;
        if (drawable2 == null) {
            drawable3 = Resources.getSystem().getDrawableForDensity(17629184, i);
        }
        return drawable3;
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public CharSequence getLabel() {
        try {
            return this.mResolveInfo.loadLabel(this.mPm);
        } catch (SecurityException e) {
            Log.e("LAInfoCompat", "Failed to extract app display name from resolve info", e);
            return "";
        }
    }

    public String getName() {
        return this.mActivityInfo.name;
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public UserHandleCompat getUser() {
        return UserHandleCompat.myUserHandle();
    }
}
