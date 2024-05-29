package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
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
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class AccountSyncSettings extends AccountPreferenceBase {
    private Account mAccount;
    private TextView mErrorInfoView;
    private ArrayList<SyncAdapterType> mInvisibleAdapters = Lists.newArrayList();
    private ImageView mProviderIcon;
    private TextView mProviderId;
    private TextView mUserId;

    @Override // com.android.settings.accounts.AccountPreferenceBase
    public /* bridge */ /* synthetic */ void updateAuthDescriptions() {
        super.updateAuthDescriptions();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int i) {
        if (i != 102) {
            return null;
        }
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.cant_sync_dialog_title).setMessage(R.string.cant_sync_dialog_message).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 9;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public int getDialogMetricsCategory(int i) {
        if (i == 102) {
            return 587;
        }
        return 0;
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.account_sync_settings);
        getPreferenceScreen().setOrderingAsAdded(false);
        setAccessibilityTitle();
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.account_sync_screen, viewGroup, false);
        ViewGroup viewGroup2 = (ViewGroup) inflate.findViewById(R.id.prefs_container);
        Utils.prepareCustomPreferencesList(viewGroup, inflate, viewGroup2, false);
        viewGroup2.addView(super.onCreateView(layoutInflater, viewGroup2, bundle));
        initializeUi(inflate);
        return inflate;
    }

    protected void initializeUi(View view) {
        this.mErrorInfoView = (TextView) view.findViewById(R.id.sync_settings_error_info);
        this.mErrorInfoView.setVisibility(8);
        this.mUserId = (TextView) view.findViewById(R.id.user_id);
        this.mProviderId = (TextView) view.findViewById(R.id.provider_id);
        this.mProviderIcon = (ImageView) view.findViewById(R.id.provider_icon);
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.e("AccountPreferenceBase", "No arguments provided when starting intent. ACCOUNT_KEY needed.");
            finish();
            return;
        }
        this.mAccount = (Account) arguments.getParcelable("account");
        if (!accountExists(this.mAccount)) {
            Log.e("AccountPreferenceBase", "Account provided does not exist: " + this.mAccount);
            finish();
            return;
        }
        if (Log.isLoggable("AccountPreferenceBase", 2)) {
            Log.v("AccountPreferenceBase", "Got account: " + this.mAccount);
        }
        this.mUserId.setText(this.mAccount.name);
        this.mProviderId.setText(this.mAccount.type);
    }

    private void setAccessibilityTitle() {
        int i;
        UserInfo userInfo = ((UserManager) getSystemService("user")).getUserInfo(this.mUserHandle.getIdentifier());
        boolean isManagedProfile = userInfo != null ? userInfo.isManagedProfile() : false;
        CharSequence title = getActivity().getTitle();
        if (isManagedProfile) {
            i = R.string.accessibility_work_account_title;
        } else {
            i = R.string.accessibility_personal_account_title;
        }
        getActivity().setTitle(Utils.createAccessibleSequence(title, getString(i, new Object[]{title})));
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        removePreference("dummy");
        this.mAuthenticatorHelper.listenToAccountUpdates();
        updateAuthDescriptions();
        onAccountsUpdate(Binder.getCallingUserHandle());
        super.onResume();
    }

    @Override // com.android.settings.accounts.AccountPreferenceBase, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    private void addSyncStateSwitch(Account account, String str, String str2, int i) {
        SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) getCachedPreference(str);
        if (syncStateSwitchPreference == null) {
            syncStateSwitchPreference = new SyncStateSwitchPreference(getPrefContext(), account, str, str2, i);
            getPreferenceScreen().addPreference(syncStateSwitchPreference);
        } else {
            syncStateSwitchPreference.setup(account, str, str2, i);
        }
        PackageManager packageManager = getPackageManager();
        syncStateSwitchPreference.setPersistent(false);
        ProviderInfo resolveContentProviderAsUser = packageManager.resolveContentProviderAsUser(str, 0, this.mUserHandle.getIdentifier());
        if (resolveContentProviderAsUser == null) {
            return;
        }
        CharSequence loadLabel = resolveContentProviderAsUser.loadLabel(packageManager);
        if (TextUtils.isEmpty(loadLabel)) {
            Log.e("AccountPreferenceBase", "Provider needs a label for authority '" + str + "'");
            return;
        }
        syncStateSwitchPreference.setTitle(loadLabel);
        syncStateSwitchPreference.setKey(str);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        MenuItem icon = menu.add(0, 1, 0, getString(R.string.sync_menu_sync_now)).setIcon(R.drawable.ic_menu_refresh_holo_dark);
        MenuItem icon2 = menu.add(0, 2, 0, getString(R.string.sync_menu_sync_cancel)).setIcon(17301560);
        icon.setShowAsAction(4);
        icon2.setShowAsAction(4);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean z = !ContentResolver.getCurrentSyncsAsUser(this.mUserHandle.getIdentifier()).isEmpty();
        menu.findItem(1).setVisible(z ? false : true);
        menu.findItem(2).setVisible(z);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 1:
                startSyncForEnabledProviders();
                return true;
            case 2:
                cancelSyncForEnabledProviders();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override // android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1) {
            int preferenceCount = getPreferenceScreen().getPreferenceCount();
            for (int i3 = 0; i3 < preferenceCount; i3++) {
                Preference preference = getPreferenceScreen().getPreference(i3);
                if (preference instanceof SyncStateSwitchPreference) {
                    SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
                    if (syncStateSwitchPreference.getUid() == i) {
                        onPreferenceTreeClick(syncStateSwitchPreference);
                        return;
                    }
                }
            }
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (getActivity() == null) {
            return false;
        }
        if (preference instanceof SyncStateSwitchPreference) {
            SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
            String authority = syncStateSwitchPreference.getAuthority();
            Account account = syncStateSwitchPreference.getAccount();
            int identifier = this.mUserHandle.getIdentifier();
            String packageName = syncStateSwitchPreference.getPackageName();
            boolean syncAutomaticallyAsUser = ContentResolver.getSyncAutomaticallyAsUser(account, authority, identifier);
            if (syncStateSwitchPreference.isOneTimeSyncMode()) {
                if (requestAccountAccessIfNeeded(packageName)) {
                    return true;
                }
                requestOrCancelSync(account, authority, true);
            } else {
                boolean isChecked = syncStateSwitchPreference.isChecked();
                if (isChecked == syncAutomaticallyAsUser || (isChecked && requestAccountAccessIfNeeded(packageName))) {
                    return true;
                }
                ContentResolver.setSyncAutomaticallyAsUser(account, authority, isChecked, identifier);
                if (!ContentResolver.getMasterSyncAutomaticallyAsUser(identifier) || !isChecked) {
                    requestOrCancelSync(account, authority, isChecked);
                }
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private boolean requestAccountAccessIfNeeded(String str) {
        IntentSender createRequestAccountAccessIntentSenderAsUser;
        if (str == null) {
            return false;
        }
        try {
            int packageUidAsUser = getContext().getPackageManager().getPackageUidAsUser(str, this.mUserHandle.getIdentifier());
            AccountManager accountManager = (AccountManager) getContext().getSystemService(AccountManager.class);
            if (!accountManager.hasAccountAccess(this.mAccount, str, this.mUserHandle) && (createRequestAccountAccessIntentSenderAsUser = accountManager.createRequestAccountAccessIntentSenderAsUser(this.mAccount, str, this.mUserHandle)) != null) {
                try {
                    startIntentSenderForResult(createRequestAccountAccessIntentSenderAsUser, packageUidAsUser, null, 0, 0, 0, null);
                    return true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e("AccountPreferenceBase", "Error requesting account access", e);
                }
            }
            return false;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e("AccountPreferenceBase", "Invalid sync ", e2);
            return false;
        }
    }

    private void startSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(true);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void cancelSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(false);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void requestOrCancelSyncForEnabledProviders(boolean z) {
        int preferenceCount = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof SyncStateSwitchPreference) {
                SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
                if (syncStateSwitchPreference.isChecked()) {
                    requestOrCancelSync(syncStateSwitchPreference.getAccount(), syncStateSwitchPreference.getAuthority(), z);
                }
            }
        }
        if (this.mAccount != null) {
            Iterator<SyncAdapterType> it = this.mInvisibleAdapters.iterator();
            while (it.hasNext()) {
                requestOrCancelSync(this.mAccount, it.next().authority, z);
            }
        }
    }

    private void requestOrCancelSync(Account account, String str, boolean z) {
        if (z) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("force", true);
            ContentResolver.requestSyncAsUser(account, str, this.mUserHandle.getIdentifier(), bundle);
            return;
        }
        ContentResolver.cancelSyncAsUser(account, str, this.mUserHandle.getIdentifier());
    }

    private boolean isSyncing(List<SyncInfo> list, Account account, String str) {
        for (SyncInfo syncInfo : list) {
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(str)) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accounts.AccountPreferenceBase
    public void onSyncStateUpdated() {
        if (isResumed()) {
            setFeedsState();
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:24:0x0076  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x008a  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x00bb  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00bf  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x00c2  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x00c6  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00d0  */
    /* JADX WARN: Removed duplicated region for block: B:47:0x0109 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x0115 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:59:0x012d A[ADDED_TO_REGION] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void setFeedsState() {
        boolean z;
        boolean z2;
        int i;
        boolean z3;
        List<SyncInfo> list;
        int i2;
        long j;
        Date date;
        boolean z4;
        boolean z5;
        boolean z6;
        Date date2 = new Date();
        int identifier = this.mUserHandle.getIdentifier();
        List<SyncInfo> currentSyncsAsUser = ContentResolver.getCurrentSyncsAsUser(identifier);
        updateAccountSwitches();
        int preferenceCount = getPreferenceScreen().getPreferenceCount();
        int i3 = 0;
        boolean z7 = false;
        while (i3 < preferenceCount) {
            Preference preference = getPreferenceScreen().getPreference(i3);
            if (!(preference instanceof SyncStateSwitchPreference)) {
                date = date2;
                list = currentSyncsAsUser;
                i2 = preferenceCount;
                i = i3;
            } else {
                SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
                String authority = syncStateSwitchPreference.getAuthority();
                Account account = syncStateSwitchPreference.getAccount();
                SyncStatusInfo syncStatusAsUser = ContentResolver.getSyncStatusAsUser(account, authority, identifier);
                boolean syncAutomaticallyAsUser = ContentResolver.getSyncAutomaticallyAsUser(account, authority, identifier);
                if (syncStatusAsUser != null) {
                    z = syncStatusAsUser.pending;
                } else {
                    z = false;
                }
                if (syncStatusAsUser != null) {
                    z2 = syncStatusAsUser.initialize;
                } else {
                    z2 = false;
                }
                boolean isSyncing = isSyncing(currentSyncsAsUser, account, authority);
                if (syncStatusAsUser != null) {
                    i = i3;
                    if (syncStatusAsUser.lastFailureTime != 0 && syncStatusAsUser.getLastFailureMesgAsInt(0) != 1) {
                        z3 = true;
                        if (!syncAutomaticallyAsUser) {
                            z3 = false;
                        }
                        if (z3 && !isSyncing && !z) {
                            z7 = true;
                        }
                        list = currentSyncsAsUser;
                        if (!Log.isLoggable("AccountPreferenceBase", 3)) {
                            StringBuilder sb = new StringBuilder();
                            i2 = preferenceCount;
                            sb.append("Update sync status: ");
                            sb.append(account);
                            sb.append(" ");
                            sb.append(authority);
                            sb.append(" active = ");
                            sb.append(isSyncing);
                            sb.append(" pend =");
                            sb.append(z);
                            Log.d("AccountPreferenceBase", sb.toString());
                        } else {
                            i2 = preferenceCount;
                        }
                        if (syncStatusAsUser == null) {
                            j = syncStatusAsUser.lastSuccessTime;
                        } else {
                            j = 0;
                        }
                        if (syncAutomaticallyAsUser) {
                            syncStateSwitchPreference.setSummary(R.string.sync_disabled);
                        } else if (isSyncing) {
                            syncStateSwitchPreference.setSummary(R.string.sync_in_progress);
                        } else {
                            if (j != 0) {
                                date2.setTime(j);
                                date = date2;
                                z4 = false;
                                syncStateSwitchPreference.setSummary(getResources().getString(R.string.last_synced, formatSyncDate(date2)));
                            } else {
                                date = date2;
                                z4 = false;
                                syncStateSwitchPreference.setSummary("");
                            }
                            int isSyncableAsUser = ContentResolver.getIsSyncableAsUser(account, authority, identifier);
                            syncStateSwitchPreference.setActive((isSyncing || isSyncableAsUser < 0 || z2) ? z4 : true);
                            syncStateSwitchPreference.setPending((z || isSyncableAsUser < 0 || z2) ? z4 : true);
                            syncStateSwitchPreference.setFailed(z3);
                            z5 = true;
                            z6 = !ContentResolver.getMasterSyncAutomaticallyAsUser(identifier);
                            syncStateSwitchPreference.setOneTimeSyncMode(z6);
                            if (!z6 && !syncAutomaticallyAsUser) {
                                z5 = z4;
                            }
                            syncStateSwitchPreference.setChecked(z5);
                        }
                        date = date2;
                        z4 = false;
                        int isSyncableAsUser2 = ContentResolver.getIsSyncableAsUser(account, authority, identifier);
                        syncStateSwitchPreference.setActive((isSyncing || isSyncableAsUser2 < 0 || z2) ? z4 : true);
                        syncStateSwitchPreference.setPending((z || isSyncableAsUser2 < 0 || z2) ? z4 : true);
                        syncStateSwitchPreference.setFailed(z3);
                        z5 = true;
                        z6 = !ContentResolver.getMasterSyncAutomaticallyAsUser(identifier);
                        syncStateSwitchPreference.setOneTimeSyncMode(z6);
                        if (!z6) {
                            z5 = z4;
                        }
                        syncStateSwitchPreference.setChecked(z5);
                    }
                } else {
                    i = i3;
                }
                z3 = false;
                if (!syncAutomaticallyAsUser) {
                }
                if (z3) {
                    z7 = true;
                }
                list = currentSyncsAsUser;
                if (!Log.isLoggable("AccountPreferenceBase", 3)) {
                }
                if (syncStatusAsUser == null) {
                }
                if (syncAutomaticallyAsUser) {
                }
                date = date2;
                z4 = false;
                int isSyncableAsUser22 = ContentResolver.getIsSyncableAsUser(account, authority, identifier);
                syncStateSwitchPreference.setActive((isSyncing || isSyncableAsUser22 < 0 || z2) ? z4 : true);
                syncStateSwitchPreference.setPending((z || isSyncableAsUser22 < 0 || z2) ? z4 : true);
                syncStateSwitchPreference.setFailed(z3);
                z5 = true;
                z6 = !ContentResolver.getMasterSyncAutomaticallyAsUser(identifier);
                syncStateSwitchPreference.setOneTimeSyncMode(z6);
                if (!z6) {
                }
                syncStateSwitchPreference.setChecked(z5);
            }
            i3 = i + 1;
            currentSyncsAsUser = list;
            preferenceCount = i2;
            date2 = date;
        }
        this.mErrorInfoView.setVisibility(z7 ? 0 : 8);
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
        for (Account account2 : AccountManager.get(getActivity()).getAccountsByTypeAsUser(account.type, this.mUserHandle)) {
            if (account2.equals(account)) {
                return true;
            }
        }
        return false;
    }

    private void updateAccountSwitches() {
        this.mInvisibleAdapters.clear();
        SyncAdapterType[] syncAdapterTypesAsUser = ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier());
        ArrayList arrayList = new ArrayList();
        for (SyncAdapterType syncAdapterType : syncAdapterTypesAsUser) {
            if (syncAdapterType.accountType.equals(this.mAccount.type)) {
                if (syncAdapterType.isUserVisible()) {
                    if (Log.isLoggable("AccountPreferenceBase", 3)) {
                        Log.d("AccountPreferenceBase", "updateAccountSwitches: added authority " + syncAdapterType.authority + " to accountType " + syncAdapterType.accountType);
                    }
                    arrayList.add(syncAdapterType);
                } else {
                    this.mInvisibleAdapters.add(syncAdapterType);
                }
            }
        }
        if (Log.isLoggable("AccountPreferenceBase", 3)) {
            Log.d("AccountPreferenceBase", "looking for sync adapters that match account " + this.mAccount);
        }
        cacheRemoveAllPrefs(getPreferenceScreen());
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            SyncAdapterType syncAdapterType2 = (SyncAdapterType) arrayList.get(i);
            int isSyncableAsUser = ContentResolver.getIsSyncableAsUser(this.mAccount, syncAdapterType2.authority, this.mUserHandle.getIdentifier());
            if (Log.isLoggable("AccountPreferenceBase", 3)) {
                Log.d("AccountPreferenceBase", "  found authority " + syncAdapterType2.authority + " " + isSyncableAsUser);
            }
            if (isSyncableAsUser > 0) {
                try {
                    addSyncStateSwitch(this.mAccount, syncAdapterType2.authority, syncAdapterType2.getPackageName(), getContext().getPackageManager().getPackageUidAsUser(syncAdapterType2.getPackageName(), this.mUserHandle.getIdentifier()));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("AccountPreferenceBase", "No uid for package" + syncAdapterType2.getPackageName(), e);
                }
            }
        }
        removeCachedPrefs(getPreferenceScreen());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.accounts.AccountPreferenceBase
    public void onAuthDescriptionsUpdated() {
        super.onAuthDescriptionsUpdated();
        if (this.mAccount != null) {
            this.mProviderIcon.setImageDrawable(getDrawableForType(this.mAccount.type));
            this.mProviderId.setText(getLabelForType(this.mAccount.type));
        }
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_accounts;
    }
}
