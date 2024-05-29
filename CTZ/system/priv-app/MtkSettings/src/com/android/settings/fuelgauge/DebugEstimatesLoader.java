package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryStats;
import android.os.SystemClock;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.utils.AsyncLoader;
import com.android.settingslib.utils.PowerUtil;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DebugEstimatesLoader extends AsyncLoader<List<BatteryInfo>> {
    private BatteryStatsHelper mStatsHelper;

    public DebugEstimatesLoader(Context context, BatteryStatsHelper batteryStatsHelper) {
        super(context);
        this.mStatsHelper = batteryStatsHelper;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.utils.AsyncLoader
    public void onDiscardResult(List<BatteryInfo> list) {
    }

    @Override // android.content.AsyncTaskLoader
    public List<BatteryInfo> loadInBackground() {
        Context context = getContext();
        PowerUsageFeatureProvider powerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
        long convertMsToUs = PowerUtil.convertMsToUs(SystemClock.elapsedRealtime());
        Intent registerReceiver = getContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        BatteryStats stats = this.mStatsHelper.getStats();
        BatteryInfo batteryInfoOld = BatteryInfo.getBatteryInfoOld(getContext(), registerReceiver, stats, convertMsToUs, false);
        Estimate enhancedBatteryPrediction = powerUsageFeatureProvider.getEnhancedBatteryPrediction(context);
        if (enhancedBatteryPrediction == null) {
            enhancedBatteryPrediction = new Estimate(0L, false, -1L);
        }
        BatteryInfo batteryInfo = BatteryInfo.getBatteryInfo(getContext(), registerReceiver, stats, enhancedBatteryPrediction, convertMsToUs, false);
        ArrayList arrayList = new ArrayList();
        arrayList.add(batteryInfoOld);
        arrayList.add(batteryInfo);
        return arrayList;
    }
}
