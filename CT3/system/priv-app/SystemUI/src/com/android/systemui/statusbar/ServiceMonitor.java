package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import java.util.Arrays;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/ServiceMonitor.class */
public class ServiceMonitor {
    private boolean mBound;
    private final Callbacks mCallbacks;
    private final Context mContext;
    private final boolean mDebug;
    private SC mServiceConnection;
    private ComponentName mServiceName;
    private final String mSettingKey;
    private final String mTag;
    private final Handler mHandler = new Handler(this) { // from class: com.android.systemui.statusbar.ServiceMonitor.1
        final ServiceMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.this$0.startService();
                    return;
                case 2:
                    this.this$0.continueStartService();
                    return;
                case 3:
                    this.this$0.stopService();
                    return;
                case 4:
                    this.this$0.packageIntent((Intent) message.obj);
                    return;
                case 5:
                    this.this$0.checkBound();
                    return;
                case 6:
                    this.this$0.serviceDisconnected((ComponentName) message.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private final ContentObserver mSettingObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.statusbar.ServiceMonitor.2
        final ServiceMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            onChange(z, null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (this.this$0.mDebug) {
                Log.d(this.this$0.mTag, "onChange selfChange=" + z + " uri=" + uri);
            }
            ComponentName componentNameFromSetting = this.this$0.getComponentNameFromSetting();
            if (!(componentNameFromSetting == null && this.this$0.mServiceName == null) && (componentNameFromSetting == null || !componentNameFromSetting.equals(this.this$0.mServiceName))) {
                if (this.this$0.mBound) {
                    this.this$0.mHandler.sendEmptyMessage(3);
                }
                this.this$0.mHandler.sendEmptyMessageDelayed(1, 500L);
            } else if (this.this$0.mDebug) {
                Log.d(this.this$0.mTag, "skipping no-op restart");
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.ServiceMonitor.3
        final ServiceMonitor this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String schemeSpecificPart = intent.getData().getSchemeSpecificPart();
            if (this.this$0.mServiceName == null || !this.this$0.mServiceName.getPackageName().equals(schemeSpecificPart)) {
                return;
            }
            this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(4, intent));
        }
    };

    /* loaded from: a.zip:com/android/systemui/statusbar/ServiceMonitor$Callbacks.class */
    public interface Callbacks {
        void onNoService();

        long onServiceStartAttempt();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/ServiceMonitor$SC.class */
    public final class SC implements ServiceConnection, IBinder.DeathRecipient {
        private ComponentName mName;
        private IBinder mService;
        final ServiceMonitor this$0;

        private SC(ServiceMonitor serviceMonitor) {
            this.this$0 = serviceMonitor;
        }

        /* synthetic */ SC(ServiceMonitor serviceMonitor, SC sc) {
            this(serviceMonitor);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            if (this.this$0.mDebug) {
                Log.d(this.this$0.mTag, "binderDied");
            }
            this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(6, this.mName));
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (this.this$0.mDebug) {
                Log.d(this.this$0.mTag, "onServiceConnected name=" + componentName + " service=" + iBinder);
            }
            this.mName = componentName;
            this.mService = iBinder;
            try {
                iBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Log.w(this.this$0.mTag, "Error linking to death", e);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (this.this$0.mDebug) {
                Log.d(this.this$0.mTag, "onServiceDisconnected name=" + componentName);
            }
            boolean unlinkToDeath = this.mService.unlinkToDeath(this, 0);
            if (this.this$0.mDebug) {
                Log.d(this.this$0.mTag, "  unlinked=" + unlinkToDeath);
            }
            this.this$0.mHandler.sendMessage(this.this$0.mHandler.obtainMessage(6, this.mName));
        }
    }

    public ServiceMonitor(String str, boolean z, Context context, String str2, Callbacks callbacks) {
        this.mTag = str + ".ServiceMonitor";
        this.mDebug = z;
        this.mContext = context;
        this.mSettingKey = str2;
        this.mCallbacks = callbacks;
    }

    private static String bundleToString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(123);
        for (String str : bundle.keySet()) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            Object obj = bundle.get(str);
            List list = obj;
            if (obj instanceof String[]) {
                list = Arrays.asList((String[]) obj);
            }
            sb.append(str).append('=').append(list);
        }
        return sb.append('}').toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkBound() {
        if (this.mDebug) {
            Log.d(this.mTag, "checkBound mBound=" + this.mBound);
        }
        if (this.mBound) {
            return;
        }
        startService();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void continueStartService() {
        if (this.mDebug) {
            Log.d(this.mTag, "continueStartService");
        }
        Intent component = new Intent().setComponent(this.mServiceName);
        try {
            this.mServiceConnection = new SC(this, null);
            this.mBound = this.mContext.bindService(component, this.mServiceConnection, 1);
            if (this.mDebug) {
                Log.d(this.mTag, "mBound: " + this.mBound);
            }
        } catch (Throwable th) {
            Log.w(this.mTag, "Error binding to service: " + this.mServiceName, th);
        }
        if (this.mBound) {
            return;
        }
        this.mCallbacks.onNoService();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ComponentName getComponentNameFromSetting() {
        ComponentName componentName = null;
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), this.mSettingKey, -2);
        if (stringForUser != null) {
            componentName = ComponentName.unflattenFromString(stringForUser);
        }
        return componentName;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void packageIntent(Intent intent) {
        if (this.mDebug) {
            Log.d(this.mTag, "packageIntent intent=" + intent + " extras=" + bundleToString(intent.getExtras()));
        }
        if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
            this.mHandler.sendEmptyMessage(1);
        } else if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction()) || "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
            PackageManager packageManager = this.mContext.getPackageManager();
            boolean z = (!isPackageAvailable() || packageManager.getApplicationEnabledSetting(this.mServiceName.getPackageName()) == 2) ? false : packageManager.getComponentEnabledSetting(this.mServiceName) != 2;
            if (this.mBound && !z) {
                stopService();
                scheduleCheckBound();
            } else if (this.mBound || !z) {
            } else {
                startService();
            }
        }
    }

    private void scheduleCheckBound() {
        this.mHandler.removeMessages(5);
        this.mHandler.sendEmptyMessageDelayed(5, 2000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void serviceDisconnected(ComponentName componentName) {
        if (this.mDebug) {
            Log.d(this.mTag, "serviceDisconnected serviceName=" + componentName + " mServiceName=" + this.mServiceName);
        }
        if (componentName.equals(this.mServiceName)) {
            this.mBound = false;
            scheduleCheckBound();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startService() {
        this.mServiceName = getComponentNameFromSetting();
        if (this.mDebug) {
            Log.d(this.mTag, "startService mServiceName=" + this.mServiceName);
        }
        if (this.mServiceName == null) {
            this.mBound = false;
            this.mCallbacks.onNoService();
            return;
        }
        this.mHandler.sendEmptyMessageDelayed(2, this.mCallbacks.onServiceStartAttempt());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopService() {
        if (this.mDebug) {
            Log.d(this.mTag, "stopService");
        }
        boolean stopService = this.mContext.stopService(new Intent().setComponent(this.mServiceName));
        if (this.mDebug) {
            Log.d(this.mTag, "  stopped=" + stopService);
        }
        this.mContext.unbindService(this.mServiceConnection);
        this.mBound = false;
    }

    public ComponentName getComponent() {
        return getComponentNameFromSetting();
    }

    public boolean isPackageAvailable() {
        ComponentName component = getComponent();
        if (component == null) {
            return false;
        }
        try {
            return this.mContext.getPackageManager().isPackageAvailable(component.getPackageName());
        } catch (RuntimeException e) {
            Log.w(this.mTag, "Error checking package availability", e);
            return false;
        }
    }

    public void setComponent(ComponentName componentName) {
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), this.mSettingKey, componentName == null ? null : componentName.flattenToShortString(), -2);
    }

    public void start() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(this.mSettingKey), false, this.mSettingObserver, -1);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mHandler.sendEmptyMessage(1);
    }
}
