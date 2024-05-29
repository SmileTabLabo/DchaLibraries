package com.android.settings.display;

import com.android.internal.logging.MetricsLogger;
/* loaded from: classes.dex */
public class ScreenZoomPreferenceFragmentForSetupWizard extends ScreenZoomSettings {
    @Override // com.android.settings.display.ScreenZoomSettings, com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 370;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        if (this.mCurrentIndex != this.mInitialIndex) {
            MetricsLogger.action(getContext(), 370, this.mCurrentIndex);
        }
        super.onStop();
    }
}
