package com.android.settings.fingerprint;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.fingerprint.FingerprintEnrollSidecar;
/* loaded from: classes.dex */
public class FingerprintEnrollFindSensor extends FingerprintEnrollBase {
    private FingerprintFindSensorAnimation mAnimation;
    private boolean mLaunchedConfirmLock;
    private boolean mNextClicked;
    private FingerprintEnrollSidecar mSidecar;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        setHeaderText(R.string.security_settings_fingerprint_enroll_find_sensor_title);
        if (savedInstanceState != null) {
            this.mLaunchedConfirmLock = savedInstanceState.getBoolean("launched_confirm_lock");
            this.mToken = savedInstanceState.getByteArray("hw_auth_token");
        }
        if (this.mToken == null && !this.mLaunchedConfirmLock) {
            launchConfirmLock();
        } else if (this.mToken != null) {
            startLookingForFingerprint();
        }
        this.mAnimation = (FingerprintFindSensorAnimation) findViewById(R.id.fingerprint_sensor_location_animation);
    }

    protected int getContentView() {
        return R.layout.fingerprint_enroll_find_sensor;
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        this.mAnimation.startAnimation();
    }

    private void startLookingForFingerprint() {
        this.mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag("sidecar");
        if (this.mSidecar == null) {
            this.mSidecar = new FingerprintEnrollSidecar();
            getFragmentManager().beginTransaction().add(this.mSidecar, "sidecar").commit();
        }
        this.mSidecar.setListener(new FingerprintEnrollSidecar.Listener() { // from class: com.android.settings.fingerprint.FingerprintEnrollFindSensor.1
            @Override // com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener
            public void onEnrollmentProgressChange(int steps, int remaining) {
                FingerprintEnrollFindSensor.this.mNextClicked = true;
                if (FingerprintEnrollFindSensor.this.mSidecar.cancelEnrollment()) {
                    return;
                }
                FingerprintEnrollFindSensor.this.proceedToEnrolling();
            }

            @Override // com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener
            public void onEnrollmentHelp(CharSequence helpString) {
            }

            @Override // com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener
            public void onEnrollmentError(int errMsgId, CharSequence errString) {
                if (!FingerprintEnrollFindSensor.this.mNextClicked || errMsgId != 5) {
                    return;
                }
                FingerprintEnrollFindSensor.this.mNextClicked = false;
                FingerprintEnrollFindSensor.this.proceedToEnrolling();
            }
        });
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        this.mAnimation.pauseAnimation();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mAnimation.stopAnimation();
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("launched_confirm_lock", this.mLaunchedConfirmLock);
        outState.putByteArray("hw_auth_token", this.mToken);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    public void onNextButtonClick() {
        this.mNextClicked = true;
        if (this.mSidecar != null && (this.mSidecar == null || this.mSidecar.cancelEnrollment())) {
            return;
        }
        proceedToEnrolling();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void proceedToEnrolling() {
        Log.e("FingerprintEnrollFindSensor", "proceedToEnrolling mSidecar " + this.mSidecar);
        if (this.mSidecar != null) {
            getFragmentManager().beginTransaction().remove(this.mSidecar).commit();
            this.mSidecar = null;
        }
        startActivityForResult(getEnrollingIntent(), 2);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                this.mToken = data.getByteArrayExtra("hw_auth_token");
                overridePendingTransition(R.anim.suw_slide_next_in, R.anim.suw_slide_next_out);
                getIntent().putExtra("hw_auth_token", this.mToken);
                startLookingForFingerprint();
                return;
            }
            finish();
        } else if (requestCode == 2) {
            if (resultCode == 1) {
                setResult(1);
                finish();
            } else if (resultCode == 2) {
                setResult(2);
                finish();
            } else if (resultCode == 3) {
                setResult(3);
                finish();
            } else {
                FingerprintManager fpm = (FingerprintManager) getSystemService(FingerprintManager.class);
                int enrolled = fpm.getEnrolledFingerprints().size();
                int max = getResources().getInteger(17694873);
                if (enrolled >= max) {
                    finish();
                } else {
                    startLookingForFingerprint();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void launchConfirmLock() {
        boolean launchedConfirmationActivity;
        long challenge = ((FingerprintManager) getSystemService(FingerprintManager.class)).preEnroll();
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
        if (this.mUserId == -10000) {
            launchedConfirmationActivity = helper.launchConfirmationActivity(1, getString(R.string.security_settings_fingerprint_preference_title), null, null, challenge);
        } else {
            launchedConfirmationActivity = helper.launchConfirmationActivity(1, getString(R.string.security_settings_fingerprint_preference_title), (CharSequence) null, (CharSequence) null, challenge, this.mUserId);
        }
        if (!launchedConfirmationActivity) {
            finish();
        } else {
            this.mLaunchedConfirmLock = true;
        }
    }

    @Override // com.android.settings.InstrumentedActivity
    protected int getMetricsCategory() {
        return 241;
    }
}
