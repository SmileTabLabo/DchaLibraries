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
import com.android.browser.UrlUtils;
import com.android.browser.provider.BrowserContract;
import com.android.browser.provider.BrowserProvider;
import com.android.browser.widget.BookmarkThumbnailWidgetProvider;
import com.android.common.content.SyncStateContentProviderHelper;
import com.google.common.annotations.VisibleForTesting;
import com.mediatek.browser.ext.IBrowserBookmarkExt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
/* loaded from: b.zip:com/android/browser/provider/BrowserProvider2.class */
public class BrowserProvider2 extends SQLiteContentProvider {
    DatabaseHelper mOpenHelper;
    static final Uri LEGACY_AUTHORITY_URI = new Uri.Builder().authority("MtkBrowserProvider").scheme("content").build();
    private static final String[] SUGGEST_PROJECTION = {qualifyColumn("history", "_id"), qualifyColumn("history", "url"), bookmarkOrHistoryColumn("title"), bookmarkOrHistoryLiteral("url", Integer.toString(2130837536), Integer.toString(2130837559)), qualifyColumn("history", "date")};
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
    private IBrowserBookmarkExt mBrowserBookmarkExt = null;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/provider/BrowserProvider2$DatabaseHelper.class */
    public final class DatabaseHelper extends SQLiteOpenHelper {
        final BrowserProvider2 this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public DatabaseHelper(BrowserProvider2 browserProvider2, Context context) {
            super(context, "browser2.db", (SQLiteDatabase.CursorFactory) null, 32);
            this.this$0 = browserProvider2;
            setWriteAheadLoggingEnabled(true);
        }

        private int addDefaultBookmarks(SQLiteDatabase sQLiteDatabase, long j, CharSequence[] charSequenceArr, TypedArray typedArray, int i) {
            Resources resources = this.this$0.getContext().getResources();
            int length = charSequenceArr.length;
            try {
                String l = Long.toString(j);
                String l2 = Long.toString(System.currentTimeMillis());
                for (int i2 = 0; i2 < length; i2 += 2) {
                    CharSequence replaceSystemPropertyInString = BrowserProvider2.replaceSystemPropertyInString(this.this$0.getContext(), charSequenceArr[i2 + 1]);
                    sQLiteDatabase.execSQL("INSERT INTO bookmarks (title, url, folder,parent,position,created) VALUES ('" + charSequenceArr[i2] + "', '" + replaceSystemPropertyInString + "', 0," + l + "," + ("http://www.google.com/".equals(replaceSystemPropertyInString.toString()) ? 1 : Integer.toString(i + i2)) + "," + l2 + ");");
                    int resourceId = typedArray.getResourceId(i2, 0);
                    byte[] bArr = null;
                    byte[] bArr2 = null;
                    try {
                        bArr = readRaw(resources, typedArray.getResourceId(i2 + 1, 0));
                    } catch (IOException e) {
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
            } finally {
                typedArray.recycle();
            }
            return length;
        }

        private void createDefaultBookmarks(SQLiteDatabase sQLiteDatabase) {
            int i = 1;
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", (Long) 1L);
            contentValues.put("sync3", "google_chrome_bookmarks");
            contentValues.put("title", "Bookmarks");
            contentValues.putNull("parent");
            contentValues.put("position", (Integer) 0);
            contentValues.put("folder", (Boolean) true);
            contentValues.put("dirty", (Boolean) true);
            sQLiteDatabase.insertOrThrow("bookmarks", null, contentValues);
            this.this$0.mBrowserBookmarkExt = Extensions.getBookmarkPlugin(this.this$0.getContext());
            int addDefaultBookmarksForCustomer = this.this$0.mBrowserBookmarkExt.addDefaultBookmarksForCustomer(sQLiteDatabase);
            if (addDefaultBookmarksForCustomer == 0) {
                if (addDefaultBookmarksForCustomer <= 0) {
                    i = 2;
                }
                addDefaultBookmarks(sQLiteDatabase, 1L, addDefaultBookmarksForYahoo(sQLiteDatabase, 1L, addDefaultBookmarksForCustomer + i));
            }
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
                    if (read <= 0) {
                        byteArrayOutputStream.flush();
                        return byteArrayOutputStream.toByteArray();
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                }
            } finally {
                openRawResource.close();
            }
        }

        public int addDefaultBookmarks(SQLiteDatabase sQLiteDatabase, long j, int i) {
            Resources resources = this.this$0.getContext().getResources();
            CharSequence[] textArray = resources.getTextArray(2131230833);
            int length = textArray.length;
            return addDefaultBookmarks(sQLiteDatabase, j, textArray, resources.obtainTypedArray(2131230810), i);
        }

        public int addDefaultBookmarksForYahoo(SQLiteDatabase sQLiteDatabase, long j, int i) {
            Resources resources = this.this$0.getContext().getResources();
            CharSequence[] textArray = resources.getTextArray(2131230812);
            int length = textArray.length;
            return addDefaultBookmarks(sQLiteDatabase, j, textArray, resources.obtainTypedArray(2131230809), i);
        }

        void createAccountsView(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE VIEW IF NOT EXISTS v_accounts AS SELECT NULL AS account_name, NULL AS account_type, 1 AS root_id UNION ALL SELECT account_name, account_type, _id AS root_id FROM bookmarks WHERE sync3 = \"bookmark_bar\" AND deleted = 0");
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
            this.this$0.insertSettingsInTransaction(sQLiteDatabase, contentValues);
            AccountManager accountManager = (AccountManager) this.this$0.getContext().getSystemService("account");
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

        boolean importFromBrowserProvider(SQLiteDatabase sQLiteDatabase) {
            Context context = this.this$0.getContext();
            File databasePath = context.getDatabasePath("browser.db");
            if (databasePath.exists()) {
                BrowserProvider.DatabaseHelper databaseHelper = new BrowserProvider.DatabaseHelper(context);
                SQLiteDatabase writableDatabase = databaseHelper.getWritableDatabase();
                int i = 0;
                Cursor cursor = null;
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
                        i = 0;
                        while (query.moveToNext()) {
                            i++;
                            ContentValues contentValues2 = new ContentValues();
                            contentValues2.put("_id", Integer.valueOf(query.getInt(0) + 1));
                            contentValues2.put("title", query.getString(3));
                            contentValues2.put("created", Integer.valueOf(query.getInt(4)));
                            contentValues2.put("position", Integer.valueOf(i));
                            contentValues2.put("folder", (Boolean) true);
                            contentValues2.put("parent", Integer.valueOf(query.getInt(1) + 1));
                            sQLiteDatabase.insertOrThrow("bookmarks", "dirty", contentValues2);
                        }
                        query.close();
                    }
                    String str2 = BrowserProvider.TABLE_NAMES[0];
                    Cursor query2 = writableDatabase.query(str2, new String[]{"url", "title", "favicon", "touch_icon", "created", "folder_id"}, "bookmark!=0", null, null, null, "visits DESC");
                    if (query2 != null) {
                        while (query2.moveToNext()) {
                            String string = query2.getString(0);
                            if (!TextUtils.isEmpty(string)) {
                                i++;
                                ContentValues contentValues3 = new ContentValues();
                                contentValues3.put("url", string);
                                contentValues3.put("title", query2.getString(1));
                                contentValues3.put("created", Integer.valueOf(query2.getInt(4)));
                                contentValues3.put("position", Integer.valueOf(i));
                                contentValues3.put("parent", Integer.valueOf(query2.getInt(5) + 1));
                                ContentValues contentValues4 = new ContentValues();
                                contentValues4.put("url_key", string);
                                contentValues4.put("favicon", query2.getBlob(2));
                                contentValues4.put("touch_icon", query2.getBlob(3));
                                sQLiteDatabase.insert("images", "thumbnail", contentValues4);
                                sQLiteDatabase.insert("bookmarks", "dirty", contentValues3);
                            }
                        }
                        query2.close();
                    }
                    Cursor query3 = writableDatabase.query(str2, new String[]{"url", "title", "visits", "date", "created"}, "visits > 0 OR bookmark = 0", null, null, null, null);
                    if (query3 != null) {
                        while (query3.moveToNext()) {
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
                        }
                        query3.close();
                    }
                    cursor = query3;
                    writableDatabase.delete(str2, null, null);
                    if (query3 != null) {
                        query3.close();
                    }
                    writableDatabase.close();
                    databaseHelper.close();
                    if (databasePath.delete()) {
                        return true;
                    }
                    databasePath.deleteOnExit();
                    return true;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    writableDatabase.close();
                    databaseHelper.close();
                    throw th;
                }
            }
            return false;
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
            this.this$0.mSyncHelper.createDatabase(sQLiteDatabase);
            if (!importFromBrowserProvider(sQLiteDatabase)) {
                createDefaultBookmarks(sQLiteDatabase);
            }
            enableSync(sQLiteDatabase);
            createOmniboxSuggestions(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onOpen(SQLiteDatabase sQLiteDatabase) {
            this.this$0.mSyncHelper.onDatabaseOpened(sQLiteDatabase);
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
                this.this$0.mSyncHelper.onAccountsChanged(sQLiteDatabase, new Account[0]);
                onCreate(sQLiteDatabase);
            }
        }
    }

    /* loaded from: b.zip:com/android/browser/provider/BrowserProvider2$OmniboxSuggestions.class */
    public interface OmniboxSuggestions {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "omnibox_suggestions");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/provider/BrowserProvider2$SuggestionsCursor.class */
    public static class SuggestionsCursor extends AbstractCursor {
        private static final String[] COLUMNS = {"_id", "suggest_intent_action", "suggest_intent_data", "suggest_text_1", "suggest_text_2", "suggest_text_2_url", "suggest_icon_1", "suggest_last_access_hint"};
        private final Cursor mSource;

        public SuggestionsCursor(Cursor cursor) {
            this.mSource = cursor;
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
        public String[] getColumnNames() {
            return COLUMNS;
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
            switch (i) {
                case 0:
                    return this.mSource.getLong(0);
                case 7:
                    return this.mSource.getLong(4);
                default:
                    throw new UnsupportedOperationException();
            }
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public short getShort(int i) {
            throw new UnsupportedOperationException();
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
                    return this.mSource.getInt(3) == 1 ? Integer.toString(2130837536) : Integer.toString(2130837559);
                case 7:
                    return Long.toString(System.currentTimeMillis());
                default:
                    return null;
            }
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public boolean isClosed() {
            return this.mSource.isClosed();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public boolean isNull(int i) {
            return this.mSource.isNull(i);
        }

        @Override // android.database.AbstractCursor, android.database.CrossProcessCursor
        public boolean onMove(int i, int i2) {
            return this.mSource.moveToPosition(i2);
        }
    }

    /* loaded from: b.zip:com/android/browser/provider/BrowserProvider2$Thumbnails.class */
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

    private String[] createCombinedQuery(Uri uri, String[] strArr, SQLiteQueryBuilder sQLiteQueryBuilder) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("deleted");
        sb.append(" = 0");
        Object[] selectionWithAccounts = getSelectionWithAccounts(uri, null, null);
        String str = (String) selectionWithAccounts[0];
        String[] strArr2 = (String[]) selectionWithAccounts[1];
        String[] strArr3 = null;
        if (str != null) {
            sb.append(" AND ").append(str);
            strArr3 = null;
            if (strArr2 != null) {
                strArr3 = new String[strArr2.length * 2];
                System.arraycopy(strArr2, 0, strArr3, 0, strArr2.length);
                System.arraycopy(strArr2, 0, strArr3, strArr2.length, strArr2.length);
            }
        }
        String sb2 = sb.toString();
        sQLiteQueryBuilder.setTables("bookmarks");
        sQLiteQueryBuilder.setTables(String.format("history LEFT OUTER JOIN (%s) bookmarks ON history.url = bookmarks.url LEFT OUTER JOIN images ON history.url = images.url_key", sQLiteQueryBuilder.buildQuery(null, sb2, null, null, null, null)));
        sQLiteQueryBuilder.setProjectionMap(COMBINED_HISTORY_PROJECTION_MAP);
        String buildQuery = sQLiteQueryBuilder.buildQuery(null, null, null, null, null, null);
        sQLiteQueryBuilder.setTables("bookmarks LEFT OUTER JOIN images ON bookmarks.url = images.url_key");
        sQLiteQueryBuilder.setProjectionMap(COMBINED_BOOKMARK_PROJECTION_MAP);
        sQLiteQueryBuilder.setTables("(" + sQLiteQueryBuilder.buildUnionQuery(new String[]{buildQuery, sQLiteQueryBuilder.buildQuery(null, sb2 + String.format(" AND %s NOT IN (SELECT %s FROM %s)", "url", "url", "history"), null, null, null, null)}, null, null) + ")");
        sQLiteQueryBuilder.setProjectionMap(null);
        return strArr3;
    }

    private Cursor doSuggestQuery(String str, String[] strArr, String str2) {
        String str3;
        Log.i("browser/BrowserProvider", "doSuggestQuery");
        if (TextUtils.isEmpty(strArr[0])) {
            str3 = "history.date != 0";
            strArr = null;
        } else {
            String str4 = strArr[0] + "%";
            if (!strArr[0].startsWith("http") && !strArr[0].startsWith("file")) {
                String[] strArr2 = {"http://" + str4, "http://www." + str4, "https://" + str4, "https://www." + str4, "%" + str4};
                Log.i("browser/BrowserProvider", "doSuggestQuery, selectionArgs: " + strArr2);
                SuggestionsCursor suggestionsCursor = new SuggestionsCursor(this.mOpenHelper.getReadableDatabase().query("v_omnibox_suggestions", new String[]{"_id", "url", "title", "bookmark"}, "url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ? OR title LIKE ?", strArr2, null, null, null, null));
                Log.i("browser/BrowserProvider", "doSuggestQuery, getCount: " + suggestionsCursor.getCount());
                return suggestionsCursor;
            }
            strArr[0] = str4;
            str3 = "history." + str;
        }
        return new SuggestionsCursor(this.mOpenHelper.getReadableDatabase().query("history LEFT OUTER JOIN bookmarks ON (history.url = bookmarks.url AND bookmarks.deleted=0 AND bookmarks.folder=0)", SUGGEST_PROJECTION, str3, strArr, null, null, null, null));
    }

    private String filterSearchClient(String str) {
        int indexOf = str.indexOf("client=");
        String str2 = str;
        if (indexOf > 0) {
            str2 = str;
            if (str.contains(".google.")) {
                int indexOf2 = str.indexOf(38, indexOf);
                str2 = indexOf2 > 0 ? str.substring(0, indexOf).concat(str.substring(indexOf2 + 1)) : str.substring(0, indexOf - 1);
            }
        }
        return str2;
    }

    private void filterSearchClient(String[] strArr) {
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                strArr[i] = filterSearchClient(strArr[i]);
            }
        }
    }

    private String[] getAccountNameAndType(long j) {
        Cursor query;
        if (j > 0 && (query = query(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, j), new String[]{"account_name", "account_type"}, null, null, null)) != null) {
            try {
                if (!query.moveToFirst()) {
                    query.close();
                    return null;
                }
                String string = query.getString(0);
                String string2 = query.getString(1);
                query.close();
                return new String[]{string, string2};
            } catch (Throwable th) {
                query.close();
                throw th;
            }
        }
        return null;
    }

    private static String getClientId(Context context) {
        String str;
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            Cursor query = context.getContentResolver().query(Uri.parse("content://com.google.settings/partner"), new String[]{"value"}, "name='client_id'", null, null);
            String str2 = "android-google";
            if (query != null) {
                str2 = "android-google";
                if (query.moveToNext()) {
                    cursor2 = query;
                    cursor = query;
                    str2 = query.getString(0);
                }
            }
            str = str2;
            if (query != null) {
                query.close();
                str = str2;
            }
        } catch (RuntimeException e) {
            str = "android-google";
            if (cursor2 != null) {
                cursor2.close();
                str = "android-google";
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return str;
    }

    private String getHomePage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("homepage", BrowserSettings.getFactoryResetUrlFromRes(context));
    }

    private long insertSearchesInTransaction(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
        String asString = contentValues.getAsString("search");
        if (TextUtils.isEmpty(asString)) {
            throw new IllegalArgumentException("Must include the SEARCH field");
        }
        Cursor cursor = null;
        try {
            Cursor query = sQLiteDatabase.query("searches", new String[]{"_id"}, "search=?", new String[]{asString}, null, null, null);
            if (!query.moveToNext()) {
                long insertOrThrow = sQLiteDatabase.insertOrThrow("searches", "search", contentValues);
                if (query != null) {
                    query.close();
                }
                return insertOrThrow;
            }
            long j = query.getLong(0);
            sQLiteDatabase.update("searches", contentValues, "_id=?", new String[]{Long.toString(j)});
            if (query != null) {
                query.close();
            }
            return j;
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public long insertSettingsInTransaction(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
        String asString = contentValues.getAsString("key");
        if (TextUtils.isEmpty(asString)) {
            throw new IllegalArgumentException("Must include the KEY field");
        }
        String[] strArr = {asString};
        Cursor cursor = null;
        try {
            Cursor query = sQLiteDatabase.query("settings", new String[]{"key"}, "key=?", strArr, null, null, null);
            if (!query.moveToNext()) {
                long insertOrThrow = sQLiteDatabase.insertOrThrow("settings", "value", contentValues);
                if (query != null) {
                    query.close();
                }
                return insertOrThrow;
            }
            long j = query.getLong(0);
            sQLiteDatabase.update("settings", contentValues, "key=?", strArr);
            if (query != null) {
                query.close();
            }
            return j;
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean isValidAccountName(long j, String str) {
        Log.e("browser/BrowserProvider", "BrowserProvider2.isValidAccountName parentId:" + j + " title:" + str);
        if (j <= 0 || str == null || str.length() == 0) {
            return true;
        }
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"title"}, "parent = ? AND deleted = ? AND folder = ?", new String[]{j + "", "0", "1"}, null);
                if (query == null || query.getCount() == 0) {
                    if (query != null) {
                        query.close();
                        return true;
                    }
                    return true;
                }
                do {
                    cursor = query;
                    cursor2 = query;
                    if (!query.moveToNext()) {
                        if (query != null) {
                            query.close();
                            return true;
                        }
                        return true;
                    }
                } while (!str.equals(query.getString(0)));
                if (query != null) {
                    query.close();
                    return false;
                }
                return false;
            } catch (IllegalStateException e) {
                Log.e("browser/BrowserProvider", e.getMessage());
                if (cursor != null) {
                    cursor.close();
                    return true;
                }
                return true;
            }
        } catch (Throwable th) {
            if (cursor2 != null) {
                cursor2.close();
            }
            throw th;
        }
    }

    private boolean isValidParent(String str, String str2, long j) {
        String[] accountNameAndType = getAccountNameAndType(j);
        return accountNameAndType != null && TextUtils.equals(str2, accountNameAndType[0]) && TextUtils.equals(str, accountNameAndType[1]);
    }

    static final String qualifyColumn(String str, String str2) {
        return str + "." + str2 + " AS " + str2;
    }

    public static CharSequence replaceSystemPropertyInString(Context context, CharSequence charSequence) {
        StringBuffer stringBuffer = new StringBuffer();
        int i = 0;
        String clientId = getClientId(context);
        int i2 = 0;
        while (i2 < charSequence.length()) {
            int i3 = i2;
            int i4 = i;
            if (charSequence.charAt(i2) == '{') {
                stringBuffer.append(charSequence.subSequence(i, i2));
                int i5 = i2;
                int i6 = i2;
                while (true) {
                    i3 = i2;
                    i4 = i5;
                    if (i6 >= charSequence.length()) {
                        break;
                    } else if (charSequence.charAt(i6) == '}') {
                        if (charSequence.subSequence(i2 + 1, i6).toString().equals("CLIENT_ID")) {
                            stringBuffer.append(clientId);
                        } else {
                            stringBuffer.append("unknown");
                        }
                        i4 = i6 + 1;
                        i3 = i6;
                    } else {
                        i6++;
                    }
                }
            }
            i2 = i3 + 1;
            i = i4;
        }
        if (charSequence.length() - i > 0) {
            stringBuffer.append(charSequence.subSequence(i, charSequence.length()));
        }
        return stringBuffer;
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

    private boolean setParentValues(long j, ContentValues contentValues) {
        String[] accountNameAndType = getAccountNameAndType(j);
        if (accountNameAndType == null) {
            return false;
        }
        contentValues.put("account_name", accountNameAndType[0]);
        contentValues.put("account_type", accountNameAndType[1]);
        return true;
    }

    private boolean shouldUpdateImages(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues) {
        Cursor query = sQLiteDatabase.query("images", new String[]{"favicon", "thumbnail", "touch_icon"}, "url_key=?", new String[]{str}, null, null, null);
        byte[] asByteArray = contentValues.getAsByteArray("favicon");
        byte[] asByteArray2 = contentValues.getAsByteArray("thumbnail");
        byte[] asByteArray3 = contentValues.getAsByteArray("touch_icon");
        try {
            if (query.getCount() <= 0) {
                boolean z = (asByteArray == null && asByteArray2 == null && asByteArray3 == null) ? false : true;
                query.close();
                return z;
            }
            while (query.moveToNext()) {
                if (asByteArray != null && !Arrays.equals(asByteArray, query.getBlob(0))) {
                    query.close();
                    return true;
                } else if (asByteArray2 != null && !Arrays.equals(asByteArray2, query.getBlob(1))) {
                    query.close();
                    return true;
                } else if (asByteArray3 != null && !Arrays.equals(asByteArray3, query.getBlob(2))) {
                    query.close();
                    return true;
                }
            }
            query.close();
            return false;
        } catch (Throwable th) {
            query.close();
            throw th;
        }
    }

    String appendAccountToSelection(Uri uri, String str) {
        String queryParameter = uri.getQueryParameter("account_name");
        String queryParameter2 = uri.getQueryParameter("account_type");
        if (TextUtils.isEmpty(queryParameter) ^ TextUtils.isEmpty(queryParameter2)) {
            throw new IllegalArgumentException("Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE for " + uri);
        }
        if (!TextUtils.isEmpty(queryParameter)) {
            StringBuilder sb = new StringBuilder("account_name=" + DatabaseUtils.sqlEscapeString(queryParameter) + " AND account_type=" + DatabaseUtils.sqlEscapeString(queryParameter2));
            if (!TextUtils.isEmpty(str)) {
                sb.append(" AND (");
                sb.append(str);
                sb.append(')');
            }
            return sb.toString();
        }
        return str;
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

    @Override // com.android.browser.provider.SQLiteContentProvider
    public int deleteInTransaction(Uri uri, String str, String[] strArr, boolean z) {
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        int i = 0;
        String str2 = str;
        String[] strArr2 = strArr;
        String str3 = str;
        String[] strArr3 = strArr;
        String str4 = str;
        String[] strArr4 = strArr;
        String str5 = str;
        String[] strArr5 = strArr;
        String str6 = str;
        String[] strArr6 = strArr;
        switch (match) {
            case 11:
                str6 = DatabaseUtils.concatenateWhere(str, "_id = ?");
                strArr6 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 10:
                i = writableDatabase.delete("thumbnails", str6, strArr6);
                break;
            case 1001:
                str2 = DatabaseUtils.concatenateWhere(str, "bookmarks._id=?");
                strArr2 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 1000:
                Object[] selectionWithAccounts = getSelectionWithAccounts(uri, str2, strArr2);
                int deleteBookmarks = deleteBookmarks((String) selectionWithAccounts[0], (String[]) selectionWithAccounts[1], z);
                pruneImages();
                i = deleteBookmarks;
                if (deleteBookmarks > 0) {
                    refreshWidgets();
                    i = deleteBookmarks;
                    break;
                }
                break;
            case 2001:
                str3 = DatabaseUtils.concatenateWhere(str, "history._id=?");
                strArr3 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 2000:
                filterSearchClient(strArr3);
                i = writableDatabase.delete("history", str3, strArr3);
                pruneImages();
                break;
            case 3001:
                str4 = DatabaseUtils.concatenateWhere(str, "searches._id=?");
                strArr4 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 3000:
                i = writableDatabase.delete("searches", str4, strArr4);
                break;
            case 4000:
                i = this.mSyncHelper.delete(writableDatabase, str, strArr);
                break;
            case 4001:
                i = this.mSyncHelper.delete(writableDatabase, "_id=" + ContentUris.parseId(uri) + " " + (str == null ? "" : " AND (" + str + ")"), strArr);
                break;
            case 9001:
                str5 = DatabaseUtils.concatenateWhere(str, "_id = CAST(? AS INTEGER)");
                strArr5 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 9000:
                String[] strArr7 = {"_id", "bookmark", "url"};
                SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
                String[] createCombinedQuery = createCombinedQuery(uri, strArr7, sQLiteQueryBuilder);
                if (strArr5 != null) {
                    createCombinedQuery = DatabaseUtils.appendSelectionArgs(createCombinedQuery, strArr5);
                }
                Cursor query = sQLiteQueryBuilder.query(writableDatabase, strArr7, str5, createCombinedQuery, null, null, null);
                while (query.moveToNext()) {
                    long j = query.getLong(0);
                    boolean z2 = query.getInt(1) != 0;
                    String string = query.getString(2);
                    if (z2) {
                        i += deleteBookmarks("_id=?", new String[]{Long.toString(j)}, z);
                        writableDatabase.delete("history", "url=?", new String[]{string});
                    } else {
                        i += writableDatabase.delete("history", "_id=?", new String[]{Long.toString(j)});
                    }
                }
                query.close();
                break;
            default:
                throw new UnsupportedOperationException("Unknown delete URI " + uri);
        }
        if (i > 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
        }
        return i;
    }

    ContentValues extractImageValues(ContentValues contentValues, String str) {
        ContentValues contentValues2 = null;
        if (contentValues.containsKey("favicon")) {
            contentValues2 = new ContentValues();
            contentValues2.put("favicon", contentValues.getAsByteArray("favicon"));
            contentValues.remove("favicon");
        }
        ContentValues contentValues3 = contentValues2;
        if (contentValues.containsKey("thumbnail")) {
            contentValues3 = contentValues2;
            if (contentValues2 == null) {
                contentValues3 = new ContentValues();
            }
            contentValues3.put("thumbnail", contentValues.getAsByteArray("thumbnail"));
            contentValues.remove("thumbnail");
        }
        ContentValues contentValues4 = contentValues3;
        if (contentValues.containsKey("touch_icon")) {
            contentValues4 = contentValues3;
            if (contentValues3 == null) {
                contentValues4 = new ContentValues();
            }
            contentValues4.put("touch_icon", contentValues.getAsByteArray("touch_icon"));
            contentValues.remove("touch_icon");
        }
        if (contentValues4 != null) {
            contentValues4.put("url_key", str);
        }
        return contentValues4;
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    public SQLiteOpenHelper getDatabaseHelper(Context context) {
        DatabaseHelper databaseHelper;
        synchronized (this) {
            if (this.mOpenHelper == null) {
                this.mOpenHelper = new DatabaseHelper(this, context);
            }
            databaseHelper = this.mOpenHelper;
        }
        return databaseHelper;
    }

    Object[] getSelectionWithAccounts(Uri uri, String str, String[] strArr) {
        String queryParameter = uri.getQueryParameter("acct_type");
        String queryParameter2 = uri.getQueryParameter("acct_name");
        boolean z = false;
        String str2 = str;
        String[] strArr2 = strArr;
        if (queryParameter != null) {
            z = false;
            str2 = str;
            strArr2 = strArr;
            if (queryParameter2 != null) {
                if (isNullAccount(queryParameter) || isNullAccount(queryParameter2)) {
                    str2 = DatabaseUtils.concatenateWhere(str, "account_name IS NULL AND account_type IS NULL");
                    strArr2 = strArr;
                    z = false;
                } else {
                    str2 = DatabaseUtils.concatenateWhere(str, "account_type=? AND account_name=? ");
                    strArr2 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{queryParameter, queryParameter2});
                    z = true;
                }
            }
        }
        return new Object[]{str2, strArr2, Boolean.valueOf(z)};
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

    int getUrlCount(SQLiteDatabase sQLiteDatabase, String str, String str2) {
        Cursor query = sQLiteDatabase.query(str, new String[]{"COUNT(*)"}, "url = ?", new String[]{str2}, null, null, null);
        int i = 0;
        try {
            if (query.moveToFirst()) {
                i = query.getInt(0);
            }
            query.close();
            return i;
        } catch (Throwable th) {
            query.close();
            throw th;
        }
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    public Uri insertInTransaction(Uri uri, ContentValues contentValues, boolean z) {
        long replaceOrThrow;
        boolean z2;
        String asString;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        int i = match;
        if (match == 9000) {
            Integer asInteger = contentValues.getAsInteger("bookmark");
            contentValues.remove("bookmark");
            if (asInteger == null || asInteger.intValue() == 0) {
                i = 2000;
            } else {
                i = 1000;
                contentValues.remove("date");
                contentValues.remove("visits");
                contentValues.remove("user_entered");
                contentValues.put("folder", (Integer) 0);
            }
        }
        switch (i) {
            case 10:
                replaceOrThrow = writableDatabase.replaceOrThrow("thumbnails", null, contentValues);
                break;
            case 1000:
                if (contentValues.containsKey("url") && (asString = contentValues.getAsString("url")) != null) {
                    contentValues.put("url", asString.trim());
                }
                if (!z) {
                    long currentTimeMillis = System.currentTimeMillis();
                    contentValues.put("created", Long.valueOf(currentTimeMillis));
                    contentValues.put("modified", Long.valueOf(currentTimeMillis));
                    contentValues.put("dirty", (Integer) 1);
                    boolean containsKey = !contentValues.containsKey("account_type") ? contentValues.containsKey("account_name") : true;
                    String asString2 = contentValues.getAsString("account_type");
                    String asString3 = contentValues.getAsString("account_name");
                    boolean containsKey2 = contentValues.containsKey("parent");
                    if (containsKey2 && containsKey) {
                        z2 = isValidParent(asString2, asString3, contentValues.getAsLong("parent").longValue());
                    } else {
                        z2 = containsKey2;
                        if (containsKey2) {
                            z2 = containsKey2;
                            if (!containsKey) {
                                z2 = setParentValues(contentValues.getAsLong("parent").longValue(), contentValues);
                            }
                        }
                    }
                    if (!z2) {
                        contentValues.put("parent", Long.valueOf(queryDefaultFolderId(asString3, asString2)));
                    }
                }
                if (!contentValues.containsKey("folder") || !contentValues.getAsBoolean("folder").booleanValue() || !contentValues.containsKey("parent") || !contentValues.containsKey("title") || isValidAccountName(contentValues.getAsLong("parent").longValue(), contentValues.getAsString("title"))) {
                    if (!contentValues.containsKey("position")) {
                        contentValues.put("position", Long.toString(Long.MIN_VALUE));
                    }
                    String asString4 = contentValues.getAsString("url");
                    ContentValues extractImageValues = extractImageValues(contentValues, asString4);
                    Boolean asBoolean = contentValues.getAsBoolean("folder");
                    if ((asBoolean == null || !asBoolean.booleanValue()) && extractImageValues != null && !TextUtils.isEmpty(asString4) && writableDatabase.update("images", extractImageValues, "url_key=?", new String[]{asString4}) == 0) {
                        writableDatabase.insertOrThrow("images", "favicon", extractImageValues);
                    }
                    replaceOrThrow = writableDatabase.insertOrThrow("bookmarks", "dirty", contentValues);
                    refreshWidgets();
                    break;
                } else {
                    return null;
                }
                break;
            case 2000:
                if (!contentValues.containsKey("created")) {
                    contentValues.put("created", Long.valueOf(System.currentTimeMillis()));
                }
                contentValues.put("url", filterSearchClient(contentValues.getAsString("url")));
                ContentValues extractImageValues2 = extractImageValues(contentValues, contentValues.getAsString("url"));
                if (extractImageValues2 != null) {
                    writableDatabase.insertOrThrow("images", "favicon", extractImageValues2);
                }
                replaceOrThrow = writableDatabase.insertOrThrow("history", "visits", contentValues);
                break;
            case 3000:
                replaceOrThrow = insertSearchesInTransaction(writableDatabase, contentValues);
                break;
            case 4000:
                replaceOrThrow = this.mSyncHelper.insert(writableDatabase, contentValues);
                break;
            case 8000:
                replaceOrThrow = 0;
                insertSettingsInTransaction(writableDatabase, contentValues);
                break;
            default:
                throw new UnsupportedOperationException("Unknown insert URI " + uri);
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

    @Override // com.android.browser.provider.SQLiteContentProvider
    public boolean isCallerSyncAdapter(Uri uri) {
        return uri.getBooleanQueryParameter("caller_is_syncadapter", false);
    }

    boolean isNullAccount(String str) {
        boolean z = true;
        if (str == null) {
            return true;
        }
        String trim = str.trim();
        if (trim.length() != 0) {
            z = trim.equals("null");
        }
        return z;
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

    int pruneImages() {
        return this.mOpenHelper.getWritableDatabase().delete("images", "url_key NOT IN (SELECT url FROM bookmarks WHERE url IS NOT NULL AND deleted == 0) AND url_key NOT IN (SELECT url FROM history WHERE url IS NOT NULL)", null);
    }

    /* JADX WARN: Code restructure failed: missing block: B:83:0x054d, code lost:
        if (r0 == 9001) goto L88;
     */
    @Override // android.content.ContentProvider
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String[] strArr3;
        String[] strArr4;
        String str3;
        String[] strArr5;
        String appendAccountToSelection;
        String[] strArr6;
        String str4;
        String str5;
        String[] strArr7;
        SQLiteDatabase readableDatabase = this.mOpenHelper.getReadableDatabase();
        int match = URI_MATCHER.match(uri);
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        String queryParameter = uri.getQueryParameter("limit");
        String queryParameter2 = uri.getQueryParameter("groupBy");
        String str6 = str;
        String[] strArr8 = strArr2;
        String str7 = str;
        String[] strArr9 = strArr2;
        String str8 = str;
        String[] strArr10 = strArr2;
        String str9 = str;
        String[] strArr11 = strArr2;
        switch (match) {
            case 11:
                str9 = DatabaseUtils.concatenateWhere(str, "_id = ?");
                strArr11 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 10:
                sQLiteQueryBuilder.setTables("thumbnails");
                strArr3 = strArr;
                str = str9;
                strArr4 = strArr11;
                str3 = str2;
                break;
            case 20:
                sQLiteQueryBuilder.setTables("v_omnibox_suggestions");
                strArr3 = strArr;
                strArr4 = strArr2;
                str3 = str2;
                break;
            case 30:
                String homePage = getHomePage(getContext());
                if (homePage == null) {
                    return null;
                }
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{"homepage"}, 1);
                matrixCursor.addRow(new String[]{homePage});
                return matrixCursor;
            case 1000:
            case 1001:
            case 1003:
                String str10 = str;
                if (!uri.getBooleanQueryParameter("show_deleted", false)) {
                    str10 = DatabaseUtils.concatenateWhere("deleted=0", str);
                }
                if (match == 1001) {
                    str5 = DatabaseUtils.concatenateWhere(str10, "bookmarks._id=?");
                    strArr7 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
                } else {
                    str5 = str10;
                    strArr7 = strArr2;
                    if (match == 1003) {
                        str5 = DatabaseUtils.concatenateWhere(str10, "bookmarks.parent=?");
                        strArr7 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
                    }
                }
                Object[] selectionWithAccounts = getSelectionWithAccounts(uri, str5, strArr7);
                str = (String) selectionWithAccounts[0];
                strArr4 = (String[]) selectionWithAccounts[1];
                boolean booleanValue = ((Boolean) selectionWithAccounts[2]).booleanValue();
                str3 = str2;
                if (TextUtils.isEmpty(str2)) {
                    str3 = booleanValue ? "position ASC, _id ASC" : "folder DESC, position ASC, _id ASC";
                }
                sQLiteQueryBuilder.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                sQLiteQueryBuilder.setTables("bookmarks LEFT OUTER JOIN images ON bookmarks.url = images.url_key");
                strArr3 = strArr;
                break;
            case 1002:
                String queryParameter3 = uri.getQueryParameter("acct_type");
                String queryParameter4 = uri.getQueryParameter("acct_name");
                boolean z = false;
                if (!isNullAccount(queryParameter3)) {
                    z = !isNullAccount(queryParameter4);
                }
                sQLiteQueryBuilder.setTables("bookmarks LEFT OUTER JOIN images ON bookmarks.url = images.url_key");
                String str11 = str2;
                if (TextUtils.isEmpty(str2)) {
                    str11 = z ? "position ASC, _id ASC" : "folder DESC, position ASC, _id ASC";
                }
                if (z) {
                    sQLiteQueryBuilder.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                    String buildQuery = sQLiteQueryBuilder.buildQuery(strArr, DatabaseUtils.concatenateWhere("account_type=? AND account_name=? AND parent = (SELECT _id FROM bookmarks WHERE sync3='bookmark_bar' AND account_type = ? AND account_name = ?) AND deleted=0", str), null, null, null, null);
                    String[] strArr12 = {queryParameter3, queryParameter4, queryParameter3, queryParameter4};
                    String[] strArr13 = strArr12;
                    if (strArr2 != null) {
                        strArr13 = DatabaseUtils.appendSelectionArgs(strArr12, strArr2);
                    }
                    String concatenateWhere = DatabaseUtils.concatenateWhere("account_type=? AND account_name=? AND sync3=?", str);
                    sQLiteQueryBuilder.setProjectionMap(OTHER_BOOKMARKS_PROJECTION_MAP);
                    String buildUnionQuery = sQLiteQueryBuilder.buildUnionQuery(new String[]{buildQuery, sQLiteQueryBuilder.buildQuery(strArr, concatenateWhere, null, null, null, null)}, str11, queryParameter);
                    String[] appendSelectionArgs = DatabaseUtils.appendSelectionArgs(strArr13, new String[]{queryParameter3, queryParameter4, "other_bookmarks"});
                    strArr6 = appendSelectionArgs;
                    str4 = buildUnionQuery;
                    if (strArr2 != null) {
                        strArr6 = DatabaseUtils.appendSelectionArgs(appendSelectionArgs, strArr2);
                        str4 = buildUnionQuery;
                    }
                } else {
                    sQLiteQueryBuilder.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                    String concatenateWhere2 = DatabaseUtils.concatenateWhere("parent=? AND deleted=0", str);
                    String[] strArr14 = {Long.toString(1L)};
                    strArr6 = strArr14;
                    if (strArr2 != null) {
                        strArr6 = DatabaseUtils.appendSelectionArgs(strArr14, strArr2);
                    }
                    str4 = sQLiteQueryBuilder.buildQuery(strArr, concatenateWhere2, null, null, str11, null);
                }
                Cursor rawQuery = readableDatabase.rawQuery(str4, strArr6);
                if (rawQuery != null) {
                    rawQuery.setNotificationUri(getContext().getContentResolver(), BrowserContract.AUTHORITY_URI);
                }
                return rawQuery;
            case 1004:
                return doSuggestQuery(str, strArr2, queryParameter);
            case 1005:
                long queryDefaultFolderId = queryDefaultFolderId(uri.getQueryParameter("acct_name"), uri.getQueryParameter("acct_type"));
                MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{"_id"});
                matrixCursor2.newRow().add(Long.valueOf(queryDefaultFolderId));
                return matrixCursor2;
            case 2001:
                str6 = DatabaseUtils.concatenateWhere(str, "history._id=?");
                strArr8 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 2000:
                filterSearchClient(strArr8);
                str3 = str2;
                if (str2 == null) {
                    str3 = "date DESC";
                }
                sQLiteQueryBuilder.setProjectionMap(HISTORY_PROJECTION_MAP);
                sQLiteQueryBuilder.setTables("history LEFT OUTER JOIN images ON history.url = images.url_key");
                strArr3 = strArr;
                str = str6;
                strArr4 = strArr8;
                break;
            case 3001:
                str7 = DatabaseUtils.concatenateWhere(str, "searches._id=?");
                strArr9 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 3000:
                sQLiteQueryBuilder.setTables("searches");
                sQLiteQueryBuilder.setProjectionMap(SEARCHES_PROJECTION_MAP);
                strArr3 = strArr;
                str = str7;
                strArr4 = strArr9;
                str3 = str2;
                break;
            case 4000:
                return this.mSyncHelper.query(readableDatabase, strArr, str, strArr2, str2);
            case 4001:
                return this.mSyncHelper.query(readableDatabase, strArr, "_id=" + ContentUris.parseId(uri) + " " + (appendAccountToSelection(uri, str) == null ? "" : " AND (" + appendAccountToSelection + ")"), strArr2, str2);
            case 5000:
                sQLiteQueryBuilder.setTables("images");
                sQLiteQueryBuilder.setProjectionMap(IMAGES_PROJECTION_MAP);
                strArr3 = strArr;
                strArr4 = strArr2;
                str3 = str2;
                break;
            case 6001:
            case 9001:
                str8 = DatabaseUtils.concatenateWhere(str, "_id = CAST(? AS INTEGER)");
                strArr10 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 6000:
            case 9000:
                if (match != 9000) {
                    strArr5 = strArr;
                    break;
                }
                strArr5 = strArr;
                if (strArr == null) {
                    strArr5 = Browser.HISTORY_PROJECTION;
                }
                strArr4 = createCombinedQuery(uri, strArr5, sQLiteQueryBuilder);
                if (strArr10 != null) {
                    strArr4 = DatabaseUtils.appendSelectionArgs(strArr4, strArr10);
                    strArr3 = strArr5;
                    str = str8;
                    str3 = str2;
                    break;
                } else {
                    strArr3 = strArr5;
                    str = str8;
                    str3 = str2;
                    break;
                }
            case 7000:
                sQLiteQueryBuilder.setTables("v_accounts");
                sQLiteQueryBuilder.setProjectionMap(ACCOUNTS_PROJECTION_MAP);
                String str12 = str;
                if ("false".equals(uri.getQueryParameter("allowEmptyAccounts"))) {
                    str12 = DatabaseUtils.concatenateWhere(str, "0 < ( SELECT count(*) FROM bookmarks WHERE deleted = 0 AND folder = 0   AND (     v_accounts.account_name = bookmarks.account_name     OR (v_accounts.account_name IS NULL AND bookmarks.account_name IS NULL)   )   AND (     v_accounts.account_type = bookmarks.account_type     OR (v_accounts.account_type IS NULL AND bookmarks.account_type IS NULL)   ) )");
                }
                strArr3 = strArr;
                str = str12;
                strArr4 = strArr2;
                str3 = str2;
                if (str2 == null) {
                    str3 = "account_name IS NOT NULL DESC, account_name ASC";
                    strArr4 = strArr2;
                    str = str12;
                    strArr3 = strArr;
                    break;
                }
                break;
            case 8000:
                sQLiteQueryBuilder.setTables("settings");
                sQLiteQueryBuilder.setProjectionMap(SETTINGS_PROJECTION_MAP);
                strArr3 = strArr;
                strArr4 = strArr2;
                str3 = str2;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URL " + uri.toString());
        }
        Cursor query = sQLiteQueryBuilder.query(readableDatabase, strArr3, str, strArr4, queryParameter2, null, str3, queryParameter);
        query.setNotificationUri(getContext().getContentResolver(), BrowserContract.AUTHORITY_URI);
        return query;
    }

    long queryDefaultFolderId(String str, String str2) {
        if (isNullAccount(str) || isNullAccount(str2)) {
            return 1L;
        }
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

    void refreshWidgets() {
        this.mUpdateWidgets = true;
    }

    @VisibleForTesting
    public void setWidgetObserver(ContentObserver contentObserver) {
        this.mWidgetObserver = contentObserver;
    }

    boolean shouldNotifyLegacy(Uri uri) {
        return uri.getPathSegments().contains("history") || uri.getPathSegments().contains("bookmarks") || uri.getPathSegments().contains("searches");
    }

    @Override // com.android.browser.provider.SQLiteContentProvider
    protected boolean syncToNetwork(Uri uri) {
        return ("com.android.browser.provider".equals(uri.getAuthority()) && uri.getPathSegments().contains("bookmarks")) ? this.mSyncToNetwork : "MtkBrowserProvider".equals(uri.getAuthority());
    }

    int updateBookmarksInTransaction(ContentValues contentValues, String str, String[] strArr, boolean z) {
        String str2;
        int update;
        int i = 0;
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        Cursor query = writableDatabase.query("bookmarks", new String[]{"_id", "version", "url", "title", "folder", "account_name", "account_type"}, str, strArr, null, null, null);
        boolean containsKey = contentValues.containsKey("parent");
        String str3 = null;
        String str4 = null;
        if (containsKey) {
            Cursor query2 = writableDatabase.query("bookmarks", new String[]{"account_name", "account_type"}, "_id = ?", new String[]{Long.toString(contentValues.getAsLong("parent").longValue())}, null, null, null);
            if (query2.moveToFirst()) {
                str3 = query2.getString(0);
                str4 = query2.getString(1);
            }
            query2.close();
            str2 = str3;
        } else {
            str2 = null;
            str4 = null;
            if (!contentValues.containsKey("account_name")) {
                str2 = null;
                str4 = null;
                if (contentValues.containsKey("account_type")) {
                    str2 = null;
                    str4 = null;
                }
            }
        }
        try {
            String[] strArr2 = new String[1];
            if (!z) {
                contentValues.put("modified", Long.valueOf(System.currentTimeMillis()));
                contentValues.put("dirty", (Integer) 1);
            }
            boolean containsKey2 = contentValues.containsKey("url");
            String str5 = null;
            if (containsKey2) {
                str5 = contentValues.getAsString("url");
            }
            ContentValues extractImageValues = extractImageValues(contentValues, str5);
            while (query.moveToNext()) {
                long j = query.getLong(0);
                strArr2[0] = Long.toString(j);
                String string = query.getString(5);
                String string2 = query.getString(6);
                if (!containsKey || (TextUtils.equals(string, str2) && TextUtils.equals(string2, str4))) {
                    if (!z) {
                        contentValues.put("version", Long.valueOf(query.getLong(1) + 1));
                    }
                    update = i + writableDatabase.update("bookmarks", contentValues, "_id=?", strArr2);
                } else {
                    ContentValues valuesFromCursor = valuesFromCursor(query);
                    valuesFromCursor.putAll(contentValues);
                    valuesFromCursor.remove("_id");
                    valuesFromCursor.remove("version");
                    valuesFromCursor.put("account_name", str2);
                    valuesFromCursor.put("account_type", str4);
                    long parseId = ContentUris.parseId(insertInTransaction(BrowserContract.Bookmarks.CONTENT_URI, valuesFromCursor, z));
                    int i2 = i;
                    if (query.getInt(4) != 0) {
                        ContentValues contentValues2 = new ContentValues(1);
                        contentValues2.put("parent", Long.valueOf(parseId));
                        i2 = i + updateBookmarksInTransaction(contentValues2, "parent=?", new String[]{Long.toString(j)}, z);
                    }
                    deleteInTransaction(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, j), null, null, z);
                    update = i2 + 1;
                }
                i = update;
                if (extractImageValues != null) {
                    String str6 = str5;
                    if (!containsKey2) {
                        str6 = query.getString(2);
                        extractImageValues.put("url_key", str6);
                    }
                    i = update;
                    str5 = str6;
                    if (!TextUtils.isEmpty(str6)) {
                        strArr2[0] = str6;
                        i = update;
                        str5 = str6;
                        if (writableDatabase.update("images", extractImageValues, "url_key=?", strArr2) == 0) {
                            writableDatabase.insert("images", "favicon", extractImageValues);
                            i = update;
                            str5 = str6;
                        }
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

    int updateHistoryInTransaction(ContentValues contentValues, String str, String[] strArr) {
        int i = 0;
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
            while (query.moveToNext()) {
                strArr2[0] = query.getString(0);
                int update = i + writableDatabase.update("history", contentValues, "_id=?", strArr2);
                i = update;
                if (extractImageValues != null) {
                    String str4 = str3;
                    if (!containsKey) {
                        str4 = query.getString(1);
                        extractImageValues.put("url_key", str4);
                    }
                    strArr2[0] = str4;
                    i = update;
                    str3 = str4;
                    if (writableDatabase.update("images", extractImageValues, "url_key=?", strArr2) == 0) {
                        writableDatabase.insert("images", "favicon", extractImageValues);
                        i = update;
                        str3 = str4;
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

    /* JADX WARN: Code restructure failed: missing block: B:5:0x0023, code lost:
        if (r0 == 9001) goto L65;
     */
    @Override // com.android.browser.provider.SQLiteContentProvider
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int updateInTransaction(Uri uri, ContentValues contentValues, String str, String[] strArr, boolean z) {
        int i;
        String asString;
        int update;
        String appendAccountToSelection;
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        if (match != 9000) {
            i = match;
        }
        Integer asInteger = contentValues.getAsInteger("bookmark");
        contentValues.remove("bookmark");
        if (asInteger == null || asInteger.intValue() == 0) {
            i = match == 9000 ? 2000 : 2001;
        } else {
            i = match == 9000 ? 1000 : 1001;
            contentValues.remove("date");
            contentValues.remove("visits");
            contentValues.remove("user_entered");
        }
        String str2 = str;
        String[] strArr2 = strArr;
        String str3 = str;
        String[] strArr3 = strArr;
        switch (i) {
            case 10:
                update = writableDatabase.update("thumbnails", contentValues, str, strArr);
                break;
            case 30:
                return (contentValues == null || (asString = contentValues.getAsString("homepage")) == null || !setHomePage(getContext(), asString)) ? 0 : 1;
            case 1001:
                str2 = DatabaseUtils.concatenateWhere(str, "bookmarks._id=?");
                strArr2 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 1000:
                Object[] selectionWithAccounts = getSelectionWithAccounts(uri, str2, strArr2);
                int updateBookmarksInTransaction = updateBookmarksInTransaction(contentValues, (String) selectionWithAccounts[0], (String[]) selectionWithAccounts[1], z);
                update = updateBookmarksInTransaction;
                if (updateBookmarksInTransaction > 0) {
                    refreshWidgets();
                    update = updateBookmarksInTransaction;
                    break;
                }
                break;
            case 2001:
                str3 = DatabaseUtils.concatenateWhere(str, "history._id=?");
                strArr3 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
            case 2000:
                update = updateHistoryInTransaction(contentValues, str3, strArr3);
                break;
            case 3000:
                update = writableDatabase.update("searches", contentValues, str, strArr);
                break;
            case 4000:
                update = this.mSyncHelper.update(this.mDb, contentValues, appendAccountToSelection(uri, str), strArr);
                break;
            case 4001:
                update = this.mSyncHelper.update(this.mDb, contentValues, "_id=" + ContentUris.parseId(uri) + " " + (appendAccountToSelection(uri, str) == null ? "" : " AND (" + appendAccountToSelection + ")"), strArr);
                break;
            case 5000:
                String asString2 = contentValues.getAsString("url_key");
                if (TextUtils.isEmpty(asString2)) {
                    throw new IllegalArgumentException("Images.URL is required");
                }
                if (shouldUpdateImages(writableDatabase, asString2, contentValues)) {
                    int update2 = writableDatabase.update("images", contentValues, "url_key=?", new String[]{asString2});
                    int i2 = update2;
                    if (update2 == 0) {
                        writableDatabase.insertOrThrow("images", "favicon", contentValues);
                        i2 = 1;
                    }
                    boolean z2 = false;
                    if (getUrlCount(writableDatabase, "bookmarks", asString2) > 0) {
                        postNotifyUri(BrowserContract.Bookmarks.CONTENT_URI);
                        z2 = contentValues.containsKey("favicon");
                        refreshWidgets();
                    }
                    if (getUrlCount(writableDatabase, "history", asString2) > 0) {
                        postNotifyUri(BrowserContract.History.CONTENT_URI);
                        z2 = contentValues.containsKey("favicon");
                    }
                    if (pruneImages() > 0 || z2) {
                        postNotifyUri(LEGACY_AUTHORITY_URI);
                    }
                    this.mSyncToNetwork = false;
                    return i2;
                }
                return 0;
            case 7000:
                this.mSyncHelper.onAccountsChanged(this.mDb, AccountManager.get(getContext()).getAccounts());
                update = 0;
                break;
            default:
                throw new UnsupportedOperationException("Unknown update URI " + uri);
        }
        pruneImages();
        if (update > 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
        }
        return update;
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
}
