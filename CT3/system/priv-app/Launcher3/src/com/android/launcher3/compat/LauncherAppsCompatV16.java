package com.android.launcher3.compat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Bundle;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.util.PackageManagerHelper;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/compat/LauncherAppsCompatV16.class */
public class LauncherAppsCompatV16 extends LauncherAppsCompat {
    private Context mContext;
    private PackageManager mPm;
    private List<LauncherAppsCompat.OnAppsChangedCallbackCompat> mCallbacks = new ArrayList();
    private PackageMonitor mPackageMonitor = new PackageMonitor(this);

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/compat/LauncherAppsCompatV16$PackageMonitor.class */
    public class PackageMonitor extends BroadcastReceiver {
        final LauncherAppsCompatV16 this$0;

        PackageMonitor(LauncherAppsCompatV16 launcherAppsCompatV16) {
            this.this$0 = launcherAppsCompatV16;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
            if (!"android.intent.action.PACKAGE_CHANGED".equals(action) && !"android.intent.action.PACKAGE_REMOVED".equals(action) && !"android.intent.action.PACKAGE_ADDED".equals(action)) {
                if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                    boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.REPLACING", Utilities.ATLEAST_KITKAT ? false : true);
                    String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    for (LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat : this.this$0.getCallbacks()) {
                        onAppsChangedCallbackCompat.onPackagesAvailable(stringArrayExtra, myUserHandle, booleanExtra);
                    }
                    return;
                } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    boolean booleanExtra2 = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                    String[] stringArrayExtra2 = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    for (LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat2 : this.this$0.getCallbacks()) {
                        onAppsChangedCallbackCompat2.onPackagesUnavailable(stringArrayExtra2, myUserHandle, booleanExtra2);
                    }
                    return;
                } else {
                    return;
                }
            }
            String schemeSpecificPart = intent.getData().getSchemeSpecificPart();
            boolean booleanExtra3 = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            if (schemeSpecificPart == null || schemeSpecificPart.length() == 0) {
                return;
            }
            if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                for (LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat3 : this.this$0.getCallbacks()) {
                    onAppsChangedCallbackCompat3.onPackageChanged(schemeSpecificPart, myUserHandle);
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (booleanExtra3) {
                    return;
                }
                for (LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat4 : this.this$0.getCallbacks()) {
                    onAppsChangedCallbackCompat4.onPackageRemoved(schemeSpecificPart, myUserHandle);
                }
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                if (booleanExtra3) {
                    for (LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat5 : this.this$0.getCallbacks()) {
                        onAppsChangedCallbackCompat5.onPackageChanged(schemeSpecificPart, myUserHandle);
                    }
                    return;
                }
                for (LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat6 : this.this$0.getCallbacks()) {
                    onAppsChangedCallbackCompat6.onPackageAdded(schemeSpecificPart, myUserHandle);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherAppsCompatV16(Context context) {
        this.mPm = context.getPackageManager();
        this.mContext = context;
    }

    private void registerForPackageIntents() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mPackageMonitor, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        this.mContext.registerReceiver(this.mPackageMonitor, intentFilter2);
    }

    private void unregisterForPackageIntents() {
        this.mContext.unregisterReceiver(this.mPackageMonitor);
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void addOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        synchronized (this) {
            if (onAppsChangedCallbackCompat != null) {
                if (!this.mCallbacks.contains(onAppsChangedCallbackCompat)) {
                    this.mCallbacks.add(onAppsChangedCallbackCompat);
                    if (this.mCallbacks.size() == 1) {
                        registerForPackageIntents();
                    }
                }
            }
        }
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public List<LauncherActivityInfoCompat> getActivityList(String str, UserHandleCompat userHandleCompat) {
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(str);
        List<ResolveInfo> queryIntentActivities = this.mPm.queryIntentActivities(intent, 0);
        ArrayList arrayList = new ArrayList(queryIntentActivities.size());
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            arrayList.add(new LauncherActivityInfoCompatV16(this.mContext, resolveInfo));
        }
        return arrayList;
    }

    List<LauncherAppsCompat.OnAppsChangedCallbackCompat> getCallbacks() {
        ArrayList arrayList;
        synchronized (this) {
            arrayList = new ArrayList(this.mCallbacks);
        }
        return arrayList;
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public boolean isActivityEnabledForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        boolean z = false;
        try {
            ActivityInfo activityInfo = this.mPm.getActivityInfo(componentName, 0);
            if (activityInfo != null) {
                z = activityInfo.isEnabled();
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public boolean isPackageEnabledForProfile(String str, UserHandleCompat userHandleCompat) {
        return PackageManagerHelper.isAppEnabled(this.mPm, str);
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public boolean isPackageSuspendedForProfile(String str, UserHandleCompat userHandleCompat) {
        return false;
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void removeOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        synchronized (this) {
            this.mCallbacks.remove(onAppsChangedCallbackCompat);
            if (this.mCallbacks.size() == 0) {
                unregisterForPackageIntents();
            }
        }
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat userHandleCompat) {
        ResolveInfo resolveActivity = this.mPm.resolveActivity(intent, 0);
        if (resolveActivity != null) {
            return new LauncherActivityInfoCompatV16(this.mContext, resolveActivity);
        }
        return null;
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void showAppDetailsForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", componentName.getPackageName(), null));
        intent.setFlags(276856832);
        this.mContext.startActivity(intent, null);
    }

    @Override // com.android.launcher3.compat.LauncherAppsCompat
    public void startActivityForProfile(ComponentName componentName, UserHandleCompat userHandleCompat, Rect rect, Bundle bundle) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setComponent(componentName);
        intent.setSourceBounds(rect);
        intent.addFlags(268435456);
        this.mContext.startActivity(intent, bundle);
    }
}
