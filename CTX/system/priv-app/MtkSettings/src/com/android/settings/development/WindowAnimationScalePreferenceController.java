package com.android.settings.development;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.view.IWindowManager;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
/* loaded from: classes.dex */
public class WindowAnimationScalePreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    static final float DEFAULT_VALUE = 1.0f;
    static final int WINDOW_ANIMATION_SCALE_SELECTOR = 0;
    private final String[] mListSummaries;
    private final String[] mListValues;
    private final IWindowManager mWindowManager;

    public WindowAnimationScalePreferenceController(Context context) {
        super(context);
        this.mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mListValues = context.getResources().getStringArray(R.array.window_animation_scale_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.window_animation_scale_entries);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "window_animation_scale";
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        writeAnimationScaleOption(obj);
        return true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        updateAnimationScaleValue();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.development.DeveloperOptionsPreferenceController
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeAnimationScaleOption(null);
    }

    private void writeAnimationScaleOption(Object obj) {
        float parseFloat;
        if (obj == null) {
            parseFloat = DEFAULT_VALUE;
        } else {
            try {
                parseFloat = Float.parseFloat(obj.toString());
            } catch (RemoteException e) {
                return;
            }
        }
        this.mWindowManager.setAnimationScale(0, parseFloat);
        updateAnimationScaleValue();
    }

    private void updateAnimationScaleValue() {
        try {
            int i = 0;
            float animationScale = this.mWindowManager.getAnimationScale(0);
            int i2 = 0;
            while (true) {
                if (i2 >= this.mListValues.length) {
                    break;
                } else if (animationScale > Float.parseFloat(this.mListValues[i2])) {
                    i2++;
                } else {
                    i = i2;
                    break;
                }
            }
            ListPreference listPreference = (ListPreference) this.mPreference;
            listPreference.setValue(this.mListValues[i]);
            listPreference.setSummary(this.mListSummaries[i]);
        } catch (RemoteException e) {
        }
    }
}
