package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.R$id;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.qs.DataUsageGraph;
import java.text.DecimalFormat;
/* loaded from: a.zip:com/android/systemui/qs/tiles/DataUsageDetailView.class */
public class DataUsageDetailView extends LinearLayout {
    private final DecimalFormat FORMAT;

    public DataUsageDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.FORMAT = new DecimalFormat("#.##");
    }

    private String formatBytes(long j) {
        double d;
        String str;
        long abs = Math.abs(j);
        if (abs > 1.048576E8d) {
            d = abs / 1.073741824E9d;
            str = "GB";
        } else if (abs > 102400.0d) {
            d = abs / 1048576.0d;
            str = "MB";
        } else {
            d = abs / 1024.0d;
            str = "KB";
        }
        return this.FORMAT.format((j < 0 ? -1 : 1) * d) + " " + str;
    }

    public void bind(DataUsageController.DataUsageInfo dataUsageInfo) {
        int i;
        long j;
        String string;
        Resources resources = this.mContext.getResources();
        int i2 = 2131558520;
        String str = null;
        if (dataUsageInfo.usageLevel < dataUsageInfo.warningLevel || dataUsageInfo.limitLevel <= 0) {
            i = 2131493572;
            j = dataUsageInfo.usageLevel;
            string = resources.getString(2131493577, formatBytes(dataUsageInfo.warningLevel));
        } else if (dataUsageInfo.usageLevel <= dataUsageInfo.limitLevel) {
            i = 2131493573;
            j = dataUsageInfo.limitLevel - dataUsageInfo.usageLevel;
            string = resources.getString(2131493575, formatBytes(dataUsageInfo.usageLevel));
            str = resources.getString(2131493576, formatBytes(dataUsageInfo.limitLevel));
        } else {
            i = 2131493574;
            j = dataUsageInfo.usageLevel - dataUsageInfo.limitLevel;
            string = resources.getString(2131493575, formatBytes(dataUsageInfo.usageLevel));
            str = resources.getString(2131493576, formatBytes(dataUsageInfo.limitLevel));
            i2 = 2131558521;
        }
        ((TextView) findViewById(16908310)).setText(i);
        TextView textView = (TextView) findViewById(2131886270);
        textView.setText(formatBytes(j));
        textView.setTextColor(this.mContext.getColor(i2));
        ((DataUsageGraph) findViewById(R$id.usage_graph)).setLevels(dataUsageInfo.limitLevel, dataUsageInfo.warningLevel, dataUsageInfo.usageLevel);
        ((TextView) findViewById(2131886272)).setText(dataUsageInfo.carrier);
        ((TextView) findViewById(2131886274)).setText(dataUsageInfo.period);
        TextView textView2 = (TextView) findViewById(2131886273);
        textView2.setVisibility(string != null ? 0 : 8);
        textView2.setText(string);
        TextView textView3 = (TextView) findViewById(2131886275);
        textView3.setVisibility(str != null ? 0 : 8);
        textView3.setText(str);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this, 16908310, 2131689856);
        FontSizeUtils.updateFontSize(this, 2131886270, 2131689857);
        FontSizeUtils.updateFontSize(this, 2131886272, 2131689856);
        FontSizeUtils.updateFontSize(this, 2131886273, 2131689856);
        FontSizeUtils.updateFontSize(this, 2131886274, 2131689856);
        FontSizeUtils.updateFontSize(this, 2131886275, 2131689856);
    }
}
