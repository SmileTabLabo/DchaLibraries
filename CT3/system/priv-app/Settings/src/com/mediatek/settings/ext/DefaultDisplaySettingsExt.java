package com.mediatek.settings.ext;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
/* loaded from: classes.dex */
public class DefaultDisplaySettingsExt implements IDisplaySettingsExt {
    private static final String KEY_CUSTOM_FONT_SIZE = "custom_font_size";
    private static final String TAG = "DefaultDisplaySettingsExt";
    private Context mContext;

    public DefaultDisplaySettingsExt(Context context) {
        this.mContext = context;
    }

    @Override // com.mediatek.settings.ext.IDisplaySettingsExt
    public void addPreference(Context context, PreferenceScreen screen) {
    }

    @Override // com.mediatek.settings.ext.IDisplaySettingsExt
    public void removePreference(Context context, PreferenceScreen screen) {
        Preference customFontSizePref = screen.findPreference(KEY_CUSTOM_FONT_SIZE);
        screen.removePreference(customFontSizePref);
        Log.d(TAG, "removePreference KEY_CUSTOM_FONT_SIZE");
    }

    @Override // com.mediatek.settings.ext.IDisplaySettingsExt
    public boolean isCustomPrefPresent() {
        return false;
    }

    @Override // com.mediatek.settings.ext.IDisplaySettingsExt
    public String[] getFontEntries(String[] defaultStr) {
        return defaultStr;
    }

    @Override // com.mediatek.settings.ext.IDisplaySettingsExt
    public String[] getFontEntryValues(String[] defaultStr) {
        return defaultStr;
    }
}
