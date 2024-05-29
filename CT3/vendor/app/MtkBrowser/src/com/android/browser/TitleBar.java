package com.android.browser;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Region;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
/* loaded from: b.zip:com/android/browser/TitleBar.class */
public class TitleBar extends RelativeLayout {
    private AccessibilityManager mAccessibilityManager;
    private AutologinBar mAutoLogin;
    private BaseUi mBaseUi;
    private FrameLayout mContentView;
    private Animator.AnimatorListener mHideTileBarAnimatorListener;
    private boolean mInLoad;
    private boolean mIsFixedTitleBar;
    private NavigationBarBase mNavBar;
    private PageProgressView mProgress;
    private boolean mShowing;
    private boolean mSkipTitleBarAnimations;
    private int mSlop;
    private Animator mTitleBarAnimator;
    private UiController mUiController;
    private boolean mUseQuickControls;

    public TitleBar(Context context, UiController uiController, BaseUi baseUi, FrameLayout frameLayout) {
        super(context, null);
        this.mHideTileBarAnimatorListener = new Animator.AnimatorListener(this) { // from class: com.android.browser.TitleBar.1
            final TitleBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.onScrollChanged();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        };
        this.mUiController = uiController;
        this.mBaseUi = baseUi;
        this.mContentView = frameLayout;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        this.mSlop = ViewConfiguration.get(this.mUiController.getActivity()).getScaledTouchSlop();
        initLayout(context);
        setFixedTitleBar();
    }

    private int calculateEmbeddedHeight() {
        int height = this.mNavBar.getHeight();
        int i = height;
        if (this.mAutoLogin != null) {
            i = height;
            if (this.mAutoLogin.getVisibility() == 0) {
                i = height + this.mAutoLogin.getHeight();
            }
        }
        return i;
    }

    private boolean inAutoLogin() {
        boolean z = false;
        if (this.mAutoLogin != null) {
            z = false;
            if (this.mAutoLogin.getVisibility() == 0) {
                z = true;
            }
        }
        return z;
    }

    private void inflateAutoLoginBar() {
        if (this.mAutoLogin != null) {
            return;
        }
        this.mAutoLogin = (AutologinBar) ((ViewStub) findViewById(2131558530)).inflate();
        this.mAutoLogin.setTitleBar(this);
    }

    private void initLayout(Context context) {
        LayoutInflater.from(context).inflate(2130968629, this);
        this.mProgress = (PageProgressView) findViewById(2131558531);
        this.mNavBar = (NavigationBarBase) findViewById(2131558528);
        this.mNavBar.setTitleBar(this);
    }

    private ViewGroup.LayoutParams makeLayoutParams() {
        return new FrameLayout.LayoutParams(-1, -2);
    }

    private void setFixedTitleBar() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (!this.mIsFixedTitleBar || viewGroup == null) {
            this.mIsFixedTitleBar = true;
            setSkipTitleBarAnimations(true);
            show();
            setSkipTitleBarAnimations(false);
            if (viewGroup != null) {
                viewGroup.removeView(this);
            }
            if (this.mIsFixedTitleBar) {
                this.mBaseUi.addFixedTitleBar(this);
                return;
            }
            this.mContentView.addView(this, makeLayoutParams());
            this.mBaseUi.setContentViewMarginTop(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void cancelTitleBarAnimation(boolean z) {
        if (this.mTitleBarAnimator != null) {
            this.mTitleBarAnimator.cancel();
            this.mTitleBarAnimator = null;
        }
        if (z) {
            setTranslationY(0.0f);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public View focusSearch(View view, int i) {
        WebView currentWebView = getCurrentWebView();
        return (130 == i && hasFocus() && currentWebView != null && currentWebView.hasFocusable() && currentWebView.getParent() != null) ? currentWebView : super.focusSearch(view, i);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean gatherTransparentRegion(Region region) {
        if (region != null) {
            int[] iArr = new int[2];
            getLocationInWindow(iArr);
            region.op(0, 0, (iArr[0] + this.mRight) - this.mLeft, (iArr[1] + this.mBottom) - this.mTop, Region.Op.DIFFERENCE);
            return true;
        }
        return true;
    }

    public WebView getCurrentWebView() {
        Tab activeTab = this.mBaseUi.getActiveTab();
        if (activeTab != null) {
            return activeTab.getWebView();
        }
        return null;
    }

    public int getEmbeddedHeight() {
        if (this.mIsFixedTitleBar) {
            return 0;
        }
        return calculateEmbeddedHeight();
    }

    public NavigationBarBase getNavigationBar() {
        return this.mNavBar;
    }

    public PageProgressView getProgressView() {
        return this.mProgress;
    }

    public BaseUi getUi() {
        return this.mBaseUi;
    }

    public UiController getUiController() {
        return this.mUiController;
    }

    public int getVisibleTitleHeight() {
        WebView webView = null;
        Tab activeTab = this.mBaseUi.getActiveTab();
        if (activeTab != null) {
            webView = activeTab.getWebView();
        }
        return webView != null ? webView.getVisibleTitleHeight() : 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hide() {
        if (this.mIsFixedTitleBar) {
            return;
        }
        if (this.mUseQuickControls) {
            setVisibility(8);
        } else if (this.mSkipTitleBarAnimations) {
            onScrollChanged();
        } else {
            cancelTitleBarAnimation(false);
            this.mTitleBarAnimator = ObjectAnimator.ofFloat(this, "translationY", getTranslationY(), (-getEmbeddedHeight()) + getVisibleTitleHeight());
            this.mTitleBarAnimator.addListener(this.mHideTileBarAnimatorListener);
            setupTitleBarAnimator(this.mTitleBarAnimator);
            this.mTitleBarAnimator.start();
        }
        this.mShowing = false;
    }

    public void hideAutoLogin(boolean z) {
        if (this.mUseQuickControls) {
            this.mAutoLogin.setVisibility(8);
            this.mBaseUi.refreshWebView();
        } else if (z) {
            Animation loadAnimation = AnimationUtils.loadAnimation(getContext(), 2131034113);
            loadAnimation.setAnimationListener(new Animation.AnimationListener(this) { // from class: com.android.browser.TitleBar.2
                final TitleBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationEnd(Animation animation) {
                    this.this$0.mAutoLogin.setVisibility(8);
                    this.this$0.mBaseUi.refreshWebView();
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationRepeat(Animation animation) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationStart(Animation animation) {
                }
            });
            this.mAutoLogin.startAnimation(loadAnimation);
        } else if (this.mAutoLogin.getAnimation() == null) {
            this.mAutoLogin.setVisibility(8);
            this.mBaseUi.refreshWebView();
        }
    }

    public boolean isEditingUrl() {
        return this.mNavBar.isEditingUrl();
    }

    public boolean isInLoad() {
        return this.mInLoad;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isShowing() {
        return this.mShowing;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setFixedTitleBar();
    }

    @Override // android.widget.RelativeLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (!this.mIsFixedTitleBar) {
            this.mBaseUi.setContentViewMarginTop(0);
            return;
        }
        this.mBaseUi.setContentViewMarginTop(-(getMeasuredHeight() - calculateEmbeddedHeight()));
    }

    public void onResume() {
        setFixedTitleBar();
    }

    public void onScrollChanged() {
        if (this.mShowing || this.mIsFixedTitleBar) {
            return;
        }
        int visibleTitleHeight = getVisibleTitleHeight() - getEmbeddedHeight();
        setTranslationY(visibleTitleHeight);
        if (visibleTitleHeight > (-this.mSlop)) {
            show();
            this.mBaseUi.showBottomBarForDuration(2000L);
        } else if (visibleTitleHeight < (-this.mSlop)) {
            this.mBaseUi.hideBottomBar();
        }
    }

    public void onTabDataChanged(Tab tab) {
        this.mNavBar.setVisibility(0);
    }

    public void setProgress(int i) {
        if (i < 100) {
            if (!this.mInLoad) {
                this.mProgress.setVisibility(0);
                this.mInLoad = true;
                this.mNavBar.onProgressStarted();
            }
            this.mProgress.setProgress((i * 10000) / 100);
            if (this.mShowing) {
                return;
            }
            show();
            return;
        }
        this.mProgress.setProgress(10000);
        this.mProgress.setVisibility(8);
        this.mInLoad = false;
        this.mNavBar.onProgressStopped();
        if (isEditingUrl() || wantsToBeVisible() || this.mUseQuickControls) {
            return;
        }
        this.mBaseUi.showTitleBarForDuration();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSkipTitleBarAnimations(boolean z) {
        this.mSkipTitleBarAnimations = z;
    }

    void setupTitleBarAnimator(Animator animator) {
        int integer = this.mContext.getResources().getInteger(2131623944);
        animator.setInterpolator(new DecelerateInterpolator(2.5f));
        animator.setDuration(integer);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void show() {
        cancelTitleBarAnimation(false);
        setLayerType(2, null);
        if (this.mSkipTitleBarAnimations) {
            setVisibility(0);
            setTranslationY(0.0f);
        } else {
            float visibleTitleHeight = (-getEmbeddedHeight()) + getVisibleTitleHeight();
            float f = visibleTitleHeight;
            if (getTranslationY() != 0.0f) {
                f = Math.max(visibleTitleHeight, getTranslationY());
            }
            this.mTitleBarAnimator = ObjectAnimator.ofFloat(this, "translationY", f, 0.0f);
            setupTitleBarAnimator(this.mTitleBarAnimator);
            this.mTitleBarAnimator.start();
        }
        this.mShowing = true;
    }

    public void showAutoLogin(boolean z) {
        if (this.mUseQuickControls) {
            this.mBaseUi.showTitleBar();
        }
        if (this.mAutoLogin == null) {
            inflateAutoLoginBar();
        }
        this.mAutoLogin.setVisibility(0);
        if (z) {
            this.mAutoLogin.startAnimation(AnimationUtils.loadAnimation(getContext(), 2131034112));
        }
    }

    public void updateAutoLogin(Tab tab, boolean z) {
        if (this.mAutoLogin == null) {
            if (tab.getDeviceAccountLogin() == null) {
                return;
            }
            inflateAutoLoginBar();
        }
        this.mAutoLogin.updateAutoLogin(tab, z);
    }

    public boolean useQuickControls() {
        return this.mUseQuickControls;
    }

    public boolean wantsToBeVisible() {
        return inAutoLogin();
    }
}
