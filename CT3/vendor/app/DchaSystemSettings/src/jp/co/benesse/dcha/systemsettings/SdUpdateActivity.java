package jp.co.benesse.dcha.systemsettings;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.DecimalFormat;
import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/SdUpdateActivity.class */
public class SdUpdateActivity extends ParentSettingActivity implements View.OnClickListener {
    private IDchaService dchaService;
    private TextView mSdMountText;
    private Button mUpdateBtn;
    public String mUpdatePath;
    private ProgressDialog progressDialog;
    private ServiceConnection serviceConnection = new ServiceConnection(this) { // from class: jp.co.benesse.dcha.systemsettings.SdUpdateActivity.1
        final SdUpdateActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.d("SDSettingActivity", "onServiceConnected 0001");
            this.this$0.dchaService = IDchaService.Stub.asInterface(iBinder);
            Logger.d("SDSettingActivity", "onServiceConnected 0002");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.d("SDSettingActivity", "onServiceDisconnected 0001");
            this.this$0.dchaService = null;
            Logger.d("SDSettingActivity", "onServiceDisconnected 0002");
        }
    };

    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/SdUpdateActivity$OsUploadTask.class */
    class OsUploadTask extends AsyncTask<String, Integer, String> {
        final SdUpdateActivity this$0;

        OsUploadTask(SdUpdateActivity sdUpdateActivity) {
            this.this$0 = sdUpdateActivity;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String doInBackground(String... strArr) {
            Logger.d("SDSettingActivity", "doInBackground 0001");
            try {
                Logger.d("SDSettingActivity", "doInBackground 0002");
                if (!this.this$0.dchaService.copyUpdateImage(this.this$0.mUpdatePath, "/cache/update.zip")) {
                    Logger.d("SDSettingActivity", "doInBackground 0004");
                    return null;
                }
                Logger.d("SDSettingActivity", "doInBackground 0003");
                this.this$0.dchaService.rebootPad(2, "/cache/update.zip");
                Logger.d("SDSettingActivity", "doInBackground 0006");
                return "success";
            } catch (Exception e) {
                Logger.d("SDSettingActivity", "doInBackground 0005");
                Logger.e("SDSettingActivity", "onClick", e);
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(String str) {
            Logger.d("SDSettingActivity", "onCancelled 0001");
            super.onCancelled((OsUploadTask) str);
            this.this$0.progressDialog.dismiss();
            if (this.this$0.progressDialog != null) {
                this.this$0.progressDialog = null;
            }
            Toast.makeText(this.this$0, this.this$0.getString(2131230741), 0).show();
            Logger.d("SDSettingActivity", "onCancelled 0002");
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String str) {
            Logger.d("SDSettingActivity", "onPostExecute 0001");
            if (str != null) {
                Logger.d("SDSettingActivity", "onPostExecute 0002");
            } else {
                Logger.d("SDSettingActivity", "onPostExecute 0003");
                Toast.makeText(this.this$0, this.this$0.getString(2131230742), 0).show();
            }
            this.this$0.mUpdateBtn.setClickable(true);
            if (this.this$0.progressDialog != null) {
                this.this$0.progressDialog.dismiss();
                this.this$0.progressDialog = null;
            }
            Logger.d("SDSettingActivity", "onPostExecute 0004");
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            Logger.d("SDSettingActivity", "onPreExecute 0001");
            this.this$0.progressDialog = new ProgressDialog(this.this$0);
            this.this$0.progressDialog.setTitle(this.this$0.getString(2131230739));
            this.this$0.progressDialog.setMessage(this.this$0.getString(2131230740));
            this.this$0.progressDialog.setIndeterminate(false);
            this.this$0.progressDialog.setProgressStyle(0);
            this.this$0.progressDialog.setCancelable(false);
            this.this$0.progressDialog.show();
            Logger.d("SDSettingActivity", "onPreExecute 0002");
        }
    }

    private boolean isSdMounted() {
        boolean z = false;
        Logger.d("SDSettingActivity", "isSdMounted 0001");
        File file = new File(this.mUpdatePath);
        StatFs statFs = new StatFs("/mnt/extsd");
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        Logger.d("SDSettingActivity", "isSdMounted 0002");
        if (!"0".equals(decimalFormat.format(statFs.getBlockSize() * statFs.getBlockCount()))) {
            z = file.exists();
        }
        return z;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d("SDSettingActivity", "onClick 0001");
        if (view.getId() == this.mUpdateBtn.getId()) {
            Logger.d("SDSettingActivity", "onClick 0002");
            if (isSdMounted()) {
                Logger.d("SDSettingActivity", "onClick 0003");
                this.mUpdateBtn.setClickable(false);
                new OsUploadTask(this).execute(new String[0]);
            } else {
                Logger.d("SDSettingActivity", "onClick 0004");
                Toast.makeText(this, "SDカードがマウントされていません。", 0).show();
            }
        }
        Logger.d("SDSettingActivity", "onClick 0005");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        Logger.d("SDSettingActivity", "onCreate 0001");
        super.onCreate(bundle);
        setContentView(2130903044);
        this.mUpdatePath = getResources().getString(2131230743);
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        bindService(intent, this.serviceConnection, 1);
        this.mSdMountText = (TextView) findViewById(2131361828);
        setFont(this.mSdMountText);
        this.mUpdateBtn = (Button) findViewById(2131361829);
        this.mUpdateBtn.setVisibility(0);
        this.mSdMountText.setText(2131230737);
        this.mUpdateBtn.setOnClickListener(this);
        Logger.d("SDSettingActivity", "onCreate 0002");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.systemsettings.ParentSettingActivity, android.app.Activity
    public void onDestroy() {
        Logger.d("SDSettingActivity", "onDestroy 0001");
        super.onDestroy();
        this.mUpdateBtn.setOnClickListener(null);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }
        this.mSdMountText = null;
        this.mUpdateBtn = null;
        if (this.serviceConnection != null) {
            Logger.d("SDSettingActivity", "onDestroy 0002");
            unbindService(this.serviceConnection);
            this.serviceConnection = null;
        }
        Logger.d("SDSettingActivity", "onDestroy 0003");
    }

    @Override // android.app.Activity
    protected void onPause() {
        Logger.d("SDSettingActivity", "onPause 0001");
        super.onPause();
        Logger.d("SDSettingActivity", "onPause 0002");
    }
}
