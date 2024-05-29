package com.android.browser;

import android.content.Context;
import android.widget.CompoundButton;
/* loaded from: classes.dex */
class HistoryItem extends BookmarkItem implements CompoundButton.OnCheckedChangeListener {
    private CompoundButton mStar;

    /* JADX INFO: Access modifiers changed from: package-private */
    public HistoryItem(Context context) {
        this(context, true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HistoryItem(Context context, boolean z) {
        super(context);
        this.mStar = (CompoundButton) findViewById(R.id.star);
        this.mStar.setOnCheckedChangeListener(this);
        if (z) {
            this.mStar.setVisibility(0);
        } else {
            this.mStar.setVisibility(8);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void copyTo(HistoryItem historyItem) {
        historyItem.mTextView.setText(this.mTextView.getText());
        historyItem.mUrlText.setText(this.mUrlText.getText());
        historyItem.setIsBookmark(this.mStar.isChecked());
        historyItem.mImageView.setImageDrawable(this.mImageView.getDrawable());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isBookmark() {
        return this.mStar.isChecked();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setIsBookmark(boolean z) {
        this.mStar.setOnCheckedChangeListener(null);
        this.mStar.setChecked(z);
        this.mStar.setOnCheckedChangeListener(this);
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        if (z) {
            setIsBookmark(false);
            com.android.browser.provider.Browser.saveBookmark(getContext(), getName(), this.mUrl);
            return;
        }
        Bookmarks.removeFromBookmarks(getContext(), getContext().getContentResolver(), this.mUrl, getName());
    }
}
