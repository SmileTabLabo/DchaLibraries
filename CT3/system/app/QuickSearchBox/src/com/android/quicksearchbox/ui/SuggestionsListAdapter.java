package com.android.quicksearchbox.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import com.android.quicksearchbox.SuggestionCursor;
import com.android.quicksearchbox.SuggestionPosition;
/* loaded from: a.zip:com/android/quicksearchbox/ui/SuggestionsListAdapter.class */
public class SuggestionsListAdapter extends SuggestionsAdapterBase<ListAdapter> {
    private Adapter mAdapter;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/quicksearchbox/ui/SuggestionsListAdapter$Adapter.class */
    public class Adapter extends BaseAdapter {
        final SuggestionsListAdapter this$0;

        Adapter(SuggestionsListAdapter suggestionsListAdapter) {
            this.this$0 = suggestionsListAdapter;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            SuggestionCursor currentSuggestions = this.this$0.getCurrentSuggestions();
            return currentSuggestions == null ? 0 : currentSuggestions.getCount();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return this.this$0.getSuggestion(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public int getItemViewType(int i) {
            return this.this$0.getSuggestionViewType(this.this$0.getCurrentSuggestions(), i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            return this.this$0.getView(this.this$0.getCurrentSuggestions(), i, i, view, viewGroup);
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public int getViewTypeCount() {
            return this.this$0.getSuggestionViewTypeCount();
        }
    }

    public SuggestionsListAdapter(SuggestionViewFactory suggestionViewFactory) {
        super(suggestionViewFactory);
        this.mAdapter = new Adapter(this);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapterBase, com.android.quicksearchbox.ui.SuggestionsAdapter
    public BaseAdapter getListAdapter() {
        return this.mAdapter;
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapterBase, com.android.quicksearchbox.ui.SuggestionsAdapter
    public SuggestionPosition getSuggestion(long j) {
        return new SuggestionPosition(getCurrentSuggestions(), (int) j);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapterBase
    public void notifyDataSetChanged() {
        this.mAdapter.notifyDataSetChanged();
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapterBase
    public void notifyDataSetInvalidated() {
        this.mAdapter.notifyDataSetInvalidated();
    }
}
