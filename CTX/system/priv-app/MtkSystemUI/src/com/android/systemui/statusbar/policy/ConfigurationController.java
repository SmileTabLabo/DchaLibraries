package com.android.systemui.statusbar.policy;

import android.content.res.Configuration;
/* loaded from: classes.dex */
public interface ConfigurationController extends CallbackController<ConfigurationListener> {

    /* loaded from: classes.dex */
    public interface ConfigurationListener {
        default void onConfigChanged(Configuration configuration) {
        }

        default void onDensityOrFontScaleChanged() {
        }

        default void onOverlayChanged() {
        }

        default void onLocaleListChanged() {
        }
    }
}
