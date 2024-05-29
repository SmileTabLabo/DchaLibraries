package com.android.settings.datausage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.datausage.TemplatePreference;
import com.android.settingslib.NetworkPolicyEditor;
import com.mediatek.settings.sim.SimHotSwapHandler;
/* loaded from: classes.dex */
public abstract class DataUsageBaseFragment extends DashboardFragment {
    private SimHotSwapHandler mSimHotSwapHandler;
    protected final TemplatePreference.NetworkServices services = new TemplatePreference.NetworkServices();

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Context context = getContext();
        this.services.mNetworkService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.services.mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
        this.services.mPolicyManager = (NetworkPolicyManager) context.getSystemService("netpolicy");
        this.services.mPolicyEditor = new NetworkPolicyEditor(this.services.mPolicyManager);
        this.services.mTelephonyManager = TelephonyManager.from(context);
        this.services.mSubscriptionManager = SubscriptionManager.from(context);
        this.services.mUserManager = UserManager.get(context);
        this.mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.android.settings.datausage.DataUsageBaseFragment.1
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                if (DataUsageBaseFragment.this.getActivity() != null) {
                    Log.d("DataUsageBase", "onSimHotSwap, finish Activity~~");
                    DataUsageBaseFragment.this.getActivity().finish();
                }
            }
        });
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.services.mPolicyEditor.read();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isAdmin() {
        return this.services.mUserManager.isAdminUser();
    }

    public boolean hasEthernet(Context context) {
        long j;
        boolean isNetworkSupported = ConnectivityManager.from(context).isNetworkSupported(9);
        try {
            INetworkStatsSession openSession = this.services.mStatsService.openSession();
            if (openSession != null) {
                j = openSession.getSummaryForNetwork(NetworkTemplate.buildTemplateEthernet(), Long.MIN_VALUE, Long.MAX_VALUE).getTotalBytes();
                TrafficStats.closeQuietly(openSession);
            } else {
                j = 0;
            }
            return isNetworkSupported && j > 0;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mSimHotSwapHandler.unregisterOnSimHotSwap();
    }
}
