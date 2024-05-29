package com.android.browser.provider;

import android.app.backup.BackupManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import com.android.browser.BrowserSettings;
import com.android.browser.search.SearchEngine;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: b.zip:com/android/browser/provider/BrowserProvider.class */
public class BrowserProvider extends ContentProvider {
    private static final Pattern STRIP_URL_PATTERN;
    private String[] SUGGEST_ARGS = new String[5];
    private BackupManager mBackupManager;
    private int mMaxSuggestionLongSize;
    private int mMaxSuggestionShortSize;
    private SQLiteOpenHelper mOpenHelper;
    private BrowserSettings mSettings;
    static final String[] TABLE_NAMES = {"bookmarks", "searches", "bookmark_folders"};
    private static final String[] SUGGEST_PROJECTION = {"_id", "url", "title", "bookmark", "user_entered"};
    private static final String[] COLUMNS = {"_id", "suggest_intent_action", "suggest_intent_data", "suggest_text_1", "suggest_text_2", "suggest_text_2_url", "suggest_icon_1", "suggest_icon_2", "suggest_intent_query", "suggest_intent_extra_data"};
    private static final UriMatcher URI_MATCHER = new UriMatcher(-1);

    /* loaded from: b.zip:com/android/browser/provider/BrowserProvider$DatabaseHelper.class */
    static class DatabaseHelper extends SQLiteOpenHelper {
        private Context mContext;

        public DatabaseHelper(Context context) {
            super(context, "browser.db", (SQLiteDatabase.CursorFactory) null, 24);
            this.mContext = context;
        }

        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.browser.provider.BrowserProvider$DatabaseHelper$1] */
        private void removeGears() {
            new Thread(this) { // from class: com.android.browser.provider.BrowserProvider.DatabaseHelper.1
                final DatabaseHelper this$1;

                {
                    this.this$1 = this;
                }

                private void deleteDirectory(File file) {
                    File[] listFiles = file.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        if (listFiles[i].isDirectory()) {
                            deleteDirectory(listFiles[i]);
                        }
                        listFiles[i].delete();
                    }
                    file.delete();
                }

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    Process.setThreadPriority(10);
                    String str = this.this$1.mContext.getApplicationInfo().dataDir;
                    File file = new File(str + File.separator + "app_plugins");
                    if (file.exists()) {
                        File[] listFiles = file.listFiles(new FilenameFilter(this) { // from class: com.android.browser.provider.BrowserProvider.DatabaseHelper.1.1
                            final AnonymousClass1 this$2;

                            {
                                this.this$2 = this;
                            }

                            @Override // java.io.FilenameFilter
                            public boolean accept(File file2, String str2) {
                                return str2.startsWith("gears");
                            }
                        });
                        for (int i = 0; i < listFiles.length; i++) {
                            if (listFiles[i].isDirectory()) {
                                deleteDirectory(listFiles[i]);
                            } else {
                                listFiles[i].delete();
                            }
                        }
                        File file2 = new File(str + File.separator + "gears");
                        if (file2.exists()) {
                            deleteDirectory(file2);
                        }
                    }
                }
            }.start();
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            CharSequence[] textArray;
            sQLiteDatabase.execSQL("CREATE TABLE bookmarks (_id INTEGER PRIMARY KEY,title TEXT,url TEXT NOT NULL,visits INTEGER,date LONG,created LONG,description TEXT,bookmark INTEGER,favicon BLOB DEFAULT NULL,thumbnail BLOB DEFAULT NULL,touch_icon BLOB DEFAULT NULL,user_entered INTEGER);");
            int length = this.mContext.getResources().getTextArray(2131230833).length;
            for (int i = 0; i < length; i += 2) {
                try {
                    sQLiteDatabase.execSQL("INSERT INTO bookmarks (title, url, visits, date, created, bookmark) VALUES('" + textArray[i] + "', '" + BrowserProvider.replaceSystemPropertyInString(this.mContext, textArray[i + 1]) + "', 0, 0, 0, 1);");
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
            sQLiteDatabase.execSQL("CREATE TABLE searches (_id INTEGER PRIMARY KEY,search TEXT,date LONG);");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            Log.w("BrowserProvider", "Upgrading database from version " + i + " to " + i2);
            if (i == 18) {
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS labels");
            }
            if (i <= 19) {
                sQLiteDatabase.execSQL("ALTER TABLE bookmarks ADD COLUMN thumbnail BLOB DEFAULT NULL;");
            }
            if (i < 21) {
                sQLiteDatabase.execSQL("ALTER TABLE bookmarks ADD COLUMN touch_icon BLOB DEFAULT NULL;");
            }
            if (i < 22) {
                sQLiteDatabase.execSQL("DELETE FROM bookmarks WHERE (bookmark = 0 AND url LIKE \"%.google.%client=ms-%\")");
                removeGears();
            }
            if (i < 23) {
                sQLiteDatabase.execSQL("ALTER TABLE bookmarks ADD COLUMN user_entered INTEGER;");
            }
            if (i >= 24) {
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS bookmarks");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS searches");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS bookmark_folders");
                onCreate(sQLiteDatabase);
                return;
            }
            sQLiteDatabase.execSQL("DELETE FROM bookmarks WHERE url IS NULL;");
            sQLiteDatabase.execSQL("ALTER TABLE bookmarks RENAME TO bookmarks_temp;");
            sQLiteDatabase.execSQL("CREATE TABLE bookmarks (_id INTEGER PRIMARY KEY,title TEXT,url TEXT NOT NULL,visits INTEGER,date LONG,created LONG,description TEXT,bookmark INTEGER,favicon BLOB DEFAULT NULL,thumbnail BLOB DEFAULT NULL,touch_icon BLOB DEFAULT NULL,user_entered INTEGER,folder_id INTEGER DEFAULT 0);");
            sQLiteDatabase.execSQL("INSERT INTO bookmarks SELECT * FROM bookmarks_temp;");
            sQLiteDatabase.execSQL("DROP TABLE bookmarks_temp;");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/provider/BrowserProvider$MySuggestionCursor.class */
    public class MySuggestionCursor extends AbstractCursor {
        private int mHistoryCount;
        private Cursor mHistoryCursor;
        private boolean mIncludeWebSearch;
        private String mString;
        private Cursor mSuggestCursor;
        private int mSuggestIntentExtraDataId;
        private int mSuggestQueryId;
        private int mSuggestText1Id;
        private int mSuggestText2Id;
        private int mSuggestText2UrlId;
        private int mSuggestionCount;
        final BrowserProvider this$0;

        public MySuggestionCursor(BrowserProvider browserProvider, Cursor cursor, Cursor cursor2, String str) {
            boolean z = false;
            this.this$0 = browserProvider;
            this.mHistoryCursor = cursor;
            this.mSuggestCursor = cursor2;
            this.mHistoryCount = cursor != null ? cursor.getCount() : 0;
            this.mSuggestionCount = cursor2 != null ? cursor2.getCount() : 0;
            if (this.mSuggestionCount > browserProvider.mMaxSuggestionLongSize - this.mHistoryCount) {
                this.mSuggestionCount = browserProvider.mMaxSuggestionLongSize - this.mHistoryCount;
            }
            this.mString = str;
            this.mIncludeWebSearch = str.length() > 0 ? true : z;
            if (this.mSuggestCursor == null) {
                this.mSuggestText1Id = -1;
                this.mSuggestText2Id = -1;
                this.mSuggestText2UrlId = -1;
                this.mSuggestQueryId = -1;
                this.mSuggestIntentExtraDataId = -1;
                return;
            }
            this.mSuggestText1Id = this.mSuggestCursor.getColumnIndex("suggest_text_1");
            this.mSuggestText2Id = this.mSuggestCursor.getColumnIndex("suggest_text_2");
            this.mSuggestText2UrlId = this.mSuggestCursor.getColumnIndex("suggest_text_2_url");
            this.mSuggestQueryId = this.mSuggestCursor.getColumnIndex("suggest_intent_query");
            this.mSuggestIntentExtraDataId = this.mSuggestCursor.getColumnIndex("suggest_intent_extra_data");
        }

        /* JADX WARN: Code restructure failed: missing block: B:5:0x0018, code lost:
            if (android.text.TextUtils.getTrimmedLength(r0) == 0) goto L8;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        private String getHistoryTitle() {
            String stripUrl;
            String string = this.mHistoryCursor.getString(2);
            if (!TextUtils.isEmpty(string)) {
                stripUrl = string;
            }
            stripUrl = BrowserProvider.stripUrl(this.mHistoryCursor.getString(1));
            return stripUrl;
        }

        private String getHistoryUrl() {
            String string = this.mHistoryCursor.getString(2);
            if (TextUtils.isEmpty(string) || TextUtils.getTrimmedLength(string) == 0) {
                return null;
            }
            return BrowserProvider.stripUrl(this.mHistoryCursor.getString(1));
        }

        @Override // android.database.AbstractCursor, android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            super.close();
            if (this.mHistoryCursor != null) {
                this.mHistoryCursor.close();
                this.mHistoryCursor = null;
            }
            if (this.mSuggestCursor != null) {
                this.mSuggestCursor.close();
                this.mSuggestCursor = null;
            }
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public void deactivate() {
            if (this.mHistoryCursor != null) {
                this.mHistoryCursor.deactivate();
            }
            if (this.mSuggestCursor != null) {
                this.mSuggestCursor.deactivate();
            }
            super.deactivate();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public String[] getColumnNames() {
            return BrowserProvider.COLUMNS;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public int getCount() {
            return this.mIncludeWebSearch ? this.mHistoryCount + this.mSuggestionCount + 1 : this.mHistoryCount + this.mSuggestionCount;
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
            if (this.mPos == -1 || i != 0) {
                throw new UnsupportedOperationException();
            }
            return this.mPos;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public short getShort(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public String getString(int i) {
            boolean z;
            boolean z2;
            if (this.mPos == -1 || this.mHistoryCursor == null) {
                return null;
            }
            if (this.mIncludeWebSearch) {
                if (this.mHistoryCount == 0 && this.mPos == 0) {
                    z2 = false;
                } else {
                    z2 = true;
                    if (this.mHistoryCount > 0) {
                        if (this.mPos == 0) {
                            z2 = true;
                        } else {
                            z2 = true;
                            if (this.mPos == 1) {
                                z2 = false;
                            }
                        }
                    }
                }
                z = z2;
                if (z2) {
                    z = this.mPos - 1 < this.mHistoryCount ? true : true;
                }
            } else {
                z = this.mPos < this.mHistoryCount ? true : true;
            }
            switch (i) {
                case 1:
                    return z ? "android.intent.action.VIEW" : "android.intent.action.SEARCH";
                case 2:
                    if (z) {
                        return this.mHistoryCursor.getString(1);
                    }
                    return null;
                case 3:
                    if (z) {
                        if (z) {
                            return getHistoryTitle();
                        }
                        if (this.mSuggestText1Id == -1) {
                            return null;
                        }
                        return this.mSuggestCursor.getString(this.mSuggestText1Id);
                    }
                    return this.mString;
                case 4:
                    if (z) {
                        if (z || this.mSuggestText2Id == -1) {
                            return null;
                        }
                        return this.mSuggestCursor.getString(this.mSuggestText2Id);
                    }
                    return this.this$0.getContext().getString(2131493226);
                case 5:
                    if (z) {
                        if (z) {
                            return getHistoryUrl();
                        }
                        if (this.mSuggestText2UrlId == -1) {
                            return null;
                        }
                        return this.mSuggestCursor.getString(this.mSuggestText2UrlId);
                    }
                    return null;
                case 6:
                    if (!z) {
                        Integer num = 2130837582;
                        return num.toString();
                    } else if (this.mHistoryCursor.getInt(3) == 1) {
                        Integer num2 = 2130837579;
                        return num2.toString();
                    } else {
                        Integer num3 = 2130837581;
                        return num3.toString();
                    }
                case 7:
                    return "0";
                case 8:
                    if (z) {
                        if (z) {
                            return this.mHistoryCursor.getString(1);
                        }
                        if (this.mSuggestQueryId == -1) {
                            return null;
                        }
                        return this.mSuggestCursor.getString(this.mSuggestQueryId);
                    }
                    return this.mString;
                case 9:
                    if (!z || z || this.mSuggestIntentExtraDataId == -1) {
                        return null;
                    }
                    return this.mSuggestCursor.getString(this.mSuggestIntentExtraDataId);
                default:
                    return null;
            }
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public boolean isNull(int i) {
            throw new UnsupportedOperationException();
        }

        @Override // android.database.AbstractCursor, android.database.CrossProcessCursor
        public boolean onMove(int i, int i2) {
            if (this.mHistoryCursor == null) {
                return false;
            }
            int i3 = i2;
            if (this.mIncludeWebSearch) {
                if (this.mHistoryCount == 0 && i2 == 0) {
                    return true;
                }
                if (this.mHistoryCount > 0) {
                    if (i2 == 0) {
                        this.mHistoryCursor.moveToPosition(0);
                        return true;
                    } else if (i2 == 1) {
                        return true;
                    }
                }
                i3 = i2 - 1;
            }
            if (this.mHistoryCount > i3) {
                this.mHistoryCursor.moveToPosition(i3);
                return true;
            }
            this.mSuggestCursor.moveToPosition(i3 - this.mHistoryCount);
            return true;
        }

        @Override // android.database.AbstractCursor, android.database.Cursor
        public boolean requery() {
            boolean z = false;
            boolean requery = this.mHistoryCursor != null ? this.mHistoryCursor.requery() : false;
            if (this.mSuggestCursor != null) {
                z = this.mSuggestCursor.requery();
            }
            return requery | z;
        }
    }

    static {
        URI_MATCHER.addURI("MtkBrowserProvider", TABLE_NAMES[0], 0);
        URI_MATCHER.addURI("MtkBrowserProvider", TABLE_NAMES[0] + "/#", 10);
        URI_MATCHER.addURI("MtkBrowserProvider", TABLE_NAMES[1], 1);
        URI_MATCHER.addURI("MtkBrowserProvider", TABLE_NAMES[1] + "/#", 11);
        URI_MATCHER.addURI("MtkBrowserProvider", TABLE_NAMES[2], 2);
        URI_MATCHER.addURI("MtkBrowserProvider", TABLE_NAMES[2] + "/#", 12);
        URI_MATCHER.addURI("MtkBrowserProvider", "search_suggest_query", 20);
        URI_MATCHER.addURI("MtkBrowserProvider", TABLE_NAMES[0] + "/search_suggest_query", 21);
        STRIP_URL_PATTERN = Pattern.compile("^(http://)(.*?)(/$)?");
    }

    private Cursor doSuggestQuery(String str, String[] strArr, boolean z) {
        String str2;
        String[] strArr2;
        SearchEngine searchEngine;
        if (strArr[0] == null || strArr[0].equals("")) {
            return new MySuggestionCursor(this, null, null, "");
        }
        String str3 = strArr[0] + "%";
        if (strArr[0].startsWith("http") || strArr[0].startsWith("file")) {
            str2 = str;
            strArr2 = new String[]{str3};
        } else {
            this.SUGGEST_ARGS[0] = "http://" + str3;
            this.SUGGEST_ARGS[1] = "http://www." + str3;
            this.SUGGEST_ARGS[2] = "https://" + str3;
            this.SUGGEST_ARGS[3] = "https://www." + str3;
            this.SUGGEST_ARGS[4] = str3;
            strArr2 = this.SUGGEST_ARGS;
            str2 = "(url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ? OR title LIKE ?) AND (bookmark = 1 OR user_entered = 1)";
        }
        Cursor query = this.mOpenHelper.getReadableDatabase().query(TABLE_NAMES[0], SUGGEST_PROJECTION, str2, strArr2, null, null, "visits DESC, date DESC", Integer.toString(this.mMaxSuggestionLongSize));
        return (z || Patterns.WEB_URL.matcher(strArr[0]).matches()) ? new MySuggestionCursor(this, query, null, "") : (strArr2 == null || strArr2.length <= 1 || query.getCount() >= 2 || (searchEngine = this.mSettings.getSearchEngine()) == null || !searchEngine.supportsSuggestions()) ? new MySuggestionCursor(this, query, null, strArr[0]) : new MySuggestionCursor(this, query, searchEngine.getSuggestions(getContext(), strArr[0]), strArr[0]);
    }

    public static String getClientId(ContentResolver contentResolver) {
        String str;
        String str2;
        Cursor cursor = null;
        Cursor cursor2 = null;
        Cursor cursor3 = null;
        Cursor cursor4 = null;
        Cursor cursor5 = null;
        try {
            Cursor query = contentResolver.query(Uri.parse("content://com.google.settings/partner"), new String[]{"value"}, "name='search_client_id'", null, null);
            if (query == null || !query.moveToNext()) {
                Cursor query2 = contentResolver.query(Uri.parse("content://com.google.settings/partner"), new String[]{"value"}, "name='client_id'", null, null);
                cursor3 = query2;
                str2 = "android-google";
                if (query2 != null) {
                    cursor3 = query2;
                    str2 = "android-google";
                    if (query2.moveToNext()) {
                        str2 = "ms-" + query2.getString(0);
                        cursor3 = query2;
                    }
                }
            } else {
                str2 = query.getString(0);
            }
            if (cursor3 != null) {
                cursor3.close();
            }
            str = str2;
            if (query != null) {
                query.close();
                str = str2;
            }
        } catch (RuntimeException e) {
            if (0 != 0) {
                cursor.close();
            }
            str = "android-google";
            if (0 != 0) {
                cursor5.close();
                str = "android-google";
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor2.close();
            }
            if (0 != 0) {
                cursor4.close();
            }
            throw th;
        }
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static CharSequence replaceSystemPropertyInString(Context context, CharSequence charSequence) {
        StringBuffer stringBuffer = new StringBuffer();
        int i = 0;
        String clientId = getClientId(context.getContentResolver());
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

    /* JADX INFO: Access modifiers changed from: private */
    public static String stripUrl(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = STRIP_URL_PATTERN.matcher(str);
        return (matcher.matches() && matcher.groupCount() == 3) ? matcher.group(2) : str;
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x0043, code lost:
        if (r0 == 11) goto L23;
     */
    @Override // android.content.ContentProvider
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int delete(Uri uri, String str, String[] strArr) {
        String sb;
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        if (match == -1 || match == 20) {
            throw new IllegalArgumentException("Unknown URL");
        }
        boolean z = match == 10;
        String str2 = null;
        if (!z) {
            sb = str;
        }
        StringBuilder sb2 = new StringBuilder();
        if (str != null && str.length() > 0) {
            sb2.append("( ");
            sb2.append(str);
            sb2.append(" ) AND ");
        }
        str2 = uri.getPathSegments().get(1);
        sb2.append("_id = ");
        sb2.append(str2);
        sb = sb2.toString();
        ContentResolver contentResolver = getContext().getContentResolver();
        if (z) {
            Cursor query = contentResolver.query(Browser.BOOKMARKS_URI, new String[]{"bookmark"}, "_id = " + str2, null, null);
            if (query.moveToNext() && query.getInt(0) != 0) {
                this.mBackupManager.dataChanged();
            }
            query.close();
        }
        int delete = writableDatabase.delete(TABLE_NAMES[match % 10], sb, strArr);
        contentResolver.notifyChange(uri, null);
        return delete;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case 0:
                return "vnd.android.cursor.dir/bookmark";
            case 1:
                return "vnd.android.cursor.dir/searches";
            case 10:
                return "vnd.android.cursor.item/bookmark";
            case 11:
                return "vnd.android.cursor.item/searches";
            case 20:
                return "vnd.android.cursor.dir/vnd.android.search.suggest";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        boolean z;
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        Uri uri2 = null;
        switch (match) {
            case 0:
                long insert = writableDatabase.insert(TABLE_NAMES[0], "url", contentValues);
                if (insert > 0) {
                    uri2 = ContentUris.withAppendedId(Browser.BOOKMARKS_URI, insert);
                }
                z = true;
                break;
            case 1:
                long insert2 = writableDatabase.insert(TABLE_NAMES[1], "url", contentValues);
                z = false;
                uri2 = null;
                if (insert2 > 0) {
                    uri2 = ContentUris.withAppendedId(Browser.SEARCHES_URI, insert2);
                    z = false;
                    break;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
        if (uri2 == null) {
            throw new IllegalArgumentException("Unknown URL");
        }
        getContext().getContentResolver().notifyChange(uri2, null);
        if (z && contentValues.containsKey("bookmark") && contentValues.getAsInteger("bookmark").intValue() != 0) {
            this.mBackupManager.dataChanged();
        }
        return uri2;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        Context context = getContext();
        boolean z = (context.getResources().getConfiguration().screenLayout & 15) == 4;
        boolean z2 = context.getResources().getConfiguration().orientation == 1;
        if (z && z2) {
            this.mMaxSuggestionLongSize = 9;
            this.mMaxSuggestionShortSize = 6;
        } else {
            this.mMaxSuggestionLongSize = 6;
            this.mMaxSuggestionShortSize = 3;
        }
        this.mOpenHelper = new DatabaseHelper(context);
        this.mBackupManager = new BackupManager(context);
        this.mSettings = BrowserSettings.getInstance();
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) throws IllegalStateException {
        int match = URI_MATCHER.match(uri);
        if (match == -1) {
            throw new IllegalArgumentException("Unknown URL");
        }
        if (match == 20 || match == 21) {
            return doSuggestQuery(str, strArr2, match == 21);
        }
        String[] strArr3 = null;
        if (strArr != null) {
            strArr3 = null;
            if (strArr.length > 0) {
                strArr3 = new String[strArr.length + 1];
                System.arraycopy(strArr, 0, strArr3, 0, strArr.length);
                strArr3[strArr.length] = "_id AS _id";
            }
        }
        String str3 = null;
        if (match == 10 || match == 11) {
            str3 = "_id = " + uri.getPathSegments().get(1);
        }
        Cursor query = this.mOpenHelper.getReadableDatabase().query(TABLE_NAMES[match % 10], strArr3, DatabaseUtils.concatenateWhere(str3, str), strArr2, null, null, str2, null);
        query.setNotificationUri(getContext().getContentResolver(), uri);
        return query;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0038, code lost:
        if (r0 == 11) goto L35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x00f0, code lost:
        if (r9.containsKey("url") != false) goto L26;
     */
    @Override // android.content.ContentProvider
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        String sb;
        boolean z;
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        if (match == -1 || match == 20) {
            throw new IllegalArgumentException("Unknown URL");
        }
        if (match != 10) {
            sb = str;
        }
        StringBuilder sb2 = new StringBuilder();
        if (str != null && str.length() > 0) {
            sb2.append("( ");
            sb2.append(str);
            sb2.append(" ) AND ");
        }
        sb2.append("_id = ");
        sb2.append(uri.getPathSegments().get(1));
        sb = sb2.toString();
        ContentResolver contentResolver = getContext().getContentResolver();
        if (match == 10 || match == 0) {
            if (contentValues.containsKey("bookmark")) {
                z = true;
            } else {
                if (!contentValues.containsKey("title")) {
                    z = false;
                }
                z = false;
                if (contentValues.containsKey("_id")) {
                    Cursor query = contentResolver.query(Browser.BOOKMARKS_URI, new String[]{"bookmark"}, "_id = " + contentValues.getAsString("_id"), null, null);
                    z = false;
                    if (query.moveToNext()) {
                        z = query.getInt(0) != 0;
                    }
                    query.close();
                }
            }
            if (z) {
                this.mBackupManager.dataChanged();
            }
        }
        int update = writableDatabase.update(TABLE_NAMES[match % 10], contentValues, sb, strArr);
        contentResolver.notifyChange(uri, null);
        return update;
    }
}
