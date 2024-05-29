package com.mediatek.settingslib;

import android.os.SystemProperties;
/* loaded from: a.zip:com/mediatek/settingslib/FeatureOption.class */
public class FeatureOption {
    public static final boolean MTK_Bluetooth_DUN = getValue("bt.profiles.dun.enabled");

    private static boolean getValue(String str) {
        return SystemProperties.get(str).equals("1");
    }
}
