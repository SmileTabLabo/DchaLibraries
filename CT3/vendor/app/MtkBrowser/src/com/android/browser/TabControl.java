package com.android.browser;

import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.webkit.WebView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: b.zip:com/android/browser/TabControl.class */
public class TabControl {
    private static final boolean DEBUG = Browser.DEBUG;
    private static long sNextId = 1;
    private final Controller mController;
    private int mCurrentTab = -1;
    private CopyOnWriteArrayList<Integer> mFreeTabIndex = new CopyOnWriteArrayList<>();
    private int mMaxTabs;
    private OnTabCountChangedListener mOnTabCountChangedListener;
    private OnThumbnailUpdatedListener mOnThumbnailUpdatedListener;
    private ArrayList<Tab> mTabQueue;
    private ArrayList<Tab> mTabs;

    /* loaded from: b.zip:com/android/browser/TabControl$OnTabCountChangedListener.class */
    public interface OnTabCountChangedListener {
        void onTabCountChanged();
    }

    /* loaded from: b.zip:com/android/browser/TabControl$OnThumbnailUpdatedListener.class */
    public interface OnThumbnailUpdatedListener {
        void onThumbnailUpdated(Tab tab);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public TabControl(Controller controller) {
        this.mController = controller;
        this.mMaxTabs = this.mController.getMaxTabs();
        this.mTabs = new ArrayList<>(this.mMaxTabs);
        this.mTabQueue = new ArrayList<>(this.mMaxTabs);
    }

    private WebView createNewWebView() {
        return createNewWebView(false);
    }

    private WebView createNewWebView(boolean z) {
        return this.mController.getWebViewFactory().createWebView(z);
    }

    private Vector<Tab> getHalfLeastUsedTabs(Tab tab) {
        Vector<Tab> vector = new Vector<>();
        if (getTabCount() == 1 || tab == null) {
            return vector;
        }
        if (this.mTabQueue.size() == 0) {
            return vector;
        }
        int i = 0;
        for (Tab tab2 : this.mTabQueue) {
            if (tab2 != null && tab2.getWebView() != null) {
                int i2 = i + 1;
                i = i2;
                if (tab2 != tab) {
                    i = i2;
                    if (tab2 != tab.getParent()) {
                        vector.add(tab2);
                        i = i2;
                    }
                }
            }
        }
        int i3 = i / 2;
        if (vector.size() > i3) {
            vector.setSize(i3);
        }
        return vector;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long getNextId() {
        long j;
        synchronized (TabControl.class) {
            try {
                j = sNextId;
                sNextId = 1 + j;
            } catch (Throwable th) {
                throw th;
            }
        }
        return j;
    }

    private boolean hasState(long j, Bundle bundle) {
        if (j == -1) {
            return false;
        }
        Bundle bundle2 = bundle.getBundle(Long.toString(j));
        boolean z = false;
        if (bundle2 != null) {
            z = !bundle2.isEmpty();
        }
        return z;
    }

    private boolean isIncognito(long j, Bundle bundle) {
        Bundle bundle2 = bundle.getBundle(Long.toString(j));
        if (bundle2 == null || bundle2.isEmpty()) {
            return false;
        }
        return bundle2.getBoolean("privateBrowsingEnabled");
    }

    private boolean setCurrentTab(Tab tab, boolean z) {
        boolean z2 = false;
        Tab tab2 = getTab(this.mCurrentTab);
        if (tab2 != tab || z) {
            if (tab2 != null) {
                tab2.putInBackground();
                this.mCurrentTab = -1;
            }
            if (tab == null) {
                return false;
            }
            int indexOf = this.mTabQueue.indexOf(tab);
            if (indexOf != -1) {
                this.mTabQueue.remove(indexOf);
            }
            this.mTabQueue.add(tab);
            this.mCurrentTab = this.mTabs.indexOf(tab);
            if (tab.getWebView() == null) {
                tab.setWebView(createNewWebView());
            }
            tab.putInForeground();
            boolean canScrollVertically = !tab.getWebView().canScrollVertically(-1) ? tab.getWebView().canScrollVertically(1) : true;
            UI ui = this.mController.getUi();
            if (tab.canGoBack() || tab.getParent() != null) {
                z2 = true;
            }
            ui.updateBottomBarState(canScrollVertically, z2, tab.canGoForward());
            return true;
        }
        return true;
    }

    private boolean tabMatchesUrl(Tab tab, String str) {
        return !str.equals(tab.getUrl()) ? str.equals(tab.getOriginalUrl()) : true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addPreloadedTab(Tab tab) {
        for (Tab tab2 : this.mTabs) {
            if (tab2 != null && tab2.getId() == tab.getId()) {
                throw new IllegalStateException("Tab with id " + tab.getId() + " already exists: " + tab2.toString());
            }
        }
        this.mTabs.add(tab);
        if (this.mOnTabCountChangedListener != null) {
            this.mOnTabCountChangedListener.onTabCountChanged();
        }
        tab.setController(this.mController);
        this.mController.onSetWebView(tab, tab.getWebView());
        tab.putInBackground();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean canCreateNewTab() {
        return this.mMaxTabs > this.mTabs.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public long canRestoreState(Bundle bundle, boolean z) {
        long[] jArr = null;
        if (bundle != null) {
            jArr = bundle.getLongArray("positions");
        }
        if (jArr == null) {
            return -1L;
        }
        long j = bundle.getLong("current");
        if (!z && (!hasState(j, bundle) || isIncognito(j, bundle))) {
            int i = 0;
            int length = jArr.length;
            while (true) {
                j = -1;
                if (i >= length) {
                    break;
                }
                j = jArr[i];
                if (hasState(j, bundle) && !isIncognito(j, bundle)) {
                    break;
                }
                i++;
            }
        }
        return j;
    }

    Tab createNewTab(Bundle bundle, boolean z) {
        this.mTabs.size();
        if (canCreateNewTab()) {
            Tab tab = new Tab(this.mController, createNewWebView(z), bundle);
            this.mTabs.add(tab);
            if (this.mOnTabCountChangedListener != null) {
                this.mOnTabCountChangedListener.onTabCountChanged();
            }
            tab.putInBackground();
            return tab;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab createNewTab(boolean z) {
        return createNewTab(null, z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroy() {
        Log.d("TabControl", "TabControl.destroy()--->Destroy all the tabs");
        for (Tab tab : this.mTabs) {
            tab.destroy();
        }
        this.mTabs.clear();
        this.mTabQueue.clear();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab findTabWithUrl(String str) {
        if (str == null) {
            return null;
        }
        Tab currentTab = getCurrentTab();
        if (currentTab == null || !tabMatchesUrl(currentTab, str)) {
            for (Tab tab : this.mTabs) {
                if (tabMatchesUrl(tab, str)) {
                    return tab;
                }
            }
            return null;
        }
        return currentTab;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void freeMemory() {
        if (getTabCount() == 0) {
            return;
        }
        String str = SystemProperties.get("ro.mtk_gmo_ram_optimize");
        Vector<Tab> halfLeastUsedTabs = getHalfLeastUsedTabs(getCurrentTab());
        this.mFreeTabIndex.clear();
        if (halfLeastUsedTabs.size() > 0) {
            Log.w("TabControl", "Free " + halfLeastUsedTabs.size() + " tabs in the browser");
            for (Tab tab : halfLeastUsedTabs) {
                this.mFreeTabIndex.add(Integer.valueOf(getTabPosition(tab) + 1));
                tab.saveState();
                tab.destroy();
            }
            if (str == null || !str.equals("1")) {
                return;
            }
        }
        Log.w("TabControl", "Free WebView's unused memory and cache");
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            currentWebView.freeMemory();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCurrentPosition() {
        return this.mCurrentTab;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebView getCurrentSubWindow() {
        Tab tab = getTab(this.mCurrentTab);
        if (tab == null) {
            return null;
        }
        return tab.getSubWebView();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getCurrentTab() {
        return getTab(this.mCurrentTab);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebView getCurrentTopWebView() {
        Tab tab = getTab(this.mCurrentTab);
        if (tab == null) {
            return null;
        }
        return tab.getTopWindow();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebView getCurrentWebView() {
        Tab tab = getTab(this.mCurrentTab);
        if (tab == null) {
            return null;
        }
        return tab.getWebView();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public CopyOnWriteArrayList<Integer> getFreeTabIndex() {
        return this.mFreeTabIndex;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getLeastUsedTab(Tab tab) {
        if (getTabCount() == 1 || tab == null || this.mTabQueue.size() == 0) {
            return null;
        }
        for (Tab tab2 : this.mTabQueue) {
            if (tab2 != null && tab2.getWebView() != null && tab2 != tab && tab2 != tab.getParent()) {
                return tab2;
            }
        }
        return null;
    }

    public OnThumbnailUpdatedListener getOnThumbnailUpdatedListener() {
        return this.mOnThumbnailUpdatedListener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getTab(int i) {
        if (i < 0 || i >= this.mTabs.size()) {
            return null;
        }
        return this.mTabs.get(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getTabCount() {
        return this.mTabs.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getTabFromAppId(String str) {
        if (str == null) {
            return null;
        }
        for (Tab tab : this.mTabs) {
            if (str.equals(tab.getAppId())) {
                return tab;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Removed duplicated region for block: B:5:0x0013  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Tab getTabFromView(WebView webView) {
        for (Tab tab : this.mTabs) {
            if (tab.getSubWebView() == webView || tab.getWebView() == webView) {
                return tab;
            }
            while (r0.hasNext()) {
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getTabPosition(Tab tab) {
        if (tab == null) {
            return -1;
        }
        return this.mTabs.indexOf(tab);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<Tab> getTabs() {
        return this.mTabs;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getVisibleWebviewNums() {
        int i = 0;
        if (this.mTabs.size() == 0) {
            return 0;
        }
        for (Tab tab : this.mTabs) {
            if (tab != null && tab.getWebView() != null) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void recreateWebView(Tab tab) {
        if (tab.getWebView() != null) {
            tab.destroy();
        }
        tab.setWebView(createNewWebView(), false);
        if (getCurrentTab() == tab) {
            setCurrentTab(tab, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeParentChildRelationShips() {
        for (Tab tab : this.mTabs) {
            tab.removeFromTree();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean removeTab(Tab tab) {
        if (tab == null) {
            return false;
        }
        Tab currentTab = getCurrentTab();
        this.mTabs.remove(tab);
        if (currentTab == tab) {
            tab.putInBackground();
            this.mCurrentTab = -1;
        } else {
            this.mCurrentTab = getTabPosition(currentTab);
        }
        tab.destroy();
        tab.removeFromTree();
        this.mTabQueue.remove(tab);
        if (this.mOnTabCountChangedListener != null) {
            this.mOnTabCountChangedListener.onTabCountChanged();
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void restoreState(Bundle bundle, long j, boolean z, boolean z2) {
        Tab tab;
        if (j == -1) {
            return;
        }
        long[] longArray = bundle.getLongArray("positions");
        long j2 = -9223372036854775807L;
        HashMap hashMap = new HashMap();
        int i = 0;
        int length = longArray.length;
        while (i < length) {
            long j3 = longArray[i];
            long j4 = j2;
            if (j3 > j2) {
                j4 = j3;
            }
            Bundle bundle2 = bundle.getBundle(Long.toString(j3));
            if (bundle2 != null && !bundle2.isEmpty() && (z || !bundle2.getBoolean("privateBrowsingEnabled"))) {
                if (j3 == j || z2) {
                    Tab createNewTab = createNewTab(bundle2, false);
                    if (createNewTab != null) {
                        hashMap.put(Long.valueOf(j3), createNewTab);
                        if (j3 == j) {
                            setCurrentTab(createNewTab);
                        }
                    }
                } else {
                    Tab tab2 = new Tab(this.mController, bundle2);
                    hashMap.put(Long.valueOf(j3), tab2);
                    this.mTabs.add(tab2);
                    if (this.mOnTabCountChangedListener != null) {
                        this.mOnTabCountChangedListener.onTabCountChanged();
                    }
                    this.mTabQueue.add(0, tab2);
                }
            }
            i++;
            j2 = j4;
        }
        sNextId = 1 + j2;
        if (this.mCurrentTab == -1 && getTabCount() > 0) {
            setCurrentTab(getTab(0));
        }
        for (long j5 : longArray) {
            Tab tab3 = (Tab) hashMap.get(Long.valueOf(j5));
            Bundle bundle3 = bundle.getBundle(Long.toString(j5));
            if (bundle3 != null && tab3 != null) {
                long j6 = bundle3.getLong("parentTab", -1L);
                if (j6 != -1 && (tab = (Tab) hashMap.get(Long.valueOf(j6))) != null) {
                    tab.addChildTab(tab3);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void saveState(Bundle bundle) {
        int tabCount = getTabCount();
        if (tabCount == 0) {
            return;
        }
        long[] jArr = new long[tabCount];
        int i = 0;
        for (Tab tab : this.mTabs) {
            Bundle saveState = tab.saveState();
            if (saveState != null) {
                jArr[i] = tab.getId();
                String l = Long.toString(tab.getId());
                if (bundle.containsKey(l)) {
                    for (Tab tab2 : this.mTabs) {
                        Log.e("TabControl", tab2.toString());
                    }
                    throw new IllegalStateException("Error saving state, duplicate tab ids!");
                }
                bundle.putBundle(l, saveState);
                i++;
            } else {
                jArr[i] = -1;
                tab.deleteThumbnail();
                i++;
            }
        }
        if (bundle.isEmpty()) {
            return;
        }
        bundle.putLongArray("positions", jArr);
        Tab currentTab = getCurrentTab();
        bundle.putLong("current", currentTab != null ? currentTab.getId() : -1L);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setActiveTab(Tab tab) {
        this.mController.setActiveTab(tab);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean setCurrentTab(Tab tab) {
        return setCurrentTab(tab, false);
    }

    public void setOnTabCountChangedListener(OnTabCountChangedListener onTabCountChangedListener) {
        this.mOnTabCountChangedListener = onTabCountChangedListener;
    }

    public void setOnThumbnailUpdatedListener(OnThumbnailUpdatedListener onThumbnailUpdatedListener) {
        this.mOnThumbnailUpdatedListener = onThumbnailUpdatedListener;
        for (Tab tab : this.mTabs) {
            WebView webView = tab.getWebView();
            if (webView != null) {
                if (onThumbnailUpdatedListener == null) {
                    tab = null;
                }
                webView.setPictureListener(tab);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopAllLoading() {
        for (Tab tab : this.mTabs) {
            WebView webView = tab.getWebView();
            if (webView != null) {
                webView.stopLoading();
            }
            WebView subWebView = tab.getSubWebView();
            if (subWebView != null) {
                subWebView.stopLoading();
            }
        }
    }
}
