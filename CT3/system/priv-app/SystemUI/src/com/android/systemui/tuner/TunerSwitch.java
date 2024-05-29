package com.android.systemui.tuner;

import android.content.Context;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.util.AttributeSet;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R$styleable;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/tuner/TunerSwitch.class */
public class TunerSwitch extends SwitchPreference implements TunerService.Tunable {
    private final int mAction;
    private final boolean mDefault;

    public TunerSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.TunerSwitch);
        this.mDefault = obtainStyledAttributes.getBoolean(0, false);
        this.mAction = obtainStyledAttributes.getInt(1, -1);
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        TunerService.get(getContext()).addTunable(this, getKey().split(","));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.TwoStatePreference, android.support.v7.preference.Preference
    public void onClick() {
        super.onClick();
        if (this.mAction != -1) {
            MetricsLogger.action(getContext(), this.mAction, isChecked());
        }
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        boolean z = false;
        if (str2 == null) {
            z = this.mDefault;
        } else if (Integer.parseInt(str2) != 0) {
            z = true;
        }
        setChecked(z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public boolean persistBoolean(boolean z) {
        for (String str : getKey().split(",")) {
            Settings.Secure.putString(getContext().getContentResolver(), str, z ? "1" : "0");
        }
        return true;
    }
}
