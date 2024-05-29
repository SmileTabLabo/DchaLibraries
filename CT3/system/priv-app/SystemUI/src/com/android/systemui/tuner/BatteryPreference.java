package com.android.systemui.tuner;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.DropDownPreference;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/tuner/BatteryPreference.class */
public class BatteryPreference extends DropDownPreference implements TunerService.Tunable {
    private final String mBattery;
    private boolean mBatteryEnabled;
    private ArraySet<String> mBlacklist;
    private boolean mHasPercentage;
    private boolean mHasSetValue;

    public BatteryPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBattery = context.getString(17039407);
        setEntryValues(new CharSequence[]{"percent", "default", "disabled"});
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        boolean z = true;
        super.onAttached();
        TunerService.get(getContext()).addTunable(this, "icon_blacklist");
        if (Settings.System.getInt(getContext().getContentResolver(), "status_bar_show_battery_percent", 0) == 0) {
            z = false;
        }
        this.mHasPercentage = z;
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            this.mBlacklist = StatusBarIconController.getIconBlacklist(str2);
            this.mBatteryEnabled = !this.mBlacklist.contains(this.mBattery);
        }
        if (this.mHasSetValue) {
            return;
        }
        this.mHasSetValue = true;
        if (this.mBatteryEnabled && this.mHasPercentage) {
            setValue("percent");
        } else if (this.mBatteryEnabled) {
            setValue("default");
        } else {
            setValue("disabled");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public boolean persistString(String str) {
        boolean equals = "percent".equals(str);
        MetricsLogger.action(getContext(), 237, equals);
        Settings.System.putInt(getContext().getContentResolver(), "status_bar_show_battery_percent", equals ? 1 : 0);
        if ("disabled".equals(str)) {
            this.mBlacklist.add(this.mBattery);
        } else {
            this.mBlacklist.remove(this.mBattery);
        }
        TunerService.get(getContext()).setValue("icon_blacklist", TextUtils.join(",", this.mBlacklist));
        return true;
    }
}
