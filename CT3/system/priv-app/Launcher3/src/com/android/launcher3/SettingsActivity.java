package com.android.launcher3;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
/* loaded from: a.zip:com/android/launcher3/SettingsActivity.class */
public class SettingsActivity extends Activity {

    /* loaded from: a.zip:com/android/launcher3/SettingsActivity$LauncherSettingsFragment.class */
    public static class LauncherSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override // android.preference.PreferenceFragment, android.app.Fragment
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            addPreferencesFromResource(2131165197);
            SwitchPreference switchPreference = (SwitchPreference) findPreference("pref_allowRotation");
            switchPreference.setPersistent(false);
            Bundle bundle2 = new Bundle();
            bundle2.putBoolean("default_value", false);
            switchPreference.setChecked(getActivity().getContentResolver().call(LauncherSettings$Settings.CONTENT_URI, "get_boolean_setting", "pref_allowRotation", bundle2).getBoolean("value"));
            switchPreference.setOnPreferenceChangeListener(this);
        }

        @Override // android.preference.Preference.OnPreferenceChangeListener
        public boolean onPreferenceChange(Preference preference, Object obj) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("value", ((Boolean) obj).booleanValue());
            getActivity().getContentResolver().call(LauncherSettings$Settings.CONTENT_URI, "set_boolean_setting", preference.getKey(), bundle);
            return true;
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getFragmentManager().beginTransaction().replace(16908290, new LauncherSettingsFragment()).commit();
    }
}
