package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.Editable;
import android.widget.EditText;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
/* loaded from: classes.dex */
public class WifiAPITest extends SettingsPreferenceFragment implements Preference.OnPreferenceClickListener {
    private Preference mWifiDisableNetwork;
    private Preference mWifiDisconnect;
    private Preference mWifiEnableNetwork;
    private WifiManager mWifiManager;
    private int netid;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.layout.wifi_api_test);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        this.mWifiDisconnect = preferenceScreen.findPreference("disconnect");
        this.mWifiDisconnect.setOnPreferenceClickListener(this);
        this.mWifiDisableNetwork = preferenceScreen.findPreference("disable_network");
        this.mWifiDisableNetwork.setOnPreferenceClickListener(this);
        this.mWifiEnableNetwork = preferenceScreen.findPreference("enable_network");
        this.mWifiEnableNetwork.setOnPreferenceClickListener(this);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 89;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        return false;
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference pref) {
        if (pref == this.mWifiDisconnect) {
            this.mWifiManager.disconnect();
            return true;
        } else if (pref == this.mWifiDisableNetwork) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            final EditText input = new EditText(getPrefContext());
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.WifiAPITest.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    try {
                        WifiAPITest.this.netid = Integer.parseInt(value.toString());
                        WifiAPITest.this.mWifiManager.disableNetwork(WifiAPITest.this.netid);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.WifiAPITest.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
            return true;
        } else if (pref == this.mWifiEnableNetwork) {
            AlertDialog.Builder alert2 = new AlertDialog.Builder(getContext());
            alert2.setTitle("Input");
            alert2.setMessage("Enter Network ID");
            final EditText input2 = new EditText(getPrefContext());
            alert2.setView(input2);
            alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.WifiAPITest.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input2.getText();
                    WifiAPITest.this.netid = Integer.parseInt(value.toString());
                    WifiAPITest.this.mWifiManager.enableNetwork(WifiAPITest.this.netid, false);
                }
            });
            alert2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.WifiAPITest.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert2.show();
            return true;
        } else {
            return true;
        }
    }
}
