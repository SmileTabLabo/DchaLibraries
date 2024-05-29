package com.android.quicksearchbox.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;
/* loaded from: a.zip:com/android/quicksearchbox/ui/SuggestionsView.class */
public class SuggestionsView extends ListView implements SuggestionsListView<ListAdapter> {
    private SuggestionsAdapter<ListAdapter> mSuggestionsAdapter;

    public SuggestionsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsListView
    public SuggestionsAdapter<ListAdapter> getSuggestionsAdapter() {
        return this.mSuggestionsAdapter;
    }

    @Override // android.widget.ListView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        setItemsCanFocus(true);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsListView
    public void setSuggestionsAdapter(SuggestionsAdapter<ListAdapter> suggestionsAdapter) {
        ListAdapter listAdapter = null;
        if (suggestionsAdapter != null) {
            listAdapter = suggestionsAdapter.getListAdapter();
        }
        super.setAdapter(listAdapter);
        this.mSuggestionsAdapter = suggestionsAdapter;
    }
}
