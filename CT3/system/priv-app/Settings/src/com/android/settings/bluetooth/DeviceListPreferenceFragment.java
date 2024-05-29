package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import java.util.Collection;
import java.util.WeakHashMap;
/* loaded from: classes.dex */
public abstract class DeviceListPreferenceFragment extends RestrictedSettingsFragment implements BluetoothCallback {
    private PreferenceGroup mDeviceListGroup;
    final WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference> mDevicePreferenceMap;
    private BluetoothDeviceFilter.Filter mFilter;
    LocalBluetoothAdapter mLocalAdapter;
    LocalBluetoothManager mLocalManager;
    BluetoothDevice mSelectedDevice;

    abstract void addPreferencesForActivity();

    /* JADX INFO: Access modifiers changed from: package-private */
    public DeviceListPreferenceFragment(String restrictedKey) {
        super(restrictedKey);
        this.mDevicePreferenceMap = new WeakHashMap<>();
        this.mFilter = BluetoothDeviceFilter.ALL_FILTER;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void setFilter(BluetoothDeviceFilter.Filter filter) {
        this.mFilter = filter;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void setFilter(int filterType) {
        this.mFilter = BluetoothDeviceFilter.getFilter(filterType);
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLocalManager = Utils.getLocalBtManager(getActivity());
        if (this.mLocalManager == null) {
            Log.e("DeviceListPreferenceFragment", "Bluetooth is not supported on this device");
            return;
        }
        this.mLocalAdapter = this.mLocalManager.getBluetoothAdapter();
        addPreferencesForActivity();
        this.mDeviceListGroup = (PreferenceCategory) findPreference("bt_device_list");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDeviceListGroup(PreferenceGroup preferenceGroup) {
        this.mDeviceListGroup = preferenceGroup;
    }

    @Override // com.android.settings.RestrictedSettingsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mLocalManager == null || isUiRestricted()) {
            return;
        }
        this.mLocalManager.setForegroundActivity(getActivity());
        this.mLocalManager.getEventManager().registerCallback(this);
        updateProgressUi(this.mLocalAdapter.isDiscovering());
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        if (this.mLocalManager == null || isUiRestricted()) {
            return;
        }
        removeAllDevices();
        this.mLocalManager.setForegroundActivity(null);
        this.mLocalManager.getEventManager().unregisterCallback(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeAllDevices() {
        this.mLocalAdapter.stopScanning();
        this.mDevicePreferenceMap.clear();
        this.mDeviceListGroup.removeAll();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addCachedDevices() {
        Collection<CachedBluetoothDevice> cachedDevices = this.mLocalManager.getCachedDeviceManager().getCachedDevicesCopy();
        for (CachedBluetoothDevice cachedDevice : cachedDevices) {
            onDeviceAdded(cachedDevice);
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if ("bt_scan".equals(preference.getKey())) {
            this.mLocalAdapter.startScanning(true);
            return true;
        } else if (preference instanceof BluetoothDevicePreference) {
            BluetoothDevicePreference btPreference = (BluetoothDevicePreference) preference;
            CachedBluetoothDevice device = btPreference.getCachedDevice();
            this.mSelectedDevice = device.getDevice();
            onDevicePreferenceClick(btPreference);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        btPreference.onClicked();
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        Log.d("DeviceListPreferenceFragment", "onDeviceAdded, Device name is " + cachedDevice.getName());
        if (this.mDevicePreferenceMap.get(cachedDevice) != null) {
            Log.d("DeviceListPreferenceFragment", "Device name " + cachedDevice.getName() + " already have preference");
        } else if (this.mLocalAdapter.getBluetoothState() != 12 || !this.mFilter.matches(cachedDevice.getDevice())) {
        } else {
            Log.d("DeviceListPreferenceFragment", "Device name " + cachedDevice.getName() + " create new preference");
            createDevicePreference(cachedDevice);
        }
    }

    void createDevicePreference(CachedBluetoothDevice cachedDevice) {
        if (this.mDeviceListGroup == null) {
            Log.w("DeviceListPreferenceFragment", "Trying to create a device preference before the list group/category exists!");
            return;
        }
        String key = cachedDevice.getDevice().getAddress();
        BluetoothDevicePreference preference = (BluetoothDevicePreference) getCachedPreference(key);
        if (preference == null) {
            preference = new BluetoothDevicePreference(getPrefContext(), cachedDevice);
            preference.setKey(key);
            this.mDeviceListGroup.addPreference(preference);
        } else {
            preference.rebind();
        }
        initDevicePreference(preference);
        this.mDevicePreferenceMap.put(cachedDevice, preference);
    }

    void initDevicePreference(BluetoothDevicePreference preference) {
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = this.mDevicePreferenceMap.remove(cachedDevice);
        if (preference == null) {
            return;
        }
        this.mDeviceListGroup.removePreference(preference);
    }

    public void onScanningStateChanged(boolean started) {
        Log.d("DeviceListPreferenceFragment", "onScanningStateChanged " + started);
        updateProgressUi(started);
    }

    private void updateProgressUi(boolean start) {
        if (!(this.mDeviceListGroup instanceof BluetoothProgressCategory)) {
            return;
        }
        ((BluetoothProgressCategory) this.mDeviceListGroup).setProgress(start);
        Log.d("DeviceListPreferenceFragment", "setProgress " + start);
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == 13) {
            Log.d("DeviceListPreferenceFragment", "BT state become to TURNING_OFF");
            updateProgressUi(false);
        } else if (bluetoothState == 10) {
            long disableEndTime = System.currentTimeMillis();
            Log.d("BtPerformanceTest", "[Performance test][Settings][Bt] Bluetooth disable end [" + disableEndTime + "]");
        } else if (bluetoothState != 12) {
        } else {
            long enableEndTime = System.currentTimeMillis();
            Log.d("BtPerformanceTest", "[Performance test][Settings][Bt] Bluetooth enable end [" + enableEndTime + "]");
        }
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }
}
