package com.android.systemui.statusbar.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.car.CarBatteryController;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.policy.BatteryController;
/* loaded from: a.zip:com/android/systemui/statusbar/car/CarStatusBar.class */
public class CarStatusBar extends PhoneStatusBar implements CarBatteryController.BatteryViewHandler {
    private BatteryMeterView mBatteryMeterView;
    private CarBatteryController mCarBatteryController;
    private CarNavigationBarView mCarNavigationBar;
    private CarNavigationBarController mController;
    private FullscreenUserSwitcher mFullscreenUserSwitcher;
    private BroadcastReceiver mPackageChangeReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.car.CarStatusBar.1
        final CarStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getData() == null || this.this$0.mController == null) {
                return;
            }
            this.this$0.mController.onPackageChange(intent.getData().getSchemeSpecificPart());
        }
    };
    private TaskStackListenerImpl mTaskStackListener;

    /* loaded from: a.zip:com/android/systemui/statusbar/car/CarStatusBar$TaskStackListenerImpl.class */
    private class TaskStackListenerImpl extends SystemServicesProxy.TaskStackListener {
        final CarStatusBar this$0;

        private TaskStackListenerImpl(CarStatusBar carStatusBar) {
            this.this$0 = carStatusBar;
        }

        /* synthetic */ TaskStackListenerImpl(CarStatusBar carStatusBar, TaskStackListenerImpl taskStackListenerImpl) {
            this(carStatusBar);
        }

        @Override // com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener
        public void onTaskStackChanged() {
            this.this$0.mController.taskChanged(Recents.getSystemServices().getRunningTask().baseActivity.getPackageName());
        }
    }

    private void registerPackageChangeReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mPackageChangeReceiver, intentFilter);
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar
    protected void addNavigationBar() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2019, 25428072, -3);
        layoutParams.setTitle("CarNavigationBar");
        layoutParams.windowAnimations = 0;
        this.mWindowManager.addView(this.mNavigationBarView, layoutParams);
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar
    protected BatteryController createBatteryController() {
        this.mCarBatteryController = new CarBatteryController(this.mContext);
        this.mCarBatteryController.addBatteryViewHandler(this);
        return this.mCarBatteryController;
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar
    protected void createNavigationBarView(Context context) {
        if (this.mNavigationBarView != null) {
            return;
        }
        this.mCarNavigationBar = (CarNavigationBarView) View.inflate(context, 2130968609, null);
        this.mController = new CarNavigationBarController(context, this.mCarNavigationBar, this);
        this.mNavigationBarView = this.mCarNavigationBar;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar
    public void createUserSwitcher() {
        if (this.mUserSwitcherController.useFullscreenUserSwitcher()) {
            this.mFullscreenUserSwitcher = new FullscreenUserSwitcher(this, this.mUserSwitcherController, (ViewStub) this.mStatusBarWindow.findViewById(2131886714));
        } else {
            super.createUserSwitcher();
        }
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar, com.android.systemui.statusbar.BaseStatusBar
    public void destroy() {
        this.mCarBatteryController.stopListening();
        super.destroy();
    }

    @Override // com.android.systemui.statusbar.car.CarBatteryController.BatteryViewHandler
    public void hideBatteryView() {
        if (Log.isLoggable("CarStatusBar", 3)) {
            Log.d("CarStatusBar", "hideBatteryView(). mBatteryMeterView: " + this.mBatteryMeterView);
        }
        if (this.mBatteryMeterView != null) {
            this.mBatteryMeterView.setVisibility(8);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar
    public PhoneStatusBarView makeStatusBarView() {
        PhoneStatusBarView makeStatusBarView = super.makeStatusBarView();
        this.mBatteryMeterView = (BatteryMeterView) makeStatusBarView.findViewById(2131886720);
        this.mBatteryMeterView.setVisibility(8);
        if (Log.isLoggable("CarStatusBar", 3)) {
            Log.d("CarStatusBar", "makeStatusBarView(). mBatteryMeterView: " + this.mBatteryMeterView);
        }
        return makeStatusBarView;
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar
    protected void repositionNavigationBar() {
    }

    @Override // com.android.systemui.statusbar.car.CarBatteryController.BatteryViewHandler
    public void showBatteryView() {
        if (Log.isLoggable("CarStatusBar", 3)) {
            Log.d("CarStatusBar", "showBatteryView(). mBatteryMeterView: " + this.mBatteryMeterView);
        }
        if (this.mBatteryMeterView != null) {
            this.mBatteryMeterView.setVisibility(0);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar, com.android.systemui.statusbar.BaseStatusBar, com.android.systemui.SystemUI
    public void start() {
        super.start();
        this.mTaskStackListener = new TaskStackListenerImpl(this, null);
        SystemServicesProxy.getInstance(this.mContext).registerTaskStackListener(this.mTaskStackListener);
        registerPackageChangeReceivers();
        this.mCarBatteryController.startListening();
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar
    public void updateKeyguardState(boolean z, boolean z2) {
        super.updateKeyguardState(z, z2);
        if (this.mFullscreenUserSwitcher != null) {
            if (this.mState == 3) {
                this.mFullscreenUserSwitcher.show();
            } else {
                this.mFullscreenUserSwitcher.hide();
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBar, com.android.systemui.statusbar.BaseStatusBar
    public void userSwitched(int i) {
        super.userSwitched(i);
        if (this.mFullscreenUserSwitcher != null) {
            this.mFullscreenUserSwitcher.onUserSwitched(i);
        }
    }
}
