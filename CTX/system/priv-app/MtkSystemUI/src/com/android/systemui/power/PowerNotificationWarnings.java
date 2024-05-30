package com.android.systemui.power;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserHandle;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import com.android.settingslib.Utils;
import com.android.settingslib.fuelgauge.BatterySaverUtils;
import com.android.settingslib.utils.PowerUtil;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.power.PowerUI;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.util.NotificationChannels;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
/* loaded from: classes.dex */
public class PowerNotificationWarnings implements PowerUI.WarningsUI {
    private int mBatteryLevel;
    private int mBucket;
    private final Context mContext;
    private Estimate mEstimate;
    private SystemUIDialog mHighTempDialog;
    private boolean mHighTempWarning;
    private boolean mInvalidCharger;
    private long mLowWarningThreshold;
    private final NotificationManager mNoMan;
    private boolean mPlaySound;
    private final PowerManager mPowerMan;
    private SystemUIDialog mSaverConfirmation;
    private SystemUIDialog mSaverEnabledConfirmation;
    private long mScreenOffTime;
    private long mSevereWarningThreshold;
    private boolean mShowAutoSaverSuggestion;
    private int mShowing;
    private SystemUIDialog mThermalShutdownDialog;
    private boolean mWarning;
    private long mWarningTriggerTimeMs;
    private static final boolean DEBUG = PowerUI.DEBUG;
    private static final String[] SHOWING_STRINGS = {"SHOWING_NOTHING", "SHOWING_WARNING", "SHOWING_SAVER", "SHOWING_INVALID_CHARGER", "SHOWING_AUTO_SAVER_SUGGESTION"};
    private static final AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Receiver mReceiver = new Receiver();
    private final Intent mOpenBatterySettings = settings("android.intent.action.POWER_USAGE_SUMMARY");

    public PowerNotificationWarnings(Context context) {
        this.mContext = context;
        this.mNoMan = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        this.mPowerMan = (PowerManager) context.getSystemService("power");
        this.mReceiver.init();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dump(PrintWriter printWriter) {
        printWriter.print("mWarning=");
        printWriter.println(this.mWarning);
        printWriter.print("mPlaySound=");
        printWriter.println(this.mPlaySound);
        printWriter.print("mInvalidCharger=");
        printWriter.println(this.mInvalidCharger);
        printWriter.print("mShowing=");
        printWriter.println(SHOWING_STRINGS[this.mShowing]);
        printWriter.print("mSaverConfirmation=");
        printWriter.println(this.mSaverConfirmation != null ? "not null" : null);
        printWriter.print("mSaverEnabledConfirmation=");
        printWriter.println(this.mSaverEnabledConfirmation != null ? "not null" : null);
        printWriter.print("mHighTempWarning=");
        printWriter.println(this.mHighTempWarning);
        printWriter.print("mHighTempDialog=");
        printWriter.println(this.mHighTempDialog != null ? "not null" : null);
        printWriter.print("mThermalShutdownDialog=");
        printWriter.println(this.mThermalShutdownDialog != null ? "not null" : null);
    }

    private int getLowBatteryAutoTriggerDefaultLevel() {
        return this.mContext.getResources().getInteger(17694803);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void update(int i, int i2, long j) {
        this.mBatteryLevel = i;
        if (i2 >= 0) {
            this.mWarningTriggerTimeMs = 0L;
        } else if (i2 < this.mBucket) {
            this.mWarningTriggerTimeMs = System.currentTimeMillis();
        }
        this.mBucket = i2;
        this.mScreenOffTime = j;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void updateEstimate(Estimate estimate) {
        this.mEstimate = estimate;
        if (estimate.estimateMillis <= this.mLowWarningThreshold) {
            this.mWarningTriggerTimeMs = System.currentTimeMillis();
        }
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void updateThresholds(long j, long j2) {
        this.mLowWarningThreshold = j;
        this.mSevereWarningThreshold = j2;
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
        } else if (this.mShowAutoSaverSuggestion) {
            if (this.mShowing != 4) {
                showAutoSaverSuggestionNotification();
            }
            this.mShowing = 4;
        } else {
            this.mNoMan.cancelAsUser("low_battery", 2, UserHandle.ALL);
            this.mNoMan.cancelAsUser("low_battery", 3, UserHandle.ALL);
            this.mNoMan.cancelAsUser("auto_saver", 49, UserHandle.ALL);
            this.mShowing = 0;
        }
    }

    private void showInvalidChargerNotification() {
        Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setSmallIcon(R.drawable.ic_power_low).setWhen(0L).setShowWhen(false).setOngoing(true).setContentTitle(this.mContext.getString(R.string.invalid_charger_title)).setContentText(this.mContext.getString(R.string.invalid_charger_text)).setColor(this.mContext.getColor(17170774));
        SystemUI.overrideNotificationAppName(this.mContext, color, false);
        Notification build = color.build();
        this.mNoMan.cancelAsUser("low_battery", 3, UserHandle.ALL);
        this.mNoMan.notifyAsUser("low_battery", 2, build, UserHandle.ALL);
    }

    protected void showWarningNotification() {
        String format = NumberFormat.getPercentInstance().format(this.mBatteryLevel / 100.0d);
        String string = this.mContext.getString(R.string.battery_low_title);
        String string2 = this.mContext.getString(R.string.battery_low_percent_format, format);
        if (this.mEstimate != null) {
            string2 = getHybridContentString(format);
        }
        Notification.Builder visibility = new Notification.Builder(this.mContext, NotificationChannels.BATTERY).setSmallIcon(R.drawable.ic_power_low).setWhen(this.mWarningTriggerTimeMs).setShowWhen(false).setContentText(string2).setContentTitle(string).setOnlyAlertOnce(true).setDeleteIntent(pendingBroadcast("PNW.dismissedWarning")).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1);
        if (BenesseExtension.getDchaState() == 0 && hasBatterySettings()) {
            visibility.setContentIntent(pendingBroadcast("PNW.batterySettings"));
        }
        if (this.mEstimate == null || this.mBucket < 0 || this.mEstimate.estimateMillis < this.mSevereWarningThreshold) {
            visibility.setColor(Utils.getColorAttr(this.mContext, 16844099));
        }
        if (BenesseExtension.getDchaState() == 0) {
            visibility.addAction(0, this.mContext.getString(R.string.battery_saver_start_action), pendingBroadcast("PNW.startSaver"));
        }
        visibility.setOnlyAlertOnce(!this.mPlaySound);
        this.mPlaySound = false;
        SystemUI.overrideNotificationAppName(this.mContext, visibility, false);
        Notification build = visibility.build();
        this.mNoMan.cancelAsUser("low_battery", 2, UserHandle.ALL);
        this.mNoMan.notifyAsUser("low_battery", 3, build, UserHandle.ALL);
    }

    private void showAutoSaverSuggestionNotification() {
        Notification.Builder contentText = new Notification.Builder(this.mContext, NotificationChannels.HINTS).setSmallIcon(R.drawable.ic_power_saver).setWhen(0L).setShowWhen(false).setContentTitle(this.mContext.getString(R.string.auto_saver_title)).setContentText(this.mContext.getString(R.string.auto_saver_text, Integer.valueOf(getLowBatteryAutoTriggerDefaultLevel())));
        contentText.setContentIntent(pendingBroadcast("PNW.enableAutoSaver"));
        contentText.setDeleteIntent(pendingBroadcast("PNW.dismissAutoSaverSuggestion"));
        contentText.addAction(0, this.mContext.getString(R.string.no_auto_saver_action), pendingBroadcast("PNW.autoSaverNoThanks"));
        SystemUI.overrideNotificationAppName(this.mContext, contentText, false);
        this.mNoMan.notifyAsUser("auto_saver", 49, contentText.build(), UserHandle.ALL);
    }

    private String getHybridContentString(String str) {
        return PowerUtil.getBatteryRemainingStringFormatted(this.mContext, this.mEstimate.estimateMillis, str, this.mEstimate.isBasedOnUsage);
    }

    private PendingIntent pendingBroadcast(String str) {
        return PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(str).setPackage(this.mContext.getPackageName()).setFlags(268435456), 0, UserHandle.CURRENT);
    }

    private static Intent settings(String str) {
        return new Intent(str).setFlags(1551892480);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public boolean isInvalidChargerWarningShowing() {
        return this.mInvalidCharger;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissHighTemperatureWarning() {
        if (!this.mHighTempWarning) {
            return;
        }
        this.mHighTempWarning = false;
        dismissHighTemperatureWarningInternal();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissHighTemperatureWarningInternal() {
        this.mNoMan.cancelAsUser("high_temp", 4, UserHandle.ALL);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showHighTemperatureWarning() {
        if (this.mHighTempWarning) {
            return;
        }
        this.mHighTempWarning = true;
        Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setSmallIcon(R.drawable.ic_device_thermostat_24).setWhen(0L).setShowWhen(false).setContentTitle(this.mContext.getString(R.string.high_temp_title)).setContentText(this.mContext.getString(R.string.high_temp_notif_message)).setVisibility(1).setContentIntent(pendingBroadcast("PNW.clickedTempWarning")).setDeleteIntent(pendingBroadcast("PNW.dismissedTempWarning")).setColor(Utils.getColorAttr(this.mContext, 16844099));
        SystemUI.overrideNotificationAppName(this.mContext, color, false);
        this.mNoMan.notifyAsUser("high_temp", 4, color.build(), UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showHighTemperatureDialog() {
        if (this.mHighTempDialog != null) {
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setIconAttribute(16843605);
        systemUIDialog.setTitle(R.string.high_temp_title);
        systemUIDialog.setMessage(R.string.high_temp_dialog_message);
        systemUIDialog.setPositiveButton(17039370, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.power.-$$Lambda$PowerNotificationWarnings$PU_JpsxNcz7jXGNa_DRkuMbEWwU
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                PowerNotificationWarnings.this.mHighTempDialog = null;
            }
        });
        systemUIDialog.show();
        this.mHighTempDialog = systemUIDialog;
    }

    void dismissThermalShutdownWarning() {
        this.mNoMan.cancelAsUser("high_temp", 39, UserHandle.ALL);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showThermalShutdownDialog() {
        if (this.mThermalShutdownDialog != null) {
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setIconAttribute(16843605);
        systemUIDialog.setTitle(R.string.thermal_shutdown_title);
        systemUIDialog.setMessage(R.string.thermal_shutdown_dialog_message);
        systemUIDialog.setPositiveButton(17039370, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.power.-$$Lambda$PowerNotificationWarnings$O5nkGS5PG2ihQrXqunpOO_aZDms
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                PowerNotificationWarnings.this.mThermalShutdownDialog = null;
            }
        });
        systemUIDialog.show();
        this.mThermalShutdownDialog = systemUIDialog;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showThermalShutdownWarning() {
        Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setSmallIcon(R.drawable.ic_device_thermostat_24).setWhen(0L).setShowWhen(false).setContentTitle(this.mContext.getString(R.string.thermal_shutdown_title)).setContentText(this.mContext.getString(R.string.thermal_shutdown_message)).setVisibility(1).setContentIntent(pendingBroadcast("PNW.clickedThermalShutdownWarning")).setDeleteIntent(pendingBroadcast("PNW.dismissedThermalShutdownWarning")).setColor(Utils.getColorAttr(this.mContext, 16844099));
        SystemUI.overrideNotificationAppName(this.mContext, color, false);
        this.mNoMan.notifyAsUser("high_temp", 39, color.build(), UserHandle.ALL);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void updateLowBatteryWarning() {
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissLowBatteryWarning() {
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "dismissing low battery warning: level=" + this.mBatteryLevel);
        }
        dismissLowBatteryNotification();
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
        return this.mOpenBatterySettings.resolveActivity(this.mContext.getPackageManager()) != null;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showLowBatteryWarning(boolean z) {
        Slog.i("PowerUI.Notification", "show low battery warning: level=" + this.mBatteryLevel + " [" + this.mBucket + "] playSound=" + z);
        this.mPlaySound = z;
        this.mWarning = true;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissInvalidChargerWarning() {
        dismissInvalidChargerNotification();
    }

    private void dismissInvalidChargerNotification() {
        if (this.mInvalidCharger) {
            Slog.i("PowerUI.Notification", "dismissing invalid charger notification");
        }
        this.mInvalidCharger = false;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showInvalidChargerWarning() {
        this.mInvalidCharger = true;
        updateNotification();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAutoSaverSuggestion() {
        this.mShowAutoSaverSuggestion = true;
        updateNotification();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissAutoSaverSuggestion() {
        this.mShowAutoSaverSuggestion = false;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void userSwitched() {
        updateNotification();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showStartSaverConfirmation() {
        if (this.mSaverConfirmation != null) {
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(R.string.battery_saver_confirmation_title);
        systemUIDialog.setMessage(getBatterySaverDescription());
        if (isEnglishLocale()) {
            systemUIDialog.setMessageHyphenationFrequency(0);
        }
        systemUIDialog.setMessageMovementMethod(LinkMovementMethod.getInstance());
        systemUIDialog.setNegativeButton(17039360, null);
        systemUIDialog.setPositiveButton(R.string.battery_saver_confirmation_ok, new DialogInterface.OnClickListener() { // from class: com.android.systemui.power.-$$Lambda$PowerNotificationWarnings$PQ6TbFMdXvgpK7h9WuYVBt-fwlE
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                PowerNotificationWarnings.this.setSaverMode(true, false);
            }
        });
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.power.-$$Lambda$PowerNotificationWarnings$xerpSCZ61JcOfwY_Falk7PImt6k
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                PowerNotificationWarnings.this.mSaverConfirmation = null;
            }
        });
        systemUIDialog.show();
        this.mSaverConfirmation = systemUIDialog;
    }

    private boolean isEnglishLocale() {
        return Objects.equals(Locale.getDefault().getLanguage(), Locale.ENGLISH.getLanguage());
    }

    private CharSequence getBatterySaverDescription() {
        Annotation[] annotationArr;
        String charSequence = this.mContext.getText(R.string.help_uri_battery_saver_learn_more_link_target).toString();
        if (TextUtils.isEmpty(charSequence)) {
            return this.mContext.getText(17039578);
        }
        SpannableString spannableString = new SpannableString(this.mContext.getText(17039579));
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(spannableString);
        for (Annotation annotation : (Annotation[]) spannableString.getSpans(0, spannableString.length(), Annotation.class)) {
            if ("url".equals(annotation.getValue())) {
                int spanStart = spannableString.getSpanStart(annotation);
                int spanEnd = spannableString.getSpanEnd(annotation);
                URLSpan uRLSpan = new URLSpan(charSequence) { // from class: com.android.systemui.power.PowerNotificationWarnings.1
                    @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
                    public void updateDrawState(TextPaint textPaint) {
                        super.updateDrawState(textPaint);
                        textPaint.setUnderlineText(false);
                    }

                    @Override // android.text.style.URLSpan, android.text.style.ClickableSpan
                    public void onClick(View view) {
                        if (PowerNotificationWarnings.this.mSaverConfirmation != null) {
                            PowerNotificationWarnings.this.mSaverConfirmation.dismiss();
                        }
                        PowerNotificationWarnings.this.mContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS").setFlags(268435456));
                        if (BenesseExtension.getDchaState() != 0) {
                            return;
                        }
                        Uri parse = Uri.parse(getURL());
                        Context context = view.getContext();
                        Intent flags = new Intent("android.intent.action.VIEW", parse).setFlags(268435456);
                        try {
                            context.startActivity(flags);
                        } catch (ActivityNotFoundException e) {
                            Log.w("PowerUI.Notification", "Activity was not found for intent, " + flags.toString());
                        }
                    }
                };
                spannableStringBuilder.setSpan(uRLSpan, spanStart, spanEnd, spannableString.getSpanFlags(uRLSpan));
            }
        }
        return spannableStringBuilder;
    }

    private void showAutoSaverEnabledConfirmation() {
        if (BenesseExtension.getDchaState() == 0 && this.mSaverEnabledConfirmation == null) {
            final Intent flags = new Intent("android.settings.BATTERY_SAVER_SETTINGS").setFlags(268435456);
            SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
            systemUIDialog.setTitle(R.string.auto_saver_enabled_title);
            systemUIDialog.setMessage(this.mContext.getString(R.string.auto_saver_enabled_text, Integer.valueOf(getLowBatteryAutoTriggerDefaultLevel())));
            systemUIDialog.setPositiveButton(R.string.auto_saver_okay_action, new DialogInterface.OnClickListener() { // from class: com.android.systemui.power.-$$Lambda$PowerNotificationWarnings$vDtwYNuUfBD9Wjct46I3oQZA9IU
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PowerNotificationWarnings.this.onAutoSaverEnabledConfirmationClosed();
                }
            });
            systemUIDialog.setNeutralButton(R.string.open_saver_setting_action, new DialogInterface.OnClickListener() { // from class: com.android.systemui.power.-$$Lambda$PowerNotificationWarnings$AbQVXaj-x5Lsbd2FymR2u02w1z0
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PowerNotificationWarnings.lambda$showAutoSaverEnabledConfirmation$5(PowerNotificationWarnings.this, flags, dialogInterface, i);
                }
            });
            systemUIDialog.setShowForAllUsers(true);
            systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.power.-$$Lambda$PowerNotificationWarnings$tYg3WGmOxz_w0_hCJQKvQn95Q7c
                @Override // android.content.DialogInterface.OnDismissListener
                public final void onDismiss(DialogInterface dialogInterface) {
                    PowerNotificationWarnings.this.onAutoSaverEnabledConfirmationClosed();
                }
            });
            systemUIDialog.show();
            this.mSaverEnabledConfirmation = systemUIDialog;
        }
    }

    public static /* synthetic */ void lambda$showAutoSaverEnabledConfirmation$5(PowerNotificationWarnings powerNotificationWarnings, Intent intent, DialogInterface dialogInterface, int i) {
        powerNotificationWarnings.mContext.startActivity(intent);
        powerNotificationWarnings.onAutoSaverEnabledConfirmationClosed();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAutoSaverEnabledConfirmationClosed() {
        this.mSaverEnabledConfirmation = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSaverMode(boolean z, boolean z2) {
        BatterySaverUtils.setPowerSaveMode(this.mContext, z, z2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleAutoBatterySaver() {
        int integer = this.mContext.getResources().getInteger(17694805);
        if (integer == 0) {
            integer = 15;
        }
        BatterySaverUtils.ensureAutoBatterySaver(this.mContext, integer);
        showAutoSaverEnabledConfirmation();
    }

    /* loaded from: classes.dex */
    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("PNW.batterySettings");
            intentFilter.addAction("PNW.startSaver");
            intentFilter.addAction("PNW.dismissedWarning");
            intentFilter.addAction("PNW.clickedTempWarning");
            intentFilter.addAction("PNW.dismissedTempWarning");
            intentFilter.addAction("PNW.clickedThermalShutdownWarning");
            intentFilter.addAction("PNW.dismissedThermalShutdownWarning");
            intentFilter.addAction("PNW.startSaverConfirmation");
            intentFilter.addAction("PNW.autoSaverSuggestion");
            intentFilter.addAction("PNW.enableAutoSaver");
            intentFilter.addAction("PNW.autoSaverNoThanks");
            intentFilter.addAction("PNW.dismissAutoSaverSuggestion");
            PowerNotificationWarnings.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, "android.permission.DEVICE_POWER", PowerNotificationWarnings.this.mHandler);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.i("PowerUI.Notification", "Received " + action);
            if (action.equals("PNW.batterySettings")) {
                if (BenesseExtension.getDchaState() == 0) {
                    PowerNotificationWarnings.this.dismissLowBatteryNotification();
                    PowerNotificationWarnings.this.mContext.startActivityAsUser(PowerNotificationWarnings.this.mOpenBatterySettings, UserHandle.CURRENT);
                }
            } else if (action.equals("PNW.startSaver")) {
                PowerNotificationWarnings.this.setSaverMode(true, true);
                PowerNotificationWarnings.this.dismissLowBatteryNotification();
            } else if (action.equals("PNW.startSaverConfirmation")) {
                PowerNotificationWarnings.this.dismissLowBatteryNotification();
                PowerNotificationWarnings.this.showStartSaverConfirmation();
            } else if (action.equals("PNW.dismissedWarning")) {
                PowerNotificationWarnings.this.dismissLowBatteryWarning();
            } else if ("PNW.clickedTempWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissHighTemperatureWarningInternal();
                PowerNotificationWarnings.this.showHighTemperatureDialog();
            } else if ("PNW.dismissedTempWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissHighTemperatureWarningInternal();
            } else if ("PNW.clickedThermalShutdownWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissThermalShutdownWarning();
                PowerNotificationWarnings.this.showThermalShutdownDialog();
            } else if ("PNW.dismissedThermalShutdownWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissThermalShutdownWarning();
            } else if ("PNW.autoSaverSuggestion".equals(action)) {
                PowerNotificationWarnings.this.showAutoSaverSuggestion();
            } else if ("PNW.dismissAutoSaverSuggestion".equals(action)) {
                PowerNotificationWarnings.this.dismissAutoSaverSuggestion();
            } else if ("PNW.enableAutoSaver".equals(action)) {
                PowerNotificationWarnings.this.dismissAutoSaverSuggestion();
                PowerNotificationWarnings.this.scheduleAutoBatterySaver();
            } else if ("PNW.autoSaverNoThanks".equals(action)) {
                PowerNotificationWarnings.this.dismissAutoSaverSuggestion();
                BatterySaverUtils.suppressAutoBatterySaver(context);
            }
        }
    }
}
