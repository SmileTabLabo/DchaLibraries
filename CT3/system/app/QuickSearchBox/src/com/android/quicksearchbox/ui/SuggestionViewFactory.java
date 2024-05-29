package com.android.quicksearchbox.ui;

import android.view.View;
import android.view.ViewGroup;
import com.android.quicksearchbox.Suggestion;
import com.android.quicksearchbox.SuggestionCursor;
import java.util.Collection;
/* loaded from: a.zip:com/android/quicksearchbox/ui/SuggestionViewFactory.class */
public interface SuggestionViewFactory {
    boolean canCreateView(Suggestion suggestion);

    Collection<String> getSuggestionViewTypes();

    View getView(SuggestionCursor suggestionCursor, String str, View view, ViewGroup viewGroup);

    String getViewType(Suggestion suggestion);
}
