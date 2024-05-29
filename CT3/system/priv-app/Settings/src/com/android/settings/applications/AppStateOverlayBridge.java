package com.android.settings.applications;

import android.content.Context;
import com.android.settings.applications.AppStateAppOpsBridge;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settingslib.applications.ApplicationsState;
/* loaded from: classes.dex */
public class AppStateOverlayBridge extends AppStateAppOpsBridge {
    private static final String[] PM_PERMISSION = {"android.permission.SYSTEM_ALERT_WINDOW"};
    public static final ApplicationsState.AppFilter FILTER_SYSTEM_ALERT_WINDOW = new ApplicationsState.AppFilter() { // from class: com.android.settings.applications.AppStateOverlayBridge.1
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(ApplicationsState.AppEntry info) {
            return info.extraInfo != null;
        }
    };

    public AppStateOverlayBridge(Context context, ApplicationsState appState, AppStateBaseBridge.Callback callback) {
        super(context, appState, callback, 24, PM_PERMISSION);
    }

    @Override // com.android.settings.applications.AppStateAppOpsBridge, com.android.settings.applications.AppStateBaseBridge
    protected void updateExtraInfo(ApplicationsState.AppEntry app, String pkg, int uid) {
        app.extraInfo = getOverlayInfo(pkg, uid);
    }

    public OverlayState getOverlayInfo(String pkg, int uid) {
        AppStateAppOpsBridge.PermissionState permissionState = super.getPermissionInfo(pkg, uid);
        return new OverlayState(permissionState);
    }

    /* loaded from: classes.dex */
    public static class OverlayState extends AppStateAppOpsBridge.PermissionState {
        public OverlayState(AppStateAppOpsBridge.PermissionState permissionState) {
            super(permissionState.packageName, permissionState.userHandle);
            this.packageInfo = permissionState.packageInfo;
            this.appOpMode = permissionState.appOpMode;
            this.permissionDeclared = permissionState.permissionDeclared;
            this.staticPermissionGranted = permissionState.staticPermissionGranted;
        }
    }
}
