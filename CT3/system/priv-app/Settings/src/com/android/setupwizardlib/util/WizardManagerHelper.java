package com.android.setupwizardlib.util;

import android.content.Intent;
/* loaded from: classes.dex */
public class WizardManagerHelper {
    public static boolean isSetupWizardIntent(Intent intent) {
        return intent.getBooleanExtra("firstRun", false);
    }

    public static boolean isLightTheme(Intent intent, boolean def) {
        String theme = intent.getStringExtra("theme");
        return isLightTheme(theme, def);
    }

    public static boolean isLightTheme(String theme, boolean def) {
        if ("holo_light".equals(theme) || "material_light".equals(theme) || "material_blue_light".equals(theme) || "glif_light".equals(theme)) {
            return true;
        }
        if ("holo".equals(theme) || "material".equals(theme) || "material_blue".equals(theme) || "glif".equals(theme)) {
            return false;
        }
        return def;
    }
}
