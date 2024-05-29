package com.android.keyguard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyButton;
/* loaded from: a.zip:com/android/keyguard/KeyguardAbsKeyInputView.class */
public abstract class KeyguardAbsKeyInputView extends LinearLayout implements KeyguardSecurityView, EmergencyButton.EmergencyButtonCallback {
    protected KeyguardSecurityCallback mCallback;
    private boolean mDismissing;
    protected View mEcaView;
    protected boolean mEnableHaptics;
    protected LockPatternUtils mLockPatternUtils;
    protected AsyncTask<?, ?, ?> mPendingLockCheck;
    protected SecurityMessageDisplay mSecurityMessageDisplay;

    public KeyguardAbsKeyInputView(Context context) {
        this(context, null);
    }

    public KeyguardAbsKeyInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPasswordChecked(int i, boolean z, int i2, boolean z2) {
        boolean z3 = KeyguardUpdateMonitor.getCurrentUser() == i;
        if (z) {
            this.mCallback.reportUnlockAttempt(i, true, 0);
            if (z3) {
                this.mDismissing = true;
                this.mCallback.dismiss(true);
            }
        } else {
            if (z2) {
                this.mCallback.reportUnlockAttempt(i, false, i2);
                if (i2 > 0) {
                    handleAttemptLockout(this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                }
            }
            if (i2 == 0) {
                this.mSecurityMessageDisplay.setMessage(getWrongPasswordStringId(), true);
            }
        }
        resetPasswordText(true, !z);
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    protected abstract String getPasswordText();

    /* JADX INFO: Access modifiers changed from: protected */
    public abstract int getPasswordTextViewId();

    protected abstract int getPromtReasonStringRes(int i);

    protected int getWrongPasswordStringId() {
        return R$string.kg_wrong_password;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.keyguard.KeyguardAbsKeyInputView$2] */
    public void handleAttemptLockout(long j) {
        setPasswordEntryEnabled(false);
        new CountDownTimer(this, j - SystemClock.elapsedRealtime(), 1000L) { // from class: com.android.keyguard.KeyguardAbsKeyInputView.2
            final KeyguardAbsKeyInputView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) "", false);
                this.this$0.resetState();
            }

            @Override // android.os.CountDownTimer
            public void onTick(long j2) {
                this.this$0.mSecurityMessageDisplay.setMessage(R$string.kg_too_many_failed_attempts_countdown, true, Integer.valueOf((int) (j2 / 1000)));
            }
        }.start();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return false;
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mSecurityMessageDisplay = KeyguardMessageArea.findSecurityMessageDisplay(this);
        this.mEcaView = findViewById(R$id.keyguard_selector_fade_container);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(R$id.emergency_call_button);
        if (emergencyButton != null) {
            emergencyButton.setCallback(this);
        }
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        onUserInput();
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        if (this.mPendingLockCheck != null) {
            this.mPendingLockCheck.cancel(false);
            this.mPendingLockCheck = null;
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        reset();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onUserInput() {
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
        this.mSecurityMessageDisplay.setMessage((CharSequence) "", false);
    }

    public void reset() {
        this.mDismissing = false;
        resetPasswordText(false, false);
        long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
        if (shouldLockout(lockoutAttemptDeadline)) {
            handleAttemptLockout(lockoutAttemptDeadline);
        } else {
            resetState();
        }
    }

    protected abstract void resetPasswordText(boolean z, boolean z2);

    protected abstract void resetState();

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mEnableHaptics = this.mLockPatternUtils.isTactileFeedbackEnabled();
    }

    protected abstract void setPasswordEntryEnabled(boolean z);

    protected abstract void setPasswordEntryInputEnabled(boolean z);

    protected boolean shouldLockout(long j) {
        return j != 0;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(String str, int i) {
        this.mSecurityMessageDisplay.setNextMessageColor(i);
        this.mSecurityMessageDisplay.setMessage((CharSequence) str, true);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        int promtReasonStringRes;
        if (i == 0 || (promtReasonStringRes = getPromtReasonStringRes(i)) == 0) {
            return;
        }
        this.mSecurityMessageDisplay.setMessage(promtReasonStringRes, true);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void verifyPasswordAndUnlock() {
        if (this.mDismissing) {
            return;
        }
        String passwordText = getPasswordText();
        setPasswordEntryInputEnabled(false);
        if (this.mPendingLockCheck != null) {
            this.mPendingLockCheck.cancel(false);
        }
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (passwordText.length() > 3) {
            this.mPendingLockCheck = LockPatternChecker.checkPassword(this.mLockPatternUtils, passwordText, currentUser, new LockPatternChecker.OnCheckCallback(this, currentUser) { // from class: com.android.keyguard.KeyguardAbsKeyInputView.1
                final KeyguardAbsKeyInputView this$0;
                final int val$userId;

                {
                    this.this$0 = this;
                    this.val$userId = currentUser;
                }

                public void onChecked(boolean z, int i) {
                    this.this$0.setPasswordEntryInputEnabled(true);
                    this.this$0.mPendingLockCheck = null;
                    this.this$0.onPasswordChecked(this.val$userId, z, i, true);
                }
            });
            return;
        }
        setPasswordEntryInputEnabled(true);
        onPasswordChecked(currentUser, false, 0, false);
    }
}
