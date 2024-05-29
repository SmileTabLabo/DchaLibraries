package com.android.settings.datausage;

import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.datausage.DataSaverBackend;
/* loaded from: classes.dex */
public class DataSaverPreference extends Preference implements DataSaverBackend.Listener {
    private final DataSaverBackend mDataSaverBackend;

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mDataSaverBackend.addListener(this);
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        super.onDetached();
        this.mDataSaverBackend.addListener(this);
    }

    @Override // com.android.settings.datausage.DataSaverBackend.Listener
    public void onDataSaverChanged(boolean isDataSaving) {
        setSummary(isDataSaving ? R.string.data_saver_on : R.string.data_saver_off);
    }

    @Override // com.android.settings.datausage.DataSaverBackend.Listener
    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
    }

    @Override // com.android.settings.datausage.DataSaverBackend.Listener
    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
    }
}
