package com.mediatek.settings.inputmethod;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;
/* loaded from: classes.dex */
public class VoiceWakeupPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private Context mContext;

    public VoiceWakeupPreferenceController(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return isWakeupSupport(this.mContext) && UserHandle.myUserId() == 0;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "voice_ui";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        Intent intent = new Intent("com.mediatek.voicecommand.VOICE_CONTROL_SETTINGS");
        intent.setFlags(268435456);
        List<ResolveInfo> queryIntentActivities = this.mContext.getPackageManager().queryIntentActivities(intent, 0);
        if (queryIntentActivities == null || queryIntentActivities.size() == 0) {
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
    }

    private boolean isWakeupSupport(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (audioManager == null) {
            Log.e("VoiceWakeupPreferenceController", "isWakeupSupport get audio service is null");
            return false;
        }
        String parameters = audioManager.getParameters("MTK_VOW_SUPPORT");
        if (parameters == null) {
            return false;
        }
        return parameters.equalsIgnoreCase("MTK_VOW_SUPPORT=true");
    }
}
