package com.android.settings.wifi.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
/* loaded from: classes.dex */
public class WifiP2pPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {
    private final IntentFilter mFilter;
    final BroadcastReceiver mReceiver;
    private Preference mWifiDirectPref;
    private final WifiManager mWifiManager;

    public WifiP2pPreferenceController(Context context, Lifecycle lifecycle, WifiManager wifiManager) {
        super(context);
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wifi.p2p.WifiP2pPreferenceController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                WifiP2pPreferenceController.this.togglePreferences();
            }
        };
        this.mFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mWifiManager = wifiManager;
        lifecycle.addObserver(this);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mWifiDirectPref = preferenceScreen.findPreference("wifi_direct");
        togglePreferences();
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnResume
    public void onResume() {
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnPause
    public void onPause() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "wifi_direct";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void togglePreferences() {
        if (this.mWifiDirectPref != null) {
            this.mWifiDirectPref.setEnabled(this.mWifiManager.isWifiEnabled());
        }
    }
}
