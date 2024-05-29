package com.mediatek.settings.ext;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.ims.ImsManager;
/* loaded from: classes.dex */
public class DefaultWfcSettingsExt implements IWfcSettingsExt {
    public static final int CONFIG_CHANGE = 4;
    public static final int CREATE = 2;
    public static final int DESTROY = 3;
    public static final int PAUSE = 1;
    public static final int RESUME = 0;
    private static final String TAG = "DefaultWfcSettingsExt";

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public void initPlugin(PreferenceFragment pf) {
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public String getWfcSummary(Context context, int defaultSummaryResId) {
        return context.getResources().getString(defaultSummaryResId);
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public void onWirelessSettingsEvent(int event) {
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public void onWfcSettingsEvent(int event) {
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public void addOtherCustomPreference() {
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public void updateWfcModePreference(PreferenceScreen root, ListPreference wfcModePref, boolean wfcEnabled, int wfcMode) {
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public boolean showWfcTetheringAlertDialog(Context context) {
        return false;
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public void customizedWfcPreference(Context context, PreferenceScreen preferenceScreen) {
    }

    @Override // com.mediatek.settings.ext.IWfcSettingsExt
    public boolean isWifiCallingProvisioned(Context context, int phoneId) {
        Log.d(TAG, "in default: isWifiCallingProvisioned");
        return true;
    }

    private int getWfcModeSummary(Context context, int wfcMode) {
        if (!ImsManager.isWfcEnabledByUser(context)) {
            return 17039591;
        }
        switch (wfcMode) {
            case 0:
                return 17039594;
            case 1:
                return 17039593;
            case 2:
                return 17039592;
            default:
                Log.e(TAG, "Unexpected WFC mode value: " + wfcMode);
                return 17039591;
        }
    }
}
