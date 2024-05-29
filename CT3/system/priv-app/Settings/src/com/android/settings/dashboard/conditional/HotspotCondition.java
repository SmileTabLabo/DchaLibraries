package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import com.android.settings.R;
import com.android.settings.TetherSettings;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.TetherUtil;
/* loaded from: classes.dex */
public class HotspotCondition extends Condition {
    private final WifiManager mWifiManager;

    public HotspotCondition(ConditionManager manager) {
        super(manager);
        this.mWifiManager = (WifiManager) this.mManager.getContext().getSystemService(WifiManager.class);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void refreshState() {
        boolean wifiTetherEnabled = this.mWifiManager.isWifiApEnabled();
        setActive(wifiTetherEnabled);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    protected Class<?> getReceiverClass() {
        return Receiver.class;
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), (int) R.drawable.ic_hotspot);
    }

    private String getSsid() {
        WifiConfiguration wifiConfig = this.mWifiManager.getWifiApConfiguration();
        if (wifiConfig == null) {
            return this.mManager.getContext().getString(17040345);
        }
        return wifiConfig.SSID;
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_hotspot_title);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_hotspot_summary, getSsid());
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence[] getActions() {
        Context context = this.mManager.getContext();
        return RestrictedLockUtils.hasBaseUserRestriction(context, "no_config_tethering", UserHandle.myUserId()) ? new CharSequence[0] : new CharSequence[]{context.getString(R.string.condition_turn_off)};
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onPrimaryClick() {
        Utils.startWithFragment(this.mManager.getContext(), TetherSettings.class.getName(), null, null, 0, R.string.tether_settings_title_all, null);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onActionClick(int index) {
        if (index == 0) {
            Context context = this.mManager.getContext();
            RestrictedLockUtils.EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_config_tethering", UserHandle.myUserId());
            if (admin != null) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, admin);
                return;
            }
            TetherUtil.setWifiTethering(false, context);
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public int getMetricsConstant() {
        return 382;
    }

    /* loaded from: classes.dex */
    public static class Receiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                return;
            }
            ((HotspotCondition) ConditionManager.get(context).getCondition(HotspotCondition.class)).refreshState();
        }
    }
}
