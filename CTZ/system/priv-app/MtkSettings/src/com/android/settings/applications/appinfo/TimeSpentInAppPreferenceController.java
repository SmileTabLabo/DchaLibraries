package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.core.BasePreferenceController;
import java.util.List;
/* loaded from: classes.dex */
public class TimeSpentInAppPreferenceController extends BasePreferenceController {
    static final Intent SEE_TIME_IN_APP_TEMPLATE = new Intent("com.android.settings.action.TIME_SPENT_IN_APP");
    private Intent mIntent;
    private final PackageManager mPackageManager;
    private String mPackageName;

    public TimeSpentInAppPreferenceController(Context context, String str) {
        super(context, str);
        this.mPackageManager = context.getPackageManager();
    }

    public void setPackageName(String str) {
        this.mPackageName = str;
        this.mIntent = new Intent(SEE_TIME_IN_APP_TEMPLATE).putExtra("android.intent.extra.PACKAGE_NAME", this.mPackageName);
    }

    @Override // com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        List<ResolveInfo> queryIntentActivities;
        if (TextUtils.isEmpty(this.mPackageName) || (queryIntentActivities = this.mPackageManager.queryIntentActivities(this.mIntent, 0)) == null || queryIntentActivities.isEmpty()) {
            return 2;
        }
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            if (isSystemApp(resolveInfo)) {
                return 0;
            }
        }
        return 2;
    }

    @Override // com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        Preference findPreference = preferenceScreen.findPreference(getPreferenceKey());
        if (findPreference != null) {
            findPreference.setIntent(this.mIntent);
        }
    }

    private boolean isSystemApp(ResolveInfo resolveInfo) {
        return (resolveInfo == null || resolveInfo.activityInfo == null || resolveInfo.activityInfo.applicationInfo == null || (resolveInfo.activityInfo.applicationInfo.flags & 1) == 0) ? false : true;
    }
}
