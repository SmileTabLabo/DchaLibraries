package com.android.settings.fingerprint;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.setupwizardlib.span.LinkSpan;
/* loaded from: classes.dex */
public class FingerprintEnrollIntroduction extends FingerprintEnrollBase implements View.OnClickListener, LinkSpan.OnClickListener {
    private TextView mErrorText;
    private boolean mFingerprintUnlockDisabledByAdmin;
    private boolean mHasPassword;
    private UserManager mUserManager;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, com.android.settings.core.InstrumentedActivity, com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mFingerprintUnlockDisabledByAdmin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this, 32, this.mUserId) != null;
        setContentView(R.layout.fingerprint_enroll_introduction);
        if (this.mFingerprintUnlockDisabledByAdmin) {
            setHeaderText(R.string.security_settings_fingerprint_enroll_introduction_title_unlock_disabled);
        } else {
            setHeaderText(R.string.security_settings_fingerprint_enroll_introduction_title);
        }
        ((Button) findViewById(R.id.fingerprint_cancel_button)).setOnClickListener(this);
        this.mErrorText = (TextView) findViewById(R.id.error_text);
        this.mUserManager = UserManager.get(this);
        updatePasswordQuality();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    protected void onResume() {
        int i;
        super.onResume();
        FingerprintManager fingerprintManagerOrNull = Utils.getFingerprintManagerOrNull(this);
        if (fingerprintManagerOrNull != null) {
            if (fingerprintManagerOrNull.getEnrolledFingerprints(this.mUserId).size() >= getResources().getInteger(17694789)) {
                i = R.string.fingerprint_intro_error_max;
            } else {
                i = 0;
            }
        } else {
            i = R.string.fingerprint_intro_error_unknown;
        }
        if (i == 0) {
            this.mErrorText.setText((CharSequence) null);
            getNextButton().setVisibility(0);
            return;
        }
        this.mErrorText.setText(i);
        getNextButton().setVisibility(8);
    }

    private void updatePasswordQuality() {
        this.mHasPassword = new ChooseLockSettingsHelper(this).utils().getActivePasswordQuality(this.mUserManager.getCredentialOwnerProfile(this.mUserId)) != 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    public Button getNextButton() {
        return (Button) findViewById(R.id.fingerprint_next_button);
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    protected void onNextButtonClick() {
        if (!this.mHasPassword) {
            launchChooseLock();
        } else {
            launchFindSensor(null);
        }
    }

    private void launchChooseLock() {
        Intent chooseLockIntent = getChooseLockIntent();
        long preEnroll = Utils.getFingerprintManagerOrNull(this).preEnroll();
        chooseLockIntent.putExtra("minimum_quality", 65536);
        chooseLockIntent.putExtra("hide_disabled_prefs", true);
        chooseLockIntent.putExtra("has_challenge", true);
        chooseLockIntent.putExtra("challenge", preEnroll);
        chooseLockIntent.putExtra("for_fingerprint", true);
        if (this.mUserId != -10000) {
            chooseLockIntent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        startActivityForResult(chooseLockIntent, 1);
    }

    private void launchFindSensor(byte[] bArr) {
        Intent findSensorIntent = getFindSensorIntent();
        if (bArr != null) {
            findSensorIntent.putExtra("hw_auth_token", bArr);
        }
        if (this.mUserId != -10000) {
            findSensorIntent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        startActivityForResult(findSensorIntent, 2);
    }

    protected Intent getChooseLockIntent() {
        return new Intent(this, ChooseLockGeneric.class);
    }

    protected Intent getFindSensorIntent() {
        return new Intent(this, FingerprintEnrollFindSensor.class);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        boolean z;
        if (i2 != 1) {
            z = false;
        } else {
            z = true;
        }
        if (i == 2) {
            if (z || i2 == 2) {
                setResult(z ? -1 : 2, intent);
                finish();
                return;
            }
        } else if (i == 1) {
            if (z) {
                updatePasswordQuality();
                launchFindSensor(intent.getByteArrayExtra("hw_auth_token"));
                return;
            }
        } else if (i == 3) {
            overridePendingTransition(R.anim.suw_slide_back_in, R.anim.suw_slide_back_out);
        }
        super.onActivityResult(i, i2, intent);
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() == R.id.fingerprint_cancel_button) {
            onCancelButtonClick();
        } else {
            super.onClick(view);
        }
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 243;
    }

    protected void onCancelButtonClick() {
        finish();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    public void initViews() {
        super.initViews();
        TextView textView = (TextView) findViewById(R.id.description_text);
        if (this.mFingerprintUnlockDisabledByAdmin) {
            textView.setText(R.string.security_settings_fingerprint_enroll_introduction_message_unlock_disabled);
        }
    }

    @Override // com.android.setupwizardlib.span.LinkSpan.OnClickListener
    public void onClick(LinkSpan linkSpan) {
        if ("url".equals(linkSpan.getId())) {
            Intent helpIntent = HelpUtils.getHelpIntent(this, getString(R.string.help_url_fingerprint), getClass().getName());
            if (helpIntent == null) {
                Log.w("FingerprintIntro", "Null help intent.");
                return;
            }
            try {
                startActivityForResult(helpIntent, 3);
            } catch (ActivityNotFoundException e) {
                Log.w("FingerprintIntro", "Activity was not found for intent, " + e);
            }
        }
    }
}
