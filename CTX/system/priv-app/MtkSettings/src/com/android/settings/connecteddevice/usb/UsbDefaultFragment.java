package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;
import com.android.settingslib.widget.FooterPreferenceMixin;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class UsbDefaultFragment extends RadioButtonPickerFragment {
    @VisibleForTesting
    UsbBackend mUsbBackend;

    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mUsbBackend = new UsbBackend(context);
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment, android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        super.onCreatePreferences(bundle, str);
        new FooterPreferenceMixin(this, getLifecycle()).createFooterPreference().setTitle(R.string.usb_default_info);
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 1312;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.usb_default_fragment;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected List<? extends CandidateInfo> getCandidates() {
        ArrayList newArrayList = Lists.newArrayList();
        for (Long l : UsbDetailsFunctionsController.FUNCTIONS_MAP.keySet()) {
            long longValue = l.longValue();
            final String string = getContext().getString(UsbDetailsFunctionsController.FUNCTIONS_MAP.get(Long.valueOf(longValue)).intValue());
            final String usbFunctionsToString = UsbBackend.usbFunctionsToString(longValue);
            if (this.mUsbBackend.areFunctionsSupported(longValue)) {
                newArrayList.add(new CandidateInfo(true) { // from class: com.android.settings.connecteddevice.usb.UsbDefaultFragment.1
                    @Override // com.android.settingslib.widget.CandidateInfo
                    public CharSequence loadLabel() {
                        return string;
                    }

                    @Override // com.android.settingslib.widget.CandidateInfo
                    public Drawable loadIcon() {
                        return null;
                    }

                    @Override // com.android.settingslib.widget.CandidateInfo
                    public String getKey() {
                        return usbFunctionsToString;
                    }
                });
            }
        }
        return newArrayList;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected String getDefaultKey() {
        return UsbBackend.usbFunctionsToString(this.mUsbBackend.getDefaultUsbFunctions());
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected boolean setDefaultKey(String str) {
        long usbFunctionsFromString = UsbBackend.usbFunctionsFromString(str);
        if (!Utils.isMonkeyRunning()) {
            this.mUsbBackend.setDefaultUsbFunctions(usbFunctionsFromString);
            return true;
        }
        return true;
    }
}
