package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/bluetooth/CachedBluetoothDevice.class */
public class CachedBluetoothDevice implements Comparable<CachedBluetoothDevice> {
    private BluetoothClass mBtClass;
    private boolean mConnectAfterPairing;
    private long mConnectAttempted;
    private final Context mContext;
    private final BluetoothDevice mDevice;
    private boolean mIsConnectingErrorPossible;
    private final LocalBluetoothAdapter mLocalAdapter;
    private boolean mLocalNapRoleConnected;
    private int mMessageRejectionCount;
    private String mName;
    private final LocalBluetoothProfileManager mProfileManager;
    private short mRssi;
    private boolean mVisible;
    private final List<LocalBluetoothProfile> mProfiles = new ArrayList();
    private final List<LocalBluetoothProfile> mRemovedProfiles = new ArrayList();
    private final Collection<Callback> mCallbacks = new ArrayList();
    private HashMap<LocalBluetoothProfile, Integer> mProfileConnectionState = new HashMap<>();

    /* loaded from: a.zip:com/android/settingslib/bluetooth/CachedBluetoothDevice$Callback.class */
    public interface Callback {
        void onDeviceAttributesChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CachedBluetoothDevice(Context context, LocalBluetoothAdapter localBluetoothAdapter, LocalBluetoothProfileManager localBluetoothProfileManager, BluetoothDevice bluetoothDevice) {
        this.mContext = context;
        this.mLocalAdapter = localBluetoothAdapter;
        this.mProfileManager = localBluetoothProfileManager;
        this.mDevice = bluetoothDevice;
        fillData();
    }

    private void connectAutoConnectableProfiles() {
        if (ensurePaired()) {
            this.mIsConnectingErrorPossible = true;
            for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
                if (localBluetoothProfile.isAutoConnectable()) {
                    localBluetoothProfile.setPreferred(this.mDevice, true);
                    Log.d("CachedBluetoothDevice", describe(localBluetoothProfile) + " setPreferred true and connect");
                    connectInt(localBluetoothProfile);
                }
            }
        }
    }

    private void connectWithoutResettingTimer(boolean z) {
        if (this.mProfiles.isEmpty()) {
            Log.d("CachedBluetoothDevice", "No profiles. Maybe we will connect later");
            return;
        }
        this.mIsConnectingErrorPossible = true;
        int i = 0;
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            if (z ? localBluetoothProfile.isConnectable() : localBluetoothProfile.isAutoConnectable()) {
                Log.d("CachedBluetoothDevice", describe(localBluetoothProfile) + " isPreferred : " + localBluetoothProfile.isPreferred(this.mDevice));
                if (localBluetoothProfile.isPreferred(this.mDevice)) {
                    i++;
                    connectInt(localBluetoothProfile);
                }
            }
        }
        if (i == 0) {
            connectAutoConnectableProfiles();
        }
    }

    private String describe(LocalBluetoothProfile localBluetoothProfile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Address:").append(this.mDevice);
        if (localBluetoothProfile != null) {
            sb.append(" Profile:").append(localBluetoothProfile);
        }
        return sb.toString();
    }

    private void dispatchAttributesChanged() {
        synchronized (this.mCallbacks) {
            for (Callback callback : this.mCallbacks) {
                callback.onDeviceAttributesChanged();
            }
        }
    }

    private boolean ensurePaired() {
        if (getBondState() == 10) {
            startPairing();
            return false;
        }
        return true;
    }

    private void fetchBtClass() {
        this.mBtClass = this.mDevice.getBluetoothClass();
        if (this.mBtClass == null) {
            Log.d("CachedBluetoothDevice", "fetchClass, mBtClass is null");
            return;
        }
        Log.d("CachedBluetoothDevice", "fetchClass, mBtClass is " + this.mBtClass.getMajorDeviceClass());
    }

    private void fetchMessageRejectionCount() {
        this.mMessageRejectionCount = this.mContext.getSharedPreferences("bluetooth_message_reject", 0).getInt(this.mDevice.getAddress(), 0);
    }

    private void fetchName() {
        this.mName = this.mDevice.getAliasName();
        Log.d("CachedBluetoothDevice", "fetchName, AlaisName is " + this.mName);
        if (TextUtils.isEmpty(this.mName)) {
            this.mName = this.mDevice.getAddress();
        }
        Log.d("CachedBluetoothDevice", "fetchName, Return Name " + this.mName);
    }

    private void fillData() {
        fetchName();
        fetchBtClass();
        updateProfiles();
        migratePhonebookPermissionChoice();
        migrateMessagePermissionChoice();
        fetchMessageRejectionCount();
        this.mVisible = false;
        dispatchAttributesChanged();
    }

    private void migrateMessagePermissionChoice() {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("bluetooth_message_permission", 0);
        if (sharedPreferences.contains(this.mDevice.getAddress())) {
            if (this.mDevice.getMessageAccessPermission() == 0) {
                int i = sharedPreferences.getInt(this.mDevice.getAddress(), 0);
                if (i == 1) {
                    this.mDevice.setMessageAccessPermission(1);
                } else if (i == 2) {
                    this.mDevice.setMessageAccessPermission(2);
                }
            }
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.remove(this.mDevice.getAddress());
            edit.commit();
        }
    }

    private void migratePhonebookPermissionChoice() {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("bluetooth_phonebook_permission", 0);
        if (sharedPreferences.contains(this.mDevice.getAddress())) {
            if (this.mDevice.getPhonebookAccessPermission() == 0) {
                int i = sharedPreferences.getInt(this.mDevice.getAddress(), 0);
                if (i == 1) {
                    this.mDevice.setPhonebookAccessPermission(1);
                } else if (i == 2) {
                    this.mDevice.setPhonebookAccessPermission(2);
                }
            }
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.remove(this.mDevice.getAddress());
            edit.commit();
        }
    }

    private void processPhonebookAccess() {
        if (this.mDevice.getBondState() == 12 && BluetoothUuid.containsAnyUuid(this.mDevice.getUuids(), PbapServerProfile.PBAB_CLIENT_UUIDS) && getPhonebookPermissionChoice() == 0) {
            if (this.mDevice.getBluetoothClass().getDeviceClass() == 1032) {
                setPhonebookPermissionChoice(1);
            } else {
                setPhonebookPermissionChoice(2);
            }
        }
    }

    private void saveMessageRejectionCount() {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences("bluetooth_message_reject", 0).edit();
        if (this.mMessageRejectionCount == 0) {
            edit.remove(this.mDevice.getAddress());
        } else {
            edit.putInt(this.mDevice.getAddress(), this.mMessageRejectionCount);
        }
        edit.commit();
    }

    private boolean updateProfiles() {
        ParcelUuid[] uuids = this.mDevice.getUuids();
        if (uuids == null) {
            Log.d("CachedBluetoothDevice", "Bluetooth device get uuid is null");
            return false;
        }
        ParcelUuid[] uuids2 = this.mLocalAdapter.getUuids();
        if (uuids2 == null) {
            Log.d("CachedBluetoothDevice", "Bluetooth Adapter get uuid is null");
            return false;
        }
        processPhonebookAccess();
        Log.d("CachedBluetoothDevice", this.mName + " update profiles");
        this.mProfileManager.updateProfiles(uuids, uuids2, this.mProfiles, this.mRemovedProfiles, this.mLocalNapRoleConnected, this.mDevice);
        return true;
    }

    public void clearProfileConnectionState() {
        Log.d("CachedBluetoothDevice", " Clearing all connection state for dev:" + this.mDevice.getName());
        for (LocalBluetoothProfile localBluetoothProfile : getProfiles()) {
            this.mProfileConnectionState.put(localBluetoothProfile, 0);
        }
    }

    @Override // java.lang.Comparable
    public int compareTo(CachedBluetoothDevice cachedBluetoothDevice) {
        int i = (cachedBluetoothDevice.isConnected() ? 1 : 0) - (isConnected() ? 1 : 0);
        if (i != 0) {
            return i;
        }
        int i2 = (cachedBluetoothDevice.getBondState() == 12 ? 1 : 0) - (getBondState() == 12 ? 1 : 0);
        if (i2 != 0) {
            return i2;
        }
        int i3 = (cachedBluetoothDevice.mVisible ? 1 : 0) - (this.mVisible ? 1 : 0);
        if (i3 != 0) {
            return i3;
        }
        int i4 = cachedBluetoothDevice.mRssi - this.mRssi;
        return i4 != 0 ? i4 : this.mName.compareTo(cachedBluetoothDevice.mName);
    }

    public void connect(boolean z) {
        if (ensurePaired()) {
            this.mConnectAttempted = SystemClock.elapsedRealtime();
            connectWithoutResettingTimer(z);
        }
    }

    void connectInt(LocalBluetoothProfile localBluetoothProfile) {
        synchronized (this) {
            if (ensurePaired()) {
                if (localBluetoothProfile.connect(this.mDevice)) {
                    Log.d("CachedBluetoothDevice", "Command sent successfully:CONNECT " + describe(localBluetoothProfile));
                } else {
                    Log.i("CachedBluetoothDevice", "Failed to connect " + localBluetoothProfile.toString() + " to " + this.mName);
                }
            }
        }
    }

    public void disconnect() {
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            disconnect(localBluetoothProfile);
        }
        PbapServerProfile pbapProfile = this.mProfileManager.getPbapProfile();
        if (pbapProfile.getConnectionStatus(this.mDevice) == 2) {
            pbapProfile.disconnect(this.mDevice);
        }
    }

    public void disconnect(LocalBluetoothProfile localBluetoothProfile) {
        if (localBluetoothProfile.disconnect(this.mDevice)) {
            Log.d("CachedBluetoothDevice", "Command sent successfully:DISCONNECT " + describe(localBluetoothProfile));
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CachedBluetoothDevice)) {
            return false;
        }
        return this.mDevice.equals(((CachedBluetoothDevice) obj).mDevice);
    }

    public int getBondState() {
        return this.mDevice.getBondState();
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public int getMaxConnectionState() {
        int i = 0;
        for (LocalBluetoothProfile localBluetoothProfile : getProfiles()) {
            int profileConnectionState = getProfileConnectionState(localBluetoothProfile);
            if (profileConnectionState > i) {
                i = profileConnectionState;
            }
        }
        return i;
    }

    public String getName() {
        return this.mName;
    }

    public int getPhonebookPermissionChoice() {
        int phonebookAccessPermission = this.mDevice.getPhonebookAccessPermission();
        if (phonebookAccessPermission == 1) {
            return 1;
        }
        return phonebookAccessPermission == 2 ? 2 : 0;
    }

    public int getProfileConnectionState(LocalBluetoothProfile localBluetoothProfile) {
        if (this.mProfileConnectionState == null || this.mProfileConnectionState.get(localBluetoothProfile) == null) {
            int connectionStatus = localBluetoothProfile.getConnectionStatus(this.mDevice);
            Log.d("CachedBluetoothDevice", describe(localBluetoothProfile) + " state : " + connectionStatus);
            this.mProfileConnectionState.put(localBluetoothProfile, Integer.valueOf(connectionStatus));
        }
        return this.mProfileConnectionState.get(localBluetoothProfile).intValue();
    }

    public List<LocalBluetoothProfile> getProfiles() {
        return Collections.unmodifiableList(this.mProfiles);
    }

    public int hashCode() {
        return this.mDevice.getAddress().hashCode();
    }

    public boolean isConnected() {
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            if (getProfileConnectionState(localBluetoothProfile) == 2) {
                return true;
            }
        }
        return false;
    }

    void onBondingDockConnect() {
        connect(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onBondingStateChanged(int i) {
        Log.d("CachedBluetoothDevice", "onBondingStateChanged to " + i);
        if (i == 10) {
            this.mProfiles.clear();
            this.mConnectAfterPairing = false;
            setPhonebookPermissionChoice(0);
            setMessagePermissionChoice(0);
            setSimPermissionChoice(0);
            this.mMessageRejectionCount = 0;
            saveMessageRejectionCount();
        }
        refresh();
        if (i == 12) {
            Log.d("CachedBluetoothDevice", "Bond state changed to bonded, mConnectAfterPairing is " + this.mConnectAfterPairing);
            if (this.mDevice.isBluetoothDock()) {
                onBondingDockConnect();
            } else if (this.mConnectAfterPairing) {
                connect(false);
            }
            this.mConnectAfterPairing = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onProfileStateChanged(LocalBluetoothProfile localBluetoothProfile, int i) {
        Log.d("CachedBluetoothDevice", "onProfileStateChanged: profile " + localBluetoothProfile + " newProfileState " + i);
        if (this.mLocalAdapter.getBluetoothState() == 13) {
            Log.d("CachedBluetoothDevice", " BT Turninig Off...Profile conn state change ignored...");
            return;
        }
        this.mProfileConnectionState.put(localBluetoothProfile, Integer.valueOf(i));
        if (i == 2) {
            if (localBluetoothProfile instanceof MapProfile) {
                localBluetoothProfile.setPreferred(this.mDevice, true);
            } else if (this.mProfiles.contains(localBluetoothProfile)) {
            } else {
                this.mRemovedProfiles.remove(localBluetoothProfile);
                this.mProfiles.add(localBluetoothProfile);
                if ((localBluetoothProfile instanceof PanProfile) && ((PanProfile) localBluetoothProfile).isLocalRoleNap(this.mDevice)) {
                    this.mLocalNapRoleConnected = true;
                }
            }
        } else if ((localBluetoothProfile instanceof MapProfile) && i == 0) {
            localBluetoothProfile.setPreferred(this.mDevice, false);
            refresh();
        } else if (this.mLocalNapRoleConnected && (localBluetoothProfile instanceof PanProfile) && ((PanProfile) localBluetoothProfile).isLocalRoleNap(this.mDevice) && i == 0) {
            Log.d("CachedBluetoothDevice", "Removing PanProfile from device after NAP disconnect");
            this.mProfiles.remove(localBluetoothProfile);
            this.mRemovedProfiles.add(localBluetoothProfile);
            this.mLocalNapRoleConnected = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onUuidChanged() {
        updateProfiles();
        long j = 5000;
        if (BluetoothUuid.isUuidPresent(this.mDevice.getUuids(), BluetoothUuid.Hogp)) {
            j = 30000;
        }
        if (!this.mProfiles.isEmpty() && this.mConnectAttempted + j > SystemClock.elapsedRealtime()) {
            connectWithoutResettingTimer(false);
        }
        dispatchAttributesChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void refresh() {
        dispatchAttributesChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void refreshBtClass() {
        fetchBtClass();
        dispatchAttributesChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void refreshName() {
        fetchName();
        dispatchAttributesChanged();
    }

    public void registerCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(callback);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBtClass(BluetoothClass bluetoothClass) {
        if (bluetoothClass == null || this.mBtClass == bluetoothClass) {
            return;
        }
        this.mBtClass = bluetoothClass;
        dispatchAttributesChanged();
    }

    public void setMessagePermissionChoice(int i) {
        int i2 = 0;
        if (i == 1) {
            i2 = 1;
        } else if (i == 2) {
            i2 = 2;
        }
        this.mDevice.setMessageAccessPermission(i2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setNewName(String str) {
        if (this.mName == null) {
            this.mName = str;
            if (this.mName == null || TextUtils.isEmpty(this.mName)) {
                this.mName = this.mDevice.getAddress();
            }
            dispatchAttributesChanged();
        }
    }

    public void setPhonebookPermissionChoice(int i) {
        int i2 = 0;
        if (i == 1) {
            i2 = 1;
        } else if (i == 2) {
            i2 = 2;
        }
        this.mDevice.setPhonebookAccessPermission(i2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setRssi(short s) {
        if (this.mRssi != s) {
            this.mRssi = s;
            dispatchAttributesChanged();
        }
    }

    void setSimPermissionChoice(int i) {
        int i2 = 0;
        if (i == 1) {
            i2 = 1;
        } else if (i == 2) {
            i2 = 2;
        }
        this.mDevice.setSimAccessPermission(i2);
    }

    public void setVisible(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            dispatchAttributesChanged();
        }
    }

    public boolean startPairing() {
        if (this.mLocalAdapter.isDiscovering()) {
            this.mLocalAdapter.cancelDiscovery();
        }
        if (this.mDevice.createBond()) {
            this.mConnectAfterPairing = true;
            return true;
        }
        return false;
    }

    public String toString() {
        return this.mDevice.toString();
    }
}
