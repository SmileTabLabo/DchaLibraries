package com.android.quicksearchbox.ui;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.quicksearchbox.SourceResult;
import com.android.quicksearchbox.Suggestion;
import com.android.quicksearchbox.SuggestionCursor;
import com.android.quicksearchbox.SuggestionPosition;
import com.android.quicksearchbox.Suggestions;
import java.util.HashMap;
/* loaded from: classes.dex */
public abstract class SuggestionsAdapterBase<A> implements SuggestionsAdapter<A> {
    private SuggestionCursor mCurrentSuggestions;
    private DataSetObserver mDataSetObserver;
    private View.OnFocusChangeListener mOnFocusChangeListener;
    private SuggestionClickListener mSuggestionClickListener;
    private Suggestions mSuggestions;
    private final SuggestionViewFactory mViewFactory;
    private boolean mClosed = false;
    private final HashMap<String, Integer> mViewTypeMap = new HashMap<>();

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public abstract A getListAdapter();

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public abstract SuggestionPosition getSuggestion(long j);

    protected abstract void notifyDataSetChanged();

    protected abstract void notifyDataSetInvalidated();

    /* JADX INFO: Access modifiers changed from: protected */
    public SuggestionsAdapterBase(SuggestionViewFactory suggestionViewFactory) {
        this.mViewFactory = suggestionViewFactory;
        for (String str : this.mViewFactory.getSuggestionViewTypes()) {
            if (!this.mViewTypeMap.containsKey(str)) {
                this.mViewTypeMap.put(str, Integer.valueOf(this.mViewTypeMap.size()));
            }
        }
    }

    public boolean isClosed() {
        return this.mClosed;
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void setSuggestionClickListener(SuggestionClickListener suggestionClickListener) {
        this.mSuggestionClickListener = suggestionClickListener;
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void setOnFocusChangeListener(View.OnFocusChangeListener onFocusChangeListener) {
        this.mOnFocusChangeListener = onFocusChangeListener;
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void setSuggestions(Suggestions suggestions) {
        if (this.mSuggestions == suggestions) {
            return;
        }
        if (this.mClosed) {
            if (suggestions != null) {
                suggestions.release();
                return;
            }
            return;
        }
        if (this.mDataSetObserver == null) {
            this.mDataSetObserver = new MySuggestionsObserver();
        }
        if (this.mSuggestions != null) {
            this.mSuggestions.unregisterDataSetObserver(this.mDataSetObserver);
            this.mSuggestions.release();
        }
        this.mSuggestions = suggestions;
        if (this.mSuggestions != null) {
            this.mSuggestions.registerDataSetObserver(this.mDataSetObserver);
        }
        onSuggestionsChanged();
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public Suggestions getSuggestions() {
        return this.mSuggestions;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public SuggestionPosition getSuggestion(int i) {
        if (this.mCurrentSuggestions == null) {
            return null;
        }
        return new SuggestionPosition(this.mCurrentSuggestions, i);
    }

    private String suggestionViewType(Suggestion suggestion) {
        String viewType = this.mViewFactory.getViewType(suggestion);
        if (!this.mViewTypeMap.containsKey(viewType)) {
            throw new IllegalStateException("Unknown viewType " + viewType);
        }
        return viewType;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getSuggestionViewType(SuggestionCursor suggestionCursor, int i) {
        if (suggestionCursor == null) {
            return 0;
        }
        suggestionCursor.moveTo(i);
        return this.mViewTypeMap.get(suggestionViewType(suggestionCursor)).intValue();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getSuggestionViewTypeCount() {
        return this.mViewTypeMap.size();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public View getView(SuggestionCursor suggestionCursor, int i, long j, View view, ViewGroup viewGroup) {
        suggestionCursor.moveTo(i);
        View view2 = this.mViewFactory.getView(suggestionCursor, suggestionCursor.getUserQuery(), view, viewGroup);
        if (view2 instanceof SuggestionView) {
            ((SuggestionView) view2).bindAdapter(this, j);
        } else {
            view2.setOnClickListener(new SuggestionViewClickListener(j));
        }
        if (this.mOnFocusChangeListener != null) {
            view2.setOnFocusChangeListener(this.mOnFocusChangeListener);
        }
        return view2;
    }

    protected void onSuggestionsChanged() {
        SourceResult sourceResult;
        if (this.mSuggestions != null) {
            sourceResult = this.mSuggestions.getResult();
        } else {
            sourceResult = null;
        }
        changeSuggestions(sourceResult);
    }

    public SuggestionCursor getCurrentSuggestions() {
        return this.mCurrentSuggestions;
    }

    private void changeSuggestions(SuggestionCursor suggestionCursor) {
        if (suggestionCursor == this.mCurrentSuggestions) {
            if (suggestionCursor != null) {
                notifyDataSetChanged();
                return;
            }
            return;
        }
        this.mCurrentSuggestions = suggestionCursor;
        if (this.mCurrentSuggestions != null) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void onSuggestionClicked(long j) {
        if (this.mClosed) {
            Log.w("QSB.SuggestionsAdapter", "onSuggestionClicked after close");
        } else if (this.mSuggestionClickListener != null) {
            this.mSuggestionClickListener.onSuggestionClicked(this, j);
        }
    }

    @Override // com.android.quicksearchbox.ui.SuggestionsAdapter
    public void onSuggestionQueryRefineClicked(long j) {
        if (this.mClosed) {
            Log.w("QSB.SuggestionsAdapter", "onSuggestionQueryRefineClicked after close");
        } else if (this.mSuggestionClickListener != null) {
            this.mSuggestionClickListener.onSuggestionQueryRefineClicked(this, j);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MySuggestionsObserver extends DataSetObserver {
        private MySuggestionsObserver() {
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            SuggestionsAdapterBase.this.onSuggestionsChanged();
        }
    }

    /* loaded from: classes.dex */
    private class SuggestionViewClickListener implements View.OnClickListener {
        private final long mSuggestionId;

        public SuggestionViewClickListener(long j) {
            this.mSuggestionId = j;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            SuggestionsAdapterBase.this.onSuggestionClicked(this.mSuggestionId);
        }
    }
}
