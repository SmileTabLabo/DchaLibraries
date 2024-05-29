package com.android.settings.datausage;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment;
import com.android.settings.datausage.AppStateDataUsageBridge;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.AppSwitchPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreferenceHelper;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class UnrestrictedDataAccess extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener, AppStateBaseBridge.Callback, ApplicationsState.Callbacks {
    private ApplicationsState mApplicationsState;
    private DataSaverBackend mDataSaverBackend;
    private AppStateDataUsageBridge mDataUsageBridge;
    private boolean mExtraLoaded;
    private ApplicationsState.AppFilter mFilter;
    private ApplicationsState.Session mSession;
    private boolean mShowSystem;

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setAnimationAllowed(true);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataUsageBridge = new AppStateDataUsageBridge(this.mApplicationsState, this, this.mDataSaverBackend);
        this.mSession = this.mApplicationsState.newSession(this, getLifecycle());
        this.mShowSystem = bundle != null && bundle.getBoolean("show_system");
        this.mFilter = this.mShowSystem ? ApplicationsState.FILTER_ALL_ENABLED : ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
        setHasOptionsMenu(true);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.add(0, 43, 0, this.mShowSystem ? R.string.menu_hide_system : R.string.menu_show_system);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 43) {
            this.mShowSystem = !this.mShowSystem;
            menuItem.setTitle(this.mShowSystem ? R.string.menu_hide_system : R.string.menu_show_system);
            this.mFilter = this.mShowSystem ? ApplicationsState.FILTER_ALL_ENABLED : ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
            if (this.mExtraLoaded) {
                rebuild();
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("show_system", this.mShowSystem);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        setLoading(true, false);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mDataUsageBridge.resume();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mDataUsageBridge.pause();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mDataUsageBridge.release();
    }

    @Override // com.android.settings.applications.AppStateBaseBridge.Callback
    public void onExtraInfoUpdated() {
        this.mExtraLoaded = true;
        rebuild();
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_unrestricted_data_access;
    }

    private void rebuild() {
        ArrayList<ApplicationsState.AppEntry> rebuild = this.mSession.rebuild(this.mFilter, ApplicationsState.ALPHA_COMPARATOR);
        if (rebuild != null) {
            onRebuildComplete(rebuild);
        }
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRunningStateChanged(boolean z) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageListChanged() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> arrayList) {
        if (getContext() == null) {
            return;
        }
        cacheRemoveAllPrefs(getPreferenceScreen());
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ApplicationsState.AppEntry appEntry = arrayList.get(i);
            if (shouldAddPreference(appEntry)) {
                String str = appEntry.info.packageName + "|" + appEntry.info.uid;
                AccessPreference accessPreference = (AccessPreference) getCachedPreference(str);
                if (accessPreference == null) {
                    accessPreference = new AccessPreference(getPrefContext(), appEntry);
                    accessPreference.setKey(str);
                    accessPreference.setOnPreferenceChangeListener(this);
                    getPreferenceScreen().addPreference(accessPreference);
                } else {
                    accessPreference.setDisabledByAdmin(RestrictedLockUtils.checkIfMeteredDataRestricted(getContext(), appEntry.info.packageName, UserHandle.getUserId(appEntry.info.uid)));
                    accessPreference.reuse();
                }
                accessPreference.setOrder(i);
            }
        }
        setLoading(false, true);
        removeCachedPrefs(getPreferenceScreen());
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageIconChanged() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageSizeChanged(String str) {
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

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 349;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.unrestricted_data_access_settings;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference instanceof AccessPreference) {
            AccessPreference accessPreference = (AccessPreference) preference;
            boolean z = obj == Boolean.TRUE;
            logSpecialPermissionChange(z, accessPreference.mEntry.info.packageName);
            this.mDataSaverBackend.setIsWhitelisted(accessPreference.mEntry.info.uid, accessPreference.mEntry.info.packageName, z);
            if (accessPreference.mState != null) {
                accessPreference.mState.isDataSaverWhitelisted = z;
            }
            return true;
        }
        return false;
    }

    @VisibleForTesting
    void logSpecialPermissionChange(boolean z, String str) {
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), z ? 781 : 782, str, new Pair[0]);
    }

    @VisibleForTesting
    boolean shouldAddPreference(ApplicationsState.AppEntry appEntry) {
        return appEntry != null && UserHandle.isApp(appEntry.info.uid);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* loaded from: classes.dex */
    public class AccessPreference extends AppSwitchPreference implements DataSaverBackend.Listener {
        private final ApplicationsState.AppEntry mEntry;
        private final RestrictedPreferenceHelper mHelper;
        private final AppStateDataUsageBridge.DataUsageState mState;

        public AccessPreference(Context context, ApplicationsState.AppEntry appEntry) {
            super(context);
            setWidgetLayoutResource(R.layout.restricted_switch_widget);
            this.mHelper = new RestrictedPreferenceHelper(context, this, null);
            this.mEntry = appEntry;
            this.mState = (AppStateDataUsageBridge.DataUsageState) this.mEntry.extraInfo;
            this.mEntry.ensureLabel(getContext());
            setDisabledByAdmin(RestrictedLockUtils.checkIfMeteredDataRestricted(context, appEntry.info.packageName, UserHandle.getUserId(appEntry.info.uid)));
            setState();
            if (this.mEntry.icon != null) {
                setIcon(this.mEntry.icon);
            }
        }

        @Override // android.support.v7.preference.Preference
        public void onAttached() {
            super.onAttached();
            UnrestrictedDataAccess.this.mDataSaverBackend.addListener(this);
        }

        @Override // android.support.v7.preference.Preference
        public void onDetached() {
            UnrestrictedDataAccess.this.mDataSaverBackend.remListener(this);
            super.onDetached();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v7.preference.TwoStatePreference, android.support.v7.preference.Preference
        public void onClick() {
            if (this.mState != null && this.mState.isDataSaverBlacklisted) {
                AppInfoDashboardFragment.startAppInfoFragment(AppDataUsage.class, R.string.app_data_usage, null, UnrestrictedDataAccess.this, this.mEntry);
            } else {
                super.onClick();
            }
        }

        @Override // android.support.v7.preference.Preference
        public void performClick() {
            if (!this.mHelper.performClick()) {
                super.performClick();
            }
        }

        private void setState() {
            setTitle(this.mEntry.label);
            if (this.mState != null) {
                setChecked(this.mState.isDataSaverWhitelisted);
                if (isDisabledByAdmin()) {
                    setSummary(R.string.disabled_by_admin);
                } else if (this.mState.isDataSaverBlacklisted) {
                    setSummary(R.string.restrict_background_blacklisted);
                } else {
                    setSummary("");
                }
            }
        }

        public void reuse() {
            setState();
            notifyChanged();
        }

        @Override // com.android.settings.widget.AppSwitchPreference, android.support.v14.preference.SwitchPreference, android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
            int i;
            if (this.mEntry.icon == null) {
                preferenceViewHolder.itemView.post(new Runnable() { // from class: com.android.settings.datausage.UnrestrictedDataAccess.AccessPreference.1
                    @Override // java.lang.Runnable
                    public void run() {
                        UnrestrictedDataAccess.this.mApplicationsState.ensureIcon(AccessPreference.this.mEntry);
                        AccessPreference.this.setIcon(AccessPreference.this.mEntry.icon);
                    }
                });
            }
            boolean isDisabledByAdmin = isDisabledByAdmin();
            View findViewById = preferenceViewHolder.findViewById(16908312);
            int i2 = 0;
            if (isDisabledByAdmin) {
                findViewById.setVisibility(0);
            } else {
                if (this.mState == null || !this.mState.isDataSaverBlacklisted) {
                    i = 0;
                } else {
                    i = 4;
                }
                findViewById.setVisibility(i);
            }
            super.onBindViewHolder(preferenceViewHolder);
            this.mHelper.onBindViewHolder(preferenceViewHolder);
            preferenceViewHolder.findViewById(R.id.restricted_icon).setVisibility(isDisabledByAdmin ? 0 : 8);
            View findViewById2 = preferenceViewHolder.findViewById(16908352);
            if (isDisabledByAdmin) {
                i2 = 8;
            }
            findViewById2.setVisibility(i2);
        }

        @Override // com.android.settings.datausage.DataSaverBackend.Listener
        public void onDataSaverChanged(boolean z) {
        }

        @Override // com.android.settings.datausage.DataSaverBackend.Listener
        public void onWhitelistStatusChanged(int i, boolean z) {
            if (this.mState != null && this.mEntry.info.uid == i) {
                this.mState.isDataSaverWhitelisted = z;
                reuse();
            }
        }

        @Override // com.android.settings.datausage.DataSaverBackend.Listener
        public void onBlacklistStatusChanged(int i, boolean z) {
            if (this.mState != null && this.mEntry.info.uid == i) {
                this.mState.isDataSaverBlacklisted = z;
                reuse();
            }
        }

        public void setDisabledByAdmin(RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
            this.mHelper.setDisabledByAdmin(enforcedAdmin);
        }

        public boolean isDisabledByAdmin() {
            return this.mHelper.isDisabledByAdmin();
        }

        @VisibleForTesting
        public ApplicationsState.AppEntry getEntryForTest() {
            return this.mEntry;
        }
    }
}
