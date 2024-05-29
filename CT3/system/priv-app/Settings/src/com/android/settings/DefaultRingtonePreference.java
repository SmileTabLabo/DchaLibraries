package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class DefaultRingtonePreference extends RingtonePreference {
    public DefaultRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // com.android.settings.RingtonePreference
    public void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", false);
    }

    @Override // com.android.settings.RingtonePreference
    protected void onSaveRingtone(Uri ringtoneUri) {
        RingtoneManager.setActualDefaultRingtoneUri(getContext(), getRingtoneType(), ringtoneUri);
    }

    @Override // com.android.settings.RingtonePreference
    protected Uri onRestoreRingtone() {
        return RingtoneManager.getActualDefaultRingtoneUri(getContext(), getRingtoneType());
    }
}
