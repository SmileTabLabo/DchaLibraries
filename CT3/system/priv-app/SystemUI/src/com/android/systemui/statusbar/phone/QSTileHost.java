package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.BatteryTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.IntentTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NightModeController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.NightModeTile;
import com.android.systemui.tuner.TunerService;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.qs.tiles.HotKnotTile;
import com.mediatek.systemui.qs.tiles.ext.ApnSettingsTile;
import com.mediatek.systemui.qs.tiles.ext.DualSimSettingsTile;
import com.mediatek.systemui.qs.tiles.ext.MobileDataTile;
import com.mediatek.systemui.qs.tiles.ext.SimDataConnectionTile;
import com.mediatek.systemui.statusbar.policy.HotKnotController;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/QSTileHost.class */
public class QSTileHost implements QSTile.Host, TunerService.Tunable {
    private static final boolean DEBUG = Log.isLoggable("QSTileHost", 3);
    private final AutoTileManager mAutoTiles;
    private final BatteryController mBattery;
    private final BluetoothController mBluetooth;
    private final CastController mCast;
    private final Context mContext;
    private int mCurrentUser;
    private final FlashlightController mFlashlight;
    private View mHeader;
    private final HotKnotController mHotKnot;
    private final HotspotController mHotspot;
    private final StatusBarIconController mIconController;
    private final KeyguardMonitor mKeyguard;
    private final LocationController mLocation;
    private final Looper mLooper;
    private final NetworkController mNetwork;
    private final NextAlarmController mNextAlarmController;
    private final NightModeController mNightModeController;
    private final RotationLockController mRotation;
    private final SecurityController mSecurity;
    private final TileServices mServices;
    private final PhoneStatusBar mStatusBar;
    private final UserInfoController mUserInfoController;
    private final UserSwitcherController mUserSwitcherController;
    private final ZenModeController mZen;
    private final LinkedHashMap<String, QSTile<?>> mTiles = new LinkedHashMap<>();
    protected final ArrayList<String> mTileSpecs = new ArrayList<>();
    private final List<QSTile.Host.Callback> mCallbacks = new ArrayList();
    private final ManagedProfileController mProfileController = new ManagedProfileController(this);

    public QSTileHost(Context context, PhoneStatusBar phoneStatusBar, BluetoothController bluetoothController, LocationController locationController, RotationLockController rotationLockController, NetworkController networkController, ZenModeController zenModeController, HotspotController hotspotController, CastController castController, FlashlightController flashlightController, UserSwitcherController userSwitcherController, UserInfoController userInfoController, KeyguardMonitor keyguardMonitor, SecurityController securityController, BatteryController batteryController, StatusBarIconController statusBarIconController, NextAlarmController nextAlarmController, HotKnotController hotKnotController) {
        this.mContext = context;
        this.mStatusBar = phoneStatusBar;
        this.mBluetooth = bluetoothController;
        this.mLocation = locationController;
        this.mRotation = rotationLockController;
        this.mNetwork = networkController;
        this.mZen = zenModeController;
        this.mHotspot = hotspotController;
        this.mCast = castController;
        this.mFlashlight = flashlightController;
        this.mUserSwitcherController = userSwitcherController;
        this.mUserInfoController = userInfoController;
        this.mKeyguard = keyguardMonitor;
        this.mSecurity = securityController;
        this.mBattery = batteryController;
        this.mIconController = statusBarIconController;
        this.mNextAlarmController = nextAlarmController;
        this.mNightModeController = new NightModeController(this.mContext, true);
        this.mHotKnot = hotKnotController;
        HandlerThread handlerThread = new HandlerThread(QSTileHost.class.getSimpleName(), 10);
        handlerThread.start();
        this.mLooper = handlerThread.getLooper();
        this.mServices = new TileServices(this, this.mLooper);
        TunerService.get(this.mContext).addTunable(this, "sysui_qs_tiles");
        this.mAutoTiles = new AutoTileManager(context, this);
    }

    public void addCallback(QSTile.Host.Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void addTile(ComponentName componentName) {
        ArrayList arrayList = new ArrayList(this.mTileSpecs);
        arrayList.add(0, CustomTile.toSpec(componentName));
        changeTiles(this.mTileSpecs, arrayList);
    }

    public void addTile(String str) {
        List<String> loadTileSpecs = loadTileSpecs(this.mContext, Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", ActivityManager.getCurrentUser()));
        if (loadTileSpecs.contains(str)) {
            return;
        }
        loadTileSpecs.add(str);
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", loadTileSpecs), ActivityManager.getCurrentUser());
    }

    public void animateToggleQSExpansion() {
        this.mHeader.callOnClick();
    }

    public void changeTiles(List<String> list, List<String> list2) {
        int size = list.size();
        int size2 = list2.size();
        for (int i = 0; i < size; i++) {
            String str = list.get(i);
            if (str.startsWith("custom(") && !list2.contains(str)) {
                ComponentName componentFromSpec = CustomTile.getComponentFromSpec(str);
                TileLifecycleManager tileLifecycleManager = new TileLifecycleManager(new Handler(), this.mContext, this.mServices, new Tile(componentFromSpec), new Intent().setComponent(componentFromSpec), new UserHandle(ActivityManager.getCurrentUser()));
                tileLifecycleManager.onStopListening();
                tileLifecycleManager.onTileRemoved();
                tileLifecycleManager.flushMessagesAndUnbind();
            }
        }
        for (int i2 = 0; i2 < size2; i2++) {
            String str2 = list2.get(i2);
            if (str2.startsWith("custom(") && !list.contains(str2)) {
                ComponentName componentFromSpec2 = CustomTile.getComponentFromSpec(str2);
                TileLifecycleManager tileLifecycleManager2 = new TileLifecycleManager(new Handler(), this.mContext, this.mServices, new Tile(componentFromSpec2), new Intent().setComponent(componentFromSpec2), new UserHandle(ActivityManager.getCurrentUser()));
                tileLifecycleManager2.onTileAdded();
                tileLifecycleManager2.flushMessagesAndUnbind();
            }
        }
        if (DEBUG) {
            Log.d("QSTileHost", "saveCurrentTiles " + list2);
        }
        Settings.Secure.putStringForUser(getContext().getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", list2), ActivityManager.getCurrentUser());
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public void collapsePanels() {
        this.mStatusBar.postAnimateCollapsePanels();
    }

    public QSTile<?> createTile(String str) {
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager.getQuickSettingsPlugin(this.mContext);
        if (str.equals("wifi")) {
            return new WifiTile(this);
        }
        if (str.equals("bt")) {
            return new BluetoothTile(this);
        }
        if (str.equals("cell")) {
            return new CellularTile(this);
        }
        if (str.equals("dnd")) {
            return new DndTile(this);
        }
        if (str.equals("inversion")) {
            return new ColorInversionTile(this);
        }
        if (str.equals("airplane")) {
            return new AirplaneModeTile(this);
        }
        if (str.equals("work")) {
            return new WorkModeTile(this);
        }
        if (str.equals("rotation")) {
            return new RotationLockTile(this);
        }
        if (str.equals("flashlight")) {
            return new FlashlightTile(this);
        }
        if (str.equals("location")) {
            return new LocationTile(this);
        }
        if (str.equals("cast")) {
            return new CastTile(this);
        }
        if (str.equals("hotspot")) {
            return new HotspotTile(this);
        }
        if (str.equals("user")) {
            return new UserTile(this);
        }
        if (str.equals("battery")) {
            return new BatteryTile(this);
        }
        if (str.equals("saver")) {
            return new DataSaverTile(this);
        }
        if (str.equals("night")) {
            return new NightModeTile(this);
        }
        if (str.equals("hotknot") && SIMHelper.isMtkHotKnotSupport()) {
            return new HotKnotTile(this);
        }
        if (!str.equals("dataconnection") || SIMHelper.isWifiOnlyDevice()) {
            if (!str.equals("simdataconnection") || SIMHelper.isWifiOnlyDevice() || quickSettingsPlugin.customizeAddQSTile(new SimDataConnectionTile(this)) == null) {
                if (!str.equals("dulsimsettings") || SIMHelper.isWifiOnlyDevice() || quickSettingsPlugin.customizeAddQSTile(new DualSimSettingsTile(this)) == null) {
                    if (!str.equals("apnsettings") || SIMHelper.isWifiOnlyDevice() || quickSettingsPlugin.customizeAddQSTile(new ApnSettingsTile(this)) == null) {
                        if (quickSettingsPlugin.doOperatorSupportTile(str)) {
                            return (QSTile) quickSettingsPlugin.createTile(this, str);
                        }
                        if (str.startsWith("intent(")) {
                            return IntentTile.create(this, str);
                        }
                        if (str.startsWith("custom(")) {
                            return CustomTile.create(this, str);
                        }
                        Log.w("QSTileHost", "Bad tile spec: " + str);
                        return null;
                    }
                    return (ApnSettingsTile) quickSettingsPlugin.customizeAddQSTile(new ApnSettingsTile(this));
                }
                return (DualSimSettingsTile) quickSettingsPlugin.customizeAddQSTile(new DualSimSettingsTile(this));
            }
            return (SimDataConnectionTile) quickSettingsPlugin.customizeAddQSTile(new SimDataConnectionTile(this));
        }
        return new MobileDataTile(this);
    }

    public void destroy() {
        this.mAutoTiles.destroy();
        TunerService.get(this.mContext).removeTunable(this);
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public BatteryController getBatteryController() {
        return this.mBattery;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public BluetoothController getBluetoothController() {
        return this.mBluetooth;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public CastController getCastController() {
        return this.mCast;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public Context getContext() {
        return this.mContext;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public FlashlightController getFlashlightController() {
        return this.mFlashlight;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public HotKnotController getHotKnotController() {
        return this.mHotKnot;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public HotspotController getHotspotController() {
        return this.mHotspot;
    }

    public StatusBarIconController getIconController() {
        return this.mIconController;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public KeyguardMonitor getKeyguardMonitor() {
        return this.mKeyguard;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public LocationController getLocationController() {
        return this.mLocation;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public Looper getLooper() {
        return this.mLooper;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public ManagedProfileController getManagedProfileController() {
        return this.mProfileController;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public NetworkController getNetworkController() {
        return this.mNetwork;
    }

    public NextAlarmController getNextAlarmController() {
        return this.mNextAlarmController;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public NightModeController getNightModeController() {
        return this.mNightModeController;
    }

    public PhoneStatusBar getPhoneStatusBar() {
        return this.mStatusBar;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public RotationLockController getRotationLockController() {
        return this.mRotation;
    }

    public SecurityController getSecurityController() {
        return this.mSecurity;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public TileServices getTileServices() {
        return this.mServices;
    }

    public Collection<QSTile<?>> getTiles() {
        return this.mTiles.values();
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public UserInfoController getUserInfoController() {
        return this.mUserInfoController;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public UserSwitcherController getUserSwitcherController() {
        return this.mUserSwitcherController;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public ZenModeController getZenModeController() {
        return this.mZen;
    }

    protected List<String> loadTileSpecs(Context context, String str) {
        String str2;
        boolean z;
        Resources resources = context.getResources();
        String string = resources.getString(2131493276);
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager.getQuickSettingsPlugin(this.mContext);
        String customizeQuickSettingsTileOrder = quickSettingsPlugin.customizeQuickSettingsTileOrder(quickSettingsPlugin.addOpTileSpecs(string));
        Log.d("QSTileHost", "loadTileSpecs() default tile list: " + customizeQuickSettingsTileOrder);
        if (str == null) {
            String string2 = resources.getString(2131493277);
            str2 = string2;
            if (DEBUG) {
                Log.d("QSTileHost", "Loaded tile specs from config: " + string2);
                str2 = string2;
            }
        } else {
            str2 = str;
            if (DEBUG) {
                Log.d("QSTileHost", "Loaded tile specs from setting: " + str);
                str2 = str;
            }
        }
        ArrayList arrayList = new ArrayList();
        boolean z2 = false;
        String[] split = str2.split(",");
        int i = 0;
        int length = split.length;
        while (i < length) {
            String trim = split[i].trim();
            if (trim.isEmpty()) {
                z = z2;
            } else if (trim.equals("default")) {
                z = z2;
                if (!z2) {
                    arrayList.addAll(Arrays.asList(customizeQuickSettingsTileOrder.split(",")));
                    z = true;
                }
            } else {
                arrayList.add(trim);
                z = z2;
            }
            i++;
            z2 = z;
        }
        return arrayList;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_qs_tiles".equals(str)) {
            if (DEBUG) {
                Log.d("QSTileHost", "Recreating tiles");
            }
            List<String> loadTileSpecs = loadTileSpecs(this.mContext, str2);
            int currentUser = ActivityManager.getCurrentUser();
            if (loadTileSpecs.equals(this.mTileSpecs) && currentUser == this.mCurrentUser) {
                return;
            }
            Iterator<T> it = this.mTiles.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                if (!loadTileSpecs.contains(entry.getKey())) {
                    if (DEBUG) {
                        Log.d("QSTileHost", "Destroying tile: " + ((String) entry.getKey()));
                    }
                    ((QSTile) entry.getValue()).destroy();
                }
            }
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (String str3 : loadTileSpecs) {
                QSTile<?> qSTile = this.mTiles.get(str3);
                if (qSTile == null || ((qSTile instanceof CustomTile) && ((CustomTile) qSTile).getUser() != currentUser)) {
                    if (DEBUG) {
                        Log.d("QSTileHost", "Creating tile: " + str3);
                    }
                    try {
                        QSTile<?> createTile = createTile(str3);
                        if (createTile != null && createTile.isAvailable()) {
                            createTile.setTileSpec(str3);
                            linkedHashMap.put(str3, createTile);
                        }
                    } catch (Throwable th) {
                        Log.w("QSTileHost", "Error creating tile for spec: " + str3, th);
                    }
                } else {
                    if (DEBUG) {
                        Log.d("QSTileHost", "Adding " + qSTile);
                    }
                    qSTile.removeCallbacks();
                    linkedHashMap.put(str3, qSTile);
                }
            }
            this.mCurrentUser = currentUser;
            this.mTileSpecs.clear();
            this.mTileSpecs.addAll(loadTileSpecs);
            this.mTiles.clear();
            this.mTiles.putAll(linkedHashMap);
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                this.mCallbacks.get(i).onTilesChanged();
            }
        }
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public void openPanels() {
        this.mStatusBar.postAnimateOpenPanels();
    }

    public void removeCallback(QSTile.Host.Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public void removeTile(ComponentName componentName) {
        ArrayList arrayList = new ArrayList(this.mTileSpecs);
        arrayList.remove(CustomTile.toSpec(componentName));
        changeTiles(this.mTileSpecs, arrayList);
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public void removeTile(String str) {
        ArrayList arrayList = new ArrayList(this.mTileSpecs);
        arrayList.remove(str);
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", arrayList), ActivityManager.getCurrentUser());
    }

    public void setHeaderView(View view) {
        this.mHeader = view;
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public void startActivityDismissingKeyguard(PendingIntent pendingIntent) {
        if (pendingIntent != null) {
            this.mStatusBar.postStartActivityDismissingKeyguard(pendingIntent);
        }
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public void startActivityDismissingKeyguard(Intent intent) {
        if (intent != null) {
            this.mStatusBar.postStartActivityDismissingKeyguard(intent, 0);
        }
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public void startRunnableDismissingKeyguard(Runnable runnable) {
        this.mStatusBar.postQSRunnableDismissingKeyguard(runnable);
    }

    @Override // com.android.systemui.qs.QSTile.Host
    public void warn(String str, Throwable th) {
    }
}
