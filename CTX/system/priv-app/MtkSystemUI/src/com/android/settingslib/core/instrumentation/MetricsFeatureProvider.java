package com.android.settingslib.core.instrumentation;

import android.content.Context;
import java.util.List;
/* loaded from: classes.dex */
public class MetricsFeatureProvider {
    private List<LogWriter> mLoggerWriters;

    public void visible(Context context, int i, int i2) {
        for (LogWriter logWriter : this.mLoggerWriters) {
            logWriter.visible(context, i, i2);
        }
    }

    public void hidden(Context context, int i) {
        for (LogWriter logWriter : this.mLoggerWriters) {
            logWriter.hidden(context, i);
        }
    }
}
