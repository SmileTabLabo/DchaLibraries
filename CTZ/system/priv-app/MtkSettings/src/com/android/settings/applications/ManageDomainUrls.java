package com.android.settings.applications;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.ArraySet;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class ManageDomainUrls extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener, ApplicationsState.Callbacks {
    private ApplicationsState mApplicationsState;
    private PreferenceGroup mDomainAppList;
    private Preference mInstantAppAccountPreference;
    private ApplicationsState.Session mSession;
    private SwitchPreference mWebAction;

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setAnimationAllowed(true);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mSession = this.mApplicationsState.newSession(this, getLifecycle());
        setHasOptionsMenu(true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.manage_domain_url_settings;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
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
        if (Settings.Global.getInt(getContext().getContentResolver(), "enable_ephemeral_feature", 1) == 0) {
            this.mDomainAppList = getPreferenceScreen();
        } else {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (preferenceScreen.getPreferenceCount() == 0) {
                PreferenceCategory preferenceCategory = new PreferenceCategory(getPrefContext());
                preferenceCategory.setTitle(R.string.web_action_section_title);
                preferenceScreen.addPreference(preferenceCategory);
                this.mWebAction = new SwitchPreference(getPrefContext());
                this.mWebAction.setTitle(R.string.web_action_enable_title);
                this.mWebAction.setSummary(R.string.web_action_enable_summary);
                this.mWebAction.setChecked(Settings.Secure.getInt(getContentResolver(), "instant_apps_enabled", 1) != 0);
                this.mWebAction.setOnPreferenceChangeListener(this);
                preferenceCategory.addPreference(this.mWebAction);
                ComponentName instantAppResolverSettingsComponent = getActivity().getPackageManager().getInstantAppResolverSettingsComponent();
                final Intent intent = null;
                if (instantAppResolverSettingsComponent != null) {
                    intent = new Intent().setComponent(instantAppResolverSettingsComponent);
                }
                if (intent != null) {
                    this.mInstantAppAccountPreference = new Preference(getPrefContext());
                    this.mInstantAppAccountPreference.setTitle(R.string.instant_apps_settings);
                    this.mInstantAppAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.applications.-$$Lambda$ManageDomainUrls$agHbI5vf9m7UaPnJCYH2ithkZhk
                        @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
                        public final boolean onPreferenceClick(Preference preference) {
                            return ManageDomainUrls.lambda$onRebuildComplete$0(ManageDomainUrls.this, intent, preference);
                        }
                    });
                    preferenceCategory.addPreference(this.mInstantAppAccountPreference);
                }
                this.mDomainAppList = new PreferenceCategory(getPrefContext());
                this.mDomainAppList.setTitle(R.string.domain_url_section_title);
                preferenceScreen.addPreference(this.mDomainAppList);
            }
        }
        rebuildAppList(this.mDomainAppList, arrayList);
    }

    public static /* synthetic */ boolean lambda$onRebuildComplete$0(ManageDomainUrls manageDomainUrls, Intent intent, Preference preference) {
        manageDomainUrls.startActivity(intent);
        return true;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference == this.mWebAction) {
            Settings.Secure.putInt(getContentResolver(), "instant_apps_enabled", ((Boolean) obj).booleanValue() ? 1 : 0);
            return true;
        }
        return false;
    }

    private void rebuild() {
        ArrayList<ApplicationsState.AppEntry> rebuild = this.mSession.rebuild(ApplicationsState.FILTER_WITH_DOMAIN_URLS, ApplicationsState.ALPHA_COMPARATOR);
        if (rebuild != null) {
            onRebuildComplete(rebuild);
        }
    }

    private void rebuildAppList(PreferenceGroup preferenceGroup, ArrayList<ApplicationsState.AppEntry> arrayList) {
        cacheRemoveAllPrefs(preferenceGroup);
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ApplicationsState.AppEntry appEntry = arrayList.get(i);
            String str = appEntry.info.packageName + "|" + appEntry.info.uid;
            DomainAppPreference domainAppPreference = (DomainAppPreference) getCachedPreference(str);
            if (domainAppPreference == null) {
                domainAppPreference = new DomainAppPreference(getPrefContext(), this.mApplicationsState, appEntry);
                domainAppPreference.setKey(str);
                domainAppPreference.setOnPreferenceClickListener(this);
                preferenceGroup.addPreference(domainAppPreference);
            } else {
                domainAppPreference.reuse();
            }
            domainAppPreference.setOrder(i);
        }
        removeCachedPrefs(preferenceGroup);
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
        rebuild();
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 143;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getClass() != DomainAppPreference.class) {
            return false;
        }
        ApplicationsState.AppEntry appEntry = ((DomainAppPreference) preference).mEntry;
        AppInfoBase.startAppInfoFragment(AppLaunchSettings.class, R.string.auto_launch_label, appEntry.info.packageName, appEntry.info.uid, this, 1, getMetricsCategory());
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class DomainAppPreference extends AppPreference {
        private final ApplicationsState mApplicationsState;
        private final ApplicationsState.AppEntry mEntry;
        private final PackageManager mPm;

        public DomainAppPreference(Context context, ApplicationsState applicationsState, ApplicationsState.AppEntry appEntry) {
            super(context);
            this.mApplicationsState = applicationsState;
            this.mPm = context.getPackageManager();
            this.mEntry = appEntry;
            this.mEntry.ensureLabel(getContext());
            setState();
            if (this.mEntry.icon != null) {
                setIcon(this.mEntry.icon);
            }
        }

        private void setState() {
            setTitle(this.mEntry.label);
            setSummary(getDomainsSummary(this.mEntry.info.packageName));
        }

        public void reuse() {
            setState();
            notifyChanged();
        }

        @Override // com.android.settings.widget.AppPreference, android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
            if (this.mEntry.icon == null) {
                preferenceViewHolder.itemView.post(new Runnable() { // from class: com.android.settings.applications.ManageDomainUrls.DomainAppPreference.1
                    @Override // java.lang.Runnable
                    public void run() {
                        DomainAppPreference.this.mApplicationsState.ensureIcon(DomainAppPreference.this.mEntry);
                        DomainAppPreference.this.setIcon(DomainAppPreference.this.mEntry.icon);
                    }
                });
            }
            super.onBindViewHolder(preferenceViewHolder);
        }

        private CharSequence getDomainsSummary(String str) {
            if (this.mPm.getIntentVerificationStatusAsUser(str, UserHandle.myUserId()) == 3) {
                return getContext().getString(R.string.domain_urls_summary_none);
            }
            ArraySet<String> handledDomains = Utils.getHandledDomains(this.mPm, str);
            if (handledDomains.size() == 0) {
                return getContext().getString(R.string.domain_urls_summary_none);
            }
            return handledDomains.size() == 1 ? getContext().getString(R.string.domain_urls_summary_one, handledDomains.valueAt(0)) : getContext().getString(R.string.domain_urls_summary_some, handledDomains.valueAt(0));
        }
    }
}
