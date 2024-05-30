package com.android.settings.network;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.telephony.CarrierConfigManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.utils.ThreadUtils;
import com.mediatek.internal.telephony.MtkPhoneConstants;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaApnSetting;
import com.mediatek.settings.ext.IApnSettingsExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/* loaded from: classes.dex */
public class ApnEditor extends SettingsPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener, View.OnKeyListener {
    static final int APN_INDEX = 2;
    static final int CARRIER_ENABLED_INDEX = 17;
    static final int MCC_INDEX = 9;
    static final int MNC_INDEX = 10;
    static final int NAME_INDEX = 1;
    static String sNotSet;
    EditTextPreference mApn;
    ApnData mApnData;
    private IApnSettingsExt mApnExt;
    EditTextPreference mApnType;
    ListPreference mAuthType;
    MultiSelectListPreference mBearerMulti;
    SwitchPreference mCarrierEnabled;
    private Uri mCarrierUri;
    private String mCurMcc;
    private String mCurMnc;
    EditTextPreference mMcc;
    EditTextPreference mMmsPort;
    EditTextPreference mMmsProxy;
    EditTextPreference mMmsc;
    EditTextPreference mMnc;
    EditTextPreference mMvnoMatchData;
    private String mMvnoMatchDataStr;
    ListPreference mMvnoType;
    private String mMvnoTypeStr;
    EditTextPreference mName;
    private boolean mNewApn;
    EditTextPreference mPassword;
    EditTextPreference mPort;
    ListPreference mProtocol;
    EditTextPreference mProxy;
    private boolean mReadOnlyApn;
    private String[] mReadOnlyApnFields;
    private String[] mReadOnlyApnTypes;
    ListPreference mRoamingProtocol;
    EditTextPreference mServer;
    private SimHotSwapHandler mSimHotSwapHandler;
    private int mSubId;
    private TelephonyManager mTelephonyManager;
    EditTextPreference mUser;
    private static final String TAG = ApnEditor.class.getSimpleName();
    private static String[] sProjection = {"_id", "name", "apn", "proxy", "port", "user", "server", "password", "mmsc", "mcc", "mnc", "numeric", "mmsproxy", "mmsport", "authtype", "type", "protocol", "carrier_enabled", "bearer", "bearer_bitmask", "roaming_protocol", "mvno_type", "mvno_match_data", "edited", "user_editable", "sourcetype"};
    private int mBearerInitialVal = 0;
    private int mSourceType = 0;
    private boolean mReadOnlyMode = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.network.ApnEditor.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                if (intent.getBooleanExtra("state", false)) {
                    Log.d(ApnEditor.TAG, "receiver: ACTION_AIRPLANE_MODE_CHANGED in ApnEditor");
                    ApnEditor.this.exitWithoutSave();
                }
            } else if (!action.equals("android.intent.action.ANY_DATA_STATE")) {
                if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    Log.d(ApnEditor.TAG, "receiver: ACTION_SIM_STATE_CHANGED");
                    ApnEditor.this.updateScreenEnableState();
                }
            } else {
                String stringExtra = intent.getStringExtra("apnType");
                String str = ApnEditor.TAG;
                Log.d(str, "Receiver,send MMS status, get type = " + stringExtra);
                if ("mms".equals(stringExtra)) {
                    ApnEditor.this.updateScreenEnableState();
                }
            }
        }
    };

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        PersistableBundle config;
        String[] strArr;
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.apn_editor);
        sNotSet = getResources().getString(R.string.apn_not_set);
        this.mName = (EditTextPreference) findPreference("apn_name");
        this.mApn = (EditTextPreference) findPreference("apn_apn");
        this.mProxy = (EditTextPreference) findPreference("apn_http_proxy");
        this.mPort = (EditTextPreference) findPreference("apn_http_port");
        this.mUser = (EditTextPreference) findPreference("apn_user");
        this.mServer = (EditTextPreference) findPreference("apn_server");
        this.mPassword = (EditTextPreference) findPreference("apn_password");
        this.mMmsProxy = (EditTextPreference) findPreference("apn_mms_proxy");
        this.mMmsPort = (EditTextPreference) findPreference("apn_mms_port");
        this.mMmsc = (EditTextPreference) findPreference("apn_mmsc");
        this.mMcc = (EditTextPreference) findPreference("apn_mcc");
        this.mMnc = (EditTextPreference) findPreference("apn_mnc");
        this.mApnType = (EditTextPreference) findPreference("apn_type");
        this.mAuthType = (ListPreference) findPreference("auth_type");
        this.mAuthType.setOnPreferenceChangeListener(this);
        this.mProtocol = (ListPreference) findPreference("apn_protocol");
        this.mProtocol.setOnPreferenceChangeListener(this);
        this.mRoamingProtocol = (ListPreference) findPreference("apn_roaming_protocol");
        this.mRoamingProtocol.setOnPreferenceChangeListener(this);
        this.mCarrierEnabled = (SwitchPreference) findPreference("carrier_enabled");
        this.mBearerMulti = (MultiSelectListPreference) findPreference("bearer_multi");
        this.mBearerMulti.setOnPreferenceChangeListener(this);
        this.mBearerMulti.setPositiveButtonText(17039370);
        this.mBearerMulti.setNegativeButtonText(17039360);
        this.mMvnoType = (ListPreference) findPreference("mvno_type");
        this.mMvnoType.setOnPreferenceChangeListener(this);
        this.mMvnoMatchData = (EditTextPreference) findPreference("mvno_match_data");
        Intent intent = getIntent();
        String action = intent.getAction();
        this.mSubId = intent.getIntExtra("sub_id", -1);
        this.mReadOnlyApn = false;
        Uri uri = null;
        this.mReadOnlyApnTypes = null;
        this.mReadOnlyApnFields = null;
        this.mApnExt = UtilsExt.getApnSettingsExt(getContext());
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) getSystemService("carrier_config");
        if (carrierConfigManager != null && (config = carrierConfigManager.getConfig()) != null) {
            this.mReadOnlyApnTypes = config.getStringArray("read_only_apn_types_string_array");
            if (!ArrayUtils.isEmpty(this.mReadOnlyApnTypes)) {
                for (String str : this.mReadOnlyApnTypes) {
                    Log.d(TAG, "onCreate: read only APN type: " + str);
                }
            }
            this.mReadOnlyApnFields = config.getStringArray("read_only_apn_fields_string_array");
        }
        if (action.equals("android.intent.action.EDIT")) {
            uri = intent.getData();
            if (uri.isPathPrefixMatch(Telephony.Carriers.CONTENT_URI)) {
                this.mReadOnlyMode = intent.getBooleanExtra("readOnly", false);
                Log.d(TAG, "Read only mode : " + this.mReadOnlyMode);
            } else {
                Log.e(TAG, "Edit request not for carrier table. Uri: " + uri);
                finish();
                return;
            }
        } else if (action.equals("android.intent.action.INSERT")) {
            this.mCarrierUri = intent.getData();
            if (!this.mCarrierUri.isPathPrefixMatch(Telephony.Carriers.CONTENT_URI)) {
                Log.e(TAG, "Insert request not for carrier table. Uri: " + this.mCarrierUri);
                finish();
                return;
            }
            this.mNewApn = true;
            this.mMvnoTypeStr = intent.getStringExtra("mvno_type");
            this.mMvnoMatchDataStr = intent.getStringExtra("mvno_match_data");
            Log.d(TAG, "mvnoType = " + this.mMvnoTypeStr + ", mvnoMatchData =" + this.mMvnoMatchDataStr);
        } else {
            finish();
            return;
        }
        sProjection = this.mApnExt.customizeApnProjection(sProjection);
        this.mApnExt.customizePreference(this.mSubId, getPreferenceScreen());
        if (uri != null) {
            this.mApnData = getApnDataFromUri(uri);
            Log.d(TAG, "uri = " + uri);
        } else {
            this.mApnData = new ApnData(sProjection.length);
            Log.d(TAG, "sProjection.length = " + sProjection.length);
        }
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        boolean z = this.mApnData.getInteger(23, 1).intValue() == 1;
        Log.d(TAG, "onCreate: EDITED " + z);
        if (!z && (this.mApnData.getInteger(24, 1).intValue() == 0 || apnTypesMatch(this.mReadOnlyApnTypes, this.mApnData.getString(15)))) {
            Log.d(TAG, "onCreate: apnTypesMatch; read-only APN");
            this.mReadOnlyApn = true;
            disableAllFields();
        } else if (!ArrayUtils.isEmpty(this.mReadOnlyApnFields)) {
            disableFields(this.mReadOnlyApnFields);
        }
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(this);
        }
        fillUI(bundle == null);
        this.mSimHotSwapHandler = new SimHotSwapHandler(getContext());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.android.settings.network.ApnEditor.1
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                Log.d(ApnEditor.TAG, "onSimHotSwap, finish Activity~~");
                ApnEditor.this.finish();
            }
        });
        IntentFilter intentFilter = new IntentFilter("android.intent.action.ANY_DATA_STATE");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        getContext().registerReceiver(this.mReceiver, intentFilter);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    static String formatInteger(String str) {
        try {
            return String.format("%d", Integer.valueOf(Integer.parseInt(str)));
        } catch (NumberFormatException e) {
            return str;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean hasAllApns(String[] strArr) {
        if (ArrayUtils.isEmpty(strArr)) {
            return false;
        }
        List asList = Arrays.asList(strArr);
        if (asList.contains("*")) {
            Log.d(TAG, "hasAllApns: true because apnList.contains(PhoneConstants.APN_TYPE_ALL)");
            return true;
        }
        for (String str : PhoneConstants.APN_TYPES) {
            if (!asList.contains(str)) {
                return false;
            }
        }
        Log.d(TAG, "hasAllApns: true");
        return true;
    }

    private boolean apnTypesMatch(String[] strArr, String str) {
        if (ArrayUtils.isEmpty(strArr)) {
            return false;
        }
        if (hasAllApns(strArr) || TextUtils.isEmpty(str)) {
            return true;
        }
        List asList = Arrays.asList(strArr);
        for (String str2 : str.split(",")) {
            if (asList.contains(str2.trim())) {
                Log.d(TAG, "apnTypesMatch: true because match found for " + str2.trim());
                return true;
            }
        }
        Log.d(TAG, "apnTypesMatch: false");
        return false;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private Preference getPreferenceFromFieldName(String str) {
        char c;
        switch (str.hashCode()) {
            case -2135515857:
                if (str.equals("mvno_type")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -1954254981:
                if (str.equals("mmsproxy")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1640523526:
                if (str.equals("carrier_enabled")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1393032351:
                if (str.equals("bearer")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1230508389:
                if (str.equals("bearer_bitmask")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -1039601666:
                if (str.equals("roaming_protocol")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -989163880:
                if (str.equals("protocol")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -905826493:
                if (str.equals("server")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -520149991:
                if (str.equals("mvno_match_data")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 96799:
                if (str.equals("apn")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 107917:
                if (str.equals("mcc")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 108258:
                if (str.equals("mnc")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 3355632:
                if (str.equals("mmsc")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 3373707:
                if (str.equals("name")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3446913:
                if (str.equals("port")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 3575610:
                if (str.equals("type")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 3599307:
                if (str.equals("user")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 106941038:
                if (str.equals("proxy")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1183882708:
                if (str.equals("mmsport")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1216985755:
                if (str.equals("password")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1433229538:
                if (str.equals("authtype")) {
                    c = '\r';
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
                return this.mName;
            case 1:
                return this.mApn;
            case 2:
                return this.mProxy;
            case 3:
                return this.mPort;
            case 4:
                return this.mUser;
            case 5:
                return this.mServer;
            case 6:
                return this.mPassword;
            case 7:
                return this.mMmsProxy;
            case '\b':
                return this.mMmsPort;
            case '\t':
                return this.mMmsc;
            case '\n':
                return this.mMcc;
            case 11:
                return this.mMnc;
            case '\f':
                return this.mApnType;
            case '\r':
                return this.mAuthType;
            case 14:
                return this.mProtocol;
            case 15:
                return this.mRoamingProtocol;
            case 16:
                return this.mCarrierEnabled;
            case 17:
            case 18:
                return this.mBearerMulti;
            case 19:
                return this.mMvnoType;
            case 20:
                return this.mMvnoMatchData;
            default:
                return null;
        }
    }

    private void disableFields(String[] strArr) {
        for (String str : strArr) {
            Preference preferenceFromFieldName = getPreferenceFromFieldName(str);
            if (preferenceFromFieldName != null) {
                preferenceFromFieldName.setEnabled(false);
            }
        }
    }

    private void disableAllFields() {
        this.mName.setEnabled(false);
        this.mApn.setEnabled(false);
        this.mProxy.setEnabled(false);
        this.mPort.setEnabled(false);
        this.mUser.setEnabled(false);
        this.mServer.setEnabled(false);
        this.mPassword.setEnabled(false);
        this.mMmsProxy.setEnabled(false);
        this.mMmsPort.setEnabled(false);
        this.mMmsc.setEnabled(false);
        this.mMcc.setEnabled(false);
        this.mMnc.setEnabled(false);
        this.mApnType.setEnabled(false);
        this.mAuthType.setEnabled(false);
        this.mProtocol.setEnabled(false);
        this.mRoamingProtocol.setEnabled(false);
        this.mCarrierEnabled.setEnabled(false);
        this.mBearerMulti.setEnabled(false);
        this.mMvnoType.setEnabled(false);
        this.mMvnoMatchData.setEnabled(false);
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 13;
    }

    void fillUI(boolean z) {
        Log.d(TAG, "fillUi... firstTime = " + z);
        if (z) {
            this.mName.setText(this.mApnData.getString(1));
            this.mApn.setText(this.mApnData.getString(2));
            this.mProxy.setText(this.mApnData.getString(3));
            this.mPort.setText(this.mApnData.getString(4));
            this.mUser.setText(this.mApnData.getString(5));
            this.mServer.setText(this.mApnData.getString(6));
            this.mPassword.setText(this.mApnData.getString(7));
            this.mMmsProxy.setText(this.mApnData.getString(12));
            this.mMmsPort.setText(this.mApnData.getString(13));
            this.mMmsc.setText(this.mApnData.getString(8));
            this.mMcc.setText(this.mApnData.getString(9));
            this.mMnc.setText(this.mApnData.getString(10));
            this.mApnType.setText(this.mApnData.getString(15));
            if (this.mNewApn) {
                String simOperator = this.mTelephonyManager.getSimOperator(this.mSubId);
                Log.d(TAG, " fillUi, numeric = " + simOperator);
                String updateMccMncForCdma = CdmaApnSetting.updateMccMncForCdma(simOperator, this.mSubId);
                if (updateMccMncForCdma != null && updateMccMncForCdma.length() > 4) {
                    String substring = updateMccMncForCdma.substring(0, 3);
                    String substring2 = updateMccMncForCdma.substring(3);
                    this.mMcc.setText(substring);
                    this.mMnc.setText(substring2);
                    this.mCurMnc = substring2;
                    this.mCurMcc = substring;
                }
                this.mSourceType = 1;
            } else {
                this.mSourceType = this.mApnData.getInteger(25).intValue();
            }
            int intValue = this.mApnData.getInteger(14, -1).intValue();
            if (intValue != -1) {
                this.mAuthType.setValueIndex(intValue);
            } else {
                this.mAuthType.setValue(null);
            }
            this.mProtocol.setValue(this.mApnData.getString(16));
            this.mRoamingProtocol.setValue(this.mApnData.getString(20));
            this.mCarrierEnabled.setChecked(this.mApnData.getInteger(17, 1).intValue() == 1);
            this.mBearerInitialVal = this.mApnData.getInteger(18, 0).intValue();
            HashSet hashSet = new HashSet();
            int intValue2 = this.mApnData.getInteger(19, 0).intValue();
            if (intValue2 == 0) {
                if (this.mBearerInitialVal == 0) {
                    hashSet.add("0");
                }
            } else {
                int i = 1;
                while (intValue2 != 0) {
                    if ((intValue2 & 1) == 1) {
                        hashSet.add("" + i);
                    }
                    intValue2 >>= 1;
                    i++;
                }
            }
            if (this.mBearerInitialVal != 0) {
                if (!hashSet.contains("" + this.mBearerInitialVal)) {
                    hashSet.add("" + this.mBearerInitialVal);
                }
            }
            this.mBearerMulti.setValues(hashSet);
            this.mMvnoType.setValue(this.mApnData.getString(21));
            this.mMvnoMatchData.setEnabled(false);
            this.mMvnoMatchData.setText(this.mApnData.getString(22));
            if (this.mNewApn && this.mMvnoTypeStr != null && this.mMvnoMatchDataStr != null) {
                this.mMvnoType.setValue(this.mMvnoTypeStr);
                this.mMvnoMatchData.setText(this.mMvnoMatchDataStr);
            }
        }
        this.mName.setSummary(checkNull(this.mName.getText()));
        this.mApn.setSummary(checkNull(this.mApn.getText()));
        this.mProxy.setSummary(checkNull(this.mProxy.getText()));
        this.mPort.setSummary(checkNull(this.mPort.getText()));
        this.mUser.setSummary(checkNull(this.mUser.getText()));
        this.mServer.setSummary(checkNull(this.mServer.getText()));
        this.mPassword.setSummary(starify(this.mPassword.getText()));
        this.mMmsProxy.setSummary(checkNull(this.mMmsProxy.getText()));
        this.mMmsPort.setSummary(checkNull(this.mMmsPort.getText()));
        this.mMmsc.setSummary(checkNull(this.mMmsc.getText()));
        this.mMcc.setSummary(formatInteger(checkNull(this.mMcc.getText())));
        this.mMnc.setSummary(formatInteger(checkNull(this.mMnc.getText())));
        this.mApnType.setSummary(checkNull(this.mApnType.getText()));
        String value = this.mAuthType.getValue();
        if (value != null) {
            int parseInt = Integer.parseInt(value);
            this.mAuthType.setValueIndex(parseInt);
            this.mAuthType.setSummary(getResources().getStringArray(R.array.apn_auth_entries)[parseInt]);
        } else {
            this.mAuthType.setSummary(sNotSet);
        }
        this.mProtocol.setSummary(checkNull(protocolDescription(this.mProtocol.getValue(), this.mProtocol)));
        this.mRoamingProtocol.setSummary(checkNull(protocolDescription(this.mRoamingProtocol.getValue(), this.mRoamingProtocol)));
        this.mBearerMulti.setSummary(checkNull(bearerMultiDescription(this.mBearerMulti.getValues())));
        this.mMvnoType.setSummary(checkNull(mvnoDescription(this.mMvnoType.getValue())));
        this.mMvnoMatchData.setSummary(checkNull(this.mMvnoMatchData.getText()));
        if (!getResources().getBoolean(R.bool.config_allow_edit_carrier_enabled)) {
            this.mCarrierEnabled.setEnabled(false);
        } else {
            this.mCarrierEnabled.setEnabled(true);
        }
    }

    private String protocolDescription(String str, ListPreference listPreference) {
        int findIndexOfValue = listPreference.findIndexOfValue(str);
        if (findIndexOfValue == -1) {
            return null;
        }
        try {
            return getResources().getStringArray(R.array.apn_protocol_entries)[findIndexOfValue];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private String bearerMultiDescription(Set<String> set) {
        String[] stringArray = getResources().getStringArray(R.array.bearer_entries);
        StringBuilder sb = new StringBuilder();
        boolean z = true;
        for (String str : set) {
            int findIndexOfValue = this.mBearerMulti.findIndexOfValue(str);
            if (z) {
                try {
                    sb.append(stringArray[findIndexOfValue]);
                    z = false;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            } else {
                sb.append(", " + stringArray[findIndexOfValue]);
            }
        }
        String sb2 = sb.toString();
        if (!TextUtils.isEmpty(sb2)) {
            return sb2;
        }
        return null;
    }

    private String mvnoDescription(String str) {
        int findIndexOfValue = this.mMvnoType.findIndexOfValue(str);
        String value = this.mMvnoType.getValue();
        if (findIndexOfValue == -1) {
            return null;
        }
        String[] stringArray = getResources().getStringArray(R.array.ext_mvno_type_entries);
        boolean z = true;
        boolean z2 = this.mReadOnlyApn || (this.mReadOnlyApnFields != null && Arrays.asList(this.mReadOnlyApnFields).contains("mvno_match_data"));
        EditTextPreference editTextPreference = this.mMvnoMatchData;
        if (z2 || findIndexOfValue == 0) {
            z = false;
        }
        editTextPreference.setEnabled(z);
        if (str != null && !str.equals(value)) {
            if (stringArray[findIndexOfValue].equals("SPN")) {
                this.mMvnoMatchData.setText(this.mTelephonyManager.getSimOperatorName());
            } else if (stringArray[findIndexOfValue].equals("IMSI")) {
                String simOperator = this.mTelephonyManager.getSimOperator(this.mSubId);
                this.mMvnoMatchData.setText(simOperator + "x");
            } else if (stringArray[findIndexOfValue].equals("GID")) {
                this.mMvnoMatchData.setText(this.mTelephonyManager.getGroupIdLevel1());
            }
        }
        try {
            return stringArray[findIndexOfValue];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object obj) {
        String key = preference.getKey();
        if ("auth_type".equals(key)) {
            try {
                int parseInt = Integer.parseInt((String) obj);
                this.mAuthType.setValueIndex(parseInt);
                this.mAuthType.setSummary(getResources().getStringArray(R.array.apn_auth_entries)[parseInt]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if ("apn_protocol".equals(key)) {
            String str = (String) obj;
            String protocolDescription = protocolDescription(str, this.mProtocol);
            if (protocolDescription == null) {
                return false;
            }
            this.mProtocol.setSummary(protocolDescription);
            this.mProtocol.setValue(str);
            return true;
        } else if ("apn_roaming_protocol".equals(key)) {
            String str2 = (String) obj;
            String protocolDescription2 = protocolDescription(str2, this.mRoamingProtocol);
            if (protocolDescription2 == null) {
                return false;
            }
            this.mRoamingProtocol.setSummary(protocolDescription2);
            this.mRoamingProtocol.setValue(str2);
            return true;
        } else if ("bearer_multi".equals(key)) {
            Set<String> set = (Set) obj;
            String bearerMultiDescription = bearerMultiDescription(set);
            if (bearerMultiDescription == null) {
                return false;
            }
            this.mBearerMulti.setValues(set);
            this.mBearerMulti.setSummary(bearerMultiDescription);
            return true;
        } else if ("mvno_type".equals(key)) {
            String str3 = (String) obj;
            String mvnoDescription = mvnoDescription(str3);
            if (mvnoDescription == null) {
                return false;
            }
            this.mMvnoType.setValue(str3);
            this.mMvnoType.setSummary(mvnoDescription);
            return true;
        } else if (!preference.equals(this.mPassword)) {
            if (preference.equals(this.mCarrierEnabled) || preference.equals(this.mBearerMulti)) {
                return true;
            }
            preference.setSummary(checkNull(obj != null ? String.valueOf(obj) : null));
            return true;
        } else {
            preference.setSummary(starify(obj != null ? String.valueOf(obj) : ""));
            return true;
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        String str = TAG;
        Log.d(str, "onCreateOptionsMenu mReadOnlyMode = " + this.mReadOnlyMode);
        if (this.mReadOnlyMode) {
            return;
        }
        if (!this.mNewApn && !this.mReadOnlyApn && this.mSourceType != 0) {
            menu.add(0, 1, 0, R.string.menu_delete).setIcon(R.drawable.ic_delete);
        }
        menu.add(0, 2, 0, R.string.menu_save).setIcon(17301582);
        menu.add(0, 3, 0, R.string.menu_cancel).setIcon(17301560);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 1:
                deleteApn();
                finish();
                return true;
            case 2:
                if (this.mSourceType == 0) {
                    showDialog(1);
                } else if (validateAndSaveApnData()) {
                    finish();
                }
                return true;
            case 3:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == 0 && i == 4) {
            if (validateAndSaveApnData()) {
                finish();
                return true;
            }
            return true;
        }
        return false;
    }

    boolean setStringValueAndCheckIfDiff(ContentValues contentValues, String str, String str2, boolean z, int i) {
        String string = this.mApnData.getString(i);
        boolean z2 = z || (!(TextUtils.isEmpty(str2) && TextUtils.isEmpty(string)) && (str2 == null || !str2.equals(string)));
        if (z2 && str2 != null) {
            contentValues.put(str, str2);
        }
        return z2;
    }

    boolean setIntValueAndCheckIfDiff(ContentValues contentValues, String str, int i, boolean z, int i2) {
        boolean z2 = z || i != this.mApnData.getInteger(i2).intValue();
        if (z2) {
            contentValues.put(str, Integer.valueOf(i));
        }
        return z2;
    }

    boolean validateAndSaveApnData() {
        int i;
        int i2;
        Log.d(TAG, "validateAndSave...");
        if (this.mReadOnlyApn) {
            return true;
        }
        String checkNotSet = checkNotSet(this.mName.getText());
        String checkNotSet2 = checkNotSet(this.mApn.getText());
        String checkNotSet3 = checkNotSet(this.mMcc.getText());
        String checkNotSet4 = checkNotSet(this.mMnc.getText());
        if (validateApnData() != null) {
            showError();
            return false;
        }
        ContentValues contentValues = new ContentValues();
        boolean stringValueAndCheckIfDiff = setStringValueAndCheckIfDiff(contentValues, "mmsc", checkNotSet(this.mMmsc.getText()), setStringValueAndCheckIfDiff(contentValues, "password", checkNotSet(this.mPassword.getText()), setStringValueAndCheckIfDiff(contentValues, "server", checkNotSet(this.mServer.getText()), setStringValueAndCheckIfDiff(contentValues, "user", checkNotSet(this.mUser.getText()), setStringValueAndCheckIfDiff(contentValues, "mmsport", checkNotSet(this.mMmsPort.getText()), setStringValueAndCheckIfDiff(contentValues, "mmsproxy", checkNotSet(this.mMmsProxy.getText()), setStringValueAndCheckIfDiff(contentValues, "port", checkNotSet(this.mPort.getText()), setStringValueAndCheckIfDiff(contentValues, "proxy", checkNotSet(this.mProxy.getText()), setStringValueAndCheckIfDiff(contentValues, "apn", checkNotSet2, setStringValueAndCheckIfDiff(contentValues, "name", checkNotSet, this.mNewApn, 1), 2), 3), 4), 12), 13), 5), 6), 7), 8);
        String value = this.mAuthType.getValue();
        if (value != null) {
            stringValueAndCheckIfDiff = setIntValueAndCheckIfDiff(contentValues, "authtype", Integer.parseInt(value), stringValueAndCheckIfDiff, 14);
        }
        boolean stringValueAndCheckIfDiff2 = setStringValueAndCheckIfDiff(contentValues, "mnc", checkNotSet4, setStringValueAndCheckIfDiff(contentValues, "mcc", checkNotSet3, setStringValueAndCheckIfDiff(contentValues, "type", checkNotSet(getUserEnteredApnType()), setStringValueAndCheckIfDiff(contentValues, "roaming_protocol", checkNotSet(this.mRoamingProtocol.getValue()), setStringValueAndCheckIfDiff(contentValues, "protocol", checkNotSet(this.mProtocol.getValue()), stringValueAndCheckIfDiff, 16), 20), 15), 9), 10);
        contentValues.put("numeric", checkNotSet3 + checkNotSet4);
        if (this.mCurMnc != null && this.mCurMcc != null && this.mCurMnc.equals(checkNotSet4) && this.mCurMcc.equals(checkNotSet3)) {
            contentValues.put("current", (Integer) 1);
        }
        Iterator<String> it = this.mBearerMulti.getValues().iterator();
        int i3 = 0;
        while (true) {
            if (it.hasNext()) {
                String next = it.next();
                if (Integer.parseInt(next) != 0) {
                    i3 |= ServiceState.getBitmaskForTech(Integer.parseInt(next));
                } else {
                    i = 0;
                    break;
                }
            } else {
                i = i3;
                break;
            }
        }
        boolean intValueAndCheckIfDiff = setIntValueAndCheckIfDiff(contentValues, "bearer_bitmask", i, stringValueAndCheckIfDiff2, 19);
        if (i != 0 && this.mBearerInitialVal != 0 && ServiceState.bitmaskHasTech(i, this.mBearerInitialVal)) {
            i2 = this.mBearerInitialVal;
        } else {
            i2 = 0;
        }
        boolean intValueAndCheckIfDiff2 = setIntValueAndCheckIfDiff(contentValues, "carrier_enabled", this.mCarrierEnabled.isChecked() ? 1 : 0, setStringValueAndCheckIfDiff(contentValues, "mvno_match_data", checkNotSet(this.mMvnoMatchData.getText()), setStringValueAndCheckIfDiff(contentValues, "mvno_type", checkNotSet(this.mMvnoType.getValue()), setIntValueAndCheckIfDiff(contentValues, "bearer", i2, intValueAndCheckIfDiff, 18), 21), 22), 17);
        contentValues.put("edited", (Integer) 1);
        contentValues.put("sourcetype", Integer.valueOf(this.mSourceType));
        this.mApnExt.saveApnValues(contentValues);
        Log.d(TAG, "Save apn " + contentValues.getAsString("apn"));
        if (intValueAndCheckIfDiff2) {
            updateApnDataToDatabase(this.mApnData.getUri() == null ? this.mCarrierUri : this.mApnData.getUri(), contentValues);
        }
        return true;
    }

    private void updateApnDataToDatabase(final Uri uri, final ContentValues contentValues) {
        ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.android.settings.network.-$$Lambda$ApnEditor$1vSLgWOnd4pMuFU2qFaSz0HXNw8
            @Override // java.lang.Runnable
            public final void run() {
                ApnEditor.lambda$updateApnDataToDatabase$0(ApnEditor.this, uri, contentValues);
            }
        });
    }

    public static /* synthetic */ void lambda$updateApnDataToDatabase$0(ApnEditor apnEditor, Uri uri, ContentValues contentValues) {
        if (uri.equals(apnEditor.mCarrierUri)) {
            if (apnEditor.getContentResolver().insert(apnEditor.mCarrierUri, contentValues) == null) {
                String str = TAG;
                Log.e(str, "Can't add a new apn to database " + apnEditor.mCarrierUri);
                return;
            }
            return;
        }
        apnEditor.getContentResolver().update(uri, contentValues, null, null);
    }

    String validateApnData() {
        String string;
        String[] strArr;
        String checkNotSet = checkNotSet(this.mName.getText());
        String checkNotSet2 = checkNotSet(this.mApn.getText());
        String checkNotSet3 = checkNotSet(this.mMcc.getText());
        String checkNotSet4 = checkNotSet(this.mMnc.getText());
        String text = this.mApnType.getText();
        if (TextUtils.isEmpty(checkNotSet)) {
            string = getResources().getString(R.string.error_name_empty);
        } else if (TextUtils.isEmpty(checkNotSet2) && (text == null || !text.contains("ia"))) {
            string = getResources().getString(R.string.error_apn_empty);
        } else if (checkNotSet3 == null || checkNotSet3.length() != 3) {
            string = getResources().getString(R.string.error_mcc_not3);
        } else if (checkNotSet4 == null || (checkNotSet4.length() & 65534) != 2) {
            string = getResources().getString(R.string.error_mnc_not23);
        } else {
            string = null;
        }
        if (string == null && !ArrayUtils.isEmpty(this.mReadOnlyApnTypes) && apnTypesMatch(this.mReadOnlyApnTypes, getUserEnteredApnType())) {
            StringBuilder sb = new StringBuilder();
            for (String str : this.mReadOnlyApnTypes) {
                sb.append(str);
                sb.append(", ");
                Log.d(TAG, "validateApnData: appending type: " + str);
            }
            if (sb.length() >= 2) {
                sb.delete(sb.length() - 2, sb.length());
            }
            return String.format(getResources().getString(R.string.error_adding_apn_type), sb);
        }
        return string;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int i) {
        if (i == 1) {
            return new AlertDialog.Builder(getContext()).setIcon(17301543).setTitle(R.string.error_title).setMessage(getString(R.string.apn_predefine_change_dialog_notice)).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.settings.network.ApnEditor.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    if (ApnEditor.this.validateAndSaveApnData()) {
                        ApnEditor.this.finish();
                    }
                }
            }).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null).create();
        }
        return super.onCreateDialog(i);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public int getDialogMetricsCategory(int i) {
        if (i == 1) {
            return 530;
        }
        return 0;
    }

    void showError() {
        ErrorDialog.showError(this);
    }

    private void deleteApn() {
        if (this.mApnData.getUri() != null) {
            getContentResolver().delete(this.mApnData.getUri(), null, null);
            this.mApnData = new ApnData(sProjection.length);
        }
    }

    private String starify(String str) {
        if (str == null || str.length() == 0) {
            return sNotSet;
        }
        char[] cArr = new char[str.length()];
        for (int i = 0; i < cArr.length; i++) {
            cArr[i] = '*';
        }
        return new String(cArr);
    }

    private String checkNull(String str) {
        return TextUtils.isEmpty(str) ? sNotSet : str;
    }

    private String checkNotSet(String str) {
        if (sNotSet.equals(str)) {
            return null;
        }
        return str;
    }

    private String getUserEnteredApnType() {
        String[] strArr;
        String text = this.mApnType.getText();
        if (text != null) {
            text = text.trim();
        }
        if ((TextUtils.isEmpty(text) || "*".equals(text)) && !ArrayUtils.isEmpty(this.mReadOnlyApnTypes)) {
            StringBuilder sb = new StringBuilder();
            List asList = Arrays.asList(this.mReadOnlyApnTypes);
            boolean z = true;
            for (String str : MtkPhoneConstants.MTK_APN_TYPES) {
                if ((!TextUtils.isEmpty(text) || !str.equals("ims")) && !asList.contains(str) && !str.equals("ia") && !str.equals("emergency")) {
                    if (!z) {
                        sb.append(",");
                    } else {
                        z = false;
                    }
                    sb.append(str);
                }
            }
            String sb2 = sb.toString();
            Log.d(TAG, "getUserEnteredApnType: changed apn type to editable apn types: " + sb2);
            return sb2;
        }
        return text;
    }

    /* loaded from: classes.dex */
    public static class ErrorDialog extends InstrumentedDialogFragment {
        public static void showError(ApnEditor apnEditor) {
            ErrorDialog errorDialog = new ErrorDialog();
            errorDialog.setTargetFragment(apnEditor, 0);
            errorDialog.show(apnEditor.getFragmentManager(), "error");
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            return new AlertDialog.Builder(getContext()).setTitle(R.string.error_title).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).setMessage(((ApnEditor) getTargetFragment()).validateApnData()).create();
        }

        @Override // com.android.settingslib.core.instrumentation.Instrumentable
        public int getMetricsCategory() {
            return 530;
        }
    }

    ApnData getApnDataFromUri(Uri uri) {
        Cursor query = getContentResolver().query(uri, sProjection, null, null, null);
        ApnData apnData = null;
        if (query != null) {
            try {
                query.moveToFirst();
                apnData = new ApnData(uri, query);
            } catch (Throwable th) {
                try {
                    throw th;
                } catch (Throwable th2) {
                    if (query != null) {
                        if (th != null) {
                            try {
                                query.close();
                            } catch (Throwable th3) {
                                th.addSuppressed(th3);
                            }
                        } else {
                            query.close();
                        }
                    }
                    throw th2;
                }
            }
        }
        if (query != null) {
            query.close();
        }
        if (apnData == null) {
            String str = TAG;
            Log.d(str, "Can't get apnData from Uri " + uri);
        }
        return apnData;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class ApnData {
        Object[] mData;
        Uri mUri;

        ApnData(int i) {
            this.mData = new Object[i];
        }

        ApnData(Uri uri, Cursor cursor) {
            this.mUri = uri;
            this.mData = new Object[cursor.getColumnCount()];
            for (int i = 0; i < this.mData.length; i++) {
                switch (cursor.getType(i)) {
                    case 1:
                        this.mData[i] = Integer.valueOf(cursor.getInt(i));
                        break;
                    case 2:
                        this.mData[i] = Float.valueOf(cursor.getFloat(i));
                        break;
                    case 3:
                        this.mData[i] = cursor.getString(i);
                        break;
                    case 4:
                        this.mData[i] = cursor.getBlob(i);
                        break;
                    default:
                        this.mData[i] = null;
                        break;
                }
            }
        }

        Uri getUri() {
            return this.mUri;
        }

        Integer getInteger(int i) {
            return (Integer) this.mData[i];
        }

        Integer getInteger(int i, Integer num) {
            Integer integer = getInteger(i);
            return integer == null ? num : integer;
        }

        String getString(int i) {
            return (String) this.mData[i];
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.mSimHotSwapHandler != null) {
            this.mSimHotSwapHandler.unregisterOnSimHotSwap();
        }
        getContext().unregisterReceiver(this.mReceiver);
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
        this.mApnExt.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exitWithoutSave() {
        if (this.mNewApn && this.mApnData.getUri() != null) {
            getContentResolver().delete(this.mApnData.getUri(), null, null);
        }
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreenEnableState() {
        boolean isSimReadyAndRadioOn = isSimReadyAndRadioOn();
        String str = TAG;
        Log.d(str, "enable = " + isSimReadyAndRadioOn + " mReadOnlyMode = " + this.mReadOnlyMode);
        getPreferenceScreen().setEnabled(isSimReadyAndRadioOn && !this.mReadOnlyMode && this.mApnExt.getScreenEnableState(this.mSubId, getActivity()));
        this.mApnExt.setApnTypePreferenceState(this.mApnType, this.mApnType.getText());
        this.mApnExt.updateFieldsStatus(this.mSubId, this.mSourceType, getPreferenceScreen(), this.mApn.getText());
        this.mApnExt.setMvnoPreferenceState(this.mMvnoType, this.mMvnoMatchData);
    }

    private boolean isSimReadyAndRadioOn() {
        boolean z = false;
        boolean z2 = 5 == TelephonyManager.getDefault().getSimState(SubscriptionManager.getSlotIndex(this.mSubId));
        boolean z3 = Settings.System.getInt(getContentResolver(), "airplane_mode_on", -1) == 1;
        if (!z3 && z2) {
            z = true;
        }
        Log.d(TAG, "isSimReadyAndRadioOn(), subId = " + this.mSubId + " ,airplaneModeEnabled = " + z3 + " ,simReady = " + z2);
        return z;
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        Preference findPreference = findPreference(str);
        if (findPreference != null) {
            if (findPreference.equals(this.mCarrierEnabled)) {
                findPreference.setSummary(checkNull(String.valueOf(sharedPreferences.getBoolean(str, true))));
            } else if (findPreference.equals(this.mPort)) {
                String string = sharedPreferences.getString(str, "");
                if (!string.equals("")) {
                    try {
                        int parseInt = Integer.parseInt(string);
                        if (parseInt > 65535 || parseInt <= 0) {
                            Toast.makeText(getContext(), getString(R.string.apn_port_warning), 1).show();
                            ((EditTextPreference) findPreference).setText("");
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), getString(R.string.apn_port_warning), 1).show();
                        ((EditTextPreference) findPreference).setText("");
                    }
                }
                findPreference.setSummary(checkNull(sharedPreferences.getString(str, "")));
            } else {
                findPreference.setSummary(checkNull(sharedPreferences.getString(str, "")));
            }
        }
    }
}
