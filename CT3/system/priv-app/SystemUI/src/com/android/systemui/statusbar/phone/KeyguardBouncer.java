package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.Context;
import android.os.UserManager;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.DejankUtils;
import com.android.systemui.classifier.FalsingManager;
import com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardBouncer.class */
public class KeyguardBouncer {
    private int mBouncerPromptReason;
    private ViewMediatorCallback mCallback;
    private ViewGroup mContainer;
    private Context mContext;
    private FalsingManager mFalsingManager;
    protected KeyguardHostView mKeyguardView;
    private LockPatternUtils mLockPatternUtils;
    private ViewGroup mNotificationPanel;
    protected ViewGroup mRoot;
    private KeyguardSecurityModel mSecurityModel;
    private boolean mShowingSoon;
    private StatusBarWindowManager mWindowManager;
    private final String TAG = "KeyguardBouncer";
    private final boolean DEBUG = true;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBouncer.1
        final KeyguardBouncer this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            this.this$0.mBouncerPromptReason = this.this$0.mCallback.getBouncerPromptReason();
        }
    };
    private final Runnable mShowRunnable = new AnonymousClass2(this);

    /* renamed from: com.android.systemui.statusbar.phone.KeyguardBouncer$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardBouncer$2.class */
    class AnonymousClass2 implements Runnable {
        final KeyguardBouncer this$0;

        AnonymousClass2(KeyguardBouncer keyguardBouncer) {
            this.this$0 = keyguardBouncer;
        }

        @Override // java.lang.Runnable
        public void run() {
            Log.d("KeyguardBouncer", "mShowRunnable.run() is called.");
            this.this$0.mRoot.setVisibility(0);
            this.this$0.mKeyguardView.onResume();
            this.this$0.showPromptReason(this.this$0.mBouncerPromptReason);
            if (this.this$0.mKeyguardView.getHeight() != 0) {
                this.this$0.mKeyguardView.startAppearAnimation();
            } else {
                this.this$0.mKeyguardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBouncer.2.1
                    final AnonymousClass2 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // android.view.ViewTreeObserver.OnPreDrawListener
                    public boolean onPreDraw() {
                        this.this$1.this$0.mKeyguardView.getViewTreeObserver().removeOnPreDrawListener(this);
                        this.this$1.this$0.mKeyguardView.startAppearAnimation();
                        return true;
                    }
                });
                this.this$0.mKeyguardView.requestLayout();
            }
            this.this$0.mShowingSoon = false;
            this.this$0.mKeyguardView.sendAccessibilityEvent(32);
        }
    }

    public KeyguardBouncer(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, StatusBarWindowManager statusBarWindowManager, ViewGroup viewGroup) {
        this.mContext = context;
        this.mCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mContainer = viewGroup;
        this.mWindowManager = statusBarWindowManager;
        this.mNotificationPanel = (ViewGroup) this.mContainer.findViewById(2131886681);
        this.mSecurityModel = new KeyguardSecurityModel(this.mContext);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
    }

    private void cancelShowRunnable() {
        DejankUtils.removeCallbacks(this.mShowRunnable);
        this.mShowingSoon = false;
    }

    protected void ensureView() {
        if (this.mRoot == null) {
            inflateView();
        }
    }

    public void hide(boolean z) {
        this.mFalsingManager.onBouncerHidden();
        Log.d("KeyguardBouncer", "hide() is called, destroyView = " + z);
        cancelShowRunnable();
        if (this.mKeyguardView != null) {
            this.mKeyguardView.cancelDismissAction();
            this.mKeyguardView.cleanUp();
        }
        if (z) {
            Log.d("KeyguardBouncer", "call removeView()");
            removeView();
        } else if (this.mRoot != null) {
            Log.d("KeyguardBouncer", "just set keyguard Invisible.");
            this.mRoot.setVisibility(4);
        }
        Log.d("KeyguardBouncer", "hide() - user has left keyguard, setAlternateUnlockEnabled(true)");
        KeyguardUpdateMonitor.getInstance(this.mContext).setAlternateUnlockEnabled(true);
    }

    protected void inflateView() {
        Log.d("KeyguardBouncer", "inflateView() is called, we force to re-inflate the \"Bouncer\" view.");
        removeView();
        this.mRoot = (ViewGroup) LayoutInflater.from(this.mContext).inflate(2130968632, (ViewGroup) null);
        this.mKeyguardView = (KeyguardHostView) this.mRoot.findViewById(2131886300);
        this.mKeyguardView.setLockPatternUtils(this.mLockPatternUtils);
        this.mKeyguardView.setViewMediatorCallback(this.mCallback);
        this.mKeyguardView.setNotificationPanelView(this.mNotificationPanel);
        this.mContainer.addView(this.mRoot, this.mContainer.getChildCount());
        this.mRoot.setVisibility(4);
        this.mRoot.setSystemUiVisibility(2097152);
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        ensureView();
        return this.mKeyguardView.interceptMediaKey(keyEvent);
    }

    public boolean isFullscreenBouncer() {
        if (this.mKeyguardView != null) {
            KeyguardSecurityModel.SecurityMode currentSecurityMode = this.mKeyguardView.getCurrentSecurityMode();
            boolean z = true;
            if (currentSecurityMode != KeyguardSecurityModel.SecurityMode.SimPinPukMe1) {
                if (currentSecurityMode == KeyguardSecurityModel.SecurityMode.SimPinPukMe2) {
                    z = true;
                } else {
                    z = true;
                    if (currentSecurityMode != KeyguardSecurityModel.SecurityMode.SimPinPukMe3) {
                        z = true;
                        if (currentSecurityMode != KeyguardSecurityModel.SecurityMode.SimPinPukMe4) {
                            z = true;
                            if (currentSecurityMode != KeyguardSecurityModel.SecurityMode.AntiTheft) {
                                z = true;
                                if (currentSecurityMode != KeyguardSecurityModel.SecurityMode.AlarmBoot) {
                                    z = false;
                                }
                            }
                        }
                    }
                }
            }
            return z;
        }
        return false;
    }

    public boolean isSecure() {
        boolean z = true;
        if (this.mKeyguardView != null) {
            z = this.mKeyguardView.getSecurityMode() != KeyguardSecurityModel.SecurityMode.None;
        }
        return z;
    }

    public boolean isShowing() {
        boolean z = true;
        if (!this.mShowingSoon) {
            z = this.mRoot != null && this.mRoot.getVisibility() == 0;
        }
        return z;
    }

    public boolean needsFullscreenBouncer() {
        ensureView();
        KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityModel.getSecurityMode();
        boolean z = true;
        if (securityMode != KeyguardSecurityModel.SecurityMode.SimPinPukMe1) {
            if (securityMode == KeyguardSecurityModel.SecurityMode.SimPinPukMe2) {
                z = true;
            } else {
                z = true;
                if (securityMode != KeyguardSecurityModel.SecurityMode.SimPinPukMe3) {
                    z = true;
                    if (securityMode != KeyguardSecurityModel.SecurityMode.SimPinPukMe4) {
                        z = true;
                        if (securityMode != KeyguardSecurityModel.SecurityMode.AntiTheft) {
                            z = true;
                            if (securityMode != KeyguardSecurityModel.SecurityMode.AlarmBoot) {
                                z = false;
                            }
                        }
                    }
                }
            }
        }
        return z;
    }

    public void notifyKeyguardAuthenticated(boolean z) {
        ensureView();
        this.mKeyguardView.finish(z);
    }

    public void onScreenTurnedOff() {
        if (this.mKeyguardView == null || this.mRoot == null || this.mRoot.getVisibility() != 0) {
            return;
        }
        this.mKeyguardView.onScreenTurnedOff();
        this.mKeyguardView.onPause();
    }

    public void prepare() {
        boolean z = this.mRoot != null;
        ensureView();
        if (z) {
            this.mKeyguardView.showPrimarySecurityScreen();
        }
        this.mBouncerPromptReason = this.mCallback.getBouncerPromptReason();
    }

    protected void removeView() {
        if (this.mRoot == null || this.mRoot.getParent() != this.mContainer) {
            return;
        }
        Log.d("KeyguardBouncer", "removeView() - really remove all views.");
        this.mContainer.removeView(this.mRoot);
        this.mRoot = null;
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mKeyguardView.shouldEnableMenuKey();
    }

    public void show(boolean z) {
        if (KeyguardUpdateMonitor.getCurrentUser() == 0 && UserManager.isSplitSystemUser()) {
            return;
        }
        this.mFalsingManager.onBouncerShown();
        ensureView();
        show(z, false);
    }

    public void show(boolean z, boolean z2) {
        Log.d("KeyguardBouncer", "show(resetSecuritySelection = " + z);
        if (PowerOffAlarmManager.isAlarmBoot()) {
            Log.d("KeyguardBouncer", "show() - this is alarm boot, just re-inflate.");
            if (this.mKeyguardView != null && this.mRoot != null) {
                Log.d("KeyguardBouncer", "show() - before re-inflate, we should pause current view.");
                this.mKeyguardView.onPause();
            }
            inflateView();
        } else {
            ensureView();
        }
        if (z) {
            this.mKeyguardView.showPrimarySecurityScreen();
        }
        if (this.mRoot.getVisibility() == 0 || this.mShowingSoon) {
            return;
        }
        int currentUser = ActivityManager.getCurrentUser();
        int currentUser2 = KeyguardUpdateMonitor.getCurrentUser();
        boolean z3 = false;
        if (!(UserManager.isSplitSystemUser() && currentUser == 0)) {
            z3 = false;
            if (currentUser == currentUser2) {
                z3 = true;
            }
        }
        if (z3 && this.mKeyguardView.dismiss()) {
            return;
        }
        if (!z3) {
            Slog.w("KeyguardBouncer", "User can't dismiss keyguard: " + currentUser + " != " + currentUser2);
        }
        if (this.mKeyguardView.dismiss(z2)) {
            return;
        }
        Log.d("KeyguardBouncer", "show() - try to dismiss \"Bouncer\" directly.");
        this.mShowingSoon = true;
        DejankUtils.postAfterTraversal(this.mShowRunnable);
    }

    public void showMessage(String str, int i) {
        this.mKeyguardView.showMessage(str, i);
    }

    public void showPromptReason(int i) {
        this.mKeyguardView.showPromptReason(i);
    }

    public void showWithDismissAction(KeyguardHostView.OnDismissAction onDismissAction) {
        ensureView();
        this.mKeyguardView.setOnDismissAction(onDismissAction);
        show(false);
    }

    public void startPreHideAnimation(Runnable runnable) {
        if (this.mKeyguardView != null) {
            this.mKeyguardView.startDisappearAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }
}
