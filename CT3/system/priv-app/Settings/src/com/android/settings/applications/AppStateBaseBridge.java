package com.android.settings.applications;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public abstract class AppStateBaseBridge implements ApplicationsState.Callbacks {
    protected final ApplicationsState.Session mAppSession;
    protected final ApplicationsState mAppState;
    protected final Callback mCallback;
    protected final BackgroundHandler mHandler;
    protected final MainHandler mMainHandler;

    /* loaded from: classes.dex */
    public interface Callback {
        void onExtraInfoUpdated();
    }

    protected abstract void loadAllExtraInfo();

    protected abstract void updateExtraInfo(ApplicationsState.AppEntry appEntry, String str, int i);

    public AppStateBaseBridge(ApplicationsState appState, Callback callback) {
        this.mAppState = appState;
        this.mAppSession = this.mAppState != null ? this.mAppState.newSession(this) : null;
        this.mCallback = callback;
        this.mHandler = new BackgroundHandler(this.mAppState != null ? this.mAppState.getBackgroundLooper() : Looper.getMainLooper());
        this.mMainHandler = new MainHandler(this, null);
    }

    public void resume() {
        this.mHandler.sendEmptyMessage(1);
        this.mAppSession.resume();
    }

    public void pause() {
        this.mAppSession.pause();
    }

    public void release() {
        this.mAppSession.release();
    }

    public void forceUpdate(String pkg, int uid) {
        this.mHandler.obtainMessage(2, uid, 0, pkg).sendToTarget();
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageListChanged() {
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onLoadEntriesCompleted() {
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRunningStateChanged(boolean running) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> apps) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageIconChanged() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onPackageSizeChanged(String packageName) {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onAllSizesComputed() {
    }

    @Override // com.android.settingslib.applications.ApplicationsState.Callbacks
    public void onLauncherInfoChanged() {
    }

    /* loaded from: classes.dex */
    private class MainHandler extends Handler {
        /* synthetic */ MainHandler(AppStateBaseBridge this$0, MainHandler mainHandler) {
            this();
        }

        private MainHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AppStateBaseBridge.this.mCallback.onExtraInfoUpdated();
                    return;
                default:
                    return;
            }
        }
    }

    /* loaded from: classes.dex */
    private class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AppStateBaseBridge.this.loadAllExtraInfo();
                    AppStateBaseBridge.this.mMainHandler.sendEmptyMessage(1);
                    return;
                case 2:
                    ArrayList<ApplicationsState.AppEntry> apps = AppStateBaseBridge.this.mAppSession.getAllApps();
                    int N = apps.size();
                    String pkg = (String) msg.obj;
                    int uid = msg.arg1;
                    for (int i = 0; i < N; i++) {
                        ApplicationsState.AppEntry app = apps.get(i);
                        if (app.info.uid == uid && pkg.equals(app.info.packageName)) {
                            AppStateBaseBridge.this.updateExtraInfo(app, pkg, uid);
                        }
                    }
                    AppStateBaseBridge.this.mMainHandler.sendEmptyMessage(1);
                    return;
                default:
                    return;
            }
        }
    }
}
