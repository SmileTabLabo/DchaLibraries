package com.android.settings.deletionhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.android.settings.R;
/* loaded from: classes.dex */
public class ActivationWarningFragment extends DialogFragment {
    public static ActivationWarningFragment newInstance() {
        return new ActivationWarningFragment();
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        return new AlertDialog.Builder(getActivity()).setMessage(R.string.automatic_storage_manager_activation_warning).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
    }
}
