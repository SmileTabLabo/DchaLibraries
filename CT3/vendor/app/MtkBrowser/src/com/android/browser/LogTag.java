package com.android.browser;

import android.util.EventLog;
/* loaded from: b.zip:com/android/browser/LogTag.class */
public class LogTag {
    public static void logBookmarkAdded(String str, String str2) {
        EventLog.writeEvent(70103, str + "|" + str2);
    }

    public static void logPageFinishedLoading(String str, long j) {
        EventLog.writeEvent(70104, str + "|" + j);
    }
}
