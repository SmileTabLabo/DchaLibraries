package com.android.browser;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/* loaded from: b.zip:com/android/browser/TabBar.class */
public class TabBar extends LinearLayout implements View.OnClickListener {
    private Drawable mActiveDrawable;
    private final Matrix mActiveMatrix;
    private BitmapShader mActiveShader;
    private final Paint mActiveShaderPaint;
    private Activity mActivity;
    private int mAddTabOverlap;
    private int mButtonWidth;
    private int mCurrentTextureHeight;
    private int mCurrentTextureWidth;
    private final Paint mFocusPaint;
    private Drawable mInactiveDrawable;
    private final Matrix mInactiveMatrix;
    private BitmapShader mInactiveShader;
    private final Paint mInactiveShaderPaint;
    private ImageButton mNewTab;
    private TabControl mTabControl;
    private Map<Tab, TabView> mTabMap;
    private int mTabOverlap;
    private int mTabSliceWidth;
    private int mTabWidth;
    private TabScrollView mTabs;
    private XLargeUi mUi;
    private UiController mUiController;
    private boolean mUseQuickControls;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/TabBar$TabView.class */
    public class TabView extends LinearLayout implements View.OnClickListener {
        ImageView mClose;
        Path mFocusPath;
        ImageView mIconView;
        View mIncognito;
        ImageView mLock;
        Path mPath;
        boolean mSelected;
        View mSnapshot;
        Tab mTab;
        View mTabContent;
        TextView mTitle;
        int[] mWindowPos;
        final TabBar this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public TabView(TabBar tabBar, Context context, Tab tab) {
            super(context);
            this.this$0 = tabBar;
            setWillNotDraw(false);
            this.mPath = new Path();
            this.mFocusPath = new Path();
            this.mWindowPos = new int[2];
            this.mTab = tab;
            setGravity(16);
            setOrientation(0);
            setPadding(tabBar.mTabOverlap, 0, tabBar.mTabSliceWidth, 0);
            this.mTabContent = LayoutInflater.from(getContext()).inflate(2130968628, (ViewGroup) this, true);
            this.mTitle = (TextView) this.mTabContent.findViewById(2131558407);
            this.mIconView = (ImageView) this.mTabContent.findViewById(2131558406);
            this.mLock = (ImageView) this.mTabContent.findViewById(2131558527);
            this.mClose = (ImageView) this.mTabContent.findViewById(2131558499);
            this.mClose.setOnClickListener(this);
            this.mIncognito = this.mTabContent.findViewById(2131558525);
            this.mSnapshot = this.mTabContent.findViewById(2131558526);
            this.mSelected = false;
            updateFromTab();
        }

        private void closeTab() {
            if (this.mTab == this.this$0.mTabControl.getCurrentTab()) {
                this.this$0.mUiController.closeCurrentTab();
            } else {
                this.this$0.mUiController.closeTab(this.mTab);
            }
        }

        private void drawClipped(Canvas canvas, Paint paint, Path path, int i) {
            Matrix matrix = this.mSelected ? this.this$0.mActiveMatrix : this.this$0.mInactiveMatrix;
            matrix.setTranslate(-i, 0.0f);
            BitmapShader bitmapShader = this.mSelected ? this.this$0.mActiveShader : this.this$0.mInactiveShader;
            bitmapShader.setLocalMatrix(matrix);
            paint.setShader(bitmapShader);
            canvas.drawPath(path, paint);
            if (isFocused()) {
                canvas.drawPath(this.mFocusPath, this.this$0.mFocusPaint);
            }
        }

        private void setFocusPath(Path path, int i, int i2, int i3, int i4) {
            path.reset();
            path.moveTo(i, i4);
            path.lineTo(i, i2);
            path.lineTo(i3 - this.this$0.mTabSliceWidth, i2);
            path.lineTo(i3, i4);
        }

        private void setTabPath(Path path, int i, int i2, int i3, int i4) {
            path.reset();
            path.moveTo(i, i4);
            path.lineTo(i, i2);
            path.lineTo(i3 - this.this$0.mTabSliceWidth, i2);
            path.lineTo(i3, i4);
            path.close();
        }

        private void updateFromTab() {
            String title = this.mTab.getTitle();
            String str = title;
            if (title == null) {
                str = this.mTab.getUrl();
            }
            setDisplayTitle(str);
            if (this.mTab.getFavicon() != null) {
                setFavicon(this.this$0.mUi.getFaviconDrawable(this.mTab.getFavicon()));
            }
            updateTabIcons();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateTabIcons() {
            this.mIncognito.setVisibility(this.mTab.isPrivateBrowsingEnabled() ? 0 : 8);
            this.mSnapshot.setVisibility(this.mTab.isSnapshot() ? 0 : 8);
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void dispatchDraw(Canvas canvas) {
            if (this.this$0.mCurrentTextureWidth != this.this$0.mUi.getContentWidth() || this.this$0.mCurrentTextureHeight != getHeight()) {
                this.this$0.mCurrentTextureWidth = this.this$0.mUi.getContentWidth();
                this.this$0.mCurrentTextureHeight = getHeight();
                if (this.this$0.mCurrentTextureWidth > 0 && this.this$0.mCurrentTextureHeight > 0) {
                    Bitmap drawableAsBitmap = TabBar.getDrawableAsBitmap(this.this$0.mActiveDrawable, this.this$0.mCurrentTextureWidth, this.this$0.mCurrentTextureHeight);
                    Bitmap drawableAsBitmap2 = TabBar.getDrawableAsBitmap(this.this$0.mInactiveDrawable, this.this$0.mCurrentTextureWidth, this.this$0.mCurrentTextureHeight);
                    this.this$0.mActiveShader = new BitmapShader(drawableAsBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    this.this$0.mActiveShaderPaint.setShader(this.this$0.mActiveShader);
                    this.this$0.mInactiveShader = new BitmapShader(drawableAsBitmap2, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    this.this$0.mInactiveShaderPaint.setShader(this.this$0.mInactiveShader);
                }
            }
            if (this.this$0.mActiveShader != null && this.this$0.mInactiveShader != null) {
                int save = canvas.save();
                getLocationInWindow(this.mWindowPos);
                drawClipped(canvas, this.mSelected ? this.this$0.mActiveShaderPaint : this.this$0.mInactiveShaderPaint, this.mPath, this.mWindowPos[0]);
                canvas.restoreToCount(save);
            }
            super.dispatchDraw(canvas);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view == this.mClose) {
                closeTab();
            }
        }

        @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            setTabPath(this.mPath, 0, 0, i3 - i, i4 - i2);
            setFocusPath(this.mFocusPath, 0, 0, i3 - i, i4 - i2);
        }

        @Override // android.view.View
        public void setActivated(boolean z) {
            this.mSelected = z;
            this.mClose.setVisibility(this.mSelected ? 0 : 8);
            this.mIconView.setVisibility(this.mSelected ? 8 : 0);
            this.mTitle.setTextAppearance(this.this$0.mActivity, this.mSelected ? 2131689485 : 2131689486);
            setHorizontalFadingEdgeEnabled(!this.mSelected);
            super.setActivated(z);
            updateLayoutParams();
            setFocusable(!z);
            postInvalidate();
        }

        void setDisplayTitle(String str) {
            if (str.startsWith("about:blank")) {
                this.mTitle.setText("about:blank");
            } else {
                this.mTitle.setText(str);
            }
        }

        void setFavicon(Drawable drawable) {
            this.mIconView.setImageDrawable(drawable);
        }

        public void updateLayoutParams() {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            layoutParams.width = this.this$0.mTabWidth;
            layoutParams.height = -1;
            setLayoutParams(layoutParams);
        }
    }

    public TabBar(Activity activity, UiController uiController, XLargeUi xLargeUi) {
        super(activity);
        this.mCurrentTextureWidth = 0;
        this.mCurrentTextureHeight = 0;
        this.mActiveShaderPaint = new Paint();
        this.mInactiveShaderPaint = new Paint();
        this.mFocusPaint = new Paint();
        this.mActiveMatrix = new Matrix();
        this.mInactiveMatrix = new Matrix();
        this.mActivity = activity;
        this.mUiController = uiController;
        this.mTabControl = this.mUiController.getTabControl();
        this.mUi = xLargeUi;
        Resources resources = activity.getResources();
        this.mTabWidth = (int) resources.getDimension(2131427328);
        this.mActiveDrawable = resources.getDrawable(2130837508);
        this.mInactiveDrawable = resources.getDrawable(2130837522);
        this.mTabMap = new HashMap();
        LayoutInflater.from(activity).inflate(2130968627, this);
        setPadding(0, (int) resources.getDimension(2131427357), 0, 0);
        this.mTabs = (TabScrollView) findViewById(2131558443);
        this.mNewTab = (ImageButton) findViewById(2131558494);
        this.mNewTab.setOnClickListener(this);
        updateTabs(this.mUiController.getTabs());
        this.mButtonWidth = -1;
        this.mTabOverlap = (int) resources.getDimension(2131427330);
        this.mAddTabOverlap = (int) resources.getDimension(2131427331);
        this.mTabSliceWidth = (int) resources.getDimension(2131427332);
        this.mActiveShaderPaint.setStyle(Paint.Style.FILL);
        this.mActiveShaderPaint.setAntiAlias(true);
        this.mInactiveShaderPaint.setStyle(Paint.Style.FILL);
        this.mInactiveShaderPaint.setAntiAlias(true);
        this.mFocusPaint.setStyle(Paint.Style.STROKE);
        this.mFocusPaint.setStrokeWidth(resources.getDimension(2131427333));
        this.mFocusPaint.setAntiAlias(true);
        this.mFocusPaint.setColor(resources.getColor(2131361801));
    }

    private void animateTabIn(Tab tab, TabView tabView) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(tabView, "scaleX", 0.0f, 1.0f);
        ofFloat.setDuration(150L);
        ofFloat.addListener(new Animator.AnimatorListener(this, tab, tabView) { // from class: com.android.browser.TabBar.2
            final TabBar this$0;
            final Tab val$tab;
            final TabView val$tv;

            {
                this.this$0 = this;
                this.val$tab = tab;
                this.val$tv = tabView;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mUi.onAddTabCompleted(this.val$tab);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.this$0.mTabs.addTab(this.val$tv);
            }
        });
        ofFloat.start();
    }

    private void animateTabOut(Tab tab, TabView tabView) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(tabView, "scaleX", 1.0f, 0.0f);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(tabView, "scaleY", 1.0f, 0.0f);
        ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(tabView, "alpha", 1.0f, 0.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ofFloat, ofFloat2, ofFloat3);
        animatorSet.setDuration(150L);
        animatorSet.addListener(new Animator.AnimatorListener(this, tabView, tab) { // from class: com.android.browser.TabBar.1
            final TabBar this$0;
            final Tab val$tab;
            final TabView val$tv;

            {
                this.this$0 = this;
                this.val$tv = tabView;
                this.val$tab = tab;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mTabs.removeTab(this.val$tv);
                this.this$0.mTabMap.remove(this.val$tab);
                this.this$0.mUi.onRemoveTabCompleted(this.val$tab);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        });
        animatorSet.start();
    }

    private TabView buildTabView(Tab tab) {
        TabView tabView = new TabView(this, this.mActivity, tab);
        this.mTabMap.put(tab, tabView);
        tabView.setOnClickListener(this);
        return tabView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Bitmap getDrawableAsBitmap(Drawable drawable, int i, int i2) {
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, i, i2);
        drawable.draw(canvas);
        canvas.setBitmap(null);
        return createBitmap;
    }

    private boolean isLoading() {
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab != null) {
            return currentTab.inPageLoad();
        }
        return false;
    }

    private void showUrlBar() {
        this.mUi.stopWebViewScrolling();
        this.mUi.showTitleBar();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mNewTab == view) {
            this.mUiController.openTabToHomePage();
        } else if (this.mTabs.getSelectedTab() != view) {
            if (view instanceof TabView) {
                Tab tab = ((TabView) view).mTab;
                int childIndex = this.mTabs.getChildIndex(view);
                if (childIndex >= 0) {
                    this.mTabs.setSelectedTab(childIndex);
                    this.mUiController.switchToTab(tab);
                }
            }
        } else if (!this.mUseQuickControls) {
            if (!this.mUi.isTitleBarShowing() || isLoading()) {
                showUrlBar();
                return;
            }
            this.mUi.stopEditingUrl();
            this.mUi.hideTitleBar();
        } else if (!this.mUi.isTitleBarShowing() || isLoading()) {
            this.mUi.stopWebViewScrolling();
            this.mUi.editUrl(false, false);
        } else {
            this.mUi.stopEditingUrl();
            this.mUi.hideTitleBar();
        }
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTabWidth = (int) this.mActivity.getResources().getDimension(2131427328);
        this.mTabs.updateLayout();
    }

    public void onFavicon(Tab tab, Bitmap bitmap) {
        TabView tabView = this.mTabMap.get(tab);
        if (tabView != null) {
            tabView.setFavicon(this.mUi.getFaviconDrawable(bitmap));
        }
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int measuredWidth = this.mTabs.getMeasuredWidth();
        int i6 = (i3 - i) - paddingLeft;
        if (this.mUseQuickControls) {
            this.mButtonWidth = 0;
            i5 = measuredWidth;
        } else {
            this.mButtonWidth = this.mNewTab.getMeasuredWidth() - this.mAddTabOverlap;
            i5 = measuredWidth;
            if (i6 - measuredWidth < this.mButtonWidth) {
                i5 = i6 - this.mButtonWidth;
            }
        }
        this.mTabs.layout(paddingLeft, paddingTop, paddingLeft + i5, i4 - i2);
        if (this.mUseQuickControls) {
            return;
        }
        this.mNewTab.layout((paddingLeft + i5) - this.mAddTabOverlap, paddingTop, ((paddingLeft + i5) + this.mButtonWidth) - this.mAddTabOverlap, i4 - i2);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int measuredWidth = getMeasuredWidth();
        int i3 = measuredWidth;
        if (!this.mUseQuickControls) {
            i3 = measuredWidth - this.mAddTabOverlap;
        }
        setMeasuredDimension(i3, getMeasuredHeight());
    }

    public void onNewTab(Tab tab) {
        animateTabIn(tab, buildTabView(tab));
    }

    public void onRemoveTab(Tab tab) {
        TabView tabView = this.mTabMap.get(tab);
        if (tabView != null) {
            animateTabOut(tab, tabView);
        } else {
            this.mTabMap.remove(tab);
        }
    }

    public void onSetActiveTab(Tab tab) {
        this.mTabs.setSelectedTab(this.mTabControl.getTabPosition(tab));
    }

    public void onUrlAndTitle(Tab tab, String str, String str2) {
        TabView tabView = this.mTabMap.get(tab);
        if (tabView != null) {
            if (str2 != null) {
                tabView.setDisplayTitle(str2);
            } else if (str != null) {
                tabView.setDisplayTitle(UrlUtils.stripUrl(str));
            }
            tabView.updateTabIcons();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setUseQuickControls(boolean z) {
        this.mUseQuickControls = z;
        this.mNewTab.setVisibility(this.mUseQuickControls ? 8 : 0);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateTabs(List<Tab> list) {
        this.mTabs.clearTabs();
        this.mTabMap.clear();
        for (Tab tab : list) {
            this.mTabs.addTab(buildTabView(tab));
        }
        this.mTabs.setSelectedTab(this.mTabControl.getCurrentPosition());
    }
}
