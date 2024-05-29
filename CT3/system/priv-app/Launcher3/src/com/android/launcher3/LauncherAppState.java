package com.android.launcher3;

import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.util.ConfigMonitor;
import java.lang.ref.WeakReference;
/* loaded from: a.zip:com/android/launcher3/LauncherAppState.class */
public class LauncherAppState {
    private static LauncherAppState INSTANCE;
    private static Context sContext;
    private static WeakReference<LauncherProvider> sLauncherProvider;
    private LauncherAccessibilityDelegate mAccessibilityDelegate;
    private final AppFilter mAppFilter;
    private final IconCache mIconCache;
    private InvariantDeviceProfile mInvariantDeviceProfile;
    final LauncherModel mModel;
    private boolean mWallpaperChangedSinceLastCheck;
    private final WidgetPreviewLoader mWidgetCache;

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }
        Log.v("Launcher", "LauncherAppState inited");
        this.mInvariantDeviceProfile = new InvariantDeviceProfile(sContext);
        this.mIconCache = new IconCache(sContext, this.mInvariantDeviceProfile);
        this.mWidgetCache = new WidgetPreviewLoader(sContext, this.mIconCache);
        this.mAppFilter = AppFilter.loadByName(sContext.getString(2131558400));
        this.mModel = new LauncherModel(this, this.mIconCache, this.mAppFilter);
        LauncherAppsCompat.getInstance(sContext).addOnAppsChangedCallback(this.mModel);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("android.search.action.GLOBAL_SEARCH_ACTIVITY_CHANGED");
        intentFilter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED);
        intentFilter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED);
        intentFilter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_AVAILABLE);
        intentFilter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_UNAVAILABLE);
        sContext.registerReceiver(this.mModel, intentFilter);
        UserManagerCompat.getInstance(sContext).enableAndResetCache();
        new ConfigMonitor(sContext).register();
        sContext.registerReceiver(new WallpaperChangedReceiver(), new IntentFilter("android.intent.action.WALLPAPER_CHANGED"));
    }

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public static LauncherProvider getLauncherProvider() {
        return sLauncherProvider.get();
    }

    public static boolean isDogfoodBuild() {
        return !FeatureFlags.IS_ALPHA_BUILD ? FeatureFlags.IS_DEV_BUILD : true;
    }

    public static void setApplicationContext(Context context) {
        if (sContext != null) {
            Log.w("Launcher", "setApplicationContext called twice! old=" + sContext + " new=" + context);
        }
        sContext = context.getApplicationContext();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setLauncherProvider(LauncherProvider launcherProvider) {
        sLauncherProvider = new WeakReference<>(launcherProvider);
    }

    public LauncherAccessibilityDelegate getAccessibilityDelegate() {
        return this.mAccessibilityDelegate;
    }

    public Context getContext() {
        return sContext;
    }

    public IconCache getIconCache() {
        return this.mIconCache;
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return this.mInvariantDeviceProfile;
    }

    public LauncherModel getModel() {
        return this.mModel;
    }

    public WidgetPreviewLoader getWidgetCache() {
        return this.mWidgetCache;
    }

    public boolean hasWallpaperChangedSinceLastCheck() {
        boolean z = this.mWallpaperChangedSinceLastCheck;
        this.mWallpaperChangedSinceLastCheck = false;
        return z;
    }

    public void onWallpaperChanged() {
        this.mWallpaperChangedSinceLastCheck = true;
    }

    public void reloadWorkspace() {
        this.mModel.resetLoadedState(false, true);
        this.mModel.startLoaderFromBackground();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LauncherModel setLauncher(Launcher launcher) {
        getLauncherProvider().setLauncherProviderChangeListener(launcher);
        this.mModel.initialize(launcher);
        LauncherAccessibilityDelegate launcherAccessibilityDelegate = null;
        if (launcher != null) {
            launcherAccessibilityDelegate = null;
            if (Utilities.ATLEAST_LOLLIPOP) {
                launcherAccessibilityDelegate = new LauncherAccessibilityDelegate(launcher);
            }
        }
        this.mAccessibilityDelegate = launcherAccessibilityDelegate;
        return this.mModel;
    }
}
