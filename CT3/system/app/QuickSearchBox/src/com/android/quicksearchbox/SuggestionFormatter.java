package com.android.quicksearchbox;

import android.text.Spannable;
/* loaded from: a.zip:com/android/quicksearchbox/SuggestionFormatter.class */
public abstract class SuggestionFormatter {
    private final TextAppearanceFactory mSpanFactory;

    /* JADX INFO: Access modifiers changed from: protected */
    public SuggestionFormatter(TextAppearanceFactory textAppearanceFactory) {
        this.mSpanFactory = textAppearanceFactory;
    }

    private void setSpans(Spannable spannable, int i, int i2, Object[] objArr) {
        for (Object obj : objArr) {
            spannable.setSpan(obj, i, i2, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void applyQueryTextStyle(Spannable spannable, int i, int i2) {
        if (i == i2) {
            return;
        }
        setSpans(spannable, i, i2, this.mSpanFactory.createSuggestionQueryTextAppearance());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void applySuggestedTextStyle(Spannable spannable, int i, int i2) {
        if (i == i2) {
            return;
        }
        setSpans(spannable, i, i2, this.mSpanFactory.createSuggestionSuggestedTextAppearance());
    }

    public abstract CharSequence formatSuggestion(String str, String str2);
}
