package com.android.settings.applications;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import com.android.settings.AppListPreference;
import com.android.settings.SelfAvailablePreference;
import java.util.List;
import java.util.Objects;
import java.util.Set;
/* loaded from: classes.dex */
public class DefaultEmergencyPreference extends AppListPreference implements SelfAvailablePreference {
    public static final Intent QUERY_INTENT = new Intent("android.telephony.action.EMERGENCY_ASSISTANCE");
    private final ContentResolver mContentResolver;

    public DefaultEmergencyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContentResolver = context.getContentResolver();
        load();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public boolean persistString(String value) {
        String previousValue = Settings.Secure.getString(this.mContentResolver, "emergency_assistance_application");
        if (!TextUtils.isEmpty(value) && !Objects.equals(value, previousValue)) {
            Settings.Secure.putString(this.mContentResolver, "emergency_assistance_application", value);
        }
        setSummary(getEntry());
        return true;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.settings.applications.DefaultEmergencyPreference$1] */
    private void load() {
        new AsyncTask<Void, Void, Set<String>>() { // from class: com.android.settings.applications.DefaultEmergencyPreference.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Set<String> doInBackground(Void[] params) {
                return DefaultEmergencyPreference.this.resolveAssistPackageAndQueryApps();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Set<String> entries) {
                String currentPkg = Settings.Secure.getString(DefaultEmergencyPreference.this.mContentResolver, "emergency_assistance_application");
                DefaultEmergencyPreference.this.setPackageNames((CharSequence[]) entries.toArray(new String[entries.size()]), currentPkg);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Set<String> resolveAssistPackageAndQueryApps() {
        boolean defaultMissing;
        Set<String> packages = new ArraySet<>();
        PackageManager packageManager = getContext().getPackageManager();
        List<ResolveInfo> infos = packageManager.queryIntentActivities(QUERY_INTENT, 0);
        PackageInfo bestMatch = null;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo info = infos.get(i);
            if (info != null && info.activityInfo != null && !packages.contains(info.activityInfo.packageName)) {
                String packageName = info.activityInfo.packageName;
                packages.add(packageName);
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                    if (isSystemApp(packageInfo) && (bestMatch == null || bestMatch.firstInstallTime > packageInfo.firstInstallTime)) {
                        bestMatch = packageInfo;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
        String defaultPackage = Settings.Secure.getString(this.mContentResolver, "emergency_assistance_application");
        if (TextUtils.isEmpty(defaultPackage)) {
            defaultMissing = true;
        } else {
            defaultMissing = !packages.contains(defaultPackage);
        }
        if (bestMatch != null && defaultMissing) {
            Settings.Secure.putString(this.mContentResolver, "emergency_assistance_application", bestMatch.packageName);
        }
        return packages;
    }

    private static boolean isCapable(Context context) {
        return context.getResources().getBoolean(17956953);
    }

    private static boolean isSystemApp(PackageInfo info) {
        return (info.applicationInfo == null || (info.applicationInfo.flags & 1) == 0) ? false : true;
    }

    @Override // com.android.settings.SelfAvailablePreference
    public boolean isAvailable(Context context) {
        return false;
    }

    public static boolean hasEmergencyPreference(String pkg, Context context) {
        Intent i = new Intent(QUERY_INTENT);
        i.setPackage(pkg);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(i, 0);
        return (resolveInfos == null || resolveInfos.size() == 0) ? false : true;
    }

    public static boolean isEmergencyDefault(String pkg, Context context) {
        String defaultPackage = Settings.Secure.getString(context.getContentResolver(), "emergency_assistance_application");
        if (defaultPackage != null) {
            return defaultPackage.equals(pkg);
        }
        return false;
    }
}
