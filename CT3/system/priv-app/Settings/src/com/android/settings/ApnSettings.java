package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.android.settingslib.RestrictedLockUtils;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaApnSetting;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.ext.IApnSettingsExt;
import com.mediatek.settings.ext.IRcseOnlyApnExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.TelephonyUtils;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class ApnSettings extends RestrictedSettingsFragment implements Preference.OnPreferenceChangeListener {
    private static final Uri DEFAULTAPN_URI = Uri.parse("content://telephony/carriers/restore");
    private static final Uri PREFERAPN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static boolean mRestoreDefaultApnMode;
    private boolean mAllowAddingApns;
    private IApnSettingsExt mApnExt;
    private boolean mHideImsApn;
    private IntentFilter mMobileStateFilter;
    private final BroadcastReceiver mMobileStateReceiver;
    private String mMvnoMatchData;
    private String mMvnoType;
    private IRcseOnlyApnExt mRcseApnExt;
    private RestoreApnProcessHandler mRestoreApnProcessHandler;
    private RestoreApnUiHandler mRestoreApnUiHandler;
    private HandlerThread mRestoreDefaultApnThread;
    private String mSelectedKey;
    private SimHotSwapHandler mSimHotSwapHandler;
    private SubscriptionInfo mSubscriptionInfo;
    private UiccController mUiccController;
    private boolean mUnavailable;
    private UserManager mUserManager;

    public ApnSettings() {
        super("no_config_mobile_networks");
        this.mMobileStateReceiver = new BroadcastReceiver() { // from class: com.android.settings.ApnSettings.1

            /* renamed from: -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues  reason: not valid java name */
            private static final /* synthetic */ int[] f1xce0d2696 = null;

            /* renamed from: -getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues  reason: not valid java name */
            private static /* synthetic */ int[] m254x9dac0c3a() {
                if (f1xce0d2696 != null) {
                    return f1xce0d2696;
                }
                int[] iArr = new int[PhoneConstants.DataState.values().length];
                try {
                    iArr[PhoneConstants.DataState.CONNECTED.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[PhoneConstants.DataState.CONNECTING.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[PhoneConstants.DataState.DISCONNECTED.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[PhoneConstants.DataState.SUSPENDED.ordinal()] = 4;
                } catch (NoSuchFieldError e4) {
                }
                f1xce0d2696 = iArr;
                return iArr;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.ANY_DATA_STATE")) {
                    PhoneConstants.DataState state = ApnSettings.getMobileDataState(intent);
                    Log.d("ApnSettings", "onReceive ACTION_ANY_DATA_CONNECTION_STATE_CHANGED,state = " + state);
                    switch (m254x9dac0c3a()[state.ordinal()]) {
                        case 1:
                            if (!ApnSettings.mRestoreDefaultApnMode) {
                                ApnSettings.this.fillList();
                                break;
                            }
                            break;
                    }
                    ApnSettings.this.updateScreenForDataStateChange(context, intent);
                } else if (!"android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                } else {
                    ApnSettings.this.updateScreenEnableState(context);
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static PhoneConstants.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra("state");
        if (str != null) {
            return Enum.valueOf(PhoneConstants.DataState.class, str);
        }
        return PhoneConstants.DataState.DISCONNECTED;
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 12;
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Activity activity = getActivity();
        int subId = activity.getIntent().getIntExtra("sub_id", -1);
        this.mMobileStateFilter = new IntentFilter("android.intent.action.ANY_DATA_STATE");
        this.mMobileStateFilter.addAction("android.intent.action.AIRPLANE_MODE");
        setIfOnlyAvailableForAdmins(true);
        this.mSubscriptionInfo = SubscriptionManager.from(activity).getActiveSubscriptionInfo(subId);
        this.mUiccController = UiccController.getInstance();
        if (this.mSubscriptionInfo == null) {
            Log.d("ApnSettings", "onCreate()... Invalid subId: " + subId);
            getActivity().finish();
        }
        this.mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.android.settings.ApnSettings.2
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                Log.d("ApnSettings", "onSimHotSwap, finish activity");
                if (ApnSettings.this.getActivity() == null) {
                    return;
                }
                ApnSettings.this.getActivity().finish();
            }
        });
        this.mApnExt = UtilsExt.getApnSettingsPlugin(activity);
        this.mApnExt.initTetherField(this);
        this.mRcseApnExt = UtilsExt.getRcseApnPlugin(activity);
        this.mRcseApnExt.onCreate(new IRcseOnlyApnExt.OnRcseOnlyApnStateChangedListener() { // from class: com.android.settings.ApnSettings.3
            @Override // com.mediatek.settings.ext.IRcseOnlyApnExt.OnRcseOnlyApnStateChangedListener
            public void OnRcseOnlyApnStateChanged() {
                Log.d("ApnSettings", "OnRcseOnlyApnStateChanged()");
                ApnSettings.this.fillList();
            }
        }, subId);
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService("carrier_config");
        PersistableBundle b = configManager.getConfig();
        this.mHideImsApn = b.getBoolean("hide_ims_apn_bool");
        this.mAllowAddingApns = b.getBoolean("allow_adding_apns_bool");
        this.mUserManager = UserManager.get(activity);
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getEmptyTextView().setText(R.string.apn_settings_not_available);
        this.mUnavailable = isUiRestricted();
        setHasOptionsMenu(!this.mUnavailable);
        if (this.mUnavailable) {
            setPreferenceScreen(new PreferenceScreen(getPrefContext(), null));
            getPreferenceScreen().removeAll();
            return;
        }
        addPreferencesFromResource(R.xml.apn_settings);
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mUnavailable) {
            return;
        }
        getActivity().registerReceiver(this.mMobileStateReceiver, this.mMobileStateFilter);
        if (!mRestoreDefaultApnMode) {
            fillList();
            removeDialog(1001);
        }
        this.mApnExt.updateTetherState();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        if (this.mUnavailable) {
            return;
        }
        getActivity().unregisterReceiver(this.mMobileStateReceiver);
    }

    @Override // com.android.settings.RestrictedSettingsFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (this.mRestoreDefaultApnThread != null) {
            this.mRestoreDefaultApnThread.quit();
        }
        if (this.mSimHotSwapHandler != null) {
            this.mSimHotSwapHandler.unregisterOnSimHotSwap();
        }
        this.mApnExt.onDestroy();
        this.mRcseApnExt.onDestory();
    }

    @Override // com.android.settings.RestrictedSettingsFragment
    public RestrictedLockUtils.EnforcedAdmin getRestrictionEnforcedAdmin() {
        UserHandle user = UserHandle.of(this.mUserManager.getUserHandle());
        if (this.mUserManager.hasUserRestriction("no_config_mobile_networks", user) && !this.mUserManager.hasBaseUserRestriction("no_config_mobile_networks", user)) {
            return RestrictedLockUtils.EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public void fillList() {
        boolean selectable;
        TelephonyManager tm = (TelephonyManager) getSystemService("phone");
        String mccmnc = this.mSubscriptionInfo == null ? "" : tm.getSimOperator(this.mSubscriptionInfo.getSubscriptionId());
        Log.d("ApnSettings", "before plugin, mccmnc = " + mccmnc);
        String mccmnc2 = this.mApnExt.getOperatorNumericFromImpi(mccmnc, SubscriptionManager.getPhoneId(this.mSubscriptionInfo.getSubscriptionId()));
        Log.d("ApnSettings", "mccmnc = " + mccmnc2);
        String where = "numeric=\"" + mccmnc2 + "\" AND NOT (type='ia' AND (apn=\"\" OR apn IS NULL)) AND user_visible!=0";
        if (!FeatureOption.MTK_VOLTE_SUPPORT || this.mHideImsApn) {
            where = where + " AND NOT (type='ims' OR type='ia,ims')";
        }
        if (this.mSubscriptionInfo != null) {
            int subId = this.mSubscriptionInfo.getSubscriptionId();
            if (CdmaUtils.isSupportCdma(subId)) {
                where = CdmaApnSetting.customizeQuerySelectionforCdma(where, mccmnc2, subId);
            }
        }
        String where2 = this.mApnExt.getFillListQuery(where, mccmnc2);
        Log.d("ApnSettings", "fillList where: " + where2);
        String order = this.mApnExt.getApnSortOrder("name ASC");
        Log.d("ApnSettings", "fillList sort: " + order);
        Cursor cursor = getContentResolver().query(Telephony.Carriers.CONTENT_URI, new String[]{"_id", "name", "apn", "type", "mvno_type", "mvno_match_data", "sourcetype"}, where2, null, order);
        if (cursor == null) {
            return;
        }
        Log.d("ApnSettings", "fillList, cursor count: " + cursor.getCount());
        IccRecords r = null;
        if (this.mUiccController != null && this.mSubscriptionInfo != null) {
            r = this.mUiccController.getIccRecords(SubscriptionManager.getPhoneId(this.mSubscriptionInfo.getSubscriptionId()), 1);
        }
        PreferenceGroup apnList = (PreferenceGroup) findPreference("apn_list");
        apnList.removeAll();
        ArrayList<Preference> mnoApnList = new ArrayList<>();
        ArrayList<Preference> mvnoApnList = new ArrayList<>();
        ArrayList<Preference> mnoMmsApnList = new ArrayList<>();
        ArrayList<Preference> mvnoMmsApnList = new ArrayList<>();
        this.mSelectedKey = getSelectedApnKey();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(1);
            String apn = cursor.getString(2);
            String key = cursor.getString(0);
            String type = cursor.getString(3);
            String mvnoType = cursor.getString(4);
            String mvnoMatchData = cursor.getString(5);
            int sourcetype = cursor.getInt(6);
            if (shouldSkipApn(type)) {
                cursor.moveToNext();
            } else {
                String name2 = this.mApnExt.updateApnName(name, sourcetype);
                ApnPreference pref = new ApnPreference(getPrefContext());
                pref.setKey(key);
                pref.setTitle(name2);
                pref.setSummary(apn);
                pref.setPersistent(false);
                pref.setOnPreferenceChangeListener(this);
                pref.setApnEditable(this.mApnExt.isAllowEditPresetApn(type, apn, mccmnc2, sourcetype));
                pref.setSubId((this.mSubscriptionInfo == null ? null : Integer.valueOf(this.mSubscriptionInfo.getSubscriptionId())).intValue());
                if (type != null && (type.equals("mms") || type.equals("ia") || type.equals("ims") || type.equals("emergency"))) {
                    selectable = false;
                } else {
                    selectable = this.mApnExt.isSelectable(type);
                }
                pref.setSelectable(selectable);
                Log.d("ApnSettings", "mSelectedKey = " + this.mSelectedKey + " key = " + key + " name = " + name2 + " selectable=" + selectable);
                if (selectable) {
                    addApnToList(pref, mnoApnList, mvnoApnList, r, mvnoType, mvnoMatchData);
                    this.mApnExt.customizeUnselectableApn(type, mnoApnList, mvnoApnList, (this.mSubscriptionInfo == null ? null : Integer.valueOf(this.mSubscriptionInfo.getSubscriptionId())).intValue());
                } else {
                    addApnToList(pref, mnoMmsApnList, mvnoMmsApnList, r, mvnoType, mvnoMatchData);
                    this.mApnExt.customizeUnselectableApn(type, mnoMmsApnList, mvnoMmsApnList, (this.mSubscriptionInfo == null ? null : Integer.valueOf(this.mSubscriptionInfo.getSubscriptionId())).intValue());
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        if (!mvnoApnList.isEmpty()) {
            mnoApnList = mvnoApnList;
            mnoMmsApnList = mvnoMmsApnList;
        }
        for (Preference preference : mnoApnList) {
            apnList.addPreference(preference);
        }
        for (Preference preference2 : mnoMmsApnList) {
            apnList.addPreference(preference2);
        }
        setPreferApnChecked(mnoApnList);
        updateScreenEnableState(getActivity());
    }

    private void addApnToList(ApnPreference pref, ArrayList<Preference> mnoList, ArrayList<Preference> mvnoList, IccRecords r, String mvnoType, String mvnoMatchData) {
        Log.d("ApnSettings", "mvnoType = " + mvnoType + ", mvnoMatchData = " + mvnoMatchData);
        if (r != null && !TextUtils.isEmpty(mvnoType) && !TextUtils.isEmpty(mvnoMatchData)) {
            if (!ApnSetting.mvnoMatches(r, mvnoType, mvnoMatchData)) {
                return;
            }
            mvnoList.add(pref);
            this.mMvnoType = mvnoType;
            this.mMvnoMatchData = mvnoMatchData;
            Log.d("ApnSettings", "mvnoMatches...");
            return;
        }
        mnoList.add(pref);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!this.mUnavailable) {
            if (this.mAllowAddingApns) {
                menu.add(0, 1, 0, getResources().getString(R.string.menu_new)).setIcon(17301555).setShowAsAction(1);
            }
            menu.add(0, 2, 0, getResources().getString(R.string.menu_restore)).setIcon(17301589);
        }
        this.mApnExt.updateMenu(menu, 1, 2, TelephonyManager.getDefault().getSimOperator(this.mSubscriptionInfo != null ? this.mSubscriptionInfo.getSubscriptionId() : -1));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                addNewApn();
                return true;
            case 2:
                restoreDefaultApn();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewApn() {
        Log.d("ApnSettings", "addNewApn...");
        Intent intent = new Intent("android.intent.action.INSERT", Telephony.Carriers.CONTENT_URI);
        int subId = this.mSubscriptionInfo != null ? this.mSubscriptionInfo.getSubscriptionId() : -1;
        intent.putExtra("sub_id", subId);
        if (!TextUtils.isEmpty(this.mMvnoType) && !TextUtils.isEmpty(this.mMvnoMatchData)) {
            intent.putExtra("mvno_type", this.mMvnoType);
            intent.putExtra("mvno_match_data", this.mMvnoMatchData);
        }
        this.mApnExt.addApnTypeExtra(intent);
        startActivity(intent);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        int pos = Integer.parseInt(preference.getKey());
        Uri url = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, pos);
        Intent intent = new Intent("android.intent.action.EDIT", url);
        Log.d("ApnSettings", "put subid = " + this.mSubscriptionInfo.getSubscriptionId());
        intent.putExtra("sub_id", this.mSubscriptionInfo.getSubscriptionId());
        startActivity(intent);
        return true;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d("ApnSettings", "onPreferenceChange(): Preference - " + preference + ", newValue - " + newValue + ", newValue type - " + newValue.getClass());
        if (newValue instanceof String) {
            setSelectedApnKey((String) newValue);
            return true;
        }
        return true;
    }

    private void setSelectedApnKey(String key) {
        this.mSelectedKey = key;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", this.mSelectedKey);
        resolver.update(getPreferApnUri(this.mSubscriptionInfo.getSubscriptionId()), values, null, null);
    }

    private String getSelectedApnKey() {
        String key = null;
        int subId = this.mSubscriptionInfo.getSubscriptionId();
        Cursor cursor = getContentResolver().query(getPreferApnUri(subId), new String[]{"_id"}, null, null, "name ASC");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(0);
        }
        cursor.close();
        Log.d("ApnSettings", "getSelectedApnKey(), key = " + key);
        return key;
    }

    private boolean restoreDefaultApn() {
        Log.d("ApnSettings", "restoreDefaultApn...");
        showDialog(1001);
        mRestoreDefaultApnMode = true;
        if (this.mRestoreApnUiHandler == null) {
            this.mRestoreApnUiHandler = new RestoreApnUiHandler(this, null);
        }
        if (this.mRestoreApnProcessHandler == null || this.mRestoreDefaultApnThread == null) {
            this.mRestoreDefaultApnThread = new HandlerThread("Restore default APN Handler: Process Thread");
            this.mRestoreDefaultApnThread.start();
            this.mRestoreApnProcessHandler = new RestoreApnProcessHandler(this.mRestoreDefaultApnThread.getLooper(), this.mRestoreApnUiHandler);
        }
        this.mRestoreApnProcessHandler.sendEmptyMessage(1);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class RestoreApnUiHandler extends Handler {
        /* synthetic */ RestoreApnUiHandler(ApnSettings this$0, RestoreApnUiHandler restoreApnUiHandler) {
            this();
        }

        private RestoreApnUiHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    Log.d("ApnSettings", "restore APN complete~~");
                    Activity activity = ApnSettings.this.getActivity();
                    if (activity == null) {
                        boolean unused = ApnSettings.mRestoreDefaultApnMode = false;
                        return;
                    }
                    ApnSettings.this.fillList();
                    ApnSettings.this.updateScreenEnableState(activity);
                    boolean unused2 = ApnSettings.mRestoreDefaultApnMode = false;
                    ApnSettings.this.removeDialog(1001);
                    Toast.makeText(activity, ApnSettings.this.getResources().getString(R.string.restore_default_apn_completed), 1).show();
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class RestoreApnProcessHandler extends Handler {
        private Handler mRestoreApnUiHandler;

        public RestoreApnProcessHandler(Looper looper, Handler restoreApnUiHandler) {
            super(looper);
            this.mRestoreApnUiHandler = restoreApnUiHandler;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d("ApnSettings", "restore APN start~~");
                    ContentResolver resolver = ApnSettings.this.getContentResolver();
                    resolver.delete(ApnSettings.this.getDefaultApnUri(ApnSettings.this.mSubscriptionInfo.getSubscriptionId()), null, null);
                    this.mRestoreApnUiHandler.sendEmptyMessage(2);
                    return;
                default:
                    return;
            }
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int id) {
        if (id == 1001) {
            ProgressDialog dialog = new ProgressDialog(getActivity()) { // from class: com.android.settings.ApnSettings.4
                @Override // android.app.Dialog
                public boolean onTouchEvent(MotionEvent event) {
                    return true;
                }
            };
            dialog.setMessage(getResources().getString(R.string.restore_default_apn));
            dialog.setCancelable(false);
            return dialog;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreenForDataStateChange(Context context, Intent intent) {
        boolean z;
        String apnType = intent.getStringExtra("apnType");
        Log.d("ApnSettings", "Receiver,send MMS status, get type = " + apnType);
        if (!"mms".equals(apnType)) {
            return;
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (isMmsInTransaction(context)) {
            z = false;
        } else {
            z = this.mApnExt.getScreenEnableState(this.mSubscriptionInfo.getSubscriptionId(), getActivity());
        }
        preferenceScreen.setEnabled(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreenEnableState(Context context) {
        int subId = this.mSubscriptionInfo.getSubscriptionId();
        boolean simReady = 5 == TelephonyManager.getDefault().getSimState(SubscriptionManager.getSlotId(subId));
        boolean airplaneModeEnabled = Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", -1) == 1;
        boolean z = !airplaneModeEnabled ? simReady : false;
        Log.d("ApnSettings", "updateScreenEnableState(), subId = " + subId + " ,airplaneModeEnabled = " + airplaneModeEnabled + " ,simReady = " + simReady);
        getPreferenceScreen().setEnabled(z ? this.mApnExt.getScreenEnableState(subId, getActivity()) : false);
        if (getActivity() == null) {
            return;
        }
        getActivity().invalidateOptionsMenu();
    }

    private boolean isMmsInTransaction(Context context) {
        NetworkInfo networkInfo;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null || (networkInfo = cm.getNetworkInfo(2)) == null) {
            return false;
        }
        NetworkInfo.State state = networkInfo.getState();
        Log.d("ApnSettings", "mms state = " + state);
        return state == NetworkInfo.State.CONNECTING || state == NetworkInfo.State.CONNECTED;
    }

    public boolean shouldSkipApn(String type) {
        return "cmmail".equals(type) || !this.mRcseApnExt.isRcseOnlyApnEnabled(type);
    }

    @Override // android.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        int size = menu.size();
        boolean isAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getActivity());
        Log.d("ApnSettings", "onPrepareOptionsMenu isAirplaneModeOn = " + isAirplaneModeOn);
        for (int i = 0; i < size; i++) {
            menu.getItem(i).setEnabled(!isAirplaneModeOn);
        }
        super.onPrepareOptionsMenu(menu);
    }

    private Uri getPreferApnUri(int subId) {
        Uri preferredUri = Uri.withAppendedPath(Uri.parse("content://telephony/carriers/preferapn"), "/subId/" + subId);
        Log.d("ApnSettings", "getPreferredApnUri: " + preferredUri);
        return this.mApnExt.getPreferCarrierUri(preferredUri, subId);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Uri getDefaultApnUri(int subId) {
        return Uri.withAppendedPath(DEFAULTAPN_URI, "/subId/" + subId);
    }

    private void setPreferApnChecked(ArrayList<Preference> apnList) {
        if (apnList == null || apnList.isEmpty()) {
            return;
        }
        String selectedKey = null;
        if (this.mSelectedKey != null) {
            for (Preference pref : apnList) {
                if (this.mSelectedKey.equals(pref.getKey())) {
                    ((ApnPreference) pref).setChecked();
                    selectedKey = this.mSelectedKey;
                }
            }
        }
        if (selectedKey == null && apnList.get(0) != null) {
            ((ApnPreference) apnList.get(0)).setChecked();
            selectedKey = apnList.get(0).getKey();
        }
        if (selectedKey != null && selectedKey != this.mSelectedKey) {
            setSelectedApnKey(selectedKey);
            this.mSelectedKey = selectedKey;
        }
        Log.d("ApnSettings", "setPreferApnChecked, APN = " + this.mSelectedKey);
    }
}
