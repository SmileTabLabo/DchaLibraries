package com.android.browser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;
import com.mediatek.browser.ext.IBrowserUrlExt;
import java.net.URISyntaxException;
import java.util.List;
/* loaded from: classes.dex */
public class UrlHandler {
    Activity mActivity;
    Controller mController;
    private static final boolean DEBUG = Browser.DEBUG;
    static final Uri RLZ_PROVIDER_URI = Uri.parse("content://com.google.android.partnersetup.rlzappprovider/");
    private static final String[] ACCEPTABLE_WEBSITE_SCHEMES = {"http:", "https:", "about:", "data:", "javascript:", "file:", "content:", "rtsp:"};
    private Boolean mIsProviderPresent = null;
    private Uri mRlzUri = null;
    private IBrowserUrlExt mBrowserUrlExt = null;

    public UrlHandler(Controller controller) {
        this.mController = controller;
        this.mActivity = this.mController.getActivity();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldOverrideUrlLoading(Tab tab, WebView webView, String str) {
        if (DEBUG) {
            Log.d("browser", "UrlHandler.shouldOverrideUrlLoading--->url = " + str);
        }
        if (webView.isPrivateBrowsingEnabled()) {
            return false;
        }
        String replaceAll = str.replaceAll(" ", "%20");
        if (DEBUG) {
            Log.d("browser", "UrlHandler.shouldOverrideUrlLoading--->new url = " + replaceAll);
        }
        if (replaceAll.startsWith("rtsp:")) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(replaceAll));
            intent.addFlags(268435456);
            this.mActivity.startActivity(intent);
            this.mController.closeEmptyTab();
            return true;
        }
        if (replaceAll.startsWith("wtai://wp/")) {
            if (replaceAll.startsWith("wtai://wp/mc;")) {
                this.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("tel:" + replaceAll.substring("wtai://wp/mc;".length()))));
                this.mController.closeEmptyTab();
                return true;
            } else if (replaceAll.startsWith("wtai://wp/sd;") || replaceAll.startsWith("wtai://wp/ap;")) {
                return false;
            }
        }
        if (replaceAll.startsWith("about:")) {
            return false;
        }
        if (rlzProviderPresent()) {
            Uri parse = Uri.parse(replaceAll);
            if (needsRlzString(parse)) {
                new RLZTask(tab, parse, webView).execute(new Void[0]);
                return true;
            }
        }
        this.mBrowserUrlExt = Extensions.getUrlPlugin(this.mActivity);
        return this.mBrowserUrlExt.redirectCustomerUrl(replaceAll) || startActivityForUrl(tab, replaceAll) || replaceAll.startsWith("ctrip://") || handleMenuClick(tab, replaceAll);
    }

    boolean startActivityForUrl(Tab tab, String str) {
        if (DEBUG) {
            Log.d("browser", "UrlHandler.startActivityForUrl--->url = " + str);
        }
        try {
            Intent parseUri = Intent.parseUri(str, 1);
            try {
                if (this.mActivity.getPackageManager().resolveActivity(parseUri, 0) == null) {
                    if (str != null && str.startsWith("mailto:")) {
                        Toast.makeText(this.mActivity, (int) R.string.need_login_email, 1).show();
                        return true;
                    } else if (str.startsWith("uber:")) {
                        Log.d("browser", "UrlHandler.startActivityForUrl--->uber2 ");
                        return true;
                    } else {
                        String str2 = parseUri.getPackage();
                        if (DEBUG) {
                            Log.d("browser", "UrlHandler.startActivityForUrl--->packagename = " + str2);
                        }
                        if (str2 != null) {
                            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://search?q=pname:" + str2));
                            intent.addCategory("android.intent.category.BROWSABLE");
                            try {
                                this.mActivity.startActivity(intent);
                                this.mController.closeEmptyTab();
                                return true;
                            } catch (ActivityNotFoundException e) {
                                if (DEBUG) {
                                    Log.w("Browser", "No activity found to handle " + str);
                                }
                                return true;
                            }
                        }
                        Log.d("browser", "UrlHandler.startActivityForUrl--->url3: " + str);
                        return !urlHasAcceptableScheme(str);
                    }
                }
                parseUri.addCategory("android.intent.category.BROWSABLE");
                parseUri.setComponent(null);
                Intent selector = parseUri.getSelector();
                if (selector != null) {
                    selector.addCategory("android.intent.category.BROWSABLE");
                    selector.setComponent(null);
                }
                if (tab != null) {
                    if (tab.getAppId() == null) {
                        if (DEBUG) {
                            Log.d("browser", "UrlHandler.startActivityForUrl--->tabId = " + tab.getId());
                        }
                        tab.setAppId(this.mActivity.getPackageName() + "-" + tab.getId());
                    }
                    parseUri.putExtra("com.android.browser.application_id", tab.getAppId());
                }
                if (UrlUtils.ACCEPTED_URI_SCHEMA_FOR_URLHANDLER.matcher(str).matches() && !isSpecializedHandlerAvailable(parseUri)) {
                    return false;
                }
                if (str != null && str.startsWith("https://www.google.com/calendar/event?")) {
                    if (DEBUG) {
                        Log.i("Browser", "url is sent by google calendar to show event detail, use Browser to show event detail, url:" + str);
                    }
                    return false;
                }
                try {
                    if (urlHasAcceptableScheme(str)) {
                        parseUri.setComponent(this.mActivity.getComponentName());
                    }
                    parseUri.putExtra("disable_url_override", true);
                    if (this.mActivity.startActivityIfNeeded(parseUri, -1)) {
                        this.mController.closeEmptyTab();
                        return true;
                    }
                } catch (ActivityNotFoundException e2) {
                }
                return false;
            } catch (Exception e3) {
                if (!str.startsWith("uber:")) {
                    return false;
                }
                Log.d("browser", "UrlHandler.startActivityForUrl--->uber ");
                return true;
            }
        } catch (URISyntaxException e4) {
            if (DEBUG) {
                Log.w("Browser", "Bad URI " + str + ": " + e4.getMessage());
            }
            return false;
        }
    }

    private static boolean urlHasAcceptableScheme(String str) {
        if (DEBUG) {
            Log.d("browser", "UrlHandler.urlHasAcceptableScheme--->url = " + str);
        }
        if (str == null) {
            return false;
        }
        for (int i = 0; i < ACCEPTABLE_WEBSITE_SCHEMES.length; i++) {
            if (str.startsWith(ACCEPTABLE_WEBSITE_SCHEMES[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpecializedHandlerAvailable(Intent intent) {
        List<ResolveInfo> queryIntentActivities = this.mActivity.getPackageManager().queryIntentActivities(intent, 64);
        if (queryIntentActivities == null || queryIntentActivities.size() == 0) {
            return false;
        }
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            IntentFilter intentFilter = resolveInfo.filter;
            if (intentFilter != null && (intentFilter.countDataAuthorities() != 0 || intentFilter.countDataPaths() != 0)) {
                return true;
            }
        }
        return false;
    }

    boolean handleMenuClick(Tab tab, String str) {
        if (DEBUG) {
            Log.d("browser", "UrlHandler.handleMenuClick()--->tab = " + tab + ", url = " + str);
        }
        boolean z = false;
        if (this.mController.isMenuDown()) {
            Controller controller = this.mController;
            if (tab != null && tab.isPrivateBrowsingEnabled()) {
                z = true;
            }
            controller.openTab(str, z, !BrowserSettings.getInstance().openInBackground(), true);
            this.mActivity.closeOptionsMenu();
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class RLZTask extends AsyncTask<Void, Void, String> {
        private Uri mSiteUri;
        private Tab mTab;
        private WebView mWebView;

        public RLZTask(Tab tab, Uri uri, WebView webView) {
            this.mTab = tab;
            this.mSiteUri = uri;
            this.mWebView = webView;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String doInBackground(Void... voidArr) {
            Cursor cursor;
            String uri = this.mSiteUri.toString();
            try {
                cursor = UrlHandler.this.mActivity.getContentResolver().query(UrlHandler.this.getRlzUri(), null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst() && !cursor.isNull(0)) {
                            uri = this.mSiteUri.buildUpon().appendQueryParameter("rlz", cursor.getString(0)).build().toString();
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return uri;
            } catch (Throwable th2) {
                th = th2;
                cursor = null;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String str) {
            if (!UrlHandler.this.mController.isActivityPaused() && UrlHandler.this.mController.getTabControl().getTabPosition(this.mTab) != -1 && !UrlHandler.this.startActivityForUrl(this.mTab, str) && !UrlHandler.this.handleMenuClick(this.mTab, str)) {
                UrlHandler.this.mController.loadUrl(this.mTab, str);
            }
        }
    }

    private boolean rlzProviderPresent() {
        if (this.mIsProviderPresent == null) {
            this.mIsProviderPresent = Boolean.valueOf(this.mActivity.getPackageManager().resolveContentProvider("com.google.android.partnersetup.rlzappprovider", 0) != null);
        }
        return this.mIsProviderPresent.booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Uri getRlzUri() {
        if (this.mRlzUri == null) {
            this.mRlzUri = Uri.withAppendedPath(RLZ_PROVIDER_URI, this.mActivity.getResources().getString(R.string.rlz_access_point));
        }
        if (DEBUG) {
            Log.d("browser", "UrlHandler.getRlzUri--->mRlzUri = " + this.mRlzUri);
        }
        return this.mRlzUri;
    }

    private static boolean needsRlzString(Uri uri) {
        String host;
        String scheme = uri.getScheme();
        if (("http".equals(scheme) || "https".equals(scheme)) && uri.isHierarchical() && uri.getQueryParameter("q") != null && uri.getQueryParameter("rlz") == null && (host = uri.getHost()) != null) {
            String[] split = host.split("\\.");
            if (split.length < 2) {
                return false;
            }
            int length = split.length - 2;
            String str = split[length];
            if (!"google".equals(str)) {
                if (split.length < 3 || !("co".equals(str) || "com".equals(str))) {
                    return false;
                }
                length = split.length - 3;
                if (!"google".equals(split[length])) {
                    return false;
                }
            }
            return length <= 0 || !"corp".equals(split[length - 1]);
        }
        return false;
    }
}
