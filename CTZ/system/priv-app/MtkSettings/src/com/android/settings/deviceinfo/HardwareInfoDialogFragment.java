package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IDeviceInfoSettingsExt;
/* loaded from: classes.dex */
public class HardwareInfoDialogFragment extends InstrumentedDialogFragment {
    private IDeviceInfoSettingsExt mExt;

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 862;
    }

    public static HardwareInfoDialogFragment newInstance() {
        return new HardwareInfoDialogFragment();
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        this.mExt = UtilsExt.getDeviceInfoSettingsExt(getActivity());
        AlertDialog.Builder positiveButton = new AlertDialog.Builder(getActivity()).setTitle(R.string.hardware_info).setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        View inflate = LayoutInflater.from(positiveButton.getContext()).inflate(R.layout.dialog_hardware_info, (ViewGroup) null);
        setText(inflate, R.id.model_label, R.id.model_value, this.mExt.customeModelInfo(DeviceModelPreferenceController.getDeviceModel()));
        setText(inflate, R.id.serial_number_label, R.id.serial_number_value, getSerialNumber());
        setText(inflate, R.id.hardware_rev_label, R.id.hardware_rev_value, SystemProperties.get("ro.boot.hardware.revision"));
        return positiveButton.setView(inflate).create();
    }

    void setText(View view, int i, int i2, String str) {
        if (view == null) {
            return;
        }
        View findViewById = view.findViewById(i);
        TextView textView = (TextView) view.findViewById(i2);
        if (!TextUtils.isEmpty(str)) {
            findViewById.setVisibility(0);
            textView.setVisibility(0);
            textView.setText(str);
            return;
        }
        findViewById.setVisibility(8);
        textView.setVisibility(8);
    }

    String getSerialNumber() {
        return Build.getSerial();
    }
}
