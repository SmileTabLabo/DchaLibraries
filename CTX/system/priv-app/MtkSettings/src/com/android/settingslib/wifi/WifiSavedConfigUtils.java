package com.android.settingslib.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.hotspot2.PasspointConfiguration;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class WifiSavedConfigUtils {
    public static List<AccessPoint> getAllConfigs(Context context, WifiManager wifiManager) {
        ArrayList arrayList = new ArrayList();
        for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
            if (!wifiConfiguration.isPasspoint() && !wifiConfiguration.isEphemeral()) {
                arrayList.add(new AccessPoint(context, wifiConfiguration));
            }
        }
        try {
            for (PasspointConfiguration passpointConfiguration : wifiManager.getPasspointConfigurations()) {
                arrayList.add(new AccessPoint(context, passpointConfiguration));
            }
        } catch (UnsupportedOperationException e) {
        }
        return arrayList;
    }
}
