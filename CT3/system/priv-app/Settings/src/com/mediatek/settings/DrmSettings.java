package com.mediatek.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.drm.DrmManagerClient;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
/* loaded from: classes.dex */
public class DrmSettings extends SettingsPreferenceFragment {
    private static DrmManagerClient sClient;
    private static Preference sPreferenceReset;
    private Context mContext;

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 81;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.drm_settings);
        sPreferenceReset = findPreference("drm_settings");
        this.mContext = getActivity();
        sClient = new DrmManagerClient(this.mContext);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        switch (id) {
            case 1000:
                builder.setMessage(getResources().getString(R.string.drm_reset_dialog_msg));
                builder.setTitle(getResources().getString(R.string.drm_settings_title));
                builder.setIcon(17301543);
                builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.mediatek.settings.DrmSettings.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (DrmSettings.sClient == null) {
                            return;
                        }
                        int result = DrmSettings.sClient.removeAllRights();
                        if (result == 0) {
                            Toast.makeText(DrmSettings.this.mContext, (int) R.string.drm_reset_toast_msg, 0).show();
                            DrmSettings.sPreferenceReset.setEnabled(false);
                        } else {
                            Log.d("DrmSettings", "removeAllRights fail!");
                        }
                        DrmManagerClient unused = DrmSettings.sClient = null;
                    }
                });
                builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null);
                AlertDialog dialog = builder.create();
                return dialog;
            default:
                return null;
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == sPreferenceReset) {
            showDialog(1000);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        sClient = null;
    }
}
