package com.android.quicksearchbox.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import com.android.quicksearchbox.QsbApplication;
import com.android.quicksearchbox.Suggestion;
import com.android.quicksearchbox.SuggestionFormatter;
/* loaded from: a.zip:com/android/quicksearchbox/ui/WebSearchSuggestionView.class */
public class WebSearchSuggestionView extends BaseSuggestionView {
    private final SuggestionFormatter mSuggestionFormatter;

    /* loaded from: a.zip:com/android/quicksearchbox/ui/WebSearchSuggestionView$Factory.class */
    public static class Factory extends SuggestionViewInflater {
        public Factory(Context context) {
            super("web_search", WebSearchSuggestionView.class, 2130968585, context);
        }

        @Override // com.android.quicksearchbox.ui.SuggestionViewInflater, com.android.quicksearchbox.ui.SuggestionViewFactory
        public boolean canCreateView(Suggestion suggestion) {
            return suggestion.isWebSearchSuggestion();
        }
    }

    /* loaded from: a.zip:com/android/quicksearchbox/ui/WebSearchSuggestionView$KeyListener.class */
    private class KeyListener implements View.OnKeyListener {
        final WebSearchSuggestionView this$0;

        private KeyListener(WebSearchSuggestionView webSearchSuggestionView) {
            this.this$0 = webSearchSuggestionView;
        }

        /* synthetic */ KeyListener(WebSearchSuggestionView webSearchSuggestionView, KeyListener keyListener) {
            this(webSearchSuggestionView);
        }

        @Override // android.view.View.OnKeyListener
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            boolean z = false;
            if (keyEvent.getAction() == 0) {
                if (i != 22 || view == this.this$0.mIcon2) {
                    z = false;
                    if (i == 21) {
                        z = false;
                        if (view == this.this$0.mIcon2) {
                            z = this.this$0.requestFocus();
                        }
                    }
                } else {
                    z = this.this$0.mIcon2.requestFocus();
                }
            }
            return z;
        }
    }

    public WebSearchSuggestionView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSuggestionFormatter = QsbApplication.get(context).getSuggestionFormatter();
    }

    private void setIsHistorySuggestion(boolean z) {
        if (!z) {
            this.mIcon1.setVisibility(4);
            return;
        }
        this.mIcon1.setImageResource(2130837555);
        this.mIcon1.setVisibility(0);
    }

    @Override // com.android.quicksearchbox.ui.BaseSuggestionView, com.android.quicksearchbox.ui.SuggestionView
    public void bindAsSuggestion(Suggestion suggestion, String str) {
        super.bindAsSuggestion(suggestion, str);
        setText1(this.mSuggestionFormatter.formatSuggestion(str, suggestion.getSuggestionText1()));
        setIsHistorySuggestion(suggestion.isHistorySuggestion());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.quicksearchbox.ui.BaseSuggestionView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        KeyListener keyListener = new KeyListener(this, null);
        setOnKeyListener(keyListener);
        this.mIcon2.setOnKeyListener(keyListener);
        this.mIcon2.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.quicksearchbox.ui.WebSearchSuggestionView.1
            final WebSearchSuggestionView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.onSuggestionQueryRefineClicked();
            }
        });
        this.mIcon2.setFocusable(true);
    }
}
