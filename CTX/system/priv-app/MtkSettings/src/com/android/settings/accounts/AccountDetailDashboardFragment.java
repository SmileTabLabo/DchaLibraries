package com.android.settings.accounts;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.drawer.Tile;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class AccountDetailDashboardFragment extends DashboardFragment {
    Account mAccount;
    private String mAccountLabel;
    private AccountSyncPreferenceController mAccountSynController;
    String mAccountType;
    private RemoveAccountPreferenceController mRemoveAccountController;

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setPreferenceComparisonCallback(null);
        Bundle arguments = getArguments();
        Activity activity = getActivity();
        UserHandle secureTargetUser = Utils.getSecureTargetUser(activity.getActivityToken(), (UserManager) getSystemService("user"), arguments, activity.getIntent().getExtras());
        if (arguments != null) {
            if (arguments.containsKey("account")) {
                this.mAccount = (Account) arguments.getParcelable("account");
            }
            if (arguments.containsKey("account_label")) {
                this.mAccountLabel = arguments.getString("account_label");
            }
            if (arguments.containsKey("account_type")) {
                this.mAccountType = arguments.getString("account_type");
            }
        }
        this.mAccountSynController.init(this.mAccount, secureTargetUser);
        this.mRemoveAccountController.init(this.mAccount, secureTargetUser);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        if (this.mAccountLabel != null) {
            getActivity().setTitle(this.mAccountLabel);
        }
        updateUi();
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 8;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "AccountDetailDashboard";
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_account_detail;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.account_type_settings;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        ArrayList arrayList = new ArrayList();
        this.mAccountSynController = new AccountSyncPreferenceController(context);
        arrayList.add(this.mAccountSynController);
        this.mRemoveAccountController = new RemoveAccountPreferenceController(context, this);
        arrayList.add(this.mRemoveAccountController);
        arrayList.add(new AccountHeaderPreferenceController(context, getLifecycle(), getActivity(), this, getArguments()));
        return arrayList;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected boolean displayTile(Tile tile) {
        Bundle bundle;
        if (this.mAccountType == null || (bundle = tile.metaData) == null) {
            return false;
        }
        boolean equals = this.mAccountType.equals(bundle.getString("com.android.settings.ia.account"));
        if (equals && tile.intent != null) {
            tile.intent.putExtra("extra.accountName", this.mAccount.name);
        }
        return equals;
    }

    void updateUi() {
        UserHandle userHandle;
        Context context = getContext();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("user_handle")) {
            userHandle = (UserHandle) arguments.getParcelable("user_handle");
        } else {
            userHandle = null;
        }
        AccountTypePreferenceLoader accountTypePreferenceLoader = new AccountTypePreferenceLoader(this, new AuthenticatorHelper(context, userHandle, null), userHandle);
        PreferenceScreen addPreferencesForType = accountTypePreferenceLoader.addPreferencesForType(this.mAccountType, getPreferenceScreen());
        if (addPreferencesForType != null) {
            accountTypePreferenceLoader.updatePreferenceIntents(addPreferencesForType, this.mAccountType, this.mAccount);
        }
    }
}
