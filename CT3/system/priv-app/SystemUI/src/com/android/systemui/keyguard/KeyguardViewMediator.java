package com.android.systemui.keyguard;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardDisplayManager;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardUtils;
import com.android.keyguard.R$string;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.phone.FingerprintUnlockController;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import com.mediatek.keyguard.Plugin.KeyguardPluginFactory;
import com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager;
import com.mediatek.keyguard.Telephony.KeyguardDialogManager;
import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/keyguard/KeyguardViewMediator.class */
public class KeyguardViewMediator extends SystemUI {
    private static final Intent USER_PRESENT_INTENT = new Intent("android.intent.action.USER_PRESENT").addFlags(603979776);
    private static boolean mKeyguardDoneOnGoing = false;
    private static final boolean sIsUserBuild = SystemProperties.get("ro.build.type").equals("user");
    private AlarmManager mAlarmManager;
    private AntiTheftManager mAntiTheftManager;
    private AudioManager mAudioManager;
    private boolean mBootCompleted;
    private boolean mBootSendUserPresent;
    private int mDelayedProfileShowingSequence;
    private int mDelayedShowingSequence;
    private boolean mDeviceInteractive;
    private KeyguardDialogManager mDialogManager;
    private IKeyguardDrawnCallback mDrawnCallback;
    private IKeyguardExitCallback mExitSecureCallback;
    private boolean mGoingToSleep;
    private Animation mHideAnimation;
    private boolean mHiding;
    private boolean mInputRestricted;
    private boolean mIsPerUserLock;
    private KeyguardDisplayManager mKeyguardDisplayManager;
    private boolean mLockLater;
    private LockPatternUtils mLockPatternUtils;
    private int mLockSoundId;
    private int mLockSoundStreamId;
    private float mLockSoundVolume;
    private SoundPool mLockSounds;
    private PowerManager mPM;
    private boolean mPendingLock;
    private boolean mPendingReset;
    private PowerOffAlarmManager mPowerOffAlarmManager;
    private SearchManager mSearchManager;
    private KeyguardSecurityModel mSecurityModel;
    private PowerManager.WakeLock mShowKeyguardWakeLock;
    private boolean mShowing;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private StatusBarManager mStatusBarManager;
    private boolean mSwitchingUser;
    private boolean mSystemReady;
    private TrustManager mTrustManager;
    private int mTrustedSoundId;
    private int mUiSoundsStreamType;
    private int mUnlockSoundId;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private VoiceWakeupManager mVoiceWakeupManager;
    private IWindowManager mWM;
    private boolean mWakeAndUnlocking;
    private boolean mSuppressNextLockSound = true;
    private boolean mExternallyEnabled = true;
    private boolean mNeedToReshowWhenReenabled = false;
    private boolean mReadyToShow = false;
    private boolean mOccluded = false;
    private String mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
    private boolean mWaitingUntilKeyguardVisible = false;
    private boolean mKeyguardDonePending = false;
    private boolean mHideAnimationRun = false;
    private final ArrayList<IKeyguardStateCallback> mKeyguardStateCallbacks = new ArrayList<>();
    KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.1

        /* renamed from: -com-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
        private static final int[] f8x8dbfd0b5 = null;
        final KeyguardViewMediator this$0;

        /* renamed from: -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
        private static /* synthetic */ int[] m989xf663cf59() {
            if (f8x8dbfd0b5 != null) {
                return f8x8dbfd0b5;
            }
            int[] iArr = new int[IccCardConstants.State.values().length];
            try {
                iArr[IccCardConstants.State.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 8;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[IccCardConstants.State.NOT_READY.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[IccCardConstants.State.PERM_DISABLED.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 5;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 6;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[IccCardConstants.State.READY.ordinal()] = 7;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[IccCardConstants.State.UNKNOWN.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            f8x8dbfd0b5 = iArr;
            return iArr;
        }

        {
            this.this$0 = this;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onClockVisibilityChanged() {
            this.this$0.adjustStatusBarLocked();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onDeviceProvisioned() {
            this.this$0.sendUserPresentBroadcast();
            synchronized (this.this$0) {
                if (UserManager.isSplitSystemUser() && KeyguardUpdateMonitor.getCurrentUser() == 0) {
                    this.this$0.doKeyguardLocked(null);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintAuthFailed() {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (this.this$0.mLockPatternUtils.isSecure(currentUser)) {
                this.this$0.mLockPatternUtils.getDevicePolicyManager().reportFailedFingerprintAttempt(currentUser);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintAuthenticated(int i) {
            if (this.this$0.mLockPatternUtils.isSecure(i)) {
                this.this$0.mLockPatternUtils.getDevicePolicyManager().reportSuccessfulFingerprintAttempt(i);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onPhoneStateChanged(int i) {
            synchronized (this.this$0) {
                if (i == 0) {
                    if (!this.this$0.mDeviceInteractive && this.this$0.mExternallyEnabled) {
                        Log.d("KeyguardViewMediator", "screen is off and call ended, let's make sure the keyguard is showing");
                    }
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
            Log.d("KeyguardViewMediator", "onSimStateChangedUsingSubId: " + state + ", phoneId=" + i);
            switch (m989xf663cf59()[state.ordinal()]) {
                case 1:
                case 3:
                    synchronized (this) {
                        if (this.this$0.shouldWaitForProvisioning()) {
                            if (this.this$0.mShowing) {
                                this.this$0.resetStateLocked();
                            } else {
                                Log.d("KeyguardViewMediator", "ICC_ABSENT isn't showing, we need to show the keyguard since the device isn't provisioned yet.");
                                this.this$0.doKeyguardLocked(null);
                            }
                        }
                        break;
                    }
                case 2:
                case 5:
                case 6:
                    synchronized (this) {
                        if (state == IccCardConstants.State.NETWORK_LOCKED && !KeyguardUtils.isMediatekSimMeLockSupport()) {
                            Log.d("KeyguardViewMediator", "Get NETWORK_LOCKED but not support ME lock. Not show.");
                        } else if (KeyguardUtils.isSystemEncrypted()) {
                            Log.d("KeyguardViewMediator", "Currently system needs to be decrypted. Not show.");
                        } else {
                            this.this$0.mUpdateMonitor.setDismissFlagWhenWfcOn(state);
                            if (this.this$0.mUpdateMonitor.getRetryPukCountOfPhoneId(i) == 0) {
                                this.this$0.mDialogManager.requestShowDialog(new InvalidDialogCallback(this.this$0, null));
                            } else if (IccCardConstants.State.NETWORK_LOCKED == state && this.this$0.mUpdateMonitor.getSimMeLeftRetryCountOfPhoneId(i) == 0) {
                                Log.d("KeyguardViewMediator", "SIM ME lock retrycount is 0, only to show dialog");
                                this.this$0.mDialogManager.requestShowDialog(new MeLockedDialogCallback(this.this$0, null));
                            } else if (!this.this$0.isShowing()) {
                                Log.d("KeyguardViewMediator", "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing; need to show keyguard so user can enter sim pin");
                                this.this$0.doKeyguardLocked(null);
                            } else if (KeyguardViewMediator.mKeyguardDoneOnGoing) {
                                Log.d("KeyguardViewMediator", "mKeyguardDoneOnGoing is true");
                                this.this$0.doKeyguardLaterLocked();
                            } else {
                                this.this$0.removeKeyguardDoneMsg();
                                this.this$0.resetStateLocked();
                            }
                        }
                        break;
                    }
                    break;
                case 4:
                    synchronized (this) {
                        if (this.this$0.mShowing) {
                            Log.d("KeyguardViewMediator", "PERM_DISABLED, resetStateLocked toshow permanently disabled message in lockscreen.");
                            this.this$0.resetStateLocked();
                        } else {
                            Log.d("KeyguardViewMediator", "PERM_DISABLED and keygaurd isn't showing.");
                            this.this$0.doKeyguardLocked(null);
                        }
                        break;
                    }
                case 7:
                    break;
                default:
                    Log.v("KeyguardViewMediator", "Ignoring state: " + state);
                    break;
            }
            try {
                int size = this.this$0.mKeyguardStateCallbacks.size();
                boolean isSimPinSecure = this.this$0.mUpdateMonitor.isSimPinSecure();
                for (int i2 = 0; i2 < size; i2++) {
                    ((IKeyguardStateCallback) this.this$0.mKeyguardStateCallbacks.get(i2)).onSimSecureStateChanged(isSimPinSecure);
                }
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onSimSecureStateChanged", e);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserInfoChanged(int i) {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            UserInfo userInfo;
            this.this$0.mSwitchingUser = false;
            if (i == 0 || (userInfo = UserManager.get(this.this$0.mContext).getUserInfo(i)) == null || !userInfo.isGuest()) {
                return;
            }
            this.this$0.dismiss();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitching(int i) {
            synchronized (this.this$0) {
                this.this$0.mSwitchingUser = true;
                this.this$0.resetKeyguardDonePendingLocked();
                this.this$0.resetStateLocked();
                this.this$0.adjustStatusBarLocked();
            }
        }
    };
    ViewMediatorCallback mViewMediatorCallback = new ViewMediatorCallback(this) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.2
        final KeyguardViewMediator this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void adjustStatusBarLocked() {
            this.this$0.adjustStatusBarLocked();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void dismiss(boolean z) {
            this.this$0.dismiss(z);
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public int getBouncerPromptReason() {
            int currentUser = ActivityManager.getCurrentUser();
            boolean isTrustUsuallyManaged = this.this$0.mTrustManager.isTrustUsuallyManaged(currentUser);
            boolean isUnlockWithFingerprintPossible = this.this$0.mUpdateMonitor.isUnlockWithFingerprintPossible(currentUser);
            boolean z = !isTrustUsuallyManaged ? isUnlockWithFingerprintPossible : true;
            KeyguardUpdateMonitor.StrongAuthTracker strongAuthTracker = this.this$0.mUpdateMonitor.getStrongAuthTracker();
            int strongAuthForUser = strongAuthTracker.getStrongAuthForUser(currentUser);
            if (!z || strongAuthTracker.hasUserAuthenticatedSinceBoot()) {
                if (isUnlockWithFingerprintPossible && this.this$0.mUpdateMonitor.hasFingerprintUnlockTimedOut(currentUser)) {
                    return 2;
                }
                if (!z || (strongAuthForUser & 2) == 0) {
                    if (!isTrustUsuallyManaged || (strongAuthForUser & 4) == 0) {
                        if (!z || (strongAuthForUser & 8) == 0) {
                            return (!isTrustUsuallyManaged || (strongAuthForUser & 16) == 0) ? 0 : 6;
                        }
                        return 5;
                    }
                    return 4;
                }
                return 3;
            }
            return 1;
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void hideLocked() {
            this.this$0.hideLocked();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public boolean isInputRestricted() {
            return this.this$0.isInputRestricted();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public boolean isKeyguardDoneOnGoing() {
            return this.this$0.isKeyguardDoneOnGoing();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public boolean isKeyguardExternallyEnabled() {
            return this.this$0.isKeyguardExternallyEnabled();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public boolean isScreenOn() {
            return this.this$0.mDeviceInteractive;
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public boolean isSecure() {
            return this.this$0.isSecure();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public boolean isShowing() {
            return this.this$0.isShowing();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardDone(boolean z) {
            if (!this.this$0.mKeyguardDonePending) {
                this.this$0.keyguardDone(true);
            }
            if (z) {
                this.this$0.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
            }
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardDoneDrawing() {
            this.this$0.mHandler.sendEmptyMessage(10);
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardDonePending(boolean z) {
            this.this$0.mKeyguardDonePending = true;
            this.this$0.mHideAnimationRun = true;
            this.this$0.mStatusBarKeyguardViewManager.startPreHideAnimation(null);
            this.this$0.mHandler.sendEmptyMessageDelayed(20, 3000L);
            if (z) {
                this.this$0.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
            }
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardGone() {
            if (this.this$0.mKeyguardDisplayManager != null) {
                Log.d("KeyguardViewMediator", "keyguard gone, call mKeyguardDisplayManager.hide()");
                this.this$0.mKeyguardDisplayManager.hide();
            } else {
                Log.d("KeyguardViewMediator", "keyguard gone, mKeyguardDisplayManager is null");
            }
            this.this$0.mVoiceWakeupManager.notifyKeyguardIsGone();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void playTrustedSound() {
            this.this$0.playTrustedSound();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void readyForKeyguardDone() {
            if (this.this$0.mKeyguardDonePending) {
                this.this$0.keyguardDone(true);
            }
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void resetKeyguard() {
            resetStateLocked();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void resetStateLocked() {
            this.this$0.resetStateLocked();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void setNeedsInput(boolean z) {
            this.this$0.mStatusBarKeyguardViewManager.setNeedsInput(z);
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void setSuppressPlaySoundFlag() {
            this.this$0.setSuppressPlaySoundFlag();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void showLocked(Bundle bundle) {
            this.this$0.showLocked(bundle);
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void updateAntiTheftLocked() {
            this.this$0.updateAntiTheftLocked();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void updateNavbarStatus() {
            this.this$0.updateNavbarStatus();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void userActivity() {
            this.this$0.userActivity();
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.3
        final KeyguardViewMediator this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            KeyguardViewMediator keyguardViewMediator;
            String action = intent.getAction();
            if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD".equals(action)) {
                int intExtra = intent.getIntExtra("seq", 0);
                Log.d("KeyguardViewMediator", "received DELAYED_KEYGUARD_ACTION with seq = " + intExtra + ", mDelayedShowingSequence = " + this.this$0.mDelayedShowingSequence);
                KeyguardViewMediator keyguardViewMediator2 = this.this$0;
                synchronized (keyguardViewMediator2) {
                    keyguardViewMediator = keyguardViewMediator2;
                    if (this.this$0.mDelayedShowingSequence == intExtra) {
                        this.this$0.mSuppressNextLockSound = true;
                        this.this$0.doKeyguardLocked(null);
                        keyguardViewMediator = keyguardViewMediator2;
                    }
                }
            } else if (!"com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK".equals(intent.getAction())) {
                if ("android.intent.action.ACTION_PRE_SHUTDOWN".equals(action)) {
                    Log.w("KeyguardViewMediator", "PRE_SHUTDOWN: " + action);
                    this.this$0.mSuppressNextLockSound = true;
                    return;
                } else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                    Log.w("KeyguardViewMediator", "IPO_SHUTDOWN: " + action);
                    this.this$0.mIsIPOShutDown = true;
                    this.this$0.mHandler.sendEmptyMessageDelayed(1002, 4000L);
                    return;
                } else if ("android.intent.action.ACTION_PREBOOT_IPO".equals(action)) {
                    Log.w("KeyguardViewMediator", "IPO_BOOTUP: " + action);
                    this.this$0.mIsIPOShutDown = false;
                    for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
                        this.this$0.mUpdateMonitor.setPinPukMeDismissFlagOfPhoneId(i, false);
                        Log.d("KeyguardViewMediator", "setPinPukMeDismissFlagOfPhoneId false: " + i);
                    }
                    return;
                } else {
                    return;
                }
            } else {
                int intExtra2 = intent.getIntExtra("seq", 0);
                int intExtra3 = intent.getIntExtra("android.intent.extra.USER_ID", 0);
                if (intExtra3 == 0) {
                    return;
                }
                KeyguardViewMediator keyguardViewMediator3 = this.this$0;
                synchronized (keyguardViewMediator3) {
                    keyguardViewMediator = keyguardViewMediator3;
                    if (this.this$0.mDelayedProfileShowingSequence == intExtra2) {
                        this.this$0.lockProfile(intExtra3);
                        keyguardViewMediator = keyguardViewMediator3;
                    }
                }
            }
        }
    };
    private Handler mHandler = new Handler(this, Looper.myLooper(), null, true) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.4
        final KeyguardViewMediator this$0;

        {
            this.this$0 = this;
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 2:
                    this.this$0.handleShow((Bundle) message.obj);
                    return;
                case 3:
                    this.this$0.handleHide();
                    return;
                case 4:
                    this.this$0.handleReset();
                    return;
                case 5:
                    this.this$0.handleVerifyUnlock();
                    return;
                case 6:
                    this.this$0.handleNotifyFinishedGoingToSleep();
                    return;
                case 7:
                    this.this$0.handleNotifyScreenTurningOn((IKeyguardDrawnCallback) message.obj);
                    return;
                case 8:
                case 11:
                case 14:
                case 15:
                case 16:
                default:
                    return;
                case 9:
                    KeyguardViewMediator keyguardViewMediator = this.this$0;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    keyguardViewMediator.handleKeyguardDone(z);
                    return;
                case 10:
                    this.this$0.handleKeyguardDoneDrawing();
                    return;
                case 12:
                    this.this$0.handleSetOccluded(message.arg1 != 0);
                    return;
                case 13:
                    synchronized (this.this$0) {
                        Log.d("KeyguardViewMediator", "doKeyguardLocked, because:KEYGUARD_TIMEOUT");
                        this.this$0.doKeyguardLocked((Bundle) message.obj);
                    }
                    return;
                case 17:
                    this.this$0.handleDismiss(((Boolean) message.obj).booleanValue());
                    return;
                case 18:
                    StartKeyguardExitAnimParams startKeyguardExitAnimParams = (StartKeyguardExitAnimParams) message.obj;
                    this.this$0.handleStartKeyguardExitAnimation(startKeyguardExitAnimParams.startTime, startKeyguardExitAnimParams.fadeoutDuration);
                    FalsingManager.getInstance(this.this$0.mContext).onSucccessfulUnlock();
                    return;
                case 19:
                    break;
                case 20:
                    Log.w("KeyguardViewMediator", "Timeout while waiting for activity drawn!");
                    break;
                case 21:
                    this.this$0.handleNotifyStartedWakingUp();
                    return;
                case 22:
                    this.this$0.handleNotifyScreenTurnedOn();
                    return;
                case 23:
                    this.this$0.handleNotifyScreenTurnedOff();
                    return;
                case 24:
                    this.this$0.handleNotifyStartedGoingToSleep();
                    return;
            }
            this.this$0.handleOnActivityDrawn();
        }
    };
    private final Runnable mKeyguardGoingAwayRunnable = new Runnable(this) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.5
        final KeyguardViewMediator this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.this$0.mStatusBarKeyguardViewManager.keyguardGoingAway();
                int i = 0;
                if (this.this$0.mStatusBarKeyguardViewManager.shouldDisableWindowAnimationsForUnlock() || this.this$0.mWakeAndUnlocking) {
                    i = 2;
                }
                int i2 = i;
                if (this.this$0.mStatusBarKeyguardViewManager.isGoingToNotificationShade()) {
                    i2 = i | 1;
                }
                int i3 = i2;
                if (this.this$0.mStatusBarKeyguardViewManager.isUnlockWithWallpaper()) {
                    i3 = i2 | 4;
                }
                ActivityManagerNative.getDefault().keyguardGoingAway(i3);
            } catch (RemoteException e) {
                Log.e("KeyguardViewMediator", "Error while calling WindowManager", e);
            }
        }
    };
    private boolean mIsIPOShutDown = false;

    /* loaded from: a.zip:com/android/systemui/keyguard/KeyguardViewMediator$InvalidDialogCallback.class */
    private class InvalidDialogCallback implements KeyguardDialogManager.DialogShowCallBack {
        final KeyguardViewMediator this$0;

        private InvalidDialogCallback(KeyguardViewMediator keyguardViewMediator) {
            this.this$0 = keyguardViewMediator;
        }

        /* synthetic */ InvalidDialogCallback(KeyguardViewMediator keyguardViewMediator, InvalidDialogCallback invalidDialogCallback) {
            this(keyguardViewMediator);
        }

        @Override // com.mediatek.keyguard.Telephony.KeyguardDialogManager.DialogShowCallBack
        public void show() {
            this.this$0.createDialog(this.this$0.mContext.getString(2131493984), this.this$0.mContext.getString(2131493985)).show();
        }
    }

    /* loaded from: a.zip:com/android/systemui/keyguard/KeyguardViewMediator$MeLockedDialogCallback.class */
    private class MeLockedDialogCallback implements KeyguardDialogManager.DialogShowCallBack {
        final KeyguardViewMediator this$0;

        private MeLockedDialogCallback(KeyguardViewMediator keyguardViewMediator) {
            this.this$0 = keyguardViewMediator;
        }

        /* synthetic */ MeLockedDialogCallback(KeyguardViewMediator keyguardViewMediator, MeLockedDialogCallback meLockedDialogCallback) {
            this(keyguardViewMediator);
        }

        @Override // com.mediatek.keyguard.Telephony.KeyguardDialogManager.DialogShowCallBack
        public void show() {
            this.this$0.createDialog(null, this.this$0.mContext.getString(R$string.simlock_slot_locked_message)).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/keyguard/KeyguardViewMediator$StartKeyguardExitAnimParams.class */
    public static class StartKeyguardExitAnimParams {
        long fadeoutDuration;
        long startTime;

        private StartKeyguardExitAnimParams(long j, long j2) {
            this.startTime = j;
            this.fadeoutDuration = j2;
        }

        /* synthetic */ StartKeyguardExitAnimParams(long j, long j2, StartKeyguardExitAnimParams startKeyguardExitAnimParams) {
            this(j, j2);
        }
    }

    private void cancelDoKeyguardForChildProfilesLocked() {
        this.mDelayedProfileShowingSequence++;
    }

    private void cancelDoKeyguardLaterLocked() {
        this.mDelayedShowingSequence++;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AlertDialog createDialog(String str, String str2) {
        AlertDialog create = new AlertDialog.Builder(this.mContext).setTitle(str).setIcon(17301543).setCancelable(false).setMessage(str2).setNegativeButton(R$string.ok, new DialogInterface.OnClickListener(this) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.7
            final KeyguardViewMediator this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mDialogManager.reportDialogClose();
                Log.d("KeyguardViewMediator", "invalid sim card ,reportCloseDialog");
            }
        }).create();
        create.getWindow().setType(2003);
        return create;
    }

    private void doKeyguardForChildProfilesLocked() {
        int[] enabledProfileIds;
        for (int i : UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId())) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i)) {
                lockProfile(i);
            }
        }
    }

    private void doKeyguardLaterForChildProfilesLocked() {
        int[] enabledProfileIds;
        for (int i : UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId())) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i)) {
                long lockTimeout = getLockTimeout(i);
                if (lockTimeout == 0) {
                    doKeyguardForChildProfilesLocked();
                } else {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
                    intent.putExtra("seq", this.mDelayedProfileShowingSequence);
                    intent.putExtra("android.intent.extra.USER_ID", i);
                    intent.addFlags(268435456);
                    this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime + lockTimeout, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doKeyguardLaterLocked() {
        long lockTimeout = getLockTimeout(KeyguardUpdateMonitor.getCurrentUser());
        if (lockTimeout == 0) {
            doKeyguardLocked(null);
        } else {
            doKeyguardLaterLocked(lockTimeout);
        }
    }

    private void doKeyguardLaterLocked(long j) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intent.putExtra("seq", this.mDelayedShowingSequence);
        intent.addFlags(268435456);
        this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime + j, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
        Log.d("KeyguardViewMediator", "setting alarm to turn off keyguard, seq = " + this.mDelayedShowingSequence);
        doKeyguardLaterForChildProfilesLocked();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doKeyguardLocked(Bundle bundle) {
        boolean z;
        if (!this.mExternallyEnabled || PowerOffAlarmManager.isAlarmBoot()) {
            Log.d("KeyguardViewMediator", "doKeyguard : externally disabled reason..mExternallyEnabled = " + this.mExternallyEnabled);
        } else if (this.mStatusBarKeyguardViewManager.isShowing()) {
            resetStateLocked();
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because it is already showing");
        } else {
            if (!UserManager.isSplitSystemUser() || KeyguardUpdateMonitor.getCurrentUser() != 0 || !this.mUpdateMonitor.isDeviceProvisioned()) {
                boolean z2 = !SystemProperties.getBoolean("keyguard.no_require_sim", true);
                boolean isDeviceProvisioned = this.mUpdateMonitor.isDeviceProvisioned();
                int i = 0;
                while (true) {
                    z = false;
                    if (i >= KeyguardUtils.getNumOfPhone()) {
                        break;
                    } else if (isSimLockedOrMissing(i, z2)) {
                        z = true;
                        break;
                    } else {
                        i++;
                    }
                }
                boolean isAntiTheftLocked = AntiTheftManager.isAntiTheftLocked();
                Log.d("KeyguardViewMediator", "lockedOrMissing is " + z + ", requireSim=" + z2 + ", provisioned=" + isDeviceProvisioned + ", antiTheftLocked=" + isAntiTheftLocked);
                if (!z && shouldWaitForProvisioning() && !isAntiTheftLocked) {
                    Log.d("KeyguardViewMediator", "doKeyguard: not showing because device isn't provisioned and the sim is not locked or missing");
                    return;
                } else if (this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) && !z && !isAntiTheftLocked) {
                    Log.d("KeyguardViewMediator", "doKeyguard: not showing because lockscreen is off");
                    return;
                } else if (this.mLockPatternUtils.checkVoldPassword(KeyguardUpdateMonitor.getCurrentUser()) && KeyguardUtils.isSystemEncrypted()) {
                    Log.d("KeyguardViewMediator", "Not showing lock screen since just decrypted");
                    setShowingLocked(false);
                    hideLocked();
                    this.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
                    return;
                }
            }
            Log.d("KeyguardViewMediator", "doKeyguard: showing the lock screen");
            showLocked(bundle);
        }
    }

    private long getLockTimeout(int i) {
        ContentResolver contentResolver;
        long j = Settings.Secure.getInt(this.mContext.getContentResolver(), "lock_screen_lock_after_timeout", 5000);
        long maximumTimeToLockForUserAndProfiles = this.mLockPatternUtils.getDevicePolicyManager().getMaximumTimeToLockForUserAndProfiles(i);
        if (maximumTimeToLockForUserAndProfiles > 0) {
            j = Math.max(Math.min(maximumTimeToLockForUserAndProfiles - Math.max(Settings.System.getInt(contentResolver, "screen_off_timeout", 30000), 0L), j), 0L);
        }
        return j;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleHide() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleHide");
            if (UserManager.isSplitSystemUser() && KeyguardUpdateMonitor.getCurrentUser() == 0) {
                Log.d("KeyguardViewMediator", "Split system user, quit unlocking.");
                return;
            }
            this.mHiding = true;
            if (!this.mShowing || this.mOccluded) {
                handleStartKeyguardExitAnimation(SystemClock.uptimeMillis() + this.mHideAnimation.getStartOffset(), this.mHideAnimation.getDuration());
            } else if (this.mHideAnimationRun) {
                this.mKeyguardGoingAwayRunnable.run();
            } else {
                this.mStatusBarKeyguardViewManager.startPreHideAnimation(this.mKeyguardGoingAwayRunnable);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleKeyguardDone(boolean z) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (this.mLockPatternUtils.isSecure(currentUser)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardDismissed(currentUser);
        }
        Log.d("KeyguardViewMediator", "handleKeyguardDone");
        synchronized (this) {
            resetKeyguardDonePendingLocked();
        }
        if (AntiTheftManager.isAntiTheftLocked()) {
            Log.d("KeyguardViewMediator", "handleKeyguardDone() - Skip keyguard done! antitheft = " + AntiTheftManager.isAntiTheftLocked() + " or sim = " + this.mUpdateMonitor.isSimPinSecure());
            return;
        }
        mKeyguardDoneOnGoing = true;
        if (z) {
            this.mUpdateMonitor.clearFailedUnlockAttempts();
        }
        this.mUpdateMonitor.clearFingerprintRecognized();
        if (this.mGoingToSleep) {
            Log.i("KeyguardViewMediator", "Device is going to sleep, aborting keyguardDone");
            return;
        }
        if (this.mExitSecureCallback != null) {
            try {
                this.mExitSecureCallback.onKeyguardExitResult(z);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(" + z + ")", e);
            }
            this.mExitSecureCallback = null;
            if (z) {
                this.mExternallyEnabled = true;
                this.mNeedToReshowWhenReenabled = false;
                updateInputRestricted();
            }
        }
        this.mSuppressNextLockSound = false;
        handleHide();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleKeyguardDoneDrawing() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing");
            if (this.mWaitingUntilKeyguardVisible) {
                Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing: notifying mWaitingUntilKeyguardVisible");
                this.mWaitingUntilKeyguardVisible = false;
                notifyAll();
                this.mHandler.removeMessages(10);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNotifyFinishedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyFinishedGoingToSleep");
            this.mStatusBarKeyguardViewManager.onFinishedGoingToSleep();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNotifyScreenTurnedOff() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOff");
            this.mStatusBarKeyguardViewManager.onScreenTurnedOff();
            this.mWakeAndUnlocking = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNotifyScreenTurnedOn() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOn");
            this.mStatusBarKeyguardViewManager.onScreenTurnedOn();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNotifyScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurningOn");
            this.mStatusBarKeyguardViewManager.onScreenTurningOn();
            if (iKeyguardDrawnCallback != null) {
                if (this.mWakeAndUnlocking) {
                    this.mDrawnCallback = iKeyguardDrawnCallback;
                } else {
                    notifyDrawn(iKeyguardDrawnCallback);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNotifyStartedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyStartedGoingToSleep");
            this.mStatusBarKeyguardViewManager.onStartedGoingToSleep();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNotifyStartedWakingUp() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyWakingUp");
            this.mStatusBarKeyguardViewManager.onStartedWakingUp();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOnActivityDrawn() {
        Log.d("KeyguardViewMediator", "handleOnActivityDrawn: mKeyguardDonePending=" + this.mKeyguardDonePending);
        if (this.mKeyguardDonePending) {
            this.mStatusBarKeyguardViewManager.onActivityDrawn();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleReset() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleReset");
            this.mStatusBarKeyguardViewManager.reset();
            adjustStatusBarLocked();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSetOccluded(boolean z) {
        synchronized (this) {
            if (this.mHiding && z) {
                startKeyguardExitAnimation(0L, 0L);
            }
            this.mStatusBarKeyguardViewManager.setOccluded(z);
            updateActivityLockScreenState();
            adjustStatusBarLocked();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleShow(Bundle bundle) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (this.mLockPatternUtils.isSecure(currentUser)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardSecured(currentUser);
        }
        synchronized (this) {
            if (!this.mSystemReady) {
                Log.d("KeyguardViewMediator", "ignoring handleShow because system is not ready.");
                setReadyToShow(false);
                updateActivityLockScreenState();
                return;
            }
            Log.d("KeyguardViewMediator", "handleShow");
            setShowingLocked(true);
            this.mStatusBarKeyguardViewManager.show(bundle);
            this.mHiding = false;
            this.mWakeAndUnlocking = false;
            resetKeyguardDonePendingLocked();
            setReadyToShow(false);
            this.mHideAnimationRun = false;
            updateActivityLockScreenState();
            adjustStatusBarLocked();
            userActivity();
            this.mHandler.postDelayed(new Runnable(this) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.6
                final KeyguardViewMediator this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        ActivityManagerNative.getDefault().closeSystemDialogs("lock");
                    } catch (RemoteException e) {
                        Log.e("KeyguardViewMediator", "handleShow() - error in closeSystemDialogs()");
                    }
                }
            }, 500L);
            if (PowerOffAlarmManager.isAlarmBoot()) {
                this.mPowerOffAlarmManager.startAlarm();
            }
            this.mShowKeyguardWakeLock.release();
            Log.d("KeyguardViewMediator", "handleShow exit");
            if (this.mKeyguardDisplayManager == null) {
                Log.d("KeyguardViewMediator", "handle show mKeyguardDisplayManager is null");
                return;
            }
            Log.d("KeyguardViewMediator", "handle show call mKeyguardDisplayManager.show()");
            this.mKeyguardDisplayManager.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleStartKeyguardExitAnimation(long j, long j2) {
        boolean z = false;
        Log.d("KeyguardViewMediator", "handleStartKeyguardExitAnimation() is called.");
        synchronized (this) {
            if (!this.mHiding) {
                StringBuilder append = new StringBuilder().append("handleStartKeyguardExitAnimation() - returns, !mHiding = ");
                if (!this.mHiding) {
                    z = true;
                }
                Log.d("KeyguardViewMediator", append.append(z).toString());
                return;
            }
            this.mHiding = false;
            if (this.mWakeAndUnlocking && this.mDrawnCallback != null) {
                this.mStatusBarKeyguardViewManager.getViewRootImpl().setReportNextDraw();
                notifyDrawn(this.mDrawnCallback);
            }
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(this.mPhoneState) && this.mShowing) {
                playSounds(false);
            }
            setShowingLocked(false);
            this.mStatusBarKeyguardViewManager.hide(j, j2);
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            updateActivityLockScreenState();
            adjustStatusBarLocked();
            sendUserPresentBroadcast();
            Log.d("KeyguardViewMediator", "set mKeyguardDoneOnGoing = false");
            mKeyguardDoneOnGoing = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleVerifyUnlock() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleVerifyUnlock");
            setShowingLocked(true);
            this.mStatusBarKeyguardViewManager.verifyUnlock();
            updateActivityLockScreenState();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideLocked() {
        Log.d("KeyguardViewMediator", "hideLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
    }

    private boolean isSimLockedOrMissing(int i, boolean z) {
        IccCardConstants.State simStateOfPhoneId = this.mUpdateMonitor.getSimStateOfPhoneId(i);
        if (this.mUpdateMonitor.isSimPinSecure(i)) {
            z = true;
        } else if (simStateOfPhoneId != IccCardConstants.State.ABSENT && simStateOfPhoneId != IccCardConstants.State.PERM_DISABLED) {
            z = false;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void lockProfile(int i) {
        this.mTrustManager.setDeviceLockedForUser(i, true);
    }

    private void maybeSendUserPresentBroadcast() {
        if (this.mSystemReady && this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
            sendUserPresentBroadcast();
        } else if (this.mSystemReady && shouldWaitForProvisioning()) {
            getLockPatternUtils().userPresent(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    private void notifyDrawn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        try {
            iKeyguardDrawnCallback.onDrawn();
        } catch (RemoteException e) {
            Slog.w("KeyguardViewMediator", "Exception calling onDrawn():", e);
        }
    }

    private void notifyFinishedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyFinishedGoingToSleep");
        this.mHandler.sendEmptyMessage(6);
    }

    private void notifyScreenOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Log.d("KeyguardViewMediator", "notifyScreenOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7, iKeyguardDrawnCallback));
    }

    private void notifyScreenTurnedOff() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOff");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23));
    }

    private void notifyScreenTurnedOn() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(22));
    }

    private void notifyStartedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyStartedGoingToSleep");
        this.mHandler.sendEmptyMessage(24);
    }

    private void notifyStartedWakingUp() {
        Log.d("KeyguardViewMediator", "notifyStartedWakingUp");
        this.mHandler.sendEmptyMessage(21);
    }

    private void playSound(int i) {
        if (i != 0 && Settings.System.getInt(this.mContext.getContentResolver(), "lockscreen_sounds_enabled", 1) == 1) {
            this.mLockSounds.stop(this.mLockSoundStreamId);
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
                if (this.mAudioManager == null) {
                    return;
                }
                this.mUiSoundsStreamType = this.mAudioManager.getUiSoundsStreamType();
            }
            if (this.mAudioManager.isStreamMute(this.mUiSoundsStreamType)) {
                return;
            }
            this.mLockSoundStreamId = this.mLockSounds.play(i, this.mLockSoundVolume, this.mLockSoundVolume, 1, 0, 1.0f);
        }
    }

    private void playSounds(boolean z) {
        Log.d("KeyguardViewMediator", "playSounds(locked = " + z + "), mSuppressNextLockSound =" + this.mSuppressNextLockSound);
        if (this.mSuppressNextLockSound) {
            this.mSuppressNextLockSound = false;
        } else {
            playSound(z ? this.mLockSoundId : this.mUnlockSoundId);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void playTrustedSound() {
        if (this.mSuppressNextLockSound) {
            return;
        }
        playSound(this.mTrustedSoundId);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeKeyguardDoneMsg() {
        this.mHandler.removeMessages(9);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetKeyguardDonePendingLocked() {
        this.mKeyguardDonePending = false;
        this.mHandler.removeMessages(20);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetStateLocked() {
        Log.e("KeyguardViewMediator", "resetStateLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendUserPresentBroadcast() {
        synchronized (this) {
            if (this.mBootCompleted) {
                int currentUser = KeyguardUpdateMonitor.getCurrentUser();
                for (int i : ((UserManager) this.mContext.getSystemService("user")).getProfileIdsWithDisabled(new UserHandle(currentUser).getIdentifier())) {
                    this.mContext.sendBroadcastAsUser(USER_PRESENT_INTENT, UserHandle.of(i));
                }
                getLockPatternUtils().userPresent(currentUser);
            } else {
                this.mBootSendUserPresent = true;
            }
        }
    }

    private void setReadyToShow(boolean z) {
        this.mReadyToShow = z;
        Log.d("KeyguardViewMediator", "mReadyToShow set as " + this.mReadyToShow);
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:18:0x009b -> B:13:0x007d). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:19:0x009e -> B:13:0x007d). Please submit an issue!!! */
    private void setShowingLocked(boolean z) {
        Log.d("KeyguardViewMediator", "setShowingLocked() - showing = " + z + ", mShowing = " + this.mShowing);
        if (z != this.mShowing) {
            this.mSecurityModel = new KeyguardSecurityModel(this.mContext);
            KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityModel.getSecurityMode();
            if (SystemProperties.getInt("ro.special", 0) == 1 && securityMode == KeyguardSecurityModel.SecurityMode.None) {
                this.mShowing = false;
            } else {
                this.mShowing = z;
            }
            for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
                try {
                    this.mKeyguardStateCallbacks.get(size).onShowingStateChanged(z);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onShowingStateChanged", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(size);
                    }
                }
            }
            updateInputRestrictedLocked();
            this.mTrustManager.reportKeyguardShowingChanged();
        }
    }

    private void setupLocked() {
        this.mPM = (PowerManager) this.mContext.getSystemService("power");
        this.mWM = WindowManagerGlobal.getWindowManagerService();
        this.mTrustManager = (TrustManager) this.mContext.getSystemService("trust");
        this.mShowKeyguardWakeLock = this.mPM.newWakeLock(1, "show keyguard");
        this.mShowKeyguardWakeLock.setReferenceCounted(false);
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD"));
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK"));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intentFilter.addAction("android.intent.action.ACTION_PRE_SHUTDOWN");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        intentFilter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mKeyguardDisplayManager = new KeyguardDisplayManager(this.mContext);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        KeyguardUpdateMonitor.setCurrentUser(ActivityManager.getCurrentUser());
        setShowingLocked((shouldWaitForProvisioning() || this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) ? false : true);
        updateInputRestrictedLocked();
        this.mTrustManager.reportKeyguardShowingChanged();
        this.mStatusBarKeyguardViewManager = SystemUIFactory.getInstance().createStatusBarKeyguardViewManager(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        this.mDeviceInteractive = this.mPM.isInteractive();
        this.mLockSounds = new SoundPool(1, 1, 0);
        String string = Settings.Global.getString(contentResolver, "lock_sound");
        if (string != null) {
            this.mLockSoundId = this.mLockSounds.load(string, 1);
        }
        if (string == null || this.mLockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load lock sound from " + string);
        }
        String string2 = Settings.Global.getString(contentResolver, "unlock_sound");
        if (string2 != null) {
            this.mUnlockSoundId = this.mLockSounds.load(string2, 1);
        }
        if (string2 == null || this.mUnlockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load unlock sound from " + string2);
        }
        String string3 = Settings.Global.getString(contentResolver, "trusted_sound");
        if (string3 != null) {
            this.mTrustedSoundId = this.mLockSounds.load(string3, 1);
        }
        if (string3 == null || this.mTrustedSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load trusted sound from " + string3);
        }
        this.mLockSoundVolume = (float) Math.pow(10.0d, this.mContext.getResources().getInteger(17694725) / 20.0f);
        this.mHideAnimation = AnimationUtils.loadAnimation(this.mContext, 17432659);
        this.mDialogManager = KeyguardDialogManager.getInstance(this.mContext);
        AntiTheftManager.checkPplStatus();
        this.mAntiTheftManager = AntiTheftManager.getInstance(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils);
        this.mAntiTheftManager.doAntiTheftLockCheck();
        this.mPowerOffAlarmManager = PowerOffAlarmManager.getInstance(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils);
        this.mVoiceWakeupManager = VoiceWakeupManager.getInstance();
        this.mVoiceWakeupManager.init(this.mContext, this.mViewMediatorCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldWaitForProvisioning() {
        boolean z = false;
        if (!this.mUpdateMonitor.isDeviceProvisioned()) {
            z = !isSecure();
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLocked(Bundle bundle) {
        Log.d("KeyguardViewMediator", "showLocked");
        this.mSecurityModel = new KeyguardSecurityModel(this.mContext);
        KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityModel.getSecurityMode();
        if (SystemProperties.getInt("ro.special", 0) == 1 && securityMode == KeyguardSecurityModel.SecurityMode.None) {
            return;
        }
        setReadyToShow(true);
        updateActivityLockScreenState();
        this.mShowKeyguardWakeLock.acquire();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, bundle));
    }

    private void updateActivityLockScreenState() {
        try {
            Log.d("KeyguardViewMediator", "updateActivityLockScreenState() - mShowing = " + this.mShowing + " !mOccluded = " + (!this.mOccluded));
            ActivityManagerNative.getDefault().setLockScreenShown(this.mShowing, this.mOccluded);
        } catch (RemoteException e) {
        }
    }

    private void updateInputRestricted() {
        synchronized (this) {
            updateInputRestrictedLocked();
            Log.d("KeyguardViewMediator", "isInputRestricted: showing=" + this.mShowing + ", needReshow=" + this.mNeedToReshowWhenReenabled + ", provisioned=" + this.mUpdateMonitor.isDeviceProvisioned());
        }
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:12:0x0047 -> B:8:0x0031). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:13:0x004a -> B:8:0x0031). Please submit an issue!!! */
    private void updateInputRestrictedLocked() {
        boolean isInputRestricted = isInputRestricted();
        if (this.mInputRestricted != isInputRestricted) {
            this.mInputRestricted = isInputRestricted;
            for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
                try {
                    this.mKeyguardStateCallbacks.get(size).onInputRestrictedStateChanged(isInputRestricted);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onDeviceProvisioned", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(size);
                    }
                }
            }
        }
    }

    public void addStateMonitorCallback(IKeyguardStateCallback iKeyguardStateCallback) {
        synchronized (this) {
            this.mKeyguardStateCallbacks.add(iKeyguardStateCallback);
            try {
                iKeyguardStateCallback.onSimSecureStateChanged(this.mUpdateMonitor.isSimPinSecure());
                iKeyguardStateCallback.onShowingStateChanged(this.mShowing);
                iKeyguardStateCallback.onInputRestrictedStateChanged(this.mInputRestricted);
                iKeyguardStateCallback.onAntiTheftStateChanged(AntiTheftManager.isAntiTheftLocked());
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onShowingStateChanged or onSimSecureStateChanged or onInputRestrictedStateChanged", e);
            }
        }
    }

    void adjustStatusBarLocked() {
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        }
        if (this.mStatusBarManager == null) {
            Log.w("KeyguardViewMediator", "Could not get status bar manager");
            return;
        }
        isSecure();
        int i = 0;
        if (this.mShowing) {
            i = 16777216;
            if (PowerOffAlarmManager.isAlarmBoot()) {
                i = 16777216 | 33554432;
            }
        }
        int i2 = i;
        if (isShowingAndNotOccluded()) {
            i2 = i | 2097152;
        }
        Log.d("KeyguardViewMediator", "adjustStatusBarLocked: mShowing=" + this.mShowing + " mOccluded=" + this.mOccluded + " isSecure=" + isSecure() + " --> flags=0x" + Integer.toHexString(i2));
        if (this.mContext instanceof Activity) {
            return;
        }
        this.mStatusBarManager.disable(i2);
    }

    public void dismiss() {
        dismiss(false);
    }

    public void dismiss(boolean z) {
        Log.d("KeyguardViewMediator", "dismiss, authenticated = " + z);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17, new Boolean(z)));
    }

    public void doKeyguardTimeout(Bundle bundle) {
        this.mHandler.removeMessages(13);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(13, bundle));
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mSystemReady: ");
        printWriter.println(this.mSystemReady);
        printWriter.print("  mBootCompleted: ");
        printWriter.println(this.mBootCompleted);
        printWriter.print("  mBootSendUserPresent: ");
        printWriter.println(this.mBootSendUserPresent);
        printWriter.print("  mExternallyEnabled: ");
        printWriter.println(this.mExternallyEnabled);
        printWriter.print("  mNeedToReshowWhenReenabled: ");
        printWriter.println(this.mNeedToReshowWhenReenabled);
        printWriter.print("  mShowing: ");
        printWriter.println(this.mShowing);
        printWriter.print("  mInputRestricted: ");
        printWriter.println(this.mInputRestricted);
        printWriter.print("  mOccluded: ");
        printWriter.println(this.mOccluded);
        printWriter.print("  mDelayedShowingSequence: ");
        printWriter.println(this.mDelayedShowingSequence);
        printWriter.print("  mExitSecureCallback: ");
        printWriter.println(this.mExitSecureCallback);
        printWriter.print("  mDeviceInteractive: ");
        printWriter.println(this.mDeviceInteractive);
        printWriter.print("  mGoingToSleep: ");
        printWriter.println(this.mGoingToSleep);
        printWriter.print("  mHiding: ");
        printWriter.println(this.mHiding);
        printWriter.print("  mWaitingUntilKeyguardVisible: ");
        printWriter.println(this.mWaitingUntilKeyguardVisible);
        printWriter.print("  mKeyguardDonePending: ");
        printWriter.println(this.mKeyguardDonePending);
        printWriter.print("  mHideAnimationRun: ");
        printWriter.println(this.mHideAnimationRun);
        printWriter.print("  mPendingReset: ");
        printWriter.println(this.mPendingReset);
        printWriter.print("  mPendingLock: ");
        printWriter.println(this.mPendingLock);
        printWriter.print("  mWakeAndUnlocking: ");
        printWriter.println(this.mWakeAndUnlocking);
        printWriter.print("  mDrawnCallback: ");
        printWriter.println(this.mDrawnCallback);
    }

    public LockPatternUtils getLockPatternUtils() {
        return this.mLockPatternUtils;
    }

    public ViewMediatorCallback getViewMediatorCallback() {
        return this.mViewMediatorCallback;
    }

    public void handleDismiss(boolean z) {
        if (!this.mShowing || this.mOccluded) {
            return;
        }
        this.mStatusBarKeyguardViewManager.dismiss(z);
    }

    public boolean isInputRestricted() {
        if (!sIsUserBuild) {
            Log.d("KeyguardViewMediator", "isInputRestricted: showing=" + this.mShowing + ", needReshow=" + this.mNeedToReshowWhenReenabled);
        }
        return !this.mShowing ? this.mNeedToReshowWhenReenabled : true;
    }

    public boolean isKeyguardDoneOnGoing() {
        return mKeyguardDoneOnGoing;
    }

    public boolean isKeyguardExternallyEnabled() {
        return this.mExternallyEnabled;
    }

    public boolean isSecure() {
        return (this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) || KeyguardUpdateMonitor.getInstance(this.mContext).isSimPinSecure()) ? true : AntiTheftManager.isAntiTheftLocked();
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean isShowingAndNotOccluded() {
        boolean z = false;
        if (this.mShowing) {
            z = !this.mOccluded;
        }
        return z;
    }

    public void keyguardDone(boolean z) {
        Log.d("KeyguardViewMediator", "keyguardDone(" + z + ")");
        EventLog.writeEvent(70000, 2);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9, Integer.valueOf(z ? 1 : 0)));
    }

    public void onActivityDrawn() {
        this.mHandler.sendEmptyMessage(19);
    }

    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        Log.d("KeyguardViewMediator", "onBootCompleted() is called");
        this.mUpdateMonitor.dispatchBootCompleted();
        synchronized (this) {
            this.mBootCompleted = true;
            if (this.mBootSendUserPresent) {
                sendUserPresentBroadcast();
            }
        }
    }

    public void onDreamingStarted() {
        synchronized (this) {
            if (this.mDeviceInteractive && this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                doKeyguardLaterLocked();
            }
        }
    }

    public void onDreamingStopped() {
        synchronized (this) {
            if (this.mDeviceInteractive) {
                cancelDoKeyguardLaterLocked();
            }
        }
    }

    public void onFinishedGoingToSleep(int i, boolean z) {
        Log.d("KeyguardViewMediator", "onFinishedGoingToSleep(" + i + ")");
        synchronized (this) {
            this.mDeviceInteractive = false;
            this.mGoingToSleep = false;
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            notifyFinishedGoingToSleep();
            if (z) {
                Log.i("KeyguardViewMediator", "Camera gesture was triggered, preventing Keyguard locking.");
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:CAMERA_GESTURE_PREVENT_LOCK");
                this.mPendingLock = false;
                this.mPendingReset = false;
            }
            if (this.mPendingReset) {
                resetStateLocked();
                this.mPendingReset = false;
            }
            if (this.mPendingLock) {
                doKeyguardLocked(null);
                this.mPendingLock = false;
            }
            if (!this.mLockLater && !z) {
                doKeyguardForChildProfilesLocked();
            }
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchFinishedGoingToSleep(i);
    }

    public void onScreenTurnedOff() {
        notifyScreenTurnedOff();
        this.mUpdateMonitor.dispatchScreenTurnedOff();
    }

    public void onScreenTurnedOn() {
        notifyScreenTurnedOn();
        this.mUpdateMonitor.dispatchScreenTurnedOn();
    }

    public void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        notifyScreenOn(iKeyguardDrawnCallback);
    }

    public void onStartedGoingToSleep(int i) {
        Log.d("KeyguardViewMediator", "onStartedGoingToSleep(" + i + ")");
        synchronized (this) {
            this.mDeviceInteractive = false;
            this.mGoingToSleep = true;
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            boolean z = !this.mLockPatternUtils.getPowerButtonInstantlyLocks(currentUser) ? !this.mLockPatternUtils.isSecure(currentUser) : true;
            long lockTimeout = getLockTimeout(KeyguardUpdateMonitor.getCurrentUser());
            this.mLockLater = false;
            KeyguardPluginFactory.getKeyguardUtilExt(this.mContext).lockImmediatelyWhenScreenTimeout();
            Log.d("KeyguardViewMediator", "onStartedGoingToSleep(" + i + ") ---ScreenOff mScreenOn = false; After--boolean lockImmediately=" + z + ", mExitSecureCallback=" + this.mExitSecureCallback + ", mShowing=" + this.mShowing + ", mIsIPOShutDown = " + this.mIsIPOShutDown);
            if (this.mExitSecureCallback != null) {
                Log.d("KeyguardViewMediator", "pending exit secure callback cancelled");
                try {
                    this.mExitSecureCallback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                }
                this.mExitSecureCallback = null;
                if (!this.mExternallyEnabled) {
                    hideLocked();
                }
            } else if (this.mShowing) {
                this.mPendingReset = true;
            } else {
                if ((i == 3 && lockTimeout > 0) || (i == 2 && !z && !this.mIsIPOShutDown)) {
                    doKeyguardLaterLocked(lockTimeout);
                    this.mLockLater = true;
                }
                if (i == 4) {
                    Log.d("KeyguardViewMediator", "Screen off because PROX_SENSOR, do not draw lock view.");
                } else if (!this.mLockPatternUtils.isLockScreenDisabled(currentUser)) {
                    this.mPendingLock = true;
                }
            }
            if (this.mPendingLock) {
                playSounds(true);
            }
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchStartedGoingToSleep(i);
        notifyStartedGoingToSleep();
    }

    public void onStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = true;
            cancelDoKeyguardLaterLocked();
            cancelDoKeyguardForChildProfilesLocked();
            Log.d("KeyguardViewMediator", "onStartedWakingUp, seq = " + this.mDelayedShowingSequence);
            notifyStartedWakingUp();
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchStartedWakingUp();
        maybeSendUserPresentBroadcast();
    }

    public void onSystemReady() {
        this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "onSystemReady");
            this.mSystemReady = true;
            doKeyguardLocked(null);
            this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
            this.mPowerOffAlarmManager.onSystemReady();
        }
        this.mIsPerUserLock = StorageManager.isFileEncryptedNativeOrEmulated();
        maybeSendUserPresentBroadcast();
    }

    public void onWakeAndUnlocking() {
        this.mWakeAndUnlocking = true;
        keyguardDone(true);
    }

    public StatusBarKeyguardViewManager registerStatusBar(PhoneStatusBar phoneStatusBar, ViewGroup viewGroup, StatusBarWindowManager statusBarWindowManager, ScrimController scrimController, FingerprintUnlockController fingerprintUnlockController) {
        this.mStatusBarKeyguardViewManager.registerStatusBar(phoneStatusBar, viewGroup, statusBarWindowManager, scrimController, fingerprintUnlockController);
        return this.mStatusBarKeyguardViewManager;
    }

    public void setCurrentUser(int i) {
        KeyguardUpdateMonitor.setCurrentUser(i);
    }

    public void setKeyguardEnabled(boolean z) {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "setKeyguardEnabled(" + z + ")");
            this.mExternallyEnabled = z;
            if (z || !this.mShowing) {
                if (z && this.mNeedToReshowWhenReenabled) {
                    Log.d("KeyguardViewMediator", "previously hidden, reshowing, reenabling status bar expansion");
                    this.mNeedToReshowWhenReenabled = false;
                    updateInputRestrictedLocked();
                    if (this.mExitSecureCallback != null) {
                        Log.d("KeyguardViewMediator", "onKeyguardExitResult(false), resetting");
                        try {
                            this.mExitSecureCallback.onKeyguardExitResult(false);
                        } catch (RemoteException e) {
                            Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                        }
                        this.mExitSecureCallback = null;
                        resetStateLocked();
                    } else {
                        showLocked(null);
                        this.mWaitingUntilKeyguardVisible = true;
                        this.mHandler.sendEmptyMessageDelayed(10, 2000L);
                        Log.d("KeyguardViewMediator", "waiting until mWaitingUntilKeyguardVisible is false");
                        while (this.mWaitingUntilKeyguardVisible) {
                            try {
                                wait();
                            } catch (InterruptedException e2) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        Log.d("KeyguardViewMediator", "done waiting for mWaitingUntilKeyguardVisible");
                    }
                }
            } else if (this.mExitSecureCallback != null) {
                Log.d("KeyguardViewMediator", "in process of verifyUnlock request, ignoring");
            } else {
                Log.d("KeyguardViewMediator", "remembering to reshow, hiding keyguard, disabling status bar expansion");
                this.mNeedToReshowWhenReenabled = true;
                updateInputRestrictedLocked();
                hideLocked();
            }
        }
    }

    public void setOccluded(boolean z) {
        if (this.mOccluded != z) {
            Log.d("KeyguardViewMediator", "setOccluded, mOccluded=" + this.mOccluded + ", isOccluded=" + z);
            this.mOccluded = z;
            this.mHandler.removeMessages(12);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(12, z ? 1 : 0, 0));
        }
    }

    void setSuppressPlaySoundFlag() {
        this.mSuppressNextLockSound = true;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        synchronized (this) {
            setupLocked();
        }
        putComponent(KeyguardViewMediator.class, this);
    }

    public void startKeyguardExitAnimation(long j, long j2) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, new StartKeyguardExitAnimParams(j, j2, null)));
    }

    public void updateAntiTheftLocked() {
        boolean isAntiTheftLocked = AntiTheftManager.isAntiTheftLocked();
        Log.d("KeyguardViewMediator", "updateAntiTheftLocked() - isAntiTheftLocked = " + isAntiTheftLocked);
        try {
            int size = this.mKeyguardStateCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mKeyguardStateCallbacks.get(i).onAntiTheftStateChanged(isAntiTheftLocked);
            }
        } catch (RemoteException e) {
            Slog.w("KeyguardViewMediator", "Failed to call onAntiTheftStateChanged", e);
        }
    }

    void updateNavbarStatus() {
        Log.d("KeyguardViewMediator", "updateNavbarStatus() is called.");
        this.mStatusBarKeyguardViewManager.updateStates();
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    public void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "verifyUnlock");
            if (shouldWaitForProvisioning()) {
                Log.d("KeyguardViewMediator", "ignoring because device isn't provisioned");
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                }
            } else if (this.mExternallyEnabled) {
                Log.w("KeyguardViewMediator", "verifyUnlock called when not externally disabled");
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e2) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e2);
                }
            } else if (this.mExitSecureCallback != null) {
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e3) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e3);
                }
            } else if (isSecure()) {
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e4) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e4);
                }
            } else {
                this.mExternallyEnabled = true;
                this.mNeedToReshowWhenReenabled = false;
                updateInputRestricted();
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(true);
                } catch (RemoteException e5) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e5);
                }
            }
        }
    }
}
