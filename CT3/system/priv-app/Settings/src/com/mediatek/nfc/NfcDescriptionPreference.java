package com.mediatek.nfc;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
/* compiled from: NfcSettings.java */
/* loaded from: classes.dex */
class NfcDescriptionPreference extends Preference {
    public NfcDescriptionPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NfcDescriptionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.findViewById(16908310);
        if (title == null) {
            return;
        }
        title.setSingleLine(false);
        title.setMaxLines(3);
    }
}
