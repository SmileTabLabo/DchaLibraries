package com.android.settings.wifi.calling;

import android.content.Context;
import com.android.ims.ImsManager;
import com.android.settings.SettingsActivity;
/* loaded from: classes.dex */
public class WifiCallingSuggestionActivity extends SettingsActivity {
    public static boolean isSuggestionComplete(Context context) {
        if (ImsManager.isWfcEnabledByPlatform(context) && ImsManager.isWfcProvisionedOnDevice(context)) {
            return ImsManager.isWfcEnabledByUser(context) && ImsManager.isNonTtyOrTtyOnVolteEnabled(context);
        }
        return true;
    }
}
