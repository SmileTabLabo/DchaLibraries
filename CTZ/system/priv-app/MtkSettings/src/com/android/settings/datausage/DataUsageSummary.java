package com.android.settings.datausage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.datausage.BillingCycleSettings;
import com.android.settings.datausage.DataUsageSummary;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.NetworkPolicyEditor;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.net.DataUsageController;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.datausage.TempDataServiceDialogActivity;
import com.mediatek.settings.ext.IDataUsageSummaryExt;
import com.mediatek.settings.sim.TelephonyUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DataUsageSummary extends DataUsageBaseFragment implements DataUsageEditController, Indexable {
    private Context mContext;
    private IDataUsageSummaryExt mDataUsageSummaryExt;
    private int mDefaultSubId;
    private NetworkTemplate mDefaultTemplate;
    private SwitchPreference mEnableDataService;
    private boolean mIsAirplaneModeOn;
    private PhoneStateListener mPhoneStateListener;
    private DataUsageSummaryPreferenceController mSummaryController;
    private DataUsageSummaryPreference mSummaryPreference;
    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryLoader.SummaryProviderFactory() { // from class: com.android.settings.datausage.-$$Lambda$YwlDb-ChrdnT61OB-L_A63UT4To
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory
        public final SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new DataUsageSummary.SummaryProvider(activity, summaryLoader);
        }
    };
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.datausage.DataUsageSummary.1
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            ArrayList arrayList = new ArrayList();
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.data_usage;
            arrayList.add(searchIndexableResource);
            SearchIndexableResource searchIndexableResource2 = new SearchIndexableResource(context);
            searchIndexableResource2.xmlResId = R.xml.data_usage_cellular;
            arrayList.add(searchIndexableResource2);
            SearchIndexableResource searchIndexableResource3 = new SearchIndexableResource(context);
            searchIndexableResource3.xmlResId = R.xml.data_usage_wifi;
            arrayList.add(searchIndexableResource3);
            return arrayList;
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<String> getNonIndexableKeys(Context context) {
            List<String> nonIndexableKeys = super.getNonIndexableKeys(context);
            if (!DataUsageUtils.hasMobileData(context)) {
                nonIndexableKeys.add("mobile_category");
                nonIndexableKeys.add("data_usage_enable");
                nonIndexableKeys.add("cellular_data_usage");
                nonIndexableKeys.add("billing_preference");
            }
            if (!DataUsageUtils.hasWifiRadio(context)) {
                nonIndexableKeys.add("wifi_data_usage");
            }
            nonIndexableKeys.add("wifi_category");
            return nonIndexableKeys;
        }
    };
    private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    int mTempPhoneid = 0;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) { // from class: com.android.settings.datausage.DataUsageSummary.4
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            if (DataUsageSummary.this.mEnableDataService != null) {
                boolean dataService = DataUsageSummary.this.getDataService();
                Log.d("DataUsageSummary", "onChange dataService = " + dataService + ", isChecked = " + DataUsageSummary.this.mEnableDataService.isChecked());
                if (dataService != DataUsageSummary.this.mEnableDataService.isChecked()) {
                    DataUsageSummary.this.mEnableDataService.setChecked(dataService);
                    return;
                }
                return;
            }
            Log.d("DataUsageSummary", "onChange mEnableDataService == null");
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.datausage.DataUsageSummary.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("DataUsageSummary", "mReceiver action = " + action);
            if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                DataUsageSummary.this.mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
                DataUsageSummary.this.updateScreenEnabled();
            } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED") || DataUsageSummary.this.mDataUsageSummaryExt.customDualReceiver(action)) {
                DataUsageSummary.this.updateScreenEnabled();
            } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE") || action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED")) {
                DataUsageSummary.this.updateScreenEnabled();
            }
        }
    };

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_data_usage;
    }

    @Override // com.android.settings.datausage.DataUsageBaseFragment, com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d("DataUsageSummary", "onCreate");
        this.mContext = getContext();
        this.mDataUsageSummaryExt = UtilsExt.getDataUsageSummaryExt(this.mContext.getApplicationContext());
        boolean hasMobileData = DataUsageUtils.hasMobileData(this.mContext);
        this.mDefaultSubId = DataUsageUtils.getDefaultSubscriptionId(this.mContext);
        if (this.mDefaultSubId == -1) {
            Log.d("DataUsageSummary", "onCreate INVALID_SUBSCRIPTION_ID Mobile data false");
            hasMobileData = false;
        }
        this.mDefaultTemplate = DataUsageUtils.getDefaultTemplate(this.mContext, this.mDefaultSubId);
        this.mSummaryPreference = (DataUsageSummaryPreference) findPreference("status_header");
        if (!hasMobileData || !isAdmin()) {
            removePreference("restrict_background");
        }
        boolean hasWifiRadio = DataUsageUtils.hasWifiRadio(this.mContext);
        if (hasMobileData) {
            addMobileSection(this.mDefaultSubId);
            List<SubscriptionInfo> activeSubscriptionInfoList = this.services.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.size() == 2) {
                addDataServiceSection(activeSubscriptionInfoList);
            }
            if (DataUsageUtils.hasSim(this.mContext) && hasWifiRadio) {
                addWifiSection();
            }
        } else if (hasWifiRadio) {
            addWifiSection();
        }
        if (DataUsageUtils.hasEthernet(this.mContext)) {
            addEthernetSection();
        }
        setHasOptionsMenu(false);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        if (UserManager.get(getContext()).isAdminUser()) {
            menuInflater.inflate(R.menu.data_usage, menu);
        }
        if (Utils.isWifiOnly(getActivity())) {
            menu.removeItem(R.id.data_usage_menu_cellular_networks);
        }
        menu.removeItem(R.id.data_usage_menu_cellular_networks);
        menu.removeItem(R.id.data_usage_menu_cellular_data_control);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.data_usage_menu_cellular_data_control /* 2131362034 */:
                Log.d("DataUsageSummary", "select CELLULAR_DATA");
                try {
                    startActivity(new Intent("com.mediatek.security.CELLULAR_DATA"));
                } catch (ActivityNotFoundException e) {
                    Log.e("DataUsageSummary", "cellular data control activity not found!!!");
                }
                return true;
            case R.id.data_usage_menu_cellular_networks /* 2131362035 */:
                Log.d("DataUsageSummary", "select CELLULAR_NETWORKDATA");
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    @Override // com.android.settings.dashboard.DashboardFragment, android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == findPreference("status_header")) {
            BillingCycleSettings.BytesEditorFragment.show((DataUsageEditController) this, false);
            return false;
        }
        return super.onPreferenceTreeClick(preference);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.data_usage;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "DataUsageSummary";
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        Activity activity = getActivity();
        ArrayList arrayList = new ArrayList();
        this.mSummaryController = new DataUsageSummaryPreferenceController(activity, getLifecycle(), this);
        arrayList.add(this.mSummaryController);
        getLifecycle().addObserver(this.mSummaryController);
        return arrayList;
    }

    void addMobileSection(int i) {
        addMobileSection(i, (SubscriptionInfo) null);
    }

    void addMobileSection(int i, int i2) {
        addMobileSection(i, null, i2);
    }

    private void addMobileSection(int i, SubscriptionInfo subscriptionInfo, int i2) {
        TemplatePreferenceCategory templatePreferenceCategory = (TemplatePreferenceCategory) inflatePreferences(R.xml.data_usage_cellular, i2);
        Log.d("DataUsageSummary", "addMobileSection with subID: " + i + " orderd = " + i2);
        templatePreferenceCategory.setTemplate(getNetworkTemplate(i), i, this.services);
        templatePreferenceCategory.pushTemplates(this.services);
        if (subscriptionInfo != null && !TextUtils.isEmpty(subscriptionInfo.getDisplayName())) {
            templatePreferenceCategory.findPreference("mobile_category").setTitle(subscriptionInfo.getDisplayName());
        }
    }

    private Preference inflatePreferences(int i, int i2) {
        PreferenceScreen inflateFromResource = getPreferenceManager().inflateFromResource(getPrefContext(), i, null);
        Preference preference = inflateFromResource.getPreference(0);
        inflateFromResource.removeAll();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preference.setOrder(i2);
        preferenceScreen.addPreference(preference);
        return preference;
    }

    private void addMobileSection(int i, SubscriptionInfo subscriptionInfo) {
        TemplatePreferenceCategory templatePreferenceCategory = (TemplatePreferenceCategory) inflatePreferences(R.xml.data_usage_cellular);
        Log.d("DataUsageSummary", "addMobileSection with subID: " + i);
        templatePreferenceCategory.setTemplate(getNetworkTemplate(i), i, this.services);
        templatePreferenceCategory.pushTemplates(this.services);
        if (subscriptionInfo != null && !TextUtils.isEmpty(subscriptionInfo.getDisplayName())) {
            templatePreferenceCategory.findPreference("mobile_category").setTitle(subscriptionInfo.getDisplayName());
        }
    }

    void addWifiSection() {
        ((TemplatePreferenceCategory) inflatePreferences(R.xml.data_usage_wifi)).setTemplate(NetworkTemplate.buildTemplateWifiWildcard(), 0, this.services);
    }

    private void addEthernetSection() {
        ((TemplatePreferenceCategory) inflatePreferences(R.xml.data_usage_ethernet)).setTemplate(NetworkTemplate.buildTemplateEthernet(), 0, this.services);
    }

    private Preference inflatePreferences(int i) {
        PreferenceScreen inflateFromResource = getPreferenceManager().inflateFromResource(getPrefContext(), i, null);
        Preference preference = inflateFromResource.getPreference(0);
        inflateFromResource.removeAll();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preference.setOrder(preferenceScreen.getPreferenceCount());
        preferenceScreen.addPreference(preference);
        return preference;
    }

    private NetworkTemplate getNetworkTemplate(int i) {
        NetworkTemplate buildTemplateMobileAll = NetworkTemplate.buildTemplateMobileAll(this.services.mTelephonyManager.getSubscriberId(i));
        Log.d("DataUsageSummary", "getNetworkTemplate with subID: " + i);
        return NetworkTemplate.normalize(buildTemplateMobileAll, this.services.mTelephonyManager.getMergedSubscriberIds());
    }

    @Override // com.android.settings.datausage.DataUsageBaseFragment, com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        TemplatePreferenceCategory templatePreferenceCategory;
        super.onResume();
        int defaultSubscriptionId = DataUsageUtils.getDefaultSubscriptionId(this.mContext);
        Log.d("DataUsageSummary", "onResumed mDefaultSubId = " + this.mDefaultSubId + " newDefaultSubId = " + defaultSubscriptionId);
        boolean hasMobileData = DataUsageUtils.hasMobileData(this.mContext);
        if (this.mDefaultSubId == -1) {
            Log.d("DataUsageSummary", "onResume INVALID_SUBSCRIPTION_ID Mobile data false");
            hasMobileData = false;
        }
        if (hasMobileData && this.mDefaultSubId != defaultSubscriptionId && (templatePreferenceCategory = (TemplatePreferenceCategory) getPreferenceScreen().findPreference("mobile_category")) != null) {
            int order = templatePreferenceCategory.getOrder();
            getPreferenceScreen().removePreference(templatePreferenceCategory);
            Log.d("DataUsageSummary", "removePreferencedd and add (data_usage_cellular_screen) order = " + order);
            addMobileSection(defaultSubscriptionId, order);
        }
        if (TelephonyUtils.getMainCapabilityPhoneId() == 0) {
            this.mTempPhoneid = 1;
        } else {
            this.mTempPhoneid = 0;
        }
        updateScreenEnabled();
        updateState();
    }

    static CharSequence formatUsage(Context context, String str, long j) {
        return formatUsage(context, str, j, 1.5625f, 0.64f);
    }

    static CharSequence formatUsage(Context context, String str, long j, float f, float f2) {
        Formatter.BytesResult formatBytes = Formatter.formatBytes(context.getResources(), j, 10);
        SpannableString spannableString = new SpannableString(formatBytes.value);
        spannableString.setSpan(new RelativeSizeSpan(f), 0, spannableString.length(), 18);
        CharSequence expandTemplate = TextUtils.expandTemplate(new SpannableString(context.getString(17039893).replace("%1$s", "^1").replace("%2$s", "^2")), spannableString, formatBytes.units);
        SpannableString spannableString2 = new SpannableString(str);
        spannableString2.setSpan(new RelativeSizeSpan(f2), 0, spannableString2.length(), 18);
        return TextUtils.expandTemplate(spannableString2, BidiFormatter.getInstance().unicodeWrap(expandTemplate.toString()));
    }

    private void updateState() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        for (int i = 1; i < preferenceScreen.getPreferenceCount(); i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if ((preference instanceof PreferenceCategory) && ((PreferenceCategory) preference).getKey().equals("service_category")) {
                if (this.mEnableDataService != null) {
                    boolean dataService = getDataService();
                    this.mEnableDataService.setChecked(dataService);
                    Log.d("DataUsageSummary", "updateState, dataService=" + dataService);
                }
            } else if (preference instanceof TemplatePreferenceCategory) {
                ((TemplatePreferenceCategory) preference).pushTemplates(this.services);
            }
        }
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 37;
    }

    @Override // com.android.settings.datausage.DataUsageEditController
    public NetworkPolicyEditor getNetworkPolicyEditor() {
        return this.services.mPolicyEditor;
    }

    @Override // com.android.settings.datausage.DataUsageEditController
    public NetworkTemplate getNetworkTemplate() {
        Log.d("DataUsageSummary", "getNetworkTemplate without subID: DefaultTemplate");
        return this.mDefaultTemplate;
    }

    @Override // com.android.settings.datausage.DataUsageEditController
    public void updateDataUsage() {
        updateState();
        this.mSummaryController.updateState(this.mSummaryPreference);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Activity mActivity;
        private final DataUsageController mDataController;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            this.mActivity = activity;
            this.mSummaryLoader = summaryLoader;
            this.mDataController = new DataUsageController(activity);
        }

        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProvider
        public void setListening(boolean z) {
            if (z) {
                if (DataUsageUtils.hasSim(this.mActivity)) {
                    this.mSummaryLoader.setSummary(this, this.mActivity.getString(R.string.data_usage_summary_format, new Object[]{formatUsedData()}));
                    return;
                }
                DataUsageController.DataUsageInfo dataUsageInfo = this.mDataController.getDataUsageInfo(NetworkTemplate.buildTemplateWifiWildcard());
                if (dataUsageInfo == null) {
                    this.mSummaryLoader.setSummary(this, null);
                    return;
                }
                this.mSummaryLoader.setSummary(this, TextUtils.expandTemplate(this.mActivity.getText(R.string.data_usage_wifi_format), DataUsageUtils.formatDataUsage(this.mActivity, dataUsageInfo.usageLevel)));
            }
        }

        private CharSequence formatUsedData() {
            SubscriptionManager subscriptionManager = (SubscriptionManager) this.mActivity.getSystemService("telephony_subscription_service");
            int defaultSubscriptionId = SubscriptionManager.getDefaultSubscriptionId();
            if (defaultSubscriptionId == -1) {
                return formatFallbackData();
            }
            SubscriptionPlan primaryPlan = DataUsageSummaryPreferenceController.getPrimaryPlan(subscriptionManager, defaultSubscriptionId);
            if (primaryPlan == null) {
                return formatFallbackData();
            }
            if (DataUsageSummaryPreferenceController.unlimited(primaryPlan.getDataLimitBytes())) {
                return DataUsageUtils.formatDataUsage(this.mActivity, primaryPlan.getDataUsageBytes());
            }
            return Utils.formatPercentage(primaryPlan.getDataUsageBytes(), primaryPlan.getDataLimitBytes());
        }

        private CharSequence formatFallbackData() {
            DataUsageController.DataUsageInfo dataUsageInfo = this.mDataController.getDataUsageInfo();
            if (dataUsageInfo == null) {
                return DataUsageUtils.formatDataUsage(this.mActivity, 0L);
            }
            if (dataUsageInfo.limitLevel <= 0) {
                return DataUsageUtils.formatDataUsage(this.mActivity, dataUsageInfo.usageLevel);
            }
            return Utils.formatPercentage(dataUsageInfo.usageLevel, dataUsageInfo.limitLevel);
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem findItem = menu.findItem(R.id.data_usage_menu_cellular_data_control);
        try {
            Class<?> cls = Class.forName("com.mediatek.cta.CtaUtils", false, ClassLoader.getSystemClassLoader());
            Method declaredMethod = cls.getDeclaredMethod("isCtaSupported", new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(cls, new Object[0]);
            if (findItem != null) {
                findItem.setVisible(Boolean.valueOf(invoke.toString()).booleanValue());
            }
        } catch (Exception e) {
            if (findItem != null) {
                findItem.setVisible(false);
            }
            e.printStackTrace();
        }
    }

    private void addDataServiceSection(List<SubscriptionInfo> list) {
        if (!isDataServiceSupport()) {
            return;
        }
        Log.d("DataUsageSummary", "addDataServiceSection");
        if (list == null || list.size() != 2) {
            Log.d("DataUsageSummary", "subscriptions size != 2");
            return;
        }
        PreferenceCategory preferenceCategory = (PreferenceCategory) inflatePreferences(R.xml.data_service_cellular);
        this.mEnableDataService = (SwitchPreference) findPreference("data_service_enable");
        this.mEnableDataService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: com.android.settings.datausage.DataUsageSummary.2
            @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                Log.d("DataUsageSummary", "onPreferenceChange, preference=" + ((Object) preference.getTitle()));
                if (preference == DataUsageSummary.this.mEnableDataService) {
                    if (!DataUsageSummary.this.mEnableDataService.isChecked()) {
                        DataUsageSummary.this.showDataServiceDialog();
                        DataUsageSummary.this.mEnableDataService.setEnabled(false);
                        return false;
                    }
                    DataUsageSummary.this.setDataService(0);
                    return true;
                }
                return true;
            }
        });
        this.mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getContext());
        if (TelephonyUtils.getMainCapabilityPhoneId() == 0) {
            this.mTempPhoneid = 1;
        } else {
            this.mTempPhoneid = 0;
        }
        updateScreenEnabled();
        this.mEnableDataService.setChecked(getDataService());
        getContentResolver().registerContentObserver(Settings.Global.getUriFor("data_service_enabled"), true, this.mContentObserver);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        this.mDataUsageSummaryExt.customReceiver(intentFilter);
        ((TelephonyManager) getSystemService("phone")).listen(getPhoneStateListener(this.mTempPhoneid, SubscriptionManager.getSubId(this.mTempPhoneid)[0]), 32);
        getContext().registerReceiver(this.mReceiver, intentFilter);
    }

    private PhoneStateListener getPhoneStateListener(int i, int i2) {
        this.mPhoneStateListener = new PhoneStateListener(Integer.valueOf(i2)) { // from class: com.android.settings.datausage.DataUsageSummary.3
            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int i3, String str) {
                Log.d("DataUsageSummary", "onCallStateChanged state = " + i3);
                DataUsageSummary.this.updateScreenEnabled();
            }
        };
        return this.mPhoneStateListener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDataServiceDialog() {
        Log.d("DataUsageSummary", "showDataServiceDialog");
        startActivity(new Intent(getContext(), TempDataServiceDialogActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreenEnabled() {
        boolean isCapabilitySwitching = TelephonyUtils.isCapabilitySwitching();
        Log.d("DataUsageSummary", "updateScreenEnabled, mIsAirplaneModeOn = " + this.mIsAirplaneModeOn + ", isSwitching = " + isCapabilitySwitching + ", mTempPhoneid = " + this.mTempPhoneid);
        if (this.mEnableDataService != null) {
            this.mEnableDataService.setEnabled((this.mIsAirplaneModeOn || isCapabilitySwitching || this.mDataUsageSummaryExt.customTempdata(this.mTempPhoneid)) ? false : true);
            this.mDataUsageSummaryExt.customTempdataHide(this.mEnableDataService);
            return;
        }
        Log.d("DataUsageSummary", "mEnableDataService == null");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean getDataService() {
        int i;
        Context context = getContext();
        if (context != null) {
            i = Settings.Global.getInt(context.getContentResolver(), "data_service_enabled", 0);
        } else {
            i = 0;
        }
        Log.d("DataUsageSummary", "getDataService =" + i);
        return i != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDataService(int i) {
        Log.d("DataUsageSummary", "setDataService =" + i);
        Settings.Global.putInt(getContext().getContentResolver(), "data_service_enabled", i);
    }

    private static boolean isDataServiceSupport() {
        return "1".equals(SystemProperties.get("persist.vendor.radio.smart.data.switch"));
    }

    @Override // com.android.settings.datausage.DataUsageBaseFragment, com.android.settings.dashboard.DashboardFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        Log.d("DataUsageSummary", "onDestroy");
        super.onDestroy();
        if (!isDataServiceSupport()) {
            return;
        }
        if (this.mEnableDataService != null) {
            getContentResolver().unregisterContentObserver(this.mContentObserver);
            getContext().unregisterReceiver(this.mReceiver);
            this.mEnableDataService = null;
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService("phone");
        if (this.mPhoneStateListener != null) {
            telephonyManager.listen(this.mPhoneStateListener, 0);
        }
    }
}
