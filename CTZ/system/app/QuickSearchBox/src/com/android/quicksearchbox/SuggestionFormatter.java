package com.android.quicksearchbox;

import android.text.Spannable;
/* loaded from: classes.dex */
public abstract class SuggestionFormatter {
    private final TextAppearanceFactory mSpanFactory;

    public abstract CharSequence formatSuggestion(String str, String str2);

    /* JADX INFO: Access modifiers changed from: protected */
    public SuggestionFormatter(TextAppearanceFactory textAppearanceFactory) {
        this.mSpanFactory = textAppearanceFactory;
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

    private void setSpans(Spannable spannable, int i, int i2, Object[] objArr) {
        for (Object obj : objArr) {
            spannable.setSpan(obj, i, i2, 0);
        }
    }
}
