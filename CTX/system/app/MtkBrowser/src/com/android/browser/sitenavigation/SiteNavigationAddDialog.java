package com.android.browser.sitenavigation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.browser.R;
import com.android.browser.UrlUtils;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
/* loaded from: classes.dex */
public class SiteNavigationAddDialog extends Activity {
    private static final String[] ACCEPTABLE_WEBSITE_SCHEMES = {"http:", "https:", "about:", "data:", "javascript:", "file:", "content:", "rtsp:"};
    private EditText mAddress;
    private Button mButtonCancel;
    private Button mButtonOK;
    private TextView mDialogText;
    private Handler mHandler;
    private boolean mIsAdding;
    private String mItemName;
    private String mItemUrl;
    private Bundle mMap;
    private EditText mName;
    private View.OnClickListener mOKListener = new View.OnClickListener() { // from class: com.android.browser.sitenavigation.SiteNavigationAddDialog.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (SiteNavigationAddDialog.this.save()) {
                SiteNavigationAddDialog.this.setResult(-1, new Intent().putExtra("need_refresh", true));
                SiteNavigationAddDialog.this.finish();
            }
        }
    };
    private View.OnClickListener mCancelListener = new View.OnClickListener() { // from class: com.android.browser.sitenavigation.SiteNavigationAddDialog.2
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            SiteNavigationAddDialog.this.finish();
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        String str;
        super.onCreate(bundle);
        requestWindowFeature(1);
        setContentView(R.layout.site_navigation_add);
        this.mMap = getIntent().getExtras();
        Log.d("@M_browser/AddSiteNavigationPage", "onCreate mMap is : " + this.mMap);
        String str2 = null;
        if (this.mMap != null) {
            Bundle bundle2 = this.mMap.getBundle("websites");
            if (bundle2 != null) {
                this.mMap = bundle2;
            }
            str2 = this.mMap.getString("name");
            str = this.mMap.getString("url");
            this.mIsAdding = this.mMap.getBoolean("isAdding");
        } else {
            str = null;
        }
        this.mItemUrl = str;
        this.mItemName = str2;
        this.mName = (EditText) findViewById(R.id.title);
        this.mName.setText(str2);
        this.mAddress = (EditText) findViewById(R.id.address);
        if (str.startsWith("about:blank")) {
            this.mAddress.setText("about:blank");
        } else {
            this.mAddress.setText(str);
        }
        this.mDialogText = (TextView) findViewById(R.id.dialog_title);
        if (this.mIsAdding) {
            this.mDialogText.setText(R.string.add);
        }
        this.mButtonOK = (Button) findViewById(R.id.OK);
        this.mButtonOK.setOnClickListener(this.mOKListener);
        this.mButtonCancel = (Button) findViewById(R.id.cancel);
        this.mButtonCancel.setOnClickListener(this.mCancelListener);
        if (!getWindow().getDecorView().isInTouchMode()) {
            this.mButtonOK.requestFocus();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SaveSiteNavigationRunnable implements Runnable {
        private Message mMessage;

        public SaveSiteNavigationRunnable(Message message) {
            this.mMessage = message;
        }

        /* JADX WARN: Removed duplicated region for block: B:17:0x00b8  */
        /* JADX WARN: Removed duplicated region for block: B:32:? A[RETURN, SYNTHETIC] */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public void run() {
            Cursor cursor;
            Bundle data = this.mMessage.getData();
            String string = data.getString("title");
            String string2 = data.getString("url");
            String string3 = data.getString("itemUrl");
            Boolean valueOf = Boolean.valueOf(data.getBoolean("toDefaultThumbnail"));
            ContentResolver contentResolver = SiteNavigationAddDialog.this.getContentResolver();
            Cursor cursor2 = null;
            try {
                try {
                    cursor = contentResolver.query(SiteNavigation.SITE_NAVIGATION_URI, new String[]{"_id"}, "url = ? COLLATE NOCASE", new String[]{string3}, null);
                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("title", string);
                                contentValues.put("url", string2);
                                contentValues.put("website", "1");
                                if (valueOf.booleanValue()) {
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    BitmapFactory.decodeResource(SiteNavigationAddDialog.this.getResources(), R.raw.sitenavigation_thumbnail_default).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    contentValues.put("thumbnail", byteArrayOutputStream.toByteArray());
                                }
                                Uri withAppendedId = ContentUris.withAppendedId(SiteNavigation.SITE_NAVIGATION_URI, cursor.getLong(0));
                                Log.d("@M_browser/AddSiteNavigationPage", "SaveSiteNavigationRunnable uri is : " + withAppendedId);
                                contentResolver.update(withAppendedId, contentValues, null, null);
                                if (cursor == null) {
                                    cursor.close();
                                    return;
                                }
                                return;
                            }
                        } catch (IllegalStateException e) {
                            e = e;
                            cursor2 = cursor;
                            Log.e("@M_browser/AddSiteNavigationPage", "saveSiteNavigationItem", e);
                            if (cursor2 != null) {
                                cursor2.close();
                                return;
                            }
                            return;
                        } catch (Throwable th) {
                            th = th;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                    Log.e("@M_browser/AddSiteNavigationPage", "saveSiteNavigationItem the item does not exist!");
                    if (cursor == null) {
                    }
                } catch (IllegalStateException e2) {
                    e = e2;
                }
            } catch (Throwable th2) {
                th = th2;
                cursor = cursor2;
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:48:0x0105  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x0106 A[Catch: UnsupportedEncodingException -> 0x0110, URISyntaxException -> 0x01c2, TryCatch #3 {UnsupportedEncodingException -> 0x0110, blocks: (B:46:0x00f8, B:49:0x0106, B:50:0x010f), top: B:96:0x00f8, outer: #1 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    boolean save() {
        String str;
        String trim = this.mName.getText().toString().trim();
        String fixUrl = UrlUtils.fixUrl(this.mAddress.getText().toString());
        boolean z = trim.length() == 0;
        boolean z2 = fixUrl.trim().length() == 0;
        Resources resources = getResources();
        if (z || z2) {
            if (z) {
                this.mName.setError(resources.getText(R.string.website_needs_title));
            }
            if (z2) {
                this.mAddress.setError(resources.getText(R.string.website_needs_url));
            }
            return false;
        } else if (!trim.equals(this.mItemName) && isSiteNavigationTitle(this, trim)) {
            this.mName.setError(resources.getText(R.string.duplicate_site_navigation_title));
            return false;
        } else {
            String trim2 = fixUrl.trim();
            try {
                if (!trim2.toLowerCase().startsWith("javascript:")) {
                    String scheme = new URI(trim2).getScheme();
                    try {
                        if (!urlHasAcceptableScheme(trim2)) {
                            if (scheme != null) {
                                this.mAddress.setError(resources.getText(R.string.site_navigation_cannot_save_url));
                                return false;
                            }
                            try {
                                WebAddress webAddress = new WebAddress(fixUrl);
                                if (webAddress.getHost().length() == 0) {
                                    throw new URISyntaxException("", "");
                                }
                                str = webAddress.toString();
                            } catch (ParseException e) {
                                throw new URISyntaxException("", "");
                            }
                        } else {
                            int i = -1;
                            if (trim2 != null) {
                                i = trim2.indexOf("://");
                            }
                            if (i > 0 && trim2.indexOf("/", i + "://".length()) < 0) {
                                str = trim2 + "/";
                                Log.d("@M_browser/AddSiteNavigationPage", "URL=" + str);
                            }
                            if (trim2.length() == trim2.getBytes("UTF-8").length) {
                                throw new URISyntaxException("", "");
                            }
                        }
                        if (trim2.length() == trim2.getBytes("UTF-8").length) {
                        }
                    } catch (UnsupportedEncodingException e2) {
                        throw new URISyntaxException("", "");
                    }
                    trim2 = str;
                }
                try {
                    String path = new URL(trim2).getPath();
                    if ((path.equals("/") && trim2.endsWith(".")) || (path.equals("") && trim2.endsWith(".."))) {
                        this.mAddress.setError(resources.getText(R.string.bookmark_url_not_valid));
                        return false;
                    } else if (!this.mItemUrl.equals(trim2) && isSiteNavigationUrl(this, trim2, trim2)) {
                        this.mAddress.setError(resources.getText(R.string.duplicate_site_navigation_url));
                        return false;
                    } else {
                        if (trim2.startsWith("about:blank")) {
                            trim2 = this.mItemUrl;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("title", trim);
                        bundle.putString("url", trim2);
                        bundle.putString("itemUrl", this.mItemUrl);
                        if (!this.mItemUrl.equals(trim2)) {
                            bundle.putBoolean("toDefaultThumbnail", true);
                        } else {
                            bundle.putBoolean("toDefaultThumbnail", false);
                        }
                        Message obtain = Message.obtain(this.mHandler, 100);
                        obtain.setData(bundle);
                        new Thread(new SaveSiteNavigationRunnable(obtain)).start();
                        return true;
                    }
                } catch (MalformedURLException e3) {
                    this.mAddress.setError(resources.getText(R.string.bookmark_url_not_valid));
                    return false;
                }
            } catch (URISyntaxException e4) {
                this.mAddress.setError(resources.getText(R.string.bookmark_url_not_valid));
                return false;
            }
        }
    }

    public static boolean isSiteNavigationUrl(Context context, String str, String str2) {
        Cursor cursor = null;
        try {
            try {
                Cursor query = context.getContentResolver().query(SiteNavigation.SITE_NAVIGATION_URI, new String[]{"title"}, "url = ? COLLATE NOCASE OR url = ? COLLATE NOCASE", new String[]{str, str2}, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            Log.d("@M_browser/AddSiteNavigationPage", "isSiteNavigationUrl will return true.");
                            if (query != null) {
                                query.close();
                            }
                            return true;
                        }
                    } catch (IllegalStateException e) {
                        e = e;
                        cursor = query;
                        Log.e("@M_browser/AddSiteNavigationPage", "isSiteNavigationUrl", e);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        cursor = query;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (query != null) {
                    query.close();
                }
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (IllegalStateException e2) {
            e = e2;
        }
        return false;
    }

    public static boolean isSiteNavigationTitle(Context context, String str) {
        Cursor cursor = null;
        try {
            try {
                Cursor query = context.getContentResolver().query(SiteNavigation.SITE_NAVIGATION_URI, new String[]{"title"}, "title = ?", new String[]{str}, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            Log.d("@M_browser/AddSiteNavigationPage", "isSiteNavigationTitle will return true.");
                            if (query != null) {
                                query.close();
                            }
                            return true;
                        }
                    } catch (IllegalStateException e) {
                        e = e;
                        cursor = query;
                        Log.e("@M_browser/AddSiteNavigationPage", "isSiteNavigationTitle", e);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        cursor = query;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (query != null) {
                    query.close();
                }
            } catch (IllegalStateException e2) {
                e = e2;
            }
            return false;
        } catch (Throwable th2) {
            th = th2;
        }
    }

    private static boolean urlHasAcceptableScheme(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < ACCEPTABLE_WEBSITE_SCHEMES.length; i++) {
            if (str.startsWith(ACCEPTABLE_WEBSITE_SCHEMES[i])) {
                return true;
            }
        }
        return false;
    }
}
