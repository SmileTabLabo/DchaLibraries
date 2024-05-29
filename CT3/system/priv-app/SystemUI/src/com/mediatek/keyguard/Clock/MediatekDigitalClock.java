package com.mediatek.keyguard.Clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R$id;
import com.mediatek.keyguard.PowerOffAlarm.Alarms;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.TimeZone;
/* loaded from: a.zip:com/mediatek/keyguard/Clock/MediatekDigitalClock.class */
public class MediatekDigitalClock extends LinearLayout {
    private AmPm mAmPm;
    private boolean mAttached;
    private Calendar mCalendar;
    private ContentObserver mFormatChangeObserver;
    private final Handler mHandler;
    private String mHoursFormat;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mLive;
    private TextView mTimeDisplayHours;
    private TextView mTimeDisplayMinutes;
    private String mTimeZoneId;

    /* renamed from: com.mediatek.keyguard.Clock.MediatekDigitalClock$1  reason: invalid class name */
    /* loaded from: a.zip:com/mediatek/keyguard/Clock/MediatekDigitalClock$1.class */
    class AnonymousClass1 extends BroadcastReceiver {
        final MediatekDigitalClock this$0;

        AnonymousClass1(MediatekDigitalClock mediatekDigitalClock) {
            this.this$0 = mediatekDigitalClock;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (this.this$0.mLive && intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                this.this$0.mCalendar = Calendar.getInstance();
            }
            this.this$0.mHandler.post(new Runnable(this) { // from class: com.mediatek.keyguard.Clock.MediatekDigitalClock.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.updateTime();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/mediatek/keyguard/Clock/MediatekDigitalClock$AmPm.class */
    public static class AmPm {
        private final TextView mAmPm;
        private final String mAmString;
        private final String mPmString;

        AmPm(View view) {
            this.mAmPm = (TextView) view.findViewById(R$id.am_pm);
            this.mAmPm.setPadding(0, 4, 0, 0);
            String[] amPmStrings = new DateFormatSymbols().getAmPmStrings();
            this.mAmString = amPmStrings[0];
            this.mPmString = amPmStrings[1];
        }

        CharSequence getAmPmText() {
            return this.mAmPm.getText();
        }

        void setIsMorning(boolean z) {
            this.mAmPm.setText(z ? this.mAmString : this.mPmString);
        }

        void setShowAmPm(boolean z) {
            this.mAmPm.setVisibility(z ? 0 : 8);
        }
    }

    /* loaded from: a.zip:com/mediatek/keyguard/Clock/MediatekDigitalClock$FormatChangeObserver.class */
    private class FormatChangeObserver extends ContentObserver {
        final MediatekDigitalClock this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public FormatChangeObserver(MediatekDigitalClock mediatekDigitalClock) {
            super(new Handler());
            this.this$0 = mediatekDigitalClock;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.setDateFormat();
            this.this$0.updateTime();
        }
    }

    public MediatekDigitalClock(Context context) {
        this(context, null);
    }

    public MediatekDigitalClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLive = true;
        this.mHandler = new Handler();
        this.mIntentReceiver = new AnonymousClass1(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDateFormat() {
        this.mHoursFormat = Alarms.get24HourMode(getContext()) ? "kk" : "h";
        this.mAmPm.setShowAmPm(!Alarms.get24HourMode(getContext()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTime() {
        if (this.mLive) {
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        }
        if (this.mTimeZoneId != null) {
            this.mCalendar.setTimeZone(TimeZone.getTimeZone(this.mTimeZoneId));
        }
        StringBuilder sb = new StringBuilder();
        CharSequence format = DateFormat.format(this.mHoursFormat, this.mCalendar);
        this.mTimeDisplayHours.setText(format);
        sb.append(format);
        CharSequence format2 = DateFormat.format(":mm", this.mCalendar);
        sb.append(format2);
        this.mTimeDisplayMinutes.setText(format2);
        this.mAmPm.setIsMorning(this.mCalendar.get(9) == 0);
        if (!Alarms.get24HourMode(getContext())) {
            sb.append(this.mAmPm.getAmPmText());
        }
        setContentDescription(sb);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.v("PowerOffAlarm", "onAttachedToWindow " + this);
        if (this.mAttached) {
            return;
        }
        this.mAttached = true;
        if (this.mLive) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.TIME_TICK");
            intentFilter.addAction("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            getContext().registerReceiver(this.mIntentReceiver, intentFilter);
        }
        this.mFormatChangeObserver = new FormatChangeObserver(this);
        getContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, this.mFormatChangeObserver);
        updateTime();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            this.mAttached = false;
            if (this.mLive) {
                getContext().unregisterReceiver(this.mIntentReceiver);
            }
            getContext().getContentResolver().unregisterContentObserver(this.mFormatChangeObserver);
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTimeDisplayHours = (TextView) findViewById(R$id.timeDisplayHours);
        this.mTimeDisplayMinutes = (TextView) findViewById(R$id.timeDisplayMinutes);
        this.mAmPm = new AmPm(this);
        this.mCalendar = Calendar.getInstance();
        setDateFormat();
    }
}
