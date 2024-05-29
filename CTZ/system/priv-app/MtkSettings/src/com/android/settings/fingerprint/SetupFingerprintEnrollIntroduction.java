package com.android.settings.fingerprint;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.settings.password.SetupChooseLockGeneric;
import com.android.settings.password.StorageManagerWrapper;
/* loaded from: classes.dex */
public class SetupFingerprintEnrollIntroduction extends FingerprintEnrollIntroduction {
    private boolean mAlreadyHadLockScreenSetup = false;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction, com.android.settings.fingerprint.FingerprintEnrollBase, com.android.settings.core.InstrumentedActivity, com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            this.mAlreadyHadLockScreenSetup = isKeyguardSecure();
        } else {
            this.mAlreadyHadLockScreenSetup = bundle.getBoolean("wasLockScreenPresent", false);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("wasLockScreenPresent", this.mAlreadyHadLockScreenSetup);
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction
    protected Intent getChooseLockIntent() {
        Intent intent = new Intent(this, SetupChooseLockGeneric.class);
        if (StorageManagerWrapper.isFileEncryptedNativeOrEmulated()) {
            intent.putExtra("lockscreen.password_type", 131072);
            intent.putExtra("show_options_button", true);
        }
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction
    protected Intent getFindSensorIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollFindSensor.class);
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction, com.android.settings.fingerprint.FingerprintEnrollBase
    public void initViews() {
        super.initViews();
        ((TextView) findViewById(R.id.description_text)).setText(R.string.security_settings_fingerprint_enroll_introduction_message_setup);
        getNextButton().setText(R.string.security_settings_fingerprint_enroll_introduction_continue_setup);
        ((Button) findViewById(R.id.fingerprint_cancel_button)).setText(R.string.security_settings_fingerprint_enroll_introduction_cancel_setup);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 2 && isKeyguardSecure() && !this.mAlreadyHadLockScreenSetup) {
            intent = getMetricIntent(intent);
        }
        super.onActivityResult(i, i2, intent);
    }

    private Intent getMetricIntent(Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.putExtra(":settings:password_quality", new LockPatternUtils(this).getKeyguardStoredPasswordQuality(UserHandle.myUserId()));
        return intent;
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction
    protected void onCancelButtonClick() {
        if (isKeyguardSecure()) {
            setResult(2, this.mAlreadyHadLockScreenSetup ? null : getMetricIntent(null));
            finish();
            return;
        }
        setResult(11);
        finish();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (!this.mAlreadyHadLockScreenSetup && isKeyguardSecure()) {
            setResult(0, getMetricIntent(null));
        }
        super.onBackPressed();
    }

    private boolean isKeyguardSecure() {
        return ((KeyguardManager) getSystemService(KeyguardManager.class)).isKeyguardSecure();
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction, com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 249;
    }
}
