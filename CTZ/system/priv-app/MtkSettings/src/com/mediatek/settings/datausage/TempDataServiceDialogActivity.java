package com.mediatek.settings.datausage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import com.android.settings.R;
import com.mediatek.settings.sim.SimHotSwapHandler;
/* loaded from: classes.dex */
public class TempDataServiceDialogActivity extends Activity {
    private Dialog mDialog;
    private IntentFilter mIntentFilter;
    private SimHotSwapHandler mSimHotSwapHandler;
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() { // from class: com.mediatek.settings.datausage.TempDataServiceDialogActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("TempDataServiceDialogActivity", "onReceive, action=" + action);
            TempDataServiceDialogActivity.this.dismissTempDataDialog();
            TempDataServiceDialogActivity.this.finish();
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        Log.d("TempDataServiceDialogActivity", "onCreate");
        super.onCreate(bundle);
        this.mSimHotSwapHandler = new SimHotSwapHandler(getApplicationContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.mediatek.settings.datausage.TempDataServiceDialogActivity.2
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                Log.d("TempDataServiceDialogActivity", "onSimHotSwap, finish Activity.");
                TempDataServiceDialogActivity.this.dismissTempDataDialog();
                TempDataServiceDialogActivity.this.finish();
            }
        });
        this.mIntentFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
        registerReceiver(this.mSubReceiver, this.mIntentFilter);
        displayTempDataDialog();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Log.d("TempDataServiceDialogActivity", "onDestroy");
        this.mSimHotSwapHandler.unregisterOnSimHotSwap();
        unregisterReceiver(this.mSubReceiver);
        dismissTempDataDialog();
        super.onDestroy();
    }

    private void displayTempDataDialog() {
        Log.d("TempDataServiceDialogActivity", "displayTempDataDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.data_service_prompt);
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.datausage.TempDataServiceDialogActivity.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("TempDataServiceDialogActivity", "onClick, OK.");
                TempDataServiceDialogActivity.this.setDataService(1);
                TempDataServiceDialogActivity.this.dismissTempDataDialog();
                TempDataServiceDialogActivity.this.finish();
            }
        });
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.datausage.TempDataServiceDialogActivity.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("TempDataServiceDialogActivity", "onClick, CANCEL.");
                TempDataServiceDialogActivity.this.dismissTempDataDialog();
                TempDataServiceDialogActivity.this.finish();
            }
        });
        builder.setCancelable(false);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.mediatek.settings.datausage.TempDataServiceDialogActivity.5
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TempDataServiceDialogActivity", "onCancel");
                TempDataServiceDialogActivity.this.dismissTempDataDialog();
                TempDataServiceDialogActivity.this.finish();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.mediatek.settings.datausage.TempDataServiceDialogActivity.6
            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == 4) {
                    Log.d("TempDataServiceDialogActivity", "onKey, Back key.");
                    TempDataServiceDialogActivity.this.dismissTempDataDialog();
                    TempDataServiceDialogActivity.this.finish();
                    return true;
                }
                return false;
            }
        });
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissTempDataDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDataService(int i) {
        Log.d("TempDataServiceDialogActivity", "setDataService, value=" + i);
        Settings.Global.putInt(getContentResolver(), "data_service_enabled", i);
    }
}
