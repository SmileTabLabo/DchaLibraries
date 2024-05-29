package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class DividerPreference extends Preference {
    private Boolean mAllowAbove;
    private Boolean mAllowBelow;

    public DividerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.DividerPreference, 0, 0);
        if (a.hasValue(0)) {
            this.mAllowAbove = Boolean.valueOf(a.getBoolean(0, false));
        }
        if (!a.hasValue(1)) {
            return;
        }
        this.mAllowBelow = Boolean.valueOf(a.getBoolean(1, false));
    }

    public DividerPreference(Context context) {
        this(context, null);
    }

    public void setDividerAllowedAbove(boolean allowed) {
        this.mAllowAbove = Boolean.valueOf(allowed);
        notifyChanged();
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (this.mAllowAbove != null) {
            holder.setDividerAllowedAbove(this.mAllowAbove.booleanValue());
        }
        if (this.mAllowBelow == null) {
            return;
        }
        holder.setDividerAllowedBelow(this.mAllowBelow.booleanValue());
    }
}
