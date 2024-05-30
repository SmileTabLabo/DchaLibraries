package com.android.settings.fingerprint;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.settings.core.InstrumentedActivity;
import com.android.setupwizardlib.GlifLayout;
/* loaded from: classes.dex */
public abstract class FingerprintEnrollBase extends InstrumentedActivity implements View.OnClickListener {
    protected byte[] mToken;
    protected int mUserId;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.core.InstrumentedActivity, com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mToken = getIntent().getByteArrayExtra("hw_auth_token");
        if (bundle != null && this.mToken == null) {
            this.mToken = bundle.getByteArray("hw_auth_token");
        }
        this.mUserId = getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
    }

    @Override // android.app.Activity, android.view.ContextThemeWrapper
    protected void onApplyThemeResource(Resources.Theme theme, int i, boolean z) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putByteArray("hw_auth_token", this.mToken);
    }

    @Override // android.app.Activity
    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        initViews();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void initViews() {
        getWindow().setStatusBarColor(0);
        Button nextButton = getNextButton();
        if (nextButton != null) {
            nextButton.setOnClickListener(this);
        }
    }

    protected GlifLayout getLayout() {
        return (GlifLayout) findViewById(R.id.setup_wizard_layout);
    }

    protected void setHeaderText(int i, boolean z) {
        TextView headerTextView = getLayout().getHeaderTextView();
        CharSequence text = headerTextView.getText();
        CharSequence text2 = getText(i);
        if (text != text2 || z) {
            if (!TextUtils.isEmpty(text)) {
                headerTextView.setAccessibilityLiveRegion(1);
            }
            getLayout().setHeaderText(text2);
            setTitle(text2);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setHeaderText(int i) {
        setHeaderText(i, false);
    }

    protected Button getNextButton() {
        return (Button) findViewById(R.id.next_button);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == getNextButton()) {
            onNextButtonClick();
        }
    }

    protected void onNextButtonClick() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Intent getEnrollingIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", FingerprintEnrollEnrolling.class.getName());
        intent.putExtra("hw_auth_token", this.mToken);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        return intent;
    }
}
