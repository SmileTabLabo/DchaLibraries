package com.android.settings.accounts;

import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.accounts.AuthenticatorHelper;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
/* loaded from: classes.dex */
abstract class AccountPreferenceBase extends SettingsPreferenceFragment implements AuthenticatorHelper.OnAccountsUpdateListener {
    protected AuthenticatorHelper mAuthenticatorHelper;
    private DateFormat mDateFormat;
    private Object mStatusChangeListenerHandle;
    private DateFormat mTimeFormat;
    private UserManager mUm;
    protected UserHandle mUserHandle;
    private final Handler mHandler = new Handler();
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() { // from class: com.android.settings.accounts.AccountPreferenceBase.1
        @Override // android.content.SyncStatusObserver
        public void onStatusChanged(int which) {
            AccountPreferenceBase.this.mHandler.post(new Runnable() { // from class: com.android.settings.accounts.AccountPreferenceBase.1.1
                @Override // java.lang.Runnable
                public void run() {
                    AccountPreferenceBase.this.onSyncStateUpdated();
                }
            });
        }
    };

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUm = (UserManager) getSystemService("user");
        Activity activity = getActivity();
        this.mUserHandle = Utils.getSecureTargetUser(activity.getActivityToken(), this.mUm, getArguments(), activity.getIntent().getExtras());
        this.mAuthenticatorHelper = new AuthenticatorHelper(activity, this.mUserHandle, this);
    }

    @Override // com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener
    public void onAccountsUpdate(UserHandle userHandle) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onAuthDescriptionsUpdated() {
    }

    protected void onSyncStateUpdated() {
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        this.mDateFormat = android.text.format.DateFormat.getDateFormat(activity);
        this.mTimeFormat = android.text.format.DateFormat.getTimeFormat(activity);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(13, this.mSyncStatusObserver);
        onSyncStateUpdated();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        return this.mAuthenticatorHelper.getAuthoritiesForAccountType(type);
    }

    public PreferenceScreen addPreferencesForType(String accountType, PreferenceScreen parent) {
        if (!this.mAuthenticatorHelper.containsAccountType(accountType)) {
            return null;
        }
        AuthenticatorDescription desc = null;
        try {
            desc = this.mAuthenticatorHelper.getAccountTypeDescription(accountType);
            if (desc == null || desc.accountPreferencesId == 0) {
                return null;
            }
            Context targetCtx = getActivity().createPackageContextAsUser(desc.packageName, 0, this.mUserHandle);
            Resources.Theme baseTheme = getResources().newTheme();
            baseTheme.applyStyle(2131689951, true);
            Context themedCtx = new ContextThemeWrapper(targetCtx, 0);
            themedCtx.getTheme().setTo(baseTheme);
            PreferenceScreen prefs = getPreferenceManager().inflateFromResource(themedCtx, desc.accountPreferencesId, parent);
            return prefs;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("AccountSettings", "Couldn't load preferences.xml file from " + desc.packageName);
            return null;
        } catch (Resources.NotFoundException e2) {
            Log.w("AccountSettings", "Couldn't load preferences.xml file from " + desc.packageName);
            return null;
        }
    }

    public void updateAuthDescriptions() {
        this.mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        onAuthDescriptionsUpdated();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Drawable getDrawableForType(String accountType) {
        return this.mAuthenticatorHelper.getDrawableForType(getActivity(), accountType);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public CharSequence getLabelForType(String accountType) {
        return this.mAuthenticatorHelper.getLabelForType(getActivity(), accountType);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String formatSyncDate(Date date) {
        return this.mDateFormat.format(date) + " " + this.mTimeFormat.format(date);
    }
}
