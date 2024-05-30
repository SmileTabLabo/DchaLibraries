package jp.co.benesse.dcha.systemsettings;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;
import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public class WifiSettingActivity extends ParentSettingActivity implements View.OnClickListener {
    private ImageView mBackBtn;
    private ImageView mConnectBtn;
    private IDchaService mDchaService;
    private ImageView mInternetBtn;
    private ImageView mNearestNetBtn;
    private ImageView mNextBtn;
    private ImageView mPankuzu;
    private Timer timer;
    private Handler mHandler = new Handler();
    private ServiceConnection mServiceConnection = new ServiceConnection() { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.d("WiFiSettingActivity", "onServiceConnected 0001");
            WifiSettingActivity.this.mDchaService = IDchaService.Stub.asInterface(iBinder);
            WifiSettingActivity.this.hideNavigationBar(true);
            Logger.d("WiFiSettingActivity", "onServiceConnected 0002");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.d("WiFiSettingActivity", "onServiceDisconnected 0001");
            WifiSettingActivity.this.mDchaService = null;
            Logger.d("WiFiSettingActivity", "onServiceDisconnected 0002");
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.d("WiFiSettingActivity", "onReceive 0001");
            String action = intent.getAction();
            if (action.equals("jp.co.benesse.dcha.allgrade.b001.ACTION_ACTIVATE")) {
                try {
                    Logger.d("WiFiSettingActivity", "onReceive 0002");
                    WifiSettingActivity.this.mDchaService.removeTask("jp.co.benesse.dcha.setupwizard");
                    WifiSettingActivity.this.mDchaService.removeTask("jp.co.benesse.dcha.allgrade.usersetting");
                    Logger.d("WiFiSettingActivity", "onReceive 0003");
                } catch (RemoteException e) {
                    Logger.d("WiFiSettingActivity", "onReceive 0004");
                    Logger.e("RemoteException", e);
                }
            } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                Logger.d("WiFiSettingActivity", "onReceive 0005");
                NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1);
                if (WifiSettingActivity.this.mNextBtn != null) {
                    Logger.d("WiFiSettingActivity", "onReceive 0006");
                    if (networkInfo != null && networkInfo.isConnected()) {
                        Logger.d("WiFiSettingActivity", "onReceive 0007");
                        WifiSettingActivity.this.mNextBtn.setEnabled(true);
                        WifiSettingActivity.this.mConnectBtn.setVisibility(0);
                    } else {
                        Logger.d("WiFiSettingActivity", "onReceive 0008");
                        WifiSettingActivity.this.mNextBtn.setEnabled(false);
                        WifiSettingActivity.this.mConnectBtn.setVisibility(4);
                    }
                }
            }
            Logger.d("WiFiSettingActivity", "onReceive 0009");
        }
    };

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        Logger.d("WiFiSettingActivity", "onCreate 0001");
        super.onCreate(bundle);
        setContentView(R.layout.act_wifi);
        this.mIsFirstFlow = getFirstFlg();
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        bindService(intent, this.mServiceConnection, 1);
        this.mNearestNetBtn = (ImageView) findViewById(R.id.nearestnet_btn);
        this.mConnectBtn = (ImageView) findViewById(R.id.connect_btn);
        this.mNextBtn = (ImageView) findViewById(R.id.next_btn);
        this.mBackBtn = (ImageView) findViewById(R.id.back_btn);
        this.mInternetBtn = (ImageView) findViewById(R.id.internet_btn);
        this.mPankuzu = (ImageView) findViewById(R.id.pankuzu);
        this.mNextBtn.setEnabled(false);
        this.mConnectBtn.setVisibility(4);
        this.mHandler.postDelayed(new Runnable() { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.3
            @Override // java.lang.Runnable
            public void run() {
                if (WifiSettingActivity.this.mNearestNetBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0005");
                    WifiSettingActivity.this.mNearestNetBtn.setOnClickListener(WifiSettingActivity.this);
                }
                if (WifiSettingActivity.this.mNextBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0010");
                    WifiSettingActivity.this.mNextBtn.setOnClickListener(WifiSettingActivity.this);
                }
                if (WifiSettingActivity.this.mBackBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0011");
                    WifiSettingActivity.this.mBackBtn.setOnClickListener(WifiSettingActivity.this);
                }
                if (WifiSettingActivity.this.mInternetBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0012");
                    WifiSettingActivity.this.mInternetBtn.setOnClickListener(WifiSettingActivity.this);
                }
            }
        }, 750L);
        if (this.mIsFirstFlow) {
            Logger.d("WiFiSettingActivity", "onCreate 0002");
            this.mBackBtn.setVisibility(8);
            this.mInternetBtn.setVisibility(8);
            getWindow().addFlags(128);
        } else {
            Logger.d("WiFiSettingActivity", "onCreate 0003");
            this.mNextBtn.setVisibility(8);
            this.mPankuzu.setVisibility(4);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("jp.co.benesse.dcha.allgrade.b001.ACTION_ACTIVATE");
        registerReceiver(this.mReceiver, intentFilter);
        Logger.d("WiFiSettingActivity", "onCreate 0004");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onStart() {
        Logger.d("WiFiSettingActivity", "onStart 0001");
        super.onStart();
        changeBtnClickable(true);
        if (this.timer != null) {
            Logger.d("WiFiSettingActivity", "onStart 0002");
            this.timer.cancel();
            this.timer = null;
        }
        Logger.d("WiFiSettingActivity", "onStart 0003");
    }

    @Override // android.app.Activity
    protected void onResume() {
        Logger.d("WiFiSettingActivity", "onResume 0001");
        super.onResume();
        hideNavigationBar(true);
        changeBtnClickable(true);
        Logger.d("WiFiSettingActivity", "onResume 0002");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onStop() {
        Logger.d("WiFiSettingActivity", "onStop 0001");
        super.onStop();
        if (this.mIsFirstFlow) {
            Logger.d("WiFiSettingActivity", "onStop 0003");
            startTimer();
        }
        Logger.d("WiFiSettingActivity", "onStop 0004");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onDestroy() {
        Logger.d("WiFiSettingActivity", "onDestroy 0001");
        super.onDestroy();
        if (getSetupStatus() == 3) {
            hideNavigationBar(true);
        }
        this.mNearestNetBtn.setOnClickListener(null);
        this.mInternetBtn.setOnClickListener(null);
        if (this.mServiceConnection != null) {
            Logger.d("WiFiSettingActivity", "onDestroy 0002");
            unbindService(this.mServiceConnection);
            this.mServiceConnection = null;
            this.mDchaService = null;
        }
        if (this.mReceiver != null) {
            Logger.d("WiFiSettingActivity", "onDestroy 0003");
            unregisterReceiver(this.mReceiver);
        }
        this.mNearestNetBtn = null;
        this.mConnectBtn = null;
        this.mNextBtn = null;
        this.mBackBtn = null;
        this.mInternetBtn = null;
        this.mPankuzu = null;
        this.mReceiver = null;
        this.mHandler = null;
        if (this.timer != null) {
            Logger.d("WiFiSettingActivity", "onDestroy 0004");
            this.timer.cancel();
            this.timer = null;
        }
        Logger.d("WiFiSettingActivity", "onDestroy 0005");
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d("WiFiSettingActivity", "onClick 0001");
        if (view.getId() == this.mNearestNetBtn.getId()) {
            Logger.d("WiFiSettingActivity", "onClick 0002");
            changeBtnClickable(false);
            moveNetworkSettingActivity();
        } else if (view.getId() == this.mNextBtn.getId()) {
            Logger.d("WiFiSettingActivity", "onClick 0005");
            changeBtnClickable(false);
            moveHealthCheckActivity();
        } else if (view.getId() == this.mBackBtn.getId()) {
            Logger.d("WiFiSettingActivity", "onClick 0006");
            changeBtnClickable(false);
            moveSettingActivity();
            finish();
        } else if (view.getId() == this.mInternetBtn.getId()) {
            Logger.d("WiFiSettingActivity", "onClick 0009");
            changeBtnClickable(false);
            moveHealthCheckActivity();
        }
        Logger.d("WiFiSettingActivity", "onClick 0010");
    }

    private void changeBtnClickable(boolean z) {
        Logger.d("WiFiSettingActivity", "changeBtnClickable 0001");
        if (z) {
            Logger.d("WiFiSettingActivity", "changeBtnClickable 0002");
            this.mNearestNetBtn.setClickable(true);
            this.mNextBtn.setClickable(true);
            this.mBackBtn.setClickable(true);
            this.mInternetBtn.setClickable(true);
            return;
        }
        Logger.d("WiFiSettingActivity", "changeBtnClickable 0003");
        this.mNearestNetBtn.setClickable(false);
        this.mNextBtn.setClickable(false);
        this.mBackBtn.setClickable(false);
        this.mInternetBtn.setClickable(false);
    }

    private void moveHealthCheckActivity() {
        Logger.d("WiFiSettingActivity", "moveHealthCheckActivity 0001");
        Intent intent = new Intent();
        if (this.mIsFirstFlow) {
            intent.setClassName("jp.co.benesse.dcha.systemsettings", "jp.co.benesse.dcha.systemsettings.HealthCheckActivity");
            Logger.d("WiFiSettingActivity", "moveHealthCheckActivity 0002");
        } else {
            intent.setClassName("jp.co.benesse.dcha.allgrade.usersetting", "jp.co.benesse.dcha.allgrade.usersetting.activity.HealthCheckActivity");
            Logger.d("WiFiSettingActivity", "moveHealthCheckActivity 0003");
        }
        intent.putExtra("first_flg", this.mIsFirstFlow);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        Logger.d("WiFiSettingActivity", "moveHealthCheckActivity 0004");
    }

    private void moveNetworkSettingActivity() {
        Logger.d("WiFiSettingActivity", "moveNetworkSettingActivity 0001");
        Intent intent = new Intent(this, NetworkSettingActivity.class);
        intent.putExtra("first_flg", this.mIsFirstFlow);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        Logger.d("WiFiSettingActivity", "moveNetworkSettingActivity 0002");
    }

    private void startTimer() {
        Logger.d("WiFiSettingActivity", "startTimer 0001");
        this.timer = new Timer(true);
        final Handler handler = new Handler();
        this.timer.schedule(new TimerTask() { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.4
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                Logger.d("WiFiSettingActivity", "run 0001");
                handler.post(new Runnable() { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Logger.d("WiFiSettingActivity", "run 0002");
                        String packageName = ((ActivityManager) WifiSettingActivity.this.getSystemService("activity")).getRunningTasks(1).get(0).baseActivity.getPackageName();
                        Logger.i("WiFiSettingActivity", "run foregroundTaskName = " + packageName);
                        if (packageName.equals("com.android.launcher")) {
                            Logger.d("WiFiSettingActivity", "run 0003");
                            try {
                                Logger.d("WiFiSettingActivity", "run 0004");
                                if (WifiSettingActivity.this.mDchaService != null) {
                                    Logger.d("WiFiSettingActivity", "run 0005");
                                    WifiSettingActivity.this.mDchaService.removeTask("jp.co.benesse.dcha.setupwizard");
                                }
                                Logger.d("WiFiSettingActivity", "run 0006");
                            } catch (RemoteException e) {
                                Logger.d("WiFiSettingActivity", "run 0007");
                                Logger.e("WiFiSettingActivity", "RemoteException", e);
                            }
                            WifiSettingActivity.this.doCancelDigicharize();
                            if (WifiSettingActivity.this.timer != null) {
                                WifiSettingActivity.this.timer.cancel();
                                WifiSettingActivity.this.timer = null;
                            }
                        }
                        Logger.d("WiFiSettingActivity", "run 0008");
                    }
                });
                Logger.d("WiFiSettingActivity", "run 0009");
            }
        }, 100L, 100L);
        Logger.d("WiFiSettingActivity", "startTimer 0002");
    }

    private int getSetupStatus() {
        Logger.d("WiFiSettingActivity", "getSetupStatus 0001");
        int i = -1;
        try {
            Logger.d("WiFiSettingActivity", "getSetupStatus 0002");
            if (this.mDchaService != null) {
                Logger.d("WiFiSettingActivity", "getSetupStatus 0003");
                i = this.mDchaService.getSetupStatus();
            }
        } catch (RemoteException e) {
            Logger.d("WiFiSettingActivity", "getSetupStatus 0004");
            Logger.e("WiFiSettingActivity", "getSetupStatus", e);
        }
        Logger.d("WiFiSettingActivity", "getSetupStatus 0005");
        return i;
    }
}
