package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.statusbar.phone.QSTileHost;
import libcore.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/external/CustomTile.class */
public class CustomTile extends QSTile<QSTile.State> implements TileLifecycleManager.TileChangeListener {
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
        this.mTile = new Tile(this.mComponent);
        setTileIcon();
        this.mServiceManager = qSTileHost.getTileServices().getTileWrapper(this);
        this.mService = this.mServiceManager.getTileService();
        this.mServiceManager.setTileChangeListener(this);
        this.mUser = ActivityManager.getCurrentUser();
    }

    public static QSTile<?> create(QSTileHost qSTileHost, String str) {
        if (str != null && str.startsWith("custom(") && str.endsWith(")")) {
            String substring = str.substring("custom(".length(), str.length() - 1);
            if (substring.isEmpty()) {
                throw new IllegalArgumentException("Empty custom tile spec action");
            }
            return new CustomTile(qSTileHost, substring);
        }
        throw new IllegalArgumentException("Bad custom tile spec: " + str);
    }

    private static int getColor(int i) {
        switch (i) {
            case 0:
                return 2131558595;
            case 1:
                return 2131558596;
            case 2:
                return 2131558597;
            default:
                return 0;
        }
    }

    public static ComponentName getComponentFromSpec(String str) {
        String substring = str.substring("custom(".length(), str.length() - 1);
        if (substring.isEmpty()) {
            throw new IllegalArgumentException("Empty custom tile spec action");
        }
        return ComponentName.unflattenFromString(substring);
    }

    private boolean iconEquals(Icon icon, Icon icon2) {
        if (icon == icon2) {
            return true;
        }
        return icon != null && icon2 != null && icon.getType() == 2 && icon2.getType() == 2 && icon.getResId() == icon2.getResId() && Objects.equal(icon.getResPackage(), icon2.getResPackage());
    }

    private Intent resolveIntent(Intent intent) {
        ResolveInfo resolveActivityAsUser = this.mContext.getPackageManager().resolveActivityAsUser(intent, 0, ActivityManager.getCurrentUser());
        Intent intent2 = null;
        if (resolveActivityAsUser != null) {
            intent2 = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES").setClassName(resolveActivityAsUser.activityInfo.packageName, resolveActivityAsUser.activityInfo.name);
        }
        return intent2;
    }

    private void setTileIcon() {
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            ServiceInfo serviceInfo = packageManager.getServiceInfo(this.mComponent, 786432);
            int i = serviceInfo.icon != 0 ? serviceInfo.icon : serviceInfo.applicationInfo.icon;
            boolean iconEquals = this.mTile.getIcon() != null ? iconEquals(this.mTile.getIcon(), this.mDefaultIcon) : true;
            this.mDefaultIcon = i != 0 ? Icon.createWithResource(this.mComponent.getPackageName(), i) : null;
            if (iconEquals) {
                this.mTile.setIcon(this.mDefaultIcon);
            }
            if (this.mTile.getLabel() == null) {
                this.mTile.setLabel(serviceInfo.loadLabel(packageManager));
            }
        } catch (Exception e) {
            this.mDefaultIcon = null;
        }
    }

    public static String toSpec(ComponentName componentName) {
        return "custom(" + componentName.flattenToShortString() + ")";
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        intent.setPackage(this.mComponent.getPackageName());
        Intent resolveIntent = resolveIntent(intent);
        return resolveIntent != null ? resolveIntent : new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", this.mComponent.getPackageName(), null));
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 268;
    }

    public Tile getQsTile() {
        return this.mTile;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    public int getUser() {
        return this.mUser;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (this.mTile.getState() == 0) {
            return;
        }
        try {
            this.mWindowManager.addWindowToken(this.mToken, 2035);
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
        MetricsLogger.action(this.mContext, getMetricsCategory(), this.mComponent.getPackageName());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleDestroy() {
        super.handleDestroy();
        if (this.mIsTokenGranted) {
            try {
                this.mWindowManager.removeWindowToken(this.mToken);
            } catch (RemoteException e) {
            }
        }
        this.mHost.getTileServices().freeService(this, this.mServiceManager);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleUpdateState(QSTile.State state, Object obj) {
        Drawable loadDrawable;
        int state2 = this.mTile.getState();
        if (this.mServiceManager.hasPendingBind()) {
            state2 = 0;
        }
        try {
            loadDrawable = this.mTile.getIcon().loadDrawable(this.mContext);
        } catch (Exception e) {
            Log.w(this.TAG, "Invalid icon, forcing into unavailable state");
            state2 = 0;
            loadDrawable = this.mDefaultIcon.loadDrawable(this.mContext);
        }
        int color = this.mContext.getColor(getColor(state2));
        loadDrawable.setTint(color);
        state.icon = new QSTile.DrawableIcon(loadDrawable);
        state.label = this.mTile.getLabel();
        if (state2 == 0) {
            state.label = new SpannableStringBuilder().append(state.label, new ForegroundColorSpan(color), 18);
        }
        if (this.mTile.getContentDescription() != null) {
            state.contentDescription = this.mTile.getContentDescription();
        } else {
            state.contentDescription = state.label;
        }
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mDefaultIcon != null;
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    public void onDialogHidden() {
        this.mIsShowingDialog = false;
        try {
            this.mWindowManager.removeWindowToken(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public void onDialogShown() {
        this.mIsShowingDialog = true;
    }

    @Override // com.android.systemui.qs.external.TileLifecycleManager.TileChangeListener
    public void onTileChanged(ComponentName componentName) {
        setTileIcon();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        try {
            if (z) {
                setTileIcon();
                refreshState();
                if (this.mServiceManager.isActiveTile()) {
                    return;
                }
                this.mServiceManager.setBindRequested(true);
                this.mService.onStartListening();
                return;
            }
            this.mService.onStopListening();
            if (this.mIsTokenGranted && !this.mIsShowingDialog) {
                try {
                    this.mWindowManager.removeWindowToken(this.mToken);
                } catch (RemoteException e) {
                }
                this.mIsTokenGranted = false;
            }
            this.mIsShowingDialog = false;
            this.mServiceManager.setBindRequested(false);
        } catch (RemoteException e2) {
        }
    }

    public void startUnlockAndRun() {
        this.mHost.startRunnableDismissingKeyguard(new Runnable(this) { // from class: com.android.systemui.qs.external.CustomTile.1
            final CustomTile this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    this.this$0.mService.onUnlockComplete();
                } catch (RemoteException e) {
                }
            }
        });
    }

    public void updateState(Tile tile) {
        this.mTile.setIcon(tile.getIcon());
        this.mTile.setLabel(tile.getLabel());
        this.mTile.setContentDescription(tile.getContentDescription());
        this.mTile.setState(tile.getState());
    }
}
