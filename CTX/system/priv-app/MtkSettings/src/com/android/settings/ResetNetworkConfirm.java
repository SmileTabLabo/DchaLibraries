package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.os.UserHandle;
import android.provider.Telephony;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.mediatek.ims.internal.MtkImsManager;
/* loaded from: classes.dex */
public class ResetNetworkConfirm extends InstrumentedFragment {
    private View mContentView;
    boolean mEraseEsim;
    EraseEsimAsyncTask mEraseEsimTask;
    private int mSubId = -1;
    private View.OnClickListener mFinalClickListener = new View.OnClickListener() { // from class: com.android.settings.ResetNetworkConfirm.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BluetoothAdapter adapter;
            if (Utils.isMonkeyRunning()) {
                return;
            }
            Activity activity = ResetNetworkConfirm.this.getActivity();
            Log.v("ResetNetwork", "begin reset ConnectivityManager");
            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService("connectivity");
            if (connectivityManager != null) {
                connectivityManager.factoryReset();
            }
            Log.v("ResetNetwork", "begin reset WifiManager");
            WifiManager wifiManager = (WifiManager) activity.getSystemService("wifi");
            if (wifiManager != null) {
                wifiManager.factoryReset();
            }
            Log.v("ResetNetwork", "begin reset TelephonyManager");
            TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService("phone");
            if (telephonyManager != null) {
                telephonyManager.factoryReset(ResetNetworkConfirm.this.mSubId);
            }
            Log.v("ResetNetwork", "begin reset NetworkPolicyManager");
            NetworkPolicyManager networkPolicyManager = (NetworkPolicyManager) activity.getSystemService("netpolicy");
            if (networkPolicyManager != null) {
                networkPolicyManager.factoryReset(telephonyManager.getSubscriberId(ResetNetworkConfirm.this.mSubId));
            }
            Log.v("ResetNetwork", "begin reset BluetoothManager");
            BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService("bluetooth");
            if (bluetoothManager != null && (adapter = bluetoothManager.getAdapter()) != null) {
                adapter.factoryReset();
            }
            Log.v("ResetNetwork", "begin reset ImsManager");
            ResetNetworkConfirm.this.resetImsNetwork(activity, ResetNetworkConfirm.this.mSubId);
            Log.v("ResetNetwork", "begin reset Apn");
            ResetNetworkConfirm.this.restoreDefaultApn(activity);
            ResetNetworkConfirm.this.esimFactoryReset(activity, activity.getPackageName());
            ResetNetworkConfirm.this.cleanUpSmsRawTable(activity);
            Log.v("ResetNetwork", "ret end");
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class EraseEsimAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final Context mContext;
        private final String mPackageName;

        EraseEsimAsyncTask(Context context, String str) {
            this.mContext = context;
            this.mPackageName = str;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Void... voidArr) {
            return Boolean.valueOf(RecoverySystem.wipeEuiccData(this.mContext, this.mPackageName));
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            if (bool.booleanValue()) {
                Toast.makeText(this.mContext, (int) R.string.reset_network_complete_toast, 0).show();
            } else {
                new AlertDialog.Builder(this.mContext).setTitle(R.string.reset_esim_error_title).setMessage(R.string.reset_esim_error_msg).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).show();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetImsNetwork(Context context, int i) {
        int phoneId;
        if (i == -1) {
            phoneId = 0;
        } else {
            phoneId = SubscriptionManager.getPhoneId(this.mSubId);
        }
        MtkImsManager.factoryReset(context, phoneId);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cleanUpSmsRawTable(Context context) {
        context.getContentResolver().delete(Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw/permanentDelete"), null, null);
    }

    void esimFactoryReset(Context context, String str) {
        if (this.mEraseEsim) {
            this.mEraseEsimTask = new EraseEsimAsyncTask(context, str);
            this.mEraseEsimTask.execute(new Void[0]);
            return;
        }
        Toast.makeText(context, (int) R.string.reset_network_complete_toast, 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void restoreDefaultApn(Context context) {
        Uri parse = Uri.parse("content://telephony/carriers/restore");
        if (SubscriptionManager.isUsableSubIdValue(this.mSubId)) {
            parse = Uri.withAppendedPath(parse, "subId/" + String.valueOf(this.mSubId));
        }
        context.getContentResolver().delete(parse, null, null);
    }

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(R.id.execute_reset_network).setOnClickListener(this.mFinalClickListener);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_network_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_network_reset", UserHandle.myUserId())) {
            return layoutInflater.inflate(R.layout.network_reset_disallowed_screen, (ViewGroup) null);
        }
        if (checkIfRestrictionEnforced != null) {
            new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_network_reset", checkIfRestrictionEnforced).setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.settings.-$$Lambda$ResetNetworkConfirm$YTG2-gTxf5vyFkKGLAaR8nzFOxo
                @Override // android.content.DialogInterface.OnDismissListener
                public final void onDismiss(DialogInterface dialogInterface) {
                    ResetNetworkConfirm.this.getActivity().finish();
                }
            }).show();
            return new View(getContext());
        }
        this.mContentView = layoutInflater.inflate(R.layout.reset_network_confirm, (ViewGroup) null);
        establishFinalConfirmationState();
        return this.mContentView;
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.mSubId = arguments.getInt("subscription", -1);
            this.mEraseEsim = arguments.getBoolean("erase_esim");
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onDestroy() {
        if (this.mEraseEsimTask != null) {
            this.mEraseEsimTask.cancel(true);
            this.mEraseEsimTask = null;
        }
        super.onDestroy();
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 84;
    }
}
