package com.android.systemui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/SystemUIService.class */
public class SystemUIService extends Service {
    @Override // android.app.Service
    protected void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        SystemUI[] services = ((SystemUIApplication) getApplication()).getServices();
        if (strArr == null || strArr.length == 0) {
            for (SystemUI systemUI : services) {
                printWriter.println("dumping service: " + systemUI.getClass().getName());
                systemUI.dump(fileDescriptor, printWriter, strArr);
            }
            return;
        }
        String str = strArr[0];
        for (SystemUI systemUI2 : services) {
            if (systemUI2.getClass().getName().endsWith(str)) {
                systemUI2.dump(fileDescriptor, printWriter, strArr);
            }
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        ((SystemUIApplication) getApplication()).startServicesIfNeeded();
    }
}
