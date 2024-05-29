package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.MathUtils;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.ConfigurationChangedReceiver;
import com.android.systemui.DemoMode;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.WifiSignalController;
import com.mediatek.systemui.ext.IStatusBarPlmnPlugin;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
import com.mediatek.systemui.ext.OpSystemUICustomizationFactoryBase;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
/* loaded from: classes.dex */
public class NetworkControllerImpl extends BroadcastReceiver implements DataUsageController.NetworkNameProvider, ConfigurationChangedReceiver, DemoMode, Dumpable, NetworkController {
    static final boolean CHATTY;
    static final boolean DEBUG;
    private final AccessPointControllerImpl mAccessPoints;
    private boolean mAirplaneMode;
    private final CallbackHandler mCallbackHandler;
    private int mCellularSubId;
    private Config mConfig;
    private final BitSet mConnectedTransports;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private List<SubscriptionInfo> mCurrentSubscriptions;
    private int mCurrentUserId;
    private final DataSaverController mDataSaverController;
    private final DataUsageController mDataUsageController;
    private MobileSignalController mDefaultSignalController;
    private boolean mDemoInetCondition;
    private boolean mDemoMode;
    private WifiSignalController.WifiState mDemoWifiState;
    private boolean[] mEmergencyPhone;
    private int mEmergencySource;
    @VisibleForTesting
    final EthernetSignalController mEthernetSignalController;
    private final boolean mHasMobileDataFeature;
    private boolean mHasNoSubs;
    private boolean mInetCondition;
    private boolean mIsEmergency;
    @VisibleForTesting
    ServiceState mLastServiceState;
    @VisibleForTesting
    boolean mListening;
    private Locale mLocale;
    @VisibleForTesting
    final SparseArray<MobileSignalController> mMobileSignalControllers;
    String[] mNetworkName;
    private final TelephonyManager mPhone;
    private final Handler mReceiverHandler;
    private final Runnable mRegisterListeners;
    private int mSignalCallbackCount;
    private boolean mSimDetected;
    int mSlotCount;
    private IStatusBarPlmnPlugin mStatusBarPlmnPlugin;
    private ISystemUIStatusBarExt mStatusBarSystemUIExt;
    private final SubscriptionDefaults mSubDefaults;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener;
    private final SubscriptionManager mSubscriptionManager;
    private boolean mUserSetup;
    private final CurrentUserTracker mUserTracker;
    private final BitSet mValidatedTransports;
    private final WifiManager mWifiManager;
    @VisibleForTesting
    final WifiSignalController mWifiSignalController;

    static {
        boolean z = true;
        DEBUG = Log.isLoggable("NetworkController", 3) || FeatureOptions.LOG_ENABLE;
        if (!Log.isLoggable("NetworkControllerChat", 3) && !FeatureOptions.LOG_ENABLE) {
            z = false;
        }
        CHATTY = z;
    }

    public NetworkControllerImpl(Context context, Looper looper, DeviceProvisionedController deviceProvisionedController) {
        this(context, (ConnectivityManager) context.getSystemService("connectivity"), (TelephonyManager) context.getSystemService("phone"), (WifiManager) context.getSystemService("wifi"), SubscriptionManager.from(context), Config.readConfig(context), looper, new CallbackHandler(), new AccessPointControllerImpl(context), new DataUsageController(context), new SubscriptionDefaults(), deviceProvisionedController);
        this.mReceiverHandler.post(this.mRegisterListeners);
    }

    @VisibleForTesting
    NetworkControllerImpl(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, SubscriptionManager subscriptionManager, Config config, Looper looper, CallbackHandler callbackHandler, AccessPointControllerImpl accessPointControllerImpl, DataUsageController dataUsageController, SubscriptionDefaults subscriptionDefaults, final DeviceProvisionedController deviceProvisionedController) {
        this.mMobileSignalControllers = new SparseArray<>();
        this.mConnectedTransports = new BitSet();
        this.mValidatedTransports = new BitSet();
        this.mAirplaneMode = false;
        this.mLocale = null;
        this.mCurrentSubscriptions = new ArrayList();
        this.mCellularSubId = -1;
        this.mSlotCount = 0;
        this.mRegisterListeners = new Runnable() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.8
            @Override // java.lang.Runnable
            public void run() {
                NetworkControllerImpl.this.registerListeners();
            }
        };
        this.mContext = context;
        this.mConfig = config;
        this.mReceiverHandler = new Handler(looper);
        this.mCallbackHandler = callbackHandler;
        this.mDataSaverController = new DataSaverControllerImpl(context);
        this.mSubscriptionManager = subscriptionManager;
        this.mSubDefaults = subscriptionDefaults;
        this.mConnectivityManager = connectivityManager;
        this.mHasMobileDataFeature = this.mConnectivityManager.isNetworkSupported(0);
        this.mSlotCount = SIMHelper.getSlotCount();
        this.mNetworkName = new String[this.mSlotCount];
        this.mEmergencyPhone = new boolean[this.mSlotCount];
        this.mPhone = telephonyManager;
        this.mWifiManager = wifiManager;
        this.mLocale = this.mContext.getResources().getConfiguration().locale;
        this.mAccessPoints = accessPointControllerImpl;
        this.mDataUsageController = dataUsageController;
        this.mDataUsageController.setNetworkController(this);
        this.mDataUsageController.setCallback(new DataUsageController.Callback() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.1
            @Override // com.android.settingslib.net.DataUsageController.Callback
            public void onMobileDataEnabled(boolean z) {
                NetworkControllerImpl.this.mCallbackHandler.setMobileDataEnabled(z);
            }
        });
        this.mWifiSignalController = new WifiSignalController(this.mContext, this.mHasMobileDataFeature, this.mCallbackHandler, this, this.mWifiManager);
        this.mEthernetSignalController = new EthernetSignalController(this.mContext, this.mCallbackHandler, this);
        updateAirplaneMode(true);
        this.mUserTracker = new CurrentUserTracker(this.mContext) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.2
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                NetworkControllerImpl.this.onUserSwitched(i);
            }
        };
        this.mUserTracker.startTracking();
        deviceProvisionedController.addCallback(new DeviceProvisionedController.DeviceProvisionedListener() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.3
            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onUserSetupChanged() {
                NetworkControllerImpl.this.setUserSetupComplete(deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser()));
            }
        });
        this.mConnectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.4
            private Network mLastNetwork;
            private NetworkCapabilities mLastNetworkCapabilities;

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                boolean z = this.mLastNetworkCapabilities != null && this.mLastNetworkCapabilities.hasCapability(16);
                boolean hasCapability = networkCapabilities.hasCapability(16);
                if (network.equals(this.mLastNetwork) && networkCapabilities.equalsTransportTypes(this.mLastNetworkCapabilities) && hasCapability == z) {
                    return;
                }
                this.mLastNetwork = network;
                this.mLastNetworkCapabilities = networkCapabilities;
                NetworkControllerImpl.this.updateConnectivity();
            }
        }, this.mReceiverHandler);
        this.mStatusBarSystemUIExt = OpSystemUICustomizationFactoryBase.getOpFactory(context).makeSystemUIStatusBar(context);
        this.mStatusBarPlmnPlugin = OpSystemUICustomizationFactoryBase.getOpFactory(context).makeStatusBarPlmn(context);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public DataSaverController getDataSaverController() {
        return this.mDataSaverController;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerListeners() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).registerListener();
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener();
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.conn.INET_CONDITION_ACTION");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        addCustomizedAction(intentFilter);
        this.mContext.registerReceiver(this, intentFilter, null, this.mReceiverHandler);
        this.mListening = true;
        updateMobileControllers();
    }

    private void addCustomizedAction(IntentFilter intentFilter) {
        intentFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intentFilter.addAction("com.mediatek.server.lwa.LWA_STATE_CHANGE_ACTION");
        intentFilter.addAction("com.mediatek.ims.MTK_IMS_SERVICE_UP");
    }

    private void unregisterListeners() {
        this.mListening = false;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).unregisterListener();
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mContext.unregisterReceiver(this);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public NetworkController.AccessPointController getAccessPointController() {
        return this.mAccessPoints;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public DataUsageController getMobileDataController() {
        return this.mDataUsageController;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void addEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, true);
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly());
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void removeEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, false);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasMobileDataFeature() {
        return this.mHasMobileDataFeature;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasVoiceCallingFeature() {
        return this.mPhone.getPhoneType() != 0;
    }

    private MobileSignalController getDataController() {
        int defaultDataSubId = this.mSubDefaults.getDefaultDataSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultDataSubId)) {
            if (DEBUG) {
                Log.e("NetworkController", "No data sim selected");
            }
            return this.mDefaultSignalController;
        } else if (this.mMobileSignalControllers.indexOfKey(defaultDataSubId) >= 0) {
            return this.mMobileSignalControllers.get(defaultDataSubId);
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for data sub: " + defaultDataSubId);
            }
            return this.mDefaultSignalController;
        }
    }

    @Override // com.android.settingslib.net.DataUsageController.NetworkNameProvider, com.android.systemui.statusbar.policy.NetworkController
    public String getMobileDataNetworkName() {
        MobileSignalController dataController = getDataController();
        return dataController != null ? dataController.getState().networkNameData : "";
    }

    public boolean isEmergencyOnly() {
        if (this.mMobileSignalControllers.size() == 0) {
            Log.d("NetworkController", "isEmergencyOnly No sims ");
            this.mEmergencySource = 0;
            for (int i = 0; i < this.mEmergencyPhone.length; i++) {
                if (this.mEmergencyPhone[i]) {
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency in phone " + i);
                    }
                    return true;
                }
            }
            return false;
        }
        int defaultVoiceSubId = this.mSubDefaults.getDefaultVoiceSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultVoiceSubId)) {
            for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
                MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i2);
                if (!valueAt.getState().isEmergency) {
                    this.mEmergencySource = 100 + valueAt.mSubscriptionInfo.getSubscriptionId();
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency " + valueAt.mTag);
                    }
                    return false;
                }
            }
        }
        if (this.mMobileSignalControllers.indexOfKey(defaultVoiceSubId) >= 0) {
            this.mEmergencySource = 200 + defaultVoiceSubId;
            if (DEBUG) {
                Log.d("NetworkController", "Getting emergency from " + defaultVoiceSubId);
            }
            return this.mMobileSignalControllers.get(defaultVoiceSubId).getState().isEmergency;
        } else if (this.mMobileSignalControllers.size() == 1) {
            this.mEmergencySource = 400 + this.mMobileSignalControllers.keyAt(0);
            if (DEBUG) {
                Log.d("NetworkController", "Getting assumed emergency from " + this.mMobileSignalControllers.keyAt(0));
            }
            return this.mMobileSignalControllers.valueAt(0).getState().isEmergency;
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for voice sub: " + defaultVoiceSubId);
            }
            this.mEmergencySource = 300 + defaultVoiceSubId;
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void recalculateEmergency() {
        this.mIsEmergency = isEmergencyOnly();
        this.mCallbackHandler.setEmergencyCallsOnly(this.mIsEmergency);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(NetworkController.SignalCallback signalCallback) {
        if (CHATTY) {
            Log.d("NetworkController", "addCallback, cb = " + signalCallback + ", mCurrentSubscriptions = " + this.mCurrentSubscriptions);
        }
        signalCallback.setSubs(this.mCurrentSubscriptions);
        signalCallback.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, R.drawable.stat_sys_airplane_mode, R.string.accessibility_airplane_mode, this.mContext));
        this.mWifiSignalController.notifyListeners(signalCallback);
        this.mEthernetSignalController.notifyListeners(signalCallback);
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).notifyListeners(signalCallback);
        }
        this.mCallbackHandler.setListening(signalCallback, true);
        signalCallback.setNoSims(this.mHasNoSubs, this.mSimDetected);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(NetworkController.SignalCallback signalCallback) {
        if (CHATTY) {
            Log.d("NetworkController", "removeCallback, cb = " + signalCallback);
        }
        this.mCallbackHandler.setListening(signalCallback, false);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.systemui.statusbar.policy.NetworkControllerImpl$5] */
    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void setWifiEnabled(final boolean z) {
        new AsyncTask<Void, Void, Void>() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.5
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                NetworkControllerImpl.this.mWifiManager.setWifiEnabled(z);
                return null;
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        this.mAccessPoints.onUserSwitched(i);
        updateConnectivity();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        char c;
        if (CHATTY) {
            Log.d("NetworkController", "onReceive: intent=" + intent);
        }
        String action = intent.getAction();
        switch (action.hashCode()) {
            case -2104353374:
                if (action.equals("android.intent.action.SERVICE_STATE")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1465084191:
                if (action.equals("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1172645946:
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1138588223:
                if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1076576821:
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -337898046:
                if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -229777127:
                if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -194455430:
                if (action.equals("com.mediatek.ims.MTK_IMS_SERVICE_UP")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -25388475:
                if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 623179603:
                if (action.equals("android.net.conn.INET_CONDITION_ACTION")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1533991190:
                if (action.equals("com.mediatek.server.lwa.LWA_STATE_CHANGE_ACTION")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                updateConnectivity();
                return;
            case 2:
                refreshLocale();
                updateAirplaneMode(false);
                return;
            case 3:
                recalculateEmergency();
                return;
            case 4:
                break;
            case 5:
                if (!intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                    updateMobileControllers();
                    return;
                }
                return;
            case 6:
                refreshPlmnCarrierLabel();
                updateMobileControllersEx(intent);
                return;
            case 7:
                this.mLastServiceState = ServiceState.newFromBundle(intent.getExtras());
                if (this.mLastServiceState != null) {
                    int intExtra = intent.getIntExtra("phone", 0);
                    this.mEmergencyPhone[intExtra] = this.mLastServiceState.isEmergencyOnly();
                    if (DEBUG) {
                        Log.d("NetworkController", "Service State changed...phoneId: " + intExtra + " ,isEmergencyOnly: " + this.mEmergencyPhone[intExtra]);
                    }
                    if (this.mMobileSignalControllers.size() == 0) {
                        recalculateEmergency();
                        return;
                    }
                    return;
                }
                return;
            case '\b':
                this.mConfig = Config.readConfig(this.mContext);
                this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk
                    @Override // java.lang.Runnable
                    public final void run() {
                        NetworkControllerImpl.this.handleConfigurationChanged();
                    }
                });
                return;
            case '\t':
                handleLwaAction(intent);
                return;
            case '\n':
                handleIMSServiceUp();
                return;
            default:
                int intExtra2 = intent.getIntExtra("subscription", -1);
                if (SubscriptionManager.isValidSubscriptionId(intExtra2)) {
                    if (this.mMobileSignalControllers.indexOfKey(intExtra2) >= 0) {
                        this.mMobileSignalControllers.get(intExtra2).handleBroadcast(intent);
                        return;
                    } else {
                        updateMobileControllers();
                        return;
                    }
                }
                this.mWifiSignalController.handleBroadcast(intent);
                return;
        }
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).handleBroadcast(intent);
        }
    }

    @Override // com.android.systemui.ConfigurationChangedReceiver
    public void onConfigurationChanged(Configuration configuration) {
        this.mConfig = Config.readConfig(this.mContext);
        this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.6
            @Override // java.lang.Runnable
            public void run() {
                NetworkControllerImpl.this.handleConfigurationChanged();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleConfigurationChanged() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setConfiguration(this.mConfig);
        }
        refreshLocale();
    }

    private void updateMobileControllersEx(Intent intent) {
        int i;
        if (intent != null) {
            i = intent.getIntExtra("simDetectStatus", 0);
            Log.d("NetworkController", "updateMobileControllers detectedType: " + i);
        } else {
            i = 4;
        }
        if (i != 3) {
            updateNoSims();
        } else {
            updateMobileControllers();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMobileControllers() {
        SIMHelper.updateSIMInfos(this.mContext);
        if (!this.mListening) {
            if (DEBUG) {
                Log.d("NetworkController", "updateMobileControllers: it's not listening");
                return;
            }
            return;
        }
        doUpdateMobileControllers();
    }

    @VisibleForTesting
    void doUpdateMobileControllers() {
        List<SubscriptionInfo> activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            Log.d("NetworkController", "subscriptions is null");
            activeSubscriptionInfoList = Collections.emptyList();
        }
        if (CHATTY) {
            Log.d("NetworkController", "doUpdateMobileControllers, subscriptions = " + activeSubscriptionInfoList);
        }
        if (hasCorrectMobileControllers(activeSubscriptionInfoList)) {
            updateNoSims();
            return;
        }
        setCurrentSubscriptions(activeSubscriptionInfoList);
        updateNoSims();
        recalculateEmergency();
    }

    @VisibleForTesting
    protected void updateNoSims() {
        boolean z = this.mHasMobileDataFeature && this.mMobileSignalControllers.size() == 0;
        boolean hasAnySim = hasAnySim();
        if (z != this.mHasNoSubs || hasAnySim != this.mSimDetected) {
            this.mHasNoSubs = z;
            this.mSimDetected = hasAnySim;
            this.mCallbackHandler.setNoSims(this.mHasNoSubs, this.mSimDetected);
        }
    }

    private boolean hasAnySim() {
        int simCount = this.mPhone.getSimCount();
        for (int i = 0; i < simCount; i++) {
            int simState = this.mPhone.getSimState(i);
            if (simState != 1 && simState != 0) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    void setCurrentSubscriptions(List<SubscriptionInfo> list) {
        int i;
        Collections.sort(list, new Comparator<SubscriptionInfo>() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.7
            @Override // java.util.Comparator
            public int compare(SubscriptionInfo subscriptionInfo, SubscriptionInfo subscriptionInfo2) {
                if (subscriptionInfo.getSimSlotIndex() == subscriptionInfo2.getSimSlotIndex()) {
                    return subscriptionInfo.getSubscriptionId() - subscriptionInfo2.getSubscriptionId();
                }
                return subscriptionInfo.getSimSlotIndex() - subscriptionInfo2.getSimSlotIndex();
            }
        });
        this.mCurrentSubscriptions = list;
        if (CHATTY) {
            Log.d("NetworkController", "setCurrentSubscriptions, controllers before update = " + this.mMobileSignalControllers);
        }
        SparseArray sparseArray = new SparseArray();
        for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
            sparseArray.put(this.mMobileSignalControllers.keyAt(i2), this.mMobileSignalControllers.valueAt(i2));
        }
        this.mMobileSignalControllers.clear();
        int size = list.size();
        int i3 = 0;
        while (i3 < size) {
            int subscriptionId = list.get(i3).getSubscriptionId();
            if (sparseArray.indexOfKey(subscriptionId) >= 0 && ((MobileSignalController) sparseArray.get(subscriptionId)).mSubscriptionInfo.getSimSlotIndex() == list.get(i3).getSimSlotIndex()) {
                MobileSignalController mobileSignalController = (MobileSignalController) sparseArray.get(subscriptionId);
                mobileSignalController.mSubscriptionInfo = list.get(i3);
                this.mMobileSignalControllers.put(subscriptionId, mobileSignalController);
                sparseArray.remove(subscriptionId);
                i = size;
            } else {
                i = size;
                MobileSignalController mobileSignalController2 = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone, this.mCallbackHandler, this, list.get(i3), this.mSubDefaults, this.mReceiverHandler.getLooper());
                mobileSignalController2.setUserSetupComplete(this.mUserSetup);
                this.mMobileSignalControllers.put(subscriptionId, mobileSignalController2);
                if (list.get(i3).getSimSlotIndex() == 0) {
                    this.mDefaultSignalController = mobileSignalController2;
                }
                if (this.mListening) {
                    mobileSignalController2.registerListener();
                }
            }
            i3++;
            size = i;
        }
        if (CHATTY) {
            Log.d("NetworkController", "setCurrentSubscriptions, removed controllers = " + sparseArray);
            Log.d("NetworkController", "setCurrentSubscriptions, controllers after update = " + this.mMobileSignalControllers);
        }
        if (this.mListening) {
            for (int i4 = 0; i4 < sparseArray.size(); i4++) {
                int keyAt = sparseArray.keyAt(i4);
                if (sparseArray.get(keyAt) == this.mDefaultSignalController) {
                    this.mDefaultSignalController = null;
                }
                ((MobileSignalController) sparseArray.get(keyAt)).unregisterListener();
            }
        }
        this.mCallbackHandler.setSubs(list);
        notifyAllListeners();
        pushConnectivityToSignals();
        updateAirplaneMode(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setUserSetupComplete(final boolean z) {
        this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$NetworkControllerImpl$ip_KPuTyKF5u8IR4L3OPJ6WObYU
            @Override // java.lang.Runnable
            public final void run() {
                NetworkControllerImpl.this.handleSetUserSetupComplete(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleSetUserSetupComplete(boolean z) {
        this.mUserSetup = z;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setUserSetupComplete(this.mUserSetup);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:9:0x001f  */
    @VisibleForTesting
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    boolean hasCorrectMobileControllers(List<SubscriptionInfo> list) {
        if (list.size() != this.mMobileSignalControllers.size()) {
            Log.d("NetworkController", "size not equals, reset subInfo");
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : list) {
            MobileSignalController mobileSignalController = this.mMobileSignalControllers.get(subscriptionInfo.getSubscriptionId());
            if (mobileSignalController == null || mobileSignalController.mSubscriptionInfo.getSimSlotIndex() != subscriptionInfo.getSimSlotIndex()) {
                Log.d("NetworkController", "info_subId = " + subscriptionInfo.getSubscriptionId() + " info_slotId = " + subscriptionInfo.getSimSlotIndex());
                return false;
            }
            while (r5.hasNext()) {
            }
        }
        if (this.mSignalCallbackCount != this.mCallbackHandler.getSignalCallbackCount()) {
            Log.d("NetworkController", "signal callbacks count not equals, reset subInfo, old = " + this.mSignalCallbackCount + ", new = " + this.mCallbackHandler.getSignalCallbackCount());
            this.mSignalCallbackCount = this.mCallbackHandler.getSignalCallbackCount();
            return false;
        }
        return true;
    }

    private void updateAirplaneMode(boolean z) {
        boolean z2 = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        if (CHATTY) {
            Log.d("NetworkController", "updateAirplaneMode, airplaneMode =" + z2);
        }
        updateAirplaneMode(z2, z);
    }

    private void updateAirplaneMode(boolean z, boolean z2) {
        if (z != this.mAirplaneMode || z2) {
            this.mAirplaneMode = z;
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                this.mMobileSignalControllers.valueAt(i).setAirplaneMode(this.mAirplaneMode);
            }
            notifyListeners();
        }
    }

    private void refreshLocale() {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (!locale.equals(this.mLocale)) {
            this.mLocale = locale;
            notifyAllListeners();
        }
    }

    private void notifyAllListeners() {
        notifyListeners();
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).notifyListeners();
        }
        this.mWifiSignalController.notifyListeners();
        this.mEthernetSignalController.notifyListeners();
    }

    private void notifyListeners() {
        this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, R.drawable.stat_sys_airplane_mode, R.string.accessibility_airplane_mode, this.mContext));
        this.mCallbackHandler.setNoSims(this.mHasNoSubs, this.mSimDetected);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConnectivity() {
        NetworkCapabilities[] defaultNetworkCapabilitiesForUser;
        int[] transportTypes;
        this.mConnectedTransports.clear();
        this.mValidatedTransports.clear();
        SubscriptionManager subscriptionManager = this.mSubscriptionManager;
        this.mCellularSubId = -1;
        for (NetworkCapabilities networkCapabilities : this.mConnectivityManager.getDefaultNetworkCapabilitiesForUser(this.mCurrentUserId)) {
            for (int i : networkCapabilities.getTransportTypes()) {
                this.mConnectedTransports.set(i);
                if (i == 0) {
                    this.mCellularSubId = getSubIdFromNetrequest(networkCapabilities.getNetworkSpecifier());
                    Log.d("NetworkController", "mCellularSubId = " + this.mCellularSubId);
                }
                if (networkCapabilities.hasCapability(16)) {
                    this.mValidatedTransports.set(i);
                }
            }
        }
        if (CHATTY) {
            Log.d("NetworkController", "updateConnectivity: mConnectedTransports=" + this.mConnectedTransports);
            Log.d("NetworkController", "updateConnectivity: mValidatedTransports=" + this.mValidatedTransports);
        }
        this.mInetCondition = !this.mValidatedTransports.isEmpty();
        pushConnectivityToSignals();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isCellularConnected(int i) {
        return this.mCellularSubId == i;
    }

    private int getSubIdFromNetrequest(NetworkSpecifier networkSpecifier) {
        SubscriptionManager subscriptionManager = this.mSubscriptionManager;
        if (networkSpecifier instanceof StringNetworkSpecifier) {
            try {
                return Integer.parseInt(((StringNetworkSpecifier) networkSpecifier).specifier);
            } catch (NumberFormatException e) {
                Log.e("NetworkController", "NumberFormatException on ");
            }
        }
        return -1;
    }

    private void pushConnectivityToSignals() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
        this.mWifiSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        this.mEthernetSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NetworkController state:");
        printWriter.println("  - telephony ------");
        printWriter.print("  hasVoiceCallingFeature()=");
        printWriter.println(hasVoiceCallingFeature());
        printWriter.println("  - connectivity ------");
        printWriter.print("  mConnectedTransports=");
        printWriter.println(this.mConnectedTransports);
        printWriter.print("  mValidatedTransports=");
        printWriter.println(this.mValidatedTransports);
        printWriter.print("  mInetCondition=");
        printWriter.println(this.mInetCondition);
        printWriter.print("  mAirplaneMode=");
        printWriter.println(this.mAirplaneMode);
        printWriter.print("  mLocale=");
        printWriter.println(this.mLocale);
        printWriter.print("  mLastServiceState=");
        printWriter.println(this.mLastServiceState);
        printWriter.print("  mIsEmergency=");
        printWriter.println(this.mIsEmergency);
        printWriter.print("  mEmergencySource=");
        printWriter.println(emergencyToString(this.mEmergencySource));
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).dump(printWriter);
        }
        this.mWifiSignalController.dump(printWriter);
        this.mEthernetSignalController.dump(printWriter);
        this.mAccessPoints.dump(printWriter);
    }

    private static final String emergencyToString(int i) {
        if (i > 300) {
            return "ASSUMED_VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i > 300) {
            return "NO_SUB(" + (i - 300) + ")";
        } else if (i > 200) {
            return "VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i > 100) {
            return "FIRST_CONTROLLER(" + (i - 100) + ")";
        } else if (i == 0) {
            return "NO_CONTROLLERS";
        } else {
            return "UNKNOWN_SOURCE";
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:72:0x0162  */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0168  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x016e  */
    /* JADX WARN: Removed duplicated region for block: B:75:0x0174  */
    @Override // com.android.systemui.DemoMode
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void dispatchDemoCommand(String str, Bundle bundle) {
        int parseInt;
        MobileSignalController.MobileIconGroup mobileIconGroup;
        boolean z;
        if (!this.mDemoMode && str.equals("enter")) {
            if (DEBUG) {
                Log.d("NetworkController", "Entering demo mode");
            }
            unregisterListeners();
            this.mDemoMode = true;
            this.mDemoInetCondition = this.mInetCondition;
            this.mDemoWifiState = this.mWifiSignalController.getState();
            this.mDemoWifiState.ssid = "DemoMode";
            return;
        }
        int i = 0;
        if (this.mDemoMode && str.equals("exit")) {
            if (DEBUG) {
                Log.d("NetworkController", "Exiting demo mode");
            }
            this.mDemoMode = false;
            updateMobileControllers();
            while (i < this.mMobileSignalControllers.size()) {
                this.mMobileSignalControllers.valueAt(i).resetLastState();
                i++;
            }
            this.mWifiSignalController.resetLastState();
            this.mReceiverHandler.post(this.mRegisterListeners);
            notifyAllListeners();
        } else if (this.mDemoMode && str.equals("network")) {
            String string = bundle.getString("airplane");
            if (string != null) {
                this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(string.equals("show"), R.drawable.stat_sys_airplane_mode, R.string.accessibility_airplane_mode, this.mContext));
            }
            String string2 = bundle.getString("fully");
            if (string2 != null) {
                this.mDemoInetCondition = Boolean.parseBoolean(string2);
                BitSet bitSet = new BitSet();
                if (this.mDemoInetCondition) {
                    bitSet.set(this.mWifiSignalController.mTransportType);
                }
                this.mWifiSignalController.updateConnectivity(bitSet, bitSet);
                for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
                    MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i2);
                    if (this.mDemoInetCondition) {
                        bitSet.set(valueAt.mTransportType);
                    }
                    valueAt.updateConnectivity(bitSet, bitSet);
                }
            }
            String string3 = bundle.getString("wifi");
            char c = 65535;
            if (string3 != null) {
                boolean equals = string3.equals("show");
                String string4 = bundle.getString("level");
                if (string4 != null) {
                    this.mDemoWifiState.level = string4.equals("null") ? -1 : Math.min(Integer.parseInt(string4), WifiIcons.WIFI_LEVEL_COUNT - 1);
                    this.mDemoWifiState.connected = this.mDemoWifiState.level >= 0;
                }
                String string5 = bundle.getString("activity");
                if (string5 != null) {
                    int hashCode = string5.hashCode();
                    if (hashCode == 3365) {
                        if (string5.equals("in")) {
                            z = true;
                            switch (z) {
                            }
                        }
                        z = true;
                        switch (z) {
                        }
                    } else if (hashCode != 110414) {
                        if (hashCode == 100357129 && string5.equals("inout")) {
                            z = false;
                            switch (z) {
                                case false:
                                    this.mWifiSignalController.setActivity(3);
                                    break;
                                case true:
                                    this.mWifiSignalController.setActivity(1);
                                    break;
                                case true:
                                    this.mWifiSignalController.setActivity(2);
                                    break;
                                default:
                                    this.mWifiSignalController.setActivity(0);
                                    break;
                            }
                        }
                        z = true;
                        switch (z) {
                        }
                    } else {
                        if (string5.equals("out")) {
                            z = true;
                            switch (z) {
                            }
                        }
                        z = true;
                        switch (z) {
                        }
                    }
                } else {
                    this.mWifiSignalController.setActivity(0);
                }
                String string6 = bundle.getString("ssid");
                if (string6 != null) {
                    this.mDemoWifiState.ssid = string6;
                }
                this.mDemoWifiState.enabled = equals;
                this.mWifiSignalController.notifyListeners();
            }
            String string7 = bundle.getString("sims");
            if (string7 != null) {
                int constrain = MathUtils.constrain(Integer.parseInt(string7), 1, 8);
                ArrayList arrayList = new ArrayList();
                if (constrain != this.mMobileSignalControllers.size()) {
                    this.mMobileSignalControllers.clear();
                    int activeSubscriptionInfoCountMax = this.mSubscriptionManager.getActiveSubscriptionInfoCountMax();
                    for (int i3 = activeSubscriptionInfoCountMax; i3 < activeSubscriptionInfoCountMax + constrain; i3++) {
                        arrayList.add(addSignalController(i3, i3));
                    }
                    this.mCallbackHandler.setSubs(arrayList);
                    for (int i4 = 0; i4 < this.mMobileSignalControllers.size(); i4++) {
                        this.mMobileSignalControllers.get(this.mMobileSignalControllers.keyAt(i4)).notifyListeners();
                    }
                }
            }
            String string8 = bundle.getString("nosim");
            if (string8 != null) {
                this.mHasNoSubs = string8.equals("show");
                this.mCallbackHandler.setNoSims(this.mHasNoSubs, this.mSimDetected);
            }
            String string9 = bundle.getString("mobile");
            if (string9 != null) {
                boolean equals2 = string9.equals("show");
                String string10 = bundle.getString("datatype");
                String string11 = bundle.getString("slot");
                if (!TextUtils.isEmpty(string11)) {
                    parseInt = Integer.parseInt(string11);
                } else {
                    parseInt = 0;
                }
                int constrain2 = MathUtils.constrain(parseInt, 0, 8);
                ArrayList arrayList2 = new ArrayList();
                while (this.mMobileSignalControllers.size() <= constrain2) {
                    int size = this.mMobileSignalControllers.size();
                    arrayList2.add(addSignalController(size, size));
                }
                if (!arrayList2.isEmpty()) {
                    this.mCallbackHandler.setSubs(arrayList2);
                }
                MobileSignalController valueAt2 = this.mMobileSignalControllers.valueAt(constrain2);
                valueAt2.getState().dataSim = string10 != null;
                valueAt2.getState().isDefault = string10 != null;
                valueAt2.getState().dataConnected = string10 != null;
                if (string10 != null) {
                    MobileSignalController.MobileState state = valueAt2.getState();
                    if (string10.equals("1x")) {
                        mobileIconGroup = TelephonyIcons.ONE_X;
                    } else if (string10.equals("3g")) {
                        mobileIconGroup = TelephonyIcons.THREE_G;
                    } else if (string10.equals("4g")) {
                        mobileIconGroup = TelephonyIcons.FOUR_G;
                    } else if (string10.equals("4g+")) {
                        mobileIconGroup = TelephonyIcons.FOUR_G_PLUS;
                    } else if (string10.equals("e")) {
                        mobileIconGroup = TelephonyIcons.E;
                    } else if (string10.equals("g")) {
                        mobileIconGroup = TelephonyIcons.G;
                    } else if (string10.equals("h")) {
                        mobileIconGroup = TelephonyIcons.H;
                    } else if (string10.equals("h+")) {
                        mobileIconGroup = TelephonyIcons.H_PLUS;
                    } else if (string10.equals("lte")) {
                        mobileIconGroup = TelephonyIcons.LTE;
                    } else if (string10.equals("lte+")) {
                        mobileIconGroup = TelephonyIcons.LTE_PLUS;
                    } else {
                        mobileIconGroup = string10.equals("dis") ? TelephonyIcons.DATA_DISABLED : TelephonyIcons.UNKNOWN;
                    }
                    state.iconGroup = mobileIconGroup;
                }
                if (bundle.containsKey("roam")) {
                    valueAt2.getState().roaming = "show".equals(bundle.getString("roam"));
                }
                String string12 = bundle.getString("level");
                if (string12 != null) {
                    valueAt2.getState().level = string12.equals("null") ? -1 : Math.min(Integer.parseInt(string12), 5);
                    valueAt2.getState().connected = valueAt2.getState().level >= 0;
                }
                String string13 = bundle.getString("activity");
                if (string13 != null) {
                    valueAt2.getState().dataConnected = true;
                    int hashCode2 = string13.hashCode();
                    if (hashCode2 != 3365) {
                        if (hashCode2 != 110414) {
                            if (hashCode2 == 100357129 && string13.equals("inout")) {
                                c = 0;
                            }
                        } else if (string13.equals("out")) {
                            c = 2;
                        }
                    } else if (string13.equals("in")) {
                        c = 1;
                    }
                    switch (c) {
                        case 0:
                            valueAt2.setActivity(3);
                            break;
                        case 1:
                            valueAt2.setActivity(1);
                            break;
                        case 2:
                            valueAt2.setActivity(2);
                            break;
                        default:
                            valueAt2.setActivity(0);
                            break;
                    }
                } else {
                    valueAt2.setActivity(0);
                }
                valueAt2.getState().enabled = equals2;
                valueAt2.notifyListeners();
            }
            String string14 = bundle.getString("carriernetworkchange");
            if (string14 != null) {
                boolean equals3 = string14.equals("show");
                while (i < this.mMobileSignalControllers.size()) {
                    this.mMobileSignalControllers.valueAt(i).setCarrierNetworkChangeMode(equals3);
                    i++;
                }
            }
        }
    }

    private SubscriptionInfo addSignalController(int i, int i2) {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(i, "", i2, "", "", 0, 0, "", 0, null, 0, 0, "");
        MobileSignalController mobileSignalController = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone, this.mCallbackHandler, this, subscriptionInfo, this.mSubDefaults, this.mReceiverHandler.getLooper());
        this.mMobileSignalControllers.put(i, mobileSignalController);
        mobileSignalController.getState().userSetup = true;
        return subscriptionInfo;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasEmergencyCryptKeeperText() {
        return EncryptionHelper.IS_DATA_ENCRYPTED;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean isRadioOn() {
        return !this.mAirplaneMode;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SubListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private SubListener() {
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            NetworkControllerImpl.this.updateMobileControllers();
        }
    }

    /* loaded from: classes.dex */
    public static class SubscriptionDefaults {
        public int getDefaultVoiceSubId() {
            return SubscriptionManager.getDefaultVoiceSubscriptionId();
        }

        public int getDefaultDataSubId() {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
    }

    @VisibleForTesting
    /* loaded from: classes.dex */
    public static class Config {
        public boolean hspaDataDistinguishable;
        public boolean showAtLeast3G = false;
        public boolean alwaysShowCdmaRssi = false;
        public boolean show4gForLte = false;
        public boolean hideLtePlus = false;
        public boolean inflateSignalStrengths = false;
        public boolean alwaysShowDataRatIcon = false;

        static Config readConfig(Context context) {
            Config config = new Config();
            Resources resources = context.getResources();
            config.showAtLeast3G = resources.getBoolean(R.bool.config_showMin3G);
            config.alwaysShowCdmaRssi = resources.getBoolean(17956887);
            config.show4gForLte = resources.getBoolean(R.bool.config_show4GForLTE);
            config.hspaDataDistinguishable = resources.getBoolean(R.bool.config_hspa_data_distinguishable);
            config.hideLtePlus = resources.getBoolean(R.bool.config_hideLtePlus);
            config.inflateSignalStrengths = resources.getBoolean(R.bool.config_inflateSignalStrength);
            PersistableBundle config2 = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfig();
            if (config2 != null) {
                config.alwaysShowDataRatIcon = config2.getBoolean("always_show_data_rat_icon_bool");
            }
            return config;
        }
    }

    private void handleLwaAction(Intent intent) {
        int intExtra = intent.getIntExtra("com.mediatek.server.lwa.EXTRA_PHONE_ID", -1);
        int intExtra2 = intent.getIntExtra("com.mediatek.server.lwa.EXTRA_STATE", -1);
        Log.d("NetworkController", "onRecevie ACTION_LWA:" + intExtra + ",lwa: state" + intExtra2);
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i);
            if (valueAt.getControllerSubInfo().getSimSlotIndex() == intExtra) {
                valueAt.handleBroadcast(intent);
                return;
            }
        }
    }

    public void refreshPlmnCarrierLabel() {
        boolean z;
        for (int i = 0; i < this.mSlotCount; i++) {
            int i2 = 0;
            while (true) {
                z = true;
                if (i2 < this.mMobileSignalControllers.size()) {
                    MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i2);
                    int i3 = -1;
                    if (valueAt.getControllerSubInfo() != null) {
                        i3 = valueAt.getControllerSubInfo().getSimSlotIndex();
                    }
                    if (i != i3) {
                        i2++;
                    } else {
                        this.mNetworkName[i3] = ((MobileSignalController.MobileState) valueAt.mCurrentState).networkName;
                        this.mStatusBarPlmnPlugin.updateCarrierLabel(i, true, valueAt.getControllserHasService(), this.mNetworkName);
                        this.mStatusBarSystemUIExt.setSimInserted(i, true);
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
            if (!z) {
                this.mNetworkName[i] = this.mContext.getString(17040140);
                this.mStatusBarPlmnPlugin.updateCarrierLabel(i, false, false, this.mNetworkName);
                this.mStatusBarSystemUIExt.setSimInserted(i, false);
            }
        }
    }

    private void handleIMSServiceUp() {
        Log.d("NetworkController", "handleIMSServiceUp");
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).registerImsListener();
        }
    }
}
