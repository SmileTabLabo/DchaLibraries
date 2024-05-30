package com.android.settings.password;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.settings.R;
import com.android.settings.password.ConfirmDeviceCredentialBaseActivity;
import com.android.settings.password.ConfirmLockPattern;
import com.android.settings.password.CredentialCheckResultTracker;
import com.android.settingslib.animation.AppearAnimationCreator;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class ConfirmLockPattern extends ConfirmDeviceCredentialBaseActivity {

    /* loaded from: classes.dex */
    public static class InternalActivity extends ConfirmLockPattern {
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }

    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        intent.putExtra(":settings:show_fragment", ConfirmLockPatternFragment.class.getName());
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity
    public boolean isValidFragment(String str) {
        return ConfirmLockPatternFragment.class.getName().equals(str);
    }

    /* loaded from: classes.dex */
    public static class ConfirmLockPatternFragment extends ConfirmDeviceCredentialBaseFragment implements CredentialCheckResultTracker.Listener, AppearAnimationCreator<Object> {
        private AppearAnimationUtils mAppearAnimationUtils;
        private CountDownTimer mCountdownTimer;
        private CredentialCheckResultTracker mCredentialCheckResultTracker;
        private CharSequence mDetailsText;
        private TextView mDetailsTextView;
        private DisappearAnimationUtils mDisappearAnimationUtils;
        private CharSequence mHeaderText;
        private TextView mHeaderTextView;
        private View mLeftSpacerLandscape;
        private LockPatternView mLockPatternView;
        private AsyncTask<?, ?, ?> mPendingLockCheck;
        private View mRightSpacerLandscape;
        private boolean mDisappearing = false;
        private Runnable mClearPatternRunnable = new Runnable() { // from class: com.android.settings.password.ConfirmLockPattern.ConfirmLockPatternFragment.2
            @Override // java.lang.Runnable
            public void run() {
                ConfirmLockPatternFragment.this.mLockPatternView.clearPattern();
            }
        };
        private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener = new LockPatternView.OnPatternListener() { // from class: com.android.settings.password.ConfirmLockPattern.ConfirmLockPatternFragment.3
            public void onPatternStart() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCleared() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCellAdded(List<LockPatternView.Cell> list) {
            }

            public void onPatternDetected(List<LockPatternView.Cell> list) {
                if (ConfirmLockPatternFragment.this.mPendingLockCheck == null && !ConfirmLockPatternFragment.this.mDisappearing) {
                    ConfirmLockPatternFragment.this.mLockPatternView.setEnabled(false);
                    boolean booleanExtra = ConfirmLockPatternFragment.this.getActivity().getIntent().getBooleanExtra("has_challenge", false);
                    Intent intent = new Intent();
                    if (booleanExtra) {
                        if (!isInternalActivity()) {
                            ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId);
                            return;
                        } else {
                            startVerifyPattern(list, intent);
                            return;
                        }
                    }
                    startCheckPattern(list, intent);
                }
            }

            /* JADX INFO: Access modifiers changed from: private */
            public boolean isInternalActivity() {
                return ConfirmLockPatternFragment.this.getActivity() instanceof InternalActivity;
            }

            private void startVerifyPattern(List<LockPatternView.Cell> list, final Intent intent) {
                AsyncTask verifyTiedProfileChallenge;
                final int i = ConfirmLockPatternFragment.this.mEffectiveUserId;
                int i2 = ConfirmLockPatternFragment.this.mUserId;
                long longExtra = ConfirmLockPatternFragment.this.getActivity().getIntent().getLongExtra("challenge", 0L);
                LockPatternChecker.OnVerifyCallback onVerifyCallback = new LockPatternChecker.OnVerifyCallback() { // from class: com.android.settings.password.ConfirmLockPattern.ConfirmLockPatternFragment.3.1
                    public void onVerified(byte[] bArr, int i3) {
                        boolean z;
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        if (bArr != null) {
                            z = true;
                            if (ConfirmLockPatternFragment.this.mReturnCredentials) {
                                intent.putExtra("hw_auth_token", bArr);
                            }
                        } else {
                            z = false;
                        }
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(z, intent, i3, i);
                    }
                };
                ConfirmLockPatternFragment confirmLockPatternFragment = ConfirmLockPatternFragment.this;
                if (i == i2) {
                    verifyTiedProfileChallenge = LockPatternChecker.verifyPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, list, longExtra, i2, onVerifyCallback);
                } else {
                    verifyTiedProfileChallenge = LockPatternChecker.verifyTiedProfileChallenge(ConfirmLockPatternFragment.this.mLockPatternUtils, LockPatternUtils.patternToString(list), true, longExtra, i2, onVerifyCallback);
                }
                confirmLockPatternFragment.mPendingLockCheck = verifyTiedProfileChallenge;
            }

            private void startCheckPattern(final List<LockPatternView.Cell> list, final Intent intent) {
                if (list.size() < 4) {
                    ConfirmLockPatternFragment.this.onPatternChecked(false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId, false);
                    return;
                }
                final int i = ConfirmLockPatternFragment.this.mEffectiveUserId;
                ConfirmLockPatternFragment.this.mPendingLockCheck = LockPatternChecker.checkPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, list, i, new LockPatternChecker.OnCheckCallback() { // from class: com.android.settings.password.ConfirmLockPattern.ConfirmLockPatternFragment.3.2
                    public void onChecked(boolean z, int i2) {
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        if (z && isInternalActivity() && ConfirmLockPatternFragment.this.mReturnCredentials) {
                            intent.putExtra("type", 2);
                            intent.putExtra("password", LockPatternUtils.patternToString(list));
                        }
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(z, intent, i2, i);
                    }
                });
            }
        };

        @Override // android.app.Fragment
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
            int i;
            if (((ConfirmLockPattern) getActivity()).getConfirmCredentialTheme() == ConfirmDeviceCredentialBaseActivity.ConfirmCredentialTheme.NORMAL) {
                i = R.layout.confirm_lock_pattern_normal;
            } else {
                i = R.layout.confirm_lock_pattern;
            }
            View inflate = layoutInflater.inflate(i, viewGroup, false);
            this.mHeaderTextView = (TextView) inflate.findViewById(R.id.headerText);
            this.mLockPatternView = inflate.findViewById(R.id.lockPattern);
            this.mDetailsTextView = (TextView) inflate.findViewById(R.id.detailsText);
            this.mErrorTextView = (TextView) inflate.findViewById(R.id.errorText);
            this.mLeftSpacerLandscape = inflate.findViewById(R.id.leftSpacer);
            this.mRightSpacerLandscape = inflate.findViewById(R.id.rightSpacer);
            inflate.findViewById(R.id.topLayout).setDefaultTouchRecepient(this.mLockPatternView);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                this.mHeaderText = intent.getCharSequenceExtra("com.android.settings.ConfirmCredentials.header");
                this.mDetailsText = intent.getCharSequenceExtra("com.android.settings.ConfirmCredentials.details");
            }
            this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
            this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(this.mEffectiveUserId));
            this.mLockPatternView.setOnPatternListener(this.mConfirmExistingLockPatternListener);
            updateStage(Stage.NeedToUnlock);
            if (bundle == null && !this.mFrp && !this.mLockPatternUtils.isLockPatternEnabled(this.mEffectiveUserId)) {
                getActivity().setResult(-1);
                getActivity().finish();
            }
            this.mAppearAnimationUtils = new AppearAnimationUtils(getContext(), 220L, 2.0f, 1.3f, AnimationUtils.loadInterpolator(getContext(), 17563662));
            this.mDisappearAnimationUtils = new DisappearAnimationUtils(getContext(), 125L, 4.0f, 0.3f, AnimationUtils.loadInterpolator(getContext(), 17563663), new AppearAnimationUtils.RowTranslationScaler() { // from class: com.android.settings.password.ConfirmLockPattern.ConfirmLockPatternFragment.1
                @Override // com.android.settingslib.animation.AppearAnimationUtils.RowTranslationScaler
                public float getRowTranslationScale(int i2, int i3) {
                    return (i3 - i2) / i3;
                }
            });
            setAccessibilityTitle(this.mHeaderTextView.getText());
            this.mCredentialCheckResultTracker = (CredentialCheckResultTracker) getFragmentManager().findFragmentByTag("check_lock_result");
            if (this.mCredentialCheckResultTracker == null) {
                this.mCredentialCheckResultTracker = new CredentialCheckResultTracker();
                getFragmentManager().beginTransaction().add(this.mCredentialCheckResultTracker, "check_lock_result").commit();
            }
            return inflate;
        }

        @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
        public void onSaveInstanceState(Bundle bundle) {
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment, com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
        public void onPause() {
            super.onPause();
            if (this.mCountdownTimer != null) {
                this.mCountdownTimer.cancel();
            }
            this.mCredentialCheckResultTracker.setListener(null);
        }

        @Override // com.android.settingslib.core.instrumentation.Instrumentable
        public int getMetricsCategory() {
            return 31;
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment, com.android.settings.core.InstrumentedFragment, com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
        public void onResume() {
            super.onResume();
            long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(this.mEffectiveUserId);
            if (lockoutAttemptDeadline != 0) {
                this.mCredentialCheckResultTracker.clearResult();
                handleAttemptLockout(lockoutAttemptDeadline);
            } else if (!this.mLockPatternView.isEnabled()) {
                updateStage(Stage.NeedToUnlock);
            }
            this.mCredentialCheckResultTracker.setListener(this);
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment
        protected void onShowError() {
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment
        public void prepareEnterAnimation() {
            super.prepareEnterAnimation();
            this.mHeaderTextView.setAlpha(0.0f);
            this.mCancelButton.setAlpha(0.0f);
            this.mLockPatternView.setAlpha(0.0f);
            this.mDetailsTextView.setAlpha(0.0f);
            this.mFingerprintIcon.setAlpha(0.0f);
        }

        private int getDefaultDetails() {
            if (this.mFrp) {
                return R.string.lockpassword_confirm_your_pattern_details_frp;
            }
            boolean isStrongAuthRequired = isStrongAuthRequired();
            if (UserManager.get(getActivity()).isManagedProfile(this.mEffectiveUserId)) {
                if (isStrongAuthRequired) {
                    return R.string.lockpassword_strong_auth_required_work_pattern;
                }
                return R.string.lockpassword_confirm_your_pattern_generic_profile;
            } else if (isStrongAuthRequired) {
                return R.string.lockpassword_strong_auth_required_device_pattern;
            } else {
                return R.string.lockpassword_confirm_your_pattern_generic;
            }
        }

        private Object[][] getActiveViews() {
            ArrayList arrayList = new ArrayList();
            arrayList.add(new ArrayList(Collections.singletonList(this.mHeaderTextView)));
            arrayList.add(new ArrayList(Collections.singletonList(this.mDetailsTextView)));
            if (this.mCancelButton.getVisibility() == 0) {
                arrayList.add(new ArrayList(Collections.singletonList(this.mCancelButton)));
            }
            LockPatternView.CellState[][] cellStates = this.mLockPatternView.getCellStates();
            for (int i = 0; i < cellStates.length; i++) {
                ArrayList arrayList2 = new ArrayList();
                for (int i2 = 0; i2 < cellStates[i].length; i2++) {
                    arrayList2.add(cellStates[i][i2]);
                }
                arrayList.add(arrayList2);
            }
            if (this.mFingerprintIcon.getVisibility() == 0) {
                arrayList.add(new ArrayList(Collections.singletonList(this.mFingerprintIcon)));
            }
            Object[][] objArr = (Object[][]) Array.newInstance(Object.class, arrayList.size(), cellStates[0].length);
            for (int i3 = 0; i3 < arrayList.size(); i3++) {
                ArrayList arrayList3 = (ArrayList) arrayList.get(i3);
                for (int i4 = 0; i4 < arrayList3.size(); i4++) {
                    objArr[i3][i4] = arrayList3.get(i4);
                }
            }
            return objArr;
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment
        public void startEnterAnimation() {
            super.startEnterAnimation();
            this.mLockPatternView.setAlpha(1.0f);
            this.mAppearAnimationUtils.startAnimation2d(getActiveViews(), null, this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateStage(Stage stage) {
            switch (stage) {
                case NeedToUnlock:
                    if (this.mHeaderText != null) {
                        this.mHeaderTextView.setText(this.mHeaderText);
                    } else {
                        this.mHeaderTextView.setText(getDefaultHeader());
                    }
                    if (this.mDetailsText != null) {
                        this.mDetailsTextView.setText(this.mDetailsText);
                    } else {
                        this.mDetailsTextView.setText(getDefaultDetails());
                    }
                    this.mErrorTextView.setText("");
                    updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId));
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    this.mLockPatternView.clearPattern();
                    break;
                case NeedToUnlockWrong:
                    showError(R.string.lockpattern_need_to_unlock_wrong, 3000L);
                    this.mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    break;
                case LockedOut:
                    this.mLockPatternView.clearPattern();
                    this.mLockPatternView.setEnabled(false);
                    break;
            }
            this.mHeaderTextView.announceForAccessibility(this.mHeaderTextView.getText());
        }

        private int getDefaultHeader() {
            return this.mFrp ? R.string.lockpassword_confirm_your_pattern_header_frp : R.string.lockpassword_confirm_your_pattern_header;
        }

        private void postClearPatternRunnable() {
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 3000L);
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment
        protected void authenticationSucceeded() {
            this.mCredentialCheckResultTracker.setResult(true, new Intent(), 0, this.mEffectiveUserId);
        }

        private void startDisappearAnimation(final Intent intent) {
            if (this.mDisappearing) {
                return;
            }
            this.mDisappearing = true;
            final ConfirmLockPattern confirmLockPattern = (ConfirmLockPattern) getActivity();
            if (confirmLockPattern == null || confirmLockPattern.isFinishing()) {
                return;
            }
            if (confirmLockPattern.getConfirmCredentialTheme() == ConfirmDeviceCredentialBaseActivity.ConfirmCredentialTheme.DARK) {
                this.mLockPatternView.clearPattern();
                this.mDisappearAnimationUtils.startAnimation2d(getActiveViews(), new Runnable() { // from class: com.android.settings.password.-$$Lambda$ConfirmLockPattern$ConfirmLockPatternFragment$5mgp_p2Jjy9apKG7HsLV4Zu-sXo
                    @Override // java.lang.Runnable
                    public final void run() {
                        ConfirmLockPattern.ConfirmLockPatternFragment.lambda$startDisappearAnimation$0(ConfirmLockPattern.this, intent);
                    }
                }, this);
                return;
            }
            confirmLockPattern.setResult(-1, intent);
            confirmLockPattern.finish();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ void lambda$startDisappearAnimation$0(ConfirmLockPattern confirmLockPattern, Intent intent) {
            confirmLockPattern.setResult(-1, intent);
            confirmLockPattern.finish();
            confirmLockPattern.overridePendingTransition(R.anim.confirm_credential_close_enter, R.anim.confirm_credential_close_exit);
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment, com.android.settings.fingerprint.FingerprintUiHelper.Callback
        public void onFingerprintIconVisibilityChanged(boolean z) {
            if (this.mLeftSpacerLandscape != null && this.mRightSpacerLandscape != null) {
                this.mLeftSpacerLandscape.setVisibility(z ? 8 : 0);
                this.mRightSpacerLandscape.setVisibility(z ? 8 : 0);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onPatternChecked(boolean z, Intent intent, int i, int i2, boolean z2) {
            this.mLockPatternView.setEnabled(true);
            if (z) {
                if (z2) {
                    reportSuccessfulAttempt();
                }
                startDisappearAnimation(intent);
                checkForPendingIntent();
                return;
            }
            if (i > 0) {
                refreshLockScreen();
                handleAttemptLockout(this.mLockPatternUtils.setLockoutAttemptDeadline(i2, i));
            } else {
                updateStage(Stage.NeedToUnlockWrong);
                postClearPatternRunnable();
            }
            if (z2) {
                reportFailedAttempt();
            }
        }

        @Override // com.android.settings.password.CredentialCheckResultTracker.Listener
        public void onCredentialChecked(boolean z, Intent intent, int i, int i2, boolean z2) {
            onPatternChecked(z, intent, i, i2, z2);
        }

        @Override // com.android.settings.password.ConfirmDeviceCredentialBaseFragment
        protected int getLastTryErrorMessage(int i) {
            switch (i) {
                case 1:
                    return R.string.lock_last_pattern_attempt_before_wipe_device;
                case 2:
                    return R.string.lock_last_pattern_attempt_before_wipe_profile;
                case 3:
                    return R.string.lock_last_pattern_attempt_before_wipe_user;
                default:
                    throw new IllegalArgumentException("Unrecognized user type:" + i);
            }
        }

        /* JADX WARN: Type inference failed for: r8v0, types: [com.android.settings.password.ConfirmLockPattern$ConfirmLockPatternFragment$4] */
        private void handleAttemptLockout(long j) {
            updateStage(Stage.LockedOut);
            this.mCountdownTimer = new CountDownTimer(j - SystemClock.elapsedRealtime(), 1000L) { // from class: com.android.settings.password.ConfirmLockPattern.ConfirmLockPatternFragment.4
                @Override // android.os.CountDownTimer
                public void onTick(long j2) {
                    ConfirmLockPatternFragment.this.mErrorTextView.setText(ConfirmLockPatternFragment.this.getString(R.string.lockpattern_too_many_failed_confirmation_attempts, new Object[]{Integer.valueOf((int) (j2 / 1000))}));
                }

                @Override // android.os.CountDownTimer
                public void onFinish() {
                    ConfirmLockPatternFragment.this.updateStage(Stage.NeedToUnlock);
                }
            }.start();
        }

        @Override // com.android.settingslib.animation.AppearAnimationCreator
        public void createAnimation(Object obj, long j, long j2, float f, boolean z, Interpolator interpolator, Runnable runnable) {
            if (obj instanceof LockPatternView.CellState) {
                this.mLockPatternView.startCellStateAnimation((LockPatternView.CellState) obj, 1.0f, z ? 1.0f : 0.0f, z ? f : 0.0f, z ? 0.0f : f, z ? 0.0f : 1.0f, 1.0f, j, j2, interpolator, runnable);
                return;
            }
            this.mAppearAnimationUtils.createAnimation((View) obj, j, j2, f, z, interpolator, runnable);
        }
    }
}
