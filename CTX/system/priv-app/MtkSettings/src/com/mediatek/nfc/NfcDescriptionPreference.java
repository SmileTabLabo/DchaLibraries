package com.mediatek.nfc;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
/* compiled from: NfcSettings.java */
/* loaded from: classes.dex */
class NfcDescriptionPreference extends Preference {
    public NfcDescriptionPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public NfcDescriptionPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        TextView textView = (TextView) preferenceViewHolder.findViewById(16908310);
        if (textView != null) {
            textView.setSingleLine(false);
            textView.setMaxLines(3);
        }
    }
}
