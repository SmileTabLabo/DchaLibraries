package com.android.settings.accounts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.utils.ThreadUtils;
import java.text.DateFormat;
import java.util.Date;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class AccountPreferenceBase extends SettingsPreferenceFragment implements AuthenticatorHelper.OnAccountsUpdateListener {
    protected static final boolean VERBOSE = Log.isLoggable("AccountPreferenceBase", 2);
    protected AccountTypePreferenceLoader mAccountTypePreferenceLoader;
    protected AuthenticatorHelper mAuthenticatorHelper;
    private DateFormat mDateFormat;
    private Object mStatusChangeListenerHandle;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() { // from class: com.android.settings.accounts.-$$Lambda$AccountPreferenceBase$duCjsGZhZVNysJ2Rj1t7N9PkFAY
        @Override // android.content.SyncStatusObserver
        public final void onStatusChanged(int i) {
            ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settings.accounts.-$$Lambda$AccountPreferenceBase$7XBpqCguERDVZFsa_jC8V8rk8o8
                @Override // java.lang.Runnable
                public final void run() {
                    AccountPreferenceBase.this.onSyncStateUpdated();
                }
            });
        }
    };
    private DateFormat mTimeFormat;
    private UserManager mUm;
    protected UserHandle mUserHandle;

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mUm = (UserManager) getSystemService("user");
        Activity activity = getActivity();
        this.mUserHandle = Utils.getSecureTargetUser(activity.getActivityToken(), this.mUm, getArguments(), activity.getIntent().getExtras());
        this.mAuthenticatorHelper = new AuthenticatorHelper(activity, this.mUserHandle, this);
        this.mAccountTypePreferenceLoader = new AccountTypePreferenceLoader(this, this.mAuthenticatorHelper, this.mUserHandle);
    }

    @Override // com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener
    public void onAccountsUpdate(UserHandle userHandle) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onAuthDescriptionsUpdated() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onSyncStateUpdated() {
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Activity activity = getActivity();
        this.mDateFormat = android.text.format.DateFormat.getDateFormat(activity);
        this.mTimeFormat = android.text.format.DateFormat.getTimeFormat(activity);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(13, this.mSyncStatusObserver);
        onSyncStateUpdated();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
    }

    public void updateAuthDescriptions() {
        this.mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        onAuthDescriptionsUpdated();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Drawable getDrawableForType(String str) {
        return this.mAuthenticatorHelper.getDrawableForType(getActivity(), str);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public CharSequence getLabelForType(String str) {
        return this.mAuthenticatorHelper.getLabelForType(getActivity(), str);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String formatSyncDate(Date date) {
        return this.mDateFormat.format(date) + " " + this.mTimeFormat.format(date);
    }
}
