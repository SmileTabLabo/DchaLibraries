package com.android.settings.development.qstile;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DevelopmentTileConfigFragment extends DashboardFragment {
    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "DevelopmentTileConfig";
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.development_tile_settings;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new DevelopmentTilePreferenceController(context));
        return arrayList;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 1224;
    }
}
