package com.android.systemui.volume;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.session.MediaSessionManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Prefs;
import com.android.systemui.SystemUI;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.statusbar.ServiceMonitor;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/volume/VolumeUI.class */
public class VolumeUI extends SystemUI {
    private static boolean LOGD = Log.isLoggable("VolumeUI", 3);
    private AudioManager mAudioManager;
    private boolean mEnabled;
    private MediaSessionManager mMediaSessionManager;
    private NotificationManager mNotificationManager;
    private VolumeDialogComponent mVolumeComponent;
    private ServiceMonitor mVolumeControllerService;
    private final Handler mHandler = new Handler();
    private final Receiver mReceiver = new Receiver(this, null);
    private final RestorationNotification mRestorationNotification = new RestorationNotification(this, null);

    /* loaded from: a.zip:com/android/systemui/volume/VolumeUI$Receiver.class */
    private final class Receiver extends BroadcastReceiver {
        final VolumeUI this$0;

        private Receiver(VolumeUI volumeUI) {
            this.this$0 = volumeUI;
        }

        /* synthetic */ Receiver(VolumeUI volumeUI, Receiver receiver) {
            this(volumeUI);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!"com.android.systemui.PREF".equals(action)) {
                ComponentName componentName = (ComponentName) intent.getParcelableExtra("component");
                boolean equals = componentName != null ? componentName.equals(this.this$0.mVolumeControllerService.getComponent()) : false;
                if ("com.android.systemui.vui.ENABLE".equals(action) && componentName != null && !equals) {
                    this.this$0.showServiceActivationDialog(componentName);
                }
                if ("com.android.systemui.vui.DISABLE".equals(action) && componentName != null && equals) {
                    this.this$0.mVolumeControllerService.setComponent(null);
                    return;
                }
                return;
            }
            String stringExtra = intent.getStringExtra("key");
            if (stringExtra == null || intent.getExtras() == null) {
                return;
            }
            Object obj = intent.getExtras().get("value");
            if (obj == null) {
                Prefs.remove(this.this$0.mContext, stringExtra);
            } else if (obj instanceof Boolean) {
                Prefs.putBoolean(this.this$0.mContext, stringExtra, ((Boolean) obj).booleanValue());
            } else if (obj instanceof Integer) {
                Prefs.putInt(this.this$0.mContext, stringExtra, ((Integer) obj).intValue());
            } else if (obj instanceof Long) {
                Prefs.putLong(this.this$0.mContext, stringExtra, ((Long) obj).longValue());
            }
        }

        public void start() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.android.systemui.vui.ENABLE");
            intentFilter.addAction("com.android.systemui.vui.DISABLE");
            intentFilter.addAction("com.android.systemui.PREF");
            this.this$0.mContext.registerReceiver(this, intentFilter, null, this.this$0.mHandler);
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeUI$RestorationNotification.class */
    private final class RestorationNotification {
        final VolumeUI this$0;

        private RestorationNotification(VolumeUI volumeUI) {
            this.this$0 = volumeUI;
        }

        /* synthetic */ RestorationNotification(VolumeUI volumeUI, RestorationNotification restorationNotification) {
            this(volumeUI);
        }

        public void hide() {
            this.this$0.mNotificationManager.cancel(2131886135);
        }

        public void show() {
            ComponentName component = this.this$0.mVolumeControllerService.getComponent();
            if (component == null) {
                Log.w("VolumeUI", "Not showing restoration notification, component not active");
                return;
            }
            Notification.Builder color = new Notification.Builder(this.this$0.mContext).setSmallIcon(2130837815).setWhen(0L).setShowWhen(false).setOngoing(true).setContentTitle(this.this$0.mContext.getString(2131493694, this.this$0.getAppLabel(component))).setContentText(this.this$0.mContext.getString(2131493695)).setContentIntent(PendingIntent.getBroadcast(this.this$0.mContext, 0, new Intent("com.android.systemui.vui.DISABLE").putExtra("component", component), 134217728)).setPriority(-2).setVisibility(1).setColor(this.this$0.mContext.getColor(17170521));
            VolumeUI.overrideNotificationAppName(this.this$0.mContext, color);
            this.this$0.mNotificationManager.notify(2131886135, color.build());
        }
    }

    /* loaded from: a.zip:com/android/systemui/volume/VolumeUI$ServiceMonitorCallbacks.class */
    private final class ServiceMonitorCallbacks implements ServiceMonitor.Callbacks {
        final VolumeUI this$0;

        private ServiceMonitorCallbacks(VolumeUI volumeUI) {
            this.this$0 = volumeUI;
        }

        /* synthetic */ ServiceMonitorCallbacks(VolumeUI volumeUI, ServiceMonitorCallbacks serviceMonitorCallbacks) {
            this(volumeUI);
        }

        @Override // com.android.systemui.statusbar.ServiceMonitor.Callbacks
        public void onNoService() {
            if (VolumeUI.LOGD) {
                Log.d("VolumeUI", "onNoService");
            }
            this.this$0.setDefaultVolumeController(true);
            this.this$0.mRestorationNotification.hide();
            if (this.this$0.mVolumeControllerService.isPackageAvailable()) {
                return;
            }
            this.this$0.mVolumeControllerService.setComponent(null);
        }

        @Override // com.android.systemui.statusbar.ServiceMonitor.Callbacks
        public long onServiceStartAttempt() {
            if (VolumeUI.LOGD) {
                Log.d("VolumeUI", "onServiceStartAttempt");
            }
            this.this$0.mVolumeControllerService.setComponent(this.this$0.mVolumeControllerService.getComponent());
            this.this$0.setDefaultVolumeController(false);
            this.this$0.getVolumeComponent().dismissNow();
            this.this$0.mRestorationNotification.show();
            return 0L;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getAppLabel(ComponentName componentName) {
        String packageName = componentName.getPackageName();
        try {
            String charSequence = this.mContext.getPackageManager().getApplicationLabel(this.mContext.getPackageManager().getApplicationInfo(packageName, 0)).toString();
            if (!TextUtils.isEmpty(charSequence)) {
                return charSequence;
            }
        } catch (Exception e) {
            Log.w("VolumeUI", "Error loading app label", e);
        }
        return packageName;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public VolumeComponent getVolumeComponent() {
        return this.mVolumeComponent;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDefaultVolumeController(boolean z) {
        if (z) {
            DndTile.setVisible(this.mContext, true);
            if (LOGD) {
                Log.d("VolumeUI", "Registering default volume controller");
            }
            getVolumeComponent().register();
            return;
        }
        if (LOGD) {
            Log.d("VolumeUI", "Unregistering default volume controller");
        }
        this.mAudioManager.setVolumeController(null);
        this.mMediaSessionManager.setRemoteVolumeController(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showServiceActivationDialog(ComponentName componentName) {
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setMessage(this.mContext.getString(2131493691, getAppLabel(componentName)));
        systemUIDialog.setPositiveButton(2131493692, new DialogInterface.OnClickListener(this, componentName) { // from class: com.android.systemui.volume.VolumeUI.1
            final VolumeUI this$0;
            final ComponentName val$component;

            {
                this.this$0 = this;
                this.val$component = componentName;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mVolumeControllerService.setComponent(this.val$component);
            }
        });
        systemUIDialog.setNegativeButton(2131493693, null);
        systemUIDialog.show();
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("mEnabled=");
        printWriter.println(this.mEnabled);
        if (this.mEnabled) {
            printWriter.print("mVolumeControllerService=");
            printWriter.println(this.mVolumeControllerService.getComponent());
            getVolumeComponent().dump(fileDescriptor, printWriter, strArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mEnabled) {
            getVolumeComponent().onConfigurationChanged(configuration);
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mEnabled = this.mContext.getResources().getBoolean(2131623965);
        if (this.mEnabled) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
            this.mVolumeComponent = new VolumeDialogComponent(this, this.mContext, null, new ZenModeControllerImpl(this.mContext, this.mHandler));
            putComponent(VolumeComponent.class, getVolumeComponent());
            this.mReceiver.start();
            this.mVolumeControllerService = new ServiceMonitor("VolumeUI", LOGD, this.mContext, "volume_controller_service_component", new ServiceMonitorCallbacks(this, null));
            this.mVolumeControllerService.start();
        }
    }
}
