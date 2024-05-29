package com.android.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.webkit.ClientCertRequest;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.android.browser.DataController;
import com.android.browser.TabControl;
import com.android.browser.homepages.HomeProvider;
import com.android.browser.provider.SnapshotProvider;
import com.android.browser.sitenavigation.SiteNavigation;
import com.mediatek.browser.ext.IBrowserUrlExt;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: b.zip:com/android/browser/Tab.class */
public class Tab implements WebView.PictureListener {
    private static final boolean DEBUG = Browser.DEBUG;
    private static Paint sAlphaPaint = new Paint();
    private static Bitmap sDefaultFavicon;
    private SaveCallback callback;
    private String mAppId;
    private IBrowserUrlExt mBrowserUrlExt;
    private Bitmap mCapture;
    private int mCaptureHeight;
    private int mCaptureWidth;
    private Vector<Tab> mChildren;
    private boolean mCloseOnBack;
    private View mContainer;
    Context mContext;
    protected PageState mCurrentState;
    private DataController mDataController;
    private DeviceAccountLogin mDeviceAccountLogin;
    private DialogInterface.OnDismissListener mDialogListener;
    private boolean mDisableOverrideUrlLoading;
    private final BrowserDownloadListener mDownloadListener;
    private ErrorConsoleView mErrorConsole;
    private GeolocationPermissionsPrompt mGeolocationPermissionsPrompt;
    private Handler mHandler;
    private long mId;
    private boolean mInForeground;
    private boolean mInPageLoad;
    private DataController.OnQueryUrlIsBookmark mIsBookmarkCallback;
    private boolean mIsErrorDialogShown;
    private long mLoadStartTime;
    private WebView mMainView;
    private int mPageError;
    private int mPageLoadProgress;
    private Tab mParent;
    private PermissionsPrompt mPermissionsPrompt;
    private LinkedList<ErrorDialog> mQueuedErrors;
    HashMap<Integer, Long> mSavePageJob;
    public String mSavePageTitle;
    public String mSavePageUrl;
    private Bundle mSavedState;
    private BrowserSettings mSettings;
    private WebView mSubView;
    private View mSubViewContainer;
    private boolean mSubWindowShown;
    DownloadTouchIcon mTouchIconLoader;
    private boolean mUpdateThumbnail;
    private final WebBackForwardListClient mWebBackForwardListClient;
    private final WebChromeClient mWebChromeClient;
    private final WebViewClient mWebViewClient;
    protected WebViewController mWebViewController;
    private boolean mWillBeClosed;

    /* renamed from: com.android.browser.Tab$2  reason: invalid class name */
    /* loaded from: b.zip:com/android/browser/Tab$2.class */
    class AnonymousClass2 extends WebViewClient {
        private Message mDontResend;
        private Message mResend;
        final Tab this$0;

        AnonymousClass2(Tab tab) {
            this.this$0 = tab;
        }

        @Override // android.webkit.WebViewClient
        public void doUpdateVisitedHistory(WebView webView, String str, boolean z) {
            this.this$0.mWebViewController.doUpdateVisitedHistory(this.this$0, z);
        }

        @Override // android.webkit.WebViewClient
        public void onFormResubmission(WebView webView, Message message, Message message2) {
            if (!this.this$0.mInForeground) {
                message.sendToTarget();
            } else if (this.mDontResend != null) {
                Log.w("Tab", "onFormResubmission should not be called again while dialog is still up");
                message.sendToTarget();
            } else {
                this.mDontResend = message;
                this.mResend = message2;
                new AlertDialog.Builder(this.this$0.mContext).setTitle(2131493196).setMessage(2131493197).setPositiveButton(2131492963, new DialogInterface.OnClickListener(this) { // from class: com.android.browser.Tab.2.1
                    final AnonymousClass2 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (this.this$1.mResend != null) {
                            this.this$1.mResend.sendToTarget();
                            this.this$1.mResend = null;
                            this.this$1.mDontResend = null;
                        }
                    }
                }).setNegativeButton(2131492962, new DialogInterface.OnClickListener(this) { // from class: com.android.browser.Tab.2.2
                    final AnonymousClass2 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (this.this$1.mDontResend != null) {
                            this.this$1.mDontResend.sendToTarget();
                            this.this$1.mResend = null;
                            this.this$1.mDontResend = null;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener(this) { // from class: com.android.browser.Tab.2.3
                    final AnonymousClass2 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // android.content.DialogInterface.OnCancelListener
                    public void onCancel(DialogInterface dialogInterface) {
                        if (this.this$1.mDontResend != null) {
                            this.this$1.mDontResend.sendToTarget();
                            this.this$1.mResend = null;
                            this.this$1.mDontResend = null;
                        }
                    }
                }).show();
            }
        }

        @Override // android.webkit.WebViewClient
        public void onLoadResource(WebView webView, String str) {
            if (str == null || str.length() <= 0 || this.this$0.mCurrentState.mSecurityState != SecurityState.SECURITY_STATE_SECURE) {
                return;
            }
            if ((URLUtil.isHttpsUrl(str) || URLUtil.isDataUrl(str)) ? true : URLUtil.isAboutUrl(str)) {
                return;
            }
            this.this$0.mCurrentState.mSecurityState = SecurityState.SECURITY_STATE_MIXED;
        }

        @Override // android.webkit.WebViewClient
        public void onPageFinished(WebView webView, String str) {
            TabControl tabControl = this.this$0.mWebViewController.getTabControl();
            if (tabControl != null) {
                Log.d("browser", "[" + tabControl.getTabPosition(this.this$0) + "/" + tabControl.getTabCount() + "] onPageFinished url=" + str);
            }
            this.this$0.mDisableOverrideUrlLoading = false;
            if (!this.this$0.isPrivateBrowsingEnabled()) {
                LogTag.logPageFinishedLoading(str, SystemClock.uptimeMillis() - this.this$0.mLoadStartTime);
            }
            this.this$0.syncCurrentState(webView, str);
            if (this.this$0.mCurrentState.mIsDownload) {
                this.this$0.mCurrentState.mUrl = this.this$0.mCurrentState.mOriginalUrl;
                if (this.this$0.mCurrentState.mUrl == null) {
                    this.this$0.mCurrentState.mUrl = "";
                }
            }
            if (str != null && str.equals(this.this$0.mSavePageUrl)) {
                this.this$0.mCurrentState.mTitle = this.this$0.mSavePageTitle;
                this.this$0.mCurrentState.mUrl = this.this$0.mSavePageUrl;
            }
            if (str != null && str.startsWith("about:blank")) {
                this.this$0.mCurrentState.mFavicon = Tab.getDefaultFavicon(this.this$0.mContext);
            }
            this.this$0.mWebViewController.onPageFinished(this.this$0);
        }

        @Override // android.webkit.WebViewClient
        public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
            TabControl tabControl = this.this$0.mWebViewController.getTabControl();
            if (tabControl != null) {
                Log.d("browser", "[" + tabControl.getTabPosition(this.this$0) + "/" + tabControl.getTabCount() + "] onPageStarted url=" + str);
            }
            this.this$0.mInPageLoad = true;
            this.this$0.mUpdateThumbnail = true;
            this.this$0.mPageLoadProgress = 5;
            this.this$0.mCurrentState = new PageState(this.this$0.mContext, webView.isPrivateBrowsingEnabled(), str, bitmap);
            this.this$0.mLoadStartTime = SystemClock.uptimeMillis();
            if (this.this$0.mTouchIconLoader != null) {
                this.this$0.mTouchIconLoader.mTab = null;
                this.this$0.mTouchIconLoader = null;
            }
            if (this.this$0.mErrorConsole != null) {
                this.this$0.mErrorConsole.clearErrorMessages();
                if (this.this$0.mWebViewController.shouldShowErrorConsole()) {
                    this.this$0.mErrorConsole.showConsole(2);
                }
            }
            if (this.this$0.mDeviceAccountLogin != null) {
                this.this$0.mDeviceAccountLogin.cancel();
                this.this$0.mDeviceAccountLogin = null;
                this.this$0.mWebViewController.hideAutoLogin(this.this$0);
            }
            this.this$0.mWebViewController.onPageStarted(this.this$0, webView, bitmap);
            this.this$0.updateBookmarkedStatus();
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
            if (this.this$0.mInForeground) {
                KeyChain.choosePrivateKeyAlias(this.this$0.mWebViewController.getActivity(), new KeyChainAliasCallback(this, clientCertRequest) { // from class: com.android.browser.Tab.2.8
                    final AnonymousClass2 this$1;
                    final ClientCertRequest val$request;

                    {
                        this.this$1 = this;
                        this.val$request = clientCertRequest;
                    }

                    @Override // android.security.KeyChainAliasCallback
                    public void alias(String str) {
                        if (str == null) {
                            this.val$request.cancel();
                        } else {
                            new KeyChainLookup(this.this$1.this$0.mContext, this.val$request, str).execute(new Void[0]);
                        }
                    }
                }, clientCertRequest.getKeyTypes(), clientCertRequest.getPrincipals(), clientCertRequest.getHost(), clientCertRequest.getPort(), null);
            } else {
                clientCertRequest.ignore();
            }
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedError(WebView webView, int i, String str, String str2) {
            Log.d("Tab", "error code: " + i + " url: " + str2);
            this.this$0.mPageError = i;
            this.this$0.mWebViewController.sendErrorCode(i, str2);
            if (i == -2 || i == -6 || i == -12 || i == -10 || i == -13) {
                return;
            }
            this.this$0.queueError(i, str);
            if (this.this$0.isPrivateBrowsingEnabled()) {
                return;
            }
            Log.e("Tab", "onReceivedError " + i + " " + str2 + " " + str);
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String str, String str2) {
            this.this$0.mWebViewController.onReceivedHttpAuthRequest(this.this$0, webView, httpAuthHandler, str, str2);
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedLoginRequest(WebView webView, String str, String str2, String str3) {
            new DeviceAccountLogin(this.this$0.mWebViewController.getActivity(), webView, this.this$0, this.this$0.mWebViewController).handleLogin(str, str2, str3);
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            if (!this.this$0.mInForeground) {
                sslErrorHandler.cancel();
                this.this$0.setSecurityState(SecurityState.SECURITY_STATE_NOT_SECURE);
            } else if (this.this$0.mSettings.showSecurityWarnings()) {
                new AlertDialog.Builder(this.this$0.mContext).setTitle(2131492970).setMessage(2131492968).setIconAttribute(16843605).setPositiveButton(2131492969, new DialogInterface.OnClickListener(this, sslErrorHandler, sslError) { // from class: com.android.browser.Tab.2.4
                    final AnonymousClass2 this$1;
                    final SslError val$error;
                    final SslErrorHandler val$handler;

                    {
                        this.this$1 = this;
                        this.val$handler = sslErrorHandler;
                        this.val$error = sslError;
                    }

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        this.val$handler.proceed();
                        this.this$1.this$0.handleProceededAfterSslError(this.val$error);
                    }
                }).setNeutralButton(2131492971, new DialogInterface.OnClickListener(this, webView, sslErrorHandler, sslError) { // from class: com.android.browser.Tab.2.5
                    final AnonymousClass2 this$1;
                    final SslError val$error;
                    final SslErrorHandler val$handler;
                    final WebView val$view;

                    {
                        this.this$1 = this;
                        this.val$view = webView;
                        this.val$handler = sslErrorHandler;
                        this.val$error = sslError;
                    }

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        this.this$1.this$0.mWebViewController.showSslCertificateOnError(this.val$view, this.val$handler, this.val$error);
                    }
                }).setNegativeButton(2131492972, new DialogInterface.OnClickListener(this) { // from class: com.android.browser.Tab.2.6
                    final AnonymousClass2 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener(this, sslErrorHandler) { // from class: com.android.browser.Tab.2.7
                    final AnonymousClass2 this$1;
                    final SslErrorHandler val$handler;

                    {
                        this.this$1 = this;
                        this.val$handler = sslErrorHandler;
                    }

                    @Override // android.content.DialogInterface.OnCancelListener
                    public void onCancel(DialogInterface dialogInterface) {
                        this.val$handler.cancel();
                        this.this$1.this$0.setSecurityState(SecurityState.SECURITY_STATE_NOT_SECURE);
                        this.this$1.this$0.mWebViewController.onUserCanceledSsl(this.this$1.this$0);
                    }
                }).show();
            } else {
                sslErrorHandler.proceed();
            }
        }

        @Override // android.webkit.WebViewClient
        public void onUnhandledKeyEvent(WebView webView, KeyEvent keyEvent) {
            if (this.this$0.mInForeground && !this.this$0.mWebViewController.onUnhandledKeyEvent(keyEvent)) {
                super.onUnhandledKeyEvent(webView, keyEvent);
            }
        }

        @Override // android.webkit.WebViewClient
        public WebResourceResponse shouldInterceptRequest(WebView webView, String str) {
            return HomeProvider.shouldInterceptRequest(this.this$0.mContext, str);
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideKeyEvent(WebView webView, KeyEvent keyEvent) {
            if (this.this$0.mInForeground) {
                return this.this$0.mWebViewController.shouldOverrideKeyEvent(keyEvent);
            }
            return false;
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            if (this.this$0.mDisableOverrideUrlLoading || !this.this$0.mInForeground) {
                return false;
            }
            return this.this$0.mWebViewController.shouldOverrideUrlLoading(this.this$0, webView, str);
        }
    }

    /* loaded from: b.zip:com/android/browser/Tab$CancelSavePageTask.class */
    private class CancelSavePageTask extends AsyncTask<Void, Void, Void> {
        final Tab this$0;

        private CancelSavePageTask(Tab tab) {
            this.this$0 = tab;
        }

        /* synthetic */ CancelSavePageTask(Tab tab, CancelSavePageTask cancelSavePageTask) {
            this(tab);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(Void... voidArr) {
            if (Tab.DEBUG) {
                Log.d("browser", "Tab()--->CancelSavePageTask()--->doInBackground()");
            }
            Notification.Builder builder = new Notification.Builder(this.this$0.mContext);
            NotificationManager notificationManager = (NotificationManager) this.this$0.mContext.getSystemService("notification");
            ArrayList<ContentProviderOperation> arrayList = new ArrayList<>();
            this.this$0.mContext.getContentResolver();
            for (Map.Entry<Integer, Long> entry : this.this$0.mSavePageJob.entrySet()) {
                long longValue = entry.getValue().longValue();
                int intValue = entry.getKey().intValue();
                builder.setSmallIcon(2130837578);
                builder.setContentText(this.this$0.mContext.getText(2131492919));
                builder.setOngoing(false);
                builder.setContentIntent(null);
                builder.setTicker(this.this$0.mContext.getText(2131492919));
                notificationManager.notify(intValue, builder.build());
                arrayList.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(SnapshotProvider.Snapshots.CONTENT_URI, longValue)).build());
            }
            try {
                this.this$0.mContext.getContentResolver().applyBatch("com.android.browser.snapshots", arrayList);
                return null;
            } catch (OperationApplicationException e) {
                Log.e("Tab", "Failed to delete save page. OperationApplicationException: " + e.getMessage());
                return null;
            } catch (RemoteException e2) {
                Log.e("Tab", "Failed to delete save page. RemoteException: " + e2.getMessage());
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Void r4) {
            if (this.this$0.mSavePageJob != null) {
                this.this$0.mSavePageJob.clear();
                this.this$0.mSavePageJob = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/Tab$ErrorDialog.class */
    public class ErrorDialog {
        public final String mDescription;
        public final int mError;
        public final int mTitle;
        final Tab this$0;

        ErrorDialog(Tab tab, int i, String str, int i2) {
            this.this$0 = tab;
            this.mTitle = i;
            this.mDescription = str;
            this.mError = i2;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: b.zip:com/android/browser/Tab$PageState.class */
    public static class PageState {
        Bitmap mFavicon;
        boolean mIncognito;
        boolean mIsBookmarkedSite;
        boolean mIsDownload = false;
        String mOriginalUrl;
        SecurityState mSecurityState;
        SslError mSslCertificateError;
        String mTitle;
        String mUrl;

        PageState(Context context, boolean z) {
            this.mIncognito = z;
            if (this.mIncognito) {
                this.mUrl = "browser:incognito";
                this.mOriginalUrl = "browser:incognito";
                this.mTitle = context.getString(2131492949);
            } else {
                this.mUrl = "";
                this.mOriginalUrl = "";
                this.mTitle = context.getString(2131492948);
            }
            this.mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
        }

        PageState(Context context, boolean z, String str, Bitmap bitmap) {
            this.mIncognito = z;
            this.mUrl = str;
            this.mOriginalUrl = str;
            if (URLUtil.isHttpsUrl(str)) {
                this.mSecurityState = SecurityState.SECURITY_STATE_SECURE;
            } else {
                this.mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
            }
            this.mFavicon = bitmap;
        }
    }

    /* loaded from: b.zip:com/android/browser/Tab$SaveCallback.class */
    private static class SaveCallback implements ValueCallback<String> {
        String mResult;

        private SaveCallback() {
        }

        @Override // android.webkit.ValueCallback
        public void onReceiveValue(String str) {
            this.mResult = str;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /* loaded from: b.zip:com/android/browser/Tab$SecurityState.class */
    public enum SecurityState {
        SECURITY_STATE_NOT_SECURE,
        SECURITY_STATE_SECURE,
        SECURITY_STATE_MIXED,
        SECURITY_STATE_BAD_CERTIFICATE;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static SecurityState[] valuesCustom() {
            return values();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/Tab$SubWindowChromeClient.class */
    public class SubWindowChromeClient extends WebChromeClient {
        private final WebChromeClient mClient;
        final Tab this$0;

        SubWindowChromeClient(Tab tab, WebChromeClient webChromeClient) {
            this.this$0 = tab;
            this.mClient = webChromeClient;
        }

        @Override // android.webkit.WebChromeClient
        public void onCloseWindow(WebView webView) {
            if (webView != this.this$0.mSubView) {
                Log.e("Tab", "Can't close the window");
            }
            this.this$0.mWebViewController.dismissSubWindow(this.this$0);
        }

        @Override // android.webkit.WebChromeClient
        public boolean onCreateWindow(WebView webView, boolean z, boolean z2, Message message) {
            return this.mClient.onCreateWindow(webView, z, z2, message);
        }

        @Override // android.webkit.WebChromeClient
        public void onProgressChanged(WebView webView, int i) {
            this.mClient.onProgressChanged(webView, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/Tab$SubWindowClient.class */
    public static class SubWindowClient extends WebViewClient {
        private final WebViewClient mClient;
        private final WebViewController mController;

        SubWindowClient(WebViewClient webViewClient, WebViewController webViewController) {
            this.mClient = webViewClient;
            this.mController = webViewController;
        }

        @Override // android.webkit.WebViewClient
        public void doUpdateVisitedHistory(WebView webView, String str, boolean z) {
            this.mClient.doUpdateVisitedHistory(webView, str, z);
        }

        @Override // android.webkit.WebViewClient
        public void onFormResubmission(WebView webView, Message message, Message message2) {
            this.mClient.onFormResubmission(webView, message, message2);
        }

        @Override // android.webkit.WebViewClient
        public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
            this.mController.endActionMode();
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
            this.mClient.onReceivedClientCertRequest(webView, clientCertRequest);
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedError(WebView webView, int i, String str, String str2) {
            this.mClient.onReceivedError(webView, i, str, str2);
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String str, String str2) {
            this.mClient.onReceivedHttpAuthRequest(webView, httpAuthHandler, str, str2);
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            this.mClient.onReceivedSslError(webView, sslErrorHandler, sslError);
        }

        @Override // android.webkit.WebViewClient
        public void onUnhandledKeyEvent(WebView webView, KeyEvent keyEvent) {
            this.mClient.onUnhandledKeyEvent(webView, keyEvent);
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideKeyEvent(WebView webView, KeyEvent keyEvent) {
            return this.mClient.shouldOverrideKeyEvent(webView, keyEvent);
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            return this.mClient.shouldOverrideUrlLoading(webView, str);
        }
    }

    static {
        sAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        sAlphaPaint.setColor(0);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab(WebViewController webViewController, Bundle bundle) {
        this(webViewController, null, bundle);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab(WebViewController webViewController, WebView webView) {
        this(webViewController, webView, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Tab(WebViewController webViewController, WebView webView, Bundle bundle) {
        this.mWillBeClosed = false;
        this.mPageError = 0;
        this.mId = -1L;
        this.mDialogListener = new DialogInterface.OnDismissListener(this) { // from class: com.android.browser.Tab.1
            final Tab this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                this.this$0.mIsErrorDialogShown = false;
                this.this$0.processNextError();
            }
        };
        this.mIsErrorDialogShown = false;
        this.mWebViewClient = new AnonymousClass2(this);
        this.mSubWindowShown = false;
        this.mWebChromeClient = new WebChromeClient(this) { // from class: com.android.browser.Tab.3

            /* renamed from: -android-webkit-ConsoleMessage$MessageLevelSwitchesValues  reason: not valid java name */
            private static final int[] f4androidwebkitConsoleMessage$MessageLevelSwitchesValues = null;
            final Tab this$0;

            /* renamed from: -getandroid-webkit-ConsoleMessage$MessageLevelSwitchesValues  reason: not valid java name */
            private static /* synthetic */ int[] m248getandroidwebkitConsoleMessage$MessageLevelSwitchesValues() {
                if (f4androidwebkitConsoleMessage$MessageLevelSwitchesValues != null) {
                    return f4androidwebkitConsoleMessage$MessageLevelSwitchesValues;
                }
                int[] iArr = new int[ConsoleMessage.MessageLevel.values().length];
                try {
                    iArr[ConsoleMessage.MessageLevel.DEBUG.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.ERROR.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.LOG.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.TIP.ordinal()] = 4;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.WARNING.ordinal()] = 5;
                } catch (NoSuchFieldError e5) {
                }
                f4androidwebkitConsoleMessage$MessageLevelSwitchesValues = iArr;
                return iArr;
            }

            {
                this.this$0 = this;
            }

            private void createWindow(boolean z, Message message) {
                WebView.WebViewTransport webViewTransport = (WebView.WebViewTransport) message.obj;
                if (z) {
                    this.this$0.createSubWindow();
                    this.this$0.mWebViewController.attachSubWindow(this.this$0);
                    webViewTransport.setWebView(this.this$0.mSubView);
                } else {
                    webViewTransport.setWebView(this.this$0.mWebViewController.openTab(null, this.this$0, true, true).getWebView());
                }
                message.sendToTarget();
            }

            @Override // android.webkit.WebChromeClient
            public Bitmap getDefaultVideoPoster() {
                if (this.this$0.mInForeground) {
                    return this.this$0.mWebViewController.getDefaultVideoPoster();
                }
                return null;
            }

            @Override // android.webkit.WebChromeClient
            public View getVideoLoadingProgressView() {
                if (this.this$0.mInForeground) {
                    return this.this$0.mWebViewController.getVideoLoadingProgressView();
                }
                return null;
            }

            @Override // android.webkit.WebChromeClient
            public void getVisitedHistory(ValueCallback<String[]> valueCallback) {
                this.this$0.mWebViewController.getVisitedHistory(valueCallback);
            }

            @Override // android.webkit.WebChromeClient
            public void onCloseWindow(WebView webView2) {
                if (this.this$0.mParent != null) {
                    if (this.this$0.mInForeground) {
                        this.this$0.mWebViewController.switchToTab(this.this$0.mParent);
                    }
                    this.this$0.mWebViewController.closeTab(this.this$0);
                }
            }

            @Override // android.webkit.WebChromeClient
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (this.this$0.mInForeground) {
                    ErrorConsoleView errorConsole = this.this$0.getErrorConsole(true);
                    errorConsole.addErrorMessage(consoleMessage);
                    if (this.this$0.mWebViewController.shouldShowErrorConsole() && errorConsole.getShowState() != 1) {
                        errorConsole.showConsole(0);
                    }
                }
                if (this.this$0.isPrivateBrowsingEnabled()) {
                    return true;
                }
                String str = "Console: " + consoleMessage.message() + " " + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber();
                switch (m248getandroidwebkitConsoleMessage$MessageLevelSwitchesValues()[consoleMessage.messageLevel().ordinal()]) {
                    case 1:
                        Log.d("browser", str);
                        return true;
                    case 2:
                        Log.e("browser", str);
                        return true;
                    case 3:
                        Log.i("browser", str);
                        return true;
                    case 4:
                        Log.v("browser", str);
                        return true;
                    case 5:
                        Log.w("browser", str);
                        return true;
                    default:
                        return true;
                }
            }

            @Override // android.webkit.WebChromeClient
            public boolean onCreateWindow(WebView webView2, boolean z, boolean z2, Message message) {
                if (this.this$0.mInForeground) {
                    if (z && this.this$0.mSubView != null) {
                        new AlertDialog.Builder(this.this$0.mContext).setTitle(2131493215).setIconAttribute(16843605).setMessage(2131493216).setPositiveButton(2131492963, (DialogInterface.OnClickListener) null).show();
                        return false;
                    } else if (!this.this$0.mWebViewController.getTabControl().canCreateNewTab()) {
                        new AlertDialog.Builder(this.this$0.mContext).setTitle(2131493213).setIconAttribute(16843605).setMessage(2131493214).setPositiveButton(2131492963, (DialogInterface.OnClickListener) null).show();
                        return false;
                    } else if (z2) {
                        createWindow(z, message);
                        return true;
                    } else if (this.this$0.mSubWindowShown) {
                        new AlertDialog.Builder(this.this$0.mContext).setTitle(2131493215).setIconAttribute(16843605).setMessage(2131493216).setPositiveButton(2131492963, (DialogInterface.OnClickListener) null).show();
                        return false;
                    } else {
                        this.this$0.mWebViewController.onShowPopupWindowAttempt(this.this$0, z, message);
                        return true;
                    }
                }
                return false;
            }

            @Override // android.webkit.WebChromeClient
            public void onExceededDatabaseQuota(String str, String str2, long j, long j2, long j3, WebStorage.QuotaUpdater quotaUpdater) {
                this.this$0.mSettings.getWebStorageSizeManager().onExceededDatabaseQuota(str, str2, j, j2, j3, quotaUpdater);
            }

            @Override // android.webkit.WebChromeClient
            public void onGeolocationPermissionsHidePrompt() {
                if (!this.this$0.mInForeground || this.this$0.mGeolocationPermissionsPrompt == null) {
                    return;
                }
                this.this$0.mGeolocationPermissionsPrompt.hide();
            }

            @Override // android.webkit.WebChromeClient
            public void onGeolocationPermissionsShowPrompt(String str, GeolocationPermissions.Callback callback) {
                if (this.this$0.mInForeground) {
                    this.this$0.getGeolocationPermissionsPrompt().show(str, callback);
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onHideCustomView() {
                if (this.this$0.mInForeground) {
                    this.this$0.mWebViewController.hideCustomView();
                }
            }

            @Override // android.webkit.WebChromeClient
            public boolean onJsAlert(WebView webView2, String str, String str2, JsResult jsResult) {
                this.this$0.mWebViewController.getTabControl().setActiveTab(this.this$0);
                return false;
            }

            @Override // android.webkit.WebChromeClient
            public boolean onJsConfirm(WebView webView2, String str, String str2, JsResult jsResult) {
                this.this$0.mWebViewController.getTabControl().setActiveTab(this.this$0);
                return false;
            }

            @Override // android.webkit.WebChromeClient
            public boolean onJsPrompt(WebView webView2, String str, String str2, String str3, JsPromptResult jsPromptResult) {
                this.this$0.mWebViewController.getTabControl().setActiveTab(this.this$0);
                return false;
            }

            @Override // android.webkit.WebChromeClient
            public void onPermissionRequest(PermissionRequest permissionRequest) {
                if (this.this$0.mInForeground) {
                    this.this$0.getPermissionsPrompt().show(permissionRequest);
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onPermissionRequestCanceled(PermissionRequest permissionRequest) {
                if (!this.this$0.mInForeground || this.this$0.mPermissionsPrompt == null) {
                    return;
                }
                this.this$0.mPermissionsPrompt.hide();
            }

            @Override // android.webkit.WebChromeClient
            public void onProgressChanged(WebView webView2, int i) {
                this.this$0.mPageLoadProgress = i;
                this.this$0.mPageError = 0;
                if (i == 100) {
                    this.this$0.mInPageLoad = false;
                    this.this$0.syncCurrentState(webView2, webView2.getUrl());
                }
                this.this$0.mWebViewController.onProgressChanged(this.this$0);
                if (this.this$0.mUpdateThumbnail && i == 100) {
                    this.this$0.mUpdateThumbnail = false;
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onReachedMaxAppCacheSize(long j, long j2, WebStorage.QuotaUpdater quotaUpdater) {
                this.this$0.mSettings.getWebStorageSizeManager().onReachedMaxAppCacheSize(j, j2, quotaUpdater);
            }

            @Override // android.webkit.WebChromeClient
            public void onReceivedIcon(WebView webView2, Bitmap bitmap) {
                this.this$0.mCurrentState.mFavicon = bitmap;
                this.this$0.mWebViewController.onFavicon(this.this$0, webView2, bitmap);
            }

            @Override // android.webkit.WebChromeClient
            public void onReceivedTitle(WebView webView2, String str) {
                this.this$0.mCurrentState.mTitle = str;
                this.this$0.mWebViewController.onReceivedTitle(this.this$0, str);
            }

            @Override // android.webkit.WebChromeClient
            public void onReceivedTouchIconUrl(WebView webView2, String str, boolean z) {
                ContentResolver contentResolver = this.this$0.mContext.getContentResolver();
                if (z && this.this$0.mTouchIconLoader != null) {
                    this.this$0.mTouchIconLoader.cancel(false);
                    this.this$0.mTouchIconLoader = null;
                }
                if (this.this$0.mTouchIconLoader == null) {
                    this.this$0.mTouchIconLoader = new DownloadTouchIcon(this.this$0, this.this$0.mContext, contentResolver, webView2);
                    this.this$0.mTouchIconLoader.execute(str);
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onRequestFocus(WebView webView2) {
                if (this.this$0.mInForeground) {
                    return;
                }
                this.this$0.mWebViewController.switchToTab(this.this$0);
            }

            @Override // android.webkit.WebChromeClient
            public void onShowCustomView(View view, int i, WebChromeClient.CustomViewCallback customViewCallback) {
                if (this.this$0.mInForeground) {
                    this.this$0.mWebViewController.showCustomView(this.this$0, view, i, customViewCallback);
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback customViewCallback) {
                Activity activity = this.this$0.mWebViewController.getActivity();
                if (activity != null) {
                    onShowCustomView(view, activity.getRequestedOrientation(), customViewCallback);
                }
            }

            @Override // android.webkit.WebChromeClient
            public boolean onShowFileChooser(WebView webView2, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (this.this$0.mInForeground) {
                    this.this$0.mWebViewController.showFileChooser(valueCallback, fileChooserParams);
                    return true;
                }
                return false;
            }
        };
        this.mIsBookmarkCallback = new DataController.OnQueryUrlIsBookmark(this) { // from class: com.android.browser.Tab.4
            final Tab this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.browser.DataController.OnQueryUrlIsBookmark
            public void onQueryUrlIsBookmark(String str, boolean z) {
                if (this.this$0.mCurrentState.mUrl.equals(str)) {
                    this.this$0.mCurrentState.mIsBookmarkedSite = z;
                    this.this$0.mWebViewController.bookmarkedStatusHasChanged(this.this$0);
                }
            }
        };
        this.callback = null;
        this.mBrowserUrlExt = null;
        if (DEBUG) {
            Log.d("browser", "Tab()--->Constructor()--->WebView is " + (webView == null ? "null" : "not null") + ", Bundle is " + (bundle == null ? "null" : "not null"));
        }
        this.mSavePageJob = new HashMap<>();
        this.mWebViewController = webViewController;
        this.mContext = this.mWebViewController.getContext();
        this.mSettings = BrowserSettings.getInstance();
        this.mDataController = DataController.getInstance(this.mContext);
        this.mCurrentState = new PageState(this.mContext, webView != null ? webView.isPrivateBrowsingEnabled() : false);
        this.mInPageLoad = false;
        this.mInForeground = false;
        this.mDownloadListener = new BrowserDownloadListener(this) { // from class: com.android.browser.Tab.5
            final Tab this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.browser.BrowserDownloadListener
            public void onDownloadStart(String str, String str2, String str3, String str4, String str5, long j) {
                String remapGenericMimeTypePublic = MimeTypeMap.getSingleton().remapGenericMimeTypePublic(str4, str, str3);
                this.this$0.mCurrentState.mIsDownload = true;
                this.this$0.mWebViewController.onDownloadStart(this.this$0, str, str2, str3, remapGenericMimeTypePublic, str5, j);
            }
        };
        this.mWebBackForwardListClient = new WebBackForwardListClient(this) { // from class: com.android.browser.Tab.6
            final Tab this$0;

            {
                this.this$0 = this;
            }
        };
        this.mCaptureWidth = this.mContext.getResources().getDimensionPixelSize(2131427376);
        this.mCaptureHeight = this.mContext.getResources().getDimensionPixelSize(2131427377);
        updateShouldCaptureThumbnails();
        restoreState(bundle);
        if (getId() == -1) {
            this.mId = TabControl.getNextId();
        }
        setWebView(webView);
        this.mHandler = new Handler(this) { // from class: com.android.browser.Tab.7
            final Tab this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 42:
                        this.this$0.capture();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Bitmap getDefaultFavicon(Context context) {
        Bitmap bitmap;
        synchronized (Tab.class) {
            try {
                if (sDefaultFavicon == null) {
                    sDefaultFavicon = BitmapFactory.decodeResource(context.getResources(), 2130837505);
                }
                bitmap = sDefaultFavicon;
            } catch (Throwable th) {
                throw th;
            }
        }
        return bitmap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleProceededAfterSslError(SslError sslError) {
        if (sslError.getUrl().equals(this.mCurrentState.mUrl)) {
            setSecurityState(SecurityState.SECURITY_STATE_BAD_CERTIFICATE);
            this.mCurrentState.mSslCertificateError = sslError;
        } else if (getSecurityState() == SecurityState.SECURITY_STATE_SECURE) {
            setSecurityState(SecurityState.SECURITY_STATE_MIXED);
        }
    }

    private void postCapture() {
        if (this.mHandler.hasMessages(42)) {
            return;
        }
        this.mHandler.sendEmptyMessageDelayed(42, 100L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processNextError() {
        if (this.mQueuedErrors == null) {
            return;
        }
        this.mQueuedErrors.removeFirst();
        if (this.mQueuedErrors.size() == 0) {
            this.mQueuedErrors = null;
        } else {
            showError(this.mQueuedErrors.getFirst());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0049, code lost:
        if (r9.isEmpty() != false) goto L19;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void queueError(int i, String str) {
        if (this.mQueuedErrors == null) {
            this.mQueuedErrors = new LinkedList<>();
        }
        for (ErrorDialog errorDialog : this.mQueuedErrors) {
            if (errorDialog.mError == i) {
                return;
            }
        }
        String str2 = str;
        if (i == -20000) {
            if (str != null) {
                str2 = str;
            }
            str2 = this.mContext.getString(2131492918);
        }
        ErrorDialog errorDialog2 = new ErrorDialog(this, (i == -14 || i == -20000) ? 2131493195 : 2131493194, str2, i);
        this.mQueuedErrors.addLast(errorDialog2);
        if (this.mQueuedErrors.size() == 1 && this.mInForeground) {
            showError(errorDialog2);
        }
    }

    private void restoreState(Bundle bundle) {
        if (DEBUG) {
            Log.d("browser", "Tab.restoreState()()---> bundle is " + bundle);
        }
        this.mSavedState = bundle;
        if (this.mSavedState == null) {
            return;
        }
        this.mId = bundle.getLong("ID");
        this.mAppId = bundle.getString("appid");
        this.mCloseOnBack = bundle.getBoolean("closeOnBack");
        restoreUserAgent();
        String string = bundle.getString("currentUrl");
        String string2 = bundle.getString("currentTitle");
        this.mCurrentState = new PageState(this.mContext, bundle.getBoolean("privateBrowsingEnabled"), string, null);
        this.mCurrentState.mTitle = string2;
        synchronized (this) {
            if (this.mCapture != null) {
                DataController.getInstance(this.mContext).loadThumbnail(this);
            }
        }
    }

    private void restoreUserAgent() {
        if (this.mMainView == null || this.mSavedState == null || this.mSavedState.getBoolean("useragent") == this.mSettings.hasDesktopUseragent(this.mMainView)) {
            return;
        }
        this.mSettings.toggleDesktopUseragent(this.mMainView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSecurityState(SecurityState securityState) {
        this.mCurrentState.mSecurityState = securityState;
        this.mCurrentState.mSslCertificateError = null;
        this.mWebViewController.onUpdatedSecurityState(this);
    }

    private void setupHwAcceleration(View view) {
        if (view == null) {
            return;
        }
        if (BrowserSettings.getInstance().isHardwareAccelerated()) {
            view.setLayerType(0, null);
        } else {
            view.setLayerType(1, null);
        }
    }

    private void showError(ErrorDialog errorDialog) {
        if (!this.mInForeground || this.mIsErrorDialogShown) {
            return;
        }
        AlertDialog create = new AlertDialog.Builder(this.mContext).setTitle(errorDialog.mTitle).setMessage(errorDialog.mDescription).setPositiveButton(2131492963, (DialogInterface.OnClickListener) null).create();
        create.setOnDismissListener(this.mDialogListener);
        create.show();
        this.mIsErrorDialogShown = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void syncCurrentState(WebView webView, String str) {
        if (this.mWillBeClosed) {
            return;
        }
        if (DEBUG) {
            Log.d("browser", "Tab.syncCurrentState()()--->url = " + str + ", webview = " + webView);
        }
        this.mCurrentState.mUrl = webView.getUrl();
        if (this.mCurrentState.mUrl == null) {
            this.mCurrentState.mUrl = "";
        }
        if (this.mPageError != 0 && this.mCurrentState.mOriginalUrl != webView.getOriginalUrl()) {
            this.mCurrentState.mUrl = str;
        }
        this.mCurrentState.mOriginalUrl = webView.getOriginalUrl();
        this.mCurrentState.mTitle = webView.getTitle();
        this.mCurrentState.mFavicon = webView.getFavicon();
        if (!URLUtil.isHttpsUrl(this.mCurrentState.mUrl)) {
            this.mCurrentState.mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
            this.mCurrentState.mSslCertificateError = null;
        }
        this.mCurrentState.mIncognito = webView.isPrivateBrowsingEnabled();
    }

    public void PopupWindowShown(boolean z) {
        this.mSubWindowShown = z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addChildTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "Tab.addChildTab()--->Tab child = " + tab);
        }
        if (this.mChildren == null) {
            this.mChildren = new Vector<>();
        }
        this.mChildren.add(tab);
        tab.setParent(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addDatabaseItemId(int i, long j) {
        if (this.mSavePageJob == null) {
            this.mSavePageJob = new HashMap<>();
        }
        this.mSavePageJob.put(Integer.valueOf(i), Long.valueOf(j));
    }

    public boolean canGoBack() {
        return this.mMainView != null ? this.mMainView.canGoBack() : false;
    }

    public boolean canGoForward() {
        return this.mMainView != null ? this.mMainView.canGoForward() : false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void capture() {
        TabControl.OnThumbnailUpdatedListener onThumbnailUpdatedListener;
        if (this.mMainView == null || this.mCapture == null || this.mMainView.getContentWidth() <= 0 || this.mMainView.getContentHeight() <= 0) {
            return;
        }
        Canvas canvas = new Canvas(this.mCapture);
        int scrollX = this.mMainView.getScrollX();
        int scrollY = this.mMainView.getScrollY() + this.mMainView.getVisibleTitleHeight();
        int save = canvas.save();
        canvas.translate(-scrollX, -scrollY);
        float width = this.mCaptureWidth / this.mMainView.getWidth();
        if (DEBUG) {
            Log.d("browser", "Tab.capture()--->left = " + scrollX + ", top = " + scrollY + ", scale = " + width);
        }
        canvas.scale(width, width, scrollX, scrollY);
        if (this.mMainView instanceof BrowserWebView) {
            ((BrowserWebView) this.mMainView).drawContent(canvas);
        } else {
            this.mMainView.draw(canvas);
        }
        canvas.restoreToCount(save);
        canvas.drawRect(0.0f, 0.0f, 1.0f, this.mCapture.getHeight(), sAlphaPaint);
        canvas.drawRect(this.mCapture.getWidth() - 1, 0.0f, this.mCapture.getWidth(), this.mCapture.getHeight(), sAlphaPaint);
        canvas.drawRect(0.0f, 0.0f, this.mCapture.getWidth(), 1.0f, sAlphaPaint);
        canvas.drawRect(0.0f, this.mCapture.getHeight() - 1, this.mCapture.getWidth(), this.mCapture.getHeight(), sAlphaPaint);
        canvas.setBitmap(null);
        this.mHandler.removeMessages(42);
        persistThumbnail();
        TabControl tabControl = this.mWebViewController.getTabControl();
        if (tabControl == null || (onThumbnailUpdatedListener = tabControl.getOnThumbnailUpdatedListener()) == null) {
            return;
        }
        onThumbnailUpdatedListener.onThumbnailUpdated(this);
    }

    public void clearTabData() {
        this.mWillBeClosed = true;
        this.mAppId = "";
        this.mCurrentState.mUrl = "";
        this.mCurrentState.mOriginalUrl = "";
        this.mCurrentState.mTitle = "";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean closeOnBack() {
        return this.mCloseOnBack;
    }

    public byte[] compressBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean containsDatabaseItemId(int i) {
        if (this.mSavePageJob != null) {
            return this.mSavePageJob.containsKey(Integer.valueOf(i));
        }
        return false;
    }

    public ContentValues createSavePageContentValues(int i, String str) {
        if (DEBUG) {
            Log.d("browser", "Tab.createSavePageContentValues()()--->id = " + i + ", file = " + str);
        }
        if (this.mMainView == null) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", this.mCurrentState.mTitle);
        contentValues.put("url", this.mCurrentState.mUrl);
        contentValues.put("date_created", Long.valueOf(System.currentTimeMillis()));
        contentValues.put("favicon", compressBitmap(getFavicon()));
        contentValues.put("thumbnail", compressBitmap(Controller.createScreenshot(this.mMainView, Controller.getDesiredThumbnailWidth(this.mContext), Controller.getDesiredThumbnailHeight(this.mContext))));
        contentValues.put("progress", (Integer) 0);
        contentValues.put("is_done", (Integer) 0);
        contentValues.put("job_id", Integer.valueOf(i));
        contentValues.put("viewstate_path", str);
        return contentValues;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean createSubWindow() {
        if (DEBUG) {
            Log.d("browser", "Tab.createSubWindow()--->mSubView = " + this.mSubView);
        }
        if (this.mSubView == null) {
            this.mWebViewController.createSubWindow(this);
            this.mSubView.setWebViewClient(new SubWindowClient(this.mWebViewClient, this.mWebViewController));
            this.mSubView.setWebChromeClient(new SubWindowChromeClient(this, this.mWebChromeClient));
            this.mSubView.setDownloadListener(new BrowserDownloadListener(this) { // from class: com.android.browser.Tab.8
                final Tab this$0;

                {
                    this.this$0 = this;
                }

                @Override // com.android.browser.BrowserDownloadListener
                public void onDownloadStart(String str, String str2, String str3, String str4, String str5, long j) {
                    this.this$0.mWebViewController.onDownloadStart(this.this$0, str, str2, str3, MimeTypeMap.getSingleton().remapGenericMimeTypePublic(str4, str, str3), str5, j);
                    if (this.this$0.mSubView.copyBackForwardList().getSize() == 0) {
                        this.this$0.mWebViewController.dismissSubWindow(this.this$0);
                    }
                }
            });
            this.mSubView.setOnCreateContextMenuListener(this.mWebViewController.getActivity());
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void deleteThumbnail() {
        DataController.getInstance(this.mContext).deleteThumbnail(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroy() {
        if (DEBUG) {
            Log.d("browser", "Tab.destroy--->" + this.mMainView);
        }
        if (this.mMainView != null) {
            dismissSubWindow();
            WebView webView = this.mMainView;
            setWebView(null);
            webView.destroy();
        }
        if (this.mSavePageJob == null || this.mSavePageJob.size() == 0) {
            return;
        }
        Toast.makeText(this.mContext, 2131492919, 1).show();
        new CancelSavePageTask(this, null).execute(new Void[0]);
    }

    public void disableUrlOverridingForLoad() {
        this.mDisableOverrideUrlLoading = true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void dismissSubWindow() {
        if (DEBUG) {
            Log.d("browser", "Tab.dismissSubWindow()--->mSubView = " + this.mSubView);
        }
        if (this.mSubView != null) {
            this.mWebViewController.endActionMode();
            this.mSubView.destroy();
            this.mSubView = null;
            this.mSubViewContainer = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getAppId() {
        return this.mAppId;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DeviceAccountLogin getDeviceAccountLogin() {
        if (DEBUG) {
            Log.d("browser", "Tab.getDeviceAccountLogin()--->");
        }
        return this.mDeviceAccountLogin;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ErrorConsoleView getErrorConsole(boolean z) {
        if (z && this.mErrorConsole == null) {
            this.mErrorConsole = new ErrorConsoleView(this.mContext);
            this.mErrorConsole.setWebView(this.mMainView);
        }
        return this.mErrorConsole;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Bitmap getFavicon() {
        return this.mCurrentState.mFavicon != null ? this.mCurrentState.mFavicon : getDefaultFavicon(this.mContext);
    }

    GeolocationPermissionsPrompt getGeolocationPermissionsPrompt() {
        if (this.mGeolocationPermissionsPrompt == null) {
            this.mGeolocationPermissionsPrompt = (GeolocationPermissionsPrompt) ((ViewStub) this.mContainer.findViewById(2131558523)).inflate();
        }
        return this.mGeolocationPermissionsPrompt;
    }

    public long getId() {
        return this.mId;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getLoadProgress() {
        if (this.mInPageLoad) {
            return this.mPageLoadProgress;
        }
        return 100;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getOriginalUrl() {
        return this.mCurrentState.mOriginalUrl == null ? getUrl() : UrlUtils.filteredUrl(this.mCurrentState.mOriginalUrl);
    }

    public Tab getParent() {
        return this.mParent;
    }

    PermissionsPrompt getPermissionsPrompt() {
        if (this.mPermissionsPrompt == null) {
            this.mPermissionsPrompt = (PermissionsPrompt) ((ViewStub) this.mContainer.findViewById(2131558522)).inflate();
        }
        return this.mPermissionsPrompt;
    }

    public Bitmap getScreenshot() {
        Bitmap bitmap;
        synchronized (this) {
            bitmap = this.mCapture;
        }
        return bitmap;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SecurityState getSecurityState() {
        return this.mCurrentState.mSecurityState;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SslError getSslCertificateError() {
        return this.mCurrentState.mSslCertificateError;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public View getSubViewContainer() {
        return this.mSubViewContainer;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebView getSubWebView() {
        return this.mSubView;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getTitle() {
        return (this.mCurrentState.mTitle == null && this.mInPageLoad) ? this.mContext.getString(2131492964) : this.mCurrentState.mTitle;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebView getTopWindow() {
        return this.mSubView != null ? this.mSubView : this.mMainView;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getUrl() {
        return UrlUtils.filteredUrl(this.mCurrentState.mUrl);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public View getViewContainer() {
        return this.mContainer;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebView getWebView() {
        return this.mMainView;
    }

    public void goBack() {
        if (this.mMainView != null) {
            this.mMainView.goBack();
        }
    }

    public void goForward() {
        if (this.mMainView != null) {
            this.mMainView.goForward();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean inForeground() {
        return this.mInForeground;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean inPageLoad() {
        return this.mInPageLoad;
    }

    public boolean isBookmarkedSite() {
        return this.mCurrentState.mIsBookmarkedSite;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isPrivateBrowsingEnabled() {
        return this.mCurrentState.mIncognito;
    }

    public boolean isSnapshot() {
        return false;
    }

    public void loadUrl(String str, Map<String, String> map) {
        if (DEBUG) {
            Log.d("browser", "Tab.loadUrl()()--->url = " + str + ", headers = " + map);
        }
        if (this.mMainView != null) {
            this.mBrowserUrlExt = Extensions.getUrlPlugin(this.mContext);
            String checkAndTrimUrl = this.mBrowserUrlExt.checkAndTrimUrl(str);
            this.mPageLoadProgress = 5;
            this.mInPageLoad = true;
            this.mCurrentState = new PageState(this.mContext, false, checkAndTrimUrl, null);
            this.mWebViewController.onPageStarted(this, this.mMainView, null);
            this.mMainView.loadUrl(checkAndTrimUrl, map);
        }
    }

    @Override // android.webkit.WebView.PictureListener
    public void onNewPicture(WebView webView, Picture picture) {
        postCapture();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void pause() {
        if (this.mMainView != null) {
            this.mMainView.onPause();
            if (this.mSubView != null) {
                this.mSubView.onPause();
            }
        }
    }

    protected void persistThumbnail() {
        DataController.getInstance(this.mContext).saveThumbnail(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void putInBackground() {
        if (this.mInForeground) {
            capture();
            this.mInForeground = false;
            pause();
            this.mMainView.setOnCreateContextMenuListener(null);
            if (this.mSubView != null) {
                this.mSubView.setOnCreateContextMenuListener(null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void putInForeground() {
        if (this.mInForeground) {
            return;
        }
        this.mInForeground = true;
        resume();
        Activity activity = this.mWebViewController.getActivity();
        this.mMainView.setOnCreateContextMenuListener(activity);
        if (this.mSubView != null) {
            this.mSubView.setOnCreateContextMenuListener(activity);
        }
        if (this.mQueuedErrors != null && this.mQueuedErrors.size() > 0) {
            showError(this.mQueuedErrors.getFirst());
        }
        this.mWebViewController.bookmarkedStatusHasChanged(this);
    }

    public void refreshIdAfterPreload() {
        this.mId = TabControl.getNextId();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeDatabaseItemId(int i) {
        if (this.mSavePageJob != null) {
            this.mSavePageJob.remove(Integer.valueOf(i));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeFromTree() {
        if (DEBUG) {
            Log.d("browser", "Tab.removeFromTree()--->tab this = " + this);
        }
        if (this.mChildren != null) {
            for (Tab tab : this.mChildren) {
                tab.setParent(null);
            }
        }
        if (this.mParent != null) {
            this.mParent.mChildren.remove(this);
        }
        deleteThumbnail();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resume() {
        if (this.mMainView != null) {
            setupHwAcceleration(this.mMainView);
            this.mMainView.onResume();
            if (this.mSubView != null) {
                this.mSubView.onResume();
            }
        }
    }

    public Bundle saveState() {
        if (this.mMainView == null) {
            return this.mSavedState;
        }
        if (TextUtils.isEmpty(this.mCurrentState.mUrl)) {
            return null;
        }
        this.mSavedState = new Bundle();
        WebBackForwardList saveState = this.mMainView.saveState(this.mSavedState);
        if (saveState == null || saveState.getSize() == 0) {
            Log.w("Tab", "Failed to save back/forward list for " + this.mCurrentState.mUrl);
        }
        this.mSavedState.putLong("ID", this.mId);
        this.mSavedState.putString("currentUrl", this.mCurrentState.mUrl);
        this.mSavedState.putString("currentTitle", this.mCurrentState.mTitle);
        this.mSavedState.putBoolean("privateBrowsingEnabled", this.mMainView.isPrivateBrowsingEnabled());
        if (this.mAppId != null) {
            this.mSavedState.putString("appid", this.mAppId);
        }
        this.mSavedState.putBoolean("closeOnBack", this.mCloseOnBack);
        if (this.mParent != null) {
            this.mSavedState.putLong("parentTab", this.mParent.mId);
        }
        this.mSavedState.putBoolean("useragent", this.mSettings.hasDesktopUseragent(getWebView()));
        return this.mSavedState;
    }

    public void setAcceptThirdPartyCookies(boolean z) {
        CookieManager cookieManager = CookieManager.getInstance();
        if (this.mMainView != null) {
            cookieManager.setAcceptThirdPartyCookies(this.mMainView, z);
        }
        if (this.mSubView != null) {
            cookieManager.setAcceptThirdPartyCookies(this.mSubView, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAppId(String str) {
        this.mAppId = str;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setCloseOnBack(boolean z) {
        this.mCloseOnBack = z;
    }

    public void setController(WebViewController webViewController) {
        this.mWebViewController = webViewController;
        updateShouldCaptureThumbnails();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDeviceAccountLogin(DeviceAccountLogin deviceAccountLogin) {
        this.mDeviceAccountLogin = deviceAccountLogin;
    }

    void setParent(Tab tab) {
        if (tab == this) {
            throw new IllegalStateException("Cannot set parent to self!");
        }
        this.mParent = tab;
        if (this.mSavedState != null) {
            if (tab == null) {
                this.mSavedState.remove("parentTab");
            } else {
                this.mSavedState.putLong("parentTab", tab.getId());
            }
        }
        if (tab != null && this.mSettings.hasDesktopUseragent(tab.getWebView()) != this.mSettings.hasDesktopUseragent(getWebView())) {
            this.mSettings.toggleDesktopUseragent(getWebView());
        }
        if (tab != null && tab.getId() == getId()) {
            throw new IllegalStateException("Parent has same ID as child!");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSubViewContainer(View view) {
        this.mSubViewContainer = view;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSubWebView(WebView webView) {
        this.mSubView = webView;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setViewContainer(View view) {
        this.mContainer = view;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setWebView(WebView webView) {
        setWebView(webView, true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setWebView(WebView webView, boolean z) {
        if (DEBUG) {
            Log.d("browser", "Tab.setWebView()--->webview = " + webView + ", restore = " + z);
        }
        if (this.mMainView == webView) {
            return;
        }
        if (this.mGeolocationPermissionsPrompt != null) {
            this.mGeolocationPermissionsPrompt.hide();
        }
        if (this.mPermissionsPrompt != null) {
            this.mPermissionsPrompt.hide();
        }
        this.mWebViewController.onSetWebView(this, webView);
        if (this.mMainView != null) {
            this.mMainView.setPictureListener(null);
            if (webView != null) {
                syncCurrentState(webView, null);
            } else {
                this.mCurrentState = new PageState(this.mContext, false);
            }
        }
        this.mMainView = webView;
        if (this.mMainView != null) {
            this.mMainView.setWebViewClient(this.mWebViewClient);
            this.mMainView.setWebChromeClient(this.mWebChromeClient);
            this.mMainView.setDownloadListener(this.mDownloadListener);
            TabControl tabControl = this.mWebViewController.getTabControl();
            if (tabControl != null && tabControl.getOnThumbnailUpdatedListener() != null) {
                this.mMainView.setPictureListener(this);
            }
            if (!z || this.mSavedState == null) {
                return;
            }
            restoreUserAgent();
            WebBackForwardList restoreState = this.mMainView.restoreState(this.mSavedState);
            if (restoreState == null || restoreState.getSize() == 0) {
                Log.w("Tab", "Failed to restore WebView state!");
                loadUrl(this.mCurrentState.mOriginalUrl, null);
            }
            this.mSavedState = null;
        }
    }

    public boolean shouldUpdateThumbnail() {
        return this.mUpdateThumbnail;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(this.mId);
        sb.append(") has parent: ");
        if (getParent() != null) {
            sb.append("true[");
            sb.append(getParent().getId());
            sb.append("]");
        } else {
            sb.append("false");
        }
        sb.append(", incog: ");
        sb.append(isPrivateBrowsingEnabled());
        if (!isPrivateBrowsingEnabled()) {
            sb.append(", title: ");
            sb.append(getTitle());
            sb.append(", url: ");
            sb.append(getUrl());
        }
        return sb.toString();
    }

    public void updateBookmarkedStatus() {
        if (this.mCurrentState.mUrl == null || !this.mCurrentState.mUrl.equals(SiteNavigation.SITE_NAVIGATION_URI.toString())) {
            this.mDataController.queryBookmarkStatus(getUrl(), this.mIsBookmarkCallback);
        } else {
            this.mDataController.queryBookmarkStatus(SiteNavigation.SITE_NAVIGATION_URI.toString(), this.mIsBookmarkCallback);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateCaptureFromBlob(byte[] bArr) {
        synchronized (this) {
            if (this.mCapture == null) {
                return;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(wrap.array(), wrap.arrayOffset(), wrap.capacity(), options);
            if (decodeByteArray == null) {
                return;
            }
            try {
                this.mCapture = Bitmap.createScaledBitmap(decodeByteArray, this.mCapture.getWidth(), this.mCapture.getHeight(), true);
            } catch (RuntimeException e) {
                Log.e("Tab", "Load capture has mismatched sizes; buffer: " + wrap.capacity() + " blob: " + bArr.length + "capture: " + this.mCapture.getByteCount());
                throw e;
            }
        }
    }

    public void updateShouldCaptureThumbnails() {
        if (this.mWebViewController.shouldCaptureThumbnails()) {
            synchronized (this) {
                if (this.mCapture == null) {
                    this.mCapture = Bitmap.createBitmap(this.mCaptureWidth, this.mCaptureHeight, Bitmap.Config.RGB_565);
                    this.mCapture.eraseColor(-1);
                    if (this.mInForeground) {
                        postCapture();
                    }
                }
            }
        }
        synchronized (this) {
            this.mCapture = null;
            deleteThumbnail();
        }
    }
}
