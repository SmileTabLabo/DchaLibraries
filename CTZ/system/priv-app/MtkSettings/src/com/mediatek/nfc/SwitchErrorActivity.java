package com.mediatek.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.R;
/* loaded from: classes.dex */
public class SwitchErrorActivity extends AlertActivity {
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        String action = intent.getAction();
        if ("android.nfc.action.SWITCH_FAIL_DIALOG_REQUEST".equals(action)) {
            String stringExtra = intent.getStringExtra("mode");
            Log.d("@M_SwitchErrorActivity", "switch fail mode is " + stringExtra);
            showErrorDialog(stringExtra);
        } else if ("android.nfc.action.NOT_NFC_SIM_DIALOG_REQUEST".equals(action)) {
            String stringExtra2 = intent.getStringExtra("android.nfc.extra.WHAT_SIM");
            Log.d("@M_SwitchErrorActivity", "show not support dialog, sim is " + stringExtra2);
            showNotSupportDialog(stringExtra2);
        } else if ("android.nfc.action.NOT_NFC_TWO_SIM_DIALOG_REQUEST".equals(action)) {
            Log.d("@M_SwitchErrorActivity", "show not support dialog for SIM1 and SIM2");
            showTwoSimNotSupportDialog();
        } else {
            Log.e("@M_SwitchErrorActivity", "Error: this activity may be started only with intent android.nfc.action.SWITCH_FAIL_DIALOG_REQUEST " + action);
            finish();
        }
    }

    private void showErrorDialog(String str) {
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mIconId = 17301543;
        alertParams.mTitle = getString(R.string.card_emulation_switch_error_title);
        alertParams.mMessage = getString(R.string.card_emulation_switch_error_message, new Object[]{str});
        alertParams.mPositiveButtonText = getString(17039370);
        setupAlert();
    }

    private void showNotSupportDialog(String str) {
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mIconId = 17301543;
        alertParams.mTitle = getString(R.string.card_emulation_switch_error_title);
        alertParams.mMessage = getString(R.string.card_emulation_sim_not_supported_message, new Object[]{str});
        alertParams.mPositiveButtonText = getString(17039370);
        setupAlert();
    }

    private void showTwoSimNotSupportDialog() {
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mIconId = 17301543;
        alertParams.mTitle = getString(R.string.card_emulation_switch_error_title);
        alertParams.mMessage = getString(R.string.card_emulation_two_sim_not_supported_message);
        alertParams.mPositiveButtonText = getString(17039370);
        setupAlert();
    }
}
