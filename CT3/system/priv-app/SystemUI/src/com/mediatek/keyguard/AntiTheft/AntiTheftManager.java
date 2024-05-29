package com.mediatek.keyguard.AntiTheft;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUtils;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.android.keyguard.R$string;
import com.android.keyguard.ViewMediatorCallback;
import com.mediatek.common.dm.DmAgent;
import com.mediatek.common.ppl.IPplManager;
import com.mediatek.internal.telephony.ppl.IPplAgent;
/* loaded from: a.zip:com/mediatek/keyguard/AntiTheft/AntiTheftManager.class */
public class AntiTheftManager {

    /* renamed from: -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues  reason: not valid java name */
    private static final int[] f9x1cbe7e58 = null;
    private static Context mContext;
    private static IPplManager mIPplManager;
    private static AntiTheftManager sInstance;
    protected KeyguardSecurityCallback mKeyguardSecurityCallback;
    private LockPatternUtils mLockPatternUtils;
    private KeyguardSecurityModel mSecurityModel;
    private ViewMediatorCallback mViewMediatorCallback;
    private static int mAntiTheftLockEnabled = 0;
    private static int mKeypadNeeded = 0;
    private static int mDismissable = 0;
    private static boolean mAntiTheftAutoTestNotShowUI = false;
    private final int MSG_ARG_LOCK = 0;
    private final int MSG_ARG_UNLOCK = 1;
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.mediatek.keyguard.AntiTheft.AntiTheftManager.1
        final AntiTheftManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("AntiTheftManager", "handleAntiTheftViewUpdate() - action = " + action);
            if ("com.mediatek.dm.LAWMO_LOCK".equals(action)) {
                Log.d("AntiTheftManager", "receive OMADM_LAWMO_LOCK");
                this.this$0.sendAntiTheftUpdateMsg(1, 0);
            } else if ("com.mediatek.dm.LAWMO_UNLOCK".equals(action)) {
                Log.d("AntiTheftManager", "receive OMADM_LAWMO_UNLOCK");
                this.this$0.sendAntiTheftUpdateMsg(1, 1);
            } else if ("com.mediatek.ppl.NOTIFY_LOCK".equals(action)) {
                Log.d("AntiTheftManager", "receive PPL_LOCK");
                if (KeyguardUtils.isSystemEncrypted()) {
                    Log.d("AntiTheftManager", "Currently system needs to be decrypted. Not show PPL.");
                } else {
                    this.this$0.sendAntiTheftUpdateMsg(2, 0);
                }
            } else if ("com.mediatek.ppl.NOTIFY_UNLOCK".equals(action)) {
                Log.d("AntiTheftManager", "receive PPL_UNLOCK");
                this.this$0.sendAntiTheftUpdateMsg(2, 1);
            } else if ("android.intent.action.ACTION_PREBOOT_IPO".equals(action)) {
                this.this$0.doBindAntiThftLockServices();
            }
        }
    };
    private Handler mHandler = new Handler(this, Looper.myLooper(), null, true) { // from class: com.mediatek.keyguard.AntiTheft.AntiTheftManager.2
        final AntiTheftManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = false;
            switch (message.what) {
                case 1001:
                    AntiTheftManager antiTheftManager = this.this$0;
                    int i = message.arg1;
                    if (message.arg2 == 0) {
                        z = true;
                    }
                    antiTheftManager.handleAntiTheftViewUpdate(i, z);
                    return;
                default:
                    return;
            }
        }
    };
    protected ServiceConnection mPplServiceConnection = new ServiceConnection(this) { // from class: com.mediatek.keyguard.AntiTheft.AntiTheftManager.3
        final AntiTheftManager this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("AntiTheftManager", "onServiceConnected() -- PPL");
            IPplManager unused = AntiTheftManager.mIPplManager = IPplManager.Stub.asInterface(iBinder);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("AntiTheftManager", "onServiceDisconnected()");
            IPplManager unused = AntiTheftManager.mIPplManager = null;
        }
    };

    /* renamed from: -getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m2275xec5d63fc() {
        if (f9x1cbe7e58 != null) {
            return f9x1cbe7e58;
        }
        int[] iArr = new int[KeyguardSecurityModel.SecurityMode.valuesCustom().length];
        try {
            iArr[KeyguardSecurityModel.SecurityMode.AlarmBoot.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.AntiTheft.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Biometric.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Invalid.ordinal()] = 8;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.None.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.PIN.ordinal()] = 10;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Password.ordinal()] = 11;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Pattern.ordinal()] = 12;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe1.ordinal()] = 2;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe2.ordinal()] = 3;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe3.ordinal()] = 4;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe4.ordinal()] = 5;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Voice.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        f9x1cbe7e58 = iArr;
        return iArr;
    }

    public AntiTheftManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        Log.d("AntiTheftManager", "AntiTheftManager() is called.");
        mContext = context;
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityModel = new KeyguardSecurityModel(mContext);
        IntentFilter intentFilter = new IntentFilter();
        setKeypadNeeded(1, false);
        setDismissable(1, false);
        intentFilter.addAction("com.mediatek.dm.LAWMO_LOCK");
        intentFilter.addAction("com.mediatek.dm.LAWMO_UNLOCK");
        if (KeyguardUtils.isPrivacyProtectionLockSupport()) {
            Log.d("AntiTheftManager", "MTK_PRIVACY_PROTECTION_LOCK is enabled.");
            setKeypadNeeded(2, true);
            setDismissable(2, true);
            intentFilter.addAction("com.mediatek.ppl.NOTIFY_LOCK");
            intentFilter.addAction("com.mediatek.ppl.NOTIFY_UNLOCK");
        }
        intentFilter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    private void bindPplService() {
        Log.e("AntiTheftManager", "binPplService() is called.");
        if (mIPplManager != null) {
            Log.d("AntiTheftManager", "bindPplService() -- the ppl service is already bound.");
            return;
        }
        try {
            Intent intent = new Intent("com.mediatek.ppl.service");
            intent.setClassName("com.mediatek.ppl", "com.mediatek.ppl.PplService");
            mContext.bindService(intent, this.mPplServiceConnection, 1);
        } catch (SecurityException e) {
            Log.e("AntiTheftManager", "bindPplService() - error in bind ppl service.");
        }
    }

    public static void checkPplStatus() {
        boolean z = !KeyguardUtils.isSystemEncrypted();
        try {
            IBinder service = ServiceManager.getService("PPLAgent");
            if (service == null) {
                Log.i("AntiTheftManager", "PplCheckLocked, PPLAgent doesn't exit");
                return;
            }
            boolean z2 = IPplAgent.Stub.asInterface(service).needLock() == 1;
            Log.i("AntiTheftManager", "PplCheckLocked, the lock flag is:" + (z2 ? z : false));
            if (z2 && z) {
                mAntiTheftLockEnabled |= 2;
            }
        } catch (RemoteException e) {
            Log.e("AntiTheftManager", "doPplLockCheck() - error in get PPLAgent service.");
        }
    }

    private void doDmLockCheck() {
        try {
            IBinder service = ServiceManager.getService("DmAgent");
            if (service != null) {
                boolean isLockFlagSet = DmAgent.Stub.asInterface(service).isLockFlagSet();
                Log.i("AntiTheftManager", "dmCheckLocked, the lock flag is:" + isLockFlagSet);
                setAntiTheftLocked(1, isLockFlagSet);
            } else {
                Log.i("AntiTheftManager", "dmCheckLocked, DmAgent doesn't exit");
            }
        } catch (RemoteException e) {
            Log.e("AntiTheftManager", "doDmLockCheck() - error in get DMAgent service.");
        }
    }

    private boolean doPplCheckPassword(String str) {
        boolean z = false;
        if (mIPplManager != null) {
            z = false;
            try {
                boolean unlock = mIPplManager.unlock(str);
                Log.i("AntiTheftManager", "doPplCheckPassword, unlockSuccess is " + unlock);
                z = unlock;
                if (unlock) {
                    z = unlock;
                    setAntiTheftLocked(2, false);
                    z = unlock;
                }
            } catch (RemoteException e) {
            }
        } else {
            Log.i("AntiTheftManager", "doPplCheckPassword() mIPplManager == null !!??");
        }
        return z;
    }

    private void doPplLockCheck() {
        if (mAntiTheftLockEnabled == 2) {
            setAntiTheftLocked(2, true);
        }
    }

    public static int getAntiTheftLayoutId() {
        return R$layout.mtk_keyguard_anti_theft_lock_view;
    }

    public static String getAntiTheftMessageAreaText(CharSequence charSequence, CharSequence charSequence2) {
        StringBuilder sb = new StringBuilder();
        if (charSequence != null && charSequence.length() > 0 && !charSequence.toString().equals("AntiTheft Noneed Print Text")) {
            sb.append(charSequence);
            sb.append(charSequence2);
        }
        sb.append(mContext.getText(getPrompt()));
        return sb.toString();
    }

    public static String getAntiTheftModeName(int i) {
        switch (i) {
            case 0:
                return "AntiTheftMode.None";
            case 1:
                return "AntiTheftMode.DmLock";
            case 2:
                return "AntiTheftMode.PplLock";
            default:
                return "AntiTheftMode.None";
        }
    }

    public static int getAntiTheftViewId() {
        return R$id.keyguard_antitheft_lock_view;
    }

    public static int getCurrentAntiTheftMode() {
        Log.d("AntiTheftManager", "getCurrentAntiTheftMode() is called.");
        if (isAntiTheftLocked()) {
            for (int i = 0; i < 32; i++) {
                int i2 = mAntiTheftLockEnabled & (1 << i);
                if (i2 != 0) {
                    return i2;
                }
            }
            return 0;
        }
        return 0;
    }

    public static AntiTheftManager getInstance(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        Log.d("AntiTheftManager", "getInstance(...) is called.");
        if (sInstance == null) {
            Log.d("AntiTheftManager", "getInstance(...) create one.");
            sInstance = new AntiTheftManager(context, viewMediatorCallback, lockPatternUtils);
        }
        return sInstance;
    }

    public static int getPrompt() {
        return getCurrentAntiTheftMode() == 1 ? R$string.dm_prompt : R$string.ppl_prompt;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAntiTheftViewUpdate(int i, boolean z) {
        if (isNeedUpdate(i, z)) {
            setAntiTheftLocked(i, z);
            if (z) {
                StringBuilder append = new StringBuilder().append("handleAntiTheftViewUpdate() - locked, !isShowing = ");
                boolean z2 = true;
                if (this.mViewMediatorCallback.isShowing()) {
                    z2 = false;
                }
                Log.d("AntiTheftManager", append.append(z2).append(" isKeyguardDoneOnGoing = ").append(this.mViewMediatorCallback.isKeyguardDoneOnGoing()).toString());
                if (!this.mViewMediatorCallback.isShowing() || this.mViewMediatorCallback.isKeyguardDoneOnGoing()) {
                    this.mViewMediatorCallback.showLocked(null);
                } else if (isAntiTheftPriorToSecMode(this.mSecurityModel.getSecurityMode())) {
                    Log.d("AntiTheftManager", "handleAntiTheftViewUpdate() - call resetStateLocked().");
                    this.mViewMediatorCallback.resetStateLocked();
                } else {
                    Log.d("AntiTheftManager", "No need to reset the security view to show AntiTheft,since current view should show above antitheft view.");
                }
            } else if (this.mKeyguardSecurityCallback != null) {
                this.mKeyguardSecurityCallback.dismiss(true);
            } else {
                Log.d("AntiTheftManager", "mKeyguardSecurityCallback is null !");
            }
            adjustStatusBarLocked();
        }
    }

    public static boolean isAntiTheftLocked() {
        boolean z = false;
        if (mAntiTheftLockEnabled != 0) {
            z = true;
        }
        return z;
    }

    public static boolean isAntiTheftPriorToSecMode(KeyguardSecurityModel.SecurityMode securityMode) {
        int currentAntiTheftMode = getCurrentAntiTheftMode();
        boolean z = false;
        if (isAntiTheftLocked()) {
            if (currentAntiTheftMode != 1) {
                z = false;
                switch (m2275xec5d63fc()[securityMode.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        break;
                    default:
                        z = true;
                        break;
                }
            } else {
                z = true;
            }
        }
        return z;
    }

    public static boolean isDismissable() {
        int currentAntiTheftMode = getCurrentAntiTheftMode();
        boolean z = false;
        if (currentAntiTheftMode == 0) {
            z = true;
        } else if ((mDismissable & currentAntiTheftMode) != 0) {
            z = true;
        }
        return z;
    }

    public static boolean isKeypadNeeded() {
        int currentAntiTheftMode = getCurrentAntiTheftMode();
        Log.d("AntiTheftManager", "getCurrentAntiTheftMode() = " + getAntiTheftModeName(currentAntiTheftMode));
        boolean z = (mKeypadNeeded & currentAntiTheftMode) != 0;
        Log.d("AntiTheftManager", "isKeypadNeeded() = " + z);
        return z;
    }

    private static boolean isNeedUpdate(int i, boolean z) {
        boolean z2;
        if (!z || (mAntiTheftLockEnabled & i) == 0) {
            z2 = true;
            if (!z) {
                z2 = true;
                if ((mAntiTheftLockEnabled & i) == 0) {
                    Log.d("AntiTheftManager", "isNeedUpdate() - lockMode( " + i + " ) is already disabled, no need update");
                    z2 = false;
                }
            }
        } else {
            Log.d("AntiTheftManager", "isNeedUpdate() - lockMode( " + i + " ) is already enabled, no need update");
            z2 = false;
        }
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAntiTheftUpdateMsg(int i, int i2) {
        Message obtainMessage = this.mHandler.obtainMessage(1001);
        obtainMessage.arg1 = i;
        obtainMessage.arg2 = i2;
        obtainMessage.sendToTarget();
    }

    private void setAntiTheftLocked(int i, boolean z) {
        if (z) {
            mAntiTheftLockEnabled |= i;
        } else {
            mAntiTheftLockEnabled &= i ^ (-1);
        }
        this.mViewMediatorCallback.updateAntiTheftLocked();
    }

    public static void setDismissable(int i, boolean z) {
        Log.d("AntiTheftManager", "mDismissable is " + mDismissable + " before");
        if (z) {
            mDismissable |= i;
        } else {
            mDismissable &= i ^ (-1);
        }
        Log.d("AntiTheftManager", "mDismissable is " + mDismissable + " after");
    }

    public static void setKeypadNeeded(int i, boolean z) {
        if (z) {
            mKeypadNeeded |= i;
        } else {
            mKeypadNeeded &= i ^ (-1);
        }
    }

    public void adjustStatusBarLocked() {
        this.mViewMediatorCallback.adjustStatusBarLocked();
    }

    public boolean checkPassword(String str) {
        boolean z = false;
        int currentAntiTheftMode = getCurrentAntiTheftMode();
        Log.d("AntiTheftManager", "checkPassword, mode is " + getAntiTheftModeName(currentAntiTheftMode));
        switch (currentAntiTheftMode) {
            case 2:
                z = doPplCheckPassword(str);
                break;
        }
        Log.d("AntiTheftManager", "checkPassword, unlockSuccess is " + z);
        return z;
    }

    public void doAntiTheftLockCheck() {
        if ("unencrypted".equalsIgnoreCase(SystemProperties.get("ro.crypto.state", "unsupported"))) {
            doPplLockCheck();
            doDmLockCheck();
        }
    }

    public void doBindAntiThftLockServices() {
        Log.d("AntiTheftManager", "doBindAntiThftLockServices() is called.");
        if (KeyguardUtils.isPrivacyProtectionLockSupport()) {
            bindPplService();
        }
    }

    public Handler getHandlerInstance() {
        return this.mHandler;
    }

    public BroadcastReceiver getPPLBroadcastReceiverInstance() {
        return this.mBroadcastReceiver;
    }

    public IPplManager getPPLManagerInstance() {
        return mIPplManager;
    }

    public void setSecurityViewCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        Log.d("AntiTheftManager", "setSecurityViewCallback(" + keyguardSecurityCallback + ")");
        this.mKeyguardSecurityCallback = keyguardSecurityCallback;
    }
}
