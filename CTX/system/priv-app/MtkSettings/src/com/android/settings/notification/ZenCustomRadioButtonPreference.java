package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import com.android.settings.R;
import com.android.settingslib.TwoTargetPreference;
/* loaded from: classes.dex */
public class ZenCustomRadioButtonPreference extends TwoTargetPreference implements View.OnClickListener {
    private RadioButton mButton;
    private boolean mChecked;
    private OnGearClickListener mOnGearClickListener;
    private OnRadioButtonClickListener mOnRadioButtonClickListener;

    /* loaded from: classes.dex */
    public interface OnGearClickListener {
        void onGearClick(ZenCustomRadioButtonPreference zenCustomRadioButtonPreference);
    }

    /* loaded from: classes.dex */
    public interface OnRadioButtonClickListener {
        void onRadioButtonClick(ZenCustomRadioButtonPreference zenCustomRadioButtonPreference);
    }

    public ZenCustomRadioButtonPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    public ZenCustomRadioButtonPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    public ZenCustomRadioButtonPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    public ZenCustomRadioButtonPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    @Override // com.android.settingslib.TwoTargetPreference
    protected int getSecondTargetResId() {
        return R.layout.preference_widget_gear;
    }

    public void setOnGearClickListener(OnGearClickListener onGearClickListener) {
        this.mOnGearClickListener = onGearClickListener;
        notifyChanged();
    }

    public void setOnRadioButtonClickListener(OnRadioButtonClickListener onRadioButtonClickListener) {
        this.mOnRadioButtonClickListener = onRadioButtonClickListener;
        notifyChanged();
    }

    @Override // com.android.settingslib.TwoTargetPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(R.id.checkbox_frame);
        if (findViewById != null) {
            findViewById.setOnClickListener(this);
        }
        this.mButton = (RadioButton) preferenceViewHolder.findViewById(16908289);
        if (this.mButton != null) {
            this.mButton.setChecked(this.mChecked);
        }
        View findViewById2 = preferenceViewHolder.findViewById(16908312);
        View findViewById3 = preferenceViewHolder.findViewById(R.id.two_target_divider);
        if (this.mOnGearClickListener != null) {
            findViewById3.setVisibility(0);
            findViewById2.setVisibility(0);
            findViewById2.setOnClickListener(this);
            return;
        }
        findViewById3.setVisibility(8);
        findViewById2.setVisibility(8);
        findViewById2.setOnClickListener(null);
    }

    public void setChecked(boolean z) {
        this.mChecked = z;
        if (this.mButton != null) {
            this.mButton.setChecked(z);
        }
    }

    @Override // android.support.v7.preference.Preference
    public void onClick() {
        if (this.mOnRadioButtonClickListener != null) {
            this.mOnRadioButtonClickListener.onRadioButtonClick(this);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() == 16908312) {
            if (this.mOnGearClickListener != null) {
                this.mOnGearClickListener.onGearClick(this);
            }
        } else if (view.getId() == R.id.checkbox_frame && this.mOnRadioButtonClickListener != null) {
            this.mOnRadioButtonClickListener.onRadioButtonClick(this);
        }
    }
}
