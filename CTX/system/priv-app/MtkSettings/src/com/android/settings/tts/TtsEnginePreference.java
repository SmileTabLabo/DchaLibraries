package com.android.settings.tts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.speech.tts.TextToSpeech;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
/* loaded from: classes.dex */
public class TtsEnginePreference extends Preference {
    private final TextToSpeech.EngineInfo mEngineInfo;
    private volatile boolean mPreventRadioButtonCallbacks;
    private RadioButton mRadioButton;
    private final CompoundButton.OnCheckedChangeListener mRadioChangeListener;
    private final RadioButtonGroupState mSharedState;

    /* loaded from: classes.dex */
    public interface RadioButtonGroupState {
        Checkable getCurrentChecked();

        String getCurrentKey();

        void setCurrentChecked(Checkable checkable);

        void setCurrentKey(String str);
    }

    public TtsEnginePreference(Context context, TextToSpeech.EngineInfo engineInfo, RadioButtonGroupState radioButtonGroupState, SettingsActivity settingsActivity) {
        super(context);
        this.mRadioChangeListener = new CompoundButton.OnCheckedChangeListener() { // from class: com.android.settings.tts.TtsEnginePreference.1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                TtsEnginePreference.this.onRadioButtonClicked(compoundButton, z);
            }
        };
        setLayoutResource(R.layout.preference_tts_engine);
        this.mSharedState = radioButtonGroupState;
        this.mEngineInfo = engineInfo;
        this.mPreventRadioButtonCallbacks = false;
        setKey(this.mEngineInfo.name);
        setTitle(this.mEngineInfo.label);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        if (this.mSharedState == null) {
            throw new IllegalStateException("Call to getView() before a call tosetSharedState()");
        }
        RadioButton radioButton = (RadioButton) preferenceViewHolder.findViewById(R.id.tts_engine_radiobutton);
        radioButton.setOnCheckedChangeListener(this.mRadioChangeListener);
        radioButton.setText(this.mEngineInfo.label);
        boolean equals = getKey().equals(this.mSharedState.getCurrentKey());
        if (equals) {
            this.mSharedState.setCurrentChecked(radioButton);
        }
        this.mPreventRadioButtonCallbacks = true;
        radioButton.setChecked(equals);
        this.mPreventRadioButtonCallbacks = false;
        this.mRadioButton = radioButton;
    }

    private boolean shouldDisplayDataAlert() {
        return !this.mEngineInfo.system;
    }

    private void displayDataAlert(DialogInterface.OnClickListener onClickListener, DialogInterface.OnClickListener onClickListener2) {
        Log.i("TtsEnginePreference", "Displaying data alert for :" + this.mEngineInfo.name);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(17039380).setMessage(getContext().getString(R.string.tts_engine_security_warning, this.mEngineInfo.label)).setCancelable(true).setPositiveButton(17039370, onClickListener).setNegativeButton(17039360, onClickListener2);
        builder.create().show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRadioButtonClicked(final CompoundButton compoundButton, boolean z) {
        if (!this.mPreventRadioButtonCallbacks && this.mSharedState.getCurrentChecked() != compoundButton && z) {
            if (shouldDisplayDataAlert()) {
                displayDataAlert(new DialogInterface.OnClickListener() { // from class: com.android.settings.tts.TtsEnginePreference.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TtsEnginePreference.this.makeCurrentEngine(compoundButton);
                    }
                }, new DialogInterface.OnClickListener() { // from class: com.android.settings.tts.TtsEnginePreference.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        compoundButton.setChecked(false);
                    }
                });
            } else {
                makeCurrentEngine(compoundButton);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void makeCurrentEngine(Checkable checkable) {
        if (this.mSharedState.getCurrentChecked() != null) {
            this.mSharedState.getCurrentChecked().setChecked(false);
        }
        this.mSharedState.setCurrentChecked(checkable);
        this.mSharedState.setCurrentKey(getKey());
        callChangeListener(this.mSharedState.getCurrentKey());
    }
}
