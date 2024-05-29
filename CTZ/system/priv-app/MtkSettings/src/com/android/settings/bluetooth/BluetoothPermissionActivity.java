package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
/* loaded from: classes.dex */
public class BluetoothPermissionActivity extends AlertActivity implements DialogInterface.OnClickListener, Preference.OnPreferenceChangeListener {
    private BluetoothDevice mDevice;
    private Button mOkButton;
    private View mView;
    private TextView messageView;
    private int mRequestType = 0;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.bluetooth.BluetoothPermissionActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL") && intent.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2) == BluetoothPermissionActivity.this.mRequestType) {
                if (BluetoothPermissionActivity.this.mDevice.equals((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"))) {
                    BluetoothPermissionActivity.this.dismissDialog();
                }
            }
        }
    };
    private boolean mReceiverRegistered = false;

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissDialog() {
        dismiss();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addPrivateFlags(524288);
        Intent intent = getIntent();
        if (!intent.getAction().equals("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST")) {
            Log.e("BluetoothPermissionActivity", "Error: this activity may be started only with intent ACTION_CONNECTION_ACCESS_REQUEST");
            finish();
            return;
        }
        this.mDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        this.mRequestType = intent.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2);
        Log.i("BluetoothPermissionActivity", "onCreate() Request type: " + this.mRequestType);
        if (this.mRequestType == 1) {
            showDialog(getString(R.string.bluetooth_connection_permission_request), this.mRequestType);
        } else if (this.mRequestType == 2) {
            showDialog(getString(R.string.bluetooth_phonebook_access_dialog_title), this.mRequestType);
        } else if (this.mRequestType == 3) {
            showDialog(getString(R.string.bluetooth_message_access_dialog_title), this.mRequestType);
        } else if (this.mRequestType == 4) {
            showDialog(getString(R.string.bluetooth_sap_request), this.mRequestType);
        } else {
            Log.e("BluetoothPermissionActivity", "Error: bad request type: " + this.mRequestType);
            finish();
            return;
        }
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL"));
        this.mReceiverRegistered = true;
    }

    private void showDialog(String str, int i) {
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mTitle = str;
        Log.i("BluetoothPermissionActivity", "showDialog() Request type: " + this.mRequestType + " this: " + this);
        switch (i) {
            case 1:
                alertParams.mView = createConnectionDialogView();
                break;
            case 2:
                alertParams.mView = createPhonebookDialogView();
                break;
            case 3:
                alertParams.mView = createMapDialogView();
                break;
            case 4:
                alertParams.mView = createSapDialogView();
                break;
        }
        alertParams.mPositiveButtonText = getString(R.string.allow);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonText = getString(R.string.deny);
        alertParams.mNegativeButtonListener = this;
        this.mOkButton = this.mAlert.getButton(-1);
        setupAlert();
    }

    public void onBackPressed() {
        Log.i("BluetoothPermissionActivity", "Back button pressed! ignoring");
    }

    /* JADX WARN: Multi-variable type inference failed */
    private View createConnectionDialogView() {
        String createRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, (ViewGroup) null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_connection_dialog_text, new Object[]{createRemoteName}));
        return this.mView;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private View createPhonebookDialogView() {
        String createRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, (ViewGroup) null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_phonebook_access_dialog_content, new Object[]{createRemoteName, createRemoteName}));
        return this.mView;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private View createMapDialogView() {
        String createRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, (ViewGroup) null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_message_access_dialog_content, new Object[]{createRemoteName, createRemoteName}));
        return this.mView;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private View createSapDialogView() {
        String createRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, (ViewGroup) null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_sap_acceptance_dialog_text, new Object[]{createRemoteName, createRemoteName}));
        return this.mView;
    }

    private void onPositive() {
        Log.d("BluetoothPermissionActivity", "onPositive");
        sendReplyIntentToReceiver(true, true);
        finish();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void onNegative() {
        boolean z;
        Log.d("BluetoothPermissionActivity", "onNegative");
        if (this.mRequestType == 3) {
            LocalBluetoothManager localBtManager = Utils.getLocalBtManager(this);
            CachedBluetoothDeviceManager cachedDeviceManager = localBtManager.getCachedDeviceManager();
            CachedBluetoothDevice findDevice = cachedDeviceManager.findDevice(this.mDevice);
            if (findDevice == null) {
                findDevice = cachedDeviceManager.addDevice(localBtManager.getBluetoothAdapter(), localBtManager.getProfileManager(), this.mDevice);
            }
            z = findDevice.checkAndIncreaseMessageRejectionCount();
        } else {
            z = true;
        }
        sendReplyIntentToReceiver(false, z);
    }

    @VisibleForTesting
    void sendReplyIntentToReceiver(boolean z, boolean z2) {
        Intent intent = new Intent("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        Log.i("BluetoothPermissionActivity", "sendReplyIntentToReceiver() Request type: " + this.mRequestType + " mReturnPackage");
        intent.putExtra("android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT", z ? 1 : 2);
        intent.putExtra("android.bluetooth.device.extra.ALWAYS_ALLOWED", z2);
        intent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
        intent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
        sendBroadcast(intent, "android.permission.BLUETOOTH_ADMIN");
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case -2:
                onNegative();
                return;
            case -1:
                onPositive();
                return;
            default:
                return;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mReceiverRegistered) {
            unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        return true;
    }
}
