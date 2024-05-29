package com.android.systemui.statusbar.car.hvac;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.R;
/* loaded from: classes.dex */
public class TemperatureTextView extends TextView implements TemperatureView {
    private final int mAreaId;
    private final int mPropertyId;
    private final String mTempFormat;

    public TemperatureTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.TemperatureView);
        this.mAreaId = obtainStyledAttributes.getInt(0, -1);
        this.mPropertyId = obtainStyledAttributes.getInt(1, -1);
        String string = obtainStyledAttributes.getString(2);
        this.mTempFormat = string == null ? "%.1f°" : string;
        ((HvacController) Dependency.get(HvacController.class)).addHvacTextView(this);
    }

    @Override // com.android.systemui.statusbar.car.hvac.TemperatureView
    public void setTemp(float f) {
        if (Float.isNaN(f)) {
            setText("--");
        } else {
            setText(String.format(this.mTempFormat, Float.valueOf(f)));
        }
    }

    @Override // com.android.systemui.statusbar.car.hvac.TemperatureView
    public int getPropertyId() {
        return this.mPropertyId;
    }

    @Override // com.android.systemui.statusbar.car.hvac.TemperatureView
    public int getAreaId() {
        return this.mAreaId;
    }
}
