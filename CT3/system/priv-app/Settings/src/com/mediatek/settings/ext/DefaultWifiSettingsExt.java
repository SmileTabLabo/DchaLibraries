package com.mediatek.settings.ext;

import android.content.ContentResolver;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.ContextMenu;
/* loaded from: classes.dex */
public class DefaultWifiSettingsExt implements IWifiSettingsExt {
    private static final String TAG = "DefaultWifiSettingsExt";

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void registerPriorityObserver(ContentResolver contentResolver) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void unregisterPriorityObserver(ContentResolver contentResolver) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void setLastConnectedConfig(WifiConfiguration config) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void updatePriority() {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void updateContextMenu(ContextMenu menu, int menuId, NetworkInfo.DetailedState state) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void emptyCategory(PreferenceScreen screen) {
        screen.removeAll();
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void emptyScreen(PreferenceScreen screen) {
        screen.removeAll();
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void refreshCategory(PreferenceScreen screen) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void recordPriority(int selectPriority) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void setNewPriority(WifiConfiguration config) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void updatePriorityAfterSubmit(WifiConfiguration config) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void disconnect(WifiConfiguration wifiConfig) {
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void addPreference(PreferenceScreen screen, Preference preference, boolean isConfiged) {
        if (screen == null) {
            return;
        }
        screen.addPreference(preference);
    }

    @Override // com.mediatek.settings.ext.IWifiSettingsExt
    public void addCategories(PreferenceScreen screen) {
    }
}
