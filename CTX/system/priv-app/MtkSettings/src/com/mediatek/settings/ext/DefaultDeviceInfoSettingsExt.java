package com.mediatek.settings.ext;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
/* loaded from: classes.dex */
public class DefaultDeviceInfoSettingsExt implements IDeviceInfoSettingsExt {
    @Override // com.mediatek.settings.ext.IDeviceInfoSettingsExt
    public void addEpushPreference(PreferenceScreen preferenceScreen) {
    }

    @Override // com.mediatek.settings.ext.IDeviceInfoSettingsExt
    public void updateSummary(Preference preference, String str, String str2) {
    }

    @Override // com.mediatek.settings.ext.IDeviceInfoSettingsExt
    public String customeModelInfo(String str) {
        return str;
    }
}
