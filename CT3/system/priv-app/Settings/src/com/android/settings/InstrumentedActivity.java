package com.android.settings;

import android.app.Activity;
import com.android.internal.logging.MetricsLogger;
/* loaded from: classes.dex */
public abstract class InstrumentedActivity extends Activity {
    protected abstract int getMetricsCategory();

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        MetricsLogger.visible(this, getMetricsCategory());
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        MetricsLogger.hidden(this, getMetricsCategory());
    }
}
