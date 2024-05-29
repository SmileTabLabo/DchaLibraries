package com.android.settings.fingerprint;

import android.content.Intent;
import android.content.res.Resources;
import android.os.UserHandle;
import android.widget.Button;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SetupChooseLockGeneric;
import com.android.settings.SetupWizardUtils;
import com.android.setupwizardlib.SetupWizardRecyclerLayout;
import com.android.setupwizardlib.items.Item;
import com.android.setupwizardlib.items.RecyclerItemAdapter;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public class SetupFingerprintEnrollIntroduction extends FingerprintEnrollIntroduction implements NavigationBar.NavigationBarListener {
    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction
    protected Intent getChooseLockIntent() {
        Intent intent = new Intent(this, SetupChooseLockGeneric.class);
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction
    protected Intent getFindSensorIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollFindSensor.class);
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
        SetupWizardRecyclerLayout layout = (SetupWizardRecyclerLayout) findViewById(R.id.setup_wizard_layout);
        RecyclerItemAdapter adapter = (RecyclerItemAdapter) layout.getAdapter();
        Item nextItem = (Item) adapter.findItemById(R.id.next_button);
        nextItem.setTitle(getText(R.string.security_settings_fingerprint_enroll_introduction_continue_setup));
        Item cancelItem = (Item) adapter.findItemById(R.id.cancel_button);
        cancelItem.setTitle(getText(R.string.security_settings_fingerprint_enroll_introduction_cancel_setup));
        SetupWizardUtils.setImmersiveMode(this);
        getNavigationBar().setNavigationBarListener(this);
        Button nextButton = getNavigationBar().getNextButton();
        nextButton.setText((CharSequence) null);
        nextButton.setEnabled(false);
        layout.setDividerInset(getResources().getDimensionPixelSize(R.dimen.suw_items_icon_divider_inset));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction, android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (data == null) {
                data = new Intent();
            }
            LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
            data.putExtra(":settings:password_quality", lockPatternUtils.getKeyguardStoredPasswordQuality(UserHandle.myUserId()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction
    protected void onCancelButtonClick() {
        SetupSkipDialog dialog = SetupSkipDialog.newInstance(getIntent().getBooleanExtra(":settings:frp_supported", false));
        dialog.show(getFragmentManager());
    }

    @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override // com.android.setupwizardlib.view.NavigationBar.NavigationBarListener
    public void onNavigateNext() {
    }

    @Override // com.android.settings.fingerprint.FingerprintEnrollIntroduction, com.android.settings.InstrumentedActivity
    protected int getMetricsCategory() {
        return 249;
    }
}
