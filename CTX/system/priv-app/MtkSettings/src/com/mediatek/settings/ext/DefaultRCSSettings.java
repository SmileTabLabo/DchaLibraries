package com.mediatek.settings.ext;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.util.Log;
import com.android.internal.telephony.SmsApplication;
import java.util.List;
/* loaded from: classes.dex */
public class DefaultRCSSettings extends ContextWrapper implements IRCSSettings {
    private static final String TAG = "DefaultRCSSettings";

    public DefaultRCSSettings(Context context) {
        super(context);
    }

    @Override // com.mediatek.settings.ext.IRCSSettings
    public void addRCSPreference(Activity activity, PreferenceScreen preferenceScreen) {
        Log.d("@M_DefaultRCSSettings", TAG);
    }

    @Override // com.mediatek.settings.ext.IRCSSettings
    public boolean isNeedAskFirstItemForSms() {
        Log.d("@M_DefaultRCSSettings", "isNeedAskFirstItemForSms");
        return true;
    }

    @Override // com.mediatek.settings.ext.IRCSSettings
    public int getDefaultSmsClickContentExt(List<SubscriptionInfo> list, int i, int i2) {
        Log.d("@M_DefaultRCSSettings", "getDefaultSmsClickContent");
        return i2;
    }

    @Override // com.mediatek.settings.ext.IRCSSettings
    public void setDefaultSmsApplication(String str, Context context) {
        SmsApplication.setDefaultApplication(str, context);
    }
}
