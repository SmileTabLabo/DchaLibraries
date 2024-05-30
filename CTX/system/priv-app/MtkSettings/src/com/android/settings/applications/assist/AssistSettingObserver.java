package com.android.settings.applications.assist;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;
import com.android.settingslib.utils.ThreadUtils;
import java.util.List;
/* loaded from: classes.dex */
public abstract class AssistSettingObserver extends ContentObserver {
    private final Uri ASSIST_URI;

    protected abstract List<Uri> getSettingUris();

    public abstract void onSettingChange();

    public AssistSettingObserver() {
        super(null);
        this.ASSIST_URI = Settings.Secure.getUriFor("assistant");
    }

    public void register(ContentResolver contentResolver, boolean z) {
        if (z) {
            contentResolver.registerContentObserver(this.ASSIST_URI, false, this);
            List<Uri> settingUris = getSettingUris();
            if (settingUris != null) {
                for (Uri uri : settingUris) {
                    contentResolver.registerContentObserver(uri, false, this);
                }
                return;
            }
            return;
        }
        contentResolver.unregisterContentObserver(this);
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean z, Uri uri) {
        boolean z2;
        super.onChange(z, uri);
        List<Uri> settingUris = getSettingUris();
        if (this.ASSIST_URI.equals(uri) || (settingUris != null && settingUris.contains(uri))) {
            z2 = true;
        } else {
            z2 = false;
        }
        if (z2) {
            ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settings.applications.assist.-$$Lambda$AssistSettingObserver$iBFvDXS30QMXzEK-zAgHqcs78mE
                @Override // java.lang.Runnable
                public final void run() {
                    AssistSettingObserver.this.onSettingChange();
                }
            });
        }
    }
}
