package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
/* loaded from: classes.dex */
public class DescriptionPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin {
    public DescriptionPreferenceController(Context context) {
        super(context, null);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "desc";
    }

    @Override // com.android.settings.notification.NotificationPreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        if (super.isAvailable()) {
            if (this.mChannel != null || hasValidGroup()) {
                if (this.mChannel == null || TextUtils.isEmpty(this.mChannel.getDescription())) {
                    return hasValidGroup() && !TextUtils.isEmpty(this.mChannelGroup.getDescription());
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        if (this.mAppRow != null) {
            if (this.mChannel != null) {
                preference.setTitle(this.mChannel.getDescription());
            } else if (hasValidGroup()) {
                preference.setTitle(this.mChannelGroup.getDescription());
            }
        }
        preference.setEnabled(false);
        preference.setSelectable(false);
    }
}
