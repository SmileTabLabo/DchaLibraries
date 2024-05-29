package com.android.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.android.browser.UI;
import java.util.List;
/* loaded from: b.zip:com/android/browser/XLargeUi.class */
public class XLargeUi extends BaseUi {
    private ActionBar mActionBar;
    private PaintDrawable mFaviconBackground;
    private Handler mHandler;
    private NavigationBarTablet mNavBar;
    private TabBar mTabBar;

    public XLargeUi(Activity activity, UiController uiController) {
        super(activity, uiController);
        this.mHandler = new Handler();
        this.mNavBar = (NavigationBarTablet) this.mTitleBar.getNavigationBar();
        this.mTabBar = new TabBar(this.mActivity, this.mUiController, this);
        this.mActionBar = this.mActivity.getActionBar();
        setupActionBar();
        setUseQuickControls(BrowserSettings.getInstance().useQuickControls());
    }

    private void checkHideActionBar() {
        if (this.mUseQuickControls) {
            this.mHandler.post(new Runnable(this) { // from class: com.android.browser.XLargeUi.1
                final XLargeUi this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mActionBar.hide();
                }
            });
        }
    }

    private Drawable getFaviconBackground() {
        if (this.mFaviconBackground == null) {
            this.mFaviconBackground = new PaintDrawable();
            Resources resources = this.mActivity.getResources();
            this.mFaviconBackground.getPaint().setColor(resources.getColor(2131361800));
            this.mFaviconBackground.setCornerRadius(resources.getDimension(2131427361));
        }
        return this.mFaviconBackground;
    }

    private boolean isTypingKey(KeyEvent keyEvent) {
        boolean z = false;
        if (keyEvent.getUnicodeChar() > 0) {
            z = true;
        }
        return z;
    }

    private void setupActionBar() {
        this.mActionBar.setNavigationMode(0);
        this.mActionBar.setDisplayOptions(16);
        this.mActionBar.setCustomView(this.mTabBar);
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void addTab(Tab tab) {
        this.mTabBar.onNewTab(tab);
    }

    @Override // com.android.browser.UI
    public boolean dispatchKey(int i, KeyEvent keyEvent) {
        if (this.mActiveTab != null) {
            WebView webView = this.mActiveTab.getWebView();
            if (keyEvent.getAction() == 0) {
                switch (i) {
                    case 19:
                    case 21:
                    case 61:
                        if (webView != null && webView.hasFocus() && !this.mTitleBar.hasFocus()) {
                            editUrl(false, false);
                            return true;
                        }
                        break;
                }
                if (keyEvent.hasModifiers(4096) || !isTypingKey(keyEvent) || this.mTitleBar.isEditingUrl()) {
                    return false;
                }
                editUrl(true, false);
                return this.mContentView.dispatchKeyEvent(keyEvent);
            }
            return false;
        }
        return false;
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void editUrl(boolean z, boolean z2) {
        super.editUrl(z, z2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getContentWidth() {
        if (this.mContentView != null) {
            return this.mContentView.getWidth();
        }
        return 0;
    }

    @Override // com.android.browser.BaseUi
    public Drawable getFaviconDrawable(Bitmap bitmap) {
        Drawable[] drawableArr = new Drawable[2];
        drawableArr[0] = getFaviconBackground();
        if (bitmap == null) {
            drawableArr[1] = this.mGenericFavicon;
        } else {
            drawableArr[1] = new BitmapDrawable(this.mActivity.getResources(), bitmap);
        }
        LayerDrawable layerDrawable = new LayerDrawable(drawableArr);
        layerDrawable.setLayerInset(1, 2, 2, 2, 2);
        return layerDrawable;
    }

    @Override // com.android.browser.UI
    public void hideIME() {
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onActionModeFinished(boolean z) {
        checkHideActionBar();
        if (z) {
            showTitleBar();
        }
    }

    @Override // com.android.browser.UI
    public void onActionModeStarted(ActionMode actionMode) {
        if (this.mTitleBar.isEditingUrl()) {
            return;
        }
        hideTitleBar();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onAddTabCompleted(Tab tab) {
        checkHideActionBar();
    }

    @Override // com.android.browser.UI
    public void onDestroy() {
        hideTitleBar();
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onHideCustomView() {
        super.onHideCustomView();
        if (!this.mUseQuickControls) {
            this.mActionBar.show();
        }
        checkHideActionBar();
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem findItem = menu.findItem(2131558584);
        if (findItem != null) {
            findItem.setVisible(false);
            return true;
        }
        return true;
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onProgressChanged(Tab tab) {
        super.onProgressChanged(tab);
        if (tab.inForeground()) {
            tab.updateBookmarkedStatus();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onRemoveTabCompleted(Tab tab) {
        checkHideActionBar();
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onResume() {
        super.onResume();
        this.mNavBar.clearCompletions();
        checkHideActionBar();
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void removeTab(Tab tab) {
        this.mTitleBar.cancelTitleBarAnimation(true);
        this.mTitleBar.setSkipTitleBarAnimations(true);
        super.removeTab(tab);
        this.mTabBar.onRemoveTab(tab);
        this.mTitleBar.setSkipTitleBarAnimations(false);
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void setActiveTab(Tab tab) {
        this.mTitleBar.cancelTitleBarAnimation(true);
        this.mTitleBar.setSkipTitleBarAnimations(true);
        super.setActiveTab(tab);
        if (((BrowserWebView) tab.getWebView()) == null) {
            Log.e("XLargeUi", "active tab with no webview detected");
            return;
        }
        this.mTabBar.onSetActiveTab(tab);
        updateLockIconToLatest(tab);
        this.mTitleBar.setSkipTitleBarAnimations(false);
    }

    @Override // com.android.browser.BaseUi
    public void setFavicon(Tab tab) {
        super.setFavicon(tab);
        this.mTabBar.onFavicon(tab, tab.getFavicon());
    }

    @Override // com.android.browser.BaseUi
    public void setUrlTitle(Tab tab) {
        super.setUrlTitle(tab);
        this.mTabBar.onUrlAndTitle(tab, tab.getUrl(), tab.getTitle());
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void setUseQuickControls(boolean z) {
        super.setUseQuickControls(z);
        checkHideActionBar();
        if (!z) {
            this.mActionBar.show();
        }
        this.mTabBar.setUseQuickControls(this.mUseQuickControls);
        for (Tab tab : this.mTabControl.getTabs()) {
            tab.updateShouldCaptureThumbnails();
        }
    }

    @Override // com.android.browser.UI
    public boolean shouldCaptureThumbnails() {
        return this.mUseQuickControls;
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void showComboView(UI.ComboViews comboViews, Bundle bundle) {
        super.showComboView(comboViews, bundle);
        if (this.mUseQuickControls) {
            this.mActionBar.show();
        }
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void showCustomView(View view, int i, WebChromeClient.CustomViewCallback customViewCallback) {
        super.showCustomView(view, i, customViewCallback);
        this.mActionBar.hide();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopWebViewScrolling() {
        BrowserWebView browserWebView = (BrowserWebView) this.mUiController.getCurrentWebView();
    }

    @Override // com.android.browser.BaseUi
    protected void updateNavigationState(Tab tab) {
        this.mNavBar.updateNavigationState(tab);
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void updateTabs(List<Tab> list) {
        this.mTabBar.updateTabs(list);
        checkHideActionBar();
    }
}
