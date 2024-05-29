package com.android.settings.location;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;
/* loaded from: classes.dex */
public class LocationForWorkPreferenceController extends LocationBasePreferenceController {
    private RestrictedSwitchPreference mPreference;

    public LocationForWorkPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean handlePreferenceTreeClick(Preference preference) {
        if ("managed_profile_location_switch".equals(preference.getKey())) {
            boolean isChecked = this.mPreference.isChecked();
            this.mUserManager.setUserRestriction("no_share_location", !isChecked, Utils.getManagedProfile(this.mUserManager));
            this.mPreference.setSummary(isChecked ? R.string.switch_on_text : R.string.switch_off_text);
            return true;
        }
        return false;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = (RestrictedSwitchPreference) preferenceScreen.findPreference("managed_profile_location_switch");
    }

    @Override // com.android.settings.location.LocationBasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return Utils.getManagedProfile(this.mUserManager) != null;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "managed_profile_location_switch";
    }

    @Override // com.android.settings.location.LocationEnabler.LocationModeChangeListener
    public void onLocationModeChanged(int i, boolean z) {
        if (!this.mPreference.isVisible() || !isAvailable()) {
            return;
        }
        RestrictedLockUtils.EnforcedAdmin shareLocationEnforcedAdmin = this.mLocationEnabler.getShareLocationEnforcedAdmin(Utils.getManagedProfile(this.mUserManager).getIdentifier());
        boolean isManagedProfileRestrictedByBase = this.mLocationEnabler.isManagedProfileRestrictedByBase();
        if (!isManagedProfileRestrictedByBase && shareLocationEnforcedAdmin != null) {
            this.mPreference.setDisabledByAdmin(shareLocationEnforcedAdmin);
            this.mPreference.setChecked(false);
            return;
        }
        boolean isEnabled = this.mLocationEnabler.isEnabled(i);
        this.mPreference.setEnabled(isEnabled);
        int i2 = R.string.switch_off_text;
        if (!isEnabled) {
            this.mPreference.setChecked(false);
        } else {
            this.mPreference.setChecked(!isManagedProfileRestrictedByBase);
            if (!isManagedProfileRestrictedByBase) {
                i2 = R.string.switch_on_text;
            }
        }
        this.mPreference.setSummary(i2);
    }
}
