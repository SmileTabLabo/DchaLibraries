package com.android.browser;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.CookieManager;
import com.android.browser.UI;
import com.android.browser.search.SearchEngine;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
/* loaded from: b.zip:com/android/browser/IntentHandler.class */
public class IntentHandler {
    private static final boolean DEBUG = Browser.DEBUG;
    static final UrlData EMPTY_URL_DATA = new UrlData(null);
    private static final String[] SCHEME_WHITELIST = {"http", "https", "about", "file", "rtsp", "tel"};
    private static final String[] URI_WHITELIST = {"content://com.android.browser.site_navigation/websites", "content://com.android.browser.home/"};
    private Activity mActivity;
    private Controller mController;
    private BrowserSettings mSettings;
    private TabControl mTabControl;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/IntentHandler$UrlData.class */
    public static class UrlData {
        final boolean mDisableUrlOverride;
        final Map<String, String> mHeaders;
        final PreloadedTabControl mPreloadedTab;
        final String mSearchBoxQueryToSubmit;
        final String mUrl;

        /* JADX INFO: Access modifiers changed from: package-private */
        public UrlData(String str) {
            this.mUrl = str;
            this.mHeaders = null;
            this.mPreloadedTab = null;
            this.mSearchBoxQueryToSubmit = null;
            this.mDisableUrlOverride = false;
        }

        UrlData(String str, Map<String, String> map, Intent intent, PreloadedTabControl preloadedTabControl, String str2) {
            this.mUrl = str;
            this.mHeaders = map;
            this.mPreloadedTab = preloadedTabControl;
            this.mSearchBoxQueryToSubmit = str2;
            if (intent != null) {
                this.mDisableUrlOverride = intent.getBooleanExtra("disable_url_override", false);
            } else {
                this.mDisableUrlOverride = false;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public PreloadedTabControl getPreloadedTab() {
            return this.mPreloadedTab;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getSearchBoxQueryToSubmit() {
            return this.mSearchBoxQueryToSubmit;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean isEmpty() {
            boolean z = true;
            if (this.mUrl != null) {
                z = this.mUrl.length() == 0;
            }
            return z;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean isPreloaded() {
            return this.mPreloadedTab != null;
        }
    }

    public IntentHandler(Activity activity, Controller controller) {
        this.mActivity = activity;
        this.mController = controller;
        this.mTabControl = this.mController.getTabControl();
        this.mSettings = controller.getSettings();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x0184, code lost:
        if ("android.intent.action.WEB_SEARCH".equals(r0) != false) goto L15;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static UrlData getUrlDataFromIntent(Intent intent) {
        String str = "";
        HashMap hashMap = null;
        PreloadedTabControl preloadedTabControl = null;
        String str2 = null;
        if (intent != null) {
            str = "";
            hashMap = null;
            preloadedTabControl = null;
            str2 = null;
            if ((intent.getFlags() & 1048576) == 0) {
                String action = intent.getAction();
                if ("android.intent.action.VIEW".equals(action) || "android.nfc.action.NDEF_DISCOVERED".equals(action)) {
                    Uri data = intent.getData();
                    String str3 = null;
                    if (data != null) {
                        str3 = data.toString();
                    }
                    String smartUrlFilter = (str3 == null || str3.startsWith("content://")) ? str3 : UrlUtils.smartUrlFilter(intent.getData());
                    HashMap hashMap2 = null;
                    if (smartUrlFilter != null) {
                        hashMap2 = null;
                        if (smartUrlFilter.startsWith("http")) {
                            Bundle bundleExtra = intent.getBundleExtra("com.android.browser.headers");
                            hashMap2 = null;
                            if (bundleExtra != null) {
                                if (!bundleExtra.isEmpty()) {
                                    Iterator<String> it = bundleExtra.keySet().iterator();
                                    HashMap hashMap3 = new HashMap();
                                    while (true) {
                                        hashMap2 = hashMap3;
                                        if (!it.hasNext()) {
                                            break;
                                        }
                                        String next = it.next();
                                        hashMap3.put(next, bundleExtra.getString(next));
                                    }
                                } else {
                                    hashMap2 = null;
                                }
                            }
                        }
                    }
                    str = smartUrlFilter;
                    hashMap = hashMap2;
                    preloadedTabControl = null;
                    str2 = null;
                    if (intent.hasExtra("preload_id")) {
                        String stringExtra = intent.getStringExtra("preload_id");
                        str2 = intent.getStringExtra("searchbox_query");
                        preloadedTabControl = Preloader.getInstance().getPreloadedTab(stringExtra);
                        hashMap = hashMap2;
                        str = smartUrlFilter;
                    }
                } else {
                    if (!"android.intent.action.SEARCH".equals(action) && !"android.intent.action.MEDIA_SEARCH".equals(action)) {
                        str = "";
                        hashMap = null;
                        preloadedTabControl = null;
                        str2 = null;
                    }
                    String stringExtra2 = intent.getStringExtra("query");
                    str = stringExtra2;
                    hashMap = null;
                    preloadedTabControl = null;
                    str2 = null;
                    if (stringExtra2 != null) {
                        String smartUrlFilter2 = UrlUtils.smartUrlFilter(UrlUtils.fixUrl(stringExtra2));
                        str = smartUrlFilter2;
                        hashMap = null;
                        preloadedTabControl = null;
                        str2 = null;
                        if (smartUrlFilter2.contains("&source=android-browser-suggest&")) {
                            String str4 = null;
                            Bundle bundleExtra2 = intent.getBundleExtra("app_data");
                            if (bundleExtra2 != null) {
                                str4 = bundleExtra2.getString("source");
                            }
                            String str5 = str4;
                            if (TextUtils.isEmpty(str4)) {
                                str5 = "unknown";
                            }
                            str = smartUrlFilter2.replace("&source=android-browser-suggest&", "&source=android-" + str5 + "&");
                            hashMap = null;
                            preloadedTabControl = null;
                            str2 = null;
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            Log.d("browser", "IntentHandler.getUrlDataFromIntent----->url : " + str + " headers: " + hashMap);
        }
        return new UrlData(str, hashMap, intent, preloadedTabControl, str2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean handleWebSearchIntent(Activity activity, Controller controller, Intent intent) {
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchIntent()----->" + intent);
        }
        if (intent == null) {
            return false;
        }
        String str = null;
        String str2 = null;
        String action = intent.getAction();
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchIntent()----->action : " + action);
        }
        if ("android.intent.action.VIEW".equals(action)) {
            Uri data = intent.getData();
            if (data != null) {
                str2 = data.toString();
            }
            if (str2 != null && str2.startsWith("content://")) {
                return false;
            }
            str = str2;
            if (controller != null) {
                str = str2;
                if (intent.getBooleanExtra("inputUrl", false)) {
                    ((BaseUi) controller.getUi()).setInputUrlFlag(true);
                    Log.d("browser", "handleWebSearchIntent inputUrl setInputUrlFlag");
                    str = str2;
                }
            }
        } else if ("android.intent.action.SEARCH".equals(action) || "android.intent.action.MEDIA_SEARCH".equals(action) || "android.intent.action.WEB_SEARCH".equals(action)) {
            str = intent.getStringExtra("query");
        }
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchIntent()----->url : " + str);
        }
        return handleWebSearchRequest(activity, controller, str, intent.getBundleExtra("app_data"), intent.getStringExtra("intent_extra_data_key"));
    }

    /* JADX WARN: Type inference failed for: r0v20, types: [com.android.browser.IntentHandler$1] */
    private static boolean handleWebSearchRequest(Activity activity, Controller controller, String str, Bundle bundle, String str2) {
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchRequest()----->" + str);
        }
        if (str == null) {
            return false;
        }
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchRequest()----->inUrl : " + str + " extraData : " + str2);
        }
        String trim = UrlUtils.fixUrl(str).trim();
        if (TextUtils.isEmpty(trim) || Patterns.WEB_URL.matcher(trim).matches() || UrlUtils.ACCEPTED_URI_SCHEMA.matcher(trim).matches()) {
            return false;
        }
        ContentResolver contentResolver = activity.getContentResolver();
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchRequest()----->newUrl : " + trim);
        }
        if (controller == null || controller.getTabControl() == null || controller.getTabControl().getCurrentWebView() == null || !controller.getTabControl().getCurrentWebView().isPrivateBrowsingEnabled()) {
            new AsyncTask<Void, Void, Void>(contentResolver, trim) { // from class: com.android.browser.IntentHandler.1
                final ContentResolver val$cr;
                final String val$newUrl;

                {
                    this.val$cr = contentResolver;
                    this.val$newUrl = trim;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public Void doInBackground(Void... voidArr) {
                    com.android.browser.provider.Browser.addSearchUrl(this.val$cr, this.val$newUrl);
                    return null;
                }
            }.execute(new Void[0]);
        }
        SearchEngine searchEngine = BrowserSettings.getInstance().getSearchEngine();
        if (searchEngine == null) {
            return false;
        }
        searchEngine.startSearch(activity, trim, bundle, str2);
        return true;
    }

    private static boolean isForbiddenUri(Uri uri) {
        for (String str : URI_WHITELIST) {
            if (str.equals(uri.toString())) {
                return false;
            }
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        String lowerCase = scheme.toLowerCase(Locale.US);
        for (String str2 : SCHEME_WHITELIST) {
            if (str2.equals(lowerCase)) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onNewIntent(Intent intent) {
        Tab tabFromAppId;
        Tab tabFromAppId2;
        String cookie;
        Uri data = intent.getData();
        int dchaState = BenesseExtension.getDchaState();
        if (data != null && isForbiddenUri(data)) {
            Log.e("browser", "Aborting intent with forbidden uri, \"" + data + "\"");
            return;
        }
        if (DEBUG) {
            Log.d("browser", "IntentHandler.onNewIntent--->" + intent);
        }
        Tab currentTab = this.mTabControl.getCurrentTab();
        Tab tab = currentTab;
        if (currentTab == null) {
            tab = this.mTabControl.getTab(0);
            if (tab == null) {
                return;
            }
            this.mController.setActiveTab(tab);
        }
        String action = intent.getAction();
        if (DEBUG) {
            Log.d("browser", "IntentHandler.onNewIntent--->action: " + action);
        }
        int flags = intent.getFlags();
        if ("android.intent.action.MAIN".equals(action) || (1048576 & flags) != 0) {
            return;
        }
        if ("show_bookmarks".equals(action)) {
            if (dchaState == 0) {
                this.mController.bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
                return;
            }
            return;
        }
        ((SearchManager) this.mActivity.getSystemService("search")).stopSearch();
        if ("android.intent.action.VIEW".equals(action) || "android.nfc.action.NDEF_DISCOVERED".equals(action) || "android.intent.action.SEARCH".equals(action) || "android.intent.action.MEDIA_SEARCH".equals(action) || "android.intent.action.WEB_SEARCH".equals(action)) {
            if (data != null && (cookie = CookieManager.getInstance().getCookie(data.toString())) != null) {
                intent.putExtra("url-cookie", cookie);
            }
            if (data != null && (data.toString().startsWith("rtsp://") || data.toString().startsWith("tel:"))) {
                intent.setData(Uri.parse(data.toString().replaceAll(" ", "%20")));
                if (data.toString().startsWith("rtsp://")) {
                    intent.addFlags(268435456);
                }
                if (dchaState == 0) {
                    this.mActivity.startActivity(intent);
                }
            } else if (handleWebSearchIntent(this.mActivity, this.mController, intent)) {
            } else {
                UrlData urlDataFromIntent = getUrlDataFromIntent(intent);
                UrlData urlData = urlDataFromIntent;
                if (urlDataFromIntent.isEmpty()) {
                    urlData = new UrlData(this.mSettings.getHomePage());
                }
                if (intent.getBooleanExtra("create_new_tab", false) || urlData.isPreloaded()) {
                    if (dchaState == 0) {
                        this.mController.openTab(urlData);
                        return;
                    }
                    return;
                }
                String stringExtra = intent.getStringExtra("com.android.browser.application_id");
                if (DEBUG) {
                    Log.d("browser", "IntentHandler.onNewIntent--->appId: " + stringExtra);
                }
                if ("android.intent.action.VIEW".equals(action) && stringExtra != null && stringExtra.startsWith(this.mActivity.getPackageName()) && (tabFromAppId2 = this.mTabControl.getTabFromAppId(stringExtra)) != null && tabFromAppId2 == this.mController.getCurrentTab()) {
                    this.mController.switchToTab(tabFromAppId2);
                    if (dchaState == 0) {
                        this.mController.loadUrlDataIn(tabFromAppId2, urlData);
                    }
                } else if (!"android.intent.action.VIEW".equals(action) || this.mActivity.getPackageName().equals(stringExtra)) {
                    if (urlData.isEmpty() || !urlData.mUrl.startsWith("about:debug")) {
                        this.mController.dismissSubWindow(tab);
                        tab.setAppId(null);
                        if (dchaState == 0) {
                            this.mController.loadUrlDataIn(tab, urlData);
                        }
                    } else if ("about:debug.dumpmem".equals(urlData.mUrl)) {
                        new OutputMemoryInfo().execute(this.mTabControl, null);
                    } else if ("about:debug.dumpmem.file".equals(urlData.mUrl)) {
                        new OutputMemoryInfo().execute(this.mTabControl, this.mTabControl);
                    } else {
                        this.mSettings.toggleDebugSettings();
                    }
                } else if (!BrowserActivity.isTablet(this.mActivity) && !this.mSettings.allowAppTabs() && (tabFromAppId = this.mTabControl.getTabFromAppId(stringExtra)) != null) {
                    this.mController.reuseTab(tabFromAppId, urlData);
                } else {
                    if (DEBUG) {
                        Log.d("browser", "IntentHandler.onNewIntent--->urlData.mUrl: " + urlData.mUrl);
                    }
                    Tab findTabWithUrl = this.mTabControl.findTabWithUrl(urlData.mUrl);
                    if (findTabWithUrl != null) {
                        findTabWithUrl.setAppId(stringExtra);
                        if (tab != findTabWithUrl) {
                            this.mController.switchToTab(findTabWithUrl);
                        }
                        if (dchaState == 0) {
                            this.mController.loadUrlDataIn(findTabWithUrl, urlData);
                            return;
                        }
                        return;
                    }
                    Tab tab2 = null;
                    if (dchaState == 0) {
                        tab2 = this.mController.openTab(urlData);
                    }
                    if (tab2 != null) {
                        tab2.setAppId(stringExtra);
                        if ((intent.getFlags() & 4194304) != 0) {
                            tab2.setCloseOnBack(true);
                        }
                    }
                }
            }
        }
    }
}
