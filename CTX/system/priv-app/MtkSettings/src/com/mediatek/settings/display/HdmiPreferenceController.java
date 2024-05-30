package com.mediatek.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.mediatek.hdmi.HdimReflectionHelper;
/* loaded from: classes.dex */
public class HdmiPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private Object mHdmiManager;

    public HdmiPreferenceController(Context context) {
        super(context);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        this.mHdmiManager = HdimReflectionHelper.getHdmiService();
        return this.mHdmiManager != null;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "hdmi_settings";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        if (!isAvailable()) {
            setVisible(preferenceScreen, getPreferenceKey(), false);
            return;
        }
        Preference findPreference = preferenceScreen.findPreference(getPreferenceKey());
        if (this.mHdmiManager != null) {
            String string = this.mContext.getString(R.string.hdmi_replace_hdmi);
            int hdmiDisplayTypeConstant = HdimReflectionHelper.getHdmiDisplayTypeConstant("DISPLAY_TYPE_MHL");
            int hdmiDisplayTypeConstant2 = HdimReflectionHelper.getHdmiDisplayTypeConstant("DISPLAY_TYPE_SLIMPORT");
            int hdmiDisplayType = HdimReflectionHelper.getHdmiDisplayType(this.mHdmiManager);
            if (hdmiDisplayType == hdmiDisplayTypeConstant) {
                String string2 = this.mContext.getString(R.string.hdmi_replace_mhl);
                findPreference.setTitle(findPreference.getTitle().toString().replaceAll(string, string2));
                findPreference.setSummary(findPreference.getSummary().toString().replaceAll(string, string2));
            } else if (hdmiDisplayType == hdmiDisplayTypeConstant2) {
                String string3 = this.mContext.getString(R.string.slimport_replace_hdmi);
                findPreference.setTitle(findPreference.getTitle().toString().replaceAll(string, string3));
                findPreference.setSummary(findPreference.getSummary().toString().replaceAll(string, string3));
            }
        }
    }
}
