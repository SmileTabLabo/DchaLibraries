package com.android.settings.fuelgauge;

import android.content.Intent;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fuelgauge.BatteryActiveView;
import com.android.settingslib.BatteryInfo;
import com.android.settingslib.graph.UsageView;
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

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String histFile = getArguments().getString("stats");
        this.mStats = BatteryStatsHelper.statsFromFile(getActivity(), histFile);
        this.mBatteryBroadcast = (Intent) getArguments().getParcelable("broadcast");
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(16843829, value, true);
        int accentColor = getContext().getColor(value.resourceId);
        this.mChargingParser = new BatteryFlagParser(accentColor, false, 524288);
        this.mScreenOn = new BatteryFlagParser(accentColor, false, 1048576);
        this.mGpsParser = new BatteryFlagParser(accentColor, false, 536870912);
        this.mFlashlightParser = new BatteryFlagParser(accentColor, true, 134217728);
        this.mCameraParser = new BatteryFlagParser(accentColor, true, 2097152);
        this.mWifiParser = new BatteryWifiParser(accentColor);
        this.mCpuParser = new BatteryFlagParser(accentColor, false, Integer.MIN_VALUE);
        this.mPhoneParser = new BatteryCellParser();
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.battery_history_detail, container, false);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateEverything();
    }

    private void updateEverything() {
        BatteryInfo info = BatteryInfo.getBatteryInfo(getContext(), this.mBatteryBroadcast, this.mStats, SystemClock.elapsedRealtime() * 1000);
        View view = getView();
        info.bindHistory((UsageView) view.findViewById(R.id.battery_usage), this.mChargingParser, this.mScreenOn, this.mGpsParser, this.mFlashlightParser, this.mCameraParser, this.mWifiParser, this.mCpuParser, this.mPhoneParser);
        ((TextView) view.findViewById(R.id.charge)).setText(info.batteryPercentString);
        ((TextView) view.findViewById(R.id.estimation)).setText(info.remainingLabel);
        bindData(this.mChargingParser, R.string.battery_stats_charging_label, R.id.charging_group);
        bindData(this.mScreenOn, R.string.battery_stats_screen_on_label, R.id.screen_on_group);
        bindData(this.mGpsParser, R.string.battery_stats_gps_on_label, R.id.gps_group);
        bindData(this.mFlashlightParser, R.string.battery_stats_flashlight_on_label, R.id.flashlight_group);
        bindData(this.mCameraParser, R.string.battery_stats_camera_on_label, R.id.camera_group);
        bindData(this.mWifiParser, R.string.battery_stats_wifi_running_label, R.id.wifi_group);
        bindData(this.mCpuParser, R.string.battery_stats_wake_lock_label, R.id.cpu_group);
        bindData(this.mPhoneParser, R.string.battery_stats_phone_signal_label, R.id.cell_network_group);
    }

    private void bindData(BatteryActiveView.BatteryActiveProvider provider, int label, int groupId) {
        View group = getView().findViewById(groupId);
        group.setVisibility(provider.hasData() ? 0 : 8);
        ((TextView) group.findViewById(16908310)).setText(label);
        ((BatteryActiveView) group.findViewById(R.id.battery_active)).setProvider(provider);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 51;
    }
}
