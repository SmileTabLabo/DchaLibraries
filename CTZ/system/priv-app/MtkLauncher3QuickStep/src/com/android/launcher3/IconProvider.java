package com.android.launcher3;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import java.util.Locale;
/* loaded from: classes.dex */
public class IconProvider {
    protected String mSystemState;

    public static IconProvider newInstance(Context context) {
        IconProvider iconProvider = (IconProvider) Utilities.getOverrideObject(IconProvider.class, context, R.string.icon_provider_class);
        iconProvider.updateSystemStateString(context);
        return iconProvider;
    }

    public void updateSystemStateString(Context context) {
        String locale;
        if (Utilities.ATLEAST_NOUGAT) {
            locale = context.getResources().getConfiguration().getLocales().toLanguageTags();
        } else {
            locale = Locale.getDefault().toString();
        }
        this.mSystemState = locale + "," + Build.VERSION.SDK_INT;
    }

    public String getIconSystemState(String str) {
        return this.mSystemState;
    }

    public Drawable getIcon(LauncherActivityInfo launcherActivityInfo, int i, boolean z) {
        return launcherActivityInfo.getIcon(i);
    }
}
