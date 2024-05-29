package com.mediatek.settingslib.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import com.mediatek.common.MPlugin;
import com.mediatek.settingslib.ext.DefaultWifiLibExt;
import com.mediatek.settingslib.ext.IWifiLibExt;
/* loaded from: a.zip:com/mediatek/settingslib/wifi/AccessPointExt.class */
public class AccessPointExt {
    public static IWifiLibExt sWifiLibExt;

    public AccessPointExt(Context context) {
        getWifiPlugin(context);
    }

    public static int getSecurity(ScanResult scanResult) {
        if (scanResult.capabilities.contains("WAPI-PSK")) {
            return 4;
        }
        return scanResult.capabilities.contains("WAPI-CERT") ? 5 : -1;
    }

    public static int getSecurity(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.allowedKeyManagement.get(6)) {
            return 4;
        }
        return wifiConfiguration.allowedKeyManagement.get(7) ? 5 : -1;
    }

    public static IWifiLibExt getWifiPlugin(Context context) {
        if (sWifiLibExt == null) {
            sWifiLibExt = (IWifiLibExt) MPlugin.createInstance(IWifiLibExt.class.getName(), context);
            if (sWifiLibExt == null) {
                sWifiLibExt = new DefaultWifiLibExt();
            }
        }
        return sWifiLibExt;
    }
}
