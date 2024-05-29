package com.android.keyguard;

import android.content.Context;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager;
/* loaded from: a.zip:com/android/keyguard/KeyguardSecurityModel.class */
public class KeyguardSecurityModel {

    /* renamed from: -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues  reason: not valid java name */
    private static final int[] f4x1cbe7e58 = null;
    private final Context mContext;
    private final boolean mIsPukScreenAvailable;
    private LockPatternUtils mLockPatternUtils;

    /* loaded from: a.zip:com/android/keyguard/KeyguardSecurityModel$SecurityMode.class */
    public enum SecurityMode {
        Invalid,
        None,
        Pattern,
        Password,
        PIN,
        SimPinPukMe1,
        SimPinPukMe2,
        SimPinPukMe3,
        SimPinPukMe4,
        AlarmBoot,
        Biometric,
        Voice,
        AntiTheft;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static SecurityMode[] valuesCustom() {
            return values();
        }
    }

    /* renamed from: -getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m615xec5d63fc() {
        if (f4x1cbe7e58 != null) {
            return f4x1cbe7e58;
        }
        int[] iArr = new int[SecurityMode.valuesCustom().length];
        try {
            iArr[SecurityMode.AlarmBoot.ordinal()] = 7;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SecurityMode.AntiTheft.ordinal()] = 8;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SecurityMode.Biometric.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SecurityMode.Invalid.ordinal()] = 9;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SecurityMode.None.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SecurityMode.PIN.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SecurityMode.Password.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SecurityMode.Pattern.ordinal()] = 13;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SecurityMode.SimPinPukMe1.ordinal()] = 2;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SecurityMode.SimPinPukMe2.ordinal()] = 3;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SecurityMode.SimPinPukMe3.ordinal()] = 4;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SecurityMode.SimPinPukMe4.ordinal()] = 5;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[SecurityMode.Voice.ordinal()] = 6;
        } catch (NoSuchFieldError e13) {
        }
        f4x1cbe7e58 = iArr;
        return iArr;
    }

    public KeyguardSecurityModel(Context context) {
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mIsPukScreenAvailable = this.mContext.getResources().getBoolean(17956936);
    }

    private boolean isBiometricUnlockSuppressed() {
        KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        boolean z = keyguardUpdateMonitor.getFailedUnlockAttempts(KeyguardUpdateMonitor.getCurrentUser()) >= 4;
        boolean z2 = true;
        if (!keyguardUpdateMonitor.getMaxBiometricUnlockAttemptsReached()) {
            z2 = true;
            if (!z) {
                z2 = true;
                if (keyguardUpdateMonitor.isAlternateUnlockEnabled()) {
                    z2 = keyguardUpdateMonitor.getPhoneState() != 0;
                }
            }
        }
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SecurityMode getAlternateFor(SecurityMode securityMode) {
        if (!isBiometricUnlockSuppressed() && (securityMode == SecurityMode.Password || securityMode == SecurityMode.PIN || securityMode == SecurityMode.Pattern)) {
            if (isBiometricUnlockEnabled()) {
                return SecurityMode.Biometric;
            }
            if (this.mLockPatternUtils.usingVoiceWeak()) {
                return SecurityMode.Voice;
            }
        }
        return securityMode;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getPhoneIdUsingSecurityMode(SecurityMode securityMode) {
        int i = -1;
        if (isSimPinPukSecurityMode(securityMode)) {
            i = securityMode.ordinal() - SecurityMode.SimPinPukMe1.ordinal();
        }
        return i;
    }

    public SecurityMode getSecurityMode() {
        SecurityMode securityMode;
        Log.d("KeyguardSecurityModel", "getSecurityMode() is called.");
        KeyguardUpdateMonitor.getInstance(this.mContext);
        SecurityMode securityMode2 = SecurityMode.None;
        if (PowerOffAlarmManager.isAlarmBoot()) {
            securityMode = SecurityMode.AlarmBoot;
        } else {
            int i = 0;
            while (true) {
                securityMode = securityMode2;
                if (i >= KeyguardUtils.getNumOfPhone()) {
                    break;
                } else if (!isPinPukOrMeRequiredOfPhoneId(i)) {
                    i++;
                } else if (i == 0) {
                    securityMode = SecurityMode.SimPinPukMe1;
                } else if (1 == i) {
                    securityMode = SecurityMode.SimPinPukMe2;
                } else if (2 == i) {
                    securityMode = SecurityMode.SimPinPukMe3;
                } else {
                    securityMode = securityMode2;
                    if (3 == i) {
                        securityMode = SecurityMode.SimPinPukMe4;
                    }
                }
            }
        }
        SecurityMode securityMode3 = securityMode;
        if (AntiTheftManager.isAntiTheftPriorToSecMode(securityMode)) {
            Log.d("KeyguardSecurityModel", "should show AntiTheft!");
            securityMode3 = SecurityMode.AntiTheft;
        }
        if (securityMode3 != SecurityMode.None) {
            Log.d("KeyguardSecurityModel", "getSecurityMode() - mode = " + securityMode3);
            return securityMode3;
        }
        int activePasswordQuality = this.mLockPatternUtils.getActivePasswordQuality(KeyguardUpdateMonitor.getCurrentUser());
        Log.d("KeyguardSecurityModel", "getSecurityMode() - security = " + activePasswordQuality);
        switch (activePasswordQuality) {
            case 0:
                return SecurityMode.None;
            case 65536:
                return SecurityMode.Pattern;
            case 131072:
            case 196608:
                return SecurityMode.PIN;
            case 262144:
            case 327680:
            case 393216:
                return SecurityMode.Password;
            default:
                throw new IllegalStateException("Unknown security quality:" + activePasswordQuality);
        }
    }

    boolean isBiometricUnlockEnabled() {
        return this.mLockPatternUtils.usingBiometricWeak();
    }

    public boolean isPinPukOrMeRequiredOfPhoneId(int i) {
        boolean z;
        KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        if (keyguardUpdateMonitor != null) {
            IccCardConstants.State simStateOfPhoneId = keyguardUpdateMonitor.getSimStateOfPhoneId(i);
            Log.d("KeyguardSecurityModel", "isPinPukOrMeRequiredOfSubId() - phoneId = " + i + ", simState = " + simStateOfPhoneId);
            if ((simStateOfPhoneId != IccCardConstants.State.PIN_REQUIRED || keyguardUpdateMonitor.getPinPukMeDismissFlagOfPhoneId(i)) && (simStateOfPhoneId != IccCardConstants.State.PUK_REQUIRED || keyguardUpdateMonitor.getPinPukMeDismissFlagOfPhoneId(i) || keyguardUpdateMonitor.getRetryPukCountOfPhoneId(i) == 0)) {
                z = false;
                if (simStateOfPhoneId == IccCardConstants.State.NETWORK_LOCKED) {
                    if (keyguardUpdateMonitor.getPinPukMeDismissFlagOfPhoneId(i)) {
                        z = false;
                    } else {
                        z = false;
                        if (keyguardUpdateMonitor.getSimMeLeftRetryCountOfPhoneId(i) != 0) {
                            z = KeyguardUtils.isMediatekSimMeLockSupport();
                        }
                    }
                }
            } else {
                z = true;
            }
            return z;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isSimPinPukSecurityMode(SecurityMode securityMode) {
        switch (m615xec5d63fc()[securityMode.ordinal()]) {
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }
}
