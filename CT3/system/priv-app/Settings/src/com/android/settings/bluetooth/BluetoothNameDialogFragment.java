package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
/* loaded from: classes.dex */
public final class BluetoothNameDialogFragment extends DialogFragment implements TextWatcher {
    private AlertDialog mAlertDialog;
    private boolean mDeviceNameEdited;
    private boolean mDeviceNameUpdated;
    EditText mDeviceNameView;
    final LocalBluetoothAdapter mLocalAdapter;
    private Button mOkButton;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.bluetooth.BluetoothNameDialogFragment.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED")) {
                BluetoothNameDialogFragment.this.updateDeviceName();
            } else if (!action.equals("android.bluetooth.adapter.action.STATE_CHANGED") || intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE) != 12) {
            } else {
                BluetoothNameDialogFragment.this.updateDeviceName();
            }
        }
    };

    public BluetoothNameDialogFragment() {
        LocalBluetoothManager localManager = Utils.getLocalBtManager(getActivity());
        this.mLocalAdapter = localManager.getBluetoothAdapter();
        Log.d("BluetoothNameDialogFragment", "BluetoothNameDialogFragment construct");
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("BluetoothNameDialogFragment", "onCreateDialog, getActivity() is " + getActivity());
        String deviceName = this.mLocalAdapter.getName();
        if (savedInstanceState != null) {
            deviceName = savedInstanceState.getString("device_name", deviceName);
            this.mDeviceNameEdited = savedInstanceState.getBoolean("device_name_edited", false);
        }
        this.mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.bluetooth_rename_device).setView(createDialogView(deviceName)).setPositiveButton(R.string.bluetooth_rename_button, new DialogInterface.OnClickListener() { // from class: com.android.settings.bluetooth.BluetoothNameDialogFragment.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                String deviceName2 = BluetoothNameDialogFragment.this.mDeviceNameView.getText().toString();
                BluetoothNameDialogFragment.this.setDeviceName(deviceName2);
            }
        }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
        this.mAlertDialog.getWindow().setSoftInputMode(5);
        return this.mAlertDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDeviceName(String deviceName) {
        Log.d("BluetoothNameDialogFragment", "Setting device name to " + deviceName);
        this.mLocalAdapter.setName(deviceName);
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("device_name", this.mDeviceNameView.getText().toString());
        outState.putBoolean("device_name_edited", this.mDeviceNameEdited);
    }

    private View createDialogView(String deviceName) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService("layout_inflater");
        View view = layoutInflater.inflate(R.layout.dialog_edittext, (ViewGroup) null);
        this.mDeviceNameView = (EditText) view.findViewById(R.id.edittext);
        this.mDeviceNameView.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(248)});
        this.mDeviceNameView.setText(deviceName);
        this.mDeviceNameView.addTextChangedListener(this);
        this.mDeviceNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.android.settings.bluetooth.BluetoothNameDialogFragment.3
            @Override // android.widget.TextView.OnEditorActionListener
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 6) {
                    BluetoothNameDialogFragment.this.setDeviceName(v.getText().toString());
                    BluetoothNameDialogFragment.this.mAlertDialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mAlertDialog = null;
        this.mDeviceNameView = null;
        this.mOkButton = null;
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mOkButton == null) {
            this.mOkButton = this.mAlertDialog.getButton(-1);
            this.mOkButton.setEnabled(this.mDeviceNameEdited);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        getActivity().registerReceiver(this.mReceiver, filter);
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
    }

    void updateDeviceName() {
        if (this.mLocalAdapter == null || !this.mLocalAdapter.isEnabled()) {
            return;
        }
        this.mDeviceNameUpdated = true;
        this.mDeviceNameEdited = false;
        this.mDeviceNameView.setText(this.mLocalAdapter.getName());
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable s) {
        if (this.mDeviceNameUpdated) {
            this.mDeviceNameUpdated = false;
            this.mOkButton.setEnabled(false);
            return;
        }
        this.mDeviceNameEdited = true;
        if (this.mOkButton == null) {
            return;
        }
        this.mOkButton.setEnabled(s.toString().trim().length() != 0);
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
