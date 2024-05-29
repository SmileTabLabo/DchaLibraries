package com.android.settings.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.appcompat.R;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.bluetooth.RequestPermissionActivity;
import com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.wifi.AccessPoint;
/* loaded from: classes.dex */
public class RequestPermissionActivity extends Activity implements DialogInterface.OnClickListener {
    private static int mRequestCode = 1;
    private CharSequence mAppLabel;
    private AlertDialog mDialog;
    private LocalBluetoothAdapter mLocalAdapter;
    private BroadcastReceiver mReceiver;
    private int mRequest;
    private int mTimeout = R.styleable.AppCompatTheme_windowNoTitle;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addPrivateFlags(524288);
        setResult(0);
        if (parseIntent()) {
            finish();
            return;
        }
        int state = this.mLocalAdapter.getState();
        if (this.mRequest == 3) {
            switch (state) {
                case 10:
                case 13:
                    proceedAndFinish();
                    return;
                case 11:
                case 12:
                    Intent intent = new Intent(this, RequestPermissionHelperActivity.class);
                    intent.putExtra("com.android.settings.bluetooth.extra.APP_LABEL", this.mAppLabel);
                    intent.setAction("com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_OFF");
                    startActivityForResult(intent, 0);
                    return;
                default:
                    Log.e("RequestPermissionActivity", "Unknown adapter state: " + state);
                    cancelAndFinish();
                    return;
            }
        }
        switch (state) {
            case 10:
            case 11:
            case 13:
                Intent intent2 = new Intent(this, RequestPermissionHelperActivity.class);
                intent2.setAction("com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON");
                intent2.setFlags(67108864);
                intent2.putExtra("com.android.settings.bluetooth.extra.APP_LABEL", this.mAppLabel);
                if (this.mRequest == 2) {
                    intent2.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", this.mTimeout);
                }
                startActivityForResult(intent2, mRequestCode);
                mRequestCode++;
                return;
            case 12:
                if (this.mRequest == 1) {
                    proceedAndFinish();
                    return;
                } else {
                    createDialog();
                    return;
                }
            default:
                Log.e("RequestPermissionActivity", "Unknown adapter state: " + state);
                cancelAndFinish();
                return;
        }
    }

    private void createDialog() {
        String string;
        String string2;
        if (getResources().getBoolean(com.android.settings.R.bool.auto_confirm_bluetooth_activation_dialog)) {
            onClick(null, -1);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (this.mReceiver != null) {
            switch (this.mRequest) {
                case 1:
                case 2:
                    builder.setMessage(getString(com.android.settings.R.string.bluetooth_turning_on));
                    break;
                default:
                    builder.setMessage(getString(com.android.settings.R.string.bluetooth_turning_off));
                    break;
            }
            builder.setCancelable(false);
        } else {
            if (this.mTimeout == 0) {
                if (this.mAppLabel != null) {
                    string2 = getString(com.android.settings.R.string.bluetooth_ask_lasting_discovery, new Object[]{this.mAppLabel});
                } else {
                    string2 = getString(com.android.settings.R.string.bluetooth_ask_lasting_discovery_no_name);
                }
                builder.setMessage(string2);
            } else {
                if (this.mAppLabel != null) {
                    string = getString(com.android.settings.R.string.bluetooth_ask_discovery, new Object[]{this.mAppLabel, Integer.valueOf(this.mTimeout)});
                } else {
                    string = getString(com.android.settings.R.string.bluetooth_ask_discovery_no_name, new Object[]{Integer.valueOf(this.mTimeout)});
                }
                builder.setMessage(string);
            }
            builder.setPositiveButton(getString(com.android.settings.R.string.allow), this);
            builder.setNegativeButton(getString(com.android.settings.R.string.deny), this);
        }
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        if (i != mRequestCode - 1) {
            Log.e("RequestPermissionActivity", "Unexpected onActivityResult " + i + ' ' + i2);
            setResult(0);
            finish();
        } else if (i2 != -1) {
            cancelAndFinish();
        } else {
            switch (this.mRequest) {
                case 1:
                case 2:
                    if (this.mLocalAdapter.getBluetoothState() == 12) {
                        proceedAndFinish();
                        return;
                    }
                    this.mReceiver = new StateChangeReceiver();
                    registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
                    createDialog();
                    return;
                case 3:
                    if (this.mLocalAdapter.getBluetoothState() == 10) {
                        proceedAndFinish();
                        return;
                    }
                    this.mReceiver = new StateChangeReceiver();
                    registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
                    createDialog();
                    return;
                default:
                    cancelAndFinish();
                    return;
            }
        }
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case -2:
                setResult(0);
                finish();
                return;
            case -1:
                proceedAndFinish();
                return;
            default:
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void proceedAndFinish() {
        int i;
        if (this.mRequest == 1 || this.mRequest == 3) {
            i = -1;
        } else if (this.mLocalAdapter.setScanMode(23, this.mTimeout)) {
            long currentTimeMillis = System.currentTimeMillis() + (this.mTimeout * 1000);
            LocalBluetoothPreferences.persistDiscoverableEndTimestamp(this, currentTimeMillis);
            if (this.mTimeout > 0) {
                BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(this, currentTimeMillis);
            }
            i = this.mTimeout;
            if (i < 1) {
                i = 1;
            }
        } else {
            i = 0;
        }
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
        setResult(i);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelAndFinish() {
        setResult(0);
        finish();
    }

    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return true;
        }
        if (intent.getAction().equals("android.bluetooth.adapter.action.REQUEST_ENABLE")) {
            this.mRequest = 1;
        } else if (intent.getAction().equals("android.bluetooth.adapter.action.REQUEST_DISABLE")) {
            this.mRequest = 3;
        } else if (intent.getAction().equals("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE")) {
            this.mRequest = 2;
            this.mTimeout = intent.getIntExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", R.styleable.AppCompatTheme_windowNoTitle);
            Log.d("RequestPermissionActivity", "Setting Bluetooth Discoverable Timeout = " + this.mTimeout);
            if (this.mTimeout < 1 || this.mTimeout > 3600) {
                this.mTimeout = R.styleable.AppCompatTheme_windowNoTitle;
            }
        } else {
            Log.e("RequestPermissionActivity", "Error: this activity may be started only with intent android.bluetooth.adapter.action.REQUEST_ENABLE or android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            setResult(0);
            return true;
        }
        LocalBluetoothManager localBtManager = Utils.getLocalBtManager(this);
        if (localBtManager == null) {
            Log.e("RequestPermissionActivity", "Error: there's a problem starting Bluetooth");
            setResult(0);
            return true;
        }
        String callingPackage = getCallingPackage();
        if (TextUtils.isEmpty(callingPackage)) {
            callingPackage = getIntent().getStringExtra("android.intent.extra.PACKAGE_NAME");
        }
        if (!TextUtils.isEmpty(callingPackage)) {
            try {
                this.mAppLabel = getPackageManager().getApplicationInfo(callingPackage, 0).loadSafeLabel(getPackageManager());
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("RequestPermissionActivity", "Couldn't find app with package name " + callingPackage);
                setResult(0);
                return true;
            }
        }
        this.mLocalAdapter = localBtManager.getBluetoothAdapter();
        return false;
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        if (this.mReceiver != null) {
            unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        setResult(0);
        super.onBackPressed();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class StateChangeReceiver extends BroadcastReceiver {
        public StateChangeReceiver() {
            RequestPermissionActivity.this.getWindow().getDecorView().postDelayed(new Runnable() { // from class: com.android.settings.bluetooth.-$$Lambda$RequestPermissionActivity$StateChangeReceiver$q4ZilZjRzY7SLoogXiJIIa__yMA
                @Override // java.lang.Runnable
                public final void run() {
                    RequestPermissionActivity.StateChangeReceiver.lambda$new$0(RequestPermissionActivity.StateChangeReceiver.this);
                }
            }, 10000L);
        }

        public static /* synthetic */ void lambda$new$0(StateChangeReceiver stateChangeReceiver) {
            if (!RequestPermissionActivity.this.isFinishing() && !RequestPermissionActivity.this.isDestroyed()) {
                RequestPermissionActivity.this.cancelAndFinish();
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", AccessPoint.UNREACHABLE_RSSI);
            switch (RequestPermissionActivity.this.mRequest) {
                case 1:
                case 2:
                    if (intExtra == 12) {
                        RequestPermissionActivity.this.proceedAndFinish();
                        return;
                    }
                    return;
                case 3:
                    if (intExtra == 10) {
                        RequestPermissionActivity.this.proceedAndFinish();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
