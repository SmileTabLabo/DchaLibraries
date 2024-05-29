package com.android.systemui;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.keyboard.KeyboardUI;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.media.RingtonePlayer;
import com.android.systemui.power.PowerUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.shortcut.ShortcutKeyDispatcher;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.SystemBars;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tv.pip.PipUI;
import com.android.systemui.usb.StorageNotification;
import com.android.systemui.volume.VolumeUI;
import java.util.HashMap;
import java.util.Map;
/* loaded from: a.zip:com/android/systemui/SystemUIApplication.class */
public class SystemUIApplication extends Application {
    private boolean mBootCompleted;
    private boolean mServicesStarted;
    private final Class<?>[] SERVICES = {TunerService.class, KeyguardViewMediator.class, Recents.class, VolumeUI.class, Divider.class, SystemBars.class, StorageNotification.class, PowerUI.class, RingtonePlayer.class, KeyboardUI.class, PipUI.class, ShortcutKeyDispatcher.class};
    private final Class<?>[] SERVICES_PER_USER = {Recents.class, PipUI.class};
    private final SystemUI[] mServices = new SystemUI[this.SERVICES.length];
    private final Map<Class<?>, Object> mComponents = new HashMap();

    private void startServicesIfNeeded(Class<?>[] clsArr) {
        if (this.mServicesStarted) {
            return;
        }
        if (!this.mBootCompleted && "1".equals(SystemProperties.get("sys.boot_completed"))) {
            this.mBootCompleted = true;
        }
        Log.v("SystemUIService", "Starting SystemUI services for user " + Process.myUserHandle().getIdentifier() + ".");
        int length = clsArr.length;
        for (int i = 0; i < length; i++) {
            Class<?> cls = clsArr[i];
            try {
                Object createInstance = SystemUIFactory.getInstance().createInstance(cls);
                SystemUI[] systemUIArr = this.mServices;
                if (createInstance == null) {
                    createInstance = cls.newInstance();
                }
                systemUIArr[i] = (SystemUI) createInstance;
                this.mServices[i].mContext = this;
                this.mServices[i].mComponents = this.mComponents;
                this.mServices[i].start();
                if (this.mBootCompleted) {
                    this.mServices[i].onBootCompleted();
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e2) {
                throw new RuntimeException(e2);
            }
        }
        this.mServicesStarted = true;
    }

    public <T> T getComponent(Class<T> cls) {
        return (T) this.mComponents.get(cls);
    }

    public SystemUI[] getServices() {
        return this.mServices;
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        if (this.mServicesStarted) {
            int length = this.mServices.length;
            for (int i = 0; i < length; i++) {
                if (this.mServices[i] != null) {
                    this.mServices[i].onConfigurationChanged(configuration);
                }
            }
        }
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        setTheme(2131952120);
        SystemUIFactory.createFromConfig(this);
        if (!Process.myUserHandle().equals(UserHandle.SYSTEM)) {
            startServicesIfNeeded(this.SERVICES_PER_USER);
            return;
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
        intentFilter.setPriority(1000);
        registerReceiver(new BroadcastReceiver(this) { // from class: com.android.systemui.SystemUIApplication.1
            final SystemUIApplication this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (this.this$0.mBootCompleted) {
                    return;
                }
                this.this$0.unregisterReceiver(this);
                this.this$0.mBootCompleted = true;
                if (this.this$0.mServicesStarted) {
                    int length = this.this$0.mServices.length;
                    for (int i = 0; i < length; i++) {
                        this.this$0.mServices[i].onBootCompleted();
                    }
                }
            }
        }, intentFilter);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void startSecondaryUserServicesIfNeeded() {
        startServicesIfNeeded(this.SERVICES_PER_USER);
    }

    public void startServicesIfNeeded() {
        startServicesIfNeeded(this.SERVICES);
    }
}
