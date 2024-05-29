package com.android.launcher3;

import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.ConfigMonitor;
import com.android.launcher3.util.Preconditions;
import com.android.launcher3.util.SettingsObserver;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
/* loaded from: classes.dex */
public class LauncherAppState {
    public static final String ACTION_FORCE_ROLOAD = "force-reload-launcher";
    private static LauncherAppState INSTANCE;
    private final Context mContext;
    private final IconCache mIconCache;
    private final InvariantDeviceProfile mInvariantDeviceProfile;
    private final LauncherModel mModel;
    private final SettingsObserver mNotificationBadgingObserver;
    private final WidgetPreviewLoader mWidgetCache;

    public static LauncherAppState getInstance(final Context context) {
        if (INSTANCE == null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                INSTANCE = new LauncherAppState(context.getApplicationContext());
            } else {
                try {
                    return (LauncherAppState) new MainThreadExecutor().submit(new Callable<LauncherAppState>() { // from class: com.android.launcher3.LauncherAppState.1
                        /* JADX WARN: Can't rename method to resolve collision */
                        @Override // java.util.concurrent.Callable
                        public LauncherAppState call() throws Exception {
                            return LauncherAppState.getInstance(context);
                        }
                    }).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public Context getContext() {
        return this.mContext;
    }

    private LauncherAppState(Context context) {
        if (getLocalProvider(context) == null) {
            throw new RuntimeException("Initializing LauncherAppState in the absence of LauncherProvider");
        }
        Log.v(Launcher.TAG, "LauncherAppState initiated");
        Preconditions.assertUIThread();
        this.mContext = context;
        this.mInvariantDeviceProfile = new InvariantDeviceProfile(this.mContext);
        this.mIconCache = new IconCache(this.mContext, this.mInvariantDeviceProfile);
        this.mWidgetCache = new WidgetPreviewLoader(this.mContext, this.mIconCache);
        this.mModel = new LauncherModel(this, this.mIconCache, AppFilter.newInstance(this.mContext));
        LauncherAppsCompat.getInstance(this.mContext).addOnAppsChangedCallback(this.mModel);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNLOCKED");
        this.mContext.registerReceiver(this.mModel, intentFilter);
        UserManagerCompat.getInstance(this.mContext).enableAndResetCache();
        new ConfigMonitor(this.mContext).register();
        if (!this.mContext.getResources().getBoolean(R.bool.notification_badging_enabled)) {
            this.mNotificationBadgingObserver = null;
            return;
        }
        this.mNotificationBadgingObserver = new SettingsObserver.Secure(this.mContext.getContentResolver()) { // from class: com.android.launcher3.LauncherAppState.2
            @Override // com.android.launcher3.util.SettingsObserver
            public void onSettingChanged(boolean z) {
                if (z) {
                    NotificationListener.requestRebind(new ComponentName(LauncherAppState.this.mContext, NotificationListener.class));
                }
            }
        };
        this.mNotificationBadgingObserver.register(SettingsActivity.NOTIFICATION_BADGING, new String[0]);
    }

    public void onTerminate() {
        this.mContext.unregisterReceiver(this.mModel);
        LauncherAppsCompat.getInstance(this.mContext).removeOnAppsChangedCallback(this.mModel);
        PackageInstallerCompat.getInstance(this.mContext).onStop();
        if (this.mNotificationBadgingObserver != null) {
            this.mNotificationBadgingObserver.unregister();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherModel setLauncher(Launcher launcher) {
        getLocalProvider(this.mContext).setLauncherProviderChangeListener(launcher);
        this.mModel.initialize(launcher);
        return this.mModel;
    }

    public IconCache getIconCache() {
        return this.mIconCache;
    }

    public LauncherModel getModel() {
        return this.mModel;
    }

    public WidgetPreviewLoader getWidgetCache() {
        return this.mWidgetCache;
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return this.mInvariantDeviceProfile;
    }

    public static InvariantDeviceProfile getIDP(Context context) {
        return getInstance(context).getInvariantDeviceProfile();
    }

    private static LauncherProvider getLocalProvider(Context context) {
        ContentProviderClient acquireContentProviderClient = context.getContentResolver().acquireContentProviderClient(LauncherProvider.AUTHORITY);
        try {
            LauncherProvider launcherProvider = (LauncherProvider) acquireContentProviderClient.getLocalContentProvider();
            if (acquireContentProviderClient != null) {
                acquireContentProviderClient.close();
            }
            return launcherProvider;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (acquireContentProviderClient != null) {
                    if (th != null) {
                        try {
                            acquireContentProviderClient.close();
                        } catch (Throwable th3) {
                            th.addSuppressed(th3);
                        }
                    } else {
                        acquireContentProviderClient.close();
                    }
                }
                throw th2;
            }
        }
    }
}
