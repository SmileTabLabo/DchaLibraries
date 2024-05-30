package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
/* loaded from: classes.dex */
public class BluetoothA2dpHwOffloadPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    private final DevelopmentSettingsDashboardFragment mFragment;

    public BluetoothA2dpHwOffloadPreferenceController(Context context, DevelopmentSettingsDashboardFragment developmentSettingsDashboardFragment) {
        super(context);
        this.mFragment = developmentSettingsDashboardFragment;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "bluetooth_disable_a2dp_hw_offload";
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        BluetoothA2dpHwOffloadRebootDialog.show(this.mFragment, this);
        return false;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!SystemProperties.getBoolean("ro.bluetooth.a2dp_offload.supported", false)) {
            this.mPreference.setEnabled(false);
            ((SwitchPreference) this.mPreference).setChecked(true);
            return;
        }
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean("persist.bluetooth.a2dp_offload.disabled", false));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.development.DeveloperOptionsPreferenceController
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        if (SystemProperties.getBoolean("ro.bluetooth.a2dp_offload.supported", false)) {
            ((SwitchPreference) this.mPreference).setChecked(false);
            SystemProperties.set("persist.bluetooth.a2dp_offload.disabled", "false");
            return;
        }
        ((SwitchPreference) this.mPreference).setChecked(true);
        SystemProperties.set("persist.bluetooth.a2dp_offload.disabled", "true");
    }

    public void onA2dpHwDialogConfirmed() {
        SystemProperties.set("persist.bluetooth.a2dp_offload.disabled", Boolean.toString(!SystemProperties.getBoolean("persist.bluetooth.a2dp_offload.disabled", false)));
    }
}
