package com.android.settings.fuelgauge;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.PreferenceViewHolder;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.TintablePreference;
import com.android.settings.Utils;
/* loaded from: classes.dex */
public class PowerGaugePreference extends TintablePreference {
    private CharSequence mContentDescription;
    private final int mIconSize;
    private BatteryEntry mInfo;
    private CharSequence mProgress;

    public PowerGaugePreference(Context context, Drawable icon, CharSequence contentDescription, BatteryEntry info) {
        super(context, null);
        setIcon(icon == null ? new ColorDrawable(0) : icon);
        setWidgetLayoutResource(R.layout.preference_widget_summary);
        this.mInfo = info;
        this.mContentDescription = contentDescription;
        this.mIconSize = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
    }

    public void setContentDescription(String name) {
        this.mContentDescription = name;
        notifyChanged();
    }

    public void setPercent(double percentOfMax, double percentOfTotal) {
        this.mProgress = Utils.formatPercentage((int) (0.5d + percentOfTotal));
        notifyChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BatteryEntry getInfo() {
        return this.mInfo;
    }

    @Override // com.android.settings.TintablePreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        ImageView icon = (ImageView) view.findViewById(16908294);
        icon.setLayoutParams(new LinearLayout.LayoutParams(this.mIconSize, this.mIconSize));
        ((TextView) view.findViewById(R.id.widget_summary)).setText(this.mProgress);
        if (this.mContentDescription == null) {
            return;
        }
        TextView titleView = (TextView) view.findViewById(16908310);
        titleView.setContentDescription(this.mContentDescription);
    }
}
