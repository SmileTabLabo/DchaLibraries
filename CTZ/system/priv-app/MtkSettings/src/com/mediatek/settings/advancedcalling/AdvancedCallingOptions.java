package com.mediatek.settings.advancedcalling;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import com.android.ims.ImsConfig;
import com.android.ims.ImsConnectionStateListener;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.settings.sim.TelephonyUtils;
/* loaded from: classes.dex */
public class AdvancedCallingOptions extends SettingsPreferenceFragment implements SwitchBar.OnSwitchChangeListener {
    private Context mContext;
    private boolean mEnableLVC;
    private boolean mEnablePlatform;
    private ImsManager mImsManager;
    private IntentFilter mIntentFilter;
    private RadioGroup mRadioGroup;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private RadioButton mVoiceButton;
    private RadioButton mVoiceVideoButton = null;
    private boolean mValidListener = false;
    private final ImsConnectionStateListener mImsRegListener = new ImsConnectionStateListener() { // from class: com.mediatek.settings.advancedcalling.AdvancedCallingOptions.1
        public void onFeatureCapabilityChanged(int i, int[] iArr, int[] iArr2) {
            Log.d("OP12AdvancedCallingOptionsFragment", "Receive IMS FeatureCapabilityChanged");
            AdvancedCallingOptions.this.handleImsStateChange(i, iArr, iArr2);
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.mediatek.settings.advancedcalling.AdvancedCallingOptions.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("OP12AdvancedCallingOptionsFragment", "onReceive()... " + action);
            if (action.equals("com.android.ims.REGISTRATION_ERROR")) {
                Log.d("OP12AdvancedCallingOptionsFragment", "IMS Registration error, disable Switch");
                setResultCode(0);
                AdvancedCallingOptions.this.mSwitch.setChecked(false);
                AdvancedCallingOptions.this.showAlert(intent);
            } else if (action.equals("com.android.intent.action.IMS_CONFIG_CHANGED")) {
                Log.d("OP12AdvancedCallingOptionsFragment", "config changed, finish Advance Calling activity");
                try {
                    ImsConfig configInterface = ImsManager.getInstance(AdvancedCallingOptions.this.mContext, SubscriptionManager.getDefaultVoicePhoneId()).getConfigInterface();
                    AdvancedCallingOptions advancedCallingOptions = AdvancedCallingOptions.this;
                    boolean z = true;
                    if (1 != configInterface.getProvisionedValue(11)) {
                        z = false;
                    }
                    advancedCallingOptions.mEnableLVC = z;
                    Log.d("OP12AdvancedCallingOptionsFragment", "enableLVC:" + AdvancedCallingOptions.this.mEnableLVC + "  enablePlatform:" + AdvancedCallingOptions.this.mEnablePlatform);
                } catch (ImsException e) {
                    Log.e("OP12AdvancedCallingOptionsFragment", "Advanced settings not updated, ImsConfig null");
                    e.printStackTrace();
                }
                AdvancedCallingOptions.this.getActivity().finish();
            } else if (action.equals("android.intent.action.PHONE_STATE")) {
                Log.d("OP12AdvancedCallingOptionsFragment", "Phone state changed, so update the screen");
                AdvancedCallingOptions.this.updateScreen();
            }
        }
    };

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Log.d("OP12AdvancedCallingOptionsFragment", "onActivityCreated");
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        this.mSwitch = this.mSwitchBar.getSwitch();
        this.mSwitchBar.show();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = getActivity();
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("com.android.ims.REGISTRATION_ERROR");
        this.mIntentFilter.addAction("android.intent.action.PHONE_STATE");
        this.mIntentFilter.addAction("com.android.intent.action.IMS_CONFIG_CHANGED");
        this.mEnablePlatform = ImsManager.isVtEnabledByPlatform(this.mContext);
        this.mImsManager = ImsManager.getInstance(this.mContext, TelephonyUtils.getMainCapabilityPhoneId());
        try {
            boolean z = true;
            if (1 != ImsManager.getInstance(this.mContext, SubscriptionManager.getDefaultVoicePhoneId()).getConfigInterface().getProvisionedValue(11)) {
                z = false;
            }
            this.mEnableLVC = z;
            Log.d("OP12AdvancedCallingOptionsFragment", "enableLVC:" + this.mEnableLVC + "  enablePlatform:" + this.mEnablePlatform);
        } catch (ImsException e) {
            Log.e("OP12AdvancedCallingOptionsFragment", "Advanced settings not updated, ImsConfig null");
            e.printStackTrace();
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate;
        if (this.mEnablePlatform) {
            inflate = layoutInflater.inflate(R.layout.calling_pref_layout, viewGroup, false);
            this.mVoiceVideoButton = (RadioButton) inflate.findViewById(R.id.hd_voice_video);
        } else {
            inflate = layoutInflater.inflate(R.layout.calling_voice_only_pref_layout, viewGroup, false);
        }
        this.mRadioGroup = (RadioGroup) inflate.findViewById(R.id.hd_voice_video_group);
        this.mVoiceButton = (RadioButton) inflate.findViewById(R.id.hd_voice_only);
        this.mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: com.mediatek.settings.advancedcalling.AdvancedCallingOptions.3
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.hd_voice_only /* 2131362167 */:
                        Log.d("OP12AdvancedCallingOptionsFragment", "Voice only button checked");
                        if (AdvancedCallingOptions.this.mVoiceButton.isEnabled()) {
                            Log.d("OP12AdvancedCallingOptionsFragment", "Show Voice only Button ON dialog");
                            AlertDialog.Builder builder = new AlertDialog.Builder(AdvancedCallingOptions.this.mContext);
                            View inflate2 = LayoutInflater.from(AdvancedCallingOptions.this.mContext).inflate(R.layout.skip_checkbox, (ViewGroup) null);
                            final CheckBox checkBox = (CheckBox) inflate2.findViewById(R.id.skip_box);
                            builder.setView(inflate2);
                            builder.setTitle(AdvancedCallingOptions.this.mContext.getString(R.string.note));
                            builder.setCancelable(false);
                            builder.setMessage(AdvancedCallingOptions.this.mContext.getString(R.string.enable_hd_voice_only_msg)).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.advancedcalling.AdvancedCallingOptions.3.2
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialogInterface, int i2) {
                                    String str = "NOT checked";
                                    if (checkBox.isChecked()) {
                                        str = "checked";
                                    }
                                    SharedPreferences.Editor edit = AdvancedCallingOptions.this.mContext.getSharedPreferences("preff_advanced_calling", 0).edit();
                                    edit.putString("skipMessage4", str);
                                    edit.commit();
                                }
                            });
                            AlertDialog create = builder.create();
                            if (!AdvancedCallingOptions.this.mContext.getSharedPreferences("preff_advanced_calling", 0).getString("skipMessage4", "NOT checked").equalsIgnoreCase("checked")) {
                                create.show();
                            }
                            if (AdvancedCallingOptions.this.mEnablePlatform && AdvancedCallingOptions.this.mEnableLVC) {
                                Settings.Global.putInt(AdvancedCallingOptions.this.mContext.getContentResolver(), "KEY_CALL_OPTIONS", 0);
                                ImsManager.getInstance(AdvancedCallingOptions.this.mContext, TelephonyUtils.getMainCapabilityPhoneId());
                                MtkImsManager.setVtSetting(AdvancedCallingOptions.this.mContext, false, 0);
                                Log.d("OP12AdvancedCallingOptionsFragment", "Set VT false");
                                return;
                            }
                            return;
                        }
                        return;
                    case R.id.hd_voice_video /* 2131362168 */:
                        Log.d("OP12AdvancedCallingOptionsFragment", "Video Button checked");
                        if (AdvancedCallingOptions.this.mEnableLVC) {
                            if (AdvancedCallingOptions.this.mVoiceVideoButton.isEnabled()) {
                                Log.d("OP12AdvancedCallingOptionsFragment", "Show Video Button ON dialog");
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(AdvancedCallingOptions.this.mContext);
                                View inflate3 = LayoutInflater.from(AdvancedCallingOptions.this.mContext).inflate(R.layout.skip_checkbox, (ViewGroup) null);
                                final CheckBox checkBox2 = (CheckBox) inflate3.findViewById(R.id.skip_box);
                                builder2.setView(inflate3);
                                builder2.setCancelable(false);
                                builder2.setTitle(AdvancedCallingOptions.this.mContext.getString(R.string.note));
                                builder2.setMessage(AdvancedCallingOptions.this.mContext.getString(R.string.advance_calling_enable_msg)).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.advancedcalling.AdvancedCallingOptions.3.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface, int i2) {
                                        String str = "NOT checked";
                                        if (checkBox2.isChecked()) {
                                            str = "checked";
                                        }
                                        SharedPreferences.Editor edit = AdvancedCallingOptions.this.mContext.getSharedPreferences("preff_advanced_calling", 0).edit();
                                        edit.putString("skipMessage3", str);
                                        edit.commit();
                                    }
                                });
                                AlertDialog create2 = builder2.create();
                                if (!AdvancedCallingOptions.this.mContext.getSharedPreferences("preff_advanced_calling", 0).getString("skipMessage3", "NOT checked").equalsIgnoreCase("checked")) {
                                    create2.show();
                                }
                                ImsManager.getInstance(AdvancedCallingOptions.this.mContext, TelephonyUtils.getMainCapabilityPhoneId());
                                MtkImsManager.setVtSetting(AdvancedCallingOptions.this.mContext, true, 0);
                                Log.d("OP12AdvancedCallingOptionsFragment", "Set VT true");
                                Settings.Global.putInt(AdvancedCallingOptions.this.mContext.getContentResolver(), "KEY_CALL_OPTIONS", 1);
                                return;
                            }
                            return;
                        }
                        AlertDialog.Builder builder3 = new AlertDialog.Builder(AdvancedCallingOptions.this.mContext);
                        builder3.setCancelable(false);
                        builder3.setTitle(AdvancedCallingOptions.this.mContext.getString(R.string.note));
                        builder3.setMessage(AdvancedCallingOptions.this.mContext.getString(R.string.lvc_disable));
                        builder3.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
                        builder3.create().show();
                        AdvancedCallingOptions.this.mVoiceVideoButton.setChecked(false);
                        AdvancedCallingOptions.this.mVoiceButton.setEnabled(false);
                        AdvancedCallingOptions.this.mVoiceButton.setChecked(true);
                        AdvancedCallingOptions.this.mVoiceButton.setEnabled(true);
                        Log.d("OP12AdvancedCallingOptionsFragment", "LVC is disabled, so disable HD Voice and Video option");
                        return;
                    default:
                        return;
                }
            }
        });
        return inflate;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        Log.d("OP12AdvancedCallingOptionsFragment", "On Resume");
        if (ImsManager.isVolteEnabledByPlatform(this.mContext)) {
            this.mSwitchBar.addOnSwitchChangeListener(this);
            this.mValidListener = true;
        }
        boolean isEnhanced4gLteModeSettingEnabledByUser = ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mContext);
        if (isEnhanced4gLteModeSettingEnabledByUser) {
            for (int i = 0; i < this.mRadioGroup.getChildCount(); i++) {
                this.mRadioGroup.getChildAt(i).setEnabled(false);
            }
            if (this.mEnablePlatform && this.mEnableLVC) {
                this.mVoiceVideoButton.setAlpha(1.0f);
                if (ImsManager.isVtEnabledByUser(this.mContext)) {
                    this.mRadioGroup.check(R.id.hd_voice_video);
                } else {
                    this.mRadioGroup.check(R.id.hd_voice_only);
                }
            } else if (this.mEnablePlatform && !this.mEnableLVC) {
                this.mVoiceVideoButton.setAlpha(0.2f);
                this.mRadioGroup.check(R.id.hd_voice_only);
            } else {
                this.mRadioGroup.check(R.id.hd_voice_only);
            }
            for (int i2 = 0; i2 < this.mRadioGroup.getChildCount(); i2++) {
                this.mRadioGroup.getChildAt(i2).setEnabled(true);
            }
        } else {
            if (this.mEnablePlatform && this.mEnableLVC) {
                this.mVoiceVideoButton.setAlpha(1.0f);
            } else if (this.mEnablePlatform && !this.mEnableLVC) {
                this.mVoiceVideoButton.setAlpha(0.2f);
            }
            for (int i3 = 0; i3 < this.mRadioGroup.getChildCount(); i3++) {
                this.mRadioGroup.getChildAt(i3).setEnabled(false);
            }
            this.mRadioGroup.clearCheck();
        }
        this.mSwitch.setChecked(isEnhanced4gLteModeSettingEnabledByUser);
        updateScreen();
        this.mContext.registerReceiver(this.mIntentReceiver, this.mIntentFilter);
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("alertShow", false)) {
            showAlert(intent);
        }
        registerForImsStateChange();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        if (this.mValidListener) {
            this.mValidListener = false;
            this.mSwitchBar.removeOnSwitchChangeListener(this);
        }
        this.mContext.unregisterReceiver(this.mIntentReceiver);
        unRegisterForImsStateChange();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.hide();
    }

    @Override // com.android.settings.widget.SwitchBar.OnSwitchChangeListener
    public void onSwitchChanged(Switch r10, boolean z) {
        Log.d("OP12AdvancedCallingOptionsFragment", "OnSwitchChanged, disable switchbar");
        this.mSwitchBar.setEnabled(false);
        if (z) {
            Log.d("OP12AdvancedCallingOptionsFragment", "Switch is checked");
            Settings.Global.putInt(this.mContext.getContentResolver(), "KEY_ADVANCED_CALLING", 1);
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            View inflate = LayoutInflater.from(this.mContext).inflate(R.layout.skip_checkbox, (ViewGroup) null);
            final CheckBox checkBox = (CheckBox) inflate.findViewById(R.id.skip_box);
            builder.setView(inflate);
            builder.setTitle(this.mContext.getString(R.string.note));
            builder.setCancelable(false);
            builder.setMessage(this.mContext.getString(R.string.advance_calling_enable_msg)).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.advancedcalling.AdvancedCallingOptions.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    String str = "NOT checked";
                    if (checkBox.isChecked()) {
                        str = "checked";
                    }
                    SharedPreferences.Editor edit = AdvancedCallingOptions.this.mContext.getSharedPreferences("preff_advanced_calling", 0).edit();
                    edit.putString("skipMessage1", str);
                    edit.commit();
                }
            });
            AlertDialog create = builder.create();
            if (!this.mContext.getSharedPreferences("preff_advanced_calling", 0).getString("skipMessage1", "NOT checked").equalsIgnoreCase("checked")) {
                create.show();
            }
            for (int i = 0; i < this.mRadioGroup.getChildCount(); i++) {
                this.mRadioGroup.getChildAt(i).setEnabled(false);
            }
            int i2 = Settings.Global.getInt(this.mContext.getContentResolver(), "KEY_CALL_OPTIONS", 1);
            if (this.mEnablePlatform && this.mEnableLVC && i2 == 1) {
                this.mRadioGroup.check(R.id.hd_voice_video);
            } else {
                this.mRadioGroup.check(R.id.hd_voice_only);
            }
            for (int i3 = 0; i3 < this.mRadioGroup.getChildCount(); i3++) {
                this.mRadioGroup.getChildAt(i3).setEnabled(true);
            }
        } else {
            Log.d("OP12AdvancedCallingOptionsFragment", "Switch is Unchecked");
            Settings.Global.putInt(this.mContext.getContentResolver(), "KEY_ADVANCED_CALLING", 0);
            AlertDialog.Builder builder2 = new AlertDialog.Builder(this.mContext);
            View inflate2 = LayoutInflater.from(this.mContext).inflate(R.layout.skip_checkbox, (ViewGroup) null);
            final CheckBox checkBox2 = (CheckBox) inflate2.findViewById(R.id.skip_box);
            builder2.setView(inflate2);
            builder2.setTitle(this.mContext.getString(R.string.note));
            builder2.setCancelable(false);
            builder2.setMessage(this.mContext.getString(R.string.advance_calling_disable_msg)).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.advancedcalling.AdvancedCallingOptions.5
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i4) {
                    String str = "NOT checked";
                    if (checkBox2.isChecked()) {
                        str = "checked";
                    }
                    SharedPreferences.Editor edit = AdvancedCallingOptions.this.mContext.getSharedPreferences("preff_advanced_calling", 0).edit();
                    edit.putString("skipMessage2", str);
                    edit.commit();
                }
            });
            AlertDialog create2 = builder2.create();
            if (!this.mContext.getSharedPreferences("preff_advanced_calling", 0).getString("skipMessage2", "NOT checked").equalsIgnoreCase("checked")) {
                create2.show();
            }
            if (this.mEnableLVC) {
                if (this.mRadioGroup.getCheckedRadioButtonId() == R.id.hd_voice_video) {
                    Settings.Global.putInt(this.mContext.getContentResolver(), "KEY_CALL_OPTIONS", 1);
                } else {
                    Settings.Global.putInt(this.mContext.getContentResolver(), "KEY_CALL_OPTIONS", 0);
                }
            }
            for (int i4 = 0; i4 < this.mRadioGroup.getChildCount(); i4++) {
                this.mRadioGroup.getChildAt(i4).setEnabled(false);
            }
            this.mRadioGroup.clearCheck();
        }
        ImsManager.setEnhanced4gLteModeSetting(this.mContext, z);
        int i5 = Settings.Global.getInt(this.mContext.getContentResolver(), "KEY_CALL_OPTIONS", 1);
        if (this.mEnablePlatform && this.mEnableLVC) {
            if (z && i5 == 1) {
                ImsManager.setVtSetting(this.mContext, z);
                Log.d("OP12AdvancedCallingOptionsFragment", "Set VT setting:" + z);
                return;
            }
            ImsManager.setVtSetting(this.mContext, false);
            Log.d("OP12AdvancedCallingOptionsFragment", "Set VT setting: false");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAlert(Intent intent) {
        CharSequence charSequenceExtra = intent.getCharSequenceExtra("alertTitle");
        CharSequence charSequenceExtra2 = intent.getCharSequenceExtra("alertMessage");
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setMessage(charSequenceExtra2).setTitle(charSequenceExtra).setIcon(17301543).setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        builder.create().show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreen() {
        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        if (settingsActivity == null) {
            return;
        }
        SwitchBar switchBar = settingsActivity.getSwitchBar();
        boolean isChecked = switchBar.getSwitch().isChecked();
        boolean z = true;
        boolean z2 = !TelecomManager.from(settingsActivity).isInCall();
        Log.d("OP12AdvancedCallingOptionsFragment", "isAdvanceCallingEnabled: " + isChecked + ", isCallStateIdle: " + z2);
        switchBar.setEnabled(z2);
        if (this.mEnablePlatform) {
            this.mVoiceVideoButton.setEnabled(isChecked && z2);
        }
        RadioButton radioButton = this.mVoiceButton;
        if (!isChecked || !z2) {
            z = false;
        }
        radioButton.setEnabled(z);
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 105;
    }

    private void registerForImsStateChange() {
        try {
            this.mImsManager.addRegistrationListener(1, this.mImsRegListener);
        } catch (ImsException e) {
            Log.e("OP12AdvancedCallingOptionsFragment", "addRegistrationListener: " + e);
        }
    }

    private void unRegisterForImsStateChange() {
        try {
            this.mImsManager.removeRegistrationListener(this.mImsRegListener);
        } catch (ImsException e) {
            Log.e("OP12AdvancedCallingOptionsFragment", "removeRegistrationListener: " + e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleImsStateChange(int i, int[] iArr, int[] iArr2) {
        if (i == 1) {
            Log.d("OP12AdvancedCallingOptionsFragment", "VoLTE capability changed to :" + iArr[0]);
            this.mSwitchBar.setEnabled(true);
            Log.d("OP12AdvancedCallingOptionsFragment", "handleImsStateChange: Feature VoLTE is enabled, so enable switch");
        }
    }
}
