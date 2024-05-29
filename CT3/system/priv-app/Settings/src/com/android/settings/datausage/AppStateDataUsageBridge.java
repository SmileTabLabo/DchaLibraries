package com.android.settings.datausage;

import com.android.settings.applications.AppStateBaseBridge;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class AppStateDataUsageBridge extends AppStateBaseBridge {
    private final DataSaverBackend mDataSaverBackend;

    public AppStateDataUsageBridge(ApplicationsState appState, AppStateBaseBridge.Callback callback, DataSaverBackend backend) {
        super(appState, callback);
        this.mDataSaverBackend = backend;
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void loadAllExtraInfo() {
        ArrayList<ApplicationsState.AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            ApplicationsState.AppEntry app = apps.get(i);
            app.extraInfo = new DataUsageState(this.mDataSaverBackend.isWhitelisted(app.info.uid), this.mDataSaverBackend.isBlacklisted(app.info.uid));
        }
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void updateExtraInfo(ApplicationsState.AppEntry app, String pkg, int uid) {
        app.extraInfo = new DataUsageState(this.mDataSaverBackend.isWhitelisted(uid), this.mDataSaverBackend.isBlacklisted(uid));
    }

    /* loaded from: classes.dex */
    public static class DataUsageState {
        public boolean isDataSaverBlacklisted;
        public boolean isDataSaverWhitelisted;

        public DataUsageState(boolean isDataSaverWhitelisted, boolean isDataSaverBlacklisted) {
            this.isDataSaverWhitelisted = isDataSaverWhitelisted;
            this.isDataSaverBlacklisted = isDataSaverBlacklisted;
        }
    }
}
