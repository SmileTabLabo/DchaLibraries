package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.settings.AppListPreference;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DefaultBrowserPreference extends AppListPreference {
    private final Handler mHandler;
    private final PackageMonitor mPackageMonitor;
    private final PackageManager mPm;
    private final Runnable mUpdateRunnable;

    public DefaultBrowserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHandler = new Handler();
        this.mUpdateRunnable = new Runnable() { // from class: com.android.settings.applications.DefaultBrowserPreference.1
            @Override // java.lang.Runnable
            public void run() {
                int dcha_state = BenesseExtension.getDchaState();
                if (dcha_state != 0) {
                    return;
                }
                DefaultBrowserPreference.this.updateDefaultBrowserPreference();
            }
        };
        this.mPackageMonitor = new PackageMonitor() { // from class: com.android.settings.applications.DefaultBrowserPreference.2
            public void onPackageAdded(String packageName, int uid) {
                sendUpdate();
            }

            public void onPackageAppeared(String packageName, int reason) {
                sendUpdate();
            }

            public void onPackageDisappeared(String packageName, int reason) {
                sendUpdate();
            }

            public void onPackageRemoved(String packageName, int uid) {
                sendUpdate();
            }

            private void sendUpdate() {
                DefaultBrowserPreference.this.mHandler.postDelayed(DefaultBrowserPreference.this.mUpdateRunnable, 500L);
            }
        };
        this.mPm = context.getPackageManager();
        refreshBrowserApps();
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        updateDefaultBrowserPreference();
        this.mPackageMonitor.register(getContext(), getContext().getMainLooper(), false);
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        this.mPackageMonitor.unregister();
        super.onDetached();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public boolean persistString(String newValue) {
        if (newValue == null || TextUtils.isEmpty(newValue)) {
            return false;
        }
        boolean result = this.mPm.setDefaultBrowserPackageNameAsUser(newValue.toString(), this.mUserId);
        if (result) {
            setSummary("%s");
        }
        if (result) {
            return super.persistString(newValue);
        }
        return false;
    }

    public void refreshBrowserApps() {
        List<String> browsers = resolveBrowserApps();
        setPackageNames((CharSequence[]) browsers.toArray(new String[browsers.size()]), null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDefaultBrowserPreference() {
        refreshBrowserApps();
        PackageManager pm = getContext().getPackageManager();
        String packageName = pm.getDefaultBrowserPackageNameAsUser(this.mUserId);
        if (!TextUtils.isEmpty(packageName)) {
            Intent intent = new Intent();
            intent.setPackage(packageName);
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.setData(Uri.parse("http:"));
            ResolveInfo info = this.mPm.resolveActivityAsUser(intent, 0, this.mUserId);
            if (info != null) {
                setValue(packageName);
                setSummary("%s");
                return;
            }
            setSummary(R.string.default_browser_title_none);
            return;
        }
        setSummary(R.string.default_browser_title_none);
        Log.d("DefaultBrowserPref", "Cannot set empty default Browser value!");
    }

    private List<String> resolveBrowserApps() {
        List<String> result = new ArrayList<>();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(Uri.parse("http:"));
        List<ResolveInfo> list = this.mPm.queryIntentActivitiesAsUser(intent, 131072, this.mUserId);
        int count = list.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo != null && !result.contains(info.activityInfo.packageName) && info.handleAllWebDataURI) {
                result.add(info.activityInfo.packageName);
            }
        }
        return result;
    }

    public static boolean hasBrowserPreference(String pkg, Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(Uri.parse("http:"));
        intent.setPackage(pkg);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        return (resolveInfos == null || resolveInfos.size() == 0) ? false : true;
    }

    public static boolean isBrowserDefault(String pkg, Context context) {
        String defaultPackage = context.getPackageManager().getDefaultBrowserPackageNameAsUser(UserHandle.myUserId());
        if (defaultPackage != null) {
            return defaultPackage.equals(pkg);
        }
        return false;
    }
}
