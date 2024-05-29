package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import com.android.internal.logging.MetricsLogger;
/* loaded from: classes.dex */
public abstract class InstrumentedFragment extends PreferenceFragment {
    protected abstract int getMetricsCategory();

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isMonkeyRunning()) {
            return;
        }
        getActivity().finish();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

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
