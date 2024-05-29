package com.android.settings.backup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.ToggleSwitch;
/* loaded from: classes.dex */
public class ToggleBackupSettingFragment extends SettingsPreferenceFragment implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    private IBackupManager mBackupManager;
    private Dialog mConfirmDialog;
    private Preference mSummaryPreference;
    protected SwitchBar mSwitchBar;
    protected ToggleSwitch mToggleSwitch;
    private boolean mWaitingForConfirmationDialog = false;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBackupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(preferenceScreen);
        this.mSummaryPreference = new Preference(getPrefContext()) { // from class: com.android.settings.backup.ToggleBackupSettingFragment.1
            @Override // android.support.v7.preference.Preference
            public void onBindViewHolder(PreferenceViewHolder view) {
                super.onBindViewHolder(view);
                TextView summaryView = (TextView) view.findViewById(16908304);
                summaryView.setText(getSummary());
            }
        };
        this.mSummaryPreference.setPersistent(false);
        this.mSummaryPreference.setLayoutResource(R.layout.text_description_preference);
        preferenceScreen.addPreference(this.mSummaryPreference);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mSwitchBar = activity.getSwitchBar();
        this.mToggleSwitch = this.mSwitchBar.getSwitch();
        if (Settings.Secure.getInt(getContentResolver(), "user_full_data_backup_aware", 0) != 0) {
            this.mSummaryPreference.setSummary(R.string.fullbackup_data_summary);
        } else {
            this.mSummaryPreference.setSummary(R.string.backup_data_summary);
        }
        try {
            this.mSwitchBar.setCheckedInternal(this.mBackupManager == null ? false : this.mBackupManager.isBackupEnabled());
        } catch (RemoteException e) {
            this.mSwitchBar.setEnabled(false);
        }
        getActivity().setTitle(R.string.backup_data_title);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(null);
        this.mSwitchBar.hide();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(new ToggleSwitch.OnBeforeCheckedChangeListener() { // from class: com.android.settings.backup.ToggleBackupSettingFragment.2
            @Override // com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                if (!checked) {
                    ToggleBackupSettingFragment.this.showEraseBackupDialog();
                    return true;
                }
                ToggleBackupSettingFragment.this.setBackupEnabled(true);
                ToggleBackupSettingFragment.this.mSwitchBar.setCheckedInternal(true);
                return true;
            }
        });
        this.mSwitchBar.show();
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        if (this.mConfirmDialog != null && this.mConfirmDialog.isShowing()) {
            this.mConfirmDialog.dismiss();
        }
        this.mConfirmDialog = null;
        super.onStop();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            this.mWaitingForConfirmationDialog = false;
            setBackupEnabled(false);
            this.mSwitchBar.setCheckedInternal(false);
        } else if (which != -2) {
        } else {
            this.mWaitingForConfirmationDialog = false;
            setBackupEnabled(true);
            this.mSwitchBar.setCheckedInternal(true);
        }
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialog) {
        if (!this.mWaitingForConfirmationDialog) {
            return;
        }
        setBackupEnabled(true);
        this.mSwitchBar.setCheckedInternal(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showEraseBackupDialog() {
        CharSequence msg = Settings.Secure.getInt(getContentResolver(), "user_full_data_backup_aware", 0) != 0 ? getResources().getText(R.string.fullbackup_erase_dialog_message) : getResources().getText(R.string.backup_erase_dialog_message);
        this.mWaitingForConfirmationDialog = true;
        this.mConfirmDialog = new AlertDialog.Builder(getActivity()).setMessage(msg).setTitle(R.string.backup_erase_dialog_title).setPositiveButton(17039370, this).setNegativeButton(17039360, this).setOnDismissListener(this).show();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 81;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setBackupEnabled(boolean enable) {
        if (this.mBackupManager == null) {
            return;
        }
        try {
            this.mBackupManager.setBackupEnabled(enable);
        } catch (RemoteException e) {
            Log.e("ToggleBackupSettingFragment", "Error communicating with BackupManager", e);
        }
    }
}
