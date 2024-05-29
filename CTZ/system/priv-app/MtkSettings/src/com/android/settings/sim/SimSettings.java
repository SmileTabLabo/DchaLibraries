package com.android.settings.sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISimManagementExt;
import com.mediatek.settings.sim.RadioPowerController;
import com.mediatek.settings.sim.RadioPowerPreference;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.TelephonyUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class SimSettings extends RestrictedSettingsFragment implements Indexable {
    private static final boolean ENG_LOAD;
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER;
    private List<SubscriptionInfo> mAvailableSubInfos;
    private Context mContext;
    private boolean mIsAirplaneModeOn;
    private int mNumSlots;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener;
    private RadioPowerController mRadioController;
    private BroadcastReceiver mReceiver;
    private List<SubscriptionInfo> mSelectableSubInfos;
    private PreferenceScreen mSimCards;
    private SimHotSwapHandler mSimHotSwapHandler;
    private ISimManagementExt mSimManagementExt;
    private List<SubscriptionInfo> mSubInfoList;
    private SubscriptionManager mSubscriptionManager;

    static {
        ENG_LOAD = SystemProperties.get("ro.build.type").equals("eng") || Log.isLoggable("SimSettings", 3);
        SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.sim.SimSettings.3
            @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
                ArrayList arrayList = new ArrayList();
                if (Utils.showSimCardTile(context)) {
                    SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
                    searchIndexableResource.xmlResId = R.xml.sim_settings;
                    arrayList.add(searchIndexableResource);
                }
                return arrayList;
            }
        };
    }

    public SimSettings() {
        super("no_config_sim");
        this.mAvailableSubInfos = null;
        this.mSubInfoList = null;
        this.mSelectableSubInfos = null;
        this.mSimCards = null;
        this.mIsAirplaneModeOn = false;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.settings.sim.SimSettings.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("SimSettings", "onReceive, action=" + action);
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    SimSettings.this.handleAirplaneModeChange(intent);
                } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    SimSettings.this.updateCellularDataValues();
                } else if (action.equals("android.telecom.action.PHONE_ACCOUNT_REGISTERED") || action.equals("android.telecom.action.PHONE_ACCOUNT_UNREGISTERED")) {
                    SimSettings.this.updateCallValues();
                } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE") || action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED")) {
                    SimSettings.this.updateActivitesCategory();
                } else if (action.equals("android.intent.action.PHONE_STATE")) {
                    SimSettings.this.updateActivitesCategory();
                } else if (action.equals("com.mediatek.intent.action.RADIO_STATE_CHANGED")) {
                    if (SimSettings.this.mRadioController.isRadioSwitchComplete(intent.getIntExtra("subId", -1))) {
                        SimSettings.this.handleRadioPowerSwitchComplete();
                    }
                }
            }
        };
        this.mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() { // from class: com.android.settings.sim.SimSettings.2
            @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
            public void onSubscriptionsChanged() {
                SimSettings.this.log("onSubscriptionsChanged:");
                SimSettings.this.updateSubscriptions();
            }
        };
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 88;
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = getActivity();
        this.mSubscriptionManager = SubscriptionManager.from(getActivity());
        addPreferencesFromResource(R.xml.sim_settings);
        this.mNumSlots = ((TelephonyManager) getActivity().getSystemService("phone")).getSimCount();
        this.mSimCards = (PreferenceScreen) findPreference("sim_cards");
        this.mAvailableSubInfos = new ArrayList(this.mNumSlots);
        this.mSelectableSubInfos = new ArrayList();
        SimSelectNotification.cancelNotification(getActivity());
        this.mRadioController = RadioPowerController.getInstance(getContext());
        initForSimStateChange();
        this.mSimManagementExt = UtilsExt.getSimManagementExt(getActivity());
        this.mSimManagementExt.onCreate();
        this.mSimManagementExt.initPlugin(this);
        logInEng("PrimarySim add option");
        this.mSimManagementExt.initPrimarySim(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSubscriptions() {
        this.mSubInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        for (int i = 0; i < this.mNumSlots; i++) {
            Preference findPreference = this.mSimCards.findPreference("sim" + i);
            if (findPreference instanceof SimPreference) {
                this.mSimCards.removePreference(findPreference);
            }
        }
        this.mAvailableSubInfos.clear();
        this.mSelectableSubInfos.clear();
        for (int i2 = 0; i2 < this.mNumSlots; i2++) {
            SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = this.mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i2);
            SimPreference simPreference = new SimPreference(getPrefContext(), activeSubscriptionInfoForSimSlotIndex, i2);
            simPreference.setOrder(i2 - this.mNumSlots);
            if (activeSubscriptionInfoForSimSlotIndex == null) {
                simPreference.bindRadioPowerState(-1, false, false, this.mIsAirplaneModeOn);
            } else {
                int subscriptionId = activeSubscriptionInfoForSimSlotIndex.getSubscriptionId();
                boolean isRadioOn = TelephonyUtils.isRadioOn(subscriptionId, this.mContext);
                simPreference.bindRadioPowerState(subscriptionId, !this.mIsAirplaneModeOn && this.mRadioController.isRadioSwitchComplete(subscriptionId, isRadioOn), isRadioOn, this.mIsAirplaneModeOn);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("addPreference slot=");
            sb.append(i2);
            sb.append(", subInfo=");
            sb.append(activeSubscriptionInfoForSimSlotIndex == null ? "null" : activeSubscriptionInfoForSimSlotIndex);
            logInEng(sb.toString());
            this.mSimCards.addPreference(simPreference);
            this.mAvailableSubInfos.add(activeSubscriptionInfoForSimSlotIndex);
            if (activeSubscriptionInfoForSimSlotIndex != null) {
                this.mSelectableSubInfos.add(activeSubscriptionInfoForSimSlotIndex);
            }
        }
        updateActivitesCategory();
    }

    private void updateSimSlotValues() {
        int preferenceCount = this.mSimCards.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = this.mSimCards.getPreference(i);
            if (preference instanceof SimPreference) {
                ((SimPreference) preference).update();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateActivitesCategory() {
        updateCellularDataValues();
        updateCallValues();
        updateSmsValues();
        this.mSimManagementExt.subChangeUpdatePrimarySIM();
    }

    private void updateSmsValues() {
        Preference findPreference = findPreference("sim_sms");
        if (findPreference == null) {
            return;
        }
        SubscriptionInfo defaultSmsSubscriptionInfo = this.mSubscriptionManager.getDefaultSmsSubscriptionInfo();
        findPreference.setTitle(R.string.sms_messages_title);
        log("[updateSmsValues] mSubInfoList=" + this.mSubInfoList);
        if (defaultSmsSubscriptionInfo != null) {
            findPreference.setSummary(defaultSmsSubscriptionInfo.getDisplayName());
            findPreference.setEnabled(this.mSelectableSubInfos.size() > 1);
        } else if (defaultSmsSubscriptionInfo == null) {
            findPreference.setSummary(R.string.sim_calls_ask_first_prefs_title);
            findPreference.setEnabled(this.mSelectableSubInfos.size() >= 1);
            this.mSimManagementExt.updateDefaultSmsSummary(findPreference);
        }
        this.mSimManagementExt.configSimPreferenceScreen(findPreference, "sim_sms", this.mSelectableSubInfos.size());
        this.mSimManagementExt.setPrefSummary(findPreference, "sim_sms");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCellularDataValues() {
        Preference findPreference = findPreference("sim_cellular_data");
        if (findPreference == null) {
            return;
        }
        SubscriptionInfo defaultDataSubscriptionInfo = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
        findPreference.setTitle(R.string.cellular_data_title);
        log("[updateCellularDataValues] mSubInfoList=" + this.mSubInfoList);
        log("default subInfo=" + defaultDataSubscriptionInfo);
        SubscriptionInfo defaultSubId = this.mSimManagementExt.setDefaultSubId(getActivity(), defaultDataSubscriptionInfo, "sim_cellular_data");
        log("updated subInfo=" + defaultSubId);
        boolean z = this.mSelectableSubInfos.size() > 1;
        if (defaultSubId != null) {
            findPreference.setSummary(defaultSubId.getDisplayName());
        } else if (defaultSubId == null) {
            findPreference.setSummary(R.string.sim_selection_required_pref);
            z = this.mSelectableSubInfos.size() >= 1;
        }
        findPreference.setEnabled(shouldEnableSimPref(z));
        this.mSimManagementExt.configSimPreferenceScreen(findPreference, "sim_cellular_data", -1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCallValues() {
        String str;
        Preference findPreference = findPreference("sim_calls");
        if (findPreference == null) {
            return;
        }
        TelecomManager from = TelecomManager.from(this.mContext);
        PhoneAccountHandle userSelectedOutgoingPhoneAccount = from.getUserSelectedOutgoingPhoneAccount();
        List<PhoneAccountHandle> callCapablePhoneAccounts = from.getCallCapablePhoneAccounts();
        findPreference.setTitle(R.string.calls_title);
        PhoneAccountHandle defaultCallValue = this.mSimManagementExt.setDefaultCallValue(userSelectedOutgoingPhoneAccount);
        log("updateCallValues, PhoneAccountSize=" + callCapablePhoneAccounts.size() + ", phoneAccount=" + defaultCallValue);
        PhoneAccount phoneAccount = defaultCallValue == null ? null : from.getPhoneAccount(defaultCallValue);
        if (phoneAccount == null) {
            str = this.mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title);
        } else {
            str = (String) phoneAccount.getLabel();
        }
        findPreference.setSummary(str);
        findPreference.setEnabled(callCapablePhoneAccounts.size() > 1);
        this.mSimManagementExt.configSimPreferenceScreen(findPreference, "sim_calls", callCapablePhoneAccounts.size());
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        updateSubscriptions();
        removeItemsForTablet();
        this.mSimManagementExt.onResume(getActivity());
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        this.mSimManagementExt.onPause();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        Context context = this.mContext;
        Intent intent = new Intent(context, SimDialogActivity.class);
        intent.addFlags(268435456);
        if (preference instanceof SimPreference) {
            Intent intent2 = new Intent(context, SimPreferenceDialog.class);
            intent2.putExtra("slot_id", ((SimPreference) preference).getSlotId());
            startActivity(intent2);
        } else if (findPreference("sim_cellular_data") == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 0);
            context.startActivity(intent);
        } else if (findPreference("sim_calls") == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 1);
            context.startActivity(intent);
        } else if (findPreference("sim_sms") == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 2);
            context.startActivity(intent);
        } else if (findPreference("primary_SIM_key") == preference) {
            log("host onPreferenceTreeClick 1");
            this.mSimManagementExt.onPreferenceClick(context);
            return true;
        } else {
            this.mSimManagementExt.handleEvent(this, context, preference);
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SimPreference extends RadioPowerPreference {
        Context mContext;
        private int mSlotId;
        private SubscriptionInfo mSubInfoRecord;

        public SimPreference(Context context, SubscriptionInfo subscriptionInfo, int i) {
            super(context);
            this.mContext = context;
            this.mSubInfoRecord = subscriptionInfo;
            this.mSlotId = i;
            setKey("sim" + this.mSlotId);
            update();
        }

        public void update() {
            Resources resources = this.mContext.getResources();
            boolean z = true;
            setTitle(String.format(this.mContext.getResources().getString(R.string.sim_editor_title), Integer.valueOf(this.mSlotId + 1)));
            if (this.mSubInfoRecord != null) {
                String phoneNumber = SimSettings.this.getPhoneNumber(this.mSubInfoRecord);
                SimSettings.this.logInEng("slot=" + this.mSlotId + ", phoneNum=" + phoneNumber);
                if (TextUtils.isEmpty(phoneNumber)) {
                    setSummary(this.mSubInfoRecord.getDisplayName());
                } else {
                    setSummary(((Object) this.mSubInfoRecord.getDisplayName()) + " - " + ((Object) PhoneNumberUtils.createTtsSpannable(phoneNumber)));
                    setEnabled(true);
                }
                setIcon(new BitmapDrawable(resources, this.mSubInfoRecord.createIconBitmap(this.mContext)));
                int subscriptionId = this.mSubInfoRecord.getSubscriptionId();
                boolean isRadioOn = TelephonyUtils.isRadioOn(subscriptionId, getContext());
                boolean isRadioSwitchComplete = SimSettings.this.mRadioController.isRadioSwitchComplete(subscriptionId, isRadioOn);
                setRadioEnabled(!SimSettings.this.mIsAirplaneModeOn && isRadioSwitchComplete);
                if (isRadioSwitchComplete) {
                    if (SimSettings.this.mIsAirplaneModeOn || !isRadioOn) {
                        z = false;
                    }
                    setRadioOn(z);
                    return;
                }
                return;
            }
            setSummary(R.string.sim_slot_empty);
            setFragment(null);
            setEnabled(false);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int getSlotId() {
            return this.mSlotId;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPhoneNumber(SubscriptionInfo subscriptionInfo) {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).getLine1Number(subscriptionInfo.getSubscriptionId());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void log(String str) {
        Log.d("SimSettings", str);
    }

    private void initForSimStateChange() {
        this.mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.android.settings.sim.SimSettings.4
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                if (SimSettings.this.getActivity() != null) {
                    SimSettings.this.log("onSimHotSwap, finish Activity.");
                    SimSettings.this.getActivity().finish();
                }
            }
        });
        this.mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getActivity().getApplicationContext());
        logInEng("initForSimStateChange, airplaneMode=" + this.mIsAirplaneModeOn);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        intentFilter.addAction("com.mediatek.intent.action.RADIO_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.telecom.action.PHONE_ACCOUNT_REGISTERED");
        intentFilter.addAction("android.telecom.action.PHONE_ACCOUNT_UNREGISTERED");
        getActivity().registerReceiver(this.mReceiver, intentFilter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRadioPowerSwitchComplete() {
        logInEng("handleRadioPowerSwitchComplete");
        updateSimSlotValues();
        updateActivitesCategory();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAirplaneModeChange(Intent intent) {
        this.mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
        Log.d("SimSettings", "airplaneMode=" + this.mIsAirplaneModeOn);
        updateSimSlotValues();
        updateActivitesCategory();
        removeItemsForTablet();
        this.mSimManagementExt.updatePrefState();
    }

    private void removeItemsForTablet() {
        if (FeatureOption.MTK_PRODUCT_IS_TABLET) {
            Preference findPreference = findPreference("sim_calls");
            Preference findPreference2 = findPreference("sim_sms");
            Preference findPreference3 = findPreference("sim_cellular_data");
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("sim_activities");
            TelephonyManager from = TelephonyManager.from(getActivity());
            if (!from.isSmsCapable() && findPreference2 != null) {
                preferenceCategory.removePreference(findPreference2);
            }
            if (!from.isMultiSimEnabled() && findPreference3 != null && findPreference2 != null) {
                preferenceCategory.removePreference(findPreference3);
                preferenceCategory.removePreference(findPreference2);
            }
            if (!from.isVoiceCapable() && findPreference != null) {
                preferenceCategory.removePreference(findPreference);
            }
        }
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        logInEng("onDestroy()");
        getActivity().unregisterReceiver(this.mReceiver);
        this.mSimHotSwapHandler.unregisterOnSimHotSwap();
        this.mSimManagementExt.onDestroy();
        super.onDestroy();
    }

    private boolean shouldEnableSimPref(boolean z) {
        int i;
        String str = SystemProperties.get("ril.cdma.inecmmode", "false");
        boolean z2 = str != null && str.contains("true");
        boolean isCapabilitySwitching = TelephonyUtils.isCapabilitySwitching();
        boolean isInCall = TelecomManager.from(this.mContext).isInCall();
        if (SystemProperties.getInt("ro.vendor.mtk_non_dsda_rsim_support", 0) == 1) {
            i = SystemProperties.getInt("vendor.gsm.prefered.rsim.slot", -1);
        } else {
            i = -1;
        }
        log("defaultState=" + z + ", capSwitching=" + isCapabilitySwitching + ", airplaneModeOn=" + this.mIsAirplaneModeOn + ", inCall=" + isInCall + ", ecbMode=" + str + ", rsimPhoneId=" + i);
        return (!z || isCapabilitySwitching || this.mIsAirplaneModeOn || isInCall || z2 || i != -1) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logInEng(String str) {
        if (ENG_LOAD) {
            Log.d("SimSettings", str);
        }
    }
}
