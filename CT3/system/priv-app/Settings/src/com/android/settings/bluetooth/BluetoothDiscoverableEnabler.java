package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
/* loaded from: classes.dex */
final class BluetoothDiscoverableEnabler implements Preference.OnPreferenceClickListener {
    private Context mContext;
    private boolean mDiscoverable;
    private final Preference mDiscoveryPreference;
    private final LocalBluetoothAdapter mLocalAdapter;
    private int mNumberOfPairedDevices;
    private final SharedPreferences mSharedPreferences;
    private int mTimeoutSecs;
    private final Handler mUiHandler;
    private final Runnable mUpdateCountdownSummaryRunnable;

    /* renamed from: com.android.settings.bluetooth.BluetoothDiscoverableEnabler$1  reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ BluetoothDiscoverableEnabler this$0;

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int mode;
            if (!"android.bluetooth.adapter.action.SCAN_MODE_CHANGED".equals(intent.getAction()) || (mode = intent.getIntExtra("android.bluetooth.adapter.extra.SCAN_MODE", Integer.MIN_VALUE)) == Integer.MIN_VALUE) {
                return;
            }
            this.this$0.handleModeChanged(mode);
        }
    }

    /* renamed from: com.android.settings.bluetooth.BluetoothDiscoverableEnabler$2  reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ BluetoothDiscoverableEnabler this$0;

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.updateCountdownSummary();
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        this.mDiscoverable = !this.mDiscoverable;
        setEnabled(this.mDiscoverable);
        return true;
    }

    private void setEnabled(boolean enable) {
        if (enable) {
            int timeout = getDiscoverableTimeout();
            long endTimestamp = System.currentTimeMillis() + (timeout * 1000);
            LocalBluetoothPreferences.persistDiscoverableEndTimestamp(this.mContext, endTimestamp);
            this.mLocalAdapter.setScanMode(23, timeout);
            updateCountdownSummary();
            Log.d("BluetoothDiscoverableEnabler", "setEnabled(): enabled = " + enable + "timeout = " + timeout);
            if (timeout > 0) {
                BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(this.mContext, endTimestamp);
                return;
            } else {
                BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(this.mContext);
                return;
            }
        }
        this.mLocalAdapter.setScanMode(21);
        BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(this.mContext);
    }

    private void updateTimerDisplay(int timeout) {
        if (getDiscoverableTimeout() == 0) {
            this.mDiscoveryPreference.setSummary(R.string.bluetooth_is_discoverable_always);
            return;
        }
        String textTimeout = formatTimeRemaining(timeout);
        this.mDiscoveryPreference.setSummary(this.mContext.getString(R.string.bluetooth_is_discoverable, textTimeout));
    }

    private static String formatTimeRemaining(int timeout) {
        StringBuilder sb = new StringBuilder(6);
        int min = timeout / 60;
        sb.append(min).append(':');
        int sec = timeout - (min * 60);
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        return sb.toString();
    }

    private int getDiscoverableTimeout() {
        if (this.mTimeoutSecs != -1) {
            return this.mTimeoutSecs;
        }
        int timeout = SystemProperties.getInt("debug.bt.discoverable_time", -1);
        if (timeout < 0) {
            String timeoutValue = this.mSharedPreferences.getString("bt_discoverable_timeout", "twomin");
            if (timeoutValue.equals("never")) {
                timeout = 0;
            } else if (timeoutValue.equals("onehour")) {
                timeout = 3600;
            } else if (timeoutValue.equals("fivemin")) {
                timeout = 300;
            } else {
                timeout = 120;
            }
        }
        this.mTimeoutSecs = timeout;
        return timeout;
    }

    void handleModeChanged(int mode) {
        Log.d("BluetoothDiscoverableEnabler", "handleModeChanged(): mode = " + mode);
        if (mode == 23) {
            this.mDiscoverable = true;
            updateCountdownSummary();
            return;
        }
        this.mDiscoverable = false;
        setSummaryNotDiscoverable();
    }

    private void setSummaryNotDiscoverable() {
        if (this.mNumberOfPairedDevices != 0) {
            this.mDiscoveryPreference.setSummary(R.string.bluetooth_only_visible_to_paired_devices);
        } else {
            this.mDiscoveryPreference.setSummary(R.string.bluetooth_not_visible_to_other_devices);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCountdownSummary() {
        int mode = this.mLocalAdapter.getScanMode();
        if (mode != 23) {
            return;
        }
        long currentTimestamp = System.currentTimeMillis();
        long endTimestamp = LocalBluetoothPreferences.getDiscoverableEndTimestamp(this.mContext);
        if (currentTimestamp > endTimestamp) {
            updateTimerDisplay(0);
            return;
        }
        int timeLeft = (int) ((endTimestamp - currentTimestamp) / 1000);
        updateTimerDisplay(timeLeft);
        synchronized (this) {
            this.mUiHandler.removeCallbacks(this.mUpdateCountdownSummaryRunnable);
            this.mUiHandler.postDelayed(this.mUpdateCountdownSummaryRunnable, 1000L);
        }
    }
}
