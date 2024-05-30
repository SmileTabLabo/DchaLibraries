package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.IllegalFormatConversionException;
/* loaded from: classes.dex */
public class KeyguardIndicationController {
    private final IBatteryStats mBatteryInfo;
    private int mBatteryLevel;
    private int mChargingSpeed;
    private int mChargingWattage;
    private final Context mContext;
    private final DevicePolicyManager mDevicePolicyManager;
    private KeyguardIndicationTextView mDisclosure;
    private boolean mDozing;
    private final int mFastThreshold;
    private final Handler mHandler;
    private ViewGroup mIndicationArea;
    private int mInitialTextColor;
    private LockIcon mLockIcon;
    private String mMessageToShowOnScreenOn;
    private boolean mPowerCharged;
    private boolean mPowerPluggedIn;
    private boolean mPowerPluggedInWired;
    private String mRestingIndication;
    private final int mSlowThreshold;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private KeyguardIndicationTextView mTextView;
    private final BroadcastReceiver mTickReceiver;
    private CharSequence mTransientIndication;
    private int mTransientTextColor;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private final UserManager mUserManager;
    private boolean mVisible;
    private final SettableWakeLock mWakeLock;

    public KeyguardIndicationController(Context context, ViewGroup viewGroup, LockIcon lockIcon) {
        this(context, viewGroup, lockIcon, WakeLock.createPartial(context, "Doze:KeyguardIndication"));
        registerCallbacks(KeyguardUpdateMonitor.getInstance(context));
    }

    @VisibleForTesting
    KeyguardIndicationController(Context context, ViewGroup viewGroup, LockIcon lockIcon, WakeLock wakeLock) {
        this.mTickReceiver = new AnonymousClass2();
        this.mHandler = new Handler() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.3
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    KeyguardIndicationController.this.hideTransientIndication();
                } else if (message.what == 2) {
                    KeyguardIndicationController.this.mLockIcon.setTransientFpError(false);
                }
            }
        };
        this.mContext = context;
        this.mIndicationArea = viewGroup;
        this.mTextView = (KeyguardIndicationTextView) viewGroup.findViewById(R.id.keyguard_indication_text);
        this.mInitialTextColor = this.mTextView != null ? this.mTextView.getCurrentTextColor() : -1;
        this.mDisclosure = (KeyguardIndicationTextView) viewGroup.findViewById(R.id.keyguard_indication_enterprise_disclosure);
        this.mLockIcon = lockIcon;
        this.mWakeLock = new SettableWakeLock(wakeLock);
        Resources resources = context.getResources();
        this.mSlowThreshold = resources.getInteger(R.integer.config_chargingSlowlyThreshold);
        this.mFastThreshold = resources.getInteger(R.integer.config_chargingFastThreshold);
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        this.mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        updateDisclosure();
    }

    private void registerCallbacks(KeyguardUpdateMonitor keyguardUpdateMonitor) {
        keyguardUpdateMonitor.registerCallback(getKeyguardCallback());
        this.mContext.registerReceiverAsUser(this.mTickReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.TIME_TICK"), null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
    }

    protected KeyguardUpdateMonitorCallback getKeyguardCallback() {
        if (this.mUpdateMonitorCallback == null) {
            this.mUpdateMonitorCallback = new BaseKeyguardCallback();
        }
        return this.mUpdateMonitorCallback;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDisclosure() {
        if (this.mDevicePolicyManager == null) {
            return;
        }
        if (!this.mDozing && this.mDevicePolicyManager.isDeviceManaged()) {
            CharSequence deviceOwnerOrganizationName = this.mDevicePolicyManager.getDeviceOwnerOrganizationName();
            if (deviceOwnerOrganizationName != null) {
                this.mDisclosure.switchIndication(this.mContext.getResources().getString(R.string.do_disclosure_with_name, deviceOwnerOrganizationName));
            } else {
                this.mDisclosure.switchIndication(R.string.do_disclosure_generic);
            }
            this.mDisclosure.setVisibility(0);
            return;
        }
        this.mDisclosure.setVisibility(8);
    }

    public void setVisible(boolean z) {
        this.mVisible = z;
        this.mIndicationArea.setVisibility(z ? 0 : 8);
        if (z) {
            if (!this.mHandler.hasMessages(1)) {
                hideTransientIndication();
            }
            updateIndication(false);
        } else if (!z) {
            hideTransientIndication();
        }
    }

    protected String getTrustGrantedIndication() {
        return null;
    }

    protected String getTrustManagedIndication() {
        return null;
    }

    public void hideTransientIndicationDelayed(long j) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), j);
    }

    public void showTransientIndication(int i) {
        showTransientIndication(this.mContext.getResources().getString(i));
    }

    public void showTransientIndication(CharSequence charSequence) {
        showTransientIndication(charSequence, this.mInitialTextColor);
    }

    public void showTransientIndication(CharSequence charSequence, int i) {
        this.mTransientIndication = charSequence;
        this.mTransientTextColor = i;
        this.mHandler.removeMessages(1);
        if (this.mDozing && !TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(true);
            hideTransientIndicationDelayed(5000L);
        }
        updateIndication(false);
    }

    public void hideTransientIndication() {
        if (this.mTransientIndication != null) {
            this.mTransientIndication = null;
            this.mHandler.removeMessages(1);
            updateIndication(false);
        }
    }

    protected final void updateIndication(boolean z) {
        if (TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(false);
        }
        if (this.mVisible) {
            if (this.mDozing) {
                this.mTextView.setTextColor(-1);
                if (!TextUtils.isEmpty(this.mTransientIndication)) {
                    this.mTextView.switchIndication(this.mTransientIndication);
                    return;
                } else if (this.mPowerPluggedIn) {
                    String computePowerIndication = computePowerIndication();
                    if (z) {
                        animateText(this.mTextView, computePowerIndication);
                        return;
                    } else {
                        this.mTextView.switchIndication(computePowerIndication);
                        return;
                    }
                } else {
                    this.mTextView.switchIndication(NumberFormat.getPercentInstance().format(this.mBatteryLevel / 100.0f));
                    return;
                }
            }
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            String trustGrantedIndication = getTrustGrantedIndication();
            String trustManagedIndication = getTrustManagedIndication();
            if (!this.mUserManager.isUserUnlocked(currentUser)) {
                this.mTextView.switchIndication(17040176);
                this.mTextView.setTextColor(this.mInitialTextColor);
            } else if (!TextUtils.isEmpty(this.mTransientIndication)) {
                this.mTextView.switchIndication(this.mTransientIndication);
                this.mTextView.setTextColor(this.mTransientTextColor);
            } else if (!TextUtils.isEmpty(trustGrantedIndication) && keyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                this.mTextView.switchIndication(trustGrantedIndication);
                this.mTextView.setTextColor(this.mInitialTextColor);
            } else if (this.mPowerPluggedIn) {
                String computePowerIndication2 = computePowerIndication();
                this.mTextView.setTextColor(this.mInitialTextColor);
                if (z) {
                    animateText(this.mTextView, computePowerIndication2);
                } else {
                    this.mTextView.switchIndication(computePowerIndication2);
                }
            } else if (!TextUtils.isEmpty(trustManagedIndication) && keyguardUpdateMonitor.getUserTrustIsManaged(currentUser) && !keyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                this.mTextView.switchIndication(trustManagedIndication);
                this.mTextView.setTextColor(this.mInitialTextColor);
            } else {
                this.mTextView.switchIndication(this.mRestingIndication);
                this.mTextView.setTextColor(this.mInitialTextColor);
            }
        }
    }

    private void animateText(final KeyguardIndicationTextView keyguardIndicationTextView, final String str) {
        final int integer = this.mContext.getResources().getInteger(R.integer.wired_charging_keyguard_text_animation_distance);
        int integer2 = this.mContext.getResources().getInteger(R.integer.wired_charging_keyguard_text_animation_duration_up);
        final int integer3 = this.mContext.getResources().getInteger(R.integer.wired_charging_keyguard_text_animation_duration_down);
        keyguardIndicationTextView.animate().translationYBy(integer).setInterpolator(Interpolators.LINEAR).setDuration(integer2).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                keyguardIndicationTextView.switchIndication(str);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                keyguardIndicationTextView.animate().setDuration(integer3).setInterpolator(Interpolators.BOUNCE).translationYBy((-1) * integer).setListener(null);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String computePowerIndication() {
        long j;
        int i;
        if (this.mPowerCharged) {
            return this.mContext.getResources().getString(R.string.keyguard_charged);
        }
        try {
            j = this.mBatteryInfo.computeChargeTimeRemaining();
        } catch (RemoteException e) {
            Log.e("KeyguardIndication", "Error calling IBatteryStats: ", e);
            j = 0;
        }
        boolean z = j > 0;
        int i2 = this.mChargingSpeed;
        if (i2 != 0) {
            if (i2 == 2) {
                if (z) {
                    i = R.string.keyguard_indication_charging_time_fast;
                } else {
                    i = R.string.keyguard_plugged_in_charging_fast;
                }
            } else if (z) {
                i = R.string.keyguard_indication_charging_time;
            } else {
                i = R.string.keyguard_plugged_in;
            }
        } else if (z) {
            i = R.string.keyguard_indication_charging_time_slowly;
        } else {
            i = R.string.keyguard_plugged_in_charging_slowly;
        }
        String format = NumberFormat.getPercentInstance().format(this.mBatteryLevel / 100.0f);
        if (z) {
            String formatShortElapsedTimeRoundingUpToMinutes = Formatter.formatShortElapsedTimeRoundingUpToMinutes(this.mContext, j);
            try {
                return this.mContext.getResources().getString(i, formatShortElapsedTimeRoundingUpToMinutes, format);
            } catch (IllegalFormatConversionException e2) {
                return this.mContext.getResources().getString(i, formatShortElapsedTimeRoundingUpToMinutes);
            }
        }
        try {
            return this.mContext.getResources().getString(i, format);
        } catch (IllegalFormatConversionException e3) {
            return this.mContext.getResources().getString(i);
        }
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.KeyguardIndicationController$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass2 extends BroadcastReceiver {
        AnonymousClass2() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            KeyguardIndicationController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.-$$Lambda$KeyguardIndicationController$2$zESedvrtrUKx0fqXwwlgga5A3WM
                @Override // java.lang.Runnable
                public final void run() {
                    KeyguardIndicationController.AnonymousClass2.lambda$onReceive$0(KeyguardIndicationController.AnonymousClass2.this);
                }
            });
        }

        public static /* synthetic */ void lambda$onReceive$0(AnonymousClass2 anonymousClass2) {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }
    }

    public void setDozing(boolean z) {
        if (this.mDozing == z) {
            return;
        }
        this.mDozing = z;
        updateIndication(false);
        updateDisclosure();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardIndicationController:");
        printWriter.println("  mTransientTextColor: " + Integer.toHexString(this.mTransientTextColor));
        printWriter.println("  mInitialTextColor: " + Integer.toHexString(this.mInitialTextColor));
        printWriter.println("  mPowerPluggedInWired: " + this.mPowerPluggedInWired);
        printWriter.println("  mPowerPluggedIn: " + this.mPowerPluggedIn);
        printWriter.println("  mPowerCharged: " + this.mPowerCharged);
        printWriter.println("  mChargingSpeed: " + this.mChargingSpeed);
        printWriter.println("  mChargingWattage: " + this.mChargingWattage);
        printWriter.println("  mMessageToShowOnScreenOn: " + this.mMessageToShowOnScreenOn);
        printWriter.println("  mDozing: " + this.mDozing);
        printWriter.println("  mBatteryLevel: " + this.mBatteryLevel);
        StringBuilder sb = new StringBuilder();
        sb.append("  mTextView.getText(): ");
        sb.append((Object) (this.mTextView == null ? null : this.mTextView.getText()));
        printWriter.println(sb.toString());
        printWriter.println("  computePowerIndication(): " + computePowerIndication());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class BaseKeyguardCallback extends KeyguardUpdateMonitorCallback {
        private int mLastSuccessiveErrorMessage = -1;

        protected BaseKeyguardCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus batteryStatus) {
            boolean z = true;
            boolean z2 = batteryStatus.status == 2 || batteryStatus.status == 5;
            boolean z3 = KeyguardIndicationController.this.mPowerPluggedIn;
            KeyguardIndicationController.this.mPowerPluggedInWired = batteryStatus.isPluggedInWired() && z2;
            KeyguardIndicationController.this.mPowerPluggedIn = batteryStatus.isPluggedIn() && z2;
            KeyguardIndicationController.this.mPowerCharged = batteryStatus.isCharged();
            KeyguardIndicationController.this.mChargingWattage = batteryStatus.maxChargingWattage;
            KeyguardIndicationController.this.mChargingSpeed = batteryStatus.getChargingSpeed(KeyguardIndicationController.this.mSlowThreshold, KeyguardIndicationController.this.mFastThreshold);
            KeyguardIndicationController.this.mBatteryLevel = batteryStatus.level;
            KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
            if (z3 || !KeyguardIndicationController.this.mPowerPluggedInWired) {
                z = false;
            }
            keyguardIndicationController.updateIndication(z);
            if (KeyguardIndicationController.this.mDozing) {
                if (!z3 && KeyguardIndicationController.this.mPowerPluggedIn) {
                    KeyguardIndicationController.this.showTransientIndication(KeyguardIndicationController.this.computePowerIndication());
                    KeyguardIndicationController.this.hideTransientIndicationDelayed(5000L);
                } else if (z3 && !KeyguardIndicationController.this.mPowerPluggedIn) {
                    KeyguardIndicationController.this.hideTransientIndication();
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                KeyguardIndicationController.this.updateDisclosure();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintHelp(int i, String str) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(KeyguardIndicationController.this.mContext);
            if (keyguardUpdateMonitor.isUnlockingWithFingerprintAllowed()) {
                int colorError = Utils.getColorError(KeyguardIndicationController.this.mContext);
                if (KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, colorError);
                } else if (keyguardUpdateMonitor.isScreenOn()) {
                    KeyguardIndicationController.this.mLockIcon.setTransientFpError(true);
                    KeyguardIndicationController.this.showTransientIndication(str, colorError);
                    KeyguardIndicationController.this.hideTransientIndicationDelayed(1300L);
                    KeyguardIndicationController.this.mHandler.removeMessages(2);
                    KeyguardIndicationController.this.mHandler.sendMessageDelayed(KeyguardIndicationController.this.mHandler.obtainMessage(2), 1300L);
                }
                this.mLastSuccessiveErrorMessage = -1;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintError(int i, String str) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(KeyguardIndicationController.this.mContext);
            if ((keyguardUpdateMonitor.isUnlockingWithFingerprintAllowed() || i == 9) && i != 5) {
                int colorError = Utils.getColorError(KeyguardIndicationController.this.mContext);
                if (KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    if (this.mLastSuccessiveErrorMessage != i) {
                        KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, colorError);
                    }
                } else if (!keyguardUpdateMonitor.isScreenOn()) {
                    KeyguardIndicationController.this.mMessageToShowOnScreenOn = str;
                } else {
                    KeyguardIndicationController.this.showTransientIndication(str, colorError);
                    KeyguardIndicationController.this.hideTransientIndicationDelayed(5000L);
                }
                this.mLastSuccessiveErrorMessage = i;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTrustAgentErrorMessage(CharSequence charSequence) {
            KeyguardIndicationController.this.showTransientIndication(charSequence, Utils.getColorError(KeyguardIndicationController.this.mContext));
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            if (KeyguardIndicationController.this.mMessageToShowOnScreenOn != null) {
                KeyguardIndicationController.this.showTransientIndication(KeyguardIndicationController.this.mMessageToShowOnScreenOn, Utils.getColorError(KeyguardIndicationController.this.mContext));
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000L);
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintRunningStateChanged(boolean z) {
            if (z) {
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintAuthenticated(int i) {
            super.onFingerprintAuthenticated(i);
            this.mLastSuccessiveErrorMessage = -1;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintAuthFailed() {
            super.onFingerprintAuthFailed();
            this.mLastSuccessiveErrorMessage = -1;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }
    }
}
