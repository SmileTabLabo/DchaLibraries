package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import com.android.settingslib.R;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/* loaded from: classes.dex */
public class CachedBluetoothDevice implements Comparable<CachedBluetoothDevice> {
    private final AudioManager mAudioManager;
    private BluetoothClass mBtClass;
    private long mConnectAttempted;
    private final Context mContext;
    private final BluetoothDevice mDevice;
    private long mHiSyncId;
    private boolean mIsConnectingErrorPossible;
    private boolean mJustDiscovered;
    private final LocalBluetoothAdapter mLocalAdapter;
    private boolean mLocalNapRoleConnected;
    private int mMessageRejectionCount;
    private String mName;
    private final LocalBluetoothProfileManager mProfileManager;
    private short mRssi;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private final List<LocalBluetoothProfile> mProfiles = new ArrayList();
    private final List<LocalBluetoothProfile> mRemovedProfiles = new ArrayList();
    private final Collection<Callback> mCallbacks = new ArrayList();
    private boolean mIsActiveDeviceA2dp = false;
    private boolean mIsActiveDeviceHeadset = false;
    private boolean mIsActiveDeviceHearingAid = false;
    private int mConnectCount = 0;
    private boolean mTimerScheduled = false;
    private boolean isReconnectedTimeOut = false;
    private HashMap<LocalBluetoothProfile, Integer> mProfileConnectionState = new HashMap<>();

    /* loaded from: classes.dex */
    public interface Callback {
        void onDeviceAttributesChanged();
    }

    static /* synthetic */ int access$108(CachedBluetoothDevice cachedBluetoothDevice) {
        int i = cachedBluetoothDevice.mConnectCount;
        cachedBluetoothDevice.mConnectCount = i + 1;
        return i;
    }

    public long getHiSyncId() {
        return this.mHiSyncId;
    }

    public void setHiSyncId(long j) {
        Log.d("CachedBluetoothDevice", "setHiSyncId: mDevice " + this.mDevice + ", id " + j);
        this.mHiSyncId = j;
    }

    private String describe(LocalBluetoothProfile localBluetoothProfile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Address:");
        sb.append(this.mDevice);
        if (localBluetoothProfile != null) {
            sb.append(" Profile:");
            sb.append(localBluetoothProfile);
        }
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onProfileStateChanged(LocalBluetoothProfile localBluetoothProfile, int i) {
        Log.d("CachedBluetoothDevice", "onProfileStateChanged: profile " + localBluetoothProfile + " newProfileState " + i);
        if (this.mLocalAdapter.getBluetoothState() == 13) {
            Log.d("CachedBluetoothDevice", " BT Turninig Off...Profile conn state change ignored...");
            return;
        }
        String str = SystemProperties.get("persist.sys.bt.autoreconnect");
        Log.d("CachedBluetoothDevice", "onProfileStateChanged: connectString = " + str);
        boolean equals = SystemProperties.get("persist.sys.bt.autoreconnect").equals("1");
        Log.d("CachedBluetoothDevice", "onProfileStateChanged: autoReconnect = " + equals);
        Log.d("CachedBluetoothDevice", "onProfileStateChanged: isReconnectedTimeOut = " + this.isReconnectedTimeOut);
        if (localBluetoothProfile instanceof A2dpSinkProfile) {
            if (i == 2) {
                stopTimer();
                Log.d("CachedBluetoothDevice", "onProfileStateChanged: timer stop ");
            } else if (i == 0 && equals && !this.isReconnectedTimeOut) {
                startTimer();
                Log.d("CachedBluetoothDevice", "onProfileStateChanged: timer start ");
                if (this.mTimer != null && !this.mTimerScheduled) {
                    this.mTimer.schedule(this.mTimerTask, 0L, 3000L);
                    this.mTimerScheduled = true;
                    Log.d("CachedBluetoothDevice", "onProfileStateChanged: timer schedule ");
                }
            }
        }
        this.mProfileConnectionState.put(localBluetoothProfile, Integer.valueOf(i));
        if (i == 2) {
            if (localBluetoothProfile instanceof MapProfile) {
                localBluetoothProfile.setPreferred(this.mDevice, true);
            }
            if (!this.mProfiles.contains(localBluetoothProfile)) {
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
        fetchActiveDevices();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CachedBluetoothDevice(Context context, LocalBluetoothAdapter localBluetoothAdapter, LocalBluetoothProfileManager localBluetoothProfileManager, BluetoothDevice bluetoothDevice) {
        this.mContext = context;
        this.mLocalAdapter = localBluetoothAdapter;
        this.mProfileManager = localBluetoothProfileManager;
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
        this.mDevice = bluetoothDevice;
        fillData();
        this.mHiSyncId = 0L;
        startTimer();
        Log.d("CachedBluetoothDevice", "CachedBluetoothDevice...");
    }

    private void startTimer() {
        Log.d("CachedBluetoothDevice", "startTimer");
        if (this.mTimer == null) {
            this.mTimer = new Timer();
        }
        if (this.mTimerTask == null) {
            this.mTimerTask = new TimerTask() { // from class: com.android.settingslib.bluetooth.CachedBluetoothDevice.1
                @Override // java.util.TimerTask, java.lang.Runnable
                public void run() {
                    try {
                        Log.d("CachedBluetoothDevice", "TimerTask, run, mDevice = " + CachedBluetoothDevice.this.mDevice);
                        if (CachedBluetoothDevice.this.mConnectCount > 20) {
                            CachedBluetoothDevice.this.stopTimer();
                            CachedBluetoothDevice.this.isReconnectedTimeOut = true;
                            Log.d("CachedBluetoothDevice", "TimerTask, run, MAX stop timer");
                        } else if (CachedBluetoothDevice.this.mDevice != null) {
                            CachedBluetoothDevice.this.connectProfile(CachedBluetoothDevice.this.mProfileManager.getA2dpSinkProfile());
                            CachedBluetoothDevice.access$108(CachedBluetoothDevice.this);
                            Log.d("CachedBluetoothDevice", "TimerTask, run, do connecting..mConnectCount = " + CachedBluetoothDevice.this.mConnectCount);
                        }
                    } catch (Exception e) {
                        Log.d("CachedBluetoothDevice", "TimerTask, run, e = " + e);
                    }
                }
            };
        }
        this.isReconnectedTimeOut = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
        if (this.mTimerTask != null) {
            this.mTimerTask.cancel();
            this.mTimerTask = null;
        }
        this.mConnectCount = 0;
        this.mTimerScheduled = false;
        this.isReconnectedTimeOut = false;
        SystemProperties.set("persist.sys.bt.autoreconnect", "0");
        Log.d("CachedBluetoothDevice", "stopTimer");
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

    public void connect(boolean z) {
        if (!ensurePaired()) {
            return;
        }
        this.mConnectAttempted = SystemClock.elapsedRealtime();
        connectWithoutResettingTimer(z);
    }

    void onBondingDockConnect() {
        connect(false);
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x0060 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:31:0x001a A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void connectWithoutResettingTimer(boolean z) {
        if (this.mProfiles.isEmpty()) {
            Log.d("CachedBluetoothDevice", "No profiles. Maybe we will connect later");
            return;
        }
        this.mIsConnectingErrorPossible = true;
        int i = 0;
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            if (z) {
                if (localBluetoothProfile.isConnectable()) {
                    Log.d("CachedBluetoothDevice", describe(localBluetoothProfile) + " isPreferred : " + localBluetoothProfile.isPreferred(this.mDevice));
                    if (!localBluetoothProfile.isPreferred(this.mDevice)) {
                        i++;
                        connectInt(localBluetoothProfile);
                    }
                }
            } else if (localBluetoothProfile.isAutoConnectable()) {
                Log.d("CachedBluetoothDevice", describe(localBluetoothProfile) + " isPreferred : " + localBluetoothProfile.isPreferred(this.mDevice));
                if (!localBluetoothProfile.isPreferred(this.mDevice)) {
                }
            }
        }
        if (i == 0) {
            connectAutoConnectableProfiles();
        }
    }

    private void connectAutoConnectableProfiles() {
        if (!ensurePaired()) {
            return;
        }
        this.mIsConnectingErrorPossible = true;
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            if (localBluetoothProfile.isAutoConnectable()) {
                localBluetoothProfile.setPreferred(this.mDevice, true);
                Log.d("CachedBluetoothDevice", describe(localBluetoothProfile) + " setPreferred true and connect");
                connectInt(localBluetoothProfile);
            }
        }
    }

    public void connectProfile(LocalBluetoothProfile localBluetoothProfile) {
        this.mConnectAttempted = SystemClock.elapsedRealtime();
        this.mIsConnectingErrorPossible = true;
        connectInt(localBluetoothProfile);
        refresh();
    }

    synchronized void connectInt(LocalBluetoothProfile localBluetoothProfile) {
        if (ensurePaired()) {
            if (localBluetoothProfile.connect(this.mDevice)) {
                Log.d("CachedBluetoothDevice", "Command sent successfully:CONNECT " + describe(localBluetoothProfile));
                return;
            }
            Log.i("CachedBluetoothDevice", "Failed to connect " + localBluetoothProfile.toString() + " to " + this.mName);
        }
    }

    private boolean ensurePaired() {
        if (getBondState() == 10) {
            startPairing();
            return false;
        }
        return true;
    }

    public boolean startPairing() {
        if (this.mLocalAdapter.isDiscovering()) {
            this.mLocalAdapter.cancelDiscovery();
        }
        if (!this.mDevice.createBond()) {
            return false;
        }
        return true;
    }

    public void unpair() {
        BluetoothDevice bluetoothDevice;
        int bondState = getBondState();
        if (bondState == 11) {
            this.mDevice.cancelBondProcess();
        }
        if (bondState != 10 && (bluetoothDevice = this.mDevice) != null && bluetoothDevice.removeBond()) {
            Log.d("CachedBluetoothDevice", "Command sent successfully:REMOVE_BOND " + describe(null));
        }
    }

    public int getProfileConnectionState(LocalBluetoothProfile localBluetoothProfile) {
        if (this.mProfileConnectionState.get(localBluetoothProfile) == null) {
            int connectionStatus = localBluetoothProfile.getConnectionStatus(this.mDevice);
            Log.d("CachedBluetoothDevice", describe(localBluetoothProfile) + " state : " + connectionStatus);
            this.mProfileConnectionState.put(localBluetoothProfile, Integer.valueOf(connectionStatus));
        }
        return this.mProfileConnectionState.get(localBluetoothProfile).intValue();
    }

    public void clearProfileConnectionState() {
        Log.d("CachedBluetoothDevice", " Clearing all connection state for dev:" + this.mDevice.getName());
        for (LocalBluetoothProfile localBluetoothProfile : getProfiles()) {
            this.mProfileConnectionState.put(localBluetoothProfile, 0);
        }
    }

    private void fillData() {
        fetchName();
        fetchBtClass();
        updateProfiles();
        fetchActiveDevices();
        migratePhonebookPermissionChoice();
        migrateMessagePermissionChoice();
        fetchMessageRejectionCount();
        dispatchAttributesChanged();
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public String getAddress() {
        return this.mDevice.getAddress();
    }

    public String getName() {
        return this.mName;
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

    public void setName(String str) {
        if (str != null && !TextUtils.equals(str, this.mName)) {
            this.mName = str;
            this.mDevice.setAlias(str);
            dispatchAttributesChanged();
        }
    }

    public boolean setActive() {
        boolean z;
        A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
        if (a2dpProfile != null && isConnectedProfile(a2dpProfile) && a2dpProfile.setActiveDevice(getDevice())) {
            Log.i("CachedBluetoothDevice", "OnPreferenceClickListener: A2DP active device=" + this);
            z = true;
        } else {
            z = false;
        }
        HeadsetProfile headsetProfile = this.mProfileManager.getHeadsetProfile();
        if (headsetProfile != null && isConnectedProfile(headsetProfile) && headsetProfile.setActiveDevice(getDevice())) {
            Log.i("CachedBluetoothDevice", "OnPreferenceClickListener: Headset active device=" + this);
            z = true;
        }
        HearingAidProfile hearingAidProfile = this.mProfileManager.getHearingAidProfile();
        if (hearingAidProfile != null && isConnectedProfile(hearingAidProfile) && hearingAidProfile.setActiveDevice(getDevice())) {
            Log.i("CachedBluetoothDevice", "OnPreferenceClickListener: Hearing Aid active device=" + this);
            return true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void refreshName() {
        fetchName();
        dispatchAttributesChanged();
    }

    private void fetchName() {
        this.mName = this.mDevice.getAliasName();
        Log.d("CachedBluetoothDevice", "fetchName, AlaisName is " + this.mName);
        if (TextUtils.isEmpty(this.mName)) {
            this.mName = this.mDevice.getAddress();
        }
        Log.d("CachedBluetoothDevice", "fetchName, Return Name " + this.mName);
    }

    public boolean hasHumanReadableName() {
        return !TextUtils.isEmpty(this.mDevice.getAliasName());
    }

    public int getBatteryLevel() {
        return this.mDevice.getBatteryLevel();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void refresh() {
        dispatchAttributesChanged();
    }

    public void setJustDiscovered(boolean z) {
        if (this.mJustDiscovered != z) {
            this.mJustDiscovered = z;
            dispatchAttributesChanged();
        }
    }

    public int getBondState() {
        return this.mDevice.getBondState();
    }

    public void onActiveDeviceChanged(boolean z, int i) {
        if (i != 21) {
            switch (i) {
                case 1:
                    r2 = this.mIsActiveDeviceHeadset != z;
                    this.mIsActiveDeviceHeadset = z;
                    break;
                case 2:
                    r2 = this.mIsActiveDeviceA2dp != z;
                    this.mIsActiveDeviceA2dp = z;
                    break;
                default:
                    Log.w("CachedBluetoothDevice", "onActiveDeviceChanged: unknown profile " + i + " isActive " + z);
                    break;
            }
        } else {
            r2 = this.mIsActiveDeviceHearingAid != z;
            this.mIsActiveDeviceHearingAid = z;
        }
        if (r2) {
            dispatchAttributesChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onAudioModeChanged() {
        dispatchAttributesChanged();
    }

    public boolean isActiveDevice(int i) {
        if (i != 21) {
            switch (i) {
                case 1:
                    return this.mIsActiveDeviceHeadset;
                case 2:
                    return this.mIsActiveDeviceA2dp;
                default:
                    Log.w("CachedBluetoothDevice", "getActiveDevice: unknown profile " + i);
                    return false;
            }
        }
        return this.mIsActiveDeviceHearingAid;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setRssi(short s) {
        if (this.mRssi != s) {
            this.mRssi = s;
            dispatchAttributesChanged();
        }
    }

    public boolean isConnected() {
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            if (getProfileConnectionState(localBluetoothProfile) == 2) {
                return true;
            }
        }
        return false;
    }

    public boolean isConnectedProfile(LocalBluetoothProfile localBluetoothProfile) {
        return getProfileConnectionState(localBluetoothProfile) == 2;
    }

    /* JADX WARN: Removed duplicated region for block: B:5:0x000d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean isBusy() {
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            int profileConnectionState = getProfileConnectionState(localBluetoothProfile);
            if (profileConnectionState == 1 || profileConnectionState == 3) {
                return true;
            }
            while (r0.hasNext()) {
            }
        }
        Log.d("CachedBluetoothDevice", this.mName + " bond state is " + getBondState());
        return getBondState() == 11;
    }

    private void fetchBtClass() {
        this.mBtClass = this.mDevice.getBluetoothClass();
        if (this.mBtClass == null) {
            Log.d("CachedBluetoothDevice", "fetchClass, mBtClass is null");
            return;
        }
        int majorDeviceClass = this.mBtClass.getMajorDeviceClass();
        Log.d("CachedBluetoothDevice", "fetchClass, mBtClass is " + majorDeviceClass);
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

    private void fetchActiveDevices() {
        A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
        if (a2dpProfile != null) {
            this.mIsActiveDeviceA2dp = this.mDevice.equals(a2dpProfile.getActiveDevice());
        }
        HeadsetProfile headsetProfile = this.mProfileManager.getHeadsetProfile();
        if (headsetProfile != null) {
            this.mIsActiveDeviceHeadset = this.mDevice.equals(headsetProfile.getActiveDevice());
        }
        HearingAidProfile hearingAidProfile = this.mProfileManager.getHearingAidProfile();
        if (hearingAidProfile != null) {
            this.mIsActiveDeviceHearingAid = hearingAidProfile.getActiveDevices().contains(this.mDevice);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void refreshBtClass() {
        fetchBtClass();
        dispatchAttributesChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onUuidChanged() {
        long j;
        updateProfiles();
        if (BluetoothUuid.isUuidPresent(this.mDevice.getUuids(), BluetoothUuid.Hogp)) {
            j = 30000;
        } else {
            j = 5000;
        }
        if (!this.mProfiles.isEmpty() && this.mConnectAttempted + j > SystemClock.elapsedRealtime()) {
            connectWithoutResettingTimer(false);
        }
        dispatchAttributesChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onBondingStateChanged(int i) {
        Log.d("CachedBluetoothDevice", "onBondingStateChanged to " + i);
        if (i == 10) {
            this.mProfiles.clear();
            setPhonebookPermissionChoice(0);
            setMessagePermissionChoice(0);
            setSimPermissionChoice(0);
            this.mMessageRejectionCount = 0;
            saveMessageRejectionCount();
        }
        refresh();
        if (i == 12) {
            Log.d("CachedBluetoothDevice", "Bond state changed to bonded, mDevice.isBondingInitiatedLocally() is " + this.mDevice.isBondingInitiatedLocally());
            if (this.mDevice.isBluetoothDock()) {
                onBondingDockConnect();
            } else if (this.mDevice.isBondingInitiatedLocally()) {
                connect(false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBtClass(BluetoothClass bluetoothClass) {
        if (bluetoothClass != null && this.mBtClass != bluetoothClass) {
            this.mBtClass = bluetoothClass;
            dispatchAttributesChanged();
        }
    }

    public BluetoothClass getBtClass() {
        return this.mBtClass;
    }

    public List<LocalBluetoothProfile> getProfiles() {
        return Collections.unmodifiableList(this.mProfiles);
    }

    public List<LocalBluetoothProfile> getConnectableProfiles() {
        Log.d("CachedBluetoothDevice", this.mName + " mprofile size is " + this.mProfiles.size());
        ArrayList arrayList = new ArrayList();
        for (LocalBluetoothProfile localBluetoothProfile : this.mProfiles) {
            if (localBluetoothProfile.isConnectable()) {
                arrayList.add(localBluetoothProfile);
            }
        }
        Log.d("CachedBluetoothDevice", this.mName + " conectable profile size is " + arrayList.size());
        return arrayList;
    }

    public List<LocalBluetoothProfile> getRemovedProfiles() {
        return this.mRemovedProfiles;
    }

    public void registerCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(callback);
        }
    }

    public void unregisterCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    private void dispatchAttributesChanged() {
        synchronized (this.mCallbacks) {
            for (Callback callback : this.mCallbacks) {
                callback.onDeviceAttributesChanged();
            }
        }
    }

    public String toString() {
        return this.mDevice.toString();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CachedBluetoothDevice)) {
            return false;
        }
        return this.mDevice.equals(((CachedBluetoothDevice) obj).mDevice);
    }

    public int hashCode() {
        return this.mDevice.getAddress().hashCode();
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
        int i3 = (cachedBluetoothDevice.mJustDiscovered ? 1 : 0) - (this.mJustDiscovered ? 1 : 0);
        if (i3 != 0) {
            return i3;
        }
        int i4 = cachedBluetoothDevice.mRssi - this.mRssi;
        return i4 != 0 ? i4 : this.mName.compareTo(cachedBluetoothDevice.mName);
    }

    public int getPhonebookPermissionChoice() {
        int phonebookAccessPermission = this.mDevice.getPhonebookAccessPermission();
        if (phonebookAccessPermission == 1) {
            return 1;
        }
        if (phonebookAccessPermission == 2) {
            return 2;
        }
        return 0;
    }

    public void setPhonebookPermissionChoice(int i) {
        int i2 = 2;
        if (i != 1) {
            if (i != 2) {
                i2 = 0;
            }
        } else {
            i2 = 1;
        }
        this.mDevice.setPhonebookAccessPermission(i2);
    }

    private void migratePhonebookPermissionChoice() {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("bluetooth_phonebook_permission", 0);
        if (!sharedPreferences.contains(this.mDevice.getAddress())) {
            return;
        }
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

    public int getMessagePermissionChoice() {
        int messageAccessPermission = this.mDevice.getMessageAccessPermission();
        if (messageAccessPermission == 1) {
            return 1;
        }
        if (messageAccessPermission == 2) {
            return 2;
        }
        return 0;
    }

    public void setMessagePermissionChoice(int i) {
        int i2 = 2;
        if (i != 1) {
            if (i != 2) {
                i2 = 0;
            }
        } else {
            i2 = 1;
        }
        this.mDevice.setMessageAccessPermission(i2);
    }

    public int getSimPermissionChoice() {
        int simAccessPermission = this.mDevice.getSimAccessPermission();
        if (simAccessPermission == 1) {
            return 1;
        }
        if (simAccessPermission == 2) {
            return 2;
        }
        return 0;
    }

    void setSimPermissionChoice(int i) {
        int i2 = 2;
        if (i != 1) {
            if (i != 2) {
                i2 = 0;
            }
        } else {
            i2 = 1;
        }
        this.mDevice.setSimAccessPermission(i2);
    }

    private void migrateMessagePermissionChoice() {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("bluetooth_message_permission", 0);
        if (!sharedPreferences.contains(this.mDevice.getAddress())) {
            return;
        }
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

    public boolean checkAndIncreaseMessageRejectionCount() {
        if (this.mMessageRejectionCount < 2) {
            this.mMessageRejectionCount++;
            saveMessageRejectionCount();
        }
        return this.mMessageRejectionCount >= 2;
    }

    private void fetchMessageRejectionCount() {
        this.mMessageRejectionCount = this.mContext.getSharedPreferences("bluetooth_message_reject", 0).getInt(this.mDevice.getAddress(), 0);
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

    private void processPhonebookAccess() {
        if (this.mDevice.getBondState() == 12 && BluetoothUuid.containsAnyUuid(this.mDevice.getUuids(), PbapServerProfile.PBAB_CLIENT_UUIDS) && getPhonebookPermissionChoice() == 0) {
            if (this.mDevice.getBluetoothClass().getDeviceClass() == 1032 || this.mDevice.getBluetoothClass().getDeviceClass() == 1028) {
                EventLog.writeEvent(1397638484, "138529441", -1, "");
            }
            setPhonebookPermissionChoice(2);
        }
    }

    public String getConnectionSummary() {
        Log.d("CachedBluetoothDevice", this.mName + " getConnectionSummary");
        boolean z = true;
        boolean z2 = true;
        boolean z3 = true;
        boolean z4 = false;
        for (LocalBluetoothProfile localBluetoothProfile : getProfiles()) {
            int profileConnectionState = getProfileConnectionState(localBluetoothProfile);
            if (localBluetoothProfile != null) {
                Log.d("CachedBluetoothDevice", "profile name is " + localBluetoothProfile.toString() + ": " + profileConnectionState);
            }
            switch (profileConnectionState) {
                case 0:
                    if (localBluetoothProfile.isProfileReady()) {
                        if (!(localBluetoothProfile instanceof A2dpProfile) && !(localBluetoothProfile instanceof A2dpSinkProfile)) {
                            if (!(localBluetoothProfile instanceof HeadsetProfile) && !(localBluetoothProfile instanceof HfpClientProfile)) {
                                if (localBluetoothProfile instanceof HearingAidProfile) {
                                    z3 = false;
                                    break;
                                } else {
                                    break;
                                }
                            } else {
                                z2 = false;
                                break;
                            }
                        } else {
                            z = false;
                            break;
                        }
                    } else {
                        break;
                    }
                    break;
                case 1:
                case 3:
                    return this.mContext.getString(Utils.getConnectionStateSummary(profileConnectionState));
                case 2:
                    Log.d("CachedBluetoothDevice", "profileConnected = true");
                    z4 = true;
                    break;
            }
        }
        int batteryLevel = getBatteryLevel();
        String formatPercentage = batteryLevel != -1 ? com.android.settingslib.Utils.formatPercentage(batteryLevel) : null;
        int i = R.string.bluetooth_pairing;
        if (z4) {
            if (z || z2 || z3) {
                if (formatPercentage != null) {
                    i = com.android.settingslib.Utils.isAudioModeOngoingCall(this.mContext) ? this.mIsActiveDeviceHeadset ? R.string.bluetooth_active_battery_level : R.string.bluetooth_battery_level : (this.mIsActiveDeviceHearingAid || this.mIsActiveDeviceA2dp) ? R.string.bluetooth_active_battery_level : R.string.bluetooth_battery_level;
                } else if (com.android.settingslib.Utils.isAudioModeOngoingCall(this.mContext)) {
                    if (this.mIsActiveDeviceHeadset) {
                        i = R.string.bluetooth_active_no_battery_level;
                    }
                } else if (this.mIsActiveDeviceHearingAid || this.mIsActiveDeviceA2dp) {
                    i = R.string.bluetooth_active_no_battery_level;
                }
            } else if (formatPercentage != null) {
                i = R.string.bluetooth_battery_level;
            }
        }
        if (i != R.string.bluetooth_pairing || getBondState() == 11) {
            return this.mContext.getString(i, formatPercentage);
        }
        return null;
    }

    public boolean isA2dpDevice() {
        return this.mProfileManager.getA2dpProfile() != null && this.mProfileManager.getA2dpProfile().getConnectionStatus(this.mDevice) == 2;
    }

    public boolean isHfpDevice() {
        return this.mProfileManager.getHeadsetProfile().getConnectionStatus(this.mDevice) == 2;
    }
}
