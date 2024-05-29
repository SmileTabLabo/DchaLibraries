package com.android.quicksearchbox;

import com.android.quicksearchbox.util.QuietlyCloseable;
import java.util.Collection;
/* loaded from: a.zip:com/android/quicksearchbox/SuggestionCursor.class */
public interface SuggestionCursor extends Suggestion, QuietlyCloseable {
    void close();

    int getCount();

    Collection<String> getExtraColumns();

    String getUserQuery();

    void moveTo(int i);
}
