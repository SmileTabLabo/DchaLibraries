package com.android.settings.dashboard;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class DashboardTilePlaceholderPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private int mOrder;

    public DashboardTilePlaceholderPreferenceController(Context context) {
        super(context);
        this.mOrder = Preference.DEFAULT_ORDER;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        Preference findPreference = preferenceScreen.findPreference(getPreferenceKey());
        if (findPreference != null) {
            this.mOrder = findPreference.getOrder();
            preferenceScreen.removePreference(findPreference);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return false;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "dashboard_tile_placeholder";
    }

    public int getOrder() {
        return this.mOrder;
    }
}
