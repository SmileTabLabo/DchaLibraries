package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class DividedCategory extends PreferenceCategory {
    public DividedCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(true);
    }
}
