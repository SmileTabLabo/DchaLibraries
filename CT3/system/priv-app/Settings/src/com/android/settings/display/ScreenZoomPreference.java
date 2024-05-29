package com.android.settings.display;

import android.content.Context;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settingslib.display.DisplayDensityUtils;
/* loaded from: classes.dex */
public class ScreenZoomPreference extends PreferenceGroup {
    public ScreenZoomPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceScreenStyle, 16842891));
        if (TextUtils.isEmpty(getFragment())) {
            setFragment("com.android.settings.display.ScreenZoomSettings");
        }
        DisplayDensityUtils density = new DisplayDensityUtils(context);
        int defaultIndex = density.getCurrentIndex();
        if (defaultIndex < 0) {
            setVisible(false);
            setEnabled(false);
        } else if (!TextUtils.isEmpty(getSummary())) {
        } else {
            String[] entries = density.getEntries();
            int currentIndex = density.getCurrentIndex();
            setSummary(entries[currentIndex]);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.PreferenceGroup
    public boolean isOnSameScreenAsChildren() {
        return false;
    }
}
