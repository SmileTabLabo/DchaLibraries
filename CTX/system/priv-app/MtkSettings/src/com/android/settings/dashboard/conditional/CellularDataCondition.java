package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import com.android.settings.R;
import com.android.settings.Settings;
/* loaded from: classes.dex */
public class CellularDataCondition extends Condition {
    private static final IntentFilter DATA_CONNECTION_FILTER = new IntentFilter("android.intent.action.ANY_DATA_STATE");
    private final Receiver mReceiver;

    public CellularDataCondition(ConditionManager conditionManager) {
        super(conditionManager);
        this.mReceiver = new Receiver();
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void refreshState() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class);
        if (!((ConnectivityManager) this.mManager.getContext().getSystemService(ConnectivityManager.class)).isNetworkSupported(0) || telephonyManager.getSimState() != 5) {
            setActive(false);
        } else {
            setActive(!telephonyManager.isDataEnabled());
        }
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    protected BroadcastReceiver getReceiver() {
        return this.mReceiver;
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    protected IntentFilter getIntentFilter() {
        return DATA_CONNECTION_FILTER;
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_cellular_off);
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
        this.mManager.getContext().startActivity(new Intent(this.mManager.getContext(), Settings.DataUsageSummaryActivity.class).addFlags(268435456));
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onActionClick(int i) {
        if (i == 0) {
            ((TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class)).setDataEnabled(true);
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + i);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public int getMetricsConstant() {
        return 380;
    }

    /* loaded from: classes.dex */
    public static class Receiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            CellularDataCondition cellularDataCondition;
            if ("android.intent.action.ANY_DATA_STATE".equals(intent.getAction()) && (cellularDataCondition = (CellularDataCondition) ConditionManager.get(context).getCondition(CellularDataCondition.class)) != null) {
                cellularDataCondition.refreshState();
            }
        }
    }
}
