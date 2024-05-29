package com.android.browser;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import com.android.browser.provider.BrowserContract;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/* loaded from: b.zip:com/android/browser/DownloadTouchIcon.class */
class DownloadTouchIcon extends AsyncTask<String, Void, Void> {
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private Cursor mCursor;
    private Message mMessage;
    private final String mOriginalUrl;
    Tab mTab;
    private final String mUrl;
    private final String mUserAgent;

    public DownloadTouchIcon(Context context, ContentResolver contentResolver, String str) {
        this.mTab = null;
        this.mContext = context.getApplicationContext();
        this.mContentResolver = contentResolver;
        this.mOriginalUrl = null;
        this.mUrl = str;
        this.mUserAgent = null;
    }

    public DownloadTouchIcon(Context context, Message message, String str) {
        this.mMessage = message;
        this.mContext = context.getApplicationContext();
        this.mContentResolver = null;
        this.mOriginalUrl = null;
        this.mUrl = null;
        this.mUserAgent = str;
    }

    public DownloadTouchIcon(Tab tab, Context context, ContentResolver contentResolver, WebView webView) {
        this.mTab = tab;
        this.mContext = context.getApplicationContext();
        this.mContentResolver = contentResolver;
        this.mOriginalUrl = webView.getOriginalUrl();
        this.mUrl = webView.getUrl();
        this.mUserAgent = webView.getSettings().getUserAgentString();
    }

    private void storeIcon(Bitmap bitmap) {
        if (this.mTab != null) {
            this.mTab.mTouchIconLoader = null;
        }
        if (bitmap == null || this.mCursor == null || isCancelled() || !this.mCursor.moveToFirst()) {
            return;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        ContentValues contentValues = new ContentValues();
        contentValues.put("touch_icon", byteArrayOutputStream.toByteArray());
        do {
            contentValues.put("url_key", this.mCursor.getString(0));
            this.mContentResolver.update(BrowserContract.Images.CONTENT_URI, contentValues, null, null);
        } while (this.mCursor.moveToNext());
    }

    @Override // android.os.AsyncTask
    public Void doInBackground(String... strArr) {
        int i;
        int i2;
        if (this.mContentResolver != null) {
            this.mCursor = Bookmarks.queryCombinedForUrl(this.mContentResolver, this.mOriginalUrl, this.mUrl);
        }
        boolean z = this.mCursor != null && this.mCursor.getCount() > 0;
        if (z || this.mMessage != null) {
            HttpURLConnection httpURLConnection = null;
            HttpURLConnection httpURLConnection2 = null;
            HttpURLConnection httpURLConnection3 = null;
            try {
                try {
                    try {
                        HttpURLConnection httpURLConnection4 = (HttpURLConnection) new URL(strArr[0]).openConnection();
                        if (this.mUserAgent != null) {
                            httpURLConnection4.addRequestProperty("User-Agent", this.mUserAgent);
                        }
                        if (httpURLConnection4.getResponseCode() == 200) {
                            InputStream inputStream = httpURLConnection4.getInputStream();
                            try {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                byte[] bArr = new byte[1024];
                                int i3 = 0;
                                while (true) {
                                    i = i3;
                                    int read = inputStream.read(bArr, 0, 1024);
                                    if (read <= 0) {
                                        break;
                                    }
                                    byteArrayOutputStream.write(bArr, 0, read);
                                    i3 = i + read;
                                }
                                byte[] byteArray = byteArrayOutputStream.toByteArray();
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeByteArray(byteArray, 0, i, options);
                                int i4 = options.outWidth;
                                int i5 = options.outHeight;
                                int integer = this.mContext.getResources().getInteger(2131623947);
                                int integer2 = this.mContext.getResources().getInteger(2131623948);
                                int i6 = 1;
                                while (true) {
                                    i2 = i6;
                                    if (i4 / i2 <= integer && i5 / i2 <= integer2) {
                                        break;
                                    }
                                    i6 = i2 * 2;
                                }
                                options.inJustDecodeBounds = false;
                                options.inSampleSize = i2;
                                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(byteArray, 0, i, options);
                                if (z) {
                                    storeIcon(decodeByteArray);
                                } else if (this.mMessage != null) {
                                    this.mMessage.getData().putParcelable("touch_icon", decodeByteArray);
                                }
                            } finally {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                        if (httpURLConnection4 != null) {
                            httpURLConnection4.disconnect();
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            httpURLConnection2.disconnect();
                        }
                        throw th;
                    }
                } catch (IOException e2) {
                    if (0 != 0) {
                        httpURLConnection3.disconnect();
                    }
                }
            } catch (ClassCastException e3) {
                Log.e("browser/DownloadTouchIcon", "Icon url cannot cast to HttpURLConnection:" + e3);
                if (0 != 0) {
                    httpURLConnection.disconnect();
                }
            }
        }
        if (this.mCursor != null) {
            this.mCursor.close();
        }
        if (this.mMessage != null) {
            this.mMessage.sendToTarget();
            return null;
        }
        return null;
    }

    @Override // android.os.AsyncTask
    protected void onCancelled() {
        if (this.mCursor != null) {
            this.mCursor.close();
        }
    }
}
