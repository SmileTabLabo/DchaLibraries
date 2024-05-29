package com.android.settings.accessibility;

import android.os.Bundle;
/* loaded from: classes.dex */
public class ToggleScreenReaderPreferenceFragmentForSetupWizard extends ToggleAccessibilityServicePreferenceFragment {
    private boolean mToggleSwitchWasInitiallyChecked;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accessibility.ToggleAccessibilityServicePreferenceFragment, com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    public void onProcessArguments(Bundle bundle) {
        super.onProcessArguments(bundle);
        this.mToggleSwitchWasInitiallyChecked = this.mToggleSwitch.isChecked();
    }

    @Override // com.android.settings.accessibility.ToggleAccessibilityServicePreferenceFragment, com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 371;
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        if (this.mToggleSwitch.isChecked() != this.mToggleSwitchWasInitiallyChecked) {
            this.mMetricsFeatureProvider.action(getContext(), 371, this.mToggleSwitch.isChecked());
        }
        super.onStop();
    }
}
