package com.android.settings.development;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
/* loaded from: classes.dex */
public class TouchPanelVersionPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin, OnActivityResultListener {
    public TouchPanelVersionPreferenceController(Context context) {
        super(context);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "touch_panel_ver";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        updatePreferenceSummary();
    }

    @Override // com.android.settings.development.OnActivityResultListener
    public boolean onActivityResult(int i, int i2, Intent intent) {
        updatePreferenceSummary();
        return true;
    }

    Intent getActivityStartIntent() {
        return new Intent(this.mContext, AppPicker.class);
    }

    private void updatePreferenceSummary() {
        String string = Settings.System.getString(null, "bc:touchpanel:nvt:fw_version");
        if (string == null || string.equals("")) {
            Settings.System.getString(null, "bc:touchpanel:fts:fw_version");
            if (this.mPreference.getSummary() == null) {
                this.mPreference.setSummary("unknown");
                return;
            }
            return;
        }
        this.mPreference.setSummary(string);
    }
}
