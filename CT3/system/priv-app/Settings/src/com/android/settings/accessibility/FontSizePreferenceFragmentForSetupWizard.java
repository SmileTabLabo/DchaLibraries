package com.android.settings.accessibility;

import com.android.internal.logging.MetricsLogger;
/* loaded from: classes.dex */
public class FontSizePreferenceFragmentForSetupWizard extends ToggleFontSizePreferenceFragment {
    @Override // com.android.settings.accessibility.ToggleFontSizePreferenceFragment, com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 369;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        if (this.mCurrentIndex != this.mInitialIndex) {
            MetricsLogger.action(getContext(), 369, this.mCurrentIndex);
        }
        super.onStop();
    }
}
