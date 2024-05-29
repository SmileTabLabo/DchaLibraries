package com.mediatek.systemui.qs.tiles.ext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import com.android.systemui.qs.QSTile;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.statusbar.extcb.IconIdWrapper;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import java.util.List;
/* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/SimDataConnectionTile.class */
public class SimDataConnectionTile extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "SimDataConnectionTile";
    private boolean mListening;
    private IconIdWrapper[] mSimConnectionIconWrapperArray;
    private SimDataSwitchStateMachine mSimDataSwitchStateMachine;
    private CharSequence mTileLabel;

    /* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/SimDataConnectionTile$SIMConnState.class */
    public enum SIMConnState {
        SIM1_E_D,
        SIM1_E_E,
        SIM1_D_D,
        SIM1_D_E,
        SIM2_E_D,
        SIM2_E_E,
        SIM2_D_D,
        SIM2_D_E,
        NO_SIM,
        SIM1_E_F,
        SIM1_D_F,
        SIM2_E_F,
        SIM2_D_F;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static SIMConnState[] valuesCustom() {
            return values();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/SimDataConnectionTile$SimDataSwitchStateMachine.class */
    public class SimDataSwitchStateMachine {

        /* renamed from: -com-mediatek-systemui-qs-tiles-ext-SimDataConnectionTile$SIMConnStateSwitchesValues  reason: not valid java name */
        private static final int[] f10xe858a61c = null;
        private static final int EVENT_SWITCH_TIME_OUT = 2000;
        private static final int SWITCH_TIME_OUT_LENGTH = 30000;
        private static final String TRANSACTION_START = "com.android.mms.transaction.START";
        private static final String TRANSACTION_STOP = "com.android.mms.transaction.STOP";
        final int[] $SWITCH_TABLE$com$mediatek$systemui$qs$tiles$ext$SimDataConnectionTile$SIMConnState;
        private boolean mIsAirlineMode;
        protected boolean mIsUserSwitching;
        boolean mMmsOngoing;
        private PhoneStateListener[] mPhoneStateListener;
        boolean mSimConnStateTrackerReady;
        private int mSlotCount;
        TelephonyManager mTelephonyManager;
        final SimDataConnectionTile this$0;
        private SIMConnState mCurrentSimConnState = SIMConnState.NO_SIM;
        private Handler mDataTimerHandler = new Handler(this) { // from class: com.mediatek.systemui.qs.tiles.ext.SimDataConnectionTile.SimDataSwitchStateMachine.1
            final SimDataSwitchStateMachine this$1;

            {
                this.this$1 = this;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.arg1;
                int i2 = message.arg2;
                switch (message.what) {
                    case SimDataSwitchStateMachine.EVENT_SWITCH_TIME_OUT /* 2000 */:
                        Log.d(SimDataConnectionTile.TAG, "switching time out..... switch from " + i + " to " + i2);
                        if (this.this$1.this$0.isWifiOnlyDevice()) {
                            return;
                        }
                        this.this$1.refresh();
                        return;
                    default:
                        return;
                }
            }
        };
        private BroadcastReceiver mSimStateIntentReceiver = new BroadcastReceiver(this) { // from class: com.mediatek.systemui.qs.tiles.ext.SimDataConnectionTile.SimDataSwitchStateMachine.2
            final SimDataSwitchStateMachine this$1;

            {
                this.this$1 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int i = 0;
                String action = intent.getAction();
                Log.d(SimDataConnectionTile.TAG, "onReceive action is " + action);
                if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    this.this$1.updateSimConnTile();
                } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    boolean booleanExtra = intent.getBooleanExtra("state", false);
                    Log.d(SimDataConnectionTile.TAG, "airline mode changed: state is " + booleanExtra);
                    if (this.this$1.mSimConnStateTrackerReady) {
                        this.this$1.setAirplaneMode(booleanExtra);
                    }
                    this.this$1.updateSimConnTile();
                } else if (action.equals("android.intent.action.ANY_DATA_STATE")) {
                    PhoneConstants.DataState mobileDataState = this.this$1.getMobileDataState(intent);
                    String stringExtra = intent.getStringExtra("apnType");
                    boolean z = false;
                    if (stringExtra != null) {
                        String[] split = stringExtra.split(",");
                        int length = split.length;
                        while (true) {
                            z = false;
                            if (i >= length) {
                                break;
                            } else if ("default".equals(split[i])) {
                                z = true;
                                break;
                            } else {
                                i++;
                            }
                        }
                    }
                    if (z) {
                        if ((mobileDataState == PhoneConstants.DataState.CONNECTED || mobileDataState == PhoneConstants.DataState.DISCONNECTED) && !this.this$1.isMmsOngoing()) {
                            this.this$1.updateSimConnTile();
                        }
                    }
                } else if (action.equals("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE")) {
                    this.this$1.updateSimConnTile();
                } else if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                    this.this$1.unRegisterPhoneStateListener();
                    this.this$1.updateSimConnTile();
                    this.this$1.registerPhoneStateListener();
                } else if (action.equals(SimDataSwitchStateMachine.TRANSACTION_START)) {
                    if (this.this$1.this$0.isWifiOnlyDevice() || !this.this$1.mSimConnStateTrackerReady) {
                        return;
                    }
                    this.this$1.setIsMmsOnging(true);
                    this.this$1.updateSimConnTile();
                } else if (action.equals(SimDataSwitchStateMachine.TRANSACTION_STOP) && !this.this$1.this$0.isWifiOnlyDevice() && this.this$1.mSimConnStateTrackerReady) {
                    this.this$1.setIsMmsOnging(false);
                    this.this$1.updateSimConnTile();
                }
            }
        };

        /* renamed from: -getcom-mediatek-systemui-qs-tiles-ext-SimDataConnectionTile$SIMConnStateSwitchesValues  reason: not valid java name */
        private static /* synthetic */ int[] m2388xe212dcc0() {
            if (f10xe858a61c != null) {
                return f10xe858a61c;
            }
            int[] iArr = new int[SIMConnState.valuesCustom().length];
            try {
                iArr[SIMConnState.NO_SIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SIMConnState.SIM1_D_D.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[SIMConnState.SIM1_D_E.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[SIMConnState.SIM1_D_F.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[SIMConnState.SIM1_E_D.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[SIMConnState.SIM1_E_E.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[SIMConnState.SIM1_E_F.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[SIMConnState.SIM2_D_D.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[SIMConnState.SIM2_D_E.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[SIMConnState.SIM2_D_F.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[SIMConnState.SIM2_E_D.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[SIMConnState.SIM2_E_E.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[SIMConnState.SIM2_E_F.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            f10xe858a61c = iArr;
            return iArr;
        }

        public SimDataSwitchStateMachine(SimDataConnectionTile simDataConnectionTile) {
            this.this$0 = simDataConnectionTile;
            this.mSlotCount = 0;
            this.mTelephonyManager = (TelephonyManager) simDataConnectionTile.mContext.getSystemService("phone");
            this.mSlotCount = SIMHelper.getSlotCount();
            this.mPhoneStateListener = new PhoneStateListener[this.mSlotCount];
        }

        private void addConnTile() {
            this.mSimConnStateTrackerReady = true;
        }

        private void enterNextState(SIMConnState sIMConnState) {
            Log.d(SimDataConnectionTile.TAG, "enterNextState state is " + sIMConnState);
            switch (m2388xe212dcc0()[sIMConnState.ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 8:
                case 9:
                case 10:
                    Log.d(SimDataConnectionTile.TAG, "No Sim or one Sim do nothing!");
                    return;
                case 5:
                    Log.d(SimDataConnectionTile.TAG, "Try to switch from Sim1 to Sim2! mSimCurrentCurrentState=" + this.mCurrentSimConnState);
                    this.mCurrentSimConnState = SIMConnState.SIM2_E_D;
                    switchDataDefaultSIM(1);
                    return;
                case 6:
                    Log.d(SimDataConnectionTile.TAG, "Try to switch from Sim1 to Sim2! mSimCurrentCurrentState=" + this.mCurrentSimConnState);
                    this.mCurrentSimConnState = SIMConnState.SIM2_E_E;
                    switchDataDefaultSIM(1);
                    return;
                case 7:
                    Log.d(SimDataConnectionTile.TAG, "Try to switch from Sim1 to Sim2! mSimCurrentCurrentState=" + this.mCurrentSimConnState);
                    switchDataDefaultSIM(1);
                    return;
                case 11:
                    Log.d(SimDataConnectionTile.TAG, "Try to switch from Sim2 to Sim1! mSimCurrentCurrentState=" + this.mCurrentSimConnState);
                    this.mCurrentSimConnState = SIMConnState.SIM1_E_D;
                    switchDataDefaultSIM(0);
                    return;
                case 12:
                    Log.d(SimDataConnectionTile.TAG, "Try to switch from Sim2 to Sim1! mSimCurrentCurrentState=" + this.mCurrentSimConnState);
                    this.mCurrentSimConnState = SIMConnState.SIM1_E_E;
                    switchDataDefaultSIM(0);
                    return;
                case 13:
                    Log.d(SimDataConnectionTile.TAG, "Try to switch from Sim2 to Sim1! mSimCurrentCurrentState=" + this.mCurrentSimConnState);
                    switchDataDefaultSIM(0);
                    return;
                default:
                    return;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public PhoneConstants.DataState getMobileDataState(Intent intent) {
            String stringExtra = intent.getStringExtra("state");
            return stringExtra != null ? Enum.valueOf(PhoneConstants.DataState.class, stringExtra) : PhoneConstants.DataState.DISCONNECTED;
        }

        private PhoneStateListener getPhoneStateListener(int i, int i2) {
            return new PhoneStateListener(this, i, i2) { // from class: com.mediatek.systemui.qs.tiles.ext.SimDataConnectionTile.SimDataSwitchStateMachine.3
                final SimDataSwitchStateMachine this$1;
                final int val$slotId;

                {
                    this.this$1 = this;
                    this.val$slotId = i2;
                }

                @Override // android.telephony.PhoneStateListener
                public void onServiceStateChanged(ServiceState serviceState) {
                    Log.d(SimDataConnectionTile.TAG, "PhoneStateListener:onServiceStateChanged, slot " + this.val$slotId + " servicestate = " + serviceState);
                    this.this$1.updateSimConnTile();
                }
            };
        }

        private void handleDataConnectionChange(int i) {
            Log.d(SimDataConnectionTile.TAG, "handleDataConnectionChange, newSlot=" + i);
            if (SubscriptionManager.getSlotId(SubscriptionManager.getDefaultDataSubscriptionId()) != i) {
                this.mDataTimerHandler.sendEmptyMessageDelayed(EVENT_SWITCH_TIME_OUT, 30000L);
                List<SubscriptionInfo> activeSubscriptionInfoList = SubscriptionManager.from(this.this$0.mContext).getActiveSubscriptionInfoList();
                if (activeSubscriptionInfoList == null || activeSubscriptionInfoList.size() <= 0) {
                    return;
                }
                boolean dataEnabled = this.mTelephonyManager.getDataEnabled();
                for (int i2 = 0; i2 < activeSubscriptionInfoList.size(); i2++) {
                    SubscriptionInfo subscriptionInfo = activeSubscriptionInfoList.get(i2);
                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                    if (i == subscriptionInfo.getSimSlotIndex()) {
                        Log.d(SimDataConnectionTile.TAG, "handleDataConnectionChange. newSlot = " + i + " subId = " + subscriptionId);
                        SubscriptionManager.from(this.this$0.mContext).setDefaultDataSubId(subscriptionId);
                        if (dataEnabled) {
                            this.mTelephonyManager.setDataEnabled(subscriptionId, true);
                        }
                    } else if (dataEnabled) {
                        this.mTelephonyManager.setDataEnabled(subscriptionId, false);
                    }
                }
            }
        }

        private boolean isAirplaneMode() {
            return this.mIsAirlineMode;
        }

        private boolean isDataConnected() {
            return TelephonyManager.getDefault().getDataState() == 2;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isMmsOngoing() {
            return this.mMmsOngoing;
        }

        private boolean isRadioOn(int i) {
            return SIMHelper.isRadioOn(SIMHelper.getFirstSubInSlot(i));
        }

        private boolean isSimEnable(List<SubscriptionInfo> list, int i) {
            boolean z = false;
            if (this.this$0.isSimInsertedBySlot(list, i)) {
                if (isAirplaneMode()) {
                    z = false;
                } else {
                    z = false;
                    if (isRadioOn(i)) {
                        z = false;
                        if (!isSimLocked(i)) {
                            z = true;
                        }
                    }
                }
            }
            return z;
        }

        private boolean isSimInsertedWithUnAvaliable(List<SubscriptionInfo> list, int i) {
            return this.this$0.isSimInsertedBySlot(list, i) ? (!isRadioOn(i) || isAirplaneMode()) ? true : isSimLocked(i) : false;
        }

        private boolean isSimLocked(int i) {
            int simState = TelephonyManager.getDefault().getSimState(i);
            boolean z = (simState == 2 || simState == 3) ? true : simState == 4;
            Log.d(SimDataConnectionTile.TAG, "isSimLocked, slotId=" + i + " simState=" + simState + " bSimLocked= " + z);
            return z;
        }

        private boolean isUserSwitching() {
            return this.mIsUserSwitching;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void registerPhoneStateListener() {
            for (int i = 0; i < this.mSlotCount; i++) {
                int firstSubInSlot = SIMHelper.getFirstSubInSlot(i);
                if (firstSubInSlot >= 0) {
                    this.mPhoneStateListener[i] = getPhoneStateListener(firstSubInSlot, i);
                    this.mTelephonyManager.listen(this.mPhoneStateListener[i], 1);
                } else {
                    this.mPhoneStateListener[i] = null;
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void registerReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
            intentFilter.addAction(TRANSACTION_START);
            intentFilter.addAction(TRANSACTION_STOP);
            intentFilter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
            intentFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
            intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
            this.this$0.mContext.registerReceiver(this.mSimStateIntentReceiver, intentFilter);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setAirplaneMode(boolean z) {
            this.mIsAirlineMode = z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setIsMmsOnging(boolean z) {
            this.mMmsOngoing = z;
        }

        private void setUserSwitching(boolean z) {
            this.mIsUserSwitching = z;
        }

        private void switchDataDefaultSIM(int i) {
            if (this.this$0.isWifiOnlyDevice()) {
                return;
            }
            setUserSwitching(true);
            handleDataConnectionChange(i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void unRegisterPhoneStateListener() {
            for (int i = 0; i < this.mSlotCount; i++) {
                if (this.mPhoneStateListener[i] != null) {
                    this.mTelephonyManager.listen(this.mPhoneStateListener[i], 0);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void unregisterReceiver() {
            this.this$0.mContext.unregisterReceiver(this.mSimStateIntentReceiver);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateSimConnTile() {
            onActualStateChange(this.this$0.mContext, null);
            this.this$0.refreshState();
        }

        public SIMConnState getCurrentSimConnState() {
            return this.mCurrentSimConnState;
        }

        /* JADX WARN: Code restructure failed: missing block: B:5:0x0027, code lost:
            if (r4.this$0.isSimInsertedBySlot(r0, 1) != false) goto L8;
         */
        /* JADX WARN: Code restructure failed: missing block: B:9:0x0039, code lost:
            if (isRadioOn(1) != false) goto L12;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public boolean isClickable() {
            boolean z;
            List<SubscriptionInfo> activeSubscriptionInfoList = SubscriptionManager.from(this.this$0.mContext).getActiveSubscriptionInfoList();
            if (!this.this$0.isSimInsertedBySlot(activeSubscriptionInfoList, 0)) {
                z = false;
            }
            if (!isRadioOn(0)) {
                z = false;
            }
            if (isAirplaneMode()) {
                z = false;
            } else {
                z = false;
                if (!isMmsOngoing()) {
                    z = false;
                    if (!isUserSwitching()) {
                        z = true;
                    }
                }
            }
            return z;
        }

        public void onActualStateChange(Context context, Intent intent) {
            List<SubscriptionInfo> activeSubscriptionInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
            boolean isSimEnable = isSimEnable(activeSubscriptionInfoList, 0);
            boolean isSimEnable2 = isSimEnable(activeSubscriptionInfoList, 1);
            boolean z = false;
            boolean z2 = false;
            int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (SubscriptionManager.getSlotId(defaultDataSubscriptionId) == 0) {
                z = true;
                z2 = false;
            } else if (SubscriptionManager.getSlotId(defaultDataSubscriptionId) == 1) {
                z = false;
                z2 = true;
            }
            Log.d(SimDataConnectionTile.TAG, "SimConnStateTracker onActualStateChange sim1Enable = " + isSimEnable + ", sim2Enable = " + isSimEnable2);
            if (isSimEnable || isSimEnable2) {
                boolean isDataConnected = isDataConnected();
                Log.d(SimDataConnectionTile.TAG, "onActualStateChange, dataConnected = " + isDataConnected + ", sim1Enable = " + isSimEnable + ", sim2Enable = " + isSimEnable2 + ", sim1Conn = " + z + ", sim2Conn = " + z2);
                if (isDataConnected) {
                    if (isSimEnable && isSimEnable2) {
                        if (z) {
                            this.mCurrentSimConnState = SIMConnState.SIM1_E_E;
                        } else {
                            this.mCurrentSimConnState = SIMConnState.SIM2_E_E;
                        }
                    } else if (isSimEnable || !isSimEnable2) {
                        if (isSimEnable && !isSimEnable2) {
                            if (isSimInsertedWithUnAvaliable(activeSubscriptionInfoList, 1) && z2) {
                                this.mCurrentSimConnState = SIMConnState.SIM2_E_F;
                            } else {
                                this.mCurrentSimConnState = SIMConnState.SIM1_D_E;
                            }
                        }
                    } else if (isSimInsertedWithUnAvaliable(activeSubscriptionInfoList, 0) && z) {
                        this.mCurrentSimConnState = SIMConnState.SIM1_E_F;
                    } else {
                        this.mCurrentSimConnState = SIMConnState.SIM2_D_E;
                    }
                } else if (isSimEnable && isSimEnable2) {
                    if (z) {
                        this.mCurrentSimConnState = SIMConnState.SIM1_E_D;
                    } else {
                        this.mCurrentSimConnState = SIMConnState.SIM2_E_D;
                    }
                } else if (isSimEnable || !isSimEnable2) {
                    if (isSimEnable && !isSimEnable2) {
                        if (isSimInsertedWithUnAvaliable(activeSubscriptionInfoList, 1) && z2) {
                            this.mCurrentSimConnState = SIMConnState.SIM2_E_F;
                        } else {
                            this.mCurrentSimConnState = SIMConnState.SIM1_D_D;
                        }
                    }
                } else if (isSimInsertedWithUnAvaliable(activeSubscriptionInfoList, 0) && z) {
                    this.mCurrentSimConnState = SIMConnState.SIM1_E_F;
                } else {
                    this.mCurrentSimConnState = SIMConnState.SIM2_D_D;
                }
            } else if (isSimInsertedWithUnAvaliable(activeSubscriptionInfoList, 0) && z) {
                this.mCurrentSimConnState = SIMConnState.SIM1_D_F;
            } else if (isSimInsertedWithUnAvaliable(activeSubscriptionInfoList, 1) && z2) {
                this.mCurrentSimConnState = SIMConnState.SIM2_D_F;
            } else {
                this.mCurrentSimConnState = SIMConnState.NO_SIM;
            }
            setUserSwitching(false);
        }

        public void refresh() {
            onActualStateChange(this.this$0.mContext, null);
            setUserSwitching(false);
        }

        public void toggleState(Context context) {
            enterNextState(this.mCurrentSimConnState);
        }
    }

    public SimDataConnectionTile(QSTile.Host host) {
        super(host);
        this.mSimConnectionIconWrapperArray = new IconIdWrapper[SIMConnState.valuesCustom().length];
        init();
    }

    private void init() {
        for (int i = 0; i < this.mSimConnectionIconWrapperArray.length; i++) {
            this.mSimConnectionIconWrapperArray[i] = new IconIdWrapper();
        }
        this.mSimDataSwitchStateMachine = new SimDataSwitchStateMachine(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSimInsertedBySlot(List<SubscriptionInfo> list, int i) {
        if (i >= SIMHelper.getSlotCount()) {
            return false;
        }
        if (list == null || list.size() <= 0) {
            Log.d(TAG, "isSimInsertedBySlot, SubscriptionInfo is null");
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : list) {
            if (subscriptionInfo.getSimSlotIndex() == i) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isWifiOnlyDevice() {
        boolean z = false;
        if (!((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0)) {
            z = true;
        }
        return z;
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 111;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        this.mTileLabel = PluginManager.getQuickSettingsPlugin(this.mContext).getTileLabel("simdataconnection");
        return this.mTileLabel;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (this.mSimDataSwitchStateMachine.isClickable()) {
            this.mSimDataSwitchStateMachine.toggleState(this.mContext);
        }
        refreshState();
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleLongClick() {
        handleClick();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int ordinal = this.mSimDataSwitchStateMachine.getCurrentSimConnState().ordinal();
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager.getQuickSettingsPlugin(this.mContext);
        quickSettingsPlugin.customizeSimDataConnectionTile(ordinal, this.mSimConnectionIconWrapperArray[ordinal]);
        booleanState.icon = QsIconWrapper.get(this.mSimConnectionIconWrapperArray[ordinal].getIconId(), this.mSimConnectionIconWrapperArray[ordinal]);
        booleanState.label = quickSettingsPlugin.getTileLabel("simdataconnection");
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            this.mSimDataSwitchStateMachine.registerReceiver();
        } else {
            this.mSimDataSwitchStateMachine.unregisterReceiver();
        }
    }
}
