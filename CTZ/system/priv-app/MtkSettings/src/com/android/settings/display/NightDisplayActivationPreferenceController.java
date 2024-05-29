package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.TogglePreferenceController;
import java.time.LocalTime;
/* loaded from: classes.dex */
public class NightDisplayActivationPreferenceController extends TogglePreferenceController {
    private ColorDisplayController mController;
    private final View.OnClickListener mListener;
    private NightDisplayTimeFormatter mTimeFormatter;
    private Button mTurnOffButton;
    private Button mTurnOnButton;

    public NightDisplayActivationPreferenceController(Context context, String str) {
        super(context, str);
        this.mListener = new View.OnClickListener() { // from class: com.android.settings.display.NightDisplayActivationPreferenceController.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                NightDisplayActivationPreferenceController.this.mController.setActivated(!NightDisplayActivationPreferenceController.this.mController.isActivated());
                NightDisplayActivationPreferenceController.this.updateStateInternal();
            }
        };
        this.mController = new ColorDisplayController(context);
        this.mTimeFormatter = new NightDisplayTimeFormatter(context);
    }

    @Override // com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        return ColorDisplayController.isAvailable(this.mContext) ? 0 : 2;
    }

    @Override // com.android.settings.core.BasePreferenceController
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "night_display_activated");
    }

    @Override // com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        LayoutPreference layoutPreference = (LayoutPreference) preferenceScreen.findPreference(getPreferenceKey());
        this.mTurnOnButton = (Button) layoutPreference.findViewById(R.id.night_display_turn_on_button);
        this.mTurnOnButton.setOnClickListener(this.mListener);
        this.mTurnOffButton = (Button) layoutPreference.findViewById(R.id.night_display_turn_off_button);
        this.mTurnOffButton.setOnClickListener(this.mListener);
    }

    @Override // com.android.settings.core.TogglePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public final void updateState(Preference preference) {
        updateStateInternal();
    }

    @Override // com.android.settings.core.TogglePreferenceController
    public boolean isChecked() {
        return this.mController.isActivated();
    }

    @Override // com.android.settings.core.TogglePreferenceController
    public boolean setChecked(boolean z) {
        return this.mController.setActivated(z);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public CharSequence getSummary() {
        return this.mTimeFormatter.getAutoModeTimeSummary(this.mContext, this.mController);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateStateInternal() {
        int i;
        String string;
        int i2;
        int i3;
        LocalTime customEndTime;
        if (this.mTurnOnButton == null || this.mTurnOffButton == null) {
            return;
        }
        boolean isActivated = this.mController.isActivated();
        int autoMode = this.mController.getAutoMode();
        if (autoMode == 1) {
            Context context = this.mContext;
            if (isActivated) {
                i3 = R.string.night_display_activation_off_custom;
            } else {
                i3 = R.string.night_display_activation_on_custom;
            }
            Object[] objArr = new Object[1];
            NightDisplayTimeFormatter nightDisplayTimeFormatter = this.mTimeFormatter;
            if (isActivated) {
                customEndTime = this.mController.getCustomStartTime();
            } else {
                customEndTime = this.mController.getCustomEndTime();
            }
            objArr[0] = nightDisplayTimeFormatter.getFormattedTimeString(customEndTime);
            string = context.getString(i3, objArr);
        } else if (autoMode == 2) {
            Context context2 = this.mContext;
            if (isActivated) {
                i2 = R.string.night_display_activation_off_twilight;
            } else {
                i2 = R.string.night_display_activation_on_twilight;
            }
            string = context2.getString(i2);
        } else {
            Context context3 = this.mContext;
            if (isActivated) {
                i = R.string.night_display_activation_off_manual;
            } else {
                i = R.string.night_display_activation_on_manual;
            }
            string = context3.getString(i);
        }
        if (isActivated) {
            this.mTurnOnButton.setVisibility(8);
            this.mTurnOffButton.setVisibility(0);
            this.mTurnOffButton.setText(string);
            return;
        }
        this.mTurnOnButton.setVisibility(0);
        this.mTurnOffButton.setVisibility(8);
        this.mTurnOnButton.setText(string);
    }
}
