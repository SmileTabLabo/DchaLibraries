package com.android.settings.fuelgauge;

import android.os.BatteryStats;
/* loaded from: classes.dex */
public class BatteryWifiParser extends BatteryFlagParser {
    public BatteryWifiParser(int accentColor) {
        super(accentColor, false, 0);
    }

    @Override // com.android.settings.fuelgauge.BatteryFlagParser
    protected boolean isSet(BatteryStats.HistoryItem record) {
        switch ((record.states2 & 15) >> 0) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 11:
            case 12:
                return false;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            default:
                return true;
        }
    }
}
