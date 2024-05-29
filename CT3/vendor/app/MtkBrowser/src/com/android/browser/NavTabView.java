package com.android.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/* loaded from: b.zip:com/android/browser/NavTabView.class */
public class NavTabView extends LinearLayout {
    private View.OnClickListener mClickListener;
    private ImageView mClose;
    private ViewGroup mContent;
    private boolean mHighlighted;
    ImageView mImage;
    private Tab mTab;
    private TextView mTitle;
    private View mTitleBar;

    public NavTabView(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(this.mContext).inflate(2130968609, this);
        this.mContent = (ViewGroup) findViewById(2131558409);
        this.mClose = (ImageView) findViewById(2131558497);
        this.mTitle = (TextView) findViewById(2131558407);
        this.mTitleBar = findViewById(2131558496);
        this.mImage = (ImageView) findViewById(2131558403);
    }

    private void setTitle() {
        if (this.mTab == null) {
            return;
        }
        if (this.mHighlighted) {
            this.mTitle.setText(this.mTab.getUrl());
        } else {
            String title = this.mTab.getTitle();
            String str = title;
            if (title == null) {
                str = this.mTab.getUrl();
            }
            this.mTitle.setText(str);
        }
        if (this.mTab.isSnapshot()) {
            setTitleIcon(2130837559);
        } else if (this.mTab.isPrivateBrowsingEnabled()) {
            setTitleIcon(2130837563);
        } else {
            setTitleIcon(0);
        }
    }

    private void setTitleIcon(int i) {
        if (i == 0) {
            this.mTitle.setPadding(this.mTitle.getCompoundDrawablePadding(), 0, 0, 0);
        } else {
            this.mTitle.setPadding(0, 0, 0, 0);
        }
        this.mTitle.setCompoundDrawablesWithIntrinsicBounds(i, 0, 0, 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isClose(View view) {
        return view == this.mClose;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isTitle(View view) {
        return view == this.mTitleBar;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isWebView(View view) {
        return view == this.mImage;
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mClickListener = onClickListener;
        this.mTitleBar.setOnClickListener(this.mClickListener);
        this.mClose.setOnClickListener(this.mClickListener);
        if (this.mImage != null) {
            this.mImage.setOnClickListener(this.mClickListener);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setWebView(Tab tab) {
        this.mTab = tab;
        setTitle();
        Bitmap screenshot = tab.getScreenshot();
        if (screenshot != null) {
            this.mImage.setImageBitmap(screenshot);
            if (tab != null) {
                this.mImage.setContentDescription(tab.getTitle());
            }
        }
    }
}
