package com.android.systemui.recents;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.systemui.SystemUIApplication;
/* loaded from: a.zip:com/android/systemui/recents/RecentsSystemUserService.class */
public class RecentsSystemUserService extends Service {
    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Recents recents = (Recents) ((SystemUIApplication) getApplication()).getComponent(Recents.class);
        if (recents != null) {
            return recents.getSystemUserCallbacks();
        }
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
    }
}
