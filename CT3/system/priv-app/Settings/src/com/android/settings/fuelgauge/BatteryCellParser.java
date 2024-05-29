package com.android.settings.fuelgauge;

import android.os.BatteryStats;
import android.util.SparseIntArray;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.BatteryActiveView;
import com.android.settingslib.BatteryInfo;
/* loaded from: classes.dex */
public class BatteryCellParser implements BatteryInfo.BatteryDataParser, BatteryActiveView.BatteryActiveProvider {
    private final SparseIntArray mData = new SparseIntArray();
    private long mLastTime;
    private int mLastValue;
    private long mLength;

    protected int getValue(BatteryStats.HistoryItem rec) {
        if (((rec.states & 448) >> 6) == 3) {
            return 0;
        }
        if ((rec.states & 2097152) != 0) {
            return 1;
        }
        int bin = (rec.states & 56) >> 3;
        return bin + 2;
    }

    @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
    public void onParsingStarted(long startTime, long endTime) {
        this.mLength = endTime - startTime;
    }

    @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
    public void onDataPoint(long time, BatteryStats.HistoryItem record) {
        int value = getValue(record);
        if (value != this.mLastValue) {
            this.mData.put((int) time, value);
            this.mLastValue = value;
        }
        this.mLastTime = time;
    }

    @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
    public void onDataGap() {
        if (this.mLastValue == 0) {
            return;
        }
        this.mData.put((int) this.mLastTime, 0);
        this.mLastValue = 0;
    }

    @Override // com.android.settingslib.BatteryInfo.BatteryDataParser
    public void onParsingDone() {
        if (this.mLastValue == 0) {
            return;
        }
        this.mData.put((int) this.mLastTime, 0);
        this.mLastValue = 0;
    }

    @Override // com.android.settings.fuelgauge.BatteryActiveView.BatteryActiveProvider
    public long getPeriod() {
        return this.mLength;
    }

    @Override // com.android.settings.fuelgauge.BatteryActiveView.BatteryActiveProvider
    public boolean hasData() {
        return this.mData.size() > 1;
    }

    @Override // com.android.settings.fuelgauge.BatteryActiveView.BatteryActiveProvider
    public SparseIntArray getColorArray() {
        SparseIntArray ret = new SparseIntArray();
        for (int i = 0; i < this.mData.size(); i++) {
            ret.put(this.mData.keyAt(i), getColor(this.mData.valueAt(i)));
        }
        return ret;
    }

    private int getColor(int i) {
        return Utils.BADNESS_COLORS[i];
    }
}
