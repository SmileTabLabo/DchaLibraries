package jp.co.benesse.dcha.setupwizard;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ProgressBar;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.setupwizard.http.FileDownloadRequest;
import jp.co.benesse.dcha.setupwizard.http.FileDownloadResponse;
import jp.co.benesse.dcha.setupwizard.http.HttpThread;
import jp.co.benesse.dcha.setupwizard.http.Request;
import jp.co.benesse.dcha.setupwizard.http.Response;
import jp.co.benesse.dcha.util.Logger;
import jp.co.benesse.dcha.util.UrlUtil;
/* loaded from: classes.dex */
public class DownloadSettingActivity extends ParentSettingActivity {
    public static final String DOWNLOAD_APK_PATH = "open/TouchSetupLogin.apk";
    public static final int PROGRESS_MAX_VALUE = 100;
    private static final String TAG = DownloadSettingActivity.class.getSimpleName();
    protected IDchaService mDchaService;
    protected DownloadTask mDownloadTask;
    protected ProgressBar mProgressBar;
    protected ServiceConnection mDchaServiceConnection = new ServiceConnection() { // from class: jp.co.benesse.dcha.setupwizard.DownloadSettingActivity.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(DownloadSettingActivity.TAG, "onServiceConnected 0001");
            DownloadSettingActivity.this.mDchaService = IDchaService.Stub.asInterface(service);
            DownloadSettingActivity.this.startDownloadTask();
            Logger.d(DownloadSettingActivity.TAG, "onServiceConnected 0004");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(DownloadSettingActivity.TAG, "onServiceDisconnected 0001");
            DownloadSettingActivity.this.mDchaService = null;
            Logger.d(DownloadSettingActivity.TAG, "onServiceDisconnected 0002");
        }
    };
    protected BroadcastReceiver mWifiReceiver = new BroadcastReceiver() { // from class: jp.co.benesse.dcha.setupwizard.DownloadSettingActivity.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.d(DownloadSettingActivity.TAG, "onReceive 0001");
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (info == null || !info.isConnected()) {
                Logger.d(DownloadSettingActivity.TAG, "onReceive 0002");
                DownloadSettingActivity.this.callWifiErrorDialog();
                if (DownloadSettingActivity.this.mDownloadTask != null) {
                    Logger.d(DownloadSettingActivity.TAG, "onReceive 0003");
                    DownloadSettingActivity.this.mDownloadTask.cancel();
                }
            }
            Logger.d(DownloadSettingActivity.TAG, "onReceive 0004");
        }
    };
    protected BroadcastReceiver mInstallReceiver = new BroadcastReceiver() { // from class: jp.co.benesse.dcha.setupwizard.DownloadSettingActivity.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.d(DownloadSettingActivity.TAG, "onReceive 0101");
            if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction()) && !intent.getExtras().getBoolean("android.intent.extra.REPLACING")) {
                Logger.d(DownloadSettingActivity.TAG, "onReceive 0102");
                if (DownloadSettingActivity.this.mInstallReceiver != null) {
                    DownloadSettingActivity.this.unregisterReceiver(DownloadSettingActivity.this.mInstallReceiver);
                    DownloadSettingActivity.this.mInstallReceiver = null;
                }
                Uri packageUri = intent.getData();
                String packageName = packageUri.getSchemeSpecificPart();
                DownloadSettingActivity.this.moveInstalledApk(packageName);
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate 0001");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_download);
        this.mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        this.mProgressBar.setMax(100);
        this.mProgressBar.setProgress(0);
        File dir = new File(getFilesDir(), getString(R.string.path_download_file));
        dir.mkdirs();
        dir.setExecutable(true, false);
        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction("android.intent.action.PACKAGE_ADDED");
        installFilter.addDataScheme("package");
        registerReceiver(this.mInstallReceiver, installFilter);
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        bindService(intent, this.mDchaServiceConnection, 1);
        Logger.d(TAG, "onCreate 0002");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onStart() {
        Logger.d(TAG, "onStart 0001");
        super.onStart();
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction("android.net.wifi.STATE_CHANGE");
        registerReceiver(this.mWifiReceiver, wifiFilter);
        Logger.d(TAG, "onStart 0002");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop 0001");
        if (this.mWifiReceiver != null) {
            Logger.d(TAG, "onStop 0002");
            unregisterReceiver(this.mWifiReceiver);
        }
        Logger.d(TAG, "onStop 0003");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy 0001");
        this.mProgressBar = null;
        this.mWifiReceiver = null;
        if (this.mDownloadTask != null) {
            Logger.d(TAG, "onDestroy 0002");
            this.mDownloadTask.cancel();
            this.mDownloadTask = null;
        }
        if (this.mInstallReceiver != null) {
            Logger.d(TAG, "onDestroy 0003");
            unregisterReceiver(this.mInstallReceiver);
            this.mInstallReceiver = null;
        }
        if (this.mDchaServiceConnection != null) {
            Logger.d(TAG, "onDestroy 0004");
            unbindService(this.mDchaServiceConnection);
            this.mDchaServiceConnection = null;
            this.mDchaService = null;
        }
        Logger.d(TAG, "onDestroy 0005");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class DownloadTask extends AsyncTask<FileDownloadRequest, Integer, FileDownloadResponse> implements Request.ResponseListener {
        protected CountDownLatch mCountDownLatch = null;
        protected HttpThread mThread = new HttpThread();
        protected FileDownloadResponse mResponse = null;

        DownloadTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public FileDownloadResponse doInBackground(FileDownloadRequest... requests) {
            Logger.d(DownloadSettingActivity.TAG, "doInBackground 0001");
            this.mCountDownLatch = new CountDownLatch(1);
            this.mThread.setResponseListener(this);
            this.mThread.postRequest(requests[0]);
            try {
                try {
                    Logger.d(DownloadSettingActivity.TAG, "doInBackground 0002");
                    this.mThread.start();
                    this.mCountDownLatch.await();
                    Logger.d(DownloadSettingActivity.TAG, "doInBackground 0004");
                    this.mThread.stopRunning();
                } catch (InterruptedException e) {
                    Logger.d(DownloadSettingActivity.TAG, "doInBackground 0003");
                    Logger.d(DownloadSettingActivity.TAG, "doInBackground InterruptedException", e);
                    DownloadSettingActivity.this.callSystemErrorDialog(DownloadSettingActivity.this.getString(R.string.error_code_fail_download));
                    cancel(true);
                    Logger.d(DownloadSettingActivity.TAG, "doInBackground 0004");
                    this.mThread.stopRunning();
                }
                if (this.mResponse != null && this.mResponse.isSuccess()) {
                    Logger.d(DownloadSettingActivity.TAG, "doInBackground 0005");
                    DownloadSettingActivity.this.mProgressBar.setProgress(100);
                    try {
                        try {
                            Logger.d(DownloadSettingActivity.TAG, "doInBackground 0006");
                            this.mResponse.outFile.setReadable(true, false);
                            if (!DownloadSettingActivity.this.mDchaService.installApp(this.mResponse.outFile.getCanonicalPath(), 0)) {
                                Logger.d(DownloadSettingActivity.TAG, "doInBackground 0007");
                                DownloadSettingActivity.this.callSystemErrorDialog(DownloadSettingActivity.this.getString(R.string.error_code_fail_install));
                                cancel(true);
                            }
                            Logger.d(DownloadSettingActivity.TAG, "doInBackground 0009");
                            this.mResponse.outFile.delete();
                        } catch (Exception e2) {
                            Logger.d(DownloadSettingActivity.TAG, "doInBackground 0008");
                            Logger.d(DownloadSettingActivity.TAG, "doInBackground Exception", e2);
                            DownloadSettingActivity.this.callSystemErrorDialog(DownloadSettingActivity.this.getString(R.string.error_code_fail_install));
                            cancel(true);
                            Logger.d(DownloadSettingActivity.TAG, "doInBackground 0009");
                            this.mResponse.outFile.delete();
                        }
                    } catch (Throwable th) {
                        Logger.d(DownloadSettingActivity.TAG, "doInBackground 0009");
                        this.mResponse.outFile.delete();
                        throw th;
                    }
                }
                Logger.d(DownloadSettingActivity.TAG, "doInBackground 0010");
                return this.mResponse;
            } catch (Throwable th2) {
                Logger.d(DownloadSettingActivity.TAG, "doInBackground 0004");
                this.mThread.stopRunning();
                throw th2;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... progress) {
            Logger.d(DownloadSettingActivity.TAG, "onProgressUpdate 0001");
            DownloadSettingActivity.this.mProgressBar.setProgress(progress[0].intValue());
            Logger.d(DownloadSettingActivity.TAG, "onProgressUpdate 0002");
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(FileDownloadResponse result) {
            Logger.d(DownloadSettingActivity.TAG, "onPostExecute 0001");
            if (result == null) {
                Logger.d(DownloadSettingActivity.TAG, "onPostExecute 0002");
                DownloadSettingActivity.this.callNetworkErrorDialog();
            } else if (result.responseCode == 404 || result.responseCode == 403) {
                Logger.d(DownloadSettingActivity.TAG, "onPostExecute 0003");
                DownloadSettingActivity.this.callSystemErrorDialog(DownloadSettingActivity.this.getString(R.string.error_code_before_open));
            } else if (!result.isSuccess()) {
                Logger.d(DownloadSettingActivity.TAG, "onPostExecute 0004");
                DownloadSettingActivity.this.callSystemErrorDialog(DownloadSettingActivity.this.getString(R.string.error_code_fail_download));
            }
            Logger.d(DownloadSettingActivity.TAG, "onPostExecute 0005");
        }

        public void cancel() {
            Logger.d(DownloadSettingActivity.TAG, "cancel 0001");
            cancel(true);
            this.mThread.cancel();
            Logger.d(DownloadSettingActivity.TAG, "cancel 0002");
        }

        @Override // jp.co.benesse.dcha.setupwizard.http.Request.ResponseListener
        public void onHttpResponse(Response response) {
            Logger.d(DownloadSettingActivity.TAG, "onHttpResponse 0001");
            Logger.d(DownloadSettingActivity.TAG, "onHttpResponse URL:", response.request.url);
            if (response instanceof FileDownloadResponse) {
                Logger.d(DownloadSettingActivity.TAG, "onHttpResponse 0002");
                this.mResponse = (FileDownloadResponse) response;
            }
            this.mCountDownLatch.countDown();
            Logger.d(DownloadSettingActivity.TAG, "onHttpResponse 0004");
        }

        @Override // jp.co.benesse.dcha.setupwizard.http.Request.ResponseListener
        public void onHttpProgress(Response response) {
            Logger.d(DownloadSettingActivity.TAG, "onHttpProgress 0001");
            if (response.contentLength > 0) {
                Logger.d(DownloadSettingActivity.TAG, "onHttpProgress 0002");
                int percent = (int) ((response.receiveLength * 100) / response.contentLength);
                publishProgress(Integer.valueOf(percent));
            }
            Logger.d(DownloadSettingActivity.TAG, "onHttpProgress 0003");
        }

        @Override // jp.co.benesse.dcha.setupwizard.http.Request.ResponseListener
        public void onHttpError(Request request) {
            Logger.d(DownloadSettingActivity.TAG, "onHttpError 0001");
            Logger.d(DownloadSettingActivity.TAG, "onHttpError URL:", request.url);
            this.mCountDownLatch.countDown();
            Logger.d(DownloadSettingActivity.TAG, "onHttpError 0002");
        }

        @Override // jp.co.benesse.dcha.setupwizard.http.Request.ResponseListener
        public void onHttpCancelled(Request request) {
            Logger.d(DownloadSettingActivity.TAG, "onHttpCancelled 0001");
            this.mCountDownLatch.countDown();
            Logger.d(DownloadSettingActivity.TAG, "onHttpCancelled 0002");
        }
    }

    protected void moveInstalledApk(String packageName) {
        try {
            Logger.d(TAG, "moveInstalledApk 0001");
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setPackage(packageName);
            intent.addCategory("android.intent.category.DEFAULT");
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            Logger.d(TAG, "moveInstalledApk 0002");
            Logger.d(TAG, "moveInstalledApk ActivityNotFoundException", e);
            callSystemErrorDialog(getString(R.string.error_code_move_installed_apk));
        }
    }

    protected void startDownloadTask() {
        try {
            Logger.d(TAG, "onServiceConnected 0002");
            FileDownloadRequest request = new FileDownloadRequest();
            request.outPath = new File(getFilesDir(), getString(R.string.path_download_file));
            request.fileOverwrite = true;
            request.url = new URL(String.valueOf(new UrlUtil().getUrlAkamai(this)) + DOWNLOAD_APK_PATH);
            this.mDownloadTask = new DownloadTask();
            this.mDownloadTask.execute(request);
        } catch (MalformedURLException e) {
            Logger.d(TAG, "onServiceConnected 0003");
            Logger.d(TAG, "onServiceConnected MalformedURLException", e);
            callSystemErrorDialog(getString(R.string.error_code_get_file_path));
        }
    }
}
