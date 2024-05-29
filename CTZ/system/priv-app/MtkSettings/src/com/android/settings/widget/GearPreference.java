package com.android.settings.widget;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.R;
import com.android.settingslib.RestrictedPreference;
/* loaded from: classes.dex */
public class GearPreference extends RestrictedPreference implements View.OnClickListener {
    private OnGearClickListener mOnGearClickListener;

    /* loaded from: classes.dex */
    public interface OnGearClickListener {
        void onGearClick(GearPreference gearPreference);
    }

    public GearPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setOnGearClickListener(OnGearClickListener onGearClickListener) {
        this.mOnGearClickListener = onGearClickListener;
        notifyChanged();
    }

    @Override // com.android.settingslib.RestrictedPreference, com.android.settingslib.TwoTargetPreference
    protected int getSecondTargetResId() {
        return R.layout.preference_widget_gear;
    }

    @Override // com.android.settingslib.RestrictedPreference, com.android.settingslib.TwoTargetPreference
    protected boolean shouldHideSecondTarget() {
        return this.mOnGearClickListener == null;
    }

    @Override // com.android.settingslib.RestrictedPreference, com.android.settingslib.TwoTargetPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(R.id.settings_button);
        if (this.mOnGearClickListener != null) {
            findViewById.setVisibility(0);
            findViewById.setOnClickListener(this);
        } else {
            findViewById.setVisibility(8);
            findViewById.setOnClickListener(null);
        }
        findViewById.setEnabled(true);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.settings_button && this.mOnGearClickListener != null) {
            this.mOnGearClickListener.onGearClick(this);
        }
    }
}
