package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.keyguard.KeyguardViewMediator;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/FingerprintUnlockController.class */
public class FingerprintUnlockController extends KeyguardUpdateMonitorCallback {
    private DozeScrimController mDozeScrimController;
    private KeyguardViewMediator mKeyguardViewMediator;
    private int mMode;
    private PhoneStatusBar mPhoneStatusBar;
    private PowerManager mPowerManager;
    private ScrimController mScrimController;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private StatusBarWindowManager mStatusBarWindowManager;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler();
    private int mPendingAuthenticatedUserId = -1;
    private final Runnable mReleaseFingerprintWakeLockRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.FingerprintUnlockController.1
        final FingerprintUnlockController this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            Log.i("FingerprintController", "fp wakelock: TIMEOUT!!");
            this.this$0.releaseFingerprintWakeLock();
        }
    };

    public FingerprintUnlockController(Context context, StatusBarWindowManager statusBarWindowManager, DozeScrimController dozeScrimController, KeyguardViewMediator keyguardViewMediator, ScrimController scrimController, PhoneStatusBar phoneStatusBar) {
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor.registerCallback(this);
        this.mStatusBarWindowManager = statusBarWindowManager;
        this.mDozeScrimController = dozeScrimController;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mScrimController = scrimController;
        this.mPhoneStatusBar = phoneStatusBar;
    }

    private int calculateMode() {
        boolean isUnlockingWithFingerprintAllowed = this.mUpdateMonitor.isUnlockingWithFingerprintAllowed();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (this.mStatusBarKeyguardViewManager.isShowing()) {
                if (this.mDozeScrimController.isPulsing() && isUnlockingWithFingerprintAllowed) {
                    return 2;
                }
                return isUnlockingWithFingerprintAllowed ? 1 : 3;
            }
            return 4;
        } else if (this.mStatusBarKeyguardViewManager.isShowing()) {
            if (this.mStatusBarKeyguardViewManager.isBouncerShowing() && isUnlockingWithFingerprintAllowed) {
                return 6;
            }
            if (isUnlockingWithFingerprintAllowed) {
                return 5;
            }
            return !this.mStatusBarKeyguardViewManager.isBouncerShowing() ? 3 : 0;
        } else {
            return 0;
        }
    }

    private void cleanup() {
        this.mMode = 0;
        releaseFingerprintWakeLock();
        this.mStatusBarWindowManager.setForceDozeBrightness(false);
        this.mPhoneStatusBar.notifyFpAuthModeChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void releaseFingerprintWakeLock() {
        if (this.mWakeLock != null) {
            this.mHandler.removeCallbacks(this.mReleaseFingerprintWakeLockRunnable);
            Log.i("FingerprintController", "releasing fp wakelock");
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void finishKeyguardFadingAway() {
        this.mMode = 0;
        if (this.mPhoneStatusBar.getNavigationBarView() != null) {
            this.mPhoneStatusBar.getNavigationBarView().setWakeAndUnlocking(false);
        }
        this.mPhoneStatusBar.notifyFpAuthModeChanged();
    }

    public int getMode() {
        return this.mMode;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFingerprintAcquired() {
        releaseFingerprintWakeLock();
        if (this.mUpdateMonitor.isDeviceInteractive()) {
            return;
        }
        this.mWakeLock = this.mPowerManager.newWakeLock(1, "wake-and-unlock wakelock");
        this.mWakeLock.acquire();
        Log.i("FingerprintController", "fingerprint acquired, grabbing fp wakelock");
        this.mHandler.postDelayed(this.mReleaseFingerprintWakeLockRunnable, 15000L);
        if (this.mDozeScrimController.isPulsing()) {
            this.mStatusBarWindowManager.setForceDozeBrightness(true);
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFingerprintAuthFailed() {
        cleanup();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFingerprintAuthenticated(int i) {
        if (this.mUpdateMonitor.isGoingToSleep()) {
            this.mPendingAuthenticatedUserId = i;
            return;
        }
        boolean isDeviceInteractive = this.mUpdateMonitor.isDeviceInteractive();
        this.mMode = calculateMode();
        if (!isDeviceInteractive) {
            Log.i("FingerprintController", "fp wakelock: Authenticated, waking up...");
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
        }
        releaseFingerprintWakeLock();
        switch (this.mMode) {
            case 2:
                this.mPhoneStatusBar.updateMediaMetaData(false, true);
            case 1:
                this.mStatusBarWindowManager.setStatusBarFocusable(false);
                this.mDozeScrimController.abortPulsing();
                this.mKeyguardViewMediator.onWakeAndUnlocking();
                this.mScrimController.setWakeAndUnlocking();
                if (this.mPhoneStatusBar.getNavigationBarView() != null) {
                    this.mPhoneStatusBar.getNavigationBarView().setWakeAndUnlocking(true);
                    break;
                }
                break;
            case 3:
            case 5:
                if (!isDeviceInteractive) {
                    this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
                }
                this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.3f);
                break;
            case 6:
                this.mStatusBarKeyguardViewManager.notifyKeyguardAuthenticated(false);
                break;
        }
        if (this.mMode != 2) {
            this.mStatusBarWindowManager.setForceDozeBrightness(false);
        }
        this.mPhoneStatusBar.notifyFpAuthModeChanged();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFingerprintError(int i, String str) {
        cleanup();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFinishedGoingToSleep(int i) {
        if (this.mPendingAuthenticatedUserId != -1) {
            this.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.FingerprintUnlockController.2
                final FingerprintUnlockController this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.onFingerprintAuthenticated(this.this$0.mPendingAuthenticatedUserId);
                }
            });
        }
        this.mPendingAuthenticatedUserId = -1;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedGoingToSleep(int i) {
        this.mPendingAuthenticatedUserId = -1;
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    public void startKeyguardFadingAway() {
        this.mHandler.postDelayed(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.FingerprintUnlockController.3
            final FingerprintUnlockController this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mStatusBarWindowManager.setForceDozeBrightness(false);
            }
        }, 96L);
    }
}
