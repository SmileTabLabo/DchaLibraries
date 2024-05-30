package com.android.browser;

import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.webkit.WebView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
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

    /* loaded from: classes.dex */
    public interface OnTabCountChangedListener {
        void onTabCountChanged();
    }

    /* loaded from: classes.dex */
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public static synchronized long getNextId() {
        long j;
        synchronized (TabControl.class) {
            j = sNextId;
            sNextId = 1 + j;
        }
        return j;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebView getCurrentWebView() {
        Tab tab = getTab(this.mCurrentTab);
        if (tab == null) {
            return null;
        }
        return tab.getWebView();
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
    public WebView getCurrentSubWindow() {
        Tab tab = getTab(this.mCurrentTab);
        if (tab == null) {
            return null;
        }
        return tab.getSubWebView();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<Tab> getTabs() {
        return this.mTabs;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getTab(int i) {
        if (i >= 0 && i < this.mTabs.size()) {
            return this.mTabs.get(i);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getCurrentTab() {
        return getTab(this.mCurrentTab);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCurrentPosition() {
        return this.mCurrentTab;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getTabPosition(Tab tab) {
        if (tab == null) {
            return -1;
        }
        return this.mTabs.indexOf(tab);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean canCreateNewTab() {
        return this.mMaxTabs > this.mTabs.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addPreloadedTab(Tab tab) {
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            if (next != null && next.getId() == tab.getId()) {
                throw new IllegalStateException("Tab with id " + tab.getId() + " already exists: " + next.toString());
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
    public Tab createNewTab(boolean z) {
        return createNewTab(null, z);
    }

    Tab createNewTab(Bundle bundle, boolean z) {
        this.mTabs.size();
        if (!canCreateNewTab()) {
            return null;
        }
        Tab tab = new Tab(this.mController, createNewWebView(z), bundle);
        this.mTabs.add(tab);
        if (this.mOnTabCountChangedListener != null) {
            this.mOnTabCountChangedListener.onTabCountChanged();
        }
        tab.putInBackground();
        return tab;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeParentChildRelationShips() {
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            it.next().removeFromTree();
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
    public void destroy() {
        Log.d("TabControl", "TabControl.destroy()--->Destroy all the tabs");
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            it.next().destroy();
        }
        this.mTabs.clear();
        this.mTabQueue.clear();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getTabCount() {
        return this.mTabs.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void saveState(Bundle bundle) {
        int tabCount = getTabCount();
        if (tabCount == 0) {
            return;
        }
        long[] jArr = new long[tabCount];
        int i = 0;
        Iterator<Tab> it = this.mTabs.iterator();
        while (true) {
            if (it.hasNext()) {
                Tab next = it.next();
                Bundle saveState = next.saveState();
                if (saveState != null) {
                    int i2 = i + 1;
                    jArr[i] = next.getId();
                    String l = Long.toString(next.getId());
                    if (bundle.containsKey(l)) {
                        Iterator<Tab> it2 = this.mTabs.iterator();
                        while (it2.hasNext()) {
                            Tab next2 = it2.next();
                            if (DEBUG) {
                                Log.e("TabControl", next2.toString());
                            }
                        }
                        throw new IllegalStateException("Error saving state, duplicate tab ids!");
                    }
                    bundle.putBundle(l, saveState);
                    i = i2;
                } else {
                    jArr[i] = -1;
                    next.deleteThumbnail();
                    i++;
                }
            } else if (!bundle.isEmpty()) {
                bundle.putLongArray("positions", jArr);
                Tab currentTab = getCurrentTab();
                bundle.putLong("current", currentTab != null ? currentTab.getId() : -1L);
                return;
            } else {
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public long canRestoreState(Bundle bundle, boolean z) {
        long[] longArray = bundle == null ? null : bundle.getLongArray("positions");
        if (longArray == null) {
            return -1L;
        }
        long j = bundle.getLong("current");
        if (!z && (!hasState(j, bundle) || isIncognito(j, bundle))) {
            for (long j2 : longArray) {
                if (hasState(j2, bundle) && !isIncognito(j2, bundle)) {
                    return j2;
                }
            }
            return -1L;
        }
        return j;
    }

    private boolean hasState(long j, Bundle bundle) {
        Bundle bundle2;
        return (j == -1 || (bundle2 = bundle.getBundle(Long.toString(j))) == null || bundle2.isEmpty()) ? false : true;
    }

    private boolean isIncognito(long j, Bundle bundle) {
        Bundle bundle2 = bundle.getBundle(Long.toString(j));
        if (bundle2 != null && !bundle2.isEmpty()) {
            return bundle2.getBoolean("privateBrowsingEnabled");
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void restoreState(Bundle bundle, long j, boolean z, boolean z2) {
        int i;
        Tab tab;
        if (j == -1) {
            return;
        }
        long[] longArray = bundle.getLongArray("positions");
        HashMap hashMap = new HashMap();
        long j2 = -9223372036854775807L;
        for (long j3 : longArray) {
            if (j3 > j2) {
                j2 = j3;
            }
            Bundle bundle2 = bundle.getBundle(Long.toString(j3));
            if (bundle2 != null && !bundle2.isEmpty() && (z || !bundle2.getBoolean("privateBrowsingEnabled"))) {
                int i2 = (j3 > j ? 1 : (j3 == j ? 0 : -1));
                if (i2 == 0 || z2) {
                    Tab createNewTab = createNewTab(bundle2, false);
                    if (createNewTab != null) {
                        hashMap.put(Long.valueOf(j3), createNewTab);
                        if (i2 == 0) {
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
        }
        sNextId = j2 + 1;
        if (this.mCurrentTab == -1 && getTabCount() > 0) {
            i = 0;
            setCurrentTab(getTab(0));
        } else {
            i = 0;
        }
        int length = longArray.length;
        while (i < length) {
            long j4 = longArray[i];
            Tab tab3 = (Tab) hashMap.get(Long.valueOf(j4));
            Bundle bundle3 = bundle.getBundle(Long.toString(j4));
            if (bundle3 != null && tab3 != null) {
                long j5 = bundle3.getLong("parentTab", -1L);
                if (j5 != -1 && (tab = (Tab) hashMap.get(Long.valueOf(j5))) != null) {
                    tab.addChildTab(tab3);
                }
            }
            i++;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void freeMemory() {
        if (getTabCount() == 0) {
            return;
        }
        String str = SystemProperties.get("ro.vendor.gmo.ram_optimize");
        Vector<Tab> halfLeastUsedTabs = getHalfLeastUsedTabs(getCurrentTab());
        this.mFreeTabIndex.clear();
        if (halfLeastUsedTabs.size() > 0) {
            Log.w("TabControl", "Free " + halfLeastUsedTabs.size() + " tabs in the browser");
            Iterator<Tab> it = halfLeastUsedTabs.iterator();
            while (it.hasNext()) {
                Tab next = it.next();
                this.mFreeTabIndex.add(Integer.valueOf(getTabPosition(next) + 1));
                next.saveState();
                next.destroy();
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
    public int getVisibleWebviewNums() {
        int i = 0;
        if (this.mTabs.size() == 0) {
            return 0;
        }
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            if (next != null && next.getWebView() != null) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public CopyOnWriteArrayList<Integer> getFreeTabIndex() {
        return this.mFreeTabIndex;
    }

    private Vector<Tab> getHalfLeastUsedTabs(Tab tab) {
        int i;
        Vector<Tab> vector = new Vector<>();
        if (getTabCount() == 1 || tab == null) {
            return vector;
        }
        if (this.mTabQueue.size() == 0) {
            return vector;
        }
        int i2 = 0;
        Iterator<Tab> it = this.mTabQueue.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            if (next != null && next.getWebView() != null) {
                i2++;
                if (next != tab && next != tab.getParent()) {
                    vector.add(next);
                }
            }
        }
        String str = SystemProperties.get("ro.vendor.gmo.ram_optimize");
        if (i2 > 2 && str != null && str.equals("1")) {
            i = (i2 + 1) / 2;
        } else {
            i = i2 / 2;
        }
        if (vector.size() > i) {
            vector.setSize(i);
        }
        return vector;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getLeastUsedTab(Tab tab) {
        if (getTabCount() == 1 || tab == null || this.mTabQueue.size() == 0) {
            return null;
        }
        Iterator<Tab> it = this.mTabQueue.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            if (next != null && next.getWebView() != null && next != tab && next != tab.getParent()) {
                return next;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Removed duplicated region for block: B:5:0x000c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Tab getTabFromView(WebView webView) {
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            if (next.getSubWebView() == webView || next.getWebView() == webView) {
                return next;
            }
            while (it.hasNext()) {
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab getTabFromAppId(String str) {
        if (str == null) {
            return null;
        }
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            if (str.equals(next.getAppId())) {
                return next;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopAllLoading() {
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            WebView webView = next.getWebView();
            if (webView != null) {
                webView.stopLoading();
            }
            WebView subWebView = next.getSubWebView();
            if (subWebView != null) {
                subWebView.stopLoading();
            }
        }
    }

    private boolean tabMatchesUrl(Tab tab, String str) {
        return str.equals(tab.getUrl()) || str.equals(tab.getOriginalUrl());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab findTabWithUrl(String str) {
        if (str == null) {
            return null;
        }
        Tab currentTab = getCurrentTab();
        if (currentTab != null && tabMatchesUrl(currentTab, str)) {
            return currentTab;
        }
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            if (tabMatchesUrl(next, str)) {
                return next;
            }
        }
        return null;
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

    private WebView createNewWebView() {
        return createNewWebView(false);
    }

    private WebView createNewWebView(boolean z) {
        return this.mController.getWebViewFactory().createWebView(z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean setCurrentTab(Tab tab) {
        return setCurrentTab(tab, false);
    }

    private boolean setCurrentTab(Tab tab, boolean z) {
        Tab tab2 = getTab(this.mCurrentTab);
        if (tab2 == tab && !z) {
            return true;
        }
        if (tab2 != null) {
            tab2.putInBackground();
            this.mCurrentTab = -1;
        }
        boolean z2 = false;
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
        boolean z3 = tab.getWebView().canScrollVertically(-1) || tab.getWebView().canScrollVertically(1);
        UI ui = this.mController.getUi();
        if (tab.canGoBack() || tab.getParent() != null) {
            z2 = true;
        }
        ui.updateBottomBarState(z3, z2, tab.canGoForward());
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setActiveTab(Tab tab) {
        this.mController.setActiveTab(tab);
    }

    public void setOnThumbnailUpdatedListener(OnThumbnailUpdatedListener onThumbnailUpdatedListener) {
        this.mOnThumbnailUpdatedListener = onThumbnailUpdatedListener;
        Iterator<Tab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            Tab next = it.next();
            WebView webView = next.getWebView();
            if (webView != null) {
                if (onThumbnailUpdatedListener == null) {
                    next = null;
                }
                webView.setPictureListener(next);
            }
        }
    }

    public OnThumbnailUpdatedListener getOnThumbnailUpdatedListener() {
        return this.mOnThumbnailUpdatedListener;
    }

    public void setOnTabCountChangedListener(OnTabCountChangedListener onTabCountChangedListener) {
        this.mOnTabCountChangedListener = onTabCountChangedListener;
    }
}
