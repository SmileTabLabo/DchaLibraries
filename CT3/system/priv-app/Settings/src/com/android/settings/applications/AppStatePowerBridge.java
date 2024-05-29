package com.android.settings.applications;

import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.fuelgauge.PowerWhitelistBackend;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class AppStatePowerBridge extends AppStateBaseBridge {
    public static final ApplicationsState.AppFilter FILTER_POWER_WHITELISTED = new ApplicationsState.CompoundFilter(ApplicationsState.FILTER_WITHOUT_DISABLED_UNTIL_USED, new ApplicationsState.AppFilter() { // from class: com.android.settings.applications.AppStatePowerBridge.1
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(ApplicationsState.AppEntry info) {
            return info.extraInfo == Boolean.TRUE;
        }
    });
    private final PowerWhitelistBackend mBackend;

    /* loaded from: classes.dex */
    public static class HighPowerState {
    }

    public AppStatePowerBridge(ApplicationsState appState, AppStateBaseBridge.Callback callback) {
        super(appState, callback);
        this.mBackend = PowerWhitelistBackend.getInstance();
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void loadAllExtraInfo() {
        ArrayList<ApplicationsState.AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            ApplicationsState.AppEntry app = apps.get(i);
            app.extraInfo = this.mBackend.isWhitelisted(app.info.packageName) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void updateExtraInfo(ApplicationsState.AppEntry app, String pkg, int uid) {
        app.extraInfo = this.mBackend.isWhitelisted(pkg) ? Boolean.TRUE : Boolean.FALSE;
    }
}
