package com.android.settings.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.notification.ZenCustomRadioButtonPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;
/* loaded from: classes.dex */
public class ZenModeVisEffectsCustomPreferenceController extends AbstractZenModePreferenceController {
    private final String KEY;
    private ZenCustomRadioButtonPreference mPreference;

    public ZenModeVisEffectsCustomPreferenceController(Context context, Lifecycle lifecycle, String str) {
        super(context, str, lifecycle);
        this.KEY = str;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return true;
    }

    @Override // com.android.settings.notification.AbstractZenModePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = (ZenCustomRadioButtonPreference) preferenceScreen.findPreference(this.KEY);
        this.mPreference.setOnGearClickListener(new ZenCustomRadioButtonPreference.OnGearClickListener() { // from class: com.android.settings.notification.-$$Lambda$ZenModeVisEffectsCustomPreferenceController$hYHNs4-TKsGpjPSCluD3oYAyplI
            @Override // com.android.settings.notification.ZenCustomRadioButtonPreference.OnGearClickListener
            public final void onGearClick(ZenCustomRadioButtonPreference zenCustomRadioButtonPreference) {
                ZenModeVisEffectsCustomPreferenceController.this.launchCustomSettings();
            }
        });
        this.mPreference.setOnRadioButtonClickListener(new ZenCustomRadioButtonPreference.OnRadioButtonClickListener() { // from class: com.android.settings.notification.-$$Lambda$ZenModeVisEffectsCustomPreferenceController$anmhCczZGnQRUAoXVehKNMc66b4
            @Override // com.android.settings.notification.ZenCustomRadioButtonPreference.OnRadioButtonClickListener
            public final void onRadioButtonClick(ZenCustomRadioButtonPreference zenCustomRadioButtonPreference) {
                ZenModeVisEffectsCustomPreferenceController.this.launchCustomSettings();
            }
        });
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference.setChecked(areCustomOptionsSelected());
    }

    protected boolean areCustomOptionsSelected() {
        return (NotificationManager.Policy.areAllVisualEffectsSuppressed(this.mBackend.mPolicy.suppressedVisualEffects) || (this.mBackend.mPolicy.suppressedVisualEffects == 0)) ? false : true;
    }

    protected void select() {
        this.mMetricsFeatureProvider.action(this.mContext, 1399, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchCustomSettings() {
        select();
        new SubSettingLauncher(this.mContext).setDestination(ZenModeBlockedEffectsSettings.class.getName()).setTitle(R.string.zen_mode_what_to_block_title).setSourceMetricsCategory(1400).launch();
    }
}
