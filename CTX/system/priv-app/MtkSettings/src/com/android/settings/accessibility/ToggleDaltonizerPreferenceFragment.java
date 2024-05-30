package com.android.settings.accessibility;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.widget.SwitchBar;
/* loaded from: classes.dex */
public class ToggleDaltonizerPreferenceFragment extends ToggleFeaturePreferenceFragment implements Preference.OnPreferenceChangeListener, SwitchBar.OnSwitchChangeListener {
    private ListPreference mType;

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 5;
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_color_correction;
    }

    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mType = (ListPreference) findPreference("type");
        if (!AccessibilitySettings.isColorTransformAccelerated(getActivity())) {
            this.mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.accessibility_display_daltonizer_preference_subtitle);
        }
        initPreferences();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_daltonizer_settings;
    }

    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    protected void onPreferenceToggled(String str, boolean z) {
        Settings.Secure.putInt(getContentResolver(), "accessibility_display_daltonizer_enabled", z ? 1 : 0);
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference == this.mType) {
            Settings.Secure.putInt(getContentResolver(), "accessibility_display_daltonizer", Integer.parseInt((String) obj));
            preference.setSummary("%s");
            return true;
        }
        return true;
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

    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    protected void updateSwitchBarText(SwitchBar switchBar) {
        switchBar.setSwitchBarText(R.string.accessibility_daltonizer_master_switch_title, R.string.accessibility_daltonizer_master_switch_title);
    }

    private void initPreferences() {
        String num = Integer.toString(Settings.Secure.getInt(getContentResolver(), "accessibility_display_daltonizer", 12));
        this.mType.setValue(num);
        this.mType.setOnPreferenceChangeListener(this);
        if (this.mType.findIndexOfValue(num) < 0) {
            this.mType.setSummary(getString(R.string.daltonizer_type_overridden, new Object[]{getString(R.string.simulate_color_space)}));
        }
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch r1, boolean z) {
        onPreferenceToggled(this.mPreferenceKey, z);
    }
}
