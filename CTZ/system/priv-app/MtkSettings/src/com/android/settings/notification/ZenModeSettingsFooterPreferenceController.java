package com.android.settings.notification;

import android.content.Context;
import android.net.Uri;
import android.service.notification.ZenModeConfig;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;
/* loaded from: classes.dex */
public class ZenModeSettingsFooterPreferenceController extends AbstractZenModePreferenceController {
    public ZenModeSettingsFooterPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, "footer_preference", lifecycle);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        switch (getZenMode()) {
            case 1:
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    @Override // com.android.settings.notification.AbstractZenModePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "footer_preference";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        super.updateState(preference);
        boolean isAvailable = isAvailable();
        preference.setVisible(isAvailable);
        if (isAvailable) {
            preference.setTitle(getFooterText());
        }
    }

    protected String getFooterText() {
        ZenModeConfig zenModeConfig = getZenModeConfig();
        String str = "";
        long j = -1;
        if (zenModeConfig.manualRule != null) {
            Uri uri = zenModeConfig.manualRule.conditionId;
            if (zenModeConfig.manualRule.enabler != null) {
                String ownerCaption = mZenModeConfigWrapper.getOwnerCaption(zenModeConfig.manualRule.enabler);
                if (!ownerCaption.isEmpty()) {
                    str = this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule_app, ownerCaption);
                }
            } else if (uri == null) {
                return this.mContext.getString(R.string.zen_mode_settings_dnd_manual_indefinite);
            } else {
                j = mZenModeConfigWrapper.parseManualRuleTime(uri);
                if (j > 0) {
                    str = this.mContext.getString(R.string.zen_mode_settings_dnd_manual_end_time, mZenModeConfigWrapper.getFormattedTime(j, this.mContext.getUserId()));
                }
            }
        }
        for (ZenModeConfig.ZenRule zenRule : zenModeConfig.automaticRules.values()) {
            if (zenRule.isAutomaticActive()) {
                if (!mZenModeConfigWrapper.isTimeRule(zenRule.conditionId)) {
                    return this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule, zenRule.name);
                }
                long parseAutomaticRuleEndTime = mZenModeConfigWrapper.parseAutomaticRuleEndTime(zenRule.conditionId);
                if (parseAutomaticRuleEndTime > j) {
                    str = this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule, zenRule.name);
                    j = parseAutomaticRuleEndTime;
                }
            }
        }
        return str;
    }
}
