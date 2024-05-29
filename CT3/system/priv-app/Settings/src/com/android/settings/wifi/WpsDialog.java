package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.settings.R;
import java.util.Timer;
import java.util.TimerTask;
/* loaded from: classes.dex */
public class WpsDialog extends AlertDialog {
    private Button mButton;
    private Context mContext;
    DialogState mDialogState;
    private final IntentFilter mFilter;
    private Handler mHandler;
    private String mMsgString;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mReceiver;
    private TextView mTextView;
    private ProgressBar mTimeoutBar;
    private Timer mTimer;
    private View mView;
    private WifiManager mWifiManager;
    private WifiManager.WpsCallback mWpsListener;
    private int mWpsSetup;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public enum DialogState {
        WPS_INIT,
        WPS_START,
        WPS_COMPLETE,
        CONNECTED,
        WPS_FAILED;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static DialogState[] valuesCustom() {
            return values();
        }
    }

    public WpsDialog(Context context, int wpsSetup) {
        super(context);
        this.mHandler = new Handler();
        this.mMsgString = "";
        this.mDialogState = DialogState.WPS_INIT;
        this.mContext = context;
        this.mWpsSetup = wpsSetup;
        this.mWpsListener = new WifiManager.WpsCallback() { // from class: com.android.settings.wifi.WpsDialog.1WpsListener
            @Override // android.net.wifi.WifiManager.WpsCallback
            public void onStarted(String pin) {
                if (pin != null) {
                    WpsDialog.this.updateDialog(DialogState.WPS_START, String.format(WpsDialog.this.mContext.getString(R.string.wifi_wps_onstart_pin), pin));
                } else {
                    WpsDialog.this.updateDialog(DialogState.WPS_START, WpsDialog.this.mContext.getString(R.string.wifi_wps_onstart_pbc));
                }
            }

            @Override // android.net.wifi.WifiManager.WpsCallback
            public void onSucceeded() {
                WpsDialog.this.updateDialog(DialogState.WPS_COMPLETE, WpsDialog.this.mContext.getString(R.string.wifi_wps_complete));
            }

            @Override // android.net.wifi.WifiManager.WpsCallback
            public void onFailed(int reason) {
                String msg;
                switch (reason) {
                    case 1:
                        msg = WpsDialog.this.mContext.getString(R.string.wifi_wps_in_progress);
                        break;
                    case 2:
                    default:
                        msg = WpsDialog.this.mContext.getString(R.string.wifi_wps_failed_generic);
                        break;
                    case 3:
                        msg = WpsDialog.this.mContext.getString(R.string.wifi_wps_failed_overlap);
                        break;
                    case 4:
                        msg = WpsDialog.this.mContext.getString(R.string.wifi_wps_failed_wep);
                        break;
                    case 5:
                        msg = WpsDialog.this.mContext.getString(R.string.wifi_wps_failed_tkip);
                        break;
                }
                WpsDialog.this.updateDialog(DialogState.WPS_FAILED, msg);
            }
        };
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wifi.WpsDialog.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                WpsDialog.this.handleEvent(context2, intent);
            }
        };
        setCanceledOnTouchOutside(false);
    }

    @Override // android.app.Dialog
    public Bundle onSaveInstanceState() {
        Bundle bundle = super.onSaveInstanceState();
        bundle.putString("android:dialogState", this.mDialogState.toString());
        bundle.putString("android:dialogMsg", this.mMsgString.toString());
        return bundle;
    }

    @Override // android.app.Dialog
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        super.onRestoreInstanceState(savedInstanceState);
        DialogState dialogState = this.mDialogState;
        DialogState dialogState2 = DialogState.valueOf(savedInstanceState.getString("android:dialogState"));
        String msg = savedInstanceState.getString("android:dialogMsg");
        updateDialog(dialogState2, msg);
    }

    @Override // android.app.AlertDialog, android.app.Dialog
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WpsDialog", "onCreate");
        this.mView = getLayoutInflater().inflate(R.layout.wifi_wps_dialog, (ViewGroup) null);
        this.mTextView = (TextView) this.mView.findViewById(R.id.wps_dialog_txt);
        this.mTextView.setText(R.string.wifi_wps_setup_msg);
        this.mTimeoutBar = (ProgressBar) this.mView.findViewById(R.id.wps_timeout_bar);
        this.mTimeoutBar.setMax(120);
        this.mTimeoutBar.setProgress(0);
        this.mProgressBar = (ProgressBar) this.mView.findViewById(R.id.wps_progress_bar);
        this.mProgressBar.setVisibility(8);
        this.mButton = (Button) this.mView.findViewById(R.id.wps_dialog_btn);
        this.mButton.setText(R.string.wifi_cancel);
        this.mButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.wifi.WpsDialog.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                WpsDialog.this.dismiss();
            }
        });
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        setView(this.mView);
        if (savedInstanceState == null) {
            WpsInfo wpsConfig = new WpsInfo();
            wpsConfig.setup = this.mWpsSetup;
            this.mWifiManager.startWps(wpsConfig, this.mWpsListener);
        }
        super.onCreate(savedInstanceState);
    }

    @Override // android.app.Dialog
    protected void onStart() {
        Log.d("WpsDialog", "onStart");
        this.mTimer = new Timer(false);
        this.mTimer.schedule(new TimerTask() { // from class: com.android.settings.wifi.WpsDialog.3
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                WpsDialog.this.mHandler.post(new Runnable() { // from class: com.android.settings.wifi.WpsDialog.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        WpsDialog.this.mTimeoutBar.incrementProgressBy(1);
                    }
                });
            }
        }, 1000L, 1000L);
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
    }

    @Override // android.app.Dialog
    protected void onStop() {
        Log.d("WpsDialog", "onStop");
        if (this.mDialogState != DialogState.WPS_COMPLETE) {
            this.mWifiManager.cancelWps(null);
        }
        if (this.mReceiver != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        if (this.mTimer == null) {
            return;
        }
        this.mTimer.cancel();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDialog(final DialogState state, final String msg) {
        if (this.mDialogState.ordinal() >= state.ordinal()) {
            return;
        }
        this.mDialogState = state;
        this.mMsgString = msg;
        this.mHandler.post(new Runnable() { // from class: com.android.settings.wifi.WpsDialog.4

            /* renamed from: -com-android-settings-wifi-WpsDialog$DialogStateSwitchesValues  reason: not valid java name */
            private static final /* synthetic */ int[] f9comandroidsettingswifiWpsDialog$DialogStateSwitchesValues = null;

            /* renamed from: -getcom-android-settings-wifi-WpsDialog$DialogStateSwitchesValues  reason: not valid java name */
            private static /* synthetic */ int[] m1329x7e11868a() {
                if (f9comandroidsettingswifiWpsDialog$DialogStateSwitchesValues != null) {
                    return f9comandroidsettingswifiWpsDialog$DialogStateSwitchesValues;
                }
                int[] iArr = new int[DialogState.valuesCustom().length];
                try {
                    iArr[DialogState.CONNECTED.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[DialogState.WPS_COMPLETE.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[DialogState.WPS_FAILED.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[DialogState.WPS_INIT.ordinal()] = 4;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[DialogState.WPS_START.ordinal()] = 5;
                } catch (NoSuchFieldError e5) {
                }
                f9comandroidsettingswifiWpsDialog$DialogStateSwitchesValues = iArr;
                return iArr;
            }

            @Override // java.lang.Runnable
            public void run() {
                switch (m1329x7e11868a()[state.ordinal()]) {
                    case 1:
                    case 3:
                        WpsDialog.this.mButton.setText(WpsDialog.this.mContext.getString(R.string.dlg_ok));
                        WpsDialog.this.mTimeoutBar.setVisibility(8);
                        WpsDialog.this.mProgressBar.setVisibility(8);
                        if (WpsDialog.this.mReceiver != null) {
                            WpsDialog.this.mContext.unregisterReceiver(WpsDialog.this.mReceiver);
                            WpsDialog.this.mReceiver = null;
                            break;
                        }
                        break;
                    case 2:
                        WpsDialog.this.mTimeoutBar.setVisibility(8);
                        WpsDialog.this.mProgressBar.setVisibility(0);
                        break;
                }
                WpsDialog.this.mTextView.setText(msg);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleEvent(Context context, Intent intent) {
        android.net.wifi.WifiInfo wifiInfo;
        String action = intent.getAction();
        if ("android.net.wifi.STATE_CHANGE".equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            NetworkInfo.DetailedState state = info.getDetailedState();
            if (state != NetworkInfo.DetailedState.CONNECTED || this.mDialogState != DialogState.WPS_COMPLETE || (wifiInfo = this.mWifiManager.getConnectionInfo()) == null) {
                return;
            }
            String msg = String.format(this.mContext.getString(R.string.wifi_wps_connected), wifiInfo.getSSID());
            updateDialog(DialogState.CONNECTED, msg);
        } else if (!"android.net.wifi.WIFI_STATE_CHANGED".equals(action) || intent.getIntExtra("wifi_state", 4) != 1) {
        } else {
            Log.d("WpsDialog", "handleEvent, wifi disabled");
            dismiss();
        }
    }
}
