package com.android.settings.graph;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.settings.R;
import com.android.settingslib.wifi.AccessPoint;
/* loaded from: classes.dex */
public class BottomLabelLayout extends LinearLayout {
    public BottomLabelLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        boolean z;
        int i3;
        int size = View.MeasureSpec.getSize(i);
        boolean isStacked = isStacked();
        boolean z2 = true;
        if (!isStacked && View.MeasureSpec.getMode(i) == 1073741824) {
            i3 = View.MeasureSpec.makeMeasureSpec(size, AccessPoint.UNREACHABLE_RSSI);
            z = true;
        } else {
            z = false;
            i3 = i;
        }
        super.onMeasure(i3, i2);
        if (!isStacked && (getMeasuredWidthAndState() & (-16777216)) == 16777216) {
            setStacked(true);
        } else {
            z2 = z;
        }
        if (z2) {
            super.onMeasure(i, i2);
        }
    }

    void setStacked(boolean z) {
        setOrientation(z ? 1 : 0);
        setGravity(z ? 8388611 : 80);
        View findViewById = findViewById(R.id.spacer);
        if (findViewById != null) {
            findViewById.setVisibility(z ? 8 : 0);
        }
    }

    private boolean isStacked() {
        return getOrientation() == 1;
    }
}
