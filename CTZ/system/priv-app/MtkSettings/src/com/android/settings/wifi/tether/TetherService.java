package com.android.settings.wifi.tether;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class TetherService extends Service {
    private static final boolean DEBUG = Log.isLoggable("TetherService", 3);
    @VisibleForTesting
    public static final String EXTRA_RESULT = "EntitlementResult";
    private ArrayList<Integer> mCurrentTethers;
    private int mCurrentTypeIndex;
    private HotspotOffReceiver mHotspotReceiver;
    private boolean mInProvisionCheck;
    private ArrayMap<Integer, List<ResultReceiver>> mPendingCallbacks;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.wifi.tether.TetherService.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (TetherService.DEBUG) {
                Log.d("TetherService", "Got provision result " + intent);
            }
            if (TetherService.this.getResources().getString(17039700).equals(intent.getAction())) {
                if (TetherService.this.mInProvisionCheck) {
                    int intValue = ((Integer) TetherService.this.mCurrentTethers.get(TetherService.this.mCurrentTypeIndex)).intValue();
                    TetherService.this.mInProvisionCheck = TetherService.DEBUG;
                    int intExtra = intent.getIntExtra(TetherService.EXTRA_RESULT, 0);
                    if (intExtra != -1) {
                        switch (intValue) {
                            case 0:
                                TetherService.this.disableWifiTethering();
                                break;
                            case 1:
                                TetherService.this.disableUsbTethering();
                                break;
                            case 2:
                                TetherService.this.disableBtTethering();
                                break;
                        }
                    }
                    TetherService.this.fireCallbacksForType(intValue, intExtra);
                    if (TetherService.access$204(TetherService.this) < TetherService.this.mCurrentTethers.size()) {
                        TetherService.this.startProvisioning(TetherService.this.mCurrentTypeIndex);
                        return;
                    } else {
                        TetherService.this.stopSelf();
                        return;
                    }
                }
                Log.e("TetherService", "Unexpected provision response " + intent);
            }
        }
    };
    private UsageStatsManagerWrapper mUsageManagerWrapper;

    static /* synthetic */ int access$204(TetherService tetherService) {
        int i = tetherService.mCurrentTypeIndex + 1;
        tetherService.mCurrentTypeIndex = i;
        return i;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            Log.d("TetherService", "Creating TetherService");
        }
        registerReceiver(this.mReceiver, new IntentFilter(getResources().getString(17039700)), "android.permission.CONNECTIVITY_INTERNAL", null);
        this.mCurrentTethers = stringToTethers(getSharedPreferences("tetherPrefs", 0).getString("currentTethers", ""));
        this.mCurrentTypeIndex = 0;
        this.mPendingCallbacks = new ArrayMap<>(3);
        this.mPendingCallbacks.put(0, new ArrayList());
        this.mPendingCallbacks.put(1, new ArrayList());
        this.mPendingCallbacks.put(2, new ArrayList());
        if (this.mUsageManagerWrapper == null) {
            this.mUsageManagerWrapper = new UsageStatsManagerWrapper(this);
        }
        this.mHotspotReceiver = new HotspotOffReceiver(this);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent.hasExtra("extraAddTetherType")) {
            int intExtra = intent.getIntExtra("extraAddTetherType", -1);
            ResultReceiver resultReceiver = (ResultReceiver) intent.getParcelableExtra("extraProvisionCallback");
            if (resultReceiver != null) {
                List<ResultReceiver> list = this.mPendingCallbacks.get(Integer.valueOf(intExtra));
                if (list != null) {
                    list.add(resultReceiver);
                } else {
                    resultReceiver.send(1, null);
                    stopSelf();
                    return 2;
                }
            }
            if (!this.mCurrentTethers.contains(Integer.valueOf(intExtra))) {
                if (DEBUG) {
                    Log.d("TetherService", "Adding tether " + intExtra);
                }
                this.mCurrentTethers.add(Integer.valueOf(intExtra));
            }
        }
        if (intent.hasExtra("extraRemTetherType")) {
            if (!this.mInProvisionCheck) {
                int intExtra2 = intent.getIntExtra("extraRemTetherType", -1);
                int indexOf = this.mCurrentTethers.indexOf(Integer.valueOf(intExtra2));
                if (DEBUG) {
                    Log.d("TetherService", "Removing tether " + intExtra2 + ", index " + indexOf);
                }
                if (indexOf >= 0) {
                    removeTypeAtIndex(indexOf);
                }
                cancelAlarmIfNecessary();
            } else if (DEBUG) {
                Log.d("TetherService", "Don't cancel alarm during provisioning");
            }
        }
        if (intent.getBooleanExtra("extraSetAlarm", DEBUG) && this.mCurrentTethers.size() == 1) {
            scheduleAlarm();
        }
        if (intent.getBooleanExtra("extraRunProvision", DEBUG)) {
            startProvisioning(this.mCurrentTypeIndex);
            return 3;
        } else if (!this.mInProvisionCheck) {
            if (DEBUG) {
                Log.d("TetherService", "Stopping self.  startid: " + i2);
            }
            stopSelf();
            return 2;
        } else {
            return 3;
        }
    }

    @Override // android.app.Service
    public void onDestroy() {
        if (this.mInProvisionCheck) {
            Log.e("TetherService", "TetherService getting destroyed while mid-provisioning" + this.mCurrentTethers.get(this.mCurrentTypeIndex));
        }
        getSharedPreferences("tetherPrefs", 0).edit().putString("currentTethers", tethersToString(this.mCurrentTethers)).commit();
        unregisterReceivers();
        if (DEBUG) {
            Log.d("TetherService", "Destroying TetherService");
        }
        super.onDestroy();
    }

    private void unregisterReceivers() {
        unregisterReceiver(this.mReceiver);
        this.mHotspotReceiver.unregister();
    }

    private void removeTypeAtIndex(int i) {
        this.mCurrentTethers.remove(i);
        if (DEBUG) {
            Log.d("TetherService", "mCurrentTypeIndex: " + this.mCurrentTypeIndex);
        }
        if (i <= this.mCurrentTypeIndex && this.mCurrentTypeIndex > 0) {
            this.mCurrentTypeIndex--;
        }
    }

    @VisibleForTesting
    void setHotspotOffReceiver(HotspotOffReceiver hotspotOffReceiver) {
        this.mHotspotReceiver = hotspotOffReceiver;
    }

    private ArrayList<Integer> stringToTethers(String str) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return arrayList;
        }
        for (String str2 : str.split(",")) {
            arrayList.add(Integer.valueOf(Integer.parseInt(str2)));
        }
        return arrayList;
    }

    private String tethersToString(ArrayList<Integer> arrayList) {
        StringBuffer stringBuffer = new StringBuffer();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                stringBuffer.append(',');
            }
            stringBuffer.append(arrayList.get(i));
        }
        return stringBuffer.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disableWifiTethering() {
        ((ConnectivityManager) getSystemService("connectivity")).stopTethering(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disableUsbTethering() {
        ((ConnectivityManager) getSystemService("connectivity")).setUsbTethering(DEBUG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disableBtTethering() {
        final BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            defaultAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() { // from class: com.android.settings.wifi.tether.TetherService.1
                @Override // android.bluetooth.BluetoothProfile.ServiceListener
                public void onServiceDisconnected(int i) {
                }

                @Override // android.bluetooth.BluetoothProfile.ServiceListener
                public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
                    ((BluetoothPan) bluetoothProfile).setBluetoothTethering((boolean) TetherService.DEBUG);
                    defaultAdapter.closeProfileProxy(5, bluetoothProfile);
                }
            }, 5);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startProvisioning(int i) {
        if (i < this.mCurrentTethers.size()) {
            Intent provisionBroadcastIntent = getProvisionBroadcastIntent(i);
            setEntitlementAppActive(i);
            if (DEBUG) {
                Log.d("TetherService", "Sending provisioning broadcast: " + provisionBroadcastIntent.getAction() + " type: " + this.mCurrentTethers.get(i));
            }
            sendBroadcast(provisionBroadcastIntent);
            this.mInProvisionCheck = true;
        }
    }

    private Intent getProvisionBroadcastIntent(int i) {
        Intent intent = new Intent(getResources().getString(17039699));
        intent.putExtra("TETHER_TYPE", this.mCurrentTethers.get(i).intValue());
        intent.setFlags(285212672);
        return intent;
    }

    private void setEntitlementAppActive(int i) {
        List<ResolveInfo> queryBroadcastReceivers = getPackageManager().queryBroadcastReceivers(getProvisionBroadcastIntent(i), 131072);
        if (queryBroadcastReceivers.isEmpty()) {
            Log.e("TetherService", "No found BroadcastReceivers for provision intent.");
            return;
        }
        for (ResolveInfo resolveInfo : queryBroadcastReceivers) {
            if (resolveInfo.activityInfo.applicationInfo.isSystemApp()) {
                this.mUsageManagerWrapper.setAppInactive(resolveInfo.activityInfo.packageName, DEBUG);
            }
        }
    }

    @VisibleForTesting
    void scheduleAlarm() {
        Intent intent = new Intent(this, TetherService.class);
        intent.putExtra("extraRunProvision", true);
        PendingIntent service = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService("alarm");
        long integer = getResources().getInteger(17694820) * 3600000;
        long elapsedRealtime = SystemClock.elapsedRealtime() + integer;
        if (DEBUG) {
            Log.d("TetherService", "Scheduling alarm at interval " + integer);
        }
        alarmManager.setRepeating(3, elapsedRealtime, integer, service);
        this.mHotspotReceiver.register();
    }

    public static void cancelRecheckAlarmIfNecessary(Context context, int i) {
        Intent intent = new Intent(context, TetherService.class);
        intent.putExtra("extraRemTetherType", i);
        context.startService(intent);
    }

    @VisibleForTesting
    void cancelAlarmIfNecessary() {
        if (this.mCurrentTethers.size() != 0) {
            if (DEBUG) {
                Log.d("TetherService", "Tethering still active, not cancelling alarm");
                return;
            }
            return;
        }
        ((AlarmManager) getSystemService("alarm")).cancel(PendingIntent.getService(this, 0, new Intent(this, TetherService.class), 0));
        if (DEBUG) {
            Log.d("TetherService", "Tethering no longer active, canceling recheck");
        }
        this.mHotspotReceiver.unregister();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireCallbacksForType(int i, int i2) {
        List<ResultReceiver> list = this.mPendingCallbacks.get(Integer.valueOf(i));
        if (list == null) {
            return;
        }
        int i3 = i2 == -1 ? 0 : 11;
        for (ResultReceiver resultReceiver : list) {
            if (DEBUG) {
                Log.d("TetherService", "Firing result: " + i3 + " to callback");
            }
            resultReceiver.send(i3, null);
        }
        list.clear();
    }

    @VisibleForTesting
    void setUsageStatsManagerWrapper(UsageStatsManagerWrapper usageStatsManagerWrapper) {
        this.mUsageManagerWrapper = usageStatsManagerWrapper;
    }

    @VisibleForTesting
    /* loaded from: classes.dex */
    public static class UsageStatsManagerWrapper {
        private final UsageStatsManager mUsageStatsManager;

        UsageStatsManagerWrapper(Context context) {
            this.mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        }

        void setAppInactive(String str, boolean z) {
            this.mUsageStatsManager.setAppInactive(str, z);
        }
    }
}
