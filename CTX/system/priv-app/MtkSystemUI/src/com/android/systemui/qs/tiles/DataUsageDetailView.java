package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.DataUsageGraph;
import java.text.DecimalFormat;
/* loaded from: classes.dex */
public class DataUsageDetailView extends LinearLayout {
    private final DecimalFormat FORMAT;

    public DataUsageDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.FORMAT = new DecimalFormat("#.##");
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this, 16908310, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_text, R.dimen.qs_data_usage_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_carrier_text, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_info_top_text, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_period_text, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_info_bottom_text, R.dimen.qs_data_usage_text_size);
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x009a  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x00fc  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00fe  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0110  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0112  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x012e  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x0130  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x0136  */
    /* JADX WARN: Removed duplicated region for block: B:37:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void bind(DataUsageController.DataUsageInfo dataUsageInfo) {
        int i;
        long j;
        String string;
        String str;
        int i2;
        boolean z;
        Resources resources = this.mContext.getResources();
        if (dataUsageInfo.usageLevel >= dataUsageInfo.warningLevel && dataUsageInfo.limitLevel > 0) {
            if (dataUsageInfo.usageLevel <= dataUsageInfo.limitLevel) {
                i = R.string.quick_settings_cellular_detail_remaining_data;
                j = dataUsageInfo.limitLevel - dataUsageInfo.usageLevel;
                string = resources.getString(R.string.quick_settings_cellular_detail_data_used, formatBytes(dataUsageInfo.usageLevel));
                str = resources.getString(R.string.quick_settings_cellular_detail_data_limit, formatBytes(dataUsageInfo.limitLevel));
            } else {
                i = R.string.quick_settings_cellular_detail_over_limit;
                j = dataUsageInfo.usageLevel - dataUsageInfo.limitLevel;
                string = resources.getString(R.string.quick_settings_cellular_detail_data_used, formatBytes(dataUsageInfo.usageLevel));
                str = resources.getString(R.string.quick_settings_cellular_detail_data_limit, formatBytes(dataUsageInfo.limitLevel));
                i2 = Utils.getDefaultColor(this.mContext, 16844099);
                if (i2 == 0) {
                    i2 = Utils.getColorAccent(this.mContext);
                }
                ((TextView) findViewById(16908310)).setText(i);
                TextView textView = (TextView) findViewById(R.id.usage_text);
                textView.setText(formatBytes(j));
                textView.setTextColor(i2);
                DataUsageGraph dataUsageGraph = (DataUsageGraph) findViewById(R.id.usage_graph);
                dataUsageGraph.setLevels(dataUsageInfo.limitLevel, dataUsageInfo.warningLevel, dataUsageInfo.usageLevel);
                ((TextView) findViewById(R.id.usage_carrier_text)).setText(dataUsageInfo.carrier);
                ((TextView) findViewById(R.id.usage_period_text)).setText(dataUsageInfo.period);
                TextView textView2 = (TextView) findViewById(R.id.usage_info_top_text);
                textView2.setVisibility(string == null ? 0 : 8);
                textView2.setText(string);
                TextView textView3 = (TextView) findViewById(R.id.usage_info_bottom_text);
                textView3.setVisibility(str == null ? 0 : 8);
                textView3.setText(str);
                z = dataUsageInfo.warningLevel <= 0 || dataUsageInfo.limitLevel > 0;
                dataUsageGraph.setVisibility(!z ? 0 : 8);
                if (z) {
                    textView2.setVisibility(8);
                    return;
                }
                return;
            }
        } else {
            i = R.string.quick_settings_cellular_detail_data_usage;
            j = dataUsageInfo.usageLevel;
            string = resources.getString(R.string.quick_settings_cellular_detail_data_warning, formatBytes(dataUsageInfo.warningLevel));
            str = null;
        }
        i2 = 0;
        if (i2 == 0) {
        }
        ((TextView) findViewById(16908310)).setText(i);
        TextView textView4 = (TextView) findViewById(R.id.usage_text);
        textView4.setText(formatBytes(j));
        textView4.setTextColor(i2);
        DataUsageGraph dataUsageGraph2 = (DataUsageGraph) findViewById(R.id.usage_graph);
        dataUsageGraph2.setLevels(dataUsageInfo.limitLevel, dataUsageInfo.warningLevel, dataUsageInfo.usageLevel);
        ((TextView) findViewById(R.id.usage_carrier_text)).setText(dataUsageInfo.carrier);
        ((TextView) findViewById(R.id.usage_period_text)).setText(dataUsageInfo.period);
        TextView textView22 = (TextView) findViewById(R.id.usage_info_top_text);
        textView22.setVisibility(string == null ? 0 : 8);
        textView22.setText(string);
        TextView textView32 = (TextView) findViewById(R.id.usage_info_bottom_text);
        textView32.setVisibility(str == null ? 0 : 8);
        textView32.setText(str);
        if (dataUsageInfo.warningLevel <= 0) {
        }
        dataUsageGraph2.setVisibility(!z ? 0 : 8);
        if (z) {
        }
    }

    private String formatBytes(long j) {
        double d;
        String str;
        double abs = Math.abs(j);
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
        StringBuilder sb = new StringBuilder();
        sb.append(this.FORMAT.format(d * (j < 0 ? -1 : 1)));
        sb.append(" ");
        sb.append(str);
        return sb.toString();
    }
}
