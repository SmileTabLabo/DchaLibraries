package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.R$string;
import com.android.systemui.statusbar.policy.NightModeController;
/* loaded from: a.zip:com/android/systemui/tuner/ColorAndAppearanceFragment.class */
public class ColorAndAppearanceFragment extends PreferenceFragment {
    private static final CharSequence KEY_NIGHT_MODE = "night_mode";
    private NightModeController mNightModeController;
    private final Runnable mResetColorMatrix = new Runnable(this) { // from class: com.android.systemui.tuner.ColorAndAppearanceFragment.1
        final ColorAndAppearanceFragment this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            ((DialogFragment) this.this$0.getFragmentManager().findFragmentByTag("RevertWarning")).dismiss();
            Settings.Secure.putString(this.this$0.getContext().getContentResolver(), "accessibility_display_color_matrix", null);
        }
    };

    /* loaded from: a.zip:com/android/systemui/tuner/ColorAndAppearanceFragment$CalibrateDialog.class */
    public static class CalibrateDialog extends DialogFragment implements DialogInterface.OnClickListener {
        private NightModeController mNightModeController;
        private float[] mValues;

        private void bindView(View view, int i) {
            SeekBar seekBar = (SeekBar) view.findViewById(16909261);
            seekBar.setMax(1000);
            seekBar.setProgress((int) (this.mValues[i] * 1000.0f));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(this, i) { // from class: com.android.systemui.tuner.ColorAndAppearanceFragment.CalibrateDialog.1
                final CalibrateDialog this$1;
                final int val$index;

                {
                    this.this$1 = this;
                    this.val$index = i;
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onProgressChanged(SeekBar seekBar2, int i2, boolean z) {
                    this.this$1.mValues[this.val$index] = i2 / 1000.0f;
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStartTrackingTouch(SeekBar seekBar2) {
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStopTrackingTouch(SeekBar seekBar2) {
                }
            });
        }

        public static void show(ColorAndAppearanceFragment colorAndAppearanceFragment) {
            CalibrateDialog calibrateDialog = new CalibrateDialog();
            calibrateDialog.setTargetFragment(colorAndAppearanceFragment, 0);
            calibrateDialog.show(colorAndAppearanceFragment.getFragmentManager(), "Calibrate");
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (this.mValues[0] == 1.0f && this.mValues[5] == 1.0f && this.mValues[10] == 1.0f) {
                this.mNightModeController.setCustomValues(null);
                return;
            }
            ((ColorAndAppearanceFragment) getTargetFragment()).startRevertTimer();
            Settings.Secure.putString(getContext().getContentResolver(), "accessibility_display_color_matrix", NightModeController.toString(this.mValues));
            RevertWarning.show((ColorAndAppearanceFragment) getTargetFragment());
        }

        @Override // android.app.DialogFragment, android.app.Fragment
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.mNightModeController = new NightModeController(getContext());
            String customValues = this.mNightModeController.getCustomValues();
            String str = customValues;
            if (customValues == null) {
                str = NightModeController.toString(NightModeController.IDENTITY_MATRIX);
            }
            this.mValues = NightModeController.toValues(str);
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            View inflate = LayoutInflater.from(getContext()).inflate(2130968606, (ViewGroup) null);
            bindView(inflate.findViewById(2131886260), 0);
            bindView(inflate.findViewById(2131886261), 5);
            bindView(inflate.findViewById(2131886262), 10);
            MetricsLogger.visible(getContext(), 305);
            return new AlertDialog.Builder(getContext()).setTitle(2131493781).setView(inflate).setPositiveButton(2131493791, this).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnDismissListener
        public void onDismiss(DialogInterface dialogInterface) {
            super.onDismiss(dialogInterface);
            MetricsLogger.hidden(getContext(), 305);
        }
    }

    /* loaded from: a.zip:com/android/systemui/tuner/ColorAndAppearanceFragment$RevertWarning.class */
    public static class RevertWarning extends DialogFragment implements DialogInterface.OnClickListener {
        public static void show(ColorAndAppearanceFragment colorAndAppearanceFragment) {
            RevertWarning revertWarning = new RevertWarning();
            revertWarning.setTargetFragment(colorAndAppearanceFragment, 0);
            revertWarning.show(colorAndAppearanceFragment.getFragmentManager(), "RevertWarning");
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialogInterface) {
            super.onCancel(dialogInterface);
            ((ColorAndAppearanceFragment) getTargetFragment()).onRevert();
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            ((ColorAndAppearanceFragment) getTargetFragment()).onApply();
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog create = new AlertDialog.Builder(getContext()).setTitle(2131493792).setMessage(2131493793).setPositiveButton(R$string.ok, this).create();
            create.setCanceledOnTouchOutside(true);
            return create;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onApply() {
        MetricsLogger.action(getContext(), 307);
        this.mNightModeController.setCustomValues(Settings.Secure.getString(getContext().getContentResolver(), "accessibility_display_color_matrix"));
        getView().removeCallbacks(this.mResetColorMatrix);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRevert() {
        getView().removeCallbacks(this.mResetColorMatrix);
        this.mResetColorMatrix.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startRevertTimer() {
        getView().postDelayed(this.mResetColorMatrix, 10000L);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mNightModeController = new NightModeController(getContext());
    }

    @Override // android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(2131296256);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnDisplayPreferenceDialogListener
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof CalibratePreference) {
            CalibrateDialog.show(this);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 306, false);
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 306, true);
        getActivity().setTitle(2131493779);
        findPreference(KEY_NIGHT_MODE).setSummary(this.mNightModeController.isEnabled() ? 2131493782 : 2131493783);
    }
}
