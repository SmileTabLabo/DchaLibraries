package com.android.browser;

import android.content.Context;
import android.util.AttributeSet;
import com.android.internal.preference.YesNoPreference;
/* loaded from: classes.dex */
class BrowserYesNoPreference extends YesNoPreference {
    public BrowserYesNoPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void onDialogClosed(boolean z) {
        super.onDialogClosed(z);
        if (z) {
            setEnabled(false);
            BrowserSettings browserSettings = BrowserSettings.getInstance();
            if ("privacy_clear_cache".equals(getKey())) {
                browserSettings.clearCache();
                browserSettings.clearDatabases();
            } else if ("privacy_clear_cookies".equals(getKey())) {
                browserSettings.clearCookies();
            } else if ("privacy_clear_history".equals(getKey())) {
                browserSettings.clearHistory();
            } else if ("privacy_clear_form_data".equals(getKey())) {
                browserSettings.clearFormData();
            } else if ("privacy_clear_passwords".equals(getKey())) {
                browserSettings.clearPasswords();
            } else if ("reset_default_preferences".equals(getKey())) {
                browserSettings.resetDefaultPreferences();
                setEnabled(true);
            } else if ("privacy_clear_geolocation_access".equals(getKey())) {
                browserSettings.clearLocationAccess();
            }
        }
    }
}
