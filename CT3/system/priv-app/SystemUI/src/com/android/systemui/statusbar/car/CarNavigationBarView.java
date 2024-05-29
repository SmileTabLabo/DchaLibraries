package com.android.systemui.statusbar.car;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.statusbar.phone.NavigationBarView;
/* loaded from: a.zip:com/android/systemui/statusbar/car/CarNavigationBarView.class */
class CarNavigationBarView extends NavigationBarView {
    private LinearLayout mLightsOutButtons;
    private LinearLayout mNavButtons;

    public CarNavigationBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void addButton(CarNavigationButton carNavigationButton, CarNavigationButton carNavigationButton2) {
        this.mNavButtons.addView(carNavigationButton);
        this.mLightsOutButtons.addView(carNavigationButton2);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarView
    public View getCurrentView() {
        return this;
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarView, android.view.View
    public void onFinishInflate() {
        this.mNavButtons = (LinearLayout) findViewById(2131886266);
        this.mLightsOutButtons = (LinearLayout) findViewById(2131886267);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarView
    public void reorient() {
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarView
    public void setDisabledFlags(int i, boolean z) {
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarView
    public void setNavigationIconHints(int i, boolean z) {
    }
}
