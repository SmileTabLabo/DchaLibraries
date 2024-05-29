package com.android.settings.inputmethod;

import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.InputDevice;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
/* loaded from: classes.dex */
public class KeyboardLayoutPickerFragment extends SettingsPreferenceFragment implements InputManager.InputDeviceListener {
    private InputManager mIm;
    private InputDeviceIdentifier mInputDeviceIdentifier;
    private KeyboardLayout[] mKeyboardLayouts;
    private int mInputDeviceId = -1;
    private HashMap<CheckBoxPreference, KeyboardLayout> mPreferenceMap = new HashMap<>();

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 58;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mInputDeviceIdentifier = getActivity().getIntent().getParcelableExtra("input_device_identifier");
        if (this.mInputDeviceIdentifier == null) {
            getActivity().finish();
        }
        this.mIm = (InputManager) getSystemService("input");
        this.mKeyboardLayouts = this.mIm.getKeyboardLayoutsForInputDevice(this.mInputDeviceIdentifier);
        Arrays.sort(this.mKeyboardLayouts);
        setPreferenceScreen(createPreferenceHierarchy());
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mIm.registerInputDeviceListener(this, null);
        InputDevice inputDevice = this.mIm.getInputDeviceByDescriptor(this.mInputDeviceIdentifier.getDescriptor());
        if (inputDevice == null) {
            getActivity().finish();
            return;
        }
        this.mInputDeviceId = inputDevice.getId();
        updateCheckedState();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        this.mIm.unregisterInputDeviceListener(this);
        this.mInputDeviceId = -1;
        super.onPause();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        CheckBoxPreference checkboxPref;
        KeyboardLayout layout;
        if ((preference instanceof CheckBoxPreference) && (layout = this.mPreferenceMap.get((checkboxPref = (CheckBoxPreference) preference))) != null) {
            boolean checked = checkboxPref.isChecked();
            if (checked) {
                this.mIm.addKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier, layout.getDescriptor());
                return true;
            }
            this.mIm.removeKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier, layout.getDescriptor());
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceAdded(int deviceId) {
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceChanged(int deviceId) {
        if (this.mInputDeviceId < 0 || deviceId != this.mInputDeviceId) {
            return;
        }
        updateCheckedState();
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceRemoved(int deviceId) {
        if (this.mInputDeviceId < 0 || deviceId != this.mInputDeviceId) {
            return;
        }
        getActivity().finish();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        KeyboardLayout[] keyboardLayoutArr;
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
        getActivity();
        for (KeyboardLayout layout : this.mKeyboardLayouts) {
            CheckBoxPreference pref = new CheckBoxPreference(getPrefContext());
            pref.setTitle(layout.getLabel());
            pref.setSummary(layout.getCollection());
            root.addPreference(pref);
            this.mPreferenceMap.put(pref, layout);
        }
        return root;
    }

    private void updateCheckedState() {
        String[] enabledKeyboardLayouts = this.mIm.getEnabledKeyboardLayoutsForInputDevice(this.mInputDeviceIdentifier);
        Arrays.sort(enabledKeyboardLayouts);
        for (Map.Entry<CheckBoxPreference, KeyboardLayout> entry : this.mPreferenceMap.entrySet()) {
            entry.getKey().setChecked(Arrays.binarySearch(enabledKeyboardLayouts, entry.getValue().getDescriptor()) >= 0);
        }
    }
}
