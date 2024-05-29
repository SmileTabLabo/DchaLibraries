package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import com.android.settings.R;
import com.android.settings.Settings;
/* loaded from: classes.dex */
public class CellularDataCondition extends Condition {
    public CellularDataCondition(ConditionManager manager) {
        super(manager);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void refreshState() {
        ConnectivityManager connectivity = (ConnectivityManager) this.mManager.getContext().getSystemService(ConnectivityManager.class);
        TelephonyManager telephony = (TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class);
        if (!connectivity.isNetworkSupported(0) || telephony.getSimState() != 5) {
            setActive(false);
        } else {
            setActive(telephony.getDataEnabled() ? false : true);
        }
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    protected Class<?> getReceiverClass() {
        return Receiver.class;
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), (int) R.drawable.ic_cellular_off);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_cellular_title);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_cellular_summary);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_on)};
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent(this.mManager.getContext(), Settings.DataUsageSummaryActivity.class));
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onActionClick(int index) {
        if (index == 0) {
            TelephonyManager telephony = (TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class);
            telephony.setDataEnabled(true);
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public int getMetricsConstant() {
        return 380;
    }

    /* loaded from: classes.dex */
    public static class Receiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.intent.action.ANY_DATA_STATE".equals(intent.getAction())) {
                return;
            }
            ((CellularDataCondition) ConditionManager.get(context).getCondition(CellularDataCondition.class)).refreshState();
        }
    }
}
