package com.android.settingslib.wifi;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/wifi/WifiStatusTracker.class */
public class WifiStatusTracker {
    public boolean connected;
    public boolean enabled;
    public int level;
    private final WifiManager mWifiManager;
    public int rssi;
    public String ssid;

    public WifiStatusTracker(WifiManager wifiManager) {
        this.mWifiManager = wifiManager;
    }

    private String getSsid(WifiInfo wifiInfo) {
        String ssid = wifiInfo.getSSID();
        if (ssid != null) {
            return ssid;
        }
        List<WifiConfiguration> configuredNetworks = this.mWifiManager.getConfiguredNetworks();
        int size = configuredNetworks.size();
        for (int i = 0; i < size; i++) {
            if (configuredNetworks.get(i).networkId == wifiInfo.getNetworkId()) {
                return configuredNetworks.get(i).SSID;
            }
        }
        return null;
    }

    public void handleBroadcast(Intent intent) {
        boolean z = false;
        String action = intent.getAction();
        if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
            if (intent.getIntExtra("wifi_state", 4) == 3) {
                z = true;
            }
            this.enabled = z;
        } else if (!action.equals("android.net.wifi.STATE_CHANGE")) {
            if (action.equals("android.net.wifi.RSSI_CHANGED")) {
                this.rssi = intent.getIntExtra("newRssi", -200);
                this.level = WifiManager.calculateSignalLevel(this.rssi, 5);
            }
        } else {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            boolean z2 = false;
            if (networkInfo != null) {
                z2 = networkInfo.isConnected();
            }
            this.connected = z2;
            if (!this.connected) {
                if (this.connected) {
                    return;
                }
                this.ssid = null;
                return;
            }
            WifiInfo connectionInfo = intent.getParcelableExtra("wifiInfo") != null ? (WifiInfo) intent.getParcelableExtra("wifiInfo") : this.mWifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                this.ssid = getSsid(connectionInfo);
            } else {
                this.ssid = null;
            }
        }
    }
}
