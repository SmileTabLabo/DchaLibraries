package com.android.quicksearchbox.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.quicksearchbox.R;
import com.android.quicksearchbox.Suggestion;
/* loaded from: classes.dex */
public abstract class BaseSuggestionView extends RelativeLayout implements SuggestionView {
    private SuggestionsAdapter<?> mAdapter;
    protected ImageView mIcon1;
    protected ImageView mIcon2;
    private long mSuggestionId;
    protected TextView mText1;
    protected TextView mText2;

    public BaseSuggestionView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public BaseSuggestionView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BaseSuggestionView(Context context) {
        super(context);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mText1 = (TextView) findViewById(R.id.text1);
        this.mText2 = (TextView) findViewById(R.id.text2);
        this.mIcon1 = (ImageView) findViewById(R.id.icon1);
        this.mIcon2 = (ImageView) findViewById(R.id.icon2);
    }

    @Override // com.android.quicksearchbox.ui.SuggestionView
    public void bindAsSuggestion(Suggestion suggestion, String str) {
        setOnClickListener(new ClickListener());
    }

    @Override // com.android.quicksearchbox.ui.SuggestionView
    public void bindAdapter(SuggestionsAdapter<?> suggestionsAdapter, long j) {
        this.mAdapter = suggestionsAdapter;
        this.mSuggestionId = j;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setText1(CharSequence charSequence) {
        this.mText1.setText(charSequence);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setText2(CharSequence charSequence) {
        this.mText2.setText(charSequence);
        if (TextUtils.isEmpty(charSequence)) {
            this.mText2.setVisibility(8);
        } else {
            this.mText2.setVisibility(0);
        }
    }

    protected void onSuggestionClicked() {
        if (this.mAdapter != null) {
            this.mAdapter.onSuggestionClicked(this.mSuggestionId);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onSuggestionQueryRefineClicked() {
        if (this.mAdapter != null) {
            this.mAdapter.onSuggestionQueryRefineClicked(this.mSuggestionId);
        }
    }

    /* loaded from: classes.dex */
    private class ClickListener implements View.OnClickListener {
        private ClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            BaseSuggestionView.this.onSuggestionClicked();
        }
    }
}
