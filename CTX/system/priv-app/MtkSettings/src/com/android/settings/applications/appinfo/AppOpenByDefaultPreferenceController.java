package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbManager;
import android.os.ServiceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppLaunchSettings;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState;
/* loaded from: classes.dex */
public class AppOpenByDefaultPreferenceController extends AppInfoPreferenceControllerBase {
    private PackageManager mPackageManager;
    private IUsbManager mUsbManager;

    public AppOpenByDefaultPreferenceController(Context context, String str) {
        super(context, str);
        this.mUsbManager = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
        this.mPackageManager = context.getPackageManager();
    }

    @Override // com.android.settings.applications.appinfo.AppInfoPreferenceControllerBase, com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        ApplicationsState.AppEntry appEntry = this.mParent.getAppEntry();
        if (appEntry == null || appEntry.info == null) {
            this.mPreference.setEnabled(false);
        } else if ((appEntry.info.flags & 8388608) == 0 || !appEntry.info.enabled) {
            this.mPreference.setEnabled(false);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        PackageInfo packageInfo = this.mParent.getPackageInfo();
        if (packageInfo != null && !AppUtils.isInstant(packageInfo.applicationInfo)) {
            preference.setVisible(true);
            preference.setSummary(AppUtils.getLaunchByDefaultSummary(this.mParent.getAppEntry(), this.mUsbManager, this.mPackageManager, this.mContext));
            return;
        }
        preference.setVisible(false);
    }

    @Override // com.android.settings.applications.appinfo.AppInfoPreferenceControllerBase
    protected Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return AppLaunchSettings.class;
    }
}
