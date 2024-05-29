package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IUserSwitchObserver;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.fingerprint.FingerprintManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;
import com.mediatek.internal.telephony.ITelephonyEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/* loaded from: a.zip:com/android/keyguard/KeyguardUpdateMonitor.class */
public class KeyguardUpdateMonitor implements TrustManager.TrustListener {

    /* renamed from: -com-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
    private static final int[] f7x8dbfd0b5 = null;
    private static int sCurrentUser;
    private static KeyguardUpdateMonitor sInstance;
    private AlarmManager mAlarmManager;
    private boolean mAlternateUnlockEnabled;
    private BatteryStatus mBatteryStatus;
    private boolean mBootCompleted;
    private boolean mBouncer;
    private final Context mContext;
    private boolean mDeviceInteractive;
    private ContentObserver mDeviceProvisionedObserver;
    private boolean mFingerprintAlreadyAuthenticated;
    private CancellationSignal mFingerprintCancelSignal;
    private FingerprintManager mFpm;
    private boolean mGoingToSleep;
    private boolean mKeyguardIsVisible;
    private int mPhoneState;
    private int mRingMode;
    private boolean mScreenOn;
    private final StrongAuthTracker mStrongAuthTracker;
    private List<SubscriptionInfo> mSubscriptionInfo;
    private SubscriptionManager mSubscriptionManager;
    private boolean mSwitchingUser;
    private TrustManager mTrustManager;
    private WifiManager mWifiManager;
    HashMap<Integer, SimData> mSimDatas = new HashMap<>();
    HashMap<Integer, ServiceState> mServiceStates = new HashMap<>();
    private HashMap<Integer, IccCardConstants.State> mSimStateOfPhoneId = new HashMap<>();
    private HashMap<Integer, CharSequence> mTelephonyPlmn = new HashMap<>();
    private HashMap<Integer, CharSequence> mTelephonySpn = new HashMap<>();
    private SparseIntArray mFailedAttempts = new SparseIntArray();
    private ArraySet<Integer> mStrongAuthNotTimedOut = new ArraySet<>();
    private final CopyOnWriteArrayList<WeakReference<KeyguardUpdateMonitorCallback>> mCallbacks = new CopyOnWriteArrayList<>();
    private int mFingerprintRunningState = 0;
    final Handler mHandler = new Handler(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.1
        final KeyguardUpdateMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = false;
            switch (message.what) {
                case 301:
                    this.this$0.handleTimeUpdate();
                    return;
                case 302:
                    this.this$0.handleBatteryUpdate((BatteryStatus) message.obj);
                    return;
                case 303:
                    this.this$0.handleCarrierInfoUpdate(((Integer) message.obj).intValue());
                    return;
                case 304:
                    this.this$0.handleSimStateChange((SimData) message.obj);
                    return;
                case 305:
                    this.this$0.handleRingerModeChange(message.arg1);
                    return;
                case 306:
                    this.this$0.handlePhoneStateChanged();
                    return;
                case 308:
                    this.this$0.handleDeviceProvisioned();
                    return;
                case 309:
                    this.this$0.handleDevicePolicyManagerStateChanged();
                    return;
                case 310:
                    this.this$0.handleUserSwitching(message.arg1, (IRemoteCallback) message.obj);
                    return;
                case 312:
                    this.this$0.handleKeyguardReset();
                    return;
                case 313:
                    this.this$0.handleBootCompleted();
                    return;
                case 314:
                    this.this$0.handleUserSwitchComplete(message.arg1);
                    return;
                case 317:
                    this.this$0.handleUserInfoChanged(message.arg1);
                    return;
                case 318:
                    this.this$0.handleReportEmergencyCallAction();
                    return;
                case 319:
                    this.this$0.handleStartedWakingUp();
                    return;
                case 320:
                    this.this$0.handleFinishedGoingToSleep(message.arg1);
                    return;
                case 321:
                    this.this$0.handleStartedGoingToSleep(message.arg1);
                    return;
                case 322:
                    this.this$0.handleKeyguardBouncerChanged(message.arg1);
                    return;
                case 327:
                    KeyguardUpdateMonitor keyguardUpdateMonitor = this.this$0;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    keyguardUpdateMonitor.handleFaceUnlockStateChanged(z, message.arg2);
                    return;
                case 328:
                    this.this$0.handleSimSubscriptionInfoChanged();
                    return;
                case 329:
                    this.this$0.handleAirplaneModeChanged();
                    return;
                case 330:
                    this.this$0.handleServiceStateChange(message.arg1, (ServiceState) message.obj);
                    return;
                case 331:
                    this.this$0.handleScreenTurnedOn();
                    return;
                case 332:
                    this.this$0.handleScreenTurnedOff();
                    return;
                case 1015:
                    this.this$0.handleAirPlaneModeUpdate(((Boolean) message.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    };
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = new SubscriptionManager.OnSubscriptionsChangedListener(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.2
        final KeyguardUpdateMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            this.this$0.mHandler.removeMessages(328);
            this.this$0.mHandler.sendEmptyMessage(328);
        }
    };
    private SparseBooleanArray mUserHasTrust = new SparseBooleanArray();
    private SparseBooleanArray mUserTrustIsManaged = new SparseBooleanArray();
    private SparseBooleanArray mUserFingerprintAuthenticated = new SparseBooleanArray();
    private SparseBooleanArray mUserFaceUnlockRunning = new SparseBooleanArray();
    private DisplayClientState mDisplayClientState = new DisplayClientState();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.3
        final KeyguardUpdateMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("KeyguardUpdateMonitor", "received broadcast " + action);
            if ("android.intent.action.TIME_TICK".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                this.this$0.mHandler.sendEmptyMessage(301);
            } else if ("android.provider.Telephony.SPN_STRINGS_UPDATED".equals(action)) {
                int intExtra = intent.getIntExtra("subscription", -1);
                Log.d("KeyguardUpdateMonitor", "SPN_STRINGS_UPDATED_ACTION, sub Id = " + intExtra);
                int phoneIdUsingSubId = KeyguardUtils.getPhoneIdUsingSubId(intExtra);
                if (!KeyguardUtils.isValidPhoneId(phoneIdUsingSubId)) {
                    Log.d("KeyguardUpdateMonitor", "SPN_STRINGS_UPDATED_ACTION, invalid phoneId = " + phoneIdUsingSubId);
                    return;
                }
                this.this$0.mTelephonyPlmn.put(Integer.valueOf(phoneIdUsingSubId), this.this$0.getTelephonyPlmnFrom(intent));
                this.this$0.mTelephonySpn.put(Integer.valueOf(phoneIdUsingSubId), this.this$0.getTelephonySpnFrom(intent));
                this.this$0.mTelephonyCsgId.put(Integer.valueOf(phoneIdUsingSubId), this.this$0.getTelephonyCsgIdFrom(intent));
                this.this$0.mTelephonyHnbName.put(Integer.valueOf(phoneIdUsingSubId), this.this$0.getTelephonyHnbNameFrom(intent));
                Log.d("KeyguardUpdateMonitor", "SPN_STRINGS_UPDATED_ACTION, update phoneId=" + phoneIdUsingSubId + ", plmn=" + this.this$0.mTelephonyPlmn.get(Integer.valueOf(phoneIdUsingSubId)) + ", spn=" + this.this$0.mTelephonySpn.get(Integer.valueOf(phoneIdUsingSubId)) + ", csgId=" + this.this$0.mTelephonyCsgId.get(Integer.valueOf(phoneIdUsingSubId)) + ", hnbName=" + this.this$0.mTelephonyHnbName.get(Integer.valueOf(phoneIdUsingSubId)));
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(303, Integer.valueOf(phoneIdUsingSubId)));
            } else if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                int intExtra2 = intent.getIntExtra("status", 1);
                int intExtra3 = intent.getIntExtra("plugged", 0);
                int intExtra4 = intent.getIntExtra("level", 0);
                int intExtra5 = intent.getIntExtra("health", 1);
                int intExtra6 = intent.getIntExtra("max_charging_current", -1);
                int intExtra7 = intent.getIntExtra("max_charging_voltage", -1);
                int i = intExtra7;
                if (intExtra7 <= 0) {
                    i = 5000000;
                }
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(302, new BatteryStatus(intExtra2, intExtra4, intExtra3, intExtra5, intExtra6 > 0 ? (intExtra6 / 1000) * (i / 1000) : -1)));
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action) || "mediatek.intent.action.ACTION_UNLOCK_SIM_LOCK".equals(action)) {
                String stringExtra = intent.getStringExtra("ss");
                SimData fromIntent = SimData.fromIntent(intent);
                Log.v("KeyguardUpdateMonitor", "action=" + action + ", state=" + stringExtra + ", slotId=" + fromIntent.phoneId + ", subId=" + fromIntent.subId + ", simArgs.simState = " + fromIntent.simState);
                if ("mediatek.intent.action.ACTION_UNLOCK_SIM_LOCK".equals(action)) {
                    Log.d("KeyguardUpdateMonitor", "ACTION_UNLOCK_SIM_LOCK, set sim state as UNKNOWN");
                    this.this$0.mSimStateOfPhoneId.put(Integer.valueOf(fromIntent.phoneId), IccCardConstants.State.UNKNOWN);
                }
                this.this$0.proceedToHandleSimStateChanged(fromIntent);
            } else if ("android.media.RINGER_MODE_CHANGED".equals(action)) {
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(305, intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1), 0));
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(306, intent.getStringExtra("state")));
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                this.this$0.dispatchBootCompleted();
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                boolean booleanExtra = intent.getBooleanExtra("state", false);
                Log.d("KeyguardUpdateMonitor", "Receive ACTION_AIRPLANE_MODE_CHANGED, state = " + booleanExtra);
                Message message = new Message();
                message.what = 1015;
                message.obj = new Boolean(booleanExtra);
                this.this$0.mHandler.sendMessage(message);
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                ServiceState newFromBundle = ServiceState.newFromBundle(intent.getExtras());
                int intExtra8 = intent.getIntExtra("subscription", -1);
                Log.v("KeyguardUpdateMonitor", "action " + action + " serviceState=" + newFromBundle + " subId=" + intExtra8);
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(330, intExtra8, 0, newFromBundle));
            }
        }
    };
    private final BroadcastReceiver mBroadcastAllReceiver = new BroadcastReceiver(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.4
        final KeyguardUpdateMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(action)) {
                this.this$0.mHandler.sendEmptyMessage(301);
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(action)) {
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(317, intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()), 0));
            } else if ("com.android.facelock.FACE_UNLOCK_STARTED".equals(action)) {
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(327, 1, getSendingUserId()));
            } else if ("com.android.facelock.FACE_UNLOCK_STOPPED".equals(action)) {
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(327, 0, getSendingUserId()));
            } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                this.this$0.mHandler.sendEmptyMessage(309);
            }
        }
    };
    private final BroadcastReceiver mStrongAuthTimeoutReceiver = new BroadcastReceiver(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.5
        final KeyguardUpdateMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.ACTION_STRONG_AUTH_TIMEOUT".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("com.android.systemui.USER_ID", -1);
                this.this$0.mStrongAuthNotTimedOut.remove(Integer.valueOf(intExtra));
                this.this$0.notifyStrongAuthStateChanged(intExtra);
            }
        }
    };
    private final FingerprintManager.LockoutResetCallback mLockoutResetCallback = new FingerprintManager.LockoutResetCallback(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.6
        final KeyguardUpdateMonitor this$0;

        {
            this.this$0 = this;
        }

        public void onLockoutReset() {
            this.this$0.handleFingerprintLockoutReset();
        }
    };
    private FingerprintManager.AuthenticationCallback mAuthenticationCallback = new FingerprintManager.AuthenticationCallback(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.7
        final KeyguardUpdateMonitor this$0;

        {
            this.this$0 = this;
        }

        public void onAuthenticationAcquired(int i) {
            this.this$0.handleFingerprintAcquired(i);
        }

        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationError(int i, CharSequence charSequence) {
            this.this$0.handleFingerprintError(i, charSequence.toString());
        }

        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationFailed() {
            this.this$0.handleFingerprintAuthFailed();
        }

        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            this.this$0.handleFingerprintHelp(i, charSequence.toString());
        }

        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult authenticationResult) {
            this.this$0.handleFingerprintAuthenticated(authenticationResult.getUserId());
        }
    };
    private boolean mNewClientRegUpdateMonitor = false;
    private boolean mShowing = true;
    private HashMap<Integer, Integer> mSimMeCategory = new HashMap<>();
    private HashMap<Integer, Integer> mSimMeLeftRetryCount = new HashMap<>();
    private int mPinPukMeDismissFlag = 0;
    private HashMap<Integer, CharSequence> mTelephonyHnbName = new HashMap<>();
    private HashMap<Integer, CharSequence> mTelephonyCsgId = new HashMap<>();
    private int mFailedBiometricUnlockAttempts = 0;
    private boolean mDeviceProvisioned = isDeviceProvisionedInSettingsDb();

    /* loaded from: a.zip:com/android/keyguard/KeyguardUpdateMonitor$BatteryStatus.class */
    public static class BatteryStatus {
        public final int health;
        public final int level;
        public final int maxChargingWattage;
        public final int plugged;
        public final int status;

        public BatteryStatus(int i, int i2, int i3, int i4, int i5) {
            this.status = i;
            this.level = i2;
            this.plugged = i3;
            this.health = i4;
            this.maxChargingWattage = i5;
        }

        public final int getChargingSpeed(int i, int i2) {
            int i3 = 0;
            if (this.maxChargingWattage <= 0) {
                i3 = -1;
            } else if (this.maxChargingWattage >= i) {
                i3 = this.maxChargingWattage > i2 ? 2 : 1;
            }
            return i3;
        }

        public boolean isBatteryLow() {
            return this.level < 16;
        }

        public boolean isCharged() {
            boolean z = true;
            if (this.status != 5) {
                z = this.level >= 100;
            }
            return z;
        }

        public boolean isPluggedIn() {
            boolean z = true;
            if (this.plugged != 1) {
                if (this.plugged == 2) {
                    z = true;
                } else {
                    z = true;
                    if (this.plugged != 4) {
                        z = false;
                    }
                }
            }
            return z;
        }
    }

    /* loaded from: a.zip:com/android/keyguard/KeyguardUpdateMonitor$DisplayClientState.class */
    static class DisplayClientState {
        DisplayClientState() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/keyguard/KeyguardUpdateMonitor$SimData.class */
    public static class SimData {
        public int phoneId;
        public int simMECategory;
        public final IccCardConstants.State simState;
        public int subId;

        SimData(IccCardConstants.State state, int i, int i2) {
            this.phoneId = 0;
            this.simMECategory = 0;
            this.simState = state;
            this.phoneId = i;
            this.subId = i2;
        }

        SimData(IccCardConstants.State state, int i, int i2, int i3) {
            this.phoneId = 0;
            this.simMECategory = 0;
            this.simState = state;
            this.phoneId = i;
            this.subId = i2;
            this.simMECategory = i3;
        }

        static SimData fromIntent(Intent intent) {
            IccCardConstants.State state;
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) || "mediatek.intent.action.ACTION_UNLOCK_SIM_LOCK".equals(intent.getAction())) {
                int i = 0;
                String stringExtra = intent.getStringExtra("ss");
                int intExtra = intent.getIntExtra("slot", 0);
                int intExtra2 = intent.getIntExtra("subscription", -1);
                if ("ABSENT".equals(stringExtra)) {
                    state = "PERM_DISABLED".equals(intent.getStringExtra("reason")) ? IccCardConstants.State.PERM_DISABLED : IccCardConstants.State.ABSENT;
                } else if ("READY".equals(stringExtra)) {
                    state = IccCardConstants.State.READY;
                } else if ("LOCKED".equals(stringExtra)) {
                    String stringExtra2 = intent.getStringExtra("reason");
                    Log.d("KeyguardUpdateMonitor", "INTENT_VALUE_ICC_LOCKED, lockedReason=" + stringExtra2);
                    if ("PIN".equals(stringExtra2)) {
                        state = IccCardConstants.State.PIN_REQUIRED;
                    } else if ("PUK".equals(stringExtra2)) {
                        state = IccCardConstants.State.PUK_REQUIRED;
                    } else if ("NETWORK".equals(stringExtra2)) {
                        i = 0;
                        state = IccCardConstants.State.NETWORK_LOCKED;
                    } else if ("NETWORK_SUBSET".equals(stringExtra2)) {
                        i = 1;
                        state = IccCardConstants.State.NETWORK_LOCKED;
                    } else if ("SERVICE_PROVIDER".equals(stringExtra2)) {
                        i = 2;
                        state = IccCardConstants.State.NETWORK_LOCKED;
                    } else if ("CORPORATE".equals(stringExtra2)) {
                        i = 3;
                        state = IccCardConstants.State.NETWORK_LOCKED;
                    } else if ("SIM".equals(stringExtra2)) {
                        i = 4;
                        state = IccCardConstants.State.NETWORK_LOCKED;
                    } else {
                        state = IccCardConstants.State.UNKNOWN;
                    }
                } else {
                    state = "NETWORK".equals(stringExtra) ? IccCardConstants.State.NETWORK_LOCKED : ("LOADED".equals(stringExtra) || "IMSI".equals(stringExtra)) ? IccCardConstants.State.READY : "NOT_READY".equals(stringExtra) ? IccCardConstants.State.NOT_READY : IccCardConstants.State.UNKNOWN;
                }
                return new SimData(state, intExtra, intExtra2, i);
            }
            throw new IllegalArgumentException("only handles intent ACTION_SIM_STATE_CHANGED");
        }

        public String toString() {
            return this.simState.toString();
        }
    }

    /* loaded from: a.zip:com/android/keyguard/KeyguardUpdateMonitor$StrongAuthTracker.class */
    public class StrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
        final KeyguardUpdateMonitor this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public StrongAuthTracker(KeyguardUpdateMonitor keyguardUpdateMonitor, Context context) {
            super(context);
            this.this$0 = keyguardUpdateMonitor;
        }

        public boolean hasUserAuthenticatedSinceBoot() {
            boolean z = false;
            if ((getStrongAuthForUser(KeyguardUpdateMonitor.getCurrentUser()) & 1) == 0) {
                z = true;
            }
            return z;
        }

        public boolean isUnlockingWithFingerprintAllowed() {
            return isFingerprintAllowedForUser(KeyguardUpdateMonitor.getCurrentUser());
        }

        public void onStrongAuthRequiredChanged(int i) {
            this.this$0.notifyStrongAuthStateChanged(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/keyguard/KeyguardUpdateMonitor$simMeStatusQueryThread.class */
    public class simMeStatusQueryThread extends Thread {
        SimData simArgs;
        final KeyguardUpdateMonitor this$0;

        simMeStatusQueryThread(KeyguardUpdateMonitor keyguardUpdateMonitor, SimData simData) {
            this.this$0 = keyguardUpdateMonitor;
            this.simArgs = simData;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                this.this$0.mSimMeCategory.put(Integer.valueOf(this.simArgs.phoneId), Integer.valueOf(this.simArgs.simMECategory));
                Log.d("KeyguardUpdateMonitor", "queryNetworkLock, phoneId =" + this.simArgs.phoneId + ", simMECategory =" + this.simArgs.simMECategory);
                if (this.simArgs.simMECategory < 0 || this.simArgs.simMECategory > 5) {
                    return;
                }
                Bundle queryNetworkLock = ITelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx")).queryNetworkLock(KeyguardUtils.getSubIdUsingPhoneId(this.simArgs.phoneId), this.simArgs.simMECategory);
                boolean z = queryNetworkLock.getBoolean("com.mediatek.phone.QUERY_SIMME_LOCK_RESULT", false);
                Log.d("KeyguardUpdateMonitor", "queryNetworkLock, query_result =" + z);
                if (z) {
                    this.this$0.mSimMeLeftRetryCount.put(Integer.valueOf(this.simArgs.phoneId), Integer.valueOf(queryNetworkLock.getInt("com.mediatek.phone.SIMME_LOCK_LEFT_COUNT", 5)));
                } else {
                    Log.e("KeyguardUpdateMonitor", "queryIccNetworkLock result fail");
                }
                this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(304, this.simArgs));
            } catch (Exception e) {
                Log.e("KeyguardUpdateMonitor", "queryIccNetworkLock got exception: " + e.getMessage());
            }
        }
    }

    /* renamed from: -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m648xf663cf59() {
        if (f7x8dbfd0b5 != null) {
            return f7x8dbfd0b5;
        }
        int[] iArr = new int[IccCardConstants.State.values().length];
        try {
            iArr[IccCardConstants.State.ABSENT.ordinal()] = 4;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[IccCardConstants.State.NOT_READY.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[IccCardConstants.State.PERM_DISABLED.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 3;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[IccCardConstants.State.READY.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[IccCardConstants.State.UNKNOWN.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        f7x8dbfd0b5 = iArr;
        return iArr;
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:13:0x02e8 -> B:7:0x0280). Please submit an issue!!! */
    private KeyguardUpdateMonitor(Context context) {
        this.mContext = context;
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        this.mStrongAuthTracker = new StrongAuthTracker(this, context);
        Log.d("KeyguardUpdateMonitor", "mDeviceProvisioned is:" + this.mDeviceProvisioned);
        if (!this.mDeviceProvisioned) {
            watchForDeviceProvisioning();
        }
        this.mBatteryStatus = new BatteryStatus(1, 100, 0, 0, 0);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        initMembers();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        intentFilter.addAction("mediatek.intent.action.ACTION_UNLOCK_SIM_LOCK");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.setPriority(1000);
        intentFilter2.addAction("android.intent.action.BOOT_COMPLETED");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter3.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter3.addAction("com.android.facelock.FACE_UNLOCK_STARTED");
        intentFilter3.addAction("com.android.facelock.FACE_UNLOCK_STOPPED");
        intentFilter3.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        context.registerReceiverAsUser(this.mBroadcastAllReceiver, UserHandle.ALL, intentFilter3, null, null);
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new IUserSwitchObserver.Stub(this) { // from class: com.android.keyguard.KeyguardUpdateMonitor.8
                final KeyguardUpdateMonitor this$0;

                {
                    this.this$0 = this;
                }

                public void onForegroundProfileSwitch(int i) {
                }

                public void onUserSwitchComplete(int i) throws RemoteException {
                    this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(314, i, 0));
                }

                public void onUserSwitching(int i, IRemoteCallback iRemoteCallback) {
                    this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(310, i, 0, iRemoteCallback));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction("com.android.systemui.ACTION_STRONG_AUTH_TIMEOUT");
        context.registerReceiver(this.mStrongAuthTimeoutReceiver, intentFilter4, "com.android.systemui.permission.SELF", null);
        this.mTrustManager = (TrustManager) context.getSystemService("trust");
        this.mTrustManager.registerTrustListener(this);
        new LockPatternUtils(context).registerStrongAuthTracker(this.mStrongAuthTracker);
        this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        updateFingerprintListeningState();
        if (this.mFpm != null) {
            this.mFpm.addLockoutResetCallback(this.mLockoutResetCallback);
        }
    }

    public static int getCurrentUser() {
        int i;
        synchronized (KeyguardUpdateMonitor.class) {
            try {
                i = sCurrentUser;
            } catch (Throwable th) {
                throw th;
            }
        }
        return i;
    }

    public static KeyguardUpdateMonitor getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KeyguardUpdateMonitor(context);
        }
        return sInstance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public CharSequence getTelephonyCsgIdFrom(Intent intent) {
        return intent.getStringExtra("csgId");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public CharSequence getTelephonyHnbNameFrom(Intent intent) {
        return intent.getStringExtra("hnbName");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public CharSequence getTelephonyPlmnFrom(Intent intent) {
        if (intent.getBooleanExtra("showPlmn", false)) {
            String stringExtra = intent.getStringExtra("plmn");
            if (stringExtra == null) {
                stringExtra = getDefaultPlmn();
            }
            return stringExtra;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public CharSequence getTelephonySpnFrom(Intent intent) {
        String stringExtra;
        if (!intent.getBooleanExtra("showSpn", false) || (stringExtra = intent.getStringExtra("spn")) == null) {
            return null;
        }
        return stringExtra;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAirPlaneModeUpdate(boolean z) {
        if (!z) {
            for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
                setPinPukMeDismissFlagOfPhoneId(i, false);
                Log.d("KeyguardUpdateMonitor", "setPinPukMeDismissFlagOfPhoneId false: " + i);
            }
            for (int i2 = 0; i2 < KeyguardUtils.getNumOfPhone(); i2++) {
                Log.d("KeyguardUpdateMonitor", "phoneId = " + i2 + " state=" + this.mSimStateOfPhoneId.get(Integer.valueOf(i2)));
                if (this.mSimStateOfPhoneId.get(Integer.valueOf(i2)) != null && !this.mSimStateOfPhoneId.get(Integer.valueOf(i2)).equals("")) {
                    switch (m648xf663cf59()[this.mSimStateOfPhoneId.get(Integer.valueOf(i2)).ordinal()]) {
                        case 1:
                        case 2:
                        case 3:
                            IccCardConstants.State state = this.mSimStateOfPhoneId.get(Integer.valueOf(i2));
                            this.mSimStateOfPhoneId.put(Integer.valueOf(i2), IccCardConstants.State.UNKNOWN);
                            SimData simData = new SimData(state, i2, KeyguardUtils.getSubIdUsingPhoneId(i2), this.mSimMeCategory.get(Integer.valueOf(i2)) != null ? this.mSimMeCategory.get(Integer.valueOf(i2)).intValue() : 0);
                            Log.v("KeyguardUpdateMonitor", "SimData state=" + simData.simState + ", phoneId=" + simData.phoneId + ", subId=" + simData.subId + ", SimData.simMECategory = " + simData.simMECategory);
                            proceedToHandleSimStateChanged(simData);
                            continue;
                    }
                }
            }
        } else if (z && KeyguardUtils.isFlightModePowerOffMd()) {
            Log.d("KeyguardUpdateMonitor", "Air mode is on, supress all SIM PIN/PUK/ME Lock views.");
            for (int i3 = 0; i3 < KeyguardUtils.getNumOfPhone(); i3++) {
                setPinPukMeDismissFlagOfPhoneId(i3, true);
                Log.d("KeyguardUpdateMonitor", "setPinPukMeDismissFlagOfPhoneId true: " + i3);
            }
        }
        for (int i4 = 0; i4 < this.mCallbacks.size(); i4++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i4).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onAirPlaneModeChanged(z);
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAirplaneModeChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleBatteryUpdate(BatteryStatus batteryStatus) {
        Log.d("KeyguardUpdateMonitor", "handleBatteryUpdate");
        boolean isBatteryUpdateInteresting = isBatteryUpdateInteresting(this.mBatteryStatus, batteryStatus);
        this.mBatteryStatus = batteryStatus;
        if (isBatteryUpdateInteresting) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onRefreshBatteryInfo(batteryStatus);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleCarrierInfoUpdate(int i) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceUnlockStateChanged(boolean z, int i) {
        Log.d("KeyguardUpdateMonitor", "handleFaceUnlockStateChanged(running = " + z + " , userId = " + i);
        this.mUserFaceUnlockRunning.put(i, z);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFaceUnlockStateChanged(z, i);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintAcquired(int i) {
        if (i != 0) {
            return;
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintAcquired();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintAuthFailed() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintAuthFailed();
            }
        }
        handleFingerprintHelp(-1, this.mContext.getString(R$string.fingerprint_not_recognized));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintAuthenticated(int i) {
        try {
            int i2 = ActivityManagerNative.getDefault().getCurrentUser().id;
            if (i2 != i) {
                Log.d("KeyguardUpdateMonitor", "Fingerprint authenticated for wrong user: " + i);
            } else if (isFingerprintDisabled(i2)) {
                Log.d("KeyguardUpdateMonitor", "Fingerprint disabled by DPM for userId: " + i2);
            } else {
                onFingerprintAuthenticated(i2);
            }
        } catch (RemoteException e) {
            Log.e("KeyguardUpdateMonitor", "Failed to get current user id: ", e);
        } finally {
            setFingerprintRunningState(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintError(int i, String str) {
        if (i == 5 && this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(0);
            startListeningForFingerprint();
        } else {
            setFingerprintRunningState(0);
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintError(i, str);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintHelp(int i, String str) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintHelp(i, str);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintLockoutReset() {
        updateFingerprintListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleKeyguardBouncerChanged(int i) {
        Log.d("KeyguardUpdateMonitor", "handleKeyguardBouncerChanged(" + i + ")");
        boolean z = i == 1;
        this.mBouncer = z;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardBouncerChanged(z);
            }
        }
        updateFingerprintListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleKeyguardReset() {
        Log.d("KeyguardUpdateMonitor", "handleKeyguardReset");
        updateFingerprintListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleReportEmergencyCallAction() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onEmergencyCallAction();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScreenTurnedOff() {
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOff();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScreenTurnedOn() {
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOn();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleServiceStateChange(int i, ServiceState serviceState) {
        Log.d("KeyguardUpdateMonitor", "handleServiceStateChange(subId=" + i + ", serviceState=" + serviceState);
        this.mServiceStates.put(Integer.valueOf(i), serviceState);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSimStateChange(SimData simData) {
        IccCardConstants.State state = simData.simState;
        Log.d("KeyguardUpdateMonitor", "handleSimStateChange: intentValue = " + simData + " state resolved to " + state.toString() + " phoneId=" + simData.phoneId);
        if (state != IccCardConstants.State.UNKNOWN) {
            if (state == IccCardConstants.State.NETWORK_LOCKED || state != this.mSimStateOfPhoneId.get(Integer.valueOf(simData.phoneId))) {
                this.mSimStateOfPhoneId.put(Integer.valueOf(simData.phoneId), state);
                int i = simData.phoneId;
                printState();
                for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                    KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
                    if (keyguardUpdateMonitorCallback != null) {
                        keyguardUpdateMonitorCallback.onSimStateChangedUsingPhoneId(i, state);
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleTimeUpdate() {
        Log.d("KeyguardUpdateMonitor", "handleTimeUpdate");
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUserInfoChanged(int i) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserInfoChanged(i);
            }
        }
    }

    private void initMembers() {
        Log.d("KeyguardUpdateMonitor", "initMembers() - NumOfPhone=" + KeyguardUtils.getNumOfPhone());
        for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
            this.mSimStateOfPhoneId.put(Integer.valueOf(i), IccCardConstants.State.UNKNOWN);
            this.mTelephonyPlmn.put(Integer.valueOf(i), getDefaultPlmn());
            this.mTelephonyCsgId.put(Integer.valueOf(i), "");
            this.mTelephonyHnbName.put(Integer.valueOf(i), "");
            this.mSimMeCategory.put(Integer.valueOf(i), 0);
            this.mSimMeLeftRetryCount.put(Integer.valueOf(i), 5);
        }
    }

    private boolean isAirplaneModeOn() {
        boolean z = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z = false;
        }
        return z;
    }

    private static boolean isBatteryUpdateInteresting(BatteryStatus batteryStatus, BatteryStatus batteryStatus2) {
        boolean isPluggedIn = batteryStatus2.isPluggedIn();
        boolean isPluggedIn2 = batteryStatus.isPluggedIn();
        boolean z = (isPluggedIn2 && isPluggedIn) ? batteryStatus.status != batteryStatus2.status : false;
        if (isPluggedIn2 == isPluggedIn && !z && batteryStatus.level == batteryStatus2.level) {
            if (isPluggedIn || !batteryStatus2.isBatteryLow() || batteryStatus2.level == batteryStatus.level) {
                return isPluggedIn && batteryStatus2.maxChargingWattage != batteryStatus.maxChargingWattage;
            }
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isDeviceProvisionedInSettingsDb() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            z = true;
        }
        return z;
    }

    private boolean isFingerprintDisabled(int i) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        return (devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures(null, i) & 32) == 0) ? isSimPinSecure() : true;
    }

    private boolean isTrustDisabled(int i) {
        return isSimPinSecure();
    }

    private boolean isWifiEnabled() {
        boolean z = true;
        int wifiState = this.mWifiManager.getWifiState();
        Log.d("KeyguardUpdateMonitor", "wifi state:" + wifiState);
        if (wifiState == 1) {
            z = false;
        }
        return z;
    }

    private void notifyFingerprintRunningStateChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintRunningStateChanged(isFingerprintDetectionRunning());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyStrongAuthStateChanged(int i) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStrongAuthStateChanged(i);
            }
        }
    }

    private void onFingerprintAuthenticated(int i) {
        this.mUserFingerprintAuthenticated.put(i, true);
        this.mFingerprintAlreadyAuthenticated = isUnlockingWithFingerprintAllowed();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintAuthenticated(i);
            }
        }
    }

    private void printState() {
        for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
            Log.d("KeyguardUpdateMonitor", "Phone# " + i + ", state = " + this.mSimStateOfPhoneId.get(Integer.valueOf(i)));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void proceedToHandleSimStateChanged(SimData simData) {
        if (IccCardConstants.State.NETWORK_LOCKED == simData.simState && KeyguardUtils.isMediatekSimMeLockSupport()) {
            new simMeStatusQueryThread(this, simData).start();
        } else {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(304, simData));
        }
    }

    private boolean refreshSimState(int i, int i2) {
        IccCardConstants.State state;
        int simState = TelephonyManager.from(this.mContext).getSimState(i2);
        try {
            state = IccCardConstants.State.intToState(simState);
        } catch (IllegalArgumentException e) {
            Log.w("KeyguardUpdateMonitor", "Unknown sim state: " + simState);
            state = IccCardConstants.State.UNKNOWN;
        }
        IccCardConstants.State state2 = this.mSimStateOfPhoneId.get(Integer.valueOf(i2));
        boolean z = state2 != state;
        boolean z2 = z;
        if (state2 == IccCardConstants.State.READY) {
            z2 = z;
            if (state == IccCardConstants.State.PIN_REQUIRED) {
                z2 = false;
            }
        }
        if (z2) {
            this.mSimStateOfPhoneId.put(Integer.valueOf(i2), state);
        }
        Log.d("KeyguardUpdateMonitor", "refreshSimState() - sub = " + i + " phoneId = " + i2 + ", ori-state = " + state2 + ", new-state = " + state + ", changed = " + z2);
        return z2;
    }

    private void scheduleStrongAuthTimeout() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        Intent intent = new Intent("com.android.systemui.ACTION_STRONG_AUTH_TIMEOUT");
        intent.putExtra("com.android.systemui.USER_ID", sCurrentUser);
        this.mAlarmManager.set(3, elapsedRealtime + 259200000, PendingIntent.getBroadcast(this.mContext, sCurrentUser, intent, 268435456));
        notifyStrongAuthStateChanged(sCurrentUser);
    }

    private void sendUpdates(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        keyguardUpdateMonitorCallback.onRefreshBatteryInfo(this.mBatteryStatus);
        keyguardUpdateMonitorCallback.onTimeChanged();
        keyguardUpdateMonitorCallback.onRingerModeChanged(this.mRingMode);
        keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
        keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
        keyguardUpdateMonitorCallback.onClockVisibilityChanged();
        for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
            keyguardUpdateMonitorCallback.onSimStateChangedUsingPhoneId(i, this.mSimStateOfPhoneId.get(Integer.valueOf(i)));
        }
    }

    public static void setCurrentUser(int i) {
        synchronized (KeyguardUpdateMonitor.class) {
            try {
                sCurrentUser = i;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void setFingerprintRunningState(int i) {
        boolean z = this.mFingerprintRunningState == 1;
        boolean z2 = i == 1;
        this.mFingerprintRunningState = i;
        if (z != z2) {
            notifyFingerprintRunningStateChanged();
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:9:0x001d, code lost:
        if (r3.mGoingToSleep != false) goto L12;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean shouldListenForFingerprint() {
        boolean z;
        if (!this.mKeyguardIsVisible && this.mDeviceInteractive && !this.mBouncer) {
            z = false;
        }
        if (this.mSwitchingUser) {
            z = false;
        } else {
            z = false;
            if (!this.mFingerprintAlreadyAuthenticated) {
                z = false;
                if (!isFingerprintDisabled(getCurrentUser())) {
                    z = true;
                }
            }
        }
        return z;
    }

    private void startListeningForFingerprint() {
        if (this.mFingerprintRunningState == 2) {
            setFingerprintRunningState(3);
            return;
        }
        Log.v("KeyguardUpdateMonitor", "startListeningForFingerprint()");
        int currentUser = ActivityManager.getCurrentUser();
        if (isUnlockWithFingerprintPossible(currentUser)) {
            if (this.mFingerprintCancelSignal != null) {
                this.mFingerprintCancelSignal.cancel();
            }
            this.mFingerprintCancelSignal = new CancellationSignal();
            this.mFpm.authenticate(null, this.mFingerprintCancelSignal, 0, this.mAuthenticationCallback, null, currentUser);
            setFingerprintRunningState(1);
        }
    }

    private void stopListeningForFingerprint() {
        Log.v("KeyguardUpdateMonitor", "stopListeningForFingerprint()");
        if (this.mFingerprintRunningState == 1) {
            this.mFingerprintCancelSignal.cancel();
            this.mFingerprintCancelSignal = null;
            setFingerprintRunningState(2);
        }
        if (this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(2);
        }
    }

    private void updateFingerprintListeningState() {
        boolean shouldListenForFingerprint = shouldListenForFingerprint();
        if (this.mFingerprintRunningState == 1 && !shouldListenForFingerprint) {
            stopListeningForFingerprint();
        } else if (this.mFingerprintRunningState == 1 || !shouldListenForFingerprint) {
        } else {
            startListeningForFingerprint();
        }
    }

    private void watchForDeviceProvisioning() {
        this.mDeviceProvisionedObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.keyguard.KeyguardUpdateMonitor.9
            final KeyguardUpdateMonitor this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                this.this$0.mDeviceProvisioned = this.this$0.isDeviceProvisionedInSettingsDb();
                if (this.this$0.mDeviceProvisioned) {
                    this.this$0.mHandler.sendEmptyMessage(308);
                }
                Log.d("KeyguardUpdateMonitor", "DEVICE_PROVISIONED state = " + this.this$0.mDeviceProvisioned);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
        boolean isDeviceProvisionedInSettingsDb = isDeviceProvisionedInSettingsDb();
        if (isDeviceProvisionedInSettingsDb != this.mDeviceProvisioned) {
            this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb;
            if (this.mDeviceProvisioned) {
                this.mHandler.sendEmptyMessage(308);
            }
        }
    }

    public void clearFailedUnlockAttempts() {
        this.mFailedAttempts.delete(sCurrentUser);
        this.mFailedBiometricUnlockAttempts = 0;
    }

    public void clearFingerprintRecognized() {
        this.mUserFingerprintAuthenticated.clear();
    }

    public void dispatchBootCompleted() {
        this.mHandler.sendEmptyMessage(313);
    }

    public void dispatchFinishedGoingToSleep(int i) {
        synchronized (this) {
            this.mDeviceInteractive = false;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(320, i, 0));
    }

    public void dispatchScreenTurnedOff() {
        synchronized (this) {
            this.mScreenOn = false;
        }
        this.mHandler.sendEmptyMessage(332);
    }

    public void dispatchScreenTurnedOn() {
        synchronized (this) {
            this.mScreenOn = true;
        }
        this.mHandler.sendEmptyMessage(331);
    }

    public void dispatchStartedGoingToSleep(int i) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(321, i, 0));
    }

    public void dispatchStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = true;
        }
        this.mHandler.sendEmptyMessage(319);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        int intValue;
        printWriter.println("KeyguardUpdateMonitor state:");
        printWriter.println("  SIM States:");
        Iterator<T> it = this.mSimDatas.values().iterator();
        while (it.hasNext()) {
            printWriter.println("    " + ((SimData) it.next()).toString());
        }
        printWriter.println("  Subs:");
        if (this.mSubscriptionInfo != null) {
            for (int i = 0; i < this.mSubscriptionInfo.size(); i++) {
                printWriter.println("    " + this.mSubscriptionInfo.get(i));
            }
        }
        printWriter.println("  Service states:");
        for (Integer num : this.mServiceStates.keySet()) {
            printWriter.println("    " + num.intValue() + "=" + this.mServiceStates.get(Integer.valueOf(intValue)));
        }
        if (this.mFpm == null || !this.mFpm.isHardwareDetected()) {
            return;
        }
        int currentUser = ActivityManager.getCurrentUser();
        int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
        printWriter.println("  Fingerprint state (user=" + currentUser + ")");
        printWriter.println("    allowed=" + isUnlockingWithFingerprintAllowed());
        printWriter.println("    auth'd=" + this.mUserFingerprintAuthenticated.get(currentUser));
        printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
        printWriter.println("    disabled(DPM)=" + isFingerprintDisabled(currentUser));
        printWriter.println("    possible=" + isUnlockWithFingerprintPossible(currentUser));
        printWriter.println("    strongAuthFlags=" + Integer.toHexString(strongAuthForUser));
        printWriter.println("    timedout=" + hasFingerprintUnlockTimedOut(currentUser));
        printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser));
    }

    public CharSequence getDefaultPlmn() {
        return this.mContext.getResources().getText(R$string.keyguard_carrier_default);
    }

    public int getFailedUnlockAttempts(int i) {
        return this.mFailedAttempts.get(i, 0);
    }

    public boolean getMaxBiometricUnlockAttemptsReached() {
        return this.mFailedBiometricUnlockAttempts >= 3;
    }

    public int getPhoneState() {
        return this.mPhoneState;
    }

    public boolean getPinPukMeDismissFlagOfPhoneId(int i) {
        int i2 = 1 << i;
        return (this.mPinPukMeDismissFlag & i2) == i2;
    }

    public int getRetryPukCountOfPhoneId(int i) {
        return i == 3 ? SystemProperties.getInt("gsm.sim.retry.puk1.4", -1) : i == 2 ? SystemProperties.getInt("gsm.sim.retry.puk1.3", -1) : i == 1 ? SystemProperties.getInt("gsm.sim.retry.puk1.2", -1) : SystemProperties.getInt("gsm.sim.retry.puk1", -1);
    }

    public int getSimMeCategoryOfPhoneId(int i) {
        return this.mSimMeCategory.get(Integer.valueOf(i)).intValue();
    }

    public int getSimMeLeftRetryCountOfPhoneId(int i) {
        return this.mSimMeLeftRetryCount.get(Integer.valueOf(i)).intValue();
    }

    public int getSimPinLockPhoneId() {
        int i;
        int i2 = 0;
        while (true) {
            i = -1;
            if (i2 >= KeyguardUtils.getNumOfPhone()) {
                break;
            }
            Log.d("KeyguardUpdateMonitor", "getSimPinLockSubId, phoneId=" + i2 + " mSimStateOfPhoneId.get(phoneId)=" + this.mSimStateOfPhoneId.get(Integer.valueOf(i2)));
            if (this.mSimStateOfPhoneId.get(Integer.valueOf(i2)) == IccCardConstants.State.PIN_REQUIRED && !getPinPukMeDismissFlagOfPhoneId(i2)) {
                i = i2;
                break;
            }
            i2++;
        }
        return i;
    }

    public int getSimPukLockPhoneId() {
        int i;
        int i2 = 0;
        while (true) {
            i = -1;
            if (i2 >= KeyguardUtils.getNumOfPhone()) {
                break;
            }
            Log.d("KeyguardUpdateMonitor", "getSimPukLockSubId, phoneId=" + i2 + " mSimStateOfSub.get(phoneId)=" + this.mSimStateOfPhoneId.get(Integer.valueOf(i2)));
            if (this.mSimStateOfPhoneId.get(Integer.valueOf(i2)) == IccCardConstants.State.PUK_REQUIRED && !getPinPukMeDismissFlagOfPhoneId(i2) && getRetryPukCountOfPhoneId(i2) != 0) {
                i = i2;
                break;
            }
            i2++;
        }
        return i;
    }

    public IccCardConstants.State getSimStateOfPhoneId(int i) {
        return this.mSimStateOfPhoneId.get(Integer.valueOf(i));
    }

    public StrongAuthTracker getStrongAuthTracker() {
        return this.mStrongAuthTracker;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x001b, code lost:
        if (r0.size() == 0) goto L16;
     */
    /* JADX WARN: Removed duplicated region for block: B:13:0x002a  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x005d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public List<SubscriptionInfo> getSubscriptionInfo(boolean z) {
        List<SubscriptionInfo> activeSubscriptionInfoList;
        List<SubscriptionInfo> list = this.mSubscriptionInfo;
        if (list != null && !z) {
            activeSubscriptionInfoList = list;
            if (list != null) {
                activeSubscriptionInfoList = list;
            }
            if (activeSubscriptionInfoList != null) {
                this.mSubscriptionInfo = new ArrayList();
            } else {
                this.mSubscriptionInfo = activeSubscriptionInfoList;
            }
            Log.d("KeyguardUpdateMonitor", "getSubscriptionInfo() - mSubscriptionInfo.size = " + this.mSubscriptionInfo.size());
            return this.mSubscriptionInfo;
        }
        activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList != null) {
        }
        Log.d("KeyguardUpdateMonitor", "getSubscriptionInfo() - mSubscriptionInfo.size = " + this.mSubscriptionInfo.size());
        return this.mSubscriptionInfo;
    }

    public SubscriptionInfo getSubscriptionInfoForSubId(int i) {
        return getSubscriptionInfoForSubId(i, false);
    }

    public SubscriptionInfo getSubscriptionInfoForSubId(int i, boolean z) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(z);
        for (int i2 = 0; i2 < subscriptionInfo.size(); i2++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i2);
            if (i == subscriptionInfo2.getSubscriptionId()) {
                return subscriptionInfo2;
            }
        }
        return null;
    }

    public boolean getUserCanSkipBouncer(int i) {
        return !getUserHasTrust(i) ? this.mUserFingerprintAuthenticated.get(i) ? isUnlockingWithFingerprintAllowed() : false : true;
    }

    public boolean getUserHasTrust(int i) {
        return !isTrustDisabled(i) ? this.mUserHasTrust.get(i) : false;
    }

    public boolean getUserTrustIsManaged(int i) {
        boolean z = false;
        if (this.mUserTrustIsManaged.get(i)) {
            z = !isTrustDisabled(i);
        }
        return z;
    }

    protected void handleBootCompleted() {
        if (this.mBootCompleted) {
            return;
        }
        this.mBootCompleted = true;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBootCompleted();
            }
        }
    }

    protected void handleDevicePolicyManagerStateChanged() {
        updateFingerprintListeningState();
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(size).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDevicePolicyManagerStateChanged();
            }
        }
    }

    protected void handleDeviceProvisioned() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDeviceProvisioned();
            }
        }
        if (this.mDeviceProvisionedObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDeviceProvisionedObserver);
            this.mDeviceProvisionedObserver = null;
        }
    }

    protected void handleFinishedGoingToSleep(int i) {
        this.mGoingToSleep = false;
        int size = this.mCallbacks.size();
        for (int i2 = 0; i2 < size; i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFinishedGoingToSleep(i);
            }
        }
        updateFingerprintListeningState();
    }

    protected void handlePhoneStateChanged() {
        Log.d("KeyguardUpdateMonitor", "handlePhoneStateChanged");
        this.mPhoneState = 0;
        for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
            int callState = TelephonyManager.getDefault().getCallState(KeyguardUtils.getSubIdUsingPhoneId(i));
            if (callState == 2) {
                this.mPhoneState = callState;
            } else if (callState == 1 && this.mPhoneState == 0) {
                this.mPhoneState = callState;
            }
        }
        Log.d("KeyguardUpdateMonitor", "handlePhoneStateChanged() - mPhoneState = " + this.mPhoneState);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
            }
        }
    }

    protected void handleRingerModeChange(int i) {
        Log.d("KeyguardUpdateMonitor", "handleRingerModeChange(" + i + ")");
        this.mRingMode = i;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRingerModeChanged(i);
            }
        }
    }

    protected void handleSimSubscriptionInfoChanged() {
        Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged()");
        List<SubscriptionInfo> activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList != null) {
            Iterator<T> it = activeSubscriptionInfoList.iterator();
            while (it.hasNext()) {
                Log.v("KeyguardUpdateMonitor", "SubInfo:" + ((SubscriptionInfo) it.next()));
            }
        } else {
            Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged: list is null");
        }
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(true);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < subscriptionInfo.size(); i++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i);
            if (refreshSimState(subscriptionInfo2.getSubscriptionId(), subscriptionInfo2.getSimSlotIndex())) {
                arrayList.add(subscriptionInfo2);
            }
        }
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            int subscriptionId = ((SubscriptionInfo) arrayList.get(i2)).getSubscriptionId();
            int simSlotIndex = ((SubscriptionInfo) arrayList.get(i2)).getSimSlotIndex();
            Log.d("KeyguardUpdateMonitor", "handleSimSubscriptionInfoChanged() - call callbacks for subId = " + subscriptionId + " & phoneId = " + simSlotIndex);
            for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onSimStateChangedUsingPhoneId(simSlotIndex, this.mSimStateOfPhoneId.get(Integer.valueOf(simSlotIndex)));
                }
            }
        }
        for (int i4 = 0; i4 < this.mCallbacks.size(); i4++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback2 = this.mCallbacks.get(i4).get();
            if (keyguardUpdateMonitorCallback2 != null) {
                keyguardUpdateMonitorCallback2.onRefreshCarrierInfo();
            }
        }
    }

    protected void handleStartedGoingToSleep(int i) {
        clearFingerprintRecognized();
        int size = this.mCallbacks.size();
        for (int i2 = 0; i2 < size; i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedGoingToSleep(i);
            }
        }
        this.mGoingToSleep = true;
        this.mFingerprintAlreadyAuthenticated = false;
        updateFingerprintListeningState();
    }

    protected void handleStartedWakingUp() {
        Log.d("KeyguardUpdateMonitor", "handleStartedWakingUp");
        updateFingerprintListeningState();
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedWakingUp();
            }
        }
    }

    protected void handleUserSwitchComplete(int i) {
        this.mSwitchingUser = false;
        updateFingerprintListeningState();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitchComplete(i);
            }
        }
    }

    protected void handleUserSwitching(int i, IRemoteCallback iRemoteCallback) {
        this.mSwitchingUser = true;
        updateFingerprintListeningState();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitching(i);
            }
        }
        try {
            iRemoteCallback.sendResult((Bundle) null);
        } catch (RemoteException e) {
        }
    }

    public boolean hasFingerprintUnlockTimedOut(int i) {
        return !this.mStrongAuthNotTimedOut.contains(Integer.valueOf(i));
    }

    public boolean isAlternateUnlockEnabled() {
        return this.mAlternateUnlockEnabled;
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    public boolean isDeviceProvisioned() {
        boolean z = false;
        if (this.mDeviceProvisioned) {
            Log.d("KeyguardUpdateMonitor", "mDeviceProvisioned == true");
            return this.mDeviceProvisioned;
        }
        Log.d("KeyguardUpdateMonitor", "isDeviceProvisioned get DEVICE_PROVISIONED from db again !!");
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            z = true;
        }
        return z;
    }

    public boolean isFaceUnlockRunning(int i) {
        return this.mUserFaceUnlockRunning.get(i);
    }

    public boolean isFingerprintDetectionRunning() {
        boolean z = true;
        if (this.mFingerprintRunningState != 1) {
            z = false;
        }
        return z;
    }

    public boolean isGoingToSleep() {
        return this.mGoingToSleep;
    }

    public boolean isSimPinSecure() {
        boolean z;
        int i = 0;
        while (true) {
            z = false;
            if (i >= KeyguardUtils.getNumOfPhone()) {
                break;
            } else if (isSimPinSecure(i)) {
                z = true;
                break;
            } else {
                i++;
            }
        }
        return z;
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x003d, code lost:
        if (com.android.keyguard.KeyguardUtils.isMediatekSimMeLockSupport() != false) goto L12;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean isSimPinSecure(int i) {
        boolean z;
        IccCardConstants.State state = this.mSimStateOfPhoneId.get(Integer.valueOf(i));
        if (state != IccCardConstants.State.PIN_REQUIRED && state != IccCardConstants.State.PUK_REQUIRED) {
            z = false;
            if (state == IccCardConstants.State.NETWORK_LOCKED) {
                z = false;
            }
            return z;
        }
        z = !getPinPukMeDismissFlagOfPhoneId(i);
        return z;
    }

    public boolean isSimPinVoiceSecure() {
        return isSimPinSecure();
    }

    public boolean isUnlockWithFingerprintPossible(int i) {
        boolean z = false;
        if (this.mFpm != null) {
            z = false;
            if (this.mFpm.isHardwareDetected()) {
                if (isFingerprintDisabled(i)) {
                    z = false;
                } else {
                    z = false;
                    if (this.mFpm.getEnrolledFingerprints(i).size() > 0) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public boolean isUnlockingWithFingerprintAllowed() {
        boolean z = false;
        if (this.mStrongAuthTracker.isUnlockingWithFingerprintAllowed()) {
            z = !hasFingerprintUnlockTimedOut(sCurrentUser);
        }
        return z;
    }

    public void minusSimMeLeftRetryCountOfPhoneId(int i) {
        int intValue = this.mSimMeLeftRetryCount.get(Integer.valueOf(i)).intValue();
        if (intValue > 0) {
            this.mSimMeLeftRetryCount.put(Integer.valueOf(i), Integer.valueOf(intValue - 1));
        }
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        Log.d("KeyguardUpdateMonitor", "onKeyguardVisibilityChanged(" + z + ")");
        this.mKeyguardIsVisible = z;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(z);
            }
        }
        if (!z) {
            this.mFingerprintAlreadyAuthenticated = false;
        }
        updateFingerprintListeningState();
    }

    public void onTrustChanged(boolean z, int i, int i2) {
        this.mUserHasTrust.put(i, z);
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustChanged(i);
                if (z && i2 != 0) {
                    keyguardUpdateMonitorCallback.onTrustGrantedWithFlags(i2, i);
                }
            }
        }
    }

    public void onTrustManagedChanged(boolean z, int i) {
        this.mUserTrustIsManaged.put(i, z);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustManagedChanged(i);
            }
        }
    }

    public void registerCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Log.v("KeyguardUpdateMonitor", "*** register callback for " + keyguardUpdateMonitorCallback);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == keyguardUpdateMonitorCallback) {
                Log.e("KeyguardUpdateMonitor", "Object tried to add another callback", new Exception("Called by"));
                return;
            }
        }
        this.mCallbacks.add(new WeakReference<>(keyguardUpdateMonitorCallback));
        removeCallback(null);
        sendUpdates(keyguardUpdateMonitorCallback);
        this.mNewClientRegUpdateMonitor = true;
    }

    public void removeCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Log.v("KeyguardUpdateMonitor", "*** unregister callback for " + keyguardUpdateMonitorCallback);
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            if (this.mCallbacks.get(size).get() == keyguardUpdateMonitorCallback) {
                this.mCallbacks.remove(size);
            }
        }
    }

    public void reportEmergencyCallAction(boolean z) {
        if (z) {
            handleReportEmergencyCallAction();
        } else {
            this.mHandler.obtainMessage(318).sendToTarget();
        }
    }

    public void reportFailedBiometricUnlockAttempt() {
        this.mFailedBiometricUnlockAttempts++;
    }

    public void reportFailedStrongAuthUnlockAttempt(int i) {
        this.mFailedAttempts.put(i, getFailedUnlockAttempts(i) + 1);
    }

    public void reportSimUnlocked(int i) {
        handleSimStateChange(new SimData(IccCardConstants.State.READY, i, KeyguardUtils.getSubIdUsingPhoneId(i)));
    }

    public void reportSuccessfulStrongAuthUnlockAttempt() {
        this.mStrongAuthNotTimedOut.add(Integer.valueOf(sCurrentUser));
        scheduleStrongAuthTimeout();
        if (this.mFpm != null) {
            this.mFpm.resetTimeout(null);
        }
    }

    public void sendKeyguardBouncerChanged(boolean z) {
        Log.d("KeyguardUpdateMonitor", "sendKeyguardBouncerChanged(" + z + ")");
        Message obtainMessage = this.mHandler.obtainMessage(322);
        obtainMessage.arg1 = z ? 1 : 0;
        obtainMessage.sendToTarget();
    }

    public void sendKeyguardReset() {
        this.mHandler.obtainMessage(312).sendToTarget();
    }

    public void setAlternateUnlockEnabled(boolean z) {
        Log.d("KeyguardUpdateMonitor", "setAlternateUnlockEnabled(enabled = " + z + ")");
        this.mAlternateUnlockEnabled = z;
    }

    public void setDismissFlagWhenWfcOn(IccCardConstants.State state) {
        if ((state == IccCardConstants.State.PIN_REQUIRED || state == IccCardConstants.State.PUK_REQUIRED || state == IccCardConstants.State.NETWORK_LOCKED) && isAirplaneModeOn() && isWifiEnabled() && KeyguardUtils.isFlightModePowerOffMd()) {
            for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
                setPinPukMeDismissFlagOfPhoneId(i, false);
                Log.d("KeyguardUpdateMonitor", "Wifi calling opened MD, setPinPukMeDismissFlagOfPhoneId false: " + i);
            }
        }
    }

    public void setPinPukMeDismissFlagOfPhoneId(int i, boolean z) {
        Log.d("KeyguardUpdateMonitor", "setPinPukMeDismissFlagOfPhoneId() - phoneId = " + i);
        if (KeyguardUtils.isValidPhoneId(i)) {
            int i2 = 1 << i;
            if (z) {
                this.mPinPukMeDismissFlag |= i2;
            } else {
                this.mPinPukMeDismissFlag &= i2 ^ (-1);
            }
        }
    }
}
