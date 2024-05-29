package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.UserHandle;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.systemui.qs.external.TileLifecycleManager;
import java.util.ArrayList;
import java.util.List;
import libcore.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/external/TileServiceManager.class */
public class TileServiceManager {
    @VisibleForTesting
    static final String PREFS_FILE = "CustomTileModes";
    private boolean mBindAllowed;
    private boolean mBindRequested;
    private boolean mBound;
    private final Handler mHandler;
    private boolean mJustBound;
    @VisibleForTesting
    final Runnable mJustBoundOver;
    private long mLastUpdate;
    private boolean mPendingBind;
    private int mPriority;
    private final TileServices mServices;
    private boolean mShowingDialog;
    private final TileLifecycleManager mStateManager;
    private final Runnable mUnbind;
    private final BroadcastReceiver mUninstallReceiver;

    /* JADX INFO: Access modifiers changed from: package-private */
    public TileServiceManager(TileServices tileServices, Handler handler, ComponentName componentName, Tile tile) {
        this(tileServices, handler, new TileLifecycleManager(handler, tileServices.getContext(), tileServices, tile, new Intent().setComponent(componentName), new UserHandle(ActivityManager.getCurrentUser())));
    }

    @VisibleForTesting
    TileServiceManager(TileServices tileServices, Handler handler, TileLifecycleManager tileLifecycleManager) {
        this.mPendingBind = true;
        this.mUnbind = new Runnable(this) { // from class: com.android.systemui.qs.external.TileServiceManager.1
            final TileServiceManager this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (!this.this$0.mBound || this.this$0.mBindRequested) {
                    return;
                }
                this.this$0.unbindService();
            }
        };
        this.mJustBoundOver = new Runnable(this) { // from class: com.android.systemui.qs.external.TileServiceManager.2
            final TileServiceManager this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mJustBound = false;
                this.this$0.mServices.recalculateBindAllowance();
            }
        };
        this.mUninstallReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.qs.external.TileServiceManager.3
            final TileServiceManager this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                    String encodedSchemeSpecificPart = intent.getData().getEncodedSchemeSpecificPart();
                    ComponentName component = this.this$0.mStateManager.getComponent();
                    if (Objects.equal(encodedSchemeSpecificPart, component.getPackageName())) {
                        if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                            Intent intent2 = new Intent("android.service.quicksettings.action.QS_TILE");
                            intent2.setPackage(encodedSchemeSpecificPart);
                            List<ResolveInfo> queryIntentServicesAsUser = context.getPackageManager().queryIntentServicesAsUser(intent2, 0, ActivityManager.getCurrentUser());
                            if (BenesseExtension.getDchaState() != 0) {
                                queryIntentServicesAsUser = new ArrayList();
                            }
                            for (ResolveInfo resolveInfo : queryIntentServicesAsUser) {
                                if (Objects.equal(resolveInfo.serviceInfo.packageName, component.getPackageName()) && Objects.equal(resolveInfo.serviceInfo.name, component.getClassName())) {
                                    return;
                                }
                            }
                        }
                        this.this$0.mServices.getHost().removeTile(component);
                    }
                }
            }
        };
        this.mServices = tileServices;
        this.mHandler = handler;
        this.mStateManager = tileLifecycleManager;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mServices.getContext().registerReceiverAsUser(this.mUninstallReceiver, new UserHandle(ActivityManager.getCurrentUser()), intentFilter, null, this.mHandler);
    }

    private void bindService() {
        if (this.mBound) {
            Log.e("TileServiceManager", "Service already bound");
            return;
        }
        this.mPendingBind = true;
        this.mBound = true;
        this.mJustBound = true;
        this.mHandler.postDelayed(this.mJustBoundOver, 5000L);
        this.mStateManager.setBindService(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unbindService() {
        if (!this.mBound) {
            Log.e("TileServiceManager", "Service not bound");
            return;
        }
        this.mBound = false;
        this.mJustBound = false;
        this.mStateManager.setBindService(false);
    }

    public void calculateBindPriority(long j) {
        if (this.mStateManager.hasPendingClick()) {
            this.mPriority = Integer.MAX_VALUE;
        } else if (this.mShowingDialog) {
            this.mPriority = 2147483646;
        } else if (this.mJustBound) {
            this.mPriority = 2147483645;
        } else if (!this.mBindRequested) {
            this.mPriority = Integer.MIN_VALUE;
        } else {
            long j2 = j - this.mLastUpdate;
            if (j2 > 2147483644) {
                this.mPriority = 2147483644;
            } else {
                this.mPriority = (int) j2;
            }
        }
    }

    public void clearPendingBind() {
        this.mPendingBind = false;
    }

    public int getBindPriority() {
        return this.mPriority;
    }

    public IQSTileService getTileService() {
        return this.mStateManager;
    }

    public void handleDestroy() {
        this.mServices.getContext().unregisterReceiver(this.mUninstallReceiver);
        this.mStateManager.handleDestroy();
    }

    public boolean hasPendingBind() {
        return this.mPendingBind;
    }

    public boolean isActiveTile() {
        return this.mStateManager.isActiveTile();
    }

    public void setBindAllowed(boolean z) {
        if (this.mBindAllowed == z) {
            return;
        }
        this.mBindAllowed = z;
        if (!this.mBindAllowed && this.mBound) {
            unbindService();
        } else if (this.mBindAllowed && this.mBindRequested && !this.mBound) {
            bindService();
        }
    }

    public void setBindRequested(boolean z) {
        if (this.mBindRequested == z) {
            return;
        }
        this.mBindRequested = z;
        if (this.mBindAllowed && this.mBindRequested && !this.mBound) {
            this.mHandler.removeCallbacks(this.mUnbind);
            bindService();
        } else {
            this.mServices.recalculateBindAllowance();
        }
        if (!this.mBound || this.mBindRequested) {
            return;
        }
        this.mHandler.postDelayed(this.mUnbind, 30000L);
    }

    public void setLastUpdate(long j) {
        this.mLastUpdate = j;
        if (this.mBound && isActiveTile()) {
            this.mStateManager.onStopListening();
            setBindRequested(false);
        }
        this.mServices.recalculateBindAllowance();
    }

    public void setShowingDialog(boolean z) {
        this.mShowingDialog = z;
    }

    public void setTileChangeListener(TileLifecycleManager.TileChangeListener tileChangeListener) {
        this.mStateManager.setTileChangeListener(tileChangeListener);
    }
}
