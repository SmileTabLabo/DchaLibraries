package com.android.systemui.qs.external;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.quicksettings.IQSService;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.support.annotation.VisibleForTesting;
import android.util.ArraySet;
import android.util.Log;
import java.util.Set;
import libcore.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/external/TileLifecycleManager.class */
public class TileLifecycleManager extends BroadcastReceiver implements IQSTileService, ServiceConnection, IBinder.DeathRecipient {
    private int mBindTryCount;
    private boolean mBound;
    private TileChangeListener mChangeListener;
    private IBinder mClickBinder;
    private final Context mContext;
    private final Handler mHandler;
    private final Intent mIntent;
    private boolean mIsBound;
    private boolean mListening;
    private Set<Integer> mQueuedMessages = new ArraySet();
    @VisibleForTesting
    boolean mReceiverRegistered;
    private boolean mUnbindImmediate;
    private final UserHandle mUser;
    private QSTileServiceWrapper mWrapper;

    /* loaded from: a.zip:com/android/systemui/qs/external/TileLifecycleManager$TileChangeListener.class */
    public interface TileChangeListener {
        void onTileChanged(ComponentName componentName);
    }

    public TileLifecycleManager(Handler handler, Context context, IQSService iQSService, Tile tile, Intent intent, UserHandle userHandle) {
        this.mContext = context;
        this.mHandler = handler;
        this.mIntent = intent;
        this.mIntent.putExtra("service", iQSService.asBinder());
        this.mIntent.putExtra("android.service.quicksettings.extra.COMPONENT", intent.getComponent());
        this.mUser = userHandle;
    }

    private boolean checkComponentState() {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (isPackageAvailable(packageManager) && isComponentAvailable(packageManager)) {
            return true;
        }
        startPackageListening();
        return false;
    }

    private void handleDeath() {
        if (this.mWrapper == null) {
            return;
        }
        this.mWrapper = null;
        if (this.mBound && checkComponentState()) {
            this.mHandler.postDelayed(new Runnable(this) { // from class: com.android.systemui.qs.external.TileLifecycleManager.1
                final TileLifecycleManager this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.this$0.mBound) {
                        this.this$0.setBindService(true);
                    }
                }
            }, 1000L);
        }
    }

    private void handlePendingMessages() {
        ArraySet arraySet;
        synchronized (this.mQueuedMessages) {
            arraySet = new ArraySet(this.mQueuedMessages);
            this.mQueuedMessages.clear();
        }
        if (arraySet.contains(0)) {
            onTileAdded();
        }
        if (this.mListening) {
            onStartListening();
        }
        if (arraySet.contains(2)) {
            if (this.mListening) {
                onClick(this.mClickBinder);
            } else {
                Log.w("TileLifecycleManager", "Managed to get click on non-listening state...");
            }
        }
        if (arraySet.contains(3)) {
            if (this.mListening) {
                onUnlockComplete();
            } else {
                Log.w("TileLifecycleManager", "Managed to get unlock on non-listening state...");
            }
        }
        if (arraySet.contains(1)) {
            if (this.mListening) {
                Log.w("TileLifecycleManager", "Managed to get remove in listening state...");
                onStopListening();
            }
            onTileRemoved();
        }
        if (this.mUnbindImmediate) {
            this.mUnbindImmediate = false;
            setBindService(false);
        }
    }

    private boolean isComponentAvailable(PackageManager packageManager) {
        boolean z = false;
        this.mIntent.getComponent().getPackageName();
        try {
            if (AppGlobals.getPackageManager().getServiceInfo(this.mIntent.getComponent(), 0, this.mUser.getIdentifier()) != null) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean isPackageAvailable(PackageManager packageManager) {
        String packageName = this.mIntent.getComponent().getPackageName();
        try {
            packageManager.getPackageInfoAsUser(packageName, 0, this.mUser.getIdentifier());
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("TileLifecycleManager", "Package not available: " + packageName);
            return false;
        }
    }

    private void queueMessage(int i) {
        synchronized (this.mQueuedMessages) {
            this.mQueuedMessages.add(Integer.valueOf(i));
        }
    }

    private void startPackageListening() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this, this.mUser, intentFilter, null, this.mHandler);
        this.mContext.registerReceiverAsUser(this, this.mUser, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, this.mHandler);
        this.mReceiverRegistered = true;
    }

    private void stopPackageListening() {
        this.mContext.unregisterReceiver(this);
        this.mReceiverRegistered = false;
    }

    public IBinder asBinder() {
        IBinder iBinder = null;
        if (this.mWrapper != null) {
            iBinder = this.mWrapper.asBinder();
        }
        return iBinder;
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        handleDeath();
    }

    public void flushMessagesAndUnbind() {
        this.mUnbindImmediate = true;
        setBindService(true);
    }

    public ComponentName getComponent() {
        return this.mIntent.getComponent();
    }

    public void handleDestroy() {
        if (this.mReceiverRegistered) {
            stopPackageListening();
        }
    }

    public boolean hasPendingClick() {
        boolean contains;
        synchronized (this.mQueuedMessages) {
            contains = this.mQueuedMessages.contains(2);
        }
        return contains;
    }

    public boolean isActiveTile() {
        boolean z = false;
        try {
            ServiceInfo serviceInfo = this.mContext.getPackageManager().getServiceInfo(this.mIntent.getComponent(), 8320);
            if (serviceInfo.metaData != null) {
                z = serviceInfo.metaData.getBoolean("android.service.quicksettings.ACTIVE_TILE", false);
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void onClick(IBinder iBinder) {
        if (this.mWrapper == null || !this.mWrapper.onClick(iBinder)) {
            this.mClickBinder = iBinder;
            queueMessage(2);
            handleDeath();
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) || Objects.equal(intent.getData().getEncodedSchemeSpecificPart(), this.mIntent.getComponent().getPackageName())) {
            if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction()) && this.mChangeListener != null) {
                this.mChangeListener.onTileChanged(this.mIntent.getComponent());
            }
            stopPackageListening();
            if (this.mBound) {
                setBindService(true);
            }
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        this.mBindTryCount = 0;
        QSTileServiceWrapper qSTileServiceWrapper = new QSTileServiceWrapper(IQSTileService.Stub.asInterface(iBinder));
        try {
            iBinder.linkToDeath(this, 0);
        } catch (RemoteException e) {
        }
        this.mWrapper = qSTileServiceWrapper;
        handlePendingMessages();
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName componentName) {
        handleDeath();
    }

    public void onStartListening() {
        this.mListening = true;
        if (this.mWrapper == null || this.mWrapper.onStartListening()) {
            return;
        }
        handleDeath();
    }

    public void onStopListening() {
        this.mListening = false;
        if (this.mWrapper == null || this.mWrapper.onStopListening()) {
            return;
        }
        handleDeath();
    }

    public void onTileAdded() {
        if (this.mWrapper == null || !this.mWrapper.onTileAdded()) {
            queueMessage(0);
            handleDeath();
        }
    }

    public void onTileRemoved() {
        if (this.mWrapper == null || !this.mWrapper.onTileRemoved()) {
            queueMessage(1);
            handleDeath();
        }
    }

    public void onUnlockComplete() {
        if (this.mWrapper == null || !this.mWrapper.onUnlockComplete()) {
            queueMessage(3);
            handleDeath();
        }
    }

    public void setBindService(boolean z) {
        this.mBound = z;
        if (!z) {
            this.mBindTryCount = 0;
            this.mWrapper = null;
            if (this.mIsBound) {
                this.mContext.unbindService(this);
                this.mIsBound = false;
            }
        } else if (this.mBindTryCount == 5) {
            startPackageListening();
        } else if (checkComponentState()) {
            this.mBindTryCount++;
            try {
                this.mIsBound = this.mContext.bindServiceAsUser(this.mIntent, this, 33554433, this.mUser);
            } catch (SecurityException e) {
                Log.e("TileLifecycleManager", "Failed to bind to service", e);
                this.mIsBound = false;
            }
        }
    }

    public void setTileChangeListener(TileChangeListener tileChangeListener) {
        this.mChangeListener = tileChangeListener;
    }
}
