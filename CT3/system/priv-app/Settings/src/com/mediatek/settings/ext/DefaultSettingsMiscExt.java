package com.mediatek.settings.ext;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.ImageView;
/* loaded from: classes.dex */
public class DefaultSettingsMiscExt extends ContextWrapper implements ISettingsMiscExt {
    static final String TAG = "DefaultSettingsMiscExt";

    public DefaultSettingsMiscExt(Context base) {
        super(base);
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public String customizeSimDisplayString(String simString, int slotId) {
        return simString;
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public void initCustomizedLocationSettings(PreferenceScreen root, int order) {
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public void updateCustomizedLocationSettings() {
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public void setFactoryResetTitle(Object obj) {
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public void setTimeoutPrefTitle(Preference pref) {
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public void addCustomizedItem(Object targetDashboardCategory, Boolean add) {
        Log.i(TAG, "DefaultSettingsMisc addCustomizedItem method going");
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public void customizeDashboardTile(Object tile, ImageView tileIcon) {
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public boolean isWifiOnlyModeSet() {
        return false;
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public String getNetworktypeString(String defaultString, int subId) {
        Log.d(TAG, "@M_getNetworktypeString defaultmethod return defaultString = " + defaultString);
        return defaultString;
    }

    @Override // com.mediatek.settings.ext.ISettingsMiscExt
    public String customizeMacAddressString(String macAddressString, String unavailable) {
        return macAddressString;
    }
}
