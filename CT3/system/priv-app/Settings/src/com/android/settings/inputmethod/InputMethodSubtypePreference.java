package com.android.settings.inputmethod;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.inputmethod.InputMethodUtils;
import java.text.Collator;
import java.util.Locale;
/* loaded from: classes.dex */
class InputMethodSubtypePreference extends SwitchWithNoTextPreference {
    private final boolean mIsSystemLanguage;
    private final boolean mIsSystemLocale;

    /* JADX INFO: Access modifiers changed from: package-private */
    public InputMethodSubtypePreference(Context context, InputMethodSubtype subtype, InputMethodInfo imi) {
        super(context);
        setPersistent(false);
        setKey(imi.getId() + subtype.hashCode());
        CharSequence subtypeLabel = InputMethodAndSubtypeUtil.getSubtypeLocaleNameAsSentence(subtype, context, imi);
        setTitle(subtypeLabel);
        String subtypeLocaleString = subtype.getLocale();
        if (TextUtils.isEmpty(subtypeLocaleString)) {
            this.mIsSystemLocale = false;
            this.mIsSystemLanguage = false;
            return;
        }
        Locale systemLocale = context.getResources().getConfiguration().locale;
        this.mIsSystemLocale = subtypeLocaleString.equals(systemLocale.toString());
        this.mIsSystemLanguage = !this.mIsSystemLocale ? InputMethodUtils.getLanguageFromLocaleString(subtypeLocaleString).equals(systemLocale.getLanguage()) : true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int compareTo(Preference rhs, Collator collator) {
        if (this == rhs) {
            return 0;
        }
        if (rhs instanceof InputMethodSubtypePreference) {
            InputMethodSubtypePreference pref = (InputMethodSubtypePreference) rhs;
            CharSequence t0 = getTitle();
            CharSequence t1 = rhs.getTitle();
            if (TextUtils.equals(t0, t1)) {
                return 0;
            }
            if (this.mIsSystemLocale) {
                return -1;
            }
            if (pref.mIsSystemLocale) {
                return 1;
            }
            if (this.mIsSystemLanguage) {
                return -1;
            }
            if (pref.mIsSystemLanguage || TextUtils.isEmpty(t0)) {
                return 1;
            }
            if (TextUtils.isEmpty(t1)) {
                return -1;
            }
            return collator.compare(t0.toString(), t1.toString());
        }
        return super.compareTo(rhs);
    }
}
