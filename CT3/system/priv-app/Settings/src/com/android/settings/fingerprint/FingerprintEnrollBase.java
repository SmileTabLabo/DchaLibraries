package com.android.settings.fingerprint;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.InstrumentedActivity;
import com.android.settings.R;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public abstract class FingerprintEnrollBase extends InstrumentedActivity implements View.OnClickListener {
    protected byte[] mToken;
    protected int mUserId;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(2131689969);
        this.mToken = getIntent().getByteArrayExtra("hw_auth_token");
        if (savedInstanceState != null && this.mToken == null) {
            this.mToken = savedInstanceState.getByteArray("hw_auth_token");
        }
        this.mUserId = getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray("hw_auth_token", this.mToken);
    }

    @Override // android.app.Activity
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initViews();
    }

    protected void initViews() {
        getWindow().addFlags(-2147417856);
        getWindow().getDecorView().setSystemUiVisibility(1280);
        getWindow().setStatusBarColor(0);
        getNavigationBar().setVisibility(8);
        Button nextButton = getNextButton();
        if (nextButton == null) {
            return;
        }
        nextButton.setOnClickListener(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public NavigationBar getNavigationBar() {
        return (NavigationBar) findViewById(R.id.suw_layout_navigation_bar);
    }

    protected SetupWizardLayout getSetupWizardLayout() {
        return (SetupWizardLayout) findViewById(R.id.setup_wizard_layout);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setHeaderText(int resId, boolean force) {
        TextView layoutTitle = getSetupWizardLayout().getHeaderTextView();
        CharSequence previousTitle = layoutTitle.getText();
        CharSequence title = getText(resId);
        if (previousTitle == title && !force) {
            return;
        }
        if (!TextUtils.isEmpty(previousTitle)) {
            layoutTitle.setAccessibilityLiveRegion(1);
        }
        getSetupWizardLayout().setHeaderText(title);
        setTitle(title);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setHeaderText(int resId) {
        setHeaderText(resId, false);
    }

    protected Button getNextButton() {
        return (Button) findViewById(R.id.next_button);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v != getNextButton()) {
            return;
        }
        onNextButtonClick();
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
