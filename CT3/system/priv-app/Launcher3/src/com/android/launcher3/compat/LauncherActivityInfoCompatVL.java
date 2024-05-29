package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/compat/LauncherActivityInfoCompatVL.class */
public class LauncherActivityInfoCompatVL extends LauncherActivityInfoCompat {
    private LauncherActivityInfo mLauncherActivityInfo;

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherActivityInfoCompatVL(LauncherActivityInfo launcherActivityInfo) {
        this.mLauncherActivityInfo = launcherActivityInfo;
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public ApplicationInfo getApplicationInfo() {
        return this.mLauncherActivityInfo.getApplicationInfo();
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public ComponentName getComponentName() {
        return this.mLauncherActivityInfo.getComponentName();
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public long getFirstInstallTime() {
        return this.mLauncherActivityInfo.getFirstInstallTime();
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getIcon(int i) {
        return this.mLauncherActivityInfo.getIcon(i);
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public CharSequence getLabel() {
        return this.mLauncherActivityInfo.getLabel();
    }

    @Override // com.android.launcher3.compat.LauncherActivityInfoCompat
    public UserHandleCompat getUser() {
        return UserHandleCompat.fromUser(this.mLauncherActivityInfo.getUser());
    }
}
