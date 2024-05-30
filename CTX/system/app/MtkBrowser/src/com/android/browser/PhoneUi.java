package com.android.browser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
/* loaded from: classes.dex */
public class PhoneUi extends BaseUi {
    private static final boolean DEBUG = Browser.DEBUG;
    private int mActionBarHeight;
    private AnimScreen mAnimScreen;
    private int mLatestOrientation;
    private NavScreen mNavScreen;
    private NavigationBarPhone mNavigationBar;
    boolean mShowNav;

    public PhoneUi(Activity activity, UiController uiController) {
        super(activity, uiController);
        this.mActionBarHeight = 0;
        this.mLatestOrientation = 0;
        this.mShowNav = false;
        setUseQuickControls(BrowserSettings.getInstance().useQuickControls());
        this.mNavigationBar = (NavigationBarPhone) this.mTitleBar.getNavigationBar();
        if (Build.VERSION.SDK_INT < 23) {
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(16843499, typedValue, true);
            this.mActionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, activity.getResources().getDisplayMetrics());
        }
    }

    @Override // com.android.browser.UI
    public void onDestroy() {
        hideTitleBar();
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void editUrl(boolean z, boolean z2) {
        if (this.mShowNav) {
            return;
        }
        super.editUrl(z, z2);
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public boolean onBackKey() {
        if (showingNavScreen() && this.mUiController.getTabControl().getTabCount() == 0) {
            return false;
        }
        if (showingNavScreen()) {
            this.mNavScreen.close(this.mUiController.getTabControl().getCurrentPosition());
            return true;
        }
        return super.onBackKey();
    }

    private boolean showingNavScreen() {
        return this.mNavScreen != null && this.mNavScreen.getVisibility() == 0;
    }

    @Override // com.android.browser.UI
    public boolean dispatchKey(int i, KeyEvent keyEvent) {
        return false;
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onProgressChanged(Tab tab) {
        super.onProgressChanged(tab);
        if (this.mNavScreen == null && getTitleBar().getHeight() > 0) {
            this.mHandler.sendEmptyMessage(100);
        }
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Log.d("PhoneUi", "PhoneUi.onConfigurationChanged(), new orientation = " + configuration.orientation);
        if (Build.VERSION.SDK_INT < 23) {
            TypedValue typedValue = new TypedValue();
            this.mActivity.getTheme().resolveAttribute(16843499, typedValue, true);
            this.mActionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, this.mActivity.getResources().getDisplayMetrics());
        }
        if (isEditingUrl() && this.mUiController != null && this.mUiController.isInCustomActionMode()) {
            ((View) this.mTitleBar.getParent()).animate().translationY(this.mActionBarHeight);
        }
        if (this.mNavigationBar.isMenuShowing() && configuration.orientation != this.mLatestOrientation) {
            this.mNavigationBar.dismissMenuOnly();
        }
        this.mLatestOrientation = configuration.orientation;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.browser.BaseUi
    public void handleMessage(Message message) {
        super.handleMessage(message);
        if (message.what == 100) {
            if (this.mNavScreen == null) {
                this.mNavScreen = new NavScreen(this.mActivity, this.mUiController, this);
                this.mCustomViewContainer.addView(this.mNavScreen, COVER_SCREEN_PARAMS);
                this.mNavScreen.setVisibility(8);
            }
            if (this.mAnimScreen == null) {
                this.mAnimScreen = new AnimScreen(this.mActivity);
                this.mAnimScreen.set(getTitleBar(), getWebView(), this.mContentView);
            }
        }
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void updateBottomBarState(boolean z, boolean z2, boolean z3) {
        if (this.mNeedBottomBar) {
            this.mBottomBar.changeBottomBarState(z2, z3);
            showBottomBarForDuration(2000L);
        }
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void setActiveTab(Tab tab) {
        this.mTitleBar.cancelTitleBarAnimation(true);
        this.mTitleBar.setSkipTitleBarAnimations(true);
        super.setActiveTab(tab);
        if (this.mShowNav) {
            detachTab(this.mActiveTab);
        }
        BrowserWebView browserWebView = (BrowserWebView) tab.getWebView();
        if (browserWebView == null) {
            Log.e("PhoneUi", "active tab with no webview detected");
            return;
        }
        if (this.mUseQuickControls) {
            this.mPieControl.forceToTop(this.mContentView);
        }
        browserWebView.setTitleBar(this.mTitleBar);
        this.mNavigationBar.onStateChanged(0);
        updateLockIconToLatest(tab);
        this.mTitleBar.setSkipTitleBarAnimations(false);
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenuState(this.mActiveTab, menu);
        return true;
    }

    public boolean inLockScreenMode() {
        return ((ActivityManager) this.mActivity.getSystemService("activity")).isInLockTaskMode();
    }

    public boolean noTabInNavScreen() {
        return showingNavScreen() && this.mUiController.getTabControl().getTabCount() == 0;
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void updateMenuState(Tab tab, Menu menu) {
        boolean z;
        boolean z2;
        boolean z3;
        MenuItem findItem = menu.findItem(R.id.bookmarks_menu_id);
        if (findItem != null) {
            findItem.setVisible(!showingNavScreen());
        }
        MenuItem findItem2 = menu.findItem(R.id.add_bookmark_menu_id);
        if (findItem2 != null) {
            if (tab != null && !tab.isSnapshot() && !showingNavScreen()) {
                z3 = true;
            } else {
                z3 = false;
            }
            findItem2.setVisible(z3);
        }
        MenuItem findItem3 = menu.findItem(R.id.page_info_menu_id);
        if (findItem3 != null) {
            findItem3.setVisible(false);
        }
        MenuItem findItem4 = menu.findItem(R.id.new_tab_menu_id);
        if (findItem4 != null && !this.mUseQuickControls) {
            findItem4.setVisible(false);
        }
        MenuItem findItem5 = menu.findItem(R.id.home_menu_id);
        if (findItem5 != null) {
            if (tab != null && !tab.isSnapshot() && !showingNavScreen()) {
                z2 = true;
            } else {
                z2 = false;
            }
            findItem5.setVisible(z2);
        }
        MenuItem findItem6 = menu.findItem(R.id.close_browser_menu_id);
        if (findItem6 != null) {
            findItem6.setVisible(!showingNavScreen());
            findItem6.setEnabled(!inLockScreenMode());
        }
        MenuItem findItem7 = menu.findItem(R.id.preferences_menu_id);
        if (findItem7 != null) {
            findItem7.setEnabled(!noTabInNavScreen());
        }
        MenuItem findItem8 = menu.findItem(R.id.history_menu_id);
        if (findItem8 != null) {
            findItem8.setEnabled(!noTabInNavScreen());
        }
        MenuItem findItem9 = menu.findItem(R.id.snapshots_menu_id);
        if (findItem9 != null) {
            findItem9.setEnabled(!noTabInNavScreen());
        }
        MenuItem findItem10 = menu.findItem(R.id.close_other_tabs_id);
        if (findItem10 != null) {
            if (tab == null || this.mTabControl.getTabCount() <= 1) {
                z = true;
            } else {
                z = false;
            }
            findItem10.setEnabled(!z);
        }
        if (showingNavScreen()) {
            menu.setGroupVisible(R.id.LIVE_MENU, false);
            menu.setGroupVisible(R.id.SNAPSHOT_MENU, false);
            menu.setGroupVisible(R.id.NAV_MENU, false);
            menu.setGroupVisible(R.id.COMBO_MENU, true);
        }
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (showingNavScreen() && menuItem.getItemId() != R.id.history_menu_id && menuItem.getItemId() != R.id.snapshots_menu_id) {
            hideNavScreen(this.mUiController.getTabControl().getCurrentPosition(), false);
        }
        return false;
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onContextMenuCreated(Menu menu) {
        hideTitleBar();
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onContextMenuClosed(Menu menu, boolean z) {
        if (z) {
            showTitleBar();
        }
    }

    @Override // com.android.browser.UI
    public void onActionModeStarted(ActionMode actionMode) {
        if (!isEditingUrl()) {
            hideTitleBar();
        } else {
            ((View) this.mTitleBar.getParent()).animate().translationY(this.mActionBarHeight);
        }
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onActionModeFinished(boolean z) {
        if (isEditingUrl()) {
            final ObjectAnimator duration = ObjectAnimator.ofFloat((View) this.mTitleBar.getParent(), "y", this.mActionBarHeight, 0.0f).setDuration(100L);
            duration.addListener(new Animator.AnimatorListener() { // from class: com.android.browser.PhoneUi.1
                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    PhoneUi.this.mTitleBar.getNavigationBar().getUrlInputView().showDropDown();
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                }
            });
            duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.browser.PhoneUi.2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    PhoneUi.this.mTitleBar.getNavigationBar().getUrlInputView().showDropDown();
                }
            });
            this.mHandler.postDelayed(new Runnable() { // from class: com.android.browser.PhoneUi.3
                @Override // java.lang.Runnable
                public void run() {
                    if (((View) PhoneUi.this.mTitleBar.getParent()).getY() != 0.0f) {
                        duration.start();
                    }
                }
            }, 300L);
        } else {
            ((View) this.mTitleBar.getParent()).animate().translationY(0.0f);
        }
        if (z) {
            showTitleBar();
        }
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public boolean isWebShowing() {
        return super.isWebShowing() && !showingNavScreen();
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void showWeb(boolean z) {
        super.showWeb(z);
        hideNavScreen(this.mUiController.getTabControl().getCurrentPosition(), z);
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public void onResume() {
        super.onResume();
        if (this.mNavScreen != null) {
            this.mNavScreen.reload();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showNavScreen() {
        if (DEBUG) {
            Log.d("browser", "PhoneUi.showNavScreen()--->");
        }
        if (this.mActiveTab != null) {
            this.mShowNav = true;
            this.mUiController.setBlockEvents(true);
            if (this.mNavScreen == null) {
                this.mNavScreen = new NavScreen(this.mActivity, this.mUiController, this);
                this.mCustomViewContainer.addView(this.mNavScreen, COVER_SCREEN_PARAMS);
            } else {
                this.mNavScreen.setVisibility(0);
                this.mNavScreen.setAlpha(1.0f);
                this.mNavScreen.refreshAdapter();
            }
            this.mActiveTab.capture();
            if (this.mAnimScreen == null) {
                this.mAnimScreen = new AnimScreen(this.mActivity);
            } else {
                this.mAnimScreen.mMain.setAlpha(1.0f);
                this.mAnimScreen.mTitle.setAlpha(1.0f);
                this.mAnimScreen.setScaleFactor(1.0f);
            }
            this.mAnimScreen.set(getTitleBar(), getWebView(), this.mContentView);
            if (this.mAnimScreen.mMain.getParent() == null) {
                this.mCustomViewContainer.addView(this.mAnimScreen.mMain, COVER_SCREEN_PARAMS);
            }
            this.mCustomViewContainer.setVisibility(0);
            this.mCustomViewContainer.bringToFront();
            this.mAnimScreen.mMain.layout(0, 0, this.mContentView.getWidth(), this.mContentView.getHeight() + this.mTitleBar.getHeight());
            int height = getTitleBar().getHeight();
            int width = this.mContentView.getWidth();
            int height2 = this.mContentView.getHeight() + this.mTitleBar.getHeight();
            int dimensionPixelSize = this.mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_width);
            int dimensionPixelSize2 = this.mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_height);
            int dimensionPixelSize3 = this.mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_titleheight);
            int width2 = (this.mContentView.getWidth() - dimensionPixelSize) / 2;
            int i = ((height2 - (dimensionPixelSize3 + dimensionPixelSize2)) / 2) + dimensionPixelSize3;
            int i2 = width2 + dimensionPixelSize;
            float width3 = dimensionPixelSize / this.mContentView.getWidth();
            this.mContentView.setVisibility(8);
            this.mFixedTitlebarContainer.setVisibility(8);
            this.mTitleBar.getNavigationBar().getUrlInputView().setVisibility(8);
            AnimatorSet animatorSet = new AnimatorSet();
            AnimatorSet animatorSet2 = new AnimatorSet();
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "left", 0, width2);
            ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "top", height, i);
            ObjectAnimator ofInt3 = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "right", width, i2);
            ObjectAnimator ofInt4 = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "bottom", height2, dimensionPixelSize2 + i);
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mAnimScreen.mTitle, "alpha", 1.0f, 0.0f);
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mAnimScreen, "scaleFactor", 1.0f, width3);
            ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(this.mAnimScreen.mMain, "alpha", 1.0f, 0.0f);
            ofFloat3.setDuration(100L);
            animatorSet2.playTogether(ofInt, ofInt2, ofInt3, ofInt4, ofFloat2, ofFloat);
            animatorSet2.setDuration(200L);
            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.browser.PhoneUi.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    PhoneUi.this.mCustomViewContainer.removeView(PhoneUi.this.mAnimScreen.mMain);
                    PhoneUi.this.finishAnimationIn();
                    PhoneUi.this.detachTab(PhoneUi.this.mActiveTab);
                    PhoneUi.this.mUiController.setBlockEvents(false);
                }
            });
            animatorSet.playSequentially(animatorSet2, ofFloat3);
            animatorSet.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishAnimationIn() {
        if (showingNavScreen()) {
            this.mNavScreen.sendAccessibilityEvent(32);
            this.mTabControl.setOnThumbnailUpdatedListener(this.mNavScreen);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hideNavScreen(int i, boolean z) {
        if (DEBUG) {
            Log.d("browser", "PhoneUi.hideNavScreen()--->position = " + i + ", animate = " + z);
        }
        this.mShowNav = false;
        if (showingNavScreen()) {
            this.mFixedTitlebarContainer.setVisibility(0);
            this.mTitleBar.getNavigationBar().getUrlInputView().setVisibility(0);
            Tab tab = this.mUiController.getTabControl().getTab(i);
            if (tab == null || !z) {
                if (tab != null) {
                    setActiveTab(tab);
                } else if (this.mTabControl.getTabCount() > 0) {
                    setActiveTab(this.mTabControl.getCurrentTab());
                }
                this.mContentView.setVisibility(0);
                finishAnimateOut();
                return;
            }
            NavTabView tabView = this.mNavScreen.getTabView(i);
            if (tabView == null) {
                if (this.mTabControl.getTabCount() > 0) {
                    setActiveTab(this.mTabControl.getCurrentTab());
                }
                this.mContentView.setVisibility(0);
                finishAnimateOut();
                return;
            }
            this.mUiController.setBlockEvents(true);
            this.mUiController.setActiveTab(tab);
            this.mContentView.setVisibility(0);
            if (this.mAnimScreen == null) {
                this.mAnimScreen = new AnimScreen(this.mActivity);
            }
            this.mAnimScreen.set(tab.getScreenshot());
            if (this.mAnimScreen.mMain.getParent() == null) {
                this.mCustomViewContainer.addView(this.mAnimScreen.mMain, COVER_SCREEN_PARAMS);
            }
            this.mAnimScreen.mMain.layout(0, 0, this.mContentView.getWidth(), this.mContentView.getHeight());
            this.mNavScreen.mScroller.finishScroller();
            ImageView imageView = tabView.mImage;
            int height = this.mTitleBar.getHeight();
            int width = this.mContentView.getWidth();
            int intrinsicWidth = imageView.getDrawable().getIntrinsicWidth();
            int intrinsicHeight = imageView.getDrawable().getIntrinsicHeight();
            int left = (tabView.getLeft() + imageView.getLeft()) - this.mNavScreen.mScroller.getScrollX();
            int top = (tabView.getTop() + imageView.getTop()) - this.mNavScreen.mScroller.getScrollY();
            int i2 = left + intrinsicWidth;
            int i3 = top + intrinsicHeight;
            float width2 = this.mContentView.getWidth() / intrinsicWidth;
            int i4 = ((int) (intrinsicHeight * width2)) + height;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.setMargins(left, top, i2, i3);
            this.mAnimScreen.mContent.setLayoutParams(layoutParams);
            this.mAnimScreen.setScaleFactor(1.0f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(ObjectAnimator.ofFloat(this.mNavScreen, "alpha", 1.0f, 0.0f), ObjectAnimator.ofFloat(this.mAnimScreen.mMain, "alpha", 0.0f, 1.0f));
            animatorSet.setDuration(100L);
            AnimatorSet animatorSet2 = new AnimatorSet();
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "left", left, 0);
            ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "top", top, height);
            ObjectAnimator ofInt3 = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "right", i2, width);
            ObjectAnimator ofInt4 = ObjectAnimator.ofInt(this.mAnimScreen.mContent, "bottom", i3, i4);
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mAnimScreen, "scaleFactor", 1.0f, width2);
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mCustomViewContainer, "alpha", 1.0f, 0.0f);
            ofFloat2.setDuration(100L);
            animatorSet2.playTogether(ofInt, ofInt2, ofInt3, ofInt4, ofFloat);
            animatorSet2.setDuration(200L);
            AnimatorSet animatorSet3 = new AnimatorSet();
            animatorSet3.playSequentially(animatorSet, animatorSet2, ofFloat2);
            animatorSet3.addListener(new AnimatorListenerAdapter() { // from class: com.android.browser.PhoneUi.5
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    PhoneUi.this.mCustomViewContainer.removeView(PhoneUi.this.mAnimScreen.mMain);
                    PhoneUi.this.finishAnimateOut();
                    PhoneUi.this.mUiController.setBlockEvents(false);
                }
            });
            animatorSet3.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishAnimateOut() {
        this.mTabControl.setOnThumbnailUpdatedListener(null);
        this.mNavScreen.setVisibility(8);
        this.mCustomViewContainer.setAlpha(1.0f);
        this.mCustomViewContainer.setVisibility(8);
    }

    @Override // com.android.browser.BaseUi, com.android.browser.UI
    public boolean needsRestoreAllTabs() {
        return false;
    }

    public void toggleNavScreen() {
        if (!showingNavScreen()) {
            showNavScreen();
        } else {
            hideNavScreen(this.mUiController.getTabControl().getCurrentPosition(), false);
        }
    }

    @Override // com.android.browser.UI
    public boolean shouldCaptureThumbnails() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class AnimScreen {
        private ImageView mContent;
        private Bitmap mContentBitmap;
        private Context mContext;
        private View mMain;
        private float mScale;
        private ImageView mTitle;
        private Bitmap mTitleBarBitmap;

        public AnimScreen(Context context) {
            this.mMain = LayoutInflater.from(context).inflate(R.layout.anim_screen, (ViewGroup) null);
            this.mTitle = (ImageView) this.mMain.findViewById(R.id.title);
            this.mContent = (ImageView) this.mMain.findViewById(R.id.content);
            this.mContent.setScaleType(ImageView.ScaleType.MATRIX);
            this.mContent.setImageMatrix(new Matrix());
            this.mScale = 1.0f;
            setScaleFactor(getScaleFactor());
            this.mContext = context;
        }

        public void set(TitleBar titleBar, WebView webView, View view) {
            if (titleBar == null || webView == null) {
                return;
            }
            if (titleBar.getWidth() > 0 && titleBar.getEmbeddedHeight() > 0) {
                if (this.mTitleBarBitmap == null || this.mTitleBarBitmap.getWidth() != titleBar.getWidth() || this.mTitleBarBitmap.getHeight() != titleBar.getEmbeddedHeight()) {
                    this.mTitleBarBitmap = safeCreateBitmap(titleBar.getWidth(), titleBar.getEmbeddedHeight());
                }
                if (this.mTitleBarBitmap != null) {
                    Canvas canvas = new Canvas(this.mTitleBarBitmap);
                    titleBar.draw(canvas);
                    canvas.setBitmap(null);
                }
            } else {
                this.mTitleBarBitmap = null;
            }
            this.mTitle.setImageBitmap(this.mTitleBarBitmap);
            this.mTitle.setVisibility(0);
            int height = webView.getHeight() - titleBar.getEmbeddedHeight();
            float dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.nav_tab_width) / webView.getWidth();
            if (dimensionPixelSize > 1.0f) {
                dimensionPixelSize = 1.0f;
            }
            int floor = (int) Math.floor(webView.getWidth() * dimensionPixelSize);
            Math.floor(height * dimensionPixelSize);
            if (view.getHeight() >= view.getWidth()) {
                if (this.mContentBitmap == null || this.mContentBitmap.getWidth() != floor || this.mContentBitmap.getHeight() != floor) {
                    this.mContentBitmap = safeCreateBitmap(floor, floor);
                }
            } else {
                int floor2 = (int) Math.floor(view.getWidth() * dimensionPixelSize);
                int floor3 = (int) Math.floor(view.getHeight() * dimensionPixelSize);
                if (this.mContentBitmap == null || this.mContentBitmap.getWidth() != floor2 || this.mContentBitmap.getHeight() != floor3) {
                    this.mContentBitmap = safeCreateBitmap(floor2, floor3);
                }
            }
            if (this.mContentBitmap != null) {
                Canvas canvas2 = new Canvas(this.mContentBitmap);
                int scrollX = webView.getScrollX();
                int scrollY = webView.getScrollY();
                canvas2.translate(-scrollX, (-scrollY) - titleBar.getEmbeddedHeight());
                canvas2.scale(dimensionPixelSize, dimensionPixelSize, scrollX, scrollY + titleBar.getEmbeddedHeight());
                webView.draw(canvas2);
                canvas2.setBitmap(null);
            }
            this.mContent.setScaleType(ImageView.ScaleType.FIT_XY);
            this.mContent.setImageBitmap(this.mContentBitmap);
        }

        private Bitmap safeCreateBitmap(int i, int i2) {
            Log.w("PhoneUi", "safeCreateBitmap() width: " + i + ", height: " + i2);
            if (i <= 0 || i2 <= 0) {
                return null;
            }
            return Bitmap.createBitmap(i, i2, Bitmap.Config.RGB_565);
        }

        public void set(Bitmap bitmap) {
            this.mTitle.setVisibility(8);
            this.mContent.setImageBitmap(bitmap);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setScaleFactor(float f) {
            this.mScale = f;
            Matrix matrix = new Matrix();
            matrix.postScale(f, f);
            this.mContent.setImageMatrix(matrix);
        }

        private float getScaleFactor() {
            return this.mScale;
        }
    }

    @Override // com.android.browser.UI
    public void hideIME() {
        ((InputMethodManager) this.mActivity.getSystemService("input_method")).hideSoftInputFromWindow(this.mNavigationBar.getWindowToken(), 0);
    }
}
