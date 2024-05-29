package com.android.browser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.android.browser.UI;
import com.android.browser.UrlInputView;
/* loaded from: b.zip:com/android/browser/NavigationBarTablet.class */
public class NavigationBarTablet extends NavigationBarBase implements UrlInputView.StateListener {
    private View mAllButton;
    private AnimatorSet mAnimation;
    private ImageButton mBackButton;
    private View mClearButton;
    private Drawable mFaviconDrawable;
    private Drawable mFocusDrawable;
    private ImageButton mForwardButton;
    private boolean mHideNavButtons;
    private View mNavButtons;
    private String mRefreshDescription;
    private Drawable mReloadDrawable;
    private ImageView mSearchButton;
    private ImageView mStar;
    private ImageView mStopButton;
    private String mStopDescription;
    private Drawable mStopDrawable;
    private Drawable mUnfocusDrawable;
    private View mUrlContainer;
    private ImageView mUrlIcon;

    public NavigationBarTablet(Context context) {
        super(context);
        init(context);
    }

    public NavigationBarTablet(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public NavigationBarTablet(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void clearOrClose() {
        if (TextUtils.isEmpty(this.mUrlInput.getText())) {
            this.mUrlInput.clearFocus();
        } else {
            this.mUrlInput.setText("");
        }
    }

    private void hideNavButtons() {
        if (this.mBaseUi.blockFocusAnimations()) {
            this.mNavButtons.setVisibility(8);
            return;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mNavButtons, View.TRANSLATION_X, 0.0f, -this.mNavButtons.getMeasuredWidth());
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this.mUrlContainer, "left", this.mUrlContainer.getLeft(), this.mUrlContainer.getPaddingLeft());
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mNavButtons, View.ALPHA, 1.0f, 0.0f);
        this.mAnimation = new AnimatorSet();
        this.mAnimation.playTogether(ofFloat, ofInt, ofFloat2);
        this.mAnimation.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.browser.NavigationBarTablet.1
            final NavigationBarTablet this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mNavButtons.setVisibility(8);
                this.this$0.mAnimation = null;
            }
        });
        this.mAnimation.setDuration(150L);
        this.mAnimation.start();
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        this.mStopDrawable = resources.getDrawable(2130837589);
        this.mReloadDrawable = resources.getDrawable(2130837576);
        this.mStopDescription = resources.getString(2131493293);
        this.mRefreshDescription = resources.getString(2131493292);
        this.mFocusDrawable = resources.getDrawable(2130837609);
        this.mUnfocusDrawable = resources.getDrawable(2130837610);
        this.mHideNavButtons = resources.getBoolean(2131296258);
    }

    private void showHideStar(Tab tab) {
        if (tab == null || !tab.inForeground()) {
            return;
        }
        int i = 0;
        if (DataUri.isDataUri(tab.getUrl())) {
            i = 8;
        }
        this.mStar.setVisibility(i);
    }

    private void showNavButtons() {
        if (this.mAnimation != null) {
            this.mAnimation.cancel();
        }
        this.mNavButtons.setVisibility(0);
        this.mNavButtons.setTranslationX(0.0f);
        if (this.mBaseUi.blockFocusAnimations()) {
            this.mNavButtons.setAlpha(1.0f);
            return;
        }
        int measuredWidth = this.mNavButtons.getMeasuredWidth();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mNavButtons, View.TRANSLATION_X, -measuredWidth, 0.0f);
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this.mUrlContainer, "left", 0, measuredWidth);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mNavButtons, View.ALPHA, 0.0f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ofFloat, ofInt, ofFloat2);
        animatorSet.setDuration(150L);
        animatorSet.start();
    }

    private void stopOrRefresh() {
        if (this.mUiController == null) {
            return;
        }
        if (this.mTitleBar.isInLoad()) {
            this.mUiController.stopLoading();
        } else if (this.mUiController.getCurrentTopWebView() != null) {
            this.mUiController.getCurrentTopWebView().reload();
        }
    }

    @Override // com.android.browser.NavigationBarBase, android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mBackButton == view && this.mUiController.getCurrentTab() != null) {
            this.mUiController.getCurrentTab().goBack();
        } else if (this.mForwardButton == view && this.mUiController.getCurrentTab() != null) {
            this.mUiController.getCurrentTab().goForward();
        } else if (this.mStar == view) {
            Intent createBookmarkCurrentPageIntent = this.mUiController.createBookmarkCurrentPageIntent(true);
            if (createBookmarkCurrentPageIntent != null) {
                getContext().startActivity(createBookmarkCurrentPageIntent);
            }
        } else if (this.mAllButton == view) {
            this.mUiController.bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
        } else if (this.mSearchButton == view) {
            this.mBaseUi.editUrl(true, true);
        } else if (this.mStopButton == view) {
            stopOrRefresh();
        } else if (this.mClearButton == view) {
            clearOrClose();
        } else {
            super.onClick(view);
        }
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mHideNavButtons = this.mContext.getResources().getBoolean(2131296258);
        if (this.mUrlInput.hasFocus()) {
            if (this.mHideNavButtons && this.mNavButtons.getVisibility() == 0) {
                int measuredWidth = this.mNavButtons.getMeasuredWidth();
                this.mNavButtons.setVisibility(8);
                this.mNavButtons.setAlpha(0.0f);
                this.mNavButtons.setTranslationX(-measuredWidth);
            } else if (this.mHideNavButtons || this.mNavButtons.getVisibility() != 8) {
            } else {
                this.mNavButtons.setVisibility(0);
                this.mNavButtons.setAlpha(1.0f);
                this.mNavButtons.setTranslationX(0.0f);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.browser.NavigationBarBase, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mAllButton = findViewById(2131558550);
        this.mNavButtons = findViewById(2131558546);
        this.mBackButton = (ImageButton) findViewById(2131558441);
        this.mForwardButton = (ImageButton) findViewById(2131558442);
        this.mUrlIcon = (ImageView) findViewById(2131558548);
        this.mStar = (ImageView) findViewById(2131558487);
        this.mStopButton = (ImageView) findViewById(2131558541);
        this.mSearchButton = (ImageView) findViewById(2131558549);
        this.mClearButton = findViewById(2131558543);
        this.mUrlContainer = findViewById(2131558547);
        this.mBackButton.setOnClickListener(this);
        this.mForwardButton.setOnClickListener(this);
        this.mStar.setOnClickListener(this);
        this.mAllButton.setOnClickListener(this);
        this.mStopButton.setOnClickListener(this);
        this.mSearchButton.setOnClickListener(this);
        this.mClearButton.setOnClickListener(this);
        this.mUrlInput.setContainer(this.mUrlContainer);
        this.mUrlInput.setStateListener(this);
    }

    @Override // com.android.browser.NavigationBarBase
    public void onProgressStarted() {
        this.mStopButton.setImageDrawable(this.mStopDrawable);
        this.mStopButton.setContentDescription(this.mStopDescription);
    }

    @Override // com.android.browser.NavigationBarBase
    public void onProgressStopped() {
        this.mStopButton.setImageDrawable(this.mReloadDrawable);
        this.mStopButton.setContentDescription(this.mRefreshDescription);
    }

    @Override // com.android.browser.UrlInputView.StateListener
    public void onStateChanged(int i) {
        switch (i) {
            case 0:
                this.mClearButton.setVisibility(8);
                return;
            case 1:
                this.mClearButton.setVisibility(8);
                if (this.mUiController == null || this.mUiController.supportsVoice()) {
                }
                return;
            case 2:
                this.mClearButton.setVisibility(0);
                return;
            default:
                return;
        }
    }

    @Override // com.android.browser.NavigationBarBase
    public void onTabDataChanged(Tab tab) {
        super.onTabDataChanged(tab);
        showHideStar(tab);
    }

    @Override // com.android.browser.NavigationBarBase
    public void setCurrentUrlIsBookmark(boolean z) {
        this.mStar.setActivated(z);
    }

    @Override // com.android.browser.NavigationBarBase
    public void setFavicon(Bitmap bitmap) {
        this.mFaviconDrawable = this.mBaseUi.getFaviconDrawable(bitmap);
        updateUrlIcon();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.browser.NavigationBarBase
    public void setFocusState(boolean z) {
        super.setFocusState(z);
        if (z) {
            if (this.mHideNavButtons) {
                hideNavButtons();
            }
            this.mSearchButton.setVisibility(8);
            this.mStar.setVisibility(8);
            this.mUrlIcon.setImageResource(2130837583);
        } else {
            if (this.mHideNavButtons) {
                showNavButtons();
            }
            showHideStar(this.mUiController.getCurrentTab());
            if (this.mTitleBar.useQuickControls()) {
                this.mSearchButton.setVisibility(8);
            } else {
                this.mSearchButton.setVisibility(0);
            }
            updateUrlIcon();
        }
        this.mUrlContainer.setBackgroundDrawable(z ? this.mFocusDrawable : this.mUnfocusDrawable);
    }

    @Override // com.android.browser.NavigationBarBase
    public void setTitleBar(TitleBar titleBar) {
        super.setTitleBar(titleBar);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateNavigationState(Tab tab) {
        if (tab != null) {
            this.mBackButton.setImageResource(tab.canGoBack() ? 2130837533 : 2130837531);
            this.mForwardButton.setImageResource(tab.canGoForward() ? 2130837554 : 2130837553);
        }
        updateUrlIcon();
    }

    void updateUrlIcon() {
        if (this.mUrlInput.hasFocus()) {
            this.mUrlIcon.setImageResource(2130837583);
            return;
        }
        if (this.mFaviconDrawable == null) {
            this.mFaviconDrawable = this.mBaseUi.getFaviconDrawable(null);
        }
        this.mUrlIcon.setImageDrawable(this.mFaviconDrawable);
    }
}
