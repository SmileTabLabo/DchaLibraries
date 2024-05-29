package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.UserHandle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.location.LocationSettings;
import com.android.settings.utils.LocalClassLoaderContextThemeWrapper;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.core.instrumentation.Instrumentable;
/* loaded from: classes.dex */
public class AccountTypePreferenceLoader {
    private AuthenticatorHelper mAuthenticatorHelper;
    private PreferenceFragment mFragment;
    private UserHandle mUserHandle;

    public AccountTypePreferenceLoader(PreferenceFragment preferenceFragment, AuthenticatorHelper authenticatorHelper, UserHandle userHandle) {
        this.mFragment = preferenceFragment;
        this.mAuthenticatorHelper = authenticatorHelper;
        this.mUserHandle = userHandle;
    }

    public PreferenceScreen addPreferencesForType(String str, PreferenceScreen preferenceScreen) {
        AuthenticatorDescription authenticatorDescription;
        if (!this.mAuthenticatorHelper.containsAccountType(str)) {
            return null;
        }
        try {
            authenticatorDescription = this.mAuthenticatorHelper.getAccountTypeDescription(str);
            if (authenticatorDescription != null) {
                try {
                    if (authenticatorDescription.accountPreferencesId != 0) {
                        Context createPackageContextAsUser = this.mFragment.getActivity().createPackageContextAsUser(authenticatorDescription.packageName, 0, this.mUserHandle);
                        Resources.Theme newTheme = this.mFragment.getResources().newTheme();
                        newTheme.applyStyle(2131952095, true);
                        LocalClassLoaderContextThemeWrapper localClassLoaderContextThemeWrapper = new LocalClassLoaderContextThemeWrapper(getClass(), createPackageContextAsUser, 0);
                        localClassLoaderContextThemeWrapper.getTheme().setTo(newTheme);
                        return this.mFragment.getPreferenceManager().inflateFromResource(localClassLoaderContextThemeWrapper, authenticatorDescription.accountPreferencesId, preferenceScreen);
                    }
                    return null;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("AccountTypePrefLoader", "Couldn't load preferences.xml file from " + authenticatorDescription.packageName);
                    return null;
                } catch (Resources.NotFoundException e2) {
                    Log.w("AccountTypePrefLoader", "Couldn't load preferences.xml file from " + authenticatorDescription.packageName);
                    return null;
                }
            }
            return null;
        } catch (PackageManager.NameNotFoundException e3) {
            authenticatorDescription = null;
        } catch (Resources.NotFoundException e4) {
            authenticatorDescription = null;
        }
    }

    public void updatePreferenceIntents(PreferenceGroup preferenceGroup, final String str, Account account) {
        final PackageManager packageManager = this.mFragment.getActivity().getPackageManager();
        int i = 0;
        while (i < preferenceGroup.getPreferenceCount()) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                updatePreferenceIntents((PreferenceGroup) preference, str, account);
            }
            Intent intent = preference.getIntent();
            if (intent != null) {
                if (TextUtils.equals(intent.getAction(), "android.settings.LOCATION_SOURCE_SETTINGS")) {
                    preference.setOnPreferenceClickListener(new FragmentStarter(LocationSettings.class.getName(), R.string.location_settings_title));
                } else if (packageManager.resolveActivityAsUser(intent, 65536, this.mUserHandle.getIdentifier()) == null) {
                    preferenceGroup.removePreference(preference);
                } else {
                    intent.putExtra("account", account);
                    intent.setFlags(intent.getFlags() | 268435456);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.accounts.AccountTypePreferenceLoader.1
                        @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
                        public boolean onPreferenceClick(Preference preference2) {
                            Intent intent2 = preference2.getIntent();
                            if (AccountTypePreferenceLoader.this.isSafeIntent(packageManager, intent2, str)) {
                                AccountTypePreferenceLoader.this.mFragment.getActivity().startActivityAsUser(intent2, AccountTypePreferenceLoader.this.mUserHandle);
                                return true;
                            }
                            Log.e("AccountTypePrefLoader", "Refusing to launch authenticator intent becauseit exploits Settings permissions: " + intent2);
                            return true;
                        }
                    });
                }
            }
            i++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSafeIntent(PackageManager packageManager, Intent intent, String str) {
        AuthenticatorDescription accountTypeDescription = this.mAuthenticatorHelper.getAccountTypeDescription(str);
        ResolveInfo resolveActivityAsUser = packageManager.resolveActivityAsUser(intent, 0, this.mUserHandle.getIdentifier());
        if (resolveActivityAsUser == null) {
            return false;
        }
        try {
            return resolveActivityAsUser.activityInfo.applicationInfo.uid == packageManager.getApplicationInfo(accountTypeDescription.packageName, 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AccountTypePrefLoader", "Intent considered unsafe due to exception.", e);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class FragmentStarter implements Preference.OnPreferenceClickListener {
        private final String mClass;
        private final int mTitleRes;

        public FragmentStarter(String str, int i) {
            this.mClass = str;
            this.mTitleRes = i;
        }

        @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
        public boolean onPreferenceClick(Preference preference) {
            int i;
            if (AccountTypePreferenceLoader.this.mFragment instanceof Instrumentable) {
                i = ((Instrumentable) AccountTypePreferenceLoader.this.mFragment).getMetricsCategory();
            } else {
                i = 0;
            }
            new SubSettingLauncher(preference.getContext()).setTitle(this.mTitleRes).setDestination(this.mClass).setSourceMetricsCategory(i).launch();
            if (this.mClass.equals(LocationSettings.class.getName())) {
                AccountTypePreferenceLoader.this.mFragment.getActivity().sendBroadcast(new Intent("com.android.settings.accounts.LAUNCHING_LOCATION_SETTINGS"), "android.permission.WRITE_SECURE_SETTINGS");
                return true;
            }
            return true;
        }
    }
}
