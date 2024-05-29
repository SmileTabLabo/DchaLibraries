package jp.co.benesse.dcha.systemsettings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/HealthCheckActivity.class */
public class HealthCheckActivity extends ParentSettingActivity implements View.OnClickListener {
    private CheckNetworkTask checkNetworkTask;
    private HealthCheckDto healthCheckDto;

    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/HealthCheckActivity$CheckNetworkTask.class */
    protected static class CheckNetworkTask extends AsyncTask<Void, HealthCheckDto, HealthCheckDto> {
        private final String TAG = "CheckNetworkTask";
        protected HealthCheckDto healthCheckDto = null;
        protected HealthCheckLogic logic;
        private WeakReference<Activity> owner;

        public CheckNetworkTask(Activity activity) {
            Logger.d("CheckNetworkTask", "CheckNetworkTask 0001");
            this.owner = new WeakReference<>(activity);
            this.logic = new HealthCheckLogic();
            Logger.d("CheckNetworkTask", "CheckNetworkTask 0002");
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public HealthCheckDto doInBackground(Void... voidArr) {
            Logger.d("CheckNetworkTask", "doInBackground 0001");
            if (isCancelled()) {
                Logger.d("CheckNetworkTask", "doInBackground 0002");
                return null;
            }
            try {
                Activity activity = this.owner.get();
                if (activity != null) {
                    Logger.d("CheckNetworkTask", "doInBackground 0003");
                    WifiManager wifiManager = (WifiManager) activity.getSystemService("wifi");
                    RotateAsyncTask rotateAsyncTask = new RotateAsyncTask(activity, 2131361796);
                    try {
                        rotateAsyncTask.executeOnExecutor(THREAD_POOL_EXECUTOR, new Void[0]);
                        this.logic.getMacAddress(activity, wifiManager.getConnectionInfo(), this.healthCheckDto);
                        this.logic.checkSsid(activity, wifiManager.getConfiguredNetworks(), this.healthCheckDto);
                        rotateAsyncTask.cancel(true);
                        publishProgress(this.healthCheckDto);
                        if (isCancelled() || this.healthCheckDto.isCheckedSsid == 2131230961) {
                            Logger.d("CheckNetworkTask", "doInBackground 0004");
                            this.healthCheckDto.isHealthChecked = 2131230961;
                            HealthCheckDto healthCheckDto = this.healthCheckDto;
                            if (this.logic != null) {
                                this.logic = null;
                            }
                            return healthCheckDto;
                        }
                        RotateAsyncTask rotateAsyncTask2 = new RotateAsyncTask(activity, 2131361799);
                        rotateAsyncTask2.executeOnExecutor(THREAD_POOL_EXECUTOR, new Void[0]);
                        this.logic.checkWifi(wifiManager.getConnectionInfo(), this.healthCheckDto);
                        rotateAsyncTask2.cancel(true);
                        publishProgress(this.healthCheckDto);
                        if (isCancelled() || this.healthCheckDto.isCheckedWifi == 2131230961) {
                            Logger.d("CheckNetworkTask", "doInBackground 0005");
                            this.healthCheckDto.isHealthChecked = 2131230961;
                            HealthCheckDto healthCheckDto2 = this.healthCheckDto;
                            if (this.logic != null) {
                                this.logic = null;
                            }
                            return healthCheckDto2;
                        }
                        RotateAsyncTask rotateAsyncTask3 = new RotateAsyncTask(activity, 2131361802);
                        rotateAsyncTask3.executeOnExecutor(THREAD_POOL_EXECUTOR, new Void[0]);
                        this.logic.checkIpAddress(activity, wifiManager.getDhcpInfo(), this.healthCheckDto);
                        rotateAsyncTask3.cancel(true);
                        publishProgress(this.healthCheckDto);
                        if (isCancelled() || this.healthCheckDto.isCheckedIpAddress == 2131230961) {
                            Logger.d("CheckNetworkTask", "doInBackground 0006");
                            this.healthCheckDto.isHealthChecked = 2131230961;
                            HealthCheckDto healthCheckDto3 = this.healthCheckDto;
                            if (this.logic != null) {
                                this.logic = null;
                            }
                            return healthCheckDto3;
                        }
                        RotateAsyncTask rotateAsyncTask4 = new RotateAsyncTask(activity, 2131361805);
                        rotateAsyncTask4.executeOnExecutor(THREAD_POOL_EXECUTOR, new Void[0]);
                        HealthChkMngDto healthChkMngDto = new HealthChkMngDto();
                        healthChkMngDto.url = "http://ctcds.benesse.ne.jp/network-check/connection.html";
                        healthChkMngDto.timeout = 30;
                        this.logic.checkNetConnection(healthChkMngDto, this.healthCheckDto);
                        rotateAsyncTask4.cancel(true);
                        publishProgress(this.healthCheckDto);
                        if (isCancelled() || this.healthCheckDto.isCheckedNetConnection == 2131230961) {
                            Logger.d("CheckNetworkTask", "doInBackground 0007");
                            this.healthCheckDto.isHealthChecked = 2131230961;
                            HealthCheckDto healthCheckDto4 = this.healthCheckDto;
                            if (this.logic != null) {
                                this.logic = null;
                            }
                            return healthCheckDto4;
                        }
                        RotateAsyncTask rotateAsyncTask5 = new RotateAsyncTask(activity, 2131361810);
                        rotateAsyncTask5.executeOnExecutor(THREAD_POOL_EXECUTOR, new Void[0]);
                        HealthChkMngDto healthChkMngDto2 = new HealthChkMngDto();
                        healthChkMngDto2.url = "http://ctcds.benesse.ne.jp/network-check/speedtest.list";
                        healthChkMngDto2.timeout = 30;
                        this.logic.checkDownloadSpeed(activity, healthChkMngDto2, this.healthCheckDto);
                        rotateAsyncTask5.cancel(true);
                        this.healthCheckDto.isHealthChecked = 2131230960;
                    } catch (Throwable th) {
                        th = th;
                        if (this.logic != null) {
                            this.logic = null;
                        }
                        throw th;
                    }
                }
                if (this.logic != null) {
                    this.logic = null;
                }
                Logger.d("CheckNetworkTask", "doInBackground 0008");
                return this.healthCheckDto;
            } catch (Throwable th2) {
                th = th2;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(HealthCheckDto healthCheckDto) {
            Logger.d("CheckNetworkTask", "onPostExecute 0001");
            HealthCheckActivity healthCheckActivity = (HealthCheckActivity) this.owner.get();
            if (healthCheckActivity != null) {
                Logger.d("CheckNetworkTask", "onPostExecute 0002");
                healthCheckActivity.updateHealthCheckInfo(healthCheckDto);
            }
            Logger.d("CheckNetworkTask", "onPostExecute 0003");
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            Logger.d("CheckNetworkTask", "onPreExecute 0001");
            this.healthCheckDto = new HealthCheckDto();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(HealthCheckDto... healthCheckDtoArr) {
            Logger.d("CheckNetworkTask", "onProgressUpdate 0001");
            HealthCheckDto healthCheckDto = healthCheckDtoArr[0];
            HealthCheckActivity healthCheckActivity = (HealthCheckActivity) this.owner.get();
            if (healthCheckActivity != null) {
                Logger.d("CheckNetworkTask", "onProgressUpdate 0002");
                healthCheckActivity.updateHealthCheckInfo(healthCheckDto);
            }
            Logger.d("CheckNetworkTask", "onProgressUpdate 0003");
        }

        public void stop() {
            Logger.d("CheckNetworkTask", "stop 0001");
            if (this.healthCheckDto != null) {
                Logger.d("CheckNetworkTask", "stop 0002");
                this.healthCheckDto.cancel();
            }
            cancel(true);
            Logger.d("CheckNetworkTask", "stop 0003");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/HealthCheckActivity$RotateAsyncTask.class */
    public static class RotateAsyncTask extends AsyncTask<Void, Void, Void> {
        private final int id;
        private WeakReference<Activity> owner;
        private final String TAG = "RotateAsyncTask";
        private int rotation = 0;

        public RotateAsyncTask(Activity activity, int i) {
            Logger.d("RotateAsyncTask", "RotateAsyncTask 0001");
            this.owner = new WeakReference<>(activity);
            this.id = i;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(Void... voidArr) {
            Logger.d("RotateAsyncTask", "doInBackground 0001");
            while (!isCancelled()) {
                try {
                    Thread.sleep(100L);
                    publishProgress(new Void[0]);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override // android.os.AsyncTask
        protected void onCancelled() {
            Logger.d("RotateAsyncTask", "onCancelled 0001");
            Activity activity = this.owner.get();
            if (activity != null) {
                Logger.d("RotateAsyncTask", "onCancelled 0002");
                activity.findViewById(this.id).setVisibility(4);
            }
            Logger.d("RotateAsyncTask", "onCancelled 0003");
            super.onCancelled();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Void... voidArr) {
            Logger.d("RotateAsyncTask", "onProgressUpdate 0001");
            Activity activity = this.owner.get();
            if (activity != null && !isCancelled()) {
                Logger.d("RotateAsyncTask", "onProgressUpdate 0002");
                ImageView imageView = (ImageView) activity.findViewById(this.id);
                if (imageView.getVisibility() != 0) {
                    Logger.d("RotateAsyncTask", "onProgressUpdate 0003");
                    imageView.setVisibility(0);
                }
                this.rotation = (this.rotation + 30) % 360;
                imageView.setRotation(this.rotation);
            }
            Logger.d("RotateAsyncTask", "onProgressUpdate 0004");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeBtnClickable(boolean z) {
        Logger.d("HealthCheckActivity", "changeBtnClickable 0001");
        View findViewById = findViewById(2131361815);
        if (findViewById != null) {
            Logger.d("HealthCheckActivity", "changeBtnClickable 0002");
            findViewById.setClickable(z);
        }
        View findViewById2 = findViewById(2131361813);
        if (findViewById2 != null) {
            Logger.d("HealthCheckActivity", "changeBtnClickable 0003");
            findViewById2.setClickable(z);
        }
        View findViewById3 = findViewById(2131361814);
        if (findViewById3 != null) {
            Logger.d("HealthCheckActivity", "changeBtnClickable 0004");
            findViewById3.setClickable(z);
        }
        Logger.d("HealthCheckActivity", "changeBtnClickable 0005");
    }

    private void moveDownloadActivity() {
        Logger.d("HealthCheckActivity", "moveDownloadActivity 0001");
        Intent intent = new Intent();
        intent.setClassName("jp.co.benesse.dcha.setupwizard", "jp.co.benesse.dcha.setupwizard.DownloadSettingActivity");
        intent.putExtra("first_flg", this.mIsFirstFlow);
        startActivity(intent);
        finish();
        Logger.d("HealthCheckActivity", "moveDownloadActivity 0002");
    }

    private void moveWifiSettingActivity() {
        Logger.d("HealthCheckActivity", "moveWifiSettingActivity 0001");
        Intent intent = new Intent();
        intent.setClassName("jp.co.benesse.dcha.systemsettings", "jp.co.benesse.dcha.systemsettings.WifiSettingActivity");
        intent.putExtra("first_flg", this.mIsFirstFlow);
        startActivity(intent);
        finish();
        Logger.d("HealthCheckActivity", "moveWifiSettingActivity 0002");
    }

    private void showDetailDialog() {
        Logger.d("HealthCheckActivity", "showDetailDialog 0001");
        HCheckDetailDialog hCheckDetailDialog = new HCheckDetailDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable("healthCheckDto", this.healthCheckDto);
        hCheckDetailDialog.setArguments(bundle);
        hCheckDetailDialog.setOnDismissListener(new DialogInterface.OnDismissListener(this) { // from class: jp.co.benesse.dcha.systemsettings.HealthCheckActivity.1
            final HealthCheckActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                this.this$0.changeBtnClickable(true);
            }
        });
        hCheckDetailDialog.show(getFragmentManager(), "dialog");
        Logger.d("HealthCheckActivity", "showDetailDialog 0002");
    }

    protected void drawingHealthCheckProgress(HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckActivity", "onProgressUpdate 0001");
        drawingProgressView(healthCheckDto.isCheckedSsid, 2131361797, 2131361795, healthCheckDto.mySsid);
        drawingProgressView(healthCheckDto.isCheckedWifi, 2131361800, 2131361798, getString(healthCheckDto.isCheckedWifi));
        drawingProgressView(healthCheckDto.isCheckedIpAddress, 2131361803, 2131361801, getString(healthCheckDto.isCheckedIpAddress));
        drawingProgressView(healthCheckDto.isCheckedNetConnection, 2131361806, 2131361804, getString(healthCheckDto.isCheckedNetConnection));
        Logger.d("HealthCheckActivity", "onProgressUpdate 0002");
    }

    protected void drawingHealthCheckResult(HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckActivity", "onPostExecute 0001");
        drawingPendingView(healthCheckDto.isCheckedWifi, 2131361798);
        drawingPendingView(healthCheckDto.isCheckedIpAddress, 2131361801);
        drawingPendingView(healthCheckDto.isCheckedNetConnection, 2131361804);
        if (healthCheckDto.isCheckedDSpeed == 2131230962) {
            Logger.d("HealthCheckActivity", "onPostExecute 0002");
            findViewById(2131361807).setVisibility(0);
        } else {
            Logger.d("HealthCheckActivity", "onPostExecute 0003");
            findViewById(2131361811).setVisibility(0);
            ImageView imageView = (ImageView) findViewById(2131361808);
            imageView.setImageResource(healthCheckDto.myDSpeedImage);
            imageView.setVisibility(0);
            TextView textView = (TextView) findViewById(2131361809);
            textView.setText(healthCheckDto.myDownloadSpeed);
            textView.setVisibility(0);
        }
        findViewById(2131361815).setEnabled(true);
        if (healthCheckDto.isHealthChecked == 2131230961) {
            Logger.d("HealthCheckActivity", "onPostExecute 0004");
            findViewById(2131361812).setVisibility(0);
            findViewById(2131361814).setVisibility(0);
        } else {
            Logger.d("HealthCheckActivity", "onPostExecute 0005");
            findViewById(2131361813).setVisibility(0);
        }
        Logger.d("HealthCheckActivity", "onPostExecute 0006");
    }

    protected void drawingPendingView(int i, int i2) {
        Logger.d("HealthCheckActivity", "drawingPendingView 0001");
        if (i == 2131230962) {
            Logger.d("HealthCheckActivity", "drawingPendingView 0002");
            TextView textView = (TextView) findViewById(i2);
            textView.setText(getString(2131230962));
            textView.setTextColor(getResources().getColor(2131099651));
            textView.setVisibility(0);
        }
        Logger.d("HealthCheckActivity", "drawingPendingView 0003");
    }

    protected void drawingProgressView(int i, int i2, int i3, String str) {
        Logger.d("HealthCheckActivity", "drawingProgressView 0001");
        if (i != 2131230962) {
            Logger.d("HealthCheckActivity", "drawingProgressView 0002");
            findViewById(i2).setVisibility(0);
            TextView textView = (TextView) findViewById(i3);
            textView.setText(str);
            if (i == 2131230960) {
                Logger.d("HealthCheckActivity", "drawingProgressView 0003");
                textView.setTextColor(getResources().getColor(2131099650));
            } else {
                Logger.d("HealthCheckActivity", "drawingProgressView 0004");
                textView.setTextColor(getResources().getColor(2131099652));
            }
            textView.setVisibility(0);
        }
        Logger.d("HealthCheckActivity", "drawingProgressView 0005");
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d("HealthCheckActivity", "onClick 0001");
        switch (view.getId()) {
            case 2131361813:
                Logger.d("HealthCheckActivity", "onClick 0003");
                changeBtnClickable(false);
                moveDownloadActivity();
                break;
            case 2131361814:
                Logger.d("HealthCheckActivity", "onClick 0004");
                changeBtnClickable(false);
                moveWifiSettingActivity();
                finish();
                break;
            case 2131361815:
                Logger.d("HealthCheckActivity", "onClick 0002");
                changeBtnClickable(false);
                showDetailDialog();
                break;
        }
        Logger.d("HealthCheckActivity", "onClick 0005");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        Logger.d("HealthCheckActivity", "onCreate 0001");
        super.onCreate(bundle);
        setContentView(2130903041);
        this.checkNetworkTask = null;
        this.healthCheckDto = null;
        this.mIsFirstFlow = getFirstFlg();
        findViewById(2131361815).setOnClickListener(this);
        findViewById(2131361815).setEnabled(false);
        findViewById(2131361813).setOnClickListener(this);
        findViewById(2131361814).setOnClickListener(this);
        if (bundle != null) {
            Logger.d("HealthCheckActivity", "onCreate 0002");
            this.healthCheckDto = (HealthCheckDto) bundle.getSerializable(HealthCheckDto.class.getSimpleName());
            bundle.clear();
        }
        getWindow().addFlags(128);
        Logger.d("HealthCheckActivity", "onCreate 0003");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onDestroy() {
        Logger.d("HealthCheckActivity", "onDestroy 0001");
        super.onDestroy();
        View findViewById = findViewById(2131361815);
        if (findViewById != null) {
            Logger.d("HealthCheckActivity", "onDestroy 0002");
            findViewById.setOnClickListener(null);
        }
        View findViewById2 = findViewById(2131361813);
        if (findViewById2 != null) {
            Logger.d("HealthCheckActivity", "onDestroy 0003");
            findViewById2.setOnClickListener(null);
        }
        View findViewById3 = findViewById(2131361814);
        if (findViewById3 != null) {
            Logger.d("HealthCheckActivity", "onDestroy 0004");
            findViewById3.setOnClickListener(null);
        }
        this.checkNetworkTask = null;
        this.healthCheckDto = null;
        Logger.d("HealthCheckActivity", "onDestroy 0005");
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        Logger.d("HealthCheckActivity", "onSaveInstanceState 0001");
        super.onSaveInstanceState(bundle);
        if (this.healthCheckDto != null && this.healthCheckDto.isHealthChecked != 2131230962) {
            Logger.d("HealthCheckActivity", "onSaveInstanceState 0002");
            bundle.putSerializable(HealthCheckDto.class.getSimpleName(), this.healthCheckDto);
        }
        Logger.d("HealthCheckActivity", "onSaveInstanceState 0003");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onStart() {
        Logger.d("HealthCheckActivity", "onStart 0001");
        super.onStart();
        findViewById(2131361795).setVisibility(4);
        findViewById(2131361796).setVisibility(4);
        findViewById(2131361797).setVisibility(4);
        findViewById(2131361798).setVisibility(4);
        findViewById(2131361799).setVisibility(4);
        findViewById(2131361800).setVisibility(4);
        findViewById(2131361801).setVisibility(4);
        findViewById(2131361802).setVisibility(4);
        findViewById(2131361803).setVisibility(4);
        findViewById(2131361804).setVisibility(4);
        findViewById(2131361805).setVisibility(4);
        findViewById(2131361806).setVisibility(4);
        findViewById(2131361807).setVisibility(4);
        findViewById(2131361808).setVisibility(4);
        findViewById(2131361809).setVisibility(4);
        findViewById(2131361810).setVisibility(4);
        findViewById(2131361811).setVisibility(4);
        findViewById(2131361812).setVisibility(4);
        findViewById(2131361813).setVisibility(8);
        findViewById(2131361814).setVisibility(8);
        changeBtnClickable(true);
        if (this.healthCheckDto == null || this.healthCheckDto.isHealthChecked == 2131230962) {
            Logger.d("HealthCheckActivity", "onStart 0003");
            this.checkNetworkTask = new CheckNetworkTask(this);
            this.checkNetworkTask.execute(new Void[0]);
        } else {
            Logger.d("HealthCheckActivity", "onStart 0004");
            updateHealthCheckInfo(this.healthCheckDto);
        }
        Logger.d("HealthCheckActivity", "onStart 0005");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onStop() {
        Logger.d("HealthCheckActivity", "onStop 0001");
        super.onStop();
        if (this.checkNetworkTask != null && this.checkNetworkTask.getStatus() != AsyncTask.Status.FINISHED) {
            Logger.d("HealthCheckActivity", "onStop 0002");
            this.checkNetworkTask.stop();
        }
        Logger.d("HealthCheckActivity", "onStop 0003");
    }

    public void updateHealthCheckInfo(HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckActivity", "updateHealthCheckInfo 0001");
        if (healthCheckDto != null) {
            Logger.d("HealthCheckActivity", "updateHealthCheckInfo 0002");
            drawingHealthCheckProgress(healthCheckDto);
            if (healthCheckDto.isHealthChecked != 2131230962) {
                Logger.d("HealthCheckActivity", "updateHealthCheckInfo 0003");
                drawingHealthCheckResult(healthCheckDto);
                this.healthCheckDto = healthCheckDto;
            }
        }
        Logger.d("HealthCheckActivity", "updateHealthCheckInfo 0004");
    }
}
