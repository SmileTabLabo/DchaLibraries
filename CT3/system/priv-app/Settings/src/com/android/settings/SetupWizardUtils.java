package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.util.WizardManagerHelper;
/* loaded from: classes.dex */
public class SetupWizardUtils {
    public static int getTheme(Intent intent) {
        if (WizardManagerHelper.isLightTheme(intent, true)) {
            return 2131689943;
        }
        return R.style.SetupWizardTheme;
    }

    public static int getTransparentTheme(Intent intent) {
        if (WizardManagerHelper.isLightTheme(intent, true)) {
            return 2131689945;
        }
        return 2131689944;
    }

    public static void setImmersiveMode(Activity activity) {
        boolean useImmersiveMode = activity.getIntent().getBooleanExtra("useImmersiveMode", false);
        if (!useImmersiveMode) {
            return;
        }
        SystemBarHelper.hideSystemBars(activity.getWindow());
    }

    public static void applyImmersiveFlags(Dialog dialog) {
        SystemBarHelper.hideSystemBars(dialog);
    }

    public static void copySetupExtras(Intent fromIntent, Intent toIntent) {
        toIntent.putExtra("theme", fromIntent.getStringExtra("theme"));
        toIntent.putExtra("useImmersiveMode", fromIntent.getBooleanExtra("useImmersiveMode", false));
    }
}
