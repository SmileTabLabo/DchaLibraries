package com.android.keyguard;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityModel;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager;
import java.io.File;
/* loaded from: a.zip:com/android/keyguard/KeyguardHostView.class */
public class KeyguardHostView extends FrameLayout implements KeyguardSecurityContainer.SecurityCallback {
    private AudioManager mAudioManager;
    private Runnable mCancelAction;
    private OnDismissAction mDismissAction;
    protected LockPatternUtils mLockPatternUtils;
    private KeyguardSecurityContainer mSecurityContainer;
    private TelephonyManager mTelephonyManager;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    protected ViewMediatorCallback mViewMediatorCallback;

    /* loaded from: a.zip:com/android/keyguard/KeyguardHostView$OnDismissAction.class */
    public interface OnDismissAction {
        boolean onDismiss();
    }

    public KeyguardHostView(Context context) {
        this(context, null);
    }

    public KeyguardHostView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTelephonyManager = null;
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.KeyguardHostView.1
            final KeyguardHostView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTrustGrantedWithFlags(int i, int i2) {
                if (i2 == KeyguardUpdateMonitor.getCurrentUser() && this.this$0.isAttachedToWindow()) {
                    boolean isVisibleToUser = this.this$0.isVisibleToUser();
                    boolean z = (i & 1) != 0;
                    boolean z2 = (i & 2) != 0;
                    if (z || z2) {
                        if (!this.this$0.mViewMediatorCallback.isScreenOn() || (!isVisibleToUser && !z2)) {
                            this.this$0.mViewMediatorCallback.playTrustedSound();
                            return;
                        }
                        if (!isVisibleToUser) {
                            Log.i("KeyguardViewBase", "TrustAgent dismissed Keyguard.");
                        }
                        this.this$0.dismiss(false);
                    }
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i) {
                this.this$0.getSecurityContainer().showPrimarySecurityScreen(false);
            }
        };
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mUpdateCallback);
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        synchronized (this) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            }
        }
        this.mAudioManager.dispatchMediaKeyEvent(keyEvent);
    }

    public void cancelDismissAction() {
        setOnDismissAction(null, null);
    }

    public void cleanUp() {
        getSecurityContainer().onPause();
    }

    public boolean dismiss() {
        if (AntiTheftManager.isDismissable()) {
            return dismiss(false);
        }
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public boolean dismiss(boolean z) {
        return this.mSecurityContainer.showNextSecurityScreenOrFinish(z);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mViewMediatorCallback != null) {
            this.mViewMediatorCallback.keyguardDoneDrawing();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (interceptMediaKey(keyEvent)) {
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() == 32) {
            accessibilityEvent.getText().add(this.mSecurityContainer.getCurrentSecurityModeContentDescription());
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(accessibilityEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchSystemUiVisibilityChanged(int i) {
        super.dispatchSystemUiVisibilityChanged(i);
        if (this.mContext instanceof Activity) {
            return;
        }
        setSystemUiVisibility(4194304);
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void finish(boolean z) {
        boolean z2 = false;
        if (this.mDismissAction != null) {
            z2 = this.mDismissAction.onDismiss();
            this.mDismissAction = null;
            this.mCancelAction = null;
        } else if (VoiceWakeupManager.getInstance().isDismissAndLaunchApp()) {
            Log.d("KeyguardViewBase", "finish() - call VoiceWakeupManager.getInstance().onDismiss().");
            VoiceWakeupManager.getInstance().onDismiss();
            z2 = false;
        }
        if (this.mViewMediatorCallback != null) {
            if (z2) {
                this.mViewMediatorCallback.keyguardDonePending(z);
            } else {
                this.mViewMediatorCallback.keyguardDone(z);
            }
        }
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mSecurityContainer.getCurrentSecurityMode();
    }

    protected KeyguardSecurityContainer getSecurityContainer() {
        return this.mSecurityContainer;
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityContainer.getSecurityMode();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean interceptMediaKey(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyEvent.getAction() != 0) {
            if (keyEvent.getAction() == 1) {
                switch (keyCode) {
                    case 79:
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                    case 89:
                    case 90:
                    case 91:
                    case 126:
                    case 127:
                    case 130:
                    case 222:
                        handleMediaKeyEvent(keyEvent);
                        return true;
                    default:
                        return false;
                }
            }
            return false;
        }
        switch (keyCode) {
            case 24:
            case 25:
            case 164:
                return false;
            case 79:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 130:
            case 222:
                break;
            case 85:
            case 126:
            case 127:
                if (this.mTelephonyManager == null) {
                    this.mTelephonyManager = (TelephonyManager) getContext().getSystemService("phone");
                }
                if (this.mTelephonyManager != null && this.mTelephonyManager.getCallState() != 0) {
                    return true;
                }
                break;
            default:
                return false;
        }
        handleMediaKeyEvent(keyEvent);
        return true;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        this.mSecurityContainer = (KeyguardSecurityContainer) findViewById(R$id.keyguard_security_container);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mSecurityContainer.setLockPatternUtils(this.mLockPatternUtils);
        this.mSecurityContainer.setSecurityCallback(this);
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void onPause() {
        Log.d("KeyguardViewBase", String.format("screen off, instance %s at %s", Integer.toHexString(hashCode()), Long.valueOf(SystemClock.uptimeMillis())));
        KeyguardUpdateMonitor.getInstance(this.mContext).setAlternateUnlockEnabled(true);
        this.mSecurityContainer.showPrimarySecurityScreen(true);
        this.mSecurityContainer.onPause();
        clearFocus();
    }

    public void onResume() {
        Log.d("KeyguardViewBase", "screen on, instance " + Integer.toHexString(hashCode()));
        this.mSecurityContainer.onResume(1);
        requestFocus();
    }

    public void onScreenTurnedOff() {
        this.mSecurityContainer.onScreenTurnedOff();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z) {
        if (this.mViewMediatorCallback != null) {
            this.mViewMediatorCallback.setNeedsInput(z);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void reset() {
        this.mViewMediatorCallback.resetKeyguard();
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityContainer.setLockPatternUtils(lockPatternUtils);
    }

    public void setNotificationPanelView(ViewGroup viewGroup) {
        this.mSecurityContainer.setNotificationPanelView(viewGroup);
    }

    public void setOnDismissAction(OnDismissAction onDismissAction) {
        this.mDismissAction = onDismissAction;
    }

    public void setOnDismissAction(OnDismissAction onDismissAction, Runnable runnable) {
        if (this.mCancelAction != null) {
            this.mCancelAction.run();
            this.mCancelAction = null;
        }
        this.mDismissAction = onDismissAction;
        this.mCancelAction = runnable;
    }

    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mViewMediatorCallback.setNeedsInput(this.mSecurityContainer.needsInput());
    }

    public boolean shouldEnableMenuKey() {
        boolean z = getResources().getBoolean(R$bool.config_disableMenuKeyInLockScreen);
        boolean isRunningInTestHarness = ActivityManager.isRunningInTestHarness();
        boolean exists = new File("/data/local/enable_menu_key").exists();
        if (!z || isRunningInTestHarness) {
            exists = true;
        }
        return exists;
    }

    public void showMessage(String str, int i) {
        this.mSecurityContainer.showMessage(str, i);
    }

    public void showPrimarySecurityScreen() {
        Log.d("KeyguardViewBase", "show()");
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void showPromptReason(int i) {
        this.mSecurityContainer.showPromptReason(i);
    }

    public void startAppearAnimation() {
        this.mSecurityContainer.startAppearAnimation();
    }

    public void startDisappearAnimation(Runnable runnable) {
        if (this.mSecurityContainer.startDisappearAnimation(runnable) || runnable == null) {
            return;
        }
        runnable.run();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void updateNavbarStatus() {
        this.mViewMediatorCallback.updateNavbarStatus();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void userActivity() {
        if (this.mViewMediatorCallback != null) {
            this.mViewMediatorCallback.userActivity();
        }
    }
}
