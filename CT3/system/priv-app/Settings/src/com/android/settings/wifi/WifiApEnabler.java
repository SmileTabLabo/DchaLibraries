package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import com.android.settings.R;
import com.android.settings.datausage.DataSaverBackend;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class WifiApEnabler {
    ConnectivityManager mCm;
    private final Context mContext;
    private final DataSaverBackend mDataSaverBackend;
    private final IntentFilter mIntentFilter;
    private final CharSequence mOriginalSummary;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wifi.WifiApEnabler.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                int state = intent.getIntExtra("wifi_state", 14);
                if (state == 14) {
                    int reason = intent.getIntExtra("wifi_ap_error_code", 0);
                    WifiApEnabler.this.handleWifiApStateChanged(state, reason);
                    return;
                }
                WifiApEnabler.this.handleWifiApStateChanged(state, 0);
            } else if ("android.net.conn.TETHER_STATE_CHANGED".equals(action)) {
                ArrayList<String> available = intent.getStringArrayListExtra("availableArray");
                ArrayList<String> active = intent.getStringArrayListExtra("activeArray");
                ArrayList<String> errored = intent.getStringArrayListExtra("erroredArray");
                WifiApEnabler.this.updateTetherState(available.toArray(), active.toArray(), errored.toArray());
            } else if (!"android.intent.action.AIRPLANE_MODE".equals(action)) {
            } else {
                WifiApEnabler.this.enableWifiSwitch();
            }
        }
    };
    private final SwitchPreference mSwitch;
    private WifiManager mWifiManager;
    private String[] mWifiRegexs;

    public WifiApEnabler(Context context, DataSaverBackend dataSaverBackend, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mDataSaverBackend = dataSaverBackend;
        this.mSwitch = switchPreference;
        this.mOriginalSummary = switchPreference.getSummary();
        switchPreference.setPersistent(false);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mWifiRegexs = this.mCm.getTetherableWifiRegexs();
        this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        this.mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
    }

    public void resume() {
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
        enableWifiSwitch();
    }

    public void pause() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void enableWifiSwitch() {
        boolean isAirplaneMode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        if (!isAirplaneMode) {
            this.mSwitch.setEnabled(this.mDataSaverBackend.isDataSaverEnabled() ? false : true);
            return;
        }
        this.mSwitch.setSummary(this.mOriginalSummary);
        this.mSwitch.setEnabled(false);
    }

    public void updateConfigSummary(WifiConfiguration wifiConfig) {
        String s = this.mContext.getString(17040345);
        SwitchPreference switchPreference = this.mSwitch;
        String string = this.mContext.getString(R.string.wifi_tether_enabled_subtext);
        Object[] objArr = new Object[1];
        if (wifiConfig != null) {
            s = wifiConfig.SSID;
        }
        objArr[0] = s;
        switchPreference.setSummary(String.format(string, objArr));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        String[] strArr;
        String[] strArr2;
        boolean wifiTethered = false;
        boolean wifiErrored = false;
        for (Object o : tethered) {
            String s = (String) o;
            for (String regex : this.mWifiRegexs) {
                if (s.matches(regex)) {
                    wifiTethered = true;
                }
            }
        }
        for (Object o2 : errored) {
            String s2 = (String) o2;
            for (String regex2 : this.mWifiRegexs) {
                if (s2.matches(regex2)) {
                    wifiErrored = true;
                }
            }
        }
        if (wifiTethered) {
            WifiConfiguration wifiConfig = this.mWifiManager.getWifiApConfiguration();
            updateConfigSummary(wifiConfig);
        } else if (wifiErrored) {
            this.mSwitch.setSummary(R.string.wifi_error);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleWifiApStateChanged(int state, int reason) {
        switch (state) {
            case 10:
                this.mSwitch.setSummary(R.string.wifi_tether_stopping);
                this.mSwitch.setChecked(false);
                this.mSwitch.setEnabled(false);
                return;
            case 11:
                this.mSwitch.setChecked(false);
                this.mSwitch.setSummary(this.mOriginalSummary);
                enableWifiSwitch();
                return;
            case 12:
                this.mSwitch.setSummary(R.string.wifi_tether_starting);
                this.mSwitch.setEnabled(false);
                return;
            case 13:
                this.mSwitch.setChecked(true);
                this.mSwitch.setEnabled(this.mDataSaverBackend.isDataSaverEnabled() ? false : true);
                return;
            default:
                this.mSwitch.setChecked(false);
                if (reason == 1) {
                    this.mSwitch.setSummary(R.string.wifi_sap_no_channel_error);
                } else {
                    this.mSwitch.setSummary(R.string.wifi_error);
                }
                enableWifiSwitch();
                return;
        }
    }
}
