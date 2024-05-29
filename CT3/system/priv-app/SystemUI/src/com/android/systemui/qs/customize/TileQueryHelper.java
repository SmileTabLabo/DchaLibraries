package com.android.systemui.qs.customize;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.mediatek.systemui.PluginManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/qs/customize/TileQueryHelper.class */
public class TileQueryHelper {
    private final Context mContext;
    private TileStateListener mListener;
    private final ArrayList<TileInfo> mTiles = new ArrayList<>();
    private final ArrayList<String> mSpecs = new ArrayList<>();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.qs.customize.TileQueryHelper$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/qs/customize/TileQueryHelper$1.class */
    public class AnonymousClass1 implements Runnable {
        final TileQueryHelper this$0;
        final Handler val$mainHandler;
        final String val$spec;
        final QSTile val$tile;

        AnonymousClass1(TileQueryHelper tileQueryHelper, QSTile qSTile, Handler handler, String str) {
            this.this$0 = tileQueryHelper;
            this.val$tile = qSTile;
            this.val$mainHandler = handler;
            this.val$spec = str;
        }

        @Override // java.lang.Runnable
        public void run() {
            QSTile.State newTileState = this.val$tile.newTileState();
            this.val$tile.getState().copyTo(newTileState);
            newTileState.label = this.val$tile.getTileLabel();
            this.val$mainHandler.post(new Runnable(this, this.val$spec, newTileState) { // from class: com.android.systemui.qs.customize.TileQueryHelper.1.1
                final AnonymousClass1 this$1;
                final String val$spec;
                final QSTile.State val$state;

                {
                    this.this$1 = this;
                    this.val$spec = r5;
                    this.val$state = newTileState;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.addTile(this.val$spec, null, this.val$state, true);
                    this.this$1.this$0.mListener.onTilesChanged(this.this$1.this$0.mTiles);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.qs.customize.TileQueryHelper$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/qs/customize/TileQueryHelper$2.class */
    public class AnonymousClass2 implements Runnable {
        final TileQueryHelper this$0;
        final QSTileHost val$host;
        final Handler val$mainHandler;

        AnonymousClass2(TileQueryHelper tileQueryHelper, Handler handler, QSTileHost qSTileHost) {
            this.this$0 = tileQueryHelper;
            this.val$mainHandler = handler;
            this.val$host = qSTileHost;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.val$mainHandler.post(new Runnable(this, this.val$host) { // from class: com.android.systemui.qs.customize.TileQueryHelper.2.1
                final AnonymousClass2 this$1;
                final QSTileHost val$host;

                {
                    this.this$1 = this;
                    this.val$host = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    new QueryTilesTask(this.this$1.this$0, null).execute(this.val$host.getTiles());
                }
            });
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/customize/TileQueryHelper$QueryTilesTask.class */
    private class QueryTilesTask extends AsyncTask<Collection<QSTile<?>>, Void, Collection<TileInfo>> {
        final TileQueryHelper this$0;

        private QueryTilesTask(TileQueryHelper tileQueryHelper) {
            this.this$0 = tileQueryHelper;
        }

        /* synthetic */ QueryTilesTask(TileQueryHelper tileQueryHelper, QueryTilesTask queryTilesTask) {
            this(tileQueryHelper);
        }

        private QSTile.State getState(Collection<QSTile<?>> collection, String str) {
            Iterator<T> it = collection.iterator();
            while (it.hasNext()) {
                QSTile qSTile = (QSTile) it.next();
                if (str.equals(qSTile.getTileSpec())) {
                    QSTile.State newTileState = qSTile.newTileState();
                    qSTile.getState().copyTo(newTileState);
                    return newTileState;
                }
            }
            return null;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Collection<TileInfo> doInBackground(Collection<QSTile<?>>... collectionArr) {
            ArrayList arrayList = new ArrayList();
            PackageManager packageManager = this.this$0.mContext.getPackageManager();
            List<ResolveInfo> queryIntentServicesAsUser = packageManager.queryIntentServicesAsUser(new Intent("android.service.quicksettings.action.QS_TILE"), 0, ActivityManager.getCurrentUser());
            if (BenesseExtension.getDchaState() != 0) {
                queryIntentServicesAsUser = new ArrayList();
            }
            for (ResolveInfo resolveInfo : queryIntentServicesAsUser) {
                ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
                CharSequence loadLabel = resolveInfo.serviceInfo.applicationInfo.loadLabel(packageManager);
                String spec = CustomTile.toSpec(componentName);
                QSTile.State state = getState(collectionArr[0], spec);
                if (state != null) {
                    this.this$0.addTile(spec, loadLabel, state, false);
                } else if (resolveInfo.serviceInfo.icon != 0 || resolveInfo.serviceInfo.applicationInfo.icon != 0) {
                    Drawable loadIcon = resolveInfo.serviceInfo.loadIcon(packageManager);
                    if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(resolveInfo.serviceInfo.permission) && loadIcon != null) {
                        loadIcon.mutate();
                        loadIcon.setTint(this.this$0.mContext.getColor(17170443));
                        CharSequence loadLabel2 = resolveInfo.serviceInfo.loadLabel(packageManager);
                        this.this$0.addTile(spec, loadIcon, loadLabel2 != null ? loadLabel2.toString() : "null", loadLabel, this.this$0.mContext);
                    }
                }
            }
            return arrayList;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Collection<TileInfo> collection) {
            this.this$0.mTiles.addAll(collection);
            this.this$0.mListener.onTilesChanged(this.this$0.mTiles);
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/customize/TileQueryHelper$TileInfo.class */
    public static class TileInfo {
        public CharSequence appLabel;
        public boolean isSystem;
        public String spec;
        public QSTile.State state;
    }

    /* loaded from: a.zip:com/android/systemui/qs/customize/TileQueryHelper$TileStateListener.class */
    public interface TileStateListener {
        void onTilesChanged(List<TileInfo> list);
    }

    public TileQueryHelper(Context context, QSTileHost qSTileHost) {
        this.mContext = context;
        addSystemTiles(qSTileHost);
    }

    private void addSystemTiles(QSTileHost qSTileHost) {
        String str = this.mContext.getString(2131493276) + ",hotspot,inversion,saver,work,cast,night";
        String[] split = (PluginManager.getQuickSettingsPlugin(this.mContext).customizeQuickSettingsTileOrder(str) + ",hotspot,inversion,saver,work,cast,night").split(",");
        Handler handler = new Handler(qSTileHost.getLooper());
        Handler handler2 = new Handler(Looper.getMainLooper());
        for (String str2 : split) {
            QSTile<?> createTile = qSTileHost.createTile(str2);
            if (createTile != null && createTile.isAvailable()) {
                createTile.setListening(this, true);
                createTile.clearState();
                createTile.refreshState();
                createTile.setListening(this, false);
                handler.post(new AnonymousClass1(this, createTile, handler2, str2));
            }
        }
        handler.post(new AnonymousClass2(this, handler2, qSTileHost));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addTile(String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2, Context context) {
        QSTile.State state = new QSTile.State();
        state.label = charSequence;
        state.contentDescription = charSequence;
        state.icon = new QSTile.DrawableIcon(drawable);
        addTile(str, charSequence2, state, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addTile(String str, CharSequence charSequence, QSTile.State state, boolean z) {
        if (this.mSpecs.contains(str)) {
            return;
        }
        TileInfo tileInfo = new TileInfo();
        tileInfo.state = state;
        QSTile.State state2 = tileInfo.state;
        String name = Button.class.getName();
        tileInfo.state.expandedAccessibilityClassName = name;
        state2.minimalAccessibilityClassName = name;
        tileInfo.spec = str;
        tileInfo.appLabel = charSequence;
        tileInfo.isSystem = z;
        this.mTiles.add(tileInfo);
        this.mSpecs.add(str);
    }

    public void setListener(TileStateListener tileStateListener) {
        this.mListener = tileStateListener;
    }
}
