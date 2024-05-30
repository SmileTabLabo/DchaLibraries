package com.android.browser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.browser.UI;
import com.android.browser.view.PieItem;
import com.android.browser.view.PieMenu;
import com.android.browser.view.PieStackView;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class PieControl implements View.OnClickListener, PieMenu.PieController {
    protected Activity mActivity;
    private PieItem mAddBookmark;
    private PieItem mBack;
    private PieItem mBookmarks;
    private PieItem mClose;
    private PieItem mFind;
    private PieItem mForward;
    private PieItem mHistory;
    private PieItem mIncognito;
    private PieItem mInfo;
    protected int mItemSize;
    private PieItem mNewTab;
    private PieItem mOptions;
    protected PieMenu mPie;
    private PieItem mRDS;
    private PieItem mRefresh;
    private PieItem mShare;
    private PieItem mShowTabs;
    private TabAdapter mTabAdapter;
    protected TextView mTabsCount;
    private BaseUi mUi;
    protected UiController mUiController;
    private PieItem mUrl;

    public PieControl(Activity activity, UiController uiController, BaseUi baseUi) {
        this.mActivity = activity;
        this.mUiController = uiController;
        this.mItemSize = (int) activity.getResources().getDimension(R.dimen.qc_item_size);
        this.mUi = baseUi;
    }

    @Override // com.android.browser.view.PieMenu.PieController
    public void stopEditingUrl() {
        this.mUi.stopEditingUrl();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void attachToContainer(FrameLayout frameLayout) {
        if (this.mPie == null) {
            this.mPie = new PieMenu(this.mActivity);
            this.mPie.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            populateMenu();
            this.mPie.setController(this);
        }
        frameLayout.addView(this.mPie);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void removeFromContainer(FrameLayout frameLayout) {
        frameLayout.removeView(this.mPie);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void forceToTop(FrameLayout frameLayout) {
        if (this.mPie.getParent() != null) {
            frameLayout.removeView(this.mPie);
            frameLayout.addView(this.mPie);
        }
    }

    protected void setClickListener(View.OnClickListener onClickListener, PieItem... pieItemArr) {
        for (PieItem pieItem : pieItemArr) {
            pieItem.getView().setOnClickListener(onClickListener);
        }
    }

    @Override // com.android.browser.view.PieMenu.PieController
    public boolean onOpen() {
        this.mTabsCount.setText(Integer.toString(this.mUiController.getTabControl().getTabCount()));
        Tab currentTab = this.mUiController.getCurrentTab();
        if (currentTab != null) {
            this.mForward.setEnabled(currentTab.canGoForward());
        }
        WebView currentWebView = this.mUiController.getCurrentWebView();
        if (currentWebView != null) {
            ImageView imageView = (ImageView) this.mRDS.getView();
            if (this.mUiController.getSettings().hasDesktopUseragent(currentWebView)) {
                imageView.setImageResource(R.drawable.ic_mobile);
                return true;
            }
            imageView.setImageResource(R.drawable.ic_desktop_holo_dark);
            return true;
        }
        return true;
    }

    protected void populateMenu() {
        this.mBack = makeItem(R.drawable.ic_back_holo_dark, 1);
        this.mUrl = makeItem(R.drawable.ic_web_holo_dark, 1);
        this.mBookmarks = makeItem(R.drawable.ic_bookmarks, 1);
        this.mHistory = makeItem(R.drawable.ic_history_holo_dark, 1);
        this.mAddBookmark = makeItem(R.drawable.ic_bookmark_on_holo_dark, 1);
        this.mRefresh = makeItem(R.drawable.ic_refresh_holo_dark, 1);
        this.mForward = makeItem(R.drawable.ic_forward_holo_dark, 1);
        this.mNewTab = makeItem(R.drawable.ic_new_window_holo_dark, 1);
        this.mIncognito = makeItem(R.drawable.ic_new_incognito_holo_dark, 1);
        this.mClose = makeItem(R.drawable.ic_close_window_holo_dark, 1);
        this.mInfo = makeItem(17301569, 1);
        this.mFind = makeItem(R.drawable.ic_search_holo_dark, 1);
        this.mShare = makeItem(R.drawable.ic_share_holo_dark, 1);
        this.mShowTabs = new PieItem(makeTabsView(), 1);
        this.mOptions = makeItem(R.drawable.ic_settings_holo_dark, 1);
        this.mRDS = makeItem(R.drawable.ic_desktop_holo_dark, 1);
        this.mTabAdapter = new TabAdapter(this.mActivity, this.mUiController);
        PieStackView pieStackView = new PieStackView(this.mActivity);
        pieStackView.setLayoutListener(new PieMenu.PieView.OnLayoutListener() { // from class: com.android.browser.PieControl.1
            @Override // com.android.browser.view.PieMenu.PieView.OnLayoutListener
            public void onLayout(int i, int i2, boolean z) {
                PieControl.this.buildTabs();
            }
        });
        pieStackView.setOnCurrentListener(this.mTabAdapter);
        pieStackView.setAdapter(this.mTabAdapter);
        this.mShowTabs.setPieView(pieStackView);
        setClickListener(this, this.mBack, this.mRefresh, this.mForward, this.mUrl, this.mFind, this.mInfo, this.mShare, this.mBookmarks, this.mNewTab, this.mIncognito, this.mClose, this.mHistory, this.mAddBookmark, this.mOptions, this.mRDS);
        if (!BrowserActivity.isTablet(this.mActivity)) {
            this.mShowTabs.getView().setOnClickListener(this);
        }
        this.mPie.addItem(this.mOptions);
        this.mOptions.addItem(this.mRDS);
        this.mOptions.addItem(makeFiller());
        this.mOptions.addItem(makeFiller());
        this.mOptions.addItem(makeFiller());
        this.mPie.addItem(this.mBack);
        this.mBack.addItem(this.mRefresh);
        this.mBack.addItem(this.mForward);
        this.mBack.addItem(makeFiller());
        this.mBack.addItem(makeFiller());
        this.mPie.addItem(this.mUrl);
        this.mUrl.addItem(this.mFind);
        this.mUrl.addItem(this.mShare);
        this.mUrl.addItem(makeFiller());
        this.mUrl.addItem(makeFiller());
        this.mPie.addItem(this.mShowTabs);
        if (Build.VERSION.SDK_INT >= 19) {
            this.mShowTabs.addItem(makeFiller());
            this.mShowTabs.addItem(this.mClose);
        } else {
            this.mShowTabs.addItem(this.mClose);
            this.mShowTabs.addItem(this.mIncognito);
        }
        this.mShowTabs.addItem(this.mNewTab);
        this.mShowTabs.addItem(makeFiller());
        this.mPie.addItem(this.mBookmarks);
        this.mBookmarks.addItem(makeFiller());
        this.mBookmarks.addItem(makeFiller());
        this.mBookmarks.addItem(this.mAddBookmark);
        this.mBookmarks.addItem(this.mHistory);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Tab currentTab = this.mUiController.getTabControl().getCurrentTab();
        if (currentTab == null) {
            return;
        }
        WebView webView = currentTab.getWebView();
        if (this.mBack.getView() == view) {
            currentTab.goBack();
        } else if (this.mForward.getView() == view) {
            currentTab.goForward();
        } else if (this.mRefresh.getView() == view) {
            if (currentTab.inPageLoad()) {
                webView.stopLoading();
            } else {
                webView.reload();
            }
        } else if (this.mUrl.getView() == view) {
            this.mUi.editUrl(false, true);
        } else if (this.mBookmarks.getView() == view) {
            this.mUiController.bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
        } else if (this.mHistory.getView() == view) {
            this.mUiController.bookmarksOrHistoryPicker(UI.ComboViews.History);
        } else if (this.mAddBookmark.getView() == view) {
            this.mUiController.bookmarkCurrentPage();
        } else if (this.mNewTab.getView() == view) {
            this.mUiController.openTab("about:blank", false, true, false);
            this.mUi.editUrl(false, true);
        } else if (this.mIncognito.getView() == view) {
            this.mUiController.openIncognitoTab();
            this.mUi.editUrl(false, true);
        } else if (this.mClose.getView() == view) {
            this.mUiController.closeCurrentTab();
        } else if (this.mOptions.getView() == view) {
            this.mUiController.openPreferences();
        } else if (this.mShare.getView() == view) {
            this.mUiController.shareCurrentPage();
        } else if (this.mInfo.getView() == view) {
            this.mUiController.showPageInfo();
        } else if (this.mFind.getView() == view) {
            this.mUiController.findOnPage();
        } else if (this.mRDS.getView() == view) {
            this.mUiController.toggleUserAgent();
        } else if (this.mShowTabs.getView() == view) {
            ((PhoneUi) this.mUi).showNavScreen();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void buildTabs() {
        List<Tab> tabs = this.mUiController.getTabs();
        this.mUi.getActiveTab().capture();
        this.mTabAdapter.setTabs(tabs);
        ((PieStackView) this.mShowTabs.getPieView()).setCurrent(this.mUiController.getTabControl().getCurrentPosition());
    }

    protected PieItem makeItem(int i, int i2) {
        ImageView imageView = new ImageView(this.mActivity);
        imageView.setImageResource(i);
        imageView.setMinimumWidth(this.mItemSize);
        imageView.setMinimumHeight(this.mItemSize);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(this.mItemSize, this.mItemSize));
        return new PieItem(imageView, i2);
    }

    protected PieItem makeFiller() {
        return new PieItem(null, 1);
    }

    protected View makeTabsView() {
        View inflate = this.mActivity.getLayoutInflater().inflate(R.layout.qc_tabs_view, (ViewGroup) null);
        this.mTabsCount = (TextView) inflate.findViewById(R.id.label);
        this.mTabsCount.setText("1");
        ImageView imageView = (ImageView) inflate.findViewById(R.id.icon);
        imageView.setImageResource(R.drawable.ic_windows_holo_dark);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        inflate.setLayoutParams(new ViewGroup.LayoutParams(this.mItemSize, this.mItemSize));
        return inflate;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class TabAdapter extends BaseAdapter implements PieStackView.OnCurrentListener {
        LayoutInflater mInflater;
        UiController mUiController;
        private List<Tab> mTabs = new ArrayList();
        private int mCurrent = -1;

        public TabAdapter(Context context, UiController uiController) {
            this.mInflater = LayoutInflater.from(context);
            this.mUiController = uiController;
        }

        public void setTabs(List<Tab> list) {
            this.mTabs = list;
            notifyDataSetChanged();
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.mTabs.size();
        }

        @Override // android.widget.Adapter
        public Tab getItem(int i) {
            return this.mTabs.get(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            final Tab tab = this.mTabs.get(i);
            View inflate = this.mInflater.inflate(R.layout.qc_tab, (ViewGroup) null);
            ImageView imageView = (ImageView) inflate.findViewById(R.id.thumb);
            TextView textView = (TextView) inflate.findViewById(R.id.title1);
            TextView textView2 = (TextView) inflate.findViewById(R.id.title2);
            Bitmap screenshot = tab.getScreenshot();
            if (screenshot != null) {
                imageView.setImageBitmap(screenshot);
            }
            if (i > this.mCurrent) {
                textView.setVisibility(8);
                textView2.setText(tab.getTitle());
            } else {
                textView2.setVisibility(8);
                textView.setText(tab.getTitle());
            }
            inflate.setOnClickListener(new View.OnClickListener() { // from class: com.android.browser.PieControl.TabAdapter.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    TabAdapter.this.mUiController.switchToTab(tab);
                }
            });
            return inflate;
        }

        @Override // com.android.browser.view.PieStackView.OnCurrentListener
        public void onSetCurrent(int i) {
            this.mCurrent = i;
        }
    }
}
