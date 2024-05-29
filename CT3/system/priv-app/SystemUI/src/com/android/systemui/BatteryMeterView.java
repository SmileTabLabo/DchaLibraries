package com.android.systemui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/BatteryMeterView.class */
public class BatteryMeterView extends ImageView implements BatteryController.BatteryStateChangeCallback, TunerService.Tunable {
    private BatteryController mBatteryController;
    private final BatteryMeterDrawable mDrawable;
    private final String mSlotBattery;

    public BatteryMeterView(Context context) {
        this(context, null, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.BatteryMeterView, i, 0);
        this.mDrawable = new BatteryMeterDrawable(context, new Handler(), obtainStyledAttributes.getColor(0, context.getColor(2131558514)));
        obtainStyledAttributes.recycle();
        this.mSlotBattery = context.getString(17039407);
        setImageDrawable(this.mDrawable);
    }

    @Override // android.widget.ImageView, android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.widget.ImageView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mBatteryController.addStateChangedCallback(this);
        this.mDrawable.startListening();
        TunerService.get(getContext()).addTunable(this, "icon_blacklist");
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        setContentDescription(getContext().getString(z2 ? 2131493425 : 2131493424, Integer.valueOf(i)));
    }

    @Override // android.widget.ImageView, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBatteryController.removeStateChangedCallback(this);
        this.mDrawable.stopListening();
        TunerService.get(getContext()).removeTunable(this);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            setVisibility(StatusBarIconController.getIconBlacklist(str2).contains(this.mSlotBattery) ? 8 : 0);
        }
    }

    public void setBatteryController(BatteryController batteryController) {
        this.mBatteryController = batteryController;
        this.mDrawable.setBatteryController(batteryController);
    }

    public void setDarkIntensity(float f) {
        this.mDrawable.setDarkIntensity(f);
    }
}
