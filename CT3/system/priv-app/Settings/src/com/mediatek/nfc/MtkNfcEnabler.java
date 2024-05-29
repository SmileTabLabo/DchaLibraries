package com.mediatek.nfc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Switch;
import com.android.settings.widget.SwitchBar;
/* loaded from: classes.dex */
public class MtkNfcEnabler implements SwitchBar.OnSwitchChangeListener {
    private final Context mContext;
    private final IntentFilter mIntentFilter;
    private final NfcAdapter mNfcAdapter;
    private int mNfcState = 1;
    private QueryTask mQueryTask = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.mediatek.nfc.MtkNfcEnabler.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!"android.nfc.action.ADAPTER_STATE_CHANGED".equals(action)) {
                return;
            }
            MtkNfcEnabler.this.mNfcState = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1);
            Log.d("@M_MtkNfcEnabler", "Receive nfc state changed : " + MtkNfcEnabler.this.mNfcState);
            if (MtkNfcEnabler.this.mNfcAdapter == null) {
                return;
            }
            MtkNfcEnabler.this.mQueryTask = new QueryTask(MtkNfcEnabler.this, null);
            MtkNfcEnabler.this.mQueryTask.execute(new Void[0]);
        }
    };
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private boolean mUpdateSwitchButtonOnly;

    public MtkNfcEnabler(Context context, SwitchBar switchBar, NfcAdapter adapter) {
        Log.d("@M_MtkNfcEnabler", "MtkNfcEnabler, switchBar = " + switchBar);
        this.mContext = context;
        this.mSwitchBar = switchBar;
        this.mSwitch = switchBar.getSwitch();
        this.mNfcAdapter = adapter;
        setupSwitchBar();
        this.mIntentFilter = new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED");
    }

    public void setupSwitchBar() {
        this.mSwitchBar.addOnSwitchChangeListener(this);
        this.mSwitchBar.show();
    }

    public void teardownSwitchBar() {
        this.mSwitchBar.removeOnSwitchChangeListener(this);
        this.mSwitchBar.hide();
    }

    public void resume() {
        Log.d("@M_MtkNfcEnabler", "Resume");
        if (this.mNfcAdapter != null) {
            this.mQueryTask = new QueryTask(this, null);
            this.mQueryTask.execute(new Void[0]);
        }
        handleNfcStateChanged(this.mNfcState);
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
    }

    public void pause() {
        Log.d("@M_MtkNfcEnabler", "Pause");
        if (this.mNfcAdapter == null) {
            return;
        }
        if (this.mQueryTask != null) {
            this.mQueryTask.cancel(true);
            Log.d("@M_MtkNfcEnabler", "mQueryTask.cancel(true)");
        }
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    private void setSwitchChecked(boolean checked) {
        if (checked == this.mSwitch.isChecked()) {
            return;
        }
        Log.d("@M_MtkNfcEnabler", "setSwitchChecked()  mUpdateSwitchButtonOnly = true ");
        this.mUpdateSwitchButtonOnly = true;
        this.mSwitch.setChecked(checked);
        this.mUpdateSwitchButtonOnly = false;
        Log.d("@M_MtkNfcEnabler", "setSwitchChecked()  mUpdateSwitchButtonOnly = false ");
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Log.d("@M_MtkNfcEnabler", "onSwitchChanged: " + isChecked + ", mNfcState: " + this.mNfcState);
        if (this.mNfcAdapter == null || this.mUpdateSwitchButtonOnly) {
            return;
        }
        if (isChecked && (this.mNfcState == 1 || this.mNfcState == 4)) {
            Log.d("@M_MtkNfcEnabler", "onSwitchChanged: enable NFC ");
            this.mNfcAdapter.enable();
            switchView.setEnabled(false);
        } else if (isChecked) {
        } else {
            if (this.mNfcState != 3 && this.mNfcState != 2) {
                return;
            }
            Log.d("@M_MtkNfcEnabler", "onSwitchChanged: disable NFC ");
            this.mNfcAdapter.disable();
            switchView.setEnabled(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNfcStateChanged(int newState) {
        Log.d("@M_MtkNfcEnabler", "handleNfcStateChanged  newState = " + newState);
        updateSwitch(newState);
    }

    private void updateSwitch(int state) {
        if (this.mSwitch == null) {
            return;
        }
        switch (state) {
            case 1:
                setSwitchChecked(false);
                this.mSwitch.setEnabled(true);
                return;
            case 2:
                setSwitchChecked(true);
                this.mSwitch.setEnabled(false);
                return;
            case 3:
                setSwitchChecked(true);
                this.mSwitch.setEnabled(true);
                return;
            case 4:
                setSwitchChecked(false);
                this.mSwitch.setEnabled(false);
                return;
            default:
                setSwitchChecked(false);
                return;
        }
    }

    /* loaded from: classes.dex */
    private class QueryTask extends AsyncTask<Void, Void, Integer> {
        /* synthetic */ QueryTask(MtkNfcEnabler this$0, QueryTask queryTask) {
            this();
        }

        private QueryTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Integer doInBackground(Void... params) {
            MtkNfcEnabler.this.mNfcState = MtkNfcEnabler.this.mNfcAdapter.getAdapterState();
            Log.d("@M_MtkNfcEnabler", "[QueryTask] doInBackground  mNfcState: " + MtkNfcEnabler.this.mNfcState);
            return Integer.valueOf(MtkNfcEnabler.this.mNfcState);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Integer result) {
            Log.d("@M_MtkNfcEnabler", "[QueryTask] onPostExecute: " + result);
            MtkNfcEnabler.this.handleNfcStateChanged(result.intValue());
        }
    }
}
