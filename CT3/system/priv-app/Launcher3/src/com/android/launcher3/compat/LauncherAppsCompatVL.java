package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.launcher3.compat.LauncherAppsCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/compat/LauncherAppsCompatVL.class */
public class LauncherAppsCompatVL extends LauncherAppsCompat {
    private Map<LauncherAppsCompat.OnAppsChangedCallbackCompat, WrappedCallback> mCallbacks = new HashMap();
    protected LauncherApps mLauncherApps;

    /* loaded from: a.zip:com/android/launcher3/compat/LauncherAppsCompatVL$WrappedCallback.class */
    private static class WrappedCallback extends LauncherApps.Callback {
        private LauncherAppsCompat.OnAppsChangedCallbackCompat mCallback;

        public WrappedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
            this.mCallback = onAppsChangedCallbackCompat;
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackageAdded(String str, UserHandle userHandle) {
            this.mCallback.onPackageAdded(str, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackageChanged(String str, UserHandle userHandle) {
            this.mCallback.onPackageChanged(str, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackageRemoved(String str, UserHandle userHandle) {
            this.mCallback.onPackageRemoved(str, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesAvailable(String[] strArr, UserHandle userHandle, boolean z) {
            this.mCallback.onPackagesAvailable(strArr, UserHandleCompat.fromUser(userHandle), z);
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesSuspended(String[] strArr, UserHandle userHandle) {
            this.mCallback.onPackagesSuspended(strArr, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesUnavailable(String[] strArr, UserHandle userHandle, boolean z) {
            this.mCallback.onPackagesUnavailable(strArr, UserHandleCompat.fromUser(userHandle), z);
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesUnsuspended(String[] strArr, UserHandle userHandle) {
            this.mCallback.onPackagesUnsuspended(strArr, UserHandleCompat.fromUser(userHandle));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherAppsCompatVL(Context context) {
        this.mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void addOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        WrappedCallback wrappedCallback = new WrappedCallback(onAppsChangedCallbackCompat);
        synchronized (this.mCallbacks) {
            this.mCallbacks.put(onAppsChangedCallbackCompat, wrappedCallback);
        }
        this.mLauncherApps.registerCallback(wrappedCallback);
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public List<LauncherActivityInfoCompat> getActivityList(String str, UserHandleCompat userHandleCompat) {
        List<LauncherActivityInfo> activityList = this.mLauncherApps.getActivityList(str, userHandleCompat.getUser());
        if (activityList.size() == 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(activityList.size());
        for (LauncherActivityInfo launcherActivityInfo : activityList) {
            arrayList.add(new LauncherActivityInfoCompatVL(launcherActivityInfo));
        }
        return arrayList;
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public boolean isActivityEnabledForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        return this.mLauncherApps.isActivityEnabled(componentName, userHandleCompat.getUser());
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public boolean isPackageEnabledForProfile(String str, UserHandleCompat userHandleCompat) {
        return this.mLauncherApps.isPackageEnabled(str, userHandleCompat.getUser());
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public boolean isPackageSuspendedForProfile(String str, UserHandleCompat userHandleCompat) {
        return false;
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void removeOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        WrappedCallback remove;
        synchronized (this.mCallbacks) {
            remove = this.mCallbacks.remove(onAppsChangedCallbackCompat);
        }
        if (remove != null) {
            this.mLauncherApps.unregisterCallback(remove);
        }
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat userHandleCompat) {
        LauncherActivityInfo resolveActivity = this.mLauncherApps.resolveActivity(intent, userHandleCompat.getUser());
        if (resolveActivity != null) {
            return new LauncherActivityInfoCompatVL(resolveActivity);
        }
        return null;
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void showAppDetailsForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        this.mLauncherApps.startAppDetailsActivity(componentName, userHandleCompat.getUser(), null, null);
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void startActivityForProfile(ComponentName componentName, UserHandleCompat userHandleCompat, Rect rect, Bundle bundle) {
        this.mLauncherApps.startMainActivity(componentName, userHandleCompat.getUser(), rect, bundle);
    }
}
