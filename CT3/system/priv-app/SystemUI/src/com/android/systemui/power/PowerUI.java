package com.android.systemui.power;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
/* loaded from: a.zip:com/android/systemui/power/PowerUI.class */
public class PowerUI extends SystemUI {
    static final boolean DEBUG = Log.isLoggable("PowerUI", 3);
    private int mLowBatteryAlertCloseLevel;
    private PowerManager mPowerManager;
    private WarningsUI mWarnings;
    private final Handler mHandler = new Handler();
    private final Receiver mReceiver = new Receiver(this, null);
    private int mBatteryLevel = 100;
    private int mBatteryStatus = 1;
    private int mPlugType = 0;
    private int mInvalidCharger = 0;
    private final int[] mLowBatteryReminderLevels = new int[2];
    private long mScreenOffTime = -1;

    /* loaded from: a.zip:com/android/systemui/power/PowerUI$Receiver.class */
    private final class Receiver extends BroadcastReceiver {
        final PowerUI this$0;

        private Receiver(PowerUI powerUI) {
            this.this$0 = powerUI;
        }

        /* synthetic */ Receiver(PowerUI powerUI, Receiver receiver) {
            this(powerUI);
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGING");
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
            this.this$0.mContext.registerReceiver(this, intentFilter, null, this.this$0.mHandler);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals("android.intent.action.BATTERY_CHANGED")) {
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    this.this$0.mScreenOffTime = SystemClock.elapsedRealtime();
                    return;
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    this.this$0.mScreenOffTime = -1L;
                    return;
                } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    this.this$0.mWarnings.userSwitched();
                    return;
                } else {
                    Slog.w("PowerUI", "unknown intent: " + intent);
                    return;
                }
            }
            int i = this.this$0.mBatteryLevel;
            this.this$0.mBatteryLevel = intent.getIntExtra("level", 100);
            int i2 = this.this$0.mBatteryStatus;
            this.this$0.mBatteryStatus = intent.getIntExtra("status", 1);
            int i3 = this.this$0.mPlugType;
            this.this$0.mPlugType = intent.getIntExtra("plugged", 1);
            int i4 = this.this$0.mInvalidCharger;
            this.this$0.mInvalidCharger = intent.getIntExtra("invalid_charger", 0);
            boolean z = this.this$0.mPlugType != 0;
            boolean z2 = i3 != 0;
            int findBatteryLevelBucket = this.this$0.findBatteryLevelBucket(i);
            int findBatteryLevelBucket2 = this.this$0.findBatteryLevelBucket(this.this$0.mBatteryLevel);
            if (PowerUI.DEBUG) {
                Slog.d("PowerUI", "buckets   ....." + this.this$0.mLowBatteryAlertCloseLevel + " .. " + this.this$0.mLowBatteryReminderLevels[0] + " .. " + this.this$0.mLowBatteryReminderLevels[1]);
                Slog.d("PowerUI", "level          " + i + " --> " + this.this$0.mBatteryLevel);
                Slog.d("PowerUI", "status         " + i2 + " --> " + this.this$0.mBatteryStatus);
                Slog.d("PowerUI", "plugType       " + i3 + " --> " + this.this$0.mPlugType);
                Slog.d("PowerUI", "invalidCharger " + i4 + " --> " + this.this$0.mInvalidCharger);
                Slog.d("PowerUI", "bucket         " + findBatteryLevelBucket + " --> " + findBatteryLevelBucket2);
                Slog.d("PowerUI", "plugged        " + z2 + " --> " + z);
            }
            this.this$0.mWarnings.update(this.this$0.mBatteryLevel, findBatteryLevelBucket2, this.this$0.mScreenOffTime);
            if (i4 == 0 && this.this$0.mInvalidCharger != 0) {
                Slog.d("PowerUI", "showing invalid charger warning");
                this.this$0.mWarnings.showInvalidChargerWarning();
                return;
            }
            if (i4 != 0 && this.this$0.mInvalidCharger == 0) {
                this.this$0.mWarnings.dismissInvalidChargerWarning();
            } else if (this.this$0.mWarnings.isInvalidChargerWarningShowing()) {
                return;
            }
            boolean isPowerSaveMode = this.this$0.mPowerManager.isPowerSaveMode();
            if (!z && !isPowerSaveMode && ((findBatteryLevelBucket2 < findBatteryLevelBucket || z2) && this.this$0.mBatteryStatus != 1 && findBatteryLevelBucket2 < 0)) {
                if (findBatteryLevelBucket2 != findBatteryLevelBucket) {
                    z2 = true;
                }
                this.this$0.mWarnings.showLowBatteryWarning(z2);
            } else if (isPowerSaveMode || z || (findBatteryLevelBucket2 > findBatteryLevelBucket && findBatteryLevelBucket2 > 0)) {
                this.this$0.mWarnings.dismissLowBatteryWarning();
            } else {
                this.this$0.mWarnings.updateLowBatteryWarning();
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/power/PowerUI$WarningsUI.class */
    public interface WarningsUI {
        void dismissInvalidChargerWarning();

        void dismissLowBatteryWarning();

        void dump(PrintWriter printWriter);

        boolean isInvalidChargerWarningShowing();

        void showInvalidChargerWarning();

        void showLowBatteryWarning(boolean z);

        void update(int i, int i2, long j);

        void updateLowBatteryWarning();

        void userSwitched();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int findBatteryLevelBucket(int i) {
        if (i >= this.mLowBatteryAlertCloseLevel) {
            return 1;
        }
        if (i > this.mLowBatteryReminderLevels[0]) {
            return 0;
        }
        for (int length = this.mLowBatteryReminderLevels.length - 1; length >= 0; length--) {
            if (i <= this.mLowBatteryReminderLevels[length]) {
                return (-1) - length;
            }
        }
        throw new RuntimeException("not possible!");
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("mLowBatteryAlertCloseLevel=");
        printWriter.println(this.mLowBatteryAlertCloseLevel);
        printWriter.print("mLowBatteryReminderLevels=");
        printWriter.println(Arrays.toString(this.mLowBatteryReminderLevels));
        printWriter.print("mBatteryLevel=");
        printWriter.println(Integer.toString(this.mBatteryLevel));
        printWriter.print("mBatteryStatus=");
        printWriter.println(Integer.toString(this.mBatteryStatus));
        printWriter.print("mPlugType=");
        printWriter.println(Integer.toString(this.mPlugType));
        printWriter.print("mInvalidCharger=");
        printWriter.println(Integer.toString(this.mInvalidCharger));
        printWriter.print("mScreenOffTime=");
        printWriter.print(this.mScreenOffTime);
        if (this.mScreenOffTime >= 0) {
            printWriter.print(" (");
            printWriter.print(SystemClock.elapsedRealtime() - this.mScreenOffTime);
            printWriter.print(" ago)");
        }
        printWriter.println();
        printWriter.print("soundTimeout=");
        printWriter.println(Settings.Global.getInt(this.mContext.getContentResolver(), "low_battery_sound_timeout", 0));
        printWriter.print("bucket: ");
        printWriter.println(Integer.toString(findBatteryLevelBucket(this.mBatteryLevel)));
        this.mWarnings.dump(printWriter);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mScreenOffTime = this.mPowerManager.isScreenOn() ? -1L : SystemClock.elapsedRealtime();
        this.mWarnings = new PowerNotificationWarnings(this.mContext, (PhoneStatusBar) getComponent(PhoneStatusBar.class));
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), false, new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.power.PowerUI.1
            final PowerUI this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                this.this$0.updateBatteryWarningLevels();
            }
        }, -1);
        updateBatteryWarningLevels();
        this.mReceiver.init();
    }

    void updateBatteryWarningLevels() {
        int integer = this.mContext.getResources().getInteger(17694798);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int integer2 = this.mContext.getResources().getInteger(17694800);
        int i = Settings.Global.getInt(contentResolver, "low_power_trigger_level", integer2);
        int i2 = i;
        if (i == 0) {
            i2 = integer2;
        }
        int i3 = i2;
        if (i2 < integer) {
            i3 = integer;
        }
        this.mLowBatteryReminderLevels[0] = i3;
        this.mLowBatteryReminderLevels[1] = integer;
        this.mLowBatteryAlertCloseLevel = this.mLowBatteryReminderLevels[0] + this.mContext.getResources().getInteger(17694801);
    }
}
