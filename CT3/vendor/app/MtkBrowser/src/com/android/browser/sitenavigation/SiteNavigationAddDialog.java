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
import com.android.browser.UrlUtils;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
/* loaded from: b.zip:com/android/browser/sitenavigation/SiteNavigationAddDialog.class */
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
    private View.OnClickListener mOKListener = new View.OnClickListener(this) { // from class: com.android.browser.sitenavigation.SiteNavigationAddDialog.1
        final SiteNavigationAddDialog this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.this$0.save()) {
                this.this$0.setResult(-1, new Intent().putExtra("need_refresh", true));
                this.this$0.finish();
            }
        }
    };
    private View.OnClickListener mCancelListener = new View.OnClickListener(this) { // from class: com.android.browser.sitenavigation.SiteNavigationAddDialog.2
        final SiteNavigationAddDialog this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.this$0.finish();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/sitenavigation/SiteNavigationAddDialog$SaveSiteNavigationRunnable.class */
    public class SaveSiteNavigationRunnable implements Runnable {
        private Message mMessage;
        final SiteNavigationAddDialog this$0;

        public SaveSiteNavigationRunnable(SiteNavigationAddDialog siteNavigationAddDialog, Message message) {
            this.this$0 = siteNavigationAddDialog;
            this.mMessage = message;
        }

        @Override // java.lang.Runnable
        public void run() {
            Bundle data = this.mMessage.getData();
            String string = data.getString("title");
            String string2 = data.getString("url");
            String string3 = data.getString("itemUrl");
            boolean z = data.getBoolean("toDefaultThumbnail");
            ContentResolver contentResolver = this.this$0.getContentResolver();
            Cursor cursor = null;
            Cursor cursor2 = null;
            try {
                try {
                    Cursor query = contentResolver.query(SiteNavigation.SITE_NAVIGATION_URI, new String[]{"_id"}, "url = ? COLLATE NOCASE", new String[]{string3}, null);
                    if (query == null || !query.moveToFirst()) {
                        Log.e("@M_browser/AddSiteNavigationPage", "saveSiteNavigationItem the item does not exist!");
                    } else {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("title", string);
                        contentValues.put("url", string2);
                        contentValues.put("website", "1");
                        if (Boolean.valueOf(z).booleanValue()) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            BitmapFactory.decodeResource(this.this$0.getResources(), 2131165217).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            contentValues.put("thumbnail", byteArrayOutputStream.toByteArray());
                        }
                        Uri withAppendedId = ContentUris.withAppendedId(SiteNavigation.SITE_NAVIGATION_URI, query.getLong(0));
                        Log.d("@M_browser/AddSiteNavigationPage", "SaveSiteNavigationRunnable uri is : " + withAppendedId);
                        contentResolver.update(withAppendedId, contentValues, null, null);
                    }
                    if (query != null) {
                        query.close();
                    }
                } catch (IllegalStateException e) {
                    Log.e("@M_browser/AddSiteNavigationPage", "saveSiteNavigationItem", e);
                    if (0 != 0) {
                        cursor2.close();
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    public static boolean isSiteNavigationTitle(Context context, String str) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = contentResolver.query(SiteNavigation.SITE_NAVIGATION_URI, new String[]{"title"}, "title = ?", new String[]{str}, null);
                if (query == null || !query.moveToFirst()) {
                    if (query != null) {
                        query.close();
                        return false;
                    }
                    return false;
                }
                cursor2 = query;
                cursor = query;
                Log.d("@M_browser/AddSiteNavigationPage", "isSiteNavigationTitle will return true.");
                if (query != null) {
                    query.close();
                    return true;
                }
                return true;
            } catch (IllegalStateException e) {
                Log.e("@M_browser/AddSiteNavigationPage", "isSiteNavigationTitle", e);
                if (cursor2 != null) {
                    cursor2.close();
                    return false;
                }
                return false;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public static boolean isSiteNavigationUrl(Context context, String str, String str2) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            try {
                Cursor query = contentResolver.query(SiteNavigation.SITE_NAVIGATION_URI, new String[]{"title"}, "url = ? COLLATE NOCASE OR url = ? COLLATE NOCASE", new String[]{str, str2}, null);
                if (query == null || !query.moveToFirst()) {
                    if (query != null) {
                        query.close();
                        return false;
                    }
                    return false;
                }
                cursor2 = query;
                cursor = query;
                Log.d("@M_browser/AddSiteNavigationPage", "isSiteNavigationUrl will return true.");
                if (query != null) {
                    query.close();
                    return true;
                }
                return true;
            } catch (IllegalStateException e) {
                cursor = cursor2;
                Log.e("@M_browser/AddSiteNavigationPage", "isSiteNavigationUrl", e);
                if (cursor2 != null) {
                    cursor2.close();
                    return false;
                }
                return false;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
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

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        setContentView(2130968620);
        String str = null;
        String str2 = null;
        this.mMap = getIntent().getExtras();
        Log.d("@M_browser/AddSiteNavigationPage", "onCreate mMap is : " + this.mMap);
        if (this.mMap != null) {
            Bundle bundle2 = this.mMap.getBundle("websites");
            if (bundle2 != null) {
                this.mMap = bundle2;
            }
            str = this.mMap.getString("name");
            str2 = this.mMap.getString("url");
            this.mIsAdding = this.mMap.getBoolean("isAdding");
        }
        this.mItemUrl = str2;
        this.mItemName = str;
        this.mName = (EditText) findViewById(2131558407);
        this.mName.setText(str);
        this.mAddress = (EditText) findViewById(2131558456);
        if (str2.startsWith("about:blank")) {
            this.mAddress.setText("about:blank");
        } else {
            this.mAddress.setText(str2);
        }
        this.mDialogText = (TextView) findViewById(2131558512);
        if (this.mIsAdding) {
            this.mDialogText.setText(2131492899);
        }
        this.mButtonOK = (Button) findViewById(2131558463);
        this.mButtonOK.setOnClickListener(this.mOKListener);
        this.mButtonCancel = (Button) findViewById(2131558462);
        this.mButtonCancel.setOnClickListener(this.mCancelListener);
        if (getWindow().getDecorView().isInTouchMode()) {
            return;
        }
        this.mButtonOK.requestFocus();
    }

    boolean save() {
        String str;
        String trim = this.mName.getText().toString().trim();
        String fixUrl = UrlUtils.fixUrl(this.mAddress.getText().toString());
        boolean z = trim.length() == 0;
        boolean z2 = fixUrl.trim().length() == 0;
        Resources resources = getResources();
        if (z || z2) {
            if (z) {
                this.mName.setError(resources.getText(2131492903));
            }
            if (z2) {
                this.mAddress.setError(resources.getText(2131492904));
                return false;
            }
            return false;
        } else if (!trim.equals(this.mItemName) && isSiteNavigationTitle(this, trim)) {
            this.mName.setError(resources.getText(2131492911));
            return false;
        } else {
            String trim2 = fixUrl.trim();
            String str2 = trim2;
            try {
                if (!trim2.toLowerCase().startsWith("javascript:")) {
                    String scheme = new URI(trim2).getScheme();
                    if (urlHasAcceptableScheme(trim2)) {
                        int i = -1;
                        if (trim2 != null) {
                            i = trim2.indexOf("://");
                        }
                        str = trim2;
                        if (i > 0) {
                            str = trim2;
                            if (trim2.indexOf("/", "://".length() + i) < 0) {
                                str = trim2 + "/";
                                Log.d("@M_browser/AddSiteNavigationPage", "URL=" + str);
                            }
                        }
                    } else if (scheme != null) {
                        this.mAddress.setError(resources.getText(2131492905));
                        return false;
                    } else {
                        try {
                            WebAddress webAddress = new WebAddress(fixUrl);
                            if (webAddress.getHost().length() == 0) {
                                throw new URISyntaxException("", "");
                            }
                            str = webAddress.toString();
                        } catch (ParseException e) {
                            throw new URISyntaxException("", "");
                        }
                    }
                    try {
                        str2 = str;
                        if (str.length() != str.getBytes("UTF-8").length) {
                            throw new URISyntaxException("", "");
                        }
                    } catch (UnsupportedEncodingException e2) {
                        throw new URISyntaxException("", "");
                    }
                }
                try {
                    String path = new URL(str2).getPath();
                    if ((path.equals("/") && str2.endsWith(".")) || (path.equals("") && str2.endsWith(".."))) {
                        this.mAddress.setError(resources.getText(2131493014));
                        return false;
                    } else if (!this.mItemUrl.equals(str2) && isSiteNavigationUrl(this, str2, str2)) {
                        this.mAddress.setError(resources.getText(2131492910));
                        return false;
                    } else {
                        String str3 = str2;
                        if (str2.startsWith("about:blank")) {
                            str3 = this.mItemUrl;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("title", trim);
                        bundle.putString("url", str3);
                        bundle.putString("itemUrl", this.mItemUrl);
                        if (this.mItemUrl.equals(str3)) {
                            bundle.putBoolean("toDefaultThumbnail", false);
                        } else {
                            bundle.putBoolean("toDefaultThumbnail", true);
                        }
                        Message obtain = Message.obtain(this.mHandler, 100);
                        obtain.setData(bundle);
                        new Thread(new SaveSiteNavigationRunnable(this, obtain)).start();
                        return true;
                    }
                } catch (MalformedURLException e3) {
                    this.mAddress.setError(resources.getText(2131493014));
                    return false;
                }
            } catch (URISyntaxException e4) {
                this.mAddress.setError(resources.getText(2131493014));
                return false;
            }
        }
    }
}
