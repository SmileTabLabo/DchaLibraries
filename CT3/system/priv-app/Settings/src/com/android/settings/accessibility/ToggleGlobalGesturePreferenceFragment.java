package com.android.settings.accessibility;

import android.provider.Settings;
import com.android.settings.widget.ToggleSwitch;
/* loaded from: classes.dex */
public class ToggleGlobalGesturePreferenceFragment extends ToggleFeaturePreferenceFragment {
    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Settings.Global.putInt(getContentResolver(), "enable_accessibility_global_gesture_enabled", enabled ? 1 : 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(new ToggleSwitch.OnBeforeCheckedChangeListener() { // from class: com.android.settings.accessibility.ToggleGlobalGesturePreferenceFragment.1
            @Override // com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                ToggleGlobalGesturePreferenceFragment.this.mSwitchBar.setCheckedInternal(checked);
                ToggleGlobalGesturePreferenceFragment.this.getArguments().putBoolean("checked", checked);
                ToggleGlobalGesturePreferenceFragment.this.onPreferenceToggled(ToggleGlobalGesturePreferenceFragment.this.mPreferenceKey, checked);
                return false;
            }
        });
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 6;
    }
}
