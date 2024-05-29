package com.android.settings.fingerprint;

import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public class SetupFingerprintEnrollFindSensor extends FingerprintEnrollFindSensor implements NavigationBar.NavigationBarListener {
    @Override // com.android.settings.fingerprint.FingerprintEnrollFindSensor
    protected int getContentView() {
        return R.layout.setup_fingerprint_enroll_find_sensor;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    public Intent getEnrollingIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollEnrolling.class);
        intent.putExtra("hw_auth_token", this.mToken);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    @Override // android.app.Activity, android.view.ContextThemeWrapper
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        int resid2 = SetupWizardUtils.getTheme(getIntent());
        super.onApplyThemeResource(theme, resid2, first);
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    protected void initViews() {
        SetupWizardUtils.setImmersiveMode(this);
        View nextButton = findViewById(R.id.next_button);
        if (nextButton != null) {
            nextButton.setVisibility(8);
        }
        getNavigationBar().setNavigationBarListener(this);
        getNavigationBar().getBackButton().setVisibility(8);
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollBase
    protected Button getNextButton() {
        return getNavigationBar().getNextButton();
    }

    @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
    public void onNavigateNext() {
        onNextButtonClick();
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollFindSensor, com.android.settings.InstrumentedActivity
    protected int getMetricsCategory() {
        return 247;
    }
}
