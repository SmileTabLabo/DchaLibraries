package com.android.browser.sitenavigation;

import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.browser.R;
import com.android.browser.sitenavigation.TemplateSiteNavigation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class RequestHandlerSiteNavigation extends Thread {
    private static final UriMatcher S_URI_MATCHER = new UriMatcher(-1);
    Context mContext;
    OutputStream mOutput;
    Uri mUri;

    static {
        S_URI_MATCHER.addURI("com.android.browser.site_navigation", "websites/res/*/*", 2);
        S_URI_MATCHER.addURI("com.android.browser.site_navigation", "websites", 1);
    }

    public RequestHandlerSiteNavigation(Context context, Uri uri, OutputStream outputStream) {
        this.mUri = uri;
        this.mContext = context.getApplicationContext();
        this.mOutput = outputStream;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        super.run();
        try {
            try {
                doHandleRequest();
            } catch (IOException e) {
                Log.e("RequestHandlerSiteNavigation", "Failed to handle request: " + this.mUri, e);
            }
        } finally {
            cleanup();
        }
    }

    void doHandleRequest() throws IOException {
        switch (S_URI_MATCHER.match(this.mUri)) {
            case 1:
                writeTemplatedIndex();
                return;
            case 2:
                writeResource(getUriResourcePath());
                return;
            default:
                return;
        }
    }

    private void writeTemplatedIndex() throws IOException {
        Cursor query;
        TemplateSiteNavigation cachedTemplate = TemplateSiteNavigation.getCachedTemplate(this.mContext, R.raw.site_navigation);
        Cursor cursor = null;
        try {
            query = this.mContext.getContentResolver().query(Uri.parse("content://com.android.browser.site_navigation/websites"), new String[]{"url", "title", "thumbnail"}, null, null, null);
        } catch (Throwable th) {
            th = th;
        }
        try {
            cachedTemplate.assignLoop("site_navigation", new TemplateSiteNavigation.CursorListEntityWrapper(query) { // from class: com.android.browser.sitenavigation.RequestHandlerSiteNavigation.1
                @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.EntityData
                public void writeValue(OutputStream outputStream, String str) throws IOException {
                    Cursor cursor2 = getCursor();
                    if (str.equals("url")) {
                        outputStream.write(RequestHandlerSiteNavigation.this.htmlEncode(cursor2.getString(0)));
                    } else if (!str.equals("title")) {
                        if (str.equals("thumbnail")) {
                            outputStream.write("data:image/png;base64,".getBytes());
                            outputStream.write(Base64.encode(cursor2.getBlob(2), 0));
                        }
                    } else {
                        String string = cursor2.getString(1);
                        if (string == null || string.length() == 0) {
                            string = RequestHandlerSiteNavigation.this.mContext.getString(R.string.sitenavigation_add);
                        }
                        outputStream.write(RequestHandlerSiteNavigation.this.htmlEncode(string));
                    }
                }
            });
            cachedTemplate.write(this.mOutput);
            if (query != null) {
                query.close();
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

    byte[] htmlEncode(String str) {
        return TextUtils.htmlEncode(str).getBytes();
    }

    String getUriResourcePath() {
        Matcher matcher = Pattern.compile("/?res/([\\w/]+)").matcher(this.mUri.getPath());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return this.mUri.getPath();
    }

    void writeResource(String str) throws IOException {
        Resources resources = this.mContext.getResources();
        int identifier = resources.getIdentifier(str, null, R.class.getPackage().getName());
        if (identifier != 0) {
            InputStream openRawResource = resources.openRawResource(identifier);
            byte[] bArr = new byte[4096];
            while (true) {
                int read = openRawResource.read(bArr);
                if (read > 0) {
                    this.mOutput.write(bArr, 0, read);
                } else {
                    openRawResource.close();
                    return;
                }
            }
        }
    }

    void cleanup() {
        try {
            this.mOutput.close();
        } catch (IOException e) {
            Log.e("RequestHandlerSiteNavigation", "Failed to close pipe!", e);
        }
    }
}
