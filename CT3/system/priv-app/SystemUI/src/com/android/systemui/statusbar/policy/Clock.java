package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.android.systemui.DemoMode;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.icu.LocaleData;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/Clock.class */
public class Clock extends TextView implements DemoMode, TunerService.Tunable {
    private final int mAmPmStyle;
    private boolean mAttached;
    private Calendar mCalendar;
    private SimpleDateFormat mClockFormat;
    private String mClockFormatString;
    private SimpleDateFormat mContentDescriptionFormat;
    private boolean mDemoMode;
    private final BroadcastReceiver mIntentReceiver;
    private Locale mLocale;
    private ContentObserver mObs;
    private final BroadcastReceiver mScreenReceiver;
    private final Runnable mSecondTick;
    private Handler mSecondsHandler;
    private boolean mShowSeconds;

    public Clock(Context context) {
        this(context, null);
    }

    public Clock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Clock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIntentReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.Clock.1
            final Clock this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                    String stringExtra = intent.getStringExtra("time-zone");
                    this.this$0.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(stringExtra));
                    if (this.this$0.mClockFormat != null) {
                        this.this$0.mClockFormat.setTimeZone(this.this$0.mCalendar.getTimeZone());
                    }
                    Log.d("Clock", "onReceive : ACTION_TIMEZONE_CHANGED : " + this.this$0.mCalendar);
                    Log.d("Clock", "TimeZone =" + TimeZone.getTimeZone(stringExtra));
                } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    Locale locale = this.this$0.getResources().getConfiguration().locale;
                    if (!locale.equals(this.this$0.mLocale)) {
                        this.this$0.mLocale = locale;
                        this.this$0.mClockFormatString = "";
                    }
                }
                this.this$0.updateClock();
            }
        };
        this.mScreenReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.Clock.2
            final Clock this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (this.this$0.mSecondsHandler != null) {
                        this.this$0.mSecondsHandler.removeCallbacks(this.this$0.mSecondTick);
                    }
                } else if (!"android.intent.action.SCREEN_ON".equals(action) || this.this$0.mSecondsHandler == null) {
                } else {
                    this.this$0.mSecondsHandler.postAtTime(this.this$0.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
                }
            }
        };
        this.mSecondTick = new Runnable(this) { // from class: com.android.systemui.statusbar.policy.Clock.3
            final Clock this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mCalendar != null) {
                    this.this$0.updateClock();
                }
                this.this$0.mSecondsHandler.postAtTime(this, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
            }
        };
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.Clock, 0, 0);
        try {
            this.mAmPmStyle = obtainStyledAttributes.getInt(0, 2);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    private final CharSequence getSmallTime() {
        SimpleDateFormat simpleDateFormat;
        int i;
        Context context = getContext();
        boolean is24HourFormat = DateFormat.is24HourFormat(context, ActivityManager.getCurrentUser());
        LocaleData localeData = LocaleData.get(context.getResources().getConfiguration().locale);
        String str = this.mShowSeconds ? is24HourFormat ? localeData.timeFormat_Hms : localeData.timeFormat_hms : is24HourFormat ? localeData.timeFormat_Hm : localeData.timeFormat_hm;
        if (BenesseExtension.getDchaState() != 0) {
            str = "M月d日aaKK:mm";
        }
        if (str.equals(this.mClockFormatString)) {
            simpleDateFormat = this.mClockFormat;
        } else {
            this.mContentDescriptionFormat = new SimpleDateFormat(str);
            String str2 = str;
            if (this.mAmPmStyle != 0) {
                boolean z = false;
                int i2 = 0;
                while (true) {
                    i = -1;
                    if (i2 >= str.length()) {
                        break;
                    }
                    char charAt = str.charAt(i2);
                    boolean z2 = z;
                    if (charAt == '\'') {
                        z2 = !z;
                    }
                    if (!z2 && charAt == 'a') {
                        i = i2;
                        break;
                    }
                    i2++;
                    z = z2;
                }
                str2 = str;
                if (i >= 0) {
                    int i3 = i;
                    while (i3 > 0 && Character.isWhitespace(str.charAt(i3 - 1))) {
                        i3--;
                    }
                    str2 = str.substring(0, i3) + (char) 61184 + str.substring(i3, i) + "a\uef01" + str.substring(i + 1);
                }
            }
            simpleDateFormat = new SimpleDateFormat(str2);
            this.mClockFormat = simpleDateFormat;
            this.mClockFormatString = str2;
        }
        String format = simpleDateFormat.format(this.mCalendar.getTime());
        if (this.mAmPmStyle != 0) {
            int indexOf = format.indexOf(61184);
            int indexOf2 = format.indexOf(61185);
            if (indexOf >= 0 && indexOf2 > indexOf) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(format);
                if (this.mAmPmStyle == 2) {
                    spannableStringBuilder.delete(indexOf, indexOf2 + 1);
                } else {
                    if (this.mAmPmStyle == 1) {
                        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.7f), indexOf, indexOf2, 34);
                    }
                    spannableStringBuilder.delete(indexOf2, indexOf2 + 1);
                    spannableStringBuilder.delete(indexOf, indexOf + 1);
                }
                return spannableStringBuilder;
            }
        }
        return format;
    }

    private void updateShowSeconds() {
        if (!this.mShowSeconds) {
            if (this.mSecondsHandler != null) {
                this.mContext.unregisterReceiver(this.mScreenReceiver);
                this.mSecondsHandler.removeCallbacks(this.mSecondTick);
                this.mSecondsHandler = null;
                updateClock();
            }
        } else if (this.mSecondsHandler != null || getDisplay() == null) {
        } else {
            this.mSecondsHandler = new Handler();
            if (getDisplay().getState() == 2) {
                this.mSecondsHandler.postAtTime(this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
            }
            IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.mContext.registerReceiver(this.mScreenReceiver, intentFilter);
        }
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            updateClock();
        } else if (this.mDemoMode && str.equals("clock")) {
            String string = bundle.getString("millis");
            String string2 = bundle.getString("hhmm");
            if (string != null) {
                this.mCalendar.setTimeInMillis(Long.parseLong(string));
            } else if (string2 != null && string2.length() == 4) {
                int parseInt = Integer.parseInt(string2.substring(0, 2));
                int parseInt2 = Integer.parseInt(string2.substring(2));
                if (DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser())) {
                    this.mCalendar.set(11, parseInt);
                } else {
                    this.mCalendar.set(10, parseInt);
                }
                this.mCalendar.set(12, parseInt2);
            }
            setText(getSmallTime());
            setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
        }
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.TIME_TICK");
            intentFilter.addAction("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            getContext().registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, getHandler());
            TunerService.get(getContext()).addTunable(this, "clock_seconds", "icon_blacklist");
            this.mObs = new ContentObserver(this, getHandler()) { // from class: com.android.systemui.statusbar.policy.Clock.4
                final Clock this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.database.ContentObserver
                public void onChange(boolean z) {
                    this.this$0.updateClock();
                }
            };
            getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor("dcha_state"), false, this.mObs, -1);
        }
        this.mCalendar = Calendar.getInstance(TimeZone.getDefault());
        updateClock();
        updateShowSeconds();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            getContext().getContentResolver().unregisterContentObserver(this.mObs);
            this.mObs = null;
            this.mAttached = false;
            TunerService.get(getContext()).removeTunable(this);
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        int i = 0;
        if (!"clock_seconds".equals(str)) {
            if ("icon_blacklist".equals(str)) {
                if (StatusBarIconController.getIconBlacklist(str2).contains("clock")) {
                    i = 8;
                }
                setVisibility(i);
                return;
            }
            return;
        }
        boolean z = false;
        if (str2 != null) {
            z = false;
            if (Integer.parseInt(str2) != 0) {
                z = true;
            }
        }
        this.mShowSeconds = z;
        updateShowSeconds();
    }

    final void updateClock() {
        if (this.mDemoMode) {
            return;
        }
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        setText(getSmallTime());
        setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
    }
}
