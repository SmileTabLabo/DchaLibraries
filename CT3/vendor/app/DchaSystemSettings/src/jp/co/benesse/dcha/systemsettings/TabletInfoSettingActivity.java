package jp.co.benesse.dcha.systemsettings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.text.TextUtils;
import android.view.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/TabletInfoSettingActivity.class */
public class TabletInfoSettingActivity extends PreferenceActivity {
    private static final Uri URI_TEST_ENVIRONMENT_INFO = Uri.parse("content://jp.co.benesse.dcha.databox.db.KvsProvider/kvs/test.environment.info");
    private IDchaService mDchaService;
    private ServiceConnection mServiceConnection = new ServiceConnection(this) { // from class: jp.co.benesse.dcha.systemsettings.TabletInfoSettingActivity.1
        final TabletInfoSettingActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.d("TabletInfoSettingActivity", "onServiceConnected 0001");
            this.this$0.mDchaService = IDchaService.Stub.asInterface(iBinder);
            this.this$0.hideNavigationBar(false);
            Logger.d("TabletInfoSettingActivity", "onServiceConnected 0002");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.d("TabletInfoSettingActivity", "onServiceDisconnected 0001");
            this.this$0.mDchaService = null;
            Logger.d("TabletInfoSettingActivity", "onServiceDisconnected 0002");
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: jp.co.benesse.dcha.systemsettings.TabletInfoSettingActivity.2
        final TabletInfoSettingActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.d("TabletInfoSettingActivity", "onReceive 0001");
            try {
                Logger.d("TabletInfoSettingActivity", "onReceive 0002");
                this.this$0.mDchaService.removeTask("jp.co.benesse.dcha.allgrade.usersetting");
                Logger.d("TabletInfoSettingActivity", "onReceive 0003");
            } catch (RemoteException e) {
                Logger.e("TabletInfoSettingActivity", "RemoteException", e);
                Logger.d("TabletInfoSettingActivity", "onReceive 0004");
            }
            Logger.d("TabletInfoSettingActivity", "onReceive 0005");
        }
    };

    private String formatKernelVersion(String str) {
        Logger.d("TabletInfoSettingActivity", "formatKernelVersion 0001");
        Matcher matcher = Pattern.compile("Linux version (\\S+) \\((\\S+?)\\) (?:\\(gcc.+? \\)) (#\\d+) (?:.*?)?((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)").matcher(str);
        if (!matcher.matches()) {
            Logger.d("TabletInfoSettingActivity", "formatKernelVersion 0002");
            Logger.e("TabletInfoSettingActivity", "Regex did not match on /proc/version: " + str);
            return "Unavailable";
        } else if (matcher.groupCount() >= 4) {
            Logger.d("TabletInfoSettingActivity", "formatKernelVersion 0004");
            return matcher.group(1) + "\n" + matcher.group(2) + " " + matcher.group(3) + "\n" + matcher.group(4);
        } else {
            Logger.d("TabletInfoSettingActivity", "formatKernelVersion 0003");
            Logger.e("TabletInfoSettingActivity", "Regex match on /proc/version only returned " + matcher.groupCount() + " groups");
            return "Unavailable";
        }
    }

    private String getFormattedKernelVersion() {
        Logger.d("TabletInfoSettingActivity", "getFormattedKernelVersion 0001");
        try {
            Logger.d("TabletInfoSettingActivity", "getFormattedKernelVersion 0002");
            return formatKernelVersion(readLine("/proc/version"));
        } catch (IOException e) {
            Logger.d("TabletInfoSettingActivity", "getFormattedKernelVersion 0003");
            Logger.e("TabletInfoSettingActivity", "IO Exception when getting kernel version for Device Info screen", e);
            return "Unavailable";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideNavigationBar(boolean z) {
        Logger.d("TabletInfoSettingActivity", "hideNavigationBar 0001");
        try {
            Logger.d("TabletInfoSettingActivity", "hideNavigationBar 0002");
            if (this.mDchaService != null) {
                Logger.d("TabletInfoSettingActivity", "hideNavigationBar 0003");
                this.mDchaService.hideNavigationBar(z);
            }
        } catch (RemoteException e) {
            Logger.d("TabletInfoSettingActivity", "hideNavigationBar 0004");
            Logger.e("TabletInfoSettingActivity", "RemoteException", e);
        }
        Logger.d("TabletInfoSettingActivity", "hideNavigationBar 0005");
    }

    private String readLine(String str) throws IOException {
        Logger.d("TabletInfoSettingActivity", "readLine 0001");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(str), 256);
        try {
            Logger.d("TabletInfoSettingActivity", "readLine 0002");
            String readLine = bufferedReader.readLine();
            Logger.d("TabletInfoSettingActivity", "readLine 0003");
            bufferedReader.close();
            return readLine;
        } catch (Throwable th) {
            Logger.d("TabletInfoSettingActivity", "readLine 0003");
            bufferedReader.close();
            throw th;
        }
    }

    private void setStringSummary(String str, String str2) {
        Logger.d("TabletInfoSettingActivity", "setStringSummary 0001");
        try {
            Logger.d("TabletInfoSettingActivity", "setStringSummary 0002");
            findPreference(str).setSummary(str2);
            Logger.d("TabletInfoSettingActivity", "setStringSummary 0003");
        } catch (RuntimeException e) {
            Logger.d("TabletInfoSettingActivity", "setStringSummary 0004");
            Logger.e("TabletInfoSettingActivity", "RuntimeException", e);
            findPreference(str).setSummary(getResources().getString(2131230780));
        }
        Logger.d("TabletInfoSettingActivity", "setStringSummary 0005");
    }

    private boolean updatePreferenceToSpecificActivityOrRemove(Context context, PreferenceGroup preferenceGroup, String str, int i) {
        Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0001");
        Preference findPreference = preferenceGroup.findPreference(str);
        if (findPreference == null) {
            Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0002");
            return false;
        }
        Intent intent = findPreference.getIntent();
        if (intent != null) {
            Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0003");
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
            int size = queryIntentActivities.size();
            for (int i2 = 0; i2 < size; i2++) {
                Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0004");
                ResolveInfo resolveInfo = queryIntentActivities.get(i2);
                if ((resolveInfo.activityInfo.applicationInfo.flags & 1) != 0) {
                    Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0005");
                    findPreference.setIntent(new Intent().setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                    if ((i & 1) != 0) {
                        Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0006");
                        findPreference.setTitle(resolveInfo.loadLabel(packageManager));
                    }
                    Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0007");
                    return true;
                }
            }
        }
        preferenceGroup.removePreference(findPreference);
        Logger.d("TabletInfoSettingActivity", "updatePreferenceToSpecificActivityOrRemove 0008");
        return false;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        Logger.d("TabletInfoSettingActivity", "dispatchKeyEvent 0001");
        if (keyEvent.getAction() != 0 || keyEvent.getKeyCode() != 4) {
            Logger.d("TabletInfoSettingActivity", "dispatchKeyEvent 0004");
            return false;
        }
        Logger.d("TabletInfoSettingActivity", "dispatchKeyEvent 0002");
        moveSettingActivity();
        Logger.d("TabletInfoSettingActivity", "dispatchKeyEvent 0003");
        return true;
    }

    protected String getKvsValue(Context context, Uri uri, String str, String str2) {
        String str3;
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = context.getContentResolver().query(uri, new String[]{"value"}, "key=?", new String[]{str}, null);
                String str4 = str2;
                if (query != null) {
                    str4 = str2;
                    if (query.moveToFirst()) {
                        cursor2 = query;
                        cursor = query;
                        str4 = query.getString(query.getColumnIndex("value"));
                    }
                }
                str3 = str4;
                if (query != null) {
                    query.close();
                    str3 = str4;
                }
            } catch (Exception e) {
                Logger.d("TabletInfoSettingActivity", "getKvsValue", e);
                str3 = str2;
                if (cursor2 != null) {
                    cursor2.close();
                    str3 = str2;
                }
            }
            return str3;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    protected void moveSettingActivity() {
        Logger.d("TabletInfoSettingActivity", "moveSettingActivity 0001");
        Intent intent = new Intent();
        intent.setClassName("jp.co.benesse.dcha.allgrade.usersetting", "jp.co.benesse.dcha.allgrade.usersetting.activity.SettingMenuActivity");
        startActivity(intent);
        finish();
        Logger.d("TabletInfoSettingActivity", "moveSettingActivity 0002");
    }

    @Override // android.preference.PreferenceActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        Logger.d("TabletInfoSettingActivity", "onCreate 0001");
        super.onCreate(bundle);
        addPreferencesFromResource(2130968576);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("jp.co.benesse.dcha.allgrade.b001.ACTION_ACTIVATE");
        registerReceiver(this.mReceiver, intentFilter);
        setStringSummary("firmware_version", Build.VERSION.RELEASE);
        findPreference("firmware_version").setEnabled(true);
        String str = Build.VERSION.SECURITY_PATCH;
        if ("".equals(str)) {
            getPreferenceScreen().removePreference(findPreference("security_patch"));
        } else {
            setStringSummary("security_patch", str);
        }
        setStringSummary("device_model", Build.MODEL);
        setStringSummary("build_number", Build.DISPLAY);
        findPreference("build_number").setEnabled(true);
        findPreference("kernel_version").setSummary(getFormattedKernelVersion());
        String kvsValue = getKvsValue(this, URI_TEST_ENVIRONMENT_INFO, "environment", null);
        String kvsValue2 = getKvsValue(this, URI_TEST_ENVIRONMENT_INFO, "version", null);
        if (TextUtils.isEmpty(kvsValue) || TextUtils.isEmpty(kvsValue2)) {
            getPreferenceScreen().removePreference(findPreference("test_environment"));
        } else {
            findPreference("test_environment").setSummary(getString(2131230970, new Object[]{kvsValue, kvsValue2}));
        }
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        bindService(intent, this.mServiceConnection, 1);
        PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("container");
        updatePreferenceToSpecificActivityOrRemove(this, preferenceGroup, "terms", 1);
        updatePreferenceToSpecificActivityOrRemove(this, preferenceGroup, "license", 1);
        updatePreferenceToSpecificActivityOrRemove(this, preferenceGroup, "copyright", 1);
        Logger.d("TabletInfoSettingActivity", "onCreate 0002");
    }

    @Override // android.preference.PreferenceActivity, android.app.ListActivity, android.app.Activity
    protected void onDestroy() {
        Logger.d("TabletInfoSettingActivity", "onDestroy 0001");
        super.onDestroy();
        unregisterReceiver(this.mReceiver);
        this.mReceiver = null;
        if (this.mServiceConnection != null) {
            Logger.d("TabletInfoSettingActivity", "onDestroy 0002");
            unbindService(this.mServiceConnection);
            this.mServiceConnection = null;
            this.mDchaService = null;
        }
        Logger.d("TabletInfoSettingActivity", "onDestroy 0003");
    }

    @Override // android.app.Activity
    protected void onPause() {
        Logger.d("TabletInfoSettingActivity", "onPause 0001");
        super.onPause();
        Logger.d("TabletInfoSettingActivity", "onPause 0002");
    }

    @Override // android.app.Activity
    protected void onResume() {
        Logger.d("TabletInfoSettingActivity", "onResume 0001");
        super.onResume();
        hideNavigationBar(false);
        Logger.d("TabletInfoSettingActivity", "onResume 0002");
    }
}
