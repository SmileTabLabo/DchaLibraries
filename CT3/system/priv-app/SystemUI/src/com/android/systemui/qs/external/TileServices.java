package com.android.systemui.qs.external;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.quicksettings.IQSService;
import android.service.quicksettings.Tile;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
/* loaded from: a.zip:com/android/systemui/qs/external/TileServices.class */
public class TileServices extends IQSService.Stub {
    private static final Comparator<TileServiceManager> SERVICE_SORT = new Comparator<TileServiceManager>() { // from class: com.android.systemui.qs.external.TileServices.2
        @Override // java.util.Comparator
        public int compare(TileServiceManager tileServiceManager, TileServiceManager tileServiceManager2) {
            return -Integer.compare(tileServiceManager.getBindPriority(), tileServiceManager2.getBindPriority());
        }
    };
    private final Context mContext;
    private final Handler mHandler;
    private final QSTileHost mHost;
    private final Handler mMainHandler;
    private final ArrayMap<CustomTile, TileServiceManager> mServices = new ArrayMap<>();
    private final ArrayMap<ComponentName, CustomTile> mTiles = new ArrayMap<>();
    private int mMaxBound = 3;
    private final BroadcastReceiver mRequestListeningReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.qs.external.TileServices.1
        final TileServices this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.service.quicksettings.action.REQUEST_LISTENING".equals(intent.getAction())) {
                this.this$0.requestListening((ComponentName) intent.getParcelableExtra("android.service.quicksettings.extra.COMPONENT"));
            }
        }
    };

    public TileServices(QSTileHost qSTileHost, Looper looper) {
        this.mHost = qSTileHost;
        this.mContext = this.mHost.getContext();
        this.mContext.registerReceiver(this.mRequestListeningReceiver, new IntentFilter("android.service.quicksettings.action.REQUEST_LISTENING"));
        this.mHandler = new Handler(looper);
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    private CustomTile getTileForComponent(ComponentName componentName) {
        CustomTile customTile;
        synchronized (this.mServices) {
            customTile = this.mTiles.get(componentName);
        }
        return customTile;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestListening(ComponentName componentName) {
        synchronized (this.mServices) {
            CustomTile tileForComponent = getTileForComponent(componentName);
            if (tileForComponent == null) {
                Log.d("TileServices", "Couldn't find tile for " + componentName);
                return;
            }
            TileServiceManager tileServiceManager = this.mServices.get(tileForComponent);
            if (tileServiceManager.isActiveTile()) {
                tileServiceManager.setBindRequested(true);
                try {
                    tileServiceManager.getTileService().onStartListening();
                } catch (RemoteException e) {
                }
            }
        }
    }

    private void verifyCaller(String str) {
        try {
            if (Binder.getCallingUid() != this.mContext.getPackageManager().getPackageUidAsUser(str, Binder.getCallingUserHandle().getIdentifier())) {
                throw new SecurityException("Component outside caller's uid");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new SecurityException(e);
        }
    }

    public void freeService(CustomTile customTile, TileServiceManager tileServiceManager) {
        synchronized (this.mServices) {
            tileServiceManager.setBindAllowed(false);
            tileServiceManager.handleDestroy();
            this.mServices.remove(customTile);
            this.mTiles.remove(customTile.getComponent());
            this.mMainHandler.post(new Runnable(this, customTile.getComponent().getClassName()) { // from class: com.android.systemui.qs.external.TileServices.3
                final TileServices this$0;
                final String val$slot;

                {
                    this.this$0 = this;
                    this.val$slot = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mHost.getIconController().removeIcon(this.val$slot);
                }
            });
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public Tile getTile(ComponentName componentName) {
        verifyCaller(componentName.getPackageName());
        CustomTile tileForComponent = getTileForComponent(componentName);
        if (tileForComponent != null) {
            return tileForComponent.getQsTile();
        }
        return null;
    }

    public TileServiceManager getTileWrapper(CustomTile customTile) {
        ComponentName component = customTile.getComponent();
        TileServiceManager onCreateTileService = onCreateTileService(component, customTile.getQsTile());
        synchronized (this.mServices) {
            this.mServices.put(customTile, onCreateTileService);
            this.mTiles.put(component, customTile);
        }
        return onCreateTileService;
    }

    public boolean isLocked() {
        return this.mHost.getKeyguardMonitor().isShowing();
    }

    public boolean isSecure() {
        KeyguardMonitor keyguardMonitor = this.mHost.getKeyguardMonitor();
        return keyguardMonitor.isSecure() ? keyguardMonitor.isShowing() : false;
    }

    protected TileServiceManager onCreateTileService(ComponentName componentName, Tile tile) {
        return new TileServiceManager(this, this.mHandler, componentName, tile);
    }

    public void onDialogHidden(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        verifyCaller(componentName.getPackageName());
        CustomTile tileForComponent = getTileForComponent(componentName);
        if (tileForComponent != null) {
            this.mServices.get(tileForComponent).setShowingDialog(false);
            tileForComponent.onDialogHidden();
        }
    }

    public void onShowDialog(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        verifyCaller(componentName.getPackageName());
        CustomTile tileForComponent = getTileForComponent(componentName);
        if (tileForComponent != null) {
            tileForComponent.onDialogShown();
            this.mHost.collapsePanels();
            this.mServices.get(tileForComponent).setShowingDialog(true);
        }
    }

    public void onStartActivity(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        verifyCaller(componentName.getPackageName());
        if (getTileForComponent(componentName) != null) {
            this.mHost.collapsePanels();
        }
    }

    public void onStartSuccessful(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        verifyCaller(componentName.getPackageName());
        CustomTile tileForComponent = getTileForComponent(componentName);
        if (tileForComponent != null) {
            synchronized (this.mServices) {
                this.mServices.get(tileForComponent).clearPendingBind();
            }
            tileForComponent.refreshState();
        }
    }

    public void recalculateBindAllowance() {
        ArrayList arrayList;
        int i;
        synchronized (this.mServices) {
            arrayList = new ArrayList(this.mServices.values());
        }
        int size = arrayList.size();
        if (size > this.mMaxBound) {
            long currentTimeMillis = System.currentTimeMillis();
            for (int i2 = 0; i2 < size; i2++) {
                ((TileServiceManager) arrayList.get(i2)).calculateBindPriority(currentTimeMillis);
            }
            Collections.sort(arrayList, SERVICE_SORT);
        }
        int i3 = 0;
        while (true) {
            i = i3;
            if (i3 >= this.mMaxBound) {
                break;
            }
            i = i3;
            if (i3 >= size) {
                break;
            }
            ((TileServiceManager) arrayList.get(i3)).setBindAllowed(true);
            i3++;
        }
        while (i < size) {
            ((TileServiceManager) arrayList.get(i)).setBindAllowed(false);
            i++;
        }
    }

    public void startUnlockAndRun(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        verifyCaller(componentName.getPackageName());
        CustomTile tileForComponent = getTileForComponent(componentName);
        if (tileForComponent != null) {
            tileForComponent.startUnlockAndRun();
        }
    }

    public void updateQsTile(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        verifyCaller(componentName.getPackageName());
        CustomTile tileForComponent = getTileForComponent(componentName);
        if (tileForComponent != null) {
            synchronized (this.mServices) {
                TileServiceManager tileServiceManager = this.mServices.get(tileForComponent);
                tileServiceManager.clearPendingBind();
                tileServiceManager.setLastUpdate(System.currentTimeMillis());
            }
            tileForComponent.updateState(tile);
            tileForComponent.refreshState();
        }
    }

    public void updateStatusIcon(Tile tile, Icon icon, String str) {
        ComponentName componentName = tile.getComponentName();
        String packageName = componentName.getPackageName();
        verifyCaller(packageName);
        if (getTileForComponent(componentName) != null) {
            try {
                UserHandle callingUserHandle = getCallingUserHandle();
                if (this.mContext.getPackageManager().getPackageInfoAsUser(packageName, 0, callingUserHandle.getIdentifier()).applicationInfo.isSystemApp()) {
                    this.mMainHandler.post(new Runnable(this, componentName, icon != null ? new StatusBarIcon(callingUserHandle, packageName, icon, 0, 0, str) : null) { // from class: com.android.systemui.qs.external.TileServices.4
                        final TileServices this$0;
                        final ComponentName val$componentName;
                        final StatusBarIcon val$statusIcon;

                        {
                            this.this$0 = this;
                            this.val$componentName = componentName;
                            this.val$statusIcon = r6;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            StatusBarIconController iconController = this.this$0.mHost.getIconController();
                            iconController.setIcon(this.val$componentName.getClassName(), this.val$statusIcon);
                            iconController.setExternalIcon(this.val$componentName.getClassName());
                        }
                    });
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
    }
}
