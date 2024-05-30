package com.android.settings.password;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.security.KeyStore;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.EncryptionInterstitial;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.fingerprint.FingerprintEnrollFindSensor;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockPassword;
import com.android.settings.password.ChooseLockPattern;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import java.util.List;
/* loaded from: classes.dex */
public class ChooseLockGeneric extends SettingsActivity {

    /* loaded from: classes.dex */
    public static class InternalActivity extends ChooseLockGeneric {
    }

    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        intent.putExtra(":settings:show_fragment", getFragmentClass().getName());
        String action = intent.getAction();
        if ("android.app.action.SET_NEW_PASSWORD".equals(action) || "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD".equals(action)) {
            intent.putExtra(":settings:hide_drawer", true);
        }
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity
    public boolean isValidFragment(String str) {
        return ChooseLockGenericFragment.class.getName().equals(str);
    }

    Class<? extends Fragment> getFragmentClass() {
        return ChooseLockGenericFragment.class;
    }

    /* loaded from: classes.dex */
    public static class ChooseLockGenericFragment extends SettingsPreferenceFragment {
        private long mChallenge;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private ChooseLockGenericController mController;
        private DevicePolicyManager mDPM;
        private boolean mEncryptionRequestDisabled;
        private int mEncryptionRequestQuality;
        private FingerprintManager mFingerprintManager;
        private KeyStore mKeyStore;
        private LockPatternUtils mLockPatternUtils;
        private ManagedLockPasswordProvider mManagedPasswordProvider;
        private int mUserId;
        private UserManager mUserManager;
        private String mUserPassword;
        private boolean mHasChallenge = false;
        private boolean mPasswordConfirmed = false;
        private boolean mWaitingForConfirmation = false;
        private boolean mForChangeCredRequiredForBoot = false;
        private boolean mHideDrawer = false;
        private boolean mIsSetNewPassword = false;
        protected boolean mForFingerprint = false;

        @Override // com.android.settingslib.core.instrumentation.Instrumentable
        public int getMetricsCategory() {
            return 27;
        }

        @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Activity activity = getActivity();
            if (!Utils.isDeviceProvisioned(activity) && !canRunBeforeDeviceProvisioned()) {
                activity.finish();
                return;
            }
            String action = getActivity().getIntent().getAction();
            this.mFingerprintManager = Utils.getFingerprintManagerOrNull(getActivity());
            this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
            this.mKeyStore = KeyStore.getInstance();
            this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
            this.mIsSetNewPassword = "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD".equals(action) || "android.app.action.SET_NEW_PASSWORD".equals(action);
            boolean booleanExtra = getActivity().getIntent().getBooleanExtra("confirm_credentials", true);
            if (getActivity() instanceof InternalActivity) {
                this.mPasswordConfirmed = !booleanExtra;
                this.mUserPassword = getActivity().getIntent().getStringExtra("password");
            }
            this.mHideDrawer = getActivity().getIntent().getBooleanExtra(":settings:hide_drawer", false);
            this.mHasChallenge = getActivity().getIntent().getBooleanExtra("has_challenge", false);
            this.mChallenge = getActivity().getIntent().getLongExtra("challenge", 0L);
            this.mForFingerprint = getActivity().getIntent().getBooleanExtra("for_fingerprint", false);
            this.mForChangeCredRequiredForBoot = getArguments() != null && getArguments().getBoolean("for_cred_req_boot");
            this.mUserManager = UserManager.get(getActivity());
            if (bundle != null) {
                this.mPasswordConfirmed = bundle.getBoolean("password_confirmed");
                this.mWaitingForConfirmation = bundle.getBoolean("waiting_for_confirmation");
                this.mEncryptionRequestQuality = bundle.getInt("encrypt_requested_quality");
                this.mEncryptionRequestDisabled = bundle.getBoolean("encrypt_requested_disabled");
                if (this.mUserPassword == null) {
                    this.mUserPassword = bundle.getString("password");
                }
            }
            this.mUserId = Utils.getSecureTargetUser(getActivity().getActivityToken(), UserManager.get(getActivity()), getArguments(), getActivity().getIntent().getExtras()).getIdentifier();
            this.mController = new ChooseLockGenericController(getContext(), this.mUserId);
            if ("android.app.action.SET_NEW_PASSWORD".equals(action) && UserManager.get(getActivity()).isManagedProfile(this.mUserId) && this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mUserId)) {
                getActivity().setTitle(R.string.lock_settings_picker_title_profile);
            }
            this.mManagedPasswordProvider = ManagedLockPasswordProvider.get(getActivity(), this.mUserId);
            if (this.mPasswordConfirmed) {
                updatePreferencesOrFinish(bundle != null);
                if (this.mForChangeCredRequiredForBoot) {
                    maybeEnableEncryption(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId), false);
                }
            } else if (!this.mWaitingForConfirmation) {
                ChooseLockSettingsHelper chooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity(), this);
                if (((UserManager.get(getActivity()).isManagedProfile(this.mUserId) && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mUserId)) && !this.mIsSetNewPassword) || !chooseLockSettingsHelper.launchConfirmationActivity(100, getString(R.string.unlock_set_unlock_launch_picker_title), true, this.mUserId)) {
                    this.mPasswordConfirmed = true;
                    updatePreferencesOrFinish(bundle != null);
                } else {
                    this.mWaitingForConfirmation = true;
                }
            }
            addHeaderView();
        }

        protected boolean canRunBeforeDeviceProvisioned() {
            return false;
        }

        protected void addHeaderView() {
            if (this.mForFingerprint) {
                setHeaderView(R.layout.choose_lock_generic_fingerprint_header);
                if (this.mIsSetNewPassword) {
                    ((TextView) getHeaderView().findViewById(R.id.fingerprint_header_description)).setText(R.string.fingerprint_unlock_title);
                }
            }
        }

        @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
        public boolean onPreferenceTreeClick(Preference preference) {
            String key = preference.getKey();
            if (!isUnlockMethodSecure(key) && this.mLockPatternUtils.isSecure(this.mUserId)) {
                showFactoryResetProtectionWarningDialog(key);
                return true;
            } else if ("unlock_skip_fingerprint".equals(key)) {
                Intent intent = new Intent(getActivity(), InternalActivity.class);
                intent.setAction(getIntent().getAction());
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                intent.putExtra("confirm_credentials", !this.mPasswordConfirmed);
                if (this.mUserPassword != null) {
                    intent.putExtra("password", this.mUserPassword);
                }
                startActivityForResult(intent, 104);
                return true;
            } else {
                return setUnlockMethod(key);
            }
        }

        private void maybeEnableEncryption(int i, boolean z) {
            int i2;
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService("device_policy");
            if (UserManager.get(getActivity()).isAdminUser() && this.mUserId == UserHandle.myUserId() && LockPatternUtils.isDeviceEncryptionEnabled() && !LockPatternUtils.isFileEncryptionEnabled() && !devicePolicyManager.getDoNotAskCredentialsOnBoot()) {
                this.mEncryptionRequestQuality = i;
                this.mEncryptionRequestDisabled = z;
                Intent intentForUnlockMethod = getIntentForUnlockMethod(i);
                intentForUnlockMethod.putExtra("for_cred_req_boot", this.mForChangeCredRequiredForBoot);
                Activity activity = getActivity();
                Intent encryptionInterstitialIntent = getEncryptionInterstitialIntent(activity, i, this.mLockPatternUtils.isCredentialRequiredToDecrypt(!AccessibilityManager.getInstance(activity).isEnabled()), intentForUnlockMethod);
                encryptionInterstitialIntent.putExtra("for_fingerprint", this.mForFingerprint);
                encryptionInterstitialIntent.putExtra(":settings:hide_drawer", this.mHideDrawer);
                if (this.mIsSetNewPassword && this.mHasChallenge) {
                    i2 = 103;
                } else {
                    i2 = 101;
                }
                startActivityForResult(encryptionInterstitialIntent, i2);
            } else if (this.mForChangeCredRequiredForBoot) {
                finish();
            } else {
                updateUnlockMethodAndFinish(i, z, false);
            }
        }

        @Override // android.app.Fragment
        public void onActivityResult(int i, int i2, Intent intent) {
            super.onActivityResult(i, i2, intent);
            this.mWaitingForConfirmation = false;
            if (i == 100 && i2 == -1) {
                this.mPasswordConfirmed = true;
                this.mUserPassword = intent.getStringExtra("password");
                updatePreferencesOrFinish(false);
                if (this.mForChangeCredRequiredForBoot) {
                    if (!TextUtils.isEmpty(this.mUserPassword)) {
                        maybeEnableEncryption(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId), false);
                    } else {
                        finish();
                    }
                }
            } else if (i == 102 || i == 101) {
                if (i2 != 0 || this.mForChangeCredRequiredForBoot) {
                    getActivity().setResult(i2, intent);
                    finish();
                } else if (getIntent().getIntExtra("lockscreen.password_type", -1) != -1) {
                    getActivity().setResult(0, intent);
                    finish();
                }
            } else if (i == 103 && i2 == 1) {
                Intent findSensorIntent = getFindSensorIntent(getActivity());
                if (intent != null) {
                    findSensorIntent.putExtras(intent.getExtras());
                }
                findSensorIntent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                startActivity(findSensorIntent);
                finish();
            } else if (i != 104) {
                getActivity().setResult(0);
                finish();
            } else if (i2 != 0) {
                Activity activity = getActivity();
                if (i2 == 1) {
                    i2 = -1;
                }
                activity.setResult(i2, intent);
                finish();
            }
            if (i == 0 && this.mForChangeCredRequiredForBoot) {
                finish();
            }
        }

        protected Intent getFindSensorIntent(Context context) {
            return new Intent(context, FingerprintEnrollFindSensor.class);
        }

        @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
        public void onSaveInstanceState(Bundle bundle) {
            super.onSaveInstanceState(bundle);
            bundle.putBoolean("password_confirmed", this.mPasswordConfirmed);
            bundle.putBoolean("waiting_for_confirmation", this.mWaitingForConfirmation);
            bundle.putInt("encrypt_requested_quality", this.mEncryptionRequestQuality);
            bundle.putBoolean("encrypt_requested_disabled", this.mEncryptionRequestDisabled);
            if (this.mUserPassword != null) {
                bundle.putString("password", this.mUserPassword);
            }
        }

        private void updatePreferencesOrFinish(boolean z) {
            Intent intent = getActivity().getIntent();
            int intExtra = intent.getIntExtra("lockscreen.password_type", -1);
            if (intExtra != -1) {
                if (!z) {
                    updateUnlockMethodAndFinish(intExtra, false, true);
                    return;
                }
                return;
            }
            int upgradeQuality = this.mController.upgradeQuality(intent.getIntExtra("minimum_quality", -1));
            boolean booleanExtra = intent.getBooleanExtra("hide_disabled_prefs", false);
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (preferenceScreen != null) {
                preferenceScreen.removeAll();
            }
            addPreferences();
            disableUnusablePreferences(upgradeQuality, booleanExtra);
            updatePreferenceText();
            updateCurrentPreference();
            updatePreferenceSummaryIfNeeded();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public void addPreferences() {
            addPreferencesFromResource(R.xml.security_settings_picker);
            findPreference(ScreenLockType.NONE.preferenceKey).setViewId(R.id.lock_none);
            findPreference("unlock_skip_fingerprint").setViewId(R.id.lock_none);
            findPreference(ScreenLockType.PIN.preferenceKey).setViewId(R.id.lock_pin);
            findPreference(ScreenLockType.PASSWORD.preferenceKey).setViewId(R.id.lock_password);
        }

        private void updatePreferenceText() {
            if (this.mForFingerprint) {
                setPreferenceTitle(ScreenLockType.PATTERN, R.string.fingerprint_unlock_set_unlock_pattern);
                setPreferenceTitle(ScreenLockType.PIN, R.string.fingerprint_unlock_set_unlock_pin);
                setPreferenceTitle(ScreenLockType.PASSWORD, R.string.fingerprint_unlock_set_unlock_password);
            }
            if (this.mManagedPasswordProvider.isSettingManagedPasswordSupported()) {
                setPreferenceTitle(ScreenLockType.MANAGED, this.mManagedPasswordProvider.getPickerOptionTitle(this.mForFingerprint));
            } else {
                removePreference(ScreenLockType.MANAGED.preferenceKey);
            }
            if (!this.mForFingerprint || !this.mIsSetNewPassword) {
                removePreference("unlock_skip_fingerprint");
            }
        }

        private void setPreferenceTitle(ScreenLockType screenLockType, int i) {
            Preference findPreference = findPreference(screenLockType.preferenceKey);
            if (findPreference != null) {
                findPreference.setTitle(i);
            }
        }

        private void setPreferenceTitle(ScreenLockType screenLockType, CharSequence charSequence) {
            Preference findPreference = findPreference(screenLockType.preferenceKey);
            if (findPreference != null) {
                findPreference.setTitle(charSequence);
            }
        }

        private void setPreferenceSummary(ScreenLockType screenLockType, int i) {
            Preference findPreference = findPreference(screenLockType.preferenceKey);
            if (findPreference != null) {
                findPreference.setSummary(i);
            }
        }

        private void updateCurrentPreference() {
            Preference findPreference = findPreference(getKeyForCurrent());
            if (findPreference != null) {
                findPreference.setSummary(R.string.current_screen_lock);
            }
        }

        private String getKeyForCurrent() {
            int credentialOwnerProfile = UserManager.get(getContext()).getCredentialOwnerProfile(this.mUserId);
            if (this.mLockPatternUtils.isLockScreenDisabled(credentialOwnerProfile)) {
                return ScreenLockType.NONE.preferenceKey;
            }
            ScreenLockType fromQuality = ScreenLockType.fromQuality(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(credentialOwnerProfile));
            if (fromQuality != null) {
                return fromQuality.preferenceKey;
            }
            return null;
        }

        protected void disableUnusablePreferences(int i, boolean z) {
            disableUnusablePreferencesImpl(i, z);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public void disableUnusablePreferencesImpl(int i, boolean z) {
            ScreenLockType[] values;
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            int passwordQuality = this.mDPM.getPasswordQuality(null, this.mUserId);
            RestrictedLockUtils.EnforcedAdmin checkIfPasswordQualityIsSet = RestrictedLockUtils.checkIfPasswordQualityIsSet(getActivity(), this.mUserId);
            for (ScreenLockType screenLockType : ScreenLockType.values()) {
                Preference findPreference = findPreference(screenLockType.preferenceKey);
                if (findPreference instanceof RestrictedPreference) {
                    boolean isScreenLockVisible = this.mController.isScreenLockVisible(screenLockType);
                    boolean isScreenLockEnabled = this.mController.isScreenLockEnabled(screenLockType, i);
                    boolean isScreenLockDisabledByAdmin = this.mController.isScreenLockDisabledByAdmin(screenLockType, passwordQuality);
                    if (z) {
                        isScreenLockVisible = isScreenLockVisible && isScreenLockEnabled;
                    }
                    if (!isScreenLockVisible) {
                        preferenceScreen.removePreference(findPreference);
                    } else if (isScreenLockDisabledByAdmin && checkIfPasswordQualityIsSet != null) {
                        ((RestrictedPreference) findPreference).setDisabledByAdmin(checkIfPasswordQualityIsSet);
                    } else if (!isScreenLockEnabled) {
                        ((RestrictedPreference) findPreference).setDisabledByAdmin(null);
                        findPreference.setSummary(R.string.unlock_set_unlock_disabled_summary);
                        findPreference.setEnabled(false);
                    } else {
                        ((RestrictedPreference) findPreference).setDisabledByAdmin(null);
                    }
                }
            }
        }

        private void updatePreferenceSummaryIfNeeded() {
            if (!StorageManager.isBlockEncrypted() || StorageManager.isNonDefaultBlockEncrypted() || AccessibilityManager.getInstance(getActivity()).getEnabledAccessibilityServiceList(-1).isEmpty()) {
                return;
            }
            setPreferenceSummary(ScreenLockType.PATTERN, R.string.secure_lock_encryption_warning);
            setPreferenceSummary(ScreenLockType.PIN, R.string.secure_lock_encryption_warning);
            setPreferenceSummary(ScreenLockType.PASSWORD, R.string.secure_lock_encryption_warning);
            setPreferenceSummary(ScreenLockType.MANAGED, R.string.secure_lock_encryption_warning);
        }

        protected Intent getLockManagedPasswordIntent(String str) {
            return this.mManagedPasswordProvider.createIntent(false, str);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public Intent getLockPasswordIntent(int i, int i2, int i3) {
            ChooseLockPassword.IntentBuilder userId = new ChooseLockPassword.IntentBuilder(getContext()).setPasswordQuality(i).setPasswordLengthRange(i2, i3).setForFingerprint(this.mForFingerprint).setUserId(this.mUserId);
            if (this.mHasChallenge) {
                userId.setChallenge(this.mChallenge);
            }
            if (this.mUserPassword != null) {
                userId.setPassword(this.mUserPassword);
            }
            return userId.build();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public Intent getLockPatternIntent() {
            ChooseLockPattern.IntentBuilder userId = new ChooseLockPattern.IntentBuilder(getContext()).setForFingerprint(this.mForFingerprint).setUserId(this.mUserId);
            if (this.mHasChallenge) {
                userId.setChallenge(this.mChallenge);
            }
            if (this.mUserPassword != null) {
                userId.setPattern(this.mUserPassword);
            }
            return userId.build();
        }

        protected Intent getEncryptionInterstitialIntent(Context context, int i, boolean z, Intent intent) {
            return EncryptionInterstitial.createStartIntent(context, i, z, intent);
        }

        void updateUnlockMethodAndFinish(int i, boolean z, boolean z2) {
            int i2;
            if (!this.mPasswordConfirmed) {
                throw new IllegalStateException("Tried to update password without confirming it");
            }
            int upgradeQuality = this.mController.upgradeQuality(i);
            Intent intentForUnlockMethod = getIntentForUnlockMethod(upgradeQuality);
            if (intentForUnlockMethod != null) {
                if (getIntent().getBooleanExtra("show_options_button", false)) {
                    intentForUnlockMethod.putExtra("show_options_button", z2);
                }
                intentForUnlockMethod.putExtra("choose_lock_generic_extras", getIntent().getExtras());
                if (this.mIsSetNewPassword && this.mHasChallenge) {
                    i2 = 103;
                } else {
                    i2 = 102;
                }
                startActivityForResult(intentForUnlockMethod, i2);
            } else if (upgradeQuality == 0) {
                this.mChooseLockSettingsHelper.utils().clearLock(this.mUserPassword, this.mUserId);
                this.mChooseLockSettingsHelper.utils().setLockScreenDisabled(z, this.mUserId);
                getActivity().setResult(-1);
                removeAllFingerprintForUserAndFinish(this.mUserId);
            } else {
                removeAllFingerprintForUserAndFinish(this.mUserId);
            }
        }

        private Intent getIntentForUnlockMethod(int i) {
            Intent intent = null;
            if (i >= 524288) {
                intent = getLockManagedPasswordIntent(this.mUserPassword);
            } else if (i >= 131072) {
                int passwordMinimumLength = this.mDPM.getPasswordMinimumLength(null, this.mUserId);
                if (passwordMinimumLength < 4) {
                    passwordMinimumLength = 4;
                }
                intent = getLockPasswordIntent(i, passwordMinimumLength, this.mDPM.getPasswordMaximumLength(i));
            } else if (i == 65536) {
                intent = getLockPatternIntent();
            }
            if (intent != null) {
                intent.putExtra(":settings:hide_drawer", this.mHideDrawer);
            }
            return intent;
        }

        private void removeAllFingerprintForUserAndFinish(final int i) {
            if (this.mFingerprintManager != null && this.mFingerprintManager.isHardwareDetected()) {
                if (this.mFingerprintManager.hasEnrolledFingerprints(i)) {
                    this.mFingerprintManager.setActiveUser(i);
                    this.mFingerprintManager.remove(new Fingerprint((CharSequence) null, i, 0, 0L), i, new FingerprintManager.RemovalCallback() { // from class: com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment.1
                        public void onRemovalError(Fingerprint fingerprint, int i2, CharSequence charSequence) {
                            Log.e("ChooseLockGenericFragment", String.format("Can't remove fingerprint %d in group %d. Reason: %s", Integer.valueOf(fingerprint.getFingerId()), Integer.valueOf(fingerprint.getGroupId()), charSequence));
                        }

                        public void onRemovalSucceeded(Fingerprint fingerprint, int i2) {
                            if (i2 == 0) {
                                ChooseLockGenericFragment.this.removeManagedProfileFingerprintsAndFinishIfNecessary(i);
                            }
                        }
                    });
                    return;
                }
                removeManagedProfileFingerprintsAndFinishIfNecessary(i);
                return;
            }
            finish();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void removeManagedProfileFingerprintsAndFinishIfNecessary(int i) {
            if (this.mFingerprintManager != null && this.mFingerprintManager.isHardwareDetected()) {
                this.mFingerprintManager.setActiveUser(UserHandle.myUserId());
            }
            boolean z = false;
            if (!this.mUserManager.getUserInfo(i).isManagedProfile()) {
                List profiles = this.mUserManager.getProfiles(i);
                int size = profiles.size();
                int i2 = 0;
                while (true) {
                    if (i2 >= size) {
                        break;
                    }
                    UserInfo userInfo = (UserInfo) profiles.get(i2);
                    if (!userInfo.isManagedProfile() || this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userInfo.id)) {
                        i2++;
                    } else {
                        removeAllFingerprintForUserAndFinish(userInfo.id);
                        z = true;
                        break;
                    }
                }
            }
            if (!z) {
                finish();
            }
        }

        @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
        public void onDestroy() {
            super.onDestroy();
        }

        @Override // com.android.settings.support.actionbar.HelpResourceProvider
        public int getHelpResource() {
            return R.string.help_url_choose_lockscreen;
        }

        private int getResIdForFactoryResetProtectionWarningTitle() {
            return UserManager.get(getActivity()).isManagedProfile(this.mUserId) ? R.string.unlock_disable_frp_warning_title_profile : R.string.unlock_disable_frp_warning_title;
        }

        private int getResIdForFactoryResetProtectionWarningMessage() {
            boolean z;
            if (this.mFingerprintManager != null && this.mFingerprintManager.isHardwareDetected()) {
                z = this.mFingerprintManager.hasEnrolledFingerprints(this.mUserId);
            } else {
                z = false;
            }
            boolean isManagedProfile = UserManager.get(getActivity()).isManagedProfile(this.mUserId);
            int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId);
            if (keyguardStoredPasswordQuality == 65536) {
                if (z && isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_pattern_fingerprint_profile;
                }
                if (z && !isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_pattern_fingerprint;
                }
                if (isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_pattern_profile;
                }
                return R.string.unlock_disable_frp_warning_content_pattern;
            } else if (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) {
                if (z && isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_pin_fingerprint_profile;
                }
                if (z && !isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_pin_fingerprint;
                }
                if (isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_pin_profile;
                }
                return R.string.unlock_disable_frp_warning_content_pin;
            } else if (keyguardStoredPasswordQuality == 262144 || keyguardStoredPasswordQuality == 327680 || keyguardStoredPasswordQuality == 393216 || keyguardStoredPasswordQuality == 524288) {
                if (z && isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_password_fingerprint_profile;
                }
                if (z && !isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_password_fingerprint;
                }
                if (isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_password_profile;
                }
                return R.string.unlock_disable_frp_warning_content_password;
            } else if (z && isManagedProfile) {
                return R.string.unlock_disable_frp_warning_content_unknown_fingerprint_profile;
            } else {
                if (z && !isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_unknown_fingerprint;
                }
                if (isManagedProfile) {
                    return R.string.unlock_disable_frp_warning_content_unknown_profile;
                }
                return R.string.unlock_disable_frp_warning_content_unknown;
            }
        }

        private boolean isUnlockMethodSecure(String str) {
            return (ScreenLockType.SWIPE.preferenceKey.equals(str) || ScreenLockType.NONE.preferenceKey.equals(str)) ? false : true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean setUnlockMethod(String str) {
            EventLog.writeEvent(90200, str);
            ScreenLockType fromKey = ScreenLockType.fromKey(str);
            if (fromKey != null) {
                switch (fromKey) {
                    case NONE:
                    case SWIPE:
                        updateUnlockMethodAndFinish(fromKey.defaultQuality, fromKey == ScreenLockType.NONE, false);
                        return true;
                    case PATTERN:
                    case PIN:
                    case PASSWORD:
                    case MANAGED:
                        maybeEnableEncryption(fromKey.defaultQuality, false);
                        return true;
                }
            }
            Log.e("ChooseLockGenericFragment", "Encountered unknown unlock method to set: " + str);
            return false;
        }

        private void showFactoryResetProtectionWarningDialog(String str) {
            FactoryResetProtectionWarningDialog.newInstance(getResIdForFactoryResetProtectionWarningTitle(), getResIdForFactoryResetProtectionWarningMessage(), str).show(getChildFragmentManager(), "frp_warning_dialog");
        }

        /* loaded from: classes.dex */
        public static class FactoryResetProtectionWarningDialog extends InstrumentedDialogFragment {
            public static FactoryResetProtectionWarningDialog newInstance(int i, int i2, String str) {
                FactoryResetProtectionWarningDialog factoryResetProtectionWarningDialog = new FactoryResetProtectionWarningDialog();
                Bundle bundle = new Bundle();
                bundle.putInt("titleRes", i);
                bundle.putInt("messageRes", i2);
                bundle.putString("unlockMethodToSet", str);
                factoryResetProtectionWarningDialog.setArguments(bundle);
                return factoryResetProtectionWarningDialog;
            }

            @Override // android.app.DialogFragment
            public void show(FragmentManager fragmentManager, String str) {
                if (fragmentManager.findFragmentByTag(str) == null) {
                    super.show(fragmentManager, str);
                }
            }

            @Override // android.app.DialogFragment
            public Dialog onCreateDialog(Bundle bundle) {
                final Bundle arguments = getArguments();
                return new AlertDialog.Builder(getActivity()).setTitle(arguments.getInt("titleRes")).setMessage(arguments.getInt("messageRes")).setPositiveButton(R.string.unlock_disable_frp_warning_ok, new DialogInterface.OnClickListener() { // from class: com.android.settings.password.-$$Lambda$ChooseLockGeneric$ChooseLockGenericFragment$FactoryResetProtectionWarningDialog$Abdb-f1FnDmiVy0c3RZHU7n2B2k
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        ((ChooseLockGeneric.ChooseLockGenericFragment) ChooseLockGeneric.ChooseLockGenericFragment.FactoryResetProtectionWarningDialog.this.getParentFragment()).setUnlockMethod(arguments.getString("unlockMethodToSet"));
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.settings.password.-$$Lambda$ChooseLockGeneric$ChooseLockGenericFragment$FactoryResetProtectionWarningDialog$YUiXVX_8NlQHl0UI000UMbpVL0U
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        ChooseLockGeneric.ChooseLockGenericFragment.FactoryResetProtectionWarningDialog.this.dismiss();
                    }
                }).create();
            }

            @Override // com.android.settingslib.core.instrumentation.Instrumentable
            public int getMetricsCategory() {
                return 528;
            }
        }
    }
}
