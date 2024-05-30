package com.android.keyguard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyButton;
/* loaded from: classes.dex */
public abstract class KeyguardAbsKeyInputView extends LinearLayout implements EmergencyButton.EmergencyButtonCallback, KeyguardSecurityView {
    protected KeyguardSecurityCallback mCallback;
    private CountDownTimer mCountdownTimer;
    private boolean mDismissing;
    protected View mEcaView;
    protected boolean mEnableHaptics;
    protected LockPatternUtils mLockPatternUtils;
    protected AsyncTask<?, ?, ?> mPendingLockCheck;
    protected SecurityMessageDisplay mSecurityMessageDisplay;

    protected abstract String getPasswordText();

    /* JADX INFO: Access modifiers changed from: protected */
    public abstract int getPasswordTextViewId();

    protected abstract int getPromptReasonStringRes(int i);

    protected abstract void resetPasswordText(boolean z, boolean z2);

    protected abstract void resetState();

    protected abstract void setPasswordEntryEnabled(boolean z);

    protected abstract void setPasswordEntryInputEnabled(boolean z);

    public KeyguardAbsKeyInputView(Context context) {
        this(context, null);
    }

    public KeyguardAbsKeyInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCountdownTimer = null;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mEnableHaptics = this.mLockPatternUtils.isTactileFeedbackEnabled();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
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

    protected boolean shouldLockout(long j) {
        return j != 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mSecurityMessageDisplay = KeyguardMessageArea.findSecurityMessageDisplay(this);
        this.mEcaView = findViewById(com.android.systemui.R.id.keyguard_selector_fade_container);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(com.android.systemui.R.id.emergency_call_button);
        if (emergencyButton != null) {
            emergencyButton.setCallback(this);
        }
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    protected int getWrongPasswordStringId() {
        return com.android.systemui.R.string.kg_wrong_password;
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
        final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (passwordText.length() <= 3) {
            setPasswordEntryInputEnabled(true);
            onPasswordChecked(currentUser, false, 0, false);
            return;
        }
        if (LatencyTracker.isEnabled(this.mContext)) {
            LatencyTracker.getInstance(this.mContext).onActionStart(3);
            LatencyTracker.getInstance(this.mContext).onActionStart(4);
        }
        this.mPendingLockCheck = LockPatternChecker.checkPassword(this.mLockPatternUtils, passwordText, currentUser, new LockPatternChecker.OnCheckCallback() { // from class: com.android.keyguard.KeyguardAbsKeyInputView.1
            public void onEarlyMatched() {
                if (LatencyTracker.isEnabled(KeyguardAbsKeyInputView.this.mContext)) {
                    LatencyTracker.getInstance(KeyguardAbsKeyInputView.this.mContext).onActionEnd(3);
                }
                KeyguardAbsKeyInputView.this.onPasswordChecked(currentUser, true, 0, true);
            }

            public void onChecked(boolean z, int i) {
                if (LatencyTracker.isEnabled(KeyguardAbsKeyInputView.this.mContext)) {
                    LatencyTracker.getInstance(KeyguardAbsKeyInputView.this.mContext).onActionEnd(4);
                }
                KeyguardAbsKeyInputView.this.setPasswordEntryInputEnabled(true);
                KeyguardAbsKeyInputView.this.mPendingLockCheck = null;
                if (!z) {
                    KeyguardAbsKeyInputView.this.onPasswordChecked(currentUser, false, i, true);
                }
            }

            public void onCancelled() {
                if (LatencyTracker.isEnabled(KeyguardAbsKeyInputView.this.mContext)) {
                    LatencyTracker.getInstance(KeyguardAbsKeyInputView.this.mContext).onActionEnd(4);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPasswordChecked(int i, boolean z, int i2, boolean z2) {
        boolean z3 = KeyguardUpdateMonitor.getCurrentUser() == i;
        if (z) {
            this.mCallback.reportUnlockAttempt(i, true, 0);
            if (z3) {
                this.mDismissing = true;
                this.mCallback.dismiss(true, i);
            }
        } else {
            if (z2) {
                this.mCallback.reportUnlockAttempt(i, false, i2);
                if (i2 > 0) {
                    handleAttemptLockout(this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                }
            }
            if (i2 == 0) {
                this.mSecurityMessageDisplay.setMessage(getWrongPasswordStringId());
            }
        }
        resetPasswordText(true, !z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Type inference failed for: r0v4, types: [com.android.keyguard.KeyguardAbsKeyInputView$2] */
    public void handleAttemptLockout(long j) {
        setPasswordEntryEnabled(false);
        long ceil = (long) Math.ceil((j - SystemClock.elapsedRealtime()) / 1000.0d);
        if (this.mCountdownTimer != null) {
            this.mCountdownTimer.cancel();
        }
        this.mCountdownTimer = new CountDownTimer(ceil * 1000, 1000L) { // from class: com.android.keyguard.KeyguardAbsKeyInputView.2
            @Override // android.os.CountDownTimer
            public void onTick(long j2) {
                int round = (int) Math.round(j2 / 1000.0d);
                KeyguardAbsKeyInputView.this.mSecurityMessageDisplay.setMessage(KeyguardAbsKeyInputView.this.mContext.getResources().getQuantityString(com.android.systemui.R.plurals.kg_too_many_failed_attempts_countdown, round, Integer.valueOf(round)));
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                KeyguardAbsKeyInputView.this.mSecurityMessageDisplay.setMessage("");
                KeyguardAbsKeyInputView.this.resetState();
            }
        }.start();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onUserInput() {
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
        this.mSecurityMessageDisplay.setMessage("");
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i != 0) {
            onUserInput();
            return false;
        }
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        if (this.mCountdownTimer != null) {
            this.mCountdownTimer.cancel();
            this.mCountdownTimer = null;
        }
        if (this.mPendingLockCheck != null) {
            this.mPendingLockCheck.cancel(false);
            this.mPendingLockCheck = null;
        }
        reset();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        int promptReasonStringRes;
        if (i != 0 && (promptReasonStringRes = getPromptReasonStringRes(i)) != 0) {
            this.mSecurityMessageDisplay.setMessage(promptReasonStringRes);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(CharSequence charSequence, int i) {
        this.mSecurityMessageDisplay.setNextMessageColor(i);
        this.mSecurityMessageDisplay.setMessage(charSequence);
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }
}
