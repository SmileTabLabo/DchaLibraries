package com.android.settings.wifi;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import com.android.settings.R;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPointPreference;
/* loaded from: classes.dex */
public class ConnectedAccessPointPreference extends AccessPointPreference implements View.OnClickListener {
    private boolean mIsCaptivePortal;
    private OnGearClickListener mOnGearClickListener;

    /* loaded from: classes.dex */
    public interface OnGearClickListener {
        void onGearClick(ConnectedAccessPointPreference connectedAccessPointPreference);
    }

    public ConnectedAccessPointPreference(AccessPoint accessPoint, Context context, AccessPointPreference.UserBadgeCache userBadgeCache, int i, boolean z) {
        super(accessPoint, context, userBadgeCache, i, z);
    }

    @Override // com.android.settingslib.wifi.AccessPointPreference
    protected int getWidgetLayoutResourceId() {
        return R.layout.preference_widget_gear_optional_background;
    }

    @Override // com.android.settingslib.wifi.AccessPointPreference
    public void refresh() {
        super.refresh();
        setShowDivider(this.mIsCaptivePortal);
        if (this.mIsCaptivePortal) {
            setSummary(R.string.wifi_tap_to_sign_in);
        }
    }

    public void setOnGearClickListener(OnGearClickListener onGearClickListener) {
        this.mOnGearClickListener = onGearClickListener;
        notifyChanged();
    }

    @Override // com.android.settingslib.wifi.AccessPointPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(R.id.settings_button);
        findViewById.setOnClickListener(this);
        preferenceViewHolder.findViewById(R.id.settings_button_no_background).setVisibility(this.mIsCaptivePortal ? 4 : 0);
        findViewById.setVisibility(this.mIsCaptivePortal ? 0 : 4);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() == R.id.settings_button && this.mOnGearClickListener != null) {
            this.mOnGearClickListener.onGearClick(this);
        }
    }

    public void setCaptivePortal(boolean z) {
        if (this.mIsCaptivePortal != z) {
            this.mIsCaptivePortal = z;
            refresh();
        }
    }
}
