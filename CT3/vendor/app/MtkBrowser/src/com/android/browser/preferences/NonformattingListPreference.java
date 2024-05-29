package com.android.browser.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
/* loaded from: b.zip:com/android/browser/preferences/NonformattingListPreference.class */
public class NonformattingListPreference extends ListPreference {
    private CharSequence mSummary;

    public NonformattingListPreference(Context context) {
        super(context);
    }

    public NonformattingListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.preference.ListPreference, android.preference.Preference
    public CharSequence getSummary() {
        return this.mSummary != null ? this.mSummary : super.getSummary();
    }

    @Override // android.preference.ListPreference, android.preference.Preference
    public void setSummary(CharSequence charSequence) {
        this.mSummary = charSequence;
        super.setSummary(charSequence);
    }
}
