package com.android.settings.fuelgauge;

import android.content.Context;
import android.util.SparseIntArray;
import com.android.internal.os.BatterySipper;
/* loaded from: classes.dex */
public interface PowerUsageFeatureProvider {
    String getAdvancedUsageScreenInfoString();

    boolean getEarlyWarningSignal(Context context, String str);

    Estimate getEnhancedBatteryPrediction(Context context);

    SparseIntArray getEnhancedBatteryPredictionCurve(Context context, long j);

    String getEnhancedEstimateDebugString(String str);

    String getOldEstimateDebugString(String str);

    boolean isEnhancedBatteryPredictionEnabled(Context context);

    boolean isEstimateDebugEnabled();

    boolean isSmartBatterySupported();

    boolean isTypeService(BatterySipper batterySipper);

    boolean isTypeSystem(BatterySipper batterySipper);
}
