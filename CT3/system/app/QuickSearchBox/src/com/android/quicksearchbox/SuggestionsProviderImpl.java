package com.android.quicksearchbox;

import android.os.Handler;
import android.util.Log;
import com.android.quicksearchbox.util.Consumer;
import com.android.quicksearchbox.util.NamedTaskExecutor;
import com.android.quicksearchbox.util.NoOpConsumer;
/* loaded from: a.zip:com/android/quicksearchbox/SuggestionsProviderImpl.class */
public class SuggestionsProviderImpl implements SuggestionsProvider {
    private final Config mConfig;
    private final Logger mLogger;
    private final Handler mPublishThread;
    private final NamedTaskExecutor mQueryExecutor;

    /* loaded from: a.zip:com/android/quicksearchbox/SuggestionsProviderImpl$SuggestionCursorReceiver.class */
    private class SuggestionCursorReceiver implements Consumer<SourceResult> {
        private final Suggestions mSuggestions;
        final SuggestionsProviderImpl this$0;

        public SuggestionCursorReceiver(SuggestionsProviderImpl suggestionsProviderImpl, Suggestions suggestions) {
            this.this$0 = suggestionsProviderImpl;
            this.mSuggestions = suggestions;
        }

        @Override // com.android.quicksearchbox.util.Consumer
        public boolean consume(SourceResult sourceResult) {
            this.mSuggestions.addResults(sourceResult);
            if (sourceResult == null || this.this$0.mLogger == null) {
                return true;
            }
            this.this$0.mLogger.logLatency(sourceResult);
            return true;
        }
    }

    public SuggestionsProviderImpl(Config config, NamedTaskExecutor namedTaskExecutor, Handler handler, Logger logger) {
        this.mConfig = config;
        this.mQueryExecutor = namedTaskExecutor;
        this.mPublishThread = handler;
        this.mLogger = logger;
    }

    private boolean shouldDisplayResults(String str) {
        return str.length() != 0 || this.mConfig.showSuggestionsForZeroQuery();
    }

    @Override // com.android.quicksearchbox.SuggestionsProvider
    public void close() {
    }

    @Override // com.android.quicksearchbox.SuggestionsProvider
    public Suggestions getSuggestions(String str, Source source) {
        Consumer noOpConsumer;
        Suggestions suggestions = new Suggestions(str, source);
        Log.i("QSB.SuggestionsProviderImpl", "chars:" + str.length() + ",source:" + source);
        if (shouldDisplayResults(str)) {
            noOpConsumer = new SuggestionCursorReceiver(this, suggestions);
        } else {
            noOpConsumer = new NoOpConsumer();
            suggestions.done();
        }
        QueryTask.startQuery(str, this.mConfig.getMaxResultsPerSource(), source, this.mQueryExecutor, this.mPublishThread, noOpConsumer);
        return suggestions;
    }
}
