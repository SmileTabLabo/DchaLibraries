package com.android.settings.fingerprint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.fingerprint.FingerprintEnrollSidecar;
import com.android.settings.password.ChooseLockSettingsHelper;
/* loaded from: classes.dex */
public class FingerprintEnrollFindSensor extends FingerprintEnrollBase {
    private FingerprintFindSensorAnimation mAnimation;
    private boolean mLaunchedConfirmLock;
    private boolean mNextClicked;
    private FingerprintEnrollSidecar mSidecar;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, com.android.settings.core.InstrumentedActivity, com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(getContentView());
        ((Button) findViewById(R.id.skip_button)).setOnClickListener(this);
        setHeaderText(R.string.security_settings_fingerprint_enroll_find_sensor_title);
        if (bundle != null) {
            this.mLaunchedConfirmLock = bundle.getBoolean("launched_confirm_lock");
            this.mToken = bundle.getByteArray("hw_auth_token");
        }
        if (this.mToken == null && !this.mLaunchedConfirmLock) {
            launchConfirmLock();
        } else if (this.mToken != null) {
            startLookingForFingerprint();
        }
        View findViewById = findViewById(R.id.fingerprint_sensor_location_animation);
        if (findViewById instanceof FingerprintFindSensorAnimation) {
            this.mAnimation = (FingerprintFindSensorAnimation) findViewById;
        } else {
            this.mAnimation = null;
        }
    }

    protected int getContentView() {
        return R.layout.fingerprint_enroll_find_sensor;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onStart() {
        super.onStart();
        if (this.mAnimation != null) {
            this.mAnimation.startAnimation();
        }
    }

    private void startLookingForFingerprint() {
        this.mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag("sidecar");
        if (this.mSidecar == null) {
            this.mSidecar = new FingerprintEnrollSidecar();
            getFragmentManager().beginTransaction().add(this.mSidecar, "sidecar").commit();
        }
        this.mSidecar.setListener(new FingerprintEnrollSidecar.Listener() { // from class: com.android.settings.fingerprint.FingerprintEnrollFindSensor.1
            @Override // com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener
            public void onEnrollmentProgressChange(int i, int i2) {
                FingerprintEnrollFindSensor.this.mNextClicked = true;
                FingerprintEnrollFindSensor.this.proceedToEnrolling(true);
            }

            @Override // com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener
            public void onEnrollmentHelp(CharSequence charSequence) {
            }

            @Override // com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener
            public void onEnrollmentError(int i, CharSequence charSequence) {
                if (FingerprintEnrollFindSensor.this.mNextClicked && i == 5) {
                    FingerprintEnrollFindSensor.this.mNextClicked = false;
                    FingerprintEnrollFindSensor.this.proceedToEnrolling(false);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        if (this.mAnimation != null) {
            this.mAnimation.pauseAnimation();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        if (this.mAnimation != null) {
            this.mAnimation.stopAnimation();
        }
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("launched_confirm_lock", this.mLaunchedConfirmLock);
        bundle.putByteArray("hw_auth_token", this.mToken);
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() == R.id.skip_button) {
            onSkipButtonClick();
        } else {
            super.onClick(view);
        }
    }

    protected void onSkipButtonClick() {
        setResult(2);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void proceedToEnrolling(boolean z) {
        if (this.mSidecar != null) {
            if (z && this.mSidecar.cancelEnrollment()) {
                return;
            }
            getFragmentManager().beginTransaction().remove(this.mSidecar).commitAllowingStateLoss();
            this.mSidecar = null;
            startActivityForResult(getEnrollingIntent(), 2);
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1) {
            if (i2 == -1) {
                this.mToken = intent.getByteArrayExtra("hw_auth_token");
                overridePendingTransition(R.anim.suw_slide_next_in, R.anim.suw_slide_next_out);
                getIntent().putExtra("hw_auth_token", this.mToken);
                startLookingForFingerprint();
                return;
            }
            finish();
        } else if (i == 2) {
            if (i2 == 1) {
                setResult(1);
                finish();
            } else if (i2 == 2) {
                setResult(2);
                finish();
            } else if (i2 == 3) {
                setResult(3);
                finish();
            } else if (Utils.getFingerprintManagerOrNull(this).getEnrolledFingerprints().size() >= getResources().getInteger(17694789)) {
                finish();
            } else {
                startLookingForFingerprint();
            }
        } else {
            super.onActivityResult(i, i2, intent);
        }
    }

    private void launchConfirmLock() {
        boolean launchConfirmationActivity;
        long preEnroll = Utils.getFingerprintManagerOrNull(this).preEnroll();
        ChooseLockSettingsHelper chooseLockSettingsHelper = new ChooseLockSettingsHelper(this);
        if (this.mUserId == -10000) {
            launchConfirmationActivity = chooseLockSettingsHelper.launchConfirmationActivity(1, getString(R.string.security_settings_fingerprint_preference_title), null, null, preEnroll);
        } else {
            launchConfirmationActivity = chooseLockSettingsHelper.launchConfirmationActivity(1, getString(R.string.security_settings_fingerprint_preference_title), (CharSequence) null, (CharSequence) null, preEnroll, this.mUserId);
        }
        if (!launchConfirmationActivity) {
            finish();
        } else {
            this.mLaunchedConfirmLock = true;
        }
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 241;
    }
}
