package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.BenesseExtension;
import android.os.Binder;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBenesseExtensionService;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.BrowserContract;
import android.provider.Settings;
import java.io.File;
/* loaded from: framework.zip:com/android/server/BenesseExtensionService.class */
public class BenesseExtensionService extends IBenesseExtensionService.Stub {
    static final String BC_PASSWORD_HIT_FLAG = "bc_password_hit";
    static final String DCHA_HASH_FILEPATH = "/factory/dcha_hash";
    private static final byte[] DEFAULT_HASH = "9b66c16d267c7c3331acafd4cb449219118998678205e8843b5e1094a9b14237".getBytes();
    static final String HASH_ALGORITHM = "SHA-256";
    static final String JAPAN_LOCALE = "ja-JP";
    static final String PROPERTY_LOCALE = "persist.sys.locale";
    static final String TAG = "BenesseExtensionService";
    private final byte[] HEX_TABLE;
    private Context mContext;
    private int mDchaState;
    private int mEnabledAdb;
    private Handler mHandler;
    private String mLanguage;
    private Object mLock;

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
        	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
        	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    BenesseExtensionService(android.content.Context r7) {
        /*
            Method dump skipped, instructions count: 416
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BenesseExtensionService.<init>(android.content.Context):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean changeAdbEnable() {
        if (getAdbEnabled() == 0 || getDchaState() == 3 || !getDchaCompletedPast() || Settings.System.getInt(this.mContext.getContentResolver(), BC_PASSWORD_HIT_FLAG, 0) != 0) {
            return false;
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeDisallowInstallUnknownSource(boolean z) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager != null) {
            userManager.setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, z, UserHandle.SYSTEM);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getAdbEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "adb_enabled", 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean getDchaCompletedPast() {
        boolean z = false;
        if (getUid(BenesseExtension.IGNORE_DCHA_COMPLETED_FILE) != 0) {
            z = BenesseExtension.COUNT_DCHA_COMPLETED_FILE.exists();
        }
        return z;
    }

    /* JADX WARN: Code restructure failed: missing block: B:5:0x0014, code lost:
        if (r0.equals("") != false) goto L8;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private String getLanguage() {
        String str;
        String str2 = SystemProperties.get(PROPERTY_LOCALE, JAPAN_LOCALE);
        if (str2 != null) {
            str = str2;
        }
        str = JAPAN_LOCALE;
        return str;
    }

    private int getUid(File file) {
        if (file.exists()) {
            return FileUtils.getUid(file.getPath());
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateEnabledBrowser() {
        int i = getDchaCompletedPast() ? 2 : getDchaState() == 0 ? 0 : getAdbEnabled() != 0 ? 0 : !JAPAN_LOCALE.equals(getLanguage()) ? 0 : 2;
        PackageManager packageManager = this.mContext.getPackageManager();
        int applicationEnabledSetting = packageManager.getApplicationEnabledSetting(BrowserContract.AUTHORITY);
        int applicationEnabledSetting2 = packageManager.getApplicationEnabledSetting("com.android.quicksearchbox");
        if (i != applicationEnabledSetting) {
            packageManager.setApplicationEnabledSetting(BrowserContract.AUTHORITY, i, 0);
        }
        if (i != applicationEnabledSetting2) {
            packageManager.setApplicationEnabledSetting("com.android.quicksearchbox", i, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUsbFunction() {
        boolean z = true;
        String str = UsbManager.USB_FUNCTION_PTP;
        String str2 = SystemProperties.get(UsbManager.ADB_PERSISTENT_PROPERTY, "none");
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "adb_enabled", 0) != 1) {
            z = false;
        }
        if (z) {
            str = UsbManager.addFunction(UsbManager.USB_FUNCTION_PTP, UsbManager.USB_FUNCTION_ADB);
        }
        if (str.equals(str2)) {
            return;
        }
        SystemProperties.set(UsbManager.ADB_PERSISTENT_PROPERTY, str);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
        	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
        	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    public boolean checkPassword(java.lang.String r7) {
        /*
            Method dump skipped, instructions count: 388
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BenesseExtensionService.checkPassword(java.lang.String):boolean");
    }

    public boolean checkUsbCam() {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            return new File("/dev/video0").exists();
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public int getDchaState() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "dcha_state", 0);
    }

    public String getString(String str) {
        if (str.equals("bc:mac_address")) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                return ((WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
        return null;
    }

    public void setDchaState(int i) {
        Settings.System.putInt(this.mContext.getContentResolver(), "dcha_state", i);
    }
}
