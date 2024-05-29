package com.android.systemui.tuner;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settingslib.R$id;
import com.android.settingslib.drawer.SettingsDrawerActivity;
/* loaded from: a.zip:com/android/systemui/tuner/TunerActivity.class */
public class TunerActivity extends SettingsDrawerActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback, PreferenceFragment.OnPreferenceStartScreenCallback {

    /* loaded from: a.zip:com/android/systemui/tuner/TunerActivity$SubSettingsFragment.class */
    public static class SubSettingsFragment extends PreferenceFragment {
        @Override // android.support.v14.preference.PreferenceFragment
        public void onCreatePreferences(Bundle bundle, String str) {
            setPreferenceScreen((PreferenceScreen) ((PreferenceFragment) getTargetFragment()).getPreferenceScreen().findPreference(str));
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (getFragmentManager().popBackStackImmediate()) {
            return;
        }
        super.onBackPressed();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getFragmentManager().findFragmentByTag("tuner") == null) {
            String action = getIntent().getAction();
            getFragmentManager().beginTransaction().replace(R$id.content_frame, getIntent().getBooleanExtra("show_night_mode", false) ? new NightModeFragment() : action != null ? action.equals("com.android.settings.action.DEMO_MODE") : false ? new DemoModeFragment() : new TunerFragment(), "tuner").commit();
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        try {
            Fragment fragment = (Fragment) Class.forName(preference.getFragment()).newInstance();
            FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
            setTitle(preference.getTitle());
            beginTransaction.replace(R$id.content_frame, fragment);
            beginTransaction.addToBackStack("PreferenceFragment");
            beginTransaction.commit();
            return true;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            Log.d("TunerActivity", "Problem launching fragment", e);
            return false;
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment.OnPreferenceStartScreenCallback
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen) {
        FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
        SubSettingsFragment subSettingsFragment = new SubSettingsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("android.support.v7.preference.PreferenceFragmentCompat.PREFERENCE_ROOT", preferenceScreen.getKey());
        subSettingsFragment.setArguments(bundle);
        subSettingsFragment.setTargetFragment(preferenceFragment, 0);
        beginTransaction.replace(R$id.content_frame, subSettingsFragment);
        beginTransaction.addToBackStack("PreferenceFragment");
        beginTransaction.commit();
        return true;
    }
}
