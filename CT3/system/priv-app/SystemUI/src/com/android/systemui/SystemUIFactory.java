package com.android.systemui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.mediatek.systemui.statusbar.policy.HotKnotController;
/* loaded from: a.zip:com/android/systemui/SystemUIFactory.class */
public class SystemUIFactory {
    static SystemUIFactory mFactory;

    public static void createFromConfig(Context context) {
        String string = context.getString(2131493280);
        if (string == null || string.length() == 0) {
            throw new RuntimeException("No SystemUIFactory component configured");
        }
        try {
            mFactory = (SystemUIFactory) context.getClassLoader().loadClass(string).newInstance();
        } catch (Throwable th) {
            Log.w("SystemUIFactory", "Error creating SystemUIFactory component: " + string, th);
            throw new RuntimeException(th);
        }
    }

    public static SystemUIFactory getInstance() {
        return mFactory;
    }

    public <T> T createInstance(Class<T> cls) {
        return null;
    }

    public KeyguardBouncer createKeyguardBouncer(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, StatusBarWindowManager statusBarWindowManager, ViewGroup viewGroup) {
        return new KeyguardBouncer(context, viewMediatorCallback, lockPatternUtils, statusBarWindowManager, viewGroup);
    }

    public NotificationIconAreaController createNotificationIconAreaController(Context context, PhoneStatusBar phoneStatusBar) {
        return new NotificationIconAreaController(context, phoneStatusBar);
    }

    public QSTileHost createQSTileHost(Context context, PhoneStatusBar phoneStatusBar, BluetoothController bluetoothController, LocationController locationController, RotationLockController rotationLockController, NetworkController networkController, ZenModeController zenModeController, HotspotController hotspotController, CastController castController, FlashlightController flashlightController, UserSwitcherController userSwitcherController, UserInfoController userInfoController, KeyguardMonitor keyguardMonitor, SecurityController securityController, BatteryController batteryController, StatusBarIconController statusBarIconController, NextAlarmController nextAlarmController, HotKnotController hotKnotController) {
        return new QSTileHost(context, phoneStatusBar, bluetoothController, locationController, rotationLockController, networkController, zenModeController, hotspotController, castController, flashlightController, userSwitcherController, userInfoController, keyguardMonitor, securityController, batteryController, statusBarIconController, nextAlarmController, hotKnotController);
    }

    public ScrimController createScrimController(ScrimView scrimView, ScrimView scrimView2, View view) {
        return new ScrimController(scrimView, scrimView2, view);
    }

    public StatusBarKeyguardViewManager createStatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        return new StatusBarKeyguardViewManager(context, viewMediatorCallback, lockPatternUtils);
    }
}
