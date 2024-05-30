package com.android.settingslib.core;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
/* loaded from: classes.dex */
public abstract class AbstractPreferenceController {
    protected final Context mContext;

    public abstract String getPreferenceKey();

    public abstract boolean isAvailable();

    public AbstractPreferenceController(Context context) {
        this.mContext = context;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        String preferenceKey = getPreferenceKey();
        if (isAvailable()) {
            setVisible(preferenceScreen, preferenceKey, true);
            if (this instanceof Preference.OnPreferenceChangeListener) {
                preferenceScreen.findPreference(preferenceKey).setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) this);
                return;
            }
            return;
        }
        setVisible(preferenceScreen, preferenceKey, false);
    }

    public void updateState(Preference preference) {
        refreshSummary(preference);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void refreshSummary(Preference preference) {
        CharSequence summary;
        if (preference == null || (summary = getSummary()) == null) {
            return;
        }
        preference.setSummary(summary);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void setVisible(PreferenceGroup preferenceGroup, String str, boolean z) {
        Preference findPreference = preferenceGroup.findPreference(str);
        if (findPreference != null) {
            findPreference.setVisible(z);
        }
    }

    public CharSequence getSummary() {
        return null;
    }
}
