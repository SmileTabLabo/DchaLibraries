package com.mediatek.settings.ext;

import android.util.Log;
/* loaded from: classes.dex */
public class DefaultWWOPJoynSettingsExt implements IWWOPJoynSettingsExt {
    private static final String TAG = "DefaultWWOPJoynSettingsExt";

    @Override // com.mediatek.settings.ext.IWWOPJoynSettingsExt
    public boolean isJoynSettingsEnabled() {
        Log.d("@M_DefaultWWOPJoynSettingsExt", "isJoynSettingsEnabled");
        return false;
    }
}
