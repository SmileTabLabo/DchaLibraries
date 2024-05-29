package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import com.android.systemui.statusbar.policy.LocationController;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/LocationControllerImpl.class */
public class LocationControllerImpl extends BroadcastReceiver implements LocationController {
    private static final int[] mHighPowerRequestAppOpArray = {42};
    private AppOpsManager mAppOpsManager;
    private boolean mAreActiveLocationRequests;
    private Context mContext;
    public final String mSlotLocation;
    private StatusBarManager mStatusBarManager;
    private ArrayList<LocationController.LocationSettingsChangeCallback> mSettingsChangeCallbacks = new ArrayList<>();
    private final H mHandler = new H(this, null);

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/LocationControllerImpl$H.class */
    private final class H extends Handler {
        final LocationControllerImpl this$0;

        private H(LocationControllerImpl locationControllerImpl) {
            this.this$0 = locationControllerImpl;
        }

        /* synthetic */ H(LocationControllerImpl locationControllerImpl, H h) {
            this(locationControllerImpl);
        }

        private void locationSettingsChanged() {
            boolean isLocationEnabled = this.this$0.isLocationEnabled();
            for (LocationController.LocationSettingsChangeCallback locationSettingsChangeCallback : this.this$0.mSettingsChangeCallbacks) {
                locationSettingsChangeCallback.onLocationSettingsChanged(isLocationEnabled);
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    locationSettingsChanged();
                    return;
                default:
                    return;
            }
        }
    }

    public LocationControllerImpl(Context context, Looper looper) {
        this.mContext = context;
        this.mSlotLocation = this.mContext.getString(17039394);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.HIGH_POWER_REQUEST_CHANGE");
        intentFilter.addAction("android.location.MODE_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, null, new Handler(looper));
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        updateActiveLocationRequests();
        refreshViews();
    }

    private boolean areActiveHighPowerLocationRequests() {
        List packagesForOps = this.mAppOpsManager.getPackagesForOps(mHighPowerRequestAppOpArray);
        if (packagesForOps != null) {
            int size = packagesForOps.size();
            for (int i = 0; i < size; i++) {
                List ops = ((AppOpsManager.PackageOps) packagesForOps.get(i)).getOps();
                if (ops != null) {
                    int size2 = ops.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        AppOpsManager.OpEntry opEntry = (AppOpsManager.OpEntry) ops.get(i2);
                        if (opEntry.getOp() == 42 && opEntry.isRunning()) {
                            return true;
                        }
                    }
                    continue;
                }
            }
            return false;
        }
        return false;
    }

    private boolean isUserLocationRestricted(int i) {
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_share_location", UserHandle.of(i));
    }

    private void refreshViews() {
        if (this.mAreActiveLocationRequests) {
            this.mStatusBarManager.setIcon(this.mSlotLocation, 2130838278, 0, this.mContext.getString(2131493508));
        } else {
            this.mStatusBarManager.removeIcon(this.mSlotLocation);
        }
    }

    private void updateActiveLocationRequests() {
        boolean z = this.mAreActiveLocationRequests;
        this.mAreActiveLocationRequests = areActiveHighPowerLocationRequests();
        if (this.mAreActiveLocationRequests != z) {
            refreshViews();
        }
    }

    @Override // com.android.systemui.statusbar.policy.LocationController
    public void addSettingsChangedCallback(LocationController.LocationSettingsChangeCallback locationSettingsChangeCallback) {
        this.mSettingsChangeCallbacks.add(locationSettingsChangeCallback);
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.systemui.statusbar.policy.LocationController
    public boolean isLocationEnabled() {
        boolean z = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, ActivityManager.getCurrentUser()) != 0) {
            z = true;
        }
        return z;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.location.HIGH_POWER_REQUEST_CHANGE".equals(action)) {
            updateActiveLocationRequests();
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    @Override // com.android.systemui.statusbar.policy.LocationController
    public void removeSettingsChangedCallback(LocationController.LocationSettingsChangeCallback locationSettingsChangeCallback) {
        this.mSettingsChangeCallbacks.remove(locationSettingsChangeCallback);
    }

    @Override // com.android.systemui.statusbar.policy.LocationController
    public boolean setLocationEnabled(boolean z) {
        int currentUser = ActivityManager.getCurrentUser();
        if (isUserLocationRestricted(currentUser)) {
            return false;
        }
        return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", z ? -1 : 0, currentUser);
    }
}
