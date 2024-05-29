package com.mediatek.server;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Point;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BenesseExtension;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBenesseExtensionService;
import android.os.IBinder;
import android.os.IStsExtensionService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import com.android.internal.app.ColorDisplayController;
import com.mediatek.datashaping.DataShapingServiceImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
/* loaded from: classes.dex */
public class BenesseExtensionService extends IBenesseExtensionService.Stub {
    static final String ACTION_DT_FW_UPDATED = "com.panasonic.sanyo.ts.intent.action.DIGITIZER_FIRMWARE_UPDATED";
    static final String ACTION_TP_FW_UPDATED = "com.panasonic.sanyo.ts.intent.action.TOUCHPANEL_FIRMWARE_UPDATED";
    static final String BC_COMPATSCREEN = "bc:compatscreen";
    static final String BC_DT_FW_UPDATE = "bc:digitizer:fw_update";
    static final String BC_DT_FW_VERSION = "bc:digitizer:fw_version";
    static final String BC_FTS_PALM_SIZE = "bc:touchpanel:palmreject:size";
    static final String BC_FTS_PEN_BATTERY = "bc:pen:battery";
    static final String BC_FTS_TP_FW_UPDATE = "bc:touchpanel:fts:fw_update";
    static final String BC_FTS_TP_FW_VERSION = "bc:touchpanel:fts:fw_version";
    static final String BC_MAC_ADDRESS = "bc:mac_address";
    static final String BC_NIGHTCOLOR_CURRENT = "bc:nightcolor:current";
    static final String BC_NIGHTCOLOR_MAX = "bc:nightcolor:max";
    static final String BC_NIGHTCOLOR_MIN = "bc:nightcolor:min";
    static final String BC_NIGHTMODE_ACTIVE = "bc:nightmode:active";
    static final String BC_NVT_PALM_SIZE = "bc:touchpanel:palmreject:size";
    static final String BC_NVT_PEN_BATTERY = "bc:pen:battery";
    static final String BC_NVT_TP_FW_UPDATE = "bc:touchpanel:nvt:fw_update";
    static final String BC_NVT_TP_FW_VERSION = "bc:touchpanel:nvt:fw_version";
    static final String BC_PASSWORD_HIT_FLAG = "bc_password_hit";
    static final String BC_SERIAL_NO = "bc:serial_no";
    static final String BC_TP_FW_UPDATE = "bc:touchpanel:fw_update";
    static final String BC_TP_FW_VERSION = "bc:touchpanel:fw_version";
    static final String BC_TP_LCD_TYPE = "bc:touchpanel:lcd_type";
    static final String DCHA_HASH_FILEPATH = "/factory/dcha_hash";
    static final String DCHA_STATE = "dcha_state";
    static final String EXTRA_RESULT = "result";
    static final String HASH_ALGORITHM = "SHA-256";
    static final String JAPAN_LOCALE = "ja-JP";
    static final String PACKAGE_NAME_BROWSER = "com.android.browser";
    static final String PACKAGE_NAME_QSB = "com.android.quicksearchbox";
    static final String PACKAGE_NAME_TRACEUR = "com.android.traceur";
    static final String PROPERTY_DCHA_STATE = "persist.sys.bc.dcha_state";
    static final String PROPERTY_LOCALE = "persist.sys.locale";
    static final String TAG = "BenesseExtensionService";
    private ColorDisplayController mColorDisplayController;
    private Context mContext;
    private IWindowManager mWindowManager;
    private int tp_type;
    static final File SYSFILE_TP_VERSION = new File("/sys/devices/platform/soc/11007000.i2c/i2c-0/0-000a/tp_fwver");
    static final File SYSFILE_DT_VERSION = new File("/sys/devices/platform/soc/11009000.i2c/i2c-2/2-0009/digi_fwver");
    static final File SYSFILE_NVT_PARM_REJECT = new File("/sys/devices/platform/soc/1100f000.i2c/i2c-3/3-0062/tp_palm_reject");
    static final File SYSFILE_FTS_PARM_REJECT = new File("/sys/devices/platform/soc/1100f000.i2c/i2c-3/3-0038/fts_palm_reject");
    static final File PROC_NVT_TP_VERSION = new File("/proc/nvt_fw_version");
    static final File FTS_TP_VERSION = new File("/sys/class/i2c-dev/i2c-3/device/3-0038/fts_fw_version");
    private static final byte[] DEFAULT_HASH = "a1e3cf8aa7858a458972592ebb9438e967da30d196bd6191cc77606cc60af183".getBytes();
    static final int[][] mTable = {new int[]{240, 1920, 1200}, new int[]{160, 1024, 768}, new int[]{160, 1280, 800}};
    private boolean mIsUpdating = false;
    private IStsExtensionService mStsExtensionService = null;
    private final byte[] HEX_TABLE = "0123456789abcdef".getBytes();
    private Handler mHandler = new Handler(true);
    private ContentObserver mDchaStateObserver = new ContentObserver(this.mHandler) { // from class: com.mediatek.server.BenesseExtensionService.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            synchronized (BenesseExtensionService.this.mLock) {
                SystemProperties.set(BenesseExtensionService.PROPERTY_DCHA_STATE, String.valueOf(BenesseExtensionService.this.getDchaStateInternal()));
                BenesseExtensionService.this.changeSafemodeRestriction(BenesseExtensionService.this.getDchaStateInternal());
                BenesseExtensionService.this.updateBrowserEnabled();
                BenesseExtensionService.this.changeDefaultUsbFunction(BenesseExtensionService.this.getDchaStateInternal());
                BenesseExtensionService.this.changeDisallowInstallUnknownSource(BenesseExtensionService.this.getDchaCompletedPast());
                BenesseExtensionService.this.updateTraceurEnabled();
            }
        }
    };
    private ContentObserver mAdbObserver = new ContentObserver(this.mHandler) { // from class: com.mediatek.server.BenesseExtensionService.2
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            synchronized (BenesseExtensionService.this.mLock) {
                Log.i(BenesseExtensionService.TAG, "getADBENABLE=" + BenesseExtensionService.this.getAdbEnabled());
                if (!BenesseExtensionService.this.changeAdbEnable()) {
                    BenesseExtensionService.this.updateBrowserEnabled();
                }
            }
        }
    };
    private BroadcastReceiver mLanguageReceiver = new BroadcastReceiver() { // from class: com.mediatek.server.BenesseExtensionService.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (BenesseExtensionService.this.mLock) {
                if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                    BenesseExtensionService.this.updateBrowserEnabled();
                }
            }
        }
    };
    ServiceConnection mConn = new ServiceConnection() { // from class: com.mediatek.server.BenesseExtensionService.4
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BenesseExtensionService.this.mStsExtensionService = IStsExtensionService.Stub.asInterface(iBinder);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            BenesseExtensionService.this.mStsExtensionService = null;
        }
    };
    private Object mLock = new Object();

    /* JADX INFO: Access modifiers changed from: package-private */
    public BenesseExtensionService(Context context) {
        this.tp_type = -1;
        this.mContext = context;
        synchronized (this.mLock) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(DCHA_STATE), false, this.mDchaStateObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("adb_enabled"), false, this.mAdbObserver, -1);
            this.mContext.registerReceiver(this.mLanguageReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
            changeSafemodeRestriction(getDchaStateInternal());
            updateBrowserEnabled();
            changeDefaultUsbFunction(getDchaStateInternal());
            changeDisallowInstallUnknownSource(getDchaCompletedPast());
            updateTraceurEnabled();
        }
        this.mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.checkService("window"));
        this.mColorDisplayController = new ColorDisplayController(context);
        this.mContext.registerReceiver(new BootCompletedReceiver(), new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        if (!PROC_NVT_TP_VERSION.exists()) {
            if (!FTS_TP_VERSION.exists()) {
                Log.e(TAG, "----- TP:Unkown -----");
                return;
            }
            Log.i(TAG, "----- TP:FTS -----");
            this.tp_type = 1;
            return;
        }
        Log.i(TAG, "----- TP:NVT -----");
        this.tp_type = 0;
    }

    public int getDchaState() {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            return getDchaStateInternal();
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getDchaStateInternal() {
        return Settings.System.getInt(this.mContext.getContentResolver(), DCHA_STATE, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean getDchaCompletedPast() {
        return !BenesseExtension.IGNORE_DCHA_COMPLETED_FILE.exists() && BenesseExtension.COUNT_DCHA_COMPLETED_FILE.exists();
    }

    public void setDchaState(int i) {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            Settings.System.putInt(this.mContext.getContentResolver(), DCHA_STATE, i);
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public String getString(String str) {
        if (str == null) {
            return null;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            switch (str.hashCode()) {
                case -1149331608:
                    if (str.equals(BC_TP_LCD_TYPE)) {
                        c = 6;
                        break;
                    }
                    break;
                case -1125691405:
                    if (str.equals(BC_SERIAL_NO)) {
                        c = 1;
                        break;
                    }
                    break;
                case 94655307:
                    if (str.equals(BC_MAC_ADDRESS)) {
                        c = 0;
                        break;
                    }
                    break;
                case 600943506:
                    if (str.equals(BC_DT_FW_VERSION)) {
                        c = 3;
                        break;
                    }
                    break;
                case 681159668:
                    if (str.equals(BC_NVT_TP_FW_VERSION)) {
                        c = 4;
                        break;
                    }
                    break;
                case 888384667:
                    if (str.equals(BC_FTS_TP_FW_VERSION)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1361443174:
                    if (str.equals(BC_TP_FW_VERSION)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                    if (wifiManager == null) {
                        return null;
                    }
                    WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                    if (connectionInfo == null) {
                        return null;
                    }
                    return connectionInfo.getMacAddress();
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN_LOCKED /* 1 */:
                    return Build.getSerial();
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN /* 2 */:
                    return !"TAB-A05-BD".equals(Build.PRODUCT) ? getTouchpanelVersion() : getFirmwareVersion(SYSFILE_TP_VERSION);
                case DataShapingServiceImpl.DATA_SHAPING_STATE_CLOSE /* 3 */:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        break;
                    } else {
                        return getFirmwareVersion(SYSFILE_DT_VERSION);
                    }
                case 4:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        return getTouchpanelVersion();
                    }
                    break;
                case 5:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        return getTouchpanelVersion();
                    }
                    break;
                case 6:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        return getLcdType();
                    }
                    break;
            }
            return null;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public boolean putString(String str, String str2) {
        if (str == null || str2 == null) {
            return false;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            int hashCode = str.hashCode();
            if (hashCode != 1111447085) {
                if (hashCode != 1247406799) {
                    if (hashCode == 1964675707 && str.equals(BC_TP_FW_UPDATE)) {
                        c = 0;
                    }
                } else if (str.equals(BC_DT_FW_UPDATE)) {
                    c = 1;
                }
            } else if (str.equals(BC_NVT_TP_FW_UPDATE)) {
                c = 2;
            }
            switch (c) {
                case 0:
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN_LOCKED /* 1 */:
                    if ("TAB-A05-BD".equals(Build.PRODUCT)) {
                        String replaceFirst = str2.replaceFirst("^/sdcard/", "/data/media/0/").replaceFirst("^/storage/emulated/0/", "/data/media/0/");
                        if (!new File(replaceFirst).isFile()) {
                            Log.e(TAG, "----- putString() : invalid file. name[" + str + "] value[" + str2 + "] -----");
                            break;
                        } else if (!checkHexFile(replaceFirst)) {
                            break;
                        } else {
                            return executeFwUpdate(getUpdateParams(str, replaceFirst));
                        }
                    } else if (BC_TP_FW_UPDATE.equals(str)) {
                        return updateTouchpanelFw(str2);
                    }
                    break;
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN /* 2 */:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        return updateTouchpanelFw(str2);
                    }
                    break;
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public int getInt(String str) {
        char c;
        if (str == null) {
            return -1;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            switch (str.hashCode()) {
                case -286987330:
                    if (str.equals(BC_NIGHTMODE_ACTIVE)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 141985806:
                    if (str.equals("bc:touchpanel:palmreject:size")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 367025166:
                    if (str.equals(BC_NIGHTCOLOR_MAX)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 367025404:
                    if (str.equals(BC_NIGHTCOLOR_MIN)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 562531059:
                    if (str.equals("bc:pen:battery")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1209732899:
                    if (str.equals(BC_NIGHTCOLOR_CURRENT)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1359997191:
                    if (str.equals(BC_COMPATSCREEN)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1664403245:
                    if (str.equals(BC_PASSWORD_HIT_FLAG)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    return getCompatScreenMode();
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN_LOCKED /* 1 */:
                    return this.mColorDisplayController.isActivated() ? 1 : 0;
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN /* 2 */:
                    return this.mColorDisplayController.getMaximumColorTemperature();
                case DataShapingServiceImpl.DATA_SHAPING_STATE_CLOSE /* 3 */:
                    return this.mColorDisplayController.getMinimumColorTemperature();
                case 4:
                    return this.mColorDisplayController.getColorTemperature();
                case 5:
                    return Settings.System.getInt(this.mContext.getContentResolver(), BC_PASSWORD_HIT_FLAG, 0);
                case 6:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        return getPenBattery();
                    }
                    break;
                case 7:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        return getPalmrejectSize();
                    }
                    break;
            }
            return -1;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public boolean putInt(String str, int i) {
        char c;
        if (str == null) {
            return false;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            switch (str.hashCode()) {
                case -286987330:
                    if (str.equals(BC_NIGHTMODE_ACTIVE)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 141985806:
                    if (str.equals("bc:touchpanel:palmreject:size")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1209732899:
                    if (str.equals(BC_NIGHTCOLOR_CURRENT)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1359997191:
                    if (str.equals(BC_COMPATSCREEN)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1664403245:
                    if (str.equals(BC_PASSWORD_HIT_FLAG)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    return setCompatScreenMode(i);
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN_LOCKED /* 1 */:
                    if (i == 0 || i == 1) {
                        return this.mColorDisplayController.setActivated(i == 1);
                    }
                    return false;
                case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN /* 2 */:
                    return this.mColorDisplayController.setColorTemperature(i);
                case DataShapingServiceImpl.DATA_SHAPING_STATE_CLOSE /* 3 */:
                    Settings.System.putInt(this.mContext.getContentResolver(), BC_PASSWORD_HIT_FLAG, i);
                    return true;
                case 4:
                    if (!"TAB-A05-BD".equals(Build.PRODUCT)) {
                        return setPalmrejectSize(i);
                    }
                    break;
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public boolean checkPassword(String str) {
        MessageDigest messageDigest;
        if (str == null) {
            return false;
        }
        byte[] bArr = new byte[64];
        byte[] bArr2 = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(DCHA_HASH_FILEPATH);
            if (fileInputStream.read(bArr) != 64) {
                bArr = (byte[]) DEFAULT_HASH.clone();
            }
            $closeResource(null, fileInputStream);
        } catch (IOException e) {
            bArr = (byte[]) DEFAULT_HASH.clone();
        }
        try {
            messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e2) {
            messageDigest = null;
        }
        if (messageDigest != null) {
            messageDigest.reset();
            byte[] digest = messageDigest.digest(str.getBytes());
            bArr2 = new byte[64];
            for (int i = 0; i < digest.length && i < bArr2.length / 2; i++) {
                int i2 = i * 2;
                bArr2[i2] = this.HEX_TABLE[(digest[i] >> 4) & 15];
                bArr2[i2 + 1] = this.HEX_TABLE[digest[i] & 15];
            }
        }
        boolean equals = Arrays.equals(bArr, bArr2);
        Log.i(TAG, "password comparison = " + equals);
        if (equals) {
            putInt(BC_PASSWORD_HIT_FLAG, 1);
        }
        return equals;
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th == null) {
            autoCloseable.close();
            return;
        }
        try {
            autoCloseable.close();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getAdbEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "adb_enabled", 0);
    }

    private String getLanguage() {
        String str = SystemProperties.get(PROPERTY_LOCALE, JAPAN_LOCALE);
        return (str == null || str.equals("")) ? JAPAN_LOCALE : str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean changeAdbEnable() {
        if (getAdbEnabled() == 0 || BenesseExtension.getDchaState() == 3 || !getDchaCompletedPast() || getInt(BC_PASSWORD_HIT_FLAG) != 0) {
            return false;
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeSafemodeRestriction(int i) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager != null) {
            userManager.setUserRestriction("no_safe_boot", i > 0, UserHandle.SYSTEM);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeDisallowInstallUnknownSource(boolean z) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager != null) {
            userManager.setUserRestriction("no_install_unknown_sources", z, UserHandle.SYSTEM);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBrowserEnabled() {
        int i = 2;
        if (!getDchaCompletedPast() && (getDchaStateInternal() == 0 || getAdbEnabled() != 0 || !JAPAN_LOCALE.equals(getLanguage()))) {
            i = 0;
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        int applicationEnabledSetting = packageManager.getApplicationEnabledSetting(PACKAGE_NAME_BROWSER);
        int applicationEnabledSetting2 = packageManager.getApplicationEnabledSetting(PACKAGE_NAME_QSB);
        if (i != applicationEnabledSetting) {
            packageManager.setApplicationEnabledSetting(PACKAGE_NAME_BROWSER, i, 0);
        }
        if (i != applicationEnabledSetting2) {
            packageManager.setApplicationEnabledSetting(PACKAGE_NAME_QSB, i, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeDefaultUsbFunction(int i) {
        if (i > 0) {
            ((UsbManager) this.mContext.getSystemService(UsbManager.class)).setScreenUnlockedFunctions(0L);
        }
    }

    private int getCompatScreenMode() {
        Point point = new Point();
        try {
            int baseDisplayDensity = this.mWindowManager.getBaseDisplayDensity(0);
            this.mWindowManager.getBaseDisplaySize(0, point);
            for (int i = 0; i < mTable.length; i++) {
                if (baseDisplayDensity == mTable[i][0] && point.x == mTable[i][1] && point.y == mTable[i][2]) {
                    return i;
                }
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "----- getCompatScreenMode() : Exception occurred! -----", e);
            return -1;
        }
    }

    private boolean setCompatScreenMode(int i) {
        if (i < 0 || i >= mTable.length) {
            return false;
        }
        try {
            this.mWindowManager.setForcedDisplayDensityForUser(0, mTable[i][0], -2);
            this.mWindowManager.setForcedDisplaySize(0, mTable[i][1], mTable[i][2]);
            return getCompatScreenMode() == i;
        } catch (RemoteException e) {
            Log.e(TAG, "----- setCompatScreenMode() : Exception occurred! -----", e);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTraceurEnabled() {
        if (getDchaStateInternal() != 0) {
            return;
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager.getApplicationEnabledSetting(PACKAGE_NAME_TRACEUR) != 0) {
            packageManager.setApplicationEnabledSetting(PACKAGE_NAME_TRACEUR, 0, 0);
        }
    }

    private String getFirmwareVersion(File file) {
        if (this.mIsUpdating) {
            return null;
        }
        if (!file.exists()) {
            return "";
        }
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            try {
                String readLine = bufferedReader.readLine();
                $closeResource(null, bufferedReader);
                $closeResource(null, fileReader);
                return readLine;
            } finally {
            }
        } catch (Throwable th) {
            return "";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class UpdateParams {
        public String broadcast;
        public String[] cmd;

        private UpdateParams() {
        }
    }

    private UpdateParams getUpdateParams(String str, String str2) {
        char c;
        UpdateParams updateParams = new UpdateParams();
        int hashCode = str.hashCode();
        if (hashCode != 1247406799) {
            if (hashCode == 1964675707 && str.equals(BC_TP_FW_UPDATE)) {
                c = 0;
            }
            c = 65535;
        } else {
            if (str.equals(BC_DT_FW_UPDATE)) {
                c = 1;
            }
            c = 65535;
        }
        switch (c) {
            case 0:
                updateParams.cmd = new String[]{"/system/bin/.wacom_flash", str2, "1", "i2c-0"};
                updateParams.broadcast = ACTION_TP_FW_UPDATED;
                return updateParams;
            case DataShapingServiceImpl.DATA_SHAPING_STATE_OPEN_LOCKED /* 1 */:
                updateParams.cmd = new String[]{"/system/bin/.wac_flash", str2, "i2c-2"};
                updateParams.broadcast = ACTION_DT_FW_UPDATED;
                return updateParams;
            default:
                return null;
        }
    }

    private boolean executeFwUpdate(final UpdateParams updateParams) {
        if (this.mIsUpdating) {
            Log.e(TAG, "----- FW update : already updating! -----");
            return false;
        }
        this.mIsUpdating = true;
        new Thread(new Runnable() { // from class: com.mediatek.server.-$$Lambda$BenesseExtensionService$DuLYMgReFex30dZ2dylIKOPJ6RA
            @Override // java.lang.Runnable
            public final void run() {
                BenesseExtensionService.lambda$executeFwUpdate$1(BenesseExtensionService.this, updateParams);
            }
        }).start();
        return true;
    }

    public static /* synthetic */ void lambda$executeFwUpdate$1(final BenesseExtensionService benesseExtensionService, final UpdateParams updateParams) {
        final int i;
        try {
            i = Runtime.getRuntime().exec(updateParams.cmd).waitFor();
        } catch (Throwable th) {
            Log.e(TAG, "----- Exception occurred! -----", th);
            i = -1;
        }
        benesseExtensionService.mHandler.post(new Runnable() { // from class: com.mediatek.server.-$$Lambda$BenesseExtensionService$erbcCrbZOhYH-JEcBSKtqZ9g-84
            @Override // java.lang.Runnable
            public final void run() {
                BenesseExtensionService.lambda$executeFwUpdate$0(BenesseExtensionService.this, updateParams, i);
            }
        });
    }

    public static /* synthetic */ void lambda$executeFwUpdate$0(BenesseExtensionService benesseExtensionService, UpdateParams updateParams, int i) {
        benesseExtensionService.mIsUpdating = false;
        benesseExtensionService.mContext.sendBroadcastAsUser(new Intent(updateParams.broadcast).putExtra(EXTRA_RESULT, i), UserHandle.ALL);
    }

    /* JADX WARN: Code restructure failed: missing block: B:28:0x0072, code lost:
        android.util.Log.e(com.mediatek.server.BenesseExtensionService.TAG, "----- invalid data! -----");
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x007a, code lost:
        $closeResource(null, r2);
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x007d, code lost:
        $closeResource(null, r1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x0080, code lost:
        return false;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean checkHexFile(String str) {
        try {
            FileReader fileReader = new FileReader(str);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String str2 = null;
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        $closeResource(null, bufferedReader);
                        $closeResource(null, fileReader);
                        if (str2.charAt(7) == '0' && str2.charAt(8) == '1') {
                            return true;
                        }
                        Log.e(TAG, "----- last line is not end of file! -----");
                        return false;
                    } else if (readLine.charAt(0) != ';') {
                        if (!readLine.matches(":[a-fA-F0-9]+") || readLine.length() % 2 == 0) {
                            break;
                        }
                        int i = 0;
                        for (int i2 = 1; i2 < readLine.length() - 1; i2 += 2) {
                            i += (Character.digit(readLine.charAt(i2), 16) << 4) + Character.digit(readLine.charAt(i2 + 1), 16);
                        }
                        if ((i & 255) != 0) {
                            Log.e(TAG, "----- wrong checksum! -----");
                            $closeResource(null, bufferedReader);
                            $closeResource(null, fileReader);
                            return false;
                        }
                        str2 = readLine;
                    } else {
                        Log.w(TAG, "----- found comment line. -----");
                    }
                } finally {
                }
            }
        } catch (Throwable th) {
            Log.e(TAG, "----- Exception occurred!!! -----", th);
            return false;
        }
    }

    /* loaded from: classes.dex */
    private final class BootCompletedReceiver extends BroadcastReceiver {
        private BootCompletedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Intent intent2 = new Intent("com.sts.tottori.stsextension.StsExtensionService");
            intent2.setPackage("com.sts.tottori.stsextension");
            context.bindServiceAsUser(intent2, BenesseExtensionService.this.mConn, 1, UserHandle.CURRENT);
        }
    }

    private String getTouchpanelVersion() {
        if (this.mStsExtensionService != null) {
            try {
                return this.mStsExtensionService.getTouchpanelVersion();
            } catch (Throwable th) {
                Log.e(TAG, "----- Exception occurred! -----", th);
                return "";
            }
        }
        return "";
    }

    private String getLcdType() {
        if (!PROC_NVT_TP_VERSION.exists() && !FTS_TP_VERSION.exists()) {
            return "";
        }
        return String.valueOf(this.tp_type);
    }

    private boolean updateTouchpanelFw(String str) {
        if (this.mStsExtensionService != null) {
            try {
                return this.mStsExtensionService.updateTouchpanelFw(str);
            } catch (Throwable th) {
                Log.e(TAG, "----- Exception occurred! -----", th);
                return false;
            }
        }
        return false;
    }

    private int getPenBattery() {
        if (this.mStsExtensionService != null) {
            try {
                return this.mStsExtensionService.getPenBattery();
            } catch (Throwable th) {
                Log.e(TAG, "----- Exception occurred! -----", th);
                return 0;
            }
        }
        return 0;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
        	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
        	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    private int getPalmrejectSize() {
        /*
            r7 = this;
            java.lang.String r0 = "3"
            int r1 = r7.tp_type
            r2 = 0
            if (r1 != 0) goto L48
            java.io.FileReader r1 = new java.io.FileReader     // Catch: java.lang.Throwable -> L3e
            java.io.File r3 = com.mediatek.server.BenesseExtensionService.SYSFILE_NVT_PARM_REJECT     // Catch: java.lang.Throwable -> L3e
            r1.<init>(r3)     // Catch: java.lang.Throwable -> L3e
            java.io.BufferedReader r3 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L38
            r3.<init>(r1)     // Catch: java.lang.Throwable -> L38
            java.lang.String r4 = r3.readLine()     // Catch: java.lang.Throwable -> L2c
            $closeResource(r2, r3)     // Catch: java.lang.Throwable -> L25
            $closeResource(r2, r1)     // Catch: java.lang.Throwable -> L1e
            goto L47
        L1e:
            r0 = move-exception
            r1 = r0
            goto L40
        L21:
            r0 = move-exception
            r3 = r0
            r0 = r4
            goto L3a
        L25:
            r0 = move-exception
            r2 = r0
            r0 = r4
            goto L39
        L29:
            r4 = move-exception
            r5 = r2
            goto L32
        L2c:
            r4 = move-exception
            throw r4     // Catch: java.lang.Throwable -> L2e
        L2e:
            r5 = move-exception
            r6 = r5
            r5 = r4
            r4 = r6
        L32:
            $closeResource(r5, r3)     // Catch: java.lang.Throwable -> L38
            throw r4     // Catch: java.lang.Throwable -> L38
        L36:
            r3 = move-exception
            goto L3a
        L38:
            r2 = move-exception
        L39:
            throw r2     // Catch: java.lang.Throwable -> L36
        L3a:
            $closeResource(r2, r1)     // Catch: java.lang.Throwable -> L3e
            throw r3     // Catch: java.lang.Throwable -> L3e
        L3e:
            r1 = move-exception
            r4 = r0
        L40:
            java.lang.String r0 = "BenesseExtensionService"
            java.lang.String r2 = "----- Exception occurred! -----"
            android.util.Log.e(r0, r2, r1)
        L47:
            goto L88
        L48:
            java.io.FileReader r1 = new java.io.FileReader     // Catch: java.lang.Throwable -> L7f
            java.io.File r3 = com.mediatek.server.BenesseExtensionService.SYSFILE_FTS_PARM_REJECT     // Catch: java.lang.Throwable -> L7f
            r1.<init>(r3)     // Catch: java.lang.Throwable -> L7f
            java.io.BufferedReader r3 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L79
            r3.<init>(r1)     // Catch: java.lang.Throwable -> L79
            java.lang.String r4 = r3.readLine()     // Catch: java.lang.Throwable -> L6d
            $closeResource(r2, r3)     // Catch: java.lang.Throwable -> L66
            $closeResource(r2, r1)     // Catch: java.lang.Throwable -> L5f
            goto L88
        L5f:
            r0 = move-exception
            r1 = r0
            goto L81
        L62:
            r0 = move-exception
            r3 = r0
            r0 = r4
            goto L7b
        L66:
            r0 = move-exception
            r2 = r0
            r0 = r4
            goto L7a
        L6a:
            r4 = move-exception
            r5 = r2
            goto L73
        L6d:
            r4 = move-exception
            throw r4     // Catch: java.lang.Throwable -> L6f
        L6f:
            r5 = move-exception
            r6 = r5
            r5 = r4
            r4 = r6
        L73:
            $closeResource(r5, r3)     // Catch: java.lang.Throwable -> L79
            throw r4     // Catch: java.lang.Throwable -> L79
        L77:
            r3 = move-exception
            goto L7b
        L79:
            r2 = move-exception
        L7a:
            throw r2     // Catch: java.lang.Throwable -> L77
        L7b:
            $closeResource(r2, r1)     // Catch: java.lang.Throwable -> L7f
            throw r3     // Catch: java.lang.Throwable -> L7f
        L7f:
            r1 = move-exception
            r4 = r0
        L81:
            java.lang.String r0 = "BenesseExtensionService"
            java.lang.String r2 = "----- Exception occurred! -----"
            android.util.Log.e(r0, r2, r1)
        L88:
            int r0 = java.lang.Integer.parseInt(r4)
            r1 = 4
            if (r0 != r1) goto L90
            r0 = 0
        L90:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.server.BenesseExtensionService.getPalmrejectSize():int");
    }

    private boolean setPalmrejectSize(int i) {
        if (i < 0 || 3 < i) {
            return false;
        }
        if (this.tp_type == 0) {
            SystemProperties.set("nvt.set_parm_rejection", String.valueOf(i != 0 ? i : 4));
        } else {
            SystemProperties.set("fts.set_parm_rejection", String.valueOf(i != 0 ? i : 4));
        }
        return i == getPalmrejectSize();
    }
}
