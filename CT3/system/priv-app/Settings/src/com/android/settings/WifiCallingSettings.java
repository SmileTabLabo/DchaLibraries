package com.android.settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.widget.SwitchBar;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IWfcSettingsExt;
/* loaded from: classes.dex */
public class WifiCallingSettings extends SettingsPreferenceFragment implements SwitchBar.OnSwitchChangeListener, Preference.OnPreferenceChangeListener {
    private ListPreference mButtonWfcMode;
    private TextView mEmptyView;
    private IntentFilter mIntentFilter;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    IWfcSettingsExt mWfcExt;
    private boolean mValidListener = false;
    private boolean mEditableWfcMode = true;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.android.settings.WifiCallingSettings.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("WifiCallingSettings", "onReceive()... " + action);
            if (action.equals("com.android.ims.REGISTRATION_ERROR")) {
                setResultCode(0);
                WifiCallingSettings.this.mSwitch.setChecked(false);
                WifiCallingSettings.this.showAlert(intent);
            } else if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                if (ImsManager.isWfcEnabledByPlatform(context)) {
                    return;
                }
                Log.d("WifiCallingSettings", "carrier config changed, finish WFC activity");
                WifiCallingSettings.this.getActivity().finish();
            } else if (!action.equals("android.intent.action.PHONE_STATE")) {
            } else {
                WifiCallingSettings.this.updateScreen();
            }
        }
    };

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mSwitchBar = activity.getSwitchBar();
        this.mSwitch = this.mSwitchBar.getSwitch();
        this.mSwitchBar.show();
        this.mEmptyView = (TextView) getView().findViewById(16908292);
        setEmptyView(this.mEmptyView);
        this.mEmptyView.setText(R.string.wifi_calling_off_explanation);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.hide();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAlert(Intent intent) {
        Context context = getActivity();
        CharSequence title = intent.getCharSequenceExtra("alertTitle");
        CharSequence message = intent.getCharSequenceExtra("alertMessage");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setTitle(title).setIcon(17301543).setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 105;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        PersistableBundle b;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wifi_calling_settings);
        this.mWfcExt = UtilsExt.getWfcSettingsPlugin(getActivity());
        this.mWfcExt.initPlugin(this);
        this.mButtonWfcMode = (ListPreference) findPreference("wifi_calling_mode");
        this.mButtonWfcMode.setOnPreferenceChangeListener(this);
        this.mWfcExt.addOtherCustomPreference();
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("com.android.ims.REGISTRATION_ERROR");
        this.mIntentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mIntentFilter.addAction("android.intent.action.PHONE_STATE");
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService("carrier_config");
        boolean isWifiOnlySupported = true;
        if (configManager != null && (b = configManager.getConfig()) != null) {
            this.mEditableWfcMode = b.getBoolean("editable_wfc_mode_bool");
            isWifiOnlySupported = b.getBoolean("carrier_wfc_supports_wifi_only_bool", true);
        }
        if (!isWifiOnlySupported) {
            this.mButtonWfcMode.setEntries(R.array.wifi_calling_mode_choices_without_wifi_only);
            this.mButtonWfcMode.setEntryValues(R.array.wifi_calling_mode_values_without_wifi_only);
        }
        this.mWfcExt.onWfcSettingsEvent(2);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        boolean z;
        super.onResume();
        Context context = getActivity();
        if (ImsManager.isWfcEnabledByPlatform(context)) {
            this.mSwitchBar.addOnSwitchChangeListener(this);
            this.mValidListener = true;
        }
        if (!ImsManager.isWfcEnabledByUser(context)) {
            z = false;
        } else {
            z = ImsManager.isNonTtyOrTtyOnVolteEnabled(context);
        }
        this.mSwitch.setChecked(z);
        int wfcMode = ImsManager.getWfcMode(context);
        this.mButtonWfcMode.setValue(Integer.toString(wfcMode));
        updateButtonWfcMode(context, z, wfcMode);
        this.mWfcExt.initPlugin(this);
        this.mWfcExt.updateWfcModePreference(getPreferenceScreen(), this.mButtonWfcMode, z, wfcMode);
        updateScreen();
        context.registerReceiver(this.mIntentReceiver, this.mIntentFilter);
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("alertShow", false)) {
            showAlert(intent);
        }
        this.mWfcExt.onWfcSettingsEvent(0);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        Context context = getActivity();
        if (this.mValidListener) {
            this.mValidListener = false;
            this.mSwitchBar.removeOnSwitchChangeListener(this);
        }
        context.unregisterReceiver(this.mIntentReceiver);
        this.mWfcExt.onWfcSettingsEvent(1);
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        this.mWfcExt.onWfcSettingsEvent(3);
        super.onDestroy();
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Log.d("WifiCallingSettings", "OnSwitchChanged");
        if (isInSwitchProcess()) {
            Log.d("WifiCallingSettings", "[onClick] Switching process ongoing");
            Toast.makeText(getActivity(), (int) R.string.Switch_not_in_use_string, 0).show();
            this.mSwitchBar.setChecked(isChecked ? false : true);
            return;
        }
        Context context = getActivity();
        ImsManager.setWfcSetting(context, isChecked);
        int wfcMode = ImsManager.getWfcMode(context);
        updateButtonWfcMode(context, isChecked, wfcMode);
        this.mWfcExt.updateWfcModePreference(getPreferenceScreen(), this.mButtonWfcMode, isChecked, wfcMode);
        if (isChecked) {
            MetricsLogger.action(getActivity(), getMetricsCategory(), wfcMode);
        } else {
            MetricsLogger.action(getActivity(), getMetricsCategory(), -1);
        }
    }

    private void updateButtonWfcMode(Context context, boolean wfcEnabled, int wfcMode) {
        this.mButtonWfcMode.setSummary(getWfcModeSummary(context, wfcMode));
        this.mButtonWfcMode.setEnabled(wfcEnabled);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (wfcEnabled) {
            preferenceScreen.addPreference(this.mButtonWfcMode);
        } else {
            preferenceScreen.removePreference(this.mButtonWfcMode);
        }
        preferenceScreen.setEnabled(this.mEditableWfcMode);
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Context context = getActivity();
        if (preference == this.mButtonWfcMode) {
            this.mButtonWfcMode.setValue((String) newValue);
            int buttonMode = Integer.valueOf((String) newValue).intValue();
            int currentMode = ImsManager.getWfcMode(context);
            if (buttonMode != currentMode) {
                ImsManager.setWfcMode(context, buttonMode);
                this.mButtonWfcMode.setSummary(getWfcModeSummary(context, buttonMode));
                MetricsLogger.action(getActivity(), getMetricsCategory(), buttonMode);
                return true;
            }
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getWfcModeSummary(Context context, int wfcMode) {
        if (!ImsManager.isWfcEnabledByUser(context)) {
            return 17039591;
        }
        switch (wfcMode) {
            case 0:
                return 17039594;
            case 1:
                return 17039593;
            case 2:
                return 17039592;
            default:
                Log.e("WifiCallingSettings", "Unexpected WFC mode value: " + wfcMode);
                return 17039591;
        }
    }

    private boolean isInSwitchProcess() {
        try {
            int imsState = ImsManager.getInstance(getActivity(), SubscriptionManager.getDefaultVoiceSubscriptionId()).getImsState();
            Log.d("@M_WifiCallingSettings", "isInSwitchProcess , imsState = " + imsState);
            return imsState == 3 || imsState == 2;
        } catch (ImsException e) {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreen() {
        boolean z;
        SettingsActivity activity = (SettingsActivity) getActivity();
        if (activity == null) {
            return;
        }
        boolean isNonTtyOrTtyOnVolteEnabled = ImsManager.isNonTtyOrTtyOnVolteEnabled(activity);
        SwitchBar switchBar = activity.getSwitchBar();
        if (!switchBar.getSwitch().isChecked()) {
            z = false;
        } else {
            z = isNonTtyOrTtyOnVolteEnabled;
        }
        boolean isCallStateIdle = !TelecomManager.from(activity).isInCall();
        Log.d("WifiCallingSettings", "isWfcEnabled: " + z + ", isCallStateIdle: " + isCallStateIdle);
        if (!isCallStateIdle) {
            isNonTtyOrTtyOnVolteEnabled = false;
        }
        switchBar.setEnabled(isNonTtyOrTtyOnVolteEnabled);
        Preference pref = getPreferenceScreen().findPreference("wifi_calling_mode");
        if (pref == null) {
            return;
        }
        if (!z) {
            isCallStateIdle = false;
        }
        pref.setEnabled(isCallStateIdle);
    }
}
