package com.android.settings.notification;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settingslib.core.lifecycle.Lifecycle;
/* loaded from: classes.dex */
public class ZenModeAlarmsPreferenceController extends AbstractZenModePreferenceController implements Preference.OnPreferenceChangeListener {
    public ZenModeAlarmsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, "zen_mode_alarms", lifecycle);
    }

    @Override // com.android.settings.notification.AbstractZenModePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "zen_mode_alarms";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        super.updateState(preference);
        SwitchPreference switchPreference = (SwitchPreference) preference;
        switch (getZenMode()) {
            case 2:
                switchPreference.setEnabled(false);
                switchPreference.setChecked(false);
                return;
            case 3:
                switchPreference.setEnabled(false);
                switchPreference.setChecked(true);
                return;
            default:
                switchPreference.setEnabled(true);
                switchPreference.setChecked(this.mBackend.isPriorityCategoryEnabled(32));
                return;
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean booleanValue = ((Boolean) obj).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            Log.d("PrefControllerMixin", "onPrefChange allowAlarms=" + booleanValue);
        }
        this.mMetricsFeatureProvider.action(this.mContext, 1226, booleanValue);
        this.mBackend.saveSoundPolicy(32, booleanValue);
        return true;
    }
}
