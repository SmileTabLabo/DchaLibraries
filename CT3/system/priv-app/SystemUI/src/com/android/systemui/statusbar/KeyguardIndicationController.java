package com.android.systemui.statusbar;

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
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
/* loaded from: a.zip:com/android/systemui/statusbar/KeyguardIndicationController.class */
public class KeyguardIndicationController {
    private final IBatteryStats mBatteryInfo;
    private int mChargingSpeed;
    private int mChargingWattage;
    private final Context mContext;
    private final int mFastThreshold;
    private final LockIcon mLockIcon;
    private String mMessageToShowOnScreenOn;
    private boolean mPowerCharged;
    private boolean mPowerPluggedIn;
    private String mRestingIndication;
    private final int mSlowThreshold;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final KeyguardIndicationTextView mTextView;
    private String mTransientIndication;
    private int mTransientTextColor;
    private boolean mVisible;
    KeyguardUpdateMonitorCallback mUpdateMonitor = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.systemui.statusbar.KeyguardIndicationController.1
        final KeyguardIndicationController this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintError(int i, String str) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.this$0.mContext);
            if (!keyguardUpdateMonitor.isUnlockingWithFingerprintAllowed() || i == 5) {
                return;
            }
            int color = this.this$0.mContext.getResources().getColor(2131558521, null);
            if (this.this$0.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                this.this$0.mStatusBarKeyguardViewManager.showBouncerMessage(str, color);
            } else if (!keyguardUpdateMonitor.isDeviceInteractive()) {
                this.this$0.mMessageToShowOnScreenOn = str;
            } else {
                this.this$0.showTransientIndication(str, color);
                this.this$0.mHandler.removeMessages(1);
                this.this$0.hideTransientIndicationDelayed(5000L);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintHelp(int i, String str) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.this$0.mContext);
            if (keyguardUpdateMonitor.isUnlockingWithFingerprintAllowed()) {
                int color = this.this$0.mContext.getResources().getColor(2131558521, null);
                if (this.this$0.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    this.this$0.mStatusBarKeyguardViewManager.showBouncerMessage(str, color);
                } else if (keyguardUpdateMonitor.isDeviceInteractive()) {
                    this.this$0.mLockIcon.setTransientFpError(true);
                    this.this$0.showTransientIndication(str, color);
                    this.this$0.mHandler.removeMessages(2);
                    this.this$0.mHandler.sendMessageDelayed(this.this$0.mHandler.obtainMessage(2), 1300L);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintRunningStateChanged(boolean z) {
            if (z) {
                this.this$0.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus batteryStatus) {
            boolean z = batteryStatus.status != 2 ? batteryStatus.status == 5 : true;
            KeyguardIndicationController keyguardIndicationController = this.this$0;
            if (!batteryStatus.isPluggedIn()) {
                z = false;
            }
            keyguardIndicationController.mPowerPluggedIn = z;
            this.this$0.mPowerCharged = batteryStatus.isCharged();
            this.this$0.mChargingWattage = batteryStatus.maxChargingWattage;
            this.this$0.mChargingSpeed = batteryStatus.getChargingSpeed(this.this$0.mSlowThreshold, this.this$0.mFastThreshold);
            this.this$0.updateIndication();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            if (this.this$0.mMessageToShowOnScreenOn != null) {
                this.this$0.showTransientIndication(this.this$0.mMessageToShowOnScreenOn, this.this$0.mContext.getResources().getColor(2131558521, null));
                this.this$0.mHandler.removeMessages(1);
                this.this$0.hideTransientIndicationDelayed(5000L);
                this.this$0.mMessageToShowOnScreenOn = null;
            }
        }
    };
    BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.KeyguardIndicationController.2
        final KeyguardIndicationController this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (this.this$0.mVisible) {
                this.this$0.updateIndication();
            }
        }
    };
    private final Handler mHandler = new Handler(this) { // from class: com.android.systemui.statusbar.KeyguardIndicationController.3
        final KeyguardIndicationController this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1 && this.this$0.mTransientIndication != null) {
                this.this$0.mTransientIndication = null;
                this.this$0.updateIndication();
            } else if (message.what == 2) {
                this.this$0.mLockIcon.setTransientFpError(false);
                this.this$0.hideTransientIndication();
            }
        }
    };

    public KeyguardIndicationController(Context context, KeyguardIndicationTextView keyguardIndicationTextView, LockIcon lockIcon) {
        this.mContext = context;
        this.mTextView = keyguardIndicationTextView;
        this.mLockIcon = lockIcon;
        Resources resources = context.getResources();
        this.mSlowThreshold = resources.getInteger(2131755106);
        this.mFastThreshold = resources.getInteger(2131755107);
        this.mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mUpdateMonitor);
        context.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.TIME_TICK"), null, null);
    }

    private int computeColor() {
        if (TextUtils.isEmpty(this.mTransientIndication)) {
            return -1;
        }
        return this.mTransientTextColor;
    }

    private String computeIndication() {
        return !TextUtils.isEmpty(this.mTransientIndication) ? this.mTransientIndication : this.mPowerPluggedIn ? computePowerIndication() : this.mRestingIndication;
    }

    private String computePowerIndication() {
        long j;
        int i;
        if (this.mPowerCharged) {
            return this.mContext.getResources().getString(2131494024);
        }
        try {
            j = this.mBatteryInfo.computeChargeTimeRemaining();
        } catch (RemoteException e) {
            Log.e("KeyguardIndication", "Error calling IBatteryStats: ", e);
            j = 0;
        }
        boolean z = j > 0;
        switch (this.mChargingSpeed) {
            case 0:
                if (!z) {
                    i = 2131494027;
                    break;
                } else {
                    i = 2131493621;
                    break;
                }
            case 1:
            default:
                if (!z) {
                    i = 2131494025;
                    break;
                } else {
                    i = 2131493619;
                    break;
                }
            case 2:
                if (!z) {
                    i = 2131494026;
                    break;
                } else {
                    i = 2131493620;
                    break;
                }
        }
        return z ? this.mContext.getResources().getString(i, Formatter.formatShortElapsedTimeRoundingUpToMinutes(this.mContext, j)) : this.mContext.getResources().getString(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIndication() {
        if (this.mVisible) {
            this.mTextView.switchIndication(computeIndication());
            this.mTextView.setTextColor(computeColor());
        }
    }

    public void hideTransientIndication() {
        if (this.mTransientIndication != null) {
            this.mTransientIndication = null;
            this.mHandler.removeMessages(1);
            updateIndication();
        }
    }

    public void hideTransientIndicationDelayed(long j) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), j);
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    public void setVisible(boolean z) {
        this.mVisible = z;
        this.mTextView.setVisibility(z ? 0 : 8);
        if (z) {
            hideTransientIndication();
            updateIndication();
        }
    }

    public void showTransientIndication(int i) {
        showTransientIndication(this.mContext.getResources().getString(i));
    }

    public void showTransientIndication(String str) {
        showTransientIndication(str, -1);
    }

    public void showTransientIndication(String str, int i) {
        this.mTransientIndication = str;
        this.mTransientTextColor = i;
        this.mHandler.removeMessages(1);
        updateIndication();
    }
}
