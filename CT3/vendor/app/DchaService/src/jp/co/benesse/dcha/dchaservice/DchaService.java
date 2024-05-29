package jp.co.benesse.dcha.dchaservice;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.view.textservice.TextServicesManager;
import com.android.internal.app.LocalePicker;
import com.android.internal.widget.LockPatternUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.dchaservice.util.Log;
/* loaded from: d.zip:jp/co/benesse/dcha/dchaservice/DchaService.class */
public class DchaService extends Service {
    private static boolean doCancelDigichalizedFlg = false;
    private static Signature[] sSystemSignature;
    protected ListPreference mAnimatorDurationScale;
    protected IDchaService.Stub mDchaServiceStub = new IDchaService.Stub(this) { // from class: jp.co.benesse.dcha.dchaservice.DchaService.1
        final DchaService this$0;

        /* renamed from: jp.co.benesse.dcha.dchaservice.DchaService$1$PackageDeleteObserver */
        /* loaded from: d.zip:jp/co/benesse/dcha/dchaservice/DchaService$1$PackageDeleteObserver.class */
        class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
            boolean finished;
            boolean result;
            final AnonymousClass1 this$1;

            PackageDeleteObserver(AnonymousClass1 anonymousClass1) {
                this.this$1 = anonymousClass1;
            }

            public void packageDeleted(String str, int i) {
                boolean z = true;
                Log.d("DchaService", "packageDeleted 0001");
                synchronized (this) {
                    this.finished = true;
                    if (i != 1) {
                        z = false;
                    }
                    this.result = z;
                    notifyAll();
                }
            }
        }

        /* renamed from: jp.co.benesse.dcha.dchaservice.DchaService$1$PackageInstallObserver */
        /* loaded from: d.zip:jp/co/benesse/dcha/dchaservice/DchaService$1$PackageInstallObserver.class */
        class PackageInstallObserver extends IPackageInstallObserver.Stub {
            boolean finished;
            int result;
            final AnonymousClass1 this$1;

            PackageInstallObserver(AnonymousClass1 anonymousClass1) {
                this.this$1 = anonymousClass1;
            }

            public void packageInstalled(String str, int i) {
                Log.d("DchaService", "packageInstalled 0001");
                synchronized (this) {
                    this.finished = true;
                    this.result = i;
                    notifyAll();
                }
            }
        }

        {
            this.this$0 = this;
        }

        private String installFailureToString(int i) {
            Field[] fields;
            Log.d("DchaService", "installFailureToString 0001");
            for (Field field : PackageManager.class.getFields()) {
                if (field.getType() == Integer.TYPE) {
                    Log.d("DchaService", "installFailureToString 0002");
                    int modifiers = field.getModifiers();
                    if ((modifiers & 16) != 0 && (modifiers & 1) != 0 && (modifiers & 8) != 0) {
                        Log.d("DchaService", "installFailureToString 0003");
                        String name = field.getName();
                        if (name.startsWith("INSTALL_FAILED_") || name.startsWith("INSTALL_PARSE_FAILED_")) {
                            Log.d("DchaService", "installFailureToString 0004");
                            try {
                                if (i == field.getInt(null)) {
                                    Log.d("DchaService", "installFailureToString 0005");
                                    return name;
                                }
                                continue;
                            } catch (IllegalAccessException e) {
                                Log.e("DchaService", "installFailureToString 0006", e);
                            }
                        }
                    }
                }
            }
            Log.d("DchaService", "installFailureToString 0007");
            return Integer.toString(i);
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void cancelSetup() throws RemoteException {
            Log.d("DchaService", "cancelSetup 0001");
            this.this$0.doCancelDigichalized();
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean checkPadRooted() throws RemoteException {
            Log.d("DchaService", "checkPadRooted 0001");
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void clearDefaultPreferredApp(String str) throws RemoteException {
            Log.d("DchaService", "clearDefaultPreferredApp 0001");
            try {
                this.this$0.getPackageManager().clearPackagePreferredActivities(str);
            } catch (Exception e) {
                Log.e("DchaService", "clearDefaultPreferredApp 0002", e);
                throw new RemoteException(e.toString());
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean copyFile(String str, String str2) throws RemoteException {
            Log.d("DchaService", "copyFile 0001");
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean copyUpdateImage(String str, String str2) {
            Log.d("DchaService", "copyUpdateImage 0001");
            if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
                Log.d("DchaService", "copyUpdateImage 0009");
                return false;
            } else if (!str2.startsWith("/cache")) {
                Log.d("DchaService", "copyUpdateImage 0010");
                return false;
            } else {
                FileChannel fileChannel = null;
                FileChannel fileChannel2 = null;
                FileChannel fileChannel3 = null;
                FileChannel fileChannel4 = null;
                FileChannel fileChannel5 = null;
                FileChannel fileChannel6 = null;
                FileChannel fileChannel7 = null;
                FileChannel fileChannel8 = null;
                try {
                    try {
                        try {
                            try {
                                File file = new File(str);
                                File file2 = new File(str2);
                                FileChannel channel = new FileInputStream(file).getChannel();
                                FileChannel channel2 = new FileOutputStream(file2).getChannel();
                                fileChannel = channel;
                                fileChannel2 = channel2;
                                fileChannel3 = channel;
                                fileChannel4 = channel2;
                                fileChannel5 = channel;
                                fileChannel6 = channel2;
                                fileChannel7 = channel;
                                fileChannel8 = channel2;
                                channel.transferTo(0L, channel.size(), channel2);
                                Log.d("DchaService", "copyUpdateImage 0005");
                                if (channel != null) {
                                    Log.d("DchaService", "copyUpdateImage 0006");
                                    try {
                                        channel.close();
                                    } catch (IOException e) {
                                    }
                                }
                                if (channel2 != null) {
                                    Log.d("DchaService", "copyUpdateImage 0007");
                                    try {
                                        channel2.close();
                                    } catch (IOException e2) {
                                    }
                                }
                                Log.d("DchaService", "copyUpdateImage 0008");
                                return true;
                            } catch (Throwable th) {
                                Log.d("DchaService", "copyUpdateImage 0005");
                                if (fileChannel7 != null) {
                                    Log.d("DchaService", "copyUpdateImage 0006");
                                    try {
                                        fileChannel7.close();
                                    } catch (IOException e3) {
                                    }
                                }
                                if (fileChannel8 != null) {
                                    Log.d("DchaService", "copyUpdateImage 0007");
                                    try {
                                        fileChannel8.close();
                                    } catch (IOException e4) {
                                    }
                                }
                                Log.d("DchaService", "copyUpdateImage 0008");
                                return false;
                            }
                        } catch (FileNotFoundException e5) {
                            Log.e("DchaService", "copyUpdateImage 0002", e5);
                            Log.d("DchaService", "copyUpdateImage 0005");
                            if (fileChannel5 != null) {
                                Log.d("DchaService", "copyUpdateImage 0006");
                                try {
                                    fileChannel5.close();
                                } catch (IOException e6) {
                                }
                            }
                            if (fileChannel6 != null) {
                                Log.d("DchaService", "copyUpdateImage 0007");
                                try {
                                    fileChannel6.close();
                                } catch (IOException e7) {
                                }
                            }
                            Log.d("DchaService", "copyUpdateImage 0008");
                            return false;
                        }
                    } catch (IOException e8) {
                        Log.e("DchaService", "copyUpdateImage 0003", e8);
                        Log.d("DchaService", "copyUpdateImage 0005");
                        if (fileChannel3 != null) {
                            Log.d("DchaService", "copyUpdateImage 0006");
                            try {
                                fileChannel3.close();
                            } catch (IOException e9) {
                            }
                        }
                        if (fileChannel4 != null) {
                            Log.d("DchaService", "copyUpdateImage 0007");
                            try {
                                fileChannel4.close();
                            } catch (IOException e10) {
                            }
                        }
                        Log.d("DchaService", "copyUpdateImage 0008");
                        return false;
                    }
                } catch (Exception e11) {
                    Log.e("DchaService", "copyUpdateImage 0004", e11);
                    Log.d("DchaService", "copyUpdateImage 0005");
                    if (fileChannel != null) {
                        Log.d("DchaService", "copyUpdateImage 0006");
                        try {
                            fileChannel.close();
                        } catch (IOException e12) {
                        }
                    }
                    if (fileChannel2 != null) {
                        Log.d("DchaService", "copyUpdateImage 0007");
                        try {
                            fileChannel2.close();
                        } catch (IOException e13) {
                        }
                    }
                    Log.d("DchaService", "copyUpdateImage 0008");
                    return false;
                }
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean deleteFile(String str) throws RemoteException {
            Log.d("DchaService", "deleteFile 0001");
            return false;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void disableADB() {
            Log.d("DchaService", "disableADB 0001");
            Settings.Secure.putInt(this.this$0.getContentResolver(), "adb_enabled", 0);
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public String getCanonicalExternalPath(String str) throws RemoteException {
            Log.d("DchaService", "getCanonicalExternalPath 0001");
            if (TextUtils.isEmpty(str)) {
                Log.d("DchaService", "getCanonicalExternalPath 0002");
                return str;
            }
            try {
                str = new File(str).getCanonicalPath();
            } catch (Exception e) {
                Log.e("DchaService", "getCanonicalExternalPath 0003", e);
            }
            Log.d("DchaService", "getCanonicalExternalPath 0004 return: " + str);
            return str;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public String getForegroundPackageName() throws RemoteException {
            Log.d("DchaService", "getForegroundPackageName 0001");
            try {
                List tasks = ActivityManagerNative.getDefault().getTasks(1, 0);
                String packageName = ((ActivityManager.RunningTaskInfo) tasks.get(0)).baseActivity.getPackageName();
                Log.d("DchaService", "Foreground package name :" + packageName);
                tasks.clear();
                Log.d("DchaService", "getForegroundPackageName 0003");
                return packageName;
            } catch (Exception e) {
                Log.e("DchaService", "getForegroundPackageName 0002", e);
                throw new RemoteException();
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public int getSetupStatus() {
            Log.d("DchaService", "getSetupStatus 0001");
            return PreferenceManager.getDefaultSharedPreferences(this.this$0).getInt("DigichalizedStatus", -1);
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public int getUserCount() {
            Log.d("DchaService", "getUserCount 0001");
            int userCount = ((UserManager) this.this$0.getSystemService("user")).getUserCount();
            Log.d("DchaService", "getUserCount return: " + userCount);
            return userCount;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void hideNavigationBar(boolean z) {
            Log.d("DchaService", "hideNavigationBar 0001");
            this.this$0.hideNavigationBar(z);
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean installApp(String str, int i) throws RemoteException {
            int i2;
            Log.d("DchaService", "installApp 0001");
            try {
                PackageManager packageManager = this.this$0.getPackageManager();
                switch (i) {
                    case 0:
                        Log.d("DchaService", "installApp 0003");
                        i2 = 64;
                        break;
                    case 1:
                        Log.d("DchaService", "installApp 0004");
                        i2 = 66;
                        break;
                    case 2:
                        Log.d("DchaService", "installApp 0005");
                        i2 = 66 | 128;
                        break;
                    default:
                        Log.d("DchaService", "installApp 0006");
                        i2 = 64;
                        break;
                }
                PackageInstallObserver packageInstallObserver = new PackageInstallObserver(this);
                packageManager.installPackage(Uri.fromFile(new File(str)), packageInstallObserver, i2, null);
                synchronized (packageInstallObserver) {
                    while (!packageInstallObserver.finished) {
                        try {
                            packageInstallObserver.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (packageInstallObserver.result == 1) {
                        Log.d("DchaService", "installApp 0008");
                        return true;
                    }
                    Log.d("DchaService", "installApp 0007");
                    Log.e("DchaService", "apk install failure:" + installFailureToString(packageInstallObserver.result));
                    Log.d("DchaService", "installApp return: false");
                    return false;
                }
            } catch (Exception e2) {
                Log.e("DchaService", "installApp 0009", e2);
                throw new RemoteException();
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean isDeviceEncryptionEnabled() {
            Log.d("DchaService", "isDeviceEncryptionEnabled 0001");
            boolean equalsIgnoreCase = "encrypted".equalsIgnoreCase(SystemProperties.get("ro.crypto.state", "unsupported"));
            Log.d("DchaService", "isDeviceEncryptionEnabled return: " + equalsIgnoreCase);
            return equalsIgnoreCase;
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void rebootPad(int i, String str) throws RemoteException {
            Log.d("DchaService", "rebootPad 0001");
            try {
                PowerManager powerManager = (PowerManager) this.this$0.getSystemService("power");
                switch (i) {
                    case 0:
                        Log.d("DchaService", "rebootPad 0002");
                        powerManager.reboot(null);
                        break;
                    case 1:
                        Log.d("DchaService", "rebootPad 0003");
                        RecoverySystem.rebootWipeUserData(this.this$0.getBaseContext());
                        break;
                    case 2:
                        Log.d("DchaService", "rebootPad 0004");
                        if (str != null) {
                            RecoverySystem.installPackage(this.this$0.getBaseContext(), new File(str));
                            break;
                        }
                        break;
                    default:
                        Log.d("DchaService", "rebootPad 0005");
                        break;
                }
                Log.d("DchaService", "rebootPad 0007");
            } catch (Exception e) {
                Log.e("DchaService", "rebootPad 0006", e);
                throw new RemoteException();
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void removeTask(String str) throws RemoteException {
            Log.d("DchaService", "removeTask 0001");
            try {
                ActivityManager activityManager = (ActivityManager) this.this$0.getSystemService("activity");
                List<ActivityManager.RecentTaskInfo> recentTasksForUser = activityManager.getRecentTasksForUser(30, 1, UserHandle.myUserId());
                if (str != null) {
                    Log.d("DchaService", "removeTask 0002");
                    for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTasksForUser) {
                        Log.d("DchaService", "removeTask " + recentTaskInfo.baseIntent.getComponent().getPackageName());
                        if (str.equals(recentTaskInfo.baseIntent.getComponent().getPackageName())) {
                            Log.d("DchaService", "removeTask 0003");
                            activityManager.removeTask(recentTaskInfo.persistentId);
                        }
                    }
                } else {
                    Log.d("DchaService", "removeTask 0004");
                    for (ActivityManager.RecentTaskInfo recentTaskInfo2 : recentTasksForUser) {
                        activityManager.removeTask(recentTaskInfo2.persistentId);
                    }
                }
                Log.d("DchaService", "removeTask 0006");
            } catch (Exception e) {
                Log.e("DchaService", "removeTask 0005", e);
                throw new RemoteException();
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void sdUnmount() throws RemoteException {
            Log.d("DchaService", "sdUnmount 0001");
            String str = System.getenv("SECONDARY_STORAGE");
            try {
                IMountService.Stub.asInterface(ServiceManager.getService("mount")).unmountVolume(getCanonicalExternalPath(str), true, false);
                Log.d("DchaService", "sdUnmount 0003");
            } catch (RemoteException e) {
                Log.e("DchaService", "sdUnmount 0002", e);
                throw new RemoteException();
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setDefaultParam() throws RemoteException {
            Log.d("DchaService", "setDefaultParam 0001");
            try {
                this.this$0.setInitialSettingsWirelessNetwork();
                this.this$0.setInitialSettingsTerminal();
                this.this$0.setInitialSettingsUser();
                this.this$0.setInitialSettingsAccount();
                this.this$0.setInitialSettingsSystem();
                this.this$0.setInitialSettingsDevelopmentOptions();
                Log.d("DchaService", "setDefaultParam 0003");
            } catch (RemoteException e) {
                Log.e("DchaService", "setDefaultParam 0002", e);
                throw new RemoteException();
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setDefaultPreferredHomeApp(String str) throws RemoteException {
            try {
                Log.d("DchaService", "setDefalutPreferredHomeApp 0001");
                Log.d("DchaService", "setDefalutPreferredHomeApp packageName:" + str);
                PackageManager packageManager = this.this$0.getPackageManager();
                ComponentName componentName = null;
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.MAIN");
                intentFilter.addCategory("android.intent.category.HOME");
                intentFilter.addCategory("android.intent.category.DEFAULT");
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                intent.addCategory("android.intent.category.DEFAULT");
                List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
                ArrayList arrayList = new ArrayList();
                for (ResolveInfo resolveInfo : queryIntentActivities) {
                    String str2 = resolveInfo.activityInfo.applicationInfo.packageName;
                    String str3 = resolveInfo.activityInfo.name;
                    Log.d("DchaService", "setDefalutPreferredHomeApp packName:" + str2);
                    Log.d("DchaService", "setDefalutPreferredHomeApp activityName:" + str3);
                    ComponentName componentName2 = new ComponentName(str2, str3);
                    arrayList.add(componentName2);
                    if (str2.equalsIgnoreCase(str)) {
                        Log.d("DchaService", "setDefalutPreferredHomeApp 0002");
                        componentName = componentName2;
                        Log.d("DchaService", "setDefalutPreferredHomeApp defaultHomeComponentName:" + componentName2);
                    }
                }
                ComponentName[] componentNameArr = (ComponentName[]) arrayList.toArray(new ComponentName[arrayList.size()]);
                if (componentName != null) {
                    Log.d("DchaService", "setDefalutPreferredHomeApp 0003");
                    packageManager.addPreferredActivityAsUser(intentFilter, 1081344, componentNameArr, componentName, 0);
                }
                Log.d("DchaService", "setDefalutPreferredHomeApp 0005");
            } catch (Exception e) {
                Log.e("DchaService", "setDefalutPrefferredHomeApp 0004", e);
                throw new RemoteException(e.toString());
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setPermissionEnforced(boolean z) throws RemoteException {
            Log.d("DchaService", "setPermissionEnforced 0001");
            ActivityThread.getPackageManager().setPermissionEnforced("android.permission.READ_EXTERNAL_STORAGE", z);
            Log.d("DchaService", "setPermissionEnforced 0002");
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setSetupStatus(int i) {
            Log.d("DchaService", "setSetupStatus 0001");
            PreferenceManager.getDefaultSharedPreferences(this.this$0).edit().putInt("DigichalizedStatus", i).commit();
            Settings.System.putInt(this.this$0.getContentResolver(), "dcha_state", i);
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public void setSystemTime(String str, String str2) {
            Log.d("DchaService", "setSystemTime 0001");
            try {
                Date parse = new SimpleDateFormat(str2, Locale.JAPAN).parse(str);
                String packageNameFromPid = this.this$0.getPackageNameFromPid(Binder.getCallingPid());
                Calendar calendar = Calendar.getInstance(Locale.JAPAN);
                calendar.set(2016, 1, 1, 0, 0);
                Calendar calendar2 = Calendar.getInstance(Locale.JAPAN);
                calendar2.setTime(parse);
                if (calendar.compareTo(calendar2) > 0) {
                    Log.d("DchaService", "setSystemTime 0002");
                    EmergencyLog.write(this.this$0, "ELK008", str + " " + packageNameFromPid);
                } else {
                    Log.d("DchaService", "setSystemTime 0003");
                    SystemClock.setCurrentTimeMillis(parse.getTime());
                }
                Log.d("DchaService", "setSystemTime set time :" + parse);
            } catch (Exception e) {
                Log.e("DchaService", "setSystemTime 0004", e);
            }
            Log.d("DchaService", "setSystemTime 0005");
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean uninstallApp(String str, int i) throws RuntimeException {
            int i2;
            Log.d("DchaService", "uninstallApp 0001");
            try {
                EmergencyLog.write(this.this$0, "ELK002", str + " " + this.this$0.getPackageNameFromPid(getCallingPid()));
                PackageManager packageManager = this.this$0.getPackageManager();
                switch (i) {
                    case 1:
                        Log.d("DchaService", "uninstallApp 0002");
                        i2 = 3;
                        break;
                    default:
                        i2 = 2;
                        break;
                }
                PackageDeleteObserver packageDeleteObserver = new PackageDeleteObserver(this);
                packageManager.deletePackage(str, packageDeleteObserver, i2);
                synchronized (packageDeleteObserver) {
                    while (!packageDeleteObserver.finished) {
                        try {
                            packageDeleteObserver.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                Log.d("DchaService", "uninstallApp 0003");
                return packageDeleteObserver.result;
            } catch (Exception e2) {
                Log.e("DchaService", "uninstallApp 0004", e2);
                throw new RuntimeException();
            }
        }

        @Override // jp.co.benesse.dcha.dchaservice.IDchaService
        public boolean verifyUpdateImage(String str) {
            Log.d("DchaService", "verifyUpdateImage 0001");
            boolean z = false;
            try {
                RecoverySystem.verifyPackage(new File(str), null, null);
                z = true;
            } catch (IOException e) {
                Log.e("DchaService", "verifyUpdateImege IO Exception", e);
            } catch (GeneralSecurityException e2) {
                Log.e("DchaService", "verifyUpdateImege GeneralSecurityException", e2);
            } catch (Exception e3) {
                Log.e("DchaService", "verifyUpdateImege Exception", e3);
            }
            Log.d("DchaService", "verifyUpdateImage 0002");
            return z;
        }
    };
    protected String mDebugApp;
    protected boolean mDontPokeProperties;
    protected boolean mLastEnabledState;
    protected ListPreference mTransitionAnimationScale;
    protected ListPreference mWindowAnimationScale;
    protected IWindowManager mWindowManager;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: d.zip:jp/co/benesse/dcha/dchaservice/DchaService$SystemPropPoker.class */
    public static class SystemPropPoker extends AsyncTask<Void, Void, Void> {
        private final String TAG = "SystemPropPoker";

        SystemPropPoker() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(Void... voidArr) {
            String[] listServices;
            Log.d("SystemPropPoker", "doInBackground 0001");
            for (String str : ServiceManager.listServices()) {
                IBinder checkService = ServiceManager.checkService(str);
                if (checkService != null) {
                    Log.d("SystemPropPoker", "doInBackground 0003");
                    Parcel obtain = Parcel.obtain();
                    try {
                        checkService.transact(1599295570, obtain, null, 0);
                    } catch (RemoteException e) {
                        Log.e("SystemPropPoker", "doInBackground 0004", e);
                    } catch (Exception e2) {
                        Log.v("DevSettings", "Somone wrote a bad service '" + str + "' that doesn't like to be poked: " + e2);
                        Log.e("SystemPropPoker", "doInBackground 0005", e2);
                    }
                    obtain.recycle();
                }
            }
            Log.d("SystemPropPoker", "doInBackground 0006");
            return null;
        }
    }

    private static Signature getFirstSignature(PackageInfo packageInfo) {
        if (packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length <= 0) {
            return null;
        }
        return packageInfo.signatures[0];
    }

    private int getNewPriorityCategories(NotificationManager.Policy policy, boolean z, int i) {
        int i2 = policy.priorityCategories;
        return z ? i2 | i : i2 & (i ^ (-1));
    }

    private static Signature getSystemSignature(PackageManager packageManager) {
        try {
            return getFirstSignature(packageManager.getPackageInfo("android", 64));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    protected static boolean isSystemPackage(PackageManager packageManager, PackageInfo packageInfo) {
        boolean z = false;
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(packageManager)};
        }
        if (sSystemSignature[0] != null) {
            z = sSystemSignature[0].equals(getFirstSignature(packageInfo));
        }
        return z;
    }

    protected static void resetDebuggerOptions() {
        Log.d("DchaService", "resetDebuggerOptions 0001");
        try {
            ActivityManagerNative.getDefault().setDebugApp((String) null, false, true);
        } catch (RemoteException e) {
        }
    }

    private NotificationManager.Policy savePolicy(NotificationManager.Policy policy, int i, int i2, int i3, int i4) {
        NotificationManager.Policy policy2 = new NotificationManager.Policy(i, i2, i3, i4);
        NotificationManager.from(getApplicationContext()).setNotificationPolicy(policy2);
        return policy2;
    }

    public void doCancelDigichalized() throws RemoteException {
        try {
            Log.d("DchaService", "doCancelDigichalized 0001");
            int setupStatus = this.mDchaServiceStub.getSetupStatus();
            Log.d("DchaService", "status:" + Integer.toString(setupStatus));
            String packageNameFromPid = getPackageNameFromPid(Binder.getCallingPid());
            EmergencyLog.write(this, "ELK000", setupStatus + " " + isFinishDigichalize() + " " + packageNameFromPid);
            if (setupStatus == 3 || isFinishDigichalize()) {
                Log.d("DchaService", "doCancelDigichalized 0008");
                EmergencyLog.write(this, "ELK005", setupStatus + " " + isFinishDigichalize() + " " + packageNameFromPid);
            } else {
                Log.d("DchaService", "doCancelDigichalized 0002");
                EmergencyLog.write(this, "ELK004", setupStatus + " " + isFinishDigichalize() + " " + packageNameFromPid);
                Intent intent = new Intent();
                intent.setAction("jp.co.benesse.dcha.databox.intent.action.COMMAND");
                intent.addCategory("jp.co.benesse.dcha.databox.intent.category.WIPE");
                intent.putExtra("send_service", "DchaService");
                sendBroadcastAsUser(intent, UserHandle.ALL);
                Log.d("DchaService", "doCancelDigichalized send wipeDataBoxIntent intent");
                HandlerThread handlerThread = new HandlerThread("handlerThread");
                handlerThread.start();
                new Handler(handlerThread.getLooper()).post(new Runnable(this) { // from class: jp.co.benesse.dcha.dchaservice.DchaService.2
                    final DchaService this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (!DchaService.doCancelDigichalizedFlg) {
                            Log.d("DchaService", "doCancelDigichalized 0003");
                            try {
                                try {
                                    Log.d("DchaService", "start uninstallApp");
                                    boolean unused = DchaService.doCancelDigichalizedFlg = true;
                                    for (ApplicationInfo applicationInfo : this.this$0.getPackageManager().getInstalledApplications(128)) {
                                        if ((applicationInfo.flags & 1) == 1) {
                                            Log.d("DchaService", "doCancelDigichalized 0004");
                                        } else {
                                            this.this$0.mDchaServiceStub.uninstallApp(applicationInfo.packageName, 0);
                                        }
                                    }
                                    this.this$0.mDchaServiceStub.setSetupStatus(0);
                                    Log.d("DchaService", "end uninstallApp");
                                    Log.d("DchaService", "doCancelDigichalized 0006");
                                    boolean unused2 = DchaService.doCancelDigichalizedFlg = false;
                                } catch (RemoteException e) {
                                    Log.e("DchaService", "doCancelDigichalized 0005", e);
                                    Log.d("DchaService", "doCancelDigichalized 0006");
                                    boolean unused3 = DchaService.doCancelDigichalizedFlg = false;
                                }
                            } catch (Throwable th) {
                                Log.d("DchaService", "doCancelDigichalized 0006");
                                boolean unused4 = DchaService.doCancelDigichalizedFlg = false;
                                throw th;
                            }
                        }
                        Log.d("DchaService", "doCancelDigichalized 0007");
                    }
                });
            }
            Log.d("DchaService", "doCancelDigichalized 0010");
        } catch (Exception e) {
            Log.e("DchaService", "doCancelDigichalized 0009", e);
            throw new RemoteException();
        }
    }

    protected String getPackageNameFromPid(int i) {
        Log.d("DchaService", "getPackageNameFromPid 0001");
        String str = "Unknown";
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager) getSystemService("activity")).getRunningAppProcesses()) {
            if (i == runningAppProcessInfo.pid) {
                Log.d("DchaService", "getPackageNameFromPid 0002");
                str = runningAppProcessInfo.processName;
            }
        }
        Log.d("DchaService", "getPackageNameFromPid 0003");
        return str;
    }

    public void hideNavigationBar(boolean z) {
        Log.d("DchaService", "hideNavigationBar 0001");
        Settings.System.putInt(getContentResolver(), "hide_navigation_bar", z ? 1 : 0);
    }

    protected boolean isFinishDigichalize() {
        Log.d("DchaService", "isFinishDigichalize 0001");
        return UpdateLog.exists();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.d("DchaService", "onBind 0001");
        return this.mDchaServiceStub;
    }

    @Override // android.app.Service
    public void onCreate() {
        Log.d("DchaService", "onCreate 0001");
        super.onCreate();
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d("DchaService", "onDestroy 0001");
        super.onDestroy();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.d("DchaService", "onStartCommand 0001");
        if (intent != null) {
            Log.d("DchaService", "onStartCommand 0002");
            int intExtra = intent.getIntExtra("REQ_COMMAND", 0);
            Log.d("DchaService", "onStartCommand intent command:" + intExtra);
            try {
                switch (intExtra) {
                    case 1:
                        Log.d("DchaService", "onStartCommand 0003");
                        hideNavigationBar(false);
                        doCancelDigichalized();
                        break;
                    case 2:
                        Log.d("DchaService", "onStartCommand 0004");
                        hideNavigationBar(false);
                        break;
                    case 3:
                        Log.d("DchaService", "onStartCommand 0005");
                        hideNavigationBar(true);
                        break;
                    default:
                        Log.d("DchaService", "onStartCommand 0006");
                        break;
                }
            } catch (Exception e) {
                Log.e("DchaService", "onStartCommand 0007", e);
            }
        }
        Log.d("DchaService", "onStartCommand 0008");
        return super.onStartCommand(intent, i, i2);
    }

    void pokeSystemProperties() {
        Log.d("DchaService", "pokeSystemProperties 0001");
        if (this.mDontPokeProperties) {
            return;
        }
        Log.d("DchaService", "pokeSystemProperties 0002");
        new SystemPropPoker().execute(new Void[0]);
    }

    protected void setInitialSettingsAccount() {
        Log.d("DchaService", "setInitialSettingsAccount start");
        LocalePicker.updateLocale(new Locale("ja", "JP"));
        ((TextServicesManager) getSystemService("textservices")).setSpellCheckerEnabled(true);
        Log.d("DchaService", "setInitialSettingsAccount 0002");
    }

    protected void setInitialSettingsDevelopmentOptions() throws RemoteException {
        Log.d("DchaService", "setInitialSettingDevelopmentOptions start");
        this.mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mDontPokeProperties = true;
        resetDebuggerOptions();
        Settings.Global.putInt(getContentResolver(), "development_settings_enabled", 0);
        Settings.Global.putInt(getContentResolver(), "stay_on_while_plugged_in", 0);
        Settings.Secure.putInt(getContentResolver(), "bluetooth_hci_log", 0);
        Settings.Global.putInt(getContentResolver(), "adb_enabled", 0);
        Settings.Secure.putInt(getContentResolver(), "bugreport_in_power_menu", 0);
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService("appops");
        List<AppOpsManager.PackageOps> packagesForOps = appOpsManager.getPackagesForOps(new int[]{58});
        if (packagesForOps != null) {
            for (AppOpsManager.PackageOps packageOps : packagesForOps) {
                if (((AppOpsManager.OpEntry) packageOps.getOps().get(0)).getMode() != 2) {
                    String packageName = packageOps.getPackageName();
                    try {
                        appOpsManager.setMode(58, getPackageManager().getApplicationInfo(packageName, 512).uid, packageName, 2);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }
            }
        }
        Settings.Global.putInt(getContentResolver(), "debug_view_attributes", 0);
        Settings.Global.putString(getContentResolver(), "debug_app", "");
        Settings.Global.putInt(getContentResolver(), "wait_for_debugger", 0);
        Settings.Global.putInt(getContentResolver(), "verifier_verify_adb_installs", 0);
        Settings.System.putInt(getContentResolver(), "screen_capture_on", 0);
        SystemProperties.set("persist.logd.size", "256K");
        Settings.System.putInt(getContentResolver(), "show_touches", 0);
        Settings.System.putInt(getContentResolver(), "pointer_location", 0);
        writeShowUpdatesOption();
        writeDebugLayoutOptions();
        writeDebuggerOptions();
        Settings.Global.putInt(getContentResolver(), "debug.force_rtl", 0);
        SystemProperties.set("debug.force_rtl", "0");
        writeAnimationScaleOption(0, this.mWindowAnimationScale, null);
        writeAnimationScaleOption(1, this.mTransitionAnimationScale, null);
        writeAnimationScaleOption(2, this.mAnimatorDurationScale, null);
        writeOverlayDisplayDevicesOptions(null);
        writeHardwareUiOptions();
        writeShowHwScreenUpdatesOptions();
        writeShowHwLayersUpdatesOptions();
        writeShowHwOverdrawOptions();
        SystemProperties.set("debug.hwui.show_non_rect_clip", "0");
        writeMsaaOptions();
        writeDisableOverlaysOption();
        Settings.Secure.putInt(getContentResolver(), "accessibility_display_daltonizer_enabled", 0);
        SystemProperties.set("persist.sys.media.use-awesome", "false");
        Settings.Secure.putInt(getContentResolver(), "usb_audio_automatic_routing_disabled", 0);
        writeStrictModeVisualOptions();
        writeCpuUsageOptions();
        writeTrackFrameTimeOptions();
        SystemProperties.set("debug.egl.trace", "");
        writeImmediatelyDestroyActivitiesOptions();
        writeAppProcessLimitOptions(null);
        Settings.Secure.putInt(getContentResolver(), "anr_show_background", 0);
        this.mDontPokeProperties = false;
        pokeSystemProperties();
        Settings.Global.putInt(getContentResolver(), "development_settings_enabled", 0);
        this.mLastEnabledState = Settings.Global.getInt(getContentResolver(), "development_settings_enabled", 0) != 0;
        if (this.mLastEnabledState) {
            Log.d("DchaService", "setInitialSettingDevelopmentOptions 0002");
            writeShowUpdatesOption();
            Settings.Global.putInt(getContentResolver(), "development_settings_enabled", 0);
        }
        Log.d("DchaService", "setInitialSettingDevelopmentOptions 0003");
    }

    protected void setInitialSettingsSystem() {
        Log.d("DchaService", "setInitialSettingsSystem start");
        Settings.Global.putInt(getContentResolver(), "auto_time", 1);
        ((AlarmManager) getSystemService("alarm")).setTimeZone("Asia/Tokyo");
        Settings.System.putString(getContentResolver(), "time_12_24", "12");
        Settings.System.putString(getContentResolver(), "date_format", "");
        Settings.Secure.putInt(getContentResolver(), "accessibility_captioning_enabled", 0);
        Settings.Secure.putInt(getContentResolver(), "accessibility_display_magnification_enabled", 0);
        Settings.Secure.putInt(getContentResolver(), "high_text_contrast_enabled", 0);
        Settings.Secure.putInt(getContentResolver(), "speak_password", 0);
        Settings.Global.putInt(getContentResolver(), "enable_accessibility_global_gesture_enabled", 0);
        Settings.Secure.putString(getContentResolver(), "long_press_timeout", "500");
        Settings.Secure.putInt(getContentResolver(), "accessibility_display_inversion_enabled", 0);
        Settings.Secure.putInt(getContentResolver(), "accessibility_display_daltonizer_enabled", 0);
        Log.d("DchaService", "setInitialSettingsSystem 0003");
    }

    protected void setInitialSettingsTerminal() throws RemoteException {
        Log.d("DchaService", "setInitialSettingsTerminal start");
        try {
            Settings.System.putInt(getContentResolver(), "screen_brightness_mode", 0);
            Settings.System.putLong(getContentResolver(), "screen_off_timeout", 900000L);
            Settings.System.putInt(getContentResolver(), "screen_dim_timeout", 300000);
            Settings.Secure.putInt(getContentResolver(), "screensaver_enabled", 0);
            Configuration configuration = new Configuration();
            configuration.fontScale = Float.parseFloat("1.0");
            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
            Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 0);
            INotificationManager asInterface = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            Context applicationContext = getApplicationContext();
            NotificationManager.Policy notificationPolicy = NotificationManager.from(applicationContext).getNotificationPolicy();
            NotificationManager.Policy savePolicy = savePolicy(notificationPolicy, getNewPriorityCategories(notificationPolicy, false, 1), notificationPolicy.priorityCallSenders, notificationPolicy.priorityMessageSenders, notificationPolicy.suppressedVisualEffects);
            NotificationManager.Policy savePolicy2 = savePolicy(savePolicy, getNewPriorityCategories(savePolicy, false, 2), savePolicy.priorityCallSenders, savePolicy.priorityMessageSenders, savePolicy.suppressedVisualEffects);
            NotificationManager.Policy savePolicy3 = savePolicy(savePolicy2, getNewPriorityCategories(savePolicy2, false, 4), savePolicy2.priorityCallSenders, savePolicy2.priorityMessageSenders, savePolicy2.suppressedVisualEffects);
            savePolicy(savePolicy3, getNewPriorityCategories(savePolicy3, false, 8), savePolicy3.priorityCallSenders, savePolicy3.priorityMessageSenders, savePolicy3.suppressedVisualEffects);
            if (this.mDchaServiceStub.getSetupStatus() != 3) {
                AppOpsManager appOpsManager = (AppOpsManager) applicationContext.getSystemService("appops");
                try {
                    IPackageManager.Stub.asInterface(ServiceManager.getService("package")).resetApplicationPreferences(UserHandle.myUserId());
                } catch (RemoteException e) {
                }
                appOpsManager.resetAllModes();
            }
            PackageManager packageManager = getPackageManager();
            List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(512);
            for (int i = 0; i < installedApplications.size(); i++) {
                ApplicationInfo applicationInfo = installedApplications.get(i);
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 64);
                    if (!isSystemPackage(packageManager, packageInfo)) {
                        Log.d("DchaService", "setInitialSettingsTerminal 0002:" + applicationInfo.packageName);
                        asInterface.setNotificationsEnabledForPackage(applicationInfo.packageName, applicationInfo.uid, false);
                        asInterface.setImportance(applicationInfo.packageName, packageInfo.applicationInfo.uid, -1000);
                    }
                } catch (Exception e2) {
                }
                if (!applicationInfo.enabled) {
                    Log.d("DchaService", "setInitialSettingsTerminal 0003");
                    if (packageManager.getApplicationEnabledSetting(applicationInfo.packageName) == 3) {
                        Log.d("DchaService", "setInitialSettingsTerminal 0004:" + applicationInfo.packageName);
                        packageManager.setApplicationEnabledSetting(applicationInfo.packageName, 0, 1);
                    }
                }
                try {
                    PackageInfo packageInfo2 = packageManager.getPackageInfo(applicationInfo.packageName, 4096);
                    if (!isSystemPackage(packageManager, packageInfo2)) {
                        int length = packageInfo2.requestedPermissions.length;
                        for (int i2 = 0; i2 < length; i2++) {
                            String str = packageInfo2.requestedPermissions[i2];
                            if ((applicationContext.getPackageManager().getPermissionFlags(str, packageInfo2.packageName, Process.myUserHandle()) & 16) == 0 && "android.permission.READ_EXTERNAL_STORAGE".equals(str)) {
                                packageManager.grantRuntimePermission(packageInfo2.packageName, str, Process.myUserHandle());
                            }
                        }
                    }
                } catch (Exception e3) {
                }
            }
            AppOpsManager appOpsManager2 = (AppOpsManager) applicationContext.getSystemService("appops");
            for (ApplicationInfo applicationInfo2 : packageManager.getInstalledApplications(128)) {
                packageManager.updateIntentVerificationStatusAsUser(applicationInfo2.packageName, 4, UserHandle.myUserId());
                appOpsManager2.setMode(24, applicationInfo2.uid, applicationInfo2.packageName, 0);
                appOpsManager2.setMode(23, applicationInfo2.uid, applicationInfo2.packageName, 0);
            }
            NetworkPolicyManager from = NetworkPolicyManager.from(applicationContext);
            int[] uidsWithPolicy = from.getUidsWithPolicy(1);
            int currentUser = ActivityManager.getCurrentUser();
            for (int i3 : uidsWithPolicy) {
                if (UserHandle.getUserId(i3) == currentUser) {
                    from.setUidPolicy(i3, 0);
                }
            }
            RingtoneManager.setActualDefaultRingtoneUri(applicationContext, 2, null);
            RingtoneManager.setActualDefaultRingtoneUri(applicationContext, 1, null);
            RingtoneManager.setActualDefaultRingtoneUri(applicationContext, 4, null);
            ((AudioManager) getSystemService("audio")).loadSoundEffects();
            Settings.System.putInt(getContentResolver(), "sound_effects_enabled", 1);
            ((UsbManager) getSystemService("usb")).setCurrentFunction("ptp");
            ((PowerManager) getSystemService("power")).setPowerSaveMode(false);
            Settings.Global.putInt(getContentResolver(), "low_power_trigger_level", 0);
            Log.d("DchaService", "setInitialSettingsTerminal 0006");
        } catch (RemoteException e4) {
            Log.e("DchaService", "setInitialSettingsTerminal 0005", e4);
            throw e4;
        }
    }

    protected void setInitialSettingsUser() {
        Log.d("DchaService", "setInitialSettingsUser start");
        Settings.Secure.putInt(getContentResolver(), "location_mode", 0);
        LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
        int callingUserId = UserHandle.getCallingUserId();
        lockPatternUtils.clearLock(callingUserId);
        lockPatternUtils.setLockScreenDisabled(true, callingUserId);
        Settings.System.putInt(getContentResolver(), "show_password", 1);
        Settings.Secure.putInt(getContentResolver(), "install_non_market_apps", 0);
        Log.d("DchaService", "setInitialSettingsUser 0002");
    }

    protected void setInitialSettingsWirelessNetwork() throws RemoteException {
        Log.d("DchaService", "setInitialSettingsWirelessNetwork 0001");
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");
        if (wifiManager != null) {
            Log.d("DchaService", "setInitialSettingsWirelessNetwork 0002");
            wifiManager.setWifiEnabled(true);
            wifiManager.enableVerboseLogging(0);
            wifiManager.enableAggressiveHandover(0);
            wifiManager.setAllowScansWithTraffic(0);
        }
        Settings.Secure.putInt(getContentResolver(), "wifi_networks_available_notification_on", 0);
        Settings.Global.putInt(getContentResolver(), "wifi_scan_always_enabled", 0);
        Settings.Global.putInt(getContentResolver(), "wifi_sleep_policy", 0);
        Settings.Global.putInt(getContentResolver(), "wifi_display_on", 0);
        Settings.Global.putInt(getContentResolver(), "wifi_display_certification_on", 0);
        Settings.Global.putInt(getContentResolver(), "bluetooth_on", 0);
        BluetoothAdapter.getDefaultAdapter().disable();
        Settings.Global.putInt(getContentResolver(), "airplane_mode_on", 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", false);
        sendBroadcast(intent);
        Log.d("DchaService", "setInitialSettingsWirelessNetwork 0003");
    }

    protected void writeAnimationScaleOption(int i, ListPreference listPreference, Object obj) {
        float parseFloat;
        Log.d("DchaService", "writeAnimationScaleOption 0001");
        if (obj != null) {
            try {
                parseFloat = Float.parseFloat(obj.toString());
            } catch (RemoteException e) {
                return;
            }
        } else {
            parseFloat = 1.0f;
        }
        this.mWindowManager.setAnimationScale(i, parseFloat);
    }

    protected void writeAppProcessLimitOptions(Object obj) {
        int parseInt;
        Log.d("DchaService", "writeAppProcessLimitOptions 0001");
        if (obj != null) {
            try {
                parseInt = Integer.parseInt(obj.toString());
            } catch (RemoteException e) {
                return;
            }
        } else {
            parseInt = -1;
        }
        ActivityManagerNative.getDefault().setProcessLimit(parseInt);
    }

    protected void writeCpuUsageOptions() {
        Log.d("DchaService", "writeCpuUsageOptions 0001");
        Settings.Global.putInt(getContentResolver(), "show_processes", 0);
        Intent className = new Intent().setClassName("com.android.systemui", "com.android.systemui.LoadAverageService");
        if (0 != 0) {
            Log.d("DchaService", "writeCpuUsageOptions 0002");
            startService(className);
            return;
        }
        Log.d("DchaService", "writeCpuUsageOptions 0003");
        stopService(className);
    }

    protected void writeDebugLayoutOptions() {
        Log.d("DchaService", "writeDebugLayoutOptions 0001");
        SystemProperties.set("debug.layout", "false");
        pokeSystemProperties();
    }

    protected void writeDebuggerOptions() {
        Log.d("DchaService", "writeDebuggerOptions 0001");
        try {
            this.mDebugApp = Settings.Global.getString(getContentResolver(), "debug_app");
            ActivityManagerNative.getDefault().setDebugApp(this.mDebugApp, false, true);
        } catch (RemoteException e) {
        }
    }

    protected void writeDisableOverlaysOption() {
        Log.d("DchaService", "writeDisableOverlaysOption 0001");
        try {
            IBinder service = ServiceManager.getService("SurfaceFlinger");
            if (service != null) {
                Log.d("DchaService", "writeDisableOverlaysOption 0002");
                Parcel obtain = Parcel.obtain();
                obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
                obtain.writeInt(0);
                service.transact(1008, obtain, null, 0);
                obtain.recycle();
            }
        } catch (RemoteException e) {
        }
    }

    protected void writeHardwareUiOptions() {
        Log.d("DchaService", "writeHardwareUiOptions 0001");
        SystemProperties.set("persist.sys.ui.hw", "false");
        pokeSystemProperties();
    }

    protected void writeImmediatelyDestroyActivitiesOptions() {
        Log.d("DchaService", "writeImmediatelyDestroyActivitiesOptions 0001");
        try {
            ActivityManagerNative.getDefault().setAlwaysFinish(false);
        } catch (RemoteException e) {
        }
    }

    protected void writeMsaaOptions() {
        Log.d("DchaService", "writeMsaaOptions 0001");
        SystemProperties.set("debug.egl.force_msaa", "false");
        pokeSystemProperties();
    }

    protected void writeOverlayDisplayDevicesOptions(Object obj) {
        Log.d("DchaService", "writeOverlayDisplayDevicesOptions 0001");
        Settings.Global.putString(getContentResolver(), "overlay_display_devices", (String) obj);
    }

    protected void writeShowHwLayersUpdatesOptions() {
        Log.d("DchaService", "writeShowHwLayersUpdatesOptions 0001");
        SystemProperties.set("debug.hwui.show_layers_updates", (String) null);
        pokeSystemProperties();
    }

    protected void writeShowHwOverdrawOptions() {
        Log.d("DchaService", "writeShowHwOverdrawOptions 0001");
        SystemProperties.set("debug.hwui.overdraw", "0");
        pokeSystemProperties();
    }

    protected void writeShowHwScreenUpdatesOptions() {
        Log.d("DchaService", "writeShowHwScreenUpdatesOptions 0001");
        SystemProperties.set("debug.hwui.show_dirty_regions", (String) null);
        pokeSystemProperties();
    }

    protected void writeShowUpdatesOption() {
        Log.d("DchaService", "writeShowUpdatesOption 0001");
        try {
            IBinder service = ServiceManager.getService("SurfaceFlinger");
            if (service != null) {
                Log.d("DchaService", "writeShowUpdatesOption 0002");
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
                service.transact(1010, obtain, obtain2, 0);
                obtain2.readInt();
                obtain2.readInt();
                int readInt = obtain2.readInt();
                obtain2.readInt();
                obtain2.readInt();
                obtain2.recycle();
                obtain.recycle();
                if (readInt != 0) {
                    Log.d("DchaService", "writeShowUpdatesOption 0003");
                    Parcel obtain3 = Parcel.obtain();
                    obtain3.writeInterfaceToken("android.ui.ISurfaceComposer");
                    obtain3.writeInt(0);
                    service.transact(1002, obtain3, null, 0);
                    obtain3.recycle();
                }
            }
        } catch (RemoteException e) {
        }
    }

    protected void writeStrictModeVisualOptions() {
        Log.d("DchaService", "writeStrictModeVisualOptions 0001");
        try {
            this.mWindowManager.setStrictModeVisualIndicatorPreference("");
        } catch (RemoteException e) {
        }
    }

    protected void writeTrackFrameTimeOptions() {
        Log.d("DchaService", "writeTrackFrameTimeOptions 0001");
        SystemProperties.set("debug.hwui.profile", "false");
        pokeSystemProperties();
    }
}
