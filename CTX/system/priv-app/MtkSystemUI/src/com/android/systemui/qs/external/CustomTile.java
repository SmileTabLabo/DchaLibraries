package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.Objects;
import java.util.function.Supplier;
/* loaded from: classes.dex */
public class CustomTile extends QSTileImpl<QSTile.State> implements TileLifecycleManager.TileChangeListener {
    private final ComponentName mComponent;
    private Icon mDefaultIcon;
    private boolean mIsShowingDialog;
    private boolean mIsTokenGranted;
    private boolean mListening;
    private final IQSTileService mService;
    private final TileServiceManager mServiceManager;
    private final Tile mTile;
    private final IBinder mToken;
    private final int mUser;
    private final IWindowManager mWindowManager;

    private CustomTile(QSTileHost qSTileHost, String str) {
        super(qSTileHost);
        this.mToken = new Binder();
        this.mWindowManager = WindowManagerGlobal.getWindowManagerService();
        this.mComponent = ComponentName.unflattenFromString(str);
        this.mTile = new Tile();
        setTileIcon();
        this.mServiceManager = qSTileHost.getTileServices().getTileWrapper(this);
        this.mService = this.mServiceManager.getTileService();
        this.mServiceManager.setTileChangeListener(this);
        this.mUser = ActivityManager.getCurrentUser();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected long getStaleTimeout() {
        return 3600000 + (60000 * this.mHost.indexOf(getTileSpec()));
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x003f A[Catch: Exception -> 0x0068, TryCatch #0 {Exception -> 0x0068, blocks: (B:3:0x0001, B:6:0x0012, B:8:0x001c, B:10:0x0023, B:12:0x002b, B:18:0x003f, B:20:0x004b, B:22:0x004f, B:23:0x0056, B:25:0x005e, B:9:0x001f), top: B:30:0x0001 }] */
    /* JADX WARN: Removed duplicated region for block: B:19:0x004a  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x004f A[Catch: Exception -> 0x0068, TryCatch #0 {Exception -> 0x0068, blocks: (B:3:0x0001, B:6:0x0012, B:8:0x001c, B:10:0x0023, B:12:0x002b, B:18:0x003f, B:20:0x004b, B:22:0x004f, B:23:0x0056, B:25:0x005e, B:9:0x001f), top: B:30:0x0001 }] */
    /* JADX WARN: Removed duplicated region for block: B:25:0x005e A[Catch: Exception -> 0x0068, TRY_LEAVE, TryCatch #0 {Exception -> 0x0068, blocks: (B:3:0x0001, B:6:0x0012, B:8:0x001c, B:10:0x0023, B:12:0x002b, B:18:0x003f, B:20:0x004b, B:22:0x004f, B:23:0x0056, B:25:0x005e, B:9:0x001f), top: B:30:0x0001 }] */
    /* JADX WARN: Removed duplicated region for block: B:33:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void setTileIcon() {
        boolean z;
        Icon icon;
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            int i = 786432;
            if (isSystemApp(packageManager)) {
                i = 786944;
            }
            ServiceInfo serviceInfo = packageManager.getServiceInfo(this.mComponent, i);
            int i2 = serviceInfo.icon != 0 ? serviceInfo.icon : serviceInfo.applicationInfo.icon;
            if (this.mTile.getIcon() != null && !iconEquals(this.mTile.getIcon(), this.mDefaultIcon)) {
                z = false;
                if (i2 != 0) {
                    icon = null;
                } else {
                    icon = Icon.createWithResource(this.mComponent.getPackageName(), i2);
                }
                this.mDefaultIcon = icon;
                if (z) {
                    this.mTile.setIcon(this.mDefaultIcon);
                }
                if (this.mTile.getLabel() != null) {
                    this.mTile.setLabel(serviceInfo.loadLabel(packageManager));
                    return;
                }
                return;
            }
            z = true;
            if (i2 != 0) {
            }
            this.mDefaultIcon = icon;
            if (z) {
            }
            if (this.mTile.getLabel() != null) {
            }
        } catch (Exception e) {
            this.mDefaultIcon = null;
        }
    }

    private boolean isSystemApp(PackageManager packageManager) throws PackageManager.NameNotFoundException {
        return packageManager.getApplicationInfo(this.mComponent.getPackageName(), 0).isSystemApp();
    }

    private boolean iconEquals(Icon icon, Icon icon2) {
        if (icon == icon2) {
            return true;
        }
        if (icon != null && icon2 != null && icon.getType() == 2 && icon2.getType() == 2 && icon.getResId() == icon2.getResId() && Objects.equals(icon.getResPackage(), icon2.getResPackage())) {
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.qs.external.TileLifecycleManager.TileChangeListener
    public void onTileChanged(ComponentName componentName) {
        setTileIcon();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mDefaultIcon != null;
    }

    public int getUser() {
        return this.mUser;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public LogMaker populate(LogMaker logMaker) {
        return super.populate(logMaker).setComponentName(this.mComponent);
    }

    public Tile getQsTile() {
        return this.mTile;
    }

    public void updateState(Tile tile) {
        this.mTile.setIcon(tile.getIcon());
        this.mTile.setLabel(tile.getLabel());
        this.mTile.setContentDescription(tile.getContentDescription());
        this.mTile.setState(tile.getState());
    }

    public void onDialogShown() {
        this.mIsShowingDialog = true;
    }

    public void onDialogHidden() {
        this.mIsShowingDialog = false;
        try {
            this.mWindowManager.removeWindowToken(this.mToken, 0);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        try {
            if (z) {
                setTileIcon();
                refreshState();
                if (!this.mServiceManager.isActiveTile()) {
                    this.mServiceManager.setBindRequested(true);
                    this.mService.onStartListening();
                    return;
                }
                return;
            }
            this.mService.onStopListening();
            if (this.mIsTokenGranted && !this.mIsShowingDialog) {
                try {
                    this.mWindowManager.removeWindowToken(this.mToken, 0);
                } catch (RemoteException e) {
                }
                this.mIsTokenGranted = false;
            }
            this.mIsShowingDialog = false;
            this.mServiceManager.setBindRequested(false);
        } catch (RemoteException e2) {
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleDestroy() {
        super.handleDestroy();
        if (this.mIsTokenGranted) {
            try {
                this.mWindowManager.removeWindowToken(this.mToken, 0);
            } catch (RemoteException e) {
            }
        }
        this.mHost.getTileServices().freeService(this, this.mServiceManager);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        intent.setPackage(this.mComponent.getPackageName());
        Intent resolveIntent = resolveIntent(intent);
        if (resolveIntent != null) {
            resolveIntent.putExtra("android.intent.extra.COMPONENT_NAME", this.mComponent);
            resolveIntent.putExtra("state", this.mTile.getState());
            return resolveIntent;
        }
        return new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", this.mComponent.getPackageName(), null));
    }

    private Intent resolveIntent(Intent intent) {
        ResolveInfo resolveActivityAsUser = this.mContext.getPackageManager().resolveActivityAsUser(intent, 0, ActivityManager.getCurrentUser());
        if (resolveActivityAsUser != null) {
            return new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES").setClassName(resolveActivityAsUser.activityInfo.packageName, resolveActivityAsUser.activityInfo.name);
        }
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (this.mTile.getState() == 0) {
            return;
        }
        try {
            this.mWindowManager.addWindowToken(this.mToken, 2035, 0);
            this.mIsTokenGranted = true;
        } catch (RemoteException e) {
        }
        try {
            if (this.mServiceManager.isActiveTile()) {
                this.mServiceManager.setBindRequested(true);
                this.mService.onStartListening();
            }
            this.mService.onClick(this.mToken);
        } catch (RemoteException e2) {
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleUpdateState(QSTile.State state, Object obj) {
        final Drawable loadDrawable;
        int state2 = this.mTile.getState();
        if (this.mServiceManager.hasPendingBind()) {
            state2 = 0;
        }
        state.state = state2;
        try {
            loadDrawable = this.mTile.getIcon().loadDrawable(this.mContext);
        } catch (Exception e) {
            Log.w(this.TAG, "Invalid icon, forcing into unavailable state");
            state.state = 0;
            loadDrawable = this.mDefaultIcon.loadDrawable(this.mContext);
        }
        state.iconSupplier = new Supplier() { // from class: com.android.systemui.qs.external.-$$Lambda$CustomTile$Oh-NzDEMM2yCWnVYbU2_DKTzaqo
            @Override // java.util.function.Supplier
            public final Object get() {
                return CustomTile.lambda$handleUpdateState$0(loadDrawable);
            }
        };
        state.label = this.mTile.getLabel();
        if (this.mTile.getContentDescription() != null) {
            state.contentDescription = this.mTile.getContentDescription();
        } else {
            state.contentDescription = state.label;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ QSTile.Icon lambda$handleUpdateState$0(Drawable drawable) {
        Drawable.ConstantState constantState = drawable.getConstantState();
        if (constantState != null) {
            return new QSTileImpl.DrawableIcon(constantState.newDrawable());
        }
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 268;
    }

    public void startUnlockAndRun() {
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.external.-$$Lambda$CustomTile$q1MKWZaaapZOjYFe9CyeyabLR0Q
            @Override // java.lang.Runnable
            public final void run() {
                CustomTile.this.mService.onUnlockComplete();
            }
        });
    }

    public static String toSpec(ComponentName componentName) {
        return "custom(" + componentName.flattenToShortString() + ")";
    }

    public static ComponentName getComponentFromSpec(String str) {
        String substring = str.substring("custom(".length(), str.length() - 1);
        if (substring.isEmpty()) {
            throw new IllegalArgumentException("Empty custom tile spec action");
        }
        return ComponentName.unflattenFromString(substring);
    }

    public static CustomTile create(QSTileHost qSTileHost, String str) {
        if (str == null || !str.startsWith("custom(") || !str.endsWith(")")) {
            throw new IllegalArgumentException("Bad custom tile spec: " + str);
        }
        String substring = str.substring("custom(".length(), str.length() - 1);
        if (substring.isEmpty()) {
            throw new IllegalArgumentException("Empty custom tile spec action");
        }
        return new CustomTile(qSTileHost, substring);
    }
}
