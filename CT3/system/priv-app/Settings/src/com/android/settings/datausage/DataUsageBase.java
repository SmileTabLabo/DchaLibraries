package com.android.settings.datausage;

import android.content.Context;
import android.net.INetworkStatsService;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.datausage.TemplatePreference;
import com.android.settingslib.NetworkPolicyEditor;
/* loaded from: classes.dex */
public abstract class DataUsageBase extends SettingsPreferenceFragment {
    protected final TemplatePreference.NetworkServices services = new TemplatePreference.NetworkServices();

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getActivity();
        this.services.mNetworkService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.services.mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
        this.services.mPolicyManager = NetworkPolicyManager.from(context);
        this.services.mPolicyEditor = new NetworkPolicyEditor(this.services.mPolicyManager);
        this.services.mTelephonyManager = TelephonyManager.from(context);
        this.services.mSubscriptionManager = SubscriptionManager.from(context);
        this.services.mUserManager = UserManager.get(context);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.services.mPolicyEditor.read();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isAdmin() {
        return this.services.mUserManager.isAdminUser();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isMobileDataAvailable(int subId) {
        return this.services.mSubscriptionManager.getActiveSubscriptionInfo(subId) != null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isNetworkPolicyModifiable(NetworkPolicy policy, int subId) {
        if (policy != null && isBandwidthControlEnabled() && this.services.mUserManager.isAdminUser()) {
            return isDataEnabled(subId);
        }
        return false;
    }

    private boolean isDataEnabled(int subId) {
        if (subId == -1) {
            return true;
        }
        return this.services.mTelephonyManager.getDataEnabled(subId);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isBandwidthControlEnabled() {
        try {
            return this.services.mNetworkService.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            Log.w("DataUsageBase", "problem talking with INetworkManagementService: " + e);
            return false;
        }
    }
}
