package com.android.settings;

import android.content.Intent;
import android.os.SystemProperties;
/* loaded from: classes.dex */
public class SetupWizardUtils {
    static final String SYSTEM_PROP_SETUPWIZARD_THEME = "setupwizard.theme";

    public static int getTheme(Intent intent) {
        String stringExtra = intent.getStringExtra("theme");
        if (stringExtra == null) {
            stringExtra = SystemProperties.get(SYSTEM_PROP_SETUPWIZARD_THEME);
        }
        if (stringExtra != null) {
            char c = 65535;
            switch (stringExtra.hashCode()) {
                case -2128555920:
                    if (stringExtra.equals("glif_v2_light")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1241052239:
                    if (stringExtra.equals("glif_v3_light")) {
                        c = 0;
                        break;
                    }
                    break;
                case 3175618:
                    if (stringExtra.equals("glif")) {
                        c = 5;
                        break;
                    }
                    break;
                case 115650329:
                    if (stringExtra.equals("glif_v2")) {
                        c = 3;
                        break;
                    }
                    break;
                case 115650330:
                    if (stringExtra.equals("glif_v3")) {
                        c = 1;
                        break;
                    }
                    break;
                case 767685465:
                    if (stringExtra.equals("glif_light")) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return 2131951805;
                case 1:
                    return R.style.GlifV3Theme;
                case 2:
                    return 2131951799;
                case 3:
                    return R.style.GlifV2Theme;
                case 4:
                    return 2131951797;
                case 5:
                    return R.style.GlifTheme;
            }
        }
        return 2131951797;
    }

    public static int getTransparentTheme(Intent intent) {
        int theme = getTheme(intent);
        if (theme == R.style.GlifV3Theme) {
            return 2131951807;
        }
        if (theme == 2131951805) {
            return 2131951806;
        }
        if (theme == R.style.GlifV2Theme) {
            return 2131951801;
        }
        if (theme == 2131951797) {
            return 2131951905;
        }
        if (theme == R.style.GlifTheme) {
            return 2131951906;
        }
        return 2131951800;
    }

    public static void copySetupExtras(Intent intent, Intent intent2) {
        intent2.putExtra("theme", intent.getStringExtra("theme"));
        intent2.putExtra("useImmersiveMode", intent.getBooleanExtra("useImmersiveMode", false));
    }
}
