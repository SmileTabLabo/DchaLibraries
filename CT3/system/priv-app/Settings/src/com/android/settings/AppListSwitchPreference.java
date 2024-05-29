package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
/* loaded from: classes.dex */
public class AppListSwitchPreference extends AppListPreference {
    private Checkable mSwitch;

    public AppListSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs, 0, R.style.AppListSwitchPreference);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSwitch = (Checkable) view.findViewById(16908352);
        this.mSwitch.setChecked(getValue() != null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.DialogPreference, android.support.v7.preference.Preference
    public void onClick() {
        if (getValue() != null) {
            if (!callChangeListener(null)) {
                return;
            }
            setValue(null);
        } else if (getEntryValues() == null || getEntryValues().length == 0) {
            Log.e("AppListSwitchPref", "Attempting to show dialog with zero entries: " + getKey());
        } else if (getEntryValues().length == 1) {
            String value = getEntryValues()[0].toString();
            if (!callChangeListener(value)) {
                return;
            }
            setValue(value);
        } else {
            super.onClick();
        }
    }

    @Override // android.support.v7.preference.ListPreference
    public void setValue(String value) {
        super.setValue(value);
        if (this.mSwitch == null) {
            return;
        }
        this.mSwitch.setChecked(value != null);
    }
}
