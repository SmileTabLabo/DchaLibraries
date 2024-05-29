package com.android.systemui.tuner;

import android.content.Context;
import android.support.v7.preference.DropDownPreference;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/tuner/ClockPreference.class */
public class ClockPreference extends DropDownPreference implements TunerService.Tunable {
    private ArraySet<String> mBlacklist;
    private final String mClock;
    private boolean mClockEnabled;
    private boolean mHasSeconds;
    private boolean mHasSetValue;

    public ClockPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClock = context.getString(17039410);
        setEntryValues(new CharSequence[]{"seconds", "default", "disabled"});
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        TunerService.get(getContext()).addTunable(this, "icon_blacklist", "clock_seconds");
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        boolean z = false;
        if ("icon_blacklist".equals(str)) {
            this.mBlacklist = StatusBarIconController.getIconBlacklist(str2);
            if (!this.mBlacklist.contains(this.mClock)) {
                z = true;
            }
            this.mClockEnabled = z;
        } else if ("clock_seconds".equals(str)) {
            boolean z2 = false;
            if (str2 != null) {
                z2 = false;
                if (Integer.parseInt(str2) != 0) {
                    z2 = true;
                }
            }
            this.mHasSeconds = z2;
        }
        if (this.mHasSetValue) {
            return;
        }
        this.mHasSetValue = true;
        if (this.mClockEnabled && this.mHasSeconds) {
            setValue("seconds");
        } else if (this.mClockEnabled) {
            setValue("default");
        } else {
            setValue("disabled");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public boolean persistString(String str) {
        TunerService.get(getContext()).setValue("clock_seconds", "seconds".equals(str) ? 1 : 0);
        if ("disabled".equals(str)) {
            this.mBlacklist.add(this.mClock);
        } else {
            this.mBlacklist.remove(this.mClock);
        }
        TunerService.get(getContext()).setValue("icon_blacklist", TextUtils.join(",", this.mBlacklist));
        return true;
    }
}
