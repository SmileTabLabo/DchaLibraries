package com.android.settings.fuelgauge;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.R;
import com.android.settings.Utils;
/* loaded from: classes.dex */
public class BatterySaverPreference extends Preference {
    private final ContentObserver mObserver;
    private PowerManager mPowerManager;

    public BatterySaverPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mObserver = new ContentObserver(new Handler()) { // from class: com.android.settings.fuelgauge.BatterySaverPreference.1
            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                BatterySaverPreference.this.updateSwitch();
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void performClick(View view) {
        Utils.startWithFragment(getContext(), getFragment(), null, null, 0, 0, getTitle());
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mPowerManager = (PowerManager) getContext().getSystemService("power");
        this.mObserver.onChange(true);
        getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), true, this.mObserver);
        getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("low_power"), true, this.mObserver);
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        super.onDetached();
        getContext().getContentResolver().unregisterContentObserver(this.mObserver);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSwitch() {
        Context context = getContext();
        boolean mode = this.mPowerManager.isPowerSaveMode();
        int format = mode ? R.string.battery_saver_on_summary : R.string.battery_saver_off_summary;
        int percent = Settings.Global.getInt(context.getContentResolver(), "low_power_trigger_level", 0);
        int percentFormat = percent > 0 ? R.string.battery_saver_desc_turn_on_auto_pct : R.string.battery_saver_desc_turn_on_auto_never;
        setSummary(context.getString(format, context.getString(percentFormat, Utils.formatPercentage(percent))));
    }
}
