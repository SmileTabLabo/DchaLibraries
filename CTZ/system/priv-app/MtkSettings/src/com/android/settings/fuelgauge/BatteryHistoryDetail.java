package com.android.settings.fuelgauge;

import android.content.Intent;
import android.os.BatteryStats;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fuelgauge.BatteryActiveView;
import com.android.settings.fuelgauge.BatteryInfo;
import com.android.settings.graph.UsageView;
import com.android.settingslib.wifi.AccessPoint;
/* loaded from: classes.dex */
public class BatteryHistoryDetail extends SettingsPreferenceFragment {
    private Intent mBatteryBroadcast;
    private BatteryFlagParser mCameraParser;
    private BatteryFlagParser mChargingParser;
    private BatteryFlagParser mCpuParser;
    private BatteryFlagParser mFlashlightParser;
    private BatteryFlagParser mGpsParser;
    private BatteryCellParser mPhoneParser;
    private BatteryFlagParser mScreenOn;
    private BatteryStats mStats;
    private BatteryWifiParser mWifiParser;

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mStats = BatteryStatsHelper.statsFromFile(getActivity(), getArguments().getString("stats"));
        this.mBatteryBroadcast = (Intent) getArguments().getParcelable("broadcast");
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(16843829, typedValue, true);
        int color = getContext().getColor(typedValue.resourceId);
        this.mChargingParser = new BatteryFlagParser(color, false, 524288);
        this.mScreenOn = new BatteryFlagParser(color, false, 1048576);
        this.mGpsParser = new BatteryFlagParser(color, false, 536870912);
        this.mFlashlightParser = new BatteryFlagParser(color, true, 134217728);
        this.mCameraParser = new BatteryFlagParser(color, true, 2097152);
        this.mWifiParser = new BatteryWifiParser(color);
        this.mCpuParser = new BatteryFlagParser(color, false, AccessPoint.UNREACHABLE_RSSI);
        this.mPhoneParser = new BatteryCellParser();
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.battery_history_detail, viewGroup, false);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        updateEverything();
    }

    private void updateEverything() {
        BatteryInfo.getBatteryInfo(getContext(), new BatteryInfo.Callback() { // from class: com.android.settings.fuelgauge.-$$Lambda$BatteryHistoryDetail$ZIvw_m8MPrnAuz9tJSzFmSFxa_8
            @Override // com.android.settings.fuelgauge.BatteryInfo.Callback
            public final void onBatteryInfoLoaded(BatteryInfo batteryInfo) {
                BatteryHistoryDetail.lambda$updateEverything$0(BatteryHistoryDetail.this, batteryInfo);
            }
        }, this.mStats, false);
    }

    public static /* synthetic */ void lambda$updateEverything$0(BatteryHistoryDetail batteryHistoryDetail, BatteryInfo batteryInfo) {
        View view = batteryHistoryDetail.getView();
        batteryInfo.bindHistory((UsageView) view.findViewById(R.id.battery_usage), batteryHistoryDetail.mChargingParser, batteryHistoryDetail.mScreenOn, batteryHistoryDetail.mGpsParser, batteryHistoryDetail.mFlashlightParser, batteryHistoryDetail.mCameraParser, batteryHistoryDetail.mWifiParser, batteryHistoryDetail.mCpuParser, batteryHistoryDetail.mPhoneParser);
        ((TextView) view.findViewById(R.id.charge)).setText(batteryInfo.batteryPercentString);
        ((TextView) view.findViewById(R.id.estimation)).setText(batteryInfo.remainingLabel);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mChargingParser, R.string.battery_stats_charging_label, R.id.charging_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mScreenOn, R.string.battery_stats_screen_on_label, R.id.screen_on_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mGpsParser, R.string.battery_stats_gps_on_label, R.id.gps_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mFlashlightParser, R.string.battery_stats_flashlight_on_label, R.id.flashlight_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mCameraParser, R.string.battery_stats_camera_on_label, R.id.camera_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mWifiParser, R.string.battery_stats_wifi_running_label, R.id.wifi_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mCpuParser, R.string.battery_stats_wake_lock_label, R.id.cpu_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mPhoneParser, R.string.battery_stats_phone_signal_label, R.id.cell_network_group);
    }

    private void bindData(BatteryActiveView.BatteryActiveProvider batteryActiveProvider, int i, int i2) {
        View findViewById = getView().findViewById(i2);
        findViewById.setVisibility(batteryActiveProvider.hasData() ? 0 : 8);
        ((TextView) findViewById.findViewById(16908310)).setText(i);
        ((BatteryActiveView) findViewById.findViewById(R.id.battery_active)).setProvider(batteryActiveProvider);
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 51;
    }
}
