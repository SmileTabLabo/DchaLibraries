package com.android.settings;

import android.os.Bundle;
/* loaded from: classes.dex */
public abstract class OptionsMenuFragment extends InstrumentedFragment {
    @Override // com.android.settings.InstrumentedFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
