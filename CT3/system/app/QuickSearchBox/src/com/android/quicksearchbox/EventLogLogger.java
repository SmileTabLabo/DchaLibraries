package com.android.quicksearchbox;

import android.content.Context;
import java.util.Random;
/* loaded from: a.zip:com/android/quicksearchbox/EventLogLogger.class */
public class EventLogLogger implements Logger {
    private final Config mConfig;
    private final Context mContext;
    private final String mPackageName;
    private final Random mRandom = new Random();

    public EventLogLogger(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
        this.mPackageName = this.mContext.getPackageName();
    }

    private String getSuggestions(SuggestionCursor suggestionCursor) {
        StringBuilder sb = new StringBuilder();
        int count = suggestionCursor == null ? 0 : suggestionCursor.getCount();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append('|');
            }
            suggestionCursor.moveTo(i);
            String name = suggestionCursor.getSuggestionSource().getName();
            String suggestionLogType = suggestionCursor.getSuggestionLogType();
            String str = suggestionLogType;
            if (suggestionLogType == null) {
                str = "";
            }
            sb.append(name).append(':').append(str).append(':').append(suggestionCursor.isSuggestionShortcut() ? "shortcut" : "");
        }
        return sb.toString();
    }

    protected Context getContext() {
        return this.mContext;
    }

    protected int getVersionCode() {
        return QsbApplication.get(getContext()).getVersionCode();
    }

    @Override // com.android.quicksearchbox.Logger
    public void logExit(SuggestionCursor suggestionCursor, int i) {
        EventLogTags.writeQsbExit(getSuggestions(suggestionCursor), i);
    }

    @Override // com.android.quicksearchbox.Logger
    public void logLatency(SourceResult sourceResult) {
    }

    @Override // com.android.quicksearchbox.Logger
    public void logSearch(int i, int i2) {
        EventLogTags.writeQsbSearch(null, i, i2);
    }

    @Override // com.android.quicksearchbox.Logger
    public void logStart(int i, int i2, String str) {
        EventLogTags.writeQsbStart(this.mPackageName, getVersionCode(), str, i2, null, null, i);
    }

    @Override // com.android.quicksearchbox.Logger
    public void logSuggestionClick(long j, SuggestionCursor suggestionCursor, int i) {
        EventLogTags.writeQsbClick(j, getSuggestions(suggestionCursor), null, suggestionCursor.getUserQuery().length(), i);
    }

    @Override // com.android.quicksearchbox.Logger
    public void logVoiceSearch() {
        EventLogTags.writeQsbVoiceSearch(null);
    }
}
