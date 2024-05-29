package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import com.android.systemui.statusbar.policy.BatteryController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/BatteryControllerImpl.class */
public class BatteryControllerImpl extends BroadcastReceiver implements BatteryController {
    private static final boolean DEBUG = Log.isLoggable("BatteryController", 3);
    protected boolean mCharged;
    protected boolean mCharging;
    private final Context mContext;
    private boolean mDemoMode;
    protected int mLevel;
    protected boolean mPluggedIn;
    private final PowerManager mPowerManager;
    protected boolean mPowerSave;
    private final ArrayList<BatteryController.BatteryStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private boolean mTestmode = false;
    private final Handler mHandler = new Handler();

    public BatteryControllerImpl(Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        registerReceiver();
        updatePowerSave();
    }

    private void firePowerSaveChanged() {
        for (int i = 0; i < this.mChangeCallbacks.size(); i++) {
            BatteryController.BatteryStateChangeCallback batteryStateChangeCallback = this.mChangeCallbacks.get(i);
            if (batteryStateChangeCallback != null) {
                batteryStateChangeCallback.onPowerSaveChanged(this.mPowerSave);
            }
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGING");
        intentFilter.addAction("com.android.systemui.BATTERY_LEVEL_TEST");
        this.mContext.registerReceiver(this, intentFilter);
    }

    private void setPowerSave(boolean z) {
        if (z == this.mPowerSave) {
            return;
        }
        this.mPowerSave = z;
        if (DEBUG) {
            Log.d("BatteryController", "Power save is " + (this.mPowerSave ? "on" : "off"));
        }
        firePowerSaveChanged();
    }

    private void updatePowerSave() {
        setPowerSave(this.mPowerManager.isPowerSaveMode());
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void addStateChangedCallback(BatteryController.BatteryStateChangeCallback batteryStateChangeCallback) {
        this.mChangeCallbacks.add(batteryStateChangeCallback);
        batteryStateChangeCallback.onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
        batteryStateChangeCallback.onPowerSaveChanged(this.mPowerSave);
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            this.mContext.unregisterReceiver(this);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            registerReceiver();
            updatePowerSave();
        } else if (this.mDemoMode && str.equals("battery")) {
            String string = bundle.getString("level");
            String string2 = bundle.getString("plugged");
            if (string != null) {
                this.mLevel = Math.min(Math.max(Integer.parseInt(string), 0), 100);
            }
            if (string2 != null) {
                this.mPluggedIn = Boolean.parseBoolean(string2);
            }
            fireBatteryLevelChanged();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BatteryController state:");
        printWriter.print("  mLevel=");
        printWriter.println(this.mLevel);
        printWriter.print("  mPluggedIn=");
        printWriter.println(this.mPluggedIn);
        printWriter.print("  mCharging=");
        printWriter.println(this.mCharging);
        printWriter.print("  mCharged=");
        printWriter.println(this.mCharged);
        printWriter.print("  mPowerSave=");
        printWriter.println(this.mPowerSave);
    }

    protected void fireBatteryLevelChanged() {
        for (int i = 0; i < this.mChangeCallbacks.size(); i++) {
            BatteryController.BatteryStateChangeCallback batteryStateChangeCallback = this.mChangeCallbacks.get(i);
            if (batteryStateChangeCallback != null) {
                batteryStateChangeCallback.onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public boolean isPowerSave() {
        return this.mPowerSave;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals("android.intent.action.BATTERY_CHANGED")) {
            if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
                updatePowerSave();
            } else if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGING")) {
                setPowerSave(intent.getBooleanExtra("mode", false));
            } else if (action.equals("com.android.systemui.BATTERY_LEVEL_TEST")) {
                this.mTestmode = true;
                this.mHandler.post(new Runnable(this, context) { // from class: com.android.systemui.statusbar.policy.BatteryControllerImpl.1
                    int saveLevel;
                    boolean savePlugged;
                    final BatteryControllerImpl this$0;
                    final Context val$context;
                    int curLevel = 0;
                    int incr = 1;
                    Intent dummy = new Intent("android.intent.action.BATTERY_CHANGED");

                    {
                        this.this$0 = this;
                        this.val$context = context;
                        this.saveLevel = this.this$0.mLevel;
                        this.savePlugged = this.this$0.mPluggedIn;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        int i = 0;
                        if (this.curLevel < 0) {
                            this.this$0.mTestmode = false;
                            this.dummy.putExtra("level", this.saveLevel);
                            this.dummy.putExtra("plugged", this.savePlugged);
                            this.dummy.putExtra("testmode", false);
                        } else {
                            this.dummy.putExtra("level", this.curLevel);
                            Intent intent2 = this.dummy;
                            if (this.incr > 0) {
                                i = 1;
                            }
                            intent2.putExtra("plugged", i);
                            this.dummy.putExtra("testmode", true);
                        }
                        this.val$context.sendBroadcast(this.dummy);
                        if (this.this$0.mTestmode) {
                            this.curLevel += this.incr;
                            if (this.curLevel == 100) {
                                this.incr *= -1;
                            }
                            this.this$0.mHandler.postDelayed(this, 200L);
                        }
                    }
                });
            }
        } else if (!this.mTestmode || intent.getBooleanExtra("testmode", false)) {
            this.mLevel = (int) ((intent.getIntExtra("level", 0) * 100.0f) / intent.getIntExtra("scale", 100));
            this.mPluggedIn = intent.getIntExtra("plugged", 0) != 0;
            int intExtra = intent.getIntExtra("status", 1);
            this.mCharged = intExtra == 5;
            boolean z = true;
            if (!this.mCharged) {
                z = intExtra == 2;
            }
            this.mCharging = z;
            fireBatteryLevelChanged();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void removeStateChangedCallback(BatteryController.BatteryStateChangeCallback batteryStateChangeCallback) {
        this.mChangeCallbacks.remove(batteryStateChangeCallback);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void setPowerSaveMode(boolean z) {
        this.mPowerManager.setPowerSaveMode(z);
    }
}
