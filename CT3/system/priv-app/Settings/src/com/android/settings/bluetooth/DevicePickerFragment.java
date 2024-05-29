package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.ProgressCategory;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
/* loaded from: classes.dex */
public final class DevicePickerFragment extends DeviceListPreferenceFragment {
    private String mLaunchClass;
    private String mLaunchPackage;
    private boolean mNeedAuth;
    private ProgressCategory mProgressCategory;
    private boolean mStartScanOnResume;

    public DevicePickerFragment() {
        super(null);
    }

    @Override // com.android.settings.bluetooth.DeviceListPreferenceFragment
    void addPreferencesForActivity() {
        addPreferencesFromResource(R.xml.device_picker);
        Intent intent = getActivity().getIntent();
        this.mNeedAuth = intent.getBooleanExtra("android.bluetooth.devicepicker.extra.NEED_AUTH", false);
        setFilter(intent.getIntExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 0));
        this.mLaunchPackage = intent.getStringExtra("android.bluetooth.devicepicker.extra.LAUNCH_PACKAGE");
        this.mLaunchClass = intent.getStringExtra("android.bluetooth.devicepicker.extra.DEVICE_PICKER_LAUNCH_CLASS");
    }

    @Override // com.android.settings.bluetooth.DeviceListPreferenceFragment
    void initDevicePreference(BluetoothDevicePreference preference) {
        preference.setWidgetLayoutResource(R.layout.preference_empty_list);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, R.string.bluetooth_search_for_devices).setEnabled(true).setShowAsAction(0);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                this.mLocalAdapter.startScanning(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 25;
    }

    @Override // com.android.settings.bluetooth.DeviceListPreferenceFragment, com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.device_picker));
        UserManager um = (UserManager) getSystemService("user");
        if (!um.hasUserRestriction("no_config_bluetooth") && savedInstanceState == null) {
            z = true;
        }
        this.mStartScanOnResume = z;
        setHasOptionsMenu(true);
        this.mProgressCategory = (ProgressCategory) findPreference("bt_device_list");
    }

    @Override // com.android.settings.RestrictedSettingsFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (this.mProgressCategory == null) {
            return;
        }
        this.mProgressCategory.removeAll();
    }

    @Override // com.android.settings.bluetooth.DeviceListPreferenceFragment, com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mProgressCategory.setNoDeviceFoundAdded(false);
        removeAllDevices();
        addCachedDevices();
        if (!this.mStartScanOnResume) {
            return;
        }
        this.mLocalAdapter.startScanning(true);
        this.mStartScanOnResume = false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.settings.bluetooth.DeviceListPreferenceFragment
    public void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        this.mLocalAdapter.stopScanning();
        LocalBluetoothPreferences.persistSelectedDeviceInPicker(getActivity(), this.mSelectedDevice.getAddress());
        if (btPreference.getCachedDevice().getBondState() == 12 || !this.mNeedAuth) {
            sendDevicePickedIntent(this.mSelectedDevice);
            finish();
            return;
        }
        super.onDevicePreferenceClick(btPreference);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        if (bondState != 12) {
            return;
        }
        BluetoothDevice device = cachedDevice.getDevice();
        if (!device.equals(this.mSelectedDevice)) {
            return;
        }
        sendDevicePickedIntent(device);
        finish();
    }

    @Override // com.android.settings.bluetooth.DeviceListPreferenceFragment, com.android.settingslib.bluetooth.BluetoothCallback
    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        if (bluetoothState != 12) {
            return;
        }
        this.mLocalAdapter.startScanning(false);
    }

    private void sendDevicePickedIntent(BluetoothDevice device) {
        Intent intent = new Intent("android.bluetooth.devicepicker.action.DEVICE_SELECTED");
        intent.putExtra("android.bluetooth.device.extra.DEVICE", device);
        if (this.mLaunchPackage != null && this.mLaunchClass != null) {
            intent.setClassName(this.mLaunchPackage, this.mLaunchClass);
        }
        getActivity().sendBroadcast(intent);
    }
}
