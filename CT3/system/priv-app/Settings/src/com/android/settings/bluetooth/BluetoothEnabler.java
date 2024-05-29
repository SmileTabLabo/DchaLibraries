package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.search.Index;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.mediatek.settings.PDebug;
/* loaded from: classes.dex */
public final class BluetoothEnabler implements SwitchBar.OnSwitchChangeListener {
    private Context mContext;
    private final IntentFilter mIntentFilter;
    private final LocalBluetoothAdapter mLocalAdapter;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private boolean mUpdateStatusOnly = false;
    private Handler mHandler = new Handler() { // from class: com.android.settings.bluetooth.BluetoothEnabler.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    boolean isBluetoothOn = msg.getData().getBoolean("is_bluetooth_on");
                    Index.getInstance(BluetoothEnabler.this.mContext).updateFromClassNameResource(BluetoothSettings.class.getName(), true, isBluetoothOn);
                    return;
                default:
                    return;
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.bluetooth.BluetoothEnabler.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
            Log.d("BluetoothEnabler", "BluetoothAdapter state changed to" + state);
            BluetoothEnabler.this.handleStateChanged(state);
        }
    };
    private boolean mValidListener = false;

    public BluetoothEnabler(Context context, SwitchBar switchBar) {
        this.mContext = context;
        this.mSwitchBar = switchBar;
        this.mSwitch = switchBar.getSwitch();
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        if (manager == null) {
            this.mLocalAdapter = null;
            this.mSwitch.setEnabled(false);
        } else {
            this.mLocalAdapter = manager.getBluetoothAdapter();
        }
        this.mIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
    }

    public void setupSwitchBar() {
        this.mSwitchBar.show();
    }

    public void teardownSwitchBar() {
        this.mSwitchBar.hide();
    }

    public void resume(Context context) {
        PDebug.Start("BluetoothEnabler.resume");
        if (this.mLocalAdapter == null) {
            this.mSwitch.setEnabled(false);
            return;
        }
        if (this.mContext != context) {
            this.mContext = context;
        }
        handleStateChanged(this.mLocalAdapter.getBluetoothState());
        this.mSwitchBar.addOnSwitchChangeListener(this);
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
        this.mValidListener = true;
        PDebug.End("BluetoothEnabler.resume");
    }

    public void pause() {
        if (this.mLocalAdapter == null) {
            return;
        }
        this.mSwitchBar.removeOnSwitchChangeListener(this);
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mValidListener = false;
    }

    void handleStateChanged(int state) {
        PDebug.Start("BluetoothEnabler.handleStateChanged");
        switch (state) {
            case 10:
                this.mUpdateStatusOnly = true;
                Log.d("BluetoothEnabler", "Begin update status: set mUpdateStatusOnly to true");
                setChecked(false);
                this.mSwitch.setEnabled(true);
                updateSearchIndex(false);
                this.mUpdateStatusOnly = false;
                Log.d("BluetoothEnabler", "End update status: set mUpdateStatusOnly to false");
                break;
            case 11:
                this.mSwitch.setEnabled(false);
                break;
            case 12:
                this.mUpdateStatusOnly = true;
                Log.d("BluetoothEnabler", "Begin update status: set mUpdateStatusOnly to true");
                setChecked(true);
                this.mSwitch.setEnabled(true);
                updateSearchIndex(true);
                this.mUpdateStatusOnly = false;
                Log.d("BluetoothEnabler", "End update status: set mUpdateStatusOnly to false");
                break;
            case 13:
                this.mSwitch.setEnabled(false);
                break;
            default:
                setChecked(false);
                this.mSwitch.setEnabled(true);
                updateSearchIndex(false);
                break;
        }
        PDebug.End("BluetoothEnabler.handleStateChanged");
    }

    private void setChecked(boolean isChecked) {
        if (isChecked == this.mSwitch.isChecked()) {
            return;
        }
        if (this.mValidListener) {
            this.mSwitchBar.removeOnSwitchChangeListener(this);
        }
        this.mSwitch.setChecked(isChecked);
        if (!this.mValidListener) {
            return;
        }
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    private void updateSearchIndex(boolean isBluetoothOn) {
        this.mHandler.removeMessages(0);
        Message msg = new Message();
        msg.what = 0;
        msg.getData().putBoolean("is_bluetooth_on", isBluetoothOn);
        this.mHandler.sendMessage(msg);
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Log.d("BluetoothEnabler", "onSwitchChanged to " + isChecked);
        if (isChecked && !WirelessUtils.isRadioAllowed(this.mContext, "bluetooth")) {
            Toast.makeText(this.mContext, (int) R.string.wifi_in_airplane_mode, 0).show();
            switchView.setChecked(false);
        }
        MetricsLogger.action(this.mContext, 159, isChecked);
        Log.d("BluetoothEnabler", "mUpdateStatusOnly is " + this.mUpdateStatusOnly);
        if (this.mLocalAdapter != null && !this.mUpdateStatusOnly) {
            boolean status = this.mLocalAdapter.setBluetoothEnabled(isChecked);
            if (isChecked && !status) {
                switchView.setChecked(false);
                this.mSwitch.setEnabled(true);
                this.mSwitchBar.setTextViewLabel(false);
                return;
            }
        }
        this.mSwitch.setEnabled(false);
    }
}
