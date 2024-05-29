package com.android.systemui.qs.car;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.settingslib.Utils;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
/* loaded from: classes.dex */
public class CarStatusBarHeader extends LinearLayout {
    public CarStatusBarHeader(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        int colorAttr = Utils.getColorAttr(getContext(), 16842800);
        float f = colorAttr == -1 ? 0.0f : 1.0f;
        Rect rect = new Rect(0, 0, 0, 0);
        applyDarkness(R.id.battery, rect, f, colorAttr);
        applyDarkness(R.id.clock, rect, f, colorAttr);
        ((BatteryMeterView) findViewById(R.id.battery)).setForceShowPercent(true);
    }

    private void applyDarkness(int i, Rect rect, float f, int i2) {
        View findViewById = findViewById(i);
        if (findViewById instanceof DarkIconDispatcher.DarkReceiver) {
            ((DarkIconDispatcher.DarkReceiver) findViewById).onDarkChanged(rect, f, i2);
        }
    }
}
