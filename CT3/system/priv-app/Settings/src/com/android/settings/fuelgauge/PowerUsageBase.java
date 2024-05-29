package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
/* loaded from: classes.dex */
public abstract class PowerUsageBase extends SettingsPreferenceFragment {
    private String mBatteryLevel;
    private String mBatteryStatus;
    protected BatteryStatsHelper mStatsHelper;
    protected UserManager mUm;
    private final Handler mHandler = new Handler() { // from class: com.android.settings.fuelgauge.PowerUsageBase.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    PowerUsageBase.this.mStatsHelper.clearStats();
                    PowerUsageBase.this.refreshStats();
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() { // from class: com.android.settings.fuelgauge.PowerUsageBase.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!"android.intent.action.BATTERY_CHANGED".equals(action) || !PowerUsageBase.this.updateBatteryStatus(intent) || PowerUsageBase.this.mHandler.hasMessages(100)) {
                return;
            }
            PowerUsageBase.this.mHandler.sendEmptyMessageDelayed(100, 500L);
        }
    };

    @Override // android.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mUm = (UserManager) activity.getSystemService("user");
        this.mStatsHelper = new BatteryStatsHelper(activity, true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mStatsHelper.create(icicle);
        setHasOptionsMenu(true);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStart() {
        super.onStart();
        this.mStatsHelper.clearStats();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        BatteryStatsHelper.dropFile(getActivity(), "tmp_bat_history.bin");
        updateBatteryStatus(getActivity().registerReceiver(this.mBatteryInfoReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED")));
        if (!this.mHandler.hasMessages(100)) {
            return;
        }
        this.mHandler.removeMessages(100);
        this.mStatsHelper.clearStats();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mBatteryInfoReceiver);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        super.onStop();
        this.mHandler.removeMessages(100);
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (!getActivity().isChangingConfigurations()) {
            return;
        }
        this.mStatsHelper.storeState();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem refresh = menu.add(0, 2, 0, R.string.menu_stats_refresh).setIcon(17302497).setAlphabeticShortcut('r');
        refresh.setShowAsAction(5);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
                this.mStatsHelper.clearStats();
                refreshStats();
                this.mHandler.removeMessages(100);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void refreshStats() {
        this.mStatsHelper.refreshStats(0, this.mUm.getUserProfiles());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updatePreference(BatteryHistoryPreference historyPref) {
        historyPref.setStats(this.mStatsHelper);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateBatteryStatus(Intent intent) {
        if (intent != null) {
            String batteryLevel = Utils.getBatteryPercentage(intent);
            String batteryStatus = Utils.getBatteryStatus(getResources(), intent);
            if (!batteryLevel.equals(this.mBatteryLevel) || !batteryStatus.equals(this.mBatteryStatus)) {
                this.mBatteryLevel = batteryLevel;
                this.mBatteryStatus = batteryStatus;
                return true;
            }
            return false;
        }
        return false;
    }
}
