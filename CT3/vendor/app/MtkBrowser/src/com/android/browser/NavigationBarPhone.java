package com.android.browser;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import com.android.browser.UrlInputView;
import com.mediatek.browser.ext.IBrowserUrlExt;
import com.mediatek.browser.hotknot.HotKnotHandler;
/* loaded from: b.zip:com/android/browser/NavigationBarPhone.class */
public class NavigationBarPhone extends NavigationBarBase implements UrlInputView.StateListener, PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener, ViewTreeObserver.OnGlobalLayoutListener {
    private IBrowserUrlExt mBrowserUrlExt;
    private ImageView mClearButton;
    private View mComboIcon;
    private View mHotKnot;
    private View mIncognitoIcon;
    private ImageView mMagnify;
    private View mMore;
    private boolean mNeedsMenu;
    private boolean mOverflowMenuShowing;
    private PopupMenu mPopupMenu;
    private String mRefreshDescription;
    private Drawable mRefreshDrawable;
    private ImageView mStopButton;
    private String mStopDescription;
    private Drawable mStopDrawable;
    private View mTabSwitcher;
    private Drawable mTextfieldBgDrawable;
    private View mTitleContainer;

    public NavigationBarPhone(Context context) {
        super(context);
        this.mBrowserUrlExt = null;
    }

    public NavigationBarPhone(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBrowserUrlExt = null;
    }

    public NavigationBarPhone(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBrowserUrlExt = null;
    }

    private void onMenuHidden() {
        this.mOverflowMenuShowing = false;
        this.mBaseUi.showTitleBarForDuration();
    }

    private void showHotKnotButton() {
        this.mHotKnot.setVisibility(8);
        if (!HotKnotHandler.isHotKnotSupported() || this.mBaseUi == null) {
            return;
        }
        Tab activeTab = this.mBaseUi.getActiveTab();
        Tab tab = activeTab;
        if (activeTab == null) {
            tab = this.mBaseUi.mTabControl.getCurrentTab();
        }
        if (tab == null || tab.getUrl() == null || tab.getUrl().length() <= 0) {
            return;
        }
        String url = tab.getUrl();
        if (url.startsWith("content:") || url.startsWith("browser:") || url.startsWith("file:")) {
            return;
        }
        this.mHotKnot.setVisibility(0);
    }

    private void showHotKnotShare() {
        Tab activeTab = this.mBaseUi.getActiveTab();
        Tab tab = activeTab;
        if (activeTab == null) {
            tab = this.mBaseUi.mTabControl.getCurrentTab();
        }
        if (tab != null) {
            HotKnotHandler.hotKnotStart(tab.getUrl());
        }
    }

    public void dismissMenuOnly() {
        if (!isMenuShowing() || this.mPopupMenu == null) {
            return;
        }
        this.mPopupMenu.setOnDismissListener(null);
        this.mPopupMenu.dismiss();
        this.mPopupMenu.setOnDismissListener(this);
    }

    @Override // com.android.browser.NavigationBarBase
    public boolean isMenuShowing() {
        return !super.isMenuShowing() ? this.mOverflowMenuShowing : true;
    }

    @Override // com.android.browser.NavigationBarBase, android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mStopButton) {
            if (this.mTitleBar.isInLoad()) {
                this.mUiController.stopLoading();
                return;
            }
            WebView webView = this.mBaseUi.getWebView();
            if (webView != null) {
                stopEditingUrl();
                webView.reload();
            }
        } else if (view == this.mTabSwitcher) {
            ((PhoneUi) this.mBaseUi).toggleNavScreen();
        } else if (this.mHotKnot == view) {
            showHotKnotShare();
        } else if (this.mMore == view) {
            showMenu(this.mMore);
        } else if (this.mClearButton == view) {
            this.mUrlInput.setText("");
        } else if (this.mComboIcon == view) {
            this.mUiController.showPageInfo();
        } else {
            super.onClick(view);
        }
    }

    @Override // android.widget.PopupMenu.OnDismissListener
    public void onDismiss(PopupMenu popupMenu) {
        if (popupMenu == this.mPopupMenu) {
            onMenuHidden();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.browser.NavigationBarBase, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mStopButton = (ImageView) findViewById(2131558541);
        this.mStopButton.setOnClickListener(this);
        this.mClearButton = (ImageView) findViewById(2131558543);
        this.mClearButton.setOnClickListener(this);
        this.mMagnify = (ImageView) findViewById(2131558538);
        this.mTabSwitcher = findViewById(2131558544);
        this.mTabSwitcher.setOnClickListener(this);
        this.mHotKnot = findViewById(2131558545);
        this.mHotKnot.setOnClickListener(this);
        this.mMore = findViewById(2131558495);
        this.mMore.setOnClickListener(this);
        this.mComboIcon = findViewById(2131558540);
        this.mComboIcon.setOnClickListener(this);
        this.mTitleContainer = findViewById(2131558537);
        setFocusState(false);
        Resources resources = getContext().getResources();
        this.mStopDrawable = resources.getDrawable(2130837589);
        this.mRefreshDrawable = resources.getDrawable(2130837576);
        this.mStopDescription = resources.getString(2131493293);
        this.mRefreshDescription = resources.getString(2131493292);
        this.mTextfieldBgDrawable = resources.getDrawable(2130837609);
        this.mUrlInput.setContainer(this);
        this.mUrlInput.setStateListener(this);
        this.mNeedsMenu = !ViewConfiguration.get(getContext()).hasPermanentMenuKey();
        this.mIncognitoIcon = findViewById(2131558539);
    }

    @Override // com.android.browser.NavigationBarBase, android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        if (view == this.mUrlInput) {
            String str = null;
            String str2 = null;
            Tab activeTab = this.mBaseUi.getActiveTab();
            Tab tab = activeTab;
            if (activeTab == null) {
                tab = this.mBaseUi.mTabControl.getCurrentTab();
            }
            if (tab != null) {
                str = tab.getUrl();
                str2 = tab.getTitle();
            }
            this.mBrowserUrlExt = Extensions.getUrlPlugin(this.mUiController.getActivity());
            String overrideFocusContent = this.mBrowserUrlExt.getOverrideFocusContent(z, this.mUrlInput.getText().toString(), (String) this.mUrlInput.getTag(), str);
            if (overrideFocusContent != null) {
                this.mUrlInput.setText((CharSequence) overrideFocusContent, false);
                this.mUrlInput.selectAll();
            } else {
                setDisplayTitle(this.mBrowserUrlExt.getOverrideFocusTitle(str2, this.mUrlInput.getText().toString()));
            }
        }
        super.onFocusChange(view, z);
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
        if (!this.mOverflowMenuShowing || this.mPopupMenu == null) {
            return;
        }
        this.mPopupMenu.show();
    }

    @Override // android.widget.PopupMenu.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        return this.mUiController.onOptionsItemSelected(menuItem);
    }

    @Override // com.android.browser.NavigationBarBase
    public void onProgressStarted() {
        super.onProgressStarted();
        if (this.mStopButton.getDrawable() != this.mStopDrawable) {
            this.mStopButton.setImageDrawable(this.mStopDrawable);
            this.mStopButton.setContentDescription(this.mStopDescription);
            if (this.mStopButton.getVisibility() != 0) {
                this.mComboIcon.setVisibility(8);
                this.mStopButton.setVisibility(0);
            }
        }
    }

    @Override // com.android.browser.NavigationBarBase
    public void onProgressStopped() {
        super.onProgressStopped();
        this.mStopButton.setImageDrawable(this.mRefreshDrawable);
        this.mStopButton.setContentDescription(this.mRefreshDescription);
        if (!isEditingUrl()) {
            this.mComboIcon.setVisibility(0);
        }
        onStateChanged(this.mUrlInput.getState());
    }

    @Override // com.android.browser.UrlInputView.StateListener
    public void onStateChanged(int i) {
        switch (i) {
            case 0:
                this.mComboIcon.setVisibility(0);
                this.mStopButton.setVisibility(8);
                this.mClearButton.setVisibility(8);
                this.mMagnify.setVisibility(8);
                this.mTabSwitcher.setVisibility(8);
                this.mTitleContainer.setBackgroundDrawable(null);
                showHotKnotButton();
                this.mMore.setVisibility(this.mNeedsMenu ? 0 : 8);
                return;
            case 1:
                this.mComboIcon.setVisibility(8);
                this.mStopButton.setVisibility(0);
                this.mClearButton.setVisibility(8);
                this.mMagnify.setVisibility(8);
                this.mTabSwitcher.setVisibility(8);
                this.mHotKnot.setVisibility(8);
                this.mMore.setVisibility(8);
                this.mTitleContainer.setBackgroundDrawable(this.mTextfieldBgDrawable);
                return;
            case 2:
                this.mComboIcon.setVisibility(8);
                this.mStopButton.setVisibility(8);
                this.mClearButton.setVisibility(0);
                this.mMagnify.setVisibility(0);
                this.mTabSwitcher.setVisibility(8);
                this.mHotKnot.setVisibility(8);
                this.mMore.setVisibility(8);
                this.mTitleContainer.setBackgroundDrawable(this.mTextfieldBgDrawable);
                return;
            default:
                return;
        }
    }

    @Override // com.android.browser.NavigationBarBase
    public void onTabDataChanged(Tab tab) {
        super.onTabDataChanged(tab);
        this.mIncognitoIcon.setVisibility(tab.isPrivateBrowsingEnabled() ? 0 : 8);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.browser.NavigationBarBase
    public void setDisplayTitle(String str) {
        this.mUrlInput.setTag(str);
        if (isEditingUrl()) {
            return;
        }
        if (str == null) {
            this.mUrlInput.setText(2131492948);
        } else if (str.startsWith("about:blank")) {
            this.mUrlInput.setText((CharSequence) UrlUtils.stripUrl("about:blank"), false);
        } else {
            this.mUrlInput.setText((CharSequence) UrlUtils.stripUrl(str), false);
        }
        this.mUrlInput.setSelection(0);
    }

    void showMenu(View view) {
        if (this.mOverflowMenuShowing) {
            return;
        }
        Activity activity = this.mUiController.getActivity();
        if (this.mPopupMenu == null) {
            this.mPopupMenu = new PopupMenu(this.mContext, view);
            this.mPopupMenu.setOnMenuItemClickListener(this);
            this.mPopupMenu.setOnDismissListener(this);
            view.getViewTreeObserver().addOnGlobalLayoutListener(this);
            if (!activity.onCreateOptionsMenu(this.mPopupMenu.getMenu())) {
                this.mPopupMenu = null;
                return;
            }
        }
        if (activity.onPrepareOptionsMenu(this.mPopupMenu.getMenu())) {
            this.mOverflowMenuShowing = true;
            this.mPopupMenu.show();
        }
    }
}
