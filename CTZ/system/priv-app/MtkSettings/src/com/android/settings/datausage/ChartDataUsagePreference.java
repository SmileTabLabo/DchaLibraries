package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkPolicy;
import android.net.NetworkStatsHistory;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.graph.UsageView;
/* loaded from: classes.dex */
public class ChartDataUsagePreference extends Preference {
    private long mEnd;
    private final int mLimitColor;
    private NetworkStatsHistory mNetwork;
    private NetworkPolicy mPolicy;
    private int mSecondaryColor;
    private int mSeriesColor;
    private long mStart;
    private final int mWarningColor;

    public ChartDataUsagePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setSelectable(false);
        this.mLimitColor = Utils.getColorAttr(context, 16844099);
        this.mWarningColor = Utils.getColorAttr(context, 16842808);
        setLayoutResource(R.layout.data_usage_graph);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        UsageView usageView = (UsageView) preferenceViewHolder.findViewById(R.id.data_usage);
        if (this.mNetwork == null) {
            return;
        }
        int top = getTop();
        usageView.clearPaths();
        usageView.configureGraph(toInt(this.mEnd - this.mStart), top);
        calcPoints(usageView);
        usageView.setBottomLabels(new CharSequence[]{Utils.formatDateRange(getContext(), this.mStart, this.mStart), Utils.formatDateRange(getContext(), this.mEnd, this.mEnd)});
        bindNetworkPolicy(usageView, this.mPolicy, top);
    }

    public int getTop() {
        int indexAfter = this.mNetwork.getIndexAfter(this.mEnd);
        NetworkStatsHistory.Entry entry = null;
        long j = 0;
        for (int indexBefore = this.mNetwork.getIndexBefore(this.mStart); indexBefore <= indexAfter; indexBefore++) {
            entry = this.mNetwork.getValues(indexBefore, entry);
            j += entry.rxBytes + entry.txBytes;
        }
        return (int) (Math.max(j, this.mPolicy != null ? Math.max(this.mPolicy.limitBytes, this.mPolicy.warningBytes) : 0L) / 524288);
    }

    void calcPoints(UsageView usageView) {
        SparseIntArray sparseIntArray = new SparseIntArray();
        int indexAfter = this.mNetwork.getIndexAfter(this.mStart);
        int indexAfter2 = this.mNetwork.getIndexAfter(this.mEnd);
        if (indexAfter < 0) {
            return;
        }
        sparseIntArray.put(0, 0);
        NetworkStatsHistory.Entry entry = null;
        long j = 0;
        while (indexAfter <= indexAfter2) {
            entry = this.mNetwork.getValues(indexAfter, entry);
            long j2 = entry.bucketStart;
            long j3 = entry.bucketDuration + j2;
            j += entry.rxBytes + entry.txBytes;
            if (indexAfter == 0) {
                sparseIntArray.put(toInt(j2 - this.mStart) - 1, -1);
            }
            int i = toInt((j2 - this.mStart) + 1);
            int i2 = (int) (j / 524288);
            sparseIntArray.put(i, i2);
            sparseIntArray.put(toInt(j3 - this.mStart), i2);
            indexAfter++;
        }
        if (sparseIntArray.size() > 1) {
            usageView.addPath(sparseIntArray);
        }
    }

    private int toInt(long j) {
        return (int) (j / 60000);
    }

    private void bindNetworkPolicy(UsageView usageView, NetworkPolicy networkPolicy, int i) {
        int i2;
        CharSequence[] charSequenceArr = new CharSequence[3];
        if (networkPolicy == null) {
            return;
        }
        int i3 = 0;
        if (networkPolicy.limitBytes != -1) {
            i2 = this.mLimitColor;
            charSequenceArr[2] = getLabel(networkPolicy.limitBytes, R.string.data_usage_sweep_limit, this.mLimitColor);
        } else {
            i2 = 0;
        }
        if (networkPolicy.warningBytes != -1) {
            usageView.setDividerLoc((int) (networkPolicy.warningBytes / 524288));
            float f = ((float) (networkPolicy.warningBytes / 524288)) / i;
            usageView.setSideLabelWeights(1.0f - f, f);
            i3 = this.mWarningColor;
            charSequenceArr[1] = getLabel(networkPolicy.warningBytes, R.string.data_usage_sweep_warning, this.mWarningColor);
        }
        usageView.setSideLabels(charSequenceArr);
        usageView.setDividerColors(i3, i2);
    }

    private CharSequence getLabel(long j, int i, int i2) {
        Formatter.BytesResult formatBytes = Formatter.formatBytes(getContext().getResources(), j, 9);
        return new SpannableStringBuilder().append(TextUtils.expandTemplate(getContext().getText(i), formatBytes.value, formatBytes.units), new ForegroundColorSpan(i2), 0);
    }

    public void setNetworkPolicy(NetworkPolicy networkPolicy) {
        this.mPolicy = networkPolicy;
        notifyChanged();
    }

    public void setVisibleRange(long j, long j2) {
        this.mStart = j;
        this.mEnd = j2;
        notifyChanged();
    }

    public long getInspectStart() {
        return this.mStart;
    }

    public long getInspectEnd() {
        return this.mEnd;
    }

    public void setNetworkStats(NetworkStatsHistory networkStatsHistory) {
        this.mNetwork = networkStatsHistory;
        notifyChanged();
    }

    public void setColors(int i, int i2) {
        this.mSeriesColor = i;
        this.mSecondaryColor = i2;
        notifyChanged();
    }
}
