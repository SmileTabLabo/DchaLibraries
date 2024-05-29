package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.UserManager;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.utils.AsyncLoader;
/* loaded from: classes.dex */
public class BatteryStatsHelperLoader extends AsyncLoader<BatteryStatsHelper> {
    BatteryUtils mBatteryUtils;
    UserManager mUserManager;

    public BatteryStatsHelperLoader(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mBatteryUtils = BatteryUtils.getInstance(context);
    }

    @Override // android.content.AsyncTaskLoader
    public BatteryStatsHelper loadInBackground() {
        BatteryStatsHelper batteryStatsHelper = new BatteryStatsHelper(getContext(), true);
        this.mBatteryUtils.initBatteryStatsHelper(batteryStatsHelper, null, this.mUserManager);
        return batteryStatsHelper;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.utils.AsyncLoader
    public void onDiscardResult(BatteryStatsHelper batteryStatsHelper) {
    }
}
