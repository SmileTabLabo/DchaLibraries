package com.android.settings.datausage;

import android.app.Activity;
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
import com.mediatek.settings.sim.SimHotSwapHandler;
@Deprecated
/* loaded from: classes.dex */
public abstract class DataUsageBase extends SettingsPreferenceFragment {
    private SimHotSwapHandler mSimHotSwapHandler;
    protected final TemplatePreference.NetworkServices services = new TemplatePreference.NetworkServices();

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Activity activity = getActivity();
        this.services.mNetworkService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.services.mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
        this.services.mPolicyManager = NetworkPolicyManager.from(activity);
        this.services.mPolicyEditor = new NetworkPolicyEditor(this.services.mPolicyManager);
        this.services.mTelephonyManager = TelephonyManager.from(activity);
        this.services.mSubscriptionManager = SubscriptionManager.from(activity);
        this.services.mUserManager = UserManager.get(activity);
        this.mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.android.settings.datausage.DataUsageBase.1
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                if (DataUsageBase.this.getActivity() != null) {
                    Log.d("DataUsageBase", "onSimHotSwap, finish Activity~~");
                    DataUsageBase.this.getActivity().finish();
                }
            }
        });
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.services.mPolicyEditor.read();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isMobileDataAvailable(int i) {
        if (i != -1) {
            return this.services.mSubscriptionManager.getActiveSubscriptionInfo(i) != null;
        }
        Log.w("DataUsageBase", "isMobileDataAvailable INVALID_SUBSCRIPTION_ID");
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isNetworkPolicyModifiable(NetworkPolicy networkPolicy, int i) {
        return networkPolicy != null && isBandwidthControlEnabled() && this.services.mUserManager.isAdminUser() && isDataEnabled(i);
    }

    private boolean isDataEnabled(int i) {
        if (i == -1) {
            Log.w("DataUsageBase", "isDataEnabled INVALID_SUBSCRIPTION_ID");
            return false;
        }
        return this.services.mTelephonyManager.getDataEnabled(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isBandwidthControlEnabled() {
        try {
            return this.services.mNetworkService.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            Log.w("DataUsageBase", "problem talking with INetworkManagementService: ", e);
            return false;
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mSimHotSwapHandler.unregisterOnSimHotSwap();
    }
}
