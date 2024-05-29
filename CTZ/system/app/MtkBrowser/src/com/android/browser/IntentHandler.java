package com.android.browser;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.CookieManager;
import com.android.browser.UI;
import com.android.browser.search.SearchEngine;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/* loaded from: classes.dex */
public class IntentHandler {
    private static final boolean DEBUG = Browser.DEBUG;
    static final UrlData EMPTY_URL_DATA = new UrlData(null);
    private static final String[] SCHEME_WHITELIST = {"http", "https", "about", "file", "rtsp", "tel"};
    private static final String[] URI_WHITELIST = {"content://com.android.browser.site_navigation/websites", "content://com.android.browser.home/"};
    private Activity mActivity;
    private Controller mController;
    private BrowserSettings mSettings;
    private TabControl mTabControl;

    public IntentHandler(Activity activity, Controller controller) {
        this.mActivity = activity;
        this.mController = controller;
        this.mTabControl = this.mController.getTabControl();
        this.mSettings = controller.getSettings();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onNewIntent(Intent intent) {
        Tab tabFromAppId;
        Tab tabFromAppId2;
        String cookie;
        Uri data = intent.getData();
        if (data != null && isForbiddenUri(data)) {
            if (DEBUG) {
                Log.e("browser", "Aborting intent with forbidden uri, \"" + data + "\"");
                return;
            }
            return;
        }
        if (DEBUG) {
            Log.d("browser", "IntentHandler.onNewIntent--->" + intent);
        }
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab == null) {
            currentTab = this.mTabControl.getTab(0);
            if (currentTab == null) {
                return;
            }
            this.mController.setActiveTab(currentTab);
        }
        String action = intent.getAction();
        if (DEBUG) {
            Log.d("browser", "IntentHandler.onNewIntent--->action: " + action);
        }
        int flags = intent.getFlags();
        if ("android.intent.action.MAIN".equals(action) || (flags & 1048576) != 0) {
            return;
        }
        if ("show_bookmarks".equals(action)) {
            this.mController.bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
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
                this.mActivity.startActivity(intent);
            } else if (handleWebSearchIntent(this.mActivity, this.mController, intent)) {
            } else {
                UrlData urlDataFromIntent = getUrlDataFromIntent(intent);
                if (urlDataFromIntent.isEmpty()) {
                    urlDataFromIntent = new UrlData(this.mSettings.getHomePage());
                }
                if (intent.getBooleanExtra("create_new_tab", false) || urlDataFromIntent.isPreloaded()) {
                    this.mController.openTab(urlDataFromIntent);
                    return;
                }
                String stringExtra = intent.getStringExtra("com.android.browser.application_id");
                if (DEBUG) {
                    Log.d("browser", "IntentHandler.onNewIntent--->appId: " + stringExtra);
                }
                if ("android.intent.action.VIEW".equals(action) && stringExtra != null && stringExtra.startsWith(this.mActivity.getPackageName()) && (tabFromAppId2 = this.mTabControl.getTabFromAppId(stringExtra)) != null && tabFromAppId2 == this.mController.getCurrentTab()) {
                    this.mController.switchToTab(tabFromAppId2);
                    this.mController.loadUrlDataIn(tabFromAppId2, urlDataFromIntent);
                } else if ("android.intent.action.VIEW".equals(action) && !this.mActivity.getPackageName().equals(stringExtra)) {
                    if (!BrowserActivity.isTablet(this.mActivity) && !this.mSettings.allowAppTabs() && (tabFromAppId = this.mTabControl.getTabFromAppId(stringExtra)) != null) {
                        this.mController.reuseTab(tabFromAppId, urlDataFromIntent);
                        return;
                    }
                    if (DEBUG) {
                        Log.d("browser", "IntentHandler.onNewIntent--->urlData.mUrl: " + urlDataFromIntent.mUrl);
                    }
                    Tab findTabWithUrl = this.mTabControl.findTabWithUrl(urlDataFromIntent.mUrl);
                    if (findTabWithUrl != null) {
                        findTabWithUrl.setAppId(stringExtra);
                        if (currentTab != findTabWithUrl) {
                            this.mController.switchToTab(findTabWithUrl);
                        }
                        this.mController.loadUrlDataIn(findTabWithUrl, urlDataFromIntent);
                        return;
                    }
                    Tab openTab = this.mController.openTab(urlDataFromIntent);
                    if (openTab != null) {
                        openTab.setAppId(stringExtra);
                        if ((intent.getFlags() & 4194304) != 0) {
                            openTab.setCloseOnBack(true);
                        }
                    }
                } else if (!urlDataFromIntent.isEmpty() && urlDataFromIntent.mUrl.startsWith("about:debug")) {
                    if ("about:debug.dumpmem".equals(urlDataFromIntent.mUrl)) {
                        new OutputMemoryInfo().execute(this.mTabControl, null);
                    } else if ("about:debug.dumpmem.file".equals(urlDataFromIntent.mUrl)) {
                        new OutputMemoryInfo().execute(this.mTabControl, this.mTabControl);
                    } else {
                        this.mSettings.toggleDebugSettings();
                    }
                } else {
                    this.mController.dismissSubWindow(currentTab);
                    currentTab.setAppId(null);
                    this.mController.loadUrlDataIn(currentTab, urlDataFromIntent);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Removed duplicated region for block: B:58:0x0111  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static UrlData getUrlDataFromIntent(Intent intent) {
        String str;
        HashMap hashMap;
        PreloadedTabControl preloadedTabControl;
        String str2;
        String str3;
        HashMap hashMap2;
        String str4;
        Bundle bundleExtra;
        String str5;
        String str6 = "";
        PreloadedTabControl preloadedTabControl2 = null;
        if (intent != null && (intent.getFlags() & 1048576) == 0) {
            String action = intent.getAction();
            if ("android.intent.action.VIEW".equals(action) || "android.nfc.action.NDEF_DISCOVERED".equals(action)) {
                Uri data = intent.getData();
                if (data != null) {
                    str3 = data.toString();
                } else {
                    str3 = null;
                }
                if (str3 != null && !str3.startsWith("content://")) {
                    str3 = UrlUtils.smartUrlFilter(intent.getData());
                }
                if (str3 != null && str3.startsWith("http") && (bundleExtra = intent.getBundleExtra("com.android.browser.headers")) != null && !bundleExtra.isEmpty()) {
                    hashMap2 = new HashMap();
                    for (String str7 : bundleExtra.keySet()) {
                        hashMap2.put(str7, bundleExtra.getString(str7));
                    }
                } else {
                    hashMap2 = null;
                }
                if (intent.hasExtra("preload_id")) {
                    String stringExtra = intent.getStringExtra("preload_id");
                    str4 = intent.getStringExtra("searchbox_query");
                    preloadedTabControl2 = Preloader.getInstance().getPreloadedTab(stringExtra);
                } else {
                    str4 = null;
                }
                str = str3;
                preloadedTabControl = preloadedTabControl2;
                str2 = str4;
                hashMap = hashMap2;
                if (DEBUG) {
                    Log.d("browser", "IntentHandler.getUrlDataFromIntent----->url : " + str + " headers: " + hashMap);
                }
                return new UrlData(str, hashMap, intent, preloadedTabControl, str2);
            } else if (("android.intent.action.SEARCH".equals(action) || "android.intent.action.MEDIA_SEARCH".equals(action) || "android.intent.action.WEB_SEARCH".equals(action)) && (str6 = intent.getStringExtra("query")) != null) {
                str6 = UrlUtils.smartUrlFilter(UrlUtils.fixUrl(str6));
                if (str6.contains("&source=android-browser-suggest&")) {
                    Bundle bundleExtra2 = intent.getBundleExtra("app_data");
                    if (bundleExtra2 != null) {
                        str5 = bundleExtra2.getString("source");
                    } else {
                        str5 = null;
                    }
                    if (TextUtils.isEmpty(str5)) {
                        str5 = "unknown";
                    }
                    str6 = str6.replace("&source=android-browser-suggest&", "&source=android-" + str5 + "&");
                }
            }
        }
        str = str6;
        hashMap = null;
        preloadedTabControl = null;
        str2 = null;
        if (DEBUG) {
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
        String action = intent.getAction();
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchIntent()----->action : " + action);
        }
        if ("android.intent.action.VIEW".equals(action)) {
            Uri data = intent.getData();
            r1 = data != null ? data.toString() : null;
            if (r1 != null && r1.startsWith("content://")) {
                return false;
            }
            if (controller != null && intent.getBooleanExtra("inputUrl", false)) {
                ((BaseUi) controller.getUi()).setInputUrlFlag(true);
                Log.d("browser", "handleWebSearchIntent inputUrl setInputUrlFlag");
            }
        } else if ("android.intent.action.SEARCH".equals(action) || "android.intent.action.MEDIA_SEARCH".equals(action) || "android.intent.action.WEB_SEARCH".equals(action)) {
            r1 = intent.getStringExtra("query");
        }
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchIntent()----->url : " + r1);
        }
        return handleWebSearchRequest(activity, controller, r1, intent.getBundleExtra("app_data"), intent.getStringExtra("intent_extra_data_key"));
    }

    /* JADX WARN: Type inference failed for: r6v1, types: [com.android.browser.IntentHandler$1] */
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
        final String trim = UrlUtils.fixUrl(str).trim();
        if (TextUtils.isEmpty(trim) || Patterns.WEB_URL.matcher(trim).matches() || UrlUtils.ACCEPTED_URI_SCHEMA.matcher(trim).matches()) {
            return false;
        }
        final ContentResolver contentResolver = activity.getContentResolver();
        if (DEBUG) {
            Log.d("browser", "IntentHandler.handleWebSearchRequest()----->newUrl : " + trim);
        }
        if (controller == null || controller.getTabControl() == null || controller.getTabControl().getCurrentWebView() == null || !controller.getTabControl().getCurrentWebView().isPrivateBrowsingEnabled()) {
            new AsyncTask<Void, Void, Void>() { // from class: com.android.browser.IntentHandler.1
                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public Void doInBackground(Void... voidArr) {
                    com.android.browser.provider.Browser.addSearchUrl(contentResolver, trim);
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
    /* loaded from: classes.dex */
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
        public boolean isEmpty() {
            return this.mUrl == null || this.mUrl.length() == 0;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public boolean isPreloaded() {
            return this.mPreloadedTab != null;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public PreloadedTabControl getPreloadedTab() {
            return this.mPreloadedTab;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public String getSearchBoxQueryToSubmit() {
            return this.mSearchBoxQueryToSubmit;
        }
    }
}
