package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextClock;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/SplitClockView.class */
public class SplitClockView extends LinearLayout {
    private TextClock mAmPmView;
    private BroadcastReceiver mIntentReceiver;
    private TextClock mTimeView;

    public SplitClockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIntentReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.SplitClockView.1
            final SplitClockView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.LOCALE_CHANGED".equals(action) || "android.intent.action.CONFIGURATION_CHANGED".equals(action) || "android.intent.action.USER_SWITCHED".equals(action)) {
                    this.this$0.updatePatterns();
                }
            }
        };
    }

    private static int getAmPmPartEndIndex(String str) {
        boolean z = false;
        int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            char charAt = str.charAt(i);
            boolean z2 = charAt == 'a';
            boolean isWhitespace = Character.isWhitespace(charAt);
            if (z2) {
                z = true;
            }
            if (!z2 && !isWhitespace) {
                if (i == length - 1) {
                    return -1;
                }
                return z ? i + 1 : -1;
            }
        }
        return z ? 0 : -1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePatterns() {
        String substring;
        String substring2;
        String timeFormatString = DateFormat.getTimeFormatString(getContext(), ActivityManager.getCurrentUser());
        int amPmPartEndIndex = getAmPmPartEndIndex(timeFormatString);
        if (amPmPartEndIndex == -1) {
            substring = timeFormatString;
            substring2 = "";
        } else {
            substring = timeFormatString.substring(0, amPmPartEndIndex);
            substring2 = timeFormatString.substring(amPmPartEndIndex);
        }
        this.mTimeView.setFormat12Hour(substring);
        this.mTimeView.setFormat24Hour(substring);
        this.mTimeView.setContentDescriptionFormat12Hour(timeFormatString);
        this.mTimeView.setContentDescriptionFormat24Hour(timeFormatString);
        this.mAmPmView.setFormat12Hour(substring2);
        this.mAmPmView.setFormat24Hour(substring2);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        getContext().registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, null);
        updatePatterns();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(this.mIntentReceiver);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTimeView = (TextClock) findViewById(2131886671);
        this.mAmPmView = (TextClock) findViewById(2131886672);
        this.mTimeView.setShowCurrentUserTime(true);
        this.mAmPmView.setShowCurrentUserTime(true);
    }
}
