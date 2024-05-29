package com.android.settings.accounts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
/* loaded from: classes.dex */
public class ManagedProfileSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private RestrictedSwitchPreference mContactPrefrence;
    private Context mContext;
    private ManagedProfileBroadcastReceiver mManagedProfileBroadcastReceiver;
    private UserHandle mManagedUser;
    private UserManager mUserManager;
    private SwitchPreference mWorkModePreference;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.managed_profile_settings);
        this.mWorkModePreference = (SwitchPreference) findPreference("work_mode");
        this.mWorkModePreference.setOnPreferenceChangeListener(this);
        this.mContactPrefrence = (RestrictedSwitchPreference) findPreference("contacts_search");
        this.mContactPrefrence.setOnPreferenceChangeListener(this);
        this.mContext = getActivity().getApplicationContext();
        this.mUserManager = (UserManager) getSystemService("user");
        this.mManagedUser = getManagedUserFromArgument();
        if (this.mManagedUser == null) {
            getActivity().finish();
        }
        this.mManagedProfileBroadcastReceiver = new ManagedProfileBroadcastReceiver(this, null);
        this.mManagedProfileBroadcastReceiver.register(getActivity());
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        loadDataAndPopulateUi();
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mManagedProfileBroadcastReceiver.unregister(getActivity());
    }

    private UserHandle getManagedUserFromArgument() {
        UserHandle userHandle;
        Bundle arguments = getArguments();
        if (arguments == null || (userHandle = (UserHandle) arguments.getParcelable("android.intent.extra.USER")) == null || !this.mUserManager.isManagedProfile(userHandle.getIdentifier())) {
            return null;
        }
        return userHandle;
    }

    private void loadDataAndPopulateUi() {
        if (this.mWorkModePreference != null) {
            this.mWorkModePreference.setChecked(!this.mUserManager.isQuietModeEnabled(this.mManagedUser));
        }
        if (this.mContactPrefrence == null) {
            return;
        }
        int value = Settings.Secure.getIntForUser(getContentResolver(), "managed_profile_contact_remote_search", 0, this.mManagedUser.getIdentifier());
        this.mContactPrefrence.setChecked(value != 0);
        RestrictedLockUtils.EnforcedAdmin enforcedAdmin = RestrictedLockUtils.checkIfRemoteContactSearchDisallowed(this.mContext, this.mManagedUser.getIdentifier());
        this.mContactPrefrence.setDisabledByAdmin(enforcedAdmin);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 401;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mWorkModePreference) {
            if (((Boolean) newValue).booleanValue()) {
                this.mUserManager.trySetQuietModeDisabled(this.mManagedUser.getIdentifier(), null);
            } else {
                this.mUserManager.setQuietModeEnabled(this.mManagedUser.getIdentifier(), true);
            }
            return true;
        } else if (preference == this.mContactPrefrence) {
            int value = ((Boolean) newValue).booleanValue() ? 1 : 0;
            Settings.Secure.putIntForUser(getContentResolver(), "managed_profile_contact_remote_search", value, this.mManagedUser.getIdentifier());
            return true;
        } else {
            return false;
        }
    }

    /* loaded from: classes.dex */
    private class ManagedProfileBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ ManagedProfileBroadcastReceiver(ManagedProfileSettings this$0, ManagedProfileBroadcastReceiver managedProfileBroadcastReceiver) {
            this();
        }

        private ManagedProfileBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("ManagedProfileSettings", "Received broadcast: " + action);
            if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == ManagedProfileSettings.this.mManagedUser.getIdentifier()) {
                    ManagedProfileSettings.this.getActivity().finish();
                }
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == ManagedProfileSettings.this.mManagedUser.getIdentifier()) {
                    ManagedProfileSettings.this.mWorkModePreference.setChecked(!ManagedProfileSettings.this.mUserManager.isQuietModeEnabled(ManagedProfileSettings.this.mManagedUser));
                }
            } else {
                Log.w("ManagedProfileSettings", "Cannot handle received broadcast: " + intent.getAction());
            }
        }

        public void register(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
            context.registerReceiver(this, intentFilter);
        }

        public void unregister(Context context) {
            context.unregisterReceiver(this);
        }
    }
}
