package com.android.browser.sitenavigation;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import com.android.browser.Extensions;
import com.android.browser.provider.BrowserProvider2;
import com.mediatek.browser.ext.IBrowserSiteNavigationExt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
/* loaded from: b.zip:com/android/browser/sitenavigation/SiteNavigationProvider.class */
public class SiteNavigationProvider extends ContentProvider {
    private static final Uri NOTIFICATION_URI;
    private static final UriMatcher S_URI_MATCHER = new UriMatcher(-1);
    private SiteNavigationDatabaseHelper mOpenHelper;

    /* loaded from: b.zip:com/android/browser/sitenavigation/SiteNavigationProvider$SiteNavigationDatabaseHelper.class */
    private class SiteNavigationDatabaseHelper extends SQLiteOpenHelper {
        private IBrowserSiteNavigationExt mBrowserSiteNavigationExt;
        private Context mContext;
        final SiteNavigationProvider this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public SiteNavigationDatabaseHelper(SiteNavigationProvider siteNavigationProvider, Context context) {
            super(context, "websites.db", (SQLiteDatabase.CursorFactory) null, 1);
            this.this$0 = siteNavigationProvider;
            this.mBrowserSiteNavigationExt = null;
            this.mContext = context;
        }

        private void createTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE websites (_id INTEGER PRIMARY KEY AUTOINCREMENT,url TEXT,title TEXT,created LONG,website INTEGER,thumbnail BLOB DEFAULT NULL,favicon BLOB DEFAULT NULL,default_thumb TEXT);");
        }

        private void initTable(SQLiteDatabase sQLiteDatabase) {
            int i;
            ByteArrayOutputStream byteArrayOutputStream;
            ByteArrayOutputStream byteArrayOutputStream2;
            Bitmap decodeResource;
            this.mBrowserSiteNavigationExt = Extensions.getSiteNavigationPlugin(this.mContext);
            CharSequence[] predefinedWebsites = this.mBrowserSiteNavigationExt.getPredefinedWebsites();
            CharSequence[] charSequenceArr = predefinedWebsites;
            if (predefinedWebsites == null) {
                charSequenceArr = this.mContext.getResources().getTextArray(2131230815);
            }
            if (charSequenceArr == null) {
                return;
            }
            int length = charSequenceArr.length;
            if (this.mContext.getResources().getBoolean(2131296256)) {
                i = 8;
            } else {
                int siteNavigationCount = this.mBrowserSiteNavigationExt.getSiteNavigationCount();
                i = siteNavigationCount;
                if (siteNavigationCount == 0) {
                    i = 9;
                }
            }
            int i2 = length;
            if (length > i * 3) {
                i2 = i * 3;
            }
            int i3 = 0;
            ContentValues contentValues = null;
            ByteArrayOutputStream byteArrayOutputStream3 = null;
            while (true) {
                byteArrayOutputStream = byteArrayOutputStream3;
                if (i3 >= i2) {
                    break;
                }
                try {
                    byteArrayOutputStream2 = new ByteArrayOutputStream();
                    ContentValues contentValues2 = contentValues;
                } catch (ArrayIndexOutOfBoundsException e) {
                    e = e;
                    Log.e("@M_browser/SiteNavigationProvider", "initTable: ArrayIndexOutOfBoundsException: " + e);
                    return;
                }
                try {
                    String charSequence = charSequenceArr[i3 + 2].toString();
                    if (charSequence == null || charSequence.length() == 0) {
                        decodeResource = BitmapFactory.decodeResource(this.mContext.getResources(), 2131165217);
                    } else {
                        decodeResource = BitmapFactory.decodeResource(this.mContext.getResources(), this.mContext.getResources().getIdentifier(charSequence, "raw", this.mContext.getPackageName()));
                    }
                    Bitmap bitmap = decodeResource;
                    if (decodeResource == null) {
                        bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 2131165217);
                    }
                    ContentValues contentValues3 = contentValues;
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream2);
                    ContentValues contentValues4 = contentValues;
                    ContentValues contentValues5 = contentValues;
                    ContentValues contentValues6 = new ContentValues();
                    try {
                        contentValues6.put("url", BrowserProvider2.replaceSystemPropertyInString(this.mContext, charSequenceArr[i3 + 1]).toString());
                        contentValues6.put("title", charSequenceArr[i3].toString());
                        contentValues6.put("created", "0");
                        contentValues6.put("website", "1");
                        contentValues6.put("thumbnail", byteArrayOutputStream2.toByteArray());
                        sQLiteDatabase.insertOrThrow("websites", "url", contentValues6);
                        i3 += 3;
                        contentValues = contentValues6;
                        byteArrayOutputStream3 = byteArrayOutputStream2;
                    } catch (ArrayIndexOutOfBoundsException e2) {
                        e = e2;
                        Log.e("@M_browser/SiteNavigationProvider", "initTable: ArrayIndexOutOfBoundsException: " + e);
                        return;
                    }
                } catch (ArrayIndexOutOfBoundsException e3) {
                    e = e3;
                    Log.e("@M_browser/SiteNavigationProvider", "initTable: ArrayIndexOutOfBoundsException: " + e);
                    return;
                }
            }
            for (int i4 = i2 / 3; i4 < i; i4++) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                ContentValues contentValues7 = contentValues;
                BitmapFactory.decodeResource(this.mContext.getResources(), 2131165217).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                ContentValues contentValues8 = contentValues;
                contentValues = new ContentValues();
                contentValues.put("url", "about:blank" + (i4 + 1));
                contentValues.put("title", "about:blank");
                contentValues.put("created", "0");
                contentValues.put("website", "1");
                contentValues.put("thumbnail", byteArrayOutputStream.toByteArray());
                sQLiteDatabase.insertOrThrow("websites", "url", contentValues);
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            createTable(sQLiteDatabase);
            initTable(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        }
    }

    static {
        S_URI_MATCHER.addURI("com.android.browser.site_navigation", "websites", 0);
        S_URI_MATCHER.addURI("com.android.browser.site_navigation", "websites/#", 1);
        NOTIFICATION_URI = SiteNavigation.SITE_NAVIGATION_URI;
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
        this.mOpenHelper = new SiteNavigationDatabaseHelper(this, getContext());
        return true;
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String str) {
        try {
            ParcelFileDescriptor[] createPipe = ParcelFileDescriptor.createPipe();
            new RequestHandlerSiteNavigation(getContext(), uri, new AssetFileDescriptor(createPipe[1], 0L, -1L).createOutputStream()).start();
            return createPipe[0];
        } catch (IOException e) {
            Log.e("browser/SiteNavigationProvider", "Failed to handle request: " + uri, e);
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables("websites");
        switch (S_URI_MATCHER.match(uri)) {
            case 0:
                break;
            default:
                Log.e("@M_browser/SiteNavigationProvider", "SiteNavigationProvider query Unknown URI: " + uri);
                return null;
            case 1:
                sQLiteQueryBuilder.appendWhere("_id=" + uri.getPathSegments().get(0));
                break;
        }
        if (TextUtils.isEmpty(str2)) {
            str2 = null;
        }
        Cursor query = sQLiteQueryBuilder.query(this.mOpenHelper.getReadableDatabase(), strArr, str, strArr2, null, null, str2);
        if (query != null) {
            query.setNotificationUri(getContext().getContentResolver(), NOTIFICATION_URI);
        }
        return query;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        int i = 0;
        switch (S_URI_MATCHER.match(uri)) {
            case 0:
                i = writableDatabase.update("websites", contentValues, str, strArr);
                break;
            case 1:
                try {
                    i = writableDatabase.update("websites", contentValues, "_id=" + uri.getLastPathSegment() + (!TextUtils.isEmpty(str) ? " AND (" + str + ')' : ""), strArr);
                    break;
                } catch (SQLiteDiskIOException e) {
                    Log.e("browser/SiteNavigationProvider", "Here happened SQLiteDiskIOException");
                    break;
                } catch (SQLiteFullException e2) {
                    Log.e("browser/SiteNavigationProvider", "Here happened SQLiteFullException");
                    break;
                }
            default:
                Log.e("@M_browser/SiteNavigationProvider", "SiteNavigationProvider update Unknown URI: " + uri);
                return 0;
        }
        if (i > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return i;
    }
}
