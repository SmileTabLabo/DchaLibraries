package com.android.settings.applications;

import android.content.Context;
import com.android.settings.applications.AppStateAppOpsBridge;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settingslib.applications.ApplicationsState;
/* loaded from: classes.dex */
public class AppStateUsageBridge extends AppStateAppOpsBridge {
    private static final String[] PM_PERMISSION = {"android.permission.PACKAGE_USAGE_STATS"};
    public static final ApplicationsState.AppFilter FILTER_APP_USAGE = new ApplicationsState.AppFilter() { // from class: com.android.settings.applications.AppStateUsageBridge.1
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(ApplicationsState.AppEntry info) {
            return info.extraInfo != null;
        }
    };

    public AppStateUsageBridge(Context context, ApplicationsState appState, AppStateBaseBridge.Callback callback) {
        super(context, appState, callback, 43, PM_PERMISSION);
    }

    @Override // com.android.settings.applications.AppStateAppOpsBridge, com.android.settings.applications.AppStateBaseBridge
    protected void updateExtraInfo(ApplicationsState.AppEntry app, String pkg, int uid) {
        app.extraInfo = getUsageInfo(pkg, uid);
    }

    public UsageState getUsageInfo(String pkg, int uid) {
        AppStateAppOpsBridge.PermissionState permissionState = super.getPermissionInfo(pkg, uid);
        return new UsageState(permissionState);
    }

    /* loaded from: classes.dex */
    public static class UsageState extends AppStateAppOpsBridge.PermissionState {
        public UsageState(AppStateAppOpsBridge.PermissionState permissionState) {
            super(permissionState.packageName, permissionState.userHandle);
            this.packageInfo = permissionState.packageInfo;
            this.appOpMode = permissionState.appOpMode;
            this.permissionDeclared = permissionState.permissionDeclared;
            this.staticPermissionGranted = permissionState.staticPermissionGranted;
        }
    }
}
