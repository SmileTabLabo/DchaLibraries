package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.widget.SummaryUpdater;
import com.android.settingslib.wifi.WifiStatusTracker;
/* loaded from: classes.dex */
public final class WifiSummaryUpdater extends SummaryUpdater {
    private static final IntentFilter INTENT_FILTER = new IntentFilter();
    private final BroadcastReceiver mReceiver;
    private final WifiStatusTracker mWifiTracker;

    static {
        INTENT_FILTER.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        INTENT_FILTER.addAction("android.net.wifi.STATE_CHANGE");
        INTENT_FILTER.addAction("android.net.wifi.RSSI_CHANGED");
    }

    public WifiSummaryUpdater(Context context, SummaryUpdater.OnSummaryChangeListener onSummaryChangeListener) {
        this(context, onSummaryChangeListener, null);
    }

    public WifiSummaryUpdater(Context context, SummaryUpdater.OnSummaryChangeListener onSummaryChangeListener, WifiStatusTracker wifiStatusTracker) {
        super(context, onSummaryChangeListener);
        WifiStatusTracker wifiStatusTracker2;
        if (wifiStatusTracker == null) {
            wifiStatusTracker2 = new WifiStatusTracker(context, (WifiManager) context.getSystemService(WifiManager.class), (NetworkScoreManager) context.getSystemService(NetworkScoreManager.class), (ConnectivityManager) context.getSystemService(ConnectivityManager.class), new Runnable() { // from class: com.android.settings.wifi.-$$Lambda$WifiSummaryUpdater$5w1MXX8MJfsbMZcSIHVb0vJmaww
                @Override // java.lang.Runnable
                public final void run() {
                    WifiSummaryUpdater.this.notifyChangeIfNeeded();
                }
            });
        } else {
            wifiStatusTracker2 = wifiStatusTracker;
        }
        this.mWifiTracker = wifiStatusTracker2;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wifi.WifiSummaryUpdater.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                WifiSummaryUpdater.this.mWifiTracker.handleBroadcast(intent);
                WifiSummaryUpdater.this.notifyChangeIfNeeded();
            }
        };
    }

    public void register(boolean z) {
        if (z) {
            this.mContext.registerReceiver(this.mReceiver, INTENT_FILTER);
        } else {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        this.mWifiTracker.setListening(z);
    }

    @Override // com.android.settings.widget.SummaryUpdater
    public String getSummary() {
        if (!this.mWifiTracker.enabled) {
            return this.mContext.getString(R.string.switch_off_text);
        }
        if (!this.mWifiTracker.connected) {
            return this.mContext.getString(R.string.disconnected);
        }
        String removeDoubleQuotes = android.net.wifi.WifiInfo.removeDoubleQuotes(this.mWifiTracker.ssid);
        return TextUtils.isEmpty(this.mWifiTracker.statusLabel) ? removeDoubleQuotes : this.mContext.getResources().getString(R.string.preference_summary_default_combination, removeDoubleQuotes, this.mWifiTracker.statusLabel);
    }
}
