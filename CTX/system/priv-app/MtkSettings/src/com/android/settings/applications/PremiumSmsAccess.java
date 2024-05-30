package com.android.settings.applications;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Pair;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.AppStateSmsPremBridge;
import com.android.settings.notification.EmptyTextSettings;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.widget.FooterPreference;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class PremiumSmsAccess extends EmptyTextSettings implements Preference.OnPreferenceChangeListener, AppStateBaseBridge.Callback, ApplicationsState.Callbacks {
    private ApplicationsState mApplicationsState;
    private ApplicationsState.Session mSession;
    private AppStateSmsPremBridge mSmsBackend;

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mSession = this.mApplicationsState.newSession(this, getLifecycle());
        this.mSmsBackend = new AppStateSmsPremBridge(getContext(), this.mApplicationsState, this);
    }

    @Override // com.android.settings.notification.EmptyTextSettings, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        setLoading(true, false);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mSmsBackend.resume();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        this.mSmsBackend.pause();
        super.onPause();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        this.mSmsBackend.release();
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.premium_sms_settings;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 388;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        PremiumSmsPreference premiumSmsPreference = (PremiumSmsPreference) preference;
        int parseInt = Integer.parseInt((String) obj);
        logSpecialPermissionChange(parseInt, premiumSmsPreference.mAppEntry.info.packageName);
        this.mSmsBackend.setSmsState(premiumSmsPreference.mAppEntry.info.packageName, parseInt);
        return true;
    }

    @VisibleForTesting
    void logSpecialPermissionChange(int i, String str) {
        int i2;
        switch (i) {
            case 1:
                i2 = 778;
                break;
            case 2:
                i2 = 779;
                break;
            case 3:
                i2 = 780;
                break;
            default:
                i2 = 0;
                break;
        }
        if (i2 != 0) {
            FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), i2, str, new Pair[0]);
        }
    }

    private void updatePrefs(ArrayList<ApplicationsState.AppEntry> arrayList) {
        if (arrayList == null) {
            return;
        }
        setEmptyText(R.string.premium_sms_none);
        setLoading(false, true);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        preferenceScreen.setOrderingAsAdded(true);
        for (int i = 0; i < arrayList.size(); i++) {
            PremiumSmsPreference premiumSmsPreference = new PremiumSmsPreference(arrayList.get(i), getPrefContext());
            premiumSmsPreference.setOnPreferenceChangeListener(this);
            preferenceScreen.addPreference(premiumSmsPreference);
        }
        if (arrayList.size() != 0) {
            FooterPreference footerPreference = new FooterPreference(getPrefContext());
            footerPreference.setTitle(R.string.premium_sms_warning);
            preferenceScreen.addPreference(footerPreference);
        }
    }

    private void update() {
        updatePrefs(this.mSession.rebuild(AppStateSmsPremBridge.FILTER_APP_PREMIUM_SMS, ApplicationsState.ALPHA_COMPARATOR));
    }

    @Override // com.android.settings.applications.AppStateBaseBridge.Callback
    public void onExtraInfoUpdated() {
        update();
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> arrayList) {
        updatePrefs(arrayList);
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRunningStateChanged(boolean z) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageListChanged() {
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PremiumSmsPreference extends DropDownPreference {
        private final ApplicationsState.AppEntry mAppEntry;

        public PremiumSmsPreference(ApplicationsState.AppEntry appEntry, Context context) {
            super(context);
            this.mAppEntry = appEntry;
            this.mAppEntry.ensureLabel(context);
            setTitle(this.mAppEntry.label);
            if (this.mAppEntry.icon != null) {
                setIcon(this.mAppEntry.icon);
            }
            setEntries(R.array.security_settings_premium_sms_values);
            setEntryValues(new CharSequence[]{String.valueOf(1), String.valueOf(2), String.valueOf(3)});
            setValue(String.valueOf(getCurrentValue()));
            setSummary("%s");
        }

        private int getCurrentValue() {
            if (this.mAppEntry.extraInfo instanceof AppStateSmsPremBridge.SmsState) {
                return ((AppStateSmsPremBridge.SmsState) this.mAppEntry.extraInfo).smsState;
            }
            return 0;
        }

        @Override // android.support.v7.preference.DropDownPreference, android.support.v7.preference.Preference
        public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
            if (getIcon() == null) {
                preferenceViewHolder.itemView.post(new Runnable() { // from class: com.android.settings.applications.PremiumSmsAccess.PremiumSmsPreference.1
                    @Override // java.lang.Runnable
                    public void run() {
                        PremiumSmsAccess.this.mApplicationsState.ensureIcon(PremiumSmsPreference.this.mAppEntry);
                        PremiumSmsPreference.this.setIcon(PremiumSmsPreference.this.mAppEntry.icon);
                    }
                });
            }
            super.onBindViewHolder(preferenceViewHolder);
        }
    }
}
