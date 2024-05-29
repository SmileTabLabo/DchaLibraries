package com.android.browser;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import com.android.browser.BrowserHistoryPage;
import com.android.browser.WebStorageSizeManager;
import com.android.browser.provider.BrowserProvider;
import com.android.browser.search.SearchEngine;
import com.android.browser.search.SearchEngines;
import com.mediatek.browser.ext.IBrowserFeatureIndexExt;
import com.mediatek.browser.ext.IBrowserSettingExt;
import com.mediatek.custom.CustomProperties;
import com.mediatek.search.SearchEngineManager;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;
/* loaded from: b.zip:com/android/browser/BrowserSettings.class */
public class BrowserSettings implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static String sFactoryResetUrl;
    private static BrowserSettings sInstance;
    private String mAppCachePath;
    private Context mContext;
    private Controller mController;
    private SharedPreferences mPrefs;
    private SearchEngine mSearchEngine;
    private WebStorageSizeManager mWebStorageSizeManager;
    private static final String[] USER_AGENTS = {null, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7", "Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B367 Safari/531.21.10", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", "Mozilla/5.0 (Linux; U; Android 3.1; en-us; Xoom Build/HMJ25) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13"};
    private static final boolean DEBUG = Browser.DEBUG;
    private static boolean sInitialized = false;
    private static IBrowserSettingExt sBrowserSettingExt = null;
    private boolean mNeedsSharedSync = true;
    private float mFontSizeMult = 1.0f;
    private boolean mLinkPrefetchAllowed = true;
    private int mPageCacheCapacity = 1;
    private Runnable mSetup = new Runnable(this) { // from class: com.android.browser.BrowserSettings.1

        /* renamed from: -android-webkit-WebSettings$TextSizeSwitchesValues  reason: not valid java name */
        private static final int[] f0androidwebkitWebSettings$TextSizeSwitchesValues = null;
        final BrowserSettings this$0;

        /* renamed from: -getandroid-webkit-WebSettings$TextSizeSwitchesValues  reason: not valid java name */
        private static /* synthetic */ int[] m98getandroidwebkitWebSettings$TextSizeSwitchesValues() {
            if (f0androidwebkitWebSettings$TextSizeSwitchesValues != null) {
                return f0androidwebkitWebSettings$TextSizeSwitchesValues;
            }
            int[] iArr = new int[WebSettings.TextSize.values().length];
            try {
                iArr[WebSettings.TextSize.LARGER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[WebSettings.TextSize.LARGEST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[WebSettings.TextSize.NORMAL.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[WebSettings.TextSize.SMALLER.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[WebSettings.TextSize.SMALLEST.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            f0androidwebkitWebSettings$TextSizeSwitchesValues = iArr;
            return iArr;
        }

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            DisplayMetrics displayMetrics = this.this$0.mContext.getResources().getDisplayMetrics();
            this.this$0.mFontSizeMult = displayMetrics.scaledDensity / displayMetrics.density;
            if (ActivityManager.staticGetMemoryClass() > 16) {
                this.this$0.mPageCacheCapacity = 5;
            }
            this.this$0.mWebStorageSizeManager = new WebStorageSizeManager(this.this$0.mContext, new WebStorageSizeManager.StatFsDiskInfo(this.this$0.getAppCachePath()), new WebStorageSizeManager.WebKitAppCacheInfo(this.this$0.getAppCachePath()));
            this.this$0.mPrefs.registerOnSharedPreferenceChangeListener(this.this$0);
            if (Build.VERSION.CODENAME.equals("REL")) {
                this.this$0.setDebugEnabled(false);
            }
            if (this.this$0.mPrefs.contains("text_size")) {
                switch (m98getandroidwebkitWebSettings$TextSizeSwitchesValues()[this.this$0.getTextSize().ordinal()]) {
                    case 1:
                        this.this$0.setTextZoom(IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_BANDWIDTH);
                        break;
                    case 2:
                        this.this$0.setTextZoom(200);
                        break;
                    case 3:
                        this.this$0.setTextZoom(75);
                        break;
                    case 4:
                        this.this$0.setTextZoom(50);
                        break;
                }
                this.this$0.mPrefs.edit().remove("text_size").apply();
            }
            IBrowserSettingExt unused = BrowserSettings.sBrowserSettingExt = Extensions.getSettingPlugin(this.this$0.mContext);
            String unused2 = BrowserSettings.sFactoryResetUrl = BrowserSettings.sBrowserSettingExt.getCustomerHomepage();
            if (BrowserSettings.sFactoryResetUrl == null) {
                String unused3 = BrowserSettings.sFactoryResetUrl = this.this$0.mContext.getResources().getString(2131493224);
                if (BrowserSettings.sFactoryResetUrl.indexOf("{CID}") != -1) {
                    String unused4 = BrowserSettings.sFactoryResetUrl = BrowserSettings.sFactoryResetUrl.replace("{CID}", BrowserProvider.getClientId(this.this$0.mContext.getContentResolver()));
                }
            }
            if (BrowserSettings.DEBUG) {
                Log.d("browser", "BrowserSettings.mSetup()--->run()--->sFactoryResetUrl : " + BrowserSettings.sFactoryResetUrl);
            }
            synchronized (BrowserSettings.class) {
                try {
                    boolean unused5 = BrowserSettings.sInitialized = true;
                    BrowserSettings.class.notifyAll();
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    };
    private LinkedList<WeakReference<WebSettings>> mManagedSettings = new LinkedList<>();
    private WeakHashMap<WebSettings, String> mCustomUserAgents = new WeakHashMap<>();

    private BrowserSettings(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        BackgroundHandler.execute(this.mSetup);
    }

    public static int getAdjustedMinimumFontSize(int i) {
        int i2 = i + 1;
        int i3 = i2;
        if (i2 > 1) {
            i3 = i2 + 3;
        }
        return i3;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getAppCachePath() {
        if (this.mAppCachePath == null) {
            this.mAppCachePath = this.mContext.getDir("appcache", 0).getPath();
        }
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.getAppCachePath()--->mAppCachePath:" + this.mAppCachePath);
        }
        return this.mAppCachePath;
    }

    public static String getFactoryResetHomeUrl(Context context) {
        requireInitialization();
        return sFactoryResetUrl;
    }

    public static String getFactoryResetUrlFromRes(Context context) {
        sBrowserSettingExt = Extensions.getSettingPlugin(context);
        sFactoryResetUrl = sBrowserSettingExt.getCustomerHomepage();
        if (sFactoryResetUrl == null) {
            sFactoryResetUrl = context.getResources().getString(2131493224);
        }
        if (sFactoryResetUrl.indexOf("{CID}") != -1) {
            sFactoryResetUrl = sFactoryResetUrl.replace("{CID}", BrowserProvider.getClientId(context.getContentResolver()));
        }
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.getFactoryResetUrlFromRes()--->sFactoryResetUrl : " + sFactoryResetUrl);
        }
        return sFactoryResetUrl;
    }

    public static BrowserSettings getInstance() {
        return sInstance;
    }

    public static String getLinkPrefetchAlwaysPreferenceString(Context context) {
        return context.getResources().getString(2131493192);
    }

    public static String getLinkPrefetchOnWifiOnlyPreferenceString(Context context) {
        return context.getResources().getString(2131493191);
    }

    public static String getPreloadAlwaysPreferenceString(Context context) {
        return context.getResources().getString(2131493186);
    }

    public static String getPreloadOnWifiOnlyPreferenceString(Context context) {
        return context.getResources().getString(2131493185);
    }

    static int getRawTextZoom(int i) {
        return ((i - 100) / 5) + 10;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Deprecated
    public WebSettings.TextSize getTextSize() {
        return WebSettings.TextSize.valueOf(this.mPrefs.getString("text_size", "NORMAL"));
    }

    public static void initialize(Context context) {
        sInstance = new BrowserSettings(context);
    }

    private static void requireInitialization() {
        synchronized (BrowserSettings.class) {
            while (!sInitialized) {
                try {
                    try {
                        BrowserSettings.class.wait();
                    } catch (InterruptedException e) {
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    private void resetCachedValues() {
        updateSearchEngine(false);
    }

    private void syncManagedSettings() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.syncManagedSettings()--->");
        }
        syncSharedSettings();
        synchronized (this.mManagedSettings) {
            Iterator<WeakReference<WebSettings>> it = this.mManagedSettings.iterator();
            while (it.hasNext()) {
                WebSettings webSettings = it.next().get();
                if (webSettings == null) {
                    it.remove();
                } else {
                    syncSetting(webSettings);
                }
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x00e6, code lost:
        if (r0.length() == 0) goto L23;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void syncSetting(WebSettings webSettings) {
        String str;
        boolean z = false;
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.syncSetting()--->");
        }
        webSettings.setGeolocationEnabled(enableGeolocation());
        webSettings.setJavaScriptEnabled(enableJavascript());
        webSettings.setLightTouchEnabled(enableLightTouch());
        webSettings.setNavDump(enableNavDump());
        webSettings.setDefaultTextEncodingName(getDefaultTextEncoding());
        webSettings.setMinimumFontSize(getMinimumFontSize());
        webSettings.setMinimumLogicalFontSize(getMinimumFontSize());
        webSettings.setPluginState(getPluginState());
        webSettings.setTextZoom(getTextZoom());
        webSettings.setDoubleTapZoom(getDoubleTapZoom());
        webSettings.setLayoutAlgorithm(getLayoutAlgorithm());
        if (!blockPopupWindows()) {
            z = true;
        }
        webSettings.setJavaScriptCanOpenWindowsAutomatically(z);
        webSettings.setLoadsImagesAutomatically(loadImages());
        webSettings.setLoadWithOverviewMode(loadPageInOverviewMode());
        webSettings.setSavePassword(rememberPasswords());
        webSettings.setSaveFormData(saveFormdata());
        webSettings.setUseWideViewPort(isWideViewport());
        sBrowserSettingExt = Extensions.getSettingPlugin(this.mContext);
        sBrowserSettingExt.setStandardFontFamily(webSettings, this.mPrefs);
        String str2 = this.mCustomUserAgents.get(webSettings);
        if (str2 != null) {
            webSettings.setUserAgentString(str2);
            return;
        }
        String string = CustomProperties.getString("browser", "UserAgent");
        if (string != null) {
            str = string;
        }
        String operatorUA = sBrowserSettingExt.getOperatorUA(webSettings.getUserAgentString());
        str = string;
        if (operatorUA != null) {
            str = string;
            if (operatorUA.length() > 0) {
                str = operatorUA;
            }
        }
        if (getUserAgent() != 0 || str == null) {
            webSettings.setUserAgentString(USER_AGENTS[getUserAgent()]);
        } else {
            webSettings.setUserAgentString(str);
        }
    }

    private void syncSharedSettings() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.syncSharedSettings()--->");
        }
        this.mNeedsSharedSync = false;
        CookieManager.getInstance().setAcceptCookie(acceptCookies());
        if (this.mController != null) {
            for (Tab tab : this.mController.getTabs()) {
                tab.setAcceptThirdPartyCookies(acceptCookies());
            }
            this.mController.setShouldShowErrorConsole(enableJavascriptConsole());
        }
    }

    private void syncStaticSettings(WebSettings webSettings) {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.syncStaticSettings()--->");
        }
        webSettings.setDefaultFontSize(16);
        webSettings.setDefaultFixedFontSize(13);
        webSettings.setNeedInitialFocus(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setEnableSmoothTransition(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheMaxSize(getWebStorageSizeManager().getAppCacheMaxSize());
        webSettings.setAppCachePath(getAppCachePath());
        webSettings.setDatabasePath(this.mContext.getDir("databases", 0).getPath());
        webSettings.setGeolocationDatabasePath(this.mContext.getDir("geolocation", 0).getPath());
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setMixedContentMode(2);
    }

    private void updateSearchEngine(boolean z) {
        String searchEngineName = getSearchEngineName();
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.updateSearchEngine()--->searchEngineName:" + searchEngineName);
        }
        if (z || this.mSearchEngine == null || searchEngineName == null || !this.mSearchEngine.getName().equals(searchEngineName)) {
            this.mSearchEngine = SearchEngines.get(this.mContext, searchEngineName);
        }
    }

    public boolean acceptCookies() {
        return this.mPrefs.getBoolean("accept_cookies", true);
    }

    public boolean allowAppTabs() {
        return this.mPrefs.getBoolean("allow_apptabs", false);
    }

    public boolean autofitPages() {
        return this.mPrefs.getBoolean("autofit_pages", true);
    }

    public boolean blockPopupWindows() {
        return this.mPrefs.getBoolean("block_popup_windows", true);
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x003b, code lost:
        if (r0.length() == 0) goto L22;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void changeUserAgent(WebView webView, boolean z) {
        String str;
        if (webView == null) {
            return;
        }
        WebSettings settings = webView.getSettings();
        if (z) {
            Log.i("Browser/Settings", "UA change to desktop");
            settings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24");
            return;
        }
        Log.i("Browser/Settings", "UA restore");
        if (this.mCustomUserAgents.get(settings) != null) {
            return;
        }
        String string = CustomProperties.getString("browser", "UserAgent");
        if (string != null) {
            str = string;
        }
        sBrowserSettingExt = Extensions.getSettingPlugin(this.mContext);
        String operatorUA = sBrowserSettingExt.getOperatorUA(settings.getUserAgentString());
        str = string;
        if (operatorUA != null) {
            str = string;
            if (operatorUA.length() > 0) {
                str = operatorUA;
            }
        }
        if (getUserAgent() != 0 || str == null) {
            settings.setUserAgentString(USER_AGENTS[getUserAgent()]);
        } else {
            settings.setUserAgentString(str);
        }
    }

    public void clearCache() {
        WebView currentWebView;
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.clearCache()--->");
        }
        WebIconDatabase.getInstance().removeAllIcons();
        if (this.mController == null || (currentWebView = this.mController.getCurrentWebView()) == null) {
            return;
        }
        currentWebView.clearCache(true);
    }

    public void clearCookies() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.clearCookies()--->");
        }
        CookieManager.getInstance().removeAllCookie();
    }

    public void clearDatabases() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.clearDatabases()--->");
        }
        WebStorage.getInstance().deleteAllData();
    }

    public void clearFormData() {
        WebView currentTopWebView;
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.clearFormData()--->");
        }
        WebViewDatabase.getInstance(this.mContext).clearFormData();
        if (this.mController == null || (currentTopWebView = this.mController.getCurrentTopWebView()) == null) {
            return;
        }
        currentTopWebView.clearFormData();
    }

    public void clearHistory() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.clearHistory()--->");
        }
        BrowserHistoryPage.ClearHistoryTask clearHistoryTask = new BrowserHistoryPage.ClearHistoryTask(this.mContext.getContentResolver());
        if (clearHistoryTask.isAlive()) {
            return;
        }
        clearHistoryTask.start();
    }

    public void clearLocationAccess() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.clearLocationAccess()--->");
        }
        GeolocationPermissions.getInstance().clearAll();
    }

    public void clearPasswords() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.clearPasswords()--->");
        }
        WebViewDatabase webViewDatabase = WebViewDatabase.getInstance(this.mContext);
        webViewDatabase.clearUsernamePassword();
        webViewDatabase.clearHttpAuthUsernamePassword();
    }

    public boolean enableGeolocation() {
        return this.mPrefs.getBoolean("enable_geolocation", true);
    }

    public boolean enableJavascript() {
        return this.mPrefs.getBoolean("enable_javascript", true);
    }

    public boolean enableJavascriptConsole() {
        if (isDebugEnabled()) {
            return this.mPrefs.getBoolean("javascript_console", true);
        }
        return false;
    }

    public boolean enableLightTouch() {
        if (isDebugEnabled()) {
            return this.mPrefs.getBoolean("enable_light_touch", false);
        }
        return false;
    }

    public boolean enableNavDump() {
        if (isDebugEnabled()) {
            return this.mPrefs.getBoolean("enable_nav_dump", false);
        }
        return false;
    }

    public int getAdjustedDoubleTapZoom(int i) {
        return (int) ((((i - 5) * 5) + 100) * this.mFontSizeMult);
    }

    public int getAdjustedTextZoom(int i) {
        return (int) ((((i - 10) * 5) + 100) * this.mFontSizeMult);
    }

    public String getDefaultDownloadPathWithMultiSDcard() {
        sBrowserSettingExt = Extensions.getSettingPlugin(this.mContext);
        if (DEBUG) {
            Log.d("browser", "Default Download Path:" + sBrowserSettingExt.getDefaultDownloadFolder());
        }
        return sBrowserSettingExt.getDefaultDownloadFolder();
    }

    public String getDefaultLinkPrefetchSetting() {
        String string = Settings.Secure.getString(this.mContext.getContentResolver(), "browser_default_link_prefetch_setting");
        String str = string;
        if (string == null) {
            str = this.mContext.getResources().getString(2131493193);
        }
        return str;
    }

    public String getDefaultPreloadSetting() {
        String string = Settings.Secure.getString(this.mContext.getContentResolver(), "browser_default_preload_setting");
        String str = string;
        if (string == null) {
            str = this.mContext.getResources().getString(2131493187);
        }
        return str;
    }

    public String getDefaultTextEncoding() {
        String string = this.mPrefs.getString("default_text_encoding", null);
        String str = string;
        if (TextUtils.isEmpty(string)) {
            str = this.mContext.getString(2131492867);
        }
        return str;
    }

    public int getDoubleTapZoom() {
        requireInitialization();
        return getAdjustedDoubleTapZoom(this.mPrefs.getInt("double_tap_zoom", 5));
    }

    public String getDownloadPath() {
        return this.mPrefs.getString("download_directory_setting", getDefaultDownloadPathWithMultiSDcard());
    }

    public String getHomePage() {
        return this.mPrefs.getString("homepage", getFactoryResetHomeUrl(this.mContext));
    }

    public String getJsEngineFlags() {
        return !isDebugEnabled() ? "" : this.mPrefs.getString("js_engine_flags", "");
    }

    public long getLastRecovered() {
        return this.mPrefs.getLong("last_recovered", 0L);
    }

    public WebSettings.LayoutAlgorithm getLayoutAlgorithm() {
        WebSettings.LayoutAlgorithm layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL;
        WebSettings.LayoutAlgorithm layoutAlgorithm2 = Build.VERSION.SDK_INT >= 19 ? WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING : WebSettings.LayoutAlgorithm.NARROW_COLUMNS;
        if (autofitPages()) {
            layoutAlgorithm = layoutAlgorithm2;
        }
        if (isDebugEnabled()) {
            layoutAlgorithm = isNormalLayout() ? WebSettings.LayoutAlgorithm.NORMAL : layoutAlgorithm2;
        }
        return layoutAlgorithm;
    }

    public String getLinkPrefetchEnabled() {
        return this.mPrefs.getString("link_prefetch_when", getDefaultLinkPrefetchSetting());
    }

    public int getMinimumFontSize() {
        return getAdjustedMinimumFontSize(this.mPrefs.getInt("min_font_size", 0));
    }

    public WebSettings.PluginState getPluginState() {
        return WebSettings.PluginState.valueOf(this.mPrefs.getString("plugin_state", "ON"));
    }

    public SharedPreferences getPreferences() {
        return this.mPrefs;
    }

    public String getPreloadEnabled() {
        return this.mPrefs.getString("preload_when", getDefaultPreloadSetting());
    }

    public SearchEngine getSearchEngine() {
        if (this.mSearchEngine == null) {
            updateSearchEngine(false);
        }
        return this.mSearchEngine;
    }

    public String getSearchEngineName() {
        SearchEngineManager searchEngineManager = (SearchEngineManager) this.mContext.getSystemService("search_engine");
        List availables = searchEngineManager.getAvailables();
        if (availables == null || availables.size() <= 0) {
            return null;
        }
        com.mediatek.common.search.SearchEngine searchEngine = searchEngineManager.getDefault();
        String name = searchEngine != null ? searchEngine.getName() : "google";
        sBrowserSettingExt = Extensions.getSettingPlugin(this.mContext);
        String searchEngine2 = sBrowserSettingExt.getSearchEngine(this.mPrefs, this.mContext);
        com.mediatek.common.search.SearchEngine byName = searchEngineManager.getByName(searchEngine2);
        String faviconUri = byName != null ? byName.getFaviconUri() : this.mPrefs.getString("search_engine_favicon", "");
        int i = -1;
        int size = availables.size();
        String[] strArr = new String[size];
        String[] strArr2 = new String[size];
        com.mediatek.common.search.SearchEngine bestMatch = searchEngineManager.getBestMatch("", faviconUri);
        boolean z = false;
        String str = searchEngine2;
        if (bestMatch != null) {
            z = false;
            str = searchEngine2;
            if (!searchEngine2.equals(bestMatch.getName())) {
                str = bestMatch.getName();
                z = true;
            }
        }
        for (int i2 = 0; i2 < size; i2++) {
            strArr[i2] = ((com.mediatek.common.search.SearchEngine) availables.get(i2)).getName();
            strArr2[i2] = ((com.mediatek.common.search.SearchEngine) availables.get(i2)).getFaviconUri();
            if (strArr[i2].equals(str)) {
                i = i2;
            }
        }
        boolean z2 = z;
        int i3 = i;
        if (i == -1) {
            i3 = 0;
            for (int i4 = 0; i4 < size; i4++) {
                if (strArr[i4].equals(name)) {
                    i3 = i4;
                }
            }
            z2 = true;
        }
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.getSearchEngineName-->selectedItem = " + i3 + "entryValues[" + i3 + "]=" + strArr);
        }
        if (z2 && i3 != -1) {
            SharedPreferences.Editor edit = this.mPrefs.edit();
            edit.putString("search_engine", strArr[i3]);
            edit.putString("search_engine_favicon", strArr2[i3]);
            edit.commit();
        }
        return strArr[i3];
    }

    public int getTextZoom() {
        requireInitialization();
        return getAdjustedTextZoom(this.mPrefs.getInt("text_zoom", 10));
    }

    public int getUserAgent() {
        if (isDebugEnabled()) {
            return Integer.parseInt(this.mPrefs.getString("user_agent", "0"));
        }
        return 0;
    }

    public WebStorageSizeManager getWebStorageSizeManager() {
        requireInitialization();
        return this.mWebStorageSizeManager;
    }

    public boolean hasDesktopUseragent(WebView webView) {
        boolean z = false;
        if (webView != null) {
            z = false;
            if (this.mCustomUserAgents.get(webView.getSettings()) != null) {
                z = true;
            }
        }
        return z;
    }

    public boolean isDebugEnabled() {
        requireInitialization();
        return this.mPrefs.getBoolean("debug_menu", false);
    }

    public boolean isDesktopUserAgent(WebView webView) {
        String userAgentString = webView.getSettings().getUserAgentString();
        if (userAgentString != null) {
            return userAgentString.equals("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24");
        }
        return false;
    }

    public boolean isHardwareAccelerated() {
        if (isDebugEnabled()) {
            return this.mPrefs.getBoolean("enable_hardware_accel", true);
        }
        return true;
    }

    public boolean isNormalLayout() {
        if (isDebugEnabled()) {
            return this.mPrefs.getBoolean("normal_layout", false);
        }
        return false;
    }

    public boolean isTracing() {
        if (isDebugEnabled()) {
            return this.mPrefs.getBoolean("enable_tracing", false);
        }
        return false;
    }

    public boolean isWideViewport() {
        if (isDebugEnabled()) {
            return this.mPrefs.getBoolean("wide_viewport", true);
        }
        return true;
    }

    public boolean loadImages() {
        return this.mPrefs.getBoolean("load_images", true);
    }

    public boolean loadPageInOverviewMode() {
        boolean z = this.mPrefs.getBoolean("load_page", true);
        Log.i("Browser/Settings", "loadMode: " + z);
        return z;
    }

    public void onConfigurationChanged(Configuration configuration) {
        updateSearchEngine(false);
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        syncManagedSettings();
        if ("search_engine".equals(str)) {
            updateSearchEngine(false);
        }
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.onSharedPreferenceChanged()--->" + str + " mControll is null:" + (this.mController == null));
        }
        if (this.mController == null) {
            return;
        }
        if ("fullscreen".equals(str)) {
            if (this.mController == null || this.mController.getUi() == null) {
                return;
            }
            this.mController.getUi().setFullscreen(useFullscreen());
        } else if ("enable_quick_controls".equals(str)) {
            if (this.mController == null || this.mController.getUi() == null) {
                return;
            }
            this.mController.getUi().setUseQuickControls(sharedPreferences.getBoolean(str, false));
        } else if ("link_prefetch_when".equals(str)) {
            updateConnectionType();
        } else if ("landscape_only".equals(str)) {
            sBrowserSettingExt = Extensions.getSettingPlugin(this.mContext);
            sBrowserSettingExt.setOnlyLandscape(sharedPreferences, this.mController.getActivity());
        }
    }

    public boolean openInBackground() {
        return this.mPrefs.getBoolean("open_in_background", false);
    }

    public boolean rememberPasswords() {
        return this.mPrefs.getBoolean("remember_passwords", true);
    }

    public void resetDefaultPreferences() {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.resetDefaultPreferences()--->");
        }
        this.mPrefs.edit().clear().putLong("last_autologin_time", this.mPrefs.getLong("last_autologin_time", -1L)).apply();
        resetCachedValues();
        syncManagedSettings();
    }

    public boolean saveFormdata() {
        return this.mPrefs.getBoolean("save_formdata", true);
    }

    public void setController(Controller controller) {
        this.mController = controller;
        if (sInitialized) {
            syncSharedSettings();
        }
        sBrowserSettingExt = Extensions.getSettingPlugin(this.mContext);
        sBrowserSettingExt.setOnlyLandscape(this.mPrefs, this.mController.getActivity());
    }

    public void setDebugEnabled(boolean z) {
        SharedPreferences.Editor edit = this.mPrefs.edit();
        edit.putBoolean("debug_menu", z);
        if (!z) {
            edit.putBoolean("enable_hardware_accel_skia", false);
        }
        edit.apply();
    }

    public void setHomePage(String str) {
        this.mPrefs.edit().putString("homepage", str).apply();
        Log.i("Browser/Settings", "BrowserSettings: setHomePage : " + str);
    }

    public void setHomePagePicker(String str) {
        this.mPrefs.edit().putString("homepage_picker", str).apply();
        Log.i("Browser/Settings", "BrowserSettings: setHomePagePicker : " + str);
    }

    public void setLastRecovered(long j) {
        this.mPrefs.edit().putLong("last_recovered", j).apply();
    }

    public void setLastRunPaused(boolean z) {
        this.mPrefs.edit().putBoolean("last_paused", z).apply();
    }

    public void setTextZoom(int i) {
        this.mPrefs.edit().putInt("text_zoom", getRawTextZoom(i)).apply();
    }

    public boolean showSecurityWarnings() {
        return this.mPrefs.getBoolean("show_security_warnings", true);
    }

    public void startManagingSettings(WebSettings webSettings) {
        if (this.mNeedsSharedSync) {
            syncSharedSettings();
        }
        synchronized (this.mManagedSettings) {
            syncStaticSettings(webSettings);
            syncSetting(webSettings);
            this.mManagedSettings.add(new WeakReference<>(webSettings));
        }
    }

    public void stopManagingSettings(WebSettings webSettings) {
        if (DEBUG) {
            Log.d("browser", "BrowserSettings.stopManagingSettings()--->");
        }
        Iterator<WeakReference<WebSettings>> it = this.mManagedSettings.iterator();
        while (it.hasNext()) {
            if (it.next().get() == webSettings) {
                it.remove();
                return;
            }
        }
    }

    public void toggleDebugSettings() {
        setDebugEnabled(!isDebugEnabled());
    }

    public void toggleDesktopUseragent(WebView webView) {
        if (webView == null) {
            return;
        }
        WebSettings settings = webView.getSettings();
        if (this.mCustomUserAgents.get(settings) != null) {
            this.mCustomUserAgents.remove(settings);
            settings.setUserAgentString(USER_AGENTS[getUserAgent()]);
            return;
        }
        this.mCustomUserAgents.put(settings, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24");
        settings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24");
    }

    public void updateConnectionType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        String linkPrefetchEnabled = getLinkPrefetchEnabled();
        boolean equals = linkPrefetchEnabled.equals(getLinkPrefetchAlwaysPreferenceString(this.mContext));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean z = equals;
        if (activeNetworkInfo != null) {
            switch (activeNetworkInfo.getType()) {
                case 1:
                case 7:
                case 9:
                    z = equals | linkPrefetchEnabled.equals(getLinkPrefetchOnWifiOnlyPreferenceString(this.mContext));
                    break;
                default:
                    z = equals;
                    break;
            }
        }
        if (this.mLinkPrefetchAllowed != z) {
            this.mLinkPrefetchAllowed = z;
            syncManagedSettings();
        }
    }

    public void updateSearchEngineSetting() {
        String searchEngine = Extensions.getRegionalPhonePlugin(this.mContext).getSearchEngine(this.mPrefs, this.mContext);
        if (searchEngine == null) {
            Log.i("Browser/Settings", "updateSearchEngineSetting ---no change");
            return;
        }
        com.mediatek.common.search.SearchEngine byName = ((SearchEngineManager) this.mContext.getSystemService("search_engine")).getByName(searchEngine);
        if (byName == null) {
            Log.i("Browser/Settings", "updateSearchEngineSetting ---" + searchEngine + " not found");
            return;
        }
        String faviconUri = byName.getFaviconUri();
        SharedPreferences.Editor edit = this.mPrefs.edit();
        edit.putString("search_engine", searchEngine);
        edit.putString("search_engine_favicon", faviconUri);
        edit.commit();
        Log.i("Browser/Settings", "updateSearchEngineSetting --" + searchEngine + "--" + faviconUri);
    }

    public boolean useFullscreen() {
        return this.mPrefs.getBoolean("fullscreen", false);
    }

    public boolean useMostVisitedHomepage() {
        return "content://com.android.browser.home/".equals(getHomePage());
    }

    public boolean useQuickControls() {
        return this.mPrefs.getBoolean("enable_quick_controls", false);
    }

    public boolean wasLastRunPaused() {
        return this.mPrefs.getBoolean("last_paused", false);
    }
}
