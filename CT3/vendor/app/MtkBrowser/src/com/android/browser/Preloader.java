package com.android.browser;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import java.util.Map;
/* loaded from: b.zip:com/android/browser/Preloader.class */
public class Preloader {
    private static Preloader sInstance;
    private final Context mContext;
    private final BrowserWebViewFactory mFactory;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private volatile PreloaderSession mSession = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/Preloader$PreloaderSession.class */
    public class PreloaderSession {
        private final String mId;
        private final PreloadedTabControl mTabControl;
        private final Runnable mTimeoutTask = new Runnable(this) { // from class: com.android.browser.Preloader.PreloaderSession.1
            final PreloaderSession this$1;

            {
                this.this$1 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                Log.d("browser.preloader", "Preload session timeout " + this.this$1.mId);
                this.this$1.this$0.discardPreload(this.this$1.mId);
            }
        };
        final Preloader this$0;

        public PreloaderSession(Preloader preloader, String str) {
            this.this$0 = preloader;
            this.mId = str;
            this.mTabControl = new PreloadedTabControl(new Tab(new PreloadController(preloader.mContext), preloader.mFactory.createWebView(false)));
            touch();
        }

        public void cancelTimeout() {
            this.this$0.mHandler.removeCallbacks(this.mTimeoutTask);
        }

        public PreloadedTabControl getTabControl() {
            return this.mTabControl;
        }

        public WebView getWebView() {
            WebView webView = null;
            Tab tab = this.mTabControl.getTab();
            if (tab != null) {
                webView = tab.getWebView();
            }
            return webView;
        }

        public void touch() {
            cancelTimeout();
            this.this$0.mHandler.postDelayed(this.mTimeoutTask, 30000L);
        }
    }

    private Preloader(Context context) {
        this.mContext = context.getApplicationContext();
        this.mFactory = new BrowserWebViewFactory(context);
    }

    public static Preloader getInstance() {
        return sInstance;
    }

    private PreloaderSession getSession(String str) {
        if (this.mSession == null) {
            Log.d("browser.preloader", "Create new preload session " + str);
            this.mSession = new PreloaderSession(this, str);
            WebViewTimersControl.getInstance().onPrerenderStart(this.mSession.getWebView());
            return this.mSession;
        } else if (this.mSession.mId.equals(str)) {
            Log.d("browser.preloader", "Returning existing preload session " + str);
            return this.mSession;
        } else {
            Log.d("browser.preloader", "Existing session in progress : " + this.mSession.mId + " returning null.");
            return null;
        }
    }

    public static void initialize(Context context) {
        sInstance = new Preloader(context);
    }

    private PreloaderSession takeSession(String str) {
        PreloaderSession preloaderSession = null;
        if (this.mSession != null) {
            preloaderSession = null;
            if (this.mSession.mId.equals(str)) {
                preloaderSession = this.mSession;
                this.mSession = null;
            }
        }
        if (preloaderSession != null) {
            preloaderSession.cancelTimeout();
        }
        return preloaderSession;
    }

    public void cancelSearchBoxPreload(String str) {
        PreloaderSession session = getSession(str);
        if (session != null) {
            session.touch();
            session.getTabControl().searchBoxCancel();
        }
    }

    public void discardPreload(String str) {
        PreloaderSession takeSession = takeSession(str);
        if (takeSession == null) {
            Log.d("browser.preloader", "Ignored discard request " + str);
            return;
        }
        Log.d("browser.preloader", "Discard preload session " + str);
        WebViewTimersControl.getInstance().onPrerenderDone(takeSession == null ? null : takeSession.getWebView());
        takeSession.getTabControl().destroy();
    }

    public PreloadedTabControl getPreloadedTab(String str) {
        PreloaderSession takeSession = takeSession(str);
        Log.d("browser.preloader", "Showing preload session " + str + "=" + takeSession);
        return takeSession == null ? null : takeSession.getTabControl();
    }

    public void handlePreloadRequest(String str, String str2, Map<String, String> map, String str3) {
        PreloaderSession session = getSession(str);
        if (session == null) {
            Log.d("browser.preloader", "Discarding preload request, existing session in progress");
            return;
        }
        session.touch();
        PreloadedTabControl tabControl = session.getTabControl();
        if (str3 == null) {
            tabControl.loadUrl(str2, map);
            return;
        }
        tabControl.loadUrlIfChanged(str2, map);
        tabControl.setQuery(str3);
    }
}
