package com.android.systemui.qs.customize;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArraySet;
import android.widget.Button;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.mediatek.systemui.ext.OpSystemUICustomizationFactoryBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class TileQueryHelper {
    private final Context mContext;
    private boolean mFinished;
    private final TileStateListener mListener;
    private final ArrayList<TileInfo> mTiles = new ArrayList<>();
    private final ArraySet<String> mSpecs = new ArraySet<>();
    private final Handler mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
    private final Handler mMainHandler = (Handler) Dependency.get(Dependency.MAIN_HANDLER);

    /* loaded from: classes.dex */
    public static class TileInfo {
        public boolean isSystem;
        public String spec;
        public QSTile.State state;
    }

    /* loaded from: classes.dex */
    public interface TileStateListener {
        void onTilesChanged(List<TileInfo> list);
    }

    public TileQueryHelper(Context context, TileStateListener tileStateListener) {
        this.mContext = context;
        this.mListener = tileStateListener;
    }

    public void queryTiles(QSTileHost qSTileHost) {
        this.mTiles.clear();
        this.mSpecs.clear();
        this.mFinished = false;
        addStockTiles(qSTileHost);
        addPackageTiles(qSTileHost);
    }

    public boolean isFinished() {
        return this.mFinished;
    }

    private void addStockTiles(QSTileHost qSTileHost) {
        String customizeQuickSettingsTileOrder = OpSystemUICustomizationFactoryBase.getOpFactory(this.mContext).makeQuickSettings(this.mContext).customizeQuickSettingsTileOrder(this.mContext.getString(R.string.quick_settings_tiles_stock));
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(Arrays.asList(customizeQuickSettingsTileOrder.split(",")));
        if (Build.IS_DEBUGGABLE) {
            arrayList.add("dbg:mem");
        }
        final ArrayList arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            QSTile createTile = qSTileHost.createTile(str);
            if (createTile != null) {
                if (!createTile.isAvailable()) {
                    createTile.destroy();
                } else {
                    createTile.setListening(this, true);
                    createTile.clearState();
                    createTile.refreshState();
                    createTile.setListening(this, false);
                    createTile.setTileSpec(str);
                    arrayList2.add(createTile);
                }
            }
        }
        this.mBgHandler.post(new Runnable() { // from class: com.android.systemui.qs.customize.-$$Lambda$TileQueryHelper$pVNHAsbxeJK0zo0OnLB_L5xKe_E
            @Override // java.lang.Runnable
            public final void run() {
                TileQueryHelper.lambda$addStockTiles$0(TileQueryHelper.this, arrayList2);
            }
        });
    }

    public static /* synthetic */ void lambda$addStockTiles$0(TileQueryHelper tileQueryHelper, ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            QSTile qSTile = (QSTile) it.next();
            QSTile.State copy = qSTile.getState().copy();
            copy.label = qSTile.getTileLabel();
            qSTile.destroy();
            tileQueryHelper.addTile(qSTile.getTileSpec(), (CharSequence) null, copy, true);
        }
        tileQueryHelper.notifyTilesChanged(false);
    }

    private void addPackageTiles(final QSTileHost qSTileHost) {
        this.mBgHandler.post(new Runnable() { // from class: com.android.systemui.qs.customize.-$$Lambda$TileQueryHelper$-7aqDrq4N73id-i9gI_WE72bklw
            @Override // java.lang.Runnable
            public final void run() {
                TileQueryHelper.lambda$addPackageTiles$1(TileQueryHelper.this, qSTileHost);
            }
        });
    }

    public static /* synthetic */ void lambda$addPackageTiles$1(TileQueryHelper tileQueryHelper, QSTileHost qSTileHost) {
        Collection<QSTile> tiles = qSTileHost.getTiles();
        PackageManager packageManager = tileQueryHelper.mContext.getPackageManager();
        List<ResolveInfo> queryIntentServicesAsUser = packageManager.queryIntentServicesAsUser(new Intent("android.service.quicksettings.action.QS_TILE"), 0, ActivityManager.getCurrentUser());
        String string = tileQueryHelper.mContext.getString(R.string.quick_settings_tiles_stock);
        for (ResolveInfo resolveInfo : queryIntentServicesAsUser) {
            ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
            if (!string.contains(componentName.flattenToString())) {
                CharSequence loadLabel = resolveInfo.serviceInfo.applicationInfo.loadLabel(packageManager);
                String spec = CustomTile.toSpec(componentName);
                QSTile.State state = tileQueryHelper.getState(tiles, spec);
                if (state != null) {
                    tileQueryHelper.addTile(spec, loadLabel, state, false);
                } else if (resolveInfo.serviceInfo.icon != 0 || resolveInfo.serviceInfo.applicationInfo.icon != 0) {
                    Drawable loadIcon = resolveInfo.serviceInfo.loadIcon(packageManager);
                    if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(resolveInfo.serviceInfo.permission) && loadIcon != null) {
                        loadIcon.mutate();
                        loadIcon.setTint(tileQueryHelper.mContext.getColor(17170443));
                        CharSequence loadLabel2 = resolveInfo.serviceInfo.loadLabel(packageManager);
                        tileQueryHelper.addTile(spec, loadIcon, loadLabel2 != null ? loadLabel2.toString() : "null", loadLabel);
                    }
                }
            }
        }
        tileQueryHelper.notifyTilesChanged(true);
    }

    private void notifyTilesChanged(final boolean z) {
        final ArrayList arrayList = new ArrayList(this.mTiles);
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.qs.customize.-$$Lambda$TileQueryHelper$td1yVFso44MefBPUi6jpDHx3Yoc
            @Override // java.lang.Runnable
            public final void run() {
                TileQueryHelper.lambda$notifyTilesChanged$2(TileQueryHelper.this, arrayList, z);
            }
        });
    }

    public static /* synthetic */ void lambda$notifyTilesChanged$2(TileQueryHelper tileQueryHelper, ArrayList arrayList, boolean z) {
        tileQueryHelper.mListener.onTilesChanged(arrayList);
        tileQueryHelper.mFinished = z;
    }

    private QSTile.State getState(Collection<QSTile> collection, String str) {
        for (QSTile qSTile : collection) {
            if (str.equals(qSTile.getTileSpec())) {
                return qSTile.getState().copy();
            }
        }
        return null;
    }

    private void addTile(String str, CharSequence charSequence, QSTile.State state, boolean z) {
        if (this.mSpecs.contains(str)) {
            return;
        }
        TileInfo tileInfo = new TileInfo();
        tileInfo.state = state;
        tileInfo.state.dualTarget = false;
        tileInfo.state.expandedAccessibilityClassName = Button.class.getName();
        tileInfo.spec = str;
        tileInfo.state.secondaryLabel = (z || TextUtils.equals(state.label, charSequence)) ? null : null;
        tileInfo.isSystem = z;
        this.mTiles.add(tileInfo);
        this.mSpecs.add(str);
    }

    private void addTile(String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2) {
        QSTile.State state = new QSTile.State();
        state.label = charSequence;
        state.contentDescription = charSequence;
        state.icon = new QSTileImpl.DrawableIcon(drawable);
        addTile(str, charSequence2, state, false);
    }
}
