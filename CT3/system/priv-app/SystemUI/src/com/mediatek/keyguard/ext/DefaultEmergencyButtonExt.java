package com.mediatek.keyguard.ext;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import com.mediatek.common.PluginImpl;
@PluginImpl(interfaceName = "com.mediatek.keyguard.ext.IEmergencyButtonExt")
/* loaded from: a.zip:com/mediatek/keyguard/ext/DefaultEmergencyButtonExt.class */
public class DefaultEmergencyButtonExt implements IEmergencyButtonExt {
    private static final boolean DEBUG = true;
    private static final String TAG = "DefaultEmergencyButtonExt";

    @Override // com.mediatek.keyguard.ext.IEmergencyButtonExt
    public void customizeEmergencyIntent(Intent intent, int i) {
    }

    @Override // com.mediatek.keyguard.ext.IEmergencyButtonExt
    public void setEmergencyButtonVisibility(View view, float f) {
    }

    @Override // com.mediatek.keyguard.ext.IEmergencyButtonExt
    public boolean showEccByServiceState(boolean[] zArr, int i) {
        int length = zArr.length;
        for (int i2 = 0; i2 < length; i2++) {
            Log.d(TAG, "showEccByServiceState i = " + i2 + " isServiceSupportEcc[i] = " + zArr[i2]);
            if (zArr[i2]) {
                return true;
            }
        }
        return false;
    }

    @Override // com.mediatek.keyguard.ext.IEmergencyButtonExt
    public boolean showEccInNonSecureUnlock() {
        Log.d(TAG, "showEccInNonSecureUnlock return false");
        return false;
    }
}
