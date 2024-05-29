package com.android.systemui.tuner;

import android.app.UiModeManager;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.policy.NightModeController;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/tuner/NightModeFragment.class */
public class NightModeFragment extends PreferenceFragment implements TunerService.Tunable, NightModeController.Listener, Preference.OnPreferenceChangeListener {
    private SwitchPreference mAdjustBrightness;
    private SwitchPreference mAdjustTint;
    private SwitchPreference mAutoSwitch;
    private NightModeController mNightModeController;
    private Switch mSwitch;
    private UiModeManager mUiModeManager;
    private static final CharSequence KEY_AUTO = "auto";
    private static final CharSequence KEY_ADJUST_TINT = "adjust_tint";
    private static final CharSequence KEY_ADJUST_BRIGHTNESS = "adjust_brightness";

    /* JADX INFO: Access modifiers changed from: private */
    public void calculateDisabled() {
        if ((this.mAdjustTint.isChecked() ? 1 : 0) + (this.mAdjustBrightness.isChecked() ? 1 : 0) != 1) {
            this.mAdjustTint.setEnabled(true);
            this.mAdjustBrightness.setEnabled(true);
        } else if (this.mAdjustTint.isChecked()) {
            this.mAdjustTint.setEnabled(false);
        } else {
            this.mAdjustBrightness.setEnabled(false);
        }
    }

    private void postCalculateDisabled() {
        getView().post(new Runnable(this) { // from class: com.android.systemui.tuner.NightModeFragment.2
            final NightModeFragment this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.calculateDisabled();
            }
        });
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mNightModeController = new NightModeController(getContext());
        this.mUiModeManager = (UiModeManager) getContext().getSystemService(UiModeManager.class);
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        getPreferenceManager().getContext();
        addPreferencesFromResource(2131296257);
        this.mAutoSwitch = (SwitchPreference) findPreference(KEY_AUTO);
        this.mAutoSwitch.setOnPreferenceChangeListener(this);
        this.mAdjustTint = (SwitchPreference) findPreference(KEY_ADJUST_TINT);
        this.mAdjustTint.setOnPreferenceChangeListener(this);
        this.mAdjustBrightness = (SwitchPreference) findPreference(KEY_ADJUST_BRIGHTNESS);
        this.mAdjustBrightness.setOnPreferenceChangeListener(this);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = LayoutInflater.from(getContext()).inflate(2130968722, viewGroup, false);
        ((ViewGroup) inflate).addView(super.onCreateView(layoutInflater, viewGroup, bundle));
        return inflate;
    }

    @Override // com.android.systemui.statusbar.policy.NightModeController.Listener
    public void onNightModeChanged() {
        this.mSwitch.setChecked(this.mNightModeController.isEnabled());
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 308, false);
        this.mNightModeController.removeListener(this);
        TunerService.get(getContext()).removeTunable(this);
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        int i = 0;
        Boolean bool = (Boolean) obj;
        if (this.mAutoSwitch == preference) {
            MetricsLogger.action(getContext(), 310, bool.booleanValue());
            this.mNightModeController.setAuto(bool.booleanValue());
            return true;
        } else if (this.mAdjustTint == preference) {
            MetricsLogger.action(getContext(), 312, bool.booleanValue());
            this.mNightModeController.setAdjustTint(bool);
            postCalculateDisabled();
            return true;
        } else if (this.mAdjustBrightness == preference) {
            MetricsLogger.action(getContext(), 313, bool.booleanValue());
            TunerService tunerService = TunerService.get(getContext());
            if (bool.booleanValue()) {
                i = 1;
            }
            tunerService.setValue("brightness_use_twilight", i);
            postCalculateDisabled();
            return true;
        } else {
            return false;
        }
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 308, true);
        this.mNightModeController.addListener(this);
        TunerService.get(getContext()).addTunable(this, "brightness_use_twilight", "tuner_night_mode_adjust_tint");
        calculateDisabled();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        boolean z = true;
        if ("brightness_use_twilight".equals(str)) {
            SwitchPreference switchPreference = this.mAdjustBrightness;
            if (str2 == null || Integer.parseInt(str2) == 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        } else if ("tuner_night_mode_adjust_tint".equals(str)) {
            SwitchPreference switchPreference2 = this.mAdjustTint;
            boolean z2 = true;
            if (str2 != null) {
                z2 = Integer.parseInt(str2) != 0;
            }
            switchPreference2.setChecked(z2);
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        View findViewById = view.findViewById(2131886716);
        this.mSwitch = (Switch) findViewById.findViewById(16908352);
        this.mSwitch.setChecked(this.mNightModeController.isEnabled());
        findViewById.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.tuner.NightModeFragment.1
            final NightModeFragment this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                boolean z = !this.this$0.mNightModeController.isEnabled();
                MetricsLogger.action(this.this$0.getContext(), 309, z);
                this.this$0.mNightModeController.setNightMode(z);
                this.this$0.mSwitch.setChecked(z);
            }
        });
    }
}
