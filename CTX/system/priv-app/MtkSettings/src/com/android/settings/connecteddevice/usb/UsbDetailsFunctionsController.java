package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.widget.RadioButtonPreference;
import java.util.LinkedHashMap;
import java.util.Map;
/* loaded from: classes.dex */
public class UsbDetailsFunctionsController extends UsbDetailsController implements RadioButtonPreference.OnClickListener {
    static final Map<Long, Integer> FUNCTIONS_MAP = new LinkedHashMap();
    private PreferenceCategory mProfilesContainer;

    static {
        FUNCTIONS_MAP.put(4L, Integer.valueOf((int) R.string.usb_use_file_transfers));
        FUNCTIONS_MAP.put(32L, Integer.valueOf((int) R.string.usb_use_tethering));
        FUNCTIONS_MAP.put(8L, Integer.valueOf((int) R.string.usb_use_MIDI));
        FUNCTIONS_MAP.put(16L, Integer.valueOf((int) R.string.usb_use_photo_transfers));
        FUNCTIONS_MAP.put(0L, Integer.valueOf((int) R.string.usb_use_charging_only));
    }

    public UsbDetailsFunctionsController(Context context, UsbDetailsFragment usbDetailsFragment, UsbBackend usbBackend) {
        super(context, usbDetailsFragment, usbBackend);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mProfilesContainer = (PreferenceCategory) preferenceScreen.findPreference(getPreferenceKey());
    }

    private RadioButtonPreference getProfilePreference(String str, int i) {
        RadioButtonPreference radioButtonPreference = (RadioButtonPreference) this.mProfilesContainer.findPreference(str);
        if (radioButtonPreference == null) {
            RadioButtonPreference radioButtonPreference2 = new RadioButtonPreference(this.mProfilesContainer.getContext());
            radioButtonPreference2.setKey(str);
            radioButtonPreference2.setTitle(i);
            radioButtonPreference2.setOnClickListener(this);
            this.mProfilesContainer.addPreference(radioButtonPreference2);
            return radioButtonPreference2;
        }
        return radioButtonPreference;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.connecteddevice.usb.UsbDetailsController
    public void refresh(boolean z, long j, int i, int i2) {
        if (!z || i2 != 2) {
            this.mProfilesContainer.setEnabled(false);
        } else {
            this.mProfilesContainer.setEnabled(true);
        }
        for (Long l : FUNCTIONS_MAP.keySet()) {
            long longValue = l.longValue();
            RadioButtonPreference profilePreference = getProfilePreference(UsbBackend.usbFunctionsToString(longValue), FUNCTIONS_MAP.get(Long.valueOf(longValue)).intValue());
            if (this.mUsbBackend.areFunctionsSupported(longValue)) {
                profilePreference.setChecked(j == longValue);
            } else {
                this.mProfilesContainer.removePreference(profilePreference);
            }
        }
    }

    @Override // com.android.settings.widget.RadioButtonPreference.OnClickListener
    public void onRadioButtonClicked(RadioButtonPreference radioButtonPreference) {
        long usbFunctionsFromString = UsbBackend.usbFunctionsFromString(radioButtonPreference.getKey());
        if (usbFunctionsFromString != this.mUsbBackend.getCurrentFunctions() && !Utils.isMonkeyRunning()) {
            this.mUsbBackend.setCurrentFunctions(usbFunctionsFromString);
        }
    }

    @Override // com.android.settings.connecteddevice.usb.UsbDetailsController, com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return !Utils.isMonkeyRunning();
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "usb_details_functions";
    }
}
