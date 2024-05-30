package com.android.browser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.browser.Tab;
import com.android.browser.UI;
import com.mediatek.browser.ext.IBrowserUrlExt;
import java.util.List;
/* loaded from: classes.dex */
public abstract class BaseUi implements UI {
    protected Tab mActiveTab;
    Activity mActivity;
    private boolean mActivityPaused;
    private boolean mBlockFocusAnimations;
    protected BottomBar mBottomBar;
    protected FrameLayout mContentView;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    protected FrameLayout mCustomViewContainer;
    private Bitmap mDefaultVideoPoster;
    private LinearLayout mErrorConsoleContainer;
    protected FrameLayout mFixedTitlebarContainer;
    protected FrameLayout mFrameLayout;
    protected FrameLayout mFullscreenContainer;
    protected Drawable mGenericFavicon;
    private InputMethodManager mInputManager;
    private Drawable mLockIconMixed;
    private Drawable mLockIconSecure;
    private NavigationBarBase mNavigationBar;
    protected boolean mNeedBottomBar;
    private int mOriginalOrientation;
    protected PieControl mPieControl;
    private Toast mStopToast;
    TabControl mTabControl;
    protected TitleBar mTitleBar;
    UiController mUiController;
    private UrlBarAutoShowManager mUrlBarAutoShowManager;
    protected boolean mUseQuickControls;
    private View mVideoProgressView;
    private static final boolean DEBUG = Browser.DEBUG;
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(-1, -1);
    protected static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = new FrameLayout.LayoutParams(-1, -1, 17);
    private boolean mInputUrlFlag = false;
    private IBrowserUrlExt mBrowserUrlExt = null;
    protected Handler mHandler = new Handler() { // from class: com.android.browser.BaseUi.2
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                BaseUi.this.suggestHideTitleBar();
            }
            if (message.what == 2 && BaseUi.this.mUiController != null && BaseUi.this.mUiController.getCurrentTab() != null && !BaseUi.this.mUiController.getCurrentTab().inPageLoad()) {
                BaseUi.this.hideBottomBar();
            }
            if (message.what == 3 && BaseUi.this.mUiController != null) {
                BaseUi.this.mUiController.closeTab((Tab) message.obj);
            }
            BaseUi.this.handleMessage(message);
        }
    };

    public BaseUi(Activity activity, UiController uiController) {
        this.mErrorConsoleContainer = null;
        this.mActivity = activity;
        this.mUiController = uiController;
        this.mTabControl = uiController.getTabControl();
        Resources resources = this.mActivity.getResources();
        this.mInputManager = (InputMethodManager) activity.getSystemService("input_method");
        this.mLockIconSecure = resources.getDrawable(R.drawable.ic_secure_holo_dark);
        this.mLockIconMixed = resources.getDrawable(R.drawable.ic_secure_partial_holo_dark);
        this.mFrameLayout = (FrameLayout) this.mActivity.getWindow().getDecorView().findViewById(16908290);
        LayoutInflater.from(this.mActivity).inflate(R.layout.custom_screen, this.mFrameLayout);
        this.mFixedTitlebarContainer = (FrameLayout) this.mFrameLayout.findViewById(R.id.fixed_titlebar_container);
        this.mContentView = (FrameLayout) this.mFrameLayout.findViewById(R.id.main_content);
        this.mCustomViewContainer = (FrameLayout) this.mFrameLayout.findViewById(R.id.fullscreen_custom_content);
        this.mErrorConsoleContainer = (LinearLayout) this.mFrameLayout.findViewById(R.id.error_console);
        this.mGenericFavicon = resources.getDrawable(R.drawable.app_web_browser_sm);
        this.mTitleBar = new TitleBar(this.mActivity, this.mUiController, this, this.mContentView);
        this.mNeedBottomBar = !BrowserActivity.isTablet(this.mActivity);
        if (this.mNeedBottomBar) {
            this.mBottomBar = new BottomBar(this.mActivity, this.mUiController, this, this.mTabControl, this.mContentView);
        }
        setFullscreen(BrowserSettings.getInstance().useFullscreen());
        this.mTitleBar.setProgress(100);
        this.mNavigationBar = this.mTitleBar.getNavigationBar();
        this.mUrlBarAutoShowManager = new UrlBarAutoShowManager(this);
    }

    private void cancelStopToast() {
        if (this.mStopToast != null) {
            this.mStopToast.cancel();
            this.mStopToast = null;
        }
    }

    @Override // com.android.browser.UI
    public void onPause() {
        if (isCustomViewShowing()) {
            onHideCustomView();
        }
        cancelStopToast();
        this.mActivityPaused = true;
    }

    @Override // com.android.browser.UI
    public void onResume() {
        this.mActivityPaused = false;
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab != null) {
            setActiveTab(currentTab);
        }
        this.mTitleBar.onResume();
    }

    protected boolean isActivityPaused() {
        return this.mActivityPaused;
    }

    @Override // com.android.browser.UI
    public void onConfigurationChanged(Configuration configuration) {
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    @Override // com.android.browser.UI
    public boolean onBackKey() {
        if (this.mCustomView != null) {
            this.mUiController.hideCustomView();
            return true;
        }
        return false;
    }

    @Override // com.android.browser.UI
    public boolean onMenuKey() {
        return false;
    }

    @Override // com.android.browser.UI
    public void setUseQuickControls(boolean z) {
        this.mUseQuickControls = z;
        if (this.mNeedBottomBar) {
            this.mBottomBar.setUseQuickControls(this.mUseQuickControls);
        }
        if (z) {
            this.mPieControl = new PieControl(this.mActivity, this.mUiController, this);
            this.mPieControl.attachToContainer(this.mContentView);
        } else if (this.mPieControl != null) {
            this.mPieControl.removeFromContainer(this.mContentView);
        }
        updateUrlBarAutoShowManagerTarget();
    }

    @Override // com.android.browser.UI
    public void onTabDataChanged(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.onTabDataChanged()--->tab = " + tab);
        }
        setUrlTitle(tab);
        setFavicon(tab);
        updateLockIconToLatest(tab);
        updateNavigationState(tab);
        this.mTitleBar.onTabDataChanged(tab);
        this.mNavigationBar.onTabDataChanged(tab);
        onProgressChanged(tab);
    }

    @Override // com.android.browser.UI
    public void onProgressChanged(Tab tab) {
        int loadProgress = tab.getLoadProgress();
        if (tab.inForeground()) {
            this.mTitleBar.setProgress(loadProgress);
        }
    }

    @Override // com.android.browser.UI
    public void bookmarkedStatusHasChanged(Tab tab) {
        if (tab.inForeground()) {
            this.mNavigationBar.setCurrentUrlIsBookmark(tab.isBookmarkedSite());
        }
    }

    @Override // com.android.browser.UI
    public void onPageStopped(Tab tab) {
        cancelStopToast();
        if (tab.inForeground()) {
            this.mStopToast = Toast.makeText(this.mActivity, (int) R.string.stopping, 0);
            this.mStopToast.show();
        }
    }

    @Override // com.android.browser.UI
    public boolean needsRestoreAllTabs() {
        return true;
    }

    @Override // com.android.browser.UI
    public void addTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.addTab()--->empty implemetion " + tab);
        }
    }

    @Override // com.android.browser.UI
    public void setActiveTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.setActiveTab()--->tab = " + tab);
        }
        if (tab == null) {
            return;
        }
        this.mBlockFocusAnimations = true;
        this.mHandler.removeMessages(1);
        if (tab != this.mActiveTab && this.mActiveTab != null) {
            WebView topWindow = this.mActiveTab.getTopWindow();
            if (topWindow != null && topWindow.hasFocus()) {
                this.mTitleBar.getNavigationBar().getUrlInputView().ignoreIME(true);
            }
            removeTabFromContentView(this.mActiveTab);
            WebView webView = this.mActiveTab.getWebView();
            if (webView != null) {
                webView.setOnTouchListener(null);
            }
        }
        this.mActiveTab = tab;
        BrowserWebView browserWebView = (BrowserWebView) this.mActiveTab.getWebView();
        updateUrlBarAutoShowManagerTarget();
        attachTabToContentView(tab);
        if (browserWebView != null) {
            if (this.mUseQuickControls) {
                this.mPieControl.forceToTop(this.mContentView);
            }
            browserWebView.setTitleBar(this.mTitleBar);
            this.mTitleBar.onScrollChanged();
        }
        this.mTitleBar.bringToFront();
        if (this.mNeedBottomBar) {
            this.mBottomBar.bringToFront();
        }
        tab.getTopWindow().requestFocus();
        this.mTitleBar.getNavigationBar().getUrlInputView().ignoreIME(false);
        setShouldShowErrorConsole(tab, this.mUiController.shouldShowErrorConsole());
        onTabDataChanged(tab);
        onProgressChanged(tab);
        this.mNavigationBar.setIncognitoMode(tab.isPrivateBrowsingEnabled());
        updateAutoLogin(tab, false);
        this.mBlockFocusAnimations = false;
    }

    protected void updateUrlBarAutoShowManagerTarget() {
        WebView webView = this.mActiveTab != null ? this.mActiveTab.getWebView() : null;
        if (!this.mUseQuickControls && (webView instanceof BrowserWebView)) {
            this.mUrlBarAutoShowManager.setTarget((BrowserWebView) webView);
        } else {
            this.mUrlBarAutoShowManager.setTarget(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getActiveTab() {
        return this.mActiveTab;
    }

    @Override // com.android.browser.UI
    public void updateTabs(List<Tab> list) {
    }

    @Override // com.android.browser.UI
    public void removeTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.removeTab()--->tab = " + tab);
        }
        if (this.mActiveTab == tab) {
            removeTabFromContentView(tab);
            this.mActiveTab = null;
        }
    }

    @Override // com.android.browser.UI
    public void detachTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.detachTab()--->tab = " + tab);
        }
        removeTabFromContentView(tab);
    }

    @Override // com.android.browser.UI
    public void attachTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.attachTab()--->tab = " + tab);
        }
        attachTabToContentView(tab);
    }

    protected void attachTabToContentView(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.attachTabToContentView()--->tab = " + tab);
        }
        if (tab == null || tab.getWebView() == null) {
            return;
        }
        View viewContainer = tab.getViewContainer();
        WebView webView = tab.getWebView();
        FrameLayout frameLayout = (FrameLayout) viewContainer.findViewById(R.id.webview_wrapper);
        ViewGroup viewGroup = (ViewGroup) webView.getParent();
        if (viewGroup != frameLayout) {
            if (viewGroup != null) {
                viewGroup.removeView(webView);
            }
            frameLayout.addView(webView);
        }
        ViewGroup viewGroup2 = (ViewGroup) viewContainer.getParent();
        if (viewGroup2 != this.mContentView) {
            if (viewGroup2 != null) {
                viewGroup2.removeView(viewContainer);
            }
            this.mContentView.addView(viewContainer, COVER_SCREEN_PARAMS);
        }
        this.mUiController.attachSubWindow(tab);
    }

    private void removeTabFromContentView(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.removeTabFromContentView()--->tab = " + tab);
        }
        hideTitleBar();
        if (tab == null) {
            return;
        }
        WebView webView = tab.getWebView();
        View viewContainer = tab.getViewContainer();
        if (webView == null) {
            return;
        }
        ((FrameLayout) viewContainer.findViewById(R.id.webview_wrapper)).removeView(webView);
        this.mContentView.removeView(viewContainer);
        this.mUiController.endActionMode();
        this.mUiController.removeSubWindow(tab);
        ErrorConsoleView errorConsole = tab.getErrorConsole(false);
        if (errorConsole != null) {
            this.mErrorConsoleContainer.removeView(errorConsole);
        }
    }

    @Override // com.android.browser.UI
    public void onSetWebView(Tab tab, WebView webView) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.onSetWebView()--->tab = " + tab + ", webView = " + webView);
        }
        View viewContainer = tab.getViewContainer();
        if (viewContainer == null) {
            viewContainer = this.mActivity.getLayoutInflater().inflate(R.layout.tab, (ViewGroup) this.mContentView, false);
            tab.setViewContainer(viewContainer);
        }
        if (tab.getWebView() != webView) {
            ((FrameLayout) viewContainer.findViewById(R.id.webview_wrapper)).removeView(tab.getWebView());
        }
    }

    @Override // com.android.browser.UI
    public void createSubWindow(Tab tab, final WebView webView) {
        if (DEBUG && webView != null) {
            Log.d("browser", "BaseUi.createSubWindow()--->subView()--->width = " + webView.getWidth() + ", view.height = " + webView.getHeight());
        }
        View inflate = this.mActivity.getLayoutInflater().inflate(R.layout.browser_subwindow, (ViewGroup) null);
        ((ViewGroup) inflate.findViewById(R.id.inner_container)).addView(webView, new ViewGroup.LayoutParams(-1, -1));
        ((ImageButton) inflate.findViewById(R.id.subwindow_close)).setOnClickListener(new View.OnClickListener() { // from class: com.android.browser.BaseUi.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((BrowserWebView) webView).getWebChromeClient().onCloseWindow(webView);
            }
        });
        tab.setSubWebView(webView);
        tab.setSubViewContainer(inflate);
    }

    @Override // com.android.browser.UI
    public void removeSubWindow(View view) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.removeSubWindow()--->");
        }
        this.mContentView.removeView(view);
        this.mUiController.endActionMode();
    }

    @Override // com.android.browser.UI
    public void attachSubWindow(View view) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.attachSubWindow()--->");
        }
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        this.mContentView.addView(view, COVER_SCREEN_PARAMS);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void refreshWebView() {
        WebView webView = getWebView();
        if (webView != null) {
            webView.invalidate();
        }
    }

    @Override // com.android.browser.UI
    public void editUrl(boolean z, boolean z2) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.editUrl()--->editUrl = " + z + ", forceIME = " + z2);
        }
        if (this.mUiController.isInCustomActionMode()) {
            this.mUiController.endActionMode();
        }
        showTitleBar();
        if (getActiveTab() != null && !getActiveTab().isSnapshot()) {
            this.mNavigationBar.startEditingUrl(z, z2);
        }
    }

    boolean canShowTitleBar() {
        return (isTitleBarShowing() || isActivityPaused() || getActiveTab() == null || getWebView() == null || this.mUiController.isInCustomActionMode()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void showTitleBar() {
        this.mHandler.removeMessages(1);
        if (canShowTitleBar()) {
            this.mTitleBar.show();
        }
    }

    protected void hideTitleBarOnly() {
        if (this.mTitleBar.isShowing()) {
            this.mTitleBar.hide();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void hideTitleBar() {
        if (this.mTitleBar.isShowing()) {
            this.mTitleBar.hide();
        }
        hideBottomBar();
    }

    protected void showBottomBarMust() {
        if (this.mNeedBottomBar && this.mBottomBar != null && !this.mBottomBar.isShowing()) {
            this.mBottomBar.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void hideBottomBar() {
        if (this.mNeedBottomBar && this.mBottomBar != null && this.mBottomBar.isShowing()) {
            this.mBottomBar.hide();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isTitleBarShowing() {
        return this.mTitleBar.getVisibility() == 0;
    }

    public boolean isEditingUrl() {
        return this.mTitleBar.isEditingUrl();
    }

    public void stopEditingUrl() {
        this.mTitleBar.getNavigationBar().stopEditingUrl();
    }

    public TitleBar getTitleBar() {
        return this.mTitleBar;
    }

    @Override // com.android.browser.UI
    public void showComboView(UI.ComboViews comboViews, Bundle bundle) {
        if (DEBUG && comboViews != null) {
            Log.d("browser", "BaseUi.showComboView()--->startingView = " + comboViews.toString());
        }
        Intent intent = new Intent(this.mActivity, ComboViewActivity.class);
        intent.putExtra("initial_view", comboViews.name());
        intent.putExtra("combo_args", bundle);
        Tab activeTab = getActiveTab();
        if (activeTab != null) {
            intent.putExtra("url", activeTab.getUrl());
        }
        this.mActivity.startActivityForResult(intent, 1);
    }

    @Override // com.android.browser.UI
    public void showCustomView(View view, int i, WebChromeClient.CustomViewCallback customViewCallback) {
        if (this.mCustomView != null) {
            customViewCallback.onCustomViewHidden();
            return;
        }
        this.mOriginalOrientation = this.mActivity.getRequestedOrientation();
        FrameLayout frameLayout = (FrameLayout) this.mActivity.getWindow().getDecorView();
        this.mFullscreenContainer = new FullscreenHolder(this.mActivity);
        this.mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        this.mFrameLayout.addView(this.mFullscreenContainer, COVER_SCREEN_PARAMS);
        this.mCustomView = view;
        setFullscreen(true);
        this.mFixedTitlebarContainer.setVisibility(4);
        this.mTitleBar.getNavigationBar().getUrlInputView().setVisibility(4);
        this.mCustomViewCallback = customViewCallback;
        this.mActivity.setRequestedOrientation(i);
    }

    @Override // com.android.browser.UI
    public void onHideCustomView() {
        BrowserWebView browserWebView = (BrowserWebView) getWebView();
        if (browserWebView != null && browserWebView.getVisibility() == 4) {
            browserWebView.setVisibility(0);
        }
        this.mFixedTitlebarContainer.setVisibility(0);
        this.mTitleBar.getNavigationBar().getUrlInputView().setVisibility(0);
        if (this.mCustomView == null) {
            return;
        }
        setFullscreen(BrowserSettings.getInstance().useFullscreen());
        FrameLayout frameLayout = (FrameLayout) this.mActivity.getWindow().getDecorView();
        this.mFrameLayout.removeView(this.mFullscreenContainer);
        this.mFullscreenContainer = null;
        this.mCustomView = null;
        this.mCustomViewCallback.onCustomViewHidden();
        this.mActivity.setRequestedOrientation(this.mOriginalOrientation);
        browserWebView.requestFocus();
    }

    @Override // com.android.browser.UI
    public boolean isCustomViewShowing() {
        return this.mCustomView != null;
    }

    @Override // com.android.browser.UI
    public boolean isWebShowing() {
        return this.mCustomView == null;
    }

    @Override // com.android.browser.UI
    public void showAutoLogin(Tab tab) {
        updateAutoLogin(tab, true);
    }

    @Override // com.android.browser.UI
    public void hideAutoLogin(Tab tab) {
        updateAutoLogin(tab, true);
    }

    protected void updateNavigationState(Tab tab) {
    }

    protected void updateAutoLogin(Tab tab, boolean z) {
        this.mTitleBar.updateAutoLogin(tab, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateLockIconToLatest(Tab tab) {
        if (tab != null && tab.inForeground()) {
            updateLockIconImage(tab.getSecurityState());
        }
    }

    private void updateLockIconImage(Tab.SecurityState securityState) {
        Drawable drawable;
        if (securityState == Tab.SecurityState.SECURITY_STATE_SECURE) {
            drawable = this.mLockIconSecure;
        } else if (securityState == Tab.SecurityState.SECURITY_STATE_MIXED || securityState == Tab.SecurityState.SECURITY_STATE_BAD_CERTIFICATE) {
            drawable = this.mLockIconMixed;
        } else {
            drawable = null;
        }
        this.mNavigationBar.setLock(drawable);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setUrlTitle(Tab tab) {
        String url = tab.getUrl();
        String title = tab.getTitle();
        Log.i("BaseUi", "Load Progress: " + tab.getLoadProgress() + "inPageLoad: " + tab.inPageLoad());
        if (TextUtils.isEmpty(title) || (!tab.inPageLoad() && title.equals(this.mActivity.getString(R.string.title_bar_loading)))) {
            title = url;
        }
        if (tab.inForeground()) {
            if (url.startsWith("file://")) {
                this.mNavigationBar.setDisplayTitle(title);
                return;
            }
            this.mBrowserUrlExt = Extensions.getUrlPlugin(this.mActivity);
            this.mNavigationBar.setDisplayTitle(this.mBrowserUrlExt.getNavigationBarTitle(title, url));
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setFavicon(Tab tab) {
        if (tab.inForeground()) {
            this.mNavigationBar.setFavicon(tab.getFavicon());
        }
    }

    @Override // com.android.browser.UI
    public void onActionModeFinished(boolean z) {
    }

    @Override // com.android.browser.UI
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override // com.android.browser.UI
    public void updateMenuState(Tab tab, Menu menu) {
    }

    @Override // com.android.browser.UI
    public void onOptionsMenuOpened() {
    }

    @Override // com.android.browser.UI
    public void onExtendedMenuOpened() {
    }

    @Override // com.android.browser.UI
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        return false;
    }

    @Override // com.android.browser.UI
    public void onOptionsMenuClosed(boolean z) {
    }

    @Override // com.android.browser.UI
    public void onExtendedMenuClosed(boolean z) {
    }

    @Override // com.android.browser.UI
    public void onContextMenuCreated(Menu menu) {
    }

    @Override // com.android.browser.UI
    public void onContextMenuClosed(Menu menu, boolean z) {
    }

    @Override // com.android.browser.UI
    public void setShouldShowErrorConsole(Tab tab, boolean z) {
        if (tab == null) {
            return;
        }
        ErrorConsoleView errorConsole = tab.getErrorConsole(true);
        if (z) {
            if (errorConsole.numberOfErrors() > 0) {
                errorConsole.showConsole(0);
            } else {
                errorConsole.showConsole(2);
            }
            if (errorConsole.getParent() != null) {
                this.mErrorConsoleContainer.removeView(errorConsole);
            }
            this.mErrorConsoleContainer.addView(errorConsole, new LinearLayout.LayoutParams(-1, -2));
            return;
        }
        this.mErrorConsoleContainer.removeView(errorConsole);
    }

    @Override // com.android.browser.UI
    public Bitmap getDefaultVideoPoster() {
        if (this.mDefaultVideoPoster == null) {
            this.mDefaultVideoPoster = BitmapFactory.decodeResource(this.mActivity.getResources(), R.drawable.default_video_poster);
        }
        return this.mDefaultVideoPoster;
    }

    @Override // com.android.browser.UI
    public View getVideoLoadingProgressView() {
        if (this.mVideoProgressView == null) {
            this.mVideoProgressView = LayoutInflater.from(this.mActivity).inflate(R.layout.video_loading_progress, (ViewGroup) null);
        }
        return this.mVideoProgressView;
    }

    @Override // com.android.browser.UI
    public void showMaxTabsWarning() {
        Toast.makeText(this.mActivity, this.mActivity.getString(R.string.max_tabs_warning), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public WebView getWebView() {
        if (this.mActiveTab != null) {
            return this.mActiveTab.getWebView();
        }
        return null;
    }

    @Override // com.android.browser.UI
    public void setFullscreen(boolean z) {
        if (DEBUG) {
            Log.d("browser", "BaseUi.setFullscreen()--->" + z);
        }
        Window window = this.mActivity.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (z) {
            attributes.flags |= 1024;
        } else {
            attributes.flags &= -1025;
            if (this.mCustomView != null) {
                this.mCustomView.setSystemUiVisibility(0);
            } else {
                this.mContentView.setSystemUiVisibility(0);
            }
        }
        if (this.mNeedBottomBar) {
            this.mBottomBar.setFullScreen(z);
        }
        window.setAttributes(attributes);
    }

    public Drawable getFaviconDrawable(Bitmap bitmap) {
        Drawable[] drawableArr = new Drawable[3];
        drawableArr[0] = new PaintDrawable(-16777216);
        drawableArr[1] = new PaintDrawable(-1);
        if (bitmap == null) {
            drawableArr[2] = this.mGenericFavicon;
        } else {
            drawableArr[2] = new BitmapDrawable(bitmap);
        }
        LayerDrawable layerDrawable = new LayerDrawable(drawableArr);
        layerDrawable.setLayerInset(1, 1, 1, 1, 1);
        layerDrawable.setLayerInset(2, 2, 2, 2, 2);
        return layerDrawable;
    }

    public boolean isLoading() {
        if (this.mActiveTab != null) {
            return this.mActiveTab.inPageLoad();
        }
        return false;
    }

    public void suggestHideTitleBar() {
        if (!isLoading() && !isEditingUrl() && !this.mTitleBar.wantsToBeVisible() && !this.mNavigationBar.isMenuShowing()) {
            hideTitleBarOnly();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void showTitleBarForDuration() {
        showTitleBarForDuration(2000L);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void showTitleBarForDuration(long j) {
        showTitleBar();
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 1), j);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void showBottomBarForDuration(long j) {
        if (getWebView() != null) {
            this.mHandler.removeMessages(2);
            showBottomBarMust();
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2), j);
        }
    }

    @Override // com.android.browser.UI
    public void closeTableDelay(Tab tab) {
        tab.clearTabData();
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 3, tab), 2000L);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void handleMessage(Message message) {
    }

    @Override // com.android.browser.UI
    public void showWeb(boolean z) {
        this.mUiController.hideCustomView();
    }

    /* loaded from: classes.dex */
    static class FullscreenHolder extends FrameLayout {
        public FullscreenHolder(Context context) {
            super(context);
            setBackgroundColor(context.getResources().getColor(R.color.black));
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setInputUrlFlag(boolean z) {
        this.mInputUrlFlag = z;
    }

    public void addFixedTitleBar(View view) {
        if (DEBUG && view != null) {
            Log.d("browser", "BaseUi.addFixedTitleBar()--->width = " + view.getWidth() + ", height = " + view.getHeight());
        }
        this.mFixedTitlebarContainer.addView(view);
    }

    public void setContentViewMarginTop(int i) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContentView.getLayoutParams();
        if (layoutParams.topMargin != i) {
            layoutParams.topMargin = i;
            this.mContentView.setLayoutParams(layoutParams);
        }
    }

    public void setContentViewMarginBottom(int i) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContentView.getLayoutParams();
        if (layoutParams.bottomMargin != i) {
            layoutParams.bottomMargin = i;
            this.mContentView.setLayoutParams(layoutParams);
        }
    }

    public boolean blockFocusAnimations() {
        return this.mBlockFocusAnimations;
    }

    @Override // com.android.browser.UI
    public void onVoiceResult(String str) {
        this.mNavigationBar.onVoiceResult(str);
    }

    @Override // com.android.browser.UI
    public void updateBottomBarState(boolean z, boolean z2, boolean z3) {
    }
}
