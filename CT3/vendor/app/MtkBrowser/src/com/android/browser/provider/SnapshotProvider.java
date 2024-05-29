package com.android.browser.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.FileUtils;
import android.text.TextUtils;
import java.io.File;
/* loaded from: b.zip:com/android/browser/provider/SnapshotProvider.class */
public class SnapshotProvider extends ContentProvider {
    static final String[] DELETE_PROJECTION;
    SnapshotDatabaseHelper mOpenHelper;
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.browser.snapshots");
    static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    static final byte[] NULL_BLOB_HACK = new byte[0];

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/provider/SnapshotProvider$SnapshotDatabaseHelper.class */
    public static final class SnapshotDatabaseHelper extends SQLiteOpenHelper {
        public SnapshotDatabaseHelper(Context context) {
            super(context, "snapshots.db", (SQLiteDatabase.CursorFactory) null, 4);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE snapshots(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT NOT NULL,date_created INTEGER,favicon BLOB,thumbnail BLOB,background INTEGER,view_state BLOB NOT NULL,viewstate_path TEXT,viewstate_size INTEGER,job_id INTEGER,progress INTEGER,is_done INTEGER);");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i < 2) {
                sQLiteDatabase.execSQL("DROP TABLE snapshots");
                onCreate(sQLiteDatabase);
            }
            if (i < 3) {
                sQLiteDatabase.execSQL("ALTER TABLE snapshots ADD COLUMN viewstate_path TEXT");
                sQLiteDatabase.execSQL("ALTER TABLE snapshots ADD COLUMN viewstate_size INTEGER");
                sQLiteDatabase.execSQL("UPDATE snapshots SET viewstate_size = length(view_state)");
            }
            if (i < 4) {
                sQLiteDatabase.execSQL("ALTER TABLE snapshots ADD COLUMN job_id INTEGER");
                sQLiteDatabase.execSQL("ALTER TABLE snapshots ADD COLUMN progress INTEGER");
                sQLiteDatabase.execSQL("ALTER TABLE snapshots ADD COLUMN is_done INTEGER");
                sQLiteDatabase.execSQL("UPDATE snapshots SET job_id = -1 ");
                sQLiteDatabase.execSQL("UPDATE snapshots SET progress = 100 ");
                sQLiteDatabase.execSQL("UPDATE snapshots SET is_done = 1 ");
            }
        }
    }

    /* loaded from: b.zip:com/android/browser/provider/SnapshotProvider$Snapshots.class */
    public interface Snapshots {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(SnapshotProvider.AUTHORITY_URI, "snapshots");
    }

    static {
        URI_MATCHER.addURI("com.android.browser.snapshots", "snapshots", 10);
        URI_MATCHER.addURI("com.android.browser.snapshots", "snapshots/#", 11);
        DELETE_PROJECTION = new String[]{"viewstate_path", "job_id"};
    }

    private void deleteDataFiles(SQLiteDatabase sQLiteDatabase, String str, String[] strArr) {
        Cursor query = sQLiteDatabase.query("snapshots", DELETE_PROJECTION, str, strArr, null, null, null);
        Context context = getContext();
        while (query.moveToNext()) {
            String string = query.getString(0);
            if (!TextUtils.isEmpty(string)) {
                if (query.getInt(1) == -1) {
                    File fileStreamPath = context.getFileStreamPath(string);
                    if (fileStreamPath.exists() && !fileStreamPath.delete()) {
                        fileStreamPath.deleteOnExit();
                    }
                } else {
                    int lastIndexOf = string.lastIndexOf(File.separator);
                    if (lastIndexOf != -1) {
                        deleteSavePageDir(new File(string.substring(0, lastIndexOf)));
                    }
                }
            }
        }
        query.close();
    }

    private void deleteSavePageDir(File file) {
        if (file.isFile()) {
            if (file.delete()) {
                return;
            }
            file.deleteOnExit();
        } else if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                if (file.delete()) {
                    return;
                }
                file.deleteOnExit();
                return;
            }
            for (File file2 : listFiles) {
                deleteSavePageDir(file2);
            }
            if (file.delete()) {
                return;
            }
            file.deleteOnExit();
        }
    }

    static File getOldDatabasePath(Context context) {
        return new File(context.getExternalFilesDir(null), "snapshots.db");
    }

    private void migrateToDataFolder() {
        File databasePath = getContext().getDatabasePath("snapshots.db");
        if (databasePath.exists()) {
            return;
        }
        File oldDatabasePath = getOldDatabasePath(getContext());
        if (oldDatabasePath.exists()) {
            if (!oldDatabasePath.renameTo(databasePath)) {
                FileUtils.copyFile(oldDatabasePath, databasePath);
            }
            oldDatabasePath.delete();
        }
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        if (writableDatabase == null) {
            return 0;
        }
        writableDatabase.beginTransaction();
        String str2 = str;
        String[] strArr2 = strArr;
        try {
            switch (URI_MATCHER.match(uri)) {
                case 10:
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown delete URI " + uri);
                case 11:
                    str2 = DatabaseUtils.concatenateWhere(str, "snapshots._id=?");
                    strArr2 = DatabaseUtils.appendSelectionArgs(strArr, new String[]{Long.toString(ContentUris.parseId(uri))});
                    break;
            }
            deleteDataFiles(writableDatabase, str2, strArr2);
            int delete = writableDatabase.delete("snapshots", str2, strArr2);
            if (delete > 0) {
                getContext().getContentResolver().notifyChange(uri, (ContentObserver) null, false);
            }
            writableDatabase.setTransactionSuccessful();
            return delete;
        } finally {
            writableDatabase.endTransaction();
        }
    }

    SQLiteDatabase getReadableDatabase() {
        return this.mOpenHelper.getReadableDatabase();
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return null;
    }

    SQLiteDatabase getWritableDatabase() {
        return this.mOpenHelper.getWritableDatabase();
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        if (writableDatabase == null) {
            return null;
        }
        writableDatabase.beginTransaction();
        try {
            switch (URI_MATCHER.match(uri)) {
                case 10:
                    if (!contentValues.containsKey("view_state")) {
                        contentValues.put("view_state", NULL_BLOB_HACK);
                    }
                    long insert = writableDatabase.insert("snapshots", "title", contentValues);
                    if (insert < 0) {
                        writableDatabase.endTransaction();
                        return null;
                    }
                    Uri withAppendedId = ContentUris.withAppendedId(uri, insert);
                    getContext().getContentResolver().notifyChange(withAppendedId, (ContentObserver) null, false);
                    writableDatabase.setTransactionSuccessful();
                    return withAppendedId;
                default:
                    throw new UnsupportedOperationException("Unknown insert URI " + uri);
            }
        } finally {
            writableDatabase.endTransaction();
        }
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        migrateToDataFolder();
        this.mOpenHelper = new SnapshotDatabaseHelper(getContext());
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        if (readableDatabase == null) {
            return null;
        }
        readableDatabase.beginTransaction();
        try {
            int match = URI_MATCHER.match(uri);
            SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
            String queryParameter = uri.getQueryParameter("limit");
            String str3 = str;
            String[] strArr3 = strArr2;
            switch (match) {
                case 10:
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown URL " + uri.toString());
                case 11:
                    str3 = DatabaseUtils.concatenateWhere(str, "_id=?");
                    strArr3 = DatabaseUtils.appendSelectionArgs(strArr2, new String[]{Long.toString(ContentUris.parseId(uri))});
                    break;
            }
            sQLiteQueryBuilder.setTables("snapshots");
            Cursor query = sQLiteQueryBuilder.query(readableDatabase, strArr, str3, strArr3, null, null, str2, queryParameter);
            query.setNotificationUri(getContext().getContentResolver(), AUTHORITY_URI);
            readableDatabase.setTransactionSuccessful();
            return query;
        } finally {
            readableDatabase.endTransaction();
        }
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        if (writableDatabase == null) {
            return 0;
        }
        writableDatabase.beginTransaction();
        try {
            int update = writableDatabase.update("snapshots", contentValues, str, strArr);
            getContext().getContentResolver().notifyChange(uri, (ContentObserver) null, false);
            writableDatabase.setTransactionSuccessful();
            return update;
        } finally {
            writableDatabase.endTransaction();
        }
    }
}
