package com.android.systemui.tuner;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Build;
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
import com.android.systemui.R;
/* loaded from: classes.dex */
public class DemoModeFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String[] STATUS_ICONS = {"volume", "bluetooth", "location", "alarm", "zen", "sync", "tty", "eri", "mute", "speakerphone", "managed_profile"};
    private final ContentObserver mDemoModeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) { // from class: com.android.systemui.tuner.DemoModeFragment.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            DemoModeFragment.this.updateDemoModeEnabled();
            DemoModeFragment.this.updateDemoModeOn();
        }
    };
    private SwitchPreference mEnabledSwitch;
    private SwitchPreference mOnSwitch;

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        Context context = getContext();
        this.mEnabledSwitch = new SwitchPreference(context);
        this.mEnabledSwitch.setTitle(R.string.enable_demo_mode);
        this.mEnabledSwitch.setOnPreferenceChangeListener(this);
        this.mOnSwitch = new SwitchPreference(context);
        this.mOnSwitch.setTitle(R.string.show_demo_mode);
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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            getFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 229, true);
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 229, false);
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        getContext().getContentResolver().unregisterContentObserver(this.mDemoModeObserver);
        super.onDestroy();
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

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v1 */
    /* JADX WARN: Type inference failed for: r5v2, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r5v3 */
    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        ?? r5 = obj == Boolean.TRUE ? 1 : 0;
        if (preference == this.mEnabledSwitch) {
            if (r5 == 0) {
                this.mOnSwitch.setChecked(false);
                stopDemoMode();
            }
            MetricsLogger.action(getContext(), 235, (boolean) r5);
            setGlobal("sysui_demo_allowed", r5);
        } else if (preference != this.mOnSwitch) {
            return false;
        } else {
            MetricsLogger.action(getContext(), 236, (boolean) r5);
            if (r5 != 0) {
                startDemoMode();
            } else {
                stopDemoMode();
            }
        }
        return true;
    }

    private void startDemoMode() {
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "enter");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "clock");
        String str = "1010";
        try {
            str = String.format("%02d00", Integer.valueOf(Integer.valueOf(Build.VERSION.RELEASE.split("\\.")[0]).intValue() % 24));
        } catch (IllegalArgumentException e) {
        }
        intent.putExtra("hhmm", str);
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "network");
        intent.putExtra("wifi", "show");
        intent.putExtra("mobile", "show");
        intent.putExtra("sims", "1");
        intent.putExtra("nosim", "false");
        intent.putExtra("level", "4");
        intent.putExtra("datatype", "lte");
        getContext().sendBroadcast(intent);
        intent.putExtra("fully", "true");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "battery");
        intent.putExtra("level", "100");
        intent.putExtra("plugged", "false");
        getContext().sendBroadcast(intent);
        intent.putExtra("command", "status");
        for (String str2 : STATUS_ICONS) {
            intent.putExtra(str2, "hide");
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

    private void setGlobal(String str, int i) {
        Settings.Global.putInt(getContext().getContentResolver(), str, i);
    }
}
