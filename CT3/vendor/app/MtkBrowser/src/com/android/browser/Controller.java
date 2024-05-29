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
import android.os.storage.StorageManager;
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
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.MimeTypeMap;
import android.webkit.SavePageClient;
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
import com.mediatek.browser.hotknot.HotKnotHandler;
import com.mediatek.storage.StorageManagerEx;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
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
/* loaded from: b.zip:com/android/browser/Controller.class */
public class Controller implements WebViewController, UiController, ActivityController {

    /* renamed from: -assertionsDisabled  reason: not valid java name */
    static final boolean f2assertionsDisabled;
    private static final boolean DEBUG;
    private static final String[] IMAGE_VIEWABLE_SCHEMES;
    private static final String SAVE_PAGE_DIR;
    private static final String[] STORAGE_PERMISSIONS;
    private static final int[] WINDOW_SHORTCUT_ID_ARRAY;
    private static String mSavePageFolder;
    private static Bitmap sThumbnailBitmap;
    private static HandlerThread sUpdateSavePageThread;
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
    private WallpaperHandler mWallpaperHandler = null;
    private int mCurrentMenuState = 0;
    private int mMenuState = 2131558571;
    private int mOldMenuState = -1;
    private boolean mActivityPaused = true;
    private IBrowserMiscExt mBrowserMiscExt = null;
    private boolean mDelayRemoveLastTab = false;
    private BrowserSettings mSettings = BrowserSettings.getInstance();
    private TabControl mTabControl = new TabControl(this);

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/Controller$BrowserSavePageClient.class */
    public class BrowserSavePageClient extends SavePageClient {
        Tab mTab;
        final Controller this$0;

        public BrowserSavePageClient(Controller controller, Tab tab) {
            this.this$0 = controller;
            this.mTab = tab;
        }

        public void getSaveDir(ValueCallback<String> valueCallback, boolean z) {
            if (this.mTab != null) {
                String title = this.mTab.getTitle();
                String str = title;
                if (title == null) {
                    str = "";
                }
                StringBuilder sb = new StringBuilder(str.replace(':', '.'));
                sb.append(System.currentTimeMillis());
                Log.d("browser/SavePage", "save dir:" + Controller.mSavePageFolder + File.separator + sb.toString() + File.separator);
                valueCallback.onReceiveValue(Controller.mSavePageFolder + File.separator + sb.toString() + File.separator);
            }
        }

        public void onSaveFinish(int i, int i2) {
            Log.d("browser/SavePage", "onSaveFinish: " + i + " " + i2);
            this.mTab.removeDatabaseItemId(i2);
            switch (i) {
                case 1:
                    this.this$0.mSavePageHandler.obtainMessage(1986, i2, 0).sendToTarget();
                    return;
                default:
                    Toast.makeText(this.this$0.mActivity, 2131492919, 1).show();
                    this.this$0.mSavePageHandler.obtainMessage(1987, i2, 0).sendToTarget();
                    return;
            }
        }

        public void onSavePageStart(int i, String str) {
            Log.d("browser/SavePage", "onSavePageStart: " + i + " " + str);
            if (this.mTab == null) {
                Log.e("Controller", "onSavePageStart: the mTab does not exist!");
                return;
            }
            ContentValues createSavePageContentValues = this.mTab.createSavePageContentValues(i, str);
            this.mTab.addDatabaseItemId(i, -1L);
            this.this$0.mSavePageHandler.obtainMessage(1984, createSavePageContentValues).sendToTarget();
            this.this$0.mNotificationManager.notify(i, this.this$0.mBuilder.build());
        }

        public void onSaveProgressChange(int i, int i2) {
            Log.d("browser/SavePage", "onSaveProgressChange: " + i + " " + i2);
            this.this$0.mSavePageHandler.obtainMessage(1985, i, i2).sendToTarget();
        }
    }

    /* loaded from: b.zip:com/android/browser/Controller$Copy.class */
    private class Copy implements MenuItem.OnMenuItemClickListener {
        private CharSequence mText;
        final Controller this$0;

        public Copy(Controller controller, CharSequence charSequence) {
            this.this$0 = controller;
            this.mText = charSequence;
        }

        @Override // android.view.MenuItem.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem menuItem) {
            this.this$0.copy(this.mText);
            return true;
        }
    }

    /* loaded from: b.zip:com/android/browser/Controller$Download.class */
    private static class Download implements MenuItem.OnMenuItemClickListener {
        private Activity mActivity;
        private boolean mPrivateBrowsing;
        private String mText;
        private String mUserAgent;

        public Download(Activity activity, String str, boolean z, String str2) {
            this.mActivity = activity;
            this.mText = str;
            this.mPrivateBrowsing = z;
            this.mUserAgent = str2;
        }

        private File getTarget(DataUri dataUri) throws IOException {
            File externalFilesDir = this.mActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            String format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-", Locale.US).format(new Date());
            String mimeType = dataUri.getMimeType();
            String extensionFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            String str = extensionFromMimeType;
            if (extensionFromMimeType == null) {
                Log.w("Controller", "Unknown mime type in data URI" + mimeType);
                str = "dat";
            }
            return File.createTempFile(format, "." + str, externalFilesDir);
        }

        private void saveDataUri() {
            FileOutputStream fileOutputStream;
            Throwable th;
            DataUri dataUri;
            File target;
            FileOutputStream fileOutputStream2 = null;
            try {
                try {
                    dataUri = new DataUri(this.mText);
                    target = getTarget(dataUri);
                    fileOutputStream = new FileOutputStream(target);
                } catch (IOException e) {
                    fileOutputStream = null;
                }
            } catch (Throwable th2) {
                fileOutputStream = fileOutputStream2;
                th = th2;
            }
            try {
                fileOutputStream.write(dataUri.getData());
                ((DownloadManager) this.mActivity.getSystemService("download")).addCompletedDownload(target.getName(), this.mActivity.getTitle().toString(), false, dataUri.getMimeType(), target.getAbsolutePath(), dataUri.getData().length, true);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException e3) {
                fileOutputStream2 = fileOutputStream;
                Log.e("Controller", "Could not save data URL");
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }

        @Override // android.view.MenuItem.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (DataUri.isDataUri(this.mText)) {
                saveDataUri();
                return true;
            }
            DownloadHandler.onDownloadStartNoStream(this.mActivity, this.mText, this.mUserAgent, null, null, null, this.mPrivateBrowsing, 0L);
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/Controller$PruneThumbnails.class */
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

    /* loaded from: b.zip:com/android/browser/Controller$UpdateSavePageDBHandler.class */
    class UpdateSavePageDBHandler extends Handler {
        ContentResolver mCr;
        final Controller this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public UpdateSavePageDBHandler(Controller controller, Looper looper) {
            super(looper);
            this.this$0 = controller;
            this.mCr = controller.mActivity.getContentResolver();
        }

        /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:26:0x0272 -> B:23:0x0248). Please submit an issue!!! */
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String[] strArr = {"title"};
            String str = null;
            switch (message.what) {
                case 1984:
                    ContentValues contentValues = (ContentValues) message.obj;
                    long parseId = ContentUris.parseId(this.mCr.insert(SnapshotProvider.Snapshots.CONTENT_URI, contentValues));
                    int intValue = contentValues.getAsInteger("job_id").intValue();
                    Log.d("browser/SavePage", "ADD_SAVE_PAGE: " + intValue);
                    for (Tab tab : this.this$0.getTabControl().getTabs()) {
                        if (tab.containsDatabaseItemId(intValue)) {
                            tab.addDatabaseItemId(intValue, parseId);
                            return;
                        }
                    }
                    return;
                case 1985:
                    Cursor query = this.mCr.query(SnapshotProvider.Snapshots.CONTENT_URI, strArr, "job_id = ? and is_done = ?", new String[]{String.valueOf(message.arg2), "0"}, null);
                    String str2 = null;
                    while (true) {
                        String str3 = str2;
                        if (!query.moveToNext()) {
                            query.close();
                            this.this$0.mBuilder.setContentTitle(str3).setProgress(100, message.arg1, false).setContentInfo(message.arg1 + "%").setOngoing(true).setSmallIcon(2130837577);
                            this.this$0.mNotificationManager.notify(message.arg2, this.this$0.mBuilder.build());
                            ContentValues contentValues2 = new ContentValues();
                            contentValues2.put("progress", Integer.valueOf(message.arg1));
                            int i = message.arg2;
                            Log.d("browser/SavePage", "UPDATE_SAVE_PAGE: " + message.arg2);
                            this.mCr.update(SnapshotProvider.Snapshots.CONTENT_URI, contentValues2, "job_id = ? and progress < ?", new String[]{String.valueOf(i), "100"});
                            return;
                        }
                        str2 = query.getString(0);
                    }
                case 1986:
                    Notification.Builder builder = new Notification.Builder(this.this$0.mActivity);
                    ContentValues contentValues3 = new ContentValues();
                    contentValues3.put("progress", (Integer) 100);
                    contentValues3.put("is_done", (Integer) 1);
                    String[] strArr2 = {String.valueOf(message.arg1), "0"};
                    Cursor query2 = this.mCr.query(SnapshotProvider.Snapshots.CONTENT_URI, new String[]{"title", "viewstate_path"}, "job_id = ? and is_done = ?", strArr2, null);
                    long j = 0;
                    while (query2.moveToNext()) {
                        String string = query2.getString(1);
                        String string2 = query2.getString(0);
                        str = string2;
                        if (!TextUtils.isEmpty(string)) {
                            File file = new File(string.substring(0, string.lastIndexOf(File.separator)));
                            try {
                                j = this.this$0.getSavePageDirSize(file);
                            } catch (IOException e) {
                                j = 0;
                            }
                            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                            intent.setData(Uri.fromFile(file));
                            this.this$0.mActivity.sendBroadcast(intent);
                            str = string2;
                        }
                    }
                    query2.close();
                    contentValues3.put("viewstate_size", Long.valueOf(j));
                    this.mCr.update(SnapshotProvider.Snapshots.CONTENT_URI, contentValues3, "job_id = ? and is_done = ?", strArr2);
                    builder.setContentIntent(this.this$0.createSavePagePendingIntent()).setAutoCancel(true).setOngoing(false).setContentTitle(str).setSmallIcon(2130837577).setContentText(this.this$0.mActivity.getText(2131492920));
                    Log.d("browser/SavePage", "FINISH_SAVE_PAGE: " + message.arg1);
                    this.this$0.mNotificationManager.notify(message.arg1, builder.build());
                    return;
                case 1987:
                    Notification.Builder builder2 = new Notification.Builder(this.this$0.mActivity);
                    String[] strArr3 = {String.valueOf(message.arg1), "0"};
                    Cursor query3 = this.mCr.query(SnapshotProvider.Snapshots.CONTENT_URI, strArr, "job_id = ? and is_done = ?", strArr3, null);
                    String str4 = null;
                    while (query3.moveToNext()) {
                        str4 = query3.getString(0);
                        Log.d("browser/SavePage", "fail title is: " + str4);
                    }
                    if (str4 != null) {
                        builder2.setContentTitle(this.this$0.mActivity.getText(2131492921) + str4).setContentIntent(null).setAutoCancel(true).setOngoing(false).setSmallIcon(2130837578);
                    } else {
                        builder2.setContentTitle(this.this$0.mActivity.getText(2131492921)).setContentIntent(null).setAutoCancel(true).setOngoing(false).setSmallIcon(2130837578);
                    }
                    Log.d("browser/SavePage", "FAIL_SAVE_PAGE: " + message.arg1);
                    this.this$0.mNotificationManager.notify(message.arg1, builder2.build());
                    query3.close();
                    this.mCr.delete(SnapshotProvider.Snapshots.CONTENT_URI, "job_id = ? and is_done = ?", strArr3);
                    return;
                default:
                    return;
            }
        }
    }

    static {
        f2assertionsDisabled = !Controller.class.desiredAssertionStatus();
        DEBUG = Browser.DEBUG;
        SAVE_PAGE_DIR = File.separator + "Download" + File.separator + "SavedPages";
        sUpdateSavePageThread = new HandlerThread("save_page");
        sUpdateSavePageThread.start();
        WINDOW_SHORTCUT_ID_ARRAY = new int[]{2131558601, 2131558602, 2131558603, 2131558604, 2131558605, 2131558606, 2131558607, 2131558608};
        IMAGE_VIEWABLE_SCHEMES = new String[]{"data", "http", "https", "file"};
        STORAGE_PERMISSIONS = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    }

    public Controller(Activity activity) {
        this.mActivity = activity;
        this.mSettings.setController(this);
        Intent intent = activity.getIntent();
        if (intent != null ? intent.getBooleanExtra("HotKnot_Intent", false) : false) {
            this.mSettings.setLastRunPaused(true);
        }
        this.mCrashRecoveryHandler = CrashRecoveryHandler.initialize(this);
        this.mCrashRecoveryHandler.preloadCrashState();
        this.mFactory = new BrowserWebViewFactory(activity);
        this.mUrlHandler = new UrlHandler(this);
        this.mIntentHandler = new IntentHandler(this.mActivity, this);
        this.mPageDialogsHandler = new PageDialogsHandler(this.mActivity, this);
        startHandler();
        this.mSavePageHandler = new UpdateSavePageDBHandler(this, sUpdateSavePageThread.getLooper());
        this.mBuilder = new Notification.Builder(this.mActivity);
        this.mNotificationManager = (NotificationManager) this.mActivity.getSystemService("notification");
        this.mBookmarksObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.browser.Controller.1
            final Controller this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int tabCount = this.this$0.mTabControl.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    this.this$0.mTabControl.getTab(i).updateBookmarkedStatus();
                }
            }
        };
        this.mSiteNavigationObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.browser.Controller.2
            final Controller this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                Log.d("Controller", "SiteNavigation.SITE_NAVIGATION_URI changed");
                if (this.this$0.getCurrentTopWebView() == null || this.this$0.getCurrentTopWebView().getUrl() == null || !this.this$0.getCurrentTopWebView().getUrl().equals("content://com.android.browser.site_navigation/websites")) {
                    return;
                }
                Log.d("Controller", "start reload");
                this.this$0.getCurrentTopWebView().reload();
            }
        };
        activity.getContentResolver().registerContentObserver(BrowserContract.Bookmarks.CONTENT_URI, true, this.mBookmarksObserver);
        activity.getContentResolver().registerContentObserver(SiteNavigation.SITE_NAVIGATION_URI, true, this.mSiteNavigationObserver);
        this.mNetworkHandler = new NetworkStateHandler(this.mActivity, this);
        this.mSystemAllowGeolocationOrigins = new SystemAllowGeolocationOrigins(this.mActivity.getApplicationContext());
        this.mSystemAllowGeolocationOrigins.start();
        openIconDatabase();
        HotKnotHandler.hotKnotInit(this.mActivity);
    }

    private boolean checkStorageState() {
        String string;
        int i;
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals("mounted")) {
            return true;
        }
        if (externalStorageState.equals("shared")) {
            string = this.mActivity.getString(2131493220);
            i = 2131493219;
        } else {
            string = this.mActivity.getString(2131493218);
            i = 2131493217;
        }
        new AlertDialog.Builder(this.mActivity).setTitle(i).setIconAttribute(16843605).setMessage(string).setPositiveButton(2131492963, (DialogInterface.OnClickListener) null).show();
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void copy(CharSequence charSequence) {
        ((ClipboardManager) this.mActivity.getSystemService("clipboard")).setText(charSequence);
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
        Bitmap bitmap = favicon;
        if (favicon != null) {
            bitmap = favicon;
            if (favicon.getWidth() > 60) {
                bitmap = Bitmap.createScaledBitmap(favicon, 60, 60, true);
            }
        }
        intent.putExtra("favicon", bitmap);
        if (z) {
            intent.putExtra("check_for_dupe", true);
        }
        intent.putExtra("gravity", 53);
        return intent;
    }

    private Tab createNewTab(boolean z, boolean z2, boolean z3) {
        Tab tab = null;
        if (this.mTabControl.canCreateNewTab()) {
            Tab createNewTab = this.mTabControl.createNewTab(z);
            addTab(createNewTab);
            tab = createNewTab;
            if (z2) {
                setActiveTab(createNewTab);
                tab = createNewTab;
            }
        } else if (z3) {
            tab = this.mTabControl.getCurrentTab();
            reuseTab(tab, null);
        } else {
            this.mUi.showMaxTabsWarning();
        }
        if (DEBUG) {
            Log.d("browser", "Controller.createNewTab()--->tab is " + tab);
        }
        return tab;
    }

    private boolean createSavePageFolder() {
        String defaultPath = StorageManagerEx.getDefaultPath();
        String str = defaultPath;
        if (!new File(defaultPath).canWrite()) {
            Log.d("browser/SavePage", "default path: " + defaultPath + " can't write");
            str = ((StorageManager) this.mActivity.getSystemService(StorageManager.class)).getPrimaryVolume().getPath();
        }
        Log.d("browser/SavePage", "default path: " + str);
        mSavePageFolder = str + SAVE_PAGE_DIR;
        File file = new File(mSavePageFolder);
        if (file.exists() || file.mkdirs()) {
            return true;
        }
        Toast.makeText(this.mActivity, 2131492922, 1).show();
        return false;
    }

    private void createSavePageNotification() {
        this.mBuilder.setContentTitle(getTabControl().getCurrentTab().getTitle());
        this.mBuilder.setSmallIcon(2130837577);
        this.mBuilder.setProgress(100, 0, false);
        this.mBuilder.setTicker(this.mActivity.getText(2131492923));
        this.mBuilder.setOngoing(false);
        this.mBuilder.setContentIntent(createSavePagePendingIntent());
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
        if (webView instanceof BrowserWebView) {
            canvas.translate(0.0f, (-((BrowserWebView) webView).getTitleHeight()) * scale);
        }
        int scrollX = webView.getScrollX();
        int scrollY = webView.getScrollY() + webView.getVisibleTitleHeight();
        canvas.translate(-scrollX, -scrollY);
        if (DEBUG) {
            Log.d("browser", "createScreenShot()--->left = " + scrollX + ", top = " + scrollY + ", overviewScale = " + scale);
        }
        canvas.scale(scale, scale, scrollX, scrollY);
        if (webView instanceof BrowserWebView) {
            ((BrowserWebView) webView).drawContent(canvas);
        } else {
            webView.draw(canvas);
        }
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(sThumbnailBitmap, i, i2, true);
        canvas.setBitmap(null);
        return createScaledBitmap;
    }

    private void downloadStart(Activity activity, String str, String str2, String str3, String str4, String str5, boolean z, long j, WebView webView, Tab tab) {
        List<String> ungrantedPermissions = PermissionHelper.getInstance().getUngrantedPermissions(STORAGE_PERMISSIONS);
        if (ungrantedPermissions.size() != 0) {
            PermissionHelper.getInstance().requestPermissions(ungrantedPermissions, new PermissionHelper.PermissionCallback(this, ungrantedPermissions, str, str2, str3, str4, str5, j, webView, tab) { // from class: com.android.browser.Controller.10
                final Controller this$0;
                final String val$contentDisposition;
                final long val$contentLength;
                final String val$mimetype;
                final String val$referer;
                final Tab val$tab;
                final List val$ungranted;
                final String val$url;
                final String val$userAgent;
                final WebView val$w;

                {
                    this.this$0 = this;
                    this.val$ungranted = ungrantedPermissions;
                    this.val$url = str;
                    this.val$userAgent = str2;
                    this.val$contentDisposition = str3;
                    this.val$mimetype = str4;
                    this.val$referer = str5;
                    this.val$contentLength = j;
                    this.val$w = webView;
                    this.val$tab = tab;
                }

                @Override // com.android.browser.PermissionHelper.PermissionCallback
                public void onPermissionsResult(int i, String[] strArr, int[] iArr) {
                    boolean z2;
                    boolean z3;
                    Log.d("browser/Controller", " onRequestPermissionsResult " + i);
                    if (iArr == null || iArr.length <= 0) {
                        return;
                    }
                    Iterator it = this.val$ungranted.iterator();
                    while (true) {
                        z2 = true;
                        if (!it.hasNext()) {
                            break;
                        }
                        String str6 = (String) it.next();
                        int i2 = 0;
                        while (true) {
                            z3 = false;
                            if (i2 < iArr.length) {
                                if (str6.equalsIgnoreCase(strArr[i2]) && iArr[i2] == 0) {
                                    z3 = true;
                                    break;
                                }
                                i2++;
                            } else {
                                break;
                            }
                        }
                        if (!z3) {
                            Log.d("browser/Controller", str6 + " is not granted !");
                            z2 = false;
                            break;
                        }
                    }
                    if (z2) {
                        DownloadHandler.onDownloadStart(this.this$0.mActivity, this.val$url, this.val$userAgent, this.val$contentDisposition, this.val$mimetype, this.val$referer, false, this.val$contentLength);
                    }
                    if (this.val$w == null || this.val$w.copyBackForwardList().getSize() != 0) {
                        return;
                    }
                    if (this.val$tab == this.this$0.mTabControl.getCurrentTab()) {
                        this.this$0.goBackOnePageOrQuit();
                    } else {
                        this.this$0.closeTab(this.val$tab);
                    }
                }
            });
            return;
        }
        DownloadHandler.onDownloadStart(this.mActivity, str, str2, str3, str4, str5, false, j);
        if (webView == null || webView.copyBackForwardList().getSize() != 0) {
            return;
        }
        if (tab == this.mTabControl.getCurrentTab()) {
            goBackOnePageOrQuit();
        } else {
            closeTab(tab);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getDesiredThumbnailHeight(Context context) {
        return context.getResources().getDimensionPixelOffset(2131427336);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getDesiredThumbnailWidth(Context context) {
        return context.getResources().getDimensionPixelOffset(2131427335);
    }

    private Tab getNextTab() {
        int currentPosition = this.mTabControl.getCurrentPosition() + 1;
        int i = currentPosition;
        if (currentPosition >= this.mTabControl.getTabCount()) {
            i = 0;
        }
        return this.mTabControl.getTab(i);
    }

    private Tab getPrevTab() {
        int currentPosition = this.mTabControl.getCurrentPosition() - 1;
        int i = currentPosition;
        if (currentPosition < 0) {
            i = this.mTabControl.getTabCount() - 1;
        }
        return this.mTabControl.getTab(i);
    }

    private void goLive() {
        Tab currentTab = getCurrentTab();
        currentTab.loadUrl(currentTab.getUrl(), null);
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

    private void maybeUpdateFavicon(Tab tab, String str, String str2, Bitmap bitmap) {
        if (DEBUG) {
            bitmap = null;
            Log.i("browser", "Controller.maybeUpdateFavicon()--->tab = " + tab + ", originalUrl = " + str + ", url = " + str2 + ", favicon is null:" + ((Object) null));
        }
        if (bitmap == null || tab.isPrivateBrowsingEnabled()) {
            return;
        }
        Bookmarks.updateFavicon(this.mActivity.getContentResolver(), str, str2, bitmap);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPreloginFinished(Bundle bundle, Intent intent, long j, boolean z) {
        int i;
        if (j == -1) {
            BackgroundHandler.execute(new PruneThumbnails(this.mActivity, null));
            if (intent == null) {
                openTabToHomePage();
            } else {
                Bundle extras = intent.getExtras();
                IntentHandler.UrlData urlData = (intent.getData() != null && "android.intent.action.VIEW".equals(intent.getAction()) && intent.getData().toString().startsWith("content://")) ? new IntentHandler.UrlData(intent.getData().toString()) : IntentHandler.getUrlDataFromIntent(intent);
                Tab openTabToHomePage = urlData.isEmpty() ? openTabToHomePage() : openTab(urlData);
                if (openTabToHomePage != null) {
                    openTabToHomePage.setAppId(intent.getStringExtra("com.android.browser.application_id"));
                }
                WebView webView = openTabToHomePage.getWebView();
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
        if (intent == null || !"show_bookmarks".equals(intent.getAction())) {
            return;
        }
        bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
    }

    private void openIconDatabase() {
        BackgroundHandler.execute(new Runnable(this, WebIconDatabase.getInstance()) { // from class: com.android.browser.Controller.4
            final Controller this$0;
            final WebIconDatabase val$instance;

            {
                this.this$0 = this;
                this.val$instance = r5;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$instance.open(this.this$0.mActivity.getDir("icons", 0).getPath());
            }
        });
    }

    private boolean pauseWebViewTimers(Tab tab) {
        if (tab == null) {
            return true;
        }
        if (tab.inPageLoad()) {
            return false;
        }
        CookieSyncManager.getInstance().stopSync();
        WebViewTimersControl.getInstance().onBrowserActivityPause(getCurrentWebView());
        return true;
    }

    private void releaseWakeLock() {
        if (this.mWakeLock == null || !this.mWakeLock.isHeld()) {
            return;
        }
        this.mHandler.removeMessages(107);
        this.mWakeLock.release();
    }

    private void resumeWebViewTimers(Tab tab) {
        boolean inPageLoad = tab.inPageLoad();
        if ((this.mActivityPaused || inPageLoad) && !(this.mActivityPaused && inPageLoad)) {
            return;
        }
        CookieSyncManager.getInstance().startSync();
        WebViewTimersControl.getInstance().onBrowserActivityResume(tab.getWebView());
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
        Bitmap bitmap3 = bitmap;
        if (bitmap != null) {
            bitmap3 = bitmap;
            if (bitmap.getWidth() > 60) {
                bitmap3 = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
            }
        }
        intent.putExtra("share_favicon", bitmap3);
        intent.putExtra("share_screenshot", bitmap2);
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(2131493050)));
        } catch (ActivityNotFoundException e) {
        }
    }

    private void showCloseSelectionDialog() {
        new AlertDialog.Builder(this.mActivity).setTitle(2131492874).setItems(new CharSequence[]{this.mActivity.getString(2131492875), this.mActivity.getString(2131492876)}, new DialogInterface.OnClickListener(this) { // from class: com.android.browser.Controller.17
            final Controller this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    this.this$0.mActivity.moveTaskToBack(true);
                } else if (i == 1) {
                    if (((ActivityManager) this.this$0.mActivity.getSystemService("activity")).isInLockTaskMode()) {
                        this.this$0.mActivity.showLockTaskEscapeMessage();
                        return;
                    }
                    this.this$0.mNotificationManager.cancelAll();
                    this.this$0.mUi.hideIME();
                    this.this$0.onDestroy();
                    this.this$0.mActivity.finish();
                    File file = new File(this.this$0.getActivity().getApplicationContext().getCacheDir(), "browser_state.parcel");
                    if (file.exists()) {
                        file.delete();
                    }
                    this.this$0.mActivity.sendBroadcast(new Intent("android.intent.action.stk.BROWSER_TERMINATION"));
                    Process.killProcess(Process.myPid());
                }
            }
        }).show();
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

    private void startHandler() {
        this.mHandler = new Handler(this) { // from class: com.android.browser.Controller.5
            final Controller this$0;

            {
                this.this$0 = this;
            }

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
                        String str4 = str;
                        if (str == "") {
                            str4 = str3;
                        }
                        if (TextUtils.isEmpty(str4) || this.this$0.getCurrentTopWebView() != (webView = (WebView) ((HashMap) message.obj).get("webview"))) {
                            return;
                        }
                        switch (message.arg1) {
                            case 2131558433:
                                if (str4 == null || !str4.startsWith("rtsp://")) {
                                    if (str4 == null || !str4.startsWith("wtai://wp/mc;")) {
                                        this.this$0.loadUrlFromContext(str4);
                                        return;
                                    } else {
                                        this.this$0.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("tel:" + str4.replaceAll(" ", "%20").substring("wtai://wp/mc;".length()))));
                                        return;
                                    }
                                }
                                Intent intent = new Intent();
                                intent.setAction("android.intent.action.VIEW");
                                intent.setData(Uri.parse(str4.replaceAll(" ", "%20")));
                                intent.addFlags(268435456);
                                this.this$0.mActivity.startActivity(intent);
                                return;
                            case 2131558626:
                                this.this$0.openTab(str4, this.this$0.mTabControl.getCurrentTab(), !this.this$0.mSettings.openInBackground(), true);
                                return;
                            case 2131558627:
                            case 2131558631:
                                DownloadHandler.onDownloadStartNoStream(this.this$0.mActivity, str4, webView.getSettings().getUserAgentString(), null, null, null, webView.isPrivateBrowsingEnabled(), 0L);
                                return;
                            case 2131558628:
                                this.this$0.copy(str4);
                                return;
                            case 2131558629:
                                Intent createBookmarkLinkIntent = this.this$0.createBookmarkLinkIntent(str4);
                                if (createBookmarkLinkIntent != null) {
                                    this.this$0.mActivity.startActivity(createBookmarkLinkIntent);
                                    return;
                                }
                                return;
                            case 2131558632:
                                this.this$0.loadUrlFromContext(str3);
                                return;
                            default:
                                return;
                        }
                    case 107:
                        if (this.this$0.mWakeLock == null || !this.this$0.mWakeLock.isHeld()) {
                            return;
                        }
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->RELEASE_WAKELOCK");
                        }
                        this.this$0.mWakeLock.release();
                        this.this$0.mTabControl.stopAllLoading();
                        return;
                    case 108:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->UPDATE_BOOKMARK_THUMBNAIL");
                        }
                        Tab tab = (Tab) message.obj;
                        if (tab != null) {
                            this.this$0.updateScreenshot(tab);
                            return;
                        }
                        return;
                    case 201:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->OPEN_BOOKMARKS");
                        }
                        this.this$0.bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
                        return;
                    case 1001:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->LOAD_URL");
                        }
                        this.this$0.loadUrlFromContext((String) message.obj);
                        return;
                    case 1002:
                        if (Controller.DEBUG) {
                            Log.i("browser", "Controller.startHandler()--->STOP_LOAD");
                        }
                        this.this$0.stopLoading();
                        return;
                    case 1100:
                        this.this$0.getTabControl().freeMemory();
                        new CheckMemoryTask(this.this$0.mHandler).execute(Integer.valueOf(this.this$0.getTabControl().getVisibleWebviewNums()), null, false, null, this.this$0.getTabControl().getFreeTabIndex(), false);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v20, types: [com.android.browser.Controller$18] */
    public void updateScreenshot(Tab tab) {
        Bitmap createScreenshot;
        String host;
        if (DEBUG) {
            Log.i("browser", "Controller.updateScreenshot()--->tab is " + tab);
        }
        WebView webView = tab.getWebView();
        if (webView == null) {
            return;
        }
        String url = tab.getUrl();
        String originalUrl = webView.getOriginalUrl();
        if (originalUrl == null) {
            originalUrl = url;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Log.d("Controller", " originalUrl: " + originalUrl + " url: " + url);
        if (Patterns.WEB_URL.matcher(url).matches() || tab.isBookmarkedSite()) {
            if ((url != null && Patterns.WEB_URL.matcher(url).matches() && ((host = new WebAddress(url).getHost()) == null || host.length() == 0)) || (createScreenshot = createScreenshot(webView, getDesiredThumbnailWidth(this.mActivity), getDesiredThumbnailHeight(this.mActivity))) == null) {
                return;
            }
            new AsyncTask<Void, Void, Void>(this, this.mActivity.getContentResolver(), originalUrl, url, createScreenshot) { // from class: com.android.browser.Controller.18
                final Controller this$0;
                final Bitmap val$bm;
                final ContentResolver val$cr;
                final String val$originalUrl;
                final String val$url;

                {
                    this.this$0 = this;
                    this.val$cr = r5;
                    this.val$originalUrl = originalUrl;
                    this.val$url = url;
                    this.val$bm = createScreenshot;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                /* JADX WARN: Removed duplicated region for block: B:31:0x00e0  */
                /* JADX WARN: Removed duplicated region for block: B:52:? A[RETURN, SYNTHETIC] */
                @Override // android.os.AsyncTask
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                */
                public Void doInBackground(Void... voidArr) {
                    Cursor cursor = null;
                    Cursor cursor2 = null;
                    Cursor cursor3 = null;
                    try {
                        try {
                            Cursor queryCombinedForUrl = Bookmarks.queryCombinedForUrl(this.val$cr, this.val$originalUrl, this.val$url);
                            if (queryCombinedForUrl == null || !queryCombinedForUrl.moveToFirst()) {
                                if (queryCombinedForUrl == null) {
                                    queryCombinedForUrl.close();
                                    return null;
                                }
                                return null;
                            }
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            this.val$bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("thumbnail", byteArrayOutputStream.toByteArray());
                            do {
                                cursor3 = queryCombinedForUrl;
                                cursor = queryCombinedForUrl;
                                cursor2 = queryCombinedForUrl;
                                contentValues.put("url_key", queryCombinedForUrl.getString(0));
                                this.val$cr.update(BrowserContract.Images.CONTENT_URI, contentValues, null, null);
                            } while (queryCombinedForUrl.moveToNext());
                            if (queryCombinedForUrl == null) {
                            }
                        } catch (SQLiteException e) {
                            Log.w("Controller", "Error when running updateScreenshot ", e);
                            if (cursor3 != null) {
                                cursor3.close();
                                return null;
                            }
                            return null;
                        } catch (IllegalStateException e2) {
                            if (cursor != null) {
                                cursor.close();
                                return null;
                            }
                            return null;
                        }
                    } catch (Throwable th) {
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        throw th;
                    }
                }
            }.execute(new Void[0]);
        }
    }

    private void updateShareMenuItems(Menu menu, Tab tab) {
        Log.d("browser/Controller", "updateShareMenuItems start");
        if (menu == null) {
            return;
        }
        MenuItem findItem = menu.findItem(2131558581);
        if (tab == null) {
            Log.d("browser/Controller", "tab == null");
            findItem.setEnabled(false);
        } else {
            String url = tab.getUrl();
            if (url == null || url.length() == 0) {
                Log.d("browser/Controller", "url == null||url.length() == 0");
                findItem.setEnabled(false);
            } else {
                Log.d("browser/Controller", "url :" + url);
                findItem.setEnabled(true);
            }
        }
        Log.d("browser/Controller", "updateShareMenuItems end");
    }

    protected void addTab(Tab tab) {
        if (DEBUG) {
            Log.d("browser", "Controller.addTab()--->tab : " + tab);
        }
        this.mUi.addTab(tab);
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
    public void attachSubWindow(Tab tab) {
        if (tab.getSubWebView() != null) {
            this.mUi.attachSubWindow(tab.getSubViewContainer());
            getCurrentTopWebView().requestFocus();
        }
    }

    @Override // com.android.browser.UiController
    public void bookmarkCurrentPage() {
        Intent createBookmarkCurrentPageIntent = createBookmarkCurrentPageIntent(false);
        if (createBookmarkCurrentPageIntent != null) {
            this.mActivity.startActivity(createBookmarkCurrentPageIntent);
        }
    }

    @Override // com.android.browser.WebViewController
    public void bookmarkedStatusHasChanged(Tab tab) {
        this.mUi.bookmarkedStatusHasChanged(tab);
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
            if (z) {
                this.mDelayRemoveLastTab = true;
            } else {
                this.mTabControl.removeTab(getCurrentTab());
            }
            this.mActivity.finish();
            return true;
        }
        Tab currentTab = this.mTabControl.getCurrentTab();
        int currentPosition = this.mTabControl.getCurrentPosition();
        Tab parent = currentTab.getParent();
        Tab tab = parent;
        if (parent == null) {
            Tab tab2 = this.mTabControl.getTab(currentPosition + 1);
            tab = tab2;
            if (tab2 == null) {
                tab = this.mTabControl.getTab(currentPosition - 1);
            }
        }
        if (z) {
            this.mTabControl.setCurrentTab(tab);
            this.mUi.closeTableDelay(currentTab);
            return false;
        } else if (switchToTab(tab)) {
            closeTab(currentTab);
            return false;
        } else {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void closeEmptyTab() {
        Tab currentTab = this.mTabControl.getCurrentTab();
        if (currentTab == null || currentTab.getWebView().copyBackForwardList().getSize() != 0) {
            return;
        }
        closeCurrentTab();
    }

    public void closeOtherTabs() {
        if (DEBUG) {
            Log.i("browser", "Controller.closeOtherTabs()--->");
        }
        int tabCount = this.mTabControl.getTabCount();
        ArrayList arrayList = new ArrayList();
        while (true) {
            tabCount--;
            if (tabCount < 0) {
                new CheckMemoryTask(this.mHandler).execute(Integer.valueOf(getTabControl().getVisibleWebviewNums()), arrayList, false, null, null, true);
                return;
            }
            Tab tab = this.mTabControl.getTab(tabCount);
            if (tab != this.mTabControl.getCurrentTab()) {
                arrayList.add(Integer.valueOf(this.mTabControl.getTabPosition(tab)));
                removeTab(tab);
            }
        }
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
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

    @Override // com.android.browser.UiController
    public Intent createBookmarkCurrentPageIntent(boolean z) {
        return createBookmarkPageIntent(z, null, null);
    }

    public Intent createBookmarkLinkIntent(String str) {
        return createBookmarkPageIntent(false, str, "");
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

    @Override // com.android.browser.WebViewController
    public void createSubWindow(Tab tab) {
        endActionMode();
        WebView webView = tab.getWebView();
        this.mUi.createSubWindow(tab, this.mFactory.createWebView(webView == null ? false : webView.isPrivateBrowsingEnabled()));
    }

    boolean didUserStopLoading() {
        return this.mLoadStopped;
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

    @Override // com.android.browser.ActivityController
    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        return this.mBlockEvents;
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public void doStart(Bundle bundle, Intent intent) {
        Calendar calendar = bundle != null ? (Calendar) bundle.getSerializable("lastActiveDate") : null;
        Calendar calendar2 = Calendar.getInstance();
        Calendar calendar3 = Calendar.getInstance();
        calendar3.add(5, -1);
        boolean z = (calendar == null || calendar.before(calendar3) || calendar.after(calendar2)) ? false : true;
        long canRestoreState = this.mTabControl.canRestoreState(bundle, z);
        if (canRestoreState == -1) {
            CookieManager.getInstance().removeSessionCookie();
        }
        GoogleAccountLogin.startLoginIfNeeded(this.mActivity, new Runnable(this, bundle, intent, canRestoreState, z) { // from class: com.android.browser.Controller.3
            final Controller this$0;
            final long val$currentTabId;
            final Bundle val$icicle;
            final Intent val$intent;
            final boolean val$restoreIncognitoTabs;

            {
                this.this$0 = this;
                this.val$icicle = bundle;
                this.val$intent = intent;
                this.val$currentTabId = canRestoreState;
                this.val$restoreIncognitoTabs = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.onPreloginFinished(this.val$icicle, this.val$intent, this.val$currentTabId, this.val$restoreIncognitoTabs);
            }
        });
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

    public void editUrl() {
        if (this.mOptionsMenuOpen) {
            this.mActivity.closeOptionsMenu();
        }
        this.mUi.editUrl(false, true);
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
    public void endActionMode() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
    }

    @Override // com.android.browser.UiController
    public void findOnPage() {
        getCurrentTopWebView().showFindDialog(null, true);
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
    public Activity getActivity() {
        return this.mActivity;
    }

    @Override // com.android.browser.WebViewController
    public Context getContext() {
        return this.mActivity;
    }

    @Override // com.android.browser.UiController
    public Tab getCurrentTab() {
        return this.mTabControl.getCurrentTab();
    }

    @Override // com.android.browser.UiController
    public WebView getCurrentTopWebView() {
        return this.mTabControl.getCurrentTopWebView();
    }

    @Override // com.android.browser.UiController
    public WebView getCurrentWebView() {
        return this.mTabControl.getCurrentWebView();
    }

    @Override // com.android.browser.WebViewController
    public Bitmap getDefaultVideoPoster() {
        return this.mUi.getDefaultVideoPoster();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getMaxTabs() {
        int integer = this.mActivity.getResources().getInteger(2131623938);
        String str = SystemProperties.get("ro.mtk_gmo_ram_optimize");
        int i = integer;
        if (str != null) {
            i = integer;
            if (str.equals("1")) {
                i = integer / 2;
            }
        }
        return i;
    }

    long getSavePageDirSize(File file) throws IOException {
        long j;
        long length;
        long j2 = 0;
        File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return 0L;
        }
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].isDirectory()) {
                j = j2;
                length = getSavePageDirSize(listFiles[i]);
            } else {
                j = j2;
                length = listFiles[i].length();
            }
            j2 = j + length;
        }
        return j2;
    }

    @Override // com.android.browser.UiController
    public BrowserSettings getSettings() {
        return this.mSettings;
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
    public TabControl getTabControl() {
        return this.mTabControl;
    }

    @Override // com.android.browser.UiController
    public List<Tab> getTabs() {
        return this.mTabControl.getTabs();
    }

    @Override // com.android.browser.UiController
    public UI getUi() {
        return this.mUi;
    }

    @Override // com.android.browser.WebViewController
    public View getVideoLoadingProgressView() {
        return this.mUi.getVideoLoadingProgressView();
    }

    @Override // com.android.browser.WebViewController
    public void getVisitedHistory(ValueCallback<String[]> valueCallback) {
        new AsyncTask<Void, Void, String[]>(this, valueCallback) { // from class: com.android.browser.Controller.6
            final Controller this$0;
            final ValueCallback val$callback;

            {
                this.this$0 = this;
                this.val$callback = valueCallback;
            }

            @Override // android.os.AsyncTask
            public String[] doInBackground(Void... voidArr) {
                return com.android.browser.provider.Browser.getVisitedHistory(this.this$0.mActivity.getContentResolver());
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(String[] strArr) {
                this.val$callback.onReceiveValue(strArr);
            }
        }.execute(new Void[0]);
    }

    public WebViewFactory getWebViewFactory() {
        return this.mFactory;
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
                this.mActivity.moveTaskToBack(true);
                onPause();
            }
        }
        if (DEBUG) {
            Log.i("browser", "Controller.goBackOnePageOrQuit()--->current tab is " + currentTab);
        }
    }

    @Override // com.android.browser.UiController, com.android.browser.ActivityController
    public void handleNewIntent(Intent intent) {
        if (getTabControl().getTabCount() == 0) {
            start(intent);
        }
        if (!this.mUi.isWebShowing()) {
            this.mUi.showWeb(false);
        }
        this.mIntentHandler.onNewIntent(intent);
    }

    @Override // com.android.browser.WebViewController
    public void hideAutoLogin(Tab tab) {
        if (!f2assertionsDisabled && !tab.inForeground()) {
            throw new AssertionError();
        }
        this.mUi.hideAutoLogin(tab);
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
    public void hideCustomView() {
        if (this.mUi.isCustomViewShowing()) {
            this.mUi.onHideCustomView();
            this.mMenuState = this.mOldMenuState;
            this.mOldMenuState = -1;
            this.mActivity.invalidateOptionsMenu();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isActivityPaused() {
        return this.mActivityPaused;
    }

    @Override // com.android.browser.UiController
    public boolean isInCustomActionMode() {
        return this.mActionMode != null;
    }

    boolean isInLoad() {
        Tab currentTab = getCurrentTab();
        return currentTab != null ? currentTab.inPageLoad() : false;
    }

    public boolean isMenuDown() {
        return this.mMenuIsDown;
    }

    boolean isMenuOrCtrlKey(int i) {
        boolean z = true;
        if (82 != i) {
            if (113 == i) {
                z = true;
            } else {
                z = true;
                if (114 != i) {
                    z = false;
                }
            }
        }
        return z;
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
        if (urlData == null || urlData.isPreloaded()) {
            return;
        }
        if (tab != null && urlData.mDisableUrlOverride) {
            tab.disableUrlOverridingForLoad();
        }
        loadUrl(tab, urlData.mUrl, urlData.mHeaders);
    }

    protected void loadUrlFromContext(String str) {
        if (DEBUG) {
            Log.i("browser", "Controller.loadUrlFromContext()--->url : " + str);
        }
        Tab currentTab = getCurrentTab();
        BrowserWebView webView = currentTab != null ? currentTab.getWebView() : null;
        if (str == null || str.length() == 0 || currentTab == null || webView == null) {
            return;
        }
        String smartUrlFilter = UrlUtils.smartUrlFilter(str);
        if (webView.getWebViewClient().shouldOverrideUrlLoading(webView, smartUrlFilter)) {
            return;
        }
        loadUrl(currentTab, smartUrlFilter);
    }

    @Override // com.android.browser.ActivityController
    public void onActionModeFinished(ActionMode actionMode) {
        if (isInCustomActionMode()) {
            this.mUi.onActionModeFinished(isInLoad());
            this.mActionMode = null;
        }
    }

    @Override // com.android.browser.ActivityController
    public void onActionModeStarted(ActionMode actionMode) {
        this.mUi.onActionModeStarted(actionMode);
        this.mActionMode = actionMode;
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
                        String str2 = stringExtra;
                        if (stringExtra == null) {
                            str2 = this.mSettings.getHomePage();
                        }
                        if (longExtra >= 0) {
                            Tab currentTab2 = getCurrentTab();
                            currentTab2.mSavePageUrl = str2;
                            currentTab2.mSavePageTitle = intent.getStringExtra("snapshot_title");
                            loadUrl(currentTab2, str2);
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

    public void onBackKey() {
        if (this.mUi.onBackKey()) {
            return;
        }
        WebView currentSubWindow = this.mTabControl.getCurrentSubWindow();
        if (currentSubWindow == null) {
            goBackOnePageOrQuit();
        } else if (currentSubWindow.canGoBack()) {
            currentSubWindow.goBack();
        } else {
            dismissSubWindow(this.mTabControl.getCurrentTab());
        }
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

    @Override // com.android.browser.ActivityController
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (menuItem.getGroupId() == 2131558642) {
            return false;
        }
        int itemId = menuItem.getItemId();
        boolean z = true;
        switch (itemId) {
            case 2131558433:
            case 2131558627:
            case 2131558628:
            case 2131558629:
                WebView currentTopWebView = getCurrentTopWebView();
                if (currentTopWebView != null) {
                    HashMap hashMap = new HashMap();
                    hashMap.put("webview", currentTopWebView);
                    currentTopWebView.requestFocusNodeHref(this.mHandler.obtainMessage(102, itemId, 0, hashMap));
                    break;
                } else {
                    z = false;
                    break;
                }
            default:
                z = onOptionsItemSelected(menuItem);
                break;
        }
        return z;
    }

    @Override // com.android.browser.ActivityController
    public void onContextMenuClosed(Menu menu) {
        this.mUi.onContextMenuClosed(menu, isInLoad());
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:95:0x0406, code lost:
        if (r0 != 7) goto L87;
     */
    @Override // com.android.browser.ActivityController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        WebView webView;
        WebView.HitTestResult hitTestResult;
        if ((view instanceof TitleBar) || !(view instanceof WebView) || (hitTestResult = (webView = (WebView) view).getHitTestResult()) == null) {
            return;
        }
        int type = hitTestResult.getType();
        if (type == 0) {
            Log.w("Controller", "We should not show context menu when nothing is touched");
        } else if (type == 9) {
        } else {
            this.mActivity.getMenuInflater().inflate(2131755011, contextMenu);
            String extra = hitTestResult.getExtra();
            Log.d("browser/Controller", "sitenavigation onCreateContextMenu imageAnchorUrlExtra is : " + hitTestResult.getImageAnchorUrlExtra());
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService("phone");
            boolean z = false;
            if (telephonyManager != null) {
                z = telephonyManager.isVoiceCapable();
            }
            contextMenu.setGroupVisible(2131558636, false);
            contextMenu.setGroupVisible(2131558639, false);
            if (z) {
                contextMenu.setGroupVisible(2131558612, type == 2);
                contextMenu.setGroupVisible(2131558616, false);
            } else {
                contextMenu.setGroupVisible(2131558612, false);
                contextMenu.setGroupVisible(2131558616, type == 2);
            }
            contextMenu.setGroupVisible(2131558619, type == 4);
            contextMenu.setGroupVisible(2131558622, type == 3);
            contextMenu.setGroupVisible(2131558630, type != 5 ? type == 8 : true);
            contextMenu.setGroupVisible(2131558625, type != 7 ? type == 8 : true);
            contextMenu.setGroupVisible(2131558634, false);
            switch (type) {
                case 2:
                    if (Uri.decode(extra).length() <= 128) {
                        contextMenu.setHeaderTitle(Uri.decode(extra));
                    } else {
                        contextMenu.setHeaderTitle(Uri.decode(extra).substring(0, 128));
                    }
                    contextMenu.findItem(2131558613).setIntent(new Intent("android.intent.action.VIEW", Uri.parse("tel:" + extra)));
                    Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
                    intent.putExtra("phone", Uri.decode(extra));
                    intent.setType("vnd.android.cursor.item/contact");
                    if (!z) {
                        contextMenu.findItem(2131558617).setIntent(intent);
                        contextMenu.findItem(2131558618).setOnMenuItemClickListener(new Copy(this, extra));
                        break;
                    } else {
                        contextMenu.findItem(2131558614).setIntent(intent);
                        contextMenu.findItem(2131558615).setOnMenuItemClickListener(new Copy(this, extra));
                        break;
                    }
                case 3:
                    if (extra.length() <= 128) {
                        contextMenu.setHeaderTitle(extra);
                    } else {
                        contextMenu.setHeaderTitle(extra.substring(0, 128));
                    }
                    contextMenu.findItem(2131558623).setIntent(new Intent("android.intent.action.VIEW", Uri.parse("geo:0,0?q=" + URLEncoder.encode(extra))));
                    contextMenu.findItem(2131558624).setOnMenuItemClickListener(new Copy(this, extra));
                    break;
                case 4:
                    if (extra.length() <= 128) {
                        contextMenu.setHeaderTitle(extra);
                    } else {
                        contextMenu.setHeaderTitle(extra.substring(0, 128));
                    }
                    contextMenu.findItem(2131558620).setIntent(new Intent("android.intent.action.VIEW", Uri.parse("mailto:" + extra)));
                    contextMenu.findItem(2131558621).setOnMenuItemClickListener(new Copy(this, extra));
                    break;
                case 5:
                    MenuItem findItem = contextMenu.findItem(2131558566);
                    findItem.setVisible(type == 5);
                    if (type == 5) {
                        if (extra.length() <= 128) {
                            contextMenu.setHeaderTitle(extra);
                        } else {
                            contextMenu.setHeaderTitle(extra.substring(0, 128));
                        }
                        findItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(this, extra) { // from class: com.android.browser.Controller.13
                            final Controller this$0;
                            final String val$extra;

                            {
                                this.this$0 = this;
                                this.val$extra = extra;
                            }

                            @Override // android.view.MenuItem.OnMenuItemClickListener
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Controller.sharePage(this.this$0.mActivity, null, this.val$extra, null, null);
                                return true;
                            }
                        });
                    }
                    contextMenu.findItem(2131558632).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(this, extra) { // from class: com.android.browser.Controller.14
                        final Controller this$0;
                        final String val$extra;

                        {
                            this.this$0 = this;
                            this.val$extra = extra;
                        }

                        @Override // android.view.MenuItem.OnMenuItemClickListener
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (Controller.isImageViewableUri(Uri.parse(this.val$extra))) {
                                this.this$0.openTab(this.val$extra, this.this$0.mTabControl.getCurrentTab(), true, true);
                                return false;
                            }
                            Log.e("Controller", "Refusing to view image with invalid URI, \"" + this.val$extra + "\"");
                            return false;
                        }
                    });
                    contextMenu.findItem(2131558631).setOnMenuItemClickListener(new Download(this.mActivity, extra, webView.isPrivateBrowsingEnabled(), webView.getSettings().getUserAgentString()));
                    this.mWallpaperHandler = new WallpaperHandler(this.mActivity, extra);
                    contextMenu.findItem(2131558633).setOnMenuItemClickListener(this.mWallpaperHandler);
                    break;
                case 6:
                default:
                    Log.w("Controller", "We should not get here.");
                    break;
                case 7:
                case 8:
                    if (extra != null && extra.startsWith("rtsp://")) {
                        contextMenu.findItem(2131558627).setVisible(false);
                    }
                    if (extra.length() <= 128) {
                        contextMenu.setHeaderTitle(extra);
                    } else {
                        contextMenu.setHeaderTitle(extra.substring(0, 128));
                    }
                    boolean canCreateNewTab = this.mTabControl.canCreateNewTab();
                    MenuItem findItem2 = contextMenu.findItem(2131558626);
                    findItem2.setTitle(getSettings().openInBackground() ? 2131493038 : 2131493037);
                    findItem2.setVisible(canCreateNewTab);
                    if (canCreateNewTab) {
                        if (8 == type) {
                            findItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(this, webView) { // from class: com.android.browser.Controller.11
                                final Controller this$0;
                                final WebView val$webview;

                                {
                                    this.this$0 = this;
                                    this.val$webview = webView;
                                }

                                @Override // android.view.MenuItem.OnMenuItemClickListener
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    HashMap hashMap = new HashMap();
                                    hashMap.put("webview", this.val$webview);
                                    this.val$webview.requestFocusNodeHref(this.this$0.mHandler.obtainMessage(102, 2131558626, 0, hashMap));
                                    return true;
                                }
                            });
                        } else {
                            findItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(this, extra) { // from class: com.android.browser.Controller.12
                                final Controller this$0;
                                final String val$extra;

                                {
                                    this.this$0 = this;
                                    this.val$extra = extra;
                                }

                                @Override // android.view.MenuItem.OnMenuItemClickListener
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    if (this.val$extra != null && this.val$extra.startsWith("rtsp://")) {
                                        Intent intent2 = new Intent();
                                        intent2.setAction("android.intent.action.VIEW");
                                        intent2.setData(Uri.parse(this.val$extra.replaceAll(" ", "%20")));
                                        intent2.addFlags(268435456);
                                        this.this$0.mActivity.startActivity(intent2);
                                        return true;
                                    } else if (this.val$extra != null && this.val$extra.startsWith("wtai://wp/mc;")) {
                                        this.this$0.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("tel:" + this.val$extra.replaceAll(" ", "%20").substring("wtai://wp/mc;".length()))));
                                        return true;
                                    } else {
                                        this.this$0.openTab(this.val$extra, this.this$0.mTabControl.getCurrentTab(), !this.this$0.mSettings.openInBackground(), true);
                                        return true;
                                    }
                                }
                            });
                        }
                    }
                    break;
            }
            this.mUi.onContextMenuCreated(contextMenu);
        }
    }

    @Override // com.android.browser.ActivityController
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.mMenuState == -1) {
            return false;
        }
        this.mActivity.getMenuInflater().inflate(2131755010, menu);
        return true;
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

    /* JADX WARN: Code restructure failed: missing block: B:24:0x018b, code lost:
        if (r0.getClassName().equals(r0.activityInfo.name) == false) goto L24;
     */
    @Override // com.android.browser.WebViewController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onDownloadStart(Tab tab, String str, String str2, String str3, String str4, String str5, long j) {
        String str6;
        WebView webView = tab.getWebView();
        Log.d("browser/Controller", "onDownloadStart: dispos=" + (str3 == null ? "null" : str3));
        if (str3 == null || !str3.regionMatches(true, 0, "attachment", 0, 10)) {
            Intent intent = new Intent("android.intent.action.VIEW");
            if (str.startsWith("http://vod02.v.vnet.mobi/mobi/vod/st02")) {
                str4 = "video/3gp";
            }
            intent.setDataAndType(Uri.parse(str), str4);
            intent.addFlags(268435456);
            ResolveInfo resolveActivity = this.mActivity.getPackageManager().resolveActivity(intent, 65536);
            Log.d("browser/Controller", "onDownloadStart: ResolveInfo=" + (resolveActivity == null ? "null" : resolveActivity));
            str6 = str4;
            if (resolveActivity != null) {
                ComponentName componentName = this.mActivity.getComponentName();
                Log.d("browser/Controller", "onDownloadStart: myName=" + componentName + ", myName.packageName=" + componentName.getPackageName() + ", info.packageName=" + resolveActivity.activityInfo.packageName + ", myName.name=" + componentName.getClassName() + ", info.name=" + resolveActivity.activityInfo.name);
                if (componentName.getPackageName().equals(resolveActivity.activityInfo.packageName)) {
                    str6 = str4;
                }
                Log.d("browser/Controller", "onDownloadStart: mimetype=" + str4);
                if (str4.equalsIgnoreCase("application/x-mpegurl") || str4.equalsIgnoreCase("application/vnd.apple.mpegurl")) {
                    this.mActivity.startActivity(intent);
                    if (webView == null || webView.copyBackForwardList().getSize() != 0) {
                        return;
                    }
                    if (tab == this.mTabControl.getCurrentTab()) {
                        goBackOnePageOrQuit();
                        return;
                    } else {
                        closeTab(tab);
                        return;
                    }
                }
                try {
                    Activity activity = this.mActivity;
                    TabControl tabControl = this.mTabControl;
                    new AlertDialog.Builder(activity).setTitle(2131492945).setIcon(17301659).setMessage(2131492892).setPositiveButton(2131492893, new DialogInterface.OnClickListener(this, activity, str, str2, str3, str4, j, tab, tabControl) { // from class: com.android.browser.Controller.7
                        final Controller this$0;
                        final Activity val$activity;
                        final String val$downloadContentDisposition;
                        final long val$downloadContentLength;
                        final String val$downloadMimetype;
                        final Tab val$downloadTab;
                        final TabControl val$downloadTabControl;
                        final String val$downloadUrl;
                        final String val$downloadUserAgent;

                        {
                            this.this$0 = this;
                            this.val$activity = activity;
                            this.val$downloadUrl = str;
                            this.val$downloadUserAgent = str2;
                            this.val$downloadContentDisposition = str3;
                            this.val$downloadMimetype = str4;
                            this.val$downloadContentLength = j;
                            this.val$downloadTab = tab;
                            this.val$downloadTabControl = tabControl;
                        }

                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DownloadHandler.onDownloadStartNoStream(this.val$activity, this.val$downloadUrl, this.val$downloadUserAgent, this.val$downloadContentDisposition, this.val$downloadMimetype, null, false, this.val$downloadContentLength);
                            Log.d("browser/Controller", "User decide to download the content");
                            WebView webView2 = this.val$downloadTab.getWebView();
                            if (webView2 == null || webView2.copyBackForwardList().getSize() != 0) {
                                return;
                            }
                            if (this.val$downloadTab == this.val$downloadTabControl.getCurrentTab()) {
                                this.this$0.goBackOnePageOrQuit();
                            } else {
                                this.this$0.closeTab(this.val$downloadTab);
                            }
                        }
                    }).setNegativeButton(2131492894, new DialogInterface.OnClickListener(this, str, intent, activity, tab, tabControl) { // from class: com.android.browser.Controller.8
                        final Controller this$0;
                        final Activity val$activity;
                        final Intent val$downloadIntent;
                        final Tab val$downloadTab;
                        final TabControl val$downloadTabControl;
                        final String val$downloadUrl;

                        {
                            this.this$0 = this;
                            this.val$downloadUrl = str;
                            this.val$downloadIntent = intent;
                            this.val$activity = activity;
                            this.val$downloadTab = tab;
                            this.val$downloadTabControl = tabControl;
                        }

                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (this.val$downloadUrl != null) {
                                String cookie = CookieManager.getInstance().getCookie(this.val$downloadUrl);
                                Log.i("browser/Controller", "url: " + this.val$downloadUrl + " url cookie: " + cookie);
                                if (cookie != null) {
                                    this.val$downloadIntent.putExtra("url-cookie", cookie);
                                }
                            }
                            this.val$activity.startActivity(this.val$downloadIntent);
                            Log.d("browser/Controller", "User decide to open the content by startActivity");
                            WebView webView2 = this.val$downloadTab.getWebView();
                            if (webView2 == null || webView2.copyBackForwardList().getSize() != 0) {
                                return;
                            }
                            if (this.val$downloadTab == this.val$downloadTabControl.getCurrentTab()) {
                                this.this$0.goBackOnePageOrQuit();
                            } else {
                                this.this$0.closeTab(this.val$downloadTab);
                            }
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener(this) { // from class: com.android.browser.Controller.9
                        final Controller this$0;

                        {
                            this.this$0 = this;
                        }

                        @Override // android.content.DialogInterface.OnCancelListener
                        public void onCancel(DialogInterface dialogInterface) {
                            Log.d("browser/Controller", "User cancel the download action");
                        }
                    }).show();
                    return;
                } catch (ActivityNotFoundException e) {
                    Log.d("Controller", "activity not found for " + str4 + " over " + Uri.parse(str).getScheme(), e);
                    str6 = str4;
                }
            }
        } else {
            str6 = str4;
        }
        Log.d("browser/Controller", "onDownloadStart: download directly, mimetype=" + str6 + ", url=" + str);
        downloadStart(this.mActivity, str, str2, str3, str6, str5, false, j, webView, tab);
    }

    @Override // com.android.browser.WebViewController
    public void onFavicon(Tab tab, WebView webView, Bitmap bitmap) {
        this.mUi.onTabDataChanged(tab);
        maybeUpdateFavicon(tab, webView.getOriginalUrl(), webView.getUrl(), bitmap);
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
                    if (keyEvent.isShiftPressed()) {
                        openIncognitoTab();
                        return true;
                    }
                    openTab("about:blank", false, true, false);
                    return true;
                }
                break;
            case 61:
                if (keyEvent.isCtrlPressed()) {
                    if (keyEvent.isShiftPressed()) {
                        switchToTab(getPrevTab());
                        return true;
                    }
                    switchToTab(getNextTab());
                    return true;
                }
                break;
            case 62:
                if (hasModifiers2) {
                    pageUp();
                    return true;
                } else if (hasNoModifiers) {
                    pageDown();
                    return true;
                } else {
                    return true;
                }
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
        switch (i) {
            case 4:
                if (this.mUi.isWebShowing()) {
                    bookmarksOrHistoryPicker(UI.ComboViews.History);
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override // com.android.browser.ActivityController
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (isMenuOrCtrlKey(i)) {
            this.mMenuIsDown = false;
            if (82 == i && keyEvent.isTracking() && !keyEvent.isCanceled()) {
                return onMenuKey();
            }
        }
        if (keyEvent.hasNoModifiers()) {
            switch (i) {
                case 4:
                    if (!keyEvent.isTracking() || keyEvent.isCanceled()) {
                        return false;
                    }
                    onBackKey();
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override // com.android.browser.ActivityController
    public void onLowMemory() {
        this.mTabControl.freeMemory();
    }

    protected boolean onMenuKey() {
        return this.mUi.onMenuKey();
    }

    @Override // com.android.browser.ActivityController
    public boolean onMenuOpened(int i, Menu menu) {
        if (!this.mOptionsMenuOpen) {
            this.mOptionsMenuOpen = true;
            this.mConfigChanged = false;
            this.mExtendedMenuOpen = false;
            this.mUi.onOptionsMenuOpened();
            return true;
        } else if (this.mConfigChanged) {
            this.mConfigChanged = false;
            return true;
        } else if (this.mExtendedMenuOpen) {
            this.mExtendedMenuOpen = false;
            this.mUi.onExtendedMenuClosed(isInLoad());
            return true;
        } else {
            this.mExtendedMenuOpen = true;
            this.mUi.onExtendedMenuOpened();
            return true;
        }
    }

    @Override // com.android.browser.UiController, com.android.browser.ActivityController
    public boolean onOptionsItemSelected(MenuItem menuItem) {
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
            case 2131558573:
                if (getCurrentTopWebView() != null) {
                    getCurrentTopWebView().reload();
                    return true;
                }
                return true;
            case 2131558574:
                getCurrentTab().goForward();
                return true;
            case 2131558575:
                stopLoading();
                return true;
            case 2131558576:
            case 2131558598:
                loadUrl(this.mTabControl.getCurrentTab(), this.mSettings.getHomePage());
                return true;
            case 2131558577:
                bookmarkCurrentPage();
                return true;
            case 2131558578:
                showCloseSelectionDialog();
                return true;
            case 2131558579:
            case 2131558588:
            case 2131558590:
            case 2131558595:
            case 2131558596:
            default:
                return false;
            case 2131558580:
                Tab currentTab = getTabControl().getCurrentTab();
                if (currentTab != null && checkStorageState() && createSavePageFolder()) {
                    createSavePageNotification();
                    WebView webView = currentTab.getWebView();
                    webView.setSavePageClient(new BrowserSavePageClient(this, currentTab));
                    if (webView.savePage()) {
                        return true;
                    }
                    Log.d("browser/SavePage", "webview.savePage() return false.");
                    Toast.makeText(this.mActivity, 2131492919, 1).show();
                    return true;
                }
                return true;
            case 2131558581:
                Tab currentTab2 = this.mTabControl.getCurrentTab();
                if (currentTab2 == null) {
                    return false;
                }
                shareCurrentPage(currentTab2);
                return true;
            case 2131558582:
                findOnPage();
                return true;
            case 2131558583:
                toggleUserAgent();
                return true;
            case 2131558584:
                bookmarksOrHistoryPicker(UI.ComboViews.Bookmarks);
                return true;
            case 2131558585:
                openTab("about:blank", false, true, false);
                return true;
            case 2131558586:
                showPageInfo();
                return true;
            case 2131558587:
                openPreferences();
                return true;
            case 2131558589:
                goLive();
                return true;
            case 2131558591:
                closeOtherTabs();
                return true;
            case 2131558592:
                bookmarksOrHistoryPicker(UI.ComboViews.History);
                return true;
            case 2131558593:
                bookmarksOrHistoryPicker(UI.ComboViews.Snapshots);
                return true;
            case 2131558594:
                getCurrentTopWebView().debugDump();
                return true;
            case 2131558597:
                viewDownloads();
                return true;
            case 2131558599:
                getCurrentTopWebView().zoomIn();
                return true;
            case 2131558600:
                getCurrentTopWebView().zoomOut();
                return true;
            case 2131558601:
            case 2131558602:
            case 2131558603:
            case 2131558604:
            case 2131558605:
            case 2131558606:
            case 2131558607:
            case 2131558608:
                int itemId = menuItem.getItemId();
                for (int i = 0; i < WINDOW_SHORTCUT_ID_ARRAY.length; i++) {
                    if (WINDOW_SHORTCUT_ID_ARRAY[i] == itemId) {
                        Tab tab = this.mTabControl.getTab(i);
                        if (tab == null || tab == this.mTabControl.getCurrentTab()) {
                            return true;
                        }
                        switchToTab(tab);
                        return true;
                    }
                }
                return true;
            case 2131558609:
                getCurrentTab().goBack();
                return true;
            case 2131558610:
                editUrl();
                return true;
            case 2131558611:
                if (this.mTabControl.getCurrentSubWindow() != null) {
                    dismissSubWindow(this.mTabControl.getCurrentTab());
                    return true;
                }
                closeCurrentTab();
                return true;
        }
    }

    @Override // com.android.browser.ActivityController
    public void onOptionsMenuClosed(Menu menu) {
        this.mOptionsMenuOpen = false;
        this.mUi.onOptionsMenuClosed(isInLoad());
    }

    @Override // com.android.browser.WebViewController
    public void onPageFinished(Tab tab) {
        if (!this.mDelayRemoveLastTab) {
            Log.i("Controller", "onPageFinished backupState " + tab.getUrl());
            this.mCrashRecoveryHandler.backupState();
        }
        this.mUi.onTabDataChanged(tab);
        if (this.mActivityPaused && pauseWebViewTimers(tab)) {
            releaseWakeLock();
        }
        if (tab.getWebView() != null && tab.inForeground()) {
            this.mUi.updateBottomBarState(!tab.getWebView().canScrollVertically(-1) ? tab.getWebView().canScrollVertically(1) : true, tab.canGoBack() || tab.getParent() != null, tab.canGoForward());
        }
        Performance.tracePageFinished();
        Performance.dumpSystemMemInfo(this.mActivity);
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(getTabControl().getTabPosition(tab)));
        new CheckMemoryTask(this.mHandler).execute(Integer.valueOf(getTabControl().getVisibleWebviewNums()), arrayList, true, tab.getUrl(), null, false);
    }

    @Override // com.android.browser.WebViewController
    public void onPageStarted(Tab tab, WebView webView, Bitmap bitmap) {
        boolean z = false;
        this.mHandler.removeMessages(108, tab);
        CookieSyncManager.getInstance().resetSync();
        this.mBrowserMiscExt = Extensions.getMiscPlugin(this.mActivity);
        this.mBrowserMiscExt.processNetworkNotify(webView, this.mActivity, this.mNetworkHandler.isNetworkUp());
        if (this.mActivityPaused) {
            resumeWebViewTimers(tab);
        }
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getCurrentTab() != null) {
            updateShareMenuItems(menu, getCurrentTab());
        }
        this.mCachedMenu = menu;
        switch (this.mMenuState) {
            case -1:
                if (this.mCurrentMenuState != this.mMenuState) {
                    menu.setGroupVisible(2131558571, false);
                    menu.setGroupEnabled(2131558571, false);
                    menu.setGroupEnabled(2131558596, false);
                    break;
                }
                break;
            default:
                if (this.mCurrentMenuState != this.mMenuState) {
                    menu.setGroupVisible(2131558571, true);
                    menu.setGroupEnabled(2131558571, true);
                    menu.setGroupEnabled(2131558596, true);
                }
                updateMenuState(getCurrentTab(), menu);
                break;
        }
        this.mCurrentMenuState = this.mMenuState;
        return this.mUi.onPrepareOptionsMenu(menu);
    }

    @Override // com.android.browser.WebViewController
    public void onProgressChanged(Tab tab) {
        int loadProgress = tab.getLoadProgress();
        Log.i("Controller", "onProgressChanged url: " + tab.getUrl() + " : " + loadProgress + "%");
        if (loadProgress == 100) {
            CookieSyncManager.getInstance().sync();
            if (!tab.isPrivateBrowsingEnabled() && !TextUtils.isEmpty(tab.getUrl()) && !tab.isSnapshot() && tab.shouldUpdateThumbnail() && (((tab.inForeground() && !didUserStopLoading()) || !tab.inForeground()) && !this.mHandler.hasMessages(108, tab))) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(108, 0, 0, tab), 500L);
            }
            if (tab.getWebView() != null && tab.inForeground()) {
                boolean canScrollVertically = !tab.getWebView().canScrollVertically(-1) ? tab.getWebView().canScrollVertically(1) : true;
                UI ui = this.mUi;
                boolean z = true;
                if (!tab.canGoBack()) {
                    z = tab.getParent() != null;
                }
                ui.updateBottomBarState(canScrollVertically, z, tab.canGoForward());
            }
        }
        this.mUi.onProgressChanged(tab);
    }

    @Override // com.android.browser.WebViewController
    public void onReceivedHttpAuthRequest(Tab tab, WebView webView, HttpAuthHandler httpAuthHandler, String str, String str2) {
        String str3 = null;
        String str4 = null;
        if (httpAuthHandler.useHttpAuthUsernamePassword()) {
            str3 = null;
            str4 = null;
            if (webView != null) {
                String[] httpAuthUsernamePassword = webView.getHttpAuthUsernamePassword(str, str2);
                str3 = null;
                str4 = null;
                if (httpAuthUsernamePassword != null) {
                    str3 = null;
                    str4 = null;
                    if (httpAuthUsernamePassword.length == 2) {
                        str4 = httpAuthUsernamePassword[0];
                        str3 = httpAuthUsernamePassword[1];
                    }
                }
            }
        }
        if (str4 != null && str3 != null) {
            httpAuthHandler.proceed(str4, str3);
        } else if (!tab.inForeground() || httpAuthHandler.suppressDialog()) {
            httpAuthHandler.cancel();
        } else {
            this.mPageDialogsHandler.showHttpAuthentication(tab, httpAuthHandler, str, str2);
        }
    }

    @Override // com.android.browser.WebViewController
    public void onReceivedTitle(Tab tab, String str) {
        this.mUi.onTabDataChanged(tab);
        String originalUrl = tab.getOriginalUrl();
        if (TextUtils.isEmpty(originalUrl) || originalUrl.length() >= 50000 || tab.isPrivateBrowsingEnabled()) {
            return;
        }
        DataController.getInstance(this.mActivity).updateHistoryTitle(originalUrl, str);
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

    @Override // com.android.browser.ActivityController
    public void onSaveInstanceState(Bundle bundle) {
        this.mCrashRecoveryHandler.writeState(createSaveState());
        this.mSettings.setLastRunPaused(true);
    }

    @Override // com.android.browser.ActivityController
    public boolean onSearchRequested() {
        this.mUi.editUrl(false, true);
        return true;
    }

    @Override // com.android.browser.WebViewController
    public void onSetWebView(Tab tab, WebView webView) {
        this.mUi.onSetWebView(tab, webView);
    }

    @Override // com.android.browser.WebViewController
    public void onShowPopupWindowAttempt(Tab tab, boolean z, Message message) {
        this.mPageDialogsHandler.showPopupWindowAttempt(tab, z, message);
    }

    @Override // com.android.browser.WebViewController
    public boolean onUnhandledKeyEvent(KeyEvent keyEvent) {
        if (isActivityPaused()) {
            return false;
        }
        return keyEvent.getAction() == 0 ? this.mActivity.onKeyDown(keyEvent.getKeyCode(), keyEvent) : this.mActivity.onKeyUp(keyEvent.getKeyCode(), keyEvent);
    }

    @Override // com.android.browser.WebViewController
    public void onUpdatedSecurityState(Tab tab) {
        this.mUi.onTabDataChanged(tab);
    }

    @Override // com.android.browser.WebViewController
    public void onUserCanceledSsl(Tab tab) {
        if (tab.canGoBack()) {
            tab.goBack();
        } else {
            tab.loadUrl(this.mSettings.getHomePage(), null);
        }
    }

    @Override // com.android.browser.UiController
    public Tab openIncognitoTab() {
        return openTab("browser:incognito", true, true, false);
    }

    @Override // com.android.browser.UiController
    public void openPreferences() {
        Intent intent = new Intent(this.mActivity, BrowserPreferencesPage.class);
        intent.putExtra("currentPage", getCurrentTopWebView().getUrl());
        this.mActivity.startActivityForResult(intent, 3);
    }

    public Tab openTab(IntentHandler.UrlData urlData) {
        Tab showPreloadedTab = showPreloadedTab(urlData);
        Tab tab = showPreloadedTab;
        if (showPreloadedTab == null) {
            Tab createNewTab = createNewTab(false, true, true);
            tab = createNewTab;
            if (createNewTab != null) {
                if (urlData.isEmpty()) {
                    tab = createNewTab;
                } else {
                    loadUrlDataIn(createNewTab, urlData);
                    tab = createNewTab;
                }
            }
        }
        return tab;
    }

    @Override // com.android.browser.WebViewController
    public Tab openTab(String str, Tab tab, boolean z, boolean z2) {
        return openTab(str, tab != null ? tab.isPrivateBrowsingEnabled() : false, z, z2, tab);
    }

    @Override // com.android.browser.UiController
    public Tab openTab(String str, boolean z, boolean z2, boolean z3) {
        return openTab(str, z, z2, z3, null);
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

    @Override // com.android.browser.UiController
    public Tab openTabToHomePage() {
        if (DEBUG) {
            Log.d("browser", "Controller.openTabToHomePage()--->");
        }
        return openTab(this.mSettings.getHomePage(), false, true, false);
    }

    protected void pageDown() {
        getCurrentTopWebView().pageDown(false);
    }

    protected void pageUp() {
        getCurrentTopWebView().pageUp(false);
    }

    @Override // com.android.browser.UiController
    public void removeSubWindow(Tab tab) {
        if (tab.getSubWebView() != null) {
            this.mUi.removeSubWindow(tab.getSubViewContainer());
        }
    }

    protected void removeTab(Tab tab) {
        this.mUi.removeTab(tab);
        this.mTabControl.removeTab(tab);
        this.mCrashRecoveryHandler.backupState();
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
    public void sendErrorCode(int i, String str) {
        Intent intent = new Intent("com.android.browser.action.SEND_ERROR");
        intent.putExtra("com.android.browser.error_code_key", i);
        intent.putExtra("com.android.browser.url_key", str);
        intent.putExtra("com.android.browser.homepage_key", this.mSettings.getHomePage());
        this.mActivity.sendBroadcast(intent);
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

    @Override // com.android.browser.UiController
    public void setBlockEvents(boolean z) {
        this.mBlockEvents = z;
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setUi(UI ui) {
        this.mUi = ui;
    }

    @Override // com.android.browser.UiController
    public void shareCurrentPage() {
        shareCurrentPage(this.mTabControl.getCurrentTab());
    }

    @Override // com.android.browser.WebViewController
    public boolean shouldCaptureThumbnails() {
        return this.mUi.shouldCaptureThumbnails();
    }

    @Override // com.android.browser.WebViewController
    public boolean shouldOverrideKeyEvent(KeyEvent keyEvent) {
        if (this.mMenuIsDown) {
            return this.mActivity.getWindow().isShortcutKey(keyEvent.getKeyCode(), keyEvent);
        }
        return false;
    }

    @Override // com.android.browser.WebViewController
    public boolean shouldOverrideUrlLoading(Tab tab, WebView webView, String str) {
        boolean shouldOverrideUrlLoading = this.mUrlHandler.shouldOverrideUrlLoading(tab, webView, str);
        if (tab.inForeground()) {
            this.mUi.updateBottomBarState(true, tab.canGoBack(), tab.canGoForward());
        }
        return shouldOverrideUrlLoading;
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
    public boolean shouldShowErrorConsole() {
        return this.mShouldShowErrorConsole;
    }

    @Override // com.android.browser.WebViewController
    public void showAutoLogin(Tab tab) {
        if (!f2assertionsDisabled && !tab.inForeground()) {
            throw new AssertionError();
        }
        this.mUi.showAutoLogin(tab);
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

    @Override // com.android.browser.WebViewController
    public void showFileChooser(ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        this.mUploadHandler = new UploadHandler(this);
        this.mUploadHandler.openFileChooser(valueCallback, fileChooserParams);
    }

    @Override // com.android.browser.UiController
    public void showPageInfo() {
        this.mPageDialogsHandler.showPageInfo(this.mTabControl.getCurrentTab(), false, null);
    }

    @Override // com.android.browser.WebViewController
    public void showSslCertificateOnError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        this.mPageDialogsHandler.showSSLCertificateOnError(webView, sslErrorHandler, sslError);
    }

    @Override // com.android.browser.ActivityController
    public void start(Intent intent) {
        this.mCrashRecoveryHandler.startRecovery(intent);
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

    @Override // com.android.browser.UiController
    public boolean supportsVoice() {
        boolean z = false;
        if (this.mActivity.getPackageManager().queryIntentActivities(new Intent("android.speech.action.RECOGNIZE_SPEECH"), 0).size() != 0) {
            z = true;
        }
        return z;
    }

    @Override // com.android.browser.WebViewController, com.android.browser.UiController
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
    public void toggleUserAgent() {
        WebView currentWebView = getCurrentWebView();
        this.mSettings.toggleDesktopUseragent(currentWebView);
        currentWebView.loadUrl(currentWebView.getOriginalUrl());
    }

    @Override // com.android.browser.UiController
    public void updateMenuState(Tab tab, Menu menu) {
        boolean z;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        boolean z6 = false;
        if (tab != null) {
            z2 = tab.canGoBack();
            z3 = tab.canGoForward();
            z4 = this.mSettings.getHomePage().equals(tab.getUrl());
            z5 = this.mSettings.hasDesktopUseragent(tab.getWebView());
            z6 = !tab.isSnapshot();
        }
        menu.findItem(2131558609).setEnabled(z2);
        menu.findItem(2131558598).setEnabled(!z4);
        MenuItem findItem = menu.findItem(2131558574);
        findItem.setEnabled(z3);
        menu.findItem(2131558575).setEnabled(isInLoad());
        menu.setGroupVisible(2131558572, z6);
        if (BrowserSettings.getInstance().useFullscreen() || BrowserSettings.getInstance().useQuickControls()) {
            findItem.setVisible(true);
            findItem.setEnabled(z3);
        } else {
            findItem.setVisible(false);
        }
        PackageManager packageManager = this.mActivity.getPackageManager();
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        menu.findItem(2131558581).setVisible(packageManager.resolveActivity(intent, 65536) != null);
        boolean enableNavDump = this.mSettings.enableNavDump();
        MenuItem findItem2 = menu.findItem(2131558594);
        findItem2.setVisible(enableNavDump);
        findItem2.setEnabled(enableNavDump);
        this.mSettings.isDebugEnabled();
        MenuItem findItem3 = menu.findItem(2131558583);
        findItem3.setChecked(z5);
        findItem3.setEnabled(true);
        menu.setGroupVisible(2131558579, z6);
        menu.setGroupVisible(2131558588, !z6);
        menu.setGroupVisible(2131558590, false);
        if (tab != null) {
            Method[] methods = tab.getWebView().getClass().getMethods();
            int i = 0;
            while (true) {
                z = true;
                if (i >= methods.length) {
                    break;
                } else if (methods[i].getName().equals("setSavePageClient")) {
                    z = false;
                    break;
                } else {
                    i++;
                }
            }
            Log.d("browser/SavePage", "install GMS: " + z);
            menu.findItem(2131558580).setVisible(!z);
            String url = tab.getUrl();
            if (z || !(url.startsWith("about:blank") || url.startsWith("content:") || url.startsWith("file:") || url.length() == 0)) {
                menu.findItem(2131558580).setEnabled(true);
            } else {
                menu.findItem(2131558580).setEnabled(false);
            }
        } else {
            menu.findItem(2131558580).setEnabled(false);
        }
        this.mUi.updateMenuState(tab, menu);
    }

    void viewDownloads() {
        this.mActivity.startActivity(new Intent("android.intent.action.VIEW_DOWNLOADS"));
    }
}
