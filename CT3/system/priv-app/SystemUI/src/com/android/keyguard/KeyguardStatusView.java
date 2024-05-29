package com.android.keyguard;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.widget.GridLayout;
import android.widget.TextClock;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import java.util.Locale;
/* loaded from: a.zip:com/android/keyguard/KeyguardStatusView.class */
public class KeyguardStatusView extends GridLayout {
    private final AlarmManager mAlarmManager;
    TextView mAlarmStatusView;
    TextClock mClockView;
    TextClock mDateView;
    KeyguardUpdateMonitorCallback mInfoCallback;
    private final LockPatternUtils mLockPatternUtils;
    TextView mOwnerInfo;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/keyguard/KeyguardStatusView$Patterns.class */
    public static final class Patterns {
        static String cacheKey;
        static String clockView12;
        static String clockView24;
        static String dateView;

        private Patterns() {
        }

        static void update(Context context, boolean z) {
            Locale locale = Locale.getDefault();
            Resources resources = context.getResources();
            String string = resources.getString(z ? R$string.abbrev_wday_month_day_no_year_alarm : R$string.abbrev_wday_month_day_no_year);
            String string2 = resources.getString(R$string.clock_12hr_format);
            String string3 = resources.getString(R$string.clock_24hr_format);
            String str = locale.toString() + string + string2 + string3;
            if (str.equals(cacheKey)) {
                return;
            }
            dateView = DateFormat.getBestDateTimePattern(locale, string);
            clockView12 = DateFormat.getBestDateTimePattern(locale, string2);
            if (!string2.contains("a")) {
                clockView12 = clockView12.replaceAll("a", "").trim();
            }
            clockView24 = DateFormat.getBestDateTimePattern(locale, string3);
            clockView24 = clockView24.replace(':', (char) 60929);
            clockView12 = clockView12.replace(':', (char) 60929);
            cacheKey = str;
        }
    }

    public KeyguardStatusView(Context context) {
        this(context, null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.KeyguardStatusView.1
            final KeyguardStatusView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i2) {
                this.this$0.setEnableMarquee(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                if (z) {
                    Slog.v("KeyguardStatusView", "refresh statusview showing:" + z);
                    this.this$0.refresh();
                    this.this$0.updateOwnerInfo();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                this.this$0.setEnableMarquee(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeChanged() {
                this.this$0.refresh();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i2) {
                this.this$0.refresh();
                this.this$0.updateOwnerInfo();
            }
        };
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mLockPatternUtils = new LockPatternUtils(getContext());
    }

    public static String formatNextAlarm(Context context, AlarmManager.AlarmClockInfo alarmClockInfo) {
        if (alarmClockInfo == null) {
            return "";
        }
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, ActivityManager.getCurrentUser()) ? "EHm" : "Ehma"), alarmClockInfo.getTriggerTime()).toString();
    }

    private String getOwnerInfo() {
        String str = null;
        if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
            str = this.mLockPatternUtils.getDeviceOwnerInfo();
        } else if (this.mLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
            str = this.mLockPatternUtils.getOwnerInfo(KeyguardUpdateMonitor.getCurrentUser());
        }
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refresh() {
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(-2);
        Patterns.update(this.mContext, nextAlarmClock != null);
        refreshTime();
        refreshAlarmStatus(nextAlarmClock);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setEnableMarquee(boolean z) {
        Log.v("KeyguardStatusView", (z ? "Enable" : "Disable") + " transport text marquee");
        if (this.mAlarmStatusView != null) {
            this.mAlarmStatusView.setSelected(z);
        }
        if (this.mOwnerInfo != null) {
            this.mOwnerInfo.setSelected(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateOwnerInfo() {
        if (this.mOwnerInfo == null) {
            return;
        }
        String ownerInfo = getOwnerInfo();
        if (TextUtils.isEmpty(ownerInfo)) {
            this.mOwnerInfo.setVisibility(8);
            return;
        }
        this.mOwnerInfo.setVisibility(0);
        this.mOwnerInfo.setText(ownerInfo);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mClockView.setTextSize(0, getResources().getDimensionPixelSize(R$dimen.widget_big_font_size));
        this.mDateView.setTextSize(0, getResources().getDimensionPixelSize(R$dimen.widget_label_font_size));
        if (this.mOwnerInfo != null) {
            this.mOwnerInfo.setTextSize(0, getResources().getDimensionPixelSize(R$dimen.widget_label_font_size));
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAlarmStatusView = (TextView) findViewById(R$id.alarm_status);
        this.mDateView = (TextClock) findViewById(R$id.date_view);
        this.mClockView = (TextClock) findViewById(R$id.clock_view);
        this.mDateView.setShowCurrentUserTime(true);
        this.mClockView.setShowCurrentUserTime(true);
        this.mOwnerInfo = (TextView) findViewById(R$id.owner_info);
        setEnableMarquee(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
        refresh();
        updateOwnerInfo();
        this.mClockView.setElegantTextHeight(false);
    }

    void refreshAlarmStatus(AlarmManager.AlarmClockInfo alarmClockInfo) {
        if (alarmClockInfo == null) {
            this.mAlarmStatusView.setVisibility(8);
            return;
        }
        String formatNextAlarm = formatNextAlarm(this.mContext, alarmClockInfo);
        this.mAlarmStatusView.setText(formatNextAlarm);
        this.mAlarmStatusView.setContentDescription(getResources().getString(R$string.keyguard_accessibility_next_alarm, formatNextAlarm));
        this.mAlarmStatusView.setVisibility(0);
    }

    public void refreshTime() {
        this.mDateView.setFormat24Hour(Patterns.dateView);
        this.mDateView.setFormat12Hour(Patterns.dateView);
        this.mClockView.setFormat12Hour(Patterns.clockView12);
        this.mClockView.setFormat24Hour(Patterns.clockView24);
    }
}
