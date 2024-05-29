package com.android.settingslib.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$styleable;
/* loaded from: a.zip:com/android/settingslib/graph/UsageView.class */
public class UsageView extends FrameLayout {
    private final TextView[] mBottomLabels;
    private final TextView[] mLabels;
    private final UsageGraph mUsageGraph;

    public UsageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater.from(context).inflate(R$layout.usage_view, this);
        this.mUsageGraph = (UsageGraph) findViewById(R$id.usage_graph);
        this.mLabels = new TextView[]{(TextView) findViewById(R$id.label_bottom), (TextView) findViewById(R$id.label_middle), (TextView) findViewById(R$id.label_top)};
        this.mBottomLabels = new TextView[]{(TextView) findViewById(R$id.label_start), (TextView) findViewById(R$id.label_end)};
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.UsageView, 0, 0);
        if (obtainStyledAttributes.hasValue(R$styleable.UsageView_sideLabels)) {
            setSideLabels(obtainStyledAttributes.getTextArray(R$styleable.UsageView_sideLabels));
        }
        if (obtainStyledAttributes.hasValue(R$styleable.UsageView_bottomLabels)) {
            setBottomLabels(obtainStyledAttributes.getTextArray(R$styleable.UsageView_bottomLabels));
        }
        if (obtainStyledAttributes.hasValue(R$styleable.UsageView_textColor)) {
            int color = obtainStyledAttributes.getColor(R$styleable.UsageView_textColor, 0);
            for (TextView textView : this.mLabels) {
                textView.setTextColor(color);
            }
            for (TextView textView2 : this.mBottomLabels) {
                textView2.setTextColor(color);
            }
        }
        if (obtainStyledAttributes.hasValue(R$styleable.UsageView_android_gravity)) {
            int i = obtainStyledAttributes.getInt(R$styleable.UsageView_android_gravity, 0);
            if (i == 8388613) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R$id.graph_label_group);
                LinearLayout linearLayout2 = (LinearLayout) findViewById(R$id.label_group);
                linearLayout.removeView(linearLayout2);
                linearLayout.addView(linearLayout2);
                linearLayout2.setGravity(8388613);
                LinearLayout linearLayout3 = (LinearLayout) findViewById(R$id.bottom_label_group);
                linearLayout3.setPadding(linearLayout3.getPaddingRight(), linearLayout3.getPaddingTop(), linearLayout3.getPaddingLeft(), linearLayout3.getPaddingBottom());
            } else if (i != 8388611) {
                throw new IllegalArgumentException("Unsupported gravity " + i);
            }
        }
        this.mUsageGraph.setAccentColor(obtainStyledAttributes.getColor(R$styleable.UsageView_android_colorAccent, 0));
    }

    public void addPath(SparseIntArray sparseIntArray) {
        this.mUsageGraph.addPath(sparseIntArray);
    }

    public void clearPaths() {
        this.mUsageGraph.clearPaths();
    }

    public void configureGraph(int i, int i2, boolean z, boolean z2) {
        this.mUsageGraph.setMax(i, i2);
        this.mUsageGraph.setShowProjection(z, z2);
    }

    public void setBottomLabels(CharSequence[] charSequenceArr) {
        if (charSequenceArr.length != this.mBottomLabels.length) {
            throw new IllegalArgumentException("Invalid number of labels");
        }
        for (int i = 0; i < this.mBottomLabels.length; i++) {
            this.mBottomLabels[i].setText(charSequenceArr[i]);
        }
    }

    public void setSideLabels(CharSequence[] charSequenceArr) {
        if (charSequenceArr.length != this.mLabels.length) {
            throw new IllegalArgumentException("Invalid number of labels");
        }
        for (int i = 0; i < this.mLabels.length; i++) {
            this.mLabels[i].setText(charSequenceArr[i]);
        }
    }
}
