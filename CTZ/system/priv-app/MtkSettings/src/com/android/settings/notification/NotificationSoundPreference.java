package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settings.RingtonePreference;
/* loaded from: classes.dex */
public class NotificationSoundPreference extends RingtonePreference {
    private Uri mRingtone;

    public NotificationSoundPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.settings.RingtonePreference
    protected Uri onRestoreRingtone() {
        return this.mRingtone;
    }

    public void setRingtone(Uri uri) {
        this.mRingtone = uri;
        setSummary(" ");
        updateRingtoneName(this.mRingtone);
    }

    @Override // com.android.settings.RingtonePreference
    public boolean onActivityResult(int i, int i2, Intent intent) {
        if (intent != null) {
            Uri uri = (Uri) intent.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
            setRingtone(uri);
            callChangeListener(uri);
            return true;
        }
        return true;
    }

    private void updateRingtoneName(final Uri uri) {
        new AsyncTask<Object, Void, CharSequence>() { // from class: com.android.settings.notification.NotificationSoundPreference.1
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.AsyncTask
            public CharSequence doInBackground(Object... objArr) {
                if (uri == null) {
                    return NotificationSoundPreference.this.getContext().getString(17040786);
                }
                if (RingtoneManager.isDefault(uri)) {
                    return NotificationSoundPreference.this.getContext().getString(R.string.notification_sound_default);
                }
                if ("android.resource".equals(uri.getScheme())) {
                    return NotificationSoundPreference.this.getContext().getString(R.string.notification_unknown_sound_title);
                }
                return Ringtone.getTitle(NotificationSoundPreference.this.getContext(), uri, false, true);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(CharSequence charSequence) {
                NotificationSoundPreference.this.setSummary(charSequence);
            }
        }.execute(new Object[0]);
    }
}
