package com.android.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
/* loaded from: b.zip:com/android/browser/BookmarkItem.class */
class BookmarkItem extends HorizontalScrollView {
    protected boolean mEnableScrolling;
    protected ImageView mImageView;
    protected TextView mTextView;
    protected String mTitle;
    protected String mUrl;
    protected TextView mUrlText;

    /* JADX INFO: Access modifiers changed from: package-private */
    public BookmarkItem(Context context) {
        super(context);
        this.mEnableScrolling = false;
        setClickable(false);
        setEnableScrolling(false);
        LayoutInflater.from(context).inflate(2130968604, this);
        this.mTextView = (TextView) findViewById(2131558407);
        this.mUrlText = (TextView) findViewById(2131558408);
        this.mImageView = (ImageView) findViewById(2131558406);
        findViewById(2131558487).setVisibility(8);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getName() {
        return this.mTitle;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getUrl() {
        return this.mUrl;
    }

    @Override // android.widget.HorizontalScrollView, android.view.ViewGroup
    protected void measureChild(View view, int i, int i2) {
        if (this.mEnableScrolling) {
            super.measureChild(view, i, i2);
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        view.measure(getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight, layoutParams.width), getChildMeasureSpec(i2, this.mPaddingTop + this.mPaddingBottom, layoutParams.height));
    }

    @Override // android.widget.HorizontalScrollView, android.view.ViewGroup
    protected void measureChildWithMargins(View view, int i, int i2, int i3, int i4) {
        if (this.mEnableScrolling) {
            super.measureChildWithMargins(view, i, i2, i3, i4);
            return;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        view.measure(getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin + i2, marginLayoutParams.width), getChildMeasureSpec(i3, this.mPaddingTop + this.mPaddingBottom + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + i4, marginLayoutParams.height));
    }

    @Override // android.widget.HorizontalScrollView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mEnableScrolling) {
            return super.onTouchEvent(motionEvent);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setEnableScrolling(boolean z) {
        this.mEnableScrolling = z;
        setFocusable(this.mEnableScrolling);
        setFocusableInTouchMode(this.mEnableScrolling);
        requestDisallowInterceptTouchEvent(!this.mEnableScrolling);
        requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFavicon(Bitmap bitmap) {
        if (bitmap != null) {
            this.mImageView.setImageBitmap(bitmap);
        } else {
            this.mImageView.setImageResource(2130837505);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFaviconBackground(Drawable drawable) {
        this.mImageView.setBackgroundDrawable(drawable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setName(String str) {
        if (str == null) {
            return;
        }
        this.mTitle = str;
        String str2 = str;
        if (str.length() > 80) {
            str2 = str.substring(0, 80);
        }
        this.mTextView.setText(str2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setUrl(String str) {
        if (str == null) {
            return;
        }
        this.mUrl = str;
        String stripUrl = UrlUtils.stripUrl(str);
        String str2 = stripUrl;
        if (stripUrl.length() > 80) {
            str2 = stripUrl.substring(0, 80);
        }
        this.mUrlText.setText(str2);
    }
}
