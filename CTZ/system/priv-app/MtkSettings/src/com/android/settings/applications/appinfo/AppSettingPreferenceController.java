package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.overlay.FeatureFactory;
/* loaded from: classes.dex */
public class AppSettingPreferenceController extends AppInfoPreferenceControllerBase {
    private String mPackageName;

    public AppSettingPreferenceController(Context context, String str) {
        super(context, str);
    }

    public AppSettingPreferenceController setPackageName(String str) {
        this.mPackageName = str;
        return this;
    }

    @Override // com.android.settings.applications.appinfo.AppInfoPreferenceControllerBase, com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        return (TextUtils.isEmpty(this.mPackageName) || this.mParent == null || resolveIntent(new Intent("android.intent.action.APPLICATION_PREFERENCES").setPackage(this.mPackageName)) == null) ? 1 : 0;
    }

    @Override // com.android.settings.applications.appinfo.AppInfoPreferenceControllerBase, com.android.settingslib.core.AbstractPreferenceController
    public boolean handlePreferenceTreeClick(Preference preference) {
        Intent resolveIntent;
        if (TextUtils.equals(preference.getKey(), getPreferenceKey()) && (resolveIntent = resolveIntent(new Intent("android.intent.action.APPLICATION_PREFERENCES").setPackage(this.mPackageName))) != null) {
            FeatureFactory.getFactory(this.mContext).getMetricsFeatureProvider().actionWithSource(this.mContext, this.mParent.getMetricsCategory(), 1017);
            this.mContext.startActivity(resolveIntent);
            return true;
        }
        return false;
    }

    private Intent resolveIntent(Intent intent) {
        ResolveInfo resolveActivity = this.mContext.getPackageManager().resolveActivity(intent, 0);
        if (resolveActivity != null) {
            return new Intent(intent.getAction()).setClassName(resolveActivity.activityInfo.packageName, resolveActivity.activityInfo.name);
        }
        return null;
    }
}
