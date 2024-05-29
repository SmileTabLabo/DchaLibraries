package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SignalController;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IMobileIconExt;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
import com.mediatek.systemui.statusbar.networktype.NetworkTypeUtils;
import com.mediatek.telephony.TelephonyManagerEx;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/MobileSignalController.class */
public class MobileSignalController extends SignalController<MobileState, MobileIconGroup> {
    private NetworkControllerImpl.Config mConfig;
    private int mDataNetType;
    private int mDataState;
    private MobileIconGroup mDefaultIcons;
    private final NetworkControllerImpl.SubscriptionDefaults mDefaults;
    private IMobileIconExt mMobileIconExt;
    private final String mNetworkNameDefault;
    private final String mNetworkNameSeparator;
    final SparseArray<MobileIconGroup> mNetworkToIconLookup;
    private final TelephonyManager mPhone;
    final PhoneStateListener mPhoneStateListener;
    private ServiceState mServiceState;
    private SignalStrength mSignalStrength;
    private ISystemUIStatusBarExt mStatusBarExt;
    SubscriptionInfo mSubscriptionInfo;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/MobileSignalController$MobileIconGroup.class */
    public static class MobileIconGroup extends SignalController.IconGroup {
        final int mDataContentDescription;
        final int mDataType;
        final boolean mIsWide;
        final int mQsDataType;

        public MobileIconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8) {
            super(str, iArr, iArr2, iArr3, i, i2, i3, i4, i5);
            this.mDataContentDescription = i6;
            this.mDataType = i7;
            this.mIsWide = z;
            this.mQsDataType = i8;
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/MobileSignalController$MobilePhoneStateListener.class */
    class MobilePhoneStateListener extends PhoneStateListener {
        final MobileSignalController this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public MobilePhoneStateListener(MobileSignalController mobileSignalController, int i, Looper looper) {
            super(i, looper);
            this.this$0 = mobileSignalController;
        }

        public void onCarrierNetworkChange(boolean z) {
            Log.d(this.this$0.mTag, "onCarrierNetworkChange: active=" + z);
            ((MobileState) this.this$0.mCurrentState).carrierNetworkChangeMode = z;
            this.this$0.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataActivity(int i) {
            Log.d(this.this$0.mTag, "onDataActivity: direction=" + i);
            this.this$0.setActivity(i);
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataConnectionStateChanged(int i, int i2) {
            Log.d(this.this$0.mTag, "onDataConnectionStateChanged: state=" + i + " type=" + i2);
            this.this$0.mDataState = i;
            this.this$0.mDataNetType = i2;
            this.this$0.mDataNetType = NetworkTypeUtils.getDataNetTypeFromServiceState(this.this$0.mDataNetType, this.this$0.mServiceState);
            this.this$0.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.d(this.this$0.mTag, "onServiceStateChanged voiceState=" + serviceState.getVoiceRegState() + " dataState=" + serviceState.getDataRegState());
            this.this$0.mServiceState = serviceState;
            this.this$0.mDataNetType = serviceState.getDataNetworkType();
            this.this$0.mDataNetType = NetworkTypeUtils.getDataNetTypeFromServiceState(this.this$0.mDataNetType, this.this$0.mServiceState);
            this.this$0.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Log.d(this.this$0.mTag, "onSignalStrengthsChanged signalStrength=" + signalStrength + (signalStrength == null ? "" : " level=" + signalStrength.getLevel()));
            this.this$0.mSignalStrength = signalStrength;
            this.this$0.updateTelephony();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/MobileSignalController$MobileState.class */
    public static class MobileState extends SignalController.State {
        boolean airplaneMode;
        boolean carrierNetworkChangeMode;
        int customizedSignalStrengthIcon;
        int customizedState;
        boolean dataConnected;
        int dataNetType;
        boolean dataSim;
        int imsCap;
        int imsRegState = 3;
        boolean isDefault;
        boolean isEmergency;
        int networkIcon;
        String networkName;
        String networkNameData;
        boolean userSetup;
        int volteIcon;

        MobileState() {
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            MobileState mobileState = (MobileState) state;
            this.dataSim = mobileState.dataSim;
            this.networkName = mobileState.networkName;
            this.networkNameData = mobileState.networkNameData;
            this.dataConnected = mobileState.dataConnected;
            this.isDefault = mobileState.isDefault;
            this.isEmergency = mobileState.isEmergency;
            this.airplaneMode = mobileState.airplaneMode;
            this.carrierNetworkChangeMode = mobileState.carrierNetworkChangeMode;
            this.userSetup = mobileState.userSetup;
            this.networkIcon = mobileState.networkIcon;
            this.dataNetType = mobileState.dataNetType;
            this.customizedState = mobileState.customizedState;
            this.customizedSignalStrengthIcon = mobileState.customizedSignalStrengthIcon;
            this.imsRegState = mobileState.imsRegState;
            this.imsCap = mobileState.imsCap;
            this.volteIcon = mobileState.volteIcon;
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public boolean equals(Object obj) {
            return (super.equals(obj) && Objects.equals(((MobileState) obj).networkName, this.networkName) && Objects.equals(((MobileState) obj).networkNameData, this.networkNameData) && ((MobileState) obj).dataSim == this.dataSim && ((MobileState) obj).dataConnected == this.dataConnected && ((MobileState) obj).isEmergency == this.isEmergency && ((MobileState) obj).airplaneMode == this.airplaneMode && ((MobileState) obj).carrierNetworkChangeMode == this.carrierNetworkChangeMode && ((MobileState) obj).networkIcon == this.networkIcon && ((MobileState) obj).volteIcon == this.volteIcon && ((MobileState) obj).dataNetType == this.dataNetType && ((MobileState) obj).customizedState == this.customizedState && ((MobileState) obj).customizedSignalStrengthIcon == this.customizedSignalStrengthIcon && ((MobileState) obj).userSetup == this.userSetup) ? ((MobileState) obj).isDefault == this.isDefault : false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(',');
            sb.append("dataSim=").append(this.dataSim).append(',');
            sb.append("networkName=").append(this.networkName).append(',');
            sb.append("networkNameData=").append(this.networkNameData).append(',');
            sb.append("dataConnected=").append(this.dataConnected).append(',');
            sb.append("isDefault=").append(this.isDefault).append(',');
            sb.append("isEmergency=").append(this.isEmergency).append(',');
            sb.append("airplaneMode=").append(this.airplaneMode).append(',');
            sb.append("carrierNetworkChangeMode=").append(this.carrierNetworkChangeMode).append(',');
            sb.append("userSetup=").append(this.userSetup);
            sb.append("networkIcon").append(this.networkIcon).append(',');
            sb.append("dataNetType").append(this.dataNetType).append(',');
            sb.append("customizedState").append(this.customizedState).append(',');
            sb.append("customizedSignalStrengthIcon").append(this.customizedSignalStrengthIcon).append(',');
            sb.append("imsRegState=").append(this.imsRegState).append(',');
            sb.append("imsCap=").append(this.imsCap).append(',');
            sb.append("volteIconId=").append(this.volteIcon).append(',');
            sb.append("carrierNetworkChangeMode=").append(this.carrierNetworkChangeMode);
        }
    }

    public MobileSignalController(Context context, NetworkControllerImpl.Config config, boolean z, TelephonyManager telephonyManager, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, SubscriptionInfo subscriptionInfo, NetworkControllerImpl.SubscriptionDefaults subscriptionDefaults, Looper looper) {
        super("MobileSignalController(" + subscriptionInfo.getSubscriptionId() + ")", context, 0, callbackHandler, networkControllerImpl);
        this.mDataNetType = 0;
        this.mDataState = 0;
        this.mNetworkToIconLookup = new SparseArray<>();
        this.mConfig = config;
        this.mPhone = telephonyManager;
        this.mDefaults = subscriptionDefaults;
        this.mSubscriptionInfo = subscriptionInfo;
        this.mMobileIconExt = PluginManager.getMobileIconExt(context);
        this.mStatusBarExt = PluginManager.getSystemUIStatusBarExt(context);
        this.mPhoneStateListener = new MobilePhoneStateListener(this, subscriptionInfo.getSubscriptionId(), looper);
        this.mNetworkNameSeparator = getStringIfExists(2131493314);
        this.mNetworkNameDefault = getStringIfExists(17040009);
        mapIconSets();
        String charSequence = subscriptionInfo.getCarrierName() != null ? subscriptionInfo.getCarrierName().toString() : this.mNetworkNameDefault;
        MobileState mobileState = (MobileState) this.mLastState;
        ((MobileState) this.mCurrentState).networkName = charSequence;
        mobileState.networkName = charSequence;
        MobileState mobileState2 = (MobileState) this.mLastState;
        ((MobileState) this.mCurrentState).networkNameData = charSequence;
        mobileState2.networkNameData = charSequence;
        MobileState mobileState3 = (MobileState) this.mLastState;
        ((MobileState) this.mCurrentState).enabled = z;
        mobileState3.enabled = z;
        MobileState mobileState4 = (MobileState) this.mLastState;
        MobileIconGroup mobileIconGroup = this.mDefaultIcons;
        ((MobileState) this.mCurrentState).iconGroup = mobileIconGroup;
        mobileState4.iconGroup = mobileIconGroup;
        initImsRegisterState();
        updateDataSim();
    }

    private int getImsEnableCap(Intent intent) {
        boolean[] booleanArrayExtra = intent.getBooleanArrayExtra("android:enablecap");
        int i = -1;
        if (booleanArrayExtra != null) {
            if (booleanArrayExtra[2]) {
                i = 2;
            } else {
                i = -1;
                if (booleanArrayExtra[0]) {
                    i = 0;
                }
            }
        }
        return i;
    }

    private int getVolteIcon() {
        int i;
        boolean z = false;
        if (isImsOverWfc()) {
            if (!SystemProperties.get("persist.radio.multisim.config", "ss").equals("ss")) {
                z = true;
            }
            i = 0;
            if (z) {
                i = 2130838309;
            }
        } else {
            i = 0;
            if (isImsOverVoice()) {
                i = 0;
                if (isLteNetWork()) {
                    i = 0;
                    if (((MobileState) this.mCurrentState).imsRegState == 0) {
                        i = 2130838307;
                    }
                }
            }
        }
        return i;
    }

    private void handleIWLANNetwork() {
        if (((MobileState) this.mCurrentState).connected && this.mServiceState != null && this.mServiceState.getDataNetworkType() == 18 && this.mServiceState.getVoiceNetworkType() == 0) {
            Log.d(this.mTag, "Current is IWLAN network only, no cellular network available");
            ((MobileState) this.mCurrentState).connected = false;
        }
        ((MobileState) this.mCurrentState).connected = this.mStatusBarExt.updateSignalStrengthWifiOnlyMode(this.mServiceState, ((MobileState) this.mCurrentState).connected);
    }

    private void handleImsAction(Intent intent) {
        ((MobileState) this.mCurrentState).imsRegState = intent.getIntExtra("android:regState", 1);
        ((MobileState) this.mCurrentState).imsCap = getImsEnableCap(intent);
        ((MobileState) this.mCurrentState).volteIcon = getVolteIcon();
        Log.d(this.mTag, "handleImsAction imsRegstate=" + ((MobileState) this.mCurrentState).imsRegState + ",imsCap = " + ((MobileState) this.mCurrentState).imsCap + ",volteIconId=" + ((MobileState) this.mCurrentState).volteIcon);
    }

    private boolean hasService() {
        boolean z = true;
        if (this.mServiceState != null) {
            switch (this.mServiceState.getVoiceRegState()) {
                case 1:
                case 2:
                    if (this.mServiceState.getDataRegState() != 0) {
                        z = false;
                    }
                    return z;
                case 3:
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }

    private void initImsRegisterState() {
        int phoneId = SubscriptionManager.getPhoneId(this.mSubscriptionInfo.getSubscriptionId());
        try {
            boolean imsRegInfo = ImsManager.getInstance(this.mContext, phoneId).getImsRegInfo();
            ((MobileState) this.mCurrentState).imsRegState = imsRegInfo ? 0 : 1;
            Log.d(this.mTag, "init imsRegState:" + ((MobileState) this.mCurrentState).imsRegState + ",phoneId:" + phoneId);
        } catch (ImsException e) {
            Log.e(this.mTag, "Fail to get Ims Status");
        }
    }

    private boolean isCarrierNetworkChangeActive() {
        return ((MobileState) this.mCurrentState).carrierNetworkChangeMode;
    }

    private boolean isCdma() {
        boolean z = false;
        if (this.mSignalStrength != null) {
            z = !this.mSignalStrength.isGsm();
        }
        return z;
    }

    private boolean isDataDisabled() {
        return !this.mPhone.getDataEnabled(this.mSubscriptionInfo.getSubscriptionId());
    }

    private boolean isImsOverVoice() {
        return ((MobileState) this.mCurrentState).imsCap == 0;
    }

    private boolean isRoaming() {
        boolean z;
        if (!isCdma()) {
            return this.mStatusBarExt.needShowRoamingIcons(this.mServiceState != null ? this.mServiceState.getRoaming() : false);
        } else if (this.mServiceState == null) {
            return false;
        } else {
            int cdmaEriIconMode = this.mServiceState.getCdmaEriIconMode();
            if (this.mServiceState == null || this.mServiceState.getCdmaEriIconIndex() == 1) {
                z = false;
            } else {
                z = true;
                if (cdmaEriIconMode != 0) {
                    z = cdmaEriIconMode == 1;
                }
            }
            return z;
        }
    }

    private void mapIconSets() {
        this.mNetworkToIconLookup.clear();
        this.mNetworkToIconLookup.put(5, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(6, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(12, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(14, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(3, TelephonyIcons.THREE_G);
        if (this.mConfig.showAtLeast3G) {
            this.mNetworkToIconLookup.put(0, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(2, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(4, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(7, TelephonyIcons.THREE_G);
            this.mDefaultIcons = TelephonyIcons.THREE_G;
        } else {
            this.mNetworkToIconLookup.put(0, TelephonyIcons.UNKNOWN);
            this.mNetworkToIconLookup.put(2, TelephonyIcons.E);
            this.mNetworkToIconLookup.put(4, TelephonyIcons.ONE_X);
            this.mNetworkToIconLookup.put(7, TelephonyIcons.ONE_X);
            this.mDefaultIcons = TelephonyIcons.G;
        }
        MobileIconGroup mobileIconGroup = TelephonyIcons.THREE_G;
        if (this.mConfig.hspaDataDistinguishable) {
            mobileIconGroup = TelephonyIcons.H;
        }
        this.mNetworkToIconLookup.put(8, mobileIconGroup);
        this.mNetworkToIconLookup.put(9, mobileIconGroup);
        this.mNetworkToIconLookup.put(10, mobileIconGroup);
        this.mNetworkToIconLookup.put(15, mobileIconGroup);
        if (this.mConfig.show4gForLte) {
            this.mNetworkToIconLookup.put(13, TelephonyIcons.FOUR_G);
        } else {
            this.mNetworkToIconLookup.put(13, TelephonyIcons.LTE);
        }
        this.mNetworkToIconLookup.put(18, TelephonyIcons.WFC);
        this.mNetworkToIconLookup.put(139, TelephonyIcons.FOUR_GA);
    }

    private void updateDataSim() {
        boolean z = true;
        int defaultDataSubId = this.mDefaults.getDefaultDataSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultDataSubId)) {
            ((MobileState) this.mCurrentState).dataSim = true;
            return;
        }
        MobileState mobileState = (MobileState) this.mCurrentState;
        if (defaultDataSubId != this.mSubscriptionInfo.getSubscriptionId()) {
            z = false;
        }
        mobileState.dataSim = z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateTelephony() {
        Log.d(this.mTag, "updateTelephonySignalStrength: hasService=" + hasService() + " ss=" + this.mSignalStrength);
        ((MobileState) this.mCurrentState).connected = hasService() && this.mSignalStrength != null;
        handleIWLANNetwork();
        if (((MobileState) this.mCurrentState).connected) {
            if (this.mSignalStrength.isGsm() || !this.mConfig.alwaysShowCdmaRssi) {
                ((MobileState) this.mCurrentState).level = this.mSignalStrength.getLevel();
            } else {
                ((MobileState) this.mCurrentState).level = this.mSignalStrength.getCdmaLevel();
            }
            ((MobileState) this.mCurrentState).level = this.mStatusBarExt.getCustomizeSignalStrengthLevel(((MobileState) this.mCurrentState).level, this.mSignalStrength, this.mServiceState);
        }
        if (this.mNetworkToIconLookup.indexOfKey(this.mDataNetType) >= 0) {
            ((MobileState) this.mCurrentState).iconGroup = this.mNetworkToIconLookup.get(this.mDataNetType);
        } else {
            ((MobileState) this.mCurrentState).iconGroup = this.mDefaultIcons;
        }
        ((MobileState) this.mCurrentState).dataNetType = this.mDataNetType;
        MobileState mobileState = (MobileState) this.mCurrentState;
        boolean z = false;
        if (((MobileState) this.mCurrentState).connected) {
            z = false;
            if (this.mDataState == 2) {
                z = true;
            }
        }
        mobileState.dataConnected = z;
        ((MobileState) this.mCurrentState).customizedState = this.mStatusBarExt.getCustomizeCsState(this.mServiceState, ((MobileState) this.mCurrentState).customizedState);
        ((MobileState) this.mCurrentState).customizedSignalStrengthIcon = this.mStatusBarExt.getCustomizeSignalStrengthIcon(this.mSubscriptionInfo.getSubscriptionId(), ((MobileState) this.mCurrentState).customizedSignalStrengthIcon, this.mSignalStrength, this.mDataNetType, this.mServiceState);
        if (isCarrierNetworkChangeActive()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.CARRIER_NETWORK_CHANGE;
        } else if (isRoaming()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.ROAMING;
        } else if (isDataDisabled()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.DATA_DISABLED;
        }
        if (isEmergencyOnly() != ((MobileState) this.mCurrentState).isEmergency) {
            ((MobileState) this.mCurrentState).isEmergency = isEmergencyOnly();
            this.mNetworkController.recalculateEmergency();
        }
        if (((MobileState) this.mCurrentState).networkName == this.mNetworkNameDefault && this.mServiceState != null && !TextUtils.isEmpty(this.mServiceState.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkName = this.mServiceState.getOperatorAlphaShort();
        }
        ((MobileState) this.mCurrentState).networkIcon = NetworkTypeUtils.getNetworkTypeIcon(this.mServiceState, this.mConfig, hasService());
        ((MobileState) this.mCurrentState).volteIcon = getVolteIcon();
        notifyListenersIfNecessary();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.statusbar.policy.SignalController
    public MobileState cleanState() {
        return new MobileState();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void dump(PrintWriter printWriter) {
        super.dump(printWriter);
        printWriter.println("  mSubscription=" + this.mSubscriptionInfo + ",");
        printWriter.println("  mServiceState=" + this.mServiceState + ",");
        printWriter.println("  mSignalStrength=" + this.mSignalStrength + ",");
        printWriter.println("  mDataState=" + this.mDataState + ",");
        printWriter.println("  mDataNetType=" + this.mDataNetType + ",");
    }

    public SubscriptionInfo getControllerSubInfo() {
        return this.mSubscriptionInfo;
    }

    public boolean getControllserHasService() {
        return hasService();
    }

    public void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.provider.Telephony.SPN_STRINGS_UPDATED")) {
            updateNetworkName(intent.getBooleanExtra("showSpn", false), intent.getStringExtra("spn"), intent.getStringExtra("spnData"), intent.getBooleanExtra("showPlmn", false), intent.getStringExtra("plmn"));
            notifyListenersIfNecessary();
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            updateDataSim();
            notifyListenersIfNecessary();
        } else if (action.equals("com.android.ims.IMS_STATE_CHANGED")) {
            handleImsAction(intent);
            notifyListenersIfNecessary();
        }
    }

    public boolean isEmergencyOnly() {
        return this.mServiceState != null ? this.mServiceState.isEmergencyOnly() : false;
    }

    public boolean isImsOverWfc() {
        return ((MobileState) this.mCurrentState).imsCap == 2;
    }

    public boolean isLteNetWork() {
        boolean z = true;
        if (this.mDataNetType != 13) {
            z = this.mDataNetType == 139;
        }
        return z;
    }

    public boolean isWfcEnable() {
        return TelephonyManagerEx.getDefault().isWifiCallingEnabled(this.mSubscriptionInfo.getSubscriptionId());
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void notifyListeners(NetworkController.SignalCallback signalCallback) {
        MobileIconGroup icons = getIcons();
        String stringIfExists = getStringIfExists(getContentDescription());
        String stringIfExists2 = getStringIfExists(icons.mDataContentDescription);
        boolean z = ((MobileState) this.mCurrentState).iconGroup == TelephonyIcons.DATA_DISABLED ? ((MobileState) this.mCurrentState).userSetup : false;
        int customizeSignalStrengthIcon = this.mStatusBarExt.getCustomizeSignalStrengthIcon(this.mSubscriptionInfo.getSubscriptionId(), getCurrentIconId(), this.mSignalStrength, this.mDataNetType, this.mServiceState);
        boolean z2 = (((MobileState) this.mCurrentState).dataConnected || ((MobileState) this.mCurrentState).iconGroup == TelephonyIcons.ROAMING) ? true : z;
        NetworkController.IconState iconState = new NetworkController.IconState(((MobileState) this.mCurrentState).enabled && !((MobileState) this.mCurrentState).airplaneMode, customizeSignalStrengthIcon, stringIfExists);
        int i = 0;
        NetworkController.IconState iconState2 = null;
        String str = null;
        if (((MobileState) this.mCurrentState).dataSim) {
            i = z2 ? icons.mQsDataType : 0;
            iconState2 = new NetworkController.IconState(((MobileState) this.mCurrentState).enabled ? !((MobileState) this.mCurrentState).isEmergency : false, getQsCurrentIconId(), stringIfExists);
            str = ((MobileState) this.mCurrentState).isEmergency ? null : ((MobileState) this.mCurrentState).networkName;
        }
        boolean z3 = (!((MobileState) this.mCurrentState).dataConnected || ((MobileState) this.mCurrentState).carrierNetworkChangeMode) ? false : ((MobileState) this.mCurrentState).activityIn;
        boolean z4 = (!((MobileState) this.mCurrentState).dataConnected || ((MobileState) this.mCurrentState).carrierNetworkChangeMode) ? false : ((MobileState) this.mCurrentState).activityOut;
        if (((MobileState) this.mCurrentState).isDefault || ((MobileState) this.mCurrentState).iconGroup == TelephonyIcons.ROAMING) {
            z = true;
        }
        signalCallback.setMobileDataIndicators(iconState, iconState2, this.mStatusBarExt.getDataTypeIcon(this.mSubscriptionInfo.getSubscriptionId(), z2 & z ? icons.mDataType : 0, this.mDataNetType, ((MobileState) this.mCurrentState).dataConnected ? 2 : 0, this.mServiceState), this.mStatusBarExt.getNetworkTypeIcon(this.mSubscriptionInfo.getSubscriptionId(), ((MobileState) this.mCurrentState).networkIcon, this.mDataNetType, this.mServiceState), (!((MobileState) this.mCurrentState).airplaneMode || isWfcEnable()) ? ((MobileState) this.mCurrentState).volteIcon : 0, i, z3, z4, stringIfExists2, str, icons.mIsWide, this.mSubscriptionInfo.getSubscriptionId());
        this.mNetworkController.refreshPlmnCarrierLabel();
    }

    public void registerListener() {
        this.mPhone.listen(this.mPhoneStateListener, 66017);
        this.mStatusBarExt.registerOpStateListener();
    }

    void setActivity(int i) {
        ((MobileState) this.mCurrentState).activityIn = i != 3 ? i == 1 : true;
        MobileState mobileState = (MobileState) this.mCurrentState;
        boolean z = true;
        if (i != 3) {
            z = i == 2;
        }
        mobileState.activityOut = z;
        notifyListenersIfNecessary();
    }

    public void setAirplaneMode(boolean z) {
        ((MobileState) this.mCurrentState).airplaneMode = z;
        notifyListenersIfNecessary();
    }

    public void setCarrierNetworkChangeMode(boolean z) {
        ((MobileState) this.mCurrentState).carrierNetworkChangeMode = z;
        updateTelephony();
    }

    public void setConfiguration(NetworkControllerImpl.Config config) {
        this.mConfig = config;
        mapIconSets();
        updateTelephony();
    }

    public void setUserSetupComplete(boolean z) {
        ((MobileState) this.mCurrentState).userSetup = z;
        notifyListenersIfNecessary();
    }

    public void unregisterListener() {
        this.mPhone.listen(this.mPhoneStateListener, 0);
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void updateConnectivity(BitSet bitSet, BitSet bitSet2) {
        boolean z = bitSet2.get(this.mTransportType);
        ((MobileState) this.mCurrentState).isDefault = bitSet.get(this.mTransportType);
        ((MobileState) this.mCurrentState).inetCondition = (z || !((MobileState) this.mCurrentState).isDefault) ? 1 : 0;
        Log.d(this.mTag, "mCurrentState.inetCondition = " + ((MobileState) this.mCurrentState).inetCondition);
        ((MobileState) this.mCurrentState).inetCondition = this.mMobileIconExt.customizeMobileNetCondition(((MobileState) this.mCurrentState).inetCondition);
        notifyListenersIfNecessary();
    }

    void updateNetworkName(boolean z, String str, String str2, boolean z2, String str3) {
        Log.d("CarrierLabel", "updateNetworkName showSpn=" + z + " spn=" + str + " dataSpn=" + str2 + " showPlmn=" + z2 + " plmn=" + str3);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        if (z2 && str3 != null) {
            sb.append(str3);
            sb2.append(str3);
        }
        if (z && str != null) {
            if (sb.length() != 0) {
                sb.append(this.mNetworkNameSeparator);
            }
            sb.append(str);
        }
        if (sb.length() != 0) {
            ((MobileState) this.mCurrentState).networkName = sb.toString();
        } else {
            ((MobileState) this.mCurrentState).networkName = this.mNetworkNameDefault;
        }
        if (z && str2 != null) {
            if (sb2.length() != 0) {
                sb2.append(this.mNetworkNameSeparator);
            }
            sb2.append(str2);
        }
        if (sb2.length() == 0 && z && str != null) {
            Log.d("CarrierLabel", "show spn instead 'no service' here: " + str);
            sb2.append(str);
        }
        if (sb2.length() != 0) {
            ((MobileState) this.mCurrentState).networkNameData = sb2.toString();
            return;
        }
        ((MobileState) this.mCurrentState).networkNameData = this.mNetworkNameDefault;
    }
}
