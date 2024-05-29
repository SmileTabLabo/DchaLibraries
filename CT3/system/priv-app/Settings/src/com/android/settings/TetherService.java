package com.android.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.settingslib.TetherUtil;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class TetherService extends Service {
    private static final boolean DEBUG = Log.isLoggable("TetherService", 3);
    public static final String EXTRA_RESULT = "EntitlementResult";
    private ArrayList<Integer> mCurrentTethers;
    private int mCurrentTypeIndex;
    private boolean mInProvisionCheck;
    private ArrayMap<Integer, List<ResultReceiver>> mPendingCallbacks;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.settings.TetherService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (TetherService.DEBUG) {
                Log.d("TetherService", "Got provision result " + intent);
            }
            String provisionResponse = TetherService.this.getResources().getString(17039413);
            if (!provisionResponse.equals(intent.getAction())) {
                return;
            }
            if (!TetherService.this.mInProvisionCheck) {
                Log.e("TetherService", "Unexpected provision response " + intent);
                return;
            }
            int checkType = ((Integer) TetherService.this.mCurrentTethers.get(TetherService.this.mCurrentTypeIndex)).intValue();
            TetherService.this.mInProvisionCheck = TetherService.DEBUG;
            int result = intent.getIntExtra(TetherService.EXTRA_RESULT, 0);
            if (result != -1) {
                switch (checkType) {
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
            TetherService.this.fireCallbacksForType(checkType, result);
            TetherService tetherService = TetherService.this;
            if (tetherService.mCurrentTypeIndex++ >= TetherService.this.mCurrentTethers.size()) {
                TetherService.this.stopSelf();
            } else {
                TetherService.this.startProvisioning(TetherService.this.mCurrentTypeIndex);
            }
        }
    };

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
        String provisionResponse = getResources().getString(17039413);
        registerReceiver(this.mReceiver, new IntentFilter(provisionResponse), "android.permission.CONNECTIVITY_INTERNAL", null);
        SharedPreferences prefs = getSharedPreferences("tetherPrefs", 0);
        this.mCurrentTethers = stringToTethers(prefs.getString("currentTethers", ""));
        this.mCurrentTypeIndex = 0;
        this.mPendingCallbacks = new ArrayMap<>(3);
        this.mPendingCallbacks.put(0, new ArrayList());
        this.mPendingCallbacks.put(1, new ArrayList());
        this.mPendingCallbacks.put(2, new ArrayList());
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("extraAddTetherType")) {
            int type = intent.getIntExtra("extraAddTetherType", -1);
            ResultReceiver callback = (ResultReceiver) intent.getParcelableExtra("extraProvisionCallback");
            if (callback != null) {
                List<ResultReceiver> callbacksForType = this.mPendingCallbacks.get(Integer.valueOf(type));
                if (callbacksForType != null) {
                    callbacksForType.add(callback);
                } else {
                    callback.send(1, null);
                    stopSelf();
                    return 2;
                }
            }
            if (!this.mCurrentTethers.contains(Integer.valueOf(type))) {
                if (DEBUG) {
                    Log.d("TetherService", "Adding tether " + type);
                }
                this.mCurrentTethers.add(Integer.valueOf(type));
            }
        }
        if (intent.hasExtra("extraRemTetherType")) {
            if (!this.mInProvisionCheck) {
                int type2 = intent.getIntExtra("extraRemTetherType", -1);
                int index = this.mCurrentTethers.indexOf(Integer.valueOf(type2));
                if (DEBUG) {
                    Log.d("TetherService", "Removing tether " + type2 + ", index " + index);
                }
                if (index >= 0) {
                    removeTypeAtIndex(index);
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
                Log.d("TetherService", "Stopping self.  startid: " + startId);
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
        SharedPreferences prefs = getSharedPreferences("tetherPrefs", 0);
        prefs.edit().putString("currentTethers", tethersToString(this.mCurrentTethers)).commit();
        if (DEBUG) {
            Log.d("TetherService", "Destroying TetherService");
        }
        unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    private void removeTypeAtIndex(int index) {
        this.mCurrentTethers.remove(index);
        if (DEBUG) {
            Log.d("TetherService", "mCurrentTypeIndex: " + this.mCurrentTypeIndex);
        }
        if (index > this.mCurrentTypeIndex || this.mCurrentTypeIndex <= 0) {
            return;
        }
        this.mCurrentTypeIndex--;
    }

    private ArrayList<Integer> stringToTethers(String tethersStr) {
        ArrayList<Integer> ret = new ArrayList<>();
        if (TextUtils.isEmpty(tethersStr)) {
            return ret;
        }
        String[] tethersSplit = tethersStr.split(",");
        for (String str : tethersSplit) {
            ret.add(Integer.valueOf(Integer.parseInt(str)));
        }
        return ret;
    }

    private String tethersToString(ArrayList<Integer> tethers) {
        StringBuffer buffer = new StringBuffer();
        int N = tethers.size();
        for (int i = 0; i < N; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(tethers.get(i));
        }
        return buffer.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disableWifiTethering() {
        TetherUtil.setWifiTethering(DEBUG, this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disableUsbTethering() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService("connectivity");
        cm.setUsbTethering(DEBUG);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disableBtTethering() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        adapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() { // from class: com.android.settings.TetherService.2
            @Override // android.bluetooth.BluetoothProfile.ServiceListener
            public void onServiceDisconnected(int profile) {
            }

            @Override // android.bluetooth.BluetoothProfile.ServiceListener
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                ((BluetoothPan) proxy).setBluetoothTethering((boolean) TetherService.DEBUG);
                adapter.closeProfileProxy(5, proxy);
            }
        }, 5);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startProvisioning(int index) {
        if (index >= this.mCurrentTethers.size()) {
            return;
        }
        String provisionAction = getResources().getString(17039412);
        if (DEBUG) {
            Log.d("TetherService", "Sending provisioning broadcast: " + provisionAction + " type: " + this.mCurrentTethers.get(index));
        }
        Intent intent = new Intent(provisionAction);
        int type = this.mCurrentTethers.get(index).intValue();
        intent.putExtra("TETHER_TYPE", type);
        intent.setFlags(268435456);
        sendBroadcast(intent);
        this.mInProvisionCheck = true;
    }

    private void scheduleAlarm() {
        Intent intent = new Intent(this, TetherService.class);
        intent.putExtra("extraRunProvision", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService("alarm");
        int period = getResources().getInteger(17694737);
        long periodMs = 3600000 * period;
        long firstTime = SystemClock.elapsedRealtime() + periodMs;
        if (DEBUG) {
            Log.d("TetherService", "Scheduling alarm at interval " + periodMs);
        }
        alarmManager.setRepeating(3, firstTime, periodMs, pendingIntent);
    }

    public static void cancelRecheckAlarmIfNecessary(Context context, int type) {
        Intent intent = new Intent(context, TetherService.class);
        intent.putExtra("extraRemTetherType", type);
        context.startService(intent);
    }

    private void cancelAlarmIfNecessary() {
        if (this.mCurrentTethers.size() != 0) {
            if (DEBUG) {
                Log.d("TetherService", "Tethering still active, not cancelling alarm");
                return;
            }
            return;
        }
        Intent intent = new Intent(this, TetherService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService("alarm");
        alarmManager.cancel(pendingIntent);
        if (DEBUG) {
            Log.d("TetherService", "Tethering no longer active, canceling recheck");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireCallbacksForType(int type, int result) {
        List<ResultReceiver> callbacksForType = this.mPendingCallbacks.get(Integer.valueOf(type));
        if (callbacksForType == null) {
            return;
        }
        int errorCode = result == -1 ? 0 : 11;
        for (ResultReceiver callback : callbacksForType) {
            if (DEBUG) {
                Log.d("TetherService", "Firing result: " + errorCode + " to callback");
            }
            callback.send(errorCode, null);
        }
        callbacksForType.clear();
    }
}
