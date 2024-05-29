package com.mediatek.settings.ext;

import android.content.Context;
import android.os.storage.VolumeInfo;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
/* loaded from: classes.dex */
public class DefaultStorageSettingsExt implements IStorageSettingsExt {
    @Override // com.mediatek.settings.ext.IStorageSettingsExt
    public void initCustomizationStoragePlugin(Context context) {
    }

    @Override // com.mediatek.settings.ext.IStorageSettingsExt
    public void updateCustomizedStorageSettingsPlugin(PreferenceCategory prefcategory) {
    }

    @Override // com.mediatek.settings.ext.IStorageSettingsExt
    public void updateCustomizedPrivateSettingsPlugin(PreferenceScreen screen, VolumeInfo vol) {
    }

    @Override // com.mediatek.settings.ext.IStorageSettingsExt
    public void updateCustomizedPrefDetails(VolumeInfo vol) {
    }

    @Override // com.mediatek.settings.ext.IStorageSettingsExt
    public void updateCustomizedStorageSummary(Object summaryProvider, Object summaryLoader) {
    }
}
