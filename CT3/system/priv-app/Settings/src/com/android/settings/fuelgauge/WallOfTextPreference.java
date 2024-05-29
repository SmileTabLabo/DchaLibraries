package com.android.settings.fuelgauge;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.DividerPreference;
/* loaded from: classes.dex */
public class WallOfTextPreference extends DividerPreference {
    public WallOfTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // com.android.settings.DividerPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView summary = (TextView) view.findViewById(16908304);
        summary.setMaxLines(20);
    }
}
