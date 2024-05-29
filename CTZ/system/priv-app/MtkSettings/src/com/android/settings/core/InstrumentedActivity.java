package com.android.settings.core;

import android.os.Bundle;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;
import com.android.settingslib.core.lifecycle.ObservableActivity;
/* loaded from: classes.dex */
public abstract class InstrumentedActivity extends ObservableActivity implements Instrumentable {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.core.lifecycle.ObservableActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getLifecycle().addObserver(new VisibilityLoggerMixin(getMetricsCategory(), FeatureFactory.getFactory(this).getMetricsFeatureProvider()));
    }
}
