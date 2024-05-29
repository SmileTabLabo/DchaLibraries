package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.AccountPreference;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.location.LocationSettings;
import com.android.settingslib.accounts.AuthenticatorHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
/* loaded from: classes.dex */
public class ManageAccountsSettings extends AccountPreferenceBase implements AuthenticatorHelper.OnAccountsUpdateListener {
    private String mAccountType;
    private String[] mAuthorities;
    private TextView mErrorInfoView;
    private Account mFirstAccount;

    @Override // com.android.settings.accounts.AccountPreferenceBase
    public /* bridge */ /* synthetic */ PreferenceScreen addPreferencesForType(String accountType, PreferenceScreen parent) {
        return super.addPreferencesForType(accountType, parent);
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase
    public /* bridge */ /* synthetic */ ArrayList getAuthoritiesForAccountType(String type) {
        return super.getAuthoritiesForAccountType(type);
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase
    public /* bridge */ /* synthetic */ void updateAuthDescriptions() {
        super.updateAuthDescriptions();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 11;
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle args = getArguments();
        if (args != null && args.containsKey("account_type")) {
            this.mAccountType = args.getString("account_type");
        }
        addPreferencesFromResource(R.xml.manage_accounts_settings);
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mAuthenticatorHelper.listenToAccountUpdates();
        updateAuthDescriptions();
        showAccountsIfNeeded();
        showSyncState();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_accounts_screen, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(R.id.prefs_container);
        Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
        View prefs = super.onCreateView(inflater, prefs_container, savedInstanceState);
        prefs_container.addView(prefs);
        return view;
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        View view = getView();
        this.mErrorInfoView = (TextView) view.findViewById(R.id.sync_settings_error_info);
        this.mErrorInfoView.setVisibility(8);
        this.mAuthorities = activity.getIntent().getStringArrayExtra("authorities");
        Bundle args = getArguments();
        if (args == null || !args.containsKey("account_label")) {
            return;
        }
        getActivity().setTitle(args.getString("account_label"));
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        super.onStop();
        Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(0, 16);
        activity.getActionBar().setCustomView((View) null);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof AccountPreference) {
            startAccountSettings((AccountPreference) preference);
            return true;
        }
        return false;
    }

    private void startAccountSettings(AccountPreference acctPref) {
        Bundle args = new Bundle();
        args.putParcelable("account", acctPref.getAccount());
        args.putParcelable("android.intent.extra.USER", this.mUserHandle);
        ((SettingsActivity) getActivity()).startPreferencePanel(AccountSyncSettings.class.getCanonicalName(), args, R.string.account_sync_settings_title, acctPref.getAccount().name, this, 1);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, getString(R.string.sync_menu_sync_now)).setIcon(R.drawable.ic_menu_refresh_holo_dark);
        menu.add(0, 2, 0, getString(R.string.sync_menu_sync_cancel)).setIcon(17301560);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override // android.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        super.onPrepareOptionsMenu(menu);
        boolean syncActive = !ContentResolver.getCurrentSyncsAsUser(this.mUserHandle.getIdentifier()).isEmpty();
        menu.findItem(1).setVisible((syncActive || this.mFirstAccount == null) ? false : true);
        MenuItem findItem = menu.findItem(2);
        if (syncActive && this.mFirstAccount != null) {
            z = true;
        }
        findItem.setVisible(z);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                requestOrCancelSyncForAccounts(true);
                return true;
            case 2:
                requestOrCancelSyncForAccounts(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void requestOrCancelSyncForAccounts(boolean sync) {
        int userId = this.mUserHandle.getIdentifier();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(userId);
        Bundle extras = new Bundle();
        extras.putBoolean("force", true);
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof AccountPreference) {
                Account account = ((AccountPreference) pref).getAccount();
                for (int j = 0; j < syncAdapters.length; j++) {
                    SyncAdapterType sa = syncAdapters[j];
                    if (syncAdapters[j].accountType.equals(this.mAccountType) && ContentResolver.getSyncAutomaticallyAsUser(account, sa.authority, userId)) {
                        if (sync) {
                            ContentResolver.requestSyncAsUser(account, sa.authority, userId, extras);
                        } else {
                            ContentResolver.cancelSyncAsUser(account, sa.authority, userId);
                        }
                    }
                }
            }
        }
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase
    protected void onSyncStateUpdated() {
        showSyncState();
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.invalidateOptionsMenu();
    }

    private void showSyncState() {
        boolean lastSyncFailed;
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        int userId = this.mUserHandle.getIdentifier();
        List<SyncInfo> currentSyncs = ContentResolver.getCurrentSyncsAsUser(userId);
        boolean anySyncFailed = false;
        Date date = new Date();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(userId);
        HashSet<String> userFacing = new HashSet<>();
        for (SyncAdapterType sa : syncAdapters) {
            if (sa.isUserVisible()) {
                userFacing.add(sa.authority);
            }
        }
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof AccountPreference) {
                AccountPreference accountPref = (AccountPreference) pref;
                Account account = accountPref.getAccount();
                int syncCount = 0;
                long lastSuccessTime = 0;
                boolean syncIsFailing = false;
                ArrayList<String> authorities = accountPref.getAuthorities();
                boolean syncingNow = false;
                if (authorities != null) {
                    for (String authority : authorities) {
                        SyncStatusInfo status = ContentResolver.getSyncStatusAsUser(account, authority, userId);
                        boolean syncEnabled = isSyncEnabled(userId, account, authority);
                        boolean authorityIsPending = ContentResolver.isSyncPending(account, authority);
                        boolean activelySyncing = isSyncing(currentSyncs, account, authority);
                        if (status == null || !syncEnabled || status.lastFailureTime == 0) {
                            lastSyncFailed = false;
                        } else {
                            lastSyncFailed = status.getLastFailureMesgAsInt(0) != 1;
                        }
                        if (lastSyncFailed && !activelySyncing && !authorityIsPending) {
                            syncIsFailing = true;
                            anySyncFailed = true;
                        }
                        syncingNow |= activelySyncing;
                        if (status != null && lastSuccessTime < status.lastSuccessTime) {
                            lastSuccessTime = status.lastSuccessTime;
                        }
                        syncCount += (syncEnabled && userFacing.contains(authority)) ? 1 : 0;
                    }
                } else if (Log.isLoggable("AccountSettings", 2)) {
                    Log.v("AccountSettings", "no syncadapters found for " + account);
                }
                if (syncIsFailing) {
                    accountPref.setSyncStatus(2, true);
                } else if (syncCount == 0) {
                    accountPref.setSyncStatus(1, true);
                } else if (syncCount > 0) {
                    if (syncingNow) {
                        accountPref.setSyncStatus(3, true);
                    } else {
                        accountPref.setSyncStatus(0, true);
                        if (lastSuccessTime > 0) {
                            accountPref.setSyncStatus(0, false);
                            date.setTime(lastSuccessTime);
                            String timeString = formatSyncDate(date);
                            accountPref.setSummary(getResources().getString(R.string.last_synced, timeString));
                        }
                    }
                } else {
                    accountPref.setSyncStatus(1, true);
                }
            }
        }
        this.mErrorInfoView.setVisibility(anySyncFailed ? 0 : 8);
    }

    private boolean isSyncing(List<SyncInfo> currentSyncs, Account account, String authority) {
        int count = currentSyncs.size();
        for (int i = 0; i < count; i++) {
            SyncInfo syncInfo = currentSyncs.get(i);
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSyncEnabled(int userId, Account account, String authority) {
        return ContentResolver.getSyncAutomaticallyAsUser(account, authority, userId) && ContentResolver.getMasterSyncAutomaticallyAsUser(userId) && ContentResolver.getIsSyncableAsUser(account, authority, userId) > 0;
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener
    public void onAccountsUpdate(UserHandle userHandle) {
        showAccountsIfNeeded();
        onSyncStateUpdated();
    }

    private void showAccountsIfNeeded() {
        if (getActivity() == null) {
            return;
        }
        Account[] accounts = AccountManager.get(getActivity()).getAccountsAsUser(this.mUserHandle.getIdentifier());
        getPreferenceScreen().removeAll();
        this.mFirstAccount = null;
        addPreferencesFromResource(R.xml.manage_accounts_settings);
        for (Account account : accounts) {
            if (this.mAccountType == null || account.type.equals(this.mAccountType)) {
                ArrayList<String> auths = getAuthoritiesForAccountType(account.type);
                boolean showAccount = true;
                if (this.mAuthorities != null && auths != null) {
                    showAccount = false;
                    String[] strArr = this.mAuthorities;
                    int length = strArr.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        String requestedAuthority = strArr[i];
                        if (!auths.contains(requestedAuthority)) {
                            i++;
                        } else {
                            showAccount = true;
                            break;
                        }
                    }
                }
                if (showAccount) {
                    Drawable icon = getDrawableForType(account.type);
                    AccountPreference preference = new AccountPreference(getPrefContext(), account, icon, auths, false);
                    getPreferenceScreen().addPreference(preference);
                    if (this.mFirstAccount == null) {
                        this.mFirstAccount = account;
                    }
                }
            }
        }
        if (this.mAccountType != null && this.mFirstAccount != null) {
            addAuthenticatorSettings();
        } else {
            finish();
        }
    }

    private void addAuthenticatorSettings() {
        PreferenceScreen prefs = addPreferencesForType(this.mAccountType, getPreferenceScreen());
        if (prefs == null) {
            return;
        }
        updatePreferenceIntents(prefs);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class FragmentStarter implements Preference.OnPreferenceClickListener {
        private final String mClass;
        private final int mTitleRes;

        public FragmentStarter(String className, int title) {
            this.mClass = className;
            this.mTitleRes = title;
        }

        @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
        public boolean onPreferenceClick(Preference preference) {
            ((SettingsActivity) ManageAccountsSettings.this.getActivity()).startPreferencePanel(this.mClass, null, this.mTitleRes, null, null, 0);
            if (this.mClass.equals(LocationSettings.class.getName())) {
                Intent intent = new Intent("com.android.settings.accounts.LAUNCHING_LOCATION_SETTINGS");
                ManageAccountsSettings.this.getActivity().sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
                return true;
            }
            return true;
        }
    }

    private void updatePreferenceIntents(PreferenceGroup prefs) {
        final PackageManager pm = getActivity().getPackageManager();
        int i = 0;
        while (i < prefs.getPreferenceCount()) {
            Preference pref = prefs.getPreference(i);
            if (pref instanceof PreferenceGroup) {
                updatePreferenceIntents((PreferenceGroup) pref);
            }
            Intent intent = pref.getIntent();
            if (intent != null) {
                if (intent.getAction().equals("android.settings.LOCATION_SOURCE_SETTINGS")) {
                    pref.setOnPreferenceClickListener(new FragmentStarter(LocationSettings.class.getName(), R.string.location_settings_title));
                } else {
                    ResolveInfo ri = pm.resolveActivityAsUser(intent, 65536, this.mUserHandle.getIdentifier());
                    if (ri == null) {
                        prefs.removePreference(pref);
                    } else {
                        intent.putExtra("account", this.mFirstAccount);
                        intent.setFlags(intent.getFlags() | 268435456);
                        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.accounts.ManageAccountsSettings.1
                            @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
                            public boolean onPreferenceClick(Preference preference) {
                                Intent prefIntent = preference.getIntent();
                                if (ManageAccountsSettings.this.isSafeIntent(pm, prefIntent)) {
                                    ManageAccountsSettings.this.getActivity().startActivityAsUser(prefIntent, ManageAccountsSettings.this.mUserHandle);
                                    return true;
                                }
                                Log.e("AccountSettings", "Refusing to launch authenticator intent because it exploits Settings permissions: " + prefIntent);
                                return true;
                            }
                        });
                    }
                }
            }
            i++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSafeIntent(PackageManager pm, Intent intent) {
        AuthenticatorDescription authDesc = this.mAuthenticatorHelper.getAccountTypeDescription(this.mAccountType);
        ResolveInfo resolveInfo = pm.resolveActivityAsUser(intent, 0, this.mUserHandle.getIdentifier());
        if (resolveInfo == null) {
            return false;
        }
        ActivityInfo resolvedActivityInfo = resolveInfo.activityInfo;
        ApplicationInfo resolvedAppInfo = resolvedActivityInfo.applicationInfo;
        try {
            if (resolvedActivityInfo.exported && (resolvedActivityInfo.permission == null || pm.checkPermission(resolvedActivityInfo.permission, authDesc.packageName) == 0)) {
                return true;
            }
            ApplicationInfo authenticatorAppInf = pm.getApplicationInfo(authDesc.packageName, 0);
            return resolvedAppInfo.uid == authenticatorAppInf.uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AccountSettings", "Intent considered unsafe due to exception.", e);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accounts.AccountPreferenceBase
    public void onAuthDescriptionsUpdated() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof AccountPreference) {
                AccountPreference accPref = (AccountPreference) pref;
                accPref.setSummary(getLabelForType(accPref.getAccount().type));
            }
        }
    }
}
