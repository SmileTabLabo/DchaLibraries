package com.android.settings.wifi;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Settings;
/* loaded from: classes.dex */
public class AdvancedWifiSettings extends RestrictedSettingsFragment {
    private PackageManager mPm;
    private boolean mUnavailable;

    public AdvancedWifiSettings() {
        super("no_config_wifi");
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 104;
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPm = getPackageManager();
        if (isUiRestricted()) {
            this.mUnavailable = true;
            setPreferenceScreen(new PreferenceScreen(getPrefContext(), null));
        } else {
            addPreferencesFromResource(R.xml.wifi_advanced_settings);
        }
        if (this.mPm.hasSystemFeature("android.hardware.wifi.direct")) {
            return;
        }
        getPreferenceScreen().removePreference(findPreference("wifi_direct"));
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getEmptyTextView().setText(R.string.wifi_advanced_not_available);
        if (!this.mUnavailable) {
            return;
        }
        getPreferenceScreen().removeAll();
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mUnavailable) {
            return;
        }
        initPreferences();
    }

    private void initPreferences() {
        Context context = getActivity();
        Intent intent = new Intent("android.credentials.INSTALL_AS_USER");
        intent.setClassName("com.android.certinstaller", "com.android.certinstaller.CertInstallerMain");
        intent.putExtra("install_as_uid", 1010);
        Preference pref = findPreference("install_credentials");
        pref.setIntent(intent);
        if (this.mPm.hasSystemFeature("android.hardware.wifi.direct")) {
            Intent wifiDirectIntent = new Intent(context, Settings.WifiP2pSettingsActivity.class);
            Preference wifiDirectPref = findPreference("wifi_direct");
            wifiDirectPref.setIntent(wifiDirectIntent);
        }
        Preference wpsPushPref = findPreference("wps_push_button");
        wpsPushPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.wifi.AdvancedWifiSettings.1
            @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
            public boolean onPreferenceClick(Preference arg0) {
                WpsFragment wpsFragment = new WpsFragment(0);
                wpsFragment.show(AdvancedWifiSettings.this.getFragmentManager(), "wps_push_button");
                return true;
            }
        });
        Preference wpsPinPref = findPreference("wps_pin_entry");
        wpsPinPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.wifi.AdvancedWifiSettings.2
            @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
            public boolean onPreferenceClick(Preference arg0) {
                WpsFragment wpsFragment = new WpsFragment(1);
                wpsFragment.show(AdvancedWifiSettings.this.getFragmentManager(), "wps_pin_entry");
                return true;
            }
        });
    }

    /* loaded from: classes.dex */
    public static class WpsFragment extends DialogFragment {
        private static int mWpsSetup;

        public WpsFragment() {
        }

        public WpsFragment(int wpsSetup) {
            mWpsSetup = wpsSetup;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new WpsDialog(getActivity(), mWpsSetup);
        }
    }
}
