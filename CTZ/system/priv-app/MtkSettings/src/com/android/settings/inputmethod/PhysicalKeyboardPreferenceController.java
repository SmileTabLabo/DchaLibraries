package com.android.settings.inputmethod;

import android.content.Context;
import android.hardware.input.InputManager;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.inputmethod.PhysicalKeyboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.List;
/* loaded from: classes.dex */
public class PhysicalKeyboardPreferenceController extends AbstractPreferenceController implements InputManager.InputDeviceListener, PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {
    private final InputManager mIm;
    private Preference mPreference;

    public PhysicalKeyboardPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mIm = (InputManager) context.getSystemService("input");
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_physical_keyboard_pref);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        this.mPreference = preference;
        updateSummary();
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "physical_keyboard_pref";
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnPause
    public void onPause() {
        this.mIm.unregisterInputDeviceListener(this);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnResume
    public void onResume() {
        this.mIm.registerInputDeviceListener(this, null);
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceAdded(int i) {
        updateSummary();
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceRemoved(int i) {
        updateSummary();
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceChanged(int i) {
        updateSummary();
    }

    private void updateSummary() {
        if (this.mPreference == null) {
            return;
        }
        List<PhysicalKeyboardFragment.HardKeyboardDeviceInfo> hardKeyboards = PhysicalKeyboardFragment.getHardKeyboards(this.mContext);
        if (hardKeyboards.isEmpty()) {
            this.mPreference.setSummary(R.string.disconnected);
            return;
        }
        String str = null;
        for (PhysicalKeyboardFragment.HardKeyboardDeviceInfo hardKeyboardDeviceInfo : hardKeyboards) {
            if (str == null) {
                str = hardKeyboardDeviceInfo.mDeviceName;
            } else {
                str = this.mContext.getString(R.string.join_many_items_middle, str, hardKeyboardDeviceInfo.mDeviceName);
            }
        }
        this.mPreference.setSummary(str);
    }
}
