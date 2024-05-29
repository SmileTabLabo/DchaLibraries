package com.android.settings;

import android.content.Context;
import android.os.UserManager;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class WorkOnlyCategory extends PreferenceCategory implements SelfAvailablePreference {
    public WorkOnlyCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // com.android.settings.SelfAvailablePreference
    public boolean isAvailable(Context context) {
        return Utils.getManagedProfile(UserManager.get(context)) != null;
    }
}
