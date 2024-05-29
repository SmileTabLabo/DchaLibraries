package com.android.settings.fuelgauge;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.conditional.BatterySaverCondition;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settings.fuelgauge.BatterySaverReceiver;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
/* loaded from: classes.dex */
public class BatterySaverController extends BasePreferenceController implements BatterySaverReceiver.BatterySaverListener, LifecycleObserver, OnStart, OnStop {
    private static final String KEY_BATTERY_SAVER = "battery_saver_summary";
    private Preference mBatterySaverPref;
    private final BatterySaverReceiver mBatteryStateChangeReceiver;
    private final ContentObserver mObserver;
    private final PowerManager mPowerManager;

    public BatterySaverController(Context context) {
        super(context, KEY_BATTERY_SAVER);
        this.mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) { // from class: com.android.settings.fuelgauge.BatterySaverController.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                BatterySaverController.this.updateSummary();
            }
        };
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mBatteryStateChangeReceiver = new BatterySaverReceiver(context);
        this.mBatteryStateChangeReceiver.setBatterySaverListener(this);
    }

    @Override // com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        return 0;
    }

    @Override // com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return KEY_BATTERY_SAVER;
    }

    @Override // com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mBatterySaverPref = preferenceScreen.findPreference(KEY_BATTERY_SAVER);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStart
    public void onStart() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), true, this.mObserver);
        this.mBatteryStateChangeReceiver.setListening(true);
        updateSummary();
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStop
    public void onStop() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        this.mBatteryStateChangeReceiver.setListening(false);
    }

    void refreshConditionManager() {
        ((BatterySaverCondition) ConditionManager.get(this.mContext).getCondition(BatterySaverCondition.class)).refreshState();
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public CharSequence getSummary() {
        boolean isPowerSaveMode = this.mPowerManager.isPowerSaveMode();
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "low_power_trigger_level", 0);
        if (isPowerSaveMode) {
            return this.mContext.getString(R.string.battery_saver_on_summary);
        }
        if (i != 0) {
            return this.mContext.getString(R.string.battery_saver_off_scheduled_summary, Utils.formatPercentage(i));
        }
        return this.mContext.getString(R.string.battery_saver_off_summary);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSummary() {
        this.mBatterySaverPref.setSummary(getSummary());
    }

    @Override // com.android.settings.fuelgauge.BatterySaverReceiver.BatterySaverListener
    public void onPowerSaveModeChanged() {
        updateSummary();
    }

    @Override // com.android.settings.fuelgauge.BatterySaverReceiver.BatterySaverListener
    public void onBatteryChanged(boolean z) {
    }
}
