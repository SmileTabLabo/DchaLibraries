package com.mediatek.keyguard.VoiceWakeup;

import android.app.ActivityManagerNative;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.mediatek.common.voicecommand.IVoiceCommandListener;
import com.mediatek.common.voicecommand.IVoiceCommandManagerService;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
/* loaded from: a.zip:com/mediatek/keyguard/VoiceWakeup/VoiceWakeupManager.class */
public class VoiceWakeupManager implements KeyguardHostView.OnDismissAction {
    private Handler mHandler;
    private String mLaunchApp;
    private LockPatternUtils mLockPatternUtils;
    private PowerManager mPM;
    private String mPkgName;
    private IVoiceCommandManagerService mVCmdMgrService;
    private ViewMediatorCallback mViewMediatorCallback;
    private KeyguardSecurityModel securityModel;
    private static VoiceWakeupManager sInstance = null;
    private static boolean delayToLightUpScreen = false;
    private Context mContext = null;
    private boolean isRegistered = false;
    private LimitedModeApp[] limitedApps = {new LimitedModeApp(this, "com.android.gallery3d/com.android.camera.CameraLauncher", "android.media.action.STILL_IMAGE_CAMERA_SECURE")};
    private ServiceConnection mVoiceServiceConnection = new ServiceConnection(this) { // from class: com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager.1
        final VoiceWakeupManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            this.this$0.log("onServiceConnected   ");
            this.this$0.mVCmdMgrService = IVoiceCommandManagerService.Stub.asInterface(iBinder);
            this.this$0.registerVoiceCommand(this.this$0.mPkgName);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            this.this$0.log("onServiceDisconnected  ");
            this.this$0.isRegistered = false;
            this.this$0.mVCmdMgrService = null;
        }
    };
    private IVoiceCommandListener mVoiceCallback = new IVoiceCommandListener.Stub(this) { // from class: com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager.2
        final VoiceWakeupManager this$0;

        {
            this.this$0 = this;
        }

        public void onVoiceCommandNotified(int i, int i2, Bundle bundle) throws RemoteException {
            int i3 = bundle.getInt("Result");
            this.this$0.log("onNotified result=" + i3 + " mainAction = " + i + " subAction = " + i2);
            if (i3 == 1 && i == 6 && i2 == 4) {
                Message.obtain(this.this$0.mVoiceCommandHandler, i, i2, 0, bundle).sendToTarget();
            }
        }
    };
    private Handler mVoiceCommandHandler = new Handler(this) { // from class: com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager.3
        final VoiceWakeupManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.this$0.handleVoiceCommandNotified((Bundle) message.obj, false);
        }
    };
    private boolean mIsDismissAndLaunchApp = false;
    private KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager.4
        final VoiceWakeupManager this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFinishedGoingToSleep(int i) {
            this.this$0.log("onFinishedGoingToSleep - we should reset mIsDismissAndLaunchApp when screen is off.");
            this.this$0.mIsDismissAndLaunchApp = false;
            this.this$0.start();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            this.this$0.log("onKeyguardVisibilityChanged(" + z + ")");
            if (VoiceWakeupManager.delayToLightUpScreen && !z) {
                this.this$0.lightUpScreen();
                boolean unused = VoiceWakeupManager.delayToLightUpScreen = false;
                this.this$0.mIsDismissAndLaunchApp = false;
            } else if (this.this$0.mIsDismissAndLaunchApp && this.this$0.mPM.isScreenOn() && !z) {
                this.this$0.log("onKeyguardVisibilityChanged() : Keyguard is hidden now, set mIsDismissAndLaunchApp = false(ex:phone call screen shows)");
                this.this$0.mIsDismissAndLaunchApp = false;
            }
        }
    };
    private final String ACTION_VOICE_WAKEUP_LAUNCH_SECURECAMERA_OWNER_ONLY = "com.android.keyguard.VoiceWakeupManager.LAUNCH_SEC_CAMERA_OWNER";
    private final String ACTION_VOICE_WAKEUP_LAUNCH_INSECURECAMERA_OWNER_ONLY = "com.android.keyguard.VoiceWakeupManager.LAUNCH_INSEC_CAMERA_OWNER";
    private final String ACTION_VOICE_WAKEUP_LAUNCH_SECURECAMERA_ANYONE = "com.android.keyguard.VoiceWakeupManager.LAUNCH_SEC_CAMERA_ANYONE";
    private final String ACTION_VOICE_WAKEUP_LAUNCH_INSECURECAMERA_ANYONE = "com.android.keyguard.VoiceWakeupManager.LAUNCH_INSEC_CAMERA_ANYONE";
    private final String ACTION_VOICE_WAKEUP_LAUNCH_MMS_OWNER_ONLY = "com.android.keyguard.VoiceWakeupManager.LAUNCH_MMS_OWNER";
    private final String ACTION_VOICE_WAKEUP_LAUNCH_MMS_ANYONE = "com.android.keyguard.VoiceWakeupManager.LAUNCH_MMS_ANYONE";
    private final int MSG_VOICE_WAKEUP_LAUNCH_SECURECAMERA_OWNER_ONLY = 1000;
    private final int MSG_VOICE_WAKEUP_LAUNCH_INSECURECAMERA_OWNER_ONLY = 1001;
    private final int MSG_VOICE_WAKEUP_LAUNCH_SECURECAMERA_ANYONE = 1002;
    private final int MSG_VOICE_WAKEUP_LAUNCH_INSECURECAMERA_ANYONE = 1003;
    private final int MSG_VOICE_WAKEUP_LAUNCH_MMS_OWNER_ONLY = 1004;
    private final int MSG_VOICE_WAKEUP_LAUNCH_MMS_ANYONE = 1005;
    private final int COMMAND_ID_LAUNCH_SECURECAMERA = 1;
    private final int COMMAND_ID_LAUNCH_INSECURECAMERA = 2;
    private final int COMMAND_ID_LAUNCH_MMS = 3;
    private final BroadcastReceiver mBroadcastReceiverForTest = new BroadcastReceiver(this) { // from class: com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager.5
        final VoiceWakeupManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int i = -1;
            if ("com.android.keyguard.VoiceWakeupManager.LAUNCH_SEC_CAMERA_OWNER".equals(action)) {
                i = 1000;
            } else if ("com.android.keyguard.VoiceWakeupManager.LAUNCH_INSEC_CAMERA_OWNER".equals(action)) {
                i = 1001;
            } else if ("com.android.keyguard.VoiceWakeupManager.LAUNCH_SEC_CAMERA_ANYONE".equals(action)) {
                i = 1002;
            } else if ("com.android.keyguard.VoiceWakeupManager.LAUNCH_INSEC_CAMERA_ANYONE".equals(action)) {
                i = 1003;
            } else if ("com.android.keyguard.VoiceWakeupManager.LAUNCH_MMS_OWNER".equals(action)) {
                i = 1004;
            } else if ("com.android.keyguard.VoiceWakeupManager.LAUNCH_MMS_ANYONE".equals(action)) {
                i = 1005;
            }
            this.this$0.mVoiceCommandHandlerForTest.obtainMessage(i).sendToTarget();
        }
    };
    private Handler mVoiceCommandHandlerForTest = new Handler(this) { // from class: com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager.6
        final VoiceWakeupManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = -1;
            int i2 = 2;
            switch (message.what) {
                case 1000:
                    i = 1;
                    i2 = 2;
                    break;
                case 1001:
                    i = 2;
                    i2 = 2;
                    break;
                case 1002:
                    i = 1;
                    i2 = 1;
                    break;
                case 1003:
                    i = 2;
                    i2 = 1;
                    break;
                case 1004:
                    i = 3;
                    i2 = 2;
                    break;
                case 1005:
                    i = 3;
                    i2 = 1;
                    break;
                default:
                    this.this$0.log("handleMessage() : msg.what is invalid!");
                    break;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("Result_Info", i);
            bundle.putInt("Reslut_Info1", i2);
            this.this$0.handleVoiceCommandNotified(bundle, true);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/mediatek/keyguard/VoiceWakeup/VoiceWakeupManager$LimitedModeApp.class */
    public class LimitedModeApp {
        public String limtedModeAppName;
        public String normalModeAppName;
        final VoiceWakeupManager this$0;

        public LimitedModeApp(VoiceWakeupManager voiceWakeupManager, String str, String str2) {
            this.this$0 = voiceWakeupManager;
            this.normalModeAppName = str;
            this.limtedModeAppName = str2;
        }
    }

    public VoiceWakeupManager() {
        Log.d("VoiceWakeupManager", "constructor is called.");
    }

    private void bindVoiceService(Context context) {
        log("bindVoiceService begin  ");
        Intent intent = new Intent();
        intent.setAction("com.mediatek.voicecommand");
        intent.addCategory("com.mediatek.nativeservice");
        intent.setPackage("com.mediatek.voicecommand");
        context.bindService(intent, this.mVoiceServiceConnection, 1);
    }

    private void dismissKeyguardOnNextActivity() {
        try {
            ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
        } catch (RemoteException e) {
            Log.w("VoiceWakeupManager", "can't dismiss keyguard on launch");
        }
    }

    private void doLaunchAppAndDismissKeyguard(int i, boolean z, boolean z2) {
        this.mIsDismissAndLaunchApp = false;
        this.mLaunchApp = getLaunchAppNameFromSettings(i, z2);
        if (this.mLaunchApp == null) {
            Log.d("VoiceWakeupManager", "AppName does not exist in Setting DB, give it a default value.");
            this.mLaunchApp = "com.android.contacts/com.android.contacts.activities.PeopleActivity";
        }
        AntiTheftManager.getInstance(null, null, null);
        boolean isAntiTheftLocked = AntiTheftManager.isAntiTheftLocked();
        boolean z3 = !this.mViewMediatorCallback.isKeyguardExternallyEnabled();
        if (isAntiTheftLocked || z3) {
            log("Give up launching since isAntitheftMode = " + isAntiTheftLocked + " isKeyguardExternallyDisabled = " + z3);
        } else if (this.mPM.isScreenOn()) {
            log("Give up launching since screen is on but we do not allow this case.");
        } else {
            this.mIsDismissAndLaunchApp = true;
            if ((this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) || this.mPM.isScreenOn()) ? false : !this.mViewMediatorCallback.isShowing()) {
                log("doLaunchAppAndDismissKeyguard() : call showLocked() due to keyguard isin the later locked status");
                this.mViewMediatorCallback.showLocked(null);
            }
            if (this.mViewMediatorCallback.isShowing() || this.mPM.isScreenOn()) {
            }
            if (this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) && this.securityModel.getSecurityMode() == KeyguardSecurityModel.SecurityMode.None) {
                log("doLaunchAppAndDismissKeyguard() : Keyguard is DISABLED, launch full-access mode APP and dismiss keyguard.");
                ComponentName unflattenFromString = ComponentName.unflattenFromString(this.mLaunchApp);
                Intent intent = new Intent();
                intent.setComponent(unflattenFromString);
                intent.setAction("android.intent.action.MAIN");
                launchApp(intent);
                lightUpScreen();
                this.mIsDismissAndLaunchApp = false;
            } else if (!this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) && this.securityModel.getSecurityMode() == KeyguardSecurityModel.SecurityMode.None) {
                log("doLaunchAppAndDismissKeyguard() : Keyguard is SLIDE mode, launch full-access mode APP and dismiss keyguard.");
                if (ComponentName.unflattenFromString(this.mLaunchApp).getClassName().indexOf("VoiceSearchActivity") != -1) {
                    lightUpScreen();
                }
                this.mViewMediatorCallback.dismiss(true);
            } else {
                log("doLaunchAppAndDismissKeyguard() : Keyguard is secured.");
                if (z) {
                    if (isSimPinPukMeModeNow()) {
                        log("doLaunchAppAndDismissKeyguard() : isUserDependentMode = TRUE but SIM PIN/PUK/ME screen shows, light up to request the password.");
                        lightUpScreen();
                        return;
                    }
                    log("doLaunchAppAndDismissKeyguard() : isUserDependentMode = TRUE, launch full-access mode APP and dismiss keyguard.");
                    this.mViewMediatorCallback.dismiss(true);
                    return;
                }
                String limtiedModeActionNameOfApp = getLimtiedModeActionNameOfApp(this.mLaunchApp);
                if (limtiedModeActionNameOfApp == null) {
                    log("doLaunchAppAndDismissKeyguard() : isUserDependentMode = FALSE & APP does not have limited mode, light up to request the password");
                    lightUpScreen();
                    this.mViewMediatorCallback.dismiss(false);
                    return;
                }
                log("doLaunchAppAndDismissKeyguard() : isUserDependentMode = FALSE & APP has limited mode, launch limited-access mode APP");
                KeyguardUpdateMonitor.getInstance(this.mContext).setAlternateUnlockEnabled(false);
                launchApp(new Intent(limtiedModeActionNameOfApp).addFlags(8388608));
                delayToLightUpScreen = true;
            }
        }
    }

    public static VoiceWakeupManager getInstance() {
        Log.d("VoiceWakeupManager", "getInstance(...) is called.");
        if (sInstance == null) {
            Log.d("VoiceWakeupManager", "getInstance(...) create one.");
            sInstance = new VoiceWakeupManager();
        }
        return sInstance;
    }

    private String getLaunchAppNameFromSettings(int i, boolean z) {
        String str = null;
        if (!z) {
            str = Settings.System.getVoiceCommandValue(this.mContext.getContentResolver(), Settings.System.BASE_VOICE_WAKEUP_COMMAND_KEY, i);
        } else if (i == 1) {
            str = "com.android.gallery3d/com.android.camera.SecureCameraActivity";
        } else if (i == 2) {
            str = "com.android.gallery3d/com.android.camera.CameraLauncher";
        } else if (i == 3) {
            str = "com.android.dialer/.DialtactsActivity";
        } else {
            log("getLaunchAppNameFromSettings() : wrong commandId = " + i);
        }
        Log.d("VoiceWakeupManager", "getLaunchAppNameFromSettings() - appName = " + str);
        return str;
    }

    private String getLimtiedModeActionNameOfApp(String str) {
        String str2;
        int i = 0;
        while (true) {
            str2 = null;
            if (i >= this.limitedApps.length) {
                break;
            } else if (str.equals(this.limitedApps[i].normalModeAppName)) {
                str2 = this.limitedApps[i].limtedModeAppName;
                break;
            } else {
                i++;
            }
        }
        return str2;
    }

    private boolean isSimPinPukMeModeNow() {
        return this.securityModel.getSecurityMode() == KeyguardSecurityModel.SecurityMode.SimPinPukMe1 || this.securityModel.getSecurityMode() == KeyguardSecurityModel.SecurityMode.SimPinPukMe2 || this.securityModel.getSecurityMode() == KeyguardSecurityModel.SecurityMode.SimPinPukMe3 || this.securityModel.getSecurityMode() == KeyguardSecurityModel.SecurityMode.SimPinPukMe4;
    }

    private void launchApp(Intent intent) {
        log("launchApp() enters.");
        dismissKeyguardOnNextActivity();
        intent.setFlags(872415232);
        this.mHandler.post(new Runnable(this, intent) { // from class: com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager.7
            final VoiceWakeupManager this$0;
            final Intent val$intent;

            {
                this.this$0 = this;
                this.val$intent = intent;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    this.this$0.mContext.startActivityAsUser(this.val$intent, new UserHandle(-2));
                    this.this$0.log("startActivity intent = " + this.val$intent.toString());
                } catch (ActivityNotFoundException e) {
                    this.this$0.log("Activity not found for intent + " + this.val$intent.getAction());
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void lightUpScreen() {
        log("lightUpScreen() is called.");
        if (!this.mIsDismissAndLaunchApp || this.mPM.isScreenOn()) {
            return;
        }
        log("lightUpScreen(), call PowerManager.wakeUp()");
        this.mPM.wakeUp(SystemClock.uptimeMillis());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void log(String str) {
        Log.d("VoiceWakeupManager", str);
    }

    private void registerBroadcastReceiverForTest() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.keyguard.VoiceWakeupManager.LAUNCH_SEC_CAMERA_OWNER");
        intentFilter.addAction("com.android.keyguard.VoiceWakeupManager.LAUNCH_INSEC_CAMERA_OWNER");
        intentFilter.addAction("com.android.keyguard.VoiceWakeupManager.LAUNCH_SEC_CAMERA_ANYONE");
        intentFilter.addAction("com.android.keyguard.VoiceWakeupManager.LAUNCH_INSEC_CAMERA_ANYONE");
        intentFilter.addAction("com.android.keyguard.VoiceWakeupManager.LAUNCH_MMS_OWNER");
        intentFilter.addAction("com.android.keyguard.VoiceWakeupManager.LAUNCH_MMS_ANYONE");
        this.mContext.registerReceiver(this.mBroadcastReceiverForTest, intentFilter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerVoiceCommand(String str) {
        if (this.isRegistered) {
            log("register voiceCommand success ");
        } else {
            try {
                int registerListener = this.mVCmdMgrService.registerListener(str, this.mVoiceCallback);
                if (registerListener == 0) {
                    this.isRegistered = true;
                    log("register voiceCommand successfuly, now send VOICE_WAKEUP_START");
                    sendVoiceCommand(this.mPkgName, 6, 1, null);
                } else {
                    log("register voiceCommand fail errorid=" + registerListener + " with pkgName=" + str);
                }
            } catch (RemoteException e) {
                this.isRegistered = false;
                this.mVCmdMgrService = null;
                log("register voiceCommand RemoteException =  " + e.getMessage());
            }
        }
        log("register voiceCommand end ");
    }

    public boolean checkIfVowSupport(Context context) {
        boolean z = false;
        if (context == null) {
            log("checkIfVowSupport() - context is still null, bypass the check...");
        } else if (KeyguardUtils.isVoiceWakeupSupport(context)) {
            log("MTK_VOW_SUPPORT is enabled in this load.");
            z = true;
        } else {
            log("MTK_VOW_SUPPORT is NOT enabled in this load.");
        }
        return z;
    }

    public void handleVoiceCommandNotified(Bundle bundle, boolean z) {
        int i = bundle.getInt("Result_Info");
        boolean z2 = bundle.getInt("Reslut_Info1") == 2;
        log("data.getInt(VoiceCommandListener.ACTION_EXTRA_RESULT_INFO1) = " + bundle.getInt("Reslut_Info1"));
        log("handleVoiceCommandNotified() commandId = " + i + " isUserDependentMode = " + z2);
        doLaunchAppAndDismissKeyguard(i, z2, z);
    }

    public void init(Context context, ViewMediatorCallback viewMediatorCallback) {
        log("init() is called.");
        this.mContext = context;
        if (checkIfVowSupport(context)) {
            this.mLockPatternUtils = new LockPatternUtils(context);
            this.mViewMediatorCallback = viewMediatorCallback;
            this.securityModel = new KeyguardSecurityModel(this.mContext);
            this.mPkgName = this.mContext.getPackageName();
            this.mHandler = new Handler();
            this.mPM = (PowerManager) this.mContext.getSystemService("power");
            KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateCallback);
            registerBroadcastReceiverForTest();
            start();
        }
    }

    public boolean isDismissAndLaunchApp() {
        log("isDismissAndLaunchApp() mIsDismissAndLaunchApp = " + this.mIsDismissAndLaunchApp);
        return this.mIsDismissAndLaunchApp;
    }

    public void notifyKeyguardIsGone() {
        log("notifyKeyguardGoneAndLightUpScreen() enters");
        if (checkIfVowSupport(this.mContext)) {
            lightUpScreen();
            this.mIsDismissAndLaunchApp = false;
        }
    }

    public void notifySecurityModeChange(KeyguardSecurityModel.SecurityMode securityMode, KeyguardSecurityModel.SecurityMode securityMode2) {
        if (checkIfVowSupport(this.mContext)) {
            log("notifySecurityModeChange curr = " + securityMode + ", next = " + securityMode2);
            log("notifySecurityModeChange original mIsDismissAndLaunchApp = " + this.mIsDismissAndLaunchApp);
            if (this.mPM.isScreenOn() && this.mIsDismissAndLaunchApp) {
                if (securityMode2 == KeyguardSecurityModel.SecurityMode.AlarmBoot || securityMode2 == KeyguardSecurityModel.SecurityMode.AntiTheft) {
                    log("notifySecurityModeChange(): mIsDismissAndLaunchApp = false");
                    this.mIsDismissAndLaunchApp = false;
                }
            }
        }
    }

    @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
    public boolean onDismiss() {
        log("onDismiss() is called.");
        if (checkIfVowSupport(this.mContext)) {
            ComponentName unflattenFromString = ComponentName.unflattenFromString(this.mLaunchApp);
            Intent intent = new Intent();
            intent.setComponent(unflattenFromString);
            intent.setAction("android.intent.action.MAIN");
            launchApp(intent);
            return true;
        }
        return false;
    }

    public void sendVoiceCommand(String str, int i, int i2, Bundle bundle) {
        if (!this.isRegistered) {
            log("didn't register , can not send voice Command  ");
            return;
        }
        try {
            if (this.mVCmdMgrService.sendCommand(str, i, i2, bundle) != 0) {
                log("send voice Command fail ");
            } else {
                log("send voice Command success ");
            }
        } catch (RemoteException e) {
            this.isRegistered = false;
            this.mVCmdMgrService = null;
            log("send voice Command RemoteException =  " + e.getMessage());
        }
    }

    public void start() {
        log("start()");
        if (checkIfVowSupport(this.mContext)) {
            log("register to service");
            if (this.mVCmdMgrService == null) {
                bindVoiceService(this.mContext);
            } else {
                registerVoiceCommand(this.mPkgName);
            }
        }
    }
}
