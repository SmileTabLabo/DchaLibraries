package com.android.settings.nfc;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
/* loaded from: classes.dex */
public class NfcEnabler extends BaseNfcEnabler {
    private final SwitchPreference mPreference;

    public NfcEnabler(Context context, SwitchPreference switchPreference) {
        super(context);
        this.mPreference = switchPreference;
    }

    @Override // com.android.settings.nfc.BaseNfcEnabler
    protected void handleNfcStateChanged(int i) {
        switch (i) {
            case 1:
                this.mPreference.setChecked(false);
                this.mPreference.setEnabled(true);
                return;
            case 2:
                this.mPreference.setChecked(true);
                this.mPreference.setEnabled(false);
                return;
            case 3:
                this.mPreference.setChecked(true);
                this.mPreference.setEnabled(true);
                return;
            case 4:
                this.mPreference.setChecked(false);
                this.mPreference.setEnabled(false);
                return;
            default:
                return;
        }
    }
}
