package com.android.settings.tts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
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
    private final SettingsActivity mSettingsActivity;
    private View mSettingsIcon;
    private final RadioButtonGroupState mSharedState;
    private Intent mVoiceCheckData;

    /* loaded from: classes.dex */
    public interface RadioButtonGroupState {
        Checkable getCurrentChecked();

        String getCurrentKey();

        void setCurrentChecked(Checkable checkable);

        void setCurrentKey(String str);
    }

    public TtsEnginePreference(Context context, TextToSpeech.EngineInfo info, RadioButtonGroupState state, SettingsActivity prefActivity) {
        super(context);
        this.mRadioChangeListener = new CompoundButton.OnCheckedChangeListener() { // from class: com.android.settings.tts.TtsEnginePreference.1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TtsEnginePreference.this.onRadioButtonClicked(buttonView, isChecked);
            }
        };
        setLayoutResource(R.layout.preference_tts_engine);
        this.mSharedState = state;
        this.mSettingsActivity = prefActivity;
        this.mEngineInfo = info;
        this.mPreventRadioButtonCallbacks = false;
        setKey(this.mEngineInfo.name);
        setTitle(this.mEngineInfo.label);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        boolean z = true;
        super.onBindViewHolder(view);
        if (this.mSharedState == null) {
            throw new IllegalStateException("Call to getView() before a call tosetSharedState()");
        }
        RadioButton rb = (RadioButton) view.findViewById(R.id.tts_engine_radiobutton);
        rb.setOnCheckedChangeListener(this.mRadioChangeListener);
        rb.setText(this.mEngineInfo.label);
        boolean isChecked = getKey().equals(this.mSharedState.getCurrentKey());
        if (isChecked) {
            this.mSharedState.setCurrentChecked(rb);
        }
        this.mPreventRadioButtonCallbacks = true;
        rb.setChecked(isChecked);
        this.mPreventRadioButtonCallbacks = false;
        this.mRadioButton = rb;
        this.mSettingsIcon = view.findViewById(R.id.tts_engine_settings);
        View view2 = this.mSettingsIcon;
        if (!isChecked || this.mVoiceCheckData == null) {
            z = false;
        }
        view2.setEnabled(z);
        if (!isChecked) {
            this.mSettingsIcon.setAlpha(0.4f);
        }
        this.mSettingsIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.tts.TtsEnginePreference.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("name", TtsEnginePreference.this.mEngineInfo.name);
                args.putString("label", TtsEnginePreference.this.mEngineInfo.label);
                if (TtsEnginePreference.this.mVoiceCheckData != null) {
                    args.putParcelable("voices", TtsEnginePreference.this.mVoiceCheckData);
                }
                TtsEnginePreference.this.mSettingsActivity.startPreferencePanel(TtsEngineSettingsFragment.class.getName(), args, 0, TtsEnginePreference.this.mEngineInfo.label, null, 0);
            }
        });
        if (this.mVoiceCheckData == null) {
            return;
        }
        this.mSettingsIcon.setEnabled(this.mRadioButton.isChecked());
    }

    public void setVoiceDataDetails(Intent data) {
        this.mVoiceCheckData = data;
        if (this.mSettingsIcon == null || this.mRadioButton == null) {
            return;
        }
        if (this.mRadioButton.isChecked()) {
            this.mSettingsIcon.setEnabled(true);
            return;
        }
        this.mSettingsIcon.setEnabled(false);
        this.mSettingsIcon.setAlpha(0.4f);
    }

    private boolean shouldDisplayDataAlert() {
        return !this.mEngineInfo.system;
    }

    private void displayDataAlert(DialogInterface.OnClickListener positiveOnClickListener, DialogInterface.OnClickListener negativeOnClickListener) {
        Log.i("TtsEnginePreference", "Displaying data alert for :" + this.mEngineInfo.name);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(17039380).setMessage(getContext().getString(R.string.tts_engine_security_warning, this.mEngineInfo.label)).setCancelable(true).setPositiveButton(17039370, positiveOnClickListener).setNegativeButton(17039360, negativeOnClickListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRadioButtonClicked(final CompoundButton buttonView, boolean isChecked) {
        if (this.mPreventRadioButtonCallbacks || this.mSharedState.getCurrentChecked() == buttonView) {
            return;
        }
        if (isChecked) {
            if (shouldDisplayDataAlert()) {
                displayDataAlert(new DialogInterface.OnClickListener() { // from class: com.android.settings.tts.TtsEnginePreference.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        TtsEnginePreference.this.makeCurrentEngine(buttonView);
                    }
                }, new DialogInterface.OnClickListener() { // from class: com.android.settings.tts.TtsEnginePreference.4
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        buttonView.setChecked(false);
                    }
                });
                return;
            } else {
                makeCurrentEngine(buttonView);
                return;
            }
        }
        this.mSettingsIcon.setEnabled(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void makeCurrentEngine(Checkable current) {
        if (this.mSharedState.getCurrentChecked() != null) {
            this.mSharedState.getCurrentChecked().setChecked(false);
        }
        this.mSharedState.setCurrentChecked(current);
        this.mSharedState.setCurrentKey(getKey());
        callChangeListener(this.mSharedState.getCurrentKey());
        this.mSettingsIcon.setEnabled(true);
    }
}
