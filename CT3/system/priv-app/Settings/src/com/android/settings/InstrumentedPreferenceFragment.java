package com.android.settings;

import android.support.v14.preference.PreferenceFragment;
import com.android.internal.logging.MetricsLogger;
/* loaded from: classes.dex */
public abstract class InstrumentedPreferenceFragment extends PreferenceFragment {
    protected abstract int getMetricsCategory();

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        MetricsLogger.visible(getActivity(), getMetricsCategory());
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        MetricsLogger.hidden(getActivity(), getMetricsCategory());
    }
}
