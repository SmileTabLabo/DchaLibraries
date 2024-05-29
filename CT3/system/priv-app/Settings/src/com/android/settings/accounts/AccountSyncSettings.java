package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.google.android.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/* loaded from: classes.dex */
public class AccountSyncSettings extends AccountPreferenceBase {
    private Account mAccount;
    private TextView mErrorInfoView;
    private ImageView mProviderIcon;
    private TextView mProviderId;
    private TextView mUserId;
    private ArrayList<SyncStateSwitchPreference> mSwitches = new ArrayList<>();
    private ArrayList<SyncAdapterType> mInvisibleAdapters = Lists.newArrayList();

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

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int id) {
        if (id == 100) {
            Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.really_remove_account_title).setMessage(R.string.really_remove_account_message).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setPositiveButton(R.string.remove_account_label, new DialogInterface.OnClickListener() { // from class: com.android.settings.accounts.AccountSyncSettings.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog2, int which) {
                    Activity activity = AccountSyncSettings.this.getActivity();
                    AccountManager.get(activity).removeAccountAsUser(AccountSyncSettings.this.mAccount, activity, new AccountManagerCallback<Bundle>() { // from class: com.android.settings.accounts.AccountSyncSettings.1.1
                        @Override // android.accounts.AccountManagerCallback
                        public void run(AccountManagerFuture<Bundle> future) {
                            if (!AccountSyncSettings.this.isResumed()) {
                                return;
                            }
                            boolean failed = true;
                            try {
                                if (future.getResult().getBoolean("booleanResult")) {
                                    failed = false;
                                }
                            } catch (AuthenticatorException e) {
                            } catch (OperationCanceledException e2) {
                            } catch (IOException e3) {
                            }
                            if (failed && AccountSyncSettings.this.getActivity() != null && !AccountSyncSettings.this.getActivity().isFinishing()) {
                                AccountSyncSettings.this.showDialog(101);
                            } else {
                                AccountSyncSettings.this.finish();
                            }
                        }
                    }, null, AccountSyncSettings.this.mUserHandle);
                }
            }).create();
            return dialog;
        } else if (id == 101) {
            Dialog dialog2 = new AlertDialog.Builder(getActivity()).setTitle(R.string.really_remove_account_title).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).setMessage(R.string.remove_account_failed).create();
            return dialog2;
        } else if (id == 102) {
            Dialog dialog3 = new AlertDialog.Builder(getActivity()).setTitle(R.string.cant_sync_dialog_title).setMessage(R.string.cant_sync_dialog_message).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
            return dialog3;
        } else {
            return null;
        }
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 9;
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.account_sync_settings);
        getPreferenceScreen().setOrderingAsAdded(false);
        setAccessibilityTitle();
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_sync_screen, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(R.id.prefs_container);
        Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
        View prefs = super.onCreateView(inflater, prefs_container, savedInstanceState);
        prefs_container.addView(prefs);
        initializeUi(view);
        return view;
    }

    protected void initializeUi(View rootView) {
        this.mErrorInfoView = (TextView) rootView.findViewById(R.id.sync_settings_error_info);
        this.mErrorInfoView.setVisibility(8);
        this.mUserId = (TextView) rootView.findViewById(R.id.user_id);
        this.mProviderId = (TextView) rootView.findViewById(R.id.provider_id);
        this.mProviderIcon = (ImageView) rootView.findViewById(R.id.provider_icon);
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.e("AccountSettings", "No arguments provided when starting intent. ACCOUNT_KEY needed.");
            finish();
            return;
        }
        this.mAccount = (Account) arguments.getParcelable("account");
        if (!accountExists(this.mAccount)) {
            Log.e("AccountSettings", "Account provided does not exist: " + this.mAccount);
            finish();
            return;
        }
        if (Log.isLoggable("AccountSettings", 2)) {
            Log.v("AccountSettings", "Got account: " + this.mAccount);
        }
        this.mUserId.setText(this.mAccount.name);
        this.mProviderId.setText(this.mAccount.type);
    }

    private void setAccessibilityTitle() {
        int i;
        UserManager um = (UserManager) getSystemService("user");
        UserInfo user = um.getUserInfo(this.mUserHandle.getIdentifier());
        boolean isManagedProfile = user != null ? user.isManagedProfile() : false;
        CharSequence currentTitle = getActivity().getTitle();
        if (isManagedProfile) {
            i = R.string.accessibility_work_account_title;
        } else {
            i = R.string.accessibility_personal_account_title;
        }
        String accessibilityTitle = getString(i, new Object[]{currentTitle});
        getActivity().setTitle(Utils.createAccessibleSequence(currentTitle, accessibilityTitle));
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        removePreference("dummy");
        this.mAuthenticatorHelper.listenToAccountUpdates();
        updateAuthDescriptions();
        onAccountsUpdate(Binder.getCallingUserHandle());
        super.onResume();
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    private void addSyncStateSwitch(Account account, String authority) {
        SyncStateSwitchPreference item = (SyncStateSwitchPreference) getCachedPreference(authority);
        if (item == null) {
            item = new SyncStateSwitchPreference(getPrefContext(), account, authority);
            getPreferenceScreen().addPreference(item);
        } else {
            item.setup(account, authority);
        }
        item.setPersistent(false);
        ProviderInfo providerInfo = getPackageManager().resolveContentProviderAsUser(authority, 0, this.mUserHandle.getIdentifier());
        if (providerInfo == null) {
            return;
        }
        CharSequence providerLabel = providerInfo.loadLabel(getPackageManager());
        if (TextUtils.isEmpty(providerLabel)) {
            Log.e("AccountSettings", "Provider needs a label for authority '" + authority + "'");
            return;
        }
        String title = getString(R.string.sync_item_title, new Object[]{providerLabel});
        item.setTitle(title);
        item.setKey(authority);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem syncNow = menu.add(0, 1, 0, getString(R.string.sync_menu_sync_now)).setIcon(R.drawable.ic_menu_refresh_holo_dark);
        MenuItem syncCancel = menu.add(0, 2, 0, getString(R.string.sync_menu_sync_cancel)).setIcon(17301560);
        if (!RestrictedLockUtils.hasBaseUserRestriction(getPrefContext(), "no_modify_accounts", this.mUserHandle.getIdentifier())) {
            MenuItem removeAccount = menu.add(0, 3, 0, getString(R.string.remove_account_label)).setIcon(R.drawable.ic_menu_delete);
            removeAccount.setShowAsAction(4);
            RestrictedLockUtils.EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getPrefContext(), "no_modify_accounts", this.mUserHandle.getIdentifier());
            if (admin == null) {
                admin = RestrictedLockUtils.checkIfAccountManagementDisabled(getPrefContext(), this.mAccount.type, this.mUserHandle.getIdentifier());
            }
            RestrictedLockUtils.setMenuItemAsDisabledByAdmin(getPrefContext(), removeAccount, admin);
        }
        syncNow.setShowAsAction(4);
        syncCancel.setShowAsAction(4);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override // android.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean syncActive = !ContentResolver.getCurrentSyncsAsUser(this.mUserHandle.getIdentifier()).isEmpty();
        menu.findItem(1).setVisible(syncActive ? false : true);
        menu.findItem(2).setVisible(syncActive);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                startSyncForEnabledProviders();
                return true;
            case 2:
                cancelSyncForEnabledProviders();
                return true;
            case 3:
                showDialog(100);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof SyncStateSwitchPreference) {
            SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) preference;
            String authority = syncPref.getAuthority();
            Account account = syncPref.getAccount();
            int userId = this.mUserHandle.getIdentifier();
            boolean syncAutomatically = ContentResolver.getSyncAutomaticallyAsUser(account, authority, userId);
            if (syncPref.isOneTimeSyncMode()) {
                requestOrCancelSync(account, authority, true);
            } else {
                boolean syncOn = syncPref.isChecked();
                if (syncOn != syncAutomatically) {
                    ContentResolver.setSyncAutomaticallyAsUser(account, authority, syncOn, userId);
                    if (!ContentResolver.getMasterSyncAutomaticallyAsUser(userId) || !syncOn) {
                        requestOrCancelSync(account, authority, syncOn);
                    }
                }
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void startSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(true);
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.invalidateOptionsMenu();
    }

    private void cancelSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(false);
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.invalidateOptionsMenu();
    }

    private void requestOrCancelSyncForEnabledProviders(boolean startSync) {
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof SyncStateSwitchPreference) {
                SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) pref;
                if (syncPref.isChecked()) {
                    requestOrCancelSync(syncPref.getAccount(), syncPref.getAuthority(), startSync);
                }
            }
        }
        if (this.mAccount == null) {
            return;
        }
        for (SyncAdapterType syncAdapter : this.mInvisibleAdapters) {
            requestOrCancelSync(this.mAccount, syncAdapter.authority, startSync);
        }
    }

    private void requestOrCancelSync(Account account, String authority, boolean flag) {
        if (flag) {
            Bundle extras = new Bundle();
            extras.putBoolean("force", true);
            ContentResolver.requestSyncAsUser(account, authority, this.mUserHandle.getIdentifier(), extras);
            return;
        }
        ContentResolver.cancelSyncAsUser(account, authority, this.mUserHandle.getIdentifier());
    }

    private boolean isSyncing(List<SyncInfo> currentSyncs, Account account, String authority) {
        for (SyncInfo syncInfo : currentSyncs) {
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase
    protected void onSyncStateUpdated() {
        if (isResumed()) {
            setFeedsState();
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.invalidateOptionsMenu();
        }
    }

    private void setFeedsState() {
        boolean lastSyncFailed;
        boolean z;
        boolean z2;
        Date date = new Date();
        int userId = this.mUserHandle.getIdentifier();
        List<SyncInfo> currentSyncs = ContentResolver.getCurrentSyncsAsUser(userId);
        boolean syncIsFailing = false;
        updateAccountSwitches();
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof SyncStateSwitchPreference) {
                SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) pref;
                String authority = syncPref.getAuthority();
                Account account = syncPref.getAccount();
                SyncStatusInfo status = ContentResolver.getSyncStatusAsUser(account, authority, userId);
                boolean syncEnabled = ContentResolver.getSyncAutomaticallyAsUser(account, authority, userId);
                boolean z3 = status == null ? false : status.pending;
                boolean z4 = status == null ? false : status.initialize;
                boolean activelySyncing = isSyncing(currentSyncs, account, authority);
                if (status == null || status.lastFailureTime == 0) {
                    lastSyncFailed = false;
                } else {
                    lastSyncFailed = status.getLastFailureMesgAsInt(0) != 1;
                }
                if (!syncEnabled) {
                    lastSyncFailed = false;
                }
                if (lastSyncFailed && !activelySyncing && !z3) {
                    syncIsFailing = true;
                }
                if (Log.isLoggable("AccountSettings", 2)) {
                    Log.d("AccountSettings", "Update sync status: " + account + " " + authority + " active = " + activelySyncing + " pend =" + z3);
                }
                long successEndTime = status == null ? 0L : status.lastSuccessTime;
                if (!syncEnabled) {
                    syncPref.setSummary(R.string.sync_disabled);
                } else if (activelySyncing) {
                    syncPref.setSummary(R.string.sync_in_progress);
                } else if (successEndTime != 0) {
                    date.setTime(successEndTime);
                    String timeString = formatSyncDate(date);
                    syncPref.setSummary(getResources().getString(R.string.last_synced, timeString));
                } else {
                    syncPref.setSummary("");
                }
                int syncState = ContentResolver.getIsSyncableAsUser(account, authority, userId);
                if (!activelySyncing || syncState < 0) {
                    z = false;
                } else {
                    z = !z4;
                }
                syncPref.setActive(z);
                if (!z3 || syncState < 0) {
                    z2 = false;
                } else {
                    z2 = !z4;
                }
                syncPref.setPending(z2);
                syncPref.setFailed(lastSyncFailed);
                boolean oneTimeSyncMode = !ContentResolver.getMasterSyncAutomaticallyAsUser(userId);
                syncPref.setOneTimeSyncMode(oneTimeSyncMode);
                if (oneTimeSyncMode) {
                    syncEnabled = true;
                }
                syncPref.setChecked(syncEnabled);
            }
        }
        this.mErrorInfoView.setVisibility(syncIsFailing ? 0 : 8);
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener
    public void onAccountsUpdate(UserHandle userHandle) {
        super.onAccountsUpdate(userHandle);
        if (!accountExists(this.mAccount)) {
            finish();
            return;
        }
        updateAccountSwitches();
        onSyncStateUpdated();
    }

    private boolean accountExists(Account account) {
        if (account == null) {
            return false;
        }
        Account[] accounts = AccountManager.get(getActivity()).getAccountsByTypeAsUser(account.type, this.mUserHandle);
        for (Account account2 : accounts) {
            if (account2.equals(account)) {
                return true;
            }
        }
        return false;
    }

    private void updateAccountSwitches() {
        this.mInvisibleAdapters.clear();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier());
        ArrayList<String> authorities = new ArrayList<>();
        for (SyncAdapterType sa : syncAdapters) {
            if (sa.accountType.equals(this.mAccount.type)) {
                if (sa.isUserVisible()) {
                    if (Log.isLoggable("AccountSettings", 2)) {
                        Log.d("AccountSettings", "updateAccountSwitches: added authority " + sa.authority + " to accountType " + sa.accountType);
                    }
                    authorities.add(sa.authority);
                } else {
                    this.mInvisibleAdapters.add(sa);
                }
            }
        }
        if (Log.isLoggable("AccountSettings", 2)) {
            Log.d("AccountSettings", "looking for sync adapters that match account " + this.mAccount);
        }
        cacheRemoveAllPrefs(getPreferenceScreen());
        int m = authorities.size();
        for (int j = 0; j < m; j++) {
            String authority = authorities.get(j);
            int syncState = ContentResolver.getIsSyncableAsUser(this.mAccount, authority, this.mUserHandle.getIdentifier());
            if (Log.isLoggable("AccountSettings", 2)) {
                Log.d("AccountSettings", "  found authority " + authority + " " + syncState);
            }
            if (syncState > 0) {
                addSyncStateSwitch(this.mAccount, authority);
            }
        }
        removeCachedPrefs(getPreferenceScreen());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accounts.AccountPreferenceBase
    public void onAuthDescriptionsUpdated() {
        super.onAuthDescriptionsUpdated();
        if (this.mAccount == null) {
            return;
        }
        this.mProviderIcon.setImageDrawable(getDrawableForType(this.mAccount.type));
        this.mProviderId.setText(getLabelForType(this.mAccount.type));
    }

    @Override // com.android.settings.SettingsPreferenceFragment
    protected int getHelpResource() {
        return R.string.help_url_accounts;
    }
}
