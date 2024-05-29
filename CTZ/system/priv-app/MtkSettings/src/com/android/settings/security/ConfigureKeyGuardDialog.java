package com.android.settings.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
/* loaded from: classes.dex */
public class ConfigureKeyGuardDialog extends InstrumentedDialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    private boolean mConfigureConfirmed;

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 1010;
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        return new AlertDialog.Builder(getActivity()).setTitle(17039380).setMessage(R.string.credentials_configure_lock_screen_hint).setPositiveButton(R.string.credentials_configure_lock_screen_button, this).setNegativeButton(17039360, this).create();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        this.mConfigureConfirmed = i == -1;
    }

    @Override // android.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        if (this.mConfigureConfirmed) {
            this.mConfigureConfirmed = false;
            startPasswordSetup();
            return;
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    void startPasswordSetup() {
        Intent intent = new Intent("android.app.action.SET_NEW_PASSWORD");
        intent.putExtra("minimum_quality", 65536);
        startActivity(intent);
    }
}
