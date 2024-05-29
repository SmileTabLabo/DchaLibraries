package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.internal.telephony.IccCardConstants;
import com.android.settingslib.WirelessUtils;
import com.mediatek.keyguard.Plugin.KeyguardPluginFactory;
import com.mediatek.keyguard.ext.ICarrierTextExt;
import com.mediatek.keyguard.ext.IOperatorSIMString;
import java.util.List;
import java.util.Locale;
/* loaded from: a.zip:com/android/keyguard/CarrierText.class */
public class CarrierText extends TextView {

    /* renamed from: -com-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
    private static final int[] f1x8dbfd0b5 = null;

    /* renamed from: -com-android-keyguard-CarrierText$StatusModeSwitchesValues  reason: not valid java name */
    private static final int[] f2comandroidkeyguardCarrierText$StatusModeSwitchesValues = null;
    private static CharSequence mSeparator;
    private final BroadcastReceiver mBroadcastReceiver;
    private KeyguardUpdateMonitorCallback mCallback;
    private String[] mCarrier;
    private boolean[] mCarrierNeedToShow;
    private ICarrierTextExt mCarrierTextExt;
    private Context mContext;
    private IOperatorSIMString mIOperatorSIMString;
    private final boolean mIsEmergencyCallCapable;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private int mNumOfPhone;
    private StatusMode[] mStatusMode;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private WifiManager mWifiManager;

    /* loaded from: a.zip:com/android/keyguard/CarrierText$CarrierTextTransformationMethod.class */
    private class CarrierTextTransformationMethod extends SingleLineTransformationMethod {
        private final boolean mAllCaps;
        private final Locale mLocale;
        final CarrierText this$0;

        public CarrierTextTransformationMethod(CarrierText carrierText, Context context, boolean z) {
            this.this$0 = carrierText;
            this.mLocale = context.getResources().getConfiguration().locale;
            this.mAllCaps = z;
        }

        @Override // android.text.method.ReplacementTransformationMethod, android.text.method.TransformationMethod
        public CharSequence getTransformation(CharSequence charSequence, View view) {
            CharSequence transformation = super.getTransformation(charSequence, view);
            String str = transformation;
            if (this.mAllCaps) {
                str = transformation;
                if (transformation != null) {
                    str = transformation.toString().toUpperCase(this.mLocale);
                }
            }
            return str;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/keyguard/CarrierText$StatusMode.class */
    public enum StatusMode {
        Normal,
        NetworkLocked,
        SimMissing,
        SimMissingLocked,
        SimPukLocked,
        SimLocked,
        SimPermDisabled,
        SimNotReady,
        SimUnknown,
        NetworkSearching;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static StatusMode[] valuesCustom() {
            return values();
        }
    }

    /* renamed from: -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m577xf663cf59() {
        if (f1x8dbfd0b5 != null) {
            return f1x8dbfd0b5;
        }
        int[] iArr = new int[IccCardConstants.State.values().length];
        try {
            iArr[IccCardConstants.State.ABSENT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 17;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[IccCardConstants.State.NOT_READY.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[IccCardConstants.State.PERM_DISABLED.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[IccCardConstants.State.READY.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[IccCardConstants.State.UNKNOWN.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        f1x8dbfd0b5 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-keyguard-CarrierText$StatusModeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m578getcomandroidkeyguardCarrierText$StatusModeSwitchesValues() {
        if (f2comandroidkeyguardCarrierText$StatusModeSwitchesValues != null) {
            return f2comandroidkeyguardCarrierText$StatusModeSwitchesValues;
        }
        int[] iArr = new int[StatusMode.valuesCustom().length];
        try {
            iArr[StatusMode.NetworkLocked.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[StatusMode.NetworkSearching.ordinal()] = 17;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[StatusMode.Normal.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[StatusMode.SimLocked.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[StatusMode.SimMissing.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[StatusMode.SimMissingLocked.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[StatusMode.SimNotReady.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[StatusMode.SimPermDisabled.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[StatusMode.SimPukLocked.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[StatusMode.SimUnknown.ordinal()] = 18;
        } catch (NoSuchFieldError e10) {
        }
        f2comandroidkeyguardCarrierText$StatusModeSwitchesValues = iArr;
        return iArr;
    }

    public CarrierText(Context context) {
        this(context, null);
        initMembers();
    }

    public CarrierText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.keyguard.CarrierText.1
            final CarrierText this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(intent.getAction())) {
                    Log.w("CarrierText", "receive IPO_SHUTDOWN & clear carrier text.");
                    this.this$0.setText("");
                }
            }
        };
        this.mCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.CarrierText.2
            final CarrierText this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                this.this$0.setSelected(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onRefreshCarrierInfo() {
                this.this$0.updateCarrierText();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
                this.this$0.updateCarrierText();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                this.this$0.setSelected(true);
            }
        };
        this.mContext = context;
        this.mIsEmergencyCallCapable = context.getResources().getBoolean(17956953);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        initMembers();
        this.mIOperatorSIMString = KeyguardPluginFactory.getOperatorSIMString(this.mContext);
        this.mCarrierTextExt = KeyguardPluginFactory.getCarrierTextExt(this.mContext);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.CarrierText, 0, 0);
        try {
            boolean z = obtainStyledAttributes.getBoolean(R$styleable.CarrierText_allCaps, false);
            obtainStyledAttributes.recycle();
            setTransformationMethod(new CarrierTextTransformationMethod(this, this.mContext, z));
            this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private CharSequence appendCsgInfo(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        CharSequence charSequence4;
        if (TextUtils.isEmpty(charSequence2)) {
            charSequence4 = charSequence;
            if (!TextUtils.isEmpty(charSequence3)) {
                charSequence4 = concatenate(charSequence, charSequence3);
            }
        } else {
            charSequence4 = concatenate(charSequence, charSequence2);
        }
        return charSequence4;
    }

    private static CharSequence concatenate(CharSequence charSequence, CharSequence charSequence2) {
        boolean z = !TextUtils.isEmpty(charSequence);
        boolean z2 = !TextUtils.isEmpty(charSequence2);
        return (z && z2) ? new StringBuilder().append(charSequence).append(mSeparator).append(charSequence2).toString() : z ? charSequence : z2 ? charSequence2 : "";
    }

    private CharSequence getCarrierTextForSimState(int i, IccCardConstants.State state, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        CharSequence makeCarrierStringOnEmergencyCapable;
        switch (m578getcomandroidkeyguardCarrierText$StatusModeSwitchesValues()[getStatusForIccState(state).ordinal()]) {
            case 1:
                makeCarrierStringOnEmergencyCapable = makeCarrierStringOnEmergencyCapable(this.mContext.getText(R$string.keyguard_network_locked_message), charSequence, charSequence2, charSequence3);
                break;
            case 2:
                makeCarrierStringOnEmergencyCapable = charSequence;
                break;
            case 3:
                makeCarrierStringOnEmergencyCapable = makeCarrierStringOnEmergencyCapable(getContext().getText(R$string.keyguard_sim_locked_message), charSequence, charSequence2, charSequence3);
                break;
            case 4:
                CharSequence text = getContext().getText(R$string.keyguard_missing_sim_message_short);
                makeCarrierStringOnEmergencyCapable = this.mCarrierTextExt.customizeCarrierTextWhenSimMissing(this.mCarrierTextExt.customizeCarrierText(makeCarrierStringOnEmergencyCapable(text, charSequence, charSequence2, charSequence3), text, i));
                break;
            case 5:
                makeCarrierStringOnEmergencyCapable = null;
                break;
            case 6:
                makeCarrierStringOnEmergencyCapable = null;
                break;
            case 7:
                makeCarrierStringOnEmergencyCapable = getContext().getText(R$string.keyguard_permanent_disabled_sim_message_short);
                break;
            case 8:
                makeCarrierStringOnEmergencyCapable = makeCarrierStringOnEmergencyCapable(getContext().getText(R$string.keyguard_sim_puk_locked_message), charSequence, charSequence2, charSequence3);
                break;
            default:
                makeCarrierStringOnEmergencyCapable = charSequence;
                break;
        }
        String str = makeCarrierStringOnEmergencyCapable;
        if (makeCarrierStringOnEmergencyCapable != null) {
            str = this.mCarrierTextExt.customizeCarrierTextWhenCardTypeLocked(makeCarrierStringOnEmergencyCapable, this.mContext, i, false).toString();
        }
        Log.d("CarrierText", "getCarrierTextForSimState simState=" + state + " text(carrierName)=" + charSequence + " HNB=" + charSequence2 + " CSG=" + charSequence3 + " carrierText=" + str);
        return str;
    }

    private StatusMode getStatusForIccState(IccCardConstants.State state) {
        boolean z;
        if (state == null) {
            return StatusMode.SimUnknown;
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceProvisioned()) {
            z = false;
        } else {
            z = true;
            if (state != IccCardConstants.State.ABSENT) {
                z = state == IccCardConstants.State.PERM_DISABLED;
            }
        }
        if (z) {
            return StatusMode.SimMissingLocked;
        }
        switch (m577xf663cf59()[state.ordinal()]) {
            case 1:
                return StatusMode.SimMissing;
            case 2:
                return StatusMode.NetworkLocked;
            case 3:
                return StatusMode.SimNotReady;
            case 4:
                return StatusMode.SimPermDisabled;
            case 5:
                return StatusMode.SimLocked;
            case 6:
                return StatusMode.SimPukLocked;
            case 7:
                return StatusMode.Normal;
            case 8:
                return StatusMode.SimUnknown;
            default:
                return StatusMode.SimMissing;
        }
    }

    private void initMembers() {
        this.mNumOfPhone = KeyguardUtils.getNumOfPhone();
        this.mCarrier = new String[this.mNumOfPhone];
        this.mCarrierNeedToShow = new boolean[this.mNumOfPhone];
        this.mStatusMode = new StatusMode[this.mNumOfPhone];
        for (int i = 0; i < this.mNumOfPhone; i++) {
            this.mStatusMode[i] = StatusMode.Normal;
        }
    }

    private boolean isWifiOnlyDevice() {
        boolean z = false;
        if (!((ConnectivityManager) getContext().getSystemService("connectivity")).isNetworkSupported(0)) {
            z = true;
        }
        return z;
    }

    private CharSequence makeCarrierStringOnEmergencyCapable(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, CharSequence charSequence4) {
        CharSequence charSequence5 = charSequence2;
        if (!TextUtils.isEmpty(charSequence2)) {
            charSequence5 = appendCsgInfo(charSequence2, charSequence3, charSequence4);
        }
        return this.mIsEmergencyCallCapable ? concatenate(charSequence, charSequence5) : charSequence;
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    private boolean showOrHideCarrier() {
        int i;
        int i2 = 0;
        for (int i3 = 0; i3 < this.mNumOfPhone; i3++) {
            StatusMode statusForIccState = getStatusForIccState(this.mKeyguardUpdateMonitor.getSimStateOfPhoneId(i3));
            boolean showCarrierTextWhenSimMissing = this.mCarrierTextExt.showCarrierTextWhenSimMissing((statusForIccState == StatusMode.SimMissing || statusForIccState == StatusMode.SimMissingLocked) ? true : statusForIccState == StatusMode.SimUnknown, i3);
            Log.d("CarrierText", "showOrHideCarrier() - after showCarrierTextWhenSimMissing,phone#" + i3 + " simMissing = " + showCarrierTextWhenSimMissing);
            if (showCarrierTextWhenSimMissing) {
                this.mCarrierNeedToShow[i3] = false;
            } else {
                this.mCarrierNeedToShow[i3] = true;
                i2++;
            }
        }
        List<SubscriptionInfo> subscriptionInfo = this.mKeyguardUpdateMonitor.getSubscriptionInfo(false);
        if (i2 == 0) {
            String charSequence = this.mUpdateMonitor.getDefaultPlmn().toString();
            int i4 = 0;
            while (true) {
                i = 0;
                if (i4 >= subscriptionInfo.size()) {
                    break;
                }
                SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i4);
                subscriptionInfo2.getSubscriptionId();
                i = subscriptionInfo2.getSimSlotIndex();
                CharSequence carrierName = subscriptionInfo2.getCarrierName();
                if (carrierName != null && !charSequence.contentEquals(carrierName)) {
                    break;
                }
                i4++;
            }
            this.mCarrierNeedToShow[i] = true;
        }
        return i2 == 0;
    }

    private void unregisterBroadcastReceiver() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (ConnectivityManager.from(this.mContext).isNetworkSupported(0)) {
            this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
            this.mKeyguardUpdateMonitor.registerCallback(this.mCallback);
        } else {
            this.mKeyguardUpdateMonitor = null;
            setText("");
        }
        registerBroadcastReceiver();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mKeyguardUpdateMonitor != null) {
            this.mKeyguardUpdateMonitor.removeCallback(this.mCallback);
        }
        unregisterBroadcastReceiver();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSeparator = getResources().getString(17040672);
        setSelected(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
        setLayerType(2, null);
    }

    /* JADX WARN: Code restructure failed: missing block: B:25:0x00e7, code lost:
        if (r7.mWifiManager.getConnectionInfo().getBSSID() != null) goto L26;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void updateCarrierText() {
        boolean z = false;
        if (isWifiOnlyDevice()) {
            Log.d("CarrierText", "updateCarrierText() - WifiOnly deivce, not show carrier text.");
            setText("");
            return;
        }
        showOrHideCarrier();
        int i = 0;
        while (i < this.mNumOfPhone) {
            int subIdUsingPhoneId = KeyguardUtils.getSubIdUsingPhoneId(i);
            IccCardConstants.State simStateOfPhoneId = this.mKeyguardUpdateMonitor.getSimStateOfPhoneId(i);
            SubscriptionInfo subscriptionInfoForSubId = this.mKeyguardUpdateMonitor.getSubscriptionInfoForSubId(subIdUsingPhoneId);
            CharSequence carrierName = subscriptionInfoForSubId == null ? null : subscriptionInfoForSubId.getCarrierName();
            Log.d("CarrierText", "updateCarrierText(): subId = " + subIdUsingPhoneId + " , phoneId = " + i + ", simState = " + simStateOfPhoneId + ", carrierName = " + carrierName);
            boolean z2 = z;
            if (simStateOfPhoneId == IccCardConstants.State.READY) {
                ServiceState serviceState = this.mKeyguardUpdateMonitor.mServiceStates.get(Integer.valueOf(subIdUsingPhoneId));
                z2 = z;
                if (serviceState != null) {
                    z2 = z;
                    if (serviceState.getDataRegState() == 0) {
                        if (serviceState.getRilDataRadioTechnology() == 18) {
                            z2 = z;
                            if (this.mWifiManager.isWifiEnabled()) {
                                z2 = z;
                                if (this.mWifiManager.getConnectionInfo() != null) {
                                    z2 = z;
                                }
                            }
                        }
                        Log.d("CarrierText", "SIM ready and in service: subId=" + subIdUsingPhoneId + ", ss=" + serviceState);
                        z2 = true;
                    }
                }
            }
            CharSequence carrierTextForSimState = getCarrierTextForSimState(i, simStateOfPhoneId, carrierName, null, null);
            String str = carrierTextForSimState;
            if (carrierTextForSimState != null) {
                String operatorSIMString = this.mIOperatorSIMString.getOperatorSIMString(carrierTextForSimState.toString(), i, IOperatorSIMString.SIMChangedTag.DELSIM, this.mContext);
                str = operatorSIMString != null ? this.mCarrierTextExt.customizeCarrierTextCapital(operatorSIMString.toString()).toString() : null;
            }
            if (str != null) {
                this.mCarrier[i] = str.toString();
            } else {
                this.mCarrier[i] = null;
            }
            i++;
            z = z2;
        }
        String str2 = null;
        String customizeCarrierTextDivider = this.mCarrierTextExt.customizeCarrierTextDivider(mSeparator.toString());
        int i2 = 0;
        while (i2 < this.mNumOfPhone) {
            String str3 = str2;
            if (this.mCarrierNeedToShow[i2]) {
                str3 = str2;
                if (this.mCarrier[i2] != null) {
                    str3 = str2 == null ? this.mCarrier[i2] : str2 + customizeCarrierTextDivider + this.mCarrier[i2];
                }
            }
            i2++;
            str2 = str3;
        }
        String str4 = str2;
        if (!z) {
            str4 = str2;
            if (WirelessUtils.isAirplaneModeOn(this.mContext)) {
                str4 = getContext().getString(R$string.airplane_mode);
            }
        }
        Log.d("CarrierText", "updateCarrierText() - after combination, carrierFinalContent = " + str4);
        setText(str4);
    }
}
