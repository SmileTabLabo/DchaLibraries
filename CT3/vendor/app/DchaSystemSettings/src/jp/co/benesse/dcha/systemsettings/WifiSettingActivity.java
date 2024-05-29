package jp.co.benesse.dcha.systemsettings;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WifiSettingActivity.class */
public class WifiSettingActivity extends ParentSettingActivity implements View.OnClickListener {
    private ImageView mBackBtn;
    private ImageView mConnectBtn;
    private IDchaService mDchaService;
    private WpsDialog mDialog;
    private ImageView mInternetBtn;
    private ImageView mNearestNetBtn;
    private ImageView mNextBtn;
    private ImageView mPankuzu;
    private ImageView mWpsPinBtn;
    private ImageView mWpsPushBtn;
    private Timer timer;
    private Handler mHandler = new Handler();
    private ServiceConnection mServiceConnection = new ServiceConnection(this) { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.1
        final WifiSettingActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.d("WiFiSettingActivity", "onServiceConnected 0001");
            this.this$0.mDchaService = IDchaService.Stub.asInterface(iBinder);
            this.this$0.hideNavigationBar(true);
            Logger.d("WiFiSettingActivity", "onServiceConnected 0002");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.d("WiFiSettingActivity", "onServiceDisconnected 0001");
            this.this$0.mDchaService = null;
            Logger.d("WiFiSettingActivity", "onServiceDisconnected 0002");
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.2
        final WifiSettingActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.d("WiFiSettingActivity", "onReceive 0001");
            String action = intent.getAction();
            if (action.equals("jp.co.benesse.dcha.allgrade.b001.ACTION_ACTIVATE")) {
                try {
                    Logger.d("WiFiSettingActivity", "onReceive 0002");
                    this.this$0.mDchaService.removeTask("jp.co.benesse.dcha.setupwizard");
                    this.this$0.mDchaService.removeTask("jp.co.benesse.dcha.allgrade.usersetting");
                    Logger.d("WiFiSettingActivity", "onReceive 0003");
                } catch (RemoteException e) {
                    Logger.d("WiFiSettingActivity", "onReceive 0004");
                    Logger.e("RemoteException", e);
                }
            } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                Logger.d("WiFiSettingActivity", "onReceive 0005");
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (this.this$0.mNextBtn != null) {
                    Logger.d("WiFiSettingActivity", "onReceive 0006");
                    if (networkInfo.isConnected()) {
                        Logger.d("WiFiSettingActivity", "onReceive 0007");
                        this.this$0.mNextBtn.setEnabled(true);
                        this.this$0.mConnectBtn.setVisibility(0);
                    } else {
                        Logger.d("WiFiSettingActivity", "onReceive 0008");
                        this.this$0.mNextBtn.setEnabled(false);
                        this.this$0.mConnectBtn.setVisibility(4);
                    }
                }
            }
            Logger.d("WiFiSettingActivity", "onReceive 0009");
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: jp.co.benesse.dcha.systemsettings.WifiSettingActivity$4  reason: invalid class name */
    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WifiSettingActivity$4.class */
    public class AnonymousClass4 extends TimerTask {
        final WifiSettingActivity this$0;
        final Handler val$handler;

        AnonymousClass4(WifiSettingActivity wifiSettingActivity, Handler handler) {
            this.this$0 = wifiSettingActivity;
            this.val$handler = handler;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Logger.d("WiFiSettingActivity", "run 0001");
            this.val$handler.post(new Runnable(this) { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.4.1
                final AnonymousClass4 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    Logger.d("WiFiSettingActivity", "run 0002");
                    String packageName = ((ActivityManager) this.this$1.this$0.getSystemService("activity")).getRunningTasks(1).get(0).baseActivity.getPackageName();
                    Logger.i("WiFiSettingActivity", "run foregroundTaskName = " + packageName);
                    if (packageName.equals("com.android.launcher")) {
                        Logger.d("WiFiSettingActivity", "run 0003");
                        try {
                            Logger.d("WiFiSettingActivity", "run 0004");
                            if (this.this$1.this$0.mDchaService != null) {
                                Logger.d("WiFiSettingActivity", "run 0005");
                                this.this$1.this$0.mDchaService.removeTask("jp.co.benesse.dcha.setupwizard");
                            }
                            Logger.d("WiFiSettingActivity", "run 0006");
                        } catch (RemoteException e) {
                            Logger.d("WiFiSettingActivity", "run 0007");
                            Logger.e("WiFiSettingActivity", "RemoteException", e);
                        }
                        this.this$1.this$0.doCancelDigicharize();
                        if (this.this$1.this$0.timer != null) {
                            this.this$1.this$0.timer.cancel();
                            this.this$1.this$0.timer = null;
                        }
                    }
                    Logger.d("WiFiSettingActivity", "run 0008");
                }
            });
            Logger.d("WiFiSettingActivity", "run 0009");
        }
    }

    private void changeBtnClickable(boolean z) {
        Logger.d("WiFiSettingActivity", "changeBtnClickable 0001");
        if (z) {
            Logger.d("WiFiSettingActivity", "changeBtnClickable 0002");
            this.mNearestNetBtn.setClickable(true);
            this.mNextBtn.setClickable(true);
            this.mBackBtn.setClickable(true);
            this.mWpsPushBtn.setClickable(true);
            this.mWpsPinBtn.setClickable(true);
            this.mInternetBtn.setClickable(true);
            return;
        }
        Logger.d("WiFiSettingActivity", "changeBtnClickable 0003");
        this.mNearestNetBtn.setClickable(false);
        this.mNextBtn.setClickable(false);
        this.mBackBtn.setClickable(false);
        this.mWpsPushBtn.setClickable(false);
        this.mWpsPinBtn.setClickable(false);
        this.mInternetBtn.setClickable(false);
    }

    private int getSetupStatus() {
        int i;
        Logger.d("WiFiSettingActivity", "getSetupStatus 0001");
        try {
            Logger.d("WiFiSettingActivity", "getSetupStatus 0002");
            i = -1;
            if (this.mDchaService != null) {
                Logger.d("WiFiSettingActivity", "getSetupStatus 0003");
                i = this.mDchaService.getSetupStatus();
            }
        } catch (RemoteException e) {
            Logger.d("WiFiSettingActivity", "getSetupStatus 0004");
            Logger.e("WiFiSettingActivity", "getSetupStatus", e);
            i = -1;
        }
        Logger.d("WiFiSettingActivity", "getSetupStatus 0005");
        return i;
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
        finish();
        Logger.d("WiFiSettingActivity", "moveHealthCheckActivity 0004");
    }

    private void moveNetworkSettingActivity() {
        Logger.d("WiFiSettingActivity", "moveNetworkSettingActivity 0001");
        Intent intent = new Intent(this, NetworkSettingActivity.class);
        intent.putExtra("first_flg", this.mIsFirstFlow);
        startActivity(intent);
        finish();
        Logger.d("WiFiSettingActivity", "moveNetworkSettingActivity 0002");
    }

    private void startTimer() {
        Logger.d("WiFiSettingActivity", "startTimer 0001");
        this.timer = new Timer(true);
        this.timer.schedule(new AnonymousClass4(this, new Handler()), 100L, 100L);
        Logger.d("WiFiSettingActivity", "startTimer 0002");
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
        } else if (view.getId() == this.mWpsPushBtn.getId()) {
            Logger.d("WiFiSettingActivity", "onClick 0007");
            this.mDialog = new WpsDialog(this, 0);
            this.mDialog.show();
        } else if (view.getId() == this.mWpsPinBtn.getId()) {
            Logger.d("WiFiSettingActivity", "onClick 0008");
            this.mDialog = new WpsDialog(this, 1);
            this.mDialog.show();
        } else if (view.getId() == this.mInternetBtn.getId()) {
            Logger.d("WiFiSettingActivity", "onClick 0009");
            changeBtnClickable(false);
            moveHealthCheckActivity();
        }
        Logger.d("WiFiSettingActivity", "onClick 0010");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        Logger.d("WiFiSettingActivity", "onCreate 0001");
        super.onCreate(bundle);
        setContentView(2130903045);
        this.mIsFirstFlow = getFirstFlg();
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        bindService(intent, this.mServiceConnection, 1);
        this.mNearestNetBtn = (ImageView) findViewById(2131361831);
        this.mWpsPushBtn = (ImageView) findViewById(2131361832);
        this.mWpsPinBtn = (ImageView) findViewById(2131361833);
        this.mConnectBtn = (ImageView) findViewById(2131361830);
        this.mNextBtn = (ImageView) findViewById(2131361813);
        this.mBackBtn = (ImageView) findViewById(2131361814);
        this.mInternetBtn = (ImageView) findViewById(2131361834);
        this.mPankuzu = (ImageView) findViewById(2131361794);
        this.mNextBtn.setEnabled(false);
        this.mConnectBtn.setVisibility(4);
        this.mHandler.postDelayed(new Runnable(this) { // from class: jp.co.benesse.dcha.systemsettings.WifiSettingActivity.3
            final WifiSettingActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mNearestNetBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0005");
                    this.this$0.mNearestNetBtn.setOnClickListener(this.this$0);
                }
                if (this.this$0.mWpsPushBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0008");
                    this.this$0.mWpsPushBtn.setOnClickListener(this.this$0);
                }
                if (this.this$0.mWpsPinBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0009");
                    this.this$0.mWpsPinBtn.setOnClickListener(this.this$0);
                }
                if (this.this$0.mNextBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0010");
                    this.this$0.mNextBtn.setOnClickListener(this.this$0);
                }
                if (this.this$0.mBackBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0011");
                    this.this$0.mBackBtn.setOnClickListener(this.this$0);
                }
                if (this.this$0.mInternetBtn != null) {
                    Logger.d("WiFiSettingActivity", "onCreate 0012");
                    this.this$0.mInternetBtn.setOnClickListener(this.this$0);
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
    public void onDestroy() {
        Logger.d("WiFiSettingActivity", "onDestroy 0001");
        super.onDestroy();
        if (getSetupStatus() == 3) {
            hideNavigationBar(true);
        }
        this.mNearestNetBtn.setOnClickListener(null);
        this.mWpsPushBtn.setOnClickListener(null);
        this.mWpsPinBtn.setOnClickListener(null);
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
        this.mWpsPushBtn = null;
        this.mWpsPinBtn = null;
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

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onStop() {
        Logger.d("WiFiSettingActivity", "onStop 0001");
        super.onStop();
        if (this.mDialog != null) {
            Logger.d("WiFiSettingActivity", "onStop 0002");
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        if (this.mIsFirstFlow) {
            Logger.d("WiFiSettingActivity", "onStop 0003");
            startTimer();
        }
        Logger.d("WiFiSettingActivity", "onStop 0004");
    }
}
