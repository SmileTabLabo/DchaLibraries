package com.android.settings.display;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class ColorModePreferenceFragment extends RadioButtonPickerFragment implements ColorDisplayController.Callback {
    static final String KEY_COLOR_MODE_AUTOMATIC = "color_mode_automatic";
    static final String KEY_COLOR_MODE_BOOSTED = "color_mode_boosted";
    static final String KEY_COLOR_MODE_NATURAL = "color_mode_natural";
    static final String KEY_COLOR_MODE_SATURATED = "color_mode_saturated";
    private ColorDisplayController mController;

    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mController = new ColorDisplayController(context);
        this.mController.setListener(this);
    }

    @Override // android.app.Fragment
    public void onDetach() {
        super.onDetach();
        if (this.mController != null) {
            this.mController.setListener((ColorDisplayController.Callback) null);
            this.mController = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.widget.RadioButtonPickerFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.color_mode_settings;
    }

    void configureAndInstallPreview(LayoutPreference layoutPreference, PreferenceScreen preferenceScreen) {
        layoutPreference.setSelectable(false);
        preferenceScreen.addPreference(layoutPreference);
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected void addStaticPreferences(PreferenceScreen preferenceScreen) {
        configureAndInstallPreview(new LayoutPreference(preferenceScreen.getContext(), (int) R.layout.color_mode_preview), preferenceScreen);
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected List<? extends CandidateInfo> getCandidates() {
        Context context = getContext();
        int[] intArray = context.getResources().getIntArray(17235987);
        ArrayList arrayList = new ArrayList();
        if (intArray != null) {
            for (int i : intArray) {
                if (i == 0) {
                    arrayList.add(new ColorModeCandidateInfo(context.getText(R.string.color_mode_option_natural), KEY_COLOR_MODE_NATURAL, true));
                } else if (i == 1) {
                    arrayList.add(new ColorModeCandidateInfo(context.getText(R.string.color_mode_option_boosted), KEY_COLOR_MODE_BOOSTED, true));
                } else if (i == 2) {
                    arrayList.add(new ColorModeCandidateInfo(context.getText(R.string.color_mode_option_saturated), KEY_COLOR_MODE_SATURATED, true));
                } else if (i == 3) {
                    arrayList.add(new ColorModeCandidateInfo(context.getText(R.string.color_mode_option_automatic), KEY_COLOR_MODE_AUTOMATIC, true));
                }
            }
        }
        return arrayList;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected String getDefaultKey() {
        int colorMode = this.mController.getColorMode();
        if (colorMode == 3) {
            return KEY_COLOR_MODE_AUTOMATIC;
        }
        if (colorMode == 2) {
            return KEY_COLOR_MODE_SATURATED;
        }
        if (colorMode == 1) {
            return KEY_COLOR_MODE_BOOSTED;
        }
        return KEY_COLOR_MODE_NATURAL;
    }

    @Override // com.android.settings.widget.RadioButtonPickerFragment
    protected boolean setDefaultKey(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode == -2029194174) {
            if (str.equals(KEY_COLOR_MODE_BOOSTED)) {
                c = 1;
            }
            c = 65535;
        } else if (hashCode == -739564821) {
            if (str.equals(KEY_COLOR_MODE_AUTOMATIC)) {
                c = 3;
            }
            c = 65535;
        } else if (hashCode != -365217559) {
            if (hashCode == 765917269 && str.equals(KEY_COLOR_MODE_SATURATED)) {
                c = 2;
            }
            c = 65535;
        } else {
            if (str.equals(KEY_COLOR_MODE_NATURAL)) {
                c = 0;
            }
            c = 65535;
        }
        switch (c) {
            case 0:
                this.mController.setColorMode(0);
                break;
            case 1:
                this.mController.setColorMode(1);
                break;
            case 2:
                this.mController.setColorMode(2);
                break;
            case 3:
                this.mController.setColorMode(3);
                break;
        }
        return true;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 1143;
    }

    /* loaded from: classes.dex */
    static class ColorModeCandidateInfo extends CandidateInfo {
        private final String mKey;
        private final CharSequence mLabel;

        ColorModeCandidateInfo(CharSequence charSequence, String str, boolean z) {
            super(z);
            this.mLabel = charSequence;
            this.mKey = str;
        }

        @Override // com.android.settingslib.widget.CandidateInfo
        public CharSequence loadLabel() {
            return this.mLabel;
        }

        @Override // com.android.settingslib.widget.CandidateInfo
        public Drawable loadIcon() {
            return null;
        }

        @Override // com.android.settingslib.widget.CandidateInfo
        public String getKey() {
            return this.mKey;
        }
    }

    public void onAccessibilityTransformChanged(boolean z) {
        if (z) {
            getActivity().onBackPressed();
        }
    }
}
