package jp.co.benesse.touch.sbox;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import java.util.Map;
import jp.co.benesse.touch.util.Logger;
/* loaded from: classes.dex */
public class SboxProvider extends ContentProvider {
    private static final int SBOX_ID = 2;
    private static final int UNSPECIFIED_ID = 0;
    private static final int WIPE_ID = 1;
    protected SboxDbHelper mDbHelper;
    protected KeyStoreAdapter mKeyStoreAdapter;
    private static final String TAG = SboxProvider.class.getSimpleName();
    private static final String AUTHORITY = SboxProvider.class.getPackage().getName();
    private static UriMatcher sUriMatcher = new UriMatcher(-1);

    static {
        sUriMatcher.addURI(AUTHORITY, "/", UNSPECIFIED_ID);
        sUriMatcher.addURI(AUTHORITY, "cmd/wipe", 1);
        sUriMatcher.addURI(AUTHORITY, "*", 2);
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        String[] strArr2;
        try {
            int match = sUriMatcher.match(uri);
            String str2 = null;
            if (match == 2) {
                str2 = addAppidToSelection(str);
                strArr2 = addAppidToselectionArgs(uri, strArr);
            } else if (match != 1) {
                throw new IllegalArgumentException("Unknown URI " + uri);
            } else {
                strArr2 = null;
            }
            return this.mDbHelper.getWritableDatabase().delete(SboxDbHelper.TABLE_NAME, str2, strArr2);
        } catch (Exception e) {
            Logger.e(TAG, e);
            return UNSPECIFIED_ID;
        }
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        try {
            if (sUriMatcher.match(uri) == 2) {
                return "vnd.android.cursor.item/vnd." + AUTHORITY;
            }
            throw new IllegalArgumentException("Unknown URI " + uri);
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        try {
            if (sUriMatcher.match(uri) == 2) {
                contentValues.remove(SboxColumns.APP_ID);
                contentValues.put(SboxColumns.APP_ID, uri.getLastPathSegment());
                for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
                    if (TextUtils.equals(SboxColumns.VALUE, entry.getKey())) {
                        Object value = entry.getValue();
                        if (value instanceof String) {
                            entry.setValue(this.mKeyStoreAdapter.encryptString((String) value));
                        }
                    }
                }
                long replace = this.mDbHelper.getWritableDatabase().replace(SboxDbHelper.TABLE_NAME, null, contentValues);
                if (replace >= 0) {
                    return ContentUris.withAppendedId(uri, replace);
                }
                throw new IllegalArgumentException("Failed to insert row into " + uri);
            }
            throw new IllegalArgumentException("Unknown URI " + uri);
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.mDbHelper = new SboxDbHelper(getContext());
        this.mDbHelper.setWriteAheadLoggingEnabled(false);
        this.mKeyStoreAdapter = new KeyStoreAdapter(getContext());
        return true;
    }

    /* JADX WARN: Type inference failed for: r7v0 */
    /* JADX WARN: Type inference failed for: r7v1 */
    /* JADX WARN: Type inference failed for: r7v4, types: [int] */
    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        ?? r7;
        MatrixCursor matrixCursor;
        try {
            r7 = sUriMatcher.match(uri);
        } catch (Exception e) {
            e = e;
            r7 = 0;
        }
        if (r7 == 2) {
            try {
                String addAppidToSelection = addAppidToSelection(str);
                String[] addAppidToselectionArgs = addAppidToselectionArgs(uri, strArr2);
                SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
                sQLiteQueryBuilder.setTables(SboxDbHelper.TABLE_NAME);
                Cursor query = sQLiteQueryBuilder.query(this.mDbHelper.getReadableDatabase(), strArr, addAppidToSelection, addAppidToselectionArgs, null, null, str2);
                try {
                    MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{SboxColumns.KEY, SboxColumns.VALUE});
                    while (query.moveToNext()) {
                        try {
                            matrixCursor2.addRow(new String[]{query.getString(query.getColumnIndex(SboxColumns.KEY)), this.mKeyStoreAdapter.decryptString(query.getString(query.getColumnIndex(SboxColumns.VALUE)))});
                        } catch (Throwable th) {
                            th = th;
                            query.close();
                            throw th;
                        }
                    }
                    query.close();
                    matrixCursor = matrixCursor2;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e2) {
                e = e2;
                Logger.e(TAG, e);
                matrixCursor = r7;
                return matrixCursor;
            }
            return matrixCursor;
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        try {
            if (sUriMatcher.match(uri) == 2) {
                String addAppidToSelection = addAppidToSelection(str);
                String[] addAppidToselectionArgs = addAppidToselectionArgs(uri, strArr);
                for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
                    if (TextUtils.equals(SboxColumns.VALUE, entry.getKey())) {
                        Object value = entry.getValue();
                        if (value instanceof String) {
                            entry.setValue(this.mKeyStoreAdapter.encryptString((String) value));
                        }
                    }
                }
                return this.mDbHelper.getWritableDatabase().update(SboxDbHelper.TABLE_NAME, contentValues, addAppidToSelection, addAppidToselectionArgs);
            }
            throw new IllegalArgumentException("Unknown URI " + uri);
        } catch (Exception e) {
            Logger.e(TAG, e);
            return UNSPECIFIED_ID;
        }
    }

    private static String addAppidToSelection(String str) {
        if (TextUtils.isEmpty(str)) {
            return "appid = ? ";
        }
        return "appid = ? AND " + str;
    }

    private static String[] addAppidToselectionArgs(Uri uri, String[] strArr) {
        String[] strArr2;
        if (strArr == null || strArr.length <= 0) {
            strArr2 = new String[1];
        } else {
            strArr2 = new String[strArr.length + 1];
            System.arraycopy(strArr, UNSPECIFIED_ID, strArr2, 1, strArr.length);
        }
        strArr2[UNSPECIFIED_ID] = uri.getLastPathSegment();
        return strArr2;
    }
}
