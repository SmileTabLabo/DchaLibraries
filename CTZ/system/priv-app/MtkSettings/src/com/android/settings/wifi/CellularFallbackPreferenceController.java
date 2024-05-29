package com.android.settings.wifi;

import android.content.Context;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
/* loaded from: classes.dex */
public class CellularFallbackPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    public CellularFallbackPreferenceController(Context context) {
        super(context);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return !avoidBadWifiConfig();
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "wifi_cellular_data_fallback";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), "wifi_cellular_data_fallback") && (preference instanceof SwitchPreference)) {
            Settings.Global.putString(this.mContext.getContentResolver(), "network_avoid_bad_wifi", ((SwitchPreference) preference).isChecked() ? "1" : null);
            return true;
        }
        return false;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        boolean avoidBadWifiCurrentSettings = avoidBadWifiCurrentSettings();
        if (preference != null) {
            ((SwitchPreference) preference).setChecked(avoidBadWifiCurrentSettings);
        }
    }

    private boolean avoidBadWifiConfig() {
        return this.mContext.getResources().getInteger(17694825) == 1;
    }

    private boolean avoidBadWifiCurrentSettings() {
        return "1".equals(Settings.Global.getString(this.mContext.getContentResolver(), "network_avoid_bad_wifi"));
    }
}
