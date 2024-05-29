package com.android.settings.applications;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.telephony.ISms;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settingslib.applications.ApplicationsState;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class AppStateSmsPremBridge extends AppStateBaseBridge {
    public static final ApplicationsState.AppFilter FILTER_APP_PREMIUM_SMS = new ApplicationsState.AppFilter() { // from class: com.android.settings.applications.AppStateSmsPremBridge.1
        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public void init() {
        }

        @Override // com.android.settingslib.applications.ApplicationsState.AppFilter
        public boolean filterApp(ApplicationsState.AppEntry info) {
            return (info.extraInfo instanceof SmsState) && ((SmsState) info.extraInfo).smsState != 0;
        }
    };
    private final Context mContext;
    private final ISms mSmsManager;

    /* loaded from: classes.dex */
    public static class SmsState {
        public int smsState;
    }

    public AppStateSmsPremBridge(Context context, ApplicationsState appState, AppStateBaseBridge.Callback callback) {
        super(appState, callback);
        this.mContext = context;
        this.mSmsManager = ISms.Stub.asInterface(ServiceManager.getService("isms"));
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void loadAllExtraInfo() {
        ArrayList<ApplicationsState.AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            ApplicationsState.AppEntry app = apps.get(i);
            updateExtraInfo(app, app.info.packageName, app.info.uid);
        }
    }

    @Override // com.android.settings.applications.AppStateBaseBridge
    protected void updateExtraInfo(ApplicationsState.AppEntry app, String pkg, int uid) {
        app.extraInfo = getState(pkg);
    }

    public SmsState getState(String pkg) {
        SmsState state = new SmsState();
        state.smsState = getSmsState(pkg);
        return state;
    }

    private int getSmsState(String pkg) {
        try {
            return this.mSmsManager.getPremiumSmsPermission(pkg);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void setSmsState(String pkg, int state) {
        try {
            this.mSmsManager.setPremiumSmsPermission(pkg, state);
        } catch (RemoteException e) {
        }
    }
}
