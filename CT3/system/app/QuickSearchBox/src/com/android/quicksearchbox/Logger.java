package com.android.quicksearchbox;
/* loaded from: a.zip:com/android/quicksearchbox/Logger.class */
public interface Logger {
    void logExit(SuggestionCursor suggestionCursor, int i);

    void logLatency(SourceResult sourceResult);

    void logSearch(int i, int i2);

    void logStart(int i, int i2, String str);

    void logSuggestionClick(long j, SuggestionCursor suggestionCursor, int i);

    void logVoiceSearch();
}
