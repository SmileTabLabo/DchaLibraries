package com.android.settings.fuelgauge;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.dashboard.conditional.BatterySaverCondition;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settings.notification.SettingPref;
import com.android.settings.widget.SwitchBar;
/* loaded from: classes.dex */
public class BatterySaverSettings extends SettingsPreferenceFragment implements SwitchBar.OnSwitchChangeListener {
    private static final boolean DEBUG = Log.isLoggable("BatterySaverSettings", 3);
    private Context mContext;
    private boolean mCreated;
    private PowerManager mPowerManager;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private SettingPref mTriggerPref;
    private boolean mValidListener;
    private final Handler mHandler = new Handler();
    private final SettingsObserver mSettingsObserver = new SettingsObserver(this.mHandler);
    private final Receiver mReceiver = new Receiver(this, null);
    private final Runnable mUpdateSwitch = new Runnable() { // from class: com.android.settings.fuelgauge.BatterySaverSettings.1
        @Override // java.lang.Runnable
        public void run() {
            BatterySaverSettings.this.updateSwitch();
        }
    };
    private final Runnable mStartMode = new Runnable() { // from class: com.android.settings.fuelgauge.BatterySaverSettings.2
        @Override // java.lang.Runnable
        public void run() {
            AsyncTask.execute(new Runnable() { // from class: com.android.settings.fuelgauge.BatterySaverSettings.2.1
                @Override // java.lang.Runnable
                public void run() {
                    if (BatterySaverSettings.DEBUG) {
                        Log.d("BatterySaverSettings", "Starting low power mode from settings");
                    }
                    BatterySaverSettings.this.trySetPowerSaveMode(true);
                }
            });
        }
    };

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 52;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mCreated) {
            this.mSwitchBar.show();
            return;
        }
        this.mCreated = true;
        addPreferencesFromResource(R.xml.battery_saver_settings);
        this.mContext = getActivity();
        this.mSwitchBar = ((SettingsActivity) this.mContext).getSwitchBar();
        this.mSwitch = this.mSwitchBar.getSwitch();
        this.mSwitchBar.show();
        this.mTriggerPref = new SettingPref(1, "turn_on_automatically", "low_power_trigger_level", 0, getResources().getIntArray(R.array.battery_saver_trigger_values)) { // from class: com.android.settings.fuelgauge.BatterySaverSettings.3
            @Override // com.android.settings.notification.SettingPref
            protected String getCaption(Resources res, int value) {
                if (value > 0 && value < 100) {
                    return res.getString(R.string.battery_saver_turn_on_automatically_pct, Utils.formatPercentage(value));
                }
                return res.getString(R.string.battery_saver_turn_on_automatically_never);
            }
        };
        this.mTriggerPref.init(this);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.hide();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mSettingsObserver.setListening(true);
        this.mReceiver.setListening(true);
        if (!this.mValidListener) {
            this.mSwitchBar.addOnSwitchChangeListener(this);
            this.mValidListener = true;
        }
        updateSwitch();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mSettingsObserver.setListening(false);
        this.mReceiver.setListening(false);
        if (!this.mValidListener) {
            return;
        }
        this.mSwitchBar.removeOnSwitchChangeListener(this);
        this.mValidListener = false;
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        this.mHandler.removeCallbacks(this.mStartMode);
        if (isChecked) {
            this.mHandler.postDelayed(this.mStartMode, 500L);
            return;
        }
        if (DEBUG) {
            Log.d("BatterySaverSettings", "Stopping low power mode from settings");
        }
        trySetPowerSaveMode(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void trySetPowerSaveMode(boolean mode) {
        if (!this.mPowerManager.setPowerSaveMode(mode)) {
            if (DEBUG) {
                Log.d("BatterySaverSettings", "Setting mode failed, fallback to current value");
            }
            this.mHandler.post(this.mUpdateSwitch);
        }
        ((BatterySaverCondition) ConditionManager.get(getContext()).getCondition(BatterySaverCondition.class)).refreshState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSwitch() {
        boolean mode = this.mPowerManager.isPowerSaveMode();
        if (DEBUG) {
            Log.d("BatterySaverSettings", "updateSwitch: isChecked=" + this.mSwitch.isChecked() + " mode=" + mode);
        }
        if (mode == this.mSwitch.isChecked()) {
            return;
        }
        if (this.mValidListener) {
            this.mSwitchBar.removeOnSwitchChangeListener(this);
        }
        this.mSwitch.setChecked(mode);
        if (!this.mValidListener) {
            return;
        }
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    /* loaded from: classes.dex */
    private final class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        /* synthetic */ Receiver(BatterySaverSettings this$0, Receiver receiver) {
            this();
        }

        private Receiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (BatterySaverSettings.DEBUG) {
                Log.d("BatterySaverSettings", "Received " + intent.getAction());
            }
            BatterySaverSettings.this.mHandler.post(BatterySaverSettings.this.mUpdateSwitch);
        }

        public void setListening(boolean listening) {
            if (listening && !this.mRegistered) {
                BatterySaverSettings.this.mContext.registerReceiver(this, new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGING"));
                this.mRegistered = true;
            } else if (listening || !this.mRegistered) {
            } else {
                BatterySaverSettings.this.mContext.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }
    }

    /* loaded from: classes.dex */
    private final class SettingsObserver extends ContentObserver {
        private final Uri LOW_POWER_MODE_TRIGGER_LEVEL_URI;

        public SettingsObserver(Handler handler) {
            super(handler);
            this.LOW_POWER_MODE_TRIGGER_LEVEL_URI = Settings.Global.getUriFor("low_power_trigger_level");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (!this.LOW_POWER_MODE_TRIGGER_LEVEL_URI.equals(uri)) {
                return;
            }
            BatterySaverSettings.this.mTriggerPref.update(BatterySaverSettings.this.mContext);
        }

        public void setListening(boolean listening) {
            ContentResolver cr = BatterySaverSettings.this.getContentResolver();
            if (listening) {
                cr.registerContentObserver(this.LOW_POWER_MODE_TRIGGER_LEVEL_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }
    }
}
