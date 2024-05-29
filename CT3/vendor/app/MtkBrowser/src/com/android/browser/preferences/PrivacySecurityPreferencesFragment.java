package com.android.browser.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
/* loaded from: b.zip:com/android/browser/preferences/PrivacySecurityPreferencesFragment.class */
public class PrivacySecurityPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(2131099659);
        findPreference("privacy_clear_history").setOnPreferenceChangeListener(this);
    }

    @Override // android.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference.getKey().equals("privacy_clear_history") && ((Boolean) obj).booleanValue()) {
            getActivity().setResult(-1, new Intent().putExtra("android.intent.extra.TEXT", preference.getKey()));
            return true;
        }
        return false;
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
    }
}
