package com.android.settings.fuelgauge;

import android.content.Context;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.utils.AsyncLoader;
/* loaded from: classes.dex */
public class BatteryInfoLoader extends AsyncLoader<BatteryInfo> {
    @VisibleForTesting
    BatteryUtils batteryUtils;
    BatteryStatsHelper mStatsHelper;

    public BatteryInfoLoader(Context context, BatteryStatsHelper batteryStatsHelper) {
        super(context);
        this.mStatsHelper = batteryStatsHelper;
        this.batteryUtils = BatteryUtils.getInstance(context);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.utils.AsyncLoader
    public void onDiscardResult(BatteryInfo batteryInfo) {
    }

    @Override // android.content.AsyncTaskLoader
    public BatteryInfo loadInBackground() {
        return this.batteryUtils.getBatteryInfo(this.mStatsHelper, "BatteryInfoLoader");
    }
}
