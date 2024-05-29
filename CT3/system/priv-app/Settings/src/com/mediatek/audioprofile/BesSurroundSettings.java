package com.mediatek.audioprofile;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.mediatek.audioprofile.BesSurroundItem;
/* loaded from: classes.dex */
public class BesSurroundSettings extends SettingsPreferenceFragment implements SwitchBar.OnSwitchChangeListener, BesSurroundItem.OnClickListener {
    private AudioManager mAudioManager;
    private Context mContext;
    private boolean mCreated;
    private BesSurroundItem mMovieMode;
    private BesSurroundItem mMusicMode;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private boolean mValidListener;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mCreated) {
            this.mSwitchBar.show();
            return;
        }
        this.mCreated = true;
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.audioprofile_bessurrond_settings);
        this.mContext = getActivity();
        this.mMovieMode = (BesSurroundItem) findPreference("movie_mode");
        this.mMusicMode = (BesSurroundItem) findPreference("music_mode");
        this.mMovieMode.setOnClickListener(this);
        this.mMusicMode.setOnClickListener(this);
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.mSwitchBar = ((SettingsActivity) this.mContext).getSwitchBar();
        this.mSwitch = this.mSwitchBar.getSwitch();
        this.mSwitchBar.show();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.hide();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (!this.mValidListener) {
            this.mSwitchBar.addOnSwitchChangeListener(this);
            this.mValidListener = true;
        }
        initBesSurroundStatus();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        if (!this.mValidListener) {
            return;
        }
        this.mSwitchBar.removeOnSwitchChangeListener(this);
        this.mValidListener = false;
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Log.d("@M_Settings/AudioP", "BesSurroundSettings:onSwitchChanged: " + isChecked);
        SoundEnhancement.setBesSurroundState(this.mAudioManager, isChecked);
        getPreferenceScreen().setEnabled(isChecked);
    }

    private void initBesSurroundStatus() {
        this.mSwitch.setChecked(SoundEnhancement.getBesSurroundState(this.mAudioManager));
        getPreferenceScreen().setEnabled(this.mSwitch.isChecked());
        boolean modeMovie = SoundEnhancement.getBesSurroundMode(this.mAudioManager) == 0;
        this.mMovieMode.setChecked(modeMovie);
        this.mMusicMode.setChecked(modeMovie ? false : true);
    }

    @Override // com.mediatek.audioprofile.BesSurroundItem.OnClickListener
    public void onRadioButtonClicked(BesSurroundItem emiter) {
        if (emiter == this.mMovieMode) {
            SoundEnhancement.setBesSurroundMode(this.mAudioManager, 0);
            this.mMusicMode.setChecked(false);
        } else if (emiter == this.mMusicMode) {
            SoundEnhancement.setBesSurroundMode(this.mAudioManager, 1);
            this.mMovieMode.setChecked(false);
        }
        emiter.setChecked(true);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 100003;
    }
}
