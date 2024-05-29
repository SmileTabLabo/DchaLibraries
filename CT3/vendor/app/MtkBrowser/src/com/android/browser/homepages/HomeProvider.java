package com.android.browser.homepages;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.webkit.WebResourceResponse;
import com.android.browser.BrowserSettings;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
/* loaded from: b.zip:com/android/browser/homepages/HomeProvider.class */
public class HomeProvider extends ContentProvider {
    private static boolean interceptFile(String str) {
        return str.startsWith("file:///") && new File(str.substring(7)).isDirectory();
    }

    public static WebResourceResponse shouldInterceptRequest(Context context, String str) {
        try {
            if (str.equals("content://com.android.browser.site_navigation/websites") || str.equals("content://com.android.browser.home/")) {
                return new WebResourceResponse("text/html", "utf-8", context.getContentResolver().openInputStream(Uri.parse(str)));
            } else if (BrowserSettings.getInstance().isDebugEnabled() && interceptFile(str)) {
                PipedInputStream pipedInputStream = new PipedInputStream();
                new RequestHandler(context, Uri.parse(str), new PipedOutputStream(pipedInputStream)).start();
                return new WebResourceResponse("text/html", "utf-8", pipedInputStream);
            } else {
                return null;
            }
        } catch (IOException e) {
            Log.e("HomeProvider", "Failed to create WebResourceResponse: " + e.getMessage());
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return null;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return false;
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String str) {
        try {
            ParcelFileDescriptor[] createPipe = ParcelFileDescriptor.createPipe();
            new RequestHandler(getContext(), uri, new AssetFileDescriptor(createPipe[1], 0L, -1L).createOutputStream()).start();
            return createPipe[0];
        } catch (IOException e) {
            Log.e("HomeProvider", "Failed to handle request: " + uri, e);
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        return null;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }
}
