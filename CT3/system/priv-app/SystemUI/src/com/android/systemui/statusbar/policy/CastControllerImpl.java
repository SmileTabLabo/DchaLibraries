package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.media.MediaRouter;
import android.media.projection.MediaProjectionInfo;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.statusbar.policy.CastController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/CastControllerImpl.class */
public class CastControllerImpl implements CastController {
    private boolean mCallbackRegistered;
    private final Context mContext;
    private boolean mDiscovering;
    private final DisplayManager mDisplayManager;
    private final MediaRouter mMediaRouter;
    private WifiP2pDevice mP2pDevice;
    private MediaProjectionInfo mProjection;
    private final MediaProjectionManager mProjectionManager;
    private final boolean mWfdSinkSupport;
    private final boolean mWfdSinkUibcSupport;
    private final ArrayList<CastController.Callback> mCallbacks = new ArrayList<>();
    private final ArrayMap<String, MediaRouter.RouteInfo> mRoutes = new ArrayMap<>();
    private final Object mDiscoveringLock = new Object();
    private final Object mProjectionLock = new Object();
    private final BroadcastReceiver mWfdReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.CastControllerImpl.1
        final CastControllerImpl this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("CastController", "onReceive(" + action + ")");
            if ("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(action)) {
                this.this$0.mP2pDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                this.this$0.fireOnWifiP2pDeviceChanged();
            } else if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(action)) {
                this.this$0.fireOnWfdStatusChanged();
            }
        }
    };
    private final MediaRouter.SimpleCallback mMediaCallback = new MediaRouter.SimpleCallback(this) { // from class: com.android.systemui.statusbar.policy.CastControllerImpl.2
        final CastControllerImpl this$0;

        {
            this.this$0 = this;
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            Log.d("CastController", "onRouteAdded: " + CastControllerImpl.routeToString(routeInfo));
            this.this$0.updateRemoteDisplays();
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            Log.d("CastController", "onRouteChanged: " + CastControllerImpl.routeToString(routeInfo));
            this.this$0.updateRemoteDisplays();
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            Log.d("CastController", "onRouteRemoved: " + CastControllerImpl.routeToString(routeInfo));
            this.this$0.updateRemoteDisplays();
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            Log.d("CastController", "onRouteSelected(" + i + "): " + CastControllerImpl.routeToString(routeInfo));
            this.this$0.updateRemoteDisplays();
        }

        @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
        public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            Log.d("CastController", "onRouteUnselected(" + i + "): " + CastControllerImpl.routeToString(routeInfo));
            this.this$0.updateRemoteDisplays();
        }
    };
    private final MediaProjectionManager.Callback mProjectionCallback = new MediaProjectionManager.Callback(this) { // from class: com.android.systemui.statusbar.policy.CastControllerImpl.3
        final CastControllerImpl this$0;

        {
            this.this$0 = this;
        }

        public void onStart(MediaProjectionInfo mediaProjectionInfo) {
            this.this$0.setProjection(mediaProjectionInfo, true);
        }

        public void onStop(MediaProjectionInfo mediaProjectionInfo) {
            this.this$0.setProjection(mediaProjectionInfo, false);
        }
    };

    public CastControllerImpl(Context context) {
        this.mContext = context;
        this.mMediaRouter = (MediaRouter) context.getSystemService("media_router");
        this.mProjectionManager = (MediaProjectionManager) context.getSystemService("media_projection");
        this.mProjection = this.mProjectionManager.getActiveProjectionInfo();
        this.mProjectionManager.addCallback(this.mProjectionCallback, new Handler());
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        this.mWfdSinkSupport = SystemProperties.get("ro.mtk_wfd_sink_support").equals("1");
        this.mWfdSinkUibcSupport = SystemProperties.get("ro.mtk_wfd_sink_uibc_support").equals("1");
        Log.d("CastController", "new CastController()");
    }

    private void ensureTagExists(MediaRouter.RouteInfo routeInfo) {
        if (routeInfo.getTag() == null) {
            routeInfo.setTag(UUID.randomUUID().toString());
        }
    }

    private void fireOnCastDevicesChanged() {
        for (CastController.Callback callback : this.mCallbacks) {
            fireOnCastDevicesChanged(callback);
        }
    }

    private void fireOnCastDevicesChanged(CastController.Callback callback) {
        callback.onCastDevicesChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireOnWfdStatusChanged() {
        for (CastController.Callback callback : this.mCallbacks) {
            fireOnWfdStatusChanged(callback);
        }
    }

    private void fireOnWfdStatusChanged(CastController.Callback callback) {
        callback.onWfdStatusChanged(this.mDisplayManager.getWifiDisplayStatus(), this.mDisplayManager.isSinkEnabled());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireOnWifiP2pDeviceChanged() {
        for (CastController.Callback callback : this.mCallbacks) {
            fireOnWifiP2pDeviceChanged(callback);
        }
    }

    private void fireOnWifiP2pDeviceChanged(CastController.Callback callback) {
        callback.onWifiP2pDeviceChanged(this.mP2pDevice);
    }

    private String getAppName(String str) {
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 0);
            if (applicationInfo != null) {
                CharSequence loadLabel = applicationInfo.loadLabel(packageManager);
                if (!TextUtils.isEmpty(loadLabel)) {
                    return loadLabel.toString();
                }
            }
            Log.w("CastController", "No label found for package: " + str);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("CastController", "Error getting appName for package: " + str, e);
        }
        return str;
    }

    private void handleDiscoveryChangeLocked() {
        if (this.mCallbackRegistered) {
            this.mMediaRouter.removeCallback(this.mMediaCallback);
            this.mCallbackRegistered = false;
        }
        if (this.mDiscovering) {
            this.mMediaRouter.addCallback(4, this.mMediaCallback, 1);
            this.mCallbackRegistered = true;
        } else if (this.mCallbacks.size() != 0) {
            this.mMediaRouter.addCallback(4, this.mMediaCallback, 8);
            this.mCallbackRegistered = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String routeToString(MediaRouter.RouteInfo routeInfo) {
        if (routeInfo == null) {
            return null;
        }
        StringBuilder append = new StringBuilder().append(routeInfo.getName()).append('/').append(routeInfo.getDescription()).append('@').append(routeInfo.getDeviceAddress()).append(",status=").append(routeInfo.getStatus());
        if (routeInfo.isDefault()) {
            append.append(",default");
        }
        if (routeInfo.isEnabled()) {
            append.append(",enabled");
        }
        if (routeInfo.isConnecting()) {
            append.append(",connecting");
        }
        if (routeInfo.isSelected()) {
            append.append(",selected");
        }
        return append.append(",id=").append(routeInfo.getTag()).toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setProjection(MediaProjectionInfo mediaProjectionInfo, boolean z) {
        boolean z2;
        MediaProjectionInfo mediaProjectionInfo2 = this.mProjection;
        synchronized (this.mProjectionLock) {
            boolean equals = Objects.equals(mediaProjectionInfo, this.mProjection);
            if (!z || equals) {
                z2 = false;
                if (!z) {
                    z2 = false;
                    if (equals) {
                        this.mProjection = null;
                        z2 = true;
                    }
                }
            } else {
                this.mProjection = mediaProjectionInfo;
                z2 = true;
            }
        }
        if (z2) {
            Log.d("CastController", "setProjection: " + mediaProjectionInfo2 + " -> " + this.mProjection);
            fireOnCastDevicesChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRemoteDisplays() {
        synchronized (this.mRoutes) {
            this.mRoutes.clear();
            int routeCount = this.mMediaRouter.getRouteCount();
            Log.d("CastController", "getRouteCount: " + routeCount);
            for (int i = 0; i < routeCount; i++) {
                MediaRouter.RouteInfo routeAt = this.mMediaRouter.getRouteAt(i);
                if (routeAt.isEnabled() && routeAt.matchesTypes(4)) {
                    ensureTagExists(routeAt);
                    this.mRoutes.put(routeAt.getTag().toString(), routeAt);
                }
            }
            MediaRouter.RouteInfo selectedRoute = this.mMediaRouter.getSelectedRoute(4);
            if (selectedRoute != null && !selectedRoute.isDefault()) {
                ensureTagExists(selectedRoute);
                this.mRoutes.put(selectedRoute.getTag().toString(), selectedRoute);
            }
        }
        fireOnCastDevicesChanged();
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public void addCallback(CastController.Callback callback) {
        this.mCallbacks.add(callback);
        fireOnCastDevicesChanged(callback);
        synchronized (this.mDiscoveringLock) {
            handleDiscoveryChangeLocked();
        }
        Log.d("CastController", "addCallback");
        if (isWfdSinkSupported()) {
            fireOnWfdStatusChanged(callback);
            fireOnWifiP2pDeviceChanged(callback);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("CastController state:");
        printWriter.print("  mDiscovering=");
        printWriter.println(this.mDiscovering);
        printWriter.print("  mCallbackRegistered=");
        printWriter.println(this.mCallbackRegistered);
        printWriter.print("  mCallbacks.size=");
        printWriter.println(this.mCallbacks.size());
        printWriter.print("  mRoutes.size=");
        printWriter.println(this.mRoutes.size());
        for (int i = 0; i < this.mRoutes.size(); i++) {
            printWriter.print("    ");
            printWriter.println(routeToString(this.mRoutes.valueAt(i)));
        }
        printWriter.print("  mProjection=");
        printWriter.println(this.mProjection);
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public Set<CastController.CastDevice> getCastDevices() {
        ArraySet arraySet = new ArraySet();
        Log.d("CastController", "getCastDevices: " + (this.mProjection != null));
        synchronized (this.mProjectionLock) {
            if (this.mProjection != null) {
                CastController.CastDevice castDevice = new CastController.CastDevice();
                castDevice.id = this.mProjection.getPackageName();
                castDevice.name = getAppName(this.mProjection.getPackageName());
                castDevice.description = this.mContext.getString(2131493555);
                castDevice.state = 2;
                castDevice.tag = this.mProjection;
                arraySet.add(castDevice);
                return arraySet;
            }
            synchronized (this.mRoutes) {
                for (MediaRouter.RouteInfo routeInfo : this.mRoutes.values()) {
                    CastController.CastDevice castDevice2 = new CastController.CastDevice();
                    castDevice2.id = routeInfo.getTag().toString();
                    CharSequence name = routeInfo.getName(this.mContext);
                    castDevice2.name = TextUtils.isEmpty(name) ? routeInfo.getDeviceAddress() : name.toString();
                    CharSequence description = routeInfo.getDescription();
                    castDevice2.description = description != null ? description.toString() : null;
                    castDevice2.state = routeInfo.isConnecting() ? 1 : (routeInfo.isSelected() && routeInfo.getStatusCode() == 6) ? 2 : 0;
                    castDevice2.tag = routeInfo;
                    arraySet.add(castDevice2);
                }
            }
            return arraySet;
        }
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public WifiP2pDevice getWifiP2pDev() {
        return this.mP2pDevice;
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public boolean isNeedShowWfdSink() {
        boolean z = false;
        if (isWfdSinkSupported()) {
            WifiDisplayStatus wifiDisplayStatus = this.mDisplayManager.getWifiDisplayStatus();
            z = wifiDisplayStatus != null ? wifiDisplayStatus.getFeatureState() == 3 : false;
        }
        Log.d("CastController", "needAddWfdSink: " + z);
        return z;
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public boolean isWfdSinkSupported() {
        return this.mWfdSinkSupport;
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public void removeCallback(CastController.Callback callback) {
        Log.d("CastController", "removeCallback");
        this.mCallbacks.remove(callback);
        synchronized (this.mDiscoveringLock) {
            handleDiscoveryChangeLocked();
        }
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public void setCurrentUserId(int i) {
        this.mMediaRouter.rebindAsUser(i);
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public void setDiscovering(boolean z) {
        synchronized (this.mDiscoveringLock) {
            if (this.mDiscovering == z) {
                return;
            }
            this.mDiscovering = z;
            Log.d("CastController", "setDiscovering: " + z);
            handleDiscoveryChangeLocked();
        }
    }

    @Override // com.android.systemui.statusbar.policy.Listenable
    public void setListening(boolean z) {
        Log.d("CastController", "register listener: " + z);
        if (isWfdSinkSupported()) {
            if (!z) {
                this.mContext.unregisterReceiver(this.mWfdReceiver);
                return;
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
            intentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
            this.mContext.registerReceiver(this.mWfdReceiver, intentFilter);
        }
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public void startCasting(CastController.CastDevice castDevice) {
        if (castDevice == null || castDevice.tag == null) {
            return;
        }
        MediaRouter.RouteInfo routeInfo = (MediaRouter.RouteInfo) castDevice.tag;
        Log.d("CastController", "startCasting: " + routeToString(routeInfo));
        this.mMediaRouter.selectRoute(4, routeInfo);
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public void stopCasting(CastController.CastDevice castDevice) {
        boolean z = castDevice.tag instanceof MediaProjectionInfo;
        Log.d("CastController", "stopCasting isProjection=" + z);
        if (!z) {
            this.mMediaRouter.getDefaultRoute().select();
            return;
        }
        MediaProjectionInfo mediaProjectionInfo = (MediaProjectionInfo) castDevice.tag;
        if (Objects.equals(this.mProjectionManager.getActiveProjectionInfo(), mediaProjectionInfo)) {
            this.mProjectionManager.stopActiveProjection();
        } else {
            Log.w("CastController", "Projection is no longer active: " + mediaProjectionInfo);
        }
    }

    @Override // com.android.systemui.statusbar.policy.CastController
    public void updateWfdFloatMenu(boolean z) {
        Log.d("CastController", "updateWfdFloatMenu: " + z);
        if (isWfdSinkSupported() && this.mWfdSinkUibcSupport) {
            Intent intent = new Intent();
            intent.setClassName("com.mediatek.floatmenu", "com.mediatek.floatmenu.FloatMenuService");
            if (z) {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } else {
                this.mContext.stopServiceAsUser(intent, UserHandle.CURRENT);
            }
        }
    }
}
