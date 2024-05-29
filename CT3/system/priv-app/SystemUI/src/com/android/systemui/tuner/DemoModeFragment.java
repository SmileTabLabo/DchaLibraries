package com.android.systemui.tuner;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.MenuItem;
import com.android.internal.logging.MetricsLogger;
/* loaded from: a.zip:com/android/systemui/tuner/DemoModeFragment.class */
public class DemoModeFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String[] STATUS_ICONS = {"volume", "bluetooth", "location", "alarm", "zen", "sync", "tty", "eri", "mute", "speakerphone", "managed_profile"};
    private final ContentObserver mDemoModeObserver = new ContentObserver(this, new Handler(Looper.getMainLooper())) { // from class: com.android.systemui.tuner.DemoModeFragment.1
        final DemoModeFragment this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.updateDemoModeEnabled();
            this.this$0.updateDemoModeOn();
        }
    };
    private SwitchPreference mEnabledSwitch;
    private SwitchPreference mOnSwitch;

    private void setGlobal(String str, int i) {
        Settings.Global.putInt(getContext().getContentResolver(), str, i);
    }

    private void startDemoMode() {
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "enter");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "clock");
        intent.putExtra("hhmm", "0700");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "network");
        intent.putExtra("wifi", "show");
        intent.putExtra("mobile", "show");
        intent.putExtra("sims", "1");
        intent.putExtra("nosim", "false");
        intent.putExtra("level", "4");
        intent.putExtra("datatypel", "");
        getContext().sendBroadcast(intent);
        intent.putExtra("fully", "true");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "battery");
        intent.putExtra("level", "100");
        intent.putExtra("plugged", "false");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "status");
        for (String str : STATUS_ICONS) {
            intent.putExtra(str, "hide");
        }
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "notifications");
        intent.putExtra("visible", "false");
        getContext().sendBroadcast(intent);
        setGlobal("sysui_tuner_demo_on", 1);
    }

    private void stopDemoMode() {
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "exit");
        getContext().sendBroadcast(intent);
        setGlobal("sysui_tuner_demo_on", 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDemoModeEnabled() {
        boolean z = Settings.Global.getInt(getContext().getContentResolver(), "sysui_demo_allowed", 0) != 0;
        this.mEnabledSwitch.setChecked(z);
        this.mOnSwitch.setEnabled(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDemoModeOn() {
        this.mOnSwitch.setChecked(Settings.Global.getInt(getContext().getContentResolver(), "sysui_tuner_demo_on", 0) != 0);
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        Context context = getContext();
        this.mEnabledSwitch = new SwitchPreference(context);
        this.mEnabledSwitch.setTitle(2131493715);
        this.mEnabledSwitch.setOnPreferenceChangeListener(this);
        this.mOnSwitch = new SwitchPreference(context);
        this.mOnSwitch.setTitle(2131493716);
        this.mOnSwitch.setEnabled(false);
        this.mOnSwitch.setOnPreferenceChangeListener(this);
        PreferenceScreen createPreferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        createPreferenceScreen.addPreference(this.mEnabledSwitch);
        createPreferenceScreen.addPreference(this.mOnSwitch);
        setPreferenceScreen(createPreferenceScreen);
        updateDemoModeEnabled();
        updateDemoModeOn();
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("sysui_demo_allowed"), false, this.mDemoModeObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor("sysui_tuner_demo_on"), false, this.mDemoModeObserver);
        setHasOptionsMenu(true);
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        getContext().getContentResolver().unregisterContentObserver(this.mDemoModeObserver);
        super.onDestroy();
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 16908332:
                getFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 229, false);
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        int i = 0;
        boolean z = obj == Boolean.TRUE;
        if (preference == this.mEnabledSwitch) {
            if (!z) {
                this.mOnSwitch.setChecked(false);
                stopDemoMode();
            }
            MetricsLogger.action(getContext(), 235, z);
            if (z) {
                i = 1;
            }
            setGlobal("sysui_demo_allowed", i);
            return true;
        } else if (preference == this.mOnSwitch) {
            MetricsLogger.action(getContext(), 236, z);
            if (z) {
                startDemoMode();
                return true;
            }
            stopDemoMode();
            return true;
        } else {
            return false;
        }
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 229, true);
    }
}
