package com.android.settings.fingerprint;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
/* loaded from: classes.dex */
public class FingerprintEnrollFinish extends FingerprintEnrollBase {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_enroll_finish);
        setHeaderText(R.string.security_settings_fingerprint_enroll_finish_title);
        Button addButton = (Button) findViewById(R.id.add_another_button);
        FingerprintManager fpm = (FingerprintManager) getSystemService("fingerprint");
        int enrolled = fpm.getEnrolledFingerprints(this.mUserId).size();
        int max = getResources().getInteger(17694873);
        if (enrolled >= max) {
            addButton.setVisibility(4);
        } else {
            addButton.setOnClickListener(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    public void onNextButtonClick() {
        setResult(1);
        finish();
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase, android.view.View.OnClickListener
    public void onClick(View v) {
        if (v.getId() == R.id.add_another_button) {
            Intent intent = getEnrollingIntent();
            intent.addFlags(33554432);
            startActivity(intent);
            finish();
        }
        super.onClick(v);
    }

    @Override // com.android.settings.InstrumentedActivity
    protected int getMetricsCategory() {
        return 242;
    }
}
