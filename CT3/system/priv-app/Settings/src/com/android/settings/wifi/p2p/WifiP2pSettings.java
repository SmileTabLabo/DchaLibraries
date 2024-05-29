package com.android.settings.wifi.p2p;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
/* loaded from: classes.dex */
public class WifiP2pSettings extends SettingsPreferenceFragment implements WifiP2pManager.PersistentGroupInfoListener, WifiP2pManager.PeerListListener {
    private DialogInterface.OnClickListener mCancelConnectListener;
    private WifiP2pManager.Channel mChannel;
    private int mConnectedDevices;
    private DialogInterface.OnClickListener mDeleteGroupListener;
    private EditText mDeviceNameText;
    private DialogInterface.OnClickListener mDisconnectListener;
    private PreferenceGroup mPeersGroup;
    private PreferenceGroup mPersistentGroup;
    private DialogInterface.OnClickListener mRenameListener;
    private String mSavedDeviceName;
    private WifiP2pPersistentGroup mSelectedGroup;
    private String mSelectedGroupName;
    private WifiP2pPeer mSelectedWifiPeer;
    private WifiP2pDevice mThisDevice;
    private Preference mThisDevicePref;
    private boolean mWifiP2pEnabled;
    private WifiP2pManager mWifiP2pManager;
    private boolean mWifiP2pSearching;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private boolean mLastGroupFormed = false;
    private WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("WifiP2pSettings", "receive action: " + action);
            if ("android.net.wifi.p2p.STATE_CHANGED".equals(action)) {
                WifiP2pSettings.this.mWifiP2pEnabled = intent.getIntExtra("wifi_p2p_state", 1) == 2;
                WifiP2pSettings.this.handleP2pStateChanged();
            } else if ("android.net.wifi.p2p.PEERS_CHANGED".equals(action)) {
                WifiP2pSettings.this.mPeers = (WifiP2pDeviceList) intent.getParcelableExtra("wifiP2pDeviceList");
                WifiP2pSettings.this.handlePeersChanged();
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                if (WifiP2pSettings.this.mWifiP2pManager == null) {
                    return;
                }
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                WifiP2pInfo wifip2pinfo = (WifiP2pInfo) intent.getParcelableExtra("wifiP2pInfo");
                if (networkInfo.isConnected()) {
                    Log.d("WifiP2pSettings", "Connected");
                } else if (!WifiP2pSettings.this.mLastGroupFormed) {
                    WifiP2pSettings.this.startSearch();
                }
                WifiP2pSettings.this.mLastGroupFormed = wifip2pinfo.groupFormed;
            } else if ("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(action)) {
                WifiP2pSettings.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                Log.d("WifiP2pSettings", "Update device info: " + WifiP2pSettings.this.mThisDevice);
                WifiP2pSettings.this.updateDevicePref();
            } else if ("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE".equals(action)) {
                int discoveryState = intent.getIntExtra("discoveryState", 1);
                Log.d("WifiP2pSettings", "Discovery state changed: " + discoveryState);
                if (discoveryState == 2) {
                    WifiP2pSettings.this.updateSearchMenu(true);
                } else {
                    WifiP2pSettings.this.updateSearchMenu(false);
                }
            } else if (!"android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED".equals(action) || WifiP2pSettings.this.mWifiP2pManager == null) {
            } else {
                WifiP2pSettings.this.mWifiP2pManager.requestPersistentGroupInfo(WifiP2pSettings.this.mChannel, WifiP2pSettings.this);
            }
        }
    };

    public WifiP2pSettings() {
        Log.d("WifiP2pSettings", "Creating WifiP2pSettings ...");
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.wifi_p2p_settings);
        this.mIntentFilter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED");
        Activity activity = getActivity();
        this.mWifiP2pManager = (WifiP2pManager) getSystemService("wifip2p");
        if (this.mWifiP2pManager != null) {
            this.mChannel = this.mWifiP2pManager.initialize(activity, getActivity().getMainLooper(), null);
            if (this.mChannel == null) {
                Log.e("WifiP2pSettings", "Failed to set up connection with wifi p2p service");
                this.mWifiP2pManager = null;
            }
        } else {
            Log.e("WifiP2pSettings", "mWifiP2pManager is null !");
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("PEER_STATE")) {
            WifiP2pDevice device = (WifiP2pDevice) savedInstanceState.getParcelable("PEER_STATE");
            this.mSelectedWifiPeer = new WifiP2pPeer(getActivity(), device);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("DEV_NAME")) {
            this.mSavedDeviceName = savedInstanceState.getString("DEV_NAME");
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("GROUP_NAME")) {
            this.mSelectedGroupName = savedInstanceState.getString("GROUP_NAME");
        }
        this.mRenameListener = new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1 || WifiP2pSettings.this.mWifiP2pManager == null) {
                    return;
                }
                String name = WifiP2pSettings.this.mDeviceNameText.getText().toString();
                if (name != null) {
                    for (int i = 0; i < name.length(); i++) {
                        char cur = name.charAt(i);
                        if (!Character.isDigit(cur) && !Character.isLetter(cur) && cur != '-' && cur != '_' && cur != ' ') {
                            Toast.makeText(WifiP2pSettings.this.getActivity(), (int) R.string.wifi_p2p_failed_rename_message, 1).show();
                            return;
                        }
                    }
                }
                WifiP2pSettings.this.mWifiP2pManager.setDeviceName(WifiP2pSettings.this.mChannel, WifiP2pSettings.this.mDeviceNameText.getText().toString(), new WifiP2pManager.ActionListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.2.1
                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onSuccess() {
                        Log.d("WifiP2pSettings", " device rename success");
                    }

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onFailure(int reason) {
                        Toast.makeText(WifiP2pSettings.this.getActivity(), (int) R.string.wifi_p2p_failed_rename_message, 1).show();
                    }
                });
            }
        };
        this.mDisconnectListener = new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1 || WifiP2pSettings.this.mWifiP2pManager == null) {
                    return;
                }
                WifiP2pSettings.this.mWifiP2pManager.removeGroup(WifiP2pSettings.this.mChannel, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.3.1
                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onSuccess() {
                        Log.d("WifiP2pSettings", " remove group success");
                    }

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onFailure(int reason) {
                        Log.d("WifiP2pSettings", " remove group fail " + reason);
                    }
                });
            }
        };
        this.mCancelConnectListener = new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1 || WifiP2pSettings.this.mWifiP2pManager == null) {
                    return;
                }
                WifiP2pSettings.this.mWifiP2pManager.cancelConnect(WifiP2pSettings.this.mChannel, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.4.1
                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onSuccess() {
                        Log.d("WifiP2pSettings", " cancel connect success");
                    }

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onFailure(int reason) {
                        Log.d("WifiP2pSettings", " cancel connect fail " + reason);
                    }
                });
            }
        };
        this.mDeleteGroupListener = new DialogInterface.OnClickListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (WifiP2pSettings.this.mWifiP2pManager == null) {
                        return;
                    }
                    if (WifiP2pSettings.this.mSelectedGroup != null) {
                        Log.d("WifiP2pSettings", " deleting group " + WifiP2pSettings.this.mSelectedGroup.getGroupName());
                        WifiP2pSettings.this.mWifiP2pManager.deletePersistentGroup(WifiP2pSettings.this.mChannel, WifiP2pSettings.this.mSelectedGroup.getNetworkId(), new WifiP2pManager.ActionListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.5.1
                            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                            public void onSuccess() {
                                Log.d("WifiP2pSettings", " delete group success");
                            }

                            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                            public void onFailure(int reason) {
                                Log.d("WifiP2pSettings", " delete group fail " + reason);
                            }
                        });
                        WifiP2pSettings.this.mSelectedGroup = null;
                        return;
                    }
                    Log.w("WifiP2pSettings", " No selected group to delete!");
                } else if (which != -2) {
                } else {
                    Log.d("WifiP2pSettings", " forgetting selected group " + WifiP2pSettings.this.mSelectedGroup.getGroupName());
                    WifiP2pSettings.this.mSelectedGroup = null;
                }
            }
        };
        setHasOptionsMenu(true);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        preferenceScreen.setOrderingAsAdded(true);
        this.mThisDevicePref = new Preference(getPrefContext());
        this.mThisDevicePref.setPersistent(false);
        this.mThisDevicePref.setSelectable(false);
        preferenceScreen.addPreference(this.mThisDevicePref);
        this.mPeersGroup = new PreferenceCategory(getPrefContext());
        this.mPeersGroup.setTitle(R.string.wifi_p2p_peer_devices);
        preferenceScreen.addPreference(this.mPeersGroup);
        this.mPersistentGroup = new PreferenceCategory(getPrefContext());
        this.mPersistentGroup.setTitle(R.string.wifi_p2p_remembered_groups);
        preferenceScreen.addPreference(this.mPersistentGroup);
        super.onActivityCreated(savedInstanceState);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
        if (this.mWifiP2pManager == null) {
            return;
        }
        this.mWifiP2pManager.requestPeers(this.mChannel, this);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        if (this.mWifiP2pManager != null) {
            this.mWifiP2pManager.stopPeerDiscovery(this.mChannel, null);
        }
        getActivity().unregisterReceiver(this.mReceiver);
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        this.mWifiP2pManager.deinitialize(this.mChannel);
        super.onDestroy();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int textId = this.mWifiP2pSearching ? R.string.wifi_p2p_menu_searching : R.string.wifi_p2p_menu_search;
        menu.add(0, 1, 0, textId).setEnabled(this.mWifiP2pEnabled).setShowAsAction(1);
        menu.add(0, 2, 0, R.string.wifi_p2p_menu_rename).setEnabled(this.mWifiP2pEnabled).setShowAsAction(1);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override // android.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        MenuItem searchMenu = menu.findItem(1);
        MenuItem renameMenu = menu.findItem(2);
        if (this.mWifiP2pEnabled && !this.mWifiP2pSearching) {
            z = true;
        }
        searchMenu.setEnabled(z);
        renameMenu.setEnabled(this.mWifiP2pEnabled);
        if (this.mWifiP2pSearching) {
            searchMenu.setTitle(R.string.wifi_p2p_menu_searching);
        } else {
            searchMenu.setTitle(R.string.wifi_p2p_menu_search);
        }
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                startSearch();
                return true;
            case 2:
                showDialog(3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof WifiP2pPeer) {
            this.mSelectedWifiPeer = (WifiP2pPeer) preference;
            if (this.mSelectedWifiPeer.device.status == 0) {
                showDialog(1);
            } else if (this.mSelectedWifiPeer.device.status == 1) {
                showDialog(2);
            } else {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = this.mSelectedWifiPeer.device.deviceAddress;
                int forceWps = SystemProperties.getInt("wifidirect.wps", -1);
                if (forceWps != -1) {
                    config.wps.setup = forceWps;
                } else if (this.mSelectedWifiPeer.device.wpsPbcSupported()) {
                    config.wps.setup = 0;
                } else if (this.mSelectedWifiPeer.device.wpsKeypadSupported()) {
                    config.wps.setup = 2;
                } else {
                    config.wps.setup = 1;
                }
                this.mWifiP2pManager.connect(this.mChannel, config, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.6
                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onSuccess() {
                        Log.d("WifiP2pSettings", " connect success");
                    }

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onFailure(int reason) {
                        Log.e("WifiP2pSettings", " connect fail " + reason);
                        Toast.makeText(WifiP2pSettings.this.getActivity(), (int) R.string.wifi_p2p_failed_connect_message, 0).show();
                    }
                });
            }
        } else if (preference instanceof WifiP2pPersistentGroup) {
            this.mSelectedGroup = (WifiP2pPersistentGroup) preference;
            showDialog(4);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int id) {
        if (id == 1) {
            String deviceName = TextUtils.isEmpty(this.mSelectedWifiPeer.device.deviceName) ? this.mSelectedWifiPeer.device.deviceAddress : this.mSelectedWifiPeer.device.deviceName;
            String msg = this.mConnectedDevices > 1 ? getActivity().getString(R.string.wifi_p2p_disconnect_multiple_message, new Object[]{deviceName, Integer.valueOf(this.mConnectedDevices - 1)}) : getActivity().getString(R.string.wifi_p2p_disconnect_message, new Object[]{deviceName});
            AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.wifi_p2p_disconnect_title).setMessage(msg).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mDisconnectListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), (DialogInterface.OnClickListener) null).create();
            return dialog;
        } else if (id == 2) {
            AlertDialog dialog2 = new AlertDialog.Builder(getActivity()).setTitle(R.string.wifi_p2p_cancel_connect_title).setMessage(getActivity().getString(R.string.wifi_p2p_cancel_connect_message, new Object[]{TextUtils.isEmpty(this.mSelectedWifiPeer.device.deviceName) ? this.mSelectedWifiPeer.device.deviceAddress : this.mSelectedWifiPeer.device.deviceName})).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mCancelConnectListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), (DialogInterface.OnClickListener) null).create();
            return dialog2;
        } else if (id != 3) {
            if (id == 4) {
                AlertDialog dialog3 = new AlertDialog.Builder(getActivity()).setMessage(getActivity().getString(R.string.wifi_p2p_delete_group_message)).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mDeleteGroupListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), this.mDeleteGroupListener).create();
                return dialog3;
            }
            return null;
        } else {
            this.mDeviceNameText = new EditText(getActivity());
            this.mDeviceNameText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
            this.mDeviceNameText.setTextDirection(5);
            if (this.mSavedDeviceName != null) {
                this.mDeviceNameText.setText(this.mSavedDeviceName);
                this.mDeviceNameText.setSelection(this.mSavedDeviceName.length());
            } else if (this.mThisDevice != null && !TextUtils.isEmpty(this.mThisDevice.deviceName)) {
                this.mDeviceNameText.setText(this.mThisDevice.deviceName);
                this.mDeviceNameText.setSelection(0, this.mThisDevice.deviceName.length());
            }
            this.mSavedDeviceName = null;
            AlertDialog dialog4 = new AlertDialog.Builder(getActivity()).setTitle(R.string.wifi_p2p_menu_rename).setView(this.mDeviceNameText).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mRenameListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), (DialogInterface.OnClickListener) null).create();
            return dialog4;
        }
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 109;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        if (this.mSelectedWifiPeer != null) {
            outState.putParcelable("PEER_STATE", this.mSelectedWifiPeer.device);
        }
        if (this.mDeviceNameText != null) {
            outState.putString("DEV_NAME", this.mDeviceNameText.getText().toString());
        }
        if (this.mSelectedGroup == null) {
            return;
        }
        outState.putString("GROUP_NAME", this.mSelectedGroup.getGroupName());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePeersChanged() {
        this.mPeersGroup.removeAll();
        this.mConnectedDevices = 0;
        Log.d("WifiP2pSettings", "List of available peers");
        for (WifiP2pDevice peer : this.mPeers.getDeviceList()) {
            Log.d("WifiP2pSettings", "-> " + peer);
            this.mPeersGroup.addPreference(new WifiP2pPeer(getActivity(), peer));
            if (peer.status == 0) {
                this.mConnectedDevices++;
            }
        }
        Log.d("WifiP2pSettings", " mConnectedDevices " + this.mConnectedDevices);
    }

    public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups) {
        this.mPersistentGroup.removeAll();
        for (WifiP2pGroup group : groups.getGroupList()) {
            Log.d("WifiP2pSettings", " group " + group);
            WifiP2pPersistentGroup wppg = new WifiP2pPersistentGroup(getActivity(), group);
            this.mPersistentGroup.addPreference(wppg);
            if (wppg.getGroupName().equals(this.mSelectedGroupName)) {
                Log.d("WifiP2pSettings", "Selecting group " + wppg.getGroupName());
                this.mSelectedGroup = wppg;
                this.mSelectedGroupName = null;
            }
        }
        if (this.mSelectedGroupName == null) {
            return;
        }
        Log.w("WifiP2pSettings", " Selected group " + this.mSelectedGroupName + " disappered on next query ");
    }

    @Override // android.net.wifi.p2p.WifiP2pManager.PeerListListener
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.d("WifiP2pSettings", "Requested peers are available");
        this.mPeers = peers;
        handlePeersChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleP2pStateChanged() {
        updateSearchMenu(false);
        this.mThisDevicePref.setEnabled(this.mWifiP2pEnabled);
        this.mPeersGroup.setEnabled(this.mWifiP2pEnabled);
        this.mPersistentGroup.setEnabled(this.mWifiP2pEnabled);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSearchMenu(boolean searching) {
        this.mWifiP2pSearching = searching;
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSearch() {
        if (this.mWifiP2pManager == null || this.mWifiP2pSearching) {
            return;
        }
        this.mWifiP2pManager.discoverPeers(this.mChannel, new WifiP2pManager.ActionListener() { // from class: com.android.settings.wifi.p2p.WifiP2pSettings.7
            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onSuccess() {
            }

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onFailure(int reason) {
                Log.d("WifiP2pSettings", " discover fail " + reason);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDevicePref() {
        if (this.mThisDevice == null) {
            return;
        }
        if (TextUtils.isEmpty(this.mThisDevice.deviceName)) {
            this.mThisDevicePref.setTitle(this.mThisDevice.deviceAddress);
        } else {
            this.mThisDevicePref.setTitle(this.mThisDevice.deviceName);
        }
    }
}
