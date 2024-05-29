package com.android.systemui.plugins;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.PluginInstanceManager;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import dalvik.system.PathClassLoader;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.Thread;
import java.util.Map;
/* loaded from: classes.dex */
public class PluginManagerImpl extends BroadcastReceiver implements PluginManager {
    private final boolean isDebuggable;
    private final Map<String, ClassLoader> mClassLoaders;
    private final Context mContext;
    private final PluginInstanceManagerFactory mFactory;
    private boolean mHasOneShot;
    private boolean mListening;
    private Looper mLooper;
    private final ArraySet<String> mOneShotPackages;
    private ClassLoaderFilter mParentClassLoader;
    private final ArrayMap<PluginListener<?>, PluginInstanceManager> mPluginMap;
    private final PluginPrefs mPluginPrefs;
    private boolean mWtfsSet;

    public PluginManagerImpl(Context context) {
        this(context, new PluginInstanceManagerFactory(), Build.IS_DEBUGGABLE, Thread.getUncaughtExceptionPreHandler());
    }

    @VisibleForTesting
    PluginManagerImpl(Context context, PluginInstanceManagerFactory pluginInstanceManagerFactory, boolean z, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.mPluginMap = new ArrayMap<>();
        this.mClassLoaders = new ArrayMap();
        this.mOneShotPackages = new ArraySet<>();
        this.mContext = context;
        this.mFactory = pluginInstanceManagerFactory;
        this.mLooper = (Looper) Dependency.get(Dependency.BG_LOOPER);
        this.isDebuggable = z;
        this.mPluginPrefs = new PluginPrefs(this.mContext);
        Thread.setUncaughtExceptionPreHandler(new PluginExceptionHandler(uncaughtExceptionHandler));
        if (this.isDebuggable) {
            new Handler(this.mLooper).post(new Runnable() { // from class: com.android.systemui.plugins.-$$Lambda$PluginManagerImpl$DDmEEnITAENQNECHR1R9V5n2bfQ
                @Override // java.lang.Runnable
                public final void run() {
                    ((PluginDependencyProvider) Dependency.get(PluginDependencyProvider.class)).allowPluginDependency(ActivityStarter.class);
                }
            });
        }
    }

    @Override // com.android.systemui.plugins.PluginManager
    public <T extends Plugin> T getOneShotPlugin(Class<T> cls) {
        ProvidesInterface providesInterface = (ProvidesInterface) cls.getDeclaredAnnotation(ProvidesInterface.class);
        if (providesInterface == null) {
            throw new RuntimeException(cls + " doesn't provide an interface");
        } else if (TextUtils.isEmpty(providesInterface.action())) {
            throw new RuntimeException(cls + " doesn't provide an action");
        } else {
            return (T) getOneShotPlugin(providesInterface.action(), cls);
        }
    }

    @Override // com.android.systemui.plugins.PluginManager
    public <T extends Plugin> T getOneShotPlugin(String str, Class<?> cls) {
        if (this.isDebuggable) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw new RuntimeException("Must be called from UI thread");
            }
            PluginInstanceManager createPluginInstanceManager = this.mFactory.createPluginInstanceManager(this.mContext, str, null, false, this.mLooper, cls, this);
            this.mPluginPrefs.addAction(str);
            PluginInstanceManager.PluginInfo<T> plugin = createPluginInstanceManager.getPlugin();
            if (plugin != null) {
                this.mOneShotPackages.add(plugin.mPackage);
                this.mHasOneShot = true;
                startListening();
                return plugin.mPlugin;
            }
            return null;
        }
        return null;
    }

    @Override // com.android.systemui.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(PluginListener<T> pluginListener, Class<?> cls) {
        addPluginListener((PluginListener) pluginListener, cls, false);
    }

    @Override // com.android.systemui.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(PluginListener<T> pluginListener, Class<?> cls, boolean z) {
        addPluginListener(PluginManager.getAction(cls), pluginListener, cls, z);
    }

    @Override // com.android.systemui.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(String str, PluginListener<T> pluginListener, Class<?> cls) {
        addPluginListener(str, pluginListener, cls, false);
    }

    @Override // com.android.systemui.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(String str, PluginListener<T> pluginListener, Class cls, boolean z) {
        if (!this.isDebuggable) {
            return;
        }
        this.mPluginPrefs.addAction(str);
        PluginInstanceManager createPluginInstanceManager = this.mFactory.createPluginInstanceManager(this.mContext, str, pluginListener, z, this.mLooper, cls, this);
        createPluginInstanceManager.loadAll();
        this.mPluginMap.put(pluginListener, createPluginInstanceManager);
        startListening();
    }

    @Override // com.android.systemui.plugins.PluginManager
    public void removePluginListener(PluginListener<?> pluginListener) {
        if (this.isDebuggable && this.mPluginMap.containsKey(pluginListener)) {
            this.mPluginMap.remove(pluginListener).destroy();
            if (this.mPluginMap.size() == 0) {
                stopListening();
            }
        }
    }

    private void startListening() {
        if (this.mListening) {
            return;
        }
        this.mListening = true;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this, intentFilter);
        intentFilter.addAction(PluginManager.PLUGIN_CHANGED);
        intentFilter.addAction("com.android.systemui.action.DISABLE_PLUGIN");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this, intentFilter, PluginInstanceManager.PLUGIN_PERMISSION, null);
        this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.USER_UNLOCKED"));
    }

    private void stopListening() {
        if (!this.mListening || this.mHasOneShot) {
            return;
        }
        this.mListening = false;
        this.mContext.unregisterReceiver(this);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String str;
        if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
            for (PluginInstanceManager pluginInstanceManager : this.mPluginMap.values()) {
                pluginInstanceManager.loadAll();
            }
        } else if ("com.android.systemui.action.DISABLE_PLUGIN".equals(intent.getAction())) {
            ComponentName unflattenFromString = ComponentName.unflattenFromString(intent.getData().toString().substring(10));
            this.mContext.getPackageManager().setComponentEnabledSetting(unflattenFromString, 2, 1);
            ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).cancel(unflattenFromString.getClassName(), 6);
        } else {
            String encodedSchemeSpecificPart = intent.getData().getEncodedSchemeSpecificPart();
            if (this.mOneShotPackages.contains(encodedSchemeSpecificPart)) {
                int identifier = this.mContext.getResources().getIdentifier("tuner", "drawable", this.mContext.getPackageName());
                int identifier2 = Resources.getSystem().getIdentifier("system_notification_accent_color", "color", "android");
                try {
                    PackageManager packageManager = this.mContext.getPackageManager();
                    str = packageManager.getApplicationInfo(encodedSchemeSpecificPart, 0).loadLabel(packageManager).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    str = encodedSchemeSpecificPart;
                }
                Notification.Builder color = new Notification.Builder(this.mContext, PluginManager.NOTIFICATION_CHANNEL_ID).setSmallIcon(identifier).setWhen(0L).setShowWhen(false).setPriority(2).setVisibility(1).setColor(this.mContext.getColor(identifier2));
                Notification.Builder contentText = color.setContentTitle("Plugin \"" + str + "\" has updated").setContentText("Restart SysUI for changes to take effect.");
                Intent intent2 = new Intent("com.android.systemui.action.RESTART");
                contentText.addAction(new Notification.Action.Builder((Icon) null, "Restart SysUI", PendingIntent.getBroadcast(this.mContext, 0, intent2.setData(Uri.parse("package://" + encodedSchemeSpecificPart)), 0)).build());
                ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).notifyAsUser(encodedSchemeSpecificPart, 6, contentText.build(), UserHandle.ALL);
            }
            if (clearClassLoader(encodedSchemeSpecificPart)) {
                Context context2 = this.mContext;
                Toast.makeText(context2, "Reloading " + encodedSchemeSpecificPart, 1).show();
            }
            if (!"android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                for (PluginInstanceManager pluginInstanceManager2 : this.mPluginMap.values()) {
                    pluginInstanceManager2.onPackageChange(encodedSchemeSpecificPart);
                }
                return;
            }
            for (PluginInstanceManager pluginInstanceManager3 : this.mPluginMap.values()) {
                pluginInstanceManager3.onPackageRemoved(encodedSchemeSpecificPart);
            }
        }
    }

    public ClassLoader getClassLoader(String str, String str2) {
        if (this.mClassLoaders.containsKey(str2)) {
            return this.mClassLoaders.get(str2);
        }
        PathClassLoader pathClassLoader = new PathClassLoader(str, getParentClassLoader());
        this.mClassLoaders.put(str2, pathClassLoader);
        return pathClassLoader;
    }

    private boolean clearClassLoader(String str) {
        return this.mClassLoaders.remove(str) != null;
    }

    ClassLoader getParentClassLoader() {
        if (this.mParentClassLoader == null) {
            this.mParentClassLoader = new ClassLoaderFilter(getClass().getClassLoader(), "com.android.systemui.plugin");
        }
        return this.mParentClassLoader;
    }

    public Context getContext(ApplicationInfo applicationInfo, String str) throws PackageManager.NameNotFoundException {
        return new PluginInstanceManager.PluginContextWrapper(this.mContext.createApplicationContext(applicationInfo, 0), getClassLoader(applicationInfo.sourceDir, str));
    }

    @Override // com.android.systemui.plugins.PluginManager
    public <T> boolean dependsOn(Plugin plugin, Class<T> cls) {
        for (int i = 0; i < this.mPluginMap.size(); i++) {
            if (this.mPluginMap.valueAt(i).dependsOn(plugin, cls)) {
                return true;
            }
        }
        return false;
    }

    public void handleWtfs() {
        if (!this.mWtfsSet) {
            this.mWtfsSet = true;
            Log.setWtfHandler(new Log.TerribleFailureHandler() { // from class: com.android.systemui.plugins.-$$Lambda$PluginManagerImpl$x7_zsW6bYkksPPNk4f5aNW7Etqo
                public final void onTerribleFailure(String str, Log.TerribleFailure terribleFailure, boolean z) {
                    PluginManagerImpl.lambda$handleWtfs$1(PluginManagerImpl.this, str, terribleFailure, z);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$handleWtfs$1(PluginManagerImpl pluginManagerImpl, String str, Log.TerribleFailure terribleFailure, boolean z) {
        throw new CrashWhilePluginActiveException(terribleFailure);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(String.format("  plugin map (%d):", Integer.valueOf(this.mPluginMap.size())));
        for (PluginListener<?> pluginListener : this.mPluginMap.keySet()) {
            printWriter.println(String.format("    %s -> %s", pluginListener, this.mPluginMap.get(pluginListener)));
        }
    }

    @VisibleForTesting
    /* loaded from: classes.dex */
    public static class PluginInstanceManagerFactory {
        public <T extends Plugin> PluginInstanceManager createPluginInstanceManager(Context context, String str, PluginListener<T> pluginListener, boolean z, Looper looper, Class<?> cls, PluginManagerImpl pluginManagerImpl) {
            return new PluginInstanceManager(context, str, pluginListener, z, looper, new VersionInfo().addClass(cls), pluginManagerImpl);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ClassLoaderFilter extends ClassLoader {
        private final ClassLoader mBase;
        private final String mPackage;

        public ClassLoaderFilter(ClassLoader classLoader, String str) {
            super(ClassLoader.getSystemClassLoader());
            this.mBase = classLoader;
            this.mPackage = str;
        }

        @Override // java.lang.ClassLoader
        protected Class<?> loadClass(String str, boolean z) throws ClassNotFoundException {
            if (!str.startsWith(this.mPackage)) {
                super.loadClass(str, z);
            }
            return this.mBase.loadClass(str);
        }
    }

    /* loaded from: classes.dex */
    private class PluginExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler mHandler;

        private PluginExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.mHandler = uncaughtExceptionHandler;
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            if (SystemProperties.getBoolean("plugin.debugging", false)) {
                this.mHandler.uncaughtException(thread, th);
                return;
            }
            boolean checkStack = checkStack(th);
            if (!checkStack) {
                for (PluginInstanceManager pluginInstanceManager : PluginManagerImpl.this.mPluginMap.values()) {
                    checkStack |= pluginInstanceManager.disableAll();
                }
            }
            if (checkStack) {
                th = new CrashWhilePluginActiveException(th);
            }
            this.mHandler.uncaughtException(thread, th);
        }

        private boolean checkStack(Throwable th) {
            StackTraceElement[] stackTrace;
            if (th == null) {
                return false;
            }
            boolean z = false;
            for (StackTraceElement stackTraceElement : th.getStackTrace()) {
                for (PluginInstanceManager pluginInstanceManager : PluginManagerImpl.this.mPluginMap.values()) {
                    z |= pluginInstanceManager.checkAndDisable(stackTraceElement.getClassName());
                }
            }
            return checkStack(th.getCause()) | z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CrashWhilePluginActiveException extends RuntimeException {
        public CrashWhilePluginActiveException(Throwable th) {
            super(th);
        }
    }
}
