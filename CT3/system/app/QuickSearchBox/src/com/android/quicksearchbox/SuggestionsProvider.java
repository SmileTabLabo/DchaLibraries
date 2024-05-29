package com.android.quicksearchbox;
/* loaded from: a.zip:com/android/quicksearchbox/SuggestionsProvider.class */
public interface SuggestionsProvider {
    void close();

    Suggestions getSuggestions(String str, Source source);
}
