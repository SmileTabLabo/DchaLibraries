package com.android.systemui.statusbar.policy;

import android.net.Uri;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/ZenModeController.class */
public interface ZenModeController {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/ZenModeController$Callback.class */
    public static class Callback {
        public void onConditionsChanged(Condition[] conditionArr) {
        }

        public void onConfigChanged(ZenModeConfig zenModeConfig) {
        }

        public void onEffectsSupressorChanged() {
        }

        public void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
        }

        public void onNextAlarmChanged() {
        }

        public void onZenAvailableChanged(boolean z) {
        }

        public void onZenChanged(int i) {
        }
    }

    void addCallback(Callback callback);

    ZenModeConfig getConfig();

    int getCurrentUser();

    ZenModeConfig.ZenRule getManualRule();

    long getNextAlarm();

    int getZen();

    boolean isCountdownConditionSupported();

    boolean isVolumeRestricted();

    void removeCallback(Callback callback);

    void setUserId(int i);

    void setZen(int i, Uri uri, String str);
}
