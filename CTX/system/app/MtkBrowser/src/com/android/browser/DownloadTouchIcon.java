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
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class DownloadTouchIcon extends AsyncTask<String, Void, Void> {
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private Cursor mCursor;
    private Message mMessage;
    private final String mOriginalUrl;
    Tab mTab;
    private final String mUrl;
    private final String mUserAgent;

    public DownloadTouchIcon(Tab tab, Context context, ContentResolver contentResolver, WebView webView) {
        this.mTab = tab;
        this.mContext = context.getApplicationContext();
        this.mContentResolver = contentResolver;
        this.mOriginalUrl = webView.getOriginalUrl();
        this.mUrl = webView.getUrl();
        this.mUserAgent = webView.getSettings().getUserAgentString();
    }

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

    /* JADX WARN: Code restructure failed: missing block: B:49:0x00ca, code lost:
        if (r14 != null) goto L64;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x00ec, code lost:
        if (r14 == null) goto L13;
     */
    /* JADX WARN: Code restructure failed: missing block: B:68:0x00f8, code lost:
        if (r14 == null) goto L13;
     */
    /* JADX WARN: Code restructure failed: missing block: B:69:0x00fa, code lost:
        r14.disconnect();
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:64:0x00f2  */
    /* JADX WARN: Type inference failed for: r14v1 */
    /* JADX WARN: Type inference failed for: r14v5, types: [java.net.HttpURLConnection] */
    @Override // android.os.AsyncTask
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Void doInBackground(String... strArr) {
        HttpURLConnection httpURLConnection;
        if (this.mContentResolver != null) {
            this.mCursor = Bookmarks.queryCombinedForUrl(this.mContentResolver, this.mOriginalUrl, this.mUrl);
        }
        int i = 1;
        boolean z = this.mCursor != null && this.mCursor.getCount() > 0;
        if (z || this.mMessage != null) {
            try {
                try {
                    httpURLConnection = (HttpURLConnection) new URL(strArr[0]).openConnection();
                    try {
                        try {
                            if (this.mUserAgent != null) {
                                httpURLConnection.addRequestProperty("User-Agent", this.mUserAgent);
                            }
                            if (httpURLConnection.getResponseCode() == 200) {
                                InputStream inputStream = httpURLConnection.getInputStream();
                                try {
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    byte[] bArr = new byte[1024];
                                    int i2 = 0;
                                    while (true) {
                                        int read = inputStream.read(bArr, 0, 1024);
                                        if (read <= 0) {
                                            break;
                                        }
                                        byteArrayOutputStream.write(bArr, 0, read);
                                        i2 += read;
                                    }
                                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;
                                    BitmapFactory.decodeByteArray(byteArray, 0, i2, options);
                                    int i3 = options.outWidth;
                                    int i4 = options.outHeight;
                                    int integer = this.mContext.getResources().getInteger(R.integer.image_width);
                                    int integer2 = this.mContext.getResources().getInteger(R.integer.image_height);
                                    while (true) {
                                        if (i3 / i <= integer && i4 / i <= integer2) {
                                            break;
                                        }
                                        i *= 2;
                                    }
                                    options.inJustDecodeBounds = false;
                                    options.inSampleSize = i;
                                    Bitmap decodeByteArray = BitmapFactory.decodeByteArray(byteArray, 0, i2, options);
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
                        } catch (IOException e2) {
                        }
                    } catch (ClassCastException e3) {
                        e = e3;
                        Log.e("browser/DownloadTouchIcon", "Icon url cannot cast to HttpURLConnection:" + e);
                    }
                } catch (Throwable th) {
                    th = th;
                    if (strArr != 0) {
                        strArr.disconnect();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                httpURLConnection = null;
            } catch (ClassCastException e5) {
                e = e5;
                httpURLConnection = null;
            } catch (Throwable th2) {
                th = th2;
                strArr = 0;
                if (strArr != 0) {
                }
                throw th;
            }
        }
        if (this.mCursor != null) {
            this.mCursor.close();
        }
        if (this.mMessage != null) {
            this.mMessage.sendToTarget();
        }
        return null;
    }

    @Override // android.os.AsyncTask
    protected void onCancelled() {
        if (this.mCursor != null) {
            this.mCursor.close();
        }
    }

    private void storeIcon(Bitmap bitmap) {
        if (this.mTab != null) {
            synchronized (this.mTab) {
                if (this.mTab != null && this == this.mTab.mTouchIconLoader) {
                    this.mTab.mTouchIconLoader = null;
                }
            }
        }
        if (bitmap != null && this.mCursor != null && !isCancelled() && this.mCursor.moveToFirst()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            ContentValues contentValues = new ContentValues();
            contentValues.put("touch_icon", byteArrayOutputStream.toByteArray());
            do {
                contentValues.put("url_key", this.mCursor.getString(0));
                this.mContentResolver.update(BrowserContract.Images.CONTENT_URI, contentValues, null, null);
            } while (this.mCursor.moveToNext());
        }
    }
}
