package com.mediatek.settings.cdma;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.android.settings.R;
import com.mediatek.settings.sim.SimHotSwapHandler;
/* loaded from: classes.dex */
public class CdmaSimDialogActivity extends Activity {
    private Dialog mDialog;
    private IntentFilter mIntentFilter;
    private SimHotSwapHandler mSimHotSwapHandler;
    private StatusBarManager mStatusBarManager;
    private int mTargetSubId = -1;
    private int mActionType = -1;
    private PhoneAccountHandle mHandle = null;
    private int mDialogType = -1;
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("CdmaSimDialogActivity", "mSubReceiver action = " + action);
            CdmaSimDialogActivity.this.finish();
        }
    };

    private void init() {
        this.mSimHotSwapHandler = new SimHotSwapHandler(getApplicationContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.2
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                Log.d("CdmaSimDialogActivity", "onSimHotSwap, finish Activity~~");
                CdmaSimDialogActivity.this.finish();
            }
        });
        this.mIntentFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
        registerReceiver(this.mSubReceiver, this.mIntentFilter);
        this.mStatusBarManager = (StatusBarManager) getSystemService("statusbar");
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("CdmaSimDialogActivity", "onCreate");
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        init();
        if (extras != null) {
            int dialogType = extras.getInt("dialog_type", -1);
            this.mTargetSubId = extras.getInt("target_subid", -1);
            this.mActionType = extras.getInt("action_type", -1);
            this.mDialogType = dialogType;
            Log.d("CdmaSimDialogActivity", "dialogType: " + dialogType + " targetSubId: " + this.mTargetSubId + " actionType: " + this.mActionType);
            switch (dialogType) {
                case 0:
                    displayDualCdmaDialog();
                    return;
                case 1:
                    displayAlertCdmaDialog();
                    return;
                case 2:
                    displayOmhWarningDialog();
                    return;
                case 3:
                    displayOmhDataPickDialog();
                    return;
                default:
                    throw new IllegalArgumentException("Invalid dialog type " + dialogType + " sent.");
            }
        }
        Log.e("CdmaSimDialogActivity", "unexpect happend");
        finish();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mSimHotSwapHandler.unregisterOnSimHotSwap();
        unregisterReceiver(this.mSubReceiver);
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            return;
        }
        this.mDialog.dismiss();
        this.mDialog = null;
    }

    private void displayDualCdmaDialog() {
        Log.d("CdmaSimDialogActivity", "displayDualCdmaDialog...");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.two_cdma_dialog_msg);
        alertDialogBuilder.setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                CdmaSimDialogActivity.this.finish();
            }
        });
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.4
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                CdmaSimDialogActivity.this.finish();
            }
        });
        alertDialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.5
            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == 4) {
                    CdmaSimDialogActivity.this.finish();
                    return true;
                }
                return false;
            }
        });
        this.mDialog = alertDialogBuilder.create();
        this.mDialog.show();
    }

    private void displayAlertCdmaDialog() {
        Log.d("CdmaSimDialogActivity", "displayAlertCdmaDialog...");
        SubscriptionInfo defaultSir = null;
        int[] list = SubscriptionManager.from(this).getActiveSubscriptionIdList();
        for (int i : list) {
            if (i != this.mTargetSubId) {
                defaultSir = SubscriptionManager.from(this).getActiveSubscriptionInfo(i);
            }
        }
        if (defaultSir != null) {
            String switchDataAlertMessage = getResources().getString(R.string.default_data_switch_msg, defaultSir.getDisplayName());
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(switchDataAlertMessage);
            alertDialogBuilder.setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.6
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    if (dialog != null) {
                        Log.d("CdmaSimDialogActivity", "displayAlertCdmaDialog, set data sub to " + CdmaSimDialogActivity.this.mTargetSubId);
                        CdmaSimDialogActivity.this.setDefaultDataSubId(CdmaSimDialogActivity.this, CdmaSimDialogActivity.this.mTargetSubId);
                        dialog.dismiss();
                    }
                    CdmaSimDialogActivity.this.finish();
                }
            });
            alertDialogBuilder.setNegativeButton(17039360, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.7
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    CdmaSimDialogActivity.this.finish();
                }
            });
            alertDialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.8
                @Override // android.content.DialogInterface.OnKeyListener
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 4) {
                        CdmaSimDialogActivity.this.finish();
                        return true;
                    }
                    return false;
                }
            });
            alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.9
                @Override // android.content.DialogInterface.OnCancelListener
                public void onCancel(DialogInterface dialog) {
                    CdmaSimDialogActivity.this.finish();
                }
            });
            this.mDialog = alertDialogBuilder.create();
            this.mDialog.show();
            return;
        }
        Log.d("CdmaSimDialogActivity", "no need to show the alert dialog");
    }

    private void displayOmhWarningDialog() {
        Log.d("CdmaSimDialogActivity", "displayOmhWarningDialog...");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.omh_warning_dialog_msg);
        alertDialogBuilder.setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.10
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                CdmaSimDialogActivity.this.finish();
            }
        });
        alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.11
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialog) {
                Log.d("CdmaSimDialogActivity", "OMH warning dialog dismissed...");
                OmhEventHandler.getInstance(CdmaSimDialogActivity.this).sendEmptyMessage(103);
                CdmaSimDialogActivity.this.enableStatusBarNavigation(true);
            }
        });
        alertDialogBuilder.setCancelable(false);
        this.mDialog = alertDialogBuilder.create();
        this.mDialog.show();
        enableStatusBarNavigation(false);
    }

    @Override // android.app.Activity
    protected void onResume() {
        if (this.mDialogType == 2) {
            enableStatusBarNavigation(false);
        }
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onPause() {
        if (this.mDialogType == 2) {
            OmhEventHandler.getInstance(this).sendEmptyMessage(103);
            enableStatusBarNavigation(true);
        }
        super.onPause();
    }

    private void displayOmhDataPickDialog() {
        Log.d("CdmaSimDialogActivity", "displayOmhDataPickDialog...");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.omh_data_pick_dialog_msg);
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    Log.d("CdmaSimDialogActivity", "OMH data pick dialog, set data sub to " + CdmaSimDialogActivity.this.mTargetSubId);
                    CdmaSimDialogActivity.this.setDefaultDataSubId(CdmaSimDialogActivity.this, CdmaSimDialogActivity.this.mTargetSubId);
                    dialog.dismiss();
                }
                CdmaSimDialogActivity.this.finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.13
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                CdmaSimDialogActivity.this.finish();
            }
        });
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.mediatek.settings.cdma.CdmaSimDialogActivity.14
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                CdmaSimDialogActivity.this.finish();
            }
        });
        this.mDialog = alertDialogBuilder.create();
        this.mDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void enableStatusBarNavigation(boolean enable) {
        int state = 0;
        if (!enable) {
            int state2 = 2097152 | 16777216;
            state = state2 | 4194304 | 33554432;
        }
        Log.d("CdmaSimDialogActivity", "enableStatusBarNavigation, enable = " + enable);
        this.mStatusBarManager.disable(state);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDefaultDataSubId(Context context, int subId) {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        subscriptionManager.setDefaultDataSubId(subId);
        if (this.mActionType != 0) {
            return;
        }
        Toast.makeText(context, (int) R.string.data_switch_started, 1).show();
    }
}
