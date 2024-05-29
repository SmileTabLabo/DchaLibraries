package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class NotificationAppPreference extends MasterSwitchPreference {
    private boolean mChecked;
    private boolean mEnableSwitch;
    private Switch mSwitch;

    public NotificationAppPreference(Context context) {
        super(context);
        this.mEnableSwitch = true;
    }

    public NotificationAppPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mEnableSwitch = true;
    }

    public NotificationAppPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mEnableSwitch = true;
    }

    public NotificationAppPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mEnableSwitch = true;
    }

    @Override // com.android.settings.widget.MasterSwitchPreference, com.android.settingslib.TwoTargetPreference
    protected int getSecondTargetResId() {
        return R.layout.preference_widget_master_switch;
    }

    @Override // com.android.settings.widget.MasterSwitchPreference, com.android.settingslib.TwoTargetPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(16908312);
        if (findViewById != null) {
            findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.notification.NotificationAppPreference.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (NotificationAppPreference.this.mSwitch == null || NotificationAppPreference.this.mSwitch.isEnabled()) {
                        NotificationAppPreference.this.setChecked(!NotificationAppPreference.this.mChecked);
                        if (!NotificationAppPreference.this.callChangeListener(Boolean.valueOf(NotificationAppPreference.this.mChecked))) {
                            NotificationAppPreference.this.setChecked(!NotificationAppPreference.this.mChecked);
                        } else {
                            NotificationAppPreference.this.persistBoolean(NotificationAppPreference.this.mChecked);
                        }
                    }
                }
            });
        }
        this.mSwitch = (Switch) preferenceViewHolder.findViewById(R.id.switchWidget);
        if (this.mSwitch != null) {
            this.mSwitch.setContentDescription(getTitle());
            this.mSwitch.setChecked(this.mChecked);
            this.mSwitch.setEnabled(this.mEnableSwitch);
        }
    }

    @Override // com.android.settings.widget.MasterSwitchPreference
    public boolean isChecked() {
        return this.mSwitch != null && this.mChecked;
    }

    @Override // com.android.settings.widget.MasterSwitchPreference
    public void setChecked(boolean z) {
        this.mChecked = z;
        if (this.mSwitch != null) {
            this.mSwitch.setChecked(z);
        }
    }

    @Override // com.android.settings.widget.MasterSwitchPreference
    public void setSwitchEnabled(boolean z) {
        this.mEnableSwitch = z;
        if (this.mSwitch != null) {
            this.mSwitch.setEnabled(z);
        }
    }

    @Override // com.android.settings.widget.MasterSwitchPreference
    public void setDisabledByAdmin(RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        setSwitchEnabled(enforcedAdmin == null);
    }
}
