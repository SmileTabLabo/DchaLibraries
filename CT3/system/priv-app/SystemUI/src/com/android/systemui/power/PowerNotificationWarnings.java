package com.android.systemui.power;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.systemui.SystemUI;
import com.android.systemui.power.PowerUI;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.io.PrintWriter;
import java.text.NumberFormat;
/* loaded from: a.zip:com/android/systemui/power/PowerNotificationWarnings.class */
public class PowerNotificationWarnings implements PowerUI.WarningsUI {
    private int mBatteryLevel;
    private int mBucket;
    private long mBucketDroppedNegativeTimeMs;
    private final Context mContext;
    private boolean mInvalidCharger;
    private final NotificationManager mNoMan;
    private boolean mPlaySound;
    private final PowerManager mPowerMan;
    private SystemUIDialog mSaverConfirmation;
    private long mScreenOffTime;
    private int mShowing;
    private boolean mWarning;
    private static final boolean DEBUG = PowerUI.DEBUG;
    private static final String[] SHOWING_STRINGS = {"SHOWING_NOTHING", "SHOWING_WARNING", "SHOWING_SAVER", "SHOWING_INVALID_CHARGER"};
    private static final AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private final Handler mHandler = new Handler();
    private final Receiver mReceiver = new Receiver(this, null);
    private final Intent mOpenBatterySettings = settings("android.intent.action.POWER_USAGE_SUMMARY");
    private final DialogInterface.OnClickListener mStartSaverMode = new AnonymousClass1(this);

    /* renamed from: com.android.systemui.power.PowerNotificationWarnings$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/power/PowerNotificationWarnings$1.class */
    class AnonymousClass1 implements DialogInterface.OnClickListener {
        final PowerNotificationWarnings this$0;

        AnonymousClass1(PowerNotificationWarnings powerNotificationWarnings) {
            this.this$0 = powerNotificationWarnings;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            AsyncTask.execute(new Runnable(this) { // from class: com.android.systemui.power.PowerNotificationWarnings.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.setSaverMode(true);
                }
            });
        }
    }

    /* loaded from: a.zip:com/android/systemui/power/PowerNotificationWarnings$Receiver.class */
    private final class Receiver extends BroadcastReceiver {
        final PowerNotificationWarnings this$0;

        private Receiver(PowerNotificationWarnings powerNotificationWarnings) {
            this.this$0 = powerNotificationWarnings;
        }

        /* synthetic */ Receiver(PowerNotificationWarnings powerNotificationWarnings, Receiver receiver) {
            this(powerNotificationWarnings);
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("PNW.batterySettings");
            intentFilter.addAction("PNW.startSaver");
            intentFilter.addAction("PNW.dismissedWarning");
            this.this$0.mContext.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, "android.permission.STATUS_BAR_SERVICE", this.this$0.mHandler);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.i("PowerUI.Notification", "Received " + action);
            if (action.equals("PNW.batterySettings")) {
                this.this$0.dismissLowBatteryNotification();
                if (BenesseExtension.getDchaState() == 0) {
                    this.this$0.mContext.startActivityAsUser(this.this$0.mOpenBatterySettings, UserHandle.CURRENT);
                }
            } else if (action.equals("PNW.startSaver")) {
                this.this$0.dismissLowBatteryNotification();
                this.this$0.showStartSaverConfirmation();
            } else if (action.equals("PNW.dismissedWarning")) {
                this.this$0.dismissLowBatteryWarning();
            }
        }
    }

    public PowerNotificationWarnings(Context context, PhoneStatusBar phoneStatusBar) {
        this.mContext = context;
        this.mNoMan = (NotificationManager) context.getSystemService("notification");
        this.mPowerMan = (PowerManager) context.getSystemService("power");
        this.mReceiver.init();
    }

    private void attachLowBatterySound(Notification.Builder builder) {
        String string;
        Uri parse;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int i = Settings.Global.getInt(contentResolver, "low_battery_sound_timeout", 0);
        long elapsedRealtime = SystemClock.elapsedRealtime() - this.mScreenOffTime;
        if (i > 0 && this.mScreenOffTime > 0 && elapsedRealtime > i) {
            Slog.i("PowerUI.Notification", "screen off too long (" + elapsedRealtime + "ms, limit " + i + "ms): not waking up the user with low battery sound");
            return;
        }
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "playing low battery sound. pick-a-doop!");
        }
        if (Settings.Global.getInt(contentResolver, "power_sounds_enabled", 1) != 1 || (string = Settings.Global.getString(contentResolver, "low_battery_sound")) == null || (parse = Uri.parse("file://" + string)) == null) {
            return;
        }
        builder.setSound(parse, AUDIO_ATTRIBUTES);
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "playing sound " + parse);
        }
    }

    private void dismissInvalidChargerNotification() {
        if (this.mInvalidCharger) {
            Slog.i("PowerUI.Notification", "dismissing invalid charger notification");
        }
        this.mInvalidCharger = false;
        updateNotification();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissLowBatteryNotification() {
        if (this.mWarning) {
            Slog.i("PowerUI.Notification", "dismissing low battery notification");
        }
        this.mWarning = false;
        updateNotification();
    }

    private boolean hasBatterySettings() {
        boolean z = false;
        if (this.mOpenBatterySettings.resolveActivity(this.mContext.getPackageManager()) != null) {
            z = false;
            if (BenesseExtension.getDchaState() == 0) {
                z = true;
            }
        }
        return z;
    }

    private PendingIntent pendingBroadcast(String str) {
        return PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(str), 0, UserHandle.CURRENT);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSaverMode(boolean z) {
        this.mPowerMan.setPowerSaveMode(z);
    }

    private static Intent settings(String str) {
        return new Intent(str).setFlags(1551892480);
    }

    private void showInvalidChargerNotification() {
        Notification.Builder color = new Notification.Builder(this.mContext).setSmallIcon(2130837697).setWhen(0L).setShowWhen(false).setOngoing(true).setContentTitle(this.mContext.getString(2131493302)).setContentText(this.mContext.getString(2131493303)).setPriority(2).setVisibility(1).setColor(this.mContext.getColor(17170521));
        SystemUI.overrideNotificationAppName(this.mContext, color);
        this.mNoMan.notifyAsUser("low_battery", 2131886132, color.build(), UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showStartSaverConfirmation() {
        if (this.mSaverConfirmation != null) {
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(2131493305);
        systemUIDialog.setMessage(17040813);
        systemUIDialog.setNegativeButton(17039360, null);
        systemUIDialog.setPositiveButton(2131493306, this.mStartSaverMode);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener(this) { // from class: com.android.systemui.power.PowerNotificationWarnings.2
            final PowerNotificationWarnings this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                this.this$0.mSaverConfirmation = null;
            }
        });
        systemUIDialog.show();
        this.mSaverConfirmation = systemUIDialog;
    }

    private void showWarningNotification() {
        Notification.Builder color = new Notification.Builder(this.mContext).setSmallIcon(2130837697).setWhen(this.mBucketDroppedNegativeTimeMs).setShowWhen(false).setContentTitle(this.mContext.getString(2131493298)).setContentText(this.mContext.getString(2131493299, NumberFormat.getPercentInstance().format(this.mBatteryLevel / 100.0d))).setOnlyAlertOnce(true).setDeleteIntent(pendingBroadcast("PNW.dismissedWarning")).setPriority(2).setVisibility(1).setColor(this.mContext.getColor(17170522));
        if (BenesseExtension.getDchaState() == 0) {
            if (hasBatterySettings()) {
                color.setContentIntent(pendingBroadcast("PNW.batterySettings"));
            }
            color.addAction(0, this.mContext.getString(2131493307), pendingBroadcast("PNW.startSaver"));
        }
        if (this.mPlaySound) {
            attachLowBatterySound(color);
            this.mPlaySound = false;
        }
        SystemUI.overrideNotificationAppName(this.mContext, color);
        this.mNoMan.notifyAsUser("low_battery", 2131886132, color.build(), UserHandle.ALL);
    }

    private void updateNotification() {
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "updateNotification mWarning=" + this.mWarning + " mPlaySound=" + this.mPlaySound + " mInvalidCharger=" + this.mInvalidCharger);
        }
        if (this.mInvalidCharger) {
            showInvalidChargerNotification();
            this.mShowing = 3;
        } else if (this.mWarning) {
            showWarningNotification();
            this.mShowing = 1;
        } else {
            this.mNoMan.cancelAsUser("low_battery", 2131886132, UserHandle.ALL);
            this.mShowing = 0;
        }
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissInvalidChargerWarning() {
        dismissInvalidChargerNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissLowBatteryWarning() {
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "dismissing low battery warning: level=" + this.mBatteryLevel);
        }
        dismissLowBatteryNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dump(PrintWriter printWriter) {
        String str = null;
        printWriter.print("mWarning=");
        printWriter.println(this.mWarning);
        printWriter.print("mPlaySound=");
        printWriter.println(this.mPlaySound);
        printWriter.print("mInvalidCharger=");
        printWriter.println(this.mInvalidCharger);
        printWriter.print("mShowing=");
        printWriter.println(SHOWING_STRINGS[this.mShowing]);
        printWriter.print("mSaverConfirmation=");
        if (this.mSaverConfirmation != null) {
            str = "not null";
        }
        printWriter.println(str);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public boolean isInvalidChargerWarningShowing() {
        return this.mInvalidCharger;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showInvalidChargerWarning() {
        this.mInvalidCharger = true;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showLowBatteryWarning(boolean z) {
        Slog.i("PowerUI.Notification", "show low battery warning: level=" + this.mBatteryLevel + " [" + this.mBucket + "] playSound=" + z);
        this.mPlaySound = z;
        this.mWarning = true;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void update(int i, int i2, long j) {
        this.mBatteryLevel = i;
        if (i2 >= 0) {
            this.mBucketDroppedNegativeTimeMs = 0L;
        } else if (i2 < this.mBucket) {
            this.mBucketDroppedNegativeTimeMs = System.currentTimeMillis();
        }
        this.mBucket = i2;
        this.mScreenOffTime = j;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void updateLowBatteryWarning() {
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void userSwitched() {
        updateNotification();
    }
}
