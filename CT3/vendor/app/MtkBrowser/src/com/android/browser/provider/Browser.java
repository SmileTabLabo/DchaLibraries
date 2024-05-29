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
/* loaded from: b.zip:com/android/browser/provider/Browser.class */
public class Browser {
    public static final Uri BOOKMARKS_URI = Uri.parse("content://MtkBrowserProvider/bookmarks");
    public static final String[] HISTORY_PROJECTION = {"_id", "url", "visits", "date", "bookmark", "title", "favicon", "thumbnail", "touch_icon", "user_entered"};
    public static final String[] TRUNCATE_HISTORY_PROJECTION = {"_id", "date"};
    public static final Uri SEARCHES_URI = Uri.parse("content://MtkBrowserProvider/searches");
    public static final String[] SEARCHES_PROJECTION = {"_id", "search", "date"};

    public static final void addSearchUrl(ContentResolver contentResolver, String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("search", str);
        contentValues.put("date", Long.valueOf(System.currentTimeMillis()));
        contentResolver.insert(BrowserContract.Searches.CONTENT_URI, contentValues);
    }

    public static final void clearHistory(ContentResolver contentResolver) {
        deleteHistoryWhere(contentResolver, null);
    }

    public static final void clearSearches(ContentResolver contentResolver) {
        try {
            contentResolver.delete(BrowserContract.Searches.CONTENT_URI, null, null);
        } catch (IllegalStateException e) {
            Log.e("browser", "clearSearches", e);
        }
    }

    public static final void deleteFromHistory(ContentResolver contentResolver, String str) {
        contentResolver.delete(BrowserContract.History.CONTENT_URI, "url=?", new String[]{str});
    }

    private static final void deleteHistoryWhere(ContentResolver contentResolver, String str) {
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = contentResolver.query(BrowserContract.History.CONTENT_URI, new String[]{"url"}, str, null, null);
                if (query.moveToFirst()) {
                    cursor2 = query;
                    cursor = query;
                    contentResolver.delete(BrowserContract.History.CONTENT_URI, str, null);
                }
                if (query != null) {
                    query.close();
                }
            } catch (IllegalStateException e) {
                cursor = cursor2;
                Log.e("browser", "deleteHistoryWhere", e);
                if (cursor2 != null) {
                    cursor2.close();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    @Deprecated
    public static final String[] getVisitedHistory(ContentResolver contentResolver) {
        String[] strArr;
        Cursor query;
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                query = contentResolver.query(BrowserContract.History.CONTENT_URI, new String[]{"url"}, "visits > 0", null, null);
            } catch (IllegalStateException e) {
                Log.e("browser", "getVisitedHistory", e);
                cursor = cursor2;
                String[] strArr2 = new String[0];
                strArr = strArr2;
                if (cursor2 != null) {
                    cursor2.close();
                    strArr = strArr2;
                }
            }
            if (query == null) {
                if (query != null) {
                    query.close();
                }
                return new String[0];
            }
            String[] strArr3 = new String[query.getCount()];
            int i = 0;
            while (true) {
                cursor2 = query;
                cursor = query;
                if (!query.moveToNext()) {
                    break;
                }
                strArr3[i] = query.getString(0);
                i++;
            }
            strArr = strArr3;
            if (query != null) {
                query.close();
                strArr = strArr3;
            }
            return strArr;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

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

    public static final void truncateHistory(ContentResolver contentResolver) {
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = contentResolver.query(BrowserContract.History.CONTENT_URI, new String[]{"_id", "url", "date"}, null, null, "date ASC");
                if (query.moveToFirst() && query.getCount() >= 250) {
                    for (int i = 0; i < 5; i++) {
                        cursor2 = query;
                        cursor = query;
                        contentResolver.delete(ContentUris.withAppendedId(BrowserContract.History.CONTENT_URI, query.getLong(0)), null, null);
                        if (!query.moveToNext()) {
                            break;
                        }
                    }
                }
                if (query != null) {
                    query.close();
                }
            } catch (IllegalStateException e) {
                cursor = cursor2;
                Log.e("browser", "truncateHistory", e);
                if (cursor2 != null) {
                    cursor2.close();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }
}
