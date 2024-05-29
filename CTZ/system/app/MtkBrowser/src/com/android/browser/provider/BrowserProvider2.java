package com.android.browser.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.AbstractCursor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.browser.BrowserSettings;
import com.android.browser.Extensions;
import com.android.browser.R;
import com.android.browser.UrlUtils;
import com.android.browser.provider.BrowserContract;
import com.android.browser.provider.BrowserProvider;
import com.android.browser.widget.BookmarkThumbnailWidgetProvider;
import com.android.common.content.SyncStateContentProviderHelper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
/* loaded from: classes.dex */
public class BrowserProvider2 extends SQLiteContentProvider {
    DatabaseHelper mOpenHelper;
    static final Uri LEGACY_AUTHORITY_URI = new Uri.Builder().authority("MtkBrowserProvider").scheme("content").build();
    private static final String[] SUGGEST_PROJECTION = {qualifyColumn("history", "_id"), qualifyColumn("history", "url"), bookmarkOrHistoryColumn("title"), bookmarkOrHistoryLiteral("url", Integer.toString(R.drawable.ic_bookmark_off_holo_dark), Integer.toString(R.drawable.ic_history_holo_dark)), qualifyColumn("history", "date")};
    public static final String[] BOOKMARK_FOLDERS_PROJECTION = {"_id", "parent_id", "folder_level", "name", "date", "visits"};
    static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    static final HashMap<String, String> ACCOUNTS_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> BOOKMARKS_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> OTHER_BOOKMARKS_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> HISTORY_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> SYNC_STATE_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> IMAGES_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> COMBINED_HISTORY_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> COMBINED_BOOKMARK_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> SEARCHES_PROJECTION_MAP = new HashMap<>();
    static final HashMap<String, String> SETTINGS_PROJECTION_MAP = new HashMap<>();
    SyncStateContentProviderHelper mSyncHelper = new SyncStateContentProviderHelper();
    ContentObserver mWidgetObserver = null;
    boolean mUpdateWidgets = false;
    boolean mSyncToNetwork = true;

    /* loaded from: classes.dex */
    public interface OmniboxSuggestions {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "omnibox_suggestions");
    }

    /* loaded from: classes.dex */
    public interface Thumbnails {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "thumbnails");
    }

    static {
        UriMatcher uriMatcher = URI_MATCHER;
        uriMatcher.addURI("com.android.browser.provider", "accounts", 7000);
        uriMatcher.addURI("com.android.browser.provider", "bookmarks", 1000);
        uriMatcher.addURI("com.android.browser.provider", "bookmarks/#", 1001);
        uriMatcher.addURI("com.android.browser.provider", "bookmarks/folder", 1002);
        uriMatcher.addURI("com.android.browser.provider", "bookmarks/folder/#", 1003);
        uriMatcher.addURI("com.android.browser.provider", "bookmarks/folder/id", 1005);
        uriMatcher.addURI("com.android.browser.provider", "search_suggest_query", 1004);
        uriMatcher.addURI("com.android.browser.provider", "bookmarks/search_suggest_query", 1004);
        uriMatcher.addURI("com.android.browser.provider", "history", 2000);
        uriMatcher.addURI("com.android.browser.provider", "history/#", 2001);
        uriMatcher.addURI("com.android.browser.provider", "searches", 3000);
        uriMatcher.addURI("com.android.browser.provider", "searches/#", 3001);
        uriMatcher.addURI("com.android.browser.provider", "syncstate", 4000);
        uriMatcher.addURI("com.android.browser.provider", "syncstate/#", 4001);
        uriMatcher.addURI("com.android.browser.provider", "images", 5000);
        uriMatcher.addURI("com.android.browser.provider", "combined", 6000);
        uriMatcher.addURI("com.android.browser.provider", "combined/#", 6001);
        uriMatcher.addURI("com.android.browser.provider", "settings", 8000);
        uriMatcher.addURI("com.android.browser.provider", "thumbnails", 10);
        uriMatcher.addURI("com.android.browser.provider", "thumbnails/#", 11);
        uriMatcher.addURI("com.android.browser.provider", "omnibox_suggestions", 20);
        URI_MATCHER.addURI("com.android.browser.provider", "homepage", 30);
        uriMatcher.addURI("MtkBrowserProvider", "searches", 3000);
        uriMatcher.addURI("MtkBrowserProvider", "searches/#", 3001);
        uriMatcher.addURI("MtkBrowserProvider", "bookmarks", 9000);
        uriMatcher.addURI("MtkBrowserProvider", "bookmarks/#", 9001);
        uriMatcher.addURI("MtkBrowserProvider", "search_suggest_query", 1004);
        uriMatcher.addURI("MtkBrowserProvider", "bookmarks/search_suggest_query", 1004);
        HashMap<String, String> hashMap = ACCOUNTS_PROJECTION_MAP;
        hashMap.put("account_type", "account_type");
        hashMap.put("account_name", "account_name");
        hashMap.put("root_id", "root_id");
        HashMap<String, String> hashMap2 = BOOKMARKS_PROJECTION_MAP;
        hashMap2.put("_id", qualifyColumn("bookmarks", "_id"));
        hashMap2.put("title", "title");
        hashMap2.put("url", "url");
        hashMap2.put("favicon", "favicon");
        hashMap2.put("thumbnail", "thumbnail");
        hashMap2.put("touch_icon", "touch_icon");
        hashMap2.put("folder", "folder");
        hashMap2.put("parent", "parent");
        hashMap2.put("position", "position");
        hashMap2.put("insert_after", "insert_after");
        hashMap2.put("deleted", "deleted");
        hashMap2.put("account_name", "account_name");
        hashMap2.put("account_type", "account_type");
        hashMap2.put("sourceid", "sourceid");
        hashMap2.put("version", "version");
        hashMap2.put("created", "created");
        hashMap2.put("modified", "modified");
        hashMap2.put("dirty", "dirty");
        hashMap2.put("sync1", "sync1");
        hashMap2.put("sync2", "sync2");
        hashMap2.put("sync3", "sync3");
        hashMap2.put("sync4", "sync4");
        hashMap2.put("sync5", "sync5");
        hashMap2.put("parent_source", "(SELECT sourceid FROM bookmarks A WHERE A._id=bookmarks.parent) AS parent_source");
        hashMap2.put("insert_after_source", "(SELECT sourceid FROM bookmarks A WHERE A._id=bookmarks.insert_after) AS insert_after_source");
        hashMap2.put("type", "CASE  WHEN folder=0 THEN 1 WHEN sync3='bookmark_bar' THEN 3 WHEN sync3='other_bookmarks' THEN 4 ELSE 2 END AS type");
        OTHER_BOOKMARKS_PROJECTION_MAP.putAll(BOOKMARKS_PROJECTION_MAP);
        OTHER_BOOKMARKS_PROJECTION_MAP.put("position", Long.toString(Long.MAX_VALUE) + " AS position");
        HashMap<String, String> hashMap3 = HISTORY_PROJECTION_MAP;
        hashMap3.put("_id", qualifyColumn("history", "_id"));
        hashMap3.put("title", "title");
        hashMap3.put("url", "url");
        hashMap3.put("favicon", "favicon");
        hashMap3.put("thumbnail", "thumbnail");
        hashMap3.put("touch_icon", "touch_icon");
        hashMap3.put("created", "created");
        hashMap3.put("date", "date");
        hashMap3.put("visits", "visits");
        hashMap3.put("user_entered", "user_entered");
        HashMap<String, String> hashMap4 = SYNC_STATE_PROJECTION_MAP;
        hashMap4.put("_id", "_id");
        hashMap4.put("account_name", "account_name");
        hashMap4.put("account_type", "account_type");
        hashMap4.put("data", "data");
        HashMap<String, String> hashMap5 = IMAGES_PROJECTION_MAP;
        hashMap5.put("url_key", "url_key");
        hashMap5.put("favicon", "favicon");
        hashMap5.put("thumbnail", "thumbnail");
        hashMap5.put("touch_icon", "touch_icon");
        HashMap<String, String> hashMap6 = COMBINED_HISTORY_PROJECTION_MAP;
        hashMap6.put("_id", bookmarkOrHistoryColumn("_id"));
        hashMap6.put("title", bookmarkOrHistoryColumn("title"));
        hashMap6.put("url", qualifyColumn("history", "url"));
        hashMap6.put("created", qualifyColumn("history", "created"));
        hashMap6.put("date", "date");
        hashMap6.put("bookmark", "CASE WHEN bookmarks._id IS NOT NULL THEN 1 ELSE 0 END AS bookmark");
        hashMap6.put("visits", "visits");
        hashMap6.put("favicon", "favicon");
        hashMap6.put("thumbnail", "thumbnail");
        hashMap6.put("touch_icon", "touch_icon");
        hashMap6.put("user_entered", "NULL AS user_entered");
        HashMap<String, String> hashMap7 = COMBINED_BOOKMARK_PROJECTION_MAP;
        hashMap7.put("_id", "_id");
        hashMap7.put("title", "title");
        hashMap7.put("url", "url");
        hashMap7.put("created", "created");
        hashMap7.put("date", "NULL AS date");
        hashMap7.put("bookmark", "1 AS bookmark");
        hashMap7.put("visits", "0 AS visits");
        hashMap7.put("favicon", "favicon");
        hashMap7.put("thumbnail", "thumbnail");
        hashMap7.put("touch_icon", "touch_icon");
        hashMap7.put("user_entered", "NULL AS user_entered");
        HashMap<String, String> hashMap8 = SEARCHES_PROJECTION_MAP;
        hashMap8.put("_id", "_id");
        hashMap8.put("search", "search");
        hashMap8.put("date", "date");
        HashMap<String, String> hashMap9 = SETTINGS_PROJECTION_MAP;
        hashMap9.put("key", "key");
        hashMap9.put("value", "value");
    }

    static final String bookmarkOrHistoryColumn(String str) {
        return "CASE WHEN bookmarks." + str + " IS NOT NULL THEN bookmarks." + str + " ELSE history." + str + " END AS " + str;
    }

    static final String bookmarkOrHistoryLiteral(String str, String str2, String str3) {
        return "CASE WHEN bookmarks." + str + " IS NOT NULL THEN \"" + str2 + "\" ELSE \"" + str3 + "\" END";
    }

    static final String qualifyColumn(String str, String str2) {
        return str + "." + str2 + " AS " + str2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, "browser2.db", (SQLiteDatabase.CursorFactory) null, 32);
            setWriteAheadLoggingEnabled(true);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE bookmarks(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT,folder INTEGER NOT NULL DEFAULT 0,parent INTEGER,position INTEGER NOT NULL,insert_after INTEGER,deleted INTEGER NOT NULL DEFAULT 0,account_name TEXT,account_type TEXT,sourceid TEXT,version INTEGER NOT NULL DEFAULT 1,created INTEGER,modified INTEGER,dirty INTEGER NOT NULL DEFAULT 0,sync1 TEXT,sync2 TEXT,sync3 TEXT,sync4 TEXT,sync5 TEXT);");
            sQLiteDatabase.execSQL("CREATE TABLE history(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT NOT NULL,created INTEGER,date INTEGER,visits INTEGER NOT NULL DEFAULT 0,user_entered INTEGER);");
            sQLiteDatabase.execSQL("CREATE TABLE images (url_key TEXT UNIQUE NOT NULL,favicon BLOB,thumbnail BLOB,touch_icon BLOB);");
            sQLiteDatabase.execSQL("CREATE INDEX imagesUrlIndex ON images(url_key)");
            sQLiteDatabase.execSQL("CREATE TABLE searches (_id INTEGER PRIMARY KEY AUTOINCREMENT,search TEXT,date LONG);");
            sQLiteDatabase.execSQL("CREATE TABLE settings (key TEXT PRIMARY KEY,value TEXT NOT NULL);");
            createAccountsView(sQLiteDatabase);
            createThumbnails(sQLiteDatabase);
            BrowserProvider2.this.mSyncHelper.createDatabase(sQLiteDatabase);
            if (!importFromBrowserProvider(sQLiteDatabase)) {
                createDefaultBookmarks(sQLiteDatabase);
            }
            enableSync(sQLiteDatabase);
            createOmniboxSuggestions(sQLiteDatabase);
        }

        void createOmniboxSuggestions(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE VIEW IF NOT EXISTS v_omnibox_suggestions  AS   SELECT _id, url, title, 1 AS bookmark, 0 AS visits, 0 AS date  FROM bookmarks   WHERE deleted = 0 AND folder = 0   UNION ALL   SELECT _id, url, title, 0 AS bookmark, visits, date   FROM history   WHERE url NOT IN (SELECT url FROM bookmarks    WHERE deleted = 0 AND folder = 0)   ORDER BY bookmark DESC, visits DESC, date DESC ");
        }

        void createThumbnails(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS thumbnails (_id INTEGER PRIMARY KEY,thumbnail BLOB NOT NULL);");
        }

        void enableSync(SQLiteDatabase sQLiteDatabase) {
            Account[] accountsByType;
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", "sync_enabled");
            contentValues.put("value", (Integer) 1);
            BrowserProvider2.this.insertSettingsInTransaction(sQLiteDatabase, contentValues);
            AccountManager accountManager = (AccountManager) BrowserProvider2.this.getContext().getSystemService("account");
            if (accountManager == null || (accountsByType = accountManager.getAccountsByType("com.google")) == null || accountsByType.length == 0) {
                return;
            }
            for (Account account : accountsByType) {
                if (ContentResolver.getIsSyncable(account, "com.android.browser.provider") == 0) {
                    ContentResolver.setIsSyncable(account, "com.android.browser.provider", 1);
                    ContentResolver.setSyncAutomatically(account, "com.android.browser.provider", true);
                }
            }
        }

        /* JADX WARN: Removed duplicated region for block: B:55:0x023e  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        boolean importFromBrowserProvider(SQLiteDatabase sQLiteDatabase) {
            Cursor cursor;
            int i;
            Context context = BrowserProvider2.this.getContext();
            File databasePath = context.getDatabasePath("browser.db");
            if (!databasePath.exists()) {
                return false;
            }
            BrowserProvider.DatabaseHelper databaseHelper = new BrowserProvider.DatabaseHelper(context);
            SQLiteDatabase writableDatabase = databaseHelper.getWritableDatabase();
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", (Long) 1L);
                contentValues.put("sync3", "google_chrome_bookmarks");
                contentValues.put("title", "Bookmarks");
                contentValues.putNull("parent");
                contentValues.put("position", (Integer) 0);
                contentValues.put("folder", (Boolean) true);
                contentValues.put("dirty", (Boolean) true);
                sQLiteDatabase.insertOrThrow("bookmarks", null, contentValues);
                String str = BrowserProvider.TABLE_NAMES[2];
                writableDatabase.execSQL("CREATE TABLE IF NOT EXISTS bookmark_folders (_id INTEGER PRIMARY KEY,parent_id INTEGER,folder_level INTEGER,name TEXT,date LONG,visits INTEGER);");
                Cursor query = writableDatabase.query(str, BrowserProvider2.BOOKMARK_FOLDERS_PROJECTION, null, null, null, null, "visits DESC");
                if (query != null) {
                    int i2 = 0;
                    while (query.moveToNext()) {
                        try {
                            i2++;
                            ContentValues contentValues2 = new ContentValues();
                            contentValues2.put("_id", Integer.valueOf(query.getInt(0) + 1));
                            contentValues2.put("title", query.getString(3));
                            contentValues2.put("created", Integer.valueOf(query.getInt(4)));
                            contentValues2.put("position", Integer.valueOf(i2));
                            contentValues2.put("folder", (Boolean) true);
                            contentValues2.put("parent", Integer.valueOf(query.getInt(1) + 1));
                            sQLiteDatabase.insertOrThrow("bookmarks", "dirty", contentValues2);
                        } catch (Throwable th) {
                            th = th;
                            if (cursor != null) {
                            }
                            writableDatabase.close();
                            databaseHelper.close();
                            throw th;
                        }
                    }
                    query.close();
                    i = i2;
                } else {
                    i = 0;
                }
                try {
                    String str2 = BrowserProvider.TABLE_NAMES[0];
                    Cursor cursor2 = query;
                    try {
                        Cursor query2 = writableDatabase.query(str2, new String[]{"url", "title", "favicon", "touch_icon", "created", "folder_id"}, "bookmark!=0", null, null, null, "visits DESC");
                        if (query2 != null) {
                            while (query2.moveToNext()) {
                                String string = query2.getString(0);
                                if (!TextUtils.isEmpty(string)) {
                                    int i3 = i + 1;
                                    ContentValues contentValues3 = new ContentValues();
                                    contentValues3.put("url", string);
                                    contentValues3.put("title", query2.getString(1));
                                    contentValues3.put("created", Integer.valueOf(query2.getInt(4)));
                                    contentValues3.put("position", Integer.valueOf(i3));
                                    contentValues3.put("parent", Integer.valueOf(query2.getInt(5) + 1));
                                    ContentValues contentValues4 = new ContentValues();
                                    contentValues4.put("url_key", string);
                                    contentValues4.put("favicon", query2.getBlob(2));
                                    contentValues4.put("touch_icon", query2.getBlob(3));
                                    sQLiteDatabase.insert("images", "thumbnail", contentValues4);
                                    sQLiteDatabase.insert("bookmarks", "dirty", contentValues3);
                                    i = i3;
                                }
                            }
                            query2.close();
                        }
                        cursor2 = query2;
                        Cursor query3 = writableDatabase.query(str2, new String[]{"url", "title", "visits", "date", "created"}, "visits > 0 OR bookmark = 0", null, null, null, null);
                        if (query3 != null) {
                            while (query3.moveToNext()) {
                                try {
                                    ContentValues contentValues5 = new ContentValues();
                                    String string2 = query3.getString(0);
                                    if (!TextUtils.isEmpty(string2)) {
                                        contentValues5.put("url", string2);
                                        contentValues5.put("title", query3.getString(1));
                                        contentValues5.put("visits", Integer.valueOf(query3.getInt(2)));
                                        contentValues5.put("date", Long.valueOf(query3.getLong(3)));
                                        contentValues5.put("created", Long.valueOf(query3.getLong(4)));
                                        sQLiteDatabase.insert("history", "favicon", contentValues5);
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    cursor = query3;
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    writableDatabase.close();
                                    databaseHelper.close();
                                    throw th;
                                }
                            }
                            query3.close();
                        }
                        writableDatabase.delete(str2, null, null);
                        if (query3 != null) {
                            query3.close();
                        }
                        writableDatabase.close();
                        databaseHelper.close();
                        if (!databasePath.delete()) {
                            databasePath.deleteOnExit();
                        }
                        return true;
                    } catch (Throwable th3) {
                        th = th3;
                        cursor = cursor2;
                    }
                } catch (Throwable th4) {
                    th = th4;
                }
            } catch (Throwable th5) {
                th = th5;
                cursor = null;
            }
        }

        void createAccountsView(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE VIEW IF NOT EXISTS v_accounts AS SELECT NULL AS account_name, NULL AS account_type, 1 AS root_id UNION ALL SELECT account_name, account_type, _id AS root_id FROM bookmarks WHERE sync3 = \"bookmark_bar\" AND deleted = 0");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i < 32) {
                createOmniboxSuggestions(sQLiteDatabase);
            }
            if (i < 31) {
                createThumbnails(sQLiteDatabase);
            }
            if (i < 30) {
                sQLiteDatabase.execSQL("DROP VIEW IF EXISTS v_snapshots_combined");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS snapshots");
            }
            if (i < 28) {
                enableSync(sQLiteDatabase);
            }
            if (i < 27) {
                createAccountsView(sQLiteDatabase);
            }
            if (i < 26) {
                sQLiteDatabase.execSQL("DROP VIEW IF EXISTS combined");
            }
            if (i < 25) {
                Log.v("browser/BrowserProvider", "onUpgrade < 25");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS bookmarks");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS history");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS searches");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS images");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS settings");
                BrowserProvider2.this.mSyncHelper.onAccountsChanged(sQLiteDatabase, new Account[0]);
                onCreate(sQLiteDatabase);
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onOpen(SQLiteDatabase sQLiteDatabase) {
            BrowserProvider2.this.mSyncHelper.onDatabaseOpened(sQLiteDatabase);
        }

        private void createDefaultBookmarks(SQLiteDatabase sQLiteDatabase) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", (Long) 1L);
            contentValues.put("sync3", "google_chrome_bookmarks");
            contentValues.put("title", "Bookmarks");
            contentValues.putNull("parent");
            contentValues.put("position", (Integer) 0);
            contentValues.put("folder", (Boolean) true);
            contentValues.put("dirty", (Boolean) true);
            sQLiteDatabase.insertOrThrow("bookmarks", null, contentValues);
            int addDefaultBookmarksForCustomer = Extensions.getBookmarkPlugin(BrowserProvider2.this.getContext()).addDefaultBookmarksForCustomer(sQLiteDatabase);
            if (addDefaultBookmarksForCustomer == 0) {
                addDefaultBookmarks(sQLiteDatabase, 1L, addDefaultBookmarksForYahoo(sQLiteDatabase, 1L, addDefaultBookmarksForCustomer + (addDefaultBookmarksForCustomer <= 0 ? 2 : 1)));
            }
        }

        public int addDefaultBookmarks(SQLiteDatabase sQLiteDatabase, long j, int i) {
            Resources resources = BrowserProvider2.this.getContext().getResources();
            CharSequence[] textArray = resources.getTextArray(R.array.bookmarks);
            int length = textArray.length;
            return addDefaultBookmarks(sQLiteDatabase, j, textArray, resources.obtainTypedArray(R.array.bookmark_preloads), i);
        }

        public int addDefaultBookmarksForYahoo(SQLiteDatabase sQLiteDatabase, long j, int i) {
            Resources resources = BrowserProvider2.this.getContext().getResources();
            CharSequence[] textArray = resources.getTextArray(R.array.bookmarks_for_yahoo);
            int length = textArray.length;
            return addDefaultBookmarks(sQLiteDatabase, j, textArray, resources.obtainTypedArray(R.array.bookmark_preloads_for_yahoo), i);
        }

        private int addDefaultBookmarks(SQLiteDatabase sQLiteDatabase, long j, CharSequence[] charSequenceArr, TypedArray typedArray, int i) {
            boolean z;
            byte[] bArr;
            Resources resources = BrowserProvider2.this.getContext().getResources();
            int length = charSequenceArr.length;
            try {
                String l = Long.toString(j);
                String l2 = Long.toString(System.currentTimeMillis());
                for (int i2 = 0; i2 < length; i2 += 2) {
                    int i3 = i2 + 1;
                    CharSequence replaceSystemPropertyInString = BrowserProvider2.replaceSystemPropertyInString(BrowserProvider2.this.getContext(), charSequenceArr[i3]);
                    if (!"http://www.google.com/".equals(replaceSystemPropertyInString.toString())) {
                        z = false;
                    } else {
                        z = true;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("INSERT INTO bookmarks (title, url, folder,parent,position,created) VALUES ('");
                    sb.append((Object) charSequenceArr[i2]);
                    sb.append("', '");
                    sb.append((Object) replaceSystemPropertyInString);
                    sb.append("', 0,");
                    sb.append(l);
                    sb.append(",");
                    sb.append(z ? 1 : Integer.toString(i + i2));
                    sb.append(",");
                    sb.append(l2);
                    sb.append(");");
                    sQLiteDatabase.execSQL(sb.toString());
                    int resourceId = typedArray.getResourceId(i2, 0);
                    int resourceId2 = typedArray.getResourceId(i3, 0);
                    byte[] bArr2 = null;
                    try {
                        bArr = readRaw(resources, resourceId2);
                    } catch (IOException e) {
                        bArr = null;
                    }
                    try {
                        bArr2 = readRaw(resources, resourceId);
                    } catch (IOException e2) {
                    }
                    if (bArr != null || bArr2 != null) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("url_key", replaceSystemPropertyInString.toString());
                        if (bArr2 != null) {
                            contentValues.put("favicon", bArr2);
                        }
                        if (bArr != null) {
                            contentValues.put("thumbnail", bArr);
                        }
                        sQLiteDatabase.insert("images", "favicon", contentValues);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e3) {
            } catch (Throwable th) {
                typedArray.recycle();
                throw th;
            }
            typedArray.recycle();
            return length;
        }

        private byte[] readRaw(Resources resources, int i) throws IOException {
            if (i == 0) {
                return null;
            }
            InputStream openRawResource = resources.openRawResource(i);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = openRawResource.read(bArr);
                    if (read > 0) {
                        byteArrayOutputStream.write(bArr, 0, read);
                    } else {
                        byteArrayOutputStream.flush();
                        return byteArrayOutputStream.toByteArray();
                    }
                }
            } finally {
                openRawResource.close();
            }
        }
    }

    private static String getClientId(Context context) {
        String str = "android-google";
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            Cursor query = contentResolver.query(Uri.parse("content://com.google.settings/partner"), new String[]{"value"}, "name='client_id'", null, null);
            if (query != null) {
                try {
                    if (query.moveToNext()) {
                        str = query.getString(0);
                    }
                } catch (RuntimeException e) {
                    cursor = query;
                    if (cursor != null) {
                        cursor.close();
                    }
                    return str;
                } catch (Throwable th) {
                    th = th;
                    cursor = query;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (RuntimeException e2) {
        } catch (Throwable th2) {
            th = th2;
        }
        return str;
    }

    public static CharSequence replaceSystemPropertyInString(Context context, CharSequence charSequence) {
        StringBuffer stringBuffer = new StringBuffer();
        String clientId = getClientId(context);
        int i = 0;
        int i2 = 0;
        while (i < charSequence.length()) {
            if (charSequence.charAt(i) == '{') {
                stringBuffer.append(charSequence.subSequence(i2, i));
                int i3 = i;
                while (true) {
                    if (i3 >= charSequence.length()) {
                        i2 = i;
                        break;
                    } else if (charSequence.charAt(i3) != '}') {
                        i3++;
                    } else {
                        if (charSequence.subSequence(i + 1, i3).toString().equals("CLIENT_ID")) {
                            stringBuffer.append(clientId);
                        } else {
                            stringBuffer.append("unknown");
                        }
                        int i4 = i3;
                        i2 = i3 + 1;
                        i = i4;
                    }
                }
            }
            i++;
        }
        if (charSequence.length() - i2 > 0) {
            stringBuffer.append(charSequence.subSequence(i2, charSequence.length()));
        }
        return stringBuffer;
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    public SQLiteOpenHelper getDatabaseHelper(Context context) {
        DatabaseHelper databaseHelper;
        synchronized (this) {
            if (this.mOpenHelper == null) {
                this.mOpenHelper = new DatabaseHelper(context);
            }
            databaseHelper = this.mOpenHelper;
        }
        return databaseHelper;
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    public boolean isCallerSyncAdapter(Uri uri) {
        return uri.getBooleanQueryParameter("caller_is_syncadapter", false);
    }

    public void setWidgetObserver(ContentObserver contentObserver) {
        this.mWidgetObserver = contentObserver;
    }

    void refreshWidgets() {
        this.mUpdateWidgets = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.browser.provider.SQLiteContentProvider
    public void onEndTransaction(boolean z) {
        super.onEndTransaction(z);
        if (this.mUpdateWidgets) {
            if (this.mWidgetObserver == null) {
                BookmarkThumbnailWidgetProvider.refreshWidgets(getContext());
            } else {
                this.mWidgetObserver.dispatchChange(false);
            }
            this.mUpdateWidgets = false;
        }
        this.mSyncToNetwork = true;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case 1000:
            case 9000:
                return "vnd.android.cursor.dir/bookmark";
            case 1001:
            case 9001:
                return "vnd.android.cursor.item/bookmark";
            case 2000:
                return "vnd.android.cursor.dir/browser-history";
            case 2001:
                return "vnd.android.cursor.item/browser-history";
            case 3000:
                return "vnd.android.cursor.dir/searches";
            case 3001:
                return "vnd.android.cursor.item/searches";
            default:
                return null;
        }
    }

    boolean isNullAccount(String str) {
        if (str == null) {
            return true;
        }
        String trim = str.trim();
        return trim.length() == 0 || trim.equals("null");
    }

    Object[] getSelectionWithAccounts(Uri uri, String str, String[] strArr) {
        boolean z;
        String queryParameter = uri.getQueryParameter("acct_type");
        String queryParameter2 = uri.getQueryParameter("acct_name");
        if (queryParameter != null && queryParameter2 != null) {
            if (!isNullAccount(queryParameter) && !isNullAccount(queryParameter2)) {
                str = DatabaseUtils.concatenateWhere(str, "account_type=? AND account_name=? ");
                strArr = DatabaseUtils.appendSelectionArgs(strArr, new String[]{queryParameter, queryParameter2});
                z = true;
                return new Object[]{str, strArr, Boolean.valueOf(z)};
            }
            str = DatabaseUtils.concatenateWhere(str, "account_name IS NULL AND account_type IS NULL");
        }
        z = false;
        return new Object[]{str, strArr, Boolean.valueOf(z)};
    }

    /* JADX WARN: Removed duplicated region for block: B:44:0x0169  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x016c  */
    @Override // android.content.ContentProvider
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3;
        String[] strArr3;
        String[] strArr4;
        String str4;
        String str5;
        String[] strArr5;
        String str6;
        String buildUnionQuery;
        String[] appendSelectionArgs;
        String[] strArr6;
        String[] strArr7;
        String str7;
        String str8 = str;
        String[] strArr8 = strArr2;
        SQLiteDatabase readableDatabase = this.mOpenHelper.getReadableDatabase();
        int match = URI_MATCHER.match(uri);
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        String queryParameter = uri.getQueryParameter("limit");
        String queryParameter2 = uri.getQueryParameter("groupBy");
        switch (match) {
            case 10:
                str3 = str8;
                strArr3 = strArr8;
                sQLiteQueryBuilder.setTables("thumbnails");
                strArr4 = strArr;
                str4 = str2;
                str5 = str3;
                strArr5 = strArr3;
                break;
            case 11:
                str3 = DatabaseUtils.concatenateWhere(str8, "_id = ?");
                strArr3 = DatabaseUtils.appendSelectionArgs(strArr8, new String[]{Long.toString(ContentUris.parseId(uri))});
                sQLiteQueryBuilder.setTables("thumbnails");
                strArr4 = strArr;
                str4 = str2;
                str5 = str3;
                strArr5 = strArr3;
                break;
            case 20:
                str6 = str8;
                sQLiteQueryBuilder.setTables("v_omnibox_suggestions");
                strArr4 = strArr;
                str4 = str2;
                str5 = str6;
                strArr5 = strArr8;
                break;
            case 30:
                String homePage = getHomePage(getContext());
                if (homePage == null) {
                    return null;
                }
                String[] strArr9 = {homePage};
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{"homepage"}, 1);
                matrixCursor.addRow(strArr9);
                return matrixCursor;
            case 1000:
            case 1001:
            case 1003:
                String str9 = str8;
                if (!uri.getBooleanQueryParameter("show_deleted", false)) {
                    str9 = DatabaseUtils.concatenateWhere("deleted=0", str9);
                }
                if (match == 1001) {
                    str9 = DatabaseUtils.concatenateWhere(str9, "bookmarks._id=?");
                    strArr8 = DatabaseUtils.appendSelectionArgs(strArr8, new String[]{Long.toString(ContentUris.parseId(uri))});
                } else if (match == 1003) {
                    str9 = DatabaseUtils.concatenateWhere(str9, "bookmarks.parent=?");
                    strArr8 = DatabaseUtils.appendSelectionArgs(strArr8, new String[]{Long.toString(ContentUris.parseId(uri))});
                }
                Object[] selectionWithAccounts = getSelectionWithAccounts(uri, str9, strArr8);
                String str10 = (String) selectionWithAccounts[0];
                String[] strArr10 = (String[]) selectionWithAccounts[1];
                String str11 = TextUtils.isEmpty(str2) ? ((Boolean) selectionWithAccounts[2]).booleanValue() ? "position ASC, _id ASC" : "folder DESC, position ASC, _id ASC" : str2;
                sQLiteQueryBuilder.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                sQLiteQueryBuilder.setTables("bookmarks LEFT OUTER JOIN images ON bookmarks.url = images.url_key");
                strArr4 = strArr;
                str4 = str11;
                str5 = str10;
                strArr5 = strArr10;
                break;
            case 1002:
                String queryParameter3 = uri.getQueryParameter("acct_type");
                String queryParameter4 = uri.getQueryParameter("acct_name");
                boolean z = (isNullAccount(queryParameter3) || isNullAccount(queryParameter4)) ? false : true;
                sQLiteQueryBuilder.setTables("bookmarks LEFT OUTER JOIN images ON bookmarks.url = images.url_key");
                String str12 = TextUtils.isEmpty(str2) ? z ? "position ASC, _id ASC" : "folder DESC, position ASC, _id ASC" : str2;
                if (z) {
                    sQLiteQueryBuilder.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                    String buildQuery = sQLiteQueryBuilder.buildQuery(strArr, DatabaseUtils.concatenateWhere("account_type=? AND account_name=? AND parent = (SELECT _id FROM bookmarks WHERE sync3='bookmark_bar' AND account_type = ? AND account_name = ?) AND deleted=0", str8), null, null, null, null);
                    String[] strArr11 = {queryParameter3, queryParameter4, queryParameter3, queryParameter4};
                    if (strArr8 != null) {
                        strArr11 = DatabaseUtils.appendSelectionArgs(strArr11, strArr8);
                    }
                    String concatenateWhere = DatabaseUtils.concatenateWhere("account_type=? AND account_name=? AND sync3=?", str8);
                    sQLiteQueryBuilder.setProjectionMap(OTHER_BOOKMARKS_PROJECTION_MAP);
                    buildUnionQuery = sQLiteQueryBuilder.buildUnionQuery(new String[]{buildQuery, sQLiteQueryBuilder.buildQuery(strArr, concatenateWhere, null, null, null, null)}, str12, queryParameter);
                    appendSelectionArgs = DatabaseUtils.appendSelectionArgs(strArr11, new String[]{queryParameter3, queryParameter4, "other_bookmarks"});
                    if (strArr8 != null) {
                        appendSelectionArgs = DatabaseUtils.appendSelectionArgs(appendSelectionArgs, strArr8);
                    }
                } else {
                    sQLiteQueryBuilder.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                    String concatenateWhere2 = DatabaseUtils.concatenateWhere("parent=? AND deleted=0", str8);
                    appendSelectionArgs = new String[]{Long.toString(1L)};
                    if (strArr8 != null) {
                        appendSelectionArgs = DatabaseUtils.appendSelectionArgs(appendSelectionArgs, strArr8);
                    }
                    buildUnionQuery = sQLiteQueryBuilder.buildQuery(strArr, concatenateWhere2, null, null, str12, null);
                }
                Cursor rawQuery = readableDatabase.rawQuery(buildUnionQuery, appendSelectionArgs);
                if (rawQuery != null) {
                    rawQuery.setNotificationUri(getContext().getContentResolver(), BrowserContract.AUTHORITY_URI);
                }
                return rawQuery;
            case 1004:
                return doSuggestQuery(str8, strArr8, queryParameter);
            case 1005:
                long queryDefaultFolderId = queryDefaultFolderId(uri.getQueryParameter("acct_name"), uri.getQueryParameter("acct_type"));
                MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{"_id"});
                matrixCursor2.newRow().add(Long.valueOf(queryDefaultFolderId));
                return matrixCursor2;
            case 2000:
                strArr6 = strArr8;
                filterSearchClient(strArr6);
                String str13 = str2 != null ? "date DESC" : str2;
                sQLiteQueryBuilder.setProjectionMap(HISTORY_PROJECTION_MAP);
                sQLiteQueryBuilder.setTables("history LEFT OUTER JOIN images ON history.url = images.url_key");
                strArr4 = strArr;
                strArr5 = strArr6;
                str5 = str8;
                str4 = str13;
                break;
            case 2001:
                str8 = DatabaseUtils.concatenateWhere(str8, "history._id=?");
                strArr6 = DatabaseUtils.appendSelectionArgs(strArr8, new String[]{Long.toString(ContentUris.parseId(uri))});
                filterSearchClient(strArr6);
                if (str2 != null) {
                }
                sQLiteQueryBuilder.setProjectionMap(HISTORY_PROJECTION_MAP);
                sQLiteQueryBuilder.setTables("history LEFT OUTER JOIN images ON history.url = images.url_key");
                strArr4 = strArr;
                strArr5 = strArr6;
                str5 = str8;
                str4 = str13;
                break;
            case 3000:
                strArr7 = strArr8;
                sQLiteQueryBuilder.setTables("searches");
                sQLiteQueryBuilder.setProjectionMap(SEARCHES_PROJECTION_MAP);
                strArr4 = strArr;
                str4 = str2;
                strArr5 = strArr7;
                str5 = str8;
                break;
            case 3001:
                str8 = DatabaseUtils.concatenateWhere(str8, "searches._id=?");
                strArr7 = DatabaseUtils.appendSelectionArgs(strArr8, new String[]{Long.toString(ContentUris.parseId(uri))});
                sQLiteQueryBuilder.setTables("searches");
                sQLiteQueryBuilder.setProjectionMap(SEARCHES_PROJECTION_MAP);
                strArr4 = strArr;
                str4 = str2;
                strArr5 = strArr7;
                str5 = str8;
                break;
            case 4000:
                return this.mSyncHelper.query(readableDatabase, strArr, str8, strArr8, str2);
            case 4001:
                String appendAccountToSelection = appendAccountToSelection(uri, str8);
                StringBuilder sb = new StringBuilder();
                sb.append("_id=");
                sb.append(ContentUris.parseId(uri));
                sb.append(" ");
                if (appendAccountToSelection == null) {
                    str7 = "";
                } else {
                    str7 = " AND (" + appendAccountToSelection + ")";
                }
                sb.append(str7);
                return this.mSyncHelper.query(readableDatabase, strArr, sb.toString(), strArr8, str2);
            case 5000:
                sQLiteQueryBuilder.setTables("images");
                sQLiteQueryBuilder.setProjectionMap(IMAGES_PROJECTION_MAP);
                str6 = str8;
                strArr4 = strArr;
                str4 = str2;
                str5 = str6;
                strArr5 = strArr8;
                break;
            case 6001:
            case 9001:
                str8 = DatabaseUtils.concatenateWhere(str8, "_id = CAST(? AS INTEGER)");
                strArr8 = DatabaseUtils.appendSelectionArgs(strArr8, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 6000:
            case 9000:
                String[] strArr12 = ((match == 9000 || match == 9001) && strArr == null) ? Browser.HISTORY_PROJECTION : strArr;
                String[] createCombinedQuery = createCombinedQuery(uri, strArr12, sQLiteQueryBuilder);
                if (strArr8 != null) {
                    createCombinedQuery = DatabaseUtils.appendSelectionArgs(createCombinedQuery, strArr8);
                }
                str4 = str2;
                strArr5 = createCombinedQuery;
                strArr4 = strArr12;
                str5 = str8;
                break;
            case 7000:
                sQLiteQueryBuilder.setTables("v_accounts");
                sQLiteQueryBuilder.setProjectionMap(ACCOUNTS_PROJECTION_MAP);
                String concatenateWhere3 = "false".equals(uri.getQueryParameter("allowEmptyAccounts")) ? DatabaseUtils.concatenateWhere(str8, "0 < ( SELECT count(*) FROM bookmarks WHERE deleted = 0 AND folder = 0   AND (     v_accounts.account_name = bookmarks.account_name     OR (v_accounts.account_name IS NULL AND bookmarks.account_name IS NULL)   )   AND (     v_accounts.account_type = bookmarks.account_type     OR (v_accounts.account_type IS NULL AND bookmarks.account_type IS NULL)   ) )") : str8;
                if (str2 == null) {
                    strArr4 = strArr;
                    str5 = concatenateWhere3;
                    str4 = "account_name IS NOT NULL DESC, account_name ASC";
                } else {
                    strArr4 = strArr;
                    str4 = str2;
                    str5 = concatenateWhere3;
                }
                strArr5 = strArr8;
                break;
            case 8000:
                sQLiteQueryBuilder.setTables("settings");
                sQLiteQueryBuilder.setProjectionMap(SETTINGS_PROJECTION_MAP);
                str6 = str8;
                strArr4 = strArr;
                str4 = str2;
                str5 = str6;
                strArr5 = strArr8;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URL " + uri.toString());
        }
        Cursor query = sQLiteQueryBuilder.query(readableDatabase, strArr4, str5, strArr5, queryParameter2, null, str4, queryParameter);
        query.setNotificationUri(getContext().getContentResolver(), BrowserContract.AUTHORITY_URI);
        return query;
    }

    private Cursor doSuggestQuery(String str, String[] strArr, String str2) {
        String str3;
        Log.i("browser/BrowserProvider", "doSuggestQuery");
        if (TextUtils.isEmpty(strArr[0])) {
            str3 = "history.date != 0";
            strArr = null;
        } else {
            String str4 = strArr[0] + "%";
            if (strArr[0].startsWith("http") || strArr[0].startsWith("file")) {
                strArr[0] = str4;
                str3 = "history." + str;
            } else {
                String[] strArr2 = {"http://" + str4, "http://www." + str4, "https://" + str4, "https://www." + str4, "%" + str4};
                StringBuilder sb = new StringBuilder();
                sb.append("doSuggestQuery, selectionArgs: ");
                sb.append(strArr2);
                Log.i("browser/BrowserProvider", sb.toString());
                SuggestionsCursor suggestionsCursor = new SuggestionsCursor(this.mOpenHelper.getReadableDatabase().query("v_omnibox_suggestions", new String[]{"_id", "url", "title", "bookmark"}, "url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ? OR title LIKE ?", strArr2, null, null, null, null));
                StringBuilder sb2 = new StringBuilder();
                sb2.append("doSuggestQuery, getCount: ");
                sb2.append(suggestionsCursor.getCount());
                Log.i("browser/BrowserProvider", sb2.toString());
                return suggestionsCursor;
            }
        }
        return new SuggestionsCursor(this.mOpenHelper.getReadableDatabase().query("history LEFT OUTER JOIN bookmarks ON (history.url = bookmarks.url AND bookmarks.deleted=0 AND bookmarks.folder=0)", SUGGEST_PROJECTION, str3, strArr, null, null, null, null));
    }

    private String[] createCombinedQuery(Uri uri, String[] strArr, SQLiteQueryBuilder sQLiteQueryBuilder) {
        String[] strArr2;
        StringBuilder sb = new StringBuilder(128);
        sb.append("deleted");
        sb.append(" = 0");
        Object[] selectionWithAccounts = getSelectionWithAccounts(uri, null, null);
        String str = (String) selectionWithAccounts[0];
        String[] strArr3 = (String[]) selectionWithAccounts[1];
        if (str != null) {
            sb.append(" AND " + str);
            if (strArr3 != null) {
                String[] strArr4 = new String[strArr3.length * 2];
                System.arraycopy(strArr3, 0, strArr4, 0, strArr3.length);
                System.arraycopy(strArr3, 0, strArr4, strArr3.length, strArr3.length);
                strArr2 = strArr4;
                String sb2 = sb.toString();
                sQLiteQueryBuilder.setTables("bookmarks");
                sQLiteQueryBuilder.setTables(String.format("history LEFT OUTER JOIN (%s) bookmarks ON history.url = bookmarks.url LEFT OUTER JOIN images ON history.url = images.url_key", sQLiteQueryBuilder.buildQuery(null, sb2, null, null, null, null)));
                sQLiteQueryBuilder.setProjectionMap(COMBINED_HISTORY_PROJECTION_MAP);
                String buildQuery = sQLiteQueryBuilder.buildQuery(null, null, null, null, null, null);
                sQLiteQueryBuilder.setTables("bookmarks LEFT OUTER JOIN images ON bookmarks.url = images.url_key");
                sQLiteQueryBuilder.setProjectionMap(COMBINED_BOOKMARK_PROJECTION_MAP);
                sQLiteQueryBuilder.setTables("(" + sQLiteQueryBuilder.buildUnionQuery(new String[]{buildQuery, sQLiteQueryBuilder.buildQuery(null, sb2 + String.format(" AND %s NOT IN (SELECT %s FROM %s)", "url", "url", "history"), null, null, null, null)}, null, null) + ")");
                sQLiteQueryBuilder.setProjectionMap(null);
                return strArr2;
            }
        }
        strArr2 = null;
        String sb22 = sb.toString();
        sQLiteQueryBuilder.setTables("bookmarks");
        sQLiteQueryBuilder.setTables(String.format("history LEFT OUTER JOIN (%s) bookmarks ON history.url = bookmarks.url LEFT OUTER JOIN images ON history.url = images.url_key", sQLiteQueryBuilder.buildQuery(null, sb22, null, null, null, null)));
        sQLiteQueryBuilder.setProjectionMap(COMBINED_HISTORY_PROJECTION_MAP);
        String buildQuery2 = sQLiteQueryBuilder.buildQuery(null, null, null, null, null, null);
        sQLiteQueryBuilder.setTables("bookmarks LEFT OUTER JOIN images ON bookmarks.url = images.url_key");
        sQLiteQueryBuilder.setProjectionMap(COMBINED_BOOKMARK_PROJECTION_MAP);
        sQLiteQueryBuilder.setTables("(" + sQLiteQueryBuilder.buildUnionQuery(new String[]{buildQuery2, sQLiteQueryBuilder.buildQuery(null, sb22 + String.format(" AND %s NOT IN (SELECT %s FROM %s)", "url", "url", "history"), null, null, null, null)}, null, null) + ")");
        sQLiteQueryBuilder.setProjectionMap(null);
        return strArr2;
    }

    int deleteBookmarks(String str, String[] strArr, boolean z) {
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        if (z) {
            return writableDatabase.delete("bookmarks", str, strArr);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("modified", Long.valueOf(System.currentTimeMillis()));
        contentValues.put("deleted", (Integer) 1);
        return updateBookmarksInTransaction(contentValues, str, strArr, z);
    }

    /* JADX WARN: Removed duplicated region for block: B:38:0x0178  */
    @Override // com.android.browser.provider.SQLiteContentProvider
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int deleteInTransaction(Uri uri, String str, String[] strArr, boolean z) {
        int delete;
        char c;
        String str2;
        String str3 = str;
        String[] strArr2 = strArr;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        switch (match) {
            case 11:
                str3 = DatabaseUtils.concatenateWhere(str3, "_id = ?");
                strArr2 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 10:
                delete = writableDatabase.delete("thumbnails", str3, strArr2);
                break;
            case 1000:
                c = 0;
                Object[] selectionWithAccounts = getSelectionWithAccounts(uri, str3, strArr2);
                delete = deleteBookmarks((String) selectionWithAccounts[c], (String[]) selectionWithAccounts[1], z);
                pruneImages();
                if (delete > 0) {
                    refreshWidgets();
                    break;
                }
                break;
            case 1001:
                c = 0;
                str3 = DatabaseUtils.concatenateWhere(str3, "bookmarks._id=?");
                strArr2 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
                Object[] selectionWithAccounts2 = getSelectionWithAccounts(uri, str3, strArr2);
                delete = deleteBookmarks((String) selectionWithAccounts2[c], (String[]) selectionWithAccounts2[1], z);
                pruneImages();
                if (delete > 0) {
                }
                break;
            case 2001:
                str3 = DatabaseUtils.concatenateWhere(str3, "history._id=?");
                strArr2 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 2000:
                filterSearchClient(strArr2);
                delete = writableDatabase.delete("history", str3, strArr2);
                pruneImages();
                break;
            case 3001:
                str3 = DatabaseUtils.concatenateWhere(str3, "searches._id=?");
                strArr2 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 3000:
                delete = writableDatabase.delete("searches", str3, strArr2);
                break;
            case 4000:
                delete = this.mSyncHelper.delete(writableDatabase, str3, strArr2);
                break;
            case 4001:
                StringBuilder sb = new StringBuilder();
                sb.append("_id=");
                sb.append(ContentUris.parseId(uri));
                sb.append(" ");
                if (str3 == null) {
                    str2 = "";
                } else {
                    str2 = " AND (" + str3 + ")";
                }
                sb.append(str2);
                delete = this.mSyncHelper.delete(writableDatabase, sb.toString(), strArr2);
                break;
            case 9001:
                str3 = DatabaseUtils.concatenateWhere(str3, "_id = CAST(? AS INTEGER)");
                strArr2 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 9000:
                String str4 = str3;
                String[] strArr3 = {"_id", "bookmark", "url"};
                SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
                String[] createCombinedQuery = createCombinedQuery(uri, strArr3, sQLiteQueryBuilder);
                if (strArr2 != null) {
                    createCombinedQuery = DatabaseUtils.appendSelectionArgs(createCombinedQuery, strArr2);
                }
                Cursor query = sQLiteQueryBuilder.query(writableDatabase, strArr3, str4, createCombinedQuery, null, null, null);
                delete = 0;
                while (query.moveToNext()) {
                    long j = query.getLong(0);
                    boolean z2 = query.getInt(1) != 0;
                    String string = query.getString(2);
                    if (z2) {
                        delete += deleteBookmarks("_id=?", new String[]{Long.toString(j)}, z);
                        writableDatabase.delete("history", "url=?", new String[]{string});
                    } else {
                        delete += writableDatabase.delete("history", "_id=?", new String[]{Long.toString(j)});
                    }
                }
                query.close();
                break;
            default:
                throw new UnsupportedOperationException("Unknown delete URI " + uri);
        }
        if (delete > 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
        }
        return delete;
    }

    long queryDefaultFolderId(String str, String str2) {
        if (!isNullAccount(str) && !isNullAccount(str2)) {
            Cursor query = this.mOpenHelper.getReadableDatabase().query("bookmarks", new String[]{"_id"}, "sync3 = ? AND account_type = ? AND account_name = ?", new String[]{"bookmark_bar", str2, str}, null, null, null);
            try {
                if (query.moveToFirst()) {
                    return query.getLong(0);
                }
                return 1L;
            } finally {
                query.close();
            }
        }
        return 1L;
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    public Uri insertInTransaction(Uri uri, ContentValues contentValues, boolean z) {
        long replaceOrThrow;
        String asString;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        if (match == 9000) {
            Integer asInteger = contentValues.getAsInteger("bookmark");
            contentValues.remove("bookmark");
            if (asInteger != null && asInteger.intValue() != 0) {
                contentValues.remove("date");
                contentValues.remove("visits");
                contentValues.remove("user_entered");
                contentValues.put("folder", (Integer) 0);
                match = 1000;
            } else {
                match = 2000;
            }
        }
        if (match == 10) {
            replaceOrThrow = writableDatabase.replaceOrThrow("thumbnails", null, contentValues);
        } else if (match != 1000) {
            if (match == 2000) {
                if (!contentValues.containsKey("created")) {
                    contentValues.put("created", Long.valueOf(System.currentTimeMillis()));
                }
                contentValues.put("url", filterSearchClient(contentValues.getAsString("url")));
                ContentValues extractImageValues = extractImageValues(contentValues, contentValues.getAsString("url"));
                if (extractImageValues != null) {
                    writableDatabase.insertOrThrow("images", "favicon", extractImageValues);
                }
                replaceOrThrow = writableDatabase.insertOrThrow("history", "visits", contentValues);
            } else if (match == 3000) {
                replaceOrThrow = insertSearchesInTransaction(writableDatabase, contentValues);
            } else if (match == 4000) {
                replaceOrThrow = this.mSyncHelper.insert(writableDatabase, contentValues);
            } else if (match == 8000) {
                insertSettingsInTransaction(writableDatabase, contentValues);
                replaceOrThrow = 0;
            } else {
                throw new UnsupportedOperationException("Unknown insert URI " + uri);
            }
        } else {
            if (contentValues.containsKey("url") && (asString = contentValues.getAsString("url")) != null) {
                contentValues.put("url", asString.trim());
            }
            if (!z) {
                long currentTimeMillis = System.currentTimeMillis();
                contentValues.put("created", Long.valueOf(currentTimeMillis));
                contentValues.put("modified", Long.valueOf(currentTimeMillis));
                contentValues.put("dirty", (Integer) 1);
                boolean z2 = contentValues.containsKey("account_type") || contentValues.containsKey("account_name");
                String asString2 = contentValues.getAsString("account_type");
                String asString3 = contentValues.getAsString("account_name");
                boolean containsKey = contentValues.containsKey("parent");
                if (containsKey && z2) {
                    containsKey = isValidParent(asString2, asString3, contentValues.getAsLong("parent").longValue());
                } else if (containsKey && !z2) {
                    containsKey = setParentValues(contentValues.getAsLong("parent").longValue(), contentValues);
                }
                if (!containsKey) {
                    contentValues.put("parent", Long.valueOf(queryDefaultFolderId(asString3, asString2)));
                }
            }
            if (contentValues.containsKey("folder") && contentValues.getAsBoolean("folder").booleanValue() && contentValues.containsKey("parent") && contentValues.containsKey("title") && !isValidAccountName(contentValues.getAsLong("parent").longValue(), contentValues.getAsString("title"))) {
                return null;
            }
            if (!contentValues.containsKey("position")) {
                contentValues.put("position", Long.toString(Long.MIN_VALUE));
            }
            String asString4 = contentValues.getAsString("url");
            ContentValues extractImageValues2 = extractImageValues(contentValues, asString4);
            Boolean asBoolean = contentValues.getAsBoolean("folder");
            if ((asBoolean == null || !asBoolean.booleanValue()) && extractImageValues2 != null && !TextUtils.isEmpty(asString4) && writableDatabase.update("images", extractImageValues2, "url_key=?", new String[]{asString4}) == 0) {
                writableDatabase.insertOrThrow("images", "favicon", extractImageValues2);
            }
            replaceOrThrow = writableDatabase.insertOrThrow("bookmarks", "dirty", contentValues);
            refreshWidgets();
        }
        if (replaceOrThrow >= 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
            return ContentUris.withAppendedId(uri, replaceOrThrow);
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r0v12, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v7, types: [android.database.Cursor] */
    /* JADX WARN: Type inference failed for: r0v8 */
    private boolean isValidAccountName(long j, String str) {
        Cursor query;
        Log.e("browser/BrowserProvider", "BrowserProvider2.isValidAccountName parentId:" + j + " title:" + str);
        if (j <= 0 || str == null || str.length() == 0) {
            return true;
        }
        Uri uri = BrowserContract.Bookmarks.CONTENT_URI;
        Cursor cursor = 0;
        try {
            try {
                query = query(uri, new String[]{"title"}, "parent = ? AND deleted = ? AND folder = ?", new String[]{j + "", "0", "1"}, null);
            } catch (Throwable th) {
                th = th;
            }
        } catch (IllegalStateException e) {
            e = e;
        }
        if (query != null) {
            try {
            } catch (IllegalStateException e2) {
                e = e2;
                cursor = query;
                Log.e("browser/BrowserProvider", e.getMessage());
                if (cursor != 0) {
                    cursor.close();
                }
                return true;
            } catch (Throwable th2) {
                th = th2;
                cursor = query;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
            if (query.getCount() != 0) {
                do {
                    cursor = query.moveToNext();
                    if (cursor == 0) {
                        if (query != null) {
                            query.close();
                        }
                        return true;
                    }
                } while (!str.equals(query.getString(0)));
                if (query != null) {
                    query.close();
                }
                return false;
            }
        }
        if (query != null) {
            query.close();
        }
        return true;
    }

    private String[] getAccountNameAndType(long j) {
        Cursor query;
        if (j > 0 && (query = query(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, j), new String[]{"account_name", "account_type"}, null, null, null)) != null) {
            try {
                if (query.moveToFirst()) {
                    return new String[]{query.getString(0), query.getString(1)};
                }
                return null;
            } finally {
                query.close();
            }
        }
        return null;
    }

    private boolean setParentValues(long j, ContentValues contentValues) {
        String[] accountNameAndType = getAccountNameAndType(j);
        if (accountNameAndType == null) {
            return false;
        }
        contentValues.put("account_name", accountNameAndType[0]);
        contentValues.put("account_type", accountNameAndType[1]);
        return true;
    }

    private boolean isValidParent(String str, String str2, long j) {
        String[] accountNameAndType = getAccountNameAndType(j);
        return accountNameAndType != null && TextUtils.equals(str2, accountNameAndType[0]) && TextUtils.equals(str, accountNameAndType[1]);
    }

    private void filterSearchClient(String[] strArr) {
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                strArr[i] = filterSearchClient(strArr[i]);
            }
        }
    }

    private String filterSearchClient(String str) {
        int indexOf = str.indexOf("client=");
        if (indexOf > 0 && str.contains(".google.")) {
            int indexOf2 = str.indexOf(38, indexOf);
            if (indexOf2 <= 0) {
                return str.substring(0, indexOf - 1);
            }
            return str.substring(0, indexOf).concat(str.substring(indexOf2 + 1));
        }
        return str;
    }

    private long insertSearchesInTransaction(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
        Cursor cursor;
        String asString = contentValues.getAsString("search");
        if (TextUtils.isEmpty(asString)) {
            throw new IllegalArgumentException("Must include the SEARCH field");
        }
        try {
            cursor = sQLiteDatabase.query("searches", new String[]{"_id"}, "search=?", new String[]{asString}, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    long j = cursor.getLong(0);
                    sQLiteDatabase.update("searches", contentValues, "_id=?", new String[]{Long.toString(j)});
                    if (cursor != null) {
                        cursor.close();
                    }
                    return j;
                }
                long insertOrThrow = sQLiteDatabase.insertOrThrow("searches", "search", contentValues);
                if (cursor != null) {
                    cursor.close();
                }
                return insertOrThrow;
            } catch (Throwable th) {
                th = th;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            cursor = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public long insertSettingsInTransaction(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
        Cursor cursor;
        String asString = contentValues.getAsString("key");
        if (TextUtils.isEmpty(asString)) {
            throw new IllegalArgumentException("Must include the KEY field");
        }
        String[] strArr = {asString};
        try {
            cursor = sQLiteDatabase.query("settings", new String[]{"key"}, "key=?", strArr, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    long j = cursor.getLong(0);
                    sQLiteDatabase.update("settings", contentValues, "key=?", strArr);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return j;
                }
                long insertOrThrow = sQLiteDatabase.insertOrThrow("settings", "value", contentValues);
                if (cursor != null) {
                    cursor.close();
                }
                return insertOrThrow;
            } catch (Throwable th) {
                th = th;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            cursor = null;
        }
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    public int updateInTransaction(Uri uri, ContentValues contentValues, String str, String[] strArr, boolean z) {
        String asString;
        String str2;
        boolean z2;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        if (match == 9000 || match == 9001) {
            Integer asInteger = contentValues.getAsInteger("bookmark");
            contentValues.remove("bookmark");
            if (asInteger == null || asInteger.intValue() == 0) {
                if (match == 9000) {
                    match = 2000;
                } else {
                    match = 2001;
                }
            } else {
                if (match == 9000) {
                    match = 1000;
                } else {
                    match = 1001;
                }
                contentValues.remove("date");
                contentValues.remove("visits");
                contentValues.remove("user_entered");
            }
        }
        int i = 0;
        switch (match) {
            case 10:
                i = writableDatabase.update("thumbnails", contentValues, str, strArr);
                break;
            case 30:
                return (contentValues == null || (asString = contentValues.getAsString("homepage")) == null || !setHomePage(getContext(), asString)) ? 0 : 1;
            case 1001:
                str = DatabaseUtils.concatenateWhere(str, "bookmarks._id=?");
                strArr = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 1000:
                Object[] selectionWithAccounts = getSelectionWithAccounts(uri, str, strArr);
                i = updateBookmarksInTransaction(contentValues, (String) selectionWithAccounts[0], (String[]) selectionWithAccounts[1], z);
                if (i > 0) {
                    refreshWidgets();
                    break;
                }
                break;
            case 2001:
                str = DatabaseUtils.concatenateWhere(str, "history._id=?");
                strArr = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 2000:
                i = updateHistoryInTransaction(contentValues, str, strArr);
                break;
            case 3000:
                i = writableDatabase.update("searches", contentValues, str, strArr);
                break;
            case 4000:
                i = this.mSyncHelper.update(this.mDb, contentValues, appendAccountToSelection(uri, str), strArr);
                break;
            case 4001:
                String appendAccountToSelection = appendAccountToSelection(uri, str);
                StringBuilder sb = new StringBuilder();
                sb.append("_id=");
                sb.append(ContentUris.parseId(uri));
                sb.append(" ");
                if (appendAccountToSelection == null) {
                    str2 = "";
                } else {
                    str2 = " AND (" + appendAccountToSelection + ")";
                }
                sb.append(str2);
                i = this.mSyncHelper.update(this.mDb, contentValues, sb.toString(), strArr);
                break;
            case 5000:
                String asString2 = contentValues.getAsString("url_key");
                if (TextUtils.isEmpty(asString2)) {
                    throw new IllegalArgumentException("Images.URL is required");
                }
                if (shouldUpdateImages(writableDatabase, asString2, contentValues)) {
                    int update = writableDatabase.update("images", contentValues, "url_key=?", new String[]{asString2});
                    if (update == 0) {
                        writableDatabase.insertOrThrow("images", "favicon", contentValues);
                        update = 1;
                    }
                    if (getUrlCount(writableDatabase, "bookmarks", asString2) > 0) {
                        postNotifyUri(BrowserContract.Bookmarks.CONTENT_URI);
                        z2 = contentValues.containsKey("favicon");
                        refreshWidgets();
                    } else {
                        z2 = false;
                    }
                    if (getUrlCount(writableDatabase, "history", asString2) > 0) {
                        postNotifyUri(BrowserContract.History.CONTENT_URI);
                        z2 = contentValues.containsKey("favicon");
                    }
                    if (pruneImages() > 0 || z2) {
                        postNotifyUri(LEGACY_AUTHORITY_URI);
                    }
                    this.mSyncToNetwork = false;
                    return update;
                }
                return 0;
            case 7000:
                this.mSyncHelper.onAccountsChanged(this.mDb, AccountManager.get(getContext()).getAccounts());
                break;
            default:
                throw new UnsupportedOperationException("Unknown update URI " + uri);
        }
        pruneImages();
        if (i > 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
        }
        return i;
    }

    private boolean shouldUpdateImages(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues) {
        boolean z = true;
        Cursor query = sQLiteDatabase.query("images", new String[]{"favicon", "thumbnail", "touch_icon"}, "url_key=?", new String[]{str}, null, null, null);
        byte[] asByteArray = contentValues.getAsByteArray("favicon");
        byte[] asByteArray2 = contentValues.getAsByteArray("thumbnail");
        byte[] asByteArray3 = contentValues.getAsByteArray("touch_icon");
        try {
            if (query.getCount() <= 0) {
                if (asByteArray == null && asByteArray2 == null && asByteArray3 == null) {
                    z = false;
                }
                return z;
            }
            while (query.moveToNext()) {
                if (asByteArray != null && !Arrays.equals(asByteArray, query.getBlob(0))) {
                    return true;
                }
                if (asByteArray2 != null && !Arrays.equals(asByteArray2, query.getBlob(1))) {
                    return true;
                }
                if (asByteArray3 != null && !Arrays.equals(asByteArray3, query.getBlob(2))) {
                    return true;
                }
            }
            return false;
        } finally {
            query.close();
        }
    }

    int getUrlCount(SQLiteDatabase sQLiteDatabase, String str, String str2) {
        Cursor query = sQLiteDatabase.query(str, new String[]{"COUNT(*)"}, "url = ?", new String[]{str2}, null, null, null);
        try {
            return query.moveToFirst() ? query.getInt(0) : 0;
        } finally {
            query.close();
        }
    }

    int updateBookmarksInTransaction(ContentValues contentValues, String str, String[] strArr, boolean z) {
        int i;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        boolean z2;
        int i2;
        String str7;
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        Cursor query = writableDatabase.query("bookmarks", new String[]{"_id", "version", "url", "title", "folder", "account_name", "account_type"}, str, strArr, null, null, null);
        boolean containsKey = contentValues.containsKey("parent");
        int i3 = 0;
        if (containsKey) {
            i = 1;
            Cursor query2 = writableDatabase.query("bookmarks", new String[]{"account_name", "account_type"}, "_id = ?", new String[]{Long.toString(contentValues.getAsLong("parent").longValue())}, null, null, null);
            if (query2.moveToFirst()) {
                str2 = query2.getString(0);
                str3 = query2.getString(1);
            } else {
                str2 = null;
                str3 = null;
            }
            query2.close();
        } else {
            i = 1;
            if (!contentValues.containsKey("account_name")) {
                contentValues.containsKey("account_type");
            }
            str2 = null;
            str3 = null;
        }
        try {
            String[] strArr2 = new String[i];
            if (!z) {
                contentValues.put("modified", Long.valueOf(System.currentTimeMillis()));
                contentValues.put("dirty", Integer.valueOf(i));
            }
            boolean containsKey2 = contentValues.containsKey("url");
            if (containsKey2) {
                str4 = contentValues.getAsString("url");
            } else {
                str4 = null;
            }
            ContentValues extractImageValues = extractImageValues(contentValues, str4);
            String str8 = str4;
            int i4 = 0;
            while (query.moveToNext()) {
                String str9 = str8;
                long j = query.getLong(i3);
                strArr2[i3] = Long.toString(j);
                String string = query.getString(5);
                String string2 = query.getString(6);
                if (containsKey && (!TextUtils.equals(string, str2) || !TextUtils.equals(string2, str3))) {
                    ContentValues valuesFromCursor = valuesFromCursor(query);
                    valuesFromCursor.putAll(contentValues);
                    valuesFromCursor.remove("_id");
                    valuesFromCursor.remove("version");
                    valuesFromCursor.put("account_name", str2);
                    valuesFromCursor.put("account_type", str3);
                    long parseId = ContentUris.parseId(insertInTransaction(BrowserContract.Bookmarks.CONTENT_URI, valuesFromCursor, z));
                    str5 = str2;
                    if (query.getInt(4) != 0) {
                        str6 = str3;
                        ContentValues contentValues2 = new ContentValues(1);
                        contentValues2.put("parent", Long.valueOf(parseId));
                        i4 += updateBookmarksInTransaction(contentValues2, "parent=?", new String[]{Long.toString(j)}, z);
                    } else {
                        str6 = str3;
                    }
                    deleteInTransaction(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, j), null, null, z);
                    i4++;
                    z2 = true;
                } else {
                    str5 = str2;
                    str6 = str3;
                    if (!z) {
                        z2 = true;
                        contentValues.put("version", Long.valueOf(query.getLong(1) + 1));
                    } else {
                        z2 = true;
                    }
                    i4 += writableDatabase.update("bookmarks", contentValues, "_id=?", strArr2);
                }
                if (extractImageValues != null) {
                    if (!containsKey2) {
                        str7 = query.getString(2);
                        extractImageValues.put("url_key", str7);
                    } else {
                        str7 = str9;
                    }
                    if (!TextUtils.isEmpty(str7)) {
                        i2 = 0;
                        strArr2[0] = str7;
                        if (writableDatabase.update("images", extractImageValues, "url_key=?", strArr2) == 0) {
                            writableDatabase.insert("images", "favicon", extractImageValues);
                        }
                    } else {
                        i2 = 0;
                    }
                    str9 = str7;
                } else {
                    i2 = 0;
                }
                i3 = i2;
                str8 = str9;
                str2 = str5;
                str3 = str6;
            }
            return i4;
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    ContentValues valuesFromCursor(Cursor cursor) {
        int columnCount = cursor.getColumnCount();
        ContentValues contentValues = new ContentValues(columnCount);
        String[] columnNames = cursor.getColumnNames();
        for (int i = 0; i < columnCount; i++) {
            switch (cursor.getType(i)) {
                case 1:
                    contentValues.put(columnNames[i], Long.valueOf(cursor.getLong(i)));
                    break;
                case 2:
                    contentValues.put(columnNames[i], Float.valueOf(cursor.getFloat(i)));
                    break;
                case 3:
                    contentValues.put(columnNames[i], cursor.getString(i));
                    break;
                case 4:
                    contentValues.put(columnNames[i], cursor.getBlob(i));
                    break;
            }
        }
        return contentValues;
    }

    int updateHistoryInTransaction(ContentValues contentValues, String str, String[] strArr) {
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        filterSearchClient(strArr);
        Cursor query = query(BrowserContract.History.CONTENT_URI, new String[]{"_id", "url"}, str, strArr, null);
        if (query == null) {
            return 0;
        }
        try {
            String[] strArr2 = new String[1];
            boolean containsKey = contentValues.containsKey("url");
            String str2 = null;
            if (containsKey) {
                str2 = filterSearchClient(contentValues.getAsString("url"));
                contentValues.put("url", str2);
            }
            ContentValues extractImageValues = extractImageValues(contentValues, str2);
            String str3 = str2;
            int i = 0;
            while (query.moveToNext()) {
                strArr2[0] = query.getString(0);
                i += writableDatabase.update("history", contentValues, "_id=?", strArr2);
                if (extractImageValues != null) {
                    if (!containsKey) {
                        str3 = query.getString(1);
                        extractImageValues.put("url_key", str3);
                    }
                    strArr2[0] = str3;
                    if (writableDatabase.update("images", extractImageValues, "url_key=?", strArr2) == 0) {
                        writableDatabase.insert("images", "favicon", extractImageValues);
                    }
                }
            }
            return i;
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    String appendAccountToSelection(Uri uri, String str) {
        String queryParameter = uri.getQueryParameter("account_name");
        String queryParameter2 = uri.getQueryParameter("account_type");
        if (TextUtils.isEmpty(queryParameter) ^ TextUtils.isEmpty(queryParameter2)) {
            throw new IllegalArgumentException("Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE for " + uri);
        } else if (!TextUtils.isEmpty(queryParameter)) {
            StringBuilder sb = new StringBuilder("account_name=" + DatabaseUtils.sqlEscapeString(queryParameter) + " AND account_type=" + DatabaseUtils.sqlEscapeString(queryParameter2));
            if (!TextUtils.isEmpty(str)) {
                sb.append(" AND (");
                sb.append(str);
                sb.append(')');
            }
            return sb.toString();
        } else {
            return str;
        }
    }

    ContentValues extractImageValues(ContentValues contentValues, String str) {
        ContentValues contentValues2;
        if (contentValues.containsKey("favicon")) {
            contentValues2 = new ContentValues();
            contentValues2.put("favicon", contentValues.getAsByteArray("favicon"));
            contentValues.remove("favicon");
        } else {
            contentValues2 = null;
        }
        if (contentValues.containsKey("thumbnail")) {
            if (contentValues2 == null) {
                contentValues2 = new ContentValues();
            }
            contentValues2.put("thumbnail", contentValues.getAsByteArray("thumbnail"));
            contentValues.remove("thumbnail");
        }
        if (contentValues.containsKey("touch_icon")) {
            if (contentValues2 == null) {
                contentValues2 = new ContentValues();
            }
            contentValues2.put("touch_icon", contentValues.getAsByteArray("touch_icon"));
            contentValues.remove("touch_icon");
        }
        if (contentValues2 != null) {
            contentValues2.put("url_key", str);
        }
        return contentValues2;
    }

    int pruneImages() {
        return this.mOpenHelper.getWritableDatabase().delete("images", "url_key NOT IN (SELECT url FROM bookmarks WHERE url IS NOT NULL AND deleted == 0) AND url_key NOT IN (SELECT url FROM history WHERE url IS NOT NULL)", null);
    }

    boolean shouldNotifyLegacy(Uri uri) {
        if (uri.getPathSegments().contains("history") || uri.getPathSegments().contains("bookmarks") || uri.getPathSegments().contains("searches")) {
            return true;
        }
        return false;
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    protected boolean syncToNetwork(Uri uri) {
        if ("com.android.browser.provider".equals(uri.getAuthority()) && uri.getPathSegments().contains("bookmarks")) {
            return this.mSyncToNetwork;
        }
        if ("MtkBrowserProvider".equals(uri.getAuthority())) {
            return true;
        }
        return false;
    }

    private boolean setHomePage(Context context, String str) {
        if (str == null || str.length() <= 0) {
            return false;
        }
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString("homepage", str);
        edit.commit();
        return true;
    }

    private String getHomePage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("homepage", BrowserSettings.getFactoryResetUrlFromRes(context));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class SuggestionsCursor extends AbstractCursor {
        private static final String[] COLUMNS = {"_id", "suggest_intent_action", "suggest_intent_data", "suggest_text_1", "suggest_text_2", "suggest_text_2_url", "suggest_icon_1", "suggest_last_access_hint"};
        private final Cursor mSource;

        public SuggestionsCursor(Cursor cursor) {
            this.mSource = cursor;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public String[] getColumnNames() {
            return COLUMNS;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public String getString(int i) {
            switch (i) {
                case 0:
                    return this.mSource.getString(i);
                case 1:
                    return "android.intent.action.VIEW";
                case 2:
                    return this.mSource.getString(1);
                case 3:
                    return this.mSource.getString(2);
                case 4:
                case 5:
                    return UrlUtils.stripUrl(this.mSource.getString(1));
                case 6:
                    if (this.mSource.getInt(3) == 1) {
                        return Integer.toString(R.drawable.ic_bookmark_off_holo_dark);
                    }
                    return Integer.toString(R.drawable.ic_history_holo_dark);
                case 7:
                    return Long.toString(System.currentTimeMillis());
                default:
                    return null;
            }
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public int getCount() {
            return this.mSource.getCount();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public double getDouble(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public float getFloat(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public int getInt(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public long getLong(int i) {
            if (i != 0) {
                if (i == 7) {
                    return this.mSource.getLong(4);
                }
                throw new UnsupportedOperationException();
            }
            return this.mSource.getLong(0);
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public short getShort(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public boolean isNull(int i) {
            return this.mSource.isNull(i);
        }

        @Override // android.database.AbstractCursor, android.database.CrossProcessCursor
        public boolean onMove(int i, int i2) {
            return this.mSource.moveToPosition(i2);
        }

        @Override // android.database.AbstractCursor, android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            this.mSource.close();
            super.close();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public void deactivate() {
            this.mSource.deactivate();
            super.deactivate();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public boolean isClosed() {
            return this.mSource.isClosed();
        }
    }
}
