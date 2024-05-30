package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.StatsLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManagerProxy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.BiConsumer;
/* loaded from: classes.dex */
public class StatusBarKeyguardViewManager implements RemoteInputController.Callback {
    private static String TAG = "StatusBarKeyguardViewManager";
    private KeyguardHostView.OnDismissAction mAfterKeyguardGoneAction;
    protected KeyguardBouncer mBouncer;
    private ViewGroup mContainer;
    protected final Context mContext;
    private boolean mDozing;
    private FingerprintUnlockController mFingerprintUnlockController;
    private boolean mGoingToSleepVisibleNotOccluded;
    private boolean mLastBouncerDismissible;
    private boolean mLastBouncerShowing;
    private boolean mLastDozing;
    private int mLastFpMode;
    protected boolean mLastOccluded;
    protected boolean mLastRemoteInputActive;
    protected boolean mLastShowing;
    protected LockPatternUtils mLockPatternUtils;
    private NotificationPanelView mNotificationPanelView;
    protected boolean mOccluded;
    private DismissWithActionRequest mPendingWakeupAction;
    protected boolean mRemoteInputActive;
    protected boolean mShowing;
    protected StatusBar mStatusBar;
    protected ViewMediatorCallback mViewMediatorCallback;
    private final boolean DEBUG = true;
    private final KeyguardBouncer.BouncerExpansionCallback mExpansionCallback = new KeyguardBouncer.BouncerExpansionCallback() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.1
        @Override // com.android.systemui.statusbar.phone.KeyguardBouncer.BouncerExpansionCallback
        public void onFullyShown() {
            StatusBarKeyguardViewManager.this.updateStates();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardBouncer.BouncerExpansionCallback
        public void onFullyHidden() {
            StatusBarKeyguardViewManager.this.updateStates();
        }
    };
    protected boolean mFirstUpdate = true;
    private final ArrayList<Runnable> mAfterKeyguardGoneRunnables = new ArrayList<>();
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onEmergencyCallAction() {
            if (StatusBarKeyguardViewManager.this.mOccluded) {
                StatusBarKeyguardViewManager.this.reset(true);
            }
        }
    };
    private Runnable mMakeNavigationBarVisibleRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.6
        @Override // java.lang.Runnable
        public void run() {
            Log.d(StatusBarKeyguardViewManager.TAG, "mMakeNavigationBarVisibleRunnable - set nav bar VISIBLE.");
            StatusBarKeyguardViewManager.this.mStatusBar.getNavigationBarView().getRootView().setVisibility(0);
        }
    };
    private final StatusBarWindowManager mStatusBarWindowManager = (StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class);

    public StatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mUpdateMonitorCallback);
    }

    public void registerStatusBar(StatusBar statusBar, ViewGroup viewGroup, NotificationPanelView notificationPanelView, FingerprintUnlockController fingerprintUnlockController, DismissCallbackRegistry dismissCallbackRegistry) {
        this.mStatusBar = statusBar;
        this.mContainer = viewGroup;
        this.mFingerprintUnlockController = fingerprintUnlockController;
        this.mBouncer = SystemUIFactory.getInstance().createKeyguardBouncer(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils, viewGroup, dismissCallbackRegistry, this.mExpansionCallback);
        this.mContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarKeyguardViewManager$la5jBJIHWW-wWro_RwMU-0UMDCw
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                StatusBarKeyguardViewManager.this.onContainerLayout(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mNotificationPanelView = notificationPanelView;
        notificationPanelView.setExpansionListener(new BiConsumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$RwixcXxEpB1TMXbyMOl_aGzysd0
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                StatusBarKeyguardViewManager.this.onPanelExpansionChanged(((Float) obj).floatValue(), ((Boolean) obj2).booleanValue());
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onContainerLayout(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mNotificationPanelView.setBouncerTop(this.mBouncer.getTop());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    public void onPanelExpansionChanged(float f, boolean z) {
        if (this.mNotificationPanelView.isUnlockHintRunning()) {
            this.mBouncer.setExpansion(1.0f);
        } else if (this.mOccluded || this.mBouncer.willDismissWithAction() || this.mBouncer.isShowingScrimmed() || this.mStatusBar.isFullScreenUserSwitcherState()) {
            this.mBouncer.setExpansion(0.0f);
        } else if (this.mShowing && !this.mDozing) {
            if (!isWakeAndUnlocking()) {
                this.mBouncer.setExpansion(f);
            }
            if (f != 1.0f && z && this.mStatusBar.isKeyguardCurrentlySecure() && !this.mBouncer.isShowing() && !this.mBouncer.isAnimatingAway()) {
                this.mBouncer.show(false, false, false);
            }
        }
    }

    public void show(Bundle bundle) {
        Log.d(TAG, "show() is called.");
        this.mShowing = true;
        this.mStatusBarWindowManager.setKeyguardShowing(true);
        reset(true);
        StatsLog.write(62, 2);
    }

    protected void showBouncerOrKeyguard(boolean z) {
        Log.d(TAG, "showBouncerOrKeyguard() is called.");
        if (this.mBouncer.needsFullscreenBouncer() && !this.mDozing) {
            Log.d(TAG, "needsFullscreenBouncer() is true, show \"Bouncer\" view directly.");
            this.mStatusBar.hideKeyguard();
            this.mBouncer.show(true);
        } else {
            Log.d(TAG, "needsFullscreenBouncer() is false,show \"Notification Keyguard\" view.");
            this.mStatusBar.showKeyguard();
            if (z) {
                hideBouncer(shouldDestroyViewOnReset());
                this.mBouncer.prepare();
            }
        }
        updateStates();
    }

    protected boolean shouldDestroyViewOnReset() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideBouncer(boolean z) {
        this.mBouncer.hide(z);
        cancelPendingWakeupAction();
    }

    private void showBouncer() {
        showBouncer(false, false);
    }

    public void showBouncer(boolean z) {
        showBouncer(false, z);
    }

    private void showBouncer(boolean z, boolean z2) {
        if (this.mShowing && !this.mBouncer.isShowing()) {
            this.mBouncer.show(false, z, z2);
        }
        updateStates();
    }

    public void dismissWithAction(KeyguardHostView.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        dismissWithAction(onDismissAction, runnable, z, null);
    }

    public void dismissWithAction(KeyguardHostView.OnDismissAction onDismissAction, Runnable runnable, boolean z, String str) {
        if (this.mShowing) {
            cancelPendingWakeupAction();
            if (this.mDozing && !isWakeAndUnlocking()) {
                this.mPendingWakeupAction = new DismissWithActionRequest(onDismissAction, runnable, z, str);
                return;
            } else if (!z) {
                Log.d(TAG, "dismissWithAction() - afterKeyguardGone = false,call showWithDismissAction");
                this.mBouncer.showWithDismissAction(onDismissAction, runnable);
            } else {
                Log.d(TAG, "dismissWithAction() - afterKeyguardGone = true, call bouncer.show()");
                this.mAfterKeyguardGoneAction = onDismissAction;
                this.mBouncer.show(false);
            }
        }
        updateStates();
    }

    private boolean isWakeAndUnlocking() {
        int mode = this.mFingerprintUnlockController.getMode();
        return mode == 1 || mode == 2;
    }

    public void addAfterKeyguardGoneRunnable(Runnable runnable) {
        this.mAfterKeyguardGoneRunnables.add(runnable);
    }

    public void reset(boolean z) {
        String str = TAG;
        Log.d(str, "reset() is called, mShowing = " + this.mShowing + " ,mOccluded = " + this.mOccluded + "hideBouncerWhenShowing = " + z);
        if (this.mShowing) {
            if (this.mOccluded && !this.mDozing) {
                this.mStatusBar.hideKeyguard();
                if (z || this.mBouncer.needsFullscreenBouncer()) {
                    hideBouncer(false);
                }
            } else {
                showBouncerOrKeyguard(z);
            }
            KeyguardUpdateMonitor.getInstance(this.mContext).sendKeyguardReset();
            updateStates();
        }
    }

    public boolean isGoingToSleepVisibleNotOccluded() {
        return this.mGoingToSleepVisibleNotOccluded;
    }

    public void onStartedGoingToSleep() {
        this.mGoingToSleepVisibleNotOccluded = isShowing() && !isOccluded();
    }

    public void onFinishedGoingToSleep() {
        this.mGoingToSleepVisibleNotOccluded = false;
        this.mBouncer.onScreenTurnedOff();
    }

    public void onStartedWakingUp() {
    }

    public void onScreenTurningOn() {
    }

    public void onScreenTurnedOn() {
    }

    @Override // com.android.systemui.statusbar.RemoteInputController.Callback
    public void onRemoteInputActive(boolean z) {
        this.mRemoteInputActive = z;
        updateStates();
    }

    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            if (z || this.mBouncer.needsFullscreenBouncer() || this.mOccluded) {
                reset(z);
            }
            updateStates();
            if (!z) {
                launchPendingWakeupAction();
            }
        }
    }

    public void onScreenTurnedOff() {
    }

    public void notifyDeviceWakeUpRequested() {
    }

    public void setNeedsInput(boolean z) {
        String str = TAG;
        Log.d(str, "setNeedsInput() - needsInput = " + z);
        this.mStatusBarWindowManager.setKeyguardNeedsInput(z);
    }

    public boolean isUnlockWithWallpaper() {
        return this.mStatusBarWindowManager.isShowingWallpaper();
    }

    public void setOccluded(boolean z, boolean z2) {
        this.mStatusBar.setOccluded(z);
        boolean z3 = true;
        if (z && !this.mOccluded && this.mShowing) {
            StatsLog.write(62, 3);
            if (this.mStatusBar.isInLaunchTransition()) {
                this.mOccluded = true;
                this.mStatusBar.fadeKeyguardAfterLaunchTransition(null, new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.3
                    @Override // java.lang.Runnable
                    public void run() {
                        if (StatusBarKeyguardViewManager.this.mOccluded) {
                            StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardOccluded(StatusBarKeyguardViewManager.this.mOccluded);
                            StatusBarKeyguardViewManager.this.reset(true);
                            return;
                        }
                        Log.d(StatusBarKeyguardViewManager.TAG, "setOccluded.run() - mOccluded was set to false");
                    }
                });
                return;
            }
        } else if (!z && this.mOccluded && this.mShowing) {
            StatsLog.write(62, 2);
        }
        boolean z4 = !this.mOccluded && z;
        this.mOccluded = z;
        Log.d(TAG, "setOccluded() - setKeyguardOccluded(" + z + ")");
        if (this.mShowing) {
            StatusBar statusBar = this.mStatusBar;
            if (!z2 || z) {
                z3 = false;
            }
            statusBar.updateMediaMetaData(false, z3);
        }
        this.mStatusBarWindowManager.setKeyguardOccluded(z);
        if (!this.mDozing) {
            reset(z4);
        }
        if (z2 && !z && this.mShowing) {
            this.mStatusBar.animateKeyguardUnoccluding();
        }
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public void startPreHideAnimation(Runnable runnable) {
        if (this.mBouncer.isShowing()) {
            this.mBouncer.startPreHideAnimation(runnable);
            this.mNotificationPanelView.onBouncerPreHideAnimation();
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void hide(long j, long j2) {
        long j3;
        long j4;
        long j5;
        this.mShowing = false;
        launchPendingWakeupAction();
        if (KeyguardUpdateMonitor.getInstance(this.mContext).needsSlowUnlockTransition()) {
            j3 = 2000;
        } else {
            j3 = j2;
        }
        long max = Math.max(0L, (j - 48) - SystemClock.uptimeMillis());
        if (this.mStatusBar.isInLaunchTransition()) {
            this.mStatusBar.fadeKeyguardAfterLaunchTransition(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.4
                @Override // java.lang.Runnable
                public void run() {
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardShowing(false);
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                    StatusBarKeyguardViewManager.this.hideBouncer(true);
                    StatusBarKeyguardViewManager.this.updateStates();
                }
            }, new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.5
                @Override // java.lang.Runnable
                public void run() {
                    StatusBarKeyguardViewManager.this.mStatusBar.hideKeyguard();
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(false);
                    StatusBarKeyguardViewManager.this.mViewMediatorCallback.keyguardGone();
                    StatusBarKeyguardViewManager.this.executeAfterKeyguardGoneAction();
                }
            });
        } else {
            executeAfterKeyguardGoneAction();
            boolean z = this.mFingerprintUnlockController.getMode() == 2;
            if (z) {
                j4 = 240;
                j5 = 0;
            } else {
                j4 = j3;
                j5 = max;
            }
            this.mStatusBar.setKeyguardFadingAway(j, j5, j4);
            this.mFingerprintUnlockController.startKeyguardFadingAway();
            hideBouncer(true);
            if (z) {
                this.mStatusBar.fadeKeyguardWhilePulsing();
                wakeAndUnlockDejank();
            } else if (!this.mStatusBar.hideKeyguard()) {
                this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                wakeAndUnlockDejank();
            } else {
                this.mStatusBar.finishKeyguardFadingAway();
                this.mFingerprintUnlockController.finishKeyguardFadingAway();
            }
            updateStates();
            this.mStatusBarWindowManager.setKeyguardShowing(false);
            this.mViewMediatorCallback.keyguardGone();
        }
        StatsLog.write(62, 1);
    }

    public void onDensityOrFontScaleChanged() {
        hideBouncer(true);
    }

    public void onThemeChanged() {
        hideBouncer(true);
        this.mBouncer.prepare();
    }

    public void onKeyguardFadedAway() {
        this.mContainer.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarKeyguardViewManager$Qxwhwo1uUdyEeH0KZ8eQm9-dLJ8
            @Override // java.lang.Runnable
            public final void run() {
                StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(false);
            }
        }, 100L);
        this.mStatusBar.finishKeyguardFadingAway();
        this.mFingerprintUnlockController.finishKeyguardFadingAway();
        WindowManagerGlobal.getInstance().trimMemory(20);
    }

    private void wakeAndUnlockDejank() {
        if (this.mFingerprintUnlockController.getMode() == 1 && LatencyTracker.isEnabled(this.mContext)) {
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarKeyguardViewManager$MwYtqufbgyJNJ1l_l2mNmQjtUTg
                @Override // java.lang.Runnable
                public final void run() {
                    LatencyTracker.getInstance(StatusBarKeyguardViewManager.this.mContext).onActionEnd(2);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeAfterKeyguardGoneAction() {
        if (this.mAfterKeyguardGoneAction != null) {
            Log.d(TAG, "executeAfterKeyguardGoneAction() is called");
            this.mAfterKeyguardGoneAction.onDismiss();
            this.mAfterKeyguardGoneAction = null;
        }
        for (int i = 0; i < this.mAfterKeyguardGoneRunnables.size(); i++) {
            this.mAfterKeyguardGoneRunnables.get(i).run();
        }
        this.mAfterKeyguardGoneRunnables.clear();
    }

    public void dismissAndCollapse() {
        this.mStatusBar.executeRunnableDismissingKeyguard(null, null, true, false, true);
    }

    public void dismiss(boolean z) {
        String str = TAG;
        Log.d(str, "dismiss(authenticated = " + z + ") is called.");
        if (VoiceWakeupManagerProxy.getInstance().isDismissAndLaunchApp()) {
            showBouncer(z, false);
        } else {
            showBouncer();
        }
    }

    public boolean isSecure() {
        return this.mBouncer.isSecure();
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean onBackPressed(boolean z) {
        Log.d(TAG, "onBackPressed()");
        if (this.mBouncer.isShowing()) {
            this.mStatusBar.endAffordanceLaunch();
            reset(z);
            this.mAfterKeyguardGoneAction = null;
            return true;
        }
        Log.d(TAG, "onBackPressed() - reset & return false");
        return false;
    }

    public boolean isBouncerShowing() {
        return this.mBouncer.isShowing();
    }

    public boolean isFullscreenBouncer() {
        return this.mBouncer.isFullscreenBouncer();
    }

    private long getNavBarShowDelay() {
        if (this.mStatusBar.isKeyguardFadingAway()) {
            return this.mStatusBar.getKeyguardFadingAwayDelay();
        }
        if (this.mBouncer.isShowing()) {
            return 320L;
        }
        return 0L;
    }

    public void updateStates() {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        int systemUiVisibility = this.mContainer.getSystemUiVisibility();
        boolean z5 = this.mShowing;
        boolean z6 = this.mOccluded;
        boolean isShowing = this.mBouncer.isShowing();
        boolean z7 = true;
        boolean z8 = !this.mBouncer.isFullscreenBouncer();
        boolean z9 = this.mRemoteInputActive;
        if (z8 || !z5 || z9) {
            z = true;
        } else {
            z = false;
        }
        if (this.mLastBouncerDismissible || !this.mLastShowing || this.mLastRemoteInputActive) {
            z2 = true;
        } else {
            z2 = false;
        }
        if (z != z2 || this.mFirstUpdate) {
            if (z8 || !z5 || z9) {
                this.mContainer.setSystemUiVisibility(systemUiVisibility & (-4194305));
            } else {
                this.mContainer.setSystemUiVisibility(systemUiVisibility | 4194304);
            }
        }
        boolean isNavBarVisible = isNavBarVisible();
        if (isNavBarVisible != getLastNavBarVisible() || this.mFirstUpdate) {
            Log.d(TAG, "updateStates() - showing = " + z5 + ", mLastShowing = " + this.mLastShowing + "\nupdateStates() - occluded = " + z6 + "mLastOccluded = " + this.mLastOccluded + "\nupdateStates() - bouncerShowing = " + isShowing + ", mLastBouncerShowing = " + this.mLastBouncerShowing + "\nupdateStates() - mFirstUpdate = " + this.mFirstUpdate);
            updateNavigationBarVisibility(isNavBarVisible);
        }
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            Log.d(TAG, "updateStates() - setBouncerShowing(" + isShowing + ")");
            this.mStatusBarWindowManager.setBouncerShowing(isShowing);
            this.mStatusBar.setBouncerShowing(isShowing);
        }
        KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        if (z5 && !z6) {
            z3 = true;
        } else {
            z3 = false;
        }
        if (this.mLastShowing && !this.mLastOccluded) {
            z4 = true;
        } else {
            z4 = false;
        }
        if (z3 != z4 || this.mFirstUpdate) {
            if (!z5 || z6) {
                z7 = false;
            }
            keyguardUpdateMonitor.onKeyguardVisibilityChanged(z7);
        }
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            keyguardUpdateMonitor.sendKeyguardBouncerChanged(isShowing);
        }
        this.mFirstUpdate = false;
        this.mLastShowing = z5;
        this.mLastOccluded = z6;
        this.mLastBouncerShowing = isShowing;
        this.mLastBouncerDismissible = z8;
        this.mLastRemoteInputActive = z9;
        this.mLastDozing = this.mDozing;
        this.mLastFpMode = this.mFingerprintUnlockController.getMode();
        this.mStatusBar.onKeyguardViewManagerStatesUpdated();
    }

    protected void updateNavigationBarVisibility(boolean z) {
        if (this.mStatusBar.getNavigationBarView() != null) {
            if (z) {
                long navBarShowDelay = getNavBarShowDelay();
                if (navBarShowDelay == 0) {
                    this.mMakeNavigationBarVisibleRunnable.run();
                    return;
                } else {
                    this.mContainer.postOnAnimationDelayed(this.mMakeNavigationBarVisibleRunnable, navBarShowDelay);
                    return;
                }
            }
            Log.d(TAG, "updateNavigationBarVisibility() - set nav bar GONE for showing notification keyguard.");
            this.mContainer.removeCallbacks(this.mMakeNavigationBarVisibleRunnable);
            this.mStatusBar.getNavigationBarView().getRootView().setVisibility(8);
        }
    }

    protected boolean isNavBarVisible() {
        return !((this.mShowing && !this.mOccluded) || (this.mDozing && this.mFingerprintUnlockController.getMode() != 2)) || this.mBouncer.isShowing() || this.mRemoteInputActive;
    }

    protected boolean getLastNavBarVisible() {
        return !((this.mLastShowing && !this.mLastOccluded) || (this.mLastDozing && this.mLastFpMode != 2)) || this.mLastBouncerShowing || this.mLastRemoteInputActive;
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mBouncer.shouldDismissOnMenuPressed();
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        return this.mBouncer.interceptMediaKey(keyEvent);
    }

    public void readyForKeyguardDone() {
        this.mViewMediatorCallback.readyForKeyguardDone();
    }

    public boolean shouldDisableWindowAnimationsForUnlock() {
        return this.mStatusBar.isInLaunchTransition();
    }

    public boolean isGoingToNotificationShade() {
        return this.mStatusBar.isGoingToNotificationShade();
    }

    public boolean isSecure(int i) {
        return this.mBouncer.isSecure() || this.mLockPatternUtils.isSecure(i);
    }

    public void keyguardGoingAway() {
        this.mStatusBar.keyguardGoingAway();
    }

    public void animateCollapsePanels(float f) {
        this.mStatusBar.animateCollapsePanels(0, true, false, f);
    }

    public void notifyKeyguardAuthenticated(boolean z) {
        this.mBouncer.notifyKeyguardAuthenticated(z);
    }

    public void showBouncerMessage(String str, int i) {
        this.mBouncer.showMessage(str, i);
    }

    public ViewRootImpl getViewRootImpl() {
        return this.mStatusBar.getStatusBarView().getViewRootImpl();
    }

    public void launchPendingWakeupAction() {
        DismissWithActionRequest dismissWithActionRequest = this.mPendingWakeupAction;
        this.mPendingWakeupAction = null;
        if (dismissWithActionRequest != null) {
            if (this.mShowing) {
                dismissWithAction(dismissWithActionRequest.dismissAction, dismissWithActionRequest.cancelAction, dismissWithActionRequest.afterKeyguardGone, dismissWithActionRequest.message);
            } else if (dismissWithActionRequest.dismissAction != null) {
                dismissWithActionRequest.dismissAction.onDismiss();
            }
        }
    }

    public void cancelPendingWakeupAction() {
        DismissWithActionRequest dismissWithActionRequest = this.mPendingWakeupAction;
        this.mPendingWakeupAction = null;
        if (dismissWithActionRequest != null && dismissWithActionRequest.cancelAction != null) {
            dismissWithActionRequest.cancelAction.run();
        }
    }

    public boolean willDismissWithAction() {
        return this.mBouncer.willDismissWithAction();
    }

    public boolean bouncerNeedsScrimming() {
        return this.mBouncer.isShowingScrimmed();
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("StatusBarKeyguardViewManager:");
        printWriter.println("  mShowing: " + this.mShowing);
        printWriter.println("  mOccluded: " + this.mOccluded);
        printWriter.println("  mRemoteInputActive: " + this.mRemoteInputActive);
        printWriter.println("  mDozing: " + this.mDozing);
        printWriter.println("  mGoingToSleepVisibleNotOccluded: " + this.mGoingToSleepVisibleNotOccluded);
        printWriter.println("  mAfterKeyguardGoneAction: " + this.mAfterKeyguardGoneAction);
        printWriter.println("  mAfterKeyguardGoneRunnables: " + this.mAfterKeyguardGoneRunnables);
        printWriter.println("  mPendingWakeupAction: " + this.mPendingWakeupAction);
        if (this.mBouncer != null) {
            this.mBouncer.dump(printWriter);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DismissWithActionRequest {
        final boolean afterKeyguardGone;
        final Runnable cancelAction;
        final KeyguardHostView.OnDismissAction dismissAction;
        final String message;

        DismissWithActionRequest(KeyguardHostView.OnDismissAction onDismissAction, Runnable runnable, boolean z, String str) {
            this.dismissAction = onDismissAction;
            this.cancelAction = runnable;
            this.afterKeyguardGone = z;
            this.message = str;
        }
    }
}
