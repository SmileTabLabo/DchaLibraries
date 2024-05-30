package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStatsHistory;
import android.util.Pair;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.net.ChartData;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Objects;
/* loaded from: classes.dex */
public class CycleAdapter extends ArrayAdapter<CycleItem> {
    private final AdapterView.OnItemSelectedListener mListener;
    private final SpinnerInterface mSpinner;

    /* loaded from: classes.dex */
    public interface SpinnerInterface {
        Object getSelectedItem();

        void setAdapter(CycleAdapter cycleAdapter);

        void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener);

        void setSelection(int i);
    }

    public CycleAdapter(Context context, SpinnerInterface spinnerInterface, AdapterView.OnItemSelectedListener onItemSelectedListener, boolean z) {
        super(context, z ? R.layout.filter_spinner_item : R.layout.data_usage_cycle_item);
        setDropDownViewResource(17367049);
        this.mSpinner = spinnerInterface;
        this.mListener = onItemSelectedListener;
        this.mSpinner.setAdapter(this);
        this.mSpinner.setOnItemSelectedListener(this.mListener);
    }

    public int findNearestPosition(CycleItem cycleItem) {
        if (cycleItem != null) {
            for (int count = getCount() - 1; count >= 0; count--) {
                if (getItem(count).compareTo(cycleItem) >= 0) {
                    return count;
                }
            }
            return 0;
        }
        return 0;
    }

    public boolean updateCycleList(NetworkPolicy networkPolicy, ChartData chartData) {
        long j;
        long j2;
        NetworkStatsHistory.Entry entry;
        boolean z;
        boolean z2;
        Iterator it;
        boolean z3;
        Iterator it2;
        ChartData chartData2 = chartData;
        CycleItem cycleItem = (CycleItem) this.mSpinner.getSelectedItem();
        clear();
        Context context = getContext();
        if (chartData2 != null) {
            j2 = chartData2.network.getStart();
            j = chartData2.network.getEnd();
        } else {
            j = Long.MIN_VALUE;
            j2 = Long.MAX_VALUE;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long j3 = j2 == Long.MAX_VALUE ? currentTimeMillis : j2;
        if (j == Long.MIN_VALUE) {
            j = currentTimeMillis + 1;
        }
        if (networkPolicy != null) {
            Iterator cycleIterator = NetworkPolicyManager.cycleIterator(networkPolicy);
            entry = null;
            z = false;
            while (cycleIterator.hasNext()) {
                Pair pair = (Pair) cycleIterator.next();
                long epochMilli = ((ZonedDateTime) pair.first).toInstant().toEpochMilli();
                long epochMilli2 = ((ZonedDateTime) pair.second).toInstant().toEpochMilli();
                if (chartData2 != null) {
                    NetworkStatsHistory.Entry values = chartData2.network.getValues(epochMilli, epochMilli2, entry);
                    it = cycleIterator;
                    z3 = values.rxBytes + values.txBytes > 0;
                    entry = values;
                } else {
                    it = cycleIterator;
                    z3 = true;
                }
                if (z3) {
                    it2 = it;
                    add(new CycleItem(context, epochMilli, epochMilli2));
                    z = true;
                } else {
                    it2 = it;
                }
                cycleIterator = it2;
            }
        } else {
            entry = null;
            z = false;
        }
        if (!z) {
            NetworkStatsHistory.Entry entry2 = entry;
            while (j > j3) {
                long j4 = j - 2419200000L;
                if (chartData2 != null) {
                    NetworkStatsHistory.Entry values2 = chartData2.network.getValues(j4, j, entry2);
                    z2 = values2.rxBytes + values2.txBytes > 0;
                    entry2 = values2;
                } else {
                    z2 = true;
                }
                if (z2) {
                    add(new CycleItem(context, j4, j));
                }
                j = j4;
                chartData2 = chartData;
            }
        }
        if (getCount() > 0) {
            int findNearestPosition = findNearestPosition(cycleItem);
            this.mSpinner.setSelection(findNearestPosition);
            if (!Objects.equals(getItem(findNearestPosition), cycleItem)) {
                this.mListener.onItemSelected(null, null, findNearestPosition, 0L);
                return false;
            }
        }
        return true;
    }

    /* loaded from: classes.dex */
    public static class CycleItem implements Comparable<CycleItem> {
        public long end;
        public CharSequence label;
        public long start;

        public CycleItem(Context context, long j, long j2) {
            this.label = Utils.formatDateRange(context, j, j2);
            this.start = j;
            this.end = j2;
        }

        public String toString() {
            return this.label.toString();
        }

        public boolean equals(Object obj) {
            if (obj instanceof CycleItem) {
                CycleItem cycleItem = (CycleItem) obj;
                return this.start == cycleItem.start && this.end == cycleItem.end;
            }
            return false;
        }

        @Override // java.lang.Comparable
        public int compareTo(CycleItem cycleItem) {
            return Long.compare(this.start, cycleItem.start);
        }
    }
}
