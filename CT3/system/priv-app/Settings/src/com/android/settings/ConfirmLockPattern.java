package com.android.settings;

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
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.settings.CredentialCheckResultTracker;
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
        LockedOut;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static Stage[] valuesCustom() {
            return values();
        }
    }

    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", ConfirmLockPatternFragment.class.getName());
        return modIntent;
    }

    @Override // com.android.settings.SettingsActivity
    protected boolean isValidFragment(String fragmentName) {
        return ConfirmLockPatternFragment.class.getName().equals(fragmentName);
    }

    /* loaded from: classes.dex */
    public static class ConfirmLockPatternFragment extends ConfirmDeviceCredentialBaseFragment implements AppearAnimationCreator<Object>, CredentialCheckResultTracker.Listener {

        /* renamed from: -com-android-settings-ConfirmLockPattern$StageSwitchesValues  reason: not valid java name */
        private static final /* synthetic */ int[] f3comandroidsettingsConfirmLockPattern$StageSwitchesValues = null;
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
        private Runnable mClearPatternRunnable = new Runnable() { // from class: com.android.settings.ConfirmLockPattern.ConfirmLockPatternFragment.1
            @Override // java.lang.Runnable
            public void run() {
                ConfirmLockPatternFragment.this.mLockPatternView.clearPattern();
            }
        };
        private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener = new LockPatternView.OnPatternListener() { // from class: com.android.settings.ConfirmLockPattern.ConfirmLockPatternFragment.2
            public void onPatternStart() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCleared() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
            }

            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                if (ConfirmLockPatternFragment.this.mPendingLockCheck != null || ConfirmLockPatternFragment.this.mDisappearing) {
                    return;
                }
                ConfirmLockPatternFragment.this.mLockPatternView.setEnabled(false);
                boolean verifyChallenge = ConfirmLockPatternFragment.this.getActivity().getIntent().getBooleanExtra("has_challenge", false);
                Intent intent = new Intent();
                if (verifyChallenge) {
                    if (isInternalActivity()) {
                        startVerifyPattern(pattern, intent);
                        return;
                    } else {
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId);
                        return;
                    }
                }
                startCheckPattern(pattern, intent);
            }

            /* JADX INFO: Access modifiers changed from: private */
            public boolean isInternalActivity() {
                return ConfirmLockPatternFragment.this.getActivity() instanceof InternalActivity;
            }

            private void startVerifyPattern(List<LockPatternView.Cell> pattern, final Intent intent) {
                AsyncTask verifyTiedProfileChallenge;
                final int localEffectiveUserId = ConfirmLockPatternFragment.this.mEffectiveUserId;
                int localUserId = ConfirmLockPatternFragment.this.mUserId;
                long challenge = ConfirmLockPatternFragment.this.getActivity().getIntent().getLongExtra("challenge", 0L);
                LockPatternChecker.OnVerifyCallback onVerifyCallback = new LockPatternChecker.OnVerifyCallback() { // from class: com.android.settings.ConfirmLockPattern.ConfirmLockPatternFragment.2.1
                    public void onVerified(byte[] token, int timeoutMs) {
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        boolean matched = false;
                        if (token != null) {
                            matched = true;
                            if (ConfirmLockPatternFragment.this.mReturnCredentials) {
                                intent.putExtra("hw_auth_token", token);
                            }
                        }
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs, localEffectiveUserId);
                    }
                };
                ConfirmLockPatternFragment confirmLockPatternFragment = ConfirmLockPatternFragment.this;
                if (localEffectiveUserId == localUserId) {
                    verifyTiedProfileChallenge = LockPatternChecker.verifyPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, pattern, challenge, localUserId, onVerifyCallback);
                } else {
                    verifyTiedProfileChallenge = LockPatternChecker.verifyTiedProfileChallenge(ConfirmLockPatternFragment.this.mLockPatternUtils, LockPatternUtils.patternToString(pattern), true, challenge, localUserId, onVerifyCallback);
                }
                confirmLockPatternFragment.mPendingLockCheck = verifyTiedProfileChallenge;
            }

            private void startCheckPattern(final List<LockPatternView.Cell> pattern, final Intent intent) {
                if (pattern.size() < 4) {
                    ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId);
                    return;
                }
                final int localEffectiveUserId = ConfirmLockPatternFragment.this.mEffectiveUserId;
                ConfirmLockPatternFragment.this.mPendingLockCheck = LockPatternChecker.checkPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, pattern, localEffectiveUserId, new LockPatternChecker.OnCheckCallback() { // from class: com.android.settings.ConfirmLockPattern.ConfirmLockPatternFragment.2.2
                    public void onChecked(boolean matched, int timeoutMs) {
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        if (matched && isInternalActivity() && ConfirmLockPatternFragment.this.mReturnCredentials) {
                            intent.putExtra("type", 2);
                            intent.putExtra("password", LockPatternUtils.patternToString(pattern));
                        }
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs, localEffectiveUserId);
                    }
                });
            }
        };

        /* renamed from: -getcom-android-settings-ConfirmLockPattern$StageSwitchesValues  reason: not valid java name */
        private static /* synthetic */ int[] m301getcomandroidsettingsConfirmLockPattern$StageSwitchesValues() {
            if (f3comandroidsettingsConfirmLockPattern$StageSwitchesValues != null) {
                return f3comandroidsettingsConfirmLockPattern$StageSwitchesValues;
            }
            int[] iArr = new int[Stage.valuesCustom().length];
            try {
                iArr[Stage.LockedOut.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Stage.NeedToUnlock.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Stage.NeedToUnlockWrong.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            f3comandroidsettingsConfirmLockPattern$StageSwitchesValues = iArr;
            return iArr;
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment, com.android.settings.InstrumentedFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.confirm_lock_pattern, (ViewGroup) null);
            this.mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
            this.mLockPatternView = view.findViewById(R.id.lockPattern);
            this.mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);
            this.mErrorTextView = (TextView) view.findViewById(R.id.errorText);
            this.mLeftSpacerLandscape = view.findViewById(R.id.leftSpacer);
            this.mRightSpacerLandscape = view.findViewById(R.id.rightSpacer);
            LinearLayoutWithDefaultTouchRecepient topLayout = view.findViewById(R.id.topLayout);
            topLayout.setDefaultTouchRecepient(this.mLockPatternView);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                this.mHeaderText = intent.getCharSequenceExtra("com.android.settings.ConfirmCredentials.header");
                this.mDetailsText = intent.getCharSequenceExtra("com.android.settings.ConfirmCredentials.details");
            }
            this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
            this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(this.mEffectiveUserId));
            this.mLockPatternView.setOnPatternListener(this.mConfirmExistingLockPatternListener);
            updateStage(Stage.NeedToUnlock);
            if (savedInstanceState == null && !this.mLockPatternUtils.isLockPatternEnabled(this.mEffectiveUserId)) {
                getActivity().setResult(-1, new Intent());
                getActivity().finish();
            }
            this.mAppearAnimationUtils = new AppearAnimationUtils(getContext(), 220L, 2.0f, 1.3f, AnimationUtils.loadInterpolator(getContext(), 17563662));
            this.mDisappearAnimationUtils = new DisappearAnimationUtils(getContext(), 125L, 4.0f, 0.3f, AnimationUtils.loadInterpolator(getContext(), 17563663), new AppearAnimationUtils.RowTranslationScaler() { // from class: com.android.settings.ConfirmLockPattern.ConfirmLockPatternFragment.3
                @Override // com.android.settingslib.animation.AppearAnimationUtils.RowTranslationScaler
                public float getRowTranslationScale(int row, int numRows) {
                    return (numRows - row) / numRows;
                }
            });
            setAccessibilityTitle(this.mHeaderTextView.getText());
            this.mCredentialCheckResultTracker = (CredentialCheckResultTracker) getFragmentManager().findFragmentByTag("check_lock_result");
            if (this.mCredentialCheckResultTracker == null) {
                this.mCredentialCheckResultTracker = new CredentialCheckResultTracker();
                getFragmentManager().beginTransaction().add(this.mCredentialCheckResultTracker, "check_lock_result").commit();
            }
            return view;
        }

        @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public void onSaveInstanceState(Bundle outState) {
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment, com.android.settings.InstrumentedFragment, android.app.Fragment
        public void onPause() {
            super.onPause();
            if (this.mCountdownTimer != null) {
                this.mCountdownTimer.cancel();
            }
            this.mCredentialCheckResultTracker.setListener(null);
        }

        @Override // com.android.settings.InstrumentedFragment
        protected int getMetricsCategory() {
            return 31;
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment, com.android.settings.InstrumentedFragment, android.app.Fragment
        public void onResume() {
            super.onResume();
            long deadline = this.mLockPatternUtils.getLockoutAttemptDeadline(this.mEffectiveUserId);
            if (deadline != 0) {
                this.mCredentialCheckResultTracker.clearResult();
                handleAttemptLockout(deadline);
            } else if (!this.mLockPatternView.isEnabled()) {
                updateStage(Stage.NeedToUnlock);
            }
            this.mCredentialCheckResultTracker.setListener(this);
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment
        protected void onShowError() {
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment
        public void prepareEnterAnimation() {
            super.prepareEnterAnimation();
            this.mHeaderTextView.setAlpha(0.0f);
            this.mCancelButton.setAlpha(0.0f);
            this.mLockPatternView.setAlpha(0.0f);
            this.mDetailsTextView.setAlpha(0.0f);
            this.mFingerprintIcon.setAlpha(0.0f);
        }

        private int getDefaultDetails() {
            boolean isProfile = Utils.isManagedProfile(UserManager.get(getActivity()), this.mEffectiveUserId);
            if (isProfile) {
                if (this.mIsStrongAuthRequired) {
                    return R.string.lockpassword_strong_auth_required_reason_restart_work_pattern;
                }
                return R.string.lockpassword_confirm_your_pattern_generic_profile;
            } else if (this.mIsStrongAuthRequired) {
                return R.string.lockpassword_strong_auth_required_reason_restart_device_pattern;
            } else {
                return R.string.lockpassword_confirm_your_pattern_generic;
            }
        }

        private Object[][] getActiveViews() {
            ArrayList<ArrayList<Object>> result = new ArrayList<>();
            result.add(new ArrayList<>(Collections.singletonList(this.mHeaderTextView)));
            result.add(new ArrayList<>(Collections.singletonList(this.mDetailsTextView)));
            if (this.mCancelButton.getVisibility() == 0) {
                result.add(new ArrayList<>(Collections.singletonList(this.mCancelButton)));
            }
            LockPatternView.CellState[][] cellStates = this.mLockPatternView.getCellStates();
            for (int i = 0; i < cellStates.length; i++) {
                ArrayList<Object> row = new ArrayList<>();
                for (int j = 0; j < cellStates[i].length; j++) {
                    row.add(cellStates[i][j]);
                }
                result.add(row);
            }
            if (this.mFingerprintIcon.getVisibility() == 0) {
                result.add(new ArrayList<>(Collections.singletonList(this.mFingerprintIcon)));
            }
            Object[][] resultArr = (Object[][]) Array.newInstance(Object.class, result.size(), cellStates[0].length);
            for (int i2 = 0; i2 < result.size(); i2++) {
                ArrayList<Object> row2 = result.get(i2);
                for (int j2 = 0; j2 < row2.size(); j2++) {
                    resultArr[i2][j2] = row2.get(j2);
                }
            }
            return resultArr;
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment
        public void startEnterAnimation() {
            super.startEnterAnimation();
            this.mLockPatternView.setAlpha(1.0f);
            this.mAppearAnimationUtils.startAnimation2d(getActiveViews(), null, this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateStage(Stage stage) {
            switch (m301getcomandroidsettingsConfirmLockPattern$StageSwitchesValues()[stage.ordinal()]) {
                case 1:
                    this.mLockPatternView.clearPattern();
                    this.mLockPatternView.setEnabled(false);
                    break;
                case 2:
                    if (this.mHeaderText != null) {
                        this.mHeaderTextView.setText(this.mHeaderText);
                    } else {
                        this.mHeaderTextView.setText(R.string.lockpassword_confirm_your_pattern_header);
                    }
                    if (this.mDetailsText != null) {
                        this.mDetailsTextView.setText(this.mDetailsText);
                    } else {
                        this.mDetailsTextView.setText(getDefaultDetails());
                    }
                    this.mErrorTextView.setText("");
                    if (isProfileChallenge()) {
                        updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId));
                    }
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    this.mLockPatternView.clearPattern();
                    break;
                case 3:
                    this.mErrorTextView.setText(R.string.lockpattern_need_to_unlock_wrong);
                    this.mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    break;
            }
            this.mHeaderTextView.announceForAccessibility(this.mHeaderTextView.getText());
        }

        private void postClearPatternRunnable() {
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 2000L);
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment
        protected void authenticationSucceeded() {
            this.mCredentialCheckResultTracker.setResult(true, new Intent(), 0, this.mEffectiveUserId);
        }

        private void startDisappearAnimation(final Intent intent) {
            if (this.mDisappearing) {
                return;
            }
            this.mDisappearing = true;
            if (getActivity().getThemeResId() == 2131689967) {
                this.mLockPatternView.clearPattern();
                this.mDisappearAnimationUtils.startAnimation2d(getActiveViews(), new Runnable() { // from class: com.android.settings.ConfirmLockPattern.ConfirmLockPatternFragment.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (ConfirmLockPatternFragment.this.getActivity() == null || ConfirmLockPatternFragment.this.getActivity().isFinishing()) {
                            return;
                        }
                        ConfirmLockPatternFragment.this.getActivity().setResult(-1, intent);
                        ConfirmLockPatternFragment.this.getActivity().finish();
                        ConfirmLockPatternFragment.this.getActivity().overridePendingTransition(R.anim.confirm_credential_close_enter, R.anim.confirm_credential_close_exit);
                    }
                }, this);
                return;
            }
            getActivity().setResult(-1, intent);
            getActivity().finish();
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment, com.android.settings.fingerprint.FingerprintUiHelper.Callback
        public void onFingerprintIconVisibilityChanged(boolean visible) {
            if (this.mLeftSpacerLandscape == null || this.mRightSpacerLandscape == null) {
                return;
            }
            this.mLeftSpacerLandscape.setVisibility(visible ? 8 : 0);
            this.mRightSpacerLandscape.setVisibility(visible ? 8 : 0);
        }

        private void onPatternChecked(boolean matched, Intent intent, int timeoutMs, int effectiveUserId, boolean newResult) {
            this.mLockPatternView.setEnabled(true);
            if (matched) {
                if (newResult) {
                    reportSuccessfullAttempt();
                }
                startDisappearAnimation(intent);
                checkForPendingIntent();
                return;
            }
            if (timeoutMs > 0) {
                long deadline = this.mLockPatternUtils.setLockoutAttemptDeadline(effectiveUserId, timeoutMs);
                handleAttemptLockout(deadline);
            } else {
                updateStage(Stage.NeedToUnlockWrong);
                postClearPatternRunnable();
            }
            if (!newResult) {
                return;
            }
            reportFailedAttempt();
        }

        @Override // com.android.settings.CredentialCheckResultTracker.Listener
        public void onCredentialChecked(boolean matched, Intent intent, int timeoutMs, int effectiveUserId, boolean newResult) {
            onPatternChecked(matched, intent, timeoutMs, effectiveUserId, newResult);
        }

        @Override // com.android.settings.ConfirmDeviceCredentialBaseFragment
        protected int getLastTryErrorMessage() {
            return R.string.lock_profile_wipe_warning_content_pattern;
        }

        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.settings.ConfirmLockPattern$ConfirmLockPatternFragment$5] */
        private void handleAttemptLockout(long elapsedRealtimeDeadline) {
            updateStage(Stage.LockedOut);
            long elapsedRealtime = SystemClock.elapsedRealtime();
            this.mCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - elapsedRealtime, 1000L) { // from class: com.android.settings.ConfirmLockPattern.ConfirmLockPatternFragment.5
                @Override // android.os.CountDownTimer
                public void onTick(long millisUntilFinished) {
                    int secondsCountdown = (int) (millisUntilFinished / 1000);
                    ConfirmLockPatternFragment.this.mErrorTextView.setText(ConfirmLockPatternFragment.this.getString(R.string.lockpattern_too_many_failed_confirmation_attempts, new Object[]{Integer.valueOf(secondsCountdown)}));
                }

                @Override // android.os.CountDownTimer
                public void onFinish() {
                    ConfirmLockPatternFragment.this.updateStage(Stage.NeedToUnlock);
                }
            }.start();
        }

        @Override // com.android.settingslib.animation.AppearAnimationCreator
        public void createAnimation(Object obj, long delay, long duration, float translationY, boolean appearing, Interpolator interpolator, Runnable finishListener) {
            if (obj instanceof LockPatternView.CellState) {
                LockPatternView.CellState animatedCell = (LockPatternView.CellState) obj;
                this.mLockPatternView.startCellStateAnimation(animatedCell, 1.0f, appearing ? 1.0f : 0.0f, appearing ? translationY : 0.0f, appearing ? 0.0f : translationY, appearing ? 0.0f : 1.0f, 1.0f, delay, duration, interpolator, finishListener);
                return;
            }
            this.mAppearAnimationUtils.createAnimation((View) obj, delay, duration, translationY, appearing, interpolator, finishListener);
        }
    }
}
