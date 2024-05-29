package com.mediatek.keyguard.PowerOffAlarm;

import android.content.Context;
import android.text.format.DateFormat;
/* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/Alarms.class */
public class Alarms {
    public static boolean get24HourMode(Context context) {
        return DateFormat.is24HourFormat(context);
    }
}
