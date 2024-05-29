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
import com.android.browser.R;
import com.android.browser.provider.BrowserProvider2;
import com.mediatek.browser.ext.IBrowserSiteNavigationExt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
/* loaded from: classes.dex */
public class SiteNavigationProvider extends ContentProvider {
    private static final Uri NOTIFICATION_URI;
    private static final UriMatcher S_URI_MATCHER = new UriMatcher(-1);
    private SiteNavigationDatabaseHelper mOpenHelper;

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
        this.mOpenHelper = new SiteNavigationDatabaseHelper(getContext());
        return true;
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
        Cursor query = sQLiteQueryBuilder.query(this.mOpenHelper.getReadableDatabase(), strArr, str, strArr2, null, null, TextUtils.isEmpty(str2) ? null : str2);
        if (query != null) {
            query.setNotificationUri(getContext().getContentResolver(), NOTIFICATION_URI);
        }
        return query;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        String str2;
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        int i = 0;
        switch (S_URI_MATCHER.match(uri)) {
            case 0:
                i = writableDatabase.update("websites", contentValues, str, strArr);
                break;
            case 1:
                StringBuilder sb = new StringBuilder();
                sb.append("_id=");
                sb.append(uri.getLastPathSegment());
                if (TextUtils.isEmpty(str)) {
                    str2 = "";
                } else {
                    str2 = " AND (" + str + ')';
                }
                sb.append(str2);
                try {
                    i = writableDatabase.update("websites", contentValues, sb.toString(), strArr);
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

    /* loaded from: classes.dex */
    private class SiteNavigationDatabaseHelper extends SQLiteOpenHelper {
        private IBrowserSiteNavigationExt mBrowserSiteNavigationExt;
        private Context mContext;

        public SiteNavigationDatabaseHelper(Context context) {
            super(context, "websites.db", (SQLiteDatabase.CursorFactory) null, 1);
            this.mBrowserSiteNavigationExt = null;
            this.mContext = context;
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            createTable(sQLiteDatabase);
            initTable(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        }

        private void createTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE websites (_id INTEGER PRIMARY KEY AUTOINCREMENT,url TEXT,title TEXT,created LONG,website INTEGER,thumbnail BLOB DEFAULT NULL,favicon BLOB DEFAULT NULL,default_thumb TEXT);");
        }

        private void initTable(SQLiteDatabase sQLiteDatabase) {
            int i;
            Bitmap decodeResource;
            Bitmap bitmap;
            this.mBrowserSiteNavigationExt = Extensions.getSiteNavigationPlugin(this.mContext);
            CharSequence[] predefinedWebsites = this.mBrowserSiteNavigationExt.getPredefinedWebsites();
            if (predefinedWebsites == null) {
                predefinedWebsites = this.mContext.getResources().getTextArray(R.array.predefined_websites_default_optr);
            }
            if (predefinedWebsites == null) {
                return;
            }
            int length = predefinedWebsites.length;
            if (!this.mContext.getResources().getBoolean(R.bool.isTablet)) {
                i = this.mBrowserSiteNavigationExt.getSiteNavigationCount();
                if (i == 0) {
                    i = 9;
                }
            } else {
                i = 8;
            }
            int i2 = i * 3;
            if (length > i2) {
                length = i2;
            }
            for (int i3 = 0; i3 < length; i3 += 3) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    String charSequence = predefinedWebsites[i3 + 2].toString();
                    if (charSequence != null && charSequence.length() != 0) {
                        decodeResource = BitmapFactory.decodeResource(this.mContext.getResources(), this.mContext.getResources().getIdentifier(charSequence, "raw", this.mContext.getPackageName()));
                    } else {
                        decodeResource = BitmapFactory.decodeResource(this.mContext.getResources(), R.raw.sitenavigation_thumbnail_default);
                    }
                    if (decodeResource == null) {
                        bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), R.raw.sitenavigation_thumbnail_default);
                    } else {
                        bitmap = decodeResource;
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("url", BrowserProvider2.replaceSystemPropertyInString(this.mContext, predefinedWebsites[i3 + 1]).toString());
                    contentValues.put("title", predefinedWebsites[i3].toString());
                    contentValues.put("created", "0");
                    contentValues.put("website", "1");
                    contentValues.put("thumbnail", byteArrayOutputStream.toByteArray());
                    sQLiteDatabase.insertOrThrow("websites", "url", contentValues);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e("@M_browser/SiteNavigationProvider", "initTable: ArrayIndexOutOfBoundsException: " + e);
                    return;
                }
            }
            int i4 = length / 3;
            while (i4 < i) {
                ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                BitmapFactory.decodeResource(this.mContext.getResources(), R.raw.sitenavigation_thumbnail_default).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream2);
                ContentValues contentValues2 = new ContentValues();
                StringBuilder sb = new StringBuilder();
                sb.append("about:blank");
                i4++;
                sb.append(i4);
                contentValues2.put("url", sb.toString());
                contentValues2.put("title", "about:blank");
                contentValues2.put("created", "0");
                contentValues2.put("website", "1");
                contentValues2.put("thumbnail", byteArrayOutputStream2.toByteArray());
                sQLiteDatabase.insertOrThrow("websites", "url", contentValues2);
            }
        }
    }
}
