package com.android.settings;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.UserManager;
import android.view.MenuItem;
/* loaded from: classes.dex */
public abstract class ConfirmDeviceCredentialBaseActivity extends SettingsActivity {
    private boolean mDark;
    private boolean mEnterAnimationPending;
    private boolean mFirstTimeVisible = true;
    private boolean mIsKeyguardLocked = false;
    private boolean mRestoring;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity, com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle savedState) {
        boolean z;
        int credentialOwnerUserId = Utils.getCredentialOwnerUserId(this, Utils.getUserIdFromBundle(this, getIntent().getExtras()));
        if (Utils.isManagedProfile(UserManager.get(this), credentialOwnerUserId)) {
            setTheme(2131689968);
        } else if (getIntent().getBooleanExtra("com.android.settings.ConfirmCredentials.darkTheme", false)) {
            setTheme(2131689967);
            this.mDark = true;
        }
        super.onCreate(savedState);
        getWindow().addFlags(8192);
        if (savedState == null) {
            z = ((KeyguardManager) getSystemService(KeyguardManager.class)).isKeyguardLocked();
        } else {
            z = savedState.getBoolean("STATE_IS_KEYGUARD_LOCKED", false);
        }
        this.mIsKeyguardLocked = z;
        if (this.mIsKeyguardLocked && getIntent().getBooleanExtra("com.android.settings.ConfirmCredentials.showWhenLocked", false)) {
            getWindow().addFlags(524288);
        }
        CharSequence msg = getIntent().getStringExtra("com.android.settings.ConfirmCredentials.title");
        setTitle(msg);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        this.mRestoring = savedState != null;
    }

    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("STATE_IS_KEYGUARD_LOCKED", this.mIsKeyguardLocked);
    }

    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        if (isChangingConfigurations() || this.mRestoring || !this.mDark || !this.mFirstTimeVisible) {
            return;
        }
        this.mFirstTimeVisible = false;
        prepareEnterAnimation();
        this.mEnterAnimationPending = true;
    }

    private ConfirmDeviceCredentialBaseFragment getFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment == null || !(fragment instanceof ConfirmDeviceCredentialBaseFragment)) {
            return null;
        }
        return (ConfirmDeviceCredentialBaseFragment) fragment;
    }

    @Override // android.app.Activity
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (!this.mEnterAnimationPending) {
            return;
        }
        startEnterAnimation();
        this.mEnterAnimationPending = false;
    }

    public void prepareEnterAnimation() {
        getFragment().prepareEnterAnimation();
    }

    public void startEnterAnimation() {
        getFragment().startEnterAnimation();
    }
}
