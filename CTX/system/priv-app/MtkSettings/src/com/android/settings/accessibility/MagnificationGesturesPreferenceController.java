package com.android.settings.accessibility;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
/* loaded from: classes.dex */
public class MagnificationGesturesPreferenceController extends TogglePreferenceController {
    private boolean mIsFromSUW;

    public MagnificationGesturesPreferenceController(Context context, String str) {
        super(context, str);
        this.mIsFromSUW = false;
    }

    @Override // com.android.settings.core.TogglePreferenceController
    public boolean isChecked() {
        return MagnificationPreferenceFragment.isChecked(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled");
    }

    @Override // com.android.settings.core.TogglePreferenceController
    public boolean setChecked(boolean z) {
        return MagnificationPreferenceFragment.setChecked(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", z);
    }

    public void setIsFromSUW(boolean z) {
        this.mIsFromSUW = z;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            Bundle extras = preference.getExtras();
            populateMagnificationGesturesPreferenceExtras(extras, this.mContext);
            extras.putBoolean("checked", isChecked());
            extras.putBoolean("from_suw", this.mIsFromSUW);
            return false;
        }
        return false;
    }

    @Override // com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        return 0;
    }

    @Override // com.android.settings.core.BasePreferenceController
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "screen_magnification_gestures_preference_screen");
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public CharSequence getSummary() {
        int i;
        if (this.mIsFromSUW) {
            i = R.string.accessibility_screen_magnification_short_summary;
        } else {
            i = isChecked() ? R.string.accessibility_feature_state_on : R.string.accessibility_feature_state_off;
        }
        return this.mContext.getString(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void populateMagnificationGesturesPreferenceExtras(Bundle bundle, Context context) {
        bundle.putString("preference_key", "accessibility_display_magnification_enabled");
        bundle.putInt("title_res", R.string.accessibility_screen_magnification_gestures_title);
        bundle.putInt("summary_res", R.string.accessibility_screen_magnification_summary);
        bundle.putInt("video_resource", R.raw.accessibility_screen_magnification);
    }
}
