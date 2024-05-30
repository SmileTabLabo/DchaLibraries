package com.android.browser;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import com.android.browser.NavTabScroller;
import com.android.browser.TabControl;
import com.android.browser.UI;
import java.util.HashMap;
/* loaded from: classes.dex */
public class NavScreen extends RelativeLayout implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, TabControl.OnThumbnailUpdatedListener {
    private static final boolean DEBUG = Browser.DEBUG;
    Activity mActivity;
    TabAdapter mAdapter;
    ImageButton mBookmarks;
    ImageButton mMore;
    boolean mNeedsMenu;
    ImageButton mNewTab;
    int mOrientation;
    PopupMenu mPopup;
    NavTabScroller mScroller;
    HashMap<Tab, View> mTabViews;
    PhoneUi mUi;
    UiController mUiController;

    public NavScreen(Activity activity, UiController uiController, PhoneUi phoneUi) {
        super(activity);
        this.mActivity = activity;
        this.mUiController = uiController;
        this.mUi = phoneUi;
        this.mOrientation = activity.getResources().getConfiguration().orientation;
        init();
    }

    public void reload() {
        int scrollValue = this.mScroller.getScrollValue();
        removeAllViews();
        if (this.mPopup != null) {
            this.mPopup.dismiss();
        }
        this.mOrientation = this.mActivity.getResources().getConfiguration().orientation;
        init();
        this.mScroller.setScrollValue(scrollValue);
        this.mAdapter.notifyDataSetChanged();
    }

    protected void showMenu() {
        this.mPopup = new PopupMenu(this.mContext, this.mMore);
        Menu menu = this.mPopup.getMenu();
        this.mPopup.getMenuInflater().inflate(R.menu.browser, menu);
        this.mUiController.updateMenuState(this.mUiController.getCurrentTab(), menu);
        this.mPopup.setOnMenuItemClickListener(this);
        this.mPopup.show();
    }

    @Override // android.widget.PopupMenu.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        return this.mUiController.onOptionsItemSelected(menuItem);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        Log.d("NavScreen", "NavScreen.onConfigurationChanged() new orientation = " + configuration.orientation + ", original orientation = " + this.mOrientation);
        if (configuration.orientation != this.mOrientation) {
            int scrollValue = this.mScroller.getScrollValue();
            removeAllViews();
            if (this.mPopup != null) {
                this.mPopup.dismiss();
            }
            this.mOrientation = configuration.orientation;
            init();
            this.mScroller.setScrollValue(scrollValue);
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void refreshAdapter() {
        this.mScroller.handleDataChanged(this.mUiController.getTabControl().getTabPosition(this.mUi.getActiveTab()));
    }

    private void init() {
        int i;
        LayoutInflater.from(this.mContext).inflate(R.layout.nav_screen, this);
        setContentDescription(this.mContext.getResources().getString(R.string.accessibility_transition_navscreen));
        this.mBookmarks = (ImageButton) findViewById(R.id.bookmarks);
        this.mNewTab = (ImageButton) findViewById(R.id.newtab);
        this.mMore = (ImageButton) findViewById(R.id.more);
        this.mBookmarks.setOnClickListener(this);
        this.mNewTab.setOnClickListener(this);
        this.mMore.setOnClickListener(this);
        this.mScroller = (NavTabScroller) findViewById(R.id.scroller);
        TabControl tabControl = this.mUiController.getTabControl();
        this.mTabViews = new HashMap<>(tabControl.getTabCount());
        this.mAdapter = new TabAdapter(this.mContext, tabControl);
        NavTabScroller navTabScroller = this.mScroller;
        if (this.mOrientation != 2) {
            i = 1;
        } else {
            i = 0;
        }
        navTabScroller.setOrientation(i);
        this.mScroller.setAdapter(this.mAdapter, this.mUiController.getTabControl().getTabPosition(this.mUi.getActiveTab()));
        this.mScroller.setOnRemoveListener(new NavTabScroller.OnRemoveListener() { // from class: com.android.browser.NavScreen.1
            @Override // com.android.browser.NavTabScroller.OnRemoveListener
            public void onRemovePosition(int i2) {
                NavScreen.this.onCloseTab(NavScreen.this.mAdapter.getItem(i2));
                NavScreen.this.mNewTab.setClickable(true);
                NavScreen.this.updateBookMarkButton();
            }
        });
        this.mNeedsMenu = !ViewConfiguration.get(getContext()).hasPermanentMenuKey();
        if (!this.mNeedsMenu) {
            this.mMore.setVisibility(8);
        }
        updateBookMarkButton();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBookMarkButton() {
        if (this.mUiController.getTabControl().getTabCount() == 0) {
            this.mBookmarks.setVisibility(8);
            this.mNewTab.setVisibility(8);
            return;
        }
        this.mBookmarks.setVisibility(0);
        this.mNewTab.setVisibility(0);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mBookmarks == view) {
            this.mUiController.bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
        } else if (this.mNewTab == view) {
            openNewTab();
        } else if (this.mMore == view) {
            showMenu();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCloseTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "NavScreen.onCloseTab()--->tab : " + tab);
        }
        if (tab != null) {
            if (tab == this.mUiController.getCurrentTab()) {
                this.mUiController.closeCurrentTab();
            } else {
                this.mUiController.closeTab(tab);
            }
            ImageView imageView = (ImageView) this.mTabViews.get(tab);
            if (imageView != null) {
                imageView.setImageBitmap(null);
            }
            this.mTabViews.remove(tab);
        }
        updateBookMarkButton();
    }

    private void openNewTab() {
        final Tab openTab = this.mUiController.openTab("about:blank", false, false, false);
        if (openTab != null) {
            this.mUiController.setBlockEvents(true);
            int tabPosition = this.mUi.mTabControl.getTabPosition(openTab);
            this.mScroller.setOnLayoutListener(new NavTabScroller.OnLayoutListener() { // from class: com.android.browser.NavScreen.2
                @Override // com.android.browser.NavTabScroller.OnLayoutListener
                public void onLayout(int i, int i2, int i3, int i4) {
                    NavScreen.this.mUi.hideNavScreen(NavScreen.this.mUi.mTabControl.getTabPosition(openTab), true);
                    NavScreen.this.switchToTab(openTab);
                }
            });
            this.mScroller.handleDataChanged(tabPosition);
            this.mUiController.setBlockEvents(false);
        }
        updateBookMarkButton();
        if (DEBUG) {
            Log.d("browser", "NavScreen.openNewTab()--->new tab is " + openTab);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void switchToTab(Tab tab) {
        if (tab != this.mUi.getActiveTab()) {
            this.mUiController.setActiveTab(tab);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void close(int i) {
        close(i, true);
    }

    protected void close(int i, boolean z) {
        this.mUi.hideNavScreen(i, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public NavTabView getTabView(int i) {
        return this.mScroller.getTabView(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class TabAdapter extends BaseAdapter {
        Context context;
        TabControl tabControl;

        public TabAdapter(Context context, TabControl tabControl) {
            this.context = context;
            this.tabControl = tabControl;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.tabControl.getTabCount();
        }

        @Override // android.widget.Adapter
        public Tab getItem(int i) {
            return this.tabControl.getTab(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final NavTabView navTabView = new NavTabView(NavScreen.this.mActivity);
            final Tab item = getItem(i);
            navTabView.setWebView(item);
            NavScreen.this.mTabViews.put(item, navTabView.mImage);
            navTabView.setOnClickListener(new View.OnClickListener() { // from class: com.android.browser.NavScreen.TabAdapter.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    if (navTabView.isClose(view2)) {
                        NavScreen.this.mNewTab.setClickable(false);
                        NavScreen.this.mScroller.animateOut(navTabView);
                        NavScreen.this.mTabViews.remove(item);
                    } else if (navTabView.isTitle(view2)) {
                        NavScreen.this.switchToTab(item);
                        NavScreen.this.mUi.getTitleBar().setSkipTitleBarAnimations(true);
                        NavScreen.this.close(i, false);
                        NavScreen.this.mUi.editUrl(false, true);
                        NavScreen.this.mUi.getTitleBar().setSkipTitleBarAnimations(false);
                    } else if (navTabView.isWebView(view2)) {
                        NavScreen.this.close(i);
                    }
                }
            });
            return navTabView;
        }
    }

    @Override // com.android.browser.TabControl.OnThumbnailUpdatedListener
    public void onThumbnailUpdated(Tab tab) {
        View view = this.mTabViews.get(tab);
        if (view != null) {
            view.invalidate();
        }
    }
}
