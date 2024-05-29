package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.MathUtils;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.DemoMode;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.WifiSignalController;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkControllerImpl.class */
public class NetworkControllerImpl extends BroadcastReceiver implements NetworkController, DemoMode, DataUsageController.NetworkNameProvider {
    private final AccessPointControllerImpl mAccessPoints;
    private boolean mAirplaneMode;
    private final CallbackHandler mCallbackHandler;
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
    final EthernetSignalController mEthernetSignalController;
    private final boolean mHasMobileDataFeature;
    private boolean mHasNoSims;
    private boolean mInetCondition;
    private boolean mIsEmergency;
    ServiceState mLastServiceState;
    boolean mListening;
    private Locale mLocale;
    final Map<Integer, MobileSignalController> mMobileSignalControllers;
    String[] mNetworkName;
    private final TelephonyManager mPhone;
    private final Handler mReceiverHandler;
    private final Runnable mRegisterListeners;
    int mSlotCount;
    private ISystemUIStatusBarExt mStatusBarSystemUIExt;
    private final SubscriptionDefaults mSubDefaults;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener;
    private final SubscriptionManager mSubscriptionManager;
    private boolean mUserSetup;
    private final BitSet mValidatedTransports;
    private final WifiManager mWifiManager;
    final WifiSignalController mWifiSignalController;
    static final boolean DEBUG = Log.isLoggable("NetworkController", 3);
    static final boolean CHATTY = Log.isLoggable("NetworkControllerChat", 3);

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkControllerImpl$Config.class */
    public static class Config {
        public boolean hspaDataDistinguishable;
        public boolean showAtLeast3G = false;
        public boolean alwaysShowCdmaRssi = false;
        public boolean show4gForLte = false;

        static Config readConfig(Context context) {
            Config config = new Config();
            Resources resources = context.getResources();
            config.showAtLeast3G = resources.getBoolean(2131623948);
            config.alwaysShowCdmaRssi = resources.getBoolean(17956965);
            config.show4gForLte = resources.getBoolean(2131623955);
            config.hspaDataDistinguishable = resources.getBoolean(2131623945);
            return config;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkControllerImpl$SubListener.class */
    public class SubListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        final NetworkControllerImpl this$0;

        private SubListener(NetworkControllerImpl networkControllerImpl) {
            this.this$0 = networkControllerImpl;
        }

        /* synthetic */ SubListener(NetworkControllerImpl networkControllerImpl, SubListener subListener) {
            this(networkControllerImpl);
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            this.this$0.updateMobileControllers();
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NetworkControllerImpl$SubscriptionDefaults.class */
    public static class SubscriptionDefaults {
        public int getDefaultDataSubId() {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }

        public int getDefaultVoiceSubId() {
            return SubscriptionManager.getDefaultVoiceSubscriptionId();
        }
    }

    NetworkControllerImpl(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, SubscriptionManager subscriptionManager, Config config, Looper looper, CallbackHandler callbackHandler, AccessPointControllerImpl accessPointControllerImpl, DataUsageController dataUsageController, SubscriptionDefaults subscriptionDefaults) {
        this.mMobileSignalControllers = new HashMap();
        this.mConnectedTransports = new BitSet();
        this.mValidatedTransports = new BitSet();
        this.mAirplaneMode = false;
        this.mLocale = null;
        this.mCurrentSubscriptions = new ArrayList();
        this.mSlotCount = 0;
        this.mRegisterListeners = new Runnable(this) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.1
            final NetworkControllerImpl this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.registerListeners();
            }
        };
        this.mContext = context;
        this.mConfig = config;
        this.mReceiverHandler = new Handler(looper);
        this.mCallbackHandler = callbackHandler;
        this.mDataSaverController = new DataSaverController(context);
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
        this.mDataUsageController.setCallback(new DataUsageController.Callback(this) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.2
            final NetworkControllerImpl this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.settingslib.net.DataUsageController.Callback
            public void onMobileDataEnabled(boolean z) {
                this.this$0.mCallbackHandler.setMobileDataEnabled(z);
            }
        });
        this.mWifiSignalController = new WifiSignalController(this.mContext, this.mHasMobileDataFeature, this.mCallbackHandler, this);
        this.mEthernetSignalController = new EthernetSignalController(this.mContext, this.mCallbackHandler, this);
        updateAirplaneMode(true);
        this.mStatusBarSystemUIExt = PluginManager.getSystemUIStatusBarExt(this.mContext);
    }

    public NetworkControllerImpl(Context context, Looper looper) {
        this(context, (ConnectivityManager) context.getSystemService("connectivity"), (TelephonyManager) context.getSystemService("phone"), (WifiManager) context.getSystemService("wifi"), SubscriptionManager.from(context), Config.readConfig(context), looper, new CallbackHandler(), new AccessPointControllerImpl(context, looper), new DataUsageController(context), new SubscriptionDefaults());
        this.mReceiverHandler.post(this.mRegisterListeners);
    }

    private void addCustomizedAction(IntentFilter intentFilter) {
        intentFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intentFilter.addAction("com.android.ims.IMS_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
    }

    private SubscriptionInfo addSignalController(int i, int i2) {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(i, "", i2, "", "", 0, 0, "", 0, null, 0, 0, "", 0);
        this.mMobileSignalControllers.put(Integer.valueOf(i), new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone, this.mCallbackHandler, this, subscriptionInfo, this.mSubDefaults, this.mReceiverHandler.getLooper()));
        return subscriptionInfo;
    }

    private static final String emergencyToString(int i) {
        return i > 300 ? "NO_SUB(" + (i - 300) + ")" : i > 200 ? "VOICE_CONTROLLER(" + (i - 200) + ")" : i > 100 ? "FIRST_CONTROLLER(" + (i - 100) + ")" : i == 0 ? "NO_CONTROLLERS" : "UNKNOWN_SOURCE";
    }

    private MobileSignalController getDataController() {
        int defaultDataSubId = this.mSubDefaults.getDefaultDataSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultDataSubId)) {
            if (DEBUG) {
                Log.e("NetworkController", "No data sim selected");
            }
            return this.mDefaultSignalController;
        } else if (this.mMobileSignalControllers.containsKey(Integer.valueOf(defaultDataSubId))) {
            return this.mMobileSignalControllers.get(Integer.valueOf(defaultDataSubId));
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for data sub: " + defaultDataSubId);
            }
            return this.mDefaultSignalController;
        }
    }

    private void notifyAllListeners() {
        notifyListeners();
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.notifyListeners();
        }
        this.mWifiSignalController.notifyListeners();
        this.mEthernetSignalController.notifyListeners();
    }

    private void notifyListeners() {
        this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, 2130838252, 2131493420, this.mContext));
        this.mCallbackHandler.setNoSims(this.mHasNoSims);
    }

    private void pushConnectivityToSignals() {
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
        this.mWifiSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        this.mEthernetSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
    }

    private void refreshLocale() {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (locale.equals(this.mLocale)) {
            return;
        }
        this.mLocale = locale;
        notifyAllListeners();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerListeners() {
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.registerListener();
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener(this, null);
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
        addCustomizedAction(intentFilter);
        this.mContext.registerReceiver(this, intentFilter, null, this.mReceiverHandler);
        this.mListening = true;
        updateMobileControllers();
    }

    private void unregisterListeners() {
        this.mListening = false;
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.unregisterListener();
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mContext.unregisterReceiver(this);
    }

    private void updateAirplaneMode(boolean z) {
        boolean z2 = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z2 = false;
        }
        updateAirplaneMode(z2, z);
    }

    private void updateAirplaneMode(boolean z, boolean z2) {
        if (z != this.mAirplaneMode || z2) {
            this.mAirplaneMode = z;
            for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
                mobileSignalController.setAirplaneMode(this.mAirplaneMode);
            }
            notifyListeners();
        }
    }

    private void updateConnectivity() {
        NetworkCapabilities[] defaultNetworkCapabilitiesForUser;
        int[] transportTypes;
        this.mConnectedTransports.clear();
        this.mValidatedTransports.clear();
        for (NetworkCapabilities networkCapabilities : this.mConnectivityManager.getDefaultNetworkCapabilitiesForUser(this.mCurrentUserId)) {
            for (int i : networkCapabilities.getTransportTypes()) {
                this.mConnectedTransports.set(i);
                if (networkCapabilities.hasCapability(16)) {
                    this.mValidatedTransports.set(i);
                }
            }
        }
        if (CHATTY) {
            Log.d("NetworkController", "updateConnectivity: mConnectedTransports=" + this.mConnectedTransports);
            Log.d("NetworkController", "updateConnectivity: mValidatedTransports=" + this.mValidatedTransports);
        }
        this.mInetCondition = this.mValidatedTransports.isEmpty() ? false : true;
        pushConnectivityToSignals();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMobileControllers() {
        SIMHelper.updateSIMInfos(this.mContext);
        if (this.mListening) {
            doUpdateMobileControllers();
        } else if (DEBUG) {
            Log.d("NetworkController", "updateMobileControllers: it's not listening");
        }
    }

    private void updateMobileControllersEx(Intent intent) {
        int i = 4;
        if (intent != null) {
            i = intent.getIntExtra("simDetectStatus", 0);
            Log.d("NetworkController", "updateMobileControllers detectedType: " + i);
        }
        if (i != 3) {
            updateNoSims();
        } else {
            updateMobileControllers();
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void addEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, true);
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly());
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void addSignalCallback(NetworkController.SignalCallback signalCallback) {
        signalCallback.setSubs(this.mCurrentSubscriptions);
        signalCallback.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, 2130838252, 2131493420, this.mContext));
        this.mWifiSignalController.notifyListeners(signalCallback);
        this.mEthernetSignalController.notifyListeners(signalCallback);
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.notifyListeners(signalCallback);
        }
        this.mCallbackHandler.setListening(signalCallback, true);
        signalCallback.setNoSims(this.mHasNoSims);
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            if (DEBUG) {
                Log.d("NetworkController", "Entering demo mode");
            }
            unregisterListeners();
            this.mDemoMode = true;
            this.mDemoInetCondition = this.mInetCondition;
            this.mDemoWifiState = this.mWifiSignalController.getState();
        } else if (this.mDemoMode && str.equals("exit")) {
            if (DEBUG) {
                Log.d("NetworkController", "Exiting demo mode");
            }
            this.mDemoMode = false;
            updateMobileControllers();
            for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
                mobileSignalController.resetLastState();
            }
            this.mWifiSignalController.resetLastState();
            this.mReceiverHandler.post(this.mRegisterListeners);
            notifyAllListeners();
        } else if (this.mDemoMode && str.equals("network")) {
            String string = bundle.getString("airplane");
            if (string != null) {
                this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(string.equals("show"), 2130838252, 2131493420, this.mContext));
            }
            String string2 = bundle.getString("fully");
            if (string2 != null) {
                this.mDemoInetCondition = Boolean.parseBoolean(string2);
                BitSet bitSet = new BitSet();
                if (this.mDemoInetCondition) {
                    bitSet.set(this.mWifiSignalController.mTransportType);
                }
                this.mWifiSignalController.updateConnectivity(bitSet, bitSet);
                for (MobileSignalController mobileSignalController2 : this.mMobileSignalControllers.values()) {
                    if (this.mDemoInetCondition) {
                        bitSet.set(mobileSignalController2.mTransportType);
                    }
                    mobileSignalController2.updateConnectivity(bitSet, bitSet);
                }
            }
            String string3 = bundle.getString("wifi");
            if (string3 != null) {
                boolean equals = string3.equals("show");
                String string4 = bundle.getString("level");
                if (string4 != null) {
                    this.mDemoWifiState.level = string4.equals("null") ? -1 : Math.min(Integer.parseInt(string4), WifiIcons.WIFI_LEVEL_COUNT - 1);
                    this.mDemoWifiState.connected = this.mDemoWifiState.level >= 0;
                }
                this.mDemoWifiState.enabled = equals;
                this.mWifiSignalController.notifyListeners();
            }
            String string5 = bundle.getString("sims");
            if (string5 != null) {
                int constrain = MathUtils.constrain(Integer.parseInt(string5), 1, 8);
                ArrayList arrayList = new ArrayList();
                if (constrain != this.mMobileSignalControllers.size()) {
                    this.mMobileSignalControllers.clear();
                    int activeSubscriptionInfoCountMax = this.mSubscriptionManager.getActiveSubscriptionInfoCountMax();
                    for (int i = activeSubscriptionInfoCountMax; i < activeSubscriptionInfoCountMax + constrain; i++) {
                        arrayList.add(addSignalController(i, i));
                    }
                    this.mCallbackHandler.setSubs(arrayList);
                }
            }
            String string6 = bundle.getString("nosim");
            if (string6 != null) {
                this.mHasNoSims = string6.equals("show");
                this.mCallbackHandler.setNoSims(this.mHasNoSims);
            }
            String string7 = bundle.getString("mobile");
            if (string7 != null) {
                boolean equals2 = string7.equals("show");
                String string8 = bundle.getString("datatype");
                String string9 = bundle.getString("slot");
                int constrain2 = MathUtils.constrain(TextUtils.isEmpty(string9) ? 0 : Integer.parseInt(string9), 0, 8);
                ArrayList arrayList2 = new ArrayList();
                while (this.mMobileSignalControllers.size() <= constrain2) {
                    int size = this.mMobileSignalControllers.size();
                    arrayList2.add(addSignalController(size, size));
                }
                if (!arrayList2.isEmpty()) {
                    this.mCallbackHandler.setSubs(arrayList2);
                }
                MobileSignalController mobileSignalController3 = ((MobileSignalController[]) this.mMobileSignalControllers.values().toArray(new MobileSignalController[0]))[constrain2];
                mobileSignalController3.getState().dataSim = string8 != null;
                if (string8 != null) {
                    mobileSignalController3.getState().iconGroup = string8.equals("1x") ? TelephonyIcons.ONE_X : string8.equals("3g") ? TelephonyIcons.THREE_G : string8.equals("4g") ? TelephonyIcons.FOUR_G : string8.equals("e") ? TelephonyIcons.E : string8.equals("g") ? TelephonyIcons.G : string8.equals("h") ? TelephonyIcons.H : string8.equals("lte") ? TelephonyIcons.LTE : string8.equals("roam") ? TelephonyIcons.ROAMING : TelephonyIcons.UNKNOWN;
                }
                int[][] iArr = TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH;
                String string10 = bundle.getString("level");
                if (string10 != null) {
                    mobileSignalController3.getState().level = string10.equals("null") ? -1 : Math.min(Integer.parseInt(string10), iArr[0].length - 1);
                    mobileSignalController3.getState().connected = mobileSignalController3.getState().level >= 0;
                }
                mobileSignalController3.getState().enabled = equals2;
                mobileSignalController3.notifyListeners();
            }
            String string11 = bundle.getString("carriernetworkchange");
            if (string11 != null) {
                boolean equals3 = string11.equals("show");
                for (MobileSignalController mobileSignalController4 : this.mMobileSignalControllers.values()) {
                    mobileSignalController4.setCarrierNetworkChangeMode(equals3);
                }
            }
        }
    }

    void doUpdateMobileControllers() {
        List<SubscriptionInfo> activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        List<SubscriptionInfo> list = activeSubscriptionInfoList;
        if (activeSubscriptionInfoList == null) {
            Log.d("NetworkController", "subscriptions is null");
            list = Collections.emptyList();
        }
        if (hasCorrectMobileControllers(list)) {
            updateNoSims();
            return;
        }
        setCurrentSubscriptions(list);
        updateNoSims();
        recalculateEmergency();
    }

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
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.dump(printWriter);
        }
        this.mWifiSignalController.dump(printWriter);
        this.mEthernetSignalController.dump(printWriter);
        this.mAccessPoints.dump(printWriter);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public NetworkController.AccessPointController getAccessPointController() {
        return this.mAccessPoints;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public DataSaverController getDataSaverController() {
        return this.mDataSaverController;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public DataUsageController getMobileDataController() {
        return this.mDataUsageController;
    }

    @Override // com.android.settingslib.net.DataUsageController.NetworkNameProvider
    public String getMobileDataNetworkName() {
        MobileSignalController dataController = getDataController();
        return dataController != null ? dataController.getState().networkNameData : "";
    }

    void handleConfigurationChanged() {
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.setConfiguration(this.mConfig);
        }
        refreshLocale();
    }

    void handleIMSAction(Intent intent) {
        this.mStatusBarSystemUIExt.setImsSlotId(intent.getIntExtra("android:phone_id", -1));
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            if (mobileSignalController.getControllerSubInfo().getSimSlotIndex() == intent.getIntExtra("android:phone_id", -1)) {
                mobileSignalController.handleBroadcast(intent);
                return;
            }
        }
    }

    void handleSetUserSetupComplete(boolean z) {
        this.mUserSetup = z;
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.setUserSetupComplete(this.mUserSetup);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:9:0x002d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    boolean hasCorrectMobileControllers(List<SubscriptionInfo> list) {
        if (list.size() != this.mMobileSignalControllers.size()) {
            Log.d("NetworkController", "size not equals, reset subInfo");
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : list) {
            MobileSignalController mobileSignalController = this.mMobileSignalControllers.get(Integer.valueOf(subscriptionInfo.getSubscriptionId()));
            if (mobileSignalController == null || mobileSignalController.mSubscriptionInfo.getSimSlotIndex() != subscriptionInfo.getSimSlotIndex()) {
                Log.d("NetworkController", "info_subId = " + subscriptionInfo.getSubscriptionId() + " info_slotId = " + subscriptionInfo.getSimSlotIndex());
                return false;
            }
            while (r0.hasNext()) {
            }
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasMobileDataFeature() {
        return this.mHasMobileDataFeature;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasVoiceCallingFeature() {
        boolean z = false;
        if (this.mPhone.getPhoneType() != 0) {
            z = true;
        }
        return z;
    }

    public boolean isEmergencyOnly() {
        if (this.mMobileSignalControllers.size() == 0) {
            Log.d("NetworkController", "isEmergencyOnly No sims ");
            this.mEmergencySource = 0;
            for (int i = 0; i < this.mEmergencyPhone.length; i++) {
                if (this.mEmergencyPhone[i]) {
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency in phone " + i);
                        return true;
                    }
                    return true;
                }
            }
            return false;
        }
        int defaultVoiceSubId = this.mSubDefaults.getDefaultVoiceSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultVoiceSubId)) {
            for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
                if (!mobileSignalController.getState().isEmergency) {
                    this.mEmergencySource = mobileSignalController.mSubscriptionInfo.getSubscriptionId() + 100;
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency " + mobileSignalController.mTag);
                        return false;
                    }
                    return false;
                }
            }
        }
        if (this.mMobileSignalControllers.containsKey(Integer.valueOf(defaultVoiceSubId))) {
            this.mEmergencySource = defaultVoiceSubId + 200;
            if (DEBUG) {
                Log.d("NetworkController", "Getting emergency from " + defaultVoiceSubId);
            }
            return this.mMobileSignalControllers.get(Integer.valueOf(defaultVoiceSubId)).getState().isEmergency;
        }
        if (DEBUG) {
            Log.e("NetworkController", "Cannot find controller for voice sub: " + defaultVoiceSubId);
        }
        this.mEmergencySource = defaultVoiceSubId + 300;
        return true;
    }

    public void onConfigurationChanged() {
        this.mConfig = Config.readConfig(this.mContext);
        this.mReceiverHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.4
            final NetworkControllerImpl this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.handleConfigurationChanged();
            }
        });
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (CHATTY) {
            Log.d("NetworkController", "onReceive: intent=" + intent);
        }
        String action = intent.getAction();
        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals("android.net.conn.INET_CONDITION_ACTION")) {
            updateConnectivity();
        } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
            refreshLocale();
            updateAirplaneMode(intent.getBooleanExtra("state", false), false);
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED")) {
            recalculateEmergency();
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
                mobileSignalController.handleBroadcast(intent);
            }
        } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
            updateMobileControllers();
        } else if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
            updateMobileControllersEx(intent);
            refreshPlmnCarrierLabel();
        } else if (action.equals("android.intent.action.SERVICE_STATE")) {
            this.mLastServiceState = ServiceState.newFromBundle(intent.getExtras());
            if (this.mLastServiceState != null) {
                int intExtra = intent.getIntExtra("phone", 0);
                this.mEmergencyPhone[intExtra] = this.mLastServiceState.isEmergencyOnly();
                if (DEBUG) {
                    Log.d("NetworkController", "Service State changed...phoneId: " + intExtra + " ,isEmergencyOnly: " + this.mEmergencyPhone[intExtra]);
                }
                if (this.mMobileSignalControllers.size() == 0) {
                    recalculateEmergency();
                }
            }
        } else if (action.equals("com.android.ims.IMS_STATE_CHANGED")) {
            Log.d("NetworkController", "onRecevie ACTION_IMS_STATE_CHANGED");
            handleIMSAction(intent);
        } else if (action.equals("android.intent.action.ACTION_PREBOOT_IPO")) {
            updateAirplaneMode(false);
        } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
            Log.d("NetworkController", "IPO SHUTDOWN!!!");
            setCurrentSubscriptions(Collections.emptyList());
            updateNoSims();
            recalculateEmergency();
        } else {
            int intExtra2 = intent.getIntExtra("subscription", -1);
            if (!SubscriptionManager.isValidSubscriptionId(intExtra2)) {
                this.mWifiSignalController.handleBroadcast(intent);
            } else if (this.mMobileSignalControllers.containsKey(Integer.valueOf(intExtra2))) {
                this.mMobileSignalControllers.get(Integer.valueOf(intExtra2)).handleBroadcast(intent);
            } else {
                updateMobileControllers();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void recalculateEmergency() {
        this.mIsEmergency = isEmergencyOnly();
        this.mCallbackHandler.setEmergencyCallsOnly(this.mIsEmergency);
    }

    public void refreshPlmnCarrierLabel() {
        boolean z;
        for (int i = 0; i < this.mSlotCount; i++) {
            Iterator<T> it = this.mMobileSignalControllers.entrySet().iterator();
            while (true) {
                z = false;
                if (!it.hasNext()) {
                    break;
                }
                Map.Entry entry = (Map.Entry) it.next();
                ((Integer) entry.getKey()).intValue();
                MobileSignalController mobileSignalController = (MobileSignalController) entry.getValue();
                int simSlotIndex = mobileSignalController.getControllerSubInfo() != null ? mobileSignalController.getControllerSubInfo().getSimSlotIndex() : -1;
                if (i == simSlotIndex) {
                    this.mNetworkName[simSlotIndex] = ((MobileSignalController.MobileState) mobileSignalController.mCurrentState).networkName;
                    PluginManager.getStatusBarPlmnPlugin(this.mContext).updateCarrierLabel(i, true, mobileSignalController.getControllserHasService(), this.mNetworkName);
                    this.mStatusBarSystemUIExt.setSimInserted(i, true);
                    z = true;
                    break;
                }
            }
            if (!z) {
                this.mNetworkName[i] = this.mContext.getString(17040009);
                PluginManager.getStatusBarPlmnPlugin(this.mContext).updateCarrierLabel(i, false, false, this.mNetworkName);
                this.mStatusBarSystemUIExt.setSimInserted(i, false);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void removeEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, false);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void removeSignalCallback(NetworkController.SignalCallback signalCallback) {
        this.mCallbackHandler.setListening(signalCallback, false);
    }

    void setCurrentSubscriptions(List<SubscriptionInfo> list) {
        Collections.sort(list, new Comparator<SubscriptionInfo>(this) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.5
            final NetworkControllerImpl this$0;

            {
                this.this$0 = this;
            }

            @Override // java.util.Comparator
            public int compare(SubscriptionInfo subscriptionInfo, SubscriptionInfo subscriptionInfo2) {
                return subscriptionInfo.getSimSlotIndex() == subscriptionInfo2.getSimSlotIndex() ? subscriptionInfo.getSubscriptionId() - subscriptionInfo2.getSubscriptionId() : subscriptionInfo.getSimSlotIndex() - subscriptionInfo2.getSimSlotIndex();
            }
        });
        this.mCurrentSubscriptions = list;
        HashMap hashMap = new HashMap(this.mMobileSignalControllers);
        this.mMobileSignalControllers.clear();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            int subscriptionId = list.get(i).getSubscriptionId();
            if (hashMap.containsKey(Integer.valueOf(subscriptionId))) {
                MobileSignalController mobileSignalController = (MobileSignalController) hashMap.remove(Integer.valueOf(subscriptionId));
                mobileSignalController.mSubscriptionInfo = list.get(i);
                this.mMobileSignalControllers.put(Integer.valueOf(subscriptionId), mobileSignalController);
            } else {
                MobileSignalController mobileSignalController2 = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone, this.mCallbackHandler, this, list.get(i), this.mSubDefaults, this.mReceiverHandler.getLooper());
                mobileSignalController2.setUserSetupComplete(this.mUserSetup);
                this.mMobileSignalControllers.put(Integer.valueOf(subscriptionId), mobileSignalController2);
                if (list.get(i).getSimSlotIndex() == 0) {
                    this.mDefaultSignalController = mobileSignalController2;
                }
                if (this.mListening) {
                    mobileSignalController2.registerListener();
                }
            }
        }
        if (this.mListening) {
            for (Integer num : hashMap.keySet()) {
                if (hashMap.get(num) == this.mDefaultSignalController) {
                    this.mDefaultSignalController = null;
                }
                ((MobileSignalController) hashMap.get(num)).unregisterListener();
            }
        }
        this.mCallbackHandler.setSubs(list);
        notifyAllListeners();
        pushConnectivityToSignals();
        updateAirplaneMode(true);
    }

    public void setUserSetupComplete(boolean z) {
        this.mReceiverHandler.post(new Runnable(this, z) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.6
            final NetworkControllerImpl this$0;
            final boolean val$userSetup;

            {
                this.this$0 = this;
                this.val$userSetup = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.handleSetUserSetupComplete(this.val$userSetup);
            }
        });
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.systemui.statusbar.policy.NetworkControllerImpl$3] */
    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void setWifiEnabled(boolean z) {
        new AsyncTask<Void, Void, Void>(this, z) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.3
            final NetworkControllerImpl this$0;
            final boolean val$enabled;

            {
                this.this$0 = this;
                this.val$enabled = z;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                int wifiApState = this.this$0.mWifiManager.getWifiApState();
                if (this.val$enabled && (wifiApState == 12 || wifiApState == 13)) {
                    this.this$0.mWifiManager.setWifiApEnabled(null, false);
                }
                this.this$0.mWifiManager.setWifiEnabled(this.val$enabled);
                return null;
            }
        }.execute(new Void[0]);
    }

    protected void updateNoSims() {
        boolean z = this.mHasMobileDataFeature && this.mMobileSignalControllers.size() == 0;
        if (z != this.mHasNoSims) {
            this.mHasNoSims = z;
            this.mCallbackHandler.setNoSims(this.mHasNoSims);
        }
    }
}
