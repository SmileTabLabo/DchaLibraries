package com.android.settings.wifi;

import android.content.Intent;
import android.support.v14.preference.PreferenceFragment;
import com.android.settings.ButtonBarHandler;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.wifi.p2p.WifiP2pSettings;
/* loaded from: classes.dex */
public class WifiPickerActivity extends SettingsActivity implements ButtonBarHandler {
    @Override // com.android.settings.SettingsActivity, android.app.Activity
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        if (!intent.hasExtra(":settings:show_fragment")) {
            intent.putExtra(":settings:show_fragment", getWifiSettingsClass().getName());
            intent.putExtra(":settings:show_fragment_title_resid", R.string.wifi_select_network);
        }
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.SettingsActivity
    public boolean isValidFragment(String str) {
        if (WifiSettings.class.getName().equals(str) || WifiP2pSettings.class.getName().equals(str) || SavedAccessPointsWifiSettings.class.getName().equals(str)) {
            return true;
        }
        return false;
    }

    Class<? extends PreferenceFragment> getWifiSettingsClass() {
        return WifiSettings.class;
    }
}
