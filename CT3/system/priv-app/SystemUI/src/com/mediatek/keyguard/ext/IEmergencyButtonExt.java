package com.mediatek.keyguard.ext;

import android.content.Intent;
import android.view.View;
/* loaded from: a.zip:com/mediatek/keyguard/ext/IEmergencyButtonExt.class */
public interface IEmergencyButtonExt {
    void customizeEmergencyIntent(Intent intent, int i);

    void setEmergencyButtonVisibility(View view, float f);

    boolean showEccByServiceState(boolean[] zArr, int i);

    boolean showEccInNonSecureUnlock();
}
