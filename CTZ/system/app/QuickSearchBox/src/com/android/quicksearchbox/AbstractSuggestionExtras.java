package com.android.quicksearchbox;
/* loaded from: classes.dex */
public abstract class AbstractSuggestionExtras implements SuggestionExtras {
    private final SuggestionExtras mMore;

    protected abstract String doGetExtra(String str);

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractSuggestionExtras(SuggestionExtras suggestionExtras) {
        this.mMore = suggestionExtras;
    }

    @Override // com.android.quicksearchbox.SuggestionExtras
    public String getExtra(String str) {
        String doGetExtra = doGetExtra(str);
        if (doGetExtra == null && this.mMore != null) {
            return this.mMore.getExtra(str);
        }
        return doGetExtra;
    }
}
