package com.android.settings.fuelgauge.batterytip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/* loaded from: classes.dex */
public class AnomalyDetectionReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        long longExtra = intent.getLongExtra("android.app.extra.STATS_CONFIG_UID", -1L);
        long longExtra2 = intent.getLongExtra("android.app.extra.STATS_CONFIG_KEY", -1L);
        long longExtra3 = intent.getLongExtra("android.app.extra.STATS_SUBSCRIPTION_ID", -1L);
        Log.i("SettingsAnomalyReceiver", "Anomaly intent received.  configUid = " + longExtra + " configKey = " + longExtra2 + " subscriptionId = " + longExtra3);
        intent.getExtras().putLong("key_anomaly_timestamp", System.currentTimeMillis());
        AnomalyDetectionJobService.scheduleAnomalyDetection(context, intent);
    }
}
