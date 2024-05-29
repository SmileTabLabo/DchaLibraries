package com.android.quicksearchbox;
/* loaded from: a.zip:com/android/quicksearchbox/AbstractSuggestionExtras.class */
public abstract class AbstractSuggestionExtras implements SuggestionExtras {
    private final SuggestionExtras mMore;

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractSuggestionExtras(SuggestionExtras suggestionExtras) {
        this.mMore = suggestionExtras;
    }

    protected abstract String doGetExtra(String str);

    @Override // com.android.quicksearchbox.SuggestionExtras
    public String getExtra(String str) {
        String doGetExtra = doGetExtra(str);
        String str2 = doGetExtra;
        if (doGetExtra == null) {
            str2 = doGetExtra;
            if (this.mMore != null) {
                str2 = this.mMore.getExtra(str);
            }
        }
        return str2;
    }
}
