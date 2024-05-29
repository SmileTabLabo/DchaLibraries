package com.android.browser;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebIconDatabase;
import android.widget.Toast;
import com.android.browser.provider.BrowserContract;
import java.io.ByteArrayOutputStream;
/* loaded from: b.zip:com/android/browser/Bookmarks.class */
public class Bookmarks {
    private static final String[] acceptableBookmarkSchemes = {"http:", "https:", "about:", "data:", "javascript:", "file:", "content:", "rtsp:"};

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void addBookmark(Context context, boolean z, String str, String str2, Bitmap bitmap, long j) {
        ContentValues contentValues = new ContentValues();
        deleteSameTitle(context, str2, j);
        deleteSameUrl(context, str, j);
        try {
            contentValues.put("title", str2);
            contentValues.put("url", str);
            contentValues.put("folder", (Integer) 0);
            contentValues.put("thumbnail", bitmapToBytes(bitmap));
            contentValues.put("parent", Long.valueOf(j));
            context.getContentResolver().insert(BrowserContract.Bookmarks.CONTENT_URI, contentValues);
        } catch (IllegalStateException e) {
            Log.e("Bookmarks", "addBookmark", e);
        }
        if (z) {
            Toast.makeText(context, 2131492954, 1).show();
        }
    }

    private static byte[] bitmapToBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static void deleteSameTitle(Context context, String str, long j) {
        Log.d("browser/Bookmarks", "deleteSameTitle title:" + str);
        if (str == null || str.length() == 0) {
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("deleted", (Integer) 1);
        Log.d("browser/Bookmarks", "same title delete :" + context.getContentResolver().update(BrowserContract.Bookmarks.CONTENT_URI, contentValues, "title =? AND parent =? AND deleted =? AND folder =0 ", new String[]{str, String.valueOf(j), String.valueOf(0)}));
    }

    private static void deleteSameUrl(Context context, String str, long j) {
        Log.d("browser/Bookmarks", "deleteSameUrl url:" + str);
        if (str == null || str.length() == 0) {
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("deleted", (Integer) 1);
        Log.d("browser/Bookmarks", "same url delete :" + context.getContentResolver().update(BrowserContract.Bookmarks.CONTENT_URI, contentValues, "url =? AND parent =? AND deleted =?", new String[]{str, String.valueOf(j), String.valueOf(0)}));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getIdByNameOrUrl(ContentResolver contentResolver, String str, String str2, long j, long j2) {
        String str3 = j2 > 0 ? "parent = ? AND (title = ? OR url = ? OR url = ?) AND folder= 0 AND _id <> " + j2 : "parent = ? AND (title = ? OR url = ? OR url = ?) AND folder= 0";
        Log.v("browser/Bookmarks", "getIdByNameOrUrl() sql:" + str3);
        Cursor query = contentResolver.query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"_id"}, str3, new String[]{String.valueOf(j), str, str2, str2.endsWith("/") ? str2.substring(0, str2.lastIndexOf("/")) : str2 + "/"}, "_id DESC");
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    return query.getInt(0);
                }
                return -1;
            } finally {
                query.close();
            }
        }
        return -1;
    }

    private static String modifyUrl(String str) {
        return str.endsWith("/") ? str.substring(0, str.length() - 1) : str;
    }

    public static Cursor queryCombinedForUrl(ContentResolver contentResolver, String str, String str2) {
        if (contentResolver == null || str2 == null) {
            return null;
        }
        String str3 = str;
        if (str == null) {
            str3 = str2;
        }
        return contentResolver.query(BrowserContract.Combined.CONTENT_URI, new String[]{"url"}, "url == ? OR url == ? OR url == ? OR url == ?", new String[]{str3, str2, modifyUrl(str3), modifyUrl(str2)}, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void removeFromBookmarks(Context context, ContentResolver contentResolver, String str, String str2) {
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = contentResolver.query(BookmarkUtils.getBookmarksUri(context), new String[]{"_id"}, "url = ? AND title = ?", new String[]{str, str2}, null);
                if (!query.moveToFirst()) {
                    if (query != null) {
                        query.close();
                        return;
                    }
                    return;
                }
                WebIconDatabase.getInstance().releaseIconForPageUrl(str);
                contentResolver.delete(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, query.getLong(0)), null, null);
                if (context != null) {
                    cursor2 = query;
                    cursor = query;
                    Toast.makeText(context, 2131492955, 1).show();
                }
                if (query != null) {
                    query.close();
                }
            } catch (IllegalStateException e) {
                Log.e("Bookmarks", "removeFromBookmarks", e);
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

    static String removeQuery(String str) {
        if (str == null) {
            return null;
        }
        int indexOf = str.indexOf(63);
        String str2 = str;
        if (indexOf != -1) {
            str2 = str.substring(0, indexOf);
        }
        return str2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.browser.Bookmarks$1] */
    public static void updateFavicon(ContentResolver contentResolver, String str, String str2, Bitmap bitmap) {
        new AsyncTask<Void, Void, Void>(bitmap, contentResolver, str, str2) { // from class: com.android.browser.Bookmarks.1
            final ContentResolver val$cr;
            final Bitmap val$favicon;
            final String val$originalUrl;
            final String val$url;

            {
                this.val$favicon = bitmap;
                this.val$cr = contentResolver;
                this.val$originalUrl = str;
                this.val$url = str2;
            }

            private void updateImages(ContentResolver contentResolver2, String str3, ContentValues contentValues) {
                String removeQuery = Bookmarks.removeQuery(str3);
                if (TextUtils.isEmpty(removeQuery)) {
                    return;
                }
                contentValues.put("url_key", removeQuery);
                contentResolver2.update(BrowserContract.Images.CONTENT_URI, contentValues, null, null);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                this.val$favicon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                ContentValues contentValues = new ContentValues();
                contentValues.put("favicon", byteArrayOutputStream.toByteArray());
                updateImages(this.val$cr, this.val$originalUrl, contentValues);
                updateImages(this.val$cr, this.val$url, contentValues);
                return null;
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean urlHasAcceptableScheme(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < acceptableBookmarkSchemes.length; i++) {
            if (str.startsWith(acceptableBookmarkSchemes[i])) {
                return true;
            }
        }
        return false;
    }
}
