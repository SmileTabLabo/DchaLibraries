package com.android.quicksearchbox;

import com.android.quicksearchbox.SuggestionCursor;
/* loaded from: a.zip:com/android/quicksearchbox/SuggestionCursorProvider.class */
public interface SuggestionCursorProvider<C extends SuggestionCursor> {
    String getName();

    C getSuggestions(String str, int i);
}
