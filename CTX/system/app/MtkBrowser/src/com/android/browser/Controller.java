package com.android.browser;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.net.WebAddress;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import com.android.browser.IntentHandler;
import com.android.browser.PermissionHelper;
import com.android.browser.UI;
import com.android.browser.provider.BrowserContract;
import com.android.browser.provider.BrowserProvider2;
import com.android.browser.provider.SnapshotProvider;
import com.android.browser.sitenavigation.SiteNavigation;
import com.mediatek.browser.ext.IBrowserMiscExt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/* loaded from: classes.dex */
public class Controller implements ActivityController, UiController, WebViewController {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String[] IMAGE_VIEWABLE_SCHEMES;
    private static final String[] STORAGE_PERMISSIONS;
    private static final int[] WINDOW_SHORTCUT_ID_ARRAY;
    private static String mSavePageFolder;
    private static Bitmap sThumbnailBitmap;
    private ActionMode mActionMode;
    private Activity mActivity;
    private boolean mBlockEvents;
    private ContentObserver mBookmarksObserver;
    private Notification.Builder mBuilder;
    private Menu mCachedMenu;
    private boolean mConfigChanged;
    private CrashRecoveryHandler mCrashRecoveryHandler;
    private boolean mExtendedMenuOpen;
    private WebViewFactory mFactory;
    private Handler mHandler;
    private IntentHandler mIntentHandler;
    private boolean mLoadStopped;
    private boolean mMenuIsDown;
    private NetworkStateHandler mNetworkHandler;
    private NotificationManager mNotificationManager;
    private boolean mOptionsMenuOpen;
    private PageDialogsHandler mPageDialogsHandler;
    private UpdateSavePageDBHandler mSavePageHandler;
    private boolean mShouldShowErrorConsole;
    private ContentObserver mSiteNavigationObserver;
    private SystemAllowGeolocationOrigins mSystemAllowGeolocationOrigins;
    private UI mUi;
    private UploadHandler mUploadHandler;
    private UrlHandler mUrlHandler;
    private String mVoiceResult;
    private PowerManager.WakeLock mWakeLock;
    private static final boolean DEBUG = Browser.DEBUG;
    private static final String SAVE_PAGE_DIR = File.separator + "Download" + File.separator + "SavedPages";
    private static HandlerThread sUpdateSavePageThread = new HandlerThread("save_page");
    private HashMap<Integer, Integer> mProgress = new HashMap<>();
    private WallpaperHandler mWallpaperHandler = null;
    private int mCurrentMenuState = 0;
    private int mMenuState = R.id.MAIN_MENU;
    private int mOldMenuState = -1;
    private boolean mActivityPaused = true;
    private IBrowserMiscExt mBrowserMiscExt = null;
    private boolean mDelayRemoveLastTab = false;
    private BrowserSettings mSettings = BrowserSettings.getInstance();
    private TabControl mTabControl = new TabControl(this);

    static {
        sUpdateSavePageThread.start();
        WINDOW_SHORTCUT_ID_ARRAY = new int[]{R.id.window_one_menu_id, R.id.window_two_menu_id, R.id.window_three_menu_id, R.id.window_four_menu_id, R.id.window_five_menu_id, R.id.window_six_menu_id, R.id.window_seven_menu_id, R.id.window_eight_menu_id};
        IMAGE_VIEWABLE_SCHEMES = new String[]{"data", "http", "https", "file"};
        STORAGE_PERMISSIONS = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    }

    public Controller(Activity activity) {
        this.mActivity = activity;
        this.mSettings.setController(this);
        this.mCrashRecoveryHandler = CrashRecoveryHandler.initialize(this);
        this.mCrashRecoveryHandler.preloadCrashState();
        this.mFactory = new BrowserWebViewFactory(activity);
        this.mUrlHandler = new UrlHandler(this);
        this.mIntentHandler = new IntentHandler(this.mActivity, this);
        this.mPageDialogsHandler = new PageDialogsHandler(this.mActivity, this);
        startHandler();
        this.mSavePageHandler = new UpdateSavePageDBHandler(sUpdateSavePageThread.getLooper());
        this.mBuilder = new Notification.Builder(this.mActivity);
        this.mNotificationManager = (NotificationManager) this.mActivity.getSystemService("notification");
        this.mBookmarksObserver = new ContentObserver(this.mHandler) { // from class: com.android.browser.Controller.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int tabCount = Controller.this.mTabControl.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    Controller.this.mTabControl.getTab(i).updateBookmarkedStatus();
                }
            }
        };
        this.mSiteNavigationObserver = new ContentObserver(this.mHandler) { // from class: com.android.browser.Controller.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                Log.d("Controller", "SiteNavigation.SITE_NAVIGATION_URI changed");
                if (Controller.this.getCurrentTopWebView() != null && Controller.this.getCurrentTopWebView().getUrl() != null && Controller.this.getCurrentTopWebView().getUrl().equals("content://com.android.browser.site_navigation/websites")) {
                    Log.d("Controller", "start reload");
                    Controller.this.getCurrentTopWebView().reload();
                }
            }
        };
        activity.getContentResolver().registerContentObserver(BrowserContract.Bookmarks.CONTENT_URI, true, this.mBookmarksObserver);
        activity.getContentResolver().registerContentObserver(SiteNavigation.SITE_NAVIGATION_URI, true, this.mSiteNavigationObserver);
        this.mNetworkHandler = new NetworkStateHandler(this.mActivity, this);
        this.mSystemAllowGeolocationOrigins = new SystemAllowGeolocationOrigins(this.mActivity.getApplicationContext());
        this.mSystemAllowGeolocationOrigins.start();
        openIconDatabase();
    }

    @Override // com.android.browser.ActivityController
    public void start(Intent intent) {
        this.mCrashRecoveryHandler.startRecovery(intent);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void doStart(final Bundle bundle, final Intent intent) {
        Calendar calendar;
        if (bundle != null) {
            calendar = (Calendar) bundle.getSerializable("lastActiveDate");
        } else {
            calendar = null;
        }
        Calendar calendar2 = Calendar.getInstance();
        Calendar calendar3 = Calendar.getInstance();
        calendar3.add(5, -1);
        final boolean z = (calendar == null || calendar.before(calendar3) || calendar.after(calendar2)) ? false : true;
        final long canRestoreState = this.mTabControl.canRestoreState(bundle, z);
        if (canRestoreState == -1) {
            CookieManager.getInstance().removeSessionCookies(null);
        }
        GoogleAccountLogin.startLoginIfNeeded(this.mActivity, new Runnable() { // from class: com.android.browser.Controller.3
            @Override // java.lang.Runnable
            public void run() {
                Controller.this.onPreloginFinished(bundle, intent, canRestoreState, z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPreloginFinished(Bundle bundle, Intent intent, long j, boolean z) {
        IntentHandler.UrlData urlDataFromIntent;
        Tab openTab;
        int i;
        if (j == -1) {
            BackgroundHandler.execute(new PruneThumbnails(this.mActivity, null));
            if (intent == null) {
                openTabToHomePage();
            } else {
                Bundle extras = intent.getExtras();
                if (intent.getData() != null && "android.intent.action.VIEW".equals(intent.getAction()) && intent.getData().toString().startsWith("content://")) {
                    urlDataFromIntent = new IntentHandler.UrlData(intent.getData().toString());
                } else {
                    urlDataFromIntent = IntentHandler.getUrlDataFromIntent(intent);
                }
                if (urlDataFromIntent.isEmpty()) {
                    openTab = openTabToHomePage();
                } else {
                    openTab = openTab(urlDataFromIntent);
                }
                if (openTab != null) {
                    openTab.setAppId(intent.getStringExtra("com.android.browser.application_id"));
                }
                WebView webView = openTab.getWebView();
                if (extras != null && (i = extras.getInt("browser.initialZoomLevel", 0)) > 0 && i <= 1000) {
                    webView.setInitialScale(i);
                }
            }
            this.mUi.updateTabs(this.mTabControl.getTabs());
        } else {
            this.mTabControl.restoreState(bundle, j, z, this.mUi.needsRestoreAllTabs());
            List<Tab> tabs = this.mTabControl.getTabs();
            ArrayList arrayList = new ArrayList(tabs.size());
            for (Tab tab : tabs) {
                arrayList.add(Long.valueOf(tab.getId()));
            }
            BackgroundHandler.execute(new PruneThumbnails(this.mActivity, arrayList));
            if (tabs.size() == 0) {
                openTabToHomePage();
            }
            this.mUi.updateTabs(tabs);
            setActiveTab(this.mTabControl.getCurrentTab());
            if (intent != null) {
                this.mIntentHandler.onNewIntent(intent);
            }
        }
        getSettings().getJsEngineFlags();
        if (intent != null && "show_bookmarks".equals(intent.getAction())) {
            bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class PruneThumbnails implements Runnable {
        private Context mContext;
        private List<Long> mIds;

        PruneThumbnails(Context context, List<Long> list) {
            this.mContext = context.getApplicationContext();
            this.mIds = list;
        }

        @Override // java.lang.Runnable
        public void run() {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (this.mIds == null || this.mIds.size() == 0) {
                contentResolver.delete(BrowserProvider2.Thumbnails.CONTENT_URI, null, null);
                return;
            }
            int size = this.mIds.size();
            StringBuilder sb = new StringBuilder();
            sb.append("_id");
            sb.append(" not in (");
            for (int i = 0; i < size; i++) {
                sb.append(this.mIds.get(i));
                if (i < size - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            contentResolver.delete(BrowserProvider2.Thumbnails.CONTENT_URI, sb.toString(), null);
        }
    }

    public WebViewFactory getWebViewFactory() {
        return this.mFactory;
    }

    @Override // com.android.browser.WebViewController
    public void onSetWebView(Tab tab, WebView webView) {
        this.mUi.onSetWebView(tab, webView);
    }

    @Override // com.android.browser.WebViewController
    public void createSubWindow(Tab tab) {
        boolean isPrivateBrowsingEnabled;
        endActionMode();
        WebView webView = tab.getWebView();
        WebViewFactory webViewFactory = this.mFactory;
        if (webView == null) {
            isPrivateBrowsingEnabled = false;
        } else {
            isPrivateBrowsingEnabled = webView.isPrivateBrowsingEnabled();
        }
        this.mUi.createSubWindow(tab, webViewFactory.createWebView(isPrivateBrowsingEnabled));
    }

    @Override // com.android.browser.WebViewController
    public Context getContext() {
        return this.mActivity;
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public Activity getActivity() {
        return this.mActivity;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setUi(UI ui) {
        this.mUi = ui;
    }

    @Override // com.android.browser.UiController
    public BrowserSettings getSettings() {
        return this.mSettings;
    }

    @Override // com.android.browser.UiController
    public UI getUi() {
        return this.mUi;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getMaxTabs() {
        int integer = this.mActivity.getResources().getInteger(R.integer.max_tabs);
        String str = SystemProperties.get("ro.vendor.gmo.ram_optimize");
        if (str != null && str.equals("1")) {
            return integer / 2;
        }
        return integer;
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public TabControl getTabControl() {
        return this.mTabControl;
    }

    @Override // com.android.browser.UiController
    public List<Tab> getTabs() {
        return this.mTabControl.getTabs();
    }

    private void openIconDatabase() {
        final WebIconDatabase webIconDatabase = WebIconDatabase.getInstance();
        BackgroundHandler.execute(new Runnable() { // from class: com.android.browser.Controller.4
            @Override // java.lang.Runnable
            public void run() {
                webIconDatabase.open(Controller.this.mActivity.getDir("icons", 0).getPath());
            }
        });
    }

    private void startHandler() {
        this.mHandler = new Handler() { // from class: com.android.browser.Controller.5
            /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                WebView webView;
                switch (message.what) {
                    case 102:
                        String str = (String) message.getData().get("url");
                        String str2 = (String) message.getData().get("title");
                        String str3 = (String) message.getData().get("src");
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->FOCUS_NODE_HREF----url : " + str + ", title : " + str2 + ", src : " + str3);
                        }
                        String str4 = str == "" ? str3 : str;
                        if (!TextUtils.isEmpty(str4) && Controller.this.getCurrentTopWebView() == (webView = (WebView) ((HashMap) message.obj).get("webview"))) {
                            int i = message.arg1;
                            if (i == R.id.open_context_menu_id) {
                                if (str4 != null && str4.startsWith("rtsp://")) {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    intent.setData(Uri.parse(str4.replaceAll(" ", "%20")));
                                    intent.addFlags(268435456);
                                    Controller.this.mActivity.startActivity(intent);
                                    return;
                                } else if (str4 != null && str4.startsWith("wtai://wp/mc;")) {
                                    String replaceAll = str4.replaceAll(" ", "%20");
                                    Controller.this.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("tel:" + replaceAll.substring("wtai://wp/mc;".length()))));
                                    return;
                                } else {
                                    Controller.this.loadUrlFromContext(str4);
                                    return;
                                }
                            }
                            switch (i) {
                                case R.id.open_newtab_context_menu_id /* 2131558624 */:
                                    Controller.this.openTab(str4, Controller.this.mTabControl.getCurrentTab(), !Controller.this.mSettings.openInBackground(), true);
                                    return;
                                case R.id.save_link_context_menu_id /* 2131558625 */:
                                    break;
                                case R.id.copy_link_context_menu_id /* 2131558626 */:
                                    Controller.this.copy(str4);
                                    return;
                                case R.id.save_link_tobookmark_context_menu_id /* 2131558627 */:
                                    Intent createBookmarkLinkIntent = Controller.this.createBookmarkLinkIntent(str4);
                                    if (createBookmarkLinkIntent != null) {
                                        Controller.this.mActivity.startActivity(createBookmarkLinkIntent);
                                        return;
                                    }
                                    return;
                                default:
                                    switch (i) {
                                        case R.id.download_context_menu_id /* 2131558629 */:
                                            break;
                                        case R.id.view_image_context_menu_id /* 2131558630 */:
                                            Controller.this.loadUrlFromContext(str3);
                                            return;
                                        default:
                                            return;
                                    }
                            }
                            DownloadHandler.onDownloadStartNoStream(Controller.this.mActivity, str4, webView.getSettings().getUserAgentString(), null, null, null, webView.isPrivateBrowsingEnabled(), 0L);
                            return;
                        }
                        return;
                    case 107:
                        if (Controller.this.mWakeLock != null && Controller.this.mWakeLock.isHeld()) {
                            if (Controller.DEBUG) {
                                Log.i("browser", "Controller.startHandler()--->RELEASE_WAKELOCK");
                            }
                            Controller.this.mWakeLock.release();
                            Controller.this.mTabControl.stopAllLoading();
                            return;
                        }
                        return;
                    case 108:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->UPDATE_BOOKMARK_THUMBNAIL");
                        }
                        Tab tab = (Tab) message.obj;
                        if (tab != null) {
                            Controller.this.updateScreenshot(tab);
                            return;
                        }
                        return;
                    case 201:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->OPEN_BOOKMARKS");
                        }
                        Controller.this.bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
                        return;
                    case 1001:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->LOAD_URL");
                        }
                        Controller.this.loadUrlFromContext((String) message.obj);
                        return;
                    case 1002:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->STOP_LOAD");
                        }
                        Controller.this.stopLoading();
                        return;
                    case 1100:
                        Controller.this.getTabControl().freeMemory();
                        new CheckMemoryTask(Controller.this.mHandler).execute(Integer.valueOf(Controller.this.getTabControl().getVisibleWebviewNums()), null, false, null, Controller.this.getTabControl().getFreeTabIndex(), false);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    @Override // com.android.browser.UiController
    public Tab getCurrentTab() {
        return this.mTabControl.getCurrentTab();
    }

    @Override // com.android.browser.UiController
    public void shareCurrentPage() {
        shareCurrentPage(this.mTabControl.getCurrentTab());
    }

    private void shareCurrentPage(Tab tab) {
        if (tab != null) {
            sharePage(this.mActivity, tab.getTitle(), tab.getUrl(), tab.getFavicon(), createScreenshot(tab.getWebView(), getDesiredThumbnailWidth(this.mActivity), getDesiredThumbnailHeight(this.mActivity)));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final void sharePage(Context context, String str, String str2, Bitmap bitmap, Bitmap bitmap2) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.TEXT", str2);
        intent.putExtra("android.intent.extra.SUBJECT", str);
        if (bitmap != null && bitmap.getWidth() > 60) {
            bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
        }
        intent.putExtra("share_favicon", bitmap);
        intent.putExtra("share_screenshot", bitmap2);
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.choosertitle_sharevia)));
        } catch (ActivityNotFoundException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void copy(CharSequence charSequence) {
        ((ClipboardManager) this.mActivity.getSystemService("clipboard")).setText(charSequence);
    }

    @Override // com.android.browser.ActivityController
    public void onConfgurationChanged(Configuration configuration) {
        this.mConfigChanged = true;
        this.mActivity.invalidateOptionsMenu();
        if (this.mPageDialogsHandler != null) {
            this.mPageDialogsHandler.onConfigurationChanged(configuration);
        }
        this.mUi.onConfigurationChanged(configuration);
        this.mSettings.onConfigurationChanged(configuration);
    }

    @Override // com.android.browser.ActivityController, com.android.browser.UiController
    public void handleNewIntent(Intent intent) {
        if (getTabControl().getTabCount() == 0) {
            start(intent);
        }
        if (!this.mUi.isWebShowing()) {
            this.mUi.showWeb(false);
        }
        this.mIntentHandler.onNewIntent(intent);
    }

    @Override // com.android.browser.ActivityController
    public void onPause() {
        if (this.mCachedMenu != null) {
            this.mCachedMenu.close();
        }
        if (this.mUi.isCustomViewShowing()) {
            hideCustomView();
        }
        if (this.mActivityPaused) {
            Log.e("Controller", "BrowserActivity is already paused.");
            return;
        }
        this.mActivityPaused = true;
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab != null) {
            currentTab.pause();
            if (!this.mDelayRemoveLastTab && !pauseWebViewTimers(currentTab)) {
                if (this.mWakeLock == null) {
                    this.mWakeLock = ((PowerManager) this.mActivity.getSystemService("power")).newWakeLock(1, "Browser");
                }
                this.mWakeLock.acquire();
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(107), 300000L);
            }
        }
        this.mUi.onPause();
        this.mNetworkHandler.onPause();
        WebView.disablePlatformNotifications();
        NfcHandler.unregister(this.mActivity);
        if (sThumbnailBitmap != null) {
            sThumbnailBitmap.recycle();
            sThumbnailBitmap = null;
        }
    }

    @Override // com.android.browser.ActivityController
    public void onSaveInstanceState(Bundle bundle) {
        this.mCrashRecoveryHandler.writeState(createSaveState());
        this.mSettings.setLastRunPaused(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Bundle createSaveState() {
        Bundle bundle = new Bundle();
        this.mTabControl.saveState(bundle);
        if (!bundle.isEmpty()) {
            bundle.putSerializable("lastActiveDate", Calendar.getInstance());
        }
        return bundle;
    }

    @Override // com.android.browser.ActivityController
    public void onResume() {
        if (!this.mActivityPaused) {
            Log.e("Controller", "BrowserActivity is already resumed.");
            return;
        }
        this.mSettings.setLastRunPaused(false);
        this.mActivityPaused = false;
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab != null) {
            currentTab.resume();
            resumeWebViewTimers(currentTab);
        }
        releaseWakeLock();
        this.mUi.onResume();
        this.mNetworkHandler.onResume();
        WebView.enablePlatformNotifications();
        NfcHandler.register(this.mActivity, this);
        if (this.mVoiceResult != null) {
            this.mUi.onVoiceResult(this.mVoiceResult);
            this.mVoiceResult = null;
        }
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mHandler.removeMessages(107);
            this.mWakeLock.release();
        }
    }

    private void resumeWebViewTimers(Tab tab) {
        boolean inPageLoad = tab.inPageLoad();
        if ((!this.mActivityPaused && !inPageLoad) || (this.mActivityPaused && inPageLoad)) {
            WebViewTimersControl.getInstance().onBrowserActivityResume(tab.getWebView(), this);
        }
    }

    private boolean pauseWebViewTimers(Tab tab) {
        if (tab == null) {
            return true;
        }
        if (!tab.inPageLoad()) {
            WebViewTimersControl.getInstance().onBrowserActivityPause(getCurrentWebView(), this);
            return true;
        }
        return false;
    }

    @Override // com.android.browser.ActivityController
    public void onDestroy() {
        if (this.mPageDialogsHandler != null) {
            this.mPageDialogsHandler.destroyDialogs();
        }
        if (this.mWallpaperHandler != null) {
            this.mWallpaperHandler.destroyDialog();
            this.mWallpaperHandler = null;
        }
        if (this.mUploadHandler != null && !this.mUploadHandler.handled()) {
            this.mUploadHandler.onResult(0, null);
            this.mUploadHandler = null;
        }
        if (this.mTabControl == null) {
            return;
        }
        this.mUi.onDestroy();
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab != null) {
            dismissSubWindow(currentTab);
            removeTab(currentTab);
        }
        this.mActivity.getContentResolver().unregisterContentObserver(this.mBookmarksObserver);
        this.mActivity.getContentResolver().unregisterContentObserver(this.mSiteNavigationObserver);
        this.mTabControl.destroy();
        WebIconDatabase.getInstance().close();
        this.mSystemAllowGeolocationOrigins.stop();
        this.mSystemAllowGeolocationOrigins = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isActivityPaused() {
        return this.mActivityPaused;
    }

    @Override // com.android.browser.ActivityController
    public void onLowMemory() {
        this.mTabControl.freeMemory();
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public boolean shouldShowErrorConsole() {
        return this.mShouldShowErrorConsole;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setShouldShowErrorConsole(boolean z) {
        if (z == this.mShouldShowErrorConsole) {
            return;
        }
        this.mShouldShowErrorConsole = z;
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab == null) {
            return;
        }
        this.mUi.setShouldShowErrorConsole(currentTab, z);
    }

    @Override // com.android.browser.UiController
    public void stopLoading() {
        this.mLoadStopped = true;
        Tab currentTab = this.mTabControl.getCurrentTab();
        WebView currentTopWebView = getCurrentTopWebView();
        if (currentTopWebView != null) {
            currentTopWebView.stopLoading();
            this.mUi.onPageStopped(currentTab);
        }
    }

    boolean didUserStopLoading() {
        return this.mLoadStopped;
    }

    @Override // com.android.browser.WebViewController
    public void onPageStarted(Tab tab, WebView webView, Bitmap bitmap) {
        this.mHandler.removeMessages(108, tab);
        this.mBrowserMiscExt = Extensions.getMiscPlugin(this.mActivity);
        this.mBrowserMiscExt.processNetworkNotify(webView, this.mActivity, this.mNetworkHandler.isNetworkUp());
        if (this.mActivityPaused) {
            resumeWebViewTimers(tab);
        }
        boolean z = false;
        this.mLoadStopped = false;
        endActionMode();
        this.mUi.onTabDataChanged(tab);
        if (tab.inForeground()) {
            UI ui = this.mUi;
            if (tab.canGoBack() || tab.getParent() != null) {
                z = true;
            }
            ui.updateBottomBarState(true, z, tab.canGoForward());
        }
        String url = tab.getUrl();
        maybeUpdateFavicon(tab, null, url, bitmap);
        Performance.tracePageStart(url);
    }

    @Override // com.android.browser.WebViewController
    public void onPageFinished(Tab tab) {
        if (!this.mDelayRemoveLastTab) {
            if (DEBUG) {
                Log.i("Controller", "onPageFinished backupState " + tab.getUrl());
            }
            this.mCrashRecoveryHandler.backupState();
        }
        this.mUi.onTabDataChanged(tab);
        if (this.mActivityPaused && pauseWebViewTimers(tab)) {
            releaseWakeLock();
        }
        if (tab.getWebView() != null && tab.inForeground()) {
            this.mUi.updateBottomBarState(tab.getWebView().canScrollVertically(-1) || tab.getWebView().canScrollVertically(1), tab.canGoBack() || tab.getParent() != null, tab.canGoForward());
        }
        Performance.tracePageFinished();
        Performance.dumpSystemMemInfo(this.mActivity);
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(getTabControl().getTabPosition(tab)));
        new CheckMemoryTask(this.mHandler).execute(Integer.valueOf(getTabControl().getVisibleWebviewNums()), arrayList, true, tab.getUrl(), null, false);
    }

    @Override // com.android.browser.WebViewController
    public void onProgressChanged(Tab tab) {
        int loadProgress = tab.getLoadProgress();
        if (DEBUG) {
            Log.i("Controller", "Network_Issue onProgressChanged url: " + tab.getUrl() + " : " + loadProgress + "%");
        }
        if (loadProgress == 100) {
            boolean z = false;
            if (!tab.isPrivateBrowsingEnabled() && !TextUtils.isEmpty(tab.getUrl()) && !tab.isSnapshot() && tab.shouldUpdateThumbnail() && (((tab.inForeground() && !didUserStopLoading()) || !tab.inForeground()) && !this.mHandler.hasMessages(108, tab))) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(108, 0, 0, tab), 500L);
            }
            if (tab.getWebView() != null && tab.inForeground()) {
                boolean z2 = tab.getWebView().canScrollVertically(-1) || tab.getWebView().canScrollVertically(1);
                UI ui = this.mUi;
                if (tab.canGoBack() || tab.getParent() != null) {
                    z = true;
                }
                ui.updateBottomBarState(z2, z, tab.canGoForward());
            }
        }
        this.mUi.onProgressChanged(tab);
    }

    @Override // com.android.browser.WebViewController
    public void onUpdatedSecurityState(Tab tab) {
        this.mUi.onTabDataChanged(tab);
    }

    @Override // com.android.browser.WebViewController
    public void onReceivedTitle(Tab tab, String str) {
        this.mUi.onTabDataChanged(tab);
        String originalUrl = tab.getOriginalUrl();
        if (!TextUtils.isEmpty(originalUrl) && originalUrl.length() < 50000 && !tab.isPrivateBrowsingEnabled()) {
            DataController.getInstance(this.mActivity).updateHistoryTitle(originalUrl, str);
        }
    }

    @Override // com.android.browser.WebViewController
    public void onFavicon(Tab tab, WebView webView, Bitmap bitmap) {
        this.mUi.onTabDataChanged(tab);
        maybeUpdateFavicon(tab, webView.getOriginalUrl(), webView.getUrl(), bitmap);
    }

    @Override // com.android.browser.WebViewController
    public boolean shouldOverrideUrlLoading(Tab tab, WebView webView, String str) {
        boolean shouldOverrideUrlLoading = this.mUrlHandler.shouldOverrideUrlLoading(tab, webView, str);
        if (tab.inForeground()) {
            this.mUi.updateBottomBarState(true, tab.canGoBack(), tab.canGoForward());
        }
        return shouldOverrideUrlLoading;
    }

    @Override // com.android.browser.WebViewController
    public void sendErrorCode(int i, String str) {
        Intent intent = new Intent("com.android.browser.action.SEND_ERROR");
        intent.putExtra("com.android.browser.error_code_key", i);
        intent.putExtra("com.android.browser.url_key", str);
        intent.putExtra("com.android.browser.homepage_key", this.mSettings.getHomePage());
        this.mActivity.sendBroadcast(intent);
    }

    @Override // com.android.browser.WebViewController
    public boolean shouldOverrideKeyEvent(KeyEvent keyEvent) {
        if (this.mMenuIsDown) {
            return this.mActivity.getWindow().isShortcutKey(keyEvent.getKeyCode(), keyEvent);
        }
        return false;
    }

    @Override // com.android.browser.WebViewController
    public boolean onUnhandledKeyEvent(KeyEvent keyEvent) {
        if (!isActivityPaused()) {
            if (keyEvent.getAction() == 0) {
                return this.mActivity.onKeyDown(keyEvent.getKeyCode(), keyEvent);
            }
            return this.mActivity.onKeyUp(keyEvent.getKeyCode(), keyEvent);
        }
        return false;
    }

    @Override // com.android.browser.WebViewController
    public void doUpdateVisitedHistory(Tab tab, boolean z) {
        if (DEBUG) {
            Log.i("browser", "Controller.doUpdateVisitedHistory()--->tab = " + tab + ", isReload = " + z);
        }
        if (tab.isPrivateBrowsingEnabled()) {
            return;
        }
        String originalUrl = tab.getOriginalUrl();
        if (TextUtils.isEmpty(originalUrl) || originalUrl.regionMatches(true, 0, "about:", 0, 6)) {
            return;
        }
        DataController.getInstance(this.mActivity).updateVisitedHistory(originalUrl);
        this.mCrashRecoveryHandler.backupState();
    }

    @Override // com.android.browser.WebViewController
    public void getVisitedHistory(final ValueCallback<String[]> valueCallback) {
        new AsyncTask<Void, Void, String[]>() { // from class: com.android.browser.Controller.6
            @Override // android.os.AsyncTask
            public String[] doInBackground(Void... voidArr) {
                return com.android.browser.provider.Browser.getVisitedHistory(Controller.this.mActivity.getContentResolver());
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(String[] strArr) {
                valueCallback.onReceiveValue(strArr);
            }
        }.execute(new Void[0]);
    }

    @Override // com.android.browser.WebViewController
    public void onReceivedHttpAuthRequest(Tab tab, WebView webView, HttpAuthHandler httpAuthHandler, String str, String str2) {
        String str3;
        String[] httpAuthUsernamePassword;
        String str4 = null;
        if (httpAuthHandler.useHttpAuthUsernamePassword() && webView != null && (httpAuthUsernamePassword = webView.getHttpAuthUsernamePassword(str, str2)) != null && httpAuthUsernamePassword.length == 2) {
            str4 = httpAuthUsernamePassword[0];
            str3 = httpAuthUsernamePassword[1];
        } else {
            str3 = null;
        }
        if (str4 != null && str3 != null) {
            httpAuthHandler.proceed(str4, str3);
        } else if (tab.inForeground()) {
            this.mPageDialogsHandler.showHttpAuthentication(tab, httpAuthHandler, str, str2);
        } else {
            httpAuthHandler.cancel();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:53:0x01ee  */
    @Override // com.android.browser.WebViewController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onDownloadStart(final Tab tab, final String str, final String str2, final String str3, String str4, String str5, final long j) {
        Tab tab2;
        String str6;
        WebView webView;
        String str7;
        String str8;
        WebView webView2;
        WebView webView3 = tab.getWebView();
        StringBuilder sb = new StringBuilder();
        sb.append("onDownloadStart: dispos=");
        sb.append(str3 == null ? "null" : str3);
        Log.d("browser/Controller", sb.toString());
        if (str3 == null || !str3.regionMatches(true, 0, "attachment", 0, 10)) {
            final Intent intent = new Intent("android.intent.action.VIEW");
            String str9 = str.startsWith("http://vod02.v.vnet.mobi/mobi/vod/st02") ? "video/3gp" : str4;
            intent.setDataAndType(Uri.parse(str), str9);
            intent.addFlags(268435456);
            ResolveInfo resolveActivity = this.mActivity.getPackageManager().resolveActivity(intent, 65536);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("onDownloadStart: ResolveInfo=");
            sb2.append(resolveActivity == null ? "null" : resolveActivity);
            Log.d("browser/Controller", sb2.toString());
            if (resolveActivity != null) {
                ComponentName componentName = this.mActivity.getComponentName();
                Log.d("browser/Controller", "onDownloadStart: myName=" + componentName + ", myName.packageName=" + componentName.getPackageName() + ", info.packageName=" + resolveActivity.activityInfo.packageName + ", myName.name=" + componentName.getClassName() + ", info.name=" + resolveActivity.activityInfo.name);
                if (!componentName.getPackageName().equals(resolveActivity.activityInfo.packageName) || !componentName.getClassName().equals(resolveActivity.activityInfo.name)) {
                    Log.d("browser/Controller", "onDownloadStart: mimetype=" + str9);
                    if (str9.equalsIgnoreCase("application/x-mpegurl") || str9.equalsIgnoreCase("application/vnd.apple.mpegurl")) {
                        this.mActivity.startActivity(intent);
                        if (webView3 != null && webView3.copyBackForwardList().getSize() == 0) {
                            if (tab == this.mTabControl.getCurrentTab()) {
                                goBackOnePageOrQuit();
                                return;
                            } else {
                                closeTab(tab);
                                return;
                            }
                        }
                        return;
                    }
                    try {
                        final Activity activity = this.mActivity;
                        final TabControl tabControl = this.mTabControl;
                        webView2 = webView3;
                        final String str10 = str9;
                        str8 = str9;
                        try {
                            new AlertDialog.Builder(activity).setTitle(R.string.application_name).setIcon(17301659).setMessage(R.string.download_or_open_content).setPositiveButton(R.string.save_content, new DialogInterface.OnClickListener() { // from class: com.android.browser.Controller.9
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    DownloadHandler.onDownloadStartNoStream(activity, str, str2, str3, str10, null, false, j);
                                    Log.d("browser/Controller", "User decide to download the content");
                                    WebView webView4 = tab.getWebView();
                                    if (webView4 != null && webView4.copyBackForwardList().getSize() == 0) {
                                        if (tab == tabControl.getCurrentTab()) {
                                            Controller.this.goBackOnePageOrQuit();
                                        } else {
                                            Controller.this.closeTab(tab);
                                        }
                                    }
                                }
                            }).setNegativeButton(R.string.open_content, new DialogInterface.OnClickListener() { // from class: com.android.browser.Controller.8
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (str != null) {
                                        String cookie = CookieManager.getInstance().getCookie(str);
                                        if (Controller.DEBUG) {
                                            Log.i("browser/Controller", "url: " + str + " url cookie: " + cookie);
                                        }
                                        if (cookie != null) {
                                            intent.putExtra("url-cookie", cookie);
                                        }
                                    }
                                    activity.startActivity(intent);
                                    Log.d("browser/Controller", "User decide to open the content by startActivity");
                                    WebView webView4 = tab.getWebView();
                                    if (webView4 != null && webView4.copyBackForwardList().getSize() == 0) {
                                        if (tab == tabControl.getCurrentTab()) {
                                            Controller.this.goBackOnePageOrQuit();
                                        } else {
                                            Controller.this.closeTab(tab);
                                        }
                                    }
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.android.browser.Controller.7
                                @Override // android.content.DialogInterface.OnCancelListener
                                public void onCancel(DialogInterface dialogInterface) {
                                    Log.d("browser/Controller", "User cancel the download action");
                                }
                            }).show();
                            return;
                        } catch (ActivityNotFoundException e) {
                            e = e;
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append("activity not found for ");
                            str6 = str8;
                            sb3.append(str6);
                            sb3.append(" over ");
                            sb3.append(Uri.parse(str).getScheme());
                            Log.d("Controller", sb3.toString(), e);
                            tab2 = tab;
                            webView = webView2;
                            str7 = str6;
                            if (DEBUG) {
                            }
                            downloadStart(this.mActivity, str, str2, str3, str7, str5, false, j, webView, tab2);
                        }
                    } catch (ActivityNotFoundException e2) {
                        e = e2;
                        str8 = str9;
                        webView2 = webView3;
                    }
                }
            }
            tab2 = tab;
            str6 = str9;
            webView = webView3;
            str7 = str6;
        } else {
            tab2 = tab;
            str7 = str4;
            webView = webView3;
        }
        if (DEBUG) {
            Log.d("browser/Controller", "onDownloadStart: download directly, mimetype=" + str7 + ", url=" + str);
        }
        downloadStart(this.mActivity, str, str2, str3, str7, str5, false, j, webView, tab2);
    }

    private void downloadStart(Activity activity, final String str, final String str2, final String str3, final String str4, final String str5, boolean z, final long j, final WebView webView, final Tab tab) {
        final List<String> ungrantedPermissions = PermissionHelper.getInstance().getUngrantedPermissions(STORAGE_PERMISSIONS);
        if (ungrantedPermissions.size() != 0) {
            PermissionHelper.getInstance().requestPermissions(ungrantedPermissions, new PermissionHelper.PermissionCallback() { // from class: com.android.browser.Controller.10
                @Override // com.android.browser.PermissionHelper.PermissionCallback
                public void onPermissionsResult(int i, String[] strArr, int[] iArr) {
                    boolean z2;
                    Log.d("browser/Controller", " onRequestPermissionsResult " + i);
                    if (iArr != null && iArr.length > 0) {
                        Iterator it = ungrantedPermissions.iterator();
                        while (true) {
                            z2 = false;
                            boolean z3 = true;
                            if (it.hasNext()) {
                                String str6 = (String) it.next();
                                int i2 = 0;
                                while (true) {
                                    if (i2 < iArr.length) {
                                        if (!str6.equalsIgnoreCase(strArr[i2]) || iArr[i2] != 0) {
                                            i2++;
                                        }
                                    } else {
                                        z3 = false;
                                        continue;
                                        break;
                                    }
                                }
                                if (!z3) {
                                    Log.d("browser/Controller", str6 + " is not granted !");
                                    break;
                                }
                            } else {
                                z2 = true;
                                break;
                            }
                        }
                        if (z2) {
                            DownloadHandler.onDownloadStart(Controller.this.mActivity, str, str2, str3, str4, str5, false, j);
                        }
                        if (webView != null && webView.copyBackForwardList().getSize() == 0) {
                            if (tab == Controller.this.mTabControl.getCurrentTab()) {
                                Controller.this.goBackOnePageOrQuit();
                            } else {
                                Controller.this.closeTab(tab);
                            }
                        }
                    }
                }
            });
            return;
        }
        DownloadHandler.onDownloadStart(this.mActivity, str, str2, str3, str4, str5, false, j);
        if (webView != null && webView.copyBackForwardList().getSize() == 0) {
            if (tab == this.mTabControl.getCurrentTab()) {
                tab.setCloseOnBack(true);
                goBackOnePageOrQuit();
                return;
            }
            closeTab(tab);
        }
    }

    @Override // com.android.browser.WebViewController
    public Bitmap getDefaultVideoPoster() {
        return this.mUi.getDefaultVideoPoster();
    }

    @Override // com.android.browser.WebViewController
    public View getVideoLoadingProgressView() {
        return this.mUi.getVideoLoadingProgressView();
    }

    @Override // com.android.browser.WebViewController
    public void showSslCertificateOnError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        this.mPageDialogsHandler.showSSLCertificateOnError(webView, sslErrorHandler, sslError);
    }

    @Override // com.android.browser.WebViewController
    public void showAutoLogin(Tab tab) {
        this.mUi.showAutoLogin(tab);
    }

    @Override // com.android.browser.WebViewController
    public void hideAutoLogin(Tab tab) {
        this.mUi.hideAutoLogin(tab);
    }

    private void maybeUpdateFavicon(Tab tab, String str, String str2, Bitmap bitmap) {
        if (DEBUG) {
            Log.i("browser", "Controller.maybeUpdateFavicon()--->tab = " + tab + ", originalUrl = " + str + ", url = " + str2 + ", favicon is null:" + ((Object) null));
            bitmap = null;
        }
        if (bitmap != null && !tab.isPrivateBrowsingEnabled()) {
            Bookmarks.updateFavicon(this.mActivity.getContentResolver(), str, str2, bitmap);
        }
    }

    @Override // com.android.browser.WebViewController
    public void bookmarkedStatusHasChanged(Tab tab) {
        this.mUi.bookmarkedStatusHasChanged(tab);
    }

    protected void pageUp() {
        getCurrentTopWebView().pageUp(false);
    }

    protected void pageDown() {
        getCurrentTopWebView().pageDown(false);
    }

    public void editUrl() {
        if (this.mOptionsMenuOpen) {
            this.mActivity.closeOptionsMenu();
        }
        this.mUi.editUrl(false, true);
    }

    @Override // com.android.browser.WebViewController
    public void showCustomView(Tab tab, View view, int i, WebChromeClient.CustomViewCallback customViewCallback) {
        if (tab.inForeground()) {
            if (this.mUi.isCustomViewShowing()) {
                customViewCallback.onCustomViewHidden();
                return;
            }
            this.mUi.showCustomView(view, i, customViewCallback);
            this.mOldMenuState = this.mMenuState;
            this.mMenuState = -1;
            this.mActivity.invalidateOptionsMenu();
        }
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public void hideCustomView() {
        if (this.mUi.isCustomViewShowing()) {
            this.mUi.onHideCustomView();
            this.mMenuState = this.mOldMenuState;
            this.mOldMenuState = -1;
            this.mActivity.invalidateOptionsMenu();
        }
    }

    @Override // com.android.browser.ActivityController
    public void onActivityResult(int i, int i2, Intent intent) {
        if (getCurrentTopWebView() == null) {
            return;
        }
        switch (i) {
            case 1:
                if (intent != null && i2 == -1) {
                    this.mUi.showWeb(false);
                    if ("android.intent.action.VIEW".equals(intent.getAction())) {
                        loadUrl(getCurrentTab(), intent.getData().toString());
                        break;
                    } else if (intent.hasExtra("open_all")) {
                        String[] stringArrayExtra = intent.getStringArrayExtra("open_all");
                        Tab currentTab = getCurrentTab();
                        for (String str : stringArrayExtra) {
                            currentTab = openTab(str, currentTab, !this.mSettings.openInBackground(), true);
                        }
                        break;
                    } else if (intent.hasExtra("snapshot_id")) {
                        long longExtra = intent.getLongExtra("snapshot_id", -1L);
                        String stringExtra = intent.getStringExtra("snapshot_url");
                        if (stringExtra == null) {
                            stringExtra = this.mSettings.getHomePage();
                        }
                        if (longExtra >= 0) {
                            Tab currentTab2 = getCurrentTab();
                            currentTab2.mSavePageUrl = stringExtra;
                            currentTab2.mSavePageTitle = intent.getStringExtra("snapshot_title");
                            loadUrl(currentTab2, stringExtra);
                            break;
                        }
                    }
                }
                break;
            case 3:
                if (i2 == -1 && intent != null && "privacy_clear_history".equals(intent.getStringExtra("android.intent.extra.TEXT"))) {
                    this.mTabControl.removeParentChildRelationShips();
                    break;
                }
                break;
            case 4:
                if (this.mUploadHandler != null) {
                    this.mUploadHandler.onResult(i2, intent);
                    break;
                }
                break;
            case 6:
                if (i2 == -1 && intent != null) {
                    ArrayList<String> stringArrayListExtra = intent.getStringArrayListExtra("android.speech.extra.RESULTS");
                    if (stringArrayListExtra.size() >= 1) {
                        this.mVoiceResult = stringArrayListExtra.get(0);
                        break;
                    }
                }
                break;
        }
        getCurrentTopWebView().requestFocus();
        this.mBrowserMiscExt = Extensions.getMiscPlugin(this.mActivity);
        this.mBrowserMiscExt.onActivityResult(i, i2, intent, this.mActivity);
    }

    @Override // com.android.browser.UiController
    public void bookmarksOrHistoryPicker(UI.ComboViews comboViews) {
        if (DEBUG) {
            Log.i("browser", "Controller.bookmarksOrHistoryPicker()--->startView = " + comboViews);
        }
        if (this.mTabControl.getCurrentWebView() == null) {
            return;
        }
        if (isInCustomActionMode()) {
            endActionMode();
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("disable_new_window", !this.mTabControl.canCreateNewTab());
        this.mUi.showComboView(comboViews, bundle);
    }

    public void onBackKey() {
        if (!this.mUi.onBackKey()) {
            WebView currentSubWindow = this.mTabControl.getCurrentSubWindow();
            if (currentSubWindow != null) {
                if (currentSubWindow.canGoBack()) {
                    currentSubWindow.goBack();
                    return;
                } else {
                    dismissSubWindow(this.mTabControl.getCurrentTab());
                    return;
                }
            }
            goBackOnePageOrQuit();
        }
    }

    protected boolean onMenuKey() {
        return this.mUi.onMenuKey();
    }

    @Override // com.android.browser.ActivityController
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.mMenuState == -1) {
            return false;
        }
        this.mActivity.getMenuInflater().inflate(R.menu.browser, menu);
        return true;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:81:0x0167, code lost:
        if (r1 == 7) goto L51;
     */
    @Override // com.android.browser.ActivityController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        final WebView webView;
        WebView.HitTestResult hitTestResult;
        boolean z;
        int i;
        if ((view instanceof TitleBar) || !(view instanceof WebView) || (hitTestResult = (webView = (WebView) view).getHitTestResult()) == null) {
            return;
        }
        int type = hitTestResult.getType();
        if (DEBUG) {
            Log.d("browser/Controller", "onCreateContextMenu type is : " + type);
        }
        if (type == 0) {
            Log.w("Controller", "We should not show context menu when nothing is touched");
        } else if (type == 9) {
        } else {
            this.mActivity.getMenuInflater().inflate(R.menu.browsercontext, contextMenu);
            final String extra = hitTestResult.getExtra();
            String str = null;
            if (view instanceof BrowserWebView) {
                str = ((BrowserWebView) webView).getSiteNavHitURL();
            }
            if (DEBUG) {
                Log.d("browser/Controller", "sitenavigation onCreateContextMenu imageAnchorUrlExtra is : " + str);
            }
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService("phone");
            if (telephonyManager != null) {
                z = telephonyManager.isVoiceCapable();
            } else {
                z = false;
            }
            contextMenu.setGroupVisible(R.id.SITE_NAVIGATION_EDIT, false);
            contextMenu.setGroupVisible(R.id.SITE_NAVIGATION_ADD, false);
            if (z) {
                contextMenu.setGroupVisible(R.id.PHONE_MENU, type == 2);
                contextMenu.setGroupVisible(R.id.NO_PHONE_MENU, false);
            } else {
                contextMenu.setGroupVisible(R.id.PHONE_MENU, false);
                contextMenu.setGroupVisible(R.id.NO_PHONE_MENU, type == 2);
            }
            contextMenu.setGroupVisible(R.id.EMAIL_MENU, type == 4);
            contextMenu.setGroupVisible(R.id.GEO_MENU, type == 3);
            contextMenu.setGroupVisible(R.id.IMAGE_MENU, type == 5 || type == 8);
            contextMenu.setGroupVisible(R.id.ANCHOR_MENU, type == 7 || type == 8);
            contextMenu.setGroupVisible(R.id.SELECT_TEXT_MENU, false);
            switch (type) {
                case 2:
                    if (Uri.decode(extra).length() > 128) {
                        contextMenu.setHeaderTitle(Uri.decode(extra).substring(0, 128));
                    } else {
                        contextMenu.setHeaderTitle(Uri.decode(extra));
                    }
                    contextMenu.findItem(R.id.dial_context_menu_id).setIntent(new Intent("android.intent.action.VIEW", Uri.parse("tel:" + extra)));
                    Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
                    intent.putExtra("phone", Uri.decode(extra));
                    intent.setType("vnd.android.cursor.item/contact");
                    if (z) {
                        contextMenu.findItem(R.id.add_contact_context_menu_id).setIntent(intent);
                        contextMenu.findItem(R.id.copy_phone_context_menu_id).setOnMenuItemClickListener(new Copy(extra));
                        break;
                    } else {
                        contextMenu.findItem(R.id.add_contact_no_phone_context_menu_id).setIntent(intent);
                        contextMenu.findItem(R.id.copy_no_phone_context_menu_id).setOnMenuItemClickListener(new Copy(extra));
                        break;
                    }
                case 3:
                    if (extra.length() <= 128) {
                        contextMenu.setHeaderTitle(extra);
                    } else {
                        contextMenu.setHeaderTitle(extra.substring(0, 128));
                    }
                    contextMenu.findItem(R.id.map_context_menu_id).setIntent(new Intent("android.intent.action.VIEW", Uri.parse("geo:0,0?q=" + URLEncoder.encode(extra))));
                    contextMenu.findItem(R.id.copy_geo_context_menu_id).setOnMenuItemClickListener(new Copy(extra));
                    break;
                case 4:
                    if (extra.length() <= 128) {
                        contextMenu.setHeaderTitle(extra);
                    } else {
                        contextMenu.setHeaderTitle(extra.substring(0, 128));
                    }
                    contextMenu.findItem(R.id.email_context_menu_id).setIntent(new Intent("android.intent.action.VIEW", Uri.parse("mailto:" + extra)));
                    contextMenu.findItem(R.id.copy_mail_context_menu_id).setOnMenuItemClickListener(new Copy(extra));
                    break;
                case 5:
                    MenuItem findItem = contextMenu.findItem(R.id.share_link_context_menu_id);
                    findItem.setVisible(type == 5);
                    if (type == 5) {
                        if (extra.length() <= 128) {
                            contextMenu.setHeaderTitle(extra);
                        } else {
                            contextMenu.setHeaderTitle(extra.substring(0, 128));
                        }
                        findItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { // from class: com.android.browser.Controller.16
                            @Override // android.view.MenuItem.OnMenuItemClickListener
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Controller.sharePage(Controller.this.mActivity, null, extra, null, null);
                                return true;
                            }
                        });
                    }
                    contextMenu.findItem(R.id.view_image_context_menu_id).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { // from class: com.android.browser.Controller.17
                        @Override // android.view.MenuItem.OnMenuItemClickListener
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (Controller.isImageViewableUri(Uri.parse(extra))) {
                                Controller.this.openTab(extra, Controller.this.mTabControl.getCurrentTab(), true, true);
                                return false;
                            } else if (Controller.DEBUG) {
                                Log.e("Controller", "Refusing to view image with invalid URI, \"" + extra + "\"");
                                return false;
                            } else {
                                return false;
                            }
                        }
                    });
                    contextMenu.findItem(R.id.download_context_menu_id).setOnMenuItemClickListener(new Download(this.mActivity, extra, webView.isPrivateBrowsingEnabled(), webView.getSettings().getUserAgentString()));
                    this.mWallpaperHandler = new WallpaperHandler(this.mActivity, extra);
                    contextMenu.findItem(R.id.set_wallpaper_context_menu_id).setOnMenuItemClickListener(this.mWallpaperHandler);
                    break;
                case 6:
                default:
                    Log.w("Controller", "We should not get here.");
                    break;
                case 7:
                case 8:
                    if (extra != null && extra.startsWith("rtsp://")) {
                        contextMenu.findItem(R.id.save_link_context_menu_id).setVisible(false);
                    }
                    if (extra.length() <= 128) {
                        contextMenu.setHeaderTitle(extra);
                    } else {
                        contextMenu.setHeaderTitle(extra.substring(0, 128));
                    }
                    boolean canCreateNewTab = this.mTabControl.canCreateNewTab();
                    MenuItem findItem2 = contextMenu.findItem(R.id.open_newtab_context_menu_id);
                    if (getSettings().openInBackground()) {
                        i = R.string.contextmenu_openlink_newwindow_background;
                    } else {
                        i = R.string.contextmenu_openlink_newwindow;
                    }
                    findItem2.setTitle(i);
                    findItem2.setVisible(canCreateNewTab);
                    if (canCreateNewTab) {
                        if (8 == type) {
                            findItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { // from class: com.android.browser.Controller.11
                                @Override // android.view.MenuItem.OnMenuItemClickListener
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    HashMap hashMap = new HashMap();
                                    hashMap.put("webview", webView);
                                    webView.requestFocusNodeHref(Controller.this.mHandler.obtainMessage(102, R.id.open_newtab_context_menu_id, 0, hashMap));
                                    return true;
                                }
                            });
                            break;
                        } else {
                            findItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { // from class: com.android.browser.Controller.12
                                @Override // android.view.MenuItem.OnMenuItemClickListener
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    if (extra != null && extra.startsWith("rtsp://")) {
                                        Intent intent2 = new Intent();
                                        intent2.setAction("android.intent.action.VIEW");
                                        intent2.setData(Uri.parse(extra.replaceAll(" ", "%20")));
                                        intent2.addFlags(268435456);
                                        Controller.this.mActivity.startActivity(intent2);
                                        return true;
                                    } else if (extra == null || !extra.startsWith("wtai://wp/mc;")) {
                                        Controller.this.openTab(extra, Controller.this.mTabControl.getCurrentTab(), !Controller.this.mSettings.openInBackground(), true);
                                        return true;
                                    } else {
                                        String replaceAll = extra.replaceAll(" ", "%20");
                                        Controller.this.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("tel:" + replaceAll.substring("wtai://wp/mc;".length()))));
                                        return true;
                                    }
                                }
                            });
                            break;
                        }
                    }
                    break;
            }
            this.mUi.onContextMenuCreated(contextMenu);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isImageViewableUri(Uri uri) {
        String scheme = uri.getScheme();
        for (String str : IMAGE_VIEWABLE_SCHEMES) {
            if (str.equals(scheme)) {
                return true;
            }
        }
        return false;
    }

    private void updateShareMenuItems(Menu menu, Tab tab) {
        Log.d("browser/Controller", "updateShareMenuItems start");
        if (menu == null) {
            return;
        }
        MenuItem findItem = menu.findItem(R.id.share_page_menu_id);
        if (tab == null) {
            Log.d("browser/Controller", "tab == null");
            findItem.setEnabled(false);
        } else {
            String url = tab.getUrl();
            if (url == null || url.length() == 0) {
                Log.d("browser/Controller", "url == null||url.length() == 0");
                findItem.setEnabled(false);
            } else {
                if (DEBUG) {
                    Log.d("browser/Controller", "url :" + url);
                }
                findItem.setEnabled(true);
            }
        }
        Log.d("browser/Controller", "updateShareMenuItems end");
    }

    @Override // com.android.browser.ActivityController
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getCurrentTab() != null) {
            updateShareMenuItems(menu, getCurrentTab());
        }
        this.mCachedMenu = menu;
        if (this.mMenuState == -1) {
            if (this.mCurrentMenuState != this.mMenuState) {
                menu.setGroupVisible(R.id.MAIN_MENU, false);
                menu.setGroupEnabled(R.id.MAIN_MENU, false);
                menu.setGroupEnabled(R.id.MAIN_SHORTCUT_MENU, false);
            }
        } else {
            if (this.mCurrentMenuState != this.mMenuState) {
                menu.setGroupVisible(R.id.MAIN_MENU, true);
                menu.setGroupEnabled(R.id.MAIN_MENU, true);
                menu.setGroupEnabled(R.id.MAIN_SHORTCUT_MENU, true);
            }
            updateMenuState(getCurrentTab(), menu);
        }
        this.mCurrentMenuState = this.mMenuState;
        return this.mUi.onPrepareOptionsMenu(menu);
    }

    @Override // com.android.browser.UiController
    public void updateMenuState(Tab tab, Menu menu) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        boolean z5;
        if (tab != null) {
            z = tab.canGoBack();
            z2 = tab.canGoForward();
            z3 = this.mSettings.getHomePage().equals(tab.getUrl());
            z4 = this.mSettings.hasDesktopUseragent(tab.getWebView());
            z5 = !tab.isSnapshot();
        } else {
            z = false;
            z2 = false;
            z3 = false;
            z4 = false;
            z5 = false;
        }
        menu.findItem(R.id.back_menu_id).setEnabled(z);
        menu.findItem(R.id.homepage_menu_id).setEnabled(!z3);
        MenuItem findItem = menu.findItem(R.id.forward_menu_id);
        findItem.setEnabled(z2);
        menu.findItem(R.id.stop_menu_id).setEnabled(isInLoad());
        menu.setGroupVisible(R.id.NAV_MENU, z5);
        if (BrowserSettings.getInstance().useFullscreen() || BrowserSettings.getInstance().useQuickControls()) {
            findItem.setVisible(false);
            findItem.setEnabled(z2);
        } else {
            findItem.setVisible(false);
        }
        PackageManager packageManager = this.mActivity.getPackageManager();
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        menu.findItem(R.id.share_page_menu_id).setVisible(packageManager.resolveActivity(intent, 65536) != null);
        boolean enableNavDump = this.mSettings.enableNavDump();
        MenuItem findItem2 = menu.findItem(R.id.dump_nav_menu_id);
        findItem2.setVisible(enableNavDump);
        findItem2.setEnabled(enableNavDump);
        this.mSettings.isDebugEnabled();
        MenuItem findItem3 = menu.findItem(R.id.ua_desktop_menu_id);
        findItem3.setChecked(z4);
        findItem3.setEnabled(true);
        menu.setGroupVisible(R.id.LIVE_MENU, z5);
        menu.setGroupVisible(R.id.SNAPSHOT_MENU, z5 ? false : true);
        menu.setGroupVisible(R.id.COMBO_MENU, false);
        this.mUi.updateMenuState(tab, menu);
    }

    @Override // com.android.browser.ActivityController, com.android.browser.UiController
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int i = 0;
        if (getCurrentTopWebView() == null) {
            return false;
        }
        if (this.mMenuIsDown) {
            this.mMenuIsDown = false;
        }
        if (this.mUi.onOptionsItemSelected(menuItem)) {
            return true;
        }
        switch (menuItem.getItemId()) {
            case R.id.reload_menu_id /* 2131558572 */:
                if (getCurrentTopWebView() != null) {
                    getCurrentTopWebView().reload();
                    break;
                }
                break;
            case R.id.forward_menu_id /* 2131558573 */:
                getCurrentTab().goForward();
                break;
            case R.id.stop_menu_id /* 2131558574 */:
                stopLoading();
                break;
            case R.id.home_menu_id /* 2131558575 */:
            case R.id.homepage_menu_id /* 2131558596 */:
                loadUrl(this.mTabControl.getCurrentTab(), this.mSettings.getHomePage());
                break;
            case R.id.add_bookmark_menu_id /* 2131558576 */:
                bookmarkCurrentPage();
                break;
            case R.id.close_browser_menu_id /* 2131558577 */:
                showCloseSelectionDialog();
                break;
            case R.id.LIVE_MENU /* 2131558578 */:
            case R.id.SNAPSHOT_MENU /* 2131558586 */:
            case R.id.COMBO_MENU /* 2131558588 */:
            case R.id.dump_counters_menu_id /* 2131558593 */:
            case R.id.MAIN_SHORTCUT_MENU /* 2131558594 */:
            default:
                return false;
            case R.id.share_page_menu_id /* 2131558579 */:
                Tab currentTab = this.mTabControl.getCurrentTab();
                if (currentTab != null) {
                    shareCurrentPage(currentTab);
                    break;
                } else {
                    return false;
                }
            case R.id.find_menu_id /* 2131558580 */:
                findOnPage();
                break;
            case R.id.ua_desktop_menu_id /* 2131558581 */:
                toggleUserAgent();
                break;
            case R.id.bookmarks_menu_id /* 2131558582 */:
                bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
                break;
            case R.id.new_tab_menu_id /* 2131558583 */:
                openTab("about:blank", false, true, false);
                break;
            case R.id.page_info_menu_id /* 2131558584 */:
                showPageInfo();
                break;
            case R.id.preferences_menu_id /* 2131558585 */:
                openPreferences();
                break;
            case R.id.snapshot_go_live /* 2131558587 */:
                goLive();
                return true;
            case R.id.close_other_tabs_id /* 2131558589 */:
                closeOtherTabs();
                break;
            case R.id.history_menu_id /* 2131558590 */:
                bookmarksOrHistoryPicker(UI.ComboViews.History);
                break;
            case R.id.snapshots_menu_id /* 2131558591 */:
                bookmarksOrHistoryPicker(UI.ComboViews.Snapshots);
                break;
            case R.id.dump_nav_menu_id /* 2131558592 */:
                getCurrentTopWebView().debugDump();
                break;
            case R.id.view_downloads_menu_id /* 2131558595 */:
                viewDownloads();
                break;
            case R.id.zoom_in_menu_id /* 2131558597 */:
                getCurrentTopWebView().zoomIn();
                break;
            case R.id.zoom_out_menu_id /* 2131558598 */:
                getCurrentTopWebView().zoomOut();
                break;
            case R.id.window_one_menu_id /* 2131558599 */:
            case R.id.window_two_menu_id /* 2131558600 */:
            case R.id.window_three_menu_id /* 2131558601 */:
            case R.id.window_four_menu_id /* 2131558602 */:
            case R.id.window_five_menu_id /* 2131558603 */:
            case R.id.window_six_menu_id /* 2131558604 */:
            case R.id.window_seven_menu_id /* 2131558605 */:
            case R.id.window_eight_menu_id /* 2131558606 */:
                int itemId = menuItem.getItemId();
                while (true) {
                    if (i >= WINDOW_SHORTCUT_ID_ARRAY.length) {
                        break;
                    } else if (WINDOW_SHORTCUT_ID_ARRAY[i] != itemId) {
                        i++;
                    } else {
                        Tab tab = this.mTabControl.getTab(i);
                        if (tab != null && tab != this.mTabControl.getCurrentTab()) {
                            switchToTab(tab);
                            break;
                        }
                    }
                }
                break;
            case R.id.back_menu_id /* 2131558607 */:
                getCurrentTab().goBack();
                break;
            case R.id.goto_menu_id /* 2131558608 */:
                editUrl();
                break;
            case R.id.close_menu_id /* 2131558609 */:
                if (this.mTabControl.getCurrentSubWindow() != null) {
                    dismissSubWindow(this.mTabControl.getCurrentTab());
                    break;
                } else {
                    closeCurrentTab();
                    break;
                }
        }
        return true;
    }

    @Override // com.android.browser.UiController
    public void toggleUserAgent() {
        WebView currentWebView = getCurrentWebView();
        this.mSettings.toggleDesktopUseragent(currentWebView);
        if (this.mSettings.hasDesktopUseragent(currentWebView)) {
            currentWebView.loadUrl(currentWebView.getOriginalUrl());
            return;
        }
        HashMap hashMap = new HashMap();
        hashMap.put(Browser.HEADER, Browser.UAPROF);
        currentWebView.loadUrl(currentWebView.getOriginalUrl(), hashMap);
    }

    @Override // com.android.browser.UiController
    public void findOnPage() {
        getCurrentTopWebView().showFindDialog(null, true);
    }

    @Override // com.android.browser.UiController
    public void openPreferences() {
        Intent intent = new Intent(this.mActivity, BrowserPreferencesPage.class);
        intent.putExtra("currentPage", getCurrentTopWebView().getUrl());
        this.mActivity.startActivityForResult(intent, 3);
    }

    @Override // com.android.browser.UiController
    public void bookmarkCurrentPage() {
        Intent createBookmarkCurrentPageIntent = createBookmarkCurrentPageIntent(false);
        if (createBookmarkCurrentPageIntent != null) {
            this.mActivity.startActivity(createBookmarkCurrentPageIntent);
        }
    }

    private void goLive() {
        Tab currentTab = getCurrentTab();
        currentTab.loadUrl(currentTab.getUrl(), null);
    }

    private void showCloseSelectionDialog() {
        new AlertDialog.Builder(this.mActivity).setTitle(R.string.option).setItems(new CharSequence[]{this.mActivity.getString(R.string.minimize), this.mActivity.getString(R.string.quit)}, new DialogInterface.OnClickListener() { // from class: com.android.browser.Controller.20
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    Controller.this.mActivity.moveTaskToBack(true);
                } else if (i == 1) {
                    if (((ActivityManager) Controller.this.mActivity.getSystemService("activity")).isInLockTaskMode()) {
                        Controller.this.mActivity.showLockTaskEscapeMessage();
                        return;
                    }
                    Controller.this.mNotificationManager.cancelAll();
                    Controller.this.mUi.hideIME();
                    Controller.this.onDestroy();
                    Controller.this.mActivity.finish();
                    File file = new File(Controller.this.getActivity().getApplicationContext().getCacheDir(), "browser_state.parcel");
                    if (file.exists()) {
                        file.delete();
                    }
                    Intent intent = new Intent("mediatek.intent.action.stk.BROWSER_TERMINATION");
                    intent.setComponent(ComponentName.unflattenFromString("com.android.stk/.EventReceiver"));
                    Controller.this.mActivity.sendBroadcast(intent);
                    Process.killProcess(Process.myPid());
                }
            }
        }).show();
    }

    @Override // com.android.browser.UiController
    public void showPageInfo() {
        this.mPageDialogsHandler.showPageInfo(this.mTabControl.getCurrentTab(), false, null);
    }

    @Override // com.android.browser.ActivityController
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (menuItem.getGroupId() == R.id.CONTEXT_MENU) {
            return false;
        }
        int itemId = menuItem.getItemId();
        if (itemId != R.id.open_context_menu_id) {
            switch (itemId) {
                case R.id.save_link_context_menu_id /* 2131558625 */:
                case R.id.copy_link_context_menu_id /* 2131558626 */:
                case R.id.save_link_tobookmark_context_menu_id /* 2131558627 */:
                    break;
                default:
                    return onOptionsItemSelected(menuItem);
            }
        }
        WebView currentTopWebView = getCurrentTopWebView();
        if (currentTopWebView == null) {
            return false;
        }
        HashMap hashMap = new HashMap();
        hashMap.put("webview", currentTopWebView);
        currentTopWebView.requestFocusNodeHref(this.mHandler.obtainMessage(102, itemId, 0, hashMap));
        return true;
    }

    @Override // com.android.browser.ActivityController
    public boolean onMenuOpened(int i, Menu menu) {
        if (this.mOptionsMenuOpen) {
            if (this.mConfigChanged) {
                this.mConfigChanged = false;
            } else if (!this.mExtendedMenuOpen) {
                this.mExtendedMenuOpen = true;
                this.mUi.onExtendedMenuOpened();
            } else {
                this.mExtendedMenuOpen = false;
                this.mUi.onExtendedMenuClosed(isInLoad());
            }
        } else {
            this.mOptionsMenuOpen = true;
            this.mConfigChanged = false;
            this.mExtendedMenuOpen = false;
            this.mUi.onOptionsMenuOpened();
        }
        return true;
    }

    @Override // com.android.browser.ActivityController
    public void onOptionsMenuClosed(Menu menu) {
        this.mOptionsMenuOpen = false;
        this.mUi.onOptionsMenuClosed(isInLoad());
    }

    @Override // com.android.browser.ActivityController
    public void onContextMenuClosed(Menu menu) {
        this.mUi.onContextMenuClosed(menu, isInLoad());
    }

    @Override // com.android.browser.UiController
    public WebView getCurrentTopWebView() {
        return this.mTabControl.getCurrentTopWebView();
    }

    @Override // com.android.browser.UiController
    public WebView getCurrentWebView() {
        return this.mTabControl.getCurrentWebView();
    }

    void viewDownloads() {
        this.mActivity.startActivity(new Intent("android.intent.action.VIEW_DOWNLOADS"));
    }

    @Override // com.android.browser.ActivityController
    public void onActionModeStarted(ActionMode actionMode) {
        this.mUi.onActionModeStarted(actionMode);
        this.mActionMode = actionMode;
    }

    @Override // com.android.browser.UiController
    public boolean isInCustomActionMode() {
        return this.mActionMode != null;
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public void endActionMode() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
    }

    @Override // com.android.browser.ActivityController
    public void onActionModeFinished(ActionMode actionMode) {
        if (isInCustomActionMode()) {
            this.mUi.onActionModeFinished(isInLoad());
            this.mActionMode = null;
        }
    }

    boolean isInLoad() {
        Tab currentTab = getCurrentTab();
        return currentTab != null && currentTab.inPageLoad();
    }

    private Intent createBookmarkPageIntent(boolean z, String str, String str2) {
        WebView currentTopWebView = getCurrentTopWebView();
        if (currentTopWebView == null) {
            return null;
        }
        Intent intent = new Intent(this.mActivity, AddBookmarkPage.class);
        if (str != null) {
            intent.putExtra("url", str);
        } else {
            intent.putExtra("url", currentTopWebView.getUrl());
        }
        if (str2 != null) {
            intent.putExtra("title", str2);
        } else {
            intent.putExtra("title", currentTopWebView.getTitle());
        }
        String touchIconUrl = currentTopWebView.getTouchIconUrl();
        if (touchIconUrl != null) {
            intent.putExtra("touch_icon_url", touchIconUrl);
            WebSettings settings = currentTopWebView.getSettings();
            if (settings != null) {
                intent.putExtra("user_agent", settings.getUserAgentString());
            }
        }
        intent.putExtra("thumbnail", createScreenshot(currentTopWebView, getDesiredThumbnailWidth(this.mActivity), getDesiredThumbnailHeight(this.mActivity)));
        Bitmap favicon = currentTopWebView.getFavicon();
        if (favicon != null && favicon.getWidth() > 60) {
            favicon = Bitmap.createScaledBitmap(favicon, 60, 60, true);
        }
        intent.putExtra("favicon", favicon);
        if (z) {
            intent.putExtra("check_for_dupe", true);
        }
        intent.putExtra("gravity", 53);
        return intent;
    }

    @Override // com.android.browser.UiController
    public Intent createBookmarkCurrentPageIntent(boolean z) {
        return createBookmarkPageIntent(z, null, null);
    }

    public Intent createBookmarkLinkIntent(String str) {
        return createBookmarkPageIntent(false, str, "");
    }

    @Override // com.android.browser.WebViewController
    public void showFileChooser(ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        this.mUploadHandler = new UploadHandler(this);
        this.mUploadHandler.openFileChooser(valueCallback, fileChooserParams);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getDesiredThumbnailWidth(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.bookmarkThumbnailWidth);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getDesiredThumbnailHeight(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.bookmarkThumbnailHeight);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Bitmap createScreenshot(WebView webView, int i, int i2) {
        if (DEBUG) {
            Log.i("browser", "Controller.createScreenshot()--->webView = " + webView + ", width = " + i + ", height = " + i2);
        }
        if (webView == null || webView.getContentHeight() == 0 || webView.getContentWidth() == 0) {
            return null;
        }
        int i3 = i * 2;
        int i4 = i2 * 2;
        if (sThumbnailBitmap == null || sThumbnailBitmap.getWidth() != i3 || sThumbnailBitmap.getHeight() != i4) {
            if (sThumbnailBitmap != null) {
                sThumbnailBitmap.recycle();
                sThumbnailBitmap = null;
            }
            sThumbnailBitmap = Bitmap.createBitmap(i3, i4, Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(sThumbnailBitmap);
        float scale = i3 / (webView.getScale() * webView.getContentWidth());
        boolean z = webView instanceof BrowserWebView;
        if (z) {
            canvas.translate(0.0f, (-((BrowserWebView) webView).getTitleHeight()) * scale);
        }
        int scrollX = webView.getScrollX();
        int scrollY = webView.getScrollY() + webView.getVisibleTitleHeight();
        canvas.translate(-scrollX, -scrollY);
        if (DEBUG) {
            Log.d("browser", "createScreenShot()--->left = " + scrollX + ", top = " + scrollY + ", overviewScale = " + scale);
        }
        canvas.scale(scale, scale, scrollX, scrollY);
        if (z) {
            ((BrowserWebView) webView).drawContent(canvas);
        } else {
            webView.draw(canvas);
        }
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(sThumbnailBitmap, i, i2, true);
        canvas.setBitmap(null);
        return createScaledBitmap;
    }

    private boolean needToIgnore(String str, String str2) {
        if (str.equalsIgnoreCase("http://www.wo.com.cn/") || str.equalsIgnoreCase("http://www.wo.com.cn") || str2.equalsIgnoreCase("http://m.wo.cn/") || str2.equalsIgnoreCase("http://m.wo.cn")) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r8v5, types: [com.android.browser.Controller$21] */
    public void updateScreenshot(Tab tab) {
        final Bitmap createScreenshot;
        String host;
        if (DEBUG) {
            Log.i("browser", "Controller.updateScreenshot()--->tab is " + tab);
        }
        WebView webView = tab.getWebView();
        if (webView == null) {
            return;
        }
        final String url = tab.getUrl();
        String originalUrl = webView.getOriginalUrl();
        final String str = originalUrl == null ? url : originalUrl;
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (DEBUG) {
            Log.d("Controller", " originalUrl: " + str + " url: " + url);
        }
        if ((!Patterns.WEB_URL.matcher(url).matches() && !tab.isBookmarkedSite() && !isDefaultBookmarks(str)) || needToIgnore(str, url)) {
            return;
        }
        if ((url != null && Patterns.WEB_URL.matcher(url).matches() && ((host = new WebAddress(url).getHost()) == null || host.length() == 0)) || (createScreenshot = createScreenshot(webView, getDesiredThumbnailWidth(this.mActivity), getDesiredThumbnailHeight(this.mActivity))) == null) {
            return;
        }
        final ContentResolver contentResolver = this.mActivity.getContentResolver();
        new AsyncTask<Void, Void, Void>() { // from class: com.android.browser.Controller.21
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Code restructure failed: missing block: B:15:0x004c, code lost:
                if (r0 != null) goto L8;
             */
            /* JADX WARN: Code restructure failed: missing block: B:22:0x005d, code lost:
                if (r0 == null) goto L6;
             */
            /* JADX WARN: Code restructure failed: missing block: B:30:0x0069, code lost:
                if (r0 == null) goto L6;
             */
            /* JADX WARN: Code restructure failed: missing block: B:31:0x006b, code lost:
                r0.close();
             */
            /* JADX WARN: Code restructure failed: missing block: B:32:0x006e, code lost:
                return null;
             */
            /* JADX WARN: Removed duplicated region for block: B:26:0x0063  */
            @Override // android.os.AsyncTask
            /*
                Code decompiled incorrectly, please refer to instructions dump.
            */
            public Void doInBackground(Void... voidArr) {
                Throwable th;
                Cursor cursor;
                Cursor cursor2 = null;
                try {
                    try {
                        cursor = Bookmarks.queryCombinedForUrl(contentResolver, str, url);
                        if (cursor != null) {
                            try {
                                if (cursor.moveToFirst()) {
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    createScreenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("thumbnail", byteArrayOutputStream.toByteArray());
                                    do {
                                        contentValues.put("url_key", cursor.getString(0));
                                        contentResolver.update(BrowserContract.Images.CONTENT_URI, contentValues, null, null);
                                    } while (cursor.moveToNext());
                                }
                            } catch (SQLiteException e) {
                                e = e;
                                Log.w("Controller", "Error when running updateScreenshot ", e);
                            } catch (IllegalStateException e2) {
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (0 != 0) {
                            cursor2.close();
                        }
                        throw th;
                    }
                } catch (SQLiteException e3) {
                    e = e3;
                    cursor = null;
                } catch (IllegalStateException e4) {
                    cursor = null;
                } catch (Throwable th3) {
                    th = th3;
                    if (0 != 0) {
                    }
                    throw th;
                }
            }
        }.execute(new Void[0]);
    }

    /* loaded from: classes.dex */
    private class Copy implements MenuItem.OnMenuItemClickListener {
        private CharSequence mText;

        @Override // android.view.MenuItem.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem menuItem) {
            Controller.this.copy(this.mText);
            return true;
        }

        public Copy(CharSequence charSequence) {
            this.mText = charSequence;
        }
    }

    /* loaded from: classes.dex */
    private static class Download implements MenuItem.OnMenuItemClickListener {
        private Activity mActivity;
        private boolean mPrivateBrowsing;
        private String mText;
        private String mUserAgent;

        @Override // android.view.MenuItem.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (DataUri.isDataUri(this.mText)) {
                saveDataUri();
                return true;
            }
            DownloadHandler.onDownloadStartNoStream(this.mActivity, this.mText, this.mUserAgent, null, null, null, this.mPrivateBrowsing, 0L);
            return true;
        }

        public Download(Activity activity, String str, boolean z, String str2) {
            this.mActivity = activity;
            this.mText = str;
            this.mPrivateBrowsing = z;
            this.mUserAgent = str2;
        }

        private void saveDataUri() {
            FileOutputStream fileOutputStream;
            Throwable th;
            DataUri dataUri;
            File target;
            FileOutputStream fileOutputStream2 = null;
            try {
                try {
                    try {
                        dataUri = new DataUri(this.mText);
                        target = getTarget(dataUri);
                        fileOutputStream = new FileOutputStream(target);
                    } catch (IOException e) {
                    }
                } catch (Throwable th2) {
                    fileOutputStream = fileOutputStream2;
                    th = th2;
                }
                try {
                    fileOutputStream.write(dataUri.getData());
                    ((DownloadManager) this.mActivity.getSystemService("download")).addCompletedDownload(target.getName(), this.mActivity.getTitle().toString(), false, dataUri.getMimeType(), target.getAbsolutePath(), dataUri.getData().length, true);
                    fileOutputStream.close();
                } catch (IOException e2) {
                    fileOutputStream2 = fileOutputStream;
                    Log.e("Controller", "Could not save data URL");
                    if (fileOutputStream2 != null) {
                        fileOutputStream2.close();
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e4) {
            }
        }

        private File getTarget(DataUri dataUri) throws IOException {
            File externalFilesDir = this.mActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            String format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-", Locale.US).format(new Date());
            String mimeType = dataUri.getMimeType();
            String extensionFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extensionFromMimeType == null) {
                Log.w("Controller", "Unknown mime type in data URI" + mimeType);
                extensionFromMimeType = "dat";
            }
            return File.createTempFile(format, "." + extensionFromMimeType, externalFilesDir);
        }
    }

    protected void addTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "Controller.addTab()--->tab : " + tab);
        }
        this.mUi.addTab(tab);
    }

    protected void removeTab(Tab tab) {
        this.mUi.removeTab(tab);
        this.mTabControl.removeTab(tab);
        this.mCrashRecoveryHandler.backupState();
    }

    @Override // com.android.browser.UiController
    public void setActiveTab(Tab tab) {
        if (tab != null) {
            this.mTabControl.setCurrentTab(tab);
            this.mUi.setActiveTab(tab);
            WebView webView = tab.getWebView();
            if (DEBUG) {
                Log.d("browser", "Controller.setActiveTab()---> webview : " + webView);
            }
            if (webView == null) {
                return;
            }
            if (this.mSettings.isDesktopUserAgent(webView)) {
                this.mSettings.changeUserAgent(webView, false);
            }
        }
        if (DEBUG) {
            Log.d("browser", "Controller.setActiveTab()--->tab : " + tab);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void closeEmptyTab() {
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab != null && currentTab.getWebView().copyBackForwardList().getSize() == 0) {
            closeCurrentTab();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void reuseTab(Tab tab, IntentHandler.UrlData urlData) {
        if (DEBUG) {
            Log.i("browser", "Controller.reuseTab()--->tab : " + tab + ", urlData : " + urlData);
        }
        dismissSubWindow(tab);
        this.mUi.detachTab(tab);
        this.mTabControl.recreateWebView(tab);
        this.mUi.attachTab(tab);
        if (this.mTabControl.getCurrentTab() != tab) {
            switchToTab(tab);
            loadUrlDataIn(tab, urlData);
            return;
        }
        setActiveTab(tab);
        loadUrlDataIn(tab, urlData);
    }

    @Override // com.android.browser.WebViewController
    public void dismissSubWindow(Tab tab) {
        removeSubWindow(tab);
        tab.dismissSubWindow();
        WebView currentTopWebView = getCurrentTopWebView();
        if (currentTopWebView != null) {
            currentTopWebView.requestFocus();
        }
    }

    @Override // com.android.browser.UiController
    public void removeSubWindow(Tab tab) {
        if (tab.getSubWebView() != null) {
            WebView webView = tab.getWebView();
            if (webView != null) {
                webView.requestFocus();
            }
            this.mUi.removeSubWindow(tab.getSubViewContainer());
        }
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public void attachSubWindow(Tab tab) {
        if (tab.getSubWebView() != null) {
            this.mUi.attachSubWindow(tab.getSubViewContainer());
            getCurrentTopWebView().requestFocus();
        }
    }

    private Tab showPreloadedTab(IntentHandler.UrlData urlData) {
        Tab leastUsedTab;
        if (DEBUG) {
            Log.i("browser", "Controller.showPreloadedTab()--->urlData : " + urlData);
        }
        if (urlData.isPreloaded()) {
            PreloadedTabControl preloadedTab = urlData.getPreloadedTab();
            String searchBoxQueryToSubmit = urlData.getSearchBoxQueryToSubmit();
            if (searchBoxQueryToSubmit != null && !preloadedTab.searchBoxSubmit(searchBoxQueryToSubmit, urlData.mUrl, urlData.mHeaders)) {
                preloadedTab.destroy();
                return null;
            }
            if (!this.mTabControl.canCreateNewTab() && (leastUsedTab = this.mTabControl.getLeastUsedTab(getCurrentTab())) != null) {
                closeTab(leastUsedTab);
            }
            Tab tab = preloadedTab.getTab();
            tab.refreshIdAfterPreload();
            this.mTabControl.addPreloadedTab(tab);
            addTab(tab);
            setActiveTab(tab);
            return tab;
        }
        return null;
    }

    public Tab openTab(IntentHandler.UrlData urlData) {
        Tab showPreloadedTab = showPreloadedTab(urlData);
        if (showPreloadedTab == null && (showPreloadedTab = createNewTab(false, true, true)) != null && !urlData.isEmpty()) {
            loadUrlDataIn(showPreloadedTab, urlData);
        }
        return showPreloadedTab;
    }

    @Override // com.android.browser.UiController
    public Tab openTabToHomePage() {
        if (DEBUG) {
            Log.d("browser", "Controller.openTabToHomePage()--->");
        }
        return openTab(this.mSettings.getHomePage(), false, true, false);
    }

    @Override // com.android.browser.UiController
    public Tab openIncognitoTab() {
        return openTab("browser:incognito", true, true, false);
    }

    @Override // com.android.browser.UiController
    public Tab openTab(String str, boolean z, boolean z2, boolean z3) {
        return openTab(str, z, z2, z3, null);
    }

    @Override // com.android.browser.WebViewController
    public Tab openTab(String str, Tab tab, boolean z, boolean z2) {
        return openTab(str, tab != null && tab.isPrivateBrowsingEnabled(), z, z2, tab);
    }

    public Tab openTab(String str, boolean z, boolean z2, boolean z3, Tab tab) {
        if (DEBUG) {
            Log.d("browser", "Controller.openTab()--->url = " + str + ", incognito = " + z + ", setActive = " + z2 + ", useCurrent = " + z3 + ", tab parent is " + tab);
        }
        Tab createNewTab = createNewTab(z, z2, z3);
        if (createNewTab != null) {
            if (tab != null && tab != createNewTab) {
                tab.addChildTab(createNewTab);
            }
            if (str != null) {
                loadUrl(createNewTab, str);
            }
        }
        return createNewTab;
    }

    private Tab createNewTab(boolean z, boolean z2, boolean z3) {
        Tab tab = null;
        if (this.mTabControl.canCreateNewTab()) {
            tab = this.mTabControl.createNewTab(z);
            addTab(tab);
            if (z2) {
                setActiveTab(tab);
            }
        } else if (z3) {
            Tab currentTab = this.mTabControl.getCurrentTab();
            reuseTab(currentTab, null);
            tab = currentTab;
        } else {
            this.mUi.showMaxTabsWarning();
        }
        if (DEBUG) {
            Log.d("browser", "Controller.createNewTab()--->tab is " + tab);
        }
        return tab;
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public boolean switchToTab(Tab tab) {
        if (DEBUG) {
            Log.i("browser", "Controller.switchToTab()--->tab is " + tab);
        }
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (tab == null || tab == currentTab) {
            return false;
        }
        setActiveTab(tab);
        return true;
    }

    @Override // com.android.browser.UiController
    public void closeCurrentTab() {
        closeCurrentTab(false);
    }

    protected boolean closeCurrentTab(boolean z) {
        if (DEBUG) {
            Log.i("browser", "Controller.closeCurrentTab()--->andQuit : " + z);
        }
        if (this.mTabControl.getTabCount() == 1) {
            this.mCrashRecoveryHandler.clearState();
            if (!z) {
                this.mTabControl.removeTab(getCurrentTab());
            } else {
                this.mDelayRemoveLastTab = true;
            }
            this.mActivity.finish();
            return true;
        }
        Tab currentTab = this.mTabControl.getCurrentTab();
        int currentPosition = this.mTabControl.getCurrentPosition();
        Tab parent = currentTab.getParent();
        if (parent == null && (parent = this.mTabControl.getTab(currentPosition + 1)) == null) {
            parent = this.mTabControl.getTab(currentPosition - 1);
        }
        if (z) {
            this.mTabControl.setCurrentTab(parent);
            this.mUi.closeTableDelay(currentTab);
            return false;
        } else if (switchToTab(parent)) {
            closeTab(currentTab);
            return false;
        } else {
            return false;
        }
    }

    @Override // com.android.browser.UiController, com.android.browser.WebViewController
    public void closeTab(Tab tab) {
        if (DEBUG) {
            Log.i("browser", "Controller.closeTab()--->tab is " + tab);
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(this.mTabControl.getTabPosition(tab)));
        if (tab == this.mTabControl.getCurrentTab()) {
            closeCurrentTab();
        } else {
            removeTab(tab);
        }
        new CheckMemoryTask(this.mHandler).execute(Integer.valueOf(getTabControl().getVisibleWebviewNums()), arrayList, false, null, null, true);
    }

    public void closeOtherTabs() {
        if (DEBUG) {
            Log.i("browser", "Controller.closeOtherTabs()--->");
        }
        ArrayList arrayList = new ArrayList();
        for (int tabCount = this.mTabControl.getTabCount() - 1; tabCount >= 0; tabCount--) {
            Tab tab = this.mTabControl.getTab(tabCount);
            if (tab != this.mTabControl.getCurrentTab()) {
                arrayList.add(Integer.valueOf(this.mTabControl.getTabPosition(tab)));
                removeTab(tab);
            }
        }
        new CheckMemoryTask(this.mHandler).execute(Integer.valueOf(getTabControl().getVisibleWebviewNums()), arrayList, false, null, null, true);
    }

    protected void loadUrlFromContext(String str) {
        if (DEBUG) {
            Log.i("browser", "Controller.loadUrlFromContext()--->url : " + str);
        }
        Tab currentTab = getCurrentTab();
        WebView webView = currentTab != null ? currentTab.getWebView() : null;
        if (str != null && str.length() != 0 && currentTab != null && webView != null) {
            String smartUrlFilter = UrlUtils.smartUrlFilter(str);
            if (!((BrowserWebView) webView).getWebViewClient().shouldOverrideUrlLoading(webView, smartUrlFilter)) {
                loadUrl(currentTab, smartUrlFilter);
            }
        }
    }

    @Override // com.android.browser.UiController
    public void loadUrl(Tab tab, String str) {
        loadUrl(tab, str, null);
    }

    protected void loadUrl(Tab tab, String str, Map<String, String> map) {
        if (DEBUG) {
            Log.d("browser", "Controller.loadUrl()--->tab : " + tab + ", url = " + str + ", headers : " + map);
        }
        if (tab != null) {
            dismissSubWindow(tab);
            tab.loadUrl(str, map);
            this.mUi.onProgressChanged(tab);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void loadUrlDataIn(Tab tab, IntentHandler.UrlData urlData) {
        if (DEBUG) {
            Log.i("browser", "Controller.loadUrlDataIn()--->tab : " + tab + ", Url Data : " + urlData);
        }
        if (urlData != null && !urlData.isPreloaded()) {
            if (tab != null && urlData.mDisableUrlOverride) {
                tab.disableUrlOverridingForLoad();
            }
            loadUrl(tab, urlData.mUrl, urlData.mHeaders);
        }
    }

    @Override // com.android.browser.WebViewController
    public void onUserCanceledSsl(Tab tab) {
        if (tab.canGoBack()) {
            tab.goBack();
        } else {
            tab.loadUrl(this.mSettings.getHomePage(), null);
        }
    }

    void goBackOnePageOrQuit() {
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab == null) {
            this.mActivity.moveTaskToBack(true);
            return;
        }
        if (currentTab.canGoBack()) {
            currentTab.goBack();
        } else {
            Tab parent = currentTab.getParent();
            if (parent != null) {
                switchToTab(parent);
                closeTab(currentTab);
            } else {
                if (currentTab.getAppId() != null || currentTab.closeOnBack()) {
                    closeCurrentTab(true);
                }
                boolean moveTaskToBack = this.mActivity.moveTaskToBack(true);
                Log.d("Controller", "moveTaskToBack: " + moveTaskToBack);
                if (moveTaskToBack) {
                    onPause();
                }
            }
        }
        if (DEBUG) {
            Log.i("browser", "Controller.goBackOnePageOrQuit()--->current tab is " + currentTab);
        }
    }

    private Tab getNextTab() {
        int currentPosition = this.mTabControl.getCurrentPosition() + 1;
        if (currentPosition >= this.mTabControl.getTabCount()) {
            currentPosition = 0;
        }
        return this.mTabControl.getTab(currentPosition);
    }

    private Tab getPrevTab() {
        int currentPosition = this.mTabControl.getCurrentPosition() - 1;
        if (currentPosition < 0) {
            currentPosition = this.mTabControl.getTabCount() - 1;
        }
        return this.mTabControl.getTab(currentPosition);
    }

    boolean isMenuOrCtrlKey(int i) {
        return 82 == i || 113 == i || 114 == i;
    }

    @Override // com.android.browser.ActivityController
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        boolean hasNoModifiers = keyEvent.hasNoModifiers();
        if (!hasNoModifiers && isMenuOrCtrlKey(i)) {
            this.mMenuIsDown = true;
            return false;
        }
        WebView currentTopWebView = getCurrentTopWebView();
        Tab currentTab = getCurrentTab();
        if (currentTopWebView == null || currentTab == null) {
            return false;
        }
        boolean hasModifiers = keyEvent.hasModifiers(4096);
        boolean hasModifiers2 = keyEvent.hasModifiers(1);
        switch (i) {
            case 4:
                if (hasNoModifiers) {
                    keyEvent.startTracking();
                    return true;
                }
                break;
            case 21:
                if (hasModifiers) {
                    currentTab.goBack();
                    return true;
                }
                break;
            case 22:
                if (hasModifiers) {
                    currentTab.goForward();
                    return true;
                }
                break;
            case 48:
                if (keyEvent.isCtrlPressed()) {
                    if (!keyEvent.isShiftPressed()) {
                        openTab("about:blank", false, true, false);
                    } else {
                        openIncognitoTab();
                    }
                    return true;
                }
                break;
            case 61:
                if (keyEvent.isCtrlPressed()) {
                    if (keyEvent.isShiftPressed()) {
                        switchToTab(getPrevTab());
                    } else {
                        switchToTab(getNextTab());
                    }
                    return true;
                }
                break;
            case 62:
                if (hasModifiers2) {
                    pageUp();
                } else if (hasNoModifiers) {
                    pageDown();
                }
                return true;
            case 82:
                this.mActivity.invalidateOptionsMenu();
                break;
            case 84:
                if (!this.mUi.isWebShowing()) {
                    return true;
                }
                break;
            case 125:
                if (hasNoModifiers) {
                    currentTab.goForward();
                    return true;
                }
                break;
        }
        return this.mUi.dispatchKey(i, keyEvent);
    }

    @Override // com.android.browser.ActivityController
    public boolean onKeyLongPress(int i, KeyEvent keyEvent) {
        if (i == 4 && this.mUi.isWebShowing()) {
            bookmarksOrHistoryPicker(UI.ComboViews.History);
            return true;
        }
        return false;
    }

    @Override // com.android.browser.ActivityController
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (isMenuOrCtrlKey(i)) {
            this.mMenuIsDown = false;
            if (82 == i && keyEvent.isTracking() && !keyEvent.isCanceled()) {
                return onMenuKey();
            }
        }
        if (keyEvent.hasNoModifiers() && i == 4 && keyEvent.isTracking() && !keyEvent.isCanceled()) {
            onBackKey();
            return true;
        }
        return false;
    }

    public boolean isMenuDown() {
        return this.mMenuIsDown;
    }

    @Override // com.android.browser.ActivityController
    public boolean onSearchRequested() {
        this.mUi.editUrl(false, true);
        return true;
    }

    @Override // com.android.browser.WebViewController
    public boolean shouldCaptureThumbnails() {
        return this.mUi.shouldCaptureThumbnails();
    }

    @Override // com.android.browser.UiController
    public boolean supportsVoice() {
        return this.mActivity.getPackageManager().queryIntentActivities(new Intent("android.speech.action.RECOGNIZE_SPEECH"), 0).size() != 0;
    }

    @Override // com.android.browser.UiController
    public void setBlockEvents(boolean z) {
        this.mBlockEvents = z;
    }

    @Override // com.android.browser.ActivityController
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return this.mBlockEvents;
    }

    @Override // com.android.browser.ActivityController
    public boolean dispatchKeyShortcutEvent(KeyEvent keyEvent) {
        return this.mBlockEvents;
    }

    @Override // com.android.browser.ActivityController
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return this.mBlockEvents;
    }

    @Override // com.android.browser.ActivityController
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        return this.mBlockEvents;
    }

    @Override // com.android.browser.ActivityController
    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        return this.mBlockEvents;
    }

    @Override // com.android.browser.WebViewController
    public void onShowPopupWindowAttempt(Tab tab, boolean z, Message message) {
        this.mPageDialogsHandler.showPopupWindowAttempt(tab, z, message);
    }

    long getSavePageDirSize(File file) throws IOException {
        long length;
        File[] listFiles = file.listFiles();
        long j = 0;
        if (listFiles == null) {
            return 0L;
        }
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].isDirectory()) {
                length = getSavePageDirSize(listFiles[i]);
            } else {
                length = listFiles[i].length();
            }
            j += length;
        }
        return j;
    }

    /* loaded from: classes.dex */
    class UpdateSavePageDBHandler extends Handler {
        ContentResolver mCr;

        public UpdateSavePageDBHandler(Looper looper) {
            super(looper);
            this.mCr = Controller.this.mActivity.getContentResolver();
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            long j;
            String[] strArr = {"title"};
            String str = null;
            switch (message.what) {
                case 1984:
                    ContentValues contentValues = (ContentValues) message.obj;
                    long parseId = ContentUris.parseId(this.mCr.insert(SnapshotProvider.Snapshots.CONTENT_URI, contentValues));
                    int intValue = contentValues.getAsInteger("job_id").intValue();
                    Log.d("browser/SavePage", "ADD_SAVE_PAGE: " + intValue);
                    for (Tab tab : Controller.this.getTabControl().getTabs()) {
                        if (tab.containsDatabaseItemId(intValue)) {
                            tab.addDatabaseItemId(intValue, parseId);
                            return;
                        }
                    }
                    return;
                case 1985:
                    Cursor query = this.mCr.query(SnapshotProvider.Snapshots.CONTENT_URI, strArr, "job_id = ? and is_done = ?", new String[]{String.valueOf(message.arg2), "0"}, null);
                    while (query.moveToNext()) {
                        str = query.getString(0);
                    }
                    query.close();
                    Controller.this.mBuilder.setContentTitle(str).setProgress(100, message.arg1, false).setContentInfo(message.arg1 + "%").setOngoing(true).setSmallIcon(R.drawable.ic_save_page_notification);
                    Controller.this.mNotificationManager.notify(message.arg2, Controller.this.mBuilder.build());
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put("progress", Integer.valueOf(message.arg1));
                    String[] strArr2 = {String.valueOf(message.arg2), "100"};
                    Log.d("browser/SavePage", "UPDATE_SAVE_PAGE: " + message.arg2);
                    this.mCr.update(SnapshotProvider.Snapshots.CONTENT_URI, contentValues2, "job_id = ? and progress < ?", strArr2);
                    return;
                case 1986:
                    Notification.Builder builder = new Notification.Builder(Controller.this.mActivity);
                    ContentValues contentValues3 = new ContentValues();
                    contentValues3.put("progress", (Integer) 100);
                    contentValues3.put("is_done", (Integer) 1);
                    String[] strArr3 = {String.valueOf(message.arg1), "0"};
                    Cursor query2 = this.mCr.query(SnapshotProvider.Snapshots.CONTENT_URI, new String[]{"title", "viewstate_path"}, "job_id = ? and is_done = ?", strArr3, null);
                    String str2 = null;
                    long j2 = 0;
                    while (query2.moveToNext()) {
                        String string = query2.getString(1);
                        String string2 = query2.getString(0);
                        if (TextUtils.isEmpty(string)) {
                            str2 = string2;
                        } else {
                            File file = new File(string.substring(0, string.lastIndexOf(File.separator)));
                            try {
                                j = Controller.this.getSavePageDirSize(file);
                            } catch (IOException e) {
                                j = 0;
                            }
                            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                            intent.setData(Uri.fromFile(file));
                            Controller.this.mActivity.sendBroadcast(intent);
                            str2 = string2;
                            j2 = j;
                        }
                    }
                    query2.close();
                    contentValues3.put("viewstate_size", Long.valueOf(j2));
                    this.mCr.update(SnapshotProvider.Snapshots.CONTENT_URI, contentValues3, "job_id = ? and is_done = ?", strArr3);
                    builder.setContentIntent(Controller.this.createSavePagePendingIntent()).setAutoCancel(true).setOngoing(false).setContentTitle(str2).setSmallIcon(R.drawable.ic_save_page_notification).setContentText(Controller.this.mActivity.getText(R.string.saved_page_complete));
                    Log.d("browser/SavePage", "FINISH_SAVE_PAGE: " + message.arg1);
                    Controller.this.mNotificationManager.notify(message.arg1, builder.build());
                    return;
                case 1987:
                    Notification.Builder builder2 = new Notification.Builder(Controller.this.mActivity);
                    String[] strArr4 = {String.valueOf(message.arg1), "0"};
                    Cursor query3 = this.mCr.query(SnapshotProvider.Snapshots.CONTENT_URI, strArr, "job_id = ? and is_done = ?", strArr4, null);
                    String str3 = null;
                    while (query3.moveToNext()) {
                        str3 = query3.getString(0);
                        if (Controller.DEBUG) {
                            Log.d("browser/SavePage", "fail title is: " + str3);
                        }
                    }
                    if (str3 != null) {
                        builder2.setContentTitle(((Object) Controller.this.mActivity.getText(R.string.saved_page_fail)) + str3).setContentIntent(null).setAutoCancel(true).setOngoing(false).setSmallIcon(R.drawable.ic_save_page_notification_fail);
                    } else {
                        builder2.setContentTitle(Controller.this.mActivity.getText(R.string.saved_page_fail)).setContentIntent(null).setAutoCancel(true).setOngoing(false).setSmallIcon(R.drawable.ic_save_page_notification_fail);
                    }
                    Log.d("browser/SavePage", "FAIL_SAVE_PAGE: " + message.arg1);
                    Controller.this.mNotificationManager.notify(message.arg1, builder2.build());
                    query3.close();
                    this.mCr.delete(SnapshotProvider.Snapshots.CONTENT_URI, "job_id = ? and is_done = ?", strArr4);
                    return;
                default:
                    return;
            }
        }
    }

    /* loaded from: classes.dex */
    class BrowserSavePageClient extends SavePageClient {
        Tab mTab;

        public BrowserSavePageClient(Tab tab) {
            this.mTab = tab;
        }

        @Override // com.android.browser.SavePageClient
        public void getSaveDir(ValueCallback<String> valueCallback, boolean z) {
            if (this.mTab != null) {
                String title = this.mTab.getTitle();
                if (title == null) {
                    title = "";
                }
                StringBuilder sb = new StringBuilder(title.replace(':', '.'));
                sb.append(System.currentTimeMillis());
                Log.d("browser/SavePage", "save dir:" + Controller.mSavePageFolder + File.separator + sb.toString() + File.separator);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(Controller.mSavePageFolder);
                sb2.append(File.separator);
                sb2.append(sb.toString());
                sb2.append(File.separator);
                valueCallback.onReceiveValue(sb2.toString());
            }
        }

        @Override // com.android.browser.SavePageClient
        public void onSavePageStart(int i, String str) {
            Log.d("browser/SavePage", "onSavePageStart: " + i + " " + str);
            if (this.mTab == null) {
                Log.e("Controller", "onSavePageStart: the mTab does not exist!");
                return;
            }
            ContentValues createSavePageContentValues = this.mTab.createSavePageContentValues(i, str);
            this.mTab.addDatabaseItemId(i, -1L);
            Controller.this.mSavePageHandler.obtainMessage(1984, createSavePageContentValues).sendToTarget();
            Controller.this.mNotificationManager.notify(i, Controller.this.mBuilder.build());
            Controller.this.mProgress.put(Integer.valueOf(i), 0);
        }

        @Override // com.android.browser.SavePageClient
        public void onSaveProgressChange(int i, int i2) {
            Log.d("browser/SavePage", "onSaveProgressChange: " + i + " " + i2);
            int intValue = ((Integer) Controller.this.mProgress.get(Integer.valueOf(i2))).intValue();
            if ((i - intValue >= 25 || i == 100) && intValue != i) {
                Controller.this.mProgress.put(Integer.valueOf(i2), Integer.valueOf(i));
                Controller.this.mSavePageHandler.obtainMessage(1985, i, i2).sendToTarget();
            }
        }

        @Override // com.android.browser.SavePageClient
        public void onSaveFinish(int i, int i2) {
            Log.d("browser/SavePage", "onSaveFinish: " + i + " " + i2);
            Controller.this.mProgress.remove(Integer.valueOf(i2));
            this.mTab.removeDatabaseItemId(i2);
            if (i == 1) {
                Controller.this.mSavePageHandler.obtainMessage(1986, i2, 0).sendToTarget();
                return;
            }
            Toast.makeText(Controller.this.mActivity, (int) R.string.saved_page_failed, 1).show();
            Controller.this.mSavePageHandler.obtainMessage(1987, i2, 0).sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public PendingIntent createSavePagePendingIntent() {
        Intent intent = new Intent(this.mActivity, ComboViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("animate_id", 0L);
        bundle.putBoolean("disable_new_window", !this.mTabControl.canCreateNewTab());
        intent.putExtra("initial_view", UI.ComboViews.Snapshots.name());
        intent.putExtra("combo_args", bundle);
        return PendingIntent.getActivity(this.mActivity, 0, intent, 134217728);
    }

    private boolean isDefaultBookmarks(String str) {
        CharSequence[] textArray = this.mActivity.getResources().getTextArray(R.array.bookmarks);
        int length = textArray.length;
        for (int i = 0; i < length; i += 2) {
            if (str.equalsIgnoreCase(textArray[i + 1].toString())) {
                return true;
            }
        }
        return false;
    }
}
