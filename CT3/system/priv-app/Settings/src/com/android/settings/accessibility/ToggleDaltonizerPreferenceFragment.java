package com.android.settings.accessibility;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.view.View;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.widget.SwitchBar;
/* loaded from: classes.dex */
public class ToggleDaltonizerPreferenceFragment extends ToggleFeaturePreferenceFragment implements Preference.OnPreferenceChangeListener, SwitchBar.OnSwitchChangeListener {
    private ListPreference mType;

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 5;
    }

    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.accessibility_daltonizer_settings);
        this.mType = (ListPreference) findPreference("type");
        initPreferences();
    }

    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Settings.Secure.putInt(getContentResolver(), "accessibility_display_daltonizer_enabled", enabled ? 1 : 0);
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mType) {
            Settings.Secure.putInt(getContentResolver(), "accessibility_display_daltonizer", Integer.parseInt((String) newValue));
            preference.setSummary("%s");
            return true;
        }
        return true;
    }

    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getString(R.string.accessibility_display_daltonizer_preference_title));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        this.mSwitchBar.setCheckedInternal(Settings.Secure.getInt(getContentResolver(), "accessibility_display_daltonizer_enabled", 0) == 1);
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    public void onRemoveSwitchBarToggleSwitch() {
        super.onRemoveSwitchBarToggleSwitch();
        this.mSwitchBar.removeOnSwitchChangeListener(this);
    }

    private void initPreferences() {
        String value = Integer.toString(Settings.Secure.getInt(getContentResolver(), "accessibility_display_daltonizer", 12));
        this.mType.setValue(value);
        this.mType.setOnPreferenceChangeListener(this);
        int index = this.mType.findIndexOfValue(value);
        if (index >= 0) {
            return;
        }
        this.mType.setSummary(getString(R.string.daltonizer_type_overridden, new Object[]{getString(R.string.simulate_color_space)}));
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        onPreferenceToggled(this.mPreferenceKey, isChecked);
    }
}
