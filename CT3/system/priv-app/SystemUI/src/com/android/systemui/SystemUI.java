package com.android.systemui;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
/* loaded from: a.zip:com/android/systemui/SystemUI.class */
public abstract class SystemUI {
    public Map<Class<?>, Object> mComponents;
    public Context mContext;

    public static void overrideNotificationAppName(Context context, Notification.Builder builder) {
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", context.getString(17039678));
        builder.addExtras(bundle);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public <T> T getComponent(Class<T> cls) {
        T t = null;
        if (this.mComponents != null) {
            t = this.mComponents.get(cls);
        }
        return t;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onBootCompleted() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
    }

    public <T, C extends T> void putComponent(Class<T> cls, C c) {
        if (this.mComponents != null) {
            this.mComponents.put(cls, c);
        }
    }

    public abstract void start();
}
