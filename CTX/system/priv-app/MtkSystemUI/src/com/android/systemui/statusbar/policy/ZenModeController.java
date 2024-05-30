package com.android.systemui.statusbar.policy;

import android.net.Uri;
import android.service.notification.ZenModeConfig;
/* loaded from: classes.dex */
public interface ZenModeController extends CallbackController<Callback> {
    boolean areNotificationsHiddenInShade();

    ZenModeConfig getConfig();

    ZenModeConfig.ZenRule getManualRule();

    long getNextAlarm();

    int getZen();

    boolean isVolumeRestricted();

    void setZen(int i, Uri uri, String str);

    /* loaded from: classes.dex */
    public interface Callback {
        default void onZenChanged(int i) {
        }

        default void onNextAlarmChanged() {
        }

        default void onZenAvailableChanged(boolean z) {
        }

        default void onEffectsSupressorChanged() {
        }

        default void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
        }

        default void onConfigChanged(ZenModeConfig zenModeConfig) {
        }
    }
}
