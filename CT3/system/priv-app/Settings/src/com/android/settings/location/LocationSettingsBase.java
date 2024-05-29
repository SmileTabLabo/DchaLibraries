package com.android.settings.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
/* loaded from: classes.dex */
public abstract class LocationSettingsBase extends SettingsPreferenceFragment {
    private boolean mActive = false;
    private int mCurrentMode;
    private BroadcastReceiver mReceiver;

    public abstract void onModeChanged(int i, boolean z);

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.settings.location.LocationSettingsBase.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable("LocationSettingsBase", 3)) {
                    Log.d("LocationSettingsBase", "Received location mode change intent: " + intent);
                }
                LocationSettingsBase.this.refreshLocationMode();
            }
        };
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mActive = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.location.MODE_CHANGED");
        getActivity().registerReceiver(this.mReceiver, filter);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        try {
            getActivity().unregisterReceiver(this.mReceiver);
        } catch (RuntimeException e) {
        }
        super.onPause();
        this.mActive = false;
    }

    private boolean isRestricted() {
        UserManager um = (UserManager) getActivity().getSystemService("user");
        return um.hasUserRestriction("no_share_location");
    }

    public void setLocationMode(int mode) {
        if (isRestricted()) {
            if (Log.isLoggable("LocationSettingsBase", 4)) {
                Log.i("LocationSettingsBase", "Restricted user, not setting location mode");
            }
            int mode2 = Settings.Secure.getInt(getContentResolver(), "location_mode", 0);
            if (this.mActive) {
                onModeChanged(mode2, true);
                return;
            }
            return;
        }
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        intent.putExtra("CURRENT_MODE", this.mCurrentMode);
        intent.putExtra("NEW_MODE", mode);
        getActivity().sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
        Settings.Secure.putInt(getContentResolver(), "location_mode", mode);
        refreshLocationMode();
    }

    public void refreshLocationMode() {
        if (!this.mActive) {
            return;
        }
        int mode = Settings.Secure.getInt(getContentResolver(), "location_mode", 0);
        this.mCurrentMode = mode;
        if (Log.isLoggable("LocationSettingsBase", 4)) {
            Log.i("LocationSettingsBase", "Location mode has been changed");
        }
        onModeChanged(mode, isRestricted());
    }
}
