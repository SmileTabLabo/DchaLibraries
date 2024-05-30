package com.android.settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.widget.TextView;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settingslib.RestrictedLockUtils;
@Deprecated
/* loaded from: classes.dex */
public abstract class RestrictedSettingsFragment extends SettingsPreferenceFragment {
    static final int REQUEST_PIN_CHALLENGE = 12309;
    AlertDialog mActionDisabledDialog;
    private boolean mChallengeRequested;
    private boolean mChallengeSucceeded;
    private TextView mEmptyTextView;
    private RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;
    private boolean mIsAdminUser;
    private final String mRestrictionKey;
    private RestrictionsManager mRestrictionsManager;
    private UserManager mUserManager;
    private boolean mOnlyAvailableForAdmins = false;
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() { // from class: com.android.settings.RestrictedSettingsFragment.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!RestrictedSettingsFragment.this.mChallengeRequested) {
                RestrictedSettingsFragment.this.mChallengeSucceeded = false;
                RestrictedSettingsFragment.this.mChallengeRequested = false;
            }
        }
    };

    public RestrictedSettingsFragment(String str) {
        this.mRestrictionKey = str;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mRestrictionsManager = (RestrictionsManager) getSystemService("restrictions");
        this.mUserManager = (UserManager) getSystemService("user");
        this.mIsAdminUser = this.mUserManager.isAdminUser();
        if (bundle != null) {
            this.mChallengeSucceeded = bundle.getBoolean("chsc", false);
            this.mChallengeRequested = bundle.getBoolean("chrq", false);
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        getActivity().registerReceiver(this.mScreenOffReceiver, intentFilter);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        this.mEmptyTextView = initEmptyTextView();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (getActivity().isChangingConfigurations()) {
            bundle.putBoolean("chrq", this.mChallengeRequested);
            bundle.putBoolean("chsc", this.mChallengeSucceeded);
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (shouldBeProviderProtected(this.mRestrictionKey)) {
            ensurePin();
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        getActivity().unregisterReceiver(this.mScreenOffReceiver);
        super.onDestroy();
    }

    @Override // android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == REQUEST_PIN_CHALLENGE) {
            if (i2 == -1) {
                this.mChallengeSucceeded = true;
                this.mChallengeRequested = false;
                if (this.mActionDisabledDialog != null && this.mActionDisabledDialog.isShowing()) {
                    this.mActionDisabledDialog.setOnDismissListener(null);
                    this.mActionDisabledDialog.dismiss();
                    return;
                }
                return;
            }
            this.mChallengeSucceeded = false;
            return;
        }
        super.onActivityResult(i, i2, intent);
    }

    private void ensurePin() {
        Intent createLocalApprovalIntent;
        if (!this.mChallengeSucceeded && !this.mChallengeRequested && this.mRestrictionsManager.hasRestrictionsProvider() && (createLocalApprovalIntent = this.mRestrictionsManager.createLocalApprovalIntent()) != null) {
            this.mChallengeRequested = true;
            this.mChallengeSucceeded = false;
            PersistableBundle persistableBundle = new PersistableBundle();
            persistableBundle.putString("android.request.mesg", getResources().getString(R.string.restr_pin_enter_admin_pin));
            createLocalApprovalIntent.putExtra("android.content.extra.REQUEST_BUNDLE", persistableBundle);
            startActivityForResult(createLocalApprovalIntent, REQUEST_PIN_CHALLENGE);
        }
    }

    protected boolean isRestrictedAndNotProviderProtected() {
        return (this.mRestrictionKey == null || "restrict_if_overridable".equals(this.mRestrictionKey) || !this.mUserManager.hasUserRestriction(this.mRestrictionKey) || this.mRestrictionsManager.hasRestrictionsProvider()) ? false : true;
    }

    protected boolean hasChallengeSucceeded() {
        return (this.mChallengeRequested && this.mChallengeSucceeded) || !this.mChallengeRequested;
    }

    protected boolean shouldBeProviderProtected(String str) {
        if (str == null) {
            return false;
        }
        return ("restrict_if_overridable".equals(str) || this.mUserManager.hasUserRestriction(this.mRestrictionKey)) && this.mRestrictionsManager.hasRestrictionsProvider();
    }

    protected TextView initEmptyTextView() {
        return (TextView) getActivity().findViewById(16908292);
    }

    public RestrictedLockUtils.EnforcedAdmin getRestrictionEnforcedAdmin() {
        this.mEnforcedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), this.mRestrictionKey, UserHandle.myUserId());
        if (this.mEnforcedAdmin != null && this.mEnforcedAdmin.userId == -10000) {
            this.mEnforcedAdmin.userId = UserHandle.myUserId();
        }
        return this.mEnforcedAdmin;
    }

    public TextView getEmptyTextView() {
        return this.mEmptyTextView;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsPreferenceFragment
    public void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        if (isUiRestrictedByOnlyAdmin() && (this.mActionDisabledDialog == null || !this.mActionDisabledDialog.isShowing())) {
            this.mActionDisabledDialog = new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder(this.mRestrictionKey, getRestrictionEnforcedAdmin()).setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.settings.-$$Lambda$RestrictedSettingsFragment$LUdTuWQX3d8kcdKiPapl2FlA0-c
                @Override // android.content.DialogInterface.OnDismissListener
                public final void onDismiss(DialogInterface dialogInterface) {
                    RestrictedSettingsFragment.this.getActivity().finish();
                }
            }).show();
            setEmptyView(new View(getContext()));
        } else if (this.mEmptyTextView != null) {
            setEmptyView(this.mEmptyTextView);
        }
        super.onDataSetChanged();
    }

    public void setIfOnlyAvailableForAdmins(boolean z) {
        this.mOnlyAvailableForAdmins = z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isUiRestricted() {
        return isRestrictedAndNotProviderProtected() || !hasChallengeSucceeded() || (!this.mIsAdminUser && this.mOnlyAvailableForAdmins);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isUiRestrictedByOnlyAdmin() {
        return isUiRestricted() && !this.mUserManager.hasBaseUserRestriction(this.mRestrictionKey, UserHandle.of(UserHandle.myUserId())) && (this.mIsAdminUser || !this.mOnlyAvailableForAdmins);
    }
}
