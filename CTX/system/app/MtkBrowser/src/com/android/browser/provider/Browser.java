package com.android.browser.provider;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.android.browser.provider.BrowserContract;
/* loaded from: classes.dex */
public class Browser {
    public static final Uri BOOKMARKS_URI = Uri.parse("content://MtkBrowserProvider/bookmarks");
    public static final String[] HISTORY_PROJECTION = {"_id", "url", "visits", "date", "bookmark", "title", "favicon", "thumbnail", "touch_icon", "user_entered"};
    public static final String[] TRUNCATE_HISTORY_PROJECTION = {"_id", "date"};
    public static final Uri SEARCHES_URI = Uri.parse("content://MtkBrowserProvider/searches");
    public static final String[] SEARCHES_PROJECTION = {"_id", "search", "date"};

    public static final void saveBookmark(Context context, String str, String str2) {
        Intent intent = new Intent("android.intent.action.INSERT", BOOKMARKS_URI);
        intent.putExtra("title", str);
        intent.putExtra("url", str2);
        context.startActivity(intent);
    }

    public static final void sendString(Context context, String str, String str2) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.TEXT", str);
        try {
            Intent createChooser = Intent.createChooser(intent, str2);
            createChooser.setFlags(268435456);
            context.startActivity(createChooser);
        } catch (ActivityNotFoundException e) {
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:16:0x0038, code lost:
        if (r10 != null) goto L18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x003a, code lost:
        r10.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x004e, code lost:
        if (r10 == null) goto L19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x0051, code lost:
        return r1;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0055  */
    /* JADX WARN: Type inference failed for: r10v0, types: [android.content.ContentResolver] */
    /* JADX WARN: Type inference failed for: r10v1 */
    /* JADX WARN: Type inference failed for: r10v4, types: [android.database.Cursor] */
    @Deprecated
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final String[] getVisitedHistory(ContentResolver contentResolver) {
        IllegalStateException e;
        Cursor cursor;
        String[] strArr;
        try {
            try {
                cursor = contentResolver.query(BrowserContract.History.CONTENT_URI, new String[]{"url"}, "visits > 0", null, null);
                try {
                    if (cursor == null) {
                        String[] strArr2 = new String[0];
                        if (cursor != null) {
                            cursor.close();
                        }
                        return strArr2;
                    }
                    strArr = new String[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext()) {
                        strArr[i] = cursor.getString(0);
                        i++;
                    }
                } catch (IllegalStateException e2) {
                    e = e2;
                    Log.e("browser", "getVisitedHistory", e);
                    strArr = new String[0];
                }
            } catch (Throwable th) {
                th = th;
                if (contentResolver != 0) {
                    contentResolver.close();
                }
                throw th;
            }
        } catch (IllegalStateException e3) {
            e = e3;
            cursor = null;
        } catch (Throwable th2) {
            th = th2;
            contentResolver = 0;
            if (contentResolver != 0) {
            }
            throw th;
        }
    }

    public static final void truncateHistory(ContentResolver contentResolver) {
        Cursor query;
        Cursor cursor = null;
        try {
            try {
                query = contentResolver.query(BrowserContract.History.CONTENT_URI, new String[]{"_id", "url", "date"}, null, null, "date ASC");
            } catch (IllegalStateException e) {
                e = e;
            }
        } catch (Throwable th) {
            th = th;
        }
        try {
            if (query.moveToFirst() && query.getCount() >= 250) {
                for (int i = 0; i < 5; i++) {
                    contentResolver.delete(ContentUris.withAppendedId(BrowserContract.History.CONTENT_URI, query.getLong(0)), null, null);
                    if (!query.moveToNext()) {
                        break;
                    }
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (IllegalStateException e2) {
            e = e2;
            cursor = query;
            Log.e("browser", "truncateHistory", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            th = th2;
            cursor = query;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public static final void clearHistory(ContentResolver contentResolver) {
        deleteHistoryWhere(contentResolver, null);
    }

    private static final void deleteHistoryWhere(ContentResolver contentResolver, String str) {
        Cursor query;
        Cursor cursor = null;
        try {
            try {
                query = contentResolver.query(BrowserContract.History.CONTENT_URI, new String[]{"url"}, str, null, null);
            } catch (Throwable th) {
                th = th;
            }
        } catch (IllegalStateException e) {
            e = e;
        }
        try {
            if (query.moveToFirst()) {
                contentResolver.delete(BrowserContract.History.CONTENT_URI, str, null);
            }
            if (query != null) {
                query.close();
            }
        } catch (IllegalStateException e2) {
            e = e2;
            cursor = query;
            Log.e("browser", "deleteHistoryWhere", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            th = th2;
            cursor = query;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public static final void deleteFromHistory(ContentResolver contentResolver, String str) {
        contentResolver.delete(BrowserContract.History.CONTENT_URI, "url=?", new String[]{str});
    }

    public static final void addSearchUrl(ContentResolver contentResolver, String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("search", str);
        contentValues.put("date", Long.valueOf(System.currentTimeMillis()));
        contentResolver.insert(BrowserContract.Searches.CONTENT_URI, contentValues);
    }

    public static final void clearSearches(ContentResolver contentResolver) {
        try {
            contentResolver.delete(BrowserContract.Searches.CONTENT_URI, null, null);
        } catch (IllegalStateException e) {
            Log.e("browser", "clearSearches", e);
        }
    }
}
