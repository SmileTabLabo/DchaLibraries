package com.android.settings.datausage;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.InstalledAppDetails;
import com.android.settings.datausage.AppStateDataUsageBridge;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class UnrestrictedDataAccess extends SettingsPreferenceFragment implements ApplicationsState.Callbacks, AppStateBaseBridge.Callback, Preference.OnPreferenceChangeListener {
    private ApplicationsState mApplicationsState;
    private DataSaverBackend mDataSaverBackend;
    private AppStateDataUsageBridge mDataUsageBridge;
    private boolean mExtraLoaded;
    private ApplicationsState.AppFilter mFilter;
    private ApplicationsState.Session mSession;
    private boolean mShowSystem;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setAnimationAllowed(true);
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataUsageBridge = new AppStateDataUsageBridge(this.mApplicationsState, this, this.mDataSaverBackend);
        this.mSession = this.mApplicationsState.newSession(this);
        this.mShowSystem = icicle != null ? icicle.getBoolean("show_system") : false;
        this.mFilter = this.mShowSystem ? ApplicationsState.FILTER_ALL_ENABLED : ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 43, 0, this.mShowSystem ? R.string.menu_hide_system : R.string.menu_show_system);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 43:
                this.mShowSystem = !this.mShowSystem;
                item.setTitle(this.mShowSystem ? R.string.menu_hide_system : R.string.menu_show_system);
                this.mFilter = this.mShowSystem ? ApplicationsState.FILTER_ALL_ENABLED : ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
                if (this.mExtraLoaded) {
                    rebuild();
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("show_system", this.mShowSystem);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLoading(true, false);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mSession.resume();
        this.mDataUsageBridge.resume();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mDataUsageBridge.pause();
        this.mSession.pause();
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mSession.release();
        this.mDataUsageBridge.release();
    }

    @Override // com.android.settings.applications.AppStateBaseBridge.Callback
    public void onExtraInfoUpdated() {
        this.mExtraLoaded = true;
        rebuild();
    }

    @Override // com.android.settings.SettingsPreferenceFragment
    protected int getHelpResource() {
        return R.string.help_url_unrestricted_data_access;
    }

    private void rebuild() {
        ArrayList<ApplicationsState.AppEntry> apps = this.mSession.rebuild(this.mFilter, ApplicationsState.ALPHA_COMPARATOR);
        if (apps == null) {
            return;
        }
        onRebuildComplete(apps);
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRunningStateChanged(boolean running) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageListChanged() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> apps) {
        if (getContext() == null) {
            return;
        }
        cacheRemoveAllPrefs(getPreferenceScreen());
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            ApplicationsState.AppEntry entry = apps.get(i);
            String key = entry.info.packageName + "|" + entry.info.uid;
            AccessPreference preference = (AccessPreference) getCachedPreference(key);
            if (preference == null) {
                preference = new AccessPreference(getPrefContext(), entry);
                preference.setKey(key);
                preference.setOnPreferenceChangeListener(this);
                getPreferenceScreen().addPreference(preference);
            } else {
                preference.reuse();
            }
            preference.setOrder(i);
        }
        setLoading(false, true);
        removeCachedPrefs(getPreferenceScreen());
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

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 349;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof AccessPreference) {
            AccessPreference accessPreference = (AccessPreference) preference;
            boolean whitelisted = newValue == Boolean.TRUE;
            this.mDataSaverBackend.setIsWhitelisted(accessPreference.mEntry.info.uid, accessPreference.mEntry.info.packageName, whitelisted);
            if (accessPreference.mState != null) {
                accessPreference.mState.isDataSaverWhitelisted = whitelisted;
                return true;
            }
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AccessPreference extends SwitchPreference implements DataSaverBackend.Listener {
        private final ApplicationsState.AppEntry mEntry;
        private final AppStateDataUsageBridge.DataUsageState mState;

        public AccessPreference(Context context, ApplicationsState.AppEntry entry) {
            super(context);
            this.mEntry = entry;
            this.mState = (AppStateDataUsageBridge.DataUsageState) this.mEntry.extraInfo;
            this.mEntry.ensureLabel(getContext());
            setState();
            if (this.mEntry.icon == null) {
                return;
            }
            setIcon(this.mEntry.icon);
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
                InstalledAppDetails.startAppInfoFragment(AppDataUsage.class, getContext().getString(R.string.app_data_usage), UnrestrictedDataAccess.this, this.mEntry);
            } else {
                super.onClick();
            }
        }

        private void setState() {
            setTitle(this.mEntry.label);
            if (this.mState == null) {
                return;
            }
            setChecked(this.mState.isDataSaverWhitelisted);
            if (this.mState.isDataSaverBlacklisted) {
                setSummary(R.string.restrict_background_blacklisted);
            } else {
                setSummary("");
            }
        }

        public void reuse() {
            setState();
            notifyChanged();
        }

        @Override // android.support.v14.preference.SwitchPreference, android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder holder) {
            if (this.mEntry.icon == null) {
                holder.itemView.post(new Runnable() { // from class: com.android.settings.datausage.UnrestrictedDataAccess.AccessPreference.1
                    @Override // java.lang.Runnable
                    public void run() {
                        UnrestrictedDataAccess.this.mApplicationsState.ensureIcon(AccessPreference.this.mEntry);
                        AccessPreference.this.setIcon(AccessPreference.this.mEntry.icon);
                    }
                });
            }
            holder.findViewById(16908312).setVisibility((this.mState == null || !this.mState.isDataSaverBlacklisted) ? 0 : 4);
            super.onBindViewHolder(holder);
        }

        @Override // com.android.settings.datausage.DataSaverBackend.Listener
        public void onDataSaverChanged(boolean isDataSaving) {
        }

        @Override // com.android.settings.datausage.DataSaverBackend.Listener
        public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
            if (this.mState == null || this.mEntry.info.uid != uid) {
                return;
            }
            this.mState.isDataSaverWhitelisted = isWhitelisted;
            reuse();
        }

        @Override // com.android.settings.datausage.DataSaverBackend.Listener
        public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
            if (this.mState == null || this.mEntry.info.uid != uid) {
                return;
            }
            this.mState.isDataSaverBlacklisted = isBlacklisted;
            reuse();
        }
    }
}
