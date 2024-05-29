package com.android.quicksearchbox.ui;

import android.database.DataSetObserver;
import android.view.View;
import com.android.quicksearchbox.SourceResult;
import com.android.quicksearchbox.SuggestionPosition;
import com.android.quicksearchbox.Suggestions;
/* loaded from: a.zip:com/android/quicksearchbox/ui/DelayingSuggestionsAdapter.class */
public class DelayingSuggestionsAdapter<A> implements SuggestionsAdapter<A> {
    private final SuggestionsAdapterBase<A> mDelayedAdapter;
    private DataSetObserver mPendingDataSetObserver;
    private Suggestions mPendingSuggestions;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/quicksearchbox/ui/DelayingSuggestionsAdapter$PendingSuggestionsObserver.class */
    public class PendingSuggestionsObserver extends DataSetObserver {
        final DelayingSuggestionsAdapter this$0;

        private PendingSuggestionsObserver(DelayingSuggestionsAdapter delayingSuggestionsAdapter) {
            this.this$0 = delayingSuggestionsAdapter;
        }

        /* synthetic */ PendingSuggestionsObserver(DelayingSuggestionsAdapter delayingSuggestionsAdapter, PendingSuggestionsObserver pendingSuggestionsObserver) {
            this(delayingSuggestionsAdapter);
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            this.this$0.onPendingSuggestionsChanged();
        }
    }

    public DelayingSuggestionsAdapter(SuggestionsAdapterBase<A> suggestionsAdapterBase) {
        this.mDelayedAdapter = suggestionsAdapterBase;
    }

    private void setPendingSuggestions(Suggestions suggestions) {
        if (this.mPendingSuggestions == suggestions) {
            return;
        }
        if (this.mDelayedAdapter.isClosed()) {
            if (suggestions != null) {
                suggestions.release();
                return;
            }
            return;
        }
        if (this.mPendingDataSetObserver == null) {
            this.mPendingDataSetObserver = new PendingSuggestionsObserver(this, null);
        }
        if (this.mPendingSuggestions != null) {
            this.mPendingSuggestions.unregisterDataSetObserver(this.mPendingDataSetObserver);
            if (this.mPendingSuggestions != getSuggestions()) {
                this.mPendingSuggestions.release();
            }
        }
        this.mPendingSuggestions = suggestions;
        if (this.mPendingSuggestions != null) {
            this.mPendingSuggestions.registerDataSetObserver(this.mPendingDataSetObserver);
        }
    }

    private boolean shouldPublish(Suggestions suggestions) {
        if (suggestions.isDone()) {
            return true;
        }
        SourceResult result = suggestions.getResult();
        return result != null && result.getCount() > 0;
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public A getListAdapter() {
        return this.mDelayedAdapter.getListAdapter();
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public SuggestionPosition getSuggestion(long j) {
        return this.mDelayedAdapter.getSuggestion(j);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public Suggestions getSuggestions() {
        return this.mDelayedAdapter.getSuggestions();
    }

    protected void onPendingSuggestionsChanged() {
        if (shouldPublish(this.mPendingSuggestions)) {
            this.mDelayedAdapter.setSuggestions(this.mPendingSuggestions);
            setPendingSuggestions(null);
        }
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void onSuggestionClicked(long j) {
        this.mDelayedAdapter.onSuggestionClicked(j);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void onSuggestionQueryRefineClicked(long j) {
        this.mDelayedAdapter.onSuggestionQueryRefineClicked(j);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void setOnFocusChangeListener(View.OnFocusChangeListener onFocusChangeListener) {
        this.mDelayedAdapter.setOnFocusChangeListener(onFocusChangeListener);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void setSuggestionClickListener(SuggestionClickListener suggestionClickListener) {
        this.mDelayedAdapter.setSuggestionClickListener(suggestionClickListener);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void setSuggestions(Suggestions suggestions) {
        if (suggestions == null) {
            this.mDelayedAdapter.setSuggestions(null);
            setPendingSuggestions(null);
        } else if (!shouldPublish(suggestions)) {
            setPendingSuggestions(suggestions);
        } else {
            this.mDelayedAdapter.setSuggestions(suggestions);
            setPendingSuggestions(null);
        }
    }
}
