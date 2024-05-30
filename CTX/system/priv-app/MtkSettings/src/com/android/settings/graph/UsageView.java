package com.android.settings.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
/* loaded from: classes.dex */
public class UsageView extends FrameLayout {
    private final TextView[] mBottomLabels;
    private final TextView[] mLabels;
    private final UsageGraph mUsageGraph;

    public UsageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater.from(context).inflate(R.layout.usage_view, this);
        this.mUsageGraph = (UsageGraph) findViewById(R.id.usage_graph);
        this.mLabels = new TextView[]{(TextView) findViewById(R.id.label_bottom), (TextView) findViewById(R.id.label_middle), (TextView) findViewById(R.id.label_top)};
        this.mBottomLabels = new TextView[]{(TextView) findViewById(R.id.label_start), (TextView) findViewById(R.id.label_end)};
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, com.android.settingslib.R.styleable.UsageView, 0, 0);
        if (obtainStyledAttributes.hasValue(3)) {
            setSideLabels(obtainStyledAttributes.getTextArray(3));
        }
        if (obtainStyledAttributes.hasValue(2)) {
            setBottomLabels(obtainStyledAttributes.getTextArray(2));
        }
        if (obtainStyledAttributes.hasValue(4)) {
            int color = obtainStyledAttributes.getColor(4, 0);
            for (TextView textView : this.mLabels) {
                textView.setTextColor(color);
            }
            for (TextView textView2 : this.mBottomLabels) {
                textView2.setTextColor(color);
            }
        }
        if (obtainStyledAttributes.hasValue(0)) {
            int i = obtainStyledAttributes.getInt(0, 0);
            if (i == 8388613) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.graph_label_group);
                LinearLayout linearLayout2 = (LinearLayout) findViewById(R.id.label_group);
                linearLayout.removeView(linearLayout2);
                linearLayout.addView(linearLayout2);
                linearLayout2.setGravity(8388613);
                LinearLayout linearLayout3 = (LinearLayout) findViewById(R.id.bottom_label_group);
                View findViewById = linearLayout3.findViewById(R.id.bottom_label_space);
                linearLayout3.removeView(findViewById);
                linearLayout3.addView(findViewById);
            } else if (i != 8388611) {
                throw new IllegalArgumentException("Unsupported gravity " + i);
            }
        }
        this.mUsageGraph.setAccentColor(obtainStyledAttributes.getColor(1, 0));
        obtainStyledAttributes.recycle();
    }

    public void clearPaths() {
        this.mUsageGraph.clearPaths();
    }

    public void addPath(SparseIntArray sparseIntArray) {
        this.mUsageGraph.addPath(sparseIntArray);
    }

    public void addProjectedPath(SparseIntArray sparseIntArray) {
        this.mUsageGraph.addProjectedPath(sparseIntArray);
    }

    public void configureGraph(int i, int i2) {
        this.mUsageGraph.setMax(i, i2);
    }

    public void setAccentColor(int i) {
        this.mUsageGraph.setAccentColor(i);
    }

    public void setDividerLoc(int i) {
        this.mUsageGraph.setDividerLoc(i);
    }

    public void setDividerColors(int i, int i2) {
        this.mUsageGraph.setDividerColors(i, i2);
    }

    public void setSideLabelWeights(float f, float f2) {
        setWeight(R.id.space1, f);
        setWeight(R.id.space2, f2);
    }

    private void setWeight(int i, float f) {
        View findViewById = findViewById(i);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
        layoutParams.weight = f;
        findViewById.setLayoutParams(layoutParams);
    }

    public void setSideLabels(CharSequence[] charSequenceArr) {
        if (charSequenceArr.length != this.mLabels.length) {
            throw new IllegalArgumentException("Invalid number of labels");
        }
        for (int i = 0; i < this.mLabels.length; i++) {
            this.mLabels[i].setText(charSequenceArr[i]);
        }
    }

    public void setBottomLabels(CharSequence[] charSequenceArr) {
        if (charSequenceArr.length != this.mBottomLabels.length) {
            throw new IllegalArgumentException("Invalid number of labels");
        }
        for (int i = 0; i < this.mBottomLabels.length; i++) {
            this.mBottomLabels[i].setText(charSequenceArr[i]);
        }
    }
}
