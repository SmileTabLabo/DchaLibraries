package com.android.settings.applications;

import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
/* loaded from: classes.dex */
public class SpecialAccessSettings extends SettingsPreferenceFragment {
    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.special_access);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 351;
    }
}
