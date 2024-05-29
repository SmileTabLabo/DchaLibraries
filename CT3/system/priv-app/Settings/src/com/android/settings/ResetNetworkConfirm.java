package com.android.settings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkPolicyManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.ims.ImsManager;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class ResetNetworkConfirm extends OptionsMenuFragment {
    private View mContentView;
    private int mSubId = -1;
    private View.OnClickListener mFinalClickListener = new View.OnClickListener() { // from class: com.android.settings.ResetNetworkConfirm.1
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            BluetoothAdapter btAdapter;
            if (Utils.isMonkeyRunning()) {
                return;
            }
            Context context = ResetNetworkConfirm.this.getActivity();
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager != null) {
                connectivityManager.factoryReset();
            }
            WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
            if (wifiManager != null) {
                wifiManager.factoryReset();
            }
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager != null) {
                telephonyManager.factoryReset(ResetNetworkConfirm.this.mSubId);
            }
            NetworkPolicyManager policyManager = (NetworkPolicyManager) context.getSystemService("netpolicy");
            if (policyManager != null) {
                String subscriberId = telephonyManager.getSubscriberId(ResetNetworkConfirm.this.mSubId);
                policyManager.factoryReset(subscriberId);
            }
            BluetoothManager btManager = (BluetoothManager) context.getSystemService("bluetooth");
            if (btManager != null && (btAdapter = btManager.getAdapter()) != null) {
                btAdapter.factoryReset();
            }
            ImsManager.factoryReset(context);
            Toast.makeText(context, (int) R.string.reset_network_complete_toast, 0).show();
        }
    };

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(R.id.execute_reset_network).setOnClickListener(this.mFinalClickListener);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RestrictedLockUtils.EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_network_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_network_reset", UserHandle.myUserId())) {
            return inflater.inflate(R.layout.network_reset_disallowed_screen, (ViewGroup) null);
        }
        if (admin != null) {
            View view = inflater.inflate(R.layout.admin_support_details_empty_view, (ViewGroup) null);
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
            view.setVisibility(0);
            return view;
        }
        this.mContentView = inflater.inflate(R.layout.reset_network_confirm, (ViewGroup) null);
        establishFinalConfirmationState();
        return this.mContentView;
    }

    @Override // com.android.settings.InstrumentedFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        this.mSubId = args.getInt("subscription", -1);
    }

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 84;
    }
}
