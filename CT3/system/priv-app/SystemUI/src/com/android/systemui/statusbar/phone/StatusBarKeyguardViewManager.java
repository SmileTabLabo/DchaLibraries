package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.statusbar.RemoteInputController;
import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarKeyguardViewManager.class */
public class StatusBarKeyguardViewManager implements RemoteInputController.Callback {
    private static String TAG = "StatusBarKeyguardViewManager";
    private KeyguardHostView.OnDismissAction mAfterKeyguardGoneAction;
    protected KeyguardBouncer mBouncer;
    private ViewGroup mContainer;
    protected final Context mContext;
    private boolean mDeferScrimFadeOut;
    private boolean mDeviceWillWakeUp;
    private FingerprintUnlockController mFingerprintUnlockController;
    private boolean mLastBouncerDismissible;
    private boolean mLastBouncerShowing;
    protected boolean mLastOccluded;
    protected boolean mLastRemoteInputActive;
    protected boolean mLastShowing;
    protected LockPatternUtils mLockPatternUtils;
    protected boolean mOccluded;
    protected PhoneStatusBar mPhoneStatusBar;
    protected boolean mRemoteInputActive;
    private boolean mScreenTurnedOn;
    private ScrimController mScrimController;
    protected boolean mShowing;
    private StatusBarWindowManager mStatusBarWindowManager;
    protected ViewMediatorCallback mViewMediatorCallback;
    private final boolean DEBUG = true;
    private boolean mDeviceInteractive = false;
    protected boolean mFirstUpdate = true;
    private Runnable mMakeNavigationBarVisibleRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.1
        final StatusBarKeyguardViewManager this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            Log.d(StatusBarKeyguardViewManager.TAG, "mMakeNavigationBarVisibleRunnable - set nav bar VISIBLE.");
            this.this$0.mPhoneStatusBar.getNavigationBarView().setVisibility(0);
        }
    };

    public StatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
    }

    private void animateScrimControllerKeyguardFadingOut(long j, long j2, Runnable runnable, boolean z) {
        Trace.asyncTraceBegin(8L, "Fading out", 0);
        this.mScrimController.animateKeyguardFadingOut(j, j2, new Runnable(this, runnable) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.6
            final StatusBarKeyguardViewManager this$0;
            final Runnable val$endRunnable;

            {
                this.this$0 = this;
                this.val$endRunnable = runnable;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.val$endRunnable != null) {
                    this.val$endRunnable.run();
                }
                this.this$0.mStatusBarWindowManager.setKeyguardFadingAway(false);
                this.this$0.mPhoneStatusBar.finishKeyguardFadingAway();
                this.this$0.mFingerprintUnlockController.finishKeyguardFadingAway();
                WindowManagerGlobal.getInstance().trimMemory(20);
                Trace.asyncTraceEnd(8L, "Fading out", 0);
            }
        }, z);
    }

    private void animateScrimControllerKeyguardFadingOut(long j, long j2, boolean z) {
        animateScrimControllerKeyguardFadingOut(j, j2, null, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeAfterKeyguardGoneAction() {
        if (this.mAfterKeyguardGoneAction != null) {
            Log.d(TAG, "executeAfterKeyguardGoneAction() is called");
            this.mAfterKeyguardGoneAction.onDismiss();
            this.mAfterKeyguardGoneAction = null;
        }
    }

    private long getNavBarShowDelay() {
        if (this.mPhoneStatusBar.isKeyguardFadingAway()) {
            return this.mPhoneStatusBar.getKeyguardFadingAwayDelay();
        }
        return 320L;
    }

    private void showBouncer() {
        showBouncer(false);
    }

    private void showBouncer(boolean z) {
        if (this.mShowing) {
            this.mBouncer.show(false, z);
        }
        updateStates();
    }

    public void animateCollapsePanels(float f) {
        this.mPhoneStatusBar.animateCollapsePanels(0, true, false, f);
    }

    public void dismiss() {
        dismiss(false);
    }

    public void dismiss(boolean z) {
        Log.d(TAG, "dismiss(authenticated = " + z + ") is called. mScreenOn = " + this.mDeviceInteractive);
        if (this.mDeviceInteractive || VoiceWakeupManager.getInstance().isDismissAndLaunchApp()) {
            showBouncer(z);
        }
        if (this.mDeviceInteractive || this.mDeviceWillWakeUp) {
            showBouncer();
        }
    }

    public void dismissWithAction(KeyguardHostView.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        if (this.mShowing) {
            if (z) {
                Log.d(TAG, "dismissWithAction() - afterKeyguardGone = true, call bouncer.show()");
                this.mBouncer.show(false);
                this.mAfterKeyguardGoneAction = onDismissAction;
            } else {
                Log.d(TAG, "dismissWithAction() - afterKeyguardGone = false,call showWithDismissAction");
                this.mBouncer.showWithDismissAction(onDismissAction);
            }
        }
        updateStates();
    }

    protected boolean getLastNavBarVisible() {
        return (!this.mLastShowing || this.mLastOccluded || this.mLastBouncerShowing) ? true : this.mLastRemoteInputActive;
    }

    public ViewRootImpl getViewRootImpl() {
        return this.mPhoneStatusBar.getStatusBarView().getViewRootImpl();
    }

    public void hide(long j, long j2) {
        Log.d(TAG, "hide() is called.");
        this.mShowing = false;
        long max = Math.max(0L, ((-48) + j) - SystemClock.uptimeMillis());
        if (this.mPhoneStatusBar.isInLaunchTransition()) {
            this.mPhoneStatusBar.fadeKeyguardAfterLaunchTransition(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.3
                final StatusBarKeyguardViewManager this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mStatusBarWindowManager.setKeyguardShowing(false);
                    this.this$0.mStatusBarWindowManager.setKeyguardFadingAway(true);
                    this.this$0.mBouncer.hide(true);
                    this.this$0.updateStates();
                    this.this$0.mScrimController.animateKeyguardFadingOut(100L, 300L, null, false);
                }
            }, new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.4
                final StatusBarKeyguardViewManager this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mPhoneStatusBar.hideKeyguard();
                    this.this$0.mStatusBarWindowManager.setKeyguardFadingAway(false);
                    this.this$0.mViewMediatorCallback.keyguardGone();
                    this.this$0.executeAfterKeyguardGoneAction();
                }
            });
            return;
        }
        if (this.mFingerprintUnlockController.getMode() == 2) {
            this.mFingerprintUnlockController.startKeyguardFadingAway();
            this.mPhoneStatusBar.setKeyguardFadingAway(j, 0L, 240L);
            this.mStatusBarWindowManager.setKeyguardFadingAway(true);
            this.mPhoneStatusBar.fadeKeyguardWhilePulsing();
            animateScrimControllerKeyguardFadingOut(0L, 240L, new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.5
                final StatusBarKeyguardViewManager this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mPhoneStatusBar.hideKeyguard();
                }
            }, false);
        } else {
            this.mFingerprintUnlockController.startKeyguardFadingAway();
            this.mPhoneStatusBar.setKeyguardFadingAway(j, max, j2);
            if (this.mPhoneStatusBar.hideKeyguard()) {
                this.mScrimController.animateGoingToFullShade(max, j2);
                this.mPhoneStatusBar.finishKeyguardFadingAway();
            } else {
                this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                if (this.mFingerprintUnlockController.getMode() != 1) {
                    animateScrimControllerKeyguardFadingOut(max, j2, false);
                } else if (this.mScreenTurnedOn) {
                    animateScrimControllerKeyguardFadingOut(0L, 200L, true);
                } else {
                    this.mDeferScrimFadeOut = true;
                }
            }
        }
        this.mStatusBarWindowManager.setKeyguardShowing(false);
        this.mBouncer.hide(true);
        this.mViewMediatorCallback.keyguardGone();
        executeAfterKeyguardGoneAction();
        updateStates();
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        return this.mBouncer.interceptMediaKey(keyEvent);
    }

    public boolean isBouncerShowing() {
        return this.mBouncer.isShowing();
    }

    public boolean isGoingToNotificationShade() {
        return this.mPhoneStatusBar.isGoingToNotificationShade();
    }

    public boolean isInputRestricted() {
        return this.mViewMediatorCallback.isInputRestricted();
    }

    protected boolean isNavBarVisible() {
        return (!this.mShowing || this.mOccluded || this.mBouncer.isShowing()) ? true : this.mRemoteInputActive;
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public boolean isScreenTurnedOn() {
        return this.mScreenTurnedOn;
    }

    public boolean isSecure() {
        return this.mBouncer.isSecure();
    }

    public boolean isSecure(int i) {
        return !this.mBouncer.isSecure() ? this.mLockPatternUtils.isSecure(i) : true;
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean isUnlockWithWallpaper() {
        return this.mStatusBarWindowManager.isShowingWallpaper();
    }

    public void keyguardGoingAway() {
        this.mPhoneStatusBar.keyguardGoingAway();
    }

    public void notifyDeviceWakeUpRequested() {
        this.mDeviceWillWakeUp = !this.mDeviceInteractive;
    }

    public void notifyKeyguardAuthenticated(boolean z) {
        this.mBouncer.notifyKeyguardAuthenticated(z);
    }

    public void onActivityDrawn() {
        if (this.mPhoneStatusBar.isCollapsing()) {
            this.mPhoneStatusBar.addPostCollapseAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.7
                final StatusBarKeyguardViewManager this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mViewMediatorCallback.readyForKeyguardDone();
                }
            });
        } else {
            this.mViewMediatorCallback.readyForKeyguardDone();
        }
    }

    public boolean onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        if (!this.mBouncer.isShowing()) {
            Log.d(TAG, "onBackPressed() - reset & return false");
            return false;
        }
        this.mPhoneStatusBar.endAffordanceLaunch();
        reset();
        this.mAfterKeyguardGoneAction = null;
        return true;
    }

    public void onDensityOrFontScaleChanged() {
        this.mBouncer.hide(true);
    }

    public void onFinishedGoingToSleep() {
        this.mDeviceInteractive = false;
        this.mPhoneStatusBar.onFinishedGoingToSleep();
        this.mBouncer.onScreenTurnedOff();
    }

    @Override // com.android.systemui.statusbar.RemoteInputController.Callback
    public void onRemoteInputActive(boolean z) {
        this.mRemoteInputActive = z;
        updateStates();
    }

    public void onScreenTurnedOff() {
        this.mScreenTurnedOn = false;
        this.mPhoneStatusBar.onScreenTurnedOff();
    }

    public void onScreenTurnedOn() {
        this.mScreenTurnedOn = true;
        if (this.mDeferScrimFadeOut) {
            this.mDeferScrimFadeOut = false;
            animateScrimControllerKeyguardFadingOut(0L, 200L, true);
            updateStates();
        }
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.onScreenTurnedOn();
        }
    }

    public void onScreenTurningOn() {
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.onScreenTurningOn();
        }
    }

    public void onStartedGoingToSleep() {
        this.mPhoneStatusBar.onStartedGoingToSleep();
    }

    public void onStartedWakingUp() {
        this.mDeviceInteractive = true;
        this.mDeviceWillWakeUp = false;
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.onStartedWakingUp();
        }
    }

    public void registerStatusBar(PhoneStatusBar phoneStatusBar, ViewGroup viewGroup, StatusBarWindowManager statusBarWindowManager, ScrimController scrimController, FingerprintUnlockController fingerprintUnlockController) {
        this.mPhoneStatusBar = phoneStatusBar;
        this.mContainer = viewGroup;
        this.mStatusBarWindowManager = statusBarWindowManager;
        this.mScrimController = scrimController;
        this.mFingerprintUnlockController = fingerprintUnlockController;
        this.mBouncer = SystemUIFactory.getInstance().createKeyguardBouncer(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils, this.mStatusBarWindowManager, viewGroup);
    }

    public void reset() {
        Log.d(TAG, "reset() is called, mShowing = " + this.mShowing + " ,mOccluded = " + this.mOccluded);
        if (this.mShowing) {
            if (this.mOccluded) {
                this.mPhoneStatusBar.hideKeyguard();
                this.mPhoneStatusBar.stopWaitingForKeyguardExit();
                this.mBouncer.hide(false);
            } else {
                showBouncerOrKeyguard();
            }
            KeyguardUpdateMonitor.getInstance(this.mContext).sendKeyguardReset();
            updateStates();
        }
    }

    public void setNeedsInput(boolean z) {
        Log.d(TAG, "setNeedsInput() - needsInput = " + z);
        this.mStatusBarWindowManager.setKeyguardNeedsInput(z);
    }

    public void setOccluded(boolean z) {
        if (z && !this.mOccluded && this.mShowing && this.mPhoneStatusBar.isInLaunchTransition()) {
            this.mOccluded = true;
            this.mPhoneStatusBar.fadeKeyguardAfterLaunchTransition(null, new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.2
                final StatusBarKeyguardViewManager this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (!this.this$0.mOccluded) {
                        Log.d(StatusBarKeyguardViewManager.TAG, "setOccluded.run() - mOccluded was set to false");
                        return;
                    }
                    Log.d(StatusBarKeyguardViewManager.TAG, "setOccluded.run() - setKeyguardOccluded(true)");
                    this.this$0.mStatusBarWindowManager.setKeyguardOccluded(true);
                    this.this$0.reset();
                }
            });
            return;
        }
        this.mOccluded = z;
        Log.d(TAG, "setOccluded() - setKeyguardOccluded(" + z + ")");
        this.mPhoneStatusBar.updateMediaMetaData(false, false);
        this.mStatusBarWindowManager.setKeyguardOccluded(z);
        reset();
    }

    public boolean shouldDisableWindowAnimationsForUnlock() {
        return this.mPhoneStatusBar.isInLaunchTransition();
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mBouncer.shouldDismissOnMenuPressed();
    }

    public void show(Bundle bundle) {
        Log.d(TAG, "show() is called.");
        this.mShowing = true;
        this.mStatusBarWindowManager.setKeyguardShowing(true);
        this.mScrimController.abortKeyguardFadingOut();
        reset();
    }

    public void showBouncerMessage(String str, int i) {
        this.mBouncer.showMessage(str, i);
    }

    protected void showBouncerOrKeyguard() {
        Log.d(TAG, "showBouncerOrKeyguard() is called.");
        if (this.mBouncer.needsFullscreenBouncer()) {
            Log.d(TAG, "needsFullscreenBouncer() is true, show \"Bouncer\" view directly.");
            this.mPhoneStatusBar.hideKeyguard();
            this.mBouncer.show(true);
            return;
        }
        Log.d(TAG, "needsFullscreenBouncer() is false,show \"Notification Keyguard\" view.");
        this.mPhoneStatusBar.showKeyguard();
        this.mBouncer.hide(false);
        this.mBouncer.prepare();
    }

    public void startPreHideAnimation(Runnable runnable) {
        if (this.mBouncer.isShowing()) {
            this.mBouncer.startPreHideAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void updateStates() {
        int systemUiVisibility = this.mContainer.getSystemUiVisibility();
        boolean z = this.mShowing;
        boolean z2 = this.mOccluded;
        boolean isShowing = this.mBouncer.isShowing();
        boolean z3 = !this.mBouncer.isFullscreenBouncer();
        boolean z4 = this.mRemoteInputActive;
        if (((z3 || !z) ? true : z4) != ((this.mLastBouncerDismissible || !this.mLastShowing) ? true : this.mLastRemoteInputActive) || this.mFirstUpdate) {
            if (z3 || !z || z4) {
                this.mContainer.setSystemUiVisibility((-4194305) & systemUiVisibility);
            } else {
                this.mContainer.setSystemUiVisibility(4194304 | systemUiVisibility);
            }
        }
        boolean isNavBarVisible = isNavBarVisible();
        if (isNavBarVisible != getLastNavBarVisible() || this.mFirstUpdate) {
            Log.d(TAG, "updateStates() - showing = " + z + ", mLastShowing = " + this.mLastShowing + "\nupdateStates() - occluded = " + z2 + "mLastOccluded = " + this.mLastOccluded + "\nupdateStates() - bouncerShowing = " + isShowing + ", mLastBouncerShowing = " + this.mLastBouncerShowing + "\nupdateStates() - mFirstUpdate = " + this.mFirstUpdate);
            if (this.mPhoneStatusBar.getNavigationBarView() != null) {
                if (isNavBarVisible) {
                    long navBarShowDelay = getNavBarShowDelay();
                    if (navBarShowDelay == 0) {
                        this.mMakeNavigationBarVisibleRunnable.run();
                    } else {
                        this.mContainer.postOnAnimationDelayed(this.mMakeNavigationBarVisibleRunnable, navBarShowDelay);
                    }
                } else {
                    Log.d(TAG, "updateStates() - set nav bar GONE for showing notification keyguard.");
                    this.mContainer.removeCallbacks(this.mMakeNavigationBarVisibleRunnable);
                    this.mPhoneStatusBar.getNavigationBarView().setVisibility(8);
                }
            }
        }
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            Log.d(TAG, "updateStates() - setBouncerShowing(" + isShowing + ")");
            this.mStatusBarWindowManager.setBouncerShowing(isShowing);
            this.mPhoneStatusBar.setBouncerShowing(isShowing);
            this.mScrimController.setBouncerShowing(isShowing);
        }
        KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        if ((z && !z2) != (this.mLastShowing && !this.mLastOccluded) || this.mFirstUpdate) {
            keyguardUpdateMonitor.onKeyguardVisibilityChanged(z && !z2);
        }
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            keyguardUpdateMonitor.sendKeyguardBouncerChanged(isShowing);
        }
        this.mFirstUpdate = false;
        this.mLastShowing = z;
        this.mLastOccluded = z2;
        this.mLastBouncerShowing = isShowing;
        this.mLastBouncerDismissible = z3;
        this.mLastRemoteInputActive = z4;
        this.mPhoneStatusBar.onKeyguardViewManagerStatesUpdated();
    }

    public void verifyUnlock() {
        show(null);
        dismiss();
    }
}
