package com.android.keyguard;

import android.os.SystemClock;
import com.android.internal.telephony.IccCardConstants;
import com.android.keyguard.KeyguardUpdateMonitor;
/* loaded from: a.zip:com/android/keyguard/KeyguardUpdateMonitorCallback.class */
public class KeyguardUpdateMonitorCallback {
    private boolean mShowing;
    private long mVisibilityChangedCalled;

    public void onAirPlaneModeChanged(boolean z) {
    }

    public void onBootCompleted() {
    }

    public void onClockVisibilityChanged() {
    }

    public void onDevicePolicyManagerStateChanged() {
    }

    public void onDeviceProvisioned() {
    }

    public void onEmergencyCallAction() {
    }

    public void onFaceUnlockStateChanged(boolean z, int i) {
    }

    public void onFingerprintAcquired() {
    }

    public void onFingerprintAuthFailed() {
    }

    public void onFingerprintAuthenticated(int i) {
    }

    public void onFingerprintError(int i, String str) {
    }

    public void onFingerprintHelp(int i, String str) {
    }

    public void onFingerprintRunningStateChanged(boolean z) {
    }

    public void onFinishedGoingToSleep(int i) {
    }

    public void onKeyguardBouncerChanged(boolean z) {
    }

    public void onKeyguardVisibilityChanged(boolean z) {
    }

    public void onKeyguardVisibilityChangedRaw(boolean z) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (z != this.mShowing || elapsedRealtime - this.mVisibilityChangedCalled >= 1000) {
            onKeyguardVisibilityChanged(z);
            this.mVisibilityChangedCalled = elapsedRealtime;
            this.mShowing = z;
        }
    }

    public void onPhoneStateChanged(int i) {
    }

    public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus batteryStatus) {
    }

    public void onRefreshCarrierInfo() {
    }

    public void onRingerModeChanged(int i) {
    }

    public void onScreenTurnedOff() {
    }

    public void onScreenTurnedOn() {
    }

    public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
    }

    public void onStartedGoingToSleep(int i) {
    }

    public void onStartedWakingUp() {
    }

    public void onStrongAuthStateChanged(int i) {
    }

    public void onTimeChanged() {
    }

    public void onTrustChanged(int i) {
    }

    public void onTrustGrantedWithFlags(int i, int i2) {
    }

    public void onTrustManagedChanged(int i) {
    }

    public void onUserInfoChanged(int i) {
    }

    public void onUserSwitchComplete(int i) {
    }

    public void onUserSwitching(int i) {
    }
}
