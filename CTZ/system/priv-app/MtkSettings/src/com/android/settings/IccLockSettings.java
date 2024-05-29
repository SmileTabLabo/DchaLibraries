package com.android.settings;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.EditPinPreference;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.TelephonyUtils;
/* loaded from: classes.dex */
public class IccLockSettings extends SettingsPreferenceFragment implements EditPinPreference.OnPinEnteredListener {
    private String mError;
    private ListView mListView;
    private String mNewPin;
    private String mOldPin;
    private Phone mPhone;
    private String mPin;
    private EditPinPreference mPinDialog;
    private SwitchPreference mPinToggle;
    private Resources mRes;
    private SimHotSwapHandler mSimHotSwapHandler;
    private TabHost mTabHost;
    private TabWidget mTabWidget;
    private boolean mToState;
    private int mDialogState = 0;
    private boolean mIsAirplaneModeOn = false;
    private Handler mHandler = new Handler() { // from class: com.android.settings.IccLockSettings.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            AsyncResult asyncResult = (AsyncResult) message.obj;
            switch (message.what) {
                case 100:
                    IccLockSettings.this.iccLockChanged(asyncResult.exception, message.arg1, (Phone) asyncResult.userObj);
                    return;
                case 101:
                    IccLockSettings.this.iccPinChanged(asyncResult.exception, message.arg1, (Phone) asyncResult.userObj);
                    return;
                case 102:
                    IccLockSettings.this.updatePreferences();
                    return;
                default:
                    return;
            }
        }
    };
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() { // from class: com.android.settings.IccLockSettings.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Dialog dialog;
            String action = intent.getAction();
            Log.d("IccLockSettings", "onReceive, action=" + action);
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                IccLockSettings.this.mHandler.sendMessage(IccLockSettings.this.mHandler.obtainMessage(102));
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                IccLockSettings.this.mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
                IccLockSettings.this.updatePreferences();
                if (IccLockSettings.this.mPinDialog != null) {
                    if (IccLockSettings.this.mIsAirplaneModeOn && (dialog = IccLockSettings.this.mPinDialog.getDialog()) != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    IccLockSettings.this.resetDialogState();
                }
            }
        }
    };
    private TabHost.OnTabChangeListener mTabListener = new TabHost.OnTabChangeListener() { // from class: com.android.settings.IccLockSettings.4
        @Override // android.widget.TabHost.OnTabChangeListener
        public void onTabChanged(String str) {
            SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = SubscriptionManager.from(IccLockSettings.this.getActivity().getBaseContext()).getActiveSubscriptionInfoForSimSlotIndex(Integer.parseInt(str));
            IccLockSettings.this.mPhone = activeSubscriptionInfoForSimSlotIndex == null ? null : PhoneFactory.getPhone(SubscriptionManager.getPhoneId(activeSubscriptionInfoForSimSlotIndex.getSubscriptionId()));
            StringBuilder sb = new StringBuilder();
            sb.append("onTabChanged(), phone=");
            sb.append((Object) (IccLockSettings.this.mPhone == null ? "null" : IccLockSettings.this.mPhone));
            Log.d("IccLockSettings", sb.toString());
            IccLockSettings.this.updatePreferences();
            IccLockSettings.this.resetDialogState();
        }
    };
    private TabHost.TabContentFactory mEmptyTabContent = new TabHost.TabContentFactory() { // from class: com.android.settings.IccLockSettings.5
        @Override // android.widget.TabHost.TabContentFactory
        public View createTabContent(String str) {
            return new View(IccLockSettings.this.mTabHost.getContext());
        }
    };

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.sim_lock_settings);
        this.mPinDialog = (EditPinPreference) findPreference("sim_pin");
        this.mPinToggle = (SwitchPreference) findPreference("sim_toggle");
        if (bundle != null && bundle.containsKey("dialogState")) {
            this.mDialogState = bundle.getInt("dialogState");
            this.mPin = bundle.getString("dialogPin");
            this.mError = bundle.getString("dialogError");
            this.mToState = bundle.getBoolean("enableState");
            switch (this.mDialogState) {
                case 3:
                    this.mOldPin = bundle.getString("oldPinCode");
                    break;
                case 4:
                    this.mOldPin = bundle.getString("oldPinCode");
                    this.mNewPin = bundle.getString("newPinCode");
                    break;
            }
        }
        this.mPinDialog.setOnPinEnteredListener(this);
        getPreferenceScreen().setPersistent(false);
        this.mRes = getResources();
        this.mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getActivity());
        this.mSimHotSwapHandler = new SimHotSwapHandler(getActivity());
        this.mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() { // from class: com.android.settings.IccLockSettings.3
            @Override // com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener
            public void onSimHotSwap() {
                Log.d("IccLockSettings", "onSimHotSwap, finish Activity.");
                IccLockSettings.this.finish();
            }
        });
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        CharSequence displayName;
        int simCount = ((TelephonyManager) getContext().getSystemService("phone")).getSimCount();
        if (simCount > 1) {
            View inflate = layoutInflater.inflate(R.layout.icc_lock_tabs, viewGroup, false);
            ViewGroup viewGroup2 = (ViewGroup) inflate.findViewById(R.id.prefs_container);
            Utils.prepareCustomPreferencesList(viewGroup, inflate, viewGroup2, false);
            viewGroup2.addView(super.onCreateView(layoutInflater, viewGroup2, bundle));
            this.mTabHost = (TabHost) inflate.findViewById(16908306);
            this.mTabWidget = (TabWidget) inflate.findViewById(16908307);
            this.mListView = (ListView) inflate.findViewById(16908298);
            this.mTabHost.setup();
            this.mTabHost.setOnTabChangedListener(this.mTabListener);
            this.mTabHost.clearAllTabs();
            SubscriptionManager from = SubscriptionManager.from(getContext());
            for (int i = 0; i < simCount; i++) {
                SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = from.getActiveSubscriptionInfoForSimSlotIndex(i);
                TabHost tabHost = this.mTabHost;
                String valueOf = String.valueOf(i);
                if (activeSubscriptionInfoForSimSlotIndex == null) {
                    displayName = getContext().getString(R.string.sim_editor_title, Integer.valueOf(i + 1));
                } else {
                    displayName = activeSubscriptionInfoForSimSlotIndex.getDisplayName();
                }
                tabHost.addTab(buildTabSpec(valueOf, String.valueOf(displayName)));
            }
            SubscriptionInfo activeSubscriptionInfoForSimSlotIndex2 = from.getActiveSubscriptionInfoForSimSlotIndex(0);
            this.mPhone = activeSubscriptionInfoForSimSlotIndex2 == null ? null : PhoneFactory.getPhone(SubscriptionManager.getPhoneId(activeSubscriptionInfoForSimSlotIndex2.getSubscriptionId()));
            StringBuilder sb = new StringBuilder();
            sb.append("onCreateView, phone=");
            sb.append((Object) (this.mPhone == null ? "null" : this.mPhone));
            Log.d("IccLockSettings", sb.toString());
            if (bundle != null && bundle.containsKey("currentTab")) {
                this.mTabHost.setCurrentTabByTag(bundle.getString("currentTab"));
            }
            return inflate;
        }
        this.mPhone = PhoneFactory.getDefaultPhone();
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        updatePreferences();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePreferences() {
        boolean z = false;
        if (this.mPinDialog != null) {
            this.mPinDialog.setEnabled((this.mPhone == null || this.mIsAirplaneModeOn) ? false : true);
        }
        if (this.mPinToggle != null) {
            SwitchPreference switchPreference = this.mPinToggle;
            if (this.mPhone != null && !this.mIsAirplaneModeOn) {
                z = true;
            }
            switchPreference.setEnabled(z);
            if (this.mPhone != null) {
                boolean iccLockEnabled = this.mPhone.getIccCard().getIccLockEnabled();
                Log.d("IccLockSettings", "iccLockEnabled=" + iccLockEnabled);
                this.mPinToggle.setChecked(iccLockEnabled);
            }
        }
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 56;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        getContext().registerReceiver(this.mSimStateReceiver, intentFilter);
        this.mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getActivity());
        updatePreferences();
        if (this.mDialogState != 0) {
            showPinDialog();
        } else {
            resetDialogState();
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(this.mSimStateReceiver);
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_icc_lock;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        if (this.mPinDialog.isDialogOpen()) {
            bundle.putInt("dialogState", this.mDialogState);
            bundle.putString("dialogPin", this.mPinDialog.getEditText().getText().toString());
            bundle.putString("dialogError", this.mError);
            bundle.putBoolean("enableState", this.mToState);
            switch (this.mDialogState) {
                case 3:
                    bundle.putString("oldPinCode", this.mOldPin);
                    break;
                case 4:
                    bundle.putString("oldPinCode", this.mOldPin);
                    bundle.putString("newPinCode", this.mNewPin);
                    break;
            }
        } else {
            super.onSaveInstanceState(bundle);
        }
        if (this.mTabHost != null) {
            bundle.putString("currentTab", this.mTabHost.getCurrentTabTag());
        }
    }

    private void showPinDialog() {
        if (this.mDialogState == 0) {
            return;
        }
        setDialogValues();
        this.mPinDialog.showPinDialog();
        EditText editText = this.mPinDialog.getEditText();
        if (!TextUtils.isEmpty(this.mPin) && editText != null) {
            editText.setSelection(this.mPin.length());
        }
    }

    private void setDialogValues() {
        String string;
        this.mPinDialog.setText(this.mPin);
        String str = "";
        switch (this.mDialogState) {
            case 1:
                str = this.mRes.getString(R.string.sim_enter_pin);
                EditPinPreference editPinPreference = this.mPinDialog;
                if (this.mToState) {
                    string = this.mRes.getString(R.string.sim_enable_sim_lock);
                } else {
                    string = this.mRes.getString(R.string.sim_disable_sim_lock);
                }
                editPinPreference.setDialogTitle(string);
                break;
            case 2:
                str = this.mRes.getString(R.string.sim_enter_old);
                this.mPinDialog.setDialogTitle(this.mRes.getString(R.string.sim_change_pin));
                break;
            case 3:
                str = this.mRes.getString(R.string.sim_enter_new);
                this.mPinDialog.setDialogTitle(this.mRes.getString(R.string.sim_change_pin));
                break;
            case 4:
                str = this.mRes.getString(R.string.sim_reenter_new);
                this.mPinDialog.setDialogTitle(this.mRes.getString(R.string.sim_change_pin));
                break;
        }
        if (this.mError != null) {
            str = this.mError + "\n" + str;
            this.mError = null;
        }
        Log.d("IccLockSettings", "setDialogValues, dialogState=" + this.mDialogState);
        this.mPinDialog.setDialogMessage(str);
    }

    @Override // com.android.settings.EditPinPreference.OnPinEnteredListener
    public void onPinEntered(EditPinPreference editPinPreference, boolean z) {
        if (!z) {
            resetDialogState();
            return;
        }
        this.mPin = editPinPreference.getText();
        if (!reasonablePin(this.mPin)) {
            this.mError = this.mRes.getString(R.string.sim_bad_pin);
            if (isResumed()) {
                showPinDialog();
                return;
            }
            return;
        }
        switch (this.mDialogState) {
            case 1:
                tryChangeIccLockState();
                return;
            case 2:
                this.mOldPin = this.mPin;
                this.mDialogState = 3;
                this.mError = null;
                this.mPin = null;
                showPinDialog();
                return;
            case 3:
                this.mNewPin = this.mPin;
                this.mDialogState = 4;
                this.mPin = null;
                showPinDialog();
                return;
            case 4:
                if (!this.mPin.equals(this.mNewPin)) {
                    this.mError = this.mRes.getString(R.string.sim_pins_dont_match);
                    this.mDialogState = 3;
                    this.mPin = null;
                    showPinDialog();
                    return;
                }
                this.mError = null;
                tryChangePin();
                return;
            default:
                return;
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mPinToggle) {
            this.mToState = this.mPinToggle.isChecked();
            this.mPinToggle.setChecked(!this.mToState);
            this.mDialogState = 1;
            showPinDialog();
        } else if (preference == this.mPinDialog) {
            this.mDialogState = 2;
            return false;
        }
        return true;
    }

    private void tryChangeIccLockState() {
        Message obtain = Message.obtain(this.mHandler, 100, this.mPhone);
        if (this.mPhone != null) {
            Log.d("IccLockSettings", "tryChangeIccLockState, toState=" + this.mToState);
            this.mPhone.getIccCard().setIccLockEnabled(this.mToState, this.mPin, obtain);
            this.mPinToggle.setEnabled(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void iccLockChanged(Throwable th, int i, Phone phone) {
        Log.d("IccLockSettings", "iccLockChanged, exception=" + th + ", attemptsRemaining=" + i);
        boolean z = false;
        boolean z2 = th == null;
        if (this.mPhone != null && this.mPhone.equals(phone)) {
            z = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("iccLockChanged, success=");
        sb.append(z2);
        sb.append(", matched=");
        sb.append(z);
        sb.append(", currentPhone=");
        sb.append((Object) (this.mPhone == null ? "null" : this.mPhone));
        sb.append(", oldPhone=");
        sb.append(phone);
        Log.d("IccLockSettings", sb.toString());
        if (z2 && z) {
            this.mPinToggle.setChecked(this.mToState);
            UtilsExt.getSimRoamingExt(getActivity()).showPinToast(this.mToState);
        } else if (!z2 && getContext() != null) {
            Toast.makeText(getContext(), getPinPasswordErrorMessage(i, th), 1).show();
        }
        if (!z) {
            return;
        }
        this.mPinToggle.setEnabled(!this.mIsAirplaneModeOn);
        resetDialogState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void iccPinChanged(Throwable th, int i, Phone phone) {
        Log.d("IccLockSettings", "iccPinChanged, exception=" + th + ", attemptsRemaining=" + i);
        boolean z = th == null;
        boolean z2 = this.mPhone != null && this.mPhone.equals(phone);
        StringBuilder sb = new StringBuilder();
        sb.append("iccPinChanged, success=");
        sb.append(z);
        sb.append(", matched=");
        sb.append(z2);
        sb.append(", currPhone=");
        sb.append((Object) (this.mPhone == null ? "null" : this.mPhone));
        sb.append(", oldPhone=");
        sb.append(phone);
        Log.d("IccLockSettings", sb.toString());
        if (!z) {
            Toast.makeText(getContext(), getPinPasswordErrorMessage(i, th), 1).show();
        } else {
            Toast.makeText(getContext(), this.mRes.getString(R.string.sim_change_succeeded), 0).show();
        }
        if (!z2) {
            return;
        }
        resetDialogState();
    }

    private void tryChangePin() {
        if (this.mPhone != null) {
            this.mPhone.getIccCard().changeIccLockPassword(this.mOldPin, this.mNewPin, Message.obtain(this.mHandler, 101, this.mPhone));
            Log.d("IccLockSettings", "tryChangePin, change pin.");
        }
    }

    private String getPinPasswordErrorMessage(int i, Throwable th) {
        String string;
        if (th instanceof CommandException) {
            CommandException commandException = (CommandException) th;
            if (commandException.getCommandError() == CommandException.Error.GENERIC_FAILURE || commandException.getCommandError() == CommandException.Error.SIM_ERR) {
                string = this.mRes.getString(R.string.pin_failed);
                Log.d("IccLockSettings", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
                return string;
            }
        }
        if (i == 0) {
            string = this.mRes.getString(R.string.wrong_pin_code_pukked);
        } else if (i > 0) {
            string = this.mRes.getQuantityString(R.plurals.wrong_pin_code, i, Integer.valueOf(i));
        } else {
            string = this.mRes.getString(R.string.pin_failed);
        }
        Log.d("IccLockSettings", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
        return string;
    }

    private boolean reasonablePin(String str) {
        if (str == null || str.length() < 4 || str.length() > 8) {
            return false;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetDialogState() {
        this.mError = null;
        this.mDialogState = 2;
        this.mPin = "";
        setDialogValues();
        this.mDialogState = 0;
    }

    private TabHost.TabSpec buildTabSpec(String str, String str2) {
        return this.mTabHost.newTabSpec(str).setIndicator(str2).setContent(this.mEmptyTabContent);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.mSimHotSwapHandler != null) {
            this.mSimHotSwapHandler.unregisterOnSimHotSwap();
        }
        super.onDestroy();
    }
}
