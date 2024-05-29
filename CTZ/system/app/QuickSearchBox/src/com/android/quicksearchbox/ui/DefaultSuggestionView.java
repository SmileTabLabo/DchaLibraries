package com.android.quicksearchbox.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.quicksearchbox.R;
import com.android.quicksearchbox.Source;
import com.android.quicksearchbox.Suggestion;
import com.android.quicksearchbox.util.Consumer;
import com.android.quicksearchbox.util.NowOrLater;
/* loaded from: classes.dex */
public class DefaultSuggestionView extends BaseSuggestionView {
    private final String TAG;
    private AsyncIcon mAsyncIcon1;
    private AsyncIcon mAsyncIcon2;

    public DefaultSuggestionView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.TAG = "QSB.DefaultSuggestionView";
    }

    public DefaultSuggestionView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.TAG = "QSB.DefaultSuggestionView";
    }

    public DefaultSuggestionView(Context context) {
        super(context);
        this.TAG = "QSB.DefaultSuggestionView";
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.quicksearchbox.ui.BaseSuggestionView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mText1 = (TextView) findViewById(R.id.text1);
        this.mText2 = (TextView) findViewById(R.id.text2);
        this.mAsyncIcon1 = new AsyncIcon(this.mIcon1) { // from class: com.android.quicksearchbox.ui.DefaultSuggestionView.1
            @Override // com.android.quicksearchbox.ui.DefaultSuggestionView.AsyncIcon
            protected String getFallbackIconId(Source source) {
                return source.getSourceIconUri().toString();
            }

            @Override // com.android.quicksearchbox.ui.DefaultSuggestionView.AsyncIcon
            protected Drawable getFallbackIcon(Source source) {
                return source.getSourceIcon();
            }
        };
        this.mAsyncIcon2 = new AsyncIcon(this.mIcon2);
    }

    @Override // com.android.quicksearchbox.ui.BaseSuggestionView, com.android.quicksearchbox.ui.SuggestionView
    public void bindAsSuggestion(Suggestion suggestion, String str) {
        CharSequence formatText;
        super.bindAsSuggestion(suggestion, str);
        CharSequence formatText2 = formatText(suggestion.getSuggestionText1(), suggestion);
        String suggestionText2Url = suggestion.getSuggestionText2Url();
        if (suggestionText2Url != null) {
            formatText = formatUrl(suggestionText2Url);
        } else {
            formatText = formatText(suggestion.getSuggestionText2(), suggestion);
        }
        if (TextUtils.isEmpty(formatText)) {
            this.mText1.setSingleLine(false);
            this.mText1.setMaxLines(2);
            this.mText1.setEllipsize(TextUtils.TruncateAt.START);
        } else {
            this.mText1.setSingleLine(true);
            this.mText1.setMaxLines(1);
            this.mText1.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }
        setText1(formatText2);
        setText2(formatText);
        this.mAsyncIcon1.set(suggestion.getSuggestionSource(), suggestion.getSuggestionIcon1());
        this.mAsyncIcon2.set(suggestion.getSuggestionSource(), suggestion.getSuggestionIcon2());
    }

    private CharSequence formatUrl(CharSequence charSequence) {
        SpannableString spannableString = new SpannableString(charSequence);
        spannableString.setSpan(new TextAppearanceSpan(null, 0, 0, getResources().getColorStateList(R.color.url_text), null), 0, charSequence.length(), 33);
        return spannableString;
    }

    private CharSequence formatText(String str, Suggestion suggestion) {
        if ("html".equals(suggestion.getSuggestionFormat()) && looksLikeHtml(str)) {
            return Html.fromHtml(str);
        }
        return str;
    }

    private boolean looksLikeHtml(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        for (int length = str.length() - 1; length >= 0; length--) {
            char charAt = str.charAt(length);
            if (charAt == '>' || charAt == '&') {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void setViewDrawable(ImageView imageView, Drawable drawable) {
        imageView.setImageDrawable(drawable);
        if (drawable == null) {
            imageView.setVisibility(8);
            return;
        }
        imageView.setVisibility(0);
        drawable.setVisible(false, false);
        drawable.setVisible(true, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AsyncIcon {
        private String mCurrentId;
        private final ImageView mView;
        private String mWantedId;

        public AsyncIcon(ImageView imageView) {
            this.mView = imageView;
        }

        public void set(final Source source, String str) {
            if (str != null) {
                Uri iconUri = source.getIconUri(str);
                final String uri = iconUri != null ? iconUri.toString() : null;
                this.mWantedId = uri;
                if (!TextUtils.equals(this.mWantedId, this.mCurrentId)) {
                    NowOrLater<Drawable> icon = source.getIcon(str);
                    if (icon.haveNow()) {
                        handleNewDrawable(icon.getNow(), uri, source);
                        return;
                    }
                    clearDrawable();
                    icon.getLater(new Consumer<Drawable>() { // from class: com.android.quicksearchbox.ui.DefaultSuggestionView.AsyncIcon.1
                        @Override // com.android.quicksearchbox.util.Consumer
                        public boolean consume(Drawable drawable) {
                            if (TextUtils.equals(uri, AsyncIcon.this.mWantedId)) {
                                AsyncIcon.this.handleNewDrawable(drawable, uri, source);
                                return true;
                            }
                            return false;
                        }
                    });
                    return;
                }
                return;
            }
            this.mWantedId = null;
            handleNewDrawable(null, null, source);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleNewDrawable(Drawable drawable, String str, Source source) {
            if (drawable == null) {
                this.mWantedId = getFallbackIconId(source);
                if (TextUtils.equals(this.mWantedId, this.mCurrentId)) {
                    return;
                }
                drawable = getFallbackIcon(source);
            }
            setDrawable(drawable, str);
        }

        private void setDrawable(Drawable drawable, String str) {
            this.mCurrentId = str;
            DefaultSuggestionView.setViewDrawable(this.mView, drawable);
        }

        private void clearDrawable() {
            this.mCurrentId = null;
            this.mView.setImageDrawable(null);
        }

        protected String getFallbackIconId(Source source) {
            return null;
        }

        protected Drawable getFallbackIcon(Source source) {
            return null;
        }
    }

    /* loaded from: classes.dex */
    public static class Factory extends SuggestionViewInflater {
        public Factory(Context context) {
            super("default", DefaultSuggestionView.class, R.layout.suggestion, context);
        }
    }
}
