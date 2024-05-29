package com.android.systemui.statusbar.policy;

import com.android.systemui.DemoMode;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/BatteryController.class */
public interface BatteryController extends DemoMode {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/BatteryController$BatteryStateChangeCallback.class */
    public interface BatteryStateChangeCallback {
        void onBatteryLevelChanged(int i, boolean z, boolean z2);

        void onPowerSaveChanged(boolean z);
    }

    void addStateChangedCallback(BatteryStateChangeCallback batteryStateChangeCallback);

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    boolean isPowerSave();

    void removeStateChangedCallback(BatteryStateChangeCallback batteryStateChangeCallback);

    void setPowerSaveMode(boolean z);
}
