package com.android.settings;

import android.speech.tts.TtsEngines;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
/* loaded from: classes.dex */
public class VoiceInputOutputSettings {
    private final SettingsPreferenceFragment mFragment;
    private PreferenceGroup mParent;
    private final TtsEngines mTtsEngines;
    private Preference mTtsSettingsPref;
    private PreferenceCategory mVoiceCategory;

    public VoiceInputOutputSettings(SettingsPreferenceFragment fragment) {
        this.mFragment = fragment;
        this.mTtsEngines = new TtsEngines(fragment.getPreferenceScreen().getContext());
    }

    public void onCreate() {
        this.mParent = this.mFragment.getPreferenceScreen();
        this.mVoiceCategory = (PreferenceCategory) this.mParent.findPreference("voice_category");
        this.mTtsSettingsPref = this.mVoiceCategory.findPreference("tts_settings");
        populateOrRemovePreferences();
    }

    private void populateOrRemovePreferences() {
        boolean hasTtsPrefs = populateOrRemoveTtsPrefs();
        if (hasTtsPrefs) {
            return;
        }
        this.mFragment.getPreferenceScreen().removePreference(this.mVoiceCategory);
    }

    private boolean populateOrRemoveTtsPrefs() {
        if (this.mTtsEngines.getEngines().isEmpty()) {
            this.mVoiceCategory.removePreference(this.mTtsSettingsPref);
            return false;
        }
        return true;
    }
}
