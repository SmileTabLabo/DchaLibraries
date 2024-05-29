package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.applications.PermissionsSummaryHelper;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class AdvancedAppSettings extends SettingsPreferenceFragment implements ApplicationsState.Callbacks, Indexable {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.applications.AdvancedAppSettings.2
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.advanced_apps;
            return Arrays.asList(sir);
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<String> getNonIndexableKeys(Context context) {
            return Utils.getNonIndexable(R.xml.advanced_apps, context);
        }
    };
    private Preference mAppDomainURLsPreference;
    private Preference mAppPermsPreference;
    private Preference mHighPowerPreference;
    private final PermissionsSummaryHelper.PermissionsResultCallback mPermissionCallback = new PermissionsSummaryHelper.PermissionsResultCallback() { // from class: com.android.settings.applications.AdvancedAppSettings.1
    };
    private ApplicationsState.Session mSession;
    private Preference mSystemAlertWindowPreference;
    private Preference mWriteSettingsPreference;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.advanced_apps);
        Preference permissions = getPreferenceScreen().findPreference("manage_perms");
        permissions.setIntent(new Intent("android.intent.action.MANAGE_PERMISSIONS"));
        ApplicationsState applicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        this.mSession = applicationsState.newSession(this);
        this.mAppPermsPreference = findPreference("manage_perms");
        this.mAppDomainURLsPreference = findPreference("domain_urls");
        this.mHighPowerPreference = findPreference("high_power_apps");
        this.mSystemAlertWindowPreference = findPreference("system_alert_window");
        this.mWriteSettingsPreference = findPreference("write_settings_apps");
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 130;
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRunningStateChanged(boolean running) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageListChanged() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> apps) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageIconChanged() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageSizeChanged(String packageName) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onAllSizesComputed() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onLauncherInfoChanged() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onLoadEntriesCompleted() {
    }
}
