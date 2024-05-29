package com.android.quicksearchbox.ui;

import com.android.quicksearchbox.Suggestion;
/* loaded from: a.zip:com/android/quicksearchbox/ui/SuggestionView.class */
public interface SuggestionView {
    void bindAdapter(SuggestionsAdapter<?> suggestionsAdapter, long j);

    void bindAsSuggestion(Suggestion suggestion, String str);
}
