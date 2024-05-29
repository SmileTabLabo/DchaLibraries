package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.settingslib.R$bool;
import com.android.settingslib.bluetooth.BluetoothEventManager;
import com.mediatek.settingslib.FeatureOption;
import com.mediatek.settingslib.bluetooth.DunServerProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/* loaded from: a.zip:com/android/settingslib/bluetooth/LocalBluetoothProfileManager.class */
public final class LocalBluetoothProfileManager {
    private A2dpProfile mA2dpProfile;
    private A2dpSinkProfile mA2dpSinkProfile;
    private final Context mContext;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private DunServerProfile mDunProfile;
    private final BluetoothEventManager mEventManager;
    private HeadsetProfile mHeadsetProfile;
    private HfpClientProfile mHfpClientProfile;
    private HidProfile mHidProfile;
    private final LocalBluetoothAdapter mLocalAdapter;
    private MapProfile mMapProfile;
    private OppProfile mOppProfile;
    private PanProfile mPanProfile;
    private PbapClientProfile mPbapClientProfile;
    private PbapServerProfile mPbapProfile;
    private final Map<String, LocalBluetoothProfile> mProfileNameMap = new HashMap();
    private final Collection<ServiceListener> mServiceListeners = new ArrayList();
    private final boolean mUsePbapPce;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/settingslib/bluetooth/LocalBluetoothProfileManager$PanStateChangedHandler.class */
    public class PanStateChangedHandler extends StateChangedHandler {
        final LocalBluetoothProfileManager this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        PanStateChangedHandler(LocalBluetoothProfileManager localBluetoothProfileManager, LocalBluetoothProfile localBluetoothProfile) {
            super(localBluetoothProfileManager, localBluetoothProfile);
            this.this$0 = localBluetoothProfileManager;
        }

        @Override // com.android.settingslib.bluetooth.LocalBluetoothProfileManager.StateChangedHandler, com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            PanProfile panProfile = (PanProfile) this.mProfile;
            int intExtra = intent.getIntExtra("android.bluetooth.pan.extra.LOCAL_ROLE", 0);
            panProfile.setLocalRole(bluetoothDevice, intExtra);
            Log.d("LocalBluetoothProfileManager", "pan profile state change, role is " + intExtra);
            super.onReceive(context, intent, bluetoothDevice);
        }
    }

    /* loaded from: a.zip:com/android/settingslib/bluetooth/LocalBluetoothProfileManager$ServiceListener.class */
    public interface ServiceListener {
        void onServiceConnected();

        void onServiceDisconnected();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/settingslib/bluetooth/LocalBluetoothProfileManager$StateChangedHandler.class */
    public class StateChangedHandler implements BluetoothEventManager.Handler {
        final LocalBluetoothProfile mProfile;
        final LocalBluetoothProfileManager this$0;

        StateChangedHandler(LocalBluetoothProfileManager localBluetoothProfileManager, LocalBluetoothProfile localBluetoothProfile) {
            this.this$0 = localBluetoothProfileManager;
            this.mProfile = localBluetoothProfile;
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice findDevice = this.this$0.mDeviceManager.findDevice(bluetoothDevice);
            CachedBluetoothDevice cachedBluetoothDevice = findDevice;
            if (findDevice == null) {
                Log.w("LocalBluetoothProfileManager", "StateChangedHandler found new device: " + bluetoothDevice);
                cachedBluetoothDevice = this.this$0.mDeviceManager.addDevice(this.this$0.mLocalAdapter, this.this$0, bluetoothDevice);
            }
            int intExtra = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
            int intExtra2 = intent.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", 0);
            if (intExtra == 0 && intExtra2 == 1) {
                Log.i("LocalBluetoothProfileManager", "Failed to connect " + this.mProfile + " device");
            }
            cachedBluetoothDevice.onProfileStateChanged(this.mProfile, intExtra);
            cachedBluetoothDevice.refresh();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LocalBluetoothProfileManager(Context context, LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, BluetoothEventManager bluetoothEventManager) {
        this.mContext = context;
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mEventManager = bluetoothEventManager;
        this.mUsePbapPce = this.mContext.getResources().getBoolean(R$bool.enable_pbap_pce_profile);
        this.mLocalAdapter.setProfileManager(this);
        this.mEventManager.setProfileManager(this);
        ParcelUuid[] uuids = localBluetoothAdapter.getUuids();
        if (uuids != null) {
            Log.d("LocalBluetoothProfileManager", "bluetooth adapter uuid: ");
            int length = uuids.length;
            for (int i = 0; i < length; i++) {
                Log.d("LocalBluetoothProfileManager", "  " + uuids[i]);
            }
            updateLocalProfiles(uuids);
        }
        Log.d("LocalBluetoothProfileManager", "LocalBluetoothProfileManager construction complete");
    }

    private void addPanProfile(LocalBluetoothProfile localBluetoothProfile, String str, String str2) {
        this.mEventManager.addProfileHandler(str2, new PanStateChangedHandler(this, localBluetoothProfile));
        this.mProfileNameMap.put(str, localBluetoothProfile);
    }

    private void addProfile(LocalBluetoothProfile localBluetoothProfile, String str, String str2) {
        this.mEventManager.addProfileHandler(str2, new StateChangedHandler(this, localBluetoothProfile));
        this.mProfileNameMap.put(str, localBluetoothProfile);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void callServiceConnectedListeners() {
        for (ServiceListener serviceListener : this.mServiceListeners) {
            serviceListener.onServiceConnected();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void callServiceDisconnectedListeners() {
        for (ServiceListener serviceListener : this.mServiceListeners) {
            serviceListener.onServiceDisconnected();
        }
    }

    public PbapServerProfile getPbapProfile() {
        return this.mPbapProfile;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setBluetoothStateOn() {
        if (this.mHidProfile == null) {
            this.mHidProfile = new HidProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
            addProfile(this.mHidProfile, "HID", "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
        }
        if (this.mPanProfile == null) {
            this.mPanProfile = new PanProfile(this.mContext);
            addPanProfile(this.mPanProfile, "PAN", "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED");
        }
        if (this.mMapProfile == null) {
            Log.d("LocalBluetoothProfileManager", "Adding local MAP profile");
            this.mMapProfile = new MapProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
            addProfile(this.mMapProfile, "MAP", "android.bluetooth.map.profile.action.CONNECTION_STATE_CHANGED");
        }
        if (this.mPbapProfile == null) {
            this.mPbapProfile = new PbapServerProfile(this.mContext);
        }
        if (this.mDunProfile == null && FeatureOption.MTK_Bluetooth_DUN) {
            Log.d("LocalBluetoothProfileManager", "Adding local DUN profile");
            this.mDunProfile = new DunServerProfile(this.mContext);
            addProfile(this.mDunProfile, "DUN Server", "android.bluetooth.dun.intent.DUN_STATE");
        }
        ParcelUuid[] uuids = this.mLocalAdapter.getUuids();
        if (uuids != null) {
            updateLocalProfiles(uuids);
        }
        this.mEventManager.readPairedDevices();
    }

    void updateLocalProfiles(ParcelUuid[] parcelUuidArr) {
        if (BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.AudioSource)) {
            if (this.mA2dpProfile == null) {
                Log.d("LocalBluetoothProfileManager", "Adding local A2DP SRC profile");
                this.mA2dpProfile = new A2dpProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mA2dpProfile, "A2DP", "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mA2dpProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: A2DP profile was previously added but the UUID is now missing.");
        }
        if (BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.AudioSink)) {
            if (this.mA2dpSinkProfile == null) {
                Log.d("LocalBluetoothProfileManager", "Adding local A2DP Sink profile");
                this.mA2dpSinkProfile = new A2dpSinkProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mA2dpSinkProfile, "A2DPSink", "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mA2dpSinkProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: A2DP Sink profile was previously added but the UUID is now missing.");
        }
        if (BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.Handsfree_AG) || BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.HSP_AG)) {
            if (this.mHeadsetProfile == null) {
                Log.d("LocalBluetoothProfileManager", "Adding local HEADSET profile");
                this.mHeadsetProfile = new HeadsetProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mHeadsetProfile, "HEADSET", "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mHeadsetProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: HEADSET profile was previously added but the UUID is now missing.");
        }
        if (BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.Handsfree)) {
            if (this.mHfpClientProfile == null) {
                Log.d("LocalBluetoothProfileManager", "Adding local HfpClient profile");
                this.mHfpClientProfile = new HfpClientProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mHfpClientProfile, "HEADSET_CLIENT", "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mHfpClientProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: Hfp Client profile was previously added but the UUID is now missing.");
        } else {
            Log.d("LocalBluetoothProfileManager", "Handsfree Uuid not found.");
        }
        if (BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.ObexObjectPush)) {
            if (this.mOppProfile == null) {
                Log.d("LocalBluetoothProfileManager", "Adding local OPP profile");
                this.mOppProfile = new OppProfile();
                this.mProfileNameMap.put("OPP", this.mOppProfile);
            }
        } else if (this.mOppProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: OPP profile was previously added but the UUID is now missing.");
        }
        if (this.mUsePbapPce) {
            if (this.mPbapClientProfile == null) {
                Log.d("LocalBluetoothProfileManager", "Adding local PBAP Client profile");
                this.mPbapClientProfile = new PbapClientProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mPbapClientProfile, "PbapClient", "android.bluetooth.pbap.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mPbapClientProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: PBAP Client profile was previously added but the UUID is now missing.");
        }
        this.mEventManager.registerProfileIntentReceiver();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateProfiles(ParcelUuid[] parcelUuidArr, ParcelUuid[] parcelUuidArr2, Collection<LocalBluetoothProfile> collection, Collection<LocalBluetoothProfile> collection2, boolean z, BluetoothDevice bluetoothDevice) {
        synchronized (this) {
            collection2.clear();
            collection2.addAll(collection);
            Log.d("LocalBluetoothProfileManager", "Current Profiles" + collection.toString());
            collection.clear();
            Log.d("LocalBluetoothProfileManager", "update profiles");
            if (parcelUuidArr == null) {
                Log.d("LocalBluetoothProfileManager", "remote device uuid is null");
                return;
            }
            if (this.mHeadsetProfile != null && ((BluetoothUuid.isUuidPresent(parcelUuidArr2, BluetoothUuid.HSP_AG) && BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.HSP)) || (BluetoothUuid.isUuidPresent(parcelUuidArr2, BluetoothUuid.Handsfree_AG) && BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.Handsfree)))) {
                Log.d("LocalBluetoothProfileManager", "Add HeadsetProfile to connectable profile list");
                collection.add(this.mHeadsetProfile);
                collection2.remove(this.mHeadsetProfile);
            }
            if (this.mHfpClientProfile != null && BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.Handsfree_AG) && BluetoothUuid.isUuidPresent(parcelUuidArr2, BluetoothUuid.Handsfree)) {
                collection.add(this.mHfpClientProfile);
                collection2.remove(this.mHfpClientProfile);
            }
            if (BluetoothUuid.containsAnyUuid(parcelUuidArr, A2dpProfile.SINK_UUIDS) && this.mA2dpProfile != null) {
                Log.d("LocalBluetoothProfileManager", "Add A2dpProfile to connectable profile list");
                collection.add(this.mA2dpProfile);
                collection2.remove(this.mA2dpProfile);
            }
            if (BluetoothUuid.containsAnyUuid(parcelUuidArr, A2dpSinkProfile.SRC_UUIDS) && this.mA2dpSinkProfile != null) {
                collection.add(this.mA2dpSinkProfile);
                collection2.remove(this.mA2dpSinkProfile);
            }
            if (BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.ObexObjectPush) && this.mOppProfile != null) {
                Log.d("LocalBluetoothProfileManager", "Add OppProfile to connectable profile list");
                collection.add(this.mOppProfile);
                collection2.remove(this.mOppProfile);
            }
            if ((BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.Hid) || BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.Hogp)) && this.mHidProfile != null) {
                Log.d("LocalBluetoothProfileManager", "Add HidProfile to connectable profile list");
                collection.add(this.mHidProfile);
                collection2.remove(this.mHidProfile);
            }
            if (z) {
                Log.d("LocalBluetoothProfileManager", "Valid PAN-NAP connection exists.");
            }
            if ((BluetoothUuid.isUuidPresent(parcelUuidArr, BluetoothUuid.NAP) && this.mPanProfile != null) || z) {
                Log.d("LocalBluetoothProfileManager", "Add PanProfile to connectable profile list");
                collection.add(this.mPanProfile);
                collection2.remove(this.mPanProfile);
            }
            if (this.mMapProfile != null && this.mMapProfile.getConnectionStatus(bluetoothDevice) == 2) {
                Log.d("LocalBluetoothProfileManager", "Add MapProfile to connectable profile list");
                collection.add(this.mMapProfile);
                collection2.remove(this.mMapProfile);
                this.mMapProfile.setPreferred(bluetoothDevice, true);
            }
            if (this.mUsePbapPce) {
                collection.add(this.mPbapClientProfile);
                collection2.remove(this.mPbapClientProfile);
                collection.remove(this.mPbapProfile);
                collection2.add(this.mPbapProfile);
            }
            Log.d("LocalBluetoothProfileManager", "New Profiles" + collection.toString());
        }
    }
}
