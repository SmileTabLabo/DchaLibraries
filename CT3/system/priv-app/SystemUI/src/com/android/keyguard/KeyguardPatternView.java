package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.media.AudioSystem;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.keyguard.EmergencyButton;
import com.android.settingslib.animation.AppearAnimationCreator;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import java.util.List;
/* loaded from: a.zip:com/android/keyguard/KeyguardPatternView.class */
public class KeyguardPatternView extends LinearLayout implements KeyguardSecurityView, AppearAnimationCreator<LockPatternView.CellState>, EmergencyButton.EmergencyButtonCallback {
    private final AppearAnimationUtils mAppearAnimationUtils;
    private KeyguardSecurityCallback mCallback;
    private Runnable mCancelPatternRunnable;
    private ViewGroup mContainer;
    private CountDownTimer mCountdownTimer;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private int mDisappearYTranslation;
    private View mEcaView;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private long mLastPokeTime;
    private LockPatternUtils mLockPatternUtils;
    private LockPatternView mLockPatternView;
    private AsyncTask<?, ?, ?> mPendingLockCheck;
    private KeyguardMessageArea mSecurityMessageDisplay;
    private Rect mTempRect;

    /* loaded from: a.zip:com/android/keyguard/KeyguardPatternView$UnlockPatternListener.class */
    private class UnlockPatternListener implements LockPatternView.OnPatternListener {
        final KeyguardPatternView this$0;

        private UnlockPatternListener(KeyguardPatternView keyguardPatternView) {
            this.this$0 = keyguardPatternView;
        }

        /* synthetic */ UnlockPatternListener(KeyguardPatternView keyguardPatternView, UnlockPatternListener unlockPatternListener) {
            this(keyguardPatternView);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onPatternChecked(int i, boolean z, int i2, boolean z2) {
            boolean z3 = KeyguardUpdateMonitor.getCurrentUser() == i;
            if (z) {
                this.this$0.mCallback.reportUnlockAttempt(i, true, 0);
                if (z3) {
                    this.this$0.mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                    this.this$0.mCallback.dismiss(true);
                    return;
                }
                return;
            }
            this.this$0.mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
            if (z2) {
                this.this$0.mCallback.reportUnlockAttempt(i, false, i2);
                if (i2 > 0) {
                    this.this$0.handleAttemptLockout(this.this$0.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                }
            }
            if (i2 == 0) {
                this.this$0.mSecurityMessageDisplay.setMessage(R$string.kg_wrong_pattern, true);
                this.this$0.mLockPatternView.postDelayed(this.this$0.mCancelPatternRunnable, 2000L);
            }
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> list) {
            this.this$0.mCallback.userActivity();
        }

        public void onPatternCleared() {
        }

        public void onPatternDetected(List<LockPatternView.Cell> list) {
            this.this$0.mLockPatternView.disableInput();
            if (this.this$0.mPendingLockCheck != null) {
                this.this$0.mPendingLockCheck.cancel(false);
            }
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (list.size() < 4) {
                this.this$0.mLockPatternView.enableInput();
                onPatternChecked(currentUser, false, 0, false);
                return;
            }
            this.this$0.mPendingLockCheck = LockPatternChecker.checkPattern(this.this$0.mLockPatternUtils, list, currentUser, new LockPatternChecker.OnCheckCallback(this, currentUser) { // from class: com.android.keyguard.KeyguardPatternView.UnlockPatternListener.1
                final UnlockPatternListener this$1;
                final int val$userId;

                {
                    this.this$1 = this;
                    this.val$userId = currentUser;
                }

                public void onChecked(boolean z, int i) {
                    this.this$1.this$0.mLockPatternView.enableInput();
                    this.this$1.this$0.mPendingLockCheck = null;
                    this.this$1.onPatternChecked(this.val$userId, z, i, true);
                }
            });
            if (list.size() > 2) {
                this.this$0.mCallback.userActivity();
            }
        }

        public void onPatternStart() {
            this.this$0.mLockPatternView.removeCallbacks(this.this$0.mCancelPatternRunnable);
            this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) "", false);
        }
    }

    public KeyguardPatternView(Context context) {
        this(context, null);
    }

    public KeyguardPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCountdownTimer = null;
        this.mLastPokeTime = -7000L;
        this.mCancelPatternRunnable = new Runnable(this) { // from class: com.android.keyguard.KeyguardPatternView.1
            final KeyguardPatternView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mLockPatternView.clearPattern();
            }
        };
        this.mTempRect = new Rect();
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mAppearAnimationUtils = new AppearAnimationUtils(context, 220L, 1.5f, 2.0f, AnimationUtils.loadInterpolator(this.mContext, 17563662));
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125L, 1.2f, 0.6f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R$dimen.disappear_y_translation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void displayDefaultSecurityMessage() {
        if (!this.mKeyguardUpdateMonitor.getMaxBiometricUnlockAttemptsReached()) {
            this.mSecurityMessageDisplay.setMessage(R$string.kg_pattern_instructions, false);
            return;
        }
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mKeyguardUpdateMonitor;
        if (lockPatternUtils.usingVoiceWeak(KeyguardUpdateMonitor.getCurrentUser())) {
            this.mSecurityMessageDisplay.setMessage(R$string.voiceunlock_multiple_failures, true);
            this.mKeyguardUpdateMonitor.setAlternateUnlockEnabled(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void enableClipping(boolean z) {
        setClipChildren(z);
        this.mContainer.setClipToPadding(z);
        this.mContainer.setClipChildren(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r1v1, types: [com.android.keyguard.KeyguardPatternView$2] */
    public void handleAttemptLockout(long j) {
        this.mLockPatternView.clearPattern();
        this.mLockPatternView.setEnabled(false);
        this.mCountdownTimer = new CountDownTimer(this, j - SystemClock.elapsedRealtime(), 1000L) { // from class: com.android.keyguard.KeyguardPatternView.2
            final KeyguardPatternView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                this.this$0.mLockPatternView.setEnabled(true);
                this.this$0.displayDefaultSecurityMessage();
            }

            @Override // android.os.CountDownTimer
            public void onTick(long j2) {
                this.this$0.mSecurityMessageDisplay.setMessage(R$string.kg_too_many_failed_attempts_countdown, true, Integer.valueOf((int) (j2 / 1000)));
            }
        }.start();
    }

    @Override // com.android.settingslib.animation.AppearAnimationCreator
    public void createAnimation(LockPatternView.CellState cellState, long j, long j2, float f, boolean z, Interpolator interpolator, Runnable runnable) {
        this.mLockPatternView.startCellStateAnimation(cellState, 1.0f, z ? 1.0f : 0.0f, z ? f : 0.0f, z ? 0.0f : f, z ? 0.0f : 1.0f, 1.0f, j, j2, interpolator, runnable);
        if (runnable != null) {
            this.mAppearAnimationUtils.createAnimation(this.mEcaView, j, j2, f, z, interpolator, (Runnable) null);
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return false;
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = this.mLockPatternUtils == null ? new LockPatternUtils(this.mContext) : this.mLockPatternUtils;
        this.mLockPatternView = findViewById(R$id.lockPatternView);
        this.mLockPatternView.setSaveEnabled(false);
        this.mLockPatternView.setOnPatternListener(new UnlockPatternListener(this, null));
        this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
        this.mSecurityMessageDisplay = (KeyguardMessageArea) KeyguardMessageArea.findSecurityMessageDisplay(this);
        this.mEcaView = findViewById(R$id.keyguard_selector_fade_container);
        this.mContainer = (ViewGroup) findViewById(R$id.container);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(R$id.emergency_call_button);
        if (emergencyButton != null) {
            emergencyButton.setCallback(this);
        }
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
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        reset();
        boolean isStreamActive = AudioSystem.isStreamActive(3, 0);
        if (this.mLockPatternUtils.usingVoiceWeak() && isStreamActive) {
            this.mSecurityMessageDisplay.setMessage(R$string.voice_unlock_media_playing, true);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long j = this.mLastPokeTime;
        if (onTouchEvent && elapsedRealtime - j > 6900) {
            this.mLastPokeTime = SystemClock.elapsedRealtime();
        }
        this.mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(this.mLockPatternView, this.mTempRect);
        motionEvent.offsetLocation(this.mTempRect.left, this.mTempRect.top);
        if (this.mLockPatternView.dispatchTouchEvent(motionEvent)) {
            onTouchEvent = true;
        }
        motionEvent.offsetLocation(-this.mTempRect.left, -this.mTempRect.top);
        return onTouchEvent;
    }

    public void reset() {
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(KeyguardUpdateMonitor.getCurrentUser()));
        this.mLockPatternView.enableInput();
        this.mLockPatternView.setEnabled(true);
        this.mLockPatternView.clearPattern();
        long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
        if (lockoutAttemptDeadline != 0) {
            handleAttemptLockout(lockoutAttemptDeadline);
        } else {
            displayDefaultSecurityMessage();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(String str, int i) {
        this.mSecurityMessageDisplay.setNextMessageColor(i);
        this.mSecurityMessageDisplay.setMessage((CharSequence) str, true);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        switch (i) {
            case 0:
                return;
            case 1:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_restart_pattern, true);
                return;
            case 2:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_timeout_pattern, true);
                return;
            case 3:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_device_admin, true);
                return;
            case 4:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_user_request, true);
                return;
            default:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_timeout_pattern, true);
                return;
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        enableClipping(false);
        setAlpha(1.0f);
        setTranslationY(this.mAppearAnimationUtils.getStartTranslation());
        AppearAnimationUtils.startTranslationYAnimation(this, 0L, 500L, 0.0f, this.mAppearAnimationUtils.getInterpolator());
        this.mAppearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), new Runnable(this) { // from class: com.android.keyguard.KeyguardPatternView.3
            final KeyguardPatternView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.enableClipping(true);
            }
        }, this);
        if (TextUtils.isEmpty(this.mSecurityMessageDisplay.getText())) {
            return;
        }
        this.mAppearAnimationUtils.createAnimation((View) this.mSecurityMessageDisplay, 0L, 220L, this.mAppearAnimationUtils.getStartTranslation(), true, this.mAppearAnimationUtils.getInterpolator(), (Runnable) null);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        this.mLockPatternView.clearPattern();
        enableClipping(false);
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0L, 300L, -this.mDisappearAnimationUtils.getStartTranslation(), this.mDisappearAnimationUtils.getInterpolator());
        this.mDisappearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), new Runnable(this, runnable) { // from class: com.android.keyguard.KeyguardPatternView.4
            final KeyguardPatternView this$0;
            final Runnable val$finishRunnable;

            {
                this.this$0 = this;
                this.val$finishRunnable = runnable;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.enableClipping(true);
                if (this.val$finishRunnable != null) {
                    this.val$finishRunnable.run();
                }
            }
        }, this);
        if (TextUtils.isEmpty(this.mSecurityMessageDisplay.getText())) {
            return true;
        }
        this.mDisappearAnimationUtils.createAnimation((View) this.mSecurityMessageDisplay, 0L, 200L, (-this.mDisappearAnimationUtils.getStartTranslation()) * 3.0f, false, this.mDisappearAnimationUtils.getInterpolator(), (Runnable) null);
        return true;
    }
}
