package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class BluetoothPairingDialogFragment extends InstrumentedDialogFragment implements DialogInterface.OnClickListener, TextWatcher {
    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    private BluetoothPairingController mPairingController;
    private BluetoothPairingDialog mPairingDialogActivity;
    private EditText mPairingView;

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        if (!isPairingControllerSet()) {
            throw new IllegalStateException("Must call setPairingController() before showing dialog");
        }
        if (!isPairingDialogActivitySet()) {
            throw new IllegalStateException("Must call setPairingDialogActivity() before showing dialog");
        }
        this.mBuilder = new AlertDialog.Builder(getActivity());
        this.mDialog = setupDialog();
        this.mDialog.setCanceledOnTouchOutside(false);
        return this.mDialog;
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (this.mPairingView.getInputType() != 2) {
            String charSequence2 = charSequence.toString();
            Log.d("BTPairingDialogFragment", "onTextChanged " + charSequence2);
            String stringFilter = stringFilter(charSequence2);
            if (!charSequence2.equals(stringFilter)) {
                this.mPairingView.setText(stringFilter);
                this.mPairingView.setSelection(stringFilter.length());
            }
        }
    }

    private String stringFilter(String str) {
        return Pattern.compile("[^\\x20-\\x7e]").matcher(str).replaceAll("").trim();
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        Button button = this.mDialog.getButton(-1);
        if (button != null) {
            button.setEnabled(this.mPairingController.isPasskeyValid(editable));
        }
        this.mPairingController.updateUserInput(editable.toString());
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            this.mPairingController.onDialogPositiveClick(this);
        } else if (i == -2) {
            this.mPairingController.onDialogNegativeClick(this);
        }
        this.mPairingDialogActivity.dismiss();
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 613;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setPairingController(BluetoothPairingController bluetoothPairingController) {
        if (isPairingControllerSet()) {
            throw new IllegalStateException("The controller can only be set once. Forcibly replacing it will lead to undefined behavior");
        }
        this.mPairingController = bluetoothPairingController;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isPairingControllerSet() {
        return this.mPairingController != null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setPairingDialogActivity(BluetoothPairingDialog bluetoothPairingDialog) {
        if (isPairingDialogActivitySet()) {
            throw new IllegalStateException("The pairing dialog activity can only be set once");
        }
        this.mPairingDialogActivity = bluetoothPairingDialog;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isPairingDialogActivitySet() {
        return this.mPairingDialogActivity != null;
    }

    private AlertDialog setupDialog() {
        switch (this.mPairingController.getDialogType()) {
            case 0:
                return createUserEntryDialog();
            case 1:
                return createConsentDialog();
            case 2:
                return createDisplayPasskeyOrPinDialog();
            default:
                Log.e("BTPairingDialogFragment", "Incorrect pairing type received, not showing any dialog");
                return null;
        }
    }

    @VisibleForTesting
    CharSequence getPairingViewText() {
        if (this.mPairingView != null) {
            return this.mPairingView.getText();
        }
        return null;
    }

    private AlertDialog createUserEntryDialog() {
        this.mBuilder.setTitle(getString(R.string.bluetooth_pairing_request, new Object[]{this.mPairingController.getDeviceName()}));
        this.mBuilder.setView(createPinEntryView());
        this.mBuilder.setPositiveButton(getString(17039370), this);
        this.mBuilder.setNegativeButton(getString(17039360), this);
        AlertDialog create = this.mBuilder.create();
        create.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.android.settings.bluetooth.-$$Lambda$BluetoothPairingDialogFragment$ItV61WjNe_T4YaZN6BYGTBHLdZc
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                BluetoothPairingDialogFragment.lambda$createUserEntryDialog$0(BluetoothPairingDialogFragment.this, dialogInterface);
            }
        });
        return create;
    }

    public static /* synthetic */ void lambda$createUserEntryDialog$0(BluetoothPairingDialogFragment bluetoothPairingDialogFragment, DialogInterface dialogInterface) {
        InputMethodManager inputMethodManager;
        if (TextUtils.isEmpty(bluetoothPairingDialogFragment.getPairingViewText())) {
            bluetoothPairingDialogFragment.mDialog.getButton(-1).setEnabled(false);
        }
        if (bluetoothPairingDialogFragment.mPairingView != null && bluetoothPairingDialogFragment.mPairingView.requestFocus() && (inputMethodManager = (InputMethodManager) bluetoothPairingDialogFragment.getContext().getSystemService("input_method")) != null) {
            inputMethodManager.showSoftInput(bluetoothPairingDialogFragment.mPairingView, 1);
        }
    }

    private View createPinEntryView() {
        View inflate = getActivity().getLayoutInflater().inflate(R.layout.bluetooth_pin_entry, (ViewGroup) null);
        TextView textView = (TextView) inflate.findViewById(R.id.pin_values_hint);
        TextView textView2 = (TextView) inflate.findViewById(R.id.message_below_pin);
        CheckBox checkBox = (CheckBox) inflate.findViewById(R.id.alphanumeric_pin);
        CheckBox checkBox2 = (CheckBox) inflate.findViewById(R.id.phonebook_sharing_message_entry_pin);
        checkBox2.setText(getString(R.string.bluetooth_pairing_shares_phonebook, new Object[]{this.mPairingController.getDeviceName()}));
        EditText editText = (EditText) inflate.findViewById(R.id.text);
        checkBox2.setVisibility(this.mPairingController.isProfileReady() ? 8 : 0);
        this.mPairingController.setContactSharingState();
        checkBox2.setOnCheckedChangeListener(this.mPairingController);
        checkBox2.setChecked(this.mPairingController.getContactSharingState());
        this.mPairingView = editText;
        editText.setInputType(2);
        editText.addTextChangedListener(this);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.android.settings.bluetooth.-$$Lambda$BluetoothPairingDialogFragment$r7iz4I0mbAZSn1y-rbFsqcyiwC0
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                BluetoothPairingDialogFragment.lambda$createPinEntryView$1(BluetoothPairingDialogFragment.this, compoundButton, z);
            }
        });
        int deviceVariantMessageId = this.mPairingController.getDeviceVariantMessageId();
        int deviceVariantMessageHintId = this.mPairingController.getDeviceVariantMessageHintId();
        int deviceMaxPasskeyLength = this.mPairingController.getDeviceMaxPasskeyLength();
        checkBox.setVisibility(this.mPairingController.pairingCodeIsAlphanumeric() ? 0 : 8);
        if (deviceVariantMessageId != -1) {
            textView2.setText(deviceVariantMessageId);
        } else {
            textView2.setVisibility(8);
        }
        if (deviceVariantMessageHintId != -1) {
            textView.setText(deviceVariantMessageHintId);
        } else {
            textView.setVisibility(8);
        }
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(deviceMaxPasskeyLength)});
        return inflate;
    }

    public static /* synthetic */ void lambda$createPinEntryView$1(BluetoothPairingDialogFragment bluetoothPairingDialogFragment, CompoundButton compoundButton, boolean z) {
        if (z) {
            bluetoothPairingDialogFragment.mPairingView.setInputType(1);
        } else {
            bluetoothPairingDialogFragment.mPairingView.setInputType(2);
        }
    }

    private AlertDialog createConfirmationDialog() {
        this.mBuilder.setTitle(getString(R.string.bluetooth_pairing_request, new Object[]{this.mPairingController.getDeviceName()}));
        this.mBuilder.setView(createView());
        this.mBuilder.setPositiveButton(getString(R.string.bluetooth_pairing_accept), this);
        this.mBuilder.setNegativeButton(getString(R.string.bluetooth_pairing_decline), this);
        return this.mBuilder.create();
    }

    private AlertDialog createConsentDialog() {
        return createConfirmationDialog();
    }

    private AlertDialog createDisplayPasskeyOrPinDialog() {
        this.mBuilder.setTitle(getString(R.string.bluetooth_pairing_request, new Object[]{this.mPairingController.getDeviceName()}));
        this.mBuilder.setView(createView());
        this.mBuilder.setNegativeButton(getString(17039360), this);
        AlertDialog create = this.mBuilder.create();
        this.mPairingController.notifyDialogDisplayed();
        return create;
    }

    private View createView() {
        View inflate = getActivity().getLayoutInflater().inflate(R.layout.bluetooth_pin_confirm, (ViewGroup) null);
        TextView textView = (TextView) inflate.findViewById(R.id.pairing_caption);
        TextView textView2 = (TextView) inflate.findViewById(R.id.pairing_subhead);
        TextView textView3 = (TextView) inflate.findViewById(R.id.pairing_code_message);
        CheckBox checkBox = (CheckBox) inflate.findViewById(R.id.phonebook_sharing_message_confirm_pin);
        checkBox.setText(getString(R.string.bluetooth_pairing_shares_phonebook, new Object[]{this.mPairingController.getDeviceName()}));
        checkBox.setVisibility(this.mPairingController.isProfileReady() ? 8 : 0);
        this.mPairingController.setContactSharingState();
        checkBox.setChecked(this.mPairingController.getContactSharingState());
        checkBox.setOnCheckedChangeListener(this.mPairingController);
        textView3.setVisibility(this.mPairingController.isDisplayPairingKeyVariant() ? 0 : 8);
        if (this.mPairingController.hasPairingContent()) {
            textView.setVisibility(0);
            textView2.setVisibility(0);
            textView2.setText(this.mPairingController.getPairingContent());
        }
        return inflate;
    }
}
