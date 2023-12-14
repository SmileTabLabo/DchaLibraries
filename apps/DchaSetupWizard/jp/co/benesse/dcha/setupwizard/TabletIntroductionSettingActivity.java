package jp.co.benesse.dcha.setupwizard;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public class TabletIntroductionSettingActivity extends ParentSettingActivity implements View.OnClickListener {
    private static final String ACTION_DATABOX_COMMAND = "jp.co.benesse.dcha.databox.intent.action.COMMAND";
    private static final String ACTION_DATABOX_IMPORTED_ENVIRONMENT_EVENT = "jp.co.benesse.dcha.databox.intent.action.IMPORTED_ENVIRONMENT_EVENT";
    private static final String CATEGORIES_DATABOX_IMPORT_ENVIRONMENT = "jp.co.benesse.dcha.databox.intent.category.IMPORT_ENVIRONMENT";
    private static final String COLUMN_KVS_SELECTION = "key=?";
    private static final String COLUMN_KVS_VALUE = "value";
    private static final String EXTRA_KEY_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
    private static final String IMPORT_ENVIRONMENT_FILENAME = "test_environment_info.xml";
    private static final String KEY_ENVIRONMENT = "environment";
    private static final String KEY_VERSION = "version";
    private static final String TEMP_DIR = "temp";
    protected ImageView mStartBtn;
    protected TextView mTestEnvironmentText;
    private static final String TAG = TabletIntroductionSettingActivity.class.getSimpleName();
    private static final Uri URI_TEST_ENVIRONMENT_INFO = Uri.parse("content://jp.co.benesse.dcha.databox.db.KvsProvider/kvs/test.environment.info");
    protected Handler mHandler = new Handler();
    public BroadcastReceiver mImportedEnvironmentReceiver = new BroadcastReceiver() { // from class: jp.co.benesse.dcha.setupwizard.TabletIntroductionSettingActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.d(TabletIntroductionSettingActivity.TAG, "onReceive 0001");
            if (TabletIntroductionSettingActivity.this.mTestEnvironmentText != null) {
                Logger.d(TabletIntroductionSettingActivity.TAG, "onReceive 0002");
                TabletIntroductionSettingActivity.this.mTestEnvironmentText.setText(BuildConfig.FLAVOR);
                String kvsValue = TabletIntroductionSettingActivity.this.getKvsValue(context, TabletIntroductionSettingActivity.URI_TEST_ENVIRONMENT_INFO, TabletIntroductionSettingActivity.KEY_ENVIRONMENT, null);
                String kvsValue2 = TabletIntroductionSettingActivity.this.getKvsValue(context, TabletIntroductionSettingActivity.URI_TEST_ENVIRONMENT_INFO, TabletIntroductionSettingActivity.KEY_VERSION, null);
                if (!TextUtils.isEmpty(kvsValue) && !TextUtils.isEmpty(kvsValue2)) {
                    Logger.d(TabletIntroductionSettingActivity.TAG, "onReceive 0003");
                    TabletIntroductionSettingActivity.this.mTestEnvironmentText.setText(TabletIntroductionSettingActivity.this.getString(R.string.test_environment_format, new Object[]{kvsValue, kvsValue2}));
                }
            }
            Logger.d(TabletIntroductionSettingActivity.TAG, "onReceive 0004");
        }
    };
    protected IDchaService mDchaService = null;
    protected ServiceConnection mDchaServiceConnection = new ServiceConnection() { // from class: jp.co.benesse.dcha.setupwizard.TabletIntroductionSettingActivity.2
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.d(TabletIntroductionSettingActivity.TAG, "onServiceConnected 0001");
            TabletIntroductionSettingActivity.this.mDchaService = IDchaService.Stub.asInterface(iBinder);
            String str = System.getenv("SECONDARY_STORAGE");
            try {
                File file = new File(str, TabletIntroductionSettingActivity.IMPORT_ENVIRONMENT_FILENAME);
                File file2 = new File(TabletIntroductionSettingActivity.this.getFilesDir(), TabletIntroductionSettingActivity.TEMP_DIR);
                if (TabletIntroductionSettingActivity.this.mDchaService.copyFile(file.getAbsolutePath(), file2.getAbsolutePath())) {
                    Logger.d(TabletIntroductionSettingActivity.TAG, "onServiceConnected 0002");
                    str = file2.getAbsolutePath();
                }
            } catch (RemoteException e) {
                Logger.e(TabletIntroductionSettingActivity.TAG, "onServiceConnected 0003", e);
            }
            Intent intent = new Intent();
            intent.setAction(TabletIntroductionSettingActivity.ACTION_DATABOX_COMMAND);
            intent.addCategory(TabletIntroductionSettingActivity.CATEGORIES_DATABOX_IMPORT_ENVIRONMENT);
            intent.putExtra(TabletIntroductionSettingActivity.EXTRA_KEY_EXTERNAL_STORAGE, str);
            TabletIntroductionSettingActivity.this.sendBroadcast(intent);
            Logger.d(TabletIntroductionSettingActivity.TAG, "onServiceConnected 0004");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.d(TabletIntroductionSettingActivity.TAG, "onServiceDisconnected 0001");
            TabletIntroductionSettingActivity.this.mDchaService = null;
            Logger.d(TabletIntroductionSettingActivity.TAG, "onServiceDisconnected 0002");
        }
    };

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        Logger.d(TAG, "onCreate 0001");
        super.onCreate(bundle);
        setContentView(R.layout.act_tablet_introduction);
        this.mStartBtn = (ImageView) findViewById(R.id.start_btn);
        this.mTestEnvironmentText = (TextView) findViewById(R.id.test_environment_text);
        setFont(this.mTestEnvironmentText);
        this.mHandler.postDelayed(new Runnable() { // from class: jp.co.benesse.dcha.setupwizard.TabletIntroductionSettingActivity.3
            @Override // java.lang.Runnable
            public void run() {
                if (TabletIntroductionSettingActivity.this.mStartBtn != null) {
                    TabletIntroductionSettingActivity.this.mStartBtn.setOnClickListener(TabletIntroductionSettingActivity.this);
                }
            }
        }, 750L);
        Logger.d(TAG, "onCreate 0002");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onStart() {
        Logger.d(TAG, "onStart 0001");
        super.onStart();
        File file = new File(getFilesDir(), TEMP_DIR);
        if (!file.exists()) {
            file.mkdir();
            file.setReadable(true, false);
            file.setWritable(true, false);
            file.setExecutable(true, false);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DATABOX_IMPORTED_ENVIRONMENT_EVENT);
        registerReceiver(this.mImportedEnvironmentReceiver, intentFilter);
        try {
            getContentResolver().delete(URI_TEST_ENVIRONMENT_INFO, null, null);
        } catch (Exception e) {
            Logger.d(TAG, "onStart 0002", e);
        }
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        bindService(intent, this.mDchaServiceConnection, 1);
        Logger.d(TAG, "onStart 0003");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onStop() {
        Logger.d(TAG, "onStop 0001");
        super.onStop();
        if (this.mDchaServiceConnection != null && this.mDchaService != null) {
            Logger.d(TAG, "onStop 0002");
            unbindService(this.mDchaServiceConnection);
            this.mDchaService = null;
        }
        if (this.mImportedEnvironmentReceiver != null) {
            Logger.d(TAG, "onStop 0003");
            unregisterReceiver(this.mImportedEnvironmentReceiver);
        }
        File file = new File(getFilesDir(), TEMP_DIR);
        File file2 = new File(file, IMPORT_ENVIRONMENT_FILENAME);
        if (file2.exists()) {
            Logger.d(TAG, "onStop 0004");
            if (file2.delete()) {
                Logger.d(TAG, "onStop 0005");
            }
        }
        if (file.exists()) {
            Logger.d(TAG, "onStop 0006");
            if (file.delete()) {
                Logger.d(TAG, "onStop 0007");
            }
        }
        Logger.d(TAG, "onStop 0008");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // jp.co.benesse.dcha.setupwizard.ParentSettingActivity, android.app.Activity
    public void onDestroy() {
        Logger.d(TAG, "onDestroy 0001");
        super.onDestroy();
        this.mStartBtn.setOnClickListener(null);
        this.mStartBtn = null;
        this.mTestEnvironmentText = null;
        this.mHandler = null;
        Logger.d(TAG, "onDestroy 0002");
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d(TAG, "onClick 0001");
        this.mStartBtn.setClickable(false);
        Intent intent = new Intent();
        intent.setClassName(ParentSettingActivity.PACKAGE_SYSTEM_SETTING, ParentSettingActivity.CLASS_SYSTEM_WIFI_SETTING);
        intent.putExtra(ParentSettingActivity.FIRST_FLG, true);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        Logger.d(TAG, "onClick 0002");
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x002c, code lost:
        if (r10 != null) goto L12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:11:0x002e, code lost:
        r10.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x003a, code lost:
        if (r10 == null) goto L20;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected String getKvsValue(Context context, Uri uri, String str, String str2) {
        if (context != null) {
            String[] strArr = {str};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{COLUMN_KVS_VALUE}, COLUMN_KVS_SELECTION, strArr, null);
                if (cursor != null && cursor.moveToFirst()) {
                    str2 = cursor.getString(cursor.getColumnIndex(COLUMN_KVS_VALUE));
                }
            } catch (Exception unused) {
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        return str2;
    }
}
