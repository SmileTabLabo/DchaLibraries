package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.EventLog;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.IRecentsSystemUserCallbacks;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.tv.RecentsTvImpl;
import com.android.systemui.stackdivider.Divider;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/recents/Recents.class */
public class Recents extends SystemUI implements RecentsComponent {
    private static RecentsConfiguration sConfiguration;
    private static RecentsDebugFlags sDebugFlags;
    private static SystemServicesProxy sSystemServicesProxy;
    private static RecentsTaskLoader sTaskLoader;
    private int mDraggingInRecentsCurrentUser;
    private Handler mHandler;
    private RecentsImpl mImpl;
    private String mOverrideRecentsPackageName;
    private RecentsSystemUser mSystemToUserCallbacks;
    private IRecentsSystemUserCallbacks mUserToSystemCallbacks;
    private final ArrayList<Runnable> mOnConnectRunnables = new ArrayList<>();
    private final IBinder.DeathRecipient mUserToSystemCallbacksDeathRcpt = new AnonymousClass1(this);
    private final ServiceConnection mUserToSystemServiceConnection = new ServiceConnection(this) { // from class: com.android.systemui.recents.Recents.2
        final Recents this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder != null) {
                this.this$0.mUserToSystemCallbacks = IRecentsSystemUserCallbacks.Stub.asInterface(iBinder);
                EventLog.writeEvent(36060, 2, Integer.valueOf(Recents.sSystemServicesProxy.getProcessUser()));
                try {
                    iBinder.linkToDeath(this.this$0.mUserToSystemCallbacksDeathRcpt, 0);
                } catch (RemoteException e) {
                    Log.e("Recents", "Lost connection to (System) SystemUI", e);
                }
                this.this$0.runAndFlushOnConnectRunnables();
            }
            this.this$0.mContext.unbindService(this);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /* renamed from: com.android.systemui.recents.Recents$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/recents/Recents$1.class */
    class AnonymousClass1 implements IBinder.DeathRecipient {
        final Recents this$0;

        AnonymousClass1(Recents recents) {
            this.this$0 = recents;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.this$0.mUserToSystemCallbacks = null;
            EventLog.writeEvent(36060, 3, Integer.valueOf(Recents.sSystemServicesProxy.getProcessUser()));
            this.this$0.mHandler.postDelayed(new Runnable(this) { // from class: com.android.systemui.recents.Recents.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.registerWithSystemUser();
                }
            }, 5000L);
        }
    }

    public static RecentsConfiguration getConfiguration() {
        return sConfiguration;
    }

    public static RecentsDebugFlags getDebugFlags() {
        return sDebugFlags;
    }

    private static String getMetricsCounterForResizeMode(int i) {
        switch (i) {
            case 2:
            case 3:
                return "window_enter_supported";
            case 4:
                return "window_enter_unsupported";
            default:
                return "window_enter_incompatible";
        }
    }

    public static SystemServicesProxy getSystemServices() {
        return sSystemServicesProxy;
    }

    public static RecentsTaskLoader getTaskLoader() {
        return sTaskLoader;
    }

    private boolean isUserSetup() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        boolean z = false;
        if (Settings.Global.getInt(contentResolver, "device_provisioned", 0) != 0) {
            z = false;
            if (Settings.Secure.getInt(contentResolver, "user_setup_complete", 0) != 0) {
                z = true;
            }
        }
        return z;
    }

    public static void logDockAttempt(Context context, ComponentName componentName, int i) {
        if (i == 0) {
            MetricsLogger.action(context, 391, componentName.flattenToShortString());
        }
        MetricsLogger.count(context, getMetricsCounterForResizeMode(i), 1);
    }

    private void postToSystemUser(Runnable runnable) {
        this.mOnConnectRunnables.add(runnable);
        if (this.mUserToSystemCallbacks != null) {
            runAndFlushOnConnectRunnables();
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this.mContext, RecentsSystemUserService.class);
        boolean bindServiceAsUser = this.mContext.bindServiceAsUser(intent, this.mUserToSystemServiceConnection, 1, UserHandle.SYSTEM);
        EventLog.writeEvent(36060, 1, Integer.valueOf(sSystemServicesProxy.getProcessUser()));
        if (bindServiceAsUser) {
            return;
        }
        this.mHandler.postDelayed(new Runnable(this) { // from class: com.android.systemui.recents.Recents.9
            final Recents this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.registerWithSystemUser();
            }
        }, 5000L);
    }

    private boolean proxyToOverridePackage(String str) {
        if (this.mOverrideRecentsPackageName != null) {
            Intent intent = new Intent(str);
            intent.setPackage(this.mOverrideRecentsPackageName);
            intent.addFlags(268435456);
            this.mContext.sendBroadcast(intent);
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerWithSystemUser() {
        postToSystemUser(new Runnable(this, sSystemServicesProxy.getProcessUser()) { // from class: com.android.systemui.recents.Recents.8
            final Recents this$0;
            final int val$processUser;

            {
                this.this$0 = this;
                this.val$processUser = r5;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    this.this$0.mUserToSystemCallbacks.registerNonSystemUserCallbacks(new RecentsImplProxy(this.this$0.mImpl), this.val$processUser);
                } catch (RemoteException e) {
                    Log.e("Recents", "Failed to register", e);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runAndFlushOnConnectRunnables() {
        for (Runnable runnable : this.mOnConnectRunnables) {
            runnable.run();
        }
        this.mOnConnectRunnables.clear();
    }

    @Override // com.android.systemui.RecentsComponent
    public void cancelPreloadingRecents() {
        if (isUserSetup()) {
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.cancelPreloadingRecents();
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser == null) {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                    return;
                }
                try {
                    nonSystemUserRecentsForUser.cancelPreloadingRecents();
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        }
    }

    @Override // com.android.systemui.RecentsComponent
    public boolean dockTopTask(int i, int i2, Rect rect, int i3) {
        if (isUserSetup()) {
            Point point = new Point();
            Rect rect2 = rect;
            if (rect == null) {
                ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(0).getRealSize(point);
                rect2 = new Rect(0, 0, point.x, point.y);
            }
            int currentUser = sSystemServicesProxy.getCurrentUser();
            SystemServicesProxy systemServices = getSystemServices();
            ActivityManager.RunningTaskInfo runningTask = systemServices.getRunningTask();
            boolean isScreenPinningActive = systemServices.isScreenPinningActive();
            boolean isHomeStack = runningTask != null ? SystemServicesProxy.isHomeStack(runningTask.stackId) : false;
            if (runningTask == null || isHomeStack || isScreenPinningActive) {
                return false;
            }
            logDockAttempt(this.mContext, runningTask.topActivity, runningTask.resizeMode);
            if (!runningTask.isDockable) {
                Toast.makeText(this.mContext, 2131493587, 0).show();
                return false;
            }
            if (i3 != -1) {
                MetricsLogger.action(this.mContext, i3, runningTask.topActivity.flattenToShortString());
            }
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.dockTopTask(runningTask.id, i, i2, rect2);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser != null) {
                    try {
                        nonSystemUserRecentsForUser.dockTopTask(runningTask.id, i, i2, rect2);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                } else {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
            this.mDraggingInRecentsCurrentUser = currentUser;
            return true;
        }
        return false;
    }

    public IBinder getSystemUserCallbacks() {
        return this.mSystemToUserCallbacks;
    }

    @Override // com.android.systemui.RecentsComponent
    public void hideRecents(boolean z, boolean z2) {
        if (isUserSetup() && !proxyToOverridePackage("com.android.systemui.recents.ACTION_HIDE")) {
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.hideRecents(z, z2);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser == null) {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                    return;
                }
                try {
                    nonSystemUserRecentsForUser.hideRecents(z, z2);
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        this.mImpl.onBootCompleted();
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        this.mImpl.onConfigurationChanged();
    }

    public final void onBusEvent(DockedTopTaskEvent dockedTopTaskEvent) {
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            return;
        }
        postToSystemUser(new Runnable(this, dockedTopTaskEvent) { // from class: com.android.systemui.recents.Recents.6
            final Recents this$0;
            final DockedTopTaskEvent val$event;

            {
                this.this$0 = this;
                this.val$event = dockedTopTaskEvent;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    this.this$0.mUserToSystemCallbacks.sendDockingTopTaskEvent(this.val$event.dragMode, this.val$event.initialRect);
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        });
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            return;
        }
        postToSystemUser(new Runnable(this) { // from class: com.android.systemui.recents.Recents.7
            final Recents this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    this.this$0.mUserToSystemCallbacks.sendLaunchRecentsEvent();
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        });
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        SystemServicesProxy systemServices = getSystemServices();
        if (systemServices.isSystemUser(systemServices.getProcessUser())) {
            this.mImpl.onVisibilityChanged(recentsVisibilityChangedEvent.applicationContext, recentsVisibilityChangedEvent.visible);
        } else {
            postToSystemUser(new Runnable(this, recentsVisibilityChangedEvent) { // from class: com.android.systemui.recents.Recents.3
                final Recents this$0;
                final RecentsVisibilityChangedEvent val$event;

                {
                    this.this$0 = this;
                    this.val$event = recentsVisibilityChangedEvent;
                }

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        this.this$0.mUserToSystemCallbacks.updateRecentsVisibility(this.val$event.visible);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(ScreenPinningRequestEvent screenPinningRequestEvent) {
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            this.mImpl.onStartScreenPinning(screenPinningRequestEvent.applicationContext, screenPinningRequestEvent.taskId);
        } else {
            postToSystemUser(new Runnable(this, screenPinningRequestEvent) { // from class: com.android.systemui.recents.Recents.4
                final Recents this$0;
                final ScreenPinningRequestEvent val$event;

                {
                    this.this$0 = this;
                    this.val$event = screenPinningRequestEvent;
                }

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        this.this$0.mUserToSystemCallbacks.startScreenPinning(this.val$event.taskId);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(RecentsDrawnEvent recentsDrawnEvent) {
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            return;
        }
        postToSystemUser(new Runnable(this) { // from class: com.android.systemui.recents.Recents.5
            final Recents this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    this.this$0.mUserToSystemCallbacks.sendRecentsDrawnEvent();
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        });
    }

    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        int currentUser = sSystemServicesProxy.getCurrentUser();
        if (sSystemServicesProxy.isSystemUser(currentUser)) {
            this.mImpl.onConfigurationChanged();
        } else if (this.mSystemToUserCallbacks != null) {
            IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
            if (nonSystemUserRecentsForUser == null) {
                Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                return;
            }
            try {
                nonSystemUserRecentsForUser.onConfigurationChanged();
            } catch (RemoteException e) {
                Log.e("Recents", "Callback failed", e);
            }
        }
    }

    @Override // com.android.systemui.RecentsComponent
    public void onDraggingInRecents(float f) {
        if (sSystemServicesProxy.isSystemUser(this.mDraggingInRecentsCurrentUser)) {
            this.mImpl.onDraggingInRecents(f);
        } else if (this.mSystemToUserCallbacks != null) {
            IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(this.mDraggingInRecentsCurrentUser);
            if (nonSystemUserRecentsForUser == null) {
                Log.e("Recents", "No SystemUI callbacks found for user: " + this.mDraggingInRecentsCurrentUser);
                return;
            }
            try {
                nonSystemUserRecentsForUser.onDraggingInRecents(f);
            } catch (RemoteException e) {
                Log.e("Recents", "Callback failed", e);
            }
        }
    }

    @Override // com.android.systemui.RecentsComponent
    public void onDraggingInRecentsEnded(float f) {
        if (sSystemServicesProxy.isSystemUser(this.mDraggingInRecentsCurrentUser)) {
            this.mImpl.onDraggingInRecentsEnded(f);
        } else if (this.mSystemToUserCallbacks != null) {
            IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(this.mDraggingInRecentsCurrentUser);
            if (nonSystemUserRecentsForUser == null) {
                Log.e("Recents", "No SystemUI callbacks found for user: " + this.mDraggingInRecentsCurrentUser);
                return;
            }
            try {
                nonSystemUserRecentsForUser.onDraggingInRecentsEnded(f);
            } catch (RemoteException e) {
                Log.e("Recents", "Callback failed", e);
            }
        }
    }

    @Override // com.android.systemui.RecentsComponent
    public void preloadRecents() {
        if (isUserSetup()) {
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.preloadRecents();
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser == null) {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                    return;
                }
                try {
                    nonSystemUserRecentsForUser.preloadRecents();
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        }
    }

    @Override // com.android.systemui.RecentsComponent
    public void showNextAffiliatedTask() {
        if (isUserSetup()) {
            this.mImpl.showNextAffiliatedTask();
        }
    }

    @Override // com.android.systemui.RecentsComponent
    public void showPrevAffiliatedTask() {
        if (isUserSetup()) {
            this.mImpl.showPrevAffiliatedTask();
        }
    }

    @Override // com.android.systemui.RecentsComponent
    public void showRecents(boolean z, boolean z2) {
        if (isUserSetup() && !proxyToOverridePackage("com.android.systemui.recents.ACTION_SHOW")) {
            int growsRecents = ((Divider) getComponent(Divider.class)).getView().growsRecents();
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.showRecents(z, false, true, false, z2, growsRecents);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser == null) {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                    return;
                }
                try {
                    nonSystemUserRecentsForUser.showRecents(z, false, true, false, z2, growsRecents);
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        sDebugFlags = new RecentsDebugFlags(this.mContext);
        sSystemServicesProxy = SystemServicesProxy.getInstance(this.mContext);
        sTaskLoader = new RecentsTaskLoader(this.mContext);
        sConfiguration = new RecentsConfiguration(this.mContext);
        this.mHandler = new Handler();
        if (((UiModeManager) this.mContext.getSystemService("uimode")).getCurrentModeType() == 4) {
            this.mImpl = new RecentsTvImpl(this.mContext);
        } else {
            this.mImpl = new RecentsImpl(this.mContext);
        }
        if ("userdebug".equals(Build.TYPE) || "eng".equals(Build.TYPE)) {
            String str = SystemProperties.get("persist.recents_override_pkg");
            if (!str.isEmpty()) {
                this.mOverrideRecentsPackageName = str;
            }
        }
        EventBus.getDefault().register(this, 1);
        EventBus.getDefault().register(sSystemServicesProxy, 1);
        EventBus.getDefault().register(sTaskLoader, 1);
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            this.mSystemToUserCallbacks = new RecentsSystemUser(this.mContext, this.mImpl);
        } else {
            registerWithSystemUser();
        }
        putComponent(Recents.class, this);
    }

    @Override // com.android.systemui.RecentsComponent
    public void toggleRecents(Display display) {
        if (isUserSetup() && !proxyToOverridePackage("com.android.systemui.recents.ACTION_TOGGLE")) {
            int growsRecents = ((Divider) getComponent(Divider.class)).getView().growsRecents();
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.toggleRecents(growsRecents);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser == null) {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                    return;
                }
                try {
                    nonSystemUserRecentsForUser.toggleRecents(growsRecents);
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                }
            }
        }
    }
}
