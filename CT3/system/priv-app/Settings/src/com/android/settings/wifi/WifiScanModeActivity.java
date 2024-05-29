package com.android.settings.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import com.android.settings.R;
/* loaded from: classes.dex */
public class WifiScanModeActivity extends Activity {
    private String mApp;
    private DialogFragment mDialog;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (savedInstanceState == null) {
            if (intent != null && intent.getAction().equals("android.net.wifi.action.REQUEST_SCAN_ALWAYS_AVAILABLE")) {
                this.mApp = getCallingPackage();
                try {
                    PackageManager pm = getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo(this.mApp, 0);
                    this.mApp = (String) pm.getApplicationLabel(ai);
                } catch (PackageManager.NameNotFoundException e) {
                }
            } else {
                finish();
                return;
            }
        } else {
            this.mApp = savedInstanceState.getString("app");
        }
        createDialog();
    }

    private void createDialog() {
        if (this.mDialog != null) {
            return;
        }
        this.mDialog = AlertDialogFragment.newInstance(this.mApp);
        this.mDialog.show(getFragmentManager(), "dialog");
    }

    private void dismissDialog() {
        if (this.mDialog == null) {
            return;
        }
        this.mDialog.dismiss();
        this.mDialog = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doPositiveClick() {
        Settings.Global.putInt(getContentResolver(), "wifi_scan_always_enabled", 1);
        setResult(-1);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doNegativeClick() {
        setResult(0);
        finish();
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("app", this.mApp);
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        dismissDialog();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        createDialog();
    }

    /* loaded from: classes.dex */
    public static class AlertDialogFragment extends DialogFragment {
        private final String mApp;

        static AlertDialogFragment newInstance(String app) {
            AlertDialogFragment frag = new AlertDialogFragment(app);
            return frag;
        }

        public AlertDialogFragment(String app) {
            this.mApp = app;
        }

        public AlertDialogFragment() {
            this.mApp = null;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.wifi_scan_always_turnon_message, new Object[]{this.mApp})).setPositiveButton(R.string.wifi_scan_always_confirm_allow, new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.WifiScanModeActivity.AlertDialogFragment.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((WifiScanModeActivity) AlertDialogFragment.this.getActivity()).doPositiveClick();
                }
            }).setNegativeButton(R.string.wifi_scan_always_confirm_deny, new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.WifiScanModeActivity.AlertDialogFragment.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((WifiScanModeActivity) AlertDialogFragment.this.getActivity()).doNegativeClick();
                }
            }).create();
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialog) {
            ((WifiScanModeActivity) getActivity()).doNegativeClick();
        }
    }
}
