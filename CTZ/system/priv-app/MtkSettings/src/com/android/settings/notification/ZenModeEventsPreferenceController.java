package com.android.settings.notification;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settingslib.core.lifecycle.Lifecycle;
/* loaded from: classes.dex */
public class ZenModeEventsPreferenceController extends AbstractZenModePreferenceController implements Preference.OnPreferenceChangeListener {
    public ZenModeEventsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, "zen_mode_events", lifecycle);
    }

    @Override // com.android.settings.notification.AbstractZenModePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "zen_mode_events";
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
            case 3:
                switchPreference.setEnabled(false);
                switchPreference.setChecked(false);
                return;
            default:
                switchPreference.setChecked(this.mBackend.isPriorityCategoryEnabled(2));
                switchPreference.setEnabled(true);
                return;
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean booleanValue = ((Boolean) obj).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            Log.d("PrefControllerMixin", "onPrefChange allowEvents=" + booleanValue);
        }
        this.mMetricsFeatureProvider.action(this.mContext, 168, booleanValue);
        this.mBackend.saveSoundPolicy(2, booleanValue);
        return true;
    }
}
