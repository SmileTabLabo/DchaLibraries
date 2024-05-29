package com.android.settings.accessibility;

import android.os.Bundle;
import com.android.internal.logging.MetricsLogger;
/* loaded from: classes.dex */
public class ToggleScreenReaderPreferenceFragmentForSetupWizard extends ToggleAccessibilityServicePreferenceFragment {
    private boolean mToggleSwitchWasInitiallyChecked;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accessibility.ToggleAccessibilityServicePreferenceFragment, com.android.settings.accessibility.ToggleFeaturePreferenceFragment
    public void onProcessArguments(Bundle arguments) {
        super.onProcessArguments(arguments);
        this.mToggleSwitchWasInitiallyChecked = this.mToggleSwitch.isChecked();
    }

    @Override // com.android.settings.accessibility.ToggleAccessibilityServicePreferenceFragment, com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 371;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        if (this.mToggleSwitch.isChecked() != this.mToggleSwitchWasInitiallyChecked) {
            MetricsLogger.action(getContext(), 371, this.mToggleSwitch.isChecked());
        }
        super.onStop();
    }
}
